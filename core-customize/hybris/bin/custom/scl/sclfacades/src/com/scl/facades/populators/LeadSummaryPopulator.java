package com.scl.facades.populators;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.enums.LeadType;
import com.scl.core.model.LeadMasterModel;
import com.scl.facades.data.LeadSummaryData;
import com.scl.facades.data.ReferenceItemData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.HybrisEnumValue;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;


import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class LeadSummaryPopulator implements Populator<LeadMasterModel, LeadSummaryData> {
    @Resource
    private EnumerationService enumerationService;

    @Autowired
    UserService userService;
    @Resource
    private Converter<AddressModel, AddressData> addressConverter;
    @Override
    public void populate(LeadMasterModel source, LeadSummaryData target) throws ConversionException {
        target.setLeadId(source.getLeadId());
        target.setName(source.getName());
        target.setContactNo(source.getContactNo());
        target.setEmail(source.getEmail());
        if(Objects.nonNull(source.getLeadType())) {
            target.setCategory(source.getLeadType().getCode());
           /*if (List.of(LeadType.ARCHITECT, LeadType.ENGINEER, LeadType.CONTRACTOR, LeadType.MASON).contains(source.getLeadType())) {
                target.setEnableFormCompletion(false);
            }*/
        }
        if(Objects.nonNull(source.getEnableFormCompletion())){
            if (source.getEnableFormCompletion().equals(Boolean.FALSE)) {
                target.setEnableFormCompletion(Boolean.FALSE);
            }
            else
                target.setEnableFormCompletion(Boolean.TRUE);
        }
        else
            target.setEnableFormCompletion(Boolean.TRUE);

        target.setGenerationDate(getFormattedDate(source.getCreationtime()));
        if(Objects.nonNull(source.getCreatedBy())) {
            if (source.getCreatedBy().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                target.setGeneratedBy("Dealer");
            }
            else if (source.getCreatedBy().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                target.setGeneratedBy("Retailer");
            }
            else if (source.getCreatedBy().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID))) {
                target.setGeneratedBy("Influencer");
                target.setCustomerCode(source.getCreatedBy().getUid());
                target.setCustomerName(source.getCreatedBy().getName());
            }
            else {
                target.setGeneratedBy("self");
            }
        }
        else {
            target.setGeneratedBy("self");
        }


        if(Objects.nonNull(source.getLeadStage())) {
            target.setStage(enumerationService.getEnumerationName(source.getLeadStage()));
        }
        if(Objects.nonNull(source.getSiteStage())) {
            target.setSiteStage(createReferenceData(source.getSiteStage()));
        }
        if(Objects.nonNull(source.getSiteType())) {
            target.setSiteType(createReferenceData(source.getSiteType()));
        }
        var addresss=source.getAddress();
        if(Objects.nonNull(addresss)) {
            target.setAddress(addressConverter.convert(addresss));
            target.setDistrictTaluka(getFormatedString(addresss.getDistrict(), addresss.getTaluka()));
            target.setStateCity(getFormatedString(addresss.getErpCity(), addresss.getState()));
        }
        else
        {
            AddressData addressData = new AddressData();
            addressData.setLine1(source.getLine1());
            addressData.setLine2(source.getLine2());
            addressData.setState(source.getState());
            addressData.setDistrict(source.getDistrict());
            addressData.setTaluka(source.getTaluka());
            addressData.setCity(source.getCity());
            addressData.setErpCity(source.getCity());
            addressData.setPostalCode(source.getPostalCode());
            target.setDistrictTaluka(getFormatedString(source.getDistrict(), source.getTaluka()));
            target.setStateCity(getFormatedString(source.getCity(), source.getState()));
            target.setAddress(addressData);
        }
    }

    private ReferenceItemData createReferenceData(HybrisEnumValue enumValue) {
        ReferenceItemData data=new ReferenceItemData();
        data.setCode(enumValue.getCode());
        data.setName(enumerationService.getEnumerationName(enumValue)!=null?enumerationService.getEnumerationName(enumValue):enumValue.getCode());
        return data;
    }

    private String getFormatedString(String str1, String str2) {
        if(Objects.nonNull(str1) && Objects.nonNull(str2)){
            return str1+", "+str2;
        }
        if(Objects.nonNull(str1)){
             return str1;
        }
        if(Objects.nonNull(str2)){
            return str2;
        }
        return null;
    }

    private String getFormattedDate(Date date) {
        if (Objects.nonNull(date)) {
            DateFormat format = new SimpleDateFormat("dd/MM/yy");
            return format.format(date);
        }
        return null;
    }
}
