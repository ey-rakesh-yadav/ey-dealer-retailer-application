package com.eydms.facades.populators;

import com.eydms.facades.data.EYDMSAddressData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Objects;


public class EYDMSAddressReversePopulator implements Populator<EYDMSAddressData, AddressModel> {
    Logger LOG= Logger.getLogger(EYDMSAddressReversePopulator.class);

    @Override
    public void populate(EYDMSAddressData source, AddressModel target) throws ConversionException {
        target.setStreetname(source.getLine1());
        target.setStreetnumber(source.getLine2());
        target.setState(source.getState());
        target.setDistrict(source.getDistrict());
        target.setErpCity(source.getCity());
        target.setCity(source.getCity());
        target.setTaluka(source.getTaluka());
        target.setPostalcode(source.getPincode());
        if(Objects.nonNull(source.getDateOfBirth())) {
            DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            try{
            target.setDateOfBirth(format.parse(source.getDateOfBirth()));
            }catch (ParseException pe){
                LOG.error(pe);
                throw new ConversionException(String.format("Unable to parse Date for %s",AddressModel.DATEOFBIRTH));
            }
        }
        target.setEmail(source.getEmail());
        target.setCellphone(source.getContactNumber());
    }
}
