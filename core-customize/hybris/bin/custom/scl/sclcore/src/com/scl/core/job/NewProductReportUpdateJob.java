package com.scl.core.job;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.EfficacyReportDao;
import com.scl.core.dao.SclUserDao;
import com.scl.core.dao.TerritoryManagementDao;
import com.scl.core.jalo.CounterVisitMaster;
import com.scl.core.model.*;
import com.scl.core.services.TerritoryManagementService;

import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.product.ProductModel;
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

public class NewProductReportUpdateJob extends AbstractJobPerformable<CronJobModel>{
	
	@Resource
	EfficacyReportDao efficacyReportDao;
	
	@Resource
	SclUserDao sclUserDao;
	
	@Resource
	TerritoryManagementDao territoryManagementDao;
	
	@Resource
	TerritoryManagementService territoryManagementService;
	
	@Resource
	FlexibleSearchService flexibleSearchService;

	@Resource
	ModelService modelService;
	
	@Resource
	BaseSiteService baseSiteService;
	
	private Date date1, date2;
	private int month, year;
	private Calendar cal;
	
	private UserGroupModel dealerGroup;
	private UserGroupModel retailerGroup;

	private static final Logger LOG = Logger.getLogger(NewProductReportUpdateJob.class);
	
	@Override
	public PerformResult perform(CronJobModel arg0) {
		
		UserGroupModel group = new UserGroupModel();
		group.setUid(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID);
		dealerGroup = flexibleSearchService.getModelByExample(group);
		
		group.setUid(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);
		retailerGroup = flexibleSearchService.getModelByExample(group);
		
		cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.DAY_OF_MONTH,1);
		cal.set(Calendar.MILLISECOND, 0);
		
		date1=cal.getTime();
		
		cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE));
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		date2=cal.getTime();

		LOG.info("Date1 is "+date1.toString()+" and Date2 is "+date2.toString());
		
		month = cal.get(Calendar.MONTH)+1;
		year = cal.get(Calendar.YEAR);
		
		executeJob(SclCoreConstants.SITE.SCL_SITE);
		executeJob(SclCoreConstants.SITE.BANGUR_SITE);
		executeJob(SclCoreConstants.SITE.ROCKSTRONG_SITE);
		
		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}
	//New Territory Change
	private void executeJob(String baseSiteUid)
	{
		BaseSiteModel site = baseSiteService.getBaseSiteForUID(baseSiteUid);
		baseSiteService.setCurrentBaseSite(site, Boolean.FALSE);
		
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
					//efficacyReport.setSubArea(subArea);
					efficacyReport.setSubAreaMaster(subArea);
//					modelService.save(efficacyReport);
				}
				
				List<SubAreaMasterModel> sArea = new ArrayList<>();
				sArea.add(subArea);
				
				List<VisitMasterModel> visitMasterModelList = efficacyReportDao.getAllVisitMasterForSubAreaAndSO(date1, date2, subArea, so);
				if(Objects.isNull(visitMasterModelList)) {
					continue;
				}
				for(VisitMasterModel visitMaster : visitMasterModelList)
				{
					List<CounterVisitMasterModel> counterVisitList = (List<CounterVisitMasterModel>) visitMaster.getCounterVisits().stream().filter(c-> (c.getEndVisitTime()!=null) && ( c.getSclCustomer().getGroups().contains(dealerGroup) || c.getSclCustomer().getGroups().contains(retailerGroup) ) ).collect(Collectors.toList());
					if (Objects.isNull(counterVisitList) || counterVisitList.isEmpty()) {
						continue;
					}
					for(CounterVisitMasterModel counterVisit : counterVisitList)
					{
						Calendar cal2 = Calendar.getInstance();
						cal2.setTime(counterVisit.getEndVisitTime());
					
						cal2.set(Calendar.HOUR, 0);
						cal2.set(Calendar.MINUTE, 0);
						cal2.set(Calendar.SECOND, 0);
						cal2.set(Calendar.MILLISECOND, 0);
						
						Date counterVisitStartDate = cal2.getTime();
						
						cal2.set(Calendar.HOUR, 23);
						cal2.set(Calendar.MINUTE, 59);
						cal2.set(Calendar.SECOND, 59);
						cal2.set(Calendar.MILLISECOND, cal.getMaximum(Calendar.MILLISECOND));

						Date counterVisitEndDate = cal2.getTime();
						
						List<ProductModel> productList = efficacyReportDao.getAllNewProducts(date1, date2);
						if(productList.isEmpty()) {
							continue;
						}
						List<List<Object>> sales = efficacyReportDao.getSalesForNewProducts(productList, counterVisit.getSclCustomer(), date1, date2);
						
						List<EfficacyNewProductReportModel> productReportList = new ArrayList<>();
						
						for(List<Object> productSales : sales)
						{	
							EfficacyNewProductReportModel productReport = modelService.create(EfficacyNewProductReportModel.class);
							productReport.setNewProducOrSKU((String) productSales.get(1));
							productReport.setOrderBooked((double) productSales.get(2));
							productReport.setCounterCode(counterVisit.getSclCustomer().getUid());
							productReport.setCounterName(counterVisit.getSclCustomer().getName());
							productReport.setCustomerNo(counterVisit.getSclCustomer().getCustomerNo());
							productReport.setDateOfVisit(visitMaster.getEndVisitTime());
							productReport.setEfficacyReportMaster(efficacyReport);
							modelService.save(productReport);
//							productReportList.add(productReport);
						}
//						modelService.saveAll(productReportList);
					}
				}
				
				List<ProductModel> monthProductList = efficacyReportDao.getAllNewProducts(date1, date2);
				if(monthProductList.isEmpty()) {
					continue;
				}
				List<SclCustomerModel> sclCustomerList = territoryManagementService.getAllCustomerForSubArea(sArea);
				double totalOrderBooked = 0.0;
				if(!sclCustomerList.isEmpty() && !Objects.isNull(sclCustomerList)) {
				for(ProductModel product : monthProductList)
				{
//					Calendar cal3 = Calendar.getInstance();
//					cal3.setTime(product.getLaunchDate());
//					cal3.set(Calendar.HOUR, 23);
//					cal3.set(Calendar.MINUTE, 59);
//					cal3.set(Calendar.SECOND, 59);
//					cal3.set(Calendar.MILLISECOND, cal.getMaximum(Calendar.MILLISECOND));
//					Date newProdEndDate = cal3.getTime();

//					LOG.info("newProdEndDate is " + newProdEndDate);
					
					totalOrderBooked += efficacyReportDao.getMonthlySalesForNewProduct(product, sclCustomerList, date1, date2);
				}
				}
				efficacyReport.setOrdersBookedForNewProductOrSKU(totalOrderBooked);
				modelService.save(efficacyReport);
			}
		}
	}
	
}