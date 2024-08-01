package com.scl.core.job;

import com.scl.core.customer.dao.SclCustomerDao;
import com.scl.core.enums.NetworkType;
import com.scl.core.model.SclCustomerModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class InfluencerNetworkTypeUpdateJob   extends AbstractJobPerformable<CronJobModel> {
    private static final Logger LOGGER = Logger.getLogger(InfluencerNetworkTypeUpdateJob.class);
    @Autowired
    SclCustomerDao sclCustomerDao;
    @Autowired
    ModelService modelService;


    @Override
    public PerformResult perform(CronJobModel arg0) {
        try {
            List<SclCustomerModel> customerList = new ArrayList<>();
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            cal.add(Calendar.MONTH, -1);
            Date oneMonthDate = cal.getTime();
            cal.add(Calendar.MONTH, -2);
            Date threeMonthDate = cal.getTime();

            List<SclCustomerModel> influencersList = sclCustomerDao.getInfluencersList();

            if (CollectionUtils.isNotEmpty(influencersList)){
                LOGGER.info("Influencer List Size:"+influencersList.size());
                for (SclCustomerModel sclCustomer : influencersList) {
                    if (sclCustomer.getLastLiftingDate() != null) {
                        if (oneMonthDate.compareTo(sclCustomer.getLastLiftingDate()) < 0)
                            sclCustomer.setNetworkType(NetworkType.ACTIVE.getCode());
                        else if (threeMonthDate.compareTo(sclCustomer.getLastLiftingDate()) < 0)
                            sclCustomer.setNetworkType(NetworkType.INACTIVE.getCode());
                        else
                            sclCustomer.setNetworkType(NetworkType.DORMANT.getCode());
                    } else {
                        sclCustomer.setNetworkType(NetworkType.DORMANT.getCode());
                    }
                    modelService.save(sclCustomer);
                    LOGGER.info(String.format("customer saving into model:%s", sclCustomer.getUid()));
                    customerList.add(sclCustomer);
                }

                modelService.saveAll(customerList);
            }else{
                LOGGER.info("Influencer list is empty");
            }
        }catch (Exception e){
            LOGGER.info(String.format("Exception in Network type Cron job Influencer:%s,%s",e.getMessage(),e.getCause()));
        }
        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }

}


