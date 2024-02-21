package com.eydms.core.order.dao.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.order.dao.OrderValidationProcessDao;
import com.eydms.core.utility.EyDmsDateUtility;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

public class OrderValidationProcessDaoImpl implements OrderValidationProcessDao
{
	private static final String DEFAULT_QUERY ="SELECT {w.code},{w.name},{w.maxQuantity}  FROM {DestinationSourceMaster as dsm JOIN Warehouse as w on {dsm.source}={w.pk} } WHERE {destinationCity}=?erpCity AND {sourcePriority}='L1'";
	private static final String DEFAULT_QUERY_MAX_QUANTITY ="SELECT {maxQuantity} FROM {DestinationSourceMaster} WHERE {source} = ?source";
	private static final String DEFAULT_QUERY_PENDING_AMOUNT ="SELECT SUM({oe.totalPrice}) FROM {OrderEntry AS oe JOIN Order AS o ON {o.pk}={oe.order} } WHERE {o.user}=?userId AND ({o.erpOrderNumber} is not null or {o.orderValidatedDate} is not null or {o.orderSentForApprovalDate} is not null) AND {oe.invoiceCreationDateAndTime} is null AND {oe.cancelledDate} is null ";
	@Autowired
	private FlexibleSearchService flexibleSearchService;
	private static final Logger LOG = Logger.getLogger(OrderValidationProcessDaoImpl.class);
	
	@Override
	public List<String> getOptimalSource(OrderModel order) {
		
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder(DEFAULT_QUERY);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		
		try {
			final String erpCity = order.getDeliveryAddress().getErpCity();
			params.put("erpCity", erpCity);
			query.addQueryParameters(params);
		}catch(NullPointerException e)
		{
			LOG.debug(e);
		}
		
		query.setResultClassList(Arrays.asList(String.class));
		
		final SearchResult<List<String>> searchResult = getFlexibleSearchService().search(query);
		List<List<String>>result = searchResult.getResult();
		return result.get(0);

	}

	public FlexibleSearchService getFlexibleSearchService() {
		return flexibleSearchService;
	}

	public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
		this.flexibleSearchService = flexibleSearchService;
	}

	@Override
	public Double getMaxQuantity(OrderModel order) {
		final StringBuilder builder = new StringBuilder(DEFAULT_QUERY_MAX_QUANTITY);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		final Map<String, Object> params = new HashMap<String, Object>();
		String source = order.getWarehouse().getPk().toString();
		params.put("source", source);
		query.addQueryParameters(params);
		query.setResultClassList(Arrays.asList(Double.class));
		final SearchResult<Double> searchResult = getFlexibleSearchService().search(query);
		List<Double>result = searchResult.getResult();
		
		return result.get(0);
	}

	@Override
	public Double getPendingOrderAmount(String userId) {
		final StringBuilder builder = new StringBuilder(DEFAULT_QUERY_PENDING_AMOUNT);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", userId);
		query.addQueryParameters(params);
		query.setResultClassList(Arrays.asList(Double.class));
		
		final SearchResult<Double > searchResult = getFlexibleSearchService().search(query);
		List<Double> result = searchResult.getResult();
		
		if(result!=null && !result.isEmpty())
			return result.get(0)!=null ? result.get(0) : 0.0;
		
		return 0.0;
	}
	
	@Override
	public Double getMaxQuantityThreshold(BaseSiteModel site, EyDmsCustomerModel customer, ProductModel product) {
		final Map<String, Object> params = new HashMap<String, Object>();

		final StringBuilder builder = new StringBuilder("");
		builder.append("SELECT MAX(INNERTABLE.quantity) FROM ({{ ")
		.append("select {oe.invoiceCreationDateAndTime} as invoiceDate ,sum({oe.quantityInMT}) as quantity ")
		.append("from {Order as o join OrderEntry as oe on {oe.order}={o.pk}} ")
		.append("where {o.site}=?site and  {o.user}=?customer and {oe.product}=?product and  {oe.cancelledDate} is null and ")
		.append(EyDmsDateUtility.getLastXDayQuery("oe.invoiceCreationDateAndTime", params, 90))
		.append("  group by {oe.invoiceCreationDateAndTime} ")
		.append("}}) INNERTABLE");
		
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		params.put("site", site);
		params.put("customer", customer);
		params.put("product", product);
		query.addQueryParameters(params);
		query.setResultClassList(Arrays.asList(Double.class));
		
		final SearchResult<Double > searchResult = getFlexibleSearchService().search(query);
		if(searchResult.getResult()!=null) {
			List<Double> result = searchResult.getResult();
			if(result!=null && !result.isEmpty())
				return result.get(0)!=null ? result.get(0) : 0.0;
			else return 0.0;
		}		
		return 0.0;
	}

	
	@Override
	public Double getQtyForExpectedDeliveredDate(BaseSiteModel site, EyDmsCustomerModel customer, ProductModel product, Date expectedDeliveryDate) {
		final Map<String, Object> params = new HashMap<String, Object>();

		LocalDate date =expectedDeliveryDate.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDate();

		final StringBuilder builder = new StringBuilder("");
		builder.append("select sum({oe.quantityInMT}) ")
		.append("from {Order as o join OrderEntry as oe on {oe.order}={o.pk}} ")
		.append("where {o.site}=?site and  {o.user}=?customer and {oe.product}=?product ")
		.append("and  {oe.expectedDeliveryDate} >=?starteDate and {oe.expectedDeliveryDate} <?endDate ")
		.append("and ({o.orderValidatedDate} is not null or {o.orderSentForApprovalDate} is not null)  and  {oe.cancelledDate} is null ");

		params.put("site", site);
		params.put("customer", customer);
		params.put("product", product);
		params.put("starteDate", date.toString());
		params.put("endDate", date.plusDays(1).toString());

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Arrays.asList(Double.class));

		final SearchResult<Double > searchResult = getFlexibleSearchService().search(query);
		if(searchResult.getResult()!=null) {
			List<Double> result = searchResult.getResult();
			if(result!=null && !result.isEmpty())
				return result.get(0)!=null ? result.get(0) : 0.0;
			else return 0.0;
		}
		return 0.0;
	}
}
