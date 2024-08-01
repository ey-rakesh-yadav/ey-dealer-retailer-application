package com.scl.occ.controllers;

import com.scl.core.enums.LeadType;
import com.scl.facades.data.*;
import com.scl.facades.network.SCLNetworkFacade;
import com.scl.facades.network.SCLNewNetworkFacade;
import com.scl.facades.prosdealer.data.DealerListData;
import com.scl.occ.annotation.ApiBaseSiteIdAndTerritoryParam;
import com.scl.occ.annotation.ApiBaseSiteIdAndUserIdAndTerritoryParam;
import com.scl.occ.dto.DealerCurrentNetworkListDto;
import com.scl.occ.dto.InfluencerSummaryListWsDto;
import com.scl.occ.dto.ProposePlanListWsDto;
import com.scl.occ.dto.SCLNetworkAdditionPlanWsDto;
import com.scl.occ.dto.dealer.DealerListWsDTO;
import com.scl.occ.security.SclSecuredAccessConstants;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping(value = "/{baseSiteId}/users/{userId}/sclNewNetwork")
@ApiVersion("v2")
@Tag(name = "SCL New Network Controller")
public class SclNewNetworkController extends SclBaseController{

    @Resource
    private SCLNewNetworkFacade sclNewNetworkFacade;

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getInfluencerDetailedList", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public InfluencerSummaryListWsDto getInfluencerDetailedList(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
                                                                         @RequestBody(required = false) RequestCustomerData requestCustomerData) {
       InfluencerSummaryListData listData = new InfluencerSummaryListData();
        List<InfluencerSummaryData> response = sclNewNetworkFacade.getInfluencerDetailedPaginatedSummaryList(requestCustomerData);

        listData.setInfluncerSummary(response);
        return getDataMapper().map(listData, InfluencerSummaryListWsDto.class, fields);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getRetailerDetailedList", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public DealerCurrentNetworkListDto getRetailerDetailedList(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
                                                                        @RequestBody(required = false) RequestCustomerData requestCustomerData) {

        DealerCurrentNetworkListData listData = new DealerCurrentNetworkListData();
        List<DealerCurrentNetworkData> response = sclNewNetworkFacade.getRetailedDetailedPaginatedSummaryList(requestCustomerData);

        listData.setDealerCurrentNetworkList(response);
        return getDataMapper().map(listData, DealerCurrentNetworkListDto.class, fields);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getDealerDetailedList", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public DealerCurrentNetworkListDto getDealerDetailedList(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
                                                                        @RequestBody(required = false) RequestCustomerData requestCustomerData) {

        DealerCurrentNetworkListData listData = new DealerCurrentNetworkListData();
        List<DealerCurrentNetworkData> response = sclNewNetworkFacade.getDealerDetailedSummaryList(requestCustomerData);

        listData.setDealerCurrentNetworkList(response);
        return getDataMapper().map(listData, DealerCurrentNetworkListDto.class, fields);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getProposalCount", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public Integer getProposalCount(@Parameter(description = "LeadType") @RequestParam final String leadType)
    {
        return sclNewNetworkFacade.getProposalCount(leadType);
    }


    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/searchProposedPlansBySO", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ProposePlanListWsDto getProposedPlansBySO(@Parameter(description = "LeadType") @RequestParam final LeadType leadType,
                                                     @RequestParam(required = false) final String filter)
    {
        var dataList =  sclNewNetworkFacade.getProposedPlansBySO(leadType,filter);
        return getDataMapper().map(dataList,ProposePlanListWsDto.class, BASIC_FIELD_SET);
    }


    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getProposedDealerRetailerPlan", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ProposePlanListWsDto getProposedDealerRetailerPlan(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,@Parameter(description = "LeadType") @RequestParam final LeadType leadType)
    {
        var listdata = sclNewNetworkFacade.getProposedPlanSummaryList(leadType);
        return getDataMapper().map(listdata, ProposePlanListWsDto.class, fields);
    }



    //SclWorkflow - Review By TSM
    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/proposedPlanViewForTSM", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public  SCLNetworkAdditionPlanData proposedPlanViewForTSMRH(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
                                                                     @RequestParam(required = false) final String status, @RequestParam() final String id) {
        return sclNewNetworkFacade.proposedPlanViewForTSMRH(status,id);
    }

   //SclWorkflow - Target Approve By TSM/RH
    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/updateTargetStatusForApprovalNwAddition", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public boolean updateTargetStatusForApprovalNwAddition(@RequestBody SCLNetworkAdditionPlanData salesApprovalData,@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        return sclNewNetworkFacade.updateTargetStatusForApprovalNwAddition(salesApprovalData);
    }

    //SclWorkflow - targetSendForRevision to SO by tsm and DI by rh - TSM and RH persona
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/targetSendForRevisionNwAddition", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public boolean targetSendForRevisionNwAddition(@RequestBody ProposePlanData salesRevisedTargetData,
                                         @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return sclNewNetworkFacade.targetSendForRevisionNwAddition(salesRevisedTargetData);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
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

        ProposePlanListData proposePlanListData = sclNewNetworkFacade.getProposedPlanSummaryListForTSMRH(searchPageData,leadType,statuses,isPendingForApproval);

        return getDataMapper().map(proposePlanListData, ProposePlanListWsDto.class, fields);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="//targetSendToRhShNwAddition", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public boolean targetSendToRhShNwAddition(@Parameter(description = "LeadType") @RequestParam final LeadType leadType)
    {
    	  return sclNewNetworkFacade.targetSendToRhShNwAddition(leadType);
    }


}
