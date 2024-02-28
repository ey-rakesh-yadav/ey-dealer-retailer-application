package com.eydms.occ.controllers;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.eydms.facades.data.*;
import com.eydms.facades.prosdealer.data.DealerListData;
import com.eydms.occ.dto.EndCustomerComplaintWsDTO;
import com.eydms.occ.dto.dealer.DealerListWsDTO;
import com.eydms.occ.security.EyDmsSecuredAccessConstants;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commercewebservicescommons.dto.user.UserWsDTO;
import de.hybris.platform.util.Config;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.pagination.WebPaginationUtils;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.eydms.core.model.TechnicalAssistanceModel;
import com.eydms.facades.TechnicalAssistanceFacade;
import com.eydms.occ.dto.NetworkAssitanceListWsDTO;

import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.paginated.util.PaginatedSearchUtils;
import de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import com.eydms.occ.dto.EndCustomerComplaintListWsDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@Controller
@RequestMapping(value = "/{baseSiteId}/users/{userId}/technicalAssistance")
@ApiVersion("v2")
@Tag(name = "TechnicalAssistance")
public class TechnicalAssistanceController extends EyDmsBaseController {

	private static final String MAX_PAGE_SIZE_KEY = "webservicescommons.pagination.maxPageSize";

	@Autowired
	TechnicalAssistanceFacade technicalAssistanceFacade;
	@Resource(name = "dataMapper")
	private DataMapper dataMapper;

	@Resource(name = "webPaginationUtils")
	private WebPaginationUtils webPaginationUtils;
	
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
	public TechnicalAssistanceListWsDTO getTechnicalAssistances(@RequestParam(required = false) final String startDate, @RequestParam(required = false) String endDate, @RequestParam(required = false) String name
			, @RequestParam(required = false) String requestNo,@RequestParam(required = false)String filter
			, @ApiFieldsParam @RequestParam(required = false, defaultValue = FieldSetLevelHelper.DEFAULT_LEVEL) final String fields
			, @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage
			, @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize
			, @RequestParam(name = "needsTotal", required = false, defaultValue = "true") final boolean needsTotal
			, @RequestParam(name = "sort", required = false,defaultValue = "modifiedtime:desc") final String sort
			,final HttpServletResponse response, @RequestParam(required = false) List<String> status, @RequestParam(required = false) List<String> constructionAdvisory)
	{
		final SearchPageData searchPageData = getWebPaginationUtils().buildSearchPageData(sort, currentPage, pageSize, needsTotal);
		recalculatePageSize(searchPageData);
		TechnicalAssistanceListData listData = new TechnicalAssistanceListData();
		SearchPageData<TechnicalAssistanceData> respone = technicalAssistanceFacade.getTechnicalAssistances(searchPageData, startDate, endDate, name, requestNo, filter,status,constructionAdvisory);
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

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/retailersForMaterial", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public DealerListWsDTO getAllRetailersForMaterial(@PathVariable final String userId)
	{
		DealerListData dataList =  technicalAssistanceFacade.getAllRetailersForMaterial(userId);
		return getDataMapper().map(dataList,DealerListWsDTO.class, BASIC_FIELD_SET);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/dealersForMaterial", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public DealerListWsDTO getAllDealersForMaterial(@PathVariable final String userId)
	{
		DealerListData dataList =  technicalAssistanceFacade.getAllDealersForMaterial(userId);
		return getDataMapper().map(dataList,DealerListWsDTO.class, BASIC_FIELD_SET);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/getCustomerByCode", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public UserWsDTO getCustomerByCode(@RequestParam final String customerCode)
	{
		CustomerData customerData = technicalAssistanceFacade.getCustomerByCode(customerCode);
		return getDataMapper().map(customerData, UserWsDTO.class, BASIC_FIELD_SET);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/customerComplaint", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public Boolean submitCustomerComplaint(@RequestBody final CustomerComplaintData customerComplaintData)
	{
		return technicalAssistanceFacade.submitCustomerComplaint(customerComplaintData);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
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

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/getEndCustomerComplaintRequestList", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public EndCustomerComplaintListWsDTO getEndCustomerComplaintRequestList(@RequestParam(required = false) final String startDate, @RequestParam(required = false) String endDate, @RequestParam(required = false) String partnerCode,@RequestParam(required = false) String requestNo,@RequestParam(required = false) String filter,@RequestParam(required = false) final List<String> requestStatuses,@ApiFieldsParam @RequestParam(required = false, defaultValue = FieldSetLevelHelper.DEFAULT_LEVEL) final String fields,@RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,@RequestParam(required = false) final Boolean isSiteVisitRequired,@RequestParam(required = false) final Boolean plannedVisitForToday,@RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,@RequestParam(name = "sort", required = false,defaultValue = "requestraiseddate:desc") final String sort,@RequestParam(name = "needsTotal", required = false, defaultValue = "true") final boolean needsTotal, @RequestParam(required = false) String subAreaMasterPk,final HttpServletResponse response)
	{
		final SearchPageData searchPageData = getWebPaginationUtils().buildSearchPageData(sort, currentPage, pageSize, needsTotal);
		recalculatePageSize(searchPageData);
		EndCustomerComplaintDataList listData = new EndCustomerComplaintDataList();
		SearchPageData<EndCustomerComplaintData> respone = technicalAssistanceFacade.getEndCustomerComplaints(searchPageData, startDate, endDate, partnerCode, requestNo, filter,requestStatuses,isSiteVisitRequired,plannedVisitForToday, subAreaMasterPk);
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



	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
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


	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/updateCustomerComplaint", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public EndCustomerComplaintData updateCustomerComplaint(@Parameter(description = "Complaint Id", required = true) @RequestParam final String complaintId, @RequestParam(required = false) final String soTaggedName, @RequestParam final Boolean isSiteVisitRequired)
	{
		return technicalAssistanceFacade.updateCustomerComplaint(complaintId,soTaggedName,isSiteVisitRequired);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/bookTechnicalExpert", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public EndCustomerComplaintData bookTechnicalExpert(@RequestBody ComplaintTEMeetingData complaintTEMeetingData)
	{
		return technicalAssistanceFacade.bookTechnicalExpert(complaintTEMeetingData);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/submitTicketClosureRequest", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public EndCustomerComplaintData submitTicketClosureRequest(@RequestBody TicketClosureRequestData ticketClosureRequestData)
	{
		return technicalAssistanceFacade.submitTicketClosureRequest(ticketClosureRequestData);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/saveSiteVisitNotRequiredForm", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public EndCustomerComplaintData saveSiteVisitNotRequiredForm(@RequestBody ComplaintSiteVisitNotRequiredData complaintSiteVisitNotRequiredData)
	{
		return technicalAssistanceFacade.saveSiteVisitNotRequiredForm(complaintSiteVisitNotRequiredData);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/saveComplaintDispatchDetails", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public EndCustomerComplaintData saveComplaintDispatchDetails(@RequestBody ComplaintDispatchDetailsData complaintDispatchDetailsData)
	{
		return technicalAssistanceFacade.saveComplaintDispatchDetails(complaintDispatchDetailsData);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/submitNextVisitPlanDetailes", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public EndCustomerComplaintData submitNextVisitPlanDetailes(@RequestBody ScheduleNextVisitData scheduleNextVisitData)
	{
		return technicalAssistanceFacade.submitNextVisitPlanDetailes(scheduleNextVisitData);
	}

	//Count to get open complaints count
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/countOfAssignedTicketNumbers", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public CustomerComplaintHomePageData countOfAssignedTicketNumbers()
	{
		return technicalAssistanceFacade.countOfAssignedTicketNumbers();
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/getComplaintDispatchDetails", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public EndCustomerComplaintData getComplaintDispatchDetails(@RequestParam String complaintId)
	{
		return technicalAssistanceFacade.getComplaintDispatchDetails(complaintId);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/removeComplaintDispatchDetails", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public ComplaintDispatchDetailsData removeComplaintDispatchDetails(@RequestParam String complaintId,@RequestParam String dispatchDetailsId)
	{
		return technicalAssistanceFacade.removeComplaintDispatchDetails(complaintId,dispatchDetailsId);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
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
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/saveDetailsFromSite", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public EndCustomerComplaintData saveDetailsFromSite(@RequestBody DetailsFromSiteData detailsFromSiteData)
	{
		return technicalAssistanceFacade.saveDetailsFromSite(detailsFromSiteData);
	}
	//Api to Save Request for another TSO
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/submitRequestForAnotherTSODetails", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public EndCustomerComplaintData submitRequestForAnotherTSODetails(@RequestParam(required = true) String complaintId,@RequestParam(required = true) String commentForAnotherTSORequest)
	{
		return technicalAssistanceFacade.submitRequestForAnotherTSODetails(complaintId,commentForAnotherTSORequest);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/saveComplaintTestPerformed", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public EndCustomerComplaintData saveComplaintTestPerformed(@RequestBody ComplaintTestPerformedData complaintTestPerformedData)
	{
		return technicalAssistanceFacade.saveComplaintTestPerformed(complaintTestPerformedData);
	}
	
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/rejectTAByTSO", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
    public TechnicalAssistanceData rejectTAByTSO(@RequestParam String requestNo, @RequestParam String rejectedReason) {
    	return technicalAssistanceFacade.rejectTAByTSO(requestNo, rejectedReason);
    }
    
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/acceptTAByTSO", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
    public TechnicalAssistanceData acceptTAByTSO(@RequestParam String requestNo, @RequestParam(required = false) String siteId) {
    	return technicalAssistanceFacade.acceptTAByTSO(requestNo, siteId);
    }
    
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/closeTAByTSO", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
    public TechnicalAssistanceData closeTAByTSO(@RequestParam String requestNo, @RequestParam String closeComment) {
    	return technicalAssistanceFacade.closeTAByTSO(requestNo, closeComment);
    }
}
