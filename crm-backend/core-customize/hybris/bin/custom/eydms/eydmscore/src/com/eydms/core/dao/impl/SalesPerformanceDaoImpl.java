package com.eydms.core.dao.impl;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.dao.SalesPerformanceDao;
import com.eydms.core.enums.*;
import com.eydms.core.model.*;
import com.eydms.core.region.dao.impl.DistrictMasterDaoImpl;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.core.utility.EyDmsDateUtility;
import com.eydms.facades.data.FilterDistrictData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserModel;
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
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.awt.*;
import java.util.*;
import java.util.List;

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
    public Double getActualTargetForSalesMTD(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel site) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}} WHERE"
                + "{o:placedBy} = ?eydmsUser and {o:versionID} IS NULL and {o:site} =?site and {o:subAreaMaster}=?subArea and {oe.cancelledDate} is null and  " + EyDmsDateUtility.getMtdClauseQuery("oe:invoiceCreationDateAndTime", params));
        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("subArea", territoryManagementService.getTerritoryById(subArea));
        params.put("eydmsUser", eydmsUser);
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
    public Double getActualTargetForSalesYTD(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel site, Date startDate, Date endDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}}  WHERE "
                + "{o:placedBy} = ?eydmsUser and {o:versionID} IS NULL and {o:site} =?site and {o:subAreaMaster}=?subArea and {oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} <= ?endDate and {oe.cancelledDate} is null ");
        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("subArea", territoryManagementService.getTerritoryById(subArea));
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

    @Override
    public Double getMonthlySalesTarget(EyDmsUserModel eydmsUser, BaseSiteModel site, String month, String year) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        if(eydmsUser!=null){
            if(eydmsUser.getUserType().getCode()!=null) {
                if (eydmsUser.getUserType().getCode().equalsIgnoreCase("SO")) {
                    builder.append("select sum({m:dealerReviewedTotalRevisedTarget}) from {MonthlySales as m} where {m:so}=?eydmsUser and {m:monthName}=?month and {m:monthYear}=?year");
                    params.put("eydmsUser", eydmsUser);
                } else if (eydmsUser.getUserType().getCode().equals("RH") || eydmsUser.getUserType().getCode().equalsIgnoreCase("TSM")) {
                    builder.append("select sum({m:dealerReviewedTotalRevisedTarget}) from {MonthlySales as m} where {m:monthName}=?month and {m:monthYear}=?year");
                }
            }
        }
        else{
            builder.append("select sum({m:dealerReviewedTotalRevisedTarget}) from {MonthlySales as m} where {m:so}=?eydmsUser and {m:monthName}=?month and {m:monthYear}=?year");
        }

        //params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("eydmsUser", eydmsUser);
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
    public Double getMonthlySalesTarget(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, String month, String year, List<String> doList, List<String> subAreaList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {m:dealerReviewedTotalRevisedTarget} from {MonthlySales as m} where {m:so}=?eydmsUser and {m:monthName}=?month and {m:monthYear}=?year");
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
        //params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("eydmsUser", eydmsUser);
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
    public List<DealerRevisedMonthlySalesModel> getMonthlySalesTargetForDealerWithBGP(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite, String month, String year, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        // userService.setCurrentUser(userService.getAdminUser());
        final StringBuilder builder = new StringBuilder("select {m:pk} from {DealerRevisedMonthlySales as m} where {m:customerCode}=?eydmsCustomer and {m:monthName}=?month and {m:monthYear}=?year");
        params.put("eydmsCustomer", eydmsCustomer.getUid());
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
    public Double getMonthlySalesTargetForDealer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite, String month, String year, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select sum({m:revisedTarget}) from {DealerRevisedMonthlySales as m} where {m:customerCode}=?eydmsCustomer and {m:monthName}=?month and {m:monthYear}=?year");
        params.put("eydmsCustomer", eydmsCustomer.getUid());
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
    public Double getMonthlySalesTargetForSP(List<EyDmsCustomerModel> eydmsCustomer, String month, String year) {
        final Map<String, Object> params = new HashMap<String, Object>();
        List<String> customerCodeList=new ArrayList<>();
        for (EyDmsCustomerModel customerModel : eydmsCustomer) {
            customerCodeList.add(customerModel.getUid());
        }

        final StringBuilder builder = new StringBuilder("select sum({m:revisedTarget}) from {DealerRevisedMonthlySales as m} where {m:customerCode} in (?eydmsCustomer) and {m:monthName}=?month and {m:monthYear}=?year");
        params.put("eydmsCustomer",customerCodeList);
        params.put("month",month);
        params.put("year",year);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(Double.class));

        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
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
    public List<MonthWiseAnnualTargetModel> getMonthlySalesTargetForRetailerWithBGP(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite, String month, String year, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        // userService.setCurrentUser(userService.getAdminUser());
        //select {m:monthTarget} from {MonthWiseAnnualTarget as m on {m:retailerRevisedAnnualSales} where {m:retailerCode}='' and {m:isAnnualSalesReviewedForRetailer} = true and {m:monthName}='May-2023 and {m:monthYear}='2023'
        final StringBuilder builder = new StringBuilder("select {m:pk} from {MonthWiseAnnualTarget as m} where {m:retailerCode}=?eydmsCustomer and {m:monthName}=?month and {m:monthYear}=?year and {m:isAnnualSalesReviewedForRetailer}=?isAnnualSalesReviewedForRetailer");
        params.put("isAnnualSalesReviewedForRetailer", true);
        params.put("eydmsCustomer", eydmsCustomer.getUid());
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

    public double getActualSaleForDealerGraphYTD(EyDmsCustomerModel eydmsCustomer, Date startDate, Date endDate, BaseSiteModel site, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();

        final StringBuilder builder = new StringBuilder();
        if (StringUtils.isBlank(bgpFilter)) {
            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}}  WHERE {o:user} = ?eydmsCustomer and {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and {oe:invoiceCreationDateAndTime} >= ?startDate and {oe:invoiceCreationDateAndTime} < ?endDate");
        } else if (bgpFilter.equalsIgnoreCase("ALL")) {
            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}}  WHERE {o:user} = ?eydmsCustomer and {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and {oe:invoiceCreationDateAndTime} >= ?startDate and {oe:invoiceCreationDateAndTime} < ?endDate");
        } else {
            CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
            ProductModel product = productService.getProductForCode(catalogVersion, bgpFilter);
            if (product != null) {
                builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk} JOIN Product as p on {oe:product}={p:pk}}  WHERE {o:user} = ?eydmsCustomer and {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and {oe:invoiceCreationDateAndTime} >= ?startDate and {oe:invoiceCreationDateAndTime} < ?endDate and {oe:product} =?product");
                params.put("product", product);
            }
        }
        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("eydmsCustomer", eydmsCustomer);
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
    public double getActualSaleForGraphYTD(EyDmsUserModel user, Date startDate, Date endDate, BaseSiteModel currentBaseSite, List<String> doList, List<String> subAreaList) {
        final Map<String, Object> params = new HashMap<String, Object>();

        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}}  WHERE {oe:status}=?orderStatus and {oe:deliveredDate} >= ?startDate and {oe:deliveredDate} < ?endDate");

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
    public Double getLastMonthSalesTarget(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, String month, String year) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        builder.append("select {m:totalTarget} from {MonthlySales as m} where {m:so}=?eydmsUser and {m:monthName}=?month and {m:monthYear}=?year");
        params.put("eydmsUser", eydmsUser);
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
    public List<List<Object>> getAnnualSalesTarget(EyDmsUserModel eydmsUser, String financialYear) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select sum({ann:totalReviewedTargetForAllDealers}) from {AnnualSales as ann} where {ann:salesOfficer}=?eydmsUser and {ann:financialYear}=?financialYear and {ann:isAnnualSalesReviewedForDealer}=?isAnnualSalesReviewedForDealer");
        params.put("eydmsUser", eydmsUser);
        //params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("financialYear", financialYear);
        params.put("isAnnualSalesReviewedForDealer", true);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(Double.class));
        final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
        List<List<Object>> result = searchResult.getResult();
        return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
    }

    @Override
    public Double getAnnualSalesTargetForDealer(EyDmsCustomerModel eydmsCustomerModel, String financialYear, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        //userService.setCurrentUser(userService.getAdminUser());
        final StringBuilder builder = new StringBuilder("select sum({ds:totalTarget}) from {DealerRevisedAnnualSales as ds JOIN AnnualSales as ann on {ds:annualSales}={ann.pk}} where {ds:customerCode}=?eydmsCustomer and {ann:financialYear}=?financialYear and {ann:isAnnualSalesReviewedForDealer}=?isAnnualSalesReviewedForDealer and {ds:isExistingDealerRevisedForReview}=?isExistingDealerRevisedForReview");
        params.put("eydmsCustomer", eydmsCustomerModel.getUid());
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
    public Double getAnnualSalesTargetForSP(List<EyDmsCustomerModel> eydmsCustomerModel, String financialYear) {
        final Map<String, Object> params = new HashMap<String, Object>();
        List<String> customerCodeList=new ArrayList<>();
        for (EyDmsCustomerModel customerModel : eydmsCustomerModel) {
            customerCodeList.add(customerModel.getUid());
        }

        final StringBuilder builder = new StringBuilder("select sum({ds:totalTarget}) from {DealerRevisedAnnualSales as ds JOIN AnnualSales as ann on {ds:annualSales}={ann.pk}} where {ds:customerCode} in (?eydmsCustomer) and {ann:financialYear}=?financialYear and {ann:isAnnualSalesReviewedForDealer}=?isAnnualSalesReviewedForDealer and {ds:isExistingDealerRevisedForReview}=?isExistingDealerRevisedForReview");
        params.put("eydmsCustomer", customerCodeList);
        params.put("financialYear", financialYear);
        params.put("isExistingDealerRevisedForReview",true);
        params.put("isAnnualSalesReviewedForDealer",true);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());;
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }


    @Override
    public List<DealerRevisedAnnualSalesModel> getAnnualSalesTargetForDealerWithBGP(EyDmsCustomerModel eydmsCustomerModel, String financialYear, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        builder.append("select {ds:pk} from {DealerRevisedAnnualSales as ds JOIN AnnualSales as ann on {ds:annualSales}={ann.pk}} where {ds:customerCode}=?eydmsCustomer and {ann:financialYear}=?financialYear and {ann:isAnnualSalesReviewedForDealer}=?isAnnualSalesReviewedForDealer and {ds:isExistingDealerRevisedForReview}=?isExistingDealerRevisedForReview");
        params.put("eydmsCustomer", eydmsCustomerModel.getUid());
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
    public Double getAnnualSalesTargetForRetailer(EyDmsCustomerModel eydmsCustomerModel, String financialYear, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        /*final StringBuilder builder = new StringBuilder("select {totalTarget}  from {RetailerRevisedAnnualSalesDetails} where {customerCode}=?eydmsCustomer and {isAnnualSalesReviewedForRetailer}=?isAnnualSalesReviewedForRetailer and {financialYear}=?financialYear");*/
        final StringBuilder builder = new StringBuilder("select {r:totalTarget}  from {AnnualSales as a JOIN RetailerRevisedAnnualSales as rs on {rs:annualSales}={a:pk} JOIN RetailerRevisedAnnualSalesDetails as r on {r:retailerRevisedAnnualSales}={rs:pk}} where {r:customerCode}=?eydmsCustomer and {r:isAnnualSalesReviewedForRetailer}=?isAnnualSalesReviewedForRetailer and {a:financialYear}=?financialYear");
        params.put("eydmsCustomer", eydmsCustomerModel.getUid());
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
    public List<RetailerRevisedAnnualSalesModel> getAnnualSalesTargetForRetailerWithBGP(EyDmsCustomerModel eydmsCustomerModel, String financialYear, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        //userService.setCurrentUser(userService.getAdminUser());
        final StringBuilder builder = new StringBuilder();
        builder.append("select {rs:pk} from {RetailerRevisedAnnualSales as rs JOIN AnnualSales as ann on {rs:annualSales}={ann.pk}} where {rs:customerCode}=?eydmsCustomer and {ann:financialYear}=?financialYear and {ann:isAnnualSalesReviewedForRetailer}=?isAnnualSalesReviewedForRetailer and {rs:isExistingRetailerRevisedForReview}=?isExistingRetailerRevisedForReview");
        params.put("eydmsCustomer", eydmsCustomerModel.getUid());
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
    public List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatio(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, EyDmsCustomerModel customerModel, List<String> doList, List<String> subAreaList) {
        try {
            EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
            final Map<String, Object> params = new HashMap<String, Object>();
            List<DistrictMasterModel> list = new ArrayList<>();
            List<SubAreaMasterModel> list1 = new ArrayList<>();
            final StringBuilder builder = new StringBuilder();
          /*  builder.append("SELECT {p:code},{p:name},sum({oe:quantityInMT}) from " +
                    "{OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN " +
                    "Product as p on {oe:product}={p:pk} JOIN " +
                    "EyDmsCustomer as sc on {sc:pk}={o:user} JOIN " +
                    "Product as p on {oe:product}={p:pk}}" +
                    "where {o:placedBy}= ?eydmsUser and " +
                    "{oe:status}=?orderStatus and " +
                    "{o:site} = ?baseSite and ");*/
            builder.append("SELECT {p:code},{p:name},sum({oe:quantityInMT}) from " +
                    "{OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN " +
                    "Product as p on {oe:product}={p:pk}} " +
                    "where {oe.cancelledDate} is null AND ");
            if (Objects.nonNull(customerModel)) {
                builder.append("{o:user}=?eydmsCustomer and ");
            }

            builder.append(EyDmsDateUtility.getMtdClauseQuery("oe.invoiceCreationDateAndTime", params));
            if(doList!=null && !doList.isEmpty()){
                for(String code: doList){
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
            params.put("eydmsUser", eydmsUser);
            params.put("baseSite", baseSite);
            params.put("orderStatus", orderStatus);
            if (Objects.nonNull(customerModel)) {
                params.put("eydmsCustomer", customerModel);
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
    public List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatio(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, List<String> doList, List<String> subAreaList) {
        try {
            EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
            final Map<String, Object> params = new HashMap<String, Object>();
            List<DistrictMasterModel> list = new ArrayList<>();
            List<SubAreaMasterModel> list1 = new ArrayList<>();
            final StringBuilder builder = new StringBuilder();
          /*  builder.append("SELECT {p:code},{p:name},sum({oe:quantityInMT}) from " +
                    "{OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN " +
                    "Product as p on {oe:product}={p:pk} JOIN " +
                    "EyDmsCustomer as sc on {sc:pk}={o:user} JOIN " +
                    "Product as p on {oe:product}={p:pk}}" +
                    "where {o:placedBy}= ?eydmsUser and " +
                    "{oe:status}=?orderStatus and " +
                    "{o:site} = ?baseSite and ");*/
            builder.append("SELECT {p:code},{p:name},sum({oe:quantityInMT}) from " +
                    "{OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN " +
                    "Product as p on {oe:product}={p:pk}} " +
                    "where {oe.cancelledDate} is null and ").append(EyDmsDateUtility.getMtdClauseQuery("oe:invoiceCreationDateAndTime", params));
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
            params.put("eydmsUser", eydmsUser);
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
                "JOIN EyDmsCustomer as sc on {o:user}={sc:pk}} " +
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
    public List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForCustomer(EyDmsCustomerModel customer,
                                                                                       BaseSiteModel baseSite, String customerType, List<String> doList, List<String> subAreaList) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            List<DistrictMasterModel> list = new ArrayList<>();
            List<SubAreaMasterModel> list1 = new ArrayList<>();
            final StringBuilder builder = new StringBuilder();

            if ((customer.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
                builder.append("SELECT {p:code},{p:name},sum({oe:quantity}) from " +
                        "{OrderRequisition AS oe JOIN " +
                        "Product as p on {oe:product}={p:pk}} WHERE " +
                        "{oe:status}=?requisitionStatus and " +
                        "{oe.toCustomer}=?customer and " +
                        EyDmsDateUtility.getMtdClauseQuery("oe:deliveredDate", params));
                builder.append(" group by {p:code},{p:name}");
                RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
                params.put("customer", customer);
                params.put("requisitionStatus", requisitionStatus);
            }
            else
            {
                builder.append("SELECT {p:code},{p:name},sum({oe:quantityInMT}) from " +
                        "{OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN " +
                        "Product as p on {oe:product}={p:pk}} Where " +
                        "{oe.cancelledDate} is null AND ").append(EyDmsDateUtility.getMtdClauseQuery("oe:invoiceCreationDateAndTime", params));
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

                if (customer.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
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

    @Override
    public List<List<Object>> getBrandwiseSalesPercentRatioAndVolumeRatioForCustomer(EyDmsCustomerModel customer) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder();
            if ((customer.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
                builder.append("SELECT {p:code},{p:name},sum({oe:quantity}) from " +
                        "{OrderRequisition AS oe JOIN " +
                        "Product as p on {oe:product}={p:pk}} WHERE " +
                        "{oe:status}=?requisitionStatus and " +
                        "{oe.toCustomer}=?customer and " +
                        EyDmsDateUtility.getMtdClauseQuery("oe:deliveredDate", params));
                builder.append(" group by {p:code},{p:name}");
                RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
                params.put("customer", customer);
                params.put("requisitionStatus", requisitionStatus);
            }
            if ((customer.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID)))) {
                builder.append("SELECT {p:code},{p:name},sum({oe:quantity}) from " +
                        "{PointRequisition AS oe JOIN " +
                        "Product as p on {oe:product}={p:pk}} WHERE " +
                        "{oe:status}=?pointRequisitionStatus and " +
                        "{oe.requestRaisedFor}=?customer and " +
                        EyDmsDateUtility.getMtdClauseQuery("oe:deliveryDate", params));
                builder.append(" group by {p:code},{p:name}");
                PointRequisitionStatus pointRequisitionStatus = PointRequisitionStatus.APPROVED;
                params.put("customer", customer);
                params.put("pointRequisitionStatus", pointRequisitionStatus);
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
    public List<List<Object>> getProductMixPercentRatioAndVolumeRatioWithPoints(EyDmsCustomerModel customer, String filter,Date startDate,Date endDate)
    {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder();
            if ((customer.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID)))) {
                if(StringUtils.isBlank(filter)){
                    builder.append("SELECT {p:code},{p:name},sum({oe:quantity}),sum({oe:points}) from " +
                            "{PointRequisition AS oe JOIN " +
                            "Product as p on {oe:product}={p:pk}} WHERE " +
                            "{oe:status}=?pointRequisitionStatus and " +
                            "{oe.requestRaisedFor}=?customer and " +
                            EyDmsDateUtility.getMtdClauseQuery("oe:deliveryDate", params));
                    builder.append(" group by {p:code},{p:name}");
                }
                else if(filter.equalsIgnoreCase("MTD")) {
                    builder.append("SELECT {p:code},{p:name},sum({oe:quantity}),sum({oe:points}) from " +
                            "{PointRequisition AS oe JOIN " +
                            "Product as p on {oe:product}={p:pk}} WHERE " +
                            "{oe:status}=?pointRequisitionStatus and " +
                            "{oe.requestRaisedFor}=?customer and " +
                            EyDmsDateUtility.getMtdClauseQuery("oe:deliveryDate", params));
                    builder.append(" group by {p:code},{p:name}");
                }
                else if(filter.equalsIgnoreCase("YTD")) {
                    builder.append("SELECT {p:code},{p:name},sum({oe:quantity}),sum({oe:points}) from " +
                            "{PointRequisition AS oe JOIN " +
                            "Product as p on {oe:product}={p:pk}} WHERE " +
                            "{oe:status}=?pointRequisitionStatus and " +
                            "{oe.requestRaisedFor}=?customer and " +
                            "{oe:deliveryDate}>=?startDate and {oe:deliveryDate}<=?endDate ");
                    builder.append(" group by {p:code},{p:name}");
                }
                else{
                    builder.append("SELECT {p:code},{p:name},sum({oe:quantity}),sum({oe:points}) from " +
                            "{PointRequisition AS oe JOIN " +
                            "Product as p on {oe:product}={p:pk}} WHERE " +
                            "{oe:status}=?pointRequisitionStatus and " +
                            "{oe.requestRaisedFor}=?customer and " +
                            EyDmsDateUtility.getMtdClauseQuery("oe:deliveryDate", params));
                    builder.append(" group by {p:code},{p:name}");
                }
                PointRequisitionStatus pointRequisitionStatus = PointRequisitionStatus.APPROVED;
                params.put("customer", customer);
                params.put("startDate",startDate);
                params.put("endDate",endDate);
                params.put("pointRequisitionStatus", pointRequisitionStatus);
            }

            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class, String.class, Double.class,Double.class));
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
        final StringBuilder builder = new StringBuilder("SELECT SUM({s:quantity}) FROM {SalesHistory as s JOIN EyDmsCustomer as sc on {s:customerNo}={sc:customerNo} JOIN PrincipalGroupRelation as p on {p:source}={sc:pk} JOIN UserGroup as u on {u:pk}={p:target}} WHERE {customerCategory}=?category and {u:uid}='EyDmsDealerGroup' AND {brand}=?brand AND").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("invoiceDate", month, year, params));
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
        final StringBuilder builder = new StringBuilder("SELECT SUM({s:quantity}) FROM {SalesHistory as s JOIN EyDmsCustomer as sc on {s:customerNo}={sc:customerNo} JOIN CustomerSubAreaMapping as cs on {sc:pk}={cs:eydmsCustomer}} where {cs:subArea}=?subArea JOIN PrincipalGroupRelation as p on {p:source}={sc:pk} JOIN UserGroup as u on {u:pk}={p:target}} WHERE {customerCategory}=?category and {u:uid}='EyDmsDealerGroup' AND {brand}=?brand AND").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("invoiceDate", month, year, params));
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
            //.append(EyDmsDateUtility.getDateClauseQueryByMonthYear("invoiceDate", month, year, params)).append("group by {s:taluka}");
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
    public List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForYTD(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, Date startDate, Date endDate, EyDmsCustomerModel customer, List<String> doList, List<String> subAreaList) {
        try {
            B2BCustomerModel user = (B2BCustomerModel) getUserService().getCurrentUser();
            final Map<String, Object> params = new HashMap<String, Object>();
            List<DistrictMasterModel> list = new ArrayList<>();
            List<SubAreaMasterModel> list1 = new ArrayList<>();
            final StringBuilder builder = new StringBuilder();
            final StringBuilder query = new StringBuilder();

            builder.append("SELECT {p:code},{p:name},sum({oe:quantityInMT}) from " +
                    "{OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN " +
                    "Product as p on {oe:product}={p:pk}} " +
                    "Where {oe.cancelledDate} is null AND  " +
                    "{oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} <= ?endDate ");
            if (Objects.nonNull(customer)) {
                if ((customer.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
                    if (Objects.nonNull(customer)) {
                        builder.append(" and {o:user}=?customer ");
                        params.put("customer", customer);
                    }
                }
            }
            if(user.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.SP_GROUP_ID)))
            {
                if (Objects.nonNull(user.getState())) {
                    String state = user.getState();
                    builder.append(" and {p:state}=?state ");
                    params.put("state", state);
                }
            }
            if (Objects.nonNull(customer)) {
                if ((customer.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
                    query.append("SELECT {p:code},{p:name},sum({oe:quantity}) from " +
                            "{OrderRequisition AS oe JOIN " +
                            "Product as p on {oe:product}={p:pk}} WHERE " +
                            "{oe:status}=?requisitionStatus and " +
                            "{oe.toCustomer}=?customer and " +
                            "{oe.deliveredDate} >= ?startDate and {oe.deliveredDate} <= ?endDate ");

                    query.append(" group by {p:code},{p:name}");
                    RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
                    params.put("customer", customer);
                    params.put("requisitionStatus", requisitionStatus);
                }
            }

            if (doList != null && !doList.isEmpty()) {
                for (String code : doList) {
                    list.add(districtMasterDao.findByCode(code));
                }
                params.put("doList", list);
                query.append(" and {oe.districtMaster} in (?doList) ");
                builder.append(" and {o.districtMaster} in (?doList) ");
            }
            if (subAreaList != null && !subAreaList.isEmpty()) {
                for (String id : subAreaList) {
                    list1.add(territoryManagementDao.getTerritoryById(id));
                }
                params.put("subAreaList", list1);
                builder.append(" and {o.subAreaMaster} in (?subAreaList) ");
                query.append(" and {oe.subAreaMaster} in (?subAreaList) ");
            }
            builder.append(" group by {p:code},{p:name}");

            OrderStatus orderStatus = OrderStatus.DELIVERED;
            //params.put("eydmsUser", eydmsUser);

            params.put("baseSite", baseSite);
            params.put("orderStatus", orderStatus);
            params.put("startDate", startDate);
            params.put("endDate", endDate);

            final FlexibleSearchQuery qry = new FlexibleSearchQuery(builder.toString());
            qry.addQueryParameters(params);
            qry.setResultClassList(Arrays.asList(String.class, String.class, Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(qry);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForMonthPicker(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, int month, int year, List<String> doList, List<String> subAreaList) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
            List<DistrictMasterModel> list = new ArrayList<>();
            List<SubAreaMasterModel> list1 = new ArrayList<>();
            // final StringBuilder builder = new StringBuilder("SELECT {p:code},{p:name},sum({oe:quantityInMT}) from {OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN Product as p on {oe:product}={p:pk} JOIN EyDmsCustomer as sc on {sc:pk}={o:user}} where {o:placedBy}= ?eydmsUser and {oe:status}=?orderStatus and {o:site} = ?baseSite and").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("oe:deliveredDate",month,year,params)).append("group by {p:code},{p:name}");

            final StringBuilder builder = new StringBuilder("SELECT {p:code},{p:name},sum({oe:quantityInMT}) from {OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN Product as p on {oe:product}={p:pk}} where {oe.cancelledDate} is null AND  ").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("oe:invoiceCreationDateAndTime",month,year,params));

           /* builder.append("SELECT {p:code},{p:name},sum({oe:quantityInMT}) from " +
                    "{OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN " +
                    "Product as p on {oe:product}={p:pk} JOIN " +
                    "EyDmsCustomer as sc on {sc:pk}={o:user} JOIN " +
                    "Product as p on {oe:product}={p:pk}}" +
                    "where {o:placedBy}= ?eydmsUser and " +
                    "{oe:status}=?orderStatus and " +
                    "{o:site} = ?baseSite and " +
                    EyDmsDateUtility.getDateClauseQueryByMonthYear("oe:deliveredDate", month, year, params) +
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
            params.put("eydmsUser", eydmsUser);
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
    public Double getActualTargetSalesForSelectedMonthAndYear(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel site, int month, int year) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}}  WHERE "

                + "{o:placedBy} = ?eydmsUser and {o:versionID} IS NULL and {o:site} =?site and {o:subAreaMaster}=?subArea and {oe.cancelledDate} is null AND ").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("oe:invoiceCreationDateAndTime",month,year,params));
        OrderStatus orderStatus=OrderStatus.DELIVERED;
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("eydmsUser",eydmsUser);
        params.put("site",site);
        params.put("orderStatus",orderStatus);


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
    public Double getActualTargetSalesForSelectedMonthAndYearForDealer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel baseSite, int month, int year, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        if (StringUtils.isBlank(bgpFilter)) {
            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}}  WHERE "
                    + "{o:user} = ?eydmsCustomer and {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and ").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("oe:invoiceCreationDateAndTime", month, year, params));
        } else if (bgpFilter.equalsIgnoreCase("ALL")) {
            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}}  WHERE "
                    + "{o:user} = ?eydmsCustomer and {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and ").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("oe:invoiceCreationDateAndTime", month, year, params));

        } else {
            CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
            ProductModel product = productService.getProductForCode(catalogVersion, bgpFilter);
            if (product != null) {
                builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk} JOIN Product as p on {oe:product}={p:pk}}  WHERE "
                        + "{o:user} = ?eydmsCustomer and {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and {oe:product} =?product and").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("oe:invoiceCreationDateAndTime", month, year, params));
                params.put("product", product);
            }
        }

        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("orderStatus", orderStatus);
        params.put("eydmsCustomer", eydmsCustomer);
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

    public Double getActualTargetSalesForSelectedMonthAndYearForSP(List<EyDmsCustomerModel> eydmsCustomer, int month, int year) {
        final Map<String, Object> params = new HashMap<String, Object>();
        //userService.setCurrentUser(userService.getAdminUser());
        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}}  WHERE {o:user} in (?eydmsCustomer) and "
                + " {o:versionID} IS NULL and {oe.cancelledDate} is null and").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("oe:invoiceCreationDateAndTime",month,year,params));
        OrderStatus orderStatus=OrderStatus.DELIVERED;
        params.put("orderStatus",orderStatus);
        params.put("eydmsCustomer",eydmsCustomer);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    @Override
    public Double getActualTargetSalesForSelectedMonthAndYearForRetailer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel baseSite, int month, int year,String bgpFilter) {

        final Map<String, Object> params = new HashMap<String, Object>();
        //userService.setCurrentUser(userService.getAdminUser());
        LOGGER.info("YearFilter and MonthFilter and  EyDmsCustomer PK:"+ year + " and "+ month + " and "+eydmsCustomer);
        final StringBuilder builder = new StringBuilder();
        if (StringUtils.isBlank(bgpFilter)) {
            builder.append("SELECT SUM({oe:quantity}) FROM {OrderRequisition AS oe}  WHERE "
                    + "{oe:status}=?requisitionStatus and {oe.toCustomer}=?eydmsCustomer and {oe:cancelledDate} is null and {oe:product} is not null and").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("oe:deliveredDate", month, year, params));
        } else if (bgpFilter.equalsIgnoreCase("ALL")) {
            builder.append("SELECT SUM({oe:quantity}) FROM {OrderRequisition AS oe}  WHERE "
                    + "{oe:status}=?requisitionStatus and {oe.toCustomer}=?eydmsCustomer and {oe:cancelledDate} is null and {oe:product} is not null and").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("oe:deliveredDate", month, year, params));
        } else {
            CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
            ProductModel product = productService.getProductForCode(catalogVersion, bgpFilter);
            if (product != null) {
                //builder.append("SELECT SUM({oe:quantity}) FROM {OrderRequisition AS oe}  WHERE "
                //         + "{oe:status}=?requisitionStatus and {oe.toCustomer}=?eydmsCustomer and {oe:product} like ?bgpFilter and").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("oe:deliveredDate",month,year,params));
                builder.append("SELECT SUM({oe:quantity}) from {OrderRequisition AS oe JOIN Product as p on {oe:product}={p:pk}} WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?eydmsCustomer and {oe:product} =?product and {oe:cancelledDate} is null and {oe:product} is not null and").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("oe:deliveredDate", month, year, params));
                params.put("product", product);

            }
        }
        RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
        params.put("requisitionStatus", requisitionStatus);
        params.put("eydmsCustomer", eydmsCustomer);
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
        final StringBuilder builder = new StringBuilder("select SUM({n:ncr}) from {EyDmsNCRThreshold as n} where {n:state}=?state AND {n:brand}=?site AND {n:yearMonth}=?yearMonth");
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
    public Double getActualTargetForSalesMTD(EyDmsUserModel eydmsUser, BaseSiteModel site, List<String> doList, List<String> subAreaList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
        List<DistrictMasterModel> list = new ArrayList<>();
        List<SubAreaMasterModel> list1 = new ArrayList<>();
        final StringBuilder builder = new StringBuilder();
        //builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}} WHERE"
        //            + "{o:placedBy} = ?eydmsUser and {o:versionID} IS NULL and {o:site} =?site and {oe:status}=?orderStatus and" + EyDmsDateUtility.getMtdClauseQuery("oe:deliveredDate", params));

        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk}} WHERE "

                + "{oe.cancelledDate} is null and " + EyDmsDateUtility.getMtdClauseQuery("oe:invoiceCreationDateAndTime", params));
        if(doList!=null && !doList.isEmpty()){
            for(String code: doList){

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
        params.put("eydmsUser", eydmsUser);
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
    public Double getActualTargetForSalesDealerMTD(EyDmsCustomerModel eydmsCustomer, BaseSiteModel site, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        //userService.setCurrentUser(userService.getAdminUser());
        final StringBuilder builder = new StringBuilder();
        if (StringUtils.isBlank(bgpFilter)) {
            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}} WHERE {o:user} = ?eydmsCustomer and {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and").append(EyDmsDateUtility.getMtdClauseQuery("oe:invoiceCreationDateAndTime", params));
        } else if (bgpFilter.equalsIgnoreCase("ALL")) {
            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}} WHERE {o:user} = ?eydmsCustomer and {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and").append(EyDmsDateUtility.getMtdClauseQuery("oe:invoiceCreationDateAndTime", params));
        } else {
            CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
            ProductModel product = productService.getProductForCode(catalogVersion, bgpFilter);
            if (product != null) {
                builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk} JOIN Product as p on {oe:product}={p:pk}} WHERE {o:user} = ?eydmsCustomer and {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and {oe:product} =?product and").append(EyDmsDateUtility.getMtdClauseQuery("oe:invoiceCreationDateAndTime", params));
                // String filterKey= "%".concat(bgpFilter.toUpperCase()).concat("%");
                //String filterKey= "%".concat(String.valueOf(product)).concat("%");
                params.put("product", product);
            }
        }
        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("eydmsCustomer", eydmsCustomer);
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
    public Double getActualTargetForSalesSPMTD(List<EyDmsCustomerModel> eydmsCustomer) {
        {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder();

            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}}  WHERE {o:user} in (?eydmsCustomer)  and   "
                    + " {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and ").append(EyDmsDateUtility.getMtdClauseQuery("oe:invoiceCreationDateAndTime", params));

            OrderStatus orderStatus=OrderStatus.DELIVERED;
            params.put("eydmsCustomer",eydmsCustomer);
            params.put("orderStatus",orderStatus);
            params.put("site",baseSiteService.getCurrentBaseSite());
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.setResultClassList(Arrays.asList(Double.class));
            query.addQueryParameters(params);
            final SearchResult<Double> searchResult = flexibleSearchService.search(query);
            if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty())) {
                return searchResult.getResult().get(0)!=null?searchResult.getResult().get(0):0.0;
            }
            else{
                return 0.0;
            }
        }
    }

    @Override
    public Double getActualTargetForSalesRetailerMTD(EyDmsCustomerModel customer, BaseSiteModel site, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        //userService.setCurrentUser(userService.getAdminUser());
        final StringBuilder builder = new StringBuilder();
        LOG.info("EyDmsCustomer and MTD date:"+ customer + " "+ EyDmsDateUtility.getMtdClauseQuery("",params));
        if (StringUtils.isBlank(bgpFilter)) {
            builder.append("select sum({p.quantity}) from {OrderRequisition as p} ")
                    .append(" where {p.toCustomer}=?customer and {p:status}=?requisitionStatus and ").append(EyDmsDateUtility.getMtdClauseQueryRetailer("p:deliveredDate", params));

        } else if (bgpFilter.equalsIgnoreCase("ALL")) {
            builder.append("SELECT sum({oe:quantity}) from {OrderRequisition AS oe} WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?customer and {oe:product} is not null and").append(EyDmsDateUtility.getMtdClauseQueryRetailer("oe:deliveredDate", params));
        } else {
            CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
            ProductModel product = productService.getProductForCode(catalogVersion, bgpFilter);
            if (product != null) {
                builder.append("SELECT sum({oe:quantity}) from {OrderRequisition AS oe JOIN Product as p on {oe:product}={p:pk}} WHERE {oe:status}=?requisitionStatus and {oe:toCustomer}=?customer and {oe:product} =?product and {oe:product} is not null and").append(EyDmsDateUtility.getMtdClauseQueryRetailer("oe:deliveredDate", params));
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
    public Double getActualTargetForSalesLastMonth(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, int year, int month, List<String> doList, List<String> subAreaList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
        List<DistrictMasterModel> list = new ArrayList<>();
        List<SubAreaMasterModel> list1 = new ArrayList<>();
        final StringBuilder builder = new StringBuilder();
        /*builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}} WHERE"

                + "{o:placedBy} = ?eydmsUser and {o:versionID} IS NULL and {o:site} =?site and {oe:status}=?orderStatus and {oe.invoiceCreationDateAndTime}>=?startDate and {oe.invoiceCreationDateAndTime} <=?endDate");*/
        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}} WHERE {oe.cancelledDate} is null and").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("oe:invoiceCreationDateAndTime",month,year,params));
        if(doList!=null && !doList.isEmpty()){
            for(String code: doList){

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
        params.put("eydmsUser", eydmsUser);
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
        builder.append("SELECT {distance} FROM {DestinationSourceMaster} AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}} WHERE"
                + "{o:placedBy} = ?eydmsUser and {o:versionID} IS NULL and {o:site} =?site and {oe:status}=?orderStatus and {oe.invoiceCreationDateAndTime}>=?startDate and {oe.invoiceCreationDateAndTime} <=?endDate");
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
    public Double getActualTargetForSalesYTD(EyDmsUserModel eydmsUser, BaseSiteModel site, Date startDate, Date endDate, List<String> doList, List<String> subAreaList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
        List<DistrictMasterModel> list = new ArrayList<>();
        List<SubAreaMasterModel> list1 = new ArrayList<>();
        final StringBuilder builder = new StringBuilder();
        //builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}}  WHERE "
        //        + "{o:placedBy} = ?eydmsUser and {o:versionID} IS NULL and {o:site} =?site and {oe:status}=?orderStatus and {oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} <= ?endDate");
        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}}  WHERE "

                + "{oe.cancelledDate} is null and {oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} <= ?endDate");
        if(doList!=null && !doList.isEmpty()){
            for(String code: doList){

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

    @Override
    public Double getActualTargetForPartnerSalesYTD(String code, EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate, List<String> doList, List<String> subAreaList) {
        {
            final Map<String, Object> params = new HashMap<String, Object>();
            EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
            List<DistrictMasterModel> list = new ArrayList<>();
            List<SubAreaMasterModel> list1 = new ArrayList<>();
            final StringBuilder builder = new StringBuilder();
            //builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}}  WHERE "
            //        + "{o:placedBy} = ?eydmsUser and {o:versionID} IS NULL and {o:site} =?site and {oe:status}=?orderStatus and {oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} <= ?endDate");
            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}}  WHERE "

                    + "{oe.cancelledDate} is null and {sc:uid}=?code and {oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} <= ?endDate");
            if(doList!=null && !doList.isEmpty()){
                for(String codes: doList){

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
            params.put("eydmsUser", eydmsUser);
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
    public Double getActualTargetForSalesDealerYTD(EyDmsCustomerModel eydmsCustomer, BaseSiteModel site, Date startDate, Date endDate, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        // userService.setCurrentUser(userService.getAdminUser());
        final StringBuilder builder = new StringBuilder();

        if(StringUtils.isBlank(bgpFilter)){
            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}}  WHERE {o:user} = ?eydmsCustomer and {oe.cancelledDate} is null and {oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} < ?endDate");
        }
        else if(bgpFilter.equalsIgnoreCase("ALL")){
            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}}  WHERE {o:user} = ?eydmsCustomer and {oe.cancelledDate} is null and {oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} < ?endDate");
        }
        else {
            CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
            ProductModel product = productService.getProductForCode(catalogVersion,bgpFilter);
            if(product!=null) {
                builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk} JOIN Product as p on {oe:product}={p:pk}}  WHERE {o:user} = ?eydmsCustomer and {oe.cancelledDate} is null and {oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} < ?endDate and {oe:product} =?product");
                // String filterKey = "%".concat(bgpFilter.toUpperCase()).concat("%");

                //String filterKey= "%".concat(String.valueOf(product)).concat("%");
                params.put("product", product);
            }
        }
        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("eydmsCustomer", eydmsCustomer);
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
    public Double getActualTargetForSalesRetailerYTD(EyDmsCustomerModel customer, BaseSiteModel site, Date startDate, Date endDate, String bgpFilter) {
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
    public Double getActualTargetForSalesLeaderYTD(EyDmsCustomerModel eydmsUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();

        LOGGER.info(String.format("EyDmsCustomer Model PK:%s",eydmsUser));

        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk}}  WHERE {o:user} = ?eydmsCustomer and {o:versionID} IS NULL and {o:site} =?site and {oe:status}=?orderStatus and {oe.deliveredDate} >= ?startDate and {oe.deliveredDate} <= ?endDate");

        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("orderStatus", orderStatus);
        params.put("eydmsCustomer", eydmsUser);
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
    public Double getActualTargetForSalesLastYear(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, String startDate, String endDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}}  WHERE "

                + "{o:placedBy} = ?eydmsUser and {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and {oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} <= ?endDate");
        OrderStatus orderStatus=OrderStatus.DELIVERED;
        params.put("eydmsUser",eydmsUser);
        params.put("site",currentBaseSite);
        params.put("orderStatus",orderStatus);
        params.put("startDate",startDate);
        params.put("endDate",endDate);

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
    public Double getActualTargetSalesForSelectedMonthAndYear(EyDmsUserModel eydmsUser, BaseSiteModel site, int month, int year, List<String> doList, List<String> subAreaList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
        List<DistrictMasterModel> list = new ArrayList<>();
        List<SubAreaMasterModel> list1 = new ArrayList<>();
        final StringBuilder builder = new StringBuilder();

        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}}  WHERE "

                + " {oe.cancelledDate} is null and").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("oe:invoiceCreationDateAndTime",month,year,params));


        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("eydmsUser", eydmsUser);
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
    public Double getActualTargetPartnerSalesForSelectedMonthAndYear(String code, EyDmsUserModel eydmsUser, BaseSiteModel baseSite, int month, int year, List<String> doList, List<String> subAreaList) {
        {
            final Map<String, Object> params = new HashMap<String, Object>();
            EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
            List<DistrictMasterModel> list = new ArrayList<>();
            List<SubAreaMasterModel> list1 = new ArrayList<>();
            final StringBuilder builder = new StringBuilder();
     /*   builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}}  WHERE "
                + "{o:placedBy} = ?eydmsUser and {o:versionID} IS NULL and {o:site} =?site and {oe:status}=?orderStatus and").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("oe:deliveredDate",month,year,params));*/
            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}}  WHERE "

                    + " {oe.cancelledDate} is null and {sc:uid}=?code and ").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("oe:invoiceCreationDateAndTime",month,year,params));

            OrderStatus orderStatus=OrderStatus.DELIVERED;
            params.put("eydmsUser",eydmsUser);
            params.put("code",code);
            params.put("site",baseSite);
            params.put("orderStatus",orderStatus);
            if(doList!=null && !doList.isEmpty()){
                for(String codes: doList){

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
    public Double getActualTargetForPartnerSalesMTD(String code, EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, List<String> doList, List<String> subAreaList) {
        {
            final Map<String, Object> params = new HashMap<String, Object>();
            EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
            List<DistrictMasterModel> list = new ArrayList<>();
            List<SubAreaMasterModel> list1 = new ArrayList<>();
            final StringBuilder builder = new StringBuilder();
            //builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}} WHERE"
            //            + "{o:placedBy} = ?eydmsUser and {o:versionID} IS NULL and {o:site} =?site and {oe:status}=?orderStatus and" + EyDmsDateUtility.getMtdClauseQuery("oe:deliveredDate", params));

            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {sc:pk}={o:user}} WHERE "

                    + "{oe.cancelledDate} is null and {sc:uid}=?code and " + EyDmsDateUtility.getMtdClauseQuery("oe:invoiceCreationDateAndTime", params));
            if(doList!=null && !doList.isEmpty()){
                for(String codes: doList){

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
            params.put("eydmsUser", eydmsUser);
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
    public List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForYTD(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, Date startDate, Date endDate, List<String> doList, List<String> subAreaList) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
            List<DistrictMasterModel> list = new ArrayList<>();
            List<SubAreaMasterModel> list1 = new ArrayList<>();
            final StringBuilder builder = new StringBuilder();
            /*builder.append("SELECT {p:code},{p:name},sum({oe:quantityInMT}) from {OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN Product as p on {oe:product}={p:pk} JOIN EyDmsCustomer as sc on {sc:pk}={o:user} JOIN Product as p on {oe:product}={p:pk}}where {o:placedBy}= ?eydmsUser and {oe:status}=?orderStatus and " +

                    "{o:site} = ?baseSite and {oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} <= ?endDate group by {p:code},{p:name}");*/
            builder.append("SELECT {p:code},{p:name},sum({oe:quantityInMT}) from {OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN Product as p on {oe:product}={p:pk}} where {oe.cancelledDate} is null and " +
                    "{oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} <= ?endDate group by {p:code},{p:name}");
            if(doList!=null && !doList.isEmpty()){
                for(String code: doList){

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
            params.put("eydmsUser", eydmsUser);
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
            final StringBuilder builder = new StringBuilder("SELECT {sc:uid},{sc:name},{sc:mobileNumber},{sc:customerNo} from {EYDMSCustomer as sc} " +
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
    public List<List<Object>> getSecondaryLeadDistanceForMonth(EyDmsUserModel eydmsUser, WarehouseType warehouseType, BaseSiteModel baseSite, Integer year1, Integer month1, List<String> doList, List<String> subAreaList) {
        final Map<String, Object> attr = new HashMap<String, Object>();
        EyDmsUserModel user = (EyDmsUserModel) userService.getCurrentUser();
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
        sql.append(EyDmsDateUtility.getDateClauseQueryByMonthYear("oe.truckDispatcheddate", month1, year1, attr));

        List<DistrictMasterModel> list = new ArrayList<>();
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
        }
        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Integer.class, Double.class));
        query.addQueryParameters(attr);
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
    public List<List<Object>> getSecondaryLeadDistance(EyDmsUserModel eydmsUser, WarehouseType warehouseType, BaseSiteModel baseSite, List<String> doList, List<String> subAreaList) {

        final Map<String, Object> attr = new HashMap<String, Object>();
        EyDmsUserModel user = (EyDmsUserModel) userService.getCurrentUser();
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
        sql.append(EyDmsDateUtility.getMtdClauseQuery("oe.truckDispatcheddate", attr));

        List<DistrictMasterModel> list = new ArrayList<>();
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
        }
        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Integer.class, Double.class));
        query.addQueryParameters(attr);
        final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult() != null && !searchResult.getResult().isEmpty() ? searchResult.getResult() : Collections.emptyList();

    }

    @Override
    public List<List<Object>> getSecondaryLeadDistanceMTD(EyDmsUserModel eydmsUser, WarehouseType warehouseType, BaseSiteModel baseSite, List<String> doList, List<String> subAreaList) {
        //.append(EyDmsDateUtility.getMtdClauseQuery("oe:deliveredDate", params));
        final Map<String, Object> attr = new HashMap<String, Object>();
        EyDmsUserModel user = (EyDmsUserModel) userService.getCurrentUser();
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
        sql.append(EyDmsDateUtility.getMtdClauseQuery("oe.truckDispatcheddate", attr));
        List<DistrictMasterModel> list = new ArrayList<>();
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
        }
        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Integer.class, Double.class));
        query.addQueryParameters(attr);
        final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult() != null && !searchResult.getResult().isEmpty() ? searchResult.getResult() : Collections.emptyList();


    }

    @Override
    public List<List<Object>> getSecondaryLeadDistanceYTD(EyDmsUserModel eydmsUser, WarehouseType warehouseType, BaseSiteModel baseSite, Date startDate, Date endDate, List<String> doList, List<String> subAreaList) {
        final Map<String, Object> attr = new HashMap<String, Object>();
        EyDmsUserModel user = (EyDmsUserModel) userService.getCurrentUser();
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
        List<DistrictMasterModel> list = new ArrayList<>();
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
        }
        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Integer.class, Double.class));
        query.addQueryParameters(attr);
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
    public Double getActualTargetForSalesMTDDealer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        // userService.setCurrentUser(userService.getAdminUser());
        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}} WHERE "
                + "{o:user} = ?eydmsCustomer and {o:versionID} IS NULL and {o:site} =?site and {oe:status}=?orderStatus and").append(EyDmsDateUtility.getMtdClauseQuery("oe:deliveredDate", params));
        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("orderStatus", orderStatus);
        params.put("eydmsCustomer", eydmsCustomer);
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
    public Double getActualTargetForSalesMTDRetailer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        // userService.setCurrentUser(userService.getAdminUser());
        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderRequisition AS oe} WHERE "
                + "{oe:status}=?requisitionStatus and {oe.toCustomer}=?eydmsCustomer and").append(EyDmsDateUtility.getMtdClauseQuery("oe:deliveredDate", params));
        RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
        params.put("requisitionStatus", requisitionStatus);
        params.put("eydmsCustomer", eydmsCustomer);
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
    public Double getActualTargetForSalesYTDDealer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite, Date startDate, Date endDate, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
//        userService.setCurrentUser(userService.getAdminUser());
        final StringBuilder builder = new StringBuilder();

        if(StringUtils.isBlank(bgpFilter)){
            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}}  WHERE {o:user} = ?eydmsCustomer and {oe.cancelledDate} is null and {oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} <= ?endDate");
        }
        else if(bgpFilter.equalsIgnoreCase("ALL")){
            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}}  WHERE {o:user} = ?eydmsCustomer and {oe.cancelledDate} is null and {oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} <= ?endDate");
        }
        else {
            CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
            ProductModel product = productService.getProductForCode(catalogVersion,bgpFilter);
            if(product!=null) {
                builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk} JOIN Product as p on {oe:product}={p:pk}}  WHERE {o:user} = ?eydmsCustomer and {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and {oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} <= ?endDate and {oe:product} =?product");

                //String filterKey = "%".concat(bgpFilter.toUpperCase()).concat("%");
                //String filterKey= "%".concat(String.valueOf(product)).concat("%");
                params.put("product", product);
            }
        }
        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("orderStatus", orderStatus);
        params.put("eydmsCustomer", eydmsCustomer);
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
    public Double getActualTargetForSalesYTDSP(List<EyDmsCustomerModel> eydmsCustomer, BaseSiteModel currentBaseSite, Date startDate, Date endDate) {
        {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder();

            for (EyDmsCustomerModel customerModel : eydmsCustomer) {
                LOGGER.info(String.format("EyDmsCustomer Model PK:%s",customerModel));
            }

            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}}  WHERE  {o:user} in (?eydmsCustomer) and {oe.cancelledDate} is null and {oe.invoiceCreationDateAndTime} >= ?startDate and {oe.invoiceCreationDateAndTime} <= ?endDate");
            OrderStatus orderStatus=OrderStatus.DELIVERED;
            params.put("orderStatus",orderStatus);
            params.put("eydmsCustomer",eydmsCustomer);
            params.put("startDate",startDate);
            params.put("endDate",endDate);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.setResultClassList(Arrays.asList(Double.class));
            query.addQueryParameters(params);

            final SearchResult<Double> searchResult = flexibleSearchService.search(query);
            if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
                return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
            else
                return 0.0;
        }
    }


    @Override
    public Double getActualTargetForSalesYTDRetailer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite, Date startDate, Date endDate, String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        LOGGER.info("StartDate and EndDate and EyDmsCustomer:"+ startDate +" "+endDate +" "+eydmsCustomer);
        if (StringUtils.isBlank(bgpFilter)) {
            builder.append("SELECT sum({oe:quantity}) from {OrderRequisition AS oe} WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?eydmsCustomer and {oe:deliveredDate}>=?startDate and {oe:deliveredDate}< ?endDate and {oe:product} is not null");
        } else if (bgpFilter.equalsIgnoreCase("ALL")) {
            builder.append("SELECT sum({oe:quantity}) from {OrderRequisition AS oe} WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?eydmsCustomer and {oe:deliveredDate}>=?startDate and {oe:deliveredDate}< ?endDate and {oe:product} is not null");
        } else {
            CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
            ProductModel product = productService.getProductForCode(catalogVersion, bgpFilter);
            if (product != null) {
                builder.append("SELECT sum({oe:quantity}) from {OrderRequisition AS oe JOIN Product as p on {oe:product}={p:pk}} WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?eydmsCustomer and {oe:deliveredDate}>=?startDate and {oe:deliveredDate}<=?endDate and {oe:product} =?product and {oe:product} is not null");
                params.put("product", product);
            }
        }
        RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
        params.put("requisitionStatus", requisitionStatus);
        params.put("eydmsCustomer", eydmsCustomer);
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

    public double getActualTargetFor10DayBucketForDealer(EyDmsCustomerModel eydmsCustomer, String bgpFilter, Date startDate, Date endDate) {
        BaseSiteModel site = baseSiteService.getCurrentBaseSite();
        final Map<String, Object> params = new HashMap<String, Object>();

        final StringBuilder builder = new StringBuilder();
        if (StringUtils.isBlank(bgpFilter)) {
            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk}}  WHERE {o:user} = ?eydmsCustomer and {oe.cancelledDate} is null and {o:versionID} IS NULL and {o:site} =?site and {oe:invoiceCreationDateAndTime} >= ?startDate and {oe:invoiceCreationDateAndTime} < ?endDate");
        } else if (bgpFilter.equalsIgnoreCase("ALL")) {
            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk}}  WHERE {o:user} = ?eydmsCustomer and {oe.cancelledDate} is null and {o:versionID} IS NULL and {o:site} =?site and {oe:invoiceCreationDateAndTime} >= ?startDate and {oe:invoiceCreationDateAndTime} < ?endDate");
        } else {
            CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
            ProductModel product = productService.getProductForCode(catalogVersion, bgpFilter);
            if (product != null) {
                builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN Product as p on {oe:product}={p:pk}}  WHERE {o:user} = ?eydmsCustomer and {oe.cancelledDate} is null and {o:versionID} IS NULL and {o:site} =?site and {oe:invoiceCreationDateAndTime} >= ?startDate and {oe:invoiceCreationDateAndTime} < ?endDate and {oe:product} =?product");
                params.put("product", product);
            }
        }
        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("orderStatus", orderStatus);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("eydmsCustomer",eydmsCustomer);
        params.put("site",site);
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
    public double getActualTargetFor10DayBucket(EyDmsUserModel eydmsUser, Date startDate, Date endDate, List<String> doList, List<String> subAreaList) {
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
        for (EyDmsCustomerModel customerModel : eydmsCustomerModel) {
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

    public List<DealerRevisedMonthlySalesModel> getMonthlySaleTargetGraphForSP(List<EyDmsCustomerModel> eydmsCustomer, String month, String year) {
        final Map<String, Object> params = new HashMap<String, Object>();
        List<String> customerCodeList=new ArrayList<>();
        for (EyDmsCustomerModel customerModel : eydmsCustomer) {
            customerCodeList.add(customerModel.getUid());
        }
        final StringBuilder builder = new StringBuilder("select {pk} from {DealerRevisedMonthlySales} where {customerCode} in (?eydmsCustomer) and {monthName}=?month and {monthYear}=?year");
        params.put("eydmsCustomer", customerCodeList);
        params.put("month", month);
        params.put("year", year);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder);
        query.addQueryParameters(params);
        final SearchResult<DealerRevisedMonthlySalesModel> searchResult = flexibleSearchService.search(query);
        if(org.apache.commons.collections.CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();
    }


    public DealerRevisedMonthlySalesModel getMonthlySaleTargetGraph(EyDmsUserModel eydmsUserModel, String month, String year, List<String> doList, List<String> subAreaList) {
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
    public Integer findDirectDispatchOrdersMTDCount(final UserModel currentUser, final WarehouseType warehouseType, final int month, final int year, List<String> doList, List<String> subAreaList) {
        final Map<String, Object> attr = new HashMap<String, Object>();
        EyDmsUserModel user = (EyDmsUserModel) userService.getCurrentUser();
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
        sql.append(EyDmsDateUtility.getDateClauseQueryByMonthYear("oe.truckDispatcheddate", month, year, attr));
     /*   if(filterTSMRH!=null && !filterTSMRH.isEmpty()) {
            if (user != null) {
                if (user.getUserType().equals("TSM")) {
                    sql.append(" and {o.subAreaMaster} in (?subAreaPK) ");
                    attr.put("subAreaPK", filterTSMRH);
                }
                if (user.getUserType().equals("RH")) {
                    sql.append(" and {o.subAreaMaster} in (?subAreaPK) ");
                    attr.put("subAreaPK", filterTSMRH);
                    sql.append(" and {o.districtMaster} in (?districtPK) ");
                    attr.put("districtPK", filterTSMRH);
                }
            }
        }*/
        List<DistrictMasterModel> list = new ArrayList<>();
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
        }
        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(Integer.class));
        query.getQueryParameters().putAll(attr);
        final SearchResult<Integer> result = this.getFlexibleSearchService().search(query);
        return result.getResult().get(0);
    }

    @Override
    public Double getActualTargetForSalesLeaderYTDRetailer(EyDmsCustomerModel eydmsUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();

        builder.append("SELECT sum({oe:quantity}) from " +
                "{OrderRequisition AS oe JOIN " +
                "Product as p on {oe:product}={p:pk}} WHERE " +
                "{oe:status}=?requisitionStatus and " +
                "{oe.toCustomer}=?customer and {oe.deliveredDate} >= ?startDate AND {oe.deliveredDate} <= ?endDate" );
        RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
        params.put("requisitionStatus", requisitionStatus);
        params.put("customer", eydmsUser);
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

    public List<List<Object>> getActualTargetForSalesLeader(DistrictMasterModel district,BaseSiteModel currentBaseSite, Date startDate, Date endDate) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder();

            LOGGER.info("Getting sales for the Dealer");

            builder.append("SELECT {sc.uid},SUM({oe.quantityInMt}) FROM {EyDmsCustomer AS sc JOIN CustomerSubAreaMapping AS cs ON {sc:pk} = {cs:eydmsCustomer} JOIN SubAreaMaster AS sm ON {sm.pk} = {cs.subAreaMaster} JOIN DistrictMaster AS d ON {d.pk} = {sm.districtMaster} LEFT JOIN Order AS o ON {cs.eydmsCustomer} = {o.user} LEFT JOIN OrderEntry AS oe ON {oe.order} = {o.pk} AND {oe.status} = ?orderStatus AND {oe.deliveredDate} >= ?startDate AND {oe.deliveredDate} <= ?endDate} WHERE {cs.brand}=?site AND {cs.isOtherBrand}=?isOtherBrand and {sc.counterType}=?counterType AND {d.pk} = ?district GROUP BY {sc.uid} ORDER BY SUM({oe.quantityInMt})");

            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("orderStatus", orderStatus);
            //params.put("eydmsCustomer", eydmsUser);
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

            // builder.append("SELECT {sc.uid},SUM(CASE WHEN {p.premium} =?premium THEN {oe.quantityInMt} ELSE 0 END) AS totalQuantity FROM {EyDmsCustomer AS sc JOIN CustomerSubAreaMapping AS cs ON {sc:pk} = {cs:eydmsCustomer} JOIN SubAreaMaster AS sm ON {sm.pk} = {cs.subAreaMaster} JOIN DistrictMaster AS d ON {d.pk} = {sm.districtMaster} LEFT JOIN Order AS o ON {cs.eydmsCustomer} = {o.user} LEFT JOIN OrderEntry AS oe ON {oe.order} = {o.pk} join Product as p on {oe:product}={p:pk} AND {oe.status} = ?orderStatus AND {oe.deliveredDate} >= ?startDate AND {oe.deliveredDate} <= ?endDate and LEFT JOIN Product AS p ON {oe:product} = {p:pk}} WHERE {cs.brand}=?site AND {cs.isOtherBrand}=?isOtherBrand and {sc.counterType}=?counterType AND {d.pk} = ?district GROUP BY {sc.uid} ORDER BY totalQuantity");
            //builder.append("select {o.user}, sum({oe.quantityInMt}) from {CustomerSubAreaMapping as cs join Order as o on {cs.eydmsCustomer}={o.user} join OrderEntry as oe on {oe.order}={o.pk} join Product as p on {oe:product}={p:pk}} WHERE {o.retailer} is null and {p.premium}=?premium and {oe.deliveredDate} >= ?startDate and {oe.deliveredDate} <= ?endDate  group by {o.user} order by sum({oe.quantityInMt}) desc");
            builder.append("SELECT {sc.uid}, SUM(CASE WHEN {p.premium} =?premium THEN {oe.quantityInMt} ELSE 0 END) AS totalQuantity FROM {EyDmsCustomer AS sc JOIN CustomerSubAreaMapping AS cs ON {sc:pk} = {cs:eydmsCustomer} JOIN SubAreaMaster AS sm ON {sm.pk} = {cs.subAreaMaster} JOIN DistrictMaster AS d ON {d.pk} = {sm.districtMaster} LEFT JOIN Order AS o ON {cs.eydmsCustomer} = {o.user} LEFT JOIN OrderEntry AS oe ON {oe.order} = {o.pk} AND {oe.status} =?orderStatus AND {oe.deliveredDate} >= ?startDate AND {oe.deliveredDate} <= ?endDate LEFT JOIN Product AS p ON {oe:product} = {p:pk}} WHERE {cs.brand}=?site AND {cs.isOtherBrand}=?isOtherBrand AND {sc.counterType}=?counterType AND {d.pk} = ?district GROUP BY {sc.uid} ORDER BY totalQuantity");
            OrderStatus orderStatus=OrderStatus.DELIVERED;
            params.put("orderStatus",orderStatus);
            params.put("district",district);
            params.put("site",currentBaseSite);
            params.put("startDate",startDate);
            params.put("endDate",endDate);
            params.put("premium","Y");
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
    public List<EyDmsCustomerModel> getOrderRequisitionSalesDataForRetailer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel brand, List<String> doList, List<String> subAreaList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder queryString = new StringBuilder("SELECT DISTINCT({sc:pk}) from {EyDmsCustomer as sc LEFT JOIN OrderRequisition AS oe on {sc:pk}={oe:toCustomer}} WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?eydmsCustomer and ").append(EyDmsDateUtility.getMtdClauseQuery("oe:deliveredDate", params));

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
        params.put("eydmsCustomer", eydmsCustomer);
        params.put("brand", brand);
        params.put("requisitionStatus", requisitionStatus);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();
    }
    @Override
    public EyDmsCustomerModel getRetailerSalesForDealer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        //select {d.retailer} from {DealerRetailerMap as d join EyDmsCustomer as c  on {d.retailer}={c.pk}} WHERE {d.dealer}=?eydmsCustomer AND {d.active}=?active AND {d.brand}=?brand
        final StringBuilder queryString = new StringBuilder("SELECT DISTINCT({sc:retailer}) from {DealerRetailerMap as sc LEFT JOIN OrderRequisition AS oe on {sc:retailer}={oe:toCustomer}} " +
                "WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?eydmsCustomer and ").append(EyDmsDateUtility.getMtdClauseQuery("oe:deliveredDate", params));
        RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
        params.put("eydmsCustomer", eydmsCustomer);
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
        query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
        query.addQueryParameters(params);
        final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult() !=null && !searchResult.getResult().isEmpty()? searchResult.getResult().get(0) : null;

    }

    @Override
    public EyDmsCustomerModel getRetailerSalesForDealerLowPerform(EyDmsCustomerModel eydmsCustomer, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder queryString = new StringBuilder("SELECT DISTINCT({sc:retailer}) from {DealerRetailerMap as sc LEFT JOIN OrderRequisition AS oe on {sc:retailer}={oe:toCustomer}} WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?eydmsCustomer and ").append(EyDmsDateUtility.getMtdClauseQuery("oe:deliveredDate", params));
        queryString.append( " group by {sc:retailer} having SUM({oe.quantity})!=0 OR SUM({oe:quantity}) is NOT NULL");
        RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
        params.put("eydmsCustomer", eydmsCustomer);
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
        query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
        query.addQueryParameters(params);
        final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult() !=null && !searchResult.getResult().isEmpty()? searchResult.getResult().get(0) : null;
    }

    @Override
    public List<DealerRetailerMapModel> getRetailerSalesForDealerZeroLift(EyDmsCustomerModel eydmsCustomer, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder queryString = new StringBuilder("SELECT DISTINCT({sc:pk}) from {DealerRetailerMap as sc LEFT JOIN OrderRequisition AS oe on {sc:retailer}={oe:toCustomer}} WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?eydmsCustomer and ").append(EyDmsDateUtility.getMtdClauseQuery("oe:deliveredDate", params));
        queryString.append( " group by {sc:pk} having SUM({oe.quantity})=0 OR SUM({oe:quantity}) is NULL");
        RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
        params.put("eydmsCustomer", eydmsCustomer);
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
    public List<DealerInfluencerMapModel> getInfluencerSalesForDealer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder queryString = new StringBuilder("SELECT DISTINCT({sc:pk}) from {DealerInfluencerMap as sc LEFT JOIN PointRequisition AS oe on {sc:influencer}={oe:requestRaisedFor}} WHERE {oe:status}=?requisitionStatus and {oe.requestRaisedFor}=?eydmsCustomer and  ").append(EyDmsDateUtility.getMtdClauseQuery("oe:deliveryDate", params));
        PointRequisitionStatus requisitionStatus = PointRequisitionStatus.APPROVED;
        params.put("eydmsCustomer", eydmsCustomer);
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
    public List<DealerInfluencerMapModel> getInfluencerSalesForDealerLowPerform(EyDmsCustomerModel eydmsCustomer, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder queryString = new StringBuilder("SELECT DISTINCT({sc:pk}) from {DealerInfluencerMap as sc LEFT JOIN PointRequisition AS oe on {sc:influencer}={oe:requestRaisedFor}} WHERE {oe:status}=?requisitionStatus and {oe.requestRaisedFor}=?eydmsCustomer and ").append(EyDmsDateUtility.getMtdClauseQuery("oe:deliveryDate", params));
        queryString.append( " group by {sc:pk} having SUM({oe.quantity})!=0 OR SUM({oe:quantity}) is NOT NULL");

        PointRequisitionStatus requisitionStatus = PointRequisitionStatus.APPROVED;
        params.put("eydmsCustomer", eydmsCustomer);
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
    public List<DealerInfluencerMapModel> getInfluencerSalesForDealerZeroLift(EyDmsCustomerModel eydmsCustomer, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder queryString = new StringBuilder("SELECT DISTINCT({sc:pk}) from {DealerInfluencerMap as sc LEFT JOIN PointRequisition AS oe on {sc:influencer}={oe:requestRaisedFor}} WHERE {oe:status}=?requisitionStatus and {oe.requestRaisedFor}=?eydmsCustomer and ").append(EyDmsDateUtility.getMtdClauseQuery("oe:deliveryDate", params));
        queryString.append( " group by {sc:pk} having SUM({oe.quantity})=0 OR SUM({oe:quantity}) is NULL");
        PointRequisitionStatus requisitionStatus = PointRequisitionStatus.APPROVED;
        params.put("eydmsCustomer", eydmsCustomer);
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
    public List<EyDmsCustomerModel> getOrderRequisitionSalesDataForZero(EyDmsCustomerModel eydmsCustomer, BaseSiteModel brand, List<String> doList, List<String> subAreaList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder queryString = new StringBuilder("SELECT DISTINCT({sc:pk}) from {EyDmsCustomer as sc LEFT JOIN OrderRequisition AS oe on {sc:pk}={oe:toCustomer}} WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?eydmsCustomer and ").append(EyDmsDateUtility.getMtdClauseQuery("oe:deliveredDate", params));

        queryString.append( " group by {sc:pk} having SUM({oe.quantity})=0 OR SUM({oe:quantity}) is NULL");

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
        params.put("eydmsCustomer", eydmsCustomer);
        params.put("brand", brand);
        params.put("requisitionStatus", requisitionStatus);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();
    }

    @Override
    public List<EyDmsCustomerModel> getOrderRequisitionSalesDataForLowPerform(EyDmsCustomerModel eydmsCustomer, BaseSiteModel brand, List<String> doList, List<String> subAreaList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder queryString = new StringBuilder("SELECT DISTINCT({sc:pk}) from {EyDmsCustomer as sc LEFT JOIN OrderRequisition AS oe on {sc:pk}={oe:toCustomer}} WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?eydmsCustomer and ").append(EyDmsDateUtility.getMtdClauseQuery("oe:deliveredDate", params));

        queryString.append( " group by {sc:pk} having SUM({oe.quantity})!=0 OR SUM({oe:quantity}) is NOT NULL");


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
        params.put("eydmsCustomer", eydmsCustomer);
        params.put("brand", brand);
        params.put("requisitionStatus", requisitionStatus);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();
    }

    @Override
    public List<EyDmsCustomerModel> getPointRequisitionSalesDataForInfluencer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel brand, List<String> doList, List<String> subAreaList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder queryString = new StringBuilder("SELECT DISTINCT({sc:pk}) from {EyDmsCustomer as sc LEFT JOIN PointRequisition AS oe on {sc:pk}={oe:requestRaisedFor}} WHERE {oe:status}=?status and {oe.requestRaisedFor}=?eydmsCustomer and ").append(EyDmsDateUtility.getMtdClauseQuery("oe:deliveryDate", params));
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
        params.put("eydmsCustomer", eydmsCustomer);
        params.put("brand", brand);
        params.put("status", status);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();
    }

    @Override
    public List<EyDmsCustomerModel> getPointRequisitionSalesDataForZero(EyDmsCustomerModel eydmsCustomer, BaseSiteModel brand, List<String> doList, List<String> subAreaList) {
        final Map<String, Object> params = new HashMap<String, Object>();

        final StringBuilder queryString = new StringBuilder("SELECT DISTINCT({sc:pk}) from {EyDmsCustomer as sc LEFT JOIN PointRequisition AS oe on {sc:pk}={oe:requestRaisedFor}} WHERE {oe:status}=?status and {oe.requestRaisedFor}=?eydmsCustomer and ").append(EyDmsDateUtility.getMtdClauseQuery("oe:deliveryDate", params));
        queryString.append( " group by {sc:pk} having SUM({oe.quantity})=0 OR SUM({oe:quantity}) is NULL");

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
        params.put("eydmsCustomer", eydmsCustomer);
        params.put("brand", brand);
        params.put("status", status);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();
    }

    @Override
    public List<EyDmsCustomerModel> getPointRequisitionSalesDataForLowPerform(EyDmsCustomerModel eydmsCustomer, BaseSiteModel brand, List<String> doList, List<String> subAreaList) {
        final Map<String, Object> params = new HashMap<String, Object>();

        final StringBuilder queryString = new StringBuilder("SELECT DISTINCT({sc:pk}) from {EyDmsCustomer as sc LEFT JOIN PointRequisition AS oe on {sc:pk}={oe:requestRaisedFor}} WHERE {oe:status}=?status and {oe.requestRaisedFor}=?eydmsCustomer and ").append(EyDmsDateUtility.getMtdClauseQuery("oe:deliveryDate", params));
        queryString.append( " group by {sc:pk} having SUM({oe.quantity})!=0 OR SUM({oe:quantity}) is NOT NULL");

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
        params.put("eydmsCustomer", eydmsCustomer);
        params.put("brand", brand);
        params.put("status", status);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();
    }

    @Override
    public List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForMonthPickerForDealer(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, int month, int year, List<String> doList, List<String> subAreaList, EyDmsCustomerModel customer) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            //EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
            StringBuilder builder=new StringBuilder();
            if((customer.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
                builder = new StringBuilder("SELECT {p:code},{p:name},sum({oe:quantityInMT}) from {OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN Product as p on {oe:product}={p:pk}} where {oe.cancelledDate} is null and ").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("oe:invoiceCreationDateAndTime", month, year, params));

                if (Objects.nonNull(customer)) {
                    builder.append(" and {o:user}=?customer ");
                    params.put("customer", customer);
                }
                builder.append(" group by {p:code},{p:name}");
                OrderStatus orderStatus = OrderStatus.DELIVERED;
                //params.put("eydmsUser", eydmsUser);
                params.put("baseSite", baseSite);
                params.put("orderStatus", orderStatus);
            }
            else if((customer.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
                builder.append("SELECT {p:code},{p:name},sum({oe:quantity}) from " +
                        "{OrderRequisition AS oe JOIN " +
                        "Product as p on {oe:product}={p:pk}} WHERE " +
                        "{oe:status}=?requisitionStatus and " +
                        "{oe.toCustomer}=?customer and " +
                        EyDmsDateUtility.getDateClauseQueryByMonthYear("oe:deliveredDate", month, year, params));
                builder.append(" group by {p:code},{p:name}");
                RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
                params.put("customer", customer);
                params.put("requisitionStatus", requisitionStatus);
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
    public List<EyDmsCustomerModel> getEyDmsCustomerLastLiftingList(List<EyDmsCustomerModel> customerFilteredList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        if (customerFilteredList != null && !customerFilteredList.isEmpty()) {
            final StringBuilder builder = new StringBuilder("SELECT {c.pk} FROM {EyDmsCustomer as c} Where {c:pk} in (?customerFilteredList) and {c:lastLiftingDate} is NOT NULL and ");
            builder.append(EyDmsDateUtility.getMtdClauseQuery("c:lastLiftingDate", params));
            params.put("customerFilteredList", customerFilteredList);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
            final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
            List<EyDmsCustomerModel> result = searchResult.getResult();
            return result != null && !result.isEmpty() ? result : Collections.emptyList();
        } else {
            return null;
        }
    }

    @Override
    public List<EyDmsCustomerModel> getEyDmsCustomerZeroLiftingList(List<EyDmsCustomerModel> customerFilteredList) {
        final Map<String, Object> params = new HashMap<String, Object>();

        if(customerFilteredList!=null && !customerFilteredList.isEmpty()) {
            final StringBuilder builder = new StringBuilder("SELECT {c.pk} FROM {EyDmsCustomer as c} Where {c:pk} in (?customerFilteredList) and {c:lastLiftingDate} is NULL");
            //   builder.append(EyDmsDateUtility.getMtdClauseQuery("c:lastLiftingDate", params));

            params.put("customerFilteredList", customerFilteredList);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
            final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
            List<EyDmsCustomerModel> result = searchResult.getResult();
            return result != null && !result.isEmpty() ? result : Collections.emptyList();
        } else {
            return null;
        }
    }

    @Override
    public OrderRequisitionModel getEyDmsCustomerFromOrderReq(EyDmsCustomerModel customerModel) {
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
    public Double getRetailerFromOrderReq(EyDmsCustomerModel customerModel,Date startDate, Date endDate) {
        if (customerModel != null) {
            Map<String, Object> map = new HashMap<String, Object>();
            StringBuilder builder = new StringBuilder("Select SUM({quantity}) from {OrderRequisition} where {toCustomer}=?customerModel and {status}=?requisitionStatus ");
            if(startDate!=null && endDate!=null){
                builder.append(" and {deliveredDate}>=?startDate  and {deliveredDate}<=?endDate");
            }
            RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;;
            map.put("requisitionStatus",requisitionStatus);
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
        }
        else{
            return 0.0;
        }
    }

    @Override
    public OrderRequisitionModel getRetailerFromOrderReqDateConstraint(EyDmsCustomerModel customerModel,Date startDate,Date endDate) {
        if (customerModel != null) {
            Map<String, Object> map = new HashMap<String, Object>();
            StringBuilder builder = new StringBuilder("Select {pk} from {OrderRequisition as o JOIN EyDmsCustomer as sc on {sc:pk}={o:toCustomer}} where {o:status}=?requisitionStatus and {o:deliveredDate}>=?startDate and {o:deliveredDate}<=?endDate and  {o.toCustomer}=?customerModel ");
            RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;;
            map.put("requisitionStatus", requisitionStatus);
            map.put("customerModel", customerModel);
            map.put("startDate",startDate);
            map.put("endDate",endDate);
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
    public Double getInfluencerFromOrderReq(EyDmsCustomerModel customerModel,Date startDate,Date endDate) {
        if (customerModel != null) {
            Map<String, Object> map = new HashMap<String, Object>();
            StringBuilder builder = new StringBuilder("Select SUM({quantity}) from {PointRequisition} where {requestRaisedFor}=?customerModel and {deliveryDate}>=?startDate and {deliveryDate}<=?endDate and {status}=?requisitionStatus ");
            PointRequisitionStatus requisitionStatus=PointRequisitionStatus.APPROVED;
            map.put("requisitionStatus",requisitionStatus);
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
        }
        else {
            return 0.0;
        }
    }

    @Override
    public PointRequisitionModel getEyDmsCustomerFromPointReq(EyDmsCustomerModel customerModel) {
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
    public Double getActualTargetForPremiumSalesLeaderYTD(EyDmsCustomerModel so, BaseSiteModel currentBaseSite, Date startDate, Date endDate) {

        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();


        LOGGER.info(String.format("EyDmsCustomer Model PK:%s",so));


        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}}  WHERE {o:user} = ?eydmsCustomer and {o:versionID} IS NULL and {o:site} =?site and {oe:status}=?orderStatus and {oe:deliveredDate} >= ?startDate and {oe:deliveredDate} <= ?endDate");

        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("orderStatus", orderStatus);
        params.put("eydmsCustomer", so);
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
    public Double getActualTargetForPremiumSalesLeaderYTDRetailer(EyDmsCustomerModel eydmsUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();

        builder.append("SELECT sum({oe:quantity}) from " +
                "{OrderRequisition AS oe JOIN " +
                "Product as p on {oe:product}={p:pk}} WHERE " +
                "{oe:status}=?requisitionStatus and {p:premium}=?premium" +
                "{oe.toCustomer}=?customer and {oe.deliveredDate} >= ?startDate AND {oe.deliveredDate} <= ?endDate" );
        RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;;
        params.put("requisitionStatus", requisitionStatus);
        params.put("customer", eydmsUser);
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
    public Double getTotalCDAvailedForDealer(EyDmsCustomerModel so, Date startDate, Date endDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT SUM({availedDiscount}) FROM {CashDiscountAvailed} WHERE {customerNo} = ?customerNo");
        if(startDate!=null && endDate!=null)
        {
            builder.append(" AND {discountAvailedDate} BETWEEN ?startDate AND ?endDate");
            params.put("startDate", startDate);
            params.put("endDate", endDate);

        }
        params.put("customerNo", so);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    @Override
    public Double getTotalCDLostForDealer(EyDmsCustomerModel so, Date startDate, Date endDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT SUM({lostDiscount}) FROM {CashDiscountLost} WHERE {customerNo} = ?customerNo");
        if(startDate!=null && endDate!=null)
        {
            builder.append(" AND {discountLostDate} BETWEEN ?startDate AND ?endDate");
            params.put("startDate", startDate);
            params.put("endDate", endDate);

        }
        params.put("customerNo", so);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    @Override
    public double getActualTargetFor10DayBucketForSP(List<EyDmsCustomerModel> eydmsCustomer, Date startDate1, Date endDate1) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk}}  WHERE {o:user} in (?eydmsCustomer) and {oe.cancelledDate} is null and {oe:invoiceCreationDateAndTime} >= ?startDate and {oe:invoiceCreationDateAndTime} < ?endDate");

        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("orderStatus", orderStatus);
        params.put("startDate", startDate1);
        params.put("endDate", endDate1);
        params.put("eydmsCustomer", eydmsCustomer);
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
    public Double getActualTargetSalesForSelectedMonthAndYearForMTDSP(List<EyDmsCustomerModel> eydmsCustomer, BaseSiteModel currentBaseSite, int month, int year) {
        final Map<String, Object> params = new HashMap<String, Object>();
        //userService.setCurrentUser(userService.getAdminUser());
        final StringBuilder builder = new StringBuilder();

        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}}  WHERE {o:user} in (?eydmsCustomer)  and   "
                + " {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and ").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("oe:invoiceCreationDateAndTime", month, year, params));


        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("orderStatus", orderStatus);
        params.put("eydmsCustomer", eydmsCustomer);
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
    public Double getMonthlySalesTargetForMtdSp(List<EyDmsCustomerModel> eydmsUser, BaseSiteModel currentBaseSite, String monthName, String yearName) {
        final Map<String, Object> params = new HashMap<String, Object>();
        List<String> customerCodeList=new ArrayList<>();
        for (EyDmsCustomerModel customerModel : eydmsUser) {
            customerCodeList.add(customerModel.getUid());
        }
        final StringBuilder builder = new StringBuilder("select {m:revisedTarget} from {DealerRevisedMonthlySales as m} where {m:customerCode} in (?eydmsCustomer) and {m:monthName}=?month and {m:monthYear}=?year");
        params.put("eydmsCustomer", customerCodeList);
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
    public double getActualSaleForDealerGraphYTDSP(List<EyDmsCustomerModel> eydmsCustomer, Date startDate, Date endDate, BaseSiteModel currentBaseSite) {
        final Map<String, Object> params = new HashMap<String, Object>();

        final StringBuilder builder = new StringBuilder();

        builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}}  WHERE {o:user} in (?eydmsCustomer) and {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and {oe:invoiceCreationDateAndTime} >= ?startDate and {oe:invoiceCreationDateAndTime} < ?endDate");

        OrderStatus orderStatus = OrderStatus.DELIVERED;
        params.put("eydmsCustomer", eydmsCustomer);
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
    public List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForCustomerMonthPicker(EyDmsCustomerModel eydmsUser, BaseSiteModel baseSite, int month, int year) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            //EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
            List<DistrictMasterModel> list = new ArrayList<>();
            List<SubAreaMasterModel> list1 = new ArrayList<>();
            final StringBuilder builder = new StringBuilder();
            if ((eydmsUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.SP_GROUP_ID)))
                    || (eydmsUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {

                builder.append("SELECT {p:code},{p:name},sum({oe:quantityInMT}) from {OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN Product as p on {oe:product}={p:pk}} where {oe.cancelledDate} is null AND ").append(EyDmsDateUtility.getDateClauseQueryByMonthYear("oe:invoiceCreationDateAndTime", month, year, params)).append("group by {p:code},{p:name}");
                OrderStatus orderStatus = OrderStatus.DELIVERED;
                params.put("eydmsCustomer", eydmsUser);
                params.put("baseSite", baseSite);
                params.put("orderStatus", orderStatus);
            }
            if ((eydmsUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
                builder.append("SELECT {p:code},{p:name},sum({oe:quantity}) from " +
                        "{OrderRequisition AS oe JOIN " +
                        "Product as p on {oe:product}={p:pk}} WHERE " +
                        "{oe:status}=?requisitionStatus and " +
                        "{oe.toCustomer}=?customer and " +
                        EyDmsDateUtility.getDateClauseQueryByMonthYear("oe:deliveredDate", month, year, params));
                builder.append(" group by {p:code},{p:name}");
                RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
                params.put("customer", eydmsUser);
                params.put("requisitionStatus", requisitionStatus);
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
    public double getMonthWiseForRetailerYTD(EyDmsCustomerModel eydmsCustomer, Date startDate, Date endDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT sum({oe:quantity}) from {OrderRequisition AS oe} WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?eydmsCustomer and {oe:deliveredDate}>=?startDate and {oe:deliveredDate}< ?endDate and {oe:product} is not null");
        RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
        params.put("requisitionStatus", requisitionStatus);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("eydmsCustomer", eydmsCustomer);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0)!=null?searchResult.getResult().get(0):0.0;
        else
            return 0.0;
    }

    @Override
    public Double getActualTargetForSalesYTDRetailerList(EyDmsCustomerModel currentUser, BaseSiteModel currentBaseSite, String startDate, String endDate) {
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
    public Double getActualTargetForSalesRetailerMTDList(EyDmsCustomerModel currentUser, BaseSiteModel currentBaseSite) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select sum({p.quantity}) from {OrderRequisition as p} ")
                .append(" where {p.toCustomer}=?toCustomerList and {p:status}=?status and ").append(EyDmsDateUtility.getMtdClauseQueryRetailer("p:deliveredDate", params));

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
    public Double getActualTargetForSalesYTRetailerList(EyDmsCustomerModel currentUser, BaseSiteModel currentBaseSite, String startDate, String endDate,String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        if (StringUtils.isBlank(bgpFilter)) {
            builder.append("SELECT sum({oe:quantity}) from {OrderRequisition AS oe} WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?eydmsCustomer and {oe:deliveredDate}>=?startDate and {oe:deliveredDate}< ?endDate");
        } else if (bgpFilter.equalsIgnoreCase("ALL")) {
            builder.append("SELECT sum({oe:quantity}) from {OrderRequisition AS oe} WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?eydmsCustomer and {oe:deliveredDate}>=?startDate and {oe:deliveredDate}< ?endDate");
        } else {
            CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
            ProductModel product = productService.getProductForCode(catalogVersion, bgpFilter);
            if (product != null) {
                builder.append("SELECT sum({oe:quantity}) from {OrderRequisition AS oe JOIN Product as p on {oe:product}={p:pk}} WHERE {oe:status}=?requisitionStatus and {oe.toCustomer}=?eydmsCustomer and {oe:deliveredDate}>=?startDate and {oe:deliveredDate}<=?endDate and {oe:product} =?product and {oe:product} is not null");
                params.put("product", product);
            }
        }
        RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
        params.put("requisitionStatus", requisitionStatus);
        params.put("eydmsCustomer", currentUser);
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

            builder.append("SELECT {sc.uid},SUM({oe.quantity}) FROM {EyDmsCustomer AS sc JOIN CustomerSubAreaMapping AS cs ON {sc:pk} = {cs:eydmsCustomer} JOIN SubAreaMaster AS sm ON {sm.pk} = {cs.subAreaMaster} LEFT JOIN OrderRequisition AS oe ON {cs.eydmsCustomer} = {oe.toCustomer} AND {oe.status} = ?requisitionStatus AND {oe.deliveredDate} >=?startDate AND {oe.deliveredDate} <=?endDate } WHERE {cs.brand}=?site AND {cs.isOtherBrand}=?isOtherBrand and {sc.counterType}=?counterType AND {cs.subAreaMaster} =?district GROUP BY {sc.uid} ORDER BY SUM({oe.quantity})");

            RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
            params.put("requisitionStatus", requisitionStatus);
            //params.put("eydmsCustomer", eydmsUser);
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

            //builder.append("SELECT {sc.uid},SUM({oe.quantity}) FROM {EyDmsCustomer AS sc JOIN CustomerSubAreaMapping AS cs ON {sc:pk} = {cs:eydmsCustomer} JOIN SubAreaMaster AS sm ON {sm.pk} = {cs.subAreaMaster} LEFT JOIN OrderRequisition AS oe ON {cs.eydmsCustomer} = {oe.toCustomer} AND {oe.status} = ?requisitionStatus AND {oe.deliveredDate} >=?startDate AND {oe.deliveredDate} <=?endDate } WHERE {cs.brand}=?site AND {cs.isOtherBrand}=?isOtherBrand and {sc.counterType}=?counterType AND {cs.subAreaMaster} =?district GROUP BY {sc.uid} ORDER BY SUM({oe.quantity})");
            builder.append("SELECT {sc.uid}, SUM(CASE WHEN {p.premium} =?premium THEN {oe.quantity} ELSE 0 END) AS totalQuantity FROM {EyDmsCustomer AS sc JOIN CustomerSubAreaMapping AS cs ON {sc:pk} = {cs:eydmsCustomer} JOIN SubAreaMaster AS sm ON {sm.pk} = {cs.subAreaMaster}  LEFT JOIN OrderRequisition AS oe ON {cs.eydmsCustomer} = {oe.toCustomer}  AND {oe.status} =?requisitionStatus AND {oe.deliveredDate} >= ?startDate AND {oe.deliveredDate} <= ?endDate LEFT JOIN Product AS p ON {oe:product} = {p:pk}} WHERE {cs.brand}=?site AND {cs.isOtherBrand}=?isOtherBrand AND {sc.counterType}=?counterType AND {cs.subAreaMaster} =?district GROUP BY {sc.uid} ORDER BY totalQuantity");

            RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
            params.put("requisitionStatus", requisitionStatus);
            //params.put("eydmsCustomer", eydmsUser);
            params.put("district", district);
            params.put("site", currentBaseSite);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            params.put("counterType", CounterType.RETAILER);
            params.put("premium","Y");
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
        query.setResultClassList(Arrays.asList(CounterVisitMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<DistrictMasterModel> searchResult = flexibleSearchService.search(query);
        List<DistrictMasterModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result.get(0) : null;
    }

    @Override
    public List<EyDmsCustomerModel> getSPForDistrict(DistrictMasterModel districtForSP) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT distinct{s.spCode} FROM {CustDepotDealerMapping as d join SpCustDepotMapping as s on {d.custDepotCode}={s.custDepotCode}join CustomerSubAreaMapping as c on {c.eydmsCustomer}={d.dealerCode} join EyDmsCustomer as u on {c.eydmsCustomer}={u.pk} join SubAreaMaster as sm on {sm.pk}={c.subAreaMaster} JOIN DistrictMaster AS dm ON {dm.pk} = {sm.districtMaster}} where {dm.pk}=?districtForSP and  {s.brand}=?brand ");
        params.put("active",Boolean.TRUE);
        params.put("brand",baseSiteService.getCurrentBaseSite());
        params.put("districtForSP",districtForSP);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
        query.addQueryParameters(params);
        final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
        List<EyDmsCustomerModel> result = searchResult.getResult();

        return result!=null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public List<EyDmsUserModel> getTsmByRegion(String district) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("Select distinct {t.tsmUser} from {TsmDistrictMapping as t join DistrictMaster as d on {t.district}={d.pk} join RegionMaster as r on {d.region}={r.pk}} where  {r.pk} in (?district) and {t.brand}=?brand and {t.isActive}=?active");
        params.put("active",Boolean.TRUE);
        params.put("brand",baseSiteService.getCurrentBaseSite());
        params.put("district",district);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsUserModel.class));
        query.addQueryParameters(params);
        final SearchResult<EyDmsUserModel> searchResult = flexibleSearchService.search(query);
        List<EyDmsUserModel> result = searchResult.getResult();

        return result!=null && !result.isEmpty() ? result : Collections.emptyList();

    }

    @Override
    public List<EyDmsUserModel> getRHByState(String district) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("Select {rh.rhUser} from {RhRegionMapping as rh join RegionMaster as r on {rh.region}={r.pk} join StateMaster as s on {r.state}={s.pk}} where {s.code}=?state and {rh:brand} =?brand  and {rh:isActive} =?active");
        params.put("active",Boolean.TRUE);
        params.put("brand",baseSiteService.getCurrentBaseSite());
        params.put("state",district);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsUserModel.class));
        query.addQueryParameters(params);
        final SearchResult<EyDmsUserModel> searchResult = flexibleSearchService.search(query);
        List<EyDmsUserModel> result = searchResult.getResult();

        return result!=null && !result.isEmpty() ? result : Collections.emptyList();

    }



    @Override
    public List<EyDmsCustomerModel> getCustomerForTsm(String district, EyDmsUserModel eydmsUserModel) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("Select distinct {c.eydmsCustomer} from {TsmDistrictMapping as t join DistrictMaster as d on {t.district}={d.pk} join RegionMaster as r on {d.region}={r.pk} join SubAreaMaster as s on {d:name}={s:district} join UserSubAreaMapping as u on {u.subareaMaster}={s.pk} join CustomerSubAreaMapping as c on {c:subAreaMaster}={u:subAreaMaster} join EyDmsCustomer as sc on {c.eydmsCustomer} = {sc.pk} } where {t.brand}=?brand and {t.isActive}=?active and {sc.counterType}=?counterType and {t.tsmUser}=?eydmsUserModel");
        params.put("active",Boolean.TRUE);
        params.put("brand",baseSiteService.getCurrentBaseSite());
        params.put("eydmsUserModel",eydmsUserModel);
        params.put("counterType", CounterType.DEALER);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsUserModel.class));
        query.addQueryParameters(params);
        final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
        List<EyDmsCustomerModel> result = searchResult.getResult();

        return result!=null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public List<EyDmsCustomerModel> getCustomerForRH(String district, EyDmsUserModel eydmsUserModel) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("Select distinct {c.eydmsCustomer} from {RhRegionMapping as rh join RegionMaster as r on {rh.region}={r.pk} join DistrictMaster as d on {d:region}={r:pk} join RegionMaster as r on {d.region}={r.pk} join SubAreaMaster as s on {d:name}={s:district} join UserSubAreaMapping as u on {u.subareaMaster}={s.pk} join CustomerSubAreaMapping as c on {c:subAreaMaster}={u:subAreaMaster} } where  {rh:brand} =?brand and {rh:isActive} =?active and {rh.rhUser}=?eydmsUserModel");
        params.put("active",Boolean.TRUE);
        params.put("brand",baseSiteService.getCurrentBaseSite());
        params.put("eydmsUserModel",eydmsUserModel);
        // params.put("counterType", CounterType.DEALER);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsUserModel.class));
        query.addQueryParameters(params);
        final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
        List<EyDmsCustomerModel> result = searchResult.getResult();

        return result!=null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public List<List<Object>> getSalesDealerByDate(String district, EyDmsUserModel eydmsUserModel,Date startDate, Date endDate) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder();

            LOGGER.info("Getting sales for the Dealer");

            builder.append("Select {tu.uid},sum({oe.quantityInMt}) from {TsmDistrictMapping as t join DistrictMaster as d on {t.district}={d.pk} join RegionMaster as r on {d.region}={r.pk} join SubAreaMaster as s on {d:name}={s:district} join UserSubAreaMapping as u on {u.subareaMaster}={s.pk} join CustomerSubAreaMapping as c on {c:subAreaMaster}={u:subAreaMaster} join EyDmsCustomer as sc on {c.eydmsCustomer} = {sc.pk} join EyDmsUser as tu on {t.tsmUser}={tu.pk} LEFT JOIN Order AS o ON {c.eydmsCustomer} = {o.user} LEFT JOIN OrderEntry AS oe ON {oe.order} = {o.pk} AND {oe.status} =?status AND {oe.deliveredDate} >=?startDate AND {oe.deliveredDate} <= ?endDate} where  {r.pk} in (?district) and {t.brand}=?brand and {t.isActive}=?active and {sc.counterType}=?counterType and {t.tsmUser}=?tsmUser GROUP BY {tu.uid} ORDER BY sum({oe.quantityInMt})");

            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("status", orderStatus);
            params.put("tsmUser", eydmsUserModel);
            params.put("district", district);
            params.put("brand", baseSiteService.getCurrentBaseSite());
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            params.put("counterType", CounterType.DEALER);
            params.put("active",Boolean.TRUE);
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
    public List<List<Object>> getRHSalesDealerByDate(EyDmsUserModel eydmsUserModel,Date startDate, Date endDate) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder();

            LOGGER.info("Getting sales for the Dealer");

            // builder.append("Select {tu.uid},sum({oe.quantityInMt}) from {TsmDistrictMapping as t join DistrictMaster as d on {t.district}={d.pk} join RegionMaster as r on {d.region}={r.pk} join SubAreaMaster as s on {d:name}={s:district} join UserSubAreaMapping as u on {u.subareaMaster}={s.pk} join CustomerSubAreaMapping as c on {c:subAreaMaster}={u:subAreaMaster} join EyDmsCustomer as sc on {c.eydmsCustomer} = {sc.pk} join EyDmsUser as tu on {t.tsmUser}={tu.pk} LEFT JOIN Order AS o ON {c.eydmsCustomer} = {o.user} LEFT JOIN OrderEntry AS oe ON {oe.order} = {o.pk} AND {oe.status} =?status AND {oe.deliveredDate} >=?startDate AND {oe.deliveredDate} <= ?endDate} where  {r.pk} in (?district) and {t.brand}=?brand and {t.isActive}=?active and {sc.counterType}=?counterType and {t.tsmUser}=?tsmUser GROUP BY {tu.uid} ORDER BY sum({oe.quantityInMt})");
            builder.append("Select {tu.uid},sum({oe.quantityInMt}) from {RhRegionMapping as rh join RegionMaster as r on {rh.region}={r.pk} join DistrictMaster as d on {d:region}={r:pk} join RegionMaster as r on {d.region}={r.pk} join SubAreaMaster as s on {d:name}={s:district} join UserSubAreaMapping as u on {u.subareaMaster}={s.pk} join CustomerSubAreaMapping as c on {c:subAreaMaster}={u:subAreaMaster} join EyDmsCustomer as sc on {c.eydmsCustomer} = {sc.pk} join EyDmsUser as tu on {rh.rhUser}={tu.pk} LEFT JOIN Order AS o ON {c.eydmsCustomer} = {o.user} LEFT JOIN OrderEntry AS oe ON {oe.order} = {o.pk} AND {oe.status} =?status AND {oe.deliveredDate} >=?startDate AND {oe.deliveredDate} <= ?endDate} where  {rh:brand} =?brand and {rh:isActive} =?active and {rh.rhUser}=?rhUser GROUP BY {tu.uid} ORDER BY sum({oe.quantityInMt})");
            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("status", orderStatus);
            params.put("rhUser", eydmsUserModel);

            params.put("brand", baseSiteService.getCurrentBaseSite());
            params.put("startDate", startDate);
            params.put("endDate", endDate);

            params.put("active",Boolean.TRUE);
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
    public List<EyDmsCustomerModel> getCustomerForSp(EyDmsCustomerModel eydmsCustomer) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT distinct{d.dealerCode} FROM {CustDepotDealerMapping as d join SpCustDepotMapping as s on {d.custDepotCode}={s.custDepotCode}} where {s.spCode}=?eydmsCustomer");
        params.put("active",Boolean.TRUE);
        params.put("brand",baseSiteService.getCurrentBaseSite());
        params.put("eydmsCustomer",eydmsCustomer);
        params.put("counterType", CounterType.DEALER);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsUserModel.class));
        query.addQueryParameters(params);
        final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
        List<EyDmsCustomerModel> result = searchResult.getResult();

        return result!=null && !result.isEmpty() ? result : Collections.emptyList();

    }

    @Override
    public Double getActualTargetForSalesSPMTDSearch(List<EyDmsCustomerModel> eydmsCustomer) {
        {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder();

            builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}}  WHERE {o:user} in (?eydmsCustomer)  and   "
                    + " {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and ").append(EyDmsDateUtility.getMtdClauseQuery("oe:invoiceCreationDateAndTime", params));

            OrderStatus orderStatus=OrderStatus.DELIVERED;
            params.put("eydmsCustomer",eydmsCustomer);
            params.put("orderStatus",orderStatus);
            params.put("site",baseSiteService.getCurrentBaseSite());
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.setResultClassList(Arrays.asList(Double.class));
            query.addQueryParameters(params);
            final SearchResult<Double> searchResult = flexibleSearchService.search(query);
            if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty())) {
                return searchResult.getResult().get(0)!=null?searchResult.getResult().get(0):0.0;
            }
            else{
                return 0.0;
            }
        }
    }

    @Override
    public List<List<Object>> getWeeklyOverallPerformance(String bgpFilter) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder();

            if(bgpFilter!=null)
            {
                CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
                ProductModel product = productService.getProductForCode(catalogVersion, bgpFilter);
                builder.append("select top 9 DATEPART(week, {p:deliveryDate}),DATEPART(year, {p:deliveryDate}) , sum({p.quantity}) from {PointRequisition as p} where {p:status} = ?status and {p:product} = ?product group by DATEPART(week, {p:deliveryDate}),DATEPART(year, {p:deliveryDate}) order by DATEPART(year, {p:deliveryDate}) desc, DATEPART(week, {p:deliveryDate}) desc");
                params.put("product",product);
            }
            else {
                builder.append("select top 9 DATEPART(week, {p:deliveryDate}),DATEPART(year, {p:deliveryDate}) , sum({p.quantity}) from {PointRequisition as p} where {p:status} = ?status group by DATEPART(week, {p:deliveryDate}),DATEPART(year, {p:deliveryDate}) order by DATEPART(year, {p:deliveryDate}) desc, DATEPART(week, {p:deliveryDate}) desc");
            }
            PointRequisitionStatus status = PointRequisitionStatus.APPROVED;
            params.put("status", status);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Integer.class, Integer.class, Double.class));
            LOG.info("Query for weekly::"+query);
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

            if(bgpFilter!=null)
            {
                CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
                ProductModel product = productService.getProductForCode(catalogVersion, bgpFilter);
                builder.append("select top 6 DATEPART(month, {p:deliveryDate}),DATEPART(year, {p:deliveryDate}) , sum({p.quantity}) from {PointRequisition as p} where {p:status} = ?status and {p:product}= ?product group by DATEPART(month, {p:deliveryDate}),DATEPART(year, {p:deliveryDate}) order by DATEPART(year, {p:deliveryDate}) desc, DATEPART(month, {p:deliveryDate}) desc");
                params.put("product",product);
            }
            else {
                builder.append("select top 6 DATEPART(month, {p:deliveryDate}),DATEPART(year, {p:deliveryDate}) , sum({p.quantity}) from {PointRequisition as p} where {p:status} = ?status group by DATEPART(month, {p:deliveryDate}),DATEPART(year, {p:deliveryDate}) order by DATEPART(year, {p:deliveryDate}) desc, DATEPART(month, {p:deliveryDate}) desc");
            }
            PointRequisitionStatus status = PointRequisitionStatus.APPROVED;
            params.put("status", status);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Integer.class, Integer.class, Double.class));
            LOG.info("Query for monthly::"+query);
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return result != null && !result.isEmpty() ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }
}