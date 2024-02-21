package com.eydms.integration.cpi.hook;

import com.eydms.core.enums.NetworkCategory;
import com.eydms.core.enums.NotificationCategory;
import com.eydms.core.enums.NotificationStatus;
import com.eydms.core.enums.RequisitionStatus;
import com.eydms.core.model.OrderRequisitionModel;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.model.EyDmsUserModel;

import com.eydms.core.notifications.service.EyDmsNotificationService;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.facades.data.OrderRequisitionData;
import com.eydms.core.services.OrderRequisitionService;
import com.eydms.integration.constants.EyDmsintegrationConstants;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.commercefacades.user.data.AddressData;
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
import de.hybris.platform.site.BaseSiteService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class EyDmsOrderPostPersistHook implements PostPersistHook {

    @Autowired
    UserService userService;

    @Autowired
    ModelService modelService;

    @Autowired
    BaseSiteService baseSiteService;

    @Autowired
    OrderRequisitionService orderRequisitionService;

    @Autowired
    KeyGenerator siteMessageUidGenerator;
    @Autowired
    EyDmsNotificationService eydmsNotificationService;

    @Autowired
    TerritoryManagementService territoryManagementService;

    private static final String NOTIFICATION_GREETING = "Dear ";

    private static final String ORDER_PLACED_BY = ", order placed by Dealer ";

    private static final String ORDER_QUANTITY = " of quantity ";

    private static final String ORDER_APPROVED = ", has been approved.";

    private static final String ORDER_DATE = " on ";

    private static final String ORDER_CANCELLED = ", has been cancelled.";


    private static final Logger LOG = Logger.getLogger(EyDmsOrderPostPersistHook.class);
    @Override
    public void execute(ItemModel item) {
        if (item instanceof OrderModel) {
            LOG.info("The persistence hook EyDmsOrderPostPersistHook is called!");
            final OrderModel orderModel = (OrderModel) item;
            if(orderModel.getStatus().equals(OrderStatus.ORDER_ACCEPTED)) {
                submitOrder(orderModel);
            }
        }
    }
    
    private void submitOrder(OrderModel order) {

        B2BCustomerModel currentUser = (B2BCustomerModel) order.getPlacedBy();
        //final StringBuilder builder = new StringBuilder(NOTIFICATION_GREETING);
        B2BCustomerModel user = null;
        if(null!=order) {
        	try {
        		String subject = " Order placed in ERP ";
        		NotificationCategory category = NotificationCategory.ORDER_PUNCH_ERP;

        		StringBuilder builder = new StringBuilder();
        		StringBuilder builder1 = new StringBuilder();
        		StringBuilder builder2 = new StringBuilder();

                Double amount= order.getTotalPrice();
                String formattedAmount = formatIndianNumber(amount);

        		user = (B2BCustomerModel) order.getUser();
        		builder.append("Order No. "+ order.getCode() +" with product "+ order.getProductName() + " of quantity " + order.getTotalQuantity() );
        		builder.append(" MT of ₹"+ formattedAmount + " has been placed for "+ user.getUid());

        		String body = builder.toString();
        		eydmsNotificationService.submitOrderNotification(order,user,body,subject,category);

        		EyDmsUserModel so = territoryManagementService.getSOforCustomer((EyDmsCustomerModel) order.getUser());
        		builder1.append("Order No. "+ order.getCode() +" with product "+ order.getProductName() + " of quantity " + order.getTotalQuantity() );
        		builder1.append(" MT of ₹"+ formattedAmount + " has been placed for "+ so.getUid());

        		String body1 = builder1.toString();
        		eydmsNotificationService.submitOrderNotification(order,so,body1,subject,category);

        		EyDmsCustomerModel sp = territoryManagementService.getSpForCustomerAndBrand((EyDmsCustomerModel) order.getUser(),order.getSite());
        		if(sp!=null) {
        			builder2.append("Order No. "+ order.getCode() +" with product "+ order.getProductName() + " of quantity " + order.getTotalQuantity() );
        			builder2.append(" MT of ₹"+ formattedAmount + " has been placed for "+ sp.getUid());

        			String body2 = builder2.toString();
        			eydmsNotificationService.submitOrderNotification(order,sp,body2,subject,category);
        		}
        		LOG.info(String.format("Order Placed In App notification sent successfully for Order %s", order.getCode()));
        	}
        	catch(Exception e) {
				LOG.error("Error while order punched to ERP notification");
        	}
            if(order.getRetailer()!=null && (order.getRequisitions() == null || order.getRequisitions().isEmpty())) {
                baseSiteService.setCurrentBaseSite(order.getSite(),true);
                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                OrderRequisitionData orderRequisitionData = new OrderRequisitionData();
                AddressData addressData = new AddressData();
                addressData.setId(order.getDeliveryAddress().getRetailerAddressPk());
                orderRequisitionData.setDeliveryAddress(addressData);

                orderRequisitionData.setIsDraft(false);

//                orderRequisitionData.setBaseSiteUid(order.getSite().getUid());


                if(order.getEntries().get(0).getProduct()!=null) {
                    orderRequisitionData.setProductCode(order.getEntries().get(0).getProduct().getCode());
                }

                orderRequisitionData.setFromCustomerUid(order.getUser().getUid());
                orderRequisitionData.setToCustomerUid(order.getRetailer().getUid());

                orderRequisitionData.setQuantity(order.getTotalQuantity() * 20);

                orderRequisitionData.setOrderCode(order.getCode());
                if(order.getErpOrderNumber()!=null) {
                    orderRequisitionData.setErpOrderNo(order.getErpOrderNumber());
                }

                if(order.getEntries().get(0).getExpectedDeliveryDate()!=null) {
                    orderRequisitionData.setExpectedDeliveryDate(dateFormat.format(order.getEntries().get(0).getExpectedDeliveryDate()));
                }

                if(order.getEntries().get(0).getExpectedDeliveryslot()!=null && order.getEntries().get(0).getExpectedDeliveryslot().getCode()!=null && !order.getEntries().get(0).getExpectedDeliveryslot().getCode().isEmpty()) {
                    orderRequisitionData.setExpectedDeliverySlot(order.getEntries().get(0).getExpectedDeliveryslot().getCode());
                }

                orderRequisitionService.saveOrderRequisitionDetails(orderRequisitionData);
            }

        }
        else
        {
            LOG.error(String.format("Error occured in Order Placed In App notification for User %s", currentUser));
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

    public static String formatIndianNumber(double number) {
        if (number < 100000) {
            return String.format("%.0f", number);
        } else {
            int quotient = (int) (number / 100000);
            int remainder = (int) (number % 100000);
            return String.format("%d,%02d,%03d", quotient, (remainder / 1000), (remainder % 1000));
        }
    }
}
