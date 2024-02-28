package com.eydms.core.services;

import com.eydms.core.model.EfficacyNewProductReportModel;
import com.eydms.core.model.EfficacyReportMasterModel;
import com.eydms.facades.data.IncreaseSalesVolumeReportListData;
import com.eydms.facades.data.ObsoleteCounterReportData;
import com.eydms.core.model.MarketIntelligenceReportModel;
import com.eydms.core.model.OutstandingDueReportModel;
import com.eydms.core.model.ReinclusionObsoleteCounterReportModel;

import java.util.List;

public interface EfficacyReportService {

	public boolean saveIncreaseSalesVolumeReport();
	EfficacyReportMasterModel getEfficacyReportForMonth(Integer month, Integer year, String subarea);

    IncreaseSalesVolumeReportListData viewDetailsIncreaseSalesVolReport(String efficacyReportId);
	List<MarketIntelligenceReportModel> getMarketIntelligenceReport(String efficacyId, String brandCode, String productCode);

	List<MarketIntelligenceReportModel> getMarketIntelligenceReports(String efficacyId);

	List<OutstandingDueReportModel> getOutStandingDueReports(String efficacyId);
	
	List<ReinclusionObsoleteCounterReportModel> getObsoleteCountersReports(String efficacyId);
	
	List<EfficacyNewProductReportModel> getNewProductReports(String efficacyId);
}
