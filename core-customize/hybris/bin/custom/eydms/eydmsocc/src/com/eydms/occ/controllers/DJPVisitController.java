package com.eydms.occ.controllers;

import javax.annotation.Resource;
import javax.validation.Valid;

import com.eydms.facades.data.*;
import com.eydms.facades.djp.data.AddNewSiteData;
import com.eydms.facades.djp.data.CounterDetailsData;
import com.eydms.facades.djp.data.DJPCounterScoreData;
import com.eydms.facades.djp.data.RouteData;
import com.eydms.facades.marketvisit.scheme.SchemeDetailsData;
import com.eydms.occ.dto.djp.CounterVisitAnalyticsWsDTO;
import com.eydms.facades.visit.data.DealerSummaryData;
import com.eydms.facades.visit.data.InfluencerSummaryData;
import com.eydms.facades.visit.data.RetailerSummaryData;
import com.eydms.facades.visit.data.SiteSummaryData;

import com.eydms.occ.dto.djp.UpdateCountersWsDTO;
import com.eydms.occ.dto.djp.DJPFinalizedPlanWsDTO;
import com.eydms.occ.dto.marketvisit.scheme.SchemeDetailsWsDTO;
import com.eydms.occ.dto.order.vehicle.DealerDriverDetailsListWsDTO;
import com.eydms.occ.dto.visit.BrandingInsightListWsDTO;
import com.eydms.occ.dto.visit.BrandingInsightWsDTO;
import com.eydms.occ.dto.visit.LeadMasterWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.OrderWsDTO;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.RequestParameterException;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import com.eydms.facades.DJPVisitFacade;
import com.eydms.occ.annotation.ApiBaseSiteIdAndUserIdAndTerritoryParam;
import com.eydms.occ.dto.CompetitorProductListWsDTO;
import com.eydms.occ.dto.DropdownListWsDTO;
import com.eydms.occ.security.EyDmsSecuredAccessConstants;
import com.eydms.occ.dto.visit.CounterVisitMasterWsDTO;
import com.eydms.occ.dto.visit.DealerFleetWsDTO;

import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;


import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping(value = "/{baseSiteId}/users/{userId}/visit")
@ApiVersion("v2")
@Tag(name = "DJP Visit Form Controller")
public class DJPVisitController extends EyDmsBaseController {

   @Resource
   private DJPVisitFacade djpVisitFacade;

    @Resource(name = "djpFinalizedPlanDetailsValidator")
    private Validator djpFinalizedPlanDetailsValidator;

    @Resource(name = "djpVisitPlanDetailsValidator")
    private Validator djpVisitPlanDetailsValidator;

   @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
   @RequestMapping(value="/competitorProducts", method = RequestMethod.POST)
   @ResponseStatus(value = HttpStatus.CREATED)
   @ResponseBody
   @ApiBaseSiteIdAndUserIdAndTerritoryParam
   public CompetitorProductListWsDTO getCompetitorProducts(@Parameter(description = "brandId") @RequestParam String brandId,@RequestParam(required = false, defaultValue = FieldSetLevelHelper.DEFAULT_LEVEL) final String fields)
   {   	
	return getDataMapper().map(djpVisitFacade.getCompetitorProducts(brandId), CompetitorProductListWsDTO.class, fields);

   }

   @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
   @RequestMapping(value="/{counterVisitId}/marketMappingDetails", method = RequestMethod.POST)
   @ResponseStatus(value = HttpStatus.CREATED)
   @ResponseBody
   @ApiBaseSiteIdAndUserIdAndTerritoryParam
   public boolean submitMarketMappingDetails(@Parameter(description = "counterVisitId") @PathVariable String counterVisitId, @RequestBody CounterVisitMasterData counterVisitMasterData, @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
   {
       counterVisitMasterData.setId(counterVisitId);
       return djpVisitFacade.submitMarketMappingDetails(counterVisitMasterData);
   }

    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/leadGeneration/{counterVisitId}", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public boolean submitLeadGeneration(@Parameter(description = "counterVisitId") @PathVariable String counterVisitId, @Parameter(description = "Lead Details") @RequestBody(required = true) LeadMasterWsDTO leadDto, @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
       LeadMasterData leadMasterData = getDataMapper().map(leadDto, LeadMasterData.class, fields);
       return djpVisitFacade.submitLeadGeneration(leadMasterData, counterVisitId);
    }
    
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/brandInsightDetails/{counterVisitId}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public boolean submitBrandInsightDetails(@Parameter(description = "counterVisitId") @PathVariable String counterVisitId, @Parameter(description = "Branding Insight Details") @RequestBody(required = true) BrandingInsightData brandingInsightData, @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return djpVisitFacade.submitBrandInsightDetails(counterVisitId, brandingInsightData);
    }
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/schemeDetails/{counterVisitId}", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public String submitSchemeDetails(@Parameter(description = "counterVisitId") @PathVariable String counterVisitId,
    		@Parameter(description = "Request body parameter that contains attributes for creating the Scheme details", required = true)
                                    @RequestBody final SchemeDetailsWsDTO schemeDetails)
    {
        final SchemeDetailsData schemeDetailsData = getDataMapper().map(schemeDetails, SchemeDetailsData.class,DEFAULT_FIELD_SET);
        return djpVisitFacade.submitSchemeDetails(counterVisitId, schemeDetailsData);
    }

    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/schemeDocuments/{schemeID}", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public void submitSchemeDocuments(@PathVariable String schemeID,@RequestParam("files") final MultipartFile[] files)
    {
        if(null == files || files.length < 1){
            throw new RequestParameterException("Files should not be empty");
        }
        if(files.length >10){
            throw new RequestParameterException("Files should not be more than 10");
        }
            for(MultipartFile file : files){
                validateDocument(file);
            }

       djpVisitFacade.submitSchemeDocuments(schemeID,files);
    }
    
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/feedback/{counterVisitId}", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public boolean submitFeedbackAndComplaints(@Parameter(description = "counterVisitId") @PathVariable String counterVisitId, @RequestBody FeedbackAndComplaintsListData feedbackAndComplaintsListData)
    {
    	return djpVisitFacade.submitFeedbackAndComplaints(counterVisitId,feedbackAndComplaintsListData);
    }
    
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/{counterVisitId}/flagDealer", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public boolean submitFlagDealer(@Parameter(description = "counterVisitId") @PathVariable String counterVisitId,@Parameter(description = "isFlagged") @RequestParam(required = true) boolean isFlagged,@Parameter(description = "remarkForFlag") @RequestParam(required = true) String remarkForFlag)
    {
    	return djpVisitFacade.submitFlagDealer(counterVisitId, isFlagged, remarkForFlag );
    }
    
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/{counterVisitId}/unflagDealer", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public boolean submitUnflagDealer(@Parameter(description = "counterVisitId") @PathVariable String counterVisitId,@Parameter(description = "isUnFlagged") @RequestParam(required = true) boolean isUnFlagged,@Parameter(description = "remarkForUnflag") @RequestParam(required = true) String remarkForUnflag)
    {
    	return djpVisitFacade.submitUnflagDealer(counterVisitId, isUnFlagged, remarkForUnflag );
    }

   @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
   @RequestMapping(value="/enum/{type}", method = RequestMethod.GET)
   @ResponseStatus(value = HttpStatus.CREATED)
   @ResponseBody
   @ApiBaseSiteIdAndUserIdAndTerritoryParam
   public DropdownListWsDTO getEnumTypes(@Parameter(description = "type") @PathVariable  String type, @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
   {
     return getDataMapper().map(djpVisitFacade.getEnumTypes(type),DropdownListWsDTO.class,fields);
   }
   
   @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
   @RequestMapping(value="/truckList", method = RequestMethod.POST)
   @ResponseBody
   @Operation(operationId = "getAllTrucks", summary = " Truck List", description = "Get list of Truck data")
   @ApiBaseSiteIdAndUserIdAndTerritoryParam
   public ResponseEntity<List<TruckModelData>> getAllTrucks()
   {
       return ResponseEntity.status(HttpStatus.OK).body(djpVisitFacade.getAllTrucks());
   } 

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT, EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/submitTruckFleet/{counterVisitId}", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public boolean submitTruckFleetDetails(@Parameter(description = "counterVisitId") @PathVariable String counterVisitId, @Parameter(description = "Dealer Fleet Details") @RequestBody(required = true) DealerFleetListData dealerFleetListData) {
		return djpVisitFacade.submitTruckFleetDetails(dealerFleetListData, counterVisitId);
	}

    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/{counterVisitId}/counterAggretaion", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public CounterAggregationData getCounterAggregationData(@Parameter(description = "counterVisitId") @PathVariable String counterVisitId)
    {
        return djpVisitFacade.getCounterAggregationData(counterVisitId);
    }

    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/finalize-visit-plan", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public void finalizeVisitPlan(@RequestBody(required = true) final DJPFinalizedPlanWsDTO finalizedPlan){

        //validate(finalizedPlan, "finalizedPlan", djpFinalizedPlanDetailsValidator);
        djpVisitFacade.finalizeCounterVisitPlan(finalizedPlan);
    }


    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/existing-counters", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public List<CounterDetailsData> getExistingCounterDetailsList(@RequestBody(required = true) final DJPFinalizedPlanWsDTO plannedData) {

        //validate(plannedData, "plannedData",djpVisitPlanDetailsValidator);
        return djpVisitFacade.getExistingCounters(plannedData);
    }

    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/add-site", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public void addNewSite(@RequestBody final AddNewSiteData siteData) {

        djpVisitFacade.createAndSaveSiteDetails(siteData);
    }

    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/get-routes", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public List<RouteData> getRoutes() {
        return djpVisitFacade.getRoutesForSalesofficer();
    }

    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/{counterVisitId}/siteVisit", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public boolean submitSiteVisitForm(@Parameter(description = "counterVisitId") @PathVariable String counterVisitId, @RequestBody SiteVisitFormData siteVisitFormData)
    {
        return djpVisitFacade.submitSiteVisitForm(counterVisitId,siteVisitFormData);
    }
	
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,	EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP })
	@RequestMapping(value = "/saveOrderRequisition/{counterVisitId}", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public boolean saveOrderRequisitionForTaggedSites(@Parameter(description = "counterVisitId") @PathVariable String counterVisitId,
			@Parameter(description = "Order Requisition Details") @RequestBody(required = true) OrderRequistionMasterData orderRequisitionData,
			@Parameter(description = "Site code") @RequestParam(required = true) String siteCode) {
		return djpVisitFacade.saveOrderRequisitionForTaggedSites(orderRequisitionData, counterVisitId, siteCode);
	}
	
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/{counterVisitId}/site360", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public SiteSummaryData getSiteSummary(@Parameter(description = "counterVisitId") @PathVariable String counterVisitId)
    {
        return djpVisitFacade.getSiteSummary(counterVisitId);
    }

    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/update-adhoc-counters", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ErrorListWsDTO updateAdHocCounters(@RequestBody(required = true) final UpdateCountersWsDTO updateCountersWsDTO) {
         return djpVisitFacade.updateAdHocCounters(updateCountersWsDTO);
    }

    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/update-counters", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ErrorListWsDTO updateCounters(@RequestBody(required = true) final UpdateCountersWsDTO updateCountersWsDTO) {
        return djpVisitFacade.updateCounters(updateCountersWsDTO);
    }

    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/completed-visits-stats", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public CounterVisitAnalyticsWsDTO getCompletedVisitsData() {
        return djpVisitFacade.getCompletedVisitStatisticsData();
	}
	
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/{counterVisitId}/dealer360", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ResponseEntity<DealerSummaryData> getDealerSummary(@Parameter(description = "counterVisitId") @PathVariable String counterVisitId)
    {
        return ResponseEntity.status(HttpStatus.OK).body(djpVisitFacade.getDealerSummary(counterVisitId));
    }
    
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/retailer360", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ResponseEntity<RetailerSummaryData> getRetailerSummary(@Parameter(description = "customerCode") @RequestParam String customerCode)
    {
    	return ResponseEntity.status(HttpStatus.OK).body(djpVisitFacade.getRetailerSummary(customerCode));
    }
    
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/{counterVisitId}/influencer360", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ResponseEntity<InfluencerSummaryData> getInfluencerSummary(@Parameter(description = "counterVisitId") @PathVariable String counterVisitId)
    {
    	
        return ResponseEntity.status(HttpStatus.OK).body(djpVisitFacade.getInfluencerSummary(counterVisitId));
    }
    
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/{counterVisitId}/dealerVisitForm", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public CounterVisitMasterData getCounterVisitFormDetails(@Parameter(description = "counterVisitId") @PathVariable String counterVisitId)
    {
        return djpVisitFacade.getCounterVisitFormDetails(counterVisitId);
    }
    
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/{counterVisitId}/dealerFleet", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public DealerFleetListData getDealerFleetDetails(@Parameter(description = "counterVisitId") @PathVariable String counterVisitId)
    {
        return djpVisitFacade.getDealerFleetDetails(counterVisitId);
    }

    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/djp-plan-compliance", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public String getDJPPlanComplianceForSalesofficer() {
        return djpVisitFacade.getDJPPlanComplianceForSO();
	}
    
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/{counterVisitId}/influencer", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public EyDmsSiteListData getInfluencerDetails(@Parameter(description = "counterVisitId") @PathVariable String counterVisitId)
    {
        return djpVisitFacade.getInfluencerDetails(counterVisitId);
    }
    
	
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,	EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP })
	@RequestMapping(value = "/getLastSiteVisitFormData/{counterVisitId}", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	@Operation(operationId = "getLastSiteVisitFormData", summary = "Get Last SiteVisitFormData", description = "Get CounterVisitData for last visit Date")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public SiteVisitFormData getLastSiteVisitFormData(@Parameter(description = "counterVisitId") @PathVariable String counterVisitId) {
		return djpVisitFacade.getLastSiteVisitFormData(counterVisitId);
	}
	
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/{counterVisitId}/totalOrderGenerated", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public Double getTotalOrderGenerated(@Parameter(description = "siteCode") @RequestParam String siteCode,@Parameter(description = "counterVisitId") @PathVariable String counterVisitId)
    {
        return djpVisitFacade.getTotalOrderGenerated(siteCode,counterVisitId);
    }

    //Sales History for Dealer 360
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/{counterVisitId}/dealer360SalesHistory", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public SalesHistoryData getSalesHistoryForDealer(@PathVariable String counterVisitId)
    {
		SalesHistoryData data = djpVisitFacade.getSalesHistoryForDealer(counterVisitId);
		return data;
    }
	
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/{counterVisitId}/dealer360Last6MonthSales", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public List<MonthlySalesData> getLastSixMonthSalesForDealer(@PathVariable String counterVisitId)
    {
        return djpVisitFacade.getLastSixMonthSalesForDealer(counterVisitId);
    }

    //Sales History for Retailer and Influencer 360
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/{counterVisitId}/retailerAndInflu360SalesHistory", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public SalesHistoryData getSalesHistoryFor360(@PathVariable String counterVisitId)
    {
        SalesHistoryData data = djpVisitFacade.getSalesHistoryDataFor360(counterVisitId);
        return data;
    }

    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/{counterVisitId}/retailer360Last6MonthSales", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public List<MonthlySalesData> getLastSixMonthSalesForRetailer(@PathVariable String counterVisitId)
    {
        return djpVisitFacade.getLastSixMonthSalesForRetailer(counterVisitId);
    }
    
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/saveCoordinates", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public Boolean saveCustomerCoordinates(@RequestParam String customerId, @RequestParam Double latitude, @RequestParam Double longitude)
    {
        return djpVisitFacade.saveCustomerCoordinates(customerId, latitude, longitude);
    }

    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/flaggedDealerCount", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public Integer flaggedDealerCount()
    {
        return djpVisitFacade.flaggedDealerCount();
    }

    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/unFlaggedDealerRequestCount", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public Integer unFlaggedDealerRequestCount()
    {
        return djpVisitFacade.unFlaggedDealerRequestCount();
    }

    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/updateUnFlagRequestApprovalByTSM", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public boolean updateUnFlagRequestApprovalByTSM(@RequestBody UnFlagRequestApprovalData unFlagRequestApprovalData)
    {
        return djpVisitFacade.updateUnFlagRequestApprovalByTSM(unFlagRequestApprovalData);
    }

    //SO app task - DJP Compliance
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/djpCompliance", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public Double getDJPCompliance() {
        return djpVisitFacade.getDJPCompliance();
    }
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getPartnerType", method = RequestMethod.POST)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public DropdownListWsDTO getPartnerType(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return getDataMapper().map(djpVisitFacade.getPartnerType(),DropdownListWsDTO.class,fields);

    }
}