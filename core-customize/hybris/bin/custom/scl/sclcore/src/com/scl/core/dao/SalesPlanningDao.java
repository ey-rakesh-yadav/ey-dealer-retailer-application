package com.scl.core.dao;

import com.scl.core.enums.CustomerCategory;
import com.scl.core.model.*;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.Date;
import java.util.List;

public interface SalesPlanningDao {
    AnnualSalesModel viewPlannedSalesforDealersRetailersMonthwise(String subArea, SclUserModel sclUser, BaseSiteModel brand);
    List<List<Object>> getLastYearShareForProduct(SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate, String subArea);
    List<List<Object>> getLastYearShareForTarget(SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate, String subArea);
    List<String> getLastYearShareForDealerTarget(String dealerCategory,SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate);
    List<List<Object>> getMonthSplitupForDealerPlannedAnnualSales(String sclCustomerID, SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate, String subArea);
    List<List<Object>> getMonthSplitupForRetailerPlannedAnnualSales(String sclCustomerID, SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate, String subArea);
    List<SclCustomerModel> getDealerDetailsForOnboarded(String subArea,Date startDate,Date endDate,int intervalPeriod);
    List<SclCustomerModel> getDealerDetailsForOnboarded(String subArea,Date startDate,Date endDate,int intervalPeriod,String filter);
    List<SclCustomerModel> getRetailerDetailsForOnboarded(String subArea,Date startDate,Date endDate,int intervalPeriod);
    List<SclCustomerModel> getRetailerDetailsForOnboarded(String subArea,Date startDate,Date endDate,int intervalPeriod,String filter);
    List<List<Object>> viewDealerDetailsForAnnualSales(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate);
    List<List<Object>> viewDealerDetailsForAnnualSales(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate, String filter);
    List<List<Object>> viewRetailerDetailsForAnnualSales(String customerCode, String subArea, SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate);
    List<List<Object>> viewMonthWiseDealerDetailsForAnnualSales(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate);
    List<List<Object>> viewMonthWiseRetailerDetailsForAnnualSales(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate);
    List<List<Object>> reviewMonthWiseSalesWithOnboardedDealers(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate);
    DealerRevisedAnnualSalesModel viewMonthlySalesTargetForDealers(String subArea,SclUserModel sclUser);
    List<List<Object>> fetchDealerCySalesForAnnualSales(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate,String customerCode);
    public DealerRevisedAnnualSalesModel getDealerRevisedAnnualDetailsForMonthlySales(String dealerCode);
    MonthlySalesModel viewMonthlySalesTargetForPlannedTab(String subArea,SclUserModel sclUser);
    List<List<Object>> viewMonthlyRevisedSalesTargetForReviewTab(String subArea, SclUserModel sclUser, BaseSiteModel baseSite,String dealerCode, Date firstDayOfMonth, Date lastDayOfMonth);
    List<List<Object>> getLastYearShareForProductMonthly(SclUserModel sclUser, BaseSiteModel baseSite, Date firstDayOfMonth, Date lastDayOfMonth);
    List<List<Object>> getLastYearShareForTargetMonthly(SclUserModel sclUser, BaseSiteModel baseSite, Date firstDayOfMonth, Date lastDayOfMonth);
    List<String> getLastYearShareForDealerTargetMonthly(String dealerCategory,SclUserModel sclUser, BaseSiteModel baseSite, Date firstDayOfMonth, Date lastDayOfMonth);
    List<List<Object>> fetchDealerDetailsForSelectedRetailer(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, String filter, Date startDate, Date endDate);
    List<List<Object>> fetchRetailerDetailsForSelectedDealer(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, String filter, Date startDate, Date endDate);
    List<List<Object>> fetchDealerCySalesForRetailer(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, String customerCode, Date startDate, Date endDate);

    AnnualSalesModel getAnnualSalesModelDetails(SclUserModel sclUser, String financialYear, String subArea, BaseSiteModel baseSite);
    AnnualSalesModel getAnnualSalesModelDetailsForSH( String financialYear, List<SubAreaMasterModel> subArea, BaseSiteModel baseSite);
    AnnualSalesModel getAnnualSalesModelDetailsForSH_RH( String financialYear, List<DistrictMasterModel> districtMasterModels, BaseSiteModel baseSite);
    MonthlySalesModel getMonthlySalesModelDetail(SclUserModel sclUser, String month, String year, String subArea,BaseSiteModel brand);
    MonthlySalesModel getMonthlySalesModelDetailForDO( String month, String year, List<SubAreaMasterModel> subAreaList,BaseSiteModel brand);
    DealerPlannedAnnualSalesModel fetchRecordForDealerPlannedAnnualSales(String subArea, SclUserModel sclUser, String filter);
    List<DealerRevisedAnnualSalesModel> fetchRecordForDealerRevisedAnnualSales(String subArea, SclUserModel sclUser, String financialYear);
    List<MonthWiseAnnualTargetModel> getMonthWiseAnnualTargetDetails(String customerCode, String productCode, String subarea);
    Double getCurrentMonthSaleForMonthlySummary(String subArea, BaseSiteModel baseSite, SclUserModel sclUser, Date startDate, Date endDate);
    Double getCurrentYearSalesForAnnualSummary(SclUserModel sclUser, BaseSiteModel baseSite, String subArea, Date startDate, Date endDate);
    Double getCurrentYearSalesForAnnualSummaryNew(BaseSiteModel baseSite, String subArea, Date startDate, Date endDate);
    Double getCurrentYearSalesForAnnualSummaryForRH(DistrictMasterModel districtMasterModel,BaseSiteModel baseSite, Date startDate, Date endDate);

    DealerPlannedAnnualSalesModel findDealerDetailsByCustomerCode(String customerCode, String subArea, SclUserModel sclUser, String nextFinancialYear);

    RetailerPlannedAnnualSalesModel findDealerDetailsForRetailerTargetSet(String customerCode, String subArea, SclUserModel sclUser, String nextFinancialYear);

    MonthWiseAnnualTargetModel fetchMonthWiseAnnualTargetDetails(String customerCode, String subArea, String key, String value, SclUserModel sclUser);

    List<List<Object>> getLastYearShareForProductFromNCR(String subArea, String catalogId, String version, Date startDate, Date endDate, CustomerCategory tr);
    List<List<Object>> getLastYearShareForProductFromNCRMonthly(String subArea, String catalogId, String version, int startDate, int endDate, CustomerCategory tr);

    List<List<Object>> getLastYearShareForDealerFromNCRAnnual(String subArea, Date startDate, Date endDate);

    List<List<Object>> getLastYearShareForDealerFromNCRMonthly(String subArea, int year, int month);

    List<String> getStateWiseProductForSummaryPage(String subArea, String catalogId, String version,String prodStatus);
    List<String> getDealerCategoryForSummaryPage();

    MonthWiseAnnualTargetModel fetchMonthWiseAnnualTargetDetailsForSku(String subArea, String customerCode, String productCode, String key, String value, SclUserModel sclUser);

    ProductSaleModel getSalesForSku(String customerCode, String productCode, String subArea, SclUserModel sclUser);

    DealerRevisedAnnualSalesModel findDealerRevisedDetailsByCustomerCode(String customerCode, String subArea, SclUserModel sclUser, String financialYear);

    RetailerPlannedAnnualSalesModel findRetailerDetailsByCustomerCode(String customerCode, String subArea, SclUserModel sclUser, String financialYear);

    RetailerRevisedAnnualSalesModel findRetailerRevisedDetailsByCustomerCode(String customerCode, String subArea, SclUserModel sclUser, String financialYear);

    MonthWiseAnnualTargetModel fetchMonthWiseAnnualTargetDetailsForDealerRetailer(String customerCode, String subArea, String key, String value, SclUserModel sclUser);

    MonthWiseAnnualTargetModel fetchMonthWiseAnnualTargetDetailsForRetailers(String customerCode, String retailerCode, String subArea, String key, String value, SclUserModel sclUser);

    MonthWiseAnnualTargetModel fetchDealerRevisedMonthWiseTargetDetails(String customerCode, String subArea, String monthYear, SclUserModel sclUser, boolean isAnnualSalesRevised);

    MonthWiseAnnualTargetModel fetchDealerRevisedMonthWiseSkuDetails(String subArea, String customerCode, String productCode, String monthYear, SclUserModel sclUser, boolean financialYear);

    MonthWiseAnnualTargetModel fetchRevisedMonthWiseSkuDetails(String subArea, SclUserModel sclUser, String currentMonthName, String customerCode, String productCode,Boolean isAnnualSalesRevised);

    ProductSaleModel getRevisedSalesForSku(String customerCode, String productCode, String subArea, SclUserModel sclUser, boolean isAnnualSalesRevised);

    Double fetchDealerCySalesForRetailerAnnualSales(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, String customerCode, Date startDate, Date endDate);

    List<List<Object>> viewDealerDetailsForRetailerAnnualSales(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate);

    MonthWiseAnnualTargetModel fetchMonthWiseSelfCounterDetails(String customerCode, String selfCounterCode, String subArea, SclUserModel sclUser, String key, String value);

    MonthWiseAnnualTargetModel fetchRevisedMonthWiseSelfCounterDetails(String customerCode, String selfCounterCode, String subArea, SclUserModel sclUser, String monthYear);

    SelfCounterSaleDetailsModel fetchRevisedSelfCounterDetails(String customerCode, String subArea, SclUserModel sclUser, boolean isAnnualSalesRevised);

    SelfCounterSaleDetailsModel fetchSelfCounterDetails(String customerCode, String subArea, SclUserModel sclUser);

    RetailerPlannedAnnualSalesDetailsModel fetchRetailerDetails(String retailerCode, SclUserModel sclUser, String subArea);

    MonthWiseAnnualTargetModel fetchRetailerMonthWiseDetails(String dealerCode, String retailerCode, String subArea, SclUserModel sclUser, String key, String value);

    Double getPlannedMonthSaleForMonthlySaleSummary(SclUserModel sclUser, BaseSiteModel baseSite, String subarea, int month, int year);

    RetailerRevisedAnnualSalesDetailsModel fetchRevisedRetailerDetails(String retailerCode, SclUserModel sclUser, String subArea);

    MonthWiseAnnualTargetModel fetchRevisedRetailerMonthWiseDetails(String dealerCode, String retailerCode, String subArea, SclUserModel sclUser, String monthYear);

    MonthWiseAnnualTargetModel fetchMonthWiseAnnualTargetDetailsOfDealerForRetailer(String customerCode, String subArea, String monthYear, SclUserModel sclUser);

    RetailerPlannedAnnualSalesModel fetchRecordForRetailerPlannedAnnualSales(String subArea, SclUserModel sclUser, String filter);

    List<List<Object>> fetchProductSaleDetailsForSummary(String subArea, SclUserModel sclUser);

    List<List<Object>> getMonthSplitupForDealerForRetailerSetting(String sclCustomerID, SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate, String subArea);

    Double getTotalTargetForDealers(SclUserModel sclUser, String subarea, String financialYear);

    Double getPlannedTargetAfterTargetSetMonthlySP(SclUserModel sclUser, String subArea, String month, String year);
    Double getRevisedTargetAfterTargetSetMonthlySP(SclUserModel sclUser, String subArea, String month, String year);

    Double getPlannedTargetForReviewMonthlySP(SclUserModel sclUser, String subArea, String month, String year);
    Double getPlannedTargetForReviewMonthlySPForRH(DistrictMasterModel districtMasterModel, String month, String year);
    Double getRevisedTargetForReviewMonthlySP(SclUserModel sclUser, String subArea, String month, String year);
    Double getRevisedTargetForReviewMonthlySPForRH(DistrictMasterModel districtMasterModel,String month, String year);

    List<List<Object>> fetchProductMixDetailsAfterTargetSetMonthlySummary(String subArea, SclUserModel sclUser, String month, String year, BaseSiteModel baseSite);
    List<List<Object>> fetchProductMixDetailsAfterTargetSetMonthlySummaryForRH(DistrictMasterModel districtMasterModel, String month, String year);
    List<List<Object>> fetchProductMixDetailsAfterTargetSetMonthlySummaryForSumRH(List<DistrictMasterModel> districtMasterModel, String month, String year);
    List<List<Object>> fetchProductMixDetailsAfterTargetSetMonthlySummaryForTSM(List<SubAreaMasterModel> subArea, String month, String year, BaseSiteModel baseSite);
    List<List<Object>> fetchProductMixDetailsForReviewTargetMonthlySummary(String subArea, SclUserModel sclUser, String month, String year, BaseSiteModel baseSite);
    List<List<Object>> fetchProductMixDetailsForReviewTargetMonthlySummaryForRH(DistrictMasterModel districtMasterModel, String month, String year);
    List<List<Object>> fetchProductMixDetailsForReviewTargetMonthlySummaryForSumRH(List<DistrictMasterModel> districtMasterModel, String month, String year);
    List<List<Object>> fetchProductMixDetailsForReviewTargetMonthlySummaryForTSM(List<SubAreaMasterModel> subArea, String month, String year, BaseSiteModel baseSite);
    List<MonthWiseAnnualTargetModel> getMonthWiseSkuDetailsForReview(String customerCode, String productCode, String subarea, SclUserModel sclUser);
    List<MonthWiseAnnualTargetModel> getMonthWiseRetailerDetailsForReview(String dealerCode, String retailerCode, String subarea, SclUserModel sclUser);

    DealerPlannedMonthlySalesModel checkExistingDealerPlannedMonthlySales(SclUserModel sclUser, String customerCode, String subArea, String monthName, String monthYear);

    ProductSaleModel checkExistingProductSaleForDealerPlannedMonthlySales(String subArea, SclUserModel sclUser, String productCode, String customerCode, String monthName, String monthYear);

    Double getPlannedTargetAfterReviewMonthlySP(SclUserModel sclUser, String subArea, String formattedMonth, String year);

    Double getRevisedTargetAfterReviewMonthlySP(SclUserModel sclUser, String subArea, String formattedMonth, String year, BaseSiteModel site);
    Double getPlannedTargetAfterReviewMonthlySPForRH(DistrictMasterModel districtMasterModel, String formattedMonth, String year);

    Double getRevisedTargetAfterReviewMonthlySPForRH(DistrictMasterModel districtMasterModel, String formattedMonth, String year);

    MonthWiseAnnualTargetModel fetchDealerRevisedMonthWiseSkuDetailsForOnboardedDealer(String subArea, String customerCode, String productCode, String monthYear, SclUserModel sclUser, Boolean isNewDealerOnboarded);

    List<DealerPlannedMonthlySalesModel> fetchDealerPlannedMonthlySalesDetails(String subArea, SclUserModel sclUser, String month, String year);

    //model to be change when revise target approval implemented for monthly sales
    List<DealerRevisedMonthlySalesModel> fetchDealerReviewedMonthlySalesDetails(String subArea, SclUserModel sclUser, String month, String year);
    MonthWiseAnnualTargetModel validateMonthwiseDealerDetailsForNoCySale(String customerCode, String subArea, String key, SclUserModel sclUser);

    double getTotalTargetForDealersAfterTargetSetting(SclUserModel sclUser, String subArea, String financialYear, BaseSiteModel baseSite);
    double getTotalTargetForDealersAfterTargetSettingForRH(DistrictMasterModel districtMasterModel, String financialYear, BaseSiteModel baseSite);

    List<List<Object>> fetchProductSaleDetailsForSummaryAfterTargetSetting(String subArea, SclUserModel sclUser, BaseSiteModel baseSite);
    List<List<Object>> fetchProductSaleDetailsForSummaryAfterTargetSettingForRH(DistrictMasterModel districtMasterModel, BaseSiteModel baseSite);
    List<List<Object>> fetchProductSaleDetailsForSummaryAfterTargetSettingForRH(List<DistrictMasterModel> districtMasterModel, BaseSiteModel baseSite);
    List<List<Object>> fetchProductSaleDetailsForSummaryAfterTargetSettingForTSM(List<SubAreaMasterModel> subArea, BaseSiteModel baseSite);
    List<List<Object>> fetchProductSaleDetailsForSummaryAfterTargetSettingForTSMRH(String subArea, SclUserModel sclUser,String districtCode,String regionCode);

    double getTotalTargetForDealersAfterReview(SclUserModel sclUser, String subArea, String financialYear, BaseSiteModel baseSite);
    double getTotalTargetForDealersAfterReviewForRH(DistrictMasterModel districtMasterModel, String financialYear, BaseSiteModel baseSite);
    double getTotalTargetForDealersAfterReviewForTSMRH(SclUserModel sclUser, String subArea, String financialYear,String districtCode,String regionCode);

    List<List<Object>> fetchProductSaleDetailsForSummaryAfterReview(String subArea, SclUserModel sclUser, BaseSiteModel baseSite);
    List<List<Object>> fetchProductSaleDetailsForSummaryAfterReviewForRH(DistrictMasterModel districtMasterModel, BaseSiteModel baseSite);
    List<List<Object>> fetchProductSaleDetailsForSummaryAfterReviewForRH(List<DistrictMasterModel> districtMasterModel, BaseSiteModel baseSite);
    List<List<Object>> fetchProductSaleDetailsForSummaryAfterReviewForTSM(List<SubAreaMasterModel> subArea, BaseSiteModel baseSite);
    List<List<Object>> fetchProductSaleDetailsForSummaryAfterReviewForTSMRH(String subArea, SclUserModel sclUser,String districtCode,String regionCode);

    MonthWiseAnnualTargetModel validateMonthwiseDealerDetailsForNoCySaleForSku(String customerCode, String productCode, String subArea, String key, SclUserModel sclUser);

    double getPlannedMonthSaleForMonthlySaleSummary(SclUserModel sclUser, String currentMonthName, String subArea, BaseSiteModel baseSite);

    DealerRevisedMonthlySalesModel checkExistingDealerRevisedMonthlySales(SclUserModel sclUser, String customerCode, String subArea, String monthName, String monthYear);

    ProductSaleModel checkExistingProductSaleForDealerRevisedMonthlySales(String subArea, SclUserModel sclUser, String productCode, String customerCode, String monthName, String monthYear);

    List<List<Object>> fetchDealerSaleDetailsForSummaryAfterTargetSetting(String subArea, SclUserModel sclUser);
    List<List<Object>> fetchDealerSaleDetailsForSummaryAfterTargetSettingForRH(DistrictMasterModel districtMasterModel);
    List<List<Object>> fetchDealerSaleDetailsForSummaryAfterTargetSettingForTSMRH(String subArea, SclUserModel sclUser,String districtCode,String regionCode);

    List<List<Object>> fetchDealerSaleDetailsForSummaryForSummaryAfterReview(String subArea, SclUserModel sclUser, BaseSiteModel baseSite);
    List<List<Object>> fetchDealerSaleDetailsForSummaryForSummaryAfterReviewForRH(DistrictMasterModel districtMasterModel);
    List<List<Object>> fetchDealerSaleDetailsForSummaryForSummaryAfterReviewForTSMRH(String subArea, SclUserModel sclUser,String districtCode,String regionCode);

    List<List<Object>> fetchDealerMixDetailsAfterTargetSetMonthlySummary(String subArea, SclUserModel sclUser, String formattedMonth, String valueOf);

    List<List<Object>> fetchDealerMixDetailsAfterTargetSetMonthlySummaryForRH(DistrictMasterModel districtMasterModel, String formattedMonth, String valueOf);

    List<List<Object>> fetchDealerMixDetailsAfterReviewMonthlySummary(String subArea, SclUserModel sclUser, String formattedMonth, String valueOf);
    List<List<Object>> fetchDealerMixDetailsAfterReviewMonthlySummaryForRH(DistrictMasterModel districtMasterModel, String formattedMonth, String valueOf);

    List<ProductSaleModel> checkExistingProductSaleForDealerRevisedMonthlySalesList(String subArea, SclUserModel sclUser, String productCode, String customerCode, String monthName, String monthYear);

    ProductSaleModel fetchProductSaleForDealerPlannedMonthlySales(String subArea, SclUserModel sclUser, String productCode, String customerCode, String monthName, String monthYear);

    ProductSaleModel fetchProductSaleForDealerRevisedMonthlySales(String subArea, SclUserModel sclUser, String productCode, String customerCode, String monthName, String monthYear);

    DealerRevisedAnnualSalesModel validateReviewForExistingDealersSale(String customerCode, String subArea, SclUserModel sclUser, String financialYear);

    MonthWiseAnnualTargetModel validateReviewForOnboardedDealersSaleSkuForMonthWise(String subArea, String customerCode, String productCode, String monthYear, SclUserModel sclUser);

    MonthWiseAnnualTargetModel validateReviewForOnboardedDealersSaleForMonthWise(String subArea, String customerCode, String monthYear, SclUserModel sclUser);

    ProductSaleModel validateReviewForOnboardedDealerSkuSale(String subArea, String customerCode, String productCode, SclUserModel sclUser);

    DealerRevisedAnnualSalesModel validateReviewForOnboardedDealersSale(String customerCode, String subArea, SclUserModel sclUser, String financialYear);

    MonthWiseAnnualTargetModel validateReviewForExistingDealersSaleForMonthWise(String subArea, String customerCode, String monthYear, SclUserModel sclUser);

    MonthWiseAnnualTargetModel validateReviewForExistingDealerSkuSaleForMonthWise(String subArea, String customerCode, String productCode, String monthYear, SclUserModel sclUser);

    ProductSaleModel validateReviewForExistingDealerSkuSale(String subArea, String customerCode, String productCode, SclUserModel sclUser);

    Double getMonthWiseAnnualTargetForDealer(SclUserModel sclUser, String dealerCode, String monthYear, List<SubAreaMasterModel> subAreas);
    SearchPageData<AnnualSalesModel> viewPlannedSalesforDealersRetailersMonthwise(SearchPageData searchPageData,String subArea, SclUserModel sclUser, BaseSiteModel brand);
    SearchPageData<RetailerPlannedAnnualSalesModel> fetchRecordForRetailerPlannedAnnualSales(SearchPageData searchPageData,String subArea, SclUserModel sclUser, String filter);

    List<MonthWiseAnnualTargetModel> validateReviewForOnboardedDealersSaleSkuForMonthWise(String subArea, String customerCode, String productCode, SclUserModel currentUser);
    List<MonthWiseAnnualTargetModel> validateReviewForOnboardedRetailerSaleForMonthWise(String subArea, String customerCode, String productCode, SclUserModel currentUser);
    List<MonthWiseAnnualTargetModel> getMonthWiseSkuDetailsBeforeReview(String customerCode, String productCode, String subarea, SclUserModel sclUser);
    List<MonthWiseAnnualTargetModel> getMonthWiseRetailerDetailsBeforeReview(String dealerCustomerCode, String retailerCustomerCode, String subarea, SclUserModel sclUser);

    ProductSaleModel fetchDealerPlannedAnnualSaleSkuDetails(String customerCode, String productCode, String subArea, SclUserModel sclUser);

    DealerRevisedAnnualSalesModel fetchRecordForDealerRevisedAnnualSalesByCode(String subArea, SclUserModel sclUser, String filter);

    List<MonthWiseAnnualTargetModel> getMonthWiseAnnualTargetDetailsForDealerRevised(String customerCode, String productCode, String subArea);

    double getRetailerCySale(String dealerCode, String retailerCode, String subArea);

    List<List<Object>> getMonthSplitupFormDealerRevisedAnnualSales(String customerCode, SclUserModel sclUser, String subArea);

    List<List<Object>> getRetailerDetailsByDealerCode(String subArea, String dealerCode, Date startDate, Date endDate);

    RetailerRevisedAnnualSalesModel validateReviewForExistingRetailersSale(String dealerCode, String subArea, SclUserModel sclUser, String financialYear);

    MonthWiseAnnualTargetModel validateReviewForExistingRetailersSaleForMonthWise(String subArea, String dealerCode, String monthYear, SclUserModel sclUser);

    SelfCounterSaleDetailsModel validateReviewForExistingSelfCounterSale(String subArea, String selfCounterCode, SclUserModel sclUser);

    MonthWiseAnnualTargetModel validateReviewForExistingSelfCounterSaleMonthWise(String subArea, String selfCounterCode, String monthYear, SclUserModel sclUser, String dealerCode);

    RetailerRevisedAnnualSalesDetailsModel validateReviewForExistingRetailerDetailsSale(String subArea, String retailerCode, SclUserModel sclUser);

    MonthWiseAnnualTargetModel validateReviewForExistingRetailerDetailSaleForMonthWise(String subArea, String retailerCode, String monthYear, SclUserModel sclUser, String dealerCode);

    MonthWiseAnnualTargetModel validateReviewForOnboardRetailerDetailSaleForMonthWise(String subArea, String retailerCode, String monthYear, SclUserModel sclUser, String dealerCode);

    RetailerRevisedAnnualSalesDetailsModel validateReviewForOnboardRetailerDetailsSale(String subArea, String retailerCode, SclUserModel sclUser);

    MonthWiseAnnualTargetModel validateOnboardedRetailerSelfCounterSaleMonthWise(String subArea, String selfCounterCode, String monthYear, SclUserModel sclUser, String dealerCode);

    SelfCounterSaleDetailsModel validateOnboardedRetailerSelfCounterSale(String subArea, String selfCounterCode, SclUserModel sclUser);

    MonthWiseAnnualTargetModel validateReviewForOnboardedRetailersSaleForMonthWise(String subArea, String dealerCode, String monthYear, SclUserModel sclUser);

    RetailerRevisedAnnualSalesModel validateReviewForOnboardedRetailersSale(String dealerCode, String subArea, SclUserModel sclUser, String financialYear);

    List<String> getRetailerListByDealerCode(String dealerCode, String subArea);
  
    Double getDealerSalesMonthlyTarget(String dealerUid, String monthYear, String monthName);
    
    Double getDealerSalesAnnualTarget(String dealerUid, String monthYear);

    AnnualSalesModel getAnnualSalesModelDetailsForTSM(String financialYear, DistrictMasterModel districtMaster, BaseSiteModel brand);
    AnnualSalesModel getAnnualSalesModelDetailsForRH(String financialYear, RegionMasterModel regionMaster, BaseSiteModel brand);

    MonthlySalesModel getMonthlySalesModelDetailsForTSM(String month, String year,  DistrictMasterModel districtMaster, BaseSiteModel brand);
    MonthlySalesModel getMonthlySalesModelDetailsForRH(String month, String year, RegionMasterModel regionMaster, BaseSiteModel brand);

    List<List<Object>> getMonthwiseTargetsForSubarea(String subArea);
    List<List<Object>> getMonthwiseSkuTargetsForSubarea(String productCode, String subArea);
    List<String> getSkuListForFinalizedTargets(String subArea);
    List<AnnualSalesModel> getAnnualSalesModelDetailsForDistrict(String financialYear, DistrictMasterModel districtMaster, BaseSiteModel brand);
    List<List<Object>> getMonthwiseTargetsForTerritory(SubAreaMasterModel subArea, DistrictMasterModel district);
    List<List<Object>> getMonthwiseSkuTargetsForTerritory(String productCode, SubAreaMasterModel subArea,DistrictMasterModel district);
    List<String> getSkuListForTerritory(SubAreaMasterModel subArea, DistrictMasterModel district);

    List<List<Object>> getMonthwiseTargetsForDistrict(DistrictMasterModel district);
    List<List<Object>> getMonthwiseSkuTargetsForDistrict(String productCode, DistrictMasterModel district);
    List<String> getSkuListForFinalizedTargets(DistrictMasterModel district);

    List<List<Object>> getMonthwiseSkuTargetsForSubareaTsm(String productCode, String subArea);

    List<ProductSaleModel> getSalesForNewSku(String customerCode, String subArea, SclUserModel sclUser);

    List<ProductSaleModel> getSalesForSkus(String customerCode, String subArea, SclUserModel sclUser);

    List<MonthlySalesModel> getMonthlySalesModelDetailsListForDO(String month, String year, List<SubAreaMasterModel> taulkaForUser, BaseSiteModel currentBaseSite);

    AnnualSalesModel getAnnualSalesModelDetails1(SclUserModel sclUser, String financialYear, String subArea, BaseSiteModel baseSite);

    AnnualSalesModel viewPlannedSalesforDealersRetailersMonthwise1(String subArea, SclUserModel sclUserModel, BaseSiteModel brand);

    MonthlySalesModel getMonthlySalesModelDetail1(SclUserModel sclUser, String monthPlus, String year, String subArea, BaseSiteModel currentBaseSite);

    AnnualSalesModel viewAnnualSalesModelForDistrict(String districtCode, SclUserModel sclUser, BaseSiteModel baseSite);
}


