package com.eydms.facades.populators;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.eydms.core.jalo.EyDmsUser;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.core.model.SubAreaMasterModel;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.core.services.impl.SlctCrmIntegrationServiceImpl;
import com.eydms.facades.data.FilterTalukaData;
import com.eydms.facades.data.TsoBasicDetails;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.enums.InfluencerType;
import com.eydms.core.model.EyDmsCustomerModel;

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

public class EyDmsCustomerPopulator implements Populator<B2BCustomerModel, CustomerData>{
	
	@Autowired
	Converter<AddressModel, AddressData> addressConverter;
	private Converter<MediaModel, ImageData> imageConverter;

	@Autowired
	UserService userService;

	@Autowired
	TerritoryManagementService territoryManagementService;

	private static final Logger LOG = Logger.getLogger(EyDmsCustomerPopulator.class);

	@Override
	public void populate(B2BCustomerModel source, CustomerData target) throws ConversionException {
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


		UserGroupModel eydmsSiteGroup = userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.SITE_USER_GROUP_UID);
		if(source instanceof EyDmsCustomerModel) {
			if(source.getGroups() != null && source.getGroups().contains(eydmsSiteGroup)) {
				Date lastVisitTime = ((EyDmsCustomerModel) source).getLastVisitTime();
				target.setLastVisitDate(lastVisitTime);
				Boolean isShreeSite = ((EyDmsCustomerModel) source).getIsShreeSite() != null ? ((EyDmsCustomerModel) source).getIsShreeSite() : false;
				target.setIsShreeSite(isShreeSite);
				Boolean isBangurSite = ((EyDmsCustomerModel) source).getIsBangurSite() != null ? ((EyDmsCustomerModel) source).getIsBangurSite() : false ;
				target.setIsBangurSite(isBangurSite);
				Boolean isRockStrongSite = ((EyDmsCustomerModel) source).getIsRockstrongSite() != null ? ((EyDmsCustomerModel) source).getIsRockstrongSite() : false ;
				target.setIsRockstrongSite(isRockStrongSite);
				target.setContactPersonName(((EyDmsCustomerModel) source).getContactPersonName());
				target.setContactPersonContact(((EyDmsCustomerModel) source).getContactNumber());
			}
		}
		
		if(source instanceof EyDmsCustomerModel && ((EyDmsCustomerModel) source).getInfluencerType()!=null) {
				target.setInfluencerType(((EyDmsCustomerModel) source).getInfluencerType().getCode());
		}
		if(source instanceof EyDmsCustomerModel && ((EyDmsCustomerModel) source).getPanCard()!=null){
			target.setPAN(((EyDmsCustomerModel) source).getPanCard());
		}
		else{
			target.setPAN("");
		}
		if(source instanceof EyDmsCustomerModel && ((EyDmsCustomerModel) source).getGstIN()!=null){
			target.setGST(((EyDmsCustomerModel) source).getGstIN());
		}
		else{
			target.setGST("");
		}
		if(source instanceof EyDmsCustomerModel && ((EyDmsCustomerModel) source).getTanNo()!=null){
			target.setTAN(((EyDmsCustomerModel) source).getTanNo());
		}
		else{
			target.setTAN("");
		}
		
		
		if(source.getMobileNumber() != null) {
			target.setContactNumber(source.getMobileNumber());
		}
		if(source instanceof EyDmsCustomerModel){
			target.setCustomerNo(((EyDmsCustomerModel) source).getCustomerNo());
			LOG.info("Inside EYDMS Customer Populator: Customer No is " + ((EyDmsCustomerModel) source).getCustomerNo());
			if(source.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID))) {
				FilterTalukaData filterTalukaData = new FilterTalukaData();
				LOG.info("Inside EYDMS Customer Populator: It's outside the subarealist");
				List<SubAreaMasterModel> subAreaList = territoryManagementService.getTaulkaForUser(filterTalukaData);
				if(subAreaList!=null && !subAreaList.isEmpty()) {
					List<EyDmsUserModel> tsoList =  territoryManagementService.getTSOforSubAreas(subAreaList);
					LOG.info(" Inside EYDMS Customer Populator: subarealist is " + subAreaList);
					List<TsoBasicDetails> tsoBasicDetailsList = new ArrayList<>();
					if(tsoList!=null && !tsoList.isEmpty()) {
						LOG.info("Inside EYDMS Customer Populator: TSO List is " + tsoList);
						for(EyDmsUserModel tsoModel : tsoList) {
							LOG.info("Inside EYDMS Customer Populator: TSO Name:"+tsoModel.getName());
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
		}

	}

	private void populateProfilePicture(final MediaModel profilePicture, final CustomerData target) {
		final ImageData profileImageData = getImageConverter().convert(profilePicture);
		target.setProfilePicture(profileImageData);
	}

	private void populateDateOfJoining(final Date dateOfJoining, final CustomerData target) {
		DateFormat dateFormat = new SimpleDateFormat(EyDmsCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
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
