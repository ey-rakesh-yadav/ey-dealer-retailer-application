package com.eydms.facades.efficacy.report;

import com.eydms.facades.data.EfficacyReportData;
import com.eydms.facades.data.IncreaseSalesVolumeReportListData;
import com.eydms.facades.data.MarketIntelligenceData;
import com.eydms.facades.data.NewProductReportListData;
import com.eydms.facades.data.ObsoleteCounterReportListData;
import com.eydms.facades.data.OutstandingDueReportData;
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


