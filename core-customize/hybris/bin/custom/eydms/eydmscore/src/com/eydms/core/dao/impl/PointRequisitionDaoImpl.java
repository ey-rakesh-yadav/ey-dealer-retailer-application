package com.eydms.core.dao.impl;

import com.eydms.core.dao.DataConstraintDao;
import com.eydms.core.dao.PointRequisitionDao;
import com.eydms.core.dao.TerritoryManagementDao;
import com.eydms.core.enums.PointRequisitionStatus;
import com.eydms.core.enums.TransactionType;
import com.eydms.core.model.*;
import com.eydms.core.region.dao.impl.DistrictMasterDaoImpl;
import com.eydms.core.services.impl.PointRequisitionServiceImpl;
import com.eydms.core.utility.EyDmsDateUtility;
import com.eydms.facades.data.FilterTalukaData;
import com.eydms.facades.data.RequestCustomerData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;

import com.eydms.core.enums.CounterType;
import com.eydms.core.enums.InfluencerType;
import com.eydms.core.model.PointRequisitionModel;
import com.eydms.core.model.EyDmsCustomerModel;
import de.hybris.platform.core.model.product.ProductModel;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.customer.services.EyDmsCustomerService;
import com.eydms.core.enums.RequisitionStatus;
import com.eydms.core.model.PointRequisitionModel;
import com.eydms.core.model.EyDmsCustomerModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;


import javax.annotation.Resource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class PointRequisitionDaoImpl extends DefaultGenericDao<PointRequisitionModel> implements PointRequisitionDao {

    private static final Logger LOG = Logger.getLogger(PointRequisitionDaoImpl.class);

    @Resource
    FlexibleSearchService flexibleSearchService;

    @Resource
    UserService userService;

    @Autowired
	BaseSiteService baseSiteService;
    @Autowired
    DistrictMasterDaoImpl districtMasterDao;
    @Autowired
    TerritoryManagementDaoImpl territoryManagementDao;
 
	@Autowired
	private CatalogVersionService catalogVersionService;
	
    @Override
    public FlexibleSearchService getFlexibleSearchService() {
        return flexibleSearchService;
    }

    @Override
    public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    private DataConstraintDao dataConstraintDao;

    @Resource
    private EyDmsCustomerService eydmsCustomerService;

    public PointRequisitionDaoImpl() {
        super(PointRequisitionModel._TYPECODE);
    }

    @Autowired
    PaginatedFlexibleSearchService paginatedFlexibleSearchService;

    @Autowired
    TerritoryManagementDaoImpl territoryManagementDaoImpl;


    @Override
    public PointRequisitionModel findByRequisitionId(String requisitionId) {
        if (requisitionId != null && !(requisitionId.isEmpty())) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(PointRequisitionModel.REQUISITIONID, requisitionId);

            final List<PointRequisitionModel> orderRequisitionList = this.find(map);
            if (orderRequisitionList != null && !(orderRequisitionList.isEmpty())) {
                return orderRequisitionList.get(0);
            }
        }
        return null;
    }

    @Override
    public ProductPointMasterModel getPointsForRequisition(ProductModel product, String schemeId) {
        final Map<String, Object> params = new HashMap<String, Object>();
        LOG.info("getPointsForRequisition DAO :" + product + " " +  schemeId);
        final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {ProductPointMaster} WHERE {product}=?product and {schemeId} = ?schemeId");
        params.put("product", product);
        params.put("schemeId", schemeId);
//        params.put("district", district);
//        params.put("state", influencer.getState());
//        params.put("influencerType", influencerType);
//        params.put("currentDate", LocalDate.now().toString());
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(ProductPointMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<ProductPointMasterModel> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : null;
        else
            return null;
    }

    @Override
    public Integer getAllocationRequestCount() {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select count(*) from {PointRequisition as p} where {p:requestRaisedTo}=?currentUser and {p:isRequisitionPlaced}=?isRequisitionPlaced and {p:status}=?status");
        PointRequisitionStatus status = PointRequisitionStatus.PENDING;
        EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
        params.put("currentUser", currentUser);
        params.put("status", status);
        Boolean isRequisitionPlaced=Boolean.TRUE;
        params.put("isRequisitionPlaced", isRequisitionPlaced);
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
    public List<PointRequisitionModel> getAllocationRequestList() {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {PointRequisition as p} where {p:requestRaisedTo}=?currentUser and {p:isRequisitionPlaced}=?isRequisitionPlaced and {p:status}=?status");
        PointRequisitionStatus status = PointRequisitionStatus.PENDING;
        EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
        params.put("currentUser", currentUser);
        params.put("status", status);
        Boolean isRequisitionPlaced = Boolean.TRUE;
        params.put("isRequisitionPlaced", isRequisitionPlaced);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(PointRequisitionModel.class));
        final SearchResult<PointRequisitionModel> searchResult = flexibleSearchService.search(query);
        List<PointRequisitionModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public SearchPageData<PointRequisitionModel> getListOfAllPointRequisition(boolean isDraft, String filter, List<String> statuses, SearchPageData searchPageData, String requisitionId, String influencerCode) {
        final StringBuilder sql = new StringBuilder();
        final Map<String, Object> map = new HashMap<String, Object>();
        EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
        sql.append("select {pk} from  {PointRequisition as p ");
        if(filter!=null){
        	sql.append(" join Product as a on {a.pk}={p.product} join EyDmsCustomer as c on {c.pk}=");
        	if((currentUser.getGroups()
        			.contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID)))) {
        		sql.append("{p.requestRaisedTo} ");
        	}
        	else {
        		sql.append("{p.requestRaisedFor} ");        		
        	}
        }
        sql.append("} where");
        if((currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID)))) {
        	sql.append(" {requestRaisedFor}=?currentUser ");
        }else {
        	sql.append(" {requestRaisedTo}=?currentUser ");
        }
        map.put("currentUser",currentUser);
        if(isDraft){
            sql.append(" and {isRequisitionPlaced}=0") ;
        }
        else {
        	Integer lastXDays = dataConstraintDao.findDaysByConstraintName("POINT_REQUISTION_LISTING_VISIBLITY");

        	sql.append(" and {isRequisitionPlaced}=1 and " + EyDmsDateUtility.getLastXDayQuery("p:requisitionCreationDate", map, lastXDays));
        }
        if(statuses!=null && !statuses.isEmpty()){
            List<PointRequisitionStatus> requisitionStatuses = new ArrayList<>();
        	for(String status : statuses){
        		requisitionStatuses.add(PointRequisitionStatus.valueOf(status));
        	}
        	sql.append(" and {p:status} in (?statues)");
        	map.put("statues",requisitionStatuses);
        }

        if(requisitionId!=null) {
        	sql.append(" and {p:requisitionId} = ?requisitionId ");
            map.put("requisitionId",requisitionId);
        }
        if(influencerCode!=null) {
        	sql.append(" and {requestRaisedFor}=?influencerCode ");
            EyDmsCustomerModel influencer = (EyDmsCustomerModel) userService.getUserForUID(influencerCode);
            map.put("influencerCode",influencer);
        }
        

        if(filter!=null){
             // EyDmsCustomerModel requestRaisedTo =eydmsCustomerService.getEyDmsCustomerForUid(filter);
               sql.append(" and (UPPER({c.uid}) like ?filter or UPPER({c.name}) like ?filter or UPPER({a.name}) like ?filter ) ");
               map.put("filter","%" + filter.toUpperCase() + "%");
        }
        
        sql.append(" order by {p:modifiedTime} desc " );
        
        
//        final Map<String, Object> params = new HashMap<String, Object>();
//        final StringBuilder builder = new StringBuilder("select {pk} from {PointRequisition}");
//        PointRequisitionStatus status = PointRequisitionStatus.PENDING;
//        EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
//        params.put("currentUser", currentUser);

      /*  final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());

        sql.append(" order by p:requisitionCreationDate" );*/
//        final Map<String, Object> params = new HashMap<String, Object>();
//        final StringBuilder builder = new StringBuilder("select {pk} from {PointRequisition}");
        /* final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());

        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(PointRequisitionModel.class));
        final SearchResult<PointRequisitionModel> searchResult = flexibleSearchService.search(query);
        List<PointRequisitionModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();*/

        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        

       // final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());


        query.setResultClassList(Collections.singletonList(PointRequisitionModel.class));
        query.getQueryParameters().putAll(map);
        parameter.setFlexibleSearchQuery(query);
        return paginatedFlexibleSearchService.search(parameter);


    }
       
    @Override
    public List<List<Object>> getBagLiftedMTDforInfluencers(List<EyDmsCustomerModel> requestRaisedForList, String startDate, String endDate,List<String> doList,List<String> subAreaList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        if(requestRaisedForList!=null && !requestRaisedForList.isEmpty()) {
            if (requestRaisedForList.size() == 1) {
                LOG.info("Size:"+requestRaisedForList.size());
                builder.append("select {p.requestRaisedFor},sum({p.quantity}) from {PointRequisition as p} ")
                        .append(" where {p.deliveryDate}>=?startDate and {p.deliveryDate}<?endDate ")
                        .append(" and {p.requestRaisedFor} = ?requestRaisedForList and {p:status}=?status ");
                params.put("requestRaisedForList", requestRaisedForList.get(0));
            } else {
                builder.append("select {p.requestRaisedFor},sum({p.quantity}) from {PointRequisition as p} ")
                        .append(" where {p.deliveryDate}>=?startDate and {p.deliveryDate}<?endDate ")
                        .append(" and {p.requestRaisedFor} in (?requestRaisedForList) and {p:status}=?status ");
                params.put("requestRaisedForList", requestRaisedForList);
            }


            PointRequisitionStatus status = PointRequisitionStatus.APPROVED;

            params.put("status", status);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            List<DistrictMasterModel> list = new ArrayList<>();
            List<SubAreaMasterModel> list1 = new ArrayList<>();

            if (doList != null && !doList.isEmpty()) {
                for (String code : doList) {
                    list.add(districtMasterDao.findByCode(code));
                }
                params.put("doList", list);
                builder.append(" and {p.districtMaster} in (?doList) ");
            }
            if (subAreaList != null && !subAreaList.isEmpty()) {
                for (String id : subAreaList) {
                    list1.add(territoryManagementDao.getTerritoryById(id));
                }
                params.put("subAreaList", list1);
                builder.append(" and {p.subAreaMaster} in (?subAreaList) ");
            }
            builder.append(" group by {p.requestRaisedFor} ");

            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(EyDmsCustomerModel.class, Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return result != null && !result.isEmpty() ? result : Collections.emptyList();
        }
        else{
            return Collections.emptyList();
        }
    }

    @Override
    public List<List<Object>> requisitionRaisedDetails(EyDmsCustomerModel currentUser) {
        final Map<String,Object> attr = new HashMap<>();

        attr.put("currentUser",currentUser);
        attr.put("isRequisitionPlaced",Boolean.TRUE);
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT count({p.pk}), sum({quantity}) FROM {PointRequisition as p} WHERE {p.requestRaisedFor}=?currentUser AND {p.isRequisitionPlaced}=?isRequisitionPlaced AND ").append(EyDmsDateUtility.getMtdClauseQuery("p.requisitionCreationDate",attr));

        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Integer.class, Double.class));
        query.getQueryParameters().putAll(attr);
        final SearchResult<List<Object>> result = getFlexibleSearchService().search(query);
        if(result.getResult()!=null && !result.getResult().isEmpty()){
            return result.getResult();
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public Integer pendingRequisitionDetails(EyDmsCustomerModel currentUser) {
        final Map<String,Object> attr = new HashMap<>();

        attr.put("currentUser",currentUser);
        attr.put("isRequisitionPlaced",Boolean.TRUE);
        attr.put("pointRequisitionStatus", PointRequisitionStatus.PENDING);
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT count({p.pk}) FROM {PointRequisition as p} WHERE {p.requestRaisedFor}=?currentUser AND {p.isRequisitionPlaced}=?isRequisitionPlaced AND {p.status}=?pointRequisitionStatus ");

        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Integer.class));
        query.getQueryParameters().putAll(attr);

        final SearchResult<Integer> result = getFlexibleSearchService().search(query);

        return result.getResult()!=null && !result.getResult().isEmpty() && result.getResult().get(0)!=null?result.getResult().get(0):0;
    }

    @Override
    public Double pointsFromPreviousYear(EyDmsCustomerModel currentUser) {
        final Map<String,Object> attr = new HashMap<>();
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();
        String currentFinancialstartDate;
        if(currentMonth == 1 || currentMonth == 2 || currentMonth == 3){
            currentFinancialstartDate = (currentYear - 1) + "-04-01";
        }
        else{
        	currentFinancialstartDate = currentYear + "-04-01";
        }

        attr.put("currentUser",currentUser);
        attr.put("startDate", currentFinancialstartDate);
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT TOP 1 {balance} FROM {PointsTransactionMaster} WHERE {customer}=?currentUser AND {date}<?startDate ORDER BY {date} DESC");

        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.getQueryParameters().putAll(attr);

        final SearchResult<Double> result = getFlexibleSearchService().search(query);
        return result.getResult()!=null && !result.getResult().isEmpty() && result.getResult().get(0)!=null?result.getResult().get(0):0.0;
    }

    @Override
    public Double pointsEarnedCurrentYear(EyDmsCustomerModel currentUser) {
        final Map<String,Object> attr = new HashMap<>();
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();
        String startDate;
        String endDate;
        if(currentMonth == 1 || currentMonth == 2 || currentMonth == 3){
            startDate = (currentYear - 1) + "-04-01";
            endDate = currentYear + "-04-01";
        }
        else{
            startDate = currentYear + "-04-01";
            endDate = (currentYear + 1) + "-04-01";
        }
        attr.put("currentUser", currentUser);
        attr.put("transactionType", TransactionType.CREDIT);
        attr.put("startDate", startDate);
        attr.put("endDate",endDate);
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT SUM({points}) FROM {PointsTransactionMaster} WHERE {customer}=?currentUser AND {date}>=?startDate AND {date}<?endDate AND {transactionType}=?transactionType");

        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.getQueryParameters().putAll(attr);

        final SearchResult<Double> result = getFlexibleSearchService().search(query);

        return result.getResult()!=null && !result.getResult().isEmpty() && result.getResult().get(0)!=null?result.getResult().get(0):0.0;
    }

    @Override
    public Double pointsRedeemed(EyDmsCustomerModel currentUser) {
        final Map<String,Object> attr = new HashMap<>();
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();
        String startDate;
        String endDate;
        if(currentMonth == 1 || currentMonth == 2 || currentMonth == 3){
            startDate = (currentYear - 1) + "-04-01";
            endDate = currentYear + "-04-01";
        }
        else{
            startDate = currentYear + "-04-01";
            endDate = (currentYear + 1) + "-04-01";
        }

        attr.put("currentUser",currentUser);
        attr.put("startDate", startDate);
        attr.put("endDate",endDate);

        attr.put("transactionType", TransactionType.DEBIT);
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT SUM({points}) FROM {PointsTransactionMaster} WHERE {customer}=?currentUser AND {date}>=?startDate AND {date}<?endDate AND {transactionType}=?transactionType");

        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.getQueryParameters().putAll(attr);

        final SearchResult<Double> result = getFlexibleSearchService().search(query);
        return result.getResult()!=null && !result.getResult().isEmpty() && result.getResult().get(0)!=null?result.getResult().get(0):0.0;
    }

    @Override
    public Double totalRedeemablePoints(EyDmsCustomerModel currentUser) {
        final Map<String,Object> attr = new HashMap<>();
        attr.put("currentUser",currentUser);
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT TOP 1 {balance} FROM {PointsTransactionMaster} WHERE {customer}=?currentUser ORDER BY {date} DESC");

        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.getQueryParameters().putAll(attr);

        final SearchResult<Double> result = getFlexibleSearchService().search(query);
        return result.getResult()!=null && !result.getResult().isEmpty() && result.getResult().get(0)!=null?result.getResult().get(0):0.0;
    }

    @Override
    public List<GiftShopModel> giftShopSummary() {
        final Map<String,Object> attr = new HashMap<>();
        String currentDate = LocalDateTime.now().toString();
        EyDmsCustomerModel eydmsCustomerModel =(EyDmsCustomerModel)userService.getCurrentUser();
        CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");

        attr.put("currentDate",currentDate);
        attr.put("influencerTypes","%" + eydmsCustomerModel.getInfluencerType().getCode() + "%");
        attr.put("geography", "%" + eydmsCustomerModel.getState() + "%");
        attr.put("counterType", CounterType.INFLUENCER);
        attr.put(GiftShopModel.CATALOGVERSION, catalogVersion);
        
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT {pk} FROM {GiftShop as g} WHERE {startDate}<=?currentDate AND {endDate}>=?currentDate AND {counterType}=?counterType AND {influencerTypes} like ?influencerTypes AND ({geography} is null or {geography} like ?geography) ");
        sql.append(" AND {g." + GiftShopModel.CATALOGVERSION + "} = (?" + GiftShopModel.CATALOGVERSION + ") ");
        sql.append(" ORDER BY {geography} DESC, {influencerTypes} DESC");
        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Collections.singletonList(GiftShopModel.class));
        query.getQueryParameters().putAll(attr);

        final SearchResult<GiftShopModel> searchResult = getFlexibleSearchService().search(query);
        List<GiftShopModel> result = searchResult.getResult();

        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public CustomersInfluencerMapModel getInfluencersListForCustomers(EyDmsCustomerModel fromCustomer, EyDmsCustomerModel influencer, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {CustomersInfluencerMap} where {fromCustomer}=?fromCustomer and {influencer}=?influencer and {brand}=?brand");

        params.put("fromCustomer", fromCustomer);
        params.put("influencer", influencer);
        params.put("brand", brand);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(CustomersInfluencerMapModel.class));
        query.addQueryParameters(params);
        final SearchResult<CustomersInfluencerMapModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
        else
            return null;
    }

    @Override
    public Double bagOffTake(EyDmsCustomerModel currentUser) {
        final Map<String, Object> attr = new HashMap<>();
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();
        String startDate;
        String endDate;
        if (currentMonth == 1 || currentMonth == 2 || currentMonth == 3) {
            startDate = (currentYear - 1) + "-04-01";
            endDate = currentYear + "-04-01";
        } else {
            startDate = currentYear + "-04-01";
            endDate = (currentYear + 1) + "-04-01";
        }

        attr.put("currentUser", currentUser);
        attr.put("status", PointRequisitionStatus.APPROVED);
        attr.put("startDate", startDate);
        attr.put("endDate", endDate);

        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT SUM({quantity}) FROM {PointRequisition} WHERE {requestRaisedFor}=?currentUser AND {status}=?status AND {deliveryDate}>=?startDate AND {deliveryDate}<?endDate");

        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.getQueryParameters().putAll(attr);

        final SearchResult<Double> result = getFlexibleSearchService().search(query);
        return result.getResult()!=null && !result.getResult().isEmpty() && result.getResult().get(0)!=null?result.getResult().get(0):0.0;
    }
    @Override
    public PointRequisitionModel getRequistionDetails(String requisitionId) {

        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {PointRequisition} where {requisitionid}=?requisitionId");
        params.put("requisitionId", requisitionId);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(CustomersInfluencerMapModel.class));
        query.addQueryParameters(params);
        final SearchResult<PointRequisitionModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
        else
            return null;
    }

    @Override
    public SearchPageData<EyDmsCustomerModel> getList(SearchPageData searchPageData, EyDmsCustomerModel eydmsCustomerModel, BaseSiteModel site) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select distinct{s.pk} from {CustomersInfluencerMap as ci join EyDmsCustomer as s on {ci.fromCustomer} ={s.pk}}  where {ci:influencer}=?currentUser");
        boolean active = Boolean.TRUE;
        params.put("currentUser", userService.getCurrentUser());
        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
        query.getQueryParameters().putAll(params);
        parameter.setFlexibleSearchQuery(query);
        return paginatedFlexibleSearchService.search(parameter);
    }

    @Override
    public SearchPageData<EyDmsCustomerModel> getAllCustomerForTerritories(String filter,SearchPageData searchPageData,List<SubAreaMasterModel> subArea) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {m.eydmsCustomer} FROM {CustomerSubAreaMapping as m join EyDmsCustomer as c on {c.pk}={m.eydmsCustomer}} WHERE {m.subAreaMaster} in (?subAreaMaster) AND {m.isActive} = ?active AND {m.brand} = ?brand and {m.eydmsCustomer} is not null and {m.isOtherBrand}=?isOtherBrand");
        boolean active = Boolean.TRUE;
        params.put("subAreaMaster", subArea);
        params.put("active", active);
        params.put("brand", baseSiteService.getCurrentBaseSite());
        if(filter!=null){
            String filterKey= "%".concat(filter.toUpperCase()).concat("%");
            builder.append(" AND ( UPPER({c.uid}) like ?filter OR UPPER({c.mobileNumber}) like ?filter OR " +
                    "UPPER({c.name}) like ?filter ) ");
            params.put("filter", filterKey);
        }
        params.put(CustomerSubAreaMappingModel.ISOTHERBRAND, Boolean.FALSE);
        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
        query.getQueryParameters().putAll(params);
        parameter.setFlexibleSearchQuery(query);
        return paginatedFlexibleSearchService.search(parameter);
    }


    @Override
    public SearchPageData<EyDmsCustomerModel> getSavedDealerRetailer(SearchPageData searchPageData, EyDmsCustomerModel eydmsCustomerModel, BaseSiteModel site) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {s.pk} from {CustomersInfluencerMap as ci join EyDmsCustomer as s on {ci.fromCustomer} ={s.pk} join PointRequisition as p on {s.pk}={p.requestRaisedFor}} where {ci.influencer}=?currentUser and {p:isRequisitionPlaced}=?isRequisitionPlaced ");
        params.put("currentUser", userService.getCurrentUser());

        Boolean isRequisitionPlaced=Boolean.TRUE;
        params.put("isRequisitionPlaced", isRequisitionPlaced);
        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
        query.getQueryParameters().putAll(params);
        parameter.setFlexibleSearchQuery(query);
        return paginatedFlexibleSearchService.search(parameter);
    }

    @Override
    public List<List<Object>> getInfluencerDetailedPaginatedSummaryList(RequestCustomerData requestCustomerData){
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
            builder.append(" {c.pk},sum({p.quantity}) FROM {CustomerSubAreaMapping as m join EyDmsCustomer as c on {c.pk}={m.eydmsCustomer} left join PointRequisition as p on {p.requestRaisedFor}={c.pk}} WHERE {m.subAreaMaster} in (?subAreaMaster) AND {c.counterType}=?counterType AND {m.isActive} = ?active AND {m.brand} = ?brand and {m.eydmsCustomer} is not null  and {p.deliveryDate}>=?startDate and {p.deliveryDate}<?endDate  and {p:status}=?status ");
            territoryManagementDaoImpl.appendFilterQuery(builder,params,requestCustomerData);
            builder.append(" group by  {c.pk} order by sum({p.quantity}) desc ");
            params.put("subAreaMaster",subAreaMaster);
            params.put("counterType",CounterType.INFLUENCER);
            //params.put("currentUser",currentUser);

        }
        else if(currentUser instanceof EyDmsCustomerModel){
            EyDmsCustomerModel customer = (EyDmsCustomerModel) currentUser;
            if(customer.getCounterType()!=null){
                if(customer.getCounterType().equals(CounterType.DEALER)){
                    builder.append("Select ");
                    if(requestCustomerData.getTopPerformers()!=null && requestCustomerData.getTopPerformers()>0){
                        builder.append("TOP  " +  requestCustomerData.getTopPerformers());
                    }
                    builder.append(" {c.pk},sum({p.quantity}) from {DealerInfluencerMap as d join EyDmsCustomer as c  on {d.influencer}={c.pk} left join PointRequisition as p on {p.requestRaisedFor}={c.pk}} WHERE {d.fromCustomer}=?eydmsCustomer  AND {d.active}=?active AND {d.brand}=?brand and {p.deliveryDate}>=?startDate and {p.deliveryDate}<?endDate  and {p:status}=?status ");
                    requestCustomerData.setIncludeNonEyDmsCustomer(true);
                    territoryManagementDaoImpl.appendFilterQuery(builder,params,requestCustomerData);
                    builder.append(" group by {c.pk} order by sum({p.quantity}) desc ");
                    params.put("eydmsCustomer",currentUser);
                } else if (customer.getCounterType().equals(CounterType.RETAILER)){
                    builder.append("Select ");
                    if(requestCustomerData.getTopPerformers()!=null && requestCustomerData.getTopPerformers()>0){
                        builder.append("TOP ?topPerformers " );
                        params.put("topPerformers",requestCustomerData.getTopPerformers());
                    }
                    builder.append(" {c.pk},sum({p.quantity})  from {RetailerInfluencerMap as d join EyDmsCustomer as c  on {d.influencer}={c.pk} join PointRequisition as p on {p.requestRaisedFor}={c.pk}} WHERE {d.fromCustomer}=?eydmsCustomer AND {d.active}=?active AND {d.brand}=?brand and {p.deliveryDate}>=?startDate and {p.deliveryDate}<?endDate  and {p:status}=?status ");
                    requestCustomerData.setIncludeNonEyDmsCustomer(true);
                    territoryManagementDaoImpl.appendFilterQuery(builder,params,requestCustomerData);
                    builder.append(" group by {c.pk} order by sum({p.quantity}) desc ");
                    params.put("eydmsCustomer",currentUser);
                }
            }

        }
        params.put("status",PointRequisitionStatus.APPROVED);
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

}
