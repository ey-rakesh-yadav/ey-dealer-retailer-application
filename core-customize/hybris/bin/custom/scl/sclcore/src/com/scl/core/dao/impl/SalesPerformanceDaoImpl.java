package com.scl.core.dao.impl;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.DataConstraintDao;
import com.scl.core.dao.SalesPerformanceDao;
import com.scl.core.enums.*;
import com.scl.core.model.*;
import com.scl.core.region.dao.impl.DistrictMasterDaoImpl;
import com.scl.core.services.DJPVisitService;
import com.scl.core.services.SalesPerformanceService;
import com.scl.core.services.TerritoryManagementService;
import com.scl.core.services.TerritoryMasterService;
import com.scl.core.utility.SclDateUtility;
import com.scl.facades.data.FilterDistrictData;
import com.scl.facades.data.FilterTalukaData;
import com.scl.facades.data.RequestCustomerData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.c2l.BrandModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.jalo.type.SearchRestriction;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class SalesPerformanceDaoImpl implements SalesPerformanceDao {

    private static final Logger LOGGER = Logger.getLogger(SalesPerformanceDaoImpl.class);
    private static final Logger LOG = Logger.getLogger(SalesPerformanceDaoImpl.class);
    @Resource
    SearchRestrictionService searchRestrictionService;
    @Resource
    DistrictMasterDaoImpl districtMasterDao;
    @Resource
    TerritoryManagementDaoImpl territoryManagementDao;
    @Resource
    ProductService productService;
    @Resource
    CatalogVersionService catalogVersionService;
    @Resource
    BaseSiteService baseSiteService;
    @Resource
    FlexibleSearchService flexibleSearchService;
    @Resource
    UserService userService;
    @Resource
    TerritoryManagementService territoryManagementService;
    @Resource
    TerritoryMasterService territoryMasterService;
    @Autowired
    SalesPerformanceService salesPerformanceService;
    @Autowired
    DJPVisitService djpVisitService;
    @Autowired
    EnumerationService enumerationService;
    @Autowired
    DataConstraintDao dataConstraintDao;

    public DistrictMasterDaoImpl getDistrictMasterDao() {
        return districtMasterDao;
    }

    public void setDistrictMasterDao(DistrictMasterDaoImpl districtMasterDao) {
        this.districtMasterDao = districtMasterDao;
    }

    public TerritoryManagementDaoImpl getTerritoryManagementDao() {
        return territoryManagementDao;
    }

    public void setTerritoryManagementDao(TerritoryManagementDaoImpl territoryManagementDao) {
        this.territoryManagementDao = territoryManagementDao;
    }

    public SearchRestrictionService getSearchRestrictionService() {
        return searchRestrictionService;
    }

    public void setSearchRestrictionService(SearchRestrictionService searchRestrictionService) {
        this.searchRestrictionService = searchRestrictionService;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public ProductService getProductService() {
        return productService;
    }

    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    public CatalogVersionService getCatalogVersionService() {
        return catalogVersionService;
    }

    public void setCatalogVersionService(CatalogVersionService catalogVersionService) {
        this.catalogVersionService = catalogVersionService;
    }

    public BaseSiteService getBaseSiteService() {
        return baseSiteService;
    }

    public void setBaseSiteService(BaseSiteService baseSiteService) {
        this.baseSiteService = baseSiteService;
    }

    @Override
    public Double getActualTargetForSalesMTD(String subArea, SclUserModel sclUser, BaseSiteModel site) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}} WHERE"
                + "{o:placedBy} = ?sclUser and {o:versionID} IS NULL and {o:site} =?site and {o:subAreaMaster}=?subArea and {oe.cancelledDate} is null and  " + SclDateUtility.getMtdClauseQuery("oe:invoiceCreationDateAndTime", params));
        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("subArea", territoryManagementService.getTerritoryById(subArea));
        params.put("sclUser", sclUser);
        params.put("site", site);
        params.put("orderStatus", orderStatus);
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
    public Double getActualTargetForSalesYTD(String subArea, SclUserModel sclUser, BaseSiteModel site, Date startDate, Date endDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}}  WHERE "
                + "{o:placedBy} = ?sclUser and {o:versionID} IS NULL and {o:site} =?site and {o:subAreaMaster}=?subArea and {oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} <= ?endDate and {oe.cancelledDate} is null ");
        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("subArea", territoryManagementService.getTerritoryById(subArea));
        params.put("sclUser", sclUser);
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

    @Override
    public Double getMonthlySalesTarget(SclUserModel sclUser, BaseSiteModel site, String month, String year) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();

        List<SclCustomerModel> dealer = salesPerformanceService.getCustomersByLeadType("DEALER", null, null, null);
        if (CollectionUtils.isNotEmpty(dealer)) {
            List<String> dealercode = new ArrayList<>();
            for (SclCustomerModel sclCustomerModel : dealer) {
                if (sclCustomerModel.getUid() != null)
                    dealercode.add(sclCustomerModel.getUid());
            }
            LOGGER.info(String.format("customers size:%s", dealercode.size()));
            builder.append("select sum({m:revisedTarget}) from {DealerRevisedMonthlySales as m} where {m:monthName}=?month and {m:monthYear}=?year and {m:customerCode} in (?dealerCode) ");
            params.put("dealerCode", dealercode);
            params.put("month", month);
            params.put("year", year);

        }

           /* if(sclUser.getUserType().getCode()!=null) {
                if (sclUser.getUserType().getCode().equalsIgnoreCase("SO")) {
                    builder.append("select sum({m:dealerReviewedTotalRevisedTarget}) from {MonthlySales as m} where {m:so}=?sclUser and {m:monthName}=?month and {m:monthYear}=?year");
                    params.put("sclUser", sclUser);
                } else if (sclUser.getUserType().getCode().equals("RH") || sclUser.getUserType().getCode().equalsIgnoreCase("TSM")) {
                    builder.append("select sum({m:dealerReviewedTotalRevisedTarget}) from {MonthlySales as m} where {m:monthName}=?month and {m:monthYear}=?year");
                }
            }*/
        //params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        //  params.put("sclUser", sclUser);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(Double.class));
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    @Override
    public Double getMonthlySalesTarget(SclUserModel sclUser, BaseSiteModel currentBaseSite, String month, String year, List<String> territoryList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        List<SclCustomerModel> filterdDealerList = new ArrayList<>();
        List<SclCustomerModel> dealer = salesPerformanceService.getCustomersByLeadType("DEALER", null, null, null);
        List<TerritoryMasterModel> territoryMasterModelList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(territoryList)) {
            for (String s : territoryList) {
                TerritoryMasterModel territoryById = territoryMasterService.getTerritoryById(s);
                territoryMasterModelList.add(territoryById);
            }
            if (CollectionUtils.isNotEmpty(territoryMasterModelList) && CollectionUtils.isNotEmpty(dealer)) {
                for (SclCustomerModel sclCustomerModel : dealer) {
                    if (territoryMasterModelList.contains(sclCustomerModel.getTerritoryCode())) {
                        LOG.info(String.format("territoryModels dealer check inside target ::%s ", territoryMasterModelList.contains(sclCustomerModel.getTerritoryCode())));
                        filterdDealerList.add(sclCustomerModel);
                    }
                    LOG.info(String.format("Filter Dealer list inside target ::%s ", filterdDealerList.size()));
                }
                if (CollectionUtils.isNotEmpty(filterdDealerList)) {
                    List<String> dealercode = new ArrayList<>();
                    for (SclCustomerModel sclCustomerModel : filterdDealerList) {
                        if (sclCustomerModel.getUid() != null)
                            dealercode.add(sclCustomerModel.getUid());
                    }
                    LOGGER.info(String.format("customers size:%s", dealercode.size()));
                    builder.append("select sum({m:revisedTarget}) from {DealerRevisedMonthlySales as m} where {m:monthName}=?month and {m:monthYear}=?year and {m:customerCode} in (?dealerCode) and {m:territoryMaster} in (?territoryList) ");
                    params.put("dealerCode", dealercode);
                    params.put("territoryList", territoryMasterModelList);

                    // final StringBuilder builder = new StringBuilder("select {m:dealerReviewedTotalRevisedTarget} from {MonthlySales as m} where {m:so}=?sclUser and {m:monthName}=?month and {m:monthYear}=?year");

                    params.put("month", month);
                    params.put("year", year);
                    final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
                    query.addQueryParameters(params);
                    query.setResultClassList(Arrays.asList(Double.class));
                    LOGGER.info(String.format("Monthly Sales Target:%s", query));
                    final SearchResult<Double> searchResult = flexibleSearchService.search(query);
                    if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
                        return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
                    else
                        return 0.0;
                }
            }
        } else if (CollectionUtils.isNotEmpty(dealer)) {
            List<String> dealercode = new ArrayList<>();
            for (SclCustomerModel sclCustomerModel : dealer) {
                if (sclCustomerModel.getUid() != null)
                    dealercode.add(sclCustomerModel.getUid());
            }
            LOGGER.info(String.format("customers size:%s", dealercode.size()));
            builder.append("select sum({m:revisedTarget}) from {DealerRevisedMonthlySales as m} where {m:monthName}=?month and {m:monthYear}=?year and {m:customerCode} in (?dealerCode) ");
            params.put("dealerCode", dealercode);
            // final StringBuilder builder = new StringBuilder("select {m:dealerReviewedTotalRevisedTarget} from {MonthlySales as m} where {m:so}=?sclUser and {m:monthName}=?month and {m:monthYear}=?year");

            params.put("month", month);
            params.put("year", year);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class));
            LOGGER.info(String.format("Monthly Sales Target:%s", query));
            final SearchResult<Double> searchResult = flexibleSearchService.search(query);
            if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
                return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
            else
                return 0.0;
        } else {
            LOGGER.info(String.format("customers list is empty for monthly sales target"));
            return 0.0;
        }
        return 0.0;
    }

    @Override
    public List<DealerRevisedMonthlySalesModel> getMonthlySalesTargetForDealerWithBGP(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite, String month, String year, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        // userService.setCurrentUser(userService.getAdminUser());
        final StringBuilder builder = new StringBuilder("select {m:pk} from {DealerRevisedMonthlySales as m} where {m:customerCode}=?sclCustomer and {m:monthName}=?month and {m:monthYear}=?year");
        params.put("sclCustomer", sclCustomer.getUid());
        params.put("month", month);
        params.put("year", year);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(DealerRevisedMonthlySalesModel.class));

        final SearchResult<DealerRevisedMonthlySalesModel> searchResult = flexibleSearchService.search(query);
        /*searchRestrictionService.disableSearchRestrictions();
        final SearchResult<DealerRevisedMonthlySalesModel> searchResult = flexibleSearchService.search(query);
        searchRestrictionService.enableSearchRestrictions();*/
        List<DealerRevisedMonthlySalesModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public Double getMonthlySalesTargetForDealer(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite, String month, String year, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select sum({m:revisedTarget}) from {DealerRevisedMonthlySales as m} where {m:customerCode}=?sclCustomer and {m:monthName}=?month and {m:monthYear}=?year");
        params.put("sclCustomer", sclCustomer.getUid());
        params.put("month", month);
        params.put("year", year);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(Double.class));

        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    public List<List<Object>> getMonthlySalesTargetForDealerList(List<SclCustomerModel> sclCustomer,String month, String year) {
        try{
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {m:customerCode},sum({m:revisedTarget}) from {DealerRevisedMonthlySales as m} where {m:customerCode} in (?sclCustomer) and {m:monthName}=?month and {m:monthYear}=?year group by {m:customerCode} ");
        if(CollectionUtils.isNotEmpty(sclCustomer)) {
            List<String> customerNos=new ArrayList<>();
            for (SclCustomerModel sclCustomerModel : sclCustomer) {
                customerNos.add(sclCustomerModel.getUid());
            }
            params.put("sclCustomer",customerNos);
        }
        params.put("month", month);
        params.put("year", year);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        LOGGER.info("Query for Target:"+query);
        query.setResultClassList(Arrays.asList(String.class, Double.class));
        final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
        List<List<Object>> result = searchResult.getResult();
        return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
    } catch (IndexOutOfBoundsException e) {
        throw new IndexOutOfBoundsException(String.valueOf(e));
    }
    }

    @Override
    public Double getMonthlySalesTargetForDealerList(List<B2BCustomerModel> b2BCustomerModel, BaseSiteModel currentBaseSite, String month, String year, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select sum({m:revisedTarget}) from {DealerRevisedMonthlySales as m} where {m:customerCode} in (?sclCustomer) and {m:monthName}=?month and {m:monthYear}=?year");
        List<String> dealersUidList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(b2BCustomerModel)) {
            for (B2BCustomerModel customerModel : b2BCustomerModel) {
                if (customerModel.getUid() != null)
                    dealersUidList.add(customerModel.getUid());
            }
            params.put("sclCustomer", dealersUidList);
        }

        params.put("month", month);
        params.put("year", year);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(Double.class));

        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }


    @Override
    public Double getMonthlySalesTargetForSP(List<SclCustomerModel> sclCustomer, String month, String year) {
        final Map<String, Object> params = new HashMap<String, Object>();
        List<String> customerCodeList = new ArrayList<>();
        for (SclCustomerModel customerModel : sclCustomer) {
            customerCodeList.add(customerModel.getUid());
        }

        final StringBuilder builder = new StringBuilder("select sum({m:revisedTarget}) from {DealerRevisedMonthlySales as m} where {m:customerCode} in (?sclCustomer) and {m:monthName}=?month and {m:monthYear}=?year");
        params.put("sclCustomer", customerCodeList);
        params.put("month", month);
        params.put("year", year);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(Double.class));

        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }


    @Override
    public Double getMonthlySalesTargetForRetailer(String retailerCode, BaseSiteModel currentBaseSite, String monthYear, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select sum({m:monthTarget}) from {MonthWiseAnnualTarget as m} where {m:retailerCode}=?retailerCode and {m:monthYear}=?monthYear and {m:isAnnualSalesReviewedForRetailerDetails}=?isAnnualSalesReviewedForRetailerDetails and {m:productCode} is null and {m:selfCounterCustomerCode} is null");
        params.put("isAnnualSalesReviewedForRetailerDetails", true);
        params.put("retailerCode", retailerCode);
        params.put("monthYear", monthYear);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(Double.class));
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    @Override
    public Double getMonthlySalesTargetForRetailer(List<SclCustomerModel> retailerList, BaseSiteModel currentBaseSite, String monthYear, String bgpFilter) {
        if (CollectionUtils.isNotEmpty(retailerList)) {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({m:monthTarget}) from {MonthWiseAnnualTarget as m} where {m:retailerCode} in (?retailerCode) and {m:monthYear}=?monthYear and {m:isAnnualSalesReviewedForRetailerDetails}=?isAnnualSalesReviewedForRetailerDetails and {m:productCode} is null and {m:selfCounterCustomerCode} is null");
            params.put("isAnnualSalesReviewedForRetailerDetails", true);
            List<String> retailerCode = new ArrayList<>();
            for (SclCustomerModel sclCustomerModel : retailerList) {
                retailerCode.add(sclCustomerModel.getUid());
                params.put("retailerCode", retailerCode);
            }
            params.put("monthYear", monthYear);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class));
            final SearchResult<Double> searchResult = flexibleSearchService.search(query);
            if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
                return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
            else
                return 0.0;
        } else
            return 0.0;
    }

    @Override
    public List<List<Object>> getMonthlySalesTargetForRetailer(List<SclCustomerModel> retailerList, String monthYear) {
        if (CollectionUtils.isNotEmpty(retailerList)) {
            try {
                final Map<String, Object> params = new HashMap<String, Object>();
                final StringBuilder builder = new StringBuilder("select {m:retailerCode},sum({m:monthTarget}) from {MonthWiseAnnualTarget as m} where {m:retailerCode} in (?retailerCode) and {m:monthYear}=?monthYear and {m:isAnnualSalesReviewedForRetailerDetails}=?isAnnualSalesReviewedForRetailerDetails and {m:productCode} is null and {m:selfCounterCustomerCode} is null group by {m:retailerCode} ");
                params.put("isAnnualSalesReviewedForRetailerDetails", true);
                List<String> retailerCode = new ArrayList<>();
                for (SclCustomerModel sclCustomerModel : retailerList) {
                    retailerCode.add(sclCustomerModel.getUid());
                    params.put("retailerCode", retailerCode);
                }
                params.put("monthYear", monthYear);
                final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
                query.addQueryParameters(params);
                query.setResultClassList(Arrays.asList(String.class,Double.class));
                final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
                List<List<Object>> result = searchResult.getResult();
                return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
            } catch (IndexOutOfBoundsException e) {
                throw new IndexOutOfBoundsException(String.valueOf(e));
            }
        }
        else
            return null;
    }



    @Override
    public List<MonthWiseAnnualTargetModel> getMonthlySalesTargetForRetailerWithBGP(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite, String month, String year, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        // userService.setCurrentUser(userService.getAdminUser());
        //select {m:monthTarget} from {MonthWiseAnnualTarget as m on {m:retailerRevisedAnnualSales} where {m:retailerCode}='' and {m:isAnnualSalesReviewedForRetailer} = true and {m:monthName}='May-2023 and {m:monthYear}='2023'
        final StringBuilder builder = new StringBuilder("select {m:pk} from {MonthWiseAnnualTarget as m} where {m:retailerCode}=?sclCustomer and {m:monthName}=?month and {m:monthYear}=?year and {m:isAnnualSalesReviewedForRetailer}=?isAnnualSalesReviewedForRetailer");
        params.put("isAnnualSalesReviewedForRetailer", true);
        params.put("sclCustomer", sclCustomer.getUid());
        params.put("month", month);
        params.put("year", year);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(MonthWiseAnnualTargetModel.class));

        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
       /* searchRestrictionService.disableSearchRestrictions();
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        searchRestrictionService.enableSearchRestrictions();*/
        List<MonthWiseAnnualTargetModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override

    public double getActualSaleForDealerGraphYTD(SclCustomerModel sclCustomer, Date startDate, Date endDate, BaseSiteModel site, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();

        final StringBuilder builder = new StringBuilder();
        if (StringUtils.isBlank(bgpFilter)) {
            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}}  WHERE {o:user} = ?sclCustomer and {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and {oe:invoiceCreationDateAndTime} >= ?startDate and {oe:invoiceCreationDateAndTime} < ?endDate");
        } else if (bgpFilter.equalsIgnoreCase("ALL")) {
            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}}  WHERE {o:user} = ?sclCustomer and {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and {oe:invoiceCreationDateAndTime} >= ?startDate and {oe:invoiceCreationDateAndTime} < ?endDate");
        } else {
            CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
            ProductModel product = productService.getProductForCode(catalogVersion, bgpFilter);
            if (product != null) {
                builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk} JOIN Product as p on {oe:product}={p:pk}}  WHERE {o:user} = ?sclCustomer and {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and {oe:invoiceCreationDateAndTime} >= ?startDate and {oe:invoiceCreationDateAndTime} < ?endDate and {oe:product} =?product");
                params.put("product", product);
            }
        }
        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("sclCustomer", sclCustomer);
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

    @Override
    public double getActualSaleForGraphYTD(SclUserModel user, Date startDate, Date endDate, BaseSiteModel currentBaseSite, List<String> doList, List<String> subAreaList) {
        final Map<String, Object> params = new HashMap<String, Object>();

        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}}  WHERE {oe:status}=?orderStatus and {oe:deliveredDate} >= ?startDate and {oe:deliveredDate} < ?endDate");

        List<DistrictMasterModel> list = new ArrayList<>();
        List<SubAreaMasterModel> list1 = new ArrayList<>();

        if (doList != null && !doList.isEmpty()) {
            for (String code : doList) {
                list.add(districtMasterDao.findByCode(code));
            }
            params.put("doList", list);
            builder.append(" and {oe.districtMaster} in (?doList) ");
        }
        if (subAreaList != null && !subAreaList.isEmpty()) {
            for (String id : subAreaList) {
                list1.add(territoryManagementDao.getTerritoryById(id));
            }
            params.put("subAreaList", list1);
            builder.append(" and {oe.subAreaMaster} in (?subAreaList) ");
        }
        OrderStatus orderStatus = OrderStatus.DELIVERED;
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

    @Override
    public Double getLastMonthSalesTarget(SclUserModel sclUser, BaseSiteModel currentBaseSite, String month, String year) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        builder.append("select {m:totalTarget} from {MonthlySales as m} where {m:so}=?sclUser and {m:monthName}=?month and {m:monthYear}=?year");
        params.put("sclUser", sclUser);
        params.put("month", month);
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
    public Double getAnnualSalesTarget(SclUserModel sclUser, String financialYear, List<String> territoryList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();

        List<SclCustomerModel> dealer = salesPerformanceService.getCustomersByLeadType("DEALER", null, null, null);
        List<TerritoryMasterModel> territoryMasterModelList = new ArrayList<>();
        List<SclCustomerModel> filterdDealerList = new ArrayList<>();
        List<String> dealercode = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(territoryList)) {
            for (String s : territoryList) {
                TerritoryMasterModel territoryById = territoryMasterService.getTerritoryById(s);
                territoryMasterModelList.add(territoryById);
            }
            if (CollectionUtils.isNotEmpty(territoryMasterModelList) && CollectionUtils.isNotEmpty(dealer)) {
                for (SclCustomerModel sclCustomerModel : dealer) {
                    if (territoryMasterModelList.contains(sclCustomerModel.getTerritoryCode())) {
                        LOG.info(String.format("territoryModels dealer check inside target ::%s ", territoryMasterModelList.contains(sclCustomerModel.getTerritoryCode())));
                        filterdDealerList.add(sclCustomerModel);
                    }
                    LOG.info(String.format("Filter Dealer list inside target ::%s ", filterdDealerList.size()));
                }
                if (CollectionUtils.isNotEmpty(filterdDealerList)) {
                    for (SclCustomerModel sclCustomerModel : filterdDealerList) {
                        if (sclCustomerModel.getUid() != null)
                            dealercode.add(sclCustomerModel.getUid());
                    }
                    LOGGER.info(String.format("customers size:%s", dealercode.size()));
                    builder.append("select sum({ds:totalTarget}) from {DealerRevisedAnnualSales as ds} where {ds:customerCode} in (?dealercode) and {ds:isExistingDealerRevisedForReview}=?isExistingDealerRevisedForReview and {ds:financialYear}=?financialYear and {ds:territoryMaster} in (?territoryList) ");
                    params.put("territoryList", territoryMasterModelList);
                }
            }
        } else {
            if (CollectionUtils.isNotEmpty(dealer)) {
                for (SclCustomerModel sclCustomerModel : dealer) {
                    if (sclCustomerModel.getUid() != null)
                        dealercode.add(sclCustomerModel.getUid());
                }
                LOGGER.info(String.format("customers size:%s", dealercode.size()));
                builder.append("select sum({ds:totalTarget}) from {DealerRevisedAnnualSales as ds} where {ds:customerCode} in (?dealercode) and {ds:isExistingDealerRevisedForReview}=?isExistingDealerRevisedForReview and {ds:financialYear}=?financialYear ");
            }
        }
        params.put("financialYear", financialYear);
        params.put("dealercode", dealercode);
        params.put("isExistingDealerRevisedForReview", true);

       /* final StringBuilder builder = new StringBuilder("select sum({ann:totalReviewedTargetForAllDealers}) from {AnnualSales as ann} " +
                "where {ann:salesOfficer}=?sclUser and {ann:financialYear}=?financialYear and " +
                "{ann:isAnnualSalesReviewedForDealer}=?isAnnualSalesReviewedForDealer");
        params.put("sclUser", sclUser);
        params.put("isAnnualSalesReviewedForDealer", true);*/

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        LOGGER.info(String.format("annual sales target:%s", query));
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    @Override
    public Double getAnnualSalesTargetForDealer(SclCustomerModel sclCustomerModel, String financialYear, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        //userService.setCurrentUser(userService.getAdminUser());
        final StringBuilder builder = new StringBuilder("select sum({ds:totalTarget}) from {DealerRevisedAnnualSales as ds} where {ds:customerCode}=?sclCustomer and {ds:isExistingDealerRevisedForReview}=?isExistingDealerRevisedForReview and {ds:financialYear}=?financialYear ");
        // JOIN AnnualSales as ann on {ds:annualSales}={ann.pk}
        //and {ann:financialYear}=?financialYear and {ann:isAnnualSalesReviewedForDealer}=?isAnnualSalesReviewedForDealer
        params.put("sclCustomer", sclCustomerModel.getUid());
        params.put("financialYear", financialYear);
        params.put("isExistingDealerRevisedForReview", true);
        params.put("isAnnualSalesReviewedForDealer", true);
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
    public Double getAnnualSalesTargetForSP(List<SclCustomerModel> sclCustomerModel, String financialYear) {
        final Map<String, Object> params = new HashMap<String, Object>();
        List<String> customerCodeList = new ArrayList<>();
        for (SclCustomerModel customerModel : sclCustomerModel) {
            customerCodeList.add(customerModel.getUid());
        }

        final StringBuilder builder = new StringBuilder("select sum({ds:totalTarget}) from {DealerRevisedAnnualSales as ds JOIN AnnualSales as ann on {ds:annualSales}={ann.pk}} where {ds:customerCode} in (?sclCustomer) and {ann:financialYear}=?financialYear and {ann:isAnnualSalesReviewedForDealer}=?isAnnualSalesReviewedForDealer and {ds:isExistingDealerRevisedForReview}=?isExistingDealerRevisedForReview");
        params.put("sclCustomer", customerCodeList);
        params.put("financialYear", financialYear);
        params.put("isExistingDealerRevisedForReview", true);
        params.put("isAnnualSalesReviewedForDealer", true);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        ;
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }


    @Override
    public List<DealerRevisedAnnualSalesModel> getAnnualSalesTargetForDealerWithBGP(SclCustomerModel sclCustomerModel, String financialYear, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        builder.append("select {ds:pk} from {DealerRevisedAnnualSales as ds} where {ds:customerCode}=?sclCustomer and {ds:isExistingDealerRevisedForReview}=?isExistingDealerRevisedForReview and {ds:financialYear}=?financialYear ");
        // JOIN AnnualSales as ann on {ds:annualSales}={ann.pk}
        //{ann:financialYear}=?financialYear and {ann:isAnnualSalesReviewedForDealer}=?isAnnualSalesReviewedForDealer and
        params.put("sclCustomer", sclCustomerModel.getUid());
        params.put("financialYear", financialYear);
        params.put("isExistingDealerRevisedForReview", true);
        params.put("isAnnualSalesReviewedForDealer", true);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(DealerRevisedAnnualSalesModel.class));

        final SearchResult<DealerRevisedAnnualSalesModel> searchResult = flexibleSearchService.search(query);
        List<DealerRevisedAnnualSalesModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public Double getAnnualSalesTargetForRetailer(SclCustomerModel sclCustomerModel, String financialYear, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        /*final StringBuilder builder = new StringBuilder("select {totalTarget}  from {RetailerRevisedAnnualSalesDetails} where {customerCode}=?sclCustomer and {isAnnualSalesReviewedForRetailer}=?isAnnualSalesReviewedForRetailer and {financialYear}=?financialYear");*/
        //final StringBuilder builder = new StringBuilder("select {r:totalTarget}  from {AnnualSales as a JOIN RetailerRevisedAnnualSales as rs on {rs:annualSales}={a:pk} JOIN RetailerRevisedAnnualSalesDetails as r on {r:retailerRevisedAnnualSales}={rs:pk}} where {r:customerCode}=?sclCustomer and {r:isAnnualSalesReviewedForRetailer}=?isAnnualSalesReviewedForRetailer and {a:financialYear}=?financialYear");
        final StringBuilder builder = new StringBuilder("select {r:totalTarget}  from {RetailerRevisedAnnualSales as rs JOIN RetailerRevisedAnnualSalesDetails as r on {r:retailerRevisedAnnualSales}={rs:pk}} where {r:customerCode}=?sclCustomer and {r:isAnnualSalesReviewedForRetailer}=?isAnnualSalesReviewedForRetailer and {r:financialYear}=?financialYear");
        params.put("sclCustomer", sclCustomerModel.getUid());
        params.put("financialYear", financialYear);
        params.put("isAnnualSalesReviewedForRetailer", true);
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
    public Double getAnnualSalesTargetForRetailer(List<SclCustomerModel> sclCustomerModel, String financialYear, String bgpFilter) {
        if (CollectionUtils.isNotEmpty(sclCustomerModel)) {
            final Map<String, Object> params = new HashMap<String, Object>();
            /*final StringBuilder builder = new StringBuilder("select {totalTarget}  from {RetailerRevisedAnnualSalesDetails} where {customerCode}=?sclCustomer and {isAnnualSalesReviewedForRetailer}=?isAnnualSalesReviewedForRetailer and {financialYear}=?financialYear");*/
            //final StringBuilder builder = new StringBuilder("select {r:totalTarget}  from {AnnualSales as a JOIN RetailerRevisedAnnualSales as rs on {rs:annualSales}={a:pk} JOIN RetailerRevisedAnnualSalesDetails as r on {r:retailerRevisedAnnualSales}={rs:pk}} where {r:customerCode}=?sclCustomer and {r:isAnnualSalesReviewedForRetailer}=?isAnnualSalesReviewedForRetailer and {a:financialYear}=?financialYear");
            final StringBuilder builder = new StringBuilder("select {r:totalTarget}  from {RetailerRevisedAnnualSales as rs JOIN RetailerRevisedAnnualSalesDetails as r on {r:retailerRevisedAnnualSales}={rs:pk}} where {r:customerCode} in (?sclCustomer) and {r:isAnnualSalesReviewedForRetailer}=?isAnnualSalesReviewedForRetailer and {r:financialYear}=?financialYear");
            for (SclCustomerModel customerModel : sclCustomerModel) {
                params.put("sclCustomer", customerModel.getUid());
            }
            params.put("financialYear", financialYear);
            params.put("isAnnualSalesReviewedForRetailer", true);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.setResultClassList(Arrays.asList(Double.class));
            query.addQueryParameters(params);
            final SearchResult<Double> searchResult = flexibleSearchService.search(query);
            if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
                return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
            else
                return 0.0;
        } else {
            return 0.0;
        }
    }

    @Override
    public List<RetailerRevisedAnnualSalesModel> getAnnualSalesTargetForRetailerWithBGP(SclCustomerModel sclCustomerModel, String financialYear, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        //userService.setCurrentUser(userService.getAdminUser());
        final StringBuilder builder = new StringBuilder();
        builder.append("select {rs:pk} from {RetailerRevisedAnnualSales as rs JOIN AnnualSales as ann on {rs:annualSales}={ann.pk}} where {rs:customerCode}=?sclCustomer and {ann:financialYear}=?financialYear and {ann:isAnnualSalesReviewedForRetailer}=?isAnnualSalesReviewedForRetailer and {rs:isExistingRetailerRevisedForReview}=?isExistingRetailerRevisedForReview");
        params.put("sclCustomer", sclCustomerModel.getUid());
        params.put("financialYear", financialYear);
        params.put("isAnnualSalesReviewedForRetailer", true);
        params.put("isExistingRetailerRevisedForReview", true);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(RetailerRevisedAnnualSalesModel.class));

        final SearchResult<RetailerRevisedAnnualSalesModel> searchResult = flexibleSearchService.search(query);

        List<RetailerRevisedAnnualSalesModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    //MTD
    @Override
    public List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatio(SclUserModel sclUser, BaseSiteModel baseSite, SclCustomerModel customerModel, List<String> doList, List<String> subAreaList) {
        try {
            SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
            final Map<String, Object> params = new HashMap<String, Object>();
            List<DistrictMasterModel> list = new ArrayList<>();
            List<SubAreaMasterModel> list1 = new ArrayList<>();
            final StringBuilder builder = new StringBuilder();
          /*  builder.append("SELECT {p:code},{p:name},sum({oe:quantityInMT}) from " +
                    "{OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN " +
                    "Product as p on {oe:product}={p:pk} JOIN " +
                    "SclCustomer as sc on {sc:pk}={o:user} JOIN " +
                    "Product as p on {oe:product}={p:pk}}" +
                    "where {o:placedBy}= ?sclUser and " +
                    "{oe:status}=?orderStatus and " +
                    "{o:site} = ?baseSite and ");*/
            builder.append("SELECT {p:code},{p:name},sum({oe:quantityInMT}) from " +
                    "{OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN " +
                    "Product as p on {oe:product}={p:pk}} " +
                    "where {oe.cancelledDate} is null AND ");
            if (Objects.nonNull(customerModel)) {
                builder.append("{o:user}=?sclCustomer and ");
            }

            builder.append(SclDateUtility.getMtdClauseQuery("oe.invoiceCreationDateAndTime", params));
            if (doList != null && !doList.isEmpty()) {
                for (String code : doList) {
                    list.add(districtMasterDao.findByCode(code));
                }
                params.put("doList", list);
                builder.append(" and {o.districtMaster} in (?doList) ");
            }
            if (subAreaList != null && !subAreaList.isEmpty()) {
                for (String id : subAreaList) {
                    list1.add(territoryManagementDao.getTerritoryById(id));
                }
                params.put("subAreaList", list1);
                builder.append(" and {o.subAreaMaster} in (?subAreaList) ");
            }
            builder.append(" group by {p:code},{p:name}");
            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("sclUser", sclUser);
            params.put("baseSite", baseSite);
            params.put("orderStatus", orderStatus);
            if (Objects.nonNull(customerModel)) {
                params.put("sclCustomer", customerModel);
            }
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class, String.class, Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatio(SclUserModel sclUser, BaseSiteModel baseSite, List<String> doList, List<String> subAreaList) {
        try {
            SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
            final Map<String, Object> params = new HashMap<String, Object>();
            List<DistrictMasterModel> list = new ArrayList<>();
            List<SubAreaMasterModel> list1 = new ArrayList<>();
            final StringBuilder builder = new StringBuilder();
          /*  builder.append("SELECT {p:code},{p:name},sum({oe:quantityInMT}) from " +
                    "{OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN " +
                    "Product as p on {oe:product}={p:pk} JOIN " +
                    "SclCustomer as sc on {sc:pk}={o:user} JOIN " +
                    "Product as p on {oe:product}={p:pk}}" +
                    "where {o:placedBy}= ?sclUser and " +
                    "{oe:status}=?orderStatus and " +
                    "{o:site} = ?baseSite and ");*/
            builder.append("SELECT {p:code},{p:name},sum({oe:quantityInMT}) from " +
                    "{OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN " +
                    "Product as p on {oe:product}={p:pk}} " +
                    "where {oe.cancelledDate} is null and ").append(SclDateUtility.getMtdClauseQuery("oe:invoiceCreationDateAndTime", params));
            builder.append(" group by {p:code},{p:name}");
            if (doList != null && !doList.isEmpty()) {
                for (String code : doList) {
                    list.add(districtMasterDao.findByCode(code));
                }
                params.put("doList", list);
                builder.append(" and {o.districtMaster} in (?doList) ");
            }
            if (subAreaList != null && !subAreaList.isEmpty()) {
                for (String id : subAreaList) {
                    list1.add(territoryManagementDao.getTerritoryById(id));
                }
                params.put("subAreaList", list1);
                builder.append(" and {o.subAreaMaster} in (?subAreaList) ");
            }
            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("sclUser", sclUser);
            params.put("baseSite", baseSite);
            params.put("orderStatus", orderStatus);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class, String.class, Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public double getSalesQuantity(String customerNo, String startDate, String endDate, BaseSiteModel brand) {
        //  LOGGER.info(String.format("Getting Sale QTY for customerNo :: %s startDate :: %s endDate :: %s",customerNo,startDate,endDate));
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT sum({oe.quantityInMT}) from {OrderEntry AS oe " +
                "JOIN Order as o ON {oe:order}={o:pk} " +
                "JOIN SclCustomer as sc on {o:user}={sc:pk}} " +
                "WHERE " +
                "{o:date}>=?startDate AND {o:date} <=?endDate " +
                "AND {sc:customerNo}=?customerNo ");
        params.put("customerNo", customerNo);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if (CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        } else {
            return 0.0;
        }
    }

    public FlexibleSearchService getFlexibleSearchService() {
        return flexibleSearchService;
    }

    public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }

    @Override
    public List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForCustomer(SclCustomerModel customer,
                                                                                       BaseSiteModel baseSite, String customerType, List<String> doList, List<String> subAreaList) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            List<DistrictMasterModel> list = new ArrayList<>();
            List<SubAreaMasterModel> list1 = new ArrayList<>();
            final StringBuilder builder = new StringBuilder();

            if ((customer.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
              /*  builder.append("SELECT {p:code},{p:name},sum({oe:quantity}) from " +
                        "{OrderRequisition AS oe JOIN " +
                        "Product as p on {oe:product}={p:pk}} WHERE " +
                        "{oe:status}=?requisitionStatus and " +
                        "{oe.toCustomer}=?customer and " +
                        SclDateUtility.getMtdClauseQuery("oe:deliveredDate", params));
                if (doList != null && !doList.isEmpty()) {
                    for (String code : doList) {

                        list.add(districtMasterDao.findByCode(code));
                    }
                    params.put("doList", list);
                    builder.append(" and {oe.districtMaster} in (?doList) ");
                }
                if (subAreaList != null && !subAreaList.isEmpty()) {
                    for (String id : subAreaList) {
                        list1.add(territoryManagementDao.getTerritoryById(id));
                    }
                    params.put("subAreaList", list1);
                    builder.append(" and {oe.subAreaMaster} in (?subAreaList) ");
                }
                builder.append(" group by {p:code},{p:name}");
                RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
                params.put("customer", customer);
                params.put("requisitionStatus", requisitionStatus);*/

                final StringBuilder builder1 = new StringBuilder();
                final StringBuilder builder2= new StringBuilder();
                if(!(userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
                    builder1.append("SELECT {p:code},{or:aliasCode},sum({or:quantity}),{or.equivalenceProductCode} " +
                            " from {OrderRequisition AS or  JOIN " +
                            " Product as p on {or:product}={p:pk}} " +
                            " where {or:status}=?requisitionStatus and {or.toCustomer} = ?sclRetailer and {or:product} is not null " +
                            " and {or:requisitionType}=?requisitionType and " +
                            SclDateUtility.getMtdClauseQuery("or:liftingDate", params));
                    if ((userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
                        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
                        builder1.append(" and {or:fromCustomer}=?sclDealer ");
                        params.put("sclDealer", currentUser);
                    }
                    builder1.append(" group by {p:code},{or:aliasCode},{or.equivalenceProductCode}");

                    builder2.append("SELECT {p:code},{m:aliasCode},sum({m:quantityInMT}),{p.equivalenceProductCode} " +
                            " from {MasterStockAllocation AS m join Product as p on {m:product}={p:pk} } " +
                            " where {m.retailer} =?sclRetailer and {m:aliasCode} is not null and {m:isInvoiceCancelled}=?cancellationFlag  and " +
                            SclDateUtility.getMtdClauseQuery("m:invoicedDate", params));
                    if ((userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
                        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
                        builder2.append(" and {m:dealer}=?sclDealer ");
                        params.put("sclDealer", currentUser);
                    }
                    builder2.append(" group by  {p:code},{m:aliasCode},{p.equivalenceProductCode} ");
                }else{
                    builder1.append("SELECT {p:code},{or:aliasCode},sum({or:quantityInBags}),{or.equivalenceProductCode} " +
                            " from {OrderRequisition AS or  JOIN " +
                            " Product as p on {or:product}={p:pk}} " +
                            " where {or:status}=?requisitionStatus and {or.toCustomer} = ?sclRetailer and {or:product} is not null " +
                            " and {or:requisitionType}=?requisitionType and " +
                            SclDateUtility.getMtdClauseQuery("or:liftingDate", params));

                    builder1.append(" group by {p:code},{or:aliasCode},{or.equivalenceProductCode}");

                    builder2.append("SELECT {p:code},{m:aliasCode},sum({m:quantityInBags}),{p.equivalenceProductCode} " +
                            " from {MasterStockAllocation AS m join Product as p on {m:product}={p:pk} } " +
                            " where {m.retailer} =?sclRetailer and {m:aliasCode} is not null and {m:isInvoiceCancelled}=?cancellationFlag  and " +
                            SclDateUtility.getMtdClauseQuery("m:invoicedDate", params));


                    builder2.append(" group by {p:code},{m:aliasCode},{p.equivalenceProductCode}");
                }

                params.put("sclRetailer", customer);
                params.put("requisitionStatus", RequisitionStatus.SERVICED_BY_DEALER);
                params.put("requisitionType", RequisitionType.LIFTING);
                params.put("cancellationFlag", Boolean.FALSE);


                 FlexibleSearchQuery query1 = new FlexibleSearchQuery(builder1.toString());
                query1.addQueryParameters(params);
                query1.setResultClassList(Arrays.asList(String.class, String.class, Double.class,String.class));
                LOGGER.info(String.format("Query for Product Mix:%s",query1));
                final SearchResult<List<Object>> searchResult1 = flexibleSearchService.search(query1);
                List<List<Object>> result1 = searchResult1.getResult();
                LOGGER.info(String.format("resultsset1 for Product Mix:%s",result1));
               // return (result1 != null && !result1.isEmpty()) ? result1 : Collections.emptyList();

                FlexibleSearchQuery query2 = new FlexibleSearchQuery(builder2.toString());
                query2.addQueryParameters(params);
                query2.setResultClassList(Arrays.asList(String.class, String.class, Double.class,String.class));
                LOGGER.info(String.format("Query2 for Product Mix:%s",query2));
                final SearchResult<List<Object>> searchResult2 = flexibleSearchService.search(query2);
                List<List<Object>> result2 = searchResult2.getResult();
                LOGGER.info(String.format("resultsset2 for Product Mix:%s",result2));
                //result2 != null && !result2.isEmpty() ? result2 : Collections.emptyList();

                if(CollectionUtils.isNotEmpty(result1) && CollectionUtils.isNotEmpty(result2)) {
                    Map<String, List<Object>> resultMap1 = mapByCode(result1);
                    Map<String, List<Object>> resultMap2 = mapByCode(result2);

                    for (String code : resultMap1.keySet()) {
                        if (resultMap2.containsKey(code)) {
                            List<Object> lista = resultMap1.get(code);
                            List<Object> listb = resultMap2.get(code);

                            double qty1 = (double) lista.get(2);
                            double qty2 = (double) listb.get(2);
                            double totalQty = qty1 + qty2;

                            lista.set(2, totalQty);
                            listb.set(2, totalQty);

                            LOG.info(String.format("Updated Qty for code {%s} in result1: {%s} after : {%s} ", code, lista.get(2),totalQty));
                            LOG.info(String.format("Updated Qty for code {%s} in result2: {%s} after : {%s} ", code, listb.get(2),totalQty));
                        }else{
                            LOG.info(String.format("not Updated Qty for code {%s} in result1: {%s}  ", code,(resultMap1.get(code)!=null && resultMap1.get(code).get(2)!=null) ?resultMap1.get(code).get(2):null));
                          //  LOG.info(String.format("not Updated Qty for code {%s} in result2: {%s}  ", code, resultMap2);
                        }
                    }
                }

                List<List<Object>> result3 =new ArrayList<>();
                result3.addAll(result1);
                result3.addAll(result2);
                List<List<Object>> lists = removeDuplicates(result3);
                return (lists != null && !lists.isEmpty()) ? lists.stream().distinct().collect(Collectors.toList()) : Collections.emptyList();
            } else {
                builder.append("SELECT {p:code},{p:name},sum({oe:quantityInMT}) from " +
                        "{OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN " +
                        "Product as p on {oe:product}={p:pk}} Where " +
                        "{oe.cancelledDate} is null AND ").append(SclDateUtility.getMtdClauseQuery("oe:invoiceCreationDateAndTime", params));
                if (doList != null && !doList.isEmpty()) {
                    for (String code : doList) {

                        list.add(districtMasterDao.findByCode(code));
                    }
                    params.put("doList", list);
                    builder.append(" and {o.districtMaster} in (?doList) ");
                }
                if (subAreaList != null && !subAreaList.isEmpty()) {
                    for (String id : subAreaList) {
                        list1.add(territoryManagementDao.getTerritoryById(id));
                    }
                    params.put("subAreaList", list1);
                    builder.append(" and {o.subAreaMaster} in (?subAreaList) ");
                }

                if (customer.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                    if (Objects.nonNull(customer)) {
                        builder.append(" and {o:user}=?customer ");
                        params.put("customer", customer);
                    }
                }
                builder.append(" group by {p:code},{p:name}");
                OrderStatus orderStatus = OrderStatus.DELIVERED;
                params.put("baseSite", baseSite);
                params.put("orderStatus", orderStatus);
            }
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class, String.class, Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();

        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }
    public static List<List<Object>> removeDuplicates(List<List<Object>> listOfLists) {
        // Using a Set to filter out duplicates
        Set<List<Object>> set = new LinkedHashSet<>(listOfLists);
        // Convert the set back to a list
        return new ArrayList<>(set);
    }
    @Override
    public List<List<Object>> getBrandwiseSalesPercentRatioAndVolumeRatioForCustomer(SclCustomerModel customer) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder1 = new StringBuilder();
            final StringBuilder builder2 = new StringBuilder();
            if ((customer.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
               /* builder.append("SELECT {p:code},{p:name},sum({oe:quantity}) from " +
                        "{OrderRequisition AS oe JOIN " +
                        "Product as p on {oe:product}={p:pk}} WHERE " +
                        "{oe:status}=?requisitionStatus and " +
                        "{oe.toCustomer}=?customer and " +
                        SclDateUtility.getMtdClauseQuery("oe:deliveredDate", params));
                builder.append(" group by {p:code},{p:name}");
                RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
                params.put("customer", customer);
                params.put("requisitionStatus", requisitionStatus);*/
                if(!(userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {

                        builder1.append("SELECT {p:code},{p:name},sum({or:quantity}),{or.equivalenceProductCode} " +
                                " from {OrderRequisition AS or  JOIN " +
                                " Product as p on {or:product}={p:pk}} " +
                                " where {or:status}=?requisitionStatus and {or.toCustomer} = ?sclRetailer and {or:product} is not null " +
                                " and {or:requisitionType}=?requisitionType and " +
                                SclDateUtility.getMtdClauseQuery("or:liftingDate", params));

                        builder1.append(" group by {p:code},{p:name},{or.equivalenceProductCode}");

                        builder2.append("SELECT {p:code},{m:aliasCode},sum({m:quantityInMT}),{p.equivalenceProductCode} " +
                                " from {MasterStockAllocation AS m join Product as p on {m.product}={p.pk}} " +
                                " where {m.retailer} =?sclRetailer and {m:aliasCode} is not null and {m:isInvoiceCancelled}=?cancellationFlag  and " +
                                SclDateUtility.getMtdClauseQuery("m:invoicedDate", params));

                        builder2.append(" group by {m.productCode},{m:aliasCode},{p.equivalenceProductCode} ");
                    }
                else{

                builder1.append("SELECT {p:code},{p:name},sum({or:quantity}),{or.equivalenceProductCode} " +
                        " from {OrderRequisition AS or  JOIN " +
                        " Product as p on {or:product}={p:pk}} " +
                        " where {or:status}=?requisitionStatus and {or.toCustomer} = ?sclRetailer and {or:product} is not null " +
                        " and {or:requisitionType}=?requisitionType and " +
                        SclDateUtility.getMtdClauseQuery("or:liftingDate", params));

                builder1.append(" group by {p:code},{p:name},{or.equivalenceProductCode}");

                    builder2.append("SELECT {p:code},{m:aliasCode},sum({m:quantityInBags}),{p.equivalenceProductCode} " +
                            " from {MasterStockAllocation AS m join Product as p on {m.product}={p.pk}} " +
                        " where {m.retailer} =?sclRetailer and {m:aliasCode} is not null and {m:isInvoiceCancelled}=?cancellationFlag  and " +
                        SclDateUtility.getMtdClauseQuery("m:invoicedDate", params));

                builder2.append(" group by {m.productCode},{m:aliasCode},{p.equivalenceProductCode} ");
            }
                params.put("sclRetailer", customer);
                params.put("requisitionStatus", RequisitionStatus.SERVICED_BY_DEALER);
                params.put("requisitionType", RequisitionType.LIFTING);
                params.put("cancellationFlag", Boolean.FALSE);


                FlexibleSearchQuery query1 = new FlexibleSearchQuery(builder1.toString());
                query1.addQueryParameters(params);
                query1.setResultClassList(Arrays.asList(String.class, String.class, Double.class,String.class));
                LOGGER.info(String.format("Query for Product Mix:%s",query1));
                final SearchResult<List<Object>> searchResult1 = flexibleSearchService.search(query1);
                List<List<Object>> result1 = searchResult1.getResult();
                // return (result1 != null && !result1.isEmpty()) ? result1 : Collections.emptyList();

                FlexibleSearchQuery query2 = new FlexibleSearchQuery(builder2.toString());
                query2.addQueryParameters(params);
                query2.setResultClassList(Arrays.asList(String.class, String.class, Double.class,String.class));
                LOGGER.info(String.format("Query2 for Product Mix:%s",query2));
                final SearchResult<List<Object>> searchResult2 = flexibleSearchService.search(query2);
                List<List<Object>> result2 = searchResult2.getResult();
                //result2 != null && !result2.isEmpty() ? result2 : Collections.emptyList();
                if(CollectionUtils.isNotEmpty(result1) && CollectionUtils.isNotEmpty(result2)) {
                    Map<String, List<Object>> resultMap1 = mapByCode(result1);
                    Map<String, List<Object>> resultMap2 = mapByCode(result2);

                    for (String code : resultMap1.keySet()) {
                        if (resultMap2.containsKey(code)) {
                            List<Object> lista = resultMap1.get(code);
                            List<Object> listb = resultMap2.get(code);

                            double qty1 = (double) lista.get(2);
                            double qty2 = (double) listb.get(2);
                            double totalQty = qty1 + qty2;

                            lista.set(2, totalQty);
                            listb.set(2, totalQty);

                            LOG.info(String.format("Updated Qty for code {%s} in result1: {%s}", code, totalQty));
                            LOG.info(String.format("Updated Qty for code {%s} in result2: {%s}", code, totalQty));
                        }else{
                            LOG.info(String.format("not Updated Qty for code {%s} in result1: {%s}  ", code,(resultMap1.get(code)!=null && resultMap1.get(code).get(2)!=null) ?resultMap1.get(code).get(2):null));
                           // LOG.info(String.format("not Updated Qty for code {%s} in result2: {%s}  ", code, resultMap2.get(code).get(2)));
                        }
                    }
                }

                List<List<Object>> result3 =new ArrayList<>();
                result3.addAll(result1);
                result3.addAll(result2);
                List<List<Object>> lists = removeDuplicates(result3);
                return (lists != null && !lists.isEmpty()) ? lists.stream().distinct().collect(Collectors.toList()) : Collections.emptyList();
               // return (result3 != null && !result3.isEmpty()) ? result3.stream().distinct().collect(Collectors.toList()) : Collections.emptyList();

            }
            else if ((customer.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID)))) {
                StringBuilder builder=new StringBuilder();
                builder.append("SELECT {p:code},{p:name},sum({oe:quantity}) from " +
                        "{PointRequisition AS oe JOIN " +
                        "Product as p on {oe:product}={p:pk}} WHERE " +
                        "{oe:status}=?pointRequisitionStatus and " +
                        "{oe.requestRaisedFor}=?customer and " +
                        SclDateUtility.getMtdClauseQuery("oe:deliveryDate", params));
                builder.append(" group by {p:code},{p:name}");
                PointRequisitionStatus pointRequisitionStatus = PointRequisitionStatus.APPROVED;
                params.put("customer", customer);
                params.put("pointRequisitionStatus", pointRequisitionStatus);
                final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
                query.addQueryParameters(params);
                query.setResultClassList(Arrays.asList(String.class, String.class, Double.class));
                final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
                List<List<Object>> result = searchResult.getResult();
                return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
            }

        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
        return  null;
    }

    @Override
    public List<List<Object>> getProductMixPercentRatioAndVolumeRatioWithPoints(SclCustomerModel customer, String filter, Date startDate, Date endDate) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder();
            if ((customer.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID)))) {
                if (StringUtils.isBlank(filter)) {
                    builder.append("SELECT {p:code},{p:name},sum({oe:quantity}),sum({oe:points}) from " +
                            "{PointRequisition AS oe JOIN " +
                            "Product as p on {oe:product}={p:pk}} WHERE " +
                            "{oe:status}=?pointRequisitionStatus and " +
                            "{oe.requestRaisedFor}=?customer and " +
                            SclDateUtility.getMtdClauseQuery("oe:deliveryDate", params));
                    builder.append(" group by {p:code},{p:name}");
                } else if (filter.equalsIgnoreCase("MTD")) {
                    builder.append("SELECT {p:code},{p:name},sum({oe:quantity}),sum({oe:points}) from " +
                            "{PointRequisition AS oe JOIN " +
                            "Product as p on {oe:product}={p:pk}} WHERE " +
                            "{oe:status}=?pointRequisitionStatus and " +
                            "{oe.requestRaisedFor}=?customer and " +
                            SclDateUtility.getMtdClauseQuery("oe:deliveryDate", params));
                    builder.append(" group by {p:code},{p:name}");
                } else if (filter.equalsIgnoreCase("YTD")) {
                    builder.append("SELECT {p:code},{p:name},sum({oe:quantity}),sum({oe:points}) from " +
                            "{PointRequisition AS oe JOIN " +
                            "Product as p on {oe:product}={p:pk}} WHERE " +
                            "{oe:status}=?pointRequisitionStatus and " +
                            "{oe.requestRaisedFor}=?customer and " +
                            "{oe:deliveryDate}>=?startDate and {oe:deliveryDate}<=?endDate ");
                    builder.append(" group by {p:code},{p:name}");
                } else {
                    builder.append("SELECT {p:code},{p:name},sum({oe:quantity}),sum({oe:points}) from " +
                            "{PointRequisition AS oe JOIN " +
                            "Product as p on {oe:product}={p:pk}} WHERE " +
                            "{oe:status}=?pointRequisitionStatus and " +
                            "{oe.requestRaisedFor}=?customer and " +
                            SclDateUtility.getMtdClauseQuery("oe:deliveryDate", params));
                    builder.append(" group by {p:code},{p:name}");
                }
                PointRequisitionStatus pointRequisitionStatus = PointRequisitionStatus.APPROVED;
                params.put("customer", customer);
                params.put("startDate", startDate);
                params.put("endDate", endDate);
                params.put("pointRequisitionStatus", pointRequisitionStatus);
            }

            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class, String.class, Double.class, Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<String> getStateWiseProductForSummaryPage(String subArea, String catalogId, String version, String prodStatus) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select {p:name} from {product as p join Catalog as c on {c.pk}={p.catalog} join CatalogVersion as cv on {cv.pk}={p.catalogversion} join ArticleApprovalStatus as aas on {p.approvalstatus}={aas.pk} JOIN UserSubAreaMapping as u on {p:state}={u:state} JOIN SubAreaMaster as sa {u:subArea}={sa:taluka}} where {cv.version}=?version and {aas.codelowercase}=?prodStatus and {c.id}=?catalogId and {sa:pk}=?subArea");
            params.put("subArea", territoryManagementService.getTerritoryById(subArea));
            params.put("catalogId", catalogId);
            params.put("version", version);
            params.put("prodStatus", prodStatus);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class));
            final SearchResult<String> searchResult = flexibleSearchService.search(query);
            List<String> result = searchResult.getResult();
            return result != null && !result.isEmpty() ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    public TerritoryManagementService getTerritoryManagementService() {
        return territoryManagementService;
    }

    public void setTerritoryManagementService(TerritoryManagementService territoryManagementService) {
        this.territoryManagementService = territoryManagementService;
    }

    @Override
    public Double getSalesHistoryDataForDealer(int month, int year, CustomerCategory category, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT SUM({s:quantity}) FROM {SalesHistory as s JOIN SclCustomer as sc on {s:customerNo}={sc:customerNo} JOIN PrincipalGroupRelation as p on {p:source}={sc:pk} JOIN UserGroup as u on {u:pk}={p:target}} WHERE {customerCategory}=?category and {u:uid}='SclDealerGroup' AND {brand}=?brand AND").append(SclDateUtility.getDateClauseQueryByMonthYear("invoiceDate", month, year, params));
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
    public Double getSalesHistoryDataForDealerWithSubArea(String subArea, int month, int year, CustomerCategory category, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT SUM({s:quantity}) FROM {SalesHistory as s JOIN SclCustomer as sc on {s:customerNo}={sc:customerNo} JOIN CustomerSubAreaMapping as cs on {sc:pk}={cs:sclCustomer}} where {cs:subArea}=?subArea JOIN PrincipalGroupRelation as p on {p:source}={sc:pk} JOIN UserGroup as u on {u:pk}={p:target}} WHERE {customerCategory}=?category and {u:uid}='SclDealerGroup' AND {brand}=?brand AND").append(SclDateUtility.getDateClauseQueryByMonthYear("invoiceDate", month, year, params));
        params.put("subArea", subArea);
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
    public List<List<Object>> getSalesHistoryModelList(String taluka, Date startDate, Date endDate, BaseSiteModel site) {
        try {
            LOGGER.info(String.format("Getting Taluka :: %s ", taluka));
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select {s:ncr},{s:quantity} " +
                    "from {SalesHistory as s} where {s:taluka}=?taluka AND {s:brand}=?site AND {s:invoiceDate}>=?startDate AND {s:invoiceDate}<=?endDate");
            //.append(SclDateUtility.getDateClauseQueryByMonthYear("invoiceDate", month, year, params)).append("group by {s:taluka}");
            params.put("taluka", taluka);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            params.put("site", site);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class, Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }

    }

    @Override
    public List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForYTD(SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate, SclCustomerModel customer, List<String> doList, List<String> subAreaList) {
        try {
            B2BCustomerModel user = (B2BCustomerModel) getUserService().getCurrentUser();
            final Map<String, Object> params = new HashMap<String, Object>();
            List<DistrictMasterModel> list = new ArrayList<>();
            List<SubAreaMasterModel> list1 = new ArrayList<>();
            final StringBuilder builder = new StringBuilder();
            final StringBuilder query = new StringBuilder();


            if (Objects.nonNull(customer)) {
                if ((customer.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
                    builder.append("SELECT {p:code},{p:name},sum({oe:quantityInMT}) from " +
                            "{OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN " +
                            "Product as p on {oe:product}={p:pk}} " +
                            "Where {oe.cancelledDate} is null AND  " +
                            "{oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} <= ?endDate ");
                    if (Objects.nonNull(customer)) {
                        builder.append(" and {o:user}=?customer ");
                        params.put("customer", customer);
                    }
                    builder.append(" group by {p:code},{p:name}");

                    OrderStatus orderStatus = OrderStatus.DELIVERED;
                    //params.put("sclUser", sclUser);

                    params.put("baseSite", baseSite);
                    params.put("orderStatus", orderStatus);
                    params.put("startDate", startDate);
                    params.put("endDate", endDate);
                } else if (user.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SP_GROUP_ID))) {
                    builder.append("SELECT {p:code},{p:name},sum({oe:quantityInMT}) from " +
                            "{OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN " +
                            "Product as p on {oe:product}={p:pk}} " +
                            "Where {oe.cancelledDate} is null AND  " +
                            "{oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} <= ?endDate ");
                    if (Objects.nonNull(user.getState())) {
                        String state = user.getState();
                        builder.append(" and {p:state}=?state ");
                        params.put("state", state);
                    }
                    builder.append(" group by {p:code},{p:name}");

                    OrderStatus orderStatus = OrderStatus.DELIVERED;
                    //params.put("sclUser", sclUser);

                    params.put("baseSite", baseSite);
                    params.put("orderStatus", orderStatus);
                    params.put("startDate", startDate);
                    params.put("endDate", endDate);
                } else if ((customer.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
                 /*   query.append("SELECT {p:code},{p:name},sum({oe:quantity}) from " +
                            "{OrderRequisition AS oe JOIN " +
                            "Product as p on {oe:product}={p:pk}} WHERE " +
                            "{oe:status}=?requisitionStatus and " +
                            "{oe.toCustomer}=?customer and " +
                            "{oe.deliveredDate} >= ?startDate and {oe.deliveredDate} <= ?endDate ");

                    query.append(" group by {p:code},{p:name}");
                    RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
                    params.put("customer", customer);
                    params.put("requisitionStatus", requisitionStatus);*/
                    final StringBuilder builder1 = new StringBuilder();
                    final StringBuilder builder2 = new StringBuilder();
                    if(!(userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
                    builder1.append("SELECT {p:code},{or:aliasCode},sum({or:quantity}),{or.equivalenceProductCode} from {OrderRequisition AS or JOIN Product as p on {or:product}={p:pk}} " +
                            " where {or:status}=?requisitionStatus and {or.toCustomer} = ?sclRetailer and {or:product} is not null and {or:requisitionType}=?requisitionType " +
                            " and {or:liftingDate} >=?startDate and {or:liftingDate} <?endDate ");
                        if ((userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
                            builder1.append(" and {or:fromCustomer}=?sclDealer ");
                            B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
                            params.put("sclDealer", currentUser);
                        }
                    builder1.append(" group by {p:code},{or:aliasCode},{or.equivalenceProductCode}");

                    builder2.append("SELECT {p:code},{m:aliasCode},sum({m:quantityInMT}),{p.equivalenceProductCode} from {MasterStockAllocation AS m join Product as p on {p:pk}={m.product} } " +
                            " where {m.retailer} =?sclRetailer and {m:aliasCode} is not null and {m:isInvoiceCancelled}=?cancellationFlag  " +
                            " and {m:invoicedDate}>=?startDate and {m:invoicedDate}<?endDate ");
                        if ((userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
                            builder2.append(" and {m:dealer}=?sclDealer ");
                            B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
                            params.put("sclDealer", currentUser);
                        }

                    builder2.append(" group by {p:code},{m:aliasCode},{p.equivalenceProductCode} ");
                }
                 else{
                    builder1.append("SELECT {p:code},{or:aliasCode},sum({or:quantity}),{or.equivalenceProductCode} from {OrderRequisition AS or JOIN Product as p on {or:product}={p:pk}} " +
                            " where {or:status}=?requisitionStatus and {or.toCustomer} = ?sclRetailer and {or:product} is not null and {or:requisitionType}=?requisitionType " +
                            " and {or:liftingDate} >=?startDate and {or:liftingDate} <?endDate ");

                    builder1.append(" group by {p:code},{or:aliasCode},{or.equivalenceProductCode}");

                        builder2.append("SELECT {p:code},{m:aliasCode},sum({m:quantityInBags}),{p.equivalenceProductCode} from {MasterStockAllocation AS m join Product as p on {p:pk}={m.product} } " +
                            " where {m.retailer} =?sclRetailer and {m:aliasCode} is not null and {m:isInvoiceCancelled}=?cancellationFlag  " +
                            " and {m:invoicedDate}>=?startDate and {m:invoicedDate}<?endDate ");

                    builder2.append(" group by {p:code},{m:aliasCode},{p.equivalenceProductCode} ");
                }
                /*RequisitionStatus requisitionStatus = RequisitionStatus.SERVICED_BY_DEALER;
                params.put("customer", sclUser);
                params.put("requisitionStatus", requisitionStatus);*/
                    params.put("sclRetailer", customer);
                    // params.put("product", product);
                    params.put("requisitionStatus", RequisitionStatus.SERVICED_BY_DEALER);
                    params.put("requisitionType", RequisitionType.LIFTING);
                    params.put("cancellationFlag", Boolean.FALSE);

                    params.put("startDate", startDate);
                    params.put("endDate", endDate);
                    final FlexibleSearchQuery qry1 = new FlexibleSearchQuery(builder1.toString());
                    qry1.addQueryParameters(params);
                    qry1.setResultClassList(Arrays.asList(String.class, String.class, Double.class,String.class));
                    LOGGER.info(String.format("Query for Product Mix OR:%s", qry1));
                    final SearchResult<List<Object>> searchResult1 = flexibleSearchService.search(qry1);
                    List<List<Object>> result1 = searchResult1.getResult();

                    final FlexibleSearchQuery qry2 = new FlexibleSearchQuery(builder2.toString());
                    qry2.addQueryParameters(params);
                    qry2.setResultClassList(Arrays.asList(String.class, String.class, Double.class,String.class));
                    LOGGER.info(String.format("Query for Product Mix Master:%s", qry2));
                    final SearchResult<List<Object>> searchResult2 = flexibleSearchService.search(qry2);
                    List<List<Object>> result2 = searchResult2.getResult();

                    if (CollectionUtils.isNotEmpty(result1) && CollectionUtils.isNotEmpty(result2)) {
                        Map<String, List<Object>> resultMap1 = mapByCode(result1);
                        Map<String, List<Object>> resultMap2 = mapByCode(result2);

                        for (String code : resultMap1.keySet()) {
                            if (resultMap2.containsKey(code)) {
                                List<Object> lista = resultMap1.get(code);
                                List<Object> listb = resultMap2.get(code);

                                double qty1 = (double) lista.get(2);
                                double qty2 = (double) listb.get(2);
                                double totalQty = qty1 + qty2;

                                lista.set(2, totalQty);
                                listb.set(2, totalQty);

                                LOG.info(String.format("Updated Qty for code {%s} in result1: {%s}", code, totalQty));
                                LOG.info(String.format("Updated Qty for code {%s} in result3: {%s}", code, totalQty));
                            }else{
                                LOG.info(String.format("not Updated Qty for code {%s} in result1: {%s}  ", code,(resultMap1.get(code)!=null && resultMap1.get(code).get(2)!=null) ?resultMap1.get(code).get(2):null));
                                //LOG.info(String.format("not Updated Qty for code {%s} in result2: {%s}  ", code, resultMap2.get(code).get(2)));
                            }
                        }
                    }

                    List<List<Object>> result3 =new ArrayList<>();
                    result3.addAll(result1);
                    result3.addAll(result2);
                    List<List<Object>> lists = removeDuplicates(result3);
                    return (lists != null && !lists.isEmpty()) ? lists.stream().distinct().collect(Collectors.toList()) : Collections.emptyList();
                    //return (result3 != null && !result3.isEmpty()) ? result3.stream().distinct().collect(Collectors.toList()) : Collections.emptyList();
                }
            }
        }
            catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
        return null;
    }

    private static Map<String, List<Object>> mapByCode(List<List<Object>> resultList) {
        Map<String, List<Object>> resultMap = new HashMap<>();
        for (List<Object> list : resultList) {
            String code = (String) list.get(0);
            resultMap.put(code, list);
        }
        return resultMap;
    }

    @Override
    public List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForMonthPicker(SclUserModel sclUser, BaseSiteModel baseSite, int month, int year, List<String> doList, List<String> subAreaList) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
            List<DistrictMasterModel> list = new ArrayList<>();
            List<SubAreaMasterModel> list1 = new ArrayList<>();
            // final StringBuilder builder = new StringBuilder("SELECT {p:code},{p:name},sum({oe:quantityInMT}) from {OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN Product as p on {oe:product}={p:pk} JOIN SclCustomer as sc on {sc:pk}={o:user}} where {o:placedBy}= ?sclUser and {oe:status}=?orderStatus and {o:site} = ?baseSite and").append(SclDateUtility.getDateClauseQueryByMonthYear("oe:deliveredDate",month,year,params)).append("group by {p:code},{p:name}");

            final StringBuilder builder = new StringBuilder("SELECT {p:code},{p:name},sum({oe:quantityInMT}) from {OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN Product as p on {oe:product}={p:pk}} where {oe.cancelledDate} is null AND  ").append(SclDateUtility.getDateClauseQueryByMonthYear("oe:invoiceCreationDateAndTime", month, year, params));

           /* builder.append("SELECT {p:code},{p:name},sum({oe:quantityInMT}) from " +
                    "{OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN " +
                    "Product as p on {oe:product}={p:pk} JOIN " +
                    "SclCustomer as sc on {sc:pk}={o:user} JOIN " +
                    "Product as p on {oe:product}={p:pk}}" +
                    "where {o:placedBy}= ?sclUser and " +
                    "{oe:status}=?orderStatus and " +
                    "{o:site} = ?baseSite and " +
                    SclDateUtility.getDateClauseQueryByMonthYear("oe:deliveredDate", month, year, params) +
                    "group by {p:code},{p:name}");*/
            if (doList != null && !doList.isEmpty()) {
                for (String code : doList) {
                    list.add(districtMasterDao.findByCode(code));
                }
                params.put("doList", list);
                builder.append(" and {o.districtMaster} in (?doList) ");
            }
            if (subAreaList != null && !subAreaList.isEmpty()) {
                for (String id : subAreaList) {
                    list1.add(territoryManagementDao.getTerritoryById(id));
                }
                params.put("subAreaList", list1);
                builder.append(" and {o.subAreaMaster} in (?subAreaList) ");
            }
            builder.append(" group by {p:code},{p:name}");
            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("sclUser", sclUser);
            params.put("baseSite", baseSite);
            params.put("orderStatus", orderStatus);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class, String.class, Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public Double getActualTargetSalesForSelectedMonthAndYear(String subArea, SclUserModel sclUser, BaseSiteModel site, int month, int year) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}}  WHERE "

                + "{o:placedBy} = ?sclUser and {o:versionID} IS NULL and {o:site} =?site and {o:subAreaMaster}=?subArea and {oe.cancelledDate} is null AND ").append(SclDateUtility.getDateClauseQueryByMonthYear("oe:invoiceCreationDateAndTime", month, year, params));
        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("subArea", territoryManagementService.getTerritoryById(subArea));
        params.put("sclUser", sclUser);
        params.put("site", site);
        params.put("orderStatus", orderStatus);


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
    public Double getActualTargetSalesForSelectedMonthAndYearForDealer(SclCustomerModel sclCustomer, BaseSiteModel baseSite, int month, int year, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        if (StringUtils.isBlank(bgpFilter)) {
            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}}  WHERE "
                    + "{o:user} = ?sclCustomer and {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and ").append(SclDateUtility.getDateClauseQueryByMonthYear("oe:invoiceCreationDateAndTime", month, year, params));
        } else if (bgpFilter.equalsIgnoreCase("ALL")) {
            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}}  WHERE "
                    + "{o:user} = ?sclCustomer and {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and ").append(SclDateUtility.getDateClauseQueryByMonthYear("oe:invoiceCreationDateAndTime", month, year, params));

        } else {
            CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
            ProductModel product = productService.getProductForCode(catalogVersion, bgpFilter);
            if (product != null) {
                builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk} JOIN Product as p on {oe:product}={p:pk}}  WHERE "
                        + "{o:user} = ?sclCustomer and {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and {oe:product} =?product and").append(SclDateUtility.getDateClauseQueryByMonthYear("oe:invoiceCreationDateAndTime", month, year, params));
                params.put("product", product);
            }
        }

        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("orderStatus", orderStatus);
        params.put("sclCustomer", sclCustomer);
        params.put("site", baseSite);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);

        // final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        //searchRestrictionService.disableSearchRestrictions();
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        //searchRestrictionService.enableSearchRestrictions();
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    @Override

    public Double getActualTargetSalesForSelectedMonthAndYearForSP(List<SclCustomerModel> sclCustomer, int month, int year) {
        final Map<String, Object> params = new HashMap<String, Object>();
        //userService.setCurrentUser(userService.getAdminUser());
        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}}  WHERE {o:user} in (?sclCustomer) and "
                + " {o:versionID} IS NULL and {oe.cancelledDate} is null and").append(SclDateUtility.getDateClauseQueryByMonthYear("oe:invoiceCreationDateAndTime", month, year, params));
        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("orderStatus", orderStatus);
        params.put("sclCustomer", sclCustomer);

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
    public Double getActualTargetSalesForSelectedMonthAndYearForRetailer(SclCustomerModel sclCustomer, BaseSiteModel baseSite, int month, int year, String bgpFilter) {

        final Map<String, Object> params = new HashMap<String, Object>();
        //userService.setCurrentUser(userService.getAdminUser());
        LOGGER.info("YearFilter and MonthFilter and  SclCustomer PK:" + year + " and " + month + " and " + sclCustomer);
        final StringBuilder builder = new StringBuilder();
        if (StringUtils.isBlank(bgpFilter)) {
            builder.append("SELECT SUM({oe:quantity}) FROM {OrderRequisition AS oe}  WHERE "
                    + "{oe:status}=?requisitionStatus and {oe.toCustomer}=?sclCustomer and {oe:cancelledDate} is null and {oe:product} is not null and").append(SclDateUtility.getDateClauseQueryByMonthYear("oe:deliveredDate", month, year, params));
        } else if (bgpFilter.equalsIgnoreCase("ALL")) {
            builder.append("SELECT SUM({oe:quantity}) FROM {OrderRequisition AS oe}  WHERE "
                    + "{oe:status}=?requisitionStatus and {oe.toCustomer}=?sclCustomer and {oe:cancelledDate} is null and {oe:product} is not null and").append(SclDateUtility.getDateClauseQueryByMonthYear("oe:deliveredDate", month, year, params));
        } else {
            CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
            ProductModel product = productService.getProductForCode(catalogVersion, bgpFilter);
            if (product != null) {
                //builder.append("SELECT SUM({oe:quantity}) FROM {OrderRequisition AS oe}  WHERE "
                //         + "{oe:status}=?requisitionStatus and {oe.toCustomer}=?sclCustomer and {oe:product} like ?bgpFilter and").append(SclDateUtility.getDateClauseQueryByMonthYear("oe:deliveredDate",month,year,params));
                builder.append("SELECT SUM({oe:quantity}) from {OrderRequisition AS oe JOIN Product as p on {oe:product}={p:pk}} WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?sclCustomer and {oe:product} =?product and {oe:cancelledDate} is null and {oe:product} is not null and").append(SclDateUtility.getDateClauseQueryByMonthYear("oe:deliveredDate", month, year, params));
                params.put("product", product);

            }
        }
        RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
        params.put("requisitionStatus", requisitionStatus);
        params.put("sclCustomer", sclCustomer);
        params.put("site", baseSite);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);

        //final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        //searchRestrictionService.disableSearchRestrictions();
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        //searchRestrictionService.enableSearchRestrictions();
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    @Override
    public Double getNCRThreshold(String state, BaseSiteModel site, String yearMonth) {
        LOGGER.info(String.format("Getting State :: %s , Base Site :: %s , yearMonth :: %s ", state, site, yearMonth));
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select SUM({n:ncr}) from {SclNCRThreshold as n} where {n:state}=?state AND {n:brand}=?site AND {n:yearMonth}=?yearMonth");
        params.put("state", state);
        params.put("site", site);
        params.put("yearMonth", yearMonth);
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
    public Double getActualTargetForSalesMTD(SclUserModel sclUser, BaseSiteModel site, List<String> doList, List<String> subAreaList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
        List<DistrictMasterModel> list = new ArrayList<>();
        List<SubAreaMasterModel> list1 = new ArrayList<>();
        final StringBuilder builder = new StringBuilder();
        //builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}} WHERE"
        //            + "{o:placedBy} = ?sclUser and {o:versionID} IS NULL and {o:site} =?site and {oe:status}=?orderStatus and" + SclDateUtility.getMtdClauseQuery("oe:deliveredDate", params));

        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk}} WHERE "

                + "{oe.cancelledDate} is null and " + SclDateUtility.getMtdClauseQuery("oe:invoiceCreationDateAndTime", params));
        if (doList != null && !doList.isEmpty()) {
            for (String code : doList) {

                list.add(districtMasterDao.findByCode(code));
            }
            params.put("doList", list);
            builder.append(" and {o.districtMaster} in (?doList) ");
        }
        if (subAreaList != null && !subAreaList.isEmpty()) {
            for (String id : subAreaList) {
                list1.add(territoryManagementDao.getTerritoryById(id));
            }
            params.put("subAreaList", list1);
            builder.append(" and {o.subAreaMaster} in (?subAreaList) ");
        }
        params.put("sclUser", sclUser);
        params.put("site", site);
        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("orderStatus", orderStatus);
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
    public Double getActualTargetForSalesDealerMTD(SclCustomerModel sclCustomer, BaseSiteModel site, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        //userService.setCurrentUser(userService.getAdminUser());
        final StringBuilder builder = new StringBuilder();
        if (StringUtils.isBlank(bgpFilter)) {
            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}} WHERE {o:user} = ?sclCustomer and {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and").append(SclDateUtility.getMtdClauseQuery("oe:invoiceCreationDateAndTime", params));
        } else if (bgpFilter.equalsIgnoreCase("ALL")) {
            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}} WHERE {o:user} = ?sclCustomer and {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and").append(SclDateUtility.getMtdClauseQuery("oe:invoiceCreationDateAndTime", params));
        } else {
            CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
            ProductModel product = productService.getProductForCode(catalogVersion, bgpFilter);
            if (product != null) {
                builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk} JOIN Product as p on {oe:product}={p:pk}} WHERE {o:user} = ?sclCustomer and {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and {oe:product} =?product and").append(SclDateUtility.getMtdClauseQuery("oe:invoiceCreationDateAndTime", params));
                // String filterKey= "%".concat(bgpFilter.toUpperCase()).concat("%");
                //String filterKey= "%".concat(String.valueOf(product)).concat("%");
                params.put("product", product);
            }
        }
        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("sclCustomer", sclCustomer);
        params.put("site", site);
        params.put("orderStatus", orderStatus);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
       /* searchRestrictionService.disableSearchRestrictions();
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        searchRestrictionService.enableSearchRestrictions();*/
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty())) {
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        } else {
            return 0.0;
        }
    }


    @Override
    public Double getActualTargetForSalesSPMTD(List<SclCustomerModel> sclCustomer) {
        {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder();

            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}}  WHERE {o:user} in (?sclCustomer)  and   "
                    + " {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and ").append(SclDateUtility.getMtdClauseQuery("oe:invoiceCreationDateAndTime", params));

            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("sclCustomer", sclCustomer);
            params.put("orderStatus", orderStatus);
            params.put("site", baseSiteService.getCurrentBaseSite());
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.setResultClassList(Arrays.asList(Double.class));
            query.addQueryParameters(params);
            final SearchResult<Double> searchResult = flexibleSearchService.search(query);
            if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty())) {
                return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
            } else {
                return 0.0;
            }
        }
    }

    @Override
    public Double getActualTargetForSalesRetailerMTD(SclCustomerModel customer, BaseSiteModel site, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        //userService.setCurrentUser(userService.getAdminUser());
        final StringBuilder builder = new StringBuilder();
        LOG.info("SclCustomer and MTD date:" + customer + " " + SclDateUtility.getMtdClauseQuery("", params));
        if (StringUtils.isBlank(bgpFilter)) {
            builder.append("select sum({p.quantity}) from {OrderRequisition as p} ")
                    .append(" where {p.toCustomer}=?customer and {p:status}=?requisitionStatus and ").append(SclDateUtility.getMtdClauseQueryRetailer("p:deliveredDate", params));

        } else if (bgpFilter.equalsIgnoreCase("ALL")) {
            builder.append("SELECT sum({oe:quantity}) from {OrderRequisition AS oe} WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?customer and {oe:product} is not null and").append(SclDateUtility.getMtdClauseQueryRetailer("oe:deliveredDate", params));
        } else {
            CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
            ProductModel product = productService.getProductForCode(catalogVersion, bgpFilter);
            if (product != null) {
                builder.append("SELECT sum({oe:quantity}) from {OrderRequisition AS oe JOIN Product as p on {oe:product}={p:pk}} WHERE {oe:status}=?requisitionStatus and {oe:toCustomer}=?customer and {oe:product} =?product and {oe:product} is not null and").append(SclDateUtility.getMtdClauseQueryRetailer("oe:deliveredDate", params));
                params.put("product", product);
            }
        }
        RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
        params.put("requisitionStatus", requisitionStatus);
        params.put("customer", customer);
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
    public Double getActualTargetForSalesLastMonth(SclUserModel sclUser, BaseSiteModel currentBaseSite, int year, int month, List<String> doList, List<String> subAreaList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
        List<DistrictMasterModel> list = new ArrayList<>();
        List<SubAreaMasterModel> list1 = new ArrayList<>();
        final StringBuilder builder = new StringBuilder();
        /*builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}} WHERE"

                + "{o:placedBy} = ?sclUser and {o:versionID} IS NULL and {o:site} =?site and {oe:status}=?orderStatus and {oe.invoiceCreationDateAndTime}>=?startDate and {oe.invoiceCreationDateAndTime} <=?endDate");*/
        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}} WHERE {oe.cancelledDate} is null and").append(SclDateUtility.getDateClauseQueryByMonthYear("oe:invoiceCreationDateAndTime", month, year, params));
        if (doList != null && !doList.isEmpty()) {
            for (String code : doList) {

                list.add(districtMasterDao.findByCode(code));
            }
            params.put("doList", list);
            builder.append(" and {o.districtMaster} in (?doList) ");
        }
        if (subAreaList != null && !subAreaList.isEmpty()) {
            for (String id : subAreaList) {
                list1.add(territoryManagementDao.getTerritoryById(id));
            }
            params.put("subAreaList", list1);
            builder.append(" and {o.subAreaMaster} in (?subAreaList) ");
        }
        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("sclUser", sclUser);
        params.put("site", currentBaseSite);
        params.put("orderStatus", orderStatus);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    public Integer getSourceAndDestination() {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT COUNT({oe.entryNumber}) FROM { OrderEntry AS oe JOIN Order AS o ON {o.pk} = {oe.order} JOIN Warehouse AS w ON {o.warehouse}={w.pk}} WHERE {o.placedBy}=?currentUser AND {w.type}=?type  AND {o:versionID} IS NULL AND {o:site} = ?site AND ");

        OrderStatus orderStatus = OrderStatus.DELIVERED;
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Integer.class));
        query.addQueryParameters(params);
        final SearchResult<Integer> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0;
        else
            return 0;
    }

/*    public Integer getSecondaryLeadDistance() {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT {distance} FROM {DestinationSourceMaster} AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}} WHERE"
                + "{o:placedBy} = ?sclUser and {o:versionID} IS NULL and {o:site} =?site and {oe:status}=?orderStatus and {oe.invoiceCreationDateAndTime}>=?startDate and {oe.invoiceCreationDateAndTime} <=?endDate");
        OrderStatus orderStatus=OrderStatus.DELIVERED;
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Integer.class));
        query.addQueryParameters(params);
        final SearchResult<Integer> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0;
        else
            return 0;
    }*/


    @Override
    public Double getActualTargetForSalesYTD(SclUserModel sclUser, BaseSiteModel site, Date startDate, Date endDate, List<String> doList, List<String> subAreaList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
        List<DistrictMasterModel> list = new ArrayList<>();
        List<SubAreaMasterModel> list1 = new ArrayList<>();
        final StringBuilder builder = new StringBuilder();
        //builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}}  WHERE "
        //        + "{o:placedBy} = ?sclUser and {o:versionID} IS NULL and {o:site} =?site and {oe:status}=?orderStatus and {oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} <= ?endDate");
        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}}  WHERE "

                + "{oe.cancelledDate} is null and {oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} <= ?endDate");
        if (doList != null && !doList.isEmpty()) {
            for (String code : doList) {

                list.add(districtMasterDao.findByCode(code));
            }
            params.put("doList", list);
            builder.append(" and {o.districtMaster} in (?doList) ");
        }
        if (subAreaList != null && !subAreaList.isEmpty()) {
            for (String id : subAreaList) {
                list1.add(territoryManagementDao.getTerritoryById(id));
            }
            params.put("subAreaList", list1);
            builder.append(" and {o.subAreaMaster} in (?subAreaList) ");
        }
        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("sclUser", sclUser);
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

    @Override
    public Double getActualTargetForPartnerSalesYTD(String code, SclUserModel sclUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate, List<String> doList, List<String> subAreaList) {
        {
            final Map<String, Object> params = new HashMap<String, Object>();
            SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
            List<DistrictMasterModel> list = new ArrayList<>();
            List<SubAreaMasterModel> list1 = new ArrayList<>();
            final StringBuilder builder = new StringBuilder();
            //builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}}  WHERE "
            //        + "{o:placedBy} = ?sclUser and {o:versionID} IS NULL and {o:site} =?site and {oe:status}=?orderStatus and {oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} <= ?endDate");
            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}}  WHERE "

                    + "{oe.cancelledDate} is null and {sc:uid}=?code and {oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} <= ?endDate");
            if (doList != null && !doList.isEmpty()) {
                for (String codes : doList) {

                    list.add(districtMasterDao.findByCode(codes));
                }
                params.put("doList", list);
                builder.append(" and {o.districtMaster} in (?doList) ");
            }
            if (subAreaList != null && !subAreaList.isEmpty()) {
                for (String id : subAreaList) {
                    list1.add(territoryManagementDao.getTerritoryById(id));
                }
                params.put("subAreaList", list1);
                builder.append(" and {o.subAreaMaster} in (?subAreaList) ");
            }
            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("sclUser", sclUser);
            params.put("code", code);
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
    }

    @Override
    public Double getActualTargetForSalesDealerYTD(SclCustomerModel sclCustomer, BaseSiteModel site, Date startDate, Date endDate, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        // userService.setCurrentUser(userService.getAdminUser());
        final StringBuilder builder = new StringBuilder();

        if (StringUtils.isBlank(bgpFilter)) {
            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}}  WHERE {o:user} = ?sclCustomer and {oe.cancelledDate} is null and {oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} < ?endDate");
        } else if (bgpFilter.equalsIgnoreCase("ALL")) {
            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}}  WHERE {o:user} = ?sclCustomer and {oe.cancelledDate} is null and {oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} < ?endDate");
        } else {
            CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
            ProductModel product = productService.getProductForCode(catalogVersion, bgpFilter);
            if (product != null) {
                builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk} JOIN Product as p on {oe:product}={p:pk}}  WHERE {o:user} = ?sclCustomer and {oe.cancelledDate} is null and {oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} < ?endDate and {oe:product} =?product");
                // String filterKey = "%".concat(bgpFilter.toUpperCase()).concat("%");

                //String filterKey= "%".concat(String.valueOf(product)).concat("%");
                params.put("product", product);
            }
        }
        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("sclCustomer", sclCustomer);
        params.put("site", site);
        params.put("orderStatus", orderStatus);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);

        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        /*searchRestrictionService.disableSearchRestrictions();
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        searchRestrictionService.enableSearchRestrictions();*/
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty())) {
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        } else {
            return 0.0;
        }
    }

    @Override
    public Double getActualTargetForSalesRetailerYTD(SclCustomerModel customer, BaseSiteModel site, Date startDate, Date endDate, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        if (StringUtils.isBlank(bgpFilter)) {
            builder.append("SELECT sum({oe:quantity}) from {OrderRequisition AS oe} WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?customer and {oe:deliveredDate}>=?startDate and {oe:deliveredDate}< ?endDate");
        } else if (bgpFilter.equalsIgnoreCase("ALL")) {
            builder.append("SELECT sum({oe:quantity}) from {OrderRequisition AS oe} WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?customer and {oe:deliveredDate}>=?startDate and {oe:deliveredDate}< ?endDate");
        } else {
            CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
            ProductModel product = productService.getProductForCode(catalogVersion, bgpFilter);
            if (product != null) {
                builder.append("SELECT sum({oe:quantity}) from {OrderRequisition AS oe JOIN Product as p on {oe:product}={p:pk}} WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?customer and {oe:deliveredDate}>=?startDate and {oe:deliveredDate}< ?endDate and {oe:product} =?product");
                params.put("product", product);
            }
        }
        RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
        params.put("requisitionStatus", requisitionStatus);
        params.put("customer", customer);
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

    @Override
    public Double getActualTargetForSalesLeaderYTD(SclCustomerModel sclUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();

        LOGGER.info(String.format("SclCustomer Model PK:%s", sclUser));

        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk}}  WHERE {o:user} = ?sclCustomer and {o:versionID} IS NULL and {o:site} =?site and {oe:status}=?orderStatus and {oe.deliveredDate} >= ?startDate and {oe.deliveredDate} <= ?endDate");

        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("orderStatus", orderStatus);
        params.put("sclCustomer", sclUser);
        params.put("site", currentBaseSite);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("premium", "N");
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
    public Double getActualTargetForSalesLastYear(SclUserModel sclUser, BaseSiteModel currentBaseSite, String startDate, String endDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}}  WHERE "

                + "{o:placedBy} = ?sclUser and {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and {oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} <= ?endDate");
        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("sclUser", sclUser);
        params.put("site", currentBaseSite);
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

    @Override
    public Double getActualTargetSalesForSelectedMonthAndYear(SclUserModel sclUser, BaseSiteModel site, int month, int year, List<String> doList, List<String> subAreaList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
        List<DistrictMasterModel> list = new ArrayList<>();
        List<SubAreaMasterModel> list1 = new ArrayList<>();
        final StringBuilder builder = new StringBuilder();

        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}}  WHERE "

                + " {oe.cancelledDate} is null and").append(SclDateUtility.getDateClauseQueryByMonthYear("oe:invoiceCreationDateAndTime", month, year, params));


        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("sclUser", sclUser);
        params.put("site", site);
        params.put("orderStatus", orderStatus);
        if (doList != null && !doList.isEmpty()) {
            for (String code : doList) {
                list.add(districtMasterDao.findByCode(code));
            }
            params.put("doList", list);
            builder.append(" and {o.districtMaster} in (?doList) ");
        }
        if (subAreaList != null && !subAreaList.isEmpty()) {
            for (String id : subAreaList) {
                list1.add(territoryManagementDao.getTerritoryById(id));
            }
            params.put("subAreaList", list1);
            builder.append(" and {o.subAreaMaster} in (?subAreaList) ");
        }
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
    public Double getActualTargetPartnerSalesForSelectedMonthAndYear(String code, SclUserModel sclUser, BaseSiteModel baseSite, int month, int year, List<String> doList, List<String> subAreaList) {
        {
            final Map<String, Object> params = new HashMap<String, Object>();
            SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
            List<DistrictMasterModel> list = new ArrayList<>();
            List<SubAreaMasterModel> list1 = new ArrayList<>();
            final StringBuilder builder = new StringBuilder();
     /*   builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}}  WHERE "
                + "{o:placedBy} = ?sclUser and {o:versionID} IS NULL and {o:site} =?site and {oe:status}=?orderStatus and").append(SclDateUtility.getDateClauseQueryByMonthYear("oe:deliveredDate",month,year,params));*/
            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}}  WHERE "

                    + " {oe.cancelledDate} is null and {sc:uid}=?code and ").append(SclDateUtility.getDateClauseQueryByMonthYear("oe:invoiceCreationDateAndTime", month, year, params));

            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("sclUser", sclUser);
            params.put("code", code);
            params.put("site", baseSite);
            params.put("orderStatus", orderStatus);
            if (doList != null && !doList.isEmpty()) {
                for (String codes : doList) {

                    list.add(districtMasterDao.findByCode(codes));
                }
                params.put("doList", list);
                builder.append(" and {o.districtMaster} in (?doList) ");
            }
            if (subAreaList != null && !subAreaList.isEmpty()) {
                for (String id : subAreaList) {
                    list1.add(territoryManagementDao.getTerritoryById(id));
                }
                params.put("subAreaList", list1);
                builder.append(" and {o.subAreaMaster} in (?subAreaList) ");
            }
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.setResultClassList(Arrays.asList(Double.class));
            query.addQueryParameters(params);
            final SearchResult<Double> searchResult = flexibleSearchService.search(query);

            if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
                return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
            else
                return 0.0;
        }
    }

    @Override
    public Double getActualTargetForPartnerSalesMTD(String code, SclUserModel sclUser, BaseSiteModel currentBaseSite, List<String> doList, List<String> subAreaList) {
        {
            final Map<String, Object> params = new HashMap<String, Object>();
            SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
            List<DistrictMasterModel> list = new ArrayList<>();
            List<SubAreaMasterModel> list1 = new ArrayList<>();
            final StringBuilder builder = new StringBuilder();
            //builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}} WHERE"
            //            + "{o:placedBy} = ?sclUser and {o:versionID} IS NULL and {o:site} =?site and {oe:status}=?orderStatus and" + SclDateUtility.getMtdClauseQuery("oe:deliveredDate", params));

            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {sc:pk}={o:user}} WHERE "

                    + "{oe.cancelledDate} is null and {sc:uid}=?code and " + SclDateUtility.getMtdClauseQuery("oe:invoiceCreationDateAndTime", params));
            if (doList != null && !doList.isEmpty()) {
                for (String codes : doList) {

                    list.add(districtMasterDao.findByCode(codes));
                }
                params.put("doList", list);
                builder.append(" and {o.districtMaster} in (?doList) ");
            }
            if (subAreaList != null && !subAreaList.isEmpty()) {
                for (String id : subAreaList) {
                    list1.add(territoryManagementDao.getTerritoryById(id));
                }
                params.put("subAreaList", list1);
                builder.append(" and {o.subAreaMaster} in (?subAreaList) ");
            }
            params.put("sclUser", sclUser);
            params.put("code", code);
            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("orderStatus", orderStatus);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.setResultClassList(Arrays.asList(Double.class));
            query.addQueryParameters(params);
            final SearchResult<Double> searchResult = flexibleSearchService.search(query);
            if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
                return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
            else
                return 0.0;
        }
    }

    @Override
    public List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForYTD(SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate, List<String> doList, List<String> subAreaList) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
            List<DistrictMasterModel> list = new ArrayList<>();
            List<SubAreaMasterModel> list1 = new ArrayList<>();
            final StringBuilder builder = new StringBuilder();
            /*builder.append("SELECT {p:code},{p:name},sum({oe:quantityInMT}) from {OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN Product as p on {oe:product}={p:pk} JOIN SclCustomer as sc on {sc:pk}={o:user} JOIN Product as p on {oe:product}={p:pk}}where {o:placedBy}= ?sclUser and {oe:status}=?orderStatus and " +

                    "{o:site} = ?baseSite and {oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} <= ?endDate group by {p:code},{p:name}");*/
            builder.append("SELECT {p:code},{p:name},sum({oe:quantityInMT}) from {OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN Product as p on {oe:product}={p:pk}} where {oe.cancelledDate} is null and " +
                    "{oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} <= ?endDate group by {p:code},{p:name}");
            if (doList != null && !doList.isEmpty()) {
                for (String code : doList) {

                    list.add(districtMasterDao.findByCode(code));
                }
                params.put("doList", list);
                builder.append(" and {o.districtMaster} in (?doList) ");
            }
            if (subAreaList != null && !subAreaList.isEmpty()) {
                for (String id : subAreaList) {
                    list1.add(territoryManagementDao.getTerritoryById(id));
                }
                params.put("subAreaList", list1);
                builder.append(" and {o.subAreaMaster} in (?subAreaList) ");
            }
            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("sclUser", sclUser);
            params.put("baseSite", baseSite);
            params.put("orderStatus", orderStatus);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class, String.class, Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public Double getDealerOutstandingAmount(String customerNo) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {netOutstanding} FROM {CreditAndOutstanding} WHERE {customerNumber}=?customerNo");
        params.put("customerNo", customerNo);
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
    public List<List<Object>> getPartnerDetailsForSales(String searchKeyWord) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("SELECT {sc:uid},{sc:name},{sc:mobileNumber},{sc:customerNo} from {SCLCustomer as sc} " +
                    "where {sc:uid}=?searchKeyword or {sc:name}=?searchKeyword or {sc:mobileNumber}=?searchKeyword or {sc:customerNo}=?searchKeyword");
            params.put("searchKeyword", searchKeyWord);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class, String.class, String.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }


    @Override
    public List<List<Object>> getSecondaryLeadDistanceForMonth(SclUserModel sclUser, WarehouseType warehouseType, BaseSiteModel baseSite, Integer year1, Integer month1, List<String> territoryList) {
        final Map<String, Object> attr = new HashMap<String, Object>();
        SclUserModel user = (SclUserModel) userService.getCurrentUser();
        attr.put(WarehouseModel.TYPE, warehouseType);
        attr.put(OrderModel.SITE, baseSiteService.getCurrentBaseSite());
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT({oe:pk}),sum({distance}) ");
        sql.append("FROM {").append(OrderEntryModel._TYPECODE).append(" AS oe ");
        sql.append("JOIN ").append(OrderModel._TYPECODE).append(" AS o ");
        sql.append("ON {o:").append(OrderModel.PK).append("}={oe:").append(OrderEntryModel.ORDER).append("} ");
        sql.append("JOIN ").append(WarehouseModel._TYPECODE).append(" AS w ");
        sql.append("ON {o:").append(OrderModel.WAREHOUSE).append("}={w:").append(WarehouseModel.PK).append("}} ");
        sql.append("WHERE ").append("{w:").append(WarehouseModel.TYPE).append("} = ?type and ");
        sql.append(SclDateUtility.getDateClauseQueryByMonthYear("oe.truckDispatcheddate", month1, year1, attr));

       /* List<DistrictMasterModel> list = new ArrayList<>();
        List<SubAreaMasterModel> list1 = new ArrayList<>();

        if (doList != null && !doList.isEmpty()) {
            for (String code : doList) {
                list.add(districtMasterDao.findByCode(code));
            }
            attr.put("doList", list);
            sql.append(" and {o.districtMaster} in (?doList) ");
        }
        if (subAreaList != null && !subAreaList.isEmpty()) {
            for (String id : subAreaList) {
                list1.add(territoryManagementDao.getTerritoryById(id));
            }
            attr.put("subAreaList", list1);
            sql.append(" and {o.subAreaMaster} in (?subAreaList) ");
        }*/

        List<TerritoryMasterModel> territoriesList = new ArrayList<>();
        if (territoryList != null && !territoryList.isEmpty()) {
            for (String id : territoryList) {
                territoriesList.add(territoryMasterService.getTerritoryById(id));
            }
            attr.put("territoriesList", territoriesList);
            sql.append(" and {o.territoryMaster} in (?territoriesList) ");
        }
        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Integer.class, Double.class));
        query.addQueryParameters(attr);
        LOGGER.info(String.format("secondary lead dis month:%s", query));
        final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult() != null && !searchResult.getResult().isEmpty() ? searchResult.getResult() : Collections.emptyList();

    }

    @Override
    public DestinationSourceMasterModel getDestinationSourceBySource(OrderType orderType, CustomerCategory customerCategory, WarehouseModel source, DeliveryModeModel deliveryMode, String city, String district, String state, BaseSiteModel brand, String grade, String packaging) {

        if (city != null && district != null && state != null) {
            Map<String, Object> map = new HashMap<>();
            map.put(DestinationSourceMasterModel.DELIVERYMODE, deliveryMode);
            map.put(DestinationSourceMasterModel.BRAND, brand);
            map.put(DestinationSourceMasterModel.ORDERTYPE, orderType);
            map.put(DestinationSourceMasterModel.CUSTOMERCATEGORY, customerCategory);
            map.put(DestinationSourceMasterModel.DESTINATIONCITY, city.toUpperCase());
            map.put(DestinationSourceMasterModel.GRADE, grade);
            map.put(DestinationSourceMasterModel.PACKAGING, packaging);
            map.put(DestinationSourceMasterModel.DESTINATIONDISTRICT, district.toUpperCase());
            map.put(DestinationSourceMasterModel.DESTINATIONSTATE, state.toUpperCase());
            map.put(DestinationSourceMasterModel.SOURCE, source);

            String queryResult = "SELECT {ds:pk} from {DestinationSourceMaster as ds} where {ds:brand}=?brand and {ds:customerCategory}=?customerCategory and {ds:deliveryMode}=?deliveryMode and {ds:orderType}=?orderType and UPPER({ds:destinationCity})=UPPER(?destinationCity) and UPPER({ds:destinationDistrict})=UPPER(?destinationDistrict) " +
                    "and UPPER({ds:destinationState})=UPPER(?destinationState) and {ds:grade}=?grade and {ds:packaging}=?packaging and {ds.source}=?source";

            final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
            query.getQueryParameters().putAll(map);
            final SearchResult<DestinationSourceMasterModel> result = this.getFlexibleSearchService().search(query);
            return result.getResult() != null ? result.getResult().get(0) : new DestinationSourceMasterModel();
        }
        return new DestinationSourceMasterModel();
    }


    @Override
    public List<List<Object>> getSecondaryLeadDistance(SclUserModel sclUser, WarehouseType warehouseType, BaseSiteModel baseSite, List<String> territoryList) {

        final Map<String, Object> attr = new HashMap<String, Object>();
        SclUserModel user = (SclUserModel) userService.getCurrentUser();
        attr.put(WarehouseModel.TYPE, warehouseType);
        attr.put(OrderModel.SITE, baseSiteService.getCurrentBaseSite());
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT({oe:pk}),sum({distance}) ");
        sql.append("FROM {").append(OrderEntryModel._TYPECODE).append(" AS oe ");
        sql.append("JOIN ").append(OrderModel._TYPECODE).append(" AS o ");
        sql.append("ON {o:").append(OrderModel.PK).append("}={oe:").append(OrderEntryModel.ORDER).append("} ");
        sql.append("JOIN ").append(WarehouseModel._TYPECODE).append(" AS w ");
        sql.append("ON {o:").append(OrderModel.WAREHOUSE).append("}={w:").append(WarehouseModel.PK).append("}} ");
        sql.append("WHERE ").append("{w:").append(WarehouseModel.TYPE).append("} = ?type and ");
        sql.append(SclDateUtility.getMtdClauseQuery("oe.truckDispatcheddate", attr));

       /* List<DistrictMasterModel> list = new ArrayList<>();
        List<SubAreaMasterModel> list1 = new ArrayList<>();

        if (doList != null && !doList.isEmpty()) {
            for (String code : doList) {
                list.add(districtMasterDao.findByCode(code));
            }
            attr.put("doList", list);
            sql.append(" and {o.districtMaster} in (?doList) ");
        }
        if (subAreaList != null && !subAreaList.isEmpty()) {
            for (String id : subAreaList) {
                list1.add(territoryManagementDao.getTerritoryById(id));
            }
            attr.put("subAreaList", list1);
            sql.append(" and {o.subAreaMaster} in (?subAreaList) ");
        }*/
        List<TerritoryMasterModel> territoriesList = new ArrayList<>();
        if (territoryList != null && !territoryList.isEmpty()) {
            for (String id : territoryList) {
                territoriesList.add(territoryMasterService.getTerritoryById(id));
            }
            attr.put("territoriesModelList", territoriesList);
            sql.append(" and {o.territoryMaster} in (?territoriesModelList) ");
        }
        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Integer.class, Double.class));
        query.addQueryParameters(attr);
        LOGGER.info(String.format("getSecondaryLeadDistance:%s", query));
        final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult() != null && !searchResult.getResult().isEmpty() ? searchResult.getResult() : Collections.emptyList();

    }

    @Override
    public List<List<Object>> getSecondaryLeadDistanceMTD(SclUserModel sclUser, WarehouseType warehouseType, BaseSiteModel baseSite, List<String> territoryList) {
        //.append(SclDateUtility.getMtdClauseQuery("oe:deliveredDate", params));
        final Map<String, Object> attr = new HashMap<String, Object>();
        SclUserModel user = (SclUserModel) userService.getCurrentUser();
        attr.put(WarehouseModel.TYPE, warehouseType);
        attr.put(OrderModel.SITE, baseSiteService.getCurrentBaseSite());
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT({oe:pk}),sum({distance}) ");
        sql.append("FROM {").append(OrderEntryModel._TYPECODE).append(" AS oe ");
        sql.append("JOIN ").append(OrderModel._TYPECODE).append(" AS o ");
        sql.append("ON {o:").append(OrderModel.PK).append("}={oe:").append(OrderEntryModel.ORDER).append("} ");
        sql.append("JOIN ").append(WarehouseModel._TYPECODE).append(" AS w ");
        sql.append("ON {o:").append(OrderModel.WAREHOUSE).append("}={w:").append(WarehouseModel.PK).append("}} ");
        sql.append("WHERE ").append("{w:").append(WarehouseModel.TYPE).append("} = ?type and ");
        sql.append(SclDateUtility.getMtdClauseQuery("oe.truckDispatcheddate", attr));
       /* List<DistrictMasterModel> list = new ArrayList<>();
        List<SubAreaMasterModel> list1 = new ArrayList<>();

        if (doList != null && !doList.isEmpty()) {
            for (String code : doList) {
                list.add(districtMasterDao.findByCode(code));
            }
            attr.put("doList", list);
            sql.append(" and {o.districtMaster} in (?doList) ");
        }
        if (subAreaList != null && !subAreaList.isEmpty()) {
            for (String id : subAreaList) {
                list1.add(territoryManagementDao.getTerritoryById(id));
            }
            attr.put("subAreaList", list1);
            sql.append(" and {o.subAreaMaster} in (?subAreaList) ");
        }*/
        List<TerritoryMasterModel> territoriesList = new ArrayList<>();
        if (territoryList != null && !territoryList.isEmpty()) {
            for (String id : territoryList) {
                territoriesList.add(territoryMasterService.getTerritoryById(id));
            }
            attr.put("territoriesModelList", territoriesList);
            sql.append(" and {o.territoryMaster} in (?territoriesModelList) ");
        }
        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Integer.class, Double.class));
        query.addQueryParameters(attr);
        LOGGER.info(String.format("getSecondaryLeadDistanceMTD:%s", query));
        final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult() != null && !searchResult.getResult().isEmpty() ? searchResult.getResult() : Collections.emptyList();


    }

    @Override
    public List<List<Object>> getSecondaryLeadDistanceYTD(SclUserModel sclUser, WarehouseType warehouseType, BaseSiteModel baseSite, Date startDate, Date endDate, List<String> territoryList) {
        final Map<String, Object> attr = new HashMap<String, Object>();
        SclUserModel user = (SclUserModel) userService.getCurrentUser();
        attr.put(WarehouseModel.TYPE, warehouseType);
        attr.put(OrderModel.SITE, baseSiteService.getCurrentBaseSite());
        attr.put("startDate", startDate);
        attr.put("endDate", endDate);
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT({oe:pk}),sum({distance}) ");
        sql.append("FROM {").append(OrderEntryModel._TYPECODE).append(" AS oe ");
        sql.append("JOIN ").append(OrderModel._TYPECODE).append(" AS o ");
        sql.append("ON {o:").append(OrderModel.PK).append("}={oe:").append(OrderEntryModel.ORDER).append("} ");
        sql.append("JOIN ").append(WarehouseModel._TYPECODE).append(" AS w ");
        sql.append("ON {o:").append(OrderModel.WAREHOUSE).append("}={w:").append(WarehouseModel.PK).append("}} ");
        sql.append("WHERE ").append("{w:").append(WarehouseModel.TYPE).append("} = ?type and ");
        sql.append("{oe.truckDispatcheddate} >= ?startDate and {oe.truckDispatcheddate} <= ?endDate");
       /* List<DistrictMasterModel> list = new ArrayList<>();
        List<SubAreaMasterModel> list1 = new ArrayList<>();

        if (doList != null && !doList.isEmpty()) {
            for (String code : doList) {
                list.add(districtMasterDao.findByCode(code));
            }
            attr.put("doList", list);
            sql.append(" and {o.districtMaster} in (?doList) ");
        }
        if (subAreaList != null && !subAreaList.isEmpty()) {
            for (String id : subAreaList) {
                list1.add(territoryManagementDao.getTerritoryById(id));
            }
            attr.put("subAreaList", list1);
            sql.append(" and {o.subAreaMaster} in (?subAreaList) ");
        }*/
        List<TerritoryMasterModel> territoriesList = new ArrayList<>();
        if (territoryList != null && !territoryList.isEmpty()) {
            for (String id : territoryList) {
                territoriesList.add(territoryMasterService.getTerritoryById(id));
            }
            attr.put("territoriesModelList", territoriesList);
            sql.append(" and {o.territoryMaster} in (?territoriesModelList) ");
        }
        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Integer.class, Double.class));
        query.addQueryParameters(attr);
        LOGGER.info(String.format("getSecondaryLeadDistanceYTD:%s", query));
        final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult() != null && !searchResult.getResult().isEmpty() ? searchResult.getResult() : Collections.emptyList();

    }

    @Override
    public double getDistance(String source, String destination) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT {distance} FROM {DestinationSourceMaster} WHERE {source}=?source and {destination}=?destination ");
        params.put("source", source);
        params.put("destination", destination);
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
    public Double getActualTargetForSalesMTDDealer(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        // userService.setCurrentUser(userService.getAdminUser());
        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}} WHERE "
                + "{o:user} = ?sclCustomer and {o:versionID} IS NULL and {o:site} =?site and {oe:status}=?orderStatus and").append(SclDateUtility.getMtdClauseQuery("oe:deliveredDate", params));
        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("orderStatus", orderStatus);
        params.put("sclCustomer", sclCustomer);
        params.put("site", currentBaseSite);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);

        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        /*searchRestrictionService.disableSearchRestrictions();
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        searchRestrictionService.enableSearchRestrictions();*/
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    @Override
    public Double getActualTargetForSalesMTDRetailer(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        // userService.setCurrentUser(userService.getAdminUser());
        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderRequisition AS oe} WHERE "
                + "{oe:status}=?requisitionStatus and {oe.toCustomer}=?sclCustomer and").append(SclDateUtility.getMtdClauseQuery("oe:deliveredDate", params));
        RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
        params.put("requisitionStatus", requisitionStatus);
        params.put("sclCustomer", sclCustomer);
        params.put("site", currentBaseSite);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);

        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
       /* searchRestrictionService.disableSearchRestrictions();
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        searchRestrictionService.enableSearchRestrictions();*/
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    @Override
    public Double getActualTargetForSalesYTDDealer(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite, Date startDate, Date endDate, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
//        userService.setCurrentUser(userService.getAdminUser());
        final StringBuilder builder = new StringBuilder();

        if (StringUtils.isBlank(bgpFilter)) {
            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}}  WHERE {o:user} = ?sclCustomer and {oe.cancelledDate} is null and {oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} <= ?endDate");
        } else if (bgpFilter.equalsIgnoreCase("ALL")) {
            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}}  WHERE {o:user} = ?sclCustomer and {oe.cancelledDate} is null and {oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} <= ?endDate");
        } else {
            CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
            ProductModel product = productService.getProductForCode(catalogVersion, bgpFilter);
            if (product != null) {
                builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk} JOIN Product as p on {oe:product}={p:pk}}  WHERE {o:user} = ?sclCustomer and {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and {oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} <= ?endDate and {oe:product} =?product");

                //String filterKey = "%".concat(bgpFilter.toUpperCase()).concat("%");
                //String filterKey= "%".concat(String.valueOf(product)).concat("%");
                params.put("product", product);
            }
        }
        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("orderStatus", orderStatus);
        params.put("sclCustomer", sclCustomer);
        params.put("site", currentBaseSite);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);

        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        /*searchRestrictionService.disableSearchRestrictions();
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        searchRestrictionService.enableSearchRestrictions();*/
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }


    @Override
    public Double getActualTargetForSalesYTDSP(List<SclCustomerModel> sclCustomer, BaseSiteModel currentBaseSite, Date startDate, Date endDate) {
        {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder();

            for (SclCustomerModel customerModel : sclCustomer) {
                LOGGER.info(String.format("SclCustomer Model PK:%s", customerModel));
            }

            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}}  WHERE  {o:user} in (?sclCustomer) and {oe.cancelledDate} is null and {oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} <= ?endDate");
            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("orderStatus", orderStatus);
            params.put("sclCustomer", sclCustomer);
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
    }


    @Override
    public Double getActualTargetForSalesYTDRetailer(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite, Date startDate, Date endDate, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        LOGGER.info("StartDate and EndDate and SclCustomer:" + startDate + " " + endDate + " " + sclCustomer);
        if (StringUtils.isBlank(bgpFilter)) {
            builder.append("SELECT sum({oe:quantity}) from {OrderRequisition AS oe} WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?sclCustomer and {oe:deliveredDate}>=?startDate and {oe:deliveredDate}< ?endDate and {oe:product} is not null");
        } else if (bgpFilter.equalsIgnoreCase("ALL")) {
            builder.append("SELECT sum({oe:quantity}) from {OrderRequisition AS oe} WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?sclCustomer and {oe:deliveredDate}>=?startDate and {oe:deliveredDate}< ?endDate and {oe:product} is not null");
        } else {
            CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
            ProductModel product = productService.getProductForCode(catalogVersion, bgpFilter);
            if (product != null) {
                builder.append("SELECT sum({oe:quantity}) from {OrderRequisition AS oe JOIN Product as p on {oe:product}={p:pk}} WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?sclCustomer and {oe:deliveredDate}>=?startDate and {oe:deliveredDate}<=?endDate and {oe:product} =?product and {oe:product} is not null");
                params.put("product", product);
            }
        }
        RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
        params.put("requisitionStatus", requisitionStatus);
        params.put("sclCustomer", sclCustomer);
        params.put("site", currentBaseSite);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);

        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        /*searchRestrictionService.disableSearchRestrictions();
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        searchRestrictionService.enableSearchRestrictions();*/
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    public double getActualTargetFor10DayBucketForDealer(SclCustomerModel sclCustomer, String bgpFilter, Date startDate, Date endDate) {
        BaseSiteModel site = baseSiteService.getCurrentBaseSite();
        final Map<String, Object> params = new HashMap<String, Object>();

        final StringBuilder builder = new StringBuilder();
        if (StringUtils.isBlank(bgpFilter)) {
            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk}}  WHERE {o:user} = ?sclCustomer and {oe.cancelledDate} is null and {o:versionID} IS NULL and {o:site} =?site and {oe:invoiceCreationDateAndTime} >= ?startDate and {oe:invoiceCreationDateAndTime} < ?endDate");
        } else if (bgpFilter.equalsIgnoreCase("ALL")) {
            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk}}  WHERE {o:user} = ?sclCustomer and {oe.cancelledDate} is null and {o:versionID} IS NULL and {o:site} =?site and {oe:invoiceCreationDateAndTime} >= ?startDate and {oe:invoiceCreationDateAndTime} < ?endDate");
        } else {
            CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
            ProductModel product = productService.getProductForCode(catalogVersion, bgpFilter);
            if (product != null) {
                builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN Product as p on {oe:product}={p:pk}}  WHERE {o:user} = ?sclCustomer and {oe.cancelledDate} is null and {o:versionID} IS NULL and {o:site} =?site and {oe:invoiceCreationDateAndTime} >= ?startDate and {oe:invoiceCreationDateAndTime} < ?endDate and {oe:product} =?product");
                params.put("product", product);
            }
        }
        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("orderStatus", orderStatus);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("sclCustomer", sclCustomer);
        params.put("site", site);
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
    public double getActualTargetFor10DayBucket(SclUserModel sclUser, Date startDate, Date endDate, List<String> doList, List<String> subAreaList) {
        final Map<String, Object> params = new HashMap<String, Object>();

        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk}}  WHERE {oe:status}=?orderStatus and {oe:deliveredDate} >= ?startDate and {oe:deliveredDate} < ?endDate");
        List<DistrictMasterModel> list = new ArrayList<>();
        List<SubAreaMasterModel> list1 = new ArrayList<>();

        if (doList != null && !doList.isEmpty()) {
            for (String code : doList) {
                list.add(districtMasterDao.findByCode(code));
            }
            params.put("doList", list);
            builder.append(" and {oe.districtMaster} in (?doList) ");
        }
        if (subAreaList != null && !subAreaList.isEmpty()) {
            for (String id : subAreaList) {
                list1.add(territoryManagementDao.getTerritoryById(id));
            }
            params.put("subAreaList", list1);
            builder.append(" and {oe.subAreaMaster} in (?subAreaList) ");
        }
        OrderStatus orderStatus = OrderStatus.DELIVERED;
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

    @Override
    public ProductSaleModel getTotalTargetForProductBGPFilter(String customerCode, String productCode) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {ProductSale} where {customerCode}=?customerCode and {productCode}=?productCode and {isAnnualSalesReviewedForDealer}=?isAnnualSalesReviewedForDealer");
        params.put("customerCode", customerCode);
        params.put("productCode", productCode);
        params.put("isAnnualSalesReviewedForDealer", true);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(ProductSaleModel.class));
        final SearchResult<ProductSaleModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public ProductSaleModel getTotalTargetForProductBGPFilterMTD(String customerCode, String productCode, String month, String year) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {ProductSale} where {customerCode}=?customerCode and {productCode}=?productCode and {isMonthlySalesForReviewedDealer}=?isMonthlySalesForReviewedDealer and {monthName}=?month and {monthYear}=?year");
        params.put("customerCode", customerCode);
        params.put("productCode", productCode);
        params.put("isMonthlySalesForReviewedDealer", true);
        params.put("month", month);
        params.put("year", year);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(ProductSaleModel.class));
        final SearchResult<ProductSaleModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }


    @Override
    public DealerRevisedMonthlySalesModel getMonthlySaleTargetGraphForDealer(String customerCode, String month, String year) {
        final Map<String, Object> params = new HashMap<String, Object>();
       /* List<String> customerCodeList=new ArrayList<>();
        for (SclCustomerModel customerModel : sclCustomerModel) {
            customerCodeList.add(customerModel.getUid());
        }*/
        final StringBuilder builder = new StringBuilder("select {pk} from {DealerRevisedMonthlySales} where {customerCode}=?customerCode and {monthName}=?month and {monthYear}=?year");
        params.put("customerCode", customerCode);
        params.put("month", month);
        params.put("year", year);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(DealerRevisedMonthlySalesModel.class));
        final SearchResult<DealerRevisedMonthlySalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    public List<DealerRevisedMonthlySalesModel> getMonthlySaleTargetGraphForSP(List<SclCustomerModel> sclCustomer, String month, String year) {
        final Map<String, Object> params = new HashMap<String, Object>();
        List<String> customerCodeList = new ArrayList<>();
        for (SclCustomerModel customerModel : sclCustomer) {
            customerCodeList.add(customerModel.getUid());
        }
        final StringBuilder builder = new StringBuilder("select {pk} from {DealerRevisedMonthlySales} where {customerCode} in (?sclCustomer) and {monthName}=?month and {monthYear}=?year");
        params.put("sclCustomer", customerCodeList);
        params.put("month", month);
        params.put("year", year);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder);
        query.addQueryParameters(params);
        final SearchResult<DealerRevisedMonthlySalesModel> searchResult = flexibleSearchService.search(query);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();
    }


    public DealerRevisedMonthlySalesModel getMonthlySaleTargetGraph(SclUserModel sclUserModel, String month, String year, List<String> doList, List<String> subAreaList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {DealerRevisedMonthlySales} where {monthName}=?month and {monthYear}=?year");
        // params.put("customerCode", customerCode);
        List<DistrictMasterModel> list = new ArrayList<>();
        List<SubAreaMasterModel> list1 = new ArrayList<>();

        if (doList != null && !doList.isEmpty()) {
            for (String code : doList) {
                list.add(districtMasterDao.findByCode(code));
            }
            params.put("doList", list);
            builder.append(" and {oe.districtMaster} in (?doList) ");
        }
        if (subAreaList != null && !subAreaList.isEmpty()) {
            for (String id : subAreaList) {
                list1.add(territoryManagementDao.getTerritoryById(id));
            }
            params.put("subAreaList", list1);
            builder.append(" and {oe.subAreaMaster} in (?subAreaList) ");
        }
        params.put("month", month);
        params.put("year", year);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(DealerRevisedMonthlySalesModel.class));
        final SearchResult<DealerRevisedMonthlySalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public Integer findDirectDispatchOrdersMTDCount(final UserModel currentUser, final WarehouseType warehouseType, final int month, final int year, List<String> doList, List<String> territoryList) {
        final Map<String, Object> attr = new HashMap<String, Object>();
        SclUserModel user = (SclUserModel) userService.getCurrentUser();
        attr.put(WarehouseModel.TYPE, warehouseType);
        attr.put(OrderModel.SITE, baseSiteService.getCurrentBaseSite());
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT({oe:pk}) ");
        sql.append("FROM {").append(OrderEntryModel._TYPECODE).append(" AS oe ");
        sql.append("JOIN ").append(OrderModel._TYPECODE).append(" AS o ");
        sql.append("ON {o:").append(OrderModel.PK).append("}={oe:").append(OrderEntryModel.ORDER).append("} ");
        sql.append("JOIN ").append(WarehouseModel._TYPECODE).append(" AS w ");
        sql.append("ON {o:").append(OrderModel.WAREHOUSE).append("}={w:").append(WarehouseModel.PK).append("}} ");
        sql.append("WHERE ").append("{w:").append(WarehouseModel.TYPE).append("} = ?type and ");
        sql.append(SclDateUtility.getDateClauseQueryByMonthYear("oe.truckDispatcheddate", month, year, attr));

    /*    List<DistrictMasterModel> list = new ArrayList<>();
        List<SubAreaMasterModel> list1 = new ArrayList<>();

        if (doList != null && !doList.isEmpty()) {
            for (String code : doList) {
                list.add(districtMasterDao.findByCode(code));
            }
            attr.put("doList", list);
            sql.append(" and {o.districtMaster} in (?doList) ");
        }*/
        List<TerritoryMasterModel> list1 = new ArrayList<>();
        if (territoryList != null && !territoryList.isEmpty()) {
            for (String id : territoryList) {
                list1.add(territoryMasterService.getTerritoryById(id));
            }
            attr.put("territoryModelList", list1);
            sql.append(" and {o.territoryMaster} in (?territoryModelList) ");
        }
        //o.territoryMaster
        //terrimater dao method to get territory
        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Integer.class));
        query.getQueryParameters().putAll(attr);
        LOGGER.info(String.format("findDirectDispatchOrdersMTDCount:%s", query));
        final SearchResult<Integer> result = this.getFlexibleSearchService().search(query);
        return result.getResult().get(0);
    }

    @Override
    public Double getActualTargetForSalesLeaderYTDRetailer(SclCustomerModel sclUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();

        builder.append("SELECT sum({oe:quantity}) from " +
                "{OrderRequisition AS oe JOIN " +
                "Product as p on {oe:product}={p:pk}} WHERE " +
                "{oe:status}=?requisitionStatus and " +
                "{oe.toCustomer}=?customer and {oe.deliveredDate} >= ?startDate AND {oe.deliveredDate} <= ?endDate");
        RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
        params.put("requisitionStatus", requisitionStatus);
        params.put("customer", sclUser);
        params.put("site", currentBaseSite);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("premium", "N");
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

    public List<List<Object>> getActualTargetForSalesLeader(DistrictMasterModel district, BaseSiteModel currentBaseSite, Date startDate, Date endDate) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder();

            LOGGER.info("Getting sales for the Dealer");

            builder.append("SELECT {sc.uid},SUM({oe.quantityInMt}) FROM {SclCustomer AS sc JOIN CustomerSubAreaMapping AS cs ON {sc:pk} = {cs:sclCustomer} JOIN SubAreaMaster AS sm ON {sm.pk} = {cs.subAreaMaster} JOIN DistrictMaster AS d ON {d.pk} = {sm.districtMaster} LEFT JOIN Order AS o ON {cs.sclCustomer} = {o.user} LEFT JOIN OrderEntry AS oe ON {oe.order} = {o.pk} AND {oe.status} = ?orderStatus AND {oe.deliveredDate} >= ?startDate AND {oe.deliveredDate} <= ?endDate} WHERE {cs.brand}=?site AND {cs.isOtherBrand}=?isOtherBrand and {sc.counterType}=?counterType AND {d.pk} = ?district GROUP BY {sc.uid} ORDER BY SUM({oe.quantityInMt})");

            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("orderStatus", orderStatus);
            //params.put("sclCustomer", sclUser);
            params.put("district", district);
            params.put("site", currentBaseSite);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            params.put("counterType", CounterType.DEALER);
            params.put(CustomerSubAreaMappingModel.ISOTHERBRAND, Boolean.FALSE);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class, Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return result != null && !result.isEmpty() ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));

        }
    }

    @Override
    public List<List<Object>> getPremiumForSalesLeader(DistrictMasterModel district, BaseSiteModel currentBaseSite, Date startDate, Date endDate) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder();

            LOGGER.info("Getting Premium Sales for the Dealer");

            // builder.append("SELECT {sc.uid},SUM(CASE WHEN {p.premium} =?premium THEN {oe.quantityInMt} ELSE 0 END) AS totalQuantity FROM {SclCustomer AS sc JOIN CustomerSubAreaMapping AS cs ON {sc:pk} = {cs:sclCustomer} JOIN SubAreaMaster AS sm ON {sm.pk} = {cs.subAreaMaster} JOIN DistrictMaster AS d ON {d.pk} = {sm.districtMaster} LEFT JOIN Order AS o ON {cs.sclCustomer} = {o.user} LEFT JOIN OrderEntry AS oe ON {oe.order} = {o.pk} join Product as p on {oe:product}={p:pk} AND {oe.status} = ?orderStatus AND {oe.deliveredDate} >= ?startDate AND {oe.deliveredDate} <= ?endDate and LEFT JOIN Product AS p ON {oe:product} = {p:pk}} WHERE {cs.brand}=?site AND {cs.isOtherBrand}=?isOtherBrand and {sc.counterType}=?counterType AND {d.pk} = ?district GROUP BY {sc.uid} ORDER BY totalQuantity");
            //builder.append("select {o.user}, sum({oe.quantityInMt}) from {CustomerSubAreaMapping as cs join Order as o on {cs.sclCustomer}={o.user} join OrderEntry as oe on {oe.order}={o.pk} join Product as p on {oe:product}={p:pk}} WHERE {o.retailer} is null and {p.premium}=?premium and {oe.deliveredDate} >= ?startDate and {oe.deliveredDate} <= ?endDate  group by {o.user} order by sum({oe.quantityInMt}) desc");
            builder.append("SELECT {sc.uid}, SUM(CASE WHEN {p.premium} =?premium THEN {oe.quantityInMt} ELSE 0 END) AS totalQuantity FROM {SclCustomer AS sc JOIN CustomerSubAreaMapping AS cs ON {sc:pk} = {cs:sclCustomer} JOIN SubAreaMaster AS sm ON {sm.pk} = {cs.subAreaMaster} JOIN DistrictMaster AS d ON {d.pk} = {sm.districtMaster} LEFT JOIN Order AS o ON {cs.sclCustomer} = {o.user} LEFT JOIN OrderEntry AS oe ON {oe.order} = {o.pk} AND {oe.status} =?orderStatus AND {oe.deliveredDate} >= ?startDate AND {oe.deliveredDate} <= ?endDate LEFT JOIN Product AS p ON {oe:product} = {p:pk}} WHERE {cs.brand}=?site AND {cs.isOtherBrand}=?isOtherBrand AND {sc.counterType}=?counterType AND {d.pk} = ?district GROUP BY {sc.uid} ORDER BY totalQuantity");
            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("orderStatus", orderStatus);
            params.put("district", district);
            params.put("site", currentBaseSite);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            params.put("premium", "Y");
            params.put("counterType", CounterType.DEALER);
            params.put(CustomerSubAreaMappingModel.ISOTHERBRAND, Boolean.FALSE);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class, Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return result != null && !result.isEmpty() ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<SclCustomerModel> getOrderRequisitionSalesDataForRetailer(SclCustomerModel sclCustomer, BaseSiteModel brand, List<String> doList, List<String> subAreaList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder queryString = new StringBuilder("SELECT DISTINCT({sc:pk}) from {SclCustomer as sc LEFT JOIN OrderRequisition AS oe on {sc:pk}={oe:toCustomer}} WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?sclCustomer and ").append(SclDateUtility.getMtdClauseQuery("oe:deliveredDate", params));

        //  queryString.append( " group by {sc:pk} having SUM({oe.quantity})==0 OR SUM({oe:quantity}) is NULL");


        List<DistrictMasterModel> list = new ArrayList<>();
        List<SubAreaMasterModel> list1 = new ArrayList<>();

        if (doList != null && !doList.isEmpty()) {
            for (String code : doList) {
                list.add(districtMasterDao.findByCode(code));
            }
            params.put("doList", list);
            queryString.append(" and {oe.districtMaster} in (?doList) ");
        }
        if (subAreaList != null && !subAreaList.isEmpty()) {
            for (String id : subAreaList) {
                list1.add(territoryManagementDao.getTerritoryById(id));
            }
            params.put("subAreaList", list1);
            queryString.append(" and {oe.subAreaMaster} in (?subAreaList) ");
        }
        RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
        params.put("sclCustomer", sclCustomer);
        params.put("brand", brand);
        params.put("requisitionStatus", requisitionStatus);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();
    }

    @Override
    public SclCustomerModel getRetailerSalesForDealer(SclCustomerModel sclCustomer, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        //select {d.retailer} from {DealerRetailerMap as d join SclCustomer as c  on {d.retailer}={c.pk}} WHERE {d.dealer}=?sclCustomer AND {d.active}=?active AND {d.brand}=?brand
        final StringBuilder queryString = new StringBuilder("SELECT DISTINCT({sc:retailer}) from {DealerRetailerMapping as sc LEFT JOIN OrderRequisition AS oe on {sc:retailer}={oe:toCustomer}} " +
                "WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?sclCustomer and ").append(SclDateUtility.getMtdClauseQuery("oe:deliveredDate", params));
        RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
        params.put("sclCustomer", sclCustomer);
        params.put("brand", brand);
        params.put("requisitionStatus", requisitionStatus);
        /*final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<DealerRetailerMapModel> searchResult = flexibleSearchService.search(query);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();*/
        FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.setResultClassList(Collections.singletonList(SclCustomerModel.class));
        query.addQueryParameters(params);
        final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult() != null && !searchResult.getResult().isEmpty() ? searchResult.getResult().get(0) : null;

    }

    @Override
    public SclCustomerModel getRetailerSalesForDealerLowPerform(SclCustomerModel sclCustomer, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder queryString = new StringBuilder("SELECT DISTINCT({sc:retailer}) from {DealerRetailerMapping as sc LEFT JOIN OrderRequisition AS oe on {sc:retailer}={oe:toCustomer}} WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?sclCustomer and ").append(SclDateUtility.getMtdClauseQuery("oe:deliveredDate", params));
        queryString.append(" group by {sc:retailer} having SUM({oe.quantity})!=0 OR SUM({oe:quantity}) is NOT NULL");
        RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
        params.put("sclCustomer", sclCustomer);
        params.put("brand", brand);
        params.put("requisitionStatus", requisitionStatus);
       /* final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<DealerRetailerMapModel> searchResult = flexibleSearchService.search(query);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();*/
        FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.setResultClassList(Collections.singletonList(SclCustomerModel.class));
        query.addQueryParameters(params);
        final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult() != null && !searchResult.getResult().isEmpty() ? searchResult.getResult().get(0) : null;
    }

    @Override
    public List<DealerRetailerMapModel> getRetailerSalesForDealerZeroLift(SclCustomerModel sclCustomer, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder queryString = new StringBuilder("SELECT DISTINCT({sc:pk}) from {DealerRetailerMapping as sc LEFT JOIN OrderRequisition AS oe on {sc:retailer}={oe:toCustomer}} WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?sclCustomer and ").append(SclDateUtility.getMtdClauseQuery("oe:deliveredDate", params));
        queryString.append(" group by {sc:pk} having SUM({oe.quantity})=0 OR SUM({oe:quantity}) is NULL");
        RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
        params.put("sclCustomer", sclCustomer);
        params.put("brand", brand);
        params.put("requisitionStatus", requisitionStatus);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<DealerRetailerMapModel> searchResult = flexibleSearchService.search(query);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();
    }

    @Override
    public List<DealerInfluencerMapModel> getInfluencerSalesForDealer(SclCustomerModel sclCustomer, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder queryString = new StringBuilder("SELECT DISTINCT({sc:pk}) from {DealerInfluencerMap as sc LEFT JOIN PointRequisition AS oe on {sc:influencer}={oe:requestRaisedFor}} WHERE {oe:status}=?requisitionStatus and {oe.requestRaisedFor}=?sclCustomer and  ").append(SclDateUtility.getMtdClauseQuery("oe:deliveryDate", params));
        PointRequisitionStatus requisitionStatus = PointRequisitionStatus.APPROVED;
        params.put("sclCustomer", sclCustomer);
        params.put("brand", brand);
        params.put("requisitionStatus", requisitionStatus);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<DealerInfluencerMapModel> searchResult = flexibleSearchService.search(query);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();
    }

    @Override
    public List<DealerInfluencerMapModel> getInfluencerSalesForDealerLowPerform(SclCustomerModel sclCustomer, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder queryString = new StringBuilder("SELECT DISTINCT({sc:pk}) from {DealerInfluencerMap as sc LEFT JOIN PointRequisition AS oe on {sc:influencer}={oe:requestRaisedFor}} WHERE {oe:status}=?requisitionStatus and {oe.requestRaisedFor}=?sclCustomer and ").append(SclDateUtility.getMtdClauseQuery("oe:deliveryDate", params));
        queryString.append(" group by {sc:pk} having SUM({oe.quantity})!=0 OR SUM({oe:quantity}) is NOT NULL");

        PointRequisitionStatus requisitionStatus = PointRequisitionStatus.APPROVED;
        params.put("sclCustomer", sclCustomer);
        params.put("brand", brand);
        params.put("requisitionStatus", requisitionStatus);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<DealerInfluencerMapModel> searchResult = flexibleSearchService.search(query);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();
    }

    @Override
    public List<DealerInfluencerMapModel> getInfluencerSalesForDealerZeroLift(SclCustomerModel sclCustomer, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder queryString = new StringBuilder("SELECT DISTINCT({sc:pk}) from {DealerInfluencerMap as sc LEFT JOIN PointRequisition AS oe on {sc:influencer}={oe:requestRaisedFor}} WHERE {oe:status}=?requisitionStatus and {oe.requestRaisedFor}=?sclCustomer and ").append(SclDateUtility.getMtdClauseQuery("oe:deliveryDate", params));
        queryString.append(" group by {sc:pk} having SUM({oe.quantity})=0 OR SUM({oe:quantity}) is NULL");
        PointRequisitionStatus requisitionStatus = PointRequisitionStatus.APPROVED;
        params.put("sclCustomer", sclCustomer);
        params.put("brand", brand);
        params.put("requisitionStatus", requisitionStatus);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<DealerInfluencerMapModel> searchResult = flexibleSearchService.search(query);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();
    }

    @Override
    public List<SclCustomerModel> getOrderRequisitionSalesDataForZero(SclCustomerModel sclCustomer, BaseSiteModel brand, List<String> doList, List<String> subAreaList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder queryString = new StringBuilder("SELECT DISTINCT({sc:pk}) from {SclCustomer as sc LEFT JOIN OrderRequisition AS oe on {sc:pk}={oe:toCustomer}} WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?sclCustomer and ").append(SclDateUtility.getMtdClauseQuery("oe:deliveredDate", params));

        queryString.append(" group by {sc:pk} having SUM({oe.quantity})=0 OR SUM({oe:quantity}) is NULL");

        List<DistrictMasterModel> list = new ArrayList<>();
        List<SubAreaMasterModel> list1 = new ArrayList<>();

        if (doList != null && !doList.isEmpty()) {
            for (String code : doList) {
                list.add(districtMasterDao.findByCode(code));
            }
            params.put("doList", list);
            queryString.append(" and {oe.districtMaster} in (?doList) ");
        }
        if (subAreaList != null && !subAreaList.isEmpty()) {
            for (String id : subAreaList) {
                list1.add(territoryManagementDao.getTerritoryById(id));
            }
            params.put("subAreaList", list1);
            queryString.append(" and {oe.subAreaMaster} in (?subAreaList) ");
        }
        RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
        params.put("sclCustomer", sclCustomer);
        params.put("brand", brand);
        params.put("requisitionStatus", requisitionStatus);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();
    }

    @Override
    public List<SclCustomerModel> getOrderRequisitionSalesDataForLowPerform(SclCustomerModel sclCustomer, BaseSiteModel brand, List<String> doList, List<String> subAreaList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder queryString = new StringBuilder("SELECT DISTINCT({sc:pk}) from {SclCustomer as sc LEFT JOIN OrderRequisition AS oe on {sc:pk}={oe:toCustomer}} WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?sclCustomer and ").append(SclDateUtility.getMtdClauseQuery("oe:deliveredDate", params));

        queryString.append(" group by {sc:pk} having SUM({oe.quantity})!=0 OR SUM({oe:quantity}) is NOT NULL");


        List<DistrictMasterModel> list = new ArrayList<>();
        List<SubAreaMasterModel> list1 = new ArrayList<>();

        if (doList != null && !doList.isEmpty()) {
            for (String code : doList) {
                list.add(districtMasterDao.findByCode(code));
            }
            params.put("doList", list);
            queryString.append(" and {oe.districtMaster} in (?doList) ");
        }
        if (subAreaList != null && !subAreaList.isEmpty()) {
            for (String id : subAreaList) {
                list1.add(territoryManagementDao.getTerritoryById(id));
            }
            params.put("subAreaList", list1);
            queryString.append(" and {oe.subAreaMaster} in (?subAreaList) ");
        }
        RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
        params.put("sclCustomer", sclCustomer);
        params.put("brand", brand);
        params.put("requisitionStatus", requisitionStatus);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();
    }

    @Override
    public List<SclCustomerModel> getPointRequisitionSalesDataForInfluencer(SclCustomerModel sclCustomer, BaseSiteModel brand, List<String> doList, List<String> subAreaList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder queryString = new StringBuilder("SELECT DISTINCT({sc:pk}) from {SclCustomer as sc LEFT JOIN PointRequisition AS oe on {sc:pk}={oe:requestRaisedFor}} WHERE {oe:status}=?status and {oe.requestRaisedFor}=?sclCustomer and ").append(SclDateUtility.getMtdClauseQuery("oe:deliveryDate", params));
        //queryString.append( " group by {sc:pk} having SUM({oe.quantity})==0 OR SUM({oe:quantity}) is NULL");
        List<DistrictMasterModel> list = new ArrayList<>();
        List<SubAreaMasterModel> list1 = new ArrayList<>();

        if (doList != null && !doList.isEmpty()) {
            for (String code : doList) {
                list.add(districtMasterDao.findByCode(code));
            }
            params.put("doList", list);
            queryString.append(" and {oe.districtMaster} in (?doList) ");
        }
        if (subAreaList != null && !subAreaList.isEmpty()) {
            for (String id : subAreaList) {
                list1.add(territoryManagementDao.getTerritoryById(id));
            }
            params.put("subAreaList", list1);
            queryString.append(" and {oe.subAreaMaster} in (?subAreaList) ");
        }
        PointRequisitionStatus status = PointRequisitionStatus.APPROVED;
        params.put("sclCustomer", sclCustomer);
        params.put("brand", brand);
        params.put("status", status);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();
    }

    @Override
    public List<SclCustomerModel> getPointRequisitionSalesDataForZero(SclCustomerModel sclCustomer, BaseSiteModel brand, List<String> doList, List<String> subAreaList) {
        final Map<String, Object> params = new HashMap<String, Object>();

        final StringBuilder queryString = new StringBuilder("SELECT DISTINCT({sc:pk}) from {SclCustomer as sc LEFT JOIN PointRequisition AS oe on {sc:pk}={oe:requestRaisedFor}} WHERE {oe:status}=?status and {oe.requestRaisedFor}=?sclCustomer and ").append(SclDateUtility.getMtdClauseQuery("oe:deliveryDate", params));
        queryString.append(" group by {sc:pk} having SUM({oe.quantity})=0 OR SUM({oe:quantity}) is NULL");

        List<DistrictMasterModel> list = new ArrayList<>();
        List<SubAreaMasterModel> list1 = new ArrayList<>();

        if (doList != null && !doList.isEmpty()) {
            for (String code : doList) {
                list.add(districtMasterDao.findByCode(code));
            }
            params.put("doList", list);
            queryString.append(" and {oe.districtMaster} in (?doList) ");
        }
        if (subAreaList != null && !subAreaList.isEmpty()) {
            for (String id : subAreaList) {
                list1.add(territoryManagementDao.getTerritoryById(id));
            }
            params.put("subAreaList", list1);
            queryString.append(" and {oe.subAreaMaster} in (?subAreaList) ");
        }
        PointRequisitionStatus status = PointRequisitionStatus.APPROVED;
        params.put("sclCustomer", sclCustomer);
        params.put("brand", brand);
        params.put("status", status);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();
    }

    @Override
    public List<SclCustomerModel> getPointRequisitionSalesDataForLowPerform(SclCustomerModel sclCustomer, BaseSiteModel brand, List<String> doList, List<String> subAreaList) {
        final Map<String, Object> params = new HashMap<String, Object>();

        final StringBuilder queryString = new StringBuilder("SELECT DISTINCT({sc:pk}) from {SclCustomer as sc LEFT JOIN PointRequisition AS oe on {sc:pk}={oe:requestRaisedFor}} WHERE {oe:status}=?status and {oe.requestRaisedFor}=?sclCustomer and ").append(SclDateUtility.getMtdClauseQuery("oe:deliveryDate", params));
        queryString.append(" group by {sc:pk} having SUM({oe.quantity})!=0 OR SUM({oe:quantity}) is NOT NULL");

        List<DistrictMasterModel> list = new ArrayList<>();
        List<SubAreaMasterModel> list1 = new ArrayList<>();

        if (doList != null && !doList.isEmpty()) {
            for (String code : doList) {
                list.add(districtMasterDao.findByCode(code));
            }
            params.put("doList", list);
            queryString.append(" and {oe.districtMaster} in (?doList) ");
        }
        if (subAreaList != null && !subAreaList.isEmpty()) {
            for (String id : subAreaList) {
                list1.add(territoryManagementDao.getTerritoryById(id));
            }
            params.put("subAreaList", list1);
            queryString.append(" and {oe.subAreaMaster} in (?subAreaList) ");
        }
        PointRequisitionStatus status = PointRequisitionStatus.APPROVED;
        params.put("sclCustomer", sclCustomer);
        params.put("brand", brand);
        params.put("status", status);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();
    }

    @Override
    public List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForMonthPickerForDealer(SclUserModel sclUser, BaseSiteModel baseSite, int month, int year, List<String> doList, List<String> subAreaList, SclCustomerModel customer) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            //SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
            StringBuilder builder = new StringBuilder();
          /*  if((customer.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
                builder = new StringBuilder("SELECT {p:code},{p:name},sum({oe:quantityInMT}) from {OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN Product as p on {oe:product}={p:pk}} where {oe.cancelledDate} is null and ").append(SclDateUtility.getDateClauseQueryByMonthYear("oe:invoiceCreationDateAndTime", month, year, params));

                if (Objects.nonNull(customer)) {
                    builder.append(" and {o:user}=?customer ");
                    params.put("customer", customer);
                }
                builder.append(" group by {p:code},{p:name}");
                OrderStatus orderStatus = OrderStatus.DELIVERED;
                //params.put("sclUser", sclUser);
                params.put("baseSite", baseSite);
                params.put("orderStatus", orderStatus);
            }
            else*/
            if ((customer.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
                /*builder.append("SELECT {p:code},{p:name},sum({or:quantity}) from " +
                        "{OrderRequisition AS oe JOIN " +
                        "Product as p on {oe:product}={p:pk}} WHERE " +
                        "{oe:status}=?requisitionStatus and " +
                        "{oe.toCustomer}=?customer and " +
                        SclDateUtility.getDateClauseQueryByMonthYear("oe:deliveredDate", month, year, params));
                builder.append(" group by {p:code},{p:name}");
                RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
                params.put("customer", customer);
                params.put("requisitionStatus", requisitionStatus);*/

                StringBuilder builder1=new StringBuilder();
                StringBuilder builder2=new StringBuilder();
                if(!(userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
                    builder1.append("SELECT {p:code},{or:aliasCode},sum({or:quantity}),{or.equivalenceProductCode} from {OrderRequisition AS or JOIN Product as p on {or:product}={p:pk}} " +
                            " where {or:status}=?requisitionStatus and {or.toCustomer} = ?sclRetailer and {or:product} is not null and {or:requisitionType}=?requisitionType and " +
                            SclDateUtility.getDateClauseQueryByMonthYear("or:liftingDate", month, year, params));
                    if ((userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
                        builder1.append(" and {or:fromCustomer}=?sclDealer ");
                        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
                        params.put("sclDealer", currentUser);
                    }
                    builder2.append("SELECT {p.code},{m:aliasCode},sum({m:quantityInMT}),{p.equivalenceProductCode} from {MasterStockAllocation AS m join Product as p on {m:product}={p:pk}} " +
                            " where {m.retailer} =?sclRetailer and {m:aliasCode} is not null and {m:isInvoiceCancelled}=?cancellationFlag  and " +

                            SclDateUtility.getDateClauseQueryByMonthYear("m:invoicedDate", month, year, params));
                    if ((userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
                        builder2.append(" and {m:dealer}=?sclDealer ");
                        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
                        params.put("sclDealer", currentUser);
                    }
                }else{
                    builder1.append("SELECT {p:code},{or:aliasCode},sum({or:quantityInBags}),{or.equivalenceProductCode} from {OrderRequisition AS or JOIN Product as p on {or:product}={p:pk}} " +
                            " where {or:status}=?requisitionStatus and {or.toCustomer} = ?sclRetailer and {or:product} is not null and {or:requisitionType}=?requisitionType and " +
                            SclDateUtility.getDateClauseQueryByMonthYear("or:liftingDate", month, year, params));

                    builder2.append("SELECT {p.code},{m:aliasCode},sum({m:quantityInBags}),{p.equivalenceProductCode} from {MasterStockAllocation AS m join Product as p on {m:product}={p:pk}} " +
                            " where {m.retailer} =?sclRetailer and {m:aliasCode} is not null and {m:isInvoiceCancelled}=?cancellationFlag  and " +

                            SclDateUtility.getDateClauseQueryByMonthYear("m:invoicedDate", month, year, params));
                }
              /*  if((.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
                    builder.append(" and {m:dealer}=?sclDealer and {or:fromCustomer}=?sclDealer ");
                    params.put("sclDealer", currentUser);
                }*/
                builder1.append(" group by {p:code},{or:aliasCode},{or.equivalenceProductCode}");
                builder2.append(" group by {p:code},{m:aliasCode},{p.equivalenceProductCode} ");
                /*RequisitionStatus requisitionStatus = RequisitionStatus.SERVICED_BY_DEALER;
                params.put("customer", sclUser);
                params.put("requisitionStatus", requisitionStatus);*/
                params.put("sclRetailer", customer);
                // params.put("product", product);
                params.put("requisitionStatus", RequisitionStatus.SERVICED_BY_DEALER);
                params.put("requisitionType", RequisitionType.LIFTING);
                params.put("cancellationFlag", Boolean.FALSE);


                final FlexibleSearchQuery query1 = new FlexibleSearchQuery(builder1.toString());
                query1.addQueryParameters(params);
                LOGGER.info(String.format("Query for Product Mix:%s",query1));
                query1.setResultClassList(Arrays.asList(String.class, String.class, Double.class,String.class));
                final SearchResult<List<Object>> searchResult1 = flexibleSearchService.search(query1);
                List<List<Object>> result1 = searchResult1.getResult();
                //return (result1 != null && !result1.isEmpty()) ? result1 : Collections.emptyList();

                final FlexibleSearchQuery query2 = new FlexibleSearchQuery(builder2.toString());
                query2.addQueryParameters(params);
                LOGGER.info(String.format("Query for Product Mix:%s",query2));
                query2.setResultClassList(Arrays.asList(String.class, String.class, Double.class,String.class));
                final SearchResult<List<Object>> searchResult2 = flexibleSearchService.search(query2);
                List<List<Object>> result2 = searchResult2.getResult();

                if(CollectionUtils.isNotEmpty(result1) && CollectionUtils.isNotEmpty(result2)) {
                    Map<String, List<Object>> resultMap1 = mapByCode(result1);
                    Map<String, List<Object>> resultMap2 = mapByCode(result2);

                    for (String code : resultMap1.keySet()) {
                        if (resultMap2.containsKey(code)) {
                            List<Object> lista = resultMap1.get(code);
                            List<Object> listb = resultMap2.get(code);

                            double qty1 = (double) lista.get(2);
                            double qty2 = (double) listb.get(2);
                            double totalQty = qty1 + qty2;

                            lista.set(2, totalQty);
                            listb.set(2, totalQty);

                            LOG.info(String.format("Updated Qty for code {%s} in result1: {%s}", code, totalQty));
                            LOG.info(String.format("Updated Qty for code {%s} in result2: {%s}", code, totalQty));
                        }else{
                            LOG.info(String.format("not Updated Qty for code {%s} in result1: {%s}  ", code,(resultMap1.get(code)!=null && resultMap1.get(code).get(2)!=null) ?resultMap1.get(code).get(2):null));
                            //LOG.info(String.format("not Updated Qty for code {%s} in result2: {%s}  ", code, resultMap2.get(code).get(2)));
                        }
                    }
                }

                List<List<Object>> result3 =new ArrayList<>();
                result3.addAll(result1);
                result3.addAll(result2);
                List<List<Object>> lists = removeDuplicates(result3);
                return (lists != null && !lists.isEmpty()) ? lists.stream().distinct().collect(Collectors.toList()) : Collections.emptyList();
                //return (result3 != null && !result3.isEmpty()) ? result3.stream().distinct().collect(Collectors.toList()) : Collections.emptyList();
            }
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
        return null;
    }


    @Override
    public List<SclCustomerModel> getSclCustomerLastLiftingList(List<SclCustomerModel> customerFilteredList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        LocalDate firstDayOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDayOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);

        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime firstDay = firstDayOfMonth.atStartOfDay(zone);
        ZonedDateTime lastDay = lastDayOfMonth.atStartOfDay(zone);
        Date date1 = Date.from(firstDay.toInstant());
        Date date2 = Date.from(lastDay.toInstant());

        if (customerFilteredList != null && !customerFilteredList.isEmpty()) {
            final StringBuilder builder = new StringBuilder("SELECT {c.pk} FROM {SclCustomer as c} Where {c:pk} in (?customerFilteredList) and {c:lastLiftingDate} is NOT NULL and {c:lastLiftingDate}>=?firstDay and  {c:lastLiftingDate}<?lastDay ");

            //builder.append(SclDateUtility.getMtdClauseQuery("c:lastLiftingDate", params));

            params.put("customerFilteredList", customerFilteredList);
            params.put("firstDay", date1);
            params.put("lastDay", date2);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Collections.singletonList(SclCustomerModel.class));
            final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
            List<SclCustomerModel> result = searchResult.getResult();
            return result != null && !result.isEmpty() ? result : Collections.emptyList();
        } else {
            return null;
        }
    }

    @Override
    public List<SclCustomerModel> getSclCustomerZeroLiftingList(List<SclCustomerModel> customerFilteredList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        LocalDate firstDayOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDayOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);

        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime firstDay = firstDayOfMonth.atStartOfDay(zone);
        ZonedDateTime lastDay = lastDayOfMonth.atStartOfDay(zone);
        Date date1 = Date.from(firstDay.toInstant());
        Date date2 = Date.from(lastDay.toInstant());

        if (customerFilteredList != null && !customerFilteredList.isEmpty()) {
            //final StringBuilder builder = new StringBuilder("SELECT {c.pk} FROM {SclCustomer as c} Where {c:pk} in (?customerFilteredList) and {c:lastLiftingDate} is NULL");
            final StringBuilder builder = new StringBuilder("SELECT {c.pk} FROM {SclCustomer as c} Where {c:pk} in (?customerFilteredList) and (({c:lastLiftingDate}<=?firstDay and  {c:lastLiftingDate}>=?lastDay) OR {c:lastLiftingDate} is NULL ) ");
            //   builder.append(SclDateUtility.getMtdClauseQuery("c:lastLiftingDate", params));
            params.put("customerFilteredList", customerFilteredList);
            params.put("firstDay", date1);
            params.put("lastDay", date2);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Collections.singletonList(SclCustomerModel.class));
            final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
            List<SclCustomerModel> result = searchResult.getResult();
            return result != null && !result.isEmpty() ? result : Collections.emptyList();
        } else {
            return null;
        }
    }

    @Override
    public OrderRequisitionModel getSclCustomerFromOrderReq(SclCustomerModel customerModel) {
        if (customerModel != null) {
            Map<String, Object> map = new HashMap<String, Object>();
            StringBuilder builder = new StringBuilder("Select {pk} from {OrderRequisition} where {toCustomer}=?customerModel");
            map.put("customerModel", customerModel);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.setResultClassList(Collections.singletonList(CustomersInfluencerMapModel.class));
            query.addQueryParameters(map);
            final SearchResult<OrderRequisitionModel> searchResult = flexibleSearchService.search(query);
            if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
                return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
            else
                return null;
        }
        return null;
    }

    @Override
    public Double getRetailerFromOrderReq(SclCustomerModel customerModel, Date startDate, Date endDate) {
        if (customerModel != null) {
            Map<String, Object> map = new HashMap<String, Object>();
            StringBuilder builder = new StringBuilder("Select SUM({quantity}) from {OrderRequisition} where {toCustomer}=?customerModel and {status}=?requisitionStatus ");
            if (startDate != null && endDate != null) {
                builder.append(" and {deliveredDate}>=?startDate  and {deliveredDate}<=?endDate");
            }
            RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
            ;
            map.put("requisitionStatus", requisitionStatus);
            map.put("customerModel", customerModel);
            map.put("startDate", startDate);
            map.put("endDate", endDate);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.setResultClassList(Collections.singletonList(Double.class));
            query.addQueryParameters(map);
            final SearchResult<Double> searchResult = flexibleSearchService.search(query);
            if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
                return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
            else
                return 0.0;
        } else {
            return 0.0;
        }
    }

    @Override
    public OrderRequisitionModel getRetailerFromOrderReqDateConstraint(SclCustomerModel customerModel, Date startDate, Date endDate) {
        if (customerModel != null) {
            Map<String, Object> map = new HashMap<String, Object>();
            StringBuilder builder = new StringBuilder("Select {pk} from {OrderRequisition as o JOIN SclCustomer as sc on {sc:pk}={o:toCustomer}} where {o:status}=?requisitionStatus and {o:deliveredDate}>=?startDate and {o:deliveredDate}<=?endDate and  {o.toCustomer}=?customerModel ");
            RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
            ;
            map.put("requisitionStatus", requisitionStatus);
            map.put("customerModel", customerModel);
            map.put("startDate", startDate);
            map.put("endDate", endDate);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.setResultClassList(Collections.singletonList(CustomersInfluencerMapModel.class));
            query.addQueryParameters(map);
            final SearchResult<OrderRequisitionModel> searchResult = flexibleSearchService.search(query);
            if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
                return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
            else
                return null;
        }
        return null;
    }

    @Override
    public Double getInfluencerFromOrderReq(SclCustomerModel customerModel, Date startDate, Date endDate) {
        if (customerModel != null) {
            Map<String, Object> map = new HashMap<String, Object>();
            StringBuilder builder = new StringBuilder("Select SUM({quantity}) from {PointRequisition} where {requestRaisedFor}=?customerModel and {deliveryDate}>=?startDate and {deliveryDate}<=?endDate and {status}=?requisitionStatus ");
            PointRequisitionStatus requisitionStatus = PointRequisitionStatus.APPROVED;
            map.put("requisitionStatus", requisitionStatus);
            map.put("customerModel", customerModel);
            map.put("startDate", startDate);
            map.put("endDate", endDate);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.setResultClassList(Collections.singletonList(Double.class));
            query.addQueryParameters(map);
            final SearchResult<Double> searchResult = flexibleSearchService.search(query);
            if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
                return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
            else
                return 0.0;
        } else {
            return 0.0;
        }
    }

    @Override
    public PointRequisitionModel getSclCustomerFromPointReq(SclCustomerModel customerModel) {
        if (customerModel != null) {
            Map<String, Object> map = new HashMap<String, Object>();
            StringBuilder builder = new StringBuilder("Select {pk} from {PointRequisition} where {requestRaisedFor}=?customerModel");
            map.put("customerModel", customerModel);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.setResultClassList(Collections.singletonList(CustomersInfluencerMapModel.class));
            query.addQueryParameters(map);
            final SearchResult<PointRequisitionModel> searchResult = flexibleSearchService.search(query);
            if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
                return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
            else
                return null;
        }
        return null;
    }

    @Override
    public Double getActualTargetForPremiumSalesLeaderYTD(SclCustomerModel so, BaseSiteModel currentBaseSite, Date startDate, Date endDate) {

        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();


        LOGGER.info(String.format("SclCustomer Model PK:%s", so));


        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}}  WHERE {o:user} = ?sclCustomer and {o:versionID} IS NULL and {o:site} =?site and {oe:status}=?orderStatus and {oe:deliveredDate} >= ?startDate and {oe:deliveredDate} <= ?endDate");

        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("orderStatus", orderStatus);
        params.put("sclCustomer", so);
        params.put("site", currentBaseSite);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("premium", "Y");
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
    public Double getActualTargetForPremiumSalesLeaderYTDRetailer(SclCustomerModel sclUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();

        builder.append("SELECT sum({oe:quantity}) from " +
                "{OrderRequisition AS oe JOIN " +
                "Product as p on {oe:product}={p:pk}} WHERE " +
                "{oe:status}=?requisitionStatus and {p:premium}=?premium" +
                "{oe.toCustomer}=?customer and {oe.deliveredDate} >= ?startDate AND {oe.deliveredDate} <= ?endDate");
        RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
        ;
        params.put("requisitionStatus", requisitionStatus);
        params.put("customer", sclUser);
        params.put("site", currentBaseSite);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("premium", "Y");
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
    public Double getTotalCDAvailedForDealer(SclCustomerModel so, Date startDate, Date endDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT SUM({availedDiscount}) FROM {CashDiscountAvailed} WHERE {customerNo} = ?customerNo");
        if (startDate != null && endDate != null) {
            builder.append(" AND {discountAvailedDate} BETWEEN ?startDate AND ?endDate");
            params.put("startDate", startDate);
            params.put("endDate", endDate);

        }
        params.put("customerNo", so);
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
    public Double getTotalCDLostForDealer(SclCustomerModel so, Date startDate, Date endDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT SUM({lostDiscount}) FROM {CashDiscountLost} WHERE {customerNo} = ?customerNo");
        if (startDate != null && endDate != null) {
            builder.append(" AND {discountLostDate} BETWEEN ?startDate AND ?endDate");
            params.put("startDate", startDate);
            params.put("endDate", endDate);

        }
        params.put("customerNo", so);
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
    public double getActualTargetFor10DayBucketForSP(List<SclCustomerModel> sclCustomer, Date startDate1, Date endDate1) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk}}  WHERE {o:user} in (?sclCustomer) and {oe.cancelledDate} is null and {oe:invoiceCreationDateAndTime} >= ?startDate and {oe:invoiceCreationDateAndTime} < ?endDate");

        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("orderStatus", orderStatus);
        params.put("startDate", startDate1);
        params.put("endDate", endDate1);
        params.put("sclCustomer", sclCustomer);
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
    public Double getActualTargetSalesForSelectedMonthAndYearForMTDSP(List<SclCustomerModel> sclCustomer, BaseSiteModel currentBaseSite, int month, int year) {
        final Map<String, Object> params = new HashMap<String, Object>();
        //userService.setCurrentUser(userService.getAdminUser());
        final StringBuilder builder = new StringBuilder();

        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}}  WHERE {o:user} in (?sclCustomer)  and   "
                + " {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and ").append(SclDateUtility.getDateClauseQueryByMonthYear("oe:invoiceCreationDateAndTime", month, year, params));


        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("orderStatus", orderStatus);
        params.put("sclCustomer", sclCustomer);
        params.put("site", currentBaseSite);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);

        // final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        //searchRestrictionService.disableSearchRestrictions();
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        //searchRestrictionService.enableSearchRestrictions();
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    @Override
    public Double getMonthlySalesTargetForMtdSp(List<SclCustomerModel> sclUser, BaseSiteModel currentBaseSite, String monthName, String yearName) {
        final Map<String, Object> params = new HashMap<String, Object>();
        List<String> customerCodeList = new ArrayList<>();
        for (SclCustomerModel customerModel : sclUser) {
            customerCodeList.add(customerModel.getUid());
        }
        final StringBuilder builder = new StringBuilder("select {m:revisedTarget} from {DealerRevisedMonthlySales as m} where {m:customerCode} in (?sclCustomer) and {m:monthName}=?month and {m:monthYear}=?year");
        params.put("sclCustomer", customerCodeList);
        params.put("month", monthName);
        params.put("year", yearName);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(Double.class));

        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    @Override
    public double getActualSaleForDealerGraphYTDSP(List<SclCustomerModel> sclCustomer, Date startDate, Date endDate, BaseSiteModel currentBaseSite) {
        final Map<String, Object> params = new HashMap<String, Object>();

        final StringBuilder builder = new StringBuilder();

        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}}  WHERE {o:user} in (?sclCustomer) and {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and {oe:invoiceCreationDateAndTime} >= ?startDate and {oe:invoiceCreationDateAndTime} < ?endDate");

        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("sclCustomer", sclCustomer);
        params.put("site", currentBaseSite);
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

    @Override
    public List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForCustomerMonthPicker(SclCustomerModel sclCustomerModel, BaseSiteModel baseSite, int month, int year, List<String> subAreaList, List<String> districtList) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder();
            B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
            List<DistrictMasterModel> list = new ArrayList<>();
            List<SubAreaMasterModel> list1 = new ArrayList<>();
            if (districtList != null && !districtList.isEmpty()) {
                for (String code : districtList) {
                    list.add(districtMasterDao.findByCode(code));
                }
                params.put("doList", list);
                builder.append(" and {oe.districtMaster} in (?doList) ");
            }
            if (subAreaList != null && !subAreaList.isEmpty()) {
                for (String id : subAreaList) {
                    list1.add(territoryManagementDao.getTerritoryById(id));
                }
                params.put("subAreaList", list1);
                builder.append(" and {oe.subAreaMaster} in (?subAreaList) ");
            }

            if ((sclCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SP_GROUP_ID)))) {
                //|| (sclUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {

                builder.append("SELECT {p:code},{p:name},sum({oe:quantityInMT}) from {OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN Product as p on {oe:product}={p:pk}} where {oe.cancelledDate} is null AND ").append(SclDateUtility.getDateClauseQueryByMonthYear("oe:invoiceCreationDateAndTime", month, year, params)).append("group by {p:code},{p:name}");
                OrderStatus orderStatus = OrderStatus.DELIVERED;
                params.put("sclCustomer", sclCustomerModel);
                params.put("baseSite", baseSite);
                params.put("orderStatus", orderStatus);
                final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
                query.addQueryParameters(params);
                query.setResultClassList(Arrays.asList(String.class, String.class, Double.class));
                final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
                List<List<Object>> result = searchResult.getResult();
                return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
            }
            if ((sclCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
               /*builder.append("SELECT {p:code},{p:name},sum({oe:quantity}) from " +
                        "{OrderRequisition AS oe JOIN " +
                        "Product as p on {oe:product}={p:pk}} WHERE " +
                        "{oe:status}=?requisitionStatus and " +
                        "{oe.toCustomer}=?customer and " +
                        SclDateUtility.getDateClauseQueryByMonthYear("oe:deliveredDate", month, year, params));*/
                final StringBuilder builder1 = new StringBuilder();
                final StringBuilder builder2 = new StringBuilder();
                if(!(userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
                    builder1.append("SELECT {p:code},{or:aliasCode},sum({or:quantity}),{or.equivalenceProductCode} from {OrderRequisition AS or JOIN Product as p on {or:product}={p:pk}} " +
                            " where {or:status}=?requisitionStatus and {or.toCustomer} = ?sclRetailer and {or:product} is not null and {or:requisitionType}=?requisitionType and " +
                            SclDateUtility.getDateClauseQueryByMonthYear("or:liftingDate", month, year, params));
                    if ((userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
                        builder1.append(" and {or:fromCustomer}=?sclDealer ");
                        params.put("sclDealer", currentUser);
                    }
                    builder1.append(" group by {p:code},{or:aliasCode},{or.equivalenceProductCode}");

                    builder2.append("SELECT {p:code},{m:aliasCode},sum({m:quantityInMT}),{p.equivalenceProductCode} from {MasterStockAllocation AS m join Product as p on {p:pk}={m:product}} " +
                            " where {m.retailer} =?sclRetailer and {m:aliasCode} is not null and {m:isInvoiceCancelled}=?cancellationFlag and  " +
                            SclDateUtility.getDateClauseQueryByMonthYear("m:invoicedDate", month, year, params));
                    if ((userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
                        builder2.append(" and {m:dealer}=?sclDealer ");
                        params.put("sclDealer", currentUser);
                    }
                    builder2.append(" group by {p:code},{m:aliasCode},{p.equivalenceProductCode} ");
                }
                else{
                    builder1.append("SELECT {p:code},{or:aliasCode},sum({or:quantity}),{or.equivalenceProductCode} from {OrderRequisition AS or JOIN Product as p on {or:product}={p:pk}} " +
                            " where {or:status}=?requisitionStatus and {or.toCustomer} = ?sclRetailer and {or:product} is not null and {or:requisitionType}=?requisitionType and " +
                            SclDateUtility.getDateClauseQueryByMonthYear("or:liftingDate", month, year, params));
                    if ((userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
                        builder1.append(" and {or:fromCustomer}=?sclDealer ");
                        params.put("sclDealer", currentUser);
                    }
                    builder1.append(" group by {p:code},{or:aliasCode},{or.equivalenceProductCode}");

                    builder2.append("SELECT {p:code},{m:aliasCode},sum({m:quantityInBags}),{p.equivalenceProductCode}  from {MasterStockAllocation AS m join Product as p on {p:pk}={m:product}} " +
                            " where {m.retailer} =?sclRetailer and {m:aliasCode} is not null and {m:isInvoiceCancelled}=?cancellationFlag and  " +
                            SclDateUtility.getDateClauseQueryByMonthYear("m:invoicedDate", month, year, params));
                    if ((currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
                        builder2.append(" and {m:dealer}=?sclDealer ");
                        params.put("sclDealer", currentUser);
                    }
                    builder2.append(" group by {p:code},{m:aliasCode},{p.equivalenceProductCode}");
                }
                /*RequisitionStatus requisitionStatus = RequisitionStatus.SERVICED_BY_DEALER;
                params.put("customer", sclUser);
                params.put("requisitionStatus", requisitionStatus);*/
                params.put("sclRetailer", sclCustomerModel);
                // params.put("product", product);
                params.put("requisitionStatus", RequisitionStatus.SERVICED_BY_DEALER);
                params.put("requisitionType", RequisitionType.LIFTING);
                params.put("cancellationFlag", Boolean.FALSE);


                final FlexibleSearchQuery query1 = new FlexibleSearchQuery(builder1.toString());
                query1.addQueryParameters(params);
                LOGGER.info(String.format("Query for Product Mix1:%s",query1));
                query1.setResultClassList(Arrays.asList(String.class, String.class, Double.class,String.class));
                final SearchResult<List<Object>> searchResult1 = flexibleSearchService.search(query1);
                List<List<Object>> result1 = searchResult1.getResult();
                //return (result != null && !result.isEmpty()) ? result : Collections.emptyList();

                final FlexibleSearchQuery query2 = new FlexibleSearchQuery(builder2.toString());
                query2.addQueryParameters(params);
                LOGGER.info(String.format("Query for Product Mix2:%s",query2));
                query2.setResultClassList(Arrays.asList(String.class, String.class, Double.class,String.class));
                final SearchResult<List<Object>> searchResult2 = flexibleSearchService.search(query2);
                List<List<Object>> result2 = searchResult2.getResult();
                //return (result != null && !result.isEmpty()) ? result : Collections.emptyList();

                if(CollectionUtils.isNotEmpty(result1) && CollectionUtils.isNotEmpty(result2)) {
                    Map<String, List<Object>> resultMap1 = mapByCode(result1);
                    Map<String, List<Object>> resultMap2 = mapByCode(result2);

                    for (String code : resultMap1.keySet()) {
                        if (resultMap2.containsKey(code)) {
                            List<Object> lista = resultMap1.get(code);
                            List<Object> listb = resultMap2.get(code);

                            double qty1 = (double) lista.get(2);
                            double qty2 = (double) listb.get(2);
                            double totalQty = qty1 + qty2;

                            lista.set(2, totalQty);
                            listb.set(2, totalQty);

                            LOG.info(String.format("Updated Qty for code {%s} in result1: {%s}", code, totalQty));
                            LOG.info(String.format("Updated Qty for code {%s} in result2: {%s}", code, totalQty));
                        }else{
                            LOG.info(String.format("not Updated Qty for code {%s} in result1: {%s}  ", code,(resultMap1.get(code)!=null && resultMap1.get(code).get(2)!=null) ?resultMap1.get(code).get(2):null));
                           // LOG.info(String.format("not Updated Qty for code {%s} in result2: {%s}  ", code, resultMap2.get(code).get(2)));
                        }
                    }
                }

                List<List<Object>> result3 =new ArrayList<>();
                result3.addAll(result1);
                result3.addAll(result2);
                List<List<Object>> lists = removeDuplicates(result3);
                return (lists != null && !lists.isEmpty()) ? lists.stream().distinct().collect(Collectors.toList()) : Collections.emptyList();
                //return (result3 != null && !result3.isEmpty()) ? result3.stream().distinct().collect(Collectors.toList()) : Collections.emptyList();


            }

        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
        return null;
    }

    @Override
    public double getMonthWiseForRetailerYTD(SclCustomerModel sclCustomer, Date startDate, Date endDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT sum({oe:quantity}) from {OrderRequisition AS oe} WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?sclCustomer and {oe:deliveredDate}>=?startDate and {oe:deliveredDate}< ?endDate and {oe:product} is not null");
        RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
        params.put("requisitionStatus", requisitionStatus);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("sclCustomer", sclCustomer);
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
    public Double getActualTargetForSalesYTDRetailerList(SclCustomerModel currentUser, BaseSiteModel currentBaseSite, String startDate, String endDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select sum({p.quantity}) from {OrderRequisition as p} ")
                .append(" where {p.deliveredDate}>=?startDate and {p.deliveredDate}<?endDate ")
                .append(" and {p.toCustomer}=?toCustomerList and {p:status}=?status ");
        RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
        params.put("status", requisitionStatus);
        params.put("toCustomerList", currentUser);
        params.put("site", currentBaseSite);
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

    @Override
    public Double getActualTargetForSalesRetailerMTDList(SclCustomerModel currentUser, BaseSiteModel currentBaseSite) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select sum({p.quantity}) from {OrderRequisition as p} ")
                .append(" where {p.toCustomer}=?toCustomerList and {p:status}=?status and ").append(SclDateUtility.getMtdClauseQueryRetailer("p:deliveredDate", params));

        RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
        params.put("status", requisitionStatus);
        params.put("toCustomerList", currentUser);
        params.put("site", currentBaseSite);
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
    public Double getActualTargetForSalesYTRetailerList(SclCustomerModel currentUser, BaseSiteModel currentBaseSite, String startDate, String endDate, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        if (StringUtils.isBlank(bgpFilter)) {
            builder.append("SELECT sum({oe:quantity}) from {OrderRequisition AS oe} WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?sclCustomer and {oe:deliveredDate}>=?startDate and {oe:deliveredDate}< ?endDate");
        } else if (bgpFilter.equalsIgnoreCase("ALL")) {
            builder.append("SELECT sum({oe:quantity}) from {OrderRequisition AS oe} WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?sclCustomer and {oe:deliveredDate}>=?startDate and {oe:deliveredDate}< ?endDate");
        } else {
            CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
            ProductModel product = productService.getProductForCode(catalogVersion, bgpFilter);
            if (product != null) {
                builder.append("SELECT sum({oe:quantity}) from {OrderRequisition AS oe JOIN Product as p on {oe:product}={p:pk}} WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?sclCustomer and {oe:deliveredDate}>=?startDate and {oe:deliveredDate}<=?endDate and {oe:product} =?product and {oe:product} is not null");
                params.put("product", product);
            }
        }
        RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
        params.put("requisitionStatus", requisitionStatus);
        params.put("sclCustomer", currentUser);
        params.put("site", currentBaseSite);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);

        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        /*searchRestrictionService.disableSearchRestrictions();
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        searchRestrictionService.enableSearchRestrictions();*/
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    @Override
    public List<List<Object>> getActualTargetForSalesLeaderRetailer(SubAreaMasterModel district, BaseSiteModel currentBaseSite, Date startDate, Date endDate) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder();

            builder.append("SELECT {sc.uid},SUM({oe.quantity}) FROM {SclCustomer AS sc JOIN CustomerSubAreaMapping AS cs ON {sc:pk} = {cs:sclCustomer} JOIN SubAreaMaster AS sm ON {sm.pk} = {cs.subAreaMaster} LEFT JOIN OrderRequisition AS oe ON {cs.sclCustomer} = {oe.toCustomer} AND {oe.status} = ?requisitionStatus AND {oe.deliveredDate} >=?startDate AND {oe.deliveredDate} <=?endDate } WHERE {cs.brand}=?site AND {cs.isOtherBrand}=?isOtherBrand and {sc.counterType}=?counterType AND {cs.subAreaMaster} =?district GROUP BY {sc.uid} ORDER BY SUM({oe.quantity})");

            RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
            params.put("requisitionStatus", requisitionStatus);
            //params.put("sclCustomer", sclUser);
            params.put("district", district);
            params.put("site", currentBaseSite);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            params.put("counterType", CounterType.RETAILER);
            params.put(CustomerSubAreaMappingModel.ISOTHERBRAND, Boolean.FALSE);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class, Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return result != null && !result.isEmpty() ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));

        }
    }

    @Override
    public List<List<Object>> getPremiumForSalesLeaderRetailer(SubAreaMasterModel district, BaseSiteModel currentBaseSite, Date startDate, Date endDate) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder();

            //builder.append("SELECT {sc.uid},SUM({oe.quantity}) FROM {SclCustomer AS sc JOIN CustomerSubAreaMapping AS cs ON {sc:pk} = {cs:sclCustomer} JOIN SubAreaMaster AS sm ON {sm.pk} = {cs.subAreaMaster} LEFT JOIN OrderRequisition AS oe ON {cs.sclCustomer} = {oe.toCustomer} AND {oe.status} = ?requisitionStatus AND {oe.deliveredDate} >=?startDate AND {oe.deliveredDate} <=?endDate } WHERE {cs.brand}=?site AND {cs.isOtherBrand}=?isOtherBrand and {sc.counterType}=?counterType AND {cs.subAreaMaster} =?district GROUP BY {sc.uid} ORDER BY SUM({oe.quantity})");
            builder.append("SELECT {sc.uid}, SUM(CASE WHEN {p.premium} =?premium THEN {oe.quantity} ELSE 0 END) AS totalQuantity FROM {SclCustomer AS sc JOIN CustomerSubAreaMapping AS cs ON {sc:pk} = {cs:sclCustomer} JOIN SubAreaMaster AS sm ON {sm.pk} = {cs.subAreaMaster}  LEFT JOIN OrderRequisition AS oe ON {cs.sclCustomer} = {oe.toCustomer}  AND {oe.status} =?requisitionStatus AND {oe.deliveredDate} >= ?startDate AND {oe.deliveredDate} <= ?endDate LEFT JOIN Product AS p ON {oe:product} = {p:pk}} WHERE {cs.brand}=?site AND {cs.isOtherBrand}=?isOtherBrand AND {sc.counterType}=?counterType AND {cs.subAreaMaster} =?district GROUP BY {sc.uid} ORDER BY totalQuantity");

            RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
            params.put("requisitionStatus", requisitionStatus);
            //params.put("sclCustomer", sclUser);
            params.put("district", district);
            params.put("site", currentBaseSite);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            params.put("counterType", CounterType.RETAILER);
            params.put("premium", "Y");
            params.put(CustomerSubAreaMappingModel.ISOTHERBRAND, Boolean.FALSE);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class, Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return result != null && !result.isEmpty() ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));

        }
    }

    @Override
    public DistrictMasterModel getDistrictForSP(FilterDistrictData filterDistrictData) {

        final Map<String, Object> params = new HashMap<String, Object>();
        SclCustomerModel currentUser = (SclCustomerModel) userService.getCurrentUser();
        final StringBuilder builder = new StringBuilder();
        if (currentUser instanceof SclCustomerModel) {
            builder.append(" SELECT distinct{dm.pk} FROM {CustDepotDealerMapping as d join SpCustDepotMapping as s on {d.custDepotCode}={s.custDepotCode}join CustomerSubAreaMapping as c on {c.sclCustomer}={d.dealerCode} join SclCustomer as u on {c.sclCustomer}={u.pk} join SubAreaMaster as sm on {sm.pk}={c.subAreaMaster} JOIN DistrictMaster AS dm ON {dm.pk} = {sm.districtMaster}} where  {s.brand} = ?brand AND {s.active} = ?active");
            params.put("active", Boolean.TRUE);
            params.put("brand", baseSiteService.getCurrentBaseSite());

            if (filterDistrictData != null && !ObjectUtils.isEmpty(filterDistrictData) && filterDistrictData.getDistrictCode() != null && !filterDistrictData.getDistrictCode().isEmpty()) {
                builder.append(" and {dm:pk} like ?code");
                params.put("code", filterDistrictData.getDistrictCode() + "%");
            } else {
                builder.append(" and {s.spCode}= ?currentUser ");
                params.put("currentUser", currentUser);

            }
            builder.append(" order by {dm:pk}");

        }

        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(CounterVisitMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<DistrictMasterModel> searchResult = flexibleSearchService.search(query);
        List<DistrictMasterModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result.get(0) : null;
    }

    @Override
    public List<SclCustomerModel> getSPForDistrict(DistrictMasterModel districtForSP) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT distinct{s.spCode} FROM {CustDepotDealerMapping as d join SpCustDepotMapping as s on {d.custDepotCode}={s.custDepotCode}join CustomerSubAreaMapping as c on {c.sclCustomer}={d.dealerCode} join SclCustomer as u on {c.sclCustomer}={u.pk} join SubAreaMaster as sm on {sm.pk}={c.subAreaMaster} JOIN DistrictMaster AS dm ON {dm.pk} = {sm.districtMaster}} where {dm.pk}=?districtForSP and  {s.brand}=?brand ");
        params.put("active", Boolean.TRUE);
        params.put("brand", baseSiteService.getCurrentBaseSite());
        params.put("districtForSP", districtForSP);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(SclCustomerModel.class));
        query.addQueryParameters(params);
        final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
        List<SclCustomerModel> result = searchResult.getResult();

        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public List<SclUserModel> getTsmByRegion(String district) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("Select distinct {t.tsmUser} from {TsmDistrictMapping as t join DistrictMaster as d on {t.district}={d.pk} join RegionMaster as r on {d.region}={r.pk}} where  {r.pk} in (?district) and {t.brand}=?brand and {t.isActive}=?active");
        params.put("active", Boolean.TRUE);
        params.put("brand", baseSiteService.getCurrentBaseSite());
        params.put("district", district);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(SclUserModel.class));
        query.addQueryParameters(params);
        final SearchResult<SclUserModel> searchResult = flexibleSearchService.search(query);
        List<SclUserModel> result = searchResult.getResult();

        return result != null && !result.isEmpty() ? result : Collections.emptyList();

    }

    @Override
    public List<SclUserModel> getRHByState(String district) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("Select {rh.rhUser} from {RhRegionMapping as rh join RegionMaster as r on {rh.region}={r.pk} join StateMaster as s on {r.state}={s.pk}} where {s.code}=?state and {rh:brand} =?brand  and {rh:isActive} =?active");
        params.put("active", Boolean.TRUE);
        params.put("brand", baseSiteService.getCurrentBaseSite());
        params.put("state", district);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(SclUserModel.class));
        query.addQueryParameters(params);
        final SearchResult<SclUserModel> searchResult = flexibleSearchService.search(query);
        List<SclUserModel> result = searchResult.getResult();

        return result != null && !result.isEmpty() ? result : Collections.emptyList();

    }


    @Override
    public List<SclCustomerModel> getCustomerForTsm(String district, SclUserModel sclUserModel) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("Select distinct {c.sclCustomer} from {TsmDistrictMapping as t join DistrictMaster as d on {t.district}={d.pk} join RegionMaster as r on {d.region}={r.pk} join SubAreaMaster as s on {d:name}={s:district} join UserSubAreaMapping as u on {u.subareaMaster}={s.pk} join CustomerSubAreaMapping as c on {c:subAreaMaster}={u:subAreaMaster} join SclCustomer as sc on {c.sclCustomer} = {sc.pk} } where {t.brand}=?brand and {t.isActive}=?active and {sc.counterType}=?counterType and {t.tsmUser}=?sclUserModel");
        params.put("active", Boolean.TRUE);
        params.put("brand", baseSiteService.getCurrentBaseSite());
        params.put("sclUserModel", sclUserModel);
        params.put("counterType", CounterType.DEALER);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(SclUserModel.class));
        query.addQueryParameters(params);
        final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
        List<SclCustomerModel> result = searchResult.getResult();

        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public List<SclCustomerModel> getCustomerForRH(String district, SclUserModel sclUserModel) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("Select distinct {c.sclCustomer} from {RhRegionMapping as rh join RegionMaster as r on {rh.region}={r.pk} join DistrictMaster as d on {d:region}={r:pk} join RegionMaster as r on {d.region}={r.pk} join SubAreaMaster as s on {d:name}={s:district} join UserSubAreaMapping as u on {u.subareaMaster}={s.pk} join CustomerSubAreaMapping as c on {c:subAreaMaster}={u:subAreaMaster} } where  {rh:brand} =?brand and {rh:isActive} =?active and {rh.rhUser}=?sclUserModel");
        params.put("active", Boolean.TRUE);
        params.put("brand", baseSiteService.getCurrentBaseSite());
        params.put("sclUserModel", sclUserModel);
        // params.put("counterType", CounterType.DEALER);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(SclUserModel.class));
        query.addQueryParameters(params);
        final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
        List<SclCustomerModel> result = searchResult.getResult();

        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public List<List<Object>> getSalesDealerByDate(String district, SclUserModel sclUserModel, Date startDate, Date endDate) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder();

            LOGGER.info("Getting sales for the Dealer");

            builder.append("Select {tu.uid},sum({oe.quantityInMt}) from {TsmDistrictMapping as t join DistrictMaster as d on {t.district}={d.pk} join RegionMaster as r on {d.region}={r.pk} join SubAreaMaster as s on {d:name}={s:district} join UserSubAreaMapping as u on {u.subareaMaster}={s.pk} join CustomerSubAreaMapping as c on {c:subAreaMaster}={u:subAreaMaster} join SclCustomer as sc on {c.sclCustomer} = {sc.pk} join SclUser as tu on {t.tsmUser}={tu.pk} LEFT JOIN Order AS o ON {c.sclCustomer} = {o.user} LEFT JOIN OrderEntry AS oe ON {oe.order} = {o.pk} AND {oe.status} =?status AND {oe.deliveredDate} >=?startDate AND {oe.deliveredDate} <= ?endDate} where  {r.pk} in (?district) and {t.brand}=?brand and {t.isActive}=?active and {sc.counterType}=?counterType and {t.tsmUser}=?tsmUser GROUP BY {tu.uid} ORDER BY sum({oe.quantityInMt})");

            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("status", orderStatus);
            params.put("tsmUser", sclUserModel);
            params.put("district", district);
            params.put("brand", baseSiteService.getCurrentBaseSite());
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            params.put("counterType", CounterType.DEALER);
            params.put("active", Boolean.TRUE);
            params.put(CustomerSubAreaMappingModel.ISOTHERBRAND, Boolean.FALSE);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class, Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return result != null && !result.isEmpty() ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));

        }
    }

    @Override
    public List<List<Object>> getRHSalesDealerByDate(SclUserModel sclUserModel, Date startDate, Date endDate) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder();

            LOGGER.info("Getting sales for the Dealer");

            // builder.append("Select {tu.uid},sum({oe.quantityInMt}) from {TsmDistrictMapping as t join DistrictMaster as d on {t.district}={d.pk} join RegionMaster as r on {d.region}={r.pk} join SubAreaMaster as s on {d:name}={s:district} join UserSubAreaMapping as u on {u.subareaMaster}={s.pk} join CustomerSubAreaMapping as c on {c:subAreaMaster}={u:subAreaMaster} join SclCustomer as sc on {c.sclCustomer} = {sc.pk} join SclUser as tu on {t.tsmUser}={tu.pk} LEFT JOIN Order AS o ON {c.sclCustomer} = {o.user} LEFT JOIN OrderEntry AS oe ON {oe.order} = {o.pk} AND {oe.status} =?status AND {oe.deliveredDate} >=?startDate AND {oe.deliveredDate} <= ?endDate} where  {r.pk} in (?district) and {t.brand}=?brand and {t.isActive}=?active and {sc.counterType}=?counterType and {t.tsmUser}=?tsmUser GROUP BY {tu.uid} ORDER BY sum({oe.quantityInMt})");
            builder.append("Select {tu.uid},sum({oe.quantityInMt}) from {RhRegionMapping as rh join RegionMaster as r on {rh.region}={r.pk} join DistrictMaster as d on {d:region}={r:pk} join RegionMaster as r on {d.region}={r.pk} join SubAreaMaster as s on {d:name}={s:district} join UserSubAreaMapping as u on {u.subareaMaster}={s.pk} join CustomerSubAreaMapping as c on {c:subAreaMaster}={u:subAreaMaster} join SclCustomer as sc on {c.sclCustomer} = {sc.pk} join SclUser as tu on {rh.rhUser}={tu.pk} LEFT JOIN Order AS o ON {c.sclCustomer} = {o.user} LEFT JOIN OrderEntry AS oe ON {oe.order} = {o.pk} AND {oe.status} =?status AND {oe.deliveredDate} >=?startDate AND {oe.deliveredDate} <= ?endDate} where  {rh:brand} =?brand and {rh:isActive} =?active and {rh.rhUser}=?rhUser GROUP BY {tu.uid} ORDER BY sum({oe.quantityInMt})");
            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("status", orderStatus);
            params.put("rhUser", sclUserModel);

            params.put("brand", baseSiteService.getCurrentBaseSite());
            params.put("startDate", startDate);
            params.put("endDate", endDate);

            params.put("active", Boolean.TRUE);
            params.put(CustomerSubAreaMappingModel.ISOTHERBRAND, Boolean.FALSE);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class, Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return result != null && !result.isEmpty() ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));

        }
    }

    @Override
    public List<SclCustomerModel> getCustomerForSp(SclCustomerModel sclCustomer) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT distinct{d.dealerCode} FROM {CustDepotDealerMapping as d join SpCustDepotMapping as s on {d.custDepotCode}={s.custDepotCode}} where {s.spCode}=?sclCustomer");
        params.put("active", Boolean.TRUE);
        params.put("brand", baseSiteService.getCurrentBaseSite());
        params.put("sclCustomer", sclCustomer);
        params.put("counterType", CounterType.DEALER);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(SclUserModel.class));
        query.addQueryParameters(params);
        final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
        List<SclCustomerModel> result = searchResult.getResult();

        return result != null && !result.isEmpty() ? result : Collections.emptyList();

    }

    @Override
    public Double getActualTargetForSalesSPMTDSearch(List<SclCustomerModel> sclCustomer) {
        {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder();

            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}}  WHERE {o:user} in (?sclCustomer)  and   "
                    + " {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and ").append(SclDateUtility.getMtdClauseQuery("oe:invoiceCreationDateAndTime", params));

            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("sclCustomer", sclCustomer);
            params.put("orderStatus", orderStatus);
            params.put("site", baseSiteService.getCurrentBaseSite());
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.setResultClassList(Arrays.asList(Double.class));
            query.addQueryParameters(params);
            final SearchResult<Double> searchResult = flexibleSearchService.search(query);
            if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty())) {
                return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
            } else {
                return 0.0;
            }
        }
    }

    @Override
    public List<List<Object>> getWeeklyOverallPerformance(String bgpFilter) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder();

            if (bgpFilter != null) {
                CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
                ProductModel product = productService.getProductForCode(catalogVersion, bgpFilter);
                builder.append("select top 9 DATEPART(week, {p:deliveryDate}),DATEPART(year, {p:deliveryDate}) , sum({p.quantity}) from {PointRequisition as p} where {p:status} = ?status and {p:product} = ?product group by DATEPART(week, {p:deliveryDate}),DATEPART(year, {p:deliveryDate}) order by DATEPART(year, {p:deliveryDate}) desc, DATEPART(week, {p:deliveryDate}) desc");
                params.put("product", product);
            } else {
                builder.append("select top 9 DATEPART(week, {p:deliveryDate}),DATEPART(year, {p:deliveryDate}) , sum({p.quantity}) from {PointRequisition as p} where {p:status} = ?status group by DATEPART(week, {p:deliveryDate}),DATEPART(year, {p:deliveryDate}) order by DATEPART(year, {p:deliveryDate}) desc, DATEPART(week, {p:deliveryDate}) desc");
            }
            PointRequisitionStatus status = PointRequisitionStatus.APPROVED;
            params.put("status", status);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Integer.class, Integer.class, Double.class));
            LOG.info("Query for weekly::" + query);
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return result != null && !result.isEmpty() ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> getMonthlyOverallPerformance(String bgpFilter) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder();

            if (bgpFilter != null) {
                CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
                ProductModel product = productService.getProductForCode(catalogVersion, bgpFilter);
                builder.append("select top 6 DATEPART(month, {p:deliveryDate}),DATEPART(year, {p:deliveryDate}) , sum({p.quantity}) from {PointRequisition as p} where {p:status} = ?status and {p:product}= ?product group by DATEPART(month, {p:deliveryDate}),DATEPART(year, {p:deliveryDate}) order by DATEPART(year, {p:deliveryDate}) desc, DATEPART(month, {p:deliveryDate}) desc");
                params.put("product", product);
            } else {
                builder.append("select top 6 DATEPART(month, {p:deliveryDate}),DATEPART(year, {p:deliveryDate}) , sum({p.quantity}) from {PointRequisition as p} where {p:status} = ?status group by DATEPART(month, {p:deliveryDate}),DATEPART(year, {p:deliveryDate}) order by DATEPART(year, {p:deliveryDate}) desc, DATEPART(month, {p:deliveryDate}) desc");
            }
            PointRequisitionStatus status = PointRequisitionStatus.APPROVED;
            params.put("status", status);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Integer.class, Integer.class, Double.class));
            LOG.info("Query for monthly::" + query);
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return result != null && !result.isEmpty() ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public ReceiptAllocaltionModel getDealerAllocation(ProductModel productCode, SclCustomerModel dealerCode) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {ReceiptAllocaltion} WHERE {dealerCode}=?dealerCode AND {product}=?product");

        params.put("dealerCode", dealerCode);
        params.put("product", productCode);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(ReceiptAllocaltionModel.class));
        query.addQueryParameters(params);
        final SearchResult<ReceiptAllocaltionModel> searchResult = flexibleSearchService.search(query);
        LOG.info("In DealerDao:getDealerAllocation method--> query:::" + builder.toString() + ":::Product Code:::" + productCode.getPk().toString() + ":::Dealer Code:::" + dealerCode.getPk().toString());
        LOG.info("In DealerDao:getDealerAllocation method--> Show the result of the query:::" + searchResult.getResult());
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
        else
            return null;
    }

    public TerritoryMasterService getTerritoryMasterService() {
        return territoryMasterService;
    }

    public void setTerritoryMasterService(TerritoryMasterService territoryMasterService) {
        this.territoryMasterService = territoryMasterService;
    }

    @Override
    public Map<String, Object> findMaxInvoicedDateAndQunatityDeliveryItem(final UserModel user) {
        Map<String, Object> map = new HashMap<>();
        map.put(DeliveryItemModel.INVOICECREATIONDATEANDTIME, null);
        map.put(DeliveryItemModel.INVOICEQUANTITY, 0.0);
        final Map<String, Object> attr = new HashMap<String, Object>();
        attr.put(OrderModel.USER, user);
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT  MAX({di.invoiceCreationDateAndTime}) FROM { OrderEntry AS oe JOIN Order AS o ON {o.pk}={oe.order} JOIN deliveryItem AS di ON {oe:pk}={di:entry} } where {o:user}=?user and {oe.cancelledDate} is null ");
        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Date.class));
        query.getQueryParameters().putAll(attr);
        final SearchResult<Date> result = this.getFlexibleSearchService().search(query);

        if (result.getResult() != null && !result.getResult().isEmpty()) {
            Date lastLiftingDate = result.getResult().get(0);
            LOGGER.info("LAst Lift date" + lastLiftingDate);
            if (lastLiftingDate != null) {
                map.put(DeliveryItemModel.INVOICECREATIONDATEANDTIME, lastLiftingDate);

                final StringBuilder sql2 = new StringBuilder();
                //attr.put(DeliveryItemModel.INVOICECREATIONDATEANDTIME, lastLiftingDate);

                String pattern = "yyyy-MM-dd HH:mm:ss";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                String date = simpleDateFormat.format(lastLiftingDate);
                LOGGER.info("LAst Lift date after format:" + date);
                sql2.append("SELECT {di.invoiceQuantity} FROM { OrderEntry AS oe JOIN Order AS o ON {o.pk}={oe.order} JOIN deliveryItem AS di ON {oe:pk}={di:entry} } where {o:user}=?user and  {di:invoiceCreationDateAndTime} like ?date and {di.cancelledDate} is null ");
                attr.put("date", "%" + date + "%");
                final FlexibleSearchQuery query1 = new FlexibleSearchQuery(sql2.toString());
                query1.setResultClassList(Arrays.asList(Double.class));
                query1.getQueryParameters().putAll(attr);
                LOGGER.info("LAst Lift qty query:" + query1);
                final SearchResult<Double> quantityRes = this.getFlexibleSearchService().search(query1);
                if (quantityRes.getResult() != null && !quantityRes.getResult().isEmpty()) {
                    Double quantity = quantityRes.getResult().get(0);
                    LOGGER.info("LAst Lift qty" + quantity);
                    if (quantity != null) {
                        map.put(DeliveryItemModel.INVOICEQUANTITY, quantity);
                    }
                }
            }
        }
        LOGGER.info(map.get(DeliveryItemModel.INVOICEQUANTITY) + " " + map.get(DeliveryItemModel.INVOICECREATIONDATEANDTIME));
        return map;
    }

    @Override
    public Map<String, Object> getPotentialForCustomer(SclCustomerModel sclCustomer, String firstDayOfMonth, String lastDayOfMonth) {
        Map<String, Object> map = new HashMap<>();
        map.put(SclCustomerModel.UID, sclCustomer.getUid());
        map.put(CounterVisitMasterModel.COUNTERPOTENTIAL, 0.0);
        map.put(CounterVisitMasterModel.ID, null);
        map.put(CounterVisitMasterModel.ENDVISITTIME, null);
        final Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("sclCustomerCode", sclCustomer.getUid());
        attr.put("firstDayOfMonth", firstDayOfMonth);
        attr.put("lastDayOfMonth", lastDayOfMonth);
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT  MAX({endVisitTime}) from {CounterVisitMaster as c join Sclcustomer as sc on {sc:pk}={c:sclCustomer}} where {sc:uid}=?sclCustomerCode and {endVisitTime}<?lastDayOfMonth ");
        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Date.class));
        query.getQueryParameters().putAll(attr);
        LOGGER.info("Max End visit time query:"+query);
        final SearchResult<Date> result = this.getFlexibleSearchService().search(query);

        if (result.getResult() != null && !result.getResult().isEmpty()) {
            Date endVisitTime = result.getResult().get(0);
            LOGGER.info("Max of end visit time" + endVisitTime);
            if (endVisitTime != null) {
                map.put(CounterVisitMasterModel.ENDVISITTIME, endVisitTime);

                final StringBuilder sql2 = new StringBuilder();
                //attr.put(DeliveryItemModel.INVOICECREATIONDATEANDTIME, lastLiftingDate);

                String pattern = "yyyy-MM-dd HH:mm";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                String date = simpleDateFormat.format(endVisitTime);
                LOGGER.info("End Visit time after format:" + date);
                sql2.append("select {c:id},{sc:uid},{c:counterPotential} from {CounterVisitMaster as c join SclCustomer as sc on {sc:pk}={c:sclCustomer}} where {sc:uid}=?sclCustomerCode and {endVisitTime} like ?date ");
                attr.put("date", "%"+date+"%");
                final FlexibleSearchQuery query1 = new FlexibleSearchQuery(sql2.toString());
                query1.setResultClassList(Arrays.asList(String.class, String.class, Double.class));
                query1.getQueryParameters().putAll(attr);
                LOGGER.info("Query for to get potential from countervisit master:" + query1);
                final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query1);
                if (searchResult.getResult() != null && !searchResult.getResult().isEmpty()) {
                    List<List<Object>> result1 = searchResult.getResult();
                    if (!result1.isEmpty()) {
                        List<Object> objects = result1.get(0);
                        if (objects.size() >= 3) { // Ensure at least three elements are present
                            LOGGER.info("Potentail Objects from Query: {} {}" + objects.get(0) + " " + objects.get(1)+ " " + objects.get(2));
                            map.put(CounterVisitMasterModel.ID, objects.get(0));
                            map.put(SclCustomerModel.UID, objects.get(1));
                            map.put(CounterVisitMasterModel.COUNTERPOTENTIAL, objects.get(2));
                        }else{
                            LOGGER.info("Expected at least two elements in the result, found less: {}" + objects.size());
                        }
                    }else{
                        LOGGER.info("Result set is empty");
                    }
                }else{
                    LOGGER.info("Search Result is empty");
                }
            }
        }
        LOGGER.info("Map values:"+map.get((CounterVisitMasterModel.COUNTERPOTENTIAL) + " " + map.get(SclCustomerModel.UID) + " " + map.get(CounterVisitMasterModel.ID)));
        return map;
    }

    @Override
    public Map<String, Object> getSelfBrandSaleforCustomer(SclCustomerModel sclCustomer, String countervistId, BrandModel brand) {
        Map<String, Object> map = new HashMap<>();
        map.put(MarketMappingDetailsModel.RETAILSALES, null);
        map.put(MarketMappingDetailsModel.WHOLESALES, null);
        final Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("countervistId", countervistId);
        attr.put("brand", brand);
        final StringBuilder sql = new StringBuilder();
        sql.append("select sum({retailSales}),sum({wholeSales}) from {MarketMappingDetails as mmd join CounterVisitMaster as c on {mmd.counterVisit}={c.pk} join Brand as b on {b.pk}={mmd.brand} join CompetitorProduct as cp on {cp.pk}={mmd.product}} where {b:pk}=?brand and {c:id}=?countervistId ");
        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Double.class, Double.class));
        query.getQueryParameters().putAll(attr);
        LOGGER.info("Query for retail and whole sale:" + query);
        final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !searchResult.getResult().isEmpty()) {
            List<List<Object>> result = searchResult.getResult();
            if (!result.isEmpty()) {
                List<Object> objects = result.get(0);
                if (objects.size() >= 2) { // Ensure at least two elements are present
                    LOGGER.info("self brand sale Objects from Query: {} {}" + objects.get(0) + " " + objects.get(1));
                    map.put(MarketMappingDetailsModel.RETAILSALES, objects.get(0));
                    map.put(MarketMappingDetailsModel.WHOLESALES, objects.get(1));
                } else {
                    LOGGER.warn("Expected at least two elements in the result, found less: {}" + objects.size());
                }
            }else {
                LOGGER.warn("result set is empty");
            }
            LOGGER.info("If part:"+map.get((MarketMappingDetailsModel.RETAILSALES) + " " + map.get(SclCustomerModel.UID) + " " + map.get(MarketMappingDetailsModel.WHOLESALES)));
            return map;
            } else {
                LOGGER.info("Else part:" + map.get((MarketMappingDetailsModel.RETAILSALES) + " " + map.get(SclCustomerModel.UID) + " " + map.get(MarketMappingDetailsModel.WHOLESALES)));
                return null;
            }
        }

    @Override
    public B2BUnitModel getB2BUnitPk(String unitUid)
        {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {B2BUnit} WHERE {uid}=?unitUid ");

            params.put("unitUid", unitUid);

            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.setResultClassList(Collections.singletonList(B2BUnitModel.class));
            query.addQueryParameters(params);
            final SearchResult<B2BUnitModel> searchResult = flexibleSearchService.search(query);
            LOG.info("B2BUnit PK-> query:::" + query);
            LOG.info("In B2BUnit --> Show the result of the query:::" + searchResult.getResult());
            if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
                return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
            else
                return null;
        }

    @Override
    public List<SclCustomerModel> getCurrentNetworkCustomersForDJP(String counterType, List<TerritoryMasterModel> territoryMasterModels, boolean sclExclusiveCustomer, String subAreaMasterId, String routeId,String searchKeyFilter) {
        String CUSTOMER_FOR_USER_QUERY = "CustomerSubAreaMapping as m join SclCustomer as s on {m.sclCustomer}={s.pk} ";
        final Map<String, Object> params = new HashMap<String, Object>();
        FilterTalukaData filterTalukaData=new FilterTalukaData();
        params.put("subAreaMaster",territoryManagementService.getTaulkaForUser(filterTalukaData));

        ZoneId zone = ZoneId.systemDefault();
        LocalDate firstDayOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDayOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);
        ZonedDateTime firstDay = firstDayOfMonth.atStartOfDay(zone);
        ZonedDateTime lastDay = lastDayOfMonth.atStartOfDay(zone);
        Date date1 = Date.from(firstDay.toInstant());
        Date date2 = Date.from(lastDay.toInstant());
        params.put("firstDay", date1);
        params.put("lastDay", date2);
        SubAreaMasterModel subArea = territoryManagementService.getTerritoryById(subAreaMasterId);
        final StringBuilder builder = new StringBuilder();
        params.put("territoryMasterModels", territoryMasterModels);

        if(StringUtils.isNotBlank(counterType)) {
            if (counterType.equalsIgnoreCase("DEALER")) {
                builder.append("SELECT {s:pk} FROM {SclCustomer as s ");
                    if(subArea!=null) {
                        builder.append("  join CounterRouteMapping as cr on {s.uid}={cr.counterCode} } ");
                    }else{
                        builder.append(" } ");
                    }
                builder.append(" where {s:counterType}=?counterType and {s:territoryCode} in (?territoryMasterModels) ");
                params.put("counterType", CounterType.DEALER);
            } else if (counterType.equalsIgnoreCase("RETAILER")) {
                builder.append("SELECT distinct {dr.retailer} FROM {DealerRetailerMapping AS dr join SclCustomer as d on {d.pk}={dr.dealer} join SclCustomer as s on {s:pk}={dr:retailer} ");
                if(subArea!=null) {
                    builder.append("  join CounterRouteMapping as cr on {s.uid}={cr.counterCode} } ");
                }else{
                    builder.append(" } ");
                }
                builder.append(" WHERE {dr:retailer} is not null and {d:territoryCode} in (?territoryMasterModels) ");
            } else if (counterType.equalsIgnoreCase("INFLUENCER")) {
                builder.append("SELECT {s.pk} FROM {").append(CUSTOMER_FOR_USER_QUERY).append("} WHERE {m.subAreaMaster} in (?subAreaMaster) ");
                params.put("counterType", CounterType.INFLUENCER);
            } else if (counterType.equalsIgnoreCase("SITE")) {
                builder.append("SELECT {s.pk} FROM {").append(CUSTOMER_FOR_USER_QUERY).append("} WHERE {m.subAreaMaster} in (?subAreaMaster) ");
                params.put("counterType", CounterType.SITE);
            }
        }else if(StringUtils.isBlank(counterType) && !sclExclusiveCustomer){
            return getAllNonSclCustomers(territoryManagementService.getTaulkaForUser(filterTalukaData),subAreaMasterId,routeId,null);
        }

        if(sclExclusiveCustomer){
            builder.append(" and {s:defaultB2BUnit}=?defaultB2BUnit ");
            B2BUnitModel sclShreeUnit = getB2BUnitPk("SclShreeUnit");
            params.put("defaultB2BUnit",sclShreeUnit);
        }

        if(StringUtils.isNotBlank(searchKeyFilter)) {
            builder.append(" and (lower({s:uid}) like lower(?searchKeyFilter) OR lower({s:name}) like lower(?searchKeyFilter)) ");
            params.put("searchKeyFilter", "%"+searchKeyFilter+"%");
        }
        if(subArea!=null) {
                builder.append(" and {cr.district}=?district and {cr.taluka}=?taluka and {cr.route}=?route and {cr.brand}=?brand  ")  ;
                String district = subArea.getDistrict();
                String taluka = subArea.getTaluka();
                if(subArea.getDistrictMaster()!=null) {
                    district = subArea.getDistrictMaster().getName();
                }

                params.put("district", district);
                params.put("taluka", taluka);
                params.put("route", routeId);
                params.put("brand", baseSiteService.getCurrentBaseSite());
        }
        if(counterType.equalsIgnoreCase("RETAILER")) {
            builder.append(" order by {dr:retailer} DESC ");
        }else{
            builder.append(" order by {s.creationTime} DESC ");
        }
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(SclCustomerModel.class));
        query.addQueryParameters(params);
        LOGGER.info("Query to get Current network customer:"+query);
        final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
        LOGGER.info(String.format("REsult Set of current network customer:%s", searchResult.getResult()!=null?searchResult.getResult():null));
        List<SclCustomerModel> result = searchResult.getResult();

        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public List<SclCustomerModel> getCurrentNetworkCustomers(String counterType, String networkType, List<TerritoryMasterModel> territoryMasterModels,String searchKeyFilter, boolean sclExclusiveCustomer,boolean isZeroLift,boolean isLowPerform) {
        String CUSTOMER_FOR_USER_QUERY = "CustomerSubAreaMapping as m join SclCustomer as s on {m.sclCustomer}={s.pk} ";
        final Map<String, Object> params = new HashMap<String, Object>();
        FilterTalukaData filterTalukaData=new FilterTalukaData();
        params.put("subAreaMaster",territoryManagementService.getTaulkaForUser(filterTalukaData));

        ZoneId zone = ZoneId.systemDefault();
        LocalDate firstDayOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDayOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);
        ZonedDateTime firstDay = firstDayOfMonth.atStartOfDay(zone);
        ZonedDateTime lastDay = lastDayOfMonth.atStartOfDay(zone);
        Date date1 = Date.from(firstDay.toInstant());
        Date date2 = Date.from(lastDay.toInstant());
        params.put("firstDay", date1);
        params.put("lastDay", date2);

        final StringBuilder builder = new StringBuilder();
        params.put("territoryMasterModels", territoryMasterModels);

        if(StringUtils.isNotBlank(counterType)) {
            if (counterType.equalsIgnoreCase("DEALER")) {
                builder.append("SELECT {s:pk} FROM {SclCustomer as s} where {s:counterType}=?counterType and {s:territoryCode} in (?territoryMasterModels) ");
                params.put("counterType", CounterType.DEALER);
            } else if (counterType.equalsIgnoreCase("RETAILER")) {
                builder.append("SELECT distinct {dr.retailer} FROM {DealerRetailerMapping AS dr join SclCustomer as d on {d.pk}={dr.dealer}  join SclCustomer as s on {s:pk}={dr:retailer} } WHERE {dr:retailer} is not null and {d:territoryCode} in (?territoryMasterModels) ");
            } else if (counterType.equalsIgnoreCase("INFLUENCER")) {
                builder.append("SELECT {s.pk} FROM {").append(CUSTOMER_FOR_USER_QUERY).append("} WHERE {m.subAreaMaster} in (?subAreaMaster) ");
                params.put("counterType", CounterType.INFLUENCER);
            } else if (counterType.equalsIgnoreCase("SITE")) {
                builder.append("SELECT {s.pk} FROM {").append(CUSTOMER_FOR_USER_QUERY).append("} WHERE {m.subAreaMaster} in (?subAreaMaster) ");
                params.put("counterType", CounterType.SITE);
            }
        }else if(StringUtils.isBlank(counterType) && !sclExclusiveCustomer){
            return getAllNonSclCustomers(territoryManagementService.getTaulkaForUser(filterTalukaData),null,null,null);
        }
        if(isZeroLift){
            builder.append(" and (({s:lastLiftingDate}<?firstDay OR  {s:lastLiftingDate}>=?lastDay) OR {s:lastLiftingDate} is NULL ) ");
        }
        if(isLowPerform){
            builder.append(" and {s:lastLiftingDate} is NOT NULL and {s:lastLiftingDate}>=?firstDay and  {s:lastLiftingDate}<?lastDay ");
        }
        if(sclExclusiveCustomer){
            builder.append(" and {s:defaultB2BUnit}=?defaultB2BUnit ");
            B2BUnitModel sclShreeUnit = getB2BUnitPk("SclShreeUnit");
            params.put("defaultB2BUnit",sclShreeUnit);
        }

        if(StringUtils.isNotBlank(searchKeyFilter)) {
            builder.append(" and ( lower({s:uid}) like lower(?searchKeyFilter) OR lower({s:name}) like lower(?searchKeyFilter) ) ");
            params.put("searchKeyFilter", "%"+searchKeyFilter+"%");
        }
        if(StringUtils.isNotBlank(networkType)){
            builder.append(" and lower({s:networkType}) like lower(?networkType) ");
            params.put("networkType",networkType+"%");
            // lower({s:networkType}) like lower('dormant%')
        }
        if(counterType.equalsIgnoreCase("RETAILER")){
            builder.append(" order by {dr.retailer} DESC ");
        }else {
            builder.append(" order by {s.creationTime} DESC ");
        }
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(SclCustomerModel.class));
        query.addQueryParameters(params);
        LOGGER.info("Query to get Current network customer:"+query);
        final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
        LOGGER.info(String.format("REsult Set of current network customer:%s", searchResult.getResult()!=null?searchResult.getResult():null));
        List<SclCustomerModel> result = searchResult.getResult();

        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }
    @Override
    public Map<String, Object> findMaxInvoicedDateAndQuantityForRetailer(final UserModel user,SclCustomerModel dealer) {
        Map<String, Object> map = new HashMap<>();
        try {

            map.put("liftingDate", null);
            map.put("liftingQty", 0.0);

            final Map<String, Object> attr = new HashMap<String, Object>();

            final StringBuilder sql1 = new StringBuilder();
            final StringBuilder sql3 = new StringBuilder();

            if(Objects.nonNull(dealer)) {
                sql1.append("SELECT  MAX({di.invoiceCreationDateAndTime}) FROM { OrderEntry AS oe JOIN Order AS o ON {o.pk}={oe.order} JOIN deliveryItem AS di ON {oe:pk}={di:entry} } where {o:user}=?sclDealer and {oe:retailer}=?user and {oe.cancelledDate} is null ");
                sql3.append("SELECT MAX({or:liftingDate}) from {OrderRequisition AS or} WHERE {or:status}=?requisitionStatus and {or.toCustomer}=?sclRetailer and {or.fromCustomer}=?sclDealer and {or:product} is not null and {or:requisitionType}=?requisitionType ");
                attr.put("sclDealer",dealer);
            }else{
                sql1.append("SELECT  MAX({di.invoiceCreationDateAndTime}) FROM { OrderEntry AS oe JOIN Order AS o ON {o.pk}={oe.order} JOIN deliveryItem AS di ON {oe:pk}={di:entry} } where {oe:retailer}=?user and {oe.cancelledDate} is null ");
                sql3.append("SELECT MAX({or:liftingDate}) from {OrderRequisition AS or} WHERE {or:status}=?requisitionStatus and {or.toCustomer}=?sclRetailer and {or:product} is not null and {or:requisitionType}=?requisitionType ");
            }
            attr.put("user", user);
            attr.put("sclRetailer", user);
            attr.put("requisitionStatus", RequisitionStatus.SERVICED_BY_DEALER);
            attr.put("requisitionType", RequisitionType.LIFTING);

            final FlexibleSearchQuery query1 = new FlexibleSearchQuery(sql1.toString());
            query1.setResultClassList(Arrays.asList(Date.class));
            query1.getQueryParameters().putAll(attr);
            LOGGER.info("Query from DeliveryItem:" + query1);
            final SearchResult<Date> result1 = this.getFlexibleSearchService().search(query1);

            final FlexibleSearchQuery query3 = new FlexibleSearchQuery(sql3.toString());
            query3.setResultClassList(Arrays.asList(Date.class));
            query3.getQueryParameters().putAll(attr);
            LOGGER.info("Query from Order requisition:" + query3);
            final SearchResult<Date> result3 = this.getFlexibleSearchService().search(query3);

            Date lastLiftingDate = null, date1 = null, date2 = null;
            if (result1.getResult() != null && !result1.getResult().isEmpty()) {
                date1 = result1.getResult().get(0);
                LOGGER.info("Last Lift date from delivery Item" + date1);
            }
            if (result3.getResult() != null && !result3.getResult().isEmpty()) {
                date2 = result3.getResult().get(0);
                LOGGER.info("Last Lift date from Order req" + date2);
            }
            if (Objects.nonNull(date1) && Objects.nonNull(date2)) {
                LocalDate localDate1 = date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate localDate2 = date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                List<LocalDate> dates = new ArrayList<>();
                dates.add(localDate1);
                dates.add(localDate2);
                LocalDate maxDate = Collections.max(dates);
                lastLiftingDate = Date.from(maxDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            } else if (Objects.isNull(date1) && Objects.nonNull(date2)) {
                lastLiftingDate = date2;
            } else if (Objects.nonNull(date1)) {
                lastLiftingDate = date1;
            }
            boolean orderDateFlag = false, deliveryFlag = false;
            final StringBuilder sql2 = new StringBuilder();
            if (lastLiftingDate != null) {
                if (lastLiftingDate.equals(date1)) {
                    deliveryFlag = true;
                } else if (lastLiftingDate.equals(date2)) {
                    orderDateFlag = true;
                }
                if (deliveryFlag) {

                    sql2.append("SELECT {di.invoiceQuantity} FROM { OrderEntry AS oe JOIN Order AS o ON {o.pk}={oe.order} JOIN deliveryItem AS di ON {oe:pk}={di:entry} } where {oe:retailer}=?user and  {di:invoiceCreationDateAndTime} like ?date and {di.cancelledDate} is null ");
                } else if (orderDateFlag) {

                    sql2.append("SELECT {or:quantity} from {OrderRequisition AS or} WHERE {or:status}=?requisitionStatus and {or.toCustomer}=?sclRetailer and {or:product} is not null and {or:requisitionType}=?requisitionType and {or:liftingDate} like ?date ");
                }

                String pattern = "yyyy-MM-dd HH:mm";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                String date = simpleDateFormat.format(lastLiftingDate);
                LOGGER.info("LAst Lift date after format:" + date);
                map.put("liftingDate", date);
                attr.put("date", "%" + date + "%");
                final FlexibleSearchQuery query2 = new FlexibleSearchQuery(sql2.toString());
                query2.setResultClassList(Arrays.asList(Double.class));
                query2.getQueryParameters().putAll(attr);
                LOGGER.info("LAst Lift qty query:" + query2);
                final SearchResult<Double> quantityRes = this.getFlexibleSearchService().search(query2);
                if (quantityRes.getResult() != null && !quantityRes.getResult().isEmpty()) {
                    Double quantity = quantityRes.getResult().get(0);
                    LOGGER.info("LAst Lift qty" + quantity);
                    if (quantity != null) {
                        map.put("liftingQty", quantity);
                    }
                }
            }

            LOGGER.info(map.get("liftingQty") + " " + map.get("liftingDate"));
            return map;
        }catch (Exception e){
            LOGGER.info("Cron last lifting Exception:"+e.getMessage()+" Cause:"+e.getCause()+" Stack:"+e.getStackTrace());
        }
        return map;
    }

    @Override
    public List<SclCustomerModel> getAllNonSclCustomers(List<SubAreaMasterModel> subAreaMasterModels,String subAreaMasterId,String route,RequestCustomerData customerData)
    {
        SubAreaMasterModel subArea=null;
        if(subAreaMasterId!=null) {
            subArea = territoryManagementService.getTerritoryById(subAreaMasterId);
        }
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {cus.sclCustomer} FROM {CustomerSubAreaMapping as cus join SclCustomer as s on {s:pk}={cus:sclCustomer} ");
        if(subArea!=null) {
            builder.append("  join CounterRouteMapping as cr on {s.uid}={cr.counterCode} } ");
        }else{
            builder.append(" } ");
        }
        builder.append(" WHERE {cus.subAreaMaster} in (?subAreaMaster) AND {cus.isActive} = ?active AND {cus.brand} = ?brand   and {s:defaultB2BUnit} in (?defaultB2BUnit) ");
        String excludeInfuencerSite = dataConstraintDao.findVersionByConstraintName(SclCoreConstants.CUSTOMER.EXCLUDE_INFLUENCER_SITE);
        if(BooleanUtils.isTrue(Boolean.valueOf(excludeInfuencerSite))) {
            builder.append(" and {s:counterType} not in (?counterList) ");
            List<CounterType>  counterTypes=new ArrayList<>();
            counterTypes.add(enumerationService.getEnumerationValue(CounterType.class,SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_TYPE));
            counterTypes.add( enumerationService.getEnumerationValue(CounterType.class,SclCoreConstants.CUSTOMER.SITE_USER_GROUP_TYPE));
            params.put("counterList",counterTypes);
        }
        boolean active = Boolean.TRUE;
        params.put("subAreaMaster", subAreaMasterModels);
        params.put("active", active);
        params.put("brand", baseSiteService.getCurrentBaseSite());
        List<B2BUnitModel> unitModelList=new ArrayList<>();
        B2BUnitModel sclOtherUnit = getB2BUnitPk("SclOtherUnit");
        B2BUnitModel sclShreeUnit = getB2BUnitPk("SclShreeUnit");
        if(customerData!=null){
            if(BooleanUtils.isTrue(customerData.getIncludeSclNonSclCustomer())) {
                unitModelList.add(sclOtherUnit);
                unitModelList.add(sclShreeUnit);
                params.put("defaultB2BUnit", unitModelList);
            }else{
                unitModelList.add(sclOtherUnit);
                params.put("defaultB2BUnit", unitModelList);
            }
        }else{
            unitModelList.add(sclOtherUnit);
            params.put("defaultB2BUnit", unitModelList);
        }

        if(subArea!=null) {
            builder.append(" and {cr.district}=?district and {cr.taluka}=?taluka and {cr.route}=?route and {cr.brand}=?brand  ")  ;
            String district = subArea.getDistrict();
            String taluka = subArea.getTaluka();
            if(subArea.getDistrictMaster()!=null) {
                district = subArea.getDistrictMaster().getName();
            }

            params.put("district", district);
            params.put("taluka", taluka);
            params.put("route", route);
            params.put("brand", baseSiteService.getCurrentBaseSite());
        }
        if(Objects.nonNull(customerData)) {
            appendFilterQuery(builder, params, customerData);
        }
        builder.append(" order by {s.creationTime} DESC ");
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(SclCustomerModel.class));
        query.addQueryParameters(params);
        LOG.info(String.format("getNonSCLCustomerForSubArea:: %s",query));
        final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
        List<SclCustomerModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }
    @Override
    public List<SclCustomerModel> getCurrentNetworkCustomersForTSMRH(RequestCustomerData requestCustomerData) {
        String CUSTOMER_FOR_USER_QUERY = "CustomerSubAreaMapping as m join SclCustomer as s on {m.sclCustomer}={s.pk} ";
        final Map<String, Object> params = new HashMap<String, Object>();
        List<SubAreaMasterModel> subAreaMasterModelList=new ArrayList<>();
        if(requestCustomerData!=null && requestCustomerData.getSubAreaMasterPk()!=null){
            SubAreaMasterModel territoryById = territoryManagementService.getTerritoryById(requestCustomerData.getSubAreaMasterPk());
            if(territoryById!=null) {
                subAreaMasterModelList.add(territoryById);
                requestCustomerData.setIncludeSclNonSclCustomer(true);
                params.put("subAreaMaster", subAreaMasterModelList);
            }
        }else {
            FilterTalukaData filterTalukaData = new FilterTalukaData();
            subAreaMasterModelList.addAll(territoryManagementService.getTaulkaForUser(filterTalukaData));
            params.put("subAreaMaster", subAreaMasterModelList);
        }

        ZoneId zone = ZoneId.systemDefault();
        LocalDate firstDayOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDayOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);
        ZonedDateTime firstDay = firstDayOfMonth.atStartOfDay(zone);
        ZonedDateTime lastDay = lastDayOfMonth.atStartOfDay(zone);
        Date date1 = Date.from(firstDay.toInstant());
        Date date2 = Date.from(lastDay.toInstant());
        params.put("firstDay", date1);
        params.put("lastDay", date2);

        final StringBuilder builder = new StringBuilder();
        List<TerritoryMasterModel> territoryMasterModels= (List<TerritoryMasterModel>) territoryMasterService.getCurrentTerritory();
        params.put("territoryMasterModels", territoryMasterModels);

        if (requestCustomerData !=null && requestCustomerData.getCounterType() != null && !requestCustomerData.getCounterType().isEmpty()) {
            for (String counterType : requestCustomerData.getCounterType()) {
                if (StringUtils.isNotBlank(counterType) && CounterType.valueOf(counterType) != null) {
                    if (counterType.equalsIgnoreCase("DEALER")) {
                        builder.append("SELECT {s:pk} FROM {SclCustomer as s} where {s:counterType}=?counterType and {s:territoryCode} in (?territoryMasterModels) ");
                        params.put("counterType", CounterType.DEALER);
                    } else if (counterType.equalsIgnoreCase("RETAILER")) {
                        builder.append("SELECT distinct {dr.retailer} FROM {DealerRetailerMapping AS dr join SclCustomer as d on {d.pk}={dr.dealer}  join SclCustomer as s on {s:pk}={dr:retailer} } WHERE {dr:retailer} is not null and {d:territoryCode} in (?territoryMasterModels) ");
                    } else if (counterType.equalsIgnoreCase("INFLUENCER")) {
                        builder.append("SELECT {s.pk} FROM {").append(CUSTOMER_FOR_USER_QUERY).append("} WHERE {m.subAreaMaster} in (?subAreaMaster) ");
                        params.put("counterType", CounterType.INFLUENCER);
                    } else if (counterType.equalsIgnoreCase("SITE")) {
                        builder.append("SELECT {s.pk} FROM {").append(CUSTOMER_FOR_USER_QUERY).append("} WHERE {m.subAreaMaster} in (?subAreaMaster) ");
                        params.put("counterType", CounterType.SITE);
                    }
                }
            }
        }else {
            return getAllNonSclCustomers(subAreaMasterModelList,null,null,requestCustomerData);
        }
        if(Objects.nonNull(requestCustomerData)) {
            appendFilterQuery(builder, params, requestCustomerData);
        }
        builder.append(" and {s:defaultB2BUnit}=?defaultB2BUnit ");
        B2BUnitModel sclShreeUnit = getB2BUnitPk("SclShreeUnit");
        params.put("defaultB2BUnit",sclShreeUnit);
        if (requestCustomerData.getCounterType() != null && !requestCustomerData.getCounterType().isEmpty()) {
            for (String counterType : requestCustomerData.getCounterType()) {
                if (StringUtils.isNotBlank(counterType) && CounterType.valueOf(counterType) != null) {
                    if (counterType.equalsIgnoreCase("RETAILER")) {
                        builder.append(" order by {dr.retailer} DESC ");
                    } else {
                        builder.append(" order by {s.creationTime} DESC ");
                    }
                }
            }
        }
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(SclCustomerModel.class));
        query.addQueryParameters(params);
        LOGGER.info("Query to get Current network customer for CustomerFOrUser:"+query);
        final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
        LOGGER.info(String.format("REsult Set of current network customer:%s", searchResult.getResult()!=null?searchResult.getResult():null));
        List<SclCustomerModel> result = searchResult.getResult();

        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    public void appendFilterQuery(StringBuilder builder, Map<String, Object> params, RequestCustomerData requestCustomerData) {
        if(requestCustomerData!=null) {
            if (StringUtils.isNotBlank(requestCustomerData.getNetworkType())) {
                builder.append(" and lower({s.networkType}) like lower(?networkType) ");
                params.put("networkType", "%" + requestCustomerData.getNetworkType() + "%");
            }
            if (StringUtils.isNotBlank(requestCustomerData.getSearchKey())) {
                builder.append(" and (UPPER({s.name}) like (?filter) or {s.uid} like (?filter) or {s.customerNo} like (?filter) or {s.mobileNumber} like (?filter) ) ");
                params.put("filter", "%" + requestCustomerData.getSearchKey().toUpperCase() + "%");
            }
            if (StringUtils.isNotBlank(requestCustomerData.getInfluencerType())) {
                builder.append(" and {s.influencerType}=?influencerType ");
                params.put("influencerType", InfluencerType.valueOf(requestCustomerData.getInfluencerType()));
            }
            if (StringUtils.isNotBlank(requestCustomerData.getCustomerUid())) {
                builder.append(" and {s.uid}=?uid ");
                params.put("uid", requestCustomerData.getCustomerUid());
            }
            if (requestCustomerData.getRemoveFlaggedCustomer() != null && requestCustomerData.getRemoveFlaggedCustomer()) {
//            builder.append("and {c.isDealerFlag}=?isDealerFlag ");
//            params.put("isDealerFlag", Boolean.FALSE);
            }
            if (Objects.nonNull(requestCustomerData.getDealerCategory())) {
                builder.append(" and {s.dealerCategory}=?dealerCategory ");
                params.put("dealerCategory", DealerCategory.valueOf(requestCustomerData.getDealerCategory()));
            }
            if (requestCustomerData.getIsNew() != null && requestCustomerData.getIsNew()) {
                LocalDate lastNinetyDay = LocalDate.now().minusDays(90);
                builder.append(" and {s.dateOfJoining}>=?lastNinetyDay ");
                params.put("lastNinetyDay", lastNinetyDay.toString());
            }
        /*if (requestCustomerData.getIncludeNonSclCustomer() == null || !requestCustomerData.getIncludeNonSclCustomer()) {
            builder.append(" and {m.isOtherBrand}=?isOtherBrand ");
            params.put(CustomerSubAreaMappingModel.ISOTHERBRAND, Boolean.FALSE);
        }*/
            if (requestCustomerData.getIsFlag() != null && requestCustomerData.getIsFlag().equals(Boolean.TRUE)) {
                builder.append(" and {s.isDealerFlag}=?isDealerFlag ");
                params.put("isDealerFlag", Boolean.TRUE);
            }
            if (requestCustomerData.getIsUnFlag() != null && requestCustomerData.getIsUnFlag().equals(Boolean.TRUE)) {
                builder.append(" and {s.isUnFlagRequestRaised}=?isUnFlagRequestRaised ");
                params.put("isUnFlagRequestRaised", Boolean.TRUE);
            }
        }
       /* if (requestCustomerData.getDistrict() != null) {
            builder.append(" and {m.district}=?district ");
            params.put("district", requestCustomerData.getDistrict());
        }
        BaseSiteModel baseSiteForUID = baseSiteService.getCurrentBaseSite();
        builder.append(" and {m.brand}=?baseSiteForUID ");
        params.put("baseSiteForUID", baseSiteForUID);*/
    }

}
