package com.eydms.core.job;

import com.eydms.core.dao.DJPVisitDao;
import com.eydms.core.model.DealerInfluencerMapModel;
import com.eydms.core.model.DealerRetailerMapModel;
import com.eydms.core.model.RetailerInfluencerMapModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateEyDmsCustomerMappingStatusJob extends AbstractJobPerformable<CronJobModel> {

    private static final Logger LOG = Logger.getLogger(UpdateEyDmsCustomerMappingStatusJob.class);

    @Autowired
    DJPVisitDao djpVisitDao;

    @Autowired
    ModelService modelService;

    @Override
    public PerformResult perform(CronJobModel cronJobModel) {
        try {

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -1);
            Date endDate=calendar.getTime();
            calendar.add(Calendar.YEAR, -1);
            Date startDate=calendar.getTime();

            List<DealerRetailerMapModel> listOfDealerAndRetailers=new ArrayList<>();
            List<DealerInfluencerMapModel> listOfDealerAndInflu=new ArrayList<>();
            List<RetailerInfluencerMapModel> listOfRetailerAndInflu=new ArrayList<>();

            List<DealerRetailerMapModel> listOfDealerAndRetailersMapping=djpVisitDao.getDealerRetailerMappingRecords(startDate, endDate);
            List<DealerInfluencerMapModel> listOfDealerAndInfluMapping=djpVisitDao.getDealerInfluMappingRecords(startDate, endDate);
            List<RetailerInfluencerMapModel> listOfRetailerAndInfluMapping=djpVisitDao.getRetailerInfluMappingRecords(startDate, endDate);

            //Set inactive status
            if(listOfDealerAndRetailersMapping !=null && !listOfDealerAndRetailersMapping.isEmpty()) {
                for (DealerRetailerMapModel dealerRetailerMapModel : listOfDealerAndRetailersMapping) {
                    //finddetailerbyid--to do
                    dealerRetailerMapModel.setStatus(false);
                    listOfDealerAndRetailers.add(dealerRetailerMapModel);
                }
                modelService.saveAll(listOfDealerAndRetailers);
            }
            if(listOfDealerAndInfluMapping !=null && !listOfDealerAndInfluMapping.isEmpty()) {
                for (DealerInfluencerMapModel dealerInfluencerMapModel : listOfDealerAndInfluMapping) {
                    dealerInfluencerMapModel.setStatus(false);
                    listOfDealerAndInflu.add(dealerInfluencerMapModel);
                }
                modelService.saveAll(listOfDealerAndInflu);
            }
            if(listOfRetailerAndInfluMapping != null && !listOfRetailerAndInfluMapping.isEmpty())
            {
                for (RetailerInfluencerMapModel retailerInfluencerMapModel : listOfRetailerAndInfluMapping) {
                    retailerInfluencerMapModel.setStatus(false);
                    listOfRetailerAndInflu.add(retailerInfluencerMapModel);
                }
                modelService.saveAll(listOfRetailerAndInflu);
            }
            return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
        }
         catch (ModelSavingException e){
            LOG.error("Unknown exception occured, reason: {} ", e);
            return new PerformResult(CronJobResult.ERROR, CronJobStatus.FINISHED);
        }
    }
}
