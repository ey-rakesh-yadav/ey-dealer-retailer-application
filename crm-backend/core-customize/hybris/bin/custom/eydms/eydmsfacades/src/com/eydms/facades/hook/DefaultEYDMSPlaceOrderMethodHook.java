package com.eydms.facades.hook;

import com.eydms.core.event.UpdateDealerRetailerMappingEvent;
import com.eydms.core.model.OrderRequisitionModel;

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
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.log4j.Logger;
import de.hybris.platform.classification.features.Feature;
import de.hybris.platform.classification.features.FeatureList;
import de.hybris.platform.classification.features.FeatureValue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.dao.OrderRequisitionDao;
import com.eydms.core.enums.CRMOrderType;
import com.eydms.core.enums.CustomerCategory;
import com.eydms.core.enums.RequisitionStatus;
import com.eydms.core.enums.ServiceType;
import com.eydms.core.order.services.EyDmsOrderService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;


public class DefaultEYDMSPlaceOrderMethodHook implements CommercePlaceOrderMethodHook {

    private SessionService sessionService;

    private ModelService modelService;

    private UserService userService;

    @Resource
    private EventService eventService;

    @Autowired
    ClassificationService classificationService;
    
    @Autowired
    EyDmsOrderService eydmsOrderService;
    
    @Autowired
    EnumerationService enumerationService;
    
	@Autowired
	OrderRequisitionDao orderRequisitionDao;
	
    public static final String ROLE_CUSTOMERMANAGERGROUP = "ROLE_CUSTOMERMANAGERGROUP";
    private static final String ACTING_USER_UID = "ACTING_USER_UID";

    private static final Logger LOG = Logger.getLogger(DefaultEYDMSPlaceOrderMethodHook.class);

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
		
    	if(CRMOrderType.GIFT.equals(orderModel.getCrmOrderType())) {
//    		if(orderModel.getPlacedBy()!=null && orderModel.getPlacedBy().getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.TSO_GROUP_ID))) {
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
    	}
    	
    	Collection<String> requistionsList = orderModel.getRequisitionNumberList();
		if(requistionsList!=null && !requistionsList.isEmpty()) {
			List<OrderRequisitionModel> modelList = new ArrayList<OrderRequisitionModel>();
			for(String requisitionId : requistionsList) {
				OrderRequisitionModel requisitionModel = orderRequisitionDao.findByRequisitionId(requisitionId);
				if(requisitionModel!=null) {
					requisitionModel.setOrder(orderModel);
					requisitionModel.setStatus(RequisitionStatus.PENDING_FULFILLMENT);
					requisitionModel.setAcceptedDate(new Date());
					if(requistionsList.size()>1) {
						requisitionModel.setServiceType(ServiceType.CLUBBED_PLACED);
					}
					else {
						requisitionModel.setServiceType(ServiceType.PLACED);
					}
					modelList.add(requisitionModel);
				}
			}
			modelService.saveAll(modelList);
		}
		
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
}
