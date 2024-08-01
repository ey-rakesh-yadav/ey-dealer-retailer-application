package com.scl.facades.populators;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.scl.core.dao.DataConstraintDao;
import com.scl.core.jalo.SclUser;
import com.scl.core.model.SclUserModel;
import com.scl.core.model.SubAreaMasterModel;
import com.scl.core.services.TerritoryManagementService;
import com.scl.core.services.impl.SlctCrmIntegrationServiceImpl;
import com.scl.facades.data.FilterTalukaData;
import com.scl.facades.data.TsoBasicDetails;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.enums.InfluencerType;
import com.scl.core.model.SclCustomerModel;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.commercefacades.product.data.ImageData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.enums.PhoneContactInfoType;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.user.AbstractContactInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.PhoneContactInfoModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.user.UserService;

public class SclCustomerPopulator implements Populator<B2BCustomerModel, CustomerData>{
	
	@Autowired
	Converter<AddressModel, AddressData> addressConverter;
	private Converter<MediaModel, ImageData> imageConverter;

	@Autowired
	private DataConstraintDao dataConstraintDao;
	@Autowired
	UserService userService;

	@Autowired
	TerritoryManagementService territoryManagementService;

	private static final Logger LOG = Logger.getLogger(SclCustomerPopulator.class);
	public static final String FIND_SCLDEALER_PASSword = "FIND_SCLDEALER_PASSword";
	public static final String FIND_SCLRETAILER_PASSword = "FIND_SCLRETAILER_PASSword";



	@Override
	public void populate(B2BCustomerModel source, CustomerData target) throws ConversionException{
		Assert.notNull(source, "Parameter source cannot be null.");
		Assert.notNull(target, "Parameter target cannot be null.");
		target.setEmployeeId(source.getEmployeeCode());
		if(null!=source.getDateOfJoining()){
			populateDateOfJoining(source.getDateOfJoining(),target);
		}
		if(null!=source.getProfilePicture()){
			populateProfilePicture(source.getProfilePicture(),target);
		}
		target.setEmail(source.getEmail());
		/*for(AbstractContactInfoModel contact: source.getContactInfos()) {
			if(contact instanceof PhoneContactInfoModel) {
				PhoneContactInfoModel phoneContact = (PhoneContactInfoModel)contact;
				if(PhoneContactInfoType.WORK.equals(phoneContact.getType())) {
					target.setContactNumber(phoneContact.getPhoneNumber());
					break;
				}
			}
		}*/

		Collection<AddressModel> list = source.getAddresses();
		if(CollectionUtils.isNotEmpty(list)) {
			List<AddressModel> billingAddressList = list.stream().filter(address -> address.getBillingAddress()).collect(Collectors.toList());
			if(billingAddressList != null && !billingAddressList.isEmpty()) {
				AddressModel billingAddress = billingAddressList.get(0);
				if(null != billingAddress)
				{
					target.setDefaultAddress(getAddressConverter().convert(billingAddress));
				}
			}
		}


		UserGroupModel sclSiteGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SITE_USER_GROUP_UID);
		if(source instanceof SclCustomerModel) {
			if(source.getGroups() != null && source.getGroups().contains(sclSiteGroup)) {
				Date lastVisitTime = ((SclCustomerModel) source).getLastVisitTime();
				target.setLastVisitDate(lastVisitTime);
				Boolean isShreeSite = ((SclCustomerModel) source).getIsShreeSite() != null ? ((SclCustomerModel) source).getIsShreeSite() : false;
				target.setIsShreeSite(isShreeSite);
				Boolean isBangurSite = ((SclCustomerModel) source).getIsBangurSite() != null ? ((SclCustomerModel) source).getIsBangurSite() : false ;
				target.setIsBangurSite(isBangurSite);
				Boolean isRockStrongSite = ((SclCustomerModel) source).getIsRockstrongSite() != null ? ((SclCustomerModel) source).getIsRockstrongSite() : false ;
				target.setIsRockstrongSite(isRockStrongSite);
				target.setContactPersonName(((SclCustomerModel) source).getContactPersonName());
				target.setContactPersonContact(((SclCustomerModel) source).getContactNumber());
			}
		}
		
		if(source instanceof SclCustomerModel && ((SclCustomerModel) source).getInfluencerType()!=null) {
				target.setInfluencerType(((SclCustomerModel) source).getInfluencerType().getCode());
		}
		if(source instanceof SclCustomerModel && ((SclCustomerModel) source).getPanCard()!=null){
			target.setPAN(((SclCustomerModel) source).getPanCard());
		}
		else{
			target.setPAN("");
		}
		if(source instanceof SclCustomerModel && ((SclCustomerModel) source).getGstIN()!=null){
			target.setGST(((SclCustomerModel) source).getGstIN());
		}
		else{
			target.setGST("");
		}
		if(source instanceof SclCustomerModel && ((SclCustomerModel) source).getTanNo()!=null){
			target.setTAN(((SclCustomerModel) source).getTanNo());
		}
		else{
			target.setTAN("");
		}

		if(source.getMobileNumber() != null) {
			target.setContactNumber(source.getMobileNumber());
		}
		if(source instanceof SclCustomerModel){
			target.setCustomerNo(((SclCustomerModel) source).getCustomerNo());
			LOG.info("Inside SCL Customer Populator: Customer No is " + ((SclCustomerModel) source).getCustomerNo());
			if(source.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID))) {
				FilterTalukaData filterTalukaData = new FilterTalukaData();
				LOG.info("Inside SCL Customer Populator: It's outside the subarealist");
				List<SubAreaMasterModel> subAreaList = territoryManagementService.getTaulkaForUser(filterTalukaData);
				if(subAreaList!=null && !subAreaList.isEmpty()) {
					List<SclUserModel> tsoList =  territoryManagementService.getTSOforSubAreas(subAreaList);
					LOG.info(" Inside SCL Customer Populator: subarealist is " + subAreaList);
					List<TsoBasicDetails> tsoBasicDetailsList = new ArrayList<>();
					if(tsoList!=null && !tsoList.isEmpty()) {
						LOG.info("Inside SCL Customer Populator: TSO List is " + tsoList);
						for(SclUserModel tsoModel : tsoList) {
							LOG.info("Inside SCL Customer Populator: TSO Name:"+tsoModel.getName());
							TsoBasicDetails tsoBasicDetailsData = new TsoBasicDetails();
							tsoBasicDetailsData.setTsoName(tsoModel.getName());
							tsoBasicDetailsData.setTsoMobileNumber(tsoModel.getMobileNumber());
							tsoBasicDetailsData.setTsoProfilePic(tsoModel.getProfilePicture()!=null ? tsoModel.getProfilePicture().getURL() : null);
							tsoBasicDetailsList.add(tsoBasicDetailsData);
						}
						target.setLinkedTsoDetails(tsoBasicDetailsList);
					}
				}

			}

			 String password=dataConstraintDao.findPasswordByConstraintName(FIND_SCLDEALER_PASSword);
			 target.setPassword(password);
		}

	}

	private void populateProfilePicture(final MediaModel profilePicture, final CustomerData target) {
		final ImageData profileImageData = getImageConverter().convert(profilePicture);
		target.setProfilePicture(profileImageData);
	}

	private void populateDateOfJoining(final Date dateOfJoining, final CustomerData target) {
		DateFormat dateFormat = new SimpleDateFormat(SclCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
		String formattedDate = dateFormat.format(dateOfJoining);
		target.setDateOfJoining(formattedDate);
	}

	public Converter<AddressModel, AddressData> getAddressConverter() {
		return addressConverter;
	}

	public void setAddressConverter(Converter<AddressModel, AddressData> addressConverter) {
		this.addressConverter = addressConverter;
	}
	public Converter<MediaModel, ImageData> getImageConverter() {
		return imageConverter;
	}

	public void setImageConverter(Converter<MediaModel, ImageData> imageConverter) {
		this.imageConverter = imageConverter;
	}

}
