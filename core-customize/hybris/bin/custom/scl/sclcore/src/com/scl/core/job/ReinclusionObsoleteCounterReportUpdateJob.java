package com.scl.core.job;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.customer.dao.SclCustomerDao;
import com.scl.core.dao.*;
import com.scl.core.model.CounterVisitMasterModel;
import com.scl.core.model.EfficacyReportMasterModel;
import com.scl.core.model.ReinclusionObsoleteCounterReportModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.SclUserModel;
import com.scl.core.model.SubAreaMasterModel;
import com.scl.core.model.VisitMasterModel;

import com.scl.core.services.SclSalesSummaryService;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class ReinclusionObsoleteCounterReportUpdateJob extends AbstractJobPerformable<CronJobModel>{

	@Resource
	EfficacyReportDao efficacyReportDao;
	@Resource
	SclUserDao sclUserDao;
	@Resource
	TerritoryManagementDao territoryManagementDao;
	@Resource
	SclCustomerDao sclCustomerDao;
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
	@Autowired
	SclSalesSummaryDao sclSalesSummaryDao;
	
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
		group.setUid(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID);
		dealerGroup = flexibleSearchService.getModelByExample(group);
		
		group.setUid(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);
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
		
		executeJob(SclCoreConstants.SITE.SCL_SITE);
		executeJob(SclCoreConstants.SITE.BANGUR_SITE);
		executeJob(SclCoreConstants.SITE.ROCKSTRONG_SITE);
		
		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}
	
	private void executeJob(String baseSiteUid)
	{
		BaseSiteModel site = baseSiteService.getBaseSiteForUID(baseSiteUid);
		baseSiteService.setCurrentBaseSite(site, Boolean.FALSE);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		List<B2BUnitModel> allowedUnit = site.getAllowedUnit();
		
		List<SclUserModel> soList = sclUserDao.getAllActiveSO().stream().filter(s-> allowedUnit.contains(s.getDefaultB2BUnit())).collect(Collectors.toList());
		
		
		for(SclUserModel so: soList)
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
					efficacyReport.setSclUser(so);
					efficacyReport.setSubAreaMaster(subArea);
				}
				

				List<VisitMasterModel> visitMasterModelList = efficacyReportDao.getAllVisitMasterForSubAreaAndSO(date1, date2, subArea, so);
				if(Objects.isNull(visitMasterModelList)) {
					continue;
				}

				List<SclCustomerModel> obsoleteCounters = efficacyReportDao.getObsoleteCountersList(so, date1, date2);
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
						List<SclCustomerModel> customerList = efficacyReportDao.getRevivedCountersList(obsoleteCounters);
						if(CollectionUtils.isNotEmpty(customerList)){
							LocalDate startDate = date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
							LocalDate endDate = date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
							for (SclCustomerModel sclCustomerList : customerList) {
								efficacyReport.setSalesVolumeRevived(sclSalesSummaryDao.getSalesDetails(sclCustomerList,startDate.getMonthValue(), startDate.getYear(), endDate.getMonthValue(), endDate.getYear(), Collections.EMPTY_LIST));
							}
						}
					//	efficacyReport.setSalesVolumeRevived(efficacyReportDao.getSalesForCustomerList(efficacyReportDao.getRevivedCountersList(obsoleteCounters), date1, date2));
					}
				}
				modelService.save(efficacyReport);

				for(VisitMasterModel visitMaster : visitMasterModelList)
				{
					List<CounterVisitMasterModel> counterVisitList = (List<CounterVisitMasterModel>) visitMaster.getCounterVisits().stream().filter(c-> (c.getEndVisitTime()!=null) && ( c.getSclCustomer().getGroups().contains(dealerGroup) || c.getSclCustomer().getGroups().contains(retailerGroup) ) ).collect(Collectors.toList());
					
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
						if(counterVisit.getSclCustomer()!=null) {
							model.setCounterCode(counterVisit.getSclCustomer().getUid());
							model.setCounterName(counterVisit.getSclCustomer().getName());
							model.setCustomerNo(counterVisit.getSclCustomer().getCustomerNo());
						}
						model.setVisitDate(visitMaster.getVisitDate());
						model.setOrderCaptured(counterVisit.getOrderGenerated());
						
						if(counterVisit.getSclCustomer().getGroups().contains(dealerGroup))
						{

							List<B2BCustomerModel> sclCustomerModels=new ArrayList<>();
							sclCustomerModels.add(counterVisit.getSclCustomer());
							orderBooked = sclSalesSummaryDao.getSalesDetails(sclCustomerModels, month, year);


							//orderBooked = sclSalesSummaryDao.getSalesDetails(counterVisit.getSclCustomer(), month, year,Collections.EMPTY_LIST);

									//orderBooked =  salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYearForDealer(counterVisit.getSclCustomer(), site, month,  year, null);
							model.setOrderBooked(orderBooked);
						}
							
						else if(counterVisit.getSclCustomer().getGroups().contains(retailerGroup))
						{
							String startDate = dateFormat.format(date1);
							String endDate = dateFormat.format(date2);
							List<SclCustomerModel> retailerList = new ArrayList<>();
							retailerList.add(counterVisit.getSclCustomer());
							if(!(orderRequisitionDao.getSalsdMTDforRetailer(retailerList,startDate, endDate,null,null).isEmpty())) {
								orderBooked = (double) orderRequisitionDao.getSalsdMTDforRetailer(retailerList,startDate, endDate,null,null).get(0).get(1);
//							orderBooked=efficacyReportDao.getMonthlySalesForRetailer(counterVisit.getSclCustomer(), date1, date2);
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
