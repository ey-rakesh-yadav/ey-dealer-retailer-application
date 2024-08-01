package com.scl.core.services.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.List;
import java.util.stream.Collectors;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.jalo.EfficacyReportMaster;
import com.scl.core.model.DJPRouteScoreMasterModel;
import com.scl.core.model.EfficacyNewProductReportModel;
import com.scl.core.model.EfficacyReportMasterModel;
import com.scl.core.model.IncreaseSalesVolumeReportModel;
import com.scl.facades.data.IncreaseSalesVolumeReportData;
import com.scl.facades.data.IncreaseSalesVolumeReportListData;
import com.scl.facades.data.ObsoleteCounterReportData;
import com.scl.core.model.MarketIntelligenceReportModel;
import com.scl.core.model.OutstandingDueReportModel;
import com.scl.core.model.ReinclusionObsoleteCounterReportModel;
import com.scl.core.model.SubAreaMasterModel;

import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.dao.EfficacyReportDao;
import com.scl.core.services.EfficacyReportService;
import com.scl.core.services.TerritoryManagementService;

public class EfficacyReportServiceImpl implements EfficacyReportService {

	@Autowired
	EfficacyReportDao efficacyReportDao;

	@Autowired
	UserService userService;

	@Autowired
	FlexibleSearchService flexibleSearchService;
	
	@Autowired
	TerritoryManagementService territoryService;

	@Override
	public boolean saveIncreaseSalesVolumeReport() {
		LocalDate date = LocalDate.now().minusMonths(1);
		int month = date.getMonthValue();
		int year = date.getYear();
		SubAreaMasterModel subarea = null;
		efficacyReportDao.findCounterVisitForMonthYear(month, year, subarea);
		return false;
	}

	@Override
	public EfficacyReportMasterModel getEfficacyReportForMonth(Integer month, Integer year, String subarea) {
		UserModel sclUser = userService.getCurrentUser();
		//New Territory Change
		return efficacyReportDao.getEfficacyReportForMonth(month, year, territoryService.getTerritoryById(subarea), sclUser);
	}

	@Override
	public IncreaseSalesVolumeReportListData viewDetailsIncreaseSalesVolReport(String efficacyReportId) {
		EfficacyReportMasterModel efficacyReport = efficacyReportDao.getEfficacyReportsMaster(efficacyReportId);
		IncreaseSalesVolumeReportListData increaseSalesVolumeReportListData = new IncreaseSalesVolumeReportListData();
		if (efficacyReport.getIncreaseSalesVolumeReports() != null && !efficacyReport.getIncreaseSalesVolumeReports().isEmpty()) {
			List<IncreaseSalesVolumeReportData> reportDataList = new ArrayList<>();
			for (IncreaseSalesVolumeReportModel increaseSalesVolumeReport : efficacyReport.getIncreaseSalesVolumeReports()) {
				IncreaseSalesVolumeReportData increaseSalesVolumeReportData = new IncreaseSalesVolumeReportData();
				increaseSalesVolumeReportData.setCounterCode(increaseSalesVolumeReport.getCounterCode());
				increaseSalesVolumeReportData.setCounterName(increaseSalesVolumeReport.getCounterName());
				if(increaseSalesVolumeReport.getVisitDate()!=null) {
					DateFormat dateFormat = new SimpleDateFormat(SclCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
					String visitDate = dateFormat.format(increaseSalesVolumeReport.getVisitDate());
					increaseSalesVolumeReportData.setDateOfVisit(visitDate);
				}
				increaseSalesVolumeReportData.setGrowth(increaseSalesVolumeReport.getGrowth());
				increaseSalesVolumeReportData.setDailySalesAfterVisit(increaseSalesVolumeReport.getAfterSales());
				increaseSalesVolumeReportData.setDailySalesBeforeVisit(increaseSalesVolumeReport.getBeforeSales());
				increaseSalesVolumeReportData.setCounterCategory(increaseSalesVolumeReport.getCounterType().getCode());
				reportDataList.add(increaseSalesVolumeReportData);
			}
			increaseSalesVolumeReportListData.setTotalNumberOfVisits(efficacyReport.getTotalNumberOfVisits());
			increaseSalesVolumeReportListData.setTotalCountersVisited(efficacyReport.getTotalCounterVisited());
			increaseSalesVolumeReportListData.setCumulativeSalesBeforeVisit(efficacyReport.getSalesBeforeVisit());
			increaseSalesVolumeReportListData.setCumulativeSalesAfterVisit(efficacyReport.getSalesAfterVisit());
			increaseSalesVolumeReportListData.setCumulativeSalesGrowth(efficacyReport.getGrowth());
			increaseSalesVolumeReportListData.setIncreaseSalesVolume(reportDataList);

		}
		return increaseSalesVolumeReportListData;
	}

    @Override
	public List<MarketIntelligenceReportModel> getMarketIntelligenceReport(String efficacyId, String brandCode, String productCode) {
		EfficacyReportMasterModel efficacyReportMaster = efficacyReportDao.getEfficacyReportsMaster(efficacyId);
		return efficacyReportMaster.getMarketIntelligenceReport().stream().filter(report -> (brandCode.equalsIgnoreCase(report.getBrandCode()) && productCode.equalsIgnoreCase(report.getProductCode()))).collect(Collectors.toList());
	}

	@Override
	public List<MarketIntelligenceReportModel> getMarketIntelligenceReports(String efficacyId) {
		EfficacyReportMasterModel efficacyReportMaster = efficacyReportDao.getEfficacyReportsMaster(efficacyId);
		return CollectionUtils.isEmpty(efficacyReportMaster.getMarketIntelligenceReport())? new ArrayList():  efficacyReportMaster.getMarketIntelligenceReport().stream().collect(Collectors.toList());
	}

	@Override
	public List<OutstandingDueReportModel> getOutStandingDueReports(String efficacyId) {
		EfficacyReportMasterModel efficacyReportMaster = efficacyReportDao.getEfficacyReportsMaster(efficacyId);
		return CollectionUtils.isEmpty(efficacyReportMaster.getOutstandingDueReports()) ? new ArrayList() : efficacyReportMaster.getOutstandingDueReports().stream().collect(Collectors.toList());
	}

	@Override
	public List<ReinclusionObsoleteCounterReportModel> getObsoleteCountersReports(String efficacyId) {
		EfficacyReportMasterModel efficacyReportMaster = efficacyReportDao.getEfficacyReportsMaster(efficacyId);
		return CollectionUtils.isEmpty(efficacyReportMaster.getReinclusionObsoleteCounterReport()) ? new ArrayList<>() : efficacyReportMaster.getReinclusionObsoleteCounterReport().stream().collect(Collectors.toList());
	}

	@Override
	public List<EfficacyNewProductReportModel> getNewProductReports(String efficacyId) {
		EfficacyReportMasterModel efficacyReportMaster = efficacyReportDao.getEfficacyReportsMaster(efficacyId);
		return CollectionUtils.isEmpty(efficacyReportMaster.getEfficacyNewProductReport()) ? new ArrayList<>() : efficacyReportMaster.getEfficacyNewProductReport().stream().collect(Collectors.toList());
	}
}
