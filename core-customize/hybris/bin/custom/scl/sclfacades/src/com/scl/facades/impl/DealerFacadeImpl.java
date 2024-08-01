package com.scl.facades.impl;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.scl.core.dao.DataConstraintDao;
import com.scl.core.dao.TerritoryMasterDao;
import com.scl.core.enums.CounterType;
import com.scl.core.enums.SclUserType;
import com.scl.core.model.*;
import com.scl.core.services.*;
import com.scl.facades.CreditLimitData;
import com.scl.facades.data.*;
import com.scl.occ.dto.LiftingBlockWsDTO;
import com.scl.occ.dto.OrderBlockWsDTO;
import com.scl.occ.dto.PartnerCustomerWsDTO;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.order.services.DealerTransitService;
import com.scl.facades.DealerFacade;

import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.user.UserService;

public class DealerFacadeImpl implements DealerFacade{

	public static final String ID = "%id";
	private static final Logger LOG = Logger.getLogger(DealerFacadeImpl.class);
	@Autowired
	UserService userService;
	
	@Autowired
	Converter<AddressModel, SCLAddressData> sclAddressConverter;
	
	@Autowired
	TerritoryManagementService territoryManagementService;
	
	@Resource
    private DealerTransitService dealerTransitService;
	
	@Resource
	DealerService dealerService;

	@Resource
	DealerStockAllocationService dealerStockAllocationService;

	@Autowired
	TerritoryMasterDao territoryMasterDao;
	@Autowired
	TerritoryMasterService territoryMasterService;

	@Autowired
	SclDealerRetailerService sclDealerRetailerService;

	@Autowired
	private DataConstraintDao dataConstraintDao;
	@Override
	public SclCustomerData getCustomerProfile(String uid) {
		B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();

		if(currentUser!=null && currentUser instanceof SclCustomerModel)
		{
			if((null == uid) || (null == currentUser.getUid()) || !currentUser.getUid().equalsIgnoreCase(uid))
			{
				throw new UnsupportedOperationException("Given uid" + uid + " " + "is not matching with logged in user " + currentUser.getUid());
			}
		}
	        	UserModel userModel =userService.getUserForUID(uid);
		SclCustomerModel customer = null;
		if(userModel!=null){
			if(userModel instanceof SclCustomerModel){
				customer = (SclCustomerModel) userModel;
			}
		}


		SclCustomerData data = new SclCustomerData();

		if(Objects.nonNull(customer))
		{
			SCLImageData profilePic = new SCLImageData();
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
			List<SclUserModel> salesOfficerList = new ArrayList<>();
			List<SclUserModel> matchedUserList;
            if(customer.getCounterType().equals(CounterType.DEALER) && Objects.nonNull(customer.getTerritoryCode())){
				matchedUserList =territoryMasterService.getUserByTerritory(customer.getTerritoryCode());
				if(CollectionUtils.isNotEmpty(matchedUserList)) {
					List<SclUserModel> filteredList = matchedUserList.stream().filter(u ->u.getUserType().equals(SclUserType.SO)).collect(Collectors.toList());
					salesOfficerList.addAll(filteredList);
				}
			} else if (customer.getCounterType().equals(CounterType.RETAILER)) {
                   SclCustomerModel matchedDealer=sclDealerRetailerService.getDealerForRetailer(customer);
				   if(Objects.nonNull(matchedDealer.getTerritoryCode())){
					   matchedUserList =territoryMasterService.getUserByTerritory(matchedDealer.getTerritoryCode());
					   if(CollectionUtils.isNotEmpty(matchedUserList)) {
						   List<SclUserModel> filteredList = matchedUserList.stream().filter(u ->u.getUserType().equals(SclUserType.SO)).collect(Collectors.toList());
						   salesOfficerList.addAll(filteredList);
					   }
				   }
			}
			//This to be yet to disucss for TSO & RH
			/*if(sclUserModel.getUserType().equals(SclUserType.RH) || sclUserModel.getUserType().equals(SclUserType.TSM)){
				if(CollectionUtils.isNotEmpty(sclUserModel.getTerritoryMaster())) {
					List<TerritoryMasterModel> territoryMasterModels=sclUserModel.getTerritoryMaster().stream().collect(Collectors.toList());
					List<TerritoryUserMappingModel> territoryUserMappingForUser=territoryMasterDao.getTerritoryUserMappingForUser((SclUserModel) currentUser,territoryMasterModels);
				   //get sclUser --> userType SO/DO-multiple
				}
			}*/

			//SclUserModel salesOfficer = territoryManagementService.getSOforCustomer(customer);
			if(CollectionUtils.isNotEmpty(salesOfficerList))
			{
				List<SalesOfficerData> salesOfficerDataList=new ArrayList<>();
				salesOfficerList.stream().forEach(so->{
					SalesOfficerData salesOfficer=new SalesOfficerData();
					salesOfficer.setSalesOfficer(so.getName());
					salesOfficer.setSalesOfficerProfilePic(so.getProfilePicture()!=null ? so.getProfilePicture().getURL() : null);
					salesOfficer.setSalesOfficerMobileNumber(so.getMobileNumber());
					salesOfficerDataList.add(salesOfficer);
				});
				data.setSalesOfficers(salesOfficerDataList);
			}
			//multiple list at data level

			Collection<AddressModel> list = customer.getAddresses();
			if(CollectionUtils.isNotEmpty(list)) {
				List<AddressModel> billingAddressList = list.stream().filter(a -> a.getBillingAddress()).collect(Collectors.toList());
				if(billingAddressList != null && !billingAddressList.isEmpty()) {
					AddressModel billingAddress = billingAddressList.get(0);
					if(null != billingAddress)
					{
						data.setAddress((sclAddressConverter.convert(billingAddress)));
					}
				}
			}

			Set<PrincipalGroupModel> ugSet = customer.getGroups();

			if(ugSet.contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))
			 {
				data.setRetailerCount(territoryManagementService.getRetailerListForDealer().size());
				data.setInfluencerCount(territoryManagementService.getInfluencerListForDealer().size());

			 }
			 else if(ugSet.contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))
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

	/**
	 *
	 * @return LiftingDateRangeData
	 */
	@Override
	public LiftingDateRangeData getLiftingDateRange(String customerId) throws Exception {
		SclCustomerModel customer=customerId!=null?(SclCustomerModel) userService.getUserForUID(customerId):null;
		return dealerStockAllocationService.getLiftingDateRange(customer);
	}

	/**
	 * @param selectedLiftingDate
	 * @param customerId
	 * @return
	 */
	@Override
	public boolean isValidSelectedDate(String selectedLiftingDate, String customerId) throws Exception {

		SclCustomerModel customer=customerId!=null?(SclCustomerModel) userService.getUserForUID(customerId):null;
		LiftingDateRangeData rangeData=dealerStockAllocationService.getLiftingDateRange(customer);
		LocalDate selectedDate= LocalDate.parse(selectedLiftingDate);
		LocalDate startDate= LocalDate.parse(rangeData.getStartDate());
		LocalDate endDate= LocalDate.parse(rangeData.getEndDate());

		return selectedDate.isAfter(startDate.minusDays(1)) && selectedDate.isBefore(endDate.plusDays(1));

	}

	/**
	 * @param searchPageData
	 * @param dealerUid
	 * @param customerId
	 * @param selectedLiftingDate
	 * @param productCode
	 * @param productAlias
	 * @param quantity
	 * @param filter
	 * @return
	 * @throws Exception
	 */
	@Override
	public InvoiceListData getInvoiceListForProduct(SearchPageData searchPageData, String dealerUid, String customerId, String selectedLiftingDate, String productCode, String productAlias, Double quantity, String filter) throws Exception {
		SclCustomerModel dealer=(SclCustomerModel) userService.getUserForUID(dealerUid);
		SclCustomerModel customer=customerId!=null?(SclCustomerModel) userService.getUserForUID(customerId):null;
		return dealerStockAllocationService.getInvoiceListForProduct(searchPageData,dealer,customer,selectedLiftingDate,productCode,productAlias,quantity,filter);

	}

	/**
	 *
	 * @param dealerId
	 * @param customerId
	 * @param selectedLiftingDate
	 * @return
	 * @throws Exception
	 */
	@Override
	public ProductStockAllocationListData getProductListForStockAllocation(String dealerId, String customerId, String selectedLiftingDate) throws Exception {
		SclCustomerModel dealer=(SclCustomerModel) userService.getUserForUID(dealerId);
		SclCustomerModel customer=customerId!=null?(SclCustomerModel) userService.getUserForUID(customerId):null;
		return dealerStockAllocationService.getProductListForStockAllocation(dealer,customer,selectedLiftingDate);
	}

	/**
     * @param dealerUid
     * @param retailerUid
     * @return orderBlockWsDTO
     */
	@Override
	public OrderBlockWsDTO getDealerOrderBlock(String dealerUid, String retailerUid) {

		OrderBlockWsDTO orderBlockWs = new OrderBlockWsDTO();
		SclCustomerModel sclCustomer = null;
		String errorMessage;
		if(StringUtils.isNotBlank(dealerUid)) {
			LOG.info(String.format("check is order allowed for dealer ::%s",dealerUid));
			sclCustomer = (SclCustomerModel) userService.getUserForUID(dealerUid);
		}
		else if(StringUtils.isNotBlank(retailerUid)){
			LOG.info(String.format("check is order allowed for retailer ::%s",retailerUid));
			 sclCustomer = (SclCustomerModel) userService.getUserForUID(retailerUid);
		}

     if(Objects.nonNull(sclCustomer)) {
		 String placeOrderEnabled;
		 //check app level block
		 if (userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)) ||
				 userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
			 placeOrderEnabled = dataConstraintDao.findVersionByConstraintName(SclCoreConstants.ORDER.UDAAN_CONNECT_PLACE_ORDER_ENABLED);
		 }
		 else{
			 placeOrderEnabled = dataConstraintDao.findVersionByConstraintName(SclCoreConstants.ORDER.UDAAN_PRO_PLACE_ORDER_ENABLED);
		 }

		 LOG.info(String.format("check placeOrderEnabled at app level ::%s",placeOrderEnabled));
		 if (BooleanUtils.isTrue(Boolean.valueOf(placeOrderEnabled))) {
			 //customer level check for block
			  if(CounterType.DEALER.equals(sclCustomer.getCounterType()) &&  BooleanUtils.isTrue(sclCustomer.getIsOrderBlock())){
				  setErrorMsg(sclCustomer, orderBlockWs);
			  }
			 else if (CounterType.RETAILER.equals(sclCustomer.getCounterType()) && (BooleanUtils.isTrue(sclCustomer.getIsOrderBlock()) && BooleanUtils.isTrue(sclCustomer.getIsBillingBlock()) && BooleanUtils.isTrue(sclCustomer.getIsDeliveryBlock())))
			 {
				 setErrorMsg(sclCustomer, orderBlockWs);
			 } else{
					 orderBlockWs.setIsPlaceOrderAllowed(Boolean.TRUE);
             }
             return orderBlockWs;
         }else if(BooleanUtils.isFalse(Boolean.valueOf(placeOrderEnabled))){
			 LOG.info(String.format("placeOrderEnabled at app level ::%s",placeOrderEnabled));
			 errorMessage = dataConstraintDao.findVersionByConstraintName(SclCoreConstants.ORDER.SAP_ORDER_PLACEMENT_BLOCKED);
			 orderBlockWs.setErrorMessage(errorMessage);
			 orderBlockWs.setIsPlaceOrderAllowed(Boolean.FALSE);
			 return orderBlockWs;
		 }
	 }else {
		 orderBlockWs.setErrorMessage(String.format("No valid Customer found with dealerUid::%s or retailer uid::%s ",dealerUid,retailerUid));
		 orderBlockWs.setIsPlaceOrderAllowed(Boolean.FALSE);
		 return orderBlockWs;
	 }
	 return orderBlockWs;
	}

	/**
	 *
	 * @param sclCustomer
	 * @param orderBlockWs
	 */
	private void setErrorMsg(SclCustomerModel sclCustomer, OrderBlockWsDTO orderBlockWs) {
		String errorMessage;
		if (userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
			errorMessage = dataConstraintDao.findVersionByConstraintName(SclCoreConstants.ORDER.SAP_CODE_BLOCKED_CUSTOMER);
		} else {
			errorMessage = dataConstraintDao.findVersionByConstraintName(SclCoreConstants.ORDER.SAP_CODE_BLOCKED);
		}
		if (StringUtils.isNotBlank(errorMessage)) {
			if (errorMessage.contains(ID)) {
				String errorMsg = errorMessage.replace(ID, sclCustomer.getUid());
				orderBlockWs.setErrorMessage(errorMsg);
				orderBlockWs.setIsPlaceOrderAllowed(Boolean.FALSE);
			}
		}
	}


	@Override
	public LiftingBlockWsDTO getCustomerLiftingBlock(String dealerUid, String retailerUid, String influencerUid) {

		LiftingBlockWsDTO liftingBlockWsDTO=new LiftingBlockWsDTO();
		SclCustomerModel sclCustomer = null;
		String errorMessage;
		if(StringUtils.isNotBlank(dealerUid)) {
			LOG.info(String.format("check is lifting allowed for dealer ::%s",dealerUid));
			sclCustomer = (SclCustomerModel) userService.getUserForUID(dealerUid);
		}
		else if(StringUtils.isNotBlank(retailerUid)){
			LOG.info(String.format("check is lifting allowed for retailer ::%s",retailerUid));
			sclCustomer = (SclCustomerModel) userService.getUserForUID(retailerUid);
		} else if(StringUtils.isNotBlank(influencerUid)){
			LOG.info(String.format("check is lifting allowed for Influencer ::%s", retailerUid));
			sclCustomer = (SclCustomerModel) userService.getUserForUID(retailerUid);
		}

		if(Objects.nonNull(sclCustomer)) {
			String liftingEnabled;
			//check app level block
			liftingEnabled = dataConstraintDao.findVersionByConstraintName(SclCoreConstants.ORDER.UDAAN_CONNECT_LIFTING_ENABLED);

			LOG.info(String.format("check Lifting Enabled at app level ::%s",liftingEnabled));
			if (BooleanUtils.isTrue(Boolean.valueOf(liftingEnabled))) {
				//customer level check for block
				boolean isBlocked=false;
				if(StringUtils.isNotEmpty(dealerUid) && sclCustomer.getCounterType().equals(CounterType.DEALER)){
					if (BooleanUtils.isTrue(sclCustomer.getIsOrderBlock())) {
						isBlocked=true;
					}
				}else if(StringUtils.isNotEmpty(retailerUid) && sclCustomer.getCounterType().equals(CounterType.RETAILER)){
					if (BooleanUtils.isTrue(sclCustomer.getIsOrderBlock()) && BooleanUtils.isTrue(sclCustomer.getIsBillingBlock()) && BooleanUtils.isTrue(sclCustomer.getIsDeliveryBlock()))
					{
						isBlocked=true;
					}
				}
				if (isBlocked) {
									errorMessage = dataConstraintDao.findVersionByConstraintName(SclCoreConstants.ORDER.CUSTOMER_LIFTING_BLOCKED);

					if (StringUtils.isNotBlank(errorMessage)) {
						if (errorMessage.contains(ID)) {
							String errorMsg = errorMessage.replace(ID, sclCustomer.getUid());
							liftingBlockWsDTO.setErrorMessage(errorMsg);
							liftingBlockWsDTO.setIsLiftingAllowed(Boolean.FALSE);
						}
					}
				} else{
					liftingBlockWsDTO.setIsLiftingAllowed(Boolean.TRUE);
				}
				return liftingBlockWsDTO;
			}else if(BooleanUtils.isFalse(Boolean.valueOf(liftingEnabled))){
				LOG.info(String.format("Lifting Enabled at app level ::%s",liftingEnabled));
				errorMessage = dataConstraintDao.findVersionByConstraintName(SclCoreConstants.ORDER.UDAAN_CONNECT_LIFTING_BLOCKED);
				liftingBlockWsDTO.setErrorMessage(errorMessage);
				liftingBlockWsDTO.setIsLiftingAllowed(Boolean.FALSE);
				return liftingBlockWsDTO;
			}
		}else {
			liftingBlockWsDTO.setErrorMessage(String.format("No valid Customer found with dealerUid::%s or retailer uid::%s or influence uid::%s ",dealerUid,retailerUid,influencerUid));
			liftingBlockWsDTO.setIsLiftingAllowed(Boolean.FALSE);
			return liftingBlockWsDTO;
		}
		return liftingBlockWsDTO;
	}

	@Override
	public SCLDealerSalesAllocationData getStockAllocationForDealer(String productCode) {
		return dealerService.getStockAllocationForDealer(productCode);
	}
	
	@Override
	public SCLDealerSalesAllocationData getStockAllocationForRetailer(String productCode) {
		return dealerService.getStockAllocationForRetailer(productCode);
	}


	/**
	 * @param dealerUid
	 * @return
	 */
	@Override
	public PartnerCustomerListData getPartnerCustomers(String dealerUid, boolean isManagePartnerWidget) {
		PartnerCustomerData sclCustomerData = new PartnerCustomerData();
		PartnerCustomerListData partnerCustomerListData = new PartnerCustomerListData();
		List<PartnerCustomerData> listData = new ArrayList<>();
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		SclCustomerModel sclCustomerModel = (SclCustomerModel) userService.getUserForUID(dealerUid);
		if(Objects.nonNull(sclCustomerModel)
				&& CollectionUtils.isNotEmpty(sclCustomerModel.getPartnerCustomer())
				&& sclCustomerModel.getPartnerCustomer().stream().anyMatch(PartnerCustomerModel::getActive)){
			Date current = new Date();
			if(!isManagePartnerWidget) {
				sclCustomerData.setName(sclCustomerModel.getName());
				sclCustomerData.setMobileNumber(sclCustomerModel.getContactNumber());
				listData.add(sclCustomerData);
			}
			List<PartnerCustomerModel> sortedPartnersList = sclCustomerModel.getPartnerCustomer().stream().sorted(Comparator.comparing(PartnerCustomerModel::getModifiedtime).reversed()).toList();
			List<PartnerCustomerData> partnerCustomerDatas = sortedPartnersList.stream()
					.filter(a -> (Objects.nonNull(a.getActive()) && a.getActive()) && (a.getValidityExpired().after(current)))
					.map(a->{
						PartnerCustomerData partnerCustomerData = new PartnerCustomerData();
						partnerCustomerData.setName(StringUtils.isNotEmpty(a.getName()) ? a.getName() : StringUtils.EMPTY);
						partnerCustomerData.setId(a.getId());
						partnerCustomerData.setMobileNumber(StringUtils.isNotEmpty(a.getMobileNumber()) ? a.getMobileNumber() : StringUtils.EMPTY);
						partnerCustomerData.setRole(StringUtils.isNotEmpty(a.getRole()) ? a.getRole() : StringUtils.EMPTY);
						if(Objects.nonNull(a.getValidityExpired())) {
							partnerCustomerData.setValidityExpired(a.getValidityExpired());
						}
						if (Objects.nonNull(a.getValidity())) {
							partnerCustomerData.setValidityInMonths(a.getValidity());
						}
						return partnerCustomerData;
					}).toList();
			listData.addAll(partnerCustomerDatas);
			partnerCustomerListData.setPartnerCustomerList(listData);
		}
		return partnerCustomerListData;
	}

	@Override
	public PartnerCustomerData saveExtendedPartnerInfo(PartnerCustomerData partnerCustomerData) {
		return dealerService.saveExtendedPartnerInfo(partnerCustomerData);
	}

	@Override
	public PartnerCustomerData updatePartnerCustomerInfo(PartnerCustomerData partnerCustomerData, String operationType) {
		SclCustomerModel dealer = (SclCustomerModel) userService.getCurrentUser();
		if(operationType.equalsIgnoreCase("edit")) {
			partnerCustomerData = dealerService.editPartnerCustomerInfo(partnerCustomerData,dealer,operationType);
		} else if (operationType.equalsIgnoreCase("delete")) {
			partnerCustomerData = dealerService.deletePartnerCustomer(partnerCustomerData,dealer,operationType);
		}

		return partnerCustomerData;
	}


}
