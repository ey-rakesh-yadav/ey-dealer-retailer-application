package com.eydms.core.orderhistory.dao.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.eydms.core.enums.OrderType;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.enums.WarehouseType;
import com.eydms.core.orderhistory.dao.OrderHistoryDao;
import com.eydms.core.utility.EyDmsDateUtility;
import com.eydms.facades.orderhistory.data.DispatchDetailsData;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;
import de.hybris.platform.site.BaseSiteService;

public class OrderHistoryDaoImpl implements OrderHistoryDao
{
	
	private FlexibleSearchService flexibleSearchService;
	
	@Autowired 
	BaseSiteService baseSiteService;
	
	@Autowired
	PaginatedFlexibleSearchService paginatedFlexibleSearchService;
	
	private static final String QUERY ="SELECT COUNT({oe.entryNumber}), SUM({oe.quantityInMT}) FROM { OrderEntry AS oe JOIN Order AS o ON {o.pk} = {oe.order} JOIN Warehouse AS w ON {oe.source}={w.pk}} WHERE ";
	private static final String OPP_QUERY ="SELECT COUNT({oe.entryNumber}) FROM { OrderEntry AS oe JOIN Order AS o ON {o.pk} = {oe.order} JOIN Warehouse AS w ON {oe.source}={w.pk}} WHERE ";

	private static final Logger LOGGER = Logger.getLogger(OrderHistoryDaoImpl.class);
	
	@Override
	public Map<String, Object> getDispatchDetails(String sourceType, String date, UserModel user) {

		WarehouseType type = null;
		WarehouseType oppType = null;

		if(sourceType.equals("Primary"))
		{
			type=WarehouseType.PLANT;
			oppType=WarehouseType.DEPOT;
		}
		else if(sourceType.equals("Secondary"))
		{
			type=WarehouseType.DEPOT;
			oppType=WarehouseType.PLANT;
		}

		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder(QUERY)
				.append(EyDmsDateUtility.getDateRangeClauseQuery("oe.truckDispatcheddate", date, date, params))
				.append(" and {w.type}=?type ");

		params.put("type", type);
		
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Integer.class, Long.class));
		query.addQueryParameters(params);
/////////////////////////////
		final Map<String, Object> oppParams = new HashMap<String, Object>();
		final StringBuilder oppBuilder = new StringBuilder(OPP_QUERY)
				.append(EyDmsDateUtility.getDateRangeClauseQuery("oe.truckDispatcheddate", date, date, oppParams))
				.append(" and {w.type}=?type ");

		oppParams.put("type", oppType);
		
		FlexibleSearchQuery oppQuery = new FlexibleSearchQuery(oppBuilder.toString());
		oppQuery.setResultClassList(Arrays.asList(Integer.class));
		oppQuery.addQueryParameters(oppParams);
		final SearchResult<Integer> oppSearchResult = getFlexibleSearchService().search(oppQuery);
		Integer oppCount = oppSearchResult.getResult()!=null 
				&& !oppSearchResult.getResult().isEmpty() 
				&& oppSearchResult.getResult().get(0)!=null?oppSearchResult.getResult().get(0):0;
		try {
			final SearchResult<List<Object>> searchResult = getFlexibleSearchService().search(query);
			List<List<Object>> result = searchResult.getResult();

			Map<String,Object> dispatchDetails = new HashMap<String, Object>();
			int count = 0;  long sum =0;
			if(result.get(0)!=null && !result.get(0).isEmpty()) {
				if(result.get(0).size()>0 && result.get(0).get(0)!=null)
					count = (int) result.get(0).get(0);
				if(result.get(0).size()>1 && result.get(0).get(1)!=null)
					sum = (long) result.get(0).get(1);
			}
			int total = count + oppCount;
			dispatchDetails.put("totalCount", total);
			dispatchDetails.put("oppositeCount", oppCount);
			dispatchDetails.put("count", count);
			dispatchDetails.put("sum", sum);
			
			return dispatchDetails;
		}
		catch(Exception e)
		{
			LOGGER.error(e);
			return null;
		}
	}
	
	@Override
	public SearchPageData<OrderEntryModel> getTradeOrderListing(SearchPageData searchPageData, String sourceType, UserModel user, Date startDate, Date endDate, BaseSiteModel site, String filter, String productName, OrderType orderType, String status)
	{
		WarehouseType type = null;
		
		if(Objects.nonNull(sourceType))
		{
			if(sourceType.equals("Primary"))
			{
				type=WarehouseType.PLANT;
			}
			else if(sourceType.equals("Secondary"))
			{
				type=WarehouseType.DEPOT;
			}
		}

		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {oe.pk} FROM { OrderEntry AS oe JOIN Order AS o ON {o.pk}={oe.order} ");
		if(Objects.nonNull(type))
		{
			builder.append("JOIN Warehouse AS w ON {w.pk}={oe.source} ");
		}
		if(Objects.nonNull(filter))
		{
			builder.append(" JOIN EyDmsCustomer AS u ON {u.pk}={o.user} ");
		}
		builder.append("} WHERE   ");
		if(Objects.nonNull(status) && status.equals(OrderStatus.TRUCK_DISPATCHED.getCode()))
		{
			builder.append(" {oe.truckDispatcheddate} BETWEEN ?startDate AND ?endDate ");
		}
		else {
			builder.append(" {oe.deliveredDate} BETWEEN ?startDate AND ?endDate ");	
		}

		if(Objects.nonNull(type))
		{
			builder.append("AND {w.type}=?type ");
			params.put("type", type);	
		}
		if(Objects.nonNull(filter))
		{
			builder.append("AND (UPPER({o.code}) LIKE ?filter OR UPPER({u.name}) LIKE ?filter OR UPPER({u.uid}) LIKE ?filter OR UPPER({u.customerNo}) LIKE ?filter) ");
			params.put("filter", "%"+filter.toUpperCase()+"%");
		}
		if(null!= orderType){
			builder.append(" AND {o.orderType} = ?orderType ");
			params.put("orderType",orderType);
		}
		if(Objects.nonNull(status) && status.equals(OrderStatus.TRUCK_DISPATCHED.getCode())){
			builder.append(" ORDER BY {oe.truckDispatcheddate} DESC");
		}
		else{
			builder.append(" ORDER BY {oe.deliveredDate} DESC ");
		}
		
		params.put("startDate", startDate);
		params.put("endDate", endDate);

		final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
		parameter.setSearchPageData(searchPageData);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(OrderEntryModel.class));
		query.getQueryParameters().putAll(params);
		parameter.setFlexibleSearchQuery(query);

		return paginatedFlexibleSearchService.search(parameter);
	}
	
	public FlexibleSearchService getFlexibleSearchService() {
		return flexibleSearchService;
	}

	
	public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
		this.flexibleSearchService = flexibleSearchService;
	}

}
