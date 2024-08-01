package com.scl.occ.controllers;

import com.scl.facades.data.EfficacyReportData;
import com.scl.facades.data.IncreaseSalesVolumeReportListData;
import com.scl.facades.data.MarketIntelligenceData;
import com.scl.facades.data.NewProductReportListData;
import com.scl.facades.data.ObsoleteCounterReportListData;
import com.scl.facades.data.OutstandingDueReportData;
import com.scl.facades.efficacy.report.EfficacyReportFacade;

import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdParam;

import io.swagger.v3.oas.annotations.tags.Tag;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;
import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import java.util.List;

@RestController
@RequestMapping(value = "{baseSiteId}/efficacyreport/")
@ApiVersion("v2")
@Tag(name = "Efficacy Report")
@PermitAll
public class EfficacyReportController extends SclBaseController {
    private static final Logger LOG = Logger.getLogger(EfficacyReportController.class);
    @Resource
    EfficacyReportFacade efficacyReportFacade;

    @RequestMapping(value = "/getEfficacyReportForMonth", method = RequestMethod.GET)
    @ResponseBody
    @ApiBaseSiteIdParam
    public EfficacyReportData getEfficacyReportForMonth(@Parameter(description = "month") @RequestParam Integer month,@Parameter(description = "year") @RequestParam Integer year,@Parameter(description = "subArea") @RequestParam String subArea) {
        return efficacyReportFacade.getEfficacyReportForMonth(month, year, subArea);
    }

    @RequestMapping(value = "/viewDetailsIncreaseSalesVolReport", method = RequestMethod.POST)
    @ResponseBody
    @ApiBaseSiteIdParam
    public IncreaseSalesVolumeReportListData viewDetailsIncreaseSalesVolReport(@Parameter(description = "efficacyReportId") @RequestParam String efficacyReportId) {
        return efficacyReportFacade.viewDetailsIncreaseSalesVolReport(efficacyReportId);
    }

    @RequestMapping(value = "/getMarketIntelligenceReport/{efficacyId}", method = RequestMethod.GET)
    @ResponseBody
    @ApiBaseSiteIdParam
    public List<MarketIntelligenceData> viewMarketIntelligenceReport(@Parameter(description = "efficacyId") @PathVariable String efficacyId,@Parameter(description = "brandCode") @RequestParam String brandCode,@Parameter(description = "productCode") @RequestParam String productCode){
        return efficacyReportFacade.getMarketIntelligenceReport(efficacyId, brandCode, productCode);
    }

    @RequestMapping(value = "/getMarketIntelligenceReports/{efficacyId}", method = RequestMethod.GET)
    @ResponseBody
    @ApiBaseSiteIdParam
    public List<MarketIntelligenceData> getMarketIntelligenceReports(@Parameter(description = "efficacyId") @PathVariable String efficacyId) {
        return efficacyReportFacade.getMarketIntelligenceReports(efficacyId);
    }
    
    @RequestMapping(value = "/getOutStandingDueReport/{efficacyId}", method = RequestMethod.GET)
    @ResponseBody
    @ApiBaseSiteIdParam
    public List<OutstandingDueReportData> getOutStandingDueReports(@Parameter(description = "efficacyId") @PathVariable String efficacyId) {
        return efficacyReportFacade.getOutStandingDueReports(efficacyId);
    }
    
    @RequestMapping(value = "/getObsoleteCountersReport/{efficacyId}", method = RequestMethod.GET)
    @ResponseBody
    @ApiBaseSiteIdParam
    public ObsoleteCounterReportListData getObsoleteCountersReports(@Parameter(description = "efficacyId") @PathVariable String efficacyId) {
        return efficacyReportFacade.getObsoleteCountersReports(efficacyId);
    }
    
    @RequestMapping(value = "/getNewProductReports/{efficacyId}", method = RequestMethod.GET)
    @ResponseBody
    @ApiBaseSiteIdParam
    public NewProductReportListData getNewProductReports(@Parameter(description = "efficacyId") @PathVariable String efficacyId) {
        return efficacyReportFacade.getNewProductReports(efficacyId);
    }
    

}
