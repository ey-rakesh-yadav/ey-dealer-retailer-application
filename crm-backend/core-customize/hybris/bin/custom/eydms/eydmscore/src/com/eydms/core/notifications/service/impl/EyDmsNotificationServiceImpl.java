package com.eydms.core.notifications.service.impl;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.dao.DJPVisitDao;
import com.eydms.core.enums.NotificationCategory;
import com.eydms.core.enums.NotificationStatus;
import com.eydms.core.model.OrderRequisitionModel;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.model.PointRequisitionModel;
import com.eydms.core.notifications.dao.EyDmsSiteMessageDao;
import com.eydms.core.notifications.service.EyDmsNotificationService;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.notificationservices.enums.NotificationType;
import de.hybris.platform.notificationservices.enums.SiteMessageType;
import de.hybris.platform.notificationservices.model.SiteMessageForCustomerModel;
import de.hybris.platform.notificationservices.model.SiteMessageModel;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.management.Notification;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;

public class EyDmsNotificationServiceImpl implements EyDmsNotificationService {

    @Autowired
    ModelService modelService;

    @Autowired
    EyDmsSiteMessageDao siteMessageDao;

    @Autowired
    UserService userService;

    @Autowired
    KeyGenerator siteMessageUidGenerator;

    @Autowired
    DJPVisitDao djpVisitDao;

    private static final Logger LOG = Logger.getLogger(EyDmsNotificationServiceImpl.class);

    private static final String NOTIFICATION_GREETING = "Dear ";

    private static final String ORDER_PLACED_BY = ", order placed by Dealer ";

    private static final String ORDER_TOTAL_PRICE = " of amount Rs.";

    private static final String CREDIT_LIMIT = ", exceeds dealers credit limit of Rs.";

    private static final String REMAINING_AMOUNT = " by Rs.";

    private static final String REVIEW = " .Please review the order details.";

    @Override
    public boolean updateNotificationStatus(String siteMessageId) {
        SiteMessageModel siteMessage = siteMessageDao.findSiteMessageById(siteMessageId);
        siteMessage.setStatus(NotificationStatus.READ);
        modelService.save(siteMessage);
        return true;
    }

    @Override
    public void sendInAppNotificationForCreditLimitBreach(OrderModel order, Double totalCreditLimit, Double currentOutstanding, Double orderAmount, Double pendingOrderAmount) {
        B2BCustomerModel currentUser = (B2BCustomerModel) order.getPlacedBy();
        final StringBuilder builder = new StringBuilder(NOTIFICATION_GREETING);
        if(null!=order) {
            SiteMessageModel notification = modelService.create(SiteMessageModel.class);
            notification.setNotificationType(NotificationType.NOTIFICATION);
            notification.setSubject(EyDmsCoreConstants.ORDER_NOTIFICATION.ORDER_CREDIT_LIMIT_BREACH_NOTIFICATION);
            builder.append(order.getPlacedBy().getUid()).append(ORDER_PLACED_BY);
            builder.append(order.getUser().getUid()).append(ORDER_TOTAL_PRICE);
            builder.append(order.getTotalPrice()).append(CREDIT_LIMIT);
            builder.append(totalCreditLimit).append(REMAINING_AMOUNT);
            double remainingAmount = Math.abs(totalCreditLimit - (currentOutstanding + orderAmount + pendingOrderAmount));
            builder.append(remainingAmount).append(REVIEW);
            notification.setBody(builder.toString());
            SiteMessageForCustomerModel customer = modelService.create(SiteMessageForCustomerModel.class);
            notification.setOwner(currentUser);
            customer.setCustomer(currentUser);
            notification.setDealerCode(order.getUser().getUid());
            notification.setOrderCode(order.getCode());
            notification.setCategory(NotificationCategory.ORDER_CREDIT_LIMIT_BREACHED);
            notification.setType(SiteMessageType.SYSTEM);
            notification.setUid(String.valueOf(siteMessageUidGenerator.generate()));
            notification.setExpiryDate(getExpiryDate());
            notification.setOrderStatus(order.getStatus().getCode());
            notification.setOrderType(order.getOrderType().getCode());
            customer.setMessage(notification);
            customer.setSentDate(new Date());
            modelService.save(notification);
            modelService.save(customer);
            LOG.info(String.format("Order Credit Limit Breached In App notification sent successfully for Order %s", order.getCode()));
        }
        else
        {
            LOG.error(String.format("Error occurred in Order Credit Limit Breached In App notification for User %s", currentUser));
        }
    }

    @Override
    public void submitOrderNotification(OrderModel order, B2BCustomerModel currentUser, String body, String subject, NotificationCategory category) {
        SiteMessageModel notification = modelService.create(SiteMessageModel.class);


        notification.setNotificationType(NotificationType.NOTIFICATION);
        notification.setSubject(subject);
        notification.setBody(body);

        SiteMessageForCustomerModel customer = modelService.create(SiteMessageForCustomerModel.class);

        notification.setOwner(currentUser);

        customer.setCustomer(currentUser);

        notification.setEntryNumber(order.getEntries().get(0).getEntryNumber());
        notification.setDealerCode(order.getUser().getUid());
        notification.setOrderCode(order.getCode());
        notification.setCategory(category);
        notification.setType(SiteMessageType.SYSTEM);
        notification.setUid(String.valueOf(siteMessageUidGenerator.generate()));
        notification.setExpiryDate(getExpiryDate());
        notification.setOrderStatus(order.getStatus().getCode());
        notification.setOrderType(order.getOrderType().getCode());

        customer.setMessage(notification);
        customer.setSentDate(new Date());

        modelService.save(notification);
        modelService.save(customer);

        LOG.info(String.format("Order Placed In App notification sent successfully for Order %s", order.getCode()));

    }

    public void submitOrderRequisitionNotification(OrderRequisitionModel order,B2BCustomerModel currentUser, String body, String subject, NotificationCategory category){

        SiteMessageModel notification = modelService.create(SiteMessageModel.class);

        notification.setNotificationType(NotificationType.NOTIFICATION);
        notification.setSubject(subject);
        notification.setBody(body);
        notification.setCategory(category);

        SiteMessageForCustomerModel customer = modelService.create(SiteMessageForCustomerModel.class);

        notification.setOwner(currentUser);

        customer.setCustomer(currentUser);

        notification.setType(SiteMessageType.SYSTEM);
        notification.setUid(String.valueOf(siteMessageUidGenerator.generate()));
        notification.setExpiryDate(getExpiryDate());
        notification.setOrderStatus(order.getStatus().getCode());
        customer.setMessage(notification);
        customer.setSentDate(new Date());
        modelService.save(notification);
        modelService.save(customer);

        LOG.info(String.format("Requisition saved"));
    }

    public void submitDealerNotification(B2BCustomerModel currentUser, String body, String subject, NotificationCategory category, Map<String,String> suggestion){

        SiteMessageModel notification = modelService.create(SiteMessageModel.class);

        notification.setNotificationType(NotificationType.NOTIFICATION);
        notification.setSubject(subject);
        notification.setBody(body);
        notification.setCategory(category);
        notification.setSuggestions(suggestion);


        SiteMessageForCustomerModel customer = modelService.create(SiteMessageForCustomerModel.class);

        notification.setOwner(currentUser);

        customer.setCustomer(currentUser);

        notification.setType(SiteMessageType.SYSTEM);
        notification.setUid(String.valueOf(siteMessageUidGenerator.generate()));
        notification.setExpiryDate(getExpiryDate());
        customer.setMessage(notification);
        customer.setSentDate(new Date());
        modelService.save(notification);
        modelService.save(customer);

        LOG.info(String.format("Point Requisition saved"));
    }

    @Override
    public void submitOrderEntryNotification(OrderModel order, B2BCustomerModel currentUser, String body, String subject, NotificationCategory category,int entryNumber) {
        SiteMessageModel notification = modelService.create(SiteMessageModel.class);


        notification.setNotificationType(NotificationType.NOTIFICATION);
        notification.setSubject(subject);
        notification.setBody(body);

        SiteMessageForCustomerModel customer = modelService.create(SiteMessageForCustomerModel.class);

        notification.setOwner(currentUser);

        customer.setCustomer(currentUser);

        notification.setEntryNumber(entryNumber);
        notification.setDealerCode(order.getUser().getUid());
        notification.setOrderCode(order.getCode());
        notification.setCategory(category);
        notification.setType(SiteMessageType.SYSTEM);
        notification.setUid(String.valueOf(siteMessageUidGenerator.generate()));
        notification.setExpiryDate(getExpiryDate());
        notification.setOrderStatus(order.getStatus().getCode());
        notification.setOrderType(order.getOrderType().getCode());

        customer.setMessage(notification);
        customer.setSentDate(new Date());

        modelService.save(notification);
        modelService.save(customer);

        LOG.info(String.format("Order Placed In App notification sent successfully for Order %s", order.getCode()));

    }

    @Override
    public void submitLimitNotification(EyDmsCustomerModel customerModel, B2BCustomerModel currentUser, String body, String subject, NotificationCategory category) {

        SiteMessageModel notification = modelService.create(SiteMessageModel.class);
        notification.setNotificationType(NotificationType.NOTIFICATION);
        notification.setSubject(subject);
        notification.setBody(body);

        SiteMessageForCustomerModel customer = modelService.create(SiteMessageForCustomerModel.class);

        notification.setOwner(currentUser);
        notification.setDealerCode(currentUser.getUid());

        customer.setCustomer(currentUser);
        customer.setMessage(notification);
        customer.setSentDate(new Date());

        modelService.save(notification);
        modelService.save(customer);

        LOG.info(String.format("App notification sent successfully for dealer %s",customerModel.getCustomerNo()));

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
