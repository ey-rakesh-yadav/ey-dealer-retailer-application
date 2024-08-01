package com.scl.core.dao.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import com.scl.core.dao.DataConstraintDao;
import com.scl.core.dao.SlctCrmIntegrationDao;
import com.scl.core.enums.*;
import com.scl.core.model.*;
import com.scl.facades.data.FilterTalukaData;
import com.scl.facades.data.LeadRequestData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.UserModel;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.dao.InfluencerDao;
import com.scl.core.services.TerritoryManagementService;
import com.scl.core.utility.SclDateUtility;
import com.scl.facades.data.RequestCustomerData;

import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.CategoryService;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;

public class InfluencerDaoImpl implements InfluencerDao{

	@Autowired
	FlexibleSearchService flexibleSearchService;

	@Autowired
	BaseSiteService baseSiteService;

	@Autowired
	CatalogVersionService catalogVersionService;

	@Autowired
	CategoryService categoryService;

	@Autowired
	UserService userService;

	@Autowired
	TerritoryManagementService territoryManagementService;

	PaginatedFlexibleSearchService paginatedFlexibleSearchService;

	Map<String, String> leadMasterSortCodeToQueryAlias;

	Map<String, String> scheduleMeetingSortCodeToQueryAlias;

	Map<String, String> sclCustomerSortCodeToQueryAlias;

	@Autowired
	DataConstraintDao dataConstraintDao;

	@Autowired
	SlctCrmIntegrationDao slctCrmIntegrationDao;

	public Map<String, String> getSclCustomerSortCodeToQueryAlias() {
		return sclCustomerSortCodeToQueryAlias;
	}

	public void setSclCustomerSortCodeToQueryAlias(Map<String, String> sclCustomerSortCodeToQueryAlias) {
		this.sclCustomerSortCodeToQueryAlias = sclCustomerSortCodeToQueryAlias;
	}

	@Override
	public SclUserModel searchSalesOfficer(List<String> keys) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {SclUser} WHERE {email} IN (?keys) OR {name} IN (?keys) OR {mobileNumber} IN (?keys) OR {uid} IN (?keys) ");
		params.put("keys", keys);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(SclUserModel.class));
		query.addQueryParameters(params);
		final SearchResult<SclUserModel> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public SclCustomerModel searchSclCustomer(List<String> keys) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {SclCustomer} WHERE {email} IN (?keys) OR {name} IN (?keys) OR {mobileNumber} IN (?keys) or {uid} in (?keys)");
		params.put("keys", keys);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(SclCustomerModel.class));
		query.addQueryParameters(params);
		final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public List<List<Object>> getGiftSchemeStatusDetails(String influencerType, String state) {
		final Map<String, Object> params = new HashMap<String, Object>();

		CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
		CategoryModel category = categoryService.getCategoryForCode(catalogVersion, "gift");

		String currentDate = LocalDate.now().toString();

		final StringBuilder builder = new StringBuilder();
		builder.append("select {p.name},{p.code},{p.state},{p.influencerType},{pr.price}" +
				" from {Gift as p join PriceRow as pr on {p.code}={pr.productId}" +
				" join CategoryProductRelation as cp on {cp.source}=?category and {cp.target}={p.pk}}" +
				" where {p.onlineDate} <= ?currentTime and {p.offlineDate} >= ?currentTime and {p.giftState} like ?state " +
				" and {p.influencerType} like ?influencerType" +
				" order by {p.code},{p.name},{p.state} desc,{p.influencerType} desc");

		params.put("category", category);
		params.put("currentTime", currentDate);
		params.put("state", "%"+state+"%");
		params.put("influencerType", "%"+influencerType+"%");

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Arrays.asList(String.class, String.class, String.class, String.class, Double.class));
		final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public MeetingScheduleModel getMeetingScheduleByCode(String meetingCode) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {MeetingSchedule} WHERE {meetingScheduleId} = ?meetingCode");
		params.put("meetingCode", meetingCode);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(MeetingScheduleModel.class));
		query.addQueryParameters(params);
		final SearchResult<MeetingScheduleModel> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public List<List<Object>> getInfluencerTypeList(String influencerType,List<SubAreaMasterModel> subAreaMasterList) {
		final Map<String, Object> params = new HashMap<String, Object>();

		StringBuilder builder = new StringBuilder("SELECT {e.code},count({c.pk}) FROM {").append(TerritoryManagementDaoImpl.CUSTOMER_FOR_USER_QUERY)
				.append(" join EnumerationValue as e on {c.influencerType}={e.pk}}")
				.append(" WHERE {c.influencerType} is not null and {m.subAreaMaster} in (?subAreaList) and {c.counterType}=?counterType and ")
				.append(SclDateUtility.getLastXDayQuery("c.dateOfJoining", params, 30));

		params.put("subAreaList", subAreaMasterList);
		params.put("counterType", CounterType.INFLUENCER);

		if(StringUtils.isNotBlank(influencerType)) {
			builder.append(" and {c.influencerType} =?influencerType");
			params.put("influencerType", InfluencerType.valueOf(influencerType));
		}

		builder.append(" group by {e.code} ");

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Arrays.asList(String.class, Integer.class));
		final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<List<Object>> getInfluencerNetworkTypeList(String influencerType,List<SubAreaMasterModel> subAreaMasterList) {
		final Map<String, Object> params = new HashMap<String, Object>();

		StringBuilder builder = new StringBuilder("SELECT {c.networkType},count({c.pk}) FROM {").append(TerritoryManagementDaoImpl.CUSTOMER_FOR_USER_QUERY)
				.append(" } WHERE {c.networkType} is not null and {m.subAreaMaster} in (?subAreaList) and {c.counterType}=?counterType ");
		params.put("subAreaList", subAreaMasterList);
		params.put("counterType", CounterType.INFLUENCER);

		if(StringUtils.isNotBlank(influencerType)) {
			builder.append(" and {c.influencerType} =?influencerType");
			params.put("influencerType", InfluencerType.valueOf(influencerType));
		}

		builder.append(" group by {c.networkType} ");

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Arrays.asList(String.class, Integer.class));
		final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<List<Object>> getInfluencerNetworkAddition(String influencerType, List<SubAreaMasterModel> subAreaMasterList) {
		final Map<String, Object> params = new HashMap<String, Object>();

		StringBuilder builder = new StringBuilder("SELECT Year({c.dateOfJoining}),MONTH({c.dateOfJoining}),count({c.pk}) FROM {").append(TerritoryManagementDaoImpl.CUSTOMER_FOR_USER_QUERY)
				.append(" } WHERE {m.subAreaMaster} in (?subAreaList) and {c.counterType}=?counterType and ")
				.append(SclDateUtility.getCurrentPreviousMonthClauseQuery("c.dateOfJoining", params));

		params.put("subAreaList", subAreaMasterList);
		params.put("counterType", CounterType.INFLUENCER);

		if(StringUtils.isNotBlank(influencerType)) {
			builder.append(" and {c.influencerType} =?influencerType");
			params.put("influencerType", InfluencerType.valueOf(influencerType));
		}

		builder.append(" group by Year({c.dateOfJoining}),MONTH({c.dateOfJoining}) ");

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Arrays.asList(Integer.class, Integer.class, Integer.class));
		final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public Integer getLeadsPendingForApproval(String influencerType, List<SubAreaMasterModel> subAreaMasterList) {
		final Map<String, Object> params = new HashMap<String, Object>();

		final StringBuilder builder = new StringBuilder("SELECT count({l.pk}) FROM {LeadMaster as l} WHERE {l.subAreaMaster} in (?subAreaList) and {l:counterType}=?counterType and {l.status}=?status ");
		params.put("subAreaList", subAreaMasterList);
		params.put("status", LeadStatus.PENDING);
		params.put("counterType", CounterType.INFLUENCER);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Integer.class));
		query.addQueryParameters(params);
		final SearchResult<Integer> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : null;
		else
			return 0;
	}

	@Override
	public Integer getOnboardingsPendingForApproval(String influencerType, List<SubAreaMasterModel> subAreaMasterList) {
		final Map<String, Object> params = new HashMap<String, Object>();

		StringBuilder builder = new StringBuilder("select count({c:pk}) from {SclCustomer as c join CustomerSubAreaMapping as m on {m.sclCustomer}={c.pk} } ")
				.append(" where {c.counterType}=?counterType AND {m.subAreaMaster} in (?subAreaList) AND {c.customerOnboardingStatus} = ?customerOnboardingStatus ");
		params.put("customerOnboardingStatus", CustomerOnboardingStatus.PENDING_FOR_APPROVAL);
		params.put("subAreaList", subAreaMasterList);
		params.put("counterType", CounterType.PROSPECTIVEINFLUENCER);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Integer.class));
		query.addQueryParameters(params);
		final SearchResult<Integer> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : null;
		else
			return 0;
	}

	@Override
	public SearchPageData<SclCustomerModel> getInfluencerOnboardingList(SearchPageData searchPageData,
																		RequestCustomerData customerRequestData, List<SubAreaMasterModel> subAreaMasterList) {
		final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
		parameter.setSearchPageData(searchPageData);
		final Map<String, Object> params = new HashMap<String, Object>();
		StringBuilder builder = new StringBuilder("select {c:pk} from {SclCustomer as c join CustomerSubAreaMapping as m on {m.sclCustomer}={c.pk} } ")
				.append(" where {c.counterType}=?counterType ");



		if(StringUtils.isNotBlank(customerRequestData.getOnboardingStatus())){
			CustomerOnboardingStatus customerOnboardingStatus = CustomerOnboardingStatus.valueOf(customerRequestData.getOnboardingStatus());
			builder.append(" AND {c.customerOnboardingStatus} = ?customerOnboardingStatus ");
			params.put("customerOnboardingStatus", customerOnboardingStatus);

			if(CustomerOnboardingStatus.APPROVED.equals(customerOnboardingStatus)) {
				builder.append(" AND {c.onboardingApprovedBy} = ?onboardingApprovedBy ");
				params.put("onboardingApprovedBy", userService.getCurrentUser());
				params.put("counterType", CounterType.INFLUENCER);
			}
			else if(CustomerOnboardingStatus.REJECTED.equals(customerOnboardingStatus)) {
				builder.append(" AND {c.onboardingRejectedBy} = ?onboardingRejectedBy ");
				params.put("onboardingRejectedBy", userService.getCurrentUser());
				params.put("counterType", CounterType.PROSPECTIVEINFLUENCER);
			}
			else {
				builder.append(" AND {m.subAreaMaster} in (?subAreaList) ");
				params.put("subAreaList", subAreaMasterList);
				params.put("counterType", CounterType.PROSPECTIVEINFLUENCER);
			}

		}

		builder.append(" and ");
		if(customerRequestData.getStartDate()!=null && customerRequestData.getEndDate()!=null)
		{
			builder.append(SclDateUtility.getDateRangeClauseQuery("c.applicationDate", customerRequestData.getStartDate(), customerRequestData.getEndDate(), params));
		}
		else {
			builder.append(SclDateUtility.getMtdClauseQuery("c.applicationDate", params));
		}
		if (StringUtils.isNotBlank(customerRequestData.getInfluencerType())) {
			builder.append(" and {c.influencerType}=?influencerType ");
			params.put("influencerType", InfluencerType.valueOf(customerRequestData.getInfluencerType()));
		}

		if(StringUtils.isNotBlank(customerRequestData.getSearchKey())){
			String search= "%".concat(customerRequestData.getSearchKey().toUpperCase()).concat("%");
			builder.append(" AND (UPPER({c:name}) like ?search OR UPPER({c:uid}) like ?search OR " +
					"UPPER({c:mobileNumber}) like ?search) ");
			params.put("search", search);
		}

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		parameter.setFlexibleSearchQuery(query);
		parameter.setSortCodeToQueryAlias(getSclCustomerSortCodeToQueryAlias());
		return getPaginatedFlexibleSearchService().search(parameter);
	}

	@Override
	public SearchPageData<LeadMasterModel> getPaginatedLeadList(SearchPageData searchPageData, LeadRequestData leadRequestData) {
		final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
		parameter.setSearchPageData(searchPageData);
		final Map<String, Object> params = new HashMap<String, Object>();
		StringBuilder builder = new StringBuilder("select {l:pk} from {LeadMaster as l} where {l:counterType}=?counterType and {l:onboardedDate} is null and ");
		params.put("counterType",CounterType.INFLUENCER);

		if(StringUtils.isNotBlank(leadRequestData.getStartDate()) && StringUtils.isNotBlank(leadRequestData.getEndDate())){
			builder.append(SclDateUtility.getDateRangeClauseQuery("l:creationtime",leadRequestData.getStartDate(), leadRequestData.getEndDate(),params));
		}
		else
		{
			builder.append(SclDateUtility.getMtdClauseQuery("l:creationtime",params));
		}

		if(leadRequestData.getStatus()!=null && !leadRequestData.getStatus().isEmpty()) {
			List<LeadStatus> leadStatusList = leadRequestData.getStatus().stream().map(status -> LeadStatus.valueOf(status)).collect(Collectors.toList());
			if(leadStatusList!=null && !leadStatusList.isEmpty())
			{
				builder.append(" AND {l:status} IN (?leadStatusList) ");
				params.put("leadStatusList", leadStatusList);
			}
		}

		FilterTalukaData filterTalukaData = new FilterTalukaData();
		List<SubAreaMasterModel> subAreaList = territoryManagementService.getTaulkaForUser(filterTalukaData);
		if(subAreaList!=null && !subAreaList.isEmpty())
		{
			builder.append(" AND {l:subAreaMaster} IN (?subAreaList) ");
			params.put("subAreaList", subAreaList);
		}

		if(StringUtils.isNotBlank(leadRequestData.getLeadType()))
		{
			LeadType leadType = LeadType.valueOf(leadRequestData.getLeadType());
			builder.append(" AND {l:leadType} =?leadType ");
			params.put("leadType", leadType);
		}

		if(StringUtils.isNotBlank(leadRequestData.getSearchKey())){
			String search= "%".concat(leadRequestData.getSearchKey().toUpperCase()).concat("%");
			builder.append(" AND (UPPER({l:name}) like ?search OR UPPER({l:leadId}) like ?search OR " +
					"UPPER({l:contactNo}) like ?search) ");
			params.put("search", search);
		}

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		parameter.setFlexibleSearchQuery(query);
		parameter.setSortCodeToQueryAlias(getLeadMasterSortCodeToQueryAlias());
		return getPaginatedFlexibleSearchService().search(parameter);
	}

	@Override
	public SearchPageData<MeetingScheduleModel> getPaginatedScheduleMeetList(SearchPageData searchPageData, String meetingType, String startDate, String endDate, String searchFilter, List<String> status) {

		final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
		parameter.setSearchPageData(searchPageData);
		final Map<String, Object> params = new HashMap<String, Object>();
		B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
		StringBuilder builder = new StringBuilder();
		if(currentUser instanceof SclUserModel) {
			builder.append("select {m.pk} from {MeetingSchedule as m} where {m.scheduledBy}=?currentUser ");
		}
		else {
			builder.append("select {m.pk} from {MeetingSchedule as m join MeetingScheduleToCustomerRel as r on {m.pk}={r.source} join SclCustomer as s on {r.target}={s.pk}}  where {r.target} in (?currentUser) ");
		}

		params.put("currentUser", userService.getCurrentUser());
		if(StringUtils.isNotBlank(searchFilter)){

			builder.append(" AND {m:meetingScheduleId} like ?searchFilter ");
			params.put("searchFilter","%" + searchFilter + "%");

		}
		if(startDate!=null && endDate!=null){
			builder.append(" and ");
			builder.append(SclDateUtility.getDateRangeClauseQuery("m:eventDate",startDate, endDate,params));
		}
		else
		{
			builder.append(" and ");
			builder.append(SclDateUtility.getMtdClauseQuery("m:eventDate",params));
		}

		if(status!=null && !status.isEmpty()) {
			List<MeetStatus> leadStatusList = status.stream().map(MeetStatus::valueOf).collect(Collectors.toList());
			if(leadStatusList!=null && !leadStatusList.isEmpty())
			{
				builder.append(" and ");
				if(leadStatusList.contains(MeetStatus.UPCOMING))
				{
					builder.append(" {m:status} IN (?leadStatusList) and {m:eventDate}>=?currentDate ");
				}
				else if(leadStatusList.contains(MeetStatus.NOT_HAPPENED))
				{
					builder.append(" ({m:status}=?status and {m:eventDate}<?currentDate) OR ({m:status} in (?leadStatusList)) ");
				}
				else
				{
					builder.append(" {m:status} IN (?leadStatusList) ");
				}
				String currentDate = LocalDateTime.now().plusHours(5).plusMinutes(30).toString();
				params.put("currentDate",currentDate);
				params.put("status",MeetStatus.UPCOMING);
				params.put("leadStatusList", leadStatusList);
			}
		}

		if(meetingType!=null) {
			builder.append(" and ");
			builder.append(" {m:meetType}=?meetType ");
			params.put("meetType",MeetingType.valueOf(meetingType));

		}

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		parameter.setFlexibleSearchQuery(query);
		parameter.setSortCodeToQueryAlias(getScheduleMeetingSortCodeToQueryAlias());
		return getPaginatedFlexibleSearchService().search(parameter);

	}

	@Override
	public List<PointRequisitionModel> getInfluencerSalesData(String uid, BaseSiteModel currentBaseSite) {
		final Map<String, Object> params = new HashMap<>();
		final StringBuilder builder = new StringBuilder("SELECT {p:pk} FROM {PointRequisition as p} where {p:brand}=?currentBaseSite and {p:requestRaisedFor}=?uid ");
		params.put("uid",uid);
		params.put("currentBaseSite",currentBaseSite);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Collections.singletonList(PointRequisitionModel.class));
		final SearchResult<PointRequisitionModel> searchResult = flexibleSearchService.search(query);
		List<PointRequisitionModel> result = searchResult.getResult();
		return result!=null && !result.isEmpty() ? result : Collections.emptyList();
	}


	@Override
	public List<InfluencerVisitMasterModel> getInfluencerVisitDetails() {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {InfluencerVisitMaster} WHERE {endVisitTime} IS NOT NULL ");
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(InfluencerVisitMasterModel.class));
		query.addQueryParameters(params);
		final SearchResult<InfluencerVisitMasterModel> searchResult = flexibleSearchService.search(query);
		List<InfluencerVisitMasterModel> result = searchResult.getResult();
		return result!=null && !result.isEmpty() ? result : Collections.emptyList();
	}


	@Override
	public List<MeetingScheduleModel> getInfluencerMeetCompleted() {
		final Map<String, Object> params = new HashMap<>();
		final StringBuilder builder = new StringBuilder("select {m.pk} from {MeetingSchedule as m} where {m.meetingForm} is not null ").append(slctCrmIntegrationDao.appendIntegrationSetting("INFLUENCER_MEETING_COMPLETION",params));
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Collections.singletonList(MeetingScheduleModel.class));
		final SearchResult<MeetingScheduleModel> searchResult = flexibleSearchService.search(query);
		List<MeetingScheduleModel> result = searchResult.getResult();
		return result!=null && !result.isEmpty() ? result : Collections.emptyList();
	}



	public PaginatedFlexibleSearchService getPaginatedFlexibleSearchService() {
		return paginatedFlexibleSearchService;
	}

	public void setPaginatedFlexibleSearchService(PaginatedFlexibleSearchService paginatedFlexibleSearchService) {
		this.paginatedFlexibleSearchService = paginatedFlexibleSearchService;
	}

	public Map<String, String> getLeadMasterSortCodeToQueryAlias() {
		return leadMasterSortCodeToQueryAlias;
	}

	public void setLeadMasterSortCodeToQueryAlias(Map<String, String> leadMasterSortCodeToQueryAlias) {
		this.leadMasterSortCodeToQueryAlias = leadMasterSortCodeToQueryAlias;
	}

	public Map<String, String> getScheduleMeetingSortCodeToQueryAlias() {
		return scheduleMeetingSortCodeToQueryAlias;
	}

	public void setScheduleMeetingSortCodeToQueryAlias(Map<String, String> scheduleMeetingSortCodeToQueryAlias) {
		this.scheduleMeetingSortCodeToQueryAlias = scheduleMeetingSortCodeToQueryAlias;
	}

	@Override
	public Integer getVisitCountMTD(SclCustomerModel sclCustomer, int month, int year) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT COUNT({endVisitTime}) FROM {InfluencerVisitMaster AS c} WHERE {c.sclCustomer}=?sclCustomer AND {c.endVisitTime} IS NOT NULL AND ").append(SclDateUtility.getDateClauseQueryByMonthYear("c.endVisitTime", month, year, params));
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
	public Integer getInfLeadGeneratedCount(SclCustomerModel sclCustomer) {
		final Map<String, Object> params = new HashMap<String, Object>();
		Integer lastXDays = dataConstraintDao.findDaysByConstraintName("PAST_YEAR_LEADS");
		LocalDate lastYear = null;
		if (lastXDays != null) {
			lastYear = LocalDate.now().minusYears(lastXDays);
			Date startDate = Date.from(lastYear.atStartOfDay(ZoneId.systemDefault()).toInstant());
			Date endDate = Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

		B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
		final StringBuilder builder = new StringBuilder();
		List<CounterType> counter = new ArrayList<>();
		if (currentUser.getUserType() != null) {
			if (currentUser.getUserType().getCode().equalsIgnoreCase("TSO")) {
				builder.append("SELECT COUNT({l:pk}) FROM {LeadMaster as l join InfluencerVisitMaster as c on {l:counterVisit}={c:pk}} WHERE {l:counterType} in (?counterType) and {l:status}=?leadStatus and {l:creationtime} >=?startDate and {l:creationtime} <?endDate and {c:sclCustomer}=?sclCustomer");
				counter.add(CounterType.INFLUENCER);
				counter.add(CounterType.SITE);
				params.put("counterType", counter);
			} else {
				builder.append("SELECT COUNT({l:pk}) FROM {LeadMaster as l join InfluencerVisitMaster as c on {l:counterVisit}={c:pk}} WHERE {l:counterType}=?counterType and {l:status}=?leadStatus and {l:creationtime} >=?startDate and {l:creationtime} <?endDate and {c:sclCustomer}=?sclCustomer");
				params.put("counterType", CounterType.INFLUENCER);
			}
		} else {
			builder.append("SELECT COUNT({l:pk}) FROM {LeadMaster as l join InfluencerVisitMaster as c on {l:counterVisit}={c:pk}} WHERE {l:counterType}=?counterType and {l:status}=?leadStatus and {l:creationtime} >=?startDate and {l:creationtime} <?endDate and {c:sclCustomer}=?sclCustomer");
			params.put("counterType", CounterType.INFLUENCER);
		}
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		params.put("leadStatus", LeadStatus.APPROVED);
		params.put("sclCustomer", sclCustomer);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Integer.class));
		query.addQueryParameters(params);
		final SearchResult<Integer> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0;

	}
		return 0;
	}

	@Override
	public int getInfluencerVisitHistoryCount(String startDate, String endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT COUNT({endVisitTime}) FROM {VisitMaster as v JOIN InfluencerVisitMaster as iv on {iv.visit}={v.pk}} WHERE {v.user}=?currentUser and {iv.endVisitTime} IS NOT NULL AND {iv.endVisitTime}>=?startDate AND  {iv.endVisitTime}<?endDate ");
		SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
		params.put("startDate",startDate);
		params.put("endDate",endDate);
		params.put("currentUser",currentUser);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Integer.class));
		query.addQueryParameters(params);
		final SearchResult<Integer> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null?searchResult.getResult().get(0):0;
		else
			return 0;
	}
}
