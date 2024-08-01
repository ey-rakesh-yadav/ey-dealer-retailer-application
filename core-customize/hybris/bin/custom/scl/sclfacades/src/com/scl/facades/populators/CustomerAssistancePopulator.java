package com.scl.facades.populators;

import com.scl.core.enums.CurrentStageOfSiteConstruction;
import com.scl.core.enums.TAExpertise;
import com.scl.core.model.CustomerComplaintModel;
import com.scl.core.model.SclUserModel;
import com.scl.core.model.TechnicalAssistanceModel;
import com.scl.facades.data.CustomerComplaintData;
import com.scl.facades.data.SCLAddressData;
import com.scl.facades.data.SclUserData;
import com.scl.facades.data.TechnicalAssistanceData;
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
    Populator<AddressModel, SCLAddressData> sclAddressPopulator;

    @Autowired
    Converter<SclUserModel, SclUserData> sclUserConverter;

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

        SCLAddressData address = new SCLAddressData();

        if(source.getAddress()!=null)
        {
            sclAddressPopulator.populate(source.getAddress(),address);
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

        SclUserModel user = (SclUserModel) userService.getUserForUID(DEFAULT_TSO);
        target.setTsoAssigned(sclUserConverter.convert(user,new SclUserData()));
        target.setConstructionStage(source.getConstructionStage() != null ? source.getConstructionStage().getCode() : null);
        target.setExpertiseRequired((source.getExpertiseRequired() != null) ? source.getExpertiseRequired().getCode() : null);
        target.setServiceStartDate(null);
        target.setServiceEndDate(null);

    }

}
