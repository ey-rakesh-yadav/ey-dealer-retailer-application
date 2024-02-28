package com.eydms.core.dao;

import java.util.Date;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.eydms.core.enums.TACurrentConstructionStage;
import com.eydms.core.enums.TAExpertise;
import com.eydms.core.enums.TAServiceRequestStatus;
import com.eydms.core.model.*;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

public interface TechnicalAssistanceDao {

	List<TAExpertise> getExpertiseListForCurrentConstructionStage(TACurrentConstructionStage constructionStage);

	SearchPageData<NetworkAssistanceModel> getNetworkAssitances(SearchPageData searchPageData, String startDate, String endDate, String partnerCode, String requestNo,
			String filters,List<String> status);
	SearchPageData<EndCustomerComplaintModel> getEndCustomerComplaints(SearchPageData searchPageData, String startDate, String endDate, String partnerCode
			, String requestNo, String filter,List<String> requestStatuses,Boolean isSiteVisitRequired,Boolean plannedVisitForToday, String subAreaMasterPk);
      
	SearchPageData<TechnicalAssistanceModel> getTechnicalAssistances(SearchPageData searchPageData, String startDate, String endDate, String name, String requestNo,
																	String filters,List<String> status, List<String> constructionAdvisory);

	TechnicalAssistanceModel getTechnicalAssistanceForRequestNo(String requestNo);
	EndCustomerComplaintModel getEndCustomerComplaintForRequestNo(String requestId);

	/**
	 *
	 * @param districtSubAreaList
	 * @param site
	 * @return
	 */
	List<EyDmsCustomerModel> getAllCustomerForDistrictSubArea(List<String> districtSubAreaList, BaseSiteModel site);

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
	List<CustomerComplaintModel> getCustomerAssistanceRequestList(EyDmsUserModel user, Date startDate, Date endDate, TAServiceRequestStatus status, String key);
  
	NetworkAssistanceModel getNetworkAssistanceForRequestNo(String requestNo);

	Integer countOfAssignedTicketNumbers();
	DetailsFromSiteModel getDetailesFromSiteById(String siteId);

	List<EndCustomerComplaintModel> getEndCustomerComplaintsForSLCT(String startDate, String endDate);

	List<EndCustomerComplaintModel> getEndCustomerComplaintClosureRequest();
}
