package com.eydms.core.job;

import com.eydms.core.dao.DJPVisitDao;
import com.eydms.core.enums.ApprovalStatus;
import com.eydms.core.model.VisitMasterModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class DJPUnflaggedApprovalSLAJob extends AbstractJobPerformable<CronJobModel> {

    private static final Logger LOG = Logger.getLogger(DJPUnflaggedApprovalSLAJob.class);

    @Autowired
    ModelService modelService;
    @Resource
    DJPVisitDao djpVisitDao;

    @Override
    public PerformResult perform(CronJobModel cronJobModel) {

        LocalDate currentDate = LocalDate.now();
        Date date = Date.from(currentDate.minusDays(3).atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<VisitMasterModel> visitList = djpVisitDao.getVisitsForDJPUnflaggedSLA(date);

        if(visitList.isEmpty()) {
            LOG.error("There are no visits with visit status as pending for approval");
            return new PerformResult(CronJobResult.FAILURE, CronJobStatus.ABORTED);
        }


        if(visitList!=null && !visitList.isEmpty()) {
            LOG.info("inside djp unflagged approval sla job" + visitList.size());
            for (VisitMasterModel model : visitList) {
                LOG.info("visit model pk" + model);
                model.setApprovalStatus(ApprovalStatus.SYSTEM_REJECTED);
                if (model.getRouteDeviationReason() != null || model.getObjectiveDeviationReason() != null) {
                    model.setApprovalStatus(ApprovalStatus.SYSTEM_APPROVED);
                } else {
                    long counterDeviationReasons = model.getCounterVisits().stream().filter(counter -> ((counter.getDeviationReason() != null) && !(counter.getDeviationReason().isEmpty()))).count();
                    if (counterDeviationReasons > 0) {
                        model.setApprovalStatus(ApprovalStatus.SYSTEM_APPROVED);
                    }
                }
                modelService.save(model);
            }
            LOG.info("job is performed");
        }
        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }
}
