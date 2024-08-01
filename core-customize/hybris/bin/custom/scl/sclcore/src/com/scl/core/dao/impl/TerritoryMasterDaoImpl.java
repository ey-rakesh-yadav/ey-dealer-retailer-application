package com.scl.core.dao.impl;

import com.scl.core.dao.TerritoryMasterDao;
import com.scl.core.enums.CounterType;
import com.scl.core.enums.DealerCategory;
import com.scl.core.enums.InfluencerType;
import com.scl.core.model.*;
import com.scl.core.oauth.SclAuthenticationProvider;
import com.scl.facades.data.RequestCustomerData;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.*;

public class TerritoryMasterDaoImpl implements TerritoryMasterDao {

    private static final Logger LOG = Logger.getLogger(TerritoryMasterDaoImpl.class);
    @Autowired
    FlexibleSearchService flexibleSearchService;
    @Autowired
    BaseSiteService baseSiteService;
    @Autowired
    UserService userService;

    @Override
    public TerritoryMasterModel getTerritoryById(String territoryId) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final String queryString = "SELECT {pk} FROM {TerritoryMaster} WHERE {territoryCode} = ?territoryId ";
        params.put("territoryId", territoryId);
        FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<TerritoryMasterModel> searchResult = flexibleSearchService.search(query);
        if (CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult().get(0);
        }
        return null;
    }

    @Override
    public TerritoryMasterModel getTerritoryByPk(String territoryPk) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final String queryString = "SELECT {pk} FROM {TerritoryMaster} WHERE {pk} = ?territoryPk ";
        params.put("territoryPk", territoryPk);
        FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<TerritoryMasterModel> searchResult = flexibleSearchService.search(query);
        if (CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult().get(0);
        }
        return null;
    }

    @Override
    public List<TerritoryMasterModel> getTerritoriesForCustomer(UserModel customer) {

        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {territoryMaster} FROM {CustomerSubAreaMapping} WHERE {sclCustomer} = ?customer AND {isActive} = ?active AND {brand} = ?brand and {sclCustomer} is not null and {isOtherBrand}=?isOtherBrand ");
        params.put("customer", customer);
        params.put("active", Boolean.TRUE);
        params.put("brand", baseSiteService.getCurrentBaseSite());
        params.put(CustomerSubAreaMappingModel.ISOTHERBRAND, Boolean.FALSE);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(SubAreaMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<TerritoryMasterModel> searchResult = flexibleSearchService.search(query);
        List<TerritoryMasterModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public List<TerritoryMasterModel> getTerritoriesForSO(SclUserModel user) {
        final Map<String, Object> params = new HashMap<>();
        Date currentDate=new Date();
        String builder = "SELECT {territoryMaster} FROM {TerritoryUserMapping} WHERE {sclUser} = ?sclUser AND {brand} = ?brand AND {validFrom}<=?currentDate AND {validTo}>=?currentDate";
        params.put("sclUser", user);
        params.put("brand", baseSiteService.getCurrentBaseSite());
        params.put("currentDate",currentDate);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder);
        query.setResultClassList(List.of(TerritoryMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<TerritoryMasterModel> searchResult = flexibleSearchService.search(query);
        List<TerritoryMasterModel> result = searchResult.getResult();
        if (CollectionUtils.isNotEmpty(result)) {
            return result;
        }
        return Collections.emptyList();
    }

    private String getCustomerForUserQuery(RequestCustomerData requestCustomerData, List<TerritoryMasterModel> territoryMasterModelList, Map<String, Object> params) {
        final StringBuilder builder = new StringBuilder("SELECT {c.pk} FROM {SclCustomer as c} WHERE {c.territoryCode} in (?territoryMasterModelList) ");
        params.put("territoryMasterModelList", territoryMasterModelList);
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
        /*if (requestCustomerData.getRemoveFlaggedCustomer() != null && requestCustomerData.getRemoveFlaggedCustomer()) {
            builder.append("and {c.isDealerFlag}=?isDealerFlag ");
            params.put("isDealerFlag", Boolean.FALSE);
        }*/
        if (Objects.nonNull(requestCustomerData.getDealerCategory())) {
            builder.append("and {c.dealerCategory}=?dealerCategory ");
            params.put("dealerCategory", DealerCategory.valueOf(requestCustomerData.getDealerCategory()));
        }
        if (requestCustomerData.getIsNew() != null && requestCustomerData.getIsNew()) {
            LocalDate lastNinetyDay = LocalDate.now().minusDays(90);
            builder.append("and {c.dateOfJoining}>=?lastNinetyDay ");
            params.put("lastNinetyDay", lastNinetyDay.toString());
        }
        if (requestCustomerData.getIsFlag() != null && requestCustomerData.getIsFlag().equals(Boolean.TRUE)) {
            builder.append("and {c.isDealerFlag}=?isDealerFlag ");
            params.put("isDealerFlag", Boolean.TRUE);
        }
        if (requestCustomerData.getIsUnFlag() != null && requestCustomerData.getIsUnFlag().equals(Boolean.TRUE)) {
            builder.append("and {c.isUnFlagRequestRaised}=?isUnFlagRequestRaised ");
            params.put("isUnFlagRequestRaised", Boolean.TRUE);
        }
    }

    @Override
    public List<SclCustomerModel> getCustomerForUser(RequestCustomerData requestCustomerData, List<TerritoryMasterModel> territoryMasterModelList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        String strQuery = getCustomerForUserQuery(requestCustomerData, territoryMasterModelList, params);
        FlexibleSearchQuery query = new FlexibleSearchQuery(strQuery);
        query.setResultClassList(Collections.singletonList(SclCustomerModel.class));
        query.addQueryParameters(params);
        final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
        List<SclCustomerModel> result = searchResult.getResult();

        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }


    @Override
    public List<TerritoryMasterModel> getTerritoryForUser(TerritoryMasterModel territoryId) {
        final Map<String, Object> params = new HashMap<>();
        Date currentDate = new Date();
        StringBuilder builder = new StringBuilder("SELECT distinct {territoryMaster} FROM {TerritoryUserMapping} WHERE {sclUser}=?sclUser and {validFrom}<=?currentDate and {validTo}>=?currentDate");
        params.put("sclUser", userService.getCurrentUser());
        if (territoryId != null) {
            builder.append(" AND {territoryMaster}=?territoryId ");
            params.put("territoryId", territoryId);
        }
        params.put("currentDate",currentDate);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder);
        query.setResultClassList(List.of(TerritoryMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<TerritoryMasterModel> searchResult = flexibleSearchService.search(query);
        List<TerritoryMasterModel> result = searchResult.getResult();
        if (CollectionUtils.isNotEmpty(result)) {
            return result;
        }
        return Collections.emptyList();
    }

    @Override
    public List<SclUserModel> getAllSalesOfficersByState(String state, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT distinct {us.sclUser} FROM {TerritoryUserMapping as us join SclUser as u on {us.sclUser}={u.pk}} WHERE {u.state}=?state AND {us.brand}=?brand AND {us.isActive}=?active");
        boolean active = Boolean.TRUE;
        params.put("state", state);
        params.put("brand", brand);
        params.put("active", active);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(SclUserModel.class));
        query.addQueryParameters(params);
        final SearchResult<SclUserModel> searchResult = flexibleSearchService.search(query);
        List<SclUserModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public List<List<Object>> getDistrictAndTalukaForCustomer(SclCustomerModel customer) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("SELECT {subArea},{district} FROM {CustomerSubAreaMapping} WHERE {sclCustomer} = ?customer AND {isActive} = ?active AND {brand} = ?brand and {sclCustomer} is not null and {isOtherBrand}=?isOtherBrand ");
            params.put("customer", customer);
            params.put("active", Boolean.TRUE);
            params.put("brand", baseSiteService.getCurrentBaseSite());
            params.put(CustomerSubAreaMappingModel.ISOTHERBRAND, Boolean.FALSE);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }

    }


    @Override
    public List<TerritoryMasterModel> getTerritoryForSO() {
        final Map<String, Object> params = new HashMap<>();
        Date currentDate=new Date();
        StringBuilder builder = new StringBuilder("SELECT DISTINCT({territoryMaster}) FROM {TerritoryUserMapping} WHERE {sclUser}=?sclUser AND {brand}=?brand AND {validFrom}<=?currentDate AND {validTo}>=?currentDate");
        params.put("sclUser", userService.getCurrentUser());
        params.put("brand", baseSiteService.getCurrentBaseSite());
        params.put("currentDate",currentDate);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder);
        query.setResultClassList(List.of(TerritoryMasterModel.class));
        query.addQueryParameters(params);
        LOG.info("Get territories for SO:: "+query);
        final SearchResult<TerritoryMasterModel> searchResult = flexibleSearchService.search(query);
        List<TerritoryMasterModel> result = searchResult.getResult();
        if (CollectionUtils.isNotEmpty(result)) {
            return result;
        }
        return Collections.emptyList();
    }

    @Override
    public DistrictMasterModel getDistrictMaster(String district) {
        final Map<String, Object> params = new HashMap<String, Object>();
        String query = "SELECT {dm.pk} FROM {DistrictMaster AS dm} WHERE {dm.code}=?district";

        params.put("district", district);

        final SearchResult<DistrictMasterModel> searchResult = flexibleSearchService.search(query, params);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public TalukaMasterModel getTalukaMaster(String taluka) {
        final Map<String, Object> params = new HashMap<String, Object>();
        String query = "SELECT {dm.pk} FROM {TalukaMaster AS dm} WHERE {dm.code}=?taluka";

        params.put("taluka", taluka);

        final SearchResult<TalukaMasterModel> searchResult = flexibleSearchService.search(query, params);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) ? searchResult.getResult().get(0) : null;

    }

    @Override
    public RegionMasterModel getRegionMaster(String region) {
        final Map<String, Object> params = new HashMap<String, Object>();
        String query = "SELECT {dm.pk} FROM {RegionMaster AS dm} WHERE {dm.code}=?region";

        params.put("region", region);

        final SearchResult<RegionMasterModel> searchResult = flexibleSearchService.search(query, params);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) ? searchResult.getResult().get(0) : null;

    }

    @Override
    public StateMasterModel getStateMaster(String state) {
        final Map<String, Object> params = new HashMap<String, Object>();
        String query = "SELECT {dm.pk} FROM {StateMaster AS dm} WHERE {dm.code}=?state";

        params.put("state",state);

        final SearchResult<StateMasterModel> searchResult = flexibleSearchService.search(query, params);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) ? searchResult.getResult().get(0) : null;


    }

    @Override
    public List<SclUserModel> getUserByTerritory(TerritoryMasterModel territoryMaster) {

        final Map<String, Object> params = new HashMap<>();
        Date currentDate = new Date();
        StringBuilder builder = new StringBuilder("SELECT {sclUser} FROM {TerritoryUserMapping} WHERE {territoryMaster}=?territoryMaster AND {brand}=?brand AND {validFrom}<=?currentDate AND {validTo}>=?currentDate ");
        params.put("territoryMaster", territoryMaster);
        params.put("brand", baseSiteService.getCurrentBaseSite());
        params.put("currentDate",currentDate);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder);
        query.setResultClassList(List.of(SclUserModel.class));
        query.addQueryParameters(params);
        LOG.info(String.format("getUserTerritory for territory code query ::%s",query));
        final SearchResult<SclUserModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) ? searchResult.getResult(): null;

    }

    @Override
    public List<SCLIntSalesHierarchyModel> getAllIntSalesHierarchy() {
        String IntSales = "SELECT {pk} FROM {SCLIntSalesHierarchy} where {dateFrom}<=CURRENT_TIMESTAMP Order By {dateTo} Asc";
        FlexibleSearchQuery query = new FlexibleSearchQuery(IntSales);
        query.setResultClassList(List.of(SCLIntSalesHierarchyModel.class));
        final SearchResult<SCLIntSalesHierarchyModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) ? searchResult.getResult() : null;

    }


    @Override
    public SCLIntSalesHierarchyModel findIntSalesHierarchyForTerritoryCode(String territoryCode) {

        final Map<String, Object> params = new HashMap<>();
        String IntSales = "SELECT {pk} FROM {SCLIntSalesHierarchy} where {territoryCode}=?territoryCode and {dateFrom}<=CURRENT_TIMESTAMP and {dateTo}>=CURRENT_TIMESTAMP ORDER BY {modifiedtime} desc";

        params.put("territoryCode", territoryCode);

        FlexibleSearchQuery query = new FlexibleSearchQuery(IntSales);
        query.setResultClassList(List.of(SCLIntSalesHierarchyModel.class));
        query.addQueryParameters(params);
        final SearchResult<SCLIntSalesHierarchyModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) ? searchResult.getResult().get(0) : null;

    }

    /**
     * @param sclUser
     * @return
     */
    @Override
    public List<TerritoryUserMappingModel> getTerritoryUserMappingForUser(SclUserModel sclUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        Date currentDate = new Date();
        if (Objects.nonNull(sclUser)) {
                StringBuilder builder = new StringBuilder("SELECT {pk} FROM {TerritoryUserMapping} WHERE {sclUser}=?sclUser and {validFrom}<=?currentDate and {validTo}>=?currentDate");
                params.put("sclUser", sclUser);
                params.put("currentDate",currentDate);
                FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
                query.setResultClassList(List.of(TerritoryUserMappingModel.class));
                query.addQueryParameters(params);
                LOG.info(String.format("get Territory USer Mapping query ::%s",query));
                final SearchResult<TerritoryUserMappingModel> searchResult = flexibleSearchService.search(query);
                if (CollectionUtils.isNotEmpty(searchResult.getResult())) {
                    return searchResult.getResult();
                }

        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public boolean checkValidTSOMapping(SclUserModel sclUser){

        final Map<String, Object> params = new HashMap<>();
        String IntSales = "SELECT {pk} FROM {TSOTALUKAMAPPING} where {tsoUser}=?sclUser and {isActive}=1";

        params.put("sclUser", sclUser);

        FlexibleSearchQuery query = new FlexibleSearchQuery(IntSales);
        query.setResultClassList(List.of(SCLIntSalesHierarchyModel.class));
        query.addQueryParameters(params);
        final SearchResult<SCLIntSalesHierarchyModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) ? false : true;

    }


}