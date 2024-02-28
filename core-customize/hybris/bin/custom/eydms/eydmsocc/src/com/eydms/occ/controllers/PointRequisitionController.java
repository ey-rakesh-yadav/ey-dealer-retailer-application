package com.eydms.occ.controllers;

import com.eydms.facades.PointRequisitionFacade;

import com.eydms.facades.data.*;

import com.eydms.facades.prosdealer.data.DealerListData;
import com.eydms.occ.annotation.ApiBaseSiteIdAndTerritoryParam;
import com.eydms.occ.dto.PointRequisitionListWsDTO;
import com.eydms.occ.dto.dealer.DealerListWsDTO;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.paginated.util.PaginatedSearchUtils;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletResponse;

import java.util.List;

@Controller
@RequestMapping(value = "/{baseSiteId}/influencer/pointReq")
@ApiVersion("v2")
@Tag(name = "Influencer Point Requisition Controller")
public class PointRequisitionController extends EyDmsBaseController {

    @Autowired
    PointRequisitionFacade infPointRequisitionFacade;

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @RequestMapping(value = "/savePointRequisitionDetails", method = RequestMethod.POST)
    @Operation(operationId = "savePointRequisitionDetails", summary = "Save Point Requisition Details")
    @ResponseBody
    @ApiBaseSiteIdParam
    public ResponseEntity<String> saveInfluencerPointRequisitionDetails(@RequestBody PointRequisitionData pointRequisitionData) {
        var requisitionId = infPointRequisitionFacade.saveInfluencerPointRequisitionDetails(pointRequisitionData);
        return ResponseEntity.status(HttpStatus.CREATED).body(requisitionId);
    }


    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/getAllocationRequestCount", method = RequestMethod.GET)
    @Operation(operationId = "getAllocationRequestCount", summary = "Get Allocation Request Count")
    @ResponseBody
    @ApiBaseSiteIdParam
    public Integer getAllocationRequestCount() {
       return infPointRequisitionFacade.getAllocationRequestCount();
    }

    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/getAllocationRequestList", method = RequestMethod.GET)
    @Operation(operationId = "getAllocationRequestList", summary = "Get Allocation Request List")
    @ResponseBody
    @ApiBaseSiteIdParam
    public List<InfluencerPointRequisitionRequestData> getAllocationRequestList() {
        return infPointRequisitionFacade.getAllocationRequestList();
    }

    //Point Requisition -fetch list of products for infleuncer point req with points earned
    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @RequestMapping(value = "/getPointsForPointRequisition", method = RequestMethod.GET)
    @Operation(operationId = "getPointsForInfluencerPointRequisition", summary = "Get Points For Influencer Point Requisition")
    @ResponseBody
    @ApiBaseSiteIdParam
    public Double getPointsForRequisition(@RequestParam String productCode, @RequestParam String influencer) {
        return infPointRequisitionFacade.getPointsForRequisition(productCode,influencer);
    }


    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/influencerCockpitSummary", method = RequestMethod.GET)
    @Operation(operationId = "influencerCockpitSummary", summary = "Influencer Cockpit Details")
    @ResponseBody
    @ApiBaseSiteIdParam
    public RequisitionCockpitData influencerCockpitSummary() {
        return infPointRequisitionFacade.influencerCockpitSummary();

    }


    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/getRequistionDetails", method = RequestMethod.GET)
    @Operation(operationId = "getRequistionDetails", summary = "Get Requisition Details")
    @ResponseBody
    @ApiBaseSiteIdParam
    public PointRequisitionData getRequistionDetails(@RequestParam String requisitionId) {
        return infPointRequisitionFacade.getRequistionDetails(requisitionId);
    }

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @RequestMapping(value = "/getListOfAllPointRequisition", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public PointRequisitionListWsDTO getListOfAllPointRequisition(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
                                                                  @Parameter(description = "Saved as Draft")  @RequestParam (required = false) final boolean isDraft,
                                                                  @Parameter(description = "Partner UID") @RequestParam(required = false) final String filter,
                                                                  @RequestParam (required = false) final List<String> statuses,
                                                                  @RequestParam (required = false) final String requisitionId,
                                                                  @RequestParam (required = false) final String influencerCode,
                                                                  @Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
                                                                  @Parameter(description = "Optional pagination parameter. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,
                                                                  final HttpServletResponse response)
    {
        final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);
        PointRequisitionListData listData = new PointRequisitionListData();
        SearchPageData<PointRequisitionData> respone = infPointRequisitionFacade.getListOfAllPointRequisition(isDraft,filter,statuses,searchPageData, requisitionId, fields, influencerCode);
        listData.setPointRequisitionData(respone.getResults());

        if (respone.getPagination() != null)
        {
            response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(respone.getPagination().getTotalNumberOfResults()));
        }
        return getDataMapper().map(listData, PointRequisitionListWsDTO.class,fields);
    }

    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/giftShopSummary", method = RequestMethod.GET)
    @Operation(operationId = "giftShopSummary", summary = "Gift Shop Details")
    @ResponseBody
    @ApiBaseSiteIdParam
    public GiftShopMessageListData giftShopSummary() {
       /* return infPointRequisitionFacade.giftShopSummary();*/

        return null;
    }
    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value="/searchBox", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public DealerListWsDTO getList(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
                                   @RequestParam(required = false) final String filter,
                                   @Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
                                   @Parameter(description = "Optional pagination parameter. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize)
    {
        final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);
        DealerListData dealerListData=new DealerListData();
        SearchPageData<CustomerData> retailerListForDealerPagination = infPointRequisitionFacade.getList(filter,searchPageData);
        dealerListData.setDealers(retailerListForDealerPagination.getResults());
        return getDataMapper().map(dealerListData,DealerListWsDTO.class, BASIC_FIELD_SET);
    }

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/getCockpitNetworkAdditionListDetails", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "getCockpitNetworkAdditionListDetails", summary = "Get Network addition details for dealer/retailer/influencer ", description = "Get Proposed Plan for Lead")
    @ApiBaseSiteIdAndTerritoryParam
    @ApiResponse(responseCode = "200", description = "get network addition list details")
    public List<NetworkAdditionData> getCockpitNetworkAdditionListDetails() {
        return infPointRequisitionFacade.getCockpitNetworkAdditionListDetails();
    }

    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value="/savedDealerRetailer", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public DealerListWsDTO getSavedDealerRetailer( @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
                                                   @Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
                                                   @Parameter(description = "Optional pagination parameter. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize)
    {
        final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);
        DealerListData dealerListData=new DealerListData();
        SearchPageData<CustomerData> retailerListForDealerPagination = infPointRequisitionFacade.getSavedDealerRetailer(searchPageData);
        dealerListData.setDealers(retailerListForDealerPagination.getResults());
        return getDataMapper().map(dealerListData,DealerListWsDTO.class, BASIC_FIELD_SET);
    }


    //DL/RL - Network- Influencer	Allocation request- cards approver/reject
    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @RequestMapping(value = "/updateAllocationRequestCards", method = RequestMethod.POST)
    @Operation(operationId = "updateAllocationRequestCards", summary = "Update Allocation Request Cards Approve and Reject")
    @ResponseBody
    @ApiBaseSiteIdParam
    public ErrorListWsDTO updateAllocationRequestCards(@RequestParam String requisitionId, @RequestParam String status, @RequestParam (required = false) String rejectionReason) {
        return infPointRequisitionFacade.updateAllocationRequestCards(requisitionId, status, rejectionReason);
    }
}


