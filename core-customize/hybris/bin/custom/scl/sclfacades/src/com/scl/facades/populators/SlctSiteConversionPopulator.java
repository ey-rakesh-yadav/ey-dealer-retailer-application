package com.scl.facades.populators;

import com.scl.core.model.*;
import com.scl.facades.data.SiteConversionData;
import com.scl.facades.data.SiteConversionListData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.i18n.I18NService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.fest.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SlctSiteConversionPopulator implements Populator<SiteTransactionModel, SiteConversionData> {

    @Autowired
    EnumerationService enumerationService;

    @Autowired
    I18NService i18NService;

    private static final Logger LOG = Logger.getLogger(SlctSiteConversionPopulator.class);

    @Override
    public void populate(SiteTransactionModel siteTransactionModel, SiteConversionData siteConversionData) throws ConversionException {
        SclSiteMasterModel siteMasterModel = siteTransactionModel.getSite();
        String siteId = siteMasterModel.getUid();
        String siteTransactionId = siteTransactionModel.getId();
        try {
            SclUserModel tsoUser = (SclUserModel) siteMasterModel.getCreatedBy();

            populateBasicSiteDetails(siteTransactionModel, siteConversionData, siteMasterModel, siteId, siteTransactionId, tsoUser);
            populateSiteAddress(siteConversionData, siteMasterModel);
            populatePreviousProductDetails(siteTransactionModel, siteConversionData);
            populateConvertedProductDetails(siteTransactionModel, siteConversionData);
            populateProvidedServicesInfo(siteTransactionModel, siteConversionData);
            populateTransactionalInfo(siteTransactionModel, siteConversionData);
            populateOtherInfluencerDetails(siteTransactionModel, siteConversionData);

            //Populate Site Status
            if(Objects.nonNull(siteTransactionModel.getSiteStatus())) {
                String siteStatus = enumerationService.getEnumerationName(siteTransactionModel.getSiteStatus(),i18NService.getCurrentLocale());
                siteConversionData.setSiteStatus(siteStatus);
            }

            //Populate Dealer Details
            if(Objects.nonNull(siteTransactionModel.getDealer())) {
                SclCustomerModel dealer = siteTransactionModel.getDealer();
                siteConversionData.setDealerCode(dealer.getUid());
                siteConversionData.setDealerName(dealer.getName());
            }

            //Populate Retailer Details
            if(Objects.nonNull(siteTransactionModel.getRetailer())) {
                SclCustomerModel retailer = siteTransactionModel.getRetailer();
                siteConversionData.setRetailerCode(retailer.getUid());
                siteConversionData.setRetailerName(retailer.getName());
            }
        }
        catch (RuntimeException e) {
            LOG.error(String.format("Exception Occured in the Site Conversion integration. Site Conversion Unique field are Site ID :%s, Site Transaction ID :%s and Error Message : %s",siteId, siteTransactionId, e.getMessage()));
        }

    }

    /**
     * Populate Basic Site Details
     * @param siteTransactionModel
     * @param siteConversionData
     * @param siteMasterModel
     * @param siteId
     * @param siteTransactionId
     * @param tsoUser
     */
    private void populateBasicSiteDetails(SiteTransactionModel siteTransactionModel, SiteConversionData siteConversionData, SclSiteMasterModel siteMasterModel, String siteId, String siteTransactionId, SclUserModel tsoUser) {
        siteConversionData.setSiteCode(siteId);
        siteConversionData.setSiteName(siteMasterModel.getName());
        siteConversionData.setSiteTransactionId(siteTransactionId);
        siteConversionData.setTsoEmailId(tsoUser.getUid());
        siteConversionData.setTsoName(tsoUser.getName());
        siteConversionData.setBalanceCementRequirement(siteTransactionModel.getBalanceCementRequirement());
        siteConversionData.setBuildUpArea(siteMasterModel.getBuiltUpArea());
        siteConversionData.setContactNumber(siteMasterModel.getMobileNumber());
        siteConversionData.setNextSlabCasting(siteTransactionModel.getNextSlabCasting());
        if(Objects.nonNull(siteTransactionModel.getPersonMetAtSite())) {
            String personMet = enumerationService.getEnumerationName(siteTransactionModel.getPersonMetAtSite(),i18NService.getCurrentLocale());
            siteConversionData.setPersonMet(personMet);
        }
        if (Objects.nonNull(siteTransactionModel.getConstructionStage())) {
            String constructionStage = enumerationService.getEnumerationName(siteTransactionModel.getConstructionStage(),i18NService.getCurrentLocale());
            siteConversionData.setConstructionStage(constructionStage);
        }
    }

    /**
     * Populate Service info like Service Type and the services provided
     * @param siteTransactionModel
     * @param siteConversionData
     */
    private static void populateProvidedServicesInfo(SiteTransactionModel siteTransactionModel, SiteConversionData siteConversionData) {
        if(siteTransactionModel.getServiceProvidedAtSite()!=null) {
            if(siteTransactionModel.getServiceProvidedAtSite()) {
                siteConversionData.setServiceProvided("YES");
            }
            else {
                siteConversionData.setServiceProvided("NO");
            }
        }
        if(Objects.nonNull(siteTransactionModel.getServiceType())) {
            siteConversionData.setServiceType(siteTransactionModel.getServiceType().getName());
        }
        if(!Collections.isEmpty(siteTransactionModel.getServiceTypeTest())) {
            String serviceTypeTest = siteTransactionModel.getServiceTypeTest().stream()
                    .map(SiteServiceTestModel::getName)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining(","));
            siteConversionData.setServiceTypeTest(serviceTypeTest);
        }
    }

    /**
     * Populate Transaction info like conversion sales, Date of purchase, price etc.
     * @param siteTransactionModel
     * @param siteConversionData
     */
    private static void populateTransactionalInfo(SiteTransactionModel siteTransactionModel, SiteConversionData siteConversionData) {
        siteConversionData.setConversionDate(siteTransactionModel.getSiteConvertedDate());
        siteConversionData.setPremiumSale(siteTransactionModel.getPremiumSale());
        siteConversionData.setNonPremiumSale(siteTransactionModel.getNonPremiumSale());
        siteConversionData.setIsPremiumConversion(siteTransactionModel.getIsPremium());
        siteConversionData.setPricePerRange(siteTransactionModel.getPricePerRange());
        siteConversionData.setDateOfPurchase(siteTransactionModel.getDateOfPurchase());
        siteConversionData.setRemarks(siteTransactionModel.getRemarks());
    }

    /**
     * Populate the details of Mason/Contractor/Architect
     * @param siteTransactionModel
     * @param siteConversionData
     */
    private static void populateOtherInfluencerDetails(SiteTransactionModel siteTransactionModel, SiteConversionData siteConversionData) {
        siteConversionData.setContractorName(siteTransactionModel.getContractorName());
        siteConversionData.setContractorPhoneNumber(siteTransactionModel.getContractorPhoneNumber());
        siteConversionData.setMasonName(siteTransactionModel.getMasonName());
        siteConversionData.setMasonPhoneNumber(siteTransactionModel.getMasonPhoneNumber());
        siteConversionData.setArchitectName(siteTransactionModel.getArchitectName());
        siteConversionData.setArchitectPhoneNumber(siteTransactionModel.getArchitectNumber());
    }

    /**
     * Populate Converted Product Details from SclSiteMasterModel to the SiteConversionData object
     * @param siteTransactionModel
     * @param siteConversionData
     */
    private void populateConvertedProductDetails(SiteTransactionModel siteTransactionModel, SiteConversionData siteConversionData) {
        if(Objects.nonNull(siteTransactionModel.getConvertedSiteCategory())) {
            String convertedSiteCategory = enumerationService.getEnumerationName(siteTransactionModel.getConvertedSiteCategory(), i18NService.getCurrentLocale());
            siteConversionData.setConvertedSiteCategory(convertedSiteCategory);
        }
        if(Objects.nonNull(siteTransactionModel.getConvertedToProduct())) {
            CompetitorProductModel convertedCementProductModel =  siteTransactionModel.getConvertedToProduct();
            String convertedProduct = convertedCementProductModel.getCode();
            siteConversionData.setConvertedCementProduct(convertedProduct);

            if(Objects.nonNull(convertedCementProductModel.getBrand())) {
                String convertedCementBrand = convertedCementProductModel.getBrand().getIsocode();
                siteConversionData.setConvertedCementBrand(convertedCementBrand);
            }
            if(Objects.nonNull(convertedCementProductModel.getPremiumProductType())) {
                String convertedCementType = enumerationService.getEnumerationName(convertedCementProductModel.getPremiumProductType(),i18NService.getCurrentLocale());
                siteConversionData.setConvertedCementType(convertedCementType);
            }
        }
    }

    /**
     * Populate Previous Product Details from SclSiteMasterModel to the SiteConversionData object
     * @param siteTransactionModel
     * @param siteConversionData
     */
    private void populatePreviousProductDetails(SiteTransactionModel siteTransactionModel, SiteConversionData siteConversionData) {
        if(Objects.nonNull(siteTransactionModel.getPreviousSiteCategory())) {
            String previousSiteCategory = enumerationService.getEnumerationName(siteTransactionModel.getPreviousSiteCategory(), i18NService.getCurrentLocale());
            siteConversionData.setPreviousSiteCategory(previousSiteCategory);
        }
        if(Objects.nonNull(siteTransactionModel.getPreviousProduct())) {
            CompetitorProductModel previousCementProductModel = siteTransactionModel.getPreviousProduct();
            String previousProduct = previousCementProductModel.getCode();
            siteConversionData.setPreviousCementProduct(previousProduct);

            if(Objects.nonNull(previousCementProductModel.getBrand())) {
                String previousCementBrand = previousCementProductModel.getBrand().getIsocode();
                siteConversionData.setPreviousCementBrand(previousCementBrand);
            }
            if(Objects.nonNull(previousCementProductModel.getPremiumProductType())) {
                String previousCementType = enumerationService.getEnumerationName(previousCementProductModel.getPremiumProductType(),i18NService.getCurrentLocale());
                siteConversionData.setPreviousCementType(previousCementType);
            }
        }
    }

    /**
     * Populate Site Address from SclSiteMasterModel to the SiteConversionData object
     * @param siteConversionData
     * @param siteMasterModel
     */
    private static void populateSiteAddress(SiteConversionData siteConversionData, SclSiteMasterModel siteMasterModel) {
        if(!Collections.isEmpty(siteMasterModel.getAddresses())) {
            List<AddressModel> billingAddressList = siteMasterModel.getAddresses().stream()
                    .filter(AddressModel::getBillingAddress)
                    .collect(Collectors.toList());
            if (!Collections.isEmpty(billingAddressList)) {

                AddressModel billingAddress = billingAddressList.get(0);
                siteConversionData.setState(billingAddress.getState());
                siteConversionData.setDistrict(billingAddress.getDistrict());
                siteConversionData.setCity(billingAddress.getErpCity());
                siteConversionData.setTaluka(billingAddress.getTaluka());
                siteConversionData.setPinCode(billingAddress.getPostalcode());
                siteConversionData.setLatitude(billingAddress.getLatitude());
                siteConversionData.setLongitude(billingAddress.getLongitude());
                if(StringUtils.isNotBlank(billingAddress.getLine1())) {
                    String billAddress = billingAddress.getLine1();
                    if(StringUtils.isNotBlank(billingAddress.getLine2())) {
                        billAddress += "," + billingAddress.getLine2();
                    }
                    siteConversionData.setSiteAddress(billAddress);
                }
            }
        }
    }
}
