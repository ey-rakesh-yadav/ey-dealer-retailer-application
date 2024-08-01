package com.scl.core.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.enums.MeetStatus;
import com.scl.core.model.MeetingScheduleModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.facades.data.InviteesData;
import com.scl.facades.data.LeadRequestData;
import com.scl.facades.data.RequestCustomerData;

import com.scl.facades.data.SclLeadData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

public interface InfluencerService {

	B2BCustomerModel searchOnboardingPartner(List<String> searchKey) throws Exception;
	
	Boolean assignOnboardingPartner(String influencerUid, String partnerUid);
	
	String getOnboardingStatus(String influencerId);

	List<List<Object>> getGiftSchemeStatusDetails(String influencerType, String state);

	SclCustomerModel fetchOnboardingPartnerDetails(String influencerId);
	MeetingScheduleModel getMeetingScheduleByCode(String meetingCode);
	
	List<List<Object>> getInfluencerTypeList(String influencerType);

	SearchPageData<SclCustomerModel> getInfluencerOnboardingList(SearchPageData searchPageData,
			RequestCustomerData customerRequestData);

	List<List<Object>> getInfluencerNetworkTypeList(String influencerType);

	List<List<Object>> getInfluencerNetworkAddition(String influencerType);


    SearchPageData<SclLeadData> getPaginatedLeadList(SearchPageData searchPageData, LeadRequestData leadRequestData);

    SearchPageData<MeetingScheduleModel> getPaginatedScheduleMeetList(SearchPageData searchPageData, String meetingType, String startDate, String endDate, String searchFilter, List<String> status);

	List<InviteesData> getInviteesForMeeting(MeetingScheduleModel meet, List<SclCustomerModel> customers);

	Integer getLeadsPendingForApproval(String influencerType);

	Integer getOnboardingsPendingForApproval(String influencerType);
	int getInfluencerVisitHistoryCount(String startDate,String endDate);

}
