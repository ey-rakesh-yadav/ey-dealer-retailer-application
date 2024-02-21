package com.eydms.facades.populators;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.model.LeadMasterModel;
import com.eydms.core.model.EyDmsSiteMasterModel;
import com.eydms.facades.data.LeadMasterData;
import com.eydms.facades.data.MapNewSiteData;
import com.eydms.facades.data.EyDmsLeadData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class LeadMasterPopulator implements Populator<LeadMasterModel, EyDmsLeadData> {
    @Resource
    private Converter<AddressModel, AddressData> addressConverter;

    @Resource
    EnumerationService enumerationService;

    @Resource
    UserService userService;

    private static final Logger LOG = Logger.getLogger(LeadMasterPopulator.class);
    @Override
    public void populate(LeadMasterModel source, EyDmsLeadData target) throws ConversionException {
        if(source!=null) {
            target.setLeadId(source.getLeadId());
            target.setName(source.getName());
            target.setMobileNo(source.getContactNo());
            if(Objects.nonNull(source.getAddress())) {
                AddressModel address = source.getAddress();
                target.setAddress(addressConverter.convert(address));
                target.setDistrict(address.getDistrict());
                target.setCity(address.getErpCity());
                target.setState(address.getState());
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
                target.setDistrict(source.getDistrict());
                target.setCity(source.getCity());
                target.setState(source.getState());
                target.setAddress(addressData);
            }
            if(Objects.nonNull(source.getLeadType()))
                //enumerationService.getEnumerationName(source.getLeadType());
                target.setType(source.getLeadType().getCode());
            if(Objects.nonNull(source.getStatus()))
                target.setStatus(source.getStatus().getCode());

            if(Objects.nonNull(source.getCreatedBy())) {
                if (source.getCreatedBy().getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                    target.setGeneratedBy("Dealer");
                }
                else if (source.getCreatedBy().getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                    target.setGeneratedBy("Retailer");
                }
                else if (source.getCreatedBy().getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID))) {
                    target.setGeneratedBy("Influencer");
                    target.setCustomerCode(source.getCreatedBy().getUid());
                    target.setCustomerName(source.getCreatedBy().getName());
                }
                else if (source.getCreatedBy().getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.TSM_GROUP_ID))) {
                    target.setGeneratedBy("TSM");
                }
                else if (source.getCreatedBy().getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RH_GROUP_ID))) {
                    target.setGeneratedBy("RH");
                }
                else if (source.getCreatedBy().getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.SALES_OFFICER_GROUP_ID))) {
                    target.setGeneratedBy("SO");
                }
                else {
                    target.setGeneratedBy("self");
                }
            }
            else {
                target.setGeneratedBy("self");
            }

            target.setLeadGenerationDate(getFormattedDate(source.getCreationtime()));
            if(source.getOnboardedCustomer()!=null)
            {
                target.setOnboardedCustomerUid(source.getOnboardedCustomer().getUid());
            }

            if(source.getApprovedDate()!=null)
            {
                target.setApprovedDate(getFormattedDate(source.getApprovedDate()));
            }
            if(source.getRejectedDate()!=null)
            {
                target.setRejectedDate(getFormattedDate(source.getRejectedDate()));
            }
        }
    }

    private String getFormattedDate(Date date) {
        if (Objects.nonNull(date)) {
            DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            return format.format(date);
        }
        return null;
    }

    public Converter<AddressModel, AddressData> getAddressConverter() {
        return addressConverter;
    }

    public void setAddressConverter(Converter<AddressModel, AddressData> addressConverter) {
        this.addressConverter = addressConverter;
    }

    public EnumerationService getEnumerationService() {
        return enumerationService;
    }

    public void setEnumerationService(EnumerationService enumerationService) {
        this.enumerationService = enumerationService;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
