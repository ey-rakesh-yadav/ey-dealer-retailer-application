package com.scl.facades.populators.complaints;

import com.scl.core.customer.services.SclCustomerService;
import com.scl.core.enums.CurrentStageOfSiteConstruction;
import com.scl.core.enums.TAExpertise;
import com.scl.core.model.CustomerComplaintModel;
import com.scl.core.model.SclUserModel;
import com.scl.facades.data.CustomerComplaintData;
import com.scl.facades.data.SCLAddressData;
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
    private static final String SCLCUSTOMER1 = "sclcustomer1";
    private Converter<SCLAddressData, AddressModel> sclAddressReverseConverter;
    private UserService userService;
    private SclCustomerService sclCustomerService;
    private PersistentKeyGenerator customerComplaintRequestNoGenerator;

    @Override
    public void populate(CustomerComplaintData source, CustomerComplaintModel target) throws ConversionException {
        AddressModel address = getSclAddressReverseConverter().convert(source.getAddress(), new AddressModel());
        if(Objects.nonNull(source.getCode()) && Objects.nonNull(getSclCustomerService().getSclCustomerForUid(source.getCode()))){
            address.setOwner((getSclCustomerService().getSclCustomerForUid(source.getCode())));
        }else {
            address.setOwner(getSclCustomerService().getSclCustomerForUid(SCLCUSTOMER1));
        }
        target.setAddress(address);
        target.setTsoAssigned((SclUserModel) getUserService().getUserForUID(DEFAULT_TSO));
        target.setName(source.getName());
        target.setConstructionStage(source.getConstructionStage() != null ? CurrentStageOfSiteConstruction.valueOf(source.getConstructionStage()) : null);
        target.setDateOfSupervisionRequired(source.getDateOfSupervisionRequired());
        target.setExpertiseRequired((source.getExpertiseRequired() != null) ? TAExpertise.valueOf(source.getExpertiseRequired()) : null);
        target.setDurationOfSupervisionRequired(source.getDurationOfSupervisionRequired());
        target.setRequestDate(new Date());
        target.setRequestNo((String) getCustomerComplaintRequestNoGenerator().generate());
        target.setTsoAssignedDate(new Date());

    }

    public Converter<SCLAddressData, AddressModel> getSclAddressReverseConverter() {
        return sclAddressReverseConverter;
    }

    public void setSclAddressReverseConverter(Converter<SCLAddressData, AddressModel> sclAddressReverseConverter) {
        this.sclAddressReverseConverter = sclAddressReverseConverter;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public SclCustomerService getSclCustomerService() {
        return sclCustomerService;
    }

    public void setSclCustomerService(SclCustomerService sclCustomerService) {
        this.sclCustomerService = sclCustomerService;
    }

    public PersistentKeyGenerator getCustomerComplaintRequestNoGenerator() {
        return customerComplaintRequestNoGenerator;
    }

    public void setCustomerComplaintRequestNoGenerator(PersistentKeyGenerator customerComplaintRequestNoGenerator) {
        this.customerComplaintRequestNoGenerator = customerComplaintRequestNoGenerator;
    }
}
