package com.scl.occ.controllers;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.DataConstraintDao;
import com.scl.core.enums.LeadType;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.services.SlctCrmIntegrationService;
import com.scl.facades.DJPVisitFacade;
import com.scl.facades.MarketMappingFacade;
import com.scl.facades.customer.SclCustomerFacade;
import com.scl.facades.data.*;
import com.scl.facades.djp.data.AddNewSiteData;
import com.scl.facades.djp.data.CounterMappingData;
import com.scl.facades.exception.SclException;
import com.scl.facades.network.SCLNetworkFacade;
import com.scl.facades.prosdealer.data.DealerListData;
import com.scl.facades.visit.data.SiteSummaryData;
import com.scl.occ.annotation.ApiBaseSiteIdAndTerritoryParam;
import com.scl.occ.annotation.ApiBaseSiteIdAndUserIdAndTerritoryParam;
import com.scl.occ.dto.*;
import com.scl.occ.dto.dealer.DealerListWsDTO;
import com.scl.occ.dto.djp.CounterMappingWsDTO;
import com.scl.occ.dto.order.vehicle.DealerVehicleDetailsListWsDTO;
import com.scl.occ.dto.visit.LeadMasterWsDTO;
import com.scl.occ.security.SclSecuredAccessConstants;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.paginated.util.PaginatedSearchUtils;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.scl.occ.controllers.SclBaseController.*;

@Controller
@RequestMapping(value = "/{baseSiteId}/network")
@ApiVersion("v2")
@Tag(name = "Scl Network Management")
public class NetworkController {
    private static final String DEFAULT_FIELD_SET = FieldSetLevelHelper.DEFAULT_LEVEL;

    private static final Logger LOGGER = Logger.getLogger(NetworkController.class);
    public static final String ID = "%id";

    @Resource
    private SclCustomerFacade sclCustomerFacade;
    @Resource(name = "dataMapper")
    private DataMapper dataMapper;
    @Resource
    private SCLNetworkFacade sclNetworkFacade;
    @Resource
    private DJPVisitFacade djpVisitFacade;
    @Resource
    MarketMappingFacade marketMappingFacade;

    @Autowired
    SlctCrmIntegrationService slctCrmIntegrationService;

    @Autowired
    UserService userService;

    @Autowired
    DataConstraintDao dataConstraintDao;

    private DataMapper getDataMapper() {
        return dataMapper;
    }


//    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_CUSTOMERGROUP"})
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(value = "/onboardRetailer", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "onboardRetailer", summary = "Onboard retailer", description = "Adding Retailer to System")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    public ResponseEntity<String> retailerSignUp(@Parameter(description = "Retailer Details") @RequestBody final SCLRetailerWsDto retailerWsDto, @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        var retailerData = dataMapper.map(retailerWsDto, SCLRetailerData.class, fields);
        String name = sclCustomerFacade.addRetailerdata(retailerData);
        if (Objects.nonNull(name)) {
            return ResponseEntity.status(HttpStatus.CREATED).body(name);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error Occured");
    }

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_CUSTOMERGROUP"})
    @GetMapping(value = "/getRetailerBasics", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "getRetailerBasics", summary = "Get Retailer's Basic Details", description = "Get Retailer's Basic Details")
    @ApiBaseSiteIdAndTerritoryParam
    @ApiResponse(responseCode = "200", description = "Retailer Basic Data")
    public SCLRetailerWsDto getRetailerBasics(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
                                              @Parameter(description = "retailer uid") @RequestParam(required = false) final String retailerUid) {
        var retailerData = sclCustomerFacade.getRetailerData(retailerUid);
        return dataMapper.map(retailerData, SCLRetailerWsDto.class, fields);
    }

    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_CUSTOMERGROUP"})
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(value = "/addCompanyDetails", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "addCompanyDetails", summary = "Company Details", description = "Adding Company Details to Retailer And Dealer")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    public ResponseEntity<String> addCompanyDetails(@Parameter(description = "Company Details") @RequestBody final SCLCompanyDetailsWsDto companyDetailsWsDto, @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        var companyDetailsData = dataMapper.map(companyDetailsWsDto, SCLCompanyDetailsData.class, fields);
        String name = sclCustomerFacade.addCompanyDetails(companyDetailsData);
        if (Objects.nonNull(name)) {
            return ResponseEntity.status(HttpStatus.CREATED).body(name);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error Occurred");
    }

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_CUSTOMERGROUP"})
    @GetMapping(value = "/getCompanyDetails", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "getCompanyDetails", summary = "Get Company Details", description = "Get Retailer's Company Details")
    @ApiBaseSiteIdAndTerritoryParam
    @ApiResponse(responseCode = "200", description = "Company Details")
    public SCLCompanyDetailsWsDto getCompanyDetails(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
                                                    @Parameter(description = "retailer uid") @RequestParam(required = false) final String retailerUid) {
        var companyDetails = sclCustomerFacade.getCompanyDetails(retailerUid);
        return dataMapper.map(companyDetails, SCLCompanyDetailsWsDto.class, fields);
    }

    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_CUSTOMERGROUP"})
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(value = "/addBusinessInformation", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "addBusinessInformation", summary = "Business Information", description = "Adding Business Information to Retailer And Dealer")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    public ResponseEntity<String> addBusinessInformation(@Parameter(description = "Business Information") @RequestBody final SCLBusinessInfoWsDto businessInfoWsDto, @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        var businessInfoData = dataMapper.map(businessInfoWsDto, SCLBusinessInfoData.class, fields);
        String name = sclCustomerFacade.addBusinessInformation(businessInfoData);
        if (Objects.nonNull(name)) {
            return ResponseEntity.status(HttpStatus.CREATED).body(name);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error Occurred");
    }

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_CUSTOMERGROUP"})
    @GetMapping(value = "/getBusinessInfo", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "getBusinessInfo", summary = "Get Business Information", description = "Get Business Information for Retailer")
    @ApiBaseSiteIdAndTerritoryParam
    @ApiResponse(responseCode = "200", description = "Business Info")
    public SCLBusinessInfoWsDto getBusinessInfo(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
    		@Parameter(description = "retailer uid") @RequestParam(required = false) final String retailerUid) {
        var businessInfo = sclCustomerFacade.getBusinessInfo(retailerUid);
        return dataMapper.map(businessInfo, SCLBusinessInfoWsDto.class, fields);
    }

    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_CUSTOMERGROUP"})
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(value = "/addFirmsFinInfo", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "addFinancialInformation", summary = "Firm's Financial Information", description = "Adding Firm's Financial Information to Retailer, Dealer and Returns Application No.")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    public ResponseEntity<String> addFinancialInformation(@Parameter(description = "Financial Information") @RequestBody final SCLFinancialInfoWsDto wsDto, @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        var dataDto = dataMapper.map(wsDto, SCLFinancialInfoData.class, fields);
        String name = sclCustomerFacade.addFinancialInformation(dataDto);
        if (Objects.nonNull(name)) {
            return ResponseEntity.status(HttpStatus.CREATED).body(name);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error Occurred");
    }

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_CUSTOMERGROUP"})
    @GetMapping(value = "/getFinancialInfo", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "getFinancialInfo", summary = "Get the Financial Information", description = "Get Financial Information for Retailer")
    @ApiBaseSiteIdAndTerritoryParam
    @ApiResponse(responseCode = "200", description = "Financial Info")
    public SCLFinancialInfoWsDto getFinancialInfo(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
                                                @Parameter(description = "retailer uid") @RequestParam(required = false) final String retailerUid) {
        var financialInfo = sclCustomerFacade.getFinancialInfo(retailerUid);
        return dataMapper.map(financialInfo, SCLFinancialInfoWsDto.class, fields);
    }
    
    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP","ROLE_CUSTOMERGROUP"})
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(value = "/addProposedPlan", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "networkProposalPlan", summary = "Add Network Proposal Plan", description = "Add/update Network Proposal Plan")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    public ResponseEntity<String> addNetworkAdditionPlan(@Parameter(description = "Network Proposal Plan") @RequestBody final SCLNetworkAdditionPlanWsDto wsDto, @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        var dataDto = dataMapper.map(wsDto, SCLNetworkAdditionPlanData.class, fields);
        String name = sclNetworkFacade.addNetworkPlan(dataDto);
        if (Objects.nonNull(name)) {
            return ResponseEntity.status(HttpStatus.CREATED).body(String.format(name));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error Occurred");
    }
    
//    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_CUSTOMERGROUP"})
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(value = "/onboardDealer", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "onboardDealer", summary = "Onboard Dealer", description = "Adding Dealer to System")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    public ResponseEntity<String> dealerSignUp(@Parameter(description = "Dealer Details") @RequestBody final SCLDealerWsDto dealerWsDto, @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        var dealerData = dataMapper.map(dealerWsDto, SCLDealerData.class, fields);
        String name = sclCustomerFacade.addDealerData(dealerData);
        if (Objects.nonNull(name)) {
            return ResponseEntity.status(HttpStatus.CREATED).body(name);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error Occured");
    }

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/getProposedPlan", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "getProposedPlan", summary = "Get Proposed Plan", description = "Get Proposed Plan for Lead")
    @ApiBaseSiteIdAndTerritoryParam
    @ApiResponse(responseCode = "200", description = "Proposed Plan")
    public SCLNetworkAdditionPlanWsDto getPrposedPlan(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
    		@Parameter(description = "Lead Type") @RequestParam(required = false) final String leadType, @Parameter(description = "Taluka") @RequestParam(required = false) final String taluka) {
        var planData = sclNetworkFacade.getNetworkPlan(leadType, taluka);
        return dataMapper.map(planData, SCLNetworkAdditionPlanWsDto.class, fields);
    }

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/getNetworkAdditionDetails", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "getNetworkAdditionDetails", summary = "Get Network addition details ", description = "Get Proposed Plan for Lead")
    @ApiBaseSiteIdAndTerritoryParam
    @ApiResponse(responseCode = "200", description = "get network addition details")
    public NetworkAdditionData getNetworkAdditionDetails(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
    		@Parameter(description = "Lead Type") @RequestParam final String leadType, @Parameter(description = "Taluka") @RequestParam final String taluka) {
        return sclNetworkFacade.getNetworkAdditionDetails(leadType, taluka);
    }

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/getNetworkAdditionListDetails", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "getNetworkAdditionListDetails", summary = "Get Network addition details for dealer/retailer/influencer ", description = "Get Proposed Plan for Lead")
    @ApiBaseSiteIdAndTerritoryParam
    @ApiResponse(responseCode = "200", description = "get network addition list details")
    public List<NetworkAdditionData> getNetworkAdditionListDetails(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
    		@Parameter(description = "Taluka") @RequestParam(required = false) final String taluka) {
        return sclNetworkFacade.getNetworkAdditionListDetails(taluka);
    }

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/getCounterInfoForTaluka", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "getCounterInfoForTaluka", summary = "Get Counter Info", description = "Get Counter Info")
    @ApiBaseSiteIdAndTerritoryParam
    @ApiResponse(responseCode = "200", description = "Get Counter Info")
    public ResponseEntity<Map<String,Integer>> getCounterInfoForTaluka(@Parameter(description = "Taluka") @RequestParam final String taluka,@Parameter(description = "LeadType") @RequestParam final String leadType)
    {
        return ResponseEntity.status(HttpStatus.OK).body(sclNetworkFacade.getCounterInfoForTaluka(taluka,leadType));
    }
    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_CUSTOMERGROUP"})
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(value = "/addNewLead", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "LeadGeneration", summary = "Add New Lead", description = "Lead Generation for Retailer")
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    @ResponseBody
    public ResponseEntity<String> addNewLead(@Parameter(description = "Add New Lead") @RequestBody final LeadMasterWsDTO wsDto, @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        LeadMasterData leadMasterData = dataMapper.map(wsDto, LeadMasterData.class, fields);
        var leadStatus = djpVisitFacade.submitLeadGeneration(leadMasterData, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(String.valueOf(leadStatus));
    }

    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(value = "/addNewCounter", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "AddCounter", summary = "Add New Counter", description = "Adding New Counter")
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    @ResponseBody
    public CounterMappingData addNewCounter(@Parameter(description = "Add New Counter") @RequestBody final CounterMappingWsDTO counter, @RequestParam(required = false) String leadId, @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        final CounterMappingData counterData = dataMapper.map(counter, CounterMappingData.class, fields);
        counterData.setEmail("dummymail@gmail.com");
        var counterId = marketMappingFacade.addCounter(counterData, null,leadId);
        return counterId;
    }

//    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_CUSTOMERGROUP"})
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(value = "/influencerBasics", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "AddInfluencer", summary = "Add Influencer", description = "Add Influencer Basic Details")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    public ResponseEntity<String> addInfluncerBasics(@Parameter(description = "Add Influencer") @RequestBody final InfluencerWsDto wsDto, @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        var data = dataMapper.map(wsDto, InfluencerData.class, fields);
        var id = sclNetworkFacade.addInfluencerBasics(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }
   // @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_CUSTOMERGROUP"})
    @GetMapping(value = "/getInfluencerBasics", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "GetInfluencer", summary = "Get Influencer", description = "Get Influencer Basic Details")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    @ApiResponse(responseCode = "200", description = "Influencer Basics")
    public InfluencerWsDto getInfluencerBasics(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,@Parameter(description = "Influncer Id") @RequestParam(required = false) final String uid) {
        var data = sclNetworkFacade.getInfluencerBasics(uid);
        return dataMapper.map(data, InfluencerWsDto.class, fields);
    }
    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_CUSTOMERGROUP"})
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(value = "/influencerFinancials", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "AddInfluencerFinancials", summary = "Add Influencer Financials", description = "Add Influencer Financials")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    public ResponseEntity<String> addFinancials(@Parameter(description = "Add Financials") @RequestBody final InfluencerFinanceWsDto wsDto, @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        var data = dataMapper.map(wsDto, InfluencerFinanceData.class, fields);
        var id = sclNetworkFacade.addInfluencerFinancials(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }
    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_CUSTOMERGROUP"})
    @GetMapping(value = "/getInfluencerFinancials", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "GetInfluencerFinancials", summary = "Get Influencer Financials", description = "Get Influencer Financials")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    @ApiResponse(responseCode = "200", description = "Influencer Financials")
    public InfluencerFinanceWsDto getInfluencerFinancials(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,@Parameter(description = "Influncer Id") @RequestParam(required = false) final String uid) {
        var data = sclNetworkFacade.getInfluencerFinancials(uid);
        return dataMapper.map(data, InfluencerFinanceWsDto.class, fields);
    }
   // @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_CUSTOMERGROUP"})
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(value = "/influncerNominee", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "influncerNominee", summary = "Add Influencer Nominee", description = "Add Influencer Nominee")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    public ResponseEntity<String> influncerNominees(@Parameter(description = "Add Nominee") @RequestBody final InfluencerNomineeWsDto wsDto, @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        var data = dataMapper.map(wsDto, InfluencerNomineeData.class, fields);
        var id = sclNetworkFacade.addInfluencerNominee(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }
    
    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/getInfluencerList",  produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "GetInfluencerList", summary = "Get Influencer List", description = "Get Influencer List")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    @ApiResponse(responseCode = "200", description = "Influencer Details")
    public InfluencerSummaryListWsDto getInfluencerList(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
    		@Parameter(description="Search term") @RequestParam(required = false) final String searchKey,@Parameter(description="Network Type") @RequestParam(required = false) final String networkType,@Parameter(description="influencerType") @RequestParam(required = false) final String influencerType,@Parameter(description="influencerCategory") @RequestParam(required = false) final String influencerCategory,
                                                        @RequestParam(required = false) List<String> subAreaList, @RequestParam(required = false) List<String> districtList) {
        var data = sclNetworkFacade.getInfluencerSummaryList(searchKey,false,networkType,influencerType,influencerCategory,subAreaList,districtList);
        return dataMapper.map(data, InfluencerSummaryListWsDto.class, fields);

    }

    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/" +
            "getPaginatedInfluencerList", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @Operation(operationId = "GetPaginatedInfluencerList", summary = "Get Paginated Influencer List", description = "Get Influencer List")
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    @ResponseBody    
    public InfluencerSummaryListWsDto getPaginatedInfluencerList(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields
            , @Parameter(description="Search term") @RequestParam(required = false) final String searchKey
            ,@Parameter(description="Network Type") @RequestParam(required = false) final String networkType
            ,@RequestParam(required = false) final String influencerType
            ,@RequestParam(required = false) final String influencerCategory,
            @Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
            @Parameter(description = "Optional {@link PaginationData} parameter in case of savedCartsOnly == true. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,
    final HttpServletResponse response,@RequestParam(required = false)List<String> subAreaList,@RequestParam(required = false)List<String> districtList)
    {
        final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);
        InfluencerSummaryListData listData = new InfluencerSummaryListData();
        SearchPageData<InfluencerSummaryData> respone = sclNetworkFacade.getPagniatedInfluencerSummaryList(searchKey,false,networkType,influencerType,influencerCategory,searchPageData,subAreaList,districtList);
        listData.setInfluncerSummary(respone.getResults());

        if (respone.getPagination() != null)
        {
            response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(respone.getPagination().getTotalNumberOfResults()));
        }
        return getDataMapper().map(listData, InfluencerSummaryListWsDto.class, fields);
    }



    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/newInfluencerList",  produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "newInfluencerList", summary = "Get New Influencer List", description = "Get New Influencer List")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    @ApiResponse(responseCode = "200", description = "New Influencer Details")
    public InfluencerSummaryListWsDto getNewInfluencerList(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
    		@Parameter(description="Search term") @RequestParam(required = false) final String searchKey,@Parameter(description="influencerType") @RequestParam(required = false) final String influencerType,@Parameter(description="networkType") @RequestParam(required = false) final String networkType,@Parameter(description="influencerCategory") @RequestParam(required = false) final String influencerCategory,
                                                           @RequestParam(required = false) List<String> subAreaList,@RequestParam(required = false) List<String> districtList) {
        var data = sclNetworkFacade.getInfluencerSummaryList(searchKey,true,networkType, influencerType,influencerCategory,subAreaList,districtList);
        return dataMapper.map(data, InfluencerSummaryListWsDto.class, fields);

    }
    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(value = "/scheduleMeeting", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "Add Meeting details", summary = "Add Influencer Meeting", description = "Add Influencer Meeting Schedule")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    public ResponseEntity<Boolean> addMeetingSchedule(@Parameter(description = "Meeting Schedule") @RequestBody final MeetingScheduleWsto wsDto, @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        var data = dataMapper.map(wsDto, MeetingScheduleData.class, fields);
        var status = sclNetworkFacade.addMeetingSchedule(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(status);
    }
    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value="/meetCards")
    @ResponseBody
    @ApiBaseSiteIdAndTerritoryParam
    public ScheduledMeetListWsDTO getInfluencerMeetCards(@ApiFieldsParam @RequestParam(defaultValue =
            DEFAULT_FIELD_SET) final String fields,@RequestParam(required = false) String meetcode,
                                                         @RequestParam(required = false) String dateFilter,
                                                         @RequestParam(required = false) String searchTerm,
                                                         @RequestParam(required = false) String status,
                                                         @RequestParam(required = false) String category,
                                                         @RequestParam(required = false) String fromDate,
                                                         @RequestParam(required = false) String toDate) {
        var meetingScheduleListData = sclNetworkFacade.getInfluencerMeetCards(meetcode,dateFilter,searchTerm,status,
                category,fromDate,toDate);
        return dataMapper.map(meetingScheduleListData, ScheduledMeetListWsDTO.class, fields);
    }
    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @PostMapping(value = "/updateMeeting", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "updateMeeting", summary = "Update Meeting", description = "Update Meeting")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    public ResponseEntity<Boolean> updateMeetingDetails(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,@Parameter(description = "Meeting Detail") @RequestBody final MeetingScheduleWsto wsDto) {
        var data = dataMapper.map(wsDto, MeetingScheduleData.class, fields);
        var status = sclNetworkFacade.saveMeetingAttendance(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(status);
    }

    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(value = "/addSite", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "AddSite", summary = "Add New Site", description = "Add New Site")
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    @ResponseBody
    public ResponseEntity<String> addNewSite(@Parameter(description = "Site Attributes") @RequestBody final AddNewSiteWsDTO wsDto, @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        var data = dataMapper.map(wsDto, AddNewSiteData.class, fields);
        djpVisitFacade.createAndSaveSiteDetails(data);
        return ResponseEntity.status(HttpStatus.CREATED).body("Success.");
    }
    
    
    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/getProspectiveNetworkList",  produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "GetProspectiveNetworkList", summary = "Prospective Network List", description = "Get Prospective Network List")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    @ApiResponse(responseCode = "200", description = "Network List")
    public ProspectiveNetworkListWsDTO getProspectiveNetworkList(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
    		@Parameter(description = "Lead Type") @RequestParam(required = false) final String networkType,
    		@Parameter(description = "searchTerm") @RequestParam(required = false) final String searchTerm,
    		@Parameter(description = "dealerCategory") @RequestParam(required = false) final String dealerCategory,
    		@Parameter(description = "taluka") @RequestParam(required = false) final String taluka,
    		@Parameter(description = "stage") @RequestParam(required = false) final String stage) {
       var data = sclNetworkFacade.getPerspectiveNetworkList(networkType, searchTerm,dealerCategory,taluka,stage);
       return dataMapper.map(data, ProspectiveNetworkListWsDTO.class, fields);
    }

    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/getLatitudeLongitudeOfProspectiveNetworkList",  produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getLatitudeLongitudeOfProspectiveNetworkList", summary = "Get Lat and Long for Dealer/Retailer", description = "Get Lat and Long for Dealer/Retailer")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    @ApiResponse(responseCode = "200", description = "Network List")
    public MapProspectiveNetworkDataList getLatitudeLongitudeOfProspectiveNetworkList(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
    		@Parameter(description = "Network Type") @RequestParam(required = true) final String networkType

    ) {
        var data = sclNetworkFacade.getLatitudeLongitudeOfProspectiveNetworkList(networkType);
        return dataMapper.map(data, MapProspectiveNetworkDataList.class, fields);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/site360", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public SiteSummaryData getSiteSummaryforNetwork(@RequestParam String customerCode)
    {
        return sclNetworkFacade.getSiteSummaryforNetwork(customerCode);
    }
    
    @GetMapping(value = "/vehicles", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "getVehicleDetails", summary = "Gets List of Vehicles", description = "Gets List of Vehicles")
    @ApiBaseSiteIdAndTerritoryParam
    public DealerVehicleDetailsListWsDTO getVehicleDetailsForDealer(@ApiFieldsParam @RequestParam(defaultValue = FieldSetLevelHelper.BASIC_LEVEL) final String fields) {
        var vehicleDetailsData = sclNetworkFacade.getDealerVehicleDetails();
        return dataMapper.map(vehicleDetailsData, DealerVehicleDetailsListWsDTO.class,fields);
    }

    /**
     * @author Praveen Kumar
     * @param subArea
     * @param networkType
     * @param leadType
     * @param sclExclusiveCustomer
     * @param searchKey
     * @param fields
     * @return
     */
   // @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @GetMapping(value = "/currentNetwork", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "currentNetwork", summary = "Get Dealer/Retailer Current Network", description = "Returns Dealer/Retailer Current Network List")
    @ApiResponse(responseCode = "200", description = "List of dealer current network")
    @ApiBaseSiteIdAndTerritoryParam
    public DealerCurrentNetworkListDto getCurrentNetwork(@Parameter(description = "Sub Area") @RequestParam(required = false) final String subArea,
    		@Parameter(description = "Network Type") @RequestParam(required = false) final String networkType,
    		@Parameter(description = "Lead Type") @RequestParam(required = true) final String leadType,
    		@Parameter(description="SCL exclusive customer") @RequestParam(required = false) final boolean sclExclusiveCustomer,
    		@Parameter(description="Search key") @RequestParam(required = false) final String searchKey,
                                                         @ApiFieldsParam @RequestParam(defaultValue =
                                                                 DEFAULT_FIELD_SET) final String fields,@RequestParam(required = false) final String dealerCategory,@RequestParam(required = false) List<String> doList,
                                                         @RequestParam(required = false) List<String> subAreaList, @RequestParam(required = false) List<String> territoryList){
        
        var data = sclNetworkFacade.getDealerCurrentNetworkData(dealerCategory, fields, networkType, leadType, sclExclusiveCustomer, searchKey, doList, subAreaList, territoryList);
        return dataMapper.map(data, DealerCurrentNetworkListDto.class, fields);
      

    }

    @RequestMapping(value = "/getExclusiveDealerPercentage", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public String getExclusiveDealerPercentage(@RequestParam(required = false) List<String> territoryList)
    {
        return sclNetworkFacade.getExclusiveDealerPercentage(territoryList);
    }
    
    @GetMapping(value = "/siteStagesSummary", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "siteStagesSummary", summary = "Gets Count of Site Stages", description = "Gets count of Site Stages")
    @ApiBaseSiteIdAndTerritoryParam
    public SiteStageSummaryListWsDTO getSiteStagesSummary(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        var siteStageData = sclNetworkFacade.getSiteStageSummary();
        return dataMapper.map(siteStageData, SiteStageSummaryListWsDTO.class,fields);
    }
    @GetMapping(value = "/siteDataList", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "siteDataList", summary = "Gets List of Sites", description = "Gets List of Sites")
    @ApiBaseSiteIdAndTerritoryParam
    public SiteDetailListWsDTO getSiteDataList(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields, @RequestParam final String siteStage,
            @Parameter(description="Search key") @RequestParam(required = false) final String searchKey) {
        var siteDataList = sclNetworkFacade.getSiteDataList(siteStage, searchKey);
        return dataMapper.map(siteDataList, SiteDetailListWsDTO.class,fields);
    }
    
    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(value = "/updateTimesContacted", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "UpdateTimesContacted", summary = "No. of times contacted", description = "Update No. of times contacted")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    public ResponseEntity<String> updateTimesContacted(@Parameter(description="customerCode") @RequestParam(name = "customerCode") final String customerCode,@Parameter(description="phoneContacted") @RequestParam(name="phoneContacted") Boolean phoneContacted) {
        String result = sclNetworkFacade.updateTimesContacted(customerCode,phoneContacted);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/getRetailerDealerSO", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "getRetailerDealerSO", summary = "Gets all Dealer/Retailer for SO", description = "Dealer/Retailer for SO")
    @ApiBaseSiteIdAndTerritoryParam
    public NwUserListWsDTO getRetailerDealerSO(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        var siteStageData = sclNetworkFacade.getRetailerDealerSO();
        return dataMapper.map(siteStageData, NwUserListWsDTO.class,fields);
    }
    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/getAddressForUser", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "getAddressForUser", summary = "Get Address for Dealer/Retailer/SO", description = "Get Address for Dealer/Retailer/SO")
    @ApiBaseSiteIdAndTerritoryParam
    public SCLAddressWsDto getAddressForUser(@ApiFieldsParam @RequestParam(defaultValue = FieldSetLevelHelper.FULL_LEVEL) final String fields,@Parameter(description="code") @RequestParam final String code) {
        var addressData = sclNetworkFacade.getAddressForUserId(code);
        return dataMapper.map(addressData, SCLAddressWsDto.class,fields);
    }
    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/getInfluencerForSO", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getInfluencerForSO", summary = "Get Influencer List from SO", description = "Get Influencer List")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    public InfluencerSummaryListWsDto getInfluencerForSO(@ApiFieldsParam @RequestParam(defaultValue =
            DEFAULT_FIELD_SET) final String fields,@Parameter(description="socode") @RequestParam final String socode,@Parameter(description="category") @RequestParam(required = false) final String category,@Parameter(description="networkType") @RequestParam(required = false) final String networkType,@Parameter(description="dealerCategory") @RequestParam(required = false) final String dealerCategory) {
        var data = sclNetworkFacade.getInfluencerSummaryListForSO(socode,category,networkType,dealerCategory);
        return dataMapper.map(data, InfluencerSummaryListWsDto.class, fields);
    }
    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/getInfluencerForMeeting", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getInfluencerForMeeting", summary = "Get Influencer List for Meeting", description = "Get Influencer List for Meeting")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    public List<InviteesData> getInfluencerForMeeting(@ApiFieldsParam @RequestParam(defaultValue =
            DEFAULT_FIELD_SET) final String fields,@Parameter(description="meetCode") @RequestParam final String meetCode,
    		@Parameter(description="influencerType") @RequestParam(required = false) final String influencerType,@Parameter(description="influencerCategory") @RequestParam(required = false) final String influencerCategory) {
       return sclNetworkFacade.getInviteesListForMeeting(meetCode,influencerType,influencerCategory);
    }
    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/getOtherNetworkSO", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "getOtherNetworkSO", summary = "Gets all SO ", description = "Gets all SO from other network")
    @ApiBaseSiteIdAndTerritoryParam
    public NwUserListWsDTO getOtherNetworkSO(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        var siteStageData = sclNetworkFacade.getOtherNetworkSO();
        return dataMapper.map(siteStageData, NwUserListWsDTO.class,fields);
    }
    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/getInActiveCustomers", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getInActiveCustomers", summary = "Get Inactive Customer List", description = "Get Inactive Customer List")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    public InactiveNetworkListWsDTO getInActiveCustomers(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields, @Parameter(description = "networkCategory") @RequestParam final String networkCategory,
            @Parameter(description = "Search Key") @RequestParam(required = false) final String searchKey,
            @Parameter(description = "Taluka") @RequestParam(required = false) final String taluka) {
        var data = sclNetworkFacade.getInactiveNetworkList(networkCategory,searchKey, taluka);
        return dataMapper.map(data, InactiveNetworkListWsDTO.class, fields);
    }

    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/getDormantCustomers", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getDormantCustomers", summary = "Get Dormant Customer List", description = "Get Dormant Customer List")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    public InactiveNetworkListWsDTO getDormantCustomers(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,@Parameter(description="networkType") @RequestParam String networkType,@Parameter(description="customerType") @RequestParam String customerType,
                                                         @Parameter(description = "Search Key") @RequestParam(required = false) final String searchKey,
                                                         @Parameter(description = "Taluka") @RequestParam(required = false) final String taluka,
                                                        @Parameter(description = "DOList") @RequestParam(required = false) final List<String> doList,
                                                        @Parameter(description = "soList") @RequestParam(required = false) final List<String> subareaList,
    @Parameter(description = "TerritoryList") @RequestParam(required = false) final List<String> territoryList) {
        var data = sclNetworkFacade.getDormantList(networkType,customerType,searchKey, taluka,doList,subareaList,territoryList);
        return dataMapper.map(data, InactiveNetworkListWsDTO.class, fields);
    }
    @GetMapping(value = "/getCustomerCards", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "getCustomerCards", summary = "Gets The List Of Customers On The Basis Of Customer Onboarding Status for Dealer/Retailer/Influencer", description = "Gets The List Of Customers On The Basis Of Customer Onboarding Status for Dealer/Retailer/Influencer")
    @ApiBaseSiteIdAndTerritoryParam
    public CustomerCardListWsDTO getCustomerCards(@Parameter(description = "Sub Area") @RequestParam(required = false) final String subArea,
    		@Parameter(description = "Lead Type") @RequestParam(required = true) final String leadType,
    		@Parameter(description = "Onboarding Status") @RequestParam(required = true) final String onboardingStatus,
    		@Parameter(description = "Search Key") @RequestParam(required = false) final String searchKey,@RequestParam(required = false) final String dealerCategory,
                                                  @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        var customerCardListData = sclNetworkFacade.getCustomerCards(dealerCategory,leadType,onboardingStatus,searchKey);
        return dataMapper.map(customerCardListData, CustomerCardListWsDTO.class,fields);
    }


    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/getInfluencerListForCategory",  produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "GetInfluencerListForCategory", summary = "Get Influencer List for Category", description = "Get Influencer List for Category")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    @ApiResponse(responseCode = "200", description = "Influencer List")
    public InfluencerSummaryListWsDto getInfluencerListForCategory(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,@RequestParam final String category,
            @Parameter(description = "Search Key") @RequestParam(required = false) final String searchKey,
                                                                   @RequestParam(required = false) final List<String> districtList,
                                                                   @RequestParam(required = false) final List<String> subAreaList) {
        var data = sclNetworkFacade.getInfluencerListForCategory(category, searchKey, districtList, subAreaList);
        return dataMapper.map(data, InfluencerSummaryListWsDto.class, fields);
    }

    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/getSPSalesPerformance",  produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getSPSalesPerformance", summary = "Get Sales Performance", description = "Get Sales Performance")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    @ApiResponse(responseCode = "200", description = "Sales Performance")
    public SPSalesPerformanceListWsDTO getSalesPerformance(@Parameter(description="Search key") @RequestParam(required = false) final String searchKey,
            @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
                                                           @RequestParam(required = false) List<String> doList, @RequestParam(required = false) List<String> subAreaList,@RequestParam(required = false) List<String> territoryList) {
        var data = sclNetworkFacade.getSPSalesPerformanceData(searchKey,doList,subAreaList);
        return dataMapper.map(data, SPSalesPerformanceListWsDTO.class, fields);
    }
    
    @GetMapping(value = "/potentialCustomer", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "GetPotentialCustomer", summary = "Top 10 potential customer",description = "Get Top 10 potential customers")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    public SCLPotentialCustomerListDto getTopPotentialCustomer(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
                                                               @RequestParam(name = "leadType") final String leadType){
        var data = sclNetworkFacade.getTopPotentialCustomer(leadType);
        return dataMapper.map(data, SCLPotentialCustomerListDto.class,fields);
    }

    @GetMapping(value = "/siteChannelDetail", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "siteChannelDetail", summary = "Gets Sites Channel Details", description = "Gets List of Sites Details")
    @ApiBaseSiteIdAndTerritoryParam
    public SiteDetailListWsDTO getSiteChannelDetails(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
                                                     @Parameter(description="Search key") @RequestParam(required = false) final String searchKey) {
        var siteDataList = sclNetworkFacade.getSiteDataList(searchKey);
        return dataMapper.map(siteDataList, SiteDetailListWsDTO.class,fields);
    }

    //Get Total Balance Potential and Monthly Consumption
    @GetMapping(value = "/getSumOfBalancePotentialMonthConsumption", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "getSumOfBalancePotentialMonthConsumption", summary = "Get Balance Potential & Monthly Consumption", description = "Get Balance Potential & Monthly Consumption")
    @ApiBaseSiteIdAndTerritoryParam
    public MarketMappingSiteDetailSummary getSumOfBalancePotentialMonthConsumption(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        var siteDataList = sclNetworkFacade.getSumOfBalancePotentialMonthConsumption();
        return dataMapper.map(siteDataList, MarketMappingSiteDetailSummary.class,fields);
    }

    @GetMapping(value = "/newSiteChannelDetails", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "newSiteChannelDetails", summary = "Gets List of New Sites", description = "Gets List of New Sites Details")
    @ApiBaseSiteIdAndTerritoryParam
    public SiteDetailListWsDTO getNewSiteChannelDetails(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
                                                        @Parameter(description="Search key") @RequestParam(required = false) final String searchKey) throws ParseException {
        var siteDataList = sclNetworkFacade.getSiteDataMTDList(searchKey);
        return dataMapper.map(siteDataList, SiteDetailListWsDTO.class,fields);
    }

    @GetMapping(value = "/siteCategoryChannelDetails", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "siteCategoryChannelDetails", summary = "Gets List of Sites by category", description = "Gets List of Sites Details by Category")
    @ApiBaseSiteIdAndTerritoryParam
    public SiteDetailListWsDTO getSiteCategoryChannelDetails(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields, @RequestParam(name = "category") final String category,
            @Parameter(description="Search key") @RequestParam(required = false) final String searchKey,
                                                             @RequestParam(required = false) final List<String> districtList,
                                                             @RequestParam(required = false) final List<String> subAreaList) {
        var siteDataList = sclNetworkFacade.getSiteDataListByCategory(category, searchKey, districtList, subAreaList);
        return dataMapper.map(siteDataList, SiteDetailListWsDTO.class,fields);
    }

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/getCustomerStagingDetails", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "getCustomerStagingDetails", summary = "Get Customer Staging Details", description = "Get Customer Staging Details")
    @ApiBaseSiteIdAndTerritoryParam
    @ApiResponse(responseCode = "200", description = "Customer Staging Data")
    public CustomerStagingWsDTO getCustomerStagingDetails(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
                                                          @Parameter(description = "customer uid") @RequestParam final String uid) {
        var customerStagingData = sclCustomerFacade.getCustomerStagingData(uid);
        return dataMapper.map(customerStagingData, CustomerStagingWsDTO.class, fields);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP,SclSecuredAccessConstants.ROLE_CLIENT })
    @GetMapping(value = "/dealerDetails360",  produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "dealerDetails360", summary = "Get Dealer Details", description = "Get Dealer Details")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    @ApiResponse(responseCode = "200", description = "Dealer Details")
    public DealerDetails360WsDTO getDealerDetails360(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,@RequestParam String dealerCode,@RequestParam(required = false) String subArea,@RequestParam(required = false) List<String> territoryList) {
        var data = sclNetworkFacade.getDealerDetails360(dealerCode,subArea,territoryList);
        return dataMapper.map(data, DealerDetails360WsDTO.class, fields);
    }


    @RequestMapping(value = "/getLocationDetails", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public CounterLocationDetailsData getLocationDetails(@Parameter(description = "dealerCode") @RequestParam(required = true) String dealerCode)
    {
        return sclNetworkFacade.getLocationDetails(dealerCode);
    }

    @RequestMapping(value = "/requestUpdate", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public CounterLocationDetailsData getRequestUpdate(@Parameter(description = "dealerCode") @RequestParam(required = true) String dealerCode)
    {
        return sclNetworkFacade.getRequestUpdate(dealerCode);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/submitUpdatedLocationDetails", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public boolean submitUpdatedLocationDetails(@RequestBody CounterLocationDetailsData counterLocationDetailsData,
                                                @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        return sclNetworkFacade.submitUpdatedLocationDetails(counterLocationDetailsData);

    }

    /**
     * @author Praveen Kumar
     * @param customerCode
     * @param fields
     * @return
     */
    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/influencerDetail360", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "Get Influencer Detail 360", summary = "Get Influencer Detail 360", description = "Get Influencer Detail 360 Page")
    @ApiBaseSiteIdAndTerritoryParam
    @ApiResponse(responseCode = "200", description = "Get Site Stages")
    public InfluencersDetails360WsDTO getInfluencer360(@Parameter(description = "customer code") @RequestParam(required = true) final String customerCode,
                                                       @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
                                                       @RequestParam (required = false) List<String> subAreaList,
                                                       @RequestParam (required = false) List<String> districtList)
    {
        var data = sclNetworkFacade.getInfluencerDetails360(customerCode,subAreaList,districtList);
        return dataMapper.map(data, InfluencersDetails360WsDTO.class, fields);
    }

    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/dealerDetailsForm",  produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "dealerDetailsForm", summary = "Get Dealer Details Form", description = "Get Dealer Details Form")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    @ApiResponse(responseCode = "200", description = "Dealer Details")
    public DealerDetailsFormWsDTO getDealerDetailsForm(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,@RequestParam String dealerCode) {
        var data = sclNetworkFacade.getDealerDetailsForm(dealerCode);
        return dataMapper.map(data, DealerDetailsFormWsDTO.class, fields);
    }
    
    @GetMapping(value = "/getOnboardingCustomerCardCount", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "getOnboardingCustomerCardCount", summary = "Gets The Count of Onboarding Customer Card For Dealer/Retailer/Influencer", description = "Gets The Count of Onbaording Cstomer Card For Dealer/Retailer/Influencer")
    @ApiBaseSiteIdAndTerritoryParam
    public ResponseEntity<Map<String,Integer>> getCustomerCardCount(@RequestParam(required = false) final String subArea, @RequestParam(required = false) List<String> doList, @RequestParam(required = false) List<String> soList, @Parameter(description = "Lead Type",schema=@Schema(allowableValues = {"DEALER", "RETAILER", "INFLUENCER", "SITE", "TPC"})) @RequestParam(required = true) final LeadType leadType, @RequestParam(required = false) final String duration){
        return ResponseEntity.status(HttpStatus.OK).body(sclNetworkFacade.getOnboardingCardCount(null,leadType,
                duration,doList,soList));
    }

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/getCustomerDetailedDetails", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "getCustomerDetailedDetails", summary = "Get Customer Detailed Details For Retailer/Dealer", description = "Get Customer Detailed Details For Retailer/Dealer")
    @ApiBaseSiteIdAndTerritoryParam
    @ApiResponse(responseCode = "200", description = "Customer Detailed Data")
    public CustomerDetailedWsDTO getCustomerDetailedDetails(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
                                                            @Parameter(description = "customer uid") @RequestParam(required = true) final String uid) {
        var customerDetailedData = sclCustomerFacade.getCustomerDetailedData(uid);
        return dataMapper.map(customerDetailedData, CustomerDetailedWsDTO.class, fields);
    }
    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/getInfluencerDetailedDetails", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "getInfluencerDetailedDetails", summary = "Get Customer Detailed Details For Influencer", description = "Get Customer Detailed Details For Influencer")
    @ApiBaseSiteIdAndTerritoryParam
    @ApiResponse(responseCode = "200", description = "Customer Detailed Data")
    public InfluencerDetailedWsDTO getInfluencerDetailedDetails(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
    		@Parameter(description = "customer uid") @RequestParam(required = true) final String uid) {
        var customerDetailedData = sclCustomerFacade.getInfluencerDetailedData(uid);
        return dataMapper.map(customerDetailedData, InfluencerDetailedWsDTO.class, fields);

    }
    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_CUSTOMERGROUP"})
    @GetMapping(value = "/leadList",  produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "leadList", summary = "Get All Leads", description = "All leads")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    @ApiResponse(responseCode = "200", description = "Lead Details ")
    public LeadSummaryListWsDTO getAllLeads(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,@Parameter(description = "Lead Type",schema=@Schema(allowableValues = {"DEALER", "RETAILER", "INFLUENCER", "ARCHITECT", "CONTRACTOR", "ENGINEER", "MASON", "SITE", "TPC", ""})) @RequestParam(required = false)LeadType leadtype,@RequestParam(required = false) String monthYear,@RequestParam(required = false) String searchTerm,@RequestParam(required = false) String leadId
    ,@RequestParam(required = false) List<String> doList, @RequestParam(required = false) List<String> subAreaList,@RequestParam(required = false) List<String> territoryList) {
        var data = sclNetworkFacade.getLeadSummaryList(leadtype,monthYear,searchTerm,leadId,  doList,  subAreaList,territoryList);
        return dataMapper.map(data, LeadSummaryListWsDTO.class, fields);
    }
    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @PutMapping(value = "/updateLeadStage",  produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "update lead", summary = "Update lead for ID", description = "Update lead for ID")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    public ResponseEntity<String> updateLeadForId( @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,@RequestParam String leadId,@RequestParam String leadStage) {
        var status = sclNetworkFacade.updateLead(leadId,leadStage);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(String.valueOf(status));
    }
    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    @DeleteMapping(value = "/removeLead",  produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "remove lead", summary = "Remove lead for ID", description = "Remove lead for ID")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    public ResponseEntity<String> removeLeadForId( @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,@RequestParam String leadId) {
        var status = sclNetworkFacade.removeLeadForId(leadId);
        return ResponseEntity.status(HttpStatus.CREATED).body(String.valueOf(status));
    }
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/searchBoxDealers", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndTerritoryParam
    public DealerListWsDTO getSearchDealersForSubArea(@RequestParam(required = false) final String subArea,
                                                   @Parameter(description="SCL exclusive customer") @RequestParam(required = false) final boolean sclExclusiveCustomer,
                                                   @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        DealerListData dataList =  sclNetworkFacade.getAllDealersForSubArea(subArea,sclExclusiveCustomer);
        return dataMapper.map(dataList,DealerListWsDTO.class, fields);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/searchBoxRetailers", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndTerritoryParam
    public DealerListWsDTO getSearchRetailersForSubArea(@RequestParam(required = false) final String subArea,
                                                     @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        DealerListData dataList =  sclNetworkFacade.getAllRetailersForSubArea(subArea);
        return dataMapper.map(dataList,DealerListWsDTO.class, fields);
    }

    /**
     * @author Praveen Kumar
     * @param fields
     * @param networkType
     * @return
     */
    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @RequestMapping(value="/searchBoxProspectiveNetwork", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndTerritoryParam
    public DealerListWsDTO getSearchedProspectiveNetwork(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
    		@Parameter(description = "Network Type") @RequestParam(required = true) final String networkType) {
        var data = sclNetworkFacade.getProspectiveCustomer(networkType);
        return dataMapper.map(data, DealerListWsDTO.class, fields);
    }
    
    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @RequestMapping(value="/searchBoxInfluencerNetwork", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndTerritoryParam
    public DealerListWsDTO getSearchedInfluencerNetwork(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        var data = sclNetworkFacade.getInfluencerCustomers();
        return dataMapper.map(data, DealerListWsDTO.class, fields);
    }
    
    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @PostMapping(value = "/verifyDenyPartner",  produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "verifyDenyPartner", summary = "Update staus of Partner", description = "Update staus of Partner")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    @ApiResponse(responseCode = "200", description = "Partner Updated")
    public boolean verifyDenyPartner(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,@RequestParam String uid,@RequestParam String status,@RequestParam(required = false) String rejectionReason) {
        var data = sclNetworkFacade.verifyDenyPartner(uid,status,rejectionReason);
        return data;
    }

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/getNetworkTypeCount", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "getNetworkTypeCount", summary = "Get Network Type Count", description = "Get Network Type Count")
    @ApiBaseSiteIdAndTerritoryParam
    @ApiResponse(responseCode = "200", description = "Get Network Type Count")
    public ResponseEntity<Map<String,Integer>> getNetworkTypeCount(@RequestParam(required = false) final String leadType,  @RequestParam(required = false) List<String> doList,
                                                                   @RequestParam(required = false) List<String> subAreaList, @RequestParam(required = false) List<String> territoryList)
    {
        return ResponseEntity.status(HttpStatus.OK).body(sclNetworkFacade.getNetworkTypeCount(leadType, doList, subAreaList, territoryList));
    }
    
    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @RequestMapping(value="/getInactiveNetworkRemovalDetails", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndTerritoryParam
    public InactiveNetworkData getInactiveNetworkRemovalDetails(@Parameter(description = "Customer Code") @RequestParam final String customerCode) {
       
        return sclNetworkFacade.getInactiveNetworkRemovalDetailsForCode(customerCode);
    }
    
    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @RequestMapping(value="/saveInactiveNetworkRemovalDetails", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndTerritoryParam
    public Boolean saveInactiveNetworkRemovalDetails(@Parameter(description = "Customer Code") @RequestBody final InactiveNetworkData data) {
       
        return sclNetworkFacade.saveInactiveNetworkRemovalDetails(data);
    }
    
    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/getSiteCategoryCount", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "getSiteCategoryCount", summary = "Get Site Category Count", description = "Get Site Category Count")
    @ApiBaseSiteIdAndTerritoryParam
    @ApiResponse(responseCode = "200", description = "Site Category Count")
    public ResponseEntity<Map<String,Object>> getSiteCategoryCount(@RequestParam(required = false) List<String> doList, @RequestParam(required = false) List<String> subAreaList)
    {
        return ResponseEntity.status(HttpStatus.OK).body(sclNetworkFacade.getSiteCategoryCount(doList, subAreaList));
    }

    /**
     * @author Praveen Kumar
     * @param leadType
     * @param searchKey
     * @param fields
     * @return
     */
    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/newOnboardRetailerList", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "newOnboardRetailerList", summary = "Get New Onboard Retailer List", description = "Returns New Onboard Retailer List")
    @ApiResponse(responseCode = "200", description = "List of New Onboard Retailer List")
    @ApiBaseSiteIdAndTerritoryParam
    public RetailerOnboardListDto newOnboardRetailerList(@Parameter(description = "Lead Type",schema=@Schema(allowableValues = {"DEALER", "RETAILER", "INFLUENCER", ""})) @RequestParam(required = false) final LeadType leadType,
    		@Parameter(description="Search key") @RequestParam(required = false) final String searchKey,
                                                         @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields){

        var data = sclNetworkFacade.getOnboardRetailerList(leadType,searchKey);
        return dataMapper.map(data, RetailerOnboardListDto.class, fields);
    }

    //Channel KPI-Graph DEALER & RETAILER
    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/getChannelKPIGraphDealerRetailer", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "getChannelKPIGraphDealerRetailer", summary = "Get ChannelKPI Graph Dealer/Retailer", description = "Get Channel KPI Graph Dealer/Retailer")
    @ApiBaseSiteIdAndTerritoryParam
    @ApiResponse(responseCode = "200", description = "Get Channel KPI Graph Dealer")
    public ChannelStrength getChannelKPIGraphDealerRetailer(String leadType,@RequestParam(required = false) List<String> doList, @RequestParam(required = false) List<String> subAreaList, @RequestParam(required = false) List<String> territoryList)
    {
        return sclNetworkFacade.getChannelKPIGraphDealerRetailer(leadType,doList,subAreaList,territoryList);
    }
    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/getSiteStages", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "SiteStages", summary = "Get Site Stages", description = "Get Site Stages")
    @ApiBaseSiteIdAndTerritoryParam
    @ApiResponse(responseCode = "200", description = "Get Site Stages")
    public ResponseEntity<Map<String,Object>> getSiteStages()
    {
        return ResponseEntity.status(HttpStatus.OK).body(sclNetworkFacade.getSiteStages());
    }
    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/getInfluencerCategoryCount", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "getInfluencerCategoryCount", summary = "Get Influencer Category Count", description = "Get Influencer Category Count")
    @ApiBaseSiteIdAndTerritoryParam
    @ApiResponse(responseCode = "200", description = "Influence Category Count")
    public ResponseEntity<Map<String,Object>> getInfluencerCategoryCount(@RequestParam(required = false) List<String> districtList, @RequestParam(required = false) List<String> subAreaList)
    {
        return ResponseEntity.status(HttpStatus.OK).body(sclNetworkFacade.getInfluencerCategoryCount(districtList, subAreaList));
    }

    //Network-get (Dealer/retailer) potential and Shree counter share Percentage
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getDealerCounterShareForNetwork", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public NetworkDealerRetailerCounterShareData getDealerRetailerCounterShareForNetwork(@RequestParam(required = false) String SOFilter,
                                                                                         @RequestParam (required = false) String taluka,@Parameter(description = "DO List") @RequestParam(required = false) List<String> doList,
                                                                                         @Parameter(description = "Sub Area List") @RequestParam(required = false) List<String> subAreaList,
                                                                                         @Parameter(description = "Territory List") @RequestParam(required = false) List<String> territoryList) {
        return sclNetworkFacade.getDealerRetailerCounterShareForNetwork(SOFilter,taluka,doList,subAreaList,territoryList);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/dealer360Last6MonthSales", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public List<MonthlySalesData> getLastSixMonthSalesForDealer(@RequestParam(required = false) String taluka,@RequestParam(required = false) String Filter,@RequestParam(required = false) List<String> territoryList)
    {
        return sclNetworkFacade.getLastSixMonthSalesForDealer(taluka,Filter,territoryList);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/retailer360Last6MonthSales", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public List<MonthlySalesData> getLastSixMonthSalesForRetailer(
            @RequestParam(required = false) String taluka,
            @RequestParam(required = true) String Filter,
            @RequestParam(required = false) List<String> territoryList,
            @RequestParam(required = false) List<String> districtList,
            @RequestParam(required = false) List<String> subAreaList)
    {
        return sclNetworkFacade.getLastSixMonthSalesForRetailer(territoryList, districtList, subAreaList, taluka, Filter);
    }


    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/getPanFromGST", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "getPanFromGST", summary = "Get PAN From GST", description = "Get PAN From GST Number")
    @ApiBaseSiteIdAndTerritoryParam
    @ApiResponse(responseCode = "200", description = "PAN Number")
    public ResponseEntity<String> getPanFromGST(@RequestParam String gstNumber)
    {
        return ResponseEntity.status(HttpStatus.OK).body(sclNetworkFacade.getPanFromGST(gstNumber));
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/influencer360Last6MonthSales", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public List<MonthlySalesData> getLastSixMonthSalesForInfluencer(@RequestParam String taluka,@RequestParam String Filter)
    {
        return sclNetworkFacade.getLastSixMonthSalesForInfluencer(taluka,Filter);
    }

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/getChrunReasonCount", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "getChrunReasonCount", summary = "Get Churn Reasons", description = "Get Churn Reasons")
    @ApiBaseSiteIdAndTerritoryParam
    @ApiResponse(responseCode = "200", description = "Churn Reasons")
    public ResponseEntity<Map<String,Object>> getChrunReasonCount(@RequestParam(required = false) List<String> territoryList)
    {
        return ResponseEntity.status(HttpStatus.OK).body(sclNetworkFacade.getChrunReasonCount(territoryList));
    }
    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/spDetails360", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "getSalesPromoterDetails", summary = "getSalesPromoterDetails", description = "getSalesPromoterDetails")
    @ApiBaseSiteIdAndTerritoryParam
    @ApiResponse(responseCode = "200", description = "Churn Reasons")
    public ResponseEntity<SalesPromoterDetailsData> getSpDetails360(@RequestParam String spCode)
    {
        return ResponseEntity.status(HttpStatus.OK).body(sclNetworkFacade.getSpDetails360(spCode));
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getNewRetailerInfluencerCountMTD", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public NewInfluencerRetailerCountData getNewRetailerInfluencerCountMTD(@RequestParam String customerType) {
        return sclNetworkFacade.getNewRetailerInfluencerCountMTD(customerType);
    }


    @Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @GetMapping(value = "/newOnboardRetailerListWithPagination", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "newOnboardRetailerListWithPagination", summary = "Get New Onboard Retailer List with Pagination", description = "Returns New Onboard Retailer List")
    @ApiResponse(responseCode = "200", description = "List of New Onboard Retailer List")
    @ResponseStatus(value = HttpStatus.CREATED)
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public SearchPageData<RetailerOnboardListDto> newOnboardRetailerListWithPagination(@Parameter(description = "Lead Type") @RequestParam(required = true) final LeadType leadType,
    		@Parameter(description="Search key") @RequestParam(required = false) final String searchKey,
                                                                                       @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
                                                                                       @Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
                                                                                       @Parameter(description = "Optional {@link PaginationData} parameter in case of savedCartsOnly == true. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,
                                                                                       final HttpServletResponse response){
        final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);
        RetailerOnboardListDto listData = new RetailerOnboardListDto();
        SearchPageData<RetailerOnboardDto> res = sclNetworkFacade.getOnboardRetailerListPagination(leadType,searchKey,searchPageData);
        listData.setRetailerOnboardList(res.getResults());
       // listData.setSalesCountMTD();

        if (res.getPagination() != null)
        {
            response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(res.getPagination().getTotalNumberOfResults()));
        }
//        return getDataMapper().map(listData, RetailerOnboardListDto.class, fields);
        return null;
    }    
    
    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/" +
            "influencerDetailList", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @Operation(operationId = "GetPaginatedInfluencerList", summary = "Get Paginated Influencer List", description = "Get Influencer List")
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    @ResponseBody    
    public InfluencerSummaryListWsDto getInfluencerDetailedList(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields
            , @Parameter(description="Search term") @RequestParam(required = false) final String searchKey
            ,@Parameter(description="Network Type") @RequestParam(required = false) final String networkType
            ,@RequestParam(required = false, defaultValue = "false") final Boolean isNew
            ,@RequestParam(required = false) final String influencerType
            ,@RequestParam(required = false) final String influencerCategory,
            @Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
            @Parameter(description = "Optional {@link PaginationData} parameter in case of savedCartsOnly == true. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,
    final HttpServletResponse response,@RequestParam(required = false, defaultValue = "true") final Boolean includeSales,
    @RequestParam(required = false, defaultValue = "false") final Boolean includeScheduleMeet, @RequestParam(required = false, defaultValue = "false") final Boolean includeNonSclCustomer,
                                                                @RequestParam(required = false) final List<String> subAreaList,@RequestParam(required = false) final List<String> districtList)
    {
        final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);
        InfluencerSummaryListData listData = new InfluencerSummaryListData();
        SearchPageData<InfluencerSummaryData> respone = sclNetworkFacade.getInfluencerDetailedSummaryList(searchKey,isNew,networkType,influencerType,influencerCategory,searchPageData, includeSales, includeScheduleMeet,includeNonSclCustomer,subAreaList,districtList);
        listData.setInfluncerSummary(respone.getResults());

        if (respone.getPagination() != null)
        {
            response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(respone.getPagination().getTotalNumberOfResults()));
        }
        return getDataMapper().map(listData, InfluencerSummaryListWsDto.class, fields);
    }
    
    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/" +
            "retailerDetailList", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @Operation(operationId = "GetPaginatedRetailerList", summary = "Get Paginated Retailer List", description = "Get Retailer List")
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    @ResponseBody    
    public DealerCurrentNetworkListDto getRetailerDetailedList(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields
            , @Parameter(description="Search term") @RequestParam(required = false) final String searchKey
            ,@Parameter(description="Network Type") @RequestParam(required = false) final String networkType
            ,@RequestParam(required = false, defaultValue = "false") final Boolean isNew
            ,@Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
            @Parameter(description = "Optional {@link PaginationData} parameter in case of savedCartsOnly == true. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,
    final HttpServletResponse response,@RequestParam(required = false) List<String> subAreaList,@RequestParam(required = false) List<String> districtList)
    {
        final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);
        DealerCurrentNetworkListData listData = new DealerCurrentNetworkListData();
        SearchPageData<DealerCurrentNetworkData> respone = sclNetworkFacade.getRetailerDetailedSummaryList(searchKey,isNew,networkType,searchPageData,subAreaList,districtList);
        listData.setDealerCurrentNetworkList(respone.getResults());

        if (respone.getPagination() != null)
        {
            response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(respone.getPagination().getTotalNumberOfResults()));
        }
        return getDataMapper().map(listData, DealerCurrentNetworkListDto.class, fields);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getRetailerInfluencerCardCountMTD", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public NewInfluencerRetailerCountData getRetailerInfluencerCardCountMTD(@RequestParam String customerType,@RequestParam String networkType) {
        return sclNetworkFacade.getRetailerInfluencerCardCountMTD(customerType,networkType);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getNetworkDormantCountCard", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public NewInfluencerRetailerCountData getNetworkDormantCountCard(@RequestParam String networkType,@RequestParam String customerType,@RequestParam(required = false) List<String> doList,
                                                                     @RequestParam(required = false) List<String> subAreaList,@RequestParam(required = false) List<String> territoryList) {
        return sclNetworkFacade.getNetworkDormantCountCard(networkType,customerType,doList,subAreaList,territoryList);
    }
    
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(value = "/sendOnboardingSmsOtp")
    @Operation(operationId = "sendOnboardingSmsOtp", summary = "Send Onboarding Sms Otp", description = "Send Onboarding Sms Otp")
    @ResponseBody

    public ResponseEntity<Boolean> sendOnboardingSmsOtp(@Parameter(description = "name") @RequestParam final String name, @Parameter(description = "mobileNo") @RequestParam final String mobileNo) {
        //sclCustomerFacade.checkMobileNumberValidation(mobileNo);
        Boolean smsSent  = sclCustomerFacade.sendOnboardingSmsOtp(name, mobileNo);

        if (smsSent.equals(Boolean.TRUE)) {
            return ResponseEntity.status(HttpStatus.CREATED).body(Boolean.TRUE);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Boolean.FALSE);
    }
    
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(value = "/sendLoginSmsOtp")
    @Operation(operationId = "sendLoginSmsOtp", summary = "Send Login Sms Otp", description = "Send Login Sms Otp")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    public LoginOTPWsDTO sendLoginSmsOtp(@Parameter(description = "uid") @RequestParam final String uid,@Parameter(description = "partnerCustomerFlag") @RequestParam(required = false) final String partnerCustomerFlag,@Parameter(description = "pcuid") @RequestParam(required = false) final String pcuid) {
        LoginOTPWsDTO loginOTPWsDTO = new LoginOTPWsDTO();
        try {
            SclCustomerModel customer = slctCrmIntegrationService.findCustomerByCustomerNo(uid);
            if (checkIsCustomerBlocked(customer)) {
                String errorMsg = String.format("The SAP code %s is blocked. Please contact our sales team", customer.getUid());
                LOGGER.info(errorMsg);
                String msg = dataConstraintDao.findVersionByConstraintName(SclCoreConstants.CUSTOMER.CUSTOMER_LOGIN_BLOCKED);
                if (StringUtils.isNotBlank(msg)) {
                    if (msg.contains(ID)) {
                        errorMsg = msg.replace(ID, customer.getUid());
                    }
                }
                loginOTPWsDTO.setErrorMessage(errorMsg);
                loginOTPWsDTO.setIsLoginAllowed(Boolean.FALSE);
                loginOTPWsDTO.setIsOtpSent(Boolean.FALSE);
                return loginOTPWsDTO;
            }else{
                loginOTPWsDTO= sclCustomerFacade.sendLoginSmsOtp(customer,partnerCustomerFlag,pcuid);
            }
        }catch (Exception exception){
            LOGGER.error(String.format("Exception occurred in sendLoginSmsOtp with message::%s",exception.getMessage()));
            loginOTPWsDTO.setErrorMessage(exception.getMessage());
            loginOTPWsDTO.setIsLoginAllowed(Boolean.FALSE);
            loginOTPWsDTO.setIsOtpSent(Boolean.FALSE);
        }
        return loginOTPWsDTO;
    }

    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(value = "/onboardSalesPromoter", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "onboardSalesPromoter", summary = "Onboard Sales Promoter", description = "Adding Sales Promoter to System")
    @ApiBaseSiteIdAndTerritoryParam
    @ResponseBody
    public ResponseEntity<String> salesPromoterSignUp(@Parameter(description = "Sales Promoter Details") @RequestBody final SCLSalesPromoterWsDto salesPromoterWsDto, @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        var salesPromoterData = dataMapper.map(salesPromoterWsDto, SCLSalesPromoterData.class, fields);
        String name = sclCustomerFacade.addSalesPromoterData(salesPromoterData);
        if (Objects.nonNull(name)) {
            return ResponseEntity.status(HttpStatus.CREATED).body(name);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error Occured");
    }
    
    @GetMapping(value = "/getOnboardingFormsSS", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "getOnboardingFormsSS", summary = "Get Onboarding Forms Screenshots", description = "Get Onboarding Forms Screenshots")
    @ApiBaseSiteIdAndTerritoryParam
    @ApiResponse(responseCode = "200", description = "OnboardingFormsSS")
    public List<SCLImageData> getOnboardingFormsSS(@Parameter(description = "Customer Uid") @RequestParam(required = true) final String uid) {
        return sclNetworkFacade.getOnboardingFormsSS(uid);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/Influencer360SalesHistoryForNetwork", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public SalesHistoryData getSalesHistoryForNetwork360(@Parameter(description = "customer code") @RequestParam(required = true) final String customerCode, @RequestParam(required = false)List<String> subAreaList, @RequestParam(required = false)List<String> districtList)
    {
        SalesHistoryData data = sclNetworkFacade.getSalesHistoryDataForNetworkInfluencer360(customerCode,subAreaList,districtList);
        return data;
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getLeadsGeneratedCountForInfluencer", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public Integer getLeadsGeneratedCountForInfluencer(@Parameter(description = "MTD/YTD Filter") @RequestParam(required = true) String filter)
    {
        return sclNetworkFacade.getLeadsGeneratedCountedForInfluencer(filter);
    }


    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @RequestMapping(value = "/sendPartnerNotification", method = RequestMethod.GET)
    @Operation(operationId = "sendPartnerNotification", summary = "Send Partner Notification")
    @ResponseBody
    @ApiBaseSiteIdParam
    public ResponseEntity<String> sendPartnerNotification(@RequestParam String uid) {
        var dealerId = sclNetworkFacade.sendPartnerNotification(uid);
        return ResponseEntity.status(HttpStatus.CREATED).body(dealerId);
    }


    private boolean checkIsCustomerBlocked(SclCustomerModel sclCustomer) {
        if(Objects.nonNull(sclCustomer)) {
            UserGroupModel dealerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID);
            UserGroupModel retailerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);
            if (sclCustomer.getGroups() != null && (sclCustomer.getGroups().contains(dealerGroup) || sclCustomer.getGroups().contains(retailerGroup))) {
                return slctCrmIntegrationService.isCustomerBlocked(sclCustomer);
            }
        }
        return false;
    }

}
