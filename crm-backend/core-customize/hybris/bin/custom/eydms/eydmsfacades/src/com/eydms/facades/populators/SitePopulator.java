package com.eydms.facades.populators;


import com.eydms.core.enums.SiteStatus;
import com.eydms.core.model.EyDmsSiteMasterModel;
import com.eydms.core.model.SiteServiceTestModel;
import com.eydms.core.model.SiteVisitMasterModel;
import com.eydms.facades.data.MapNewSiteData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

public class SitePopulator implements Populator<EyDmsSiteMasterModel, MapNewSiteData> {

    private static final Logger LOG = Logger.getLogger(SitePopulator.class);

    @Override
    public void populate(EyDmsSiteMasterModel source, MapNewSiteData target) throws ConversionException {
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
        if(source.getCementBrand()!=null){
            target.setCementBrand(source.getCementBrand().getCode());
        }
        if(source.getSiteCategoryType()!=null){
            target.setSiteCategoryType(source.getSiteCategoryType().getCode());
        }

        target.setSiteStatus(source.getSiteStatus().getCode());
        target.setRemarks(source.getRemarks());
        target.setReasons(source.getReasons());
        target.setNumberOfBagsPurchased(source.getNumberOfBagsPurchased());
        target.setDateOfPurchase(String.valueOf(source.getDateOfPurchase()));
        if(source.getDealer()!=null){
            target.setDealer(source.getDealer().getUid());
        }
        if(source.getSp()!=null){
            target.setSp(source.getSp().getUid());
        }
        target.setRetailer(source.getRetailer());
        target.setTechnicalAssistanceRequired(source.getTechnicalAssistanceRequired());
        
        if(source.getPreviousCategoryType()!=null)
            target.setVisitSiteCategory(source.getPreviousCategoryType().getCode());
        if(source.getPreviousCementType()!=null)
            target.setVisitCementType(source.getPreviousCementType().getCode());
        if(source.getPreviousCementBrand()!=null)
            target.setVisitCementBrand(source.getPreviousCementBrand().getCode());
        
        if(source.getSiteStatus()!=null && source.getSiteStatus().equals(SiteStatus.SITE_CONVERTED)) {
        	 target.setConvertedToCementType(target.getCementType());
             target.setConvertedToBrand(target.getCementBrand());
        }else if(source.getSiteStatus()!=null && source.getSiteStatus().equals(SiteStatus.SITE_UPGRADED)) {
                target.setUpgradeToCementType(target.getCementType());
                target.setUpgradeToBrand(target.getCementBrand());
        }
               
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
    }

}
