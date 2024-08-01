package com.scl.facades.efficacy.report;

import com.scl.facades.data.EfficacyReportData;
import com.scl.facades.data.IncreaseSalesVolumeReportListData;
import com.scl.facades.data.MarketIntelligenceData;
import com.scl.facades.data.NewProductReportListData;
import com.scl.facades.data.ObsoleteCounterReportListData;
import com.scl.facades.data.OutstandingDueReportData;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface EfficacyReportFacade
{
    EfficacyReportData getEfficacyReportForMonth(Integer month, Integer year, String subarea);

    IncreaseSalesVolumeReportListData viewDetailsIncreaseSalesVolReport(String efficacyReportId);
    List<MarketIntelligenceData> getMarketIntelligenceReport(String efficacyId, String brandCode, String productCode);

    List<MarketIntelligenceData> getMarketIntelligenceReports(String efficacyId);

    List<OutstandingDueReportData> getOutStandingDueReports(String efficacyId);
    
    ObsoleteCounterReportListData getObsoleteCountersReports(String efficacyId);
    
    NewProductReportListData getNewProductReports(String efficacyId);
}


