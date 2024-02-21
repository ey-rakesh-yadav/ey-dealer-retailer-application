package com.eydms.core.services;

import com.eydms.core.model.*;
import com.eydms.facades.data.*;
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
    AnnualSalesModel viewPlannedSalesforDealersRetailersMonthWise(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel brand);
    List<List<Object>> getLastYearShareForProduct(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, String s);
    List<List<Object>> getLastYearShareForTarget(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, String s);
    List<String> getLastYearShareForDealerTarget(String dealerCategory,EyDmsUserModel eydmsUser, BaseSiteModel baseSite);

    List<EyDmsCustomerModel> getDealerDetailsForOnboarded(String subArea, int intervalPeriod, String filter);
    List<EyDmsCustomerModel> getRetailerDetailsForOnboarded(String subArea,int intervalPeriod, String filter);

    List<List<Object>> viewDealerDetailsForAnnualSales(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel baseSite);
    List<List<Object>> viewDealerDetailsForAnnualSales(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel baseSite,String filter);
    List<List<Object>> viewRetailerDetailsForAnnualSales(String customerCode, String subArea, EyDmsUserModel eydmsUser, BaseSiteModel baseSite);
    List<List<Object>> viewMonthWiseDealerDetailsForAnnualSales(BaseSiteModel baseSite, EyDmsUserModel eydmsUser, String subArea);
    List<List<Object>> viewMonthWiseRetailerDetailsForAnnualSales(BaseSiteModel baseSite, EyDmsUserModel eydmsUser, String subArea);

    DealerRevisedAnnualSalesModel viewMonthlySalesTargetForDealers(String subArea, EyDmsUserModel eydmsUser);

    List<List<Object>> fetchDealerCySalesForAnnualSales(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel baseSite, String customerCode);

    boolean submitPlannedMonthlySalesTargetSettingForDealers(MonthlySalesTargetSettingListData monthlySalesTargetSettingListData);
    boolean submitRevisedMonthlySalesTargetSettingForDealers(MonthlySalesTargetSettingListData monthlySalesTargetSettingListData);
    MonthlySalesModel viewMonthlySalesTargetForPlannedTab(String subArea, EyDmsUserModel eydmsUser);
    List<List<Object>> viewMonthlyRevisedSalesTargetForReviewTab(String subArea, BaseSiteModel currentBaseSite, EyDmsUserModel eydmsUser, String customerCode, Date firstDayOfMonth, Date lastDayOfMonth);
    boolean submitMonthlySalesTargetForReviewTab(MonthlySalesTargetSettingListData monthlySalesTargetSettingListData);
    List<List<Object>> getLastYearShareForProductMonthly(EyDmsUserModel eydmsUser, BaseSiteModel baseSite);
    List<List<Object>> getLastYearShareForTargetMonthly(EyDmsUserModel eydmsUser, BaseSiteModel baseSite);
    List<String> getLastYearShareForDealerTargetMonthly(String dealerCategory,EyDmsUserModel eydmsUser, BaseSiteModel baseSite);

    boolean saveAnnualSalesTargetSettingForDealers(AnnualSalesTargetSettingListData annualSalesTargetSettingListData, EyDmsUserModel eydmsUser, String subArea);

    List<List<Object>> fetchDealerDetailsForSelectedRetailer(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel baseSite, String filter);

    List<List<Object>> fetchRetailerDetailsForSelectedDealer(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel baseSite, String filter);

    List<List<Object>> fetchDealerCySalesForRetailer(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel baseSite, String customerCode);

    DealerPlannedAnnualSalesModel fetchRecordForDealerPlannedAnnualSales(String subArea, EyDmsUserModel eydmsUser, String filter);

    boolean saveMonthWiseDealersDetailsForAnnSales(AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData, String subArea);

    boolean saveAnnualSalesTargetSettingForRetailers(AnnualSalesTargetSettingData annualSalesTargetSettingData, String subArea);

    List<DealerRevisedAnnualSalesModel> fetchRecordForDealerRevisedAnnualSales(String subArea, EyDmsUserModel eydmsUser, String financialYear);

    boolean saveMonthWiseRetailerForAnnSales(AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData, String subArea);

    boolean saveOnboardedDealersForAnnSales(AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData, String subArea);

    List<MonthWiseAnnualTargetModel> getMonthWiseAnnualTargetDetails(String customerCode, String productCode, String subarea);

    boolean saveOnboardedRetailerForAnnSales(AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData);

    boolean saveMonthlySalesTargetForDealers(MonthlySalesTargetSettingListData monthlySalesTargetSettingListData);

    boolean saveReviseMonthlySalesTargetForDealer(MonthlySalesTargetSettingListData monthlySalesTargetSettingListData);

    Double getCurrentYearSalesForAnnualSummary(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, String subarea);
    Double getCurrentYearSalesForAnnualSummaryNew(BaseSiteModel baseSite, String subarea);
    Double getCurrentYearSalesForAnnualSummaryForRH(DistrictMasterModel districtMasterModel,BaseSiteModel baseSite);


    boolean isDealerCustomerCodeExisting(final String customerCode, String subArea, EyDmsUserModel eydmsUser, String nextFinancialYear);

    List<List<Object>> getLastYearShareForProductFromNCR(String subArea);
    List<List<Object>> getLastYearShareForProductFromNCRMonthly(String subArea);

    List<List<Object>> getLastYearShareForDealerFromNCRAnnual(String subarea);

    List<List<Object>> getLastYearShareForDealerFromNCRMonthly(String subarea);

    List<String> getStateWiseProductForSummaryPage(String state);
    List<String> getDealerCategoryForSummaryPage();

    List<List<Object>> viewDealerDetailsForRetailerAnnualSales(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel baseSite);

    Double fetchDealerCySalesForRetailerAnnualSales(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel baseSite, String customerCode);

    Double getPlannedMonthSaleForMonthlySaleSummary(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, String subarea);

    RetailerPlannedAnnualSalesModel fetchRecordForRetailerPlannedAnnualSales(String subArea, EyDmsUserModel eydmsUser, String filter);

    List<List<Object>> fetchProductSaleDetailsForSummary(String subArea, EyDmsUserModel eydmsUser);

    Double getTotalTargetForDealers(EyDmsUserModel eydmsUser, String subarea, String financialYear);

    Double getPlannedTargetAfterTargetSetMonthlySP(EyDmsUserModel eydmsUser, String subArea);
    Double getRevisedTargetAfterTargetSetMonthlySP(EyDmsUserModel eydmsUser, String subArea);

    Double getPlannedTargetForReviewMonthlySP(EyDmsUserModel eydmsUser, String subArea,BaseSiteModel site);
    Double getRevisedTargetForReviewMonthlySP(EyDmsUserModel eydmsUser, String subArea, BaseSiteModel site);
    Double getPlannedTargetForReviewMonthlySPForRH(DistrictMasterModel districtMasterModel);
    Double getRevisedTargetForReviewMonthlySPForRH(DistrictMasterModel districtMasterModel);
    List<List<Object>> fetchProductMixDetailsAfterTargetSetMonthlySummary(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel baseSite);
    List<List<Object>> fetchProductMixDetailsAfterTargetSetMonthlySummaryForRH(DistrictMasterModel districtMasterModel);
    List<List<Object>> fetchProductMixDetailsAfterTargetSetMonthlySummaryForSumRH(List<DistrictMasterModel> districtMasterModel);
    List<List<Object>> fetchProductMixDetailsAfterTargetSetMonthlySummaryForTSM(List<SubAreaMasterModel> subArea, BaseSiteModel currentBaseSite);

    List<List<Object>> fetchProductMixDetailsForReviewTargetMonthlySummary(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel baseSite);
    List<List<Object>> fetchProductMixDetailsForReviewTargetMonthlySummaryForRH(DistrictMasterModel districtMasterModel);
    List<List<Object>> fetchProductMixDetailsForReviewTargetMonthlySummaryForSumRH(List<DistrictMasterModel> districtMasterModel);
    List<List<Object>> fetchProductMixDetailsForReviewTargetMonthlySummaryForTSM(List<SubAreaMasterModel> subArea, BaseSiteModel currentBaseSite);

    List<MonthWiseAnnualTargetModel> getMonthWiseSkuDetailsForReview(String customerCode, String productCode, String subarea, EyDmsUserModel currentUser);
    List<MonthWiseAnnualTargetModel> getMonthWiseRetailerDetailsForReview(String dealerCode, String retailerCode, String subarea, EyDmsUserModel currentUser);

    Double getPlannedTargetAfterReviewMonthlySP(EyDmsUserModel eydmsUser, String subarea,BaseSiteModel site);

    Double getRevisedTargetAfterReviewMonthlySP(EyDmsUserModel eydmsUser, String subarea,BaseSiteModel site);
    Double getPlannedTargetAfterReviewMonthlySPForRH(DistrictMasterModel districtMasterModel);

    Double getRevisedTargetAfterReviewMonthlySPForRH(DistrictMasterModel districtMasterModel);

    List<DealerPlannedMonthlySalesModel> fetchDealerPlannedMonthlySalesDetails(String subArea, EyDmsUserModel eydmsUser);

    List<DealerRevisedMonthlySalesModel> fetchDealerReviewedMonthlySalesDetails(String subArea, EyDmsUserModel eydmsUser);

    List<EyDmsCustomerModel> getDealerDetailsForOnboarded(String subArea, int intervalPeriod);
    List<EyDmsCustomerModel> getRetailerDetailsForOnboarded(String subArea,int intervalPeriod);

    double getTotalTargetForDealersAfterTargetSetting(EyDmsUserModel eydmsUser, String subArea, String nextFinancialYear, BaseSiteModel baseSite);
    double getTotalTargetForDealersAfterTargetSettingForRH(DistrictMasterModel districtMasterModel, String nextFinancialYear, BaseSiteModel baseSite);

    List<List<Object>> fetchProductSaleDetailsForSummaryAfterTargetSetting(String toString, EyDmsUserModel eydmsUser, BaseSiteModel baseSite);
    List<List<Object>> fetchProductSaleDetailsForSummaryAfterTargetSettingForRH(DistrictMasterModel districtMasterModel, BaseSiteModel baseSite);
    List<List<Object>> fetchProductSaleDetailsForSummaryAfterTargetSettingForRH(List<DistrictMasterModel> districtMasterModel, BaseSiteModel baseSite);
    List<List<Object>> fetchProductSaleDetailsForSummaryAfterTargetSettingForTSM(List<SubAreaMasterModel> subAreaMasterModels, BaseSiteModel baseSite);
    double getTotalTargetForDealersAfterReview(EyDmsUserModel eydmsUser, String toString, String nextFinancialYear, BaseSiteModel baseSite);
    double getTotalTargetForDealersAfterReviewForRH(DistrictMasterModel districtMasterModel, String nextFinancialYear, BaseSiteModel baseSite);

    List<List<Object>> fetchProductSaleDetailsForSummaryAfterReview(String toString, EyDmsUserModel eydmsUser, BaseSiteModel baseSite);
    List<List<Object>> fetchProductSaleDetailsForSummaryAfterReviewForRH(DistrictMasterModel districtMasterModel, BaseSiteModel baseSite);
    List<List<Object>> fetchProductSaleDetailsForSummaryAfterReviewForRH(List<DistrictMasterModel> districtMasterModel, BaseSiteModel baseSite);
    List<List<Object>> fetchProductSaleDetailsForSummaryAfterReviewForTSM(List<SubAreaMasterModel> toString, BaseSiteModel baseSite);
    List<List<Object>> fetchProductSaleDetailsForSummaryAfterReviewForTSMRH(String toString, EyDmsUserModel eydmsUser,String disctrictCode,String regionCode);

    double getPlannedMonthSaleForMonthlySaleSummary(EyDmsUserModel eydmsUser, String currentMonthName, String subArea, BaseSiteModel baseSite);

    List<List<Object>> fetchDealerSaleDetailsForSummaryAfterTargetSetting(String toString, EyDmsUserModel eydmsUser);
    List<List<Object>> fetchDealerSaleDetailsForSummaryAfterTargetSettingForRH(DistrictMasterModel districtMasterModel);
    List<List<Object>> fetchDealerSaleDetailsForSummaryAfterTargetSettingForTSMRH(String toString, EyDmsUserModel eydmsUser,String distictCode,String regionCode);

    List<List<Object>> fetchDealerSaleDetailsForSummaryForSummaryAfterReview(String toString, EyDmsUserModel eydmsUser, BaseSiteModel baseSite);
    List<List<Object>> fetchDealerSaleDetailsForSummaryForSummaryAfterReviewForRH(DistrictMasterModel districtMasterModel);
    List<List<Object>> fetchDealerSaleDetailsForSummaryForSummaryAfterReviewForTSMRH(String toString, EyDmsUserModel eydmsUser,String districtCode,String regionCode);
    MonthlySalesModel getMonthlySalesModelDetail(EyDmsUserModel eydmsUser, String subArea,BaseSiteModel brand);
    MonthlySalesModel getMonthlySalesModelDetailForDO(List<SubAreaMasterModel> subAreaList,BaseSiteModel brand);

    List<List<Object>> fetchDealerMixDetailsAfterTargetSetMonthlySummary(String subArea, EyDmsUserModel eydmsUser);
    List<List<Object>> fetchDealerMixDetailsAfterTargetSetMonthlySummaryForRH(DistrictMasterModel districtMasterModel);
    List<List<Object>> fetchDealerMixDetailsAfterReviewMonthlySummary(String subArea, EyDmsUserModel eydmsUser);

    List<List<Object>> fetchDealerMixDetailsAfterReviewMonthlySummaryForRH(DistrictMasterModel districtMasterModel);
    ProductSaleModel fetchProductSaleForDealerPlannedMonthlySales(String toString, EyDmsUserModel eydmsUser, String code, String customerCode, String monthName, String monthYear);

    ProductSaleModel fetchProductSaleForDealerRevisedMonthlySales(String toString, EyDmsUserModel eydmsUser, String code, String customerCode, String monthName, String monthYear);
    Double getMonthWiseAnnualTargetForDealer(EyDmsUserModel eydmsUser, String dealerCode,  String monthYear, List<SubAreaMasterModel> subAreas);

    SearchPageData<AnnualSalesModel> viewPlannedSalesforDealersRetailersMonthWise(SearchPageData searchPageData,String subArea, EyDmsUserModel eydmsUser, BaseSiteModel brand);
    SearchPageData<RetailerPlannedAnnualSalesModel> fetchRecordForRetailerPlannedAnnualSales(SearchPageData searchPageData,String subArea, EyDmsUserModel eydmsUser, String filter);
    List<MonthWiseAnnualTargetModel> getMonthWiseSkuDetailsBeforeReview(String customerCode, String productCode, String subarea, EyDmsUserModel currentUser);
    List<MonthWiseAnnualTargetModel> getMonthWiseRetailerDetailsBeforeReview(String dealerCustomerCode, String retailerCustCode, String subarea, EyDmsUserModel currentUser);

    DealerRevisedAnnualSalesModel fetchRecordForDealerRevisedAnnualSalesByCode(String subArea, EyDmsUserModel eydmsUser, String filter);

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