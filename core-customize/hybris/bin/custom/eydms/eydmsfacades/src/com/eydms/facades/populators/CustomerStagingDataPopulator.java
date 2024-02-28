package com.eydms.facades.populators;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.facades.data.CustomerStagingData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.user.UserService;

import javax.annotation.Resource;


public class CustomerStagingDataPopulator implements Populator<EyDmsCustomerModel, CustomerStagingData> {

    @Resource
    private UserService userService;

    @Resource
    private EnumerationService enumerationService;

    @Override
    public void populate(EyDmsCustomerModel source, CustomerStagingData target) throws ConversionException {
        target.setName(source.getName());
        target.setApplicationNo(source.getApplicationNo());
        target.setPotential(source.getCounterPotential());
        target.setApplicationDate(source.getApplicationDate());
        target.setUid(source.getUid());
        target.setApprovedBySoDate(source.getApprovedBySoDate());
        target.setPendingWithSoDate(source.getPendingWithSoDate());
        target.setRejectedBySoDate(source.getRejectedBySoDate());
        target.setCustomerQueryDate(source.getApplicationDate());
        target.setApprovedByStateHeadDate(source.getApprovedByStateHeadDate());
        target.setRejectedByStateHeadDate(source.getRejectedByStateHeadDate());
        target.setApprovedBySalesAccountDate(source.getApprovedBySalesAccountDate());
        target.setRejectedBySalesAccountDate(source.getRejectedBySalesAccountDate());
        target.setApprovedByNationalSalesHeadDate(source.getApprovedByNationalSalesHeadDate());
        target.setRejectedByNationSalesHeadDate(source.getRejectedByNationSalesHeadDate());
        target.setCreditReceivedDate(source.getCreditReceivedDate());


        if(source.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))){
            if(null!=source.getDealerStageStatus()){
                target.setStageStatus(enumerationService.getEnumerationName(source.getDealerStageStatus()));
            }
            if(null!=source.getDealerStageSubStatus()){
                target.setStageSubStatus(enumerationService.getEnumerationName(source.getDealerStageSubStatus()));
            }
        }
        if(source.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))){
            if(null!=source.getRetailerStageStatus()){
                target.setStageStatus(enumerationService.getEnumerationName(source.getRetailerStageStatus()));
            }
            if(null!=source.getRetailerStageSubStatus()){
                target.setStageSubStatus(enumerationService.getEnumerationName(source.getRetailerStageSubStatus()));
            }
        }

    }


}
