package com.eydms.facades.populators;

import com.eydms.facades.data.EYDMSAddressData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class EYDMSAddressPopulator implements Populator<AddressModel, EYDMSAddressData> {
    @Override
    public void populate(AddressModel source, EYDMSAddressData target) throws ConversionException {
    target.setLine1(source.getLine1());
    target.setLine2(source.getLine2());
    target.setCity(source.getCity());
    target.setDistrict(source.getDistrict());
    target.setTaluka(source.getTaluka());
    target.setState(source.getState());
    target.setPincode(source.getPostalcode());
    target.setEmail(source.getEmail());
    if(Objects.nonNull(source.getDateOfBirth())){
        target.setDateOfBirth(getFormattedDate(source.getDateOfBirth()));
    }
    target.setContactNumber(source.getCellphone());
    }

    private String getFormattedDate(Date dateOfBirth) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
       return sdf.format(dateOfBirth);
    }
}
