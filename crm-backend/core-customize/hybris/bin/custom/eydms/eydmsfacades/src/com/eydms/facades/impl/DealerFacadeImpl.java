package com.eydms.facades.impl;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.eydms.core.model.DealerModel;
import com.eydms.facades.CreditLimitData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.core.order.services.DealerTransitService;
import com.eydms.core.services.DealerService;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.facades.DealerFacade;
import com.eydms.facades.data.MonthlySalesData;
import com.eydms.facades.data.EYDMSAddressData;
import com.eydms.facades.data.EYDMSDealerSalesAllocationData;
import com.eydms.facades.data.EYDMSImageData;
import com.eydms.facades.data.EyDmsCustomerData;

import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.user.UserService;

public class DealerFacadeImpl implements DealerFacade{

	@Autowired
	UserService userService;
	
	@Autowired
	Converter<AddressModel, EYDMSAddressData> eydmsAddressConverter;
	
	@Autowired
	TerritoryManagementService territoryManagementService;
	
	@Resource
    private DealerTransitService dealerTransitService;
	
	@Resource
	DealerService dealerService;
	
	@Override
	public EyDmsCustomerData getCustomerProfile(String uid) {
		B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();

		if(currentUser!=null && currentUser instanceof EyDmsCustomerModel)
		{
			if((null == uid) || (null == currentUser.getUid()) || !currentUser.getUid().equalsIgnoreCase(uid))
			{
				throw new UnsupportedOperationException("Given uid" + uid + " " + "is not matching with logged in user " + currentUser.getUid());
			}
		}
		EyDmsCustomerModel customer = (EyDmsCustomerModel) userService.getUserForUID(uid);

		EyDmsCustomerData data = new EyDmsCustomerData();

		if(!Objects.isNull(customer))
		{
			EYDMSImageData profilePic = new EYDMSImageData();
			if(customer.getProfilePicture()!=null)
			{
				profilePic.setUrl(customer.getProfilePicture().getURL());
				data.setProfilePic(profilePic);
			}

			data.setName(customer.getName());
			data.setContactNumber(customer.getMobileNumber());
			data.setEmailId(customer.getEmail());
			data.setErpCustomerNo(customer.getCustomerNo());
			if(customer.getPanCard()!=null){
				data.setPanNo(customer.getPanCard());
			}
			if(customer.getTanNo()!=null){
				data.setTanNo(customer.getTanNo());
			}
			if(customer.getGstIN()!=null){
				data.setGstIn(customer.getGstIN());
			}

			EyDmsUserModel salesOfficer = territoryManagementService.getSOforCustomer(customer);
			if(!Objects.isNull(salesOfficer))
			{
				data.setSalesOfficer(salesOfficer.getName());
				data.setSalesOfficerProfilePic(salesOfficer.getProfilePicture()!=null ? salesOfficer.getProfilePicture().getURL() : null);
				data.setSalesOfficerMobileNumber(salesOfficer.getMobileNumber());
			}

			Collection<AddressModel> list = customer.getAddresses();
			if(CollectionUtils.isNotEmpty(list)) {
				List<AddressModel> billingAddressList = list.stream().filter(a -> a.getBillingAddress()).collect(Collectors.toList());
				if(billingAddressList != null && !billingAddressList.isEmpty()) {
					AddressModel billingAddress = billingAddressList.get(0);
					if(null != billingAddress)
					{
						data.setAddress((eydmsAddressConverter.convert(billingAddress)));
					}
				}
			}

			Set<PrincipalGroupModel> ugSet = customer.getGroups();

			if(ugSet.contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))
			 {
				data.setRetailerCount(territoryManagementService.getRetailerListForDealer().size());
				data.setInfluencerCount(territoryManagementService.getInfluencerListForDealer().size());

			 }
			 else if(ugSet.contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))
			 {
				 data.setInfluencerCount(territoryManagementService.getInfluencerListForRetailer().size());
			 }

			data.setFleetCount(dealerTransitService.fetchVehicleDetailsForDealer(customer).size());
			data.setUid(customer.getUid());

			Date doj = customer.getDateOfJoining();

			if(doj!=null)
			{
				int year = Calendar.getInstance().get(Calendar.YEAR);

				Calendar cal = Calendar.getInstance();
				cal.setTime(doj);

				int yearOfJoining = cal.get(Calendar.YEAR);

				data.setYearsOfAssociation(year-yearOfJoining);
			}


		}

		return data;
	}

	@Override
	public List<MonthlySalesData> getLastSixMonthSalesForDealer(String userId, String filter, String customerType,String customerId) {

		return dealerService.getLastSixMonthSalesForDealer(userId, filter, customerType,customerId);
	}

	@Override
	public CreditLimitData getHighPriorityActions(String uid) {

		return dealerService.getHighPriorityActions(uid);
	}

	@Override
	public List<CreditLimitData> getHighPriorityActionsForDealer(String uid){
		return dealerService.getHighPriorityActionsForDealer(uid);
	}
	
	@Override
	public EYDMSDealerSalesAllocationData getStockAllocationForDealer(String productCode) {
		return dealerService.getStockAllocationForDealer(productCode);
	}
	
	@Override
	public EYDMSDealerSalesAllocationData getStockAllocationForRetailer(String productCode) {
		return dealerService.getStockAllocationForRetailer(productCode);
	}
}
