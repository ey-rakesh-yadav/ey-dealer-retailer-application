package com.eydms.core.dao.impl;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.dao.NetworkDao;
import com.eydms.core.enums.*;
import com.eydms.core.model.*;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.core.utility.EyDmsDateUtility;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

public class NetworkDaoImpl implements NetworkDao {

    private static final Logger LOG = Logger.getLogger(NetworkDaoImpl.class);
    public static final String BRAND = "brand";
    @Resource
    FlexibleSearchService flexibleSearchService;

    @Resource
    SearchRestrictionService searchRestrictionService;
    @Resource
    BaseSiteService baseSiteService;
    @Resource
    TerritoryManagementService territoryManagementService;
    @Resource
    UserService userService;

    @Autowired
    PaginatedFlexibleSearchService paginatedFlexibleSearchService;

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public FlexibleSearchService getFlexibleSearchService() {
        return flexibleSearchService;
    }

    public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }

    public SearchRestrictionService getSearchRestrictionService() {
        return searchRestrictionService;
    }

    public void setSearchRestrictionService(SearchRestrictionService searchRestrictionService) {
        this.searchRestrictionService = searchRestrictionService;
    }

    public BaseSiteService getBaseSiteService() {
        return baseSiteService;
    }

    public void setBaseSiteService(BaseSiteService baseSiteService) {
        this.baseSiteService = baseSiteService;
    }

    public TerritoryManagementService getTerritoryManagementService() {
        return territoryManagementService;
    }

    public void setTerritoryManagementService(TerritoryManagementService territoryManagementService) {
        this.territoryManagementService = territoryManagementService;
    }

    private final String LOCATION_COUNTER_DETAILS_POPULATE_QUERY = "SELECT {sc:name},{add:latitude},{add:longitude},{add:streetNumber},{add:streetName},{add:city},{add:taluka},{add:district},{add:state},{add:postalCode} ,{sc:uid}" +
            "from {EYDMSCustomer as sc JOIN Address as add on {sc:pk}={add:owner}} " +
            "where {sc:uid}=?dealerCode";

    @Override
    public NetworkAdditionPlanModel findNeworkPlanByTalukaAndLeadType(List<SubAreaMasterModel> subArea, String leadType, Date timestamp) {
        final Map<String, Object> params = new HashMap<>();
        String queryString = "SELECT {pk} FROM {NetworkAdditionPlan as np join LeadType as l on {np.applicableTo}={l.pk}} WHERE {np.subAreaMaster} IN (?subArea) and {l.code}=?leadType and {creationTime}>=?timestamp";
        params.put("subArea", subArea);
        params.put("leadType", LeadType.valueOf(leadType).getCode());
        params.put("timestamp", timestamp);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<NetworkAdditionPlanModel> searchResult = flexibleSearchService.search(query);
        if (CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult().get(0);
        }

        return null;
    }

    @Override
    public ProspectiveNetworkModel findPerspectiveNetworkByCode(String code) {
        final Map<String, Object> params = new HashMap<>();
        String queryString = "SELECT {pk} FROM {ProspectiveNetwork} WHERE {crmAccountID}=?code and {brand}=?brand";
        params.put("code", code);
        params.put(BRAND, baseSiteService.getCurrentBaseSite());
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<ProspectiveNetworkModel> searchResult = flexibleSearchService.search(query);
        if (CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult().get(0);
        }
        return null;
    }


    @Override
    public List<DealerVehicleDetailsModel> getAllVehicleDetails() {
        String queryString = "SELECT {pk} FROM {DealerVehicleDetails}";
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        final SearchResult<DealerVehicleDetailsModel> searchResult = flexibleSearchService.search(query);
        if (CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }
        return null;
    }

    @Override
    public List<EyDmsCustomerModel> getDealersForSalesPromoter(EyDmsCustomerModel promoter) {
        String queryString = "Select {sc.pk} from {SalesPromoterDealerMap as spmap join EyDmsCustomer as sc on {sc" +
                ".pk}={spmap.dealer}} where  {spmap.promoter}=?promoter";
        final Map<String, Object> params = new HashMap<>();
        params.put("promoter", promoter);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
        if (CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();
    }

    @Override
    public List<EyDmsCustomerModel> getNetworkDealersForSalesPromoter(EyDmsCustomerModel promoter, List<SubAreaMasterModel> subAreas) {
        String queryString = "Select {sc.pk} from {SalesPromoterDealerMap as spmap join EyDmsCustomer as sc on {sc" +
                ".pk}={spmap.dealer} join CustomerSubAreaMapping as csm on {csm.eydmsCustomer}={sc.pk}} " +
                "where  {spmap.promoter}=?promoter and {csm.subAreaMaster} in (?subAreaMaster) and {csm.isActive} = " +
                "?active and  {csm.brand} = ?brand and {csm.isOtherBrand}=?isOtherBrand";
        final Map<String, Object> params = new HashMap<>();
        boolean active = Boolean.TRUE;
        params.put("subAreaMaster", subAreas);
        params.put("active", active);
        params.put(BRAND, baseSiteService.getCurrentBaseSite());
        params.put(CustomerSubAreaMappingModel.ISOTHERBRAND, Boolean.FALSE);
        params.put("promoter", promoter);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
        if (CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();
    }

    @Override
    public List<LeadMasterModel> getAllLeadsToDate(Date start, Date end, List<SubAreaMasterModel> subAreaList) {
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        final Map<String, Object> params = new HashMap<>();
        String queryString = new String();
        if (currentUser instanceof EyDmsUserModel) {
            queryString = "SELECT {pk } FROM {" + LeadMasterModel._TYPECODE + "} where {brand}=?brand and {subAreaMaster} in (?subAreas)";
            params.put("subAreas", subAreaList);
        } else if (currentUser instanceof EyDmsCustomerModel) {
            queryString = "SELECT {pk} FROM {" + LeadMasterModel._TYPECODE + "} where {brand}=?brand and {createdBy}=?currentUser";
            params.put("currentUser", currentUser);
        }

        String dateClause = " and {creationtime}>=?start and {creationtime}<?end";
        params.put(BRAND, baseSiteService.getCurrentBaseSite());
        if (Objects.nonNull(start) && Objects.nonNull(end)) {
            params.put("start", start);
            params.put("end", end);
            queryString += dateClause;
        }
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<LeadMasterModel> searchResult = flexibleSearchService.search(query);
        if (CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }
        return new ArrayList<LeadMasterModel>();
    }

    @Override
    public List<NirmanMitraSalesHistoryModel> getMitraSalesDataForCustomer(String customerNo, BaseSiteModel brand, String transactionType) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final String queryString = "select {pk} from {NirmanMitraSalesHistory} where {toCustAccNumber}=?customerNo AND {brand}=?brand AND {transactionTypeDisp}=?transactionType";
        params.put("customerNo", customerNo);
        params.put(BRAND, brand);
        params.put("transactionType", transactionType);

       /* final StringBuilder builder = new StringBuilder("select sum({o.quantity}) from {OrderRequisition AS o} where {o.toCustomer} =?customerNo and {o:status}=?status and ").append(EyDmsDateUtility.getMtdClauseQuery("o:deliveredDate", params));
        params.put("customerNo", customerNo);
        params.put("status", RequisitionStatus.DELIVERED);*/
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<NirmanMitraSalesHistoryModel> searchResult = flexibleSearchService.search(query);
        if (CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();
    }

    @Override
    public NetworkRemovalModel getNetworkRemovalModelForCustomerCode(String code) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {NetworkRemoval} WHERE {code}=?code");
        params.put("code", code);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(NetworkRemovalModel.class));
        query.addQueryParameters(params);
        final SearchResult<NetworkRemovalModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
        else
            return null;
    }

    @Override
    public List<List<Object>> getLatitudeLongitudeOfProspectiveNetworkList(String code) {
        try {
            final Map<String, Object> params = new HashMap<>();

            String queryString = "select {sc:uid}, {sc:latitude},{sc:longitude} from {ProspectiveNetwork as pn JOIN " +
                    "EyDmsCustomer as sc on {pn:crmaccountid}={sc.uid}}" +
                    "where {pn:crmaccountid}=?code";
            params.put("code", code);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class, Double.class, Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<EyDmsCustomerModel> getInActiveCustomers(String dealerUserGroupUid, BaseSiteModel brand, List<SubAreaMasterModel> subAreas) {
        final Map<String, Object> params = new HashMap<>();
        String queryString = "Select {sc.pk} from {CustomerSubAreaMapping as csm join EyDmsCustomer as sc on {csm.eydmsCustomer}={sc.pk} join PrincipalGroupRelation as pgr on {pgr.source}={sc.pk} join PrincipalGroup as pg on {pgr.target}={pg.pk} join CreditAndOutstanding as co on {co.customerCode}={sc.customerNo} join SecurityDepositStatus as sds on {sds.pk}={co.securityDepositStatus}}";
        String whereCondtion = " where {pg.uid}=?userGroupId and {brand}=?brand and {sds.code}=?depositStsCode ";
        params.put("depositStsCode", "REFUNDED");
        params.put("userGroupId", dealerUserGroupUid);
        params.put(BRAND, brand);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString + whereCondtion);
        query.addQueryParameters(params);
        final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
        if (CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }

        return Collections.emptyList();
    }

    @Override
    public CounterVisitMasterModel getVisitIdByEyDmsCustomer(String customercode) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {cv:id} from {EyDmsCustomer as sc JOIN CounterVisitMaster as cv ON {sc:pk}={cv:eydmsCustomer}} where {sc:uid}=?customercode");
        params.put("customercode", customercode);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(CounterVisitMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<CounterVisitMasterModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty())) {
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
        } else
            return null;
    }

    @Override
    public List<EyDmsCustomerModel> getOnboardedCustomerTillDate(String userGroupId, String taluka, Date timestamp, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<>();
        String queryString = new String();
        String whereCondtion = new String();

        if (taluka.equalsIgnoreCase("ALL")) {
            queryString = "Select {sc.pk} from {CustomerSubAreaMapping as csm join EyDmsCustomer as sc on {csm.eydmsCustomer}={sc.pk} join PrincipalGroupRelation as pgr on {pgr.source}={sc.pk} join PrincipalGroup as pg on {pgr.target}={pg.pk} join SubAreamaster as sm on {sm.pk}={csm.subAreaMaster} }";
            whereCondtion = " where  {pg.uid}=?userGroupId and {brand}=?brand and {sc.dateOfJoining}>=?joiningDate ";

        } else {
            queryString = "Select {sc.pk} from {CustomerSubAreaMapping as csm join EyDmsCustomer as sc on {csm.eydmsCustomer}={sc.pk} join PrincipalGroupRelation as pgr on {pgr.source}={sc.pk} join PrincipalGroup as pg on {pgr.target}={pg.pk} join SubAreamaster as sm on {sm.pk}={csm.subAreaMaster} }";
            whereCondtion = " where  {sm.taluka} =?taluka and {pg.uid}=?userGroupId and {brand}=?brand and {sc.dateOfJoining}>=?joiningDate ";
        }

        params.put("joiningDate", timestamp);
        params.put("userGroupId", userGroupId);
        params.put(BRAND, brand);
        params.put("taluka", taluka);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString + whereCondtion);
        query.addQueryParameters(params);
        final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
        if (CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();
    }

    public double getCustomerTargetForDealer(String customerNo, String subArea, String month, String year) {
        SubAreaMasterModel subAreaMaster = territoryManagementService.getTerritoryById(subArea);
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {totalTarget} from {MonthlySales as m " +
                "JOIN DealerPlannedMonthlySales as dp on {dp:monthlySales}={m:pk}} " +
                "where " +
                "{dp:customerCode}=?customerCode " +
                "and {monthName}=?monthName " +
                "and {monthYear}=?monthYear " +
                "and {dp:subarea}=?subArea ");
        params.put("customerCode", customerNo);
        params.put("monthName", month);
        params.put("monthYear", year);
        params.put("subArea", subAreaMaster.getTaluka());
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        } else {
            return 0.0;
        }
    }

    @Override
    public List<List<Object>> getCounterLocationDetails(String dealerCode) {

        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder(LOCATION_COUNTER_DETAILS_POPULATE_QUERY);
            params.put("dealerCode", dealerCode);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class, Double.class, Double.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<NetworkRemovalModel> getNetworkRemovalForSubArea(List<SubAreaMasterModel> subareaMaster, BaseSiteModel site) {
        String queryString = "SELECT {pk} FROM {" + NetworkRemovalModel._TYPECODE + "} where {brand}=?brand and {subAreaMaster} in " +
                "(?subAreas)";
        final Map<String, Object> params = new HashMap<>();
        params.put(BRAND, site);
        params.put("subAreas", subareaMaster);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);

        final SearchResult<NetworkRemovalModel> searchResult = flexibleSearchService.search(query);
        if (CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();
    }

    @Override
    public Double getSalesHistoryData(BaseSiteModel brand, String transactionType, int month, int year) {
        List<EyDmsCustomerModel> eydmsCustomerModelList = new ArrayList<>();
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        if (currentUser instanceof EyDmsUserModel) {
            eydmsCustomerModelList = territoryManagementService.getRetailersForSubArea();
        } else if (currentUser instanceof EyDmsCustomerModel) {
            if (currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                eydmsCustomerModelList = territoryManagementService.getInfluencerListForDealer();
            } else if (currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                eydmsCustomerModelList = territoryManagementService.getInfluencerListForRetailer();
            }
        }
        //List<String> nirmanMitraCodeList = eydmsCustomerModelList.stream().map(EyDmsCustomerModel::getNirmanMitraCode).collect(Collectors.toList());
        final Map<String, Object> params = new HashMap<String, Object>();
        //sql.append("SELECT SUM({transactionQuantity}) FROM {NirmanMitraSalesHistory} WHERE {brand}=?brand AND {transactionTypeDisp}=?transactionType AND").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("transactionDate",month,year,params));
        /*sql.append("SELECT SUM({transactionQuantity}) FROM {NirmanMitraSalesHistory} WHERE {brand}=?brand AND {transactionTypeDisp}=?transactionType AND {toCustAccNumber} in (?nirmanMitraCodeList) AND").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("transactionDate",month,year,params));
        params.put("brand", brand);
        params.put("transactionType", transactionType);
        params.put("nirmanMitraCodeList",nirmanMitraCodeList);*/
        final StringBuilder builder = new StringBuilder("select sum({o.quantity}) from {OrderRequisition AS o} where  {o.toCustomer} IN (?cusUids) and {o:status}=?status and ").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("o.deliveredDate", month, year, params));
        params.put("cusUids", eydmsCustomerModelList);
        params.put("status", RequisitionStatus.DELIVERED);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    /*@Override
    public List<List<Object>> getSalesHistoryDataForDealer(EyDmsUserModel eydmsUser, BaseSiteModel brand, Date startDate, Date endDate) {
        return null;
    }*/

    @Override
    public Double getSalesHistoryDataForDealer(BaseSiteModel brand, CustomerCategory category, int month, int year) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT SUM({quantity}) FROM {SalesHistory} WHERE {customerCategory}=?category AND {brand}=?brand AND").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("invoiceDate", month, year, params));
        params.put("category", category);
        params.put("brand", brand);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    @Override
    public Double getActualTargetForSalesYTD(EyDmsUserModel eydmsUser, BaseSiteModel site, Date startDate, Date endDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}}  WHERE "
                + "{o:placedBy} = ?eydmsUser and {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null  and {oe:invoiceCreationDateAndTime} >= ?startDate and {oe:invoiceCreationDateAndTime} <= ?endDate");
        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("eydmsUser", eydmsUser);
        params.put("site", site);
        params.put("orderStatus", orderStatus);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    /*@Override
    public List<List<Object>> getSalesHistoryDataForDealerTaluka(EyDmsUserModel eydmsUser,BaseSiteModel brand,  Date startDate, Date endDate, String taluka) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT year({deliveredDate}), month({deliveredDate}),SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk} JOIN CustomerSubAreaMapping as cs on {cs.eydmsCustomer}={o.user} join SubAreamaster as sm on {sm.pk}={cs.subAreaMaster}}  WHERE "
                + "{o:placedBy} = ?eydmsUser and {o:versionID} IS NULL and {o:site} =?site and {oe:status}=?orderStatus and {oe:deliveredDate} >= ?startDate and {oe:deliveredDate} <= ?endDate and {sm.taluka}=?taluka group by year({deliveredDate}), month({deliveredDate}) order by year({deliveredDate}) desc, month({deliveredDate}) desc");
        OrderStatus orderStatus=OrderStatus.DELIVERED;
        params.put("eydmsUser",eydmsUser);
        params.put("site",brand);
        params.put("orderStatus",orderStatus);
        params.put("startDate",startDate);
        params.put("endDate",endDate);
        params.put("taluka",taluka);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(String.class,String.class,Double.class));
        final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult();
    }
*/

    public Double getSalesHistoryDataForDealerTaluka(BaseSiteModel brand, CustomerCategory category, int month, int year, String taluka) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT SUM({quantity}) FROM {SalesHistory} WHERE {customerCategory}=?category AND {brand}=?brand AND {taluka}=?taluka AND").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("invoiceDate", month, year, params));
        //final StringBuilder builder = new StringBuilder(" SELECT year({deliveredDate}), month({deliveredDate}),SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk} JOIN CustomerSubAreaMapping as cs on {cs.eydmsCustomer}={o.user} join SubAreamaster as sm on {sm.pk}={cs.subAreaMaster}}  WHERE "
        // + "{o:placedBy} = ?eydmsUser and {o:versionID} IS NULL and {o:site} =?site and {oe:status}=?orderStatus and {oe:deliveredDate} >= ?startDate and {oe:deliveredDate} <= ?endDate and {sm.taluka}=?taluka group by year({deliveredDate}), month({deliveredDate}) order by year({deliveredDate}) desc, month({deliveredDate}) desc");
        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("category", category);
        params.put("brand", brand);
        params.put("taluka", taluka);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    @Override
    public Double getSalesTargetForTaluka(String taluka, String customerType, int month, int year) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {salesTarget} FROM {TargetSales} WHERE {customerType}=?customerType AND {month}=?month AND {year}=?year");
        params.put("customerType", customerType);
        params.put("month", month + 1);
        params.put("year", year);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    @Override
    public double getSalesHistoryDataForTaluka(String taluka, BaseSiteModel brand, String transactionType, int month, int year) {
        List<EyDmsCustomerModel> eydmsCustomerModelList = new ArrayList<>();
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        if (currentUser instanceof EyDmsUserModel) {
            if (taluka.equalsIgnoreCase("ALL")) {
                eydmsCustomerModelList = territoryManagementService.getRetailersForSubArea();
            } else {
                eydmsCustomerModelList = territoryManagementService.getRetailersForSubArea(taluka);
            }
        } else if (currentUser instanceof EyDmsCustomerModel) {
            if (currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                eydmsCustomerModelList = territoryManagementService.getInfluencerListForDealer();
            } else if (currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                eydmsCustomerModelList = territoryManagementService.getInfluencerListForRetailer();
            }
        }
        List<String> nirmanMitraCodeList = eydmsCustomerModelList.stream().map(EyDmsCustomerModel::getNirmanMitraCode).collect(Collectors.toList());
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder sql = new StringBuilder();
        //AND {toCustAccNumber} in (?nirmanMitraCodeList)
        sql.append("SELECT SUM({transactionQuantity}) FROM {NirmanMitraSalesHistory} WHERE {brand}=?brand AND {transactionTypeDisp}=?transactionType AND {taluka}=?taluka  AND {toCustAccNumber} in (?nirmanMitraCodeList)").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("transactionDate", month, year, params));
        params.put("brand", brand);
        params.put("transactionType", transactionType);
        params.put("taluka", taluka);
        params.put("nirmanMitraCodeList", nirmanMitraCodeList);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    @Override
    public Integer getNewInfluencerCountMTD(EyDmsCustomerModel eydmsCustomer, BaseSiteModel baseSite, Date startDate, Date endDate, Date doj, String fromCustomerType) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT count({cus:influencer}) FROM {CustomersInfluencerMap as cus JOIN EyDmsCustomer as sc on {cus.influencer} ={sc.pk}} WHERE {cus:fromCustomer}=?currentUser AND {cus:fromCustomerType}=?fromCustomerType AND {cus:active}=?active AND {cus:brand}=?brand and {sc:dateOfJoining}>=?doj and {sc:lastLiftingDate}>=?startDate and {sc:lastLiftingDate}<=?endDate");
        params.put("fromCustomerType", fromCustomerType);
        boolean active = Boolean.TRUE;
        params.put("active", active);
        params.put("currentUser", eydmsCustomer);
        params.put("brand", baseSite);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("doj", doj);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Integer.class));
        query.addQueryParameters(params);
        final SearchResult<Integer> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0;
        else
            return 0;
    }

    @Override
    public List<EyDmsCustomerModel> getRetailerCardCountMTD(EyDmsCustomerModel eydmsCustomer, BaseSiteModel baseSite, Date startDate, Date endDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {cus.retailer} from {DealerRetailerMap as cus join eydmsCustomer as sc on {cus.retailer} ={sc.pk} } WHERE {cus:dealer}=?currentUser AND {cus:active}=?active AND {cus:brand}=?brand and {sc:lastLiftingDate}>=?startDate and {sc:lastLiftingDate}<=?endDate");
        boolean active = Boolean.TRUE;
        params.put("active", active);
        params.put("currentUser", eydmsCustomer);
        params.put("brand", baseSite);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder);
        query.addQueryParameters(params);
        final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
        if (CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();
    }

    @Override
    public List<EyDmsCustomerModel> getInfluencerCardCountMTD(EyDmsCustomerModel eydmsCustomer, BaseSiteModel baseSite, Date startDate, Date endDate, String fromCustomerType) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {cus:influencer} FROM {CustomersInfluencerMap as cus JOIN EyDmsCustomer as sc on {cus.influencer} ={sc.pk}} WHERE {cus:fromCustomer}=?currentUser AND {cus:fromCustomerType}=?fromCustomerType AND {cus:active}=?active AND {cus:brand}=?brand and {sc:lastLiftingDate}>=?startDate and {sc:lastLiftingDate}<=?endDate");
        boolean active = Boolean.TRUE;
        params.put("active", active);
        params.put("currentUser", eydmsCustomer);
        params.put("brand", baseSite);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("fromCustomerType", fromCustomerType);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder);
        query.addQueryParameters(params);
        final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
        if (CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();
    }

    @Override
    public Double getActualTargetForSalesYTDTaluka(EyDmsUserModel salesOfficer, BaseSiteModel site, Date startDate, Date endDate, String taluka) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk} JOIN CustomerSubAreaMapping as cs on {cs.eydmsCustomer}={o.user} join SubAreamaster as sm on {sm.pk}={cs.subAreaMaster}}  WHERE "
                + "{o:placedBy} = ?eydmsUser and {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and {oe:invoiceCreationDateAndTime} >= ?startDate and {oe:invoiceCreationDateAndTime} <= ?endDate and {sm.taluka}=?taluka");
        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("eydmsUser", salesOfficer);
        params.put("site", site);
        params.put("orderStatus", orderStatus);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("taluka", taluka);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }


    @Override
    public Integer getNewRetailerCountMTD(EyDmsCustomerModel eydmsCustomer, BaseSiteModel baseSite, Date startDate, Date endDate, Date doj) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select count({cus.retailer}) from {DealerRetailerMap as cus join eydmsCustomer as sc on {cus.retailer} ={sc.pk} } WHERE {cus:dealer}=?currentUser AND {cus:active}=?active AND {cus:brand}=?brand and {sc:dateOfJoining}>=?doj and {sc:lastLiftingDate}>=?startDate and {sc:lastLiftingDate}<=?endDate");
        boolean active = Boolean.TRUE;
        params.put("active", active);
        params.put("currentUser", eydmsCustomer);
        params.put("brand", baseSite);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("doj", doj);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Integer.class));
        query.addQueryParameters(params);
        final SearchResult<Integer> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0;
        else
            return 0;
    }


    @Override
    public Double getSalesTarget(String customerType, int month, int year) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {salesTarget} FROM {TargetSales} WHERE {customerType}=?customerType AND {month}=?month AND {year}=?year");
        params.put("customerType", customerType);
        params.put("month", month + 1);
        params.put("year", year);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }


    @Override
    public List<NetworkAdditionPlanModel> getNetworkAdditionPlanSummary(LeadType leadType, List<EyDmsUserModel> soForUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final String queryString = "SELECT {pk} FROM {NetworkAdditionPlan as np join LeadType as l on {np.applicableTo}={l.pk}} WHERE {np.raisedBy} IN (?soForUser) and {l.code}=?leadType ";
        params.put("leadType", leadType.getCode());
        params.put("soForUser", soForUser);
        FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.setResultClassList(Arrays.asList(NetworkAdditionPlanModel.class));
        query.addQueryParameters(params);
        final SearchResult<NetworkAdditionPlanModel> searchResult = flexibleSearchService.search(query);
        List<NetworkAdditionPlanModel> result = searchResult.getResult();
        if (Objects.nonNull(result)) {
            return result;
        }
        return Collections.emptyList();

    }

    @Override
    public List<NetworkAdditionPlanModel> getProposedPlansBySO(LeadType leadType, List<EyDmsUserModel> soForUser, String filter) {

        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {NetworkAdditionPlan as np join LeadType as l on {np.applicableTo}={l.pk} join EyDmsUser as sc on {np.raisedBy}={sc.pk} join SubAreaMaster as sm on {np.subAreaMaster}={sm.pk}} WHERE {np.raisedBy} IN (?soForUser) and {l.code}=?leadType ");
        params.put("leadType", leadType.getCode());
        params.put("soForUser", soForUser);
        if (filter != null) {
            String filterKey = "%".concat(filter.toUpperCase()).concat("%");
            builder.append(" AND ( UPPER({sc.uid}) like ?filter OR  UPPER({sm.district}) like ?filter OR UPPER({sc.mobileNumber}) like ?filter OR " +
                    "UPPER({sc.name}) like ?filter )");
            params.put("filter", filterKey);
        }

        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(NetworkAdditionPlanModel.class));
        query.addQueryParameters(params);
        final SearchResult<NetworkAdditionPlanModel> searchResult = flexibleSearchService.search(query);
        List<NetworkAdditionPlanModel> result = searchResult.getResult();
        if (Objects.nonNull(result)) {
            return result;
        }
        return Collections.emptyList();
    }

    @Override
    public Double getSalesQuantityForRetailerMTD(EyDmsCustomerModel customerNo, BaseSiteModel currentBaseSite) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select sum({o.quantity}) from {OrderRequisition AS o} where {o.toCustomer} =?customerNo and {o:status}=?status and ").append(EyDmsDateUtility.getMtdClauseQuery("o:deliveredDate", params));
        params.put("customerNo", customerNo);
        params.put("status", RequisitionStatus.DELIVERED);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        } else {
            return 0.0;
        }
    }

    @Override
    public Double getSalesQuantityForRetailerMonthYear(EyDmsCustomerModel customerNo, BaseSiteModel currentBaseSite, int month, int year) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select sum({o.quantity}) from {OrderRequisition AS o} where {o.toCustomer} =?customerNo and {o:status}=?status and ").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("o:deliveredDate", month, year, params));
        params.put("customerNo", customerNo);
        params.put("status", RequisitionStatus.DELIVERED);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        } else {
            return 0.0;
        }
    }


    @Override
    public NetworkAdditionPlanModel getNetworkAdditionPlan(EyDmsUserModel eydmsUserModel) {
        final Map<String, Object> params = new HashMap<>();
        String queryString = "SELECT {pk} FROM {NetworkAdditionPlan as np } WHERE {np.raisedBy}=?eydmsUserModel";
        params.put("eydmsUserModel", eydmsUserModel);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<NetworkAdditionPlanModel> searchResult = flexibleSearchService.search(query);
        if (CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult().get(0);
        }

        return null;
    }

    @Override
    public NetworkAdditionPlanModel getNetworkAdditionPlan(String id) {
        final Map<String, Object> params = new HashMap<>();
        String queryString = "SELECT {pk} FROM {NetworkAdditionPlan as np } WHERE {np.id}=?id";
        params.put("id", id);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<NetworkAdditionPlanModel> searchResult = flexibleSearchService.search(query);
        if (CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult().get(0);
        }

        return null;
    }

    @Override
    public Double getSalesQuantityForInfluencerMTD(EyDmsCustomerModel customerNo, BaseSiteModel currentBaseSite) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select sum({o.quantity}) from {PointRequisition AS o} where {o.requestRaisedFor} =?customerNo and {o:status}=?status and ").append(EyDmsDateUtility.getMtdClauseQuery("o:deliveryDate", params));
        params.put("customerNo", customerNo);
        params.put("status", PointRequisitionStatus.APPROVED);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        } else {
            return 0.0;
        }
    }

    @Override
    public Double getSalesQuantityForInfluencerMonthYear(EyDmsCustomerModel customerNo, BaseSiteModel currentBaseSite, int month, int year) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select sum({o.quantity}) from {PointRequisition AS o} where {o.requestRaisedFor} =?customerNo and {o:status}=?status and ").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("o:deliveryDate", month, year, params));
        params.put("customerNo", customerNo);
        params.put("status", PointRequisitionStatus.APPROVED);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        } else {
            return 0.0;
        }

    }

    @Override
    public NetworkAdditionPlanModel getProposedPlanViewForTSMRH( String status, String id) {
        final Map<String, Object> params = new HashMap<>();
        final StringBuilder builder = new StringBuilder(" SELECT {pk} FROM {NetworkAdditionPlan as np } WHERE {np.id}=?id ");
       // String queryString = "SELECT {pk} FROM {NetworkAdditionPlan as np } WHERE {np.raisedBy}=?eydmsUserModel and {np.subAreaMaster}=?subArea";
        //params.put("eydmsUserModel", eydmsUserModel);
       // params.put("subArea", subArea);
        params.put("id",id);
//        if(currentUser.getUserType().getCode().equals("TSM")){
//
//        }
//        else if(currentUser.getUserType().getCode().equals("RH")){
//
//        }
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        final SearchResult<NetworkAdditionPlanModel> searchResult = flexibleSearchService.search(query);
        if (CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult().get(0);
        }

        return null;
    }

    @Override

    public SubAreaMasterModel getSubareaForSOString(String subArea) {
        final Map<String, Object> params = new HashMap<>();
        String queryString = "SELECT {pk} FROM {SubAreaMaster} WHERE {pk}=?subArea";
        params.put("subArea", subArea.toString());
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<SubAreaMasterModel> searchResult = flexibleSearchService.search(query);
        if (CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult().get(0);
        }

        return null;

    }

    public SearchPageData<NetworkAdditionPlanModel> getNetworkAdditionPlanSummaryForTSMRH(SearchPageData searchPageData, LeadType leadType,List<String> statuses,boolean isPendingForApproval,List<EyDmsUserModel> soForUser, List<EyDmsUserModel> tsmForUser, EyDmsUserModel currentUser) {
        final Map<String, Object> params = new HashMap<String, Object>();

        final StringBuilder builder = new StringBuilder(" SELECT {pk} FROM {NetworkAdditionPlan as np join LeadType as l on {np.applicableTo}={l.pk}} ");
        if(currentUser.getUserType().getCode().equals("TSM")){
        //    builder.append(" WHERE {np.raisedBy} IN (?soForUser) and {l.code}=?leadType and {np:approvalLevel}=?approvalLevel ");
            builder.append(" WHERE {np.raisedBy} IN (?soForUser) and {l.code}=?leadType  ");
            params.put("soForUser",soForUser);
            /*TerritoryLevels approvalLevel = TerritoryLevels.DISTRICT;
            params.put("approvalLevel",approvalLevel);*/
           /* if(isPendingForApproval)
            {
                builder.append(" and {np:approvalLevel}=?approvalLevel");
                params.put("approvalLevel",approvalLevel);
            }*/

        }
        else if(currentUser.getUserType().getCode().equals("RH")){
//            builder.append(" WHERE {np.revisedBy} IN (?tsmForUser) and {l.code}=?leadType ");
        //    builder.append(" WHERE {np.approvedBy} IN (?tsmForUser) and {l.code}=?leadType and {np:approvalLevel}=?approvalLevel");
            builder.append(" WHERE {np.approvedBy} IN (?tsmForUser) and {l.code}=?leadType ");
            params.put("tsmForUser", tsmForUser);
            /*TerritoryLevels approvalLevel = TerritoryLevels.REGION;
            params.put("approvalLevel",approvalLevel);*/
           /* if(isPendingForApproval)
            {
                builder.append(" and {np:approvalLevel}=?approvalLevel");
                params.put("approvalLevel",approvalLevel);
            }*/

        }
        else if(currentUser.getUserType().getCode().equals("SO")){
            TerritoryLevels approvalLevel = TerritoryLevels.SUBAREA;
            if(isPendingForApproval)
            {
                builder.append(" and {np:approvalLevel}=?approvalLevel");
                params.put("approvalLevel",approvalLevel);
            }

        }
        params.put("leadType", leadType.getCode());
        if(statuses!=null && !statuses.isEmpty()){
            List<NetworkAdditionStatus> networkAdditionStatus = new ArrayList<>();
            for(String status : statuses){
                networkAdditionStatus.add(NetworkAdditionStatus.valueOf(status));
            }
            builder.append(" and {np:status} in (?status)");
            params.put("status",networkAdditionStatus);
        }

        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(NetworkAdditionPlanModel.class));
        query.addQueryParameters(params);
        parameter.setFlexibleSearchQuery(query);
        return paginatedFlexibleSearchService.search(parameter);

    }

    public  Integer getApprovedAdditionSumForTSMRH(LeadType leadType,List<EyDmsUserModel> soForUser, List<EyDmsUserModel> tsmForUser, EyDmsUserModel currentUser) {
        final Map<String, Object> params = new HashMap<String, Object>();

        final StringBuilder builder = new StringBuilder(" SELECT sum({systemProposed}) FROM {NetworkAdditionPlan as np join LeadType as l on {np.applicableTo}={l.pk}}");

        if(currentUser.getUserType().getCode().equals("TSM")){
            builder.append(" WHERE  {np.status}=?status and {np.raisedBy} IN (?soForUser) and {l.code}=?leadType ");
            params.put("status",NetworkAdditionStatus.APPROVED_BY_TSM);
            params.put("soForUser",soForUser);
            TerritoryLevels approvalLevel = TerritoryLevels.DISTRICT;

        }
        else if(currentUser.getUserType().getCode().equals("RH")){
            builder.append(" WHERE {np.status}=?status and {np.approvedBy} IN (?currentUser) and {l.code}=?leadType ");
            params.put("status",NetworkAdditionStatus.APPROVED_BY_RH);
            params.put("currentUser", currentUser);

        }
        else if(currentUser.getUserType().getCode().equals("SO")){
            TerritoryLevels approvalLevel = TerritoryLevels.SUBAREA;

        }
        params.put("leadType", leadType.getCode());

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Integer.class));
        query.addQueryParameters(params);
        final SearchResult<Integer> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0;
        else
            return 0;

    }


    @Override
    public LeadMasterModel findItemByUidParam(String leadId) {
        final Map<String, Object> params = new HashMap<>();
        String queryString = String.format("select {pk} from {LeadMaster} where {leadId}=?leadId");
        params.put("leadId", leadId);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<LeadMasterModel> searchResult = flexibleSearchService.search(query);
        if (CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult().get(0);
        }
        return null;
    }

    @Override
    public List<NetworkAdditionPlanModel> getCountOfProposedPlanSummaryListForRH(LeadType leadType, List<EyDmsUserModel> tsmForUser) {


        final Map<String, Object> params = new HashMap<String, Object>();
        final String queryString = "SELECT {pk} FROM {NetworkAdditionPlan as np join LeadType as l on {np.applicableTo}={l.pk}} WHERE {np.revisedBy} IN (?tsmForUser) and {l.code}=?leadType ";
        params.put("leadType", leadType.getCode());
        params.put("tsmForUser", tsmForUser);
        FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.setResultClassList(Arrays.asList(NetworkAdditionPlanModel.class));
        query.addQueryParameters(params);
        final SearchResult<NetworkAdditionPlanModel> searchResult = flexibleSearchService.search(query);
        List<NetworkAdditionPlanModel> result = searchResult.getResult();
        if (Objects.nonNull(result)) {
            return result;
        }
        return Collections.emptyList();
    }

    @Override
    public List<NetworkAdditionPlanModel> getNetworkAdditionSummaryForRH(LeadType leadType) {
        LOG.info("LeadType " + leadType);
        final Map<String, Object> params = new HashMap<>();
        String queryString = "SELECT {pk} FROM {NetworkAdditionPlan as np join LeadType as l on {np.applicableTo}={l.pk}} WHERE {l.code}=?leadType and {np.status}=?status ";
        params.put("leadType",leadType.getCode());
        params.put("status",NetworkAdditionStatus.APPROVED_BY_TSM);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.setResultClassList(Arrays.asList(NetworkAdditionPlanModel.class));
        query.addQueryParameters(params);
        final SearchResult<NetworkAdditionPlanModel> searchResult = flexibleSearchService.search(query);
        List<NetworkAdditionPlanModel> result = searchResult.getResult();
        if (Objects.nonNull(result)) {
            return result;
        }
        return Collections.emptyList();
    }


    @Override
    public Integer getLeadsGeneratedCountForInfluencerMtd(EyDmsCustomerModel currentUser, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select count({pk}) from {LeadMaster} where {brand}=?brand and {createdBy}=?currentUser and " + EyDmsDateUtility.getMtdClauseQuery("creationtime", params));
        params.put("brand", brand);
        params.put("currentUser", currentUser);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Integer.class));
        query.addQueryParameters(params);
        LOG.info("Query used to get Leads Generated Count for Influencer MTD" + query);
        final SearchResult<Integer> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0;
        else
            return 0;
    }

    @Override
    public Integer getLeadsGeneratedCountForInfluencerYtd(EyDmsCustomerModel currentUser, BaseSiteModel brand, Date startDate, Date endDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select count({pk}) from {LeadMaster} where {brand}=?brand and {createdBy}=?currentUser and {creationtime} >= ?startDate and {creationtime} <= ?endDate");
        params.put("brand", brand);
        params.put("currentUser", currentUser);
        params.put("startDate",startDate);
        params.put("endDate",endDate);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Integer.class));
        query.addQueryParameters(params);
        LOG.info("Query used to get Leads Generated Count for Influencer YTD" + query);
        final SearchResult<Integer> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0;
        else
            return 0;
    }

}
