package com.eydms.facades.populators;

import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.facades.data.EYDMSAddressData;
import com.eydms.facades.data.EYDMSImageData;
import com.eydms.facades.data.EYDMSRetailerData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.apache.commons.lang.BooleanUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

public class EYDMSRetailerPopulator implements Populator<EyDmsCustomerModel, EYDMSRetailerData> {
    @Resource
    Converter<AddressModel, EYDMSAddressData> eydmsAddressConverter;
    @Resource
    private Converter<MediaModel, EYDMSImageData> eydmsImageConverter;
    @Override
    public void populate(EyDmsCustomerModel source, EYDMSRetailerData target) throws ConversionException {
        target.setName(source.getName());
        target.setEmail(source.getEmail());
        target.setContactNumber(source.getContactNumber());
        target.setAadharNo(source.getAadharNo());
        target.setGstin(source.getGstIN());
        target.setGstinCertificate(eydmsImageConverter.convert(source.getGstinCertificate()));
        target.setStateOfRegistration(source.getRegistrationState());
        var registeredAdrs= source.getAddresses().stream().filter(AddressModel::getBillingAddress).findFirst().orElse(null);
        if(null!=registeredAdrs) {
            var registeredAdr = eydmsAddressConverter.convert(registeredAdrs);
            target.setRegisteredAddress(registeredAdr);
        }
        List<EYDMSAddressData> shippingAdrs=new ArrayList<>();
        source.getAddresses().forEach(adr->{
            if(BooleanUtils.isTrue(adr.getShippingAddress())){
                shippingAdrs.add(eydmsAddressConverter.convert(adr));
            }
        });

        target.setShippingAddress(shippingAdrs);
        target.setAadharDoc(eydmsImageConverter.convert(source.getAadharPhoto()));
        target.setImage(eydmsImageConverter.convert(source.getImage()));
    }
}
