package com.scl.core.dao.impl;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.SlctCrmIntegrationDao;
import com.scl.core.enums.*;
import com.scl.core.model.*;
import com.scl.core.services.TerritoryManagementService;
import com.scl.core.services.TerritoryMasterService;
import com.scl.core.utility.SclDateUtility;
import com.scl.facades.data.FilterTalukaData;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;

import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.dao.TechnicalAssistanceDao;

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
	TerritoryMasterService territoryMasterService;
	
	@Autowired
	UserService userService;
	@Autowired
	TerritoryManagementService territoryManagementService;

	@Autowired
	PaginatedFlexibleSearchService paginatedFlexibleSearchService;
	@Autowired
	SlctCrmIntegrationDao slctCrmIntegrationDao;

	@Autowired
	BaseSiteService baseSiteService;
	private static final Logger LOG = Logger.getLogger(TechnicalAssistanceDaoImpl.class);
	private static final String TECHNICALASSISTANCE = "TechnicalAssistance";

	private static final String ENDCUSTOMERCOMPLAINT = "EndCustomerComplaint";
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
			sql.append(" join SclCustomer as c on {c.pk}={n.raisedBy} ");
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
			, String requestId, String searchKey,List<String> requestStatuses,Boolean isSiteVisitRequired,Boolean plannedVisitForToday, String subAreaMasterPk,List<String> taluka) {
		final StringBuilder sql = new StringBuilder();
		final Map<String, Object> params = new HashMap<String, Object>();
		sql.append("select {n.pk} from {EndCustomerComplaint as n ");
		if (searchKey != null) {
			sql.append(" join SclUser as u on {u.pk}={n.raisedBy} ");
		}
		sql.append(" } where {n:requestId}!='0' ");


		B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
		if (currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.TSO_GROUP_ID))) {
			if(Objects.nonNull(isSiteVisitRequired) && isSiteVisitRequired) {
				sql.append(" and {n:tsoAssigned}=?currentUser and {n:isSiteVisitRequired}=?isSiteVisitRequired ");
				params.put("isSiteVisitRequired",isSiteVisitRequired);
				params.put("currentUser", currentUser);
			}else{
				FilterTalukaData filterTalukaData=new FilterTalukaData();
				sql.append(" and {n:tsoAssigned}=?currentUser ");
				//params.put("subArea", territoryManagementService.getTaulkaForUser(filterTalukaData));
				params.put("currentUser", currentUser);
			}
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
			/*FilterTalukaData filterTalukaData=new FilterTalukaData();
			sql.append(" and {n:subArea} in (?subArea) ");
			params.put("subArea", territoryManagementService.getTaulkaForUser(filterTalukaData));
			*/
		/*	List<TerritoryMasterModel> territoriesForSO = territoryMasterService.getTerritoriesForSO();
			if(CollectionUtils.isNotEmpty(territoriesForSO)) {
				sql.append(" and {n:territoryMaster} in (?territoriesForSO) ");
				params.put("territoriesForSO", territoriesForSO);

			}*/

		}

		if (startDate != null && endDate != null) {
			sql.append(" AND ");
			sql.append(SclDateUtility.getDateRangeClauseQuery("n.requestRaisedDate", startDate, endDate, params));
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


		if(Objects.nonNull(isSiteVisitRequired) && isSiteVisitRequired){
				sql.append(" and {n:isSiteVisitRequired} =?isSiteVisitRequired ");
				params.put("isSiteVisitRequired", isSiteVisitRequired);
		}
		if(taluka!=null && CollectionUtils.isNotEmpty(taluka)){
			if (!params.isEmpty()) {
				sql.append("and ");
			}
			sql.append("  {n:taluka} in (?taluka) ");
			params.put("taluka", taluka);
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
		query.setResultClassList(Collections.singletonList(EndCustomerComplaintModel.class));
		query.getQueryParameters().putAll(params);
		parameter.setFlexibleSearchQuery(query);
		LOG.info(String.format("Query for EndCustomer Complaint %s",query));
		parameter.setSortCodeToQueryAlias(getEndCustomerSortCodeToQueryAlias());
		return paginatedFlexibleSearchService.search(parameter);

	}

	@Override
	public SearchPageData<TechnicalAssistanceModel> getTechnicalAssistances(SearchPageData searchPageData, String startDate, String endDate, String name
			, String requestNo, String filter,List<String> status, List<String> constructionAdvisory,List<String> taluka) {

		final StringBuilder sql = new StringBuilder();
		sql.append("select {t.pk} from {TechnicalAssistance as t } where ");
		final Map<String, Object> params = new HashMap<String, Object>(20);

		/*else {
			sql.append(SclDateUtility.getMtdClauseQuery("t.requestDate", params));
		}*/

		B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
		if (currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.TSO_GROUP_ID))) {
			sql.append(" {t:tsoAssigned}=?currentUser");
			/*FilterTalukaData filterTalukaData=new FilterTalukaData();
			params.put("subAreaList",territoryManagementService.getTaulkaForUser(filterTalukaData));*/

			params.put("currentUser", currentUser);
			if (status != null && !status.isEmpty()) {
				List<TATSOStatus> tsoRequestStatuses = status.stream().map(each->TATSOStatus.valueOf(each)).collect(Collectors.toList());
				sql.append(" AND {t:tsoStatus} in (?tsoStatus) ");
				params.put("tsoStatus", tsoRequestStatuses);
			}
		}
		else  {
			/*//Added Territory code
			List<TerritoryMasterModel> territoriesForSO = territoryMasterService.getTerritoriesForSO();
			if(CollectionUtils.isNotEmpty(territoriesForSO)) {
				sql.append(" {t.territoryMaster} in (?territoryMasterList) ");
				params.put("territoryMasterList",territoriesForSO);
			}*/
			if (!params.isEmpty()) {
				sql.append(" and ");
			}

			sql.append(" {t:subArea} in (?subAreaList) ");
			FilterTalukaData filterTalukaData=new FilterTalukaData();
			params.put("subAreaList",territoryManagementService.getTaulkaForUser(filterTalukaData));

			if (!params.isEmpty()) {
				sql.append(" and ");
			}

			sql.append("  {t:raisedBy}=?currentUser ");
			params.put("currentUser", currentUser);

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

		if(taluka!=null && CollectionUtils.isNotEmpty(taluka)){
			if (!params.isEmpty()) {
				sql.append("and ");
			}
			params.put("taluka", taluka);
			sql.append("  {t.taluka} in (?taluka) ");
		}


	      if (startDate != null && endDate != null) {
			if(!params.isEmpty()){
				sql.append("and ");
			}
			params.put("startDate", startDate);
			params.put("endDate", endDate);
			sql.append(" {t.dateOfSupervisionRequired} >= ?startDate and {t.dateOfSupervisionRequired} <= ?endDate ");
		}

		//sql.append(" order by {t.requestNo} ");
		final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
		parameter.setSearchPageData(searchPageData);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Collections.singletonList(TechnicalAssistanceModel.class));
		query.getQueryParameters().putAll(params);
		parameter.setFlexibleSearchQuery(query);
		LOG.info(String.format("Query for Technical Assistance %s",query));
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
	public List<SclCustomerModel> getAllCustomerForDistrictSubArea(List<String> districtSubAreaList, BaseSiteModel site) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {sclCustomer} FROM {CustomerSubAreaMapping} WHERE {brand} = ?brand AND {isActive} = ?active AND CONCAT({district},'_',{subArea}) IN (?districtSubAreaList)");

		boolean active = Boolean.TRUE;
		params.put("active", active);
		params.put("brand", site);
		params.put("districtSubAreaList",districtSubAreaList);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(SclCustomerModel.class));
		query.addQueryParameters(params);
		final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
		List<SclCustomerModel> result = searchResult.getResult();

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
	public List<CustomerComplaintModel> getCustomerAssistanceRequestList(SclUserModel user, Date startDate, Date endDate, TAServiceRequestStatus status, String key) {
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
	public Integer countOfAssignedTicketNumbers(SclUserModel tsoUser) {
		final Map<String, Object> params = new HashMap<String, Object>();
		//B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
		final StringBuilder builder = new StringBuilder("select count({e.pk}) from {EndCustomerComplaint as e} where {e.tsoAssigned}=?tsoUser  and {status} != ?status");
		params.put("tsoUser", (B2BCustomerModel) tsoUser);
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

	/**
	 * Get TSO by Taluka or District or State
	 * @param taluka
	 * @param district
	 * @param state
	 * @return sclUserModel
	 */
	@Override
	public List<SclUserModel> getTSO(String taluka, String district, String state){
		final Map<String, Object> params = new HashMap<String, Object>();
		boolean active = Boolean.TRUE;
		List<SclUserModel> result = new ArrayList<>();
		final StringBuilder builder = new StringBuilder();
		result = getTsoByTerritories
				(taluka, district, state, params, active, result, builder);
		return result!=null && !result.isEmpty() ? result : Collections.emptyList();
	}

	/**
	 * Fetches the list of SclUSer Models for taluka or district or state
	 * @param taluka
	 * @param district
	 * @param state
	 * @param params
	 * @param active
	 * @param result
	 * @param builder
	 * @return the list of SclUSer Models for taluka or district or state
	 */
	private List<SclUserModel> getTsoByTerritories(String taluka, String district, String state, Map<String, Object> params, boolean active, List<SclUserModel> result, StringBuilder builder) {
		if (StringUtils.isNotEmpty(taluka)) {
			builder.append("SELECT distinct {tsoUser} from {TSOTalukaMapping as ttm join SubAreaMaster as sam on {ttm:subAreaMaster}={sam:pk} join SclUser as u on {ttm:tsoUser}={u:pk}} where {sam:taluka}=?taluka and {ttm:isActive}=?active");
			params.put("taluka", taluka);
			params.put("active", active);
			result = getSclUserModels(builder, params);
		}
		// If no TSOs found for Taluka, then fetch the TSOs for the given district
		if(CollectionUtils.isEmpty(result) && StringUtils.isNotEmpty(district)){
			builder.setLength(0); //Clear the String build
			builder.append("select distinct {tsoUser} from {TSOTalukaMapping as ttm join SubAreaMaster as sam on {ttm:subAreaMaster}={sam:pk} join SclUser as u on {ttm:tsoUser}={u:pk}} where {sam:district}=?district and {ttm:isActive}=?active");
			params.clear(); //Clear the params
			params.put("district", district);
			params.put("active", active);
			result = getSclUserModels(builder, params);
		}
		// If no TSOs found for District, then fetch the TSOs for the given state
		if(CollectionUtils.isEmpty(result) && StringUtils.isNotEmpty(state)){
			builder.setLength(0);
			builder.append("select distinct {tsoUser} from {TSOTalukaMapping as ttm join SclUser as u on {ttm:tsoUser}={u:pk}} where {ttm:state}=?state and {ttm:isActive}=?active");
			params.clear();
			params.put("state", state);
			params.put("active", active);
			result = getSclUserModels(builder, params);
		}
		return result;
	}

	/**
	 * To Fetch SclUserModels
	 * @param builder
	 * @param params
	 * @return
	 */
	private List<SclUserModel> getSclUserModels(StringBuilder builder, Map<String, Object> params) {
		builder.append(" and {ttm:brand}=?brand");
		params.put("brand", baseSiteService.getCurrentBaseSite());
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(SclUserModel.class));
		query.addQueryParameters(params);
		LOG.info(String.format("Get Mapped TSOs query :%s",query));
		final SearchResult<SclUserModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	/**
	 * Get No of TA Assigned Numbers
	 * @param tsoUser
	 * @return
	 */
	@Override
	public Integer countOfTAAssignedTicketNumbers(SclUserModel tsoUser) {
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("tsoUser", (B2BCustomerModel) tsoUser);
		TATSOStatus tsoStatus = TATSOStatus.PENDING;
		params.put("tsoStatus", tsoStatus);
		final StringBuilder builder = new StringBuilder("SELECT count({ta.pk}) from {TechnicalAssistance AS ta} WHERE {ta:tsoAssigned}=?tsoUser AND {ta:tsoStatus}=?tsoStatus ");
		final FlexibleSearchQuery searchQuery = new FlexibleSearchQuery(builder.toString());
		searchQuery.setResultClassList(Arrays.asList(Integer.class));
		searchQuery.getQueryParameters().putAll(params);
		final SearchResult<Integer> searchResult = flexibleSearchService.search(searchQuery);
		LOG.info(String.format("Query for fetching Count of TA Assigned Ticket Number :%s for TSO %s",searchQuery,tsoUser.getUid()));
		return searchResult.getResult()!=null && !searchResult.getResult().isEmpty()? searchResult.getResult().get(0) : 0;
	}

	/**
	 * @param complaintId
	 * @return
	 */
	@Override
	public EndCustomerComplaintModel getEndCustomerComplaintForRequestNumber(String requestId) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {EndCustomerComplaint} WHERE {requestId}=?requestId and {requestRaisedDate} is not null and {ticketClosureRequestDate} is null");
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

	/**
	 * @param requestNo
	 * @return
	 */
	@Override
	public TechnicalAssistanceModel getTechnicalAssistanceForRequestNumber(String requestNo) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {TechnicalAssistance} WHERE {requestNo}=?requestNo and {requestDate} is not null and {closedDate} is null");
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

	/**
	 *
	 * @param cardType
	 * @param currentUser
	 * @return List of Distinct Taluka's
	 */
	@Override
	public List<String> getTalukasForTaOrCc(String cardType, UserModel currentUser){
		final Map<String, Object> params = new HashMap<String, Object>();
		StringBuilder builder = new StringBuilder();
		if(cardType.equalsIgnoreCase(TECHNICALASSISTANCE)) {
			builder = new StringBuilder("select distinct {taluka} FROM {TechnicalAssistance} WHERE");
		}
		else if(cardType.equalsIgnoreCase(ENDCUSTOMERCOMPLAINT)){
			builder = new StringBuilder("select distinct {taluka} FROM {EndCustomerComplaint} WHERE");
		}
		if(currentUser instanceof SclUserModel) {
			SclUserModel currentSclUser = (SclUserModel) currentUser;
			if(currentSclUser.getUserType()!=null) {
				if(currentSclUser.getUserType().equals(SclUserType.TSO)) {
					builder.append(" {tsoAssigned} = ?currentUser");
				}
				else if(currentSclUser.getUserType().equals(SclUserType.SO) || currentSclUser.getUserType().equals(SclUserType.TSM) || currentSclUser.getUserType().equals(SclUserType.RH)) {
					builder.append(" {raisedBy} = ?currentUser");
				}
				params.put("currentUser",currentUser);
			}
		} else if (currentUser instanceof SclCustomerModel) {
			//TODO - Handle this logic for Dealer/Retailer/Influencer
		}
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		query.addQueryParameters(params);
		LOG.info(String.format("getTalukasForTaOrCc query for user %s  :: %s ",currentUser,query));
		final SearchResult<String> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
	}
}
