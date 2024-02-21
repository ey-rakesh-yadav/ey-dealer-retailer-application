package com.eydms.core.job;

import com.eydms.core.dao.AutoCancellingRequestRaisedByInfluencerDao;
import com.eydms.core.enums.PointRequisitionStatus;
import com.eydms.core.model.PointRequisitionModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class AutoCancellingRequestRaisedByInfluencerJob extends AbstractJobPerformable<CronJobModel> {
    @Resource
    AutoCancellingRequestRaisedByInfluencerDao autoCancellingRequestRaisedByInfluencerDao;
    @Resource
    ModelService modelService;

    private static final Logger LOG = Logger.getLogger(AutoCancellingRequestRaisedByInfluencerJob.class);

    @Override
    public PerformResult perform(CronJobModel cronJobModel) {

        LocalDate currentDate=LocalDate.now();
        Date beforeThreeDays = Date.from(currentDate.minusDays(3).atStartOfDay(ZoneId.systemDefault()).toInstant());
        LOG.info(String.format("Before Three Days:%s",beforeThreeDays));
        List<PointRequisitionModel> listOfRequestRaisedBeforeThreeDays = autoCancellingRequestRaisedByInfluencerDao.getListOfRequestRaisedBeforeThreeDays(beforeThreeDays);
        if(listOfRequestRaisedBeforeThreeDays!=null && !listOfRequestRaisedBeforeThreeDays.isEmpty()) {
            LOG.info(String.format("Point Req for Cancel:%s",listOfRequestRaisedBeforeThreeDays.size()));
            for (PointRequisitionModel listOfRequestRaisedBeforeThreeDay : listOfRequestRaisedBeforeThreeDays) {
                listOfRequestRaisedBeforeThreeDay.setReqCancellationDate(Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                listOfRequestRaisedBeforeThreeDay.setStatus(PointRequisitionStatus.CANCELLED);
                listOfRequestRaisedBeforeThreeDay.setCancelReason("Auto Cancelled by system");
                modelService.save(listOfRequestRaisedBeforeThreeDay);
            }
        }
        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }

    public AutoCancellingRequestRaisedByInfluencerDao getAutoCancellingRequestRaisedByInfluencerDao() {
        return autoCancellingRequestRaisedByInfluencerDao;
    }

    public void setAutoCancellingRequestRaisedByInfluencerDao(AutoCancellingRequestRaisedByInfluencerDao autoCancellingRequestRaisedByInfluencerDao) {
        this.autoCancellingRequestRaisedByInfluencerDao = autoCancellingRequestRaisedByInfluencerDao;
    }

    public ModelService getModelService() {
        return modelService;
    }

    @Override
    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }
}
