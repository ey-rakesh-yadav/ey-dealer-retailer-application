package com.eydms.core.job;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.customer.dao.EyDmsCustomerDao;
import com.eydms.core.dao.*;
import com.eydms.core.model.CounterVisitMasterModel;
import com.eydms.core.model.EfficacyReportMasterModel;
import com.eydms.core.model.ReinclusionObsoleteCounterReportModel;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.core.model.SubAreaMasterModel;
import com.eydms.core.model.VisitMasterModel;

import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class ReinclusionObsoleteCounterReportUpdateJob extends AbstractJobPerformable<CronJobModel>{

	@Resource
	EfficacyReportDao efficacyReportDao;
	
	@Resource
	EyDmsUserDao eydmsUserDao;
	
	@Resource
	TerritoryManagementDao territoryManagementDao;
	
	@Resource
	EyDmsCustomerDao eydmsCustomerDao;
	
	@Resource 
	FlexibleSearchService flexibleSearchService;
	
	@Resource
	ModelService modelService;
	
	@Resource
	BaseSiteService baseSiteService;

	@Autowired
	OrderRequisitionDao orderRequisitionDao;

	@Autowired
	SalesPerformanceDao salesPerformanceDao;
	
	private Date date1, date2;
	private int month, year;
	private Calendar cal;
	
	private UserGroupModel dealerGroup;
	private UserGroupModel retailerGroup;

	private static final Logger LOG = Logger.getLogger(ReinclusionObsoleteCounterReportModel.class);
	
	//New Territory Change
	@Override
	public PerformResult perform(CronJobModel arg0) {
		
		UserGroupModel group = new UserGroupModel();
		group.setUid(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID);
		dealerGroup = flexibleSearchService.getModelByExample(group);
		
		group.setUid(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);
		retailerGroup = flexibleSearchService.getModelByExample(group);
		
		cal=Calendar.getInstance();
		
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.DAY_OF_MONTH,1);
		date2=cal.getTime();
		
		cal.add(Calendar.MONTH, -1);
		date1=cal.getTime();
		
		year=cal.get(Calendar.YEAR);
		month=cal.get(Calendar.MONTH)+1;

		LOG.info("Date1 is "+date1.toString()+" and Date2 is "+date2.toString());
		
		executeJob(EyDmsCoreConstants.SITE.SHREE_SITE);
		executeJob(EyDmsCoreConstants.SITE.BANGUR_SITE);
		executeJob(EyDmsCoreConstants.SITE.ROCKSTRONG_SITE);
		
		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}
	
	private void executeJob(String baseSiteUid)
	{
		BaseSiteModel site = baseSiteService.getBaseSiteForUID(baseSiteUid);
		baseSiteService.setCurrentBaseSite(site, Boolean.FALSE);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		List<B2BUnitModel> allowedUnit = site.getAllowedUnit();
		
		List<EyDmsUserModel> soList = eydmsUserDao.getAllActiveSO().stream().filter(s-> allowedUnit.contains(s.getDefaultB2BUnit())).collect(Collectors.toList());
		
		
		for(EyDmsUserModel so: soList)
		{
			List<SubAreaMasterModel> subAreas = territoryManagementDao.getTerritoriesForSO(so);

			List<EfficacyReportMasterModel> efficacyReportList = new ArrayList<>();

			for(SubAreaMasterModel subArea : subAreas)
			{
				if(Objects.isNull(subArea)) {
					continue;
				}
				EfficacyReportMasterModel efficacyReport = efficacyReportDao.getEfficacyReportForMonth(month, year, subArea, so);
				if(Objects.isNull(efficacyReport))
				{
					efficacyReport = modelService.create(EfficacyReportMasterModel.class);
					efficacyReport.setMonth(month);
					efficacyReport.setYear(year);
					efficacyReport.setEyDmsUser(so);
					efficacyReport.setSubAreaMaster(subArea);
//					modelService.save(efficacyReport);
				}
				
//				List<SubAreaMasterModel> sArea = new ArrayList<>();
//				sArea.add(subArea);
				
				List<VisitMasterModel> visitMasterModelList = efficacyReportDao.getAllVisitMasterForSubAreaAndSO(date1, date2, subArea, so);
				if(Objects.isNull(visitMasterModelList)) {
					continue;
				}

				List<EyDmsCustomerModel> obsoleteCounters = efficacyReportDao.getObsoleteCountersList(so, date1, date2);
				efficacyReport.setObseleteCountersVisited(obsoleteCounters.size());
				if(obsoleteCounters.isEmpty()) {
					efficacyReport.setObseleteCountersRevived(0);
					efficacyReport.setSalesVolumeRevived(0.0);
				}
				else {
					efficacyReport.setObseleteCountersRevived(efficacyReportDao.getRevivedCountersList(obsoleteCounters).size());
					if(efficacyReportDao.getRevivedCountersList(obsoleteCounters).isEmpty()) {
						efficacyReport.setSalesVolumeRevived(0.0);
					}
					else {
						efficacyReport.setSalesVolumeRevived(efficacyReportDao.getSalesForCustomerList(efficacyReportDao.getRevivedCountersList(obsoleteCounters), date1, date2));
					}
				}
				modelService.save(efficacyReport);

				for(VisitMasterModel visitMaster : visitMasterModelList)
				{
					List<CounterVisitMasterModel> counterVisitList = (List<CounterVisitMasterModel>) visitMaster.getCounterVisits().stream().filter(c-> (c.getEndVisitTime()!=null) && ( c.getEyDmsCustomer().getGroups().contains(dealerGroup) || c.getEyDmsCustomer().getGroups().contains(retailerGroup) ) ).collect(Collectors.toList());
					
					List<ReinclusionObsoleteCounterReportModel> modelList = new ArrayList<>();
					
					for(CounterVisitMasterModel counterVisit : counterVisitList)
					{
						ReinclusionObsoleteCounterReportModel model = modelService.create(ReinclusionObsoleteCounterReportModel.class);

						if(counterVisit.getLastLiftingDate()!=null && counterVisit.getEndVisitTime()!=null) {
							long dateBeforeInMs = counterVisit.getLastLiftingDate().getTime();
							long dateAfterInMs = counterVisit.getEndVisitTime().getTime();
							long timeDiff = Math.abs(dateAfterInMs - dateBeforeInMs);
							int daysDiff = (int) (timeDiff / (1000 * 60 * 60 * 24));
							model.setDaysSinceLastLifting(daysDiff);
						}

						double orderBooked;
						if(counterVisit.getEyDmsCustomer()!=null) {
							model.setCounterCode(counterVisit.getEyDmsCustomer().getUid());
							model.setCounterName(counterVisit.getEyDmsCustomer().getName());
							model.setCustomerNo(counterVisit.getEyDmsCustomer().getCustomerNo());
						}
						model.setVisitDate(visitMaster.getVisitDate());
						model.setOrderCaptured(counterVisit.getOrderGenerated());
						
						if(counterVisit.getEyDmsCustomer().getGroups().contains(dealerGroup))
						{
							orderBooked =  salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYearForDealer(counterVisit.getEyDmsCustomer(), site, month,  year, null);
//							orderBooked=efficacyReportDao.getMonthlySalesForDealer(counterVisit.getEyDmsCustomer(), date1, date2);
							model.setOrderBooked(orderBooked);
						}
							
						else if(counterVisit.getEyDmsCustomer().getGroups().contains(retailerGroup))
						{
							String startDate = dateFormat.format(date1);
							String endDate = dateFormat.format(date2);
							List<EyDmsCustomerModel> retailerList = new ArrayList<>();
							retailerList.add(counterVisit.getEyDmsCustomer());
							if(!(orderRequisitionDao.getSalsdMTDforRetailer(retailerList,startDate, endDate,null,null).isEmpty())) {
								orderBooked = (double) orderRequisitionDao.getSalsdMTDforRetailer(retailerList,startDate, endDate,null,null).get(0).get(1);
//							orderBooked=efficacyReportDao.getMonthlySalesForRetailer(counterVisit.getEyDmsCustomer(), date1, date2);
								model.setOrderBooked(orderBooked);
							}
							else {
								model.setOrderBooked(0.0);

							}
						}

						model.setEfficacyReportMaster(efficacyReport);
						modelList.add(model);
					}
					modelService.saveAll(modelList);
				}
			}
		}
	}

}
