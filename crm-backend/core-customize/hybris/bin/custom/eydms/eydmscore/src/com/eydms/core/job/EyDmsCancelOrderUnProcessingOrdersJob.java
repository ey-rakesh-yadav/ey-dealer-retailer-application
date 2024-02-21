package com.eydms.core.job;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.order.EYDMSB2BOrderService;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.site.BaseSiteService;

public class EyDmsCancelOrderUnProcessingOrdersJob extends AbstractJobPerformable<CronJobModel> {

	private static final Logger LOG = Logger.getLogger(EyDmsCancelOrderUnProcessingOrdersJob.class);
	private static String REASON = "Unprocessed Order cancelled automatically";

	@Resource
	private FlexibleSearchService flexibleSearchService;
	
	@Autowired
	EYDMSB2BOrderService eydmsB2BOrderService;
	
	@Autowired
	BaseSiteService baseSiteService;
	
	@Override
	public PerformResult perform(CronJobModel arg0) {
		PerformResult jobResult = new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);

		executeJob(EyDmsCoreConstants.SITE.SHREE_SITE);
		executeJob(EyDmsCoreConstants.SITE.BANGUR_SITE);
		executeJob(EyDmsCoreConstants.SITE.ROCKSTRONG_SITE);
		return jobResult;
	}
	
	private void executeJob(String baseSiteId) {
		BaseSiteModel site = baseSiteService.getBaseSiteForUID(baseSiteId);
		List<OrderModel> orderList = getUnprocessedOrders(site);
		for (OrderModel orderModel: orderList) {
			try {
				eydmsB2BOrderService.cancelOrderFromCrm(orderModel, REASON, null, true);
			}
			catch (Exception e) {
				LOG.info("Exception caught but ignoring it"+e.getMessage());
				continue;
			}

		}
	}

	private List<OrderModel> getUnprocessedOrders(BaseSiteModel site) {
		final Map<String, Object> params = new HashMap<String, Object>();
		
		Integer thresholdDay = site.getUprocessedOrderCancelDayThreshold();
		LocalDate endDate = LocalDate.now().minusDays(thresholdDay);
		LocalDate startDate = LocalDate.now().minusDays(365);
		
		final StringBuilder builder = new StringBuilder("SELECT {o.pk} FROM {Order as o} WHERE {o.site}=?site and {o.erpOrderNumber} is null and {o.status} in (?orderStatus) and {o.creationTime}>=?startDate and {o.creationTime}<?endDate  ");
		List<OrderStatus> orderStatus = new ArrayList<OrderStatus>();
		orderStatus.add(OrderStatus.ORDER_RECEIVED);
		orderStatus.add(OrderStatus.ORDER_FAILED_VALIDATION);
		orderStatus.add(OrderStatus.PROCESSING_ERROR);
		orderStatus.add(OrderStatus.ORDER_MODIFIED);
		orderStatus.add(OrderStatus.ERROR_IN_ERP);
		orderStatus.add(OrderStatus.APPROVED);
		orderStatus.add(OrderStatus.ORDER_VALIDATED);

		params.put("orderStatus", orderStatus);
		params.put("site", site);
		params.put("startDate", startDate.toString());
		params.put("endDate", endDate.toString());
		
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(OrderModel.class));
		query.addQueryParameters(params);
		final SearchResult<OrderModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
	}
}
