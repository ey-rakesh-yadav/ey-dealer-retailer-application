package com.scl.facades.populators;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.model.SclCustomerModel;

import de.hybris.platform.commercefacades.product.data.ImageData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.enums.PhoneContactInfoType;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.user.AbstractContactInfoModel;
import de.hybris.platform.core.model.user.PhoneContactInfoModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;


public class DealerBasicPopulator implements Populator<SclCustomerModel, CustomerData> {

	@Autowired
	UserService userService;
	private Converter<MediaModel, ImageData> imageConverter;


	
	
	public Converter<MediaModel, ImageData> getImageConverter() {
		return imageConverter;
	}

	public void setImageConverter(Converter<MediaModel, ImageData> imageConverter) {
		this.imageConverter = imageConverter;
	}
	

	@Override
	public void populate(SclCustomerModel source, CustomerData target) throws ConversionException {
		
		target.setUid(source.getUid());
		target.setName(source.getName());
		target.setEmail(source.getEmail());
		if(source.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))){
			target.setPartnerType("Dealer");
			}
		else if(source.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
			target.setPartnerType("Retailer");
		}
		else {
			target.setPartnerType("Influencer");
		}


		if(null != source.getContactInfos())
		/*for(AbstractContactInfoModel contact: source.getContactInfos()) {
			if(contact instanceof PhoneContactInfoModel) {
				PhoneContactInfoModel phoneContact = (PhoneContactInfoModel)contact;
				if(PhoneContactInfoType.WORK.equals(phoneContact.getType())) {
					target.setContactNumber(phoneContact.getPhoneNumber());
					break;
				}
			}
		}*/

		//changing customer id to customer account no
		target.setCustomerId(source.getCustomerNo());
		target.setContactNumber(source.getMobileNumber());

		
		if(null!=source.getProfilePicture()){
			populateProfilePicture(source.getProfilePicture(),target);
		}
		if(source.getState()!=null)
			target.setState(source.getState());

		target.setIsBillingBlock(source.getIsBillingBlock());
		target.setIsDeliveryBlock(source.getIsDeliveryBlock());
		target.setIsOrderBlock(source.getIsOrderBlock());

	}

	private void populateProfilePicture(final MediaModel profilePicture, final CustomerData target) {
		final ImageData profileImageData = getImageConverter().convert(profilePicture);
		target.setProfilePicture(profileImageData);
	}
}
