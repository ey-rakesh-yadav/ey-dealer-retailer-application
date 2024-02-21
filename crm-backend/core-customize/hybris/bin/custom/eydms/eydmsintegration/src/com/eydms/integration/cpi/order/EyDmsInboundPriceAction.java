package com.eydms.integration.cpi.order;

import static com.google.common.base.Preconditions.checkArgument;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.eydms.core.enums.OrderType;
import com.eydms.core.model.EyDmsOutboundPriceModel;

import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.task.RetryLaterException;


public class EyDmsInboundPriceAction extends AbstractSimpleDecisionAction<OrderProcessModel> {

    private static final Logger LOG = Logger.getLogger(EyDmsInboundPriceAction.class);

    private EyDmsSapCpiOutboundService eydmsSapCpiDefaultOutboundService;
    
    @Autowired
    ModelService modelService;
    
    @Autowired
    BusinessProcessService businessProcessService;
    @Override
    public Transition executeAction(OrderProcessModel process) throws RetryLaterException {

        final OrderModel order = process.getOrder();

        if (order.getOrderType().equals(OrderType.SO)) {
        	executeEyDmsOutboundPrice(process, order, true);
         }
        return Transition.OK;
    }
    
    public boolean executeEyDmsOutboundPrice(OrderProcessModel process, OrderModel order, boolean orderCreation) {
    	EyDmsOutboundPriceModel outboundPrice = new EyDmsOutboundPriceModel();
    	if(order.getEntries()!=null && !order.getEntries().isEmpty() & order.getSite()!=null && order.getEntries().get(0).getProduct()!=null && order.getDeliveryAddress()!=null && order.getEntries().get(0).getSource()!=null) {
    		outboundPrice.setProduct(order.getEntries().get(0).getProduct().getGrade());
    		outboundPrice.setPacking_type(order.getEntries().get(0).getProduct().getBagType());
    		outboundPrice.setBill_date(LocalDate.now().toString());
    		outboundPrice.setOrg_id(order.getSite().getUid());
    		outboundPrice.setOrganization_id(order.getEntries().get(0).getSource().getOrganisationId());
    		outboundPrice.setCity(order.getDeliveryAddress().getErpCity());
    		outboundPrice.setTaluka(order.getDeliveryAddress().getTaluka());
    		outboundPrice.setDistrict(order.getDeliveryAddress().getDistrict());
    		outboundPrice.setState(order.getDeliveryAddress().getState());
    		final PriceWrapper pWrapper = new PriceWrapper();
    		pWrapper.responseRecieved = false;
    		pWrapper.apiCallCount = 0;
    		callEyDmsOutboundPrice(outboundPrice, order, process, pWrapper, orderCreation);
    	}
		return true;     
    }
    
    private void callEyDmsOutboundPrice(EyDmsOutboundPriceModel outboundPrice, OrderModel order, OrderProcessModel process
    		, PriceWrapper pWrapper, boolean orderCreation) {
    	pWrapper.apiCallCount++;
       	LOG.error("Price api call count " + pWrapper.apiCallCount + " order - Test" + order.getCode() );
    	getEyDmsSapCpiDefaultOutboundService().getPrice(outboundPrice).subscribe(

    			// onNext
    			responseEntityMap -> {
    				
    				String response = getPropertyValue(responseEntityMap, "GET_EFFECTIVE_ORDER_VALUE");
    				if(StringUtils.isNotBlank(response)) {
    					LOG.error("Price api call response Test" + order.getCode() + " : " + response);
    					String[] arr = response.split(",");

    					Double priceValue=0.0;
    					String status =null;
    					if(arr.length>0 && arr[0]!=null && !arr[0].isBlank()) {
    						priceValue = Double.valueOf(arr[0]);
    					}
    					if(arr.length>1 && arr[1]!=null && !arr[1].isBlank()) {
    						status = arr[1];
    					}
    					if(status!=null && status.equalsIgnoreCase("Y")) {

    						Double basePrice = priceValue;

    						order.getEntries().forEach(entry -> {
    							entry.setBasePrice(basePrice);
    							entry.setTotalPrice(basePrice * entry.getQuantityInMT());
    							modelService.save(entry);
    						});
    						order.setTotalPrice(order.getEntries().stream().collect(Collectors.summingDouble(AbstractOrderEntryModel::getTotalPrice)));
    						order.setPriceSlctStatus(status);
    						modelService.save(order);
    						modelService.refresh(order);
//    						if(!orderCreation && process!=null && process.getCurrentTasks()!=null && !process.getCurrentTasks().isEmpty() 
//    								&& process.getCurrentTasks().iterator().next().getAction().equals("waitForOrderReviewDecision")) {
//    							LOG.error("Price api call triggered order process order review for order Test" + order.getCode());
//    							businessProcessService.triggerEvent(process.getCode()+"_ORDER_REVIEW_DECISION");
//    						}
//    						else 
    						if(orderCreation) {
    							LOG.error("Price api call triggered order process price action for order Test" + order.getCode());
    							businessProcessService.triggerEvent(process.getCode()+"_WAIT_PRICE_ACTION");
    						
       						}
    					}
    					else {
    						order.setPriceSlctStatus(status);
    						modelService.save(order);
    						modelService.refresh(order);
    						//updatePriceNotFoundStatus(order);
    						if(orderCreation) {
    							LOG.error("Price api call triggered order process price action for order Test" + order.getCode());
    							businessProcessService.triggerEvent(process.getCode()+"_WAIT_PRICE_ACTION");
    						
       						}
    					}
    					pWrapper.responseRecieved =true;
    				}
    				else {
    					LOG.error("Price api call empty response Test" + order.getCode());
    					if(pWrapper.apiCallCount<10 && pWrapper.responseRecieved==false) {
    						callEyDmsOutboundPrice(outboundPrice, order, process, pWrapper, orderCreation);
    					}
    					else {
    						//updatePriceNotFoundStatus(order);
    						if(orderCreation) {
    							LOG.error("Price api call triggered order process price action for order Test" + order.getCode());
    							businessProcessService.triggerEvent(process.getCode()+"_WAIT_PRICE_ACTION");
    						
       						}
    					}    					
    				}

    			}

    			// onError
    			, error -> {
    				LOG.error("Price api call error Test" + order.getCode());
    				if(pWrapper.apiCallCount<10 && pWrapper.responseRecieved==false) {
						callEyDmsOutboundPrice(outboundPrice, order, process, pWrapper, orderCreation);
					}
					else {
						//updatePriceNotFoundStatus(order);
						if(orderCreation) {
							LOG.error("Price api call triggered order process price action for order Test" + order.getCode());
							businessProcessService.triggerEvent(process.getCode()+"_WAIT_PRICE_ACTION");
						
   						}
					}
    			}

    			);
    }
    
    private void updatePriceNotFoundStatus(OrderModel order) {
//    	if(order.getStatus()!=null && order.getStatus().equals(OrderStatus.ORDER_RECEIVED)) {
//    		order.getEntries().forEach(entry -> {
//    			((OrderEntryModel)entry).setStatus(OrderStatus.PRICE_NOT_FOUND);
//    			modelService.save(entry);
//    		});
//    		order.setStatus(OrderStatus.PRICE_NOT_FOUND);
//    		modelService.save(order);
//    		modelService.refresh(order);
//    		LOG.error("Price api call status update to Price Not Found");
//    	}
    }

    public EyDmsSapCpiOutboundService getEyDmsSapCpiDefaultOutboundService() {
        return eydmsSapCpiDefaultOutboundService;
    }

    public void setEyDmsSapCpiDefaultOutboundService(EyDmsSapCpiOutboundService eydmsSapCpiDefaultOutboundService) {
        this.eydmsSapCpiDefaultOutboundService = eydmsSapCpiDefaultOutboundService;
    }
    
	static String getPropertyValue(ResponseEntity<Map> responseEntityMap, String property)
	{
		if (responseEntityMap.getBody() != null)
		{
			Object next = responseEntityMap.getBody().keySet().iterator().next();
			checkArgument(next != null,
					String.format("SCPI response entity key set cannot be null for property [%s]!", property));

			String responseKey = next.toString();
			checkArgument(responseKey != null && !responseKey.isEmpty(),
					String.format("SCPI response property can neither be null nor empty for property [%s]!", property));

			Object propertyValue = responseEntityMap.getBody().get(responseKey);
			//checkArgument(propertyValue != null, String.format("SCPI response property [%s] value cannot be null!", property));

			return propertyValue.toString();
		}
		else
		{
			return null;
		}
	}
	
	 public class PriceWrapper {
		    public int apiCallCount;
		    public boolean responseRecieved;
	 }
}
