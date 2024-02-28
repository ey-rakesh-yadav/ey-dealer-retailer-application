package com.eydms.core.services;

import com.eydms.core.enums.LeadType;
import com.eydms.core.enums.PartnerLevel;
import com.eydms.core.model.*;
import com.eydms.facades.data.*;
import com.eydms.facades.visit.data.SiteSummaryData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface NetworkService {
    DealerCurrentNetworkListData getDealerCurrentNetworkWsData(String dealerCategory , String fields, String networkType, BaseSiteModel site, String leadType, boolean eydmsExclusiveCustomer, String searchKey);
    ProspectiveNetworkModel findNetworkByCode(String code);
    List<NirmanMitraSalesHistoryModel> getMitraSalesDataForCustomer(String customerNo, BaseSiteModel brand, String transactionType );
    List<DealerVehicleDetailsModel> getAllVehicleDetails();

    List<EyDmsCustomerModel> getEyDmsCustomerForGroupAndSO(String userGroupUid,String uid);

    double getLostSaleForCustomer(EyDmsCustomerModel user);
    List<EyDmsCustomerModel> getDealersForSalesPromoter(EyDmsCustomerModel promoter);

    String getSPNetworkPotentialMTD(EyDmsCustomerModel promoter);

    String getSPNetwokSalesMTD(EyDmsCustomerModel promoter);

    Double getSPNetworkShare(EyDmsCustomerModel promoter);
    List<EyDmsCustomerModel> getEyDmsCustomerForGroup(String userGroupUid);
    EYDMSPotentialCustomerListData getTopPotentialCustomerListData(String leadType);

    List<SchemeDetailsModel> getOnGoingSchemes(PartnerLevel dealer);

    List<LeadMasterModel> getAllLeads(LeadType leadType,String monthYear,String searchTerm);
    List<LeadMasterModel> getAllLostLeads(LeadType leadType);

    List<EyDmsCustomerModel> getCustomerListFromSubArea(String subArea,BaseSiteModel site);
    Map<String,Integer> getCounterInfoForTaluka(String taluka,String leadType);

    /**
     *
     * @return MeetingScheduleListData
     */
    List<MeetingScheduleModel> getInfluencerMeetCards();
    Map<String,Integer> getNetworkTypeCount(String leadType);
    
    InactiveNetworkData getInactiveNetworkRemovalDetailsForCode(String code);
    
    Boolean saveInactiveNetworkRemovalDetails(InactiveNetworkData data);

    List<EyDmsUserModel> getOtherNetworkSO();
    RetailerOnboardListData getOnboarderRetailerData(LeadType leadType, String searchKey);
    ChannelStrength getChannelKPIGraphDealerRetailer(String leadType);

    List<List<Object>> getLatitudeLongitudeOfProspectiveNetworkList(String customerCode);

    List<EyDmsCustomerModel> getInActiveCustomers(String dealerUserGroupUid);

    SiteSummaryData getSiteSummaryforNetwork(String customerCode);

    List<EyDmsCustomerModel> filterEyDmsCustomersWithSearchTerm(List<EyDmsCustomerModel> customers,String searchTerm);
    String getDaysSinceLastLifting(List<NirmanMitraSalesHistoryModel> salesHistry);

    List<MeetingScheduleModel> filterMeetings(List<MeetingScheduleModel> meetingScheduleModels, String searchTerm);

    Integer getOnboarderCustomer(String leadType, String taluka);
    NetworkDealerRetailerCounterShareData getDealerRetailerCounterShareForNetwork( String SOFilter,BaseSiteModel site, String taluka);

    List<MeetingScheduleModel> filterMeetForStatus(List<MeetingScheduleModel> meetingScheduleModels, String statusFilter);

    List<MeetingScheduleModel> filterMeetForCategory(List<MeetingScheduleModel> meetingScheduleModels, String categoryFilter);

    List<EyDmsCustomerModel> getFilteredCustomerForNetworkType(List<EyDmsCustomerModel> influencerModelList, String networkType);

    List<EyDmsCustomerModel> getFilteredInfluencerForCategory(List<EyDmsCustomerModel> influencerModelList, String category);


    List<EyDmsCustomerModel> filterEyDmsCustomersWithDealerCategory(List<EyDmsCustomerModel> networkCustomerList, String dealerCategory);

    List<List<Object>> getCounterLocationDetails(String dealerCode);

    boolean submitUpdatedLocationDetails(CounterLocationDetailsData counterLocationDetailsData);

    String getExclusiveDealerPercentage();
    List<InviteesData> getInviteesForMeeting(MeetingScheduleModel meet, List<EyDmsCustomerModel> customers);

    String isMultiBrand(EyDmsCustomerModel promoter);

    List<MonthlySalesData> getLastSixMonthSalesForDealer(String taluka,BaseSiteModel site,String Filter);

    List<MonthlySalesData> getLastSixMonthSalesForRetailer(String taluka, BaseSiteModel site, String Filter);

    List<MonthlySalesData> getLastSixMonthSalesForInfluencer(String taluka, BaseSiteModel site, String filter);

    List<NetworkRemovalModel> getNetworkRemoval();

    List<SubAreaMasterModel> getSubAreaForSalesPromoter(EyDmsCustomerModel promoter);

    List<EyDmsUserModel> getSalesOfficersForSubArea(List<SubAreaMasterModel> subAreaMasters);

    String getTotalOutstandingForPromoter(EyDmsCustomerModel promoter);

    String getOutstandingDaysForPromoter(EyDmsCustomerModel promoter);
    Integer getNewInfluencerCountMTD( EyDmsCustomerModel eydmsCustomer,BaseSiteModel baseSite, Date startDate, Date endDate,Date doj,String fromCustomerType);
    Integer getNewRetailerCountMTD(EyDmsCustomerModel eydmsCustomer, BaseSiteModel baseSite, Date startDate, Date endDate, Date doj);
    Integer getRetailerInfluencerCardCountMTD(String customerType,EyDmsCustomerModel eydmsCustomer, BaseSiteModel baseSite, Date startDate, Date endDate,String networktype);

    Integer getNetworkDormantCountCard(String customerType,EyDmsUserModel currentUser, BaseSiteModel currentBaseSite, Date startDateForCM, Date endDateForCM, String networkType);
    
    List<EYDMSImageData> getOnboardingFormsSS(String uid);

    SalesHistoryData getSalesHistoryDataForNetworkInfluencer360(EyDmsCustomerModel influencer);

    LeadMasterModel findItemByUidParam(String leadId);

    Integer getLeadsGeneratedCountedForInfluencer(String filter, EyDmsCustomerModel customerModel, BaseSiteModel brand);

}
