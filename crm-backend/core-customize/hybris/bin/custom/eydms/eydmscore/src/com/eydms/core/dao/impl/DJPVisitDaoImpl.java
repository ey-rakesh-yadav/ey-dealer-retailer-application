package com.eydms.core.dao.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.eydms.core.enums.*;
import com.eydms.core.services.TerritoryManagementService;
import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.dao.DJPVisitDao;
import com.eydms.core.model.*;
import com.eydms.core.utility.EyDmsDateUtility;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;
import de.hybris.platform.site.BaseSiteService;

public class DJPVisitDaoImpl implements DJPVisitDao {
	private static final Logger LOG = Logger.getLogger(DJPVisitDaoImpl.class);
	
	private static final String MARKET_VISIT_QUERY ="SELECT {vm.pk} FROM {VisitMaster AS vm} WHERE {vm.status} IN (?status) AND {vm.user}=?currentUser AND {vm.visitPlannedDate} >= ?date ";
	private static final String COUNTER_LIST_QUERY ="SELECT {vm.pk} FROM {VisitMaster AS vm} WHERE {vm.pk}=?id";

	private static final String VISIT_LIST_QUERY ="SELECT {vm.pk} FROM {VisitMaster AS vm} WHERE {vm.pk}=?visitId";

	private static final String REVIEW_LOGS_QUERY ="SELECT {vm.pk} FROM {VisitMaster AS vm } WHERE {vm.user}=?currentUser AND {vm.status}=?status AND {vm.endVisitTime}>= ?startDate AND {vm.endVisitTime}< ?endDate ORDER BY {vm.visitDate} DESC";
	private static final String MARKET_VISIT_COUNT_QUERY ="SELECT count({vm.pk}) FROM {VisitMaster AS vm} WHERE {vm.status} IN (?status) AND {vm.user}=?currentUser AND {vm.visitPlannedDate} >= ?date ";
	private static final String AVG_TIME_SPENT_QUERY ="SELECT count({c.pk}), sum(DATEDIFF( MINUTE, {c.startVisitTime}, {c.endVisitTime})), {enum.code} FROM {VisitMaster AS vm join CounterVisitMaster as c on {c.visit}= {vm.pk} JOIN EnumerationValue AS enum ON {enum.pk}={c.counterType}} WHERE {vm.user}=?currentUser AND  {c.endVisitTime} BETWEEN ?startDate AND ?endDate group by {enum.code}";

	private static final String LAST_SIX_VISIT_QUERY ="SELECT TOP 6 {c.endVisitTime} FROM {CounterVisitMaster as c} WHERE {c.eydmsCustomer}=?eydmsCustomer and {c.endVisitTime} is not null order by {c.endVisitTime} desc ";
	private static final String LAST_VISTED_ROUTE_QUERY ="SELECT  TOP 1 {vm.visitPlannedDate}, count({c.pk}) FROM {VisitMaster AS vm join CounterVisitMaster as c on {c.visit}= {vm.pk} } WHERE {vm.user}=?currentUser AND {vm.status}=?status and {c.endVisitTime} is not null  and {vm.route}=?route group by {vm.visitPlannedDate} order by {vm.visitPlannedDate} desc ";

	private static final String GET_VISIT_MASTER_BETWEEN_DATES_FOR_SO = "SELECT{"+VisitMasterModel.PK+"} from {"+VisitMasterModel._TYPECODE+"} WHERE {" +VisitMasterModel.USER+"} = ?user AND {"+VisitMasterModel.ENDVISITTIME+"} IS NOT NULL AND {"+VisitMasterModel.VISITPLANNEDDATE+"} >= ?startDate AND {"+VisitMasterModel.VISITPLANNEDDATE+" } <= ?endDate";

	private static final String LAST_VISTED_DATE_QUERY ="select {lastVisitTime} from {EyDmsCustomer as s JOIN CounterVisitMaster as c on {s.lastVisitTime}={c.endVisitTime}} where {c.eydmsCustomer}=?eydmsCustomer";

//	private static final String GET_RECOMMENDED_COUNTER_VISIT_FOR_SO = "SELECT {"+ DJPCounterScoreMasterModel.PK+"} " +
//			"FROM {"+DJPCounterScoreMasterModel._TYPECODE+" AS csm JOIN "+DJPRouteScoreMasterModel._TYPECODE+
//			" AS rsm ON {csm:"+DJPCounterScoreMasterModel.ROUTESCORE+"} = {rsm:"+DJPRouteScoreMasterModel.PK+
//			"} JOIN "+DJPRunMasterModel._TYPECODE+" AS rm ON {rm:"+DJPRunMasterModel.PK+"} = {rsm:"+DJPRouteScoreMasterModel.RUN+
//			"}} WHERE {rm:"+DJPRunMasterModel.PLANDATE+"} >= ?planStartDate AND {rm:"+DJPRunMasterModel.PLANDATE+"} <= ?planEndDate AND {csm:"+DJPCounterScoreMasterModel.CUSTOMER+
//			"} IN({{ SELECT {"+EyDmsCustomerModel.PK+"} FROM {"+EyDmsCustomerModel._TYPECODE+"} WHERE {"+EyDmsCustomerModel.SO+"} =?salesofficer }})";

	private static final String GET_RECOMMENDED_COUNTER_VISIT_FOR_SO = "SELECT {"+ DJPCounterScoreMasterModel.PK+"} " +
			"FROM {"+DJPCounterScoreMasterModel._TYPECODE+" AS csm JOIN "+DJPRouteScoreMasterModel._TYPECODE+
			" AS rsm ON {csm:"+DJPCounterScoreMasterModel.ROUTESCORE+"} = {rsm:"+DJPRouteScoreMasterModel.PK+
			"} JOIN "+DJPRunMasterModel._TYPECODE+" AS rm ON {rm:"+DJPRunMasterModel.PK+"} = {rsm:"+DJPRouteScoreMasterModel.RUN+
			"}} WHERE {rm:"+DJPRunMasterModel.PLANDATE+"} >= ?planStartDate AND {rm:"+DJPRunMasterModel.PLANDATE+"} <= ?planEndDate AND {rm.officer}=?salesofficer ";
	
	private static final String VISIT_FOR_DAY = "SELECT {vm.pk} FROM {VisitMaster AS vm} WHERE {vm.user}=?currentUser AND  ";
	@Resource
	private PaginatedFlexibleSearchService paginatedFlexibleSearchService;
	
	@Resource
	private FlexibleSearchService flexibleSearchService;
	
	@Autowired
	BaseSiteService baseSiteService;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private SearchRestrictionService searchRestrictionService;

	@Autowired
	TerritoryManagementService territoryManagementService;
	
	@Override
	public SearchPageData<VisitMasterModel> getMarketVisitDetails(SearchPageData searchPageData, UserModel user) {
		
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder(MARKET_VISIT_QUERY);
		
		List<VisitStatus> status = new ArrayList<>();
		status.add(VisitStatus.STARTED);
		status.add(VisitStatus.NOT_STARTED);
		LocalDate date = LocalDate.now();
		String checkDate= new Date().toString();
		if(checkDate.contains("GMT")) {
			LocalDateTime dateTime = LocalDateTime.now().plusHours(5).plusMinutes(30);
			date = dateTime.toLocalDate();
		}
		params.put("status", status);
		params.put("currentUser", user);
		params.put("date", date.toString());

		LOG.error("DJP_PLANNED_VIST date  : " + checkDate + "   date send " + date.toString());
		
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		
		final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
		parameter.setSearchPageData(searchPageData);

		query.setResultClassList(Collections.singletonList(VisitMasterModel.class));
		parameter.setFlexibleSearchQuery(query);

		return paginatedFlexibleSearchService.search(parameter);

	}

	@Override
	public VisitMasterModel getCounterList(String id, UserModel user) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder(COUNTER_LIST_QUERY);
		params.put("id", id);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Collections.singletonList(VisitMasterModel.class));
		final SearchResult<VisitMasterModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult().get(0);
	}
	
	@Override
	public SearchPageData<VisitMasterModel> getReviewLogs(SearchPageData searchPageData, UserModel user, Date startDate, Date endDate) {
		
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder(REVIEW_LOGS_QUERY);
		
		VisitStatus status = VisitStatus.COMPLETED;
	
		params.put("currentUser", user);
		params.put("status", status);
		
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		
		final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
		parameter.setSearchPageData(searchPageData);

		query.setResultClassList(Collections.singletonList(VisitMasterModel.class));
		parameter.setFlexibleSearchQuery(query);

		return paginatedFlexibleSearchService.search(parameter);

	}

	@Override
	public Long getCountOfTotalJouneyPlanned(UserModel user) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder(MARKET_VISIT_COUNT_QUERY);
		
		List<VisitStatus> status = new ArrayList<>();
		status.add(VisitStatus.STARTED);
		status.add(VisitStatus.NOT_STARTED);
		
		params.put("status", status);
		params.put("currentUser", user);
		params.put("date", LocalDate.now().toString());
		
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Collections.singletonList(Long.class));
		final SearchResult<Long> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult().get(0);
	}

	@Override
	public List<List<Object>> getAvgTimeSpent(UserModel user, Date startDate, Date endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder(AVG_TIME_SPENT_QUERY);
		
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		params.put("currentUser", user);
		
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Arrays.asList(Integer.class,Long.class,String.class));
		final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<Date> getLastSixCounterVisitDates(EyDmsCustomerModel customer) {
				

		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder(LAST_SIX_VISIT_QUERY);
		params.put("eydmsCustomer", customer);
		
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Collections.singletonList(Date.class));
		final SearchResult<Date> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	
	}

	@Override
	public List<List<Object>> counterVisitedForSelectedRoutes(RouteMasterModel route, UserModel user) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder(LAST_VISTED_ROUTE_QUERY);
		VisitStatus status = VisitStatus.COMPLETED;

		params.put("status", status);
		params.put("currentUser", user);
		params.put("route", route);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Arrays.asList(Date.class, Integer.class));
		final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<VisitMasterModel> getCompletedPlannedVisitsBetweenDatesForSO(final EyDmsUserModel eydmsUserModel , final Date startDate , final Date endDate){

		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_VISIT_MASTER_BETWEEN_DATES_FOR_SO);
		query.addQueryParameter("user",eydmsUserModel);
		query.addQueryParameter("startDate",startDate);
		query.addQueryParameter("endDate",endDate);
		query.addQueryParameter("status",VisitStatus.COMPLETED);

		final SearchResult<VisitMasterModel> searchResult = flexibleSearchService.search(query);

		return searchResult.getResult();
	}


	public Date getLastVisitDate(EyDmsCustomerModel eydmsCustomer) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder(LAST_VISTED_DATE_QUERY);
		params.put("eydmsCustomer", eydmsCustomer);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Date.class));
		query.addQueryParameters(params);
		final SearchResult<Date> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult().get(0);
	}
	
	@Override
	public Integer getVisitCountMTD(EyDmsCustomerModel eydmsCustomer, int month, int year) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT COUNT({endVisitTime}) FROM {CounterVisitMaster AS c} WHERE {c.eydmsCustomer}=?eydmsCustomer AND {c.endVisitTime} IS NOT NULL AND ").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("c.endVisitTime", month, year, params));
		params.put("eydmsCustomer", eydmsCustomer);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Integer.class));
		query.addQueryParameters(params);
		final SearchResult<Integer> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null?searchResult.getResult().get(0):0;
		else
			return 0;
	}

	@Override
	public List<DJPCounterScoreMasterModel> getRecommendedPlanVisitForSalesOfficer(final EyDmsUserModel eydmsUserModel , final Date planStartDate, final Date planEndDate){
		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_RECOMMENDED_COUNTER_VISIT_FOR_SO);
		query.addQueryParameter("salesOfficer",eydmsUserModel);
		query.addQueryParameter("planStartDate",planStartDate);
		query.addQueryParameter("planEndDate",planEndDate);
		final SearchResult<DJPCounterScoreMasterModel> searchResult = flexibleSearchService.search(query);

		return searchResult.getResult();
	}

	@Override
	public Double getDealerOutstandingAmount(String customer) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {totalOutstanding} FROM {CreditAndOutstanding} WHERE {customerCode}=?customer");
		params.put("customer", customer);	
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Double.class));
		query.addQueryParameters(params);
		final SearchResult<Double> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;	
		else
			return 0.0;
	
	}

	@Override
	public List<List<Object>> getCounterSharesForDealerOrRetailer(String userId, int month, int year) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT SUM({c.counterShare}), COUNT({c.pk}) FROM {CounterVisitMaster AS c} WHERE {c.eydmsCustomer}=?userId AND ").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("c.endVisitTime", month+1, year, params));
		params.put("userId", userId);	
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Double.class,Integer.class));
		query.addQueryParameters(params);
		final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);

			return searchResult.getResult();

	}
	
	@Override
	public Double getDealerCreditLimit(String customer) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {creditLimit} FROM {CreditAndOutstanding} WHERE {customerCode}=?customer");
		params.put("customer", customer);	
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Double.class));
		query.addQueryParameters(params);
		final SearchResult<Double> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;	
		else
			return 0.0;
	
	}
	
	@Override
	public List<List<Double>> getOutstandingBucketsForDealer(String customer) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {bucket1},{bucket2},{bucket3},{bucket4},{bucket5},{bucket6},{bucket7},{bucket8},{bucket9},{bucket10} FROM {CreditAndOutstanding} WHERE {customerCode}=?customer");
		params.put("customer", customer);	
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Double.class,Double.class,Double.class,Double.class,Double.class,Double.class,Double.class,Double.class,Double.class,Double.class));
		query.addQueryParameters(params);
		final SearchResult<List<Double>> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();

	}
	
	@Override
	public List<OrderModel> getLastAcceptedOrderForDealer(String customer) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT TOP 1 {pk} FROM {Order} WHERE {user}=?customer AND {orderAcceptedDate} IS NOT NULL ORDER BY {orderAcceptedDate} DESC");
		params.put("customer", customer);	
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(OrderModel.class));
		query.addQueryParameters(params);
		final SearchResult<OrderModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}
	
	@Override
	public List<OrderModel> getLastAcceptedOrderForRetailer(String customer) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT TOP 1 {pk} FROM {Order} WHERE {retailer}=?customer AND {orderAcceptedDate} IS NOT NULL ORDER BY {orderAcceptedDate} DESC");
		params.put("customer", customer);	
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(OrderModel.class));
		query.addQueryParameters(params);
		final SearchResult<OrderModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();

	}
	
	@Override
	public List<VisitMasterModel> getPlannedVisitForToday(UserModel user, String plannedDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("currentUser", user);

		final StringBuilder builder = new StringBuilder(VISIT_FOR_DAY).append(EyDmsDateUtility.getDateRangeClauseQuery("vm.visitPlannedDate", plannedDate, plannedDate, params));
		
		
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Collections.singletonList(VisitMasterModel.class));
		final SearchResult<VisitMasterModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}
  
  @Override
	public Double getTotalOrderGenerated(EyDmsCustomerModel eydmsCustomer,CounterVisitMasterModel counterVisit) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT SUM({totalOrderGenerate}) FROM {OrderRequisitionMaster} WHERE {eydmsCustomer}=?eydmsCustomer and {counterVisit}=?counterVisit");
		params.put("eydmsCustomer", eydmsCustomer);
	  	params.put("counterVisit", counterVisit);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Double.class));
		query.addQueryParameters(params);
		final SearchResult<Double> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null?searchResult.getResult().get(0):0.0;
		else
			return 0.0;
	}
  
  @Override
	public Double getSalesHistoryData(String customerNo, int month, int year, CustomerCategory category,
			BaseSiteModel brand) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT SUM({quantity}) FROM {SalesHistory} WHERE {customerNo}=?customerNo AND {customerCategory}=?category AND {brand}=?brand AND").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("invoiceDate",month,year, params));
		params.put("customerNo", customerNo);	
		params.put("category", category);
		params.put("brand", brand);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Double.class));
		query.addQueryParameters(params);
		final SearchResult<Double> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;	
		else
			return 0.0;
   }
  
  @Override
  public ObjectiveModel findOjectiveById(String objectiveId) {
	  ObjectiveModel model = null;
	  if(objectiveId!=null) {
		  final Map<String, Object> params = new HashMap<String, Object>();
		  final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {Objective} WHERE {objectiveId}=?objectiveId ");
		  params.put("objectiveId", objectiveId);	
		  final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		  query.setResultClassList(Arrays.asList(ObjectiveModel.class));
		  query.addQueryParameters(params);
		  final SearchResult<ObjectiveModel> searchResult = flexibleSearchService.search(query);
		  if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			  model = searchResult.getResult().get(0);
	  }
	  return model;
  } 
  
  @Override
  public List<DJPRouteScoreMasterModel> findAllRouteForPlannedDate(DJPRunMasterModel djpRun) {
	  List<DJPRouteScoreMasterModel> modelList = new ArrayList<DJPRouteScoreMasterModel>();
	  final Map<String, Object> params = new HashMap<String, Object>();
	  final StringBuilder builder = new StringBuilder("SELECT {dr.pk} FROM {DJPRouteScoreMaster as dr} WHERE {dr.run}=?run order by {dr.routesScore} ");
	  params.put("run", djpRun);	
	  final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
	  query.setResultClassList(Arrays.asList(DJPRouteScoreMasterModel.class));
	  query.addQueryParameters(params);
	  final SearchResult<DJPRouteScoreMasterModel> searchResult = flexibleSearchService.search(query);
	  if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
		  modelList = searchResult.getResult();
	  return modelList;
  }

	@Override
	public double getSalesHistoryDataFor360(String customerNo, BaseSiteModel brand, String transactionType, int month, int year) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT SUM({transactionQuantity}) FROM {NirmanMitraSalesHistory} WHERE {toCustAccNumber}=?customerNo AND {brand}=?brand AND {transactionTypeDisp}=?transactionType AND").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("transactionDate",month,year,params));

		params.put("customerNo", customerNo);
		params.put("brand", brand);
		params.put("transactionType", transactionType);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Arrays.asList(Double.class));
		query.addQueryParameters(params);
		final SearchResult<Double> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
		else
			return 0.0;
	}

	@Override
	public Date getLastLiftingDateForDealer(String customerNo, BaseSiteModel brand) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select MAX({invoiceDate}) from {SalesHistory} where {customerNo}=?customerNo AND {brand}=?brand");
		params.put("customerNo", customerNo);
		params.put("brand", brand);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Date.class));
		query.addQueryParameters(params);
		final SearchResult<Date> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult().get(0);
	}

	@Override
	public Double getLastLiftingQuantityForDealer(String customerNo, BaseSiteModel brand, Date maxDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder sql = new StringBuilder("SELECT SUM({quantity}) FROM {SalesHistory} WHERE {customerNo}=?customerNo AND {brand}=?brand AND {invoiceDate}=?maxDate");
		params.put("customerNo", customerNo);
		params.put("brand", brand);
		params.put("maxDate", maxDate);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Arrays.asList(Double.class));
		query.addQueryParameters(params);
		final SearchResult<Double> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
		else
			return 0.0;
	}

	@Override
	public Date getLastLiftingDateForRetailerOrInfluencer(String customerNo, BaseSiteModel brand, String transactionType) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select MAX({transactionDate}) from {NirmanMitraSalesHistory} where {toCustAccNumber}=?customerNo AND {brand}=?brand AND {transactionTypeDisp}=?transactionType");
		params.put("customerNo", customerNo);
		params.put("brand", brand);
		params.put("transactionType", transactionType);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Date.class));
		query.addQueryParameters(params);
		final SearchResult<Date> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult().get(0);
	}
	@Override
	public Double getLastLiftingQuantityForRetailerOrInfluencer(String customerNo, BaseSiteModel brand, Date maxDate, String transactionType) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder sql = new StringBuilder("SELECT TOP 1 {transactionQuantity} FROM {NirmanMitraSalesHistory} WHERE {toCustAccNumber}=?customerNo AND {brand}=?brand AND {transactionTypeDisp}=?transactionType AND {transactionDate} <= ?maxDate ORDER BY {transactionDate} DESC ");
		params.put("customerNo", customerNo);
		params.put("brand", brand);
		params.put("maxDate", maxDate);
		params.put("transactionType", transactionType);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Arrays.asList(Double.class));
		query.addQueryParameters(params);
		final SearchResult<Double> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
		else
			return 0.0;
	}
	@Override
	public Date getLastLiftingDateForRetailerFromOrderReq(EyDmsCustomerModel customerNo, BaseSiteModel brand, String transactionType) {
		final Map<String, Object> params = new HashMap<String, Object>();
		//final StringBuilder builder = new StringBuilder("select MAX({transactionDate}) from {NirmanMitraSalesHistory} where {toCustAccNumber}=?customerNo AND {brand}=?brand AND {transactionTypeDisp}=?transactionType");
		final StringBuilder builder = new StringBuilder("select MAX({o:deliveredDate}) from {OrderRequisition as o} where {o.toCustomer} =?customerNo and {o:status}=?status ");
		params.put("customerNo", customerNo);
		params.put("brand", brand);
		params.put("transactionType", transactionType);
		params.put("status", RequisitionStatus.DELIVERED);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Date.class));
		query.addQueryParameters(params);
		final SearchResult<Date> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult().get(0);
	}
	@Override
	public Double getLastLiftingQuantityForRetailerFromOrderReq(EyDmsCustomerModel customerNo, BaseSiteModel brand, Date maxDate, String transactionType) {
		final Map<String, Object> params = new HashMap<String, Object>();
		//final StringBuilder sql = new StringBuilder("SELECT TOP 1 {transactionQuantity} FROM {NirmanMitraSalesHistory} WHERE {toCustAccNumber}=?customerNo AND {brand}=?brand AND {transactionTypeDisp}=?transactionType AND {transactionDate} <= ?maxDate ORDER BY {transactionDate} DESC ");
		final StringBuilder sql = new StringBuilder("SELECT TOP 1 {o:quantity} FROM {OrderRequisition as o} WHERE {o.toCustomer} =?customerNo AND  {o:status}=?status  AND {o:deliveredDate} <= ?maxDate ORDER BY {o:deliveredDate} DESC ");
		params.put("customerNo", customerNo);
		params.put("brand", brand);
		params.put("maxDate", maxDate);
		params.put("transactionType", transactionType);
		params.put("status", RequisitionStatus.DELIVERED);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Arrays.asList(Double.class));
		query.addQueryParameters(params);
		final SearchResult<Double> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
		else
			return 0.0;
	}
	@Override
	public Date getLastLiftingDateForInfluencerFromPointReq(EyDmsCustomerModel customerNo, BaseSiteModel brand, String transactionType) {
		final Map<String, Object> params = new HashMap<String, Object>();
		//final StringBuilder builder = new StringBuilder("select MAX({transactionDate}) from {NirmanMitraSalesHistory} where {toCustAccNumber}=?customerNo AND {brand}=?brand AND {transactionTypeDisp}=?transactionType");
		final StringBuilder builder = new StringBuilder("select MAX({o:deliveryDate}) from {PointRequisition as o} where {o.requestRaisedFor} =?customerNo and {o:status}=?status ");
		params.put("customerNo", customerNo);
		params.put("brand", brand);
		params.put("transactionType", transactionType);
		params.put("status", PointRequisitionStatus.APPROVED);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Date.class));
		query.addQueryParameters(params);
		final SearchResult<Date> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult().get(0);
	}
	@Override
	public Double getLastLiftingQuantityForInfluencerFromPointReq(EyDmsCustomerModel customerNo, BaseSiteModel brand, Date maxDate, String transactionType) {
		final Map<String, Object> params = new HashMap<String, Object>();
		//final StringBuilder sql = new StringBuilder("SELECT TOP 1 {transactionQuantity} FROM {NirmanMitraSalesHistory} WHERE {toCustAccNumber}=?customerNo AND {brand}=?brand AND {transactionTypeDisp}=?transactionType AND {transactionDate} <= ?maxDate ORDER BY {transactionDate} DESC ");
		final StringBuilder sql = new StringBuilder("SELECT TOP 1 {o:quantity} FROM {PointRequisition as o} WHERE {o.requestRaisedFor} =?customerNo AND  {o:status}=?status and {o:deliveryDate} <= ?maxDate ORDER BY {o:deliveryDate} DESC ");
		params.put("customerNo", customerNo);
		params.put("brand", brand);
		params.put("maxDate", maxDate);
		params.put("transactionType", transactionType);
		params.put("status", PointRequisitionStatus.APPROVED);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Arrays.asList(Double.class));
		query.addQueryParameters(params);
		final SearchResult<Double> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
		else
			return 0.0;
	}

    @Override
    public Integer flaggedDealerCount(List<SubAreaMasterModel> subAreaMasterModels) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT COUNT({c.pk}) FROM {CustomerSubAreaMapping as m join EyDmsCustomer as c on {m.eydmsCustomer}={c.pk}} WHERE {m.subAreaMaster} in (?subAreaMasterModels) and {c.isDealerFlag}=?isDealerFlag and {m.brand}=?brand and {m:isActive}=?isActive and {m:isOtherBrand}=?isOtherBrand");
		params.put("isDealerFlag", Boolean.TRUE);
		params.put("brand", baseSiteService.getCurrentBaseSite());
		params.put("isActive", Boolean.TRUE);
		params.put("isOtherBrand", Boolean.FALSE);
		params.put("subAreaMasterModels",subAreaMasterModels);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Integer.class));
		query.addQueryParameters(params);
		final SearchResult<Integer> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null?searchResult.getResult().get(0):0;
		else
			return 0;
    }

	@Override
	public Integer unFlaggedDealerRequestCount(List<SubAreaMasterModel> subAreaMasterModels) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT COUNT({c.pk}) FROM {CustomerSubAreaMapping as m join EyDmsCustomer as c on {m.eydmsCustomer}={c.pk}} WHERE {m.subAreaMaster} in (?subAreaMasterModels) and {c.isUnFlagRequestRaised}=?isUnFlagRequestRaised and {m.brand}=?brand and {m:isActive}=?isActive and {m:isOtherBrand}=?isOtherBrand");
		params.put("isUnFlagRequestRaised", Boolean.TRUE);
		params.put("subAreaMasterModels",subAreaMasterModels);
		params.put("brand", baseSiteService.getCurrentBaseSite());
		params.put("isActive", Boolean.TRUE);
		params.put("isOtherBrand", Boolean.FALSE);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Integer.class));
		query.addQueryParameters(params);
		final SearchResult<Integer> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null?searchResult.getResult().get(0):0;
		else
			return 0;
	}

	@Override
	public VisitMasterModel updateStatusForApprovalByTsm(String visitId) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder(VISIT_LIST_QUERY);
		params.put("visitId", visitId);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder);
		query.setResultClassList(Collections.singletonList(VisitMasterModel.class));
		query.addQueryParameters(params);
		final SearchResult<VisitMasterModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult() !=null && !searchResult.getResult().isEmpty()? searchResult.getResult().get(0) : null;
	}

	@Override
	public VisitMasterModel updateStatusForRejectedByTsm(String visitId) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder(VISIT_LIST_QUERY);
		params.put("visitId", visitId);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder);
		query.setResultClassList(Collections.singletonList(VisitMasterModel.class));
		query.addQueryParameters(params);
		final SearchResult<VisitMasterModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult() !=null && !searchResult.getResult().isEmpty()? searchResult.getResult().get(0) : null;
	}

	@Override
	public Double getAvgSalesDataForDealer(String customerNo, Date startDate, Date endDate, CustomerCategory category,
									  BaseSiteModel brand) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT SUM({billingPrice}) FROM {SalesHistory} WHERE {customerNo}=?customerNo AND {customerCategory}=?category AND {brand}=?brand AND {invoiceDate} <= ?startDate AND {invoiceDate} >= ?endDate");
		params.put("customerNo", customerNo);
		params.put("category", category);
		params.put("brand", brand);
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Double.class));
		query.addQueryParameters(params);
		final SearchResult<Double> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
		else
			return 0.0;
	}

	@Override
	public Double getSalesHistoryDataForDealer(String customerNo, int month, int year, CustomerCategory category, BaseSiteModel brand) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT SUM({quantity}) FROM {SalesHistory} WHERE {customerNo}=?customerNo AND {customerCategory}=?category AND {brand}=?brand AND").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("invoiceDate", month, year, params));
		params.put("customerNo", customerNo);
		params.put("category", category);
		params.put("brand", brand);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Double.class));
		query.addQueryParameters(params);
		final SearchResult<Double> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
		else
			return 0.0;
	}

	@Override
	public Double getSalesTargetFor360(String customerNo, String customerType, int month, int year) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {salesTarget} FROM {TargetSales} WHERE {customerNo}=?customerNo AND {customerType}=?customerType AND {month}=?month AND {year}=?year");
		params.put("customerNo", customerNo);
		params.put("customerType", customerType);
		params.put("month", month+1);
		params.put("year", year);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Double.class));
		query.addQueryParameters(params);
		final SearchResult<Double> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
		else
			return 0.0;
	}

	@Override
	public Double getOrderCapturedForCounter(String customerNo, Date startDate, Date endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT SUM({oe.quantityInMt}) FROM {OrderEntry AS oe JOIN Order AS o ON {o.pk} = {oe.order} } WHERE {o.user}=?customerNo AND {o.orderAcceptedDate} BETWEEN ?startDate AND ?endDate AND {o.erpOrderNumber} IS NOT NULL AND {oe.status}!=?cancelled");
		OrderStatus cancelled = OrderStatus.CANCELLED;
		params.put("customerNo", customerNo);
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		params.put("cancelled", cancelled);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Double.class));
		query.addQueryParameters(params);
		final SearchResult<Double> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
		else
			return 0.0;
	}

	@Override
	public Double getPreviousCounterPotentialForCounter(Date currentDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT TOP 1 {cvm.totalSale} FROM {CounterVisitMaster AS cvm JOIN VisitMaster AS vm ON {cvm.visit}= {vm.pk}} WHERE {vm.visitPlannedDate} < ?currentDate AND {vm.status}=?completed  ");
		VisitStatus completed = VisitStatus.COMPLETED;
		params.put("currentDate", currentDate);
		params.put("completed", completed);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Double.class));
		query.addQueryParameters(params);
		final SearchResult<Double> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
		else
			return 0.0;
	}
	
	@Override
	public List<List<Object>> getAllRoutesForSO(List<SubAreaMasterModel> subAreas) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {routeId},{routeName} FROM {RouteMaster} WHERE {subAreaMaster} IN (?subAreas) and {brand}=?brand and {active}=?active ");
		params.put("subAreas", subAreas);
		params.put("brand", baseSiteService.getCurrentBaseSite().getUid());
		params.put("active", Boolean.TRUE);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Arrays.asList(String.class,String.class));
		final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public Integer getDealerToRetailerNetwork(String customerNo) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select count({retailerCustNo}) from {DealerRetailerMap} where {dealerCustNo}=?customerNo and {status} =?status");
		params.put("customerNo", customerNo);
		params.put("status",true);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Integer.class));
		query.addQueryParameters(params);
		final SearchResult<Integer> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null?searchResult.getResult().get(0):0;
		else
			return 0;
	}

	@Override
	public Integer getDealerToInfluencerNetwork(String customerNo) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select count({influencerCustNo}) from {DealerInfluencerMap} where {dealerCustNo}=?customerNo and {status} =?status");
		params.put("customerNo", customerNo);
		params.put("status",true);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Integer.class));
		query.addQueryParameters(params);
		final SearchResult<Integer> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null?searchResult.getResult().get(0):0;
		else
			return 0;
	}

	@Override
	public Integer getRetailerToInfluencerNetwork(String customerNo) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select count({influencerCustNo}) from {RetailerInfluencerMap} where {retailerCustNo}=?customerNo and {status} =?status");
		params.put("customerNo", customerNo);
		params.put("status",true);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Integer.class));
		query.addQueryParameters(params);
		final SearchResult<Integer> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null?searchResult.getResult().get(0):0;
		else
			return 0;
	}
	
	@Override
	public Double getOutstandingAmountBetweenDates(String customerCode, Date startdate, Date endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {outstandingAmount} FROM {OutstandingHistory} WHERE {customerCode}=?customerCode AND {updatedDate} BETWEEN ?startDate AND ?endDate");
		params.put("customerCode", customerCode);
		params.put("startdate", startdate);
		params.put("endDate", endDate);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Double.class));
		query.addQueryParameters(params);
		final SearchResult<Double> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
		else
			return 0.0;
	}

	@Override
	public List<DealerRetailerMapModel> getDealerRetailerMappingRecords(Date startDate, Date endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {DealerRetailerMap} where {lastLiftingDate} not between ?startDate and ?endDate");
		params.put("startDate", startDate);
		params.put("endDate",endDate);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(DealerRetailerMapModel.class));
		query.addQueryParameters(params);
		final SearchResult<DealerRetailerMapModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<DealerInfluencerMapModel> getDealerInfluMappingRecords(Date startDate, Date endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {DealerInfluencerMap} where {lastLiftingDate} not between ?startDate and ?endDate");
		params.put("startDate", startDate);
		params.put("endDate",endDate);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(DealerInfluencerMapModel.class));
		query.addQueryParameters(params);
		final SearchResult<DealerInfluencerMapModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<RetailerInfluencerMapModel> getRetailerInfluMappingRecords(Date startDate, Date endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {RetailerInfluencerMap} where {lastLiftingDate} not between ?startDate and ?endDate");
		params.put("startDate", startDate);
		params.put("endDate",endDate);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(RetailerInfluencerMapModel.class));
		query.addQueryParameters(params);
		final SearchResult<RetailerInfluencerMapModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<List<String>> getMappingRecordsByTransType() {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select distinct{fromCustAccNumber},{toCustAccNumber},{transactionTypeDisp} from {NirmanMitraSalesHistory} where {transactionType} ='DR' or {transactionType} ='DM'");
		//params.put("transactionType", "DR");
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Arrays.asList(String.class,String.class,String.class));
		final SearchResult<List<String>> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<VisitMasterModel> getAllVisit(UserModel user, Date startDate, Date endDate) {
		
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder(REVIEW_LOGS_QUERY);
		
		VisitStatus status = VisitStatus.COMPLETED;
	
		params.put("currentUser", user);
		params.put("status", status);
		
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(VisitMasterModel.class));
		query.addQueryParameters(params);
		final SearchResult<VisitMasterModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}
	
	@Override
	public Integer getVisitCountBetweenDates(EyDmsCustomerModel eydmsCustomer, Date startDate, Date endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT COUNT({endVisitTime}) FROM {CounterVisitMaster AS c} WHERE {c.eydmsCustomer}=?eydmsCustomer AND {c.endVisitTime} IS NOT NULL AND {c.endVisitTime} BETWEEN ?startDate AND ?endDate"); 
		params.put("eydmsCustomer", eydmsCustomer);
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Integer.class));
		query.addQueryParameters(params);
		final SearchResult<Integer> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null?searchResult.getResult().get(0):0;
		else
			return 0;
	}

	@Override
	public SecurityDepositStatus getSecurityDepositStatusForDealer(String customerNo) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT  {securityDepositStatus} FROM {CreditAndOutstanding} WHERE {customerCode} =?customerNo"); 
		params.put("customerNo", customerNo);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(SecurityDepositStatus.class));
		query.addQueryParameters(params);
		final SearchResult<SecurityDepositStatus> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null?searchResult.getResult().get(0):null;
		else
			return null;
	}

	@Override
	public Double getSalesHistoryDataForDealerList(List<String> customerNos, int monthValue, int year,
			CustomerCategory category, BaseSiteModel brand) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT SUM({quantity}) FROM {SalesHistory} WHERE {customerNo} IN (?customerNos) AND {customerCategory}=?category AND {brand}=?brand AND").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("invoiceDate",monthValue,year, params));
		params.put("customerNos", customerNos);	
		params.put("category", category);
		params.put("brand", brand);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Double.class));
		query.addQueryParameters(params);
		final SearchResult<Double> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;	
		else
			return 0.0;
	}


	@Override
	public Double getDailyAverageSales(String customer) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {dailyAverageSales} FROM {CreditAndOutstanding} WHERE {customerCode}=?customer");
		params.put("customer", customer);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Double.class));
		query.addQueryParameters(params);
		final SearchResult<Double> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
		else
			return 0.0;

	}

	@SuppressWarnings("unchecked")
	@Override
	public Integer getPendingApprovalVisitsCountForTsmorRh(EyDmsUserModel currentUser) {
		return (Integer) sessionService.executeInLocalView(new SessionExecutionBody() {
			@Override
			public Integer execute() {
				try {
					searchRestrictionService.disableSearchRestrictions();
					final Map<String, Object> params = new HashMap<String, Object>();
					final StringBuilder builder = new StringBuilder("SELECT count({v:pk}) from {VisitMaster as v} where {v:approvalStatus} = ?approvalStatus ");
					params.put("approvalStatus", ApprovalStatus.PENDING_APPROVAL);
					if(currentUser.getUserType().getCode().equals("TSM")) {
						builder.append(" and {v:districtMaster} in (?currentDistricts)");
						List<DistrictMasterModel> currentDistrictsList = territoryManagementService.getCurrentDistrict().stream().collect(Collectors.toList());
						params.put("currentDistricts",currentDistrictsList);
					} else if (currentUser.getUserType().getCode().equals("RH")) {
						builder.append(" and {v:regionMaster} in (?currentRegions)");
						List<RegionMasterModel> currentRegionsList = territoryManagementService.getCurrentRegion().stream().collect(Collectors.toList());
						params.put("currentRegions",currentRegionsList);
					}

					final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
					query.setResultClassList(Arrays.asList(Integer.class));
					query.addQueryParameters(params);
					final SearchResult<Integer> searchResult = flexibleSearchService.search(query);
					if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
						return searchResult.getResult().get(0)!=null?searchResult.getResult().get(0):0;
					else
						return 0;
				} finally {
					searchRestrictionService.enableSearchRestrictions();
				}
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public SearchPageData<VisitMasterModel> getReviewLogsForTSM(SearchPageData searchPageData, EyDmsUserModel user, Date startDate, Date endDate, String searchKey) {
		return (SearchPageData<VisitMasterModel>) sessionService.executeInLocalView(new SessionExecutionBody() {
			@Override
			public SearchPageData<VisitMasterModel> execute() {
				try {
					searchRestrictionService.disableSearchRestrictions();
					final Map<String, Object> params = new HashMap<String, Object>();
					final StringBuilder builder = new StringBuilder("SELECT {vm.pk} FROM {VisitMaster AS vm JOIN EyDmsUser as u on {vm:user}={u:pk}} WHERE {vm.user}!=?currentUser AND {u:userType} = ?userType AND {vm.status}=?status AND {vm.endVisitTime}>= ?startDate AND {vm.endVisitTime}< ?endDate AND {vm:districtMaster} in (?districtMastersList) ");

					VisitStatus status = VisitStatus.COMPLETED;
					List<DistrictMasterModel> currentDistrictsList = territoryManagementService.getCurrentDistrict().stream().collect(Collectors.toList());
					if(searchKey!=null && !searchKey.isEmpty()) {
						builder.append("AND {u:name} LIKE ?searchKey ");
						params.put("searchKey","%"+searchKey+"%");
					}

					builder.append("ORDER BY {vm.visitDate} DESC");

					params.put("currentUser", user);
					params.put("status", status);

					params.put("startDate", startDate);
					params.put("endDate", endDate);
					params.put("districtMastersList",currentDistrictsList);
					params.put("userType",EyDmsUserType.SO);


					final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
					query.addQueryParameters(params);

					final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
					parameter.setSearchPageData(searchPageData);

					query.setResultClassList(Collections.singletonList(VisitMasterModel.class));
					parameter.setFlexibleSearchQuery(query);

					return paginatedFlexibleSearchService.search(parameter);
				} finally {
					searchRestrictionService.enableSearchRestrictions();
				}
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public SearchPageData<VisitMasterModel> getReviewLogsForRH(SearchPageData searchPageData, EyDmsUserModel user, Date startDate, Date endDate, String searchKey) {
		return (SearchPageData<VisitMasterModel>) sessionService.executeInLocalView(new SessionExecutionBody() {
			@Override
			public SearchPageData<VisitMasterModel> execute() {
				try {
					searchRestrictionService.disableSearchRestrictions();
					final Map<String, Object> params = new HashMap<String, Object>();
					final StringBuilder builder = new StringBuilder("SELECT {vm.pk} FROM {VisitMaster AS vm JOIN EyDmsUser as u on {vm:user}={u:pk}} WHERE {vm.user}!=?currentUser AND {u:userType} = ?userType AND {vm.status}=?status AND {vm.endVisitTime}>= ?startDate AND {vm.endVisitTime}< ?endDate AND {vm:regionMaster} in (?regionMastersList) ");

					VisitStatus status = VisitStatus.COMPLETED;
					List<RegionMasterModel> currentRegionsList = territoryManagementService.getCurrentRegion().stream().collect(Collectors.toList());
					if(searchKey!=null && !searchKey.isEmpty()) {
						builder.append("AND {u:name} LIKE ?searchKey ");
						params.put("searchKey","%"+searchKey+"%");
					}

					builder.append("ORDER BY {vm.visitDate} DESC");

					params.put("currentUser", user);
					params.put("status", status);

					params.put("startDate", startDate);
					params.put("endDate", endDate);
					params.put("regionMastersList",currentRegionsList);
					params.put("userType",EyDmsUserType.SO);


					final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
					query.addQueryParameters(params);

					final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
					parameter.setSearchPageData(searchPageData);

					query.setResultClassList(Collections.singletonList(VisitMasterModel.class));
					parameter.setFlexibleSearchQuery(query);

					return paginatedFlexibleSearchService.search(parameter);
				} finally {
					searchRestrictionService.enableSearchRestrictions();
				}
			}
		});
	}

	@Override
	public List<List<Object>> getCompletedPlannedVisitsByApprovalStatus() {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder();
		builder.append("SELECT count({v:pk}), {v:approvalStatus} from {VisitMaster as v} where "
				+ "{v:endVisitTime} is not null and " + EyDmsDateUtility.getMtdClauseQuery("v:visitPlannedDate", params));
		builder.append(" group by {v:approvalStatus}");
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Arrays.asList(Integer.class, ApprovalStatus.class));
		final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
		List<List<Object>> result = searchResult.getResult();
		return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
	}

	@Override
	public List<VisitMasterModel> getVisitsForDJPUnflaggedSLA(Date date){
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder();
		builder.append("SELECT {v:pk} from {VisitMaster as v} where {v:endVisitTime} < ?date and {v:approvalStatus} =?status");
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		params.put("date",date);
		params.put("status",ApprovalStatus.PENDING_APPROVAL);
		query.addQueryParameters(params);
		final SearchResult<VisitMasterModel> searchResult = flexibleSearchService.search(query);
		return (searchResult != null && !searchResult.getResult().isEmpty()) ? searchResult.getResult() : Collections.emptyList();
	}

}
