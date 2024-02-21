package com.eydms.occ.controllers;

import com.eydms.facades.BrandingFacade;
import com.eydms.facades.data.*;
import com.eydms.facades.order.data.BrandingTrackingListData;
import com.eydms.facades.prosdealer.data.DealerListData;
import com.eydms.occ.dto.BrandingRequestDetailsListWsDTO;
import com.eydms.occ.dto.DropdownListWsDTO;
import com.eydms.occ.dto.dealer.DealerListWsDTO;
import com.eydms.occ.security.EyDmsSecuredAccessConstants;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.paginated.util.PaginatedSearchUtils;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.List;
import io.swagger.v3.oas.annotations.Parameter;
@Controller
@RequestMapping(value = "/{baseSiteId}/users/{userId}/branding")
@ApiVersion("v2")
@Tag(name = "Branding Controller")

public class BrandingController  extends EyDmsBaseController {

    @Resource
    BrandingFacade brandingFacade;

    //Getting Branding Site Type Drop Down
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/enum/{type}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public DropdownListWsDTO getEnumTypes(@Parameter(description = "Type") @PathVariable String type, @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return getDataMapper().map(brandingFacade.getEnumTypes(type),DropdownListWsDTO.class,fields);
    }

    //Search for the counter -code/name/no  - 1) Autopopulate counter details(name,code,location(address),primary contact no)
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/getCounterDetailsForPointOfSale", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public BrandingRequestDetailsData getCounterDetailsForPointOfSale(String searchKeyword)
    {
        return brandingFacade.getCounterDetailsForPointOfSale(searchKeyword);
    }

    //Submit POS Request form & (SAVE) Enter secondary contact no, Quantity, dimensions, Details &) & Requisition submitted popup
    //Upload photos before branding activity performed - camera capture
    //Submit Outdoors Request form & Submit Dealer cost Request form
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/submitBrandingRequisition", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public boolean submitBrandingRequisition(@RequestBody BrandingRequestDetailsData brandingRequestDetailsData,
                                                      @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return brandingFacade.submitBrandingRequisition(brandingRequestDetailsData);
    }

    //Search bar for searching for a specific branding requisition and also retrive all branding requisition
    //Displaying list of card list and Request Tile details
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/getBrandingRequestDetails", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public BrandingRequestDetailsListWsDTO getBrandingRequestDetails(@RequestParam(required = false) String filter, @RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate,@RequestParam(required = false) List<String> requestStatus, @RequestParam(required = false) List<String> brandingSiteType,
                                                                     @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields, @Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
                                                                     @Parameter(description = "Optional {@link PaginationData} parameter in case of savedCartsOnly == true. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,
                                                                     final HttpServletResponse response) throws ParseException {
        BrandingRequestDetailsListData brandingRequestDetailsListData=new BrandingRequestDetailsListData();
        final SearchPageData<Object> searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);
        SearchPageData<BrandingRequestDetailsData> requestDetails = brandingFacade.getBrandingRequestDetails(filter,startDate,endDate,requestStatus,brandingSiteType,searchPageData);
        brandingRequestDetailsListData.setBrandingRequestDetails(requestDetails.getResults());
        if (requestDetails.getPagination() != null)
        {
            response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(requestDetails.getPagination().getTotalNumberOfResults()));
        }
        return getDataMapper().map(brandingRequestDetailsListData, BrandingRequestDetailsListWsDTO.class, fields);
    }

    //Select branding type dropdown -for POS
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/getBrandingSiteTypeDropDownList/{brandType}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public DropdownListWsDTO getBrandingSiteTypeDropDownList(@Parameter(description = "Brand Type") @PathVariable String brandType, @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return getDataMapper().map(brandingFacade.getBrandingSiteTypeDropDownList(brandType),DropdownListWsDTO.class,fields);
    }

    //Activity verification' details - Verify - Submit & save api form & popup
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/submitActivityVerificationDetailsForRequest", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public boolean submitActivityVerificationDetailsForRequest(@RequestBody BrandingRequestDetailsData brandingRequestDetails,
                                                      @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return brandingFacade.submitActivityVerificationDetailsForRequest(brandingRequestDetails);
    }

    //Activity verification' button - Requisition details auto-populate , enter Quantity,dimension, details, photo before branding
    //getBranding req data
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/getBrandReqDetailsForActivityVerification", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public BrandingRequestDetailsData getBrandReqDetailsForActivityVerification(String requisitionNumber)
    {
        return brandingFacade.getBrandReqDetailsForActivityVerification(requisitionNumber);
    }

    //Activity details' button - Autopopulate Requisition details & Activity verification details with back button
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/getActivityDetailsForRequest", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public  BrandingRequestDetailsData getActivityDetailsForRequest(@RequestParam(required = true) String requisitionNumber)
    {
        return brandingFacade.getActivityDetailsForRequest(requisitionNumber);
    }

    //Get all Dealer and Retailer list on Search
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getAllDealerRetailerList", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public DealerListWsDTO getAllDealerRetailerList(@RequestParam(required = false) boolean isDealer,@RequestParam(required = false) boolean isRetailer)
    {
        DealerListData dataList =  brandingFacade.getAllDealerRetailerList(isDealer,isRetailer);
        return getDataMapper().map(dataList,DealerListWsDTO.class, BASIC_FIELD_SET);
    }

    //Get all Dealer - Search on DealerCost Sharing Branding
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getAllDealersForSubArea", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public DealerListWsDTO getAllDealersForSubArea()
    {
        DealerListData dataList =  brandingFacade.getAllDealersForSubArea();
        return getDataMapper().map(dataList,DealerListWsDTO.class, BASIC_FIELD_SET);
    }

    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/states", method = RequestMethod.GET)
    @ResponseBody
    @Operation(operationId = "getAllState", summary = " State List", description = "Get list of States")
    @ApiBaseSiteIdAndUserIdParam
    public DropdownListWsDTO getAllState()
    {
        return getDataMapper().map(brandingFacade.findAllState(),DropdownListWsDTO.class,BASIC_FIELD_SET);
    }

    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/districts/{state}", method = RequestMethod.GET)
    @ResponseBody
    @Operation(operationId = "getAllDistrict", summary = " District List", description = "Get list of district")
    @ApiBaseSiteIdAndUserIdParam
    public DropdownListWsDTO getAllDistrict(@Parameter(description = "State") @PathVariable final String state)
    {
        return getDataMapper().map(brandingFacade.findAllDistrict(state),DropdownListWsDTO.class,BASIC_FIELD_SET);
    }

    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/talukas/{state}/districtName/{district}", method = RequestMethod.GET)
    @ResponseBody
    @Operation(operationId = "getAllTaluka", summary = " Taluka List", description = "Get list of taluka")
    @ApiBaseSiteIdAndUserIdParam
    public DropdownListWsDTO getAllTaluka(@Parameter(description = "State") @PathVariable final String state, @Parameter(description = "District") @PathVariable final String district)
    {
        return getDataMapper().map(brandingFacade.findAllTaluka(state, district),DropdownListWsDTO.class,BASIC_FIELD_SET);
    }

    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/erpCities/{state}/districtName/{district}/talukaName/{taluka}", method = RequestMethod.GET)
    @ResponseBody
    @Operation(operationId = "getAllErpCities", summary = " ErpCity List", description = "Get list of erpCity")
    @ApiBaseSiteIdAndUserIdParam
    public DropdownListWsDTO getAllErpCity(@Parameter(description = "State") @PathVariable final String state, @Parameter(description = "District") @PathVariable final String district,@Parameter(description = "Taluka") @PathVariable final String taluka)
    {
        return getDataMapper().map(brandingFacade.findAllErpCity(state, district, taluka),DropdownListWsDTO.class,BASIC_FIELD_SET);
    }

    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/getTrackingStatusDetailsForRequest", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public TrackingStatusDetailsData getTrackingStatusDetailsForRequest(String requisitionNumber,String requestStatus)
    {
        return brandingFacade.getTrackingStatusDetailsForRequest(requisitionNumber,requestStatus);
    }

    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/viewRequestDetailsFromTrackingPage", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public TrackingStatusDetailsData viewRequestDetailsFromTrackingPage(String requisitionNumber,String requestStatus)
    {
        return brandingFacade.viewRequestDetailsFromTrackingPage(requisitionNumber,requestStatus);
    }

    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/getTrackingStatusDetailsForCompleteForm", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public TrackingStatusDetailsData getTrackingStatusDetailsForCompleteForm(String requisitionNumber,String requestStatus)
    {
        return brandingFacade.getTrackingStatusDetailsForCompleteForm(requisitionNumber,requestStatus);
    }

    //Approve Dealer/Retailer Branding Request
    //Reject Dealer/Retailer Branding Request
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/updateBrandingRequestStatus", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdParam
    public boolean updateBrandingRequestStatus(@RequestParam String requisitionNo, @RequestParam String status, @RequestParam(required = false) String comments)
    {
        return brandingFacade.updateBrandingRequestStatus(requisitionNo, status, comments);
    }

    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/getBrandingRequisitionTracker", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public BrandingTrackingListData getBrandingRequisitionTrackerDetails(@RequestParam String requisitionNumber)
    {
        return brandingFacade.getBrandingRequisitionTrackerDetails(requisitionNumber);
    }
}
