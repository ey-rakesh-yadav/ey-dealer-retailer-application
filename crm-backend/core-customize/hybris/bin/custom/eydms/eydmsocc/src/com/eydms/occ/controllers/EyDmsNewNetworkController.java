package com.eydms.occ.controllers;

import com.eydms.core.enums.LeadType;
import com.eydms.facades.data.*;
import com.eydms.facades.network.EYDMSNewNetworkFacade;
import com.eydms.facades.prosdealer.data.DealerListData;
import com.eydms.occ.annotation.ApiBaseSiteIdAndUserIdAndTerritoryParam;
import com.eydms.occ.dto.DealerCurrentNetworkListDto;
import com.eydms.occ.dto.InfluencerSummaryListWsDto;
import com.eydms.occ.dto.ProposePlanListWsDto;
import com.eydms.occ.dto.EYDMSNetworkAdditionPlanWsDto;
import com.eydms.occ.dto.dealer.DealerListWsDTO;
import com.eydms.occ.security.EyDmsSecuredAccessConstants;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.paginated.util.PaginatedSearchUtils;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping(value = "/{baseSiteId}/users/{userId}/eydmsNewNetwork")
@ApiVersion("v2")
@Tag(name = "EYDMS New Network Controller")
public class EyDmsNewNetworkController extends EyDmsBaseController {

    @Resource
    private EYDMSNewNetworkFacade eydmsNewNetworkFacade;

    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getInfluencerDetailedList", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public InfluencerSummaryListWsDto getInfluencerDetailedList(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
                                                                         @RequestBody(required = false) RequestCustomerData requestCustomerData) {
       InfluencerSummaryListData listData = new InfluencerSummaryListData();
        List<InfluencerSummaryData> response = eydmsNewNetworkFacade.getInfluencerDetailedPaginatedSummaryList(requestCustomerData);

        listData.setInfluncerSummary(response);
        return getDataMapper().map(listData, InfluencerSummaryListWsDto.class, fields);
    }

    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getRetailerDetailedList", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public DealerCurrentNetworkListDto getRetailerDetailedList(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
                                                                        @RequestBody(required = false) RequestCustomerData requestCustomerData) {

        DealerCurrentNetworkListData listData = new DealerCurrentNetworkListData();
        List<DealerCurrentNetworkData> response = eydmsNewNetworkFacade.getRetailedDetailedPaginatedSummaryList(requestCustomerData);

        listData.setDealerCurrentNetworkList(response);
        return getDataMapper().map(listData, DealerCurrentNetworkListDto.class, fields);
    }

    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getDealerDetailedList", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public DealerCurrentNetworkListDto getDealerDetailedList(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
                                                                        @RequestBody(required = false) RequestCustomerData requestCustomerData) {

        DealerCurrentNetworkListData listData = new DealerCurrentNetworkListData();
        List<DealerCurrentNetworkData> response = eydmsNewNetworkFacade.getDealerDetailedSummaryList(requestCustomerData);

        listData.setDealerCurrentNetworkList(response);
        return getDataMapper().map(listData, DealerCurrentNetworkListDto.class, fields);
    }

    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getProposalCount", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public Integer getProposalCount(@Parameter(description = "LeadType") @RequestParam final String leadType)
    {
        return eydmsNewNetworkFacade.getProposalCount(leadType);
    }


    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/searchProposedPlansBySO", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ProposePlanListWsDto getProposedPlansBySO(@Parameter(description = "LeadType") @RequestParam final LeadType leadType,
                                                     @RequestParam(required = false) final String filter)
    {
        var dataList =  eydmsNewNetworkFacade.getProposedPlansBySO(leadType,filter);
        return getDataMapper().map(dataList,ProposePlanListWsDto.class, BASIC_FIELD_SET);
    }


    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getProposedDealerRetailerPlan", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ProposePlanListWsDto getProposedDealerRetailerPlan(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,@Parameter(description = "LeadType") @RequestParam final LeadType leadType)
    {
        var listdata = eydmsNewNetworkFacade.getProposedPlanSummaryList(leadType);
        return getDataMapper().map(listdata, ProposePlanListWsDto.class, fields);
    }



    //EyDmsWorkflow - Review By TSM
    @Secured({EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT, EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/proposedPlanViewForTSM", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public  EYDMSNetworkAdditionPlanData proposedPlanViewForTSMRH(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
                                                                     @RequestParam(required = false) final String status, @RequestParam() final String id) {
        return eydmsNewNetworkFacade.proposedPlanViewForTSMRH(status,id);
    }

   //EyDmsWorkflow - Target Approve By TSM/RH
    @Secured({EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT, EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/updateTargetStatusForApprovalNwAddition", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public boolean updateTargetStatusForApprovalNwAddition(@RequestBody EYDMSNetworkAdditionPlanData salesApprovalData,@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        return eydmsNewNetworkFacade.updateTargetStatusForApprovalNwAddition(salesApprovalData);
    }

    //EyDmsWorkflow - targetSendForRevision to SO by tsm and DI by rh - TSM and RH persona
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/targetSendForRevisionNwAddition", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public boolean targetSendForRevisionNwAddition(@RequestBody ProposePlanData salesRevisedTargetData,
                                         @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return eydmsNewNetworkFacade.targetSendForRevisionNwAddition(salesRevisedTargetData);
    }

    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getProposedDealerRetailerPlanForTSMRH", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ProposePlanListWsDto getProposedDealerRetailerPlanForTSMRH(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,@RequestParam(required = false) String filter,@Parameter(description = "LeadType") @RequestParam final LeadType leadType,
    		@Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage, @RequestParam (required = false) final List<String> statuses,
    		@Parameter(description = "Pending for approval")  @RequestParam (required = false) final boolean isPendingForApproval,
    		@Parameter(description = "Optional {@link PaginationData} parameter in case of savedCartsOnly == true. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,final HttpServletResponse response)
    {
        final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);

        ProposePlanListData proposePlanListData = eydmsNewNetworkFacade.getProposedPlanSummaryListForTSMRH(searchPageData,leadType,statuses,isPendingForApproval);

        return getDataMapper().map(proposePlanListData, ProposePlanListWsDto.class, fields);
    }

    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="//targetSendToRhShNwAddition", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public boolean targetSendToRhShNwAddition(@Parameter(description = "LeadType") @RequestParam final LeadType leadType)
    {
    	  return eydmsNewNetworkFacade.targetSendToRhShNwAddition(leadType);
    }


}
