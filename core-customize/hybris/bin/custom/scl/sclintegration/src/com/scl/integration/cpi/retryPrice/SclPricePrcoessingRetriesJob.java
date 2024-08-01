package com.scl.integration.cpi.retryPrice;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import com.scl.core.enums.OrderType;
import com.scl.integration.cpi.order.SclInboundPriceAction;

import de.hybris.platform.core.enums.ExportStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

public class SclPricePrcoessingRetriesJob extends AbstractJobPerformable<CronJobModel> {

    @Resource
    private FlexibleSearchService flexibleSearchService;

    @Resource
    ModelService modelService;

    private static final Logger LOG = Logger.getLogger(SclPricePrcoessingRetriesJob.class);

    @Autowired
    SclInboundPriceAction sclOutboundPriceAction;

    @Override
    public PerformResult perform(CronJobModel arg0) {
        List<OrderModel> ordersList = getUnprocessedOrders();
        PerformResult jobResult = new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
        for (OrderModel order: ordersList) {
        	LOG.error("PRICE_RETRY_" + order.getCode());
            OrderProcessModel orderProcessModel = null;
            if(order.getOrderProcess()!=null) {
                Optional<OrderProcessModel> orderProcess = order.getOrderProcess().stream().filter(op -> op instanceof OrderProcessModel).findFirst();
                if(orderProcess.isPresent()){
                    orderProcessModel = orderProcess.get();
                }
            }
            sclOutboundPriceAction.executeSclOutboundPrice(orderProcessModel, order, false);
        }
        return jobResult;
    }

    protected void setOrderStatus(final OrderModel order, final ExportStatus exportStatus) {
        order.setExportStatus(exportStatus);
        modelService.save(order);
    }


    //@Override
    private List<OrderModel> getUnprocessedOrders() {
        final Map<String, Object> params = new HashMap<String, Object>();
        String startDate = LocalDate.now().minusDays(15).toString();
        //String endDate = LocalDate.now().plusDays(1).toString();
        Date endDate = new Date(System.currentTimeMillis() - 900 * 1000);
        LOG.error("PRICE_RETRY " + startDate);
        LOG.error("PRICE_RETRY " + endDate.toString());
        final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {Order as o} WHERE {o.priceSlctStatus} is null and {o.totalPrice}=0 and {o.cancelledDate} is null and {o.orderType}=?orderType and {o.date} >= ?start and {o.date} < ?end ");

        OrderType orderType = OrderType.SO;
        params.put("orderType", orderType);
        params.put("start", startDate);
        params.put("end", endDate);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(OrderModel.class));
        query.addQueryParameters(params);
        final SearchResult<OrderModel> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
    }
}