package com.scl.facades.impl;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.InfluencerDao;
import com.scl.core.dao.SalesPlanningDao;
import com.scl.core.enums.SclUserType;
import com.scl.core.enums.TerritoryLevels;
import com.scl.core.enums.WorkflowActions;
import com.scl.core.enums.WorkflowStatus;
import com.scl.core.model.*;
import com.scl.core.services.*;
import com.scl.facades.SalesPlanningFacade;
import com.scl.facades.data.*;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.stream.Collectors;

public class SalesPlanningFacadeImpl implements SalesPlanningFacade {
    @Autowired
    InfluencerDao influencerDao;
    @Autowired
    SclWorkflowService sclWorkflowService;
    @Autowired
    ModelService modelService;
    @Resource
    SalesPlanningService salesPlanningService;
    @Resource
    SalesPerformanceService salesPerformanceService;
    @Resource
    SalesPlanningDao salesPlanningDao;
    @Resource
    private UserService userService;
    @Resource
    private BaseSiteService baseSiteService;
    @Resource
    private TerritoryManagementService territoryManagementService;
    @Resource
    private RetailerSalesSummaryService retailerSalesSummaryService;
    @Resource
    private ProductService productService;
    @Resource
    CatalogVersionService catalogVersionService;

    @Autowired
    Converter<RetailerSalesSummaryModel, AnnualSalesTargetSettingData> annualTargetSettingDataConverter;

    private static final Logger LOG = Logger.getLogger(SalesPlanningFacadeImpl.class);

    @Override
    public ErrorListWsDTO submitAnnualSalesTargetSettingForDealers(AnnualSalesTargetSettingListData data) {
        return salesPlanningService.submitAnnualSalesTargetSettingForDealers(data);
    }

    @Override
    public ErrorListWsDTO submitAnnualSalesTargetSettingForRetailers(AnnualSalesTargetSettingListData data) {
        return salesPlanningService.submitAnnualSalesTargetSettingForRetailers(data);
    }

    @Override
    public ErrorListWsDTO submitFinalizeAnnualSalesTargetSettingForDealers(AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData) {
        return salesPlanningService.submitFinalizeAnnualSalesTargetSettingForDealers(annualSalesMonthWiseTargetListData);
    }

    @Override
    public ErrorListWsDTO submitFinalizeAnnualSalesTargetSettingForRetailers(AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData) {
        return salesPlanningService.submitFinalizeAnnualSalesTargetSettingForRetailers(annualSalesMonthWiseTargetListData);
    }

    @Override
    public boolean submitOnboardedAnnualSalesTargetSettingForDealers(AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData) {
        return salesPlanningService.submitOnboardedAnnualSalesTargetSettingForDealers(annualSalesMonthWiseTargetListData);
    }

    @Override
    public boolean submitOnboardedAnnualSalesTargetSettingForRetailers(AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData) {
        return salesPlanningService.submitOnboardedAnnualSalesTargetSettingForRetailers(annualSalesMonthWiseTargetListData);
    }

   @Override
    public AnnualSalesMonthWiseTargetListData viewPlannedSalesforDealersMonthwise(String subArea, String filter) {
        SclUserModel sclUser = (SclUserModel) getUserService().getCurrentUser();
        List<AnnualSalesMonthWiseTargetData> annualSalesMonthWiseTargetDataList = new ArrayList<>();
        BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
        SubAreaMasterModel subAreaMaster=territoryManagementService.getTerritoryById(subArea);
        AnnualSalesModel annualSalesModel = salesPlanningService.viewPlannedSalesforDealersRetailersMonthWise(subArea, sclUser, brand);
        AnnualSalesMonthWiseTargetListData list = new AnnualSalesMonthWiseTargetListData();
        if(annualSalesModel!=null) {
            list.setTotalCurrentYearSales(annualSalesModel.getDealerPlannedTotalCySales()!=null?annualSalesModel.getDealerPlannedTotalCySales():0.0);
            list.setTotalPlanSales(annualSalesModel.getDealerPlannedTotalPlanSales()!=null?annualSalesModel.getDealerPlannedTotalPlanSales():0.0);
        }
        if(StringUtils.isBlank(filter)) {
            if (annualSalesModel != null) {
                if (annualSalesModel.getDealerRevisedAnnualSales() != null && Objects.nonNull(annualSalesModel.getDealerRevisedAnnualSales()) &&
                        !annualSalesModel.getDealerRevisedAnnualSales().isEmpty()) {
                    for (DealerRevisedAnnualSalesModel dealerRevisedAnnualSalesModel : annualSalesModel.getDealerRevisedAnnualSales()) {
                        if(annualSalesModel.getSubAreaMaster()!=null)
                        {
                            SubAreaMasterModel subAreaMasterModel = annualSalesModel.getSubAreaMaster();
                       // for (SubAreaMasterModel subAreaMasterModel : annualSalesModel.getSubAreaMasterList()) {
                            if (dealerRevisedAnnualSalesModel.getIsExistingDealerRevisedForReview() != null && dealerRevisedAnnualSalesModel.getIsNewDealerOnboarded() != null && dealerRevisedAnnualSalesModel.getIsExistingDealerRevisedForReview().equals(false) && dealerRevisedAnnualSalesModel.getIsNewDealerOnboarded().equals(false)) {
                                if (subAreaMasterModel.equals(dealerRevisedAnnualSalesModel.getSubAreaMaster())) {
                                    AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData = new AnnualSalesMonthWiseTargetData();
                                    annualSalesMonthWiseTargetData.setCustomerCode(dealerRevisedAnnualSalesModel.getCustomerCode());
                                    annualSalesMonthWiseTargetData.setCustomerName(dealerRevisedAnnualSalesModel.getCustomerName());
                                    annualSalesMonthWiseTargetData.setCustomerPotential(dealerRevisedAnnualSalesModel.getCustomerPotential() != null ? dealerRevisedAnnualSalesModel.getCustomerPotential() : 0.0);
                                    annualSalesMonthWiseTargetData.setTotalTarget(dealerRevisedAnnualSalesModel.getTotalTarget());

                                    List<MonthWiseTargetData> monthWiseTargetDataList = new ArrayList<>();
                                    if (dealerRevisedAnnualSalesModel.getMonthWiseAnnualTarget() != null &&
                                            !dealerRevisedAnnualSalesModel.getMonthWiseAnnualTarget().isEmpty()) {
                                        for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : dealerRevisedAnnualSalesModel.getMonthWiseAnnualTarget()) {
                                            MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                            if (monthWiseAnnualTargetModel.getCustomerCode().equals(dealerRevisedAnnualSalesModel.getCustomerCode()) && monthWiseAnnualTargetModel.getIsAnnualSalesRevisedForDealer().equals(true)) {
                                                monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                                monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                                monthWiseTargetDataList.add(monthWiseTargetData);
                                            }
                                        }
                                    }
                                    annualSalesMonthWiseTargetData.setMonthWiseTarget(monthWiseTargetDataList);

                                    List<SKUData> skuDataList = new ArrayList<>();
                                    if (dealerRevisedAnnualSalesModel.getListOfSkus() != null && !dealerRevisedAnnualSalesModel.getListOfSkus().isEmpty()) {
                                        for (ProductModel sku : dealerRevisedAnnualSalesModel.getListOfSkus()) {
                                            SKUData skuData = new SKUData();
                                            skuData.setProductCode(sku.getCode());
                                            skuData.setProductName(sku.getName());
                                            if (sku.getProductSale() != null && !sku.getProductSale().isEmpty()) {
                                                for (ProductSaleModel productSale :
                                                        sku.getProductSale()) {
                                                    skuData.setTotalTarget(productSale.getTotalTarget());
                                                }
                                            }

                                            List<MonthWiseTargetData> monthWiseTargetDataSkuList = new ArrayList<>();
                                            List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetList = salesPlanningService.getMonthWiseSkuDetailsBeforeReview(dealerRevisedAnnualSalesModel.getCustomerCode(), skuData.getProductCode(), dealerRevisedAnnualSalesModel.getSubAreaMaster().getPk().toString(), sclUser);
                                            for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : monthWiseAnnualTargetList) {
                                                MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                                if (monthWiseAnnualTargetModel.getProductCode().equals(skuData.getProductCode()) && monthWiseAnnualTargetModel.getIsAnnualSalesRevisedForDealer().equals(true)) {
                                                    monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                                    monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                                    monthWiseTargetDataSkuList.add(monthWiseTargetData);
                                                }
                                            }
                                            skuData.setMonthWiseSkuTarget(monthWiseTargetDataSkuList);
                                            skuDataList.add(skuData);
                                        }
                                    }
                                    annualSalesMonthWiseTargetData.setSkuDataList(skuDataList);
                                    annualSalesMonthWiseTargetDataList.add(annualSalesMonthWiseTargetData);
                                }
                            }
                        //}
                        }
                    }
                }
            }
        }
        else
        {
            DealerRevisedAnnualSalesModel dealerRevisedAnnualSale = salesPlanningService.fetchRecordForDealerRevisedAnnualSalesByCode(subAreaMaster.getPk().toString(), sclUser, filter);
            if(dealerRevisedAnnualSale !=null)
            {
                if(dealerRevisedAnnualSale.getSubAreaMaster().equals(subAreaMaster)) {
                    if (dealerRevisedAnnualSale.getIsExistingDealerRevisedForReview() != null && dealerRevisedAnnualSale.getIsNewDealerOnboarded() != null && dealerRevisedAnnualSale.getIsNewDealerOnboarded().equals(false) && dealerRevisedAnnualSale.getIsExistingDealerRevisedForReview().equals(false)) {
                        AnnualSalesMonthWiseTargetData data = new AnnualSalesMonthWiseTargetData();
                        data.setCustomerCode(dealerRevisedAnnualSale.getCustomerCode());
                        data.setCustomerName(dealerRevisedAnnualSale.getCustomerName());
                        data.setCustomerPotential(dealerRevisedAnnualSale.getCustomerPotential());
                        data.setTotalTarget(dealerRevisedAnnualSale.getTotalTarget());
                        List<MonthWiseTargetData> monthWiseTargetDataList = new ArrayList<>();
                        if (dealerRevisedAnnualSale.getMonthWiseAnnualTarget() != null && !dealerRevisedAnnualSale.getMonthWiseAnnualTarget().isEmpty()) {
                            for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : dealerRevisedAnnualSale.getMonthWiseAnnualTarget()) {
                                if (monthWiseAnnualTargetModel.getCustomerCode().equals(dealerRevisedAnnualSale.getCustomerCode())) {
                                    MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                    monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                    monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                    monthWiseTargetDataList.add(monthWiseTargetData);
                                }
                            }
                        }
                        List<SKUData> skuDataList = new ArrayList<>();
                        if (dealerRevisedAnnualSale.getListOfSkus() != null && !dealerRevisedAnnualSale.getListOfSkus().isEmpty()) {
                            for (ProductModel sku : dealerRevisedAnnualSale.getListOfSkus()) {
                                SKUData skuData = new SKUData();
                                skuData.setProductCode(sku.getCode());
                                skuData.setProductName(sku.getName());
                                if (sku.getProductSale() != null && sku.getProductSale().isEmpty())
                                    for (ProductSaleModel productSaleModel : sku.getProductSale()) {
                                        skuData.setCySales(productSaleModel.getCySales());
                                        skuData.setTotalTarget(productSaleModel.getTotalTarget());
                                    }
                                List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetList = salesPlanningService.getMonthWiseAnnualTargetDetailsForDealerRevised(dealerRevisedAnnualSale.getCustomerCode(), skuData.getProductCode(), dealerRevisedAnnualSale.getSubAreaMaster().getPk().toString());
                                List<MonthWiseTargetData> monthWiseSkuTargetDataList = new ArrayList<>();
                                if (monthWiseAnnualTargetList != null && !monthWiseAnnualTargetList.isEmpty()) {
                                    for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : monthWiseAnnualTargetList) {
                                        if (monthWiseAnnualTargetModel.getCustomerCode().equals(dealerRevisedAnnualSale.getCustomerCode()) &&
                                                monthWiseAnnualTargetModel.getProductCode().equals(sku.getCode())) {
                                            MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                            monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                            monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                            monthWiseSkuTargetDataList.add(monthWiseTargetData);
                                        }
                                    }
                                }
                                skuData.setMonthWiseSkuTarget(monthWiseSkuTargetDataList);
                                skuDataList.add(skuData);
                            }
                        }
                        data.setMonthWiseTarget(monthWiseTargetDataList);
                        data.setSkuDataList(skuDataList);
                        annualSalesMonthWiseTargetDataList.add(data);
                    }
                }
            }
        }
        if(subAreaMaster!=null)
        list.setSubArea(subAreaMaster.getTaluka());
        list.setSubAreaId(subAreaMaster.getPk().toString());
        list.setAnnualSalesMonthWiseTargetData(annualSalesMonthWiseTargetDataList);
        list.setSalesOfficer(sclUser.getUid());
        return list;
    }

    @Override
    public AnnualSalesMonthWiseTargetListData viewPlannedSalesforRetailerMonthwise(String subArea, String filter) {
        SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
        SubAreaMasterModel subAreaMaster=territoryManagementService.getTerritoryById(subArea);
        BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
        AnnualSalesModel annualSalesMonthwiseModel = salesPlanningService.viewPlannedSalesforDealersRetailersMonthWise(subAreaMaster.getPk().toString(), currentUser, brand);
        AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData = new AnnualSalesMonthWiseTargetListData();

        annualSalesMonthWiseTargetListData.setTotalPlanSales(annualSalesMonthwiseModel.getTotalPlannedYearSales()!=null?annualSalesMonthwiseModel.getTotalPlannedYearSales():0.0);
        annualSalesMonthWiseTargetListData.setTotalCurrentYearSales(annualSalesMonthwiseModel.getTotalCurrentYearSales()!=null?annualSalesMonthwiseModel.getTotalCurrentYearSales():0.0);
        List<AnnualSalesMonthWiseTargetData> annualSalesMonthWiseTargetDataList = new ArrayList<>();

        if(annualSalesMonthwiseModel!=null && annualSalesMonthwiseModel.getRetailerPlannedAnnualSales()!=null && !annualSalesMonthwiseModel.getRetailerPlannedAnnualSales().isEmpty()) {
            for (RetailerPlannedAnnualSalesModel retailerPlannedAnnualSalesModel : annualSalesMonthwiseModel.getRetailerPlannedAnnualSales()) {
                if (annualSalesMonthwiseModel.getSubAreaMaster()!=null) {
                    if (annualSalesMonthwiseModel.getSubAreaMaster().equals(retailerPlannedAnnualSalesModel.getSubAreaMaster())) {
                        annualSalesMonthWiseTargetListData.setSubArea(subAreaMaster.getTaluka());
                        annualSalesMonthWiseTargetListData.setSubAreaId(subAreaMaster.getPk().toString());
                        AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData = new AnnualSalesMonthWiseTargetData();
                        annualSalesMonthWiseTargetData.setCustomerCode(retailerPlannedAnnualSalesModel.getCustomerCode());
                        annualSalesMonthWiseTargetData.setCustomerName(retailerPlannedAnnualSalesModel.getCustomerName());
                        annualSalesMonthWiseTargetData.setCustomerPotential(retailerPlannedAnnualSalesModel.getCustomerPotential()!=null ?retailerPlannedAnnualSalesModel.getCustomerPotential():0.0);
                        annualSalesMonthWiseTargetData.setTotalTarget(retailerPlannedAnnualSalesModel.getTotalTarget());
                        List<MonthWiseTargetData> monthWiseTargetDataList = new ArrayList<>();

                        if (retailerPlannedAnnualSalesModel.getMonthWiseAnnualTarget() != null &&
                                !retailerPlannedAnnualSalesModel.getMonthWiseAnnualTarget().isEmpty()) {
                            for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : retailerPlannedAnnualSalesModel.getMonthWiseAnnualTarget()) {
                                if (retailerPlannedAnnualSalesModel.getCustomerCode().equals(monthWiseAnnualTargetModel.getCustomerCode())) {
                                    MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                    monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                    monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                    monthWiseTargetDataList.add(monthWiseTargetData);
                                }
                            }
                        }
                        annualSalesMonthWiseTargetData.setMonthWiseTarget(monthWiseTargetDataList);

                        List<RetailerDetailsData> retailerDetailsDataList = new ArrayList<>();
                        if (retailerPlannedAnnualSalesModel.getListOfRetailersPlanned() != null && !retailerPlannedAnnualSalesModel.getListOfRetailersPlanned().isEmpty()) {
                            for (RetailerPlannedAnnualSalesDetailsModel retailerPlannedAnnualSalesDetailsModel : retailerPlannedAnnualSalesModel.getListOfRetailersPlanned()) {
                                RetailerDetailsData retailerDetailsData = new RetailerDetailsData();
                                retailerDetailsData.setCustomerCode(retailerPlannedAnnualSalesDetailsModel.getCustomerCode());
                                retailerDetailsData.setCustomerName(retailerPlannedAnnualSalesDetailsModel.getCustomerName());
                                retailerDetailsData.setCustomerPotential(retailerPlannedAnnualSalesDetailsModel.getCustomerPotential()!=null?retailerPlannedAnnualSalesDetailsModel.getCustomerPotential():0.0);
                                retailerDetailsData.setTotalTarget(retailerPlannedAnnualSalesDetailsModel.getTotalTarget());
                                List<MonthWiseTargetData> monthWiseTargetDataListforRetailer = new ArrayList<>();
                                for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : retailerPlannedAnnualSalesDetailsModel.getMonthWiseAnnualTarget()) {
                                    if (monthWiseAnnualTargetModel.getCustomerCode().equals(retailerPlannedAnnualSalesDetailsModel.getCustomerCode())) {
                                        MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                        monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                        monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                        monthWiseTargetDataListforRetailer.add(monthWiseTargetData);
                                    }
                                }
                                retailerDetailsData.setMonthWiseSkuTarget(monthWiseTargetDataListforRetailer);
                                retailerDetailsDataList.add(retailerDetailsData);
                            }
                            List<MonthWiseTargetData> monthWiseAnnualTargetModelsforSelf = new ArrayList<>();
                            SelfCounterSaleDetailsModel dealerSelfCounterSale = retailerPlannedAnnualSalesModel.getDealerSelfCounterSale();
                            SelfCounterSaleData selfCounterSaleData = new SelfCounterSaleData();
                            selfCounterSaleData.setCustomerCode(dealerSelfCounterSale.getCustomerCode());
                            selfCounterSaleData.setCustomerName(dealerSelfCounterSale.getCustomerName());
                            selfCounterSaleData.setCustomerPotential(dealerSelfCounterSale.getCustomerPotential()!=null ? dealerSelfCounterSale.getCustomerPotential():0.0);
                            selfCounterSaleData.setTotalTarget(dealerSelfCounterSale.getTotalTarget());
                            for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : dealerSelfCounterSale.getMonthWiseAnnualTarget()) {
                                if (monthWiseAnnualTargetModel.getCustomerCode().equals(selfCounterSaleData.getCustomerCode())) {
                                    MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                    monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                    monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                    monthWiseAnnualTargetModelsforSelf.add(monthWiseTargetData);
                                }
                            }
                            selfCounterSaleData.setMonthWiseTarget(monthWiseAnnualTargetModelsforSelf);
                            annualSalesMonthWiseTargetData.setSelfCounterSale(selfCounterSaleData);
                        }
                        annualSalesMonthWiseTargetData.setRetailerData(retailerDetailsDataList);
                        annualSalesMonthWiseTargetDataList.add(annualSalesMonthWiseTargetData);
                    }
                }
            }
            annualSalesMonthWiseTargetListData.setAnnualSalesMonthWiseTargetData(annualSalesMonthWiseTargetDataList);
        }
        return annualSalesMonthWiseTargetListData;
    }

    @Override
    public AnnualTargetSettingSummaryData viewAnnualSalesSummary(boolean isAnnualSummaryTargetSet, boolean isAnnualSummaryAfterTargetSetting, boolean isAnnualSummaryForReview, boolean isAnnualSummaryAfterReview) {

        SclUserModel sclUser = (SclUserModel) getUserService().getCurrentUser();
        BaseSiteModel baseSite = baseSiteService.getCurrentBaseSite();
        List<SubAreaMasterModel> subAreas = territoryManagementService.getTerritoriesForSO();
        if (CollectionUtils.isEmpty(subAreas)) {
            throw new ModelNotFoundException("No Subarea Attached to current user: " + sclUser.getUid());
        }
        AnnualTargetSettingSummaryData annualTargetSettingSummaryData = new AnnualTargetSettingSummaryData();
        if(subAreas!=null && !subAreas.isEmpty()){
            AnnualSalesModel annualSalesModel = salesPlanningService.getAnnualSalesModelDetailsForSH(findNextFinancialYear(), subAreas, baseSite);
            if(annualSalesModel!=null) {
                if(annualSalesModel.getRevisedTargetBySH()!=null && annualSalesModel.getRevisedByShEmpCode()!=null) {
                    annualTargetSettingSummaryData.setRevisedTargetBySH(annualSalesModel.getRevisedTargetBySH());
                    annualTargetSettingSummaryData.setRevisedTargetBySHEmpCode(annualSalesModel.getRevisedByShEmpCode());
                    annualTargetSettingSummaryData.setIsTopDownIndicatorOn(annualSalesModel.getIsTopDownIndicatorOn());
                }
            }
        }
        List<SubareaListData> subareaList = new ArrayList<>();
        List<String> dealerCategoryList = salesPlanningService.getDealerCategoryForSummaryPage();
        double sumOfQuantityForLastYear = 0.0;
        double sumOfDealerSaleForLastYear = 0.0;
        double currentYearSale = 0.0;
        double plannedYearSale = 0.0;
        for (SubAreaMasterModel subArea : subAreas) {
            List<AnnualTargetProductMixData> annualTargetProductMixDataList = new ArrayList<>();
            List<AnnualTargetDealerChannelMixData> annualTargetDealerChannelMixDataList = new ArrayList<>();
            SubareaListData subareaListData = new SubareaListData();
            subareaListData.setSubareaName(subArea.getTaluka());
            subareaListData.setSubAreaId(subArea.getPk().toString());

           if(isAnnualSummaryAfterTargetSetting)
            {
                currentYearSale = salesPlanningService.getCurrentYearSalesForAnnualSummary(sclUser, baseSite, subArea.getPk().toString());
                subareaListData.setCurrentYearSale(currentYearSale!=0.0?currentYearSale:0.0);

                plannedYearSale=salesPlanningService.getTotalTargetForDealersAfterTargetSetting(sclUser,subArea.getPk().toString(),findNextFinancialYear(),baseSite);
                subareaListData.setPlannedYearSale(plannedYearSale!=0.0?plannedYearSale:0.0);

                double finalSumOfQuantity=0.0;
                List<List<Object>> list = salesPlanningService.fetchProductSaleDetailsForSummaryAfterTargetSetting(subArea.getPk().toString(), sclUser,baseSite);
                if(list!=null && !list.isEmpty()) {
                    finalSumOfQuantity += list.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2-> (double) objects2.get(0)).sum();
                    double allProdSum = finalSumOfQuantity;
                    for (List<Object> objects : list) {
                        AnnualTargetProductMixData annualTargetProductMixData = new AnnualTargetProductMixData();
                        double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                        String skuName = (String) objects.get(1);
                        annualTargetProductMixData.setSkuName(skuName);
                        annualTargetProductMixData.setLastYearShare(0.0);
                        double targetProductMix = Math.round((target / allProdSum) * 100);
                        if (targetProductMix != 0.0)
                            annualTargetProductMixData.setTargetProductMixPercentage(targetProductMix);
                        else
                            annualTargetProductMixData.setTargetProductMixPercentage(0.0);
                        annualTargetProductMixDataList.add(annualTargetProductMixData);
                    }
                    subareaListData.setAnnualTargetProductMix(annualTargetProductMixDataList);
                }
                else {
                    List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(sclUser.getState());
                    if (productList != null && !productList.isEmpty()) {
                        for (String product : productList) {
                            AnnualTargetProductMixData annualTargetProductMixData = new AnnualTargetProductMixData();
                            annualTargetProductMixData.setLastYearShare(0.0);
                            annualTargetProductMixData.setTargetProductMixPercentage(0.0);
                            annualTargetProductMixData.setSkuName(product);
                            annualTargetProductMixDataList.add(annualTargetProductMixData);
                        }
                        subareaListData.setAnnualTargetProductMix(annualTargetProductMixDataList);
                    }
                }
                //dealer category channel mix
                double finalSumOfDealerQuantity=0.0;
                List<List<Object>> dealerSaleList = salesPlanningService.fetchDealerSaleDetailsForSummaryAfterTargetSetting(subArea.getPk().toString(), sclUser);
                if(dealerSaleList!=null && !dealerSaleList.isEmpty()) {
                    finalSumOfDealerQuantity += dealerSaleList.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2-> (double) objects2.get(0)).sum();
                    double allDealerCategorySum = finalSumOfDealerQuantity;
                    for (List<Object> objects : dealerSaleList) {
                        AnnualTargetDealerChannelMixData annualTargetDealerChannelMixData = new AnnualTargetDealerChannelMixData();
                        double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                        String dealerCategory = (String) objects.get(1);
                        annualTargetDealerChannelMixData.setDealerCategory(dealerCategory);
                        annualTargetDealerChannelMixData.setLastYearCounterSale(0.0);

                        double contributionPlanPercentage = Math.round((target / allDealerCategorySum) * 100);
                        if (contributionPlanPercentage != 0.0)
                            annualTargetDealerChannelMixData.setContributionPlanPercentage(contributionPlanPercentage);
                        else
                            annualTargetDealerChannelMixData.setContributionPlanPercentage(0.0);
                        annualTargetDealerChannelMixDataList.add(annualTargetDealerChannelMixData);
                    }
                    subareaListData.setAnnualTargetDealerChannelMix(annualTargetDealerChannelMixDataList);
                }
                else  {
                    for (String dealerCategory : dealerCategoryList) {
                        AnnualTargetDealerChannelMixData annualTargetDealerChannelMixData = new AnnualTargetDealerChannelMixData();
                        annualTargetDealerChannelMixData.setDealerCategory(dealerCategory);
                        annualTargetDealerChannelMixData.setContributionPlanPercentage(0.0);
                        annualTargetDealerChannelMixData.setLastYearCounterSale(0.0);
                        annualTargetDealerChannelMixDataList.add(annualTargetDealerChannelMixData);
                    }
                    subareaListData.setAnnualTargetDealerChannelMix(annualTargetDealerChannelMixDataList);
                }

                AnnualSalesModel annualSalesModel = salesPlanningService.viewPlannedSalesforDealersRetailersMonthWise(subArea.getPk().toString(),sclUser, baseSite);
                if(annualSalesModel!=null)
                {
                  /*  if(annualSalesModel.getRevisedByShEmpCode()!=null && annualSalesModel.getRevisedTargetBySH()!=null)
                    {
                        subareaListData.setIsTopDownIndicatorOn(true);
                        subareaListData.setRevisedTargetBySH(annualSalesModel.getRevisedTargetBySH());
                        subareaListData.setRevisedBy(annualSalesModel.getRevisedByShEmpCode());
                        subareaListData.setCommentsForRevision(annualSalesModel.getCommentsForRevision()!=null?annualSalesModel.getCommentsForRevision():"");
                    }else {*/
                        subareaListData.setIsTopDownIndicatorOn(false);
                        subareaListData.setRevisedTarget(annualSalesModel.getRevisedTarget() != null ? annualSalesModel.getRevisedTarget() : 0.0);
                        subareaListData.setRevisedBy(annualSalesModel.getRevisedBy() != null ? annualSalesModel.getRevisedBy().getUid() : "");
                        subareaListData.setCommentsForRevision(annualSalesModel.getCommentsForRevision() != null ? annualSalesModel.getCommentsForRevision() : "");
                    //}
                }
            }
            else if (isAnnualSummaryForReview)
            {
                currentYearSale = salesPlanningService.getCurrentYearSalesForAnnualSummary(sclUser, baseSite, subArea.getPk().toString());
                subareaListData.setCurrentYearSale(currentYearSale!=0.0?currentYearSale:0.0);

                plannedYearSale=salesPlanningService.getTotalTargetForDealersAfterTargetSetting(sclUser,subArea.getPk().toString(),findNextFinancialYear(), baseSite);
                subareaListData.setPlannedYearSale(plannedYearSale!=0.0?plannedYearSale:0.0);

                double finalSumOfQuantity=0.0;
                List<List<Object>> list = salesPlanningService.fetchProductSaleDetailsForSummaryAfterTargetSetting(subArea.getPk().toString(), sclUser, baseSite);
                if(list!=null && !list.isEmpty()) {
                    finalSumOfQuantity += list.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2-> (double) objects2.get(0)).sum();
                    double allProdSum = finalSumOfQuantity;
                    for (List<Object> objects : list) {
                        AnnualTargetProductMixData annualTargetProductMixData = new AnnualTargetProductMixData();
                        double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                        String skuName = (String) objects.get(1);
                        annualTargetProductMixData.setSkuName(skuName);
                        annualTargetProductMixData.setLastYearShare(0.0);
                        double targetProductMix = Math.round((target / allProdSum) * 100);
                        if (targetProductMix != 0.0)
                            annualTargetProductMixData.setTargetProductMixPercentage(targetProductMix);
                        else
                            annualTargetProductMixData.setTargetProductMixPercentage(0.0);
                        annualTargetProductMixDataList.add(annualTargetProductMixData);
                    }
                    subareaListData.setAnnualTargetProductMix(annualTargetProductMixDataList);
                }
                else {
                    List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(sclUser.getState());
                    if (productList != null && !productList.isEmpty()) {
                        for (String product : productList) {
                            AnnualTargetProductMixData annualTargetProductMixData = new AnnualTargetProductMixData();
                            annualTargetProductMixData.setLastYearShare(0.0);
                            annualTargetProductMixData.setTargetProductMixPercentage(0.0);
                            annualTargetProductMixData.setSkuName(product);
                            annualTargetProductMixDataList.add(annualTargetProductMixData);
                        }
                        subareaListData.setAnnualTargetProductMix(annualTargetProductMixDataList);
                    }
                }

                //dealer category channel mix
                double finalSumOfDealerQuantity=0.0;
                List<List<Object>> dealerSaleList = salesPlanningService.fetchDealerSaleDetailsForSummaryAfterTargetSetting(subArea.getPk().toString(), sclUser);
                if(dealerSaleList!=null && !dealerSaleList.isEmpty()) {
                    finalSumOfDealerQuantity += dealerSaleList.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2-> (double) objects2.get(0)).sum();
                    double allDealerCategorySum = finalSumOfDealerQuantity;
                    for (List<Object> objects : dealerSaleList) {
                        AnnualTargetDealerChannelMixData annualTargetDealerChannelMixData = new AnnualTargetDealerChannelMixData();
                        double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                        String dealerCategory = (String) objects.get(1);
                        annualTargetDealerChannelMixData.setDealerCategory(dealerCategory);
                        annualTargetDealerChannelMixData.setLastYearCounterSale(0.0);

                        double contributionPlanPercentage = Math.round((target / allDealerCategorySum) * 100);
                        if (contributionPlanPercentage != 0.0)
                            annualTargetDealerChannelMixData.setContributionPlanPercentage(contributionPlanPercentage);
                        else
                            annualTargetDealerChannelMixData.setContributionPlanPercentage(0.0);
                        annualTargetDealerChannelMixDataList.add(annualTargetDealerChannelMixData);
                    }
                    subareaListData.setAnnualTargetDealerChannelMix(annualTargetDealerChannelMixDataList);
                }
                else  {
                    for (String dealerCategory : dealerCategoryList) {
                        AnnualTargetDealerChannelMixData annualTargetDealerChannelMixData = new AnnualTargetDealerChannelMixData();
                        annualTargetDealerChannelMixData.setDealerCategory(dealerCategory);
                        annualTargetDealerChannelMixData.setContributionPlanPercentage(0.0);
                        annualTargetDealerChannelMixData.setLastYearCounterSale(0.0);
                        annualTargetDealerChannelMixDataList.add(annualTargetDealerChannelMixData);
                    }
                    subareaListData.setAnnualTargetDealerChannelMix(annualTargetDealerChannelMixDataList);
                }
            }
            else if (isAnnualSummaryAfterReview)
            {
                currentYearSale = salesPlanningService.getCurrentYearSalesForAnnualSummary(sclUser, baseSite, subArea.getPk().toString());
                subareaListData.setCurrentYearSale(currentYearSale!=0.0?currentYearSale:0.0);

                plannedYearSale=salesPlanningService.getTotalTargetForDealersAfterReview(sclUser,subArea.getPk().toString(),findNextFinancialYear(),baseSite);
                subareaListData.setPlannedYearSale(plannedYearSale!=0.0?plannedYearSale:0.0);
                double finalSumOfQuantity=0.0;
                List<List<Object>> list = salesPlanningService.fetchProductSaleDetailsForSummaryAfterReview(subArea.getPk().toString(), sclUser, baseSite);
                if(list!=null && !list.isEmpty()) {
                    finalSumOfQuantity += list.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2-> (double) objects2.get(0)).sum();
                    double allProdSum = finalSumOfQuantity;
                    for (List<Object> objects : list) {
                        AnnualTargetProductMixData annualTargetProductMixData = new AnnualTargetProductMixData();
                        double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                        String skuName = (String) objects.get(1);
                        annualTargetProductMixData.setSkuName(skuName);
                        annualTargetProductMixData.setLastYearShare(0.0);
                        double targetProductMix = Math.round((target / allProdSum) * 100);
                        if (targetProductMix != 0.0)
                            annualTargetProductMixData.setTargetProductMixPercentage(targetProductMix);
                        else
                            annualTargetProductMixData.setTargetProductMixPercentage(0.0);
                        annualTargetProductMixDataList.add(annualTargetProductMixData);
                    }
                    subareaListData.setAnnualTargetProductMix(annualTargetProductMixDataList);
                }
                else {
                    List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(sclUser.getState());
                    if (productList != null && !productList.isEmpty()) {
                        for (String product : productList) {
                            AnnualTargetProductMixData annualTargetProductMixData = new AnnualTargetProductMixData();
                            annualTargetProductMixData.setLastYearShare(0.0);
                            annualTargetProductMixData.setTargetProductMixPercentage(0.0);
                            annualTargetProductMixData.setSkuName(product);
                            annualTargetProductMixDataList.add(annualTargetProductMixData);
                        }
                        subareaListData.setAnnualTargetProductMix(annualTargetProductMixDataList);
                    }
                }

                //dealer category channel mix
                double finalSumOfDealerQuantity=0.0;
                List<List<Object>> dealerSaleList = salesPlanningService.fetchDealerSaleDetailsForSummaryForSummaryAfterReview(subArea.getPk().toString(), sclUser, baseSite);
                if(dealerSaleList!=null && !dealerSaleList.isEmpty()) {
                    finalSumOfDealerQuantity += dealerSaleList.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2-> (double) objects2.get(0)).sum();
                    double allDealerCategorySum = finalSumOfDealerQuantity;
                    for (List<Object> objects : dealerSaleList) {
                        AnnualTargetDealerChannelMixData annualTargetDealerChannelMixData = new AnnualTargetDealerChannelMixData();
                        double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                        String dealerCategory = (String) objects.get(1);
                        annualTargetDealerChannelMixData.setDealerCategory(dealerCategory);
                        annualTargetDealerChannelMixData.setLastYearCounterSale(0.0);

                        double contributionPlanPercentage = Math.round((target / allDealerCategorySum) * 100);
                        if (contributionPlanPercentage != 0.0)
                            annualTargetDealerChannelMixData.setContributionPlanPercentage(contributionPlanPercentage);
                        else
                            annualTargetDealerChannelMixData.setContributionPlanPercentage(0.0);
                        annualTargetDealerChannelMixDataList.add(annualTargetDealerChannelMixData);
                    }
                    subareaListData.setAnnualTargetDealerChannelMix(annualTargetDealerChannelMixDataList);
                }
                else  {
                    for (String dealerCategory : dealerCategoryList) {
                        AnnualTargetDealerChannelMixData annualTargetDealerChannelMixData = new AnnualTargetDealerChannelMixData();
                        annualTargetDealerChannelMixData.setDealerCategory(dealerCategory);
                        annualTargetDealerChannelMixData.setContributionPlanPercentage(0.0);
                        annualTargetDealerChannelMixData.setLastYearCounterSale(0.0);
                        annualTargetDealerChannelMixDataList.add(annualTargetDealerChannelMixData);
                    }
                    subareaListData.setAnnualTargetDealerChannelMix(annualTargetDealerChannelMixDataList);
                }
            }
            else
           {
               //before target set scenario 1
               LOG.info("getCurrentYearSalesForAnnualSummary facade" + "scluser pk :" + sclUser + "baseSite pk :" + baseSite + "subArea pk :" + subArea.getPk().toString());
               currentYearSale = salesPlanningService.getCurrentYearSalesForAnnualSummary(sclUser, baseSite, subArea.getPk().toString());
               LOG.info("currentYearSale facade ::" + currentYearSale);
               subareaListData.setCurrentYearSale(currentYearSale!=0.0?currentYearSale:0.0);
               LOG.info("after setting cy sale in to subarea list ::" + subareaListData.getCurrentYearSale());
               subareaListData.setPlannedYearSale(0.0);

               List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(sclUser.getState());
                   if (productList != null && !productList.isEmpty()) {
                       for (String product : productList) {
                           AnnualTargetProductMixData annualTargetProductMixData = new AnnualTargetProductMixData();

                        //List<List<Object>> lastYearShareForProductAnnual = salesPlanningService.getLastYearShareForProductFromNCR(subArea.getPk().toString());
                           /*List<List<Object>> lastYearShareForProductAnnual = salesPlanningService.getLastYearShareForProduct(sclUser,baseSite,subArea.getPk().toString());
                           if(lastYearShareForProductAnnual!=null && !lastYearShareForProductAnnual.isEmpty())
                        {
                            for (List<Object> objectList : lastYearShareForProductAnnual) {
                                String productCode = (String) objectList.get(0);
                                String productName = (String) objectList.get(1);
                                double pquantityInMT = (Double) objectList.get(2)!=0.0? (Double) objectList.get(2) : 0.0;
                                sumOfQuantityForLastYear += pquantityInMT;
                                double lastYearShareSum = sumOfQuantityForLastYear;
                                double lastYearShare = (pquantityInMT / lastYearShareSum) * 100;
                                LOG.info("ProductName from ncr and product from state is equal" + productName.equals(product));
                                if(productName.equals(product)) {
                                    if (lastYearShare != 0.0)
                                        annualTargetProductMixData.setLastYearShare(lastYearShare);
                                    else
                                        annualTargetProductMixData.setLastYearShare(0.0);
                                }
                                else
                                    LOG.info("ProductName from ncr and product from state is not equal");
                                    annualTargetProductMixData.setLastYearShare(0.0);
                            }
                        }
                        else*/
                           annualTargetProductMixData.setLastYearShare(0.0);
                           annualTargetProductMixData.setTargetProductMixPercentage(0.0);
                           annualTargetProductMixData.setSkuName(product);
                           annualTargetProductMixDataList.add(annualTargetProductMixData);
                       }
                       subareaListData.setAnnualTargetProductMix(annualTargetProductMixDataList);
                   }
               if (dealerCategoryList != null && !dealerCategoryList.isEmpty()) {
                   for (String dealerCategory : dealerCategoryList) {
                       AnnualTargetDealerChannelMixData annualTargetDealerChannelMixData = new AnnualTargetDealerChannelMixData();
                       annualTargetDealerChannelMixData.setDealerCategory(dealerCategory);
                       annualTargetDealerChannelMixData.setContributionPlanPercentage(0.0);

                      // List<List<Object>> lastYearShareTargetForDealerAnnual=salesPlanningService.getLastYearShareForDealerFromNCRAnnual(subArea.getPk().toString());
                       /*List<List<Object>> lastYearShareTargetForDealerAnnual=salesPlanningService.getLastYearShareForTarget(sclUser,baseSite,subArea.getPk().toString());
                       if(lastYearShareTargetForDealerAnnual!=null && !lastYearShareTargetForDealerAnnual.isEmpty()) {
                           for (List<Object> objects : lastYearShareTargetForDealerAnnual) {
                            String category= (String) objects.get(0);
                            double dealerSale= (Double) objects.get(1);
                            sumOfDealerSaleForLastYear += dealerSale;
                            double lastYearShareSumForDealer = sumOfDealerSaleForLastYear;
                            double lastYearCounterShareForDealer = (dealerSale / lastYearShareSumForDealer) * 100;
                            if(category.equals(dealerCategory))
                            {
                                if(lastYearCounterShareForDealer!=0.0)
                                {
                                    annualTargetDealerChannelMixData.setLastYearCounterSale(lastYearCounterShareForDealer);
                                }
                                else
                                    annualTargetDealerChannelMixData.setLastYearCounterSale(0.0);
                            }
                            else
                                annualTargetDealerChannelMixData.setLastYearCounterSale(0.0);
                           }
                       }
                       else*/
                           annualTargetDealerChannelMixData.setLastYearCounterSale(0.0);
                       annualTargetDealerChannelMixDataList.add(annualTargetDealerChannelMixData);
                   }
                   subareaListData.setAnnualTargetDealerChannelMix(annualTargetDealerChannelMixDataList);
               }
           }
            subareaList.add(subareaListData);
        }
        annualTargetSettingSummaryData.setSubareaList(subareaList);
        annualTargetSettingSummaryData.setPlannedYear(findNextFinancialYear());
        annualTargetSettingSummaryData.setTotalCurrentYearSales(0.0);
        annualTargetSettingSummaryData.setTotalPlannedYearSales(0.0);
        return annualTargetSettingSummaryData;
    }

    @Override
    public AnnualTargetSettingSummaryData viewAnnualSalesSummaryTSM(boolean isAnnualSummaryForReview, boolean isAnnualSummaryAfterReview) {
        {
            SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
            Collection<DistrictMasterModel> currentDistrict = territoryManagementService.getCurrentDistrict();

            BaseSiteModel baseSite = baseSiteService.getCurrentBaseSite();
            FilterTalukaData filterTalukaData = new FilterTalukaData();
            List<SubAreaMasterModel> taulkaForUser = territoryManagementService.getTaulkaForUser(filterTalukaData);
            AnnualTargetSettingSummaryData annualTargetSettingSummaryData = new AnnualTargetSettingSummaryData();
            if(taulkaForUser!=null && !taulkaForUser.isEmpty()){
                AnnualSalesModel annualSalesModel = salesPlanningService.getAnnualSalesModelDetailsForSH(findNextFinancialYear(), taulkaForUser, baseSite);
                if(annualSalesModel!=null) {
                    if (annualSalesModel.getRevisedTargetBySH() != null && annualSalesModel.getRevisedByShEmpCode() != null) {
                        annualTargetSettingSummaryData.setRevisedTargetBySH(annualSalesModel.getRevisedTargetBySH());
                        annualTargetSettingSummaryData.setRevisedTargetBySHEmpCode(annualSalesModel.getRevisedByShEmpCode());
                        annualTargetSettingSummaryData.setIsTopDownIndicatorOn(annualSalesModel.getIsTopDownIndicatorOn());
                    }
                }
            }

            SummaryMonthlySubareaListData summaryMonthlySubareaListData=new SummaryMonthlySubareaListData();
            List<SumMonthlyTargetProductMixData> sumMonthlyTargetProductMixDataList=new ArrayList<>();
            List<SubareaListData> subareaList = new ArrayList<>();
            List<String> dealerCategoryList = salesPlanningService.getDealerCategoryForSummaryPage();
            double currentYearSale = 0.0;
            double sumCurrentYearSale=0.0;
            double plannedYearSale = 0.0;
            double sumPlannedYearSale=0.0;
            if(currentUser.getUserType().getCode().equalsIgnoreCase("TSM")) {
                List<List<Object>> list1=new ArrayList<>();
                double finalSumOfQuantity1 = 0.0;
                 if (isAnnualSummaryForReview) {
                     if(taulkaForUser!=null && !taulkaForUser.isEmpty()) {
                         list1 = salesPlanningService.fetchProductSaleDetailsForSummaryAfterTargetSettingForTSM(taulkaForUser,baseSite);
                     }
                }
                 else if (isAnnualSummaryAfterReview) {
                     if(taulkaForUser!=null && !taulkaForUser.isEmpty()) {
                         list1 = salesPlanningService.fetchProductSaleDetailsForSummaryAfterReviewForTSM(taulkaForUser,baseSite);
                     }
                }
                        if (list1 != null && !list1.isEmpty()) {
                            finalSumOfQuantity1 += list1.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2 -> (double) objects2.get(0)).sum();
                            double allProdSum = finalSumOfQuantity1;
                            for (List<Object> objects : list1) {
                                SumMonthlyTargetProductMixData annualTargetProductMixData = new SumMonthlyTargetProductMixData();
                                double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                                String skuName = (String) objects.get(1);
                                annualTargetProductMixData.setSkuName(skuName);
                                annualTargetProductMixData.setLastYearShare(0.0);
                                double targetProductMix = Math.round((target / allProdSum) * 100);
                                if (targetProductMix != 0.0)
                                    annualTargetProductMixData.setTargetProductMixPercentage(targetProductMix);
                                else
                                    annualTargetProductMixData.setTargetProductMixPercentage(0.0);
                                sumMonthlyTargetProductMixDataList.add(annualTargetProductMixData);
                            }
                        } else {
                            //check how to get state here
                            List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(currentUser.getState());
                            if (productList != null && !productList.isEmpty()) {
                                for (String product : productList) {
                                    SumMonthlyTargetProductMixData annualTargetProductMixData = new SumMonthlyTargetProductMixData();
                                    annualTargetProductMixData.setLastYearShare(0.0);
                                    annualTargetProductMixData.setTargetProductMixPercentage(0.0);
                                    annualTargetProductMixData.setSkuName(product);
                                    sumMonthlyTargetProductMixDataList.add(annualTargetProductMixData);
                                }
                            }
                        }
                    if (taulkaForUser != null && !taulkaForUser.isEmpty()) {
                            for (SubAreaMasterModel subArea : taulkaForUser) {
                                SclUserModel sclUser = territoryManagementService.getSalesOfficerforTaluka(subArea.getTaluka(), baseSite);
                                LOG.info(subArea.getPk().toString());
                                LOG.info(sclUser);
                                    List<AnnualTargetProductMixData> annualTargetProductMixDataList = new ArrayList<>();
                                    List<AnnualTargetDealerChannelMixData> annualTargetDealerChannelMixDataList = new ArrayList<>();
                                    SubareaListData subareaListData = new SubareaListData();
                                    subareaListData.setSubareaName(subArea.getTaluka());
                                    subareaListData.setSubAreaId(subArea.getPk().toString());

                                    if (isAnnualSummaryForReview) {
                                        currentYearSale = salesPlanningService.getCurrentYearSalesForAnnualSummaryNew(baseSite, subArea.getPk().toString()); //pass scluser for new sales changes
                                        LOG.info("current YEar sale from facade:"+currentYearSale);
                                        subareaListData.setCurrentYearSale(currentYearSale != 0.0 ? currentYearSale : 0.0);

                                        plannedYearSale = salesPlanningService.getTotalTargetForDealersAfterTargetSetting(sclUser, subArea.getPk().toString(), findNextFinancialYear(), baseSite);
                                        subareaListData.setPlannedYearSale(plannedYearSale != 0.0 ? plannedYearSale : 0.0);
                                        LOG.info("Planned YEar sale from facade:"+currentYearSale);
/*
                                        sumCurrentYearSale+=currentYearSale;
                                        sumPlannedYearSale+=plannedYearSale;
*/

                                        double finalSumOfQuantity = 0.0;
                                        List<List<Object>> list = salesPlanningService.fetchProductSaleDetailsForSummaryAfterTargetSetting(subArea.getPk().toString(), sclUser, baseSite);
                                        if (list != null && !list.isEmpty()) {
                                            finalSumOfQuantity += list.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2 -> (double) objects2.get(0)).sum();
                                            double allProdSum = finalSumOfQuantity;
                                            for (List<Object> objects : list) {
                                                AnnualTargetProductMixData annualTargetProductMixData = new AnnualTargetProductMixData();
                                                double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                                                String skuName = (String) objects.get(1);
                                                annualTargetProductMixData.setSkuName(skuName);
                                                annualTargetProductMixData.setLastYearShare(0.0);
                                                double targetProductMix = Math.round((target / allProdSum) * 100);
                                                if (targetProductMix != 0.0)
                                                    annualTargetProductMixData.setTargetProductMixPercentage(targetProductMix);
                                                else
                                                    annualTargetProductMixData.setTargetProductMixPercentage(0.0);
                                                annualTargetProductMixDataList.add(annualTargetProductMixData);
                                            }
                                            subareaListData.setAnnualTargetProductMix(annualTargetProductMixDataList);
                                        } else {
                                            List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(sclUser.getState());
                                            if (productList != null && !productList.isEmpty()) {
                                                for (String product : productList) {
                                                    AnnualTargetProductMixData annualTargetProductMixData = new AnnualTargetProductMixData();
                                                    annualTargetProductMixData.setLastYearShare(0.0);
                                                    annualTargetProductMixData.setTargetProductMixPercentage(0.0);
                                                    annualTargetProductMixData.setSkuName(product);
                                                    annualTargetProductMixDataList.add(annualTargetProductMixData);
                                                }
                                                subareaListData.setAnnualTargetProductMix(annualTargetProductMixDataList);
                                            }
                                        }

                                        //dealer category channel mix
                                        double finalSumOfDealerQuantity = 0.0;
                                        List<List<Object>> dealerSaleList = salesPlanningService.fetchDealerSaleDetailsForSummaryAfterTargetSetting(subArea.getPk().toString(), sclUser);
                                        if (dealerSaleList != null && !dealerSaleList.isEmpty()) {
                                            finalSumOfDealerQuantity += dealerSaleList.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2 -> (double) objects2.get(0)).sum();
                                            double allDealerCategorySum = finalSumOfDealerQuantity;
                                            for (List<Object> objects : dealerSaleList) {
                                                AnnualTargetDealerChannelMixData annualTargetDealerChannelMixData = new AnnualTargetDealerChannelMixData();
                                                double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                                                String dealerCategory = (String) objects.get(1);
                                                annualTargetDealerChannelMixData.setDealerCategory(dealerCategory);
                                                annualTargetDealerChannelMixData.setLastYearCounterSale(0.0);

                                                double contributionPlanPercentage = Math.round((target / allDealerCategorySum) * 100);
                                                if (contributionPlanPercentage != 0.0)
                                                    annualTargetDealerChannelMixData.setContributionPlanPercentage(contributionPlanPercentage);
                                                else
                                                    annualTargetDealerChannelMixData.setContributionPlanPercentage(0.0);
                                                annualTargetDealerChannelMixDataList.add(annualTargetDealerChannelMixData);
                                            }
                                            subareaListData.setAnnualTargetDealerChannelMix(annualTargetDealerChannelMixDataList);
                                        } else {
                                            for (String dealerCategory : dealerCategoryList) {
                                                AnnualTargetDealerChannelMixData annualTargetDealerChannelMixData = new AnnualTargetDealerChannelMixData();
                                                annualTargetDealerChannelMixData.setDealerCategory(dealerCategory);
                                                annualTargetDealerChannelMixData.setContributionPlanPercentage(0.0);
                                                annualTargetDealerChannelMixData.setLastYearCounterSale(0.0);
                                                annualTargetDealerChannelMixDataList.add(annualTargetDealerChannelMixData);
                                            }
                                            subareaListData.setAnnualTargetDealerChannelMix(annualTargetDealerChannelMixDataList);
                                            if (currentDistrict != null && !currentDistrict.isEmpty()) {
                                                for (DistrictMasterModel districtMasterModel : currentDistrict) {
                                                    subareaListData.setDistrictCode(districtMasterModel.getCode());
                                                    subareaListData.setDistrictName(districtMasterModel.getName());
                                                    AnnualSalesModel annualSalesModelDetailsForTSM = salesPlanningDao.getAnnualSalesModelDetailsForTSM(findNextFinancialYear(), districtMasterModel, baseSiteService.getCurrentBaseSite());
                                                    if (annualSalesModelDetailsForTSM != null) {
                                                        if (annualSalesModelDetailsForTSM.getActionPerformedBy()!=null && annualSalesModelDetailsForTSM.getActionPerformedBy().equals(currentUser) && annualSalesModelDetailsForTSM.getActionPerformed()!=null && annualSalesModelDetailsForTSM.getActionPerformed().equals(WorkflowActions.APPROVED)){
                                                            subareaListData.setIsTargetApproved(true);
                                                        }
                                                        else{
                                                            subareaListData.setIsTargetApproved(false);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else if (isAnnualSummaryAfterReview) {
                                        currentYearSale = salesPlanningService.getCurrentYearSalesForAnnualSummaryNew(baseSite, subArea.getPk().toString());
                                        subareaListData.setCurrentYearSale(currentYearSale != 0.0 ? currentYearSale : 0.0);
                                        LOG.info("current YEar sale from facade:"+currentYearSale);
                                        plannedYearSale = salesPlanningService.getTotalTargetForDealersAfterReview(sclUser, subArea.getPk().toString(), findNextFinancialYear(), baseSite);
                                        subareaListData.setPlannedYearSale(plannedYearSale != 0.0 ? plannedYearSale : 0.0);
                                        LOG.info("Planned YEar sale from facade:"+plannedYearSale);
                                       /* sumCurrentYearSale+=currentYearSale;
                                        sumPlannedYearSale+=plannedYearSale;
*/
                                        double finalSumOfQuantity = 0.0;
                                        List<List<Object>> list = salesPlanningService.fetchProductSaleDetailsForSummaryAfterReview(subArea.getPk().toString(), sclUser, baseSite);
                                        if (list != null && !list.isEmpty()) {
                                            finalSumOfQuantity += list.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2 -> (double) objects2.get(0)).sum();
                                            double allProdSum = finalSumOfQuantity;
                                            for (List<Object> objects : list) {
                                                AnnualTargetProductMixData annualTargetProductMixData = new AnnualTargetProductMixData();
                                                double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                                                String skuName = (String) objects.get(1);
                                                annualTargetProductMixData.setSkuName(skuName);
                                                annualTargetProductMixData.setLastYearShare(0.0);
                                                double targetProductMix = Math.round((target / allProdSum) * 100);
                                                if (targetProductMix != 0.0)
                                                    annualTargetProductMixData.setTargetProductMixPercentage(targetProductMix);
                                                else
                                                    annualTargetProductMixData.setTargetProductMixPercentage(0.0);
                                                annualTargetProductMixDataList.add(annualTargetProductMixData);
                                            }
                                            subareaListData.setAnnualTargetProductMix(annualTargetProductMixDataList);
                                        } else {
                                            List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(sclUser.getState());
                                            if (productList != null && !productList.isEmpty()) {
                                                for (String product : productList) {
                                                    AnnualTargetProductMixData annualTargetProductMixData = new AnnualTargetProductMixData();
                                                    annualTargetProductMixData.setLastYearShare(0.0);
                                                    annualTargetProductMixData.setTargetProductMixPercentage(0.0);
                                                    annualTargetProductMixData.setSkuName(product);
                                                    annualTargetProductMixDataList.add(annualTargetProductMixData);
                                                }
                                                subareaListData.setAnnualTargetProductMix(annualTargetProductMixDataList);
                                            }
                                        }

                                        //dealer category channel mix
                                        double finalSumOfDealerQuantity = 0.0;
                                        List<List<Object>> dealerSaleList = salesPlanningService.fetchDealerSaleDetailsForSummaryForSummaryAfterReview(subArea.getPk().toString(), sclUser, baseSite);
                                        if (dealerSaleList != null && !dealerSaleList.isEmpty()) {
                                            finalSumOfDealerQuantity += dealerSaleList.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2 -> (double) objects2.get(0)).sum();
                                            double allDealerCategorySum = finalSumOfDealerQuantity;
                                            for (List<Object> objects : dealerSaleList) {
                                                AnnualTargetDealerChannelMixData annualTargetDealerChannelMixData = new AnnualTargetDealerChannelMixData();
                                                double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                                                String dealerCategory = (String) objects.get(1);
                                                annualTargetDealerChannelMixData.setDealerCategory(dealerCategory);
                                                annualTargetDealerChannelMixData.setLastYearCounterSale(0.0);

                                                double contributionPlanPercentage = Math.round((target / allDealerCategorySum) * 100);
                                                if (contributionPlanPercentage != 0.0)
                                                    annualTargetDealerChannelMixData.setContributionPlanPercentage(contributionPlanPercentage);
                                                else
                                                    annualTargetDealerChannelMixData.setContributionPlanPercentage(0.0);
                                                annualTargetDealerChannelMixDataList.add(annualTargetDealerChannelMixData);
                                            }
                                            subareaListData.setAnnualTargetDealerChannelMix(annualTargetDealerChannelMixDataList);
                                        } else {
                                            for (String dealerCategory : dealerCategoryList) {
                                                AnnualTargetDealerChannelMixData annualTargetDealerChannelMixData = new AnnualTargetDealerChannelMixData();
                                                annualTargetDealerChannelMixData.setDealerCategory(dealerCategory);
                                                annualTargetDealerChannelMixData.setContributionPlanPercentage(0.0);
                                                annualTargetDealerChannelMixData.setLastYearCounterSale(0.0);
                                                annualTargetDealerChannelMixDataList.add(annualTargetDealerChannelMixData);
                                            }
                                            subareaListData.setAnnualTargetDealerChannelMix(annualTargetDealerChannelMixDataList);
                                            if (currentDistrict != null && !currentDistrict.isEmpty()) {
                                                for (DistrictMasterModel districtMasterModel : currentDistrict) {
                                                    subareaListData.setDistrictCode(districtMasterModel.getCode());
                                                    subareaListData.setDistrictName(districtMasterModel.getName());
                                                    AnnualSalesModel annualSalesModelDetailsForTSM = salesPlanningService.getAnnualSalesModelDetailsForTSM(findNextFinancialYear(), districtMasterModel, baseSiteService.getCurrentBaseSite());
                                                    if (annualSalesModelDetailsForTSM != null) {
                                                        if (annualSalesModelDetailsForTSM.getActionPerformedBy()!=null && annualSalesModelDetailsForTSM.getActionPerformedBy().equals(currentUser) && annualSalesModelDetailsForTSM.getActionPerformed()!=null && annualSalesModelDetailsForTSM.getActionPerformed().equals(WorkflowActions.APPROVED)) {
                                                            subareaListData.setIsTargetApproved(true);
                                                        }
                                                        else{
                                                            subareaListData.setIsTargetApproved(false);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    subareaList.add(subareaListData);
                                }
                        }
                }
            DistrictMasterModel districtMaster=null;
            if (currentDistrict != null && !currentDistrict.isEmpty()){
                for (DistrictMasterModel districtMasterModel : currentDistrict) {
                    districtMaster=districtMasterModel;
                    summaryMonthlySubareaListData.setDistrictCode(districtMasterModel.getCode());
                    summaryMonthlySubareaListData.setDistrictName(districtMasterModel.getName());
                }
            }
            else{
                summaryMonthlySubareaListData.setDistrictCode("");
                summaryMonthlySubareaListData.setDistrictName("");
            }
            if(districtMaster!=null) {
                AnnualSalesModel annualSalesModel = salesPlanningService.getAnnualSalesModelDetailsForTSM(findNextFinancialYear(), districtMaster, baseSiteService.getCurrentBaseSite());
                if (annualSalesModel != null) {
                   /* if(annualSalesModel.getRevisedByShEmpCode()!=null && annualSalesModel.getRevisedTargetBySH()!=null)
                    {
                        annualTargetSettingSummaryData.setIsTopDownIndicatorOn(true);
                        annualTargetSettingSummaryData.setRevisedTargetBySH(annualSalesModel.getRevisedTargetBySH());
                        annualTargetSettingSummaryData.setRevisedBy(annualSalesModel.getRevisedByShEmpCode());
                        annualTargetSettingSummaryData.setCommentsForRevision(annualSalesModel.getCommentsForRevision()!=null?annualSalesModel.getCommentsForRevision():"");
                    }else {*/
                        annualTargetSettingSummaryData.setIsTopDownIndicatorOn(false);
                        annualTargetSettingSummaryData.setRevisedTarget(annualSalesModel.getRevisedTarget() != null ? annualSalesModel.getRevisedTarget() : 0.0);
                        annualTargetSettingSummaryData.setRevisedBy(annualSalesModel.getRevisedBy() != null ? annualSalesModel.getRevisedBy().getUid() : "");
                        annualTargetSettingSummaryData.setCommentsForRevision(annualSalesModel.getCommentsForRevision() != null ? annualSalesModel.getCommentsForRevision() : "");
                    //}
                }
            }
            //add null check
            if(subareaList!=null && !subareaList.isEmpty()) {
                for (SubareaListData subareaListData : subareaList) {
                    if(subareaListData.getIsTargetApproved()!=null) {
                        if (subareaListData.getIsTargetApproved().equals(Boolean.TRUE)) {
                            annualTargetSettingSummaryData.setIsTargetSetForUser(Boolean.TRUE);
                        } else {
                            annualTargetSettingSummaryData.setIsTargetSetForUser(Boolean.FALSE);
                        }
                    }
                }
            }
            if(subareaList!=null && !subareaList.isEmpty()) {
                for (SubareaListData subareaListData : subareaList) {
                     sumCurrentYearSale += subareaListData.getCurrentYearSale();
                     sumPlannedYearSale+=subareaListData.getPlannedYearSale();
                }
            }
            summaryMonthlySubareaListData.setPlannedMonthSale(sumPlannedYearSale);
            summaryMonthlySubareaListData.setRevisedMonthSale(sumCurrentYearSale);
            summaryMonthlySubareaListData.setSumMonthlyTargetProductMix(sumMonthlyTargetProductMixDataList);
            annualTargetSettingSummaryData.setSummaryMonthlySubareaListData(summaryMonthlySubareaListData);
            annualTargetSettingSummaryData.setSubareaList(subareaList);
            annualTargetSettingSummaryData.setPlannedYear(findNextFinancialYear());
            annualTargetSettingSummaryData.setTotalCurrentYearSales(sumCurrentYearSale);
            annualTargetSettingSummaryData.setTotalPlannedYearSales(sumPlannedYearSale);
            annualTargetSettingSummaryData.setTsmUid(currentUser.getUid());
            if(currentDistrict!=null && !currentDistrict.isEmpty()) {
                for (DistrictMasterModel districtMasterModel : currentDistrict) {
                    annualTargetSettingSummaryData.setDistrictName(districtMasterModel.getName());
                    annualTargetSettingSummaryData.setDistrictCode(districtMasterModel.getCode());
                }
            }
            return annualTargetSettingSummaryData;
        }
    }

    @Override
    public AnnualTargetSettingSummaryData viewAnnualSalesSummaryRH(boolean isAnnualSummaryForReview, boolean isAnnualSummaryAfterReview) {
            SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
        final Collection<RegionMasterModel> currentRegion = territoryManagementService.getCurrentRegion();
        SclUserModel sclUser = null;
            BaseSiteModel baseSite = baseSiteService.getCurrentBaseSite();
            FilterDistrictData filterDistrictData = new FilterDistrictData();
            FilterTalukaData filterTalukaData = new FilterTalukaData();
            List<DistrictMasterModel> districtMasterModels = new ArrayList<>();
            List<DistrictMasterModel> districtForUser = territoryManagementService.getDistrictForUser(filterDistrictData);
            List<SumMonthlyTargetProductMixData> sumMonthlyTargetProductMixDataList=new ArrayList<>();
            AnnualTargetSettingSummaryData annualTargetSettingSummaryData = new AnnualTargetSettingSummaryData();
        if(districtForUser!=null && !districtForUser.isEmpty()){
            AnnualSalesModel annualSalesModel = salesPlanningService.getAnnualSalesModelDetailsForSH_RH(findNextFinancialYear(), districtForUser, baseSite);
            if(annualSalesModel!=null) {
                if(annualSalesModel.getRevisedTargetBySH()!=null && annualSalesModel.getRevisedByShEmpCode()!=null) {
                    annualTargetSettingSummaryData.setRevisedTargetBySH(annualSalesModel.getRevisedTargetBySH());
                    annualTargetSettingSummaryData.setRevisedTargetBySHEmpCode(annualSalesModel.getRevisedByShEmpCode());
                    annualTargetSettingSummaryData.setIsTopDownIndicatorOn(annualSalesModel.getIsTopDownIndicatorOn());
                }
            }
        }
            List<SubareaListData> subareaList = new ArrayList<>();
            List<String> dealerCategoryList = salesPlanningService.getDealerCategoryForSummaryPage();
            double sumOfQuantityForLastYear = 0.0;
            double sumOfDealerSaleForLastYear = 0.0;
            double currentYearSale = 0.0;
            double sumCurrentYearSale = 0.0;
            double plannedYearSale = 0.0;
            double sumPlannedYearSale = 0.0;
            if (currentUser.getUserType().getCode().equalsIgnoreCase("RH")) {
                if(districtForUser!=null && !districtForUser.isEmpty()) {
                          districtMasterModels.addAll(districtForUser);
                      }
                List<List<Object>> list1=new ArrayList<>();
                double finalSumOfQuantity1 = 0.0;
                if (isAnnualSummaryForReview) {
                    if(districtMasterModels!=null && !districtMasterModels.isEmpty()) {
                        list1 = salesPlanningService.fetchProductSaleDetailsForSummaryAfterTargetSettingForRH(districtMasterModels,baseSite);
                    }
                }
                else if (isAnnualSummaryAfterReview) {
                    if(districtMasterModels!=null && !districtMasterModels.isEmpty()) {
                        list1 = salesPlanningService.fetchProductSaleDetailsForSummaryAfterReviewForRH(districtMasterModels,baseSite);
                    }
                }
                if (list1 != null && !list1.isEmpty()) {
                    finalSumOfQuantity1 += list1.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2 -> (double) objects2.get(0)).sum();
                    double allProdSum = finalSumOfQuantity1;
                    for (List<Object> objects : list1) {
                        SumMonthlyTargetProductMixData annualTargetProductMixData = new SumMonthlyTargetProductMixData();
                        double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                        String skuName = (String) objects.get(1);
                        annualTargetProductMixData.setSkuName(skuName);
                        annualTargetProductMixData.setLastYearShare(0.0);
                        double targetProductMix = Math.round((target / allProdSum) * 100);
                        if (targetProductMix != 0.0)
                            annualTargetProductMixData.setTargetProductMixPercentage(targetProductMix);
                        else
                            annualTargetProductMixData.setTargetProductMixPercentage(0.0);
                        sumMonthlyTargetProductMixDataList.add(annualTargetProductMixData);
                    }
                } else {
                    List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(currentUser.getState());
                    if (productList != null && !productList.isEmpty()) {
                        for (String product : productList) {
                            SumMonthlyTargetProductMixData annualTargetProductMixData = new SumMonthlyTargetProductMixData();
                            annualTargetProductMixData.setLastYearShare(0.0);
                            annualTargetProductMixData.setTargetProductMixPercentage(0.0);
                            annualTargetProductMixData.setSkuName(product);
                            sumMonthlyTargetProductMixDataList.add(annualTargetProductMixData);
                        }
                    }
                }
                if (districtForUser != null && !districtForUser.isEmpty()) {
                    for (DistrictMasterModel districtMasterModel : districtForUser) {
                        if (districtMasterModel != null) {
                                    List<AnnualTargetProductMixData> annualTargetProductMixDataList = new ArrayList<>();
                                    List<AnnualTargetDealerChannelMixData> annualTargetDealerChannelMixDataList = new ArrayList<>();
                                    SubareaListData subareaListData = new SubareaListData();
                                    subareaListData.setDistrictCode(districtMasterModel.getCode());
                                    subareaListData.setDistrictName(districtMasterModel.getName());

                                    if (isAnnualSummaryForReview) {
                                        currentYearSale = salesPlanningService.getCurrentYearSalesForAnnualSummaryForRH(districtMasterModel,baseSite);// pass scluser for new sales changes
                                        subareaListData.setCurrentYearSale(currentYearSale != 0.0 ? currentYearSale : 0.0);
                                      //  sumCurrentYearSale+=currentYearSale;
                                        plannedYearSale = salesPlanningService.getTotalTargetForDealersAfterTargetSettingForRH(districtMasterModel , findNextFinancialYear(), baseSite);
                                        subareaListData.setPlannedYearSale(plannedYearSale != 0.0 ? plannedYearSale : 0.0);
                                        //sumPlannedYearSale+=plannedYearSale;
                                        double finalSumOfQuantity = 0.0;
                                        List<List<Object>> list = salesPlanningService.fetchProductSaleDetailsForSummaryAfterTargetSettingForRH(districtMasterModel,baseSite);
                                        if (list != null && !list.isEmpty()) {
                                            finalSumOfQuantity += list.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2 -> (double) objects2.get(0)).sum();
                                            double allProdSum = finalSumOfQuantity;
                                            for (List<Object> objects : list) {
                                                AnnualTargetProductMixData annualTargetProductMixData = new AnnualTargetProductMixData();
                                                double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                                                String skuName = (String) objects.get(1);
                                                annualTargetProductMixData.setSkuName(skuName);
                                                annualTargetProductMixData.setLastYearShare(0.0);
                                                double targetProductMix = Math.round((target / allProdSum) * 100);
                                                if (targetProductMix != 0.0)
                                                    annualTargetProductMixData.setTargetProductMixPercentage(targetProductMix);
                                                else
                                                    annualTargetProductMixData.setTargetProductMixPercentage(0.0);
                                                annualTargetProductMixDataList.add(annualTargetProductMixData);
                                            }
                                            subareaListData.setAnnualTargetProductMix(annualTargetProductMixDataList);
                                        } else {
                                            List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(currentUser.getState());
                                            if (productList != null && !productList.isEmpty()) {
                                                for (String product : productList) {
                                                    AnnualTargetProductMixData annualTargetProductMixData = new AnnualTargetProductMixData();
                                                    annualTargetProductMixData.setLastYearShare(0.0);
                                                    annualTargetProductMixData.setTargetProductMixPercentage(0.0);
                                                    annualTargetProductMixData.setSkuName(product);
                                                    annualTargetProductMixDataList.add(annualTargetProductMixData);
                                                }
                                                subareaListData.setAnnualTargetProductMix(annualTargetProductMixDataList);
                                            }
                                        }

                                        //dealer category channel mix
                                        double finalSumOfDealerQuantity = 0.0;
                                        List<List<Object>> dealerSaleList = salesPlanningService.fetchDealerSaleDetailsForSummaryAfterTargetSettingForRH(districtMasterModel);
                                        if (dealerSaleList != null && !dealerSaleList.isEmpty()) {
                                            finalSumOfDealerQuantity += dealerSaleList.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2 -> (double) objects2.get(0)).sum();
                                            double allDealerCategorySum = finalSumOfDealerQuantity;
                                            for (List<Object> objects : dealerSaleList) {
                                                AnnualTargetDealerChannelMixData annualTargetDealerChannelMixData = new AnnualTargetDealerChannelMixData();
                                                double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                                                String dealerCategory = (String) objects.get(1);
                                                annualTargetDealerChannelMixData.setDealerCategory(dealerCategory);
                                                annualTargetDealerChannelMixData.setLastYearCounterSale(0.0);

                                                double contributionPlanPercentage = Math.round((target / allDealerCategorySum) * 100);
                                                if (contributionPlanPercentage != 0.0)
                                                    annualTargetDealerChannelMixData.setContributionPlanPercentage(contributionPlanPercentage);
                                                else
                                                    annualTargetDealerChannelMixData.setContributionPlanPercentage(0.0);
                                                annualTargetDealerChannelMixDataList.add(annualTargetDealerChannelMixData);
                                            }
                                            subareaListData.setAnnualTargetDealerChannelMix(annualTargetDealerChannelMixDataList);
                                        } else {
                                            for (String dealerCategory : dealerCategoryList) {
                                                AnnualTargetDealerChannelMixData annualTargetDealerChannelMixData = new AnnualTargetDealerChannelMixData();
                                                annualTargetDealerChannelMixData.setDealerCategory(dealerCategory);
                                                annualTargetDealerChannelMixData.setContributionPlanPercentage(0.0);
                                                annualTargetDealerChannelMixData.setLastYearCounterSale(0.0);
                                                annualTargetDealerChannelMixDataList.add(annualTargetDealerChannelMixData);
                                            }
                                            subareaListData.setAnnualTargetDealerChannelMix(annualTargetDealerChannelMixDataList);
                                        }
                                        List<RegionMasterModel> regionMasterModels= (List<RegionMasterModel>) currentRegion;
                                        AnnualSalesModel annualSalesModelDetailsForRH = salesPlanningService.getAnnualSalesModelDetailsForRH(findNextFinancialYear(), regionMasterModels.get(0), baseSiteService.getCurrentBaseSite());
                                        if(annualSalesModelDetailsForRH!=null){
                                            if(annualSalesModelDetailsForRH.getActionPerformedBy()!=null && annualSalesModelDetailsForRH.getActionPerformedBy().equals(currentUser) && annualSalesModelDetailsForRH.getActionPerformed()!=null && annualSalesModelDetailsForRH.getActionPerformed().equals(WorkflowActions.APPROVED)){
                                                subareaListData.setIsTargetApproved(true);
                                            }
                                            else{
                                                subareaListData.setIsTargetApproved(false);
                                            }
                                        }
                                    } else if (isAnnualSummaryAfterReview) {
                                        currentYearSale = salesPlanningService.getCurrentYearSalesForAnnualSummaryForRH(districtMasterModel, baseSite);
                                        subareaListData.setCurrentYearSale(currentYearSale != 0.0 ? currentYearSale : 0.0);
                                     //   sumCurrentYearSale+=currentYearSale;
                                        plannedYearSale = salesPlanningService.getTotalTargetForDealersAfterReviewForRH(districtMasterModel, findNextFinancialYear(), baseSite);
                                        subareaListData.setPlannedYearSale(plannedYearSale != 0.0 ? plannedYearSale : 0.0);
                                      //  sumPlannedYearSale+=plannedYearSale;
                                        double finalSumOfQuantity = 0.0;
                                        List<List<Object>> list = salesPlanningService.fetchProductSaleDetailsForSummaryAfterReviewForRH(districtMasterModel, baseSite);
                                        if (list != null && !list.isEmpty()) {
                                            finalSumOfQuantity += list.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2 -> (double) objects2.get(0)).sum();
                                            double allProdSum = finalSumOfQuantity;
                                            for (List<Object> objects : list) {
                                                AnnualTargetProductMixData annualTargetProductMixData = new AnnualTargetProductMixData();
                                                double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                                                String skuName = (String) objects.get(1);
                                                annualTargetProductMixData.setSkuName(skuName);
                                                annualTargetProductMixData.setLastYearShare(0.0);
                                                double targetProductMix = Math.round((target / allProdSum) * 100);
                                                if (targetProductMix != 0.0)
                                                    annualTargetProductMixData.setTargetProductMixPercentage(targetProductMix);
                                                else
                                                    annualTargetProductMixData.setTargetProductMixPercentage(0.0);
                                                annualTargetProductMixDataList.add(annualTargetProductMixData);
                                            }
                                            subareaListData.setAnnualTargetProductMix(annualTargetProductMixDataList);
                                        } else {
                                            List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(currentUser.getState());
                                            if (productList != null && !productList.isEmpty()) {
                                                for (String product : productList) {
                                                    AnnualTargetProductMixData annualTargetProductMixData = new AnnualTargetProductMixData();
                                                    annualTargetProductMixData.setLastYearShare(0.0);
                                                    annualTargetProductMixData.setTargetProductMixPercentage(0.0);
                                                    annualTargetProductMixData.setSkuName(product);
                                                    annualTargetProductMixDataList.add(annualTargetProductMixData);
                                                }
                                                subareaListData.setAnnualTargetProductMix(annualTargetProductMixDataList);
                                            }
                                        }

                                        //dealer category channel mix
                                        double finalSumOfDealerQuantity = 0.0;
                                        List<List<Object>> dealerSaleList = salesPlanningService.fetchDealerSaleDetailsForSummaryForSummaryAfterReviewForRH(districtMasterModel);
                                        if (dealerSaleList != null && !dealerSaleList.isEmpty()) {
                                            finalSumOfDealerQuantity += dealerSaleList.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2 -> (double) objects2.get(0)).sum();
                                            double allDealerCategorySum = finalSumOfDealerQuantity;
                                            for (List<Object> objects : dealerSaleList) {
                                                AnnualTargetDealerChannelMixData annualTargetDealerChannelMixData = new AnnualTargetDealerChannelMixData();
                                                double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                                                String dealerCategory = (String) objects.get(1);
                                                annualTargetDealerChannelMixData.setDealerCategory(dealerCategory);
                                                annualTargetDealerChannelMixData.setLastYearCounterSale(0.0);

                                                double contributionPlanPercentage = Math.round((target / allDealerCategorySum) * 100);
                                                if (contributionPlanPercentage != 0.0)
                                                    annualTargetDealerChannelMixData.setContributionPlanPercentage(contributionPlanPercentage);
                                                else
                                                    annualTargetDealerChannelMixData.setContributionPlanPercentage(0.0);
                                                annualTargetDealerChannelMixDataList.add(annualTargetDealerChannelMixData);
                                            }
                                            subareaListData.setAnnualTargetDealerChannelMix(annualTargetDealerChannelMixDataList);
                                        } else {
                                            for (String dealerCategory : dealerCategoryList) {
                                                AnnualTargetDealerChannelMixData annualTargetDealerChannelMixData = new AnnualTargetDealerChannelMixData();
                                                annualTargetDealerChannelMixData.setDealerCategory(dealerCategory);
                                                annualTargetDealerChannelMixData.setContributionPlanPercentage(0.0);
                                                annualTargetDealerChannelMixData.setLastYearCounterSale(0.0);
                                                annualTargetDealerChannelMixDataList.add(annualTargetDealerChannelMixData);
                                            }
                                            subareaListData.setAnnualTargetDealerChannelMix(annualTargetDealerChannelMixDataList);
                                        }
                                        List<RegionMasterModel> regionMasterModels= (List<RegionMasterModel>) currentRegion;
                                        if(regionMasterModels!=null && !regionMasterModels.isEmpty()) {
                                            AnnualSalesModel annualSalesModelDetailsForRH = salesPlanningService.getAnnualSalesModelDetailsForRH(findNextFinancialYear(), regionMasterModels.get(0), baseSiteService.getCurrentBaseSite());
                                            if (annualSalesModelDetailsForRH != null) {
                                                if (annualSalesModelDetailsForRH.getActionPerformedBy()!=null && annualSalesModelDetailsForRH.getActionPerformedBy().equals(currentUser) && annualSalesModelDetailsForRH.getActionPerformed()!=null && annualSalesModelDetailsForRH.getActionPerformed().equals(WorkflowActions.APPROVED)) {
                                                    subareaListData.setIsTargetApproved(true);
                                                }
                                                else{
                                                    subareaListData.setIsTargetApproved(false);
                                                }
                                            }
                                        }
                                    }
                                    subareaList.add(subareaListData);
                        }
                    }
                }
            }
            if(subareaList!=null && !subareaList.isEmpty()) {
                for (SubareaListData subareaListData : subareaList) {
                    if(subareaListData.getIsTargetApproved()!=null) {
                        if (subareaListData.getIsTargetApproved().equals(Boolean.TRUE)) {
                            annualTargetSettingSummaryData.setIsTargetSetForUser(Boolean.TRUE);
                        } else {
                            annualTargetSettingSummaryData.setIsTargetSetForUser(Boolean.FALSE);
                        }
                    }
                }
            }
        if(subareaList!=null && !subareaList.isEmpty()) {
            for (SubareaListData subareaListData : subareaList) {
                sumCurrentYearSale+=subareaListData.getCurrentYearSale();
                sumPlannedYearSale+=subareaListData.getPlannedYearSale();
            }
        }
            SummaryMonthlySubareaListData summaryMonthlySubareaListData=new SummaryMonthlySubareaListData();
            List<RegionMasterModel> regionMasterModels= (List<RegionMasterModel>) currentRegion;
            if(regionMasterModels!=null && !regionMasterModels.isEmpty()) {
                annualTargetSettingSummaryData.setRegionCode(regionMasterModels.get(0).getCode());
                annualTargetSettingSummaryData.setRegionName(regionMasterModels.get(0).getName());
                summaryMonthlySubareaListData.setDistrictName("");
                summaryMonthlySubareaListData.setDistrictCode("");
            }
            else{
                annualTargetSettingSummaryData.setRegionCode("");
                annualTargetSettingSummaryData.setRegionName("");
                summaryMonthlySubareaListData.setDistrictName("");
                summaryMonthlySubareaListData.setDistrictCode("");
            }
            if(regionMasterModels!=null) {
                AnnualSalesModel annualSalesModel = salesPlanningService.getAnnualSalesModelDetailsForRH( findNextFinancialYear(), regionMasterModels.get(0), baseSiteService.getCurrentBaseSite());
                if (annualSalesModel != null) {
                   /* if(annualSalesModel.getRevisedByShEmpCode()!=null && annualSalesModel.getRevisedTargetBySH()!=null)
                    {
                        annualTargetSettingSummaryData.setIsTopDownIndicatorOn(true);
                        annualTargetSettingSummaryData.setRevisedTargetBySH(annualSalesModel.getRevisedTargetBySH());
                        annualTargetSettingSummaryData.setRevisedBy(annualSalesModel.getRevisedByShEmpCode());
                        annualTargetSettingSummaryData.setCommentsForRevision(annualSalesModel.getCommentsForRevision()!=null?annualSalesModel.getCommentsForRevision():"");
                    }else {*/
                        annualTargetSettingSummaryData.setIsTopDownIndicatorOn(false);
                        annualTargetSettingSummaryData.setRevisedTarget(annualSalesModel.getRevisedTarget() != null ? annualSalesModel.getRevisedTarget() : 0.0);
                        annualTargetSettingSummaryData.setRevisedBy(annualSalesModel.getRevisedBy() != null ? annualSalesModel.getRevisedBy().getUid() : "");
                        annualTargetSettingSummaryData.setCommentsForRevision(annualSalesModel.getCommentsForRevision() != null ? annualSalesModel.getCommentsForRevision() : "");
                    //}
                }
            }
            summaryMonthlySubareaListData.setPlannedMonthSale(sumPlannedYearSale);
            summaryMonthlySubareaListData.setRevisedMonthSale(sumCurrentYearSale);
            summaryMonthlySubareaListData.setSumMonthlyTargetProductMix(sumMonthlyTargetProductMixDataList);
            annualTargetSettingSummaryData.setSummaryMonthlySubareaListData(summaryMonthlySubareaListData);
            annualTargetSettingSummaryData.setRhUid(currentUser.getUid());
            annualTargetSettingSummaryData.setSubareaList(subareaList);
            annualTargetSettingSummaryData.setPlannedYear(findNextFinancialYear());
            annualTargetSettingSummaryData.setTotalCurrentYearSales(sumCurrentYearSale);
            annualTargetSettingSummaryData.setTotalPlannedYearSales(sumPlannedYearSale);
            return annualTargetSettingSummaryData;
        }

    @Override
    public SalesPlanningMonthYearDueDateData showMonthYearDueDateForTSMRH() {
        SalesPlanningMonthYearDueDateData salesPlanningMonthYearDueDateData=new SalesPlanningMonthYearDueDateData();
        LocalDate currentDate=LocalDate.now();
        String monthName = currentDate.getMonth().plus(1).name();
        LocalDate due=LocalDate.of(currentDate.getYear(),currentDate.getMonth(),currentDate.lengthOfMonth());
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("d-MM-uuuu");
        String monthDueDate = "Due on " + due.format(formatters);

        String financialYear = findNextFinancialYear();
        Date d = salesPerformanceService.getCurrentFinancialYear().get(0);
        LocalDate date = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate date1 = date.minusMonths(1);
        LocalDate duedatte=LocalDate.of(date1.getYear(),date1.getMonth(),date1.lengthOfMonth());
        DateTimeFormatter formattersYear = DateTimeFormatter.ofPattern("d-MM-uuuu");
        String yearDueDate = "Due on " + duedatte.format(formattersYear);
               salesPlanningMonthYearDueDateData.setFinancialYear(financialYear);
               salesPlanningMonthYearDueDateData.setMonthDueDate(monthDueDate);
               salesPlanningMonthYearDueDateData.setMonth(monthName);
               salesPlanningMonthYearDueDateData.setYearDueDate(yearDueDate);
        return  salesPlanningMonthYearDueDateData;
    }

    @Override
    public ViewBucketwiseRequestList viewBucketwiseRequestForTSMRH(String searchKey) {
        List<ViewBucketwiseRequest> viewBucketwiseRequests=new ArrayList<>();
        ViewBucketwiseRequestList list=new ViewBucketwiseRequestList();
        double totBucket1=0.0,totBucket2=0.0,totBucket3=0.0,totRevisedTarget=0.0;
        FilterTalukaData filterTalukaData=new FilterTalukaData();
        List<UserSubAreaMappingModel> soSubAreaMappingForUser = territoryManagementService.getSOSubAreaMappingForUser(filterTalukaData);
        boolean cardStatus=false;
        for (UserSubAreaMappingModel userSubAreaMappingModel : soSubAreaMappingForUser) {
            ViewBucketwiseRequest request=new ViewBucketwiseRequest();
            SubAreaMasterModel subAreaMaster = userSubAreaMappingModel.getSubAreaMaster();
            SclUserModel sclUser = userSubAreaMappingModel.getSclUser();
            List<DealerRevisedMonthlySalesModel> dealerRevisedMonthlySalesModels = new ArrayList<>();

            //we need to re check on the attribute
            if(searchKey!=null){
                List<String> search=new ArrayList<>();
                search.add(searchKey);
                SclUserModel user = influencerDao.searchSalesOfficer(search);
                dealerRevisedMonthlySalesModels=salesPlanningService.fetchDealerReviewedMonthlySalesDetails(subAreaMaster.getPk().toString(), user);
                if(dealerRevisedMonthlySalesModels==null && dealerRevisedMonthlySalesModels.isEmpty()){
                    LOG.info("SO not found for this "+sclUser.getUid());
                }
            }
            else {
                dealerRevisedMonthlySalesModels=salesPlanningService.fetchDealerReviewedMonthlySalesDetails(subAreaMaster.getPk().toString(), sclUser);
            }
            if(dealerRevisedMonthlySalesModels!=null && !dealerRevisedMonthlySalesModels.isEmpty()) {
                for (DealerRevisedMonthlySalesModel dealerRevisedMonthlySalesModel : dealerRevisedMonthlySalesModels) {
                    if (dealerRevisedMonthlySalesModel != null) {
                     if( dealerRevisedMonthlySalesModel.getActionPerformed()==null ||
                             (dealerRevisedMonthlySalesModel.getActionPerformed()!=null &&
                                    (dealerRevisedMonthlySalesModel.getActionPerformed().equals(WorkflowActions.REVISED))) ||
                            (dealerRevisedMonthlySalesModel.getActionPerformed()!=null &&
                                    (dealerRevisedMonthlySalesModel.getActionPerformed().equals(WorkflowActions.ACCEPTED))) ||
                        (dealerRevisedMonthlySalesModel.getActionPerformed()!=null &&
                                (dealerRevisedMonthlySalesModel.getActionPerformed().equals(WorkflowActions.REVIEWED))) ||
                        (dealerRevisedMonthlySalesModel.getActionPerformed()!=null &&
                                (dealerRevisedMonthlySalesModel.getActionPerformed().equals(WorkflowActions.SENT))) ||
                        (dealerRevisedMonthlySalesModel.getActionPerformed()!=null &&
                                (dealerRevisedMonthlySalesModel.getActionPerformed().equals(WorkflowActions.FAILED)))){
                        totBucket1 += dealerRevisedMonthlySalesModel.getBucket1();
                        totBucket2 += dealerRevisedMonthlySalesModel.getBucket2();
                        totBucket3 += dealerRevisedMonthlySalesModel.getBucket3();
                        totRevisedTarget += dealerRevisedMonthlySalesModel.getRevisedTarget();
                        LOG.info("In All case");
                        cardStatus=true;
                    }
                    }
                }

                if(cardStatus) {
                    request.setBucket1(totBucket1);
                    request.setBucket2(totBucket2);
                    request.setBucket3(totBucket3);
                    request.setTotalBucket(totRevisedTarget);
                    request.setDeviation(totRevisedTarget - (totBucket1 + totBucket2 + totBucket3));
                    if (totRevisedTarget != 0.0) {
                        request.setDeviationPercent((totRevisedTarget - (totBucket1 + totBucket2 + totBucket3)) / totRevisedTarget * 100);
                    } else {
                        request.setDeviationPercent(0.0);
                    }
                    request.setUserCode(sclUser.getUid());
                    request.setUserName(sclUser.getName());
                    request.setSubAreaName(subAreaMaster.getPk().toString());
                    LocalDate localDate = LocalDate.now();
                    LocalDate nextMonth = localDate.plusMonths(1);
                    int currentYear = localDate.getYear();
                    SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
                    ZoneId zone = ZoneId.systemDefault();
                    ZonedDateTime dateTime = nextMonth.atStartOfDay(zone);
                    String month = monthFormat.format(Date.from(dateTime.toInstant()));
                    String formattedMonth = month.concat("-").concat(String.valueOf(currentYear));
                    request.setMonth(formattedMonth);
                    viewBucketwiseRequests.add(request);
                }
            }
        }
        if(viewBucketwiseRequests!=null && !viewBucketwiseRequests.isEmpty()) {
            list.setViewBucketwiseRequest(viewBucketwiseRequests);
        }
        return list;
    }

    @Override
    public ViewBucketwiseRequest monthwiseSummaryForTSM() {
        ViewBucketwiseRequest request=new ViewBucketwiseRequest();
        double totBucket1=0.0,totBucket2=0.0,totBucket3=0.0,totRevisedTarget=0.0;
        SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
        FilterTalukaData filterTalukaData=new FilterTalukaData();
        List<UserSubAreaMappingModel> soSubAreaMappingForUser = territoryManagementService.getSOSubAreaMappingForUser(filterTalukaData);
        for (UserSubAreaMappingModel userSubAreaMappingModel : soSubAreaMappingForUser) {
            SubAreaMasterModel subAreaMaster = userSubAreaMappingModel.getSubAreaMaster();
            SclUserModel sclUser = userSubAreaMappingModel.getSclUser();
            List<DealerRevisedMonthlySalesModel> dealerRevisedMonthlySalesModels = new ArrayList<>();
            //we need to re check on the attribute
            dealerRevisedMonthlySalesModels=salesPlanningService.fetchDealerReviewedMonthlySalesDetails(subAreaMaster.getPk().toString(), sclUser);
            if(dealerRevisedMonthlySalesModels!=null && !dealerRevisedMonthlySalesModels.isEmpty()) {
                for (DealerRevisedMonthlySalesModel dealerRevisedMonthlySalesModel : dealerRevisedMonthlySalesModels) {
                    if (dealerRevisedMonthlySalesModel != null) {
                        totBucket1 += dealerRevisedMonthlySalesModel.getBucket1();
                        totBucket2 += dealerRevisedMonthlySalesModel.getBucket2();
                        totBucket3 += dealerRevisedMonthlySalesModel.getBucket3();
                        totRevisedTarget += dealerRevisedMonthlySalesModel.getRevisedTarget();
                    }
                }
            }
        }
        request.setBucket1(totBucket1);
        request.setBucket2(totBucket2);
        request.setBucket3(totBucket3);
        request.setTotalBucket(totRevisedTarget);
        request.setDeviation(totRevisedTarget - (totBucket1 + totBucket2 + totBucket3));
        if(totRevisedTarget!=0.0) {
            request.setDeviationPercent((totRevisedTarget - (totBucket1 + totBucket2 + totBucket3) / totRevisedTarget) * 100);
        }else{
            request.setDeviationPercent(0.0);
        }
        request.setUserName(currentUser.getName());
        request.setUserCode(currentUser.getUid());
       request.setDistrictName(currentUser.getDistrict());
       return request;
    }

    @Override
    public MonthlyTargetSettingSummaryData viewMonthlySalesSummary(boolean isMonthlySummaryAfterSubmitPlanned, boolean isMonthlySummaryForReview, boolean isMonthlySummaryAfterSubmitRevised, boolean isMonthlySummaryAfterSubmitReviewed) {
        SclUserModel sclUser = (SclUserModel) getUserService().getCurrentUser();
        BaseSiteModel baseSite = baseSiteService.getCurrentBaseSite();

        List<SubAreaMasterModel> subAreas = territoryManagementService.getTerritoriesForSO();
        if (CollectionUtils.isEmpty(subAreas)) {
            throw new ModelNotFoundException("No Subarea Attached to current user: " + sclUser.getUid());
        }

        MonthlyTargetSettingSummaryData monthlyTargetSettingSummaryData = new MonthlyTargetSettingSummaryData();
        if(subAreas!=null && !subAreas.isEmpty()) {
            MonthlySalesModel monthlySalesModelDetailForDO = salesPlanningService.getMonthlySalesModelDetailForDO(subAreas, baseSiteService.getCurrentBaseSite());
            if (monthlySalesModelDetailForDO != null) {
                if (monthlySalesModelDetailForDO.getRevisedTargetBySH() != null && monthlySalesModelDetailForDO.getRevisedByShEmpCode() != null) {
                    monthlyTargetSettingSummaryData.setRevisedTargetBySH(monthlySalesModelDetailForDO.getRevisedTargetBySH());
                    monthlyTargetSettingSummaryData.setRevisedTargetBySHEmpCode(monthlySalesModelDetailForDO.getRevisedByShEmpCode());
                    monthlyTargetSettingSummaryData.setIsTopDownIndicatorOn(monthlySalesModelDetailForDO.getIsTopDownIndicatorOn());
                }
            }
        }

        List<MonthlySubareaListData> subareaList = new ArrayList<>();
        List<String> dealerCategoryList = salesPlanningService.getDealerCategoryForSummaryPage();
        double revisedMonthSale=0.0;
        double plannedMonthSale=0.0;
        final double[] sumOfQuantity = {0.0};
        for (SubAreaMasterModel subarea : subAreas) {
            List<MonthlyTargetProductMixData> monthlyTargetProductMixDataList = new ArrayList<>();
            List<MonthlyTargetDealerChannelMixData> monthlyTargetDealerChannelMixDataList = new ArrayList<>();
            MonthlySubareaListData subareaListData = new MonthlySubareaListData();
            subareaListData.setSubareaName(subarea.getTaluka());
            subareaListData.setSubAreaId(subarea.getPk().toString());

            //scenario 2: monthly sales after finalize planned sales
            if(isMonthlySummaryAfterSubmitPlanned) {
                //scenario 1 : monthly target set
                StringBuilder str= new StringBuilder();

                Calendar cal = Calendar.getInstance();
                int month = cal.get(Calendar.MONTH);
                int nextMonth = (cal.get(Calendar.MONTH) + 1) % 12;
                int year = cal.get(Calendar.YEAR);

                String currentMonth = theMonth(nextMonth);

                String currentMonthName=null;

                //temp changes
                if(nextMonth>=0 && nextMonth<=2) {
                    currentMonthName= String.valueOf(str.append(currentMonth).append("-").append(LocalDate.now().getYear() + 1));
                }
                if(nextMonth>=3 && nextMonth<=11){
                    currentMonthName = String.valueOf(str.append(currentMonth).append("-").append(LocalDate.now().getYear()));
                }
                plannedMonthSale=salesPlanningService.getPlannedMonthSaleForMonthlySaleSummary(sclUser,currentMonthName, subarea.getPk().toString(),baseSite);
              //  plannedMonthSale = salesPlanningService.getPlannedTargetAfterTargetSetMonthlySP(sclUser, subarea.getPk().toString());
                subareaListData.setPlannedMonthSale(plannedMonthSale != 0.0 ? plannedMonthSale : 0.0);//using query annual table + last year same month
                revisedMonthSale = salesPlanningService.getRevisedTargetAfterTargetSetMonthlySP(sclUser, subarea.getPk().toString());
                subareaListData.setRevisedMonthSale(revisedMonthSale!=0.0?revisedMonthSale:0.0);

                List<List<Object>> list = salesPlanningService.fetchProductMixDetailsAfterTargetSetMonthlySummary(subarea.getPk().toString(), sclUser, baseSite);
                double finalSumOfQuantity = 0.0;
                if (list != null && !list.isEmpty()) {
                    finalSumOfQuantity += list.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2 -> (double) objects2.get(0)).sum();
                    double allProdSum = finalSumOfQuantity;
                    for (List<Object> objects : list) {
                        MonthlyTargetProductMixData monthlyTargetProductMixData = new MonthlyTargetProductMixData();
                        double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                        String skuName = (String) objects.get(1);
                        monthlyTargetProductMixData.setSkuName(skuName);
                        /*List<List<Object>> lastYearShareForProductMonthly = salesPlanningService.getLastYearShareForProductFromNCRMonthly(subarea.getPk().toString());
                        if(lastYearShareForProductMonthly!=null && !lastYearShareForProductMonthly.isEmpty())
                        {
                            for (List<Object> objectList : lastYearShareForProductMonthly) {
                                String productCode = (String) objectList.get(0);
                                String productName = (String) objectList.get(1);
                                double pquantityInMT = (Double) objectList.get(2)!=0.0? (Double) objectList.get(2) : 0.0;
                                sumOfQuantity[0] += pquantityInMT;
                                double lastYearShareSum = sumOfQuantity[0];
                                double lastYearShare = (pquantityInMT / lastYearShareSum) * 100;
                                if(lastYearShare!=0.0)
                                    monthlyTargetProductMixData.setLastYearShare(lastYearShare);
                                else
                                    monthlyTargetProductMixData.setLastYearShare(0.0);
                            }

                        }
                        else
                            monthlyTargetProductMixData.setLastYearShare(0.0);*/
                        monthlyTargetProductMixData.setLastYearShare(0.0);
                        double targetProductMix = Math.round((target / allProdSum) * 100);
                        if (targetProductMix != 0.0)
                            monthlyTargetProductMixData.setTargetProductMixPercentage(targetProductMix);
                        else
                            monthlyTargetProductMixData.setTargetProductMixPercentage(0.0);
                        monthlyTargetProductMixDataList.add(monthlyTargetProductMixData);
                    }
                    subareaListData.setMonthlyTargetProductMix(monthlyTargetProductMixDataList);
                }
                else {
                    List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(sclUser.getState());
                    if (productList != null && !productList.isEmpty()) {
                        for (String product : productList) {
                            MonthlyTargetProductMixData monthlyTargetProductMixData = new MonthlyTargetProductMixData();
                            monthlyTargetProductMixData.setLastYearShare(0.0);
                            monthlyTargetProductMixData.setTargetProductMixPercentage(0.0);
                            monthlyTargetProductMixData.setSkuName(product);
                            monthlyTargetProductMixDataList.add(monthlyTargetProductMixData);
                        }
                        subareaListData.setMonthlyTargetProductMix(monthlyTargetProductMixDataList);
                    }
                }

                //dealer channel mix
                double finalSumOfDealerQuantity=0.0;
                List<List<Object>> dealerList = salesPlanningService.fetchDealerMixDetailsAfterTargetSetMonthlySummary(subarea.getPk().toString(), sclUser);
                if(dealerList!=null && !dealerList.isEmpty()) {
                    finalSumOfDealerQuantity += dealerList.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2-> (double) objects2.get(0)).sum();
                    double allDealerCategorySum = finalSumOfDealerQuantity;
                    for (List<Object> objects : dealerList) {
                        MonthlyTargetDealerChannelMixData monthlyTargetDealerChannelMixData = new MonthlyTargetDealerChannelMixData();
                        double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                        String dealerCategory = (String) objects.get(1);
                        monthlyTargetDealerChannelMixData.setDealerCategory(dealerCategory);
                        monthlyTargetDealerChannelMixData.setLastYearCounterSale(0.0);

                        double contributionPlanPercentage = Math.round((target / allDealerCategorySum) * 100);
                        if (contributionPlanPercentage != 0.0)
                            monthlyTargetDealerChannelMixData.setContributionPlanPercentage(contributionPlanPercentage);
                        else
                            monthlyTargetDealerChannelMixData.setContributionPlanPercentage(0.0);
                        monthlyTargetDealerChannelMixDataList.add(monthlyTargetDealerChannelMixData);
                    }
                    subareaListData.setMonthlyTargetDealerChannelMix(monthlyTargetDealerChannelMixDataList);
                }
                else  {
                    for (String dealerCategory : dealerCategoryList) {
                        MonthlyTargetDealerChannelMixData monthlyTargetDealerChannelMixData = new MonthlyTargetDealerChannelMixData();
                        monthlyTargetDealerChannelMixData.setDealerCategory(dealerCategory);
                        monthlyTargetDealerChannelMixData.setContributionPlanPercentage(0.0);
                        monthlyTargetDealerChannelMixData.setLastYearCounterSale(0.0);
                        monthlyTargetDealerChannelMixDataList.add(monthlyTargetDealerChannelMixData);
                    }
                    subareaListData.setMonthlyTargetDealerChannelMix(monthlyTargetDealerChannelMixDataList);
                }
                MonthlySalesModel monthlySalesModel = salesPlanningService.viewMonthlySalesTargetForPlannedTab(subarea.getPk().toString(), sclUser);
                if(monthlySalesModel!=null)
                {
                    if(monthlySalesModel.getRevisedByShEmpCode()!=null && monthlySalesModel.getRevisedTargetBySH()!=null)
                    {
                        subareaListData.setIsTopDownIndicatorOn(true);
                        subareaListData.setRevisedTarget(monthlySalesModel.getRevisedTargetSendToDO());
                        subareaListData.setPremiumTargetByRH(monthlySalesModel.getRevisedPremiumTargetSentToDO()!=null?monthlySalesModel.getRevisedPremiumTargetSentToDO():0.0);
                        subareaListData.setNonPremiumTargetByRH(monthlySalesModel.getRevisedNonPremiumTargetSentToDO()!=null?monthlySalesModel.getRevisedNonPremiumTargetSentToDO():0.0);
                        subareaListData.setRevisedBy(monthlySalesModel.getRevisedBy()!=null?monthlySalesModel.getRevisedBy().getUid():"");
                        subareaListData.setCommentsForRevision(monthlySalesModel.getCommentsForRevision()!=null?monthlySalesModel.getCommentsForRevision():"");
                    }else {
                        subareaListData.setIsTopDownIndicatorOn(false);
                        subareaListData.setRevisedTarget(monthlySalesModel.getRevisedTarget() != null ? monthlySalesModel.getRevisedTarget() : 0.0);
                        subareaListData.setRevisedBy(monthlySalesModel.getRevisedBy() != null ? monthlySalesModel.getRevisedBy().getUid() : "");
                        subareaListData.setCommentsForRevision(monthlySalesModel.getCommentsForRevision() != null ? monthlySalesModel.getCommentsForRevision() : "");
                    }
                }
            }
            //scenario 3: monthly sales summary page - planned and review tab
            else if(isMonthlySummaryForReview) {
                //scenario 1 : monthly target set
                StringBuilder str= new StringBuilder();
                Calendar cal = Calendar.getInstance();
                int month = cal.get(Calendar.MONTH);
                int nextMonth = (cal.get(Calendar.MONTH) + 1) % 12;
                int year = cal.get(Calendar.YEAR);

                String currentMonth = theMonth(nextMonth);

                String currentMonthName=null;
                //temp changes
                if(nextMonth>=0 && nextMonth<=2) {
                    currentMonthName= String.valueOf(str.append(currentMonth).append("-").append(LocalDate.now().getYear() + 1));
                }
                if(nextMonth>=3 && nextMonth<=11){
                    currentMonthName = String.valueOf(str.append(currentMonth).append("-").append(LocalDate.now().getYear()));
                }
                plannedMonthSale=salesPlanningService.getPlannedMonthSaleForMonthlySaleSummary(sclUser,currentMonthName, subarea.getPk().toString(), baseSite);
               // plannedMonthSale = salesPlanningService.getPlannedTargetForReviewMonthlySP(sclUser, subarea.getPk().toString(),baseSite);
                subareaListData.setPlannedMonthSale(plannedMonthSale != 0.0 ? plannedMonthSale : 0.0);//using query annual table + last year same month
                revisedMonthSale = salesPlanningService.getRevisedTargetForReviewMonthlySP(sclUser, subarea.getPk().toString(),baseSite);
                subareaListData.setRevisedMonthSale(revisedMonthSale!=0.0?revisedMonthSale:0.0);

                List<List<Object>> list = salesPlanningService.fetchProductMixDetailsAfterTargetSetMonthlySummary(subarea.getPk().toString(), sclUser, baseSite);
                double finalSumOfQuantity = 0.0;
                if (list != null && !list.isEmpty()) {
                    finalSumOfQuantity += list.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2 -> (double) objects2.get(0)).sum();
                    double allProdSum = finalSumOfQuantity;
                    for (List<Object> objects : list) {
                        MonthlyTargetProductMixData monthlyTargetProductMixData = new MonthlyTargetProductMixData();
                        double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                        String skuName = (String) objects.get(1);
                        monthlyTargetProductMixData.setSkuName(skuName);
                        /*List<List<Object>> lastYearShareForProductMonthly = salesPlanningService.getLastYearShareForProductFromNCRMonthly(subarea.getPk().toString());
                        if(lastYearShareForProductMonthly!=null && !lastYearShareForProductMonthly.isEmpty())
                        {
                            for (List<Object> objectList : lastYearShareForProductMonthly) {
                                String productCode = (String) objectList.get(0);
                                String productName = (String) objectList.get(1);
                                double pquantityInMT = (Double) objectList.get(2)!=0.0? (Double) objectList.get(2) : 0.0;
                                sumOfQuantity[0] += pquantityInMT;
                                double lastYearShareSum = sumOfQuantity[0];
                                double lastYearShare = (pquantityInMT / lastYearShareSum) * 100;
                                if(lastYearShare!=0.0)
                                    monthlyTargetProductMixData.setLastYearShare(lastYearShare);
                                else
                                    monthlyTargetProductMixData.setLastYearShare(0.0);
                            }

                        }
                        else
                            monthlyTargetProductMixData.setLastYearShare(0.0);*/
                        monthlyTargetProductMixData.setLastYearShare(0.0);
                        double targetProductMix = Math.round((target / allProdSum) * 100);
                        if (targetProductMix != 0.0)
                            monthlyTargetProductMixData.setTargetProductMixPercentage(targetProductMix);
                        else
                            monthlyTargetProductMixData.setTargetProductMixPercentage(0.0);
                        monthlyTargetProductMixDataList.add(monthlyTargetProductMixData);
                    }
                    subareaListData.setMonthlyTargetProductMix(monthlyTargetProductMixDataList);
                }
                else {
                    List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(sclUser.getState());
                    if (productList != null && !productList.isEmpty()) {
                        for (String product : productList) {
                            MonthlyTargetProductMixData monthlyTargetProductMixData = new MonthlyTargetProductMixData();
                            monthlyTargetProductMixData.setLastYearShare(0.0);
                            monthlyTargetProductMixData.setTargetProductMixPercentage(0.0);
                            monthlyTargetProductMixData.setSkuName(product);
                            monthlyTargetProductMixDataList.add(monthlyTargetProductMixData);
                        }
                        subareaListData.setMonthlyTargetProductMix(monthlyTargetProductMixDataList);
                    }
                }

                //dealer channel mix
                double finalSumOfDealerQuantity=0.0;
                List<List<Object>> dealerList = salesPlanningService.fetchDealerMixDetailsAfterTargetSetMonthlySummary(subarea.getPk().toString(), sclUser);
                if(dealerList!=null && !dealerList.isEmpty()) {
                    finalSumOfDealerQuantity += dealerList.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2-> (double) objects2.get(0)).sum();
                    double allDealerCategorySum = finalSumOfDealerQuantity;
                    for (List<Object> objects : dealerList) {
                        MonthlyTargetDealerChannelMixData monthlyTargetDealerChannelMixData = new MonthlyTargetDealerChannelMixData();
                        double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                        String dealerCategory = (String) objects.get(1);
                        monthlyTargetDealerChannelMixData.setDealerCategory(dealerCategory);
                        monthlyTargetDealerChannelMixData.setLastYearCounterSale(0.0);

                        double contributionPlanPercentage = Math.round((target / allDealerCategorySum) * 100);
                        if (contributionPlanPercentage != 0.0)
                            monthlyTargetDealerChannelMixData.setContributionPlanPercentage(contributionPlanPercentage);
                        else
                            monthlyTargetDealerChannelMixData.setContributionPlanPercentage(0.0);
                        monthlyTargetDealerChannelMixDataList.add(monthlyTargetDealerChannelMixData);
                    }
                    subareaListData.setMonthlyTargetDealerChannelMix(monthlyTargetDealerChannelMixDataList);
                }
                else  {
                    for (String dealerCategory : dealerCategoryList) {
                        MonthlyTargetDealerChannelMixData monthlyTargetDealerChannelMixData = new MonthlyTargetDealerChannelMixData();
                        monthlyTargetDealerChannelMixData.setDealerCategory(dealerCategory);
                        monthlyTargetDealerChannelMixData.setContributionPlanPercentage(0.0);
                        monthlyTargetDealerChannelMixData.setLastYearCounterSale(0.0);
                        monthlyTargetDealerChannelMixDataList.add(monthlyTargetDealerChannelMixData);
                    }
                    subareaListData.setMonthlyTargetDealerChannelMix(monthlyTargetDealerChannelMixDataList);
                }
            }
            //scenario 4: monthly sales summary page - after review
            else if(isMonthlySummaryAfterSubmitReviewed) {
                //scenario 1 : monthly target set
                StringBuilder str= new StringBuilder();
                Calendar cal = Calendar.getInstance();
                int month = cal.get(Calendar.MONTH);
                int nextMonth = (cal.get(Calendar.MONTH) + 1) % 12;
                int year = cal.get(Calendar.YEAR);

                String currentMonth = theMonth(nextMonth);

                String currentMonthName=null;
                //temp changes
                if(nextMonth>=0 && nextMonth<=2) {
                    currentMonthName= String.valueOf(str.append(currentMonth).append("-").append(LocalDate.now().getYear() + 1));
                }
                if(nextMonth>=3 && nextMonth<=11){
                    currentMonthName = String.valueOf(str.append(currentMonth).append("-").append(LocalDate.now().getYear()));
                }
                plannedMonthSale=salesPlanningService.getPlannedMonthSaleForMonthlySaleSummary(sclUser,currentMonthName, subarea.getPk().toString(), baseSite);
               // plannedMonthSale = salesPlanningService.getPlannedTargetAfterReviewMonthlySP(sclUser, subarea.getPk().toString(),baseSite);
                subareaListData.setPlannedMonthSale(plannedMonthSale != 0.0 ? plannedMonthSale : 0.0);//using query annual table + last year same month
                revisedMonthSale = salesPlanningService.getRevisedTargetAfterReviewMonthlySP(sclUser, subarea.getPk().toString(),baseSite);
                subareaListData.setRevisedMonthSale(revisedMonthSale!=0.0?revisedMonthSale:0.0);

                List<List<Object>> list = salesPlanningService.fetchProductMixDetailsForReviewTargetMonthlySummary(subarea.getPk().toString(), sclUser,baseSite);
                double finalSumOfQuantity = 0.0;
                if (list != null && !list.isEmpty()) {
                    finalSumOfQuantity += list.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2 -> (double) objects2.get(0)).sum();
                    double allProdSum = finalSumOfQuantity;
                    for (List<Object> objects : list) {
                        MonthlyTargetProductMixData monthlyTargetProductMixData = new MonthlyTargetProductMixData();
                        double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                        String skuName = (String) objects.get(1);
                        monthlyTargetProductMixData.setSkuName(skuName);
                        /*List<List<Object>> lastYearShareForProductMonthly = salesPlanningService.getLastYearShareForProductFromNCRMonthly(subarea.getPk().toString());
                        if(lastYearShareForProductMonthly!=null && !lastYearShareForProductMonthly.isEmpty())
                        {
                            for (List<Object> objectList : lastYearShareForProductMonthly) {
                                String productCode = (String) objectList.get(0);
                                String productName = (String) objectList.get(1);
                                double pquantityInMT = (Double) objectList.get(2)!=0.0? (Double) objectList.get(2) : 0.0;
                                sumOfQuantity[0] += pquantityInMT;
                                double lastYearShareSum = sumOfQuantity[0];
                                double lastYearShare = (pquantityInMT / lastYearShareSum) * 100;
                                if(lastYearShare!=0.0)
                                    monthlyTargetProductMixData.setLastYearShare(lastYearShare);
                                else
                                    monthlyTargetProductMixData.setLastYearShare(0.0);
                            }

                        }
                        else
                            monthlyTargetProductMixData.setLastYearShare(0.0);*/
                        monthlyTargetProductMixData.setLastYearShare(0.0);
                        double targetProductMix = Math.round((target / allProdSum) * 100);
                        if (targetProductMix != 0.0)
                            monthlyTargetProductMixData.setTargetProductMixPercentage(targetProductMix);
                        else
                            monthlyTargetProductMixData.setTargetProductMixPercentage(0.0);
                        monthlyTargetProductMixDataList.add(monthlyTargetProductMixData);
                    }
                    subareaListData.setMonthlyTargetProductMix(monthlyTargetProductMixDataList);
                }
                else {
                    List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(sclUser.getState());
                    if (productList != null && !productList.isEmpty()) {
                        for (String product : productList) {
                            MonthlyTargetProductMixData monthlyTargetProductMixData = new MonthlyTargetProductMixData();
                            monthlyTargetProductMixData.setLastYearShare(0.0);
                            monthlyTargetProductMixData.setTargetProductMixPercentage(0.0);
                            monthlyTargetProductMixData.setSkuName(product);
                            monthlyTargetProductMixDataList.add(monthlyTargetProductMixData);
                        }
                        subareaListData.setMonthlyTargetProductMix(monthlyTargetProductMixDataList);
                    }
                }

                //dealer channel mix
                double finalSumOfDealerQuantity=0.0;
                List<List<Object>> dealerList = salesPlanningService.fetchDealerMixDetailsAfterReviewMonthlySummary(subarea.getPk().toString(), sclUser);
                if(dealerList!=null && !dealerList.isEmpty()) {
                    finalSumOfDealerQuantity += dealerList.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2-> (double) objects2.get(0)).sum();
                    double allDealerCategorySum = finalSumOfDealerQuantity;
                    for (List<Object> objects : dealerList) {
                        MonthlyTargetDealerChannelMixData monthlyTargetDealerChannelMixData = new MonthlyTargetDealerChannelMixData();
                        double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                        String dealerCategory = (String) objects.get(1);
                        monthlyTargetDealerChannelMixData.setDealerCategory(dealerCategory);
                        monthlyTargetDealerChannelMixData.setLastYearCounterSale(0.0);

                        double contributionPlanPercentage = Math.round((target / allDealerCategorySum) * 100);
                        if (contributionPlanPercentage != 0.0)
                            monthlyTargetDealerChannelMixData.setContributionPlanPercentage(contributionPlanPercentage);
                        else
                            monthlyTargetDealerChannelMixData.setContributionPlanPercentage(0.0);
                        monthlyTargetDealerChannelMixDataList.add(monthlyTargetDealerChannelMixData);
                    }
                    subareaListData.setMonthlyTargetDealerChannelMix(monthlyTargetDealerChannelMixDataList);
                }
                else  {
                    for (String dealerCategory : dealerCategoryList) {
                        MonthlyTargetDealerChannelMixData monthlyTargetDealerChannelMixData = new MonthlyTargetDealerChannelMixData();
                        monthlyTargetDealerChannelMixData.setDealerCategory(dealerCategory);
                        monthlyTargetDealerChannelMixData.setContributionPlanPercentage(0.0);
                        monthlyTargetDealerChannelMixData.setLastYearCounterSale(0.0);
                        monthlyTargetDealerChannelMixDataList.add(monthlyTargetDealerChannelMixData);
                    }
                    subareaListData.setMonthlyTargetDealerChannelMix(monthlyTargetDealerChannelMixDataList);
                }

            }
            else
            {
                //scenario 1 : monthly target set
                StringBuilder str= new StringBuilder();
                Calendar cal = Calendar.getInstance();
                int month = cal.get(Calendar.MONTH);
                int nextMonth = (cal.get(Calendar.MONTH) + 1) % 12;
                int year = cal.get(Calendar.YEAR);

                String currentMonth = theMonth(nextMonth);

                String currentMonthName=null;
                //temp changes
                if(nextMonth>=0 && nextMonth<=2) {
                    currentMonthName= String.valueOf(str.append(currentMonth).append("-").append(LocalDate.now().getYear() + 1));
                }
                if(nextMonth>=3 && nextMonth<=11){
                    currentMonthName = String.valueOf(str.append(currentMonth).append("-").append(LocalDate.now().getYear()));
                }

                //plannedMonthSale = salesPlanningService.getPlannedMonthSaleForMonthlySaleSummary(sclUser, baseSite, subarea.getPk().toString());
                plannedMonthSale=salesPlanningService.getPlannedMonthSaleForMonthlySaleSummary(sclUser,currentMonthName, subarea.getPk().toString(), baseSite);
                subareaListData.setPlannedMonthSale(plannedMonthSale != 0.0 ? plannedMonthSale : 0.0);//using query annual table + last year same month
                subareaListData.setRevisedMonthSale(0.0);

                List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(sclUser.getState());
                if (productList != null && !productList.isEmpty()) {
                    for (String product : productList) {
                        MonthlyTargetProductMixData monthlyTargetProductMixData = new MonthlyTargetProductMixData();
                        /*List<List<Object>> lastYearShareForProductMonthly = salesPlanningService.getLastYearShareForProductFromNCRMonthly(subarea.getPk().toString());
                        if(lastYearShareForProductMonthly!=null && !lastYearShareForProductMonthly.isEmpty())
                        {
                            for (List<Object> objectList : lastYearShareForProductMonthly) {
                                String productCode = (String) objectList.get(0);
                                String productName = (String) objectList.get(1);
                                double pquantityInMT = (Double) objectList.get(2)!=0.0? (Double) objectList.get(2) : 0.0;
                                sumOfQuantity[0] += pquantityInMT;
                                double lastYearShareSum = sumOfQuantity[0];
                                double lastYearShare = (pquantityInMT / lastYearShareSum) * 100;
                                if(lastYearShare!=0.0)
                                    monthlyTargetProductMixData.setLastYearShare(lastYearShare);
                                else
                                    monthlyTargetProductMixData.setLastYearShare(0.0);
                            }

                        }
                        else
                            monthlyTargetProductMixData.setLastYearShare(0.0);*/
                        monthlyTargetProductMixData.setLastYearShare(0.0);
                        monthlyTargetProductMixData.setTargetProductMixPercentage(0.0);
                        monthlyTargetProductMixData.setSkuName(product);
                        monthlyTargetProductMixDataList.add(monthlyTargetProductMixData);
                        subareaListData.setMonthlyTargetProductMix(monthlyTargetProductMixDataList);
                    }

                }
                if (dealerCategoryList != null && !dealerCategoryList.isEmpty()) {
                    for (String dealerCategory : dealerCategoryList) {
                        MonthlyTargetDealerChannelMixData monthlyTargetDealerChannelMixData = new MonthlyTargetDealerChannelMixData();
                        monthlyTargetDealerChannelMixData.setDealerCategory(dealerCategory);
                        monthlyTargetDealerChannelMixData.setContributionPlanPercentage(0.0);
                        monthlyTargetDealerChannelMixData.setLastYearCounterSale(0.0);
                        monthlyTargetDealerChannelMixDataList.add(monthlyTargetDealerChannelMixData);
                        subareaListData.setMonthlyTargetDealerChannelMix(monthlyTargetDealerChannelMixDataList);
                    }
                }
            }
            subareaList.add(subareaListData);
        }
        monthlyTargetSettingSummaryData.setSubareaList(subareaList);
        monthlyTargetSettingSummaryData.setPlannedSaleMonth(findNextMonth());
        monthlyTargetSettingSummaryData.setRevisedPlanMonth(findNextMonth());
        monthlyTargetSettingSummaryData.setTotalCurrentMonthSales(0.0);
        monthlyTargetSettingSummaryData.setTotalPlannedMonthSales(0.0);
        return monthlyTargetSettingSummaryData;
    }

    @Override
    public AnnualSalesMonthWiseTargetListData viewReviewedSalesforDealersMonthwise(String subArea, String filter) {
        SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
        BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
        AnnualSalesModel annualSalesModel = salesPlanningService.viewPlannedSalesforDealersRetailersMonthWise(subArea, currentUser, brand);
        AnnualSalesMonthWiseTargetListData listData = new AnnualSalesMonthWiseTargetListData();

        SubAreaMasterModel subAreaMaster=territoryManagementService.getTerritoryById(subArea);
        listData.setTotalPlanSales(annualSalesModel.getDealerRevisedTotalPlanSales()!=null?annualSalesModel.getDealerRevisedTotalPlanSales():0.0);
        listData.setTotalCurrentYearSales(annualSalesModel.getDealerRevisedTotalCySales()!=null?annualSalesModel.getDealerRevisedTotalCySales():0.0);
        listData.setSubArea(subAreaMaster.getTaluka());
        listData.setSubAreaId(subAreaMaster.getPk().toString());
        if(StringUtils.isBlank(filter)) {
            if (annualSalesModel!=null && annualSalesModel.getIsAnnualSalesReviewedForDealer()!=null && annualSalesModel.getIsAnnualSalesReviewedForDealer().equals(false))
            {
                if (annualSalesModel.getDealerRevisedAnnualSales() != null && Objects.nonNull(annualSalesModel.getDealerRevisedAnnualSales()) &&
                        !annualSalesModel.getDealerRevisedAnnualSales().isEmpty()) {
                    List<AnnualSalesMonthWiseTargetData> annualSalesMonthWiseTargetDataList = new ArrayList<>();
                    for (DealerRevisedAnnualSalesModel dealerRevisedAnnualSalesModel : annualSalesModel.getDealerRevisedAnnualSales()) {
                        //for (SubAreaMasterModel subAreaMasterModel : annualSalesModel.getSubAreaMasterList()) {
                        if(annualSalesModel.getSubAreaMaster()!=null)
                        {
                            SubAreaMasterModel subAreaMasterModel = annualSalesModel.getSubAreaMaster();
                        if (subAreaMasterModel.equals(dealerRevisedAnnualSalesModel.getSubAreaMaster())) {
                            listData.setSubArea(subAreaMasterModel.getTaluka());
                            listData.setSubAreaId(subAreaMasterModel.getPk().toString());
                            AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData = new AnnualSalesMonthWiseTargetData();
                            annualSalesMonthWiseTargetData.setCustomerCode(dealerRevisedAnnualSalesModel.getCustomerCode());
                            annualSalesMonthWiseTargetData.setCustomerName(dealerRevisedAnnualSalesModel.getCustomerName());
                            annualSalesMonthWiseTargetData.setCustomerPotential(dealerRevisedAnnualSalesModel.getCustomerPotential() != null ? dealerRevisedAnnualSalesModel.getCustomerPotential() : 0.0);
                            annualSalesMonthWiseTargetData.setTotalTarget(dealerRevisedAnnualSalesModel.getTotalTarget());

                            List<MonthWiseTargetData> monthWiseTargetDataList = new ArrayList<>();
                            if (dealerRevisedAnnualSalesModel.getMonthWiseAnnualTarget() != null &&
                                    !dealerRevisedAnnualSalesModel.getMonthWiseAnnualTarget().isEmpty()) {
                                for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : dealerRevisedAnnualSalesModel.getMonthWiseAnnualTarget()) {
                                    MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                    if (monthWiseAnnualTargetModel.getCustomerCode().equals(dealerRevisedAnnualSalesModel.getCustomerCode()) && monthWiseAnnualTargetModel.getIsAnnualSalesRevisedForDealer().equals(true)) {
                                        monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                        monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                        monthWiseTargetDataList.add(monthWiseTargetData);
                                    }
                                }
                            }
                            annualSalesMonthWiseTargetData.setMonthWiseTarget(monthWiseTargetDataList);

                            List<SKUData> skuDataList = new ArrayList<>();
                            if (dealerRevisedAnnualSalesModel.getListOfSkus() != null && !dealerRevisedAnnualSalesModel.getListOfSkus().isEmpty()) {
                                for (ProductModel sku : dealerRevisedAnnualSalesModel.getListOfSkus()) {
                                    SKUData skuData = new SKUData();
                                    skuData.setProductCode(sku.getCode());
                                    skuData.setProductName(sku.getName());
                                    if (sku.getProductSale() != null && !sku.getProductSale().isEmpty()) {
                                        for (ProductSaleModel productSale :
                                                sku.getProductSale()) {
                                            skuData.setTotalTarget(productSale.getTotalTarget());
                                        }
                                    }

                                    List<MonthWiseTargetData> monthWiseTargetDataSkuList = new ArrayList<>();
                                    List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetList = salesPlanningService.getMonthWiseSkuDetailsBeforeReview(dealerRevisedAnnualSalesModel.getCustomerCode(), skuData.getProductCode(), dealerRevisedAnnualSalesModel.getSubAreaMaster().getPk().toString(), currentUser);
                                    for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : monthWiseAnnualTargetList) {
                                        MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                        if (monthWiseAnnualTargetModel.getProductCode().equals(skuData.getProductCode()) && monthWiseAnnualTargetModel.getIsAnnualSalesRevisedForDealer().equals(true)) {
                                            monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                            monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                            monthWiseTargetDataSkuList.add(monthWiseTargetData);
                                        }
                                    }
                                    skuData.setMonthWiseSkuTarget(monthWiseTargetDataSkuList);
                                    skuDataList.add(skuData);
                                }
                            }
                            annualSalesMonthWiseTargetData.setSkuDataList(skuDataList);
                            annualSalesMonthWiseTargetDataList.add(annualSalesMonthWiseTargetData);
                        }
                        //}
                    }

                        int intervalPeriod = 15;
                        List<SclCustomerModel> dealerDetailsForOnboarded = salesPlanningService.getDealerDetailsForOnboarded(subArea, intervalPeriod);
                        List<SKUData> skuDataListForOnboard = new ArrayList<>();

                        String[] s = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "InvalidNumber"};
                        Map<String, String> results = new LinkedHashMap<>();
                        results.put("4", "0");
                        results.put("5", "0");
                        results.put("6", "0");
                        results.put("7", "0");

                        results.put("8", "0");
                        results.put("9", "0");
                        results.put("10", "0");
                        results.put("11", "0");

                        results.put("12", "0");
                        results.put("1", "0");
                        results.put("2", "0");
                        results.put("3", "0");

                        List<SpOnboardAnnualTargetSettingData> spOnboardAnnualTargetSettingDataList = new ArrayList<>();
                        if (dealerDetailsForOnboarded != null && !dealerDetailsForOnboarded.isEmpty()) {
                            for (SclCustomerModel sclCustomerModel : dealerDetailsForOnboarded) {
                                SpOnboardAnnualTargetSettingData spOnboardAnnualTargetSettingData = new SpOnboardAnnualTargetSettingData();
                                spOnboardAnnualTargetSettingData.setCustomerCode(sclCustomerModel.getUid());
                                spOnboardAnnualTargetSettingData.setCustomerName(sclCustomerModel.getName());
                                spOnboardAnnualTargetSettingData.setCustomerPotential(sclCustomerModel.getCounterPotential() != null ? sclCustomerModel.getCounterPotential() : 0.0);
                                spOnboardAnnualTargetSettingData.setIsNewDealerOnboarded(true);
                                spOnboardAnnualTargetSettingData.setSkuDataList(skuDataListForOnboard);

                                List<MonthWiseTargetData> monthWiseTargetDataListOnboard = new ArrayList<>();
                                for (Map.Entry<String, String> mapEntries : results.entrySet()) {
                                    MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                    String key = mapEntries.getKey();
                                    String value = mapEntries.getValue();
                                    StringBuilder str = new StringBuilder();
                                    if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 12) {
                                        if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 3) {
                                            monthWiseTargetData.setMonthYear(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear() + 1))));
                                        }
                                        if (Integer.parseInt(key) >= 4 && Integer.parseInt(key) <= 12) {
                                            monthWiseTargetData.setMonthYear(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear()))));
                                        }
                                    }
                                    monthWiseTargetData.setMonthTarget(0.0);
                                    monthWiseTargetDataListOnboard.add(monthWiseTargetData);
                                }
                                spOnboardAnnualTargetSettingData.setMonthWiseTarget(monthWiseTargetDataListOnboard);
                                spOnboardAnnualTargetSettingDataList.add(spOnboardAnnualTargetSettingData);
                            }
                        }

                        listData.setOnbordedTargetSetData(spOnboardAnnualTargetSettingDataList);
                        listData.setAnnualSalesMonthWiseTargetData(annualSalesMonthWiseTargetDataList);
                    }
                }
        }
            else
            {
                if (annualSalesModel.getDealerRevisedAnnualSales() != null && Objects.nonNull(annualSalesModel.getDealerRevisedAnnualSales()) &&
                        !annualSalesModel.getDealerRevisedAnnualSales().isEmpty()) {
                    List<AnnualSalesMonthWiseTargetData> annualSalesMonthWiseTargetDataList = new ArrayList<>();
                    List<SpOnboardAnnualTargetSettingData> spOnboardAnnualTargetSettingDataList = new ArrayList<>();
                    for (DealerRevisedAnnualSalesModel dealerRevisedAnnualSalesModel : annualSalesModel.getDealerRevisedAnnualSales()) {
                       // for (SubAreaMasterModel subAreaMasterModel : annualSalesModel.getSubAreaMasterList()) {
                            if(annualSalesModel.getSubAreaMaster()!=null)
                            {
                                SubAreaMasterModel subAreaMasterModel = annualSalesModel.getSubAreaMaster();
                            if (subAreaMasterModel.equals(dealerRevisedAnnualSalesModel.getSubAreaMaster())) {
                                listData.setSubArea(subAreaMasterModel.getTaluka());
                                listData.setSubAreaId(subAreaMasterModel.getPk().toString());
                                if (dealerRevisedAnnualSalesModel.getIsExistingDealerRevisedForReview() != null && dealerRevisedAnnualSalesModel.getIsExistingDealerRevisedForReview().equals(true)) {
                                    AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData = new AnnualSalesMonthWiseTargetData();
                                    annualSalesMonthWiseTargetData.setCustomerCode(dealerRevisedAnnualSalesModel.getCustomerCode());
                                    annualSalesMonthWiseTargetData.setCustomerName(dealerRevisedAnnualSalesModel.getCustomerName());
                                    annualSalesMonthWiseTargetData.setCustomerPotential(dealerRevisedAnnualSalesModel.getCustomerPotential() != null ? dealerRevisedAnnualSalesModel.getCustomerPotential() : 0.0);
                                    annualSalesMonthWiseTargetData.setTotalTarget(dealerRevisedAnnualSalesModel.getTotalTarget());

                                    List<MonthWiseTargetData> monthWiseTargetDataListExisting = new ArrayList<>();
                                    if (dealerRevisedAnnualSalesModel.getMonthWiseAnnualTarget() != null &&
                                            !dealerRevisedAnnualSalesModel.getMonthWiseAnnualTarget().isEmpty()) {
                                        for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : dealerRevisedAnnualSalesModel.getMonthWiseAnnualTarget()) {
                                            MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                            if (monthWiseAnnualTargetModel.getCustomerCode().equals(dealerRevisedAnnualSalesModel.getCustomerCode()) && monthWiseAnnualTargetModel.getIsAnnualSalesReviewedForDealer().equals(true)) {
                                                monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                                monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                                monthWiseTargetDataListExisting.add(monthWiseTargetData);
                                            }
                                        }
                                    }
                                    annualSalesMonthWiseTargetData.setMonthWiseTarget(monthWiseTargetDataListExisting);

                                    List<SKUData> skuDataList = new ArrayList<>();
                                    if (dealerRevisedAnnualSalesModel.getListOfSkus() != null && !dealerRevisedAnnualSalesModel.getListOfSkus().isEmpty()) {
                                        for (ProductModel sku : dealerRevisedAnnualSalesModel.getListOfSkus()) {
                                            SKUData skuData = new SKUData();
                                            skuData.setProductCode(sku.getCode());
                                            skuData.setProductName(sku.getName());

                                            ProductSaleModel productSaleModel = salesPlanningDao.validateReviewForExistingDealerSkuSale(subAreaMaster.getPk().toString(), annualSalesMonthWiseTargetData.getCustomerCode(), sku.getCode(), currentUser);
                                            if (productSaleModel != null) {
                                                skuData.setTotalTarget(productSaleModel.getTotalTarget());
                                            }
                                            List<MonthWiseTargetData> monthWiseTargetDataSkuList = new ArrayList<>();
                                            List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetList = salesPlanningService.getMonthWiseSkuDetailsForReview(dealerRevisedAnnualSalesModel.getCustomerCode(), sku.getCode(), dealerRevisedAnnualSalesModel.getSubAreaMaster().getPk().toString(), currentUser);
                                            for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : monthWiseAnnualTargetList) {
                                                MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                                if (monthWiseAnnualTargetModel.getProductCode().equals(skuData.getProductCode()) && monthWiseAnnualTargetModel.getIsAnnualSalesReviewedForDealer().equals(true)) {
                                                    monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                                    monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                                    monthWiseTargetDataSkuList.add(monthWiseTargetData);
                                                }
                                            }
                                            skuData.setMonthWiseSkuTarget(monthWiseTargetDataSkuList);
                                            skuDataList.add(skuData);
                                        }
                                    }
                                    annualSalesMonthWiseTargetData.setSkuDataList(skuDataList);
                                    annualSalesMonthWiseTargetDataList.add(annualSalesMonthWiseTargetData);
                                }
                                if (dealerRevisedAnnualSalesModel.getIsNewDealerOnboarded() != null && dealerRevisedAnnualSalesModel.getIsNewDealerOnboarded().equals(true)) {
                                    SpOnboardAnnualTargetSettingData onboardedData = new SpOnboardAnnualTargetSettingData();
                                    onboardedData.setCustomerCode(dealerRevisedAnnualSalesModel.getCustomerCode());
                                    onboardedData.setCustomerName(dealerRevisedAnnualSalesModel.getCustomerName());
                                    onboardedData.setCustomerPotential(dealerRevisedAnnualSalesModel.getCustomerPotential());
                                    onboardedData.setTotalTarget(dealerRevisedAnnualSalesModel.getTotalTarget());
                                    List<MonthWiseTargetData> monthWiseListForOnboardedDealer = new ArrayList<>();
                                    if (dealerRevisedAnnualSalesModel.getMonthWiseAnnualTarget() != null && !dealerRevisedAnnualSalesModel.getMonthWiseAnnualTarget().isEmpty()) {
                                        for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : dealerRevisedAnnualSalesModel.getMonthWiseAnnualTarget()) {
                                            if (monthWiseAnnualTargetModel.getCustomerCode().equals(dealerRevisedAnnualSalesModel.getCustomerCode()) && monthWiseAnnualTargetModel.getIsAnnualSalesOnboardedForDealer() != null && monthWiseAnnualTargetModel.getIsAnnualSalesOnboardedForDealer().equals(true)) {
                                                MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                                monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                                monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                                monthWiseListForOnboardedDealer.add(monthWiseTargetData);
                                            }
                                        }
                                    }
                                    onboardedData.setMonthWiseTarget(monthWiseListForOnboardedDealer);

                                    List<SKUData> skuDataListForOnboardDealer = new ArrayList<>();
                                    if (dealerRevisedAnnualSalesModel.getListOfSkus() != null && !dealerRevisedAnnualSalesModel.getListOfSkus().isEmpty()) {
                                        for (ProductModel sku : dealerRevisedAnnualSalesModel.getListOfSkus()) {
                                            SKUData skuData = new SKUData();
                                            ProductSaleModel productSaleModel = getSalesPlanningDao().validateReviewForOnboardedDealerSkuSale(subAreaMaster.getPk().toString(), dealerRevisedAnnualSalesModel.getCustomerCode(), sku.getCode(), currentUser);
                                            skuData.setProductCode(productSaleModel.getProductCode());
                                            skuData.setProductName(productSaleModel.getProductName());
                                            skuData.setTotalTarget(productSaleModel.getTotalTarget());

                                            List<MonthWiseTargetData> monthWiseListForOnboardedDealerForSku = new ArrayList<>();
                                            List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetModelDetails = getSalesPlanningDao().validateReviewForOnboardedDealersSaleSkuForMonthWise(dealerRevisedAnnualSalesModel.getCustomerCode(), sku.getCode(), subAreaMaster.getPk().toString(), currentUser);
                                            if (monthWiseAnnualTargetModelDetails != null && !monthWiseAnnualTargetModelDetails.isEmpty()) {
                                                for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModelDetail : monthWiseAnnualTargetModelDetails) {
                                                    if (monthWiseAnnualTargetModelDetail.getCustomerCode().equals(dealerRevisedAnnualSalesModel.getCustomerCode()) && (monthWiseAnnualTargetModelDetail.getProductCode().equals(sku.getCode())) && monthWiseAnnualTargetModelDetail.getIsAnnualSalesOnboardedForDealer() != null && monthWiseAnnualTargetModelDetail.getIsAnnualSalesOnboardedForDealer().equals(true)) {
                                                        MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                                        monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModelDetail.getMonthYear());
                                                        monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModelDetail.getMonthTarget());
                                                        monthWiseListForOnboardedDealerForSku.add(monthWiseTargetData);
                                                    }
                                                }
                                            }
                                            skuData.setMonthWiseSkuTarget(monthWiseListForOnboardedDealerForSku);
                                            skuDataListForOnboardDealer.add(skuData);
                                        }
                                        onboardedData.setSkuDataList(skuDataListForOnboardDealer);
                                    } else {
                                        onboardedData.setSkuDataList(skuDataListForOnboardDealer);
                                        if (dealerRevisedAnnualSalesModel.getIsNewDealerOnboarded() != null)
                                            onboardedData.setIsNewDealerOnboarded(true);
                                    }
                                    spOnboardAnnualTargetSettingDataList.add(onboardedData);
                                }
                                listData.setOnbordedTargetSetData(spOnboardAnnualTargetSettingDataList);
                                listData.setAnnualSalesMonthWiseTargetData(annualSalesMonthWiseTargetDataList);
                            }
                            // }
                        }
                    }
                }
            }
        }
        else {
            SclCustomerModel sclCustomer = (SclCustomerModel) userService.getUserForUID(filter);
            List<AnnualSalesMonthWiseTargetData> annualSalesMonthWiseTargetDataList = new ArrayList<>();
            if (annualSalesModel.getIsAnnualSalesReviewedForDealer() != null && annualSalesModel.getIsAnnualSalesReviewedForDealer().equals(false))
            {
                DealerRevisedAnnualSalesModel dealerRevisedAnnualSalesModel = salesPlanningDao.findDealerRevisedDetailsByCustomerCode(sclCustomer.getUid(), subAreaMaster.getPk().toString(), currentUser, findNextFinancialYear());
            if (dealerRevisedAnnualSalesModel != null) {
              //  for (SubAreaMasterModel subAreaMasterModel : annualSalesModel.getSubAreaMasterList()) {
                if(annualSalesModel.getSubAreaMaster()!=null)
                {
                    SubAreaMasterModel subAreaMasterModel = annualSalesModel.getSubAreaMaster();
                    if (subAreaMasterModel.equals(dealerRevisedAnnualSalesModel.getSubAreaMaster())) {
                        listData.setSubArea(subAreaMasterModel.getTaluka());
                        listData.setSubAreaId(subAreaMasterModel.getPk().toString());
                        AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData = new AnnualSalesMonthWiseTargetData();
                        annualSalesMonthWiseTargetData.setCustomerCode(dealerRevisedAnnualSalesModel.getCustomerCode());
                        annualSalesMonthWiseTargetData.setCustomerName(dealerRevisedAnnualSalesModel.getCustomerName());
                        annualSalesMonthWiseTargetData.setCustomerPotential(dealerRevisedAnnualSalesModel.getCustomerPotential() != null ? dealerRevisedAnnualSalesModel.getCustomerPotential() : 0.0);
                        annualSalesMonthWiseTargetData.setTotalTarget(dealerRevisedAnnualSalesModel.getTotalTarget());

                        if (dealerRevisedAnnualSalesModel.getMonthWiseAnnualTarget() != null &&
                                !dealerRevisedAnnualSalesModel.getMonthWiseAnnualTarget().isEmpty()) {
                            List<MonthWiseTargetData> monthWiseTargetDataList = new ArrayList<>();
                            for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : dealerRevisedAnnualSalesModel.getMonthWiseAnnualTarget()) {
                                MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                if (monthWiseAnnualTargetModel.getCustomerCode().equals(dealerRevisedAnnualSalesModel.getCustomerCode()) && monthWiseAnnualTargetModel.getIsAnnualSalesRevisedForDealer().equals(true)) {
                                    monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                    monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                    monthWiseTargetDataList.add(monthWiseTargetData);
                                }
                            }
                            annualSalesMonthWiseTargetData.setMonthWiseTarget(monthWiseTargetDataList);
                        }


                        List<SKUData> skuDataList = new ArrayList<>();
                        if (dealerRevisedAnnualSalesModel.getListOfSkus() != null && !dealerRevisedAnnualSalesModel.getListOfSkus().isEmpty()) {
                            for (ProductModel sku : dealerRevisedAnnualSalesModel.getListOfSkus()) {
                                SKUData skuData = new SKUData();
                                skuData.setProductCode(sku.getCode());
                                skuData.setProductName(sku.getName());
                                if (sku.getProductSale() != null && !sku.getProductSale().isEmpty()) {
                                    for (ProductSaleModel productSale :
                                            sku.getProductSale()) {
                                        skuData.setTotalTarget(productSale.getTotalTarget());
                                    }
                                }

                                List<MonthWiseTargetData> monthWiseTargetDataSkuList = new ArrayList<>();
                                List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetList = salesPlanningService.getMonthWiseSkuDetailsBeforeReview(dealerRevisedAnnualSalesModel.getCustomerCode(), skuData.getProductCode(), dealerRevisedAnnualSalesModel.getSubAreaMaster().getPk().toString(), currentUser);
                                for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : monthWiseAnnualTargetList) {
                                    MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                    if (monthWiseAnnualTargetModel.getProductCode().equals(skuData.getProductCode()) && monthWiseAnnualTargetModel.getIsAnnualSalesRevisedForDealer().equals(true)) {
                                        monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                        monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                        monthWiseTargetDataSkuList.add(monthWiseTargetData);
                                    }
                                }
                                skuData.setMonthWiseSkuTarget(monthWiseTargetDataSkuList);
                                skuDataList.add(skuData);
                            }
                        }
                        annualSalesMonthWiseTargetData.setSkuDataList(skuDataList);
                        annualSalesMonthWiseTargetDataList.add(annualSalesMonthWiseTargetData);
                    }
                    // }
                }
            }
        }
            else
            {
                DealerRevisedAnnualSalesModel dealerRevisedAnnualSalesModel = salesPlanningDao.findDealerRevisedDetailsByCustomerCode(sclCustomer.getUid(), subAreaMaster.getPk().toString(), currentUser, findNextFinancialYear());
                if (dealerRevisedAnnualSalesModel != null && dealerRevisedAnnualSalesModel.getIsExistingDealerRevisedForReview()!=null && dealerRevisedAnnualSalesModel.getIsExistingDealerRevisedForReview().equals(true)) {
                    //for (SubAreaMasterModel subAreaMasterModel : annualSalesModel.getSubAreaMasterList()) {
                    if(annualSalesModel.getSubAreaMaster()!=null)
                    {
                        SubAreaMasterModel subAreaMasterModel = annualSalesModel.getSubAreaMaster();
                        if (subAreaMasterModel.equals(dealerRevisedAnnualSalesModel.getSubAreaMaster())) {
                            listData.setSubArea(subAreaMasterModel.getTaluka());
                            listData.setSubAreaId(subAreaMasterModel.getPk().toString());
                            AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData = new AnnualSalesMonthWiseTargetData();
                            annualSalesMonthWiseTargetData.setCustomerCode(dealerRevisedAnnualSalesModel.getCustomerCode());
                            annualSalesMonthWiseTargetData.setCustomerName(dealerRevisedAnnualSalesModel.getCustomerName());
                            annualSalesMonthWiseTargetData.setCustomerPotential(dealerRevisedAnnualSalesModel.getCustomerPotential() != null ? dealerRevisedAnnualSalesModel.getCustomerPotential() : 0.0);
                            annualSalesMonthWiseTargetData.setTotalTarget(dealerRevisedAnnualSalesModel.getTotalTarget());

                            List<MonthWiseTargetData> monthWiseTargetDataList = new ArrayList<>();
                            if (dealerRevisedAnnualSalesModel.getMonthWiseAnnualTarget() != null &&
                                    !dealerRevisedAnnualSalesModel.getMonthWiseAnnualTarget().isEmpty()) {
                                for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : dealerRevisedAnnualSalesModel.getMonthWiseAnnualTarget()) {
                                    MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                    if (monthWiseAnnualTargetModel.getCustomerCode().equals(dealerRevisedAnnualSalesModel.getCustomerCode()) && monthWiseAnnualTargetModel.getIsAnnualSalesRevisedForDealer().equals(true)) {
                                        monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                        monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                        monthWiseTargetDataList.add(monthWiseTargetData);
                                    }
                                }
                            }
                            annualSalesMonthWiseTargetData.setMonthWiseTarget(monthWiseTargetDataList);

                            List<SKUData> skuDataList = new ArrayList<>();
                            if (dealerRevisedAnnualSalesModel.getListOfSkus() != null && !dealerRevisedAnnualSalesModel.getListOfSkus().isEmpty()) {
                                for (ProductModel sku : dealerRevisedAnnualSalesModel.getListOfSkus()) {
                                    SKUData skuData = new SKUData();
                                    skuData.setProductCode(sku.getCode());
                                    skuData.setProductName(sku.getName());
                                    if (sku.getProductSale() != null && !sku.getProductSale().isEmpty()) {
                                        for (ProductSaleModel productSale :
                                                sku.getProductSale()) {
                                            skuData.setTotalTarget(productSale.getTotalTarget());
                                        }
                                    }

                                    List<MonthWiseTargetData> monthWiseTargetDataSkuList = new ArrayList<>();
                                    List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetList = salesPlanningService.getMonthWiseSkuDetailsForReview(dealerRevisedAnnualSalesModel.getCustomerCode(), skuData.getProductCode(), dealerRevisedAnnualSalesModel.getSubAreaMaster().getPk().toString(), currentUser);
                                    for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : monthWiseAnnualTargetList) {
                                        MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                        if (monthWiseAnnualTargetModel.getProductCode().equals(skuData.getProductCode()) && monthWiseAnnualTargetModel.getIsAnnualSalesRevisedForDealer().equals(true)) {
                                            monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                            monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                            monthWiseTargetDataSkuList.add(monthWiseTargetData);
                                        }
                                    }
                                    skuData.setMonthWiseSkuTarget(monthWiseTargetDataSkuList);
                                    skuDataList.add(skuData);
                                }
                            }
                            annualSalesMonthWiseTargetData.setSkuDataList(skuDataList);
                            annualSalesMonthWiseTargetDataList.add(annualSalesMonthWiseTargetData);
                        }
                    }
                }
            }
                    listData.setAnnualSalesMonthWiseTargetData(annualSalesMonthWiseTargetDataList);
        }
        return listData;
    }

    @Override
    public AnnualSalesMonthWiseTargetListData viewReviewedSalesforRetailesMonthwise(String subArea, String filter) {
        SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
        BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
        AnnualSalesModel annualSalesMonthwiseModel = salesPlanningService.viewPlannedSalesforDealersRetailersMonthWise(subArea, currentUser,brand);
        SubAreaMasterModel subAreaMaster = territoryManagementService.getTerritoryById(subArea);
        AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData = new AnnualSalesMonthWiseTargetListData();
        annualSalesMonthWiseTargetListData.setSubArea(subAreaMaster.getTaluka());
        annualSalesMonthWiseTargetListData.setSubAreaId(subAreaMaster.getPk().toString());
        if (StringUtils.isBlank(filter)) {
            if (annualSalesMonthwiseModel!=null && annualSalesMonthwiseModel.getIsAnnualSalesReviewedForRetailer() != null && annualSalesMonthwiseModel.getIsAnnualSalesReviewedForRetailer().equals(false)) {
                if (annualSalesMonthwiseModel.getRetailerRevisedAnnualSales() != null && Objects.nonNull(annualSalesMonthwiseModel.getRetailerRevisedAnnualSales()) &&
                        !annualSalesMonthwiseModel.getRetailerRevisedAnnualSales().isEmpty()) {
                    List<AnnualSalesMonthWiseTargetData> annualSalesMonthWiseTargetDataList = new ArrayList<>();
                    for (RetailerRevisedAnnualSalesModel retailerRevisedAnnualSalesModel : annualSalesMonthwiseModel.getRetailerRevisedAnnualSales()) {
                        if (annualSalesMonthwiseModel.getSubAreaMaster()!=null) {
                            SubAreaMasterModel subAreaMasterModel=annualSalesMonthwiseModel.getSubAreaMaster();
                            AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData = new AnnualSalesMonthWiseTargetData();
                            if (subAreaMasterModel.equals(retailerRevisedAnnualSalesModel.getSubAreaMaster())) {
                                annualSalesMonthWiseTargetListData.setSubArea(subAreaMasterModel.getTaluka());
                                annualSalesMonthWiseTargetListData.setSubAreaId(subAreaMasterModel.getPk().toString());
                                annualSalesMonthWiseTargetData.setCustomerCode(retailerRevisedAnnualSalesModel.getCustomerCode());
                                annualSalesMonthWiseTargetData.setCustomerName(retailerRevisedAnnualSalesModel.getCustomerName());
                                annualSalesMonthWiseTargetData.setCustomerPotential(retailerRevisedAnnualSalesModel.getCustomerPotential() != null ? retailerRevisedAnnualSalesModel.getCustomerPotential() : 0.0);
                                annualSalesMonthWiseTargetData.setTotalTarget(retailerRevisedAnnualSalesModel.getTotalTarget());
                                annualSalesMonthWiseTargetData.setErpCustomerNo(retailerRevisedAnnualSalesModel.getErpCustomerNo());

                                List<MonthWiseTargetData> monthWiseTargetDataList = new ArrayList<>();
                                if (retailerRevisedAnnualSalesModel.getMonthWiseAnnualTarget() != null &&
                                        !retailerRevisedAnnualSalesModel.getMonthWiseAnnualTarget().isEmpty()) {
                                    for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : retailerRevisedAnnualSalesModel.getMonthWiseAnnualTarget()) {
                                        MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                        if (monthWiseAnnualTargetModel.getCustomerCode().equals(retailerRevisedAnnualSalesModel.getCustomerCode()) &&
                                                monthWiseAnnualTargetModel.getIsAnnualSalesRevisedForRetailer().equals(true)) {
                                            monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                            monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                            monthWiseTargetDataList.add(monthWiseTargetData);
                                        }
                                    }
                                }
                                annualSalesMonthWiseTargetData.setMonthWiseTarget(monthWiseTargetDataList);

                                List<RetailerDetailsData> retailerRevisedAnnualSalesDetailsList = new ArrayList<>();
                                if (retailerRevisedAnnualSalesModel.getRetailerRevisedAnnualSalesDetails() != null && !retailerRevisedAnnualSalesModel.getRetailerRevisedAnnualSalesDetails().isEmpty()) {
                                    for (RetailerRevisedAnnualSalesDetailsModel retailerRevisedAnnualSalesDetail : retailerRevisedAnnualSalesModel.getRetailerRevisedAnnualSalesDetails()) {
                                        RetailerDetailsData retailerDetailsData = new RetailerDetailsData();
                                        retailerDetailsData.setCustomerCode(retailerRevisedAnnualSalesDetail.getCustomerCode());
                                        retailerDetailsData.setCustomerName(retailerRevisedAnnualSalesDetail.getCustomerName());
                                        retailerDetailsData.setCustomerPotential(retailerRevisedAnnualSalesDetail.getCustomerPotential() != null ? retailerRevisedAnnualSalesDetail.getCustomerPotential() : 0.0);
                                        retailerDetailsData.setTotalTarget(retailerRevisedAnnualSalesDetail.getTotalTarget());
                                        retailerDetailsData.setErpCustomerNo(retailerRevisedAnnualSalesDetail.getErpCustomerNo());

                                        List<MonthWiseTargetData> monthWiseTargetDataListforRetailer = new ArrayList<>();
                                        List<MonthWiseAnnualTargetModel> monthWiseSkuDetailsBeforeReview = salesPlanningService.getMonthWiseRetailerDetailsBeforeReview(retailerRevisedAnnualSalesModel.getCustomerCode(), retailerRevisedAnnualSalesDetail.getCustomerCode(), retailerRevisedAnnualSalesModel.getSubAreaMaster().getPk().toString(), currentUser);
                                        if (retailerRevisedAnnualSalesDetail.getMonthWiseAnnualTarget() != null && !retailerRevisedAnnualSalesDetail.getMonthWiseAnnualTarget().isEmpty())
                                            for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : monthWiseSkuDetailsBeforeReview) {
                                                if (monthWiseAnnualTargetModel.getRetailerCode().equalsIgnoreCase(retailerRevisedAnnualSalesDetail.getCustomerCode())
                                                        && monthWiseAnnualTargetModel.getIsAnnualSalesRevisedForRetailer().equals(true)) {
                                                    MonthWiseTargetData monthWiseTargetDataforRet = new MonthWiseTargetData();
                                                    monthWiseTargetDataforRet.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                                    monthWiseTargetDataforRet.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                                    monthWiseTargetDataListforRetailer.add(monthWiseTargetDataforRet);
                                                }
                                            }
                                        retailerDetailsData.setMonthWiseSkuTarget(monthWiseTargetDataListforRetailer);
                                        retailerRevisedAnnualSalesDetailsList.add(retailerDetailsData);
                                    }
                                }
                                annualSalesMonthWiseTargetData.setRetailerData(retailerRevisedAnnualSalesDetailsList);
                                annualSalesMonthWiseTargetDataList.add(annualSalesMonthWiseTargetData);

                                if (retailerRevisedAnnualSalesModel.getDealerSelfCounterSale() != null) {
                                    SelfCounterSaleDetailsModel dealerSelfCounterSale = retailerRevisedAnnualSalesModel.getDealerSelfCounterSale();
                                    if (dealerSelfCounterSale.getIsAnnualSalesRevisedForRetailer().equals(true)) {
                                        SelfCounterSaleData selfCounterSaleData = new SelfCounterSaleData();
                                        selfCounterSaleData.setCustomerCode(dealerSelfCounterSale.getCustomerCode());
                                        selfCounterSaleData.setCustomerName(dealerSelfCounterSale.getCustomerName());
                                        selfCounterSaleData.setCustomerPotential(dealerSelfCounterSale.getCustomerPotential() != null ? dealerSelfCounterSale.getCustomerPotential() : 0.0);
                                        selfCounterSaleData.setTotalTarget(dealerSelfCounterSale.getTotalTarget());
                                        selfCounterSaleData.setErpCustomerNo(dealerSelfCounterSale.getErpCustomerNo());

                                        List<MonthWiseTargetData> monthWiseSelfCounterList = new ArrayList<>();
                                        if (dealerSelfCounterSale.getMonthWiseAnnualTarget() != null && !dealerSelfCounterSale.getMonthWiseAnnualTarget().isEmpty())
                                            for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : dealerSelfCounterSale.getMonthWiseAnnualTarget()) {
                                                if (monthWiseAnnualTargetModel.getSelfCounterCustomerCode().equals(dealerSelfCounterSale.getCustomerCode()) && monthWiseAnnualTargetModel.getIsAnnualSalesRevisedForRetailer().equals(true)) {
                                                    MonthWiseTargetData monthWiseTargetDataforRet = new MonthWiseTargetData();
                                                    monthWiseTargetDataforRet.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                                    monthWiseTargetDataforRet.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                                    monthWiseSelfCounterList.add(monthWiseTargetDataforRet);
                                                }
                                            }
                                        selfCounterSaleData.setMonthWiseTarget(monthWiseSelfCounterList);
                                        annualSalesMonthWiseTargetData.setSelfCounterSale(selfCounterSaleData);
                                    }
                                }
                            }
                        }
                            int intervalPeriod = 15;
                            List<SclCustomerModel> retailerDetailsForOnboarded = salesPlanningService.getRetailerDetailsForOnboarded(subArea, intervalPeriod);
                            List<RetailerDetailsData> retailerDetailsListForOnboard = new ArrayList<>();

                            String[] s = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "InvalidNumber"};
                            Map<String, String> results = new LinkedHashMap<>();
                            results.put("4", "0");
                            results.put("5", "0");
                            results.put("6", "0");
                            results.put("7", "0");

                            results.put("8", "0");
                            results.put("9", "0");
                            results.put("10", "0");
                            results.put("11", "0");

                            results.put("12", "0");
                            results.put("1", "0");
                            results.put("2", "0");
                            results.put("3", "0");

                            List<SpOnboardAnnualTargetSettingData> spOnboardAnnualTargetSettingDataList = new ArrayList<>();
                            if (retailerDetailsForOnboarded != null && !retailerDetailsForOnboarded.isEmpty()) {
                                for (SclCustomerModel sclCustomerModel : retailerDetailsForOnboarded) {
                                    SpOnboardAnnualTargetSettingData spOnboardAnnualTargetSettingData = new SpOnboardAnnualTargetSettingData();
                                    spOnboardAnnualTargetSettingData.setCustomerCode(sclCustomerModel.getUid());
                                    spOnboardAnnualTargetSettingData.setCustomerName(sclCustomerModel.getName());
                                    spOnboardAnnualTargetSettingData.setErpCustomerNo(sclCustomerModel.getCustomerNo());
                                    spOnboardAnnualTargetSettingData.setCustomerPotential(sclCustomerModel.getCounterPotential() != null ? sclCustomerModel.getCounterPotential() : 0.0);
                                    spOnboardAnnualTargetSettingData.setIsNewDealerOnboarded(true);
                                    spOnboardAnnualTargetSettingData.setRetailerData(retailerDetailsListForOnboard);

                                    List<MonthWiseTargetData> monthWiseTargetDataListOnboard = new ArrayList<>();
                                    for (Map.Entry<String, String> mapEntries : results.entrySet()) {
                                        MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                        String key = mapEntries.getKey();
                                        StringBuilder str = new StringBuilder();
                                        if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 12) {
                                            if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 3) {
                                                monthWiseTargetData.setMonthYear(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear() + 1))));
                                            }
                                            if (Integer.parseInt(key) >= 4 && Integer.parseInt(key) <= 12) {
                                                monthWiseTargetData.setMonthYear(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear()))));
                                            }
                                        }
                                        monthWiseTargetData.setMonthTarget(0.0);
                                        monthWiseTargetDataListOnboard.add(monthWiseTargetData);
                                    }
                                    spOnboardAnnualTargetSettingData.setMonthWiseTarget(monthWiseTargetDataListOnboard);
                                    spOnboardAnnualTargetSettingDataList.add(spOnboardAnnualTargetSettingData);
                                }
                            }
                        }
                    }
               }
            else {
                if (annualSalesMonthwiseModel.getRetailerRevisedAnnualSales() != null && Objects.nonNull(annualSalesMonthwiseModel.getRetailerRevisedAnnualSales()) &&
                        !annualSalesMonthwiseModel.getRetailerRevisedAnnualSales().isEmpty()) {
                    List<AnnualSalesMonthWiseTargetData> annualSalesMonthWiseTargetDataList = new ArrayList<>();
                    List<SpOnboardAnnualTargetSettingData> spOnboardAnnualTargetSettingDataList = new ArrayList<>();
                    for (RetailerRevisedAnnualSalesModel retailerRevisedAnnualSalesModel : annualSalesMonthwiseModel.getRetailerRevisedAnnualSales()) {
                        for (SubAreaMasterModel subAreaMasterModel : annualSalesMonthwiseModel.getSubAreaMasterList()) {
                            if (subAreaMasterModel.equals(retailerRevisedAnnualSalesModel.getSubAreaMaster())) {
                                if (retailerRevisedAnnualSalesModel.getIsExistingRetailerRevisedForReview() != null && retailerRevisedAnnualSalesModel.getIsExistingRetailerRevisedForReview().equals(true)) {
                                    AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData = new AnnualSalesMonthWiseTargetData();
                                    annualSalesMonthWiseTargetData.setCustomerCode(retailerRevisedAnnualSalesModel.getCustomerCode());
                                    annualSalesMonthWiseTargetData.setCustomerName(retailerRevisedAnnualSalesModel.getCustomerName());
                                    annualSalesMonthWiseTargetData.setCustomerPotential(retailerRevisedAnnualSalesModel.getCustomerPotential() != null ? retailerRevisedAnnualSalesModel.getCustomerPotential() : 0.0);
                                    annualSalesMonthWiseTargetData.setTotalTarget(retailerRevisedAnnualSalesModel.getTotalTarget());
                                    annualSalesMonthWiseTargetData.setErpCustomerNo(retailerRevisedAnnualSalesModel.getErpCustomerNo());

                                    List<MonthWiseTargetData> monthWiseTargetDataListExisting = new ArrayList<>();
                                    if (retailerRevisedAnnualSalesModel.getMonthWiseAnnualTarget() != null &&
                                            !retailerRevisedAnnualSalesModel.getMonthWiseAnnualTarget().isEmpty()) {
                                        for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : retailerRevisedAnnualSalesModel.getMonthWiseAnnualTarget()) {
                                            MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                            if (monthWiseAnnualTargetModel.getCustomerCode().equals(retailerRevisedAnnualSalesModel.getCustomerCode()) && monthWiseAnnualTargetModel.getIsAnnualSalesReviewedForRetailer().equals(true)) {
                                                monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                                monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                                monthWiseTargetDataListExisting.add(monthWiseTargetData);
                                            }
                                        }
                                    }
                                    annualSalesMonthWiseTargetData.setMonthWiseTarget(monthWiseTargetDataListExisting);

                                    List<RetailerDetailsData> retailerRevisedAnnualSalesDetailsList = new ArrayList<>();
                                    if (retailerRevisedAnnualSalesModel.getRetailerRevisedAnnualSalesDetails() != null && !retailerRevisedAnnualSalesModel.getRetailerRevisedAnnualSalesDetails().isEmpty()) {
                                        for (RetailerRevisedAnnualSalesDetailsModel retailerRevisedAnnualSalesDetail : retailerRevisedAnnualSalesModel.getRetailerRevisedAnnualSalesDetails()) {
                                            RetailerDetailsData retailerDetailsData = new RetailerDetailsData();
                                            retailerDetailsData.setCustomerCode(retailerRevisedAnnualSalesDetail.getCustomerCode());
                                            retailerDetailsData.setCustomerName(retailerRevisedAnnualSalesDetail.getCustomerName());
                                            retailerDetailsData.setCustomerPotential(retailerRevisedAnnualSalesDetail.getCustomerPotential() != null ? retailerRevisedAnnualSalesDetail.getCustomerPotential() : 0.0);
                                            retailerDetailsData.setTotalTarget(retailerRevisedAnnualSalesDetail.getTotalTarget());
                                            retailerDetailsData.setErpCustomerNo(retailerRevisedAnnualSalesDetail.getErpCustomerNo());
                                            List<MonthWiseTargetData> monthWiseTargetDataListforRetailer = new ArrayList<>();
                                            List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetList = salesPlanningService.getMonthWiseRetailerDetailsForReview(retailerRevisedAnnualSalesModel.getCustomerCode(), retailerRevisedAnnualSalesDetail.getCustomerCode(), retailerRevisedAnnualSalesModel.getSubAreaMaster().getPk().toString(), currentUser);
                                                if(monthWiseAnnualTargetList!=null && !monthWiseAnnualTargetList.isEmpty()) {
                                                    for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : monthWiseAnnualTargetList) {
                                                        if (monthWiseAnnualTargetModel.getRetailerCode().equalsIgnoreCase(retailerRevisedAnnualSalesDetail.getCustomerCode()) && monthWiseAnnualTargetModel.getIsAnnualSalesReviewedForRetailerDetails().equals(true)) {
                                                            MonthWiseTargetData monthWiseTargetDataforRet = new MonthWiseTargetData();
                                                            monthWiseTargetDataforRet.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                                            monthWiseTargetDataforRet.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                                            monthWiseTargetDataListforRetailer.add(monthWiseTargetDataforRet);
                                                        }
                                                    }
                                                }
                                            retailerDetailsData.setMonthWiseSkuTarget(monthWiseTargetDataListforRetailer);
                                            retailerRevisedAnnualSalesDetailsList.add(retailerDetailsData);
                                        }
                                    }
                                    annualSalesMonthWiseTargetData.setRetailerData(retailerRevisedAnnualSalesDetailsList);

                                    if (retailerRevisedAnnualSalesModel.getDealerSelfCounterSale() != null) {
                                        SelfCounterSaleDetailsModel dealerSelfCounterSale = retailerRevisedAnnualSalesModel.getDealerSelfCounterSale();
                                        SelfCounterSaleData selfCounterSaleData = new SelfCounterSaleData();
                                        selfCounterSaleData.setCustomerCode(dealerSelfCounterSale.getCustomerCode());
                                        selfCounterSaleData.setCustomerName(dealerSelfCounterSale.getCustomerName());
                                        selfCounterSaleData.setCustomerPotential(dealerSelfCounterSale.getCustomerPotential() != null ? dealerSelfCounterSale.getCustomerPotential() : 0.0);
                                        selfCounterSaleData.setTotalTarget(dealerSelfCounterSale.getTotalTarget());
                                        selfCounterSaleData.setErpCustomerNo(dealerSelfCounterSale.getErpCustomerNo());

                                        List<MonthWiseTargetData> monthWiseSelfCounterList = new ArrayList<>();
                                        if (dealerSelfCounterSale.getMonthWiseAnnualTarget() != null && !dealerSelfCounterSale.getMonthWiseAnnualTarget().isEmpty())
                                            for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : dealerSelfCounterSale.getMonthWiseAnnualTarget()) {
                                                if (monthWiseAnnualTargetModel.getIsAnnualSalesReviewedForRetailer().equals(true)) {
                                                    MonthWiseTargetData monthWiseTargetDataforRet = new MonthWiseTargetData();
                                                    monthWiseTargetDataforRet.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                                    monthWiseTargetDataforRet.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                                    monthWiseSelfCounterList.add(monthWiseTargetDataforRet);
                                                }
                                            }
                                        selfCounterSaleData.setMonthWiseTarget(monthWiseSelfCounterList);
                                        annualSalesMonthWiseTargetData.setSelfCounterSale(selfCounterSaleData);
                                    }
                                    annualSalesMonthWiseTargetDataList.add(annualSalesMonthWiseTargetData);
                                }

                                //need to check below condition
                                if (retailerRevisedAnnualSalesModel.getIsNewRetailerOnboarded() != null && retailerRevisedAnnualSalesModel.getIsNewRetailerOnboarded().equals(true)) {
                                    SpOnboardAnnualTargetSettingData onboardedData = new SpOnboardAnnualTargetSettingData();
                                    onboardedData.setCustomerCode(retailerRevisedAnnualSalesModel.getCustomerCode());
                                    onboardedData.setCustomerName(retailerRevisedAnnualSalesModel.getCustomerName());
                                    onboardedData.setCustomerPotential(retailerRevisedAnnualSalesModel.getCustomerPotential());
                                    onboardedData.setTotalTarget(retailerRevisedAnnualSalesModel.getTotalTarget());
                                    onboardedData.setErpCustomerNo(retailerRevisedAnnualSalesModel.getErpCustomerNo());

                                    List<MonthWiseTargetData> monthWiseListForOnboardedRetailer = new ArrayList<>();
                                    if (retailerRevisedAnnualSalesModel.getMonthWiseAnnualTarget() != null && !retailerRevisedAnnualSalesModel.getMonthWiseAnnualTarget().isEmpty()) {
                                        for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : retailerRevisedAnnualSalesModel.getMonthWiseAnnualTarget()) {
                                            if (monthWiseAnnualTargetModel.getCustomerCode().equals(retailerRevisedAnnualSalesModel.getCustomerCode()) && monthWiseAnnualTargetModel.getIsAnnualSalesOnboardedForRetailer() != null && monthWiseAnnualTargetModel.getIsAnnualSalesOnboardedForRetailer().equals(true)) {
                                                MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                                monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                                monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                                monthWiseListForOnboardedRetailer.add(monthWiseTargetData);
                                            }
                                        }
                                    }
                                    onboardedData.setMonthWiseTarget(monthWiseListForOnboardedRetailer);

                                    List<RetailerDetailsData> retailerDetailsDataList = new ArrayList<>();
                                    if (retailerRevisedAnnualSalesModel.getListOfRetailersRevised() != null && !retailerRevisedAnnualSalesModel.getListOfRetailersRevised().isEmpty()) {
                                        for (RetailerRevisedAnnualSalesDetailsModel revisedAnnualSalesDetailsModel:retailerRevisedAnnualSalesModel.getListOfRetailersRevised()) {
                                            RetailerDetailsData retailerDetailsData = new RetailerDetailsData();

                                            retailerDetailsData.setCustomerCode(revisedAnnualSalesDetailsModel.getCustomerCode());
                                            retailerDetailsData.setCustomerName(revisedAnnualSalesDetailsModel.getCustomerName());
                                            retailerDetailsData.setCustomerPotential(revisedAnnualSalesDetailsModel.getCustomerPotential());
                                            retailerDetailsData.setErpCustomerNo(revisedAnnualSalesDetailsModel.getErpCustomerNo());
                                            retailerDetailsData.setTotalTarget(revisedAnnualSalesDetailsModel.getTotalTarget());

                                            List<MonthWiseTargetData> monthWiseListForOnboardedDealerForSku = new ArrayList<>();
                                            List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetModelDetails = getSalesPlanningDao().validateReviewForOnboardedRetailerSaleForMonthWise(retailerRevisedAnnualSalesModel.getCustomerCode(), revisedAnnualSalesDetailsModel.getCustomerCode(), subAreaMaster.getPk().toString(), currentUser);
                                            if (monthWiseAnnualTargetModelDetails != null && !monthWiseAnnualTargetModelDetails.isEmpty()) {
                                                for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModelDetail : monthWiseAnnualTargetModelDetails) {
                                                    if(monthWiseAnnualTargetModelDetail.getCustomerCode().equals(retailerRevisedAnnualSalesModel.getCustomerCode()) && (monthWiseAnnualTargetModelDetail.getRetailerCode().equals(revisedAnnualSalesDetailsModel.getCustomerCode())) && monthWiseAnnualTargetModelDetail.getIsAnnualSalesOnboardedForRetailer() != null && monthWiseAnnualTargetModelDetail.getIsAnnualSalesOnboardedForRetailer().equals(true))
                                                    {
                                                        MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                                        monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModelDetail.getMonthYear());
                                                        monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModelDetail.getMonthTarget());
                                                        monthWiseListForOnboardedDealerForSku.add(monthWiseTargetData);
                                                    }
                                                }
                                            }
                                            retailerDetailsData.setMonthWiseSkuTarget(monthWiseListForOnboardedDealerForSku);
                                            retailerDetailsDataList.add(retailerDetailsData);
                                        }
                                        onboardedData.setRetailerData(retailerDetailsDataList);
                                    }

                                    spOnboardAnnualTargetSettingDataList.add(onboardedData);
                                }
                            }
                        }
                    }
                }
            }
        }
        else {
            SclCustomerModel sclCustomer = (SclCustomerModel) userService.getUserForUID(filter);
            List<AnnualSalesMonthWiseTargetData> annualSalesMonthWiseTargetDataList = new ArrayList<>();
            if (annualSalesMonthwiseModel.getIsAnnualSalesReviewedForDealer() != null && annualSalesMonthwiseModel.getIsAnnualSalesReviewedForDealer().equals(false))
            {
                RetailerRevisedAnnualSalesModel retailerRevisedAnnualSalesModel = salesPlanningDao.findRetailerRevisedDetailsByCustomerCode(sclCustomer.getUid(), subAreaMaster.getPk().toString(), currentUser, findNextFinancialYear());
                if (retailerRevisedAnnualSalesModel != null) {
                    for (SubAreaMasterModel subAreaMasterModel : annualSalesMonthwiseModel.getSubAreaMasterList()) {
                        if (subAreaMasterModel.equals(retailerRevisedAnnualSalesModel.getSubAreaMaster())) {
                           /* listData.setSubArea(subAreaMasterModel.getTaluka());
                            listData.setSubAreaId(subAreaMasterModel.getPk().toString());*/
                            AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData = new AnnualSalesMonthWiseTargetData();
                            annualSalesMonthWiseTargetData.setCustomerCode(retailerRevisedAnnualSalesModel.getCustomerCode());
                            annualSalesMonthWiseTargetData.setCustomerName(retailerRevisedAnnualSalesModel.getCustomerName());
                            annualSalesMonthWiseTargetData.setCustomerPotential(retailerRevisedAnnualSalesModel.getCustomerPotential() != null ? retailerRevisedAnnualSalesModel.getCustomerPotential() : 0.0);
                            annualSalesMonthWiseTargetData.setTotalTarget(retailerRevisedAnnualSalesModel.getTotalTarget());

                            if (retailerRevisedAnnualSalesModel.getMonthWiseAnnualTarget() != null &&
                                    !retailerRevisedAnnualSalesModel.getMonthWiseAnnualTarget().isEmpty()) {
                                List<MonthWiseTargetData> monthWiseTargetDataList = new ArrayList<>();
                                for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : retailerRevisedAnnualSalesModel.getMonthWiseAnnualTarget()) {
                                    MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                    if (monthWiseAnnualTargetModel.getCustomerCode().equals(retailerRevisedAnnualSalesModel.getCustomerCode()) && monthWiseAnnualTargetModel.getIsAnnualSalesRevisedForRetailer().equals(true)) {
                                        monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                        monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                        monthWiseTargetDataList.add(monthWiseTargetData);
                                    }
                                }
                                annualSalesMonthWiseTargetData.setMonthWiseTarget(monthWiseTargetDataList);
                            }

                            List<RetailerDetailsData> retailerDetailsDataList = new ArrayList<>();
                           // List<SKUData> skuDataList = new ArrayList<>();
                            if (retailerRevisedAnnualSalesModel.getListOfRetailersRevised() != null && !retailerRevisedAnnualSalesModel.getListOfRetailersRevised().isEmpty()) {
                                for (RetailerRevisedAnnualSalesDetailsModel revisedAnnualSalesDetailsModel: retailerRevisedAnnualSalesModel.getListOfRetailersRevised()) {
                                    RetailerDetailsData retailerDetailsData = new RetailerDetailsData();
                                    retailerDetailsData.setCustomerCode(revisedAnnualSalesDetailsModel.getCustomerCode());
                                    retailerDetailsData.setErpCustomerNo(revisedAnnualSalesDetailsModel.getErpCustomerNo());
                                    retailerDetailsData.setCustomerName(revisedAnnualSalesDetailsModel.getCustomerName());
                                    retailerDetailsData.setTotalTarget(revisedAnnualSalesDetailsModel.getTotalTarget());
                                    retailerDetailsData.setCustomerPotential(revisedAnnualSalesDetailsModel.getCustomerPotential());

                                    List<MonthWiseTargetData> monthWiseTargetDataSkuList = new ArrayList<>();
                                    List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetList = salesPlanningService.getMonthWiseRetailerDetailsBeforeReview(retailerRevisedAnnualSalesModel.getCustomerCode(), revisedAnnualSalesDetailsModel.getCustomerCode(), retailerRevisedAnnualSalesModel.getSubAreaMaster().getPk().toString(), currentUser);
                                    for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : monthWiseAnnualTargetList) {
                                        MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                        if (monthWiseAnnualTargetModel.getRetailerCode().equals(revisedAnnualSalesDetailsModel.getCustomerCode()) && monthWiseAnnualTargetModel.getIsAnnualSalesRevisedForRetailer().equals(true)) {
                                            monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                            monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                            monthWiseTargetDataSkuList.add(monthWiseTargetData);
                                        }
                                    }
                                    retailerDetailsData.setMonthWiseSkuTarget(monthWiseTargetDataSkuList);
                                    retailerDetailsDataList.add(retailerDetailsData);
                                }
                            }
                            annualSalesMonthWiseTargetData.setRetailerData(retailerDetailsDataList);
                            annualSalesMonthWiseTargetDataList.add(annualSalesMonthWiseTargetData);
                        }
                    }
                }
            }
            else
            {
                RetailerRevisedAnnualSalesModel retailerRevisedAnnualSalesModel = salesPlanningDao.findRetailerRevisedDetailsByCustomerCode(sclCustomer.getUid(), subAreaMaster.getPk().toString(), currentUser, findNextFinancialYear());
                if (retailerRevisedAnnualSalesModel != null && retailerRevisedAnnualSalesModel.getIsExistingRetailerRevisedForReview()!=null && retailerRevisedAnnualSalesModel.getIsExistingRetailerRevisedForReview().equals(true)) {
                    for (SubAreaMasterModel subAreaMasterModel : annualSalesMonthwiseModel.getSubAreaMasterList()) {
                        if (subAreaMasterModel.equals(retailerRevisedAnnualSalesModel.getSubAreaMaster())) {
                           /* listData.setSubArea(subAreaMasterModel.getTaluka());
                            listData.setSubAreaId(subAreaMasterModel.getPk().toString());*/
                            AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData = new AnnualSalesMonthWiseTargetData();
                            annualSalesMonthWiseTargetData.setCustomerCode(retailerRevisedAnnualSalesModel.getCustomerCode());
                            annualSalesMonthWiseTargetData.setCustomerName(retailerRevisedAnnualSalesModel.getCustomerName());
                            annualSalesMonthWiseTargetData.setCustomerPotential(retailerRevisedAnnualSalesModel.getCustomerPotential() != null ? retailerRevisedAnnualSalesModel.getCustomerPotential() : 0.0);
                            annualSalesMonthWiseTargetData.setTotalTarget(retailerRevisedAnnualSalesModel.getTotalTarget());

                            List<MonthWiseTargetData> monthWiseTargetDataList = new ArrayList<>();
                            if (retailerRevisedAnnualSalesModel.getMonthWiseAnnualTarget() != null &&
                                    !retailerRevisedAnnualSalesModel.getMonthWiseAnnualTarget().isEmpty()) {
                                for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : retailerRevisedAnnualSalesModel.getMonthWiseAnnualTarget()) {
                                    MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                    if (monthWiseAnnualTargetModel.getCustomerCode().equals(retailerRevisedAnnualSalesModel.getCustomerCode()) && monthWiseAnnualTargetModel.getIsAnnualSalesRevisedForRetailer().equals(true)) {
                                        monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                        monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                        monthWiseTargetDataList.add(monthWiseTargetData);
                                    }
                                }
                            }
                            annualSalesMonthWiseTargetData.setMonthWiseTarget(monthWiseTargetDataList);

                            List<SKUData> skuDataList = new ArrayList<>();
                            List<RetailerDetailsData> retailerDetailsDataList = new ArrayList<>();
                            if (retailerRevisedAnnualSalesModel.getListOfRetailersRevised() != null && !retailerRevisedAnnualSalesModel.getListOfRetailersRevised().isEmpty()) {
                                for (RetailerRevisedAnnualSalesDetailsModel revisedAnnualSalesDetailsModel:retailerRevisedAnnualSalesModel.getListOfRetailersRevised()) {
                                    RetailerDetailsData retailerDetailsData = new RetailerDetailsData();
                                    retailerDetailsData.setCustomerCode(revisedAnnualSalesDetailsModel.getCustomerCode());
                                    retailerDetailsData.setCustomerName(revisedAnnualSalesDetailsModel.getCustomerName());
                                    retailerDetailsData.setCustomerPotential(revisedAnnualSalesDetailsModel.getCustomerPotential());
                                    retailerDetailsData.setTotalTarget(revisedAnnualSalesDetailsModel.getTotalTarget());

                                    List<MonthWiseTargetData> monthWiseTargetDataSkuList = new ArrayList<>();
                                    List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetList = salesPlanningService.getMonthWiseRetailerDetailsForReview(retailerRevisedAnnualSalesModel.getCustomerCode(), revisedAnnualSalesDetailsModel.getCustomerCode(), retailerRevisedAnnualSalesModel.getSubAreaMaster().getPk().toString(), currentUser);
                                    for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : monthWiseAnnualTargetList) {
                                        MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                        if (monthWiseAnnualTargetModel.getCustomerCode().equals(revisedAnnualSalesDetailsModel.getCustomerCode()) && monthWiseAnnualTargetModel.getIsAnnualSalesRevisedForRetailer().equals(true)) {
                                            monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                            monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                            monthWiseTargetDataSkuList.add(monthWiseTargetData);
                                        }
                                    }
                                    retailerDetailsData.setMonthWiseSkuTarget(monthWiseTargetDataSkuList);
                                    retailerDetailsDataList.add(retailerDetailsData);
                                }
                            }
                            annualSalesMonthWiseTargetData.setRetailerData(retailerDetailsDataList);
                            annualSalesMonthWiseTargetDataList.add(annualSalesMonthWiseTargetData);
                        }
                    }
                }
            }
          //results
        }
            return annualSalesMonthWiseTargetListData;
    }

    public AnnualSalesTargetSettingListData viewDealerDetailsForAnnualSales(String subArea, String filter) {
        SclUserModel sclUser = (SclUserModel) getUserService().getCurrentUser();
        BaseSiteModel baseSite = baseSiteService.getCurrentBaseSite();
        AnnualSalesTargetSettingListData list = new AnnualSalesTargetSettingListData();
        String financialYear = findNextFinancialYear();
        AnnualSalesModel annualSalesModelDetails = salesPlanningDao.getAnnualSalesModelDetails(sclUser,financialYear,subArea, baseSite);

        SubAreaMasterModel subAreaMaster=territoryManagementService.getTerritoryById(subArea);
        List<AnnualSalesTargetSettingData> listOfAnnualTargetSetting = new ArrayList<>();
        if (StringUtils.isBlank(filter)) {
            List<List<Object>> listOfDealerDetails = salesPlanningService.viewDealerDetailsForAnnualSales(subAreaMaster.getPk().toString(), sclUser, baseSite);
            if (listOfDealerDetails != null && !listOfDealerDetails.isEmpty()) {
                for (List<Object> details : listOfDealerDetails) {
                    AnnualSalesTargetSettingData annualSalesTargetSettingData = new AnnualSalesTargetSettingData();
                    SclCustomerModel sclCustomer = (SclCustomerModel) userService.getUserForUID((String) details.get(0));
                    annualSalesTargetSettingData.setCustomerCode(sclCustomer.getUid());
                    annualSalesTargetSettingData.setCustomerName(sclCustomer.getName());
                    annualSalesTargetSettingData.setCustomerPotential(sclCustomer.getCounterPotential() !=null ? sclCustomer.getCounterPotential() :0.0);

                    List<List<Object>> dealerCySalesDetails = salesPlanningService.fetchDealerCySalesForAnnualSales(subAreaMaster.getPk().toString(), sclUser, baseSite, annualSalesTargetSettingData.getCustomerCode());
                    List<SKUData> listOfSku = new ArrayList<>();
                    if (dealerCySalesDetails != null && !dealerCySalesDetails.isEmpty()) {
                        for (List<Object> dealerSales : dealerCySalesDetails) {
                            if(dealerSales.get(3).equals(annualSalesTargetSettingData.getCustomerCode())) {
                                SKUData skuData = new SKUData();
                                List<ProductSaleModel> salesForSkus = salesPlanningDao.getSalesForSkus(annualSalesTargetSettingData.getCustomerCode(), subArea, sclUser);
                                if(salesForSkus!=null && !salesForSkus.isEmpty())
                                {
                                    for (ProductSaleModel productSales : salesForSkus) {
                                        if(annualSalesTargetSettingData.getCustomerCode().equals(dealerSales.get(3)) && productSales.getProductCode().equals((String) dealerSales.get(1))){
                                            LOG.info("no new sku");
                                            skuData.setCySales((Double) dealerSales.get(0));
                                            skuData.setProductCode((String) dealerSales.get(1));
                                            skuData.setProductName((String) dealerSales.get(2));
                                            skuData.setPlanSales(productSales.getPlanSales());
                                            if(productSales.getPremium()!=null)
                                            skuData.setPremium(productSales.getPremium());
                                        }
                                    }
                                }
                                else {
                                    skuData.setCySales((Double) dealerSales.get(0));
                                    skuData.setProductCode((String) dealerSales.get(1));
                                    skuData.setProductName((String) dealerSales.get(2));
                                    skuData.setPlanSales(0.0);
                                    String premium = (String) dealerSales.get(4);
                                    if(StringUtils.isNotBlank(premium)) {
                                        boolean isPremium = premium.equalsIgnoreCase("Y");
                                        skuData.setPremium(isPremium);
                                    }
                                }
                                listOfSku.add(skuData);
                            }
                        }

                        List<ProductSaleModel> productSaleModelList = salesPlanningDao.getSalesForNewSku(annualSalesTargetSettingData.getCustomerCode(),subArea,sclUser);
                        if(productSaleModelList!=null && !productSaleModelList.isEmpty()) {
                            for (ProductSaleModel productSaleModel : productSaleModelList) {
                                if (productSaleModel.getCustomerCode().equals(annualSalesTargetSettingData.getCustomerCode()) && productSaleModel.getIsNewSku() != null && productSaleModel.getIsNewSku().equals(true)) {
                                    SKUData skuData = new SKUData();
                                    skuData.setCySales(productSaleModel.getCySales());
                                    skuData.setProductCode(productSaleModel.getProductCode());
                                    skuData.setProductName(productSaleModel.getProductName());
                                    skuData.setPlanSales(productSaleModel.getPlanSales());
                                    if(productSaleModel.getPremium()!=null)
                                        skuData.setPremium(Boolean.valueOf(productSaleModel.getPremium()));
                                    listOfSku.add(skuData);
                                }
                            }
                        }
                    }
                    if (listOfSku != null && !listOfSku.isEmpty())
                        annualSalesTargetSettingData.setSkuDataList(listOfSku);
                    double sumOfProductCySales = 0.0;

                    if(annualSalesTargetSettingData.getSkuDataList()!=null && !annualSalesTargetSettingData.getSkuDataList().isEmpty()) {
                        for (SKUData skuData : annualSalesTargetSettingData.getSkuDataList()) {
                            sumOfProductCySales += skuData.getCySales();
                        }
                    }
                    //sum of sku's sale = dealer cy sales
                    annualSalesTargetSettingData.setCurrentYearSales(sumOfProductCySales);

                    double sumOfProductPlanSales = 0.0;

                    if(annualSalesModelDetails!=null)
                    {
                        if(annualSalesModelDetails.getDealerPlannedAnnualSales()!=null && !annualSalesModelDetails.getDealerPlannedAnnualSales().isEmpty())
                        {
                            for (DealerPlannedAnnualSalesModel dealerPlannedAnnualSale : annualSalesModelDetails.getDealerPlannedAnnualSales()) {

                                if(dealerPlannedAnnualSale.getCustomerCode().equalsIgnoreCase(annualSalesTargetSettingData.getCustomerCode()))
                                {
                                    if(dealerPlannedAnnualSale.getPlannedYearSales()!=null && dealerPlannedAnnualSale.getPlannedYearSales()!=0.0)
                                        sumOfProductPlanSales += dealerPlannedAnnualSale.getPlannedYearSales();

                                    annualSalesTargetSettingData.setPlanSales(sumOfProductPlanSales);

                                }
                                else
                                {
                                    annualSalesTargetSettingData.setPlanSales(0.0);
                                }
                            }
                        }
                    }
                    else
                        annualSalesTargetSettingData.setPlanSales(0.0);

                    listOfAnnualTargetSetting.add(annualSalesTargetSettingData);
                }
            }

            List<SclCustomerModel> dealers = territoryManagementService.getDealersForSubArea(subAreaMaster.getPk().toString());
            if(dealers!=null && !dealers.isEmpty())
                for (SclCustomerModel dealer : dealers) {
                    List<List<Object>> listOfDealerDetailsWithoutCySales = salesPlanningService.viewDealerDetailsForAnnualSales(subAreaMaster.getPk().toString(), sclUser, baseSite,dealer.getUid());
                    List<SKUData> emptyListOfSku=new ArrayList<>();
                    if(listOfDealerDetailsWithoutCySales == null || listOfDealerDetailsWithoutCySales.isEmpty())
                    {
                        AnnualSalesTargetSettingData data = new AnnualSalesTargetSettingData();
                        data.setCustomerCode(dealer.getUid());
                        data.setCustomerName(dealer.getName());
                        data.setCustomerPotential(dealer.getCounterPotential()!=null?dealer.getCounterPotential():0.0);
                        data.setCurrentYearSales(0.0);

                        if(annualSalesModelDetails!=null)
                        {
                            if(annualSalesModelDetails.getDealerPlannedAnnualSales()!=null && !annualSalesModelDetails.getDealerPlannedAnnualSales().isEmpty())
                            {
                                for (DealerPlannedAnnualSalesModel dealerPlannedAnnualSale : annualSalesModelDetails.getDealerPlannedAnnualSales()) {
                                    if(dealerPlannedAnnualSale.getCustomerCode().equalsIgnoreCase(data.getCustomerCode()))
                                    {
                                        if(dealerPlannedAnnualSale.getPlannedYearSales()!=null && dealerPlannedAnnualSale.getPlannedYearSales()!=0.0)
                                            data.setPlanSales(dealerPlannedAnnualSale.getPlannedYearSales());
                                    }
                                    else
                                    {
                                        data.setPlanSales(0.0);
                                    }
                                }
                            }
                        }
                        else
                            data.setPlanSales(0.0);

                        if(annualSalesModelDetails!=null) {
                            if(annualSalesModelDetails.getDealerPlannedAnnualSales()!=null && !annualSalesModelDetails.getDealerPlannedAnnualSales().isEmpty())
                            {
                                for (DealerPlannedAnnualSalesModel dealerPlannedAnnualSale : annualSalesModelDetails.getDealerPlannedAnnualSales())
                                {
                                    if(dealerPlannedAnnualSale.getListOfSkus()!=null && !dealerPlannedAnnualSale.getListOfSkus().isEmpty())
                                    {
                                        for (ProductModel product : dealerPlannedAnnualSale.getListOfSkus()) {
                                            ProductSaleModel productSaleModelDetails = salesPlanningDao.getSalesForSku(data.getCustomerCode(),product.getCode(),subArea,sclUser);
                                            if(dealerPlannedAnnualSale.getCustomerCode().equals(data.getCustomerCode()) && productSaleModelDetails.getProductCode().equals(product.getCode()))
                                            {
                                                SKUData skuData = new SKUData();
                                                skuData.setCySales(productSaleModelDetails.getCySales());
                                                skuData.setProductCode(productSaleModelDetails.getProductCode());
                                                skuData.setProductName(productSaleModelDetails.getProductName());
                                                skuData.setPlanSales(productSaleModelDetails.getPlanSales());
                                                if(productSaleModelDetails.getPremium()!=null)
                                                skuData.setPremium(productSaleModelDetails.getPremium());
                                                emptyListOfSku.add(skuData);
                                            }
                                        }
                                    }
                                }
                            }
                            data.setSkuDataList(emptyListOfSku);
                        }
                        else
                            data.setSkuDataList(emptyListOfSku);

                        listOfAnnualTargetSetting.add(data);
                        list.setAnnualSalesTargetSetting(listOfAnnualTargetSetting);
                    }
                }
        } else {
            List<List<Object>> listOfDealerDetails = salesPlanningService.viewDealerDetailsForAnnualSales(subAreaMaster.getPk().toString(), sclUser, baseSite,filter);
            //if cy sales is not present for selected dealer from search filter
            List<SKUData> emptyListOfSku=new ArrayList<>();
            if (listOfDealerDetails == null || listOfDealerDetails.isEmpty()) {
                AnnualSalesTargetSettingData data = new AnnualSalesTargetSettingData();
                SclCustomerModel sclCustomer = (SclCustomerModel) userService.getUserForUID(filter);
                data.setCustomerCode(sclCustomer.getUid());
                data.setCustomerName(sclCustomer.getName());
                data.setCustomerPotential(sclCustomer.getCounterPotential()!=null?sclCustomer.getCounterPotential():0.0);
                data.setCurrentYearSales(0.0);

                if(annualSalesModelDetails!=null)
                {
                    if(annualSalesModelDetails.getDealerPlannedAnnualSales()!=null && !annualSalesModelDetails.getDealerPlannedAnnualSales().isEmpty())
                    {
                        for (DealerPlannedAnnualSalesModel dealerPlannedAnnualSale : annualSalesModelDetails.getDealerPlannedAnnualSales()) {
                            if(dealerPlannedAnnualSale.getCustomerCode().equalsIgnoreCase(data.getCustomerCode()))
                            {
                                if(dealerPlannedAnnualSale.getPlannedYearSales()!=null && dealerPlannedAnnualSale.getPlannedYearSales()!=0.0)
                                    data.setPlanSales(dealerPlannedAnnualSale.getPlannedYearSales());
                            }
                            else
                            {
                                data.setPlanSales(0.0);
                            }
                        }
                    }
                }
                else
                    data.setPlanSales(0.0);

                if(annualSalesModelDetails!=null) {
                    if(annualSalesModelDetails.getDealerPlannedAnnualSales()!=null && !annualSalesModelDetails.getDealerPlannedAnnualSales().isEmpty())
                    {
                        for (DealerPlannedAnnualSalesModel dealerPlannedAnnualSale : annualSalesModelDetails.getDealerPlannedAnnualSales())
                        {
                            if(dealerPlannedAnnualSale.getListOfSkus()!=null && !dealerPlannedAnnualSale.getListOfSkus().isEmpty())
                            {
                                for (ProductModel product : dealerPlannedAnnualSale.getListOfSkus()) {
                                    ProductSaleModel productSaleModelDetails = salesPlanningDao.getSalesForSku(data.getCustomerCode(),product.getCode(),subArea,sclUser);
                                    if(dealerPlannedAnnualSale.getCustomerCode().equals(data.getCustomerCode()) && productSaleModelDetails.getProductCode().equals(product.getCode()))
                                    {
                                        SKUData skuData = new SKUData();
                                        skuData.setCySales(productSaleModelDetails.getCySales());
                                        skuData.setProductCode(productSaleModelDetails.getProductCode());
                                        skuData.setProductName(productSaleModelDetails.getProductName());
                                        skuData.setPlanSales(productSaleModelDetails.getPlanSales());
                                        if(productSaleModelDetails.getPremium()!=null)
                                        skuData.setPremium(productSaleModelDetails.getPremium());
                                        emptyListOfSku.add(skuData);
                                    }
                                }
                            }
                        }
                    }
                    data.setSkuDataList(emptyListOfSku);
                }
                else
                    data.setSkuDataList(emptyListOfSku);

                listOfAnnualTargetSetting.add(data);
                list.setAnnualSalesTargetSetting(listOfAnnualTargetSetting);
            }
            else if (listOfDealerDetails != null && !listOfDealerDetails.isEmpty())
            //if (listOfDealerDetails != null && !listOfDealerDetails.isEmpty())
            {
                for (List<Object> details : listOfDealerDetails) {
                    AnnualSalesTargetSettingData annualSalesTargetSettingData = new AnnualSalesTargetSettingData();
                    SclCustomerModel sclCustomer = (SclCustomerModel) userService.getUserForUID((String) details.get(0));
                    annualSalesTargetSettingData.setCustomerCode(sclCustomer.getUid());
                    annualSalesTargetSettingData.setCustomerName(sclCustomer.getName());
                    annualSalesTargetSettingData.setCustomerPotential(sclCustomer.getCounterPotential()!=null ? sclCustomer.getCounterPotential() :0.0);
                    List<List<Object>> dealerCySalesDetails = salesPlanningService.fetchDealerCySalesForAnnualSales(subAreaMaster.getPk().toString(), sclUser, baseSite, annualSalesTargetSettingData.getCustomerCode());
                    List<SKUData> listOfSku = new ArrayList<>();
                    if (dealerCySalesDetails != null && !dealerCySalesDetails.isEmpty()) {
                        for (List<Object> dealerSales : dealerCySalesDetails) {
                            LOG.info("dealer uid : "+ dealerSales.get(3));
                            if(dealerSales.get(3).equals(annualSalesTargetSettingData.getCustomerCode())) {
                                SKUData skuData = new SKUData();
                                skuData.setCySales((Double) dealerSales.get(0));
                                skuData.setProductCode((String) dealerSales.get(1));
                                skuData.setProductName((String) dealerSales.get(2));
                               /* ProductSaleModel productSaleModelDetails = salesPlanningDao.getSalesForSku(annualSalesTargetSettingData.getCustomerCode(),skuData.getProductCode(),subArea,sclUser);
                                if(productSaleModelDetails!=null)
                                {
                                    if(annualSalesTargetSettingData.getCustomerCode().equals(dealerSales.get(3)) && productSaleModelDetails.getProductCode().equals(skuData.getProductCode()))
                                        skuData.setPlanSales(productSaleModelDetails.getPlanSales());
                                }
                                else
                                    skuData.setPlanSales(0.0);

                                if(productSaleModelDetails!=null && productSaleModelDetails.getIsNewSku()!=null && productSaleModelDetails.getIsNewSku().equals(true))
                                {
                                    skuData.setCySales(productSaleModelDetails.getCySales());
                                    skuData.setProductCode(productSaleModelDetails.getProductCode());
                                    skuData.setProductName(productSaleModelDetails.getProductName());
                                    skuData.setPlanSales(productSaleModelDetails.getPlanSales());
                                }*/


                                List<ProductSaleModel> salesForSkus = salesPlanningDao.getSalesForSkus(annualSalesTargetSettingData.getCustomerCode(), subArea, sclUser);
                                if(salesForSkus!=null && !salesForSkus.isEmpty())
                                {
                                    for (ProductSaleModel productSales : salesForSkus) {
                                        if(annualSalesTargetSettingData.getCustomerCode().equals(dealerSales.get(3)) && productSales.getProductCode().equals((String) dealerSales.get(1))){
                                            LOG.info("no new sku");
                                            skuData.setCySales((Double) dealerSales.get(0));
                                            skuData.setProductCode((String) dealerSales.get(1));
                                            skuData.setProductName((String) dealerSales.get(2));
                                            skuData.setPlanSales(productSales.getPlanSales());
                                            if(productSales.getPremium()!=null)
                                            skuData.setPremium(productSales.getPremium());
                                        }
                                    }
                                }
                                else {
                                    skuData.setCySales((Double) dealerSales.get(0));
                                    skuData.setProductCode((String) dealerSales.get(1));
                                    skuData.setProductName((String) dealerSales.get(2));
                                    skuData.setPlanSales(0.0);
                                    String premium = (String) dealerSales.get(4);
                                    if(StringUtils.isNotBlank(premium)) {
                                        boolean isPremium = premium.equalsIgnoreCase("Y");
                                        skuData.setPremium(Boolean.valueOf(isPremium));
                                    }
                                }
                                listOfSku.add(skuData);
                            }
                        }

                        List<ProductSaleModel> productSaleModelList = salesPlanningDao.getSalesForNewSku(annualSalesTargetSettingData.getCustomerCode(),subArea,sclUser);
                        if(productSaleModelList!=null && !productSaleModelList.isEmpty()) {
                            for (ProductSaleModel productSaleModel : productSaleModelList) {
                                if (productSaleModel.getCustomerCode().equals(annualSalesTargetSettingData.getCustomerCode()) && productSaleModel.getIsNewSku() != null && productSaleModel.getIsNewSku().equals(true)) {
                                    SKUData skuData = new SKUData();
                                    skuData.setCySales(productSaleModel.getCySales());
                                    skuData.setProductCode(productSaleModel.getProductCode());
                                    skuData.setProductName(productSaleModel.getProductName());
                                    skuData.setPlanSales(productSaleModel.getPlanSales());
                                    if(productSaleModel.getPremium()!=null)
                                    skuData.setPremium(productSaleModel.getPremium());
                                    listOfSku.add(skuData);
                                }
                            }
                        }

                    }
                    if (listOfSku != null && !listOfSku.isEmpty())
                        annualSalesTargetSettingData.setSkuDataList(listOfSku);
                    double sumOfProductCySales = 0.0;
                    if(annualSalesTargetSettingData.getSkuDataList()!=null && !annualSalesTargetSettingData.getSkuDataList().isEmpty()) {
                        for (SKUData skuData : annualSalesTargetSettingData.getSkuDataList()) {
                            sumOfProductCySales += skuData.getCySales();
                        }
                    }
                    //sum of sku's sale = dealer cy sales
                    annualSalesTargetSettingData.setCurrentYearSales(sumOfProductCySales);

                    if(annualSalesModelDetails!=null)
                    {
                        if(annualSalesModelDetails.getDealerPlannedAnnualSales()!=null && !annualSalesModelDetails.getDealerPlannedAnnualSales().isEmpty())
                        {
                            for (DealerPlannedAnnualSalesModel dealerPlannedAnnualSale : annualSalesModelDetails.getDealerPlannedAnnualSales()) {
                                if (dealerPlannedAnnualSale.getCustomerCode().equalsIgnoreCase(annualSalesTargetSettingData.getCustomerCode())) {
                                    if (dealerPlannedAnnualSale.getPlannedYearSales() != null && dealerPlannedAnnualSale.getPlannedYearSales() != 0.0)
                                        annualSalesTargetSettingData.setPlanSales(dealerPlannedAnnualSale.getPlannedYearSales());
                                }
                                else
                                    annualSalesTargetSettingData.setPlanSales(0.0);
                            }
                        }
                    }
                    else
                        annualSalesTargetSettingData.setPlanSales(0.0);
                    listOfAnnualTargetSetting.add(annualSalesTargetSettingData);
                }
            }
        }
        double sumOfAllDealerCySales = 0.0, sumOfPlanSales=0.0;
        if (listOfAnnualTargetSetting != null && !listOfAnnualTargetSetting.isEmpty())
            for (AnnualSalesTargetSettingData annualSalesTargetSettingData : listOfAnnualTargetSetting) {
                sumOfAllDealerCySales += annualSalesTargetSettingData.getCurrentYearSales();
                list.setTotalCurrentYearSales(sumOfAllDealerCySales);
                if(annualSalesModelDetails!=null)
                {
                    list.setTotalPlanSales(annualSalesModelDetails.getDealerPlannedTotalPlanSales());
                }
                else
                {
                    list.setTotalPlanSales(0.0);
                }
            }
        if(subAreaMaster!=null && subAreaMaster.getTaluka()!=null)
        list.setSubArea(subAreaMaster.getTaluka());
        list.setSubAreaId(subAreaMaster.getPk().toString());
        list.setAnnualSalesTargetSetting(listOfAnnualTargetSetting);
        return list;
    }

        @Override
        public AnnualSalesTargetSettingListData viewRetailerDetailsForAnnualSales (String subArea, String filter){
            SclUserModel sclUser = (SclUserModel) getUserService().getCurrentUser();
            BaseSiteModel baseSite = baseSiteService.getCurrentBaseSite();
            AnnualSalesTargetSettingListData list = new AnnualSalesTargetSettingListData();
            List<AnnualSalesTargetSettingData> listOfAnnualTargetSetting = new ArrayList<>();

            SubAreaMasterModel subAreaMaster=territoryManagementService.getTerritoryById(subArea);

            if(StringUtils.isBlank(filter)) {
                List<List<Object>> listOfDealerDetails = salesPlanningService.viewDealerDetailsForRetailerAnnualSales(subAreaMaster.getPk().toString(), sclUser, baseSite);
                if (listOfDealerDetails != null && !listOfDealerDetails.isEmpty()) {
                    for (List<Object> dealerDetails : listOfDealerDetails) {
                        AnnualSalesTargetSettingData data = new AnnualSalesTargetSettingData();
                        SclCustomerModel sclCustomer = (SclCustomerModel) userService.getUserForUID((String) dealerDetails.get(0));
                        data.setCustomerCode(sclCustomer.getUid());
                        data.setCustomerName(sclCustomer.getName());
                        data.setCustomerPotential(sclCustomer.getCounterPotential() != null ? sclCustomer.getCounterPotential() : 0.0);
                        Double dealerCySalesDetails = salesPlanningService.fetchDealerCySalesForRetailerAnnualSales(subAreaMaster.getPk().toString(), sclUser, baseSite, sclCustomer.getUid());
                        data.setCurrentYearSales((Double)dealerCySalesDetails!=0.0?(Double)dealerCySalesDetails:0.0);
                        List<List<Object>> listOfRetailerDetails = salesPlanningService.viewRetailerDetailsForAnnualSales(data.getCustomerCode(), subAreaMaster.getPk().toString(), sclUser, baseSite);
                        List<RetailerDetailsData> retailerDetailsDataList = new ArrayList<>();
                        if (listOfRetailerDetails != null && !listOfRetailerDetails.isEmpty()) {
                            for (List<Object> retailerDetails : listOfRetailerDetails) {
                                if (retailerDetails.get(4).equals(data.getCustomerCode())) {
                                    RetailerDetailsData retailerDetailsData = new RetailerDetailsData();
                                    SclCustomerModel retailer = (SclCustomerModel) userService.getUserForUID((String) retailerDetails.get(0));
                                    retailerDetailsData.setCustomerCode(retailer.getUid());
                                    retailerDetailsData.setCustomerName(retailer.getName());
                                    retailerDetailsData.setCustomerPotential(retailer.getCounterPotential() != null ? retailer.getCounterPotential() : 0.0);
                                    retailerDetailsData.setCySales((Double) retailerDetails.get(3));
                                    retailerDetailsDataList.add(retailerDetailsData);
                                }
                            }
                            data.setRetailerData(retailerDetailsDataList);
                        }
                        double tempDealerCySale = 0.0;
                        for (RetailerDetailsData retailerDetailsData : retailerDetailsDataList) {
                            tempDealerCySale += retailerDetailsData.getCySales();
                        }

                        SelfCounterSaleData selfCounterSaleData = new SelfCounterSaleData();
                        selfCounterSaleData.setCustomerCode(data.getCustomerCode());
                        selfCounterSaleData.setCustomerName(data.getCustomerName());
                        selfCounterSaleData.setCustomerPotential(data.getCustomerPotential());


                        double dealerSelfCounterSelfCySale= 0.0;
                        dealerSelfCounterSelfCySale = (data.getCurrentYearSales() - tempDealerCySale);
                        if (dealerSelfCounterSelfCySale != 0.0)
                            selfCounterSaleData.setCySales(dealerSelfCounterSelfCySale);
                        else
                            selfCounterSaleData.setCySales(0.0);

                        data.setSelfCounterSale(selfCounterSaleData);
                        listOfAnnualTargetSetting.add(data);
                        }

                    }
                    double sumOfAllDealerCySales = 0.0;
                    if (listOfAnnualTargetSetting != null && !listOfAnnualTargetSetting.isEmpty())
                        for (AnnualSalesTargetSettingData annualSalesTargetSettingData : listOfAnnualTargetSetting) {
                            sumOfAllDealerCySales += annualSalesTargetSettingData.getCurrentYearSales();
                            list.setTotalCurrentYearSales(sumOfAllDealerCySales);
                        }

                    list.setAnnualSalesTargetSetting(listOfAnnualTargetSetting);
                    if(subAreaMaster!=null && subAreaMaster.getTaluka()!=null)
                    list.setSubArea(subAreaMaster.getTaluka());
                    list.setSubAreaId(subAreaMaster.getPk().toString());
                }
            else {
                //if filter contains retailer
                SclCustomerModel filterCustomer = (SclCustomerModel) getUserService().getUserForUID(filter);
                List<List<Object>> listOfDealers = salesPlanningService.fetchDealerDetailsForSelectedRetailer(subAreaMaster.getPk().toString(), sclUser, baseSite, filter);
                List<List<Object>> listOfRetailers = salesPlanningService.fetchRetailerDetailsForSelectedDealer(subAreaMaster.getPk().toString(), sclUser, baseSite, filter);

                //if cy sales is not present for selected retailer and its assigned dealers from search filter - doubt

                /*if (filterCustomer.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                    if (listOfDealers == null || listOfDealers.isEmpty()) {
                        AnnualSalesTargetSettingData data = new AnnualSalesTargetSettingData();
                        SclCustomerModel sclCustomer = (SclCustomerModel) userService.getUserForUID(filter);
                        RetailerDetailsData retailerDetailsData = new RetailerDetailsData();
                        retailerDetailsData.setCustomerCode(sclCustomer.getUid());
                        retailerDetailsData.setCustomerName(sclCustomer.getName());
                        retailerDetailsData.setCustomerPotential(sclCustomer.getCounterPotential() != null ? sclCustomer.getCounterPotential() : 0.0);
                        retailerDetailsData.setCySales(0.0);
                        listOfAnnualTargetSetting.add(data);
                    }
                }
                else if (filterCustomer.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))
                {
                    if (listOfRetailers == null || listOfRetailers.isEmpty()) {
                        AnnualSalesTargetSettingData data = new AnnualSalesTargetSettingData();
                        SclCustomerModel sclCustomer = (SclCustomerModel) userService.getUserForUID(filter);
                        data.setCustomerCode(sclCustomer.getUid());
                        data.setCustomerName(sclCustomer.getName());
                        data.setCustomerPotential(sclCustomer.getCounterPotential()!=null?sclCustomer.getCounterPotential():0.0);
                        data.setCurrentYearSales(0.0);
                        listOfAnnualTargetSetting.add(data);
                    }
                }*/

                if (filterCustomer.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                    if (listOfDealers != null && !listOfDealers.isEmpty()) {
                        for (List<Object> dealerDetail : listOfDealers) {
                            AnnualSalesTargetSettingData data = new AnnualSalesTargetSettingData();
                            SclCustomerModel dealer = (SclCustomerModel) getUserService().getUserForUID((String) dealerDetail.get(0));
                            data.setCustomerCode(dealer.getUid());
                            data.setCustomerName(dealer.getName());
                            data.setCustomerPotential(dealer.getCounterPotential() != null ? dealer.getCounterPotential() : 0.0);
                            data.setCurrentYearSales((Double) dealerDetail.get(1));

                            List<RetailerDetailsData> retailerList = new ArrayList<>();
                            RetailerDetailsData retailerDetailsData = new RetailerDetailsData();
                            retailerDetailsData.setCustomerCode(filterCustomer.getUid());
                            retailerDetailsData.setCustomerName(filterCustomer.getName());
                            retailerDetailsData.setCustomerPotential(filterCustomer.getCounterPotential() != null ? filterCustomer.getCounterPotential() : 0.0);

                            List<List<Object>> listOfRetailerDetails = salesPlanningService.viewRetailerDetailsForAnnualSales(data.getCustomerCode(), subAreaMaster.getPk().toString(), sclUser, baseSite);
                            for (List<Object> listOfRetailerDetail : listOfRetailerDetails) {
                                retailerDetailsData.setCySales((Double) listOfRetailerDetail.get(3));
                            }
                            retailerList.add(retailerDetailsData);
                            data.setRetailerData(retailerList);

                            Double dealerCySalesDetails = salesPlanningService.fetchDealerCySalesForRetailerAnnualSales(subAreaMaster.getPk().toString(), sclUser, baseSite, data.getCustomerCode());
                            SelfCounterSaleData selfCounterSaleData = new SelfCounterSaleData();
                            selfCounterSaleData.setCustomerCode(data.getCustomerCode());
                            selfCounterSaleData.setCustomerName(data.getCustomerName());
                            selfCounterSaleData.setCustomerPotential(data.getCustomerPotential() != null ? data.getCustomerPotential() : 0.0);
                            data.setSelfCounterSale(selfCounterSaleData);

                            double tempDealerCySale=0.0;
                            for (RetailerDetailsData retailerDatum : data.getRetailerData()) {
                                tempDealerCySale += retailerDatum.getCySales();
                            }

                            data.setCurrentYearSales(tempDealerCySale);

                            double dealerSelfCounterSelfCySale= 0.0;
                            dealerSelfCounterSelfCySale = (data.getCurrentYearSales() - tempDealerCySale);
                            if (dealerSelfCounterSelfCySale != 0.0)
                                selfCounterSaleData.setCySales(dealerSelfCounterSelfCySale);
                            else
                                selfCounterSaleData.setCySales(0.0);

                            data.setSelfCounterSale(selfCounterSaleData);

                            listOfAnnualTargetSetting.add(data);
                        }
                        list.setAnnualSalesTargetSetting(listOfAnnualTargetSetting);
                    }
                } else if (filterCustomer.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                    if (listOfRetailers != null && !listOfRetailers.isEmpty()) {
                        AnnualSalesTargetSettingData data = new AnnualSalesTargetSettingData();
                        data.setCustomerCode(filterCustomer.getUid());
                        data.setCustomerName(filterCustomer.getName());
                        data.setCustomerPotential(filterCustomer.getCounterPotential() != null ? filterCustomer.getCounterPotential() : 0.0);

                        List<RetailerDetailsData> retailerList = new ArrayList<>();
                        for (List<Object> retailerDetail : listOfRetailers) {

                            SclCustomerModel retailer = (SclCustomerModel) getUserService().getUserForUID((String) retailerDetail.get(0));
                            RetailerDetailsData retailerDetailsData = new RetailerDetailsData();
                            retailerDetailsData.setCustomerCode(retailer.getUid());
                            retailerDetailsData.setCustomerName(retailer.getName());
                            retailerDetailsData.setCustomerPotential(retailer.getCounterPotential() != null ? data.getCustomerPotential() : 0.0);
                            retailerDetailsData.setCySales((Double) retailerDetail.get(1));
                            retailerList.add(retailerDetailsData);
                        }
                            data.setRetailerData(retailerList);

                            Double dealerCySalesDetails = salesPlanningService.fetchDealerCySalesForRetailerAnnualSales(subAreaMaster.getPk().toString(), sclUser, baseSite, data.getCustomerCode());
                            SelfCounterSaleData selfCounterSaleData = new SelfCounterSaleData();
                            selfCounterSaleData.setCustomerCode(data.getCustomerCode());
                            selfCounterSaleData.setCustomerName(data.getCustomerName());
                            selfCounterSaleData.setCustomerPotential(data.getCustomerPotential() != null ? data.getCustomerPotential() : 0.0);
                            data.setSelfCounterSale(selfCounterSaleData);

                            double tempDealerCySale=0.0;
                            for (RetailerDetailsData retailerDatum : data.getRetailerData()) {
                                tempDealerCySale += retailerDatum.getCySales();
                            }

                        data.setCurrentYearSales(dealerCySalesDetails);

                        double dealerSelfCounterSelfCySale= 0.0;
                        dealerSelfCounterSelfCySale = (data.getCurrentYearSales() - tempDealerCySale);
                        if (dealerSelfCounterSelfCySale != 0.0)
                            selfCounterSaleData.setCySales(dealerSelfCounterSelfCySale);
                        else
                            selfCounterSaleData.setCySales(0.0);

                        data.setSelfCounterSale(selfCounterSaleData);

                            listOfAnnualTargetSetting.add(data);
                            list.setAnnualSalesTargetSetting(listOfAnnualTargetSetting);
                    }
                }
            }
            double sumOfAllDealerCySales = 0.0;
            if (listOfAnnualTargetSetting != null && !listOfAnnualTargetSetting.isEmpty())
                for (AnnualSalesTargetSettingData annualSalesTargetSettingData : listOfAnnualTargetSetting) {
                    if(annualSalesTargetSettingData.getCurrentYearSales()!=null)
                        sumOfAllDealerCySales += annualSalesTargetSettingData.getCurrentYearSales();
                    list.setTotalCurrentYearSales(sumOfAllDealerCySales);
                }
            if(subAreaMaster!=null && subAreaMaster.getTaluka()!=null)
                list.setSubArea(subAreaMaster.getTaluka());
            list.setSubAreaId(subAreaMaster.getPk().toString());
            return list;
        }

        @Override
        public AnnualSalesMonthWiseTargetListData viewMonthWiseDealerDetailsForAnnualSales(String subArea, String filter){
            SclUserModel sclUser = (SclUserModel) getUserService().getCurrentUser();
            List<AnnualSalesMonthWiseTargetData> annualSalesMonthWiseTargetDataList = new ArrayList<>();
            SubAreaMasterModel subAreaMaster=territoryManagementService.getTerritoryById(subArea);
            BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
            AnnualSalesModel annualSalesModel = salesPlanningService.viewPlannedSalesforDealersRetailersMonthWise(subAreaMaster.getPk().toString(), sclUser, brand);
            AnnualSalesMonthWiseTargetListData list = new AnnualSalesMonthWiseTargetListData();
            if(annualSalesModel!=null) {
                list.setTotalCurrentYearSales(annualSalesModel.getDealerPlannedTotalCySales());
                list.setTotalPlanSales(annualSalesModel.getDealerPlannedTotalPlanSales());
            }

            if(StringUtils.isBlank(filter)) {
                if (annualSalesModel != null) {
                    if (annualSalesModel.getDealerPlannedAnnualSales() != null &&
                            !annualSalesModel.getDealerPlannedAnnualSales().isEmpty()) {
                        for (DealerPlannedAnnualSalesModel dealerPlannedAnnualSale : annualSalesModel.getDealerPlannedAnnualSales()) {
                            if (dealerPlannedAnnualSale.getSubAreaMaster().equals(subAreaMaster)) {
                                AnnualSalesMonthWiseTargetData data = new AnnualSalesMonthWiseTargetData();
                                //for (SubAreaMasterModel subAreaMasterModel : annualSalesModel.getSubAreaMasterList()) {
                                if(annualSalesModel.getSubAreaMaster()!=null) {
                                    SubAreaMasterModel subAreaMasterModel = annualSalesModel.getSubAreaMaster();
                                    if (subAreaMasterModel.equals(dealerPlannedAnnualSale.getSubAreaMaster())) {
                                        list.setSubArea(subAreaMasterModel.getTaluka());
                                        list.setSubAreaId(subAreaMasterModel.getPk().toString());
                                    }
                                }
                                //}
                                data.setCustomerCode(dealerPlannedAnnualSale.getCustomerCode());
                                data.setCustomerName(dealerPlannedAnnualSale.getCustomerName());
                                data.setCustomerPotential(dealerPlannedAnnualSale.getCustomerPotential()!=null?dealerPlannedAnnualSale.getCustomerPotential():0.0);
                                data.setTotalTarget(dealerPlannedAnnualSale.getTotalTarget());
                                List<MonthWiseTargetData> monthWiseTargetDataList = new ArrayList<>();
                                if (dealerPlannedAnnualSale.getMonthWiseAnnualTarget() != null && !dealerPlannedAnnualSale.getMonthWiseAnnualTarget().isEmpty()) {
                                    for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : dealerPlannedAnnualSale.getMonthWiseAnnualTarget()) {
                                        //customerCode logic equal need to add.
                                        if (monthWiseAnnualTargetModel.getCustomerCode().equals(dealerPlannedAnnualSale.getCustomerCode())) {
                                            MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                            LOG.info("Dealer monthwise target ::" + monthWiseAnnualTargetModel.getMonthYear() + " - "  + monthWiseAnnualTargetModel.getMonthTarget());
                                            monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                            monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                            monthWiseTargetDataList.add(monthWiseTargetData);
                                        }
                                    }
                                }
                                List<SKUData> skuDataList = new ArrayList<>();

                                if (dealerPlannedAnnualSale.getListOfSkus() != null && !dealerPlannedAnnualSale.getListOfSkus().isEmpty()) {
                                    for (ProductModel sku : dealerPlannedAnnualSale.getListOfSkus()) {
                                        SKUData skuData = new SKUData();
                                        skuData.setProductCode(sku.getCode());
                                        skuData.setProductName(sku.getName());

                                        ProductSaleModel productSaleModel = salesPlanningDao.getSalesForSku(data.getCustomerCode(), skuData.getProductCode(), subAreaMaster.getPk().toString(), sclUser);
                                        if(productSaleModel!=null)
                                        {
                                            skuData.setCySales(productSaleModel.getCySales());
                                            skuData.setTotalTarget(productSaleModel.getTotalTarget());
                                            skuData.setPremium(productSaleModel.getPremium()!=null ? productSaleModel.getPremium():false);
                                        }
                                        List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetListForSku = salesPlanningService.getMonthWiseAnnualTargetDetails(dealerPlannedAnnualSale.getCustomerCode(), sku.getCode(), dealerPlannedAnnualSale.getSubAreaMaster().getPk().toString());
                                        List<MonthWiseTargetData> monthWiseSkuTargetDataList = new ArrayList<>();
                                        if(monthWiseAnnualTargetListForSku!=null && !monthWiseAnnualTargetListForSku.isEmpty()) {
                                            for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : monthWiseAnnualTargetListForSku) {
                                                if (monthWiseAnnualTargetModel.getCustomerCode().equals(dealerPlannedAnnualSale.getCustomerCode()) &&
                                                        monthWiseAnnualTargetModel.getProductCode().equals(sku.getCode())) {
                                                    LOG.info("Dealer and SKU monthwise target ::" + monthWiseAnnualTargetModel.getMonthYear() + " - "  + monthWiseAnnualTargetModel.getMonthTarget());
                                                    MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                                    monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                                    monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                                    monthWiseSkuTargetDataList.add(monthWiseTargetData);
                                                }
                                            }
                                        }
                                        skuData.setMonthWiseSkuTarget(monthWiseSkuTargetDataList);
                                        skuDataList.add(skuData);
                                    }
                                }
                                data.setMonthWiseTarget(monthWiseTargetDataList);
                                data.setSkuDataList(skuDataList);
                                annualSalesMonthWiseTargetDataList.add(data);
                            }
                        }
                    }
                }
            }
            else
            {
                DealerPlannedAnnualSalesModel dealerPlannedAnnualSale = salesPlanningService.fetchRecordForDealerPlannedAnnualSales(subAreaMaster.getPk().toString(), sclUser, filter);
                if(dealerPlannedAnnualSale !=null)
                {
                    if(dealerPlannedAnnualSale.getSubAreaMaster().equals(subAreaMaster))
                    {
                        AnnualSalesMonthWiseTargetData data = new AnnualSalesMonthWiseTargetData();
                        data.setCustomerCode(dealerPlannedAnnualSale.getCustomerCode());
                        data.setCustomerName(dealerPlannedAnnualSale.getCustomerName());
                        data.setCustomerPotential(dealerPlannedAnnualSale.getCustomerPotential()!=null ?dealerPlannedAnnualSale.getCustomerPotential():0.0);
                        data.setTotalTarget(dealerPlannedAnnualSale.getTotalTarget());
                        List<MonthWiseTargetData> monthWiseTargetDataList = new ArrayList<>();
                        if (dealerPlannedAnnualSale.getMonthWiseAnnualTarget() != null && !dealerPlannedAnnualSale.getMonthWiseAnnualTarget().isEmpty()) {
                            for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : dealerPlannedAnnualSale.getMonthWiseAnnualTarget()) {
                                if (monthWiseAnnualTargetModel.getCustomerCode().equals(dealerPlannedAnnualSale.getCustomerCode())) {
                                    MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                    monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                    monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                    monthWiseTargetDataList.add(monthWiseTargetData);
                                }
                            }
                        }
                        List<SKUData> skuDataList = new ArrayList<>();
                        if (dealerPlannedAnnualSale.getListOfSkus() != null && !dealerPlannedAnnualSale.getListOfSkus().isEmpty()) {
                            for (ProductModel sku : dealerPlannedAnnualSale.getListOfSkus()) {
                                SKUData skuData = new SKUData();
                                skuData.setProductCode(sku.getCode());
                                skuData.setProductName(sku.getName());
                                ProductSaleModel productSaleModel = salesPlanningDao.getSalesForSku(data.getCustomerCode(), skuData.getProductCode(), subAreaMaster.getPk().toString(), sclUser);
                                if(productSaleModel!=null)
                                {
                                        skuData.setCySales(productSaleModel.getCySales());
                                        skuData.setTotalTarget(productSaleModel.getTotalTarget());
                                        skuData.setPremium(productSaleModel.getPremium()!=null ? productSaleModel.getPremium() : false);
                                }
                                List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetListForSku = salesPlanningService.getMonthWiseAnnualTargetDetails(dealerPlannedAnnualSale.getCustomerCode(), skuData.getProductCode(),dealerPlannedAnnualSale.getSubAreaMaster().getPk().toString());
                                List<MonthWiseTargetData> monthWiseSkuTargetDataList = new ArrayList<>();
                                if(monthWiseAnnualTargetListForSku!=null && !monthWiseAnnualTargetListForSku.isEmpty()) {
                                    for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : monthWiseAnnualTargetListForSku) {
                                        if (monthWiseAnnualTargetModel.getCustomerCode().equals(dealerPlannedAnnualSale.getCustomerCode()) &&
                                                monthWiseAnnualTargetModel.getProductCode().equals(sku.getCode())) {
                                            MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                            monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                            monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                            monthWiseSkuTargetDataList.add(monthWiseTargetData);
                                        }
                                    }
                                }
                                skuData.setMonthWiseSkuTarget(monthWiseSkuTargetDataList);
                                skuDataList.add(skuData);
                            }
                        }
                        data.setMonthWiseTarget(monthWiseTargetDataList);
                        data.setSkuDataList(skuDataList);
                        annualSalesMonthWiseTargetDataList.add(data);
                    }
                }
            }
            list.setSubArea(subAreaMaster.getTaluka());
            list.setSubAreaId(subAreaMaster.getPk().toString());
            list.setAnnualSalesMonthWiseTargetData(annualSalesMonthWiseTargetDataList);
            return list;
        }

        @Override
        public AnnualSalesMonthWiseTargetListData viewMonthWiseRetailerDetailsForAnnualSales(String subArea, String filter) {
            SclUserModel sclUser = (SclUserModel) getUserService().getCurrentUser();
            List<AnnualSalesMonthWiseTargetData> annualSalesMonthWiseTargetDataList = new ArrayList<>();

            SubAreaMasterModel subAreaMaster=territoryManagementService.getTerritoryById(subArea);
            BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
            AnnualSalesModel annualSalesModel = salesPlanningService.viewPlannedSalesforDealersRetailersMonthWise(subAreaMaster.getPk().toString(), sclUser, brand);
            AnnualSalesMonthWiseTargetListData list = new AnnualSalesMonthWiseTargetListData();
            if (StringUtils.isBlank(filter))
            {
                if (annualSalesModel != null) {
                    list.setTotalPlanSales(annualSalesModel.getRetailerPlannedTotalPlanSales()!=null ?annualSalesModel.getRetailerPlannedTotalPlanSales():0.0);
                    list.setTotalCurrentYearSales(annualSalesModel.getRetailerPlannedTotalCySales()!=null ?annualSalesModel.getRetailerPlannedTotalCySales():0.0);
                    if (annualSalesModel.getRetailerPlannedAnnualSales() != null && !annualSalesModel.getRetailerPlannedAnnualSales().isEmpty()) {
                        for (RetailerPlannedAnnualSalesModel retailerPlannedAnnualSale : annualSalesModel.getRetailerPlannedAnnualSales()) {
                            if (retailerPlannedAnnualSale.getSubAreaMaster().equals(subAreaMaster)) {
                                AnnualSalesMonthWiseTargetData data = new AnnualSalesMonthWiseTargetData();
                                data.setCustomerCode(retailerPlannedAnnualSale.getCustomerCode());
                                data.setCustomerName(retailerPlannedAnnualSale.getCustomerName());
                                data.setCustomerPotential(retailerPlannedAnnualSale.getCustomerPotential()!=null?retailerPlannedAnnualSale.getCustomerPotential():0.0);
                                data.setTotalTarget(retailerPlannedAnnualSale.getTotalTarget());
                                List<RetailerDetailsData> retailerDetailsDataList = new ArrayList<>();
                                if (retailerPlannedAnnualSale.getListOfRetailersPlanned() != null && !retailerPlannedAnnualSale.getListOfRetailersPlanned().isEmpty()) {
                                    for (RetailerPlannedAnnualSalesDetailsModel retailer : retailerPlannedAnnualSale.getListOfRetailersPlanned()) {
                                        RetailerDetailsData retailerDetailsData = new RetailerDetailsData();
                                        retailerDetailsData.setCustomerCode(retailer.getCustomerCode());
                                        retailerDetailsData.setCustomerName(retailer.getCustomerName());
                                        retailerDetailsData.setCustomerPotential(retailer.getCustomerPotential());
                                        retailerDetailsData.setTotalTarget(retailer.getTotalTarget());
                                        List<MonthWiseTargetData> monthWiseTargetDataListForRetailerDetail = new ArrayList<>();
                                        for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : retailer.getMonthWiseAnnualTarget()) {
                                            MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                            monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                            monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                            monthWiseTargetDataListForRetailerDetail.add(monthWiseTargetData);
                                        }
                                        retailerDetailsData.setMonthWiseSkuTarget(monthWiseTargetDataListForRetailerDetail);
                                        retailerDetailsDataList.add(retailerDetailsData);
                                    }
                                }
                                List<MonthWiseTargetData> monthWiseTargetDataList = new ArrayList<>();
                                if (retailerPlannedAnnualSale.getMonthWiseAnnualTarget() != null && !retailerPlannedAnnualSale.getMonthWiseAnnualTarget().isEmpty()) {
                                    for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : retailerPlannedAnnualSale.getMonthWiseAnnualTarget()) {
                                        if (monthWiseAnnualTargetModel.getCustomerCode().equals(retailerPlannedAnnualSale.getCustomerCode())) {
                                            MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                            monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                            monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                            monthWiseTargetDataList.add(monthWiseTargetData);
                                        }
                                    }
                                }

                                List<MonthWiseTargetData> monthWiseTargetDataListforSelf = new ArrayList<>();
                                SelfCounterSaleDetailsModel dealerSelfCounterSale = retailerPlannedAnnualSale.getDealerSelfCounterSale();
                                SelfCounterSaleData selfCounterSaleData = new SelfCounterSaleData();
                                selfCounterSaleData.setCustomerCode(dealerSelfCounterSale.getCustomerCode());
                                selfCounterSaleData.setCustomerName(dealerSelfCounterSale.getCustomerName());
                                selfCounterSaleData.setCustomerPotential(dealerSelfCounterSale.getCustomerPotential());
                                selfCounterSaleData.setTotalTarget(dealerSelfCounterSale.getTotalTarget());
                                for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : dealerSelfCounterSale.getMonthWiseAnnualTarget()) {
                                    MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                    monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                    monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                    monthWiseTargetDataListforSelf.add(monthWiseTargetData);
                                }
                                selfCounterSaleData.setMonthWiseTarget(monthWiseTargetDataListforSelf);

                                data.setSelfCounterSale(selfCounterSaleData);
                                data.setMonthWiseTarget(monthWiseTargetDataList);
                                data.setRetailerData(retailerDetailsDataList);
                                annualSalesMonthWiseTargetDataList.add(data);
                            }
                        }
                    }
                }
            }
            else
            {
                RetailerPlannedAnnualSalesModel retailerPlannedAnnualSale=salesPlanningService.fetchRecordForRetailerPlannedAnnualSales(subAreaMaster.getPk().toString(),sclUser,filter);
                if(retailerPlannedAnnualSale !=null) {
                    if (retailerPlannedAnnualSale.getSubarea().equals(subArea)) {
                        AnnualSalesMonthWiseTargetData data = new AnnualSalesMonthWiseTargetData();

                        data.setCustomerCode(retailerPlannedAnnualSale.getCustomerCode());
                        data.setCustomerName(retailerPlannedAnnualSale.getCustomerName());
                        data.setCustomerPotential(retailerPlannedAnnualSale.getCustomerPotential());
                        data.setTotalTarget(retailerPlannedAnnualSale.getTotalTarget());
                        List<RetailerDetailsData> retailerDetailsDataList = new ArrayList<>();
                        if (retailerPlannedAnnualSale.getListOfRetailersPlanned() != null && !retailerPlannedAnnualSale.getListOfRetailersPlanned().isEmpty()) {
                            for (RetailerPlannedAnnualSalesDetailsModel retailer : retailerPlannedAnnualSale.getListOfRetailersPlanned()) {
                                RetailerDetailsData retailerDetailsData = new RetailerDetailsData();
                                retailerDetailsData.setCustomerCode(retailer.getCustomerCode());
                                retailerDetailsData.setCustomerName(retailer.getCustomerName());
                                retailerDetailsData.setCustomerPotential(retailer.getCustomerPotential()!=null?retailer.getCustomerPotential():0.0);
                                retailerDetailsData.setTotalTarget(retailer.getTotalTarget());
                                List<MonthWiseTargetData> monthWiseTargetDataListForRetailerDetail = new ArrayList<>();
                                for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : retailer.getMonthWiseAnnualTarget()) {
                                    MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                    monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                    monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                    monthWiseTargetDataListForRetailerDetail.add(monthWiseTargetData);
                                }
                                retailerDetailsData.setMonthWiseSkuTarget(monthWiseTargetDataListForRetailerDetail);
                                retailerDetailsDataList.add(retailerDetailsData);
                            }
                        }
                        List<MonthWiseTargetData> monthWiseTargetDataList = new ArrayList<>();
                        if (retailerPlannedAnnualSale.getMonthWiseAnnualTarget() != null && !retailerPlannedAnnualSale.getMonthWiseAnnualTarget().isEmpty()) {
                            for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : retailerPlannedAnnualSale.getMonthWiseAnnualTarget()) {
                                if (monthWiseAnnualTargetModel.getCustomerCode().equals(retailerPlannedAnnualSale.getCustomerCode())) {
                                    MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                    monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                    monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                    monthWiseTargetDataList.add(monthWiseTargetData);
                                }
                            }
                        }

                        List<MonthWiseTargetData> monthWiseTargetDataListforSelf = new ArrayList<>();
                        SelfCounterSaleDetailsModel dealerSelfCounterSale = retailerPlannedAnnualSale.getDealerSelfCounterSale();
                        SelfCounterSaleData selfCounterSaleData = new SelfCounterSaleData();
                        selfCounterSaleData.setCustomerCode(dealerSelfCounterSale.getCustomerCode());
                        selfCounterSaleData.setCustomerName(dealerSelfCounterSale.getCustomerName());
                        selfCounterSaleData.setCustomerPotential(dealerSelfCounterSale.getCustomerPotential()!=null ? dealerSelfCounterSale.getCustomerPotential():0.0);
                        selfCounterSaleData.setTotalTarget(dealerSelfCounterSale.getTotalTarget());
                        for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : dealerSelfCounterSale.getMonthWiseAnnualTarget()) {
                            MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                            monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                            monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                            monthWiseTargetDataListforSelf.add(monthWiseTargetData);
                        }
                        selfCounterSaleData.setMonthWiseTarget(monthWiseTargetDataListforSelf);

                        data.setSelfCounterSale(selfCounterSaleData);
                        data.setMonthWiseTarget(monthWiseTargetDataList);
                        data.setRetailerData(retailerDetailsDataList);
                        annualSalesMonthWiseTargetDataList.add(data);
                    }
                }
            }
            list.setSubArea(subAreaMaster.getTaluka());
            list.setSubAreaId(subAreaMaster.getPk().toString());
            list.setAnnualSalesMonthWiseTargetData(annualSalesMonthWiseTargetDataList);
            return list;
        }

    @Override
        public List<RetailerDetailsData> getRetailerList (String subArea, String dealerCode){
            List<RetailerDetailsData> list = new ArrayList<>();
            List<MonthWiseTargetData> monthWiseTargetDataList = new ArrayList<>();
            List<SclCustomerModel> retailerList = territoryManagementService.getAllRetailersForSubAreaTOP(subArea, dealerCode);
            if (retailerList != null && !retailerList.isEmpty()) {
                for (SclCustomerModel sclCustomer : retailerList) {
                    RetailerDetailsData retailerDetailsData = new RetailerDetailsData();
                    retailerDetailsData.setCustomerCode(sclCustomer.getUid());
                    retailerDetailsData.setCustomerName(sclCustomer.getName());
                    retailerDetailsData.setCustomerPotential(sclCustomer.getCounterPotential() != null ? sclCustomer.getCounterPotential() : 0);
                    retailerDetailsData.setErpCustomerNo(sclCustomer.getCustomerNo());
                    retailerDetailsData.setCySales(0.0);
                    retailerDetailsData.setPlanSales(0.0);
                    retailerDetailsData.setTotalTarget(0.0);
                    retailerDetailsData.setMonthWiseSkuTarget(monthWiseTargetDataList);
                    retailerDetailsData.setIsNewRetailerOnboarded(true);
                    list.add(retailerDetailsData);
                }
            }
            return list;
        }

    public static String theMonth(int month){
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        // Ensure month is within the valid range (0 to 11)
        int validMonth = (month % 12 + 12) % 12;
        return monthNames[validMonth];
    }

    @Override
        public MonthlySalesTargetSettingListData viewMonthlySalesTargetForDealers(String subArea) {
        SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
        BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
        SubAreaMasterModel subAreaModel = territoryManagementService.getTerritoryById(subArea);
        AnnualSalesModel annualSalesModel = salesPlanningService.viewPlannedSalesforDealersRetailersMonthWise(subAreaModel.getPk().toString(), currentUser, brand);
        MonthlySalesTargetSettingListData monthlySalesTargetSettingListData=new MonthlySalesTargetSettingListData();

        double bucketDivide=0.0;
        StringBuilder str=new StringBuilder();

        //need to remove +1 from year and handle incase of no annual target for any month

        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        int nextMonth = (cal.get(Calendar.MONTH) + 1) % 12;
        int year = cal.get(Calendar.YEAR);

        String currentMonth = theMonth(nextMonth);

        String currentMonthName=null;

        //temp changes
        if(nextMonth>=0 && nextMonth<=2) {
            currentMonthName= String.valueOf(str.append(currentMonth).append("-").append(LocalDate.now().getYear() + 1));
        }
        if(nextMonth>=3 && nextMonth<=11){
            currentMonthName = String.valueOf(str.append(currentMonth).append("-").append(LocalDate.now().getYear()));
        }

        List<MonthlySalesTargetSettingData> monthlySalesTargetSettingDataList = new ArrayList<>();
        if(annualSalesModel!=null) {
            if (annualSalesModel.getDealerRevisedAnnualSales() != null && !annualSalesModel.getDealerRevisedAnnualSales().isEmpty()) {
                for (DealerRevisedAnnualSalesModel dealerRevisedAnnualSalesModel : annualSalesModel.getDealerRevisedAnnualSales()) {
                    if ((dealerRevisedAnnualSalesModel.getIsExistingDealerRevisedForReview().equals(false)) && (dealerRevisedAnnualSalesModel.getIsNewDealerOnboarded().equals(false))) {
                        if (dealerRevisedAnnualSalesModel.getSubAreaMaster().equals(subAreaModel)) {
                            monthlySalesTargetSettingListData.setSubArea(subAreaModel.getTaluka());
                            monthlySalesTargetSettingListData.setSubAreaId(subAreaModel.getPk().toString());
                        }
                        MonthlySalesTargetSettingData monthlySalesTargetSettingData = new MonthlySalesTargetSettingData();
                        monthlySalesTargetSettingData.setCustomerCode(dealerRevisedAnnualSalesModel.getCustomerCode());
                        monthlySalesTargetSettingData.setCustomerName(dealerRevisedAnnualSalesModel.getCustomerName());
                        monthlySalesTargetSettingData.setCustomerPotential(dealerRevisedAnnualSalesModel.getCustomerPotential() != null ? dealerRevisedAnnualSalesModel.getCustomerPotential() : 0.0);
                        List<MonthlySKUData> monthlySKUDataList = new ArrayList<>();
                        for (ProductModel sku : dealerRevisedAnnualSalesModel.getListOfSkus()) {
                            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel = getSalesPlanningDao().fetchRevisedMonthWiseSkuDetails(subAreaModel.getPk().toString(), currentUser, currentMonthName, dealerRevisedAnnualSalesModel.getCustomerCode(), sku.getCode(), annualSalesModel.getIsAnnualSalesRevised());

                            if (monthWiseAnnualTargetModel.getProductCode().equals(sku.getCode()) &&
                                    monthWiseAnnualTargetModel.getCustomerCode().equals(dealerRevisedAnnualSalesModel.getCustomerCode())) {
                                if (monthWiseAnnualTargetModel.getMonthYear().equals(currentMonthName)) {
                                    MonthlySKUData monthlySKUData = new MonthlySKUData();
                                    monthlySKUData.setProductCode(sku.getCode());
                                    monthlySKUData.setProductName(sku.getName());
                                    monthlySKUData.setPremium(monthWiseAnnualTargetModel.getPremium());
                                    if (monthWiseAnnualTargetModel.getMonthTarget() != 0.0) {
                                        bucketDivide = Math.round(monthWiseAnnualTargetModel.getMonthTarget() / 3);
                                        monthlySKUData.setBucket1(bucketDivide);
                                        monthlySKUData.setBucket2(bucketDivide);
                                        double sumOfAllBucket = monthlySKUData.getBucket1() + monthlySKUData.getBucket2();
                                        monthlySKUData.setBucket3(monthWiseAnnualTargetModel.getMonthTarget() - sumOfAllBucket);
                                        monthlySKUData.setPlannedTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                        monthlySKUData.setRevisedTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                    } else {
                                        monthlySKUData.setBucket1(0.0);
                                        monthlySKUData.setBucket2(0.0);
                                        monthlySKUData.setBucket3(0.0);
                                        monthlySKUData.setPlannedTarget(0.0);
                                        monthlySKUData.setRevisedTarget(0.0);
                                    }
                                    monthlySKUDataList.add(monthlySKUData);
                                }
                            }
                            monthlySalesTargetSettingData.setMonthlySkuDataList(monthlySKUDataList);
                        }

                        MonthWiseAnnualTargetModel monthWiseAnnualTargetModel = getSalesPlanningDao().fetchDealerRevisedMonthWiseTargetDetails(dealerRevisedAnnualSalesModel.getCustomerCode(), subAreaModel.getPk().toString(), currentMonthName, currentUser, true);
                        if (monthWiseAnnualTargetModel != null) {
                            if (monthWiseAnnualTargetModel.getCustomerCode().equals(monthlySalesTargetSettingData.getCustomerCode())) {
                                if (monthWiseAnnualTargetModel.getMonthYear().equals(currentMonthName)) {
                                    monthlySalesTargetSettingData.setMonthName(currentMonthName);
                                    /*String[] splitForYear= currentMonthName.split("-");
                                    if (splitForYear.length == 2) {
                                        String year1 = splitForYear[1];
                                        monthlySalesTargetSettingData.setMonthYear(year1);
                                    }*/
                                    String formattedMonth= currentMonth.concat("-").concat(String.valueOf(year));
                                    monthlySalesTargetSettingData.setMonthYear(formattedMonth);
                                    double dealerBucket1 = 0.0, dealerBucket2 = 0.0, dealerBucket3 = 0.0, dealerPlannedTarget = 0.0, dealerRevisedTarget = 0.0;
                                    for (MonthlySKUData monthlySKUData : monthlySalesTargetSettingData.getMonthlySkuDataList()) {
                                        dealerBucket1 += monthlySKUData.getBucket1();
                                        dealerBucket2 += monthlySKUData.getBucket2();
                                        dealerBucket3 += monthlySKUData.getBucket3();
                                        dealerPlannedTarget += monthlySKUData.getPlannedTarget();
                                        dealerRevisedTarget += monthlySKUData.getRevisedTarget();
                                    }

                                    monthlySalesTargetSettingData.setBucket1(dealerBucket1);
                                    monthlySalesTargetSettingData.setBucket2(dealerBucket2);
                                    monthlySalesTargetSettingData.setBucket3(dealerBucket3);
                                    monthlySalesTargetSettingData.setPlannedTarget(dealerPlannedTarget);
                                    monthlySalesTargetSettingData.setRevisedTarget(dealerRevisedTarget);
                                    monthlySalesTargetSettingDataList.add(monthlySalesTargetSettingData);
                                }
                            }
                        }
                        monthlySalesTargetSettingListData.setMonthlySalesTargetSettingData(monthlySalesTargetSettingDataList);
                    }
                }
            }
        }
        return monthlySalesTargetSettingListData;
    }

    @Override
    public boolean submitPlannedMonthlySalesTargetSettingForDealers(MonthlySalesTargetSettingListData monthlySalesTargetSettingListData) {
        return salesPlanningService.submitPlannedMonthlySalesTargetSettingForDealers(monthlySalesTargetSettingListData);
    }

    @Override
    public boolean submitRevisedMonthlySalesTargetSettingForDealers(MonthlySalesTargetSettingListData monthlySalesTargetSettingListData) {
        return salesPlanningService.submitRevisedMonthlySalesTargetSettingForDealers(monthlySalesTargetSettingListData);
    }

    @Override
    public MonthlySalesTargetSettingListData viewMonthlySalesTargetForPlannedTab(String subArea) {

        List<MonthlySalesTargetSettingData> monthlySalesTargetSettingDataList = new ArrayList<>();
        SclUserModel sclUser = (SclUserModel) userService.getCurrentUser();

        SubAreaMasterModel subAreaMaster=territoryManagementService.getTerritoryById(subArea);
        MonthlySalesTargetSettingListData monthlySalesTargetSettingListData = new MonthlySalesTargetSettingListData();
        MonthlySalesModel monthlySalesModel = salesPlanningService.getMonthlySalesModelDetail(sclUser, subArea,baseSiteService.getCurrentBaseSite());
     //   List<DealerPlannedMonthlySalesModel> dealerPlannedMonthlySaleList = salesPlanningService.fetchDealerPlannedMonthlySalesDetails(subAreaMaster.getPk().toString(), sclUser);
        if(monthlySalesModel!=null) {
            if(monthlySalesModel.getActionPerformed()==null || (monthlySalesModel.getActionPerformed()!=null
                    && monthlySalesModel.getActionPerformed().equals(WorkflowActions.REJECTED)) ||
                    monthlySalesModel.getIsMonthlySalesReviewed().equals(false)) {
                if (monthlySalesModel.getDealerPlannedMonthlySales() != null && !monthlySalesModel.getDealerPlannedMonthlySales().isEmpty()) {
                    for (DealerPlannedMonthlySalesModel dealerPlannedMonthlySale : monthlySalesModel.getDealerPlannedMonthlySales()) {
                        MonthlySalesTargetSettingData monthlySalesTargetSettingData = new MonthlySalesTargetSettingData();
                        monthlySalesTargetSettingData.setCustomerCode(dealerPlannedMonthlySale.getCustomerCode());
                        monthlySalesTargetSettingData.setCustomerName(dealerPlannedMonthlySale.getCustomerName());
                        monthlySalesTargetSettingData.setCustomerPotential(dealerPlannedMonthlySale.getCustomerPotential());
                        monthlySalesTargetSettingData.setMonthYear(dealerPlannedMonthlySale.getMonthYear());
                        monthlySalesTargetSettingData.setMonthName(dealerPlannedMonthlySale.getMonthName());
                        List<MonthlySKUData> monthlySKUDataList = new ArrayList<>();
                        for (ProductModel sku : dealerPlannedMonthlySale.getListOfSkus()) {
                            ProductSaleModel productSaleModel = salesPlanningService.fetchProductSaleForDealerPlannedMonthlySales(subAreaMaster.getPk().toString(), sclUser, sku.getCode(), dealerPlannedMonthlySale.getCustomerCode(), dealerPlannedMonthlySale.getMonthName(), dealerPlannedMonthlySale.getMonthYear());
                            //  if (listOfSkus.getProductSale() != null && !listOfSkus.getProductSale().isEmpty())
                            //   for (ProductSaleModel productSaleModel : listOfSkus.getProductSale()) {
                            MonthlySKUData monthlySKUData = new MonthlySKUData();
                            monthlySKUData.setProductCode(productSaleModel.getProductCode());
                            monthlySKUData.setProductName(productSaleModel.getProductName());
                            monthlySKUData.setBucket1(productSaleModel.getBucket1());
                            monthlySKUData.setBucket2(productSaleModel.getBucket2());
                            monthlySKUData.setBucket3(productSaleModel.getBucket3());
                            monthlySKUData.setPlannedTarget(productSaleModel.getPlannedTarget());
                            monthlySKUData.setRevisedTarget(productSaleModel.getRevisedTarget());
                            monthlySKUDataList.add(monthlySKUData);
                            //     }
                        }
                        double dealerBucket1 = 0.0, dealerBucket2 = 0.0, dealerBucket3 = 0.0, dealerPlannedTarget = 0.0, dealerRevisedTarget = 0.0;
                        for (MonthlySKUData monthlySKUData : monthlySKUDataList) {
                            dealerBucket1 += monthlySKUData.getBucket1();
                            dealerBucket2 += monthlySKUData.getBucket2();
                            dealerBucket3 += monthlySKUData.getBucket3();
                            dealerPlannedTarget += monthlySKUData.getPlannedTarget();
                            dealerRevisedTarget += monthlySKUData.getRevisedTarget();
                        }
                        monthlySalesTargetSettingData.setPlannedTarget(dealerPlannedTarget);
                        monthlySalesTargetSettingData.setRevisedTarget(dealerRevisedTarget);
                        monthlySalesTargetSettingData.setBucket1(dealerBucket1);
                        monthlySalesTargetSettingData.setBucket2(dealerBucket2);
                        monthlySalesTargetSettingData.setBucket3(dealerBucket3);
                        monthlySalesTargetSettingData.setMonthlySkuDataList(monthlySKUDataList);
                        monthlySalesTargetSettingDataList.add(monthlySalesTargetSettingData);
                    }
                }
            }
            else if(monthlySalesModel.getActionPerformed()!=null && monthlySalesModel.getActionPerformed().equals(WorkflowActions.APPROVED)){
                    //dealer revised monthly sales should come
                    List<DealerRevisedMonthlySalesModel> dealerReviewedMonthlySaleList = salesPlanningService.fetchDealerReviewedMonthlySalesDetails(subAreaMaster.getPk().toString(), sclUser);
                    if (dealerReviewedMonthlySaleList != null && !dealerReviewedMonthlySaleList.isEmpty()) {
                        for (DealerRevisedMonthlySalesModel dealerReviewedMonthlySale : dealerReviewedMonthlySaleList) {
                            MonthlySalesTargetSettingData monthlySalesTargetSettingData = new MonthlySalesTargetSettingData();
                            monthlySalesTargetSettingData.setCustomerCode(dealerReviewedMonthlySale.getCustomerCode());
                            monthlySalesTargetSettingData.setCustomerName(dealerReviewedMonthlySale.getCustomerName());
                            monthlySalesTargetSettingData.setCustomerPotential(dealerReviewedMonthlySale.getCustomerPotential());
                            monthlySalesTargetSettingData.setMonthYear(dealerReviewedMonthlySale.getMonthYear());
                            monthlySalesTargetSettingData.setMonthName(dealerReviewedMonthlySale.getMonthName());
                            List<MonthlySKUData> monthlySKUDataList = new ArrayList<>();
                            for (ProductModel sku : dealerReviewedMonthlySale.getListOfSkus()) {
                                ProductSaleModel productSaleModel = salesPlanningService.fetchProductSaleForDealerRevisedMonthlySales(subAreaMaster.getPk().toString(), sclUser, sku.getCode(), dealerReviewedMonthlySale.getCustomerCode(), dealerReviewedMonthlySale.getMonthName(), dealerReviewedMonthlySale.getMonthYear());

                                // if (listOfSkus.getProductSale() != null && !listOfSkus.getProductSale().isEmpty())
                                //       for (ProductSaleModel productSaleModel : listOfSkus.getProductSale()) {
                                MonthlySKUData monthlySKUData = new MonthlySKUData();
                                monthlySKUData.setProductCode(productSaleModel.getProductCode());
                                monthlySKUData.setProductName(productSaleModel.getProductName());
                                monthlySKUData.setBucket1(productSaleModel.getBucket1());
                                monthlySKUData.setBucket2(productSaleModel.getBucket2());
                                monthlySKUData.setBucket3(productSaleModel.getBucket3());
                                monthlySKUData.setPlannedTarget(productSaleModel.getPlannedTarget());
                                monthlySKUData.setRevisedTarget(productSaleModel.getRevisedTarget());
                                monthlySKUDataList.add(monthlySKUData);
                                //     }
                            }
                            double dealerBucket1 = 0.0, dealerBucket2 = 0.0, dealerBucket3 = 0.0, dealerPlannedTarget = 0.0, dealerRevisedTarget = 0.0;
                            for (MonthlySKUData monthlySKUData : monthlySKUDataList) {
                                dealerBucket1 += monthlySKUData.getBucket1();
                                dealerBucket2 += monthlySKUData.getBucket2();
                                dealerBucket3 += monthlySKUData.getBucket3();
                                dealerPlannedTarget += monthlySKUData.getPlannedTarget();
                                dealerRevisedTarget += monthlySKUData.getRevisedTarget();
                            }
                            monthlySalesTargetSettingData.setPlannedTarget(dealerPlannedTarget);
                            monthlySalesTargetSettingData.setRevisedTarget(dealerRevisedTarget);
                            monthlySalesTargetSettingData.setBucket1(dealerBucket1);
                            monthlySalesTargetSettingData.setBucket2(dealerBucket2);
                            monthlySalesTargetSettingData.setBucket3(dealerBucket3);
                            monthlySalesTargetSettingData.setMonthlySkuDataList(monthlySKUDataList);
                            monthlySalesTargetSettingDataList.add(monthlySalesTargetSettingData);
                        }
                    }
            }
        }
        monthlySalesTargetSettingListData.setSubArea(subAreaMaster.getTaluka());
        monthlySalesTargetSettingListData.setSubAreaId(subAreaMaster.getPk().toString());
        monthlySalesTargetSettingListData.setMonthlySalesTargetSettingData(monthlySalesTargetSettingDataList);
        return monthlySalesTargetSettingListData;
    }

    //to be checked
    @Override
    public MonthlySalesTargetSettingListData viewMonthlyRevisedSalesTargetForRevisedTarget(String subArea) {
       List<MonthlySKUData> monthlySKUDataList = new ArrayList<>();
        List<MonthlySalesTargetSettingData> monthlySalesTargetSettingDataList = new ArrayList<>();

        SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
        MonthlySalesModel monthlySalesModel = salesPlanningService.viewMonthlySalesTargetForPlannedTab(subArea, currentUser);
        MonthlySalesTargetSettingListData monthlySalesTargetSettingListData=new MonthlySalesTargetSettingListData();
        //if(monthlySalesModel!=null && monthlySalesModel.getIsMonthlySalesRevised().equals(true))
        //{
            if(monthlySalesModel.getDealerPlannedMonthlySales()!=null && !monthlySalesModel.getDealerPlannedMonthlySales().isEmpty()) {
                for (DealerPlannedMonthlySalesModel dealerPlannedMonthlySalesModel : monthlySalesModel.getDealerPlannedMonthlySales()) {
                for (SubAreaListingModel subAreaListingModel : monthlySalesModel.getSubAreaList()) {
                    if (subAreaListingModel.getSubAreaName().equals(dealerPlannedMonthlySalesModel.getSubarea())){
                        monthlySalesTargetSettingListData.setSubArea(subAreaListingModel.getSubAreaName());
                    }
                }
                monthlySalesTargetSettingListData.setTotalTarget(monthlySalesModel.getTotalTarget());
                monthlySalesTargetSettingListData.setTotalBucket1(monthlySalesModel.getTotalBucket1());
                monthlySalesTargetSettingListData.setTotalBucket2(monthlySalesModel.getTotalBucket2());
                monthlySalesTargetSettingListData.setTotalBucket3(monthlySalesModel.getTotalBucket3());
                monthlySalesTargetSettingListData.setTotalPlannedTarget(monthlySalesModel.getTotalPlannedTarget());
                monthlySalesTargetSettingListData.setTotalRevisedTarget(monthlySalesModel.getTotalRevisedTarget());
                    MonthlySalesTargetSettingData monthlySalesTargetSettingData = new MonthlySalesTargetSettingData();
                    monthlySalesTargetSettingData.setCustomerCode(dealerPlannedMonthlySalesModel.getCustomerCode());
                    monthlySalesTargetSettingData.setCustomerName(dealerPlannedMonthlySalesModel.getCustomerName());
                    monthlySalesTargetSettingData.setCustomerPotential(dealerPlannedMonthlySalesModel.getCustomerPotential());
                    monthlySalesTargetSettingData.setPlannedTarget(dealerPlannedMonthlySalesModel.getPlannedTarget());
                    monthlySalesTargetSettingData.setRevisedTarget(dealerPlannedMonthlySalesModel.getRevisedTarget());
                    monthlySalesTargetSettingData.setBucket1(dealerPlannedMonthlySalesModel.getBucket1());
                    monthlySalesTargetSettingData.setBucket2(dealerPlannedMonthlySalesModel.getBucket2());
                    monthlySalesTargetSettingData.setBucket3(dealerPlannedMonthlySalesModel.getBucket3());
                    monthlySalesTargetSettingData.setMonthName(dealerPlannedMonthlySalesModel.getMonthName());
                    monthlySalesTargetSettingData.setMonthYear(dealerPlannedMonthlySalesModel.getMonthYear());
                    for (ProductModel listOfSkus : dealerPlannedMonthlySalesModel.getListOfSkus()) {
                        MonthlySKUData monthlySKUData = new MonthlySKUData();
                        monthlySKUData.setProductName(listOfSkus.getCode());
                        monthlySKUData.setProductName(listOfSkus.getName());
                        monthlySKUData.setPlannedTarget(listOfSkus.getPlannedTarget());
                        monthlySKUData.setRevisedTarget(listOfSkus.getRevisedTarget());
                        monthlySKUData.setBucket1(listOfSkus.getBucket1());
                        monthlySKUData.setBucket2(listOfSkus.getBucket2());
                        monthlySKUData.setBucket3(listOfSkus.getBucket3());
                        monthlySKUDataList.add(monthlySKUData);
                    }
                    monthlySalesTargetSettingData.setMonthlySkuDataList(monthlySKUDataList);
                    monthlySalesTargetSettingDataList.add(monthlySalesTargetSettingData);
                    monthlySalesTargetSettingListData.setMonthlySalesTargetSettingData(monthlySalesTargetSettingDataList);
                }
            }
       // }
        return monthlySalesTargetSettingListData;
    }

    @Override
    public boolean submitMonthlySalesTargetForReviewTab(MonthlySalesTargetSettingListData monthlySalesTargetSettingListData) {
        return salesPlanningService.submitMonthlySalesTargetForReviewTab(monthlySalesTargetSettingListData);
    }

    @Override
    public MonthlySalesTargetSettingListData viewMonthlyRevisedSalesTargetForReviewTab(String subArea) {

        /*List<MonthlySalesTargetSettingData> monthlySalesTargetSettingDataList = new ArrayList<>();
        SclUserModel sclUser = (SclUserModel) userService.getCurrentUser();
        //MonthlySalesModel monthlySalesModel = salesPlanningDao.viewMonthlySalesTargetForPlannedTab(subArea, sclUser);
        MonthlySalesTargetSettingListData monthlySalesTargetSettingListData=new MonthlySalesTargetSettingListData();
        String customerCode = null;
        Date firstDayOfMonth = null, lastDayOfMonth = null;
        final double[] sumOfQuantity = {0.0};
        List<List<Object>> lists = new ArrayList<>();
        List<DealerRevisedMonthlySalesModel> dealerRevisedMonthlySaleList=salesPlanningService.fetchDealerReviewedMonthlySalesDetails(subArea,sclUser);
                    if (dealerRevisedMonthlySaleList != null && !dealerRevisedMonthlySaleList.isEmpty()) {
                        for (DealerRevisedMonthlySalesModel dealerRevisedMonthlySale : dealerRevisedMonthlySaleList) {
                            //customerCode = dealerRevisedMonthlySale.getCustomerCode();
                            //firstDayOfMonth = getFirstDateOfMonth(getMonthNumber(dealerRevisedMonthlySale.getMonthName()));
                            //lastDayOfMonth = getLastDateOfMonth(getMonthNumber(dealerRevisedMonthlySale.getMonthName()));
                            //lists = salesPlanningService.viewMonthlyRevisedSalesTargetForReviewTab(subArea, baseSiteService.getCurrentBaseSite(), sclUser, customerCode, firstDayOfMonth, lastDayOfMonth);
                            *//*String finalCustomerCode = customerCode;
                            lists.forEach(objects -> {
                                if (!objects.isEmpty() && objects != null) {
                                    String dealerCode = (String) objects.get(0);
                                    Double qty = (Double) objects.get(2);
                                    Date date = (Date) objects.get(3);
                                    String productCode = (String) objects.get(4);
                                    String productName = (String) objects.get(5);
                                    if (dealerCode.equals(finalCustomerCode)) {
                                        if (date.getDay() <= 10) {
                                            sumOfQuantity[0] += qty;
                                        } else if (date.getDay() <= 20) {
                                            sumOfQuantity[1] += qty;
                                        } else {
                                            sumOfQuantity[2] += qty;
                                        }
                                    }
                                }
                            });*//*
                            MonthlySalesTargetSettingData monthlySalesTargetSettingData = new MonthlySalesTargetSettingData();
                            monthlySalesTargetSettingData.setCustomerCode(dealerRevisedMonthlySale.getCustomerCode());
                            monthlySalesTargetSettingData.setCustomerName(dealerRevisedMonthlySale.getCustomerName());
                            monthlySalesTargetSettingData.setCustomerPotential(dealerRevisedMonthlySale.getCustomerPotential());
                            monthlySalesTargetSettingData.setMonthYear(dealerRevisedMonthlySale.getMonthYear());
                            monthlySalesTargetSettingData.setMonthName(dealerRevisedMonthlySale.getMonthName());
                            monthlySalesTargetSettingData.setPlannedTarget(dealerRevisedMonthlySale.getPlannedTarget());
                            monthlySalesTargetSettingData.setRevisedTarget(dealerRevisedMonthlySale.getRevisedTarget());
                            monthlySalesTargetSettingData.setBucket1(dealerRevisedMonthlySale.getBucket1());
                            monthlySalesTargetSettingData.setBucket2(dealerRevisedMonthlySale.getBucket2());
                            monthlySalesTargetSettingData.setBucket3(dealerRevisedMonthlySale.getBucket3());
                            //monthlySalesTargetSettingData.setBucket1(sumOfQuantity[0]);
                            //monthlySalesTargetSettingData.setBucket2(sumOfQuantity[1]);
                            //monthlySalesTargetSettingData.setBucket3(sumOfQuantity[2]);
                            *//*List<MonthlySKUData> monthlySKUDataList = new ArrayList<>();
                            for (ProductModel listOfSkus : dealerRevisedMonthlySale.getListOfSkus()) {
                                //set product bucket
                                *//**//*lists.forEach(objects -> {
                                    if (!objects.isEmpty() && objects != null) {
                                        String dealerCode = (String) objects.get(0);
                                        Double qty = (Double) objects.get(2);
                                        Date date = (Date) objects.get(3);
                                        String productCode = (String) objects.get(4);
                                        String productName= (String) objects.get(5);
                                        if (listOfSkus.getCode().equals(productCode)) {
                                            if (date.getDay() <= 10) {
                                                sumOfQuantity[0] += qty;
                                            } else if (date.getDay() <= 20) {
                                                sumOfQuantity[1] += qty;
                                            } else {
                                                sumOfQuantity[2] += qty;
                                            }
                                        }
                                    }
                                });*//**//*
                                if (listOfSkus.getProductSale() != null && !listOfSkus.getProductSale().isEmpty())
                                    for (ProductSaleModel productSaleModel : listOfSkus.getProductSale()) {
                                        MonthlySKUData monthlySKUData = new MonthlySKUData();
                                        monthlySKUData.setProductCode(productSaleModel.getProductCode());
                                        monthlySKUData.setProductName(productSaleModel.getProductName());
                                        monthlySKUData.setBucket1(productSaleModel.getBucket1());
                                        monthlySKUData.setBucket2(productSaleModel.getBucket2());
                                        monthlySKUData.setBucket3(productSaleModel.getBucket3());
                                        monthlySKUData.setPlannedTarget(productSaleModel.getPlannedTarget());
                                        monthlySKUData.setRevisedTarget(productSaleModel.getRevisedTarget());
                                        monthlySKUDataList.add(monthlySKUData);
                                    }
                            }*//*
                            List<MonthlySKUData> monthlySKUDataList = new ArrayList<>();
                            for (ProductModel listOfSkus : dealerRevisedMonthlySale.getListOfSkus()) {
                                if (listOfSkus.getProductSale() != null && !listOfSkus.getProductSale().isEmpty())
                                    for (ProductSaleModel productSaleModel : listOfSkus.getProductSale()) {
                                        MonthlySKUData monthlySKUData = new MonthlySKUData();
                                        monthlySKUData.setProductCode(productSaleModel.getProductCode());
                                        monthlySKUData.setProductName(productSaleModel.getProductName());
                                        monthlySKUData.setBucket1(productSaleModel.getBucket1());
                                        monthlySKUData.setBucket2(productSaleModel.getBucket2());
                                        monthlySKUData.setBucket3(productSaleModel.getBucket3());
                                        monthlySKUData.setPlannedTarget(productSaleModel.getPlannedTarget());
                                        monthlySKUData.setRevisedTarget(productSaleModel.getRevisedTarget());
                                        monthlySKUDataList.add(monthlySKUData);
                                    }
                            }
                            monthlySalesTargetSettingData.setMonthlySkuDataList(monthlySKUDataList);
                            monthlySalesTargetSettingDataList.add(monthlySalesTargetSettingData);
                        }
                    }
        monthlySalesTargetSettingListData.setSubArea(subArea);
        monthlySalesTargetSettingListData.setMonthlySalesTargetSettingData(monthlySalesTargetSettingDataList);
        return  monthlySalesTargetSettingListData;*/
        List<MonthlySalesTargetSettingData> monthlySalesTargetSettingDataList = new ArrayList<>();
        SclUserModel sclUser = (SclUserModel) userService.getCurrentUser();
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        SubAreaMasterModel subAreaMaster=territoryManagementService.getTerritoryById(subArea);
        MonthlySalesModel monthlySalesModel =salesPlanningService.getMonthlySalesModelDetail(sclUser,subAreaMaster.getPk().toString(),currentBaseSite);

        MonthlySalesTargetSettingListData monthlySalesTargetSettingListData = new MonthlySalesTargetSettingListData();
        if(monthlySalesModel!=null) {
            if (monthlySalesModel.getIsMonthlySalesReviewed().equals(false) &&
                    monthlySalesModel.getActionPerformed() == null) {
                List<DealerPlannedMonthlySalesModel> dealerPlannedMonthlySaleList = salesPlanningService.fetchDealerPlannedMonthlySalesDetails(subAreaMaster.getPk().toString(), sclUser);
                if (dealerPlannedMonthlySaleList != null && !dealerPlannedMonthlySaleList.isEmpty()) {
                    for (DealerPlannedMonthlySalesModel dealerPlannedMonthlySale : dealerPlannedMonthlySaleList) {
                        MonthlySalesTargetSettingData monthlySalesTargetSettingData = new MonthlySalesTargetSettingData();
                        monthlySalesTargetSettingData.setCustomerCode(dealerPlannedMonthlySale.getCustomerCode());
                        monthlySalesTargetSettingData.setCustomerName(dealerPlannedMonthlySale.getCustomerName());
                        monthlySalesTargetSettingData.setCustomerPotential(dealerPlannedMonthlySale.getCustomerPotential());
                        monthlySalesTargetSettingData.setMonthYear(dealerPlannedMonthlySale.getMonthYear());
                        monthlySalesTargetSettingData.setMonthName(dealerPlannedMonthlySale.getMonthName());
                        List<MonthlySKUData> monthlySKUDataList = new ArrayList<>();
                        for (ProductModel sku : dealerPlannedMonthlySale.getListOfSkus()) {
                            ProductSaleModel productSaleModel = salesPlanningService.fetchProductSaleForDealerPlannedMonthlySales(subAreaMaster.getPk().toString(), sclUser, sku.getCode(), dealerPlannedMonthlySale.getCustomerCode(), dealerPlannedMonthlySale.getMonthName(), dealerPlannedMonthlySale.getMonthYear());
                            //  if (listOfSkus.getProductSale() != null && !listOfSkus.getProductSale().isEmpty())
                            //   for (ProductSaleModel productSaleModel : listOfSkus.getProductSale()) {
                            MonthlySKUData monthlySKUData = new MonthlySKUData();
                            monthlySKUData.setProductCode(productSaleModel.getProductCode());
                            monthlySKUData.setProductName(productSaleModel.getProductName());
                            monthlySKUData.setBucket1(productSaleModel.getBucket1());
                            monthlySKUData.setBucket2(productSaleModel.getBucket2());
                            monthlySKUData.setBucket3(productSaleModel.getBucket3());
                            monthlySKUData.setPlannedTarget(productSaleModel.getPlannedTarget());
                            monthlySKUData.setRevisedTarget(productSaleModel.getRevisedTarget());
                            monthlySKUDataList.add(monthlySKUData);
                            //     }
                        }
                        double dealerBucket1 = 0.0, dealerBucket2 = 0.0, dealerBucket3 = 0.0, dealerPlannedTarget = 0.0, dealerRevisedTarget = 0.0;
                        for (MonthlySKUData monthlySKUData : monthlySKUDataList) {
                            dealerBucket1 += monthlySKUData.getBucket1();
                            dealerBucket2 += monthlySKUData.getBucket2();
                            dealerBucket3 += monthlySKUData.getBucket3();
                            dealerPlannedTarget += monthlySKUData.getPlannedTarget();
                            dealerRevisedTarget += monthlySKUData.getRevisedTarget();
                        }
                        monthlySalesTargetSettingData.setPlannedTarget(dealerPlannedTarget);
                        monthlySalesTargetSettingData.setRevisedTarget(dealerRevisedTarget);
                        monthlySalesTargetSettingData.setBucket1(dealerBucket1);
                        monthlySalesTargetSettingData.setBucket2(dealerBucket2);
                        monthlySalesTargetSettingData.setBucket3(dealerBucket3);
                        monthlySalesTargetSettingData.setMonthlySkuDataList(monthlySKUDataList);
                        monthlySalesTargetSettingDataList.add(monthlySalesTargetSettingData);
                    }
                }
            }
            else if (monthlySalesModel.getActionPerformed() != null && monthlySalesModel.getActionPerformed().equals(WorkflowActions.REJECTED)) {
                List<DealerPlannedMonthlySalesModel> dealerPlannedMonthlySaleList = salesPlanningService.fetchDealerPlannedMonthlySalesDetails(subAreaMaster.getPk().toString(), sclUser);
                if (dealerPlannedMonthlySaleList != null && !dealerPlannedMonthlySaleList.isEmpty()) {
                    for (DealerPlannedMonthlySalesModel dealerPlannedMonthlySale : dealerPlannedMonthlySaleList) {
                        MonthlySalesTargetSettingData monthlySalesTargetSettingData = new MonthlySalesTargetSettingData();
                        monthlySalesTargetSettingData.setCustomerCode(dealerPlannedMonthlySale.getCustomerCode());
                        monthlySalesTargetSettingData.setCustomerName(dealerPlannedMonthlySale.getCustomerName());
                        monthlySalesTargetSettingData.setCustomerPotential(dealerPlannedMonthlySale.getCustomerPotential());
                        monthlySalesTargetSettingData.setMonthYear(dealerPlannedMonthlySale.getMonthYear());
                        monthlySalesTargetSettingData.setMonthName(dealerPlannedMonthlySale.getMonthName());
                        List<MonthlySKUData> monthlySKUDataList = new ArrayList<>();
                        for (ProductModel sku : dealerPlannedMonthlySale.getListOfSkus()) {
                            ProductSaleModel productSaleModel = salesPlanningService.fetchProductSaleForDealerPlannedMonthlySales(subAreaMaster.getPk().toString(), sclUser, sku.getCode(), dealerPlannedMonthlySale.getCustomerCode(), dealerPlannedMonthlySale.getMonthName(), dealerPlannedMonthlySale.getMonthYear());
                            //  if (listOfSkus.getProductSale() != null && !listOfSkus.getProductSale().isEmpty())
                            //   for (ProductSaleModel productSaleModel : listOfSkus.getProductSale()) {
                            MonthlySKUData monthlySKUData = new MonthlySKUData();
                            monthlySKUData.setProductCode(productSaleModel.getProductCode());
                            monthlySKUData.setProductName(productSaleModel.getProductName());
                            monthlySKUData.setBucket1(productSaleModel.getBucket1());
                            monthlySKUData.setBucket2(productSaleModel.getBucket2());
                            monthlySKUData.setBucket3(productSaleModel.getBucket3());
                            monthlySKUData.setPlannedTarget(productSaleModel.getPlannedTarget());
                            monthlySKUData.setRevisedTarget(productSaleModel.getRevisedTarget());
                            monthlySKUDataList.add(monthlySKUData);
                            //     }
                        }
                        double dealerBucket1 = 0.0, dealerBucket2 = 0.0, dealerBucket3 = 0.0, dealerPlannedTarget = 0.0, dealerRevisedTarget = 0.0;
                        for (MonthlySKUData monthlySKUData : monthlySKUDataList) {
                            dealerBucket1 += monthlySKUData.getBucket1();
                            dealerBucket2 += monthlySKUData.getBucket2();
                            dealerBucket3 += monthlySKUData.getBucket3();
                            dealerPlannedTarget += monthlySKUData.getPlannedTarget();
                            dealerRevisedTarget += monthlySKUData.getRevisedTarget();
                        }
                        monthlySalesTargetSettingData.setPlannedTarget(dealerPlannedTarget);
                        monthlySalesTargetSettingData.setRevisedTarget(dealerRevisedTarget);
                        monthlySalesTargetSettingData.setBucket1(dealerBucket1);
                        monthlySalesTargetSettingData.setBucket2(dealerBucket2);
                        monthlySalesTargetSettingData.setBucket3(dealerBucket3);
                        monthlySalesTargetSettingData.setMonthlySkuDataList(monthlySKUDataList);
                        monthlySalesTargetSettingDataList.add(monthlySalesTargetSettingData);
                    }
                }
            }
            else if ( monthlySalesModel.getIsMonthlySalesReviewed().equals(true) &&
                    monthlySalesModel.getActionPerformed() == null ){
                //dealer revised monthly sales should come
                List<DealerRevisedMonthlySalesModel> dealerReviewedMonthlySaleList = salesPlanningService.fetchDealerReviewedMonthlySalesDetails(subAreaMaster.getPk().toString(), sclUser);
                if (dealerReviewedMonthlySaleList != null && !dealerReviewedMonthlySaleList.isEmpty()) {
                    for (DealerRevisedMonthlySalesModel dealerReviewedMonthlySale : dealerReviewedMonthlySaleList) {
                        MonthlySalesTargetSettingData monthlySalesTargetSettingData = new MonthlySalesTargetSettingData();
                        monthlySalesTargetSettingData.setCustomerCode(dealerReviewedMonthlySale.getCustomerCode());
                        monthlySalesTargetSettingData.setCustomerName(dealerReviewedMonthlySale.getCustomerName());
                        monthlySalesTargetSettingData.setCustomerPotential(dealerReviewedMonthlySale.getCustomerPotential());
                        monthlySalesTargetSettingData.setMonthYear(dealerReviewedMonthlySale.getMonthYear());
                        monthlySalesTargetSettingData.setMonthName(dealerReviewedMonthlySale.getMonthName());
                        List<MonthlySKUData> monthlySKUDataList = new ArrayList<>();
                        for (ProductModel sku : dealerReviewedMonthlySale.getListOfSkus()) {
                            ProductSaleModel productSaleModel = salesPlanningService.fetchProductSaleForDealerRevisedMonthlySales(subAreaMaster.getPk().toString(), sclUser, sku.getCode(), dealerReviewedMonthlySale.getCustomerCode(), dealerReviewedMonthlySale.getMonthName(), dealerReviewedMonthlySale.getMonthYear());

                            // if (listOfSkus.getProductSale() != null && !listOfSkus.getProductSale().isEmpty())
                            //       for (ProductSaleModel productSaleModel : listOfSkus.getProductSale()) {
                            MonthlySKUData monthlySKUData = new MonthlySKUData();
                            monthlySKUData.setProductCode(productSaleModel.getProductCode());
                            monthlySKUData.setProductName(productSaleModel.getProductName());
                            monthlySKUData.setBucket1(productSaleModel.getBucket1());
                            monthlySKUData.setBucket2(productSaleModel.getBucket2());
                            monthlySKUData.setBucket3(productSaleModel.getBucket3());
                            monthlySKUData.setPlannedTarget(productSaleModel.getPlannedTarget());
                            monthlySKUData.setRevisedTarget(productSaleModel.getRevisedTarget());
                            monthlySKUDataList.add(monthlySKUData);
                            //     }
                        }
                        double dealerBucket1 = 0.0, dealerBucket2 = 0.0, dealerBucket3 = 0.0, dealerPlannedTarget = 0.0, dealerRevisedTarget = 0.0;
                        for (MonthlySKUData monthlySKUData : monthlySKUDataList) {
                            dealerBucket1 += monthlySKUData.getBucket1();
                            dealerBucket2 += monthlySKUData.getBucket2();
                            dealerBucket3 += monthlySKUData.getBucket3();
                            dealerPlannedTarget += monthlySKUData.getPlannedTarget();
                            dealerRevisedTarget += monthlySKUData.getRevisedTarget();
                        }
                        monthlySalesTargetSettingData.setPlannedTarget(dealerPlannedTarget);
                        monthlySalesTargetSettingData.setRevisedTarget(dealerRevisedTarget);
                        monthlySalesTargetSettingData.setBucket1(dealerBucket1);
                        monthlySalesTargetSettingData.setBucket2(dealerBucket2);
                        monthlySalesTargetSettingData.setBucket3(dealerBucket3);
                        monthlySalesTargetSettingData.setMonthlySkuDataList(monthlySKUDataList);
                        monthlySalesTargetSettingDataList.add(monthlySalesTargetSettingData);
                    }
                }
            }else if(monthlySalesModel.getActionPerformed() != null  && monthlySalesModel.getActionPerformed().equals(WorkflowActions.APPROVED)){
                {
                    //dealer revised monthly sales should come
                    List<DealerRevisedMonthlySalesModel> dealerReviewedMonthlySaleList = salesPlanningService.fetchDealerReviewedMonthlySalesDetails(subAreaMaster.getPk().toString(), sclUser);
                    if (dealerReviewedMonthlySaleList != null && !dealerReviewedMonthlySaleList.isEmpty()) {
                        for (DealerRevisedMonthlySalesModel dealerReviewedMonthlySale : dealerReviewedMonthlySaleList) {
                            MonthlySalesTargetSettingData monthlySalesTargetSettingData = new MonthlySalesTargetSettingData();
                            monthlySalesTargetSettingData.setCustomerCode(dealerReviewedMonthlySale.getCustomerCode());
                            monthlySalesTargetSettingData.setCustomerName(dealerReviewedMonthlySale.getCustomerName());
                            monthlySalesTargetSettingData.setCustomerPotential(dealerReviewedMonthlySale.getCustomerPotential());
                            monthlySalesTargetSettingData.setMonthYear(dealerReviewedMonthlySale.getMonthYear());
                            monthlySalesTargetSettingData.setMonthName(dealerReviewedMonthlySale.getMonthName());
                            List<MonthlySKUData> monthlySKUDataList = new ArrayList<>();
                            for (ProductModel sku : dealerReviewedMonthlySale.getListOfSkus()) {
                                ProductSaleModel productSaleModel = salesPlanningService.fetchProductSaleForDealerRevisedMonthlySales(subAreaMaster.getPk().toString(), sclUser, sku.getCode(), dealerReviewedMonthlySale.getCustomerCode(), dealerReviewedMonthlySale.getMonthName(), dealerReviewedMonthlySale.getMonthYear());

                                // if (listOfSkus.getProductSale() != null && !listOfSkus.getProductSale().isEmpty())
                                //       for (ProductSaleModel productSaleModel : listOfSkus.getProductSale()) {
                                MonthlySKUData monthlySKUData = new MonthlySKUData();
                                monthlySKUData.setProductCode(productSaleModel.getProductCode());
                                monthlySKUData.setProductName(productSaleModel.getProductName());
                                monthlySKUData.setBucket1(productSaleModel.getBucket1());
                                monthlySKUData.setBucket2(productSaleModel.getBucket2());
                                monthlySKUData.setBucket3(productSaleModel.getBucket3());
                                monthlySKUData.setPlannedTarget(productSaleModel.getPlannedTarget());
                                monthlySKUData.setRevisedTarget(productSaleModel.getRevisedTarget());
                                monthlySKUDataList.add(monthlySKUData);
                                //     }
                            }
                            double dealerBucket1 = 0.0, dealerBucket2 = 0.0, dealerBucket3 = 0.0, dealerPlannedTarget = 0.0, dealerRevisedTarget = 0.0;
                            for (MonthlySKUData monthlySKUData : monthlySKUDataList) {
                                dealerBucket1 += monthlySKUData.getBucket1();
                                dealerBucket2 += monthlySKUData.getBucket2();
                                dealerBucket3 += monthlySKUData.getBucket3();
                                dealerPlannedTarget += monthlySKUData.getPlannedTarget();
                                dealerRevisedTarget += monthlySKUData.getRevisedTarget();
                            }
                            monthlySalesTargetSettingData.setPlannedTarget(dealerPlannedTarget);
                            monthlySalesTargetSettingData.setRevisedTarget(dealerRevisedTarget);
                            monthlySalesTargetSettingData.setBucket1(dealerBucket1);
                            monthlySalesTargetSettingData.setBucket2(dealerBucket2);
                            monthlySalesTargetSettingData.setBucket3(dealerBucket3);
                            monthlySalesTargetSettingData.setMonthlySkuDataList(monthlySKUDataList);
                            monthlySalesTargetSettingDataList.add(monthlySalesTargetSettingData);
                        }
                    }
                }
            }
        }
        monthlySalesTargetSettingListData.setSubArea(subAreaMaster.getTaluka());
        monthlySalesTargetSettingListData.setSubAreaId(subAreaMaster.getPk().toString());
        monthlySalesTargetSettingListData.setMonthlySalesTargetSettingData(monthlySalesTargetSettingDataList);
        return monthlySalesTargetSettingListData;
}

    @Override
    public MonthlySalesTargetSettingListData viewMonthlyRevisedSalesTargetForReviewTabForTSM(String subArea) {
        List<MonthlySalesTargetSettingData> monthlySalesTargetSettingDataList = new ArrayList<>();
        SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        SubAreaMasterModel subAreaMaster=territoryManagementService.getTerritoryById(subArea);
        FilterTalukaData filterTalukaData=new FilterTalukaData();
        List<SclUserModel> soForUser = territoryManagementService.getSOForUser(filterTalukaData);
        //add brand


        MonthlySalesTargetSettingListData monthlySalesTargetSettingListData = new MonthlySalesTargetSettingListData();
        if(soForUser!=null && !soForUser.isEmpty()) {
            for (SclUserModel sclUserModel : soForUser) {
                MonthlySalesModel monthlySalesModel =salesPlanningService.getMonthlySalesModelDetail(sclUserModel,subAreaMaster.getPk().toString(),currentBaseSite);
            if (monthlySalesModel != null && monthlySalesModel.getIsMonthlySalesReviewed().equals(false)) {
                List<DealerPlannedMonthlySalesModel> dealerPlannedMonthlySaleList = salesPlanningService.fetchDealerPlannedMonthlySalesDetails(subAreaMaster.getPk().toString(), sclUserModel);
                if (dealerPlannedMonthlySaleList != null && !dealerPlannedMonthlySaleList.isEmpty()) {
                    for (DealerPlannedMonthlySalesModel dealerPlannedMonthlySale : dealerPlannedMonthlySaleList) {
                        MonthlySalesTargetSettingData monthlySalesTargetSettingData = new MonthlySalesTargetSettingData();
                        monthlySalesTargetSettingData.setCustomerCode(dealerPlannedMonthlySale.getCustomerCode());
                        monthlySalesTargetSettingData.setCustomerName(dealerPlannedMonthlySale.getCustomerName());
                        monthlySalesTargetSettingData.setCustomerPotential(dealerPlannedMonthlySale.getCustomerPotential());
                        monthlySalesTargetSettingData.setMonthYear(dealerPlannedMonthlySale.getMonthYear());
                        monthlySalesTargetSettingData.setMonthName(dealerPlannedMonthlySale.getMonthName());
                        List<MonthlySKUData> monthlySKUDataList = new ArrayList<>();
                        for (ProductModel sku : dealerPlannedMonthlySale.getListOfSkus()) {
                            ProductSaleModel productSaleModel = salesPlanningService.fetchProductSaleForDealerPlannedMonthlySales(subAreaMaster.getPk().toString(), sclUserModel, sku.getCode(), dealerPlannedMonthlySale.getCustomerCode(), dealerPlannedMonthlySale.getMonthName(), dealerPlannedMonthlySale.getMonthYear());
                            //  if (listOfSkus.getProductSale() != null && !listOfSkus.getProductSale().isEmpty())
                            //   for (ProductSaleModel productSaleModel : listOfSkus.getProductSale()) {
                            MonthlySKUData monthlySKUData = new MonthlySKUData();
                            monthlySKUData.setProductCode(productSaleModel.getProductCode());
                            monthlySKUData.setProductName(productSaleModel.getProductName());
                            monthlySKUData.setBucket1(productSaleModel.getBucket1());
                            monthlySKUData.setBucket2(productSaleModel.getBucket2());
                            monthlySKUData.setBucket3(productSaleModel.getBucket3());
                            monthlySKUData.setPlannedTarget(productSaleModel.getPlannedTarget());
                            monthlySKUData.setRevisedTarget(productSaleModel.getRevisedTarget());
                            monthlySKUDataList.add(monthlySKUData);
                            //     }
                        }
                        double dealerBucket1 = 0.0, dealerBucket2 = 0.0, dealerBucket3 = 0.0, dealerPlannedTarget = 0.0, dealerRevisedTarget = 0.0;
                        for (MonthlySKUData monthlySKUData : monthlySKUDataList) {
                            dealerBucket1 += monthlySKUData.getBucket1();
                            dealerBucket2 += monthlySKUData.getBucket2();
                            dealerBucket3 += monthlySKUData.getBucket3();
                            dealerPlannedTarget += monthlySKUData.getPlannedTarget();
                            dealerRevisedTarget += monthlySKUData.getRevisedTarget();
                        }
                        monthlySalesTargetSettingData.setPlannedTarget(dealerPlannedTarget);
                        monthlySalesTargetSettingData.setRevisedTarget(dealerRevisedTarget);
                        monthlySalesTargetSettingData.setBucket1(dealerBucket1);
                        monthlySalesTargetSettingData.setBucket2(dealerBucket2);
                        monthlySalesTargetSettingData.setBucket3(dealerBucket3);
                        monthlySalesTargetSettingData.setMonthlySkuDataList(monthlySKUDataList);
                        monthlySalesTargetSettingDataList.add(monthlySalesTargetSettingData);
                    }
                }
            } else {
                //dealer revised monthly sales should come
                List<DealerRevisedMonthlySalesModel> dealerReviewedMonthlySaleList = salesPlanningService.fetchDealerReviewedMonthlySalesDetails(subAreaMaster.getPk().toString(), soForUser.get(0));
                if (dealerReviewedMonthlySaleList != null && !dealerReviewedMonthlySaleList.isEmpty()) {
                    for (DealerRevisedMonthlySalesModel dealerReviewedMonthlySale : dealerReviewedMonthlySaleList) {
                        MonthlySalesTargetSettingData monthlySalesTargetSettingData = new MonthlySalesTargetSettingData();
                        monthlySalesTargetSettingData.setCustomerCode(dealerReviewedMonthlySale.getCustomerCode());
                        monthlySalesTargetSettingData.setCustomerName(dealerReviewedMonthlySale.getCustomerName());
                        monthlySalesTargetSettingData.setCustomerPotential(dealerReviewedMonthlySale.getCustomerPotential());
                        monthlySalesTargetSettingData.setMonthYear(dealerReviewedMonthlySale.getMonthYear());
                        monthlySalesTargetSettingData.setMonthName(dealerReviewedMonthlySale.getMonthName());
                        List<MonthlySKUData> monthlySKUDataList = new ArrayList<>();
                        for (ProductModel sku : dealerReviewedMonthlySale.getListOfSkus()) {
                            ProductSaleModel productSaleModel = salesPlanningService.fetchProductSaleForDealerRevisedMonthlySales(subAreaMaster.getPk().toString(), soForUser.get(0), sku.getCode(), dealerReviewedMonthlySale.getCustomerCode(), dealerReviewedMonthlySale.getMonthName(), dealerReviewedMonthlySale.getMonthYear());

                            // if (listOfSkus.getProductSale() != null && !listOfSkus.getProductSale().isEmpty())
                            //       for (ProductSaleModel productSaleModel : listOfSkus.getProductSale()) {
                            MonthlySKUData monthlySKUData = new MonthlySKUData();
                            monthlySKUData.setProductCode(productSaleModel.getProductCode());
                            monthlySKUData.setProductName(productSaleModel.getProductName());
                            monthlySKUData.setBucket1(productSaleModel.getBucket1());
                            monthlySKUData.setBucket2(productSaleModel.getBucket2());
                            monthlySKUData.setBucket3(productSaleModel.getBucket3());
                            monthlySKUData.setPlannedTarget(productSaleModel.getPlannedTarget());
                            monthlySKUData.setRevisedTarget(productSaleModel.getRevisedTarget());
                            monthlySKUDataList.add(monthlySKUData);
                            //     }
                        }
                        double dealerBucket1 = 0.0, dealerBucket2 = 0.0, dealerBucket3 = 0.0, dealerPlannedTarget = 0.0, dealerRevisedTarget = 0.0;
                        for (MonthlySKUData monthlySKUData : monthlySKUDataList) {
                            dealerBucket1 += monthlySKUData.getBucket1();
                            dealerBucket2 += monthlySKUData.getBucket2();
                            dealerBucket3 += monthlySKUData.getBucket3();
                            dealerPlannedTarget += monthlySKUData.getPlannedTarget();
                            dealerRevisedTarget += monthlySKUData.getRevisedTarget();
                        }
                        monthlySalesTargetSettingData.setPlannedTarget(dealerPlannedTarget);
                        monthlySalesTargetSettingData.setRevisedTarget(dealerRevisedTarget);
                        monthlySalesTargetSettingData.setBucket1(dealerBucket1);
                        monthlySalesTargetSettingData.setBucket2(dealerBucket2);
                        monthlySalesTargetSettingData.setBucket3(dealerBucket3);
                        monthlySalesTargetSettingData.setMonthlySkuDataList(monthlySKUDataList);
                        monthlySalesTargetSettingData.setSoUid(sclUserModel.getUid());
                        monthlySalesTargetSettingDataList.add(monthlySalesTargetSettingData);
                    }
                }
            }
          }
        }
        Collection<DistrictMasterModel> districtMaster = territoryManagementService.getCurrentDistrict();
        if(districtMaster!=null && !districtMaster.isEmpty()) {
            for (DistrictMasterModel districtMasterModel : districtMaster) {
                monthlySalesTargetSettingListData.setDistrictCode(districtMasterModel.getCode());
                monthlySalesTargetSettingListData.setDistrictName(districtMasterModel.getName());
            }
        }
        else{
            monthlySalesTargetSettingListData.setDistrictCode("");
            monthlySalesTargetSettingListData.setDistrictName("");
        }
        monthlySalesTargetSettingListData.setDiUid(currentUser.getUid());
      //  monthlySalesTargetSettingListData.setSoUid(soForUser.get(0).getUid());
        monthlySalesTargetSettingListData.setSubArea(subAreaMaster.getTaluka());
        monthlySalesTargetSettingListData.setSubAreaId(subAreaMaster.getPk().toString());
        monthlySalesTargetSettingListData.setMonthlySalesTargetSettingData(monthlySalesTargetSettingDataList);
        return monthlySalesTargetSettingListData;
    }

    @Override
    public MonthlySalesTargetSettingListData viewMonthlyRevisedSalesTargetForReviewTabForRH(String subArea)
        {
            List<MonthlySalesTargetSettingData> monthlySalesTargetSettingDataList = new ArrayList<>();
            SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
            SubAreaMasterModel subAreaMaster=territoryManagementService.getTerritoryById(subArea);
            BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
           FilterTalukaData filterTalukaData=new FilterTalukaData();
           List<SclUserModel> soForUser = territoryManagementService.getSOForUser(filterTalukaData);
           // MonthlySalesModel monthlySalesModel =salesPlanningService.getMonthlySalesModelDetail(null,subAreaMaster.getPk().toString(),currentBaseSite);

            MonthlySalesTargetSettingListData monthlySalesTargetSettingListData = new MonthlySalesTargetSettingListData();

            if(soForUser!=null && !soForUser.isEmpty()) {
                for (SclUserModel sclUserModel : soForUser) {
                    MonthlySalesModel monthlySalesModel =salesPlanningService.getMonthlySalesModelDetail(sclUserModel,subAreaMaster.getPk().toString(),currentBaseSite);
                    if(monthlySalesModel!=null) {
                        if(monthlySalesModel.getRevisedTarget()==null && monthlySalesModel.getActionPerformedBy()==null)
                        {
                            monthlySalesTargetSettingListData.setIsTargetRevised(false);
                            monthlySalesTargetSettingListData.setIsTargetApproved(false);
                            LOG.info("first time by RH");
                        }
                        else if(monthlySalesModel.getRevisedTarget()!=null && !(monthlySalesModel.getDealerPlannedTotalRevisedTarget().equals(monthlySalesModel.getRevisedTarget())) && monthlySalesModel.getActionPerformedBy()==null
                                && monthlySalesModel.getRevisedBy() != null && monthlySalesModel.getRevisedBy().equals(currentUser) &&
                                monthlySalesModel.getActionPerformedBy()==null)
                        {
                            monthlySalesTargetSettingListData.setIsTargetRevised(true);
                            monthlySalesTargetSettingListData.setIsTargetApproved(true);
                            LOG.info("RH has revised but DO has not made any change");
                            LOG.info("Revised by RH and send to do - approve and target revision both disabled");
                        }
                        else if(monthlySalesModel.getRevisedTarget()!=null && monthlySalesModel.getDealerPlannedTotalRevisedTarget().equals(monthlySalesModel.getRevisedTarget())
                                && monthlySalesModel.getRevisedBy() != null && monthlySalesModel.getRevisedBy().equals(currentUser) &&
                                monthlySalesModel.getActionPerformedBy()==null)
                        {
                            monthlySalesTargetSettingListData.setIsTargetRevised(false);
                            monthlySalesTargetSettingListData.setIsTargetApproved(false);
                            LOG.info("DO has sent REvised target to RH");
                        }
                        else if(monthlySalesModel.getRevisedBy()!=null && monthlySalesModel.getRevisedBy().equals(currentUser) && monthlySalesModel.getRevisedTarget()!=null
                        && monthlySalesModel.getActionPerformedBy()!=null && monthlySalesModel.getActionPerformedBy().equals(currentUser)){
                            monthlySalesTargetSettingListData.setIsTargetRevised(true);
                            monthlySalesTargetSettingListData.setIsTargetApproved(true);
                            LOG.info("RH approved the Revised target sent by DO");
                        }
                        else if(monthlySalesModel.getActionPerformedBy()!=null && monthlySalesModel.getActionPerformedBy().equals(currentUser)
                                && monthlySalesModel.getRevisedBy()==null){
                            monthlySalesTargetSettingListData.setIsTargetRevised(true);
                            monthlySalesTargetSettingListData.setIsTargetApproved(true);
                            LOG.info("Direclty approved by RH");
                        }
                        //top down
                        //Revised by RH and send to do - approve and target revision both disabled 4343 line
                        //revised by SH - approved - disabled and target revision enabled
                        if(monthlySalesModel.getRevisedByShEmpCode()!=null && monthlySalesModel.getRevisedTargetBySH()!=null
                                && monthlySalesModel.getIsTopDownIndicatorOn()!=null && monthlySalesModel.getIsTopDownIndicatorOn()){
                            monthlySalesTargetSettingListData.setIsTargetApprovedForRHTopDown(true);
                            monthlySalesTargetSettingListData.setIsTargetRevisedForRHTopDown(false);
                            monthlySalesTargetSettingListData.setIsTopDownIndicatorOn(true);
                            LOG.info("topdown - revised by SH - approved - disabled and target revision enabled");
                        }

                        if(monthlySalesModel.getRevisedByShEmpCode()!=null && monthlySalesModel.getRevisedTargetBySH()!=null
                                && monthlySalesModel.getIsTopDownIndicatorOn()!=null && monthlySalesModel.getIsTopDownIndicatorOn() && monthlySalesModel.getRevisedTargetSendToDO()!=null){
                            monthlySalesTargetSettingListData.setIsTargetApprovedForRHTopDown(true);
                            monthlySalesTargetSettingListData.setIsTargetRevisedForRHTopDown(true);
                            monthlySalesTargetSettingListData.setIsTopDownIndicatorOn(true);
                            LOG.info("topdown - revised by RH - approved - disabled and target revision disabled");
                        }

                    }
                    if (monthlySalesModel != null) {
                        List<DealerPlannedMonthlySalesModel> dealerPlannedMonthlySaleList = salesPlanningService.fetchDealerPlannedMonthlySalesDetails(subAreaMaster.getPk().toString(), sclUserModel);
                        if (dealerPlannedMonthlySaleList != null && !dealerPlannedMonthlySaleList.isEmpty()) {
                            for (DealerPlannedMonthlySalesModel dealerPlannedMonthlySale : dealerPlannedMonthlySaleList) {
                                MonthlySalesTargetSettingData monthlySalesTargetSettingData = new MonthlySalesTargetSettingData();
                                monthlySalesTargetSettingData.setCustomerCode(dealerPlannedMonthlySale.getCustomerCode());
                                monthlySalesTargetSettingData.setCustomerName(dealerPlannedMonthlySale.getCustomerName());
                                monthlySalesTargetSettingData.setCustomerPotential(dealerPlannedMonthlySale.getCustomerPotential());
                                monthlySalesTargetSettingData.setMonthYear(dealerPlannedMonthlySale.getMonthYear());
                                monthlySalesTargetSettingData.setMonthName(dealerPlannedMonthlySale.getMonthName());
                                List<MonthlySKUData> monthlySKUDataList = new ArrayList<>();
                                for (ProductModel sku : dealerPlannedMonthlySale.getListOfSkus()) {
                                    ProductSaleModel productSaleModel = salesPlanningService.fetchProductSaleForDealerPlannedMonthlySales(subAreaMaster.getPk().toString(), sclUserModel, sku.getCode(), dealerPlannedMonthlySale.getCustomerCode(), dealerPlannedMonthlySale.getMonthName(), dealerPlannedMonthlySale.getMonthYear());
                                    //  if (listOfSkus.getProductSale() != null && !listOfSkus.getProductSale().isEmpty())
                                    //   for (ProductSaleModel productSaleModel : listOfSkus.getProductSale()) {
                                    MonthlySKUData monthlySKUData = new MonthlySKUData();
                                    monthlySKUData.setProductCode(productSaleModel.getProductCode());
                                    monthlySKUData.setProductName(productSaleModel.getProductName());
                                    monthlySKUData.setBucket1(productSaleModel.getBucket1());
                                    monthlySKUData.setBucket2(productSaleModel.getBucket2());
                                    monthlySKUData.setBucket3(productSaleModel.getBucket3());
                                    monthlySKUData.setPlannedTarget(productSaleModel.getPlannedTarget());
                                    monthlySKUData.setRevisedTarget(productSaleModel.getRevisedTarget());
                                    monthlySKUDataList.add(monthlySKUData);
                                    //     }
                                }
                                double dealerBucket1 = 0.0, dealerBucket2 = 0.0, dealerBucket3 = 0.0, dealerPlannedTarget = 0.0, dealerRevisedTarget = 0.0;
                                for (MonthlySKUData monthlySKUData : monthlySKUDataList) {
                                    dealerBucket1 += monthlySKUData.getBucket1();
                                    dealerBucket2 += monthlySKUData.getBucket2();
                                    dealerBucket3 += monthlySKUData.getBucket3();
                                    dealerPlannedTarget += monthlySKUData.getPlannedTarget();
                                    dealerRevisedTarget += monthlySKUData.getRevisedTarget();
                                }
                                monthlySalesTargetSettingData.setPlannedTarget(dealerPlannedTarget);
                                monthlySalesTargetSettingData.setRevisedTarget(dealerRevisedTarget);
                                monthlySalesTargetSettingData.setBucket1(dealerBucket1);
                                monthlySalesTargetSettingData.setBucket2(dealerBucket2);
                                monthlySalesTargetSettingData.setBucket3(dealerBucket3);
                                monthlySalesTargetSettingData.setMonthlySkuDataList(monthlySKUDataList);
                                monthlySalesTargetSettingDataList.add(monthlySalesTargetSettingData);
                                monthlySalesTargetSettingData.setSoUid(monthlySalesModel.getSo().getUid());
                            }
                        }
                    } /*else {
                        //dealer revised monthly sales should come
                        List<DealerRevisedMonthlySalesModel> dealerReviewedMonthlySaleList = salesPlanningService.fetchDealerReviewedMonthlySalesDetails(subAreaMaster.getPk().toString(), sclUserModel);
                        if (dealerReviewedMonthlySaleList != null && !dealerReviewedMonthlySaleList.isEmpty()) {
                            for (DealerRevisedMonthlySalesModel dealerReviewedMonthlySale : dealerReviewedMonthlySaleList) {
                                MonthlySalesTargetSettingData monthlySalesTargetSettingData = new MonthlySalesTargetSettingData();
                                monthlySalesTargetSettingData.setCustomerCode(dealerReviewedMonthlySale.getCustomerCode());
                                monthlySalesTargetSettingData.setCustomerName(dealerReviewedMonthlySale.getCustomerName());
                                monthlySalesTargetSettingData.setCustomerPotential(dealerReviewedMonthlySale.getCustomerPotential());
                                monthlySalesTargetSettingData.setMonthYear(dealerReviewedMonthlySale.getMonthYear());
                                monthlySalesTargetSettingData.setMonthName(dealerReviewedMonthlySale.getMonthName());
                                List<MonthlySKUData> monthlySKUDataList = new ArrayList<>();
                                for (ProductModel sku : dealerReviewedMonthlySale.getListOfSkus()) {
                                    ProductSaleModel productSaleModel = salesPlanningService.fetchProductSaleForDealerRevisedMonthlySales(subAreaMaster.getPk().toString(), sclUserModel, sku.getCode(), dealerReviewedMonthlySale.getCustomerCode(), dealerReviewedMonthlySale.getMonthName(), dealerReviewedMonthlySale.getMonthYear());

                                    // if (listOfSkus.getProductSale() != null && !listOfSkus.getProductSale().isEmpty())
                                    //       for (ProductSaleModel productSaleModel : listOfSkus.getProductSale()) {
                                    MonthlySKUData monthlySKUData = new MonthlySKUData();
                                    monthlySKUData.setProductCode(productSaleModel.getProductCode());
                                    monthlySKUData.setProductName(productSaleModel.getProductName());
                                    monthlySKUData.setBucket1(productSaleModel.getBucket1());
                                    monthlySKUData.setBucket2(productSaleModel.getBucket2());
                                    monthlySKUData.setBucket3(productSaleModel.getBucket3());
                                    monthlySKUData.setPlannedTarget(productSaleModel.getPlannedTarget());
                                    monthlySKUData.setRevisedTarget(productSaleModel.getRevisedTarget());
                                    monthlySKUDataList.add(monthlySKUData);
                                    //     }
                                }
                                double dealerBucket1 = 0.0, dealerBucket2 = 0.0, dealerBucket3 = 0.0, dealerPlannedTarget = 0.0, dealerRevisedTarget = 0.0;
                                for (MonthlySKUData monthlySKUData : monthlySKUDataList) {
                                    dealerBucket1 += monthlySKUData.getBucket1();
                                    dealerBucket2 += monthlySKUData.getBucket2();
                                    dealerBucket3 += monthlySKUData.getBucket3();
                                    dealerPlannedTarget += monthlySKUData.getPlannedTarget();
                                    dealerRevisedTarget += monthlySKUData.getRevisedTarget();
                                }
                                monthlySalesTargetSettingData.setPlannedTarget(dealerPlannedTarget);
                                monthlySalesTargetSettingData.setRevisedTarget(dealerRevisedTarget);
                                monthlySalesTargetSettingData.setBucket1(dealerBucket1);
                                monthlySalesTargetSettingData.setBucket2(dealerBucket2);
                                monthlySalesTargetSettingData.setBucket3(dealerBucket3);
                                monthlySalesTargetSettingData.setMonthlySkuDataList(monthlySKUDataList);
                                monthlySalesTargetSettingData.setSoUid(sclUserModel.getUid());
                                monthlySalesTargetSettingDataList.add(monthlySalesTargetSettingData);
                            }
                        }
                    } */

                }
            }
            Collection<RegionMasterModel> region = territoryManagementService.getCurrentRegion();
            if(region!=null && !region.isEmpty()) {
                for (RegionMasterModel regionMasterModel : region) {
                    monthlySalesTargetSettingListData.setDistrictCode(regionMasterModel.getCode());
                    monthlySalesTargetSettingListData.setDistrictName(regionMasterModel.getName());
                }
            }
            else{
                monthlySalesTargetSettingListData.setDistrictCode("");
                monthlySalesTargetSettingListData.setDistrictName("");
            }
            monthlySalesTargetSettingListData.setDiUid(currentUser.getUid());
            //monthlySalesTargetSettingListData.setSoUid(month);
            monthlySalesTargetSettingListData.setSubArea(subAreaMaster.getTaluka());
            monthlySalesTargetSettingListData.setSubAreaId(subAreaMaster.getPk().toString());
            monthlySalesTargetSettingListData.setMonthlySalesTargetSettingData(monthlySalesTargetSettingDataList);
            return monthlySalesTargetSettingListData;
        }


    @Override
    public boolean saveAnnualSalesTargetSettingForDealers(AnnualSalesTargetSettingListData annualSalesTargetSettingListData, String subArea) {
        SclUserModel sclUser = (SclUserModel) getUserService().getCurrentUser();
        return salesPlanningService.saveAnnualSalesTargetSettingForDealers(annualSalesTargetSettingListData, sclUser, subArea);
    }

    @Override
    public AnnualSalesTargetSettingListData viewSavedAnnualSalesTargetSettingForDealers(String subArea) {
        SclUserModel sclUser = (SclUserModel) getUserService().getCurrentUser();
        BaseSiteModel baseSite = baseSiteService.getCurrentBaseSite();
        AnnualSalesTargetSettingListData list = new AnnualSalesTargetSettingListData();

        List<AnnualSalesTargetSettingData> listOfAnnualTargetSetting = new ArrayList<>();
        List<List<Object>> listOfDealerDetails = salesPlanningService.viewDealerDetailsForAnnualSales(subArea, sclUser, baseSite);
            if (listOfDealerDetails != null && !listOfDealerDetails.isEmpty()) {
                for (List<Object> details : listOfDealerDetails) {
                    AnnualSalesTargetSettingData annualSalesTargetSettingData = new AnnualSalesTargetSettingData();
                    SclCustomerModel sclCustomer = (SclCustomerModel) userService.getUserForUID((String) details.get(0));
                    annualSalesTargetSettingData.setCustomerCode(sclCustomer.getUid());
                    annualSalesTargetSettingData.setCustomerName(sclCustomer.getName());
                    annualSalesTargetSettingData.setCustomerPotential(sclCustomer.getCounterPotential() != null ? sclCustomer.getCounterPotential() : 0.0);
                    List<List<Object>> dealerCySalesDetails = salesPlanningService.fetchDealerCySalesForAnnualSales(subArea, sclUser, baseSite, annualSalesTargetSettingData.getCustomerCode());
                    List<SKUData> listOfSku = new ArrayList<>();
                    if (dealerCySalesDetails != null && !dealerCySalesDetails.isEmpty()) {
                        for (List<Object> dealerSales : dealerCySalesDetails) {
                            if (dealerSales.get(3).equals(annualSalesTargetSettingData.getCustomerCode())) {
                                SKUData skuData = new SKUData();
                                skuData.setCySales((Double) dealerSales.get(0));
                                skuData.setProductCode((String) dealerSales.get(1));
                                skuData.setProductName((String) dealerSales.get(2));
                                listOfSku.add(skuData);
                            }
                        }
                    }
                        annualSalesTargetSettingData.setSkuDataList(listOfSku);
                    double sumOfProductCySales = 0.0;
                    for (SKUData skuData : annualSalesTargetSettingData.getSkuDataList()) {
                        sumOfProductCySales += skuData.getCySales();
                    }
                    //sum of sku's sale = dealer cy sales
                    annualSalesTargetSettingData.setCurrentYearSales(sumOfProductCySales);
                    listOfAnnualTargetSetting.add(annualSalesTargetSettingData);
                }

        }
        list.setSubArea(subArea);
        list.setAnnualSalesTargetSetting(listOfAnnualTargetSetting);
        return list;
    }

    @Override
    public boolean saveAnnualSalesTargetSettingForRetailers(AnnualSalesTargetSettingData annualSalesTargetSettingData, String subArea) {
        return salesPlanningService.saveAnnualSalesTargetSettingForRetailers(annualSalesTargetSettingData,subArea);
    }

    @Override
    public boolean saveMonthWiseDealersDetailsForAnnSales(AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData, String subArea) {
        return salesPlanningService.saveMonthWiseDealersDetailsForAnnSales(annualSalesMonthWiseTargetData,subArea);
    }

    @Override
    public boolean saveMonthWiseRetailerForAnnSales(AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData, String subArea) {
        return salesPlanningService.saveMonthWiseRetailerForAnnSales(annualSalesMonthWiseTargetData,subArea);
    }

    @Override
    public boolean saveOnboardedDealersForAnnSales(AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData,String subArea) {
        return salesPlanningService.saveOnboardedDealersForAnnSales(annualSalesMonthWiseTargetData,subArea);
    }

    @Override
    public boolean saveOnboardedRetailerForAnnSales(AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData) {
        return salesPlanningService.saveOnboardedRetailerForAnnSales(annualSalesMonthWiseTargetListData);
    }

    @Override
    public boolean saveMonthlySalesTargetForDealers(MonthlySalesTargetSettingListData monthlySalesTargetSettingListData) {
        return salesPlanningService.saveMonthlySalesTargetForDealers(monthlySalesTargetSettingListData);
    }

    @Override
    public boolean saveReviseMonthlySalesTargetForDealer(MonthlySalesTargetSettingListData monthlySalesTargetSettingListData) {
        return salesPlanningService.saveReviseMonthlySalesTargetForDealer(monthlySalesTargetSettingListData);
    }

    @Override
    public MonthlySalesTargetSettingListData viewSavedMonthSalesTargetSetForDealers(String subArea) {
        MonthlySalesTargetSettingListData list=new MonthlySalesTargetSettingListData();
        return list;
    }

    @Override
    public MonthlySalesTargetSettingListData viewSavedRevMonthSalesTargetSetForDealers(String subArea) {
        MonthlySalesTargetSettingListData list=new MonthlySalesTargetSettingListData();
        return list;
    }
    @Override
    public List<Double> getTotalPlanAndCySalesRetailerPlanned(SearchPageData searchPageData,String subArea){
        SclUserModel sclUser = (SclUserModel) getUserService().getCurrentUser();
        SubAreaMasterModel subAreaMaster=territoryManagementService.getTerritoryById(subArea);
        BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
        SearchPageData<AnnualSalesModel> annualSalesModelResult=salesPlanningService.viewPlannedSalesforDealersRetailersMonthWise(searchPageData,subAreaMaster.getPk().toString(), sclUser, brand);
        double totalPlanSales= 0.0;
        double totalCySales=0.0;
       List<Double> list=new ArrayList<>();
       if(annualSalesModelResult.getResults()!=null) {
           for (AnnualSalesModel annualSalesModel : annualSalesModelResult.getResults()) {
               if (annualSalesModel != null) {
                   totalPlanSales = annualSalesModel.getRetailerPlannedTotalPlanSales() != null ? annualSalesModel.getRetailerPlannedTotalPlanSales() : 0.0;
                   totalCySales = annualSalesModel.getRetailerPlannedTotalCySales() != null ? annualSalesModel.getRetailerPlannedTotalCySales() : 0.0;
                   list.add(totalPlanSales);
                   list.add(totalCySales);
               }
           }
       }
        return  list;
    }
    @Override
    public List<Double> getTotalPlanAndCySalesRetailerRevised(SearchPageData searchPageData,String subArea){
        SclUserModel sclUser = (SclUserModel) getUserService().getCurrentUser();
        BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
        SubAreaMasterModel subAreaMaster=territoryManagementService.getTerritoryById(subArea);
        SearchPageData<AnnualSalesModel> annualSalesModelResult=salesPlanningService.viewPlannedSalesforDealersRetailersMonthWise(searchPageData,subAreaMaster.getPk().toString(), sclUser, brand);
        double totalPlanSales= 0.0;
        double totalCySales=0.0;
        List<Double> list=new ArrayList<>();
        if(annualSalesModelResult.getResults()!=null) {
            for (AnnualSalesModel annualSalesModel : annualSalesModelResult.getResults()) {
                if (annualSalesModel != null) {
                    totalPlanSales = annualSalesModel.getRetailerRevisedTotalPlanSales() != null ? annualSalesModel.getRetailerRevisedTotalPlanSales() : 0.0;
                    totalCySales = annualSalesModel.getRetailerRevisedTotalCySales() != null ? annualSalesModel.getRetailerRevisedTotalCySales() : 0.0;
                    list.add(totalPlanSales);
                    list.add(totalCySales);
                }
            }
        }
        return  list;
    }
    @Override
    public SearchPageData<AnnualSalesMonthWiseTargetData> viewReviewedSalesforRetailesMonthwise(SearchPageData searchPageData, String subArea, String filter) {
        SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
        BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
        List<AnnualSalesMonthWiseTargetData> annualSalesMonthWiseTargetDataList = new ArrayList<>();
        SubAreaMasterModel subAreaMaster = territoryManagementService.getTerritoryById(subArea);
        final SearchPageData<AnnualSalesMonthWiseTargetData> result = new SearchPageData<>();

        //search
        SearchPageData<AnnualSalesModel> annualSalesModelResult = salesPlanningService.viewPlannedSalesforDealersRetailersMonthWise(searchPageData, subAreaMaster.getPk().toString(), currentUser, brand);
        for (AnnualSalesModel annualSalesMonthwiseModel : annualSalesModelResult.getResults()) {

            if (StringUtils.isBlank(filter)) {
                if (annualSalesMonthwiseModel.getIsAnnualSalesReviewedForRetailer() != null && annualSalesMonthwiseModel.getIsAnnualSalesReviewedForRetailer().equals(false)) {
                    if (annualSalesMonthwiseModel.getRetailerRevisedAnnualSales() != null && Objects.nonNull(annualSalesMonthwiseModel.getRetailerRevisedAnnualSales()) &&
                            !annualSalesMonthwiseModel.getRetailerRevisedAnnualSales().isEmpty()) {

                        for (RetailerRevisedAnnualSalesModel retailerRevisedAnnualSalesModel : annualSalesMonthwiseModel.getRetailerRevisedAnnualSales()) {
                            for (SubAreaMasterModel subAreaMasterModel : annualSalesMonthwiseModel.getSubAreaMasterList()) {
                                AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData = new AnnualSalesMonthWiseTargetData();
                                if (subAreaMasterModel.equals(retailerRevisedAnnualSalesModel.getSubAreaMaster())) {
                                    // annualSalesMonthWiseTargetListData.setSubArea(subAreaMasterModel.getTaluka());
                                    //annualSalesMonthWiseTargetListData.setSubAreaId(subAreaMasterModel.getPk().toString());
                                    annualSalesMonthWiseTargetData.setCustomerCode(retailerRevisedAnnualSalesModel.getCustomerCode());
                                    annualSalesMonthWiseTargetData.setCustomerName(retailerRevisedAnnualSalesModel.getCustomerName());
                                    annualSalesMonthWiseTargetData.setCustomerPotential(retailerRevisedAnnualSalesModel.getCustomerPotential() != null ? retailerRevisedAnnualSalesModel.getCustomerPotential() : 0.0);
                                    annualSalesMonthWiseTargetData.setTotalTarget(retailerRevisedAnnualSalesModel.getTotalTarget());
                                    annualSalesMonthWiseTargetData.setErpCustomerNo(retailerRevisedAnnualSalesModel.getErpCustomerNo());

                                    List<MonthWiseTargetData> monthWiseTargetDataList = new ArrayList<>();
                                    if (retailerRevisedAnnualSalesModel.getMonthWiseAnnualTarget() != null &&
                                            !retailerRevisedAnnualSalesModel.getMonthWiseAnnualTarget().isEmpty()) {
                                        for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : retailerRevisedAnnualSalesModel.getMonthWiseAnnualTarget()) {
                                            MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                            if (monthWiseAnnualTargetModel.getCustomerCode().equals(retailerRevisedAnnualSalesModel.getCustomerCode()) &&
                                                    monthWiseAnnualTargetModel.getIsAnnualSalesRevisedForRetailer().equals(true)) {
                                                monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                                monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                                monthWiseTargetDataList.add(monthWiseTargetData);
                                            }
                                        }
                                    }
                                    annualSalesMonthWiseTargetData.setMonthWiseTarget(monthWiseTargetDataList);

                                    List<RetailerDetailsData> retailerRevisedAnnualSalesDetailsList = new ArrayList<>();
                                    if (retailerRevisedAnnualSalesModel.getRetailerRevisedAnnualSalesDetails() != null && !retailerRevisedAnnualSalesModel.getRetailerRevisedAnnualSalesDetails().isEmpty()) {
                                        for (RetailerRevisedAnnualSalesDetailsModel retailerRevisedAnnualSalesDetail : retailerRevisedAnnualSalesModel.getRetailerRevisedAnnualSalesDetails()) {
                                            RetailerDetailsData retailerDetailsData = new RetailerDetailsData();
                                            retailerDetailsData.setCustomerCode(retailerRevisedAnnualSalesDetail.getCustomerCode());
                                            retailerDetailsData.setCustomerName(retailerRevisedAnnualSalesDetail.getCustomerName());
                                            retailerDetailsData.setCustomerPotential(retailerRevisedAnnualSalesDetail.getCustomerPotential() != null ? retailerRevisedAnnualSalesDetail.getCustomerPotential() : 0.0);
                                            retailerDetailsData.setTotalTarget(retailerRevisedAnnualSalesDetail.getTotalTarget());
                                            retailerDetailsData.setErpCustomerNo(retailerRevisedAnnualSalesDetail.getErpCustomerNo());

                                            List<MonthWiseTargetData> monthWiseTargetDataListforRetailer = new ArrayList<>();
                                            List<MonthWiseAnnualTargetModel> monthWiseRetailerDetailsBeforeReview = salesPlanningService.getMonthWiseRetailerDetailsBeforeReview(retailerRevisedAnnualSalesModel.getCustomerCode(), retailerRevisedAnnualSalesDetail.getCustomerCode(), retailerRevisedAnnualSalesModel.getSubAreaMaster().getPk().toString(), currentUser);
                                            if (retailerRevisedAnnualSalesDetail.getMonthWiseAnnualTarget() != null && !retailerRevisedAnnualSalesDetail.getMonthWiseAnnualTarget().isEmpty())
                                                //doubt
                                                for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : monthWiseRetailerDetailsBeforeReview) {
                                                    if (monthWiseAnnualTargetModel.getCustomerCode().equalsIgnoreCase(retailerRevisedAnnualSalesModel.getCustomerCode()) && monthWiseAnnualTargetModel.getRetailerCode().equalsIgnoreCase(retailerRevisedAnnualSalesDetail.getCustomerCode())
                                                            && monthWiseAnnualTargetModel.getIsAnnualSalesRevisedForRetailer().equals(true)) {
                                                        MonthWiseTargetData monthWiseTargetDataforRet = new MonthWiseTargetData();
                                                        monthWiseTargetDataforRet.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                                        monthWiseTargetDataforRet.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                                        monthWiseTargetDataListforRetailer.add(monthWiseTargetDataforRet);
                                                    }
                                                }
                                            retailerDetailsData.setMonthWiseSkuTarget(monthWiseTargetDataListforRetailer);
                                            retailerRevisedAnnualSalesDetailsList.add(retailerDetailsData);
                                        }
                                    }
                                    annualSalesMonthWiseTargetData.setRetailerData(retailerRevisedAnnualSalesDetailsList);
                                    annualSalesMonthWiseTargetDataList.add(annualSalesMonthWiseTargetData);

                                    if (retailerRevisedAnnualSalesModel.getDealerSelfCounterSale() != null) {
                                        SelfCounterSaleDetailsModel dealerSelfCounterSale = retailerRevisedAnnualSalesModel.getDealerSelfCounterSale();
                                        //doubt
                                        if (dealerSelfCounterSale.getIsAnnualSalesRevisedForRetailer().equals(true)) {
                                            SelfCounterSaleData selfCounterSaleData = new SelfCounterSaleData();
                                            selfCounterSaleData.setCustomerCode(dealerSelfCounterSale.getCustomerCode());
                                            selfCounterSaleData.setCustomerName(dealerSelfCounterSale.getCustomerName());
                                            selfCounterSaleData.setCustomerPotential(dealerSelfCounterSale.getCustomerPotential() != null ? dealerSelfCounterSale.getCustomerPotential() : 0.0);
                                            selfCounterSaleData.setTotalTarget(dealerSelfCounterSale.getTotalTarget());
                                            selfCounterSaleData.setErpCustomerNo(dealerSelfCounterSale.getErpCustomerNo());

                                            List<MonthWiseTargetData> monthWiseSelfCounterList = new ArrayList<>();
                                            if (dealerSelfCounterSale.getMonthWiseAnnualTarget() != null && !dealerSelfCounterSale.getMonthWiseAnnualTarget().isEmpty())
                                                for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : dealerSelfCounterSale.getMonthWiseAnnualTarget()) {
                                                    if (dealerSelfCounterSale.getCustomerCode().equalsIgnoreCase(monthWiseAnnualTargetModel.getSelfCounterCustomerCode()) &&
                                                            monthWiseAnnualTargetModel.getIsAnnualSalesRevisedForRetailer().equals(true)) {
                                                        MonthWiseTargetData monthWiseTargetDataforRet = new MonthWiseTargetData();
                                                        monthWiseTargetDataforRet.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                                        monthWiseTargetDataforRet.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                                        monthWiseSelfCounterList.add(monthWiseTargetDataforRet);
                                                    }
                                                }
                                            selfCounterSaleData.setMonthWiseTarget(monthWiseSelfCounterList);
                                            annualSalesMonthWiseTargetData.setSelfCounterSale(selfCounterSaleData);
                                        }
                                    }
                                }
                            } //subarea for

                           }
                    }
                } else {
                    if (annualSalesMonthwiseModel.getRetailerRevisedAnnualSales() != null && Objects.nonNull(annualSalesMonthwiseModel.getRetailerRevisedAnnualSales()) &&
                            !annualSalesMonthwiseModel.getRetailerRevisedAnnualSales().isEmpty()) {
                        //List<AnnualSalesMonthWiseTargetData> annualSalesMonthWiseTargetDataList = new ArrayList<>();
                        List<SpOnboardAnnualTargetSettingData> spOnboardAnnualTargetSettingDataList = new ArrayList<>();
                        for (RetailerRevisedAnnualSalesModel retailerRevisedAnnualSalesModel : annualSalesMonthwiseModel.getRetailerRevisedAnnualSales()) {
                            for (SubAreaMasterModel subAreaMasterModel : annualSalesMonthwiseModel.getSubAreaMasterList()) {
                                if (subAreaMasterModel.equals(retailerRevisedAnnualSalesModel.getSubAreaMaster())) {
                                    if (retailerRevisedAnnualSalesModel.getIsExistingRetailerRevisedForReview() != null && retailerRevisedAnnualSalesModel.getIsExistingRetailerRevisedForReview().equals(true)) {
                                        AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData = new AnnualSalesMonthWiseTargetData();
                                        annualSalesMonthWiseTargetData.setCustomerCode(retailerRevisedAnnualSalesModel.getCustomerCode());
                                        annualSalesMonthWiseTargetData.setCustomerName(retailerRevisedAnnualSalesModel.getCustomerName());
                                        annualSalesMonthWiseTargetData.setCustomerPotential(retailerRevisedAnnualSalesModel.getCustomerPotential() != null ? retailerRevisedAnnualSalesModel.getCustomerPotential() : 0.0);
                                        annualSalesMonthWiseTargetData.setTotalTarget(retailerRevisedAnnualSalesModel.getTotalTarget());
                                        annualSalesMonthWiseTargetData.setErpCustomerNo(retailerRevisedAnnualSalesModel.getErpCustomerNo());

                                        List<MonthWiseTargetData> monthWiseTargetDataListExisting = new ArrayList<>();
                                        if (retailerRevisedAnnualSalesModel.getMonthWiseAnnualTarget() != null &&
                                                !retailerRevisedAnnualSalesModel.getMonthWiseAnnualTarget().isEmpty()) {
                                            for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : retailerRevisedAnnualSalesModel.getMonthWiseAnnualTarget()) {
                                                MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                                if (monthWiseAnnualTargetModel.getCustomerCode().equals(retailerRevisedAnnualSalesModel.getCustomerCode()) && monthWiseAnnualTargetModel.getIsAnnualSalesReviewedForRetailer().equals(true)) {
                                                    monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                                    monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                                    monthWiseTargetDataListExisting.add(monthWiseTargetData);
                                                }
                                            }
                                        }
                                        annualSalesMonthWiseTargetData.setMonthWiseTarget(monthWiseTargetDataListExisting);

                                        List<RetailerDetailsData> retailerRevisedAnnualSalesDetailsList = new ArrayList<>();
                                        if (retailerRevisedAnnualSalesModel.getRetailerRevisedAnnualSalesDetails() != null && !retailerRevisedAnnualSalesModel.getRetailerRevisedAnnualSalesDetails().isEmpty()) {
                                            for (RetailerRevisedAnnualSalesDetailsModel retailerRevisedAnnualSalesDetail : retailerRevisedAnnualSalesModel.getRetailerRevisedAnnualSalesDetails()) {
                                                RetailerDetailsData retailerDetailsData = new RetailerDetailsData();
                                                retailerDetailsData.setCustomerCode(retailerRevisedAnnualSalesDetail.getCustomerCode());
                                                retailerDetailsData.setCustomerName(retailerRevisedAnnualSalesDetail.getCustomerName());
                                                retailerDetailsData.setCustomerPotential(retailerRevisedAnnualSalesDetail.getCustomerPotential() != null ? retailerRevisedAnnualSalesDetail.getCustomerPotential() : 0.0);
                                                retailerDetailsData.setTotalTarget(retailerRevisedAnnualSalesDetail.getTotalTarget());
                                                retailerDetailsData.setErpCustomerNo(retailerRevisedAnnualSalesDetail.getErpCustomerNo());

                                                List<MonthWiseTargetData> monthWiseTargetDataListforRetailer = new ArrayList<>();
                                                List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetList = salesPlanningService.getMonthWiseRetailerDetailsForReview(retailerRevisedAnnualSalesModel.getCustomerCode(), retailerDetailsData.getCustomerCode(), retailerRevisedAnnualSalesModel.getSubAreaMaster().getPk().toString(), currentUser);

                                                for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : monthWiseAnnualTargetList) {
                                                    if (monthWiseAnnualTargetModel.getCustomerCode().equalsIgnoreCase(retailerRevisedAnnualSalesModel.getCustomerCode()) && monthWiseAnnualTargetModel.getRetailerCode().equalsIgnoreCase(retailerRevisedAnnualSalesDetail.getCustomerCode())
                                                            && monthWiseAnnualTargetModel.getIsAnnualSalesReviewedForRetailerDetails().equals(true)) {
                                                        MonthWiseTargetData monthWiseTargetDataforRet = new MonthWiseTargetData();
                                                        monthWiseTargetDataforRet.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                                        monthWiseTargetDataforRet.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                                        monthWiseTargetDataListforRetailer.add(monthWiseTargetDataforRet);
                                                    }
                                                }
                                                retailerDetailsData.setMonthWiseSkuTarget(monthWiseTargetDataListforRetailer);
                                                retailerRevisedAnnualSalesDetailsList.add(retailerDetailsData);
                                            }
                                        }
                                        annualSalesMonthWiseTargetData.setRetailerData(retailerRevisedAnnualSalesDetailsList);


                                        if (retailerRevisedAnnualSalesModel.getDealerSelfCounterSale() != null) {
                                            SelfCounterSaleDetailsModel dealerSelfCounterSale = retailerRevisedAnnualSalesModel.getDealerSelfCounterSale();
                                            SelfCounterSaleData selfCounterSaleData = new SelfCounterSaleData();
                                            selfCounterSaleData.setCustomerCode(dealerSelfCounterSale.getCustomerCode());
                                            selfCounterSaleData.setCustomerName(dealerSelfCounterSale.getCustomerName());
                                            selfCounterSaleData.setCustomerPotential(dealerSelfCounterSale.getCustomerPotential() != null ? dealerSelfCounterSale.getCustomerPotential() : 0.0);
                                            selfCounterSaleData.setTotalTarget(dealerSelfCounterSale.getTotalTarget());
                                            selfCounterSaleData.setErpCustomerNo(dealerSelfCounterSale.getErpCustomerNo());

                                            List<MonthWiseTargetData> monthWiseSelfCounterList = new ArrayList<>();
                                            if (dealerSelfCounterSale.getMonthWiseAnnualTarget() != null && !dealerSelfCounterSale.getMonthWiseAnnualTarget().isEmpty())
                                                for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : dealerSelfCounterSale.getMonthWiseAnnualTarget()) {
                                                    if (monthWiseAnnualTargetModel.getSelfCounterCustomerCode().equals(dealerSelfCounterSale.getCustomerCode()) && monthWiseAnnualTargetModel.getIsAnnualSalesReviewedForSelfCounter().equals(true)) {
                                                        MonthWiseTargetData monthWiseTargetDataforRet = new MonthWiseTargetData();
                                                        monthWiseTargetDataforRet.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                                        monthWiseTargetDataforRet.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                                        monthWiseSelfCounterList.add(monthWiseTargetDataforRet);
                                                    }
                                                }
                                            selfCounterSaleData.setMonthWiseTarget(monthWiseSelfCounterList);
                                            annualSalesMonthWiseTargetData.setSelfCounterSale(selfCounterSaleData);
                                        }
                                        annualSalesMonthWiseTargetDataList.add(annualSalesMonthWiseTargetData);
                                    }
                                }
                            }
                        }
                    }
                }
                result.setPagination(annualSalesModelResult.getPagination());
                result.setSorts(annualSalesModelResult.getSorts());
                result.setResults(annualSalesMonthWiseTargetDataList);
            } else {
                SclCustomerModel sclCustomer = (SclCustomerModel) userService.getUserForUID(filter);

                if (annualSalesMonthwiseModel.getIsAnnualSalesReviewedForDealer() != null && annualSalesMonthwiseModel.getIsAnnualSalesReviewedForDealer().equals(false)) {
                    RetailerRevisedAnnualSalesModel retailerRevisedAnnualSalesModel = salesPlanningDao.findRetailerRevisedDetailsByCustomerCode(sclCustomer.getUid(), subAreaMaster.getPk().toString(), currentUser, findNextFinancialYear());
                    if (retailerRevisedAnnualSalesModel != null) {
                        for (SubAreaMasterModel subAreaMasterModel : annualSalesMonthwiseModel.getSubAreaMasterList()) {
                            if (subAreaMasterModel.equals(retailerRevisedAnnualSalesModel.getSubAreaMaster())) {
                           /* listData.setSubArea(subAreaMasterModel.getTaluka());
                            listData.setSubAreaId(subAreaMasterModel.getPk().toString());*/
                                AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData = new AnnualSalesMonthWiseTargetData();
                                annualSalesMonthWiseTargetData.setCustomerCode(retailerRevisedAnnualSalesModel.getCustomerCode());
                                annualSalesMonthWiseTargetData.setCustomerName(retailerRevisedAnnualSalesModel.getCustomerName());
                                annualSalesMonthWiseTargetData.setCustomerPotential(retailerRevisedAnnualSalesModel.getCustomerPotential() != null ? retailerRevisedAnnualSalesModel.getCustomerPotential() : 0.0);
                                annualSalesMonthWiseTargetData.setTotalTarget(retailerRevisedAnnualSalesModel.getTotalTarget());
                                annualSalesMonthWiseTargetData.setErpCustomerNo(retailerRevisedAnnualSalesModel.getErpCustomerNo());

                                if (retailerRevisedAnnualSalesModel.getMonthWiseAnnualTarget() != null &&
                                        !retailerRevisedAnnualSalesModel.getMonthWiseAnnualTarget().isEmpty()) {
                                    List<MonthWiseTargetData> monthWiseTargetDataList = new ArrayList<>();
                                    for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : retailerRevisedAnnualSalesModel.getMonthWiseAnnualTarget()) {
                                        MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                        if (monthWiseAnnualTargetModel.getCustomerCode().equals(retailerRevisedAnnualSalesModel.getCustomerCode()) && monthWiseAnnualTargetModel.getIsAnnualSalesRevisedForRetailer().equals(true)) {
                                            monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                            monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                            monthWiseTargetDataList.add(monthWiseTargetData);
                                        }
                                    }
                                    annualSalesMonthWiseTargetData.setMonthWiseTarget(monthWiseTargetDataList);
                                }

                                List<RetailerDetailsData> retailerDetailsDataList = new ArrayList<>();
                                // List<SKUData> skuDataList = new ArrayList<>();
                                if (retailerRevisedAnnualSalesModel.getListOfRetailersRevised() != null && !retailerRevisedAnnualSalesModel.getListOfRetailersRevised().isEmpty()) {
                                    for (RetailerRevisedAnnualSalesDetailsModel revisedAnnualSalesDetailsModel : retailerRevisedAnnualSalesModel.getListOfRetailersRevised()) {
                                        RetailerDetailsData retailerDetailsData = new RetailerDetailsData();
                                        retailerDetailsData.setCustomerCode(revisedAnnualSalesDetailsModel.getCustomerCode());
                                        retailerDetailsData.setErpCustomerNo(revisedAnnualSalesDetailsModel.getErpCustomerNo());
                                        retailerDetailsData.setCustomerName(revisedAnnualSalesDetailsModel.getCustomerName());
                                        retailerDetailsData.setTotalTarget(revisedAnnualSalesDetailsModel.getTotalTarget());
                                        retailerDetailsData.setCustomerPotential(revisedAnnualSalesDetailsModel.getCustomerPotential());


                                        List<MonthWiseTargetData> monthWiseTargetDataSkuList = new ArrayList<>();
                                        List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetList = salesPlanningService.getMonthWiseRetailerDetailsBeforeReview(retailerRevisedAnnualSalesModel.getCustomerCode(), revisedAnnualSalesDetailsModel.getCustomerCode(), retailerRevisedAnnualSalesModel.getSubAreaMaster().getPk().toString(), currentUser);
                                        for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : monthWiseAnnualTargetList) {
                                            MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                            if (monthWiseAnnualTargetModel.getCustomerCode().equalsIgnoreCase(retailerRevisedAnnualSalesModel.getCustomerCode()) &&
                                                    monthWiseAnnualTargetModel.getRetailerCode().equals(revisedAnnualSalesDetailsModel.getCustomerCode()) &&
                                                    monthWiseAnnualTargetModel.getIsAnnualSalesRevisedForRetailer().equals(true)) {
                                                monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                                monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                                monthWiseTargetDataSkuList.add(monthWiseTargetData);
                                            }
                                        }
                                        retailerDetailsData.setMonthWiseSkuTarget(monthWiseTargetDataSkuList);
                                        retailerDetailsDataList.add(retailerDetailsData);
                                    }
                                }
                                annualSalesMonthWiseTargetData.setRetailerData(retailerDetailsDataList);
                                annualSalesMonthWiseTargetDataList.add(annualSalesMonthWiseTargetData);
                            }
                        }
                    }
                } else {
                    RetailerRevisedAnnualSalesModel retailerRevisedAnnualSalesModel = salesPlanningDao.findRetailerRevisedDetailsByCustomerCode(sclCustomer.getUid(), subAreaMaster.getPk().toString(), currentUser, findNextFinancialYear());
                    if (retailerRevisedAnnualSalesModel != null && retailerRevisedAnnualSalesModel.getIsExistingRetailerRevisedForReview() != null && retailerRevisedAnnualSalesModel.getIsExistingRetailerRevisedForReview().equals(true)) {
                        for (SubAreaMasterModel subAreaMasterModel : annualSalesMonthwiseModel.getSubAreaMasterList()) {
                            if (subAreaMasterModel.equals(retailerRevisedAnnualSalesModel.getSubAreaMaster())) {
                           /* listData.setSubArea(subAreaMasterModel.getTaluka());
                            listData.setSubAreaId(subAreaMasterModel.getPk().toString());*/
                                AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData = new AnnualSalesMonthWiseTargetData();
                                annualSalesMonthWiseTargetData.setCustomerCode(retailerRevisedAnnualSalesModel.getCustomerCode());
                                annualSalesMonthWiseTargetData.setCustomerName(retailerRevisedAnnualSalesModel.getCustomerName());
                                annualSalesMonthWiseTargetData.setCustomerPotential(retailerRevisedAnnualSalesModel.getCustomerPotential() != null ? retailerRevisedAnnualSalesModel.getCustomerPotential() : 0.0);
                                annualSalesMonthWiseTargetData.setTotalTarget(retailerRevisedAnnualSalesModel.getTotalTarget());
                                annualSalesMonthWiseTargetData.setErpCustomerNo(retailerRevisedAnnualSalesModel.getErpCustomerNo());

                                List<MonthWiseTargetData> monthWiseTargetDataList = new ArrayList<>();
                                if (retailerRevisedAnnualSalesModel.getMonthWiseAnnualTarget() != null &&
                                        !retailerRevisedAnnualSalesModel.getMonthWiseAnnualTarget().isEmpty()) {
                                    for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : retailerRevisedAnnualSalesModel.getMonthWiseAnnualTarget()) {
                                        MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                        if (monthWiseAnnualTargetModel.getCustomerCode().equals(retailerRevisedAnnualSalesModel.getCustomerCode()) && monthWiseAnnualTargetModel.getIsAnnualSalesRevisedForRetailer().equals(true)) {
                                            monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                            monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                            monthWiseTargetDataList.add(monthWiseTargetData);
                                        }
                                    }
                                }
                                annualSalesMonthWiseTargetData.setMonthWiseTarget(monthWiseTargetDataList);


                                List<RetailerDetailsData> retailerDetailsDataList = new ArrayList<>();
                                if (retailerRevisedAnnualSalesModel.getListOfRetailersRevised() != null && !retailerRevisedAnnualSalesModel.getListOfRetailersRevised().isEmpty()) {
                                    for (RetailerRevisedAnnualSalesDetailsModel revisedAnnualSalesDetailsModel : retailerRevisedAnnualSalesModel.getListOfRetailersRevised()) {
                                        RetailerDetailsData retailerDetailsData = new RetailerDetailsData();
                                        retailerDetailsData.setCustomerCode(revisedAnnualSalesDetailsModel.getCustomerCode());
                                        retailerDetailsData.setCustomerName(revisedAnnualSalesDetailsModel.getCustomerName());
                                        retailerDetailsData.setCustomerPotential(revisedAnnualSalesDetailsModel.getCustomerPotential());
                                        retailerDetailsData.setTotalTarget(revisedAnnualSalesDetailsModel.getTotalTarget());
                                        retailerDetailsData.setErpCustomerNo(revisedAnnualSalesDetailsModel.getErpCustomerNo());

                                        List<MonthWiseTargetData> monthWiseTargetDataSkuList = new ArrayList<>();
                                        List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetList = salesPlanningService.getMonthWiseRetailerDetailsForReview(retailerRevisedAnnualSalesModel.getCustomerCode(), revisedAnnualSalesDetailsModel.getCustomerCode(), retailerRevisedAnnualSalesModel.getSubAreaMaster().getPk().toString(), currentUser);
                                        for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : monthWiseAnnualTargetList) {
                                            MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                            if (monthWiseAnnualTargetModel.getCustomerCode().equalsIgnoreCase(retailerRevisedAnnualSalesModel.getCustomerCode()) && monthWiseAnnualTargetModel.getRetailerCode().equals(revisedAnnualSalesDetailsModel.getCustomerCode()) && monthWiseAnnualTargetModel.getIsAnnualSalesRevisedForRetailer().equals(true)) {
                                                monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                                monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                                monthWiseTargetDataSkuList.add(monthWiseTargetData);
                                            }
                                        }
                                        retailerDetailsData.setMonthWiseSkuTarget(monthWiseTargetDataSkuList);
                                        retailerDetailsDataList.add(retailerDetailsData);
                                    }
                                }
                                annualSalesMonthWiseTargetData.setRetailerData(retailerDetailsDataList);
                                annualSalesMonthWiseTargetDataList.add(annualSalesMonthWiseTargetData);
                            }
                        }
                    }
                }
            }
            //results
            result.setPagination(annualSalesModelResult.getPagination());
            result.setSorts(annualSalesModelResult.getSorts());
            result.setResults(annualSalesMonthWiseTargetDataList);
        }
        return result;
    }

    @Override
    public SearchPageData<SpOnboardAnnualTargetSettingData> viewReviewedSalesforRetailesMonthwiseOnboarded(SearchPageData searchPageData, String subArea, String filter) {
        SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
        BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
        SubAreaMasterModel subAreaMaster = territoryManagementService.getTerritoryById(subArea);

        final SearchPageData<SpOnboardAnnualTargetSettingData> result = new SearchPageData<>();
        List<SpOnboardAnnualTargetSettingData> spOnboardAnnualTargetSettingDataList = new ArrayList<>();
        //search
        SearchPageData<AnnualSalesModel> annualSalesModelResult = salesPlanningService.viewPlannedSalesforDealersRetailersMonthWise(searchPageData, subAreaMaster.getPk().toString(), currentUser, brand);
        for (AnnualSalesModel annualSalesMonthwiseModel : annualSalesModelResult.getResults()) {

            AnnualSalesMonthWiseTargetListData list = new AnnualSalesMonthWiseTargetListData();
            if (StringUtils.isBlank(filter)) {
                if (annualSalesMonthwiseModel.getIsAnnualSalesReviewedForRetailer() != null && annualSalesMonthwiseModel.getIsAnnualSalesReviewedForRetailer().equals(false)) {
                    if (annualSalesMonthwiseModel.getRetailerRevisedAnnualSales() != null && Objects.nonNull(annualSalesMonthwiseModel.getRetailerRevisedAnnualSales()) &&
                            !annualSalesMonthwiseModel.getRetailerRevisedAnnualSales().isEmpty()) {

                    //    for (RetailerRevisedAnnualSalesModel retailerRevisedAnnualSalesModel : annualSalesMonthwiseModel.getRetailerRevisedAnnualSales()) {
                      //      for (SubAreaMasterModel subAreaMasterModel : annualSalesMonthwiseModel.getSubAreaMasterList()) {
                            //    if (subAreaMasterModel.equals(retailerRevisedAnnualSalesModel.getSubAreaMaster())) {

                                    int intervalPeriod = 15;
                                    List<SclCustomerModel> retailerDetailsForOnboarded = salesPlanningService.getDealerDetailsForOnboarded(subArea, intervalPeriod);
                                    List<RetailerDetailsData> retailerDetailsListForOnboard = new ArrayList<>();

                                    String[] s = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "InvalidNumber"};
                                    Map<String, String> results = new LinkedHashMap<>();
                                    results.put("4", "0");
                                    results.put("5", "0");
                                    results.put("6", "0");
                                    results.put("7", "0");

                                    results.put("8", "0");
                                    results.put("9", "0");
                                    results.put("10", "0");
                                    results.put("11", "0");

                                    results.put("12", "0");
                                    results.put("1", "0");
                                    results.put("2", "0");
                                    results.put("3", "0");

                                //    List<SpOnboardAnnualTargetSettingData> spOnboardAnnualTargetSettingDataList = new ArrayList<>();
                                    if (retailerDetailsForOnboarded != null && !retailerDetailsForOnboarded.isEmpty()) {
                                        for (SclCustomerModel sclCustomerModel : retailerDetailsForOnboarded) {
                                            SpOnboardAnnualTargetSettingData spOnboardAnnualTargetSettingData = new SpOnboardAnnualTargetSettingData();
                                            spOnboardAnnualTargetSettingData.setCustomerCode(sclCustomerModel.getUid());
                                            spOnboardAnnualTargetSettingData.setCustomerName(sclCustomerModel.getName());
                                            spOnboardAnnualTargetSettingData.setErpCustomerNo(sclCustomerModel.getCustomerNo());
                                            spOnboardAnnualTargetSettingData.setCustomerPotential(sclCustomerModel.getCounterPotential() != null ? sclCustomerModel.getCounterPotential() : 0.0);
                                            spOnboardAnnualTargetSettingData.setIsNewDealerOnboarded(true);
                                            spOnboardAnnualTargetSettingData.setRetailerData(retailerDetailsListForOnboard);

                                            List<MonthWiseTargetData> monthWiseTargetDataListOnboard = new ArrayList<>();
                                            for (Map.Entry<String, String> mapEntries : results.entrySet()) {
                                                MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                                String key = mapEntries.getKey();
                                                StringBuilder str = new StringBuilder();
                                                if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 12) {
                                                    if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 3) {
                                                        monthWiseTargetData.setMonthYear(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear() + 1))));
                                                    }
                                                    if (Integer.parseInt(key) >= 4 && Integer.parseInt(key) <= 12) {
                                                        monthWiseTargetData.setMonthYear(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear()))));
                                                    }
                                                }
                                                monthWiseTargetData.setMonthTarget(0.0);
                                                monthWiseTargetDataListOnboard.add(monthWiseTargetData);
                                            }
                                            spOnboardAnnualTargetSettingData.setMonthWiseTarget(monthWiseTargetDataListOnboard);

                                            SelfCounterSaleData selfCounterSaleData=new SelfCounterSaleData();
                                            selfCounterSaleData.setCustomerCode(sclCustomerModel.getUid());
                                            selfCounterSaleData.setCustomerName(sclCustomerModel.getName());
                                            selfCounterSaleData.setErpCustomerNo(sclCustomerModel.getCustomerNo());
                                            selfCounterSaleData.setCustomerPotential(Objects.nonNull(sclCustomerModel.getCounterPotential())?sclCustomerModel.getCounterPotential():0.0);


                                            List<MonthWiseTargetData> monthWiseTargetDataListOnboardSelf = new ArrayList<>();
                                            for (Map.Entry<String, String> mapEntries : results.entrySet()) {
                                                MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                                String key = mapEntries.getKey();
                                                StringBuilder str = new StringBuilder();
                                                if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 12) {
                                                    if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 3) {
                                                        monthWiseTargetData.setMonthYear(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear() + 1))));
                                                    }
                                                    if (Integer.parseInt(key) >= 4 && Integer.parseInt(key) <= 12) {
                                                        monthWiseTargetData.setMonthYear(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear()))));
                                                    }
                                                }
                                                monthWiseTargetData.setMonthTarget(0.0);
                                                monthWiseTargetDataListOnboardSelf.add(monthWiseTargetData);
                                            }
                                            selfCounterSaleData.setMonthWiseTarget(monthWiseTargetDataListOnboardSelf);

                                            spOnboardAnnualTargetSettingData.setSelfCounterSale(selfCounterSaleData);
                                            spOnboardAnnualTargetSettingDataList.add(spOnboardAnnualTargetSettingData);
                                        }
                                    }
                         //       }
                       //     }
                      //  }
                    }
                } else {
                    if (annualSalesMonthwiseModel.getRetailerRevisedAnnualSales() != null && Objects.nonNull(annualSalesMonthwiseModel.getRetailerRevisedAnnualSales()) &&
                            !annualSalesMonthwiseModel.getRetailerRevisedAnnualSales().isEmpty()) {
                      //  List<SpOnboardAnnualTargetSettingData> spOnboardAnnualTargetSettingDataList = new ArrayList<>();
                        for (RetailerRevisedAnnualSalesModel retailerRevisedAnnualSalesModel : annualSalesMonthwiseModel.getRetailerRevisedAnnualSales()) {
                            for (SubAreaMasterModel subAreaMasterModel : annualSalesMonthwiseModel.getSubAreaMasterList()) {
                                if (subAreaMasterModel.equals(retailerRevisedAnnualSalesModel.getSubAreaMaster())) {
                                        if (retailerRevisedAnnualSalesModel.getIsNewDealerOnboarded() != null && retailerRevisedAnnualSalesModel.getIsNewDealerOnboarded().equals(true)) {
                                            SpOnboardAnnualTargetSettingData onboardedData = new SpOnboardAnnualTargetSettingData();
                                            onboardedData.setCustomerCode(retailerRevisedAnnualSalesModel.getCustomerCode());
                                            onboardedData.setCustomerName(retailerRevisedAnnualSalesModel.getCustomerName());
                                            onboardedData.setCustomerPotential(retailerRevisedAnnualSalesModel.getCustomerPotential());
                                            onboardedData.setTotalTarget(retailerRevisedAnnualSalesModel.getTotalTarget());
                                            onboardedData.setErpCustomerNo(retailerRevisedAnnualSalesModel.getErpCustomerNo());

                                            List<MonthWiseTargetData> monthWiseListForOnboardedRetailer = new ArrayList<>();
                                            if (retailerRevisedAnnualSalesModel.getMonthWiseAnnualTarget() != null && !retailerRevisedAnnualSalesModel.getMonthWiseAnnualTarget().isEmpty()) {
                                                for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : retailerRevisedAnnualSalesModel.getMonthWiseAnnualTarget()) {
                                                    if (monthWiseAnnualTargetModel.getCustomerCode().equals(retailerRevisedAnnualSalesModel.getCustomerCode()) && monthWiseAnnualTargetModel.getIsAnnualSalesOnboardedForRetailer() != null && monthWiseAnnualTargetModel.getIsAnnualSalesOnboardedForRetailer().equals(true)) {
                                                        MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                                        monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                                        monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                                        monthWiseListForOnboardedRetailer.add(monthWiseTargetData);
                                                    }
                                                }
                                            }
                                            onboardedData.setMonthWiseTarget(monthWiseListForOnboardedRetailer);

                                            List<RetailerDetailsData> retailerDetailsDataList = new ArrayList<>();
                                            if (retailerRevisedAnnualSalesModel.getListOfRetailersRevised() != null && !retailerRevisedAnnualSalesModel.getListOfRetailersRevised().isEmpty()) {
                                                for (RetailerRevisedAnnualSalesDetailsModel revisedAnnualSalesDetailsModel : retailerRevisedAnnualSalesModel.getListOfRetailersRevised()) {
                                                    RetailerDetailsData retailerDetailsData = new RetailerDetailsData();

                                                    retailerDetailsData.setCustomerCode(revisedAnnualSalesDetailsModel.getCustomerCode());
                                                    retailerDetailsData.setCustomerName(revisedAnnualSalesDetailsModel.getCustomerName());
                                                    retailerDetailsData.setCustomerPotential(revisedAnnualSalesDetailsModel.getCustomerPotential());
                                                    retailerDetailsData.setErpCustomerNo(revisedAnnualSalesDetailsModel.getErpCustomerNo());
                                                    retailerDetailsData.setTotalTarget(revisedAnnualSalesDetailsModel.getTotalTarget());
                                                    retailerDetailsData.setIsNewRetailerOnboarded(revisedAnnualSalesDetailsModel.getIsNewRetailerOnboarded());

                                                    List<MonthWiseTargetData> monthWiseListForOnboardedDealerForSku = new ArrayList<>();
                                                    List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetModelDetails = getSalesPlanningDao().validateReviewForOnboardedRetailerSaleForMonthWise(retailerRevisedAnnualSalesModel.getCustomerCode(), revisedAnnualSalesDetailsModel.getCustomerCode(), subAreaMaster.getPk().toString(), currentUser);
                                                    if (monthWiseAnnualTargetModelDetails != null && !monthWiseAnnualTargetModelDetails.isEmpty()) {
                                                        for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModelDetail : monthWiseAnnualTargetModelDetails) {
                                                            if (monthWiseAnnualTargetModelDetail.getCustomerCode().equals(retailerRevisedAnnualSalesModel.getCustomerCode()) && (monthWiseAnnualTargetModelDetail.getRetailerCode().equals(revisedAnnualSalesDetailsModel.getCustomerCode())) && monthWiseAnnualTargetModelDetail.getIsAnnualSalesOnboardedForRetailer() != null && monthWiseAnnualTargetModelDetail.getIsAnnualSalesOnboardedForRetailer().equals(true)) {
                                                                MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                                                monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModelDetail.getMonthYear());
                                                                monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModelDetail.getMonthTarget());
                                                                monthWiseListForOnboardedDealerForSku.add(monthWiseTargetData);
                                                            }
                                                        }
                                                    }
                                                    retailerDetailsData.setMonthWiseSkuTarget(monthWiseListForOnboardedDealerForSku);
                                                    retailerDetailsDataList.add(retailerDetailsData);
                                                }
                                                onboardedData.setRetailerData(retailerDetailsDataList);
                                            }
                                            else
                                            {
                                                onboardedData.setRetailerData(retailerDetailsDataList);
                                                onboardedData.setIsNewDealerOnboarded(retailerRevisedAnnualSalesModel.getIsNewDealerOnboarded());
                                            }
                                            SelfCounterSaleDetailsModel dealerSelfCounterSale = retailerRevisedAnnualSalesModel.getDealerSelfCounterSale();
                                            SelfCounterSaleData selfCounterSaleData=new SelfCounterSaleData();
                                            selfCounterSaleData.setCustomerCode(dealerSelfCounterSale.getCustomerCode());
                                            selfCounterSaleData.setErpCustomerNo(dealerSelfCounterSale.getErpCustomerNo());
                                            selfCounterSaleData.setCustomerName(dealerSelfCounterSale.getCustomerName());
                                            selfCounterSaleData.setCustomerPotential(dealerSelfCounterSale.getCustomerPotential());

                                            List<MonthWiseTargetData> monthWiseSelfCounterList = new ArrayList<>();
                                            if (dealerSelfCounterSale.getMonthWiseAnnualTarget() != null && !dealerSelfCounterSale.getMonthWiseAnnualTarget().isEmpty())
                                                for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : dealerSelfCounterSale.getMonthWiseAnnualTarget()) {
                                                    if (dealerSelfCounterSale.getCustomerCode().equalsIgnoreCase(monthWiseAnnualTargetModel.getSelfCounterCustomerCode()) &&
                                                            monthWiseAnnualTargetModel.getIsAnnualSalesOnboardedForRetailer().equals(true)) {
                                                        MonthWiseTargetData monthWiseTargetDataforRet = new MonthWiseTargetData();
                                                        monthWiseTargetDataforRet.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                                        monthWiseTargetDataforRet.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                                        monthWiseSelfCounterList.add(monthWiseTargetDataforRet);
                                                    }
                                                }
                                            selfCounterSaleData.setMonthWiseTarget(monthWiseSelfCounterList);
                                            onboardedData.setSelfCounterSale(selfCounterSaleData);
                                            spOnboardAnnualTargetSettingDataList.add(onboardedData);
                                        }
                                }
                            }
                        }
                    }
                }
                result.setPagination(annualSalesModelResult.getPagination());
                result.setSorts(annualSalesModelResult.getSorts());
                result.setResults(spOnboardAnnualTargetSettingDataList);
            }
        }
        return result;
    }

    @Override
    public SalesHighPriorityActionData sendAlertForSalesPlanHighPriorityAction() {
        SclUserModel sclUser = (SclUserModel) userService.getCurrentUser();
        SalesHighPriorityActionData salesHighPriorityActionData = new SalesHighPriorityActionData();
        BaseSiteModel baseSite = baseSiteService.getCurrentBaseSite();
        double annualTarget=0.0, monthlyTarget=0.0;

        LocalDate cal=LocalDate.now();
        LocalDate startDate, endDate, startDate1, endDate1;

        List<SubAreaMasterModel> subAreas = territoryManagementService.getTerritoriesForSO();
        if (CollectionUtils.isEmpty(subAreas)) {
            throw new ModelNotFoundException("No Subarea Attached to current user: " + sclUser.getUid());
        }
        if(subAreas!=null && !subAreas.isEmpty()) {
            for (SubAreaMasterModel subArea : subAreas) {
                annualTarget = salesPlanningService.getTotalTargetForDealersAfterTargetSetting(sclUser, subArea.getPk().toString(), findNextFinancialYear(), baseSite);
                startDate1 = LocalDate.of(cal.getYear(), Month.FEBRUARY, 15);
                endDate1 = LocalDate.of(cal.getYear(), Month.FEBRUARY, 23);
                if (annualTarget == 0.0 && cal.isBefore(startDate1) && cal.isAfter(endDate1)) {
                    salesHighPriorityActionData.setIsAnnualPlanTarget(true);
                }
                else
                    salesHighPriorityActionData.setIsAnnualPlanTarget(false);

                int noOfDaysInTheMonth = cal.lengthOfMonth();
                startDate = LocalDate.of(cal.getYear(), cal.getMonth(), 15);
                endDate = LocalDate.of(cal.getYear(), cal.getMonth(), noOfDaysInTheMonth);

                monthlyTarget = salesPlanningService.getRevisedTargetAfterTargetSetMonthlySP(sclUser, subArea.getPk().toString());
                if (monthlyTarget == 0.0 && cal.isAfter(startDate) && cal.isBefore(endDate)) {
                    salesHighPriorityActionData.setIsMonthlyPlanTarget(true);
                }
                else
                    salesHighPriorityActionData.setIsMonthlyPlanTarget(false);
            }
        }
        return salesHighPriorityActionData;
    }

    @Override
    public SalesTargetApprovedData targetSendForRevision(SalesRevisedTargetData salesRevisedTargetData) {
        return salesPlanningService.targetSendForRevision(salesRevisedTargetData);
    }

    @Override
    public SalesTargetApprovedData updateTargetStatusForApproval(SalesApprovalData salesApprovalData) {
        return salesPlanningService.updateTargetStatusForApproval(salesApprovalData);
    }

    @Override
    public SearchPageData<AnnualSalesMonthWiseTargetData> viewMonthWiseRetailerDetailsForAnnualSalesPagination(SearchPageData searchPageData, String subArea, String filter) {
            SclUserModel sclUser = (SclUserModel) getUserService().getCurrentUser();
            List<AnnualSalesMonthWiseTargetData> annualSalesMonthWiseTargetDataList = new ArrayList<>();
            BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
            SubAreaMasterModel subAreaMaster=territoryManagementService.getTerritoryById(subArea);

        final SearchPageData<AnnualSalesMonthWiseTargetData> result = new SearchPageData<>();

        //search
        SearchPageData<AnnualSalesModel> annualSalesModelResult=salesPlanningService.viewPlannedSalesforDealersRetailersMonthWise(searchPageData,subAreaMaster.getPk().toString(), sclUser, brand);
        for (AnnualSalesModel annualSalesModel : annualSalesModelResult.getResults()) {

            AnnualSalesMonthWiseTargetListData list = new AnnualSalesMonthWiseTargetListData();
            if (StringUtils.isBlank(filter)) {
                if (annualSalesModel != null) {
                    if (annualSalesModel.getRetailerPlannedAnnualSales() != null && !annualSalesModel.getRetailerPlannedAnnualSales().isEmpty()) {
                        for (RetailerPlannedAnnualSalesModel retailerPlannedAnnualSale : annualSalesModel.getRetailerPlannedAnnualSales()) {
                            if (retailerPlannedAnnualSale.getSubAreaMaster().equals(subAreaMaster)) {
                                AnnualSalesMonthWiseTargetData data = new AnnualSalesMonthWiseTargetData();
                                data.setCustomerCode(retailerPlannedAnnualSale.getCustomerCode());
                                data.setCustomerName(retailerPlannedAnnualSale.getCustomerName());
                                data.setErpCustomerNo(retailerPlannedAnnualSale.getErpCustomerNo());
                                data.setCustomerPotential(retailerPlannedAnnualSale.getCustomerPotential() != null ? retailerPlannedAnnualSale.getCustomerPotential() : 0.0);
                                data.setTotalTarget(retailerPlannedAnnualSale.getTotalTarget());
                                List<RetailerDetailsData> retailerDetailsDataList = new ArrayList<>();
                                if (retailerPlannedAnnualSale.getListOfRetailersPlanned() != null && !retailerPlannedAnnualSale.getListOfRetailersPlanned().isEmpty()) {
                                    for (RetailerPlannedAnnualSalesDetailsModel retailer : retailerPlannedAnnualSale.getListOfRetailersPlanned()) {
                                        RetailerDetailsData retailerDetailsData = new RetailerDetailsData();
                                        retailerDetailsData.setCustomerCode(retailer.getCustomerCode());
                                        retailerDetailsData.setErpCustomerNo(retailer.getErpCustomerNo());
                                        retailerDetailsData.setCustomerName(retailer.getCustomerName());
                                        retailerDetailsData.setCustomerPotential(retailer.getCustomerPotential());
                                        retailerDetailsData.setTotalTarget(retailer.getTotalTarget());
                                        List<MonthWiseTargetData> monthWiseTargetDataListForRetailerDetail = new ArrayList<>();
                                        for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : retailer.getMonthWiseAnnualTarget()) {
                                            if(monthWiseAnnualTargetModel.getRetailerCode().equals(retailer.getCustomerCode())) {
                                                MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                                monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                                monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                                monthWiseTargetDataListForRetailerDetail.add(monthWiseTargetData);
                                            }
                                        }
                                        retailerDetailsData.setMonthWiseSkuTarget(monthWiseTargetDataListForRetailerDetail);
                                        retailerDetailsDataList.add(retailerDetailsData);
                                    }
                                }
                                List<MonthWiseTargetData> monthWiseTargetDataList = new ArrayList<>();
                                if (retailerPlannedAnnualSale.getMonthWiseAnnualTarget() != null && !retailerPlannedAnnualSale.getMonthWiseAnnualTarget().isEmpty()) {
                                    for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : retailerPlannedAnnualSale.getMonthWiseAnnualTarget()) {
                                        if (monthWiseAnnualTargetModel.getCustomerCode().equals(retailerPlannedAnnualSale.getCustomerCode())) {
                                            MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                            monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                            monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                            monthWiseTargetDataList.add(monthWiseTargetData);
                                        }
                                    }
                                }

                                List<MonthWiseTargetData> monthWiseTargetDataListforSelf = new ArrayList<>();
                                SelfCounterSaleDetailsModel dealerSelfCounterSale = retailerPlannedAnnualSale.getDealerSelfCounterSale();
                                SelfCounterSaleData selfCounterSaleData = new SelfCounterSaleData();
                                selfCounterSaleData.setCustomerCode(dealerSelfCounterSale.getCustomerCode());
                                selfCounterSaleData.setCustomerName(dealerSelfCounterSale.getCustomerName());
                                selfCounterSaleData.setCustomerPotential(dealerSelfCounterSale.getCustomerPotential());
                                selfCounterSaleData.setTotalTarget(dealerSelfCounterSale.getTotalTarget());
                                selfCounterSaleData.setErpCustomerNo(dealerSelfCounterSale.getErpCustomerNo());
                                for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : dealerSelfCounterSale.getMonthWiseAnnualTarget()) {
                                    if(monthWiseAnnualTargetModel.getSelfCounterCustomerCode().equals(dealerSelfCounterSale.getCustomerCode())) {
                                        MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                        monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                        monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                        monthWiseTargetDataListforSelf.add(monthWiseTargetData);
                                    }
                                }
                                selfCounterSaleData.setMonthWiseTarget(monthWiseTargetDataListforSelf);

                                data.setSelfCounterSale(selfCounterSaleData);
                                data.setMonthWiseTarget(monthWiseTargetDataList);
                                data.setRetailerData(retailerDetailsDataList);
                                annualSalesMonthWiseTargetDataList.add(data);
                            }
                        }
                    }
                }
                result.setPagination(annualSalesModelResult.getPagination());
                result.setSorts(annualSalesModelResult.getSorts());
                result.setResults(annualSalesMonthWiseTargetDataList);
            }
            else {
                //search
                SearchPageData<RetailerPlannedAnnualSalesModel> retailerPlannedAnnualSaleResult = salesPlanningService.fetchRecordForRetailerPlannedAnnualSales(searchPageData, subAreaMaster.getPk().toString(), sclUser, filter);
                for (RetailerPlannedAnnualSalesModel retailerPlannedAnnualSale : retailerPlannedAnnualSaleResult.getResults()) {

                    if (retailerPlannedAnnualSale != null) {
                        if (retailerPlannedAnnualSale.getSubAreaMaster().equals(subAreaMaster)) {
                            AnnualSalesMonthWiseTargetData data = new AnnualSalesMonthWiseTargetData();
                            data.setCustomerCode(retailerPlannedAnnualSale.getCustomerCode());
                            data.setCustomerName(retailerPlannedAnnualSale.getCustomerName());
                            data.setCustomerPotential(retailerPlannedAnnualSale.getCustomerPotential()!=0.0 ? retailerPlannedAnnualSale.getCustomerPotential():0.0);
                            data.setErpCustomerNo(retailerPlannedAnnualSale.getErpCustomerNo());
                            data.setTotalTarget(retailerPlannedAnnualSale.getTotalTarget());
                            List<RetailerDetailsData> retailerDetailsDataList = new ArrayList<>();
                            if (retailerPlannedAnnualSale.getListOfRetailersPlanned() != null && !retailerPlannedAnnualSale.getListOfRetailersPlanned().isEmpty()) {
                                for (RetailerPlannedAnnualSalesDetailsModel retailer : retailerPlannedAnnualSale.getListOfRetailersPlanned()) {
                                    RetailerDetailsData retailerDetailsData = new RetailerDetailsData();
                                    retailerDetailsData.setCustomerCode(retailer.getCustomerCode());
                                    retailerDetailsData.setCustomerName(retailer.getCustomerName());
                                    retailerDetailsData.setCustomerPotential(retailer.getCustomerPotential() != null ? retailer.getCustomerPotential() : 0.0);
                                    retailerDetailsData.setTotalTarget(retailer.getTotalTarget());
                                    retailerDetailsData.setErpCustomerNo(retailer.getErpCustomerNo());
                                    List<MonthWiseTargetData> monthWiseTargetDataListForRetailerDetail = new ArrayList<>();
                                    for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : retailer.getMonthWiseAnnualTarget()) {
                                        if(monthWiseAnnualTargetModel.getRetailerCode().equals(retailer.getCustomerCode())) {
                                            MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                            monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                            monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                            monthWiseTargetDataListForRetailerDetail.add(monthWiseTargetData);
                                        }
                                    }
                                    retailerDetailsData.setMonthWiseSkuTarget(monthWiseTargetDataListForRetailerDetail);
                                    retailerDetailsDataList.add(retailerDetailsData);
                                }
                            }
                            List<MonthWiseTargetData> monthWiseTargetDataList = new ArrayList<>();
                            if (retailerPlannedAnnualSale.getMonthWiseAnnualTarget() != null && !retailerPlannedAnnualSale.getMonthWiseAnnualTarget().isEmpty()) {
                                for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : retailerPlannedAnnualSale.getMonthWiseAnnualTarget()) {
                                    if (monthWiseAnnualTargetModel.getCustomerCode().equals(retailerPlannedAnnualSale.getCustomerCode())) {
                                        MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                        monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                        monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                        monthWiseTargetDataList.add(monthWiseTargetData);
                                    }
                                }
                            }

                            List<MonthWiseTargetData> monthWiseTargetDataListforSelf = new ArrayList<>();
                            SelfCounterSaleDetailsModel dealerSelfCounterSale = retailerPlannedAnnualSale.getDealerSelfCounterSale();
                            SelfCounterSaleData selfCounterSaleData = new SelfCounterSaleData();
                            selfCounterSaleData.setCustomerCode(dealerSelfCounterSale.getCustomerCode());
                            selfCounterSaleData.setCustomerName(dealerSelfCounterSale.getCustomerName());
                            selfCounterSaleData.setCustomerPotential(dealerSelfCounterSale.getCustomerPotential() != null ? dealerSelfCounterSale.getCustomerPotential() : 0.0);
                            selfCounterSaleData.setTotalTarget(dealerSelfCounterSale.getTotalTarget());
                            selfCounterSaleData.setErpCustomerNo(dealerSelfCounterSale.getErpCustomerNo());
                            for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : dealerSelfCounterSale.getMonthWiseAnnualTarget()) {
                                if (monthWiseAnnualTargetModel.getSelfCounterCustomerCode().equals(dealerSelfCounterSale.getCustomerCode())) {
                                    MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                    monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                    monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                    monthWiseTargetDataListforSelf.add(monthWiseTargetData);
                                }
                            }
                            selfCounterSaleData.setMonthWiseTarget(monthWiseTargetDataListforSelf);

                            data.setSelfCounterSale(selfCounterSaleData);
                            data.setMonthWiseTarget(monthWiseTargetDataList);
                            data.setRetailerData(retailerDetailsDataList);
                            annualSalesMonthWiseTargetDataList.add(data);
                        }
                    }
                }
                result.setPagination(retailerPlannedAnnualSaleResult.getPagination());
                result.setSorts(retailerPlannedAnnualSaleResult.getSorts());
                result.setResults(annualSalesMonthWiseTargetDataList);
            }
            list.setSubArea(subAreaMaster.getTaluka());
            list.setSubAreaId(subAreaMaster.getPk().toString());
            list.setAnnualSalesMonthWiseTargetData(annualSalesMonthWiseTargetDataList);
        }
        return result;
    }

    @Override
    public SearchPageData<AnnualSalesMonthWiseTargetData> viewPlannedSalesforRetailerMonthwise(SearchPageData searchPageData, String subArea, String filter) {
        SclUserModel sclUser = (SclUserModel) getUserService().getCurrentUser();
        List<AnnualSalesMonthWiseTargetData> annualSalesMonthWiseTargetDataList = new ArrayList<>();
        BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
        SubAreaMasterModel subAreaMaster=territoryManagementService.getTerritoryById(subArea);

        final SearchPageData<AnnualSalesMonthWiseTargetData> result = new SearchPageData<>();

        //search
        SearchPageData<AnnualSalesModel> annualSalesModelResult=salesPlanningService.viewPlannedSalesforDealersRetailersMonthWise(searchPageData,subAreaMaster.getPk().toString(), sclUser, brand);
        for (AnnualSalesModel annualSalesModel : annualSalesModelResult.getResults()) {

            AnnualSalesMonthWiseTargetListData list = new AnnualSalesMonthWiseTargetListData();
            if (StringUtils.isBlank(filter)) {
                // && annualSalesModel.getIsAnnualSalesPlannedForRetailer().equals(true)
                if (annualSalesModel != null) {
                    if (annualSalesModel.getRetailerPlannedAnnualSales() != null && !annualSalesModel.getRetailerPlannedAnnualSales().isEmpty()) {
                        for (RetailerPlannedAnnualSalesModel retailerPlannedAnnualSale : annualSalesModel.getRetailerPlannedAnnualSales()) {
                            if (retailerPlannedAnnualSale.getSubAreaMaster().equals(subAreaMaster)) {
                                AnnualSalesMonthWiseTargetData data = new AnnualSalesMonthWiseTargetData();
                                data.setCustomerCode(retailerPlannedAnnualSale.getCustomerCode());
                                data.setCustomerName(retailerPlannedAnnualSale.getCustomerName());
                                data.setErpCustomerNo(retailerPlannedAnnualSale.getErpCustomerNo());
                                data.setCustomerPotential(retailerPlannedAnnualSale.getCustomerPotential() != null ? retailerPlannedAnnualSale.getCustomerPotential() : 0.0);
                                data.setTotalTarget(retailerPlannedAnnualSale.getTotalTarget());
                                List<RetailerDetailsData> retailerDetailsDataList = new ArrayList<>();
                                if (retailerPlannedAnnualSale.getListOfRetailersPlanned() != null && !retailerPlannedAnnualSale.getListOfRetailersPlanned().isEmpty()) {
                                    for (RetailerPlannedAnnualSalesDetailsModel retailer : retailerPlannedAnnualSale.getListOfRetailersPlanned()) {
                                        RetailerDetailsData retailerDetailsData = new RetailerDetailsData();
                                        retailerDetailsData.setCustomerCode(retailer.getCustomerCode());
                                        retailerDetailsData.setCustomerName(retailer.getCustomerName());
                                        retailerDetailsData.setCustomerPotential(retailer.getCustomerPotential());
                                        retailerDetailsData.setTotalTarget(retailer.getTotalTarget());
                                        retailerDetailsData.setErpCustomerNo(retailer.getErpCustomerNo());
                                        List<MonthWiseTargetData> monthWiseTargetDataListForRetailerDetail = new ArrayList<>();
                                        for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : retailer.getMonthWiseAnnualTarget()) {
                                            MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                            monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                            monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                            monthWiseTargetDataListForRetailerDetail.add(monthWiseTargetData);
                                        }
                                        retailerDetailsData.setMonthWiseSkuTarget(monthWiseTargetDataListForRetailerDetail);
                                        retailerDetailsDataList.add(retailerDetailsData);
                                    }
                                }
                                List<MonthWiseTargetData> monthWiseTargetDataList = new ArrayList<>();
                                if (retailerPlannedAnnualSale.getMonthWiseAnnualTarget() != null && !retailerPlannedAnnualSale.getMonthWiseAnnualTarget().isEmpty()) {
                                    for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : retailerPlannedAnnualSale.getMonthWiseAnnualTarget()) {
                                        if (monthWiseAnnualTargetModel.getCustomerCode().equals(retailerPlannedAnnualSale.getCustomerCode())) {
                                            MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                            monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                            monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                            monthWiseTargetDataList.add(monthWiseTargetData);
                                        }
                                    }
                                }

                                List<MonthWiseTargetData> monthWiseTargetDataListforSelf = new ArrayList<>();
                                SelfCounterSaleDetailsModel dealerSelfCounterSale = retailerPlannedAnnualSale.getDealerSelfCounterSale();
                                SelfCounterSaleData selfCounterSaleData = new SelfCounterSaleData();
                                selfCounterSaleData.setCustomerCode(dealerSelfCounterSale.getCustomerCode());
                                selfCounterSaleData.setCustomerName(dealerSelfCounterSale.getCustomerName());
                                selfCounterSaleData.setCustomerPotential(dealerSelfCounterSale.getCustomerPotential());
                                selfCounterSaleData.setErpCustomerNo(dealerSelfCounterSale.getErpCustomerNo());
                                selfCounterSaleData.setTotalTarget(dealerSelfCounterSale.getTotalTarget());
                                for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : dealerSelfCounterSale.getMonthWiseAnnualTarget()) {
                                    MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                    monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                    monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                    monthWiseTargetDataListforSelf.add(monthWiseTargetData);
                                }
                                selfCounterSaleData.setMonthWiseTarget(monthWiseTargetDataListforSelf);

                                data.setSelfCounterSale(selfCounterSaleData);
                                data.setMonthWiseTarget(monthWiseTargetDataList);
                                data.setRetailerData(retailerDetailsDataList);
                                annualSalesMonthWiseTargetDataList.add(data);
                            }
                        }
                    }
                }
                result.setPagination(annualSalesModelResult.getPagination());
                result.setSorts(annualSalesModelResult.getSorts());
                result.setResults(annualSalesMonthWiseTargetDataList);
            }
            else {
                //search
                SearchPageData<RetailerPlannedAnnualSalesModel> retailerPlannedAnnualSaleResult = salesPlanningService.fetchRecordForRetailerPlannedAnnualSales(searchPageData, subAreaMaster.getPk().toString(), sclUser, filter);
                for (RetailerPlannedAnnualSalesModel retailerPlannedAnnualSale : retailerPlannedAnnualSaleResult.getResults()) {

                    if (retailerPlannedAnnualSale != null) {
                        if (retailerPlannedAnnualSale.getSubAreaMaster().equals(subAreaMaster)) {
                            AnnualSalesMonthWiseTargetData data = new AnnualSalesMonthWiseTargetData();

                            data.setCustomerCode(retailerPlannedAnnualSale.getCustomerCode());
                            data.setCustomerName(retailerPlannedAnnualSale.getCustomerName());
                            data.setCustomerPotential(retailerPlannedAnnualSale.getCustomerPotential());
                            data.setTotalTarget(retailerPlannedAnnualSale.getTotalTarget());
                            data.setErpCustomerNo(retailerPlannedAnnualSale.getErpCustomerNo());
                            List<RetailerDetailsData> retailerDetailsDataList = new ArrayList<>();
                            if (retailerPlannedAnnualSale.getListOfRetailersPlanned() != null && !retailerPlannedAnnualSale.getListOfRetailersPlanned().isEmpty()) {
                                for (RetailerPlannedAnnualSalesDetailsModel retailer : retailerPlannedAnnualSale.getListOfRetailersPlanned()) {
                                    RetailerDetailsData retailerDetailsData = new RetailerDetailsData();
                                    retailerDetailsData.setCustomerCode(retailer.getCustomerCode());
                                    retailerDetailsData.setErpCustomerNo(retailer.getErpCustomerNo());
                                    retailerDetailsData.setCustomerName(retailer.getCustomerName());
                                    retailerDetailsData.setCustomerPotential(retailer.getCustomerPotential() != null ? retailer.getCustomerPotential() : 0.0);
                                    retailerDetailsData.setTotalTarget(retailer.getTotalTarget());
                                    List<MonthWiseTargetData> monthWiseTargetDataListForRetailerDetail = new ArrayList<>();
                                    for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : retailer.getMonthWiseAnnualTarget()) {
                                        MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                        monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                        monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                        monthWiseTargetDataListForRetailerDetail.add(monthWiseTargetData);
                                    }
                                    retailerDetailsData.setMonthWiseSkuTarget(monthWiseTargetDataListForRetailerDetail);
                                    retailerDetailsDataList.add(retailerDetailsData);
                                }
                            }
                            List<MonthWiseTargetData> monthWiseTargetDataList = new ArrayList<>();
                            if (retailerPlannedAnnualSale.getMonthWiseAnnualTarget() != null && !retailerPlannedAnnualSale.getMonthWiseAnnualTarget().isEmpty()) {
                                for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : retailerPlannedAnnualSale.getMonthWiseAnnualTarget()) {
                                    if (monthWiseAnnualTargetModel.getCustomerCode().equals(retailerPlannedAnnualSale.getCustomerCode())) {
                                        MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                        monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                        monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                        monthWiseTargetDataList.add(monthWiseTargetData);
                                    }
                                }
                            }

                            List<MonthWiseTargetData> monthWiseTargetDataListforSelf = new ArrayList<>();
                            SelfCounterSaleDetailsModel dealerSelfCounterSale = retailerPlannedAnnualSale.getDealerSelfCounterSale();
                            SelfCounterSaleData selfCounterSaleData = new SelfCounterSaleData();
                            selfCounterSaleData.setCustomerCode(dealerSelfCounterSale.getCustomerCode());
                            selfCounterSaleData.setErpCustomerNo(dealerSelfCounterSale.getErpCustomerNo());
                            selfCounterSaleData.setCustomerName(dealerSelfCounterSale.getCustomerName());
                            selfCounterSaleData.setCustomerPotential(dealerSelfCounterSale.getCustomerPotential() != null ? dealerSelfCounterSale.getCustomerPotential() : 0.0);
                            selfCounterSaleData.setTotalTarget(dealerSelfCounterSale.getTotalTarget());
                            for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : dealerSelfCounterSale.getMonthWiseAnnualTarget()) {
                                MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                monthWiseTargetDataListforSelf.add(monthWiseTargetData);
                            }
                            selfCounterSaleData.setMonthWiseTarget(monthWiseTargetDataListforSelf);

                            data.setSelfCounterSale(selfCounterSaleData);
                            data.setMonthWiseTarget(monthWiseTargetDataList);
                            data.setRetailerData(retailerDetailsDataList);
                            annualSalesMonthWiseTargetDataList.add(data);
                        }
                    }
                }
                result.setPagination(retailerPlannedAnnualSaleResult.getPagination());
                result.setSorts(retailerPlannedAnnualSaleResult.getSorts());
                result.setResults(annualSalesMonthWiseTargetDataList);
            }
            /*list.setSubArea(subAreaMaster.getTaluka());
            list.setSubAreaId(subAreaMaster.getPk().toString());
            list.setAnnualSalesMonthWiseTargetData(annualSalesMonthWiseTargetDataList);*/
        }
        return result;
    }

    @Override
    public SearchPageData<AnnualSalesTargetSettingData> viewRetailerDetailsForAnnualSalesWithPagination(SearchPageData<Object> searchPageData, String subArea, String filter) {
        SclUserModel sclUser = (SclUserModel) getUserService().getCurrentUser();
        BaseSiteModel baseSite = baseSiteService.getCurrentBaseSite();
        final SearchPageData<AnnualSalesTargetSettingData> result = new SearchPageData<>();
        SubAreaMasterModel subAreaMaster=territoryManagementService.getTerritoryById(subArea);
        String financialYear= findNextFinancialYear();
        AnnualSalesModel annualSalesModelDetails = salesPlanningDao.getAnnualSalesModelDetails(sclUser,financialYear,subAreaMaster.getPk().toString(), baseSite);
        List<AnnualSalesTargetSettingData> annualSalesTargetSettingDataList = new ArrayList<>();
        double retailerCySaleSum=0.0;
        List<String> filteredDealerList = new ArrayList<>();
        if(StringUtils.isBlank(filter)) {
            SearchPageData<RetailerSalesSummaryModel> retailerSummaryDataList = retailerSalesSummaryService.getCurrentProratedFyDetails(subAreaMaster.getPk().toString(),searchPageData, null);
            List<String> unfilteredDealerList = retailerSummaryDataList.getResults().stream().map(p -> p.getDealerCode()).collect(Collectors.toList());
            if(unfilteredDealerList!=null && !unfilteredDealerList.isEmpty()) {
                filteredDealerList  = unfilteredDealerList.stream().distinct().collect(Collectors.toList());
            }
            if(filteredDealerList!=null && !filteredDealerList.isEmpty()) {
                for (String dealer : filteredDealerList) {
                    SclCustomerModel dealerCode = (SclCustomerModel) getUserService().getUserForUID(dealer);
                    AnnualSalesTargetSettingData annualSalesTargetSettingData = new AnnualSalesTargetSettingData();
                    annualSalesTargetSettingData.setCustomerCode(dealerCode.getUid());
                    annualSalesTargetSettingData.setCustomerName(dealerCode.getName());
                    annualSalesTargetSettingData.setCustomerPotential(dealerCode.getCounterPotential() != null ? dealerCode.getCounterPotential() : 0.0);
                    annualSalesTargetSettingData.setErpCustomerNo(dealerCode.getCustomerNo());
                    //Dealer Cy Sale
                    double dealerCySale = salesPlanningService.fetchDealerCySalesForRetailerAnnualSales(subAreaMaster.getPk().toString(), sclUser, baseSite, annualSalesTargetSettingData.getCustomerCode());
                    annualSalesTargetSettingData.setCurrentYearSales(dealerCySale);

                    if(annualSalesModelDetails!=null)
                    {
                        if(annualSalesModelDetails.getRetailerPlannedAnnualSales()!=null && !annualSalesModelDetails.getRetailerPlannedAnnualSales().isEmpty())
                        {
                            for (RetailerPlannedAnnualSalesModel retailerPlannedAnnualSale : annualSalesModelDetails.getRetailerPlannedAnnualSales()) {
                                if(retailerPlannedAnnualSale.getCustomerCode().equals(annualSalesTargetSettingData.getCustomerCode())) {
                                    if (retailerPlannedAnnualSale.getPlannedYearSales() != null)
                                        annualSalesTargetSettingData.setPlanSales(retailerPlannedAnnualSale.getPlannedYearSales());
                                }
                                else
                                {
                                    annualSalesTargetSettingData.setPlanSales(0.0);
                                }
                            }
                        }
                        else
                        {
                            annualSalesTargetSettingData.setPlanSales(0.0);
                        }
                    }
                    else {
                        annualSalesTargetSettingData.setPlanSales(0.0);
                    }
                    List<RetailerDetailsData> listOfRetailers = new ArrayList<>();
                    List<String> retailerList = salesPlanningService.getRetailerListByDealerCode(dealerCode.getUid(), subAreaMaster.getPk().toString());
                    if (retailerList != null && !retailerList.isEmpty()) {
                        for (String retailer : retailerList) {
                            SclCustomerModel retailerCode = (SclCustomerModel) getUserService().getUserForUID(retailer);

                            RetailerDetailsData retailerDetailsData = new RetailerDetailsData();
                            retailerDetailsData.setCustomerCode(retailerCode.getUid());
                            retailerDetailsData.setCustomerName(retailerCode.getName());
                            retailerDetailsData.setCustomerPotential(retailerCode.getCounterPotential() != null ? retailerCode.getCounterPotential() : 0.0);
                            retailerDetailsData.setErpCustomerNo(retailerCode.getCustomerNo());

                            double retailerCySale = salesPlanningService.getRetailerCySale(dealerCode.getUid(), retailerCode.getUid(), subAreaMaster.getPk().toString());
                            retailerDetailsData.setCySales(retailerCySale);

                            if(annualSalesModelDetails!=null)
                            {
                                if(annualSalesModelDetails.getRetailerPlannedAnnualSales()!=null && !annualSalesModelDetails.getRetailerPlannedAnnualSales().isEmpty())
                                {
                                    for (RetailerPlannedAnnualSalesModel retailerPlannedAnnualSale : annualSalesModelDetails.getRetailerPlannedAnnualSales()) {
                                        if(retailerPlannedAnnualSale.getListOfRetailersPlanned()!=null && !retailerPlannedAnnualSale.getListOfRetailersPlanned().isEmpty()) {
                                            for (RetailerPlannedAnnualSalesDetailsModel retailerPlannedAnnualSalesDetailsModel : retailerPlannedAnnualSale.getListOfRetailersPlanned()) {
                                                if(retailerPlannedAnnualSalesDetailsModel.getCustomerCode().equals(retailerDetailsData.getCustomerCode())) {
                                                    retailerDetailsData.setPlanSales(retailerPlannedAnnualSalesDetailsModel.getPlannedYearSales());
                                                }
                                                else
                                                {
                                                    retailerDetailsData.setPlanSales(0.0);
                                                }
                                            }
                                        }
                                        else
                                        {
                                            retailerDetailsData.setPlanSales(0.0);
                                        }
                                    }
                                }
                                else
                                {
                                    retailerDetailsData.setPlanSales(0.0);
                                }
                            }
                            else {
                                retailerDetailsData.setPlanSales(0.0);
                            }

                            listOfRetailers.add(retailerDetailsData);
                        }
                        annualSalesTargetSettingData.setRetailerData(listOfRetailers);

                        if (annualSalesTargetSettingData.getRetailerData() != null && !annualSalesTargetSettingData.getRetailerData().isEmpty())
                            for (RetailerDetailsData retailerDatum : annualSalesTargetSettingData.getRetailerData()) {
                                retailerCySaleSum += retailerDatum.getCySales();
                            }

                        SelfCounterSaleData selfCounterSaleData = new SelfCounterSaleData();
                        selfCounterSaleData.setCustomerCode(dealerCode.getUid());
                        selfCounterSaleData.setCustomerName(dealerCode.getName());
                        selfCounterSaleData.setCustomerPotential(dealerCode.getCounterPotential() != null ? dealerCode.getCounterPotential() : 0.0);
                        selfCounterSaleData.setErpCustomerNo(dealerCode.getCustomerNo());

                        LOG.info("dealer cy sale" + annualSalesTargetSettingData.getCurrentYearSales());
                        LOG.info("retailer cy sale " + retailerCySaleSum);
                        double selfCounterCySale = 0.0;
                        if (annualSalesTargetSettingData.getCurrentYearSales() != 0.0 && retailerCySaleSum != 0.0) {
                            selfCounterCySale = Math.abs(annualSalesTargetSettingData.getCurrentYearSales() - retailerCySaleSum);
                            if (selfCounterCySale != 0.0) {
                                selfCounterSaleData.setCySales(selfCounterCySale);
                            } else
                                selfCounterSaleData.setCySales(0.0);
                        }

                        if(annualSalesModelDetails!=null) {
                            if (annualSalesModelDetails.getRetailerPlannedAnnualSales() != null && !annualSalesModelDetails.getRetailerPlannedAnnualSales().isEmpty()) {
                                for (RetailerPlannedAnnualSalesModel retailerPlannedAnnualSale : annualSalesModelDetails.getRetailerPlannedAnnualSales()) {
                                    if(retailerPlannedAnnualSale.getDealerSelfCounterSale()!=null)
                                    {
                                        SelfCounterSaleDetailsModel dealerSelfCounterSale = retailerPlannedAnnualSale.getDealerSelfCounterSale();
                                        if(dealerSelfCounterSale.getCustomerCode().equals(selfCounterSaleData.getCustomerCode())) {
                                            selfCounterSaleData.setPlanSales(dealerSelfCounterSale.getPlanSales());
                                        }
                                        else
                                        {
                                            selfCounterSaleData.setPlanSales(0.0);
                                        }
                                    }
                                    else
                                    {
                                        selfCounterSaleData.setPlanSales(0.0);
                                    }
                                }
                            }
                            else
                            {
                                selfCounterSaleData.setPlanSales(0.0);
                            }
                        }
                        else
                        {
                            selfCounterSaleData.setPlanSales(0.0);
                        }

                        annualSalesTargetSettingData.setSelfCounterSale(selfCounterSaleData);
                    }

                    annualSalesTargetSettingDataList.add(annualSalesTargetSettingData);
                }
            }
                result.setResults(annualSalesTargetSettingDataList);
                result.setPagination(retailerSummaryDataList.getPagination());
                result.setSorts(retailerSummaryDataList.getSorts());
            }
        else
        {
            //List<AnnualSalesTargetSettingData> annualTargetDataListNew = new ArrayList<>();
            SearchPageData<RetailerSalesSummaryModel> retailerSummaryDataList = retailerSalesSummaryService.getCurrentProratedFyDetails(subAreaMaster.getPk().toString(),searchPageData, filter);
            List<String> unfilteredDealerList = retailerSummaryDataList.getResults().stream().map(p -> p.getDealerCode()).collect(Collectors.toList());
            if(unfilteredDealerList!=null && !unfilteredDealerList.isEmpty()) {
                filteredDealerList  = unfilteredDealerList.stream().distinct().collect(Collectors.toList());
            }
            if(filteredDealerList!=null && !filteredDealerList.isEmpty()) {
                for (String dealer : filteredDealerList) {
                    SclCustomerModel dealerCode = (SclCustomerModel) getUserService().getUserForUID(dealer);
                    AnnualSalesTargetSettingData annualSalesTargetSettingData = new AnnualSalesTargetSettingData();
                    annualSalesTargetSettingData.setCustomerCode(dealerCode.getUid());
                    annualSalesTargetSettingData.setCustomerName(dealerCode.getName());
                    annualSalesTargetSettingData.setCustomerPotential(dealerCode.getCounterPotential() != null ? dealerCode.getCounterPotential() : 0.0);
                    annualSalesTargetSettingData.setErpCustomerNo(dealerCode.getCustomerNo());
                    //Dealer Cy Sale
                    double dealerCySale = salesPlanningService.fetchDealerCySalesForRetailerAnnualSales(subAreaMaster.getPk().toString(), sclUser, baseSite, annualSalesTargetSettingData.getCustomerCode());
                    annualSalesTargetSettingData.setCurrentYearSales(dealerCySale);

                    if(annualSalesModelDetails!=null)
                    {
                        if(annualSalesModelDetails.getRetailerPlannedAnnualSales()!=null && !annualSalesModelDetails.getRetailerPlannedAnnualSales().isEmpty())
                        {
                            for (RetailerPlannedAnnualSalesModel retailerPlannedAnnualSale : annualSalesModelDetails.getRetailerPlannedAnnualSales()) {
                                if(retailerPlannedAnnualSale.getCustomerCode().equals(annualSalesTargetSettingData.getCustomerCode())) {
                                    if (retailerPlannedAnnualSale.getPlannedYearSales() != null)
                                        annualSalesTargetSettingData.setPlanSales(retailerPlannedAnnualSale.getPlannedYearSales());
                                }
                                else
                                {
                                    annualSalesTargetSettingData.setPlanSales(0.0);
                                }
                            }
                        }
                        else
                        {
                            annualSalesTargetSettingData.setPlanSales(0.0);
                        }
                    }
                    else {
                        annualSalesTargetSettingData.setPlanSales(0.0);
                    }

                    List<RetailerDetailsData> listOfRetailers = new ArrayList<>();
                    List<String> retailerList = salesPlanningService.getRetailerListByDealerCode(dealerCode.getUid(), subAreaMaster.getPk().toString());
                    if (retailerList != null && !retailerList.isEmpty()) {
                        for (String retailer : retailerList) {
                            SclCustomerModel retailerCode = (SclCustomerModel) getUserService().getUserForUID(retailer);

                            RetailerDetailsData retailerDetailsData = new RetailerDetailsData();
                            retailerDetailsData.setCustomerCode(retailerCode.getUid());
                            retailerDetailsData.setCustomerName(retailerCode.getName());
                            retailerDetailsData.setCustomerPotential(retailerCode.getCounterPotential() != null ? retailerCode.getCounterPotential() : 0.0);
                            retailerDetailsData.setErpCustomerNo(retailerCode.getCustomerNo());

                            double retailerCySale = salesPlanningService.getRetailerCySale(dealerCode.getUid(), retailerCode.getUid(), subAreaMaster.getPk().toString());
                            retailerDetailsData.setCySales(retailerCySale);

                            if(annualSalesModelDetails!=null)
                            {
                                if(annualSalesModelDetails.getRetailerPlannedAnnualSales()!=null && !annualSalesModelDetails.getRetailerPlannedAnnualSales().isEmpty())
                                {
                                    for (RetailerPlannedAnnualSalesModel retailerPlannedAnnualSale : annualSalesModelDetails.getRetailerPlannedAnnualSales()) {
                                        if(retailerPlannedAnnualSale.getListOfRetailersPlanned()!=null && !retailerPlannedAnnualSale.getListOfRetailersPlanned().isEmpty()) {
                                            for (RetailerPlannedAnnualSalesDetailsModel retailerPlannedAnnualSalesDetailsModel : retailerPlannedAnnualSale.getListOfRetailersPlanned()) {
                                                if(retailerPlannedAnnualSalesDetailsModel.getCustomerCode().equals(retailerDetailsData.getCustomerCode())) {
                                                    retailerDetailsData.setPlanSales(retailerPlannedAnnualSalesDetailsModel.getPlannedYearSales());
                                                }
                                                else
                                                {
                                                    retailerDetailsData.setPlanSales(0.0);
                                                }
                                            }
                                        }
                                        else
                                        {
                                            retailerDetailsData.setPlanSales(0.0);
                                        }
                                    }
                                }
                                else
                                {
                                    retailerDetailsData.setPlanSales(0.0);
                                }
                            }
                            else {
                                retailerDetailsData.setPlanSales(0.0);
                            }

                            listOfRetailers.add(retailerDetailsData);
                        }
                        annualSalesTargetSettingData.setRetailerData(listOfRetailers);

                        if (annualSalesTargetSettingData.getRetailerData() != null && !annualSalesTargetSettingData.getRetailerData().isEmpty())
                            for (RetailerDetailsData retailerDatum : annualSalesTargetSettingData.getRetailerData()) {
                                retailerCySaleSum += retailerDatum.getCySales();
                            }

                        SelfCounterSaleData selfCounterSaleData = new SelfCounterSaleData();
                        selfCounterSaleData.setCustomerCode(dealerCode.getUid());
                        selfCounterSaleData.setCustomerName(dealerCode.getName());
                        selfCounterSaleData.setCustomerPotential(dealerCode.getCounterPotential() != null ? dealerCode.getCounterPotential() : 0.0);
                        selfCounterSaleData.setErpCustomerNo(dealerCode.getCustomerNo());

                        double selfCounterCySale = 0.0;
                        LOG.info("dealer cy sale" + annualSalesTargetSettingData.getCurrentYearSales());
                        LOG.info("retailer cy sale " + retailerCySaleSum);

                        if (annualSalesTargetSettingData.getCurrentYearSales() != 0.0 && retailerCySaleSum != 0.0) {
                            selfCounterCySale = Math.abs(annualSalesTargetSettingData.getCurrentYearSales() - retailerCySaleSum);
                            if (selfCounterCySale != 0.0) {
                                selfCounterSaleData.setCySales(selfCounterCySale);
                            } else
                                selfCounterSaleData.setCySales(0.0);
                        }
                        if(annualSalesModelDetails!=null) {
                            if (annualSalesModelDetails.getRetailerPlannedAnnualSales() != null && !annualSalesModelDetails.getRetailerPlannedAnnualSales().isEmpty()) {
                                for (RetailerPlannedAnnualSalesModel retailerPlannedAnnualSale : annualSalesModelDetails.getRetailerPlannedAnnualSales()) {
                                    if(retailerPlannedAnnualSale.getDealerSelfCounterSale()!=null)
                                    {
                                        SelfCounterSaleDetailsModel dealerSelfCounterSale = retailerPlannedAnnualSale.getDealerSelfCounterSale();
                                        if(dealerSelfCounterSale.getCustomerCode().equals(selfCounterSaleData.getCustomerCode())) {
                                            selfCounterSaleData.setPlanSales(dealerSelfCounterSale.getPlanSales());
                                        }
                                        else
                                        {
                                            selfCounterSaleData.setPlanSales(0.0);
                                        }
                                    }
                                    else
                                    {
                                        selfCounterSaleData.setPlanSales(0.0);
                                    }
                                }
                            }
                            else
                            {
                                selfCounterSaleData.setPlanSales(0.0);
                            }
                        }
                        else
                        {
                            selfCounterSaleData.setPlanSales(0.0);
                        }

                        annualSalesTargetSettingData.setSelfCounterSale(selfCounterSaleData);
                    }
                    annualSalesTargetSettingDataList.add(annualSalesTargetSettingData);
                }
            }
            result.setResults(annualSalesTargetSettingDataList);
            result.setPagination(retailerSummaryDataList.getPagination());
            result.setSorts(retailerSummaryDataList.getSorts());
        }
        return result;
    }

    /*@Override
    public SearchPageData<AnnualSalesTargetSettingData> viewRetailerDetailsForAnnualSalesWithPagination(SearchPageData<Object> searchPageData, String subArea, String filter) {
        SclUserModel sclUser = (SclUserModel) getUserService().getCurrentUser();
        BaseSiteModel baseSite = baseSiteService.getCurrentBaseSite();
        final SearchPageData<AnnualSalesTargetSettingData> result = new SearchPageData<>();
        SubAreaMasterModel subAreaMaster=territoryManagementService.getTerritoryById(subArea);
        List<AnnualSalesTargetSettingData> annualSalesTargetSettingDataList = new ArrayList<>();
        if(StringUtils.isBlank(filter)) {
            SearchPageData<List<List<Object>>> retailerSummaryDataList = retailerSalesSummaryService.getCurrentProratedFyDetails(subAreaMaster.getPk().toString(),searchPageData, null);
            if(retailerSummaryDataList!=null && !retailerSummaryDataList.getResults().isEmpty()) {
                for (List<List<Object>> retailerSummaryDataListResult : retailerSummaryDataList.getResults()) {
                    for (List<Object> objects : retailerSummaryDataListResult) {
                        SclCustomerModel dealer = (SclCustomerModel) getUserService().getUserForUID((String) objects.get(0));
                        AnnualSalesTargetSettingData annualSalesTargetSettingData = new AnnualSalesTargetSettingData();
                        annualSalesTargetSettingData.setCustomerCode(dealer.getUid());
                        annualSalesTargetSettingData.setCustomerName(dealer.getName());
                        annualSalesTargetSettingData.setCustomerPotential(dealer.getCounterPotential()!=null ? dealer.getCounterPotential() :0.0);
                        annualSalesTargetSettingData.setErpCustomerNo(dealer.getCustomerNo());
                        //Dealer Cy Sale
                        double dealerCySale = salesPlanningService.fetchDealerCySalesForRetailerAnnualSales(subAreaMaster.getPk().toString(), sclUser, baseSite, annualSalesTargetSettingData.getCustomerCode());
                        annualSalesTargetSettingData.setCurrentYearSales(dealerCySale);

                        List<RetailerDetailsData> listOfRetailers = new ArrayList<>();
                        List<List<Object>> retailerList = salesPlanningService.getRetailerDetailsByDealerCode(subAreaMaster.getPk().toString(), annualSalesTargetSettingData.getCustomerCode());
                        if(retailerList!=null && !retailerList.isEmpty()) {
                            for (List<Object> objects1 : retailerList) {
                                SclCustomerModel retailer = (SclCustomerModel) getUserService().getUserForUID((String) objects1.get(0));
                                RetailerDetailsData retailerDetailsData = new RetailerDetailsData();
                                retailerDetailsData.setCustomerCode(retailer.getUid());
                                retailerDetailsData.setCustomerName(retailer.getName());
                                retailerDetailsData.setCustomerPotential(retailer.getCounterPotential()!=null ? retailer.getCounterPotential() : 0.0);
                                retailerDetailsData.setErpCustomerNo(retailer.getCustomerNo());
                                retailerDetailsData.setCySales((Double) objects1.get(1));
                                listOfRetailers.add(retailerDetailsData);
                            }
                            annualSalesTargetSettingData.setRetailerData(listOfRetailers);
                        }

                        SelfCounterSaleData selfCounterSaleData = new SelfCounterSaleData();
                        selfCounterSaleData.setCustomerCode(dealer.getUid());
                        selfCounterSaleData.setCustomerName(dealer.getName());
                        selfCounterSaleData.setCustomerPotential(dealer.getCounterPotential()!=null ? dealer.getCounterPotential() :0.0);
                        selfCounterSaleData.setErpCustomerNo(dealer.getCustomerNo());
                        double retailerCySaleSum=0.0;
                        if(annualSalesTargetSettingData.getRetailerData()!=null && !annualSalesTargetSettingData.getRetailerData().isEmpty())
                            for (RetailerDetailsData retailerDatum : annualSalesTargetSettingData.getRetailerData()) {
                                retailerCySaleSum += retailerDatum.getCySales();
                            }
                        double selfCounterCySale=0.0;
                        if(annualSalesTargetSettingData.getCurrentYearSales()!=0.0 && retailerCySaleSum!=0.0) {
                            selfCounterCySale = annualSalesTargetSettingData.getCurrentYearSales() - retailerCySaleSum;
                            if(selfCounterCySale!=0.0) {
                                selfCounterSaleData.setCySales(selfCounterCySale);
                            }
                            else
                                selfCounterSaleData.setCySales(0.0);
                        }
                        annualSalesTargetSettingData.setSelfCounterSale(selfCounterSaleData);
                        annualSalesTargetSettingDataList.add(annualSalesTargetSettingData);
                    }
                }
            }
            result.setResults(annualSalesTargetSettingDataList);
            result.setPagination(retailerSummaryDataList.getPagination());
            result.setSorts(retailerSummaryDataList.getSorts());
        }
        else {
            SearchPageData<List<List<Object>>> retailerSummaryDataList = retailerSalesSummaryService.getCurrentProratedFyDetails(subAreaMaster.getPk().toString(), searchPageData, null);
            if (retailerSummaryDataList != null && !retailerSummaryDataList.getResults().isEmpty()) {
                for (List<List<Object>> retailerSummaryDataListResult : retailerSummaryDataList.getResults()) {
                    for (List<Object> objects : retailerSummaryDataListResult) {
                        SclCustomerModel dealer = (SclCustomerModel) getUserService().getUserForUID((String) objects.get(0));
                        AnnualSalesTargetSettingData annualSalesTargetSettingData = new AnnualSalesTargetSettingData();
                        annualSalesTargetSettingData.setCustomerCode(dealer.getUid());
                        annualSalesTargetSettingData.setCustomerName(dealer.getName());
                        annualSalesTargetSettingData.setCustomerPotential(dealer.getCounterPotential() != null ? dealer.getCounterPotential() : 0.0);
                        annualSalesTargetSettingData.setErpCustomerNo(dealer.getCustomerNo());
                        //Dealer Cy Sale
                        double dealerCySale = salesPlanningService.fetchDealerCySalesForRetailerAnnualSales(subAreaMaster.getPk().toString(), sclUser, baseSite, annualSalesTargetSettingData.getCustomerCode());
                        annualSalesTargetSettingData.setCurrentYearSales(dealerCySale);

                        List<RetailerDetailsData> listOfRetailers = new ArrayList<>();
                        List<List<Object>> retailerList = salesPlanningService.getRetailerDetailsByDealerCode(subAreaMaster.getPk().toString(), annualSalesTargetSettingData.getCustomerCode());
                        if (retailerList != null && !retailerList.isEmpty()) {
                            for (List<Object> object : retailerList) {
                                SclCustomerModel retailer = (SclCustomerModel) getUserService().getUserForUID((String) object.get(0));
                                RetailerDetailsData retailerDetailsData = new RetailerDetailsData();
                                retailerDetailsData.setCustomerCode(retailer.getUid());
                                retailerDetailsData.setCustomerName(retailer.getName());
                                retailerDetailsData.setCustomerPotential(retailer.getCounterPotential() != null ? retailer.getCounterPotential() : 0.0);
                                retailerDetailsData.setErpCustomerNo(retailer.getCustomerNo());
                                retailerDetailsData.setCySales((Double) object.get(1));
                                listOfRetailers.add(retailerDetailsData);
                            }
                            annualSalesTargetSettingData.setRetailerData(listOfRetailers);
                        }

                        SelfCounterSaleData selfCounterSaleData = new SelfCounterSaleData();
                        selfCounterSaleData.setCustomerCode(dealer.getUid());
                        selfCounterSaleData.setCustomerName(dealer.getName());
                        selfCounterSaleData.setCustomerPotential(dealer.getCounterPotential() != null ? dealer.getCounterPotential() : 0.0);
                        selfCounterSaleData.setErpCustomerNo(dealer.getCustomerNo());
                        double retailerCySaleSum = 0.0;
                        if (annualSalesTargetSettingData.getRetailerData() != null && !annualSalesTargetSettingData.getRetailerData().isEmpty())
                            for (RetailerDetailsData retailerDatum : annualSalesTargetSettingData.getRetailerData()) {
                                retailerCySaleSum += retailerDatum.getCySales();
                            }
                        double selfCounterCySale = 0.0;
                        if (annualSalesTargetSettingData.getCurrentYearSales() != 0.0 && retailerCySaleSum != 0.0) {
                            selfCounterCySale = annualSalesTargetSettingData.getCurrentYearSales() - retailerCySaleSum;
                            if (selfCounterCySale != 0.0) {
                                selfCounterSaleData.setCySales(selfCounterCySale);
                            } else
                                selfCounterSaleData.setCySales(0.0);
                        }
                        annualSalesTargetSettingData.setSelfCounterSale(selfCounterSaleData);
                        annualSalesTargetSettingDataList.add(annualSalesTargetSettingData);
                    }
                }
            }
            result.setResults(annualSalesTargetSettingDataList);
            result.setPagination(retailerSummaryDataList.getPagination());
            result.setSorts(retailerSummaryDataList.getSorts());
        }
        return result;
    }*/

    @Override
    public AnnualSalesMonthWiseTargetListData reviewMonthwiseTargetsForDealer(String subArea, String filter) {
        SclUserModel user = (SclUserModel) getUserService().getCurrentUser();
        BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
        SclUserModel sclUser = null;
        List<AnnualSalesMonthWiseTargetData> annualSalesMonthWiseTargetDataList = new ArrayList<>();
        List<SkuWiseMonthlyTargetData> skuWiseMonthlyTargetDataList = new ArrayList<>();
        Collection<DistrictMasterModel> district = territoryManagementService.getCurrentDistrict();
        SubAreaMasterModel subAreaMaster = territoryManagementService.getTerritoryById(subArea);
        AnnualSalesModel annualSalesModel = null;
        List<SubAreaMasterModel> subAreaMasterModelList = new ArrayList<>();
        subAreaMasterModelList.add(subAreaMaster);
        List<SclUserModel> usersList = territoryManagementService.getUsersForSubAreas(subAreaMasterModelList);
        AnnualSalesMonthWiseTargetListData list = new AnnualSalesMonthWiseTargetListData();
        if (user.getUserType() != null && user.getUserType().equals(SclUserType.TSM))
        {
            if (usersList != null && !usersList.isEmpty()) {
                for (SclUserModel sclUserModel : usersList) {
                    if (sclUserModel.getUserType() != null && sclUserModel.getUserType().equals(SclUserType.SO)) {
                        sclUser = (SclUserModel) getUserService().getUserForUID(sclUserModel.getUid());
                    }
                    annualSalesModel = salesPlanningService.viewPlannedSalesforDealersRetailersMonthWise(subArea, sclUser,brand);

                    if (annualSalesModel != null) {
                        list.setTotalCurrentYearSales(annualSalesModel.getDealerPlannedTotalCySales() != null ? annualSalesModel.getDealerPlannedTotalCySales() : 0.0);
                        list.setTotalPlanSales(annualSalesModel.getDealerPlannedTotalPlanSales() != null ? annualSalesModel.getDealerPlannedTotalPlanSales() : 0.0);
                        list.setSalesOfficer(annualSalesModel.getSalesOfficer()!=null ? annualSalesModel.getSalesOfficer().getUid():"");
                        //new condition
                        // TSM-When revised target is sent back to TSM, sent for review and target should be enabled again

                        //first time - no action (tsm)
                        if(annualSalesModel.getRevisedTarget()==null &&
                                annualSalesModel.getActionPerformedBy()==null && annualSalesModel.getRevisedBy() == null){
                            list.setIsTargetRevised(false);//enable
                            list.setIsTargetApproved(false);//enable
                            LOG.info("first time - no action (tsm)");
                        }
                        //tsm sent to DO for Revision
                       else if(annualSalesModel.getRevisedTarget()!=null && annualSalesModel.getRevisedBy() != null
                                && annualSalesModel.getRevisedBy().equals(user) &&
                                annualSalesModel.getDealerPlannedTotalPlanSales().equals(annualSalesModel.getRevisedTarget()) &&
                                annualSalesModel.getActionPerformedBy()==null )
                        {
                            list.setIsTargetRevised(false);//enable
                            list.setIsTargetApproved(false);
                            LOG.info("tsm sent to DO for Revision");
                        }
                        else if(annualSalesModel.getRevisedTarget()!=null && annualSalesModel.getRevisedBy() != null
                                && annualSalesModel.getRevisedBy().equals(user) &&
                                !(annualSalesModel.getDealerPlannedTotalPlanSales().equals(annualSalesModel.getRevisedTarget())) &&
                                annualSalesModel.getActionPerformedBy()==null )
                        {
                            list.setIsTargetRevised(true);//disable
                            list.setIsTargetApproved(true);
                            LOG.info("tsm sent but DO has not made any action");
                        }
                        else if(annualSalesModel.getRevisedTarget()!=null && annualSalesModel.getRevisedBy() != null
                                && annualSalesModel.getRevisedBy().getUserType().getCode().equalsIgnoreCase("RH") &&
                                !(annualSalesModel.getDealerPlannedTotalPlanSales().equals(annualSalesModel.getRevisedTarget())) &&
                                annualSalesModel.getActionPerformedBy() !=null && annualSalesModel.getActionPerformedBy().equals(user))
                        {
                            list.setIsTargetRevised(false);//enable
                            list.setIsTargetApproved(false);//enable
                            LOG.info("tsm approved by revised by RH");
                        }
                        else if(annualSalesModel.getRevisedTarget()==null && annualSalesModel.getRevisedBy() == null &&
                                annualSalesModel.getActionPerformedBy() !=null && annualSalesModel.getActionPerformedBy().equals(user))
                        {
                            list.setIsTargetRevised(true);//disable
                            list.setIsTargetApproved(true);//disable
                            LOG.info("tsm approved by not revised by tsm - direclty tsm approving");
                        }
                        else if(annualSalesModel.getActionPerformedBy()!=null && annualSalesModel.getActionPerformedBy().equals(user) &&
                                annualSalesModel.getRevisedTarget()!=null && annualSalesModel.getRevisedBy() != null &&
                                annualSalesModel.getRevisedBy().equals(user) && annualSalesModel.getActionPerformed().equals(WorkflowActions.APPROVED)){
                            list.setIsTargetApproved(true);//disable
                            list.setIsTargetRevised(true);//disable
                            LOG.info("approved by and Revised by TSM");
                        }
                    }
                    if (StringUtils.isBlank(filter)) {
                        if (annualSalesModel != null) {
                            if (annualSalesModel.getDealerRevisedAnnualSales() != null && Objects.nonNull(annualSalesModel.getDealerRevisedAnnualSales()) &&
                                    !annualSalesModel.getDealerRevisedAnnualSales().isEmpty()) {
                                for (DealerRevisedAnnualSalesModel dealerRevisedAnnualSalesModel : annualSalesModel.getDealerRevisedAnnualSales()) {
                                   // for (SubAreaMasterModel subAreaMasterModel : annualSalesModel.getSubAreaMasterList()) {
                                        if (dealerRevisedAnnualSalesModel.getIsExistingDealerRevisedForReview() != null && dealerRevisedAnnualSalesModel.getIsNewDealerOnboarded() != null && dealerRevisedAnnualSalesModel.getIsExistingDealerRevisedForReview().equals(false) && dealerRevisedAnnualSalesModel.getIsNewDealerOnboarded().equals(false)) {
                                            if (subAreaMaster.equals(dealerRevisedAnnualSalesModel.getSubAreaMaster())) {
                                                AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData = new AnnualSalesMonthWiseTargetData();
                                                annualSalesMonthWiseTargetData.setCustomerCode(dealerRevisedAnnualSalesModel.getCustomerCode());
                                                annualSalesMonthWiseTargetData.setCustomerName(dealerRevisedAnnualSalesModel.getCustomerName());
                                                annualSalesMonthWiseTargetData.setCustomerPotential(dealerRevisedAnnualSalesModel.getCustomerPotential() != null ? dealerRevisedAnnualSalesModel.getCustomerPotential() : 0.0);
                                                annualSalesMonthWiseTargetData.setTotalTarget(dealerRevisedAnnualSalesModel.getTotalTarget());

                                                List<MonthWiseTargetData> monthWiseTargetDataList = new ArrayList<>();
                                                if (dealerRevisedAnnualSalesModel.getMonthWiseAnnualTarget() != null &&
                                                        !dealerRevisedAnnualSalesModel.getMonthWiseAnnualTarget().isEmpty()) {
                                                    for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : dealerRevisedAnnualSalesModel.getMonthWiseAnnualTarget()) {
                                                        MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                                        if (monthWiseAnnualTargetModel.getCustomerCode().equals(dealerRevisedAnnualSalesModel.getCustomerCode()) && monthWiseAnnualTargetModel.getIsAnnualSalesRevisedForDealer().equals(true)) {
                                                            monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                                            monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                                            monthWiseTargetDataList.add(monthWiseTargetData);
                                                        }
                                                    }
                                                }
                                                annualSalesMonthWiseTargetData.setMonthWiseTarget(monthWiseTargetDataList);

                                                List<SKUData> skuDataList = new ArrayList<>();
                                                if (dealerRevisedAnnualSalesModel.getListOfSkus() != null && !dealerRevisedAnnualSalesModel.getListOfSkus().isEmpty()) {
                                                    for (ProductModel sku : dealerRevisedAnnualSalesModel.getListOfSkus()) {
                                                        SKUData skuData = new SKUData();
                                                        skuData.setProductCode(sku.getCode());
                                                        skuData.setProductName(sku.getName());
                                                        if (sku.getProductSale() != null && !sku.getProductSale().isEmpty()) {
                                                            for (ProductSaleModel productSale :
                                                                    sku.getProductSale()) {
                                                                skuData.setTotalTarget(productSale.getTotalTarget());
                                                            }
                                                        }

                                                        List<MonthWiseTargetData> monthWiseTargetDataSkuList = new ArrayList<>();
                                                        List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetList = salesPlanningService.getMonthWiseSkuDetailsBeforeReview(dealerRevisedAnnualSalesModel.getCustomerCode(), skuData.getProductCode(), dealerRevisedAnnualSalesModel.getSubAreaMaster().getPk().toString(), sclUser);
                                                        for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : monthWiseAnnualTargetList) {
                                                            MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                                            if (monthWiseAnnualTargetModel.getProductCode().equals(skuData.getProductCode()) && monthWiseAnnualTargetModel.getIsAnnualSalesRevisedForDealer().equals(true)) {
                                                                monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                                                monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                                                monthWiseTargetDataSkuList.add(monthWiseTargetData);
                                                            }
                                                        }
                                                        skuData.setMonthWiseSkuTarget(monthWiseTargetDataSkuList);
                                                        skuDataList.add(skuData);
                                                    }
                                                }
                                                annualSalesMonthWiseTargetData.setSkuDataList(skuDataList);
                                                annualSalesMonthWiseTargetDataList.add(annualSalesMonthWiseTargetData);
                                            }
                                        }
                                    //}
                                }
                            }
                        }
                    } else {
                        DealerRevisedAnnualSalesModel dealerRevisedAnnualSale = salesPlanningService.fetchRecordForDealerRevisedAnnualSalesByCode(subAreaMaster.getPk().toString(), sclUser, filter);
                        if (dealerRevisedAnnualSale != null) {
                            if (dealerRevisedAnnualSale.getSubAreaMaster().equals(subAreaMaster)) {
                                if (dealerRevisedAnnualSale.getIsExistingDealerRevisedForReview() != null && dealerRevisedAnnualSale.getIsNewDealerOnboarded() != null && dealerRevisedAnnualSale.getIsNewDealerOnboarded().equals(false) && dealerRevisedAnnualSale.getIsExistingDealerRevisedForReview().equals(false)) {
                                    AnnualSalesMonthWiseTargetData data = new AnnualSalesMonthWiseTargetData();
                                    data.setCustomerCode(dealerRevisedAnnualSale.getCustomerCode());
                                    data.setCustomerName(dealerRevisedAnnualSale.getCustomerName());
                                    data.setCustomerPotential(dealerRevisedAnnualSale.getCustomerPotential());
                                    data.setTotalTarget(dealerRevisedAnnualSale.getTotalTarget());
                                    List<MonthWiseTargetData> monthWiseTargetDataList = new ArrayList<>();
                                    if (dealerRevisedAnnualSale.getMonthWiseAnnualTarget() != null && !dealerRevisedAnnualSale.getMonthWiseAnnualTarget().isEmpty()) {
                                        for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : dealerRevisedAnnualSale.getMonthWiseAnnualTarget()) {
                                            if (monthWiseAnnualTargetModel.getCustomerCode().equals(dealerRevisedAnnualSale.getCustomerCode())) {
                                                MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                                monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                                monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                                monthWiseTargetDataList.add(monthWiseTargetData);
                                            }
                                        }
                                    }
                                    List<SKUData> skuDataList = new ArrayList<>();
                                    if (dealerRevisedAnnualSale.getListOfSkus() != null && !dealerRevisedAnnualSale.getListOfSkus().isEmpty()) {
                                        for (ProductModel sku : dealerRevisedAnnualSale.getListOfSkus()) {
                                            SKUData skuData = new SKUData();
                                            skuData.setProductCode(sku.getCode());
                                            skuData.setProductName(sku.getName());
                                            if (sku.getProductSale() != null && sku.getProductSale().isEmpty())
                                                for (ProductSaleModel productSaleModel : sku.getProductSale()) {
                                                    skuData.setCySales(productSaleModel.getCySales());
                                                    skuData.setTotalTarget(productSaleModel.getTotalTarget());
                                                }
                                            List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetList = salesPlanningService.getMonthWiseAnnualTargetDetailsForDealerRevised(dealerRevisedAnnualSale.getCustomerCode(), skuData.getProductCode(), dealerRevisedAnnualSale.getSubAreaMaster().getPk().toString());
                                            List<MonthWiseTargetData> monthWiseSkuTargetDataList = new ArrayList<>();
                                            if (monthWiseAnnualTargetList != null && !monthWiseAnnualTargetList.isEmpty()) {
                                                for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : monthWiseAnnualTargetList) {
                                                    if (monthWiseAnnualTargetModel.getCustomerCode().equals(dealerRevisedAnnualSale.getCustomerCode()) &&
                                                            monthWiseAnnualTargetModel.getProductCode().equals(sku.getCode())) {
                                                        MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                                        monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                                        monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                                        monthWiseSkuTargetDataList.add(monthWiseTargetData);
                                                    }
                                                }
                                            }
                                            skuData.setMonthWiseSkuTarget(monthWiseSkuTargetDataList);
                                            skuDataList.add(skuData);
                                        }
                                    }
                                    data.setMonthWiseTarget(monthWiseTargetDataList);
                                    data.setSkuDataList(skuDataList);
                                    annualSalesMonthWiseTargetDataList.add(data);
                                }
                            }
                        }
                    }
                }
            }
    }
        
        List<String> skuListForFinalizedTargets = salesPlanningDao.getSkuListForFinalizedTargets(subAreaMaster.getPk().toString());
        double totalTarget = 0.0;
        if(skuListForFinalizedTargets!=null && !skuListForFinalizedTargets.isEmpty())
        {
            for (String skus : skuListForFinalizedTargets) {
                double totalSkuTarget = 0.0;
                SkuWiseMonthlyTargetData skuWiseMonthlyTargetData = new SkuWiseMonthlyTargetData();
                CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
                ProductModel product = productService.getProductForCode(catalogVersion, skus);
                skuWiseMonthlyTargetData.setProductCode(product.getCode());
                skuWiseMonthlyTargetData.setProductName(product.getName());
                List<SkuTotalMonthlyTargetData> skuTotalMonthlyTargetDataList = new ArrayList<>();
                List<List<Object>> monthwiseSkuTargetsForSubarea = salesPlanningDao.getMonthwiseSkuTargetsForSubareaTsm(product.getCode(), subAreaMaster.getPk().toString());
                if(monthwiseSkuTargetsForSubarea!=null && !monthwiseSkuTargetsForSubarea.isEmpty())
                {
                    totalSkuTarget += monthwiseSkuTargetsForSubarea.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2-> (double) objects2.get(0)).sum();
                    for (List<Object> objects : monthwiseSkuTargetsForSubarea) {
                       double monthTarget= (double) objects.get(0);
                       String monthYear = (String) objects.get(1);
                       SkuTotalMonthlyTargetData skuTotalMonthlyTargetData = new SkuTotalMonthlyTargetData();
                       skuTotalMonthlyTargetData.setMonthYear(monthYear);
                       skuTotalMonthlyTargetData.setTotalTargetForMonth(monthTarget);
                       skuTotalMonthlyTargetDataList.add(skuTotalMonthlyTargetData);
                    }
                    skuWiseMonthlyTargetData.setSkuMonthWiseTotalTargets(skuTotalMonthlyTargetDataList);
                }

                skuWiseMonthlyTargetData.setTotalTargetForSku(totalSkuTarget);
                skuWiseMonthlyTargetDataList.add(skuWiseMonthlyTargetData);
            }
        }

        list.setTotalTargetForAllSku(skuWiseMonthlyTargetDataList);
        List<TotalMonthlyTargetData> totalMonthlyTargetDataList = new ArrayList<>();
        List<List<Object>> monthwiseTargetsForSubarea = salesPlanningDao.getMonthwiseTargetsForSubarea(subAreaMaster.getPk().toString());
        if(monthwiseTargetsForSubarea!=null && !monthwiseTargetsForSubarea.isEmpty())
        {
            totalTarget += monthwiseTargetsForSubarea.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2-> (double) objects2.get(0)).sum();

            for (List<Object> objects : monthwiseTargetsForSubarea) {
                double monthTarget= (double) objects.get(0);
                String monthYear = (String) objects.get(1);
                TotalMonthlyTargetData totalMonthlyTargetData = new TotalMonthlyTargetData();
                totalMonthlyTargetData.setMonthYear(monthYear);
                totalMonthlyTargetData.setTotalTargetforMonth(monthTarget);
                totalMonthlyTargetDataList.add(totalMonthlyTargetData);
            }

        }
        list.setTotalTargetForAllDealers(totalTarget);
        list.setTotalTargetForAllDealersMonthWise(totalMonthlyTargetDataList);
        if (subAreaMaster != null)
            list.setSubArea(subAreaMaster.getTaluka());
        list.setSubAreaId(subAreaMaster.getPk().toString());
        list.setAnnualSalesMonthWiseTargetData(annualSalesMonthWiseTargetDataList);
        if(district!=null)
        {
            for (DistrictMasterModel districtMasterModel : district) {
                list.setDistrictCode(districtMasterModel.getCode());
                list.setDistrictName(districtMasterModel.getName());
            }
        }
        /*if (annualSalesModel!=null && annualSalesModel.getSalesOfficer() != null)
            list.setSalesOfficer(annualSalesModel.getSalesOfficer().getUid());*/
        list.setDistrictIncharge(user.getUid());
        return list;
    }

    @Override
    public MonthlyTargetSettingSummaryData viewMonthlySalesSummaryForTSM(boolean isMonthlySummaryForReview, boolean isMonthlySummaryAfterSubmitReviewed) {
        SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        MonthlyTargetSettingSummaryData monthlyTargetSettingSummaryData = new MonthlyTargetSettingSummaryData();
        List<MonthlySubareaListData> subareaList = new ArrayList<>();
        List<String> dealerCategoryList = salesPlanningService.getDealerCategoryForSummaryPage();
        double revisedMonthSale = 0.0;
        double plannedMonthSale = 0.0;
        double sumRevisedMonthSale = 0.0;
        double sumPlannedMonthSale = 0.0;
        final double[] sumOfQuantity = {0.0};
        FilterTalukaData filterTalukaData = new FilterTalukaData();
        List<SubAreaMasterModel> taulkaForUser = territoryManagementService.getTaulkaForUser(filterTalukaData);
        if(taulkaForUser!=null && !taulkaForUser.isEmpty()) {
            MonthlySalesModel monthlySalesModelDetail = salesPlanningService.getMonthlySalesModelDetailForDO(taulkaForUser, baseSiteService.getCurrentBaseSite());
            if (monthlySalesModelDetail != null) {
                if (monthlySalesModelDetail.getRevisedTargetBySH() != null && monthlySalesModelDetail.getRevisedByShEmpCode() != null) {
                    monthlyTargetSettingSummaryData.setRevisedTargetBySHEmpCode(monthlySalesModelDetail.getRevisedByShEmpCode());
                    monthlyTargetSettingSummaryData.setRevisedTargetBySH(monthlySalesModelDetail.getRevisedTargetBySH());
                    monthlyTargetSettingSummaryData.setIsTopDownIndicatorOn(monthlySalesModelDetail.getIsTopDownIndicatorOn());
                }
            }
        }

        List<SumMonthlyTargetProductMixData> sumMonthlyTargetProductMixDataList = new ArrayList<>();
        SummaryMonthlySubareaListData summaryMonthlySubareaListData = new SummaryMonthlySubareaListData();
       
            List<List<Object>> list1=new ArrayList<>();
            if (isMonthlySummaryForReview) {
                if(taulkaForUser!=null && !taulkaForUser.isEmpty()) {
                    list1 = salesPlanningService.fetchProductMixDetailsAfterTargetSetMonthlySummaryForTSM(taulkaForUser,currentBaseSite);
                }
            }
            else if(isMonthlySummaryAfterSubmitReviewed){
                if(taulkaForUser!=null && !taulkaForUser.isEmpty()) {
                    list1 = salesPlanningService.fetchProductMixDetailsForReviewTargetMonthlySummaryForTSM(taulkaForUser,currentBaseSite);
                }
            }
                double finalSumOfQuantity = 0.0;
                if (list1 != null && !list1.isEmpty()) {
                    finalSumOfQuantity += list1.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).filter(objects -> Objects.nonNull(objects.get(0))).mapToDouble(objects2 -> (double) objects2.get(0)).sum();
                    double allProdSum = finalSumOfQuantity;
                    for (List<Object> objects : list1) {
                        SumMonthlyTargetProductMixData monthlyTargetProductMixData = new SumMonthlyTargetProductMixData();
                        double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                        String skuName = (String) objects.get(1);
                        monthlyTargetProductMixData.setSkuName(skuName);
                        monthlyTargetProductMixData.setLastYearShare(0.0);
                        double targetProductMix = Math.round((target / allProdSum) * 100);
                        if (targetProductMix != 0.0)
                            monthlyTargetProductMixData.setTargetProductMixPercentage(targetProductMix);
                        else
                            monthlyTargetProductMixData.setTargetProductMixPercentage(0.0);
                        sumMonthlyTargetProductMixDataList.add(monthlyTargetProductMixData);
                    }
                } else {
                    //chck code how to het state
                    List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(currentUser.getState());
                    if (productList != null && !productList.isEmpty()) {
                        for (String product : productList) {
                            SumMonthlyTargetProductMixData monthlyTargetProductMixData = new SumMonthlyTargetProductMixData();
                            monthlyTargetProductMixData.setLastYearShare(0.0);
                            monthlyTargetProductMixData.setTargetProductMixPercentage(0.0);
                            monthlyTargetProductMixData.setSkuName(product);
                            sumMonthlyTargetProductMixDataList.add(monthlyTargetProductMixData);
                        }
                    }
                }
        if(taulkaForUser!=null && !taulkaForUser.isEmpty()) {
                            for (SubAreaMasterModel subarea : taulkaForUser) {
                                SclUserModel sclUser = territoryManagementService.getSalesOfficerforTaluka(subarea.getTaluka(), currentBaseSite);
                                List<MonthlyTargetProductMixData> monthlyTargetProductMixDataList = new ArrayList<>();
                                List<MonthlyTargetDealerChannelMixData> monthlyTargetDealerChannelMixDataList = new ArrayList<>();
                                MonthlySubareaListData subareaListData = new MonthlySubareaListData();
                                subareaListData.setSubareaName(subarea.getTaluka());
                                subareaListData.setSubAreaId(subarea.getPk().toString());

                                //scenario 3: monthly sales summary page - planned and review tab
                                if (isMonthlySummaryForReview) {
                                        //add brand
                                    plannedMonthSale = salesPlanningService.getPlannedTargetForReviewMonthlySP(sclUser, subarea.getPk().toString(),currentBaseSite);
                                    subareaListData.setPlannedMonthSale(plannedMonthSale != 0.0 ? plannedMonthSale : 0.0);//using query annual table + last year same month
                                    revisedMonthSale = salesPlanningService.getRevisedTargetForReviewMonthlySP(sclUser, subarea.getPk().toString(),currentBaseSite);
                                    subareaListData.setRevisedMonthSale(revisedMonthSale != 0.0 ? revisedMonthSale : 0.0);
                                    //summary page - summation of all subareaproductmix
                                     sumRevisedMonthSale += revisedMonthSale;
                                     sumPlannedMonthSale += plannedMonthSale;
                                    List<List<Object>> list = salesPlanningService.fetchProductMixDetailsAfterTargetSetMonthlySummary(subarea.getPk().toString(), sclUser, currentBaseSite);
                                    double finalSumOfQuantity1 = 0.0;
                                    if (list != null && !list.isEmpty()) {
                                        finalSumOfQuantity1 += list.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2 -> (double) objects2.get(0)).sum();
                                        double allProdSum = finalSumOfQuantity1;
                                        for (List<Object> objects : list) {
                                            MonthlyTargetProductMixData monthlyTargetProductMixData = new MonthlyTargetProductMixData();
                                            double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                                            String skuName = (String) objects.get(1);
                                            monthlyTargetProductMixData.setSkuName(skuName);
                                            monthlyTargetProductMixData.setLastYearShare(0.0);
                                            double targetProductMix = Math.round((target / allProdSum) * 100);
                                            if (targetProductMix != 0.0)
                                                monthlyTargetProductMixData.setTargetProductMixPercentage(targetProductMix);
                                            else
                                                monthlyTargetProductMixData.setTargetProductMixPercentage(0.0);
                                            monthlyTargetProductMixDataList.add(monthlyTargetProductMixData);
                                        }
                                        subareaListData.setMonthlyTargetProductMix(monthlyTargetProductMixDataList);
                                    } else {
                                        List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(sclUser.getState());
                                        if (productList != null && !productList.isEmpty()) {
                                            for (String product : productList) {
                                                MonthlyTargetProductMixData monthlyTargetProductMixData = new MonthlyTargetProductMixData();
                                                monthlyTargetProductMixData.setLastYearShare(0.0);
                                                monthlyTargetProductMixData.setTargetProductMixPercentage(0.0);
                                                monthlyTargetProductMixData.setSkuName(product);
                                                monthlyTargetProductMixDataList.add(monthlyTargetProductMixData);
                                            }
                                            subareaListData.setMonthlyTargetProductMix(monthlyTargetProductMixDataList);
                                        }
                                    }

                                    //dealer channel mix
                                    double finalSumOfDealerQuantity = 0.0;
                                    List<List<Object>> dealerList = salesPlanningService.fetchDealerMixDetailsAfterTargetSetMonthlySummary(subarea.getPk().toString(), sclUser);
                                    if (dealerList != null && !dealerList.isEmpty()) {
                                        finalSumOfDealerQuantity += dealerList.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2 -> (double) objects2.get(0)).sum();
                                        double allDealerCategorySum = finalSumOfDealerQuantity;
                                        for (List<Object> objects : dealerList) {
                                            MonthlyTargetDealerChannelMixData monthlyTargetDealerChannelMixData = new MonthlyTargetDealerChannelMixData();
                                            double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                                            String dealerCategory = (String) objects.get(1);
                                            monthlyTargetDealerChannelMixData.setDealerCategory(dealerCategory);
                                            monthlyTargetDealerChannelMixData.setLastYearCounterSale(0.0);

                                            double contributionPlanPercentage = Math.round((target / allDealerCategorySum) * 100);
                                            if (contributionPlanPercentage != 0.0)
                                                monthlyTargetDealerChannelMixData.setContributionPlanPercentage(contributionPlanPercentage);
                                            else
                                                monthlyTargetDealerChannelMixData.setContributionPlanPercentage(0.0);
                                            monthlyTargetDealerChannelMixDataList.add(monthlyTargetDealerChannelMixData);
                                        }
                                        subareaListData.setMonthlyTargetDealerChannelMix(monthlyTargetDealerChannelMixDataList);
                                    } else {
                                        for (String dealerCategory : dealerCategoryList) {
                                            MonthlyTargetDealerChannelMixData monthlyTargetDealerChannelMixData = new MonthlyTargetDealerChannelMixData();
                                            monthlyTargetDealerChannelMixData.setDealerCategory(dealerCategory);
                                            monthlyTargetDealerChannelMixData.setContributionPlanPercentage(0.0);
                                            monthlyTargetDealerChannelMixData.setLastYearCounterSale(0.0);
                                            monthlyTargetDealerChannelMixDataList.add(monthlyTargetDealerChannelMixData);
                                        }
                                        subareaListData.setMonthlyTargetDealerChannelMix(monthlyTargetDealerChannelMixDataList);
                                    }
                                    MonthlySalesModel monthlySalesModel = salesPlanningService.viewMonthlySalesTargetForPlannedTab(subarea.getPk().toString(), sclUser);
                                    if(monthlySalesModel!=null)
                                    {
                                       /* if(monthlySalesModel.getRevisedByShEmpCode()!=null && monthlySalesModel.getRevisedTargetBySH()!=null)
                                        {
                                            subareaListData.setIsTopDownIndicatorOn(true);
                                            subareaListData.setRevisedTargetBySH(monthlySalesModel.getRevisedTargetBySH());
                                            subareaListData.setRevisedBy(monthlySalesModel.getRevisedByShEmpCode());
                                            subareaListData.setCommentsForRevision(monthlySalesModel.getCommentsForRevision()!=null?monthlySalesModel.getCommentsForRevision():"");
                                        }else {*/
                                            subareaListData.setIsTopDownIndicatorOn(false);
                                            subareaListData.setRevisedTarget(monthlySalesModel.getRevisedTarget() != null ? monthlySalesModel.getRevisedTarget() : 0.0);
                                            subareaListData.setRevisedBy(monthlySalesModel.getRevisedBy() != null ? monthlySalesModel.getRevisedBy().getUid() : "");
                                            subareaListData.setCommentsForRevision(monthlySalesModel.getCommentsForRevision() != null ? monthlySalesModel.getCommentsForRevision() : "");
                                        //}
                                    }
                                }
                                //scenario 4: monthly sales summary page - after review
                                else if (isMonthlySummaryAfterSubmitReviewed) {

                                    plannedMonthSale = salesPlanningService.getPlannedTargetAfterReviewMonthlySP(sclUser, subarea.getPk().toString(),currentBaseSite);
                                    subareaListData.setPlannedMonthSale(plannedMonthSale != 0.0 ? plannedMonthSale : 0.0);//using query annual table + last year same month
                                    revisedMonthSale = salesPlanningService.getRevisedTargetAfterReviewMonthlySP(sclUser, subarea.getPk().toString(),currentBaseSite);
                                    subareaListData.setRevisedMonthSale(revisedMonthSale != 0.0 ? revisedMonthSale : 0.0);

                                    List<List<Object>> list = salesPlanningService.fetchProductMixDetailsForReviewTargetMonthlySummary(subarea.getPk().toString(), sclUser, currentBaseSite);
                                    double finalSumOfQuantity2 = 0.0;
                                    if (list != null && !list.isEmpty()) {
                                        finalSumOfQuantity2 += list.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2 -> (double) objects2.get(0)).sum();
                                        double allProdSum = finalSumOfQuantity2;
                                        for (List<Object> objects : list) {
                                            MonthlyTargetProductMixData monthlyTargetProductMixData = new MonthlyTargetProductMixData();
                                            double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                                            String skuName = (String) objects.get(1);
                                            monthlyTargetProductMixData.setSkuName(skuName);
                                            monthlyTargetProductMixData.setLastYearShare(0.0);
                                            double targetProductMix = Math.round((target / allProdSum) * 100);
                                            if (targetProductMix != 0.0)
                                                monthlyTargetProductMixData.setTargetProductMixPercentage(targetProductMix);
                                            else
                                                monthlyTargetProductMixData.setTargetProductMixPercentage(0.0);
                                            monthlyTargetProductMixDataList.add(monthlyTargetProductMixData);
                                        }
                                        subareaListData.setMonthlyTargetProductMix(monthlyTargetProductMixDataList);
                                    } else {
                                        List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(sclUser.getState());
                                        if (productList != null && !productList.isEmpty()) {
                                            for (String product : productList) {
                                                MonthlyTargetProductMixData monthlyTargetProductMixData = new MonthlyTargetProductMixData();
                                                monthlyTargetProductMixData.setLastYearShare(0.0);
                                                monthlyTargetProductMixData.setTargetProductMixPercentage(0.0);
                                                monthlyTargetProductMixData.setSkuName(product);
                                                monthlyTargetProductMixDataList.add(monthlyTargetProductMixData);
                                            }
                                            subareaListData.setMonthlyTargetProductMix(monthlyTargetProductMixDataList);
                                        }
                                    }

                                    //dealer channel mix
                                    double finalSumOfDealerQuantity = 0.0;
                                    List<List<Object>> dealerList = salesPlanningService.fetchDealerMixDetailsAfterReviewMonthlySummary(subarea.getPk().toString(), sclUser);
                                    if (dealerList != null && !dealerList.isEmpty()) {
                                        finalSumOfDealerQuantity += dealerList.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2 -> (double) objects2.get(0)).sum();
                                        double allDealerCategorySum = finalSumOfDealerQuantity;
                                        for (List<Object> objects : dealerList) {
                                            MonthlyTargetDealerChannelMixData monthlyTargetDealerChannelMixData = new MonthlyTargetDealerChannelMixData();
                                            double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                                            String dealerCategory = (String) objects.get(1);
                                            monthlyTargetDealerChannelMixData.setDealerCategory(dealerCategory);
                                            monthlyTargetDealerChannelMixData.setLastYearCounterSale(0.0);

                                            double contributionPlanPercentage = Math.round((target / allDealerCategorySum) * 100);
                                            if (contributionPlanPercentage != 0.0)
                                                monthlyTargetDealerChannelMixData.setContributionPlanPercentage(contributionPlanPercentage);
                                            else
                                                monthlyTargetDealerChannelMixData.setContributionPlanPercentage(0.0);
                                            monthlyTargetDealerChannelMixDataList.add(monthlyTargetDealerChannelMixData);
                                        }
                                        subareaListData.setMonthlyTargetDealerChannelMix(monthlyTargetDealerChannelMixDataList);
                                    } else {
                                        for (String dealerCategory : dealerCategoryList) {
                                            MonthlyTargetDealerChannelMixData monthlyTargetDealerChannelMixData = new MonthlyTargetDealerChannelMixData();
                                            monthlyTargetDealerChannelMixData.setDealerCategory(dealerCategory);
                                            monthlyTargetDealerChannelMixData.setContributionPlanPercentage(0.0);
                                            monthlyTargetDealerChannelMixData.setLastYearCounterSale(0.0);
                                            monthlyTargetDealerChannelMixDataList.add(monthlyTargetDealerChannelMixData);
                                        }
                                        subareaListData.setMonthlyTargetDealerChannelMix(monthlyTargetDealerChannelMixDataList);
                                    }

                                }
                                subareaList.add(subareaListData);
                            }
                        }
                    
        if(territoryManagementService.getCurrentDistrict()!=null && !territoryManagementService.getCurrentDistrict().isEmpty()) {
            for (DistrictMasterModel districtMasterModel : territoryManagementService.getCurrentDistrict()) {
                summaryMonthlySubareaListData.setDistrictCode(districtMasterModel.getCode());
                summaryMonthlySubareaListData.setDistrictName(districtMasterModel.getName());
                monthlyTargetSettingSummaryData.setDistrictCode(districtMasterModel.getCode());
                monthlyTargetSettingSummaryData.setDistrictName(districtMasterModel.getName());
            }
        }else{
            summaryMonthlySubareaListData.setDistrictCode("");
            summaryMonthlySubareaListData.setDistrictName("");
            monthlyTargetSettingSummaryData.setDistrictCode("");
            monthlyTargetSettingSummaryData.setDistrictName("");
        }
        ViewBucketwiseRequest request = monthwiseSummaryForTSM();
            summaryMonthlySubareaListData.setPlannedMonthSale(sumPlannedMonthSale);
            summaryMonthlySubareaListData.setRevisedMonthSale(sumRevisedMonthSale);
            summaryMonthlySubareaListData.setSumMonthlyTargetProductMix(sumMonthlyTargetProductMixDataList);
            monthlyTargetSettingSummaryData.setSummaryMonthlySubareaListData(summaryMonthlySubareaListData);
            monthlyTargetSettingSummaryData.setSubareaList(subareaList);
            monthlyTargetSettingSummaryData.setPlannedSaleMonth(findNextMonth());
            monthlyTargetSettingSummaryData.setRevisedPlanMonth(findNextMonth());
            monthlyTargetSettingSummaryData.setTotalCurrentMonthSales(0.0);
            monthlyTargetSettingSummaryData.setTotalPlannedMonthSales(0.0);
            if(request!=null) {
                monthlyTargetSettingSummaryData.setMonthTarget(request.getTotalBucket());
                monthlyTargetSettingSummaryData.setBucket1(request.getBucket1());
                monthlyTargetSettingSummaryData.setBucket2(request.getBucket2());
                monthlyTargetSettingSummaryData.setBucket3(request.getBucket3());
                monthlyTargetSettingSummaryData.setBucket3Delta(Double.valueOf(request.getDeviationPercent()));
            }
            return monthlyTargetSettingSummaryData;
        }

    @Override
    public MonthlyTargetSettingSummaryData viewMonthlySalesSummaryForRH(boolean isMonthlySummaryForReview, boolean isMonthlySummaryAfterSubmitReviewed) {
            SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        MonthlyTargetSettingSummaryData monthlyTargetSettingSummaryData = new MonthlyTargetSettingSummaryData();
            List<MonthlySubareaListData> subareaList = new ArrayList<>();
            List<String> dealerCategoryList = salesPlanningService.getDealerCategoryForSummaryPage();
            double revisedMonthSale = 0.0;
            double plannedMonthSale = 0.0;
            double sumTargetProductMix = 0.0;
            double sumLastYearShare = 0.0;
            double sumRevisedMonthSale = 0.0;
            double sumPlannedMonthSale = 0.0;
            final double[] sumOfQuantity = {0.0};
        FilterTalukaData filterTalukaData = new FilterTalukaData();
        List<SubAreaMasterModel> taulkaForUser = territoryManagementService.getTaulkaForUser(filterTalukaData);
        if(taulkaForUser!=null && !taulkaForUser.isEmpty()) {
            MonthlySalesModel monthlySalesModelDetail = salesPlanningService.getMonthlySalesModelDetailForDO(taulkaForUser, baseSiteService.getCurrentBaseSite());
            /*List<MonthlySalesModel> monthlySalesModelList = salesPlanningService.getMonthlySalesModelDetailsListForDO(taulkaForUser,baseSiteService.getCurrentBaseSite());
            if(monthlySalesModelList!=null && !monthlySalesModelList.isEmpty())
            {
                double totalRevisedTargetBySH= 0.0, totalRevisedPremiumTargetBySH=0.0, totalRevisedNonPremiumTargetBySH=0.0;
                Boolean isTopIndicatorOn=false;
                String revisedByShEmpCode="";
                String comments="";
                for (MonthlySalesModel monthlySalesModel : monthlySalesModelList) {
                    totalRevisedTargetBySH += monthlySalesModel.getRevisedTargetBySH()!=null?monthlySalesModel.getRevisedTargetBySH():0.0;
                    totalRevisedPremiumTargetBySH += monthlySalesModel.getPremiumTargetBySH()!=null?monthlySalesModel.getPremiumTargetBySH():0.0;
                    totalRevisedNonPremiumTargetBySH += monthlySalesModel.getNonPremiumTargetBySH()!=null?monthlySalesModel.getNonPremiumTargetBySH():0.0;
                    isTopIndicatorOn=monthlySalesModel.getIsTopDownIndicatorOn()!=null?monthlySalesModel.getIsTopDownIndicatorOn():isTopIndicatorOn;
                    revisedByShEmpCode=monthlySalesModel.getRevisedByShEmpCode()!=null?monthlySalesModel.getRevisedByShEmpCode():"";
                    comments=monthlySalesModel.getCommentsBySH()!=null?monthlySalesModel.getCommentsBySH():comments;
                }

                monthlyTargetSettingSummaryData.setRevisedTargetBySHEmpCode(revisedByShEmpCode);
                monthlyTargetSettingSummaryData.setRevisedTargetBySH(totalRevisedTargetBySH);
                monthlyTargetSettingSummaryData.setCommentsBySH(monthlySalesModelDetail.getCommentsBySH());
                monthlyTargetSettingSummaryData.setIsTopDownIndicatorOn(monthlySalesModelDetail.getIsTopDownIndicatorOn());
                monthlyTargetSettingSummaryData.setPremiumTargetBySH(totalRevisedPremiumTargetBySH);
                monthlyTargetSettingSummaryData.setNonPremiumTargetBySH(totalRevisedNonPremiumTargetBySH);
                monthlyTargetSettingSummaryData.setRevisedPremiumTargetSentToDOs(0.0);
                monthlyTargetSettingSummaryData.setRevisedNonPremiumTargetSentToDOs(0.0);
                monthlyTargetSettingSummaryData.setDiffOfPremiumTarget(0.0);
                monthlyTargetSettingSummaryData.setDiffOfNonPremiumTarget(0.0);

            }*/
            if (monthlySalesModelDetail != null) {
                if (monthlySalesModelDetail.getRevisedTargetBySH() != null && monthlySalesModelDetail.getRevisedByShEmpCode() != null) {
                    monthlyTargetSettingSummaryData.setRevisedTargetBySHEmpCode(monthlySalesModelDetail.getRevisedByShEmpCode());
                    monthlyTargetSettingSummaryData.setRevisedTargetBySH(monthlySalesModelDetail.getRevisedTargetBySH());
                    monthlyTargetSettingSummaryData.setCommentsBySH(monthlySalesModelDetail.getCommentsBySH());
                    monthlyTargetSettingSummaryData.setIsTopDownIndicatorOn(monthlySalesModelDetail.getIsTopDownIndicatorOn());
                    monthlyTargetSettingSummaryData.setPremiumTargetBySH(monthlySalesModelDetail.getPremiumTargetBySH());
                    monthlyTargetSettingSummaryData.setNonPremiumTargetBySH(monthlySalesModelDetail.getNonPremiumTargetBySH());
                    monthlyTargetSettingSummaryData.setRevisedPremiumTargetSentToDOs(0.0);
                    monthlyTargetSettingSummaryData.setRevisedNonPremiumTargetSentToDOs(0.0);
                    monthlyTargetSettingSummaryData.setDiffOfPremiumTarget(0.0);
                    monthlyTargetSettingSummaryData.setDiffOfNonPremiumTarget(0.0);
                }
            }
        }
        List<SumMonthlyTargetProductMixData> sumMonthlyTargetProductMixDataList = new ArrayList<>();
        SummaryMonthlySubareaListData summaryMonthlySubareaListData = new SummaryMonthlySubareaListData();
        if (currentUser.getUserType().getCode().equalsIgnoreCase("RH")) {
            if (taulkaForUser != null && !taulkaForUser.isEmpty()) {
                        List<List<Object>> list1=new ArrayList<>();
                if (isMonthlySummaryForReview) {
                    list1 = salesPlanningService.fetchProductMixDetailsAfterTargetSetMonthlySummaryForTSM(taulkaForUser, currentBaseSite);
                }   else if (isMonthlySummaryAfterSubmitReviewed) {
                     list1 = salesPlanningService.fetchProductMixDetailsForReviewTargetMonthlySummaryForTSM(taulkaForUser, currentBaseSite);
                }
                                double finalSumOfQuantity = 0.0;
                                if (list1 != null && !list1.isEmpty()) {
                                    finalSumOfQuantity += list1.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).filter(objects -> Objects.nonNull(objects.get(0))).mapToDouble(objects2 -> (double) objects2.get(0)).sum();
                                    double allProdSum = finalSumOfQuantity;
                                    for (List<Object> objects : list1) {
                                        SumMonthlyTargetProductMixData monthlyTargetProductMixData = new SumMonthlyTargetProductMixData();
                                        double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                                        String skuName = (String) objects.get(1);
                                        monthlyTargetProductMixData.setSkuName(skuName);
                                        monthlyTargetProductMixData.setLastYearShare(0.0);
                                        double targetProductMix = Math.round((target / allProdSum) * 100);
                                        sumTargetProductMix += targetProductMix;
                                        sumLastYearShare += 0.0;
                                        if (targetProductMix != 0.0)
                                            monthlyTargetProductMixData.setTargetProductMixPercentage(targetProductMix);
                                        else
                                            monthlyTargetProductMixData.setTargetProductMixPercentage(0.0);
                                        sumMonthlyTargetProductMixDataList.add(monthlyTargetProductMixData);
                                    }
                                } else {
                                    List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(currentUser.getState());
                                    if (productList != null && !productList.isEmpty()) {
                                        for (String product : productList) {
                                            SumMonthlyTargetProductMixData monthlyTargetProductMixData = new SumMonthlyTargetProductMixData();
                                            monthlyTargetProductMixData.setLastYearShare(0.0);
                                            monthlyTargetProductMixData.setTargetProductMixPercentage(0.0);
                                            monthlyTargetProductMixData.setSkuName(product);
                                            sumMonthlyTargetProductMixDataList.add(monthlyTargetProductMixData);
                                        }
                                    }
                                }
                            }

                        if ( taulkaForUser!= null && !taulkaForUser.isEmpty()) {
                            for (SubAreaMasterModel subarea : taulkaForUser) {
                                SclUserModel sclUser = territoryManagementService.getSalesOfficerforTaluka(subarea.getTaluka(), currentBaseSite);
                                List<MonthlyTargetProductMixData> monthlyTargetProductMixDataList = new ArrayList<>();
                                List<MonthlyTargetDealerChannelMixData> monthlyTargetDealerChannelMixDataList = new ArrayList<>();
                                MonthlySubareaListData subareaListData = new MonthlySubareaListData();
                                subareaListData.setSubareaName(subarea.getTaluka());
                                subareaListData.setSubAreaId(subarea.getPk().toString());
                                if(sclUser!=null) {
                                    //scenario 3: monthly sales summary page - planned and review tab
                                    if (isMonthlySummaryForReview) {

                                        plannedMonthSale = salesPlanningService.getPlannedTargetForReviewMonthlySP(sclUser, subarea.getPk().toString(), currentBaseSite);
                                        subareaListData.setPlannedMonthSale(plannedMonthSale != 0.0 ? plannedMonthSale : 0.0);//using query annual table + last year same month
                                        revisedMonthSale = salesPlanningService.getRevisedTargetForReviewMonthlySP(sclUser, subarea.getPk().toString(), currentBaseSite);
                                        subareaListData.setRevisedMonthSale(revisedMonthSale != 0.0 ? revisedMonthSale : 0.0);
                                        //summary page - summation of all subareaproductmix
                                        sumRevisedMonthSale += revisedMonthSale;
                                        sumPlannedMonthSale += plannedMonthSale;
                                        List<List<Object>> list = salesPlanningService.fetchProductMixDetailsAfterTargetSetMonthlySummary(subarea.getPk().toString(), sclUser, currentBaseSite);
                                        double finalSumOfQuantity1 = 0.0;
                                        if (list != null && !list.isEmpty()) {
                                            finalSumOfQuantity1 += list.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2 -> (double) objects2.get(0)).sum();
                                            double allProdSum = finalSumOfQuantity1;
                                            for (List<Object> objects : list) {
                                                MonthlyTargetProductMixData monthlyTargetProductMixData = new MonthlyTargetProductMixData();
                                                double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                                                String skuName = (String) objects.get(1);
                                                monthlyTargetProductMixData.setSkuName(skuName);
                                                monthlyTargetProductMixData.setLastYearShare(0.0);
                                                double targetProductMix = Math.round((target / allProdSum) * 100);
                                                if (targetProductMix != 0.0)
                                                    monthlyTargetProductMixData.setTargetProductMixPercentage(targetProductMix);
                                                else
                                                    monthlyTargetProductMixData.setTargetProductMixPercentage(0.0);
                                                monthlyTargetProductMixDataList.add(monthlyTargetProductMixData);
                                            }
                                            subareaListData.setMonthlyTargetProductMix(monthlyTargetProductMixDataList);
                                        } else {
                                            List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(sclUser.getState());
                                            if (productList != null && !productList.isEmpty()) {
                                                for (String product : productList) {
                                                    MonthlyTargetProductMixData monthlyTargetProductMixData = new MonthlyTargetProductMixData();
                                                    monthlyTargetProductMixData.setLastYearShare(0.0);
                                                    monthlyTargetProductMixData.setTargetProductMixPercentage(0.0);
                                                    monthlyTargetProductMixData.setSkuName(product);
                                                    monthlyTargetProductMixDataList.add(monthlyTargetProductMixData);
                                                }
                                                subareaListData.setMonthlyTargetProductMix(monthlyTargetProductMixDataList);
                                            }
                                        }

                                        //dealer channel mix
                                        double finalSumOfDealerQuantity = 0.0;
                                        List<List<Object>> dealerList = salesPlanningService.fetchDealerMixDetailsAfterTargetSetMonthlySummary(subarea.getPk().toString(), sclUser);
                                        if (dealerList != null && !dealerList.isEmpty()) {
                                            finalSumOfDealerQuantity += dealerList.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2 -> (double) objects2.get(0)).sum();
                                            double allDealerCategorySum = finalSumOfDealerQuantity;
                                            for (List<Object> objects : dealerList) {
                                                MonthlyTargetDealerChannelMixData monthlyTargetDealerChannelMixData = new MonthlyTargetDealerChannelMixData();
                                                double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                                                String dealerCategory = (String) objects.get(1);
                                                monthlyTargetDealerChannelMixData.setDealerCategory(dealerCategory);
                                                monthlyTargetDealerChannelMixData.setLastYearCounterSale(0.0);

                                                double contributionPlanPercentage = Math.round((target / allDealerCategorySum) * 100);
                                                if (contributionPlanPercentage != 0.0)
                                                    monthlyTargetDealerChannelMixData.setContributionPlanPercentage(contributionPlanPercentage);
                                                else
                                                    monthlyTargetDealerChannelMixData.setContributionPlanPercentage(0.0);
                                                monthlyTargetDealerChannelMixDataList.add(monthlyTargetDealerChannelMixData);
                                            }
                                            subareaListData.setMonthlyTargetDealerChannelMix(monthlyTargetDealerChannelMixDataList);
                                        } else {
                                            for (String dealerCategory : dealerCategoryList) {
                                                MonthlyTargetDealerChannelMixData monthlyTargetDealerChannelMixData = new MonthlyTargetDealerChannelMixData();
                                                monthlyTargetDealerChannelMixData.setDealerCategory(dealerCategory);
                                                monthlyTargetDealerChannelMixData.setContributionPlanPercentage(0.0);
                                                monthlyTargetDealerChannelMixData.setLastYearCounterSale(0.0);
                                                monthlyTargetDealerChannelMixDataList.add(monthlyTargetDealerChannelMixData);
                                            }
                                            subareaListData.setMonthlyTargetDealerChannelMix(monthlyTargetDealerChannelMixDataList);
                                        }
                                        //added newly after modification
                                        MonthlySalesModel monthlySalesModel = salesPlanningService.viewMonthlySalesTargetForPlannedTab(subarea.getPk().toString(), sclUser);
                                        if(monthlySalesModel!=null)
                                        {
                                           if(monthlySalesModel.getRevisedByShEmpCode()!=null && monthlySalesModel.getRevisedTargetBySH()!=null) {
                                               subareaListData.setIsTopDownIndicatorOn(true);
                                               subareaListData.setRevisedTargetBySH(monthlySalesModel.getRevisedTargetBySH());
                                               subareaListData.setRevisedBy(monthlySalesModel.getRevisedByShEmpCode());
                                               subareaListData.setPremiumTargetBySH(monthlySalesModel.getPremiumTargetBySH()!=null?monthlySalesModel.getPremiumTargetBySH():0.0);
                                               subareaListData.setNonPremiumTargetBySH(monthlySalesModel.getNonPremiumTargetBySH()!=null?monthlySalesModel.getNonPremiumTargetBySH():0.0);
                                               subareaListData.setRevisedPremiumTargetSentToDO(monthlySalesModel.getRevisedPremiumTargetSentToDO()!=null?monthlySalesModel.getRevisedPremiumTargetSentToDO():0.0);
                                               subareaListData.setRevisedNonPremiumTargetSentToDO(monthlySalesModel.getRevisedNonPremiumTargetSentToDO()!=null?monthlySalesModel.getRevisedNonPremiumTargetSentToDO():0.0);
                                               subareaListData.setRevisedTargetSentToDO(monthlySalesModel.getRevisedTargetSendToDO()!=null?monthlySalesModel.getRevisedTargetSendToDO():0.0);
                                           }
                                            else {
                                                subareaListData.setIsTopDownIndicatorOn(false);
                                                subareaListData.setRevisedTarget(monthlySalesModel.getRevisedTarget() != null ? monthlySalesModel.getRevisedTarget() : 0.0);
                                                subareaListData.setRevisedBy(monthlySalesModel.getRevisedBy() != null ? monthlySalesModel.getRevisedBy().getUid() : "");
                                                subareaListData.setCommentsForRevision(monthlySalesModel.getCommentsForRevision() != null ? monthlySalesModel.getCommentsForRevision() : "");
                                            }
                                        }
                                    }
                                    //scenario 4: monthly sales summary page - after review
                                    else if (isMonthlySummaryAfterSubmitReviewed) {

                                        plannedMonthSale = salesPlanningService.getPlannedTargetAfterReviewMonthlySP(sclUser, subarea.getPk().toString(), currentBaseSite);
                                        subareaListData.setPlannedMonthSale(plannedMonthSale != 0.0 ? plannedMonthSale : 0.0);//using query annual table + last year same month
                                        revisedMonthSale = salesPlanningService.getRevisedTargetAfterReviewMonthlySP(sclUser, subarea.getPk().toString(), currentBaseSite);
                                        subareaListData.setRevisedMonthSale(revisedMonthSale != 0.0 ? revisedMonthSale : 0.0);

                                        List<List<Object>> list = salesPlanningService.fetchProductMixDetailsForReviewTargetMonthlySummary(subarea.getPk().toString(), sclUser, currentBaseSite);
                                        double finalSumOfQuantity2 = 0.0;
                                        if (list != null && !list.isEmpty()) {
                                            finalSumOfQuantity2 += list.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2 -> (double) objects2.get(0)).sum();
                                            double allProdSum = finalSumOfQuantity2;
                                            for (List<Object> objects : list) {
                                                MonthlyTargetProductMixData monthlyTargetProductMixData = new MonthlyTargetProductMixData();
                                                double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                                                String skuName = (String) objects.get(1);
                                                monthlyTargetProductMixData.setSkuName(skuName);
                                                monthlyTargetProductMixData.setLastYearShare(0.0);
                                                double targetProductMix = Math.round((target / allProdSum) * 100);
                                                if (targetProductMix != 0.0)
                                                    monthlyTargetProductMixData.setTargetProductMixPercentage(targetProductMix);
                                                else
                                                    monthlyTargetProductMixData.setTargetProductMixPercentage(0.0);
                                                monthlyTargetProductMixDataList.add(monthlyTargetProductMixData);
                                            }
                                            subareaListData.setMonthlyTargetProductMix(monthlyTargetProductMixDataList);
                                        } else {
                                            List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(sclUser.getState());
                                            if (productList != null && !productList.isEmpty()) {
                                                for (String product : productList) {
                                                    MonthlyTargetProductMixData monthlyTargetProductMixData = new MonthlyTargetProductMixData();
                                                    monthlyTargetProductMixData.setLastYearShare(0.0);
                                                    monthlyTargetProductMixData.setTargetProductMixPercentage(0.0);
                                                    monthlyTargetProductMixData.setSkuName(product);
                                                    monthlyTargetProductMixDataList.add(monthlyTargetProductMixData);
                                                }
                                                subareaListData.setMonthlyTargetProductMix(monthlyTargetProductMixDataList);
                                            }
                                        }

                                        //dealer channel mix
                                        double finalSumOfDealerQuantity = 0.0;
                                        List<List<Object>> dealerList = salesPlanningService.fetchDealerMixDetailsAfterReviewMonthlySummary(subarea.getPk().toString(), sclUser);
                                        if (dealerList != null && !dealerList.isEmpty()) {
                                            finalSumOfDealerQuantity += dealerList.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2 -> (double) objects2.get(0)).sum();
                                            double allDealerCategorySum = finalSumOfDealerQuantity;
                                            for (List<Object> objects : dealerList) {
                                                MonthlyTargetDealerChannelMixData monthlyTargetDealerChannelMixData = new MonthlyTargetDealerChannelMixData();
                                                double target = (double) objects.get(0) != 0.0 ? (double) objects.get(0) : 0.0;
                                                String dealerCategory = (String) objects.get(1);
                                                monthlyTargetDealerChannelMixData.setDealerCategory(dealerCategory);
                                                monthlyTargetDealerChannelMixData.setLastYearCounterSale(0.0);

                                                double contributionPlanPercentage = Math.round((target / allDealerCategorySum) * 100);
                                                if (contributionPlanPercentage != 0.0)
                                                    monthlyTargetDealerChannelMixData.setContributionPlanPercentage(contributionPlanPercentage);
                                                else
                                                    monthlyTargetDealerChannelMixData.setContributionPlanPercentage(0.0);
                                                monthlyTargetDealerChannelMixDataList.add(monthlyTargetDealerChannelMixData);
                                            }
                                            subareaListData.setMonthlyTargetDealerChannelMix(monthlyTargetDealerChannelMixDataList);
                                        } else {
                                            for (String dealerCategory : dealerCategoryList) {
                                                MonthlyTargetDealerChannelMixData monthlyTargetDealerChannelMixData = new MonthlyTargetDealerChannelMixData();
                                                monthlyTargetDealerChannelMixData.setDealerCategory(dealerCategory);
                                                monthlyTargetDealerChannelMixData.setContributionPlanPercentage(0.0);
                                                monthlyTargetDealerChannelMixData.setLastYearCounterSale(0.0);
                                                monthlyTargetDealerChannelMixDataList.add(monthlyTargetDealerChannelMixData);
                                            }
                                            subareaListData.setMonthlyTargetDealerChannelMix(monthlyTargetDealerChannelMixDataList);
                                        }

                                    }
                                subareaList.add(subareaListData);
                                }
                            }
                        }
            }
        if(territoryManagementService.getCurrentRegion()!=null && !territoryManagementService.getCurrentRegion().isEmpty()) {
            for (RegionMasterModel regionMasterModel : territoryManagementService.getCurrentRegion()) {
                summaryMonthlySubareaListData.setDistrictCode(regionMasterModel.getCode());
                summaryMonthlySubareaListData.setDistrictName(regionMasterModel.getName());
                monthlyTargetSettingSummaryData.setDistrictCode(regionMasterModel.getCode());
                monthlyTargetSettingSummaryData.setDistrictName(regionMasterModel.getName());
            }
        }else{
            summaryMonthlySubareaListData.setDistrictCode("");
            summaryMonthlySubareaListData.setDistrictName("");
            monthlyTargetSettingSummaryData.setDistrictCode("");
            monthlyTargetSettingSummaryData.setDistrictName("");
        }
            summaryMonthlySubareaListData.setPlannedMonthSale(sumPlannedMonthSale);
            summaryMonthlySubareaListData.setRevisedMonthSale(sumRevisedMonthSale);
            summaryMonthlySubareaListData.setSumMonthlyTargetProductMix(sumMonthlyTargetProductMixDataList);
            monthlyTargetSettingSummaryData.setSummaryMonthlySubareaListData(summaryMonthlySubareaListData);
            monthlyTargetSettingSummaryData.setSubareaList(subareaList);
            monthlyTargetSettingSummaryData.setPlannedSaleMonth(findNextMonth());
            monthlyTargetSettingSummaryData.setRevisedPlanMonth(findNextMonth());
            monthlyTargetSettingSummaryData.setTotalCurrentMonthSales(0.0);
            monthlyTargetSettingSummaryData.setTotalPlannedMonthSales(0.0);
            return monthlyTargetSettingSummaryData;
        }

    @Override
    public AnnualSalesReviewListData reviewAnnualSalesMonthwiseTargetsForRH(String district, String filter) {
        SclUserModel sclUser = (SclUserModel) getUserService().getCurrentUser();
        AnnualSalesReviewListData annualSalesReviewListData = new AnnualSalesReviewListData();
        List<TotalMonthlyTargetData> totalMonthlyTargetDataList = new ArrayList<>();
        List<AnnualSalesReviewData> annualSalesReviewDataList = new ArrayList<>();
        FilterDistrictData filterDistrictData = new FilterDistrictData();
        filterDistrictData.setDistrictName(district);
        List<DistrictMasterModel> districtForUser = territoryManagementService.getDistrictForUser(filterDistrictData);
        if(districtForUser!=null && !districtForUser.isEmpty()) {
            for (DistrictMasterModel districtMasterModel : districtForUser) {
                List<AnnualSalesModel> annualSalesModelList = salesPlanningDao.getAnnualSalesModelDetailsForDistrict(findNextFinancialYear(), districtMasterModel, baseSiteService.getCurrentBaseSite());
                if(annualSalesModelList!=null && !annualSalesModelList.isEmpty())
                {
                    for (AnnualSalesModel annualSalesModel : annualSalesModelList) {
                        if(annualSalesModel.getActionPerformed()!=null && annualSalesModel.getActionPerformed().equals(WorkflowActions.APPROVED))
                        {
                            //doubt- new Condition
                            if(annualSalesModel.getActionPerformedBy()!=null && annualSalesModel.getActionPerformedBy().equals(sclUser)){
                                annualSalesReviewListData.setIsTargetApproved(true);
                                annualSalesReviewListData.setIsTargetRevised(true);
                                LOG.info("Directly Approved by RH");
                            }
                            //(revised by - rh revised target - not null actionperformedBy -tsm dealerPlannedTarget!=reviedTarget)
                             if(annualSalesModel.getActionPerformedBy()!=null && annualSalesModel.getActionPerformedBy().getUserType().getCode().equalsIgnoreCase("TSM")
                            && annualSalesModel.getRevisedBy()!=null && annualSalesModel.getRevisedTarget()!=null && annualSalesModel.getRevisedBy().equals(sclUser) &&
                                !(annualSalesModel.getDealerPlannedTotalPlanSales().equals(annualSalesModel.getRevisedTarget()))){
                                annualSalesReviewListData.setIsTargetApproved(true);
                                annualSalesReviewListData.setIsTargetRevised(true);
                                LOG.info("Approved By TSM and Revised By RH");
                            }
                             if(annualSalesModel.getActionPerformedBy()!=null && annualSalesModel.getActionPerformedBy().getUserType().getCode().equalsIgnoreCase("TSM") &&
                                    annualSalesModel.getDealerPlannedTotalPlanSales().equals(annualSalesModel.getRevisedTarget()) ) {
                                annualSalesReviewListData.setIsTargetApproved(false);//enable
                                annualSalesReviewListData.setIsTargetRevised(false);
                                LOG.info("Approved by TSM and send to RH");
                            }
                            if(annualSalesModel.getActionPerformedBy()!=null && annualSalesModel.getActionPerformedBy().getUserType().getCode().equalsIgnoreCase("TSM") &&
                                    annualSalesModel.getDealerPlannedTotalPlanSales().equals(annualSalesModel.getRevisedTarget()) && annualSalesModel.getRevisedBy()!=null &&
                                    !(annualSalesModel.getRevisedBy().equals(sclUser))) {
                                annualSalesReviewListData.setIsTargetApproved(true);//disable
                                annualSalesReviewListData.setIsTargetRevised(true);
                                LOG.info("Approved by TSM and send to RH");
                            }
                            if(annualSalesModel.getRevisedByShEmpCode()!=null && annualSalesModel.getRevisedTargetBySH()!=null
                                    && annualSalesModel.getIsTopDownIndicatorOn()!=null && annualSalesModel.getIsTopDownIndicatorOn()){
                                annualSalesReviewListData.setIsTargetApprovedForRHTopDown(true);
                                annualSalesReviewListData.setIsTargetRevisedForRHTopDown(false);
                                annualSalesReviewListData.setIsTopDownIndicatorOn(true);
                                LOG.info("topdown annual - revised by SH - approved - disabled and target revision enabled");
                            }


                            AnnualSalesReviewData annualSalesReviewData = new AnnualSalesReviewData();
                            annualSalesReviewData.setDistrictInchargeUid(annualSalesModel.getActionPerformedBy()!=null? annualSalesModel.getActionPerformedBy().getUid() : "");
                            if(annualSalesModel.getSubAreaMaster()!=null) {

                                SubAreaMasterModel subAreaMasterModel = annualSalesModel.getSubAreaMaster();
                                // if(annualSalesModel.getSubAreaMasterList()!=null && !annualSalesModel.getSubAreaMasterList().isEmpty())
                                //  for (SubAreaMasterModel subAreaMasterModel : annualSalesModel.getSubAreaMasterList()) {
                                annualSalesReviewData.setSubAreaId(subAreaMasterModel.getPk().toString());
                                annualSalesReviewData.setSubAreaName(subAreaMasterModel.getTaluka());

                                double totalTargetForSubarea = 0.0;
                                //List<List<Object>> monthwiseTargets = salesPlanningDao.getMonthwiseTargetsForTerritory(subAreaMasterModel, districtMasterModel);
                                List<List<Object>> monthwiseTargets = salesPlanningDao.getMonthwiseTargetsForSubarea(subAreaMasterModel.getPk().toString());
                                if (monthwiseTargets != null && !monthwiseTargets.isEmpty()) {
                                    totalTargetForSubarea += monthwiseTargets.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2 -> (double) objects2.get(0)).sum();
                                    for (List<Object> object : monthwiseTargets) {
                                        double monthTarget = (double) object.get(0);
                                        String monthYear = (String) object.get(1);
                                        TotalMonthlyTargetData totalMonthlyTargetData = new TotalMonthlyTargetData();
                                        totalMonthlyTargetData.setTotalTargetforMonth(monthTarget);
                                        totalMonthlyTargetData.setMonthYear(monthYear);
                                        totalMonthlyTargetDataList.add(totalMonthlyTargetData);
                                    }
                                }
                                annualSalesReviewData.setTotalTargetForSubarea(totalTargetForSubarea);
                                annualSalesReviewData.setTotalMonthWiseTargetForSubarea(totalMonthlyTargetDataList);


                                List<SkuWiseMonthlyTargetData> skuWiseMonthlyTargetDataList = new ArrayList<>();
                                List<String> skuList = salesPlanningDao.getSkuListForTerritory(subAreaMasterModel, districtMasterModel);
                                if (skuList != null && !skuList.isEmpty()) {
                                    for (String sku : skuList) {
                                        double totalTargetForSku = 0.0;
                                        SkuWiseMonthlyTargetData skuWiseMonthlyTargetData = new SkuWiseMonthlyTargetData();
                                        CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
                                        ProductModel product = productService.getProductForCode(catalogVersion, sku);
                                        skuWiseMonthlyTargetData.setProductCode(product.getCode());
                                        skuWiseMonthlyTargetData.setProductName(product.getName());
                                        List<SkuTotalMonthlyTargetData> skuTotalMonthlyTargetDataList = new ArrayList<>();
                                        //List<List<Object>> monthwiseSkuTargets = salesPlanningDao.getMonthwiseSkuTargetsForTerritory(sku, subAreaMasterModel, districtMasterModel);
                                        List<List<Object>> monthwiseSkuTargets = salesPlanningDao.getMonthwiseSkuTargetsForSubarea(sku, subAreaMasterModel.getPk().toString());
                                        if (monthwiseSkuTargets != null && !monthwiseSkuTargets.isEmpty()) {
                                            totalTargetForSku += monthwiseSkuTargets.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2 -> (double) objects2.get(0)).sum();

                                            for (List<Object> object : monthwiseTargets) {
                                                double monthTarget = (double) object.get(0);
                                                String monthYear = (String) object.get(1);
                                                SkuTotalMonthlyTargetData skuTotalMonthlyTargetData = new SkuTotalMonthlyTargetData();
                                                skuTotalMonthlyTargetData.setTotalTargetForMonth(monthTarget);
                                                skuTotalMonthlyTargetData.setMonthYear(monthYear);
                                                skuTotalMonthlyTargetDataList.add(skuTotalMonthlyTargetData);
                                            }
                                        }
                                        skuWiseMonthlyTargetData.setSkuMonthWiseTotalTargets(skuTotalMonthlyTargetDataList);
                                        skuWiseMonthlyTargetData.setTotalTargetForSku(totalTargetForSku);
                                        skuWiseMonthlyTargetDataList.add(skuWiseMonthlyTargetData);
                                    }

                                }
                                annualSalesReviewData.setSkuWiseTargets(skuWiseMonthlyTargetDataList);
                                annualSalesReviewDataList.add(annualSalesReviewData);
                            }
                          //  }
                        }
                    }
                }
                annualSalesReviewListData.setSalesReviewData(annualSalesReviewDataList);
                //set for district level
                List<SkuWiseMonthlyTargetData> skuWiseMonthlyTargetDataList = new ArrayList<>();
                List<String> skuListForFinalizedTargets = salesPlanningDao.getSkuListForFinalizedTargets(districtMasterModel);
                 double totalTarget = 0.0;
                if(skuListForFinalizedTargets!=null && !skuListForFinalizedTargets.isEmpty())
                {
                    for (String skus : skuListForFinalizedTargets) {
                        double totalSkuTarget = 0.0;
                        SkuWiseMonthlyTargetData skuWiseMonthlyTargetData = new SkuWiseMonthlyTargetData();
                        CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
                        ProductModel product = productService.getProductForCode(catalogVersion, skus);
                        skuWiseMonthlyTargetData.setProductCode(product.getCode());
                        skuWiseMonthlyTargetData.setProductName(product.getName());
                        List<SkuTotalMonthlyTargetData> skuTotalMonthlyTargetDataList = new ArrayList<>();
                        List<List<Object>> monthwiseSkuTargetsForSubarea = salesPlanningDao.getMonthwiseSkuTargetsForDistrict(product.getCode(), districtMasterModel);
                        if(monthwiseSkuTargetsForSubarea!=null && !monthwiseSkuTargetsForSubarea.isEmpty())
                        {
                            totalSkuTarget += monthwiseSkuTargetsForSubarea.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2-> (double) objects2.get(0)).sum();
                            for (List<Object> objects : monthwiseSkuTargetsForSubarea) {
                                double monthTarget= (double) objects.get(0);
                                String monthYear = (String) objects.get(1);
                                SkuTotalMonthlyTargetData skuTotalMonthlyTargetData = new SkuTotalMonthlyTargetData();
                                skuTotalMonthlyTargetData.setMonthYear(monthYear);
                                skuTotalMonthlyTargetData.setTotalTargetForMonth(monthTarget);
                                skuTotalMonthlyTargetDataList.add(skuTotalMonthlyTargetData);
                            }
                            skuWiseMonthlyTargetData.setSkuMonthWiseTotalTargets(skuTotalMonthlyTargetDataList);
                        }
                        skuWiseMonthlyTargetData.setTotalTargetForSku(totalSkuTarget);
                        skuWiseMonthlyTargetDataList.add(skuWiseMonthlyTargetData);
                    }
                }

                annualSalesReviewListData.setTotalTargetForAllSku(skuWiseMonthlyTargetDataList);
                List<TotalMonthlyTargetData> totalMonthlyTargetDataListForDist = new ArrayList<>();
                List<List<Object>> monthwiseTargetsForSubarea = salesPlanningDao.getMonthwiseTargetsForDistrict(districtMasterModel);
                if(monthwiseTargetsForSubarea!=null && !monthwiseTargetsForSubarea.isEmpty())
                {
                    totalTarget += monthwiseTargetsForSubarea.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects2-> (double) objects2.get(0)).sum();

                    for (List<Object> objects : monthwiseTargetsForSubarea) {
                        double monthTarget= (double) objects.get(0);
                        String monthYear = (String) objects.get(1);
                        TotalMonthlyTargetData totalMonthlyTargetData = new TotalMonthlyTargetData();
                        totalMonthlyTargetData.setMonthYear(monthYear);
                        totalMonthlyTargetData.setTotalTargetforMonth(monthTarget);
                        totalMonthlyTargetDataListForDist.add(totalMonthlyTargetData);
                    }

                }
                annualSalesReviewListData.setTotalTargetForAllSubarea(totalTarget);
                annualSalesReviewListData.setTotalMonthwiseTargetForAllSubarea(totalMonthlyTargetDataListForDist);
                annualSalesReviewListData.setDistrictCode(districtMasterModel.getCode());
                annualSalesReviewListData.setDistrictName(districtMasterModel.getName());
            }

        }
        Collection<RegionMasterModel> currentRegion = territoryManagementService.getCurrentRegion();
        if(currentRegion!=null && !currentRegion.isEmpty())
        {
            for (RegionMasterModel regionMasterModel : currentRegion) {
                annualSalesReviewListData.setRegionCode(regionMasterModel.getCode());
                annualSalesReviewListData.setRegionName(regionMasterModel.getName());
            }
        }
        annualSalesReviewListData.setRhUid(sclUser.getUid());
        return annualSalesReviewListData;
    }

    @Override
    public boolean sendApprovedTargetToUser(boolean isTargetSetForUser) {
        return salesPlanningService.sendApprovedTargetToUser(isTargetSetForUser);
    }

    @Override
    public SalesTargetApprovedData updateStatusForBucketApproval(ViewBucketwiseRequest viewBucketwiseRequest) {
        SalesTargetApprovedData salesTargetApprovedData = new SalesTargetApprovedData();
        if (viewBucketwiseRequest != null) {
            try {
                SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
                SclWorkflowModel sclWorkflow = null;
                MonthlySalesModel monthlySalesModel = null;
                BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
                RegionMasterModel regionMaster = null;
                Collection<RegionMasterModel> currentRegion = territoryManagementService.getCurrentRegion();
              /*  if (currentRegion != null && !currentRegion.isEmpty()) {
                    for (RegionMasterModel regionMasterModel : currentRegion) {
                        regionMaster = regionMasterModel;
                    }
                }*/
                if (currentUser.getUserType().getCode().equalsIgnoreCase("RH")) {
                   // SclUserModel userForUID = null;
                  //  if (viewBucketwiseRequest.getUserCode() != null) {
                       // userForUID = (SclUserModel) userService.getUserForUID(viewBucketwiseRequest.getUserCode());
                        monthlySalesModel = salesPlanningService.viewMonthlySalesTargetForPlannedTab(viewBucketwiseRequest.getSubAreaName(), currentUser);
                        //monthlySalesModel = salesPlanningDao.getMonthlySalesModelDetailsForRH(LocalDate.now().getMonth().plus(1).name(), String.valueOf(LocalDate.now().getYear()), regionMaster, currentBaseSite);
                        if (monthlySalesModel != null) {
                            if (monthlySalesModel.getSclWorkflow() != null) {
                                sclWorkflow = monthlySalesModel.getSclWorkflow();
                            }
                            SubAreaMasterModel subAreaMaster=territoryManagementService.getTerritoryById(viewBucketwiseRequest.getSubAreaName());
                            if (monthlySalesModel.getSubAreaMasterList().contains(subAreaMaster)) {
                                if (viewBucketwiseRequest.getApproveStatus().equals(Boolean.TRUE)) {
                                    monthlySalesModel.setActionPerformed(WorkflowActions.APPROVED);
                                    for (DealerRevisedMonthlySalesModel dealerRevisedMonthlySale : monthlySalesModel.getDealerRevisedMonthlySales()) {
                                        if(dealerRevisedMonthlySale.getSubAreaMaster().equals(subAreaMaster)){
                                            dealerRevisedMonthlySale.setActionPerformed(WorkflowActions.APPROVED);
                                            modelService.save(dealerRevisedMonthlySale);
                                        }
                                    }
                                    SclWorkflowActionModel sclWorkflowActionModel = sclWorkflowService.saveWorkflowAction(sclWorkflow, "Approval for SO Bucketwise from RH", currentBaseSite, subAreaMaster, TerritoryLevels.SUBAREA);
                                    if (sclWorkflowActionModel != null) {
                                        sclWorkflowService.updateWorkflowAction(sclWorkflowActionModel, currentUser, WorkflowActions.APPROVED, "Comments Addedfor Approval");
                                    }
                                } else if (viewBucketwiseRequest.getApproveStatus().equals(Boolean.FALSE)) {
                                    monthlySalesModel.setActionPerformed(WorkflowActions.REJECTED);
                                    for (DealerRevisedMonthlySalesModel dealerRevisedMonthlySale : monthlySalesModel.getDealerRevisedMonthlySales()) {
                                        if(dealerRevisedMonthlySale.getSubAreaMaster().equals(subAreaMaster)){
                                            dealerRevisedMonthlySale.setActionPerformed(WorkflowActions.REJECTED);
                                            modelService.save(dealerRevisedMonthlySale);
                                        }
                                    }
                                    SclWorkflowActionModel sclWorkflowActionModel = sclWorkflowService.saveWorkflowAction(sclWorkflow, "Rejected for SO Bucketwise from RH", currentBaseSite,subAreaMaster, TerritoryLevels.SUBAREA);
                                    if (sclWorkflowActionModel != null) {
                                        sclWorkflowService.updateWorkflowAction(sclWorkflowActionModel, currentUser, WorkflowActions.REJECTED, "Comments Added for Rejetion");
                                    }
                                }
                                monthlySalesModel.setActionPerformedBy(currentUser);
                                monthlySalesModel.setActionPerformedDate(new Date());
                                monthlySalesModel.setCommentsForRevision(viewBucketwiseRequest.getCommentsForStatus());
                                modelService.save(monthlySalesModel);
                            }
                        }
                   // }
                    //}
                    if (sclWorkflow != null) {
                        sclWorkflow.setStatus(WorkflowStatus.IN_PROCESS);
                        modelService.save(sclWorkflow);
                    }
                    if (monthlySalesModel != null) {
                        if (monthlySalesModel.getActionPerformedBy() != null && monthlySalesModel.getActionPerformedBy().equals(currentUser) && monthlySalesModel.getActionPerformed() != null && monthlySalesModel.getActionPerformed().equals(WorkflowActions.APPROVED)) {
                            salesTargetApprovedData.setIsTargetApproved(true);
                        } else {
                            salesTargetApprovedData.setIsTargetApproved(false);
                        }
                    } else {
                        salesTargetApprovedData.setIsTargetApproved(false);
                    }

                }
            } catch(UnknownIdentifierException e){
                String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getName() + " Occurred";
                throw new UnknownIdentifierException(errorMsg);
            }
            return salesTargetApprovedData;
        }
        else{
            salesTargetApprovedData.setIsTargetApproved(false);
            return salesTargetApprovedData;
        }
    }

    @Override
    public SalesTargetApprovedData targetSendForRevisionForMonthly(SalesRevisedTargetData salesRevisedTargetData) {
        return salesPlanningService.targetSendForRevisionForMonthly(salesRevisedTargetData);
    }

    @Override
    public SalesTargetApprovedData updateTargetStatusForApprovalMonthly(SalesApprovalData salesApprovalData) {
        return salesPlanningService.updateTargetStatusForApprovalMonthly(salesApprovalData);
    }

    public static int getMonthNumber(String monthName) {
        String[] monthNameSplit= monthName.split("-");
        DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("MMM").withLocale(Locale.ENGLISH);
        TemporalAccessor temporalAccessor = dtFormatter.parse(monthNameSplit[0]);
        return temporalAccessor.get(ChronoField.MONTH_OF_YEAR);
       // return Month.valueOf(monthName.toUpperCase()).getValue();
    }
    public static Date getFirstDateOfMonth(int month){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }

    public static Date getLastDateOfMonth(int month){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }

    String findNextFinancialYear()
    {
        LocalDate date = LocalDate.now();
        int currentYear=date.getYear();
        int fyYear=currentYear+1;
        StringBuilder f=new StringBuilder();
        return String.valueOf(f.append(String.valueOf(currentYear)).append("-").append(String.valueOf(fyYear)));
    }

    String findNextMonth()
    {
        LocalDate localDate = LocalDate.now();
        LocalDate nextMonth = localDate.plusMonths(1);
        SimpleDateFormat formatter = new SimpleDateFormat("MMM");
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = nextMonth.atStartOfDay(zone);
        String date = formatter.format(Date.from(dateTime.toInstant()));
        return date;
    }
    public SalesPlanningService getSalesPlanningService() {
        return salesPlanningService;
    }

    public void setSalesPlanningService(SalesPlanningService salesPlanningService) {
        this.salesPlanningService = salesPlanningService;
    }

    public SalesPlanningDao getSalesPlanningDao() {
        return salesPlanningDao;
    }

    public void setSalesPlanningDao(SalesPlanningDao salesPlanningDao) {
        this.salesPlanningDao = salesPlanningDao;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public BaseSiteService getBaseSiteService() {
        return baseSiteService;
    }

    public void setBaseSiteService(BaseSiteService baseSiteService) {
        this.baseSiteService = baseSiteService;
    }

    public TerritoryManagementService getTerritoryManagementService() {
        return territoryManagementService;
    }

    public void setTerritoryManagementService(TerritoryManagementService territoryManagementService) {
        this.territoryManagementService = territoryManagementService;
    }

    public RetailerSalesSummaryService getRetailerSalesSummaryService() {
        return retailerSalesSummaryService;
    }

    public void setRetailerSalesSummaryService(RetailerSalesSummaryService retailerSalesSummaryService) {
        this.retailerSalesSummaryService = retailerSalesSummaryService;
    }

    public SalesPerformanceService getSalesPerformanceService() {
        return salesPerformanceService;
    }

    public void setSalesPerformanceService(SalesPerformanceService salesPerformanceService) {
        this.salesPerformanceService = salesPerformanceService;
    }
}
