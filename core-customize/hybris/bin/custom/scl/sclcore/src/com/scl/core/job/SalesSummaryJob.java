package com.scl.core.job;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.SalesSummaryDao;
import com.scl.core.dao.SclUserDao;
import com.scl.core.model.*;
import com.scl.core.services.TerritoryManagementService;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.log4j.Logger;


import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SalesSummaryJob extends AbstractJobPerformable<CronJobModel> {
    @Resource
    TerritoryManagementService territoryManagementService;
    @Resource
    SalesSummaryDao salesSummaryDao;
    @Resource
    BaseSiteService baseSiteService;
    @Resource
    private KeyGenerator salesSummaryCustomCodeGenerator;
    @Resource
    SclUserDao sclUserDao;
    @Resource
    UserService userService;
    @Resource
    ModelService modelService;

    private static final Logger LOG = Logger.getLogger(SalesSummaryJob.class);

    @Override
    public PerformResult perform(CronJobModel cronJobModel) {

        executeJob(SclCoreConstants.SITE.SHREE_SITE);
        executeJob(SclCoreConstants.SITE.BANGUR_SITE);
        executeJob(SclCoreConstants.SITE.ROCKSTRONG_SITE);

        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }

    private void executeJob(String baseSiteUid) {

        String salesSummaryJobStatus = "N";
        LocalDate now = LocalDate.now();
        LocalDate thirty = now.minusDays(30);
        Instant instant = now.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        Date endDate = Date.from(instant);
        Instant instantThirty = thirty.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        Date startDate = Date.from(instantThirty);
        LOG.info(String.format("Start Date::%s , End Date :: %s", startDate, endDate));

        BaseSiteModel site = baseSiteService.getBaseSiteForUID(baseSiteUid);
        baseSiteService.setCurrentBaseSite(site, Boolean.FALSE);

        List<B2BUnitModel> allowedUnit = site.getAllowedUnit();

        List<SclUserModel> soList = sclUserDao.getAllActiveSO().stream().filter(s -> allowedUnit.contains(s.getDefaultB2BUnit())).collect(Collectors.toList());

        for (SclUserModel so : soList) {
            userService.setCurrentUser(so);
            List<SclCustomerModel> retailersForSubArea = territoryManagementService.getRetailersForSubArea().stream().filter(s -> allowedUnit.contains(s.getDefaultB2BUnit())).collect(Collectors.toList());
            if (retailersForSubArea != null) {
                for (SclCustomerModel sclCustomerModel : retailersForSubArea) {
                    //30 Days logic - Dao impl below
                    //List<List<Object>> customerFromOrderReqForUpload = salesSummaryDao.getCustomerFromNirmanMitraForUpload(TRANSACTION_TYPE, sclCustomerModel.getCustomerNo(), status,startDate,endDate);
                    List<List<Object>> customerFromOrderReqForUpload = salesSummaryDao.getCustomerFromOrderRequisitionForUploadTest(salesSummaryJobStatus, sclCustomerModel);

                    if (customerFromOrderReqForUpload != null && !customerFromOrderReqForUpload.isEmpty()) {
                        LOG.info(String.format("Retailer customer number : %s , Status: %s , Start Date::%s , End Date :: %s", sclCustomerModel.getCustomerNo(), salesSummaryJobStatus, startDate, endDate));
                        for (List<Object> objects : customerFromOrderReqForUpload) {

                            String month = (String) objects.get(0);

                            String year = (String) objects.get(1);
                            SclCustomerModel dealer = (SclCustomerModel) objects.get(2);
                            Double sale = (Double) objects.get(3);
                            Date startDate1 = (Date) objects.get(4);
                            Date endDate1 =(Date) objects.get(4);
                            //String brand = (String) objects.get(5);
                            LOG.info(String.format("month: %s , year : %s , Status: %s , dealerCustomerNo ::%s , sale : %s , custom job code : %s ", month, year, salesSummaryJobStatus, dealer.getCustomerNo(), String.valueOf(sale), salesSummaryCustomCodeGenerator.toString()));
                            if (dealer != null) {
                                RetailerSalesSummaryModel salesSummaryModelExist = salesSummaryDao.validateRecordFromSalesSummary(month, year, dealer.getCustomerNo(), sclCustomerModel.getCustomerNo(),startDate1, endDate1);
                                if (salesSummaryModelExist == null) {
                                    RetailerSalesSummaryModel salesSummaryModel = modelService.create(RetailerSalesSummaryModel.class);
                                    salesSummaryModel.setDealerCode(dealer.getUid());
                                    salesSummaryModel.setDealerName(dealer.getName());
                                    salesSummaryModel.setDealerPotential(dealer.getCounterPotential()!=null ? dealer.getCounterPotential():0.0);
                                    salesSummaryModel.setDealerErpCustomerNo(dealer.getCustomerNo());

                                    salesSummaryModel.setMonth(month);
                                    salesSummaryModel.setYear(year);
                                    salesSummaryModel.setRetailerCode(sclCustomerModel.getUid());
                                    salesSummaryModel.setRetailerName(sclCustomerModel.getName());
                                    salesSummaryModel.setRetailerPotential(sclCustomerModel.getCounterPotential()!=null ? sclCustomerModel.getCounterPotential() :0.0);
                                    salesSummaryModel.setRetailerErpCustomerNo(sclCustomerModel.getCustomerNo());
                                    salesSummaryModel.setSale(sale);
                                    salesSummaryModel.setId(String.valueOf(salesSummaryCustomCodeGenerator.generate()));
                                    salesSummaryModel.setStartDate(startDate1);
                                    salesSummaryModel.setEndDate(endDate1);
                                    if (site != null)
                                        salesSummaryModel.setBrand(site);

                                    if (territoryManagementService.getTerritoriesForCustomer(sclCustomerModel) != null) {
                                        for (SubAreaMasterModel subAreaMasterModel : territoryManagementService.getTerritoriesForCustomer(sclCustomerModel)) {
                                            salesSummaryModel.setSubArea(subAreaMasterModel);
                                        }
                                    }
                                    modelService.save(salesSummaryModel);
                                    OrderRequisitionModel orderRequisitionModel = salesSummaryDao.updateJobStatusForOrderRequisition(sclCustomerModel,salesSummaryJobStatus);
                                    if (orderRequisitionModel != null) {
                                        orderRequisitionModel.setSaleSummaryJobStatus("Y");
                                        modelService.save(orderRequisitionModel);
                                    }
                                } else {
                                    salesSummaryModelExist.setDealerCode(dealer.getUid());
                                    salesSummaryModelExist.setDealerName(dealer.getName());
                                    salesSummaryModelExist.setDealerPotential(dealer.getCounterPotential());
                                    salesSummaryModelExist.setDealerErpCustomerNo(dealer.getCustomerNo());

                                    salesSummaryModelExist.setMonth(month);
                                    salesSummaryModelExist.setYear(year);
                                    salesSummaryModelExist.setRetailerCode(sclCustomerModel.getUid());
                                    salesSummaryModelExist.setRetailerName(sclCustomerModel.getName());
                                    salesSummaryModelExist.setRetailerPotential(sclCustomerModel.getCounterPotential());
                                    salesSummaryModelExist.setRetailerErpCustomerNo(sclCustomerModel.getCustomerNo());
                                    salesSummaryModelExist.setSale(sale);
                                    salesSummaryModelExist.setId(String.valueOf(salesSummaryCustomCodeGenerator.generate()));
                                    salesSummaryModelExist.setStartDate(startDate1);
                                    salesSummaryModelExist.setEndDate(endDate1);
                                    if (site != null)
                                        salesSummaryModelExist.setBrand(site);
                                        if (territoryManagementService.getTerritoriesForCustomer(sclCustomerModel) != null) {
                                            for (SubAreaMasterModel subAreaMasterModel : territoryManagementService.getTerritoriesForCustomer(sclCustomerModel)) {
                                                salesSummaryModelExist.setSubArea(subAreaMasterModel);
                                            }
                                        }
                                        modelService.save(salesSummaryModelExist);
                                        OrderRequisitionModel orderRequisitionModel = salesSummaryDao.updateJobStatusForOrderRequisition(sclCustomerModel,salesSummaryJobStatus);
                                        if (orderRequisitionModel != null) {
                                        orderRequisitionModel.setSaleSummaryJobStatus("Y");
                                        modelService.save(orderRequisitionModel);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public TerritoryManagementService getTerritoryManagementService() {
        return territoryManagementService;
    }

    public void setTerritoryManagementService(TerritoryManagementService territoryManagementService) {
        this.territoryManagementService = territoryManagementService;
    }

    public SalesSummaryDao getSalesSummaryDao() {
        return salesSummaryDao;
    }

    public void setSalesSummaryDao(SalesSummaryDao salesSummaryDao) {
        this.salesSummaryDao = salesSummaryDao;
    }

    public BaseSiteService getBaseSiteService() {
        return baseSiteService;
    }

    public void setBaseSiteService(BaseSiteService baseSiteService) {
        this.baseSiteService = baseSiteService;
    }

    public KeyGenerator getSalesSummaryCustomCodeGenerator() {
        return salesSummaryCustomCodeGenerator;
    }

    public void setSalesSummaryCustomCodeGenerator(KeyGenerator salesSummaryCustomCodeGenerator) {
        this.salesSummaryCustomCodeGenerator = salesSummaryCustomCodeGenerator;
    }

    public SclUserDao getSclUserDao() {
        return sclUserDao;
    }

    public void setSclUserDao(SclUserDao sclUserDao) {
        this.sclUserDao = sclUserDao;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public ModelService getModelService() {
        return modelService;
    }

    @Override
    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }
}
