package com.eydms.core.dao.impl;


import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.dao.EyDmsUserDao;
import com.eydms.core.jalo.EyDmsUser;
import com.eydms.core.model.*;
import com.eydms.core.region.dao.impl.DistrictMasterDaoImpl;
import com.eydms.core.services.TerritoryManagementService;
import de.hybris.platform.b2b.jalo.B2BCustomer;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class EyDmsUserDaoImpl implements EyDmsUserDao {

	private static final Logger LOGGER = Logger.getLogger(EyDmsUserDaoImpl.class);
	@Autowired
	FlexibleSearchService flexibleSearchService;
	@Autowired
	UserService userService;
	@Autowired
	TerritoryManagementService territoryService;

	@Autowired
	BaseSiteService baseSiteService;

	@Autowired
	DistrictMasterDaoImpl districtMasterDaoImpl;

	@Autowired
	TerritoryManagementDaoImpl territoryManagementDaoImpl;
	@Override
	public double getOutstandingAmountForSO(List<String> customerCode) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT SUM({cao.totalOutstanding}) FROM {CreditAndOutstanding AS cao} WHERE {cao.customerCode} IN (?customerCode)");
		params.put("customerCode", customerCode);	
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
	public List<List<Double>> getOutstandingBucketsForSO(List<String> customerCode) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT SUM({bucket1}), SUM({bucket2}), SUM({bucket3}), SUM({bucket4}), SUM({bucket5}), SUM({bucket6}), SUM({bucket7}), SUM({bucket8}), SUM({bucket9}), SUM({bucket10}) FROM {CreditAndOutstanding} WHERE {customerCode} IN (?customerCode)");
		params.put("customerCode", customerCode);	
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Double.class,Double.class,Double.class,Double.class,Double.class,Double.class,Double.class,Double.class,Double.class,Double.class));
		query.addQueryParameters(params);
		final SearchResult<List<Double>> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();

	}
	

	@Override
	public List<EyDmsUserModel> getAllActiveSO() {

		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {EyDmsUser} WHERE {active}=1");
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(EyDmsUser.class));
		final SearchResult<EyDmsUserModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}
	
	//New Territory Change
	@Override
	public Double getCustomerSaleQTY(String customerUID, BaseSiteModel site){
		final Map<String, Object> params = new HashMap<String, Object>();
		final UserModel user = (EyDmsUserModel) userService.getCurrentUser();
		OrderStatus orderStatus = OrderStatus.DELIVERED;
		final StringBuilder builder = new StringBuilder("SELECT sum({oe.quantityInMT}) from {OrderEntry AS oe " +
				"JOIN Order as o ON {oe:order}={o:pk} " +
				"JOIN EyDmsCustomer as sc on {o:user}={sc:pk} " +
				"JOIN OrderStatus as os on {o:status}={os:pk}} "+
				"where {o:placedBy}=?placedBy " +
				"and {o:site} =?site " +
				"and {os:code}=?orderStatuscode " +
				"and {sc:uid}=?customerUID  ");
		//"JOIN CustomerSubAreaMapping as csm on {csm:eydmsCustomer}={sc:pk} " +
		params.put("placedBy",user);
		params.put("customerUID",customerUID);
		params.put("orderStatuscode",orderStatus.getCode());
		params.put("site",site);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Arrays.asList(Double.class));
		final SearchResult<Double> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult()!=null && !searchResult.getResult().isEmpty()
				&& searchResult.getResult().get(0)!=null
				?searchResult.getResult().get(0): Double.valueOf(0);
	}

	@Override
	public EyDmsCustomerModel getCustomerModelAndOrdersForSpecificDate(String customerUID,  BaseSiteModel site,String startDate,String endDate) {
		final Map<String, Object> params = new HashMap<>();
		final UserModel user = (EyDmsUserModel) userService.getCurrentUser();
		OrderStatus orderStatus = OrderStatus.DELIVERED;
		final StringBuilder builder = new StringBuilder("SELECT {sc:pk} from {OrderEntry AS oe " +
				"JOIN Order as o ON {oe:order}={o:pk} " +
				"JOIN EyDmsCustomer as sc on {o:user}={sc:pk} " +
				"JOIN OrderStatus as os on {o:status}={os:pk}} "+
				"where {oe.deliveredDate}>=?startDate and {oe.deliveredDate}<=?endDate " +
				"and {os:code}=?orderStatuscode " +
				"and {sc:uid}=?customerUID  ");
		params.put("customerUID",customerUID);
		params.put("orderStatuscode",orderStatus.getCode());
	    params.put("startDate",startDate);
		params.put("endDate",endDate);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
		query.addQueryParameters(params);
		final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	//New Territory Change
	@Override
	public List<EyDmsCustomerModel> getAllCustomerForSubArea(String subArea, BaseSiteModel site){

		final Map<String, Object> params = new HashMap<>();
		final StringBuilder builder = new StringBuilder("SELECT {eydmsCustomer} FROM {CustomerSubAreaMapping as csm " +
				"join SubAreaMaster as sm on {csm.subAreaMaster}={sm.pk} } WHERE  {isActive} = '1' AND {brand} = ?brand");
		params.put("subArea", subArea);
		params.put("brand", site);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
		query.addQueryParameters(params);
		final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
		List<EyDmsCustomerModel> result = searchResult.getResult();

		return result!=null && !result.isEmpty() ? result : Collections.emptyList();
	}

	//New Territory Change
	@Override
	public List<EyDmsCustomerModel> getAllCustomersForSubAreaByOnboardingStatus( BaseSiteModel site, String onboardingStatus){
		B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
		StringBuilder builder=new StringBuilder();
		if(currentUser instanceof EyDmsUserModel){
			 builder = new StringBuilder("SELECT {pk} FROM {EyDmsCustomer as sc JOIN " +
					"CustomerSubAreaMapping as csm on {sc:pk}={csm:eydmsCustomer}} WHERE  {csm:isActive} = ?active AND {csm:brand} = ?brand AND {sc:customerOnboardingStatus} in ({{select {pk} from {CustomerOnboardingStatus as cos} where {cos:code}= ?onboardingStatus}})");
		}
		else if(currentUser instanceof  EyDmsCustomerModel){
				builder = new StringBuilder("SELECT {pk} FROM {EyDmsCustomer as sc} WHERE {sc:customerOnboardingStatus} in ({{select {pk} from {CustomerOnboardingStatus as cos} where {cos:code}= ?onboardingStatus}}) and {sc.onboardingPlacedBy}=?currentUser");
		}
		final Map<String, Object> params = new HashMap<String, Object>();
		boolean active = Boolean.TRUE;
		params.put("active", active);
		params.put("brand", site);
		params.put("onboardingStatus", onboardingStatus);
		params.put("currentUser", currentUser);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
		query.addQueryParameters(params);
		final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
		List<EyDmsCustomerModel> result = searchResult.getResult();

		return result!=null && !result.isEmpty() ? result : Collections.emptyList();
	}

	//New Territory Change
	@Override
	public List<CustomerSubAreaMappingModel> getAllDistrictForSO(UserModel eydmsUser, BaseSiteModel site) {

		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {CustomerSubAreaMapping AS cm} WHERE {cm:eydmsCustomer} = ?eydmsUser AND {cm:isActive} = ?active AND {cm:brand} = ?brand");
		boolean active = Boolean.TRUE;
		params.put("eydmsUser", eydmsUser);
		params.put("active", active);
		params.put("brand", site);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		//query.setResultClassList(Arrays.asList(String.class,String.class,String.class));
		query.addQueryParameters(params);
		final SearchResult<CustomerSubAreaMappingModel> searchResult = flexibleSearchService.search(query);
		List<CustomerSubAreaMappingModel> result = searchResult.getResult();

		return result!=null ? result : Collections.emptyList();
	}

	@Override
	public double getSalesQuantity(String customerNo, String startDate, String endDate){
		LOGGER.info(String.format("Getting Sale QTY for customerNo :: %s startDate :: %s endDate :: %s",customerNo,startDate,endDate));
		final Map<String, Object> params = new HashMap<>();
		final StringBuilder builder = new StringBuilder("SELECT sum({oe.quantityInMT}) from {OrderEntry AS oe " +
				"JOIN Order as o ON {oe:order}={o:pk} " +
				"JOIN EyDmsCustomer as sc on {o:user}={sc:pk}} " +
				"WHERE {oe.cancelledDate} is null and " +
				"{oe:invoiceCreationDateAndTime}>=?startDate AND {oe:invoiceCreationDateAndTime} <?endDate " +
				"AND {sc:uid}=?customerNo ");
		params.put("orderStatus", OrderStatus.DELIVERED);
		params.put("customerNo", customerNo);
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Double.class));
		query.addQueryParameters(params);
		final SearchResult<Double> searchResult = flexibleSearchService.search(query);
		if(CollectionUtils.isNotEmpty(searchResult.getResult())) {
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
		}else {
			return 0.0;
		}
	}

	@Override
	public double getSalesQuantityForSalesPerformance(String customerUID, String startDate, String endDate,List<String> doList, List<String> subAreaList){
		LOGGER.info(String.format("Getting Sale QTY for customerUID :: %s startDate :: %s endDate :: %s",customerUID,startDate,endDate));
		final Map<String, Object> params = new HashMap<String, Object>();
		OrderStatus orderStatus = OrderStatus.DELIVERED;
//		final StringBuilder builder = new StringBuilder("SELECT sum({oe.quantityInMT}) from {OrderEntry AS oe " +
//				"JOIN Order as o ON {oe:order}={o:pk} " +
//				"JOIN EyDmsCustomer as sc on {o:user}={sc:pk} " +
//				"JOIN OrderStatus as os on {o:status}={os:pk}} "+
//				"where {o:placedBy}=?placedBy " +
//				"and {o:site} =?site " +
//				"and {oe.deliveredDate}>=?startDate and {oe.deliveredDate}<=?endDate " +
//				"and {os:code}=?orderStatuscode " +
//				"and {sc:uid}=?customerUID  ");
//		params.put("placedBy",user);

		List<DistrictMasterModel> list = new ArrayList<>();
		List<SubAreaMasterModel> list1 = new ArrayList<>();

		/*final StringBuilder builder = new StringBuilder("SELECT sum({oe.quantityInMT}) from {OrderEntry AS oe " +
				"JOIN Order as o ON {oe:order}={o:pk} " +
				"JOIN EyDmsCustomer as sc on {o:user}={sc:pk}} " +
				"where {oe.deliveredDate}>=?startDate and {oe.deliveredDate}<=?endDate " +
				"and {oe:status}=?orderStatus " +
				"and {sc:uid}=?customerUID  ");*/

		final StringBuilder builder = new StringBuilder("SELECT sum({oe.quantityInMT}) from {OrderEntry AS oe " +
				"JOIN Order as o ON {oe:order}={o:pk} " +
				"JOIN EyDmsCustomer as sc on {o:user}={sc:pk}} " +
				"where {oe.invoiceCreationDateAndTime}>=?startDate and {oe.invoiceCreationDateAndTime}<?endDate " +
				"and {oe.cancelledDate} is null " +
				"and {sc:uid}=?customerUID  ");
		if(doList!=null && !doList.isEmpty()){
			for(String code: doList){
				list.add(districtMasterDaoImpl.findByCode(code));
			}
			params.put("doList",list);
			builder.append(" and {o.districtMaster} in (?doList) ");
		}
		if(subAreaList!=null && !subAreaList.isEmpty()){
			for(String id: subAreaList){
				list1.add(territoryManagementDaoImpl.getTerritoryById(id));
			}
			params.put("subAreaList",list1);
			builder.append(" and {o.subAreaMaster} in (?subAreaList) ");
		}
		params.put("customerUID",customerUID);
		params.put("orderStatus",orderStatus);
		//params.put("subArea",territoryService.getTerritoryById(subArea));
//		params.put("site",site);
		params.put("startDate",startDate);
		params.put("endDate",endDate);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Double.class));
		query.addQueryParameters(params);
		final SearchResult<Double> searchResult = flexibleSearchService.search(query);
		if(CollectionUtils.isNotEmpty(searchResult.getResult())) {
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
		}else {
			return 0.0;
		}
	}
	@Override
	public double getSalesQuantityForBottomLogging(String customerUID, String startDate, String endDate,List<String> doList, List<String> subAreaList){
		LOGGER.info(String.format("Getting Sale QTY for customerUID :: %s startDate :: %s endDate :: %s",customerUID,startDate,endDate));
		final Map<String, Object> params = new HashMap<String, Object>();
		//final EyDmsUserModel user = (EyDmsUserModel) userService.getCurrentUser();
		OrderStatus orderStatus = OrderStatus.DELIVERED;
//		final StringBuilder builder = new StringBuilder("SELECT sum({oe.quantityInMT}) from {OrderEntry AS oe " +
//				"JOIN Order as o ON {oe:order}={o:pk} " +
//				"JOIN EyDmsCustomer as sc on {o:user}={sc:pk} " +
//				"JOIN OrderStatus as os on {o:status}={os:pk}} "+
//				"where {o:placedBy}=?placedBy " +
//				"and {o:site} =?site " +
//				"and {oe.deliveredDate}>=?startDate and {oe.deliveredDate}<=?endDate " +
//				"and {os:code}=?orderStatuscode " +
//				"and {sc:uid}=?customerUID  ");
//		params.put("placedBy",user);

		List<DistrictMasterModel> list = new ArrayList<>();
		List<SubAreaMasterModel> list1 = new ArrayList<>();

	/*	final StringBuilder builder = new StringBuilder("SELECT sum({oe.quantityInMT}) from {OrderEntry AS oe " +
				"JOIN Order as o ON {oe:order}={o:pk} " +
				"JOIN EyDmsCustomer as sc on {o:user}={sc:pk}} where "+
				"{oe.deliveredDate}>=?startDate and {oe.deliveredDate}<=?endDate " +
				"and {sc:uid}=?customerUID and {oe:status}=?orderStatus");*/

		final StringBuilder builder = new StringBuilder("SELECT sum({oe.quantityInMT}) from {OrderEntry AS oe " +
				"JOIN Order as o ON {oe:order}={o:pk} " +
				"JOIN EyDmsCustomer as sc on {o:user}={sc:pk}} where "+
				"{oe.invoiceCreationDateAndTime}>=?startDate and {oe.invoiceCreationDateAndTime}<?endDate " +
				"and {sc:uid}=?customerUID and {oe.cancelledDate} is null");
		if(doList!=null && !doList.isEmpty()){
			for(String code: doList){
				list.add(districtMasterDaoImpl.findByCode(code));
			}
			params.put("doList",list);
			builder.append(" and {o.districtMaster} in (?doList) ");
		}
		if(subAreaList!=null && !subAreaList.isEmpty()){
			for(String id: subAreaList){
				list1.add(territoryManagementDaoImpl.getTerritoryById(id));
			}
			params.put("subAreaList",list1);
			builder.append(" and {o.subAreaMaster} in (?subAreaList) ");
		}

		params.put("customerUID",customerUID);
//		params.put("site",site);
		params.put("orderStatus",orderStatus);
		params.put("startDate",startDate);
		params.put("endDate",endDate);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Double.class));
		query.addQueryParameters(params);
		final SearchResult<Double> searchResult = flexibleSearchService.search(query);
		if(CollectionUtils.isNotEmpty(searchResult.getResult())) {
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
		}else {
			return 0.0;
		}
	}
	@Override
	public double getCustomerTarget(String customerNo, String month, String year){
		//SubAreaMasterModel subAreaMaster = territoryService.getTerritoryById(subArea);
		LOGGER.info(String.format("Getting Target For customerNo :: %s month :: %s year :: %s",customerNo,month,year));
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {totalTarget} from {MonthlySales as m " +
				"JOIN DealerRevisedMonthlySales as dp on {dp:monthlySales}={m:pk}} " +
				"where " +
				"{dp:customerCode}=?customerCode " +
				"and {monthName}=?monthName " +
				"and {monthYear}=?monthYear " );
		params.put("customerCode",customerNo);
		params.put("monthName",month);
		params.put("monthYear",year);
		//params.put("subArea",subAreaMaster.getTaluka());
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Double.class));
		query.addQueryParameters(params);
		final SearchResult<Double> searchResult = flexibleSearchService.search(query);
		if(CollectionUtils.isNotEmpty(searchResult.getResult())) {
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
		}else {
			return 0.0;
		}
	}
	
	@Override
	public List<EyDmsCustomerModel> getCustomerwithoutCustomerNumber(BaseSiteModel site){
		final Map<String, Object> queryParams = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {sc:pk} FROM {eydmsCustomer AS sc} " +
				"WHERE {sc:customerNo} IS NULL");
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(queryParams);
		final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
		List<EyDmsCustomerModel> result = searchResult.getResult();
		return result!=null ? result : Collections.emptyList();
	}

	@Override
	public List<MeetingScheduleModel> getInfluencerMeetCards(UserModel currentUser) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {MeetingSchedule} WHERE {scheduledBy} = ?currentUser");
		params.put("currentUser", currentUser);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(MeetingScheduleModel.class));
		query.addQueryParameters(params);
		final SearchResult<MeetingScheduleModel> searchResult = flexibleSearchService.search(query);
		List<MeetingScheduleModel> result = searchResult.getResult();
		return result != null && !result.isEmpty() ? result : Collections.emptyList();
	}
	
	@Override
	public AddressModel getAddressByErpId(String erpAddressId, EyDmsCustomerModel customer) {

		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("erpAddressId", erpAddressId);
		params.put("owner", customer);

		String searchQuery = "select {a.pk} from {Address as a} where {a.erpAddressId}=?erpAddressId and {a.owner}=?owner ";
		final FlexibleSearchQuery query = new FlexibleSearchQuery(searchQuery);
		query.addQueryParameters(params);
		final SearchResult<AddressModel> searchResult = flexibleSearchService.search(query);
		return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;

	}

	@Override
	public AddressModel getAddressByPk(String pk) {

		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("pk", pk);

		String searchQuery = "select {a.pk} from {Address as a} where {a.pk}=?pk ";
		final FlexibleSearchQuery query = new FlexibleSearchQuery(searchQuery);
		query.addQueryParameters(params);
		final SearchResult<AddressModel> searchResult = flexibleSearchService.search(query);
		return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;

	}

	@Override
	public AddressModel getDealerAddressByRetailerPk(String retailerAddressPk, EyDmsCustomerModel customer) {

		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("retailerAddressPk", retailerAddressPk);
		params.put("owner", customer);

		String searchQuery = "select {a.pk} from {Address as a} where {a.retailerAddressPk}=?retailerAddressPk and {a.owner}=?owner ";
		final FlexibleSearchQuery query = new FlexibleSearchQuery(searchQuery);
		query.addQueryParameters(params);
		final SearchResult<AddressModel> searchResult = flexibleSearchService.search(query);
		return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;

	}
	
	@Override
	public List<String> filterAddressByLpSource(List<String> stateDistrictTalukaList) {
		List<String> output = new ArrayList<String>(); 
		if(stateDistrictTalukaList!=null && !stateDistrictTalukaList.isEmpty()) {
			final Map<String, Object> params = new HashMap<String, Object>();
			params.put("stateDistrictTaluka", stateDistrictTalukaList);
			params.put("brand", baseSiteService.getCurrentBaseSite());
			String searchQuery = "select CONCAT(Upper({l.destinationState}),upper({l.destinationDistrict}),upper({l.destinationTaluka}),upper({l.destinationCity})) from {DestinationSourceMaster as l} where {brand}=?brand and CONCAT(Upper({l.destinationState}),upper({l.destinationDistrict}),upper({l.destinationTaluka}),upper({l.destinationCity})) in (?stateDistrictTaluka) group by  CONCAT(Upper({l.destinationState}),upper({l.destinationDistrict}),upper({l.destinationTaluka}),upper({l.destinationCity})) ";
			final FlexibleSearchQuery query = new FlexibleSearchQuery(searchQuery);
			query.setResultClassList(Collections.singletonList(String.class));
			query.addQueryParameters(params);
			final SearchResult<String> searchResult = flexibleSearchService.search(query);
			if(searchResult.getResult()!=null ) {
				output = searchResult.getResult();
			}
		}
		return output;
	}

    @Override
    public List<EyDmsCustomerModel> getCountOfCreditLimitBreachedUser(List<SubAreaMasterModel> subAreas, String date) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {csm.eydmsCustomer} FROM {CustomerSubAreaMapping as csm join EyDmsCustomer as sc on {csm.eydmsCustomer}={sc.pk}} WHERE {csm.subAreaMaster} in (?subAreas) AND {csm.eydmsCustomer} is not null and {sc.creditLimitBreachedDate} like ?creditLimitBreachedDate and {sc.isCreditLimitBreached} =?isCreditLimitBreached ");
		boolean isCreditLimitBreached = Boolean.TRUE;
		params.put("subAreas", subAreas);
		params.put("creditLimitBreachedDate",date + "%");
		params.put("isCreditLimitBreached" , isCreditLimitBreached);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
		query.addQueryParameters(params);
		final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
		List<EyDmsCustomerModel> result = searchResult.getResult();

		return result!=null && !result.isEmpty() ? result : Collections.emptyList();
    }

	@Override
	public Integer getDealersCountForDSOGreaterThanThirty(List<String> customerNos) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT COUNT({totalOutstanding}/{dailyAverageSales}) FROM {CreditAndOutstanding} WHERE {customerCode} IN (?customerNos) AND {totalOutstanding} > 0 AND  {dailyAverageSales} > 0 AND {totalOutstanding}/{dailyAverageSales} > 30");
		params.put("customerNos", customerNos);	
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Integer.class));
		query.addQueryParameters(params);
		final SearchResult<Integer> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0;	
		else
			return 0;
	}

	@Override
	public double getSalesQuantityForCustomerList(List<String> customerNos, String startDate, String endDate, BaseSiteModel brand){
		LOGGER.info(String.format("Getting Sale QTY for customerNo :: %s startDate :: %s endDate :: %s",customerNos,startDate,endDate));
		final Map<String, Object> params = new HashMap<>();
		final StringBuilder builder = new StringBuilder("SELECT sum({oe.quantityInMT}) from {OrderEntry AS oe " +
				"JOIN Order as o ON {oe:order}={o:pk} " +
				"JOIN EyDmsCustomer as sc on {o:user}={sc:pk}} " +
				"WHERE " +
				"{oe:deliveredDate}>=?startDate AND {oe:deliveredDate} <?endDate " +
				"AND {sc:customerNo} IN (?customerNos)  and {o:status}=?orderStatus");
		params.put("orderStatus", OrderStatus.DELIVERED);
		params.put("customerNos", customerNos);
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Double.class));
		query.addQueryParameters(params);
		final SearchResult<Double> searchResult = flexibleSearchService.search(query);
		if(CollectionUtils.isNotEmpty(searchResult.getResult())) {
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
		}else {
			return 0.0;
		}
	}
	



}
