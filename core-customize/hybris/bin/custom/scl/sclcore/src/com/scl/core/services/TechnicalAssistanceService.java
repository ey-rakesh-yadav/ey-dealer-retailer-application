package com.scl.core.services;

import java.util.List;
import java.util.Map;

import com.scl.core.model.*;
import com.scl.facades.data.*;

import de.hybris.platform.core.HybrisEnumValue;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

public interface TechnicalAssistanceService {

	public Boolean saveForm(String userId, TechnicalAssistanceData data);
	
	public List<String> getExpertiseListForCurrentConstructionStage(String constructionStage);

	SearchPageData<NetworkAssistanceModel> getNetworkAssitances(SearchPageData searchPageData, String startDate,
			String endDate, String partnerCode, String requestNo, String filters,List<String> status);
	SearchPageData<EndCustomerComplaintModel> getEndCustomerComplaints(SearchPageData searchPageData, String startDate, String endDate, String partnerCode
			,String requestNo, String filters,List<String> requestStatuses,Boolean isSiteVisitRequired,Boolean plannedVisitForToday, String subAreaMasterPk,List<String> talukas);
	EndCustomerComplaintModel getEndCustomerComplaintForRequestNo(String requestId);
  
  	public SearchPageData<TechnicalAssistanceModel> getTechnicalAssistances(SearchPageData searchPageData, String startDate,
																  String endDate, String name, String requestNo, String filters,List<String> status,List<String> constructionAdvisory,List<String> taluka);
	
	public TechnicalAssistanceModel getTechnicalAssistanceRequestDetails(String requestNo);

	/**
	 *
	 * @param userId
	 * @return
	 */
	List<SclCustomerModel> getAllRetailersForMaterial(String userId);

	/**
	 *
	 * @param userId
	 * @return
	 */
	List<SclCustomerModel> getAllDealersForMaterial(String userId);

	/**
	 *
	 * @param customerComplaintModel
	 * @return
	 */
	Boolean submitCustomerComplaint(CustomerComplaintModel customerComplaintModel);

	/**
	 *
	 * @param invoiceNo
	 * @return
	 */
	InvoiceMasterModel getInvoiceMasterByInvoiceNo(String invoiceNo);

	/**
	 *
	 * @param requestNo
	 * @return
	 */
	CustomerComplaintModel getCustomerAssistanceRequestDetails(String requestNo);

	/**
	 *
	 * @param userId
	 * @param year
	 * @param month
	 * @param status
	 * @param searchKey
	 * @return
	 */
	List<CustomerComplaintModel> getCustomerAssistanceRequestList(String userId, Integer year, Integer month, String status, String searchKey);

  	public Boolean saveSOComment(String requestNo, String comment);
	
	public Boolean markRequestAsCompleted(String requestNo,String resolvedComment);
	
	public HybrisEnumValue getEnumerationValueForLocalizedName(String enumClass, String localizedName);

	public Boolean newRequestForm(NetworkAssitanceData networkAssitanceData);

	/**
	 * Count No of Assigned Ticket Numbers
	 * @param tsouser
	 * @return
	 */
	Integer countOfAssignedTicketNumbers(SclUserModel tsouser);
	DetailsFromSiteModel getDetailesFromSiteById(String complaintId);
	EndCustomerComplaintData submitRequestForAnotherTSODetails(String complaintId, String commentForAnotherTSORequest);

	TechnicalAssistanceModel rejectTAByTSO(String requestNo, String rejectReason);

	TechnicalAssistanceModel acceptTAByTSO(String requestNo, String rejectedReason);

	TechnicalAssistanceModel closeTAByTSO(String requestNo, String closeComment);

	/**
	 * Get TSO
	 * @param taluka
	 * @param district
	 * @param state
	 * @return
	 */
	List<SclUserModel> getTSO(String taluka, String district, String state);

	/**
	 * Get No of TA Assigned Ticket Numbers
	 * @param tsouser
	 * @return
	 */
	Integer countOfTAAssignedTicketNumbers(SclUserModel tsouser);

	EndCustomerComplaintModel getEndCustomerComplaintRequestNumber(String complaintId);

	/**
	 *
	 * @param cardType
	 * @return
	 */
	TalukaTaCcData getTalukasForTaOrCc(String cardType);

}
