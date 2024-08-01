package com.scl.integration.cpi.retryorder;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.scl.core.dao.DataConstraintDao;
import com.scl.core.enums.CreatedFromCRMorERP;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

//import com.scl.core.order.dao.SclOrderCountDao;
//import com.scl.core.order.services.SclOrderService;
import com.scl.integration.service.SclintegrationService;

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

import com.scl.integration.cpi.order.SclSapCpiOutboundOrderConversionService;
import com.scl.integration.cpi.order.SclSapCpiOutboundService;

import com.scl.core.enums.OrderType;
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

public class SclOrderProcessingRetriesJob extends AbstractJobPerformable<CronJobModel> {

    //SclOrderCountDao sclOrderCountDao;
    public static final String SCL_ORDER_RETRY_QUERY = "SCL_ORDER_RETRY_QUERY";

    @Resource
    private FlexibleSearchService flexibleSearchService;

    @Resource
    ModelService modelService;

    @Resource
    DataConstraintDao dataConstraintDao;

    private static final Logger LOG = Logger.getLogger(SclOrderProcessingRetriesJob.class);

    private SclSapCpiOutboundService sclSapCpiDefaultOutboundService;

    private SclSapCpiOutboundOrderConversionService sclSapCpiOutboundOrderConversionService;

    private BusinessProcessService businessProcessService;

    @Override
    public PerformResult perform(CronJobModel arg0) {
    	//Get the Unprocessed Orders
    	List<OrderModel> ordersList = getUnprocessedOrders();//sclOrderCountDao.getUnprocessedOrders();
    	PerformResult jobResult = new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    	//For each order then send back to CPI for re-processing
    	for (OrderModel order: ordersList) {
            try {
                if (order.getEntries() != null) {
                    List<AbstractOrderEntryModel> lineItemsToBepunhced = order.getEntries().stream()
                            .filter(entry -> ((OrderEntryModel) entry).getStatus() == null || !((OrderEntryModel) entry).getStatus().equals(OrderStatus.CANCELLED))
                            .sorted(Comparator.comparing(AbstractOrderEntryModel::getEntryNumber))
                            .collect(Collectors.toList());
                    if (lineItemsToBepunhced != null && !lineItemsToBepunhced.isEmpty()) {
                        getSclSapCpiDefaultOutboundService().sendOrder(getSclSapCpiOutboundOrderConversionService().convertOrderToSapCpiOrder(order)).subscribe(

                                // onNext
                                responseEntityMap -> {

                                	if (responseEntityMap.getStatusCode().is2xxSuccessful()) {

                                        setOrderStatus(order, ExportStatus.EXPORTED);
                                        LOG.info(String.format("The OMM order [%s] has been successfully sent to the SAP backend through SCPI!",
                                                order.getCode()));

                                    }  else {

                                    	setOrderErrorStatus(order, ExportStatus.NOTEXPORTED);
                                        LOG.error(String.format("The OMM order [%s] has not been sent to the SAP backend!",
                                                order.getCode()));

                                    }

                                    final String eventName = new StringBuilder().append(SapOrderExchangeActionConstants.ERP_ORDER_SEND_COMPLETION_EVENT).append(order.getCode()).toString();
                                    getBusinessProcessService().triggerEvent(eventName);
                                }

                                // onError
                                , error -> {

                                	setOrderErrorStatus(order, ExportStatus.NOTEXPORTED);
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

            catch(RuntimeException e){
             LOG.info("SclOrderProcessingRetriesJob exception for order :"+ order.getCode() + " msg: "+ e.getMessage());
            }
    	}

    	return jobResult;
    }

    protected void setOrderStatus(final OrderModel order, final ExportStatus exportStatus) {
        order.setExportStatus(exportStatus);
        modelService.save(order);
    }

    public SclSapCpiOutboundService getSclSapCpiDefaultOutboundService() {
        return sclSapCpiDefaultOutboundService;
    }

    public void setSclSapCpiDefaultOutboundService(SclSapCpiOutboundService sclSapCpiDefaultOutboundService) {
        this.sclSapCpiDefaultOutboundService = sclSapCpiDefaultOutboundService;
    }

    public SclSapCpiOutboundOrderConversionService getSclSapCpiOutboundOrderConversionService() {
        return sclSapCpiOutboundOrderConversionService;
    }

    public void setSclSapCpiOutboundOrderConversionService(SclSapCpiOutboundOrderConversionService sclSapCpiOutboundOrderConversionService) {
        this.sclSapCpiOutboundOrderConversionService = sclSapCpiOutboundOrderConversionService;
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
        final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {Order as o} WHERE {o.erpOrderNumber} is null and {o.status} in (?orderStatus) and {o.orderType}=?orderType and {o.isOrderSendtoErpOnce}=?isOrderSendtoErpOnce and {o.createdFromCRMorERP}=?createdFromCRMorERP ");
        builder.append(dataConstraintDao.findQueryByConstraintName(SCL_ORDER_RETRY_QUERY));

        List<OrderStatus> orderStatus = new ArrayList<OrderStatus>();
        orderStatus.add(OrderStatus.ERROR_IN_ERP);
        orderStatus.add(OrderStatus.ERROR_IN_CPI);
        OrderType orderType = OrderType.SO;
        params.put("orderStatus", orderStatus);
        params.put("orderType", orderType);
        params.put("createdFromCRMorERP", CreatedFromCRMorERP.CRM);
        params.put("isOrderSendtoErpOnce", Boolean.TRUE);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(OrderModel.class));
        query.addQueryParameters(params);

        LOG.info("SCL retries job getUnprocessedOrders query::"+query);
        final SearchResult<OrderModel> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
    }

    protected void setOrderErrorStatus(final OrderModel order, final ExportStatus exportStatus) {

        order.setExportStatus(exportStatus);
        order.setStatus(OrderStatus.ERROR_IN_CPI);
        modelService.save(order);

    }
}
