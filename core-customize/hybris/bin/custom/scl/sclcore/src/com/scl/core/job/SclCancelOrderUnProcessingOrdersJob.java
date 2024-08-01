package com.scl.core.job;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.scl.core.dao.DataConstraintDao;
import com.scl.core.enums.CreatedFromCRMorERP;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.order.SCLB2BOrderService;

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

public class SclCancelOrderUnProcessingOrdersJob extends AbstractJobPerformable<CronJobModel> {

	private static final Logger LOG = Logger.getLogger(SclCancelOrderUnProcessingOrdersJob.class);
	private static String REASON = "Unprocessed Order cancelled automatically";
	public static final String FIND_UNPROCESSORDER_DAYS = "FIND_UNPROCESSORDER_DAYS";

	@Resource
	private FlexibleSearchService flexibleSearchService;
	
	@Autowired
	SCLB2BOrderService sclB2BOrderService;

	@Resource
	DataConstraintDao dataConstraintDao;


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
		List<OrderModel> orderList = getUnprocessedOrders(site);
		for (OrderModel orderModel: orderList) {
			try {
				sclB2BOrderService.cancelOrderFromCrm(orderModel, orderModel.getErpStatusDesc(), null, true);
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
		LocalDate startDate = LocalDate.now().minusDays(dataConstraintDao.findDaysByConstraintName(FIND_UNPROCESSORDER_DAYS));
		final StringBuilder builder = new StringBuilder("SELECT {o.pk} FROM {Order as o} WHERE  {o.erpOrderNumber} is null and {o.status} in (?orderStatus)  and {o.createdFromCRMorERP}=?createdFromCRMorERP and {o.creationTime}>=?startDate  and {o.creationTime}<?endDate");
		List<OrderStatus> orderStatus = new ArrayList<OrderStatus>();
		orderStatus.add(OrderStatus.ERROR_IN_ERP);
		orderStatus.add(OrderStatus.ERROR_IN_CPI);

		params.put("createdFromCRMorERP", CreatedFromCRMorERP.CRM);
		params.put("orderStatus", orderStatus);
		params.put("startDate", startDate.toString());
		params.put("endDate", endDate.toString());
		
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(OrderModel.class));
		query.addQueryParameters(params);
		
		LOG.info("SCL cancel unprocessed order job query::"+query);
		
		final SearchResult<OrderModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
	}
}
