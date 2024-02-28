package com.eydms.core.job;

import com.eydms.core.customer.dao.EyDmsCustomerDao;
import com.eydms.core.enums.NetworkType;
import com.eydms.core.model.EyDmsCustomerModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.model.ModelService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class UpdateRetailerInfluencerNetworkTypeJob extends AbstractJobPerformable {

    @Resource
    ModelService modelService;
    @Resource
    EyDmsCustomerDao eydmsCustomerDao;

    public EyDmsCustomerDao getEyDmsCustomerDao() {
        return eydmsCustomerDao;
    }

    public void setEyDmsCustomerDao(EyDmsCustomerDao eydmsCustomerDao) {
        this.eydmsCustomerDao = eydmsCustomerDao;
    }

    @Override
    public PerformResult perform(CronJobModel cronJobModel) {
        List<EyDmsCustomerModel> customerList=new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.MONTH, -1);
        Date oneMonthDate = cal.getTime();
        cal.add(Calendar.MONTH, -2);
        Date threeMonthDate = cal.getTime();
        List<EyDmsCustomerModel> retailerInfluencerList = eydmsCustomerDao.getRetailerInfluencerList();
        for (EyDmsCustomerModel eydmsCustomer : retailerInfluencerList) {
            if(eydmsCustomer.getLastLiftingDate()!=null)
            {
                if (oneMonthDate.compareTo(eydmsCustomer.getLastLiftingDate())<0)
                    eydmsCustomer.setNetworkType(NetworkType.ACTIVE.getCode());
                else if(threeMonthDate.compareTo(eydmsCustomer.getLastLiftingDate())<0)
                    eydmsCustomer.setNetworkType(NetworkType.INACTIVE.getCode());
                else
                    eydmsCustomer.setNetworkType(NetworkType.DORMANT.getCode());
            }
            else
            {
                eydmsCustomer.setNetworkType(NetworkType.DORMANT.getCode());
            }
            customerList.add(eydmsCustomer);
        }
        modelService.saveAll(customerList);
        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }
}
