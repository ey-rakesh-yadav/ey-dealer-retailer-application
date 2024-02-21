package com.eydms.core.dao.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.annotation.Resource;


import com.eydms.core.enums.*;

import com.eydms.core.enums.CounterType;
import com.eydms.core.enums.PointRequisitionStatus;

import com.eydms.core.model.*;
import com.eydms.core.utility.EyDmsDateUtility;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.c2l.BrandModel;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.VendorModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import org.apache.log4j.Logger;

import com.eydms.core.dao.SlctCrmIntegrationDao;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.site.BaseSiteService;
import org.springframework.beans.factory.annotation.Autowired;


public class SlctCrmIntegrationDaoImpl implements SlctCrmIntegrationDao {
	private static final Logger LOG = Logger.getLogger(SlctCrmIntegrationDaoImpl.class);

	@Resource
	FlexibleSearchService flexibleSearchService;

	@Resource
	BaseSiteService baseSiteService;

	@Resource
	EnumerationService enumerationService;

	@Autowired
	private SessionService sessionService;
	
	@Autowired
	private SearchRestrictionService searchRestrictionService;

	@Override
	public List<MarketMappingDetailsModel> getAllMarketMappingDetails() {

		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {MarketMappingDetails} where {synced} = ?status");

		LOG.info("This is MarketMappingDetails query: "+builder);
		params.put("status", Boolean.FALSE);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(MarketMappingDetailsModel.class));
		query.addQueryParameters(params);
		final SearchResult<MarketMappingDetailsModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<VisitMasterModel> getAllVisit() {

		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {VisitMaster} where {synced} = ?status");

		LOG.info("This is VisitMaster query: "+builder);
		params.put("status", Boolean.FALSE);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(VisitMasterModel.class));
		query.addQueryParameters(params);
		final SearchResult<VisitMasterModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<CounterVisitMasterModel> getAllCounterVisit() {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {CounterVisitMaster} where {synced} = ?status and {counterType} in (?counterType)");

		List<CounterType> counterTypes = new ArrayList<>();
		counterTypes.add(CounterType.DEALER);
		counterTypes.add(CounterType.RETAILER);
		params.put("counterType",counterTypes);
		params.put("status", Boolean.FALSE);
		LOG.info("This is CounterVisitMaster query: "+builder);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(CounterVisitMasterModel.class));
		query.addQueryParameters(params);
		final SearchResult<CounterVisitMasterModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<EyDmsCustomerModel> getAllInfluencerDetails() {

		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {EyDmsCustomer} where {counterType} = ?counterType and {applicationDate} is not null").append(this.appendIntegrationSetting("ONBOARDED_INFLUENCER_DETAILS",params));

		params.put("counterType", CounterType.INFLUENCER);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
		query.addQueryParameters(params);
		LOG.info("This is Influencer Onboarding query: "+query);
		final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<List<Object>> getAllRetailerSalesInfo() {

		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {ord.code},{ord.erpOrderNumber},{deal.customerNo},{ret.uid}, {ret.customerNo} from {Order as ord JOIN EyDmsCustomer as deal on {ord.user}={deal.pk} JOIN EyDmsCustomer as ret on {ord.retailer}={ret.pk}} where {ret.synced} = ?status and {ord.erpOrderNumber} IS NOT NULL and {ord.retailer} IS NOT NULL");

		LOG.info("This is retailer sales information query: "+builder);
		params.put("status", Boolean.FALSE);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Arrays.asList(String.class, String.class, String.class, String.class, String.class));
		final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<DestinationSourceMasterModel> getLpSourceMasterList(BaseSiteModel brand,
																	DeliveryModeModel deliveryMode, String grade, String packaging, String destCityId) {

		if(destCityId!=null) {
			Map<String, Object> map = new HashMap<>();
			map.put(DestinationSourceMasterModel.DELIVERYMODE, deliveryMode);
			map.put(DestinationSourceMasterModel.BRAND, brand);
			map.put(DestinationSourceMasterModel.ORDERTYPE, OrderType.SO);
			map.put(DestinationSourceMasterModel.CUSTOMERCATEGORY, CustomerCategory.TR);
			map.put(DestinationSourceMasterModel.GRADE, grade);
			map.put(DestinationSourceMasterModel.PACKAGING, packaging);
			map.put(DestinationSourceMasterModel.DESTINATIONCITYID, destCityId);

			final StringBuilder builder = new StringBuilder("select {pk} from {DestinationSourceMaster} where {deliveryMode}=?deliveryMode and {brand}=?brand and {orderType}=?orderType and {customerCategory}=?customerCategory and {grade}=?grade and {packaging}=?packaging and {destinationCityId}=?destinationCityId");

			final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
			query.getQueryParameters().putAll(map);
			query.setResultClassList(Collections.singletonList(DestinationSourceMasterModel.class));
			final SearchResult<DestinationSourceMasterModel> searchResult = flexibleSearchService.search(query);
			if(searchResult.getResult()!=null && !searchResult.getResult().isEmpty()) {
				return searchResult.getResult();
			}
		}
		return Collections.emptyList();
	}

	@Override
	public CreditAndOutstandingModel getCrmOutstandingDetails(String custCode) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {CreditAndOutstanding} where {customerCode} = ?custCode");

		params.put("custCode", custCode);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(CreditAndOutstandingModel.class));
		query.addQueryParameters(params);
		final SearchResult<CreditAndOutstandingModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public DJPCounterScoreMasterModel getCounterScoreDetails(String id) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {DJPCounterScoreMaster} where {id} = ?id");

		params.put("id", id);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(DJPCounterScoreMasterModel.class));
		query.addQueryParameters(params);
		final SearchResult<DJPCounterScoreMasterModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	public DJPRouteScoreMasterModel getRouteScoreDetails(String id) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {DJPRouteScoreMaster} where {id} = ?id");

		params.put("id", id);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(DJPRouteScoreMasterModel.class));
		query.addQueryParameters(params);
		final SearchResult<DJPRouteScoreMasterModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public ObjectiveModel getObjectiveDetails(String objectiveId) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {Objective} where {objectiveId} = ?objectiveId");

		params.put("objectiveId", objectiveId);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(ObjectiveModel.class));
		query.addQueryParameters(params);
		final SearchResult<ObjectiveModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public EyDmsCustomerModel getCustomerDetails(String uid) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {EyDmsCustomer} where {uid} = ?uid");

		params.put("uid", uid);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
		query.addQueryParameters(params);
		final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public DJPRunMasterModel getRunMasterDetails(String id) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {DJPRunMaster} where {id} = ?id");

		params.put("id", id);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(DJPRunMasterModel.class));
		query.addQueryParameters(params);
		final SearchResult<DJPRunMasterModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public RouteMasterModel getRouteMasterDetails(String routeId) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {RouteMaster} where {routeId} = ?routeId");

		params.put("routeId", routeId);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(RouteMasterModel.class));
		query.addQueryParameters(params);
		final SearchResult<RouteMasterModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public EyDmsCustomerModel getMitraMasterDetails(String customerID) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {EyDmsCustomer} where {customerID} = ?customerID");

		params.put("customerID", customerID);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
		query.addQueryParameters(params);
		final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public List<OrderEntryModel> getOrderLineScheduleDetails() {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {oe.pk} from {OrderEntry as oe join Order as o on {oe.order}={o.pk}} where {oe.synced} = ?status and {o.erpOrderNumber} is not null and {o.versionId} is null");

		params.put("status", Boolean.FALSE);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(OrderEntryModel.class));
		query.addQueryParameters(params);
		final SearchResult<OrderEntryModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public EyDmsCustomerModel getCustomerByCustNo(String customerNo) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {EyDmsCustomer} where {customerNo} = ?customerNo");

		params.put("customerNo", customerNo);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
		query.addQueryParameters(params);
		final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;

	}

	@Override
	public CreditAndOutstandingModel getCreditByCustCode(String custCode) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {CreditAndOutstanding} where {customerCode} = ?custCode");

		params.put("custCode", custCode);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(CreditAndOutstandingModel.class));
		query.addQueryParameters(params);
		final SearchResult<CreditAndOutstandingModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public List<List<Object>> getSlctCrmCustomerDetails() {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {cust.pk}, {sub.pk}, {counter.route} from {EyDmsCustomer as cust join CustomerSubAreaMapping as sub on {cust.pk}={sub.eydmsCustomer} join CounterRouteMapping as counter on {cust.uid}={counter.counterCode}} where {cust.synced} = ?status and {sub.isActive} = ?active");

		params.put("status", Boolean.FALSE);
		params.put("active", Boolean.TRUE);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Arrays.asList(EyDmsCustomerModel.class, CustomerSubAreaMappingModel.class, String.class));
		final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<ProspectiveNetworkModel> deleteAllProspectiveNetworkDetails() {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {ProspectiveNetwork}");

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(ProspectiveNetworkModel.class));
		query.addQueryParameters(params);
		final SearchResult<ProspectiveNetworkModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<LedgerDetailsModel> deleteLedgerDetails(Date transactionDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {LedgerDetails} where {date} = ?transactionDate");
		params.put("transactionDate", transactionDate);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(LedgerDetailsModel.class));
		query.addQueryParameters(params);
		final SearchResult<LedgerDetailsModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<ISOMasterModel> getIsoMasterDetails() {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {ISOMaster}");

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(ISOMasterModel.class));
		query.addQueryParameters(params);
		final SearchResult<ISOMasterModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public OutstandingHistoryModel getOutstandingHistory(String custCode) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {OutstandingHistory} where {customerCode} = ?custCode");

		params.put("custCode", custCode);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(OutstandingHistoryModel.class));
		query.addQueryParameters(params);
		final SearchResult<OutstandingHistoryModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public OrderEntryModel getOrderEntryByDiNumber(String diNumber) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {OrderEntry} where {diNumber} = ?diNumber");

		params.put("diNumber", diNumber);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(OrderEntryModel.class));
		query.addQueryParameters(params);
		final SearchResult<OrderEntryModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public List<ISOMasterModel> getIsoMasterDetailsByDeliveryId(String deliveryId) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {ISOMaster} where {deliveryId} = ?deliveryId");

		params.put("deliveryId", deliveryId);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(ISOMasterModel.class));
		query.addQueryParameters(params);
		final SearchResult<ISOMasterModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult() : null;
		else
			return null;
	}

	@Override
	public AddressModel getAddressByErpAddressId(String erpAddressId) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {Address} where {erpAddressId} = ?erpAddressId");

		params.put("erpAddressId", erpAddressId);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(AddressModel.class));
		query.addQueryParameters(params);
		final SearchResult<AddressModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;

	}

	@Override
	public CustomerSubAreaMappingModel getCustomerSubAreaMapByCustomer(EyDmsCustomerModel eydmsCustomerModel) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {CustomerSubAreaMapping} where {eydmsCustomer} = ?eydmsCustomerModel");

		params.put("eydmsCustomerModel", eydmsCustomerModel);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(CustomerSubAreaMappingModel.class));
		query.addQueryParameters(params);
		final SearchResult<CustomerSubAreaMappingModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;

	}


	@Override
	public SubAreaMasterModel getSubAreaByTalukaAndDistrict(String taluka, String district) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {SubAreaMaster} where {taluka} = ?taluka and {district} = ?district");

		params.put("taluka", taluka);
		params.put("district", district);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(SubAreaMasterModel.class));
		query.addQueryParameters(params);
		final SearchResult<SubAreaMasterModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;

	}

	@Override
	public PrincipalGroupModel getPrincipalGroupByUid(String uid) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {PrincipalGroup} where {uid} = ?uid");

		params.put("uid", uid);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(PrincipalGroupModel.class));
		query.addQueryParameters(params);
		final SearchResult<PrincipalGroupModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;

	}

	@Override
	public CountryModel getCountryByIsoCode(String isoCode) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {Country} where {isocode} = ?isoCode");

		params.put("isoCode", isoCode);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(CountryModel.class));
		query.addQueryParameters(params);
		final SearchResult<CountryModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;

	}

	@Override
	public List<MonthlySalesModel> getMonthlySalesModel(String monthYear) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {MonthlySales} where {synced} = ?status and {monthName}=?monthYear");

		params.put("status", Boolean.FALSE);
		params.put("monthYear", monthYear);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(MonthlySalesModel.class));
		query.addQueryParameters(params);
		LOG.info("The bottom up integration query is" + query);
		final SearchResult<MonthlySalesModel> searchResult = flexibleSearchService.search(query);
		return  searchResult.getResult();
	}

	@Override
	public List<DealerPlannedMonthlySalesModel> getDealerPannedMonthlySalesDetails(List<SubAreaMasterModel> subAreaMasterList, EyDmsUserModel salesOfficer, String monthName, String monthYear) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {dp:pk} from {MonthlySales as ms join DealerPlannedMonthlySales as dp on {dp:monthlySales}={ms:pk}} where {ms:so} = ?salesOfficer and {dp:subAreaMaster} in (?subAreaMasterList) and {dp:monthName} = ?monthName and {dp:monthYear} = ?monthYear and {dp:synced} = ?status");
		params.put("salesOfficer", salesOfficer);
		params.put("subAreaMasterList", subAreaMasterList);
		params.put("monthName", monthName);
		params.put("monthYear", monthYear);
		params.put("status", Boolean.FALSE);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(DealerPlannedMonthlySalesModel.class));
		query.addQueryParameters(params);
		final SearchResult<DealerPlannedMonthlySalesModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public GeographicalMasterModel getGeographicalMasterModel(String taluka, String district, String city, String state) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {GeographicalMaster} where {taluka} = ?taluka and {district} = ?district and {erpCity} = ?city and {state} = ?state");

		params.put("taluka", taluka);
		params.put("district", district);
		params.put("city", city);
		params.put("state", state);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(GeographicalMasterModel.class));
		query.addQueryParameters(params);
		final SearchResult<GeographicalMasterModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public FreightAndIncoTermsMasterModel getFreightAndIncoTermsMasterModel(String brand, String district, String state, String orgType) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {FreightAndIncoTermsMaster} where {brand} = ?brand and {district} = ?district and {state} = ?state and {orgType} = ?orgType");

		params.put("brand", brand);
		params.put("district", district);
		params.put("orgType", orgType);
		params.put("state", state);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(FreightAndIncoTermsMasterModel.class));
		query.addQueryParameters(params);
		final SearchResult<FreightAndIncoTermsMasterModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public SalesOrderDeliverySLAModel getDeliverySlaModel(String route) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {SalesOrderDeliverySLA} where {route} = ?route");

		params.put("route", route);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(SalesOrderDeliverySLAModel.class));
		query.addQueryParameters(params);
		final SearchResult<SalesOrderDeliverySLAModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public SalesHistoryModel getSalesHistoryModel(String customerTransactionId, String customerTransactionLineId, CMSSiteModel brand) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {SalesHistory} where {customerTransactionId} = ?customerTransactionId and {customerTransactionLineId}=?customerTransactionLineId and {brand}=?brand");

		params.put("customerTransactionId", customerTransactionId);
		params.put("customerTransactionLineId", customerTransactionLineId);
		params.put("brand", brand);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(SalesHistoryModel.class));
		query.addQueryParameters(params);
		final SearchResult<SalesHistoryModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public WarehouseModel findWarehouseByCode(String code) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {Warehouse} where {code} = ?code");

		params.put("code", code);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(WarehouseModel.class));
		query.addQueryParameters(params);
		final SearchResult<WarehouseModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public VendorModel findVendorByCode(String code) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {Vendor} where {code} = ?code");

		params.put("code", code);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(VendorModel.class));
		query.addQueryParameters(params);
		final SearchResult<VendorModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public EtaTrackerModel getEtaByDiNumber(String deliveryId) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {EtaTracker} where {deliveryId} = ?deliveryId");

		params.put("deliveryId", deliveryId);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(EtaTrackerModel.class));
		query.addQueryParameters(params);
		final SearchResult<EtaTrackerModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public LedgerDetailsModel getLedgerDetails(String transactionLineGlRcId, Date transactionLineLastUpdateDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {LedgerDetails} where {transactionLineGlRcId} = ?transactionLineGlRcId and {transactionLineLastUpdateDate} = ?transactionLineLastUpdateDate");

		params.put("transactionLineGlRcId", transactionLineGlRcId);
		params.put("transactionLineLastUpdateDate", transactionLineLastUpdateDate);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(LedgerDetailsModel.class));
		query.addQueryParameters(params);
		final SearchResult<LedgerDetailsModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public List<OrderEntryModel> getOrderLinesForEpod() {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {OrderEntry} where ({epodCompletionSynced} = ?epodSynced and {epodInitiateDate} is not null) or ({epodFeedbackSynced} = ?feedbackSynced and {epodFeedback} is not null)");
		params.put("epodSynced", Boolean.FALSE);
		params.put("feedbackSynced", Boolean.FALSE);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(OrderEntryModel.class));
		query.addQueryParameters(params);
		final SearchResult<OrderEntryModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public OrderEntryModel findOrderEntryByErpLineItemId(String erpLineItemId) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {oe.pk} from {OrderEntry as oe join Order as o on {oe.order}={o.pk}} where {oe.erpLineItemId} = ?erpLineItemId");

		params.put("erpLineItemId", erpLineItemId);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(OrderEntryModel.class));
		query.addQueryParameters(params);
		final SearchResult<OrderEntryModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public List<DestinationSourceMasterModel> getAllDestinationSourceRecords() {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {DestinationSourceMaster}");

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(DestinationSourceMasterModel.class));
		final SearchResult<DestinationSourceMasterModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult();
		else
			return Collections.emptyList();
	}

	@Override
	public DeliveryModeModel getDeliveryMode(String deliveryMode) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {DeliveryMode} where {code} = ?deliveryMode");

		params.put("deliveryMode", deliveryMode);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(DeliveryModeModel.class));
		query.addQueryParameters(params);
		final SearchResult<DeliveryModeModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public DepotSubAreaMappingModel getDepotSubAreaMappingModel(String taluka, String district, String state, String depotCode, String brand) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {dsam:pk} from {DepotSubAreaMapping as dsam join CMSSite as br on {dsam:brand}={br:pk}" +
				" join Warehouse as w on {dsam:depot}={w:pk}} where {dsam:subArea} = ?taluka and {dsam:district} = ?district" +
				" and {dsam:state} = ?state and {w:code} = ?depotCode and {br:uid} = ?brand");

		params.put("taluka", taluka);
		params.put("district", district);
		params.put("state", state);
		params.put("depotCode", depotCode);
		params.put("brand", brand);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(DepotSubAreaMappingModel.class));
		query.addQueryParameters(params);
		final SearchResult<DepotSubAreaMappingModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public RouteMasterModel getRouteMaster(String taluka, String district, String state, String brand) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {rm:pk} from {RouteMaster as rm join SubAreaMaster as sam on {rm:subAreaMaster}={sam:pk}} where {rm:brand} = ?brand and {rm:state} = ?state and {sam:taluka} = ?taluka and {sam:district} = ?district and {isDefaultRoute} = ?isDefault");

		params.put("taluka", taluka);
		params.put("district", district);
		params.put("state", state);
		params.put("brand", brand);
		params.put("isDefault",Boolean.TRUE);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(RouteMasterModel.class));
		query.addQueryParameters(params);
		final SearchResult<RouteMasterModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public CounterRouteMappingModel getCounterRouteMapping(String customerUid) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {CounterRouteMapping} where {counterCode} = ?customerUid");
		params.put("customerUid", customerUid);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(CounterRouteMappingModel.class));
		query.addQueryParameters(params);
		final SearchResult<CounterRouteMappingModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public EyDmsUserModel getDoByUidAndEmpCode(String uid) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {EyDmsUser} where {uid} = ?uid");
		params.put("uid", uid);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(EyDmsUserModel.class));
		query.addQueryParameters(params);
		final SearchResult<EyDmsUserModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public UserSubAreaMappingModel getDoSubAreaMappingUid(String soUid, String brand, SubAreaMasterModel subAreaMaster, String state) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {usam:pk} from {UserSubAreaMapping as usam join EyDmsUser as u on {usam:eydmsUser}={u:pk}} where {u:uid} = ?uid and {usam:brand} = ?brand and {usam:subAreaMaster} = ?subAreaMaster and {usam:state} = ?state");
		params.put("uid", soUid);
		params.put("brand", (CMSSiteModel) baseSiteService.getBaseSiteForUID(brand));
		params.put("subAreaMaster", subAreaMaster);
		params.put("state",state);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(UserSubAreaMappingModel.class));
		query.addQueryParameters(params);
		final SearchResult<UserSubAreaMappingModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public CurrencyModel getCurrencyModelByISOCode(String isoCode) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {Currency} where {isoCode} = ?isoCode");

		params.put("isoCode", isoCode);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(CurrencyModel.class));
		query.addQueryParameters(params);
		final SearchResult<CurrencyModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;

	}

	@Override
	public Date getInvoiceDateFromNcr(String erpLineItemId) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {s:invoiceDate} from {SalesHistory as s JOIN OrderEntry as oe on {s:lineId}={oe:erpLineItemId}} where {s:lineId} =?erpLineItemId and {s:quantity} > 0");
		params.put("erpLineItemId", erpLineItemId);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Date.class));
		query.addQueryParameters(params);
		final SearchResult<Date> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult().get(0);
	}

	@Override
	public List<List<Object>> getCountofLeadsGenerated(Integer year, Integer month) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select count(*),{a:district},{a:state} from {LeadMaster as l join Address as a on {a.pk}={l.address}} where ").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("l:creationTime", month, year, params));
		builder.append(" group by {a:district},{a:state}");
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Arrays.asList(Integer.class, String.class, String.class));
		final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<List<Object>> getProductSaleDetails(EyDmsUserModel so, String monthName, String monthYear, List<SubAreaMasterModel> subAreaMasterModelList, DistrictMasterModel districtMaster, CMSSiteModel brand) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {productCode},sum({bucket1}), sum({bucket2}), sum({bucket3}), {productGrade}, {productPackaging}, {premium}  from {ProductSale} " +
				"where {salesOfficer}=?so and {brand} = ?brand and {monthName} = ?monthName and {monthYear} = ?monthYear and {subAreaMaster} in (?subAreaMasterModelList) " +
				"and {districtMaster} = ?districtMaster and {isMonthlySalesForPlannedDealer} = ?isMonthlySalesForPlannedDealer and {isMonthlySalesForRevisedDealer} = ?isMonthlySalesForRevisedDealer  group by {productCode}, {productGrade}, {productPackaging}, {premium}");
		params.put("so",so);
		params.put("brand",brand);
		params.put("monthName",monthName);
		params.put("monthYear",monthYear);
		params.put("subAreaMasterModelList",subAreaMasterModelList);
		params.put("districtMaster", districtMaster);
		params.put("isMonthlySalesForPlannedDealer",Boolean.TRUE);
		params.put("isMonthlySalesForRevisedDealer",Boolean.FALSE);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Arrays.asList(String.class, Double.class, Double.class, Double.class, String.class, String.class, Boolean.class));
		final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public ProductModel getProductModelByGradeAndBagType(String grade, String bagType, CatalogVersionModel catalogVersion) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {Product} where {grade} = ?grade and {bagType} = ?bagType and {catalogVersion} = ?catalogVersion");

		params.put("grade", grade);
		params.put("bagType", bagType);
		params.put("catalogVersion", catalogVersion);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(ProductModel.class));
		query.addQueryParameters(params);
		final SearchResult<ProductModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public List<MonthlySalesModel> getTopDownSpMonthlySales(String monthName, String monthYear, String district, String state, String taluka, BaseSiteModel brand) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {ms:pk} from {MonthlySales as ms join DistrictMaster as dm on {ms:districtMaster}={dm:pk} join StateMaster as sm on {ms:stateMaster}={sm:pk} join SubAreaMaster as sam on {ms:subAreaMaster}={sam:pk}} where {ms:monthName} = ?monthName and {ms:monthYear} = ?monthYear and {sam:taluka} = ?taluka and {dm:name} = ?district and {sm:name} = ?state and {ms:brand} = ?brand");

		params.put("monthName", monthName);
		params.put("monthYear", monthYear);
		params.put("district", district);
		params.put("state", state);
		params.put("taluka", taluka);
		params.put("brand", brand);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(MonthlySalesModel.class));
		query.addQueryParameters(params);
		final SearchResult<MonthlySalesModel> searchResult = flexibleSearchService.search(query);
		return  searchResult.getResult();
	}

	@Override
	public List<AnnualSalesModel> getAnnualSalesModel(String financialYear) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {AnnualSales} where {synced} = ?status and {financialYear} = ?financialYear");

		params.put("status", Boolean.FALSE);
		params.put("financialYear", financialYear);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(AnnualSalesModel.class));
		query.addQueryParameters(params);
		final SearchResult<AnnualSalesModel> searchResult = flexibleSearchService.search(query);
		return  searchResult.getResult();
	}

	@Override
	public List<List<Object>> getProductDetailsFromAnnualSales(DistrictMasterModel districtMasterModel, BaseSiteModel brand) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select distinct {m:productCode},{m:productGrade},{m:productPackaging},{m:productBagType} from {MonthWiseAnnualTarget as m} where {m:districtMaster}=?districtMaster and " +
				"{m:isAnnualSalesRevisedForDealer}=?isAnnualSalesRevisedForDealer and {m:productCode} is not null and {m:customerCode} is not null and {m:retailerCode} is null and " +
				"{m:selfCounterCustomerCode} is null and {m:brand}=?brand");

		params.put("districtMaster", districtMasterModel);
		params.put("isAnnualSalesRevisedForDealer", Boolean.TRUE);
		params.put("brand", brand);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Arrays.asList(String.class, String.class, String.class, String.class));
		final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<List<Object>> getMonthWiseTargets(DistrictMasterModel districtMasterModel, BaseSiteModel brand, String productCode) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {m:monthYear},sum({m:monthTarget}) from {MonthWiseAnnualTarget as m} where {m:districtMaster}=?districtMaster and" +
				" {m:isAnnualSalesRevisedForDealer}=?isAnnualSalesRevisedForDealer  and {m:productCode}=?productCode and {m:customerCode} is not null and {m:retailerCode} is null and {m:selfCounterCustomerCode} is null and" +
				" {m:brand}=?brand group by {m:monthYear} ORDER BY CONVERT(datetime, '01-' + {m:monthYear})");

		params.put("districtMaster", districtMasterModel);
		params.put("isAnnualSalesRevisedForDealer", Boolean.TRUE);
		params.put("brand", brand);
		params.put("productCode", productCode);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Arrays.asList(String.class, Double.class));
		final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public BrandModel getBrandByIsoCode(String isoCode) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {Brand} where {isoCode} = ?isoCode");

		params.put("isoCode", isoCode);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(BrandModel.class));
		query.addQueryParameters(params);
		final SearchResult<BrandModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CompetitorProductModel getCompetitorProductByCodeAndState(String code, String state, String brand) {
		return (CompetitorProductModel) sessionService.executeInLocalView(new SessionExecutionBody() {
			@Override
			public CompetitorProductModel execute() {
				try {
					searchRestrictionService.disableSearchRestrictions();
					final Map<String, Object> params = new HashMap<String, Object>();
					final StringBuilder builder = new StringBuilder("select {cp:pk} from {CompetitorProduct as cp join Brand as b on {cp:brand}={b:pk}} where {cp:code} = ?code and {cp:state} = ?state and {b:isoCode}=?brand");

					params.put("code", code);
					params.put("state", state);
					params.put("brand",brand);

					final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
					query.setResultClassList(Collections.singletonList(CompetitorProductModel.class));
					query.addQueryParameters(params);
					final SearchResult<CompetitorProductModel> searchResult = flexibleSearchService.search(query);
					if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
						return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
					else
						return null;
				}
				finally {
					searchRestrictionService.enableSearchRestrictions();
				}
			}
		});
	}

	@Override
	public List<OrderModel> getModifiedOrdersOfYesterday(String date) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {Order} where {modifiedtime} like ?date and {versionId} is null");

		params.put("date", date);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(OrderModel.class));
		query.addQueryParameters(params);
		final SearchResult<OrderModel> searchResult = flexibleSearchService.search(query);
		return  searchResult.getResult();
	}

	@Override
	public List<ProductSaleModel> getProductSaleModels() {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {ProductSale} where {synced} = ?status and {isMonthlySalesForReviewedDealer} = ?isMonthlySalesForReviewedDealer");

		params.put("status", Boolean.FALSE);
		params.put("isMonthlySalesForReviewedDealer", Boolean.TRUE);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(ProductSaleModel.class));
		query.addQueryParameters(params);
		final SearchResult<ProductSaleModel> searchResult = flexibleSearchService.search(query);
		return  searchResult.getResult();
	}

	@Override
	public List<OrderEntryModel> getModifiedOrderEntries(String date) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {oe:pk} from {Order as o join OrderEntry as oe on {oe:order}={o:pk}} where {oe:modifiedtime} like ?date and {o:versionId} is null");

		params.put("date", date);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(OrderEntryModel.class));
		query.addQueryParameters(params);
		final SearchResult<OrderEntryModel> searchResult = flexibleSearchService.search(query);
		return  searchResult.getResult();
	}

	@Override
	public String getStateCode(String stateName) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {stateCode} from {StateCodes} where {stateName} = ?stateName");

		params.put("stateName", stateName);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(String.class));
		query.addQueryParameters(params);
		final SearchResult<String> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public IntegrationSettingModel getIntegrationSettingModelByName(String integrationName) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {IntegrationSetting} where {integrationName} = ?integrationName");

		params.put("integrationName", integrationName);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(IntegrationSettingModel.class));
		query.addQueryParameters(params);
		final SearchResult<IntegrationSettingModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public String appendIntegrationSetting(String integrationName, Map<String, Object> params) {
		IntegrationSettingModel integrationSettingModel = this.getIntegrationSettingModelByName(integrationName);
		String columnName = integrationSettingModel.getColumnName()!=null ? integrationSettingModel.getColumnName() : "";
		String columnName1 = attachSymbol("{",columnName,"}");
		String syncedColumnName = integrationSettingModel.getSyncedColumnName()!=null ? attachSymbol("{",integrationSettingModel.getSyncedColumnName(),"}" ): "";
		String strategy = integrationSettingModel.getRetrievalStrategy();
		DateFormat dateFormat = null;
		if(integrationSettingModel.getDateFormat()!=null && !integrationSettingModel.getDateFormat().isEmpty()) {
			dateFormat = new SimpleDateFormat(integrationSettingModel.getDateFormat());
		}

		final StringBuilder builder = new StringBuilder();

		if(strategy!=null) {
			builder.append(" and ");
			if(strategy.equals("synced")) {
				builder.append(syncedColumnName + "=0");
			}
			else if(strategy.equals("lastXDays")) {
				builder.append(EyDmsDateUtility.getLastXDayQuery(columnName,params,integrationSettingModel.getLastXDays()));
			}
			else if (strategy.equals("syncedAndLastXDays")) {
				builder.append(syncedColumnName + "=0");
				builder.append(" and ").append(EyDmsDateUtility.getLastXDayQuery(columnName,params,integrationSettingModel.getLastXDays()));
			}
		}

		if(integrationSettingModel.getStartDate()!=null) {
			String startDate = attachSymbol("'",dateFormat.format(integrationSettingModel.getStartDate()),"'");
			builder.append(" and ").append(columnName1 +">=" + startDate);
		}
		if(integrationSettingModel.getExclusiveEndDate()!=null) {
			String endDate = attachSymbol("'",dateFormat.format(integrationSettingModel.getExclusiveEndDate()),"'");
			builder.append(" and ").append(columnName1 +"<" + endDate);
		}

		if(integrationSettingModel.getKeyValue()!=null && integrationSettingModel.getUniqueKey()!=null && !integrationSettingModel.getKeyValue().isEmpty()) {
			String uniqueKey = attachSymbol("{",integrationSettingModel.getUniqueKey(),"}");
			String keyValues = attachSymbol("(", integrationSettingModel.getKeyValue(), ")");
			builder.append(" and ").append(uniqueKey + " in " + keyValues);
		}

		if(integrationSettingModel.getAppendQuery()!=null && !integrationSettingModel.getAppendQuery().isEmpty()) {
			builder.append(" and ").append(integrationSettingModel.getAppendQuery());
		}

		return builder.toString();
	}

	@Override
	public List<PointRequisitionModel> getPointRequisitionDetails() {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {PointRequisition} where {status} = ?status and {reqApprovedDate} is not null").append(this.appendIntegrationSetting("POINT_REQUISITION_SALES_DATA",params));

		params.put("status", PointRequisitionStatus.APPROVED);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(PointRequisitionModel.class));
		query.addQueryParameters(params);
		LOG.info("Point Requisition Integration Query used:"+query);
		final SearchResult<PointRequisitionModel> searchResult = flexibleSearchService.search(query);
		return  searchResult.getResult();
	}

    @Override
    public List<EyDmsSiteMasterModel> getSiteMasterDetailsForSLCT() {
		final Map<String, Object> params = new HashMap<String, Object>();
		String builder = "select {pk} from {EyDmsSiteMaster} Where ";
		String appendQuery=this.appendIntegrationSetting("SITE_MASTER_INTEGRATION", params);
		if(appendQuery!=null)
		{
			appendQuery = appendQuery.substring(4);
			builder += appendQuery;
		}

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder);
		query.setResultClassList(Collections.singletonList(EyDmsSiteMasterModel.class));
		query.addQueryParameters(params);
		LOG.info("EyDmsSiteMaster Integration Query used:"+query);
		final SearchResult<EyDmsSiteMasterModel> searchResult = flexibleSearchService.search(query);
		return  searchResult.getResult();
    }

	@Override
	public List<EndCustomerComplaintModel> getEndCustomerComplaintsForSLCT(String startDate, String endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {EndCustomerComplaint} WHERE  {tsoStatus}!=?tsoStatus and {isSiteVisitRequired} IS NOT NULL ").append(this.appendIntegrationSetting("END_CUSTOMER_FORM_DATA",params));
		params.put("tsoStatus", CustomerComplaintTSOStatus.CLOSED);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(EndCustomerComplaintModel.class));
		query.addQueryParameters(params);
		LOG.info("Complaint Form Integration Query used:"+query);
		final SearchResult<EndCustomerComplaintModel> searchResult = flexibleSearchService.search(query);
		List<EndCustomerComplaintModel> result = searchResult.getResult();
		return result!=null && !result.isEmpty() ? result : Collections.emptyList();
	}

	@Override
	public List<InfluencerVisitMasterModel> getInfluencerVisitDetails() {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {InfluencerVisitMaster} WHERE {endVisitTime} IS NOT NULL ").append(this.appendIntegrationSetting("INFLUENCER_FORM_DATA",params));
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(InfluencerVisitMasterModel.class));
		query.addQueryParameters(params);
		LOG.info("Influencer Form Integration Query used:"+query);
		final SearchResult<InfluencerVisitMasterModel> searchResult = flexibleSearchService.search(query);
		List<InfluencerVisitMasterModel> result = searchResult.getResult();
		return result!=null && !result.isEmpty() ? result : Collections.emptyList();
	}

	private static String attachSymbol(String startSymbol, String variableName, String endSymbol) {
		return startSymbol + variableName + endSymbol;
	}

	@Override
	public List<SiteVisitMasterModel> getSiteVisitForms() {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {SiteVisitMaster} where {endVisitTime} is not null").append(this.appendIntegrationSetting("SITE_VISIT_FORM_DATA",params));

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(SiteVisitMasterModel.class));
		query.addQueryParameters(params);
		LOG.info("Site Visit Form Integration Query used:"+query);
		final SearchResult<SiteVisitMasterModel> searchResult = flexibleSearchService.search(query);
		return  searchResult.getResult();
	}

	@Override
	public ProductPointMasterModel getProductPointMasterModel(String schemeId, ProductModel product) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {ProductPointMaster} where {schemeId} = ?schemeId and {product} = ?product");

		params.put("schemeId", schemeId);
		params.put("product", product);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(ProductPointMasterModel.class));
		query.addQueryParameters(params);
		final SearchResult<ProductPointMasterModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public PriceRowModel getPriceRowByProduct(String productCode) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {PriceRow} where {productId} = ?productCode");

		params.put("productCode", productCode);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(PriceRowModel.class));
		query.addQueryParameters(params);
		final SearchResult<PriceRowModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

    @Override
    public GiftSchemeModel getSchemesDefinitionById(String schemeId, String state, String influencerType) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {GiftScheme} where {schemeId} = ?schemeId and {state} = ?state and {influencerType} = ?influencerType");

		params.put("schemeId", schemeId);
		params.put("state",state);
		params.put("influencerType", influencerType);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(GiftSchemeModel.class));
		query.addQueryParameters(params);
		final SearchResult<GiftSchemeModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;

    }

	@Override
	public List<List<Object>> getGiftRedemptionForSLCT() {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {p:code},{p:name},{oe:quantity},{o.orderSentForApprovalDate},{o:user},{o:code},{o:approvedBy} FROM {Order AS o JOIN OrderEntry AS oe ON {o.pk}={oe.order} JOIN Product AS p ON {oe.product}={p.pk}} where  {o:crmOrderType}=?gift and {o:status}=?status and {o:orderSentForApprovalDate} IS NOT NULL  and {o:approvedBy} IS NOT NULL ").append(this.appendIntegrationSetting("GIFT_REDEMPTION_DATA",params));
		params.put("gift",CRMOrderType.GIFT);
		params.put("status", OrderStatus.APPROVED);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Arrays.asList(String.class, String.class,String.class, String.class,String.class, String.class,String.class));
		final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	public List<OrderEntryModel> getGiftRedemptionsForSLCT() {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {oe:pk} FROM {OrderEntry AS oe JOIN Order AS o ON {o.pk}={oe.order} JOIN Product AS p ON {oe.product}={p.pk}}" +
				" where  {o:crmOrderType}=?gift and {o:status}=?status and {o:orderSentForApprovalDate} IS NOT NULL  and {o:approvedBy} IS NOT NULL ").append(this.appendIntegrationSetting("GIFT_REDEMPTION_DATA",params));
		params.put("gift",CRMOrderType.GIFT);
		params.put("status", OrderStatus.APPROVED);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(OrderEntryModel.class));
		query.addQueryParameters(params);
		LOG.info("gift Redemption Integration Query used:"+query);
		final SearchResult<OrderEntryModel> searchResult = flexibleSearchService.search(query);
		return  searchResult.getResult();
	}

	@Override
	public GiftModel getGiftModelByCode(String code, CatalogVersionModel catalogVersion) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {Gift} where {code} = ?code and {catalogVersion} = ?catalogVersion");

		params.put("code", code);
		params.put("catalogVersion", catalogVersion);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(GiftModel.class));
		query.addQueryParameters(params);
		final SearchResult<GiftModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public StockLevelModel getStockLevelByProductAndWarehouse(String productCode, WarehouseModel warehouseModel) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {StockLevel} where {productCode} = ?productCode and {warehouse} = ?warehouseModel");

		params.put("productCode", productCode);
		params.put("warehouseModel",warehouseModel);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(StockLevelModel.class));
		query.addQueryParameters(params);
		final SearchResult<StockLevelModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public TsoTalukaMappingModel getTsoTalukaMappingByEmp(String soUid, String brand, SubAreaMasterModel subAreaMaster) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {ttm:pk} from {TsoTalukaMapping as ttm join EyDmsUser as u on {ttm:tsoUser}={u:pk}} where {u:uid} = ?uid and {ttm:brand} = ?brand and {ttm:subAreaMaster} = ?subAreaMaster");
		params.put("uid", soUid);
		params.put("brand", (CMSSiteModel) baseSiteService.getBaseSiteForUID(brand));
		params.put("subAreaMaster", subAreaMaster);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(TsoTalukaMappingModel.class));
		query.addQueryParameters(params);
		final SearchResult<TsoTalukaMappingModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}
}