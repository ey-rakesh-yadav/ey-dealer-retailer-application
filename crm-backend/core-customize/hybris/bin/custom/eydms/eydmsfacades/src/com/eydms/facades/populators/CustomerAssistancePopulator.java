package com.eydms.facades.populators;

import com.eydms.core.enums.CurrentStageOfSiteConstruction;
import com.eydms.core.enums.TAExpertise;
import com.eydms.core.model.CustomerComplaintModel;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.core.model.TechnicalAssistanceModel;
import com.eydms.facades.data.CustomerComplaintData;
import com.eydms.facades.data.EYDMSAddressData;
import com.eydms.facades.data.EyDmsUserData;
import com.eydms.facades.data.TechnicalAssistanceData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.user.UserService;
import org.aspectj.weaver.tools.PointcutParameter;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;

public class CustomerAssistancePopulator implements Populator<CustomerComplaintModel, CustomerComplaintData> {

    private static final String DEFAULT_TSO = "defaulttso@shreecement.com" ;

    @Autowired
    Populator<AddressModel, EYDMSAddressData> eydmsAddressPopulator;

    @Autowired
    Converter<EyDmsUserModel, EyDmsUserData> eydmsUserConverter;

    @Autowired
    UserService userService;

    @Override
    public void populate(CustomerComplaintModel source, CustomerComplaintData target) throws ConversionException {

        target.setRequestStatus(source.getRequestStatus() != null ? source.getRequestStatus().getCode() : null);

        target.setRequestNo(source.getRequestNo());

        SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss");

        if(source.getRequestDate()!=null)
        {
           target.setRequestDate(source.getRequestDate());
        }

        target.setName(source.getName());
        target.setContactNumber(source.getAddress()!=null ? source.getAddress().getCellphone() : null);

        EYDMSAddressData address = new EYDMSAddressData();

        if(source.getAddress()!=null)
        {
            eydmsAddressPopulator.populate(source.getAddress(),address);
        }

        target.setAddress(address);

        if(source.getDateOfSupervisionRequired()!=null)
        {
            target.setDateOfSupervisionRequired(source.getDateOfSupervisionRequired());
        }

        if(source.getTsoAssignedDate()!=null)
        {
            target.setTsoAssignedDate(source.getTsoAssignedDate());
        }

        EyDmsUserModel user = (EyDmsUserModel) userService.getUserForUID(DEFAULT_TSO);
        target.setTsoAssigned(eydmsUserConverter.convert(user,new EyDmsUserData()));
        target.setConstructionStage(source.getConstructionStage() != null ? source.getConstructionStage().getCode() : null);
        target.setExpertiseRequired((source.getExpertiseRequired() != null) ? source.getExpertiseRequired().getCode() : null);
        target.setServiceStartDate(null);
        target.setServiceEndDate(null);

    }

}
