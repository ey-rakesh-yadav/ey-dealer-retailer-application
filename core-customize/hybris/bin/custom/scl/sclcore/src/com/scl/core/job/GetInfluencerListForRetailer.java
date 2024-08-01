package com.scl.core.job;

import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;

public class GetInfluencerListForRetailer extends AbstractJobPerformable<CronJobModel> {
    @Override
    public PerformResult perform(CronJobModel cronJobModel) {
        return null;
    }
}
