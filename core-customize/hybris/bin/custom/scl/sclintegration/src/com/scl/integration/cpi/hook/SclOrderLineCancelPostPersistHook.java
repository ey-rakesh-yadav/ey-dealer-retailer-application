package com.scl.integration.cpi.hook;

import com.scl.core.enums.NotificationCategory;
import com.scl.core.enums.NotificationStatus;
import com.scl.core.model.SclUserModel;
import com.scl.integration.constants.SclintegrationConstants;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.notificationservices.enums.NotificationType;
import de.hybris.platform.notificationservices.enums.SiteMessageType;
import de.hybris.platform.notificationservices.model.SiteMessageForCustomerModel;
import de.hybris.platform.notificationservices.model.SiteMessageModel;
import de.hybris.platform.odata2services.odata.persistence.hook.PostPersistHook;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class SclOrderLineCancelPostPersistHook implements PostPersistHook {

    @Autowired
    UserService userService;

    @Autowired
    ModelService modelService;

    @Autowired
    KeyGenerator siteMessageUidGenerator;
    private static final String NOTIFICATION_GREETING = "Dear ";

    private static final String ORDER_PLACED_BY = ", order placed by Dealer ";

    private static final String ORDER_QUANTITY = " of quantity ";

    private static final String ORDER_APPROVED = ", has been approved.";

    private static final String ORDER_DATE = " on ";

    private static final String ORDER_CANCELLED = ", has been cancelled.";

    private static final Logger LOG = Logger.getLogger(SclOrderPostPersistHook.class);
    @Override
    public void execute(ItemModel item) {
        if (item instanceof OrderModel) {
            LOG.info("The persistence hook SclOrderPostPersistHook is called!");
            final OrderModel orderModel = (OrderModel) item;

            try {
                if (null != orderModel.getEntries()) {
                    for (int i = 0; i < orderModel.getEntries().size(); i++) {
                        OrderEntryModel entry = (OrderEntryModel) orderModel.getEntries().get(i);
                        if (entry.getStatus() != null && entry.getStatus().equals(OrderStatus.ERP_ORDER_LINE_CANCELLED))
                            cancelOrder(orderModel);
                        break;
                    }
                }
            }catch (RuntimeException e){
                LOG.info("SclOrderLinePostPersistHook for orderCode: "+ orderModel.getCode() + " Exception: "+ e.getMessage() );
                e.printStackTrace();
            }
        }
    }

    private void cancelOrder(OrderModel order) {
        B2BCustomerModel currentUser = null;
        if(order.getPlacedBy()!=null) {
            currentUser = (B2BCustomerModel) order.getPlacedBy();
        }
        final StringBuilder builder = new StringBuilder(NOTIFICATION_GREETING);
        if(null!=order) {
            SiteMessageModel notification = modelService.create(SiteMessageModel.class);
            notification.setNotificationType(NotificationType.NOTIFICATION);
            notification.setSubject(SclintegrationConstants.ORDER_LINE_CANCELLED_NOTIFICATION);
            builder.append(order.getPlacedBy().getUid()).append(ORDER_PLACED_BY);
            builder.append(order.getUser().getUid()).append(ORDER_DATE);
            DateFormat dateFormat = new SimpleDateFormat(SclintegrationConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
            String orderDate = dateFormat.format(order.getDate());
            builder.append(orderDate).append(ORDER_QUANTITY);
            builder.append(order.getTotalQuantity().toString()).append(ORDER_CANCELLED);
            notification.setBody(builder.toString());
            SiteMessageForCustomerModel customer = modelService.create(SiteMessageForCustomerModel.class);
            notification.setOwner(currentUser);
            notification.setEntryNumber(order.getEntries().get(0).getEntryNumber());
            notification.setDealerCode(order.getUser().getUid());
            notification.setOrderCode(order.getCode());
            notification.setCategory(NotificationCategory.ORDER_LINE_CANCELLED_ERP);
            notification.setOrderStatus(order.getStatus().getCode());
            notification.setOrderType(order.getOrderType().getCode());
            customer.setCustomer(currentUser);
            notification.setType(SiteMessageType.SYSTEM);
            notification.setUid(String.valueOf(siteMessageUidGenerator.generate()));
            notification.setExpiryDate(getExpiryDate());
            customer.setMessage(notification);
            customer.setSentDate(new Date());
            modelService.save(notification);
            modelService.save(customer);
            LOG.info(String.format("Order Line Cancelled In App notification sent successfully for Order %s", order.getCode()));
        }
        else
        {
            LOG.error(String.format("Error occured in Order Line Cancelled In App notification for User %s", currentUser));
        }
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
