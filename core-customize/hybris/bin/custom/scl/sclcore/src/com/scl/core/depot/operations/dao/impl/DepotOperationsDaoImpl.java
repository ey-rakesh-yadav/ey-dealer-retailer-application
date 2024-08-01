package com.scl.core.depot.operations.dao.impl;

import java.text.DecimalFormat;
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

import com.scl.core.model.DepotSubAreaMappingModel;
import com.scl.core.model.ISOMasterModel;
import com.scl.core.model.SubAreaMasterModel;
import com.scl.core.model.VisitMasterModel;
import com.scl.core.services.impl.TerritoryManagementServiceImpl;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.customer.services.impl.DefaultSclCustomerAccountService;
import com.scl.core.depot.operations.dao.DepotOperationsDao;
import com.scl.core.enums.CustomerCategory;
import com.scl.core.enums.VisitStatus;
import com.scl.core.enums.WarehouseType;
import com.scl.facades.data.FilterTalukaData;
import com.scl.facades.data.ISOVisibilityData;
import com.scl.facades.depot.operations.data.DailyCapacityUtilizationData;
import com.scl.facades.depot.operations.data.DepotStockData;

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
	DecimalFormat df = new DecimalFormat("#0.00");
	private FlexibleSearchService flexibleSearchService;

	private static final String DEFAULT_QUERY ="SELECT {w.code}, {w.name}, SUM({s.openingStock}), SUM({s.stockIn}), SUM({s.stockOut}) FROM {Warehouse AS w JOIN DepotStockMaster AS s ON {w.pk}={s.depot} ";
	//private static final String PRODUCT_JOIN_QUERY =" JOIN Product AS p ON  {s.custCategory} = {p.custCategory} AND {s.inventoryId} = {p.inventoryId}  JOIN CatalogVersion AS c ON {c.pk}={p.catalogVersion} AND {c.version}='Online' ";
	private static final String DEFAULT_WHERE__QUERY =" } WHERE {w.type}=?depot and {w.pk} in (?warehouses) ";
	private static final String PRODUCT_QUERY =" AND {p.grade} in (?productList) ";
	private static final String END_QUERY =" GROUP BY {w.code},{w.name}";

	private static final String ORDER_BOOKED_QUERY = "SELECT SUM({oe.quantityInMT}) FROM {OrderEntry AS oe JOIN Warehouse AS w ON {w.pk}={oe.source} JOIN Product AS p ON {oe.product} = {p.pk} } WHERE  {w.type}=?depot AND {oe.erpLineItemId} is not null  and {oe.cancelledDate} is null AND ({oe.remainingQuantity} IS NULL OR {oe.remainingQuantity}=0) and {oe.source} in (?warehouses) ";
	//private static final String STOCK_DISPATCHED_QUERY = "SELECT SUM({oe.quantityInMT}) FROM { OrderEntry AS oe JOIN Order AS o on {o.pk}={oe.order} JOIN Warehouse AS w ON {w.pk}={oe.source} JOIN DepotSubAreaMapping AS sa ON {sa.depot}={o.warehouse} JOIN Product AS p ON {oe.product} = {p.pk} } WHERE {oe.truckDispatchedDate} BETWEEN ?startDate AND ?endDate AND  {sa.subAreaMaster} IN (?subAreaList) and {sa.brand}=?brand and {sa.active}=?active  ";

	private static final String DEPOT_QUERY=" AND {w.code} in (?depotList) ";

	private static final String OPENING_AND_INTRANSIT_STOCK_QUERY = "SELECT SUM({s.openingStock}), SUM({s.stockIn}), SUM({s.stockOut}) FROM {DepotStockMaster AS s JOIN Warehouse AS w ON {w.pk}={s.depot} ";

	private static final Logger LOGGER = Logger.getLogger(DefaultSclCustomerAccountService.class);

	private static final String SEARCH_QUERY ="SELECT SUM({s.openingStock}), sum({s.stockIn}), sum({s.stockOut}) FROM {DepotStockMaster AS s JOIN Warehouse AS w ON {w.pk}={s.depot}} WHERE {w.type}=?type and  {w.pk} in (?warehouses) ";

	private static final String FIND_DEPOT_ADDRESS = "SELECT {address:pk} FROM {Address AS address} WHERE {address:owner}=?depot " ;

	private static final String FIND_DEPOT_ADDRESS_BY_PK = "SELECT {address:pk} FROM {Address AS address} WHERE {address:pk}=?pk " ;
	//New Territory Change
//	private static final String FIND_DEPOT_GRADES = "SELECT distinct({p.code}),{p.name} FROM {DepotStockMaster AS s JOIN  DepotSubAreaMapping AS sa ON {s.depot}={sa.depot} JOIN Product AS p ON {p.code}={s.productCode} JOIN ISOMaster as im on {p:code}={im.productCode}} WHERE {sa.subAreaMaster} IN (?subAreaList) and {sa.brand}=?brand and {sa.active}=?active ";
	private static final String FIND_DEPOT_GRADES = "SELECT distinct({p.code}),{p.name} FROM {DepotStockMaster AS s JOIN  DepotSubAreaMapping AS sa ON {s.depot}={sa.depot} JOIN Product AS p ON {p.code}={s.productCode} } WHERE {sa.subAreaMaster} IN (?subAreaList) and {sa.brand}=?brand and {sa.active}=?active ";
	
	private WarehouseType depot = WarehouseType.DEPOT;

	private static final String FIND_DEPOTSUBAREA_MAPPING_QUERY = "SELECT {"+DepotSubAreaMappingModel.PK+"} FROM {"+DepotSubAreaMappingModel._TYPECODE+"} WHERE {"+DepotSubAreaMappingModel.BRAND+"} = ?brand AND {subAreaMaster} IN (?subAreaList) AND {"+DepotSubAreaMappingModel.ACTIVE+"} =?active" ;

	private static final String FIND_PRODUCT_ISOMASTER = "SELECT  distinct({p.code}),{p.name} FROM {ISOMaster as im JOIN Product as p on {p:code}={im:productCode}} WHERE {im:depotCode} IN (?depotCodes) AND {im:packagingType} NOT IN ('Loose' , 'LOOSE') ";
	private static final String FIND_PRODUCT_ISOMASTER_FALSE = "SELECT  distinct({p.code}),{p.name} FROM {ISOMaster as im JOIN Product as p on {p:code}={im:productCode}} WHERE {im:depotCode} IN (?depotCodes) AND {im:packagingType} NOT IN ('Loose' , 'LOOSE') AND {actualDepartureDate} IS NOT NULL AND {receiptDate} IS NULL  ";

	private static final String FIND_WAREHOUSE_NAME = "SELECT {w:name} FROM {WareHouse as w} WHERE {w:code}=?code ";
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
	public List<DepotStockData> getStockAvailability(List<String> productCodes, List<String> depotCode) {
		List<DepotStockData> stockData = new ArrayList<>();
		final Map<String, Object> params = new HashMap<String, Object>();
		FilterTalukaData filterTalukaData = new FilterTalukaData();
		List<DepotSubAreaMappingModel> depotSubAreaMappingList = findDepotSubAreaMappingByBrandAndSubArea(territoryService.getTaulkaForUser(filterTalukaData));
		if(depotSubAreaMappingList!=null && !depotSubAreaMappingList.isEmpty()) {
			List<WarehouseModel> warehouseModels = depotSubAreaMappingList.stream().map(DepotSubAreaMappingModel :: getDepot ).distinct().collect(Collectors.toList());
			if(warehouseModels!=null && !warehouseModels.isEmpty()) {
				final StringBuilder builder = new StringBuilder(DEFAULT_QUERY);
				builder.append(DEFAULT_WHERE__QUERY);
				if(Objects.nonNull(productCodes))
				{
					builder.append(" AND {s.productCode} in (?productList) ");
					params.put("productList", productCodes);
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

				final StringBuilder builder1 = new StringBuilder("SELECT {depotCode}, sum({qtyShipped}) FROM {ISOMaster} WHERE {depotCode} IN (?depotCodes) AND {packagingType} NOT IN ('Loose' , 'LOOSE') AND {actualDepartureDate} IS NOT NULL AND {receiptDate} IS NULL ");
				final Map<String, Object> params1 = new HashMap<String, Object>();
				if(depotCode!=null && !depotCode.isEmpty())
				{
					params1.put("depotCodes", depotCode);
				}
				else {
					params1.put("depotCodes", warehouseModels.stream().map(data1->data1.getCode()).collect(Collectors.toList()));
				}
				if(Objects.nonNull(productCodes))
				{
					builder1.append(" and {productCode} in (?productList) ");
					params1.put("productList", productCodes);
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
				/*	if(data.get(3)!=null)
						stock+=(Double)data.get(3);
					if(data.size()>4 && data.get(4)!=null)
						stock-=(Double)data.get(4);
					d.setOpeningStock(stock);
				*/
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
					if(d.getInTransitStock()!=0.0 || d.getOpeningStock()!=0.0){
						stockData.add(d);
					}

				}
			}
		}
		return stockData;
	}

	@Override
	public DailyCapacityUtilizationData getDailyCapacityUtilization(List<String> productCodes, List<String> depotCode,  UserModel user, Date startDate, Date endDate)
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

				final StringBuilder builder1 = new StringBuilder("SELECT sum({qtyShipped}) FROM {ISOMaster} WHERE {depotCode} IN (?depotCodes) AND {deliveryMode}='ROAD' AND {packagingType} NOT IN ('Loose' , 'LOOSE') AND {actualDepartureDate} IS NOT NULL AND {receiptDate} IS NULL ");
				final Map<String, Object> params1 = new HashMap<String, Object>();

				final StringBuilder builder4 = new StringBuilder("SELECT sum({qtyShipped}) FROM {ISOMaster} WHERE {depotCode} IN (?depotCodes) AND {deliveryMode}='RAIL' AND {packagingType} NOT IN ('Loose' , 'LOOSE') AND {actualDepartureDate} IS NOT NULL AND {receiptDate} IS NULL ");

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

				if(Objects.nonNull(productCodes))
				{
					//builder2.append(PRODUCT_QUERY);
					builder2.append(" AND {p:code} in (?productList) ");
					params2.put("productList", productCodes);
					
					builder3.append(" AND {s.productCode} in (?productList) ");
					params3.put("productList", productCodes);
					
					builder1.append(" and {productCode} in (?productList) ");
					params1.put("productList", productCodes);
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
				LOGGER.info(String.format("Qurey1 OPENING_AND_INTRANSIT_STOCK_QUERY:%s",query1));
				query1.setResultClassList(Arrays.asList(Double.class));
				final SearchResult<Double> searchResult1 = getFlexibleSearchService().search(query1);
				if(searchResult1.getResult()!=null && !searchResult1.getResult().isEmpty() && searchResult1.getResult().get(0)!=null) {
					roadTransitStock = searchResult1.getResult().get(0);
				}

				final FlexibleSearchQuery query4 = new FlexibleSearchQuery(builder4.toString());
				query4.addQueryParameters(params1);
				LOGGER.info(String.format("Qurey4 :%s",query4));
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
				LOGGER.info(String.format("Qurey3 OPENING_AND_INTRANSIT_STOCK_QUERY:%s",query3));
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
				LOGGER.info(String.format("Qurey for Order booked:%s",query2));
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
	public String findStockAvailabilityCounts(WarehouseType type) {
		String totalStockValue = null;
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
				LOGGER.info(String.format("Query for Stock Availability Card:%s",query));
				final SearchResult<List<Double>> result = getFlexibleSearchService().search(query);

				Double totalStock = 0.0;
				if(result.getResult()!=null && !result.getResult().isEmpty()) {
					if(result.getResult().get(0).size()>0 && result.getResult().get(0).get(0)!=null) {
						totalStock+=result.getResult().get(0).get(0);
					}
				/*	if(result.getResult().get(0).size()>1 && result.getResult().get(0).get(1)!=null) {
						totalStock+=result.getResult().get(0).get(1);
					}
					if(result.getResult().get(0).size()>2 && result.getResult().get(0).get(2)!=null) {
						totalStock-=result.getResult().get(0).get(2);
					}*/
				}
				LOGGER.info(String.format("Stock Availability Card Value:%s",String.valueOf(totalStock)));
				totalStockValue=df.format(totalStock);
				return totalStockValue;
			}
		}
		return totalStockValue;
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
	public List<List<Object>> getDepotListOfGrades(List<SubAreaMasterModel> subAreas,List<String> depotCodes,boolean forISOOrders) {
		/*final Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("subAreaList",subAreas);
		queryParams.put("brand",baseSiteService.getCurrentBaseSite());
		queryParams.put("active",Boolean.TRUE);
		FlexibleSearchQuery query = new FlexibleSearchQuery(FIND_DEPOT_GRADES);
		query.setResultClassList(Arrays.asList(String.class, String.class));
		query.addQueryParameters(queryParams);
		final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
		List<List<Object>> result = searchResult.getResult();
		return result!=null && !result.isEmpty() ? result : Collections.emptyList();*/

			final Map<String, Object> queryParams = new HashMap<String, Object>();

			FilterTalukaData filterTalukaData = new FilterTalukaData();
			queryParams.put("depotCodes", depotCodes);
			queryParams.put("subAreaList", territoryService.getTaulkaForUser(filterTalukaData));
			queryParams.put("brand", baseSiteService.getCurrentBaseSite());
			queryParams.put("active", Boolean.TRUE);


			FlexibleSearchQuery query1 = new FlexibleSearchQuery(FIND_PRODUCT_ISOMASTER);
			FlexibleSearchQuery query3 = new FlexibleSearchQuery(FIND_PRODUCT_ISOMASTER_FALSE);
			FlexibleSearchQuery query2 = new FlexibleSearchQuery(FIND_DEPOT_GRADES);

			query1.setResultClassList(Arrays.asList(String.class, String.class));
			query1.addQueryParameters(queryParams);
			LOGGER.info(String.format("Query for Product List1:%s", query1));
			final SearchResult<List<Object>> searchResult1 = flexibleSearchService.search(query1);
			List<List<Object>> result1 = searchResult1.getResult();

			query2.setResultClassList(Arrays.asList(String.class, String.class));
			query2.addQueryParameters(queryParams);
			LOGGER.info(String.format("Query for Depot Grades List2:%s", query2));
			final SearchResult<List<Object>> searchResult2 = flexibleSearchService.search(query2);
			List<List<Object>> result2 = searchResult2.getResult();

			query3.setResultClassList(Arrays.asList(String.class, String.class));
			query3.addQueryParameters(queryParams);
			LOGGER.info(String.format("Query for Product List3:%s", query3));
			final SearchResult<List<Object>> searchResult3 = flexibleSearchService.search(query3);
			List<List<Object>> result3 = searchResult3.getResult();

			List<List<Object>> finalResult = new ArrayList<>();
			if (CollectionUtils.isNotEmpty(result3)) {
				finalResult.addAll(result3);
			}
			if (CollectionUtils.isNotEmpty(result2)) {
				finalResult.addAll(result2);
			}

			if (BooleanUtils.isTrue(forISOOrders)) {
				return result1 != null && !result1.isEmpty() ? result1.stream().distinct().collect(Collectors.toList()) : Collections.emptyList();
			} else {
				return finalResult != null && !finalResult.isEmpty() ? finalResult.stream().distinct().collect(Collectors.toList()) : Collections.emptyList();
			}
		}

	@Override
	public SearchPageData<ISOMasterModel> getISOVisibilityDetails(SearchPageData<ISOVisibilityData> searchPageData,
			List<String> depotCodes, String status, List<String> deliveryMode, List<String> productCodes) {
		final Map<String, Object> params = new HashMap<String, Object>();
		//final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {ISOMaster} WHERE {depotCode} IN (?depotCodes) AND {packagingType} IN ('Packed' , 'PACKED')");
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {ISOMaster} WHERE {depotCode} IN (?depotCodes) AND {packagingType} NOT IN ('Loose' , 'LOOSE') ");

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

		if(CollectionUtils.isNotEmpty(productCodes)){
			builder.append(" AND {productCode} IN (?productCodes)");
			params.put("productCodes", productCodes);
		}
		
		params.put("depotCodes", depotCodes);

		builder.append(" ORDER BY {deliveryId} DESC ");

		LOGGER.info(String.format("Query for getISOVisibilityDetails :: %s ",builder.toString()));
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
		//final StringBuilder builder = new StringBuilder("SELECT COUNT(*) FROM {ISOMaster} WHERE {depotCode} IN (?depotCodes) AND {packagingType} IN ('Packed' , 'PACKED') AND {actualDepartureDate} IS NOT NULL AND {receiptDate} IS NULL");
		final StringBuilder builder = new StringBuilder("SELECT COUNT(*) FROM {ISOMaster} WHERE {depotCode} IN (?depotCodes) AND {packagingType} NOT IN ('Loose' , 'LOOSE') AND {actualDepartureDate} IS NOT NULL AND {receiptDate} IS NULL");

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

    @Override
    public List<List<Object>> getProductListForDepot(List<String> depotCodes,boolean forISOOrders) {
		final Map<String, Object> queryParams = new HashMap<String, Object>();

		FilterTalukaData filterTalukaData = new FilterTalukaData();
		queryParams.put("depotCodes", depotCodes);
		queryParams.put("subAreaList",territoryService.getTaulkaForUser(filterTalukaData));
		queryParams.put("brand",baseSiteService.getCurrentBaseSite());
		queryParams.put("active",Boolean.TRUE);



		FlexibleSearchQuery query1 = new FlexibleSearchQuery(FIND_PRODUCT_ISOMASTER);
		FlexibleSearchQuery query3 = new FlexibleSearchQuery(FIND_PRODUCT_ISOMASTER_FALSE);
		FlexibleSearchQuery query2 = new FlexibleSearchQuery(FIND_DEPOT_GRADES);

		query1.setResultClassList(Arrays.asList(String.class, String.class));
		query1.addQueryParameters(queryParams);
		LOGGER.info(String.format("Query for Product List1:%s",query1));
		final SearchResult<List<Object>> searchResult1 = flexibleSearchService.search(query1);
		List<List<Object>> result1 = searchResult1.getResult();

		query2.setResultClassList(Arrays.asList(String.class, String.class));
		query2.addQueryParameters(queryParams);
		LOGGER.info(String.format("Query for Depot Grades List2:%s",query2));
		final SearchResult<List<Object>> searchResult2 = flexibleSearchService.search(query2);
			List<List<Object>> result2 = searchResult2.getResult();

		query3.setResultClassList(Arrays.asList(String.class, String.class));
		query3.addQueryParameters(queryParams);
		LOGGER.info(String.format("Query for Product List3:%s",query3));
		final SearchResult<List<Object>> searchResult3 = flexibleSearchService.search(query3);
		List<List<Object>> result3 = searchResult3.getResult();

		List<List<Object>> finalResult=new ArrayList<>();
		if(CollectionUtils.isNotEmpty(result3)) {
			finalResult.addAll(result3);
		}
		if(CollectionUtils.isNotEmpty(result2)) {
			finalResult.addAll(result2);
		}

		if(BooleanUtils.isTrue(forISOOrders))
		{
			return result1!=null && !result1.isEmpty() ? result1.stream().distinct().collect(Collectors.toList()) : Collections.emptyList();
		}else{
			return finalResult!=null && !finalResult.isEmpty() ? finalResult.stream().distinct().collect(Collectors.toList()) : Collections.emptyList();
		}

	}

	@Override
	public String getDepotCodeName(String depotCode) {

		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder(FIND_WAREHOUSE_NAME);

		params.put("code", depotCode);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		query.addQueryParameters(params);
		LOGGER.info(String.format("Query for Warehouse name:%s",query));
		final SearchResult<String> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}
}