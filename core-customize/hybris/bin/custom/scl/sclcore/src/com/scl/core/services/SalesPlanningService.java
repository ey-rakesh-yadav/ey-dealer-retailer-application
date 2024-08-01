package com.scl.core.services;

import com.scl.core.model.*;
import com.scl.facades.data.*;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;

import java.util.Date;
import java.util.List;

public interface SalesPlanningService {
    ErrorListWsDTO submitAnnualSalesTargetSettingForDealers(AnnualSalesTargetSettingListData data);
    ErrorListWsDTO submitAnnualSalesTargetSettingForRetailers(AnnualSalesTargetSettingListData data);
    ErrorListWsDTO submitFinalizeAnnualSalesTargetSettingForDealers(AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData);
    //boolean submitFinalizeAnnualSalesTargetSettingForDealers(AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData);
    ErrorListWsDTO submitFinalizeAnnualSalesTargetSettingForRetailers(AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData);
    boolean submitOnboardedAnnualSalesTargetSettingForDealers(AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData);
    boolean submitOnboardedAnnualSalesTargetSettingForRetailers(AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData);
    AnnualSalesModel viewPlannedSalesforDealersRetailersMonthWise(String subArea, SclUserModel sclUser, BaseSiteModel brand);
    List<List<Object>> getLastYearShareForProduct(SclUserModel sclUser, BaseSiteModel baseSite, String s);
    List<List<Object>> getLastYearShareForTarget(SclUserModel sclUser, BaseSiteModel baseSite, String s);
    List<String> getLastYearShareForDealerTarget(String dealerCategory,SclUserModel sclUser, BaseSiteModel baseSite);

    List<SclCustomerModel> getDealerDetailsForOnboarded(String subArea, int intervalPeriod, String filter);
    List<SclCustomerModel> getRetailerDetailsForOnboarded(String subArea,int intervalPeriod, String filter);

    List<List<Object>> viewDealerDetailsForAnnualSales(String subArea, SclUserModel sclUser, BaseSiteModel baseSite);
    List<List<Object>> viewDealerDetailsForAnnualSales(String subArea, SclUserModel sclUser, BaseSiteModel baseSite,String filter);
    List<List<Object>> viewRetailerDetailsForAnnualSales(String customerCode, String subArea, SclUserModel sclUser, BaseSiteModel baseSite);
    List<List<Object>> viewMonthWiseDealerDetailsForAnnualSales(BaseSiteModel baseSite, SclUserModel sclUser, String subArea);
    List<List<Object>> viewMonthWiseRetailerDetailsForAnnualSales(BaseSiteModel baseSite, SclUserModel sclUser, String subArea);

    DealerRevisedAnnualSalesModel viewMonthlySalesTargetForDealers(String subArea, SclUserModel sclUser);

    List<List<Object>> fetchDealerCySalesForAnnualSales(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, String customerCode);

    boolean submitPlannedMonthlySalesTargetSettingForDealers(MonthlySalesTargetSettingListData monthlySalesTargetSettingListData);
    boolean submitRevisedMonthlySalesTargetSettingForDealers(MonthlySalesTargetSettingListData monthlySalesTargetSettingListData);
    MonthlySalesModel viewMonthlySalesTargetForPlannedTab(String subArea, SclUserModel sclUser);
    List<List<Object>> viewMonthlyRevisedSalesTargetForReviewTab(String subArea, BaseSiteModel currentBaseSite, SclUserModel sclUser, String customerCode, Date firstDayOfMonth, Date lastDayOfMonth);
    boolean submitMonthlySalesTargetForReviewTab(MonthlySalesTargetSettingListData monthlySalesTargetSettingListData);
    List<List<Object>> getLastYearShareForProductMonthly(SclUserModel sclUser, BaseSiteModel baseSite);
    List<List<Object>> getLastYearShareForTargetMonthly(SclUserModel sclUser, BaseSiteModel baseSite);
    List<String> getLastYearShareForDealerTargetMonthly(String dealerCategory,SclUserModel sclUser, BaseSiteModel baseSite);

    boolean saveAnnualSalesTargetSettingForDealers(AnnualSalesTargetSettingListData annualSalesTargetSettingListData, SclUserModel sclUser, String subArea);

    List<List<Object>> fetchDealerDetailsForSelectedRetailer(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, String filter);

    List<List<Object>> fetchRetailerDetailsForSelectedDealer(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, String filter);

    List<List<Object>> fetchDealerCySalesForRetailer(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, String customerCode);

    DealerPlannedAnnualSalesModel fetchRecordForDealerPlannedAnnualSales(String subArea, SclUserModel sclUser, String filter);

    boolean saveMonthWiseDealersDetailsForAnnSales(AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData, String subArea);

    boolean saveAnnualSalesTargetSettingForRetailers(AnnualSalesTargetSettingData annualSalesTargetSettingData, String subArea);

    List<DealerRevisedAnnualSalesModel> fetchRecordForDealerRevisedAnnualSales(String subArea, SclUserModel sclUser, String financialYear);

    boolean saveMonthWiseRetailerForAnnSales(AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData, String subArea);

    boolean saveOnboardedDealersForAnnSales(AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData, String subArea);

    List<MonthWiseAnnualTargetModel> getMonthWiseAnnualTargetDetails(String customerCode, String productCode, String subarea);

    boolean saveOnboardedRetailerForAnnSales(AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData);

    boolean saveMonthlySalesTargetForDealers(MonthlySalesTargetSettingListData monthlySalesTargetSettingListData);

    boolean saveReviseMonthlySalesTargetForDealer(MonthlySalesTargetSettingListData monthlySalesTargetSettingListData);

    Double getCurrentYearSalesForAnnualSummary(SclUserModel sclUser, BaseSiteModel baseSite, String subarea);
    Double getCurrentYearSalesForAnnualSummaryNew(BaseSiteModel baseSite, String subarea);
    Double getCurrentYearSalesForAnnualSummaryForRH(DistrictMasterModel districtMasterModel,BaseSiteModel baseSite);


    boolean isDealerCustomerCodeExisting(final String customerCode, String subArea, SclUserModel sclUser, String nextFinancialYear);

    List<List<Object>> getLastYearShareForProductFromNCR(String subArea);
    List<List<Object>> getLastYearShareForProductFromNCRMonthly(String subArea);

    List<List<Object>> getLastYearShareForDealerFromNCRAnnual(String subarea);

    List<List<Object>> getLastYearShareForDealerFromNCRMonthly(String subarea);

    List<String> getStateWiseProductForSummaryPage(String state);
    List<String> getDealerCategoryForSummaryPage();

    List<List<Object>> viewDealerDetailsForRetailerAnnualSales(String subArea, SclUserModel sclUser, BaseSiteModel baseSite);

    Double fetchDealerCySalesForRetailerAnnualSales(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, String customerCode);

    Double getPlannedMonthSaleForMonthlySaleSummary(SclUserModel sclUser, BaseSiteModel baseSite, String subarea);

    RetailerPlannedAnnualSalesModel fetchRecordForRetailerPlannedAnnualSales(String subArea, SclUserModel sclUser, String filter);

    List<List<Object>> fetchProductSaleDetailsForSummary(String subArea, SclUserModel sclUser);

    Double getTotalTargetForDealers(SclUserModel sclUser, String subarea, String financialYear);

    Double getPlannedTargetAfterTargetSetMonthlySP(SclUserModel sclUser, String subArea);
    Double getRevisedTargetAfterTargetSetMonthlySP(SclUserModel sclUser, String subArea);

    Double getPlannedTargetForReviewMonthlySP(SclUserModel sclUser, String subArea,BaseSiteModel site);
    Double getRevisedTargetForReviewMonthlySP(SclUserModel sclUser, String subArea, BaseSiteModel site);
    Double getPlannedTargetForReviewMonthlySPForRH(DistrictMasterModel districtMasterModel);
    Double getRevisedTargetForReviewMonthlySPForRH(DistrictMasterModel districtMasterModel);
    List<List<Object>> fetchProductMixDetailsAfterTargetSetMonthlySummary(String subArea, SclUserModel sclUser, BaseSiteModel baseSite);
    List<List<Object>> fetchProductMixDetailsAfterTargetSetMonthlySummaryForRH(DistrictMasterModel districtMasterModel);
    List<List<Object>> fetchProductMixDetailsAfterTargetSetMonthlySummaryForSumRH(List<DistrictMasterModel> districtMasterModel);
    List<List<Object>> fetchProductMixDetailsAfterTargetSetMonthlySummaryForTSM(List<SubAreaMasterModel> subArea, BaseSiteModel currentBaseSite);

    List<List<Object>> fetchProductMixDetailsForReviewTargetMonthlySummary(String subArea, SclUserModel sclUser, BaseSiteModel baseSite);
    List<List<Object>> fetchProductMixDetailsForReviewTargetMonthlySummaryForRH(DistrictMasterModel districtMasterModel);
    List<List<Object>> fetchProductMixDetailsForReviewTargetMonthlySummaryForSumRH(List<DistrictMasterModel> districtMasterModel);
    List<List<Object>> fetchProductMixDetailsForReviewTargetMonthlySummaryForTSM(List<SubAreaMasterModel> subArea, BaseSiteModel currentBaseSite);

    List<MonthWiseAnnualTargetModel> getMonthWiseSkuDetailsForReview(String customerCode, String productCode, String subarea, SclUserModel currentUser);
    List<MonthWiseAnnualTargetModel> getMonthWiseRetailerDetailsForReview(String dealerCode, String retailerCode, String subarea, SclUserModel currentUser);

    Double getPlannedTargetAfterReviewMonthlySP(SclUserModel sclUser, String subarea,BaseSiteModel site);

    Double getRevisedTargetAfterReviewMonthlySP(SclUserModel sclUser, String subarea,BaseSiteModel site);
    Double getPlannedTargetAfterReviewMonthlySPForRH(DistrictMasterModel districtMasterModel);

    Double getRevisedTargetAfterReviewMonthlySPForRH(DistrictMasterModel districtMasterModel);

    List<DealerPlannedMonthlySalesModel> fetchDealerPlannedMonthlySalesDetails(String subArea, SclUserModel sclUser);

    List<DealerRevisedMonthlySalesModel> fetchDealerReviewedMonthlySalesDetails(String subArea, SclUserModel sclUser);

    List<SclCustomerModel> getDealerDetailsForOnboarded(String subArea, int intervalPeriod);
    List<SclCustomerModel> getRetailerDetailsForOnboarded(String subArea,int intervalPeriod);

    double getTotalTargetForDealersAfterTargetSetting(SclUserModel sclUser, String subArea, String nextFinancialYear, BaseSiteModel baseSite);
    double getTotalTargetForDealersAfterTargetSettingForRH(DistrictMasterModel districtMasterModel, String nextFinancialYear, BaseSiteModel baseSite);

    List<List<Object>> fetchProductSaleDetailsForSummaryAfterTargetSetting(String toString, SclUserModel sclUser, BaseSiteModel baseSite);
    List<List<Object>> fetchProductSaleDetailsForSummaryAfterTargetSettingForRH(DistrictMasterModel districtMasterModel, BaseSiteModel baseSite);
    List<List<Object>> fetchProductSaleDetailsForSummaryAfterTargetSettingForRH(List<DistrictMasterModel> districtMasterModel, BaseSiteModel baseSite);
    List<List<Object>> fetchProductSaleDetailsForSummaryAfterTargetSettingForTSM(List<SubAreaMasterModel> subAreaMasterModels, BaseSiteModel baseSite);
    double getTotalTargetForDealersAfterReview(SclUserModel sclUser, String toString, String nextFinancialYear, BaseSiteModel baseSite);
    double getTotalTargetForDealersAfterReviewForRH(DistrictMasterModel districtMasterModel, String nextFinancialYear, BaseSiteModel baseSite);

    List<List<Object>> fetchProductSaleDetailsForSummaryAfterReview(String toString, SclUserModel sclUser, BaseSiteModel baseSite);
    List<List<Object>> fetchProductSaleDetailsForSummaryAfterReviewForRH(DistrictMasterModel districtMasterModel, BaseSiteModel baseSite);
    List<List<Object>> fetchProductSaleDetailsForSummaryAfterReviewForRH(List<DistrictMasterModel> districtMasterModel, BaseSiteModel baseSite);
    List<List<Object>> fetchProductSaleDetailsForSummaryAfterReviewForTSM(List<SubAreaMasterModel> toString, BaseSiteModel baseSite);
    List<List<Object>> fetchProductSaleDetailsForSummaryAfterReviewForTSMRH(String toString, SclUserModel sclUser,String disctrictCode,String regionCode);

    double getPlannedMonthSaleForMonthlySaleSummary(SclUserModel sclUser, String currentMonthName, String subArea, BaseSiteModel baseSite);

    List<List<Object>> fetchDealerSaleDetailsForSummaryAfterTargetSetting(String toString, SclUserModel sclUser);
    List<List<Object>> fetchDealerSaleDetailsForSummaryAfterTargetSettingForRH(DistrictMasterModel districtMasterModel);
    List<List<Object>> fetchDealerSaleDetailsForSummaryAfterTargetSettingForTSMRH(String toString, SclUserModel sclUser,String distictCode,String regionCode);

    List<List<Object>> fetchDealerSaleDetailsForSummaryForSummaryAfterReview(String toString, SclUserModel sclUser, BaseSiteModel baseSite);
    List<List<Object>> fetchDealerSaleDetailsForSummaryForSummaryAfterReviewForRH(DistrictMasterModel districtMasterModel);
    List<List<Object>> fetchDealerSaleDetailsForSummaryForSummaryAfterReviewForTSMRH(String toString, SclUserModel sclUser,String districtCode,String regionCode);
    MonthlySalesModel getMonthlySalesModelDetail(SclUserModel sclUser, String subArea,BaseSiteModel brand);
    MonthlySalesModel getMonthlySalesModelDetailForDO(List<SubAreaMasterModel> subAreaList,BaseSiteModel brand);

    List<List<Object>> fetchDealerMixDetailsAfterTargetSetMonthlySummary(String subArea, SclUserModel sclUser);
    List<List<Object>> fetchDealerMixDetailsAfterTargetSetMonthlySummaryForRH(DistrictMasterModel districtMasterModel);
    List<List<Object>> fetchDealerMixDetailsAfterReviewMonthlySummary(String subArea, SclUserModel sclUser);

    List<List<Object>> fetchDealerMixDetailsAfterReviewMonthlySummaryForRH(DistrictMasterModel districtMasterModel);
    ProductSaleModel fetchProductSaleForDealerPlannedMonthlySales(String toString, SclUserModel sclUser, String code, String customerCode, String monthName, String monthYear);

    ProductSaleModel fetchProductSaleForDealerRevisedMonthlySales(String toString, SclUserModel sclUser, String code, String customerCode, String monthName, String monthYear);
    Double getMonthWiseAnnualTargetForDealer(SclUserModel sclUser, String dealerCode,  String monthYear, List<SubAreaMasterModel> subAreas);

    SearchPageData<AnnualSalesModel> viewPlannedSalesforDealersRetailersMonthWise(SearchPageData searchPageData,String subArea, SclUserModel sclUser, BaseSiteModel brand);
    SearchPageData<RetailerPlannedAnnualSalesModel> fetchRecordForRetailerPlannedAnnualSales(SearchPageData searchPageData,String subArea, SclUserModel sclUser, String filter);
    List<MonthWiseAnnualTargetModel> getMonthWiseSkuDetailsBeforeReview(String customerCode, String productCode, String subarea, SclUserModel currentUser);
    List<MonthWiseAnnualTargetModel> getMonthWiseRetailerDetailsBeforeReview(String dealerCustomerCode, String retailerCustCode, String subarea, SclUserModel currentUser);

    DealerRevisedAnnualSalesModel fetchRecordForDealerRevisedAnnualSalesByCode(String subArea, SclUserModel sclUser, String filter);

    List<MonthWiseAnnualTargetModel> getMonthWiseAnnualTargetDetailsForDealerRevised(String customerCode, String productCode, String subArea);

    double getRetailerCySale(String dealerCode, String retailerCode, String subArea);

    List<List<Object>> getRetailerDetailsByDealerCode(String toString, String customerCode);

    List<String> getRetailerListByDealerCode(String dealerCode, String s);

    SalesTargetApprovedData targetSendForRevision(SalesRevisedTargetData salesRevisedTargetData);

    SalesTargetApprovedData updateTargetStatusForApproval(SalesApprovalData salesApprovalData);
    boolean sendApprovedTargetToUser(boolean isTargetSetForUser);
    AnnualSalesModel getAnnualSalesModelDetailsForTSM(String financialYear, DistrictMasterModel districtMaster, BaseSiteModel brand);
    AnnualSalesModel getAnnualSalesModelDetailsForRH(String financialYear, RegionMasterModel regionMaster, BaseSiteModel brand);
    AnnualSalesModel getAnnualSalesModelDetailsForSH( String financialYear, List<SubAreaMasterModel> subArea, BaseSiteModel baseSite);
    AnnualSalesModel getAnnualSalesModelDetailsForSH_RH( String financialYear, List<DistrictMasterModel> subArea, BaseSiteModel baseSite);
    boolean updateStatusForBucketApproval(ViewBucketwiseRequest viewBucketwiseRequest);

    SalesTargetApprovedData updateTargetStatusForApprovalMonthly(SalesApprovalData salesApprovalData);

    SalesTargetApprovedData targetSendForRevisionForMonthly(SalesRevisedTargetData salesRevisedTargetData);

    List<MonthlySalesModel> getMonthlySalesModelDetailsListForDO(List<SubAreaMasterModel> taulkaForUser, BaseSiteModel currentBaseSite);
}