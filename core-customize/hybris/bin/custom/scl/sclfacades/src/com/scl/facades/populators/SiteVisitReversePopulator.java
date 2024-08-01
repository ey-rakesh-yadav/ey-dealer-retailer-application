package com.scl.facades.populators;

import com.scl.core.dao.SiteManagementDao;
import com.scl.core.enums.*;
import com.scl.core.model.*;
import com.scl.core.services.impl.SiteManagementServiceImpl;
import com.scl.facades.constants.SclFacadesConstants;
import com.scl.facades.data.MapNewSiteData;
import com.scl.facades.djp.data.CounterMappingData;
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
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class SiteVisitReversePopulator implements Populator<MapNewSiteData, SiteVisitMasterModel> {

    private static final Logger LOG = Logger.getLogger(SiteVisitReversePopulator.class);

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

    @Autowired
    EnumerationService enumerationService;

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
            target.setSiteCategoryType(enumerationService.getEnumerationValue(CompetitorProductType.class,source.getSiteCategoryType()));
            }
            boolean isPremiumProduct = false;
            PremiumProductType premiumProductType = null;
           if(source.getConvertedToCementType()!=null) {
               premiumProductType = enumerationService.getEnumerationValue(PremiumProductType.class,source.getConvertedToCementType());
               isPremiumProduct = isPremiumCementType(premiumProductType);
            }

            if(source.getConvertedToBrand()!=null) {
                LOG.info(String.format("Converted to Cement brand is::%s for Site ID::%s, SiteVisit ID::%s", source.getConvertedToBrand(), source.getSiteId(), target.getId()));
                target.setConvertedToProduct(siteManagementDao.findCementProductByCode(source.getConvertedToBrand()));
            }

            if(source.getLostToCementBrand()!=null) {
                LOG.info(String.format("Lost to Cement brand is::%s for Site ID::%s, SiteVisit ID::%s", source.getLostToCementBrand(), source.getSiteId(), target.getId()));
                target.setLostToCementProduct(siteManagementDao.findCementProductByCode(source.getLostToCementBrand()));

                if (!source.getReasonsForSiteLoss().isBlank()) {
                    target.setReasonForSiteLoss(enumerationService.getEnumerationValue(ReasonForSiteLoss.class, source.getReasonsForSiteLoss()));
                }

                target.setSiteLostDate(new Date());
            }

            if(source.getCementBrand()!=null && (source.getSiteCategoryType()!=null && !source.getSiteCategoryType().equalsIgnoreCase(SclFacadesConstants.SITE_CATEGORY.FRESH_SITE))) {
                target.setCementProduct(siteManagementDao.findCementProductByCode(source.getCementBrand()));
            }
            if (source.getSiteStatus() != null && source.getSiteStatus().equalsIgnoreCase(SiteStatus.SITE_CONVERTED.getCode()))
            {
                target.setSiteConvertedDate(new Date());
            }
            target.setIsPremium(isPremiumProduct);

        if(source.getSiteStatus()!=null) {
            target.setSiteStatus(SiteStatus.valueOf(source.getSiteStatus()));
        }

        target.setRemarks(source.getRemarks());
        target.setReasons(source.getReasons());
        target.setNumberOfBagsPurchased(source.getNumberOfBagsPurchased());
        target.setPricePerRange(source.getPricePerRange());
        if(StringUtils.isNotBlank(source.getDateOfPurchase())) {
            target.setDateOfPurchase(getParsedDate(source.getDateOfPurchase()));
        }
        if(StringUtils.isNotBlank(source.getNextDateOfVisit())) {
            target.setNextDateOfVisit(getParsedDate(source.getNextDateOfVisit()));
        }
        if(StringUtils.isNotBlank(source.getDealer())) {
            SclCustomerModel dealer = (SclCustomerModel) userService.getUserForUID(source.getDealer());
            target.setDealer(dealer);
        }
        if(StringUtils.isNotBlank(source.getSp())) {
            SclCustomerModel sp = (SclCustomerModel) userService.getUserForUID(source.getSp());
            target.setSp(sp);
        }
        target.setRetailer(source.getRetailer());
        target.setTechnicalAssistanceRequired(source.getTechnicalAssistanceRequired());
        target.setSynced(Boolean.FALSE);
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

    private boolean isPremiumCementType(PremiumProductType cementType) {
        return cementType != null && cementType.getCode() != null &&
                cementType.getCode().toUpperCase().contains("PREMIUM") && !cementType.getCode().toUpperCase().contains("NONPREMIUM");
    }

}
