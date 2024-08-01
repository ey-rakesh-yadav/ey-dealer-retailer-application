package com.scl.occ.controllers;

import java.text.ParseException;
import java.util.List;

import com.scl.facades.data.*;
import com.scl.occ.annotation.ApiBaseSiteIdAndTerritoryParam;
import com.scl.occ.dto.InfluencersDetails360WsDTO;
import com.scl.occ.dto.ScheduledMeetListWsDTO;
import com.scl.occ.dto.SclLeadListWsDTO;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import com.scl.facades.InfluencerFacade;
import com.scl.facades.data.InfluencerManagementHomePageData;
import com.scl.occ.dto.CustomerCardListWsDTO;
import com.scl.occ.security.SclSecuredAccessConstants;


import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@RequestMapping(value = "/{baseSiteId}/sclInfluencer")
@ApiVersion("v2")
@Tag(name = "SCL Influencer Controller")
public class SclInfluencerController extends SclBaseController{
	
	@Autowired
	InfluencerFacade influencerFacade;

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/searchOnboardingPartner", method = RequestMethod.GET)
    @Operation(operationId = "SearchOnboardingPartner", summary = "Search Onboarding Partner")
	@ResponseBody
    @ApiBaseSiteIdParam
    public OnboardingPartnerData searchOnboardingPartner(@Parameter(description = "searchKey") @RequestParam List<String> searchKey) throws Exception {
		return influencerFacade.searchOnboardingPartner(searchKey);
	}
	
	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/assignOnboardingPartner", method = RequestMethod.POST)
    @Operation(operationId = "assignOnboardingPartner", summary = "Assign Onboarding Partner")
	@ResponseBody
    @ApiBaseSiteIdParam
    public Boolean assignOnboardingPartner(@Parameter(description = "influencerUid") @RequestParam String influencerUid, @Parameter(description = "partnerUid") @RequestParam(required = false) String partnerUid) {
		return influencerFacade.assignOnboardingPartner(influencerUid, partnerUid);
	}
	
	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/getOnboardingStatus", method = RequestMethod.GET)
    @Operation(operationId = "getOnboardingStatus", summary = "Get Onboarding Status")
	@ResponseBody
    @ApiBaseSiteIdParam
    public String getOnboardingStatus(@Parameter(description = "influencerUid") @RequestParam String influencerUid) {
		return influencerFacade.getOnboardingStatus(influencerUid);
	}

    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/infCockpitSchemeStatus", method = RequestMethod.GET)
    @Operation(operationId = "infCockpitSchemeStatus", summary = "Influencer Cockpit Scheme Status")
    @ResponseBody
    @ApiBaseSiteIdParam
    public InfCockpitSchemeStatusData getGiftPointsStatus() throws CMSItemNotFoundException, ConversionException, ParseException {
        return influencerFacade.getGiftPointsStatus();
    }
    
    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/editProfile", method = RequestMethod.POST)
    @Operation(operationId = "editProfile", summary = "Edit Customer Profile")
    @ResponseBody
    @ApiBaseSiteIdParam
    public Boolean editProfile(@Parameter(description = "ProfileData") @RequestBody SclCustomerData data) {
        return influencerFacade.editProfile(data);
    }

    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/fetchOnboardingPartnerDetails", method = RequestMethod.GET)
    @Operation(operationId = "fetchOnboardingPartnerDetails", summary = "Fetch Onboarding Partner")
    @ResponseBody
    @ApiBaseSiteIdParam
    public OnboardingPartnerData fetchOnboardingPartnerDetails(@RequestParam String influencerUid)  {
        return influencerFacade.fetchOnboardingPartnerDetails(influencerUid);
    }

    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/submitMeetingCompletionForm", method = RequestMethod.POST)
    @Operation(operationId = "submitMeetingCompletionForm", summary = "submit Meeting Completion Form")
    @ResponseBody
    @ApiBaseSiteIdParam
    public Boolean submitMeetingCompletionForm(@RequestBody MeetingCompletionFormData data) {
        return influencerFacade.submitMeetingCompletionForm(data);
    }
    

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/influencerManagementHomePage", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdParam
    public InfluencerManagementHomePageData getInfluencerManagementHomePage(@RequestParam(required = false) String influencerType) {
        return influencerFacade.getInfluencerManagementHomePage(influencerType);
    }
    

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/influencerOnboardingList", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public CustomerCardListWsDTO getInfluencerOnboardingList(@RequestParam(name = "currentPage", required = false, defaultValue = "0") final int currentPage,
                                                   @RequestParam(name = "pageSize", required = false, defaultValue = "10") final int pageSize,
                                                   @RequestParam(name = "sort", defaultValue = "creationtime:desc") final String sort,
                                                   @RequestParam(name = "needsTotal", required = false, defaultValue = "true") final boolean needsTotal,
                                                   @RequestParam(defaultValue = "DEFAULT") final String fields,
                                                   @RequestBody RequestCustomerData customerRequestData) {

    	final SearchPageData searchPageData = webPaginationUtils.buildSearchPageData(sort, currentPage, pageSize, needsTotal);
    	recalculatePageSize(searchPageData);
    	CustomerCardListData customerCardListData = influencerFacade.getInfluencerOnboardingList(searchPageData,customerRequestData);
    	return getDataMapper().map(customerCardListData, CustomerCardListWsDTO.class,fields);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getLeadList", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public SclLeadListWsDTO getLeadList(@RequestParam(name = "currentPage", required = false, defaultValue = "0") final int currentPage,
                                        @RequestParam(name = "pageSize", required = false, defaultValue = "10") final int pageSize,
                                        @RequestParam(name = "sort", defaultValue = "modifiedtime:desc") final String sort,
                                        @RequestParam(name = "needsTotal", required = false, defaultValue = "true") final boolean needsTotal,
                                        @RequestParam(defaultValue = "DEFAULT") final String fields, @RequestBody LeadRequestData leadRequestData) {

        final SearchPageData searchPageData = webPaginationUtils.buildSearchPageData(sort, currentPage, pageSize, needsTotal);
        recalculatePageSize(searchPageData);

        SclLeadListData sclLeadListData = new SclLeadListData();
        SearchPageData<SclLeadData> paginatedLeadList = influencerFacade.getPaginatedLeadList(searchPageData, leadRequestData);
        sclLeadListData.setLeads(paginatedLeadList.getResults());
        return getDataMapper().map(sclLeadListData, SclLeadListWsDTO.class, fields);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getInfluencerDetailsFor360", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public InfluencersDetails360WsData getInfluencerDetailsFor360(@Parameter(description = "influencer code") @RequestParam(required = true) final String influencerCode,
                                                       @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
       return influencerFacade.getInfluencerDetailsFor360(influencerCode);
       // return getDataMapper().map(data, InfluencersDetails360WsDTO.class, fields);
    }



    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_CUSTOMERGROUP"})
    @GetMapping(value="/scheduleMeetList")
    @ResponseBody
    @ApiBaseSiteIdAndTerritoryParam
    public ScheduledMeetListWsDTO getScheduleMeetList(@RequestParam(name = "meetingType", required = false) final String meetingType,
                                                      @RequestParam(name = "startDate", required = false) final String startDate,
                                                      @RequestParam(name = "endDate", required = false) final String endDate,
                                                      @RequestParam(name = "search", required = false) final String searchFilter,
                                                      @RequestParam(name = "status", required = false) final List<String> status,
                                                      @RequestParam(name = "currentPage", required = false, defaultValue = "0") final int currentPage,
                                                      @RequestParam(name = "pageSize", required = false, defaultValue = "10") final int pageSize,
                                                      @RequestParam(name = "sort", defaultValue = "eventdate:desc") final String sort,
                                                      @RequestParam(name = "needsTotal", required = false, defaultValue = "true") final boolean needsTotal,
                                                      @ApiFieldsParam @RequestParam(defaultValue =
                                                              DEFAULT_FIELD_SET) final String fields
                                                     ) {
        final SearchPageData searchPageData = webPaginationUtils.buildSearchPageData(sort, currentPage, pageSize, needsTotal);
        recalculatePageSize(searchPageData);
        ScheduledMeetListData scheduledMeetListData = new ScheduledMeetListData();
        SearchPageData<ScheduledMeetData> paginatedLeadList = influencerFacade.getPaginatedScheduleMeetList(searchPageData,meetingType,startDate,endDate,searchFilter,status);
        scheduledMeetListData.setMeetCards(paginatedLeadList.getResults());
        return getDataMapper().map(scheduledMeetListData, ScheduledMeetListWsDTO.class, fields);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/updateLeadStatus", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdParam
    public SclLeadData updateLeadStatus(@RequestParam String leadId, @RequestParam String status, @RequestParam(required = false) String rejectedComment) {
        return influencerFacade.updateLeadStatus(leadId,status,rejectedComment);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/viewLeadDetailsById", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdParam
    public SclLeadData viewLeadDetailsById(@RequestParam String leadId) {
        return influencerFacade.viewLeadDetailsById(leadId);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/saveInfluencerVisitForm", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdParam
    public InfluencerVisitData saveInfluencerVisitForm(@RequestBody InfluencerVisitData influencerVisitData) {
        return influencerFacade.saveInfluencerVisitForm(influencerVisitData);
    }
  
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getMeetingCompletionFormDetail", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdParam
    public ScheduledMeetData getMeetingCompletionFormDetail(@RequestParam String scheduleMeetId) {
        return influencerFacade.getMeetingCompletionFormDetail(scheduleMeetId);

    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getInfluencerVisitHistory", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdParam
    public InfluencerVisitHistoryListData getInfluencerVisitHistory(@RequestParam(required = false) String filter) {
        return influencerFacade.getInfluencerVisitHistory(filter);
    }
}
