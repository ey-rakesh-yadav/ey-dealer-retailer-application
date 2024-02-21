package com.eydms.core.dao;

import java.util.List;

import com.eydms.core.model.*;
import com.eydms.facades.data.LeadRequestData;
import com.eydms.facades.data.RequestCustomerData;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

public interface InfluencerDao {
	
	EyDmsUserModel searchSalesOfficer(List<String> keys);
	
	EyDmsCustomerModel searchEyDmsCustomer(List<String> keys);

	List<List<Object>> getGiftSchemeStatusDetails(String influencerType, String state);
	MeetingScheduleModel getMeetingScheduleByCode(String meetingCode);
	
	List<List<Object>> getInfluencerTypeList(String influencerType, List<SubAreaMasterModel> subAreaMasterList);

	List<List<Object>> getInfluencerNetworkTypeList(String influencerType, List<SubAreaMasterModel> subAreaMasterList);

	List<List<Object>> getInfluencerNetworkAddition(String influencerType, List<SubAreaMasterModel> subAreaMasterList);

	Integer getLeadsPendingForApproval(String influencerType, List<SubAreaMasterModel> subAreaMasterList);

	SearchPageData<EyDmsCustomerModel> getInfluencerOnboardingList(SearchPageData searchPageData,
			RequestCustomerData customerRequestData, List<SubAreaMasterModel> subAreaMasterList);

    SearchPageData<LeadMasterModel> getPaginatedLeadList(SearchPageData searchPageData, LeadRequestData leadRequestData);

    SearchPageData<MeetingScheduleModel> getPaginatedScheduleMeetList(SearchPageData searchPageData, String meetingType, String startDate, String endDate, String searchFilter, List<String> status);

    List<PointRequisitionModel> getInfluencerSalesData(String uid, BaseSiteModel currentBaseSite);

	Integer getOnboardingsPendingForApproval(String influencerType, List<SubAreaMasterModel> subAreaMasterList);


	List<InfluencerVisitMasterModel> getInfluencerVisitDetails();

	List<MeetingScheduleModel> getInfluencerMeetCompleted();
	Integer getVisitCountMTD(EyDmsCustomerModel eydmsCustomer, int month, int year);

	//Integer getInfLeadGeneratedCount();
	int getInfluencerVisitHistoryCount(String startDate,String endDate);

	Integer getInfLeadGeneratedCount(EyDmsCustomerModel source);


}
