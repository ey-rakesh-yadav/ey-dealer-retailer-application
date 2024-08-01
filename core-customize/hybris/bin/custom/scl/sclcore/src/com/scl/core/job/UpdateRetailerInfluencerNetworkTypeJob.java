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

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class UpdateRetailerInfluencerNetworkTypeJob extends AbstractJobPerformable {

    @Resource
    ModelService modelService;
    @Resource
    SclCustomerDao sclCustomerDao;

    public SclCustomerDao getSclCustomerDao() {
        return sclCustomerDao;
    }

    public void setSclCustomerDao(SclCustomerDao sclCustomerDao) {
        this.sclCustomerDao = sclCustomerDao;
    }

    @Override
    public PerformResult perform(CronJobModel cronJobModel) {
        List<SclCustomerModel> customerList=new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.MONTH, -1);
        Date oneMonthDate = cal.getTime();
        cal.add(Calendar.MONTH, -2);
        Date threeMonthDate = cal.getTime();
        List<SclCustomerModel> retailerInfluencerList = sclCustomerDao.getRetailerInfluencerList();
        for (SclCustomerModel sclCustomer : retailerInfluencerList) {
            if(sclCustomer.getLastLiftingDate()!=null)
            {
                if (oneMonthDate.compareTo(sclCustomer.getLastLiftingDate())<0)
                    sclCustomer.setNetworkType(NetworkType.ACTIVE.getCode());
                else if(threeMonthDate.compareTo(sclCustomer.getLastLiftingDate())<0)
                    sclCustomer.setNetworkType(NetworkType.INACTIVE.getCode());
                else
                    sclCustomer.setNetworkType(NetworkType.DORMANT.getCode());
            }
            else
            {
                sclCustomer.setNetworkType(NetworkType.DORMANT.getCode());
            }
            customerList.add(sclCustomer);
        }
        modelService.saveAll(customerList);
        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }
}
