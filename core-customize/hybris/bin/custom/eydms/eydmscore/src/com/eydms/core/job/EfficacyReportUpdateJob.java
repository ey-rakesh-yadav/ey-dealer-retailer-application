package com.eydms.core.job;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.eydms.core.dao.EfficacyReportDao;
import com.eydms.core.dao.EyDmsUserDao;
import com.eydms.core.dao.TerritoryManagementDao;
import com.eydms.core.enums.CustomerCategory;
import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.dao.DJPVisitDao;
import com.eydms.core.model.*;
import com.eydms.core.services.TerritoryManagementService;

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

public class EfficacyReportUpdateJob extends AbstractJobPerformable<CronJobModel>{

	@Resource
	EyDmsUserDao eydmsUserDao;
	
	@Resource
	TerritoryManagementDao territoryManagementDao;
	
	@Resource
	BaseSiteService baseSiteService;
	
	@Resource
	EfficacyReportDao efficacyReportDao;
	
	@Resource
	ModelService modelService;
	
	@Resource
	DJPVisitDao djpVisitDao;
	
	@Resource
	FlexibleSearchService flexibleSearchService;
	
	@Resource
	TerritoryManagementService territoryManagementService;
	
	private int year,month;
	private Date date1, date2, endDate1, endDate2;
	private CustomerCategory category = CustomerCategory.TR;
	private UserGroupModel dealerGroup;

	private static final Logger LOG = Logger.getLogger(EfficacyReportUpdateJob.class);
	
	@Override
	public PerformResult perform(CronJobModel arg0) {

		UserGroupModel group = new UserGroupModel();
		group.setUid(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID);
		dealerGroup = flexibleSearchService.getModelByExample(group);
		
		Calendar cal = Calendar.getInstance();

//		cal.add(Calendar.MONTH, -1);
//		year=cal.get(Calendar.YEAR);
//		month=cal.get(Calendar.MONTH)+2;
//
//		cal.set(Calendar.HOUR, 0);
//		cal.set(Calendar.MINUTE, 0);
//		cal.set(Calendar.SECOND, 0);
//		cal.set(Calendar.DAY_OF_MONTH,0);
//		cal.set(Calendar.MILLISECOND, 0);
//
//		date1 = cal.getTime();
//
//
//		if(cal.get(Calendar.MONTH)==0)
//			cal.add(Calendar.YEAR, -1);
//		cal.add(Calendar.MONTH,-1);
//		date2 = cal.getTime();
//
//		cal.add(Calendar.YEAR, -1);
//		endDate2 = cal.getTime();
//
//		if(cal.get(Calendar.MONDAY)==11)
//		{
//			cal.set(Calendar.MONTH, 0);
//			cal.add(Calendar.YEAR, 1);
//		}
//		else
//		{
//			cal.add(Calendar.MONTH, 1);
//
//		}
//		endDate1 = cal.getTime();
//
//		cal=Calendar.getInstance();

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
	
	//New Territory Change
	private void executeJob(String baseSiteUid)
	{
		
		BaseSiteModel site = baseSiteService.getBaseSiteForUID(baseSiteUid);
		baseSiteService.setCurrentBaseSite(site, Boolean.FALSE);
		
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
					modelService.save(efficacyReport);
				}
				
				List<OutstandingDueReportModel> outstandingDueReportList = new ArrayList<>();
				
				List<SubAreaMasterModel> sArea = new ArrayList<>();
				sArea.add(subArea);
				
				List<EyDmsCustomerModel> eydmsCustomerList = territoryManagementService.getAllCustomerForSubArea(sArea).stream().filter(c-> c.getGroups().contains(dealerGroup)).collect(Collectors.toList());
				
				for(EyDmsCustomerModel eydmsCustomer : eydmsCustomerList)
				{
					if(null == eydmsCustomer.getCustomerNo() || eydmsCustomer.getCustomerNo().isEmpty()) {
						continue;
					}
					OutstandingDueReportModel outstandingDueReportModel = modelService.create(OutstandingDueReportModel.class);
					
					outstandingDueReportModel.setEyDmsCustomer(eydmsCustomer);
					outstandingDueReportModel.setCounterName(eydmsCustomer.getName());
					outstandingDueReportModel.setCounterCode(eydmsCustomer.getUid());
					outstandingDueReportModel.setCustomerNo(eydmsCustomer.getCustomerNo());
					
					List<List<Double>> listBefore = efficacyReportDao.getOutstandingAmountAndDailyAverageSalesWithinDate(eydmsCustomer.getCustomerNo(), date1);
					List<List<Double>> listAfter = efficacyReportDao.getOutstandingAmountAndDailyAverageSalesWithinDate(eydmsCustomer.getCustomerNo(), date2);
					
					double outstandingBefore = 0.0;
                	double outstandingAfter = 0.0;
                	
                	double ageingDaysBefore=0.0;
                    double ageingDaysAfter= 0.0;
                    
                    double dailyAverageSalesBefore=0.0;
                    double dailyAverageSalesAfter= 0.0;
                	
					if(!listBefore.isEmpty()&&!Objects.isNull(listBefore))
					{
						outstandingBefore = listBefore.get(0).get(0);
						dailyAverageSalesBefore = listBefore.get(0).get(1);
						if(dailyAverageSalesBefore!=0.0)
						{
							ageingDaysBefore=outstandingBefore/dailyAverageSalesBefore;
							outstandingDueReportModel.setDailyAverageSalesStartMonth(dailyAverageSalesBefore);
						}
							
					}
					
					if(!listAfter.isEmpty()&&!Objects.isNull(listAfter))
					{
						outstandingAfter = listAfter.get(0).get(0);
						dailyAverageSalesAfter = listAfter.get(0).get(1);
						if(dailyAverageSalesAfter!=0.0)
						{
							ageingDaysAfter=outstandingAfter/dailyAverageSalesAfter;
							outstandingDueReportModel.setDailyAverageSalesEndMonth(dailyAverageSalesAfter);
						}
							
					}
					
					outstandingDueReportModel.setAgeingDaysStartMonth(Math.ceil(ageingDaysBefore));
					outstandingDueReportModel.setOutstandingStartMonth(outstandingBefore);
					outstandingDueReportModel.setAgeingDaysEndMonth(Math.ceil(ageingDaysAfter));
					outstandingDueReportModel.setOutstandingEndMonth(outstandingAfter);
                	
                	outstandingDueReportList.add(outstandingDueReportModel);	
               
				}
				
				double totalOutstandingBefore=outstandingDueReportList.stream().filter(c-> c.getOutstandingStartMonth()!=null).mapToDouble(c-> c.getOutstandingStartMonth()).sum();
				double totalOutstandingAfter=outstandingDueReportList.stream().filter(c-> c.getOutstandingEndMonth()!=null).mapToDouble(c-> c.getOutstandingEndMonth()).sum();

				double totalDailyAverageSalesBefore = outstandingDueReportList.stream().filter(c->c.getDailyAverageSalesStartMonth()!=null).mapToDouble(c-> c.getDailyAverageSalesStartMonth()).sum();
	            double totalDailyAverageSalesAfter = outstandingDueReportList.stream().filter(c->c.getDailyAverageSalesEndMonth()!=null).mapToDouble(c-> c.getDailyAverageSalesEndMonth()).sum();
				
				if(totalDailyAverageSalesBefore!=0.0)
                {	
                    efficacyReport.setAgeingDaysStartMonth(Math.ceil(totalOutstandingBefore/totalDailyAverageSalesBefore));
                }
                else
                {
                	efficacyReport.setAgeingDaysStartMonth(0.0);
                }
                
               if(totalDailyAverageSalesAfter!=0.0)
               	{
            	   efficacyReport.setAgeingDaysEndMonth(Math.ceil(totalOutstandingAfter/totalDailyAverageSalesAfter));
                }
               else
                {
            	   efficacyReport.setAgeingDaysEndMonth(0.0);
                }
				
				efficacyReport.setOutstandingDueReports(outstandingDueReportList);
				efficacyReport.setTotalOutstandingStartMonth(totalOutstandingBefore);
				efficacyReport.setTotalOutstandingEndMonth(totalOutstandingAfter);

//				efficacyReport.setOutstandingCleared(null);

				modelService.saveAll(outstandingDueReportList);
				
				efficacyReportList.add(efficacyReport);
			}
			modelService.saveAll(efficacyReportList);
		}
	}

}