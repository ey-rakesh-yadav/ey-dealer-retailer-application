package com.scl.core.job;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.customer.dao.SclCustomerDao;
import com.scl.core.dao.*;
import com.scl.core.enums.CounterType;
import com.scl.core.model.*;
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

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class SalesVolumeGrowthReportJob extends AbstractJobPerformable<CronJobModel> {

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
    SclSalesSummaryDao sclSalesSummaryDao;

    private Date date1, date2;
    private int month, year;
    private Calendar cal;

    private UserGroupModel dealerGroup;
    private UserGroupModel retailerGroup;

    private static final Logger LOG = Logger.getLogger(SalesVolumeGrowthReportJob.class);

    @Override
    public PerformResult perform(CronJobModel arg0) {
        UserGroupModel group = new UserGroupModel();
        group.setUid(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID);
        dealerGroup = flexibleSearchService.getModelByExample(group);

        group.setUid(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);
        retailerGroup = flexibleSearchService.getModelByExample(group);

        cal= Calendar.getInstance();

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
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        BaseSiteModel site = baseSiteService.getBaseSiteForUID(baseSiteUid);
        baseSiteService.setCurrentBaseSite(site, Boolean.FALSE);

        List<B2BUnitModel> allowedUnit = site.getAllowedUnit();

        List<SclUserModel> soList = sclUserDao.getAllActiveSO().stream().filter(s-> allowedUnit.contains(s.getDefaultB2BUnit())).collect(Collectors.toList());


        for(SclUserModel so: soList)
        {
            List<String> uniqueCountersVisited = new ArrayList<>();
            int totalNoOfCountersVisited = 0;
            Double cumulativeSalesBeforeVisit, cumulativeSalesAfterVisit, cumulativeSalesGrowth;
            cumulativeSalesBeforeVisit = cumulativeSalesAfterVisit = cumulativeSalesGrowth = 0.0;

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
                    if (Objects.isNull(counterVisitList) || counterVisitList.isEmpty()) {
                        continue;
                    }
                    for(CounterVisitMasterModel counterVisitMasterModel : counterVisitList) {
                        int visitDate;
                        Double salesBeforeVisit, salesAfterVisit, saleGrowth;
                        SclCustomerModel sclCustomerModel = counterVisitMasterModel.getSclCustomer();
                        LOG.info("CounterVisit Id is " + counterVisitMasterModel.getId() + "\n" + "Customer is " + sclCustomerModel.getName());

                        Calendar cal2 = Calendar.getInstance();
                        cal2.setTime(counterVisitMasterModel.getEndVisitTime());
                        visitDate = cal2.get(Calendar.DATE);

                        int lastDateOfMonth = cal2.getActualMaximum(Calendar.DATE);

                        cal2.set(Calendar.HOUR, 0);
                        cal2.set(Calendar.MINUTE, 0);
                        cal2.set(Calendar.SECOND, 0);
                        cal2.set(Calendar.MILLISECOND, 0);

                        Date beforeVisitEndDate = cal2.getTime();

                        cal2.add(Calendar.DATE,-3);

                        Date beforeVisitStartDate = cal2.getTime();

                        Calendar cal3 = Calendar.getInstance();
                        cal3.setTime(beforeVisitEndDate);
                        cal3.add(Calendar.DATE,3);

                        Date afterVisitEndDate = cal3.getTime();

                        LOG.info("Date of visit is "+beforeVisitEndDate.toString() + "/n" + "BeforeVisitStartDate is " + beforeVisitStartDate.toString() + "/n" + "After Visit End Date is " + afterVisitEndDate.toString());

                        IncreaseSalesVolumeReportModel salesVolumeReportModel = modelService.create(IncreaseSalesVolumeReportModel.class);
                        salesVolumeReportModel.setSclCustomer(sclCustomerModel);
                        salesVolumeReportModel.setCounterName(sclCustomerModel.getName());
                        salesVolumeReportModel.setCounterCode(sclCustomerModel.getUid());
                        salesVolumeReportModel.setCounterType(sclCustomerModel.getCounterType());
                        salesVolumeReportModel.setVisitDate(counterVisitMasterModel.getEndVisitTime());

                        if(!uniqueCountersVisited.contains(sclCustomerModel.getUid())) {
                            uniqueCountersVisited.add(sclCustomerModel.getUid());
                        }

                        if(sclCustomerModel.getCounterType().equals(CounterType.DEALER)) {
                            if(visitDate-3<=0) {
                                salesBeforeVisit = 0.0;
                                salesVolumeReportModel.setBeforeSales(salesBeforeVisit);
                            } else {
                                LocalDate startDate = beforeVisitStartDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                                LocalDate endDate = beforeVisitStartDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                                salesBeforeVisit = sclSalesSummaryDao.getSalesDetails(sclCustomerModel, startDate.getMonthValue(), startDate.getYear(), endDate.getMonthValue(), endDate.getYear(),Collections.EMPTY_LIST);
                                       //  salesBeforeVisit = efficacyReportDao.getActualSalesForDealer(sclCustomerModel,site,beforeVisitStartDate, beforeVisitEndDate);
                                salesVolumeReportModel.setBeforeSales(salesBeforeVisit);
                            }

                            if(lastDateOfMonth-visitDate<=3) {
                                salesAfterVisit = 0.0;
                                salesVolumeReportModel.setAfterSales(salesAfterVisit);
                            }
                            else {
                                LocalDate startDate = beforeVisitEndDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                                LocalDate endDate = afterVisitEndDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                                salesAfterVisit = sclSalesSummaryDao.getSalesDetails(sclCustomerModel, startDate.getMonthValue(), startDate.getYear(), endDate.getMonthValue(), endDate.getYear(),Collections.EMPTY_LIST);

                                              //  salesAfterVisit = efficacyReportDao.getActualSalesForDealer(sclCustomerModel,site,beforeVisitEndDate,afterVisitEndDate);
                                salesVolumeReportModel.setAfterSales(salesAfterVisit);
                            }
                            saleGrowth = salesAfterVisit - salesBeforeVisit;
                            salesVolumeReportModel.setGrowth(saleGrowth);

                            cumulativeSalesBeforeVisit += salesBeforeVisit;
                            cumulativeSalesAfterVisit += salesAfterVisit;
                            cumulativeSalesGrowth += saleGrowth;

                        }
                        else if(counterVisitMasterModel.getSclCustomer().getCounterType().equals(CounterType.RETAILER)) {
                            if(visitDate-3<=0) {
                                salesBeforeVisit = 0.0;
                                salesVolumeReportModel.setBeforeSales(salesBeforeVisit);
                            } else {
                                String startDate = dateFormat.format(beforeVisitStartDate);
                                String endDate = dateFormat.format(beforeVisitEndDate);
                                List<SclCustomerModel> retailerList = new ArrayList<>();
                                retailerList.add(sclCustomerModel);
                                if(!(orderRequisitionDao.getSalsdMTDforRetailer(retailerList,startDate, endDate,null,null).isEmpty())) {
                                    salesBeforeVisit = (double) orderRequisitionDao.getSalsdMTDforRetailer(retailerList,startDate, endDate,null,null).get(0).get(1);

                                }
                                else {
                                    salesBeforeVisit = 0.0;

                                }
                                salesVolumeReportModel.setBeforeSales(salesBeforeVisit);
                            }

                            if(lastDateOfMonth-visitDate<3) {
                                salesAfterVisit = 0.0;
                                salesVolumeReportModel.setAfterSales(salesAfterVisit);
                            }
                            else {
                                String startDate = dateFormat.format(beforeVisitEndDate);
                                String endDate = dateFormat.format(beforeVisitStartDate);
                                List<SclCustomerModel> retailerList = new ArrayList<>();
                                retailerList.add(sclCustomerModel);
                                if(!(orderRequisitionDao.getSalsdMTDforRetailer(retailerList,startDate, endDate,null,null).isEmpty())) {
                                    salesAfterVisit = (double) orderRequisitionDao.getSalsdMTDforRetailer(retailerList,startDate, endDate,null,null).get(0).get(1);
                                }
                                else {
                                    salesAfterVisit = 0.0;

                                }
                                salesVolumeReportModel.setAfterSales(salesAfterVisit);
                            }
                            saleGrowth = salesAfterVisit - salesBeforeVisit;
                            salesVolumeReportModel.setGrowth(saleGrowth);

                            cumulativeSalesBeforeVisit += salesBeforeVisit;
                            cumulativeSalesAfterVisit += salesAfterVisit;
                            cumulativeSalesGrowth += saleGrowth;

                        }
                        salesVolumeReportModel.setEfficacyReportMaster(efficacyReport);
                        salesVolumeReportModel.setScluser(so);
                        salesVolumeReportModel.setSubarea(subArea.getTaluka());
                        modelService.save(salesVolumeReportModel);
                        totalNoOfCountersVisited += 1;
                    }
                }

                efficacyReport.setTotalCounterVisited(uniqueCountersVisited.size());
                efficacyReport.setTotalNumberOfVisits(String.valueOf(totalNoOfCountersVisited));
                efficacyReport.setSalesBeforeVisit(cumulativeSalesBeforeVisit);
                efficacyReport.setSalesAfterVisit(cumulativeSalesAfterVisit);
                efficacyReport.setGrowth(cumulativeSalesGrowth);
                modelService.save(efficacyReport);
            }
        }
    }
}
