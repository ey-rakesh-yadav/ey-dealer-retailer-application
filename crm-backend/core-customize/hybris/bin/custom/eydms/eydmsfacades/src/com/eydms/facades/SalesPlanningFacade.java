package com.eydms.facades;

import com.eydms.facades.data.*;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;

import java.util.List;

public interface SalesPlanningFacade {
    ErrorListWsDTO submitAnnualSalesTargetSettingForDealers(AnnualSalesTargetSettingListData data);
    ErrorListWsDTO submitAnnualSalesTargetSettingForRetailers(AnnualSalesTargetSettingListData data);
    //boolean submitFinalizeAnnualSalesTargetSettingForDealers(AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData);
    ErrorListWsDTO submitFinalizeAnnualSalesTargetSettingForDealers(AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData);
    ErrorListWsDTO submitFinalizeAnnualSalesTargetSettingForRetailers(AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData);
    boolean submitOnboardedAnnualSalesTargetSettingForDealers(AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData);
    boolean submitOnboardedAnnualSalesTargetSettingForRetailers(AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData);
    AnnualSalesMonthWiseTargetListData viewPlannedSalesforDealersMonthwise(String subArea, String filter);
    AnnualSalesMonthWiseTargetListData viewPlannedSalesforRetailerMonthwise(String subArea, String filter);
    AnnualTargetSettingSummaryData viewAnnualSalesSummary(boolean isAnnualSummaryTargetSet, boolean isAnnualSummaryAfterTargetSetting, boolean isAnnualSummaryForReview, boolean isAnnualSummaryAfterReview);
    AnnualTargetSettingSummaryData viewAnnualSalesSummaryTSM(boolean isAnnualSummaryForReview, boolean isAnnualSummaryAfterReview);
    AnnualTargetSettingSummaryData viewAnnualSalesSummaryRH(boolean isAnnualSummaryForReview, boolean isAnnualSummaryAfterReview);
    SalesPlanningMonthYearDueDateData showMonthYearDueDateForTSMRH();
    ViewBucketwiseRequestList viewBucketwiseRequestForTSMRH(String searchKey);
    ViewBucketwiseRequest monthwiseSummaryForTSM();
    AnnualSalesMonthWiseTargetListData viewReviewedSalesforDealersMonthwise(String subArea, String filter);
    AnnualSalesMonthWiseTargetListData viewReviewedSalesforRetailesMonthwise(String subArea, String filter);
    AnnualSalesTargetSettingListData viewDealerDetailsForAnnualSales(String subArea, String filter);
    AnnualSalesTargetSettingListData viewRetailerDetailsForAnnualSales(String subArea, String filter);
    AnnualSalesMonthWiseTargetListData viewMonthWiseDealerDetailsForAnnualSales(String subArea, String filter);
    AnnualSalesMonthWiseTargetListData viewMonthWiseRetailerDetailsForAnnualSales(String subArea, String filter);
    List<RetailerDetailsData> getRetailerList(String subArea, String dealerCode);
    MonthlySalesTargetSettingListData viewMonthlySalesTargetForDealers(String subArea);
    boolean submitPlannedMonthlySalesTargetSettingForDealers(MonthlySalesTargetSettingListData monthlySalesTargetSettingListData);
    boolean submitRevisedMonthlySalesTargetSettingForDealers(MonthlySalesTargetSettingListData monthlySalesTargetSettingListData);
    MonthlySalesTargetSettingListData viewMonthlySalesTargetForPlannedTab(String subArea);
    MonthlySalesTargetSettingListData viewMonthlyRevisedSalesTargetForRevisedTarget(String subArea);
    MonthlySalesTargetSettingListData viewMonthlyRevisedSalesTargetForReviewTab(String subArea);
    MonthlySalesTargetSettingListData viewMonthlyRevisedSalesTargetForReviewTabForTSM(String subArea);
    MonthlySalesTargetSettingListData viewMonthlyRevisedSalesTargetForReviewTabForRH(String subArea);
    boolean submitMonthlySalesTargetForReviewTab(MonthlySalesTargetSettingListData monthlySalesTargetSettingListData);
    MonthlyTargetSettingSummaryData viewMonthlySalesSummary(boolean isMonthlySummaryAfterSubmitPlanned, boolean isMonthlySummaryForReview, boolean isMonthlySummaryAfterSubmitRevised, boolean isMonthlySummaryAfterSubmitReviewed);
    boolean saveAnnualSalesTargetSettingForDealers(AnnualSalesTargetSettingListData annualSalesTargetSettingListData, String subArea);
    AnnualSalesTargetSettingListData viewSavedAnnualSalesTargetSettingForDealers(String subArea);
    boolean saveAnnualSalesTargetSettingForRetailers(AnnualSalesTargetSettingData annualSalesTargetSettingData, String subArea);
    boolean saveMonthWiseDealersDetailsForAnnSales(AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData, String subArea);
    boolean saveMonthWiseRetailerForAnnSales(AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData, String subArea);
    boolean saveOnboardedDealersForAnnSales(AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData, String subArea);
    boolean saveOnboardedRetailerForAnnSales(AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData);
    boolean saveMonthlySalesTargetForDealers(MonthlySalesTargetSettingListData monthlySalesTargetSettingListData);
    boolean saveReviseMonthlySalesTargetForDealer(MonthlySalesTargetSettingListData monthlySalesTargetSettingListData);
    MonthlySalesTargetSettingListData viewSavedMonthSalesTargetSetForDealers(String subArea);
    MonthlySalesTargetSettingListData viewSavedRevMonthSalesTargetSetForDealers(String subArea);
    //Pagination
    SearchPageData<AnnualSalesMonthWiseTargetData> viewMonthWiseRetailerDetailsForAnnualSalesPagination(SearchPageData searchPageData, String subArea, String filter);
    SearchPageData<AnnualSalesMonthWiseTargetData> viewPlannedSalesforRetailerMonthwise(SearchPageData searchPageData, String subArea, String filter);
    SearchPageData<AnnualSalesTargetSettingData> viewRetailerDetailsForAnnualSalesWithPagination(SearchPageData<Object> searchPageData, String subArea, String filter);
    public List<Double> getTotalPlanAndCySalesRetailerPlanned(SearchPageData searchPageData,String subArea);
    public List<Double> getTotalPlanAndCySalesRetailerRevised(SearchPageData searchPageData,String subArea);
    SearchPageData<AnnualSalesMonthWiseTargetData> viewReviewedSalesforRetailesMonthwise(SearchPageData searchPageData,String subArea, String filter);
    SearchPageData<SpOnboardAnnualTargetSettingData> viewReviewedSalesforRetailesMonthwiseOnboarded(SearchPageData searchPageData,String subArea, String filter);

    SalesHighPriorityActionData sendAlertForSalesPlanHighPriorityAction();

    SalesTargetApprovedData targetSendForRevision(SalesRevisedTargetData salesRevisedTargetData);

    SalesTargetApprovedData updateTargetStatusForApproval(SalesApprovalData salesApprovalData);

    AnnualSalesMonthWiseTargetListData reviewMonthwiseTargetsForDealer(String subArea, String filter);
    MonthlyTargetSettingSummaryData viewMonthlySalesSummaryForTSM(boolean isMonthlySummaryForReview,boolean isMonthlySummaryAfterSubmitReviewed);
    MonthlyTargetSettingSummaryData viewMonthlySalesSummaryForRH(boolean isMonthlySummaryForReview,boolean isMonthlySummaryAfterSubmitReviewed);

    AnnualSalesReviewListData reviewAnnualSalesMonthwiseTargetsForRH(String district, String filter);
    boolean sendApprovedTargetToUser(boolean isTargetSetForUser);
    SalesTargetApprovedData updateStatusForBucketApproval(ViewBucketwiseRequest viewBucketwiseRequest);

    SalesTargetApprovedData targetSendForRevisionForMonthly(SalesRevisedTargetData salesRevisedTargetData);

    SalesTargetApprovedData updateTargetStatusForApprovalMonthly(SalesApprovalData salesApprovalData);
}
