package com.eydms.facades.network;


import com.eydms.core.enums.LeadType;
import com.eydms.core.model.MeetingScheduleModel;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.facades.data.*;
import com.eydms.facades.data.order.vehicle.DealerVehicleDetailsListData;
import com.eydms.facades.prosdealer.data.DealerListData;
import com.eydms.facades.visit.data.SiteSummaryData;
import com.eydms.occ.dto.InfluencerNomineeData;
import com.eydms.occ.dto.RetailerOnboardDto;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

public interface EYDMSNetworkFacade {
    String addNetworkPlan(EYDMSNetworkAdditionPlanData planData);
    EYDMSNetworkAdditionPlanData getNetworkPlan(String uid, String taluka);
    String addInfluencerBasics(InfluencerData data);
    InfluencerData getInfluencerBasics(String id);
    String addInfluencerFinancials(InfluencerFinanceData data);
    InfluencerFinanceData getInfluencerFinancials(String id);
    String addInfluencerNominee(InfluencerNomineeData data);
    InfluencerSummaryListData getInfluencerSummaryList(String searchKey, boolean isNew, String networkType, String category,String dealerCategory);
    InfluencerSummaryListData getInfluencerSummaryListForSO(String socode, String category, String networkType,String dealerCategory);
    List<InviteesData> getInviteesListForMeeting(String meetCode, String influencerType,
                                                 String influencerCategory);
    Boolean addMeetingSchedule(MeetingScheduleData data);
    Boolean saveMeetingAttendance(MeetingScheduleData  data);
    ProspectiveNetworkListData getPerspectiveNetworkList(String leadType,String customerCode,String dealerCategory,String taluka, String stage);
    DealerVehicleDetailsListData getDealerVehicleDetails();
    DealerCurrentNetworkListData getDealerCurrentNetworkData(String subArea, String fields, String networkType, String leadType, boolean eydmsExclusiveCustomer, String searchKey);
    SiteStageSummaryListData getSiteStageSummary();
    SiteDetailListData getSiteDataList(String siteStage, String searchKey);
    SiteDetailListData getSiteDataListByCategory(String category, String searchKey);
    SiteDetailListData getSiteDataMTDList(String searchKey) throws ParseException;
    SiteDetailListData getSiteDataList(String searchKey);
    String updateTimesContacted(String customerNo, Boolean phoneContacted);
    NwUserListData getRetailerDealerSO();
    NwUserListData getOtherNetworkSO();
    EYDMSAddressData getAddressForUserId(String userId);
    InactiveNetworkListData getInactiveNetworkList(String category, String searchKey, String taluka);

    InactiveNetworkListData getDormantList(String networkType,String customerType, String searchKey, String taluka);
    CustomerCardListData getCustomerCards(String subArea,String leadType,String onboardingStatus, String searchKey);

    InfluencerSummaryListData getInfluencerListForCategory(String category, String searchKey);
    SPSalesPerformanceListData getSPSalesPerformanceData(String searchKey);
    EYDMSPotentialCustomerListData getTopPotentialCustomer(String leadType);
    DealerDetails360Data getDealerDetails360(String dealerCode,String subArea);
    CounterLocationDetailsData getLocationDetails(String dealerCode);
    CounterLocationDetailsData getRequestUpdate(String dealerCode);

    boolean submitUpdatedLocationDetails(CounterLocationDetailsData counterLocationDetailsData);
    InfluencersDetails360WsData getInfluencerDetails360(String influencerCode);
    DealerDetailsFormData getDealerDetailsForm(String dealerCode);
    Map<String,Integer> getOnboardingCardCount(String subArea, LeadType leadType,String duration);
    LeadSummaryListData getLeadSummaryList(LeadType leadType,String monthYear,String searchTerm,String leadId);
    boolean removeLeadForId(String leadId);
    boolean updateLead(String leadId,String leadStage);
    DealerListData getAllDealersForSubArea(String subArea, boolean eydmsExclusiveCustomer);
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
    Map<String,Integer> getNetworkTypeCount(String leadType);
    
    InactiveNetworkData getInactiveNetworkRemovalDetailsForCode(String code);
    
    Boolean saveInactiveNetworkRemovalDetails(InactiveNetworkData data);

    Map<String,Object> getSiteCategoryCount();
    String getPanFromGST(String gstnumber);
    Map<String,Object> getInfluencerCategoryCount();
    Map<String,Object> getChrunReasonCount();
    SalesPromoterDetailsData getSpDetails360(String spCode);
    RetailerOnboardListData getOnboardRetailerList(LeadType leadType, String searchKey);
    SearchPageData<RetailerOnboardDto> getOnboardRetailerListPagination(LeadType leadType, String searchKey, SearchPageData searchPageData);
    MarketMappingSiteDetailSummary getSumOfBalancePotentialMonthConsumption();
    Map<String,Object> getSiteStages();

    NetworkAdditionData getNetworkAdditionDetails(String leadType, String taluka);

    ChannelStrength getChannelKPIGraphDealerRetailer(String leadType);

    MapProspectiveNetworkDataList getLatitudeLongitudeOfProspectiveNetworkList(String networkType);

    SiteSummaryData getSiteSummaryforNetwork(String customerCode);
    NetworkDealerRetailerCounterShareData getDealerRetailerCounterShareForNetwork( String filter,String taluka);


    String getExclusiveDealerPercentage();


    List<MonthlySalesData> getLastSixMonthSalesForDealer(String taluka,String Filter);

    List<MonthlySalesData> getLastSixMonthSalesForRetailer(String taluka, String Filter);

    List<MonthlySalesData> getLastSixMonthSalesForInfluencer(String taluka, String Filter);

    List<NetworkAdditionData> getNetworkAdditionListDetails(String taluka);

    SearchPageData<InfluencerSummaryData> getPagniatedInfluencerSummaryList(String searchKey, boolean isNew, String networkType, String category,String dealerCategory, SearchPageData searchPageData);
    NewInfluencerRetailerCountData getNewRetailerInfluencerCountMTD(String customerType);
    NewInfluencerRetailerCountData getRetailerInfluencerCardCountMTD(String CustomerType,String networkType);
	SearchPageData<InfluencerSummaryData> getInfluencerDetailedSummaryList(String searchKey, Boolean isNew,
                                                                           String networkType, String influencerType, String influencerCategory, SearchPageData searchPageData,Boolean includeSales, Boolean includeScheduleMeet, Boolean includeNonEyDmsCustomer);

	SearchPageData<DealerCurrentNetworkData> getRetailerDetailedSummaryList(String searchKey, Boolean isNew,
			String networkType, SearchPageData searchPageData);

    NewInfluencerRetailerCountData getNetworkDormantCountCard(String networkType,String customerType);
    public List<DealerCurrentNetworkData> getRetailerDetailedSummaryListData(List<EyDmsCustomerModel> retailerList) ;
    
    List<EYDMSImageData> getOnboardingFormsSS(String uid);
    public List<InfluencerSummaryData> getInfluencerDetailedSummaryListData(MeetingScheduleModel model,List<EyDmsCustomerModel> influencerList,Boolean includeSales, Boolean includeScheduleMeet);

    public SalesHistoryData getSalesHistoryDataForNetworkInfluencer360(String influencerCode);

    Integer getLeadsGeneratedCountedForInfluencer(String filter);
}
