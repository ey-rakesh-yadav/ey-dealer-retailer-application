package com.scl.core.services.impl;


import com.scl.core.dao.SalesPlanningDao;
import com.scl.core.enums.*;
import com.scl.core.jalo.RetailerPlannedAnnualSalesDetails;
import com.scl.core.jalo.SclUser;
import com.scl.core.model.*;
import com.scl.core.services.SalesPlanningService;
import com.scl.core.services.TerritoryManagementService;
import com.scl.core.utility.SclProductUtility;
import com.scl.facades.data.*;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;
import de.hybris.platform.webservicescommons.dto.error.ErrorWsDTO;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

public class SalesPlanningServiceImpl implements SalesPlanningService {
    private static final String DEALER_CUSTOMER_CODE_EXISITNG = "Dealer-Customer Code is already present";
    private static final String SKU_CODE_EXISTING = "Target has been already set for SKU";
    private static final String SKU_CODE_EXISTING_FINALIZE="Target has been already finalized for SKU";
    private static final String RETAILER_CODE_EXISTING="Target has been already set for Retailer";
    private static final String RETAILER_CODE_EXISTING_FINALIZE="Target has been already finalized for Retailer";
    private static final String MONTHLY_SKU_CODE_EXISTING="Target has been set for SKU";
    private static final String ASP_BOTTOM_UP_WORKFLOW="ASPBottomUpWorkflow";
    private static final String ASP_TOP_DOWN_WORKFLOW="ASPTopDownWorkflow";
    private static final String MSP_BOTTOM_UP_WORKFLOW="MSPBottomUpWorkflow";
    private static final Logger LOG = Logger.getLogger(SalesPlanningServiceImpl.class);

    @Resource
    UserService userService;
    @Resource
    ModelService modelService;
    @Resource
    ProductService productService;
    @Resource
    SalesPlanningDao salesPlanningDao;
    @Resource
    BaseSiteService baseSiteService;
    @Autowired
    TerritoryManagementService territoryManagementService;
    @Autowired
    SclProductUtility sclProductUtility;
    @Autowired
    SclWorkflowServiceImpl sclWorkflowService;
    @Autowired
    SclSalesSummaryServiceImpl sclSalesSummaryService;

    private AnnualSalesModel validateAnnualSalesModel(SclUserModel sclUser, String subArea, BaseSiteModel baseSite){
        String financialYear = findNextFinancialYear();
        //AnnualSalesModel annualSalesModelDetails = salesPlanningDao.getAnnualSalesModelDetails(sclUser,financialYear,subArea,baseSite);
        AnnualSalesModel annualSalesModelDetails = salesPlanningDao.getAnnualSalesModelDetails1(sclUser,financialYear,subArea,baseSite);
        if(annualSalesModelDetails!=null){
            return  annualSalesModelDetails;
        }
        else{
            AnnualSalesModel annualSalesModel=modelService.create(AnnualSalesModel.class);
            return annualSalesModel;
        }
    }

    private DealerPlannedAnnualSalesModel validateDealerAnnualSalesModel(String customerCode, String subArea, SclUserModel sclUser, String nextFinancialYear){
        String financialYear = findNextFinancialYear();
        DealerPlannedAnnualSalesModel dealerPlannedAnnualSalesDetails = salesPlanningDao.findDealerDetailsByCustomerCode(customerCode,subArea,sclUser,financialYear);
        if(dealerPlannedAnnualSalesDetails!=null){
            return  dealerPlannedAnnualSalesDetails;
        }
        else{
            DealerPlannedAnnualSalesModel dealerPlannedAnnualSalesModel=modelService.create(DealerPlannedAnnualSalesModel.class);
            return dealerPlannedAnnualSalesModel;
        }
    }

    private DealerRevisedAnnualSalesModel validateDealerRevisedAnnualSalesModel(String customerCode, String subArea, SclUserModel sclUser, String financialYear){
        DealerRevisedAnnualSalesModel dealerRevisedAnnualSalesModelDetails = salesPlanningDao.findDealerRevisedDetailsByCustomerCode(customerCode,subArea,sclUser,financialYear);
        if(dealerRevisedAnnualSalesModelDetails!=null){
            return  dealerRevisedAnnualSalesModelDetails;
        }
        else{
            DealerRevisedAnnualSalesModel dealerRevisedAnnualSalesModel=modelService.create(DealerRevisedAnnualSalesModel.class);
            return dealerRevisedAnnualSalesModel;
        }
    }

    private RetailerPlannedAnnualSalesModel validateRetailerPlannedAnnualSalesModel(String customerCode, String subArea, SclUserModel sclUser, String financialYear){
        RetailerPlannedAnnualSalesModel retailerPlannedAnnualSalesModelDetails = salesPlanningDao.findRetailerDetailsByCustomerCode(customerCode,subArea,sclUser,financialYear);
        if(retailerPlannedAnnualSalesModelDetails!=null){
            return  retailerPlannedAnnualSalesModelDetails;
        }
        else{
            RetailerPlannedAnnualSalesModel retailerPlannedAnnualSalesModel=modelService.create(RetailerPlannedAnnualSalesModel.class);
            return retailerPlannedAnnualSalesModel;
        }
    }

    private RetailerRevisedAnnualSalesModel validateRetailerRevisedAnnualSalesModel(String customerCode, String subArea, SclUserModel sclUser, String financialYear){
        RetailerRevisedAnnualSalesModel retailerRevisedAnnualSalesModelDetails = salesPlanningDao.findRetailerRevisedDetailsByCustomerCode(customerCode,subArea,sclUser,financialYear);
        if(retailerRevisedAnnualSalesModelDetails!=null){
            return  retailerRevisedAnnualSalesModelDetails;
        }
        else{
            RetailerRevisedAnnualSalesModel retailerRevisedAnnualSalesModel=modelService.create(RetailerRevisedAnnualSalesModel.class);
            return retailerRevisedAnnualSalesModel;
        }
    }

    private MonthWiseAnnualTargetModel validateMonthWiseDetailsForDealerRetailer(String customerCode, String subArea, String key, String value, SclUserModel sclUser){
        MonthWiseAnnualTargetModel monthWiseAnnualTargetModelDetails = salesPlanningDao.fetchMonthWiseAnnualTargetDetailsForDealerRetailer(customerCode,subArea,key,value, sclUser);
        if(monthWiseAnnualTargetModelDetails!=null){
            return  monthWiseAnnualTargetModelDetails;
        }
        else{
            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel=modelService.create(MonthWiseAnnualTargetModel.class);
            return monthWiseAnnualTargetModel;
        }
    }

    private MonthWiseAnnualTargetModel validateMonthWiseDetailsForRetailers(String customerCode, String retailerCode, String subArea, String key, String value, SclUserModel sclUser){
        MonthWiseAnnualTargetModel monthWiseAnnualTargetModelDetails = salesPlanningDao.fetchMonthWiseAnnualTargetDetailsForRetailers(customerCode,retailerCode,subArea, key, value,sclUser);
        if(monthWiseAnnualTargetModelDetails!=null){
            return  monthWiseAnnualTargetModelDetails;
        }
        else{
            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel=modelService.create(MonthWiseAnnualTargetModel.class);
            return monthWiseAnnualTargetModel;
        }
    }

    private MonthWiseAnnualTargetModel validateMonthWiseAnnualTargetModel(String customerCode, String subArea, String key, String value, SclUserModel sclUser){
        MonthWiseAnnualTargetModel monthWiseAnnualTargetModelDetails = salesPlanningDao.fetchMonthWiseAnnualTargetDetails(customerCode,subArea, key, value,sclUser);
        if(monthWiseAnnualTargetModelDetails!=null){
            return  monthWiseAnnualTargetModelDetails;
        }
        else{
            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel=modelService.create(MonthWiseAnnualTargetModel.class);
            return monthWiseAnnualTargetModel;
        }
    }

    private MonthWiseAnnualTargetModel validateMonthWiseAnnualTargetModelForSku(String subArea, String customerCode, String productCode, String key, String value, SclUserModel sclUser) {
        MonthWiseAnnualTargetModel monthWiseAnnualTargetModelDetails = salesPlanningDao.fetchMonthWiseAnnualTargetDetailsForSku(subArea,customerCode,productCode,key,value,sclUser);
        if(monthWiseAnnualTargetModelDetails!=null){
            return  monthWiseAnnualTargetModelDetails;
        }
        else{
            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel=modelService.create(MonthWiseAnnualTargetModel.class);
            return monthWiseAnnualTargetModel;
        }
    }

    private MonthWiseAnnualTargetModel validateDealerRevisedMonthWiseSkuDetails(String subArea, String customerCode, String productCode, String monthYear, SclUserModel sclUser, boolean isAnnualSalesRevised) {
        MonthWiseAnnualTargetModel monthWiseAnnualTargetModelDetails = salesPlanningDao.fetchDealerRevisedMonthWiseSkuDetails(subArea,customerCode,productCode,monthYear,sclUser,isAnnualSalesRevised);
        if(monthWiseAnnualTargetModelDetails!=null){
            return  monthWiseAnnualTargetModelDetails;
        }
        else{
            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel=modelService.create(MonthWiseAnnualTargetModel.class);
            return monthWiseAnnualTargetModel;
        }
    }

    private ProductSaleModel validateProductSalesModel(String customerCode, String productCode, String subArea, SclUserModel sclUser) {
        ProductSaleModel productSaleModelDetails = salesPlanningDao.getSalesForSku(customerCode,productCode,subArea,sclUser);
        if(productSaleModelDetails!=null)
        {
            return productSaleModelDetails;
        }
        else
        {
            ProductSaleModel productSaleModel=modelService.create(ProductSaleModel.class);
            return productSaleModel;
        }
    }

    private ProductSaleModel validateRevisedProductSalesModel(String customerCode, String productCode, String subArea, SclUserModel sclUser, boolean isAnnualSalesRevised) {
        ProductSaleModel productSaleModelDetails = salesPlanningDao.getRevisedSalesForSku(customerCode,productCode,subArea,sclUser,isAnnualSalesRevised);
        if(productSaleModelDetails!=null)
        {
            return productSaleModelDetails;
        }
        else
        {
            ProductSaleModel productSaleModel=modelService.create(ProductSaleModel.class);
            return productSaleModel;
        }
    }

    private MonthWiseAnnualTargetModel validateDealerRevisedMonthWiseTargetModel(String customerCode, String subArea, String monthYear, SclUserModel sclUser, boolean isAnnualSalesRevised){
        MonthWiseAnnualTargetModel monthWiseAnnualTargetModelDetails = salesPlanningDao.fetchDealerRevisedMonthWiseTargetDetails(customerCode, subArea, monthYear,sclUser, isAnnualSalesRevised);
        if(monthWiseAnnualTargetModelDetails!=null){
            return  monthWiseAnnualTargetModelDetails;
        }
        else{
            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel=modelService.create(MonthWiseAnnualTargetModel.class);
            return monthWiseAnnualTargetModel;
        }
    }

    private MonthWiseAnnualTargetModel validateSelfCounterMonthWiseDetailsModel(String customerCode, String selfCounterCode, String subArea, SclUserModel sclUser, String key, String value) {
        MonthWiseAnnualTargetModel monthWiseAnnualTargetModelDetails = salesPlanningDao.fetchMonthWiseSelfCounterDetails(customerCode, selfCounterCode, subArea, sclUser, key, value);
        if(monthWiseAnnualTargetModelDetails!=null){
            return  monthWiseAnnualTargetModelDetails;
        }
        else{
            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel=modelService.create(MonthWiseAnnualTargetModel.class);
            return monthWiseAnnualTargetModel;
        }
    }

    private MonthWiseAnnualTargetModel validateRevisedSelfCounterMonthWiseDetailsModel(String customerCode, String selfCounterCode, String subArea, SclUserModel sclUser, String monthYear) {
        MonthWiseAnnualTargetModel monthWiseAnnualTargetModelDetails = salesPlanningDao.fetchRevisedMonthWiseSelfCounterDetails(customerCode, selfCounterCode, subArea,sclUser,monthYear);
        if(monthWiseAnnualTargetModelDetails!=null){
            return  monthWiseAnnualTargetModelDetails;
        }
        else{
            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel=modelService.create(MonthWiseAnnualTargetModel.class);
            return monthWiseAnnualTargetModel;
        }
    }

    private SelfCounterSaleDetailsModel validateRevisedSelfCounterSaleModel(String customerCode, String subArea, SclUserModel sclUser, boolean isAnnualSalesRevised) {
        SelfCounterSaleDetailsModel selfCounterDetails = salesPlanningDao.fetchRevisedSelfCounterDetails(customerCode, subArea,sclUser, isAnnualSalesRevised);
        if(selfCounterDetails!=null){
            return  selfCounterDetails;
        }
        else{
            SelfCounterSaleDetailsModel selfCounterDetailsModel=modelService.create(SelfCounterSaleDetailsModel.class);
            return selfCounterDetailsModel;
        }
    }

    private SelfCounterSaleDetailsModel validateSelfCounterSaleModel(String customerCode, String subArea, SclUserModel sclUser) {
        SelfCounterSaleDetailsModel selfCounterDetails = salesPlanningDao.fetchSelfCounterDetails(customerCode,subArea, sclUser);
        if(selfCounterDetails!=null){
            return  selfCounterDetails;
        }
        else{
            SelfCounterSaleDetailsModel selfCounterDetailsModel=modelService.create(SelfCounterSaleDetailsModel.class);
            return selfCounterDetailsModel;
        }
    }

    private MonthWiseAnnualTargetModel validateMonthWiseDetailsOfDealerForRetailer(String customerCode, String subArea, String monthYear, SclUserModel sclUser){
        MonthWiseAnnualTargetModel monthWiseAnnualTargetModelDetails = salesPlanningDao.fetchMonthWiseAnnualTargetDetailsOfDealerForRetailer(customerCode,subArea,monthYear, sclUser);
        if(monthWiseAnnualTargetModelDetails!=null){
            return  monthWiseAnnualTargetModelDetails;
        }
        else{
            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel=modelService.create(MonthWiseAnnualTargetModel.class);
            return monthWiseAnnualTargetModel;
        }
    }

    private MonthWiseAnnualTargetModel validateRetailerMonthWiseDetails(String dealerCode, String retailerCode, String subArea, SclUserModel sclUser,String key, String value) {
        MonthWiseAnnualTargetModel monthWiseAnnualTargetModelDetails = salesPlanningDao.fetchRetailerMonthWiseDetails(dealerCode, retailerCode, subArea,sclUser,key, value);
        if(monthWiseAnnualTargetModelDetails!=null){
            return  monthWiseAnnualTargetModelDetails;
        }
        else{
            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel=modelService.create(MonthWiseAnnualTargetModel.class);
            return monthWiseAnnualTargetModel;
        }
    }

    private RetailerPlannedAnnualSalesDetailsModel validatePlannedRetailersDetails(String retailerCode, SclUserModel sclUser, String subArea) {
        RetailerPlannedAnnualSalesDetailsModel retailerPlannedAnnualSalesDetails = salesPlanningDao.fetchRetailerDetails(retailerCode,sclUser, subArea);
        if(retailerPlannedAnnualSalesDetails!=null){
            return  retailerPlannedAnnualSalesDetails;
        }
        else{
            RetailerPlannedAnnualSalesDetailsModel retailerPlannedAnnualSalesDetailsModel=modelService.create(RetailerPlannedAnnualSalesDetailsModel.class);
            return retailerPlannedAnnualSalesDetailsModel;
        }
    }

    private MonthWiseAnnualTargetModel validateRevisedRetailerMonthWiseDetails(String dealerCode, String retailerCode, String subArea, SclUserModel sclUser,String monthYear) {
        MonthWiseAnnualTargetModel monthWiseAnnualTargetModelDetails = salesPlanningDao.fetchRevisedRetailerMonthWiseDetails(dealerCode, retailerCode, subArea,sclUser,monthYear);
        if(monthWiseAnnualTargetModelDetails!=null){
            return  monthWiseAnnualTargetModelDetails;
        }
        else{
            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel=modelService.create(MonthWiseAnnualTargetModel.class);
            return monthWiseAnnualTargetModel;
        }
    }

    private RetailerRevisedAnnualSalesDetailsModel validateRevisedRetailersDetails(String retailerCode, SclUserModel sclUser, String subArea) {
        RetailerRevisedAnnualSalesDetailsModel retailerRevisedAnnualSalesDetails= salesPlanningDao.fetchRevisedRetailerDetails(retailerCode,sclUser, subArea);
        if(retailerRevisedAnnualSalesDetails!=null){
            return  retailerRevisedAnnualSalesDetails;
        }
        else{
            RetailerRevisedAnnualSalesDetailsModel retailerRevisedAnnualSalesDetailsModel=modelService.create(RetailerRevisedAnnualSalesDetailsModel.class);
            return retailerRevisedAnnualSalesDetailsModel;
        }
    }
    
    private MonthWiseAnnualTargetModel validateMonthWiseAnnualTargetModelForNoCySale(String customerCode, String subArea, String key, SclUserModel sclUser) {
        MonthWiseAnnualTargetModel monthWiseAnnualTargetModelDetails=salesPlanningDao.validateMonthwiseDealerDetailsForNoCySale(customerCode,subArea,key,sclUser);
        if(monthWiseAnnualTargetModelDetails!=null)
        {
            return monthWiseAnnualTargetModelDetails;
        }
        else
        {
            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel=modelService.create(MonthWiseAnnualTargetModel.class);
            return monthWiseAnnualTargetModel;
        }
    }

    private MonthWiseAnnualTargetModel validateMonthWiseAnnualTargetModelForNoCySaleForSku(String customerCode, String productCode, String subArea, String key, SclUserModel sclUser) {
        MonthWiseAnnualTargetModel monthWiseAnnualTargetModelDetails=salesPlanningDao.validateMonthwiseDealerDetailsForNoCySaleForSku(customerCode, productCode, subArea,key,sclUser);
        if(monthWiseAnnualTargetModelDetails!=null)
        {
            return monthWiseAnnualTargetModelDetails;
        }
        else
        {
            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel=modelService.create(MonthWiseAnnualTargetModel.class);
            return monthWiseAnnualTargetModel;
        }
    }

   @Override
    public ErrorListWsDTO submitAnnualSalesTargetSettingForDealers(AnnualSalesTargetSettingListData data) {
        final ErrorListWsDTO errorListWsDTO = new ErrorListWsDTO();
        final List<ErrorWsDTO> errorWsDTOList = new ArrayList<>();
        BaseSiteModel baseSite = baseSiteService.getCurrentBaseSite();
        SubAreaMasterModel subAreaMaster=territoryManagementService.getTerritoryById(data.getSubAreaId());
        if (data.getAnnualSalesTargetSetting() != null && !data.getAnnualSalesTargetSetting().isEmpty()) {
            SclUserModel sclUser = (SclUserModel) userService.getCurrentUser();
            AnnualSalesModel annualSalesModel = validateAnnualSalesModel(sclUser, subAreaMaster.getPk().toString(),baseSite);
            if (annualSalesModel != null) {
                if (annualSalesModel.getSubAreaMasterList() != null && !annualSalesModel.getSubAreaMasterList().isEmpty()) {
                    List<SubAreaMasterModel> subAreaListModelsForAnnual = new ArrayList<>(annualSalesModel.getSubAreaMasterList());
                    if(annualSalesModel.getSubAreaMasterList().contains(subAreaMaster) && annualSalesModel.getBrand()!=null && annualSalesModel.getBrand().equals(baseSite) && annualSalesModel.getSalesOfficer()!=null && annualSalesModel.getSalesOfficer().equals(sclUser))
                    {
                        annualSalesModel.setSalesOfficer(sclUser);
                        annualSalesModel.setDealerPlannedTotalCySales(data.getTotalCurrentYearSales() != null ? data.getTotalCurrentYearSales() : 0.0);
                        annualSalesModel.setDealerPlannedTotalPlanSales(data.getTotalPlanSales() != null ? data.getTotalPlanSales() : 0.0);
                        annualSalesModel.setIsAnnualSalesPlanned(true);
                        annualSalesModel.setBrand(baseSite);
                        subAreaListModelsForAnnual.add(subAreaMaster);
                        annualSalesModel.setSubAreaMasterList(subAreaListModelsForAnnual);
                    }
                    else
                    {
                        annualSalesModel.setSalesOfficer(sclUser);
                        annualSalesModel.setDealerPlannedTotalCySales(data.getTotalCurrentYearSales() != null ? data.getTotalCurrentYearSales() : 0.0);
                        annualSalesModel.setDealerPlannedTotalPlanSales(data.getTotalPlanSales() != null ? data.getTotalPlanSales() : 0.0);
                        annualSalesModel.setIsAnnualSalesPlanned(true);
                        annualSalesModel.setBrand(baseSite);
                        subAreaListModelsForAnnual.add(subAreaMaster);
                        annualSalesModel.setSubAreaMasterList(subAreaListModelsForAnnual);
                    }
                } else {
                    List<SubAreaMasterModel> subAreaListModelsForAnnual = new ArrayList<>();
                    annualSalesModel.setSalesOfficer(sclUser);
                    annualSalesModel.setDealerPlannedTotalCySales(data.getTotalCurrentYearSales() != null ? data.getTotalCurrentYearSales() : 0.0);
                    annualSalesModel.setDealerPlannedTotalPlanSales(data.getTotalPlanSales() != null ? data.getTotalPlanSales() : 0.0);
                    annualSalesModel.setIsAnnualSalesPlanned(true);
                    annualSalesModel.setBrand(baseSite);
                    subAreaListModelsForAnnual.add(subAreaMaster);
                    annualSalesModel.setSubAreaMasterList(subAreaListModelsForAnnual);
                }
            }
            annualSalesModel.setFinancialYear(findNextFinancialYear());
            //set district, region, state in annual sales along with subarea
            if(subAreaMaster != null) {
                annualSalesModel.setSubAreaMaster(subAreaMaster);
                DistrictMasterModel district = subAreaMaster.getDistrictMaster();
                if(district!=null) {
                    annualSalesModel.setDistrictMaster(district);
                    RegionMasterModel region = district.getRegion();
                    if(region!=null) {
                        annualSalesModel.setRegionMaster(region);
                    }
                    StateMasterModel state=region.getState();
                    if(state!=null)
                    {
                        annualSalesModel.setStateMaster(state);
                    }
                }
            }
            modelService.save(annualSalesModel);

            String nextFinancialYear = findNextFinancialYear();
            for (AnnualSalesTargetSettingData annualSalesTargetSettingData : data.getAnnualSalesTargetSetting()) {
                double sumOfMonthwiseTarget=0.0;
                double finalSum=0.0;

                String tempMonthYear = "";
                double tempMonthTarget = 0.0;

                DealerPlannedAnnualSalesModel dealerPlannedAnnualSalesModel = validateDealerAnnualSalesModel(annualSalesTargetSettingData.getCustomerCode(), subAreaMaster.getPk().toString(), sclUser, nextFinancialYear);
                SclCustomerModel sclCustomer = (SclCustomerModel) userService.getUserForUID(annualSalesTargetSettingData.getCustomerCode());
                dealerPlannedAnnualSalesModel.setCurrentYearSales(annualSalesTargetSettingData.getCurrentYearSales());
                dealerPlannedAnnualSalesModel.setPlannedYearSales(annualSalesTargetSettingData.getPlanSales());
                dealerPlannedAnnualSalesModel.setCustomerCode(sclCustomer.getUid());
                dealerPlannedAnnualSalesModel.setCustomerName(sclCustomer.getName());
                dealerPlannedAnnualSalesModel.setCustomerCategory(CounterType.DEALER);
                dealerPlannedAnnualSalesModel.setFinancialYear(findNextFinancialYear());//2023-2024
                dealerPlannedAnnualSalesModel.setCustomerPotential(Double.valueOf(annualSalesTargetSettingData.getCustomerPotential()));
                dealerPlannedAnnualSalesModel.setStatus("Finalized");
                dealerPlannedAnnualSalesModel.setSubAreaMaster(subAreaMaster);
                dealerPlannedAnnualSalesModel.setBrand(baseSite);

                if(subAreaMaster != null) {
                    DistrictMasterModel district = subAreaMaster.getDistrictMaster();
                    if (district != null) {
                        dealerPlannedAnnualSalesModel.setDistrictMaster(district);
                        RegionMasterModel region = district.getRegion();
                        if (region != null) {
                            dealerPlannedAnnualSalesModel.setRegionMaster(region);
                            StateMasterModel state = region.getState();
                            if (state != null) {
                                dealerPlannedAnnualSalesModel.setStateMaster(state);
                            }
                        }
                    }
                }

                Date startDate = null, endDate = null;
                List<Date> date = getCurrentFY();
                double finalSumOfQuantity = 0.0;
                double perMonthPercentage = 0.0;
                startDate = date.get(0);
                endDate = date.get(1);

                String[] s = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "InvalidNumber"};
                List<List<Object>> monthSplitupForDealerPlannedAnnualSales = salesPlanningDao.getMonthSplitupForDealerPlannedAnnualSales(sclCustomer.getUid(), sclUser, baseSiteService.getCurrentBaseSite(), startDate, endDate, subAreaMaster.getPk().toString());
                finalSumOfQuantity += monthSplitupForDealerPlannedAnnualSales.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects1 -> objects1.size() > 1 ? (Double) objects1.get(2) : 0.0).sum();
                double finalSumOfQuantity1 = finalSumOfQuantity;

                List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetModels = new ArrayList();
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

                for (List<Object> objects : monthSplitupForDealerPlannedAnnualSales) {
                    if (!objects.isEmpty() && objects != null) {
                        String monthName = (String) objects.get(0);
                        Double qty = (Double) objects.get(2);
                        for (Map.Entry<String, String> mapEntries : results.entrySet()) {
                            if (mapEntries.getKey().equals(monthName)) {
                                mapEntries.setValue(String.valueOf(qty));
                            }
                        }
                    }
                }

                for (Map.Entry<String, String> listOfMonthAndQty : results.entrySet()) {
                    String key = listOfMonthAndQty.getKey();
                    String value = listOfMonthAndQty.getValue();

                    MonthWiseAnnualTargetModel monthWiseAnnualTargetModel = validateMonthWiseAnnualTargetModel(annualSalesTargetSettingData.getCustomerCode(), subAreaMaster.getPk().toString(), key, value, sclUser);
                    StringBuilder str = new StringBuilder();
                    if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 12) {
                        if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 3) {
                            monthWiseAnnualTargetModel.setMonthYear(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear() + 1))));
                        }
                        if (Integer.parseInt(key) >= 4 && Integer.parseInt(key) <= 12) {
                            monthWiseAnnualTargetModel.setMonthYear(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear()))));
                        }
                    }
                    monthWiseAnnualTargetModel.setCustomerCode(sclCustomer.getUid());

                    //if (value != null && finalSumOfQuantity1 != 0.0 && Integer.parseInt(key) != 3)
                    if (value != null && finalSumOfQuantity1 != 0.0) {
                        perMonthPercentage = (Double.parseDouble(value) / finalSumOfQuantity1) * 100;
                        LOG.info("dealer :: " + annualSalesTargetSettingData.getCustomerCode() + "per month percentage ::" + perMonthPercentage);
                        monthWiseAnnualTargetModel.setPerMonthPercentage(perMonthPercentage);
                    } else {
                        monthWiseAnnualTargetModel.setPerMonthPercentage(0.0);
                    }
                    if (annualSalesTargetSettingData.getPlanSales() != 0.0) {
                        if(monthSplitupForDealerPlannedAnnualSales == null || monthSplitupForDealerPlannedAnnualSales.isEmpty())
                        {
                            if (monthWiseAnnualTargetModel.getMonthYear().startsWith("Mar")) {
                                LOG.info("inside if key is equal to 3" + monthWiseAnnualTargetModel.getMonthYear());
                                monthWiseAnnualTargetModel.setMonthTarget(annualSalesTargetSettingData.getPlanSales());
                                LOG.info("month wise annual target for dealer" + monthWiseAnnualTargetModel.getMonthTarget());
                            } else {
                                monthWiseAnnualTargetModel.setMonthTarget(0.0);
                            }
                        }
                        else
                        {
                            //double monthTargetForMarch=0.0;
                           // double monthwiseTarget=0.0;
                            double monthwiseTarget=0.0;
                            monthwiseTarget = Math.round((annualSalesTargetSettingData.getPlanSales() * perMonthPercentage) / 100);
                            LOG.info("dealer code ::" + annualSalesTargetSettingData.getCustomerCode() + " " + "Plan Target ::" + annualSalesTargetSettingData.getPlanSales());
                            LOG.info("Month wise target for dealer ::" + " " + annualSalesTargetSettingData.getCustomerCode() + " " + "Month Year" + monthWiseAnnualTargetModel.getMonthYear() + " " + monthWiseAnnualTargetModel.getMonthTarget());
                            monthWiseAnnualTargetModel.setMonthTarget(monthwiseTarget);
                            sumOfMonthwiseTarget +=monthwiseTarget;
                           // if(monthwiseTarget > 0.0 && !monthWiseAnnualTargetModel.getMonthYear().startsWith("Mar"))  {
                            if(monthwiseTarget > 0.0)
                            {
                                tempMonthTarget = monthwiseTarget;
                                tempMonthYear = monthWiseAnnualTargetModel.getMonthYear();
                            }

                            /*if(!monthWiseAnnualTargetModel.getMonthYear().startsWith("Mar"))
                            {
                                monthwiseTarget  = Math.round((annualSalesTargetSettingData.getPlanSales() * perMonthPercentage) / 100);
                                sumOfMonthwiseTarget += monthwiseTarget;
                                LOG.info("dealer :: " + annualSalesTargetSettingData.getCustomerCode() + "sumOfMonthwiseTarget :: " + sumOfMonthwiseTarget);
                                LOG.info("dealer :: " + annualSalesTargetSettingData.getCustomerCode() + "monthwiseTarget :: " + monthwiseTarget);
                                monthWiseAnnualTargetModel.setMonthTarget(monthwiseTarget);
                            }
                            finalSum = sumOfMonthwiseTarget;
                            LOG.info("dealer :: " + annualSalesTargetSettingData.getCustomerCode() + "finalSum :: " + finalSum);

                            if(monthWiseAnnualTargetModel.getMonthYear().startsWith("Mar"))
                            {
                                monthTargetForMarch = Math.abs(annualSalesTargetSettingData.getPlanSales() - finalSum);
                                LOG.info("dealer :: " + annualSalesTargetSettingData.getCustomerCode() + "monthTargetForMarch :: " + monthTargetForMarch);
                                monthWiseAnnualTargetModel.setMonthTarget(monthTargetForMarch);
                            }*/
                        }
                    } else {
                        monthWiseAnnualTargetModel.setMonthTarget(0.0);
                    }
                    monthWiseAnnualTargetModel.setSubAreaMaster(subAreaMaster);
                    monthWiseAnnualTargetModel.setSalesOfficer(sclUser);
                    monthWiseAnnualTargetModel.setBrand(baseSite);
                    if(subAreaMaster != null) {
                        DistrictMasterModel district = subAreaMaster.getDistrictMaster();
                        if (district != null) {
                            monthWiseAnnualTargetModel.setDistrictMaster(district);
                            RegionMasterModel region = district.getRegion();
                            if (region != null) {
                                monthWiseAnnualTargetModel.setRegionMaster(region);
                                StateMasterModel state = region.getState();
                                if (state != null) {
                                    monthWiseAnnualTargetModel.setStateMaster(state);
                                }
                            }
                        }
                    }
                    monthWiseAnnualTargetModels.add(monthWiseAnnualTargetModel);
                    modelService.save(monthWiseAnnualTargetModel);
                }
                double finalMonthWiseTarget=0.0, diff = tempMonthTarget;
                if(annualSalesTargetSettingData.getPlanSales() > sumOfMonthwiseTarget)
                {
                    finalMonthWiseTarget = annualSalesTargetSettingData.getPlanSales() - sumOfMonthwiseTarget;
                    diff = tempMonthTarget + finalMonthWiseTarget;
                }
                else if(annualSalesTargetSettingData.getPlanSales() < sumOfMonthwiseTarget)
                {
                    finalMonthWiseTarget =  sumOfMonthwiseTarget - annualSalesTargetSettingData.getPlanSales() ;
                    diff = tempMonthTarget - finalMonthWiseTarget;
                }
                for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : monthWiseAnnualTargetModels) {
                    //if (monthWiseAnnualTargetModel.getMonthYear().equals(tempMonthYear) && annualSalesTargetSettingData.getCustomerCode().equals(monthWiseAnnualTargetModel.getCustomerCode()) && monthWiseAnnualTargetModel.getProductCode() == null && !monthWiseAnnualTargetModel.getMonthYear().startsWith("Mar"))
                    if (monthWiseAnnualTargetModel.getMonthYear().equals(tempMonthYear) && annualSalesTargetSettingData.getCustomerCode().equals(monthWiseAnnualTargetModel.getCustomerCode()) && monthWiseAnnualTargetModel.getProductCode() == null)
                    {
                        LOG.info("Month wise target for dealer for last month::" + "" + annualSalesTargetSettingData.getCustomerCode() + " " + tempMonthYear + ":: differnce" + Math.abs(diff));
                        monthWiseAnnualTargetModel.setMonthTarget(Math.abs(diff));
                        modelService.save(monthWiseAnnualTargetModel);
                        LOG.info("After Assigning Target to last month for dealer" + monthWiseAnnualTargetModel.getMonthYear() + " " + monthWiseAnnualTargetModel.getMonthTarget());
                        break;
                    }
                }

                dealerPlannedAnnualSalesModel.setMonthWiseAnnualTarget(monthWiseAnnualTargetModels);
                modelService.save(dealerPlannedAnnualSalesModel);

                if (annualSalesTargetSettingData.getSkuDataList() != null && !annualSalesTargetSettingData.getSkuDataList().isEmpty()) {
                    List<ProductModel> skuList = new ArrayList<>();
                    //skuList.addAll(dealerPlannedAnnualSalesModel.getListOfSkus());
                    for (SKUData skuData : annualSalesTargetSettingData.getSkuDataList()) {
                        double sumOfMonthwiseTargetSku=0.0;
                        double finalSumSku=0.0;
                        String tempMonthYearForSku = "";
                        double tempMonthTargetForSku = 0.0;
                        List<ProductModel> sku = skuList.stream().filter(p -> p.getCode().equals(skuData.getProductCode())).collect(Collectors.toList());

                                /*if (CollectionUtils.isNotEmpty(sku)) {
                                        //  error in case of same customer with same product
                                        ErrorWsDTO error = getError(skuData.getProductName(), SKU_CODE_EXISTING.concat(" ").concat("For" + " " + annualSalesTargetSettingData.getCustomerCode()), AmbiguousIdentifierException.class.getName());
                                        errorWsDTOList.add(error);
                                        //break;
                                    }*/
                        List<ProductSaleModel> productSaleModelList = new ArrayList<>();
                        double totalTargetForAllSKU = 0.0;
                        ProductModel productModel = productService.getProductForCode(skuData.getProductCode());
                        //ProductModel productModel = sclProductUtility.getProductByCatalogVersion(skuData.getProductCode());
                        totalTargetForAllSKU += skuData.getPlanSales();
                        ProductSaleModel productSaleModel = validateProductSalesModel(annualSalesTargetSettingData.getCustomerCode(), skuData.getProductCode(), subAreaMaster.getPk().toString(), sclUser);
                        productSaleModel.setCySales(skuData.getCySales());
                        productSaleModel.setPlanSales(skuData.getPlanSales());
                        productSaleModel.setProductCode(skuData.getProductCode());
                        productSaleModel.setProductName(skuData.getProductName());
                        productSaleModel.setProductGrade(productModel.getGrade());
                        productSaleModel.setProductPackaging(productModel.getPackagingCondition());
                        productSaleModel.setProductPackType(productModel.getBagType());
                        productSaleModel.setCustomerCode(annualSalesTargetSettingData.getCustomerCode());
                        productSaleModel.setTotalTarget(skuData.getPlanSales());
                        productSaleModel.setSubAreaMaster(subAreaMaster);
                        productSaleModel.setSalesOfficer(sclUser);
                        productSaleModel.setIsNewSku(skuData.getIsNewSku());
                        productSaleModel.setBrand(baseSite);
                        if(skuData.getIsNewSku()!=null && skuData.getIsNewSku())
                        {
                            String premium="";
                            if(StringUtils.isNotBlank(productModel.getPremium())) {
                                 premium = productModel.getPremium();
                                 boolean isPremium = premium.equalsIgnoreCase("Y");
                                 productSaleModel.setPremium(isPremium);
                            }
                        }
                        else {
                            productSaleModel.setPremium(skuData.getPremium() != null ? skuData.getPremium() : false);
                        }

                        if(subAreaMaster != null) {
                            DistrictMasterModel district = subAreaMaster.getDistrictMaster();
                            if (district != null) {
                                productSaleModel.setDistrictMaster(district);
                                RegionMasterModel region = district.getRegion();
                                if (region != null) {
                                    productSaleModel.setRegionMaster(region);
                                    StateMasterModel state = region.getState();
                                     if(state != null) {
                                        productSaleModel.setStateMaster(state);
                                    }
                                }
                            }
                        }

                        modelService.save(productSaleModel);
                        productSaleModelList.add(productSaleModel);
                        productModel.setProductSale(productSaleModelList);
                        //Here we have to set Total Target for every product wise
                        Map<String, String> resultsSKU = new LinkedHashMap<>();
                        resultsSKU.put("4", "0");
                        resultsSKU.put("5", "0");
                        resultsSKU.put("6", "0");
                        resultsSKU.put("7", "0");

                        resultsSKU.put("8", "0");
                        resultsSKU.put("9", "0");
                        resultsSKU.put("10", "0");
                        resultsSKU.put("11", "0");

                        resultsSKU.put("12", "0");
                        resultsSKU.put("1", "0");
                        resultsSKU.put("2", "0");
                        resultsSKU.put("3", "0");

                        for (List<Object> objects : monthSplitupForDealerPlannedAnnualSales) {
                            if (!objects.isEmpty() && objects != null) {
                                String monthName = (String) objects.get(0);//6
                                Double qty = (Double) objects.get(2);
                                for (Map.Entry<String, String> mapEntries : resultsSKU.entrySet()) {
                                    if (mapEntries.getKey().equals(monthName)) {
                                        mapEntries.setValue(String.valueOf(qty));
                                    }
                                }
                            }
                        }
                        double totalMonthlyTarget = 0.0;
                        List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetModelsforSKU = new ArrayList();
                        for (Map.Entry<String, String> listOfMonthAndQty : resultsSKU.entrySet()) {
                            String key = listOfMonthAndQty.getKey();
                            String value = listOfMonthAndQty.getValue();
                            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel = validateMonthWiseAnnualTargetModelForSku(subAreaMaster.getPk().toString(), sclCustomer.getUid(), skuData.getProductCode(), key, value, sclUser);
                            if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 12) {
                                StringBuilder str = new StringBuilder();
                                if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 3) {
                                    monthWiseAnnualTargetModel.setMonthYear(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear() + 1))));
                                }
                                if (Integer.parseInt(key) >= 4 && Integer.parseInt(key) <= 12) {
                                    monthWiseAnnualTargetModel.setMonthYear(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear()))));
                                }
                            }
                            monthWiseAnnualTargetModel.setCustomerCode(sclCustomer.getUid());
                            monthWiseAnnualTargetModel.setProductCode(skuData.getProductCode());
                            monthWiseAnnualTargetModel.setPremium(skuData.getPremium()!=null?skuData.getPremium():false);
                            ProductModel product = productService.getProductForCode(skuData.getProductCode());
                            if(!Objects.isNull(product)) {
                                monthWiseAnnualTargetModel.setProductGrade(product.getGrade());
                                monthWiseAnnualTargetModel.setProductPackaging(product.getPackagingCondition());
                                monthWiseAnnualTargetModel.setProductBagType(product.getBagType());
                            }
                            //if (value != null && finalSumOfQuantity1 != 0.0 && Integer.parseInt(key) != 3) {
                            if (value != null && finalSumOfQuantity1 != 0.0) {
                                perMonthPercentage = (Double.parseDouble(value) / finalSumOfQuantity1) * 100;
                                LOG.info("dealer :: " + annualSalesTargetSettingData.getCustomerCode() + " " + "Sku code ::" + skuData.getProductCode() + " "+"per month percentage ::" + perMonthPercentage);
                                monthWiseAnnualTargetModel.setPerMonthPercentage(perMonthPercentage);
                            } else {
                                monthWiseAnnualTargetModel.setPerMonthPercentage(0.0);
                            }
                            if (skuData.getPlanSales() != 0.0) {
                                if(monthSplitupForDealerPlannedAnnualSales == null || monthSplitupForDealerPlannedAnnualSales.isEmpty())
                                {
                                    if (monthWiseAnnualTargetModel.getMonthYear().startsWith("Mar")) {
                                        LOG.info("inside if key is equal to 3" + monthWiseAnnualTargetModel.getMonthYear());
                                        monthWiseAnnualTargetModel.setMonthTarget(skuData.getPlanSales());
                                        LOG.info("month wise annual target for dealer" + monthWiseAnnualTargetModel.getMonthTarget());
                                    } else {
                                        monthWiseAnnualTargetModel.setMonthTarget(0.0);
                                    }
                                }
                                else {
                                    double monthwiseTarget=0.0;
                                    monthwiseTarget = Math.round((skuData.getPlanSales() * perMonthPercentage) / 100);
                                    LOG.info("dealer code ::" + annualSalesTargetSettingData.getCustomerCode() + " " + "sku code ::" + skuData.getProductCode() + "Plan Target ::" + skuData.getPlanSales());
                                    monthWiseAnnualTargetModel.setMonthTarget(monthwiseTarget);
                                    LOG.info("Month wise target for dealer and sku ::" + " " + annualSalesTargetSettingData.getCustomerCode() + " " + "Month Year" + monthWiseAnnualTargetModel.getMonthYear() + " " + monthWiseAnnualTargetModel.getMonthTarget());
                                    sumOfMonthwiseTargetSku +=monthwiseTarget;
                                    //if(monthwiseTarget > 0.0 && !monthWiseAnnualTargetModel.getMonthYear().startsWith("Mar")) {
                                    if(monthwiseTarget > 0.0) {
                                        tempMonthTargetForSku = monthwiseTarget;
                                        tempMonthYearForSku = monthWiseAnnualTargetModel.getMonthYear();
                                    }

                                    /*double monthTargetForMarch=0.0;
                                    double monthwiseTarget=0.0;
                                    if(!monthWiseAnnualTargetModel.getMonthYear().startsWith("Mar"))
                                    {
                                        monthwiseTarget = Math.round((skuData.getPlanSales() * perMonthPercentage) / 100);
                                        LOG.info("dealer :: " + annualSalesTargetSettingData.getCustomerCode() + " " + "Sku code ::" + skuData.getProductCode() + " " + "monthwiseTarget ::" + monthwiseTarget);

                                        sumOfMonthwiseTargetSku +=  monthwiseTarget;
                                        LOG.info("dealer :: " + annualSalesTargetSettingData.getCustomerCode() + " " + "Sku code ::" + skuData.getProductCode() + " " + "sumOfMonthwiseTargetSku ::" + sumOfMonthwiseTargetSku);

                                        monthWiseAnnualTargetModel.setMonthTarget(monthwiseTarget);
                                    }
                                    finalSumSku=sumOfMonthwiseTargetSku;
                                    LOG.info("dealer :: " + annualSalesTargetSettingData.getCustomerCode() + " " + "Sku code ::" + skuData.getProductCode() + " " + "finalSumSku ::" + finalSumSku);

                                    if(monthWiseAnnualTargetModel.getMonthYear().startsWith("Mar"))
                                    {
                                        monthTargetForMarch = Math.abs(skuData.getPlanSales() - finalSumSku);
                                        LOG.info("dealer :: " + annualSalesTargetSettingData.getCustomerCode() + " " + "Sku code ::" + skuData.getProductCode() + " " + "monthTargetForMarch ::" + monthTargetForMarch);

                                        monthWiseAnnualTargetModel.setMonthTarget(monthTargetForMarch);
                                    }*/
                                }
                            } else {
                                monthWiseAnnualTargetModel.setMonthTarget(0.0);
                            }
                            // totalMonthlyTarget += monthwiseTarget;
                            // monthWiseAnnualTargetModel.setTotalMonthlyWiseTarget(totalMonthlyTarget);
                            monthWiseAnnualTargetModel.setSubAreaMaster(subAreaMaster);
                            if(subAreaMaster != null) {
                                DistrictMasterModel district = subAreaMaster.getDistrictMaster();
                                if (district != null) {
                                    monthWiseAnnualTargetModel.setDistrictMaster(district);
                                    RegionMasterModel region = district.getRegion();
                                    if (region != null) {
                                        monthWiseAnnualTargetModel.setRegionMaster(region);
                                        StateMasterModel state = region.getState();
                                        if (state != null) {
                                            monthWiseAnnualTargetModel.setStateMaster(state);
                                        }
                                    }
                                }
                            }
                            monthWiseAnnualTargetModel.setSalesOfficer(sclUser);
                            monthWiseAnnualTargetModel.setBrand(baseSite);
                            monthWiseAnnualTargetModelsforSKU.add(monthWiseAnnualTargetModel);
                            modelService.save(monthWiseAnnualTargetModel);
                        }

                        double finalMonthWiseTargetForSku=0.0, diffForSku = tempMonthTargetForSku;
                        if(skuData.getPlanSales() > sumOfMonthwiseTargetSku)
                        {
                            finalMonthWiseTargetForSku = skuData.getPlanSales() - sumOfMonthwiseTargetSku;
                            diffForSku = tempMonthTargetForSku + finalMonthWiseTargetForSku;
                        }
                        else if(skuData.getPlanSales() < sumOfMonthwiseTargetSku)
                        {
                            finalMonthWiseTargetForSku =  sumOfMonthwiseTargetSku - skuData.getPlanSales();
                            diffForSku = tempMonthTargetForSku - finalMonthWiseTargetForSku;
                        }
                        for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : monthWiseAnnualTargetModelsforSKU) {
                           // if (monthWiseAnnualTargetModel.getMonthYear().equals(tempMonthYearForSku) && skuData.getProductCode().equals(monthWiseAnnualTargetModel.getProductCode())
                           //         && annualSalesTargetSettingData.getCustomerCode().equals(monthWiseAnnualTargetModel.getCustomerCode()) && !monthWiseAnnualTargetModel.getMonthYear().startsWith("Mar"))
                                if (monthWiseAnnualTargetModel.getMonthYear().equals(tempMonthYearForSku) && skuData.getProductCode().equals(monthWiseAnnualTargetModel.getProductCode())
                                        && annualSalesTargetSettingData.getCustomerCode().equals(monthWiseAnnualTargetModel.getCustomerCode()))
                            {
                                LOG.info("Month wise target for dealer and sku for last month::" + " " + annualSalesTargetSettingData.getCustomerCode() + " " + "sku code ::" + " " + skuData.getProductCode() + tempMonthYearForSku + ":: differnce" + Math.abs(diffForSku));
                                monthWiseAnnualTargetModel.setMonthTarget(Math.abs(diffForSku));
                                modelService.save(monthWiseAnnualTargetModel);
                                LOG.info("After Assigning Target to last month for dealer and sku" + monthWiseAnnualTargetModel.getMonthYear() + " " + monthWiseAnnualTargetModel.getMonthTarget());
                                break;
                            }
                        }

                        modelService.save(productModel);
                        skuList.add(productModel);
                        dealerPlannedAnnualSalesModel.setTotalTarget(totalTargetForAllSKU);
                    }
                    dealerPlannedAnnualSalesModel.setListOfSkus(skuList);
                    dealerPlannedAnnualSalesModel.setAnnualSales(annualSalesModel);
                    modelService.save(dealerPlannedAnnualSalesModel);
                }
            }
            modelService.save(annualSalesModel);
        }
        errorListWsDTO.setErrors(errorWsDTOList);
        return errorListWsDTO;
    }

    @Override
    public ErrorListWsDTO submitAnnualSalesTargetSettingForRetailers(AnnualSalesTargetSettingListData listData) {
        final ErrorListWsDTO errorListWsDTO = new ErrorListWsDTO();
        final List<ErrorWsDTO> errorWsDTOList = new ArrayList<>();
        BaseSiteModel baseSite = baseSiteService.getCurrentBaseSite();
         if (listData.getAnnualSalesTargetSetting() != null && !listData.getAnnualSalesTargetSetting().isEmpty()) {
           SclUserModel sclUser = (SclUserModel) userService.getCurrentUser();
             SubAreaMasterModel subAreaMaster=territoryManagementService.getTerritoryById(listData.getSubAreaId());
             AnnualSalesModel annualSalesModel = validateAnnualSalesModel(sclUser, subAreaMaster.getPk().toString(), baseSite);
             if (annualSalesModel != null) {
                 if (annualSalesModel.getSubAreaMasterList() != null && !annualSalesModel.getSubAreaMasterList().isEmpty()) {
                     List<SubAreaMasterModel> subAreaListModelsForAnnual = new ArrayList<>(annualSalesModel.getSubAreaMasterList());
                     for (SubAreaMasterModel subAreaMasterModel : annualSalesModel.getSubAreaMasterList()) {
                         if (subAreaMasterModel.equals(subAreaMaster)) {
                             annualSalesModel.setSalesOfficer(sclUser);
                             annualSalesModel.setRetailerPlannedTotalCySales(listData.getTotalCurrentYearSales() != null ? listData.getTotalCurrentYearSales() : 0.0);
                             annualSalesModel.setRetailerPlannedTotalPlanSales(listData.getTotalPlanSales() != null ? listData.getTotalPlanSales() : 0.0);
                             annualSalesModel.setIsAnnualSalesPlannedForRetailer(true);
                             annualSalesModel.setBrand(baseSite);
                             subAreaListModelsForAnnual.add(subAreaMaster);
                             annualSalesModel.setSubAreaMasterList(subAreaListModelsForAnnual);
                         }
                     }
                 } else {
                     List<SubAreaMasterModel> subAreaListModelsForAnnual = new ArrayList<>();
                     annualSalesModel.setSalesOfficer(sclUser);
                     annualSalesModel.setRetailerPlannedTotalCySales(listData.getTotalCurrentYearSales() != null ? listData.getTotalCurrentYearSales() : 0.0);
                     annualSalesModel.setRetailerPlannedTotalPlanSales(listData.getTotalPlanSales() != null ? listData.getTotalPlanSales() : 0.0);
                     annualSalesModel.setIsAnnualSalesPlannedForRetailer(true);
                     annualSalesModel.setBrand(baseSite);
                     subAreaListModelsForAnnual.add(subAreaMaster);
                     annualSalesModel.setSubAreaMasterList(subAreaListModelsForAnnual);
                 }
             }
             annualSalesModel.setFinancialYear(findNextFinancialYear());
             if(subAreaMaster != null) {
                 annualSalesModel.setSubAreaMaster(subAreaMaster);
                 DistrictMasterModel district = subAreaMaster.getDistrictMaster();
                 if(district!=null) {
                     annualSalesModel.setDistrictMaster(district);
                     RegionMasterModel region = district.getRegion();
                     if(region!=null) {
                         annualSalesModel.setRegionMaster(region);
                     }
                     StateMasterModel state=region.getState();
                     if(state!=null)
                     {
                         annualSalesModel.setStateMaster(state);
                     }
                 }
             }
             modelService.save(annualSalesModel);

            double dealerMonthwiseData[]= new double[13];
            double retailerMonthwiseDataList[]= new double[13];
            String financialYear = findNextFinancialYear();
            for (AnnualSalesTargetSettingData data : listData.getAnnualSalesTargetSetting()) {
                double sumOfMonthwiseTarget=0.0;
                double finalSum=0.0;

                String tempMonthYear = "";
                double tempMonthTarget = 0.0;
                RetailerPlannedAnnualSalesModel retailerPlannedAnnualSalesModel = validateRetailerPlannedAnnualSalesModel(data.getCustomerCode(), subAreaMaster.getPk().toString(), sclUser, financialYear);
                retailerPlannedAnnualSalesModel.setCurrentYearSales(data.getCurrentYearSales());
                retailerPlannedAnnualSalesModel.setPlannedYearSales(data.getPlanSales());
                retailerPlannedAnnualSalesModel.setCustomerCode(data.getCustomerCode());
                retailerPlannedAnnualSalesModel.setCustomerName(data.getCustomerName());
                retailerPlannedAnnualSalesModel.setCustomerPotential(data.getCustomerPotential());
                retailerPlannedAnnualSalesModel.setErpCustomerNo(data.getErpCustomerNo());
                retailerPlannedAnnualSalesModel.setFinancialYear(findNextFinancialYear());
                retailerPlannedAnnualSalesModel.setSubAreaMaster(subAreaMaster);
                retailerPlannedAnnualSalesModel.setBrand(baseSite);
                Date startDate = null, endDate = null;
                List<Date> date = getCurrentFY();
                double finalSumOfQuantity = 0.0;
                double perMonthPercentage = 0.0;
                startDate = date.get(0);
                endDate = date.get(1);

                double finalSumOfSelfQuantity =0.0;
                double finalSumOfRetailerQuantity =0.0;
                double totalTargetForRetailerDetails=0.0;
                double totalTargetForSelfCounter=0.0;
                double totalTargetForDealer =0.0;
                String[] s = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "InvalidNumber"};
                //List<List<Object>> monthSplitupForRetailerPlannedAnnualSales = salesPlanningDao.getMonthSplitupForDealerPlannedAnnualSales(data.getCustomerCode(), sclUser, baseSiteService.getCurrentBaseSite(), startDate, endDate, subAreaMaster.getPk().toString());
                List<List<Object>> monthSplitupForRetailerPlannedAnnualSales = salesPlanningDao.getMonthSplitupFormDealerRevisedAnnualSales(data.getCustomerCode(), sclUser, subAreaMaster.getPk().toString());
                finalSumOfQuantity += monthSplitupForRetailerPlannedAnnualSales.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects1 -> objects1.size() > 0 ? (Double) objects1.get(1) : 0.0).sum();
                double finalSumOfQuantity1 = finalSumOfQuantity;

                Map<String, String> monthNameMap = new LinkedHashMap<>();
                monthNameMap.put("4", "Apr");
                monthNameMap.put("5", "May");
                monthNameMap.put("6", "Jun");
                monthNameMap.put("7", "Jul");
                monthNameMap.put("8", "Aug");
                monthNameMap.put("9", "Sep");
                monthNameMap.put("10", "Oct");
                monthNameMap.put("11", "Nov");
                monthNameMap.put("12", "Dec");
                monthNameMap.put("1", "Jan");
                monthNameMap.put("2", "Feb");
                monthNameMap.put("3", "Mar");

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

                for (List<Object> monthSplitUp : monthSplitupForRetailerPlannedAnnualSales) {
                    String monthYear = (String) monthSplitUp.get(0);
                    String[] split = monthYear.split("-");
                    String monthName = split[0];
                    String keyVal=null;
                    Double qty = (Double) monthSplitUp.get(1);
                    for (Map.Entry<String, String> mapEntries1 : monthNameMap.entrySet()) {
                        if (mapEntries1.getValue().equalsIgnoreCase(monthName)) {
                            keyVal=mapEntries1.getKey();
                            LOG.info("dealer split up mapEntries1 :" + keyVal);
                        }
                    }
                    for (Map.Entry<String, String> mapEntries2 : results.entrySet()) {
                        if (mapEntries2.getKey().equalsIgnoreCase(keyVal)) {
                            mapEntries2.setValue(String.valueOf(qty));
                            LOG.info("dealer split up value mapEntries2 :" + mapEntries2.getValue());
                        }
                    }
                }

                for (Map.Entry<String, String> resultsLog : results.entrySet()) {
                    LOG.info("resultsLog for dealer :" +  "key:" + resultsLog.getKey() + "value:" + resultsLog.getValue());
                }

                List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetModelsRet = new ArrayList();
                for (Map.Entry<String, String> listOfMonthAndQty : results.entrySet()) {
                    String key = listOfMonthAndQty.getKey();
                    String value = listOfMonthAndQty.getValue();

                    MonthWiseAnnualTargetModel monthWiseAnnualTargetModel = validateMonthWiseDetailsForDealerRetailer(data.getCustomerCode(), subAreaMaster.getPk().toString(), key, value, sclUser);
                    if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 12) {
                        StringBuilder str = new StringBuilder();
                        if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 3) {
                            monthWiseAnnualTargetModel.setMonthYear(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear() + 1))));
                        }
                        if (Integer.parseInt(key) >= 4 && Integer.parseInt(key) <= 12) {
                            monthWiseAnnualTargetModel.setMonthYear(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear()))));
                        }
                    }
                    monthWiseAnnualTargetModel.setCustomerCode(data.getCustomerCode());

                    LOG.info("finalSumOfQuantity1 dealer :"+finalSumOfQuantity1);
                    LOG.info("key for dealer=" + key);
                    LOG.info("value for dealer =" +value);
                    if (value!= null && finalSumOfQuantity1 != 0.0) {
                        perMonthPercentage = ((Double.parseDouble(value) / finalSumOfQuantity1) * 100);
                        LOG.info("Retailer Planning (dealer) :: " + data.getCustomerCode() + "per month percentage ::" + perMonthPercentage);
                        monthWiseAnnualTargetModel.setPerMonthPercentage(perMonthPercentage);
                    } else {
                        monthWiseAnnualTargetModel.setPerMonthPercentage(0.0);
                    }
                    modelService.save(monthWiseAnnualTargetModel);

                    if (data.getPlanSales() != 0.0) {
                        if(monthSplitupForRetailerPlannedAnnualSales == null || monthSplitupForRetailerPlannedAnnualSales.isEmpty())
                        {
                            if (monthWiseAnnualTargetModel.getMonthYear().startsWith("Mar")) {
                                LOG.info("inside if key is equal to 3 for retailer planning (dealer)" + monthWiseAnnualTargetModel.getMonthYear());
                                monthWiseAnnualTargetModel.setMonthTarget(data.getPlanSales());
                                LOG.info("month wise annual target for retailer planning (dealer)" + monthWiseAnnualTargetModel.getMonthTarget());
                            } else {
                                monthWiseAnnualTargetModel.setMonthTarget(0.0);
                            }
                        }
                        else
                        {
                            double monthwiseTarget=0.0;
                            monthwiseTarget = Math.round((data.getPlanSales() * perMonthPercentage) / 100);
                            LOG.info("dealer code ::" + data.getCustomerCode() + " " + "Plan Target ::" + data.getPlanSales());
                            monthWiseAnnualTargetModel.setMonthTarget(monthwiseTarget);
                            LOG.info("Month wise target for retailer plan (dealer) ::" + " " + data.getCustomerCode() + " " + "Month Year" + monthWiseAnnualTargetModel.getMonthYear() + " " + monthWiseAnnualTargetModel.getMonthTarget());
                            sumOfMonthwiseTarget +=monthwiseTarget;
                            if(monthwiseTarget > 0.0)  {
                                tempMonthTarget = monthwiseTarget;
                                tempMonthYear = monthWiseAnnualTargetModel.getMonthYear();
                            }
                        }
                    }
                    else {
                        monthWiseAnnualTargetModel.setMonthTarget(0.0);
                    }
                    monthWiseAnnualTargetModel.setSubAreaMaster(subAreaMaster);
                    monthWiseAnnualTargetModel.setSalesOfficer(sclUser);
                    monthWiseAnnualTargetModel.setBrand(baseSite);
                    monthWiseAnnualTargetModelsRet.add(monthWiseAnnualTargetModel);
                    modelService.save(monthWiseAnnualTargetModel);
                }

                double finalMonthWiseTarget=0.0, diff = tempMonthTarget;
                if(data.getPlanSales() > sumOfMonthwiseTarget)
                {
                    finalMonthWiseTarget = data.getPlanSales() - sumOfMonthwiseTarget;
                    diff = tempMonthTarget + finalMonthWiseTarget;
                }
                else if(data.getPlanSales() < sumOfMonthwiseTarget)
                {
                    finalMonthWiseTarget =  sumOfMonthwiseTarget - data.getPlanSales() ;
                    diff = tempMonthTarget - finalMonthWiseTarget;
                }
                for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : monthWiseAnnualTargetModelsRet) {
                    if (monthWiseAnnualTargetModel.getMonthYear().equals(tempMonthYear) && data.getCustomerCode().equals(monthWiseAnnualTargetModel.getCustomerCode()) && monthWiseAnnualTargetModel.getProductCode() == null && monthWiseAnnualTargetModel.getRetailerCode() == null && monthWiseAnnualTargetModel.getSelfCounterCustomerCode() == null)
                    {
                        LOG.info("Month wise target for retailer plan (dealer) for last month::" + "" + data.getCustomerCode() + " " + tempMonthYear + ":: differnce" + Math.abs(diff));
                        monthWiseAnnualTargetModel.setMonthTarget(Math.abs(diff));
                        modelService.save(monthWiseAnnualTargetModel);
                        LOG.info("After Assigning Target to last month for retailer plan (dealer)" + monthWiseAnnualTargetModel.getMonthYear() + " " + monthWiseAnnualTargetModel.getMonthTarget());
                        break;
                    }
                }

                retailerPlannedAnnualSalesModel.setMonthWiseAnnualTarget(monthWiseAnnualTargetModelsRet);
                modelService.save(retailerPlannedAnnualSalesModel);

                for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : retailerPlannedAnnualSalesModel.getMonthWiseAnnualTarget()) {
                    totalTargetForDealer += monthWiseAnnualTargetModel.getMonthTarget();
                }
                retailerPlannedAnnualSalesModel.setTotalTarget(totalTargetForDealer);

                if (data.getRetailerData() != null && !data.getRetailerData().isEmpty()) {
                    List<RetailerPlannedAnnualSalesDetailsModel> retailerDetailsList = new ArrayList<>();
                    //retailerDetailsList.addAll(retailerPlannedAnnualSalesModel.getListOfRetailersPlanned());
                    for (RetailerDetailsData retailerDatum : data.getRetailerData()) {
                        double sumOfMonthwiseTargetForRetailerDetails = 0.0;
                        double finalSumForRetailerDetails = 0.0;

                        String tempMonthYearForRetailerDetails = "";
                        double tempMonthTargetForRetailerDetails = 0.0;
                        // if (retailerDetailsList != null && !retailerDetailsList.isEmpty()) {
                        //List<RetailerPlannedAnnualSalesDetailsModel> retailerDetails = retailerDetailsList.stream().filter(r -> r.getCustomerCode().equals(retailerDatum.getCustomerCode())).collect(Collectors.toList());
                            /*if (CollectionUtils.isNotEmpty(retailerDetails)) {
                                //  error in case of same customer with same product
                                ErrorWsDTO error = getError(retailerDatum.getCustomerName(), RETAILER_CODE_EXISTING.concat(" ").concat("For" + " " + data.getCustomerCode()), AmbiguousIdentifierException.class.getName());
                                errorWsDTOList.add(error);
                            }*/ //else {
                        double totalTargetForAllRetailers = 0.0;
                        RetailerPlannedAnnualSalesDetailsModel retailerPlannedAnnualSalesDetailsModel = validatePlannedRetailersDetails(retailerDatum.getCustomerCode(), sclUser, subAreaMaster.getPk().toString());
                        retailerPlannedAnnualSalesDetailsModel.setCustomerCode(retailerDatum.getCustomerCode());
                        retailerPlannedAnnualSalesDetailsModel.setCustomerName(retailerDatum.getCustomerName());
                        retailerPlannedAnnualSalesDetailsModel.setCurrentYearSales(retailerDatum.getCySales());
                        retailerPlannedAnnualSalesDetailsModel.setCustomerPotential(retailerDatum.getCustomerPotential() != null ? retailerDatum.getCustomerPotential() : 0.0);
                        retailerPlannedAnnualSalesDetailsModel.setPlannedYearSales(retailerDatum.getPlanSales());
                        retailerPlannedAnnualSalesDetailsModel.setTotalTarget(retailerDatum.getPlanSales());
                        retailerPlannedAnnualSalesDetailsModel.setSubAreaMaster(subAreaMaster);
                        retailerPlannedAnnualSalesDetailsModel.setSalesOfficer(sclUser);
                        retailerPlannedAnnualSalesDetailsModel.setErpCustomerNo(retailerDatum.getErpCustomerNo());
                        retailerPlannedAnnualSalesDetailsModel.setBrand(baseSite);
                        totalTargetForAllRetailers += retailerDatum.getPlanSales();


                        List<List<Object>> monthSplitupForRetailerDetailPlannedAnnualSales = salesPlanningDao.getMonthSplitupFormDealerRevisedAnnualSales(data.getCustomerCode(), sclUser, subAreaMaster.getPk().toString());
                        finalSumOfRetailerQuantity += monthSplitupForRetailerDetailPlannedAnnualSales.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects1 -> objects1.size() > 0 ? (Double) objects1.get(1) : 0.0).sum();
                        double finalSumOfQuantityRet = finalSumOfRetailerQuantity;

                        Map<String, String> monthNameMapForRetailerDetails = new LinkedHashMap<>();
                        monthNameMapForRetailerDetails.put("4", "Apr");
                        monthNameMapForRetailerDetails.put("5", "May");
                        monthNameMapForRetailerDetails.put("6", "Jun");
                        monthNameMapForRetailerDetails.put("7", "Jul");
                        monthNameMapForRetailerDetails.put("8", "Aug");
                        monthNameMapForRetailerDetails.put("9", "Sep");
                        monthNameMapForRetailerDetails.put("10", "Oct");
                        monthNameMapForRetailerDetails.put("11", "Nov");
                        monthNameMapForRetailerDetails.put("12", "Dec");
                        monthNameMapForRetailerDetails.put("1", "Jan");
                        monthNameMapForRetailerDetails.put("2", "Feb");
                        monthNameMapForRetailerDetails.put("3", "Mar");

                        Map<String, String> resultsForRetailerDetails = new LinkedHashMap<>();
                        resultsForRetailerDetails.put("4", "0");
                        resultsForRetailerDetails.put("5", "0");
                        resultsForRetailerDetails.put("6", "0");
                        resultsForRetailerDetails.put("7", "0");
                        resultsForRetailerDetails.put("8", "0");
                        resultsForRetailerDetails.put("9", "0");
                        resultsForRetailerDetails.put("10", "0");
                        resultsForRetailerDetails.put("11", "0");
                        resultsForRetailerDetails.put("12", "0");
                        resultsForRetailerDetails.put("1", "0");
                        resultsForRetailerDetails.put("2", "0");
                        resultsForRetailerDetails.put("3", "0");

                        for (List<Object> monthSplitUp : monthSplitupForRetailerDetailPlannedAnnualSales) {
                            String monthYear = (String) monthSplitUp.get(0);
                            String[] split = monthYear.split("-");
                            String monthName = split[0];
                            String keyVal = null;
                            Double qty = (Double) monthSplitUp.get(1);
                            for (Map.Entry<String, String> mapEntries1 : monthNameMapForRetailerDetails.entrySet()) {
                                if (mapEntries1.getValue().equalsIgnoreCase(monthName)) {
                                    keyVal = mapEntries1.getKey();
                                    LOG.info("Retailer Details split up mapEntries1 :" + keyVal);
                                }
                            }
                            for (Map.Entry<String, String> mapEntries2 : resultsForRetailerDetails.entrySet()) {
                                if (mapEntries2.getKey().equalsIgnoreCase(keyVal)) {
                                    mapEntries2.setValue(String.valueOf(qty));
                                    LOG.info("Retailer Details split up mapEntries2 :" + mapEntries2.getValue());
                                }
                            }
                        }

                        for (Map.Entry<String, String> resultsLog : resultsForRetailerDetails.entrySet()) {
                            LOG.info("resultsLog for retailer details:" + "key:" + resultsLog.getKey() + "value:" + resultsLog.getValue());
                        }

                        List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetModelsforRetailers = new ArrayList();
                        for (Map.Entry<String, String> listOfMonthAndQty : resultsForRetailerDetails.entrySet()) {
                            String key = listOfMonthAndQty.getKey();
                            String value = listOfMonthAndQty.getValue();
                            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel = validateRetailerMonthWiseDetails(data.getCustomerCode(), retailerDatum.getCustomerCode(), subAreaMaster.getPk().toString(), sclUser, key, value);
                            monthWiseAnnualTargetModel.setCustomerCode(data.getCustomerCode());
                            monthWiseAnnualTargetModel.setRetailerCode(retailerDatum.getCustomerCode());
                            monthWiseAnnualTargetModel.setSubAreaMaster(subAreaMaster);
                            monthWiseAnnualTargetModel.setSalesOfficer(sclUser);
                            monthWiseAnnualTargetModel.setBrand(baseSite);
                            if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 12) {
                                StringBuilder str = new StringBuilder();
                                if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 3) {
                                    monthWiseAnnualTargetModel.setMonthYear(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear() + 1))));
                                }
                                if (Integer.parseInt(key) >= 4 && Integer.parseInt(key) <= 12) {
                                    monthWiseAnnualTargetModel.setMonthYear(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear()))));
                                }
                                modelService.save(monthWiseAnnualTargetModel);
                            }

                            LOG.info("finalSumOfQuantityRet Retailer Details :" + finalSumOfQuantityRet);
                            LOG.info("key for Retailer Details=" + key);
                            LOG.info("value for Retailer Details =" + value);

                            if (value != null && finalSumOfQuantityRet != 0.0) {
                                perMonthPercentage = ((Double.parseDouble(value) / finalSumOfQuantityRet) * 100);
                                monthWiseAnnualTargetModel.setPerMonthPercentage(perMonthPercentage);
                            } else {
                                monthWiseAnnualTargetModel.setPerMonthPercentage(0.0);
                            }
                            modelService.save(monthWiseAnnualTargetModel);

                            if (retailerDatum.getPlanSales() != 0.0) {
                                if (monthSplitupForRetailerDetailPlannedAnnualSales == null || monthSplitupForRetailerDetailPlannedAnnualSales.isEmpty()) {
                                    if (monthWiseAnnualTargetModel.getMonthYear().startsWith("Mar")) {
                                        LOG.info("inside if key is equal to 3 for retailer details" + monthWiseAnnualTargetModel.getMonthYear());
                                        monthWiseAnnualTargetModel.setMonthTarget(retailerDatum.getPlanSales());
                                        LOG.info("month wise annual target for retailer details" + monthWiseAnnualTargetModel.getMonthTarget());
                                    } else {
                                        monthWiseAnnualTargetModel.setMonthTarget(0.0);
                                    }
                                } else {
                                    double monthwiseTarget = 0.0;
                                    monthwiseTarget = Math.round((retailerDatum.getPlanSales() * perMonthPercentage) / 100);
                                    LOG.info("retailer code ::" + retailerDatum.getCustomerCode() + " " + "Plan Target ::" + retailerDatum.getPlanSales());
                                    monthWiseAnnualTargetModel.setMonthTarget(monthwiseTarget);
                                    LOG.info("Month wise target for retailer details ::" + " " + retailerDatum.getCustomerCode() + " " + "Month Year" + monthWiseAnnualTargetModel.getMonthYear() + " " + monthWiseAnnualTargetModel.getMonthTarget());
                                    sumOfMonthwiseTargetForRetailerDetails += monthwiseTarget;
                                    if (monthwiseTarget > 0.0) {
                                        tempMonthTargetForRetailerDetails = monthwiseTarget;
                                        tempMonthYearForRetailerDetails = monthWiseAnnualTargetModel.getMonthYear();
                                    }
                                }
                            } else {
                                monthWiseAnnualTargetModel.setMonthTarget(0.0);
                            }

                            monthWiseAnnualTargetModelsforRetailers.add(monthWiseAnnualTargetModel);
                            modelService.save(monthWiseAnnualTargetModel);
                        }
                        double finalMonthWiseTargetForRetailerDetails = 0.0, diffForRetailerDetails = tempMonthTargetForRetailerDetails;
                        if (retailerDatum.getPlanSales() > sumOfMonthwiseTargetForRetailerDetails) {
                            finalMonthWiseTargetForRetailerDetails = retailerDatum.getPlanSales() - sumOfMonthwiseTargetForRetailerDetails;
                            diffForRetailerDetails = tempMonthTargetForRetailerDetails + finalMonthWiseTargetForRetailerDetails;
                        } else if (retailerDatum.getPlanSales() < sumOfMonthwiseTargetForRetailerDetails) {
                            finalMonthWiseTargetForRetailerDetails = sumOfMonthwiseTargetForRetailerDetails - retailerDatum.getPlanSales();
                            diffForRetailerDetails = tempMonthTargetForRetailerDetails - finalMonthWiseTargetForRetailerDetails;
                        }
                        for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : monthWiseAnnualTargetModelsforRetailers) {
                            if (monthWiseAnnualTargetModel.getMonthYear().equals(tempMonthYearForRetailerDetails) && retailerDatum.getCustomerCode().equals(monthWiseAnnualTargetModel.getRetailerCode()) && data.getCustomerCode().equals(monthWiseAnnualTargetModel.getCustomerCode()) && monthWiseAnnualTargetModel.getProductCode() == null && monthWiseAnnualTargetModel.getSelfCounterCustomerCode() == null) {
                                LOG.info("Month wise target for retailer details for last month::" + "" + data.getCustomerCode() + " " + tempMonthYear + ":: differnce" + Math.abs(diffForRetailerDetails));
                                monthWiseAnnualTargetModel.setMonthTarget(Math.abs(diffForRetailerDetails));
                                modelService.save(monthWiseAnnualTargetModel);
                                LOG.info("After Assigning Target to last month for retailer details" + monthWiseAnnualTargetModel.getMonthYear() + " " + monthWiseAnnualTargetModel.getMonthTarget());
                                break;
                            }
                        }

                        retailerPlannedAnnualSalesDetailsModel.setMonthWiseAnnualTarget(monthWiseAnnualTargetModelsforRetailers);
                        for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : retailerPlannedAnnualSalesDetailsModel.getMonthWiseAnnualTarget()) {
                            totalTargetForRetailerDetails += monthWiseAnnualTargetModel.getMonthTarget();
                        }
                        retailerPlannedAnnualSalesDetailsModel.setTotalTarget(totalTargetForRetailerDetails);

                        modelService.save(retailerPlannedAnnualSalesDetailsModel);
                        retailerDetailsList.add(retailerPlannedAnnualSalesDetailsModel);
                        modelService.save(retailerPlannedAnnualSalesDetailsModel);

                    }
                    retailerPlannedAnnualSalesModel.setListOfRetailersPlanned(retailerDetailsList);
                }

                //self counter
                double sumOfMonthwiseTargetForSelfCounter=0.0;
                double finalSumForSelfCounter=0.0;

                String tempMonthYearForSelfCounter = "";
                double tempMonthTargetForSelfCounter = 0.0;

                SelfCounterSaleDetailsModel selfCounterSaleDetailsModel = validateSelfCounterSaleModel(data.getCustomerCode(), subAreaMaster.getPk().toString(), sclUser);
                SelfCounterSaleData selfCounterSaleData = data.getSelfCounterSale();

                selfCounterSaleDetailsModel.setCustomerCode(selfCounterSaleData.getCustomerCode());
                selfCounterSaleDetailsModel.setCustomerName(selfCounterSaleData.getCustomerName());
                selfCounterSaleDetailsModel.setCustomerPotential(selfCounterSaleData.getCustomerPotential()!=null?selfCounterSaleData.getCustomerPotential():0.0);
                selfCounterSaleDetailsModel.setCySales(selfCounterSaleData.getCySales());
                selfCounterSaleDetailsModel.setPlanSales(selfCounterSaleData.getPlanSales());
                selfCounterSaleDetailsModel.setSubAreaMaster(subAreaMaster);
                selfCounterSaleDetailsModel.setSalesOfficer(sclUser);
                selfCounterSaleDetailsModel.setErpCustomerNo(selfCounterSaleData.getErpCustomerNo());
                selfCounterSaleDetailsModel.setBrand(baseSite);

                List<List<Object>> monthSplitupForSelfCounterAnnualSelfSales = salesPlanningDao.getMonthSplitupFormDealerRevisedAnnualSales(data.getCustomerCode(), sclUser, subAreaMaster.getPk().toString());
                finalSumOfSelfQuantity += monthSplitupForSelfCounterAnnualSelfSales.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToDouble(objects1 -> objects1.size() > 0 ? (Double) objects1.get(1) : 0.0).sum();
                double finalSumOfQuantitySelf = finalSumOfSelfQuantity;

                Map<String, String> monthNameMapForSelfCounter = new LinkedHashMap<>();
                monthNameMapForSelfCounter.put("4", "Apr");
                monthNameMapForSelfCounter.put("5", "May");
                monthNameMapForSelfCounter.put("6", "Jun");
                monthNameMapForSelfCounter.put("7", "Jul");
                monthNameMapForSelfCounter.put("8", "Aug");
                monthNameMapForSelfCounter.put("9", "Sep");
                monthNameMapForSelfCounter.put("10", "Oct");
                monthNameMapForSelfCounter.put("11", "Nov");
                monthNameMapForSelfCounter.put("12", "Dec");
                monthNameMapForSelfCounter.put("1", "Jan");
                monthNameMapForSelfCounter.put("2", "Feb");
                monthNameMapForSelfCounter.put("3", "Mar");

                Map<String, String> resultsforSelfCounterSale = new LinkedHashMap<>();
                resultsforSelfCounterSale.put("4", "0");
                resultsforSelfCounterSale.put("5", "0");
                resultsforSelfCounterSale.put("6", "0");
                resultsforSelfCounterSale.put("7", "0");
                resultsforSelfCounterSale.put("8", "0");
                resultsforSelfCounterSale.put("9", "0");
                resultsforSelfCounterSale.put("10", "0");
                resultsforSelfCounterSale.put("11", "0");
                resultsforSelfCounterSale.put("12", "0");
                resultsforSelfCounterSale.put("1", "0");
                resultsforSelfCounterSale.put("2", "0");
                resultsforSelfCounterSale.put("3", "0");


                for (List<Object> monthSplitUp : monthSplitupForSelfCounterAnnualSelfSales) {
                    String monthYear = (String) monthSplitUp.get(0);
                    String[] split = monthYear.split("-");
                    String monthName = split[0];
                    Double qty = (Double) monthSplitUp.get(1);
                    String keyVal=null;
                    for (Map.Entry<String, String> mapEntries1 : monthNameMapForSelfCounter.entrySet()) {
                        if (mapEntries1.getValue().equalsIgnoreCase(monthName)) {
                            keyVal=mapEntries1.getKey();
                            LOG.info("self counter split up mapEntries1 :" + keyVal);
                        }
                    }
                    for (Map.Entry<String, String> mapEntries2 : resultsforSelfCounterSale.entrySet()) {
                        if (mapEntries2.getKey().equalsIgnoreCase(keyVal)) {
                            mapEntries2.setValue(String.valueOf(qty));
                            LOG.info("self counter split up mapEntries2 :" + mapEntries2.getValue());
                        }
                    }
                }

                for (Map.Entry<String, String> resultsLog : resultsforSelfCounterSale.entrySet()) {
                    LOG.info("resultsLog for self counter :" +  "key:" + resultsLog.getKey() + "value:" + resultsLog.getValue());
                }

                List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetModelsforSelfCounter = new ArrayList();
                for (Map.Entry<String, String> listOfMonthAndQty : resultsforSelfCounterSale.entrySet()) {
                    String key = listOfMonthAndQty.getKey();
                    String value = listOfMonthAndQty.getValue();
                    MonthWiseAnnualTargetModel monthWiseAnnualTargetModel = validateSelfCounterMonthWiseDetailsModel(data.getCustomerCode(), selfCounterSaleData.getCustomerCode(), subAreaMaster.getPk().toString(), sclUser, key, value);
                    monthWiseAnnualTargetModel.setCustomerCode(selfCounterSaleData.getCustomerCode());
                    monthWiseAnnualTargetModel.setSelfCounterCustomerCode(selfCounterSaleData.getCustomerCode());
                    monthWiseAnnualTargetModel.setSubAreaMaster(subAreaMaster);
                    monthWiseAnnualTargetModel.setSalesOfficer(sclUser);
                    monthWiseAnnualTargetModel.setBrand(baseSite);

                    if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 12) {
                        StringBuilder str = new StringBuilder();
                        if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 3) {
                            monthWiseAnnualTargetModel.setMonthYear(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear() + 1))));
                        }
                        if (Integer.parseInt(key) >= 4 && Integer.parseInt(key) <= 12) {
                            monthWiseAnnualTargetModel.setMonthYear(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear()))));
                        }
                        modelService.save(monthWiseAnnualTargetModel);
                    }

                    LOG.info("finalSumOfQuantitySelf self counter :"+finalSumOfQuantitySelf);
                    LOG.info("key for self counter=" + key);
                    LOG.info("value for self counter =" +value);

                    if (value != null && finalSumOfQuantitySelf != 0.0) {
                        perMonthPercentage = ((Double.parseDouble(value) / finalSumOfQuantitySelf) * 100);
                        LOG.info("Retailer Planning (self counter) :: " + data.getCustomerCode() + "per month percentage ::" + perMonthPercentage);
                        monthWiseAnnualTargetModel.setPerMonthPercentage(perMonthPercentage);
                    } else {
                        monthWiseAnnualTargetModel.setPerMonthPercentage(0.0);
                    }

                    modelService.save(monthWiseAnnualTargetModel);

                    if (selfCounterSaleData.getPlanSales() != 0.0) {
                        if (monthSplitupForSelfCounterAnnualSelfSales == null || monthSplitupForSelfCounterAnnualSelfSales.isEmpty()) {
                            if (monthWiseAnnualTargetModel.getMonthYear().startsWith("Mar")) {
                                LOG.info("inside if key is equal to 3 for self counter" + monthWiseAnnualTargetModel.getMonthYear());
                                monthWiseAnnualTargetModel.setMonthTarget(selfCounterSaleData.getPlanSales());
                                LOG.info("month wise annual target for retailer (self counter)" + monthWiseAnnualTargetModel.getMonthTarget());
                            } else {
                                monthWiseAnnualTargetModel.setMonthTarget(0.0);
                            }
                        } else {
                            double monthwiseTarget = 0.0;
                            monthwiseTarget = Math.round((selfCounterSaleData.getPlanSales() * perMonthPercentage) / 100);
                            LOG.info("self counter code ::" + selfCounterSaleData.getCustomerCode() + " " + "Plan Target ::" + selfCounterSaleData.getPlanSales());
                            monthWiseAnnualTargetModel.setMonthTarget(monthwiseTarget);
                            LOG.info("Month wise target for self counter ::" + " " + data.getCustomerCode() + " " + "Month Year" + monthWiseAnnualTargetModel.getMonthYear() + " " + monthWiseAnnualTargetModel.getMonthTarget());
                            sumOfMonthwiseTargetForSelfCounter += monthwiseTarget;
                            if (monthwiseTarget > 0.0) {
                                tempMonthTargetForSelfCounter = monthwiseTarget;
                                tempMonthYearForSelfCounter = monthWiseAnnualTargetModel.getMonthYear();
                            }
                        }
                    } else {
                        monthWiseAnnualTargetModel.setMonthTarget(0.0);
                    }

                    monthWiseAnnualTargetModelsforSelfCounter.add(monthWiseAnnualTargetModel);
                    modelService.save(monthWiseAnnualTargetModel);
                }

                double finalMonthWiseTargetForSelfCounter=0.0, diffForSelfCounter = tempMonthTargetForSelfCounter;
                if(selfCounterSaleData.getPlanSales() > sumOfMonthwiseTargetForSelfCounter)
                {
                    finalMonthWiseTargetForSelfCounter = selfCounterSaleData.getPlanSales() - sumOfMonthwiseTargetForSelfCounter;
                    diffForSelfCounter = tempMonthTargetForSelfCounter + finalMonthWiseTargetForSelfCounter;
                }
                else if(selfCounterSaleData.getPlanSales() < sumOfMonthwiseTargetForSelfCounter)
                {
                    finalMonthWiseTargetForSelfCounter =  sumOfMonthwiseTargetForSelfCounter - selfCounterSaleData.getPlanSales() ;
                    diffForSelfCounter = tempMonthTargetForSelfCounter - finalMonthWiseTargetForSelfCounter;
                }
                for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : monthWiseAnnualTargetModelsforSelfCounter) {
                    if (monthWiseAnnualTargetModel.getMonthYear().equals(tempMonthYearForSelfCounter) && selfCounterSaleData.getCustomerCode().equals(monthWiseAnnualTargetModel.getSelfCounterCustomerCode()) && data.getCustomerCode().equals(monthWiseAnnualTargetModel.getCustomerCode()) && monthWiseAnnualTargetModel.getProductCode() == null && monthWiseAnnualTargetModel.getRetailerCode() == null)
                    {
                        LOG.info("Month wise target for self counter for last month::" + "" + selfCounterSaleData.getCustomerCode() + " " + tempMonthYear + ":: differnce" + Math.abs(diffForSelfCounter));
                        monthWiseAnnualTargetModel.setMonthTarget(Math.abs(diffForSelfCounter));
                        modelService.save(monthWiseAnnualTargetModel);
                        LOG.info("After Assigning Target to last month for self counter" + monthWiseAnnualTargetModel.getMonthYear() + " " + monthWiseAnnualTargetModel.getMonthTarget());
                        break;
                    }
                }

                selfCounterSaleDetailsModel.setMonthWiseAnnualTarget(monthWiseAnnualTargetModelsforSelfCounter);

                /*for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : selfCounterSaleDetailsModel.getMonthWiseAnnualTarget()) {
                    totalTargetForSelfCounter +=monthWiseAnnualTargetModel.getMonthTarget();
                }*/

                totalTargetForSelfCounter = totalTargetForDealer - totalTargetForRetailerDetails;
                selfCounterSaleDetailsModel.setTotalTarget(Math.abs(totalTargetForSelfCounter));
                modelService.save(selfCounterSaleDetailsModel);
                retailerPlannedAnnualSalesModel.setDealerSelfCounterSale(selfCounterSaleDetailsModel);
                modelService.save(retailerPlannedAnnualSalesModel);

                retailerPlannedAnnualSalesModel.setAnnualSale(annualSalesModel);
                modelService.save(retailerPlannedAnnualSalesModel);
            }
            modelService.save(annualSalesModel);
        }
        errorListWsDTO.setErrors(errorWsDTOList);
        return errorListWsDTO;
    }

    @Override
    public ErrorListWsDTO submitFinalizeAnnualSalesTargetSettingForDealers(AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData) {
        final ErrorListWsDTO errorListWsDTO = new ErrorListWsDTO();
        final List<ErrorWsDTO> errorWsDTOList = new ArrayList<>();
        BaseSiteModel baseSite = baseSiteService.getCurrentBaseSite();
        SubAreaMasterModel subAreaMaster=territoryManagementService.getTerritoryById(annualSalesMonthWiseTargetListData.getSubAreaId());
        if (annualSalesMonthWiseTargetListData.getAnnualSalesMonthWiseTargetData() != null && !annualSalesMonthWiseTargetListData.getAnnualSalesMonthWiseTargetData().isEmpty()) {
            List<DealerRevisedAnnualSalesModel> dealerRevisedAnnualSalesModelList = new ArrayList<>();
            SclUserModel sclUser = (SclUserModel) getUserService().getCurrentUser();
            AnnualSalesModel annualSalesModel = validateAnnualSalesModel(sclUser, subAreaMaster.getPk().toString(), baseSite);
            if (annualSalesModel != null) {
                if (annualSalesModel.getSubAreaMasterList() != null && !annualSalesModel.getSubAreaMasterList().isEmpty()) {
                    List<SubAreaMasterModel> subAreaListModelsForAnnual = new ArrayList<>(annualSalesModel.getSubAreaMasterList());
                    if(annualSalesModel.getSubAreaMasterList().contains(subAreaMaster))
                    {
                        annualSalesModel.setSalesOfficer(sclUser);
                        annualSalesModel.setDealerRevisedTotalPlanSales(annualSalesMonthWiseTargetListData.getTotalPlanSales() != null ? annualSalesMonthWiseTargetListData.getTotalPlanSales() : 0.0);
                        annualSalesModel.setDealerRevisedTotalCySales(annualSalesMonthWiseTargetListData.getTotalCurrentYearSales() != null ? annualSalesMonthWiseTargetListData.getTotalCurrentYearSales() : 0.0);
                        annualSalesModel.setIsAnnualSalesRevised(true);
                        annualSalesModel.setBrand(baseSite);
                        subAreaListModelsForAnnual.add(subAreaMaster);
                        annualSalesModel.setSubAreaMasterList(subAreaListModelsForAnnual);
                    }
                    else
                    {
                        annualSalesModel.setSalesOfficer(sclUser);
                        annualSalesModel.setDealerRevisedTotalPlanSales(annualSalesMonthWiseTargetListData.getTotalPlanSales() != null ? annualSalesMonthWiseTargetListData.getTotalPlanSales() : 0.0);
                        annualSalesModel.setDealerRevisedTotalCySales(annualSalesMonthWiseTargetListData.getTotalCurrentYearSales() != null ? annualSalesMonthWiseTargetListData.getTotalCurrentYearSales() : 0.0);
                        annualSalesModel.setIsAnnualSalesRevised(true);
                        annualSalesModel.setBrand(baseSite);
                        subAreaListModelsForAnnual.add(subAreaMaster);
                        annualSalesModel.setSubAreaMasterList(subAreaListModelsForAnnual);
                    }
                } else {
                    List<SubAreaMasterModel> subAreaListModelsForAnnual = new ArrayList<>();
                    annualSalesModel.setSalesOfficer(sclUser);
                    annualSalesModel.setDealerRevisedTotalPlanSales(annualSalesMonthWiseTargetListData.getTotalPlanSales() != null ? annualSalesMonthWiseTargetListData.getTotalPlanSales() : 0.0);
                    annualSalesModel.setDealerRevisedTotalCySales(annualSalesMonthWiseTargetListData.getTotalCurrentYearSales() != null ? annualSalesMonthWiseTargetListData.getTotalCurrentYearSales() : 0.0);
                    annualSalesModel.setIsAnnualSalesRevised(true);
                    subAreaListModelsForAnnual.add(subAreaMaster);
                    annualSalesModel.setSubAreaMasterList(subAreaListModelsForAnnual);
                }
            }
            annualSalesModel.setSalesOfficer(sclUser);
            annualSalesModel.setFinancialYear(findNextFinancialYear());
            if(subAreaMaster != null) {
                annualSalesModel.setSubAreaMaster(subAreaMaster);
                DistrictMasterModel district = subAreaMaster.getDistrictMaster();
                if(district!=null) {
                    annualSalesModel.setDistrictMaster(district);
                    RegionMasterModel region = district.getRegion();
                    if(region!=null) {
                        annualSalesModel.setRegionMaster(region);
                    }
                    StateMasterModel state=region.getState();
                    if(state!=null)
                    {
                        annualSalesModel.setStateMaster(state);
                    }
                }
            }

            //totaltarget for dealers and monthwise
            if (annualSalesMonthWiseTargetListData.getTotalTargetForAllDealersMonthWise() != null && !annualSalesMonthWiseTargetListData.getTotalTargetForAllDealersMonthWise().isEmpty()) {
                final Map<String, Double> monthTargetMapForDealers = new HashMap<>();
                annualSalesMonthWiseTargetListData.getTotalTargetForAllDealersMonthWise().stream().forEach(totalMonthlyTargetData ->
                        monthTargetMapForDealers.put(totalMonthlyTargetData.getMonthYear(), totalMonthlyTargetData.getTotalTargetforMonth()));
                annualSalesModel.setTotalRevisedTargetForAllDealersMonthWise(monthTargetMapForDealers);
            }
            annualSalesModel.setTotalRevisedTargetForAllDealers(annualSalesMonthWiseTargetListData.getTotalTargetForAllDealers());
            modelService.save(annualSalesModel);

            if (annualSalesMonthWiseTargetListData.getAnnualSalesMonthWiseTargetData() != null && !annualSalesMonthWiseTargetListData.getAnnualSalesMonthWiseTargetData().isEmpty()) {
                for (AnnualSalesMonthWiseTargetData data : annualSalesMonthWiseTargetListData.getAnnualSalesMonthWiseTargetData()) {
                    if (data.getIsAnnualSalesRevised()!=null && data.getIsAnnualSalesRevised().equals(true)) {
                        String financialYear = findNextFinancialYear();
                        DealerRevisedAnnualSalesModel dealerRevisedAnnualSalesModel = validateDealerRevisedAnnualSalesModel(data.getCustomerCode(), subAreaMaster.getPk().toString(), sclUser, financialYear);
                        dealerRevisedAnnualSalesModel.setCustomerCode(data.getCustomerCode());
                        dealerRevisedAnnualSalesModel.setCustomerName(data.getCustomerName());
                        dealerRevisedAnnualSalesModel.setCustomerPotential(data.getCustomerPotential() != null ? data.getCustomerPotential() : 0.0);
                        dealerRevisedAnnualSalesModel.setCustomerCategory(CounterType.DEALER);
                        dealerRevisedAnnualSalesModel.setFinancialYear(findNextFinancialYear());
                        dealerRevisedAnnualSalesModel.setStatus("Finalized");
                        dealerRevisedAnnualSalesModel.setSubAreaMaster(subAreaMaster);
                        dealerRevisedAnnualSalesModel.setTotalTarget(data.getTotalTarget());
                        dealerRevisedAnnualSalesModel.setBrand(baseSite);
                        if(subAreaMaster != null) {
                            DistrictMasterModel district = subAreaMaster.getDistrictMaster();
                            if (district != null) {
                                dealerRevisedAnnualSalesModel.setDistrictMaster(district);
                                RegionMasterModel region = district.getRegion();
                                if (region != null) {
                                    dealerRevisedAnnualSalesModel.setRegionMaster(region);
                                    StateMasterModel state = region.getState();
                                    if (state != null) {
                                        dealerRevisedAnnualSalesModel.setStateMaster(state);
                                    }
                                }
                            }
                        }

                        List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetModels = new ArrayList<>();
                        for (MonthWiseTargetData monthWiseTargetData : data.getMonthWiseTarget()) {
                            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel = validateDealerRevisedMonthWiseTargetModel(data.getCustomerCode(), subAreaMaster.getPk().toString(), monthWiseTargetData.getMonthYear(), sclUser, data.getIsAnnualSalesRevised());
                            monthWiseAnnualTargetModel.setMonthTarget(monthWiseTargetData.getMonthTarget());
                            monthWiseAnnualTargetModel.setMonthYear(monthWiseTargetData.getMonthYear());
                            monthWiseAnnualTargetModel.setCustomerCode(data.getCustomerCode());
                            monthWiseAnnualTargetModel.setIsAnnualSalesRevisedForDealer(true);
                            monthWiseAnnualTargetModel.setDealerRevisedAnnualSales(dealerRevisedAnnualSalesModel);
                            monthWiseAnnualTargetModel.setSubAreaMaster(subAreaMaster);
                            monthWiseAnnualTargetModel.setSalesOfficer(sclUser);
                            monthWiseAnnualTargetModel.setBrand(baseSite);
                            if(subAreaMaster != null) {
                                DistrictMasterModel district = subAreaMaster.getDistrictMaster();
                                if (district != null) {
                                    monthWiseAnnualTargetModel.setDistrictMaster(district);
                                    RegionMasterModel region = district.getRegion();
                                    if (region != null) {
                                        monthWiseAnnualTargetModel.setRegionMaster(region);
                                        StateMasterModel state = region.getState();
                                        if (state != null) {
                                            monthWiseAnnualTargetModel.setStateMaster(state);
                                        }
                                    }
                                }
                            }
                            monthWiseAnnualTargetModels.add(monthWiseAnnualTargetModel);
                            modelService.save(monthWiseAnnualTargetModel);
                        }
                        dealerRevisedAnnualSalesModel.setMonthWiseAnnualTarget(monthWiseAnnualTargetModels);

                        if (data.getSkuDataList() != null && !data.getSkuDataList().isEmpty()) {
                            List<ProductModel> skuList = new ArrayList<>();
                           // skuList.addAll(dealerRevisedAnnualSalesModel.getListOfSkus());
                            for (SKUData skuData : data.getSkuDataList()) {
                                //if (skuList != null && !skuList.isEmpty()) {
                                    List<ProductModel> sku = skuList.stream().filter(p -> p.getCode().equals(skuData.getProductCode())).collect(Collectors.toList());
                                    /*if (CollectionUtils.isNotEmpty(sku)) {
                                        //  error in case of same customer with same product
                                        ErrorWsDTO error = getError(skuData.getProductName(), SKU_CODE_EXISTING_FINALIZE.concat(" ").concat("For" + " " + data.getCustomerCode()), AmbiguousIdentifierException.class.getName());
                                        errorWsDTOList.add(error);
                                        //return false;
                                    }*/
                                        List<ProductSaleModel> productSaleModelList = new ArrayList<>();
                                        ProductModel productModel = productService.getProductForCode(skuData.getProductCode());
                                        //ProductModel productModel = sclProductUtility.getProductByCatalogVersion(skuData.getProductCode());
                                        ProductSaleModel productSaleModel = validateRevisedProductSalesModel(data.getCustomerCode(), skuData.getProductCode(), subAreaMaster.getPk().toString(), sclUser,annualSalesModel.getIsAnnualSalesRevised());
                                        productSaleModel.setProductCode(skuData.getProductCode());
                                        productSaleModel.setProductName(skuData.getProductName());
                                        productSaleModel.setProductGrade(productModel.getGrade());
                                        productSaleModel.setProductPackaging(productModel.getPackagingCondition());
                                        productSaleModel.setProductPackType(productModel.getBagType());
                                        productSaleModel.setCustomerCode(data.getCustomerCode());
                                        productSaleModel.setTotalTarget(skuData.getTotalTarget());
                                        productSaleModel.setSubAreaMaster(subAreaMaster);
                                        productSaleModel.setSalesOfficer(sclUser);
                                        productSaleModel.setIsAnnualSalesRevisedForDealer(true);
                                        productSaleModel.setBrand(baseSite);
                                        productSaleModel.setPremium(skuData.getPremium());
                                if(subAreaMaster != null) {
                                    DistrictMasterModel district = subAreaMaster.getDistrictMaster();
                                    if (district != null) {
                                        productSaleModel.setDistrictMaster(district);
                                        RegionMasterModel region = district.getRegion();
                                        if (region != null) {
                                            productSaleModel.setRegionMaster(region);
                                            StateMasterModel state = region.getState();
                                            if (state != null) {
                                                productSaleModel.setStateMaster(state);
                                            }
                                        }
                                    }
                                }
                                        modelService.save(productSaleModel);
                                        productSaleModelList.add(productSaleModel);
                                        productModel.setProductSale(productSaleModelList);

                                        List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetModelsforSKU = new ArrayList();

                                        if (skuData.getMonthWiseSkuTarget() != null && !skuData.getMonthWiseSkuTarget().isEmpty()) {
                                            for (MonthWiseTargetData monthWiseTargetData : skuData.getMonthWiseSkuTarget()) {

                                                MonthWiseAnnualTargetModel monthWiseAnnualTargetModel = validateDealerRevisedMonthWiseSkuDetails(subAreaMaster.getPk().toString(), data.getCustomerCode(), skuData.getProductCode(), monthWiseTargetData.getMonthYear(), sclUser, data.getIsAnnualSalesRevised());
                                                monthWiseAnnualTargetModel.setCustomerCode(data.getCustomerCode());
                                                monthWiseAnnualTargetModel.setProductCode(skuData.getProductCode());
                                                ProductModel product = productService.getProductForCode(skuData.getProductCode());
                                                if(!Objects.isNull(product)) {
                                                    monthWiseAnnualTargetModel.setProductGrade(product.getGrade());
                                                    monthWiseAnnualTargetModel.setProductPackaging(product.getPackagingCondition());
                                                    monthWiseAnnualTargetModel.setProductBagType(product.getBagType());
                                                }
                                                monthWiseAnnualTargetModel.setMonthYear(monthWiseTargetData.getMonthYear());
                                                monthWiseAnnualTargetModel.setMonthTarget(monthWiseTargetData.getMonthTarget());
                                                monthWiseAnnualTargetModel.setSubAreaMaster(subAreaMaster);
                                                monthWiseAnnualTargetModel.setSalesOfficer(sclUser);
                                                monthWiseAnnualTargetModel.setIsAnnualSalesRevisedForDealer(true);
                                                monthWiseAnnualTargetModel.setBrand(baseSite);
                                                monthWiseAnnualTargetModel.setPremium(skuData.getPremium());
                                                if(subAreaMaster != null) {
                                                    DistrictMasterModel district = subAreaMaster.getDistrictMaster();
                                                    if (district != null) {
                                                        monthWiseAnnualTargetModel.setDistrictMaster(district);
                                                        RegionMasterModel region = district.getRegion();
                                                        if (region != null) {
                                                            monthWiseAnnualTargetModel.setRegionMaster(region);
                                                            StateMasterModel state = region.getState();
                                                            if (state != null) {
                                                                monthWiseAnnualTargetModel.setStateMaster(state);
                                                            }
                                                        }
                                                    }
                                                }
                                                monthWiseAnnualTargetModelsforSKU.add(monthWiseAnnualTargetModel);
                                                modelService.save(monthWiseAnnualTargetModel);
                                            }
                                        }
                                        skuList.add(productModel);
                                        modelService.save(productModel);
                                }
                                dealerRevisedAnnualSalesModel.setListOfSkus(skuList);
                            }
                            dealerRevisedAnnualSalesModel.setAnnualSales(annualSalesModel);
                            modelService.save(dealerRevisedAnnualSalesModel);
                            dealerRevisedAnnualSalesModelList.add(dealerRevisedAnnualSalesModel);
                        }
                    }
            }
            annualSalesModel.setDealerRevisedAnnualSales(dealerRevisedAnnualSalesModelList);

            //TSM sent revised target and DO has finalize the target
            if(annualSalesModel.getActionPerformedBy()!=null)
            {
                annualSalesModel.setActionPerformedBy(null);
            }
            modelService.save(annualSalesModel);
            //calling sclworkflow- //targetSentToTSM
            String actionName="targetSentToTSM";
            SclWorkflowModel sclWorkflowModel = null;
            if(annualSalesModel.getSclWorkflow()==null)
            {
                sclWorkflowModel= sclWorkflowService.saveWorkflow(ASP_BOTTOM_UP_WORKFLOW, WorkflowStatus.START, WorkflowType.ANNUAL_SALES_PLANNING);
                sclWorkflowModel.setStatus(WorkflowStatus.START);
                modelService.save(sclWorkflowModel);
                annualSalesModel.setSclWorkflow(sclWorkflowModel);
                modelService.save(annualSalesModel);
            }
            else
            {
                sclWorkflowModel = annualSalesModel.getSclWorkflow();
            }

            /*if(annualSalesModel.getRevisedTarget()!=null && annualSalesModel.getRevisedTarget()!=0.0)
            {
                if(annualSalesModel.getTotalRevisedTargetForAllDealers()!=null && annualSalesModel.getTotalRevisedTargetForAllDealers()!=0.0) {
                    if (annualSalesModel.getRevisedTarget().equals(annualSalesModel.getTotalRevisedTargetForAllDealers())) {
                        annualSalesModel.setActionPerformed(WorkflowActions.AUTO_APPROVED);
                        annualSalesModel.setActionPerformedDate(new Date());
                        annualSalesModel.setTargetSentForRevision(false);
                        annualSalesModel.setIsTargetApproved(false);
                        SclWorkflowActionModel sclWorkflowActionModel = sclWorkflowService.saveWorkflowAction(sclWorkflowModel, "targetAutoApproved", baseSite, subAreaMaster, TerritoryLevels.DISTRICT);
                        if(sclWorkflowActionModel!=null)
                        {
                            if(annualSalesModel.getRevisedBy()!=null)
                            sclWorkflowService.updateWorkflowAction(sclWorkflowActionModel,annualSalesModel.getRevisedBy() ,WorkflowActions.AUTO_APPROVED,"Target has been auto approved for subarea" + subAreaMaster.getTaluka());
                        }
                        modelService.save(annualSalesModel);
                    }
                }
            }*/

                SclWorkflowActionModel sclWorkflowActionModel = sclWorkflowService.saveWorkflowAction(sclWorkflowModel, "targetSentToTSM", baseSite, subAreaMaster, TerritoryLevels.DISTRICT);
                LOG.info("SclWorkflow Action finalize api" + sclWorkflowActionModel);
                modelService.save(sclWorkflowActionModel);

         }
        errorListWsDTO.setErrors(errorWsDTOList);
        return errorListWsDTO;
    }

    @Override
    public ErrorListWsDTO submitFinalizeAnnualSalesTargetSettingForRetailers(AnnualSalesMonthWiseTargetListData listData) {
        final ErrorListWsDTO errorListWsDTO = new ErrorListWsDTO();
        final List<ErrorWsDTO> errorWsDTOList = new ArrayList<>();
        BaseSiteModel baseSite = baseSiteService.getCurrentBaseSite();
        if (listData.getAnnualSalesMonthWiseTargetData() != null && !listData.getAnnualSalesMonthWiseTargetData().isEmpty()) {
            SclUserModel sclUser = (SclUserModel) getUserService().getCurrentUser();
            SubAreaMasterModel subAreaMaster=territoryManagementService.getTerritoryById(listData.getSubAreaId());
            AnnualSalesModel annualSalesModel = validateAnnualSalesModel(sclUser, subAreaMaster.getPk().toString(), baseSite);
            if (annualSalesModel != null) {
                if (annualSalesModel.getSubAreaMasterList() != null && !annualSalesModel.getSubAreaMasterList().isEmpty()) {
                    List<SubAreaMasterModel> subAreaListModelsForAnnual = new ArrayList<>(annualSalesModel.getSubAreaMasterList());
                    if(annualSalesModel.getSubAreaMasterList().contains(subAreaMaster))
                    {
                        annualSalesModel.setSalesOfficer(sclUser);
                        annualSalesModel.setRetailerRevisedTotalCySales(listData.getTotalCurrentYearSales() != null ? listData.getTotalCurrentYearSales() : 0.0);
                        annualSalesModel.setRetailerRevisedTotalPlanSales(listData.getTotalPlanSales() != null ? listData.getTotalPlanSales() : 0.0);
                        annualSalesModel.setIsAnnualSalesRevisedForRetailer(true);
                        annualSalesModel.setBrand(baseSite);
                        subAreaListModelsForAnnual.add(subAreaMaster);
                        annualSalesModel.setSubAreaMasterList(subAreaListModelsForAnnual);
                    }
                    else
                    {
                        annualSalesModel.setSalesOfficer(sclUser);
                        annualSalesModel.setRetailerRevisedTotalCySales(listData.getTotalCurrentYearSales() != null ? listData.getTotalCurrentYearSales() : 0.0);
                        annualSalesModel.setRetailerRevisedTotalPlanSales(listData.getTotalPlanSales() != null ? listData.getTotalPlanSales() : 0.0);
                        annualSalesModel.setIsAnnualSalesRevisedForRetailer(true);
                        annualSalesModel.setBrand(baseSite);
                        subAreaListModelsForAnnual.add(subAreaMaster);
                        annualSalesModel.setSubAreaMasterList(subAreaListModelsForAnnual);
                    }
                } else {
                    List<SubAreaMasterModel> subAreaListModelsForAnnual = new ArrayList<>();
                    annualSalesModel.setSalesOfficer(sclUser);
                    annualSalesModel.setRetailerRevisedTotalCySales(listData.getTotalCurrentYearSales() != null ? listData.getTotalCurrentYearSales() : 0.0);
                    annualSalesModel.setRetailerRevisedTotalPlanSales(listData.getTotalPlanSales() != null ? listData.getTotalPlanSales() : 0.0);
                    annualSalesModel.setIsAnnualSalesRevisedForRetailer(true);
                    annualSalesModel.setBrand(baseSite);
                    subAreaListModelsForAnnual.add(subAreaMaster);
                    annualSalesModel.setSubAreaMasterList(subAreaListModelsForAnnual);
                }
            }

            annualSalesModel.setFinancialYear(findNextFinancialYear());
            if(subAreaMaster != null) {
                annualSalesModel.setSubAreaMaster(subAreaMaster);
                DistrictMasterModel district = subAreaMaster.getDistrictMaster();
                if(district!=null) {
                    annualSalesModel.setDistrictMaster(district);
                    RegionMasterModel region = district.getRegion();
                    if(region!=null) {
                        annualSalesModel.setRegionMaster(region);
                    }
                    StateMasterModel state=region.getState();
                    if(state!=null)
                    {
                        annualSalesModel.setStateMaster(state);
                    }
                }
            }
            //totaltarget for dealers and monthwise
            if (listData.getTotalTargetForAllDealersMonthWise() != null && !listData.getTotalTargetForAllDealersMonthWise().isEmpty()) {
                final Map<String, Double> monthTargetMapForRetailers = new HashMap<>();
                listData.getTotalTargetForAllDealersMonthWise().stream().forEach(totalMonthlyTargetData ->
                        monthTargetMapForRetailers.put(totalMonthlyTargetData.getMonthYear(), totalMonthlyTargetData.getTotalTargetforMonth()));
                annualSalesModel.setTotalRevisedTargetForAllRetailersMonthWise(monthTargetMapForRetailers);
            }
            annualSalesModel.setTotalRevisedTargetForAllRetailers(listData.getTotalTargetForAllDealers());
            modelService.save(annualSalesModel);

            if (listData.getAnnualSalesMonthWiseTargetData() != null && !listData.getAnnualSalesMonthWiseTargetData().isEmpty()) {
                for (AnnualSalesMonthWiseTargetData data : listData.getAnnualSalesMonthWiseTargetData()) {
                    if (data.getIsAnnualSalesRevised()!=null && data.getIsAnnualSalesRevised().equals(true)) {
                        String financialYear = findNextFinancialYear();
                        RetailerRevisedAnnualSalesModel retailerRevisedAnnualSalesModel = validateRetailerRevisedAnnualSalesModel(data.getCustomerCode(), subAreaMaster.getPk().toString(), sclUser, financialYear);
                        retailerRevisedAnnualSalesModel.setCustomerPotential(data.getCustomerPotential());
                        retailerRevisedAnnualSalesModel.setCustomerCode(data.getCustomerCode());
                        retailerRevisedAnnualSalesModel.setCustomerName(data.getCustomerName());
                        retailerRevisedAnnualSalesModel.setFinancialYear(findNextFinancialYear());
                        retailerRevisedAnnualSalesModel.setTotalTarget(data.getTotalTarget());
                        retailerRevisedAnnualSalesModel.setSubAreaMaster(subAreaMaster);
                        retailerRevisedAnnualSalesModel.setStatus("Finalized");
                        retailerRevisedAnnualSalesModel.setIsAnnualSalesRevisedForRetailer(true);
                        retailerRevisedAnnualSalesModel.setErpCustomerNo(data.getErpCustomerNo());
                        retailerRevisedAnnualSalesModel.setBrand(baseSite);

                        if (data.getMonthWiseTarget() != null && !data.getMonthWiseTarget().isEmpty()) {
                            List<MonthWiseAnnualTargetModel> monthList = new ArrayList<>();
                            for (MonthWiseTargetData monthWiseTargetData : data.getMonthWiseTarget()) {
                                MonthWiseAnnualTargetModel monthWiseAnnualTargetModel = validateMonthWiseDetailsOfDealerForRetailer(data.getCustomerCode(), subAreaMaster.getPk().toString(), monthWiseTargetData.getMonthYear(), sclUser);
                                monthWiseAnnualTargetModel.setMonthYear(monthWiseTargetData.getMonthYear());
                                monthWiseAnnualTargetModel.setMonthTarget(monthWiseTargetData.getMonthTarget());
                                monthWiseAnnualTargetModel.setCustomerCode(data.getCustomerCode());
                                monthWiseAnnualTargetModel.setSalesOfficer(sclUser);
                                monthWiseAnnualTargetModel.setSubAreaMaster(subAreaMaster);
                                monthWiseAnnualTargetModel.setIsAnnualSalesRevisedForRetailer(true);
                                monthWiseAnnualTargetModel.setBrand(baseSite);
                                monthList.add(monthWiseAnnualTargetModel);
                                modelService.save(monthWiseAnnualTargetModel);
                            }
                            retailerRevisedAnnualSalesModel.setMonthWiseAnnualTarget(monthList);
                        }

                        //self counter
                        SelfCounterSaleData selfCounterSaleData = data.getSelfCounterSale();
                        SelfCounterSaleDetailsModel selfCounterSaleDetailsModel = validateRevisedSelfCounterSaleModel(selfCounterSaleData.getCustomerCode(), subAreaMaster.getPk().toString(), sclUser, data.getIsAnnualSalesRevised());
                        selfCounterSaleDetailsModel.setCustomerCode(selfCounterSaleData.getCustomerCode());
                        selfCounterSaleDetailsModel.setCustomerName(selfCounterSaleData.getCustomerName());
                        selfCounterSaleDetailsModel.setCustomerPotential(selfCounterSaleData.getCustomerPotential());
                        selfCounterSaleDetailsModel.setSalesOfficer(sclUser);
                        selfCounterSaleDetailsModel.setErpCustomerNo(selfCounterSaleData.getErpCustomerNo());
                        selfCounterSaleDetailsModel.setSubAreaMaster(subAreaMaster);
                        selfCounterSaleDetailsModel.setTotalTarget(selfCounterSaleData.getTotalTarget());
                        selfCounterSaleDetailsModel.setIsAnnualSalesRevisedForRetailer(true);
                        selfCounterSaleDetailsModel.setBrand(baseSite);

                        List<MonthWiseAnnualTargetModel> monthWiseSelfCounterList = new ArrayList();
                        for (MonthWiseTargetData monthWiseTargetData : selfCounterSaleData.getMonthWiseTarget()) {
                            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel = validateRevisedSelfCounterMonthWiseDetailsModel(data.getCustomerCode(), selfCounterSaleData.getCustomerCode(), subAreaMaster.getPk().toString(), sclUser, monthWiseTargetData.getMonthYear());
                            monthWiseAnnualTargetModel.setMonthYear(monthWiseTargetData.getMonthYear());
                            monthWiseAnnualTargetModel.setMonthTarget(monthWiseTargetData.getMonthTarget());
                            monthWiseAnnualTargetModel.setCustomerCode(data.getCustomerCode());
                            monthWiseAnnualTargetModel.setSelfCounterCustomerCode(selfCounterSaleData.getCustomerCode());
                            monthWiseAnnualTargetModel.setSubAreaMaster(subAreaMaster);
                            monthWiseAnnualTargetModel.setSalesOfficer(sclUser);
                            monthWiseAnnualTargetModel.setIsAnnualSalesRevisedForRetailer(true);
                            monthWiseAnnualTargetModel.setSelfCounterSaleDetails(selfCounterSaleDetailsModel);
                            monthWiseAnnualTargetModel.setBrand(baseSite);
                            monthWiseSelfCounterList.add(monthWiseAnnualTargetModel);
                        }
                        selfCounterSaleDetailsModel.setMonthWiseAnnualTarget(monthWiseSelfCounterList);
                        retailerRevisedAnnualSalesModel.setDealerSelfCounterSale(selfCounterSaleDetailsModel);
                        modelService.save(retailerRevisedAnnualSalesModel);

                        if (data.getRetailerData() != null && !data.getRetailerData().isEmpty()) {
                            List<RetailerRevisedAnnualSalesDetailsModel> retailerDetailsList = new ArrayList<>();
                           // retailerDetailsList.addAll(retailerRevisedAnnualSalesModel.getListOfRetailersRevised());
                            for (RetailerDetailsData retailerDatum : data.getRetailerData()) {
                                //if (retailerDetailsList != null && !retailerDetailsList.isEmpty()) {
                                   // List<RetailerRevisedAnnualSalesDetailsModel> retailerDetails = retailerDetailsList.stream().filter(p -> p.getCustomerCode().equals(retailerDatum.getCustomerCode())).collect(Collectors.toList());
                                    /*if (CollectionUtils.isNotEmpty(retailerDetails)) {
                                        ErrorWsDTO error = getError(retailerDatum.getCustomerName(), RETAILER_CODE_EXISTING_FINALIZE.concat(" ").concat("For" + " " + data.getCustomerCode()), AmbiguousIdentifierException.class.getName());
                                        errorWsDTOList.add(error);
                                    }*/ //else {
                                        RetailerRevisedAnnualSalesDetailsModel retailerRevisedAnnualSalesDetailsModel = validateRevisedRetailersDetails(retailerDatum.getCustomerCode(), sclUser, subAreaMaster.getPk().toString());
                                        retailerRevisedAnnualSalesDetailsModel.setCustomerCode(retailerDatum.getCustomerCode());
                                        retailerRevisedAnnualSalesDetailsModel.setCustomerName(retailerDatum.getCustomerName());
                                        retailerRevisedAnnualSalesDetailsModel.setCustomerPotential(retailerDatum.getCustomerPotential());
                                        retailerRevisedAnnualSalesDetailsModel.setTotalTarget(retailerDatum.getTotalTarget());
                                        retailerRevisedAnnualSalesDetailsModel.setSalesOfficer(sclUser);
                                        retailerRevisedAnnualSalesDetailsModel.setSubAreaMaster(subAreaMaster);
                                        retailerRevisedAnnualSalesDetailsModel.setStatus("Finalized");
                                        retailerRevisedAnnualSalesDetailsModel.setIsAnnualSalesRevisedForRetailer(true);
                                        retailerRevisedAnnualSalesDetailsModel.setErpCustomerNo(retailerDatum.getErpCustomerNo());
                                        retailerRevisedAnnualSalesDetailsModel.setBrand(baseSite);

                                        List<MonthWiseAnnualTargetModel> monthWiseListForRetailer = new ArrayList<>();
                                        if (retailerDatum.getMonthWiseSkuTarget() != null && !retailerDatum.getMonthWiseSkuTarget().isEmpty()) {
                                            for (MonthWiseTargetData monthWiseTargetData : retailerDatum.getMonthWiseSkuTarget()) {
                                                MonthWiseAnnualTargetModel monthWiseAnnualTargetModel = validateRevisedRetailerMonthWiseDetails(data.getCustomerCode(), retailerDatum.getCustomerCode(), subAreaMaster.getPk().toString(), sclUser, monthWiseTargetData.getMonthYear());
                                                monthWiseAnnualTargetModel.setCustomerCode(data.getCustomerCode());
                                                monthWiseAnnualTargetModel.setRetailerCode(retailerDatum.getCustomerCode());
                                                monthWiseAnnualTargetModel.setSubAreaMaster(subAreaMaster);
                                                monthWiseAnnualTargetModel.setSalesOfficer(sclUser);
                                                monthWiseAnnualTargetModel.setIsAnnualSalesRevisedForRetailer(true);
                                                monthWiseAnnualTargetModel.setMonthYear(monthWiseTargetData.getMonthYear());
                                                monthWiseAnnualTargetModel.setMonthTarget(monthWiseTargetData.getMonthTarget());
                                                monthWiseAnnualTargetModel.setRetailerRevisedAnnualSalesDetails(retailerRevisedAnnualSalesDetailsModel);
                                                monthWiseAnnualTargetModel.setBrand(baseSite);
                                                monthWiseListForRetailer.add(monthWiseAnnualTargetModel);
                                                modelService.save(monthWiseAnnualTargetModel);
                                            }
                                        }
                                        retailerRevisedAnnualSalesDetailsModel.setMonthWiseAnnualTarget(monthWiseListForRetailer);
                                        modelService.save(retailerRevisedAnnualSalesDetailsModel);
                                        retailerDetailsList.add(retailerRevisedAnnualSalesDetailsModel);
                                        retailerRevisedAnnualSalesDetailsModel.setRetailerRevisedAnnualSales(retailerRevisedAnnualSalesModel);
                                        modelService.save(retailerRevisedAnnualSalesDetailsModel);
                                    }
                                    retailerRevisedAnnualSalesModel.setListOfRetailersRevised(retailerDetailsList);
                                //}
                                    /*else {

                                    RetailerRevisedAnnualSalesDetailsModel retailerRevisedAnnualSalesDetailsModel = validateRevisedRetailersDetails(retailerDatum.getCustomerCode(), sclUser, subAreaMaster.getPk().toString());
                                    retailerRevisedAnnualSalesDetailsModel.setCustomerCode(retailerDatum.getCustomerCode());
                                    retailerRevisedAnnualSalesDetailsModel.setCustomerName(retailerDatum.getCustomerName());
                                    retailerRevisedAnnualSalesDetailsModel.setCustomerPotential(retailerDatum.getCustomerPotential());
                                    retailerRevisedAnnualSalesDetailsModel.setTotalTarget(retailerDatum.getTotalTarget());
                                    retailerRevisedAnnualSalesDetailsModel.setSalesOfficer(sclUser);
                                    retailerRevisedAnnualSalesDetailsModel.setSubAreaMaster(subAreaMaster);
                                    retailerRevisedAnnualSalesDetailsModel.setIsAnnualSalesRevisedForRetailer(true);
                                    retailerRevisedAnnualSalesDetailsModel.setStatus("Finalized");

                                    List<MonthWiseAnnualTargetModel> monthWiseListForRetailer = new ArrayList<>();
                                    if (retailerDatum.getMonthWiseSkuTarget() != null && !retailerDatum.getMonthWiseSkuTarget().isEmpty()) {
                                        for (MonthWiseTargetData monthWiseTargetData : retailerDatum.getMonthWiseSkuTarget()) {
                                            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel = validateRevisedRetailerMonthWiseDetails(data.getCustomerCode(), retailerDatum.getCustomerCode(), subAreaMaster.getPk().toString(), sclUser, monthWiseTargetData.getMonthYear());
                                            monthWiseAnnualTargetModel.setCustomerCode(data.getCustomerCode());
                                            monthWiseAnnualTargetModel.setRetailerCode(retailerDatum.getCustomerCode());
                                            monthWiseAnnualTargetModel.setSubAreaMaster(subAreaMaster);
                                            monthWiseAnnualTargetModel.setSalesOfficer(sclUser);
                                            monthWiseAnnualTargetModel.setMonthYear(monthWiseTargetData.getMonthYear());
                                            monthWiseAnnualTargetModel.setMonthTarget(monthWiseTargetData.getMonthTarget());
                                            monthWiseAnnualTargetModel.setIsAnnualSalesRevisedForRetailer(true);
                                            monthWiseAnnualTargetModel.setRetailerRevisedAnnualSalesDetails(retailerRevisedAnnualSalesDetailsModel);
                                            monthWiseListForRetailer.add(monthWiseAnnualTargetModel);
                                            modelService.save(monthWiseAnnualTargetModel);
                                        }
                                        retailerRevisedAnnualSalesDetailsModel.setMonthWiseAnnualTarget(monthWiseListForRetailer);
                                        modelService.save(retailerRevisedAnnualSalesDetailsModel);
                                    }
                                    retailerDetailsList.add(retailerRevisedAnnualSalesDetailsModel);
                                    retailerRevisedAnnualSalesDetailsModel.setRetailerRevisedAnnualSales(retailerRevisedAnnualSalesModel);
                                    modelService.save(retailerRevisedAnnualSalesDetailsModel);
                                }*/
                           // }
                            retailerRevisedAnnualSalesModel.setAnnualSales(annualSalesModel);
                            modelService.save(retailerRevisedAnnualSalesModel);
                        }
                    }
                }
            }
            modelService.save(annualSalesModel);
        }
        errorListWsDTO.setErrors(errorWsDTOList);
        return errorListWsDTO;
    }

    @Override
    public boolean submitOnboardedAnnualSalesTargetSettingForDealers(AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData) {
        final ErrorListWsDTO errorListWsDTO = new ErrorListWsDTO();
        final List<ErrorWsDTO> errorWsDTOList = new ArrayList<>();
        BaseSiteModel baseSite=baseSiteService.getCurrentBaseSite();
        if (annualSalesMonthWiseTargetListData.getAnnualSalesMonthWiseTargetData() != null
                && !annualSalesMonthWiseTargetListData.getAnnualSalesMonthWiseTargetData().isEmpty()) {

            SclUserModel sclUser = (SclUserModel) getUserService().getCurrentUser();
            SubAreaMasterModel subAreaMaster = territoryManagementService.getTerritoryById(annualSalesMonthWiseTargetListData.getSubAreaId());
            AnnualSalesModel annualSalesModel = validateAnnualSalesModel(sclUser, subAreaMaster.getPk().toString(), baseSite);

            if (annualSalesModel != null) {
                if (annualSalesModel.getSubAreaMasterList() != null && !annualSalesModel.getSubAreaMasterList().isEmpty()) {
                    List<SubAreaMasterModel> subAreaListModelsForAnnual = new ArrayList<>(annualSalesModel.getSubAreaMasterList());
                    for (SubAreaMasterModel subAreaMasterModel : annualSalesModel.getSubAreaMasterList()) {
                        if (subAreaMasterModel.equals(subAreaMaster)) {
                            annualSalesModel.setSalesOfficer(sclUser);
                            annualSalesModel.setIsAnnualSalesReviewedForDealer(true);
                            annualSalesModel.setBrand(baseSite);
                            subAreaListModelsForAnnual.add(subAreaMaster);
                            annualSalesModel.setSubAreaMasterList(subAreaListModelsForAnnual);
                        }
                    }
                } else {
                    List<SubAreaMasterModel> subAreaListModelsForAnnual = new ArrayList<>();
                    annualSalesModel.setSalesOfficer(sclUser);
                    annualSalesModel.setIsAnnualSalesReviewedForDealer(true);
                    annualSalesModel.setBrand(baseSite);
                    subAreaListModelsForAnnual.add(subAreaMaster);
                    annualSalesModel.setSubAreaMasterList(subAreaListModelsForAnnual);
                }
            }
            annualSalesModel.setTotalReviewedTargetForAllDealers(annualSalesMonthWiseTargetListData.getTotalTargetForAllDealers());
            if (annualSalesMonthWiseTargetListData.getTotalTargetForAllDealersMonthWise() != null && !annualSalesMonthWiseTargetListData.getTotalTargetForAllDealersMonthWise().isEmpty()) {
                final Map<String, Double> monthTargetMapForDealers = new HashMap<>();
                annualSalesMonthWiseTargetListData.getTotalTargetForAllDealersMonthWise().stream().forEach(totalMonthlyTargetData ->
                        monthTargetMapForDealers.put(totalMonthlyTargetData.getMonthYear(), totalMonthlyTargetData.getTotalTargetforMonth()));
                annualSalesModel.setTotalReviewedTargetForAllDealersMonthWise(monthTargetMapForDealers);
            }

            annualSalesModel.setFinancialYear(findNextFinancialYear());
            if(subAreaMaster != null) {
                DistrictMasterModel district = subAreaMaster.getDistrictMaster();
                if(district!=null) {
                    annualSalesModel.setDistrictMaster(district);
                    RegionMasterModel region = district.getRegion();
                    if(region!=null) {
                        annualSalesModel.setRegionMaster(region);
                    }
                    StateMasterModel state=region.getState();
                    if(state!=null)
                    {
                        annualSalesModel.setStateMaster(state);
                    }
                }
            }
            modelService.save(annualSalesModel);

            //check for onboarded one for dealerrevised

            if (annualSalesMonthWiseTargetListData.getAnnualSalesMonthWiseTargetData() != null && !annualSalesMonthWiseTargetListData.getAnnualSalesMonthWiseTargetData().isEmpty())
            {
                for (AnnualSalesMonthWiseTargetData data : annualSalesMonthWiseTargetListData.getAnnualSalesMonthWiseTargetData()) {
                    if(data.getIsAnnualSalesRevised()!=null && data.getIsAnnualSalesRevisedForReview()!=null && data.getIsAnnualSalesRevised().equals(true) && data.getIsAnnualSalesRevisedForReview().equals(true))
                    {
                        //validate if dealer revised annual sale is existing with IsExistingDealerRevisedForReview flag
                        String financialYear = findNextFinancialYear();
                        DealerRevisedAnnualSalesModel dealerRevisedAnnualSalesModel = validateReviewForExistingDealersSale(data.getCustomerCode(), subAreaMaster.getPk().toString(), sclUser, financialYear);
                        SclCustomerModel sclCustomer = (SclCustomerModel) getUserService().getUserForUID(data.getCustomerCode());
                        dealerRevisedAnnualSalesModel.setCustomerCode(sclCustomer.getUid());
                        dealerRevisedAnnualSalesModel.setCustomerName(data.getCustomerName());
                        dealerRevisedAnnualSalesModel.setCustomerPotential(data.getCustomerPotential());
                        dealerRevisedAnnualSalesModel.setCustomerCategory(CounterType.DEALER);
                        dealerRevisedAnnualSalesModel.setFinancialYear(findNextFinancialYear());
                        dealerRevisedAnnualSalesModel.setSubAreaMaster(subAreaMaster);
                        dealerRevisedAnnualSalesModel.setIsExistingDealerRevisedForReview(true);
                        dealerRevisedAnnualSalesModel.setTotalTarget(data.getTotalTarget());
                        dealerRevisedAnnualSalesModel.setBrand(baseSite);
                        if(subAreaMaster != null) {
                            DistrictMasterModel district = subAreaMaster.getDistrictMaster();
                            if(district!=null) {
                                dealerRevisedAnnualSalesModel.setDistrictMaster(district);
                                RegionMasterModel region = district.getRegion();
                                if(region!=null) {
                                    dealerRevisedAnnualSalesModel.setRegionMaster(region);
                                }
                                StateMasterModel state=region.getState();
                                if(state!=null)
                                {
                                    dealerRevisedAnnualSalesModel.setStateMaster(state);
                                }
                            }
                        }
                        List<ProductModel> skuList = new ArrayList<>();
                        if(data.getSkuDataList()!=null && !data.getSkuDataList().isEmpty()) {
                            List<ProductSaleModel> productSaleModelList = new ArrayList<>();
                            for (SKUData skuData : data.getSkuDataList()) {
                                //List<ProductSaleModel> productSaleModelList = new ArrayList<>();
                                ProductModel productModel = getProductService().getProductForCode(skuData.getProductCode());
                               // validate if product sales model is existing with setIsAnnualSalesReviewedForDealer flag
                                ProductSaleModel productSaleModel = validateReviewForExistingDealerSkuSale(subAreaMaster.getPk().toString(), data.getCustomerCode(), skuData.getProductCode(),sclUser);
                                productSaleModel.setProductCode(skuData.getProductCode());
                                productSaleModel.setProductName(skuData.getProductName());
                                productSaleModel.setProductGrade(productModel.getGrade());
                                productSaleModel.setProductPackaging(productModel.getPackagingCondition());
                                productSaleModel.setProductPackType(productModel.getBagType());
                                productSaleModel.setTotalTarget(skuData.getTotalTarget());
                                productSaleModel.setCustomerCode(data.getCustomerCode());
                                productSaleModel.setSalesOfficer(sclUser);
                                productSaleModel.setSubAreaMaster(subAreaMaster);
                                productSaleModel.setIsAnnualSalesReviewedForDealer(true);
                                productSaleModel.setBrand(baseSite);
                                if(subAreaMaster != null) {
                                    DistrictMasterModel district = subAreaMaster.getDistrictMaster();
                                    if(district!=null) {
                                        productSaleModel.setDistrictMaster(district);
                                        RegionMasterModel region = district.getRegion();
                                        if(region!=null) {
                                            productSaleModel.setRegionMaster(region);
                                        }
                                        StateMasterModel state=region.getState();
                                        if(state!=null)
                                        {
                                            productSaleModel.setStateMaster(state);
                                        }
                                    }
                                }
                                productSaleModelList.add(productSaleModel);
                                modelService.save(productSaleModel);

                                List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetModelsforSKU = new ArrayList();
                                if (skuData.getMonthWiseSkuTarget() != null && !skuData.getMonthWiseSkuTarget().isEmpty()) {
                                    for (MonthWiseTargetData monthWiseTargetData : skuData.getMonthWiseSkuTarget()) {
                                        // validate if MonthWiseAnnualTargetModel is existing with setIsAnnualSalesReviewedForDealer flag for sku
                                        MonthWiseAnnualTargetModel monthWiseAnnualTargetModel = validateReviewForExistingDealerSkuSaleForMonthWise(subAreaMaster.getPk().toString(), data.getCustomerCode(), skuData.getProductCode(), monthWiseTargetData.getMonthYear(), sclUser);
                                        monthWiseAnnualTargetModel.setCustomerCode(data.getCustomerCode());
                                        monthWiseAnnualTargetModel.setProductCode(skuData.getProductCode());
                                        ProductModel product = productService.getProductForCode(skuData.getProductCode());
                                        if(!Objects.isNull(product)) {
                                            monthWiseAnnualTargetModel.setProductGrade(product.getGrade());
                                            monthWiseAnnualTargetModel.setProductPackaging(product.getPackagingCondition());
                                            monthWiseAnnualTargetModel.setProductBagType(product.getBagType());
                                        }
                                        monthWiseAnnualTargetModel.setMonthYear(monthWiseTargetData.getMonthYear());
                                        monthWiseAnnualTargetModel.setMonthTarget(monthWiseTargetData.getMonthTarget());
                                        monthWiseAnnualTargetModel.setSubAreaMaster(subAreaMaster);
                                        monthWiseAnnualTargetModel.setSalesOfficer(sclUser);
                                        monthWiseAnnualTargetModel.setIsAnnualSalesReviewedForDealer(true);
                                        monthWiseAnnualTargetModel.setBrand(baseSite);
                                        if(subAreaMaster != null) {
                                            DistrictMasterModel district = subAreaMaster.getDistrictMaster();
                                            if(district!=null) {
                                                monthWiseAnnualTargetModel.setDistrictMaster(district);
                                                RegionMasterModel region = district.getRegion();
                                                if(region!=null) {
                                                    monthWiseAnnualTargetModel.setRegionMaster(region);
                                                }
                                                StateMasterModel state=region.getState();
                                                if(state!=null)
                                                {
                                                    monthWiseAnnualTargetModel.setStateMaster(state);
                                                }
                                            }
                                        }
                                        monthWiseAnnualTargetModelsforSKU.add(monthWiseAnnualTargetModel);
                                        modelService.save(monthWiseAnnualTargetModel);
                                    }
                                }
                                modelService.save(productModel);
                                skuList.add(productModel);
                            }
                        }

                        dealerRevisedAnnualSalesModel.setListOfSkus(skuList);

                        List<MonthWiseAnnualTargetModel> monthList = new ArrayList<>();
                        if (data.getMonthWiseTarget() != null &&
                                !data.getMonthWiseTarget().isEmpty()) {
                            for (MonthWiseTargetData monthWiseTargetData : data.getMonthWiseTarget()) {
                                // validate if MonthWiseAnnualTargetModel is existing with setIsAnnualSalesReviewedForDealer flag for dealer
                                MonthWiseAnnualTargetModel monthWiseAnnualTargetModel = validateReviewForExistingDealersSaleForMonthWise(subAreaMaster.getPk().toString(), data.getCustomerCode(), monthWiseTargetData.getMonthYear(), sclUser);
                                monthWiseAnnualTargetModel.setMonthYear(monthWiseTargetData.getMonthYear());
                                monthWiseAnnualTargetModel.setMonthTarget(monthWiseTargetData.getMonthTarget());
                                monthWiseAnnualTargetModel.setCustomerCode(data.getCustomerCode());
                                monthWiseAnnualTargetModel.setSubAreaMaster(subAreaMaster);
                                monthWiseAnnualTargetModel.setSalesOfficer(sclUser);
                                monthWiseAnnualTargetModel.setIsAnnualSalesReviewedForDealer(true);
                                monthWiseAnnualTargetModel.setBrand(baseSite);
                                if(subAreaMaster != null) {
                                    DistrictMasterModel district = subAreaMaster.getDistrictMaster();
                                    if(district!=null) {
                                        monthWiseAnnualTargetModel.setDistrictMaster(district);
                                        RegionMasterModel region = district.getRegion();
                                        if(region!=null) {
                                            monthWiseAnnualTargetModel.setRegionMaster(region);
                                        }
                                        StateMasterModel state=region.getState();
                                        if(state!=null)
                                        {
                                            monthWiseAnnualTargetModel.setStateMaster(state);
                                        }
                                    }
                                }
                                monthList.add(monthWiseAnnualTargetModel);
                                modelService.save(monthWiseAnnualTargetModel);
                            }
                            dealerRevisedAnnualSalesModel.setMonthWiseAnnualTarget(monthList);
                        }
                        dealerRevisedAnnualSalesModel.setAnnualSales(annualSalesModel);
                        modelService.save(dealerRevisedAnnualSalesModel);
                    }
                    else if (data.getIsNewDealerOnboarded()!=null && data.getIsNewDealerOnboarded().equals(true))
                    {
                        String financialYear=findNextFinancialYear();
                        DealerRevisedAnnualSalesModel dealerRevisedAnnualSalesModel = validateReviewForOnboardedDealersSale(data.getCustomerCode(), subAreaMaster.getPk().toString(), sclUser, financialYear);
                        SclCustomerModel sclCustomer = (SclCustomerModel) getUserService().getUserForUID(data.getCustomerCode());
                        dealerRevisedAnnualSalesModel.setCustomerCode(sclCustomer.getUid());
                        dealerRevisedAnnualSalesModel.setCustomerName(data.getCustomerName());
                        dealerRevisedAnnualSalesModel.setCustomerPotential(data.getCustomerPotential());
                        dealerRevisedAnnualSalesModel.setCustomerCategory(CounterType.DEALER);
                        dealerRevisedAnnualSalesModel.setFinancialYear(findNextFinancialYear());
                        dealerRevisedAnnualSalesModel.setSubAreaMaster(subAreaMaster);
                        dealerRevisedAnnualSalesModel.setIsNewDealerOnboarded(true);
                        dealerRevisedAnnualSalesModel.setStatus("OnboardedDealer");
                        dealerRevisedAnnualSalesModel.setTotalTarget(data.getTotalTarget());
                        dealerRevisedAnnualSalesModel.setBrand(baseSite);
                        if(subAreaMaster != null) {
                            DistrictMasterModel district = subAreaMaster.getDistrictMaster();
                            if(district!=null) {
                                dealerRevisedAnnualSalesModel.setDistrictMaster(district);
                                RegionMasterModel region = district.getRegion();
                                if(region!=null) {
                                    dealerRevisedAnnualSalesModel.setRegionMaster(region);
                                }
                                StateMasterModel state=region.getState();
                                if(state!=null)
                                {
                                    dealerRevisedAnnualSalesModel.setStateMaster(state);
                                }
                            }
                        }
                        List<ProductModel> skuListOnboard = new ArrayList<>();
                        if(data.getSkuDataList()!=null && !data.getSkuDataList().isEmpty()) {
                            List<ProductSaleModel> productSaleModelListForOnboard = new ArrayList<>();
                            for (SKUData skuData : data.getSkuDataList()) {
                                //List<ProductSaleModel> productSaleModelList = new ArrayList<>();
                                ProductModel productModel = getProductService().getProductForCode(skuData.getProductCode());
                                ProductSaleModel productSaleModel = validateReviewForOnboardedDealerSkuSale(subAreaMaster.getPk().toString(), data.getCustomerCode(), skuData.getProductCode(),sclUser);
                                productSaleModel.setProductCode(skuData.getProductCode());
                                productSaleModel.setProductName(skuData.getProductName());
                                productSaleModel.setProductGrade(productModel.getGrade());
                                productSaleModel.setProductPackaging(productModel.getPackagingCondition());
                                productSaleModel.setProductPackType(productModel.getBagType());
                                productSaleModel.setTotalTarget(skuData.getTotalTarget());
                                productSaleModel.setCustomerCode(data.getCustomerCode());
                                productSaleModel.setSalesOfficer(sclUser);
                                productSaleModel.setSubAreaMaster(subAreaMaster);
                                productSaleModel.setIsAnnualSalesOnboardedForDealer(true);
                                productSaleModel.setBrand(baseSite);
                                if(subAreaMaster != null) {
                                    DistrictMasterModel district = subAreaMaster.getDistrictMaster();
                                    if(district!=null) {
                                        productSaleModel.setDistrictMaster(district);
                                        RegionMasterModel region = district.getRegion();
                                        if(region!=null) {
                                            productSaleModel.setRegionMaster(region);
                                        }
                                        StateMasterModel state=region.getState();
                                        if(state!=null)
                                        {
                                            productSaleModel.setStateMaster(state);
                                        }
                                    }
                                }
                                productSaleModelListForOnboard.add(productSaleModel);
                                modelService.save(productSaleModel);

                                List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetModelsforSKU = new ArrayList();
                                if (skuData.getMonthWiseSkuTarget() != null && !skuData.getMonthWiseSkuTarget().isEmpty()) {
                                    for (MonthWiseTargetData monthWiseTargetData : skuData.getMonthWiseSkuTarget()) {
                                        MonthWiseAnnualTargetModel monthWiseAnnualTargetModel = validateReviewForOnboardedDealersSaleSkuForMonthWise(subAreaMaster.getPk().toString(), data.getCustomerCode(), skuData.getProductCode(),monthWiseTargetData.getMonthYear(), sclUser);
                                        monthWiseAnnualTargetModel.setCustomerCode(data.getCustomerCode());
                                        monthWiseAnnualTargetModel.setProductCode(skuData.getProductCode());
                                        ProductModel product = productService.getProductForCode(skuData.getProductCode());
                                        if(!Objects.isNull(product)) {
                                            monthWiseAnnualTargetModel.setProductGrade(product.getGrade());
                                            monthWiseAnnualTargetModel.setProductPackaging(product.getPackagingCondition());
                                            monthWiseAnnualTargetModel.setProductBagType(product.getBagType());
                                        }
                                        monthWiseAnnualTargetModel.setMonthYear(monthWiseTargetData.getMonthYear());
                                        monthWiseAnnualTargetModel.setMonthTarget(monthWiseTargetData.getMonthTarget());
                                        monthWiseAnnualTargetModel.setSubAreaMaster(subAreaMaster);
                                        monthWiseAnnualTargetModel.setSalesOfficer(sclUser);
                                        monthWiseAnnualTargetModel.setIsAnnualSalesOnboardedForDealer(true);
                                        monthWiseAnnualTargetModel.setBrand(baseSite);
                                        if(subAreaMaster != null) {
                                            DistrictMasterModel district = subAreaMaster.getDistrictMaster();
                                            if(district!=null) {
                                                monthWiseAnnualTargetModel.setDistrictMaster(district);
                                                RegionMasterModel region = district.getRegion();
                                                if(region!=null) {
                                                    monthWiseAnnualTargetModel.setRegionMaster(region);
                                                }
                                                StateMasterModel state=region.getState();
                                                if(state!=null)
                                                {
                                                    monthWiseAnnualTargetModel.setStateMaster(state);
                                                }
                                            }
                                        }
                                        monthWiseAnnualTargetModelsforSKU.add(monthWiseAnnualTargetModel);
                                        modelService.save(monthWiseAnnualTargetModel);
                                    }
                                }
                                modelService.save(productModel);
                                skuListOnboard.add(productModel);
                            }
                        }
                        dealerRevisedAnnualSalesModel.setListOfSkus(skuListOnboard);

                        List<MonthWiseAnnualTargetModel> monthListForOnboardDealer = new ArrayList<>();
                        if (data.getMonthWiseTarget() != null &&
                                !data.getMonthWiseTarget().isEmpty()) {
                            for (MonthWiseTargetData monthWiseTargetData : data.getMonthWiseTarget()) {
                                MonthWiseAnnualTargetModel monthWiseAnnualTargetModel = validateReviewForOnboardedDealersSaleForMonthWise(subAreaMaster.getPk().toString(), data.getCustomerCode(), monthWiseTargetData.getMonthYear(), sclUser);
                                monthWiseAnnualTargetModel.setMonthYear(monthWiseTargetData.getMonthYear());
                                monthWiseAnnualTargetModel.setMonthTarget(monthWiseTargetData.getMonthTarget());
                                monthWiseAnnualTargetModel.setCustomerCode(data.getCustomerCode());
                                monthWiseAnnualTargetModel.setSubAreaMaster(subAreaMaster);
                                monthWiseAnnualTargetModel.setSalesOfficer(sclUser);
                                monthWiseAnnualTargetModel.setIsAnnualSalesOnboardedForDealer(true);
                                monthWiseAnnualTargetModel.setBrand(baseSite);
                                if(subAreaMaster != null) {
                                    DistrictMasterModel district = subAreaMaster.getDistrictMaster();
                                    if(district!=null) {
                                        monthWiseAnnualTargetModel.setDistrictMaster(district);
                                        RegionMasterModel region = district.getRegion();
                                        if(region!=null) {
                                            monthWiseAnnualTargetModel.setRegionMaster(region);
                                        }
                                        StateMasterModel state=region.getState();
                                        if(state!=null)
                                        {
                                            monthWiseAnnualTargetModel.setStateMaster(state);
                                        }
                                    }
                                }
                                monthListForOnboardDealer.add(monthWiseAnnualTargetModel);
                                modelService.save(monthWiseAnnualTargetModel);
                            }
                            dealerRevisedAnnualSalesModel.setMonthWiseAnnualTarget(monthListForOnboardDealer);
                        }
                        dealerRevisedAnnualSalesModel.setAnnualSales(annualSalesModel);
                        modelService.save(dealerRevisedAnnualSalesModel);
                    }
                }
            }
            modelService.save(annualSalesModel);
        }
        return true;
    }

    private MonthWiseAnnualTargetModel validateReviewForOnboardedDealersSaleSkuForMonthWise(String subArea, String customerCode, String productCode, String monthYear, SclUserModel sclUser) {
        MonthWiseAnnualTargetModel monthWiseAnnualTargetModelDetails = getSalesPlanningDao().validateReviewForOnboardedDealersSaleSkuForMonthWise(subArea,customerCode,productCode,monthYear,sclUser);
        if(monthWiseAnnualTargetModelDetails!=null)
        {
            return monthWiseAnnualTargetModelDetails;
        }
        else
        {
            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel=modelService.create(MonthWiseAnnualTargetModel.class);
            return monthWiseAnnualTargetModel;
        }
    }

    private MonthWiseAnnualTargetModel validateReviewForOnboardedDealersSaleForMonthWise(String subArea, String customerCode, String monthYear, SclUserModel sclUser) {
        MonthWiseAnnualTargetModel monthWiseAnnualTargetModelDetails = getSalesPlanningDao().validateReviewForOnboardedDealersSaleForMonthWise(subArea,customerCode,monthYear,sclUser);
        if(monthWiseAnnualTargetModelDetails!=null)
        {
            return monthWiseAnnualTargetModelDetails;
        }
        else
        {
            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel=modelService.create(MonthWiseAnnualTargetModel.class);
            return monthWiseAnnualTargetModel;
        }
    }

    private ProductSaleModel validateReviewForOnboardedDealerSkuSale(String subArea, String customerCode, String productCode, SclUserModel sclUser) {
        ProductSaleModel productSaleModelDetails = getSalesPlanningDao().validateReviewForOnboardedDealerSkuSale(subArea,customerCode,productCode,sclUser);
        if(productSaleModelDetails!=null)
        {
            return productSaleModelDetails;
        }
        else
        {
            ProductSaleModel productSaleModel=modelService.create(ProductSaleModel.class);
            return productSaleModel;
        }
    }

    private DealerRevisedAnnualSalesModel validateReviewForOnboardedDealersSale(String customerCode, String subArea, SclUserModel sclUser, String financialYear) {
        DealerRevisedAnnualSalesModel dealerRevisedAnnualSalesModelDetails= getSalesPlanningDao().validateReviewForOnboardedDealersSale(customerCode,subArea,sclUser,financialYear);
        if(dealerRevisedAnnualSalesModelDetails!=null)
        {
            return dealerRevisedAnnualSalesModelDetails;
        }
        else
        {
            DealerRevisedAnnualSalesModel dealerRevisedAnnualSalesModel = modelService.create(DealerRevisedAnnualSalesModel.class);
            return dealerRevisedAnnualSalesModel;
        }
    }

    private MonthWiseAnnualTargetModel validateReviewForExistingDealersSaleForMonthWise(String subArea, String customerCode, String monthYear, SclUserModel sclUser) {
        MonthWiseAnnualTargetModel monthWiseAnnualTargetModelDetails = getSalesPlanningDao().validateReviewForExistingDealersSaleForMonthWise(subArea,customerCode,monthYear,sclUser);
        if(monthWiseAnnualTargetModelDetails!=null)
        {
            return monthWiseAnnualTargetModelDetails;
        }
        else
        {
            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel=modelService.create(MonthWiseAnnualTargetModel.class);
            return monthWiseAnnualTargetModel;
        }
    }

    private MonthWiseAnnualTargetModel validateReviewForExistingDealerSkuSaleForMonthWise(String subArea, String customerCode, String productCode, String monthYear, SclUserModel sclUser) {
        MonthWiseAnnualTargetModel monthWiseAnnualTargetModelDetails = getSalesPlanningDao().validateReviewForExistingDealerSkuSaleForMonthWise(subArea,customerCode,productCode,monthYear,sclUser);
        if(monthWiseAnnualTargetModelDetails!=null)
        {
            return monthWiseAnnualTargetModelDetails;
        }
        else
        {
            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel=modelService.create(MonthWiseAnnualTargetModel.class);
            return monthWiseAnnualTargetModel;
        }
    }

    private ProductSaleModel validateReviewForExistingDealerSkuSale(String subArea, String customerCode, String productCode, SclUserModel sclUser) {
    ProductSaleModel productSaleModelDetails = getSalesPlanningDao().validateReviewForExistingDealerSkuSale(subArea,customerCode,productCode,sclUser);
    if(productSaleModelDetails!=null)
    {
        return productSaleModelDetails;
    }
    else
    {
        ProductSaleModel productSaleModel=modelService.create(ProductSaleModel.class);
        return productSaleModel;
    }
    }

    private DealerRevisedAnnualSalesModel validateReviewForExistingDealersSale(String customerCode, String subArea, SclUserModel sclUser, String financialYear) {
        DealerRevisedAnnualSalesModel dealerRevisedAnnualSalesModelDetails= getSalesPlanningDao().validateReviewForExistingDealersSale(customerCode,subArea,sclUser,financialYear);
        if(dealerRevisedAnnualSalesModelDetails!=null)
        {
            return dealerRevisedAnnualSalesModelDetails;
        }
        else
        {
            DealerRevisedAnnualSalesModel dealerRevisedAnnualSalesModel = modelService.create(DealerRevisedAnnualSalesModel.class);
            return dealerRevisedAnnualSalesModel;
        }
    }

    private MonthWiseAnnualTargetModel validateDealerRevisedMonthWiseSkuDetailsForOnboardedDealer(String subArea, String customerCode, String productCode, String monthYear, SclUserModel sclUser, Boolean isNewDealerOnboarded) {
        MonthWiseAnnualTargetModel monthWiseAnnualTargetModelDetails = salesPlanningDao.fetchDealerRevisedMonthWiseSkuDetailsForOnboardedDealer(subArea,customerCode,productCode,monthYear,sclUser,isNewDealerOnboarded);
        if(monthWiseAnnualTargetModelDetails!=null){
            return  monthWiseAnnualTargetModelDetails;
        }
        else{
            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel=modelService.create(MonthWiseAnnualTargetModel.class);
            return monthWiseAnnualTargetModel;
        }
    }

    @Override
    public boolean submitOnboardedAnnualSalesTargetSettingForRetailers(AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData) {
        final ErrorListWsDTO errorListWsDTO = new ErrorListWsDTO();
        final List<ErrorWsDTO> errorWsDTOList = new ArrayList<>();
        BaseSiteModel baseSite=baseSiteService.getCurrentBaseSite();
        if (annualSalesMonthWiseTargetListData.getAnnualSalesMonthWiseTargetData() != null
                && !annualSalesMonthWiseTargetListData.getAnnualSalesMonthWiseTargetData().isEmpty()) {
            SclUserModel sclUser = (SclUserModel) getUserService().getCurrentUser();
            SubAreaMasterModel subAreaMaster = territoryManagementService.getTerritoryById(annualSalesMonthWiseTargetListData.getSubAreaId());
            AnnualSalesModel annualSalesModel = validateAnnualSalesModel(sclUser, subAreaMaster.getPk().toString(), baseSite);
            if (annualSalesModel != null) {
                if (annualSalesModel.getSubAreaMasterList() != null && !annualSalesModel.getSubAreaMasterList().isEmpty()) {
                    List<SubAreaMasterModel> subAreaListModelsForAnnual = new ArrayList<>(annualSalesModel.getSubAreaMasterList());
                    for (SubAreaMasterModel subAreaMasterModel : annualSalesModel.getSubAreaMasterList()) {
                        if (subAreaMasterModel.equals(subAreaMaster)) {
                            annualSalesModel.setSalesOfficer(sclUser);
                            annualSalesModel.setIsAnnualSalesReviewedForRetailer(true);
                            annualSalesModel.setBrand(baseSite);
                            subAreaListModelsForAnnual.add(subAreaMaster);
                            annualSalesModel.setSubAreaMasterList(subAreaListModelsForAnnual);
                        }
                    }
                } else {
                    List<SubAreaMasterModel> subAreaListModelsForAnnual = new ArrayList<>();
                    annualSalesModel.setSalesOfficer(sclUser);
                    annualSalesModel.setIsAnnualSalesReviewedForRetailer(true);
                    annualSalesModel.setBrand(baseSite);
                    subAreaListModelsForAnnual.add(subAreaMaster);
                    annualSalesModel.setSubAreaMasterList(subAreaListModelsForAnnual);
                }
            }

            annualSalesModel.setFinancialYear(findNextFinancialYear());
            if(subAreaMaster != null) {
                annualSalesModel.setSubAreaMaster(subAreaMaster);
                DistrictMasterModel district = subAreaMaster.getDistrictMaster();
                if(district!=null) {
                    annualSalesModel.setDistrictMaster(district);
                    RegionMasterModel region = district.getRegion();
                    if(region!=null) {
                        annualSalesModel.setRegionMaster(region);
                    }
                    StateMasterModel state=region.getState();
                    if(state!=null)
                    {
                        annualSalesModel.setStateMaster(state);
                    }
                }
            }
            annualSalesModel.setTotalReviewedTargetForAllRetailers(annualSalesMonthWiseTargetListData.getTotalTargetForAllDealers());
            if (annualSalesMonthWiseTargetListData.getTotalTargetForAllDealersMonthWise() != null && !annualSalesMonthWiseTargetListData.getTotalTargetForAllDealersMonthWise().isEmpty()) {
                final Map<String, Double> monthTargetMapForDealers = new HashMap<>();
                annualSalesMonthWiseTargetListData.getTotalTargetForAllDealersMonthWise().stream().forEach(totalMonthlyTargetData ->
                        monthTargetMapForDealers.put(totalMonthlyTargetData.getMonthYear(), totalMonthlyTargetData.getTotalTargetforMonth()));
                annualSalesModel.setTotalReviewedTargetForAllRetailersMonthWise(monthTargetMapForDealers);
            }
            modelService.save(annualSalesModel);


            if (annualSalesMonthWiseTargetListData.getAnnualSalesMonthWiseTargetData() != null && !annualSalesMonthWiseTargetListData.getAnnualSalesMonthWiseTargetData().isEmpty()) {
                for (AnnualSalesMonthWiseTargetData data : annualSalesMonthWiseTargetListData.getAnnualSalesMonthWiseTargetData()) {
                    if (data.getIsAnnualSalesRevised() != null && data.getIsAnnualSalesRevisedForReview() != null && data.getIsAnnualSalesRevised().equals(true) && data.getIsAnnualSalesRevisedForReview().equals(true))
                    {
                    //validate if Retailer revised annual sale is existing with IsExistingRetailerRevisedForReview flag
                        String financialYear = findNextFinancialYear();
                        RetailerRevisedAnnualSalesModel retailerRevisedAnnualSalesModel = validateReviewForExistingRetailersSale(data.getCustomerCode(), subAreaMaster.getPk().toString(), sclUser, financialYear);
                        retailerRevisedAnnualSalesModel.setCustomerCode(data.getCustomerCode());
                        retailerRevisedAnnualSalesModel.setCustomerName(data.getCustomerName());
                        retailerRevisedAnnualSalesModel.setCustomerPotential(data.getCustomerPotential());
                        retailerRevisedAnnualSalesModel.setErpCustomerNo(data.getErpCustomerNo());
                        retailerRevisedAnnualSalesModel.setSubAreaMaster(subAreaMaster);
                        retailerRevisedAnnualSalesModel.setTotalTarget(data.getTotalTarget());
                        retailerRevisedAnnualSalesModel.setFinancialYear(findNextFinancialYear());
                        retailerRevisedAnnualSalesModel.setIsExistingRetailerRevisedForReview(true);
                        retailerRevisedAnnualSalesModel.setBrand(baseSite);

                        List<MonthWiseAnnualTargetModel> dealerMonthWiseList = new ArrayList<>();
                        if (data.getMonthWiseTarget() != null && !data.getMonthWiseTarget().isEmpty()) {
                            for (MonthWiseTargetData monthWiseTargetData : data.getMonthWiseTarget()) {
                                // validate if MonthWiseAnnualTargetModel is existing with setIsAnnualSalesReviewedForRetailer flag for retailer - dealer data
                                MonthWiseAnnualTargetModel monthWiseAnnualTargetModel = validateReviewForExistingRetailersSaleForMonthWise(subAreaMaster.getPk().toString(), data.getCustomerCode(), monthWiseTargetData.getMonthYear(), sclUser);
                                monthWiseAnnualTargetModel.setCustomerCode(data.getCustomerCode());
                                monthWiseAnnualTargetModel.setMonthYear(monthWiseTargetData.getMonthYear());
                                monthWiseAnnualTargetModel.setMonthTarget(monthWiseTargetData.getMonthTarget());
                                monthWiseAnnualTargetModel.setSubAreaMaster(subAreaMaster);
                                monthWiseAnnualTargetModel.setSalesOfficer(sclUser);
                                monthWiseAnnualTargetModel.setIsAnnualSalesReviewedForRetailer(true);
                                monthWiseAnnualTargetModel.setBrand(baseSite);
                                dealerMonthWiseList.add(monthWiseAnnualTargetModel);
                                modelService.save(monthWiseAnnualTargetModel);
                            }
                            retailerRevisedAnnualSalesModel.setMonthWiseAnnualTarget(dealerMonthWiseList);
                        }

                        SelfCounterSaleData selfCounterSaleData = data.getSelfCounterSale();
                        SelfCounterSaleDetailsModel selfCounterSaleDetailsModel = validateReviewForExistingSelfCounterSale(subAreaMaster.getPk().toString(), selfCounterSaleData.getCustomerCode(), sclUser);
                        selfCounterSaleDetailsModel.setCustomerCode(selfCounterSaleData.getCustomerCode());
                        selfCounterSaleDetailsModel.setCustomerPotential(selfCounterSaleData.getCustomerPotential());
                        selfCounterSaleDetailsModel.setCustomerName(selfCounterSaleData.getCustomerName());
                        selfCounterSaleDetailsModel.setTotalTarget(selfCounterSaleData.getTotalTarget());
                        selfCounterSaleDetailsModel.setErpCustomerNo(selfCounterSaleData.getErpCustomerNo());
                        selfCounterSaleDetailsModel.setIsAnnualSalesReviewedForRetailer(true);
                        selfCounterSaleDetailsModel.setSubAreaMaster(subAreaMaster);
                        selfCounterSaleDetailsModel.setSalesOfficer(sclUser);
                        selfCounterSaleDetailsModel.setBrand(baseSite);

                        List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetModelForSelf = new ArrayList<>();
                        for (MonthWiseTargetData monthWiseTargetData : selfCounterSaleData.getMonthWiseTarget()) {
                            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel = validateReviewForExistingSelfCounterSaleMonthWise(subAreaMaster.getPk().toString(), selfCounterSaleData.getCustomerCode(), monthWiseTargetData.getMonthYear(), sclUser, data.getCustomerCode());
                            monthWiseAnnualTargetModel.setMonthYear(monthWiseTargetData.getMonthYear());
                            monthWiseAnnualTargetModel.setMonthTarget(monthWiseTargetData.getMonthTarget());
                            monthWiseAnnualTargetModel.setSubAreaMaster(subAreaMaster);
                            monthWiseAnnualTargetModel.setSalesOfficer(sclUser);
                            monthWiseAnnualTargetModel.setSelfCounterCustomerCode(selfCounterSaleData.getCustomerCode());
                            monthWiseAnnualTargetModel.setCustomerCode(data.getCustomerCode());
                            monthWiseAnnualTargetModel.setIsAnnualSalesReviewedForSelfCounter(true);
                            monthWiseAnnualTargetModel.setBrand(baseSite);
                            monthWiseAnnualTargetModelForSelf.add(monthWiseAnnualTargetModel);
                            modelService.save(monthWiseAnnualTargetModel);
                        }
                        selfCounterSaleDetailsModel.setMonthWiseAnnualTarget(monthWiseAnnualTargetModelForSelf);
                        modelService.save(selfCounterSaleDetailsModel);
                        retailerRevisedAnnualSalesModel.setDealerSelfCounterSale(selfCounterSaleDetailsModel);

                    List<RetailerRevisedAnnualSalesDetailsModel> retailerRevisedAnnualSalesDetailsModelList = new ArrayList<>();
                    if (data.getRetailerData() != null && !data.getRetailerData().isEmpty()) {
                        for (RetailerDetailsData retailerDatum : data.getRetailerData()) {
                                // validate if product sales model is existing with setIsAnnualSalesReviewedForDealer flag
                            RetailerRevisedAnnualSalesDetailsModel retailerRevisedAnnualSalesDetailsModel = validateReviewForExistingRetailerDetailsSale(subAreaMaster.getPk().toString(), retailerDatum.getCustomerCode(), sclUser);
                                retailerRevisedAnnualSalesDetailsModel.setCustomerCode(retailerDatum.getCustomerCode());
                                retailerRevisedAnnualSalesDetailsModel.setCustomerName(retailerDatum.getCustomerName());
                                retailerRevisedAnnualSalesDetailsModel.setCustomerPotential(retailerDatum.getCustomerPotential());
                                retailerRevisedAnnualSalesDetailsModel.setTotalTarget(retailerDatum.getTotalTarget());
                                retailerRevisedAnnualSalesDetailsModel.setFinancialYear(findNextFinancialYear());
                                retailerRevisedAnnualSalesDetailsModel.setSubAreaMaster(subAreaMaster);
                                retailerRevisedAnnualSalesDetailsModel.setIsAnnualSalesReviewedForRetailer(true);
                                retailerRevisedAnnualSalesDetailsModel.setSalesOfficer(sclUser);
                                retailerRevisedAnnualSalesDetailsModel.setErpCustomerNo(retailerDatum.getErpCustomerNo());
                                retailerRevisedAnnualSalesDetailsModel.setBrand(baseSite);

                                List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetModelsForRetailerDetail = new ArrayList<>();
                                if (retailerDatum.getMonthWiseSkuTarget() != null && !retailerDatum.getMonthWiseSkuTarget().isEmpty()) {
                                    for (MonthWiseTargetData monthWiseTargetData : retailerDatum.getMonthWiseSkuTarget()) {
                                        MonthWiseAnnualTargetModel monthWiseAnnualTargetModel = validateReviewForExistingRetailerDetailSaleForMonthWise(subAreaMaster.getPk().toString(), retailerDatum.getCustomerCode(), monthWiseTargetData.getMonthYear(), sclUser, data.getCustomerCode());
                                        monthWiseAnnualTargetModel.setMonthYear(monthWiseTargetData.getMonthYear());
                                        monthWiseAnnualTargetModel.setMonthTarget(monthWiseTargetData.getMonthTarget());
                                        monthWiseAnnualTargetModel.setSalesOfficer(sclUser);
                                        monthWiseAnnualTargetModel.setSubAreaMaster(subAreaMaster);
                                        monthWiseAnnualTargetModel.setRetailerCode(retailerDatum.getCustomerCode());
                                        monthWiseAnnualTargetModel.setCustomerCode(data.getCustomerCode());
                                        monthWiseAnnualTargetModel.setIsAnnualSalesReviewedForRetailerDetails(true);
                                        monthWiseAnnualTargetModel.setBrand(baseSite);
                                        monthWiseAnnualTargetModel.setRetailerRevisedAnnualSalesDetails(retailerRevisedAnnualSalesDetailsModel);
                                        monthWiseAnnualTargetModelsForRetailerDetail.add(monthWiseAnnualTargetModel);
                                        modelService.save(monthWiseAnnualTargetModel);
                                    }
                                    retailerRevisedAnnualSalesDetailsModel.setMonthWiseAnnualTarget(monthWiseAnnualTargetModelsForRetailerDetail);
                                    modelService.save(retailerRevisedAnnualSalesDetailsModel);
                                }

                            retailerRevisedAnnualSalesDetailsModel.setRetailerRevisedAnnualSales(retailerRevisedAnnualSalesModel);
                            modelService.save(retailerRevisedAnnualSalesDetailsModel);
                            retailerRevisedAnnualSalesDetailsModelList.add(retailerRevisedAnnualSalesDetailsModel);
                        }
                    }
                    retailerRevisedAnnualSalesModel.setListOfRetailersRevised(retailerRevisedAnnualSalesDetailsModelList);
                    retailerRevisedAnnualSalesModel.setAnnualSales(annualSalesModel);
                    modelService.save(retailerRevisedAnnualSalesModel);
                }
                else if (data.getIsNewDealerOnboarded()!=null && data.getIsNewDealerOnboarded().equals(true))
                {
                    String financialYear=findNextFinancialYear();
                    RetailerRevisedAnnualSalesModel retailerRevisedAnnualSalesModel = validateReviewForOnboardedRetailersSale(data.getCustomerCode(), subAreaMaster.getPk().toString(), sclUser, financialYear);
                    SclCustomerModel sclCustomer = (SclCustomerModel) getUserService().getUserForUID(data.getCustomerCode());
                    retailerRevisedAnnualSalesModel.setCustomerCode(sclCustomer.getUid());
                    retailerRevisedAnnualSalesModel.setCustomerName(data.getCustomerName());
                    retailerRevisedAnnualSalesModel.setCustomerPotential(data.getCustomerPotential());
                    retailerRevisedAnnualSalesModel.setErpCustomerNo(data.getErpCustomerNo());
                    retailerRevisedAnnualSalesModel.setFinancialYear(findNextFinancialYear());
                    retailerRevisedAnnualSalesModel.setSubAreaMaster(subAreaMaster);
                    retailerRevisedAnnualSalesModel.setIsNewDealerOnboarded(true);
                    //retailerRevisedAnnualSalesModel.setIsNewRetailerOnboarded(true);
                    retailerRevisedAnnualSalesModel.setStatus("OnboardedDealer");
                    retailerRevisedAnnualSalesModel.setTotalTarget(data.getTotalTarget());
                    retailerRevisedAnnualSalesModel.setBrand(baseSite);

                    List<MonthWiseAnnualTargetModel> monthListForOnboardRetailer = new ArrayList<>();
                    if (data.getMonthWiseTarget() != null &&
                            !data.getMonthWiseTarget().isEmpty()) {
                        for (MonthWiseTargetData monthWiseTargetData : data.getMonthWiseTarget()) {
                            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel = validateReviewForOnboardedRetailersSaleForMonthWise(subAreaMaster.getPk().toString(), data.getCustomerCode(), monthWiseTargetData.getMonthYear(), sclUser);
                            monthWiseAnnualTargetModel.setMonthYear(monthWiseTargetData.getMonthYear());
                            monthWiseAnnualTargetModel.setMonthTarget(monthWiseTargetData.getMonthTarget());
                            monthWiseAnnualTargetModel.setCustomerCode(data.getCustomerCode());
                            monthWiseAnnualTargetModel.setSubAreaMaster(subAreaMaster);
                            monthWiseAnnualTargetModel.setSalesOfficer(sclUser);
                            monthWiseAnnualTargetModel.setIsAnnualSalesOnboardedForRetailer(true);
                            monthWiseAnnualTargetModel.setBrand(baseSite);
                            monthListForOnboardRetailer.add(monthWiseAnnualTargetModel);
                            modelService.save(monthWiseAnnualTargetModel);
                        }
                        retailerRevisedAnnualSalesModel.setMonthWiseAnnualTarget(monthListForOnboardRetailer);
                    }
                    SelfCounterSaleData selfCounterSaleData = data.getSelfCounterSale();
                    SelfCounterSaleDetailsModel selfCounterSaleDetailsModel = validateOnboardedRetailerSelfCounterSale(subAreaMaster.getPk().toString(), selfCounterSaleData.getCustomerCode(), sclUser);
                    selfCounterSaleDetailsModel.setCustomerCode(selfCounterSaleData.getCustomerCode());
                    selfCounterSaleDetailsModel.setCustomerPotential(selfCounterSaleData.getCustomerPotential());
                    selfCounterSaleDetailsModel.setCustomerName(selfCounterSaleData.getCustomerName());
                    selfCounterSaleDetailsModel.setTotalTarget(selfCounterSaleData.getTotalTarget());
                    selfCounterSaleDetailsModel.setErpCustomerNo(selfCounterSaleData.getErpCustomerNo());
                    selfCounterSaleDetailsModel.setIsAnnualSalesOnboardedForRetailer(true);
                    selfCounterSaleDetailsModel.setSubAreaMaster(subAreaMaster);
                    selfCounterSaleDetailsModel.setSalesOfficer(sclUser);
                    selfCounterSaleDetailsModel.setBrand(baseSite);

                    List<MonthWiseAnnualTargetModel> monthWiseForSelfOnboardRetailer = new ArrayList<>();
                    for (MonthWiseTargetData monthWiseTargetData : selfCounterSaleData.getMonthWiseTarget()) {
                        MonthWiseAnnualTargetModel monthWiseAnnualTargetModel = validateOnboardedRetailerSelfCounterSaleMonthWise(subAreaMaster.getPk().toString(), selfCounterSaleData.getCustomerCode(), monthWiseTargetData.getMonthYear(), sclUser, data.getCustomerCode());
                        monthWiseAnnualTargetModel.setMonthYear(monthWiseTargetData.getMonthYear());
                        monthWiseAnnualTargetModel.setMonthTarget(monthWiseTargetData.getMonthTarget());
                        monthWiseAnnualTargetModel.setSubAreaMaster(subAreaMaster);
                        monthWiseAnnualTargetModel.setSalesOfficer(sclUser);
                        monthWiseAnnualTargetModel.setSelfCounterCustomerCode(selfCounterSaleData.getCustomerCode());
                        monthWiseAnnualTargetModel.setCustomerCode(data.getCustomerCode());
                        monthWiseAnnualTargetModel.setIsAnnualSalesOnboardedForRetailer(true);
                        monthWiseAnnualTargetModel.setBrand(baseSite);
                        monthWiseForSelfOnboardRetailer.add(monthWiseAnnualTargetModel);
                        modelService.save(monthWiseAnnualTargetModel);
                    }
                    selfCounterSaleDetailsModel.setMonthWiseAnnualTarget(monthWiseForSelfOnboardRetailer);
                    modelService.save(selfCounterSaleDetailsModel);
                    retailerRevisedAnnualSalesModel.setDealerSelfCounterSale(selfCounterSaleDetailsModel);

                    List<RetailerRevisedAnnualSalesDetailsModel> retailerDetailsListForOnboard = new ArrayList<>();
                    if (data.getRetailerData() != null && !data.getRetailerData().isEmpty()) {
                        for (RetailerDetailsData retailerDatum : data.getRetailerData()) {
                            // validate if product sales model is existing with setIsAnnualSalesReviewedForDealer flag
                            RetailerRevisedAnnualSalesDetailsModel retailerRevisedAnnualSalesDetailsModel = validateReviewForOnboardRetailerDetailsSale(subAreaMaster.getPk().toString(), retailerDatum.getCustomerCode(), sclUser);
                            retailerRevisedAnnualSalesDetailsModel.setCustomerCode(retailerDatum.getCustomerCode());
                            retailerRevisedAnnualSalesDetailsModel.setCustomerName(retailerDatum.getCustomerName());
                            retailerRevisedAnnualSalesDetailsModel.setCustomerPotential(retailerDatum.getCustomerPotential());
                            retailerRevisedAnnualSalesDetailsModel.setTotalTarget(retailerDatum.getTotalTarget());
                            retailerRevisedAnnualSalesDetailsModel.setFinancialYear(findNextFinancialYear());
                            retailerRevisedAnnualSalesDetailsModel.setSubAreaMaster(subAreaMaster);
                            retailerRevisedAnnualSalesDetailsModel.setIsNewRetailerOnboarded(true);
                            retailerRevisedAnnualSalesDetailsModel.setSalesOfficer(sclUser);
                            retailerRevisedAnnualSalesDetailsModel.setErpCustomerNo(retailerDatum.getErpCustomerNo());
                            retailerRevisedAnnualSalesDetailsModel.setBrand(baseSite);

                            List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetModelsForRetailerDetail = new ArrayList<>();
                            if (retailerDatum.getMonthWiseSkuTarget() != null && !retailerDatum.getMonthWiseSkuTarget().isEmpty()) {
                                for (MonthWiseTargetData monthWiseTargetData : retailerDatum.getMonthWiseSkuTarget()) {
                                    MonthWiseAnnualTargetModel monthWiseAnnualTargetModel = validateReviewForOnboardRetailerDetailSaleForMonthWise(subAreaMaster.getPk().toString(), retailerDatum.getCustomerCode(), monthWiseTargetData.getMonthYear(), sclUser, data.getCustomerCode());
                                    monthWiseAnnualTargetModel.setMonthYear(monthWiseTargetData.getMonthYear());
                                    monthWiseAnnualTargetModel.setMonthTarget(monthWiseTargetData.getMonthTarget());
                                    monthWiseAnnualTargetModel.setSalesOfficer(sclUser);
                                    monthWiseAnnualTargetModel.setSubAreaMaster(subAreaMaster);
                                    monthWiseAnnualTargetModel.setRetailerCode(retailerDatum.getCustomerCode());
                                    monthWiseAnnualTargetModel.setCustomerCode(data.getCustomerCode());
                                    monthWiseAnnualTargetModel.setIsAnnualSalesOnboardedForRetailer(true);
                                    monthWiseAnnualTargetModel.setBrand(baseSite);
                                    monthWiseAnnualTargetModel.setRetailerRevisedAnnualSalesDetails(retailerRevisedAnnualSalesDetailsModel);
                                    monthWiseAnnualTargetModelsForRetailerDetail.add(monthWiseAnnualTargetModel);
                                    modelService.save(monthWiseAnnualTargetModel);
                                }
                                retailerRevisedAnnualSalesDetailsModel.setMonthWiseAnnualTarget(monthWiseAnnualTargetModelsForRetailerDetail);
                                modelService.save(retailerRevisedAnnualSalesDetailsModel);
                            }

                            retailerRevisedAnnualSalesDetailsModel.setRetailerRevisedAnnualSales(retailerRevisedAnnualSalesModel);
                            modelService.save(retailerRevisedAnnualSalesDetailsModel);
                            retailerDetailsListForOnboard.add(retailerRevisedAnnualSalesDetailsModel);
                        }
                    }
                    retailerRevisedAnnualSalesModel.setListOfRetailersRevised(retailerDetailsListForOnboard);
                    retailerRevisedAnnualSalesModel.setAnnualSales(annualSalesModel);
                    modelService.save(retailerRevisedAnnualSalesModel);
                }
            }
        }
            modelService.save(annualSalesModel);
        }
      //  errorListWsDTO.setErrors(errorWsDTOList);
     //   return errorListWsDTO;
        return true;
    }

    private MonthWiseAnnualTargetModel validateReviewForOnboardRetailerDetailSaleForMonthWise(String subArea, String retailerCode, String monthYear, SclUserModel sclUser, String dealerCode) {
        MonthWiseAnnualTargetModel monthWiseAnnualTargetModelDetails = salesPlanningDao.validateReviewForOnboardRetailerDetailSaleForMonthWise(subArea, retailerCode, monthYear, sclUser, dealerCode);
        if(monthWiseAnnualTargetModelDetails !=null) {
            return monthWiseAnnualTargetModelDetails;
        }
        else {
            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel = modelService.create(MonthWiseAnnualTargetModel.class);
            return monthWiseAnnualTargetModel;
        }
    }

    private RetailerRevisedAnnualSalesDetailsModel validateReviewForOnboardRetailerDetailsSale(String subArea, String retailerCode, SclUserModel sclUser) {
        RetailerRevisedAnnualSalesDetailsModel details = salesPlanningDao.validateReviewForOnboardRetailerDetailsSale(subArea, retailerCode, sclUser);
        if(details!=null)
        {
            return details;
        }
        else {
            RetailerRevisedAnnualSalesDetailsModel retailerRevisedAnnualSalesDetailsModel = modelService.create(RetailerRevisedAnnualSalesDetailsModel.class);
            return retailerRevisedAnnualSalesDetailsModel;
        }
    }

    private MonthWiseAnnualTargetModel validateOnboardedRetailerSelfCounterSaleMonthWise(String subArea, String selfCounterCode, String monthYear, SclUserModel sclUser, String dealerCode) {
        MonthWiseAnnualTargetModel monthWiseAnnualTargetModelDetails = salesPlanningDao.validateOnboardedRetailerSelfCounterSaleMonthWise(subArea, selfCounterCode, monthYear, sclUser, dealerCode);
        if(monthWiseAnnualTargetModelDetails !=null) {
            return monthWiseAnnualTargetModelDetails;
        }
        else {
            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel = modelService.create(MonthWiseAnnualTargetModel.class);
            return monthWiseAnnualTargetModel;
        }
    }

    private SelfCounterSaleDetailsModel validateOnboardedRetailerSelfCounterSale(String subArea, String selfCounterCode, SclUserModel sclUser) {
        SelfCounterSaleDetailsModel selfCounterSaleDetailsModelDetails = salesPlanningDao.validateOnboardedRetailerSelfCounterSale(subArea, selfCounterCode, sclUser);
        if(selfCounterSaleDetailsModelDetails != null)
        {
            return  selfCounterSaleDetailsModelDetails;
        }
        else
        {
            SelfCounterSaleDetailsModel selfCounterSaleDetailsModel=modelService.create(SelfCounterSaleDetailsModel.class);
            return selfCounterSaleDetailsModel;
        }
    }

    private MonthWiseAnnualTargetModel validateReviewForOnboardedRetailersSaleForMonthWise(String subArea, String dealerCode, String monthYear, SclUserModel sclUser) {
        MonthWiseAnnualTargetModel monthWiseAnnualTargetModelDetails = salesPlanningDao.validateReviewForOnboardedRetailersSaleForMonthWise(subArea, dealerCode, monthYear, sclUser);
        if(monthWiseAnnualTargetModelDetails !=null) {
            return monthWiseAnnualTargetModelDetails;
        }
        else {
            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel = modelService.create(MonthWiseAnnualTargetModel.class);
            return monthWiseAnnualTargetModel;
        }
    }

    private RetailerRevisedAnnualSalesModel validateReviewForOnboardedRetailersSale(String dealerCode, String subArea, SclUserModel sclUser, String financialYear) {
        RetailerRevisedAnnualSalesModel retailerRevisedAnnualSalesModelDetails = salesPlanningDao.validateReviewForOnboardedRetailersSale(dealerCode, subArea, sclUser, financialYear);
        if(retailerRevisedAnnualSalesModelDetails !=null) {
            return retailerRevisedAnnualSalesModelDetails;
        }
        else {
            RetailerRevisedAnnualSalesModel retailerRevisedAnnualSalesModel = modelService.create(RetailerRevisedAnnualSalesModel.class);
            return retailerRevisedAnnualSalesModel;
        }
    }

    private MonthWiseAnnualTargetModel validateReviewForExistingRetailerDetailSaleForMonthWise(String subArea, String retailerCode, String monthYear, SclUserModel sclUser, String dealerCode) {
        MonthWiseAnnualTargetModel monthWiseAnnualTargetModelDetails = salesPlanningDao.validateReviewForExistingRetailerDetailSaleForMonthWise(subArea, retailerCode, monthYear, sclUser, dealerCode);
        if(monthWiseAnnualTargetModelDetails !=null) {
            return monthWiseAnnualTargetModelDetails;
        }
        else {
            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel = modelService.create(MonthWiseAnnualTargetModel.class);
            return monthWiseAnnualTargetModel;
        }
    }

    private RetailerRevisedAnnualSalesDetailsModel validateReviewForExistingRetailerDetailsSale(String subArea, String retailerCode, SclUserModel sclUser) {
        RetailerRevisedAnnualSalesDetailsModel details = salesPlanningDao.validateReviewForExistingRetailerDetailsSale(subArea, retailerCode, sclUser);
        if(details!=null)
        {
            return details;
        }
        else {
            RetailerRevisedAnnualSalesDetailsModel retailerRevisedAnnualSalesDetailsModel = modelService.create(RetailerRevisedAnnualSalesDetailsModel.class);
            return retailerRevisedAnnualSalesDetailsModel;
        }
    }

    private SelfCounterSaleDetailsModel validateReviewForExistingSelfCounterSale(String subArea, String selfCounterCode, SclUserModel sclUser) {
    SelfCounterSaleDetailsModel selfCounterSaleDetailsModelDetails = salesPlanningDao.validateReviewForExistingSelfCounterSale(subArea, selfCounterCode, sclUser);
    if(selfCounterSaleDetailsModelDetails != null)
    {
        return  selfCounterSaleDetailsModelDetails;
    }
    else
    {
        SelfCounterSaleDetailsModel selfCounterSaleDetailsModel=modelService.create(SelfCounterSaleDetailsModel.class);
        return selfCounterSaleDetailsModel;
    }
    }

    private MonthWiseAnnualTargetModel validateReviewForExistingSelfCounterSaleMonthWise(String subArea, String selfCounterCode, String monthYear, SclUserModel sclUser, String dealerCode) {
        MonthWiseAnnualTargetModel monthWiseAnnualTargetModelDetails = salesPlanningDao.validateReviewForExistingSelfCounterSaleMonthWise(subArea, selfCounterCode, monthYear, sclUser, dealerCode);
        if(monthWiseAnnualTargetModelDetails !=null) {
            return monthWiseAnnualTargetModelDetails;
        }
        else {
            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel = modelService.create(MonthWiseAnnualTargetModel.class);
            return monthWiseAnnualTargetModel;
        }
    }

    private MonthWiseAnnualTargetModel validateReviewForExistingRetailersSaleForMonthWise(String subArea, String dealerCode, String monthYear, SclUserModel sclUser) {
        MonthWiseAnnualTargetModel monthWiseAnnualTargetModelDetails = salesPlanningDao.validateReviewForExistingRetailersSaleForMonthWise(subArea, dealerCode, monthYear, sclUser);
        if(monthWiseAnnualTargetModelDetails !=null) {
            return monthWiseAnnualTargetModelDetails;
        }
        else {
            MonthWiseAnnualTargetModel monthWiseAnnualTargetModel = modelService.create(MonthWiseAnnualTargetModel.class);
            return monthWiseAnnualTargetModel;
        }
    }

    private RetailerRevisedAnnualSalesModel validateReviewForExistingRetailersSale(String dealerCode, String subArea, SclUserModel sclUser, String financialYear) {
        RetailerRevisedAnnualSalesModel retailerRevisedAnnualSalesModelDetails = salesPlanningDao.validateReviewForExistingRetailersSale(dealerCode, subArea, sclUser, financialYear);
        if(retailerRevisedAnnualSalesModelDetails !=null) {
            return retailerRevisedAnnualSalesModelDetails;
        }
        else {
            RetailerRevisedAnnualSalesModel retailerRevisedAnnualSalesModel = modelService.create(RetailerRevisedAnnualSalesModel.class);
            return retailerRevisedAnnualSalesModel;
        }
    }

    @Override
    public AnnualSalesModel viewPlannedSalesforDealersRetailersMonthWise(String subArea, SclUserModel sclUserModel, BaseSiteModel brand) {
        //AnnualSalesModel annualSalesModel = getSalesPlanningDao().viewPlannedSalesforDealersRetailersMonthwise(subArea,sclUserModel, brand);
        AnnualSalesModel annualSalesModel = getSalesPlanningDao().viewPlannedSalesforDealersRetailersMonthwise1(subArea,sclUserModel, brand);
        return annualSalesModel;
    }

    @Override
    public List<SclCustomerModel> getDealerDetailsForOnboarded(String subArea, int intervalPeriod, String filter) {
        Date startDate=null,endDate=null;
        List<Date> dates= getCurrentFY();
        startDate=dates.get(0);
        endDate=dates.get(1);
        List<SclCustomerModel> dealerDetailsForOnboarded = dealerDetailsForOnboarded = getSalesPlanningDao().getDealerDetailsForOnboarded(subArea, startDate, endDate, intervalPeriod, filter);
        return dealerDetailsForOnboarded;
    }

    @Override
    public List<SclCustomerModel> getDealerDetailsForOnboarded(String subArea, int intervalPeriod) {
        Date startDate=null,endDate=null;
        List<Date> dates= getCurrentFY();
        startDate=dates.get(0);
        endDate=dates.get(1);
        List<SclCustomerModel> dealerDetailsForOnboarded = dealerDetailsForOnboarded = getSalesPlanningDao().getDealerDetailsForOnboarded(subArea, startDate, endDate, intervalPeriod);
        return dealerDetailsForOnboarded;
    }

    @Override
    public List<SclCustomerModel> getRetailerDetailsForOnboarded(String subArea, int intervalPeriod, String filter) {
        Date startDate=null,endDate=null;
        List<Date> dates= getCurrentFY();
        startDate = dates.get(0);
        endDate = dates.get(1);
        List<SclCustomerModel> retailerDetailsForOnboarded = getSalesPlanningDao().getRetailerDetailsForOnboarded(subArea, startDate, endDate, intervalPeriod, filter);
        return retailerDetailsForOnboarded;
    }

    @Override
    public List<SclCustomerModel> getRetailerDetailsForOnboarded(String subArea, int intervalPeriod) {
        Date startDate=null,endDate=null;
        List<Date> dates= getCurrentFY();
        startDate = dates.get(0);
        endDate = dates.get(1);
        List<SclCustomerModel> retailerDetailsForOnboarded = getSalesPlanningDao().getRetailerDetailsForOnboarded(subArea, startDate, endDate, intervalPeriod);
        return retailerDetailsForOnboarded;
    }

    @Override
    public double getTotalTargetForDealersAfterTargetSetting(SclUserModel sclUser, String subArea, String financialYear, BaseSiteModel baseSite) {
        return salesPlanningDao.getTotalTargetForDealersAfterTargetSetting(sclUser,subArea,financialYear,baseSite);
    }

    @Override
    public double getTotalTargetForDealersAfterTargetSettingForRH(DistrictMasterModel districtMasterModel, String nextFinancialYear, BaseSiteModel baseSite) {
        return salesPlanningDao.getTotalTargetForDealersAfterTargetSettingForRH(districtMasterModel,nextFinancialYear,baseSite);
    }

    @Override
    public List<List<Object>> fetchProductSaleDetailsForSummaryAfterTargetSetting(String subArea, SclUserModel sclUser, BaseSiteModel baseSite) {
        return salesPlanningDao.fetchProductSaleDetailsForSummaryAfterTargetSetting(subArea,sclUser,baseSite);
    }

    @Override
    public List<List<Object>> fetchProductSaleDetailsForSummaryAfterTargetSettingForRH(DistrictMasterModel districtMasterModel, BaseSiteModel baseSite) {
        return salesPlanningDao.fetchProductSaleDetailsForSummaryAfterTargetSettingForRH(districtMasterModel,baseSite);
    }

    @Override
    public List<List<Object>> fetchProductSaleDetailsForSummaryAfterTargetSettingForRH(List<DistrictMasterModel> districtMasterModel, BaseSiteModel baseSite) {
        return salesPlanningDao.fetchProductSaleDetailsForSummaryAfterTargetSettingForRH(districtMasterModel,baseSite);
    }

    @Override
    public List<List<Object>> fetchProductSaleDetailsForSummaryAfterTargetSettingForTSM(List<SubAreaMasterModel> subAreaMasterModels, BaseSiteModel baseSite) {
        return salesPlanningDao.fetchProductSaleDetailsForSummaryAfterTargetSettingForTSM(subAreaMasterModels,baseSite);
    }

    @Override
    public double getTotalTargetForDealersAfterReview(SclUserModel sclUser, String subArea, String financialYear, BaseSiteModel baseSite) {
        return salesPlanningDao.getTotalTargetForDealersAfterReview(sclUser,subArea,financialYear,baseSite);
    }

    @Override
    public double getTotalTargetForDealersAfterReviewForRH(DistrictMasterModel districtMasterModel, String nextFinancialYear, BaseSiteModel baseSite) {
        return salesPlanningDao.getTotalTargetForDealersAfterReviewForRH(districtMasterModel,nextFinancialYear,baseSite);
    }

    @Override
    public List<List<Object>> fetchProductSaleDetailsForSummaryAfterReview(String subArea, SclUserModel sclUser, BaseSiteModel baseSite) {
        return salesPlanningDao.fetchProductSaleDetailsForSummaryAfterReview(subArea,sclUser,baseSite);
    }

    @Override
    public List<List<Object>> fetchProductSaleDetailsForSummaryAfterReviewForRH(DistrictMasterModel districtMasterModel, BaseSiteModel baseSite) {
        return salesPlanningDao.fetchProductSaleDetailsForSummaryAfterReviewForRH(districtMasterModel,baseSite);
    }

    @Override
    public List<List<Object>> fetchProductSaleDetailsForSummaryAfterReviewForRH(List<DistrictMasterModel> districtMasterModel, BaseSiteModel baseSite) {
        return salesPlanningDao.fetchProductSaleDetailsForSummaryAfterReviewForRH(districtMasterModel,baseSite);
    }

    @Override
    public List<List<Object>> fetchProductSaleDetailsForSummaryAfterReviewForTSM(List<SubAreaMasterModel> subAreaList, BaseSiteModel baseSite) {
        return salesPlanningDao.fetchProductSaleDetailsForSummaryAfterReviewForTSM(subAreaList,baseSite);
    }

    @Override
    public List<List<Object>> fetchProductSaleDetailsForSummaryAfterReviewForTSMRH(String subArea, SclUserModel sclUser, String disctrictCode, String regionCode) {
        return salesPlanningDao.fetchProductSaleDetailsForSummaryAfterReviewForTSMRH(subArea,sclUser,disctrictCode,regionCode);
    }

    @Override
    public double getPlannedMonthSaleForMonthlySaleSummary(SclUserModel sclUser, String currentMonthName, String subArea, BaseSiteModel baseSite) {
        return salesPlanningDao.getPlannedMonthSaleForMonthlySaleSummary(sclUser,currentMonthName,subArea,baseSite);
    }

    @Override
    public List<List<Object>> getLastYearShareForProduct(SclUserModel sclUser, BaseSiteModel baseSite, String subArea) {
        Date startDate=null,endDate=null;
        //List<Date> date = findFiscalYearforLastYearShare();
        List<Date> date = getCurrentFY();
        startDate=date.get(0);
        endDate=date.get(1);
        return salesPlanningDao.getLastYearShareForProduct(sclUser,baseSite,startDate,endDate,subArea);
    }

    @Override
    public List<List<Object>> getLastYearShareForTarget(SclUserModel sclUser, BaseSiteModel baseSite, String subArea) {
        Date startDate=null,endDate=null;
        //List<Date> date = findFiscalYearforLastYearShare();
        List<Date> date = getCurrentFY();
        startDate=date.get(0);
        endDate=date.get(1);
        return salesPlanningDao.getLastYearShareForTarget(sclUser,baseSite,startDate,endDate,subArea);
    }

    @Override
    public List<String> getLastYearShareForDealerTarget(String dealerCategory,SclUserModel sclUser, BaseSiteModel baseSite) {
        Date startDate=null,endDate=null;
        List<Date> date = findFiscalYearforLastYearShare();
        startDate=date.get(0);
        endDate=date.get(1);
        return salesPlanningDao.getLastYearShareForDealerTarget(dealerCategory,sclUser,baseSite,startDate,endDate);
    }

    @Override
    public List<List<Object>> getLastYearShareForProductMonthly(SclUserModel sclUser, BaseSiteModel baseSite) {
        Date currentDate = Calendar.getInstance().getTime();
        int monthInNumber = currentDate.getMonth();
        Date firstDateOfMonth = getFirstDateOfMonth(monthInNumber);
        Date lastDateOfMonth = getLastDateOfMonth(monthInNumber);
        List<List<Object>> lastYearShareForProductMonthly = salesPlanningDao.getLastYearShareForProductMonthly(sclUser, baseSite, firstDateOfMonth, lastDateOfMonth);
        return lastYearShareForProductMonthly;
    }

    @Override
    public List<List<Object>> getLastYearShareForTargetMonthly(SclUserModel sclUser, BaseSiteModel baseSite) {
        Date currentDate = Calendar.getInstance().getTime();
        int monthInNumber = currentDate.getMonth();
        Date firstDateOfMonth = getFirstDateOfMonth(monthInNumber);
        Date lastDateOfMonth = getLastDateOfMonth(monthInNumber);
        List<List<Object>> lastYearShareForTargetMonthly = salesPlanningDao.getLastYearShareForTargetMonthly(sclUser, baseSite, firstDateOfMonth, lastDateOfMonth);
        return lastYearShareForTargetMonthly;
    }

    @Override
    public List<String> getLastYearShareForDealerTargetMonthly(String dealerCategory, SclUserModel sclUser, BaseSiteModel baseSite) {
        Date currentDate = Calendar.getInstance().getTime();
        int monthInNumber = currentDate.getMonth();
        Date firstDateOfMonth = getFirstDateOfMonth(monthInNumber);
        Date lastDateOfMonth = getLastDateOfMonth(monthInNumber);
        List<String> lastYearShareForDealerTargetMonthly = salesPlanningDao.getLastYearShareForDealerTargetMonthly(dealerCategory, sclUser, baseSite, firstDateOfMonth, lastDateOfMonth);
        return lastYearShareForDealerTargetMonthly;
    }

    @Override
    public boolean saveAnnualSalesTargetSettingForDealers(AnnualSalesTargetSettingListData data, SclUserModel sclUser, String subArea) {

        /*AnnualSalesModel annualSalesModel=validateAnnualSalesModel(sclUser, subAreaMaster.getPk().toString());
            if (annualSalesModel.getSubAreaList() != null && !annualSalesModel.getSubAreaList().isEmpty()) {
                List<SubAreaListingModel> subAreaListModelsForAnnual = new ArrayList<>(annualSalesModel.getSubAreaList());
                if(annualSalesModel.getSubAreaList().contains(data.getSubArea())) {
                    for (SubAreaListingModel subAreaListModel : subAreaListModelsForAnnual) {
                        subAreaListModel.setSubAreaName(subArea);
                        subAreaListModelsForAnnual.add(subAreaListModel);
                        subAreaListModel.setAnnualSales(annualSalesModel);
                        annualSalesModel.setSubAreaList(subAreaListModelsForAnnual);
                        modelService.save(subAreaListModel);
                    }
                }
            } else {
                List<SubAreaListingModel> subAreaListModels = new ArrayList<>();
                SubAreaListingModel subAreaListModel = modelService.create(SubAreaListingModel.class);
                subAreaListModel.setSubAreaName(subArea);
                subAreaListModel.setAnnualSales(annualSalesModel);
                subAreaListModels.add(subAreaListModel);
                annualSalesModel.setSubAreaList(subAreaListModels);
                modelService.save(subAreaListModel);
            }

            annualSalesModel.setSalesOfficer(sclUser);
            annualSalesModel.setFinancialYear(findNextFinancialYear());
            annualSalesModel.setDealerPlannedTotalCySales(data.getTotalCurrentYearSales());
            annualSalesModel.setDealerPlannedTotalPlanSales(data.getTotalPlanSales());
            annualSalesModel.setIsAnnualSalesRevised(false);

            List<DealerPlannedAnnualSalesModel> dealerPlanList = new ArrayList<>();
            if (annualSalesModel.getDealerPlannedAnnualSales() == null || annualSalesModel.getDealerPlannedAnnualSales().isEmpty()) {
                if (data.getAnnualSalesTargetSetting() != null && !data.getAnnualSalesTargetSetting().isEmpty()) {
                    for (AnnualSalesTargetSettingData annualSalesTargetSettingData : data.getAnnualSalesTargetSetting()) {
                        DealerPlannedAnnualSalesModel dealerPlan = modelService.create(DealerPlannedAnnualSalesModel.class);
                        dealerPlan.setCustomerCode(annualSalesTargetSettingData.getCustomerCode());
                        dealerPlan.setCustomerName(annualSalesTargetSettingData.getCustomerName());
                        dealerPlan.setCustomerPotential(annualSalesTargetSettingData.getCustomerPotential());
                        dealerPlan.setCurrentYearSales(annualSalesTargetSettingData.getCurrentYearSales());
                        dealerPlan.setPlannedYearSales(annualSalesTargetSettingData.getPlanSales());
                        dealerPlan.setStatus(annualSalesTargetSettingData.getStatus());//draft
                        dealerPlan.setSubarea(subArea);
                        dealerPlan.setFinancialYear(findNextFinancialYear());
                        if (annualSalesTargetSettingData.getSkuDataList() != null && !annualSalesTargetSettingData.getSkuDataList().isEmpty()) {
                            List<ProductModel> skuList = new ArrayList<>();
                            for (SKUData skuData : annualSalesTargetSettingData.getSkuDataList()) {
                                ProductModel sku = productService.getProductForCode(skuData.getProductCode());
                                sku.setCode(skuData.getProductCode());
                                sku.setName(skuData.getProductName());
                                sku.setCySales(skuData.getCySales());
                                sku.setPlanSales(skuData.getPlanSales());
                                skuList.add(sku);
                                modelService.save(sku);
                            }
                            dealerPlan.setListOfSkus(skuList);
                            dealerPlanList.add(dealerPlan);
                        }
                        modelService.save(dealerPlan);
                        annualSalesModel.setDealerPlannedAnnualSales(dealerPlanList);
                        modelService.save(annualSalesModel);
                    }
                }
            }
        else {
            List<DealerPlannedAnnualSalesModel> listOfDealerPlanSales=new ArrayList<>(annualSalesModel.getDealerPlannedAnnualSales());
            for (DealerPlannedAnnualSalesModel dealerPlannedAnnualSale : listOfDealerPlanSales) {
                if(data.getAnnualSalesTargetSetting()!=null && !data.getAnnualSalesTargetSetting().isEmpty())
                {
                    for (AnnualSalesTargetSettingData annualSalesTargetSettingData : data.getAnnualSalesTargetSetting()) {
                        if (dealerPlannedAnnualSale.getCustomerCode().equals(annualSalesTargetSettingData.getCustomerCode())) {
                            dealerPlannedAnnualSale.setCustomerCode(annualSalesTargetSettingData.getCustomerCode());
                            dealerPlannedAnnualSale.setCustomerName(annualSalesTargetSettingData.getCustomerName());
                            dealerPlannedAnnualSale.setCustomerPotential(annualSalesTargetSettingData.getCustomerPotential());
                            dealerPlannedAnnualSale.setCurrentYearSales(annualSalesTargetSettingData.getCurrentYearSales());
                            dealerPlannedAnnualSale.setPlannedYearSales(annualSalesTargetSettingData.getPlanSales());
                            dealerPlannedAnnualSale.setStatus(annualSalesTargetSettingData.getStatus());//draft
                            dealerPlannedAnnualSale.setFinancialYear(findNextFinancialYear());
                            dealerPlannedAnnualSale.setSubarea(subArea);
                            if (annualSalesTargetSettingData.getSkuDataList() != null && !annualSalesTargetSettingData.getSkuDataList().isEmpty()) {
                                List<ProductModel> skuList = new ArrayList<>(dealerPlannedAnnualSale.getListOfSkus());
                                for (SKUData skuData : annualSalesTargetSettingData.getSkuDataList()) {
                                    if (dealerPlannedAnnualSale.getListOfSkus() != null && !dealerPlannedAnnualSale.getListOfSkus().isEmpty()) {
                                        for (ProductModel sku : skuList) {
                                            if (sku.getCode().equals(skuData.getProductCode())) {
                                                sku.setCode(skuData.getProductCode());
                                                sku.setName(skuData.getProductName());
                                                sku.setCySales(skuData.getCySales());
                                                sku.setPlanSales(skuData.getPlanSales());
                                                modelService.save(sku);
                                            }
                                        }
                                    }
                                }
                                dealerPlannedAnnualSale.setListOfSkus(skuList);
                                dealerPlanList.add(dealerPlannedAnnualSale);
                            }
                        }
                    }
                }
            modelService.save(dealerPlannedAnnualSale);
          }
            annualSalesModel.setDealerPlannedAnnualSales(dealerPlanList);
            modelService.save(annualSalesModel);
        }*/
        return true;
    }

    @Override
    public List<List<Object>> fetchDealerDetailsForSelectedRetailer(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, String filter) {
        //List<Date> dates = findFiscalYearforLastYearShare();
        List<Date> dates= getCurrentFY();
        Date startDate = dates.get(0);
        Date endDate = dates.get(1);
        return salesPlanningDao.fetchDealerDetailsForSelectedRetailer(subArea,sclUser,baseSite,filter,startDate,endDate);
    }

    @Override
    public List<List<Object>> fetchRetailerDetailsForSelectedDealer(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, String filter) {
        //List<Date> dates = findFiscalYearforLastYearShare();
        List<Date> dates= getCurrentFY();
        Date startDate = dates.get(0);
        Date endDate = dates.get(1);
        return salesPlanningDao.fetchRetailerDetailsForSelectedDealer(subArea,sclUser,baseSite,filter,startDate,endDate);
    }

    @Override
    public List<List<Object>> fetchDealerCySalesForRetailer(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, String customerCode) {
        //List<Date> dates = findFiscalYearforLastYearShare();
        List<Date> dates= getCurrentFY();
        Date startDate = dates.get(0);
        Date endDate = dates.get(1);
        return salesPlanningDao.fetchDealerCySalesForRetailer(subArea,sclUser,baseSite,customerCode,startDate,endDate);
    }

    @Override
    public DealerPlannedAnnualSalesModel fetchRecordForDealerPlannedAnnualSales(String subArea, SclUserModel sclUser, String filter) {
        return salesPlanningDao.fetchRecordForDealerPlannedAnnualSales(subArea,sclUser,filter);
    }

    @Override
    public RetailerPlannedAnnualSalesModel fetchRecordForRetailerPlannedAnnualSales(String subArea, SclUserModel sclUser, String filter) {
        return salesPlanningDao.fetchRecordForRetailerPlannedAnnualSales(subArea,sclUser,filter);
    }

    @Override
    public List<List<Object>> fetchProductSaleDetailsForSummary(String subArea, SclUserModel sclUser) {
        return salesPlanningDao.fetchProductSaleDetailsForSummary(subArea,sclUser);
    }

    @Override
    public Double getTotalTargetForDealers(SclUserModel sclUser, String subarea, String financialYear) {
        return salesPlanningDao.getTotalTargetForDealers(sclUser,subarea,financialYear);
    }

    @Override
    public Double getPlannedTargetAfterTargetSetMonthlySP(SclUserModel sclUser, String subArea) {

        //need to change once data available for future years for eg: no 2023 data so using 2024 for testing so +1
        LocalDate localDate=LocalDate.now();
        LocalDate nextMonth=localDate.plusMonths(1);
        int currentYear=localDate.plusYears(1).getYear();
        //int nextYear=localDate.plusYears(1).getYear();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = nextMonth.atStartOfDay(zone);
        String month = monthFormat.format(Date.from(dateTime.toInstant()));
        String formattedMonth=month.concat("-").concat(String.valueOf(currentYear));
        return salesPlanningDao.getPlannedTargetAfterTargetSetMonthlySP(sclUser,subArea,formattedMonth, String.valueOf(currentYear));
    }

    @Override
    public Double getRevisedTargetAfterTargetSetMonthlySP(SclUserModel sclUser, String subArea) {
        //need to change once data available for future years for eg: no 2023 data so using 2024 for testing so +1
        LocalDate localDate=LocalDate.now();
        LocalDate nextMonth=localDate.plusMonths(1);
        //int nextYear=localDate.plusYears(1).getYear();
        int currentYear=localDate.getYear();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = nextMonth.atStartOfDay(zone);
        String month = monthFormat.format(Date.from(dateTime.toInstant()));
        String formattedMonth=month.concat("-").concat(String.valueOf(currentYear));
        return salesPlanningDao.getRevisedTargetAfterTargetSetMonthlySP(sclUser,subArea,formattedMonth, String.valueOf(currentYear));
    }

    @Override
    public Double getPlannedTargetForReviewMonthlySP(SclUserModel sclUser, String subArea, BaseSiteModel site) {
        //need to change once data available for future years for eg: no 2023 data so using 2024 for testing so +1
        LocalDate localDate=LocalDate.now();
        LocalDate nextMonth=localDate.plusMonths(1);
        //int nextYear=localDate.plusYears(1).getYear();
        int currentYear=localDate.getYear();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = nextMonth.atStartOfDay(zone);
        String month = monthFormat.format(Date.from(dateTime.toInstant()));
        String formattedMonth=month.concat("-").concat(String.valueOf(currentYear));
        return salesPlanningDao.getPlannedTargetForReviewMonthlySP(sclUser,subArea,formattedMonth,String.valueOf(currentYear));
    }

    @Override
    public Double getRevisedTargetForReviewMonthlySP(SclUserModel sclUser, String subArea, BaseSiteModel site) {
        //need to change once data available for future years for eg: no 2023 data so using 2024 for testing so +1
        LocalDate localDate=LocalDate.now();
        LocalDate nextMonth=localDate.plusMonths(1);
        //int nextYear=localDate.plusYears(1).getYear();
        int currentYear=localDate.getYear();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = nextMonth.atStartOfDay(zone);
        String month = monthFormat.format(Date.from(dateTime.toInstant()));
        String formattedMonth=month.concat("-").concat(String.valueOf(currentYear));
        return salesPlanningDao.getRevisedTargetForReviewMonthlySP(sclUser,subArea,formattedMonth,String.valueOf(currentYear));
    }

    @Override
    public Double getPlannedTargetForReviewMonthlySPForRH(DistrictMasterModel districtMasterModel) {
        //need to change once data available for future years for eg: no 2023 data so using 2024 for testing so +1
        LocalDate localDate=LocalDate.now();
        LocalDate nextMonth=localDate.plusMonths(1);
        //int nextYear=localDate.plusYears(1).getYear();
        int currentYear=localDate.getYear();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = nextMonth.atStartOfDay(zone);
        String month = monthFormat.format(Date.from(dateTime.toInstant()));
        String formattedMonth=month.concat("-").concat(String.valueOf(currentYear));
        return salesPlanningDao.getPlannedTargetForReviewMonthlySPForRH(districtMasterModel,formattedMonth,String.valueOf(currentYear));
    }

    @Override
    public Double getRevisedTargetForReviewMonthlySPForRH(DistrictMasterModel districtMasterModel) {
        //need to change once data available for future years for eg: no 2023 data so using 2024 for testing so +1
        LocalDate localDate=LocalDate.now();
        LocalDate nextMonth=localDate.plusMonths(1);
        //int nextYear=localDate.plusYears(1).getYear();
        int currentYear=localDate.getYear();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = nextMonth.atStartOfDay(zone);
        String month = monthFormat.format(Date.from(dateTime.toInstant()));
        String formattedMonth=month.concat("-").concat(String.valueOf(currentYear));
        return salesPlanningDao.getRevisedTargetForReviewMonthlySPForRH(districtMasterModel,formattedMonth,String.valueOf(currentYear));
    }

    @Override
    public List<List<Object>> fetchProductMixDetailsAfterTargetSetMonthlySummary(String subArea, SclUserModel sclUser, BaseSiteModel baseSite) {
        //need to change once data available for future years for eg: no 2023 data so using 2024 for testing so +1
        LocalDate localDate=LocalDate.now();
        LocalDate nextMonth=localDate.plusMonths(1);
        //int nextYear=localDate.plusYears(1).getYear();
        int currentYear=localDate.getYear();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = nextMonth.atStartOfDay(zone);
        String month = monthFormat.format(Date.from(dateTime.toInstant()));
        String formattedMonth=month.concat("-").concat(String.valueOf(currentYear));
        return salesPlanningDao.fetchProductMixDetailsAfterTargetSetMonthlySummary(subArea,sclUser,formattedMonth,String.valueOf(currentYear),baseSite);
    }

    @Override
    public List<List<Object>> fetchProductMixDetailsAfterTargetSetMonthlySummaryForRH(DistrictMasterModel districtMasterModel) {
        //need to change once data available for future years for eg: no 2023 data so using 2024 for testing so +1
        LocalDate localDate=LocalDate.now();
        LocalDate nextMonth=localDate.plusMonths(1);
        //int nextYear=localDate.plusYears(1).getYear();
        int currentYear=localDate.getYear();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = nextMonth.atStartOfDay(zone);
        String month = monthFormat.format(Date.from(dateTime.toInstant()));
        String formattedMonth=month.concat("-").concat(String.valueOf(currentYear));
        return salesPlanningDao.fetchProductMixDetailsAfterTargetSetMonthlySummaryForRH(districtMasterModel,formattedMonth,String.valueOf(currentYear));
    }

    @Override
    public List<List<Object>> fetchProductMixDetailsAfterTargetSetMonthlySummaryForSumRH(List<DistrictMasterModel> districtMasterModel) {
        //need to change once data available for future years for eg: no 2023 data so using 2024 for testing so +1
        LocalDate localDate=LocalDate.now();
        LocalDate nextMonth=localDate.plusMonths(1);
        //int nextYear=localDate.plusYears(1).getYear();
        int currentYear=localDate.getYear();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = nextMonth.atStartOfDay(zone);
        String month = monthFormat.format(Date.from(dateTime.toInstant()));
        String formattedMonth=month.concat("-").concat(String.valueOf(currentYear));
        return salesPlanningDao.fetchProductMixDetailsAfterTargetSetMonthlySummaryForSumRH(districtMasterModel,formattedMonth,String.valueOf(currentYear));
    }

    @Override
    public List<List<Object>> fetchProductMixDetailsAfterTargetSetMonthlySummaryForTSM(List<SubAreaMasterModel> subArea, BaseSiteModel baseSite) {
        //need to change once data available for future years for eg: no 2023 data so using 2024 for testing so +1
        LocalDate localDate=LocalDate.now();
        LocalDate nextMonth=localDate.plusMonths(1);
        //int nextYear=localDate.plusYears(1).getYear();
        int currentYear=localDate.getYear();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = nextMonth.atStartOfDay(zone);
        String month = monthFormat.format(Date.from(dateTime.toInstant()));
        String formattedMonth=month.concat("-").concat(String.valueOf(currentYear));
        return salesPlanningDao.fetchProductMixDetailsAfterTargetSetMonthlySummaryForTSM(subArea,formattedMonth,String.valueOf(currentYear),baseSite);
    }

    @Override
    public List<List<Object>> fetchProductMixDetailsForReviewTargetMonthlySummary(String subArea, SclUserModel sclUser, BaseSiteModel baseSite) {
        //need to change once data available for future years for eg: no 2023 data so using 2024 for testing so +1
        LocalDate localDate=LocalDate.now();
        LocalDate nextMonth=localDate.plusMonths(1);
        //int nextYear=localDate.plusYears(1).getYear();
        int currentYear=localDate.getYear();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = nextMonth.atStartOfDay(zone);
        String month = monthFormat.format(Date.from(dateTime.toInstant()));
        String formattedMonth=month.concat("-").concat(String.valueOf(currentYear));
        return salesPlanningDao.fetchProductMixDetailsForReviewTargetMonthlySummary(subArea,sclUser,formattedMonth,String.valueOf(currentYear),baseSite);
    }

    @Override
    public List<List<Object>> fetchProductMixDetailsForReviewTargetMonthlySummaryForRH(DistrictMasterModel districtMasterModel) {
        //need to change once data available for future years for eg: no 2023 data so using 2024 for testing so +1
        LocalDate localDate=LocalDate.now();
        LocalDate nextMonth=localDate.plusMonths(1);
        //int nextYear=localDate.plusYears(1).getYear();
        int currentYear=localDate.getYear();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = nextMonth.atStartOfDay(zone);
        String month = monthFormat.format(Date.from(dateTime.toInstant()));
        String formattedMonth=month.concat("-").concat(String.valueOf(currentYear));
        return salesPlanningDao.fetchProductMixDetailsForReviewTargetMonthlySummaryForRH(districtMasterModel,formattedMonth,String.valueOf(currentYear));
    }

    @Override
    public List<List<Object>> fetchProductMixDetailsForReviewTargetMonthlySummaryForSumRH(List<DistrictMasterModel> districtMasterModel) {
        //need to change once data available for future years for eg: no 2023 data so using 2024 for testing so +1
        LocalDate localDate=LocalDate.now();
        LocalDate nextMonth=localDate.plusMonths(1);
        //int nextYear=localDate.plusYears(1).getYear();
        int currentYear=localDate.getYear();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = nextMonth.atStartOfDay(zone);
        String month = monthFormat.format(Date.from(dateTime.toInstant()));
        String formattedMonth=month.concat("-").concat(String.valueOf(currentYear));
        return salesPlanningDao.fetchProductMixDetailsForReviewTargetMonthlySummaryForSumRH(districtMasterModel,formattedMonth,String.valueOf(currentYear));
    }

    @Override
    public List<List<Object>> fetchProductMixDetailsForReviewTargetMonthlySummaryForTSM(List<SubAreaMasterModel> subArea, BaseSiteModel baseSite) {
        //need to change once data available for future years for eg: no 2023 data so using 2024 for testing so +1
        LocalDate localDate=LocalDate.now();
        LocalDate nextMonth=localDate.plusMonths(1);
        //int nextYear=localDate.plusYears(1).getYear();
        int currentYear=localDate.getYear();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = nextMonth.atStartOfDay(zone);
        String month = monthFormat.format(Date.from(dateTime.toInstant()));
        String formattedMonth=month.concat("-").concat(String.valueOf(currentYear));
        return salesPlanningDao.fetchProductMixDetailsForReviewTargetMonthlySummaryForTSM(subArea,formattedMonth,String.valueOf(currentYear), baseSite);
    }

    @Override
    public Double getPlannedTargetAfterReviewMonthlySP(SclUserModel sclUser, String subArea,BaseSiteModel site) {
        //need to change once data available for future years for eg: no 2023 data so using 2024 for testing so +1
        LocalDate localDate=LocalDate.now();
        LocalDate nextMonth=localDate.plusMonths(1);
        //int nextYear=localDate.plusYears(1).getYear();
        int currentYear=localDate.getYear();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = nextMonth.atStartOfDay(zone);
        String month = monthFormat.format(Date.from(dateTime.toInstant()));
        String formattedMonth=month.concat("-").concat(String.valueOf(currentYear));
        return salesPlanningDao.getPlannedTargetAfterReviewMonthlySP(sclUser,subArea,formattedMonth,String.valueOf(currentYear));
    }

    @Override
    public Double getRevisedTargetAfterReviewMonthlySP(SclUserModel sclUser, String subArea,BaseSiteModel site) {
        //need to change once data available for future years for eg: no 2023 data so using 2024 for testing so +1
        LocalDate localDate=LocalDate.now();
        LocalDate nextMonth=localDate.plusMonths(1);
        //int nextYear=localDate.plusYears(1).getYear();
        int currentYear=localDate.getYear();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = nextMonth.atStartOfDay(zone);
        String month = monthFormat.format(Date.from(dateTime.toInstant()));
        String formattedMonth=month.concat("-").concat(String.valueOf(currentYear));
        return salesPlanningDao.getRevisedTargetAfterReviewMonthlySP(sclUser,subArea,formattedMonth,String.valueOf(currentYear),site);
    }

    @Override
    public Double getPlannedTargetAfterReviewMonthlySPForRH(DistrictMasterModel districtMasterModel) {
        //need to change once data available for future years for eg: no 2023 data so using 2024 for testing so +1
        LocalDate localDate=LocalDate.now();
        LocalDate nextMonth=localDate.plusMonths(1);
        //int nextYear=localDate.plusYears(1).getYear();
        int currentYear=localDate.getYear();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = nextMonth.atStartOfDay(zone);
        String month = monthFormat.format(Date.from(dateTime.toInstant()));
        String formattedMonth=month.concat("-").concat(String.valueOf(currentYear));
        return salesPlanningDao.getPlannedTargetAfterReviewMonthlySPForRH(districtMasterModel,formattedMonth,String.valueOf(currentYear));
    }

    @Override
    public Double getRevisedTargetAfterReviewMonthlySPForRH(DistrictMasterModel districtMasterModel) {
        //need to change once data available for future years for eg: no 2023 data so using 2024 for testing so +1
        LocalDate localDate=LocalDate.now();
        LocalDate nextMonth=localDate.plusMonths(1);
        //int nextYear=localDate.plusYears(1).getYear();
        int currentYear=localDate.getYear();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = nextMonth.atStartOfDay(zone);
        String month = monthFormat.format(Date.from(dateTime.toInstant()));
        String formattedMonth=month.concat("-").concat(String.valueOf(currentYear));
        return salesPlanningDao.getRevisedTargetAfterReviewMonthlySPForRH(districtMasterModel,formattedMonth,String.valueOf(currentYear));
    }

    @Override
    public List<DealerPlannedMonthlySalesModel> fetchDealerPlannedMonthlySalesDetails(String subArea, SclUserModel sclUser) {
        //need to change once data available for future years for eg: no 2023 data so using 2024 for testing so +1
        LocalDate localDate=LocalDate.now();
        LocalDate nextMonth=localDate.plusMonths(1);
        //int nextYear=localDate.plusYears(1).getYear();
        int currentYear=localDate.getYear();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = nextMonth.atStartOfDay(zone);
        String month = monthFormat.format(Date.from(dateTime.toInstant()));
        String formattedMonth=month.concat("-").concat(String.valueOf(currentYear));
        return salesPlanningDao.fetchDealerPlannedMonthlySalesDetails(subArea,sclUser,formattedMonth,String.valueOf(currentYear));
    }

    //model to be change to DealerReviewMonthlySalesModel - when revise target approval process will implement
    @Override
    public List<DealerRevisedMonthlySalesModel> fetchDealerReviewedMonthlySalesDetails(String subArea, SclUserModel sclUser) {
        //need to change once data available for future years for eg: no 2023 data so using 2024 for testing so +1
        LocalDate localDate=LocalDate.now();
        LocalDate nextMonth=localDate.plusMonths(1);
       // int nextYear=localDate.plusYears(1).getYear();
        int currentYear=localDate.getYear();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = nextMonth.atStartOfDay(zone);
        String month = monthFormat.format(Date.from(dateTime.toInstant()));
        String formattedMonth=month.concat("-").concat(String.valueOf(currentYear));
        return salesPlanningDao.fetchDealerReviewedMonthlySalesDetails(subArea,sclUser,formattedMonth,String.valueOf(currentYear));
    }

    String findNextFinancialYear()
    {
        LocalDate date = LocalDate.now();
        int currentYear=date.getYear();
        int fyYear=currentYear+1;
        StringBuilder f=new StringBuilder();
        return String.valueOf(f.append(String.valueOf(currentYear)).append("-").append(String.valueOf(fyYear)));
    }

    @Override
    public List<List<Object>> viewDealerDetailsForAnnualSales(String subArea, SclUserModel sclUser, BaseSiteModel baseSite) {
        //List<Date> dates = findFiscalYearforLastYearShare();
        List<Date> dates= getCurrentFY();
        Date startDate = dates.get(0);
        Date endDate = dates.get(1);
        LOG.info("start Date viewDealerDetailsForAnnualSales :" +startDate + " " + "end Date viewDealerDetailsForAnnualSales:" + endDate);
        return salesPlanningDao.viewDealerDetailsForAnnualSales(subArea,sclUser,baseSite,startDate, endDate);
    }

    @Override
    public List<List<Object>> fetchDealerCySalesForAnnualSales(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, String customerCode) {
        //List<Date> dates = findFiscalYearforLastYearShare();
        List<Date> dates= getCurrentFY();
        Date startDate = dates.get(0);
        Date endDate = dates.get(1);
        LOG.info("fetchDealerCySalesForAnnualSales ::" + startDate + " " + endDate);
        return salesPlanningDao.fetchDealerCySalesForAnnualSales(subArea,sclUser,baseSite,startDate, endDate,customerCode);
    }

    @Override
    public List<List<Object>> viewDealerDetailsForAnnualSales(String subArea, SclUserModel sclUser, BaseSiteModel baseSite,String filter) {
        //List<Date> dates = findFiscalYearforLastYearShare();
        List<Date> dates= getCurrentFY();
        Date startDate = dates.get(0);
        Date endDate = dates.get(1);
        LOG.info("viewDealerDetailsForAnnualSales ::" + startDate + " " + endDate);
        return salesPlanningDao.viewDealerDetailsForAnnualSales(subArea,sclUser,baseSite,startDate, endDate,filter);
    }

    @Override
    public List<List<Object>> viewRetailerDetailsForAnnualSales(String customerCode, String subArea, SclUserModel sclUser, BaseSiteModel baseSite) {
        //List<Date> dates = findFiscalYearforLastYearShare();
        List<Date> dates= getCurrentFY();
        Date startDate = dates.get(0);
        Date endDate = dates.get(1);
        return salesPlanningDao.viewRetailerDetailsForAnnualSales(customerCode, subArea, sclUser, baseSite, startDate, endDate);
    }

    @Override
    public List<List<Object>> viewMonthWiseDealerDetailsForAnnualSales(BaseSiteModel baseSite, SclUserModel sclUser, String subArea) {
        //List<Date> dates = findFiscalYearforLastYearShare();
        List<Date> dates= getCurrentFY();
        Date startDate = dates.get(0);
        Date endDate = dates.get(1);
        return salesPlanningDao.viewMonthWiseDealerDetailsForAnnualSales(subArea,sclUser,baseSite,startDate, endDate);
    }

    @Override
    public List<List<Object>> viewMonthWiseRetailerDetailsForAnnualSales(BaseSiteModel baseSite, SclUserModel sclUser, String subArea) {
        //List<Date> dates = findFiscalYearforLastYearShare();
        List<Date> dates= getCurrentFY();
        Date startDate = dates.get(0);
        Date endDate = dates.get(1);
        return salesPlanningDao.viewMonthWiseRetailerDetailsForAnnualSales(subArea,sclUser,baseSite,startDate, endDate);
    }
    List<Date> findFiscalYearforLastYearShare(){
        List<Date> dates=new ArrayList<>();
        Date startDate,endDate;
        Year starYear,endYear;
        LocalDate currentDate = LocalDate.now();
        Month currentMonth = currentDate.getMonth();
        Year currentYear = Year.of(currentDate.getYear()-1);
        Calendar c1 = Calendar.getInstance();
        if(currentMonth.getValue() < 4  ){
            starYear= Year.of(currentYear.getValue()-1);
            c1.set(starYear.getValue(),03,01);
            startDate = c1.getTime();
            endYear= Year.of(currentYear.getValue());
            c1.set(endYear.getValue(),02,31);
            endDate = c1.getTime();
            dates.add(startDate);
            dates.add(endDate);
        }
        else {
            starYear=currentYear;
            c1.set(starYear.getValue(),03,01);
            startDate = c1.getTime();
            endYear= Year.of(currentYear.getValue()+1);
            c1.set(endYear.getValue(),02,31);
            endDate = c1.getTime();
            dates.add(startDate);
            dates.add(endDate);
        }
        return dates;
    }

    @Override
    public DealerRevisedAnnualSalesModel viewMonthlySalesTargetForDealers(String subArea,SclUserModel sclUser) {
        DealerRevisedAnnualSalesModel dealerRevisedAnnualSalesModel = salesPlanningDao.viewMonthlySalesTargetForDealers(subArea, sclUser);
        return dealerRevisedAnnualSalesModel;
    }

   @Override
    public boolean submitPlannedMonthlySalesTargetSettingForDealers(MonthlySalesTargetSettingListData listData) {
        final ErrorListWsDTO errorListWsDTO = new ErrorListWsDTO();
        final List<ErrorWsDTO> errorWsDTOList = new ArrayList<>();
        BaseSiteModel baseSite=baseSiteService.getCurrentBaseSite();
        List<DealerPlannedMonthlySalesModel> dealerPlannedMonthlySalesModels=new ArrayList<>();

        double totBuk1=0.0,totBuk2=0.0,totBuk3=0.0,totPT=0.0,totRT=0.0;
        if(listData.getMonthlySalesTargetSettingData()!=null && !listData.getMonthlySalesTargetSettingData().isEmpty())
        {

            SclUserModel sclUser = (SclUserModel) userService.getCurrentUser();
            SubAreaMasterModel subAreaMaster = territoryManagementService.getTerritoryById(listData.getSubAreaId());
            MonthlySalesModel monthlySalesModel = validateMonthlySalesModel(sclUser,subAreaMaster.getPk().toString());

            if(monthlySalesModel!=null) {
                if(monthlySalesModel.getSubAreaMasterList()!=null && !monthlySalesModel.getSubAreaMasterList().isEmpty()) {
                    List<SubAreaMasterModel> subAreaMasterModelList=new ArrayList<>(monthlySalesModel.getSubAreaMasterList());
                    for (SubAreaMasterModel subAreaMasterModel : monthlySalesModel.getSubAreaMasterList()) {
                        if (subAreaMasterModel.equals(subAreaMaster)) {
                            monthlySalesModel.setSo(sclUser);
                            monthlySalesModel.setDealerPlannedTotalPlannedTarget(listData.getTotalPlannedTarget() != null ? listData.getTotalPlannedTarget() : 0.0);
                            monthlySalesModel.setDealerPlannedTotalRevisedTarget(listData.getTotalRevisedTarget() != null ? listData.getTotalRevisedTarget() : 0.0);
                            monthlySalesModel.setIsMonthlySalesPlanned(true);
                            monthlySalesModel.setBrand(baseSite);
                            subAreaMasterModelList.add(subAreaMaster);
                            monthlySalesModel.setSubAreaMasterList(subAreaMasterModelList);
                        }
                    }
                }
                else {
                    List<SubAreaMasterModel> subAreaMasterModelList=new ArrayList<>();
                    monthlySalesModel.setSo(sclUser);
                    monthlySalesModel.setDealerPlannedTotalPlannedTarget(listData.getTotalPlannedTarget() != null ? listData.getTotalPlannedTarget() : 0.0);
                    monthlySalesModel.setDealerPlannedTotalRevisedTarget(listData.getTotalRevisedTarget() != null ? listData.getTotalRevisedTarget() : 0.0);
                    monthlySalesModel.setIsMonthlySalesPlanned(true);
                    monthlySalesModel.setBrand(baseSite);
                    subAreaMasterModelList.add(subAreaMaster);
                    monthlySalesModel.setSubAreaMasterList(subAreaMasterModelList);
                }
            }
            if(subAreaMaster != null) {
                monthlySalesModel.setSubAreaMaster(subAreaMaster);
                DistrictMasterModel district = subAreaMaster.getDistrictMaster();
                if(district!=null) {
                    monthlySalesModel.setDistrictMaster(district);
                    RegionMasterModel region = district.getRegion();
                    if(region!=null) {
                        monthlySalesModel.setRegionMaster(region);
                    }
                    StateMasterModel state=region.getState();
                    if(state!=null)
                    {
                        monthlySalesModel.setStateMaster(state);
                    }
                }
            }
            modelService.save(monthlySalesModel);

            for (MonthlySalesTargetSettingData monthlySalesTargetSettingDatum : listData.getMonthlySalesTargetSettingData()) {
                DealerPlannedMonthlySalesModel dealerPlannedMonthlySalesModel = validateDealerPlannedMonthlySales(sclUser,monthlySalesTargetSettingDatum.getCustomerCode(),subAreaMaster.getPk().toString(),monthlySalesTargetSettingDatum.getMonthName(),monthlySalesTargetSettingDatum.getMonthYear());
                DealerRevisedAnnualSalesModel dealerRevisedAnnualSalesModel = salesPlanningDao.getDealerRevisedAnnualDetailsForMonthlySales(monthlySalesTargetSettingDatum.getCustomerCode());
                dealerPlannedMonthlySalesModel.setCustomerCode(monthlySalesTargetSettingDatum.getCustomerCode());
                dealerPlannedMonthlySalesModel.setCustomerName(monthlySalesTargetSettingDatum.getCustomerName());
                dealerPlannedMonthlySalesModel.setCustomerPotential(monthlySalesTargetSettingDatum.getCustomerPotential());
                dealerPlannedMonthlySalesModel.setMonthName(monthlySalesTargetSettingDatum.getMonthName());
                dealerPlannedMonthlySalesModel.setMonthYear(monthlySalesTargetSettingDatum.getMonthYear());
                dealerPlannedMonthlySalesModel.setPlannedTarget(monthlySalesTargetSettingDatum.getPlannedTarget());
                dealerPlannedMonthlySalesModel.setRevisedTarget(monthlySalesTargetSettingDatum.getRevisedTarget());
                dealerPlannedMonthlySalesModel.setBucket1(monthlySalesTargetSettingDatum.getBucket1());
                dealerPlannedMonthlySalesModel.setBucket2(monthlySalesTargetSettingDatum.getBucket2());
                dealerPlannedMonthlySalesModel.setBucket3(monthlySalesTargetSettingDatum.getBucket3());
                dealerPlannedMonthlySalesModel.setSubAreaMaster(subAreaMaster);
                dealerPlannedMonthlySalesModel.setBrand(baseSite);
                //set dealerRevisedAnnualSales also for month changes
                if(subAreaMaster != null) {
                    DistrictMasterModel district = subAreaMaster.getDistrictMaster();
                    if(district!=null) {
                        dealerPlannedMonthlySalesModel.setDistrictMaster(district);
                        RegionMasterModel region = district.getRegion();
                        if(region!=null) {
                            dealerPlannedMonthlySalesModel.setRegionMaster(region);
                        }
                        StateMasterModel state=region.getState();
                        if(state!=null)
                        {
                            dealerPlannedMonthlySalesModel.setStateMaster(state);
                        }
                    }
                }
                dealerPlannedMonthlySalesModel.setDealerRevisedAnnualSales(dealerRevisedAnnualSalesModel);
                modelService.save(dealerPlannedMonthlySalesModel);

                if (monthlySalesTargetSettingDatum.getMonthlySkuDataList() != null && !monthlySalesTargetSettingDatum.getMonthlySkuDataList().isEmpty()) {
                    List<ProductModel> skuList = new ArrayList<>();
                    //skuList.addAll(dealerPlannedMonthlySalesModel.getListOfSkus());
                    for (MonthlySKUData monthlySKUData : monthlySalesTargetSettingDatum.getMonthlySkuDataList()) {
                            //List<ProductModel> sku = skuList.stream().filter(p -> p.getCode().equals(monthlySKUData.getProductCode())).collect(Collectors.toList());
                                List<ProductSaleModel> productSaleModelList = new ArrayList<>();
                                ProductModel productModel = getProductService().getProductForCode(monthlySKUData.getProductCode());
                                ProductSaleModel productSaleModel = validateProductSalesForDealerPlannedMonthlySales(subAreaMaster.getPk().toString(), sclUser, monthlySKUData.getProductCode(), monthlySalesTargetSettingDatum.getCustomerCode(), monthlySalesTargetSettingDatum.getMonthName(), monthlySalesTargetSettingDatum.getMonthYear());
                                productSaleModel.setCustomerCode(monthlySalesTargetSettingDatum.getCustomerCode());
                                productSaleModel.setProductCode(monthlySKUData.getProductCode());
                                productSaleModel.setProductName(monthlySKUData.getProductName());
                                productSaleModel.setProductGrade(productModel.getGrade());
                                productSaleModel.setProductPackaging(productModel.getPackagingCondition());
                                productSaleModel.setProductPackType(productModel.getBagType());
                                productSaleModel.setPlannedTarget(monthlySKUData.getPlannedTarget());
                                productSaleModel.setRevisedTarget(monthlySKUData.getRevisedTarget());
                                productSaleModel.setIsMonthlySalesForPlannedDealer(true);
                                productSaleModel.setBucket1(monthlySKUData.getBucket1());
                                productSaleModel.setBucket2(monthlySKUData.getBucket2());
                                productSaleModel.setBucket3(monthlySKUData.getBucket3());
                                productSaleModel.setMonthName(monthlySalesTargetSettingDatum.getMonthName());
                                productSaleModel.setMonthYear(monthlySalesTargetSettingDatum.getMonthYear());
                                productSaleModel.setSalesOfficer(sclUser);
                                productSaleModel.setPremium(monthlySKUData.getPremium());
                                productSaleModel.setSubAreaMaster(subAreaMaster);
                        if(subAreaMaster != null) {
                            DistrictMasterModel district = subAreaMaster.getDistrictMaster();
                            if(district!=null) {
                                productSaleModel.setDistrictMaster(district);
                                RegionMasterModel region = district.getRegion();
                                if(region!=null) {
                                    productSaleModel.setRegionMaster(region);
                                }
                                StateMasterModel state=region.getState();
                                if(state!=null)
                                {
                                    productSaleModel.setStateMaster(state);
                                }
                            }
                        }
                                productSaleModel.setBrand(baseSite);
                                modelService.save(productSaleModel);
                                productSaleModelList.add(productSaleModel);
                                productModel.setProductSale(productSaleModelList);
                                modelService.save(productModel);
                                skuList.add(productModel);
                            }
                    dealerPlannedMonthlySalesModel.setListOfSkus(skuList);
                    dealerPlannedMonthlySalesModel.setMonthlySales(monthlySalesModel);
                    monthlySalesModel.setMonthName(monthlySalesTargetSettingDatum.getMonthName());
                    monthlySalesModel.setMonthYear(monthlySalesTargetSettingDatum.getMonthYear());
                    modelService.save(monthlySalesModel);
                    modelService.save(dealerPlannedMonthlySalesModel);
                    dealerPlannedMonthlySalesModels.add(dealerPlannedMonthlySalesModel);
                    totBuk1 += dealerPlannedMonthlySalesModel.getBucket1();
                    totBuk2 += dealerPlannedMonthlySalesModel.getBucket2();
                    totBuk3 += dealerPlannedMonthlySalesModel.getBucket3();
                    totPT += dealerPlannedMonthlySalesModel.getPlannedTarget();
                    totRT += dealerPlannedMonthlySalesModel.getRevisedTarget();
                    modelService.save(dealerPlannedMonthlySalesModel);
                }
            }
            monthlySalesModel.setTotalBucket1(totBuk1);
            monthlySalesModel.setTotalBucket2(totBuk2);
            monthlySalesModel.setTotalBucket3(totBuk3);
            monthlySalesModel.setTotalPlannedTarget(totPT);
            monthlySalesModel.setTotalRevisedTarget(totRT);
            monthlySalesModel.setDealerPlannedMonthlySales(dealerPlannedMonthlySalesModels);
            monthlySalesModel.setTotalTarget(totBuk1+totBuk2+totBuk3+totPT+totRT);
            modelService.save(monthlySalesModel);

            SclWorkflowModel sclWorkflowModel = null;
            if(monthlySalesModel.getSclWorkflow()==null)
            {
                sclWorkflowModel= sclWorkflowService.saveWorkflow(MSP_BOTTOM_UP_WORKFLOW, WorkflowStatus.START, WorkflowType.MONTHLY_SALES_PLANNING);
                sclWorkflowModel.setStatus(WorkflowStatus.START);
                modelService.save(sclWorkflowModel);
                monthlySalesModel.setSclWorkflow(sclWorkflowModel);
                modelService.save(monthlySalesModel);
            }
            else
            {
                sclWorkflowModel = monthlySalesModel.getSclWorkflow();
            }
            //logic for auto approve-rh
        }
        //     errorListWsDTO.setErrors(errorWsDTOList);
        //      return errorListWsDTO;
        return true;
    }

    private ProductSaleModel validateProductSalesForDealerPlannedMonthlySales(String subArea, SclUserModel sclUser, String productCode, String customerCode, String monthName, String monthYear) {
        ProductSaleModel productSaleDetails = getSalesPlanningDao().checkExistingProductSaleForDealerPlannedMonthlySales(subArea, sclUser, productCode, customerCode,monthName,monthYear);
        if(productSaleDetails!=null){
            return  productSaleDetails;
        }
        else{
            ProductSaleModel productSaleModel=modelService.create(ProductSaleModel.class);
            return productSaleModel;
        }
    }

    private DealerPlannedMonthlySalesModel validateDealerPlannedMonthlySales(SclUserModel sclUser, String customerCode, String subArea, String monthName, String monthYear) {
        DealerPlannedMonthlySalesModel dealerPlannedMonthlySalesDetails = getSalesPlanningDao().checkExistingDealerPlannedMonthlySales(sclUser,customerCode,subArea,monthName,monthYear);
        if(dealerPlannedMonthlySalesDetails!=null){
            return  dealerPlannedMonthlySalesDetails;
        }
        else{
            DealerPlannedMonthlySalesModel dealerPlannedMonthlySalesModel=modelService.create(DealerPlannedMonthlySalesModel.class);
            return dealerPlannedMonthlySalesModel;
        }
    }

    @Override
    public boolean submitRevisedMonthlySalesTargetSettingForDealers(MonthlySalesTargetSettingListData listData) {
        final ErrorListWsDTO errorListWsDTO = new ErrorListWsDTO();
        final List<ErrorWsDTO> errorWsDTOList = new ArrayList<>();
        List<DealerRevisedMonthlySalesModel> dealerRevisedMonthlySalesModels = new ArrayList<>();
        List<ProductModel> productModels = new ArrayList<>();
        double totBuk1 = 0.0, totBuk2 = 0.0, totBuk3 = 0.0, totPT = 0.0, totRT = 0.0;
        if (listData.getMonthlySalesTargetSettingData() != null && !listData.getMonthlySalesTargetSettingData().isEmpty()) {
            SclUserModel sclUser = (SclUserModel) userService.getCurrentUser();
            MonthlySalesModel monthlySalesModel = validateMonthlySalesModel(sclUser,listData.getSubArea());
            if(monthlySalesModel!=null) {
                if (monthlySalesModel.getSubAreaList() != null && !monthlySalesModel.getSubAreaList().isEmpty()) {
                    List<SubAreaListingModel> subAreaList = new ArrayList<>(monthlySalesModel.getSubAreaList());
                    for (SubAreaListingModel subAreaListingModel : monthlySalesModel.getSubAreaList()) {
                        if (subAreaListingModel.getSubAreaName().equals(listData.getSubArea())) {
                            for (SubAreaListingModel subAreaListModel : monthlySalesModel.getSubAreaList()) {
                                subAreaListModel.setSubAreaName(listData.getSubArea());
                                subAreaList.add(subAreaListModel);
                                subAreaListModel.setMonthlySales(monthlySalesModel);
                                monthlySalesModel.setSubAreaList(subAreaList);
                                modelService.save(subAreaListModel);
                            }
                        }
                    }
                }  else {
                    List<SubAreaListingModel> subAreaListModels = new ArrayList<>();
                    SubAreaListingModel subAreaListModel = modelService.create(SubAreaListingModel.class);
                    subAreaListModel.setSubAreaName(listData.getSubArea());
                    subAreaListModel.setMonthlySales(monthlySalesModel);
                    subAreaListModels.add(subAreaListModel);
                    monthlySalesModel.setSubAreaList(subAreaListModels);
                    modelService.save(subAreaListModel);
                }
            }
            monthlySalesModel.setSo(sclUser);
            monthlySalesModel.setDealerRevisedTotalPlannedTarget(listData.getTotalPlannedTarget()!=null?listData.getTotalPlannedTarget():0.0);
            monthlySalesModel.setDealerRevisedTotalRevisedTarget(listData.getTotalRevisedTarget()!=null?listData.getTotalRevisedTarget():0.0);
            monthlySalesModel.setIsMonthlySalesRevised(true);
            modelService.save(monthlySalesModel);
            
            for (MonthlySalesTargetSettingData monthlySalesTargetSettingDatum : listData.getMonthlySalesTargetSettingData()) {
                if (monthlySalesTargetSettingDatum.getIsMonthlySalesRevised().equals(true)) {
                    DealerRevisedMonthlySalesModel dealerRevisedMonthlySalesModel = modelService.create(DealerRevisedMonthlySalesModel.class);
                    DealerRevisedAnnualSalesModel dealerRevisedAnnualSalesModel = salesPlanningDao.getDealerRevisedAnnualDetailsForMonthlySales(monthlySalesTargetSettingDatum.getCustomerCode());
                    dealerRevisedMonthlySalesModel.setCustomerCode(monthlySalesTargetSettingDatum.getCustomerCode());
                    dealerRevisedMonthlySalesModel.setCustomerName(monthlySalesTargetSettingDatum.getCustomerName());
                    dealerRevisedMonthlySalesModel.setCustomerPotential(monthlySalesTargetSettingDatum.getCustomerPotential());
                    dealerRevisedMonthlySalesModel.setMonthName(monthlySalesTargetSettingDatum.getMonthName());
                    dealerRevisedMonthlySalesModel.setMonthYear(monthlySalesTargetSettingDatum.getMonthYear());
                    monthlySalesModel.setMonthName(monthlySalesTargetSettingDatum.getMonthName());
                    monthlySalesModel.setMonthYear(monthlySalesTargetSettingDatum.getMonthYear());
                    dealerRevisedMonthlySalesModel.setPlannedTarget(monthlySalesTargetSettingDatum.getPlannedTarget());
                    dealerRevisedMonthlySalesModel.setRevisedTarget(monthlySalesTargetSettingDatum.getRevisedTarget());
                    dealerRevisedMonthlySalesModel.setBucket1(monthlySalesTargetSettingDatum.getBucket1());
                    dealerRevisedMonthlySalesModel.setBucket2(monthlySalesTargetSettingDatum.getBucket2());
                    dealerRevisedMonthlySalesModel.setBucket3(monthlySalesTargetSettingDatum.getBucket3());
                    List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetModels=new ArrayList<>();
                    for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : dealerRevisedAnnualSalesModel.getMonthWiseAnnualTarget()) {
                        if(monthWiseAnnualTargetModel.getMonthYear().equals(dealerRevisedMonthlySalesModel.getMonthName()) &&
                                monthWiseAnnualTargetModel.getCustomerCode().equals(dealerRevisedMonthlySalesModel.getCustomerCode())){
                            //setting dealerRevisedAnnualSales also for month changes
                            monthWiseAnnualTargetModel.setMonthTarget(dealerRevisedMonthlySalesModel.getRevisedTarget());
                            monthWiseAnnualTargetModel.setMonthYear(dealerRevisedMonthlySalesModel.getMonthName());
                            monthWiseAnnualTargetModel.setSalesOfficer(sclUser);
                            monthWiseAnnualTargetModel.setSubarea(listData.getSubArea());
                            monthWiseAnnualTargetModels.add(monthWiseAnnualTargetModel);
                            dealerRevisedAnnualSalesModel.setMonthWiseAnnualTarget(monthWiseAnnualTargetModels);
                            modelService.save(dealerRevisedAnnualSalesModel);
                        }
                    }

                    List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetModelsForSKU=new ArrayList<>();
                    List<ProductModel> productModelListforDealerRevised=new ArrayList<>();
                    dealerRevisedMonthlySalesModel.setDealerRevisedAnnualSales(dealerRevisedAnnualSalesModel);
                    for (MonthlySKUData monthlySKUData : monthlySalesTargetSettingDatum.getMonthlySkuDataList()) {
                        List<ProductSaleModel> productSaleModelList=new ArrayList<>();
                        ProductModel productModel = getProductService().getProductForCode(monthlySKUData.getProductCode());
                        ProductSaleModel productSaleModel=modelService.create(ProductSaleModel.class);
                        productSaleModel.setCustomerCode(monthlySalesTargetSettingDatum.getCustomerCode());
                        productSaleModel.setProductCode(monthlySKUData.getProductCode());
                        productSaleModel.setProductName(monthlySKUData.getProductName());
                        productSaleModel.setProductGrade(productModel.getGrade());
                        productSaleModel.setProductPackaging(productModel.getPackagingCondition());
                        productSaleModel.setProductPackType(productModel.getBagType());
                        productSaleModel.setPlannedTarget(monthlySKUData.getPlannedTarget());
                        productSaleModel.setRevisedTarget(monthlySKUData.getRevisedTarget());
                        productSaleModel.setBucket1(monthlySKUData.getBucket1());
                        productSaleModel.setBucket2(monthlySKUData.getBucket2());
                        productSaleModel.setBucket3(monthlySKUData.getBucket3());
                        productSaleModel.setIsAnnualSalesRevisedForDealer(true);
                        productSaleModelList.add(productSaleModel);
                        productModel.setProductSale(productSaleModelList);
                        productModels.add(productModel);
                        modelService.save(productModel);
                        for (ProductModel listOfSkus : dealerRevisedAnnualSalesModel.getListOfSkus()) {
                            for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : listOfSkus.getMonthWiseAnnualTarget()) {
                                if(monthWiseAnnualTargetModel.getMonthYear().equals(dealerRevisedMonthlySalesModel.getMonthName()) &&
                                        monthWiseAnnualTargetModel.getProductCode().equals(monthlySKUData.getProductCode()) &&
                                        monthWiseAnnualTargetModel.getCustomerCode().equals(dealerRevisedMonthlySalesModel.getCustomerCode())){
                                    monthWiseAnnualTargetModel.setMonthYear(dealerRevisedMonthlySalesModel.getMonthName());
                                    monthWiseAnnualTargetModel.setMonthTarget(monthlySKUData.getRevisedTarget());
                                    monthWiseAnnualTargetModel.setSalesOfficer(sclUser);
                                    monthWiseAnnualTargetModel.setSubarea(listData.getSubArea());
                                    monthWiseAnnualTargetModelsForSKU.add(monthWiseAnnualTargetModel);
                                    productModelListforDealerRevised.add(listOfSkus);
                                    dealerRevisedAnnualSalesModel.setListOfSkus(productModelListforDealerRevised);
                                    modelService.save(productModel);
                                    modelService.save(dealerRevisedAnnualSalesModel);
                                }
                            }
                        }
                    }
                    dealerRevisedMonthlySalesModel.setListOfSkus(productModels);
                    dealerRevisedMonthlySalesModel.setMonthlySales(monthlySalesModel);
                    modelService.save(dealerRevisedMonthlySalesModel);
                    dealerRevisedMonthlySalesModels.add(dealerRevisedMonthlySalesModel);
                    totBuk1 += dealerRevisedMonthlySalesModel.getBucket1();
                    totBuk2 += dealerRevisedMonthlySalesModel.getBucket2();
                    totBuk3 += dealerRevisedMonthlySalesModel.getBucket3();
                    totPT += dealerRevisedMonthlySalesModel.getPlannedTarget();
                    totRT+=dealerRevisedMonthlySalesModel.getRevisedTarget();
                }
                monthlySalesModel.setTotalBucket1(totBuk1);
                monthlySalesModel.setTotalBucket2(totBuk2);
                monthlySalesModel.setTotalBucket3(totBuk3);
                monthlySalesModel.setTotalPlannedTarget(totPT);
                monthlySalesModel.setTotalRevisedTarget(totRT);
                monthlySalesModel.setDealerRevisedMonthlySales(dealerRevisedMonthlySalesModels);
                monthlySalesModel.setTotalTarget(totBuk1 + totBuk2 + totBuk3 + totPT + totRT);
                modelService.save(monthlySalesModel);
                modelService.save(dealerRevisedMonthlySalesModels);
                //doubt for - it will saved into monthlysales as well as dealerrevisedmonthly sales
            }
        }
       // errorListWsDTO.setErrors(errorWsDTOList);
       // return errorListWsDTO;
        return true;
    }

    @Override
    public MonthlySalesModel viewMonthlySalesTargetForPlannedTab(String subArea,SclUserModel sclUser) {
        MonthlySalesModel monthlySalesModel = salesPlanningDao.viewMonthlySalesTargetForPlannedTab(subArea, sclUser);
        return monthlySalesModel;
    }

    @Override
    public List<List<Object>> viewMonthlyRevisedSalesTargetForReviewTab(String subArea, BaseSiteModel baseSite, SclUserModel sclUser, String customerCode, Date firstDayOfMonth, Date lastDayOfMonth) {
        return salesPlanningDao.viewMonthlyRevisedSalesTargetForReviewTab(subArea,sclUser,baseSite,customerCode,firstDayOfMonth,lastDayOfMonth);
    }

   public MonthlySalesModel validateMonthlySalesModel(SclUserModel sclUser, String subArea)
    {
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        //MonthlySalesModel monthlySalesModelDetail = salesPlanningDao.getMonthlySalesModelDetail(sclUser, getMonthPlus(new Date()), getYear(new Date()), subArea,currentBaseSite);
        MonthlySalesModel monthlySalesModelDetail = salesPlanningDao.getMonthlySalesModelDetail1(sclUser, getMonthPlus(new Date()), getYear(new Date()), subArea,currentBaseSite);

        if(monthlySalesModelDetail!=null) {
            return monthlySalesModelDetail;
        }
        else {
            MonthlySalesModel monthlySalesModel=modelService.create(MonthlySalesModel.class);
            return  monthlySalesModel;
        }
    }
    
    @Override
    public boolean submitMonthlySalesTargetForReviewTab(MonthlySalesTargetSettingListData monthlySalesTargetSettingListData) {
        final ErrorListWsDTO errorListWsDTO = new ErrorListWsDTO();
        final List<ErrorWsDTO> errorWsDTOList = new ArrayList<>();
        BaseSiteModel baseSite= baseSiteService.getCurrentBaseSite();
        List<DealerRevisedMonthlySalesModel> dealerRevisedMonthlySalesModels = new ArrayList<>();
        double totBuk1 = 0.0, totBuk2 = 0.0, totBuk3 = 0.0, totPT = 0.0, totRT = 0.0;
        SubAreaMasterModel subAreaMaster=territoryManagementService.getTerritoryById(monthlySalesTargetSettingListData.getSubAreaId());
        if (monthlySalesTargetSettingListData.getMonthlySalesTargetSettingData() != null && !monthlySalesTargetSettingListData.getMonthlySalesTargetSettingData().isEmpty()) {
            SclUserModel sclUser = (SclUserModel) userService.getCurrentUser();
            MonthlySalesModel monthlySalesModel = validateMonthlySalesModel(sclUser, subAreaMaster.getPk().toString());
            if(monthlySalesModel!=null) {
                if(monthlySalesModel.getSubAreaMasterList()!=null && !monthlySalesModel.getSubAreaMasterList().isEmpty()) {
                    List<SubAreaMasterModel> subAreaMasterModelList=new ArrayList<>(monthlySalesModel.getSubAreaMasterList());
                    for (SubAreaMasterModel subAreaMasterModel : monthlySalesModel.getSubAreaMasterList()) {
                        if (subAreaMasterModel.equals(subAreaMaster)) {
                            monthlySalesModel.setSo(sclUser);
                            monthlySalesModel.setDealerReviewedTotalPlannedTarget(monthlySalesTargetSettingListData.getTotalPlannedTarget() != null ? monthlySalesTargetSettingListData.getTotalPlannedTarget() : 0.0);
                            monthlySalesModel.setDealerReviewedTotalRevisedTarget(monthlySalesTargetSettingListData.getTotalRevisedTarget() != null ? monthlySalesTargetSettingListData.getTotalRevisedTarget() : 0.0);
                            monthlySalesModel.setIsMonthlySalesReviewed(true);
                            monthlySalesModel.setBrand(baseSite);
                            subAreaMasterModelList.add(subAreaMaster);
                            monthlySalesModel.setSubAreaMasterList(subAreaMasterModelList);
                        }
                    }
                }
                else {
                    List<SubAreaMasterModel> subAreaMasterModelList=new ArrayList<>();
                    monthlySalesModel.setSo(sclUser);
                    monthlySalesModel.setDealerReviewedTotalPlannedTarget(monthlySalesTargetSettingListData.getTotalPlannedTarget() != null ? monthlySalesTargetSettingListData.getTotalPlannedTarget() : 0.0);
                    monthlySalesModel.setDealerReviewedTotalRevisedTarget(monthlySalesTargetSettingListData.getTotalRevisedTarget() != null ? monthlySalesTargetSettingListData.getTotalRevisedTarget() : 0.0);
                    monthlySalesModel.setIsMonthlySalesReviewed(true);
                    monthlySalesModel.setBrand(baseSite);
                    subAreaMasterModelList.add(subAreaMaster);
                    monthlySalesModel.setSubAreaMasterList(subAreaMasterModelList);
                }
            }

            if(subAreaMaster != null) {
                monthlySalesModel.setSubAreaMaster(subAreaMaster);
                DistrictMasterModel district = subAreaMaster.getDistrictMaster();
                if(district!=null) {
                    monthlySalesModel.setDistrictMaster(district);
                    RegionMasterModel region = district.getRegion();
                    if(region!=null) {
                        monthlySalesModel.setRegionMaster(region);
                    }
                    StateMasterModel state=region.getState();
                    if(state!=null)
                    {
                        monthlySalesModel.setStateMaster(state);
                    }
                }
            }
            modelService.save(monthlySalesModel);

            for (MonthlySalesTargetSettingData monthlySalesTargetSettingDatum : monthlySalesTargetSettingListData.getMonthlySalesTargetSettingData()) {
                if (monthlySalesTargetSettingDatum.getIsMonthlySalesRevised().equals(true)) {
                    DealerRevisedMonthlySalesModel dealerRevisedMonthlySalesModel = validateDealerRevisedMonthlySales(sclUser,monthlySalesTargetSettingDatum.getCustomerCode(),subAreaMaster.getPk().toString(),monthlySalesTargetSettingDatum.getMonthName(),monthlySalesTargetSettingDatum.getMonthYear());
                    //DealerRevisedAnnualSalesModel dealerRevisedAnnualSalesModel = salesPlanningDao.getDealerRevisedAnnualDetailsForMonthlySales(monthlySalesTargetSettingDatum.getCustomerCode());
                    dealerRevisedMonthlySalesModel.setCustomerCode(monthlySalesTargetSettingDatum.getCustomerCode());
                    dealerRevisedMonthlySalesModel.setCustomerName(monthlySalesTargetSettingDatum.getCustomerName());
                    dealerRevisedMonthlySalesModel.setCustomerPotential(monthlySalesTargetSettingDatum.getCustomerPotential());
                    dealerRevisedMonthlySalesModel.setMonthName(monthlySalesTargetSettingDatum.getMonthName());
                    dealerRevisedMonthlySalesModel.setMonthYear(monthlySalesTargetSettingDatum.getMonthYear());
                    dealerRevisedMonthlySalesModel.setPlannedTarget(monthlySalesTargetSettingDatum.getPlannedTarget());
                    dealerRevisedMonthlySalesModel.setRevisedTarget(monthlySalesTargetSettingDatum.getRevisedTarget());
                    dealerRevisedMonthlySalesModel.setBucket1(monthlySalesTargetSettingDatum.getBucket1());
                    dealerRevisedMonthlySalesModel.setBucket2(monthlySalesTargetSettingDatum.getBucket2());
                    dealerRevisedMonthlySalesModel.setBucket3(monthlySalesTargetSettingDatum.getBucket3());
                    dealerRevisedMonthlySalesModel.setSubAreaMaster(subAreaMaster);
                    dealerRevisedMonthlySalesModel.setBrand(baseSite);
                    if(subAreaMaster != null) {
                        DistrictMasterModel district = subAreaMaster.getDistrictMaster();
                        if(district!=null) {
                            dealerRevisedMonthlySalesModel.setDistrictMaster(district);
                            RegionMasterModel region = district.getRegion();
                            if(region!=null) {
                                dealerRevisedMonthlySalesModel.setRegionMaster(region);
                            }
                            StateMasterModel state=region.getState();
                            if(state!=null)
                            {
                                dealerRevisedMonthlySalesModel.setStateMaster(state);
                            }
                        }
                    }
                    //List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetModels=new ArrayList<>();
                    /*for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : dealerRevisedAnnualSalesModel.getMonthWiseAnnualTarget()) {
                        if(monthWiseAnnualTargetModel.getMonthYear().equals(dealerRevisedMonthlySalesModel.getMonthName()) &&
                                monthWiseAnnualTargetModel.getCustomerCode().equals(dealerRevisedMonthlySalesModel.getCustomerCode())){
                            //setting dealerRevisedAnnualSales also for month changes
                            monthWiseAnnualTargetModel.setMonthTarget(dealerRevisedMonthlySalesModel.getRevisedTarget());
                            monthWiseAnnualTargetModel.setMonthYear(dealerRevisedMonthlySalesModel.getMonthName());
                            monthWiseAnnualTargetModels.add(monthWiseAnnualTargetModel);
                            dealerRevisedAnnualSalesModel.setMonthWiseAnnualTarget(monthWiseAnnualTargetModels);
                            modelService.save(dealerRevisedAnnualSalesModel);
                        }
                    }*/

                   // List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetModelsForSKU=new ArrayList<>();
                   //List<ProductModel> productModelListforDealerRevised=new ArrayList<>();
                    //dealerRevisedMonthlySalesModel.setDealerRevisedAnnualSales(dealerRevisedAnnualSalesModel);
                    List<ProductModel> skuList = new ArrayList<>();
                    for (MonthlySKUData monthlySKUData : monthlySalesTargetSettingDatum.getMonthlySkuDataList()) {
                        List<ProductSaleModel> productSaleModelList=new ArrayList<>();
                        ProductModel productModel = getProductService().getProductForCode(monthlySKUData.getProductCode());
                        ProductSaleModel productSaleModel=validateProductSalesForDealerRevisedMonthlySales(subAreaMaster.getPk().toString(), sclUser, monthlySKUData.getProductCode(), monthlySalesTargetSettingDatum.getCustomerCode(), monthlySalesTargetSettingDatum.getMonthName(), monthlySalesTargetSettingDatum.getMonthYear());
                        productSaleModel.setCustomerCode(monthlySalesTargetSettingDatum.getCustomerCode());
                        productSaleModel.setProductCode(monthlySKUData.getProductCode());
                        productSaleModel.setProductName(monthlySKUData.getProductName());
                        productSaleModel.setProductGrade(productModel.getGrade());
                        productSaleModel.setProductPackaging(productModel.getPackagingCondition());
                        productSaleModel.setProductPackType(productModel.getBagType());
                        productSaleModel.setPlannedTarget(monthlySKUData.getPlannedTarget());
                        productSaleModel.setRevisedTarget(monthlySKUData.getRevisedTarget());
                        productSaleModel.setBucket1(monthlySKUData.getBucket1());
                        productSaleModel.setBucket2(monthlySKUData.getBucket2());
                        productSaleModel.setBucket3(monthlySKUData.getBucket3());
                        productSaleModel.setSubAreaMaster(subAreaMaster);
                        productSaleModel.setSalesOfficer(sclUser);
                        productSaleModel.setIsMonthlySalesForReviewedDealer(true);
                        productSaleModel.setMonthYear(monthlySalesTargetSettingDatum.getMonthYear());
                        productSaleModel.setMonthName(monthlySalesTargetSettingDatum.getMonthName());
                        productSaleModel.setBrand(baseSite);
                        if(subAreaMaster != null) {
                            DistrictMasterModel district = subAreaMaster.getDistrictMaster();
                            if(district!=null) {
                                productSaleModel.setDistrictMaster(district);
                                RegionMasterModel region = district.getRegion();
                                if(region!=null) {
                                    productSaleModel.setRegionMaster(region);
                                }
                                StateMasterModel state=region.getState();
                                if(state!=null)
                                {
                                    productSaleModel.setStateMaster(state);
                                }
                            }
                        }
                        modelService.save(productSaleModel);
                        productSaleModelList.add(productSaleModel);
                        productModel.setProductSale(productSaleModelList);
                        modelService.save(productModel);
                        skuList.add(productModel);
                       /*{ for (ProductModel listOfSkus : dealerRevisedAnnualSalesModel.getListOfSkus())
                            for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : listOfSkus.getMonthWiseAnnualTarget()) {
                                if(monthWiseAnnualTargetModel.getMonthYear().equals(dealerRevisedMonthlySalesModel.getMonthName()) &&
                                        monthWiseAnnualTargetModel.getProductCode().equals(monthlySKUData.getProductCode()) &&
                                        monthWiseAnnualTargetModel.getCustomerCode().equals(dealerRevisedMonthlySalesModel.getCustomerCode())){
                                    monthWiseAnnualTargetModel.setMonthYear(dealerRevisedMonthlySalesModel.getMonthName());
                                    monthWiseAnnualTargetModel.setMonthTarget(monthlySKUData.getRevisedTarget());
                                    monthWiseAnnualTargetModelsForSKU.add(monthWiseAnnualTargetModel);
                                    listOfSkus.setMonthWiseAnnualTarget(monthWiseAnnualTargetModelsForSKU);
                                    productModelListforDealerRevised.add(listOfSkus);
                                    dealerRevisedAnnualSalesModel.setListOfSkus(productModelListforDealerRevised);
                                    modelService.save(dealerRevisedAnnualSalesModel);
                                }
                            }
                        }*/
                    }
                    dealerRevisedMonthlySalesModel.setListOfSkus(skuList);
                    dealerRevisedMonthlySalesModel.setMonthlySales(monthlySalesModel);
                    modelService.save(dealerRevisedMonthlySalesModel);
                    dealerRevisedMonthlySalesModels.add(dealerRevisedMonthlySalesModel);
                    totBuk1 += dealerRevisedMonthlySalesModel.getBucket1();
                    totBuk2 += dealerRevisedMonthlySalesModel.getBucket2();
                    totBuk3 += dealerRevisedMonthlySalesModel.getBucket3();
                    totPT += dealerRevisedMonthlySalesModel.getPlannedTarget();
                    totRT+=dealerRevisedMonthlySalesModel.getRevisedTarget();
                }
                monthlySalesModel.setTotalBucket1(totBuk1);
                monthlySalesModel.setTotalBucket2(totBuk2);
                monthlySalesModel.setTotalBucket3(totBuk3);
                monthlySalesModel.setTotalPlannedTarget(totPT);
                monthlySalesModel.setTotalRevisedTarget(totRT);
                monthlySalesModel.setDealerRevisedMonthlySales(dealerRevisedMonthlySalesModels);
                monthlySalesModel.setTotalTarget(totBuk1 + totBuk2 + totBuk3 + totPT + totRT);
                monthlySalesModel.setMonthName(monthlySalesTargetSettingDatum.getMonthName());
                monthlySalesModel.setMonthYear(monthlySalesTargetSettingDatum.getMonthYear());
                modelService.save(monthlySalesModel);
                //doubt for - it will saved into monthlysales as well as dealerrevisedmonthly sales
            }
        }
     //   errorListWsDTO.setErrors(errorWsDTOList);
      //  return errorListWsDTO;
        return true;
    }

    private ProductSaleModel validateProductSalesForDealerRevisedMonthlySales(String subArea, SclUserModel sclUser, String productCode, String customerCode, String monthName, String monthYear) {
        ProductSaleModel productSaleDetails = getSalesPlanningDao().checkExistingProductSaleForDealerRevisedMonthlySales(subArea, sclUser, productCode, customerCode,monthName,monthYear);
        if(productSaleDetails!=null){
            return  productSaleDetails;
        }
        else{
            ProductSaleModel productSaleModel=modelService.create(ProductSaleModel.class);
            return productSaleModel;
        }
    }

    private DealerRevisedMonthlySalesModel validateDealerRevisedMonthlySales(SclUserModel sclUser, String customerCode, String subArea, String monthName, String monthYear) {
        DealerRevisedMonthlySalesModel dealerRevisedMonthlySalesModelDetails = getSalesPlanningDao().checkExistingDealerRevisedMonthlySales(sclUser,customerCode,subArea,monthName,monthYear);
        if(dealerRevisedMonthlySalesModelDetails!=null){
            return  dealerRevisedMonthlySalesModelDetails;
        }
        else{
            DealerRevisedMonthlySalesModel dealerRevisedMonthlySalesModel=modelService.create(DealerRevisedMonthlySalesModel.class);
            return dealerRevisedMonthlySalesModel;
        }
    }

    @Override
    public boolean saveMonthWiseDealersDetailsForAnnSales(AnnualSalesMonthWiseTargetData data, String subArea) {
        SclUserModel sclUser=(SclUserModel) getUserService().getCurrentUser();
        BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
        AnnualSalesModel annualSales = salesPlanningDao.viewPlannedSalesforDealersRetailersMonthwise(subArea, sclUser, brand);
        if(annualSales!=null )
        {
            List<DealerRevisedAnnualSalesModel> dealerReviseList=new ArrayList<>(annualSales.getDealerRevisedAnnualSales());
            if(dealerReviseList!=null && !dealerReviseList.isEmpty())
            {
                for (DealerRevisedAnnualSalesModel dealerRevisedAnnualSale : dealerReviseList) {
                    if(dealerRevisedAnnualSale.getCustomerCode().equals(data.getCustomerCode()))
                    {
                        dealerRevisedAnnualSale.setCustomerCode(data.getCustomerCode());
                        dealerRevisedAnnualSale.setCustomerName(data.getCustomerName());
                        dealerRevisedAnnualSale.setCustomerPotential(data.getCustomerPotential()!=null?data.getCustomerPotential():0.0);
                        dealerRevisedAnnualSale.setTotalTarget(data.getTotalTarget()!=null?data.getTotalTarget():0.0);

                        List<MonthWiseAnnualTargetModel> monthWiseDealerSaleList=new ArrayList<>(dealerRevisedAnnualSale.getMonthWiseAnnualTarget());
                        if(monthWiseDealerSaleList!=null && !monthWiseDealerSaleList.isEmpty())
                        {
                            for (MonthWiseAnnualTargetModel monthWiseDealerSale : monthWiseDealerSaleList) {
                                if(data.getMonthWiseTarget()!=null && !data.getMonthWiseTarget().isEmpty())
                                {
                                    for (MonthWiseTargetData monthWiseTargetData : data.getMonthWiseTarget()) {
                                        monthWiseDealerSale.setCustomerCode(data.getCustomerCode());
                                        monthWiseDealerSale.setMonthYear(monthWiseTargetData.getMonthYear());
                                        monthWiseDealerSale.setMonthTarget(monthWiseTargetData.getMonthTarget());
                                        modelService.save(monthWiseDealerSale);
                                    }
                                }
                            }
                        }
                        dealerRevisedAnnualSale.setMonthWiseAnnualTarget(monthWiseDealerSaleList);

                        List<ProductModel> skuList=new ArrayList<>(dealerRevisedAnnualSale.getListOfSkus());
                        if(skuList!=null && !skuList.isEmpty())
                        {
                            for (ProductModel productModel : skuList) {
                            if(data.getSkuDataList()!=null && !data.getSkuDataList().isEmpty())
                            {
                                for (SKUData skuData : data.getSkuDataList()) {
                                    if(productModel.getCode().equals(skuData.getProductCode()))
                                    {
                                        productModel.setCode(skuData.getProductCode());
                                        productModel.setName(skuData.getProductName());
                                        productModel.setTotalTarget(skuData.getTotalTarget());

                                        List<MonthWiseAnnualTargetModel> skuMonthWiseList=new ArrayList<>(productModel.getMonthWiseAnnualTarget());
                                        if(skuMonthWiseList!=null && !skuMonthWiseList.isEmpty()) {
                                            for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : skuMonthWiseList) {
                                                if(skuData.getMonthWiseSkuTarget()!=null && !skuData.getMonthWiseSkuTarget().isEmpty())
                                                {
                                                    for (MonthWiseTargetData monthWiseTargetData : skuData.getMonthWiseSkuTarget()) {
                                                        if(monthWiseAnnualTargetModel.getProductCode().equals(skuData.getProductCode())) {
                                                            monthWiseAnnualTargetModel.setProductCode(skuData.getProductCode());
                                                            ProductModel product = productService.getProductForCode(skuData.getProductCode());
                                                            if(!Objects.isNull(product)) {
                                                                monthWiseAnnualTargetModel.setProductGrade(product.getGrade());
                                                                monthWiseAnnualTargetModel.setProductPackaging(product.getPackagingCondition());
                                                                monthWiseAnnualTargetModel.setProductBagType(product.getBagType());
                                                            }
                                                            monthWiseAnnualTargetModel.setMonthTarget(monthWiseTargetData.getMonthTarget());
                                                            monthWiseAnnualTargetModel.setMonthYear(monthWiseTargetData.getMonthYear());
                                                            modelService.save(monthWiseAnnualTargetModel);
                                                        }
                                                    }
                                                }
                                            }
                                            productModel.setMonthWiseAnnualTarget(skuMonthWiseList);
                                            modelService.save(productModel);
                                        }
                                    }
                                }
                              }
                            }
                        }
                        dealerRevisedAnnualSale.setListOfSkus(skuList);
                        modelService.save(dealerRevisedAnnualSale);
                        dealerReviseList.add(dealerRevisedAnnualSale);
                    }
                    annualSales.setDealerRevisedAnnualSales(dealerReviseList);
                    modelService.save(annualSales);
                }
            }
        }
        return true;
    }

    @Override
    public boolean saveAnnualSalesTargetSettingForRetailers(AnnualSalesTargetSettingData annualSalesTargetSettingData, String subArea)
    {
        SclUserModel sclUser=(SclUserModel) getUserService().getCurrentUser();
        BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
        AnnualSalesModel annualSales = salesPlanningDao.viewPlannedSalesforDealersRetailersMonthwise(subArea, sclUser, brand);
        List<RetailerPlannedAnnualSalesModel> retailerPlanList = new ArrayList<>();
        if(annualSales == null) {
            AnnualSalesModel annualSalesModel = modelService.create(AnnualSalesModel.class);
            annualSalesModel.setSalesOfficer(sclUser);
            annualSalesModel.setSubarea(subArea);
            if (annualSalesModel.getRetailerPlannedAnnualSales() == null || annualSalesModel.getRetailerPlannedAnnualSales().isEmpty()) {
                RetailerPlannedAnnualSalesModel retailerPlan = modelService.create(RetailerPlannedAnnualSalesModel.class);
                retailerPlan.setCustomerCode(annualSalesTargetSettingData.getCustomerCode());
                retailerPlan.setCustomerName(annualSalesTargetSettingData.getCustomerName());
                retailerPlan.setCustomerPotential(annualSalesTargetSettingData.getCustomerPotential());
                retailerPlan.setCurrentYearSales(annualSalesTargetSettingData.getCurrentYearSales());
                retailerPlan.setPlannedYearSales(annualSalesTargetSettingData.getPlanSales());
                retailerPlan.setStatus(annualSalesTargetSettingData.getStatus());//draft
                retailerPlan.setFinancialYear(findNextFinancialYear());
                if (annualSalesTargetSettingData.getRetailerData() != null && !annualSalesTargetSettingData.getRetailerData().isEmpty()) {
                    List<RetailerPlannedAnnualSalesDetailsModel> retailerDetailsList = new ArrayList<>();
                    for (RetailerDetailsData retailerDetailsData : annualSalesTargetSettingData.getRetailerData()) {
                        RetailerPlannedAnnualSalesDetailsModel retailer = modelService.create(RetailerPlannedAnnualSalesDetails.class);
                        retailer.setCustomerCode(retailerDetailsData.getCustomerCode());
                        retailer.setCustomerName(retailerDetailsData.getCustomerName());
                        retailer.setCustomerPotential(retailerDetailsData.getCustomerPotential());
                        retailer.setCurrentYearSales(retailerDetailsData.getCySales());
                        retailer.setPlannedYearSales(retailerDetailsData.getPlanSales());
                        modelService.save(retailer);
                    }
                    retailerPlan.setListOfRetailersPlanned(retailerDetailsList);
                    retailerPlanList.add(retailerPlan);
                }
                modelService.save(retailerPlan);
                annualSalesModel.setRetailerPlannedAnnualSales(retailerPlanList);
                modelService.save(annualSalesModel);
            }
        }
        else {
            List<RetailerPlannedAnnualSalesModel> listOfRetailerPlanSales=new ArrayList<>(annualSales.getRetailerPlannedAnnualSales());
            if (listOfRetailerPlanSales != null && !listOfRetailerPlanSales.isEmpty()) {
                for (RetailerPlannedAnnualSalesModel retailerPlannedAnnualSale : listOfRetailerPlanSales) {
                    if (retailerPlannedAnnualSale.getCustomerCode().equals(annualSalesTargetSettingData.getCustomerCode())) {
                        retailerPlannedAnnualSale.setCustomerCode(annualSalesTargetSettingData.getCustomerCode());
                        retailerPlannedAnnualSale.setCustomerName(annualSalesTargetSettingData.getCustomerName());
                        retailerPlannedAnnualSale.setCustomerPotential(annualSalesTargetSettingData.getCustomerPotential());
                        retailerPlannedAnnualSale.setCurrentYearSales(annualSalesTargetSettingData.getCurrentYearSales());
                        retailerPlannedAnnualSale.setPlannedYearSales(annualSalesTargetSettingData.getPlanSales());
                        retailerPlannedAnnualSale.setStatus(annualSalesTargetSettingData.getStatus());//draft
                        retailerPlannedAnnualSale.setFinancialYear(findNextFinancialYear());
                        if (annualSalesTargetSettingData.getRetailerData() != null && !annualSalesTargetSettingData.getRetailerData().isEmpty()) {
                            List<RetailerPlannedAnnualSalesDetailsModel> retailerDetailsList = new ArrayList<>(retailerPlannedAnnualSale.getListOfRetailersPlanned());
                            for (RetailerDetailsData retailerDetailsData : annualSalesTargetSettingData.getRetailerData()) {
                                if(retailerDetailsList!=null && !retailerDetailsList.isEmpty()) {
                                    for (RetailerPlannedAnnualSalesDetailsModel retailer : retailerDetailsList) {
                                       if(retailer.getCustomerCode().equals(retailerDetailsData.getCustomerCode()))
                                       {
                                           retailer.setCustomerCode(retailerDetailsData.getCustomerCode());
                                           retailer.setCustomerName(retailerDetailsData.getCustomerName());
                                           retailer.setCustomerPotential(retailerDetailsData.getCustomerPotential());
                                           retailer.setCurrentYearSales(retailerDetailsData.getCySales());
                                           retailer.setPlannedYearSales(retailerDetailsData.getPlanSales());
                                           modelService.save(retailer);
                                       }
                                    }
                                }
                            }
                            retailerPlannedAnnualSale.setListOfRetailersPlanned(retailerDetailsList);
                            retailerPlanList.add(retailerPlannedAnnualSale);

                        }
                    }
                    modelService.save(retailerPlannedAnnualSale);
                    annualSales.setRetailerPlannedAnnualSales(retailerPlanList);
                    modelService.save(annualSales);
                }
            }
        }
        return true;
    }

    @Override
    public List<DealerRevisedAnnualSalesModel> fetchRecordForDealerRevisedAnnualSales(String subArea, SclUserModel sclUser, String financialYear) {
        return salesPlanningDao.fetchRecordForDealerRevisedAnnualSales(subArea,sclUser,financialYear);
    }

    @Override
    public boolean saveMonthWiseRetailerForAnnSales(AnnualSalesMonthWiseTargetData data, String subArea) {
        SclUserModel sclUser=(SclUserModel) getUserService().getCurrentUser();
        BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
        AnnualSalesModel annualSales = salesPlanningDao.viewPlannedSalesforDealersRetailersMonthwise(subArea, sclUser, brand);
        if(annualSales!=null )
        {
            List<RetailerRevisedAnnualSalesModel> retailerReviseList=new ArrayList<>(annualSales.getRetailerRevisedAnnualSales());
            if(retailerReviseList!=null && !retailerReviseList.isEmpty())
            {
                for (RetailerRevisedAnnualSalesModel retailerRevisedAnnualSale : retailerReviseList) {
                    if(retailerRevisedAnnualSale.getCustomerCode().equals(data.getCustomerCode()))
                    {
                        retailerRevisedAnnualSale.setCustomerCode(data.getCustomerCode());
                        retailerRevisedAnnualSale.setCustomerName(data.getCustomerName());
                        retailerRevisedAnnualSale.setCustomerPotential(data.getCustomerPotential()!=null?data.getCustomerPotential():0.0);
                        retailerRevisedAnnualSale.setTotalTarget(data.getTotalTarget()!=null?data.getTotalTarget():0.0);

                        List<MonthWiseAnnualTargetModel> monthWiseDealerSaleList=new ArrayList<>(retailerRevisedAnnualSale.getMonthWiseAnnualTarget());
                        if(monthWiseDealerSaleList!=null && !monthWiseDealerSaleList.isEmpty())
                        {
                            for (MonthWiseAnnualTargetModel monthWiseDealerSale : monthWiseDealerSaleList) {
                                if(data.getMonthWiseTarget()!=null && !data.getMonthWiseTarget().isEmpty())
                                {
                                    for (MonthWiseTargetData monthWiseTargetData : data.getMonthWiseTarget()) {
                                        monthWiseDealerSale.setCustomerCode(data.getCustomerCode());
                                        monthWiseDealerSale.setMonthYear(monthWiseTargetData.getMonthYear());
                                        monthWiseDealerSale.setMonthTarget(monthWiseTargetData.getMonthTarget());
                                        modelService.save(monthWiseDealerSale);
                                    }
                                }
                            }
                        }
                        retailerRevisedAnnualSale.setMonthWiseAnnualTarget(monthWiseDealerSaleList);

                        List<RetailerRevisedAnnualSalesDetailsModel> retailerDetailsList=new ArrayList<>(retailerRevisedAnnualSale.getListOfRetailersRevised());
                        if(retailerDetailsList!=null && !retailerDetailsList.isEmpty())
                        {
                            for (RetailerRevisedAnnualSalesDetailsModel retailer : retailerDetailsList) {
                                if(data.getRetailerData()!=null && !data.getRetailerData().isEmpty())
                                {
                                    for (RetailerDetailsData retailerData : data.getRetailerData()) {
                                        if(retailer.getCustomerCode().equals(retailerData.getCustomerCode()))
                                        {
                                            retailer.setCustomerCode(retailerData.getCustomerCode());
                                            retailer.setCustomerName(retailerData.getCustomerName());
                                            retailer.setCustomerPotential(retailerData.getCustomerPotential()!=null?retailerData.getCustomerPotential():0.0);
                                            retailer.setTotalTarget(retailerData.getCySales());

                                            List<MonthWiseAnnualTargetModel> retailerMonthWiseList=new ArrayList<>(retailer.getMonthWiseAnnualTarget());
                                            if(retailerMonthWiseList!=null && !retailerMonthWiseList.isEmpty()) {
                                                for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : retailerMonthWiseList) {
                                                    if(retailerData.getMonthWiseSkuTarget()!=null && !retailerData.getMonthWiseSkuTarget().isEmpty())
                                                    {
                                                        for (MonthWiseTargetData monthWiseTargetData : retailerData.getMonthWiseSkuTarget()) {
                                                            if(monthWiseAnnualTargetModel.getCustomerCode().equals(retailerData.getCustomerCode())) {
                                                                monthWiseAnnualTargetModel.setCustomerCode(retailerData.getCustomerCode());
                                                                monthWiseAnnualTargetModel.setMonthTarget(monthWiseTargetData.getMonthTarget());
                                                                monthWiseAnnualTargetModel.setMonthYear(monthWiseTargetData.getMonthYear());
                                                                modelService.save(monthWiseAnnualTargetModel);
                                                            }
                                                        }
                                                    }
                                                }
                                                retailer.setMonthWiseAnnualTarget(retailerMonthWiseList);
                                                modelService.save(retailer);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        retailerRevisedAnnualSale.setRetailerRevisedAnnualSalesDetails(retailerDetailsList);
                        modelService.save(retailerRevisedAnnualSale);
                        retailerReviseList.add(retailerRevisedAnnualSale);
                    }
                    annualSales.setRetailerRevisedAnnualSales(retailerReviseList);
                    modelService.save(annualSales);
                }
            }
        }
        return true;
    }

    @Override
    public boolean saveOnboardedDealersForAnnSales(AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData, String subArea) {
        SclUserModel sclUser=(SclUserModel) getUserService().getCurrentUser();
        BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
        AnnualSalesModel annualSales = salesPlanningDao.viewPlannedSalesforDealersRetailersMonthwise(subArea, sclUser, brand);
        return false;
    }

    @Override
    public List<MonthWiseAnnualTargetModel> getMonthWiseAnnualTargetDetails(String customerCode, String productCode, String subarea) {
        return salesPlanningDao.getMonthWiseAnnualTargetDetails(customerCode,productCode,subarea);
    }

    @Override
    public boolean saveOnboardedRetailerForAnnSales(AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData) {
        return true;
    }

    @Override
    public boolean saveMonthlySalesTargetForDealers(MonthlySalesTargetSettingListData monthlySalesTargetSettingListData) {
        SclUserModel sclUser=(SclUserModel) getUserService().getCurrentUser();
        MonthlySalesModel monthlySalesModel = validateMonthlySalesModel(sclUser,monthlySalesTargetSettingListData.getSubArea());
        if(monthlySalesModel!=null){
            if (monthlySalesModel.getSubAreaList() != null && !monthlySalesModel.getSubAreaList().isEmpty()) {
                List<SubAreaListingModel> subAreaListModelsForAnnual = new ArrayList<>(monthlySalesModel.getSubAreaList());
                if (monthlySalesModel.getSubAreaList().contains(monthlySalesTargetSettingListData.getSubArea())) {
                    for (SubAreaListingModel subAreaListModel : monthlySalesModel.getSubAreaList()) {
                        subAreaListModel.setSubAreaName(monthlySalesTargetSettingListData.getSubArea());
                        subAreaListModelsForAnnual.add(subAreaListModel);
                        subAreaListModel.setMonthlySales(monthlySalesModel);
                        monthlySalesModel.setSubAreaList(subAreaListModelsForAnnual);
                        modelService.save(subAreaListModel);
                    }
                }
            }
        } else {
            List<SubAreaListingModel> subAreaListModels = new ArrayList<>();
            SubAreaListingModel subAreaListModel = modelService.create(SubAreaListingModel.class);
            subAreaListModel.setSubAreaName(monthlySalesTargetSettingListData.getSubArea());
            subAreaListModel.setMonthlySales(monthlySalesModel);
            subAreaListModels.add(subAreaListModel);
            monthlySalesModel.setSubAreaList(subAreaListModels);
            modelService.save(subAreaListModel);
        }

        return true;
    }

    @Override
    public boolean saveReviseMonthlySalesTargetForDealer(MonthlySalesTargetSettingListData monthlySalesTargetSettingListData) {
        SclUserModel sclUser=(SclUserModel) getUserService().getCurrentUser();
        return true;
    }

    @Override
    public Double getCurrentYearSalesForAnnualSummary(SclUserModel sclUser, BaseSiteModel baseSite, String subarea) {
        Date startDate=null,endDate=null;
        List<Date> dates = getCurrentFY();
        startDate=dates.get(0);
        endDate=dates.get(1);
        LOG.info("startDate ::" + startDate + "endDate ::" +endDate);
        LOG.info("getCurrentYearSalesForAnnualSummary service" + "scluser pk :" + sclUser + "baseSite pk :" + baseSite + "subArea pk :" + subarea);
        //uncomment once dev completed
        //return sclSalesSummaryService.getCurrentFySales(sclUser,Collections.EMPTY_LIST); //pass territory list
        return salesPlanningDao.getCurrentYearSalesForAnnualSummary(sclUser,baseSite,subarea,startDate,endDate);
    }

    @Override
    public Double getCurrentYearSalesForAnnualSummaryNew(BaseSiteModel baseSite, String subarea) {
        Date startDate=null,endDate=null;
        List<Date> dates = getCurrentFY();
        startDate=dates.get(0);
        endDate=dates.get(1);
        LOG.info("startDate ::" + startDate + "endDate ::" +endDate);
        LOG.info("getCurrentYearSalesForAnnualSummary service"  + "baseSite pk :" + baseSite + "subArea pk :" + subarea);
        LOG.info("current year Sales form service:"+salesPlanningDao.getCurrentYearSalesForAnnualSummaryNew(baseSite,subarea,startDate,endDate));
        //uncomment once dev completed
        //return sclSalesSummaryService.getCurrentFySales(sclUser,Collections.EMPTY_LIST); //pass territory list
        return salesPlanningDao.getCurrentYearSalesForAnnualSummaryNew(baseSite,subarea,startDate,endDate);
    }

    @Override
    public Double getCurrentYearSalesForAnnualSummaryForRH(DistrictMasterModel districtMasterModel, BaseSiteModel baseSite) {
        Date startDate=null,endDate=null;
        List<Date> dates = getCurrentFY();
        startDate=dates.get(0);
        endDate=dates.get(1);
        LOG.info("startDate ::" + startDate + "endDate ::" +endDate);
      //  LOG.info("getCurrentYearSalesForAnnualSummary service" + "scluser pk :" + sclUser + "baseSite pk :" + baseSite + "subArea pk :" + subarea);
        //uncomment once dev completed
        //return sclSalesSummaryService.getCurrentFySales(sclUser,Collections.EMPTY_LIST); //pass territory list
        return salesPlanningDao.getCurrentYearSalesForAnnualSummaryForRH(districtMasterModel,baseSite,startDate,endDate);
    }

    @Override
    public boolean isDealerCustomerCodeExisting(String customerCode, String subArea, SclUserModel sclUser, String nextFinancialYear) {
        DealerPlannedAnnualSalesModel dealerPlannedAnnualSalesModel = salesPlanningDao.findDealerDetailsByCustomerCode(customerCode,subArea,sclUser,nextFinancialYear);
        if(null!= dealerPlannedAnnualSalesModel){
            return Boolean.TRUE;
        }
        else{
            return Boolean.FALSE;
        }
    }

    @Override
    public List<List<Object>> getLastYearShareForProductFromNCR(String subArea) {
        String baseSite=baseSiteService.getCurrentBaseSite().getUid();
        String catalogId= baseSite.concat("ProductCatalog");
        //String prodStatus="approved";
        String version="Online";
        Date startDate=null,endDate=null;
       // List<Date> dates=findFiscalYearforLastYearShare();
        List<Date> dates=getCurrentFY();
        startDate= dates.get(0);
        endDate=dates.get(1);

        return salesPlanningDao.getLastYearShareForProductFromNCR(subArea, catalogId, version,startDate,endDate, CustomerCategory.TR);
    }

    @Override
    public List<List<Object>> getLastYearShareForDealerFromNCRAnnual(String subArea) {
        Date startDate=null,endDate=null;
      //  List<Date> dates=findFiscalYearforLastYearShare();
        List<Date> dates=getCurrentFY();
        startDate= dates.get(0);
        endDate=dates.get(1);

        return salesPlanningDao.getLastYearShareForDealerFromNCRAnnual(subArea,startDate,endDate);
    }

    @Override
    public List<List<Object>> getLastYearShareForProductFromNCRMonthly(String subArea) {
        int month=0; int year=0;
        String baseSite=baseSiteService.getCurrentBaseSite().getUid();
        String catalogId= baseSite.concat("ProductCatalog");
        String version="Online";

        List<Integer> list=getLastYearSameMonth();
        year=list.get(0);
        month=list.get(1);
        return salesPlanningDao.getLastYearShareForProductFromNCRMonthly(subArea, catalogId, version,year,month, CustomerCategory.TR);
    }

    @Override
    public List<List<Object>> getLastYearShareForDealerFromNCRMonthly(String subArea) {
        int month=0; int year=0;
        List<Integer> list=getLastYearSameMonth();
        year=list.get(0);
        month=list.get(1);
        return salesPlanningDao.getLastYearShareForDealerFromNCRMonthly(subArea,year,month);
    }

    @Override
    public List<String> getStateWiseProductForSummaryPage(String state) {
        String baseSite=baseSiteService.getCurrentBaseSite().getUid();
        String catalogId= baseSite.concat("ProductCatalog");
        String prodStatus="approved";
        String version="Online";
        return salesPlanningDao.getStateWiseProductForSummaryPage(state,catalogId,version,prodStatus);
    }

    @Override
    public List<String> getDealerCategoryForSummaryPage() {
        return salesPlanningDao.getDealerCategoryForSummaryPage();
    }

    @Override
    public List<List<Object>> viewDealerDetailsForRetailerAnnualSales(String subArea, SclUserModel sclUser, BaseSiteModel baseSite) {
        List<Date> dates= getCurrentFY();
        Date startDate = dates.get(0);
        Date endDate = dates.get(1);
        return salesPlanningDao.viewDealerDetailsForRetailerAnnualSales(subArea,sclUser,baseSite,startDate, endDate);
    }

    @Override
    public Double fetchDealerCySalesForRetailerAnnualSales(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, String customerCode) {
        List<Date> dates= getCurrentFY();
        Date startDate = dates.get(0);
        Date endDate = dates.get(1);
        return salesPlanningDao.fetchDealerCySalesForRetailerAnnualSales(subArea,sclUser,baseSite,customerCode,startDate,endDate);
    }

    @Override
    public Double getPlannedMonthSaleForMonthlySaleSummary(SclUserModel sclUser, BaseSiteModel baseSite, String subarea) {
        int month=0; int year=0;
        List<Integer> list=getLastYearSameMonth();
        year=list.get(0);
        month=list.get(1);
        return salesPlanningDao.getPlannedMonthSaleForMonthlySaleSummary(sclUser,baseSite,subarea,month,year);
    }

    private boolean isDealerCustomerCodeExistingForRetailerTargetSetting(String customerCode, String subArea, SclUserModel sclUser, String nextFinancialYear) {
        RetailerPlannedAnnualSalesModel retailerPlannedAnnualSalesModel = salesPlanningDao.findDealerDetailsForRetailerTargetSet(customerCode,subArea,sclUser,nextFinancialYear);
        if(null!= retailerPlannedAnnualSalesModel){
            return Boolean.TRUE;
        }
        else{
            return Boolean.FALSE;
        }
    }

    @Override
    public List<MonthWiseAnnualTargetModel> getMonthWiseSkuDetailsForReview(String customerCode, String productCode, String subarea, SclUserModel sclUser) {
        return salesPlanningDao.getMonthWiseSkuDetailsForReview(customerCode,productCode,subarea,sclUser);
    }

    @Override
    public List<MonthWiseAnnualTargetModel> getMonthWiseRetailerDetailsForReview(String dealerCode, String retailerCode, String subarea, SclUserModel sclUser) {
        return salesPlanningDao.getMonthWiseRetailerDetailsForReview(dealerCode,retailerCode,subarea,sclUser);
    }
    @Override
    public List<MonthWiseAnnualTargetModel> getMonthWiseSkuDetailsBeforeReview(String customerCode, String productCode, String subarea, SclUserModel sclUser) {
        return salesPlanningDao.getMonthWiseSkuDetailsBeforeReview(customerCode,productCode,subarea,sclUser);
    }
    @Override
    public List<MonthWiseAnnualTargetModel> getMonthWiseRetailerDetailsBeforeReview(String dealerCustomerCode, String retailerCustomerCode, String subarea, SclUserModel sclUser) {
        return salesPlanningDao.getMonthWiseRetailerDetailsBeforeReview(dealerCustomerCode,retailerCustomerCode,subarea,sclUser);
    }

    /*private List<Date> getCurrentFY() {
        List<Date> dates=new ArrayList<>();
        Date startDate,endDate;
        Year starYear,endYear;
        LocalDate currentDate = LocalDate.now();
        Month currentMonth = currentDate.getMonth();
        Year currentYear = Year.of(currentDate.getYear());
        Calendar c1 = Calendar.getInstance();
        if(currentMonth.getValue() <= 3  ){
            starYear= Year.of(currentYear.getValue()-1);
            c1.set(starYear.getValue(),02,01);
            startDate = c1.getTime();
            endYear= Year.of(currentYear.getValue());
            //c1.set(endYear.getValue(),01,28);
            c1.set(endYear.getValue(),02,01);
            endDate = c1.getTime();
            dates.add(startDate);
            dates.add(endDate);
        }
        else {
            //starYear=currentYear;
            starYear= Year.of(currentYear.getValue()-1);
            c1.set(starYear.getValue(),02,01);
            startDate = c1.getTime();
            //endYear= Year.of(currentYear.getValue()+1);
            endYear= Year.of(currentYear.getValue());
           // c1.set(endYear.getValue(),01,28);
            c1.set(endYear.getValue(),02,01);
            endDate = c1.getTime();
            dates.add(startDate);
            dates.add(endDate);
        }
        return dates;
    }*/

    private List<Date> getCurrentFY() {
        List<Date> dates=new ArrayList<>();
        Date startDate1 = null,endDate1 = null;

        LocalDate currentDate = LocalDate.now();
        LocalDate startDate, endDate;
        startDate= LocalDate.of(currentDate.getYear()-1, Month.MARCH, 1);
        endDate = LocalDate.of(currentDate.getYear(),Month.MARCH, 1);
        startDate1 = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        endDate1 = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        dates.add(startDate1);
        dates.add(endDate1);
        return dates;
    }

    public static Date getFirstDateOfMonth(int month){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH,
                calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }

    public static Date getLastDateOfMonth(int month){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }

    public static String getYear(Date date){
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        return yearFormat.format(date);
    }
    public static String getMonth(Date date){
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        String year=yearFormat.format(date);
        String month=monthFormat.format(date);
        String formattedMonth=month.concat("-").concat(year);
        return formattedMonth;
    }

    public static String getYearPlus(Date date){
        LocalDate localDate=LocalDate.now();
        int nextYear=localDate.plusYears(1).getYear();
        return String.valueOf(nextYear);
    }

    public static String getMonthPlus(Date date){
        LocalDate localDate=LocalDate.now();
        LocalDate nextMonth=localDate.plusMonths(1);
        //int nextYear=localDate.plusYears(1).getYear();
        int currentYear=localDate.getYear();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = nextMonth.atStartOfDay(zone);
        String month = monthFormat.format(Date.from(dateTime.toInstant()));
        String formattedMonth=month.concat("-").concat(String.valueOf(currentYear));
        return formattedMonth;
    }

    String findCurrentFinancialYear()
    {
        LocalDate date = LocalDate.now();
        int currentYear=date.getYear();
        int fyYear=currentYear-1;
        StringBuilder f=new StringBuilder();
        return String.valueOf(f.append(String.valueOf(fyYear)).append("-").append(String.valueOf(currentYear)));
    }

    List<Integer> getLastYearSameMonth()
    {
        List<Integer> list = new ArrayList<>();
        LocalDate date=LocalDate.now();
        LocalDate nextMonth=date.plusMonths(1);
        LocalDate lastYearSameMonth = nextMonth.minusYears(1);
        int year=lastYearSameMonth.getYear();
        int month=lastYearSameMonth.getMonthValue();
        list.add(year);
        list.add(month);
        return list;
    }

    @Override
    public List<List<Object>> fetchDealerSaleDetailsForSummaryAfterTargetSetting(String subArea, SclUserModel sclUser) {
        return salesPlanningDao.fetchDealerSaleDetailsForSummaryAfterTargetSetting(subArea,sclUser);
    }

    @Override
    public List<List<Object>> fetchDealerSaleDetailsForSummaryAfterTargetSettingForRH(DistrictMasterModel districtMasterModel) {
        return salesPlanningDao.fetchDealerSaleDetailsForSummaryAfterTargetSettingForRH(districtMasterModel);
    }

    @Override
    public List<List<Object>> fetchDealerSaleDetailsForSummaryAfterTargetSettingForTSMRH(String subArea, SclUserModel sclUser, String distictCode, String regionCode) {
        return salesPlanningDao.fetchDealerSaleDetailsForSummaryAfterTargetSettingForTSMRH(subArea,sclUser,distictCode,regionCode);
    }

    @Override
    public List<List<Object>> fetchDealerSaleDetailsForSummaryForSummaryAfterReview(String subArea, SclUserModel sclUser, BaseSiteModel baseSite) {
        return salesPlanningDao.fetchDealerSaleDetailsForSummaryForSummaryAfterReview(subArea,sclUser,baseSite);
    }

    @Override
    public List<List<Object>> fetchDealerSaleDetailsForSummaryForSummaryAfterReviewForRH(DistrictMasterModel districtMasterModel) {
        return salesPlanningDao.fetchDealerSaleDetailsForSummaryForSummaryAfterReviewForRH(districtMasterModel);
    }

    @Override
    public List<List<Object>> fetchDealerSaleDetailsForSummaryForSummaryAfterReviewForTSMRH(String subArea, SclUserModel sclUser, String districtCode, String regionCode) {
        return salesPlanningDao.fetchDealerSaleDetailsForSummaryForSummaryAfterReviewForTSMRH(subArea,sclUser,districtCode,regionCode);
    }

    @Override
    public MonthlySalesModel getMonthlySalesModelDetail(SclUserModel sclUser, String subArea,BaseSiteModel brand) {
        //MonthlySalesModel monthlySalesModelDetail = salesPlanningDao.getMonthlySalesModelDetail(sclUser, getMonthPlus(new Date()), getYear(new Date()), subArea,brand);
        MonthlySalesModel monthlySalesModelDetail = salesPlanningDao.getMonthlySalesModelDetail1(sclUser, getMonthPlus(new Date()), getYear(new Date()), subArea,brand);

        return monthlySalesModelDetail;
    }

    @Override
    public MonthlySalesModel getMonthlySalesModelDetailForDO(List<SubAreaMasterModel> subAreaList, BaseSiteModel brand) {
        MonthlySalesModel monthlySalesModelDetail = salesPlanningDao.getMonthlySalesModelDetailForDO( getMonthPlus(new Date()), getYear(new Date()), subAreaList,brand);
        return monthlySalesModelDetail;
    }

    @Override
    public List<List<Object>> fetchDealerMixDetailsAfterTargetSetMonthlySummary(String subArea, SclUserModel sclUser) {
        //need to change once data available for future years for eg: no 2023 data so using 2024 for testing so +1
        LocalDate localDate=LocalDate.now();
        LocalDate nextMonth=localDate.plusMonths(1);
        //int nextYear=localDate.plusYears(1).getYear();
        int currentYear=localDate.getYear();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = nextMonth.atStartOfDay(zone);
        String month = monthFormat.format(Date.from(dateTime.toInstant()));
        String formattedMonth=month.concat("-").concat(String.valueOf(currentYear));
        return salesPlanningDao.fetchDealerMixDetailsAfterTargetSetMonthlySummary(subArea,sclUser,formattedMonth,String.valueOf(currentYear));
    }

    @Override
    public List<List<Object>> fetchDealerMixDetailsAfterTargetSetMonthlySummaryForRH(DistrictMasterModel districtMasterModel) {
        //need to change once data available for future years for eg: no 2023 data so using 2024 for testing so +1
        LocalDate localDate=LocalDate.now();
        LocalDate nextMonth=localDate.plusMonths(1);
        //int nextYear=localDate.plusYears(1).getYear();
        int currentYear=localDate.getYear();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = nextMonth.atStartOfDay(zone);
        String month = monthFormat.format(Date.from(dateTime.toInstant()));
        String formattedMonth=month.concat("-").concat(String.valueOf(currentYear));
        return salesPlanningDao.fetchDealerMixDetailsAfterTargetSetMonthlySummaryForRH(districtMasterModel,formattedMonth,String.valueOf(currentYear));
    }

    @Override
    public List<List<Object>> fetchDealerMixDetailsAfterReviewMonthlySummary(String subArea, SclUserModel sclUser) {
        //need to change once data available for future years for eg: no 2023 data so using 2024 for testing so +1
        LocalDate localDate=LocalDate.now();
        LocalDate nextMonth=localDate.plusMonths(1);
        //int nextYear=localDate.plusYears(1).getYear();
        int currentYear=localDate.getYear();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = nextMonth.atStartOfDay(zone);
        String month = monthFormat.format(Date.from(dateTime.toInstant()));
        String formattedMonth=month.concat("-").concat(String.valueOf(currentYear));
        return salesPlanningDao.fetchDealerMixDetailsAfterReviewMonthlySummary(subArea,sclUser,formattedMonth,String.valueOf(currentYear));
    }

    @Override
    public List<List<Object>> fetchDealerMixDetailsAfterReviewMonthlySummaryForRH(DistrictMasterModel districtMasterModel) {
        //need to change once data available for future years for eg: no 2023 data so using 2024 for testing so +1
        LocalDate localDate=LocalDate.now();
        LocalDate nextMonth=localDate.plusMonths(1);
        //int nextYear=localDate.plusYears(1).getYear();
        int currentYear=localDate.getYear();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = nextMonth.atStartOfDay(zone);
        String month = monthFormat.format(Date.from(dateTime.toInstant()));
        String formattedMonth=month.concat("-").concat(String.valueOf(currentYear));
        return salesPlanningDao.fetchDealerMixDetailsAfterReviewMonthlySummaryForRH(districtMasterModel,formattedMonth,String.valueOf(currentYear));
    }

    @Override
    public ProductSaleModel fetchProductSaleForDealerPlannedMonthlySales(String subArea, SclUserModel sclUser, String productCode, String customerCode, String monthName, String monthYear) {
        return salesPlanningDao.fetchProductSaleForDealerPlannedMonthlySales(subArea, sclUser, productCode, customerCode, monthName,monthYear);
    }

    @Override
    public ProductSaleModel fetchProductSaleForDealerRevisedMonthlySales(String subArea, SclUserModel sclUser, String productCode, String customerCode, String monthName, String monthYear) {
        return salesPlanningDao.fetchProductSaleForDealerRevisedMonthlySales(subArea, sclUser, productCode, customerCode, monthName,monthYear);
    }

    @Override
    public Double getMonthWiseAnnualTargetForDealer(SclUserModel sclUser, String dealerCode, String monthYear, List<SubAreaMasterModel> subAreas) {
        return salesPlanningDao.getMonthWiseAnnualTargetForDealer(sclUser,dealerCode,monthYear,subAreas);
    }

    @Override
    public SearchPageData<AnnualSalesModel> viewPlannedSalesforDealersRetailersMonthWise(SearchPageData searchPageData, String subArea, SclUserModel sclUser, BaseSiteModel brand) {
        SearchPageData<AnnualSalesModel> annualSalesModel = getSalesPlanningDao().viewPlannedSalesforDealersRetailersMonthwise(searchPageData,subArea, sclUser, brand);
        return annualSalesModel;
    }

    @Override
    public SearchPageData<RetailerPlannedAnnualSalesModel> fetchRecordForRetailerPlannedAnnualSales(SearchPageData searchPageData, String subArea, SclUserModel sclUser, String filter) {
        return salesPlanningDao.fetchRecordForRetailerPlannedAnnualSales(searchPageData,subArea,sclUser,filter);
    }

    @Override
    public DealerRevisedAnnualSalesModel fetchRecordForDealerRevisedAnnualSalesByCode(String subArea, SclUserModel sclUser, String filter) {
        return salesPlanningDao.fetchRecordForDealerRevisedAnnualSalesByCode(subArea,sclUser,filter);
    }

    @Override
    public List<MonthWiseAnnualTargetModel> getMonthWiseAnnualTargetDetailsForDealerRevised(String customerCode, String productCode, String subArea) {
        return salesPlanningDao.getMonthWiseAnnualTargetDetailsForDealerRevised(customerCode,productCode,subArea);
    }

    @Override
    public double getRetailerCySale(String dealerCode, String retailerCode, String subArea) {
        return salesPlanningDao.getRetailerCySale(dealerCode,retailerCode,subArea);
    }

    @Override
    public List<List<Object>> getRetailerDetailsByDealerCode(String subArea, String dealerCode) {
        Date startDate = null, endDate =null;
        List<Date> date = getCurrentFY();
        startDate = date.get(0);
        endDate = date.get(1);
        return salesPlanningDao.getRetailerDetailsByDealerCode(subArea, dealerCode,startDate, endDate);
    }

    @Override
    public List<String> getRetailerListByDealerCode(String dealerCode, String subArea) {
        Date startDate = null, endDate =null;
        List<Date> date = getCurrentFY();
        startDate = date.get(0);
        endDate = date.get(1);
        return salesPlanningDao.getRetailerListByDealerCode(dealerCode, subArea);
    }

    @Override
    public SalesTargetApprovedData targetSendForRevision(SalesRevisedTargetData salesRevisedTargetData) {
        SalesTargetApprovedData data=new SalesTargetApprovedData();
        SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
        BaseSiteModel baseSite = (BaseSiteModel) baseSiteService.getCurrentBaseSite();

        SubAreaMasterModel subAreaMaster=territoryManagementService.getTerritoryById(salesRevisedTargetData.getSubAreaId());

        AnnualSalesModel annualSalesModel = null;
        SclUserModel sclUserModel=null;
        SclWorkflowModel sclWorkflow =null;
        List<SubAreaMasterModel> subAreaMasterModelList = new ArrayList<>();
        subAreaMasterModelList.add(subAreaMaster);
        List<SclUserModel> usersList = territoryManagementService.getUsersForSubAreas(subAreaMasterModelList);
        if(usersList!=null && !usersList.isEmpty())
        {
            for (SclUserModel sclUser : usersList) {
                if (sclUser.getUserType() != null && sclUser.getUserType().equals(SclUserType.SO)) {
                    sclUserModel = (SclUserModel) getUserService().getUserForUID(sclUser.getUid());
                }
                annualSalesModel = viewPlannedSalesforDealersRetailersMonthWise(salesRevisedTargetData.getSubAreaId(), sclUserModel, baseSite);
                if(annualSalesModel!=null) {
                    sclWorkflow = annualSalesModel.getSclWorkflow();
                }
                //send revision of target by tsm to so
                if(currentUser.getUserType()!=null && currentUser.getUserType().equals(SclUserType.TSM))
                {
                    try {
                        if (annualSalesModel!=null && annualSalesModel.getSubAreaMaster()!=null) {
                            if (annualSalesModel.getSubAreaMaster().equals(subAreaMaster)) {
                               /* if(salesRevisedTargetData.getRevisedTargetBySH()!=null){
                                    annualSalesModel.setRevisedTargetBySH(salesRevisedTargetData.getRevisedTargetBySH());
                                    B2BCustomerModel userForUID = (B2BCustomerModel) getUserService().getUserForUID(salesRevisedTargetData.getRevisedBy());
                                    annualSalesModel.setRevisedByShEmpCode(userForUID.getUid());
                                    annualSalesModel.setCommentsForRevision(salesRevisedTargetData.getComments());
                                }else {*/
                                    annualSalesModel.setRevisedTarget(salesRevisedTargetData.getRevisedTarget());
                                    annualSalesModel.setRevisedBy((B2BCustomerModel) getUserService().getUserForUID(salesRevisedTargetData.getRevisedBy()));
                                    annualSalesModel.setCommentsForRevision(salesRevisedTargetData.getComments());
                                //}
                                SclWorkflowActionModel sclWorkflowActionModel = sclWorkflowService.saveWorkflowAction(sclWorkflow, "sentForRevisionByTSM", baseSite, subAreaMaster, TerritoryLevels.DISTRICT);
                                if (sclWorkflowActionModel != null) {
                                    sclWorkflowService.updateWorkflowAction(sclWorkflowActionModel, currentUser, WorkflowActions.REVISED, salesRevisedTargetData.getComments());
                                }
                                sclWorkflow.setStatus(WorkflowStatus.IN_PROCESS);
                                modelService.save(sclWorkflow);
                                modelService.save(annualSalesModel);
                            }
                        }
                        if (annualSalesModel!=null && annualSalesModel.getRevisedTarget() != null && annualSalesModel.getRevisedBy().equals(currentUser)) {
                            data.setIsTargetRevised(true);
                        } else {
                            data.setIsTargetRevised(false);
                        }
                    }
                    catch (UnknownIdentifierException e)
                    {
                        LOG.error("Error occurred while sending for revision of target "+e.getMessage()+"\n");
                    }
                }
                else if(currentUser.getUserType()!=null && currentUser.getUserType().equals(SclUserType.RH))
                {
                    try {
                        if (annualSalesModel!=null && annualSalesModel.getSubAreaMaster()!=null) {
                            if (annualSalesModel.getSubAreaMaster().equals(subAreaMaster)) {
                                annualSalesModel.setRevisedTarget(salesRevisedTargetData.getRevisedTarget());
                                annualSalesModel.setRevisedBy((B2BCustomerModel) getUserService().getUserForUID(salesRevisedTargetData.getRevisedBy()));
                                annualSalesModel.setCommentsForRevision(salesRevisedTargetData.getComments());
                                SclWorkflowActionModel sclWorkflowActionModel = sclWorkflowService.saveWorkflowAction(sclWorkflow, "sentForRevisionByRH", baseSite, subAreaMaster, TerritoryLevels.REGION);
                                if (sclWorkflowActionModel != null) {
                                    sclWorkflowService.updateWorkflowAction(sclWorkflowActionModel, currentUser, WorkflowActions.REVISED, salesRevisedTargetData.getComments());
                                }
                                sclWorkflow.setStatus(WorkflowStatus.IN_PROCESS);
                                modelService.save(sclWorkflow);
                                modelService.save(annualSalesModel);
                            }
                        }
                        if (annualSalesModel!=null && annualSalesModel.getRevisedTarget() != null && annualSalesModel.getRevisedBy().equals(currentUser)) {
                            data.setIsTargetRevised(true);
                        } else {
                            data.setIsTargetRevised(false);
                        }
                    }
                    catch (UnknownIdentifierException e)
                    {
                        LOG.error("Error occurred while sending for revision of target "+e.getMessage()+"\n");

                    }
                }

            }
        }else {
            data.setIsTargetRevised(false);
        }
        return  data;
    }

    @Override
    public SalesTargetApprovedData updateTargetStatusForApproval(SalesApprovalData salesApprovalData) {
        SalesTargetApprovedData salesTargetApprovedData=new SalesTargetApprovedData();
        SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
        BaseSiteModel baseSite = baseSiteService.getCurrentBaseSite();
        SubAreaMasterModel subAreaMaster=null;
        if(StringUtils.isNotEmpty(salesApprovalData.getSubAreaId())) {
            subAreaMaster  = territoryManagementService.getTerritoryById(salesApprovalData.getSubAreaId());
        }
        AnnualSalesModel annualSalesModel = null;
        SclUserModel sclUserModel=null;
        SclWorkflowModel sclWorkflowModel=null;
        /*List<SubAreaMasterModel> subAreaMasterModelList = new ArrayList<>();
        subAreaMasterModelList.add(subAreaMaster);*/
        //List<SclUserModel> usersList = territoryManagementService.getUsersForSubAreas(subAreaMasterModelList);
        FilterTalukaData filterTalukaData = new FilterTalukaData();
        List<SclUserModel> usersList = territoryManagementService.getSOForUser(filterTalukaData);
        if(usersList!=null && !usersList.isEmpty()) {
            for (SclUserModel sclUser : usersList) {
                if (sclUser.getUserType() != null && sclUser.getUserType().equals(SclUserType.SO)) {
                    sclUserModel = (SclUserModel) getUserService().getUserForUID(sclUser.getUid());
                }

                if(currentUser.getUserType()!=null && currentUser.getUserType().equals(SclUserType.TSM)) {
                    try {
                        if(subAreaMaster!=null) {
                            annualSalesModel = viewPlannedSalesforDealersRetailersMonthWise(subAreaMaster.getPk().toString(), sclUserModel, baseSite);
                        }
                        if (annualSalesModel != null && annualSalesModel.getSubAreaMaster()!=null)
                        {
                            if (annualSalesModel.getSubAreaMaster().equals(subAreaMaster)) {
                                //check up on district
                                annualSalesModel.setActionPerformed(WorkflowActions.APPROVED);
                                annualSalesModel.setActionPerformedBy(currentUser);
                                annualSalesModel.setActionPerformedDate(new Date());

                                if(annualSalesModel.getDealerRevisedAnnualSales()!=null && !annualSalesModel.getDealerRevisedAnnualSales().isEmpty())
                                {
                                    for (DealerRevisedAnnualSalesModel dealerRevisedAnnualSale : annualSalesModel.getDealerRevisedAnnualSales()) {
                                        if(!dealerRevisedAnnualSale.getIsExistingDealerRevisedForReview().equals(true) && !dealerRevisedAnnualSale.getIsNewDealerOnboarded().equals(true))
                                        {
                                            dealerRevisedAnnualSale.setActionPerformed(WorkflowActions.APPROVED);
                                            if(dealerRevisedAnnualSale.getMonthWiseAnnualTarget()!=null && !dealerRevisedAnnualSale.getMonthWiseAnnualTarget().isEmpty())
                                            for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : dealerRevisedAnnualSale.getMonthWiseAnnualTarget()) {
                                                 if(monthWiseAnnualTargetModel.getIsAnnualSalesRevisedForDealer()!=null && monthWiseAnnualTargetModel.getIsAnnualSalesRevisedForDealer().equals(true))
                                                 monthWiseAnnualTargetModel.setActionPerformed(WorkflowActions.APPROVED);
                                                 modelService.save(monthWiseAnnualTargetModel);
                                            }
                                            if(dealerRevisedAnnualSale.getListOfSkus()!=null && !dealerRevisedAnnualSale.getListOfSkus().isEmpty())
                                            {
                                                for (ProductModel sku : dealerRevisedAnnualSale.getListOfSkus()) {
                                                    if(sku.getProductSale()!=null && !sku.getProductSale().isEmpty())
                                                    for (ProductSaleModel productSaleModel : sku.getProductSale()) {
                                                        if(productSaleModel.getIsAnnualSalesRevisedForDealer()!=null && productSaleModel.getIsAnnualSalesRevisedForDealer().equals(true))
                                                            productSaleModel.setActionPerformed(WorkflowActions.APPROVED);
                                                            modelService.save(productSaleModel);
                                                        List<MonthWiseAnnualTargetModel> monthWiseAnnualTargetList = getMonthWiseSkuDetailsBeforeReview(dealerRevisedAnnualSale.getCustomerCode(), productSaleModel.getProductCode(), dealerRevisedAnnualSale.getSubAreaMaster().getPk().toString(), annualSalesModel.getSalesOfficer());
                                                        if(monthWiseAnnualTargetList!=null && !monthWiseAnnualTargetList.isEmpty())
                                                        {
                                                            for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : monthWiseAnnualTargetList) {
                                                                if(monthWiseAnnualTargetModel.getProductCode().equals(productSaleModel.getProductCode()) && monthWiseAnnualTargetModel.getIsAnnualSalesRevisedForDealer().equals(true))
                                                                {
                                                                    monthWiseAnnualTargetModel.setActionPerformed(WorkflowActions.APPROVED);
                                                                    modelService.save(monthWiseAnnualTargetModel);
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            modelService.save(dealerRevisedAnnualSale);
                                        }
                                    }
                                }

                                if(annualSalesModel!=null) {
                                    sclWorkflowModel = annualSalesModel.getSclWorkflow();
                                }
                                SclWorkflowActionModel sclWorkflowActionModel = sclWorkflowService.saveWorkflowAction(sclWorkflowModel, "targetApprovedByTsm", baseSite, subAreaMaster, TerritoryLevels.DISTRICT);
                                if (sclWorkflowActionModel != null) {
                                    sclWorkflowService.updateWorkflowAction(sclWorkflowActionModel, currentUser, WorkflowActions.APPROVED, "Target is approved for subarea " + subAreaMaster.getTaluka());
                                }
                                sclWorkflowModel.setStatus(WorkflowStatus.IN_PROCESS);
                                modelService.save(sclWorkflowModel);
                                modelService.save(annualSalesModel);
                            }
                        }
                        if(annualSalesModel!=null){
                            if(annualSalesModel.getActionPerformedBy()!=null && annualSalesModel.getActionPerformedBy().equals(currentUser) && annualSalesModel.getActionPerformed()!=null && annualSalesModel.getActionPerformed().equals(WorkflowActions.APPROVED)){
                               salesTargetApprovedData.setIsTargetApproved(true);
                            }else{
                                salesTargetApprovedData.setIsTargetApproved(false);
                            }
                        }
                    }
                    catch (UnknownIdentifierException e)
                    {
                        LOG.error("Error occurred while approving target "+e.getMessage()+"\n");
                        String errorMsg = e.getMessage()!=null?e.getMessage():e.getClass().getName() + " Occurred";
                        throw new UnknownIdentifierException(errorMsg);
                    }
                }
                else if(currentUser.getUserType()!=null && currentUser.getUserType().equals(SclUserType.RH))
                {
                    try {
                            if(StringUtils.isNotEmpty(salesApprovalData.getDistrictCode())) {
                                annualSalesModel = getSalesPlanningDao().viewAnnualSalesModelForDistrict(salesApprovalData.getDistrictCode(),sclUser,baseSite);
                                if (annualSalesModel != null && annualSalesModel.getDistrictMaster()!=null) {
                                    if (annualSalesModel.getDistrictMaster().getCode() != null && annualSalesModel.getDistrictMaster().getCode().equals(salesApprovalData.getDistrictCode())) {
                                        annualSalesModel.setActionPerformed(WorkflowActions.APPROVED);
                                        annualSalesModel.setActionPerformedBy(currentUser);
                                        annualSalesModel.setActionPerformedDate(new Date());
                                        /*if(annualSalesModel!=null) {
                                            sclWorkflowModel = annualSalesModel.getSclWorkflow();
                                        }
                                        SclWorkflowActionModel sclWorkflowActionModel = sclWorkflowService.saveWorkflowAction(sclWorkflowModel, "targetApprovedByRH", baseSite, subAreaMaster, TerritoryLevels.REGION);
                                        if (sclWorkflowActionModel != null) {
                                            sclWorkflowService.updateWorkflowAction(sclWorkflowActionModel, currentUser, WorkflowActions.APPROVED, "Target is approved for district " + subAreaMaster.getDistrictMaster());
                                        }
                                        sclWorkflowModel.setStatus(WorkflowStatus.IN_PROCESS);
                                        modelService.save(sclWorkflowModel);*/
                                        modelService.save(annualSalesModel);
                                    }
                                }
                            }
                        if(annualSalesModel!=null){
                            if(annualSalesModel.getActionPerformedBy()!=null && annualSalesModel.getActionPerformedBy().equals(currentUser) && annualSalesModel.getActionPerformed()!=null && annualSalesModel.getActionPerformed().equals(WorkflowActions.APPROVED)){
                                salesTargetApprovedData.setIsTargetApproved(true);
                            }else{
                                salesTargetApprovedData.setIsTargetApproved(false);
                            }
                        }
                    }
                    catch (UnknownIdentifierException e)
                    {
                        LOG.error("Error occurred while approving target "+e.getMessage()+"\n");
                        String errorMsg = e.getMessage()!=null?e.getMessage():e.getClass().getName() + " Occurred";
                        throw new UnknownIdentifierException(errorMsg);
                    }
                }
                else
                {
                    LOG.info("current user is not TSM/RH");
                    salesTargetApprovedData.setIsTargetApproved(false);
                    return salesTargetApprovedData;
                }
            }
            return salesTargetApprovedData;
        }
        else {
            LOG.info("User List is Empty from updateTargetStatusForApproval");
            return salesTargetApprovedData;
        }

        /*FilterTalukaData filterTalukaData=new FilterTalukaData();
        if (salesApprovalData.getDistrictCode() != null) {
            filterTalukaData.setDistrictCode(salesApprovalData.getDistrictCode());
        }
        List<SubAreaMasterModel> subAreas = territoryManagementService.getTaulkaForUser(filterTalukaData)*/
        /*if(currentUser.getUserType().getCode().equals("RH")) {
            if (districtCode != null) {
                filterTalukaData.setDistrictCode(districtCode);
            }
        }*/
       // FilterDistrictData filterDistrictData=new FilterDistrictData();
       // filterDistrictData.setDistrictName(districtCode);
       // List<DistrictMasterModel> districts = territoryManagementService.getDistrictForUser(filterDistrictData); */
    }

    @Override
    public boolean sendApprovedTargetToUser(boolean isTargetSetForUser) {
        if(isTargetSetForUser) {
            SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
            SclWorkflowModel sclWorkflow = null;
            BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
            RegionMasterModel regionMaster = null;
            Collection<RegionMasterModel> currentRegion = territoryManagementService.getCurrentRegion();
            if (currentRegion != null && !currentRegion.isEmpty()) {
                for (RegionMasterModel regionMasterModel : currentRegion) {
                    regionMaster = regionMasterModel;
                }
            }
            DistrictMasterModel districtMaster = null;
            Collection<DistrictMasterModel> currentDistrict = territoryManagementService.getCurrentDistrict();
            if (currentDistrict != null && !currentDistrict.isEmpty()) {
                for (DistrictMasterModel districtMasterModel : currentDistrict) {
                    districtMaster = districtMasterModel;
                }
            }
            if (currentUser.getUserType().getCode().equalsIgnoreCase("TSM")) {
                AnnualSalesModel annualSalesModelDetailsForTSM = salesPlanningDao.getAnnualSalesModelDetailsForTSM(findNextFinancialYear(), districtMaster, currentBaseSite);
                if (annualSalesModelDetailsForTSM != null) {
                    if(annualSalesModelDetailsForTSM.getSclWorkflow()!=null) {
                        sclWorkflow = annualSalesModelDetailsForTSM.getSclWorkflow();
                    }
                    if (districtMaster != null) {
                        if (districtMaster.getSubAreas() != null) {
                            for (SubAreaMasterModel subArea : districtMaster.getSubAreas()) {
                                SclWorkflowActionModel sclWorkflowActionModel = sclWorkflowService.saveWorkflowAction(sclWorkflow, "sentTargetToRH", currentBaseSite, subArea, TerritoryLevels.DISTRICT);
                                if (sclWorkflowActionModel != null) {
                                    sclWorkflowService.updateWorkflowAction(sclWorkflowActionModel, currentUser, WorkflowActions.SENT, "Target Sent to RH");
                                }
                            }
                        }
                    }
                }
            } else if (currentUser.getUserType().getCode().equalsIgnoreCase("RH")) {
                AnnualSalesModel annualSalesModelDetailsForRH = salesPlanningDao.getAnnualSalesModelDetailsForRH(findNextFinancialYear(), regionMaster, currentBaseSite);
                if (annualSalesModelDetailsForRH != null) {
                    if(annualSalesModelDetailsForRH.getSclWorkflow()!=null) {
                        sclWorkflow = annualSalesModelDetailsForRH.getSclWorkflow();
                    }
                    if (regionMaster != null) {
                        if (regionMaster.getDistricts() != null && !regionMaster.getDistricts().isEmpty()) {
                            Collection<DistrictMasterModel> districts = regionMaster.getDistricts();
                            for (DistrictMasterModel district : districts) {
                                for (SubAreaMasterModel subArea : district.getSubAreas()) {
                                    SclWorkflowActionModel sclWorkflowActionModel = sclWorkflowService.saveWorkflowAction(sclWorkflow, "sentTargetToSH", currentBaseSite, subArea, TerritoryLevels.REGION);
                                    if (sclWorkflowActionModel != null) {
                                        sclWorkflowService.updateWorkflowAction(sclWorkflowActionModel, currentUser, WorkflowActions.SENT, "Comments Added");
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if(sclWorkflow!=null) {
                sclWorkflow.setStatus(WorkflowStatus.IN_PROCESS);
                modelService.save(sclWorkflow);
            }
            return true;
        }else {
            return false;
        }
    }

    @Override
    public AnnualSalesModel getAnnualSalesModelDetailsForTSM(String financialYear, DistrictMasterModel districtMaster, BaseSiteModel brand) {
        return salesPlanningDao.getAnnualSalesModelDetailsForTSM(financialYear,districtMaster,brand);
    }

    @Override
    public AnnualSalesModel getAnnualSalesModelDetailsForRH(String financialYear, RegionMasterModel regionMaster, BaseSiteModel brand) {
        return salesPlanningDao.getAnnualSalesModelDetailsForRH(financialYear,regionMaster,brand);
    }

    @Override
    public AnnualSalesModel getAnnualSalesModelDetailsForSH(String financialYear, List<SubAreaMasterModel> subArea, BaseSiteModel baseSite) {
        return  salesPlanningDao.getAnnualSalesModelDetailsForSH(financialYear,subArea,baseSite);
    }

    @Override
    public AnnualSalesModel getAnnualSalesModelDetailsForSH_RH(String financialYear, List<DistrictMasterModel> district, BaseSiteModel baseSite) {
        return salesPlanningDao.getAnnualSalesModelDetailsForSH_RH(financialYear, district, baseSite);
    }

    @Override
    public boolean updateStatusForBucketApproval(ViewBucketwiseRequest viewBucketwiseRequest) {/*
        if(viewBucketwiseRequest!=null) {
            try {
                SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
                SclWorkflowModel sclWorkflow = null;
                BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
                RegionMasterModel regionMaster = null;
                Collection<RegionMasterModel> currentRegion = territoryManagementService.getCurrentRegion();
                if (currentRegion != null && !currentRegion.isEmpty()) {
                    for (RegionMasterModel regionMasterModel : currentRegion) {
                        regionMaster = regionMasterModel;
                    }
                }
                if (currentUser.getUserType().getCode().equalsIgnoreCase("RH")) {
                    SclUserModel userForUID = null;
                    if (viewBucketwiseRequest.getUserCode() != null)
                        userForUID = (SclUserModel) userService.getUserForUID(viewBucketwiseRequest.getUserCode());
                    MonthlySalesModel monthlySalesModel = viewMonthlySalesTargetForPlannedTab(viewBucketwiseRequest.getSubAreaName(), userForUID);
                    // MonthlySalesModel monthlySalesModel = salesPlanningDao.getMonthlySalesModelDetailsForRH(LocalDate.now().getMonth().name(), String.valueOf(LocalDate.now().getYear()), regionMaster, currentBaseSite);
                    if (monthlySalesModel != null) {
                        if (monthlySalesModel.getSclWorkflow() != null) {
                            sclWorkflow = monthlySalesModel.getSclWorkflow();
                        }
                        if (regionMaster != null) {
                            if (regionMaster.getDistricts() != null && !regionMaster.getDistricts().isEmpty()) {
                                Collection<DistrictMasterModel> districts = regionMaster.getDistricts();
                                for (DistrictMasterModel district : districts) {
                                    for (SubAreaMasterModel subArea : district.getSubAreas()) {
                                        if (subArea.getTaluka().equalsIgnoreCase(viewBucketwiseRequest.getSubAreaName()) &&
                                                monthlySalesModel.getSubAreaMaster().getTaluka().equalsIgnoreCase(viewBucketwiseRequest.getSubAreaName())) {
                                            if (viewBucketwiseRequest.getApproveStatus().equals(Boolean.TRUE)) {
                                                monthlySalesModel.setActionPerformed(WorkflowActions.APPROVED);
                                                SclWorkflowActionModel sclWorkflowActionModel = sclWorkflowService.saveWorkflowAction(sclWorkflow, "Approval for SO Bucketwise from RH", currentBaseSite, subArea, TerritoryLevels.SUBAREA);
                                                if (sclWorkflowActionModel != null) {
                                                    sclWorkflowService.updateWorkflowAction(sclWorkflowActionModel, currentUser, WorkflowActions.APPROVED, "Comments Addedfor Approval");
                                                }
                                               // monthlySalesModel.setIsTargetApprovedByRH(true);
                                            } else if (viewBucketwiseRequest.getApproveStatus().equals(Boolean.FALSE)) {
                                                monthlySalesModel.setActionPerformed(WorkflowActions.REJECTED);
                                                SclWorkflowActionModel sclWorkflowActionModel = sclWorkflowService.saveWorkflowAction(sclWorkflow, "Rejected for SO Bucketwise from RH", currentBaseSite, subArea, TerritoryLevels.SUBAREA);
                                                if (sclWorkflowActionModel != null) {
                                                    sclWorkflowService.updateWorkflowAction(sclWorkflowActionModel, currentUser, WorkflowActions.REJECTED, "Comments Added for Rejetion");
                                                }
                                               // monthlySalesModel.setIsTargetApprovedByRH(false);
                                            }
                                            monthlySalesModel.setActionPerformedBy(currentUser);
                                            monthlySalesModel.setActionPerformedDate(new Date());
                                            monthlySalesModel.setCommentsForRevision(viewBucketwiseRequest.getCommentsForStatus());
                                            modelService.save(monthlySalesModel);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (sclWorkflow != null) {
                    sclWorkflow.setStatus(WorkflowStatus.IN_PROCESS);
                    modelService.save(sclWorkflow);
                }
            }catch(ModelSavingException e){
                LOG.info("Model Saving Error:"+e.getMessage());
            }
            return true;
        }else {
                return false;
            }*/
    return false;
    }

    @Override
    public SalesTargetApprovedData targetSendForRevisionForMonthly(SalesRevisedTargetData salesRevisedTargetData) {
        SalesTargetApprovedData data=new SalesTargetApprovedData();
        SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
        BaseSiteModel baseSite = (BaseSiteModel) baseSiteService.getCurrentBaseSite();

        SubAreaMasterModel subAreaMaster=territoryManagementService.getTerritoryById(salesRevisedTargetData.getSubAreaId());

        MonthlySalesModel monthlySalesModel = null;
        SclUserModel sclUserModel=null;
        SclWorkflowModel sclWorkflow =null;
        List<SubAreaMasterModel> subAreaMasterModelList = new ArrayList<>();
        subAreaMasterModelList.add(subAreaMaster);
        List<SclUserModel> usersList = territoryManagementService.getUsersForSubAreas(subAreaMasterModelList);
        if(usersList!=null && !usersList.isEmpty())
        {
            for (SclUserModel sclUser : usersList) {
                if (sclUser.getUserType() != null && sclUser.getUserType().equals(SclUserType.SO)) {
                    sclUserModel = (SclUserModel) getUserService().getUserForUID(sclUser.getUid());
                }
                monthlySalesModel = viewMonthlySalesTargetForPlannedTab(salesRevisedTargetData.getSubAreaId(), currentUser);
                if(monthlySalesModel!=null) {
                    sclWorkflow = monthlySalesModel.getSclWorkflow();
                }
                //send revision of target by rh to so
                if(currentUser.getUserType()!=null && currentUser.getUserType().equals(SclUserType.RH))
                {
                    try {
                        if(monthlySalesModel!=null) {
                                if (monthlySalesModel.getSubAreaMaster()!=null && monthlySalesModel.getSubAreaMaster().equals(subAreaMaster)) {
                                  /*  if(salesRevisedTargetData.getRevisedTargetBySH()!=null){
                                        monthlySalesModel.setRevisedTargetBySH(salesRevisedTargetData.getRevisedTarget());
                                        B2BCustomerModel userForUID = (B2BCustomerModel) getUserService().getUserForUID(salesRevisedTargetData.getRevisedBy());
                                        monthlySalesModel.setRevisedByShEmpCode(userForUID.getUid());
                                        monthlySalesModel.setCommentsForRevision(salesRevisedTargetData.getComments());
                                    }else {*/
                                        monthlySalesModel.setRevisedTarget(salesRevisedTargetData.getRevisedTarget());
                                        monthlySalesModel.setRevisedBy((B2BCustomerModel) getUserService().getUserForUID(salesRevisedTargetData.getRevisedBy()));
                                        monthlySalesModel.setCommentsForRevision(salesRevisedTargetData.getComments());
                                    //}
                                    SclWorkflowActionModel sclWorkflowActionModel = sclWorkflowService.saveWorkflowAction(sclWorkflow, "sentForRevisionByRH", baseSite, subAreaMaster, TerritoryLevels.REGION);
                                    if (sclWorkflowActionModel != null) {
                                        sclWorkflowService.updateWorkflowAction(sclWorkflowActionModel, currentUser, WorkflowActions.REVISED, salesRevisedTargetData.getComments());
                                    }
                                    sclWorkflow.setStatus(WorkflowStatus.IN_PROCESS);
                                    modelService.save(sclWorkflow);

                                    if(salesRevisedTargetData.getIsTopDownIndicatorOn()!=null && salesRevisedTargetData.getIsTopDownIndicatorOn())
                                    {
                                        monthlySalesModel.setRevisedTargetSendToDO(salesRevisedTargetData.getRevisedTarget());
                                        monthlySalesModel.setRevisedPremiumTargetSentToDO(salesRevisedTargetData.getPremiumRevisedTargetByRH());
                                        monthlySalesModel.setRevisedNonPremiumTargetSentToDO(salesRevisedTargetData.getNonPremiumRevisedTargetByRH());
                                        monthlySalesModel.setCommentsForRevision(salesRevisedTargetData.getComments());
                                        monthlySalesModel.setGapToRevisedPremiumTargetBySH(salesRevisedTargetData.getGapToRevisedPremiumTargetBySH());
                                        monthlySalesModel.setGapToRevisedNonPremiumTargetBySH(salesRevisedTargetData.getGapToRevisedNonPremiumTargetBySH());
                                        sclWorkflowActionModel = sclWorkflowService.saveWorkflowAction(sclWorkflow, "sentForRevisionByRHForTopDown", baseSite, subAreaMaster, TerritoryLevels.REGION);
                                        if (sclWorkflowActionModel != null) {
                                            sclWorkflowService.updateWorkflowAction(sclWorkflowActionModel, currentUser, WorkflowActions.REVISED, salesRevisedTargetData.getComments());
                                        }
                                        sclWorkflow.setStatus(WorkflowStatus.IN_PROCESS);
                                        modelService.save(sclWorkflow);
                                    }
                                    modelService.save(monthlySalesModel);
                            }
                        }
                        if(monthlySalesModel!= null) {
                            if (monthlySalesModel.getRevisedTarget() != null && monthlySalesModel.getRevisedBy().equals(currentUser)) {
                                data.setIsTargetRevised(true);
                            } else {
                                data.setIsTargetRevised(false);
                            }
                        }
                    }
                    catch (UnknownIdentifierException e)
                    {
                        LOG.error("Error occurred while sending for revision of target "+e.getMessage()+"\n");
                    }
                }
            }
        }else {
            data.setIsTargetRevised(false);
        }
        return  data;
    }

    @Override
    public List<MonthlySalesModel> getMonthlySalesModelDetailsListForDO(List<SubAreaMasterModel> taulkaForUser, BaseSiteModel currentBaseSite) {
        return salesPlanningDao.getMonthlySalesModelDetailsListForDO(getMonthPlus(new Date()), getYear(new Date()), taulkaForUser,currentBaseSite);
    }

    @Override
    public SalesTargetApprovedData updateTargetStatusForApprovalMonthly(SalesApprovalData salesApprovalData) {
        SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
        BaseSiteModel baseSite = baseSiteService.getCurrentBaseSite();
        SalesTargetApprovedData data=new SalesTargetApprovedData();
        SubAreaMasterModel subAreaMaster=territoryManagementService.getTerritoryById(salesApprovalData.getSubAreaId());
        MonthlySalesModel monthlySalesModel = null;
        SclUserModel sclUserModel=null;
        SclWorkflowModel sclWorkflowModel=null;
        List<SubAreaMasterModel> subAreaMasterModelList = new ArrayList<>();
        subAreaMasterModelList.add(subAreaMaster);
        List<SclUserModel> usersList = territoryManagementService.getUsersForSubAreas(subAreaMasterModelList);
        if(usersList!=null && !usersList.isEmpty()) {
            for (SclUserModel sclUser : usersList) {
                if (sclUser.getUserType() != null && sclUser.getUserType().equals(SclUserType.SO)) {
                    sclUserModel = (SclUserModel) getUserService().getUserForUID(sclUser.getUid());
                }
                monthlySalesModel = viewMonthlySalesTargetForPlannedTab(salesApprovalData.getSubAreaId(), currentUser);
                if(monthlySalesModel!=null) {
                    sclWorkflowModel = monthlySalesModel.getSclWorkflow();
                }
                //no need of tsm approval
                if(currentUser.getUserType()!=null && currentUser.getUserType().equals(SclUserType.RH))
                {
                    try {
                        if (monthlySalesModel!=null && monthlySalesModel.getSubAreaMaster()!=null && monthlySalesModel.getSubAreaMaster().equals(subAreaMaster)) {
                            //check up on district and region
                            monthlySalesModel.setActionPerformed(WorkflowActions.APPROVED);
                            monthlySalesModel.setActionPerformedBy(currentUser);
                            monthlySalesModel.setActionPerformedDate(new Date());
                            SclWorkflowActionModel sclWorkflowActionModel= sclWorkflowService.saveWorkflowAction(sclWorkflowModel, "targetApprovedByRH", baseSite, subAreaMaster, TerritoryLevels.REGION);
                            if(sclWorkflowActionModel!=null)
                            {
                                sclWorkflowService.updateWorkflowAction(sclWorkflowActionModel, currentUser, WorkflowActions.APPROVED, "Target is approved for district " +subAreaMaster.getDistrictMaster());
                            }
                            sclWorkflowModel.setStatus(WorkflowStatus.IN_PROCESS);
                            modelService.save(sclWorkflowModel);
                            modelService.save(monthlySalesModel);
                        }

                        if(monthlySalesModel!=null) {
                            if (monthlySalesModel.getActionPerformedBy() != null && monthlySalesModel.getActionPerformedBy().equals(currentUser) && monthlySalesModel.getActionPerformed() != null && monthlySalesModel.getActionPerformed().equals(WorkflowActions.APPROVED)) {
                                data.setIsTargetApproved(true);
                            }
                            else{
                                data.setIsTargetApproved(false);
                            }
                        }
                    }
                    catch (ModelSavingException e)
                    {
                        LOG.error("Error occurred while approving target "+e.getMessage()+"\n");
                        return data;
                    }
                }
            }
            return data;
        }
        else{
            LOG.info("User List is Empty from updateTargetStatusForApprovalMonthly");
            return data;
        }


        /*FilterTalukaData filterTalukaData=new FilterTalukaData();
        if (salesApprovalData.getDistrictCode() != null) {
            filterTalukaData.setDistrictCode(salesApprovalData.getDistrictCode());
        }
        List<SubAreaMasterModel> subAreas = territoryManagementService.getTaulkaForUser(filterTalukaData)*/
        /*if(currentUser.getUserType().getCode().equals("RH")) {
            if (districtCode != null) {
                filterTalukaData.setDistrictCode(districtCode);
            }
        }*/
        // FilterDistrictData filterDistrictData=new FilterDistrictData();
        // filterDistrictData.setDistrictName(districtCode);
        // List<DistrictMasterModel> districts = territoryManagementService.getDistrictForUser(filterDistrictData); */
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public ModelService getModelService() {
        return modelService;
    }

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    public ProductService getProductService() {
        return productService;
    }

    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    public SalesPlanningDao getSalesPlanningDao() {
        return salesPlanningDao;
    }

    public void setSalesPlanningDao(SalesPlanningDao salesPlanningDao) {
        this.salesPlanningDao = salesPlanningDao;
    }

    public BaseSiteService getBaseSiteService() {
        return baseSiteService;
    }

    public void setBaseSiteService(BaseSiteService baseSiteService) {
        this.baseSiteService = baseSiteService;
    }

    private ErrorWsDTO getError(final String code, final String reason, final String type) {
        ErrorWsDTO errorWsDTO = new ErrorWsDTO();
        errorWsDTO.setReason(reason);
        errorWsDTO.setType(type);
        errorWsDTO.setErrorCode(code);
        return errorWsDTO;
    }
}
