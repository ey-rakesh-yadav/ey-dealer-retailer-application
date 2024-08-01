package com.scl.facades.hook;

import com.scl.core.enums.*;
import com.scl.core.event.UpdateDealerRetailerMappingEvent;
import com.scl.core.model.OrderRequisitionModel;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.classification.ClassificationService;
import de.hybris.platform.cms2.jalo.site.CMSSite;
import de.hybris.platform.commerceservices.order.hook.CommercePlaceOrderMethodHook;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.commerceservices.service.data.CommerceOrderResult;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.AddressService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import de.hybris.platform.classification.features.Feature;
import de.hybris.platform.classification.features.FeatureList;
import de.hybris.platform.classification.features.FeatureValue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.OrderRequisitionDao;
import com.scl.core.order.services.SclOrderService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;


public class DefaultSCLPlaceOrderMethodHook implements CommercePlaceOrderMethodHook {

    private SessionService sessionService;

    private ModelService modelService;

    private UserService userService;

    @Resource
    private EventService eventService;

    @Autowired
    ClassificationService classificationService;
    
    @Autowired
    SclOrderService sclOrderService;
    
    @Autowired
    EnumerationService enumerationService;
    
	@Autowired
	OrderRequisitionDao orderRequisitionDao;
	
	@Autowired
	private AddressService addressService;
	
    public static final String ROLE_CUSTOMERMANAGERGROUP = "ROLE_CUSTOMERMANAGERGROUP";
    private static final String ACTING_USER_UID = "ACTING_USER_UID";

    private static final Logger LOG = Logger.getLogger(DefaultSCLPlaceOrderMethodHook.class);

    @Override
    public void afterPlaceOrder(CommerceCheckoutParameter parameter, CommerceOrderResult orderResult) throws InvalidCartException {
    	OrderModel orderModel = orderResult.getOrder();
    	orderModel.setStatus(OrderStatus.ORDER_RECEIVED);
    	final Authentication auth = getAuth();
		if (hasRole(ROLE_CUSTOMERMANAGERGROUP, auth)){
			orderModel.setPlacedBy(userService.getUserForUID(sessionService.getAttribute(ACTING_USER_UID)));
		}
		else {
			orderModel.setPlacedBy(userService.getCurrentUser());
		}
		//setting created from CRM
        orderModel.setCreatedFromCRMorERP(CreatedFromCRMorERP.CRM);
    	if(CRMOrderType.GIFT.equals(orderModel.getCrmOrderType())) {
//    		if(orderModel.getPlacedBy()!=null && orderModel.getPlacedBy().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.TSO_GROUP_ID))) {
//    			orderModel.setStatus(OrderStatus.APPROVED);
//    			orderModel.setOrderSentForApprovalDate(new Date());
//    			orderModel.setApprovedBy((B2BCustomerModel) userService.getCurrentUser());
//    		}
//    		else {
//    			orderModel.setStatus(OrderStatus.PENDING_APPROVAL);
//    		}    
    		orderModel.setStatus(OrderStatus.PENDING_APPROVAL);
    	}
    	else {
    		Double totalPrice = calculateTotalPrice(orderModel);
    		orderModel.setTotalPrice(totalPrice);
    		updateOrderEntryAddress(orderModel);
    	}


    	List<AbstractOrderEntryModel> entries = orderModel.getEntries();
		if(CollectionUtils.isNotEmpty(entries)) {
			List<OrderRequisitionModel> modelList = new ArrayList<OrderRequisitionModel>();
			for(AbstractOrderEntryModel entry : entries) {
				OrderRequisitionModel requisitionModel = orderRequisitionDao.findByRequisitionId(entry.getOrderRequisitionId());
				if(requisitionModel!=null) {
					requisitionModel.setOrderEntry(entry);
					requisitionModel.setStatus(RequisitionStatus.PENDING_FULFILLMENT);
					requisitionModel.setAcceptedDate(new Date());
                    requisitionModel.setServiceType(ServiceType.PLACED);
					/*if(requistionsList.size()>1) {
						requisitionModel.setServiceType(ServiceType.CLUBBED_PLACED);
					}
					else {
						requisitionModel.setServiceType(ServiceType.PLACED);
					}*/
					modelList.add(requisitionModel);
				}
			}
			modelService.saveAll(modelList);
		}
		orderModel.setLatestStatusUpdate(orderModel.getDate());

    	modelService.save(orderModel);
    	modelService.refresh(orderModel);
    	if(!CRMOrderType.GIFT.equals(orderModel.getCrmOrderType()) && null!= orderModel.getRetailer()){
    		updateDealerRetailerMapping(orderModel);
    	}
    }

    private void updateDealerRetailerMapping(final OrderModel orderModel) {
        UpdateDealerRetailerMappingEvent event = new UpdateDealerRetailerMappingEvent(orderModel);
        eventService.publishEvent(event);
        if(LOG.isDebugEnabled()){
            LOG.debug("Published UpdateDealerRetailerMapping for order: "+ event.getOrder().getCode());

     }
	}
    
    protected double calculateTotalPrice(OrderModel orderModel)
    {
    	List<AbstractOrderEntryModel> entries = orderModel.getEntries();
    	Double totalPrice=0.0;
    	for(AbstractOrderEntryModel entry : entries)
    	{
    		entry.setTotalPrice(0.0);
    		entry.setBasePrice(0.0);
    		((OrderEntryModel)entry).setStatus(OrderStatus.ORDER_RECEIVED);
    		entry.setLatestStatusUpdate(new Date());
    		modelService.save(entry);
    		modelService.refresh(entry);
    	}
    	return totalPrice;
    }
    
    protected Authentication getAuth()
    {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    protected boolean hasRole(final String role, final Authentication auth)
    {
        if (auth != null)
        {
            for (final GrantedAuthority ga : auth.getAuthorities())
            {
                if (ga.getAuthority().equals(role))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void beforePlaceOrder(CommerceCheckoutParameter parameter) throws InvalidCartException {
        //DO Nothingparameter
    }

    @Override
    public void beforeSubmitOrder(CommerceCheckoutParameter parameter, CommerceOrderResult result) throws InvalidCartException {
        //DO Nothing
    }

    public SessionService getSessionService() {
        return sessionService;
    }

    public void setSessionService(SessionService sessionService) {
        this.sessionService = sessionService;
    }
    public ModelService getModelService() {
        return modelService;
    }

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    
    private void updateOrderEntryAddress(OrderModel order) {
    	for(AbstractOrderEntryModel entryModel : order.getEntries()) {
    		if(entryModel.getDeliveryAddress()!=null) {
    			AddressModel clonedAddress = addressService.cloneAddress(entryModel.getDeliveryAddress());
    			clonedAddress.setOwner(entryModel);
    			entryModel.setDeliveryAddress(clonedAddress);
    			modelService.saveAll(clonedAddress, entryModel);
    		}
    	}
    }
}
