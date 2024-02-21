package com.eydms.integration.cpi.retryorder;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

//import com.eydms.core.order.dao.EyDmsOrderCountDao;
//import com.eydms.core.order.services.EyDmsOrderService;
import com.eydms.integration.service.EyDmsintegrationService;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import com.eydms.integration.cpi.order.EyDmsSapCpiOutboundOrderConversionService;
import com.eydms.integration.cpi.order.EyDmsSapCpiOutboundService;

import com.eydms.core.enums.OrderType;
import de.hybris.platform.core.enums.ExportStatus;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.sap.orderexchange.constants.SapOrderExchangeActionConstants;
import de.hybris.platform.sap.sapcpiadapter.service.SapCpiOutboundService;
import de.hybris.platform.sap.sapcpiorderexchange.actions.SapCpiOmmOrderOutboundAction;
import de.hybris.platform.sap.sapcpiorderexchange.service.SapCpiOrderOutboundConversionService;
import de.hybris.platform.task.RetryLaterException;
import de.hybris.platform.processengine.BusinessProcessService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import static de.hybris.platform.sap.sapcpiadapter.service.SapCpiOutboundService.*;

public class EyDmsOrderProcessingRetriesJob extends AbstractJobPerformable<CronJobModel> {

    //EyDmsOrderCountDao eydmsOrderCountDao;

    @Resource
    private FlexibleSearchService flexibleSearchService;

    @Resource
    ModelService modelService;

    private static final Logger LOG = Logger.getLogger(EyDmsOrderProcessingRetriesJob.class);

    private EyDmsSapCpiOutboundService eydmsSapCpiDefaultOutboundService;

    private EyDmsSapCpiOutboundOrderConversionService eydmsSapCpiOutboundOrderConversionService;

    private BusinessProcessService businessProcessService;

    @Override
    public PerformResult perform(CronJobModel arg0) {
    	//Get the Unprocessed Orders
    	List<OrderModel> ordersList = getUnprocessedOrders();//eydmsOrderCountDao.getUnprocessedOrders();
    	PerformResult jobResult = new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    	//For each order then send back to CPI for re-processing
    	for (OrderModel order: ordersList) {
    		if(order.getEntries()!=null) {
    			List<AbstractOrderEntryModel> lineItemsToBepunhced = order.getEntries().stream()
    					.filter(entry->((OrderEntryModel)entry).getStatus()==null || !((OrderEntryModel)entry).getStatus().equals(OrderStatus.CANCELLED))
    					.sorted(Comparator.comparing(AbstractOrderEntryModel::getEntryNumber))
    					.collect(Collectors.toList());
    			if(lineItemsToBepunhced!=null && !lineItemsToBepunhced.isEmpty()) {
    				getEyDmsSapCpiDefaultOutboundService().sendOrder(getEyDmsSapCpiOutboundOrderConversionService().convertOrderToSapCpiOrder(order)).subscribe(

    						// onNext
    						responseEntityMap -> {

    							if (isSentSuccessfully(responseEntityMap)) {
    								setOrderStatus(order, ExportStatus.EXPORTED);
    								LOG.info(String.format("The OMM order [%s] has been successfully sent to the SAP backend through SCPI! %n%s",
    										order.getCode(), getPropertyValue(responseEntityMap, RESPONSE_MESSAGE)));
    							} else {
    								setOrderStatus(order, ExportStatus.NOTEXPORTED);
    								LOG.error(String.format("The OMM order [%s] has not been sent to the SAP backend! %n%s",
    										order.getCode(), getPropertyValue(responseEntityMap, RESPONSE_MESSAGE)));
    							}

    							final String eventName = new StringBuilder().append(SapOrderExchangeActionConstants.ERP_ORDER_SEND_COMPLETION_EVENT).append(order.getCode()).toString();
    							getBusinessProcessService().triggerEvent(eventName);
    						}

    						// onError
    						, error -> {

    							setOrderStatus(order, ExportStatus.NOTEXPORTED);
    							LOG.error(String.format("The OMM order [%s] has not been sent to the SAP backend through SCPI! %n%s", order.getCode(), error.getMessage()), error);

    							final String eventName = new StringBuilder().append(SapOrderExchangeActionConstants.ERP_ORDER_SEND_COMPLETION_EVENT).append(order.getCode()).toString();
    							getBusinessProcessService().triggerEvent(eventName);
    						}

    						);
    	            order.setOrderSendtoErpDate(new Date());
    	            order.setIsOrderPickedByCronJob(true);
    	            modelService.save(order);
    	            modelService.refresh(order);
    			}
    		}

    	}

    	return jobResult;
    }

    protected void setOrderStatus(final OrderModel order, final ExportStatus exportStatus) {
        order.setExportStatus(exportStatus);
        modelService.save(order);
    }

    public EyDmsSapCpiOutboundService getEyDmsSapCpiDefaultOutboundService() {
        return eydmsSapCpiDefaultOutboundService;
    }

    public void setEyDmsSapCpiDefaultOutboundService(EyDmsSapCpiOutboundService eydmsSapCpiDefaultOutboundService) {
        this.eydmsSapCpiDefaultOutboundService = eydmsSapCpiDefaultOutboundService;
    }

    public EyDmsSapCpiOutboundOrderConversionService getEyDmsSapCpiOutboundOrderConversionService() {
        return eydmsSapCpiOutboundOrderConversionService;
    }

    public void setEyDmsSapCpiOutboundOrderConversionService(EyDmsSapCpiOutboundOrderConversionService eydmsSapCpiOutboundOrderConversionService) {
        this.eydmsSapCpiOutboundOrderConversionService = eydmsSapCpiOutboundOrderConversionService;
    }

    protected BusinessProcessService getBusinessProcessService() {
        return businessProcessService;
    }

    @Required
    public void setBusinessProcessService(BusinessProcessService businessProcessService) {
        this.businessProcessService = businessProcessService;
    }

    //@Override
    private List<OrderModel> getUnprocessedOrders() {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {Order as o} WHERE {o.erpOrderNumber} is null and {o.status} in (?orderStatus) and {o.orderType}=?orderType and {o.isOrderSendtoErpOnce}=?isOrderSendtoErpOnce ");

        List<OrderStatus> orderStatus = new ArrayList<OrderStatus>();
        orderStatus.add(OrderStatus.ORDER_VALIDATED);
        orderStatus.add(OrderStatus.APPROVED);
        orderStatus.add(OrderStatus.ERROR_IN_ERP);
        OrderType orderType = OrderType.SO;
        params.put("orderStatus", orderStatus);
        params.put("orderType", orderType);
        params.put("isOrderSendtoErpOnce", Boolean.TRUE);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(OrderModel.class));
        query.addQueryParameters(params);
        final SearchResult<OrderModel> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
    }
}
