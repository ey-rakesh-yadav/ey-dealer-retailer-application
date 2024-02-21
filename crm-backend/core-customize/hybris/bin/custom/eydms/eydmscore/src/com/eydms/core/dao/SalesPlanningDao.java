package com.eydms.core.dao;

import com.eydms.core.enums.CustomerCategory;
import com.eydms.core.model.*;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.Date;
import java.util.List;

public interface SalesPlanningDao {
    AnnualSalesModel viewPlannedSalesforDealersRetailersMonthwise(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel brand);
    List<List<Object>> getLastYearShareForProduct(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, Date startDate, Date endDate, String subArea);
    List<List<Object>> getLastYearShareForTarget(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, Date startDate, Date endDate, String subArea);
    List<String> getLastYearShareForDealerTarget(String dealerCategory,EyDmsUserModel eydmsUser, BaseSiteModel baseSite, Date startDate, Date endDate);
    List<List<Object>> getMonthSplitupForDealerPlannedAnnualSales(String eydmsCustomerID, EyDmsUserModel eydmsUser, BaseSiteModel baseSite, Date startDate, Date endDate, String subArea);
    List<List<Object>> getMonthSplitupForRetailerPlannedAnnualSales(String eydmsCustomerID, EyDmsUserModel eydmsUser, BaseSiteModel baseSite, Date startDate, Date endDate, String subArea);
    List<EyDmsCustomerModel> getDealerDetailsForOnboarded(String subArea,Date startDate,Date endDate,int intervalPeriod);
    List<EyDmsCustomerModel> getDealerDetailsForOnboarded(String subArea,Date startDate,Date endDate,int intervalPeriod,String filter);
    List<EyDmsCustomerModel> getRetailerDetailsForOnboarded(String subArea,Date startDate,Date endDate,int intervalPeriod);
    List<EyDmsCustomerModel> getRetailerDetailsForOnboarded(String subArea,Date startDate,Date endDate,int intervalPeriod,String filter);
    List<List<Object>> viewDealerDetailsForAnnualSales(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel baseSite, Date startDate, Date endDate);
    List<List<Object>> viewDealerDetailsForAnnualSales(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel baseSite, Date startDate, Date endDate, String filter);
    List<List<Object>> viewRetailerDetailsForAnnualSales(String customerCode, String subArea, EyDmsUserModel eydmsUser, BaseSiteModel baseSite, Date startDate, Date endDate);
    List<List<Object>> viewMonthWiseDealerDetailsForAnnualSales(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel baseSite, Date startDate, Date endDate);
    List<List<Object>> viewMonthWiseRetailerDetailsForAnnualSales(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel baseSite, Date startDate, Date endDate);
    List<List<Object>> reviewMonthWiseSalesWithOnboardedDealers(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel baseSite, Date startDate, Date endDate);
    DealerRevisedAnnualSalesModel viewMonthlySalesTargetForDealers(String subArea,EyDmsUserModel eydmsUser);
    List<List<Object>> fetchDealerCySalesForAnnualSales(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel baseSite, Date startDate, Date endDate,String customerCode);
    public DealerRevisedAnnualSalesModel getDealerRevisedAnnualDetailsForMonthlySales(String dealerCode);
    MonthlySalesModel viewMonthlySalesTargetForPlannedTab(String subArea,EyDmsUserModel eydmsUser);
    List<List<Object>> viewMonthlyRevisedSalesTargetForReviewTab(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel baseSite,String dealerCode, Date firstDayOfMonth, Date lastDayOfMonth);
    List<List<Object>> getLastYearShareForProductMonthly(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, Date firstDayOfMonth, Date lastDayOfMonth);
    List<List<Object>> getLastYearShareForTargetMonthly(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, Date firstDayOfMonth, Date lastDayOfMonth);
    List<String> getLastYearShareForDealerTargetMonthly(String dealerCategory,EyDmsUserModel eydmsUser, BaseSiteModel baseSite, Date firstDayOfMonth, Date lastDayOfMonth);
    List<List<Object>> fetchDealerDetailsForSelectedRetailer(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel baseSite, String filter, Date startDate, Date endDate);
    List<List<Object>> fetchRetailerDetailsForSelectedDealer(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel baseSite, String filter, Date startDate, Date endDate);
    List<List<Object>> fetchDealerCySalesForRetailer(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel baseSite, String customerCode, Date startDate, Date endDate);

    AnnualSalesModel getAnnualSalesModelDetails(EyDmsUserModel eydmsUser, String financialYear, String subArea, BaseSiteModel baseSite);
    AnnualSalesModel getAnnualSalesModelDetailsForSH( String financialYear, List<SubAreaMasterModel> subArea, BaseSiteModel baseSite);
    AnnualSalesModel getAnnualSalesModelDetailsForSH_RH( String financialYear, List<DistrictMasterModel> districtMasterModels, BaseSiteModel baseSite);
    MonthlySalesModel getMonthlySalesModelDetail(EyDmsUserModel eydmsUser, String month, String year, String subArea,BaseSiteModel brand);
    MonthlySalesModel getMonthlySalesModelDetailForDO( String month, String year, List<SubAreaMasterModel> subAreaList,BaseSiteModel brand);
    DealerPlannedAnnualSalesModel fetchRecordForDealerPlannedAnnualSales(String subArea, EyDmsUserModel eydmsUser, String filter);
    List<DealerRevisedAnnualSalesModel> fetchRecordForDealerRevisedAnnualSales(String subArea, EyDmsUserModel eydmsUser, String financialYear);
    List<MonthWiseAnnualTargetModel> getMonthWiseAnnualTargetDetails(String customerCode, String productCode, String subarea);
    Double getCurrentMonthSaleForMonthlySummary(String subArea, BaseSiteModel baseSite, EyDmsUserModel eydmsUser, Date startDate, Date endDate);
    Double getCurrentYearSalesForAnnualSummary(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, String subArea, Date startDate, Date endDate);
    Double getCurrentYearSalesForAnnualSummaryNew(BaseSiteModel baseSite, String subArea, Date startDate, Date endDate);
    Double getCurrentYearSalesForAnnualSummaryForRH(DistrictMasterModel districtMasterModel,BaseSiteModel baseSite, Date startDate, Date endDate);

    DealerPlannedAnnualSalesModel findDealerDetailsByCustomerCode(String customerCode, String subArea, EyDmsUserModel eydmsUser, String nextFinancialYear);

    RetailerPlannedAnnualSalesModel findDealerDetailsForRetailerTargetSet(String customerCode, String subArea, EyDmsUserModel eydmsUser, String nextFinancialYear);

    MonthWiseAnnualTargetModel fetchMonthWiseAnnualTargetDetails(String customerCode, String subArea, String key, String value, EyDmsUserModel eydmsUser);

    List<List<Object>> getLastYearShareForProductFromNCR(String subArea, String catalogId, String version, Date startDate, Date endDate, CustomerCategory tr);
    List<List<Object>> getLastYearShareForProductFromNCRMonthly(String subArea, String catalogId, String version, int startDate, int endDate, CustomerCategory tr);

    List<List<Object>> getLastYearShareForDealerFromNCRAnnual(String subArea, Date startDate, Date endDate);

    List<List<Object>> getLastYearShareForDealerFromNCRMonthly(String subArea, int year, int month);

    List<String> getStateWiseProductForSummaryPage(String subArea, String catalogId, String version,String prodStatus);
    List<String> getDealerCategoryForSummaryPage();

    MonthWiseAnnualTargetModel fetchMonthWiseAnnualTargetDetailsForSku(String subArea, String customerCode, String productCode, String key, String value, EyDmsUserModel eydmsUser);

    ProductSaleModel getSalesForSku(String customerCode, String productCode, String subArea, EyDmsUserModel eydmsUser);

    DealerRevisedAnnualSalesModel findDealerRevisedDetailsByCustomerCode(String customerCode, String subArea, EyDmsUserModel eydmsUser, String financialYear);

    RetailerPlannedAnnualSalesModel findRetailerDetailsByCustomerCode(String customerCode, String subArea, EyDmsUserModel eydmsUser, String financialYear);

    RetailerRevisedAnnualSalesModel findRetailerRevisedDetailsByCustomerCode(String customerCode, String subArea, EyDmsUserModel eydmsUser, String financialYear);

    MonthWiseAnnualTargetModel fetchMonthWiseAnnualTargetDetailsForDealerRetailer(String customerCode, String subArea, String key, String value, EyDmsUserModel eydmsUser);

    MonthWiseAnnualTargetModel fetchMonthWiseAnnualTargetDetailsForRetailers(String customerCode, String retailerCode, String subArea, String key, String value, EyDmsUserModel eydmsUser);

    MonthWiseAnnualTargetModel fetchDealerRevisedMonthWiseTargetDetails(String customerCode, String subArea, String monthYear, EyDmsUserModel eydmsUser, boolean isAnnualSalesRevised);

    MonthWiseAnnualTargetModel fetchDealerRevisedMonthWiseSkuDetails(String subArea, String customerCode, String productCode, String monthYear, EyDmsUserModel eydmsUser, boolean financialYear);

    MonthWiseAnnualTargetModel fetchRevisedMonthWiseSkuDetails(String subArea, EyDmsUserModel eydmsUser, String currentMonthName, String customerCode, String productCode,Boolean isAnnualSalesRevised);

    ProductSaleModel getRevisedSalesForSku(String customerCode, String productCode, String subArea, EyDmsUserModel eydmsUser, boolean isAnnualSalesRevised);

    Double fetchDealerCySalesForRetailerAnnualSales(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel baseSite, String customerCode, Date startDate, Date endDate);

    List<List<Object>> viewDealerDetailsForRetailerAnnualSales(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel baseSite, Date startDate, Date endDate);

    MonthWiseAnnualTargetModel fetchMonthWiseSelfCounterDetails(String customerCode, String selfCounterCode, String subArea, EyDmsUserModel eydmsUser, String key, String value);

    MonthWiseAnnualTargetModel fetchRevisedMonthWiseSelfCounterDetails(String customerCode, String selfCounterCode, String subArea, EyDmsUserModel eydmsUser, String monthYear);

    SelfCounterSaleDetailsModel fetchRevisedSelfCounterDetails(String customerCode, String subArea, EyDmsUserModel eydmsUser, boolean isAnnualSalesRevised);

    SelfCounterSaleDetailsModel fetchSelfCounterDetails(String customerCode, String subArea, EyDmsUserModel eydmsUser);

    RetailerPlannedAnnualSalesDetailsModel fetchRetailerDetails(String retailerCode, EyDmsUserModel eydmsUser, String subArea);

    MonthWiseAnnualTargetModel fetchRetailerMonthWiseDetails(String dealerCode, String retailerCode, String subArea, EyDmsUserModel eydmsUser, String key, String value);

    Double getPlannedMonthSaleForMonthlySaleSummary(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, String subarea, int month, int year);

    RetailerRevisedAnnualSalesDetailsModel fetchRevisedRetailerDetails(String retailerCode, EyDmsUserModel eydmsUser, String subArea);

    MonthWiseAnnualTargetModel fetchRevisedRetailerMonthWiseDetails(String dealerCode, String retailerCode, String subArea, EyDmsUserModel eydmsUser, String monthYear);

    MonthWiseAnnualTargetModel fetchMonthWiseAnnualTargetDetailsOfDealerForRetailer(String customerCode, String subArea, String monthYear, EyDmsUserModel eydmsUser);

    RetailerPlannedAnnualSalesModel fetchRecordForRetailerPlannedAnnualSales(String subArea, EyDmsUserModel eydmsUser, String filter);

    List<List<Object>> fetchProductSaleDetailsForSummary(String subArea, EyDmsUserModel eydmsUser);

    List<List<Object>> getMonthSplitupForDealerForRetailerSetting(String eydmsCustomerID, EyDmsUserModel eydmsUser, BaseSiteModel baseSite, Date startDate, Date endDate, String subArea);

    Double getTotalTargetForDealers(EyDmsUserModel eydmsUser, String subarea, String financialYear);

    Double getPlannedTargetAfterTargetSetMonthlySP(EyDmsUserModel eydmsUser, String subArea, String month, String year);
    Double getRevisedTargetAfterTargetSetMonthlySP(EyDmsUserModel eydmsUser, String subArea, String month, String year);

    Double getPlannedTargetForReviewMonthlySP(EyDmsUserModel eydmsUser, String subArea, String month, String year);
    Double getPlannedTargetForReviewMonthlySPForRH(DistrictMasterModel districtMasterModel, String month, String year);
    Double getRevisedTargetForReviewMonthlySP(EyDmsUserModel eydmsUser, String subArea, String month, String year);
    Double getRevisedTargetForReviewMonthlySPForRH(DistrictMasterModel districtMasterModel,String month, String year);

    List<List<Object>> fetchProductMixDetailsAfterTargetSetMonthlySummary(String subArea, EyDmsUserModel eydmsUser, String month, String year, BaseSiteModel baseSite);
    List<List<Object>> fetchProductMixDetailsAfterTargetSetMonthlySummaryForRH(DistrictMasterModel districtMasterModel, String month, String year);
    List<List<Object>> fetchProductMixDetailsAfterTargetSetMonthlySummaryForSumRH(List<DistrictMasterModel> districtMasterModel, String month, String year);
    List<List<Object>> fetchProductMixDetailsAfterTargetSetMonthlySummaryForTSM(List<SubAreaMasterModel> subArea, String month, String year, BaseSiteModel baseSite);
    List<List<Object>> fetchProductMixDetailsForReviewTargetMonthlySummary(String subArea, EyDmsUserModel eydmsUser, String month, String year, BaseSiteModel baseSite);
    List<List<Object>> fetchProductMixDetailsForReviewTargetMonthlySummaryForRH(DistrictMasterModel districtMasterModel, String month, String year);
    List<List<Object>> fetchProductMixDetailsForReviewTargetMonthlySummaryForSumRH(List<DistrictMasterModel> districtMasterModel, String month, String year);
    List<List<Object>> fetchProductMixDetailsForReviewTargetMonthlySummaryForTSM(List<SubAreaMasterModel> subArea, String month, String year, BaseSiteModel baseSite);
    List<MonthWiseAnnualTargetModel> getMonthWiseSkuDetailsForReview(String customerCode, String productCode, String subarea, EyDmsUserModel eydmsUser);
    List<MonthWiseAnnualTargetModel> getMonthWiseRetailerDetailsForReview(String dealerCode, String retailerCode, String subarea, EyDmsUserModel eydmsUser);

    DealerPlannedMonthlySalesModel checkExistingDealerPlannedMonthlySales(EyDmsUserModel eydmsUser, String customerCode, String subArea, String monthName, String monthYear);

    ProductSaleModel checkExistingProductSaleForDealerPlannedMonthlySales(String subArea, EyDmsUserModel eydmsUser, String productCode, String customerCode, String monthName, String monthYear);

    Double getPlannedTargetAfterReviewMonthlySP(EyDmsUserModel eydmsUser, String subArea, String formattedMonth, String year);

    Double getRevisedTargetAfterReviewMonthlySP(EyDmsUserModel eydmsUser, String subArea, String formattedMonth, String year, BaseSiteModel site);
    Double getPlannedTargetAfterReviewMonthlySPForRH(DistrictMasterModel districtMasterModel, String formattedMonth, String year);

    Double getRevisedTargetAfterReviewMonthlySPForRH(DistrictMasterModel districtMasterModel, String formattedMonth, String year);

    MonthWiseAnnualTargetModel fetchDealerRevisedMonthWiseSkuDetailsForOnboardedDealer(String subArea, String customerCode, String productCode, String monthYear, EyDmsUserModel eydmsUser, Boolean isNewDealerOnboarded);

    List<DealerPlannedMonthlySalesModel> fetchDealerPlannedMonthlySalesDetails(String subArea, EyDmsUserModel eydmsUser, String month, String year);

    //model to be change when revise target approval implemented for monthly sales
    List<DealerRevisedMonthlySalesModel> fetchDealerReviewedMonthlySalesDetails(String subArea, EyDmsUserModel eydmsUser, String month, String year);
    MonthWiseAnnualTargetModel validateMonthwiseDealerDetailsForNoCySale(String customerCode, String subArea, String key, EyDmsUserModel eydmsUser);

    double getTotalTargetForDealersAfterTargetSetting(EyDmsUserModel eydmsUser, String subArea, String financialYear, BaseSiteModel baseSite);
    double getTotalTargetForDealersAfterTargetSettingForRH(DistrictMasterModel districtMasterModel, String financialYear, BaseSiteModel baseSite);

    List<List<Object>> fetchProductSaleDetailsForSummaryAfterTargetSetting(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel baseSite);
    List<List<Object>> fetchProductSaleDetailsForSummaryAfterTargetSettingForRH(DistrictMasterModel districtMasterModel, BaseSiteModel baseSite);
    List<List<Object>> fetchProductSaleDetailsForSummaryAfterTargetSettingForRH(List<DistrictMasterModel> districtMasterModel, BaseSiteModel baseSite);
    List<List<Object>> fetchProductSaleDetailsForSummaryAfterTargetSettingForTSM(List<SubAreaMasterModel> subArea, BaseSiteModel baseSite);
    List<List<Object>> fetchProductSaleDetailsForSummaryAfterTargetSettingForTSMRH(String subArea, EyDmsUserModel eydmsUser,String districtCode,String regionCode);

    double getTotalTargetForDealersAfterReview(EyDmsUserModel eydmsUser, String subArea, String financialYear, BaseSiteModel baseSite);
    double getTotalTargetForDealersAfterReviewForRH(DistrictMasterModel districtMasterModel, String financialYear, BaseSiteModel baseSite);
    double getTotalTargetForDealersAfterReviewForTSMRH(EyDmsUserModel eydmsUser, String subArea, String financialYear,String districtCode,String regionCode);

    List<List<Object>> fetchProductSaleDetailsForSummaryAfterReview(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel baseSite);
    List<List<Object>> fetchProductSaleDetailsForSummaryAfterReviewForRH(DistrictMasterModel districtMasterModel, BaseSiteModel baseSite);
    List<List<Object>> fetchProductSaleDetailsForSummaryAfterReviewForRH(List<DistrictMasterModel> districtMasterModel, BaseSiteModel baseSite);
    List<List<Object>> fetchProductSaleDetailsForSummaryAfterReviewForTSM(List<SubAreaMasterModel> subArea, BaseSiteModel baseSite);
    List<List<Object>> fetchProductSaleDetailsForSummaryAfterReviewForTSMRH(String subArea, EyDmsUserModel eydmsUser,String districtCode,String regionCode);

    MonthWiseAnnualTargetModel validateMonthwiseDealerDetailsForNoCySaleForSku(String customerCode, String productCode, String subArea, String key, EyDmsUserModel eydmsUser);

    double getPlannedMonthSaleForMonthlySaleSummary(EyDmsUserModel eydmsUser, String currentMonthName, String subArea, BaseSiteModel baseSite);

    DealerRevisedMonthlySalesModel checkExistingDealerRevisedMonthlySales(EyDmsUserModel eydmsUser, String customerCode, String subArea, String monthName, String monthYear);

    ProductSaleModel checkExistingProductSaleForDealerRevisedMonthlySales(String subArea, EyDmsUserModel eydmsUser, String productCode, String customerCode, String monthName, String monthYear);

    List<List<Object>> fetchDealerSaleDetailsForSummaryAfterTargetSetting(String subArea, EyDmsUserModel eydmsUser);
    List<List<Object>> fetchDealerSaleDetailsForSummaryAfterTargetSettingForRH(DistrictMasterModel districtMasterModel);
    List<List<Object>> fetchDealerSaleDetailsForSummaryAfterTargetSettingForTSMRH(String subArea, EyDmsUserModel eydmsUser,String districtCode,String regionCode);

    List<List<Object>> fetchDealerSaleDetailsForSummaryForSummaryAfterReview(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel baseSite);
    List<List<Object>> fetchDealerSaleDetailsForSummaryForSummaryAfterReviewForRH(DistrictMasterModel districtMasterModel);
    List<List<Object>> fetchDealerSaleDetailsForSummaryForSummaryAfterReviewForTSMRH(String subArea, EyDmsUserModel eydmsUser,String districtCode,String regionCode);

    List<List<Object>> fetchDealerMixDetailsAfterTargetSetMonthlySummary(String subArea, EyDmsUserModel eydmsUser, String formattedMonth, String valueOf);

    List<List<Object>> fetchDealerMixDetailsAfterTargetSetMonthlySummaryForRH(DistrictMasterModel districtMasterModel, String formattedMonth, String valueOf);

    List<List<Object>> fetchDealerMixDetailsAfterReviewMonthlySummary(String subArea, EyDmsUserModel eydmsUser, String formattedMonth, String valueOf);
    List<List<Object>> fetchDealerMixDetailsAfterReviewMonthlySummaryForRH(DistrictMasterModel districtMasterModel, String formattedMonth, String valueOf);

    List<ProductSaleModel> checkExistingProductSaleForDealerRevisedMonthlySalesList(String subArea, EyDmsUserModel eydmsUser, String productCode, String customerCode, String monthName, String monthYear);

    ProductSaleModel fetchProductSaleForDealerPlannedMonthlySales(String subArea, EyDmsUserModel eydmsUser, String productCode, String customerCode, String monthName, String monthYear);

    ProductSaleModel fetchProductSaleForDealerRevisedMonthlySales(String subArea, EyDmsUserModel eydmsUser, String productCode, String customerCode, String monthName, String monthYear);

    DealerRevisedAnnualSalesModel validateReviewForExistingDealersSale(String customerCode, String subArea, EyDmsUserModel eydmsUser, String financialYear);

    MonthWiseAnnualTargetModel validateReviewForOnboardedDealersSaleSkuForMonthWise(String subArea, String customerCode, String productCode, String monthYear, EyDmsUserModel eydmsUser);

    MonthWiseAnnualTargetModel validateReviewForOnboardedDealersSaleForMonthWise(String subArea, String customerCode, String monthYear, EyDmsUserModel eydmsUser);

    ProductSaleModel validateReviewForOnboardedDealerSkuSale(String subArea, String customerCode, String productCode, EyDmsUserModel eydmsUser);

    DealerRevisedAnnualSalesModel validateReviewForOnboardedDealersSale(String customerCode, String subArea, EyDmsUserModel eydmsUser, String financialYear);

    MonthWiseAnnualTargetModel validateReviewForExistingDealersSaleForMonthWise(String subArea, String customerCode, String monthYear, EyDmsUserModel eydmsUser);

    MonthWiseAnnualTargetModel validateReviewForExistingDealerSkuSaleForMonthWise(String subArea, String customerCode, String productCode, String monthYear, EyDmsUserModel eydmsUser);

    ProductSaleModel validateReviewForExistingDealerSkuSale(String subArea, String customerCode, String productCode, EyDmsUserModel eydmsUser);

    Double getMonthWiseAnnualTargetForDealer(EyDmsUserModel eydmsUser, String dealerCode, String monthYear, List<SubAreaMasterModel> subAreas);
    SearchPageData<AnnualSalesModel> viewPlannedSalesforDealersRetailersMonthwise(SearchPageData searchPageData,String subArea, EyDmsUserModel eydmsUser, BaseSiteModel brand);
    SearchPageData<RetailerPlannedAnnualSalesModel> fetchRecordForRetailerPlannedAnnualSales(SearchPageData searchPageData,String subArea, EyDmsUserModel eydmsUser, String filter);

    List<MonthWiseAnnualTargetModel> validateReviewForOnboardedDealersSaleSkuForMonthWise(String subArea, String customerCode, String productCode, EyDmsUserModel currentUser);
    List<MonthWiseAnnualTargetModel> validateReviewForOnboardedRetailerSaleForMonthWise(String subArea, String customerCode, String productCode, EyDmsUserModel currentUser);
    List<MonthWiseAnnualTargetModel> getMonthWiseSkuDetailsBeforeReview(String customerCode, String productCode, String subarea, EyDmsUserModel eydmsUser);
    List<MonthWiseAnnualTargetModel> getMonthWiseRetailerDetailsBeforeReview(String dealerCustomerCode, String retailerCustomerCode, String subarea, EyDmsUserModel eydmsUser);

    ProductSaleModel fetchDealerPlannedAnnualSaleSkuDetails(String customerCode, String productCode, String subArea, EyDmsUserModel eydmsUser);

    DealerRevisedAnnualSalesModel fetchRecordForDealerRevisedAnnualSalesByCode(String subArea, EyDmsUserModel eydmsUser, String filter);

    List<MonthWiseAnnualTargetModel> getMonthWiseAnnualTargetDetailsForDealerRevised(String customerCode, String productCode, String subArea);

    double getRetailerCySale(String dealerCode, String retailerCode, String subArea);

    List<List<Object>> getMonthSplitupFormDealerRevisedAnnualSales(String customerCode, EyDmsUserModel eydmsUser, String subArea);

    List<List<Object>> getRetailerDetailsByDealerCode(String subArea, String dealerCode, Date startDate, Date endDate);

    RetailerRevisedAnnualSalesModel validateReviewForExistingRetailersSale(String dealerCode, String subArea, EyDmsUserModel eydmsUser, String financialYear);

    MonthWiseAnnualTargetModel validateReviewForExistingRetailersSaleForMonthWise(String subArea, String dealerCode, String monthYear, EyDmsUserModel eydmsUser);

    SelfCounterSaleDetailsModel validateReviewForExistingSelfCounterSale(String subArea, String selfCounterCode, EyDmsUserModel eydmsUser);

    MonthWiseAnnualTargetModel validateReviewForExistingSelfCounterSaleMonthWise(String subArea, String selfCounterCode, String monthYear, EyDmsUserModel eydmsUser, String dealerCode);

    RetailerRevisedAnnualSalesDetailsModel validateReviewForExistingRetailerDetailsSale(String subArea, String retailerCode, EyDmsUserModel eydmsUser);

    MonthWiseAnnualTargetModel validateReviewForExistingRetailerDetailSaleForMonthWise(String subArea, String retailerCode, String monthYear, EyDmsUserModel eydmsUser, String dealerCode);

    MonthWiseAnnualTargetModel validateReviewForOnboardRetailerDetailSaleForMonthWise(String subArea, String retailerCode, String monthYear, EyDmsUserModel eydmsUser, String dealerCode);

    RetailerRevisedAnnualSalesDetailsModel validateReviewForOnboardRetailerDetailsSale(String subArea, String retailerCode, EyDmsUserModel eydmsUser);

    MonthWiseAnnualTargetModel validateOnboardedRetailerSelfCounterSaleMonthWise(String subArea, String selfCounterCode, String monthYear, EyDmsUserModel eydmsUser, String dealerCode);

    SelfCounterSaleDetailsModel validateOnboardedRetailerSelfCounterSale(String subArea, String selfCounterCode, EyDmsUserModel eydmsUser);

    MonthWiseAnnualTargetModel validateReviewForOnboardedRetailersSaleForMonthWise(String subArea, String dealerCode, String monthYear, EyDmsUserModel eydmsUser);

    RetailerRevisedAnnualSalesModel validateReviewForOnboardedRetailersSale(String dealerCode, String subArea, EyDmsUserModel eydmsUser, String financialYear);

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

    List<ProductSaleModel> getSalesForNewSku(String customerCode, String subArea, EyDmsUserModel eydmsUser);

    List<ProductSaleModel> getSalesForSkus(String customerCode, String subArea, EyDmsUserModel eydmsUser);

    List<MonthlySalesModel> getMonthlySalesModelDetailsListForDO(String month, String year, List<SubAreaMasterModel> taulkaForUser, BaseSiteModel currentBaseSite);

    AnnualSalesModel getAnnualSalesModelDetails1(EyDmsUserModel eydmsUser, String financialYear, String subArea, BaseSiteModel baseSite);

    AnnualSalesModel viewPlannedSalesforDealersRetailersMonthwise1(String subArea, EyDmsUserModel eydmsUserModel, BaseSiteModel brand);

    MonthlySalesModel getMonthlySalesModelDetail1(EyDmsUserModel eydmsUser, String monthPlus, String year, String subArea, BaseSiteModel currentBaseSite);

    AnnualSalesModel viewAnnualSalesModelForDistrict(String districtCode, EyDmsUserModel eydmsUser, BaseSiteModel baseSite);
}


