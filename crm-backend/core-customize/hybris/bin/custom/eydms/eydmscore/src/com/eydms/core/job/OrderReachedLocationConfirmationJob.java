package com.eydms.core.job;

import com.eydms.core.enums.NotificationCategory;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.core.notifications.service.EyDmsNotificationService;
import com.eydms.core.services.TerritoryManagementService;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class OrderReachedLocationConfirmationJob extends AbstractJobPerformable<CronJobModel> {
    @Resource
    FlexibleSearchService flexibleSearchService;

    @Resource
    ModelService modelService;

    @Resource
    private TerritoryManagementService territoryManagementService;

    @Autowired
    KeyGenerator siteMessageUidGenerator;

    @Autowired
    EyDmsNotificationService eydmsNotificationService;

    private final static Logger LOG = Logger.getLogger(OrderReachedLocationConfirmationJob.class);

    @Override
    public PerformResult perform(CronJobModel cronJobModel) {

        List<OrderEntryModel> orderEntryModel= getOrderEntries();
        if(orderEntryModel.isEmpty()) {
            LOG.error("There are no Order Entries with OrderReachedLocation is null or currentDate greater than 4 hrs");
            return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
        }

        for(OrderEntryModel entryModel : orderEntryModel) {

            try{
                StringBuilder builder = new StringBuilder();
                builder.append("Order no."+entryModel.getOrder().getCode()+" / "+entryModel.getEntryNumber()+ " arrived at the destination since more than 4 hours ");
                builder.append(",kindly confirm the order with order number "+entryModel.getOrder().getCode()+" / "+entryModel.getEntryNumber());
                builder.append(", "+entryModel.getProduct().getName()+" , "+entryModel.getQuantityInMT()+ " MT ");

                String body = builder.toString();
                StringBuilder builder1 = new StringBuilder();
                builder1.append("Order Reached Location Confirmation");

                String subject = builder1.toString();

                NotificationCategory category = NotificationCategory.ORDER_REACHED_LOCATION_CONFIRMATION;
                eydmsNotificationService.submitOrderEntryNotification(entryModel.getOrder(),(B2BCustomerModel) entryModel.getOrder().getUser(),body,subject,category,entryModel.getEntryNumber());

                StringBuilder builders = new StringBuilder();
                builders.append(entryModel.getOrder().getUser().getName()+ "- "+entryModel.getOrder().getUser().getUid()+" has not confirmed that the order has reached the destination - "+entryModel.getOrder().getDeliveryAddress());
                builders.append(" ,kindly as the dealer to confirm the order with order number "+entryModel.getOrder().getCode()+" / "+entryModel.getEntryNumber());
                builders.append(" , "+entryModel.getProduct().getName()+" , "+entryModel.getQuantityInMT()+ " MT ");


                String bod = builders.toString();
                StringBuilder builder2 = new StringBuilder();
                builder2.append("Order Reached Location Confirmation");

                String sub = builder2.toString();

                EyDmsUserModel so = territoryManagementService.getSOforCustomer((EyDmsCustomerModel) entryModel.getOrder().getUser());
                eydmsNotificationService.submitOrderEntryNotification(entryModel.getOrder(),so,bod,sub,category,entryModel.getEntryNumber());

                EyDmsCustomerModel sp = territoryManagementService.getSpForCustomerAndBrand((EyDmsCustomerModel) entryModel.getOrder().getUser(),entryModel.getOrder().getSite());
                eydmsNotificationService.submitOrderEntryNotification(entryModel.getOrder(),sp,bod,sub,category,entryModel.getEntryNumber());
                entryModel.setNotificationForConfirmGoodsReceived(new Date());
                modelService.save(entryModel);

            }
            catch(Exception e){
                LOG.error("Error while sending Order Arrival Confirmation Notification");
            }

        }

        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }

    public List<OrderEntryModel> getOrderEntries() {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select  {pk} from {OrderEntry} where {truckReachedDate}>=?startDate and {truckReachedDate}<=?endDate  and {deliveredDate} is NUll and {notificationForConfirmGoodsReceived} is null OR datediff(MINUTE, {notificationForConfirmGoodsReceived},current_timestamp) > 240 ");
        params.put("status", OrderStatus.TRUCK_REACHED_DESTINATION);
        LocalDate endDate1=LocalDate.now();
        LocalDate startDate1=endDate1.minusDays(15);

        Date startDate = Date.from(startDate1.atStartOfDay(ZoneId.systemDefault()).toInstant());
        LocalDateTime currentLocalDateTime = LocalDateTime.now(); // Get current date and time

        // Subtract 12 hours from currentLocalDateTime
        LocalDateTime newLocalDateTime = currentLocalDateTime.minusHours(12);

        Date endDate = Date.from(newLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());

        params.put("startDate",startDate);
        params.put("endDate",endDate);
        params.put("startTime", LocalTime.now().minus(12, ChronoUnit.HOURS).getHour());
        params.put("endTime", LocalTime.now().getHour());

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(OrderEntryModel.class));
        query.addQueryParameters(params);
        final SearchResult<OrderEntryModel> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult() != null && !(searchResult.getResult().isEmpty())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();
    }

}
