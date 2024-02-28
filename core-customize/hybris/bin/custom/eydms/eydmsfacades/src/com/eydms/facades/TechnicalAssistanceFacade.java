package com.eydms.facades;

import java.util.List;
import java.util.Map;

import com.eydms.core.model.EndCustomerComplaintModel;
import com.eydms.core.model.NetworkAssistanceModel;
import com.eydms.facades.data.*;

import com.eydms.facades.prosdealer.data.DealerListData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

public interface TechnicalAssistanceFacade {
	
	public Boolean saveForm(String userId, TechnicalAssistanceData data);
	
	public List<String> getExpertiseListForCurrentConstructionStage(String constructionStage);

	SearchPageData<TechnicalAssistanceData> getTechnicalAssistances(SearchPageData searchPageData, String startDate,
															   String endDate, String partnerCode, String requestNo, String filters,List<String> status, List<String> constructionAdvisory);
	
	public TechnicalAssistanceData getTechnicalAssistanceRequestDetails(String requestNo);

	SearchPageData<NetworkAssitanceData> getNetworkAssitances(SearchPageData searchPageData, String startDate,
			String endDate, String partnerCode, String requestNo, String filters,List<String> status);
	SearchPageData<EndCustomerComplaintData> getEndCustomerComplaints(SearchPageData searchPageData, String startDate, String endDate, String partnerCode
			, String requestNo, String filters,List<String> requestStatuses,Boolean isSiteVisitRequired, Boolean plannedVisitForToday,String subAreaMasterPk);
	
	Boolean saveSOComment(String requestNo, String comment);
	
	Boolean markRequestAsCompleted(String requestNo,String resolvedComment);

	/**
	 *
	 * @param userId
	 * @return
	 */
	DealerListData getAllRetailersForMaterial(String userId);

	/**
	 *
	 * @param userId
	 * @return
	 */
	DealerListData getAllDealersForMaterial(String userId);

	/**
	 *
	 * @param customerComplaintData
	 * @return
	 */
	Boolean submitCustomerComplaint(CustomerComplaintData customerComplaintData);

	/**
	 *
	 * @param invoiceNo
	 * @return
	 */
	InvoiceMasterData getInvoiceMasterByInvoiceNo(String invoiceNo);

	/**
	 *
	 * @param customerCode
	 * @return
	 */
	CustomerData getCustomerByCode(String customerCode);

	/**
	 *
	 * @param requestNo
	 * @return
	 */
	CustomerComplaintData getCustomerAssistanceRequestDetails(String requestNo);

	/**
	 *
	 * @param userId
	 * @param year
	 * @param month
	 * @param status
	 * @param searchKey
	 * @return
	 */
	CustomerAssistanceListData getCustomerAssistanceRequestList(String userId, Integer year, Integer month, String status, String searchKey);

	Boolean newRequestForm(NetworkAssitanceData networkAssitanceData);
	String addEndCustomerDetails(EndCustomerComplaintData data);
	EndCustomerComplaintData getEndCustomerDetailsByRequestId(String requestId, boolean isUiRelatedChange);
	EndCustomerComplaintData getEndCustomerComplaintRequestTrackerDetails(String requestId);

    EndCustomerComplaintData updateCustomerComplaint(String complaintId, String soTaggedName, Boolean isSiteVisitRequired);

	EndCustomerComplaintData bookTechnicalExpert(ComplaintTEMeetingData complaintTEMeetingData);


	EndCustomerComplaintData submitTicketClosureRequest(TicketClosureRequestData ticketClosureRequestData);

    EndCustomerComplaintData saveSiteVisitNotRequiredForm(ComplaintSiteVisitNotRequiredData complaintSiteVisitNotRequiredData);

	EndCustomerComplaintData saveComplaintDispatchDetails(ComplaintDispatchDetailsData complaintDispatchDetailsData);

	EndCustomerComplaintData submitNextVisitPlanDetailes(ScheduleNextVisitData scheduleNextVisitData);
	CustomerComplaintHomePageData countOfAssignedTicketNumbers();

	EndCustomerComplaintData getComplaintDispatchDetails(String complaintId);

	ComplaintDispatchDetailsData removeComplaintDispatchDetails(String complaintId, String dispatchDetailsId);

	EndCustomerComplaintData saveComplaintRootCauseIdentifiedForm(ComplaintRootCauseIndentifiedData complaintRootCauseIndentifiedData);

	EndCustomerComplaintData saveDetailsFromSite(DetailsFromSiteData detailsFromSiteData);
	EndCustomerComplaintData submitRequestForAnotherTSODetails(String complaintId,String commentForAnotherTSORequest);

	EndCustomerComplaintData saveComplaintTestPerformed(ComplaintTestPerformedData complaintTestPerformedData);

	TechnicalAssistanceData rejectTAByTSO(String requestNo, String rejectedReason);

	TechnicalAssistanceData acceptTAByTSO(String requestNo, String siteId);

	TechnicalAssistanceData closeTAByTSO(String requestNo, String closeComment);
}
