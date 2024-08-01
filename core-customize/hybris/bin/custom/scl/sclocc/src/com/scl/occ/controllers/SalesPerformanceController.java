package com.scl.occ.controllers;


import com.scl.core.model.SubAreaMasterModel;
import com.scl.facades.SalesPerformanceFacade;
import com.scl.facades.TerritoryManagementFacade;
import com.scl.facades.data.*;
import com.scl.facades.prosdealer.data.DealerListData;
import com.scl.occ.annotation.ApiBaseSiteIdAndUserIdAndTerritoryParam;
import com.scl.occ.dto.AnnualSalesTargetSettingListWsDTO;
import com.scl.occ.dto.DealerCurrentNetworkListDto;
import com.scl.occ.dto.InfluencerSummaryListWsDto;

import com.scl.occ.dto.SalesPerformNetworkDetailsListWsDTO;
import com.scl.occ.dto.dealer.DealerListWsDTO;
import com.scl.occ.security.SclSecuredAccessConstants;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.paginated.util.PaginatedSearchUtils;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;

import static com.scl.occ.controllers.SclBaseController.DEFAULT_FIELD_SET;

@Controller
@RequestMapping(value = "/{baseSiteId}/users/{userId}/salesPerform")
@ApiVersion("v2")
@Tag(name = "Sales Performance Controller")
public class SalesPerformanceController extends SclBaseController  {

    @Resource
    SalesPerformanceFacade salesPerformanceFacade;
    @Resource(name = "dataMapper")
    private DataMapper dataMapper;
    @Resource
    TerritoryManagementFacade territoryManagementFacade;

    //Sales-get total actual and target data for sales donut chart
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getTotalAndActualTargetForSales", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public SalesAndAchievementData getTotalAndActualTargetForSales(@RequestParam(required = false) String filter,
    		@Parameter(description = "year") @RequestParam(required = false) Integer year, @Parameter(description = "month") @RequestParam(required = false) Integer month,
                                                                   @RequestParam(required = false) List<String> territoryList)
    {
        int month1 = 0, year1 = 0;
        if (month != null)
            month1 = month.intValue();
        if (year != null)
            year1 = year.intValue();
        return salesPerformanceFacade.getTotalAndActualTargetForSales(filter,year1,month1,territoryList);
    }
    
    //Sales-get prorated actual and target data for sales donut chart
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getProratedActualAndActualTargetForSales", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public SalesAndAchievementData getProratedActualAndActualTargetForSales(@RequestParam(required = false) String filter,
    		@Parameter(description = "year") @RequestParam(required = false) Integer year, @Parameter(description = "month") @RequestParam(required = false) Integer month,
                                                                            @RequestParam(required = false) List<String> territoryList)
    {
        int month1 = 0, year1 = 0;
        if (month != null)
            month1 = month.intValue();
        if (year != null)
            year1 = year.intValue();
        return salesPerformanceFacade.getProratedActualAndActualTargetForSales(filter,year1,month1,territoryList);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getProratedBreach", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ProratedBreachData getProratedBreach( @RequestParam(required = false) List<String> doList,
                                                 @RequestParam(required = false) List<String> subAreaList)
    {

        return salesPerformanceFacade.getProratedBreach(doList,subAreaList);
    }

    //Sales-Get Predicted achievement count in MT/Month basis
    //Sales-Get Current rate count in MT/Month basis
    //Sales-Get Asking rate  count in MT/Month basis
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getPredicateCurrentAndAskingRateForSales", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public CurrentAskingPredicatedData getPredicatedAchievementCurrentAndAskingRate(@RequestParam(required = false) String filter,
                                                                                    @RequestParam(required = false) List<String> territoryList,
                                                                                    @Parameter(description = "year") @RequestParam(required = false) Integer year,
                                                                                    @Parameter(description = "month") @RequestParam(required = false) Integer month)
    {
        int month1 = 0, year1 = 0;
        if (month != null)
            month1 = month.intValue();
        if (year != null)
            year1 = year.intValue();
        return salesPerformanceFacade.getPredicatedAchievementCurrentAndAskingRate(filter,territoryList,month1,year1);
    }

    //Sales-search for so who's tagged to that sub area- territory management
    //Sales-get Top 5 employees in Daily,monthly, yearly basis
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getLeaderboardEmpList", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public SalesLeaderboardListData getTop5LeaderboardEmpList(@RequestParam String state, @RequestParam(required = false) String filter, @RequestParam(required = false) String soFilter, @RequestParam(required = false) List<String> doList,
                                                              @RequestParam(required = false) List<String> subAreaList)
    {
        return salesPerformanceFacade.getTop5LeaderboardEmpList(state,filter,soFilter,null,null);
    }

    //Sales-LeaderBoard in Quarterly,monthly, yearly basis - dealer/retailer
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getSalesLeaderboardEmpList", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public LeaderboardListData getSalesLeaderboardEmpList(@RequestParam String filter,@RequestParam String leadType)
    {
        return salesPerformanceFacade.getSalesLeaderboardEmpList(filter,leadType);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getTsmSalesLeaderboardEmpList", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public TsmLeaderboardListData getTsmSalesLeaderboardEmpList(@RequestParam String filter)
    {
        return salesPerformanceFacade.getTsmSalesLeaderboardEmpList(filter);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getSpSalesLeaderboardEmpList", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public LeaderboardListData getSpSalesLeaderboardEmpList(@RequestParam String filter,@RequestParam(required = false) String districtName)
    {
        return salesPerformanceFacade.getSpSalesLeaderboardEmpList(filter,districtName);
    }



    //Market-Display counter share target and actual data (with last month and last year filter)
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getDealerCounterShareForMarket", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public MarketCounterShareData getDealerCounterShareForMarket( @RequestParam(required = false) String filter,
                                                                  @Parameter(description = "year") @RequestParam(required = false) Integer year, @Parameter(description = "month") @RequestParam(required = false) Integer month,
                                                                  @RequestParam(required = false) List<String> doList,
                                                                  @RequestParam(required = false) List<String> subAreaList)
        {
            int month1 = 0, year1 = 0;
            if (month != null)
                month1 = month.intValue();
            if (year != null)
                year1 = year.intValue();

            return salesPerformanceFacade.getDealerCounterShareForMarket(filter,year1,month1,doList,subAreaList);
        }

    @RequestMapping(value = "/directDispatchOrdersByMonthAndYear", method = RequestMethod.GET)
    @ResponseBody
    @Operation(operationId = "getDirectDispatchOrdersPercentage", summary = "Direct Dispatch Orders MTD Percentage", description = "Direct Dispatch Orders MTD Percentage as per month and year")
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ResponseEntity<Map<String,Object>> getDirectDispatchOrdersPercentage(
            @Parameter(description = "Filters by Month") @RequestParam(required = false) Integer month, @Parameter(description = "Filters by Year") @RequestParam(required = false) Integer year,
            @RequestParam(required = false) List<String> doList,
            @RequestParam(required = false) List<String> subAreaList) {
        int month1 = 0, year1 = 0;
        if(month!=null)
            month1 = month.intValue();
        if(year!=null)
            year1 = year.intValue();
        return ResponseEntity.status(HttpStatus.OK).body(salesPerformanceFacade.getDirectDispatchOrdersMTDPercentage(month1, year1,doList,subAreaList));
    }

        //Market-get secondary lead distance count details - not implemented
        @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
        @RequestMapping(value = "/getSecondaryLeadDistanceCount", method = RequestMethod.GET)
        @ResponseStatus(value = HttpStatus.CREATED)
        @ResponseBody
        @ApiBaseSiteIdAndUserIdAndTerritoryParam
        public Double getSecondaryLeadDistanceCount(@RequestParam(required = false) String filter, @RequestParam(required = false) Integer month, @RequestParam(required = false) Integer year,
                                                    @RequestParam(required = false) List<String> territoryList)
        {
            int month1 = 0, year1 = 0;
            if(month!=null)
                month1 = month.intValue();
            if(year!=null)
                year1 = year.intValue();

            return salesPerformanceFacade.getSecondaryLeadDistanceCount(filter,month1,year1,territoryList);
        }

        //Market-display NCR level for last three months (red, amber and green level)
        @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
        @RequestMapping(value="/getNCRTrendList", method = RequestMethod.GET)
        @ResponseStatus(value = HttpStatus.CREATED)
        @ResponseBody
        @ApiBaseSiteIdAndUserIdAndTerritoryParam
        public NCRTrendListData getNCRTrendList()
        {
            return salesPerformanceFacade.getNCRTrendList();
        }

    //Cockpit -display NCR level for last one month (red, amber and green level)
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getNCRTrendListForOneMonth", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public NCRTrendListData getNCRTrendListForOneMonth()
    {
        return salesPerformanceFacade.getNCRTrendListForOneMonth();
    }

        //Network-Get count of all dealer/retailers/influencers
        @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
        @RequestMapping(value = "/getCountForAllDealerRetailerInfluencers", method = RequestMethod.GET)
        @ResponseStatus(value = HttpStatus.CREATED)
        @ResponseBody
        @ApiBaseSiteIdAndUserIdAndTerritoryParam
        public Integer getCountForAllDealerRetailerInfluencers (@RequestParam String leadType,
                                                               @RequestParam(required = false) List<String> territoryList, @RequestParam(required = false) List<String> subAreaList, @RequestParam(required = false) List<String> districtList)
        {
            return salesPerformanceFacade.getCountForAllDealerRetailerInfluencers(leadType,territoryList,subAreaList,districtList);
        }

    //Network-Get count of all retailers/influencers
    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/getCountOfAllRetailersInfluencers", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public Integer getCountOfAllRetailersInfluencers (@RequestParam String leadType)
    {
        return salesPerformanceFacade.getCountOfAllRetailersInfluencers(leadType);
    }
        //Network-Display list of all tagged dealers,retailers,influencers
        @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
        @RequestMapping(value = "/getListOfAllDealerRetailerInfluencers", method = RequestMethod.GET)
        @ResponseStatus(value = HttpStatus.CREATED)
        @ResponseBody
        @ApiBaseSiteIdAndUserIdAndTerritoryParam
        public SalesPerformNetworkDetailsListData getListOfAllDealerRetailerInfluencers (
        @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
        @Parameter(description = "Lead Type") @RequestParam(required = true) final String leadType,
        @Parameter(description = "filter") @RequestParam(required = false) final String filter, @RequestParam(required = false) List<String> territoryList, @RequestParam(required = false)List<String> subAreaList, @RequestParam(required = false)List<String> districtList){
            return salesPerformanceFacade.getListOfAllDealerRetailerInfluencers(fields, leadType, filter,territoryList,subAreaList,districtList);

        }

    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/getListOfAllRetailerInfluencersForDealer", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public SalesPerformNetworkDetailsListData getListOfAllRetailerInfluencersForDealer (
            @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
            @Parameter(description = "Lead Type") @RequestParam(required = true) final String leadType,
            @Parameter(description = "filter") @RequestParam(required = false) final String filter,@RequestParam(required = false) List<String> doList, @RequestParam(required = false) List<String> subAreaList){
        return salesPerformanceFacade.getListOfAllRetailerInfluencersForDealer(fields, leadType, filter,doList,subAreaList);

    }
        //Network-Get count of low performing dealers,retailers and influencers
        //Network-Get average counter share of low performing dealers, retailers and influencers
        //Network-Get Avg monthly orders count of all dealers, retailers and influencers
        //Network-Get total month potential count of all dealers, retailers and influencers
        @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
        @RequestMapping(value = "/getLowPerformingNetworkDataForDealerRetailerInfluencers", method = RequestMethod.GET)
        @ResponseStatus(value = HttpStatus.CREATED)
        @ResponseBody
        @ApiBaseSiteIdAndUserIdAndTerritoryParam
        public LowPerformingNetworkData getLowPerformingNetworkDataForDealerRetailerInfluencers (@RequestParam String leadType,
                @Parameter(description = "Filters by Month") @RequestParam(required = false) Integer month,
                @Parameter(description = "Filters by Year") @RequestParam(required = false) Integer year,@RequestParam(required = false) String filter,@RequestParam(required = false) List<String> territoryList, @RequestParam(required = false)List<String> subAreaList, @RequestParam(required = false)List<String> districtList){
            int month1 = 0, year1 = 0;
            if (month != null)
                month1 = month.intValue();
            if (year != null)
                year1 = year.intValue();
            return salesPerformanceFacade.getLowPerformingNetworkDataForDealerRetailerInfluencers(leadType, month1, year1,filter,territoryList,subAreaList,districtList);
        }

    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/getLowPerformingCountDetailsDealer", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public LowPerformingNetworkData getLowPerformingCountDetailsDealer (@RequestParam String leadType){
        return salesPerformanceFacade.getLowPerformingCountDetailsDealer(leadType);
    }
        //Network-Display list of all low performing of all dealers, retailers and influeners
        @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
        @GetMapping(value = "/getListOfAllLowPerformingRetailerInfluencersForDealer", produces = MediaType.APPLICATION_JSON_VALUE)
        @ResponseBody
        @Operation(operationId = "get List Of All Low Performing Retailer Influencers For Dealer", summary = "Get Influencer/Retailer Low Performing Details", description = "Returns Inf/Retailer Low Performing List")
        @ApiResponse(responseCode = "200", description = "List of Low Performing dealer current network")
        @ApiBaseSiteIdAndUserIdAndTerritoryParam
        public SalesPerformNetworkDetailsListData getListOfAllLowPerformingRetailerInfluencersForDealer (
        @Parameter(description = "Lead Type") @RequestParam(required = true) final String leadType,
        @Parameter(description = "Search key") @RequestParam(required = false) final String searchKey,
        @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,@RequestParam(required = false) List<String> doList,@RequestParam(required = false) List<String> subAreaList){

            var data = salesPerformanceFacade.getListOfAllLowPerformingRetailerInfluencersForDealer(fields, leadType, searchKey);
            return dataMapper.map(data, SalesPerformNetworkDetailsListData.class, fields);

        }
    //Network-Display list of all low performing of all dealers, retailers and influeners
    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @GetMapping(value = "/getListOfAllLowPerformingDealers", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(operationId = "LowPerformingDetails for Dealers", summary = "Get Dealer/Retailer Low Performing Details", description = "Returns Dealer/Retailer Low Performing List")
    @ApiResponse(responseCode = "200", description = "List of Low Performing dealer current network")
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public SalesPerformNetworkDetailsListData getListOfAllLowPerformingForDealers (
            @Parameter(description = "Lead Type") @RequestParam(required = true) final String leadType,
            @Parameter(description = "Search key") @RequestParam(required = false) final String searchKey,
            @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,@RequestParam(required = false) List<String> territoryList, @RequestParam(required = false)List<String> subAreaList, @RequestParam(required = false)List<String> districtList){

        var data = salesPerformanceFacade.getListOfAllLowPerformingDealerRetailerInfluencers(fields, leadType, searchKey,territoryList,subAreaList,districtList);
        return dataMapper.map(data, SalesPerformNetworkDetailsListData.class, fields);

    }
        //Home - Bottom 3 lagging counters
        @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
        @GetMapping(value = "/getBottomLaggingCounters", produces = MediaType.APPLICATION_JSON_VALUE)
        @ResponseBody
        @Operation(operationId = "getBottomLaggingCounters", summary = "Get Dealer Bottom Lagging Details", description = "Returns Dealer Bottom Lagging List")
        @ApiResponse(responseCode = "200", description = "List of Bottom lagging dealer")
        @ApiBaseSiteIdAndUserIdAndTerritoryParam
        public SalesPerformNetworkDetailsListData getBottomLaggingCounters(@RequestParam(required = false) List<String> territoryList){

           return salesPerformanceFacade.getBottomLaggingCounters(territoryList);
    }

    //Market-get product wise sales % ratio and volume ratio
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getProductwiseSalesPercentRatioAndVolume", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ProductMixVolumeAndRatioListData getProductwiseSalesPercentRatioAndVolumeRatio(@RequestParam(required = false) String filter, @RequestParam(required = false) Integer month, @RequestParam(required = false) Integer year,@RequestParam(required = false) List<String> territoryList)
    {
        int month1 = 0, year1 = 0;
        if(month!=null)
            month1 = month.intValue();
        if(year!=null)
            year1 = year.intValue();
        return salesPerformanceFacade.getProductwiseSalesPercentRatioAndVolumeRatio(filter, month1, year1,territoryList);
    }
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getProductwiseSalesPercentRatioAndVolumeRatioForCustomer", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ProductMixVolumeAndRatioListData getProductwiseSalesPercentRatioAndVolumeRatioForCustomer(@RequestParam(required = false) String filter, @RequestParam(required = false) Integer month, @RequestParam(required = false) Integer year,
                                                                                          @RequestParam(required = false) List<String> territoryList, @RequestParam(required = false) List<String> subAreList, @RequestParam(required = false) List<String> districtList)
    {
        int month1 = 0, year1 = 0;
        if(month!=null)
            month1 = month.intValue();
        if(year!=null)
            year1 = year.intValue();
        return salesPerformanceFacade.getProductwiseSalesPercentRatioAndVolumeRatioForCust(filter, month1, year1,territoryList,subAreList,districtList);
    }

    //Market-display NCR level for last three months (red, amber and green level)

    //Network-get (Dealer/retailer/influencer) potential and actual counter share
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getDealerCounterShareForNetwork", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public NetworkCounterShareData getDealerCounterShareForNetwork(@RequestParam(required = false) String filter, @RequestParam(required = false) Integer month, @RequestParam(required = false) Integer year,
                                                                   @RequestParam(required = false) List<String> doList,
                                                                   @RequestParam(required = false) List<String> subAreaList,@RequestParam(required = false) List<String> territoryList) {
        int month1 = 0, year1 = 0;
        if (month != null)
            month1 = month.intValue();
        if (year != null)
            year1 = year.intValue();
        return salesPerformanceFacade.getDealerCounterShareForNetwork(filter,month1, year1, doList, subAreaList,territoryList);
    }

        //Network-Get count and potential of all dealer/retailers/influencers with zero lifting
        //Network-Display list of all dealer/retailers/influencers with zero lifting
        //Network-Get count and potential of all dealer/retailers/influencers with zero lifting
        @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
        @RequestMapping(value = "/getCountAndPotentialForZeroLifting", method = RequestMethod.GET)
        @ResponseStatus(value = HttpStatus.CREATED)
        @ResponseBody
        @ApiBaseSiteIdAndUserIdAndTerritoryParam
        public ZeroLiftingNetworkData getCountAndPotentialForZeroLifting (@RequestParam String leadType, @Parameter(description = "Filters by Month") @RequestParam(required = false) Integer month, @Parameter(description = "Filters by Year") @RequestParam(required = false) Integer year,@RequestParam(required = false) String filter, @RequestParam(required = false) List<String> territoryList, @RequestParam(required = false)List<String> subAreaList, @RequestParam(required = false)List<String> districtList)
        {
            int month1 = 0, year1 = 0;
            if(month!=null)
                month1 = month.intValue();
            if(year!=null)
                year1 = year.intValue();
            return salesPerformanceFacade.getCountAndPotentialForZeroLifting( leadType,month1,year1,filter,territoryList,subAreaList,districtList);
        }

    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/getCountAndPotentialForZeroLiftingDealer", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ZeroLiftingNetworkData getCountAndPotentialForZeroLiftingDealer (@RequestParam String leadType)
    {
        return salesPerformanceFacade.getCountAndPotentialForZeroLiftingDealer(leadType);
    }
        //Network-Display list of all dealer/retailers/influencers with zero lifting
        @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
        @RequestMapping(value = "/getListOfAllZeroLiftingDealerRetailerInfluencers", method = RequestMethod.GET)
        @ResponseStatus(value = HttpStatus.CREATED)
        @ResponseBody
        @ApiBaseSiteIdAndUserIdAndTerritoryParam
        public SalesPerformNetworkDetailsListData getListOfAllZeroLiftingDealerRetailerInfluencers (
        @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
        @Parameter(description = "Lead Type") @RequestParam(required = true) final String leadType,
        @Parameter(description = "Search Key") @RequestParam(required = false) final String searchKey,@RequestParam(required = false) List<String> territoryList,@RequestParam(required = false)List<String> subAreaList,@RequestParam(required = false)List<String> districtList){
            return salesPerformanceFacade.getListOfAllZeroLiftingDealerRetailerInfluencers(fields, leadType, searchKey,territoryList,subAreaList,districtList);

        }

    //Network-Display list of all dealer/retailers/influencers with zero lifting
    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/getListOfAllZeroLiftingRetailersInfluencerForDealers", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public SalesPerformNetworkDetailsListData getListOfAllZeroLiftingRetailersInfluencerForDealers (
            @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
            @Parameter(description = "Lead Type") @RequestParam(required = true) final String leadType,
            @Parameter(description = "Search Key") @RequestParam(required = false) final String searchKey){
        return salesPerformanceFacade.getListOfAllZeroLiftingRetailersInfluencerForDealers(fields, leadType, searchKey);

    }


    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
        @RequestMapping(value = "/getSalesHistoryForDealer", method = RequestMethod.GET)
        @ResponseStatus(value = HttpStatus.CREATED)
        @ResponseBody
        @ApiBaseSiteIdAndUserIdAndTerritoryParam
        public SalesPerformamceCockpitSaleData getSalesHistoryForDealer (@RequestParam(required = false) String subArea)
        {
            SalesPerformamceCockpitSaleData data = salesPerformanceFacade.getSalesHistoryForDealer(subArea);
            return data;
        }

        //Get all Partner - Search on Partners on Sales Performance
        @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
        @RequestMapping(value="/getAllPartnersForSubArea", method = RequestMethod.GET)
        @ResponseStatus(value = HttpStatus.CREATED)
        @ResponseBody
        @ApiBaseSiteIdAndUserIdAndTerritoryParam
        public DealerListWsDTO getAllPartnersForSubArea(@RequestParam(required = false) List<String> territoryList)
        {
            DealerListData dataList =  territoryManagementFacade.getAllDealersForSubArea(territoryList);
            return getDataMapper().map(dataList,DealerListWsDTO.class, BASIC_FIELD_SET);
        }
        //Get Partner Details Data For Sales by using code,name,mobilenumber and customernumber
        @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
        @RequestMapping(value = "/getPartnerDetailsForSales", method = RequestMethod.GET)
        @ResponseStatus(value = HttpStatus.CREATED)
        @ResponseBody
        @ApiBaseSiteIdAndUserIdAndTerritoryParam
        public PartnerDetailsDataForSales getPartnerDetailsForSales(@RequestParam(required = true) String searchKeyWord,@RequestParam(required = false) String filter,@RequestParam(required = false) Integer year,@RequestParam(required = false) Integer month,@RequestParam(required = false) List<String> doList,@RequestParam(required = false) List<String> subAreaList)
        {
            PartnerDetailsDataForSales data = salesPerformanceFacade.getPartnerDetailsForSales(searchKeyWord,filter,year,month,doList,subAreaList);
            return data;
        }
    //Market-get product wise sales % ratio and volume ratio
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getBrandWiseSalesPercentRatioAndVolumeRatio", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ProductMixVolumeAndRatioListData getBrandWiseSalesPercentRatioAndVolumeRatio(@RequestParam String customerCode)
    {
        return salesPerformanceFacade.getBrandWiseSalesPercentRatioAndVolumeRatio(customerCode);
    }

    //Sales-Get Predicted achievement count in MT/Month basis - Dealer/Retailer Persona
    //Sales-Get Current rate count in MT/Month basis - Dealer/Retailer Persona
    //Sales-Get Asking rate  count in MT/Month basis - Dealer/Retailer Persona
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getPredicateCurrentAndAskingRateForSalesDealerRetailer", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public CurrentAskingPredicatedData getPredicatedAchievementCurrentAndAskingRateDealerRetailer(@RequestParam(required = false) String filter,@Parameter(description = "BGP Filter") @RequestParam(required = false) String bgpFilter,
                                                                                                  @Parameter(description = "year") @RequestParam(required = false) Integer year,
                                                                                                  @Parameter(description = "month") @RequestParam(required = false) Integer month)
    {
        int month1 = 0, year1 = 0;
        if (month != null)
            month1 = month.intValue();
        if (year != null)
            year1 = year.intValue();
        return salesPerformanceFacade.getPredicatedAchievementCurrentAndAskingRateDealerRetailer(filter,bgpFilter,month1,year1);
    }

    //Sales-get prorated actual and target data for sales donut chart -Dealer/Retailer Persona
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getProratedActualAndTargetForSalesDealerRetailer", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public SalesAndAchievementData getProratedActualAndTargetForSalesDealerRetailer(@RequestParam(required = false) String filter,
                                                                            @Parameter(description = "year") @RequestParam(required = false) Integer year,
                                                                                     @Parameter(description = "month") @RequestParam(required = false) Integer month,
                                                                                    @Parameter(description = "BGP Filter") @RequestParam(required = false) String bgpFilter)
    {
        int month1 = 0, year1 = 0;
        if (month != null)
            month1 = month.intValue();
        if (year != null)
            year1 = year.intValue();
        return salesPerformanceFacade.getProratedActualAndTargetForSalesDealerRetailer(filter,year1,month1,bgpFilter);
    }


    //Sales-get total actual and target data for sales donut chart -Dealer/Retailer/TSH/RH Persona
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getTotalActualAndTargetSaleForCustomer", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public SalesAndAchievementData getTotalActualAndTargetSaleForCustomer(@RequestParam(required = false) String filter,
                                                                                    @Parameter(description = "year") @RequestParam(required = false) Integer year,
                                                                                    @Parameter(description = "month") @RequestParam(required = false) Integer month,
                                                                                    @Parameter(description = "BGP Filter") @RequestParam(required = false) String bgpFilter, @Parameter(description = "counter type") @RequestParam(required = false) String counterType,
                                                                          @Parameter(description = "retailer Id") @RequestParam(required = false) String retailerId)
    {
        int month1 = 0, year1 = 0;
        if (month != null)
            month1 = month.intValue();
        if (year != null)
            year1 = year.intValue();
        return salesPerformanceFacade.getTotalActualAndTargetSaleForCustomer(filter,year1,month1,bgpFilter,counterType,retailerId);
    }

    //Sales-get actual and target graph - 10 dqy bucket/MTD/YTD Dealer/Retailer/TSH/RH Persona
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getActualVsTargetSalesGraph", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public MonthlySalesListData getActualVsTargetSalesGraph(@RequestParam(required = false) String filter, @RequestParam(required = false) String counterType, @RequestParam(required = false) String bgpFilter)
    {
        return salesPerformanceFacade.getActualVsTargetSalesGraph(filter,counterType,bgpFilter);
    }

    //Sales-get actual and target graph - 10 dqy bucket/MTD/YTD TSMRH Persona
    //doubt for TSM work flow - still not developed
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getActualVsTargetSalesGraphForTSMRH", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public MonthlySalesListData getActualVsTargetSalesGraphForTSMRH(@RequestParam(required = false) String filter,
                                                                    @RequestParam(required = false) List<String> doList,
                                                                    @RequestParam(required = false) List<String> subAreaList)
    {
        return salesPerformanceFacade.getActualVsTargetSalesGraphForTSMRH(filter,doList,subAreaList);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getDealerCounterShareForSP", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public NetworkCounterShareData getDealerCounterShareForSP(@RequestParam(required = false) String filter, @RequestParam(required = false) Integer month, @RequestParam(required = false) Integer year,
                                                              @RequestParam(required = false) List<String> doList,
                                                              @RequestParam(required = false) List<String> subAreaList) {
        int month1 = 0, year1 = 0;
        if (month != null)
            month1 = month.intValue();
        if (year != null)
            year1 = year.intValue();
        return salesPerformanceFacade.getDealerCounterShareForSP(filter,month1, year1,doList,subAreaList);
    }
    //Product Mix - volume,ratio and points earned MTd and YTD
    //Market-get product wise sales % ratio and volume ratio
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getProductMixPercentRatioAndVolumeRatioWithPoints", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ProductMixVolumeAndRatioListData getProductMixPercentRatioAndVolumeRatioWithPoints(@RequestParam(required = false) String filter)
    {
        return salesPerformanceFacade.getProductMixPercentRatioAndVolumeRatioWithPoints(filter);
    }

    //Bar chart - influecner overall performance
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getInfOverallPerformance", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public OverallPerformanceListData getInfluencerOverallPerformance(@RequestParam String filter, @RequestParam(required = false) String bgpFilter)
    {
        return salesPerformanceFacade.getInfluencerOverallPerformance(filter,bgpFilter);
    }

    //Counter share calculation logic api
    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/getCounterShareData", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public CounterShareResponseData getCounterShareData (@RequestBody final CounterShareData counterShareData)
    {
        return salesPerformanceFacade.getCounterShareData(counterShareData);
    }

    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/getCurrentNetworkCustomers", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public DealerCurrentNetworkListData getCurrentNetworkCustomers (@RequestParam(required = true) String leadType,@RequestParam(required = false) String networkType,@RequestParam(required = false) String searchKeyFilter,@RequestParam(required = false) boolean sclExclusiveCustomer)
    {
        return salesPerformanceFacade.getCurrentNetworkCustomers(leadType,networkType,searchKeyFilter, sclExclusiveCustomer);
    }

    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/getLastLiftingDateAndQtyForCustomers", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public List<Map<String,Object>> getLastLiftingDateAndQtyForCustomers (@RequestParam(required = false)String sclCustomerCode,@RequestParam(required = true) String leadType)
    {
        return salesPerformanceFacade.getLastLiftingDateAndQtyForCustomers(sclCustomerCode,leadType);
    }
}
