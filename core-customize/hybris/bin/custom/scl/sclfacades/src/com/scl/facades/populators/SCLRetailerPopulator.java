package com.scl.facades.populators;

import com.scl.core.model.SclCustomerModel;
import com.scl.facades.data.SCLAddressData;
import com.scl.facades.data.SCLImageData;
import com.scl.facades.data.SCLRetailerData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.apache.commons.lang.BooleanUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

public class SCLRetailerPopulator implements Populator<SclCustomerModel, SCLRetailerData> {
    @Resource
    Converter<AddressModel, SCLAddressData> sclAddressConverter;
    @Resource
    private Converter<MediaModel, SCLImageData> sclImageConverter;
    @Override
    public void populate(SclCustomerModel source, SCLRetailerData target) throws ConversionException {
        target.setName(source.getName());
        target.setEmail(source.getEmail());
        target.setContactNumber(source.getContactNumber());
        target.setAadharNo(source.getAadharNo());
        target.setGstin(source.getGstIN());
        target.setGstinCertificate(sclImageConverter.convert(source.getGstinCertificate()));
        target.setStateOfRegistration(source.getRegistrationState());
        var registeredAdrs= source.getAddresses().stream().filter(AddressModel::getBillingAddress).findFirst().orElse(null);
        if(null!=registeredAdrs) {
            var registeredAdr = sclAddressConverter.convert(registeredAdrs);
            target.setRegisteredAddress(registeredAdr);
        }
        List<SCLAddressData> shippingAdrs=new ArrayList<>();
        source.getAddresses().forEach(adr->{
            if(BooleanUtils.isTrue(adr.getShippingAddress())){
                shippingAdrs.add(sclAddressConverter.convert(adr));
            }
        });

        target.setShippingAddress(shippingAdrs);
        target.setAadharDoc(sclImageConverter.convert(source.getAadharPhoto()));
        target.setImage(sclImageConverter.convert(source.getImage()));
    }
}
