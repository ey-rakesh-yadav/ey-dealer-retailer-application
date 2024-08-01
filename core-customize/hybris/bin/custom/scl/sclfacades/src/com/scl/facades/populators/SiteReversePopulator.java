package com.scl.facades.populators;

import com.scl.core.dao.DataConstraintDao;
import com.scl.core.dao.SiteManagementDao;
import com.scl.core.enums.*;
import com.scl.core.jalo.SiteServiceType;
import com.scl.core.model.*;
import com.scl.core.services.impl.SiteManagementServiceImpl;
import com.scl.facades.data.MapNewSiteData;
import com.scl.facades.djp.data.CounterMappingData;
import com.scl.facades.constants.SclFacadesConstants;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class SiteReversePopulator implements Populator<MapNewSiteData, SclSiteMasterModel> {
    private static final Logger LOG = Logger.getLogger(SiteReversePopulator.class);

    @Autowired
    ModelService modelService;

    @Autowired
    UserService userService;

    @Autowired
    SiteManagementDao siteManagementDao;

    @Autowired
    EnumerationService enumerationService;

    @Autowired
    DataConstraintDao dataConstraintDao;

    public static final String SITEACTIVE_NO = "NO";

    public static final String SITEACTIVE_YES = "YES";

    @Override
    public void populate(MapNewSiteData source, SclSiteMasterModel target) throws ConversionException {

        String active = "NO";

        if (source.getName() != null)
            target.setName(source.getName());
        if (source.getMobileNumber() != null)
            target.setMobileNumber(source.getMobileNumber());
        target.setTypeOfVisit(TypeOfVisit.TRADE_VISIT);
        if (source.getMasonName() != null)
            target.setMasonName(source.getMasonName());
        if (source.getMasonPhoneNumber() != null)
            target.setMasonPhoneNumber(source.getMasonPhoneNumber());
        if (source.getArchitectName() != null)
            target.setArchitectName(source.getArchitectName());
        if (source.getArchitectPhoneNumber() != null)
            target.setArchitectNumber(source.getArchitectPhoneNumber());
        if (source.getBuiltUpArea() != null)
            target.setBuiltUpArea(source.getBuiltUpArea());
        if (source.getBalanceCementRequirement() != null)
            target.setBalanceCementRequirement(source.getBalanceCementRequirement());
        if (source.getServiceProvidedAtSite() != null)
            target.setServiceProvidedAtSite(source.getServiceProvidedAtSite());
        if (source.getLatitude() != null)
            target.setLatitude(source.getLatitude());
        if (source.getLatitude() != null)
            target.setLongitude(source.getLongitude());
        if (source.getRemarks() != null)
            target.setRemarks(source.getRemarks());
        if (source.getReasons() != null)
            target.setReasons(source.getReasons());

        if (source.getContractorName() != null)
            target.setContractorName(source.getContractorName());
        if (source.getContractorPhoneNumber() != null) {
            target.setContractorPhoneNumber(source.getContractorPhoneNumber());
        }
        if (source.getPersonMetAtSite() != null) {
            target.setPersonMetAtSite(PersonMetAtSite.valueOf(source.getPersonMetAtSite()));
        }
        if (source.getCurrentStageOfConstruction() != null) {
            target.setConstructionStage(ConstructionStage.valueOf(source.getCurrentStageOfConstruction()));
        }

        if (source.getCurrentStageOfConstruction() != null) {
            target.setCurrentStageOfConstruction(CurrentStageOfSiteConstruction.valueOf(source.getCurrentStageOfConstruction()));
        }

        if (source.getNextSlabCasting() != null && StringUtils.isNotBlank(source.getNextSlabCasting())) {
            target.setNextSlabCasting(getParsedDate(source.getNextSlabCasting()));
        }
        if (source.getServiceType() != null) {
            target.setServiceType(siteManagementDao.findServiceTypeByCode(source.getServiceType()));
        }

        if (source.getServiceTypeTest() != null) {
            List<String> serviceTypeTestDataList = source.getServiceTypeTest();
            if (serviceTypeTestDataList != null && !serviceTypeTestDataList.isEmpty()) {
                List<SiteServiceTestModel> list = serviceTypeTestDataList.stream().map(serviceTypeTest -> siteManagementDao.findServiceTypeTestByCode(serviceTypeTest)).collect(Collectors.toList());
                target.setServiceTypeTest(list);
            }
        }
        if (source.getSiteCategoryType() != null) {
            target.setSiteCategoryType(enumerationService.getEnumerationValue(CompetitorProductType.class, source.getSiteCategoryType()));
//            target.setSiteCategoryType(siteManagementDao.findCategoryTypeByCode(source.getSiteCategoryType()));
        }
        boolean isPrevious = false;
        boolean isPremiumProduct = false;
        PremiumProductType premiumProductType = null;
        CompetitorProductModel cementProduct = null;
        int orderCount = target.getOrderCount() != null ? target.getOrderCount() : 0;
        CompetitorProductModel previousCementProduct = null;

        /**Commenting this code block to refactor. Will remove this code block in future.**/
/*        if (source.getSiteStatus() != null && source.getSiteStatus().equalsIgnoreCase(SiteStatus.SITE_CONVERTED.getCode())) {
            LOG.info(String.format("site status is::%s",source.getSiteStatus()));
            if (source.getConvertedToCementType() != null) {
                premiumProductType = enumerationService.getEnumerationValue(PremiumProductType.class, source.getConvertedToCementType());

                if (premiumProductType != null && premiumProductType.getCode() != null
                        && premiumProductType.getCode().toUpperCase().contains("PREMIUM") && !premiumProductType.getCode().toUpperCase().contains("NONPREMIUM")) {
                    isPremiumProduct = true;
                }
            }

            if (source.getConvertedToBrand() != null) {
                LOG.info(String.format("converted to brand is::%s",source.getConvertedToBrand()));
                cementProduct = siteManagementDao.findCementProductByCode(source.getConvertedToBrand());
                LOG.info(String.format("fetched cement product for convertedToBrand is::%s",cementProduct));
                target.setCementProduct(cementProduct);
            }

            target.setSiteConvertedDate(new Date());
            isPrevious = true;
        } else if (source.getSiteStatus()!=null && source.getSiteStatus().equalsIgnoreCase(SiteStatus.SITE_LOST.getCode())) {
            LOG.info(String.format("site status is::%s",source.getSiteStatus()));
            if (source.getLostToCementType() != null) {
                premiumProductType = enumerationService.getEnumerationValue(PremiumProductType.class, source.getLostToCementType());

                if (premiumProductType != null && premiumProductType.getCode() != null
                        && premiumProductType.getCode().toUpperCase().contains("PREMIUM") && !premiumProductType.getCode().toUpperCase().contains("NONPREMIUM")) {
                    isPremiumProduct = true;
                }
            }

            if (source.getLostToCementBrand() != null) {
                LOG.info(String.format("Lost to brand is::%s",source.getLostToCementBrand()));
                cementProduct = siteManagementDao.findCementProductByCode(source.getLostToCementBrand());
                LOG.info(String.format("fetched cement product for lostToBrand is::%s",cementProduct));
                target.setCementProduct(cementProduct);
                target.setLostToCementProduct(cementProduct);
            }
            if(!source.getReasonsForSiteLoss().isBlank()) {
                target.setReasonForSiteLoss(enumerationService.getEnumerationValue(ReasonForSiteLoss.class, source.getReasonsForSiteLoss()));
            }

            isPrevious = true;
        } else {

            if (source.getCementBrand() != null) {
                LOG.info(String.format("cement brand is::%s",source.getCementBrand()));
                cementProduct = siteManagementDao.findCementProductByCode(source.getCementBrand());
                LOG.info(String.format("fetched cementProduct is::%s",cementProduct));
                target.setCementProduct(cementProduct);
            }
        }
        target.setIsPremium(isPremiumProduct);

        if (isPrevious) {
            if (source.getCementBrand() != null) {
                previousCementProduct = siteManagementDao.findCementProductByCode(source.getCementBrand());
                target.setPreviousCementProduct(previousCementProduct);
            }
            if (source.getSiteCategoryType() != null) {
                target.setPreviousCategoryType(enumerationService.getEnumerationValue(CompetitorProductType.class, source.getSiteCategoryType()));
            }

        }*/

        String siteStatus = source.getSiteStatus();
        if (siteStatus != null) {
            LOG.info(String.format("site status is::%s", siteStatus));
            switch (siteStatus.toUpperCase()) {
                case SclFacadesConstants.SITE_STATUS.SITE_CONVERTED:
                    handleSiteConverted(source, target);
                    isPrevious = true;
                    break;
                case SclFacadesConstants.SITE_STATUS.SITE_LOST:
                    handleSiteLost(source, target);
                    isPrevious = true;
                    break;
                default:
                    handleCementBrand(source, target);
                    break;
            }

            if(source.getSiteCategoryType().equalsIgnoreCase(SclFacadesConstants.SITE_CATEGORY.FRESH_SITE)){
                isPrevious = false;
            }
        }

        target.setIsPremium(isPremiumProduct);

        if (isPrevious) {
            handlePreviousState(source, target);
        }

        if (source.getNumberOfBagsPurchased() != null) {
            Date lastVisitTime = new Date();
            String qtyValue = "";
            int siteConversionCap= dataConstraintDao.findDaysByConstraintName("SITE_CONVERSION_MAX_CAP");
            if (source.getNumberOfBagsPurchased()!=0.0 && orderCount <= siteConversionCap) {
                orderCount += 1;
                target.setOrderCount(orderCount);
            }
            if (target.getSiteBagQtyMap() != null && !target.getSiteBagQtyMap().isEmpty()) {
                Map<Date, String> siteBagQtyMap = target.getSiteBagQtyMap();
                Map<Date, String> updatedSiteBagQtyType = new HashMap<>(siteBagQtyMap);

                if (source.getSiteStatus()!=null && source.getSiteStatus().equalsIgnoreCase(SiteStatus.SITE_CONVERTED.getCode()) && orderCount <= siteConversionCap) {
                    qtyValue = (source.getConvertedToCementType().contains("NONPREMIUM")) ? "NON-PREMIUM:" : "PREMIUM:";
                    qtyValue += source.getNumberOfBagsPurchased();

                }
                else if (null == source.getSiteStatus() || source.getSiteStatus().isEmpty()) {
                    qtyValue = (source.getCementType().contains("NONPREMIUM")) ? "NON-PREMIUM:" : "PREMIUM:";
                    qtyValue += source.getNumberOfBagsPurchased();
                }
                if(!qtyValue.isEmpty()) {
                    LOG.info("Existing Site Bag Qty Map: " + siteBagQtyMap);
                    updatedSiteBagQtyType.put(lastVisitTime, qtyValue);
                    target.setSiteBagQtyMap(updatedSiteBagQtyType);
                }
            }
            else {
                Map<Date, String> newSiteBagQtyMap = new HashMap<>();
                if (source.getSiteStatus()!=null && source.getSiteStatus().equalsIgnoreCase(SiteStatus.SITE_CONVERTED.getCode()) && orderCount <= siteConversionCap) {
                    qtyValue = (source.getConvertedToCementType().contains("NONPREMIUM")) ? "NON-PREMIUM:" : "PREMIUM:";
                    qtyValue += source.getNumberOfBagsPurchased();

                }
                else if (null == source.getSiteStatus() || source.getSiteStatus().isEmpty()) {
                    qtyValue = (source.getCementType().contains("NONPREMIUM")) ? "NON-PREMIUM:" : "PREMIUM:";
                    qtyValue += source.getNumberOfBagsPurchased();
                }
                if(!qtyValue.isEmpty()) {
                    LOG.info("New Site Bag Qty Map: " + newSiteBagQtyMap);
                    newSiteBagQtyMap.put(lastVisitTime, qtyValue);
                    target.setSiteBagQtyMap(newSiteBagQtyMap);
                }
            }

            double numberOfBagsPurchased = target.getNumberOfBagsPurchased()!=null ? target.getNumberOfBagsPurchased() + source.getNumberOfBagsPurchased() : source.getNumberOfBagsPurchased();
            target.setNumberOfBagsPurchased(numberOfBagsPurchased);
            target.setSiteConversionSynced(false);
        }

        if (source.getSiteStatus() != null) {
            target.setSiteStatus(SiteStatus.valueOf(source.getSiteStatus()));
        }


        if (source.getDateOfPurchase() != null && StringUtils.isNotBlank(source.getDateOfPurchase())) {
            target.setDateOfPurchase(getParsedDate(source.getDateOfPurchase()));
        }

        if (source.getNextDateOfVisit() != null && StringUtils.isNotBlank(source.getNextDateOfVisit())) {
            target.setNextDateOfVisit(getParsedDate(source.getNextDateOfVisit()));
        }

        if (StringUtils.isNotBlank(source.getDealer())) {
            SclCustomerModel dealer = (SclCustomerModel) userService.getUserForUID(source.getDealer());
            target.setDealer(dealer);
        }
        if (StringUtils.isNotBlank(source.getSp())) {
            SclCustomerModel sp = (SclCustomerModel) userService.getUserForUID(source.getSp());
            target.setSp(sp);
        }

        target.setRetailer(source.getRetailer());

        if (source.getTechnicalAssistanceRequired() != null) {
            target.setTechnicalAssistanceRequired(source.getTechnicalAssistanceRequired());
        }
        target.setPricePerRange(source.getPricePerRange());


        target.setSynced(Boolean.FALSE);


        Integer last_site_active_days = dataConstraintDao.findDaysByConstraintName("LAST_SITE_ACTIVE_DAYS");
        LocalDate last6MonthsDate = LocalDate.now().minusDays(last_site_active_days);
        Date date = Date.from(last6MonthsDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        if (target != null) {
            if ((target.getSiteStatus() != null && target.getSiteStatus().equals(SiteStatus.CLOSED) || (target.getLastSiteVisitDate() != null && target.getLastSiteVisitDate().before(date)))) {
                target.setSiteActive(SITEACTIVE_NO);
            } else {
                target.setSiteActive(SITEACTIVE_YES);
            }
        }
    }

    private Date getParsedDate(String dateOfPurchase) {
        Date startDate = null;
        if (dateOfPurchase != null) {
            try {
                startDate = new SimpleDateFormat("dd/MM/yyyy").parse(dateOfPurchase);

            } catch (ParseException e) {
                LOG.error("Error Parsing Date", e);
                throw new IllegalArgumentException(String.format("Please provide valid date %s", dateOfPurchase));
            }
        }
        return startDate;
    }


    private void handleSiteConverted(MapNewSiteData source, SclSiteMasterModel target) {
        PremiumProductType premiumProductType = getPremiumProductType(source.getConvertedToCementType());
        boolean isPremiumProduct = checkIfPremiumProduct(premiumProductType);

        if (source.getConvertedToBrand() != null) {
            LOG.info(String.format("converted to brand is::%s for Site Uid::%s", source.getConvertedToBrand(), source.getSiteId()));
            CompetitorProductModel cementProduct = siteManagementDao.findCementProductByCode(source.getConvertedToBrand());
            LOG.info(String.format("fetched cement product for convertedToBrand is::%s for Site Uid::%s", cementProduct, source.getSiteId()));

            target.setCementProduct(cementProduct);
        }

        target.setSiteConvertedDate(new Date());
        target.setIsPremium(isPremiumProduct);
    }

    private void handleSiteLost(MapNewSiteData source, SclSiteMasterModel target) {
        PremiumProductType premiumProductType = getPremiumProductType(source.getLostToCementType());
        boolean isPremiumProduct = checkIfPremiumProduct(premiumProductType);

        if (source.getLostToCementBrand() != null) {
            LOG.info(String.format("Lost to brand is::%s for Site Uid::%s", source.getLostToCementBrand(),source.getSiteId()));
            CompetitorProductModel cementProduct = siteManagementDao.findCementProductByCode(source.getLostToCementBrand());
            LOG.info(String.format("fetched cement product for lostToBrand is::%s for Site Uid::%s", cementProduct,source.getSiteId()));
            target.setCementProduct(cementProduct);
            target.setLostToCementProduct(cementProduct);
            target.setSiteLostDate(new Date());
        }

        if (!source.getReasonsForSiteLoss().isBlank()) {
            target.setReasonForSiteLoss(enumerationService.getEnumerationValue(ReasonForSiteLoss.class, source.getReasonsForSiteLoss()));
        }

        target.setIsPremium(isPremiumProduct);
    }

    private void handleCementBrand(MapNewSiteData source, SclSiteMasterModel target) {
        if (source.getCementBrand() != null) {
            LOG.info(String.format("cement brand is::%s for Site Uid::%s", source.getCementBrand(),source.getSiteId()));
            CompetitorProductModel cementProduct = siteManagementDao.findCementProductByCode(source.getCementBrand());
            LOG.info(String.format("fetched cementProduct is::%s for Site Uid::%s", cementProduct,source.getSiteId()));
            target.setCementProduct(cementProduct);
        }
    }

    private void handlePreviousState(MapNewSiteData source, SclSiteMasterModel target) {
        if (source.getCementBrand() != null) {
            LOG.info(String.format("cement brand is::%s for Site Uid::%s", source.getCementBrand(),source.getSiteId()));
            CompetitorProductModel previousCementProduct = siteManagementDao.findCementProductByCode(source.getCementBrand());
            LOG.info(String.format("fetched previousCementProduct is::%s for Site Uid::%s", previousCementProduct,source.getSiteId()));
            target.setPreviousCementProduct(previousCementProduct);
        }

        if (source.getSiteCategoryType() != null) {
            target.setPreviousCategoryType(enumerationService.getEnumerationValue(CompetitorProductType.class, source.getSiteCategoryType()));
        }
    }

    private PremiumProductType getPremiumProductType(String cementType) {
        if (cementType != null) {
            return enumerationService.getEnumerationValue(PremiumProductType.class, cementType);
        }
        return null;
    }

    private boolean checkIfPremiumProduct(PremiumProductType premiumProductType) {
        return premiumProductType != null && premiumProductType.getCode() != null
                && premiumProductType.getCode().toUpperCase().contains("PREMIUM")
                && !premiumProductType.getCode().toUpperCase().contains("NONPREMIUM");
    }
}

