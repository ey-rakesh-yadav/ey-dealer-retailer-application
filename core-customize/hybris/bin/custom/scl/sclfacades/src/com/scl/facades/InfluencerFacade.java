package com.scl.facades;

import java.util.List;

import com.scl.facades.data.*;
import com.scl.facades.data.InfluencerManagementHomePageData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import org.springframework.web.bind.annotation.RequestParam;

public interface InfluencerFacade {

	OnboardingPartnerData searchOnboardingPartner(List<String> searchKey) throws Exception;
	
	Boolean assignOnboardingPartner(String influencerUid, String partnerUid);
	
	String getOnboardingStatus(String influencerId);

    InfCockpitSchemeStatusData getGiftPointsStatus();
    
    Boolean editProfile(SclCustomerData data);

	OnboardingPartnerData fetchOnboardingPartnerDetails(String influencerUid);
	Boolean submitMeetingCompletionForm(MeetingCompletionFormData data);

	InfluencerManagementHomePageData getInfluencerManagementHomePage(String influencerType);

	CustomerCardListData getInfluencerOnboardingList(SearchPageData searchPageData,
			RequestCustomerData customerRequestData);

    SearchPageData<SclLeadData> getPaginatedLeadList(SearchPageData searchPageData, LeadRequestData leadRequestData);

    SclLeadData updateLeadStatus(String leadId, String status,String rejectedComment);

	SclLeadData viewLeadDetailsById(String leadId);

    SearchPageData<ScheduledMeetData> getPaginatedScheduleMeetList(SearchPageData searchPageData, String meetingType, String startDate, String endDate, String searchFilter, List<String> status);

	InfluencerVisitData saveInfluencerVisitForm(InfluencerVisitData influencerVisitData);

	ScheduledMeetData getMeetingCompletionFormDetail(String scheduleMeetId);

	InfluencersDetails360WsData getInfluencerDetailsFor360(String influencerCode);
	InfluencerVisitHistoryListData getInfluencerVisitHistory(String filter);
}
