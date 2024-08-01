package com.scl.core.services;

import com.scl.core.enums.LeadType;
import com.scl.core.enums.PartnerLevel;
import com.scl.core.model.*;
import com.scl.facades.data.*;
import com.scl.facades.visit.data.SiteSummaryData;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface NetworkService {
    DealerCurrentNetworkListData getDealerCurrentNetworkWsData(String dealerCategory , String fields, String networkType, BaseSiteModel site, String leadType, boolean sclExclusiveCustomer, String searchKey, List<String> doList,
                                                               List<String> subAreaList, List<String> territoryList);
    ProspectiveNetworkModel findNetworkByCode(String code);
    List<NirmanMitraSalesHistoryModel> getMitraSalesDataForCustomer(String customerNo, BaseSiteModel brand, String transactionType );
    List<DealerVehicleDetailsModel> getAllVehicleDetails();

    List<SclCustomerModel> getSclCustomerForGroupAndSO(String userGroupUid,String uid);

    double getLostSaleForCustomer(SclCustomerModel user);
    List<SclCustomerModel> getDealersForSalesPromoter(SclCustomerModel promoter);

    String getSPNetworkPotentialMTD(SclCustomerModel promoter);

    String getSPNetwokSalesMTD(SclCustomerModel promoter);

    Double getSPNetworkShare(SclCustomerModel promoter);
    List<SclCustomerModel> getSclCustomerForGroup(String userGroupUid);
    SCLPotentialCustomerListData getTopPotentialCustomerListData(String leadType);

    List<SchemeDetailsModel> getOnGoingSchemes(PartnerLevel dealer);

    List<LeadMasterModel> getAllLeads(LeadType leadType, String monthYear, String searchTerm,  List<String> doList,  List<String> subAreaList,List<String> territoryList);
    List<LeadMasterModel> getAllLostLeads(LeadType leadType);

    List<SclCustomerModel> getCustomerListFromSubArea(String subArea,BaseSiteModel site);
    Map<String,Integer> getCounterInfoForTaluka(String taluka,String leadType);

    /**
     *
     * @return MeetingScheduleListData
     */
    List<MeetingScheduleModel> getInfluencerMeetCards();
    Map<String,Integer> getNetworkTypeCount(String leadType, List<String> doList,
                                            List<String> subAreaList, List<String> territoryList);

    List<SubAreaMasterModel> getSubAreaList(List<String> doList, List<String> subAreaList);

    InactiveNetworkData getInactiveNetworkRemovalDetailsForCode(String code);

    Boolean saveInactiveNetworkRemovalDetails(InactiveNetworkData data);

    List<SclUserModel> getOtherNetworkSO();
    RetailerOnboardListData getOnboarderRetailerData(LeadType leadType, String searchKey);
    ChannelStrength getChannelKPIGraphDealerRetailer(String leadType,List<String> doList,  List<String> subAreaList,  List<String> territoryList);

    List<List<Object>> getLatitudeLongitudeOfProspectiveNetworkList(String customerCode);

    List<SclCustomerModel> getInActiveCustomers(String dealerUserGroupUid);

    SiteSummaryData getSiteSummaryforNetwork(String customerCode);

    List<SclCustomerModel> filterSclCustomersWithSearchTerm(List<SclCustomerModel> customers,String searchTerm);

    String getDaysSinceLastLifting(List<NirmanMitraSalesHistoryModel> salesHistry);

    List<MeetingScheduleModel> filterMeetings(List<MeetingScheduleModel> meetingScheduleModels, String searchTerm);

    Integer getOnboarderCustomer(String leadType, String taluka);
    NetworkDealerRetailerCounterShareData getDealerRetailerCounterShareForNetwork(String SOFilter, BaseSiteModel site, String taluka, List<String> doList, List<String> subAreaList, List<String> territoryList);

    List<MeetingScheduleModel> filterMeetForStatus(List<MeetingScheduleModel> meetingScheduleModels, String statusFilter);

    List<MeetingScheduleModel> filterMeetForCategory(List<MeetingScheduleModel> meetingScheduleModels, String categoryFilter);

    List<SclCustomerModel> getFilteredCustomerForNetworkType(List<SclCustomerModel> influencerModelList, String networkType);

    List<SclCustomerModel> getFilteredInfluencerForCategory(List<SclCustomerModel> influencerModelList, String category);


    List<SclCustomerModel> filterSclCustomersWithDealerCategory(List<SclCustomerModel> networkCustomerList, String dealerCategory);

    List<List<Object>> getCounterLocationDetails(String dealerCode);

    boolean submitUpdatedLocationDetails(CounterLocationDetailsData counterLocationDetailsData);

    String getExclusiveDealerPercentage(List<String> territoryList);
    List<InviteesData> getInviteesForMeeting(MeetingScheduleModel meet, List<SclCustomerModel> customers);

    String isMultiBrand(SclCustomerModel promoter);

    List<MonthlySalesData> getLastSixMonthSalesForDealer(String taluka,BaseSiteModel site,String Filter,List<String> territoryList);

    List<MonthlySalesData> getLastSixMonthSalesForRetailer(String taluka, BaseSiteModel site, String Filter, List<String> territoryList, List<String> districtList, List<String> subAreaList);

    List<MonthlySalesData> getLastSixMonthSalesForInfluencer(String taluka, BaseSiteModel site, String filter);

    List<NetworkRemovalModel> getNetworkRemoval();

    List<SubAreaMasterModel> getSubAreaForSalesPromoter(SclCustomerModel promoter);

    List<SclUserModel> getSalesOfficersForSubArea(List<SubAreaMasterModel> subAreaMasters);

    String getTotalOutstandingForPromoter(SclCustomerModel promoter);

    String getOutstandingDaysForPromoter(SclCustomerModel promoter);
    Integer getNewInfluencerCountMTD( SclCustomerModel sclCustomer,BaseSiteModel baseSite, Date startDate, Date endDate,Date doj,String fromCustomerType);
    Integer getNewRetailerCountMTD(SclCustomerModel sclCustomer, BaseSiteModel baseSite, Date startDate, Date endDate, Date doj);
    Integer getRetailerInfluencerCardCountMTD(String customerType,SclCustomerModel sclCustomer, BaseSiteModel baseSite, Date startDate, Date endDate,String networktype);

    Integer getNetworkDormantCountCard(String customerType,SclUserModel currentUser, BaseSiteModel currentBaseSite, Date startDateForCM, Date endDateForCM, String networkType,List<String> doList,
                                       List<String> subAreaList,List<String> territoryList);

    List<SCLImageData> getOnboardingFormsSS(String uid);

    SalesHistoryData getSalesHistoryDataForNetworkInfluencer360(SclCustomerModel influencer,List<String> subAreaList,List<String> districtList);

    LeadMasterModel findItemByUidParam(String leadId);

    Integer getLeadsGeneratedCountedForInfluencer(String filter, SclCustomerModel customerModel, BaseSiteModel brand);

    List<SclCustomerModel> getSalesPromotersForSubArea(List<String> doList, List<String> subareaList);

    List<List<Object>> getOrderReqSalesForRetailer(List<SclCustomerModel> sclReailer,SclCustomerModel dealer,String startDate,String endDate);

    List<List<Object>> getMasterSalesForRetailer(List<SclCustomerModel> sclReailer,SclCustomerModel dealer,String startDate,String endDate);
    List<List<Object>> getSalesForRetailerList(List<SclCustomerModel> sclRetailer, SclCustomerModel dealer, String startDate, String endDate);

    Map<String,Double>  getSalesQuantityForRetailerByMTD(SclCustomerModel sclReailer,List<SclCustomerModel> sclDealer,String product) ;
    Map<String,Double>  getSalesQuantityForRetailerByMTD(List<SclCustomerModel> sclReailer,List<SclCustomerModel> sclDealer,String product) ;
    Map<String,Double>  getSalesQuantityForRetailerByYTD(SclCustomerModel sclReailer,List<SclCustomerModel> sclDealer,String product) ;
    Map<String,Double>  getSalesQuantityForRetailerByYTD(List<SclCustomerModel> sclReailer,List<SclCustomerModel> sclDealer,String product,int StartYear,int endYear) ;
    Map<String,Double>  getSalesQuantityForRetailerByDate(SclCustomerModel sclReailer,String startDate,String endDate,List<SclCustomerModel> sclDealer,String product) ;
    Map<String,Double>  getSalesQuantityForRetailerByDate(List<SclCustomerModel> sclReailer,String startDate,String endDate,List<SclCustomerModel> sclDealer,String product) ;
    Map<String,Double>  getSalesQuantityForRetailerByMonthYear(SclCustomerModel sclReailer,int month,int year,List<SclCustomerModel> sclDealer,String product) ;
    Map<String,Double>  getSalesQuantityForRetailerByMonthYear(List<SclCustomerModel> sclReailer,int month,int year,List<SclCustomerModel> sclDealer,String product) ;

    boolean isBodyPresentInSiteMessage(String body);
}