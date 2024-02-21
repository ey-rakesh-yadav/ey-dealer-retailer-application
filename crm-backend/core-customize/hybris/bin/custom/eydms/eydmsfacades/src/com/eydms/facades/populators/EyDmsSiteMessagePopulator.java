package com.eydms.facades.populators;

import de.hybris.platform.commercefacades.product.data.ImageData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.notificationfacades.data.SiteMessageData;
import de.hybris.platform.notificationservices.model.SiteMessageForCustomerModel;
import de.hybris.platform.notificationservices.model.SiteMessageModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

public class EyDmsSiteMessagePopulator implements Populator<SiteMessageForCustomerModel, SiteMessageData> {
    @Autowired
    UserService userService;

    @Autowired
    Converter<MediaModel, ImageData> imageConverter;

    @Override
    public void populate(SiteMessageForCustomerModel source, SiteMessageData target) {
        Assert.notNull(source, "Parameter source cannot be null.");
        Assert.notNull(target, "Parameter target cannot be null.");
        Assert.notNull(source.getMessage(), "Parameter source message cannot be null.");

        final SiteMessageModel message = source.getMessage();

        target.setEntryNumber(String.valueOf(message.getEntryNumber()));

        target.setOrderCode(message.getOrderCode());
        target.setDealerCode(message.getDealerCode());
        target.setSuggestions(message.getSuggestions());


        if (null!=message.getDealerCode()){
            UserModel userForUID = userService.getUserForUID(message.getDealerCode());
        if (null != userForUID.getProfilePicture()) {
            populateProfilePicture(userForUID.getProfilePicture(), target);
        }
       }
        if(null!=message.getStatus()) {
            target.setStatus(message.getStatus().getCode());
        }
        if(null!=message.getCategory()) {
            target.setCategory(message.getCategory().getCode());
        }
        if(null!=message.getExpiryDate()) {
            target.setExpiryDate(message.getExpiryDate().toString());
        }
        if(null!=message.getPk()) {
            target.setSiteMessageId(message.getPk().toString());
        }
        if(null!=message.getOrderStatus()) {
            target.setOrderStatus(message.getOrderStatus());
        }
        if(null!=message.getOrderType()) {
            target.setOrderType(message.getOrderType());
        }
    }

    private void populateProfilePicture(MediaModel profilePicture, SiteMessageData target) {
        final ImageData profileImageData = imageConverter.convert(profilePicture);
        if(null != profileImageData)
            target.setDealerImage(profileImageData);
    }
}
