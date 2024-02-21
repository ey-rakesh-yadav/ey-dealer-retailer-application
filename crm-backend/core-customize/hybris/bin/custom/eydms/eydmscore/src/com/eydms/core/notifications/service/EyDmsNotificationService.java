package com.eydms.core.notifications.service;

import com.eydms.core.enums.NotificationCategory;
import com.eydms.core.model.OrderRequisitionModel;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.model.PointRequisitionModel;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.model.order.OrderModel;

import javax.management.Notification;
import java.util.Map;

public interface EyDmsNotificationService {
    boolean updateNotificationStatus(String siteMessageId);
    void sendInAppNotificationForCreditLimitBreach(OrderModel order, Double totalCreditLimit, Double currentOutstanding, Double orderAmount, Double pendingOrderAmount);

    void submitOrderNotification(OrderModel order, B2BCustomerModel currentUser, String body, String subject, NotificationCategory category);

    void submitOrderEntryNotification(OrderModel order, B2BCustomerModel currentUser, String body, String subject, NotificationCategory category,int entryNumber);

     void submitOrderRequisitionNotification(OrderRequisitionModel order, B2BCustomerModel currentUser, String body, String subject, NotificationCategory category);

    void submitLimitNotification(EyDmsCustomerModel customerModel, B2BCustomerModel currentUser, String body, String subject, NotificationCategory category);
  
    void submitDealerNotification(B2BCustomerModel currentUser, String body, String subject, NotificationCategory category, Map<String,String> suggestion);

}
