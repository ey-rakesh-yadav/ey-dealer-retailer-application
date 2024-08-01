package com.scl.core.dao.impl;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.DataConstraintDao;
import com.scl.core.dao.SclDealerRetailerDao;
import com.scl.core.enums.CounterType;
import com.scl.core.enums.CustomerGrouping;
import com.scl.core.model.*;
import com.scl.core.region.dao.GeographicalRegionDao;
import com.scl.core.utility.SclDateUtility;
import de.hybris.platform.cms2.servicelayer.services.admin.CMSAdminSiteService;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.*;

/**
 * The type Scl dealer retailer dao.
 */
public class SclDealerRetailerDaoImpl implements SclDealerRetailerDao {

    public static final Logger LOG = Logger.getLogger(SclDealerRetailerDaoImpl.class);
    private FlexibleSearchService flexibleSearchService;

    private PaginatedFlexibleSearchService paginatedFlexibleSearchService;
    private Map<String, String> sclDealerRetailerSortCodeToQueryAlias;
    private ConfigurationService configurationService;
    @Autowired
    DataConstraintDao dataConstraintDao;

    @Resource
    CMSAdminSiteService cmsAdminSiteService;
    @Resource
    private GeographicalRegionDao geographicalRegionDao;


    private static final String Dealer_Retailer_Mapping_List= "SELECT {" + DealerRetailerMappingModel.PK + "} FROM {" + DealerRetailerMappingModel._TYPECODE + "}";
    private static final String Dealer_Retailer_Mapping_List_For_Dealer=  "SELECT {" + DealerRetailerMappingModel.PK + "} FROM {" + DealerRetailerMappingModel._TYPECODE + "} WHERE {" +  DealerRetailerMappingModel.DEALER + "}=?dealer AND {"+DealerRetailerMappingModel.RETAILER+"} IS NULL";;

    private static final String Dealer_Retailer__List =  "select {pk} from {sclcustomer as scl} where {scl.CustomerGrouping} in (?CustomerGrouping)";

    private static final String GEO_MASTER_List =  "select {pk} from {GeographicalMaster}";

    private static final String Retailer_Mapping_List_For_Dealer=  "SELECT {" + DealerRetailerMappingModel.RETAILER + "} FROM {" + DealerRetailerMappingModel._TYPECODE + "} WHERE {" +  DealerRetailerMappingModel.DEALER + "}=?dealer AND {"+DealerRetailerMappingModel.RETAILER+"} IS Not NULL";;
    private static final String DEALER_Mapping_For_RETAILER=  "SELECT DISTINCT({" + DealerRetailerMappingModel.DEALER + "}) FROM {" + DealerRetailerMappingModel._TYPECODE + "} WHERE {" +  DealerRetailerMappingModel.RETAILER + "}=?retailer AND {"+DealerRetailerMappingModel.DEALER+"} IS Not NULL";


    /**
     * Gets dealer retailer mapping list.
     *
     * @return the dealer retailer mapping list
     */
    @Override
    public List<DealerRetailerMappingModel> getDealerRetailerMappingList() {

        final FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(Dealer_Retailer_Mapping_List);
        final SearchResult<DealerRetailerMappingModel> result = this.getFlexibleSearchService().search(flexibleSearchQuery);
        if (result == null)
        {
            LOG.debug("DealerRetailerMapping search: No Results found ! ");
            return Collections.emptyList();
        }
        return result.getResult();
    }

    @Override
    public List<DealerRetailerMappingModel> getDealerRetailerMappingListForDealer(SclCustomerModel dealer) {

        final FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(Dealer_Retailer_Mapping_List_For_Dealer);
        flexibleSearchQuery.addQueryParameter("dealer",dealer);
        final SearchResult<DealerRetailerMappingModel> result = this.getFlexibleSearchService().search(flexibleSearchQuery);
        if (result == null)
        {
            LOG.debug("DealerRetailerMapping search: No Results found ! ");
            return Collections.emptyList();
        }
        return result.getResult();
    }

    @Override
    public List<SclCustomerModel> getSclCustomerModelList( Date xOldDate) {

        final StringBuilder builder = new StringBuilder();
        builder.append(Dealer_Retailer__List);
        List<CustomerGrouping> CustomerGrouping = new ArrayList<CustomerGrouping>();
        CustomerGrouping.add(com.scl.core.enums.CustomerGrouping.ZDOM);
        CustomerGrouping.add(com.scl.core.enums.CustomerGrouping.ZRET);
       // CustomerGrouping.add(com.scl.core.enums.CustomerGrouping.YDOM);
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("CustomerGrouping",CustomerGrouping);

        if(null!=xOldDate){
            builder.append(" AND {scl.modifiedtime}>=?xOldDate");
            params.put("xOldDate", xOldDate);
        }

        final FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(builder.toString());
        flexibleSearchQuery.addQueryParameters(params);
        
        LOG.info(String.format("SclCustomerModel search query : %s", flexibleSearchQuery));

        final SearchResult<SclCustomerModel> result = this.getFlexibleSearchService().search(flexibleSearchQuery);
        if (result == null)
        {
            LOG.info("SclCustomerModel search: No Results found ! ");
            return Collections.emptyList();
        }

        LOG.info(" No of customers found "+ result.getCount());
        return result.getResult();
    }


    @Override
    public List<GeographicalMasterModel> getSclGeoMasterList( Date xOldDate) {

        final StringBuilder builder = new StringBuilder();
        builder.append(GEO_MASTER_List);
        final Map<String, Object> params = new HashMap<String, Object>();


         if(null!=xOldDate){
            builder.append(" where {modifiedtime}>=?xOldDate");
            params.put("xOldDate", xOldDate);
        }
         
        builder.append(" order by {todate}"); 
        
        final FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(builder.toString());
        flexibleSearchQuery.addQueryParameters(params);
        
        LOG.info(String.format("GeographicalMaster search Query : %s", flexibleSearchQuery));

        final SearchResult<GeographicalMasterModel> result = this.getFlexibleSearchService().search(flexibleSearchQuery);
        if (result == null)
        {
            LOG.info("GeographicalMaster search: No Results found ! ");
            return Collections.emptyList();
        }

        LOG.info(" No of GeographicalMasters found "+ result.getCount());
        return result.getResult();
    }


    @Override
    public List<AddressModel> getSclAddressByGeoMaster(String transportationZone){

        final StringBuilder builder = new StringBuilder();
        builder.append("select {pk} from {address} where {transportationzone}=?transportationZone  and {sapaddressusage} in ('DE','WE') ");
        final Map<String, Object> params = new HashMap<String, Object>();

        params.put("transportationZone", transportationZone);

        final FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(builder.toString());
        flexibleSearchQuery.addQueryParameters(params);

        final SearchResult<AddressModel> result = this.getFlexibleSearchService().search(flexibleSearchQuery);
        if (result == null)
        {
            LOG.info("AddressModel search: No Results found ! ");
            return Collections.emptyList();
        }

        LOG.info(" No of AddressModels found "+ result.getCount());
        return result.getResult();
    }

    @Override
    public List<SclCustomerModel> getRetailerMappingListForDealer(SclCustomerModel dealer) {

        final FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(Retailer_Mapping_List_For_Dealer);
        flexibleSearchQuery.addQueryParameter("dealer",dealer);
        final SearchResult<SclCustomerModel> result = this.getFlexibleSearchService().search(flexibleSearchQuery);
        if (result == null)
        {
            LOG.debug("getRetailerMappingListForDealer search: No Results found ! ");
            return Collections.emptyList();
        }
        return result.getResult();
    }


   @Override
    public DealerRetailerMappingModel getDealerRetailerMappingModel(SclCustomerModel sclCust, String addPK, String retailerPK){
        final Map<String, Object> params = new HashMap<String, Object>();
        String query = "SELECT {drm.pk} FROM {DealerRetailerMapping AS drm join address as add on {drm.shipTo}={add.pk}}  WHERE {drm.dealer}=?sclCust ";

        params.put("sclCust",sclCust);

       if(StringUtils.isNotEmpty(addPK)){
           query+=" and {add.pk} =?addPK";
           params.put("addPK",addPK);
       }

        if(StringUtils.isNotEmpty(retailerPK)){
            query+=" and {drm.retailer}=?retailerPK";
            params.put("retailerPK",retailerPK);
        }
        final SearchResult<DealerRetailerMappingModel> searchResult = this.getFlexibleSearchService().search(query, params);
        return org.apache.commons.collections.CollectionUtils.isNotEmpty(searchResult.getResult())? searchResult.getResult().get(0):null;

    }

    @Override
    public List<DealerRetailerMappingModel> getDealerRetailerMappingListModel(SclCustomerModel sclCust, String addPK, String retailerPK){
        final Map<String, Object> params = new HashMap<String, Object>();
        String query = "SELECT {drm.pk} FROM {DealerRetailerMapping AS drm join address as add on {drm.shipTo}={add.pk}}  WHERE {drm.dealer}=?sclCust ";

        params.put("sclCust",sclCust);

        if(StringUtils.isNotEmpty(addPK)){
            query+=" and {add.pk} =?addPK";
            params.put("addPK",addPK);
        }

        if(StringUtils.isNotEmpty(retailerPK)){
            query+=" and {drm.retailer}=?retailerPK";
            params.put("retailerPK",retailerPK);
        }
        final SearchResult<DealerRetailerMappingModel> searchResult = this.getFlexibleSearchService().search(query, params);
        return org.apache.commons.collections.CollectionUtils.isNotEmpty(searchResult.getResult())? searchResult.getResult():null;

    }



    @Override
    public CustomerSubAreaMappingModel getCustomerSubAreaMapping(String sclCust, SubAreaMasterModel subareamaster, String State, String subArea, String district) {
        final Map<String, Object> params = new HashMap<String, Object>();
        String query = "SELECT {csam.pk} FROM {CustomerSubAreaMapping AS csam  join sclcustomer as sclcus on {csam.sclCustomer}={sclcus.pk} }  WHERE {sclcus.uid}=?sclCust ";

        params.put("sclCust",sclCust);
        //params.put("subareamaster",subareamaster);
        //params.put("state",State);
        //params.put("subArea",subArea);
        //params.put("brand",cmsAdminSiteService.getSiteForId(SclCoreConstants.SCL_SITE));
        //params.put("district",district);
        final SearchResult<CustomerSubAreaMappingModel> searchResult = this.getFlexibleSearchService().search(query, params);
        return org.apache.commons.collections.CollectionUtils.isNotEmpty(searchResult.getResult())? searchResult.getResult().get(0):null;
    }

    /**
     * Gets address list for dealer and retailer.
     *
     * @param searchPageData     the search page data
     * @param dealer             the dealer
     * @param retailerId         the retailer id
     * @param transportationZone the transportation zone
     * @param filter             the filter
     * @return the address list for dealer and retailer
     */
    @Override
    public  List<AddressModel>  getAddressListForDealerAndRetailer(SearchPageData searchPageData, CustomerModel customer, String retailerId, String transportationZone,String filter) {

        Integer lastXDays = dataConstraintDao.findDaysByConstraintName("LAST_ADDRESS_USED_DAYS");

        GeographicalMasterModel geographicalMaster = geographicalRegionDao.fetchGeographicalMaster(transportationZone);

        final StringBuilder sql = new StringBuilder();
        final Map<String, Object> params = new HashMap<String, Object>();
        sql.append("SELECT {drm.shipTo} FROM {DealerRetailerMapping AS drm JOIN Address AS a ON {drm.shipTo}={a.pk} }");
        SclCustomerModel sclCustomer;
        //dealer check
        if(customer instanceof SclCustomerModel) {
             sclCustomer = (SclCustomerModel) customer;
            if(sclCustomer.getCounterType().equals(CounterType.DEALER)) {
                sql.append(" WHERE {drm.dealer}=?dealer");
                params.put("dealer", sclCustomer);
            } else if(sclCustomer.getCounterType().equals(CounterType.RETAILER)){
                sql.append(" WHERE {drm.retailer}=?retailer");
                params.put("retailer", sclCustomer);
            }
        }
        if (null != retailerId) {
            sql.append(" AND {drm.retailer}=({{SELECT {r.pk} FROM {SclCustomer as r} WHERE {r.uid}=?retailerId}})");
            params.put("retailerId", retailerId);
        }else{
            sql.append(" AND {drm.retailer} IS NULL");
        }

        if (Objects.nonNull(geographicalMaster)) {
            sql.append(" AND {a.transportationZone}=?geoMaster");
            params.put("geoMaster", geographicalMaster.getTransportationZone());
        }
        if(null!= lastXDays){
            sql.append(" AND ")
            .append(SclDateUtility.getLastXDayQuery("drm.lastUsed", params, lastXDays));
        }

        if (StringUtils.isNotBlank(filter)) {
            sql.append(" AND (UPPER({a.streetnumber}) like (?filter) OR UPPER({a.streetname}) like (?filter) OR UPPER({a.state}) like (?filter) OR UPPER({a.district}) like (?filter) OR UPPER({a.erpCity}) like (?filter)  OR UPPER({a.taluka}) like (?filter) OR {a.postalcode} like (?filter) OR UPPER({a.accountName}) like (?filter) )");
            params.put("filter", "%" + filter.toUpperCase() + "%");
        }

        final FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(sql.toString());
        flexibleSearchQuery.addQueryParameters(params);

        LOG.info(String.format("Get  DealerAndRetailer Address list query::%s",flexibleSearchQuery));

        return getPaginatedFlexibleSearchForAddress(searchPageData,flexibleSearchQuery);

    }


    /**
     * Get paginated flexible search for address list.
     *
     * @param searchPageData the search page data
     * @param searchQuery    the search query
     * @return the list
     */
    private List<AddressModel> getPaginatedFlexibleSearchForAddress(SearchPageData searchPageData,FlexibleSearchQuery searchQuery){
        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        parameter.setFlexibleSearchQuery(searchQuery);
        parameter.setSortCodeToQueryAlias(getSclDealerRetailerSortCodeToQueryAlias());

        final SearchPageData<AddressModel> searchResult = this.getPaginatedFlexibleSearchService().search(parameter);
        if(searchResult!=null && searchResult.getResults()!=null) {
            List<AddressModel> result = searchResult.getResults();
            if (result == null) {
                LOG.debug("DealerRetailerMapping Address search: No Results found ! ");
                return Collections.emptyList();
            }
            return result;
        }else{
            LOG.debug("DealerRetailerMapping Address search: No Results found ! ");
            return Collections.emptyList();
        }
    }

    @Override
    public DealerRetailerMappingModel getDealerRetailerMappingForDealerAndShipto(SclCustomerModel dealer, String shipToId){
        final StringBuilder sql = new StringBuilder();
        final Map<String, Object> params = new HashMap<String, Object>();
        sql.append("SELECT {drm.pk} FROM {DealerRetailerMapping AS drm JOIN Address AS a ON {drm.shipTo}={a.pk}} WHERE {drm.dealer}=?dealer and {a.partnerFunctionId}=?shipToId");
        params.put("dealer", dealer);
        params.put("shipToId", shipToId);

        final FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(sql.toString());
        flexibleSearchQuery.addQueryParameters(params);

        final SearchResult<DealerRetailerMappingModel> result = this.getFlexibleSearchService().search(flexibleSearchQuery);
        if (CollectionUtils.isEmpty(result.getResult()))
        {
            LOG.debug("getDealerRetailerMappingForDealerAndShipto search: No Results found ! ");
            return null;
        }
        return result.getResult().get(0);

    }


    @Override
    public UserSubAreaMappingModel getUserSubAreaMappingModelModel(SclUserModel sclUser, SubAreaMasterModel subAreaMaster, String district, String state, String subArea){
        final Map<String, Object> params = new HashMap<String, Object>();
        String query = "SELECT {usa.pk} FROM {UserSubAreaMapping AS usa}  WHERE {usa.sclUser}=?sclUser and {usa.subAreaMaster} =?subAreaMaster and {usa.district}=?district and {usa.state}=?state and {usa.subArea}=?subArea and {usa.brand}=?brand";

        params.put("sclUser",sclUser);
        params.put("subAreaMaster",subAreaMaster);
        params.put("district",district);
        params.put("state",state);
        params.put("subArea",subArea);
        params.put("brand",cmsAdminSiteService.getSiteForId(SclCoreConstants.SCL_SITE));
        final SearchResult<UserSubAreaMappingModel> searchResult = flexibleSearchService.search(query, params);
        return org.apache.commons.collections.CollectionUtils.isNotEmpty(searchResult.getResult())? searchResult.getResult().get(0):null;

    }

    /**
     * @param retailer
     * @return
     */
    @Override
    public SclCustomerModel getDealerForRetailer(SclCustomerModel retailer) {
        final FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(DEALER_Mapping_For_RETAILER);
        flexibleSearchQuery.addQueryParameter("retailer",retailer);
        final SearchResult<SclCustomerModel> result = this.getFlexibleSearchService().search(flexibleSearchQuery);
        if (result == null)
        {
            LOG.debug("DealerRetailerMapping search: No Results found ! ");
            return null;
        }
        return result.getResult().get(0);
    }


    /**
     * Gets flexible search service.
     *
     * @return the flexible search service
     */
    public FlexibleSearchService getFlexibleSearchService() {
        return flexibleSearchService;
    }

    /**
     * Sets flexible search service.
     *
     * @param flexibleSearchService the flexible search service
     */
    public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }

    /**
     * Gets paginated flexible search service.
     *
     * @return the paginated flexible search service
     */
    public PaginatedFlexibleSearchService getPaginatedFlexibleSearchService() {
        return paginatedFlexibleSearchService;
    }

    /**
     * Sets paginated flexible search service.
     *
     * @param paginatedFlexibleSearchService the paginated flexible search service
     */
    public void setPaginatedFlexibleSearchService(PaginatedFlexibleSearchService paginatedFlexibleSearchService) {
        this.paginatedFlexibleSearchService = paginatedFlexibleSearchService;
    }

    /**
     * Gets scl dealer retailer sort code to query alias.
     *
     * @return the scl dealer retailer sort code to query alias
     */
    public Map<String, String> getSclDealerRetailerSortCodeToQueryAlias() {
        return sclDealerRetailerSortCodeToQueryAlias;
    }

    /**
     * Sets scl dealer retailer sort code to query alias.
     *
     * @param sclDealerRetailerSortCodeToQueryAlias the scl dealer retailer sort code to query alias
     */
    public void setSclDealerRetailerSortCodeToQueryAlias(Map<String, String> sclDealerRetailerSortCodeToQueryAlias) {
        this.sclDealerRetailerSortCodeToQueryAlias = sclDealerRetailerSortCodeToQueryAlias;
    }

    /**
     * Gets configuration service.
     *
     * @return the configuration service
     */
    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    /**
     * Sets configuration service.
     *
     * @param configurationService the configuration service
     */
    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
}
