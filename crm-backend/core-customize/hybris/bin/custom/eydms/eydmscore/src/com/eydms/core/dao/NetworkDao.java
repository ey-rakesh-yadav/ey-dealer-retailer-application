package com.eydms.core.dao;

import com.eydms.core.enums.CustomerCategory;
import com.eydms.core.enums.LeadType;
import com.eydms.core.model.*;
import com.eydms.facades.data.ProposePlanListData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.Date;
import java.util.List;

public interface NetworkDao {
    NetworkAdditionPlanModel findNeworkPlanByTalukaAndLeadType(List<SubAreaMasterModel> taluka, String leadType, Date timestamp);
    ProspectiveNetworkModel findPerspectiveNetworkByCode(String code);
    List<NirmanMitraSalesHistoryModel> getMitraSalesDataForCustomer(String customerNo, BaseSiteModel brand, String transactionType );
    List<DealerVehicleDetailsModel> getAllVehicleDetails();
    List<EyDmsCustomerModel> getDealersForSalesPromoter(EyDmsCustomerModel promoter);
    List<EyDmsCustomerModel> getNetworkDealersForSalesPromoter(EyDmsCustomerModel promoter,
                                                             List<SubAreaMasterModel> subAreas);

    List<LeadMasterModel> getAllLeadsToDate(Date start, Date end, List<SubAreaMasterModel> subAreaList);
    
    NetworkRemovalModel getNetworkRemovalModelForCustomerCode(String code);

    List<List<Object>>
    getLatitudeLongitudeOfProspectiveNetworkList(String customerCode);

    List<EyDmsCustomerModel> getInActiveCustomers(String dealerUserGroupUid,BaseSiteModel brand,List<SubAreaMasterModel> subAreas);

    CounterVisitMasterModel getVisitIdByEyDmsCustomer(String customercode);

    List<EyDmsCustomerModel> getOnboardedCustomerTillDate(String groupForLead, String taluka, Date timestamp,BaseSiteModel brand);
    double getCustomerTargetForDealer(String customerNo, String subArea, String month, String year);

    List<List<Object>> getCounterLocationDetails(String dealerCode);

    List<NetworkRemovalModel> getNetworkRemovalForSubArea(List<SubAreaMasterModel> subareaMaster, BaseSiteModel site);

    Double getSalesHistoryData(BaseSiteModel brand, String transactionType, int month, int year);

    //List<List<Object>> getSalesHistoryDataForDealer(EyDmsUserModel eydmsUser,BaseSiteModel brand, Date startDate, Date endDate);

    Double getActualTargetForSalesYTD(EyDmsUserModel eydmsUser, BaseSiteModel site, Date startDate, Date endDate);

    Double getSalesHistoryDataForDealer(BaseSiteModel brand, CustomerCategory category, int month, int year);

    //List<List<Object>> getSalesHistoryDataForDealerTaluka(EyDmsUserModel eydmsUser,BaseSiteModel brand,  Date startDate, Date endDate, String taluka);

    Double getSalesHistoryDataForDealerTaluka(BaseSiteModel brand, CustomerCategory category, int month, int year,String taluka);
    Double getSalesTarget(String customerType, int monthValue, int year);

    Double getSalesTargetForTaluka(String taluka,String customerType, int monthValue, int year);

    double getSalesHistoryDataForTaluka(String taluka,BaseSiteModel brand, String transactionType, int month, int year);
    Integer getNewRetailerCountMTD(EyDmsCustomerModel eydmsCustomer, BaseSiteModel baseSite, Date startDate, Date endDate, Date doj);
    Integer getNewInfluencerCountMTD(EyDmsCustomerModel eydmsCustomer, BaseSiteModel baseSite,Date startDate, Date endDate,Date doj,String fromCustomerType);
    List<EyDmsCustomerModel>  getRetailerCardCountMTD(EyDmsCustomerModel eydmsCustomer, BaseSiteModel baseSite,Date startDate, Date endDate);
    List<EyDmsCustomerModel>  getInfluencerCardCountMTD(EyDmsCustomerModel eydmsCustomer, BaseSiteModel baseSite,Date startDate, Date endDate,String fromCustomerType);

    Double getActualTargetForSalesYTDTaluka(EyDmsUserModel salesOfficer, BaseSiteModel site, Date startDate, Date endDate, String taluka);

    List<NetworkAdditionPlanModel> getNetworkAdditionPlanSummary(LeadType leadType,List<EyDmsUserModel> soForUser);

    List<NetworkAdditionPlanModel> getProposedPlansBySO(LeadType leadType, List<EyDmsUserModel> soForUser,String filter);

    Double getSalesQuantityForRetailerMTD(EyDmsCustomerModel customerNo, BaseSiteModel currentBaseSite);
    Double getSalesQuantityForRetailerMonthYear(EyDmsCustomerModel customerNo, BaseSiteModel currentBaseSite,int month,int year) ;


    NetworkAdditionPlanModel getNetworkAdditionPlan(EyDmsUserModel eydmsUserModel);

    NetworkAdditionPlanModel getNetworkAdditionPlan(String id);
    Double getSalesQuantityForInfluencerMTD(EyDmsCustomerModel customerNo, BaseSiteModel currentBaseSite);
    Double getSalesQuantityForInfluencerMonthYear(EyDmsCustomerModel customerNo, BaseSiteModel currentBaseSite,int month,int year) ;

    NetworkAdditionPlanModel getProposedPlanViewForTSMRH(String status, String id);

    SubAreaMasterModel getSubareaForSOString(String subArea);

    SearchPageData<NetworkAdditionPlanModel> getNetworkAdditionPlanSummaryForTSMRH(SearchPageData searchPageData, LeadType leadType,List<String> statuses,boolean isPendingForApproval,List<EyDmsUserModel> soForUser, List<EyDmsUserModel> tsmForUser,EyDmsUserModel currentUser);


    LeadMasterModel findItemByUidParam(String leadId);

    List<NetworkAdditionPlanModel> getCountOfProposedPlanSummaryListForRH(LeadType leadType, List<EyDmsUserModel> tsmForUser);

    List<NetworkAdditionPlanModel> getNetworkAdditionSummaryForRH(LeadType leadType);

    Integer getApprovedAdditionSumForTSMRH(LeadType leadType,List<EyDmsUserModel> soForUser, List<EyDmsUserModel> tsmForUser, EyDmsUserModel currentUser);

    Integer getLeadsGeneratedCountForInfluencerMtd(EyDmsCustomerModel currentUser, BaseSiteModel brand);

    Integer getLeadsGeneratedCountForInfluencerYtd(EyDmsCustomerModel currentUser, BaseSiteModel brand, Date startDate, Date endDate);
}
