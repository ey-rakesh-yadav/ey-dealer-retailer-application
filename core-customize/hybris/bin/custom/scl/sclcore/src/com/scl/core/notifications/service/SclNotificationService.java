package com.scl.core.notifications.service;

import com.scl.core.enums.NotificationCategory;
import com.scl.core.model.*;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.model.order.OrderModel;

import javax.management.Notification;
import java.util.Map;

public interface SclNotificationService {
    boolean updateNotificationStatus(String siteMessageId);
    void sendInAppNotificationForCreditLimitBreach(OrderModel order, Double totalCreditLimit, Double currentOutstanding, Double orderAmount, Double pendingOrderAmount);

    void submitOrderNotification(OrderModel order, B2BCustomerModel currentUser, String body, String subject, NotificationCategory category);

    void submitOrderEntryNotification(OrderModel order, B2BCustomerModel currentUser, String body, String subject, NotificationCategory category,int entryNumber);

     void submitOrderRequisitionNotification(OrderRequisitionModel order, B2BCustomerModel currentUser, String body, String subject, NotificationCategory category);

    void submitLimitNotification(SclCustomerModel customerModel, B2BCustomerModel currentUser, String body, String subject, NotificationCategory category);
  
    void submitDealerNotification(B2BCustomerModel currentUser, String body, String subject, NotificationCategory category, Map<String,String> suggestion);

    void siteConversionNotification(SclSiteMasterModel sclSiteMaster, B2BCustomerModel mappedSO, String body, String subject, NotificationCategory category);

    void updateNotificationForPartner(PartnerCustomerModel partnerCustomer, String body,NotificationCategory category);
}
