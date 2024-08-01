package com.scl.core.job;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.customer.dao.SclCustomerDao;
import com.scl.core.dao.SalesPerformanceDao;
import com.scl.core.enums.NetworkType;
import com.scl.core.model.DeliveryItemModel;
import com.scl.core.model.SclCustomerModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.*;

public class DealersNetworkTypeUpdateJob extends AbstractJobPerformable<CronJobModel> {
    private static final Logger LOGGER = Logger.getLogger(DealersNetworkTypeUpdateJob.class);
    @Autowired
    SclCustomerDao sclCustomerDao;
    @Autowired
    ModelService modelService;
    @Autowired
    SalesPerformanceDao salesPerformanceDao;
    @Autowired
    UserService userService;

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

            List<SclCustomerModel> dealersList = sclCustomerDao.getDealersList();

            if (CollectionUtils.isNotEmpty(dealersList)){
                LOGGER.info("Dealers List Size:"+dealersList.size());
                for (SclCustomerModel sclCustomer : dealersList) {
                    if (sclCustomer.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                        Map<String, Object> resultMax = salesPerformanceDao.findMaxInvoicedDateAndQunatityDeliveryItem(sclCustomer);
                        if (resultMax.get(DeliveryItemModel.INVOICECREATIONDATEANDTIME) != null) {
                            sclCustomer.setLastLiftingDate((Date) resultMax.get(DeliveryItemModel.INVOICECREATIONDATEANDTIME));
                            double qty = (double) resultMax.get(DeliveryItemModel.INVOICEQUANTITY);
                            sclCustomer.setLastLiftingQuantity(qty);
                        }
                    }
                    modelService.save(sclCustomer);
                    modelService.refresh(sclCustomer);
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
                LOGGER.info("Dealer list is empty");
            }
        }catch (Exception e){
            LOGGER.info(String.format("Exception in Network type Cron job Dealer:%s,%s",e.getMessage(),e.getCause()));
        }
        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }

}

