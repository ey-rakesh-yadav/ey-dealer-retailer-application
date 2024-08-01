package com.scl.core.dao;

import com.scl.core.enums.CustomerCategory;
import com.scl.core.enums.LeadType;
import com.scl.core.model.*;
import com.scl.facades.data.ProposePlanListData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface NetworkDao {
    NetworkAdditionPlanModel findNeworkPlanByTalukaAndLeadType(List<SubAreaMasterModel> taluka, String leadType, Date timestamp);
    ProspectiveNetworkModel findPerspectiveNetworkByCode(String code);
    List<NirmanMitraSalesHistoryModel> getMitraSalesDataForCustomer(String customerNo, BaseSiteModel brand, String transactionType );
    List<DealerVehicleDetailsModel> getAllVehicleDetails();
    List<SclCustomerModel> getDealersForSalesPromoter(SclCustomerModel promoter);
    List<SclCustomerModel> getNetworkDealersForSalesPromoter(SclCustomerModel promoter,
                                                             List<SubAreaMasterModel> subAreas);

    List<LeadMasterModel> getAllLeadsToDate(Date start, Date end, List<SubAreaMasterModel> subAreaList);


    List<LeadMasterModel> getAllLeadsToDates(Date start, Date end,  List<SubAreaMasterModel> subAreaList);
    
    NetworkRemovalModel getNetworkRemovalModelForCustomerCode(String code);

    List<List<Object>>
    getLatitudeLongitudeOfProspectiveNetworkList(String customerCode);

    List<SclCustomerModel> getInActiveCustomers(String dealerUserGroupUid,BaseSiteModel brand,List<SubAreaMasterModel> subAreas);

    CounterVisitMasterModel getVisitIdBySclCustomer(String customercode);

    List<SclCustomerModel> getOnboardedCustomerTillDate(String groupForLead, String taluka, Date timestamp,BaseSiteModel brand);
    double getCustomerTargetForDealer(String customerNo, String subArea, String month, String year);

    List<List<Object>> getCounterLocationDetails(String dealerCode);

    List<NetworkRemovalModel> getNetworkRemovalForSubArea(List<SubAreaMasterModel> subareaMaster, BaseSiteModel site);

    Double getSalesHistoryData(BaseSiteModel brand, String transactionType, int month, int year);

    //List<List<Object>> getSalesHistoryDataForDealer(SclUserModel sclUser,BaseSiteModel brand, Date startDate, Date endDate);

    Double getActualTargetForSalesYTD(SclUserModel sclUser, BaseSiteModel site, Date startDate, Date endDate);

    Double getSalesHistoryDataForDealer(BaseSiteModel brand, CustomerCategory category, int month, int year);

    //List<List<Object>> getSalesHistoryDataForDealerTaluka(SclUserModel sclUser,BaseSiteModel brand,  Date startDate, Date endDate, String taluka);

    Double getSalesHistoryDataForDealerTaluka(BaseSiteModel brand, CustomerCategory category, int month, int year,String taluka);
    Double getSalesTarget(String customerType, int monthValue, int year);

    Double getSalesTargetForTaluka(String taluka,String customerType, int monthValue, int year);

    double getSalesHistoryDataForTaluka(String taluka,BaseSiteModel brand, String transactionType, int month, int year);
    Integer getNewRetailerCountMTD(SclCustomerModel sclCustomer, BaseSiteModel baseSite, Date startDate, Date endDate, Date doj);
    Integer getNewInfluencerCountMTD(SclCustomerModel sclCustomer, BaseSiteModel baseSite,Date startDate, Date endDate,Date doj,String fromCustomerType);
    List<SclCustomerModel>  getRetailerCardCountMTD(SclCustomerModel sclCustomer, BaseSiteModel baseSite,Date startDate, Date endDate);
    List<SclCustomerModel>  getInfluencerCardCountMTD(SclCustomerModel sclCustomer, BaseSiteModel baseSite,Date startDate, Date endDate,String fromCustomerType);

    Double getActualTargetForSalesYTDTaluka(SclUserModel salesOfficer, BaseSiteModel site, Date startDate, Date endDate, String taluka);

    List<NetworkAdditionPlanModel> getNetworkAdditionPlanSummary(LeadType leadType,List<SclUserModel> soForUser);

    List<NetworkAdditionPlanModel> getProposedPlansBySO(LeadType leadType, List<SclUserModel> soForUser,String filter);

    Double getSalesQuantityForRetailerMTD(SclCustomerModel customerNo, BaseSiteModel currentBaseSite);
    Double getSalesQuantityForRetailerMonthYear(SclCustomerModel customerNo, BaseSiteModel currentBaseSite,int month,int year,List<String> subAreaList,List<String> districtList) ;
    Map<String,Double> getSalesQuantityForRetailerByMonth(SclCustomerModel sclReailer, String startDate, String endDate, List<SclCustomerModel> sclDealer, String product) ;
    Map<String,Double> getSalesQuantityForRetailerByMonth(List<SclCustomerModel> sclReailer, String startDate, String endDate, List<SclCustomerModel> sclDealer, String product) ;

    NetworkAdditionPlanModel getNetworkAdditionPlan(SclUserModel sclUserModel);

    NetworkAdditionPlanModel getNetworkAdditionPlan(String id);
    Double getSalesQuantityForInfluencerMTD(SclCustomerModel customerNo, BaseSiteModel currentBaseSite);
    Double getSalesQuantityForInfluencerMonthYear(SclCustomerModel customerNo, BaseSiteModel currentBaseSite,int month,int year,List<String> subAreaList,List<String> districtList) ;

    NetworkAdditionPlanModel getProposedPlanViewForTSMRH(String status, String id);

    SubAreaMasterModel getSubareaForSOString(String subArea);

    SearchPageData<NetworkAdditionPlanModel> getNetworkAdditionPlanSummaryForTSMRH(SearchPageData searchPageData, LeadType leadType,List<String> statuses,boolean isPendingForApproval,List<SclUserModel> soForUser, List<SclUserModel> tsmForUser,SclUserModel currentUser);


    LeadMasterModel findItemByUidParam(String leadId);

    List<NetworkAdditionPlanModel> getCountOfProposedPlanSummaryListForRH(LeadType leadType, List<SclUserModel> tsmForUser);

    List<NetworkAdditionPlanModel> getNetworkAdditionSummaryForRH(LeadType leadType);

    Integer getApprovedAdditionSumForTSMRH(LeadType leadType,List<SclUserModel> soForUser, List<SclUserModel> tsmForUser, SclUserModel currentUser);

    Integer getLeadsGeneratedCountForInfluencerMtd(SclCustomerModel currentUser, BaseSiteModel brand);

    Integer getLeadsGeneratedCountForInfluencerYtd(SclCustomerModel currentUser, BaseSiteModel brand, Date startDate, Date endDate);
    List<List<Object>> getOrderReqSalesForRetailer(List<SclCustomerModel> sclReailer, SclCustomerModel dealer, String startDate, String endDate);
    List<List<Object>> getMasterSalesForRetailer(List<SclCustomerModel> sclReailer, SclCustomerModel dealer, String startDate, String endDate);
    List<List<Object>> getSalesForRetailerList(List<SclCustomerModel> sclRetailer, SclCustomerModel dealer, String startDate, String endDate);
}
