package com.scl.facades.populators;


import com.scl.core.enums.SiteStatus;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.SclSiteMasterModel;
import com.scl.core.model.SiteServiceTestModel;
import com.scl.core.model.SiteVisitMasterModel;
import com.scl.facades.data.MapNewSiteData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

public class SitePopulator implements Populator<SclSiteMasterModel, MapNewSiteData> {

    private static final Logger LOG = Logger.getLogger(SitePopulator.class);
    @Autowired
    UserService userService;

    @Override
    public void populate(SclSiteMasterModel source, MapNewSiteData target) throws ConversionException {

        SimpleDateFormat formatter= new SimpleDateFormat("dd/MM/yyyy");

        target.setName(source.getName());
        target.setMobileNumber(source.getMobileNumber());
        target.setContractorName(source.getContractorName());
        target.setContractorPhoneNumber(source.getContractorPhoneNumber());
        target.setMasonName(source.getMasonName());
        target.setMasonPhoneNumber(source.getMasonPhoneNumber());
        target.setArchitectName(source.getArchitectName());
        target.setArchitectPhoneNumber(source.getArchitectNumber());
        target.setPersonMetAtSite(String.valueOf(source.getPersonMetAtSite()));
        target.setCurrentStageOfConstruction(String.valueOf(source.getConstructionStage()));
        target.setBuiltUpArea(source.getBuiltUpArea());
        target.setNextSlabCasting(String.valueOf(source.getNextSlabCasting()));
        target.setBalanceCementRequirement(source.getBalanceCementRequirement());
        target.setServiceProvidedAtSite(source.getServiceProvidedAtSite());
        target.setLatitude(source.getLatitude());
        target.setLongitude(source.getLatitude());
        target.setPricePerRange(source.getPricePerRange());

        if(source.getServiceType()!=null) {
            target.setServiceType(source.getServiceType().getCode());
        }

        List<SiteServiceTestModel> siteServiceTestList = source.getServiceTypeTest();
        if(siteServiceTestList!=null && !siteServiceTestList.isEmpty()){
            List<String> list = siteServiceTestList.stream().map(siteServiceTestModel -> siteServiceTestModel.getCode()).collect(Collectors.toList());
            target.setServiceTypeTest(list);
        }

        if(source.getSiteCategoryType()!=null) {
            target.setSiteCategoryType(source.getSiteCategoryType().getCode());
        }
        if(source.getCementType()!=null){
            target.setCementType(source.getCementType().getCode());
        }
        if(source.getCementProduct()!=null){
            target.setCementBrand(source.getCementProduct().getCode());
            if(source.getCementProduct().getPremiumProductType()!=null) {
                target.setCementType(source.getCementProduct().getPremiumProductType().getCode());
            }
        }
        if(source.getReasonForSiteLoss()!=null) {
            target.setReasonsForSiteLoss(source.getReasonForSiteLoss().getCode());
        }
        if(source.getSiteStatus()!=null) {
            target.setSiteStatus(source.getSiteStatus().getCode());
        }

        target.setRemarks(source.getRemarks());
        target.setReasons(source.getReasons());
        target.setNumberOfBagsPurchased(source.getNumberOfBagsPurchased());
        target.setDateOfPurchase(String.valueOf(source.getDateOfPurchase()));
        if(source.getNextDateOfVisit()!=null){
        target.setNextDateOfVisit(formatter.format(source.getNextDateOfVisit()));}
        if(source.getDealer()!=null){
            target.setDealer(source.getDealer().getUid());
        }
        if(source.getSp()!=null){
            target.setSp(source.getSp().getUid());
        }
        if(StringUtils.isNotBlank(source.getRetailer())) {
            if(userService.getUserForUID(source.getRetailer()) != null) {
                SclCustomerModel retailer = (SclCustomerModel) userService.getUserForUID(source.getRetailer());
                if(retailer!=null)
                    target.setRetailer(retailer.getName());
            }
        }
        target.setTechnicalAssistanceRequired(source.getTechnicalAssistanceRequired());

        if(source.getPreviousCategoryType()!=null)
            target.setVisitSiteCategory(source.getPreviousCategoryType().getCode());

        if(source.getPreviousCementProduct()!=null) {
            target.setVisitCementBrand(source.getPreviousCementProduct().getCode());
            if (source.getPreviousCementProduct().getPremiumProductType() != null) {
                target.setVisitCementType(source.getPreviousCementProduct().getPremiumProductType().getCode());
            }

        }
        if(source.getSiteStatus()!=null && source.getSiteStatus().equals(SiteStatus.SITE_CONVERTED)) {
        	 target.setConvertedToCementType(target.getCementType());
             target.setConvertedToBrand(target.getCementBrand());
        }

//        else if(source.getSiteStatus()!=null && source.getSiteStatus().equals(SiteStatus.SITE_UPGRADED)) {
//                target.setUpgradeToCementType(target.getCementType());
//                target.setUpgradeToBrand(target.getCementBrand());
//        }
//        if(source.getLastCounterVisit()!=null){
//            SiteVisitMasterModel siteVisit = (SiteVisitMasterModel) source.getLastCounterVisit();
//            if(source.getSiteCategoryType()!=null)
//                target.setVisitSiteCategory(siteVisit.getSiteCategoryType().getCode());
//            if(siteVisit.getCementType()!=null)
//                target.setVisitCementType(siteVisit.getCementType().getCode());
//            if(siteVisit.getCementBrand()!=null)
//                target.setVisitCementBrand(siteVisit.getCementBrand().getCode());
//            if(siteVisit.getUpgradeToCementType()!=null)
//                target.setUpgradeToCementType(siteVisit.getUpgradeToCementType().getCode());
//            if(siteVisit.getUpgradeToBrand()!=null)
//                target.setUpgradeToBrand(siteVisit.getUpgradeToBrand().getCode());
//            if(siteVisit.getConvertedToCementType()!=null)
//                target.setConvertedToCementType(siteVisit.getConvertedToCementType().getCode());
//            if(siteVisit.getConvertedToBrand()!=null)
//                target.setConvertedToBrand(siteVisit.getConvertedToBrand().getCode());
//        }

        if(source.getTypeOfVisit()!=null)
            target.setTypeOfVisit(source.getTypeOfVisit().getCode());

        target.setOrderCount(source.getOrderCount());
    }


}
