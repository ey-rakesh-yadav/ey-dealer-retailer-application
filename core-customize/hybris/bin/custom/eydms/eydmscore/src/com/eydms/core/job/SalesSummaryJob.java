package com.eydms.core.job;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.dao.SalesSummaryDao;
import com.eydms.core.dao.EyDmsUserDao;
import com.eydms.core.model.*;
import com.eydms.core.services.TerritoryManagementService;
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
    EyDmsUserDao eydmsUserDao;
    @Resource
    UserService userService;
    @Resource
    ModelService modelService;

    private static final Logger LOG = Logger.getLogger(SalesSummaryJob.class);

    @Override
    public PerformResult perform(CronJobModel cronJobModel) {

        executeJob(EyDmsCoreConstants.SITE.SHREE_SITE);
        executeJob(EyDmsCoreConstants.SITE.BANGUR_SITE);
        executeJob(EyDmsCoreConstants.SITE.ROCKSTRONG_SITE);

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

        List<EyDmsUserModel> soList = eydmsUserDao.getAllActiveSO().stream().filter(s -> allowedUnit.contains(s.getDefaultB2BUnit())).collect(Collectors.toList());

        for (EyDmsUserModel so : soList) {
            userService.setCurrentUser(so);
            List<EyDmsCustomerModel> retailersForSubArea = territoryManagementService.getRetailersForSubArea().stream().filter(s -> allowedUnit.contains(s.getDefaultB2BUnit())).collect(Collectors.toList());
            if (retailersForSubArea != null) {
                for (EyDmsCustomerModel eydmsCustomerModel : retailersForSubArea) {
                    //30 Days logic - Dao impl below
                    //List<List<Object>> customerFromOrderReqForUpload = salesSummaryDao.getCustomerFromNirmanMitraForUpload(TRANSACTION_TYPE, eydmsCustomerModel.getCustomerNo(), status,startDate,endDate);
                    List<List<Object>> customerFromOrderReqForUpload = salesSummaryDao.getCustomerFromOrderRequisitionForUploadTest(salesSummaryJobStatus, eydmsCustomerModel);

                    if (customerFromOrderReqForUpload != null && !customerFromOrderReqForUpload.isEmpty()) {
                        LOG.info(String.format("Retailer customer number : %s , Status: %s , Start Date::%s , End Date :: %s", eydmsCustomerModel.getCustomerNo(), salesSummaryJobStatus, startDate, endDate));
                        for (List<Object> objects : customerFromOrderReqForUpload) {

                            String month = (String) objects.get(0);

                            String year = (String) objects.get(1);
                            EyDmsCustomerModel dealer = (EyDmsCustomerModel) objects.get(2);
                            Double sale = (Double) objects.get(3);
                            Date startDate1 = (Date) objects.get(4);
                            Date endDate1 =(Date) objects.get(4);
                            //String brand = (String) objects.get(5);
                            LOG.info(String.format("month: %s , year : %s , Status: %s , dealerCustomerNo ::%s , sale : %s , custom job code : %s ", month, year, salesSummaryJobStatus, dealer.getCustomerNo(), String.valueOf(sale), salesSummaryCustomCodeGenerator.toString()));
                            if (dealer != null) {
                                RetailerSalesSummaryModel salesSummaryModelExist = salesSummaryDao.validateRecordFromSalesSummary(month, year, dealer.getCustomerNo(), eydmsCustomerModel.getCustomerNo(),startDate1, endDate1);
                                if (salesSummaryModelExist == null) {
                                    RetailerSalesSummaryModel salesSummaryModel = modelService.create(RetailerSalesSummaryModel.class);
                                    salesSummaryModel.setDealerCode(dealer.getUid());
                                    salesSummaryModel.setDealerName(dealer.getName());
                                    salesSummaryModel.setDealerPotential(dealer.getCounterPotential()!=null ? dealer.getCounterPotential():0.0);
                                    salesSummaryModel.setDealerErpCustomerNo(dealer.getCustomerNo());

                                    salesSummaryModel.setMonth(month);
                                    salesSummaryModel.setYear(year);
                                    salesSummaryModel.setRetailerCode(eydmsCustomerModel.getUid());
                                    salesSummaryModel.setRetailerName(eydmsCustomerModel.getName());
                                    salesSummaryModel.setRetailerPotential(eydmsCustomerModel.getCounterPotential()!=null ? eydmsCustomerModel.getCounterPotential() :0.0);
                                    salesSummaryModel.setRetailerErpCustomerNo(eydmsCustomerModel.getCustomerNo());
                                    salesSummaryModel.setSale(sale);
                                    salesSummaryModel.setId(String.valueOf(salesSummaryCustomCodeGenerator.generate()));
                                    salesSummaryModel.setStartDate(startDate1);
                                    salesSummaryModel.setEndDate(endDate1);
                                    if (site != null)
                                        salesSummaryModel.setBrand(site);

                                    if (territoryManagementService.getTerritoriesForCustomer(eydmsCustomerModel) != null) {
                                        for (SubAreaMasterModel subAreaMasterModel : territoryManagementService.getTerritoriesForCustomer(eydmsCustomerModel)) {
                                            salesSummaryModel.setSubArea(subAreaMasterModel);
                                        }
                                    }
                                    modelService.save(salesSummaryModel);
                                    OrderRequisitionModel orderRequisitionModel = salesSummaryDao.updateJobStatusForOrderRequisition(eydmsCustomerModel,salesSummaryJobStatus);
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
                                    salesSummaryModelExist.setRetailerCode(eydmsCustomerModel.getUid());
                                    salesSummaryModelExist.setRetailerName(eydmsCustomerModel.getName());
                                    salesSummaryModelExist.setRetailerPotential(eydmsCustomerModel.getCounterPotential());
                                    salesSummaryModelExist.setRetailerErpCustomerNo(eydmsCustomerModel.getCustomerNo());
                                    salesSummaryModelExist.setSale(sale);
                                    salesSummaryModelExist.setId(String.valueOf(salesSummaryCustomCodeGenerator.generate()));
                                    salesSummaryModelExist.setStartDate(startDate1);
                                    salesSummaryModelExist.setEndDate(endDate1);
                                    if (site != null)
                                        salesSummaryModelExist.setBrand(site);
                                        if (territoryManagementService.getTerritoriesForCustomer(eydmsCustomerModel) != null) {
                                            for (SubAreaMasterModel subAreaMasterModel : territoryManagementService.getTerritoriesForCustomer(eydmsCustomerModel)) {
                                                salesSummaryModelExist.setSubArea(subAreaMasterModel);
                                            }
                                        }
                                        modelService.save(salesSummaryModelExist);
                                        OrderRequisitionModel orderRequisitionModel = salesSummaryDao.updateJobStatusForOrderRequisition(eydmsCustomerModel,salesSummaryJobStatus);
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

    public EyDmsUserDao getEyDmsUserDao() {
        return eydmsUserDao;
    }

    public void setEyDmsUserDao(EyDmsUserDao eydmsUserDao) {
        this.eydmsUserDao = eydmsUserDao;
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
