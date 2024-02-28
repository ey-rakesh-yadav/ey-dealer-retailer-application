package com.eydms.core.dao.impl;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.enums.*;
import com.eydms.core.model.*;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.category.CategoryService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.dao.SchemesAndDiscountDao;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;

public class SchemesAndDiscountDaoImpl implements SchemesAndDiscountDao{
	
	@Autowired
	private FlexibleSearchService flexibleSearchService;
	
	@Autowired
	UserService userService;

	@Autowired
	CategoryService categoryService;

	@Autowired
	CatalogVersionService catalogVersionService;
	@Autowired
	BaseSiteService baseSiteService;

	Map<String, String> giftOrderSortCodeToQueryAlias;

	public PaginatedFlexibleSearchService getPaginatedFlexibleSearchService() {
		return paginatedFlexibleSearchService;
	}

	public void setPaginatedFlexibleSearchService(PaginatedFlexibleSearchService paginatedFlexibleSearchService) {
		this.paginatedFlexibleSearchService = paginatedFlexibleSearchService;
	}

	@Autowired
	PaginatedFlexibleSearchService paginatedFlexibleSearchService;
	
	@Override
	public Collection<GiftShopModel> findCategoriesByGeographyAndPeroid(CatalogVersionModel catalogVersion,
			String geography, String counterType, String influencerType, LocalDate now) {
		final StringBuilder query = new StringBuilder("SELECT {cat." + GiftShopModel.PK + "} ");
		query.append("FROM {" + GiftShopModel._TYPECODE + " AS cat} ");
		query.append("WHERE {cat." + GiftShopModel.COUNTERTYPE + "} = ?" + GiftShopModel.COUNTERTYPE);
		query.append(" AND {cat." + GiftShopModel.CATALOGVERSION + "} = (?" + GiftShopModel.CATALOGVERSION + ")");
		query.append(" AND {cat." + GiftShopModel.STARTDATE + "} <= ?date");
		query.append(" AND {cat." + GiftShopModel.ENDDATE + "} >= ?date");
		query.append(" AND {cat." + GiftShopModel.GEOGRAPHY + "} like ?" + GiftShopModel.GEOGRAPHY);
		query.append(" AND {cat." + GiftShopModel.INFLUENCERTYPES + "} like ?" + GiftShopModel.INFLUENCERTYPES);


		final Map<String, Object> params = new HashMap<String, Object>(4);
		params.put(GiftShopModel.CATALOGVERSION, catalogVersion);
		params.put(GiftShopModel.GEOGRAPHY, "%"+geography+"%");
		params.put(GiftShopModel.COUNTERTYPE, CounterType.valueOf(counterType));
		params.put(GiftShopModel.INFLUENCERTYPES, "%"+influencerType+"%");
		params.put("date", now.toString());

		
		final FlexibleSearchQuery searchQuery = new FlexibleSearchQuery(query.toString());
		searchQuery.addQueryParameters(params);
		searchQuery.setResultClassList(Collections.singletonList(GiftShopModel.class));
		final SearchResult<GiftShopModel> searchResult = flexibleSearchService.search(searchQuery);
		return searchResult.getResult();
		}

	@Override
	public List<List<Object>> findCurrentYearSchemes(CatalogVersionModel catalogVersion, String geography,
			String counterType, String influencerType, String financiyalYearFrom, String financiyalYearTo) {
		final StringBuilder query = new StringBuilder("select distinct {g.code},{g.name},{enum.code},sum({t.points}) as points "
				+ "from {GiftShop as g left join "
				+ "PointsTransactionMaster as t on {g.pk}={t.scheme} and {t.customer} =?customer  "
				+ "left join EnumerationValue AS enum "
				+ "ON {enum.pk}={t.transactionType}} where {g.startDate} >= ?startDate and {g.endDate} < ?endDate "
				+ "and {g.counterType} = ?counterType and {g.influencerTypes} like ?influencerTypes "
				+ "and {g.geography} like ?geography ");
		query.append(" AND {g." + GiftShopModel.CATALOGVERSION + "} = (?" + GiftShopModel.CATALOGVERSION + ") ");
		query.append("group by {g.code},{g.name},{enum.code} ");


		final Map<String, Object> params = new HashMap<String, Object>(4);
		params.put(GiftShopModel.CATALOGVERSION, catalogVersion);
		params.put(GiftShopModel.STARTDATE, financiyalYearFrom);
		params.put(GiftShopModel.ENDDATE, financiyalYearTo);
		params.put(PointsTransactionMasterModel.CUSTOMER, userService.getCurrentUser());
		params.put(GiftShopModel.COUNTERTYPE, CounterType.valueOf(counterType));
		params.put("influencerTypes", "%"+influencerType+"%");
		params.put(GiftShopModel.GEOGRAPHY, "%"+geography+"%");

		final FlexibleSearchQuery searchQuery = new FlexibleSearchQuery(query.toString());
		searchQuery.addQueryParameters(params);
		searchQuery.setResultClassList(Arrays.asList(String.class,String.class,String.class,Double.class));
		final SearchResult<List<Object>> searchResult = flexibleSearchService.search(searchQuery);
		return searchResult.getResult();
	}

	@Override
	public String findRecentGiftOrder(UserModel customer, GiftType giftType, GiftShopModel giftShop) {
		final StringBuilder query = new StringBuilder(" Select top 1 {o.code} from {Order as o }")
				.append(" where {o.user}=?customer and {o.crmOrderType}=?crmOrderType and {o.giftType}=?giftType" ) ;

		final Map<String, Object> params = new HashMap<String, Object>(4);

//		if(giftShop!=null) {
//			params.put(OrderModel.GIFTSHOP, giftShop);
//			query.append(" and {o.giftShop}=?giftShop ");
//		}
		query.append(" and {o.cancelledDate} is null ");
		query.append(" order by {o.date} desc ");
		
		params.put("customer", customer);
		params.put("crmOrderType", CRMOrderType.GIFT);
		params.put("giftType", giftType);

		final FlexibleSearchQuery searchQuery = new FlexibleSearchQuery(query.toString());
		searchQuery.addQueryParameters(params);
		searchQuery.setResultClassList(Collections.singletonList(String.class));
		final SearchResult<String> searchResult = flexibleSearchService.search(searchQuery);
		return searchResult.getResult()!=null && !searchResult.getResult().isEmpty()? searchResult.getResult().get(0) : null;
	}

	@Override
	public List<OrderModel> getGiftOrdersForDistrictAndTaluka(List<String> districtSubarea, String status, String key) {
		final Map<String, Object> params = new HashMap<String, Object>();
	
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {Order AS o JOIN EyDmsCustomer AS c ON {o.user}={c.pk} } WHERE {o.crmOrderType}=?Gift AND CONCAT({o.district},'_',{o.subArea}) IN (?districtSubArea)");
		
		CRMOrderType gift = CRMOrderType.GIFT;
		params.put("gift", gift);	
		params.put("districtSubarea", districtSubarea);	
		
		if(key!=null)
		{
			String searchKey = "%".concat(key).concat("%");
			
			builder.append("  AND ( {o.code} LIKE ?searchKey OR {c.name} LIKE ?searchKey OR {c.mobileNumber} LIKE ?searchKey OR {c.customerNo} LIKE ?searchKey )");
			params.put("searchKey", searchKey);
		}
		
		if(status!=null)
		{
			OrderStatus orderStatus=null;
			
			if(status.compareTo("Pending")==0)
				orderStatus=OrderStatus.PENDING_APPROVAL;
			else if(status.compareTo("Approved")==0)
				orderStatus=OrderStatus.APPROVED;
			else if(status.compareTo("Rejected")==0)
				orderStatus=OrderStatus.REJECTED;
			
			builder.append(" AND {o.status} = ?status");
			
			params.put("status", orderStatus);
		}
		
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(OrderModel.class));
		query.addQueryParameters(params);
		final SearchResult<OrderModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
	}

	@Override
	public SearchPageData<OrderEntryModel> getDisbursementHistory(UserModel user, GiftShopModel giftShop, String startDate,
			String endDate, BaseSiteModel currentBaseSite, SearchPageData searchPageData, String orderCode) {
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT {oe:pk} FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk}} WHERE {o.crmOrderType}=?crmOrderType and {o:cancelledDate} is null ");
		final Map<String, Object> params = new HashMap<String, Object>(6);
		if(user!=null) {
			params.put(OrderModel.USER, user);
			sql.append(" and {o.user}=?user ");
		}
		if(giftShop!=null) {
			params.put(OrderModel.GIFTSHOP, giftShop);			
			sql.append(" and {o.giftShop}=?giftShop ");
		}
		if(orderCode!=null) {
			params.put(OrderModel.CODE, orderCode);			
			sql.append(" and {o.code}=?code ");
		}
		if(startDate!=null && endDate!=null) {
			params.put("startDate", startDate);
			params.put("endDate", endDate);
			sql.append(" and {oe.dateOfReceiving}>=?startDate and {oe.dateOfReceiving} <=?endDate");
		}
		params.put(OrderModel.SITE, currentBaseSite);
		params.put("crmOrderType", CRMOrderType.GIFT);
		
		final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
		parameter.setSearchPageData(searchPageData);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Collections.singletonList(OrderEntryModel.class));
		query.getQueryParameters().putAll(params);
		parameter.setFlexibleSearchQuery(query);
		return paginatedFlexibleSearchService.search(parameter);
	}

	@Override
	public SearchPageData<ProductModel> getProductsForScheme(CategoryModel giftShop,
			SearchPageData searchPageData, CategoryModel giftType) {
		final StringBuilder sql = new StringBuilder();
		sql.append("select {p.pk} from {CategoryProductRelation as cp join Product as p on {cp.source}=?giftShop and {p.pk}={cp.target}  join CategoryProductRelation as pp on {pp.source}=?giftType and {p.pk}={pp.target}} order by {p.name}");
		final Map<String, Object> params = new HashMap<String, Object>(2);
		params.put("giftShop", giftShop);
		params.put("giftType", giftType);

		final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
		parameter.setSearchPageData(searchPageData);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Collections.singletonList(ProductModel.class));
		query.getQueryParameters().putAll(params);
		parameter.setFlexibleSearchQuery(query);
		return paginatedFlexibleSearchService.search(parameter);
	}

	@Override
	public PointsTransactionMasterModel findPointsTransactionMasterByOrderCode(String orderCode) {
		final StringBuilder query = new StringBuilder();
		query.append("SELECT {p.pk} FROM {PointsTransactionMaster as p} WHERE {p.active}=?active AND {p.transactionType}=?transactionType and {p.orderCode}=?orderCode ");

		final Map<String, Object> params = new HashMap<String, Object>(8);

		params.put(PointsTransactionMasterModel.ORDERCODE, orderCode);			
		params.put(PointsTransactionMasterModel.ACTIVE, Boolean.TRUE);			
		params.put(PointsTransactionMasterModel.TRANSACTIONTYPE, TransactionType.DEBIT);			

		final FlexibleSearchQuery searchQuery = new FlexibleSearchQuery(query.toString());
		searchQuery.addQueryParameters(params);
		searchQuery.setResultClassList(Collections.singletonList(PointsTransactionMasterModel.class));
		final SearchResult<PointsTransactionMasterModel> searchResult = flexibleSearchService.search(searchQuery);
		return searchResult.getResult()!=null && !searchResult.getResult().isEmpty()? searchResult.getResult().get(0) : null;
	}

    @Override
    public SearchPageData<OrderModel> getPaginatedGiftOrderList(SearchPageData searchPageData, String scheme, String searchFilter, List<String> status) {
		final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
		parameter.setSearchPageData(searchPageData);
		final Map<String, Object> params = new HashMap<String, Object>();
		StringBuilder builder = new StringBuilder();


		if(userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID)))
		{
			if(searchFilter!=null){
				builder.append("select {o.pk} from {Order as o JOIN EyDmsCustomer as u on {u:pk}={o:user} } where {o.crmOrderType}=?Gift and {o.user}=?currentuser ");
			}
			else{
				builder.append("select {o.pk} from {Order as o} where {o.crmOrderType}=?Gift and {o.user}=?currentuser ");
			}

		}
		else {
			if(searchFilter!=null){
				builder.append("select {o.pk} from {Order as o JOIN EyDmsCustomer as u on {u:pk}={o:user} } where {o.crmOrderType}=?Gift");
			}
			else{
				builder.append("select {o.pk} from {Order as o} where {o.crmOrderType}=?Gift");
			}

		}

        params.put("currentUser",userService.getCurrentUser());
		CRMOrderType gift = CRMOrderType.GIFT;
		params.put("gift", gift);


		if(StringUtils.isNotBlank(searchFilter)){
			builder.append(" and (UPPER({o:code}) like ?searchKey OR UPPER({u:uid}) like ?searchKey OR UPPER({u:name}) like ?searchKey OR UPPER({u:customerNo}) like ?searchKey) ");
			params.put("searchKey","%"+searchFilter.toUpperCase()+"%");
		}

		if(status!=null && !status.isEmpty())
		{

			List<OrderStatus> orderStatus = status.stream().map(obj -> OrderStatus.valueOf(obj)).collect(Collectors.toList());
			if(orderStatus!=null && !orderStatus.isEmpty())
			{
				builder.append(" and ");
				builder.append(" {o:status} IN (?orderStatus) ");
				params.put("orderStatus", orderStatus);
			}

		}


		/*if(scheme!=null) {

			CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");

			GiftShopModel giftShop = (GiftShopModel) categoryService.getCategoryForCode(catalogVersion, scheme);
			builder.append(" and ");
			builder.append(" {o:giftShop}=?giftShop  ");

			params.put("giftShop", giftShop);

		}*/


		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		parameter.setFlexibleSearchQuery(query);
		parameter.setSortCodeToQueryAlias(getGiftOrderSortCodeToQueryAlias());
		return getPaginatedFlexibleSearchService().search(parameter);
    }

	@Override
	public SearchPageData<GiftShopModel> getPaginatedGiftShopSchemeList(SearchPageData searchPageData, String scheme, String searchFilter, List<String> status) {
		final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
		parameter.setSearchPageData(searchPageData);
		final Map<String, Object> params = new HashMap<String, Object>();
		StringBuilder builder = new StringBuilder();
		if(searchFilter!=null){
			builder.append("select {o.giftShop} from {Order as o JOIN EyDmsCustomer as u on {u:pk}={o:user} } where {o.crmOrderType}=?Gift  ");
		}
		else{
			builder.append("select {o.giftShop} from {Order as o} where {o.crmOrderType}=?Gift ");
		}

		CRMOrderType gift = CRMOrderType.GIFT;
		params.put("gift", gift);

		if(StringUtils.isNotBlank(searchFilter)){
			builder.append(" and (UPPER({o:code}) like ?searchKey OR UPPER({u:uid}) like ?searchKey OR UPPER({u:name}) like ?searchKey OR UPPER({u:customerNo}) like ?searchKey) ");
			params.put("searchKey","%"+searchFilter.toUpperCase()+"%");
		}

		if(status!=null && !status.isEmpty())
		{

			List<OrderStatus> orderStatus = status.stream().map(obj -> OrderStatus.valueOf(obj)).collect(Collectors.toList());
			if(orderStatus!=null && !orderStatus.isEmpty())
			{
				builder.append(" and ");
				builder.append(" {o:status} IN (?orderStatus) ");
				params.put("orderStatus", orderStatus);
			}

		}
		if(scheme!=null) {

			CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");

			GiftShopModel giftShop = (GiftShopModel) categoryService.getCategoryForCode(catalogVersion, scheme);
			builder.append(" and ");
			builder.append(" {o:giftShop}=?giftShop  ");

			params.put("giftShop", giftShop);

		}

		builder.append(" group by {o.giftShop}");


		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(GiftShopModel.class));
		query.getQueryParameters().putAll(params);
		parameter.setFlexibleSearchQuery(query);
		return paginatedFlexibleSearchService.search(parameter);

	}

	public Map<String, String> getGiftOrderSortCodeToQueryAlias() {
		return giftOrderSortCodeToQueryAlias;
	}

	public void setGiftOrderSortCodeToQueryAlias(Map<String, String> giftOrderSortCodeToQueryAlias) {
		this.giftOrderSortCodeToQueryAlias = giftOrderSortCodeToQueryAlias;
	}

	@Override
	public Collection<GiftSchemeModel> findGiftSchemeByStateAndPeriod(String state, String influencerType, LocalDate now) {
		final StringBuilder query = new StringBuilder("select {pk} from {GiftScheme} where {state} = ?state and {influencerType} = ?influencerType and {startDate} <= ?date and {endDate} >= ?date ");

		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("state", state);
		params.put("influencerType", influencerType);
		params.put("date", now.toString());

		final FlexibleSearchQuery searchQuery = new FlexibleSearchQuery(query.toString());
		searchQuery.addQueryParameters(params);
		searchQuery.setResultClassList(Collections.singletonList(GiftSchemeModel.class));
		final SearchResult<GiftSchemeModel> searchResult = flexibleSearchService.search(searchQuery);
		return searchResult.getResult();
	}

}
