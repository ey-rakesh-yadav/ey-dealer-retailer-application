package com.eydms.occ.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.eydms.facades.data.*;
import com.eydms.facades.data.marketvisit.MarketVisitDetailsData;
import com.eydms.facades.djp.data.marketvisit.VisitSummaryData;
import com.eydms.facades.dto.marketvisit.MarketVisitDetailsWsDTO;
import com.eydms.facades.dto.marketvisit.VisitSummaryWsDTO;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.microsoft.sqlserver.jdbc.StringUtils;
import com.eydms.facades.DJPVisitFacade;
import com.eydms.facades.djp.data.CounterDetailsData;
import com.eydms.facades.djp.data.DJPCounterScoreData;
import com.eydms.facades.djp.data.DJPRouteScoreData;
import com.eydms.facades.djp.data.ObjectiveData;
import com.eydms.occ.annotation.ApiBaseSiteIdAndUserIdAndTerritoryParam;
import com.eydms.occ.dto.DropdownListWsDTO;
import com.eydms.occ.dto.djp.DJPFinalizedPlanWsDTO;
import com.eydms.occ.dto.djp.VisitMasterListWsDTO;
import com.eydms.occ.security.EyDmsSecuredAccessConstants;

import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.paginated.util.PaginatedSearchUtils;
import de.hybris.platform.webservicescommons.errors.exceptions.WebserviceValidationException;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
	
@Controller
@RequestMapping(value = "/{baseSiteId}/users/{userId}/marketVisit")
@ApiVersion("v2")
@Tag(name = "DJP Market Visit Controller")
public class DJPMarketVisitController extends EyDmsBaseController {


	   @Resource
	   private DJPVisitFacade djpVisitFacade;
	   
	   @Resource(name = "djpVisitPlanDetailsValidator")
	   private Validator djpVisitPlanDetailsValidator;

	   @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP })
	   @RequestMapping(value="/startVisit", method = RequestMethod.GET)
	   @ResponseStatus(value = HttpStatus.CREATED)
	   @ResponseBody
	   @ApiBaseSiteIdAndUserIdAndTerritoryParam
	   public CounterVisitListData startVisit(@RequestParam final String id,
			   @ApiFieldsParam @RequestParam(defaultValue = BASIC_FIELD_SET) final String fields)
	   {

		   CounterVisitListData result = new CounterVisitListData();
		   List<CounterVisitData> counterDetails = djpVisitFacade.getCounterList(id);
		   result.setCounterVisitList(counterDetails);

		   return result;
	   }

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP })
	@RequestMapping(value="/plannedVisit", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public VisitMasterListWsDTO getMarketVisitDetails(@Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
			@Parameter(description = "Optional pagination parameter. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize, final HttpServletResponse response,
			@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		VisitMasterListData result = new VisitMasterListData();
		final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);
		SearchPageData<VisitMasterData> visitDetails = djpVisitFacade.getMarketVisitDetails(searchPageData);
		result.setVisitDetailsList(visitDetails.getResults());

		if (visitDetails.getPagination() != null)
		{
			response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(visitDetails.getPagination().getTotalNumberOfResults()));
		}

		return getDataMapper().map(result, VisitMasterListWsDTO.class,fields);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/counterScoreList", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public List<DJPCounterScoreData> getCounterScoreList(@RequestParam String routeScoreId, @RequestParam String objectiveId)
	{
		return djpVisitFacade.getDJPCounterScores(routeScoreId, objectiveId);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/startVisit", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public boolean startDjpVisit(@RequestParam String visitId) {
		return djpVisitFacade.startDjpVisit(visitId);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/completeVisit", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public boolean completeDjpVisit(@RequestParam String visitId) {
		return djpVisitFacade.completeDjpVisit(visitId);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/visit-summary", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public MarketVisitDetailsWsDTO getVisitSummaryDetails(@Parameter(description = "Visit ID",required = true) @RequestParam final String visitId) {
		 MarketVisitDetailsData marketVisitDetailsData = djpVisitFacade.fetchMarketVisitDetailsData(visitId);
		 return getDataMapper().map(marketVisitDetailsData,MarketVisitDetailsWsDTO.class,DEFAULT_FIELD_SET);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/visit-summary", method = RequestMethod.POST)
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public void saveVisitSummaryDetails(@Parameter(description = "Visit Summary Details", required = true) @RequestBody final VisitSummaryWsDTO visitSummary) {
		VisitSummaryData visitSummaryData = getDataMapper().map(visitSummary,VisitSummaryData.class,DEFAULT_FIELD_SET);
		djpVisitFacade.submitVisitSummaryDetails(visitSummaryData);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/startCounterVisit", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public boolean startCounterVisit(@RequestParam String counterVisitId) {
		return djpVisitFacade.startCounterVisit(counterVisitId);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/completeCounterVisit", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public long completeCounterVisit(@RequestParam String counterVisitId) {
		return djpVisitFacade.completeCounterVisit(counterVisitId);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/djpRouteScores", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public List<DJPRouteScoreData> getDJPRouteScores(@RequestParam String plannedDate, @RequestParam String district, @RequestParam String taluka) {
		return djpVisitFacade.getDJPRouteScores(plannedDate, district, taluka);
	}

	//new djp route score method
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/djpRoutes", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public List<DJPRouteScoreData> getDJPRoutes(@RequestParam String plannedDate, @RequestParam String subAreaMasterPk) {
		return djpVisitFacade.getDJPRouteScores(plannedDate, subAreaMasterPk);
	}
	
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/djpObjectives", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public List<ObjectiveData> getDJPAllObjective(@RequestParam String routeId, @RequestParam(required = false) String routeScoreId) {
		return djpVisitFacade.getDJPObjective(routeId, routeScoreId);
	}	 
	
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/objectives", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public List<ObjectiveData> getDJPObjective(@RequestParam String routeScoreId) {
		return djpVisitFacade.getDJPObjective(routeScoreId);
	}	   
	
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP })
	@RequestMapping(value="/reviewLogs", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public VisitMasterListWsDTO getReviewLogs(@Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
			@Parameter(description = "Optional pagination parameter. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize, final HttpServletResponse response,
			@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
			@RequestParam(required = false) final String startDate, @RequestParam(required = false) final String endDate, @RequestParam(required = false) final String searchKey,@RequestParam(required = true, defaultValue = "false") final Boolean isDjpApprovalWidget)
	{
		if(!(StringUtils.isEmpty(startDate)&&StringUtils.isEmpty(endDate)))
		{
			validateDate(startDate,endDate);
		}

		VisitMasterListData result = new VisitMasterListData();
		final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);
		SearchPageData<VisitMasterData> visitDetails = djpVisitFacade.getReviewLogs(searchPageData,startDate,endDate,searchKey,isDjpApprovalWidget);
		result.setVisitDetailsList(visitDetails.getResults());

		if (visitDetails.getPagination() != null)
		{
			response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(visitDetails.getPagination().getTotalNumberOfResults()));
		}

		return getDataMapper().map(result, VisitMasterListWsDTO.class,fields);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
	@RequestMapping(value="/updateStatusForApprovalByTsm", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public boolean updateStatusForApprovalByTsm(@RequestParam String visitId)
	{
		return djpVisitFacade.updateStatusForApproval(visitId);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
	@RequestMapping(value="/updateStatusForRejectedByTsm", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public boolean updateStatusForRejectedByTsm(@RequestParam String visitId)
	{
		return djpVisitFacade.updateStatusForRejectedByTsm(visitId);
	}

	
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/counterNotVisited", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public Long getCountOfCounterNotVisited() {
		return djpVisitFacade.getCountOfCounterNotVisited();
	}
	
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/totalJouneyPlanned", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public Long getCountOfTotalJouneyPlanned() {
		return djpVisitFacade.getCountOfTotalJouneyPlanned();
	}	
	
	
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/avgTimeSpent", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public Map<String, Double> getAvgTimeSpent() {
		return djpVisitFacade.getAvgTimeSpent();
	}	

    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/adHocExistingCounters", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public List<CounterDetailsData> getAdHocExistingCounters(@RequestBody(required = true) final DJPFinalizedPlanWsDTO plannedData) {
        return djpVisitFacade.getAdHocExistingCounters(plannedData);
    }

    
	private void validateDate(String startDate, String endDate)
	{
		
			final Map<String, String> params = new HashMap<>();
	        params.put("START_DATE_EMPTY_ERROR","StartDate cannot be empty");
	        params.put("END_DATE_EMPTY_ERROR","EndDate cannot be empty");
	        params.put("START_DATE_GREATER_ERROR","StartDate cannot be greater than EndDate");

	        final Errors errors = new MapBindingResult(params, "params");
	        
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			sdf.setLenient(false);
			Date newStartDate = null;
			Date newEndDate = null;
			if(Objects.isNull(startDate))
			{
				errors.rejectValue(params.get("START_DATE_EMPTY_ERROR"),"StartDate cannot be empty");
			}
			else if(Objects.isNull(endDate))
			{
				errors.rejectValue(params.get("END_DATE_EMPTY_ERROR"),"EndDate cannot be empty");
			}
			else
			{
				try {
					newStartDate = sdf.parse(startDate);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				try {
					newEndDate = sdf.parse(endDate);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				if(newStartDate.after(newEndDate))
				{
					errors.rejectValue(params.get("START_DATE_GREATER_ERROR"),"StartDate cannot be greater than EndDate");
				}
			}
			
			if(errors.hasErrors()){
	            throw new WebserviceValidationException(errors);
	        }
		}
	
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/counterNotVisitedList", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public List<CounterDetailsData> getcounterNotVisitedList(@Parameter(description = "Month") @RequestParam(required = false) int month,@Parameter(description = "year") @RequestParam(required = false) int year) {
		return djpVisitFacade.getcounterNotVisitedList(month, year);
	}
	
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/lastSixCounterVisitDates", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public Map<String, String> getLastSixCounterVisitDates(@Parameter(description = "customerId") @RequestParam String customerId) {
		return djpVisitFacade.getLastSixCounterVisitDates(customerId);
	}	

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/counterVisitedForSelectedRoutes", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public Map<String, Object> counterVisitedForSelectedRoutes(@RequestParam String routeScoreId) {
		return djpVisitFacade.counterVisitedForSelectedRoutes(routeScoreId);
	}	
	
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/counterVisitedForRoutes", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public Map<String, Object> counterVisitedForRoutes(@RequestParam String route) {
		return djpVisitFacade.counterVisitedForRoutes(route);
	}	
	
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP })
	@RequestMapping(value="/selectedCounterList", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public CounterVisitListData getSelectedCounterList(@Parameter(description = "id") @RequestParam final String id,
			@ApiFieldsParam @RequestParam(defaultValue = BASIC_FIELD_SET) final String fields)
	{

		CounterVisitListData result = new CounterVisitListData();
		List<CounterVisitData> counterDetails = djpVisitFacade.getSelectedCounterList(id);
		result.setCounterVisitList(counterDetails);

		return result;
	}
	
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,	EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP })
	@RequestMapping(value = "/getLastVisitDate/{counterVisitId}", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	@Operation(operationId = "getLastVisitDate", summary = "Returns the Last visited date", description = "Returns the Last visited date of the user")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public String getLastVisitDate(@Parameter(description = "counterVisitId") @PathVariable String counterVisitId) {
		return djpVisitFacade.getLastVisitDate(counterVisitId);
	}
	
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,	EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP })
	@RequestMapping(value = "/getVisitCountMTD/{counterVisitId}", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	@Operation(operationId = "getVisitCountMTD", summary = "Returns the Visit count MTD", description = "Returns the Visit count for current month")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public Integer getVisitCountMTD(@Parameter(description = "counterVisitId") @PathVariable String counterVisitId) {
		return djpVisitFacade.getVisitCountMTD(counterVisitId);
	}

	
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP })
	@RequestMapping(value="/todaysPlan", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public CounterVisitListData getTodaysPlan(@ApiFieldsParam @RequestParam(defaultValue = BASIC_FIELD_SET) final String fields)
	{
		CounterVisitListData result = new CounterVisitListData();
		List<CounterVisitData> counterDetails = djpVisitFacade.getTodaysPlan();
		result.setCounterVisitList(counterDetails);
		return result;
	}
	
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/route", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public String getRouteForId(@Parameter(description = "id") @RequestParam String id)
	{
		return djpVisitFacade.getRouteForId(id);
	}
	
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/routeList", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public DropdownListWsDTO getListOfRoutes(@Parameter(description = "List of Sub area") @RequestParam(required = false) List<String> subArea, @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		return getDataMapper().map(djpVisitFacade.getListOfRoutes(subArea),DropdownListWsDTO.class,fields);

	}
	
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/journey-detail", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public MarketVisitDetailsWsDTO getVisitJourneyDetails(@Parameter(description = "Visit ID",required = true) @RequestParam final String visitId) {
		 MarketVisitDetailsData marketVisitDetailsData = djpVisitFacade.getVisitJourneyDetails(visitId);
		 return getDataMapper().map(marketVisitDetailsData,MarketVisitDetailsWsDTO.class,DEFAULT_FIELD_SET);
	}
	
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/allObjectives", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public List<ObjectiveData> getAllObjective() {
		return djpVisitFacade.getAllObjective();
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,	EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP })
	@RequestMapping(value = "/getPendingForApprovalVisitsCount", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	@Operation(operationId  = "getPendingForApprovalVisitsCount", summary = "Returns the Pending for apprpval visits count", description = "Returns the Pending for apprpval visits count")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public Integer getPendingForApprovalVisitsCount() {
		return djpVisitFacade.getPendingApprovalVisitsCountForTsmorRh();
	}
}
