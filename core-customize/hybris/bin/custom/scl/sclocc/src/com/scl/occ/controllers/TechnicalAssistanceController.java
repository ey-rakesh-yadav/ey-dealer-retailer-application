package com.scl.occ.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.scl.core.model.SclUserModel;
import com.scl.core.services.TechnicalAssistanceService;
import com.scl.facades.data.*;
import com.scl.facades.prosdealer.data.DealerListData;
import com.scl.occ.annotation.ApiBaseSiteIdAndTerritoryParam;
import com.scl.occ.dto.*;
import com.scl.occ.dto.dealer.DealerListWsDTO;
import com.scl.occ.security.SclSecuredAccessConstants;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commercewebservicescommons.dto.user.UserWsDTO;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.Config;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.pagination.WebPaginationUtils;
import io.swagger.v3.oas.annotations.Parameter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.fest.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.scl.core.model.TechnicalAssistanceModel;
import com.scl.facades.TechnicalAssistanceFacade;

import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.paginated.util.PaginatedSearchUtils;
import de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@Controller
@RequestMapping(value = "/{baseSiteId}/users/{userId}/technicalAssistance")
@ApiVersion("v2")
@Tag(name = "TechnicalAssistance")
public class TechnicalAssistanceController extends SclBaseController {

	private static final Logger LOG = LogManager.getLogger(TechnicalAssistanceController.class);

	private static final String MAX_PAGE_SIZE_KEY = "webservicescommons.pagination.maxPageSize";

	@Autowired
	TechnicalAssistanceFacade technicalAssistanceFacade;
	@Resource(name = "dataMapper")
	private DataMapper dataMapper;

	@Resource(name = "webPaginationUtils")
	private WebPaginationUtils webPaginationUtils;

	@Autowired
	UserService userService;

	@Autowired
	TechnicalAssistanceService technicalAssistanceService;
	
	@RequestMapping(value = "/saveForm", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public Boolean saveForm(@Parameter(description = "userId") @PathVariable(required = true) String userId, @Parameter(description = "Form object.", required = true) @RequestBody final TechnicalAssistanceData data)
	{
		return technicalAssistanceFacade.saveForm(userId, data);
	}
	
	@RequestMapping(value = "/getExpertiseList", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public List<String> getExpertiseListForCurrentConstructionStage(@Parameter(description = "Construction Stage", required = true) @RequestParam final String constructionStage)
	{
		return technicalAssistanceFacade.getExpertiseListForCurrentConstructionStage(constructionStage);
	}
	
	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/networkAssistance", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public NetworkAssitanceListWsDTO getNetworkAssitances(@RequestParam(required = false) final String startDate, @RequestParam(required = false) String endDate, @RequestParam(required = false) String partnerCode
			, @RequestParam(required = false) String requestNo,String filter
			, @ApiFieldsParam @RequestParam(required = false, defaultValue = FieldSetLevelHelper.DEFAULT_LEVEL) final String fields
			, @Parameter(description = "Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage
			, @Parameter(description = "Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize
			,final HttpServletResponse response,@RequestParam(required = false) List<String> status)
	{
		final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);
		NetworkAssitanceListData listData = new NetworkAssitanceListData();
		SearchPageData<NetworkAssitanceData> respone = technicalAssistanceFacade.getNetworkAssitances(searchPageData, startDate, endDate, partnerCode, requestNo, filter,status);
		listData.setNetworkList(respone.getResults());

		if (respone.getPagination() != null)
		{
			response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(respone.getPagination().getTotalNumberOfResults()));
		}
		return getDataMapper().map(listData, NetworkAssitanceListWsDTO.class, fields);
	}	
  
 	@RequestMapping(value = "/technicalAssistanceList", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public TechnicalAssistanceListWsDTO getTechnicalAssistances(@RequestParam(required = false) final String startDate, @RequestParam(required = false) String endDate, @RequestParam(required = false) String name, @RequestParam(required = false) String requestNo, @RequestParam(required = false)String filter, @ApiFieldsParam @RequestParam(required = false, defaultValue = FieldSetLevelHelper.DEFAULT_LEVEL) final String fields
			, @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage
			, @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize
			, @RequestParam(name = "needsTotal", required = false, defaultValue = "true") final boolean needsTotal
			, @RequestParam(name = "sort", required = false,defaultValue = "modifiedtime:desc") final String sort
			, final HttpServletResponse response, @RequestParam(required = false) List<String> status, @RequestParam(required = false) List<String> constructionAdvisory,@RequestParam(required = false) List<String> taluka)
	{
		final SearchPageData searchPageData = getWebPaginationUtils().buildSearchPageData(sort, currentPage, pageSize, needsTotal);
		recalculatePageSize(searchPageData);
		TechnicalAssistanceListData listData = new TechnicalAssistanceListData();
		SearchPageData<TechnicalAssistanceData> respone = technicalAssistanceFacade.getTechnicalAssistances(searchPageData, startDate, endDate, name, requestNo, filter,status,constructionAdvisory,taluka);
		listData.setTechnicalAssistance(respone.getResults());

		if (respone.getPagination() != null)
		{
			response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(respone.getPagination().getTotalNumberOfResults()));
		}
		return getDataMapper().map(listData, TechnicalAssistanceListWsDTO.class, fields);
	}

	@RequestMapping(value = "/technicalAssistanceDetails", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public TechnicalAssistanceData getTechnicalAssistanceRequestDetails(@Parameter(description = "Request Number", required = true) @RequestParam final String requestNo)
	{
		return technicalAssistanceFacade.getTechnicalAssistanceRequestDetails(requestNo);
	}

	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/retailersForMaterial", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public DealerListWsDTO getAllRetailersForMaterial(@PathVariable final String userId)
	{
		DealerListData dataList =  technicalAssistanceFacade.getAllRetailersForMaterial(userId);
		return getDataMapper().map(dataList,DealerListWsDTO.class, BASIC_FIELD_SET);
	}

	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/dealersForMaterial", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public DealerListWsDTO getAllDealersForMaterial(@PathVariable final String userId)
	{
		DealerListData dataList =  technicalAssistanceFacade.getAllDealersForMaterial(userId);
		return getDataMapper().map(dataList,DealerListWsDTO.class, BASIC_FIELD_SET);
	}

	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/getCustomerByCode", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public UserWsDTO getCustomerByCode(@RequestParam final String customerCode)
	{
		CustomerData customerData = technicalAssistanceFacade.getCustomerByCode(customerCode);
		return getDataMapper().map(customerData, UserWsDTO.class, BASIC_FIELD_SET);
	}

	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/customerComplaint", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public Boolean submitCustomerComplaint(@RequestBody final CustomerComplaintData customerComplaintData)
	{
		return technicalAssistanceFacade.submitCustomerComplaint(customerComplaintData);
	}

	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/getInvoiceMaster", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public InvoiceMasterData getInvoiceMaster(@RequestParam final String invoiceNo)
	{
		return technicalAssistanceFacade.getInvoiceMasterByInvoiceNo(invoiceNo);
	}


	@RequestMapping(value = "/customerAssistanceRequestList", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public CustomerAssistanceListData getCustomerAssistanceRequestList(@Parameter(description = "userId") @PathVariable(required = true) String userId, @Parameter(description = "searchKey") @RequestParam(required = false) String searchKey,
																	   @Parameter(description = "year") @RequestParam(required = false) Integer year, @Parameter(description = "month") @RequestParam(required = false) Integer month, @Parameter(description = "status") @RequestParam(required = false) String status)
	{
		return technicalAssistanceFacade.getCustomerAssistanceRequestList(userId, year, month, status, searchKey);
	}

	@RequestMapping(value = "/customerAssistanceDetails", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public CustomerComplaintData getCustomerAssistanceRequestDetails(@Parameter(description = "Request Number", required = true) @RequestParam final String requestNo)
	{
		return technicalAssistanceFacade.getCustomerAssistanceRequestDetails(requestNo);
	}
  
  	@RequestMapping(value = "/saveSOComment", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public Boolean saveSOComment(@Parameter(description = "Request Number", required = true) @RequestParam final String requestNo, @Parameter(description = "Comment By SO", required = true) @RequestParam final String comment)
	{
		return technicalAssistanceFacade.saveSOComment(requestNo, comment);
	}
	
	@RequestMapping(value = "/markAsCompleted", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public Boolean markRequestAsCompleted(@RequestParam final String requestNo, @RequestParam(required = false) String resolvedComment)
	{
		return technicalAssistanceFacade.markRequestAsCompleted(requestNo,resolvedComment);
	}

	@RequestMapping(value = "/submitNetworkAssistanceRequestForm", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public Boolean submitNetworkAssistanceRequestForm(@RequestBody NetworkAssitanceData networkAssitanceData)
	{
		return technicalAssistanceFacade.newRequestForm(networkAssitanceData);
	}

	@ResponseStatus(value = HttpStatus.CREATED)
	@PostMapping(value = "/addEndCustomerDetails", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "AddEndCustomer", summary = "Add EndCustomer")
	@ApiBaseSiteIdAndUserIdParam
	@ResponseBody
	public ResponseEntity<String> addEndCustomerDetails(@RequestBody final EndCustomerComplaintWsDTO wsDto, @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
		var data = dataMapper.map(wsDto, EndCustomerComplaintData.class, fields);
		var id = technicalAssistanceFacade.addEndCustomerDetails(data);
		return ResponseEntity.status(HttpStatus.CREATED).body(id);
	}

	/**
	 * Get Customer Complaint Request List
	 * @param startDate
	 * @param endDate
	 * @param partnerCode
	 * @param requestNo
	 * @param filter
	 * @param requestStatuses
	 * @param talukas
	 * @param fields
	 * @param currentPage
	 * @param isSiteVisitRequired
	 * @param plannedVisitForToday
	 * @param pageSize
	 * @param sort
	 * @param needsTotal
	 * @param subAreaMasterPk
	 * @param response
	 * @return
	 */
	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/getEndCustomerComplaintRequestList", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public EndCustomerComplaintListWsDTO getEndCustomerComplaintRequestList(@RequestParam(required = false) final String startDate, @RequestParam(required = false) String endDate, @RequestParam(required = false) String partnerCode,@RequestParam(required = false) String requestNo,@RequestParam(required = false) String filter,@RequestParam(required = false) final List<String> requestStatuses,@RequestParam(required = false) final List<String> talukas,@ApiFieldsParam @RequestParam(required = false, defaultValue = FieldSetLevelHelper.DEFAULT_LEVEL) final String fields,@RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,@RequestParam(required = false) final Boolean isSiteVisitRequired,@RequestParam(required = false) final Boolean plannedVisitForToday,@RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,@RequestParam(name = "sort", required = false,defaultValue = "requestraiseddate:desc") final String sort,@RequestParam(name = "needsTotal", required = false, defaultValue = "true") final boolean needsTotal, @RequestParam(required = false) String subAreaMasterPk,final HttpServletResponse response)
	{
		final SearchPageData searchPageData = getWebPaginationUtils().buildSearchPageData(sort, currentPage, pageSize, needsTotal);
		recalculatePageSize(searchPageData);
		EndCustomerComplaintDataList listData = new EndCustomerComplaintDataList();
		SearchPageData<EndCustomerComplaintData> respone = technicalAssistanceFacade.getEndCustomerComplaints(searchPageData, startDate, endDate, partnerCode, requestNo, filter,requestStatuses,isSiteVisitRequired,plannedVisitForToday, subAreaMasterPk, talukas);
		listData.setEndCustomerComplaintDataList(respone.getResults());
		if (respone.getPagination() != null)
		{
			response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(respone.getPagination().getTotalNumberOfResults()));
		}
		return getDataMapper().map(listData, EndCustomerComplaintListWsDTO.class, fields);
	}
	protected void recalculatePageSize(final SearchPageData searchPageData)
	{
		int pageSize = searchPageData.getPagination().getPageSize();
		if (pageSize <= 0)
		{
			final int maxPageSize = Config.getInt(MAX_PAGE_SIZE_KEY, 1000);
			pageSize = webPaginationUtils.getDefaultPageSize();
			pageSize = pageSize > maxPageSize ? maxPageSize : pageSize;
			searchPageData.getPagination().setPageSize(pageSize);
		}
	}



	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/getEndCustomerDetailsByRequestId", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public EndCustomerComplaintData getEndCustomerDetailsByRequestId(@Parameter(description = "Request Number", required = true) @RequestParam final String requestNo)
	{
		return technicalAssistanceFacade.getEndCustomerDetailsByRequestId(requestNo,true);
	}

	@RequestMapping(value = "/getEndCustomerComplaintRequestTrackerDetails", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public EndCustomerComplaintData getEndCustomerComplaintRequestTrackerDetails(@Parameter(description = "Request Number", required = true) @RequestParam final String requestNo)
	{
		return technicalAssistanceFacade.getEndCustomerComplaintRequestTrackerDetails(requestNo);
	}


	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/updateCustomerComplaint", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public EndCustomerComplaintData updateCustomerComplaint(@Parameter(description = "Complaint Id", required = true) @RequestParam final String complaintId, @RequestParam(required = false) final String soTaggedName,@RequestParam(required = false) final Boolean isSiteVisitRequired)
	{
		return technicalAssistanceFacade.updateCustomerComplaint(complaintId,soTaggedName,isSiteVisitRequired);
	}

	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/bookTechnicalExpert", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public EndCustomerComplaintData bookTechnicalExpert(@RequestBody ComplaintTEMeetingData complaintTEMeetingData)
	{
		return technicalAssistanceFacade.bookTechnicalExpert(complaintTEMeetingData);
	}

	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/submitTicketClosureRequest", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public EndCustomerComplaintData submitTicketClosureRequest(@RequestBody TicketClosureRequestData ticketClosureRequestData)
	{
		return technicalAssistanceFacade.submitTicketClosureRequest(ticketClosureRequestData);
	}

	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/saveSiteVisitNotRequiredForm", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public EndCustomerComplaintData saveSiteVisitNotRequiredForm(@RequestBody ComplaintSiteVisitNotRequiredData complaintSiteVisitNotRequiredData)
	{
		return technicalAssistanceFacade.saveSiteVisitNotRequiredForm(complaintSiteVisitNotRequiredData);
	}

	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/saveComplaintDispatchDetails", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public EndCustomerComplaintData saveComplaintDispatchDetails(@RequestBody ComplaintDispatchDetailsData complaintDispatchDetailsData)
	{
		return technicalAssistanceFacade.saveComplaintDispatchDetails(complaintDispatchDetailsData);
	}

	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/submitNextVisitPlanDetailes", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public EndCustomerComplaintData submitNextVisitPlanDetailes(@RequestBody ScheduleNextVisitData scheduleNextVisitData)
	{
		return technicalAssistanceFacade.submitNextVisitPlanDetailes(scheduleNextVisitData);
	}

	//Count to get open complaints count
	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/countOfAssignedTicketNumbers", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public CustomerComplaintHomePageData countOfAssignedTicketNumbers()
	{
		return technicalAssistanceFacade.countOfAssignedTicketNumbers();
	}

	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/getComplaintDispatchDetails", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public EndCustomerComplaintData getComplaintDispatchDetails(@RequestParam String complaintId)
	{
		return technicalAssistanceFacade.getComplaintDispatchDetails(complaintId);
	}

	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/removeComplaintDispatchDetails", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public ComplaintDispatchDetailsData removeComplaintDispatchDetails(@RequestParam String complaintId,@RequestParam String dispatchDetailsId)
	{
		return technicalAssistanceFacade.removeComplaintDispatchDetails(complaintId,dispatchDetailsId);
	}

	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/saveComplaintRootCauseIdentifiedForm", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public EndCustomerComplaintData saveComplaintRootCauseIdentifiedForm(@RequestBody ComplaintRootCauseIndentifiedData complaintRootCauseIndentifiedData)
	{
		return technicalAssistanceFacade.saveComplaintRootCauseIdentifiedForm(complaintRootCauseIndentifiedData);
	}

	protected WebPaginationUtils getWebPaginationUtils() {
		return webPaginationUtils;
	}

	//API to save details from Site
	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/saveDetailsFromSite", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public EndCustomerComplaintData saveDetailsFromSite(@RequestBody DetailsFromSiteData detailsFromSiteData)
	{
		return technicalAssistanceFacade.saveDetailsFromSite(detailsFromSiteData);
	}
	//Api to Save Request for another TSO
	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/submitRequestForAnotherTSODetails", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public EndCustomerComplaintData submitRequestForAnotherTSODetails(@RequestParam(required = true) String complaintId,@RequestParam(required = true) String commentForAnotherTSORequest)
	{
		return technicalAssistanceFacade.submitRequestForAnotherTSODetails(complaintId,commentForAnotherTSORequest);
	}

	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/saveComplaintTestPerformed", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public EndCustomerComplaintData saveComplaintTestPerformed(@RequestBody ComplaintTestPerformedData complaintTestPerformedData)
	{
		return technicalAssistanceFacade.saveComplaintTestPerformed(complaintTestPerformedData);
	}
	
	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/rejectTAByTSO", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
    public TechnicalAssistanceData rejectTAByTSO(@RequestParam String requestNo, @RequestParam String rejectedReason) {
    	return technicalAssistanceFacade.rejectTAByTSO(requestNo, rejectedReason);
    }
    
	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/acceptTAByTSO", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
    public TechnicalAssistanceData acceptTAByTSO(@RequestParam String requestNo, @RequestParam(required = false) String siteId) {
    	return technicalAssistanceFacade.acceptTAByTSO(requestNo, siteId);
    }
    
	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/closeTAByTSO", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
    public TechnicalAssistanceData closeTAByTSO(@RequestParam String requestNo, @RequestParam String closeComment) {
    	return technicalAssistanceFacade.closeTAByTSO(requestNo, closeComment);
    }

	/**
	 *
	 * @param taluka
	 * @param district
	 * @param state
	 * @param fields
	 * @return
	 */
	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/tsoUserMapping", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public TSOUserWsDto getTsoUserMapping(@Parameter(description = "taluka") @RequestParam(required = true) String taluka, @Parameter(description = "district") @RequestParam(required = true) String district, @Parameter(description = "state") @RequestParam(required = true) String state,@Parameter(description = "pendingRequest") @RequestParam(required = false) String pendingRequest,
										  @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,final HttpServletResponse response)

	{
		if(StringUtils.isEmpty(taluka)){
			throw new IllegalArgumentException("Please provide Taluka");
		}
		if(StringUtils.isEmpty(district))
		{
			throw new IllegalArgumentException("Please provide District");
		}
		if(StringUtils.isEmpty(state)){
			throw new IllegalArgumentException("Please provide State");
		}
		TSOUserData tsoUserData = technicalAssistanceFacade.getTSO(taluka, district, state,pendingRequest);
		//If no TSOs found for the provided Taluka/District/State, then the HTTP Response code be sent as 204
		if(Collections.isEmpty(tsoUserData.getSclUserData())) {
			SclUserModel doUser = (SclUserModel)  userService.getCurrentUser();
			LOG.warn(String.format("No TSOs found for DO %s in taluke %s, district %s and state %s",doUser.getUid(), taluka, district, state));
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			return null; // Return null to ensure no further processing occurs
		}
		response.setStatus(HttpServletResponse.SC_CREATED);
		return getDataMapper().map(tsoUserData, TSOUserWsDto.class, fields);
	}

	/**
	 * Get Taluka For TechnicalAssitance/EndCustomerComplaint who is assigned or raised by.
	 * @param cardType
	 * @param fields
	 * @returns List of Distinct Taluka's
	 */
	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/talukaTaCc", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public TalukaTaCcWsDTO getTalukaTaCc(@Parameter(description = "cardType") @RequestParam(required = true) String cardType,@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)

	{
		if(StringUtils.isEmpty(cardType)){
			throw new IllegalArgumentException("Please provide cartType");
		}
		TalukaTaCcData talukaTaCcData = technicalAssistanceService.getTalukasForTaOrCc(cardType);
		return getDataMapper().map(talukaTaCcData, TalukaTaCcWsDTO.class, fields);
	}
}
