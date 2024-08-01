package com.scl.core.job;

import com.scl.core.enums.NotificationCategory;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.services.TerritoryManagementService;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.notificationservices.enums.NotificationType;
import de.hybris.platform.notificationservices.enums.SiteMessageType;
import de.hybris.platform.notificationservices.model.SiteMessageForCustomerModel;
import de.hybris.platform.notificationservices.model.SiteMessageModel;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class NotificationForConfirmGoodsReceivedJob extends AbstractJobPerformable<CronJobModel> {


    @Resource
    FlexibleSearchService flexibleSearchService;

    @Resource
    ModelService modelService;

    @Resource
    private TerritoryManagementService territoryManagementService;

    @Autowired
    KeyGenerator siteMessageUidGenerator;

    private final static Logger LOG = Logger.getLogger(NotificationForConfirmGoodsReceivedJob.class);

    @Override
    public PerformResult perform(CronJobModel cronJobModel) {

        List<OrderEntryModel> orderEntryModel= getOrderEntries();
        if(orderEntryModel.isEmpty()) {
            LOG.error("There are no Order Entries with notificationForConfirmGoodsReceived is null or currentDate greater than 4 hrs");
            return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
        }

        for(OrderEntryModel entryModel : orderEntryModel) {
            OrderModel order = entryModel.getOrder();
            notifyDealer(order,entryModel);
            notifySO(order,entryModel);
            notifySP(order,entryModel);
            entryModel.setNotificationForConfirmGoodsReceived(new Date());
            modelService.save(entryModel);
        }

        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }

    public List<OrderEntryModel> getOrderEntries() {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select  {pk} from {OrderEntry} where {epodInitiateDate}>=?startDate and {epodInitiateDate}<=?endDate  and {deliveredDate} is NUll and {notificationForConfirmGoodsReceived} is null OR datediff(MINUTE, {notificationForConfirmGoodsReceived},current_timestamp) > 240 ");
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


    //notification to Dealer
    public void notifyDealer(OrderModel order,OrderEntryModel entryModel){
        B2BCustomerModel dealer = (B2BCustomerModel) order.getUser();

        final StringBuilder builder = new StringBuilder();
        builder.append("Order no " +order.getCode()+"  with product "+entryModel.getProduct().getName()+" and quantity "+ entryModel.getQuantityInMT() +" has been pending for EPOD for more than 12 hours, kindly ask " + order.getUser().getUid() + " to approve if delivery is completed");
        // builder.append("Order no " +order.getCode()+"  with product "+entryModel.getProduct().getName()+" and quantity "+ entryModel.getQuantityInMT() +" has been ending for EPOD for more than 12 hours, kindly approve if delivery is completed");
        String body = builder.toString();

        SiteMessageModel notification = modelService.create(SiteMessageModel.class);
        notification.setNotificationType(NotificationType.NOTIFICATION);
        notification.setCategory(NotificationCategory.CONFIRM_GOODS_RECEIVED);
        notification.setSubject("EPOD pending by dealer");
        notification.setBody(body);


        SiteMessageForCustomerModel customer = modelService.create(SiteMessageForCustomerModel.class);
        notification.setOwner(dealer);
        customer.setCustomer(dealer);
        notification.setDealerCode(order.getUser().getUid());
        notification.setOrderCode(order.getCode());
        notification.setType(SiteMessageType.SYSTEM);
        notification.setUid(String.valueOf(siteMessageUidGenerator.generate()));
        notification.setExpiryDate(getExpiryDate());
        notification.setOrderStatus(order.getStatus().getCode());
        notification.setOrderType(order.getOrderType().getCode());
        customer.setMessage(notification);
        customer.setSentDate(new Date());
        modelService.save(notification);
        modelService.save(customer);
    }

    //notification to SO
    public void notifySO(OrderModel order,OrderEntryModel entryModel){
        SclCustomerModel dealer = (SclCustomerModel) order.getUser();

        B2BCustomerModel so = territoryManagementService.getSOforCustomer(dealer);
        final StringBuilder builder = new StringBuilder();
        //builder.append("Order no " +order.getCode()+"  with product "+entryModel.getProduct().getName()+" and quantity "+ entryModel.getQuantityInMT() +" has been ending for EPOD for more than 12 hours, kindly approve if delivery is completed");
        builder.append("Order no " +order.getCode()+"  with product "+entryModel.getProduct().getName()+" and quantity "+ entryModel.getQuantityInMT() +" has been pending for EPOD for more than 12 hours, kindly ask " + order.getUser().getUid() + " to approve if delivery is completed");
        String body = builder.toString();
        SiteMessageModel notification = modelService.create(SiteMessageModel.class);
        notification.setNotificationType(NotificationType.NOTIFICATION);
        notification.setCategory(NotificationCategory.CONFIRM_GOODS_RECEIVED);
        notification.setSubject("EPOD pending by dealer");
        notification.setBody(body);
        SiteMessageForCustomerModel customer = modelService.create(SiteMessageForCustomerModel.class);
        notification.setOwner(so);
        customer.setCustomer(so);
        notification.setDealerCode(order.getUser().getUid());
        notification.setOrderCode(order.getCode());
        notification.setType(SiteMessageType.SYSTEM);
        notification.setUid(String.valueOf(siteMessageUidGenerator.generate()));
        notification.setExpiryDate(getExpiryDate());
        notification.setOrderStatus(order.getStatus().getCode());
        notification.setOrderType(order.getOrderType().getCode());
        customer.setMessage(notification);
        customer.setSentDate(new Date());
        modelService.save(notification);
        modelService.save(customer);

    }
    public void notifySP(OrderModel order,OrderEntryModel entryModel){
        SclCustomerModel dealer = (SclCustomerModel) order.getUser();
        BaseSiteModel currentBaseSite  = order.getSite();
        B2BCustomerModel sp = territoryManagementService.getSpForCustomerAndBrand(dealer,currentBaseSite);
        final StringBuilder builder = new StringBuilder();
        //builder.append("Order no " +order.getCode()+"  with product "+entryModel.getProduct().getName()+" and quantity "+ entryModel.getQuantityInMT() +" has been ending for EPOD for more than 12 hours, kindly approve if delivery is completed");
        builder.append("Order no " +order.getCode()+"  with product "+entryModel.getProduct().getName()+" and quantity "+ entryModel.getQuantityInMT() +" has been pending for EPOD for more than 12 hours, kindly ask  " + order.getUser().getUid() + " to approve if delivery is completed");
        String body = builder.toString();
        SiteMessageModel notification = modelService.create(SiteMessageModel.class);
        notification.setNotificationType(NotificationType.NOTIFICATION);
        notification.setCategory(NotificationCategory.CONFIRM_GOODS_RECEIVED);
        notification.setSubject("EPOD pending by dealer");
        notification.setBody(body);

        SiteMessageForCustomerModel customer = modelService.create(SiteMessageForCustomerModel.class);
        notification.setOwner(sp);
        customer.setCustomer(sp);
        notification.setDealerCode(order.getUser().getUid());
        notification.setOrderCode(order.getCode());
        notification.setType(SiteMessageType.SYSTEM);
        notification.setUid(String.valueOf(siteMessageUidGenerator.generate()));
        notification.setExpiryDate(getExpiryDate());
        notification.setOrderStatus(order.getStatus().getCode());
        notification.setOrderType(order.getOrderType().getCode());
        customer.setMessage(notification);
        customer.setSentDate(new Date());
        modelService.save(notification);
        modelService.save(customer);

    }

    private Date getExpiryDate(){
        LocalDate date = LocalDate.now().plusDays(30);
        Date expiryDate = null;
        try {
            expiryDate = new SimpleDateFormat("yyyy-MM-dd").parse(String.valueOf(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return expiryDate;
    }
}
