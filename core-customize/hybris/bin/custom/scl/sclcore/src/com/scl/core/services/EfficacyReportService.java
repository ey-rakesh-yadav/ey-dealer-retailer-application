package com.scl.core.services;

import com.scl.core.model.EfficacyNewProductReportModel;
import com.scl.core.model.EfficacyReportMasterModel;
import com.scl.facades.data.IncreaseSalesVolumeReportListData;
import com.scl.facades.data.ObsoleteCounterReportData;
import com.scl.core.model.MarketIntelligenceReportModel;
import com.scl.core.model.OutstandingDueReportModel;
import com.scl.core.model.ReinclusionObsoleteCounterReportModel;

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
