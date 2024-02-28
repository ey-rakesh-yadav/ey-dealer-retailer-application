package com.eydms.occ.controllers;

import com.eydms.facades.TSOSiteManagementFacade;
import com.eydms.facades.data.*;
import com.eydms.facades.depot.operations.data.DepotStockData;
import com.eydms.occ.annotation.ApiBaseSiteIdAndUserIdAndTerritoryParam;
import com.eydms.occ.security.EyDmsSecuredAccessConstants;

import com.eydms.occ.dto.DropdownListWsDTO;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Controller
@RequestMapping(value = "/{baseSiteId}/tsoSite")
@ApiVersion("v2")
@Tag(name = "TSO Site Management Controller")
public class TSOSiteManagementController extends EyDmsBaseController {

    @Resource
    TSOSiteManagementFacade tsoSiteManagementFacade;

    //Quality Test Api's
    //Material Test
    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @RequestMapping(value = "/submitMaterialTest", method = RequestMethod.POST)
    @Operation(operationId = "submitMaterialTest", summary = "Submit Material Test Details")
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ResponseEntity<String> submitMaterialTest(@RequestBody QualityTestListData qualityTestListData) {
        var materialTestId = tsoSiteManagementFacade.submitMaterialTest(qualityTestListData);
        return ResponseEntity.status(HttpStatus.CREATED).body(materialTestId);
    }

    //Material Test
    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @RequestMapping(value = "/submitConcreteTest", method = RequestMethod.POST)
    @Operation(operationId = "submitConcreteTest", summary = "Submit Concrete Test Details")
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ResponseEntity<String> submitConcreteTest(@RequestBody QualityTestListData qualityTestListData) {
        var concreteTestId = tsoSiteManagementFacade.submitConcreteTest(qualityTestListData);
        return ResponseEntity.status(HttpStatus.CREATED).body(concreteTestId);
    }

    //Other test
    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @RequestMapping(value = "/submitOtherTest", method = RequestMethod.POST)
    @Operation(operationId = "submitOtherTest", summary = "Submit Other Test Details")
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ResponseEntity<String> submitOtherTest(@RequestBody QualityTestListData qualityTestListData) {
        var otherTestId = tsoSiteManagementFacade.submitOtherTest(qualityTestListData);
        return ResponseEntity.status(HttpStatus.CREATED).body(otherTestId);
    }

    //Sales from trade site - 1st and 2nd purchase details
    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @RequestMapping(value = "/submitSalesFromTradeSite", method = RequestMethod.POST)
    @Operation(operationId = "submitSalesFromTradeSite", summary = "Submit Sales From Trade Site")
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public boolean submitSalesFromTradeSite(@RequestBody SalesFromTradeSiteListData salesFromTradeSiteListData) {
        return tsoSiteManagementFacade.submitSalesFromTradeSite(salesFromTradeSiteListData);
    }


    //Sales Demonstration for Site
    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @RequestMapping(value = "/submitSiteDemo", method = RequestMethod.POST)
    @Operation(operationId = "submitSiteDemo", summary = "Submit Site Demonstration")
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public ResponseEntity<List<SiteIdData>> submitSiteDemonstration(@RequestBody SiteDemonstrationListData siteDemonstrationListData) {
        List<SiteIdData> siteIdData = tsoSiteManagementFacade.submitSiteDemonstration(siteDemonstrationListData);
        return ResponseEntity.status(HttpStatus.OK).body(siteIdData);
    }

    //Submit Site Visit Form
    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @RequestMapping(value = "/submitSiteVisitForm", method = RequestMethod.POST)
    @Operation(operationId = "submitSiteVisitForm", summary = "Submit Site Visit Form")
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public boolean submitSiteVisitForm(@RequestBody TSOSiteVisitFormData siteVisitFormData) {
        return tsoSiteManagementFacade.submitSiteVisitForm(siteVisitFormData);
    }

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @RequestMapping(value = "/getAllSiteDemos/{tsoUserCode}", method = RequestMethod.GET)
    @Operation(operationId = "getAllSiteDemos", summary = "Get All the Site Demonstrations details")
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public SiteDemonstrationListData getAllSiteDemos(@PathVariable String tsoUserCode) {
        return tsoSiteManagementFacade.getAllSiteDemonstrations(tsoUserCode);
    }
  
    //Select Site Stages based on Site Demo
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/getSiteDemoStages/{demoName}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public DropdownListWsDTO getSiteDemoStages(@PathVariable String demoName, @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return getDataMapper().map(tsoSiteManagementFacade.getSiteDemoStages(demoName),DropdownListWsDTO.class,fields);
    }

    //Submit Annual Budget
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/submitAnnualBudgetSite", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public boolean submitAnnualBudgetSite(@RequestBody SiteAnnualBudgetData siteAnnualBudgetData)
    {
        return tsoSiteManagementFacade.submitAnnualBudgetSite(siteAnnualBudgetData);
    }

    //Select site feedback
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(value="/submitSiteFeedback", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public boolean submitSiteFeedback(@RequestBody SiteFeedbackListData siteFeedbackData)
    {
        return tsoSiteManagementFacade.submitSiteFeedback(siteFeedbackData);
    }
    
    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @RequestMapping(value = "/getSiteAnnualBudgets/{tsoUserCode}", method = RequestMethod.GET)
    @Operation(operationId = "getSiteAnnualBudgets", summary = "Get All the Site Annual Budgets")
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public SiteAnnualBudgetListData getSiteAnnualBudgets(@PathVariable String tsoUserCode) {
        return tsoSiteManagementFacade.getSiteAnnualBudgets(tsoUserCode);
    }
    
    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @RequestMapping(value = "/getSiteTestReports/{tsoUserCode}", method = RequestMethod.GET)
    @Operation(operationId = "getSiteTestReports", summary = "Get all Quality Test Reports for the Site")
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public QualityTestReportListData getSiteTestReports(@PathVariable String tsoUserCode) {
        return tsoSiteManagementFacade.getQualityTestReports(tsoUserCode);
    }
}
