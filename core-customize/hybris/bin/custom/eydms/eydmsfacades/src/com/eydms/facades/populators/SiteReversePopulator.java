package com.eydms.facades.populators;

import com.eydms.core.dao.SiteManagementDao;
import com.eydms.core.enums.*;
import com.eydms.core.jalo.SiteServiceType;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.model.EyDmsSiteMasterModel;
import com.eydms.core.model.SiteServiceTestModel;
import com.eydms.core.model.SiteServiceTypeModel;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class SiteReversePopulator implements Populator<MapNewSiteData, EyDmsSiteMasterModel> {

    private static final Logger LOG = Logger.getLogger(SiteReversePopulator.class);

    @Autowired
    ModelService modelService;

    @Autowired
    UserService userService;

    @Autowired
    SiteManagementDao siteManagementDao;

    @Override
    public void populate(MapNewSiteData source, EyDmsSiteMasterModel target) throws ConversionException {
        target.setName(source.getName());
        target.setMobileNumber(source.getMobileNumber());
        target.setTypeOfVisit(TypeOfVisit.TRADE_VISIT);
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
        target.setLatitude(source.getLatitude());
        target.setLongitude(source.getLatitude());

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
        boolean isPrevious = false;
        boolean isPremiumProduct = false;
        if (source.getSiteStatus() != null && source.getSiteStatus().equalsIgnoreCase(SiteStatus.SITE_CONVERTED.getCode())) {
            if(source.getConvertedToCementType()!=null) {
                target.setCementType(siteManagementDao.findCementTypeByCode(source.getConvertedToCementType()));
                if(target.getCementType()!=null && target.getCementType().getName()!=null
                        && target.getCementType().getName().toUpperCase().contains("PREMIUM")) {
                    isPremiumProduct = true;
                }
            }
            target.setIsPremium(isPremiumProduct);
            if(source.getConvertedToBrand()!=null) {
                target.setCementBrand(siteManagementDao.findCementBrandByCode(source.getConvertedToBrand()));
            }
            if(source.getSiteCategoryType()!=null) {
                target.setSiteCategoryType(siteManagementDao.findCategoryTypeByCode("SS"));
            }
            target.setSiteConvertedDate(new Date());
            isPrevious = true;
        }
        else if (source.getSiteStatus() != null && source.getSiteStatus().equalsIgnoreCase(SiteStatus.SITE_UPGRADED.getCode())) {
            if(source.getUpgradeToCementType()!=null) {
                target.setCementType(siteManagementDao.findCementTypeByCode(source.getUpgradeToCementType()));
            }
            if(source.getUpgradeToBrand()!=null) {
                target.setCementBrand(siteManagementDao.findCementBrandByCode(source.getUpgradeToBrand()));
            }
            target.setSiteUpgradedDate(new Date());
            isPrevious = true;
        }
        else {
            if(source.getCementType()!=null) {
                target.setCementType(siteManagementDao.findCementTypeByCode(source.getCementType()));
            }
            if(source.getCementBrand()!=null) {
                target.setCementBrand(siteManagementDao.findCementBrandByCode(source.getCementBrand()));
            }
        }
        if(isPrevious) {
        	if(source.getCementType()!=null) {
        		target.setPreviousCementType(siteManagementDao.findCementTypeByCode(source.getCementType()));
        	}
        	if(source.getCementBrand()!=null) {
        		target.setPreviousCementBrand(siteManagementDao.findCementBrandByCode(source.getCementBrand()));
        	}
        	if(source.getSiteCategoryType()!=null) {
        		target.setPreviousCategoryType(siteManagementDao.findCategoryTypeByCode(source.getSiteCategoryType()));
        	}
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
