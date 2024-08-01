package com.scl.core.job;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import javax.annotation.Resource;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.customer.dao.SclCustomerDao;
import com.scl.core.dao.EfficacyReportDao;
import com.scl.core.dao.SclUserDao;
import com.scl.core.dao.TerritoryManagementDao;
import com.scl.core.model.CounterVisitMasterModel;
import com.scl.core.model.EfficacyReportMasterModel;
import com.scl.core.model.MarketIntelligenceReportModel;
import com.scl.core.model.MarketMappingDetailsModel;
import com.scl.core.model.SclUserModel;
import com.scl.core.model.SubAreaMasterModel;
import com.scl.core.model.VisitMasterModel;

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

public class MarketIntelligenceReportUpdateJob extends AbstractJobPerformable<CronJobModel> {

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
	
	private Date date1, date2;
	private int month, year;
	private Calendar cal;
	
	private UserGroupModel dealerGroup;
	private UserGroupModel retailerGroup;

	private static final Logger LOG = Logger.getLogger(MarketIntelligenceReportUpdateJob.class);
	
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
			LOG.info("Current SO is "+so.getName()+":"+so.getUid());
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
                    modelService.save(efficacyReport);
				}
				
//				List<SubAreaMasterModel> sArea = new ArrayList<>();
//				sArea.add(subArea);
				
				List<VisitMasterModel> visitMasterModelList = efficacyReportDao.getAllVisitMasterForSubAreaAndSO(date1, date2, subArea, so);
				if(Objects.isNull(visitMasterModelList)) {
					continue;
				}
				for(VisitMasterModel visitMaster : visitMasterModelList)
				{
					List<CounterVisitMasterModel> counterVisitList = (List<CounterVisitMasterModel>) visitMaster.getCounterVisits().stream().filter(c-> (c.getEndVisitTime()!=null) && ( c.getSclCustomer().getGroups().contains(dealerGroup) || c.getSclCustomer().getGroups().contains(retailerGroup) ) ).collect(Collectors.toList());
					
					//Map<Object, Map<Object, List<MarketMappingDetailsModel>>> modelList = counterVisitList.stream().flatMap(c-> c.getMarketMapping().stream()).collect(Collectors.groupingBy(m-> m.getBrand().getName(),Collectors.groupingBy(m-> m.getProduct().getName())));							
					
					 Map<String, List<MarketMappingDetailsModel>> map = counterVisitList.stream().flatMap(c-> c.getMarketMapping().stream()).collect(Collectors.groupingBy(p -> getGroupingByKey(p), Collectors.mapping((MarketMappingDetailsModel p) -> p, toList())));
					 
					 Set<String> keys = map.keySet();
					 
					 List<MarketIntelligenceReportModel> reportList = new ArrayList<>();
					 
					 for(String key : keys)
					 {
						 List<MarketMappingDetailsModel> modelList = map.get(key);
						 
						 Map<Double, Long> rspMap = modelList.stream().collect(groupingBy(m-> m.getRetailsalePrice(), Collectors.counting()));
						 Map<Double, Long> wspMap = modelList.stream().collect(groupingBy(m-> m.getWholesalePrice(), Collectors.counting()));
						 Map<Double, Long> discountMap = modelList.stream().collect(groupingBy(m-> m.getDiscount(), Collectors.counting()));
						 Map<Double, Long> billingMap = modelList.stream().collect(groupingBy(m-> m.getBilling(), Collectors.counting()));
						
						 
						 MarketIntelligenceReportModel report = modelService.create(MarketIntelligenceReportModel.class);
						 
						 report.setVisitDate(visitMaster.getVisitPlannedDate());
						 report.setProductCode(map.get(key).get(0).getProduct().getCode());
						 report.setProductName(map.get(key).get(0).getProduct().getName());
						 report.setBrandCode(map.get(key).get(0).getBrand().getIsocode());
						 report.setBrandName(map.get(key).get(0).getBrand().getName());
						 report.setRsp(trimData(rspMap).getFinalValue());
						 report.setWsp(trimData(wspMap).getFinalValue());
						 report.setDiscount(trimData(discountMap).getFinalValue());
						 report.setBillingPrice(trimData(billingMap).getFinalValue());
						 report.setEfficacyReportMaster(efficacyReport);
						 
						 reportList.add(report);
					 }
					 
					modelService.saveAll(reportList);
				}
			}
		}
	}
	
	 private static String getGroupingByKey(MarketMappingDetailsModel m) {
		if(m.getProduct()!=null && m.getBrand()!=null)
	        return m.getProduct().getName() + "" + m.getBrand().getName();
		return "";
	    }
	 
	 private static ProductOccuranceData trimData(Map<Double, Long> occMap) {

	        Map<Long, ProductOccuranceData> vcMap = new HashMap<>();
	        Set<Double> keys = occMap.keySet();
	        for ( Double value: keys) {
	            Long occ = occMap.get(value);
	            ProductOccuranceData data = vcMap.get(occ);
	            if(data == null){
	                data = new ProductOccuranceData();
	                vcMap.put( occ, data);
	            }
	            data.addValue(value);
	            data.increment();
	        }
	        List<Long> collect = vcMap.keySet().stream().sorted(Comparator.reverseOrder()).collect(toList());
	        return vcMap.get( collect.get(0));
	    }
}

class ProductOccuranceData{
   
	double value;
    int count;

    public void addValue( double value) {
        this.value += value;
    }

    public void increment() {
        count++;
    }

    public double getFinalValue(){
        return  value/count;
    }
    
    @Override
    public String toString() {
        return "VisitOccuranceData{" +
                "value=" + value +
                ", count=" + count +
                '}';
    }
	
}

