package com.scl.occ.controllers;

import com.scl.core.model.SubAreaMasterModel;
import com.scl.core.services.TerritoryManagementService;
import com.scl.facades.SalesPlanningFacade;
import com.scl.facades.data.*;
import com.scl.occ.annotation.ApiBaseSiteIdAndUserIdAndTerritoryParam;
import com.scl.occ.dto.AnnualSalesMonthWiseTargetListWsDTO;
import com.scl.occ.security.SclSecuredAccessConstants;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.paginated.util.PaginatedSearchUtils;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import com.scl.occ.dto.*;

@Controller
@RequestMapping(value = "/{baseSiteId}/users/{userId}/salesPlanning")
@ApiVersion("v2")
@Tag(name = "Sales Planning Controller")
public class SalesPlanningController extends SclBaseController {
    @Resource
    private SalesPlanningFacade salesPlanningFacade;
    @Resource
    TerritoryManagementService territoryManagementService;

    //Submit Annual sales target for dealers (CY sales, plan sales as per sku's, total target)
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/submitAnnualSalesTargetSettingForDealers", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ErrorListWsDTO submitAnnualSalesTargetSettingForDealers(@RequestBody AnnualSalesTargetSettingListData annualSalesTargetSettingData,
                                                                   @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return salesPlanningFacade.submitAnnualSalesTargetSettingForDealers(annualSalesTargetSettingData);
    }

    //Submit Annual sales target for retailers (CY sales, plan sales as per sku's, total target)
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/submitAnnualSalesTargetSettingForRetailers", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ErrorListWsDTO submitAnnualSalesTargetSettingForRetailers(@RequestBody AnnualSalesTargetSettingListData annualSalesTargetSettingData,
                                                              @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return salesPlanningFacade.submitAnnualSalesTargetSettingForRetailers(annualSalesTargetSettingData);
    }

    //Modify/Finalize annual sales review target for Dealers as per month wise
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/finalizeAnnualSalesTargetSettingForDealers", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ErrorListWsDTO finalizeAnnualSalesTargetSettingForDealers(@RequestBody AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData,
                                                              @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return salesPlanningFacade.submitFinalizeAnnualSalesTargetSettingForDealers(annualSalesMonthWiseTargetListData);
    }

    //Modify/Finalize annual sales review target for Retailers as per month wise
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/finalizeAnnualSalesTargetSettingForRetailers", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ErrorListWsDTO finalizeAnnualSalesTargetSettingForRetailers(@RequestBody AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData,
                                                                @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return salesPlanningFacade.submitFinalizeAnnualSalesTargetSettingForRetailers(annualSalesMonthWiseTargetListData);
    }

    //Submit Reviewed Sales for new Retailers (New Record) month wise
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/onboardedAnnualSalesTargetSettingForRetailers", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public boolean onboardedAnnualSalesTargetSettingForRetailers(@RequestBody AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData,
                                                                 @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return salesPlanningFacade.submitOnboardedAnnualSalesTargetSettingForRetailers(annualSalesMonthWiseTargetListData);
    }

    //Submit Reviewed Sales for Onborded Dealers as per sku's (New Record) month wise
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/onboardedAnnualSalesTargetSettingForDealers", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public boolean onboardedAnnualSalesTargetSettingForDealers(@RequestBody AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData,
                                                               @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return salesPlanningFacade.submitOnboardedAnnualSalesTargetSettingForDealers(annualSalesMonthWiseTargetListData);
    }

    //Display Planned Sales for Dealers Month wise (previously saved record)
    //Annual sales summary planned tab- implement product level plan filter
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/viewPlannedSalesForDealerMonthwiseForm", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public AnnualSalesMonthWiseTargetListData viewPlannedSalesforDealersMonthwise(@RequestParam String subArea,@RequestParam(required = false) String filter)
    {
        return salesPlanningFacade.viewPlannedSalesforDealersMonthwise(subArea,filter);
    }

    //Annual sales summary planned tab- implement retailer level plan filter
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/viewPlannedSalesforRetailerMonthwise", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public AnnualSalesMonthWiseTargetListData viewPlannedSalesforRetailerMonthwise(@RequestParam String subArea,@RequestParam(required = false) String filter)
    {
        return salesPlanningFacade.viewPlannedSalesforRetailerMonthwise(subArea, filter);
    }

    //Display Review Sales for Dealers and showcase newly onboarded dealers month wise (previously saved record)
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/viewReviewedSalesforDealerMonthwise", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public AnnualSalesMonthWiseTargetListData viewReviewedSalesForDealersMonthWise(@RequestParam String subArea, @RequestParam String financialYear, @RequestParam(required = false) String filter)
    {
        return salesPlanningFacade.viewReviewedSalesforDealersMonthwise(subArea,filter);
    }

    //Display Review Sales for Retailers and showcase newly onboarded Retailers month wise (previously saved record)
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/viewReviewedSalesforRetailerMonthwise", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public AnnualSalesMonthWiseTargetListData viewReviewedSalesforRetailerMonthWise(@RequestParam String subArea, @RequestParam(required = false) String filter)
    {
        return salesPlanningFacade.viewReviewedSalesforRetailesMonthwise(subArea,filter);
    }


    //Display Annual Summary as per Subarea of SO (current year sales, plan sales, product mix details, dealer mix details)
    //Showcasing the Dealer tier-wise target channel mix percentage as per subarea
    //Showcasing the Target Product Mix percentage for the mentioned products as per subarea
    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
        @RequestMapping(value = "/viewAnnualSalesSummary", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public AnnualTargetSettingSummaryData viewAnnualSalesSummary(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields, @RequestParam(required = false) boolean isAnnualSummaryTargetSet, @RequestParam(required = false) boolean isAnnualSummaryAfterTargetSetting, @RequestParam(required = false) boolean isAnnualSummaryForReview, @RequestParam(required = false) boolean isAnnualSummaryAfterReview) {
        return salesPlanningFacade.viewAnnualSalesSummary(isAnnualSummaryTargetSet, isAnnualSummaryAfterTargetSetting, isAnnualSummaryForReview, isAnnualSummaryAfterReview);
    }

    //AnnualSales: Display Dealer Details and CY sales (SKU wise) as per sub area
    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/viewDealerDetailsForAnnualSales", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public AnnualSalesTargetSettingListData viewDealerDetailsForAnnualSales(@RequestParam String subArea, @RequestParam(required = false) String filter,
                                                                            @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        return salesPlanningFacade.viewDealerDetailsForAnnualSales(subArea,filter);
    }

    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/viewRetailerDetailsForAnnualSales", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public AnnualSalesTargetSettingListWsDTO viewRetailerDetailsForAnnualSales(@RequestParam String subArea, @RequestParam(required = false) String filter,
                                                                               @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,@Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
                                                                               @Parameter(description = "Optional {@link PaginationData} parameter in case of savedCartsOnly == true. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,
                                                                               final HttpServletResponse response) {

        AnnualSalesTargetSettingListData annualSalesTargetSettingListData = new AnnualSalesTargetSettingListData();
        double cySale=0.0;
        final SearchPageData<Object> searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);
        SearchPageData<AnnualSalesTargetSettingData> targetSettingData = salesPlanningFacade.viewRetailerDetailsForAnnualSalesWithPagination(searchPageData, subArea, filter);
        if(targetSettingData.getResults() !=null)
        for (AnnualSalesTargetSettingData result : targetSettingData.getResults()) {
            cySale  +=  result.getCurrentYearSales();
        }

        SubAreaMasterModel subAreaMaster=territoryManagementService.getTerritoryById(subArea);
        annualSalesTargetSettingListData.setAnnualSalesTargetSetting(targetSettingData.getResults());
        annualSalesTargetSettingListData.setSubArea(subAreaMaster.getTaluka());
        annualSalesTargetSettingListData.setSubAreaId(subAreaMaster.getPk().toString());

        List<Double> totalPlanAndCySales = salesPlanningFacade.getTotalPlanAndCySalesRetailerPlanned(searchPageData, subArea);
        if(totalPlanAndCySales!=null && !totalPlanAndCySales.isEmpty()) {
            annualSalesTargetSettingListData.setTotalPlanSales(0.0);
            annualSalesTargetSettingListData.setTotalCurrentYearSales(cySale);
        }
        if (targetSettingData.getPagination() != null)
        {
            response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(targetSettingData.getPagination().getTotalNumberOfResults()));
        }
        return getDataMapper().map(annualSalesTargetSettingListData, AnnualSalesTargetSettingListWsDTO.class, fields);
    }


    //AnnualSales: Display Dealer's Annual sales review division in month wise (FY- april to march) as per subarea
    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/viewMonthWiseDealerDetails", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public AnnualSalesMonthWiseTargetListData viewMonthWiseDealerDetailsForAnnualSales(@RequestParam String subArea, @RequestParam(required = false) String filter,
                                                                                       @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        return salesPlanningFacade.viewMonthWiseDealerDetailsForAnnualSales(subArea,filter);
    }

    //AnnualSales: Display Retailers Annual sales review division in month wise (FY- april to march) as per subarea
    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/viewMonthWiseRetailerDetails", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public AnnualSalesMonthWiseTargetListData viewMonthWiseRetailerDetailsForAnnualSales(@RequestParam String subArea,
                                                                                         @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields, @RequestParam(required = false) String filter) {
        return salesPlanningFacade.viewMonthWiseRetailerDetailsForAnnualSales(subArea,filter);
    }

    //Annual Sales Summary review-Populate list of retailers with code,name and potential
    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/getRetailersList", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public List<RetailerDetailsData> getRetailerList(@RequestParam String subArea, @RequestParam final String dealerCode) {
        return salesPlanningFacade.getRetailerList(subArea,dealerCode);
    }

    //Display Monthly sales target for dealers (Planned Target, Revised Target, Bucket1, Bucket 2 and Bucket3 as per dealer and sku wise)
    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/viewMonthlySalesTargetForDealers", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public MonthlySalesTargetSettingListData viewMonthlySalesTargetForDealers(@RequestParam String subArea,@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        return salesPlanningFacade.viewMonthlySalesTargetForDealers(subArea);
    }

    //Submit Monthly sales target for dealers (Planned Target, Revised Target, Bucket1, Bucket 2 and Bucket3 as per dealer and sku wise)
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/submitPlannedMonthlySalesTargetSettingForDealers", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public boolean submitPlannedMonthlySalesTargetSettingForDealers(@RequestBody MonthlySalesTargetSettingListData monthlySalesTargetSettingListData,
                                                                    @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return salesPlanningFacade.submitPlannedMonthlySalesTargetSettingForDealers(monthlySalesTargetSettingListData);
    }

    //Submit Revised Monthly sales target for dealers (Planned Target, Revised Target, Bucket1, Bucket 2 and Bucket3 as per dealer and sku wise)
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/submitRevisedMonthlySalesTargetSettingForDealers", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public boolean submitRevisedMonthlySalesTargetSettingForDealers(@RequestBody MonthlySalesTargetSettingListData monthlySalesTargetSettingListData,
                                                                    @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return salesPlanningFacade.submitRevisedMonthlySalesTargetSettingForDealers(monthlySalesTargetSettingListData);
    }

    //Planned Tab-Display Monthly sales target for dealers (Planned Target, Revised Target, Bucket1, Bucket 2 and Bucket3 as per dealer and sku wise)
    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/viewMonthlyPlannedSalesTargetForPlannedTab", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public MonthlySalesTargetSettingListData viewMonthlyPlannedSalesTargetForPlannedTab(@RequestParam String subArea,@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        return salesPlanningFacade.viewMonthlySalesTargetForPlannedTab(subArea);
    }

    //Api for action on revised target- Display Monthly revised sale for dealers (Total Planned Target, Revised Target, Bucket1, Bucket 2 and Bucket3 as per dealer and sku wise)
    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/viewMonthlyRevisedSalesTargetForRevisedTarget", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public MonthlySalesTargetSettingListData viewMonthlyRevisedSalesTargetForRevisedTarget(@RequestParam String subArea,@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        return salesPlanningFacade.viewMonthlyRevisedSalesTargetForRevisedTarget(subArea);
    }

    //Review Tab-Display Monthly sales target for dealers (Planned Target, Revised Target, Bucket1, Bucket 2 and Bucket3 as per dealer and sku wise)
    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/viewMonthlyRevisedSalesTargetForReviewTab", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public MonthlySalesTargetSettingListData viewMonthlyRevisedSalesTargetForReviewTab(@RequestParam String subArea,@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        return salesPlanningFacade.viewMonthlyRevisedSalesTargetForReviewTab(subArea);
    }

    //Review Tab-Submit Monthly sales target for dealers (Planned Target, Revised Target, Bucket1, Bucket 2 and Bucket3 as per dealer and sku wise)
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/submitMonthlySalesTargetForReviewTab", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public boolean submitMonthlySalesTargetForReviewTab(@RequestBody MonthlySalesTargetSettingListData monthlySalesTargetSettingListData,
                                                        @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return salesPlanningFacade.submitMonthlySalesTargetForReviewTab(monthlySalesTargetSettingListData);
    }

    //Api for Monthly sales plan summary with revised target,cy sales, planned sales,product mix and dealer category mix as per subarea, total planned sale and total cy sales monthwise
    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/viewMonthlySalesSummary", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public MonthlyTargetSettingSummaryData viewMonthlySalesSummary(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields, @RequestParam(required = false) boolean isMonthlySummaryAfterSubmitPlanned, @RequestParam(required = false) boolean isMonthlySummaryForReview, @RequestParam(required = false) boolean isMonthlySummaryAfterSubmitRevised, @RequestParam(required = false) boolean isMonthlySummaryAfterSubmitReviewed) {
        return salesPlanningFacade.viewMonthlySalesSummary(isMonthlySummaryAfterSubmitPlanned,isMonthlySummaryForReview,isMonthlySummaryAfterSubmitRevised,isMonthlySummaryAfterSubmitReviewed);
    }

    //Display saved dealer details, cy and plan sales targets (override previous saved record)
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/viewSavedAnnualSalesTargetSettingForDealers", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public AnnualSalesTargetSettingListData viewSavedAnnualSalesTargetSettingForDealers(@RequestParam String subArea, @RequestParam(required = false) String filter,
                                                                                        @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return salesPlanningFacade.viewSavedAnnualSalesTargetSettingForDealers(subArea);
    }

    //save/submit dealer details, cy and plan sales for setting new target
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/saveAnnualSalesTargetSettingForDealers", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public boolean saveAnnualSalesTargetSettingForDealers(@RequestBody AnnualSalesTargetSettingListData annualSalesTargetSettingListData, @RequestParam(required = true) String subArea,
                                                          @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return salesPlanningFacade.saveAnnualSalesTargetSettingForDealers(annualSalesTargetSettingListData,subArea);
    }

    //save/submit retailer details, cy and plan sales for setting new target
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/saveAnnSalesTargetSettingForRetailers", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public boolean saveAnnualSalesTargetSettingForRetailers(@RequestBody AnnualSalesTargetSettingData annualSalesTargetSettingData, @RequestParam(required = true) String subArea,
                                                            @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return salesPlanningFacade.saveAnnualSalesTargetSettingForRetailers(annualSalesTargetSettingData,subArea);
    }

    //save/submit Dealer's Annual sales review division in month wise (FY- april to march)
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/saveMonthWiseDealersForAnnSales", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public boolean saveMonthWiseDealersDetailsForAnnSales(@RequestBody AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData, @RequestParam(required = true) String subArea,
                                                          @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return salesPlanningFacade.saveMonthWiseDealersDetailsForAnnSales(annualSalesMonthWiseTargetData,subArea);
    }

    //save/submit retailer's Annual sales review division in month wise (FY- april to march)
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/saveMonthWiseRetailerForAnnSales", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public boolean saveMonthWiseRetailerForAnnSales(@RequestBody AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData, @RequestParam(required = true) String subArea,
                                                    @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return salesPlanningFacade.saveMonthWiseRetailerForAnnSales(annualSalesMonthWiseTargetData,subArea);
    }

    //Save/submit Review Sales for Dealers and showcase newly onboarded dealers month wise
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/saveOnboardedDealersForAnnSales", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public boolean saveOnboardedDealersForAnnSales(@RequestBody AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData, String subArea,
                                                   @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return salesPlanningFacade.saveOnboardedDealersForAnnSales(annualSalesMonthWiseTargetData,subArea);
    }

    //Save/submit Review Sales for Retailers and showcase newly onboarded dealers month wise
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/saveOnboardedRetailerForAnnSales", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public boolean saveOnboardedRetailerForAnnSales(@RequestBody AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData,
                                                    @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return salesPlanningFacade.saveOnboardedRetailerForAnnSales(annualSalesMonthWiseTargetListData);
    }

    /* Intermediate Save for Monthly sales planning */
    //Display Saved Monthly sales target for dealers (Planned Target, Revised Target, Bucket1, Bucket 2 and Bucket3 as per dealer and sku wise)
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/viewSavedMonthSalesTargetSetForDealers", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public MonthlySalesTargetSettingListData viewSavedMonthSalesTargetSetForDealers(@RequestParam String subArea, @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return salesPlanningFacade.viewSavedMonthSalesTargetSetForDealers(subArea);
    }

    //Save/Submit Monthly sales target for dealers (Planned Target, Revised Target, Bucket1, Bucket 2 and Bucket3 as per dealer and sku wise)
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/saveMonthlySalesTargetForDealers", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public boolean saveMonthlySalesTargetForDealer(@RequestBody MonthlySalesTargetSettingListData monthlySalesTargetSettingListData,
                                                   @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return salesPlanningFacade.saveMonthlySalesTargetForDealers(monthlySalesTargetSettingListData);
    }

    //Display Saved Revised Monthly Sales target(Planned Target, Revised Target, Bucket1, Bucket 2 and Bucket3 as per dealer and sku wise)
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/viewSavedRevMonthSalesTargetSetForDealers", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public MonthlySalesTargetSettingListData viewSavedRevMonthSalesTargetSetForDealers(@RequestParam String subArea, @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return salesPlanningFacade.viewSavedRevMonthSalesTargetSetForDealers(subArea);
    }

    //Save/Submit Revise Monthly Sales target (Planned Target, Revised Target, Bucket1, Bucket 2 and Bucket3 as per dealer and sku wise)
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/saveReviseMonthlySalesTargetForDealer", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public boolean saveReviseMonthlySalesTargetForDealer(@RequestBody MonthlySalesTargetSettingListData monthlySalesTargetSettingListData,
                                                         @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return salesPlanningFacade.saveReviseMonthlySalesTargetForDealer(monthlySalesTargetSettingListData);
    }

    //AnnualSales: Display Retailers Annual sales review division in month wise (FY- april to march) as per subarea - with Pagination
    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/viewMonthWiseRetailerDetailsWithPagination", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public AnnualSalesMonthWiseTargetListWsDTO viewMonthWiseRetailerDetailsWithPagination(@RequestParam String subArea,
                                                                                          @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
                                                                                          @RequestParam(required = false) String filter,
                                                                                          @Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
                                                                                          @Parameter(description = "Optional {@link PaginationData} parameter in case of savedCartsOnly == true. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,
                                                                                          final HttpServletResponse response) {

        AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData=new AnnualSalesMonthWiseTargetListData();

        final SearchPageData<Object> searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);

        SearchPageData<AnnualSalesMonthWiseTargetData> annualSalesMonthWiseTargetDataSearchPageData = salesPlanningFacade.viewMonthWiseRetailerDetailsForAnnualSalesPagination(searchPageData, subArea, filter);
        annualSalesMonthWiseTargetListData.setAnnualSalesMonthWiseTargetData(annualSalesMonthWiseTargetDataSearchPageData.getResults());
        SubAreaMasterModel subAreaMaster=territoryManagementService.getTerritoryById(subArea);
        annualSalesMonthWiseTargetListData.setAnnualSalesMonthWiseTargetData(annualSalesMonthWiseTargetDataSearchPageData.getResults());
        annualSalesMonthWiseTargetListData.setSubArea(subAreaMaster.getTaluka());
        annualSalesMonthWiseTargetListData.setSubAreaId(subAreaMaster.getPk().toString());

        List<Double> totalPlanAndCySales = salesPlanningFacade.getTotalPlanAndCySalesRetailerPlanned(searchPageData, subArea);
        annualSalesMonthWiseTargetListData.setTotalPlanSales(totalPlanAndCySales.get(0));
        annualSalesMonthWiseTargetListData.setTotalCurrentYearSales(totalPlanAndCySales.get(1));
        if (annualSalesMonthWiseTargetDataSearchPageData.getPagination() != null)
        {
            response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(annualSalesMonthWiseTargetDataSearchPageData.getPagination().getTotalNumberOfResults()));
        }
        return getDataMapper().map(annualSalesMonthWiseTargetListData, AnnualSalesMonthWiseTargetListWsDTO.class, fields);
    }

    //Annual sales summary planned tab- implement retailer level plan filter - With Pagination
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/viewPlannedSalesforRetailerMonthwiseWithPagination", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public AnnualSalesMonthWiseTargetListWsDTO viewPlannedSalesforRetailerMonthwiseWithPagination(@RequestParam String subArea,
                                                                                                 @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
                                                                                                 @RequestParam(required = false) String filter,
                                                                                                 @Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
                                                                                                 @Parameter(description = "Optional {@link PaginationData} parameter in case of savedCartsOnly == true. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,
                                                                                                 final HttpServletResponse response)
    {
        AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData=new AnnualSalesMonthWiseTargetListData();

        final SearchPageData<Object> searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);

        SearchPageData<AnnualSalesMonthWiseTargetData> annualSalesMonthWiseTargetDataSearchPageData =  salesPlanningFacade.viewPlannedSalesforRetailerMonthwise(searchPageData,subArea, filter);
        annualSalesMonthWiseTargetListData.setAnnualSalesMonthWiseTargetData(annualSalesMonthWiseTargetDataSearchPageData.getResults());
        SubAreaMasterModel subAreaMaster=territoryManagementService.getTerritoryById(subArea);
        annualSalesMonthWiseTargetListData.setAnnualSalesMonthWiseTargetData(annualSalesMonthWiseTargetDataSearchPageData.getResults());
        annualSalesMonthWiseTargetListData.setSubArea(subAreaMaster.getTaluka());
        annualSalesMonthWiseTargetListData.setSubAreaId(subAreaMaster.getPk().toString());

        List<Double> totalPlanAndCySales = salesPlanningFacade.getTotalPlanAndCySalesRetailerPlanned(searchPageData, subArea);
        annualSalesMonthWiseTargetListData.setTotalPlanSales(totalPlanAndCySales.get(0));
        annualSalesMonthWiseTargetListData.setTotalCurrentYearSales(totalPlanAndCySales.get(1));

        if (annualSalesMonthWiseTargetDataSearchPageData.getPagination() != null)
        {
            response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(annualSalesMonthWiseTargetDataSearchPageData.getPagination().getTotalNumberOfResults()));
        }
        return getDataMapper().map(annualSalesMonthWiseTargetListData, AnnualSalesMonthWiseTargetListWsDTO.class, fields);
    }

    //Display Review Sales for Retailers and showcase newly onboarded Retailers month wise (previously saved record)
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/viewReviewedSalesforRetailerMonthWiseWithPagination", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public AnnualSalesMonthWiseTargetListWsDTO viewReviewedSalesforRetailerMonthWiseWithPagination(@RequestParam String subArea,
                                                                                                   @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
                                                                                                   @RequestParam(required = false) String filter,
                                                                                                   @Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
                                                                                                   @Parameter(description = "Optional {@link PaginationData} parameter in case of savedCartsOnly == true. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,
                                                                                                   final HttpServletResponse response)
    {

        AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData=new AnnualSalesMonthWiseTargetListData();

        final SearchPageData<Object> searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);


        SearchPageData<AnnualSalesMonthWiseTargetData> annualSalesMonthWiseTargetDataSearchPageData =  salesPlanningFacade.viewReviewedSalesforRetailesMonthwise(searchPageData,subArea,filter);
        SearchPageData<SpOnboardAnnualTargetSettingData> onboardedData =  salesPlanningFacade.viewReviewedSalesforRetailesMonthwiseOnboarded(searchPageData,subArea,filter);
        annualSalesMonthWiseTargetListData.setAnnualSalesMonthWiseTargetData(annualSalesMonthWiseTargetDataSearchPageData.getResults());
        annualSalesMonthWiseTargetListData.setOnbordedTargetSetData(onboardedData.getResults());

        SubAreaMasterModel subAreaMaster=territoryManagementService.getTerritoryById(subArea);
        annualSalesMonthWiseTargetListData.setAnnualSalesMonthWiseTargetData(annualSalesMonthWiseTargetDataSearchPageData.getResults());
        annualSalesMonthWiseTargetListData.setSubArea(subAreaMaster.getTaluka());
        annualSalesMonthWiseTargetListData.setSubAreaId(subAreaMaster.getPk().toString());

        if (annualSalesMonthWiseTargetDataSearchPageData.getPagination() != null)
        {
            response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(annualSalesMonthWiseTargetDataSearchPageData.getPagination().getTotalNumberOfResults()));
        }
        return getDataMapper().map(annualSalesMonthWiseTargetListData, AnnualSalesMonthWiseTargetListWsDTO.class, fields);
    }

    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/alertForSalesPlanHighPriorityAction", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public SalesHighPriorityActionData sendAlertForSalesPlanHighPriorityAction() {
        return salesPlanningFacade.sendAlertForSalesPlanHighPriorityAction();
    }

    //TSM Annual sales summary page (currentyearsale, plannedyear sale,dealer mix, summary of subareawise product mix, all same for each subarea)
    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/viewAnnualSalesSummaryForTSM", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public AnnualTargetSettingSummaryData viewAnnualSalesSummaryForTSM(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields, @RequestParam(required = false) boolean isAnnualSummaryForReview, @RequestParam(required = false) boolean isAnnualSummaryAfterReview) {
        return salesPlanningFacade.viewAnnualSalesSummaryTSM(isAnnualSummaryForReview, isAnnualSummaryAfterReview);
    }

    //RH Annual sales summary page (currentyearsale, plannedyear sale,dealer mix, summary of subareawise product mix, all same for each subarea)
    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/viewAnnualSalesSummaryForRH", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public AnnualTargetSettingSummaryData viewAnnualSalesSummaryForRH(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields, @RequestParam(required = false) boolean isAnnualSummaryForReview, @RequestParam(required = false) boolean isAnnualSummaryAfterReview) {
        return salesPlanningFacade.viewAnnualSalesSummaryRH(isAnnualSummaryForReview, isAnnualSummaryAfterReview);
    }

    //Api to get (year/month/duedate) for annual and monthly sales planning
    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/showMonthYearDueDateForTSMRH", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public SalesPlanningMonthYearDueDateData showMonthYearDueDateForTSMRH(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        return salesPlanningFacade.showMonthYearDueDateForTSMRH();
    }
    //Api to view list of monthwise buckets target as per subarea
    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/viewBucketwiseRequestForTSMRH", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ViewBucketwiseRequestList viewBucketwiseRequestForTSMRH(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,@RequestParam(required = false) String searchKey) {
        return salesPlanningFacade.viewBucketwiseRequestForTSMRH(searchKey);
    }
    //Api to view list of monthwise buckets target as per subarea
    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/monthwiseSummaryForTSM", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ViewBucketwiseRequest monthwiseSummaryForTSM(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,@RequestParam(required = false) String searchKey) {
        return salesPlanningFacade.monthwiseSummaryForTSM();
    }
    //TSM-Review Tab-Display Monthly sales target for dealers (Planned Target, Revised Target, Bucket1, Bucket 2 and Bucket3 as per dealer and sku wise)
    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/viewMonthlyRevisedSalesTargetForReviewTabForTSM", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public MonthlySalesTargetSettingListData viewMonthlyRevisedSalesTargetForReviewTabForTSM(@RequestParam String subArea,@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        return salesPlanningFacade.viewMonthlyRevisedSalesTargetForReviewTabForTSM(subArea);
    }
    //RH-Review Tab-Display Monthly sales target for dealers (Planned Target, Revised Target, Bucket1, Bucket 2 and Bucket3 as per dealer and sku wise)
    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/viewMonthlyRevisedSalesTargetForReviewTabForRH", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public MonthlySalesTargetSettingListData viewMonthlyRevisedSalesTargetForReviewTabForRH(@RequestParam String subArea,@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        return salesPlanningFacade.viewMonthlyRevisedSalesTargetForReviewTabForRH(subArea);
    }

    //SclWorkflow - targetSendForRevision to SO by tsm and DI by rh - TSM and RH persona
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/targetSendForRevision", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public SalesTargetApprovedData targetSendForRevision(@RequestBody SalesRevisedTargetData salesRevisedTargetData,
                                                        @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return salesPlanningFacade.targetSendForRevision(salesRevisedTargetData);
    }

    //SclWorkflow - Target Approve By TSM/RH
    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/updateTargetStatusForApproval", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public  SalesTargetApprovedData updateTargetStatusForApproval(@RequestBody SalesApprovalData salesApprovalData, @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        return salesPlanningFacade.updateTargetStatusForApproval(salesApprovalData);
    }

    //TSM Review - list of monthwise targets for each subarea
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/reviewMonthwiseTargetsForDealer", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public AnnualSalesMonthWiseTargetListData reviewMonthwiseTargetsForDealer(@RequestParam String subArea, @RequestParam(required = false) String filter)
    {
        return salesPlanningFacade.reviewMonthwiseTargetsForDealer(subArea,filter);
    }

    //Api for Monthly sales plan summary with revised target,cy sales, planned sales,product mix and dealer category mix as per subarea, total planned sale and total cy sales monthwise - FOR TSM
    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/viewMonthlySalesSummaryForTSM", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public MonthlyTargetSettingSummaryData viewMonthlySalesSummaryForTSM(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields, @RequestParam(required = false) boolean isMonthlySummaryForReview, @RequestParam(required = false) boolean isMonthlySummaryAfterSubmitReviewed) {
        return salesPlanningFacade.viewMonthlySalesSummaryForTSM(isMonthlySummaryForReview,isMonthlySummaryAfterSubmitReviewed);
    }
    //Api for Monthly sales plan summary with revised target,cy sales, planned sales,product mix and dealer category mix as per subarea, total planned sale and total cy sales monthwise - FOR RH
    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/viewMonthlySalesSummaryForRH", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public MonthlyTargetSettingSummaryData viewMonthlySalesSummaryForRH(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields, @RequestParam(required = false) boolean isMonthlySummaryForReview, @RequestParam(required = false) boolean isMonthlySummaryAfterSubmitReviewed) {
        return salesPlanningFacade.viewMonthlySalesSummaryForRH(isMonthlySummaryForReview,isMonthlySummaryAfterSubmitReviewed);
    }

    //RH Review - list of monthwise targets for each district
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/reviewAnnualSalesMonthwiseTargetsForRH", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public AnnualSalesReviewListData reviewAnnualSalesMonthwiseTargetsForRH(@RequestParam String district, @RequestParam(required = false) String filter)
    {
        return salesPlanningFacade.reviewAnnualSalesMonthwiseTargetsForRH(district,filter);
    }

    //SclWorkflow -Api to send all approved subarea target of the district to the RH
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/sendApprovedTargetToUser", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public boolean sendApprovedTargetToUser(@RequestParam boolean isTargetSetForUser,
                                         @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return salesPlanningFacade.sendApprovedTargetToUser(isTargetSetForUser);
    }

    //SclWorkflow -Api to send all approved subarea target of the district to the RH
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/updateStatusForBucketApproval", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public SalesTargetApprovedData updateStatusForBucketApproval(@RequestBody ViewBucketwiseRequest viewBucketwiseRequest,
                                            @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return salesPlanningFacade.updateStatusForBucketApproval(viewBucketwiseRequest);
    }

    //SclWorkflow - targetSendForRevision to SO by tsm and DI by rh - TSM and RH persona Monthly
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/targetSendForRevisionForMonthly", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public SalesTargetApprovedData targetSendForRevisionForMonthly(@RequestBody SalesRevisedTargetData salesRevisedTargetData,
                                         @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return salesPlanningFacade.targetSendForRevisionForMonthly(salesRevisedTargetData);
    }

    //SclWorkflow - Target Approve By TSM/RH Monthly
    @Secured({SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value = "/updateTargetStatusForApprovalMonthly", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public SalesTargetApprovedData updateTargetStatusForApprovalMonthly(@RequestBody SalesApprovalData salesApprovalData,@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        return salesPlanningFacade.updateTargetStatusForApprovalMonthly(salesApprovalData);
    }
}
