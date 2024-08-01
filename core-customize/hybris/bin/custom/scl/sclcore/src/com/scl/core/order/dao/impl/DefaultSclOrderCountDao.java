package com.scl.core.order.dao.impl;

import java.util.*;

import com.scl.core.dao.impl.TerritoryManagementDaoImpl;
import com.scl.core.enums.*;
import com.scl.core.jalo.SalesHistory;
import com.scl.core.model.*;
import com.scl.core.order.services.impl.DefaultSclOrderService;
import com.scl.facades.data.FilterTalukaData;
import com.scl.facades.data.RequestCustomerData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.DataConstraintDao;
import com.scl.core.order.dao.SclOrderCountDao;
import com.scl.core.services.TerritoryManagementService;
import com.scl.core.utility.SclDateUtility;

import de.hybris.platform.b2b.dao.impl.DefaultB2BOrderDao;
import de.hybris.platform.core.enums.DeliveryStatus;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.store.BaseStoreModel;

public class DefaultSclOrderCountDao extends DefaultB2BOrderDao implements SclOrderCountDao {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultSclOrderCountDao.class.getName());

	private static final String ERP_CITY = "erpCity";
	private static final String END_DATE = "endDate";
	private static final String START_DATE = "startDate";

	@Autowired
	BaseSiteService baseSiteService;

	@Autowired
	PaginatedFlexibleSearchService paginatedFlexibleSearchService;

	@Autowired
	TerritoryManagementService territoryService;

	@Autowired
	ConfigurationService configurationService;

	@Autowired
	DataConstraintDao dataConstraintDao;

	@Autowired
	TerritoryManagementDaoImpl territoryManagementDaoImpl;

	@Autowired
	UserService userService;
	@Autowired
	ProductService productService;

	/**
	 * Find orders in any status by date range
	 * @param user
	 * @param startDate
	 * @param endDate
	 * @param status
	 * @return
	 */
	@Override
	public Map<String, Long> findOrdersInAnyStatusByDateRange(UserModel user, OrderStatus status) {
		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put(OrderModel.STATUS, status);

		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT({di:pk}),SUM({di.diQuantity}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN deliveryItem AS di ON {oe:pk}={di:entry}} WHERE "
				+ SclDateUtility.getMtdClauseQuery("di:deliveredDate", attr));
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.getQueryParameters().putAll(attr);
		query.setResultClassList(Arrays.asList(Long.class,Long.class));

		final SearchResult<List<Object>> result = this.getFlexibleSearchService().search(query);
		Map<String, Long> map = new HashMap<String, Long>();
		long count = 0;
		long quantity = 0;
		if(result.getResult()!=null && !result.getResult().isEmpty()  && result.getResult().get(0)!=null) {
			if(result.getResult().get(0).size()>0 && result.getResult().get(0).get(0)!=null) {
				count = (long) result.getResult().get(0).get(0);
			}
			if(result.getResult().get(0).size()>1 && result.getResult().get(0).get(1)!=null) {
				quantity = (long) result.getResult().get(0).get(1);
			}
		}
		map.put("count", count);
		map.put("quantity", quantity);
		return map ;
	}

	/**
	 * Find orders in any status for SO
	 * @param user
	 * @param status
	 * @return
	 */
	@Override
	public Integer findOrdersByStatusForSO(final UserModel user, final OrderStatus[] status, Boolean approvalPending)
	{
		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put("statusList", Arrays.asList(status));
		//attr.put(OrderModel.PLACEDBY, user);
		Integer lastXDays = dataConstraintDao.findDaysByConstraintName("ORDER_LISTING_VISIBLITY");
		//attr.put(OrderModel.SUBAREAMASTER, territoryService.getCurrentTerritory());
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT({o:pk}) from { ").append(OrderModel._TYPECODE).append(" as o} WHERE ")
				.append(SclDateUtility.getLastXDayQuery("modifiedtime", attr, lastXDays)).append(" and {o:status} in (?statusList)  ");

		if(approvalPending) {
			sql.append(" and {o:approvalLevel} = ?approvalLevel ");
			getApprovalLevelByUser(user,attr);
		}
		//sql.append(" AND {o.subAreaMaster} in (?subAreaMaster) ");
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Arrays.asList(Integer.class));
		query.getQueryParameters().putAll(attr);
		final SearchResult<Integer> result = this.getFlexibleSearchService().search(query);
		return result.getResult().get(0);
	}

	@Override
	public Integer findOrderEntriesByStatusForSO(final UserModel user, final OrderStatus[] status)
	{
		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put("statusList", Arrays.asList(status));
		//attr.put(OrderModel.PLACEDBY, user);
		Integer lastXDays = dataConstraintDao.findDaysByConstraintName("ORDER_LISTING_VISIBLITY");
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT({oe:pk}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk}} WHERE {oe.remainingQuantity}>0 AND {oe.cancelledDate} is null AND ")
				.append(SclDateUtility.getLastXDayQuery("oe:modifiedtime", attr, lastXDays)).append(" AND {oe:status} IN (?statusList) ");
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Arrays.asList(Integer.class));
		query.getQueryParameters().putAll(attr);
		final SearchResult<Integer> result = this.getFlexibleSearchService().search(query);
		return result.getResult().get(0);
	}

	@Override
	public Integer findDeliveryItemByStatusForSO(final UserModel user, final DeliveryItemStatus[] status)
	{
		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put("statusList", Arrays.asList(status));
		Integer lastXDays = dataConstraintDao.findDaysByConstraintName("ORDER_LISTING_VISIBLITY");
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT({d:pk}) FROM {DeliveryItem as d join OrderEntry AS oe on {d.entry}={oe.pk} JOIN Order AS o ON {oe:order}={o:pk}} WHERE ")
				.append(SclDateUtility.getLastXDayQuery("d:latestStatusUpdate", attr, lastXDays)).append(" AND {d:status} IN (?statusList) ");
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Arrays.asList(Integer.class));
		query.getQueryParameters().putAll(attr);
		final SearchResult<Integer> result = this.getFlexibleSearchService().search(query);
		return result.getResult().get(0);
	}
	
	@Override
	public Map<String, Object> findMaxInvoicedDateAndQunatity(final UserModel user)
	{
		Map<String, Object> map = new HashMap<>();
		map.put(OrderEntryModel.INVOICECREATIONDATEANDTIME, null);
		map.put(OrderEntryModel.QUANTITYINMT, 0);
		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put(OrderModel.USER, user);
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT MAX({oe:invoiceCreationDateAndTime}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk}} WHERE {o.user}=?user and {oe.cancelledDate} is null ");
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Arrays.asList(Date.class));
		query.getQueryParameters().putAll(attr);
		final SearchResult<Date> result = this.getFlexibleSearchService().search(query);

		if(result.getResult()!=null && !result.getResult().isEmpty()) {
			Date lastLiftingDate =  result.getResult().get(0);
			if(lastLiftingDate!=null) {
				map.put(OrderEntryModel.INVOICECREATIONDATEANDTIME, lastLiftingDate);

				final StringBuilder sql2 = new StringBuilder();
				attr.put(OrderEntryModel.INVOICECREATIONDATEANDTIME, lastLiftingDate);
				sql2.append("SELECT {oe:quantityInMT} FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk}} WHERE {o.user}=?user and {oe:invoiceCreationDateAndTime}=?invoiceCreationDateAndTime  and {oe.cancelledDate} is null ");
				final FlexibleSearchQuery query1 = new FlexibleSearchQuery(sql2.toString());
				query1.setResultClassList(Arrays.asList(Double.class));
				query1.getQueryParameters().putAll(attr);
				final SearchResult<Double> quantityRes = this.getFlexibleSearchService().search(query1);
				if(quantityRes.getResult()!=null && !quantityRes.getResult().isEmpty()) {
					Double quantity =  quantityRes.getResult().get(0);
					if(quantity!=null) {
						map.put(OrderEntryModel.QUANTITYINMT, quantity);
					}
				}
			}
		}
		return map;
	}

	@Override
	public Integer findOrderByExpectedDeliveryDate(final UserModel user, final Date estimatedDeliveryDate, String routeId)
	{
		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put(OrderEntryModel.EXPECTEDDELIVERYDATE, estimatedDeliveryDate);
		attr.put(OrderModel.USER, user);
		attr.put(OrderModel.ROUTEID, routeId);
		attr.put(OrderModel.SITE, baseSiteService.getCurrentBaseSite());
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT({oe:pk}) from { ").append(OrderModel._TYPECODE).append(" as o JOIN OrderEntry as oe on {o.pk}={oe.order}} WHERE {o:user} = ?user AND {o:site} = ?site ")
				.append(" and {oe:expectedDeliveryDate} = ?expectedDeliveryDate AND {o.routeId}=?routeId AND {o:" + OrderModel.VERSIONID + "} IS NULL");
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Arrays.asList(Integer.class));
		query.getQueryParameters().putAll(attr);
		final SearchResult<Integer> result = this.getFlexibleSearchService().search(query);
		return result.getResult().get(0);
	}

	@Override
	public Integer findISOOrderByExpectedDeliveryDate(final WarehouseModel depotCode, final Date estimatedDeliveryDate, String routeId)
	{
		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put(OrderEntryModel.EXPECTEDDELIVERYDATE, estimatedDeliveryDate);
		attr.put(OrderModel.DESTINATION, depotCode);
		attr.put(OrderModel.ROUTEID, routeId);
		attr.put(OrderModel.SITE, baseSiteService.getCurrentBaseSite());
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT({oe:pk}) from { ").append(OrderModel._TYPECODE).append(" as o JOIN OrderEntry as oe on {o.pk}={oe.order}} WHERE {o:destination} = ?destination AND {o:site} = ?site ")
				.append(" and {oe:expectedDeliveryDate} = ?expectedDeliveryDate AND {o.routeId}=?routeId AND {o:" + OrderModel.VERSIONID + "} IS NULL");
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Arrays.asList(Integer.class));
		query.getQueryParameters().putAll(attr);
		final SearchResult<Integer> result = this.getFlexibleSearchService().search(query);
		return result.getResult().get(0);
	}

	/**
	 * Find direct Dispatch orders MTD count
	 * @param currentUser
	 * @param warehouseType
	 * @return
	 */
	@Override
	public Integer findDirectDispatchOrdersMTDCount(final UserModel currentUser, final WarehouseType warehouseType, final int month, final int year)
	{
		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put(WarehouseModel.TYPE, warehouseType);

		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT({di:pk}) ");
		sql.append("FROM {").append(OrderEntryModel._TYPECODE).append(" AS oe ");
		sql.append("JOIN ").append(OrderModel._TYPECODE).append(" AS o ");
		sql.append("ON {o:").append(OrderModel.PK).append("}={oe:").append(OrderEntryModel.ORDER).append("} ");
		sql.append("JOIN ").append(WarehouseModel._TYPECODE).append(" AS w ");
		sql.append("ON {oe:").append(OrderEntryModel.SOURCE).append("}={w:").append(WarehouseModel.PK).append("} ");
		sql.append(" JOIN ").append(DeliveryItemModel._TYPECODE).append(" AS di");
		sql.append(" ON {oe:").append(OrderEntryModel.PK).append("}={di:").append(DeliveryItemModel.ENTRY).append("}} ");
		sql.append("WHERE ");
		sql.append(SclDateUtility.getDateClauseQueryByMonthYear("di.truckDispatchedDateAndTime", month, year, attr));
		sql.append(" and {w:").append(WarehouseModel.TYPE).append("} = ?type ");

		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Arrays.asList(Integer.class));
		query.getQueryParameters().putAll(attr);
		LOG.info(String.format("Build Query for findDirectDispatchOrdersMTDCount :: %s",query));
		final SearchResult<Integer> result = this.getFlexibleSearchService().search(query);
		return result.getResult().get(0);
	}

	@Override
	public Double findMaxOrderQuantityForSO(UserModel currentUser, final Date startDate, final Date endDate) {
		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put(OrderModel.USER, currentUser);
		attr.put(START_DATE, startDate);
		attr.put(END_DATE, endDate);
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT MAX({o:totalQuantity}) ");
		sql.append("FROM {").append(OrderModel._TYPECODE).append(" AS o} ");
		sql.append("WHERE ").append("{o:").append(OrderModel.USER).append("} = ?user ");
		sql.append("AND {o:").append(OrderModel.ORDERACCEPTEDDATE).append("} >= ?startDate ");
		sql.append("AND {o:").append(OrderModel.ORDERACCEPTEDDATE).append("} <= ?endDate ");
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Arrays.asList(Double.class));
		query.getQueryParameters().putAll(attr);
		final SearchResult<Double> result = this.getFlexibleSearchService().search(query);
		return result.getResult().get(0);
	}

	@Override
	public Integer checkOrderCountBeforeThreeMonths(UserModel currentUser, Date startDate, Date endDate) {
		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put(OrderModel.USER, currentUser);
		attr.put(START_DATE, startDate);
		attr.put(END_DATE, endDate);
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT({o:pk}) from { ").append(OrderModel._TYPECODE).append(" as o} WHERE {o:user} = ?user ")
				.append(" and {o:orderAcceptedDate} >= ?startDate and {o:orderAcceptedDate} <= ?endDate  AND {" + OrderModel.VERSIONID + "} IS NULL");
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Arrays.asList(Integer.class));
		query.getQueryParameters().putAll(attr);
		final SearchResult<Integer> result = this.getFlexibleSearchService().search(query);
		return result.getResult().get(0);
	}

	@Override
	public List<Double> findOrderQuantityListForSO(UserModel currentUser, Date startDate, Date endDate, Set<String> erpcity) {
		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put(OrderModel.USER, currentUser);
		attr.put(START_DATE, startDate);
		attr.put(END_DATE, endDate);
		attr.put(ERP_CITY, erpcity);
		String queryResult="SELECT MAX({o:totalQuantity}) from {Order as o JOIN Address as a on {o:deliveryAddress}={a:pk} JOIN GeographicalMaster as g on {g:district}={a:district}} where {g:erpCity} IN (?erpCity) and {o:orderAcceptedDate} >= ?startDate  and {o:orderAcceptedDate} <= ?endDate GROUP BY {o:user}";
		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
		query.setResultClassList(Arrays.asList(Double.class));
		query.getQueryParameters().putAll(attr);
		final SearchResult<Double> result = this.getFlexibleSearchService().search(query);
		return result.getResult();
	}

	@Override
	public SearchPageData<OrderModel> findOrdersListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, Boolean isCreditLimitBreached, String spApprovalFilter, Boolean approvalPending) {
		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put("statusList", Arrays.asList(status));
		//attr.put(OrderModel.USER, user);
		attr.put("isCreditLimitBreached", isCreditLimitBreached);
		Integer lastXDays = dataConstraintDao.findDaysByConstraintName("ORDER_LISTING_VISIBLITY");
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT {o:pk} from { ").append(OrderModel._TYPECODE).append(" as o} WHERE   ")
				.append(SclDateUtility.getLastXDayQuery("o:modifiedtime", attr, lastXDays)).append(" and {o:status} in (?statusList) ");
		if(isCreditLimitBreached){
			sql.append(" and {o:creditLimitBreached} = ?isCreditLimitBreached ");
		}
		if(approvalPending) {
			sql.append(" and {o:approvalLevel} = ?approvalLevel ");
			getApprovalLevelByUser(user,attr);
		}
		sql.append(appendSpApprovalActionQuery(attr, user, spApprovalFilter));

		sql.append(" ORDER BY {o:modifiedtime} DESC ");
		final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
		parameter.setSearchPageData(searchPageData);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Collections.singletonList(OrderModel.class));
		query.getQueryParameters().putAll(attr);
		parameter.setFlexibleSearchQuery(query);
		return paginatedFlexibleSearchService.search(parameter);
	}

	@Override
	public SearchPageData<OrderEntryModel> findOrderEntriesListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, String spApprovalFilter) {
		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put("statusList", Arrays.asList(status));
		//attr.put(OrderModel.USER, user);
		Integer lastXDays = dataConstraintDao.findDaysByConstraintName("ORDER_LISTING_VISIBLITY");
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT {oe:pk} FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk}} WHERE {oe.remainingQuantity}>0 AND  {oe.cancelledDate} is null AND ")
				.append(SclDateUtility.getLastXDayQuery("oe:modifiedtime", attr, lastXDays)).append(" AND {oe:status} IN (?statusList) ");
		sql.append(appendSpApprovalActionQuery(attr, user, spApprovalFilter));
		sql.append(" ORDER BY {oe.latestStatusUpdate} DESC ");
		final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
		parameter.setSearchPageData(searchPageData);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Collections.singletonList(OrderEntryModel.class));
		query.getQueryParameters().putAll(attr);
		parameter.setFlexibleSearchQuery(query);
		LOG.info("findOrderEntriesListByStatusForSO dao query :" +query);
		return paginatedFlexibleSearchService.search(parameter);
	}
	
	@Override
	public SearchPageData<DeliveryItemModel> findDeliveryItemListByStatus(UserModel user, BaseSiteModel site, DeliveryItemStatus[] status, SearchPageData searchPageData, String spApprovalFilter, String searchKey) {
		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put("statusList", Arrays.asList(status));
		Integer lastXDays = dataConstraintDao.findDaysByConstraintName("ORDER_LISTING_VISIBLITY");
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT {d:pk} FROM {Order AS o JOIN OrderEntry AS oe ON {oe:order}={o:pk} join DeliveryItem as d on {oe.pk}={d.entry} ");
		if(StringUtils.isNotBlank(searchKey)){
			sql.append(" JOIN SclCustomer as u on {u:pk}={o:user} ");
		}
		sql.append("} WHERE ")
				//d:modifiedtime
			.append(SclDateUtility.getLastXDayQuery("d:latestStatusUpdate", attr, lastXDays)).append(" AND {d:status} IN (?statusList) ");

		if(StringUtils.isNotBlank(searchKey)){
			WarehouseModel warehouseByCode = getWarehouseByCode(searchKey);
			if(Objects.nonNull(warehouseByCode)){
				sql.append(" and ( {oe:source} is not null AND {oe:source} = ?warehouseByCode ) ");
				attr.put("warehouseByCode",warehouseByCode);
			}
			else{
			sql.append(" and (UPPER({o:code}) like ?filter OR UPPER({u:uid}) like ?filter OR UPPER({u:name}) like ?filter OR UPPER({u:customerNo}) like ?filter OR ({d:truckNo} is not null and {d:truckNo} like ?filter) OR UPPER({oe:productAliasName}) like ?filter OR {o:erpOrderNumber} like ?filter )");
			attr.put("filter","%"+searchKey.toUpperCase()+"%");
			}
		}
		sql.append(appendSpApprovalActionQuery(attr, user, spApprovalFilter));
		sql.append(" ORDER BY {d:latestStatusUpdate} DESC ");
		final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
		parameter.setSearchPageData(searchPageData);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Collections.singletonList(DeliveryItemModel.class));
		query.getQueryParameters().putAll(attr);
		parameter.setFlexibleSearchQuery(query);
		LOG.info("Filtered Delivery Items : " +query + "  Attr: " + attr);
		return paginatedFlexibleSearchService.search(parameter);
	}

	public String appendSpApprovalActionQuery(Map<String, Object> param, UserModel user, String spApprovalFilter) {
		String queryResult = "";
		if(spApprovalFilter!=null && (spApprovalFilter.equals("approved") || spApprovalFilter.equals("rejected"))) {
			queryResult = queryResult.concat(" and {o.spApprovalStatus}=?spApprovalStatus ");
			queryResult = queryResult.concat(" and {o.spApprovalActionBy}=?spApprovalActionBy ");
			if(spApprovalFilter.equals("approved")) {
				param.put("spApprovalStatus", SPApprovalStatus.APPROVED);
			}
			else {
				param.put("spApprovalStatus", SPApprovalStatus.REJECTED);
			}
			param.put("spApprovalActionBy", (B2BCustomerModel)user);
		}
		return queryResult;
	}

	@Override
	public SearchPageData<OrderModel> findOrdersListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, String filter ,String productName , OrderType orderType, Boolean isCreditLimitBreached, String spApprovalFilter, Boolean approvalPending) {

		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put("statusList", Arrays.asList(status));
		attr.put("store", store);
		//attr.put(OrderModel.USER, user);
		attr.put("isCreditLimitBreached", isCreditLimitBreached);
		Integer lastXDays = dataConstraintDao.findDaysByConstraintName("ORDER_LISTING_VISIBLITY");
		String queryResult= "SELECT {o:pk} from {ORDER as o JOIN SclCustomer as u on {u:pk}={o:user} } WHERE " + SclDateUtility.getLastXDayQuery("o:modifiedtime", attr, lastXDays) + " and {o:status} in (?statusList) " ;

		if(null != productName) {
			List<String> productList = Arrays.asList(productName.split(","));
			if (productList != null && !productList.isEmpty()) {
				queryResult = queryResult.concat(" and {o:productName} in (?productList) ");
				attr.put("productList", productList);
			}
		}
		if(StringUtils.isNotBlank(filter)){
			queryResult = queryResult.concat(" and (UPPER({o:code}) like ?filter OR UPPER({u:uid}) like ?filter OR UPPER({u:name}) like ?filter OR UPPER({u:customerNo}) like ?filter) ");
			attr.put("filter","%"+filter.toUpperCase()+"%");
		}

		if(null!= orderType){
			queryResult = queryResult.concat(" and {o:orderType} = ?orderType ");
			attr.put("orderType",orderType);
		}
		if(isCreditLimitBreached){
			queryResult.concat(" and {o:creditLimitBreached} = ?isCreditLimitBreached ");
		}

		if(approvalPending) {
			queryResult = queryResult.concat(" and {o:approvalLevel} = ?approvalLevel ");
			getApprovalLevelByUser(user,attr);
		}

		queryResult = queryResult.concat(appendSpApprovalActionQuery(attr, user, spApprovalFilter));
		queryResult = queryResult.concat(" ORDER BY {o:modifiedtime} DESC");

		final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
		parameter.setSearchPageData(searchPageData);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
		query.setResultClassList(Collections.singletonList(OrderModel.class));
		query.getQueryParameters().putAll(attr);
		parameter.setFlexibleSearchQuery(query);

		return paginatedFlexibleSearchService.search(parameter);
	}

	private WarehouseModel getWarehouseByCode(String code){
		final Map<String, Object> params = new HashMap<String, Object>();
		LOG.info("Warehouse Code:"+code);
		String builder = "SELECT {pk} FROM {Warehouse} WHERE {code}=?code ";
		params.put("code", code);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder);
		query.setResultClassList(Collections.singletonList(WarehouseModel.class));
		query.addQueryParameters(params);
		final SearchResult<WarehouseModel> searchResult = getFlexibleSearchService().search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}
	private List<ProductModel> getProductByName(String name){
		final Map<String, Object> params = new HashMap<String, Object>();
		LOG.info("Product name:"+name);
		String builder = "select {pa.product} from {ProductAlias as pa join Product as p on{p.pk}={pa.product} join Catalog as c on {c.pk}={p.catalog} join CatalogVersion as cv on {cv.pk}={p.catalogversion} } where {cv.version}='Online' and UPPER({pa:aliasName}) = ?name ";
		//select {p.pk} from {Product as p join Catalog as c on {c.pk}={p.catalog} join CatalogVersion as cv on {cv.pk}={p.catalogversion} join ProductAlias as pa on {p:pk}={pa:product}} where {cv.version}='Online' and {pa:aliasName}=?name";
		//params.put("filter","%"+name.toUpperCase()+"%");
		params.put("name",name.toUpperCase());
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder);
		query.addQueryParameters(params);
		query.setResultClassList(Collections.singletonList(ProductModel.class));
		final SearchResult<ProductModel> searchResult = getFlexibleSearchService().search(query);
		if (searchResult.getResult()!=null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult();
		return Collections.EMPTY_LIST;
	}
	@Override
	public SearchPageData<OrderEntryModel> findOrderEntriesListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, String filter ,String productName , OrderType orderType, String spApprovalFilter) {
		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put("statusList", Arrays.asList(status));
		attr.put("store", store);
		//attr.put(OrderModel.USER, user);
		Integer lastXDays = dataConstraintDao.findDaysByConstraintName("ORDER_LISTING_VISIBLITY");
		String queryResult=" SELECT {oe:pk} FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as u on {u:pk}={o:user} } WHERE  {oe.remainingQuantity}>0 AND  {oe.cancelledDate} is null AND "
				+ SclDateUtility.getLastXDayQuery("oe:modifiedtime", attr, lastXDays) + " AND {oe:status} IN (?statusList) ";

/*
		if(null != productName) {
			List<String> productList = Arrays.asList(productName.split(","));
			if (productList != null && !productList.isEmpty()) {
				queryResult = queryResult.concat(" and {o:productName} in (?productList) ");
				attr.put("productList", productList);
			}
		}
*/


		if(StringUtils.isNotBlank(filter)){
			WarehouseModel warehouseByCode = getWarehouseByCode(filter);
			if(Objects.nonNull(warehouseByCode)){
				queryResult = queryResult.concat(" and ( {oe:source} is not null AND {oe:source} = ?warehouseByCode ) ");
				attr.put("warehouseByCode",warehouseByCode);
			}else {
				queryResult = queryResult.concat(" and (UPPER({o:code}) like ?filter OR UPPER({u:uid}) like ?filter OR UPPER({u:name}) like ?filter OR UPPER({u:customerNo}) like ?filter OR ({oe:truckNo} is not null and {oe:truckNo} like ?filter) OR UPPER({oe:productAliasName}) like ?filter OR {o:erpOrderNumber} like ?filter )");
				attr.put("filter", "%" + filter.toUpperCase() + "%");
			}
		}
		if(null!= orderType){
			queryResult = queryResult.concat(" and {o:orderType} = ?orderType ");
			attr.put("orderType",orderType);
		}
		queryResult = queryResult.concat(appendSpApprovalActionQuery(attr, user, spApprovalFilter));
		queryResult = queryResult.concat(" ORDER BY {oe:latestStatusUpdate} DESC  ");
		final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
		parameter.setSearchPageData(searchPageData);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
		query.setResultClassList(Collections.singletonList(OrderEntryModel.class));
		query.getQueryParameters().putAll(attr);
		parameter.setFlexibleSearchQuery(query);

		return paginatedFlexibleSearchService.search(parameter);
		}

	@Override
	public Integer findCreditBreachCountMTD (SclCustomerModel dealer) {
		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put(OrderModel.USER, dealer);
		attr.put("boolean", true);
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT({o:pk}) from { ").append(OrderModel._TYPECODE).append(" as o} WHERE {o:creditLimitBreached} = ?boolean  ")
				.append(" AND {o:user} = ?user  and ").append(SclDateUtility.getMtdClauseQuery("o:date", attr)).append(" AND {o:" + OrderModel.VERSIONID + "} IS NULL");
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Arrays.asList(Integer.class));
		query.getQueryParameters().putAll(attr);
		final SearchResult<Integer> result = this.getFlexibleSearchService().search(query);
		return result.getResult().get(0);
	}

	@Override
	public Integer findCancelOrdersByStatusForSO(UserModel user, OrderStatus[] status) {
		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put("statusList", Arrays.asList(status));
		Integer lastXDays = dataConstraintDao.findDaysByConstraintName("CANCELLED_ORDER_VISIBLITY");

		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT({o:pk}) from { ").append(OrderModel._TYPECODE).append(" as o} WHERE ")
				.append(SclDateUtility.getLastXDayQuery("o:cancelledDate", attr, lastXDays)).append(" and {o:status} in (?statusList) ");
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Arrays.asList(Integer.class));
		query.getQueryParameters().putAll(attr);
		final SearchResult<Integer> result = this.getFlexibleSearchService().search(query);
		return result.getResult().get(0);
	}

	@Override
	public Integer findCancelOrderEntriesByStatusForSO(UserModel user, OrderStatus[] status) {
		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put("statusList", Arrays.asList(status));
		Integer lastXDays = dataConstraintDao.findDaysByConstraintName("CANCELLED_ORDER_VISIBLITY");

		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT({oe:pk}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk}} WHERE ")
				.append(SclDateUtility.getLastXDayQuery("oe:cancelledDate", attr, lastXDays)).append(" AND {oe:status} IN (?statusList) ");
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Arrays.asList(Integer.class));
		query.getQueryParameters().putAll(attr);
		final SearchResult<Integer> result = this.getFlexibleSearchService().search(query);
		return result.getResult().get(0);
	}

	@Override
	public SearchPageData<OrderModel> findCancelOrdersListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, String spApprovalFilter, String monthYear) {
		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put("statusList", Arrays.asList(status));
		Integer lastXDays = dataConstraintDao.findDaysByConstraintName("CANCELLED_ORDER_VISIBLITY");
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT {o:pk} from {").append(OrderModel._TYPECODE).append(" as o} WHERE ");
		if(monthYear!=null) {
			sql.append("{o:cancelledDate} like ?monthYear");
			attr.put("monthYear", monthYear);
		}
		else {
			sql.append(SclDateUtility.getLastXDayQuery("o:cancelledDate", attr, lastXDays));
		}
		sql.append(" and {o:status} in (?statusList) ");
		sql.append(appendSpApprovalActionQuery(attr, user, spApprovalFilter));
		sql.append(" ORDER BY {o:cancelledDate} DESC");
		final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
		parameter.setSearchPageData(searchPageData);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Collections.singletonList(OrderModel.class));
		query.getQueryParameters().putAll(attr);
		parameter.setFlexibleSearchQuery(query);
		return paginatedFlexibleSearchService.search(parameter);
	}

	@Override
	public SearchPageData<OrderEntryModel> findCancelOrderEntriesListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, String spApprovalFilter, String monthYear) {
		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put("statusList", Arrays.asList(status));
		Integer lastXDays = dataConstraintDao.findDaysByConstraintName("CANCELLED_ORDER_VISIBLITY");

		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT {oe:pk} FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk}} WHERE ");
		if(monthYear!=null) {
			sql.append("{oe:cancelledDate} like ?monthYear");
			attr.put("monthYear", monthYear);
		}
		else {
			sql.append(SclDateUtility.getLastXDayQuery("oe:cancelledDate", attr, lastXDays));
		}

		//has to confirm with samyak about all status we can get from FE

		sql.append(" AND {oe:status} IN (?statusList) ");
		//commented due to NO requirement
		//sql.append(" AND ({o.erpOrderNumber} is not null or {o.orderValidatedDate} is not null or {o.orderSentForApprovalDate} is not null) ");
		sql.append(appendSpApprovalActionQuery(attr, user, spApprovalFilter));
		sql.append(" ORDER BY {oe:cancelledDate} DESC ");
		final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
		parameter.setSearchPageData(searchPageData);
		LOG.info(String.format("Build Query for findCancelOrderEntriesListByStatusForSO :: %s",sql.toString()));
		attr.entrySet().stream().forEach(i -> LOG.info(String.format("attr: %s :: %s",i.getKey(),i.getValue())));
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Collections.singletonList(OrderEntryModel.class));
		query.getQueryParameters().putAll(attr);
		parameter.setFlexibleSearchQuery(query);
		return paginatedFlexibleSearchService.search(parameter);
	}

	@Override
	public SearchPageData<OrderModel> findCancelOrdersListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, String filter ,String productName , OrderType orderType, String spApprovalFilter, String monthYear) {

		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put("statusList", Arrays.asList(status));
		Integer lastXDays = dataConstraintDao.findDaysByConstraintName("CANCELLED_ORDER_VISIBLITY");
		String queryResult= "SELECT {o:pk} from {ORDER as o JOIN OrderEntry AS oe ON {o:pk}={oe:order} JOIN SclCustomer as u on {u:pk}={o:user} } WHERE ";

		if(monthYear!=null) {
			queryResult = queryResult.concat("{o:cancelledDate} like ?monthYear ");
			attr.put("monthYear", monthYear);
		}
		else {
			queryResult = queryResult.concat(SclDateUtility.getLastXDayQuery("o:cancelledDate", attr, lastXDays));
		}
		queryResult = queryResult.concat(" and {o:status} in (?statusList)");

		if(null != productName) {
			List<String> productList = Arrays.asList(productName.split(","));
			if (productList != null && !productList.isEmpty()) {
				queryResult = queryResult.concat(" and {o:productName} in (?productList) ");
				attr.put("productList", productList);
			}
		}

		if(StringUtils.isNotBlank(filter)){
			WarehouseModel warehouseByCode = getWarehouseByCode(filter);
			if(Objects.nonNull(warehouseByCode)){
				queryResult = queryResult.concat(" and ( {oe:source} is not null AND {oe:source} = ?warehouseByCode ) ");
				attr.put("warehouseByCode",warehouseByCode);
			}
			else {
				queryResult = queryResult.concat(" and (UPPER({o:code}) like ?filter OR UPPER({u:uid}) like ?filter OR UPPER({u:name}) like ?filter OR UPPER({u:customerNo}) like ?filter OR UPPER({oe:productAliasName}) like ?filter OR {o:erpOrderNumber} like ?filter )");
				attr.put("filter", "%" + filter.toUpperCase() + "%");
			}
		}

		if(null!= orderType){
			queryResult = queryResult.concat(" and {o:orderType} = ?orderType ");
			attr.put("orderType",orderType);
		}
		queryResult = queryResult.concat(appendSpApprovalActionQuery(attr, user, spApprovalFilter));
		queryResult = queryResult.concat(" ORDER BY {o:cancelledDate} DESC ");
		final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
		parameter.setSearchPageData(searchPageData);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
		query.setResultClassList(Collections.singletonList(OrderModel.class));
		query.getQueryParameters().putAll(attr);
		parameter.setFlexibleSearchQuery(query);
		LOG.info("Filtered cancelled order list for SO : " +query);
		return paginatedFlexibleSearchService.search(parameter);
	}

	@Override
	public SearchPageData<OrderEntryModel> findCancelOrderEntriesListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, String filter ,String productName , OrderType orderType, String spApprovalFilter, String monthYear) {
		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put("statusList", Arrays.asList(status));
		Integer lastXDays = dataConstraintDao.findDaysByConstraintName("CANCELLED_ORDER_VISIBLITY");
		String queryResult=" SELECT {oe:pk} FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as u on {u:pk}={o:user} } WHERE ";
		if(monthYear!=null) {
			queryResult = queryResult.concat("{oe:cancelledDate} LIKE ?monthYear");
			attr.put("monthYear", monthYear);
		}
		else {
			queryResult = queryResult.concat(SclDateUtility.getLastXDayQuery("oe:cancelledDate", attr, lastXDays));
		}
		//clarification from samyak
		queryResult = queryResult.concat(" AND {oe:status} IN (?statusList)");
		//commened due to No requirement
		//queryResult = queryResult.concat(" AND ({o.erpOrderNumber} is not null or {o.orderValidatedDate} is not null or {o.orderSentForApprovalDate} is not null) ");

		if(StringUtils.isNotBlank(filter)){
			WarehouseModel warehouseByCode = getWarehouseByCode(filter);
			if(Objects.nonNull(warehouseByCode)){
				queryResult = queryResult.concat(" and ( {oe:source} is not null AND {oe:source} = ?warehouseByCode ) ");
				attr.put("warehouseByCode",warehouseByCode);
			}
			else {
				queryResult = queryResult.concat(" and (UPPER({o:code}) like ?filter OR UPPER({u:uid}) like ?filter OR UPPER({u:name}) like ?filter OR UPPER({u:customerNo}) like ?filter OR UPPER({oe:productAliasName}) like ?filter OR {o:erpOrderNumber} like ?filter )");
				attr.put("filter", "%" + filter.toUpperCase() + "%");
			}
		}
		if(null!= orderType){
			queryResult = queryResult.concat(" and {o:orderType} = ?orderType ");
			attr.put("orderType",orderType);
		}
		queryResult = queryResult.concat(appendSpApprovalActionQuery(attr, user, spApprovalFilter));
		queryResult = queryResult.concat(" ORDER BY {oe:cancelledDate} DESC ");
		final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
		parameter.setSearchPageData(searchPageData);

		attr.entrySet().stream().forEach(i -> LOG.info(String.format("attr: %s :: %s",i.getKey(),i.getValue())));
		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
		query.setResultClassList(Collections.singletonList(OrderEntryModel.class));
		query.getQueryParameters().putAll(attr);
		parameter.setFlexibleSearchQuery(query);
		LOG.info("Build Query for findCancelOrderEntriesListByStatusForSO OVL :: %s", query);
		return paginatedFlexibleSearchService.search(parameter);
	}

	@Override
	public Integer checkOrderCountBeforeThreeMonths(SclCustomerModel sclCustomer) {
		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put("sclCustomer",sclCustomer.getCustomerNo());
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT({pk}) FROM {SalesHistory} WHERE {customerNo} =?sclCustomer AND").append(SclDateUtility.getSixAndFourMonthsClauseQuery("salesOrderDate", attr));
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Arrays.asList(Integer.class));
		query.getQueryParameters().putAll(attr);
		final SearchResult<Integer> result = this.getFlexibleSearchService().search(query);
		return result.getResult().get(0)!=null ? result.getResult().get(0) : 0;
	}

	@Override
	public Double findMaxOrderQuantityForSO(SclCustomerModel sclCustomer) {
		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put(OrderModel.SITE, baseSiteService.getCurrentBaseSite());
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT({pk}) FROM {SalesHistory} WHERE {customerNo} =?sclCustomer AND").append(SclDateUtility.getSixAndFourMonthsClauseQuery("salesOrderDate", attr)+ "GROUP BY {customerNo}");
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Arrays.asList(Double.class));
		query.getQueryParameters().putAll(attr);
		final SearchResult<Double> result = this.getFlexibleSearchService().search(query);
		return result.getResult().get(0)!=null ? result.getResult().get(0) : 0.0;
	}


	@Override
	public List<Double> findOrderQuantityListForSO(String subArea) {
		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put("subArea",territoryService.getTerritoryById(subArea));
		final StringBuilder queryResult = new StringBuilder();
		queryResult.append("select SUM({sh.quantity}) from {SalesHistory as sh JOIN SclCustomer as sc ON {sc.customerNo}={sh.customerNo} JOIN CustomerSubAreaMapping as cs ON {sc.pk}={cs.sclCustomer}} where {cs.subAreaMaster}=?subArea and"+ SclDateUtility.getSixMonthsClauseQuery("sh.salesOrderDate",attr) + "GROUP BY {sh.salesOrderNo}");
		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
		query.setResultClassList(Arrays.asList(Double.class));
		query.getQueryParameters().putAll(attr);
		final SearchResult<Double> result = this.getFlexibleSearchService().search(query);
		if(result.getResult()!=null && !result.getResult().isEmpty())
		{
			return result.getResult();
		}
		else
		{
			return Collections.emptyList();
		}
	}

	@Override
	public SearchPageData<DeliveryItemModel> findOrderEntriesListByStatusForEPOD(UserModel user, BaseStoreModel store, List<String> Statuses, SearchPageData searchPageData, String filter) {
		final Map<String, Object> attr = new HashMap<String, Object>();
		List<EpodStatus> epodStatusList = new ArrayList<>();
		for(String status: Statuses){
			epodStatusList.add(EpodStatus.valueOf(status));
		}
		attr.put("statusList",epodStatusList);
		attr.put("store", store);
		//attr.put(OrderModel.USER, user);
		Integer lastXDays = dataConstraintDao.findDaysByConstraintName("ORDER_LISTING_VISIBLITY");
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT {di:pk} FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk}");
		sql.append(" JOIN DeliveryItem AS di ON {di:entry}={oe:pk} ");
		if(filter!=null) {
			sql.append(" join SclCustomer as c on {c.pk}={o.user} ");
		}
		sql.append("} WHERE ");
		if(epodStatusList.contains(EpodStatus.PENDING)){
			sql.append(SclDateUtility.getLastXDayQuery("di:epodInitiateDate", attr, lastXDays));
		}
		else{
			sql.append(SclDateUtility.getLastXDayQuery("di:deliveredDate", attr, lastXDays));
		}

		sql.append(" AND {di:epodStatus} IN (?statusList) ");
		if(filter!=null)
		{
			sql.append(" and (UPPER({o.code}) like (?filter) or UPPER({c.name}) like (?filter) or {c.uid} like (?filter) or {c.customerNo} like (?filter) or {c.mobileNumber} like (?filter) ) ");
			attr.put("filter", "%"+filter+"%");
		}
		if(epodStatusList.contains(EpodStatus.PENDING)){
			sql.append(" ORDER BY {di.epodInitiateDate} DESC ");
		}
		else{
			sql.append(" ORDER BY {di.deliveredDate} DESC ");
		}

		final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
		parameter.setSearchPageData(searchPageData);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Collections.singletonList(DeliveryItemModel.class));
		query.getQueryParameters().putAll(attr);
		parameter.setFlexibleSearchQuery(query);
		return paginatedFlexibleSearchService.search(parameter);
	}

	@Override
	public List<List<Object>> getDealerDetailedSummaryList(RequestCustomerData requestCustomerData) {
		final Map<String, Object> params  =  new HashMap<>();
		B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
		final StringBuilder builder = new StringBuilder();
		if(currentUser instanceof SclUserModel){
			FilterTalukaData filterTalukaData = new FilterTalukaData();
			List<SubAreaMasterModel> subAreaMaster = territoryManagementDaoImpl.getTalukaForUser(filterTalukaData);
			builder.append("Select ");
			if(requestCustomerData.getTopPerformers()!=null && requestCustomerData.getTopPerformers()>0){
				builder.append("TOP ?topPerformers " );
				params.put("topPerformers",requestCustomerData.getTopPerformers());
			}
			builder.append(" {c.pk},sum({oe.quantityInMT}) FROM {CustomerSubAreaMapping as m join SclCustomer as c on {c.pk}={m.sclCustomer} left join Order as o on {c.pk}={o.user} left join OrderEntry as oe on {oe.order}={o.pk}} WHERE {m.subAreaMaster} in (?subAreaMaster) AND {c.counterType}=?counterType AND {m.isActive} = ?active AND {m.brand} = ?brand and {m.sclCustomer} is not null and {m.isOtherBrand}=?isOtherBrand and {oe.deliveredDate}>=?startDate and {oe.deliveredDate}<?endDate ");
			territoryManagementDaoImpl.appendFilterQuery(builder,params,requestCustomerData);
			builder.append(" group by  {c.pk} order by sum({oe.quantityInMT}) desc ");
			params.put("subAreaMaster",subAreaMaster);
			params.put("counterType", CounterType.DEALER);
		}
		else if(((SclCustomerModel)currentUser).getCounterType()!=null && ((SclCustomerModel)currentUser).getCounterType().equals(CounterType.SP)){
			// SclCustomerModel customer = (SclCustomerModel) currentUser;
			builder.append("Select ");
			if(requestCustomerData.getTopPerformers()!=null && requestCustomerData.getTopPerformers()>0){
				builder.append("TOP ?topPerformers " );
				params.put("topPerformers",requestCustomerData.getTopPerformers());
			}
			builder.append(" {c.pk},sum({oe.quantityInMT}) from {CustDepotDealerMapping as d join SpCustDepotMapping as s on {d.custDepotCode}={s.custDepotCode} join SclCustomer as c on {c.pk}={d.dealerCode}  left join Order as o on {c.pk}={o.user} left join OrderEntry as oe on {oe.order}={o.pk}} where {s.spCode} = ?currentUser AND {s.brand} = ?brand AND {s.active} = ?active and {oe.deliveredDate}>=?startDate and {oe.deliveredDate}<?endDate ");
			requestCustomerData.setIncludeNonSclCustomer(true);
			territoryManagementDaoImpl.appendFilterQuery(builder,params,requestCustomerData);
			builder.append(" group by  {c.pk} order by sum({oe.quantityInMT}) desc ");
			params.put("currentUser",currentUser);
		}
//		params.put("status", RequisitionStatus.DELIVERED);
		params.put("active",Boolean.TRUE);
		params.put("brand",baseSiteService.getCurrentBaseSite());
		params.put("startDate",requestCustomerData.getStartDate());
		params.put("endDate",requestCustomerData.getEndDate());
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(SclCustomerModel.class, Double.class));
		query.getQueryParameters().putAll(params);
		final SearchResult<List<Object>> result = getFlexibleSearchService().search(query);
		if(result.getResult()!=null && !result.getResult().isEmpty()){
			return result.getResult();
		}
		return Collections.EMPTY_LIST;
	}

	@Override
	public void getApprovalLevelByUser(UserModel user, Map<String, Object> attr) {
		if(user.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SALES_OFFICER_GROUP_ID))) {
			attr.put("approvalLevel",TerritoryLevels.SUBAREA);
		} else if (user.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.TSM_GROUP_ID))) {
			attr.put("approvalLevel",TerritoryLevels.DISTRICT);
		} else if (user.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RH_GROUP_ID))) {
			attr.put("approvalLevel",TerritoryLevels.REGION);
		}
	}

	@Override
	public List<SalesHistoryModel> getNCREntriesExistingInOrderEntry() {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {SalesHistory as s JOIN OrderEntry as oe on {oe:erpLineItemId}={s:lineId}} WHERE {s.synced}=?synced");
		params.put("synced", false);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(SalesHistoryModel.class));
		query.addQueryParameters(params);
		final SearchResult<SalesHistoryModel> searchResult = getFlexibleSearchService().search(query);
		return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
	}

	@Override
	public List<String> getNCREntriesNotExistingInOrderEntry() {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {lineId} FROM {SalesHistory as s JOIN OrderEntry as oe on {oe:erpLineItemId}={s:lineId}} WHERE {s.synced}=?synced and {oe:pk} is null");
		params.put("synced", false);

		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		query.addQueryParameters(params);
		final SearchResult<String> searchResult = getFlexibleSearchService().search(query);
		return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
	}
}
