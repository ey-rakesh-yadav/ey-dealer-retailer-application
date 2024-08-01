package com.scl.integration.cpi.hook;

import com.scl.core.enums.NetworkCategory;
import com.scl.core.enums.NotificationCategory;
import com.scl.core.enums.NotificationStatus;
import com.scl.core.enums.RequisitionStatus;
import com.scl.core.model.OrderRequisitionModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.SclUserModel;

import com.scl.core.notifications.service.SclNotificationService;
import com.scl.core.services.TerritoryManagementService;
import com.scl.facades.data.OrderRequisitionData;
import com.scl.core.services.OrderRequisitionService;
import com.scl.integration.constants.SclintegrationConstants;

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

public class SclOrderPostPersistHook implements PostPersistHook {

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
    SclNotificationService sclNotificationService;

    @Autowired
    TerritoryManagementService territoryManagementService;

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
                if (orderModel.getStatus().equals(OrderStatus.ORDER_ACCEPTED)) {
                    submitOrder(orderModel);
                }
            }catch (RuntimeException e){
                LOG.info("SclOrderPostPersistHook for orderCode:"+ orderModel.getCode() + " exception: "+ e.getMessage());
               e.printStackTrace();
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
        		sclNotificationService.submitOrderNotification(order,user,body,subject,category);

        		SclUserModel so = territoryManagementService.getSOforCustomer((SclCustomerModel) order.getUser());
        		builder1.append("Order No. "+ order.getCode() +" with product "+ order.getProductName() + " of quantity " + order.getTotalQuantity() );
        		builder1.append(" MT of ₹"+ formattedAmount + " has been placed for "+ so.getUid());

        		String body1 = builder1.toString();
        		sclNotificationService.submitOrderNotification(order,so,body1,subject,category);

        		SclCustomerModel sp = territoryManagementService.getSpForCustomerAndBrand((SclCustomerModel) order.getUser(),order.getSite());
        		if(sp!=null) {
        			builder2.append("Order No. "+ order.getCode() +" with product "+ order.getProductName() + " of quantity " + order.getTotalQuantity() );
        			builder2.append(" MT of ₹"+ formattedAmount + " has been placed for "+ sp.getUid());

        			String body2 = builder2.toString();
        			sclNotificationService.submitOrderNotification(order,sp,body2,subject,category);
        		}
        		LOG.info(String.format("Order Placed In App notification sent successfully for Order %s", order.getCode()));
        	}
        	catch(Exception e) {
				LOG.error("Error while order punched to ERP notification");
        	}
            if((order.getRetailer()!=null && order.getEntries()!=null) && (order.getEntries().get(0).getRequisitions() == null || order.getEntries().get(0).getRequisitions().isEmpty())) {
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

                orderRequisitionData.setQuantityInMT(order.getTotalQuantity() * 20);

                orderRequisitionData.setOrderCode(order.getCode());
                if(order.getErpOrderNumber()!=null) {
                    orderRequisitionData.setErpOrderNo(order.getErpOrderNumber());
                }

                if(order.getEntries().get(0).getExpectedDeliveryDate()!=null) {
                    orderRequisitionData.setExpectedDeliveryDate(dateFormat.format(order.getEntries().get(0).getExpectedDeliveryDate()));
                }

                if((order.getEntries().get(0).getExpectedDeliveryslot()!=null && order.getEntries().get(0).getExpectedDeliveryslot().getCentreTime()!=null) && !order.getEntries().get(0).getExpectedDeliveryslot().getCentreTime().isEmpty()) {
                    orderRequisitionData.setExpectedDeliverySlot(order.getEntries().get(0).getExpectedDeliveryslot().getCentreTime());
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
