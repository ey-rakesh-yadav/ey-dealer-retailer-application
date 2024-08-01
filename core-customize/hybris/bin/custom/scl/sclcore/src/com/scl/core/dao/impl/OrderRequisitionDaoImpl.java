package com.scl.core.dao.impl;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.DataConstraintDao;
import com.scl.core.dao.OrderRequisitionDao;
import com.scl.core.enums.*;
import com.scl.core.model.*;
import com.scl.core.region.dao.impl.DistrictMasterDaoImpl;
import com.scl.core.services.TerritoryMasterService;
import com.scl.core.utility.SclDateUtility;

import com.scl.facades.data.FilterTalukaData;
import com.scl.facades.data.OrderRequisitionData;
import com.scl.facades.data.RequestCustomerData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import javax.annotation.Resource;

public class OrderRequisitionDaoImpl extends DefaultGenericDao<OrderRequisitionModel> implements OrderRequisitionDao {
    private static final org.apache.log4j.Logger LOG = Logger.getLogger(OrderRequisitionDaoImpl.class);
    @Autowired
    UserService userService;
    @Autowired
    TerritoryMasterService territoryMasterService;

    @Autowired
    PaginatedFlexibleSearchService paginatedFlexibleSearchService;
    @Autowired
    DistrictMasterDaoImpl districtMasterDao;
    
    @Resource
    FlexibleSearchService flexibleSearchService;
    @Autowired
    TerritoryManagementDaoImpl territoryManagementDaoImpl;

    @Autowired
    BaseSiteService baseSiteService;

    public OrderRequisitionDaoImpl() {
        super(OrderRequisitionModel._TYPECODE);
    }

    @Override
    public OrderRequisitionModel findByRequisitionId(String requisitionId) {
        if(requisitionId != null && !(requisitionId.isEmpty())) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(OrderRequisitionModel.REQUISITIONID, requisitionId);

            final List<OrderRequisitionModel> orderRequisitionList = this.find(map);
            if(orderRequisitionList!= null && !(orderRequisitionList.isEmpty())) {
                return orderRequisitionList.get(0);
            }
        }
        return null;
    }

    @Autowired
    DataConstraintDao dataConstraintDao;

    @Override
    public SearchPageData<OrderRequisitionModel> getOrderRequisitionDetails(RequisitionStatus[] statuses, String submitType, String fromDate, SclCustomerModel currentUser, String productCode, SearchPageData searchPageData, String requisitionId, String searchKey) {
        final StringBuilder sql = new StringBuilder();
        final Map<String, Object> map = new HashMap<String, Object>();

        List<RequisitionStatus> requisitionStatuses = new ArrayList<>();

        if(currentUser!=null) {
            if(currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                if(searchKey!=null && !searchKey.isEmpty()) {
                    sql.append("select {or:pk} from {OrderRequisition as or join SclCustomer as cust on {or:fromCustomer}={cust:pk}}" +
                            " where {toCustomer} = ?retailer  and {cust:name} like ?searchKey or {cust:uid} like ?searchKey or {cust:customerNo} like ?searchKey or {or:requisitionId} like ?searchKey");
                    map.put("searchKey", searchKey.toUpperCase() + "%");
                  //  map.put("requisitionPlaced", Boolean.TRUE);
                }
                else {
                    sql.append("select {pk} from {OrderRequisition} where " +
                            " {toCustomer} = ?retailer");
                }
                map.put("retailer", currentUser);
            }
            else if(currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                if(productCode != null && !productCode.isEmpty()) {
                    sql.append("select {or:pk} from {OrderRequisition as or join Product as p on {or:product}={p:pk}} where " +
                            " {fromCustomer} = ?dealer and {p:code} = ?productCode");
                    map.put("productCode", productCode);
                }
                else if(searchKey!=null && !searchKey.isEmpty()) {
                    sql.append("Select {or:pk} from {OrderRequisition as or join SclCustomer as cust on {or:toCustomer}={cust:pk}}" +
                            " where {fromCustomer} = ?dealer and  {cust:name} like ?searchKey or {cust:uid} like ?searchKey or {cust:customerNo} like ?searchKey or {or:requisitionId} like ?searchKey");
                 /*   if(submitType.toLowerCase().equals("draft")) {
                        map.put("requisitionPlaced",Boolean.FALSE);
                    }
                    else {
                        map.put("requisitionPlaced",Boolean.TRUE);
                    }*/
                    map.put("searchKey", searchKey.toUpperCase() + "%");
                }
                else {
                    sql.append("select {pk} from {OrderRequisition} where " +
                            " {fromCustomer} = ?dealer");
                }
                map.put("dealer", currentUser);
                //submitType = "all";
                //need to check
                /*sql.append(" and ({serviceType} = ?serviceType  or {acceptedDate} is null) ");
                map.put("serviceType", ServiceType.CLUBBED_PLACED);*/
                
            }
            if(StringUtils.isEmpty(fromDate)) {
                Integer lastXDays = dataConstraintDao.findDaysByConstraintName("ORDER_REQUISTION_LISTING_VISIBLITY");
                sql.append(" and ").append(SclDateUtility.getLastXDayQuery("requisitionDate", map, lastXDays));
                LOG.info(String.format("Last X days:%s",lastXDays));
            }
            else {
                LOG.info(String.format("From Date:%s",fromDate));
            	sql.append(" and {requisitionDate} like ?fromDate");
            	 map.put("fromDate",fromDate);
            }
           
        }

    /*    if(!submitType.isEmpty() && submitType!=null) {
            if(submitType.equals("draft") || submitType.equals("Draft")) {
                map.put("requisitionPlaced", Boolean.FALSE);
            } else {
                map.put("requisitionPlaced", Boolean.TRUE);
            }
            sql.append(" and {isRequisitionPlaced} = ?requisitionPlaced");
        }*/

        if(requisitionId!=null && !requisitionId.isEmpty()) {
            sql.append(" and {requisitionId} = ?requisitionId");
            map.put("requisitionId",requisitionId);
        }

        if(statuses.length>0){
            for (RequisitionStatus status : statuses) {
                LOG.info(String.format("Req status for Order Req:%s",status));
            }
            sql.append(" and {status} in (?statusList)");
            map.put("statusList", Arrays.asList(statuses));
        }
       /* if(statuses!=null && !statuses.isEmpty()) {
            for(String status : statuses) {
                requisitionStatuses.add(RequisitionStatus.valueOf(status));
            }
            map.put("requisitionStatuses",requisitionStatuses);
            sql.append(" and {status} in (?requisitionStatuses)");
        }*/

        if(currentUser.getCounterType().equals(CounterType.RETAILER)) {
            sql.append(" order by {modifiedtime} desc");
        }
        else if(currentUser.getCounterType().equals(CounterType.DEALER)) {
            sql.append(" order by {requisitionDate} desc");
        }

        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Collections.singletonList(OrderRequisitionModel.class));
        query.getQueryParameters().putAll(map);
        parameter.setFlexibleSearchQuery(query);
        LOG.info(String.format("Query for Order REq from Get api:%s",query));
        return paginatedFlexibleSearchService.search(parameter);
    }
    @Override
    public List<List<Object>> getSalsdMTDforRetailer(List<SclCustomerModel> toCustomerList, String startDate, String endDate,List<String> doList,List<String> territoryList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        if(CollectionUtils.isNotEmpty(toCustomerList)) {
            final StringBuilder builder = new StringBuilder("select {p.toCustomer},sum({p.quantity}) from {OrderRequisition as p} ")
                    .append(" where {p.deliveredDate}>=?startDate and {p.deliveredDate}<=?endDate ")
                    .append(" and {p.toCustomer} in (?toCustomerList) and {p:status}=?status ");
            params.put("toCustomerList", toCustomerList);
            params.put("status", RequisitionStatus.DELIVERED);
            params.put("startDate", startDate);
            params.put("endDate", endDate);

            List<TerritoryMasterModel> list1 = new ArrayList<>();

            if ( territoryList!= null && !territoryList.isEmpty()) {
                for (String id : territoryList) {
                    list1.add(territoryMasterService.getTerritoryById(id));
                }
                params.put("territoryModelList", list1);
                builder.append(" and {p.territoryMaster} in (?territoryModelList) ");
            }
            builder.append(" group by {p.toCustomer}");
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(SclCustomerModel.class, Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return result != null && !result.isEmpty() ? result : Collections.emptyList();
        }
        else{
            return Collections.emptyList();
        }
    }

    @Override
    public DealerRetailerMappingModel getDealerforRetailerDetails(SclCustomerModel dealer, SclCustomerModel retailer, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {DealerRetailerMapping} where {dealer}=?dealer and {retailer}=?retailer and {brand} = ?brand");

        params.put("dealer", dealer);
        params.put("retailer", retailer);
        params.put("brand", brand);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(DealerRetailerMappingModel.class));
        query.addQueryParameters(params);
        final SearchResult<DealerRetailerMappingModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
        else
            return null;
    }

    @Override
    public List<List<Object>> getRetailedDetailedPaginatedSummaryList(RequestCustomerData requestCustomerData) {
        final Map<String, Object> params  =  new HashMap<>();
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        final StringBuilder builder = new StringBuilder();
        if(currentUser instanceof SclUserModel){
            FilterTalukaData filterTalukaData = new FilterTalukaData();
            List<SubAreaMasterModel> subAreaMaster = territoryManagementDaoImpl.getTalukaForUser(filterTalukaData);
            builder.append("Select ");
            if(requestCustomerData.getTopPerformers()!=null && requestCustomerData.getTopPerformers()>0){
                builder.append("TOP  " +  requestCustomerData.getTopPerformers());
            }
            builder.append(" {c.pk},sum({p.quantity}) FROM {CustomerSubAreaMapping as m join SclCustomer as c on {c.pk}={m.sclCustomer} left join OrderRequisition as p on {c.pk}={p.toCustomer}} WHERE {m.subAreaMaster} in (?subAreaMaster) AND {c.counterType}=?counterType AND {m.isActive} = ?active AND {m.brand} = ?brand and {m.sclCustomer} is not null and {m.isOtherBrand}=?isOtherBrand and {p.deliveredDate}>=?startDate and {p.deliveredDate}<?endDate  and {p:status}=?status ");
            territoryManagementDaoImpl.appendFilterQuery(builder,params,requestCustomerData);
            builder.append(" group by  {c.pk} order by sum({p.quantity}) desc ");
            params.put("subAreaMaster",subAreaMaster);
            params.put("counterType", CounterType.RETAILER);
        }
        else if(currentUser instanceof SclCustomerModel){
            SclCustomerModel customer = (SclCustomerModel) currentUser;
            if(customer.getCounterType()!=null){
                builder.append("Select ");
                if(requestCustomerData.getTopPerformers()!=null && requestCustomerData.getTopPerformers()>0){
                    builder.append("TOP  " +  requestCustomerData.getTopPerformers());
                }
                builder.append(" {c.pk},sum({p.quantity}) from {DealerRetailerMapping as d join SclCustomer as c  on {d.retailer}={c.pk} left join OrderRequisition as p on {c.pk}={p.toCustomer}} WHERE {d.dealer}=?sclCustomer AND  {p.deliveredDate}>=?startDate and {p.deliveredDate}<?endDate  and {p:status}=?status ");
                requestCustomerData.setIncludeNonSclCustomer(true);
                territoryManagementDaoImpl.appendFilterQuery(builder,params,requestCustomerData);
                builder.append(" group by {c.pk} order by sum({p.quantity}) desc ");
                params.put("sclCustomer",currentUser);
            }

        }
        params.put("status",RequisitionStatus.DELIVERED);
        params.put("active",Boolean.TRUE);
        params.put("brand",baseSiteService.getCurrentBaseSite());
        params.put("startDate",requestCustomerData.getStartDate());
        params.put("endDate",requestCustomerData.getEndDate());

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());

        query.setResultClassList(Arrays.asList(SclCustomerModel.class, Double.class));
        query.getQueryParameters().putAll(params);

        final SearchResult<List<Object>> result = getFlexibleSearchService().search(query);
        if(result.getResult()!=null && !result.getResult().isEmpty()){
            return result.getResult();
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public OrderModel findOrderByCode(String code) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {Order} where {code} = ?code and {versionId} is null");

        params.put("code", code);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(OrderModel.class));
        query.addQueryParameters(params);
        final SearchResult<OrderModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
        else
            return null;
    }

    @Override
    public OrderModel getOrderFromERPOrderNumber(String erpOrderNo){
        final Map<String, Object> params = new HashMap<String, Object>();
        String query = "SELECT {o.pk} FROM {order AS o}  WHERE {o.erpOrderNumber}=?erpOrderNo";
        params.put("erpOrderNo",erpOrderNo);
        final SearchResult<OrderModel> searchResult = flexibleSearchService.search(query, params);
        return CollectionUtils.isNotEmpty(searchResult.getResult())? searchResult.getResult().get(0):null;
    }

    @Override
    public ProductModel getProductFromEquiCode(String equiCode, CatalogVersionModel catalogVer){
        final Map<String, Object> params = new HashMap<String, Object>();
        String query = "SELECT {p.pk} FROM {product AS p}  WHERE {p.code}=?code and {p.catalogVersion}=?catalogVer";
        params.put("code",equiCode);
        params.put("catalogVer",catalogVer);
        final SearchResult<ProductModel> searchResult = flexibleSearchService.search(query, params);
        return CollectionUtils.isNotEmpty(searchResult.getResult())? searchResult.getResult().get(0):null;
    }

    @Override
    public TerritoryMasterModel getTerritoryMasterByTrriId(String trriId){

        final Map<String, Object> params = new HashMap<String, Object>();
        String query = "SELECT {pk} FROM {TerritoryMaster} WHERE {territoryCode} = ?trriId";
        params.put("trriId",trriId);
        final SearchResult<TerritoryMasterModel> searchResult = flexibleSearchService.search(query, params);
        return CollectionUtils.isNotEmpty(searchResult.getResult())? searchResult.getResult().get(0):null;
    }

    @Override
    public TerritoryUserMappingModel getTerritoryUserMapping(String trriId, String Uid){
        final Map<String, Object> params = new HashMap<String, Object>();
        String query = "SELECT {tum.pk} FROM {TerritoryUserMapping AS tum join TerritoryMaster as tm on {tum.territoryMaster}={tm.pk} join scluser as su on {tum.sclUser}={su.pk} }  WHERE {su.uid}=?uid and {tm.territoryCode} =?trriId";
        params.put("trriId",trriId);
        params.put("uid",Uid);
        final SearchResult<TerritoryUserMappingModel> searchResult = flexibleSearchService.search(query, params);

        return CollectionUtils.isNotEmpty(searchResult.getResult())? searchResult.getResult().get(0):null;
    }

    @Override
    public MasterStockAllocationModel getMasterAllocationEntry(OrderRequisitionData orderRequisitionData) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {MasterStockAllocation}  WHERE {id} =?invoiceId");
        params.put("invoiceId",orderRequisitionData.getInvoiceNumber());

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(MasterStockAllocationModel.class));
        query.addQueryParameters(params);
        LOG.info(String.format("query for MasterStock pk:%s",query));
        final SearchResult<MasterStockAllocationModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
        else
            return null;
    }

    @Override
    public List<OrderRequisitionModel> getSalesDetailsForDealerOfRetailersFromOrmDao(SclCustomerModel raisedByCustomer, SclCustomerModel raisedToCustomer, String fromDate, String toDate, String filter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {or:pk} from {OrderRequisition AS or} ")
                .append(" where {or:status}=?requisitionStatus and {or:requisitionType}=?requisitionType  and {or:toCustomer}=?raisedToCustomer and {or:product} is not null ");
        if(Objects.nonNull(raisedByCustomer)){
            builder.append(" and {or:fromCustomer}=?raisedByCustomer ");
            params.put("raisedByCustomer", raisedByCustomer);
        }


        params.put("raisedToCustomer", raisedToCustomer);
        params.put("requisitionStatus",RequisitionStatus.SERVICED_BY_DEALER);
        params.put("requisitionType", RequisitionType.LIFTING);

        if (StringUtils.isNotBlank(fromDate) && StringUtils.isNotBlank(toDate)) {
            builder.append(" and {or:liftingDate}>=?fromDate and {or:liftingDate}<=?toDate ");
            params.put("fromDate", fromDate);
            params.put("toDate", toDate);
        }
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(OrderRequisitionModel.class));
        query.addQueryParameters(params);
        LOG.info(String.format("Sales visibility query from OrderRequisition ::%s", query));
        final SearchResult<OrderRequisitionModel> searchResult = flexibleSearchService.search(query);
        List<OrderRequisitionModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public List<MasterStockAllocationModel> getSalesDetailsForDealerOfRetailersFromMsaDao(SclCustomerModel raisedByCustomer, SclCustomerModel raisedToCustomer, String fromDate, String toDate, String filter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {m:pk} from {MasterStockAllocation AS m} ")
                .append("where {m:retailer} =?retailer and {m:isInvoiceCancelled}=?isInvoiceCancelled ");
        if(Objects.nonNull(raisedByCustomer)){
            builder.append(" and {m:dealer}=?dealer " );
            params.put("dealer", raisedByCustomer);
        }

        params.put("retailer", raisedToCustomer);
        params.put("isInvoiceCancelled", Boolean.FALSE);

        if (StringUtils.isNotBlank(fromDate) && StringUtils.isNotBlank(toDate)) {
            builder.append(" and {m:invoicedDate}>=?fromDate and {m:invoicedDate}<=?toDate ");
            params.put("fromDate", fromDate);
            params.put("toDate", toDate);
        }
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(MasterStockAllocationModel.class));
        query.addQueryParameters(params);
        LOG.info(String.format("Sales visibility query from MasterStockAllocation::%s", query));
        final SearchResult<MasterStockAllocationModel> searchResult = flexibleSearchService.search(query);
        List<MasterStockAllocationModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

}
