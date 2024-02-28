package com.eydms.core.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.enums.MeetStatus;
import com.eydms.core.model.MeetingScheduleModel;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.facades.data.InviteesData;
import com.eydms.facades.data.LeadRequestData;
import com.eydms.facades.data.RequestCustomerData;

import com.eydms.facades.data.EyDmsLeadData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

public interface InfluencerService {

	B2BCustomerModel searchOnboardingPartner(List<String> searchKey) throws Exception;
	
	Boolean assignOnboardingPartner(String influencerUid, String partnerUid);
	
	String getOnboardingStatus(String influencerId);

	List<List<Object>> getGiftSchemeStatusDetails(String influencerType, String state);

	EyDmsCustomerModel fetchOnboardingPartnerDetails(String influencerId);
	MeetingScheduleModel getMeetingScheduleByCode(String meetingCode);
	
	List<List<Object>> getInfluencerTypeList(String influencerType);

	SearchPageData<EyDmsCustomerModel> getInfluencerOnboardingList(SearchPageData searchPageData,
			RequestCustomerData customerRequestData);

	List<List<Object>> getInfluencerNetworkTypeList(String influencerType);

	List<List<Object>> getInfluencerNetworkAddition(String influencerType);


    SearchPageData<EyDmsLeadData> getPaginatedLeadList(SearchPageData searchPageData, LeadRequestData leadRequestData);

    SearchPageData<MeetingScheduleModel> getPaginatedScheduleMeetList(SearchPageData searchPageData, String meetingType, String startDate, String endDate, String searchFilter, List<String> status);

	List<InviteesData> getInviteesForMeeting(MeetingScheduleModel meet, List<EyDmsCustomerModel> customers);

	Integer getLeadsPendingForApproval(String influencerType);

	Integer getOnboardingsPendingForApproval(String influencerType);
	int getInfluencerVisitHistoryCount(String startDate,String endDate);

}
