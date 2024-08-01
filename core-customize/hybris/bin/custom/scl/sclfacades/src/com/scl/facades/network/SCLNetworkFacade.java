package com.scl.facades.network;


import com.scl.core.enums.LeadType;
import com.scl.core.model.MeetingScheduleModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.facades.data.*;
import com.scl.facades.data.order.vehicle.DealerVehicleDetailsListData;
import com.scl.facades.prosdealer.data.DealerListData;
import com.scl.facades.visit.data.SiteSummaryData;
import com.scl.occ.dto.InfluencerNomineeData;
import com.scl.occ.dto.RetailerOnboardDto;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

public interface SCLNetworkFacade {
    String addNetworkPlan(SCLNetworkAdditionPlanData planData);
    SCLNetworkAdditionPlanData getNetworkPlan(String uid, String taluka);
    String addInfluencerBasics(InfluencerData data);
    InfluencerData getInfluencerBasics(String id);
    String addInfluencerFinancials(InfluencerFinanceData data);
    InfluencerFinanceData getInfluencerFinancials(String id);
    String addInfluencerNominee(InfluencerNomineeData data);
    InfluencerSummaryListData getInfluencerSummaryList(String searchKey, boolean isNew, String networkType, String category,String dealerCategory,List<String> subAreaList,List<String> districtList);
    InfluencerSummaryListData getInfluencerSummaryListForSO(String socode, String category, String networkType,String dealerCategory);
    List<InviteesData> getInviteesListForMeeting(String meetCode, String influencerType,
                                                 String influencerCategory);
    Boolean addMeetingSchedule(MeetingScheduleData data);
    Boolean saveMeetingAttendance(MeetingScheduleData  data);
    ProspectiveNetworkListData getPerspectiveNetworkList(String leadType,String customerCode,String dealerCategory,String taluka, String stage);
    DealerVehicleDetailsListData getDealerVehicleDetails();
    DealerCurrentNetworkListData getDealerCurrentNetworkData(String subArea, String fields, String networkType, String leadType, boolean sclExclusiveCustomer, String searchKey, List<String> doList,
                                                             List<String> subAreaList, List<String> territoryList);
    SiteStageSummaryListData getSiteStageSummary();
    SiteDetailListData getSiteDataList(String siteStage, String searchKey);
    SiteDetailListData getSiteDataListByCategory(String category, String searchKey, List<String> districtList, List<String> subAreaList);
    SiteDetailListData getSiteDataMTDList(String searchKey) throws ParseException;
    SiteDetailListData getSiteDataList(String searchKey);

    String updateTimesContacted(String customerNo, Boolean phoneContacted);
    NwUserListData getRetailerDealerSO();
    NwUserListData getOtherNetworkSO();
    SCLAddressData getAddressForUserId(String userId);
    InactiveNetworkListData getInactiveNetworkList(String category, String searchKey, String taluka);

    InactiveNetworkListData getDormantList(String networkType,String customerType, String searchKey, String taluka,List<String> doList, List<String> subAreaList,List<String> territoryList);
    CustomerCardListData getCustomerCards(String subArea,String leadType,String onboardingStatus, String searchKey);


 
    SPSalesPerformanceListData getSPSalesPerformanceData(String searchKey,List<String> doList,List<String> subareaList);

    InfluencerSummaryListData getInfluencerListForCategory(String category, String searchKey, List<String> districtList, List<String> subAreaList);
  
    SCLPotentialCustomerListData getTopPotentialCustomer(String leadType);
    DealerDetails360Data getDealerDetails360(String dealerCode,String subArea, List<String> territoryList);
    CounterLocationDetailsData getLocationDetails(String dealerCode);
    CounterLocationDetailsData getRequestUpdate(String dealerCode);

    boolean submitUpdatedLocationDetails(CounterLocationDetailsData counterLocationDetailsData);
    InfluencersDetails360WsData getInfluencerDetails360(String influencerCode,List<String> subAreaList,List<String> districtList);
    DealerDetailsFormData getDealerDetailsForm(String dealerCode);
    Map<String,Integer> getOnboardingCardCount(String subArea, LeadType leadType,String duration, List<String> doList, List<String> soList);
    LeadSummaryListData getLeadSummaryList(LeadType leadType,String monthYear,String searchTerm,String leadId,List<String> doList, List<String> subAreaList,List<String> territoryList);
    boolean removeLeadForId(String leadId);
    boolean updateLead(String leadId,String leadStage);
    DealerListData getAllDealersForSubArea(String subArea, boolean sclExclusiveCustomer);
    DealerListData getAllRetailersForSubArea(String subArea);
    DealerListData getProspectiveCustomer(String leadType);
    boolean verifyDenyPartner(String uid,String status,String reason);
    Map<String,Integer> getCounterInfoForTaluka(String taluka,String leadType);
    DealerListData getInfluencerCustomers();

    /**
     *
     * @return MeetingScheduleListData
     */
    ScheduledMeetListData getInfluencerMeetCards(String code,String dateFilter,String searchterm,String status,
                                                 String category,String fromDate,String toDate);
    Map<String,Integer> getNetworkTypeCount(String leadType, List<String> doList,
                                            List<String> subAreaList, List<String> territoryList);
    
    InactiveNetworkData getInactiveNetworkRemovalDetailsForCode(String code);
    
    Boolean saveInactiveNetworkRemovalDetails(InactiveNetworkData data);

    Map<String,Object> getSiteCategoryCount(List<String> doList, List<String> subAreaList);
    String getPanFromGST(String gstnumber);

  
    Map<String,Object> getChrunReasonCount(List<String> territoryList);

    Map<String,Object> getInfluencerCategoryCount(List<String> districtList, List<String> subAreaList);
  

    SalesPromoterDetailsData getSpDetails360(String spCode);
    RetailerOnboardListData getOnboardRetailerList(LeadType leadType, String searchKey);
    SearchPageData<RetailerOnboardDto> getOnboardRetailerListPagination(LeadType leadType, String searchKey, SearchPageData searchPageData);
    MarketMappingSiteDetailSummary getSumOfBalancePotentialMonthConsumption();
    Map<String,Object> getSiteStages();

    NetworkAdditionData getNetworkAdditionDetails(String leadType, String taluka);

    ChannelStrength getChannelKPIGraphDealerRetailer(String leadType,List<String> doList,  List<String> subAreaList,  List<String> territoryList);

    MapProspectiveNetworkDataList getLatitudeLongitudeOfProspectiveNetworkList(String networkType);

    SiteSummaryData getSiteSummaryforNetwork(String customerCode);
    NetworkDealerRetailerCounterShareData getDealerRetailerCounterShareForNetwork(String filter, String taluka, List<String> doList,  List<String> subAreaList,  List<String> territoryList);


    String getExclusiveDealerPercentage(List<String> territoryList);


    List<MonthlySalesData> getLastSixMonthSalesForDealer(String taluka,String Filter,List<String> territoryList);

    List<MonthlySalesData> getLastSixMonthSalesForRetailer(List<String> territoryList, List<String> districtList, List<String> subAreaList, String taluka, String Filter);

    List<MonthlySalesData> getLastSixMonthSalesForInfluencer(String taluka, String Filter);

    List<NetworkAdditionData> getNetworkAdditionListDetails(String taluka);

    SearchPageData<InfluencerSummaryData> getPagniatedInfluencerSummaryList(String searchKey, boolean isNew, String networkType, String category,String dealerCategory, SearchPageData searchPageData,List<String> subAreaList,List<String> districtList);
    NewInfluencerRetailerCountData getNewRetailerInfluencerCountMTD(String customerType);
    NewInfluencerRetailerCountData getRetailerInfluencerCardCountMTD(String CustomerType,String networkType);
	SearchPageData<InfluencerSummaryData> getInfluencerDetailedSummaryList(String searchKey, Boolean isNew,
                                                                           String networkType, String influencerType, String influencerCategory, SearchPageData searchPageData,Boolean includeSales, Boolean includeScheduleMeet, Boolean includeNonSclCustomer,
                                                                           List<String> subAreaList,List<String> districtList);

	SearchPageData<DealerCurrentNetworkData> getRetailerDetailedSummaryList(String searchKey, Boolean isNew,
			String networkType, SearchPageData searchPageData, List<String> subAreaList,List<String> districtList);

    NewInfluencerRetailerCountData getNetworkDormantCountCard(String networkType,String customerType,List<String> doList,
                                                              List<String> subAreaList, List<String> territoryList);
    public List<DealerCurrentNetworkData> getRetailerDetailedSummaryListData(List<SclCustomerModel> retailerList, List<String> subAreaList,List<String> districtList) ;
    
    List<SCLImageData> getOnboardingFormsSS(String uid);
    public List<InfluencerSummaryData> getInfluencerDetailedSummaryListData(MeetingScheduleModel model,List<SclCustomerModel> influencerList,Boolean includeSales, Boolean includeScheduleMeet,List<String> subareaList,List<String> districtList);

    public SalesHistoryData getSalesHistoryDataForNetworkInfluencer360(String influencerCode,List<String> subAreaList,List<String> districtList);

    Integer getLeadsGeneratedCountedForInfluencer(String filter);

    String sendPartnerNotification(String uid);

}
