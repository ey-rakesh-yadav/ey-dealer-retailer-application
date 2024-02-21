package com.eydms.facades.efficacy.report.impl;

import com.eydms.core.model.EfficacyNewProductReportModel;
import com.eydms.core.model.EfficacyReportMasterModel;
import com.eydms.core.model.MarketIntelligenceReportModel;
import com.eydms.core.model.OutstandingDueReportModel;
import com.eydms.core.model.ReinclusionObsoleteCounterReportModel;
import com.eydms.core.services.EfficacyReportService;
import com.eydms.facades.data.EfficacyReportData;
import com.eydms.facades.data.IncreaseSalesVolumeReportListData;
import com.eydms.facades.data.MarketIntelligenceData;
import com.eydms.facades.data.NewProductReportData;
import com.eydms.facades.data.NewProductReportListData;
import com.eydms.facades.data.ObsoleteCounterReportData;
import com.eydms.facades.data.ObsoleteCounterReportListData;
import com.eydms.facades.data.OutstandingDueReportData;
import com.eydms.facades.efficacy.report.EfficacyReportFacade;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.List;

import javax.annotation.Resource;

public class EfficacyReportFacadeImpl implements EfficacyReportFacade
{
    EfficacyReportService efficacyReportService;
    Converter<EfficacyReportMasterModel, EfficacyReportData> efficacyReportConverter;
    Converter<MarketIntelligenceReportModel,MarketIntelligenceData>  marketIntelligenceReportConverter;

    Converter<OutstandingDueReportModel, OutstandingDueReportData> outstandingDueReportConverter;
    
    @Resource
    Converter<ReinclusionObsoleteCounterReportModel,ObsoleteCounterReportData> obsoleteCountersReportConverter;
    
    @Resource
    Converter<EfficacyNewProductReportModel,NewProductReportData> efficacyNewProductReportConverter;
    
	@Override
    public EfficacyReportData getEfficacyReportForMonth(Integer month, Integer year, String subarea) {
        EfficacyReportMasterModel efficacyReportMasterModel = getEfficacyReportService().getEfficacyReportForMonth(month,year,subarea);
        EfficacyReportData efficacyReportData = new EfficacyReportData();
        getEfficacyReportConverter().convert(efficacyReportMasterModel,efficacyReportData);
        return efficacyReportData;
    }

    @Override
    public IncreaseSalesVolumeReportListData viewDetailsIncreaseSalesVolReport(String efficacyReportId) {
        return efficacyReportService.viewDetailsIncreaseSalesVolReport(efficacyReportId);
    }

    @Override
    public List<MarketIntelligenceData> getMarketIntelligenceReport(String efficacyId, String brandCode, String productCode){
        List<MarketIntelligenceReportModel> marketIntelligenceReportModels =getEfficacyReportService().getMarketIntelligenceReport(efficacyId, brandCode, productCode);
        return getMarketIntelligenceReportConverter().convertAll(marketIntelligenceReportModels);
    }

    @Override
    public List<MarketIntelligenceData> getMarketIntelligenceReports(String efficacyId){
        List<MarketIntelligenceReportModel> marketIntelligenceReportList = getEfficacyReportService().getMarketIntelligenceReports(efficacyId);
        return getMarketIntelligenceReportConverter().convertAll(marketIntelligenceReportList);
    }

    @Override
    public List<OutstandingDueReportData> getOutStandingDueReports(String efficacyId) {
        List<OutstandingDueReportModel> outstandingDueReportModelList = getEfficacyReportService().getOutStandingDueReports(efficacyId);
        return getOutstandingDueReportConverter().convertAll(outstandingDueReportModelList);
    }

    public EfficacyReportService getEfficacyReportService() {
        return efficacyReportService;
    }

    public void setEfficacyReportService(EfficacyReportService efficacyReportService) {
        this.efficacyReportService = efficacyReportService;
    }

    public Converter<EfficacyReportMasterModel, EfficacyReportData> getEfficacyReportConverter() {
        return efficacyReportConverter;
    }

    public void setEfficacyReportConverter(Converter<EfficacyReportMasterModel, EfficacyReportData> efficacyReportConverter) {
        this.efficacyReportConverter = efficacyReportConverter;
    }

    public Converter<MarketIntelligenceReportModel, MarketIntelligenceData> getMarketIntelligenceReportConverter() {
        return marketIntelligenceReportConverter;
    }

    public void setMarketIntelligenceReportConverter(Converter<MarketIntelligenceReportModel, MarketIntelligenceData> marketIntelligenceReportConverter) {
        this.marketIntelligenceReportConverter = marketIntelligenceReportConverter;
    }

    public Converter<OutstandingDueReportModel, OutstandingDueReportData> getOutstandingDueReportConverter() {
        return outstandingDueReportConverter;
    }

    public void setOutstandingDueReportConverter(Converter<OutstandingDueReportModel, OutstandingDueReportData> outstandingDueReportConverter) {
        this.outstandingDueReportConverter = outstandingDueReportConverter;
    }

	@Override
	public ObsoleteCounterReportListData getObsoleteCountersReports(String efficacyId) {
		List<ReinclusionObsoleteCounterReportModel> modelList = getEfficacyReportService().getObsoleteCountersReports(efficacyId);
		ObsoleteCounterReportListData data = new ObsoleteCounterReportListData();
		data.setObsoleteCounterReportData(obsoleteCountersReportConverter.convertAll(modelList));
		return data;
	}

	@Override
	public NewProductReportListData getNewProductReports(String efficacyId) {
		List<EfficacyNewProductReportModel> modelList = getEfficacyReportService().getNewProductReports(efficacyId);
		NewProductReportListData data = new NewProductReportListData();
		data.setNewProductReportData(efficacyNewProductReportConverter.convertAll(modelList));
		return data;
	}
}
