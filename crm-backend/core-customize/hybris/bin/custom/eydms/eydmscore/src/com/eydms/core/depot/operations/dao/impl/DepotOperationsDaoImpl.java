package com.eydms.core.depot.operations.dao.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.eydms.core.model.DepotSubAreaMappingModel;
import com.eydms.core.model.ISOMasterModel;
import com.eydms.core.model.SubAreaMasterModel;
import com.eydms.core.model.VisitMasterModel;
import com.eydms.core.services.impl.TerritoryManagementServiceImpl;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.customer.services.impl.DefaultEyDmsCustomerAccountService;
import com.eydms.core.depot.operations.dao.DepotOperationsDao;
import com.eydms.core.enums.CustomerCategory;
import com.eydms.core.enums.VisitStatus;
import com.eydms.core.enums.WarehouseType;
import com.eydms.facades.data.FilterTalukaData;
import com.eydms.facades.data.ISOVisibilityData;
import com.eydms.facades.depot.operations.data.DailyCapacityUtilizationData;
import com.eydms.facades.depot.operations.data.DepotStockData;

import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;

public class DepotOperationsDaoImpl implements DepotOperationsDao
{
	private FlexibleSearchService flexibleSearchService;

	private static final String DEFAULT_QUERY ="SELECT {w.code}, {w.name}, SUM({s.openingStock}), SUM({s.stockIn}), SUM({s.stockOut}) FROM {Warehouse AS w JOIN DepotStockMaster AS s ON {w.pk}={s.depot} ";
	//private static final String PRODUCT_JOIN_QUERY =" JOIN Product AS p ON  {s.custCategory} = {p.custCategory} AND {s.inventoryId} = {p.inventoryId}  JOIN CatalogVersion AS c ON {c.pk}={p.catalogVersion} AND {c.version}='Online' ";
	private static final String DEFAULT_WHERE__QUERY =" } WHERE {w.type}=?depot and {w.pk} in (?warehouses) ";
	private static final String PRODUCT_QUERY =" AND {p.grade} in (?productList) ";
	private static final String END_QUERY =" GROUP BY {w.code},{w.name}";


	//private static final String STOCK_DISPATCHED_QUERY = "SELECT SUM({oe.quantityInMT}) FROM { OrderEntry AS oe JOIN Order AS o on {o.pk}={oe.order} JOIN Warehouse AS w ON {w.pk}={oe.source} JOIN DepotSubAreaMapping AS sa ON {sa.depot}={o.warehouse} JOIN Product AS p ON {oe.product} = {p.pk} } WHERE {oe.truckDispatchedDate} BETWEEN ?startDate AND ?endDate AND  {sa.subAreaMaster} IN (?subAreaList) and {sa.brand}=?brand and {sa.active}=?active  ";
	private static final String ORDER_BOOKED_QUERY = "SELECT SUM({oe.quantityInMT}) FROM {OrderEntry AS oe JOIN Warehouse AS w ON {w.pk}={oe.source} JOIN Product AS p ON {oe.product} = {p.pk} } WHERE  {w.type}=?depot AND {oe.erpLineItemId} is not null and {oe.cancelledDate} is null AND {oe.invoiceCreationDateAndTime} IS NULL and {oe.source} in (?warehouses)   ";
	private static final String DEPOT_QUERY=" AND {w.code} in (?depotList) ";

	private static final String OPENING_AND_INTRANSIT_STOCK_QUERY = "SELECT SUM({s.openingStock}), SUM({s.stockIn}), SUM({s.stockOut}) FROM {DepotStockMaster AS s JOIN Warehouse AS w ON {w.pk}={s.depot} ";

	private static final Logger LOGGER = Logger.getLogger(DefaultEyDmsCustomerAccountService.class);

	private static final String SEARCH_QUERY ="SELECT SUM({s.openingStock}), sum({s.stockIn}), sum({s.stockOut}) FROM {DepotStockMaster AS s JOIN Warehouse AS w ON {w.pk}={s.depot}} WHERE {w.type}=?type and  {w.pk} in (?warehouses) ";

	private static final String FIND_DEPOT_ADDRESS = "SELECT {address:pk} FROM {Address AS address} WHERE {address:owner}=?depot " ;

	private static final String FIND_DEPOT_ADDRESS_BY_PK = "SELECT {address:pk} FROM {Address AS address} WHERE {address:pk}=?pk " ;
	//New Territory Change
	private static final String FIND_DEPOT_GRADES = "SELECT distinct {s.grade} FROM {DepotStockMaster AS s JOIN  DepotSubAreaMapping AS sa ON {s.depot}={sa.depot}} WHERE {sa.subAreaMaster} IN (?subAreaList) and {sa.brand}=?brand and {sa.active}=?active ";

	private WarehouseType depot = WarehouseType.DEPOT;

	private static final String FIND_DEPOTSUBAREA_MAPPING_QUERY = "SELECT {"+DepotSubAreaMappingModel.PK+"} FROM {"+DepotSubAreaMappingModel._TYPECODE+"} WHERE {"+DepotSubAreaMappingModel.BRAND+"} = ?brand AND {subAreaMaster} IN (?subAreaList) AND {"+DepotSubAreaMappingModel.ACTIVE+"} =?active" ;

	@Autowired
	UserService userService;

	@Autowired
	BaseSiteService baseSiteService;

	@Autowired
	TerritoryManagementServiceImpl territoryService;

	@Autowired
	private PaginatedFlexibleSearchService paginatedFlexibleSearchService;

	@Autowired
	CatalogVersionService catalogVersionService;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private SearchRestrictionService searchRestrictionService;


	@Override
	public List<DepotStockData> getStockAvailability(List<String> productName, List<String> depotCode) {
		List<DepotStockData> stockData = new ArrayList<>();
		final Map<String, Object> params = new HashMap<String, Object>();
		FilterTalukaData filterTalukaData = new FilterTalukaData();
		List<DepotSubAreaMappingModel> depotSubAreaMappingList = findDepotSubAreaMappingByBrandAndSubArea(territoryService.getTaulkaForUser(filterTalukaData));
		if(depotSubAreaMappingList!=null && !depotSubAreaMappingList.isEmpty()) {
			List<WarehouseModel> warehouseModels = depotSubAreaMappingList.stream().map(DepotSubAreaMappingModel :: getDepot ).distinct().collect(Collectors.toList());
			if(warehouseModels!=null && !warehouseModels.isEmpty()) {
				final StringBuilder builder = new StringBuilder(DEFAULT_QUERY);
				builder.append(DEFAULT_WHERE__QUERY);
				if(Objects.nonNull(productName))
				{
					builder.append(" AND {s.grade} in (?productList) ");
					params.put("productList", productName);
				}
				if(Objects.nonNull(depotCode))
				{
					builder.append(" AND {w.code} in (?depotList) ");
					params.put("depotList", depotCode);
				}
				builder.append(END_QUERY);

				final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
				params.put("depot", depot);
				params.put("warehouses", warehouseModels);

				query.addQueryParameters(params);

				query.setResultClassList(Arrays.asList(String.class,String.class,Double.class,Double.class,Double.class));

				final SearchResult<List<Object>> searchResult = getFlexibleSearchService().search(query);
				List<List<Object>> openingStockList = new ArrayList<List<Object>>();
				if(searchResult.getResult()!=null & !searchResult.getResult().isEmpty()) {
					openingStockList = searchResult.getResult();
				}

				final StringBuilder builder1 = new StringBuilder("SELECT {depotCode}, sum({qtyShipped}) FROM {ISOMaster} WHERE {depotCode} IN (?depotCodes) AND {packagingType} IN ('Packed' , 'PACKED') AND {actualDepartureDate} IS NOT NULL AND {receiptDate} IS NULL ");
				final Map<String, Object> params1 = new HashMap<String, Object>();
				if(depotCode!=null && !depotCode.isEmpty())
				{
					params1.put("depotCodes", depotCode);
				}
				else {
					params1.put("depotCodes", warehouseModels.stream().map(data1->data1.getCode()).collect(Collectors.toList()));
				}
				if(Objects.nonNull(productName))
				{
					builder1.append(" and {grade} in (?productList) ");
					params1.put("productList", productName);
				}
				builder1.append(" group by {depotCode}  ");
				final FlexibleSearchQuery query1 = new FlexibleSearchQuery(builder1.toString());
				query1.addQueryParameters(params1);
				query1.setResultClassList(Arrays.asList(String.class, Double.class));
				final SearchResult<List<Object>> searchResult1 = getFlexibleSearchService().search(query1);
				List<List<Object>> inTransitList = new ArrayList<List<Object>>();
				if(searchResult1.getResult()!=null && !searchResult1.getResult().isEmpty() ) {
					inTransitList = searchResult1.getResult();
				}

				List<DepotStockData> openingStockData = new ArrayList<>();

				for(List<Object> data: openingStockList)
				{
					DepotStockData d = new DepotStockData();
					d.setDepotCode((String) data.get(0));
					d.setDepotName((String) data.get(1));

					Double stock = 0.0;
					if(data.get(2)!=null)
						stock+=(Double)data.get(2);
					if(data.get(3)!=null)
						stock+=(Double)data.get(3);
					if(data.size()>4 && data.get(4)!=null)
						stock-=(Double)data.get(4);
					d.setOpeningStock(stock);
					openingStockData.add(d);
				}
				List<DepotStockData> inTransitStockData = new ArrayList<>();

				for(List<Object> data: inTransitList)
				{
					DepotStockData d = new DepotStockData();
					d.setDepotCode((String) data.get(0));
					Double inTransitStock = 0.0;
					if(data.get(1)!=null)
						inTransitStock+=(Double)data.get(1);
					d.setInTransitStock(inTransitStock);
					inTransitStockData.add(d);
				}

				for(WarehouseModel warehouse: warehouseModels) {
					DepotStockData d = new DepotStockData();
					d.setDepotCode(warehouse.getCode());
					d.setDepotName(warehouse.getName());
					d.setOpeningStock(0.0);
					d.setInTransitStock(0.0);
					Optional<DepotStockData> openingStockOpt = openingStockData.stream().filter(data -> data.getDepotCode()!=null && data.getDepotCode().equalsIgnoreCase(warehouse.getCode())).findAny();
					if(openingStockOpt.isPresent()) {
						DepotStockData openingData = openingStockOpt.get();
						if(openingData.getOpeningStock()!=null)
							d.setOpeningStock(openingData.getOpeningStock());
					}
					Optional<DepotStockData> inTransitStockOpt = inTransitStockData.stream().filter(data -> data.getDepotCode()!=null && data.getDepotCode().equalsIgnoreCase(warehouse.getCode())).findAny();
					if(inTransitStockOpt.isPresent()) {
						DepotStockData inTransitData = inTransitStockOpt.get();
						if(inTransitData.getInTransitStock()!=null)
							d.setInTransitStock(inTransitData.getInTransitStock());
					}
					stockData.add(d);
				}
			}
		}
		return stockData;
	}

	@Override
	public DailyCapacityUtilizationData getDailyCapacityUtilization(List<String> productName, List<String> depotCode,  UserModel user, Date startDate, Date endDate)
	{
		DailyCapacityUtilizationData data = new DailyCapacityUtilizationData();
		FilterTalukaData filterTalukaData = new FilterTalukaData();
		List<DepotSubAreaMappingModel> depotSubAreaMappingList = findDepotSubAreaMappingByBrandAndSubArea(territoryService.getTaulkaForUser(filterTalukaData));
		if(depotSubAreaMappingList!=null && !depotSubAreaMappingList.isEmpty()) {
			List<WarehouseModel> warehouseModels = depotSubAreaMappingList.stream().map(DepotSubAreaMappingModel :: getDepot ).distinct().collect(Collectors.toList());
			if(warehouseModels!=null && !warehouseModels.isEmpty()) {

				final Map<String, Object> params2 = new HashMap<String, Object>();
				final StringBuilder builder2 =new StringBuilder(ORDER_BOOKED_QUERY);
				params2.put("warehouses", warehouseModels);
				params2.put("depot", depot);
				//params.put("startDate",startDate);
				//params.put("endDate",endDate);

				final StringBuilder builder1 = new StringBuilder("SELECT sum({qtyShipped}) FROM {ISOMaster} WHERE {depotCode} IN (?depotCodes) AND {deliveryMode}='ROAD' AND {packagingType} IN ('Packed' , 'PACKED') AND {actualDepartureDate} IS NOT NULL AND {receiptDate} IS NULL ");
				final Map<String, Object> params1 = new HashMap<String, Object>();

				final StringBuilder builder4 = new StringBuilder("SELECT sum({qtyShipped}) FROM {ISOMaster} WHERE {depotCode} IN (?depotCodes) AND {deliveryMode}='RAIL' AND {packagingType} IN ('Packed' , 'PACKED') AND {actualDepartureDate} IS NOT NULL AND {receiptDate} IS NULL ");

				final StringBuilder builder3 =new StringBuilder(OPENING_AND_INTRANSIT_STOCK_QUERY);
				final Map<String, Object> params3 = new HashMap<String, Object>();

				params3.put("depot", depot);
				params3.put("warehouses", warehouseModels);

				builder3.append(DEFAULT_WHERE__QUERY);

				if(depotCode!=null && !depotCode.isEmpty())
				{
					builder2.append(DEPOT_QUERY);
					params2.put("depotList", depotCode);

					builder3.append(DEPOT_QUERY);
					params3.put("depotList", depotCode);

					params1.put("depotCodes", depotCode);
				}
				else {
					params1.put("depotCodes", warehouseModels.stream().map(data1->data1.getCode()).collect(Collectors.toList()));
				}

				if(Objects.nonNull(productName))
				{
					builder2.append(PRODUCT_QUERY);
					params2.put("productList", productName);

					builder3.append(" AND {s.grade} in (?productList) ");
					params3.put("productList", productName);

					builder1.append(" and {grade} in (?productList) ");
					params1.put("productList", productName);
				}

				double dispatchedStock = 0;
				double orderBooked = 0;
				double openingStock = 0;
				double inTransitStock = 0;
				double receivedStock = 0;
				double roadTransitStock = 0;
				double railTransitStock = 0;

				final FlexibleSearchQuery query1 = new FlexibleSearchQuery(builder1.toString());
				query1.addQueryParameters(params1);
				query1.setResultClassList(Arrays.asList(Double.class));
				final SearchResult<Double> searchResult1 = getFlexibleSearchService().search(query1);
				if(searchResult1.getResult()!=null && !searchResult1.getResult().isEmpty() && searchResult1.getResult().get(0)!=null) {
					roadTransitStock = searchResult1.getResult().get(0);
				}

				final FlexibleSearchQuery query4 = new FlexibleSearchQuery(builder4.toString());
				query4.addQueryParameters(params1);
				query4.setResultClassList(Arrays.asList(Double.class));
				final SearchResult<Double> searchResult4 = getFlexibleSearchService().search(query4);
				if(searchResult4.getResult()!=null && !searchResult4.getResult().isEmpty() && searchResult4.getResult().get(0)!=null) {
					railTransitStock = searchResult4.getResult().get(0);
				}

				inTransitStock = roadTransitStock+railTransitStock;
				data.setInTransitStock(inTransitStock);
				data.setRoadInTransit(roadTransitStock);
				data.setRailInTransit(railTransitStock);

				final FlexibleSearchQuery query3 = new FlexibleSearchQuery(builder3.toString());
				query3.addQueryParameters(params3);
				query3.setResultClassList(Arrays.asList(Double.class,Double.class,Double.class));
				final SearchResult<List<Double>> searchResult3 = getFlexibleSearchService().search(query3);
				if(searchResult3.getResult()!=null && !searchResult3.getResult().isEmpty()) {
					if(searchResult3.getResult().get(0).size()>0 && searchResult3.getResult().get(0).get(0)!=null) {
						openingStock=searchResult3.getResult().get(0).get(0);
					}
					if(searchResult3.getResult().get(0).size()>1 && searchResult3.getResult().get(0).get(1)!=null) {
						receivedStock=searchResult3.getResult().get(0).get(1);
					}
					if(searchResult3.getResult().get(0).size()>2 && searchResult3.getResult().get(0).get(2)!=null) {
						dispatchedStock=searchResult3.getResult().get(0).get(2);
					}
				}
				data.setReceivedStock(receivedStock);
				data.setDispatchedStock(dispatchedStock);
				data.setOpeningStock(openingStock);

				final FlexibleSearchQuery query2 = new FlexibleSearchQuery(builder2.toString());
				query2.addQueryParameters(params2);
				query2.setResultClassList(Arrays.asList(Double.class));
				final SearchResult<Double> searchResult2 = getFlexibleSearchService().search(query2);
				if(searchResult2.getResult()!=null && !searchResult2.getResult().isEmpty() && searchResult2.getResult().get(0)!=null)
					orderBooked = searchResult2.getResult().get(0);
				data.setOrderBooked(orderBooked);

				double stockInHand = ((openingStock+receivedStock+inTransitStock) - (dispatchedStock+orderBooked));

				data.setStockInHand(stockInHand);
			}
		}
		return data;
	}


	@Override
	public Map<String, Map<String, Integer>> getDispatchTATAndDeliveryTime() {
		Map<String, Map<String, Integer>> time = new HashMap<String, Map<String, Integer>>();
		time.put("Dispatch TAT", null);
		time.put("Service Level Delivery Time", null);
		return time;
	}

	@Override
	public Long findStockAvailabilityCounts(WarehouseType type) {
		FilterTalukaData filterTalukaData = new FilterTalukaData();
		List<DepotSubAreaMappingModel> depotSubAreaMappingList = findDepotSubAreaMappingByBrandAndSubArea(territoryService.getTaulkaForUser(filterTalukaData));
		if(depotSubAreaMappingList!=null && !depotSubAreaMappingList.isEmpty()) {
			List<WarehouseModel> warehouseModels = depotSubAreaMappingList.stream().map(DepotSubAreaMappingModel :: getDepot ).distinct().collect(Collectors.toList());
			if(warehouseModels!=null && !warehouseModels.isEmpty()) {
				final Map<String, Object> attr = new HashMap<String, Object>();
				attr.put("type", type);
				attr.put("warehouses", warehouseModels);
				final StringBuilder sql = new StringBuilder();
				sql.append(SEARCH_QUERY);
				final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
				query.setResultClassList(Arrays.asList(Double.class,Double.class,Double.class));
				query.getQueryParameters().putAll(attr);
				final SearchResult<List<Double>> result = getFlexibleSearchService().search(query);
				Double totalStock = 0.0;
				if(result.getResult()!=null && !result.getResult().isEmpty()) {
					if(result.getResult().get(0).size()>0 && result.getResult().get(0).get(0)!=null) {
						totalStock+=result.getResult().get(0).get(0);
					}
					if(result.getResult().get(0).size()>1 && result.getResult().get(0).get(1)!=null) {
						totalStock+=result.getResult().get(0).get(1);
					}
					if(result.getResult().get(0).size()>2 && result.getResult().get(0).get(2)!=null) {
						totalStock-=result.getResult().get(0).get(2);
					}
				}

				return totalStock.longValue();
			}
		}
		return 0L;
	}

	public FlexibleSearchService getFlexibleSearchService() {
		return flexibleSearchService;
	}

	public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
		this.flexibleSearchService = flexibleSearchService;
	}

	@Override
	public Collection<AddressModel> findDepotAddress(WarehouseModel warehouse){
		final Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("depot", warehouse);
		final SearchResult<AddressModel> result = getFlexibleSearchService().search(FIND_DEPOT_ADDRESS,
				queryParams);
		return result.getResult();
	}

	@Override
	public AddressModel findDepotAddressByPk(String pk){
		final Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("pk", pk);
		final SearchResult<AddressModel> result = getFlexibleSearchService().search(FIND_DEPOT_ADDRESS_BY_PK,
				queryParams);
		return result.getResult().get(0);
	}

	//New Territory Change
	@Override
	public List<DepotSubAreaMappingModel> findDepotSubAreaMappingByBrandAndSubArea(List<SubAreaMasterModel> subAreas){
		final Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("brand",baseSiteService.getCurrentBaseSite());
		queryParams.put("subAreaList",subAreas);
		queryParams.put("active",Boolean.TRUE);
		final SearchResult<DepotSubAreaMappingModel> result = getFlexibleSearchService().search(FIND_DEPOTSUBAREA_MAPPING_QUERY,
				queryParams);

		return result.getResult();
	}
	//New Territory Change
	@Override
	public List<String> getDepotListOfGrades(List<SubAreaMasterModel> subAreas) {
		final Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("subAreaList",subAreas);
		queryParams.put("brand",baseSiteService.getCurrentBaseSite());
		queryParams.put("active",Boolean.TRUE);
		FlexibleSearchQuery query = new FlexibleSearchQuery(FIND_DEPOT_GRADES);
		query.setResultClassList(Arrays.asList(String.class));
		query.addQueryParameters(queryParams);
		final SearchResult<String> searchResult = flexibleSearchService.search(query);
		List<String> result = searchResult.getResult();
		return result!=null && !result.isEmpty() ? result : Collections.emptyList();
	}

	@Override
	public SearchPageData<ISOMasterModel> getISOVisibilityDetails(SearchPageData<ISOVisibilityData> searchPageData,
																  List<String> depotCodes, String status, List<String> deliveryMode) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {ISOMaster} WHERE {depotCode} IN (?depotCodes) AND {packagingType} IN ('Packed' , 'PACKED')");

		SearchPageData<ISOMasterModel> result = new SearchPageData<ISOMasterModel>();
		result.setPagination(searchPageData.getPagination());
		result.setSorts(searchPageData.getSorts());

		if(status.equalsIgnoreCase("Delivered"))
		{
			builder.append(" AND {receiptDate} IS NOT NULL");
		}
		else if(status.equalsIgnoreCase("Pending"))
		{
			builder.append(" AND {actualDepartureDate} IS NOT NULL AND {receiptDate} IS NULL");
		}

		if(Objects.isNull(deliveryMode))
		{
			deliveryMode = Collections.emptyList();
		}

		if(!deliveryMode.isEmpty())
		{

			builder.append(" AND {deliveryMode} IN (?deliveryMode)");
			params.put("deliveryMode", deliveryMode);
		}

		params.put("depotCodes", depotCodes);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);

		final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
		parameter.setSearchPageData(result);

		query.setResultClassList(Collections.singletonList(ISOMasterModel.class));
		parameter.setFlexibleSearchQuery(query);

		return paginatedFlexibleSearchService.search(parameter);
	}

	@Override
	public String getProductForISOVisibility(String grade, String state, String packCondition, String brand) {
		return (String)  sessionService.executeInLocalView(new SessionExecutionBody()
		{
			@Override
			public String execute()
			{
				try {
					searchRestrictionService.disableSearchRestrictions();
					final Map<String, Object> params = new HashMap<String, Object>();
					final StringBuilder builder = new StringBuilder("SELECT {name} FROM {Product} WHERE {grade}=?grade AND {state}=?state AND {packagingCondition}=?packCondition AND {catalogVersion}=?catalogVersion");

					CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(brand + "ProductCatalog", "Online");

					params.put("grade", grade);
					params.put("state", state);
					params.put("packCondition", packCondition);
					params.put("catalogVersion", catalogVersion);

					final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
					query.setResultClassList(Arrays.asList(String.class));
					query.addQueryParameters(params);
					final SearchResult<String> searchResult = flexibleSearchService.search(query);
					if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
						return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : "";
					else
						return "";
				}
				finally {
					searchRestrictionService.enableSearchRestrictions();
				}
			}
		});
	}

	@Override
	public Integer getMRNPendingCount(List<String> depotCodes) {

		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT COUNT(*) FROM {ISOMaster} WHERE {depotCode} IN (?depotCodes) AND {packagingType} IN ('Packed' , 'PACKED') AND {actualDepartureDate} IS NOT NULL AND {receiptDate} IS NULL");

		params.put("depotCodes", depotCodes);

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
	public Date getEtaDateForIsoMaster(String deliveryId) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {etaDate} FROM {EtaTracker} WHERE {deliveryId} = ?deliveryId");
		params.put("deliveryId", deliveryId);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Date.class));
		query.addQueryParameters(params);
		final SearchResult<Date> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty())) {
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : null;
		}
		else {
			return null;
		}
	}

}