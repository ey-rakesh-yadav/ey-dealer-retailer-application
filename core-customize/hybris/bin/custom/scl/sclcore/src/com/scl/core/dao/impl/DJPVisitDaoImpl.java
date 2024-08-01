package com.scl.core.dao.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.TerritoryManagementDao;
import com.scl.core.enums.*;
import com.scl.core.region.dao.DistrictMasterDao;
import com.scl.core.services.TerritoryManagementService;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;

import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.dao.DJPVisitDao;
import com.scl.core.model.*;
import com.scl.core.utility.SclDateUtility;

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
	@Autowired
	UserService userService;
	
	private static final String MARKET_VISIT_QUERY ="SELECT {vm.pk} FROM {VisitMaster AS vm} WHERE {vm.status} IN (?status) AND {vm.user}=?currentUser AND {vm.visitPlannedDate} >= ?date ";
	private static final String COUNTER_LIST_QUERY ="SELECT {vm.pk} FROM {VisitMaster AS vm} WHERE {vm.pk}=?id";

	private static final String VISIT_LIST_QUERY ="SELECT {vm.pk} FROM {VisitMaster AS vm} WHERE {vm.pk}=?visitId";

	private static final String REVIEW_LOGS_QUERY ="SELECT {vm.pk} FROM {VisitMaster AS vm } WHERE {vm.user}=?currentUser AND {vm.status}=?status AND {vm.endVisitTime}>= ?startDate AND {vm.endVisitTime}< ?endDate ORDER BY {vm.endVisitTime} DESC";
	private static final String MARKET_VISIT_COUNT_QUERY ="SELECT count({vm.pk}) FROM {VisitMaster AS vm} WHERE {vm.status} IN (?status) AND {vm.user}=?currentUser AND {vm.visitPlannedDate} >= ?date ";
	private static final String AVG_TIME_SPENT_QUERY ="SELECT count({c.pk}), sum(DATEDIFF( MINUTE, {c.startVisitTime}, {c.endVisitTime})), {enum.code} FROM {VisitMaster AS vm join CounterVisitMaster as c on {c.visit}= {vm.pk} JOIN EnumerationValue AS enum ON {enum.pk}={c.counterType}} WHERE {vm.user}=?currentUser AND  {c.endVisitTime} BETWEEN ?startDate AND ?endDate group by {enum.code}";

	private static final String LAST_SIX_VISIT_QUERY ="SELECT TOP 6 {c.endVisitTime} FROM {CounterVisitMaster as c} WHERE {c.sclCustomer}=?sclCustomer and {c.endVisitTime} is not null order by {c.endVisitTime} desc ";
	private static final String LAST_VISTED_ROUTE_QUERY ="SELECT  TOP 1 {vm.visitPlannedDate}, count({c.pk}) FROM {VisitMaster AS vm join CounterVisitMaster as c on {c.visit}= {vm.pk} } WHERE {vm.user}=?currentUser AND {vm.status}=?status and {c.endVisitTime} is not null  and {vm.route}=?route group by {vm.visitPlannedDate} order by {vm.visitPlannedDate} desc ";

	private static final String GET_VISIT_MASTER_BETWEEN_DATES_FOR_SO = "SELECT{"+VisitMasterModel.PK+"} from {"+VisitMasterModel._TYPECODE+"} WHERE {" +VisitMasterModel.USER+"} = ?user AND {"+VisitMasterModel.ENDVISITTIME+"} IS NOT NULL AND {"+VisitMasterModel.VISITPLANNEDDATE+"} >= ?startDate AND {"+VisitMasterModel.VISITPLANNEDDATE+" } <= ?endDate";

	private static final String LAST_VISTED_DATE_QUERY ="select {lastVisitTime} from {SclCustomer as s JOIN CounterVisitMaster as c on {s.lastVisitTime}={c.endVisitTime}} where {c.sclCustomer}=?sclCustomer";

//	private static final String GET_RECOMMENDED_COUNTER_VISIT_FOR_SO = "SELECT {"+ DJPCounterScoreMasterModel.PK+"} " +
//			"FROM {"+DJPCounterScoreMasterModel._TYPECODE+" AS csm JOIN "+DJPRouteScoreMasterModel._TYPECODE+
//			" AS rsm ON {csm:"+DJPCounterScoreMasterModel.ROUTESCORE+"} = {rsm:"+DJPRouteScoreMasterModel.PK+
//			"} JOIN "+DJPRunMasterModel._TYPECODE+" AS rm ON {rm:"+DJPRunMasterModel.PK+"} = {rsm:"+DJPRouteScoreMasterModel.RUN+
//			"}} WHERE {rm:"+DJPRunMasterModel.PLANDATE+"} >= ?planStartDate AND {rm:"+DJPRunMasterModel.PLANDATE+"} <= ?planEndDate AND {csm:"+DJPCounterScoreMasterModel.CUSTOMER+
//			"} IN({{ SELECT {"+SclCustomerModel.PK+"} FROM {"+SclCustomerModel._TYPECODE+"} WHERE {"+SclCustomerModel.SO+"} =?salesofficer }})";

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
	@Resource
	TerritoryManagementDao territoryManagementDao;
	@Resource
	DistrictMasterDao districtMasterDao;
	
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
		LOG.info(String.format("DJP_PLANNED_VIST Query::%s",query));
		final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
		parameter.setSearchPageData(searchPageData);

		query.setResultClassList(Collections.singletonList(VisitMasterModel.class));
		parameter.setFlexibleSearchQuery(query);
		LOG.info("Planned Visits :: "+query);
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
		LOG.info(String.format("getCountOfTotalJouneyPlanned ::%s",query));
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
	public List<Date> getLastSixCounterVisitDates(SclCustomerModel customer) {
				

		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder(LAST_SIX_VISIT_QUERY);
		params.put("sclCustomer", customer);
		
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
	public List<VisitMasterModel> getCompletedPlannedVisitsBetweenDatesForSO(final SclUserModel sclUserModel , final Date startDate , final Date endDate){

		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_VISIT_MASTER_BETWEEN_DATES_FOR_SO);
		query.addQueryParameter("user",sclUserModel);
		query.addQueryParameter("startDate",startDate);
		query.addQueryParameter("endDate",endDate);
		query.addQueryParameter("status",VisitStatus.COMPLETED);

		final SearchResult<VisitMasterModel> searchResult = flexibleSearchService.search(query);

		return searchResult.getResult();
	}


	public Date getLastVisitDate(SclCustomerModel sclCustomer) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder(LAST_VISTED_DATE_QUERY);
		params.put("sclCustomer", sclCustomer);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Date.class));
		query.addQueryParameters(params);
		final SearchResult<Date> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult().get(0);
	}
	
	@Override
	public Integer getVisitCountMTD(SclCustomerModel sclCustomer, int month, int year) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT COUNT({endVisitTime}) FROM {CounterVisitMaster AS c} WHERE {c.sclCustomer}=?sclCustomer AND {c.endVisitTime} IS NOT NULL AND ").append(SclDateUtility.getDateClauseQueryByMonthYear("c.endVisitTime", month, year, params));
		params.put("sclCustomer", sclCustomer);
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
	public List<DJPCounterScoreMasterModel> getRecommendedPlanVisitForSalesOfficer(final SclUserModel sclUserModel , final Date planStartDate, final Date planEndDate){
		final FlexibleSearchQuery query = new FlexibleSearchQuery(GET_RECOMMENDED_COUNTER_VISIT_FOR_SO);
		query.addQueryParameter("salesOfficer",sclUserModel);
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
    public List<List<Object>> getDealerOutstandingAmount(List<SclCustomerModel> sclCustomerModels) {
		try{
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {customerCode},sum({totalOutstanding}) FROM {CreditAndOutstanding} WHERE {customerCode} in (?customer) group by {customerCode} ");
		//params.put("customer", customer);
		List<String> customerNos=new ArrayList<>();
		if(CollectionUtils.isNotEmpty(sclCustomerModels)){
			for (SclCustomerModel sclCustomerModel : sclCustomerModels) {
				customerNos.add(sclCustomerModel.getUid());
			}
			params.put("customer",customerNos);
		}
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Arrays.asList(String.class, Double.class));
		final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
		List<List<Object>> result = searchResult.getResult();
		return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
	} catch (IndexOutOfBoundsException e) {
		throw new IndexOutOfBoundsException(String.valueOf(e));
	}
    }

    @Override
	public List<List<Object>> getCounterSharesForDealerOrRetailer(String userId, int month, int year) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT SUM({c.counterShare}), COUNT({c.pk}) FROM {CounterVisitMaster AS c} WHERE {c.sclCustomer}=?userId AND ").append(SclDateUtility.getDateClauseQueryByMonthYear("c.endVisitTime", month, year, params));
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

		final StringBuilder builder = new StringBuilder(VISIT_FOR_DAY).append(SclDateUtility.getDateRangeClauseQuery("vm.visitPlannedDate", plannedDate, plannedDate, params));
		
		
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Collections.singletonList(VisitMasterModel.class));
		final SearchResult<VisitMasterModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}
  
  @Override
	public Double getTotalOrderGenerated(SclCustomerModel sclCustomer,CounterVisitMasterModel counterVisit) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT SUM({totalOrderGenerate}) FROM {OrderRequisitionMaster} WHERE {sclCustomer}=?sclCustomer and {counterVisit}=?counterVisit");
		params.put("sclCustomer", sclCustomer);
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
		final StringBuilder builder = new StringBuilder("SELECT SUM({quantity}) FROM {SalesHistory} WHERE {customerNo}=?customerNo AND {customerCategory}=?category AND {brand}=?brand AND").append(SclDateUtility.getDateClauseQueryByMonthYear("invoiceDate",month,year, params));
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
		sql.append("SELECT SUM({transactionQuantity}) FROM {NirmanMitraSalesHistory} WHERE {toCustAccNumber}=?customerNo AND {brand}=?brand AND {transactionTypeDisp}=?transactionType AND").append(SclDateUtility.getDateClauseQueryByMonthYear("transactionDate",month,year,params));

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
	public Date getLastLiftingDateForRetailerFromOrderReq(SclCustomerModel customerNo, BaseSiteModel brand, String transactionType,List<String> subAreaList,List<String> districtList) {
		final Map<String, Object> params = new HashMap<String, Object>();
		//final StringBuilder builder = new StringBuilder("select MAX({transactionDate}) from {NirmanMitraSalesHistory} where {toCustAccNumber}=?customerNo AND {brand}=?brand AND {transactionTypeDisp}=?transactionType");
		final StringBuilder builder = new StringBuilder("select MAX({o:deliveredDate}) from {OrderRequisition as o} where {o.toCustomer} =?customerNo and {o:status}=?status ");
		params.put("customerNo", customerNo);
		params.put("brand", brand);
		params.put("transactionType", transactionType);
		params.put("status", RequisitionStatus.DELIVERED);
		List<DistrictMasterModel> list = new ArrayList<>();
		List<SubAreaMasterModel> list1 = new ArrayList<>();

		if(CollectionUtils.isNotEmpty(districtList)){
			for (String code : districtList) {
				list.add(districtMasterDao.findByCode(code));
			}
			params.put("doList", list);
			builder.append(" and {o.districtMaster} in (?doList) ");
		}
		if(CollectionUtils.isNotEmpty(subAreaList)){
			for (String id : subAreaList) {
				list1.add(territoryManagementDao.getTerritoryById(id));
			}
			params.put("subAreaList", list1);
			builder.append(" and {o.subAreaMaster} in (?subAreaList) ");
		}
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Date.class));
		query.addQueryParameters(params);
		final SearchResult<Date> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult().get(0);
	}
	@Override
	public Double getLastLiftingQuantityForRetailerFromOrderReq(SclCustomerModel customerNo, BaseSiteModel brand, Date maxDate, String transactionType,List<String> subAreaList,List<String> districtList) {
		final Map<String, Object> params = new HashMap<String, Object>();
		//final StringBuilder sql = new StringBuilder("SELECT TOP 1 {transactionQuantity} FROM {NirmanMitraSalesHistory} WHERE {toCustAccNumber}=?customerNo AND {brand}=?brand AND {transactionTypeDisp}=?transactionType AND {transactionDate} <= ?maxDate ORDER BY {transactionDate} DESC ");
		final StringBuilder sql = new StringBuilder("SELECT TOP 1 {o:quantity} FROM {OrderRequisition as o} WHERE {o.toCustomer} =?customerNo AND  {o:status}=?status  AND {o:deliveredDate} <= ?maxDate ");
		params.put("customerNo", customerNo);
		params.put("brand", brand);
		params.put("maxDate", maxDate);
		params.put("transactionType", transactionType);
		params.put("status", RequisitionStatus.DELIVERED);
		List<DistrictMasterModel> list = new ArrayList<>();
		List<SubAreaMasterModel> list1 = new ArrayList<>();

		if(CollectionUtils.isNotEmpty(districtList)){
			for (String code : districtList) {
				list.add(districtMasterDao.findByCode(code));
			}
			params.put("doList", list);
			sql.append(" and {o.districtMaster} in (?doList) ");
		}
		if(CollectionUtils.isNotEmpty(subAreaList)){
			for (String id : subAreaList) {
				list1.add(territoryManagementDao.getTerritoryById(id));
			}
			params.put("subAreaList", list1);
			sql.append(" and {o.subAreaMaster} in (?subAreaList) ");
		}
		sql.append(" ORDER BY {o:deliveredDate} DESC  ");
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
	public Date getLastLiftingDateForInfluencerFromPointReq(SclCustomerModel customerModel, BaseSiteModel brand, String transactionType,List<String> subAreaList,List<String> districtList) {
		final Map<String, Object> params = new HashMap<String, Object>();
		//final StringBuilder builder = new StringBuilder("select MAX({transactionDate}) from {NirmanMitraSalesHistory} where {toCustAccNumber}=?customerNo AND {brand}=?brand AND {transactionTypeDisp}=?transactionType");
		final StringBuilder builder = new StringBuilder("select MAX({o:deliveryDate}) from {PointRequisition as o} where {o.requestRaisedFor} =?customerModel and {o:status}=?status ");
		params.put("customerModel", customerModel);
//		params.put("brand", brand);
//		params.put("transactionType", transactionType);
		params.put("status", PointRequisitionStatus.APPROVED);
		List<DistrictMasterModel> list = new ArrayList<>();
		List<SubAreaMasterModel> list1 = new ArrayList<>();

		if(CollectionUtils.isNotEmpty(districtList)){
			for (String code : districtList) {
				list.add(districtMasterDao.findByCode(code));
			}
			params.put("doList", list);
			builder.append(" and {o.districtMaster} in (?doList) ");
		}
		if(CollectionUtils.isNotEmpty(subAreaList)){
			for (String id : subAreaList) {
				list1.add(territoryManagementDao.getTerritoryById(id));
			}
			params.put("subAreaList", list1);
			builder.append(" and {o.subAreaMaster} in (?subAreaList) ");
		}
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Date.class));
		query.addQueryParameters(params);
		final SearchResult<Date> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult().get(0);
	}
	@Override
	public Double getLastLiftingQuantityForInfluencerFromPointReq(SclCustomerModel customerNo, BaseSiteModel brand, Date maxDate, String transactionType,List<String> subAreaList,List<String> districtList) {
		final Map<String, Object> params = new HashMap<String, Object>();
		//final StringBuilder sql = new StringBuilder("SELECT TOP 1 {transactionQuantity} FROM {NirmanMitraSalesHistory} WHERE {toCustAccNumber}=?customerNo AND {brand}=?brand AND {transactionTypeDisp}=?transactionType AND {transactionDate} <= ?maxDate ORDER BY {transactionDate} DESC ");
		final StringBuilder sql = new StringBuilder("SELECT TOP 1 {o:quantity} FROM {PointRequisition as o} WHERE {o.requestRaisedFor} =?customerNo AND  {o:status}=?status and {o:deliveryDate} <= ?maxDate  ");
		params.put("customerNo", customerNo);
		params.put("brand", brand);
		params.put("maxDate", maxDate);
		params.put("transactionType", transactionType);
		params.put("status", PointRequisitionStatus.APPROVED);
		List<DistrictMasterModel> list = new ArrayList<>();
		List<SubAreaMasterModel> list1 = new ArrayList<>();

		if(CollectionUtils.isNotEmpty(districtList)){
			for (String code : districtList) {
				list.add(districtMasterDao.findByCode(code));
			}
			params.put("doList", list);
			sql.append(" and {o.districtMaster} in (?doList) ");
		}
		if(CollectionUtils.isNotEmpty(subAreaList)){
			for (String id : subAreaList) {
				list1.add(territoryManagementDao.getTerritoryById(id));
			}
			params.put("subAreaList", list1);
			sql.append(" and {o.subAreaMaster} in (?subAreaList) ");
		}
		sql.append("ORDER BY {o:deliveryDate} DESC ");
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
		final StringBuilder builder = new StringBuilder("SELECT COUNT({c.pk}) FROM {CustomerSubAreaMapping as m join SclCustomer as c on {m.sclCustomer}={c.pk}} WHERE {m.subAreaMaster} in (?subAreaMasterModels) and {c.isDealerFlag}=?isDealerFlag and {m.brand}=?brand and {m:isActive}=?isActive and {m:isOtherBrand}=?isOtherBrand");
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
		final StringBuilder builder = new StringBuilder("SELECT COUNT({c.pk}) FROM {CustomerSubAreaMapping as m join SclCustomer as c on {m.sclCustomer}={c.pk}} WHERE {m.subAreaMaster} in (?subAreaMasterModels) and {c.isUnFlagRequestRaised}=?isUnFlagRequestRaised and {m.brand}=?brand and {m:isActive}=?isActive and {m:isOtherBrand}=?isOtherBrand");
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
		final StringBuilder builder = new StringBuilder("SELECT SUM({quantity}) FROM {SalesHistory} WHERE {customerNo}=?customerNo AND {customerCategory}=?category AND {brand}=?brand AND").append(SclDateUtility.getDateClauseQueryByMonthYear("invoiceDate", month, year, params));
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
	public Double getOrderCapturedForCounter(SclCustomerModel sclCustomerModel, Date startDate, Date endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder =new StringBuilder();
		if((sclCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))){
			builder.append("SELECT SUM({oe.quantityInMt}) FROM {OrderEntry AS oe JOIN Order AS o ON {o.pk} = {oe.order} } " +
				"WHERE {o.user}=?sclCustomerModel AND {o.orderAcceptedDate} >=?startDate AND  {o.orderAcceptedDate}< ?endDate AND {o.erpOrderNumber} IS NOT NULL AND {oe.status}!=?cancelled");
		}
		if((sclCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
			builder.append("SELECT SUM({oe.quantityInMt}) FROM {OrderEntry AS oe JOIN Order AS o ON {o.pk} = {oe.order} } " +
					"WHERE {oe.retailer}=?sclCustomerModel AND {o.orderAcceptedDate} >=?startDate AND  {o.orderAcceptedDate}< ?endDate AND {o.erpOrderNumber} IS NOT NULL AND {oe.status}!=?cancelled");
		}
		OrderStatus cancelled = OrderStatus.CANCELLED;
		params.put("sclCustomerModel", sclCustomerModel);
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
	public Integer getVisitCountBetweenDates(SclCustomerModel sclCustomer, Date startDate, Date endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT COUNT({endVisitTime}) FROM {CounterVisitMaster AS c} WHERE {c.sclCustomer}=?sclCustomer AND {c.endVisitTime} IS NOT NULL AND {c.endVisitTime} BETWEEN ?startDate AND ?endDate"); 
		params.put("sclCustomer", sclCustomer);
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
		final StringBuilder builder = new StringBuilder("SELECT SUM({quantity}) FROM {SalesHistory} WHERE {customerNo} IN (?customerNos) AND {customerCategory}=?category AND {brand}=?brand AND").append(SclDateUtility.getDateClauseQueryByMonthYear("invoiceDate",monthValue,year, params));
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
	public Integer getPendingApprovalVisitsCountForTsmorRh(SclUserModel currentUser) {
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
	public SearchPageData<VisitMasterModel> getReviewLogsForTSM(SearchPageData searchPageData, SclUserModel user, Date startDate, Date endDate, String searchKey) {
		return (SearchPageData<VisitMasterModel>) sessionService.executeInLocalView(new SessionExecutionBody() {
			@Override
			public SearchPageData<VisitMasterModel> execute() {
				try {
					searchRestrictionService.disableSearchRestrictions();
					final Map<String, Object> params = new HashMap<String, Object>();
					final StringBuilder builder = new StringBuilder("SELECT {vm.pk} FROM {VisitMaster AS vm JOIN SclUser as u on {vm:user}={u:pk}} WHERE {vm.user}!=?currentUser AND {u:userType} = ?userType AND {vm.status}=?status AND {vm.endVisitTime}>= ?startDate AND {vm.endVisitTime}< ?endDate AND {vm:districtMaster} in (?districtMastersList) ");

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
					params.put("userType",SclUserType.SO);


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
	public SearchPageData<VisitMasterModel> getReviewLogsForRH(SearchPageData searchPageData, SclUserModel user, Date startDate, Date endDate, String searchKey) {
		return (SearchPageData<VisitMasterModel>) sessionService.executeInLocalView(new SessionExecutionBody() {
			@Override
			public SearchPageData<VisitMasterModel> execute() {
				try {
					searchRestrictionService.disableSearchRestrictions();
					final Map<String, Object> params = new HashMap<String, Object>();
					final StringBuilder builder = new StringBuilder("SELECT {vm.pk} FROM {VisitMaster AS vm JOIN SclUser as u on {vm:user}={u:pk}} WHERE {vm.user}!=?currentUser AND {u:userType} = ?userType AND {vm.status}=?status AND {vm.endVisitTime}>= ?startDate AND {vm.endVisitTime}< ?endDate AND {vm:regionMaster} in (?regionMastersList) ");

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
					params.put("userType",SclUserType.SO);


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
				+ "{v:endVisitTime} is not null and " + SclDateUtility.getMtdClauseQuery("v:visitPlannedDate", params));
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

	/**
	 * @param sclCustomerModel
	 * @param soTerritory
	 * @return
	 */
	@Override
	public Integer getRetailerCountByTerritory(SclCustomerModel sclCustomerModel, Collection<TerritoryMasterModel> soTerritory) {

		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT count({dr.pk}) FROM {DealerRetailerMapping AS dr join SclCustomer as sc on {sc.pk}={dr.dealer} } WHERE {dr.retailer}=?retailer AND {sc.territoryCode} IN (?soTerritory)");
         List<TerritoryMasterModel> soTerritoryList=soTerritory.stream().collect(Collectors.toList());
		params.put("retailer", sclCustomerModel);
		params.put("soTerritory", soTerritoryList);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Collections.singletonList(Integer.class));
       LOG.info(String.format("getRetailer count by territory::%s",query));
		final SearchResult<Integer> searchResult = flexibleSearchService.search(query);
		return CollectionUtils.isNotEmpty(searchResult.getResult())? searchResult.getResult().get(0):0;
	}
	public Integer getRetailerCountByExceptCurTerritory(SclCustomerModel sclCustomerModel, Collection<TerritoryMasterModel> soTerritory) {

		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT count({dr.pk}) FROM {DealerRetailerMapping AS dr join SclCustomer as sc on {sc.pk}={dr.dealer} } WHERE {dr.retailer}=?retailer AND {sc.territoryCode} NOT IN (?soTerritory)");
		List<TerritoryMasterModel> soTerritoryList=soTerritory.stream().collect(Collectors.toList());
		params.put("retailer", sclCustomerModel);
		params.put("soTerritory", soTerritoryList);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Collections.singletonList(Integer.class));
		LOG.info(String.format("getRetailer count by territory::%s",query));
		final SearchResult<Integer> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult().get(0);
	}

	/**
	 * Get Latest Countervisit by Retailer/Dealer uid
	 * @param uid
	 * @param sclUser
	 * @return
	 */
	@Override
	public CounterVisitMasterModel getLatestCounterVisit(String uid,SclUserModel sclUser) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {cvm:pk} from {SclCustomer as cust join CounterVisitMaster as cvm on {cvm:sclCustomer}={cust:pk} join VisitMaster as vs on {cvm.visit}={vs.pk}} where {cust:uid}=?uid and {cvm:endVisitTime} is not null and {vs.user}=?sclUser order by {cvm:endVisitTime} desc");
		params.put("uid", uid);
		params.put("sclUser", sclUser);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(CounterVisitMasterModel.class));
		query.addQueryParameters(params);
		LOG.info(String.format("Get Latest Counter Visit Query :%s with uid :%s",query,uid));
		final SearchResult<CounterVisitMasterModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	/**
	 * Get Market Mapping Details by Latest Counter Visit
	 * @param latestCounterVisit
	 * @return
	 */
	@Override
	public List<MarketMappingDetailsModel> getMarketMappingDetailsByVisitId(CounterVisitMasterModel latestCounterVisit){
		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put("latestCounterVisit",latestCounterVisit);
		final StringBuilder sql = new StringBuilder();
		sql.append("select distinct {mmd:pk} from {CounterVisitMaster as cvm join MarketMappingDetails as mmd on {mmd.counterVisit}={cvm.pk} join CompetitorProduct as cp on {mmd:product}={cp.pk}} where {mmd:counterVisit}=?latestCounterVisit");
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Arrays.asList(MarketMappingDetailsModel.class));
		query.getQueryParameters().putAll(attr);
		LOG.info(String.format("Get Market Mapping Details Query ::%s",query));
		final SearchResult<MarketMappingDetailsModel> result = flexibleSearchService.search(query);
		return result.getResult() != null && !result.getResult().isEmpty() ? result.getResult() : Collections.emptyList();

	}

	/**
	 * @param sclUser
	 * @param sclCustomer
	 * @return
	 */
	@Override
	public Date getEndVisitTime(SclUserModel sclUser, SclCustomerModel sclCustomer) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("Select MAX({cv.endVisitTime}) from {CounterVisitMaster as cv join VisitMaster as v on {v.pk}={cv.visit} } where {cv.sclCustomer}=?sclCustomer and {v.user}=?sclUser and  {cv.endVisitTime} IS NOT NULL");
		params.put("sclCustomer", sclCustomer);
		params.put("sclUser", sclUser);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Date.class));
		query.addQueryParameters(params);
		LOG.info(String.format("getEndVisitTime Query :%s ",query));
		final SearchResult<Date> searchResult = flexibleSearchService.search(query);
		if (CollectionUtils.isNotEmpty(searchResult.getResult()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	public PaginatedFlexibleSearchService getPaginatedFlexibleSearchService() {
		return paginatedFlexibleSearchService;
	}

	public void setPaginatedFlexibleSearchService(PaginatedFlexibleSearchService paginatedFlexibleSearchService) {
		this.paginatedFlexibleSearchService = paginatedFlexibleSearchService;
	}

	public TerritoryManagementDao getTerritoryManagementDao() {
		return territoryManagementDao;
	}

	public void setTerritoryManagementDao(TerritoryManagementDao territoryManagementDao) {
		this.territoryManagementDao = territoryManagementDao;
	}

	public DistrictMasterDao getDistrictMasterDao() {
		return districtMasterDao;
	}

	public void setDistrictMasterDao(DistrictMasterDao districtMasterDao) {
		this.districtMasterDao = districtMasterDao;
	}
}
