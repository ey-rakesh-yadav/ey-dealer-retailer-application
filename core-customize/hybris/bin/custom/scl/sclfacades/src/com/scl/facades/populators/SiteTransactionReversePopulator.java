package com.scl.facades.populators;

import com.scl.core.dao.DataConstraintDao;
import com.scl.core.dao.SiteManagementDao;
import com.scl.core.enums.*;
import com.scl.core.model.*;
import com.scl.facades.data.MapNewSiteData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.enumeration.EnumerationService;
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
import java.util.Objects;
import java.util.stream.Collectors;

public class SiteTransactionReversePopulator implements Populator<MapNewSiteData, SiteTransactionModel> {

    private static final Logger LOG = Logger.getLogger(SiteTransactionReversePopulator.class);

    @Autowired
    ModelService modelService;

    @Autowired
    UserService userService;

    @Autowired
    SiteManagementDao siteManagementDao;

    @Autowired
    EnumerationService enumerationService;

    @Override
    public void populate(MapNewSiteData mapNewSiteData, SiteTransactionModel siteTransactionModel) throws ConversionException {
        boolean isPremiumProduct = false;
        PremiumProductType premiumProductType = null;
        String siteId = mapNewSiteData.getSiteId();
        String siteTransactionId = mapNewSiteData.getSiteTransactionId();

         //Populating Basic Site Details captured in the form
        siteTransactionModel.setContractorName(mapNewSiteData.getContractorName());
        siteTransactionModel.setContractorPhoneNumber(mapNewSiteData.getContractorPhoneNumber());
        siteTransactionModel.setMasonName(mapNewSiteData.getMasonName());
        siteTransactionModel.setMasonPhoneNumber(mapNewSiteData.getMasonPhoneNumber());
        siteTransactionModel.setArchitectName(mapNewSiteData.getArchitectName());
        siteTransactionModel.setArchitectNumber(mapNewSiteData.getArchitectPhoneNumber());
        siteTransactionModel.setPersonMetAtSite(enumerationService.getEnumerationValue(PersonMetAtSite.class, mapNewSiteData.getPersonMetAtSite()));
        siteTransactionModel.setConstructionStage(enumerationService.getEnumerationValue(ConstructionStage.class, mapNewSiteData.getCurrentStageOfConstruction()));
        siteTransactionModel.setNextSlabCasting(parseDate(mapNewSiteData.getNextSlabCasting()));
        siteTransactionModel.setBalanceCementRequirement(mapNewSiteData.getBalanceCementRequirement());
        siteTransactionModel.setServiceProvidedAtSite(mapNewSiteData.getServiceProvidedAtSite());

        //Populating the ServiceType into the SiteTransaction Model
        populateServiceType(mapNewSiteData, siteTransactionModel, siteId, siteTransactionId);

        //Populate List of SiteServiceTest Models into the Site Transaction Model
        populateSiteServicesList(mapNewSiteData, siteTransactionModel, siteId, siteTransactionId);

        //Populating the Previous Product details before the Site got converted
        populateCementPrevProdDetails(mapNewSiteData, siteTransactionModel, siteId, siteTransactionId);

        //Populating the Converted Product details after the Site got converted
        populateConvertedCementProdDetails(mapNewSiteData, siteTransactionModel, siteId, siteTransactionId);

        siteTransactionModel.setSiteStatus(enumerationService.getEnumerationValue(SiteStatus.class, mapNewSiteData.getSiteStatus().toUpperCase()));

        //Checking if the Converted Product Type is Premium or Not
        if(StringUtils.isNotBlank(mapNewSiteData.getConvertedToCementType())) {
            premiumProductType = enumerationService.getEnumerationValue(PremiumProductType.class,mapNewSiteData.getConvertedToCementType());
            isPremiumProduct = isPremiumCementType(premiumProductType);
        }
        siteTransactionModel.setIsPremium(isPremiumProduct);

         //Populating Transaction Sales Details
        siteTransactionModel.setNumberOfBagsPurchased(mapNewSiteData.getNumberOfBagsPurchased());
        if(isPremiumProduct) {
            siteTransactionModel.setPremiumSale(mapNewSiteData.getNumberOfBagsPurchased());
            siteTransactionModel.setNonPremiumSale(0.0);
        }
        else {
            siteTransactionModel.setNonPremiumSale(mapNewSiteData.getNumberOfBagsPurchased());
            siteTransactionModel.setPremiumSale(0.0);
        }
        siteTransactionModel.setDateOfPurchase(parseDate(mapNewSiteData.getDateOfPurchase()));
        siteTransactionModel.setPricePerRange(mapNewSiteData.getPricePerRange());

         //Populating Mapped SclCustomer Details if any. Ex: Dealer, Retailer and Sales Promoter if mapped to that Site for the product sale
        siteTransactionModel.setDealer(getCustomerForUid(mapNewSiteData.getDealer()));
        siteTransactionModel.setRetailer(getCustomerForUid(mapNewSiteData.getRetailer()));
        siteTransactionModel.setSalesPromoter(getCustomerForUid(mapNewSiteData.getSp()));

        siteTransactionModel.setRemarks(mapNewSiteData.getRemarks());
        siteTransactionModel.setSiteConvertedDate(new Date());

         //Setting the SiteConversionSynced field to false so this model would get picked up in the SLCT CRM integration Oubound API
        siteTransactionModel.setSiteConversionSynced(Boolean.FALSE);
    }

    /**
     * Populate List of SiteServiceTest Models into the Site Transaction Model
     *
     * @param mapNewSiteData
     * @param siteTransactionModel
     * @param siteId
     * @param siteTransactionId
     */
    private void populateSiteServicesList(MapNewSiteData mapNewSiteData, SiteTransactionModel siteTransactionModel, String siteId, String siteTransactionId) {
        List<String> serviceTypeTestDataList = mapNewSiteData.getServiceTypeTest();

        if (serviceTypeTestDataList != null && !serviceTypeTestDataList.isEmpty()) {
            LOG.info(String.format("Site Service Test List coming from UI for Site ID %s and Site Transaction ID %s :: %s", siteId, siteTransactionId, serviceTypeTestDataList));

            List<SiteServiceTestModel> list = serviceTypeTestDataList.stream()
                    .map(siteManagementDao::findServiceTypeTestByCode)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            siteTransactionModel.setServiceTypeTest(list);

            List<String> fetchedSiteServiceCodes = list.stream()
                    .map(SiteServiceTestModel::getCode)
                    .collect(Collectors.toList());

            LOG.info(String.format("Fetched List of SiteServiceTestModel codes for Site ID %s and Site Transaction ID %s :: %s", siteId, siteTransactionId, fetchedSiteServiceCodes));
        }
    }

    /**
     * Populates the ServiceType details into the SiteTransactionModel
     *
     * @param mapNewSiteData
     * @param siteTransactionModel
     * @param siteId
     * @param siteTransactionId
     */
    private void populateServiceType(MapNewSiteData mapNewSiteData, SiteTransactionModel siteTransactionModel, String siteId, String siteTransactionId) {
        if(StringUtils.isNotBlank(mapNewSiteData.getServiceType())) {
            LOG.info(String.format("ServiceType coming from UI for Site ID %s and Site Transaction ID %s is %s", siteId, siteTransactionId, mapNewSiteData.getServiceType()));
            SiteServiceTypeModel siteServiceTypeModel = siteManagementDao.findServiceTypeByCode(mapNewSiteData.getServiceType());
            siteTransactionModel.setServiceType(siteServiceTypeModel);
            if(Objects.isNull(siteServiceTypeModel))
                LOG.info(String.format("No ServiceType Model found with ServiceType code %s for the Site ID %s and Site Transaction ID %s", mapNewSiteData.getServiceType(), siteId, siteTransactionId));
        }
    }

    /**
     * Populates the details of the converted cement product into the SiteTransactionModel.
     *
     * @param mapNewSiteData
     * @param siteTransactionModel
     * @param siteID
     * @param siteTransactionId
     */
    private void populateConvertedCementProdDetails(MapNewSiteData mapNewSiteData, SiteTransactionModel siteTransactionModel, String siteID, String siteTransactionId) {
        if(StringUtils.isNotBlank(mapNewSiteData.getConvertedToBrand())) {
            LOG.info(String.format("Converted to cement brand coming for Site ID %s and Site Transaction ID %s from UI is::%s", siteID, siteTransactionId, mapNewSiteData.getConvertedToBrand()));
            CompetitorProductModel convertedToProduct = siteManagementDao.findCementProductByCode(mapNewSiteData.getConvertedToBrand());
            siteTransactionModel.setConvertedToProduct(convertedToProduct);
            LOG.info(String.format("fetched Converted Cement Product for Site ID %s and Site Transaction ID %s is::%s", siteID, siteTransactionId, convertedToProduct));
            if(Objects.nonNull(convertedToProduct)) {
                siteTransactionModel.setConvertedSiteCategory(convertedToProduct.getCompetitorProductType());
            }
        }
    }

    /**
     * Populates the details of the previous cement product into the SiteTransactionModel.
     *
     * @param mapNewSiteData
     * @param siteTransactionModel
     * @param siteId
     * @param siteTransactionId
     */
    private void populateCementPrevProdDetails(MapNewSiteData mapNewSiteData, SiteTransactionModel siteTransactionModel, String siteId, String siteTransactionId) {
        if(StringUtils.isNotBlank(mapNewSiteData.getCementBrand())) {
            LOG.info(String.format("Previous cement brand coming for Site ID %s and Site Transaction ID %s from UI is::%s", siteId, siteTransactionId, mapNewSiteData.getCementBrand()));
            CompetitorProductModel previousProduct = siteManagementDao.findCementProductByCode(mapNewSiteData.getCementBrand());
            siteTransactionModel.setPreviousProduct(previousProduct);
            LOG.info(String.format("fetched Previous Cement Product for Site ID %s and Site Transaction ID %s is::%s",siteId, siteTransactionId, previousProduct));
            if(Objects.nonNull(previousProduct)) {
                siteTransactionModel.setPreviousSiteCategory(previousProduct.getCompetitorProductType());
            }
        }
    }

    /**
     * Converts the date from String type to Date type
     * @param date
     * @return the date in the given dateformat
     */
    private Date parseDate(String date) {
        if (StringUtils.isNotBlank(date)) {
            try {
                return new SimpleDateFormat("dd/MM/yyyy").parse(date);
            } catch (ParseException e) {
                LOG.error("Error Parsing Date", e);
                throw new IllegalArgumentException(String.format("Please provide a valid date: %s", date));
            }
        }
        return null;
    }

    /**
     * Check if the converted product is premium or not
     * @param cementType
     * @return true if the product is premium else false
     */
    private boolean isPremiumCementType(PremiumProductType cementType) {
        return cementType != null && cementType.getCode() != null &&
                cementType.getCode().toUpperCase().contains("PREMIUM") && !cementType.getCode().toUpperCase().contains("NONPREMIUM");
    }

    /**
     * Fetches the SclCustomerModel for a given UID
     * @param uid
     * @return the SclCustomerModel if exists for the UID, else null
     */
    private SclCustomerModel getCustomerForUid(String uid) {
        if(StringUtils.isNotBlank(uid)) {
            return (SclCustomerModel) userService.getUserForUID(uid);
        }
        return null;
    }

}
