package com.eydms.core.dao.impl;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.dao.DataConstraintDao;
import com.eydms.core.dao.OrderRequisitionDao;
import com.eydms.core.enums.CounterType;
import com.eydms.core.enums.PointRequisitionStatus;
import com.eydms.core.enums.RequisitionStatus;
import com.eydms.core.enums.ServiceType;
import com.eydms.core.model.*;
import com.eydms.core.region.dao.DistrictMasterDao;
import com.eydms.core.region.dao.impl.DistrictMasterDaoImpl;
import com.eydms.core.utility.EyDmsDateUtility;

import com.eydms.facades.data.FilterTalukaData;
import com.eydms.facades.data.RequestCustomerData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import javax.annotation.Resource;

public class OrderRequisitionDaoImpl extends DefaultGenericDao<OrderRequisitionModel> implements OrderRequisitionDao {

    @Autowired
    UserService userService;

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
    public SearchPageData<OrderRequisitionModel> getOrderRequisitionDetails(List<String> statuses, String submitType, String fromDate, EyDmsCustomerModel currentUser, String productCode, SearchPageData searchPageData, String requisitionId, String searchKey) {
        final StringBuilder sql = new StringBuilder();
        final Map<String, Object> map = new HashMap<String, Object>();

        List<RequisitionStatus> requisitionStatuses = new ArrayList<>();

        if(currentUser!=null) {
            if(currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                if(searchKey!=null && !searchKey.isEmpty()) {
                    sql.append("select {or:pk} from {OrderRequisition as or join EyDmsCustomer as cust on {or:fromCustomer}={cust:pk}}" +
                            " where {toCustomer} = ?retailer and {or:isRequisitionPlaced} = ?requisitionPlaced and {cust:name} like ?searchKey or {cust:uid} like ?searchKey or {cust:customerNo} like ?searchKey or {or:requisitionId} like ?searchKey");
                    map.put("searchKey", searchKey.toUpperCase() + "%");
                    map.put("requisitionPlaced", Boolean.TRUE);
                }
                else {
                    sql.append("select {pk} from {OrderRequisition} where " +
                            " {toCustomer} = ?retailer");
                }
                map.put("retailer", currentUser);
            }
            else if(currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                if(productCode != null && !productCode.isEmpty()) {
                    sql.append("select {or:pk} from {OrderRequisition as or join Product as p on {or:product}={p:pk}} where " +
                            " {fromCustomer} = ?dealer and {p:code} = ?productCode");
                    map.put("productCode", productCode);
                }
                else if(searchKey!=null && !searchKey.isEmpty()) {
                    sql.append("Select {or:pk} from {OrderRequisition as or join EyDmsCustomer as cust on {or:toCustomer}={cust:pk}}" +
                            " where {fromCustomer} = ?dealer and {or:isRequisitionPlaced} = ?requisitionPlaced and {cust:name} like ?searchKey or {cust:uid} like ?searchKey or {cust:customerNo} like ?searchKey or {or:requisitionId} like ?searchKey");
                    if(submitType.toLowerCase().equals("draft")) {
                        map.put("requisitionPlaced",Boolean.FALSE);
                    }
                    else {
                        map.put("requisitionPlaced",Boolean.TRUE);
                    }
                    map.put("searchKey", searchKey.toUpperCase() + "%");
                }
                else {
                    sql.append("select {pk} from {OrderRequisition} where " +
                            " {fromCustomer} = ?dealer");
                }
                map.put("dealer", currentUser);
                submitType = "all";
                
                sql.append(" and ({serviceType} = ?serviceType  or {acceptedDate} is null) ");
                map.put("serviceType", ServiceType.CLUBBED_PLACED);
                
            }
            if(fromDate==null) {
                Integer lastXDays = dataConstraintDao.findDaysByConstraintName("ORDER_REQUISTION_LISTING_VISIBLITY");
                sql.append(" and ")
                .append(EyDmsDateUtility.getLastXDayQuery("requisitionDate", map, lastXDays));
            }
            else {
            	sql.append(" and {requisitionDate} like ?fromDate");
            	 map.put("fromDate",fromDate);
            }
           
        }

        if(!submitType.isEmpty() && submitType!=null) {
            if(submitType.equals("draft") || submitType.equals("Draft")) {
                map.put("requisitionPlaced", Boolean.FALSE);
            } else {
                map.put("requisitionPlaced", Boolean.TRUE);
            }
            sql.append(" and {isRequisitionPlaced} = ?requisitionPlaced");
        }

        if(requisitionId!=null && !requisitionId.isEmpty()) {
            sql.append(" and {requisitionId} = ?requisitionId");
            map.put("requisitionId",requisitionId);
        }

        if(statuses!=null && !statuses.isEmpty()) {
            for(String status : statuses) {
                requisitionStatuses.add(RequisitionStatus.valueOf(status));
            }
            map.put("requisitionStatuses",requisitionStatuses);
            sql.append(" and {status} in (?requisitionStatuses)");
        }

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
        return paginatedFlexibleSearchService.search(parameter);
    }
    @Override
    public List<List<Object>> getSalsdMTDforRetailer(List<EyDmsCustomerModel> toCustomerList, String startDate, String endDate,List<String> doList,List<String> subAreaList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {p.toCustomer},sum({p.quantity}) from {OrderRequisition as p} ")
        		.append(" where {p.deliveredDate}>=?startDate and {p.deliveredDate}<=?endDate ")
        		.append(" and {p.toCustomer} in (?toCustomerList) and {p:status}=?status ");
        params.put("toCustomerList", toCustomerList);
        params.put("status", RequisitionStatus.DELIVERED);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        List<DistrictMasterModel> list = new ArrayList<>();
        List<SubAreaMasterModel> list1 = new ArrayList<>();
        if(doList!=null && !doList.isEmpty()){
            for(String codes: doList){
                list.add(districtMasterDao.findByCode(codes));
            }
            params.put("doList", list);
            builder.append(" and {p.districtMaster} in (?doList) ");
        }
        if (subAreaList != null && !subAreaList.isEmpty()) {
            for (String id : subAreaList) {
                list1.add(territoryManagementDaoImpl.getTerritoryById(id));
            }
            params.put("subAreaList", list1);
            builder.append(" and {p.subAreaMaster} in (?subAreaList) ");
        }
        builder.append(" group by {p.toCustomer}");
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(EyDmsCustomerModel.class, Double.class));
        final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
        List<List<Object>> result = searchResult.getResult();
        return result!=null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public DealerRetailerMapModel getDealerforRetailerDetails(EyDmsCustomerModel dealer, EyDmsCustomerModel retailer, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {DealerRetailerMap} where {dealer}=?dealer and {retailer}=?retailer and {brand} = ?brand");

        params.put("dealer", dealer);
        params.put("retailer", retailer);
        params.put("brand", brand);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(DealerRetailerMapModel.class));
        query.addQueryParameters(params);
        final SearchResult<DealerRetailerMapModel> searchResult = flexibleSearchService.search(query);
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
        if(currentUser instanceof EyDmsUserModel){
            FilterTalukaData filterTalukaData = new FilterTalukaData();
            List<SubAreaMasterModel> subAreaMaster = territoryManagementDaoImpl.getTalukaForUser(filterTalukaData);
            builder.append("Select ");
            if(requestCustomerData.getTopPerformers()!=null && requestCustomerData.getTopPerformers()>0){
                builder.append("TOP  " +  requestCustomerData.getTopPerformers());
            }
            builder.append(" {c.pk},sum({p.quantity}) FROM {CustomerSubAreaMapping as m join EyDmsCustomer as c on {c.pk}={m.eydmsCustomer} left join OrderRequisition as p on {c.pk}={p.toCustomer}} WHERE {m.subAreaMaster} in (?subAreaMaster) AND {c.counterType}=?counterType AND {m.isActive} = ?active AND {m.brand} = ?brand and {m.eydmsCustomer} is not null and {m.isOtherBrand}=?isOtherBrand and {p.deliveredDate}>=?startDate and {p.deliveredDate}<?endDate  and {p:status}=?status ");
            territoryManagementDaoImpl.appendFilterQuery(builder,params,requestCustomerData);
            builder.append(" group by  {c.pk} order by sum({p.quantity}) desc ");
            params.put("subAreaMaster",subAreaMaster);
            params.put("counterType", CounterType.RETAILER);
        }
        else if(currentUser instanceof EyDmsCustomerModel){
            EyDmsCustomerModel customer = (EyDmsCustomerModel) currentUser;
            if(customer.getCounterType()!=null){
                builder.append("Select ");
                if(requestCustomerData.getTopPerformers()!=null && requestCustomerData.getTopPerformers()>0){
                    builder.append("TOP  " +  requestCustomerData.getTopPerformers());
                }
                builder.append(" {c.pk},sum({p.quantity}) from {DealerRetailerMap as d join EyDmsCustomer as c  on {d.retailer}={c.pk} left join OrderRequisition as p on {c.pk}={p.toCustomer}} WHERE {d.dealer}=?eydmsCustomer AND {d.active}=?active AND {d.brand}=?brand and {p.deliveredDate}>=?startDate and {p.deliveredDate}<?endDate  and {p:status}=?status ");
                requestCustomerData.setIncludeNonEyDmsCustomer(true);
                territoryManagementDaoImpl.appendFilterQuery(builder,params,requestCustomerData);
                builder.append(" group by {c.pk} order by sum({p.quantity}) desc ");
                params.put("eydmsCustomer",currentUser);
            }

        }
        params.put("status",RequisitionStatus.DELIVERED);
        params.put("active",Boolean.TRUE);
        params.put("brand",baseSiteService.getCurrentBaseSite());
        params.put("startDate",requestCustomerData.getStartDate());
        params.put("endDate",requestCustomerData.getEndDate());

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());

        query.setResultClassList(Arrays.asList(EyDmsCustomerModel.class, Double.class));
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
}
