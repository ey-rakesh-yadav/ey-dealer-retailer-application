package com.scl.core.dao;

import java.util.Date;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.scl.core.enums.TACurrentConstructionStage;
import com.scl.core.enums.TAExpertise;
import com.scl.core.enums.TAServiceRequestStatus;
import com.scl.core.model.*;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.core.model.user.UserModel;

public interface TechnicalAssistanceDao {

	List<TAExpertise> getExpertiseListForCurrentConstructionStage(TACurrentConstructionStage constructionStage);

	SearchPageData<NetworkAssistanceModel> getNetworkAssitances(SearchPageData searchPageData, String startDate, String endDate, String partnerCode, String requestNo,
			String filters,List<String> status);
	SearchPageData<EndCustomerComplaintModel> getEndCustomerComplaints(SearchPageData searchPageData, String startDate, String endDate, String partnerCode
			, String requestNo, String filter,List<String> requestStatuses,Boolean isSiteVisitRequired,Boolean plannedVisitForToday, String subAreaMasterPk,List<String> talukas);
      
	SearchPageData<TechnicalAssistanceModel> getTechnicalAssistances(SearchPageData searchPageData, String startDate, String endDate, String name, String requestNo,
																	String filters,List<String> status, List<String> constructionAdvisory,List<String> taluka);

	TechnicalAssistanceModel getTechnicalAssistanceForRequestNo(String requestNo);
	EndCustomerComplaintModel getEndCustomerComplaintForRequestNo(String requestId);

	/**
	 *
	 * @param districtSubAreaList
	 * @param site
	 * @return
	 */
	List<SclCustomerModel> getAllCustomerForDistrictSubArea(List<String> districtSubAreaList, BaseSiteModel site);

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
	CustomerComplaintModel getCustomerAssistanceForRequestNo(String requestNo);

	/**
	 *
	 * @param user
	 * @param o
	 * @param o1
	 * @param status
	 * @param key
	 * @return
	 */
	List<CustomerComplaintModel> getCustomerAssistanceRequestList(SclUserModel user, Date startDate, Date endDate, TAServiceRequestStatus status, String key);
  
	NetworkAssistanceModel getNetworkAssistanceForRequestNo(String requestNo);

	Integer countOfAssignedTicketNumbers(SclUserModel user);
	DetailsFromSiteModel getDetailesFromSiteById(String siteId);

	List<EndCustomerComplaintModel> getEndCustomerComplaintsForSLCT(String startDate, String endDate);

	List<EndCustomerComplaintModel> getEndCustomerComplaintClosureRequest();

	/**
	 *
	 * @param taluka
	 * @param district
	 * @param state
	 * @return
	 */
	List<SclUserModel> getTSO(String taluka, String district, String state);

	/**
	 *
	 * @param user
	 * @return
	 */
	Integer countOfTAAssignedTicketNumbers(SclUserModel user);

	EndCustomerComplaintModel getEndCustomerComplaintForRequestNumber(String requestId);

	TechnicalAssistanceModel getTechnicalAssistanceForRequestNumber(String requestNo);

    /**
	  * Fetches the list of talukas of either TA or CC cards for the current user
	  * @param cardType
	  * @param currentUser
	  * @return
	  */
      List<String> getTalukasForTaOrCc(String cardType, UserModel currentUser);

}
