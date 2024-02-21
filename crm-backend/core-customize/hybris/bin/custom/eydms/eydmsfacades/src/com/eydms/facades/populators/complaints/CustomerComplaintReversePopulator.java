package com.eydms.facades.populators.complaints;

import com.eydms.core.customer.services.EyDmsCustomerService;
import com.eydms.core.enums.CurrentStageOfSiteConstruction;
import com.eydms.core.enums.TAExpertise;
import com.eydms.core.model.CustomerComplaintModel;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.facades.data.CustomerComplaintData;
import com.eydms.facades.data.EYDMSAddressData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.keygenerator.impl.PersistentKeyGenerator;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.Date;
import java.util.Objects;

public class CustomerComplaintReversePopulator implements Populator<CustomerComplaintData, CustomerComplaintModel> {

    private static final String DEFAULT_TSO = "defaulttso@shreecement.com" ;
    private static final String EYDMSCUSTOMER1 = "eydmscustomer1";
    private Converter<EYDMSAddressData, AddressModel> eydmsAddressReverseConverter;
    private UserService userService;
    private EyDmsCustomerService eydmsCustomerService;
    private PersistentKeyGenerator customerComplaintRequestNoGenerator;

    @Override
    public void populate(CustomerComplaintData source, CustomerComplaintModel target) throws ConversionException {
        AddressModel address = getEyDmsAddressReverseConverter().convert(source.getAddress(), new AddressModel());
        if(Objects.nonNull(source.getCode()) && Objects.nonNull(getEyDmsCustomerService().getEyDmsCustomerForUid(source.getCode()))){
            address.setOwner((getEyDmsCustomerService().getEyDmsCustomerForUid(source.getCode())));
        }else {
            address.setOwner(getEyDmsCustomerService().getEyDmsCustomerForUid(EYDMSCUSTOMER1));
        }
        target.setAddress(address);
        target.setTsoAssigned((EyDmsUserModel) getUserService().getUserForUID(DEFAULT_TSO));
        target.setName(source.getName());
        target.setConstructionStage(source.getConstructionStage() != null ? CurrentStageOfSiteConstruction.valueOf(source.getConstructionStage()) : null);
        target.setDateOfSupervisionRequired(source.getDateOfSupervisionRequired());
        target.setExpertiseRequired((source.getExpertiseRequired() != null) ? TAExpertise.valueOf(source.getExpertiseRequired()) : null);
        target.setDurationOfSupervisionRequired(source.getDurationOfSupervisionRequired());
        target.setRequestDate(new Date());
        target.setRequestNo((String) getCustomerComplaintRequestNoGenerator().generate());
        target.setTsoAssignedDate(new Date());

    }

    public Converter<EYDMSAddressData, AddressModel> getEyDmsAddressReverseConverter() {
        return eydmsAddressReverseConverter;
    }

    public void setEyDmsAddressReverseConverter(Converter<EYDMSAddressData, AddressModel> eydmsAddressReverseConverter) {
        this.eydmsAddressReverseConverter = eydmsAddressReverseConverter;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public EyDmsCustomerService getEyDmsCustomerService() {
        return eydmsCustomerService;
    }

    public void setEyDmsCustomerService(EyDmsCustomerService eydmsCustomerService) {
        this.eydmsCustomerService = eydmsCustomerService;
    }

    public PersistentKeyGenerator getCustomerComplaintRequestNoGenerator() {
        return customerComplaintRequestNoGenerator;
    }

    public void setCustomerComplaintRequestNoGenerator(PersistentKeyGenerator customerComplaintRequestNoGenerator) {
        this.customerComplaintRequestNoGenerator = customerComplaintRequestNoGenerator;
    }
}
