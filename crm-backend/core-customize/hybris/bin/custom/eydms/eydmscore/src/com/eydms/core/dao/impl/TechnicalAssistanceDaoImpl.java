package com.eydms.core.dao.impl;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.dao.SlctCrmIntegrationDao;
import com.eydms.core.enums.*;
import com.eydms.core.model.*;
import com.eydms.core.utility.EyDmsDateUtility;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.dao.TechnicalAssistanceDao;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;

public class TechnicalAssistanceDaoImpl implements TechnicalAssistanceDao{

	@Resource
	FlexibleSearchService flexibleSearchService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	PaginatedFlexibleSearchService paginatedFlexibleSearchService;

	@Autowired
	SlctCrmIntegrationDao slctCrmIntegrationDao;


	public Map<String, String> getEndCustomerSortCodeToQueryAlias() {
		return endCustomerSortCodeToQueryAlias;
	}

	public void setEndCustomerSortCodeToQueryAlias(Map<String, String> endCustomerSortCodeToQueryAlias) {
		this.endCustomerSortCodeToQueryAlias = endCustomerSortCodeToQueryAlias;
	}

	private Map<String, String> endCustomerSortCodeToQueryAlias;

	private Map<String, String> technicalAssistanceSortCodeToQueryAlias;

	public Map<String, String> getTechnicalAssistanceSortCodeToQueryAlias() {
		return technicalAssistanceSortCodeToQueryAlias;
	}

	public void setTechnicalAssistanceSortCodeToQueryAlias(Map<String, String> technicalAssistanceSortCodeToQueryAlias) {
		this.technicalAssistanceSortCodeToQueryAlias = technicalAssistanceSortCodeToQueryAlias;
	}

	@Override
	public List<TAExpertise> getExpertiseListForCurrentConstructionStage(TACurrentConstructionStage constructionStage) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {tAExpertise} FROM {TACurrentConstructionStage2Expertise} WHERE {tACurrentConstructionStage}=?constructionStage ");
		params.put("constructionStage", constructionStage);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(TAExpertise.class));
		query.addQueryParameters(params);
		final SearchResult<TAExpertise> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
	}

  	@Override
	public SearchPageData<NetworkAssistanceModel> getNetworkAssitances(SearchPageData searchPageData, String startDate, String endDate, String partnerCode
			, String requestNo, String filter,List<String> status) {
		final StringBuilder sql = new StringBuilder();
		sql.append("select {n.pk} from {NetworkAssistance as n ");
		if(filter!=null){
			sql.append(" join EyDmsCustomer as c on {c.pk}={n.raisedBy} ");
		}
		sql.append(" } where  {n.requestDate} is not null ");
		final Map<String, Object> params = new HashMap<String, Object>();
		if(startDate!=null && endDate!=null) {
			params.put("startDate", startDate);
			params.put("endDate", endDate);
			sql.append(" and {n.requestDate}>=?startDate and {n.requestDate} <=?endDate ");
		}
		if(filter!=null){
			String filterKey= "%".concat(filter.toUpperCase()).concat("%");
			sql.append(" AND ( UPPER({n:title}) like ?filter OR UPPER({c:name}) like ?filter OR UPPER({c:uid}) like ?filter OR UPPER({c:customerNo}) like ?filter OR " +
					"UPPER({n:requestNo}) like ?filter ) ");
			params.put("filter", filterKey);
		}
		if(requestNo!=null) {
			params.put("requestNo", requestNo);
			sql.append(" and {n.requestNo}=?requestNo ");
		}
		if(partnerCode!=null) {
			params.put(NetworkAssistanceModel.RAISEDBY, (B2BCustomerModel)userService.getUserForUID(partnerCode));			
			sql.append(" and {n.raisedBy}=?raisedBy ");
		}

		if (status != null && !status.isEmpty()) {
			List<AssitanceStatus> requestStatusList = new ArrayList<>();

			for (String statuses : status) {
				requestStatusList.add(AssitanceStatus.valueOf(statuses));
			}

			/*if (!params.isEmpty()) {
				sql.append("and ");
			}
*/
			sql.append("and {n.status} in (?requestStatusList) ");
			params.put("requestStatusList", requestStatusList);
		}
		
		sql.append(" order by {n.modifiedTime} desc ");
		final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
		parameter.setSearchPageData(searchPageData);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Collections.singletonList(NetworkAssistanceModel.class));
		query.getQueryParameters().putAll(params);
		parameter.setFlexibleSearchQuery(query);
		return paginatedFlexibleSearchService.search(parameter);
	}

	@Override
	public SearchPageData<EndCustomerComplaintModel> getEndCustomerComplaints(SearchPageData searchPageData, String startDate, String endDate, String partnerCode
			, String requestId, String searchKey,List<String> requestStatuses,Boolean isSiteVisitRequired,Boolean plannedVisitForToday, String subAreaMasterPk) {
		final StringBuilder sql = new StringBuilder();
		final Map<String, Object> params = new HashMap<String, Object>();
		sql.append("select {n.pk} from {EndCustomerComplaint as n ");
		if (searchKey != null) {
			sql.append(" join EyDmsUser as u on {u.pk}={n.raisedBy} ");
		}
		sql.append(" } where {n:requestId}!='0' ");


		B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
		if (currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.TSO_GROUP_ID))) {
			sql.append(" and {n:tsoAssigned}=?currentUser ");
			params.put("currentUser", currentUser);
			if (requestStatuses != null && !requestStatuses.isEmpty()) {
				List<CustomerComplaintTSOStatus> tsoRequestStatuses = requestStatuses.stream().map(each->CustomerComplaintTSOStatus.valueOf(each)).collect(Collectors.toList());
				sql.append(" AND {n:tsoStatus} in (?tsoStatus) ");
				params.put("tsoStatus", tsoRequestStatuses);
			}
		}
		else {
			List<TAServiceRequestStatus> taServiceRequestStatuses = new ArrayList<>();
			if (requestStatuses != null && !requestStatuses.isEmpty()) {
				TAServiceRequestStatus requestStatus = null;
				for (String site : requestStatuses) {
					if (site.compareTo(TAServiceRequestStatus.REQUEST_RAISED.getCode()) == 0)
						requestStatus = TAServiceRequestStatus.REQUEST_RAISED;
					else if (site.compareTo(TAServiceRequestStatus.SERVICE_ONGOING.getCode()) == 0)
						requestStatus = TAServiceRequestStatus.SERVICE_ONGOING;
					else if (site.compareTo(TAServiceRequestStatus.SERVICE_COMPLETED.getCode()) == 0)
						requestStatus = TAServiceRequestStatus.SERVICE_COMPLETED;
					else if (site.compareTo(TAServiceRequestStatus.TSO_ASSIGNED.getCode()) == 0)
						requestStatus = TAServiceRequestStatus.TSO_ASSIGNED;
					taServiceRequestStatuses.add(requestStatus);
				}
				sql.append(" AND {n:status} in (?taServiceRequestStatuses) ");
				params.put("taServiceRequestStatuses", taServiceRequestStatuses);
			}

				sql.append(" and {n:raisedBy}=?currentUser ");
				params.put("currentUser", currentUser);



		}

		if (startDate != null && endDate != null) {
			sql.append(" AND ");
			sql.append(EyDmsDateUtility.getDateRangeClauseQuery("n.requestRaisedDate", startDate, endDate, params));
		}
		if (searchKey != null) {
			String filterKey = "%".concat(searchKey.toUpperCase()).concat("%");
			sql.append(" AND ( UPPER({n:requestId}) like ?filter OR UPPER({u:name}) like ?filter OR UPPER({u:uid}) like ?filter OR " +
					"UPPER({n:customerName}) like ?filter OR UPPER({n:phoneNumber}) like ?filter ) ");
			params.put("filter", filterKey);
		}
		if (requestId != null) {
			params.put("requestId", requestId);
			sql.append(" and {n.requestId}=?requestId ");
		}
		if (partnerCode != null) {
			params.put(EndCustomerComplaintModel.RAISEDBY, (B2BCustomerModel) userService.getUserForUID(partnerCode));
			sql.append(" and {n.raisedBy}=?raisedBy ");
		}


		if(Objects.nonNull(isSiteVisitRequired)){
			if (isSiteVisitRequired) {
				sql.append(" and {n:isSiteVisitRequired} =?isSiteVisitRequired ");
				params.put("isSiteVisitRequired", isSiteVisitRequired);
			}
		}

		if(Objects.nonNull(plannedVisitForToday)){
			if (plannedVisitForToday) {
				String today= LocalDate.now().toString();
				String nextDay= LocalDate.now().plusDays(1).toString();
				sql.append(" and {n:nextVisitDate} >=?today and {n:nextVisitDate} <?nextDay ");
				params.put("today", today);
				params.put("nextDay", nextDay);

			}
		}
		if(StringUtils.isNotBlank(subAreaMasterPk)) {
			sql.append(" and {n:subArea} =?subArea ");
			params.put("subArea", subAreaMasterPk);
		}
		
		final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
		parameter.setSearchPageData(searchPageData);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
	/*	query.setResultClassList(Collections.singletonList(EndCustomerComplaintModel.class));
		query.getQueryParameters().putAll(params);*/
		query.addQueryParameters(params);
		parameter.setFlexibleSearchQuery(query);
		parameter.setSortCodeToQueryAlias(getEndCustomerSortCodeToQueryAlias());
		return paginatedFlexibleSearchService.search(parameter);


	}

	@Override
	public SearchPageData<TechnicalAssistanceModel> getTechnicalAssistances(SearchPageData searchPageData, String startDate, String endDate, String name
			, String requestNo, String filter,List<String> status, List<String> constructionAdvisory) {

		final StringBuilder sql = new StringBuilder();
		sql.append("select {t.pk} from {TechnicalAssistance as t } where ");
		final Map<String, Object> params = new HashMap<String, Object>(20);
		
		if (startDate != null && endDate != null) {
			params.put("startDate", startDate);
			params.put("endDate", endDate);
			
			sql.append(" {t.requestDate} >= ?startDate and {t.requestDate} <= ?endDate ");
		}
		else {
			sql.append(EyDmsDateUtility.getMtdClauseQuery("t.requestDate", params));
		}
		
		B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
		if (currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.TSO_GROUP_ID))) {
			if (!params.isEmpty()) {
				sql.append(" and ");
			}
			sql.append(" {t:tsoAssigned}=?currentUser ");
			params.put("currentUser", currentUser);
			if (status != null && !status.isEmpty()) {
				List<TATSOStatus> tsoRequestStatuses = status.stream().map(each->TATSOStatus.valueOf(each)).collect(Collectors.toList());
				sql.append(" AND {t:tsoStatus} in (?tsoStatus) ");
				params.put("tsoStatus", tsoRequestStatuses);
			}
		}
			else  {
		if (status != null && !status.isEmpty()) {
			List<TAServiceRequestStatus> requestStatusList = new ArrayList<>();

			for (String statuses : status) {
				requestStatusList.add(TAServiceRequestStatus.valueOf(statuses));
			}

			if (!params.isEmpty()) {
				sql.append(" and ");
			}

			sql.append(" {t.requestStatus} in (?requestStatusList) ");
			params.put("requestStatusList", requestStatusList);
		}
				if (!params.isEmpty()) {
				sql.append(" and ");
			}
			sql.append("  {t:raisedBy}=?currentUser ");
			params.put("currentUser", currentUser);
	}
		
		if (constructionAdvisory != null && !constructionAdvisory.isEmpty()) {
			//ConstructionAdvisory advisory = ConstructionAdvisory.valueOf(constructionAdvisory.get(0));
			String advisory = constructionAdvisory.get(0);

			if (!params.isEmpty()) {
				sql.append("and ");
			}

			sql.append(" {t.constructionAdvisorys} = ?constructionAdvisory ");
			params.put("constructionAdvisory", advisory);
		}
		
		if(StringUtils.isNotBlank(filter)){

			if(!params.isEmpty()){
				sql.append("and ");
			}
			String filterKey= "%".concat(filter.toUpperCase()).concat("%");
			sql.append("  (UPPER({t:name}) like ?filter OR " +
					"UPPER({t:requestNo}) like ?filter OR UPPER({t:cellPhone}) like ?filter ) ");
			params.put("filter", filterKey);
		}

		if(requestNo!=null) {

			if (!params.isEmpty()) {
				sql.append("and ");
			}
			params.put("requestNo", requestNo);
			sql.append("  {t.requestNo}=?requestNo ");
		}


		//sql.append(" order by {t.requestNo} ");
		final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
		parameter.setSearchPageData(searchPageData);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Collections.singletonList(TechnicalAssistanceModel.class));
		query.getQueryParameters().putAll(params);
		parameter.setFlexibleSearchQuery(query);
		parameter.setSortCodeToQueryAlias(getTechnicalAssistanceSortCodeToQueryAlias());
		return paginatedFlexibleSearchService.search(parameter);
	}

	@Override
	public TechnicalAssistanceModel getTechnicalAssistanceForRequestNo(String requestNo) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {TechnicalAssistance} WHERE {requestNo}=?requestNo");
		params.put("requestNo", requestNo);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(TechnicalAssistanceModel.class));
		query.addQueryParameters(params);
		final SearchResult<TechnicalAssistanceModel> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : null;	
		else
			return null;
	}

	@Override
	public EndCustomerComplaintModel getEndCustomerComplaintForRequestNo(String requestId) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {EndCustomerComplaint} WHERE {requestId}=?requestId");
		params.put("requestId", requestId);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(EndCustomerComplaintModel.class));
		query.addQueryParameters(params);
		final SearchResult<EndCustomerComplaintModel> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	//Not to be used
	@Override
	public List<EyDmsCustomerModel> getAllCustomerForDistrictSubArea(List<String> districtSubAreaList, BaseSiteModel site) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {eydmsCustomer} FROM {CustomerSubAreaMapping} WHERE {brand} = ?brand AND {isActive} = ?active AND CONCAT({district},'_',{subArea}) IN (?districtSubAreaList)");

		boolean active = Boolean.TRUE;
		params.put("active", active);
		params.put("brand", site);
		params.put("districtSubAreaList",districtSubAreaList);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
		query.addQueryParameters(params);
		final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
		List<EyDmsCustomerModel> result = searchResult.getResult();

		return result!=null && !result.isEmpty() ? result : Collections.emptyList();
	}

	@Override
	public InvoiceMasterModel getInvoiceMasterByInvoiceNo(String invoiceNo) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {PK} FROM {InvoiceMaster} WHERE {invoiceNo} = ?invoiceNo ");

		boolean active = Boolean.TRUE;
		params.put("invoiceNo",invoiceNo);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(InvoiceMasterModel.class));
		query.addQueryParameters(params);
		final SearchResult<InvoiceMasterModel> searchResult = flexibleSearchService.search(query);
		List<InvoiceMasterModel> result = searchResult.getResult();

		return result!=null && !result.isEmpty() ? result.get(0) : null;
	}

	@Override
	public CustomerComplaintModel getCustomerAssistanceForRequestNo(String requestNo) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {CustomerComplaint} WHERE {requestNo}=?requestNo");
		params.put("requestNo", requestNo);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(CustomerComplaintModel.class));
		query.addQueryParameters(params);
		final SearchResult<CustomerComplaintModel> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public List<CustomerComplaintModel> getCustomerAssistanceRequestList(EyDmsUserModel user, Date startDate, Date endDate, TAServiceRequestStatus status, String key) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {CustomerComplaint} WHERE {raisedBy}=?user");
		params.put("user", user);

		if(startDate!=null && endDate!=null)
		{
			builder.append(" AND {requestDate} BETWEEN ?startDate AND ?endDate");
			params.put("user", user);
		}

		if(status!=null)
		{
			builder.append(" AND {requestStatus}=?status");
			params.put("status", status);
		}

		if(key!=null)
		{
			String searchKey = "%".concat(key).concat("%");
			builder.append(" AND ( {name} LIKE ?searchKey OR {requestNo} LIKE ?searchKey )");
			params.put("searchKey", searchKey);
		}

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(CustomerComplaintModel.class));
		query.addQueryParameters(params);
		final SearchResult<CustomerComplaintModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
	}

  @Override
	public NetworkAssistanceModel getNetworkAssistanceForRequestNo(String requestNo)
	{
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {NetworkAssistance} WHERE {requestNo}=?requestNo");
		params.put("requestNo", requestNo);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(NetworkAssistanceModel.class));
		query.addQueryParameters(params);
		final SearchResult<NetworkAssistanceModel> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : null;	
		else
			return null;
	}

	@Override
	public Integer countOfAssignedTicketNumbers() {
		final Map<String, Object> params = new HashMap<String, Object>();
		B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
		final StringBuilder builder = new StringBuilder("select count({e.pk}) from {EndCustomerComplaint as e} where {e.tsoAssigned}=?currentUser  and {status} != ?status");
		params.put("currentUser", currentUser);
		params.put("status",TAServiceRequestStatus.CLOSED);//have to correct
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Integer.class));
        query.addQueryParameters(params);
        final SearchResult<Integer> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0;
        else
            return 0;
	}



    @Override
    public DetailsFromSiteModel getDetailesFromSiteById(String siteId) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {DetailsFromSite} WHERE {id}=?siteId");
		params.put("siteId", siteId);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(DetailsFromSiteModel.class));
		query.addQueryParameters(params);
		final SearchResult<DetailsFromSiteModel> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : null;
		else
			return null;
    }
	@Override
	public List<EndCustomerComplaintModel> getEndCustomerComplaintsForSLCT(String startDate, String endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {EndCustomerComplaint} WHERE {syncFlag}=?flag and {tsoStatus}!=?tsoStatus and {isSiteVisitRequired} IS NOT NULL ");
		if(startDate!=null && endDate!=null){
			builder.append(" and {requestRaisedDate} >= ?startDate and {requestRaisedDate} < ?endDate ");
			params.put("startDate",startDate);
			params.put("endDate",endDate);
		}
		params.put("flag", Boolean.FALSE);
		params.put("tsoStatus",CustomerComplaintTSOStatus.CLOSED);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(EndCustomerComplaintModel.class));
		query.addQueryParameters(params);
		final SearchResult<EndCustomerComplaintModel> searchResult = flexibleSearchService.search(query);
		List<EndCustomerComplaintModel> result = searchResult.getResult();
		return result!=null && !result.isEmpty() ? result : Collections.emptyList();
	}

	@Override
	public List<EndCustomerComplaintModel> getEndCustomerComplaintClosureRequest() {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {e.pk} from {EndCustomerComplaint as e}  where  {e.tsoStatus}=?tsoStatus ").append(slctCrmIntegrationDao.appendIntegrationSetting("TSO_TICKET_CLOSURE",params));
		params.put("tsoStatus",CustomerComplaintTSOStatus.CLOSURE_REQUEST_SENT);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(EndCustomerComplaintModel.class));
		query.addQueryParameters(params);
		final SearchResult<EndCustomerComplaintModel> searchResult = flexibleSearchService.search(query);
		List<EndCustomerComplaintModel> result = searchResult.getResult();
		return result!=null && !result.isEmpty() ? result : Collections.emptyList();
	}
}
