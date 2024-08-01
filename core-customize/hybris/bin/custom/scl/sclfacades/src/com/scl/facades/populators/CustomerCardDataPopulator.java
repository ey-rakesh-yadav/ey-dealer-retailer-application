package com.scl.facades.populators;

import com.scl.core.model.SclCustomerModel;
import com.scl.core.utility.SclDateUtility;
import com.scl.facades.data.CustomerCardData;
import com.scl.facades.data.SCLAddressData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.lang.BooleanUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

public class CustomerCardDataPopulator implements Populator<SclCustomerModel, CustomerCardData> {
    @Resource
    private UserService userService;

    @Resource
    Converter<AddressModel, SCLAddressData> sclAddressConverter;

    @Override
    public void populate(SclCustomerModel source, CustomerCardData target) throws ConversionException {
        target.setName(source.getName());
        target.setPhone(source.getMobileNumber());
        target.setApplicationNo(source.getApplicationNo());
        if(source.getApplicationDate()!=null) {
        	target.setApplicationDate(SclDateUtility.getFormattedDate(source.getApplicationDate(),"dd MMMM yyyy"));
        }
        target.setPotential(source.getCounterPotential());
        target.setUid(source.getUid());
        target.setRejectionReason(source.getRejectionReason());
        if(source.getCustomerOnboardingStatus()!=null) {
        	target.setStatus(source.getCustomerOnboardingStatus().getCode());
        }
        if(source.getInfluencerType()!=null) {
        	target.setType(source.getInfluencerType().getCode());
        }
        if(source.getApprovedBySoDate()!=null) {
        	target.setApprovedDate(SclDateUtility.getFormattedDate(source.getApprovedBySoDate(),"dd MMMM yyyy"));
        }
        if(source.getRejectedBySoDate()!=null) {
        	target.setRejectedDate(SclDateUtility.getFormattedDate(source.getRejectedBySoDate(),"dd MMMM yyyy"));
        }
        if(source.getOnboardingPlacedBy()!=null) {
        	target.setRequestedBy(source.getOnboardingPlacedBy().getName());
        }
        if(source.getPendingWithSoDate()!=null) {
        	target.setRequestedDate(SclDateUtility.getFormattedDate(source.getPendingWithSoDate(),"dd MMMM yyyy"));
        }
        List<SCLAddressData> addresses=new ArrayList<>();
         source.getAddresses().forEach(addressModel -> {
             if(BooleanUtils.isTrue(addressModel.getBillingAddress())){
                 var adrdata=sclAddressConverter.convert(addressModel);
                 adrdata.setEmail(source.getEmail());
                 addresses.add(adrdata);
             }
         });
         if(!addresses.isEmpty()){
             target.setAddress(addresses.get(0));
         }

    }


}
