package com.eydms.core.dao.impl;

import com.eydms.core.dao.TerritoryManagementDao;
import com.eydms.core.enums.CounterType;
import com.eydms.core.enums.DealerCategory;
import com.eydms.core.enums.InfluencerType;
import com.eydms.core.enums.EyDmsUserType;
import com.eydms.core.model.*;
import com.eydms.core.utility.EyDmsDateUtility;
import com.eydms.facades.data.FilterDistrictData;
import com.eydms.facades.data.FilterRegionData;
import com.eydms.facades.data.FilterTalukaData;
import com.eydms.facades.data.RequestCustomerData;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class TerritoryManagementDaoImpl implements TerritoryManagementDao {

    @Autowired
    FlexibleSearchService flexibleSearchService;

    @Autowired
    UserService userService;

    public FlexibleSearchService getFlexibleSearchService() {
        return flexibleSearchService;
    }

    @Resource
    private B2BUnitService b2bUnitService;

    @Autowired
    BaseSiteService baseSiteService;

    @Autowired
    PaginatedFlexibleSearchService paginatedFlexibleSearchService;

    static String CUSTOMER_FOR_USER_QUERY = "CustomerSubAreaMapping as m join EyDmsCustomer as c on {m.eydmsCustomer}={c.pk} ";
    
    //TO_BE_CHECKED
    @Override
    public List<List<Object>> getAllStateDistrictSubAreaForSO(BaseSiteModel site) {

        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {state},{district},{subArea} FROM {UserSubAreaMapping} WHERE {eydmsUser} = ?eydmsUser AND {isActive} = ?active AND {brand} = ?brand");
        boolean active = Boolean.TRUE;
        params.put("eydmsUser", userService.getCurrentUser());
        params.put("active", active);
        params.put("brand", site);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(String.class, String.class, String.class));
        query.addQueryParameters(params);
        final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
        List<List<Object>> result = searchResult.getResult();

        return result != null ? result : Collections.emptyList();
    }

    //TO_BE_CHECKED
    @Override
    public List<EyDmsCustomerModel> getAllRetailersForSubAreaTOP(List<SubAreaMasterModel> subAreas, BaseSiteModel site, String dealerCode) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {cs.eydmsCustomer} from {CustomerSubAreaMapping as cs left join DealerRetailerMap as dr on {cs.eydmsCustomer}={dr.retailer} and {dr.dealer}=?dealer} where {cs.subAreaMaster} in (?subAreas) AND {isActive} = ?active AND {brand} = ?brand and {cs.eydmsCustomer} is not null and {cs.isOtherBrand}=?isOtherBrand order by {dr.orderCount} desc");
        boolean active = Boolean.TRUE;
        params.put("subAreas", subAreas);
        params.put("active", active);
        params.put("brand", site);
        params.put("dealer", userService.getUserForUID(dealerCode));
        params.put(CustomerSubAreaMappingModel.ISOTHERBRAND, Boolean.FALSE);

        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
        query.addQueryParameters(params);
        final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
        List<EyDmsCustomerModel> result = searchResult.getResult();

        return result != null && !result.isEmpty() ? result : Collections.emptyList();

    }

    //USEFULL
    @Override
    public List<String> getAllStatesForSO(UserModel eydmsUser, BaseSiteModel site) {
        List<String> result = new ArrayList<String>();
        result.add(((B2BCustomerModel) eydmsUser).getState());
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }


    //USEFULL
    @Override
    public EyDmsUserModel getSOForSubArea(EyDmsCustomerModel eydmsCustomer) {
        final Map<String, Object> params = new HashMap<String, Object>();

        final StringBuilder builder = new StringBuilder("SELECT {u:eydmsUser} FROM {CustomerSubAreaMapping as c JOIN UserSubAreaMapping as u on {u:subAreaMaster}={c:subAreaMaster} and {u:brand}={c:brand}} WHERE {c.eydmsCustomer} =?eydmsCustomer and {u.isActive}=?active");
        boolean active = Boolean.TRUE;
        params.put("eydmsCustomer", eydmsCustomer);
        params.put("active", active);

        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsUserModel.class));
        query.addQueryParameters(params);
        final SearchResult<EyDmsUserModel> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult() != null && !searchResult.getResult().isEmpty() ? searchResult.getResult().get(0) : null;
    }

    //TO_BE_CHECKED
    @Override
    public EyDmsUserModel getEyDmsUserForSubArea(String subArea, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {eydmsUser} FROM {UserSubAreaMapping} WHERE {subArea}=?subArea AND {brand}=?brand AND {isActive}=?active");
        boolean active = Boolean.TRUE;
        params.put("subArea", subArea);
        params.put("brand", brand);
        params.put("active", active);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
        query.addQueryParameters(params);
        final SearchResult<EyDmsUserModel> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult() != null && !searchResult.getResult().isEmpty() ? searchResult.getResult().get(0) : null;
    }


    //USEFULL
    @Override
    public List<EyDmsUserModel> getAllSalesOfficersByState(String state, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT distinct {us.eydmsUser} FROM {UserSubAreaMapping as us join EyDmsUser as u on {us.eydmsUser}={u.pk}} WHERE {u.state}=?state AND {us.brand}=?brand AND {us.isActive}=?active");
        boolean active = Boolean.TRUE;
        params.put("state", state);
        params.put("brand", brand);
        params.put("active", active);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsUserModel.class));
        query.addQueryParameters(params);
        final SearchResult<EyDmsUserModel> searchResult = flexibleSearchService.search(query);
        List<EyDmsUserModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SearchRestrictionService searchRestrictionService;

    //USEFULL
    @SuppressWarnings("unchecked")
    @Override
    public List<SubAreaMasterModel> getTerritoriesForSOInLocalView() {
        return (List<SubAreaMasterModel>) sessionService.executeInLocalView(new SessionExecutionBody() {
            @Override
            public List<SubAreaMasterModel> execute() {
                try {
                    searchRestrictionService.disableSearchRestrictions();
                    final Map<String, Object> params = new HashMap<String, Object>();
                    final StringBuilder builder = new StringBuilder("SELECT {subAreaMaster} FROM {UserSubAreaMapping} WHERE {eydmsUser} = ?eydmsUser AND {isActive} = ?active AND {brand} = ?brand");
                    params.put("eydmsUser", userService.getCurrentUser());
                    params.put("active", Boolean.TRUE);
                    params.put("brand", baseSiteService.getCurrentBaseSite());
                    FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
                    query.setResultClassList(Arrays.asList(SubAreaMasterModel.class));
                    query.addQueryParameters(params);
                    final SearchResult<SubAreaMasterModel> searchResult = flexibleSearchService.search(query);
                    List<SubAreaMasterModel> result = searchResult.getResult();
                    return result != null && !result.isEmpty() ? result : Collections.emptyList();
                } finally {
                    searchRestrictionService.enableSearchRestrictions();
                }
            }
        });
    }

    //USEFULL
    @SuppressWarnings("unchecked")
    @Override
    public List<EyDmsCustomerModel> getAllCustomerForTerritoriesInLocalView(List<SubAreaMasterModel> subAreaMaster) {
        return (List<EyDmsCustomerModel>) sessionService.executeInLocalView(new SessionExecutionBody() {
            @Override
            public List<EyDmsCustomerModel> execute() {
                try {
                    searchRestrictionService.disableSearchRestrictions();
                    final Map<String, Object> params = new HashMap<String, Object>();
                    final StringBuilder builder = new StringBuilder("SELECT {eydmsCustomer} FROM {CustomerSubAreaMapping} WHERE {subAreaMaster} in (?subAreaMaster) AND {isActive} = ?active AND {brand} = ?brand and {eydmsCustomer} is not null ");
                    params.put("subAreaMaster", subAreaMaster);
                    params.put("active", Boolean.TRUE);
                    params.put("brand", baseSiteService.getCurrentBaseSite());
//    				params.put(CustomerSubAreaMappingModel.ISOTHERBRAND, Boolean.FALSE);
                    FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
                    query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
                    query.addQueryParameters(params);
                    final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
                    List<EyDmsCustomerModel> result = searchResult.getResult();
                    return result != null && !result.isEmpty() ? result : Collections.emptyList();
                } finally {
                    searchRestrictionService.enableSearchRestrictions();
                }
            }
        });
    }

    //USEFULL
    @Override
    public SubAreaMasterModel getTerritoryByIdInLocalView(String territoryId) {

        return (SubAreaMasterModel) sessionService.executeInLocalView(new SessionExecutionBody() {
            @Override
            public SubAreaMasterModel execute() {
                try {
                    searchRestrictionService.disableSearchRestrictions();
                    final Map<String, Object> params = new HashMap<String, Object>();
                    final String queryString = "SELECT {pk} FROM {SubAreaMaster} WHERE {pk} = ?territoryId ";
                    params.put("territoryId", territoryId);

                    FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
                    query.addQueryParameters(params);
                    final SearchResult<SubAreaMasterModel> searchResult = flexibleSearchService.search(query);
                    if (CollectionUtils.isNotEmpty(searchResult.getResult())) {
                        return searchResult.getResult().get(0);
                    }
                    return null;
                } finally {
                    searchRestrictionService.enableSearchRestrictions();
                }
            }
        });
    }

    //USEFULL
    @Override
    public List<EyDmsUserModel> getEyDmsUsersForSubArea(List<SubAreaMasterModel> subAreaMasters) {
        final Map<String, Object> params = new HashMap<>();
        final String queryString = "SELECT {eydmsUser} FROM {UserSubAreaMapping} WHERE {subAreaMaster} in ( ?subAreas)";
        params.put("subAreas", subAreaMasters);
        FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<EyDmsUserModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null) {
            return searchResult.getResult();
        }
        return Collections.emptyList();
    }

    //USEFULL
    @Override
    public List<EyDmsCustomerModel> getRetailerListForDealer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel site) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {d.retailer} from {DealerRetailerMap as d join EyDmsCustomer as c  on {d.retailer}={c.pk}} WHERE {d.dealer}=?eydmsCustomer AND {d.active}=?active AND {d.brand}=?brand");
        boolean active = Boolean.TRUE;
        params.put("eydmsCustomer", eydmsCustomer);
        params.put("active", active);
        params.put("brand", site);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
        query.addQueryParameters(params);
        final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult();
    }

    //USEFULL
    @Override
    public List<EyDmsCustomerModel> getInfluencerListForDealer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel site) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {d.influencer} from {DealerInfluencerMap as d join EyDmsCustomer as c  on {d.influencer}={c.pk}} WHERE {d.fromCustomer}=?eydmsCustomer AND {d.fromCustomerType}=?dealer  AND {d.active}=?active AND {d.brand}=?brand");
        String dealer = "Dealer";
        boolean active = Boolean.TRUE;
        params.put("eydmsCustomer", eydmsCustomer);
        params.put("dealer", dealer);
        params.put("active", active);
        params.put("brand", site);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
        query.addQueryParameters(params);
        final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult();
    }

    //USEFULL
    @Override
    public List<EyDmsCustomerModel> getInfluencerListForRetailer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel site) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {influencer} from {RetailerInfluencerMap} WHERE {fromCustomer}=?eydmsCustomer AND {fromCustomerType}=?retailer AND {active}=?active AND {brand}=?brand");
        params.put("eydmsCustomer", eydmsCustomer);
        String retailer = "Retailer";
        boolean active = Boolean.TRUE;
        params.put("eydmsCustomer", eydmsCustomer);
        params.put("active", active);
        params.put("brand", site);
        params.put("retailer", retailer);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
        query.addQueryParameters(params);
        final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult();
    }

    //USEFULL
    @Override
    public SearchPageData<EyDmsCustomerModel> getRetailerListForDealerPagination(SearchPageData searchPageData, EyDmsCustomerModel eydmsCustomer, BaseSiteModel site, String networkType, boolean isNew, String filter, Boolean isTop) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {d.retailer} from {DealerRetailerMap as d join EyDmsCustomer as c  on {d.retailer}={c.pk}} WHERE {d.dealer}=?eydmsCustomer AND {d.brand}=?brand ");
        if(isTop!=null && isTop) {
            builder.append(" AND {d.topActive}=?active ");
        }
        else {
            builder.append(" AND {d.active}=?active ");
        }
        boolean active = Boolean.TRUE;
        params.put("eydmsCustomer", eydmsCustomer);
        params.put("active", active);
        params.put("brand", site);
        appendFilterQuery(builder, params, networkType, isNew, filter, null, null);
        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
        query.getQueryParameters().putAll(params);
        parameter.setFlexibleSearchQuery(query);
        return paginatedFlexibleSearchService.search(parameter);
    }

    //USEFULL
    @Override
    public SearchPageData<EyDmsCustomerModel> getInfluencerListForDealerPagination(SearchPageData searchPageData, EyDmsCustomerModel eydmsCustomer, BaseSiteModel site, String networkType, boolean isNew, String filter, String influencerType, String dealerCategory) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {d.influencer} from {DealerInfluencerMap as d join EyDmsCustomer as c  on {d.influencer}={c.pk}} WHERE {d.fromCustomer}=?eydmsCustomer AND {d.fromCustomerType}=?dealer  AND {d.active}=?active AND {d.brand}=?brand");
        boolean active = Boolean.TRUE;
        String dealer = "Dealer";
        params.put("eydmsCustomer", eydmsCustomer);
        params.put("active", active);
        params.put("brand", site);
        params.put("dealer", dealer);
        appendFilterQuery(builder, params, networkType, isNew, filter, influencerType, dealerCategory);
        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
        query.getQueryParameters().putAll(params);
        parameter.setFlexibleSearchQuery(query);
        return paginatedFlexibleSearchService.search(parameter);
    }

    //USEFULL
    @Override
    public SearchPageData<EyDmsCustomerModel> getInfluencerListForRetailerPagination(SearchPageData searchPageData, EyDmsCustomerModel eydmsCustomer, BaseSiteModel site, String networkType, boolean isNew, String filter, String influencerType, String dealerCategory) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {d.influencer} from {RetailerInfluencerMap as d join EyDmsCustomer as c  on {d.influencer}={c.pk}} WHERE {d.fromCustomer}=?eydmsCustomer AND {d.fromCustomerType}=?retailer AND {d.active}=?active AND {d.brand}=?brand");
        boolean active = Boolean.TRUE;
        String retailer = "Retailer";
        params.put("eydmsCustomer", eydmsCustomer);
        params.put("active", active);
        params.put("brand", site);
        params.put("retailer", retailer);
        appendFilterQuery(builder, params, networkType, isNew, filter, influencerType, dealerCategory);
        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
        query.getQueryParameters().putAll(params);
        parameter.setFlexibleSearchQuery(query);
        return paginatedFlexibleSearchService.search(parameter);
    }


    //USEFULL
    @Override
    public List<SubAreaMasterModel> getTerritoriesForSO(EyDmsUserModel user) {
        final Map<String, Object> params = new HashMap<>();
        String builder = "SELECT {subAreaMaster} FROM {UserSubAreaMapping} WHERE {eydmsUser} = ?eydmsUser AND {isActive} = 1 AND {brand} = ?brand";
        params.put("eydmsUser", user);
        params.put("brand", baseSiteService.getCurrentBaseSite());
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder);
        query.setResultClassList(List.of(SubAreaMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<SubAreaMasterModel> searchResult = flexibleSearchService.search(query);
        List<SubAreaMasterModel> result = searchResult.getResult();
        if (CollectionUtils.isNotEmpty(result)) {
            return result;
        }
        return Collections.emptyList();
    }

    //USEFULL
    @Override
    public List<SubAreaMasterModel> getTerritoriesForCustomer(UserModel customer) {

        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {subAreaMaster} FROM {CustomerSubAreaMapping} WHERE {eydmsCustomer} = ?customer AND {isActive} = ?active AND {brand} = ?brand and {eydmsCustomer} is not null ");
        params.put("customer", customer);
        params.put("active", Boolean.TRUE);
        params.put("brand", baseSiteService.getCurrentBaseSite());
      //  params.put(CustomerSubAreaMappingModel.ISOTHERBRAND, Boolean.FALSE);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(SubAreaMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<SubAreaMasterModel> searchResult = flexibleSearchService.search(query);
        List<SubAreaMasterModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    //USEFULL
    @Override
    public List<SubAreaMasterModel> getTerritoriesForCustomerIncludingNonActive(UserModel customer) {

        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {subAreaMaster} FROM {CustomerSubAreaMapping} WHERE {eydmsCustomer} = ?customer ");
        params.put("customer", customer);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(SubAreaMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<SubAreaMasterModel> searchResult = flexibleSearchService.search(query);
        List<SubAreaMasterModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    //TO_BE_CHECKED
    @Override
    public List<SubAreaMasterModel> getTerritoriesForPromoter(UserModel customer) {
        String queryString = "Select {dsm.subAreaMaster} from {DepotSubAreaMapping as dsm join Warehouse as w on {dsm.depot}={w.pk}}";
        String whereCondition = " where {w.user}=?user and {dsm.active}=?active and {dsm.brand}=?brand";
        final Map<String, Object> params = new HashMap<>();
        params.put("user", customer);
        params.put("active", Boolean.TRUE);
        params.put("brand", baseSiteService.getCurrentBaseSite());
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString + whereCondition);
        query.addQueryParameters(params);
        final SearchResult<SubAreaMasterModel> searchResult = flexibleSearchService.search(query);
        if (CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();
    }

    //USEFULL
    @Override
    public SubAreaMasterModel getTerritoryById(String territoryId) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final String queryString = "SELECT {pk} FROM {SubAreaMaster} WHERE {pk} = ?territoryId ";
        params.put("territoryId", territoryId);

        FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<SubAreaMasterModel> searchResult = flexibleSearchService.search(query);
        if (CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult().get(0);
        }
        return null;
    }

    //TO_BE_CHECKED
    @Override
    public List<EyDmsCustomerModel> getEYDMSAndNonEYDMSCustomerForSubArea(List<SubAreaMasterModel> subAreaMaster) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {eydmsCustomer} FROM {CustomerSubAreaMapping} WHERE {subAreaMaster} in (?subAreaMaster) AND {isActive} = ?active AND {brand} = ?brand ");
        boolean active = Boolean.TRUE;
        params.put("subAreaMaster", subAreaMaster);
        params.put("active", active);
        params.put("brand", baseSiteService.getCurrentBaseSite());
        builder.append(" order by {creationTime} DESC ");
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
        query.addQueryParameters(params);
        final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
        List<EyDmsCustomerModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    //USEFULL
    @Override
    public SubAreaMasterModel getTerritoryByDistrictAndTaluka(String district, String taluka) {
        return (SubAreaMasterModel) sessionService.executeInLocalView(new SessionExecutionBody() {
            @Override
            public SubAreaMasterModel execute() {
                try {
                    searchRestrictionService.disableSearchRestrictions();
                    final Map<String, Object> params = new HashMap<String, Object>();
                    final String queryString = "SELECT {pk} FROM {SubAreaMaster} WHERE {district} = ?district and {taluka} = ?taluka ";
                    params.put("district", district);
                    params.put("taluka", taluka);
                    FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
                    query.addQueryParameters(params);
                    final SearchResult<SubAreaMasterModel> searchResult = flexibleSearchService.search(query);
                    if (CollectionUtils.isNotEmpty(searchResult.getResult())) {
                        return searchResult.getResult().get(0);
                    }
                    return null;

                } finally {
                    searchRestrictionService.enableSearchRestrictions();
                }
            }
        });

    }


    private void appendFilterQuery(StringBuilder builder, Map<String, Object> params, String networkType, boolean isNew, String filter, String influencerType, String dealerCategory) {
        if (StringUtils.isNotBlank(networkType)) {
            builder.append(" and {c.networkType}=?networkType ");
            params.put("networkType", networkType);
        }
        if (StringUtils.isNotBlank(filter)) {
            builder.append(" and (UPPER({c.name}) like (?filter) or {c.uid} like (?filter) or {c.customerNo} like (?filter) or {c.mobileNumber} like (?filter) )");
            params.put("filter", "%" + filter.toUpperCase() + "%");
        }
        if (StringUtils.isNotBlank(influencerType)) {
            builder.append(" and {c.influencerType}=?influencerType ");
            params.put("influencerType", InfluencerType.valueOf(influencerType));
        }
        if (Objects.nonNull(dealerCategory)) {
            builder.append(" and {c.dealerCategory}=?dealerCategory ");
            params.put("dealerCategory", DealerCategory.valueOf(dealerCategory));
        }
        if (isNew) {
            LocalDate lastNinetyDay = LocalDate.now().minusDays(90);
            builder.append(" and {c.dateOfJoining}>=?lastNinetyDay ");
            params.put("lastNinetyDay", lastNinetyDay.toString());
        }
    }

    //USEFULL
    @Override
    public Integer getDealerCountForRetailer(EyDmsCustomerModel currentUser, BaseSiteModel currentSite) {
        final Map<String, Object> attr = new HashMap<>();
        boolean active = Boolean.TRUE;
        attr.put("currentUser", currentUser);
        attr.put("currentSite", currentSite);
        attr.put("active", active);

        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(DISTINCT{d.dealer}) FROM {DealerRetailerMap as d} WHERE {d.retailer}=?currentUser AND {d.brand}=?currentSite AND {d.active}=?active");

        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Integer.class));
        query.getQueryParameters().putAll(attr);

        final SearchResult<Integer> result = getFlexibleSearchService().search(query);

        return result.getResult().get(0);

    }

    //USEFULL
    @Override
    public Integer getRetailerCountForDealer(EyDmsCustomerModel currentUser, BaseSiteModel currentSite) {
        final Map<String, Object> attr = new HashMap<>();
        boolean active = Boolean.TRUE;
        attr.put("currentUser", currentUser);
        attr.put("currentSite", currentSite);
        attr.put("active", active);

        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(DISTINCT{d.retailer}) FROM {DealerRetailerMap as d} WHERE {d.dealer}=?currentUser AND {d.brand}=?currentSite AND {d.active}=?active");

        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Integer.class));
        query.getQueryParameters().putAll(attr);

        final SearchResult<Integer> result = getFlexibleSearchService().search(query);

        return result.getResult().get(0);
    }

    //USEFULL
    @Override
    public Integer getInfluencerCountForDealer(EyDmsCustomerModel currentUser, BaseSiteModel currentSite) {
        final Map<String, Object> attr = new HashMap<>();
        boolean active = Boolean.TRUE;
        attr.put("currentUser", currentUser);
        attr.put("currentSite", currentSite);
        attr.put("active", active);

        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(DISTINCT{d.influencer}) FROM {DealerInfluencerMap as d} WHERE {d.fromCustomer}=?currentUser AND {d.brand}=?currentSite AND {d.active}=?active AND {d.fromCustomerType}= 'Dealer' ");

        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Integer.class));
        query.getQueryParameters().putAll(attr);

        final SearchResult<Integer> result = getFlexibleSearchService().search(query);

        return result.getResult().get(0);
    }

    //USEFULL
    @Override
    public SearchPageData<EyDmsCustomerModel> getDealerListForRetailerPagination(SearchPageData searchPageData, EyDmsCustomerModel currentUser, BaseSiteModel currentSite, String filter) {
        final Map<String, Object> attr = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select DISTINCT {d.dealer} from {DealerRetailerMap as d join EyDmsCustomer as c on {d.dealer}={c.pk}} where {d.retailer}=?currentUser AND {d.active}=?active AND {d.brand}=?currentSite ");
        boolean active = Boolean.TRUE;
        attr.put("currentUser", currentUser);
        attr.put("active", active);
        attr.put("currentSite", currentSite);
        appendFilterQuery(builder, attr, null, false, filter, null, null);
        builder.append(" order by {d.dealer}");
        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
        query.getQueryParameters().putAll(attr);
        parameter.setFlexibleSearchQuery(query);
        return paginatedFlexibleSearchService.search(parameter);
    }

    //USEFULL
    @Override
    public List<SubAreaMasterModel> getTalukaForUser(FilterTalukaData filterTalukaData) {
        final Map<String, Object> params = new HashMap<String, Object>();
        EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
        final StringBuilder builder = new StringBuilder();

        if (currentUser instanceof EyDmsUserModel) {
            if (currentUser.getUserType() != null) {
                if (currentUser.getUserType().getCode().equals("SO")) {
                    builder.append("select {s:pk} from {UserSubAreaMapping as u join SubAreaMaster as s on {u:subAreaMaster}={s:pk}}" +
                            " where {u:eydmsUser} = ?eydmsUser and {u:brand} = ?brand and {u:isActive} = ?active");
                    params.put("eydmsUser", currentUser);
                } else if (currentUser.getUserType().getCode().equals("TSM")) {
                    builder.append("select {s:pk} from {TsmDistrictMapping as tsm join DistrictMaster as d on {tsm:district}={d:pk}" +
                            " join SubAreaMaster as s on {d:name}={s:district}} where {tsm:tsmUser} = ?tsmUser and {tsm:brand} = ?brand" +
                            " and {tsm:isActive} = ?active");
                    params.put("tsmUser", currentUser);
                    if (filterTalukaData != null && !ObjectUtils.isEmpty(filterTalukaData) && filterTalukaData.getDistrictCode() != null && !filterTalukaData.getDistrictCode().isEmpty()) {
                        builder.append(" and {d:code} = ?districtCode");
                        params.put("districtCode", filterTalukaData.getDistrictCode());
                    }
                } else if (currentUser.getUserType().getCode().equals("RH")) {
                    builder.append("select {s:pk} from {RhRegionMapping as rh join RegionMaster as r on {rh:region}={r:pk}" +
                            " join DistrictMaster as d on {d:region}={r:pk} join SubAreaMaster as s on {d:name}={s:district}}" +
                            " where {rh:rhUser} = ?rhUser and {rh:brand} = ?brand and {rh:isActive} = ?active");
                    params.put("rhUser", currentUser);
                    if (filterTalukaData != null && !ObjectUtils.isEmpty(filterTalukaData) && filterTalukaData.getRegionCode() != null && !filterTalukaData.getRegionCode().isEmpty()) {
                        builder.append(" and {r:code} = ?regionCode");
                        params.put("regionCode", filterTalukaData.getRegionCode());
                        if (filterTalukaData.getDistrictCode() != null && !ObjectUtils.isEmpty(filterTalukaData) && !filterTalukaData.getDistrictCode().isEmpty()) {
                            builder.append(" and {d:code} = ?districtCode");
                            params.put("districtCode", filterTalukaData.getDistrictCode());
                        }
                    }
                } else if (currentUser.getUserType().equals(EyDmsUserType.TSO)) {
                    builder.append("select {s:pk} from {TsoTalukaMapping as t join SubAreaMaster as s on {t:subAreaMaster}={s:pk}}" +
                            " where {t:tsoUser} = ?eydmsUser and  {t:isActive} = ?active");
                    params.put("eydmsUser", currentUser);

                }

                if (filterTalukaData != null && !ObjectUtils.isEmpty(filterTalukaData) && filterTalukaData.getTalukaName() != null && !filterTalukaData.getTalukaName().isEmpty()) {
                    builder.append(" and {s:taluka} like ?talukaName");
                    params.put("talukaName", filterTalukaData.getTalukaName().toUpperCase() + "%");
                }
                builder.append(" order by {s:pk}");
                params.put("active", Boolean.TRUE);
                params.put("brand", baseSiteService.getCurrentBaseSite());

            } else {
                throw new UnknownIdentifierException("User Type not set for the user");
            }

        }
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(SubAreaMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<SubAreaMasterModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !searchResult.getResult().isEmpty()) {
            return searchResult.getResult();
        } else {
            return Collections.emptyList();
        }
    }

    //USEFULL
    @Override
    public List<DistrictMasterModel> getDistrictForUser(FilterDistrictData filterDistrictData) {
        final Map<String, Object> params = new HashMap<String, Object>();
        EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
        final StringBuilder builder = new StringBuilder();

        if (currentUser instanceof EyDmsUserModel) {
            if (currentUser.getUserType().getCode().equals("SO")) {
                builder.append("select distinct {d:pk} from {UserSubAreaMapping as u join SubAreaMaster as s on {u:subAreaMaster}={s:pk}" +
                        " join DistrictMaster as d on {d:name}={s:district}} where {u:eydmsUser} = ?eydmsUser and {u:brand} = ?brand and {u:isActive} = ?active");
                params.put("eydmsUser", userService.getCurrentUser());

            } else if (currentUser.getUserType().getCode().equals("TSM")) {
                builder.append("select {d:pk} from {TsmDistrictMapping as tsm join DistrictMaster as d on {tsm:district}={d:pk}}" +
                        " where {tsm:tsmUser} = ?tsmUser and {tsm:brand} = ?brand and {tsm:isActive} = ?active");
                params.put("tsmUser", currentUser);
            } else if (currentUser.getUserType().getCode().equals("RH")) {
                builder.append("select {d:pk} from {RhRegionMapping as rh join RegionMaster as r on {rh:region}={r:pk}" + " join DistrictMaster as d on {d:region}={r:pk}} where {rh:rhUser} = ?rhUser and {rh:brand} = ?brand and {rh:isActive} = ?active");
                params.put("rhUser", currentUser);
                if (filterDistrictData != null && !ObjectUtils.isEmpty(filterDistrictData) && filterDistrictData.getRegionCode() != null && !filterDistrictData.getRegionCode().isEmpty()) {
                    builder.append(" and {r:code} = ?regionCode");
                    params.put("regionCode", filterDistrictData.getRegionCode());
                }

            }
            if (filterDistrictData != null && !ObjectUtils.isEmpty(filterDistrictData) && filterDistrictData.getDistrictName() != null && !filterDistrictData.getDistrictName().isEmpty()) {
                builder.append(" and {d:name} like ?districtName");
                params.put("districtName", filterDistrictData.getDistrictName().toUpperCase() + "%");
            }
            builder.append(" order by {d:pk}");
            params.put("active", Boolean.TRUE);
            params.put("brand", baseSiteService.getCurrentBaseSite());
        }
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(DistrictMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<DistrictMasterModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !searchResult.getResult().isEmpty()) {
            return searchResult.getResult();
        } else {
            return Collections.emptyList();
        }
    }

    //USEFULL
    @Override
    public List<EyDmsCustomerModel> getCustomerForUser(RequestCustomerData requestCustomerData, List<SubAreaMasterModel> subAreaMasterList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        String strQuery = getCustomerForUserQuery(requestCustomerData, subAreaMasterList, params);
        FlexibleSearchQuery query = new FlexibleSearchQuery(strQuery);
        query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
        query.addQueryParameters(params);
        final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
        List<EyDmsCustomerModel> result = searchResult.getResult();

        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    //USEFULL
    @Override
    public SearchPageData<EyDmsCustomerModel> getPaginatedCustomerForUser(SearchPageData searchPageData, RequestCustomerData requestCustomerData, List<SubAreaMasterModel> subAreaMasterList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        String strQuery = getCustomerForUserQuery(requestCustomerData, subAreaMasterList, params);
        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(strQuery);
        query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
        query.getQueryParameters().putAll(params);
        parameter.setFlexibleSearchQuery(query);
        return paginatedFlexibleSearchService.search(parameter);
    }

    private String getCustomerForUserQuery(RequestCustomerData requestCustomerData, List<SubAreaMasterModel> subAreaMasterList, Map<String, Object> params) {
        final StringBuilder builder = new StringBuilder("SELECT {c.pk} FROM {").append(CUSTOMER_FOR_USER_QUERY).append("} WHERE {m.subAreaMaster} in (?subAreaList) ");
        params.put("subAreaList", subAreaMasterList);
        appendFilterQuery(builder, params, requestCustomerData);
        return builder.toString();
    }

    public void appendFilterQuery(StringBuilder builder, Map<String, Object> params, RequestCustomerData requestCustomerData) {
        if (requestCustomerData.getCounterType() != null && !requestCustomerData.getCounterType().isEmpty()) {
            List<CounterType> counterTypes = new ArrayList<CounterType>();
            for (String counterType : requestCustomerData.getCounterType()) {
                if (StringUtils.isNotBlank(counterType) && CounterType.valueOf(counterType) != null) {
                    counterTypes.add(CounterType.valueOf(counterType));
                }
            }
            if (!counterTypes.isEmpty()) {
                builder.append("and {c.counterType} in (?counterTypes) ");
                params.put("counterTypes", counterTypes);
            }
        }
        if (StringUtils.isNotBlank(requestCustomerData.getNetworkType())) {
            builder.append("and {c.networkType}=?networkType ");
            params.put("networkType", requestCustomerData.getNetworkType());
        }
        if (StringUtils.isNotBlank(requestCustomerData.getSearchKey())) {
            builder.append("and (UPPER({c.name}) like (?filter) or {c.uid} like (?filter) or {c.customerNo} like (?filter) or {c.mobileNumber} like (?filter) ) ");
            params.put("filter", "%" + requestCustomerData.getSearchKey().toUpperCase() + "%");
        }
        if (StringUtils.isNotBlank(requestCustomerData.getInfluencerType())) {
            builder.append("and {c.influencerType}=?influencerType ");
            params.put("influencerType", InfluencerType.valueOf(requestCustomerData.getInfluencerType()));
        }
        if (StringUtils.isNotBlank(requestCustomerData.getCustomerUid())) {
            builder.append("and {c.uid}=?uid ");
            params.put("uid", requestCustomerData.getCustomerUid());
        }
        if (requestCustomerData.getRemoveFlaggedCustomer() != null && requestCustomerData.getRemoveFlaggedCustomer()) {
//            builder.append("and {c.isDealerFlag}=?isDealerFlag ");
//            params.put("isDealerFlag", Boolean.FALSE);
        }
        if (Objects.nonNull(requestCustomerData.getDealerCategory())) {
            builder.append("and {c.dealerCategory}=?dealerCategory ");
            params.put("dealerCategory", DealerCategory.valueOf(requestCustomerData.getDealerCategory()));
        }
        if (requestCustomerData.getIsNew() != null && requestCustomerData.getIsNew()) {
            LocalDate lastNinetyDay = LocalDate.now().minusDays(90);
            builder.append("and {c.dateOfJoining}>=?lastNinetyDay ");
            params.put("lastNinetyDay", lastNinetyDay.toString());
        }
        if (requestCustomerData.getIncludeNonEyDmsCustomer() == null || !requestCustomerData.getIncludeNonEyDmsCustomer()) {
            builder.append("and {m.isOtherBrand}=?isOtherBrand ");
            params.put(CustomerSubAreaMappingModel.ISOTHERBRAND, Boolean.FALSE);
        }
        if (requestCustomerData.getIsFlag() != null && requestCustomerData.getIsFlag().equals(Boolean.TRUE)) {
            builder.append("and {c.isDealerFlag}=?isDealerFlag ");
            params.put("isDealerFlag", Boolean.TRUE);
        }
        if (requestCustomerData.getIsUnFlag() != null && requestCustomerData.getIsUnFlag().equals(Boolean.TRUE)) {
            builder.append("and {c.isUnFlagRequestRaised}=?isUnFlagRequestRaised ");
            params.put("isUnFlagRequestRaised", Boolean.TRUE);
        }
        if (requestCustomerData.getDistrict() != null) {
            builder.append("and {m.district}=?district ");
            params.put("district", requestCustomerData.getDistrict());
        }
        if (requestCustomerData.getBrand() != null) {
            BaseSiteModel baseSiteForUID = baseSiteService.getBaseSiteForUID(requestCustomerData.getBrand());
            builder.append("and {m.brand}=?baseSiteForUID ");
            params.put("baseSiteForUID", baseSiteForUID);
        }
    }

    //USEFULL
    @Override
    public List<RegionMasterModel> getRegionsForRH(FilterRegionData filterRegionData) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("Select distinct {r.pk} from {RhRegionMapping as rh join RegionMaster as r on {rh.region}={r.pk}} where ");
        if (StringUtils.isNotBlank(filterRegionData.getSearchKey())) {
            builder.append("{r.name} like ?searchKey");
            params.put("searchKey", "%" + filterRegionData.getSearchKey() + "%");
        }
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(RegionMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<RegionMasterModel> searchResult = flexibleSearchService.search(query);
        List<RegionMasterModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();

    }

    //USEFULL
    @Override
    public List<RegionMasterModel> getRegionsForTSM(FilterRegionData filterRegionData) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("Select distinct {r.pk} from {TsmDistrictMapping as t join DistrictMaster as d on {t.district}={d.pk} join RegionMaster as r on {d.region}={r.pk}} where ");
        if (StringUtils.isNotBlank(filterRegionData.getSearchKey())) {
            builder.append("{r.name} like ?searchKey");
            params.put("searchKey", "%" + filterRegionData.getSearchKey() + "%");
        }
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(RegionMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<RegionMasterModel> searchResult = flexibleSearchService.search(query);
        List<RegionMasterModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    //USEFULL
    @Override
    public List<RegionMasterModel> getRegionsForSO(FilterRegionData filterRegionData) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("Select distinct {r.pk} from {UserSubAreaMapping as u join SubAreaMaster as s on {u.subAreaMaster}={s.pk} join DistrictMaster as d on {s.district}={d.name} join RegionMaster as r on {d.region}={r.pk}} where ");
        if (StringUtils.isNotBlank(filterRegionData.getSearchKey())) {
            builder.append("{r.name} like ?searchKey");
            params.put("searchKey", "%" + filterRegionData.getSearchKey() + "%");
        }
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(RegionMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<RegionMasterModel> searchResult = flexibleSearchService.search(query);
        List<RegionMasterModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    //USEFULL
    @Override
    public Integer getInfluencerCountForRetailer(EyDmsCustomerModel currentUser, BaseSiteModel currentSite) {
        final Map<String, Object> attr = new HashMap<>();
        boolean active = Boolean.TRUE;
        attr.put("currentUser", currentUser);
        attr.put("currentSite", currentSite);
        attr.put("active", active);

        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(DISTINCT{r.influencer}) FROM {RetailerInfluencerMap as r} WHERE {r.fromCustomer}=?currentUser AND {r.brand}=?currentSite AND {r.active}=?active AND {r.fromCustomerType}= 'Retailer' ");

        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Integer.class));
        query.getQueryParameters().putAll(attr);

        final SearchResult<Integer> result = getFlexibleSearchService().search(query);

        return result.getResult().get(0);
    }

    //USEFULL
    @Override
    public List<UserSubAreaMappingModel> getSOSubAreaMappingForUser(FilterTalukaData filterTalukaData) {
        {
            final Map<String, Object> params = new HashMap<String, Object>();
            EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
            final StringBuilder builder = new StringBuilder();

            if (currentUser instanceof EyDmsUserModel) {
                if (currentUser.getUserType().getCode().equals("TSM")) {
                    builder.append("select {u:pk} from {TsmDistrictMapping as tsm join DistrictMaster as d on {tsm:district}={d:pk} " +
                            " join SubAreaMaster as s on {d:name}={s:district} " +
                            " join UserSubAreaMapping as u on {u.subareaMaster}={s.pk}} " +
                            " where {tsm:tsmUser} = ?tsmUser and {tsm:brand} = ?brand and {tsm:isActive} = ?active and {u.brand}= ?brand and {u.isActive}=?active  ");

                    params.put("tsmUser", currentUser);
                    if (filterTalukaData != null && !ObjectUtils.isEmpty(filterTalukaData) && filterTalukaData.getDistrictCode() != null && !filterTalukaData.getDistrictCode().isEmpty()) {
                        builder.append(" and {d:code} = ?districtCode");
                        params.put("districtCode", filterTalukaData.getDistrictCode());
                    }
                } else if (currentUser.getUserType().getCode().equals("RH")) {
                    builder.append("select {u:pk} from {RhRegionMapping as rh join RegionMaster as r on {rh:region}={r:pk} " +
                            " join DistrictMaster as d on {d:region}={r:pk} " +
                            " join SubAreaMaster as s on {d:name}={s:district} " +
                            " join UserSubAreaMapping as u on {u.subareaMaster}={s.pk}} " +
                            " where {rh:rhUser} = ?rhUser and {rh:brand} = ?brand and {rh:isActive} = ?active and {u.brand}= ?brand and {u.isActive}=?active ");
                    params.put("rhUser", currentUser);
                    if (filterTalukaData != null && !ObjectUtils.isEmpty(filterTalukaData) && filterTalukaData.getRegionCode() != null && !filterTalukaData.getRegionCode().isEmpty()) {
                        builder.append(" and {r:code} = ?regionCode");
                        params.put("regionCode", filterTalukaData.getRegionCode());
                    }
                    if (filterTalukaData != null && filterTalukaData.getDistrictCode() != null && !ObjectUtils.isEmpty(filterTalukaData) && !filterTalukaData.getDistrictCode().isEmpty()) {
                        builder.append(" and {d:code} = ?districtCode");
                        params.put("districtCode", filterTalukaData.getDistrictCode());
                    }
                }

                if (filterTalukaData != null && !ObjectUtils.isEmpty(filterTalukaData) && filterTalukaData.getTalukaName() != null && !filterTalukaData.getTalukaName().isEmpty()) {
                    builder.append(" and {s:taluka} like ?talukaName");
                    params.put("talukaName", filterTalukaData.getTalukaName().toUpperCase() + "%");
                }
                builder.append(" order by {s:pk}");
                params.put("active", Boolean.TRUE);
                params.put("brand", baseSiteService.getCurrentBaseSite());
            }
            FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.setResultClassList(Collections.singletonList(UserSubAreaMappingModel.class));
            query.addQueryParameters(params);
            final SearchResult<UserSubAreaMappingModel> searchResult = flexibleSearchService.search(query);
            if (searchResult.getResult() != null && !searchResult.getResult().isEmpty()) {
                return searchResult.getResult();
            } else {
                return Collections.emptyList();
            }
        }
    }

    //USEFULL
    @Override
    public List<TsmDistrictMappingModel> getTSMDistrcitMappingForUser(FilterDistrictData filterDistrictData) {
        {
            final Map<String, Object> params = new HashMap<String, Object>();
            EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
            final StringBuilder builder = new StringBuilder();

            if (currentUser instanceof EyDmsUserModel) {
                if (currentUser.getUserType().getCode().equals("RH")) {
                    builder.append("select {t:pk} from {RhRegionMapping as rh join RegionMaster as r on {rh:region}={r:pk}" +
                            " join DistrictMaster as d on {d:region}={r:pk}" +
                            " join TsmDistrictMapping as t on {t.district}={d.pk}}" +
                            " where {rh:rhUser} = ?rhUser and {rh:brand} = ?brand and {rh:isActive} = ?active and {t.brand}=?brand and {t.isActive}=?active ");
                    params.put("rhUser", currentUser);
                    if (filterDistrictData != null && !ObjectUtils.isEmpty(filterDistrictData) && filterDistrictData.getRegionCode() != null && !filterDistrictData.getRegionCode().isEmpty()) {
                        builder.append(" and {r:code} = ?regionCode");
                        params.put("regionCode", filterDistrictData.getRegionCode());
                    }
                }
                if (filterDistrictData != null && !ObjectUtils.isEmpty(filterDistrictData) && filterDistrictData.getDistrictName() != null && !filterDistrictData.getDistrictName().isEmpty()) {
                    builder.append(" and {d:name} like ?districtName");
                    params.put("districtName", filterDistrictData.getDistrictName().toUpperCase() + "%");
                }
                builder.append(" order by {d:pk}");
                params.put("active", Boolean.TRUE);
                params.put("brand", baseSiteService.getCurrentBaseSite());
            }
            FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.setResultClassList(Collections.singletonList(TsmDistrictMappingModel.class));
            query.addQueryParameters(params);
            final SearchResult<TsmDistrictMappingModel> searchResult = flexibleSearchService.search(query);
            if (searchResult.getResult() != null && !searchResult.getResult().isEmpty()) {
                return searchResult.getResult();
            } else {
                return Collections.emptyList();
            }
        }
    }

    //USEFULL
    @Override
    public List<EyDmsCustomerModel> getDealersForSP(RequestCustomerData requestCustomerData) {
        final Map<String, Object> params = new HashMap<String, Object>();
        String strQuery = getDealersForSPQuery(requestCustomerData, params);

        FlexibleSearchQuery query = new FlexibleSearchQuery(strQuery);
        query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
        query.addQueryParameters(params);
        final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
        List<EyDmsCustomerModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    private String getDealersForSPQuery(RequestCustomerData requestCustomerData, Map<String, Object> params) {
        final StringBuilder builder = new StringBuilder("Select {c.pk} from {CustDepotDealerMapping as d join SpCustDepotMapping as s on {d.custDepotCode}={s.custDepotCode} join EyDmsCustomer as c on {c.pk}={d.dealerCode}}")
                .append(" where {s.spCode} = ?currentUser AND {s.brand} = ?brand AND {d.brand}= ?brand AND {s.active} = ?active ");
        params.put("active", Boolean.TRUE);
        params.put("brand", baseSiteService.getCurrentBaseSite());
        params.put("currentUser", userService.getCurrentUser());
        requestCustomerData.setIncludeNonEyDmsCustomer(true);
        appendFilterQuery(builder, params, requestCustomerData);
        return builder.toString();
    }

    //USEFULL
    @Override
    public SearchPageData<EyDmsCustomerModel> getPaginatedDealersForSP(SearchPageData searchPageData, RequestCustomerData requestCustomerData) {
        final Map<String, Object> params = new HashMap<String, Object>();
        String strQuery = getDealersForSPQuery(requestCustomerData, params);
        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(strQuery);
        query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
        query.getQueryParameters().putAll(params);
        parameter.setFlexibleSearchQuery(query);
        return paginatedFlexibleSearchService.search(parameter);
    }

    //USEFULL
    @Override
    public List<SubAreaMasterModel> getTalukaForSP(FilterTalukaData filterTalukaData) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("Select distinct{c.subAreaMaster} from {CustDepotDealerMapping as d join SpCustDepotMapping as s on {d.custDepotCode}={s.custDepotCode} join CustomerSubAreaMapping as c on {c.eydmsCustomer}={d.dealerCode} } Where ")
                .append(" {s.spCode} = ?currentUser AND {s.brand} = ?brand AND {d.brand}= ?brand AND {s.active} = ?active ");
        params.put("active", Boolean.TRUE);
        params.put("brand", baseSiteService.getCurrentBaseSite());
        params.put("currentUser", userService.getCurrentUser());
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(SubAreaMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<SubAreaMasterModel> searchResult = flexibleSearchService.search(query);
        List<SubAreaMasterModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();

    }

    //USEFULL
    @Override
    public List<EyDmsUserModel> getSOForSP(FilterTalukaData filterTalukaData) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("Select distinct{u.eydmsUser} from {CustDepotDealerMapping as d join SpCustDepotMapping as s on {d.custDepotCode}={s.custDepotCode} join CustomerSubAreaMapping as c on {c.eydmsCustomer}={d.dealerCode} join UserSubAreaMapping as u on {u.subAreaMaster}={c.subAreaMaster}} Where")
                .append(" {s.spCode} = ?currentUser AND {s.brand} = ?brand AND {d.brand}= ?brand AND {s.active} = ?active ");
        params.put("active", Boolean.TRUE);
        params.put("brand", baseSiteService.getCurrentBaseSite());
        params.put("currentUser", userService.getCurrentUser());
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsUserModel.class));
        query.addQueryParameters(params);
        final SearchResult<EyDmsUserModel> searchResult = flexibleSearchService.search(query);
        List<EyDmsUserModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();

    }

    //USEFULL
    @SuppressWarnings("unchecked")
    @Override
    public List<SubAreaMasterModel> getTalukaForUserInLocalView(B2BCustomerModel currentUser, FilterTalukaData filterTalukaData) {

        return (List<SubAreaMasterModel>) sessionService.executeInLocalView(new SessionExecutionBody() {
            @Override
            public List<SubAreaMasterModel> execute() {
                try {
                    searchRestrictionService.disableSearchRestrictions();
                    final Map<String, Object> params = new HashMap<String, Object>();
                    final StringBuilder builder = new StringBuilder();

                    if (currentUser instanceof EyDmsUserModel) {
                        if (currentUser.getUserType().getCode().equals("SO")) {
                            builder.append("select {s:pk} from {UserSubAreaMapping as u join SubAreaMaster as s on {u:subAreaMaster}={s:pk}}" +
                                    " where {u:eydmsUser} = ?eydmsUser and {u:brand} = ?brand and {u:isActive} = ?active");
                            params.put("eydmsUser", currentUser);
                        } else if (currentUser.getUserType().getCode().equals("TSM")) {
                            builder.append("select {s:pk} from {TsmDistrictMapping as tsm join DistrictMaster as d on {tsm:district}={d:pk}" +
                                    " join SubAreaMaster as s on {d:name}={s:district}} where {tsm:tsmUser} = ?tsmUser and {tsm:brand} = ?brand" +
                                    " and {tsm:isActive} = ?active");
                            params.put("tsmUser", currentUser);
                            if (filterTalukaData != null && !ObjectUtils.isEmpty(filterTalukaData) && filterTalukaData.getDistrictCode() != null && !filterTalukaData.getDistrictCode().isEmpty()) {
                                builder.append(" and {d:code} = ?districtCode");
                                params.put("districtCode", filterTalukaData.getDistrictCode());
                            }
                        } else if (currentUser.getUserType().getCode().equals("RH")) {
                            builder.append("select {s:pk} from {RhRegionMapping as rh join RegionMaster as r on {rh:region}={r:pk}" +
                                    " join DistrictMaster as d on {d:region}={r:pk} join SubAreaMaster as s on {d:name}={s:district}}" +
                                    " where {rh:rhUser} = ?rhUser and {rh:brand} = ?brand and {rh:isActive} = ?active");
                            params.put("rhUser", currentUser);
                            if (filterTalukaData != null && !ObjectUtils.isEmpty(filterTalukaData) && filterTalukaData.getRegionCode() != null && !filterTalukaData.getRegionCode().isEmpty()) {
                                builder.append(" and {r:code} = ?regionCode");
                                params.put("regionCode", filterTalukaData.getRegionCode());
                                if (filterTalukaData.getDistrictCode() != null && !ObjectUtils.isEmpty(filterTalukaData) && !filterTalukaData.getDistrictCode().isEmpty()) {
                                    builder.append(" and {d:code} = ?districtCode");
                                    params.put("districtCode", filterTalukaData.getDistrictCode());
                                }
                            }
                        }
						else if (currentUser.getUserType().equals(EyDmsUserType.TSO)) {
								builder.append("select {s:pk} from {TsoTalukaMapping as t join SubAreaMaster as s on {t:subAreaMaster}={s:pk}}" +
								" where {t:tsoUser} = ?eydmsUser and  {t:isActive} = ?active");
							params.put("eydmsUser", currentUser);

						}
                        if (filterTalukaData != null && !ObjectUtils.isEmpty(filterTalukaData) && filterTalukaData.getTalukaName() != null && !filterTalukaData.getTalukaName().isEmpty()) {
                            builder.append(" and {s:taluka} like ?talukaName");
                            params.put("talukaName", filterTalukaData.getTalukaName().toUpperCase() + "%");
                        }
                        builder.append(" order by {s:pk}");
                        params.put("active", Boolean.TRUE);
                        params.put("brand", baseSiteService.getCurrentBaseSite());
                    }
                    FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
                    query.setResultClassList(Collections.singletonList(SubAreaMasterModel.class));
                    query.addQueryParameters(params);
                    final SearchResult<SubAreaMasterModel> searchResult = flexibleSearchService.search(query);
                    if (searchResult.getResult() != null && !searchResult.getResult().isEmpty()) {
                        return searchResult.getResult();
                    } else {
                        return Collections.emptyList();
                    }
                } finally {
                    searchRestrictionService.enableSearchRestrictions();
                }
            }
        });
    }

    //USEFULL
    @Override
    public CustDepotMasterModel getCustDepotForCustomer(EyDmsCustomerModel customer) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {custDepotCode} FROM {CustDepotDealerMapping} WHERE {dealerCode} = ?customer AND {active} = ?active AND {brand} = ?brand ");
        params.put("customer", customer);
        params.put("active", Boolean.TRUE);
        params.put("brand", baseSiteService.getCurrentBaseSite());
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(CustDepotMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<CustDepotMasterModel> searchResult = flexibleSearchService.search(query);
        List<CustDepotMasterModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result.get(0) : null;
    }

    //USEFULL
    @Override
    public List<CustDepotMasterModel> getCustDepotForSP(B2BCustomerModel spCode) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {custDepotCode} FROM {SpCustDepotMapping} WHERE {spCode} = ?spCode AND {active} = ?active AND {brand} = ?brand ");
        params.put("spCode", spCode);
        params.put("active", Boolean.TRUE);
        params.put("brand", baseSiteService.getCurrentBaseSite());
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(CustDepotMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<CustDepotMasterModel> searchResult = flexibleSearchService.search(query);
        List<CustDepotMasterModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    //USEFULL
    @Override
    public CustDepotMasterModel getCustDepotForCode(String code) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {d:pk} FROM {CustDepotMaster as d} WHERE {d:code} = ?code ");
        params.put("code", code);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(CustDepotMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<CustDepotMasterModel> searchResult = flexibleSearchService.search(query);
        List<CustDepotMasterModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result.get(0) : null;
    }


    //USEFULL
    @SuppressWarnings("unchecked")
    @Override
    public List<EyDmsCustomerModel> getDealersForSPInLocalView(B2BCustomerModel userForUID) {
        return (List<EyDmsCustomerModel>) sessionService.executeInLocalView(new SessionExecutionBody() {
            @Override
            public List<EyDmsCustomerModel> execute() {
                try {
                    searchRestrictionService.disableSearchRestrictions();
                    UserModel currentUser = userService.getCurrentUser();
                    userService.setCurrentUser(userForUID);
                    RequestCustomerData customerData = new RequestCustomerData();
                    customerData.setCounterType(List.of("Dealer"));
                    List<EyDmsCustomerModel> list = getDealersForSP(customerData);
                    userService.setCurrentUser(currentUser);
                    return list;
                } finally {
                    searchRestrictionService.enableSearchRestrictions();
                }
            }
        });
    }

    //USEFULL
    @Override
    public SearchPageData<CreditAndOutstandingModel> getDealerOutstandingDetailsForTSMRH(SearchPageData searchPageData, List<EyDmsUserModel> soList, List<EyDmsUserModel> tsmList) {
        EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
        BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("isActive", Boolean.TRUE);
        params.put("brand", brand);
        params.put("currentUser", currentUser);
        final StringBuilder builder = new StringBuilder();
        if (currentUser != null) {
            if (currentUser.getUserType().getCode().equals("TSM")) {
                builder.append("Select {cao.pk} FROM {TsmDistrictMapping as tmap JOIN  DistrictMaster as d ON {d.pk}={tmap.district} JOIN SubAreaMaster as s  ON {s.districtMaster}={d.pk} JOIN CustomerSubAreaMapping AS cmap  ON {cmap.subAreaMaster}={s.pk}  JOIN EyDmsCustomer AS c ON {c.pk}={cmap.eydmsCustomer} join CreditAndOutstanding AS cao on {cao.customerCode}={c.customerNo} ");

                if (soList != null && !soList.isEmpty()) {
                    builder.append(" join UserSubAreaMapping as umap on {umap.subAreaMaster}={s.pk} ");
                    builder.append(" } where {tmap.tsmUser}=?currentUser and {cmap.brand}=?brand and {cmap.isActive}=?isActive and {umap.brand}=?brand and {umap.isActive}=?isActive and  {umap.eydmsUser} in (?soList) ");
                    params.put("soList", soList);
                } else {
                    builder.append(" } where {tmap.tsmUser}=?currentUser and {cmap.brand}=?brand and {cmap.isActive}=?isActive ");
                }

            } else if (currentUser.getUserType().getCode().equals("RH")) {
                builder.append("Select {cao.pk} FROM {RhRegionMapping as rhMap JOIN RegionMaster as r on {rhMap.region}={r.pk} JOIN  DistrictMaster as d ON {d.region}={r.pk} JOIN SubAreaMaster as s  ON {s.districtMaster}={d.pk} JOIN CustomerSubAreaMapping AS cmap  ON {cmap.subAreaMaster}={s.pk}  JOIN EyDmsCustomer AS c ON {c.pk}={cmap.eydmsCustomer} join CreditAndOutstanding AS cao on {cao.customerCode}={c.customerNo}  ");
                if ((soList != null && !soList.isEmpty()) && (tsmList == null && tsmList.isEmpty())) {
                    builder.append(" join UserSubAreaMapping as umap on {umap.subAreaMaster}={s.pk} ");
                    builder.append(" } where {rhMap.rhUser}='?currentUser and {cmap.brand}=?brand and {cmap.isActive}=?isActive and {umap.brand}=?brand and {umap.isActive}=?isActive and {umap.eydmsUser} in (?soList) ");
                    params.put("soList", soList);
                } else if ((soList == null && soList.isEmpty()) && (tsmList != null && !tsmList.isEmpty())) {
                    builder.append(" JOIN TsmDistrictMapping as tmap ON {d.pk}={tmap.district}  ");
                    builder.append(" } where {rhMap.rhUser}=?currentUser and {cmap.brand}=?brand and {cmap.isActive}=?isActive and {tmap.brand}=?brand and {tmap.isActive}=?isActive and {tmap.tsmUser} in (?tsmList) ");
                    params.put("tsmList", tsmList);
                } else if ((soList != null && !soList.isEmpty()) && (tsmList != null && !tsmList.isEmpty())) {
                    builder.append("  join UserSubAreaMapping as umap on {umap.subAreaMaster}={s.pk} JOIN TsmDistrictMapping as tmap ON {d.pk}={tmap.district}  ");
                    builder.append("  } where {rhMap.rhUser}=?currentUser and {cmap.brand}=?brand and {cmap.isActive}=?isActive and {umap.brand}=?brand and {umap.isActive}=?isActive and {umap.eydmsUser} in (?soList) and {tmap.brand}=?brand and {tmap.isActive}=?isActive  and {tmap.tsmUser} in (?tsmList) ");
                    params.put("soList", soList);
                    params.put("tsmList", tsmList);
                } else {
                    builder.append(" } where {rhMap.rhUser}=?currentUser and {cmap.brand}=?brand and {cmap.isActive}=?isActive");
                }
            }
        }

        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder);
        query.setResultClassList(Collections.singletonList(CreditAndOutstandingModel.class));
        query.getQueryParameters().putAll(params);
        parameter.setFlexibleSearchQuery(query);
        return paginatedFlexibleSearchService.search(parameter);

    }

    @Override
    public List<List<Object>> getOutstandingDataForTSMRH() {
        EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
        BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        params.put("currentUser", currentUser);
        params.put("brand", brand);
        params.put("isActive", Boolean.TRUE);

        if (currentUser != null && currentUser.getUserType() != null) {
            if (currentUser.getUserType().equals(EyDmsUserType.TSM)) {
                builder.append("Select sum({cao.totalOutstanding}),sum({cao.dailyAverageSales}) ");
                builder.append(" FROM {TsmDistrictMapping as tmap JOIN  DistrictMaster as d ON {d.pk}={tmap.district} JOIN SubAreaMaster as s  ON {s.districtMaster}={d.pk} JOIN CustomerSubAreaMapping AS cmap  ON {cmap.subAreaMaster}={s.pk}  JOIN EyDmsCustomer AS c ON {c.pk}={cmap.eydmsCustomer} join CreditAndOutstanding AS cao on {cao.customerCode}={c.customerNo}  } where {tmap.tsmUser}=?currentUser and {cmap.brand}=?brand and {cmap.isActive}=?isActive ");

            } else if (currentUser.getUserType().equals(EyDmsUserType.RH)) {
                builder.append("Select sum({cao.totalOutstanding}), sum({cao.dailyAverageSales})  ");
                builder.append(" FROM {RhRegionMapping as rhMap JOIN RegionMaster as r on {rhMap.region}={r.pk} JOIN  DistrictMaster as d ON {d.region}={r.pk} JOIN SubAreaMaster as s  ON {s.districtMaster}={d.pk} JOIN CustomerSubAreaMapping AS cmap  ON {cmap.subAreaMaster}={s.pk}  JOIN EyDmsCustomer AS c ON {c.pk}={cmap.eydmsCustomer} join CreditAndOutstanding AS cao on {cao.customerCode}={c.customerNo}  } where {rhMap.rhUser}=?currentUser and {cmap.brand}=?brand and {cmap.isActive}=?isActive ");
            }
        }

        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class, Double.class));
        query.addQueryParameters(params);
        SearchResult<List<Object>> result = flexibleSearchService.search(query);
        if (!result.getResult().isEmpty() && result.getResult() != null) {
            return result.getResult();
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public List<List<Double>> getBucketListForTSMRH() {
        EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
        BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        params.put("currentUser", currentUser);
        params.put("brand", brand);
        params.put("isActive", Boolean.TRUE);

        if (currentUser != null && currentUser.getUserType() != null) {
            if (currentUser.getUserType().equals(EyDmsUserType.TSM)) {
                builder.append("Select sum({cao.bucket1}),sum({cao.bucket2}),sum({cao.bucket3}),sum({cao.bucket4}),sum({cao.bucket5}),sum({cao.bucket6}),sum({cao.bucket7}),sum({cao.bucket8}),sum({cao.bucket9}),sum({cao.bucket10}) ");
                builder.append(" FROM {TsmDistrictMapping as tmap JOIN  DistrictMaster as d ON {d.pk}={tmap.district} JOIN SubAreaMaster as s  ON {s.districtMaster}={d.pk} JOIN CustomerSubAreaMapping AS cmap  ON {cmap.subAreaMaster}={s.pk}  JOIN EyDmsCustomer AS c ON {c.pk}={cmap.eydmsCustomer} join CreditAndOutstanding AS cao on {cao.customerCode}={c.customerNo}  } where {tmap.tsmUser}=?currentUser and {cmap.brand}=?brand and {cmap.isActive}=?isActive ");

            } else if (currentUser.getUserType().equals(EyDmsUserType.RH)) {
                builder.append("Select sum({cao.bucket1}),sum({cao.bucket2}),sum({cao.bucket3}),sum({cao.bucket4}),sum({cao.bucket5}),sum({cao.bucket6}),sum({cao.bucket7}),sum({cao.bucket8}),sum({cao.bucket9}),sum({cao.bucket10})  ");
                builder.append(" FROM {RhRegionMapping as rhMap JOIN RegionMaster as r on {rhMap.region}={r.pk} JOIN  DistrictMaster as d ON {d.region}={r.pk} JOIN SubAreaMaster as s  ON {s.districtMaster}={d.pk} JOIN CustomerSubAreaMapping AS cmap  ON {cmap.subAreaMaster}={s.pk}  JOIN EyDmsCustomer AS c ON {c.pk}={cmap.eydmsCustomer} join CreditAndOutstanding AS cao on {cao.customerCode}={c.customerNo}  } where {rhMap.rhUser}=?currentUser and {cmap.brand}=?brand and {cmap.isActive}=?isActive ");
            }
        }

        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class));
        query.addQueryParameters(params);
        SearchResult<List<Double>> result = flexibleSearchService.search(query);
        if (!result.getResult().isEmpty() && result.getResult() != null) {
            return result.getResult();
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    //TO_BE_CHECKED
    @Override
    public EyDmsUserModel getSalesOfficerforTaluka(String taluka, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT distinct{su.pk} FROM {UserSubAreaMapping as u  join SubAreaMaster as sm on {sm.pk}={u.subAreaMaster} JOIN EyDmsUser as su on {su.pk}={u.eydmsUser}} WHERE {sm.taluka}=?taluka AND {u.brand}=?brand AND {u.isActive}=?active");
        boolean active = Boolean.TRUE;
        params.put("taluka", taluka);
        params.put("brand", brand);
        params.put("active", active);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(EyDmsUserModel.class));
        query.addQueryParameters(params);
        final SearchResult<EyDmsUserModel> searchResult = flexibleSearchService.search(query);
        List<EyDmsUserModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result.get(0) : null;
    }

    //USEFULL
    @Override
    public List<DistrictMasterModel> getDistrictForSP(FilterDistrictData filterDistrictData) {
        final Map<String, Object> params = new HashMap<String, Object>();
        EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
        final StringBuilder builder = new StringBuilder();
        if (currentUser instanceof EyDmsCustomerModel) {
            builder.append(" SELECT distinct{dm.pk} FROM {CustDepotDealerMapping as d join SpCustDepotMapping as s on {d.custDepotCode}={s.custDepotCode}join CustomerSubAreaMapping as c on {c.eydmsCustomer}={d.dealerCode} join EyDmsCustomer as u on {c.eydmsCustomer}={u.pk} join SubAreaMaster as sm on {sm.pk}={c.subAreaMaster} JOIN DistrictMaster AS dm ON {dm.pk} = {sm.districtMaster}} where  {s.brand} = ?brand AND {s.active} = ?active");
            params.put("active", Boolean.TRUE);
            params.put("brand", baseSiteService.getCurrentBaseSite());

            if (filterDistrictData != null && !ObjectUtils.isEmpty(filterDistrictData) && filterDistrictData.getDistrictCode() != null && !filterDistrictData.getDistrictCode().isEmpty()) {
                builder.append(" and {dm:pk} like ?code");
                params.put("code", filterDistrictData.getDistrictCode() + "%");
            }
            else
            {
                builder.append(" and {s.spCode}= ?currentUser ");
                params.put("currentUser", currentUser);

            }
            builder.append(" order by {dm:pk}");

        }

        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(DistrictMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<DistrictMasterModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !searchResult.getResult().isEmpty()) {
            return searchResult.getResult();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public CounterVisitMasterModel findCounterVisitForUnFlaggedDealer(EyDmsCustomerModel eydmsCustomer, boolean isUnFlagRequestRaised, Date unFlagtime, String remarkForUnFlag) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {c:pk} from {CounterVisitMaster as c JOIN EyDmsCustomer as sc on {c:eydmsCustomer}={sc:pk}} where {c:eydmsCustomer}=?eydmsCustomer and {c:isUnFlagRequestRaised}=?isUnFlagRequestRaised and {c:unFlagRequestTime} LIKE ?unFlagtime and {c:remarkForUnFlag}=?remarkForUnFlag");
        params.put("eydmsCustomer", eydmsCustomer);
        params.put("isUnFlagRequestRaised", isUnFlagRequestRaised);
        //String unFlag="%".concat(unFlagtime.toString()).concat("%");
        params.put("unFlagtime", unFlagtime);
        params.put("remarkForUnFlag", remarkForUnFlag);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(CounterVisitMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<CounterVisitMasterModel> searchResult = flexibleSearchService.search(query);
        List<CounterVisitMasterModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result.get(0) : null;
    }

    //USEFULL
    @Override
    public EyDmsCustomerModel getSPForCustomer(EyDmsCustomerModel dealer) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT distinct{s.spcode} FROM {CustDepotDealerMapping as d join SpCustDepotMapping as s on {d.custDepotCode}={s.custDepotCode}} where  {d.dealerCode} =?eydmsCustomer  ");
        boolean active = Boolean.TRUE;
        params.put("eydmsCustomer", dealer);
        params.put("active", active);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
        query.addQueryParameters(params);
        final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult() != null && !searchResult.getResult().isEmpty() ? searchResult.getResult().get(0) : null;
    }

    //USEFULL
    @Override
    public SearchPageData<EyDmsCustomerModel> getAllCustomerForStateDistrict(SearchPageData searchPageData, String site, String state, String district, String city, String pincode, String influencerType, String counterType) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select distinct {sc.pk} from {EYDMSCustomer as sc  JOIN CustomerSubAreaMapping as m on {m.eydmsCustomer}={sc.pk} JOIN Address as add on {sc:pk}={add:owner}} where {sc.active}=1 and {add.billingAddress}=1 and {m.brand}=?brand and {add.state}=?state ");

        params.put("brand", baseSiteService.getBaseSiteForUID(site));
        params.put("state", state);

        if (counterType !=null && !counterType.isEmpty()) {
            params.put("counterType", CounterType.valueOf(counterType));//Dealer or influencer so that this can be used for both
            builder.append(" and {sc.counterType}=?counterType ");
        }
        if (influencerType !=null && !influencerType.isEmpty() && counterType.equalsIgnoreCase("Influencer")) {
            params.put("influencerType", InfluencerType.valueOf(influencerType));
            builder.append(" and {sc.influencerType}=?influencerType ");
        }
        if (district !=null && !district.isEmpty()) {
            params.put("district", district);
            builder.append(" and {add.district}=?district ");
        }
        if (city !=null && !city.isEmpty()) {
            params.put("city", city);
            builder.append(" and {add.erpcity}=?city ");
        }
        if (pincode !=null && !pincode.isEmpty()) {
            params.put("pincode", pincode);
            builder.append(" and {add.postalcode}=?pincode ");
        }

        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
        query.getQueryParameters().putAll(params);
        parameter.setFlexibleSearchQuery(query);
        return paginatedFlexibleSearchService.search(parameter);
    }

    //USEFULL
    @Override
    public EyDmsCustomerModel getSPForCustomerAndBrand(EyDmsCustomerModel dealer,BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT distinct{s.spcode} FROM {CustDepotDealerMapping as d join SpCustDepotMapping as s on {d.custDepotCode}={s.custDepotCode}} where  {d.dealerCode} =?eydmsCustomer and {s.brand}=?brand ");
        boolean active = Boolean.TRUE;
        params.put("eydmsCustomer", dealer);
        params.put("active", active);
        params.put("brand",brand);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
        query.addQueryParameters(params);
        final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult() != null && !searchResult.getResult().isEmpty() ? searchResult.getResult().get(0) : null;

    }

    //USEFULL
    @Override
    public List<EyDmsUserModel> getTSMforDistrict(DistrictMasterModel district, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        builder.append("Select {tsmUser} from {TsmDistrictMapping as t} where {t.brand}=?brand and {t.isActive}=?active ");
        params.put("active", Boolean.TRUE);
        params.put("brand",brand);

        if(district!=null){
            builder.append(" and {t.district}=?district ");
            params.put("district",district);
        }

        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsUserModel.class));
        query.addQueryParameters(params);
        final SearchResult<EyDmsUserModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !searchResult.getResult().isEmpty()) {
            return searchResult.getResult();
        } else {
            return Collections.emptyList();
        }
    }

    //USEFULL
    @Override
    public List<EyDmsUserModel> getRHforRegion(RegionMasterModel region, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();

        final StringBuilder builder = new StringBuilder();
        builder.append("Select {rhUser} from {RhRegionMapping as r} where {r.brand}=?brand and {r.isActive}=?active ");
        params.put("active", Boolean.TRUE);
        params.put("brand",brand);
        if(region!=null){
            builder.append(" and {r.region}=?region ");
            params.put("region",region);
        }

        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsUserModel.class));
        query.addQueryParameters(params);
        final SearchResult<EyDmsUserModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !searchResult.getResult().isEmpty()) {
            return searchResult.getResult();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<EyDmsUserModel> getTSOforSubArea(SubAreaMasterModel subAreaMaster) {
        final Map<String, Object> params = new HashMap<String, Object>();

        final StringBuilder builder = new StringBuilder();
        builder.append("select {t:tsoUser} from {TsoTalukaMapping as t}" +
                " where {t:subAreaMaster} = ?subAreaMaster and  {t:isActive} = ?active");

        params.put("active", Boolean.TRUE);
        params.put("subAreaMaster",subAreaMaster);

        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsUserModel.class));
        query.addQueryParameters(params);
        final SearchResult<EyDmsUserModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !searchResult.getResult().isEmpty()) {
            return searchResult.getResult();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<EyDmsCustomerModel> getAllCustomerForTerritoriesSO(List<SubAreaMasterModel> subAreaMaster) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {eydmsCustomer} FROM {CustomerSubAreaMapping} WHERE {subAreaMaster} in (?subAreaMaster) AND {isActive} = ?active AND {brand} = ?brand and {eydmsCustomer} is not null and {isOtherBrand}=?isOtherBrand ");
        boolean active = Boolean.TRUE;
        params.put("subAreaMaster", subAreaMaster);
        params.put("active", active);
        params.put("brand", baseSiteService.getCurrentBaseSite());
        params.put(CustomerSubAreaMappingModel.ISOTHERBRAND, Boolean.TRUE);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
        query.addQueryParameters(params);
        final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
        List<EyDmsCustomerModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public List<SubAreaMasterModel> getTerritoryForCustWithAllBrands(EyDmsCustomerModel customer) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {subAreaMaster} FROM {CustomerSubAreaMapping} WHERE {eydmsCustomer} = ?customer AND {isActive} = ?active  and {eydmsCustomer} is not null ");
        params.put("customer", customer);
        params.put("active", Boolean.TRUE);
       // params.put("brand", baseSiteService.getCurrentBaseSite());
        //  params.put(CustomerSubAreaMappingModel.ISOTHERBRAND, Boolean.FALSE);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(SubAreaMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<SubAreaMasterModel> searchResult = flexibleSearchService.search(query);
        List<SubAreaMasterModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();

    }

    @Override
    public List<EyDmsUserModel> getTSOforSubAreas(List<SubAreaMasterModel> subArea) {
        final Map<String, Object> params = new HashMap<String, Object>();

        final StringBuilder builder = new StringBuilder();
        builder.append("select distinct({t:tsoUser}) from {TsoTalukaMapping as t}" +
                " where {t:subAreaMaster} in (?subAreaMaster) and  {t:isActive} = ?active");

        params.put("active", Boolean.TRUE);
        params.put("subAreaMaster",subArea);

        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsUserModel.class));
        query.addQueryParameters(params);
        final SearchResult<EyDmsUserModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !searchResult.getResult().isEmpty()) {
            return searchResult.getResult();
        } else {
            return Collections.emptyList();
        }
    }
}