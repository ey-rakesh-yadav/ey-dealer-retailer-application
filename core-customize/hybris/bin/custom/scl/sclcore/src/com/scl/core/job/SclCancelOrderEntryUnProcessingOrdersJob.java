package com.scl.core.job;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.hybris.yprofile.dto.Order;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.order.SCLB2BOrderService;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.site.BaseSiteService;

public class SclCancelOrderEntryUnProcessingOrdersJob extends AbstractJobPerformable<CronJobModel> {

	private static final Logger LOG = Logger.getLogger(SclCancelOrderEntryUnProcessingOrdersJob.class);
	private static String REASON = "Unprocessed Order Entry cancelled automatically";

	@Autowired
	private FlexibleSearchService flexibleSearchService;
	
	@Autowired
	SCLB2BOrderService sclB2BOrderService;
	
	@Autowired
	BaseSiteService baseSiteService;
	
	@Override
	public PerformResult perform(CronJobModel arg0) {
		PerformResult jobResult = new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
		executeJob(SclCoreConstants.SITE.SCL_SITE);
		return jobResult;
	}
	
	private void executeJob(String baseSiteId) {
		BaseSiteModel site = baseSiteService.getBaseSiteForUID(baseSiteId);
		List<OrderEntryModel> orderEntryList = getUnprocessedOrderEntries(site);
		for (OrderEntryModel orderEntryModel: orderEntryList) {
			sclB2BOrderService.cancelOrderEntryFromCRM(orderEntryModel, REASON, null, true);
			sclB2BOrderService.getRequisitionStatusByOrderLines(orderEntryModel);
		}
	}
	private List<OrderEntryModel> getUnprocessedOrderEntries(BaseSiteModel site) {
		final Map<String, Object> params = new HashMap<String, Object>();
		
		Integer thresholdDay = site.getUprocessedOrderEntryCancelDayThreshold();
//		Double thresholdQty = site.getUprocessedOrderEntryCancelQtyThreshold();
		LocalDate endDate = LocalDate.now().minusDays(thresholdDay);
		LocalDate startDate = LocalDate.now().minusDays(365);

		final StringBuilder builder = new StringBuilder("SELECT {oe.pk} FROM {Order as o join OrderEntry as oe on {oe.order}={o.pk} } WHERE {o.site}=?site  and {oe.status} in (?orderStatus) and {oe.creationTime}>=?startDate and {oe.creationTime}<?endDate ") ;

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
//		params.put("thresholdQty", thresholdQty);
		
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(OrderEntryModel.class));
		query.addQueryParameters(params);
		final SearchResult<OrderEntryModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
	}
}
