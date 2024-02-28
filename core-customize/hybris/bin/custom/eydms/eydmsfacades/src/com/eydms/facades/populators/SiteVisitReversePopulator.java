package com.eydms.facades.populators;

import com.eydms.core.dao.SiteManagementDao;
import com.eydms.core.enums.*;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.model.EyDmsSiteMasterModel;
import com.eydms.core.model.SiteServiceTestModel;
import com.eydms.core.model.SiteVisitMasterModel;
import com.eydms.core.services.impl.SiteManagementServiceImpl;
import com.eydms.facades.data.MapNewSiteData;
import com.eydms.facades.djp.data.CounterMappingData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class SiteVisitReversePopulator implements Populator<MapNewSiteData, SiteVisitMasterModel> {

    private static final Logger LOG = Logger.getLogger(SiteReversePopulator.class);

    @Autowired
    ModelService modelService;

    @Autowired
    private Populator<AddressData, AddressModel> addressReversePopulator;

    @Autowired
    CustomerAccountService customerAccountService;

    @Autowired
    UserService userService;

    @Autowired
    SiteManagementDao siteManagementDao;

    @Override
    public void populate(MapNewSiteData source, SiteVisitMasterModel target) throws ConversionException {
        //target.setName(source.getName());
        //target.setMobileNumber(source.getMobileNumber());
        target.setContractorName(source.getContractorName());
        target.setContractorPhoneNumber(source.getContractorPhoneNumber());
        target.setMasonName(source.getMasonName());
        target.setMasonPhoneNumber(source.getMasonPhoneNumber());
        target.setArchitectName(source.getArchitectName());
        target.setArchitectNumber(source.getArchitectPhoneNumber());
        target.setPersonMetAtSite(PersonMetAtSite.valueOf(source.getPersonMetAtSite()));
        target.setConstructionStage(ConstructionStage.valueOf(source.getCurrentStageOfConstruction()));
        target.setBuiltUpArea(source.getBuiltUpArea());
        if(StringUtils.isNotBlank(source.getNextSlabCasting())) {
            target.setNextSlabCasting(getParsedDate(source.getNextSlabCasting()));
        }
        target.setBalanceCementRequirement(source.getBalanceCementRequirement());
        target.setServiceProvidedAtSite(source.getServiceProvidedAtSite());
        if(source.getServiceType()!=null) {
            target.setServiceType(siteManagementDao.findServiceTypeByCode(source.getServiceType()));
        }

        List<String> serviceTypeTestDataList = source.getServiceTypeTest();
        if (serviceTypeTestDataList != null && !serviceTypeTestDataList.isEmpty()) {
            List<SiteServiceTestModel> list = serviceTypeTestDataList.stream().map(serviceTypeTest -> siteManagementDao.findServiceTypeTestByCode(serviceTypeTest)).collect(Collectors.toList());
            target.setServiceTypeTest(list);
        }

            if(source.getSiteCategoryType()!=null) {
            target.setSiteCategoryType(siteManagementDao.findCategoryTypeByCode(source.getSiteCategoryType()));
            }
            boolean isPremiumProduct = false;
           if(source.getConvertedToCementType()!=null) {
                target.setConvertedToCementType(siteManagementDao.findCementTypeByCode(source.getConvertedToCementType()));
                if(target.getConvertedToCementType()!=null && target.getConvertedToCementType().getName()!=null
                		&& target.getConvertedToCementType().getName().toUpperCase().contains("PREMIUM")) {
                	isPremiumProduct = true;
                }
            }


            if(source.getConvertedToBrand()!=null) {
                target.setConvertedToBrand(siteManagementDao.findCementBrandByCode(source.getConvertedToBrand()));
            }
            if(source.getUpgradeToCementType()!=null) {
                target.setUpgradeToCementType(siteManagementDao.findCementTypeByCode(source.getUpgradeToCementType()));
                if(target.getUpgradeToCementType()!=null && target.getUpgradeToCementType().getName()!=null
                		&& target.getUpgradeToCementType().getName().toUpperCase().contains("PREMIUM")) {
                	isPremiumProduct = true;
                }
            }
            if(source.getUpgradeToBrand()!=null) {
                target.setUpgradeToBrand(siteManagementDao.findCementBrandByCode(source.getUpgradeToBrand()));
            }
            if(source.getCementType()!=null) {
                target.setCementType(siteManagementDao.findCementTypeByCode(source.getCementType()));
               /* if(target.getCementType()!=null && target.getCementType().getName()!=null
                        && target.getCementType().getName().toUpperCase().contains("PREMIUM")) {
                    isPremiumProduct = true;
                }*/
            }
            if(source.getCementBrand()!=null) {
                target.setCementBrand(siteManagementDao.findCementBrandByCode(source.getCementBrand()));
            }
            if (source.getSiteStatus() != null && source.getSiteStatus().equalsIgnoreCase(SiteStatus.SITE_CONVERTED.getCode()))
            {
                target.setSiteConvertedDate(new Date());
            }
            target.setIsPremium(isPremiumProduct);

            if (source.getSiteStatus() != null && source.getSiteStatus().equalsIgnoreCase(SiteStatus.SITE_UPGRADED.getCode()))
            {
             target.setSiteUpgradedDate(new Date());
            }

        target.setSiteStatus(SiteStatus.valueOf(source.getSiteStatus()));
        target.setRemarks(source.getRemarks());
        target.setReasons(source.getReasons());
        target.setNumberOfBagsPurchased(source.getNumberOfBagsPurchased());
        if(StringUtils.isNotBlank(source.getDateOfPurchase())) {
            target.setDateOfPurchase(getParsedDate(source.getDateOfPurchase()));
        }
        if(StringUtils.isNotBlank(source.getDealer())) {
            EyDmsCustomerModel dealer = (EyDmsCustomerModel) userService.getUserForUID(source.getDealer());
            target.setDealer(dealer);
        }
        if(StringUtils.isNotBlank(source.getSp())) {
            EyDmsCustomerModel sp = (EyDmsCustomerModel) userService.getUserForUID(source.getSp());
            target.setSp(sp);
        }
        target.setRetailer(source.getRetailer());
        target.setTechnicalAssistanceRequired(source.getTechnicalAssistanceRequired());
    }

    private Date getParsedDate(String date) {
        Date startDate = null;
        if(date!=null) {
            try {
                startDate = new SimpleDateFormat("dd/MM/yyyy").parse(date);

            } catch (ParseException e) {
                LOG.error("Error Parsing Date", e);
                throw new IllegalArgumentException(String.format("Please provide valid date %s", date));
            }
        }
        return startDate;
    }
}
