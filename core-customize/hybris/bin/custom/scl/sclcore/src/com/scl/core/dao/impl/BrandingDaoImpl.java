    package com.scl.core.dao.impl;

import com.scl.core.constants.GeneratedSclCoreConstants;
import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.BrandingDao;
import com.scl.core.enums.BrandingRequestStatus;
import com.scl.core.enums.BrandingSiteType;
import com.scl.core.enums.DealerRequestStatus;
import com.scl.core.model.*;
import com.scl.core.services.TerritoryManagementService;
import com.scl.core.services.TerritoryMasterService;
import com.scl.core.utility.SclDateUtility;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BrandingDaoImpl implements BrandingDao {
    @Resource
    FlexibleSearchService flexibleSearchService;
    @Resource
    UserService userService;
    @Resource
    TerritoryManagementService territoryManagementService;
    @Autowired
    TerritoryMasterService territoryMasterService;
    @Resource
    PaginatedFlexibleSearchService paginatedFlexibleSearchService;

    //select {uid},{name},{customerNo} from {SclCustomer} where {uid}='17320838' or {name}='17320838' or {customerNo}='17320838'
   // private final String COUNTER_DETAILS_POPULATE_QUERY="SELECT {sc:PK} FROM {SclCustomer as sc} WHERE {sc:uid}=?searchKeyword OR {sc:name}=?searchKeyword OR {sc:customerNo}=?searchKeyword";
    private final String COUNTER_DETAILS_POPULATE_QUERY="SELECT {sc:uid},{sc:name},{sc:mobileNumber},{add:streetNumber},{add:streetName},{add:district},{add:city},{add:state} " +
            "from {SCLCustomer as sc JOIN Address as add on {sc:pk}={add:owner}} " +
            "where {sc:uid}=?searchKeyword or {sc:name}=?searchKeyword or " +
            "{sc:mobileNumber}=?searchKeyword or {sc:customerNo}=?searchKeyword";

    private final String COUNTER_DETAILS_POPULATE_QUERY_NEW="SELECT {sc:pk} from {SCLCustomer as sc} where {sc:uid}=?searchKeyword or {sc:name}=?searchKeyword or " +
            "{sc:mobileNumber}=?searchKeyword or {sc:customerNo}=?searchKeyword";
    private final String BRAND_TRACKING_REQUEST_DETAILS_QUERY="SELECT {bts:PK} FROM {BrandingTrackingStatus as bts JOIN EnumerationValue AS enum" +
            " ON {enum:pk}={bts:requestStatus} JOIN BrandingRequestDetails as brd ON {brd:requisitionNumber}={bts:requestNumber}} " +
            "where {bts.trackStatus}=?requestSta and {bts.requestNumber}=?requestNumber";

    private final String BRAND_TRACKING_HISTORY_DETAILS_QUERY="Select {bts:pk} from {BrandingTrackStatusStageGate as bts JOIN " +
            "EnumerationValue AS enum ON {enum:pk}={bts:requestStatus} JOIN " +
            "BrandingRequestDetails as brd ON {brd:requisitionNumber}={bts:requestNumber}} " +
            "where {bts:requestNumber}=?requestNumber";

    private final String BRANDING_REQUEST_DETAILS_MODEL_QUERY="SELECT {brd:PK} FROM {BrandingRequestDetails as brd} order by {brd:requestRaisedDate} desc";
    private final String GET_BRANDING_REQUEST_DETAILS_UPLOAD_QUERY="SELECT {brd:PK} FROM {BrandingRequestDetails as brd} " +
            "where {brd:requisitionNumber}=?reqNumber";
    private final String GET_BRANDING_TRACKING_DETAILS_BY_REQUEST_NUMBER="SELECT {brd:PK} FROM {BrandingTrackingStatus as brd} " +
            "where {brd:requestNumber}=?reqNumber";

    private final String GET_ACTIVITY_REQUEST_DETAILS_UPLOAD_QUERY="SELECT {brd:PK} FROM {BrandingActivityVerfication as brd} " +
            "where {brd:requisitionNumber}=?reqNumber";
    private final String ACTIVITY_VERIFICATION_DETAILS_BY_REQUEST_NUMBER="SELECT {bav:PK} FROM {BrandingActivityVerfication as bav JOIN BrandingRequestDetails as brd " +
            "on {brd:requisitionNumber}={bav:requestNumber}} " +
            "where {bav:requestNumber}=?requisitionNumber";

    public FlexibleSearchService getFlexibleSearchService() {
        return flexibleSearchService;
    }

    public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }

    @Override
    public List<List<Object>> getCounterDetailsForPointOfSale(String searchKeyWord) {
        try{
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(COUNTER_DETAILS_POPULATE_QUERY);
        params.put("searchKeyword", searchKeyWord);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class,String.class,String.class,String.class,String.class,String.class,String.class,String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }
    public SclCustomerModel getCounterDetailsForPointOfSaleNew(String searchKeyWord)  {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder(COUNTER_DETAILS_POPULATE_QUERY_NEW);
            params.put("searchKeyword", searchKeyWord);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Collections.singletonList(SclCustomerModel.class));
            final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
            return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;

    }
    @Override
    public List<BrandingTrackStatusStageGateModel> getBrandingTrackHistoryDetails(String requestNumber) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(BRAND_TRACKING_HISTORY_DETAILS_QUERY);
        params.put("requestNumber",requestNumber);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(BrandingTrackStatusStageGateModel.class));
        final SearchResult<BrandingTrackStatusStageGateModel> searchResult = flexibleSearchService.search(query);
        List<BrandingTrackStatusStageGateModel> result = searchResult.getResult();
        return result!=null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public List<BrandingRequestDetailsModel> viewBrandingRequestDetails() {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(BRANDING_REQUEST_DETAILS_MODEL_QUERY);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(BrandingRequestDetailsModel.class));
        final SearchResult<BrandingRequestDetailsModel> searchResult = flexibleSearchService.search(query);
        List<BrandingRequestDetailsModel> result = searchResult.getResult();
        return result!=null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public SearchPageData<BrandingRequestDetailsModel> getBrandingRequestDetails(String filter, String startDate, String endDate, List<String> requestSta, List<String> brandingSiteType,SearchPageData searchPageData) {
        final Map<String, Object> params = new HashMap<String, Object>();
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        final StringBuilder builder = new StringBuilder("SELECT {brd:PK} FROM {BrandingRequestDetails as brd} WHERE {brd:requisitionNumber}!='0' ");
        if(currentUser instanceof SclUserModel) {
          /*  if (currentUser.getUserType().getCode().equalsIgnoreCase("TSM")) {
                builder.append(" AND {brd.districtMaster} in (?districtList)  ");
                params.put("districtList", territoryManagementService.getCurrentDistrict());
            }
            if (currentUser.getUserType().getCode().equalsIgnoreCase("RH")) {
                builder.append(" AND {brd.regionMaster} in (?regionList) ");
                params.put("regionList", territoryManagementService.getCurrentRegion());
            }
            if (currentUser.getUserType().getCode().equalsIgnoreCase("SO")) {*/
                builder.append(" AND {brd.subAreaMaster} in (?subAreaList) ");
                params.put("subAreaList", territoryManagementService.getTerritoriesForSO());


           /* //Added Territory master code
            List<TerritoryMasterModel> territoriesForSO = territoryMasterService.getTerritoriesForSO();
            if(CollectionUtils.isNotEmpty(territoriesForSO)) {
                builder.append(" AND {brd.territoryMaster} in (?territoryList) ");
                params.put("territoryList", territoriesForSO);
            }
            */

        }
        if (currentUser instanceof SclCustomerModel) {
           /* if ((currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) ||
                    ((currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))))) {
                builder.append(" AND {brd.requestRaisedBy} = ?currentUser ");
                params.put("currentUser", currentUser);
            }*/
            if ((currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) ||
                    ((currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))))) {
                builder.append(" AND ({brd.counterCode} = ?currentUserId OR {brd.requestRaisedBy} = ?currentUser )");
                params.put("currentUserId", currentUser.getUid());
                params.put("currentUser", currentUser);
            }
        }

        if(filter!=null){
            String filterKey= "%".concat(filter.toUpperCase()).concat("%");
            builder.append(" AND ( UPPER({brd:counterName}) like ?filter OR UPPER({brd:counterCode}) like ?filter OR " +
                    "UPPER({brd:requisitionNumber}) like ?filter OR " +
                    "UPPER({brd:primaryContactNumber}) like ?filter OR " +
                    "UPPER({brd:counterErpCustNo}) like ?filter ) ");
            params.put("filter", filterKey);
        }
        if ((currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) ||
                ((currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))))) {

            /*List<DealerRequestStatus> statuses=new ArrayList<>();
            if(requestSta!=null && !requestSta.isEmpty()){
                List<BrandingRequestStatus> reqStatus=new ArrayList<>();
                 DealerRequestStatus reqStatus=null;
                for (String requestStatus : requestSta) {
                    if(requestStatus.compareTo(DealerRequestStatus.PENDING.getCode())==0)
                        reqStatus= DealerRequestStatus.PENDING;
                    else if(requestStatus.compareTo(DealerRequestStatus.APPROVED.getCode())==0)
                        reqStatus= DealerRequestStatus.APPROVED;
                    else if(requestStatus.compareTo(DealerRequestStatus.COMPLETED.getCode())==0)
                        reqStatus= DealerRequestStatus.COMPLETED;
                    else if(requestStatus.compareTo(DealerRequestStatus.REJECTED.getCode())==0)
                        reqStatus= DealerRequestStatus.REJECTED;
                    else if(requestStatus.compareTo(DealerRequestStatus.CANCELLED.getCode())==0)
                        reqStatus= DealerRequestStatus.CANCELLED;
                    statuses.add(reqStatus);
                    }
                    if(CollectionUtils.isNotEmpty(statuses)) {
                    builder.append(" AND {brd:dealerRequestStatus} in (?statuses) ");
                    params.put("statuses", statuses);
                }*/

            if(requestSta!=null && !requestSta.isEmpty()){
                List<BrandingRequestStatus> reqStatus=new ArrayList<>();
                if(requestSta.get(0)!=null && requestSta.get(0).toUpperCase().contains("REJECTED")) {
                        builder.append(" AND {brd:requestStatus} in (?rejectedList) ");
                        reqStatus.add(BrandingRequestStatus.LBT_REJECTED);
                        reqStatus.add(BrandingRequestStatus.REQUEST_REJECTED);
                        reqStatus.add(BrandingRequestStatus.NSH_REJECTED);
                        reqStatus.add(BrandingRequestStatus.CBT_REJECTED);
                        params.put("rejectedList",reqStatus);
                    }
                    if(requestSta.get(0)!=null && requestSta.get(0).toUpperCase().contains("CANCELLED")) {
                        builder.append(" AND {brd:requestStatus} in (?cancelledList) ");
                        reqStatus.add(BrandingRequestStatus.REQUISITION_CANCELLED);
                        params.put("cancelledList",reqStatus);
                    }
                else if(requestSta.get(0)!=null && requestSta.get(0).toUpperCase().contains("PENDING")) {
                    builder.append(" AND {brd:requestStatus} not in (?pendingList) and {brd:nshApprovedDate} is null ");
                    reqStatus.add(BrandingRequestStatus.REQUISITION_CANCELLED);
                    reqStatus.add(BrandingRequestStatus.REQUEST_REJECTED);
                    reqStatus.add(BrandingRequestStatus.LBT_REJECTED);
                    reqStatus.add(BrandingRequestStatus.CBT_REJECTED);
                    reqStatus.add(BrandingRequestStatus.REQUEST_REJECTED);
                    params.put("pendingList", reqStatus);
                }
                else if(requestSta.get(0)!=null && requestSta.get(0).toUpperCase().contains("APPROVED")) {
                    builder.append(" AND {brd:requestStatus} not in (?approvedList) and {brd:nshApprovedDate} is not null and {brd:invoiceUploadedDate} is null ");
                    reqStatus.add(BrandingRequestStatus.REQUISITION_CANCELLED);
                    reqStatus.add(BrandingRequestStatus.REQUEST_REJECTED);
                    reqStatus.add(BrandingRequestStatus.LBT_REJECTED);
                    reqStatus.add(BrandingRequestStatus.CBT_REJECTED);
                    reqStatus.add(BrandingRequestStatus.REQUEST_REJECTED);
                    params.put("approvedList", reqStatus);
                }
                else if(requestSta.get(0)!=null && requestSta.get(0).toUpperCase().contains("COMPLETED")) {
                    reqStatus.add(BrandingRequestStatus.REQUISITION_CANCELLED);
                    reqStatus.add(BrandingRequestStatus.REQUEST_REJECTED);
                    reqStatus.add(BrandingRequestStatus.LBT_REJECTED);
                    reqStatus.add(BrandingRequestStatus.CBT_REJECTED);
                    reqStatus.add(BrandingRequestStatus.REQUEST_REJECTED);
                    params.put("approvedList", reqStatus);
                    builder.append(" AND {brd:requestStatus} not in (?approvedList) and {brd:invoiceUploadedDate} is not null ");
                }
            }
        }
        else {
            List<BrandingRequestStatus> statuses = new ArrayList<>();
            if (requestSta != null && !requestSta.isEmpty()) {
                BrandingRequestStatus reqStatus = null;
                for (String requestStatus : requestSta) {
                    if (requestStatus.compareTo(BrandingRequestStatus.REQUEST_RAISED.getCode()) == 0)
                        reqStatus = BrandingRequestStatus.REQUEST_RAISED;
                    if (requestStatus.compareTo(BrandingRequestStatus.PENDING.getCode()) == 0)
                        reqStatus = BrandingRequestStatus.PENDING;
                    else if (requestStatus.compareTo(BrandingRequestStatus.ACTIVITY_VERIFIED.getCode()) == 0)
                        reqStatus = BrandingRequestStatus.ACTIVITY_VERIFIED;
                    else if (requestStatus.compareTo(BrandingRequestStatus.REQUISITION_RAISED.getCode()) == 0)
                        reqStatus = BrandingRequestStatus.REQUISITION_RAISED;
                    else if (requestStatus.compareTo(BrandingRequestStatus.LBT_REJECTED.getCode()) == 0)
                        reqStatus = BrandingRequestStatus.LBT_REJECTED;
                    else if (requestStatus.compareTo(BrandingRequestStatus.LBT_APPROVED.getCode()) == 0)
                        reqStatus = BrandingRequestStatus.LBT_APPROVED;
                    else if (requestStatus.compareTo(BrandingRequestStatus.CBT_APPROVED.getCode()) == 0)
                        reqStatus = BrandingRequestStatus.CBT_APPROVED;
                    else if (requestStatus.compareTo(BrandingRequestStatus.CBT_REJECTED.getCode()) == 0)
                        reqStatus = BrandingRequestStatus.CBT_REJECTED;
                    else if (requestStatus.compareTo(BrandingRequestStatus.NSH_APPROVED.getCode()) == 0)
                        reqStatus = BrandingRequestStatus.NSH_APPROVED;
                    else if (requestStatus.compareTo(BrandingRequestStatus.NSH_REJECTED.getCode()) == 0)
                        reqStatus = BrandingRequestStatus.NSH_REJECTED;
                    else if (requestStatus.compareTo(BrandingRequestStatus.REQUEST_REJECTED.getCode()) == 0)
                        reqStatus = BrandingRequestStatus.REQUEST_REJECTED;
                    else if (requestStatus.compareTo(BrandingRequestStatus.REQUISITION_CANCELLED.getCode()) == 0)
                        reqStatus = BrandingRequestStatus.REQUISITION_CANCELLED;
                    statuses.add(reqStatus);
                }
                if (CollectionUtils.isNotEmpty(statuses)) {
                    builder.append(" AND {brd:requestStatus} in (?statuses) ");
                    params.put("statuses", statuses);
                }
            }
        }
        List<BrandingSiteType> brandingSiteTypes=new ArrayList<>();
        if(brandingSiteType!=null && !brandingSiteType.isEmpty()){
            BrandingSiteType siteType=null;
            for (String site : brandingSiteType) {
                if(site.compareTo(BrandingSiteType.POINT_OF_SALE.getCode())==0)
                    siteType= BrandingSiteType.POINT_OF_SALE;
                else if(site.compareTo(BrandingSiteType.OUTDOORS.getCode())==0)
                    siteType= BrandingSiteType.OUTDOORS;
                else if(site.compareTo(BrandingSiteType.DEALER_COSTSHARING_BRANDING.getCode())==0)
                    siteType= BrandingSiteType.DEALER_COSTSHARING_BRANDING;
                brandingSiteTypes.add(siteType);
            }
            if(CollectionUtils.isNotEmpty(brandingSiteTypes)) {
                builder.append(" AND {brd:brandSiteType} in (?brandingSiteTypes) ");
                params.put("brandingSiteTypes", brandingSiteTypes);
            }
        }
        if(startDate!=null && endDate!=null){
            try {
                builder.append(" AND ");
           /* Date startDate1=new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
            Date endDate1=new SimpleDateFormat("yyyy-MM-dd").parse(endDate);*/
            builder.append(SclDateUtility.getDateRangeClauseQuery("brd.requestRaisedDate", startDate, endDate, params));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        builder.append(" order by {brd:requestRaisedDate} desc ");
        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(BrandingRequestDetailsModel.class));
        query.getQueryParameters().putAll(params);
        parameter.setFlexibleSearchQuery(query);
        return paginatedFlexibleSearchService.search(parameter);
    }

    @Override
    public BrandingTrackingStatusModel getTrackingStatusDetailsForRequest(String requisitionNumber, String requestStatus) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(BRAND_TRACKING_REQUEST_DETAILS_QUERY);
        params.put("requestNumber",requisitionNumber);
        if(requestStatus!=null){
            BrandingRequestStatus reqStatus=null;

            if(requestStatus.compareTo(BrandingRequestStatus.REQUEST_RAISED.getCode())==0)
                reqStatus= BrandingRequestStatus.REQUEST_RAISED;

            builder.append(" AND  {brd:requestStatus}=?requestSta");
            params.put("requestSta", reqStatus);
        }

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(BrandingTrackingStatusModel.class));
        final SearchResult<BrandingTrackingStatusModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public BrandingTrackingStatusModel getBrandingTrackingDetailsByReqNumber(String reqNumber) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(GET_BRANDING_TRACKING_DETAILS_BY_REQUEST_NUMBER);
        params.put("reqNumber", reqNumber);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(BrandingTrackingStatusModel.class));
        final SearchResult<BrandingTrackingStatusModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public BrandingRequestDetailsModel getBrandingRequestDetailsByReqNumber(String reqNumber) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(GET_BRANDING_REQUEST_DETAILS_UPLOAD_QUERY);
        params.put("reqNumber", reqNumber);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(BrandingRequestDetailsModel.class));
        final SearchResult<BrandingRequestDetailsModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public BrandingActivityVerficationModel getActivityDetailsForRequest(String requisitionNumber) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(ACTIVITY_VERIFICATION_DETAILS_BY_REQUEST_NUMBER);
        params.put("requisitionNumber", requisitionNumber);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(BrandingActivityVerficationModel.class));
        final SearchResult<BrandingActivityVerficationModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public TerritoryManagementService getTerritoryManagementService() {
        return territoryManagementService;
    }

    public void setTerritoryManagementService(TerritoryManagementService territoryManagementService) {
        this.territoryManagementService = territoryManagementService;
    }

    public PaginatedFlexibleSearchService getPaginatedFlexibleSearchService() {
        return paginatedFlexibleSearchService;
    }

    public void setPaginatedFlexibleSearchService(PaginatedFlexibleSearchService paginatedFlexibleSearchService) {
        this.paginatedFlexibleSearchService = paginatedFlexibleSearchService;
    }
}
