package com.scl.core.dao.impl;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.SalesPlanningDao;
import com.scl.core.enums.CustomerCategory;
import com.scl.core.model.*;
import com.scl.core.services.TerritoryManagementService;
import com.scl.core.utility.SclDateUtility;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.enums.ArticleApprovalStatus;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;

class SalesPlanningDaoImpl implements SalesPlanningDao {

    private static final Logger LOG = Logger.getLogger(SalesPlanningDaoImpl.class);
    @Resource
    PaginatedFlexibleSearchService paginatedFlexibleSearchService;

    @Resource
    BaseSiteService baseSiteService;
    @Autowired
    SearchRestrictionService searchRestrictionService;
    @Autowired
    SessionService sessionService;

    //SELECT {ann:pk} FROM {AnnualSales as ann JOIN SubAreaList as sub on {sub.annualSales}={ann:PK}} where {salesOfficer}=8796095152132
    private static final String ANNUAL_PLANNED_SALES_VIEW_QUERY = "SELECT {ann:pk} FROM {AnnualSales as ann} where {ann:salesOfficer}=?sclUser and {ann:subAreaMaster}=?subArea and {ann:brand}=?brand";
    private static final String ANNUAL_SALES_VIEW_QUERY = "SELECT {ann:pk} FROM {AnnualSales as ann} where {ann:salesOfficer}=?sclUser and {ann:subAreaMaster}=?subArea and {ann:financialYear}=?financialYear and {ann:brand}=?brand";
    private static final String ANNUAL_SALES_VIEW_QUERY_FORSH = "SELECT {ann:pk} FROM {AnnualSales as ann} where  {ann:subAreaMaster} in (?subArea) and {ann:financialYear}=?financialYear and {ann:brand}=?brand";
    private static final String ANNUAL_SALES_VIEW_QUERY_FORSH_RH = "SELECT {ann:pk} FROM {AnnualSales as ann} where {ann:financialYear}=?financialYear and {ann:brand}=?brand and {ann:districtMaster} in (?districtMaster)";
    private static final String MONTHLY_PLANNED_SALES_VIEW_QUERY = "SELECT {pk} FROM {MonthlySales as ms} WHERE {ms:so}=?sclUser and {ms:subAreaMaster}=?subArea and {ms:brand}=?brand";
    private static final String MONTHLY_PLANNED_SALES_VIEW_QUERY_FORTSMRH = "SELECT {pk} FROM {MonthlySales as ms} WHERE {ms:subAreaMaster}=?subArea and {ms:brand}=?brand";
    private static final String MONTHLY_SALES_VIEW_QUERY = "SELECT {ms:pk} FROM {MonthlySales as ms} WHERE {ms:so}=?sclUser and {ms:monthName}=?month and {ms:monthYear}=?year and {ms:subAreaMaster}=?subArea and {ms:brand}=?brand";
    private static final String MONTHLY_SALES_VIEW_QUERY_FORSH = "SELECT {ms:pk} FROM {MonthlySales as ms} WHERE {ms:monthName}=?month and {ms:monthYear}=?year and {ms:subAreaMaster} in (?subArea) and {ms:brand}=?brand";
    private static final String MONTHLY_SALES_VIEW_QUERY_FORTSMRH = "SELECT {ms:pk} FROM {MonthlySales as ms} WHERE {ms:monthName}=?month and {ms:monthYear}=?year and {ms:subAreaMaster}=?subArea and {ms:brand}=?brand";
    private static final String PRODUCT_MIX_PERCENTAGE =
            "SELECT {p:code},{p:name},sum({oe:quantityInMT}) from " +
                    "{OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN " +
                    "Product as p on {oe:product}={p:pk} JOIN " +
                    "SclCustomer as sc on {sc:pk}={o:user}} " +
                    "where {o:placedBy}= ?sclUser and " +
                    "{oe:status}=?orderStatus and " +
                    "{o:site} = ?baseSite " +
                    "and {oe:deliveredDate}>=?startDate and {oe:deliveredDate}<=?endDate and" +
                    "{o:subAreaMaster}=?subArea" +
                    " group by {p:code},{p:name}";
    private static final String DEALERCATEGORY_TARGET_LAST_YEAR_SHARE=
            "SELECT {d.code},sum({oe:quantityInMT}) from " +
            "{OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN " +
            "SclCustomer as sc on {sc:pk}={o:user} JOIN " +
            "DealerCategory AS d ON {d.pk}={sc.dealerCategory}} " +
            "where {o:placedBy}= ?sclUser and " +
            "{oe:status}=?orderStatus and " +
            "{o:site}=?baseSite and " +
            "{o.date}>=?startDate and {o.date}<=?endDate and" +
             "{o:subAreaMaster}=?subArea" +
            " group by {d.code}";
    private static final String DEALER_TARGET_LAST_YEAR_SHARE=
            "SELECT DISTINCT{sc.uid} from " +
                    "{OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN " +
                    "Product as p on {oe:product}={p:pk} JOIN " +
                    "SclCustomer as sc on {sc:pk}={o:user} JOIN " +
                    "EnumerationValue AS enum ON {enum.pk}={sc.DealerCategory}} " +
                    "where {o:placedBy}=?sclUser and " +
                    "{oe:status}=?orderStatus and " +
                    "{o:site}=?baseSite and " +
                    "{o.date}>=?startDate and {o.date}<=?endDate and " +
                    "{enum.code}=?dealerCategory";
    private static final String MONTHWISE_QUANTITY_FOR_DEALER =
            "SELECT MONTH({oe.invoiceCreationDateAndTime}), YEAR({oe.invoiceCreationDateAndTime}), sum({oe.quantityInMT}) from {OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {sc:pk}={o:user}} where {o:placedBy}=?sclUser and {o:site}=?baseSite and {sc.uid}=?sclCustomer and {oe.invoiceCreationDateAndTime}>=?startDate and {oe.invoiceCreationDateAndTime} < ?endDate and {o:subAreaMaster}=?subArea and {oe.cancelledDate} is null group by MONTH({oe.invoiceCreationDateAndTime}), YEAR({oe.invoiceCreationDateAndTime})";

    private static final String MONTHWISE_QUANTITY_FOR_RETAILER = "SELECT MONTH({oe.deliveredDate}), YEAR({oe.deliveredDate}), sum({oe.quantityInMT}) from" +
            "{OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN " +
            "SclCustomer as sc on {sc:pk}={o:user} JOIN " +
            "SclCustomer as s on {s:pk}={o:retailer}} " +
            "where {o:placedBy}=?sclUser and " +
            "{oe:status}=?orderStatus and " +
            "{o:site} =?baseSite and " +
            "{s.uid}=?sclCustomer and " +
            "{oe.deliveredDate}>=?startDate and {oe.deliveredDate}<=?endDate and {o:subAreaMaster}=?subArea " +
            "group by MONTH({oe.deliveredDate}),YEAR({oe.deliveredDate})";

    private static final String NEW_ONBOARDED_DEALER_VIEW_QUERY=
            "SELECT {sc:pk} from {SclCustomer as sc JOIN CustomerSubAreaMapping as cs on {sc:pk}={cs:sclCustomer} JOIN PrincipalGroupRelation as p on {p:source}={sc:pk} JOIN UserGroup as u on {u:pk}={p:target}} where {cs:subAreaMaster}=?subArea and {sc:dateOfJoining}>=?startDate and {sc:dateOfJoining}<=?endDate and {u:uid}=?group and DAY({sc:dateOfJoining})<=?intervalPeriod";

    private static final String NEW_ONBOARDED_DEALER_VIEW_QUERY_WITH_FILTER=
            "SELECT {sc:pk} from {SclCustomer as sc JOIN CustomerSubAreaMapping as cs on {sc:pk}={cs:sclCustomer} JOIN PrincipalGroupRelation as p on {p:source}={sc:pk} JOIN UserGroup as u on {u:pk}={p:target}} where {cs:subAreaMaster}=?subArea and {sc:dateOfJoining}>=?startDate and {sc:dateOfJoining}<=?endDate and {u:uid}=?group and UPPER({sc:uid}) like ?filter and DAY({sc:dateOfJoining})<=?intervalPeriod";
    private static final String NEW_ONBOARDED_RETAILER_VIEW_QUERY=
            "SELECT {sc:pk} from {SclCustomer as sc JOIN CustomerSubAreaMapping as cs on {sc:pk}={cs:sclCustomer} JOIN PrincipalGroupRelation as p on {p:source}={sc:pk} JOIN UserGroup as u on {u:pk}={p:target}} where {cs:subAreaMaster}=?subArea and {sc:dateOfJoining}>=?startDate and {sc:dateOfJoining}<=?endDate and {u:uid}=?group and DAY({sc:dateOfJoining})<=?intervalPeriod";

    private static final String NEW_ONBOARDED_RETAILER_VIEW_QUERY_WITH_FILTER=
            "SELECT {sc:pk} from {SclCustomer as sc JOIN CustomerSubAreaMapping as cs on {sc:pk}={cs:sclCustomer} JOIN PrincipalGroupRelation as p on {p:source}={sc:pk} JOIN UserGroup as u on {u:pk}={p:target}} where {cs:subArea}=?subArea and {sc:dateOfJoining}>=?startDate and {sc:dateOfJoining}<=?endDate and {u:uid}=?group and UPPER({sc:uid}) like ?filter and DAY({sc:dateOfJoining})<=?intervalPeriod";

    private static final String  DEALER_DETAILS="SELECT distinct{sc:uid},{sc:name},{sc:counterPotential} from {OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}} where {o:placedBy}=?sclUser and {o:site}=?baseSite and {o:subAreaMaster}=?subArea and {oe:invoiceCreationDateAndTime}>=?startDate and {oe:invoiceCreationDateAndTime} < ?endDate and {oe.cancelledDate} is null";
    private static final String  DEALER_DETAILS_FOR_RETAILER="SELECT distinct{sc:uid},{sc:name},{sc:counterPotential} from {OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}} where {o:placedBy}=?sclUser and {oe:status}=?orderStatus and {o:site}=?baseSite and {o:subAreaMaster}=?subArea and {oe:deliveredDate}>=?startDate and {oe:deliveredDate}<=?endDate";
    private static final String  DEALER_LAST_FY_CY_SALES= "SELECT sum({oe:quantityInMT}),{p:code},{p:name},{sc:uid},{p:premium} from {OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN Product as p on {oe:product}={p:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}} where {o:placedBy}=?sclUser and {o:site}=?baseSite and {o:subAreaMaster}=?subArea and {oe:invoiceCreationDateAndTime}>=?startDate and {oe:invoiceCreationDateAndTime} < ?endDate and {sc:uid}=?customerCode and {oe.cancelledDate} is null group by {p:code},{p:name},{sc:uid},{p:premium}";
    private static final String  DEALER_CY_SALES_FOR_RETAILER= "SELECT sum({oe:quantityInMT}) from {OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN Product as p on {oe:product}={p:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}} where {o:placedBy}=?sclUser and {o:site}=?baseSite and {o:subAreaMaster}=?subArea and {oe:invoiceCreationDateAndTime}>=?startDate and {oe:invoiceCreationDateAndTime}<=?endDate and {sc:uid}=?customerCode and {oe.cancelledDate} is null";

    private static final String  DEALER_DETAILS_WITH_FILTER="SELECT distinct{sc:uid},{sc:name},{sc:counterPotential} from {OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}} where {o:placedBy}=?sclUser and {o:site}=?baseSite and {o:subAreaMaster}=?subArea and {oe:invoiceCreationDateAndTime}>=?startDate and {oe:invoiceCreationDateAndTime} < ?endDate and {sc:uid} =?filter and {oe.cancelledDate} is null";

    private static final String  Last_Year_Retailer_Sales="SELECT {s:uid}, {s:name}, {s:counterPotential}, sum({oe:quantityInMT}),{sc:uid} from {OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk} JOIN SclCustomer as s on {o:retailer}={s:pk}} where {o:placedBy}=?sclUser and {oe:status}=?orderStatus and {o:site} =?baseSite and {o:subAreaMaster}=?subArea and {oe.deliveredDate}>=?startDate and {oe.deliveredDate}<=?endDate and {sc:uid}=?customerCode group by {s:uid},{s:name},{s:counterPotential},{sc:uid}";

    private static final String DEALER_REVISED_ANNUAL_SALES_VIEW_QUERY =
            "SELECT {pk} FROM {DealerRevisedAnnualSales as drs " +
                    "JOIN AnnualSales as ann on {ann.pk}={drs.annualSales}} " +
                    "WHERE {ann.subarea} IN (?subArea) and {ann:salesOfficer}=?sclUser";

    private static final String GET_DEALER_REVISED_ANNUAL_SALES_QUERY =
            "SELECT {pk} FROM {DealerRevisedAnnualSales as drs} " +
                    "WHERE {drs:customerCode}=?dealerCode ";

    private static final String VIEW_REVISED_MONTHLYSALES_REVIEWTAB =
            "SELECT {sc:uid},{sc:name},sum({oe:quantityInMT}),{oe:deliveredDate}" +
                    " from {OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk}" +
                    " JOIN SclCustomer as sc on {o:user}={sc:pk}} " +
                    " where {o:placedBy}=?=sclUser and " +
                    " {oe:status}=?orderStatus and " +
                    " {o:site}=?baseSite and " +
                    " {o:subAreaMaster}=?subArea and " +
                    " {oe:deliveredDate}>=?firstDayOfMonth and " +
                    " {oe:deliveredDate}<=?lastDayOfMonth and " +
                    " {sc.uid}=?dealerCode" +
                    " group by {sc:uid},{sc:name},{oe:deliveredDate}";

    private static final String DEALER_DETAILS_FOR_SELECTED_RETAILER="SELECT distinct{sc:uid}, sum({oe.quantityInMT}) from {OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk} JOIN SclCustomer as s on {o:retailer}={s:pk}} where {o:placedBy}=?sclUser and {o:site} =?baseSite and {oe:status}=?orderStatus and {o:subAreaMaster}=?subArea and {oe.deliveredDate}>=?startDate and {oe.deliveredDate}<=?endDate and UPPER({s:uid}) like ?filter group by {sc:uid}";
    private static final String RETAILER_DETAILS_FOR_SELECTED_DEALER="SELECT distinct{s:uid}, sum({oe.quantityInMT}) from {OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk} JOIN SclCustomer as s on {o:retailer}={s:pk}} where {o:placedBy}=?sclUser and {o:site} =?baseSite and {oe:status}=?orderStatus and {o:subAreaMaster}=?subArea and {oe.deliveredDate}>=?startDate and {oe.deliveredDate}<=?endDate and UPPER({sc:uid}) like ?filter group by {s:uid}";
    private static final String DEALER_PLANNED_ANNUAL_SALES="select {ds:pk} from {DealerPlannedAnnualSales as ds JOIN AnnualSales as ann on {ds:annualSales}={ann.pk}} where {ds:subAreaMaster}=?subArea and {ann:salesOfficer}=?sclUser and UPPER({ds:customerCode}) like ?filter";
    private static final String DEALER_REVISED_ANNUAL_SALES="select {ds:pk} from {DealerRevisedAnnualSales as ds JOIN AnnualSales as ann on {ds:annualSales}={ann.pk}} where {ds:subarea}=?subArea and {ann:salesOfficer}=?sclUser and {ann:financialYear}=?financialYear";
    private static final String MONTH_WISE_ANNUAL_TARGET_DETAILS="select {pk} from {MonthWiseAnnualTarget} where {customerCode}=?customerCode and {productCode}=?productCode and {subAreaMaster}=?subArea and {isAnnualSalesRevisedForDealer}=?isAnnualSalesRevisedForDealer";
    private static final String CURRENT_MONTH_SALE_MONTHLY_SUMMARY="SELECT sum({oe:quantityInMT}) from {OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}} where {o:placedBy}=?sclUser and {oe:status}=?orderStatus and {o:site}=?baseSite and {o:subAreaMaster}=?subArea and {o:date}>=?startDate and {o:date}<=?endDate";
    private static final String CURRENT_YEAR_SALES_ANNUAL_SUMMARY = "SELECT sum({oe:quantityInMT}) from {OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}} where {o:placedBy}=?sclUser and {o:site}=?baseSite and {o:subAreaMaster}=?subArea and {oe:invoiceCreationDateAndTime}>=?startDate and {oe.invoiceCreationDateAndTime} < ?endDate and {oe.cancelledDate} is null";
    private static final String CURRENT_YEAR_SALES_ANNUAL_SUMMARY_NEW = "SELECT sum({oe:quantityInMT}) from {OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}} where {o:site}=?baseSite and {o:subAreaMaster}=?subArea and {oe:invoiceCreationDateAndTime}>=?startDate and {oe.invoiceCreationDateAndTime} < ?endDate and {oe.cancelledDate} is null";
    private static final String DEALER_PLANNED_ANNUAL_SALES_DETAILS="select {ds:pk} from {DealerPlannedAnnualSales as ds JOIN AnnualSales as ann on {ds:annualSales}={ann.pk}} where {ds:subAreaMaster}=?subArea and {ann:salesOfficer}=?sclUser and {ann:financialYear}=?financialYear and {ds:customerCode}=?customerCode";
    private static final String RETAILER_PLANNED_ANNUAL_SALES_DETAILS = "select {rs:pk} from {RetailerPlannedAnnualSales as rs JOIN AnnualSales as ann on {rs:annualSale}={ann.pk}} where {rs:subAreaMaster}=?subArea and {ann:salesOfficer}=?sclUser and {ann:financialYear}=?financialYear and {rs:customerCode}=?customerCode";
    private static final String MONTH_WISE_ANNUAL_SALES_TARGET_DETAILS ="select {pk} from {MonthWiseAnnualTarget} where {customerCode}=?customerCode and {subAreaMaster}=?subArea and {monthYear}=?monthYear and {salesOfficer}=?sclUser and {productCode} is null";
    private static final String MONTH_WISE_DETAILE_OF_DEALER_FOR_RETAILER ="select {pk} from {MonthWiseAnnualTarget} where {customerCode}=?customerCode and {subAreaMaster}=?subArea and {monthYear}=?monthYear and {salesOfficer}=?sclUser  and {productCode} is null and {retailerCode} is null and {selfCounterCustomerCode} is null and {isAnnualSalesRevisedForRetailer}=?isAnnualSalesRevisedForRetailer";

    private static final String LAST_YEAR_PRODUCT_SHARE_NCR_ANNUAL = "select {p:code},{p:name}, sum({s:quantity}) from {SalesHistory as s JOIN Product as p on {p:state}={s:state} and {p:packagingCondition}={s:packagingCondition} and {p:inventoryId}={s:inventoryItemId} join Catalog as c on {c.pk}={p.catalog} join CatalogVersion as cv on {cv.pk}={p.catalogversion} JOIN CustomerSubAreaMapping as u on {s:state}={u:state}} where {cv.version}=?catalogVersion and {c.id}=?catalogId and {s:invoiceDate} >=?startDate and {s:invoiceDate}<=?endDate and {u:subAreaMaster}=?subArea and {s:customerCategory}=?customerCategory and {s:inventoryItemId} is not null  group by {p:code},{p:name}";
    private static final String LAST_YEAR_DEALER_SHARE_NCR_ANNUAL="select {d:code},sum({s:quantity}) from {SalesHistory as s JOIN SclCustomer as sc on {s:customerNo}={sc:customerNo} JOIN DealerCategory as d on {sc:dealerCategory}={d:pk} JOIN  CustomerSubAreaMapping as u on {u:state}={s:state}} where {s:invoiceDate}>=?startDate and {s:invoiceDate}<=?endDate and {u:subAreaMaster}=?subArea group by {d:code}";
    private static final String DEALER_REVISED_ANNUAL_SALES_DETAILS="select {ds:pk} from {DealerRevisedAnnualSales as ds JOIN AnnualSales as ann on {ds:annualSales}={ann.pk}} where {ds:subAreaMaster}=?subArea and {ann:salesOfficer}=?sclUser and {ann:financialYear}=?financialYear and {ds:customerCode}=?customerCode";
    private static final String RETAILER_REVISED_ANNUAL_SALES_DETAILS = "select {rs:pk} from {RetailerRevisedAnnualSales as rs JOIN AnnualSales as ann on {rs:annualSales}={ann.pk}} where {rs:subAreaMaster}=?subArea and {ann:salesOfficer}=?sclUser and {ann:financialYear}=?financialYear and {rs:customerCode}=?customerCode and {rs:isAnnualSalesRevisedForRetailer}=?isAnnualSalesRevisedForRetailer";
    private static final String RETAILER_PLANNED_ANNUAL_SALES="select {rs:pk} from {RetailerPlannedAnnualSales as rs JOIN AnnualSales as ann on {rs:annualSale}={ann.pk}} where {rs:subAreaMaster}=?subArea and {ann:salesOfficer}=?sclUser and UPPER({rs:customerCode}) like ?filter";

    @Resource
    private FlexibleSearchService flexibleSearchService;

    @Resource
    TerritoryManagementService territoryManagementService;

    private static final String PRODUCT_MIX_PERCENTAGE_MONTHLY =
            "SELECT {p:code},{p:name},sum({oe:quantityInMT}) from " +
                    "{OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN " +
                    "Product as p on {oe:product}={p:pk} JOIN " +
                    "SclCustomer as sc on {sc:pk}={o:user}} " +
                    "where {o:placedBy}= ?sclUser and " +
                    "{oe:status}=?orderStatus and " +
                    "{o:site} = ?baseSite " +
                    "and {o:date}>=?firstDayOfMonth and {o:date}<=?lastDayOfMonth" +
                    " group by {p:code},{p:name}";
    private static final String DEALERCATEGORY_TARGET_LAST_YEAR_SHARE_MONTHLY=
            "SELECT {enum.code},sum({oe:quantityInMT}) from " +
                    "{OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN " +
                    "Product as p on {oe:product}={p:pk} JOIN " +
                    "SclCustomer as sc on {sc:pk}={o:user} JOIN " +
                    "EnumerationValue AS enum ON {enum.pk}={sc.DealerCategory}} " +
                    "where {o:placedBy}= ?sclUser and " +
                    "{oe:status}=?orderStatus and " +
                    "{o:site}=?baseSite and " +
                    "{o.date}>=?firstDayOfMonth and {o.date}<=?lastDayOfMonth" +
                    " group by {enum.code}";

    private static final String DEALER_TARGET_LAST_YEAR_SHARE_MONTHLY=
            "SELECT DISTINCT{u.uid} from " +
                    "{OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN " +
                    "Product as p on {oe:product}={p:pk} JOIN " +
                    "SclCustomer as sc on {sc:pk}={o:user} JOIN " +
                    "EnumerationValue AS enum ON {enum.pk}={sc.DealerCategory}} " +
                    "where {o:placedBy}=?sclUser and " +
                    "{oe:status}=?orderStatus and " +
                    "{o:site}=?baseSite and " +
                    "{o.date}>=?firstDayOfMonth and {o.date}<=?lastDayOfMonth and " +
                    "{enum.code}=?dealerCategory";

    private static final String MONTHWISE_QUANTITY_FOR_DEALER_OF_RETAILER="SELECT MONTH({oe.deliveredDate}), YEAR({oe.deliveredDate}), sum({oe.quantityInMT}) from {OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {sc:pk}={o:user} JOIN SclCustomer as s on {sc:pk}={o:retailer}} where {o:placedBy}=?sclUser and {oe:status}=?orderStatus and {o:site}=?baseSite and {sc.uid}=?sclCustomer and {oe.deliveredDate}>=?startDate and {oe.deliveredDate}<=?endDate and {o:subAreaMaster}=?subArea group by MONTH({oe.deliveredDate}),YEAR({oe.deliveredDate})";
    @Override
    public AnnualSalesModel viewPlannedSalesforDealersRetailersMonthwise(String subArea, SclUserModel sclUser, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(ANNUAL_PLANNED_SALES_VIEW_QUERY);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("sclUser",sclUser);
        params.put("brand",brand);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(AnnualSalesModel.class));
        final SearchResult<AnnualSalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public AnnualSalesModel getAnnualSalesModelDetails(SclUserModel sclUser, String financialYear, String subArea, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(ANNUAL_SALES_VIEW_QUERY);
        params.put("sclUser",sclUser);
        params.put("financialYear",financialYear);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("brand",brand);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(AnnualSalesModel.class));
        final SearchResult<AnnualSalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public AnnualSalesModel getAnnualSalesModelDetailsForSH(String financialYear, List<SubAreaMasterModel> subArea, BaseSiteModel baseSite) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(ANNUAL_SALES_VIEW_QUERY_FORSH);
        params.put("financialYear",financialYear);
        params.put("subArea",subArea);
        params.put("brand",baseSite);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(AnnualSalesModel.class));
        final SearchResult<AnnualSalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public AnnualSalesModel getAnnualSalesModelDetailsForSH_RH(String financialYear, List<DistrictMasterModel> districtMasterModels, BaseSiteModel baseSite) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(ANNUAL_SALES_VIEW_QUERY_FORSH_RH);
        params.put("financialYear",financialYear);
        params.put("districtMaster",districtMasterModels);
        params.put("brand",baseSite);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(AnnualSalesModel.class));
        final SearchResult<AnnualSalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public List<List<Object>> getLastYearShareForProduct(SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate, String subArea) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
             final StringBuilder builder = new StringBuilder(PRODUCT_MIX_PERCENTAGE);
            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("sclUser", sclUser);
            params.put("baseSite", baseSite);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            params.put("orderStatus", orderStatus);
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
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
    public List<List<Object>> getLastYearShareForTarget(SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate, String subArea) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder(DEALERCATEGORY_TARGET_LAST_YEAR_SHARE);
            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("sclUser", sclUser);
            params.put("baseSite", baseSite);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            params.put("orderStatus", orderStatus);
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
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

    @Override
    public List<String> getLastYearShareForDealerTarget(String dealerCategory,SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder(DEALER_TARGET_LAST_YEAR_SHARE);
            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("sclUser", sclUser);
            params.put("baseSite", baseSite);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            params.put("orderStatus", orderStatus);
            params.put("dealerCategory",dealerCategory);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class));
            final SearchResult<String> searchResult = flexibleSearchService.search(query);
            List<String> result = searchResult.getResult();
            return result!=null && !result.isEmpty() ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> getMonthSplitupForDealerPlannedAnnualSales(String sclCustomer, SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate, String subArea) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder(MONTHWISE_QUANTITY_FOR_DEALER);
            OrderStatus orderStatus = OrderStatus.DELIVERED;
            //need to remove scluser - placed by
            params.put("sclUser", sclUser);
            params.put("baseSite", baseSite);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            params.put("orderStatus", orderStatus);
            params.put("sclCustomer",sclCustomer);
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class,String.class,Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    public List<List<Object>> getMonthSplitupForRetailerPlannedAnnualSales(String sclCustomer, SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate, String subArea) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder(MONTHWISE_QUANTITY_FOR_RETAILER);
            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("sclUser", sclUser);
            params.put("baseSite", baseSite);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            params.put("orderStatus", orderStatus);
            params.put("sclCustomer",sclCustomer);
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class,String.class,Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }


    public List<SclCustomerModel> getDealerDetailsForOnboarded(String subArea,Date startDate,Date endDate,int intervalPeriod){
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(NEW_ONBOARDED_DEALER_VIEW_QUERY);
        String group= SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID;
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("startDate",startDate);
        params.put("endDate",endDate);
        params.put("intervalPeriod",intervalPeriod);
        params.put("group",group);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(SclCustomerModel.class));
        final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
        List<SclCustomerModel> result = searchResult.getResult();
        return result!=null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public List<SclCustomerModel> getDealerDetailsForOnboarded(String subArea, Date startDate, Date endDate, int intervalPeriod, String filter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(NEW_ONBOARDED_DEALER_VIEW_QUERY_WITH_FILTER);
        String group= SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID;
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("startDate",startDate);
        params.put("endDate",endDate);
        params.put("intervalPeriod",intervalPeriod);
        params.put("filter","%"+filter.toUpperCase()+"%");
        params.put("group",group);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(SclCustomerModel.class));
        final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
        List<SclCustomerModel> result = searchResult.getResult();
        return result!=null && !result.isEmpty() ? result : Collections.emptyList();

    }

    @Override
    public List<SclCustomerModel> getRetailerDetailsForOnboarded(String subArea, Date startDate, Date endDate, int intervalPeriod) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(NEW_ONBOARDED_RETAILER_VIEW_QUERY);
        String group= SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID;
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("intervalPeriod", intervalPeriod);
        params.put("group",group);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(SclCustomerModel.class));
        final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
        List<SclCustomerModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }


    @Override
    public List<SclCustomerModel> getRetailerDetailsForOnboarded(String subArea, Date startDate, Date endDate, int intervalPeriod, String filter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(NEW_ONBOARDED_RETAILER_VIEW_QUERY_WITH_FILTER);
        String group= SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID;
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("intervalPeriod", intervalPeriod);
        params.put("filter","%"+filter.toUpperCase()+"%");
        params.put("group",group);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(SclCustomerModel.class));
        final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
        List<SclCustomerModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public List<List<Object>> viewDealerDetailsForAnnualSales(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder(DEALER_DETAILS);
            // new changes for territory
            /*final StringBuilder builder = new StringBuilder("SELECT distinct{sc:uid},{sc:name},{sc:counterPotential} from {SalesSummary as s JOIN SclCustomer as sc on {s:customerNo}={sc:customerNo}} where " +
                    "({s:month} >=?startMonth and {s:year} =?startYear) or ({s:month} <=?endMonth and {s:year} =?endYear) and {s:territoryMaster}=?territoryMaster");
            params.put("startMonth", startMonth);
            params.put("endMonth", endMonth);
            params.put("startYear", startYear);
            params.put("endYear", endYear);
            params.put("territoryMaster", territoryMasterService.getTerritoryById(territory));
             */
            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            //need to remove scluser - placed by
            params.put("sclUser", sclUser);
            params.put("baseSite", baseSite);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            params.put("orderStatus", orderStatus);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class,String.class,Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> fetchDealerCySalesForAnnualSales(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate, String customerCode) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder(DEALER_LAST_FY_CY_SALES);
            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            //need to remove scl user - placedBy
            params.put("sclUser", sclUser);
            params.put("baseSite", baseSite);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            params.put("orderStatus", orderStatus);
            params.put("customerCode", customerCode);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class,String.class,String.class,String.class,String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> viewDealerDetailsForAnnualSales(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate, String filter) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder(DEALER_DETAILS_WITH_FILTER);
            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            //sclUser to be removed from query - placed by
            params.put("sclUser", sclUser);
            params.put("baseSite", baseSite);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            params.put("orderStatus", orderStatus);
            params.put("filter",filter);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class,String.class,Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> viewRetailerDetailsForAnnualSales(String customerCode, String subArea, SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder(Last_Year_Retailer_Sales);
            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            params.put("sclUser", sclUser);
            params.put("baseSite", baseSite);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            params.put("orderStatus", orderStatus);
            params.put("customerCode",customerCode);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class,String.class, Double.class, Double.class,String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> viewMonthWiseDealerDetailsForAnnualSales(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate) {
        return Collections.emptyList();
    }

    @Override
    public List<List<Object>> viewMonthWiseRetailerDetailsForAnnualSales(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate) {
        return Collections.emptyList();
    }

    @Override
    public List<List<Object>> reviewMonthWiseSalesWithOnboardedDealers(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate) {
        return Collections.emptyList();
    }

    @Override
    public DealerRevisedAnnualSalesModel viewMonthlySalesTargetForDealers(String subArea,SclUserModel sclUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(DEALER_REVISED_ANNUAL_SALES_VIEW_QUERY);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("sclUser",sclUser);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(DealerRevisedAnnualSalesModel.class));
        final SearchResult<DealerRevisedAnnualSalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public DealerRevisedAnnualSalesModel getDealerRevisedAnnualDetailsForMonthlySales(String dealerCode)
    {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(GET_DEALER_REVISED_ANNUAL_SALES_QUERY);
        params.put("dealerCode", dealerCode);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(DealerRevisedAnnualSalesModel.class));
        final SearchResult<DealerRevisedAnnualSalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public MonthlySalesModel viewMonthlySalesTargetForPlannedTab(String subArea,SclUserModel sclUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        if(sclUser!=null){
            if(sclUser.getUserType().getCode()!=null) {
                if (sclUser.getUserType().getCode().equalsIgnoreCase("SO")) {
                    builder.append(MONTHLY_PLANNED_SALES_VIEW_QUERY);
                    params.put("sclUser", sclUser);
                } else if (sclUser.getUserType().getCode().equals("RH") || sclUser.getUserType().getCode().equalsIgnoreCase("TSM")) {
                    builder.append(MONTHLY_PLANNED_SALES_VIEW_QUERY_FORTSMRH);
                }
            }
        }
        else{
            builder.append(MONTHLY_PLANNED_SALES_VIEW_QUERY_FORTSMRH);
        }

        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("brand", baseSiteService.getCurrentBaseSite());
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(MonthlySalesModel.class));
        final SearchResult<MonthlySalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public MonthlySalesModel getMonthlySalesModelDetail(SclUserModel sclUser, String month, String year, String subArea, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        if(sclUser!=null){
            if(sclUser.getUserType().getCode()!=null) {
                if (sclUser.getUserType().getCode().equalsIgnoreCase("SO")) {
                    builder.append(MONTHLY_SALES_VIEW_QUERY);
                    params.put("sclUser", sclUser);
                } else if (sclUser.getUserType().getCode().equals("RH") || sclUser.getUserType().getCode().equalsIgnoreCase("TSM")) {
                    builder.append(MONTHLY_SALES_VIEW_QUERY_FORTSMRH);
                }
            }
        }
        else{
                builder.append(MONTHLY_SALES_VIEW_QUERY_FORTSMRH);
            }

        params.put("month",month);
        params.put("year",year);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("brand",brand);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(MonthlySalesModel.class));
        final SearchResult<MonthlySalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public MonthlySalesModel getMonthlySalesModelDetailForDO(String month, String year, List<SubAreaMasterModel> subAreaList, BaseSiteModel brand) {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder();
         /*   if(sclUser!=null){
                if(sclUser.getUserType().getCode()!=null) {
                    if (sclUser.getUserType().getCode().equalsIgnoreCase("SO")) {
                        builder.append(MONTHLY_SALES_VIEW_QUERY);
                        params.put("sclUser", sclUser);
                    } else if (sclUser.getUserType().getCode().equals("RH") || sclUser.getUserType().getCode().equalsIgnoreCase("TSM")) {
                        builder.append(MONTHLY_SALES_VIEW_QUERY_FORTSMRH);
                    }
                }
            }
            else{*/
                builder.append(MONTHLY_SALES_VIEW_QUERY_FORSH);
            //}

            params.put("month",month);
            params.put("year",year);
            params.put("subArea",subAreaList);
            params.put("brand",brand);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Collections.singletonList(MonthlySalesModel.class));
            final SearchResult<MonthlySalesModel> searchResult = flexibleSearchService.search(query);
            return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public List<List<Object>> viewMonthlyRevisedSalesTargetForReviewTab(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, String dealerCode, Date firstDayOfMonth, Date lastDayOfMonth) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder(VIEW_REVISED_MONTHLYSALES_REVIEWTAB);
            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("sclUser", sclUser);
            params.put("baseSite", baseSite);
            params.put("firstDayOfMonth", firstDayOfMonth);
            params.put("lastDayOfMonth", lastDayOfMonth);
            params.put("orderStatus", orderStatus);
            params.put("dealerCode",dealerCode);
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class, String.class, Double.class,Date.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }
    @Override
    public List<List<Object>> getLastYearShareForProductMonthly(SclUserModel sclUser, BaseSiteModel baseSite, Date firstDayOfMonth, Date lastDayOfMonth) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder(PRODUCT_MIX_PERCENTAGE_MONTHLY);
            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("sclUser", sclUser);
            params.put("baseSite", baseSite);
            params.put("firstDayOfMonth", firstDayOfMonth);
            params.put("lastDayOfMonth", lastDayOfMonth);
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
    public List<List<Object>> getLastYearShareForTargetMonthly(SclUserModel sclUser, BaseSiteModel baseSite, Date firstDayOfMonth, Date lastDayOfMonth) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder(DEALERCATEGORY_TARGET_LAST_YEAR_SHARE_MONTHLY);
            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("sclUser", sclUser);
            params.put("baseSite", baseSite);
            params.put("firstDayOfMonth", firstDayOfMonth);
            params.put("lastDayOfMonth", lastDayOfMonth);
            params.put("orderStatus", orderStatus);
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

    @Override
    public List<String> getLastYearShareForDealerTargetMonthly(String dealerCategory,SclUserModel sclUser, BaseSiteModel baseSite, Date firstDayOfMonth, Date lastDayOfMonth) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder(DEALER_TARGET_LAST_YEAR_SHARE_MONTHLY);
            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("sclUser", sclUser);
            params.put("baseSite", baseSite);
            params.put("firstDayOfMonth", firstDayOfMonth);
            params.put("lastDayOfMonth", lastDayOfMonth);
            params.put("orderStatus", orderStatus);
            params.put("dealerCategory",dealerCategory);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class));
            final SearchResult<String> searchResult = flexibleSearchService.search(query);
            List<String> result = searchResult.getResult();
            return result!=null && !result.isEmpty() ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> fetchDealerDetailsForSelectedRetailer(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, String filter, Date startDate, Date endDate) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder(DEALER_DETAILS_FOR_SELECTED_RETAILER);
            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("sclUser", sclUser);
            params.put("baseSite", baseSite);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            params.put("orderStatus", orderStatus);
            params.put("filter","%"+filter.toUpperCase()+"%");
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class,Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return result!=null && !result.isEmpty() ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> fetchRetailerDetailsForSelectedDealer(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, String filter, Date startDate, Date endDate) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder(RETAILER_DETAILS_FOR_SELECTED_DEALER);
            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("sclUser", sclUser);
            params.put("baseSite", baseSite);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            params.put("orderStatus", orderStatus);
            params.put("filter", "%" + filter.toUpperCase() + "%");
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class,Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return result!=null && !result.isEmpty() ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> fetchDealerCySalesForRetailer(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, String customerCode, Date startDate, Date endDate) {
        return null;
    }

    @Override
    public DealerPlannedAnnualSalesModel fetchRecordForDealerPlannedAnnualSales(String subArea, SclUserModel sclUser, String filter) {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder(DEALER_PLANNED_ANNUAL_SALES);
            params.put("sclUser", sclUser);
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            params.put("filter", "%" + filter.toUpperCase() + "%");
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(DealerPlannedAnnualSalesModel.class));
            final SearchResult<DealerPlannedAnnualSalesModel> searchResult = flexibleSearchService.search(query);
            return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public List<DealerRevisedAnnualSalesModel> fetchRecordForDealerRevisedAnnualSales(String subArea, SclUserModel sclUser, String financialYear) {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder(DEALER_REVISED_ANNUAL_SALES);
            params.put("sclUser", sclUser);
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            params.put("financialYear",financialYear);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Collections.singletonList(DealerRevisedAnnualSalesModel.class));
            final SearchResult<DealerRevisedAnnualSalesModel> searchResult = flexibleSearchService.search(query);
            List<DealerRevisedAnnualSalesModel> result = searchResult.getResult();
            return result != null && !result.isEmpty() ? result : Collections.emptyList();
        }

    @Override
    public List<MonthWiseAnnualTargetModel> getMonthWiseAnnualTargetDetails(String customerCode, String productCode, String subArea) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(MONTH_WISE_ANNUAL_TARGET_DETAILS);
        boolean isAnnualSalesRevisedForDealer = Boolean.FALSE;
        //boolean isNoMonthlyCySaleForSku=Boolean.TRUE;
        params.put("customerCode", customerCode);
        params.put("productCode",productCode);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("isAnnualSalesRevisedForDealer", isAnnualSalesRevisedForDealer);
        //params.put("isNoMonthlyCySaleForSku",isNoMonthlyCySaleForSku);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        List<MonthWiseAnnualTargetModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public Double getCurrentMonthSaleForMonthlySummary(String subArea, BaseSiteModel baseSite, SclUserModel sclUser, Date startDate, Date endDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(CURRENT_MONTH_SALE_MONTHLY_SUMMARY);
        OrderStatus orderStatus=OrderStatus.DELIVERED;
        params.put("sclUser", sclUser);
        params.put("baseSite",baseSite);
        params.put("orderStatus",orderStatus);
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

    @Override
    public Double getCurrentYearSalesForAnnualSummary(SclUserModel sclUser, BaseSiteModel baseSite, String subArea, Date startDate, Date endDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(CURRENT_YEAR_SALES_ANNUAL_SUMMARY);
        OrderStatus orderStatus=OrderStatus.DELIVERED;
        params.put("sclUser", sclUser);
        params.put("baseSite",baseSite);
        params.put("orderStatus",orderStatus);
        params.put("startDate",startDate);
        params.put("endDate",endDate);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty())) {
            LOG.info("getCurrentYearSalesForAnnualSummary DAO" + "scluser pk :" + sclUser + "baseSite pk :" + baseSite + "subArea pk :" + subArea);
            //LOG.info("cy sale summary DAO ::" + searchResult.getResult().get(0));
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        }
        else
            return 0.0;
    }

    @Override
    public Double getCurrentYearSalesForAnnualSummaryNew(BaseSiteModel baseSite, String subArea, Date startDate, Date endDate) {
        return (Double) sessionService.executeInLocalView(new SessionExecutionBody() {
            @Override
            public Double execute() {
                try {
                    searchRestrictionService.disableSearchRestrictions();
                    final Map<String, Object> params = new HashMap<String, Object>();
                    final StringBuilder builder = new StringBuilder(CURRENT_YEAR_SALES_ANNUAL_SUMMARY_NEW);
                    OrderStatus orderStatus = OrderStatus.DELIVERED;
                    params.put("baseSite", baseSite);
                    params.put("orderStatus", orderStatus);
                    params.put("startDate", startDate);
                    params.put("endDate", endDate);
                    params.put("subArea", territoryManagementService.getTerritoryById(subArea));
                    final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
                    query.setResultClassList(Arrays.asList(Double.class));
                    query.addQueryParameters(params);
                    final SearchResult<Double> searchResult = flexibleSearchService.search(query);
                    if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty())) {
                        LOG.info("getCurrentYearSalesForAnnualSummary DAO baseSite pk :" + baseSite + "subArea pk :" + subArea);
                        LOG.info("cy sale summary DAO ::" + searchResult.getResult().get(0));
                        return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
                    } else
                        return 0.0;
                } finally {
                    searchRestrictionService.enableSearchRestrictions();
                }
            }
        });
    }

    @Override
    public Double getCurrentYearSalesForAnnualSummaryForRH(DistrictMasterModel districtMasterModel, BaseSiteModel baseSite,  Date startDate, Date endDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT sum({oe:quantityInMT}) from {OrderEntry AS oe JOIN Order as o ON {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}} where {o:site}=?baseSite and {o.districtMaster}=?districtMasterModel and {oe:invoiceCreationDateAndTime}>=?startDate and {oe.invoiceCreationDateAndTime} < ?endDate and {oe.cancelledDate} is null ");
        OrderStatus orderStatus=OrderStatus.DELIVERED;
        params.put("districtMasterModel", districtMasterModel);
        params.put("baseSite",baseSite);
        params.put("orderStatus",orderStatus);
        params.put("startDate",startDate);
        params.put("endDate",endDate);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty())) {
            LOG.info("getCurrentYearSalesForAnnualSummary DAO" +  "baseSite pk :" + baseSite );
            //LOG.info("cy sale summary DAO ::" + searchResult.getResult().get(0));
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        }
        else
            return 0.0;
    }

    @Override
    public DealerPlannedAnnualSalesModel findDealerDetailsByCustomerCode(String customerCode, String subArea, SclUserModel sclUser, String financialYear) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(DEALER_PLANNED_ANNUAL_SALES_DETAILS);
        params.put("sclUser", sclUser);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("customerCode",customerCode);
        params.put("financialYear",financialYear);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(DealerPlannedAnnualSalesModel.class));
        final SearchResult<DealerPlannedAnnualSalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public RetailerPlannedAnnualSalesModel findDealerDetailsForRetailerTargetSet(String customerCode, String subArea, SclUserModel sclUser, String financialYear) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(RETAILER_PLANNED_ANNUAL_SALES_DETAILS);
        params.put("sclUser", sclUser);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("customerCode",customerCode);
        params.put("financialYear",financialYear);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(RetailerPlannedAnnualSalesModel.class));
        final SearchResult<RetailerPlannedAnnualSalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public MonthWiseAnnualTargetModel fetchMonthWiseAnnualTargetDetails(String customerCode, String subArea, String key, String value, SclUserModel sclUser) {
        String[] s = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "InvalidNumber"};
        String monthYear="";
        StringBuilder str=new StringBuilder();
        if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 12) {
            if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 3) {
                monthYear=(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear() + 1))));
            }
            if (Integer.parseInt(key) >= 4 && Integer.parseInt(key) <= 12) {
                monthYear=(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear()))));
            }
        }

        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(MONTH_WISE_ANNUAL_SALES_TARGET_DETAILS);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("customerCode",customerCode);
        params.put("monthYear",monthYear);
        params.put("sclUser",sclUser);
       // params.put("isAnnualSalesRevisedForDealer",false);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public List<List<Object>> getLastYearShareForProductFromNCR(String subArea, String catalogId, String catalogVersion, Date startDate, Date endDate, CustomerCategory customerCategory) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder(LAST_YEAR_PRODUCT_SHARE_NCR_ANNUAL);
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            params.put("catalogVersion", catalogVersion);
            params.put("catalogId", catalogId);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            params.put("customerCategory", customerCategory);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class, String.class, Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return result!=null && !result.isEmpty() ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> getLastYearShareForDealerFromNCRAnnual(String subArea, Date startDate, Date endDate) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder(LAST_YEAR_DEALER_SHARE_NCR_ANNUAL);
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class,Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return result!=null && !result.isEmpty() ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> getLastYearShareForProductFromNCRMonthly(String subArea, String catalogId, String catalogVersion, int year, int month, CustomerCategory customerCategory) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select {p:code},{p:name},sum({s:quantity}) from {SalesHistory as s JOIN Product as p on {p:state}={s:state} and {p:packagingCondition}={s:packagingCondition} and {p:inventoryId}={s:inventoryItemId} join Catalog as c on {c.pk}={p.catalog} join CatalogVersion as cv on {cv.pk}={p.catalogversion} JOIN CustomerSubAreaMapping as u on {s:state}={u:state}} where {cv.version}=?catalogVersion and {c.id}=?catalogId and {u:subAreaMaster}=?subArea and {s:customerCategory}=?customerCategory and {s:inventoryItemId} is not null and").append(SclDateUtility.getDateClauseQueryByMonthYear("invoiceDate",month,year,params)).append("group by {p:code},{p:name}");
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            params.put("catalogVersion", catalogVersion);
            params.put("catalogId", catalogId);
            params.put("month", month);
            params.put("year", year);
            params.put("customerCategory", customerCategory);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class, String.class,Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return result!=null && !result.isEmpty() ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> getLastYearShareForDealerFromNCRMonthly(String subArea,int year, int month) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select {d:code},sum({s:quantity}) from {SalesHistory as s JOIN SclCustomer as sc on {s:customerNo}={sc:customerNo} JOIN DealerCategory as d on {sc:dealerCategory}={d:pk} JOIN  CustomerSubAreaMapping as u on {u:state}={s:state}} where {u:subAreaMaster}=?subArea and").append(SclDateUtility.getDateClauseQueryByMonthYear("invoiceDate",month,year,params)).append("group by {d:code}");
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            params.put("month", month);
            params.put("year", year);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class,Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return result!=null && !result.isEmpty() ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<String> getStateWiseProductForSummaryPage(String state, String catalogId, String version, String prodStatus) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select {p:name} from {Product as p join Catalog as c on {c.pk}={p.catalog} join CatalogVersion as cv on {cv.pk}={p.catalogversion}} where {cv.version}=?version and {c.id}=?catalogId and {p:approvalStatus}=?approved ");
            if(state!=null){
                builder.append(" and {p:state}=?state ");
            }
            params.put("approved", ArticleApprovalStatus.APPROVED);
            params.put("catalogId", catalogId);
            params.put("version", version);
            params.put("state", state);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class));
            final SearchResult<String> searchResult = flexibleSearchService.search(query);
            List<String> result = searchResult.getResult();
            return result!=null && !result.isEmpty() ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<String> getDealerCategoryForSummaryPage() {
        try {
            final StringBuilder builder = new StringBuilder("select distinct{d:code} from {SclCustomer as sc JOIN DealerCategory as d on {sc:dealerCategory}={d:pk}}");
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.setResultClassList(Arrays.asList(String.class));
            final SearchResult<String> searchResult = flexibleSearchService.search(query);
            List<String> result = searchResult.getResult();
            return result!=null && !result.isEmpty() ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public MonthWiseAnnualTargetModel fetchMonthWiseAnnualTargetDetailsForSku(String subArea, String customerCode, String productCode, String key, String value, SclUserModel sclUser) {
        String[] s = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "InvalidNumber"};
        String monthYear="";
        StringBuilder str=new StringBuilder();
        if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 12) {
            if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 3) {
                monthYear=(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear() + 1))));
            }
            if (Integer.parseInt(key) >= 4 && Integer.parseInt(key) <= 12) {
                monthYear=(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear()))));
            }
        }

        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {MonthWiseAnnualTarget} where {customerCode}=?customerCode and {subAreaMaster}=?subArea and {productCode}=?productCode and {monthYear}=?monthYear and {salesOfficer}=?sclUser");
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("customerCode",customerCode);
        params.put("productCode",productCode);
        params.put("monthYear",monthYear);
        params.put("sclUser",sclUser);
        //params.put("premium",premium);
       // params.put("isAnnualSalesRevisedForDealer", false);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public ProductSaleModel getSalesForSku(String customerCode, String productCode, String subArea, SclUserModel sclUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {ProductSale} where {customerCode}=?customerCode and {productCode}=?productCode and {subAreaMaster}=?subArea and {salesOfficer}=?sclUser and {isAnnualSalesRevisedForDealer}=?isAnnualSalesRevisedForDealer");
        params.put("customerCode",customerCode);
        params.put("productCode",productCode);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("sclUser",sclUser);
        params.put("isAnnualSalesRevisedForDealer", false);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(ProductSaleModel.class));
        final SearchResult<ProductSaleModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public DealerRevisedAnnualSalesModel findDealerRevisedDetailsByCustomerCode(String customerCode, String subArea, SclUserModel sclUser, String financialYear) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(DEALER_REVISED_ANNUAL_SALES_DETAILS);
        params.put("sclUser", sclUser);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("customerCode",customerCode);
        params.put("financialYear",financialYear);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(DealerRevisedAnnualSalesModel.class));
        final SearchResult<DealerRevisedAnnualSalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public RetailerPlannedAnnualSalesModel findRetailerDetailsByCustomerCode(String customerCode, String subArea, SclUserModel sclUser, String financialYear) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(RETAILER_PLANNED_ANNUAL_SALES_DETAILS);
        params.put("sclUser", sclUser);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("customerCode",customerCode);
        params.put("financialYear",financialYear);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(RetailerPlannedAnnualSalesModel.class));
        final SearchResult<RetailerPlannedAnnualSalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public RetailerRevisedAnnualSalesModel findRetailerRevisedDetailsByCustomerCode(String customerCode, String subArea, SclUserModel sclUser, String financialYear) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(RETAILER_REVISED_ANNUAL_SALES_DETAILS);
        params.put("sclUser", sclUser);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("customerCode",customerCode);
        params.put("financialYear",financialYear);
        params.put("isAnnualSalesRevisedForRetailer",true);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(RetailerRevisedAnnualSalesModel.class));
        final SearchResult<RetailerRevisedAnnualSalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0):null;
    }

    @Override
    public MonthWiseAnnualTargetModel fetchMonthWiseAnnualTargetDetailsOfDealerForRetailer(String customerCode, String subArea, String monthYear, SclUserModel sclUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(MONTH_WISE_DETAILE_OF_DEALER_FOR_RETAILER);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("customerCode",customerCode);
        params.put("monthYear",monthYear);
        params.put("sclUser",sclUser);
        params.put("isAnnualSalesRevisedForRetailer",true);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public MonthWiseAnnualTargetModel fetchMonthWiseAnnualTargetDetailsForDealerRetailer(String customerCode, String subArea, String key, String value, SclUserModel sclUser) {
        String[] s = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "InvalidNumber"};
        String monthYear="";
        StringBuilder str=new StringBuilder();
        if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 12) {
            if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 3) {
                monthYear=(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear() + 1))));
            }
            if (Integer.parseInt(key) >= 4 && Integer.parseInt(key) <= 12) {
                monthYear=(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear()))));
            }
        }

        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(MONTH_WISE_DETAILE_OF_DEALER_FOR_RETAILER);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("customerCode",customerCode);
        params.put("monthYear",monthYear);
        params.put("sclUser",sclUser);
        params.put("isAnnualSalesRevisedForRetailer",false);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public MonthWiseAnnualTargetModel fetchMonthWiseAnnualTargetDetailsForRetailers(String customerCode, String retailerCode, String subArea, String key, String value, SclUserModel sclUser) {
        return null;
    }

    @Override
    public MonthWiseAnnualTargetModel fetchDealerRevisedMonthWiseTargetDetails(String customerCode, String subArea, String monthYear, SclUserModel sclUser, boolean isAnnualSalesRevised) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {m:pk} from {MonthWiseAnnualTarget as m JOIN DealerRevisedAnnualSales as ds on {m:dealerRevisedAnnualSales}={ds:pk}}  where {m:customerCode}=?customerCode and {m:monthYear}=?monthYear and {m:salesOfficer}=?sclUser and {m:subAreaMaster}=?subArea and {m:isAnnualSalesRevisedForDealer}=?isAnnualSalesRevised");
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("customerCode",customerCode);
        params.put("monthYear",monthYear);
        params.put("isAnnualSalesRevised",true);
        params.put("sclUser",sclUser);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public MonthWiseAnnualTargetModel fetchDealerRevisedMonthWiseSkuDetails(String subArea, String customerCode, String productCode, String monthYear, SclUserModel sclUser, boolean isAnnualSalesRevised) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {MonthWiseAnnualTarget} where {customerCode}=?customerCode and {productCode}=?productCode and {monthYear}=?monthYear and {salesOfficer}=?sclUser and {subAreaMaster}=?subArea and {isAnnualSalesRevisedForDealer}=?isAnnualSalesRevised");
        params.put("subArea", territoryManagementService.getTerritoryById(subArea));
        params.put("customerCode",customerCode);
        params.put("productCode",productCode);
        params.put("monthYear",monthYear);
        params.put("isAnnualSalesRevised",isAnnualSalesRevised);
        params.put("sclUser",sclUser);
        //params.put("premium",premium);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0):null;
    }

    @Override
    public MonthWiseAnnualTargetModel fetchRevisedMonthWiseSkuDetails(String subArea, SclUserModel sclUser, String monthYear, String customerCode, String productCode, Boolean isAnnualSalesRevised) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {MonthWiseAnnualTarget} where {customerCode}=?customerCode and {productCode}=?productCode and {monthYear}=?monthYear and {salesOfficer}=?sclUser and {subAreaMaster}=?subArea and {isAnnualSalesRevisedForDealer}=?isAnnualSalesRevisedForDealer");
        boolean isAnnualSalesRevisedForDealer=true;
        params.put("subArea", territoryManagementService.getTerritoryById(subArea));
        params.put("customerCode",customerCode);
        params.put("productCode",productCode);
        params.put("monthYear",monthYear);
        params.put("isAnnualSalesRevisedForDealer",isAnnualSalesRevisedForDealer);
        params.put("sclUser",sclUser);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public ProductSaleModel getRevisedSalesForSku(String customerCode, String productCode, String subArea, SclUserModel sclUser, boolean isAnnualSalesRevised) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {ProductSale} where {customerCode}=?customerCode and {productCode}=?productCode and {subAreaMaster}=?subArea and {salesOfficer}=?sclUser and {isAnnualSalesRevisedForDealer}=?isAnnualSalesRevised");
        params.put("customerCode",customerCode);
        params.put("productCode",productCode);
        params.put("subArea", territoryManagementService.getTerritoryById(subArea));
        params.put("sclUser",sclUser);
        params.put("isAnnualSalesRevised",isAnnualSalesRevised);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(ProductSaleModel.class));
        final SearchResult<ProductSaleModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public Double fetchDealerCySalesForRetailerAnnualSales(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, String customerCode, Date startDate, Date endDate) {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder(DEALER_CY_SALES_FOR_RETAILER);
            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            params.put("sclUser", sclUser);
            params.put("baseSite", baseSite);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            params.put("orderStatus", orderStatus);
            params.put("customerCode", customerCode);
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
    public List<List<Object>> viewDealerDetailsForRetailerAnnualSales(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder(DEALER_DETAILS_FOR_RETAILER);
            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            params.put("sclUser", sclUser);
            params.put("baseSite", baseSite);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            params.put("orderStatus", orderStatus);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class,String.class,Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public MonthWiseAnnualTargetModel fetchMonthWiseSelfCounterDetails(String customerCode, String selfCounterCode, String subArea, SclUserModel sclUser, String key, String value) {
        String[] s = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "InvalidNumber"};
        String monthYear="";
        StringBuilder str=new StringBuilder();
        if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 12) {
            if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 3) {
                monthYear=(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear() + 1))));
            }
            if (Integer.parseInt(key) >= 4 && Integer.parseInt(key) <= 12) {
                monthYear=(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear()))));
            }
        }

        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {MonthWiseAnnualTarget} where {customerCode}=?customerCode and {selfCounterCustomerCode}=?selfCounterCode and {monthYear}=?monthYear and {salesOfficer}=?sclUser and {subAreaMaster}=?subArea and {retailerCode} is null and {productCode} is null");
        params.put("subArea", territoryManagementService.getTerritoryById(subArea));
        params.put("customerCode",customerCode);
        params.put("selfCounterCode",selfCounterCode);
        params.put("monthYear",monthYear);
        params.put("sclUser",sclUser);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public MonthWiseAnnualTargetModel fetchRevisedMonthWiseSelfCounterDetails(String customerCode, String selfCounterCode, String subArea, SclUserModel sclUser, String monthYear) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {MonthWiseAnnualTarget} where {customerCode}=?customerCode and {selfCounterCustomerCode}=?selfCounterCode and {monthYear}=?monthYear and {salesOfficer}=?sclUser and {subAreaMaster}=?subArea and {isAnnualSalesRevisedForRetailer}=?isAnnualSalesRevisedForRetailer and {productCode} is null and {retailerCode} is null");
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("customerCode",customerCode);
        params.put("selfCounterCode",selfCounterCode);
        params.put("monthYear",monthYear);
        params.put("sclUser",sclUser);
        params.put("isAnnualSalesRevisedForRetailer",true);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public SelfCounterSaleDetailsModel fetchSelfCounterDetails(String customerCode, String subArea, SclUserModel sclUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {SelfCounterSaleDetails} where {customerCode}=?customerCode and {salesOfficer}=?sclUser and {subAreaMaster}=?subArea");
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("customerCode",customerCode);
        params.put("sclUser",sclUser);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(SelfCounterSaleDetailsModel.class));
        final SearchResult<SelfCounterSaleDetailsModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public RetailerPlannedAnnualSalesDetailsModel fetchRetailerDetails(String retailerCode,SclUserModel sclUser, String subArea) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {RetailerPlannedAnnualSalesDetails} where {customerCode}=?retailerCode and {salesOfficer}=?sclUser and {subArea}=?subArea");
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("retailerCode",retailerCode);
        params.put("sclUser",sclUser);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(RetailerPlannedAnnualSalesDetailsModel.class));
        final SearchResult<RetailerPlannedAnnualSalesDetailsModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public MonthWiseAnnualTargetModel fetchRetailerMonthWiseDetails(String dealerCode, String retailerCode, String subArea, SclUserModel sclUser, String key, String value) {
        String[] s = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "InvalidNumber"};
        String monthYear="";
        StringBuilder str=new StringBuilder();
        if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 12) {
            if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 3) {
                monthYear=(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear() + 1))));
            }
            if (Integer.parseInt(key) >= 4 && Integer.parseInt(key) <= 12) {
                monthYear=(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear()))));
            }
        }

        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {MonthWiseAnnualTarget} where {customerCode}=?dealerCode and {retailerCode}=?retailerCode and {monthYear}=?monthYear and {salesOfficer}=?sclUser and {subAreaMaster}=?subArea and {productCode} is null and {selfCounterCustomerCode} is null");
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("dealerCode",dealerCode);
        params.put("retailerCode",retailerCode);
        params.put("monthYear",monthYear);
        params.put("sclUser",sclUser);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public SelfCounterSaleDetailsModel fetchRevisedSelfCounterDetails(String customerCode, String subArea, SclUserModel sclUser, boolean isAnnualSalesRevised) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {SelfCounterSaleDetails} where {customerCode}=?customerCode and {salesOfficer}=?sclUser and {subAreaMaster}=?subArea and {isAnnualSalesRevisedForRetailer}=?isAnnualSalesRevisedForRetailer");
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("customerCode",customerCode);
        params.put("sclUser",sclUser);
        params.put("isAnnualSalesRevisedForRetailer",true);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(SelfCounterSaleDetailsModel.class));
        final SearchResult<SelfCounterSaleDetailsModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public Double getPlannedMonthSaleForMonthlySaleSummary(SclUserModel sclUser, BaseSiteModel baseSite, String subArea, int month, int year)
    {
        final Map<String, Object> params = new HashMap<String, Object>();
       // final StringBuilder builder = new StringBuilder("select sum({s:quantity}) from {SalesHistory as s JOIN UserSubAreaMapping as u on {s:state}={u:state}} where {u:subAreaMaster}=?subArea and").append(SclDateUtility.getDateClauseQueryByMonthYear("invoiceDate",month,year,params));
        final StringBuilder builder = new StringBuilder("SELECT sum({oe:quantityInMT}) from {OrderEntry as oe JOIN Order as o on {oe:order}={o:pk} JOIN SclCustomer as sc on {o:user}={sc:pk}} where {o:placedBy}=?sclUser and {oe:status}=?orderStatus and {o:site}=?baseSite and {o:subAreaMaster}=?subArea and").append(SclDateUtility.getDateClauseQueryByMonthYear("oe:deliveredDate", month, year, params));
        OrderStatus orderStatus= OrderStatus.DELIVERED;
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("baseSite",baseSite);
        params.put("sclUser",sclUser);
        params.put("orderStatus",orderStatus);
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
    public RetailerRevisedAnnualSalesDetailsModel fetchRevisedRetailerDetails(String retailerCode, SclUserModel sclUser, String subArea) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {RetailerRevisedAnnualSalesDetails} where {customerCode}=?retailerCode and {salesOfficer}=?sclUser and {subAreaMaster}=?subArea and {isAnnualSalesRevisedForRetailer}=?isAnnualSalesRevisedForRetailer");
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("retailerCode",retailerCode);
        params.put("sclUser",sclUser);
        params.put("isAnnualSalesRevisedForRetailer",true);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(RetailerRevisedAnnualSalesDetailsModel.class));
        final SearchResult<RetailerRevisedAnnualSalesDetailsModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public MonthWiseAnnualTargetModel fetchRevisedRetailerMonthWiseDetails(String dealerCode, String retailerCode, String subArea, SclUserModel sclUser, String monthYear) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {MonthWiseAnnualTarget} where {customerCode}=?dealerCode and {retailerCode}=?retailerCode and {monthYear}=?monthYear and {salesOfficer}=?sclUser and {subAreaMaster}=?subArea and {isAnnualSalesRevisedForRetailer}=?isAnnualSalesRevisedForRetailer and {selfCounterCustomerCode} is null and {productCode} is null");
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("dealerCode",dealerCode);
        params.put("retailerCode",retailerCode);
        params.put("monthYear",monthYear);
        params.put("isAnnualSalesRevisedForRetailer",true);
        params.put("sclUser",sclUser);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public RetailerPlannedAnnualSalesModel fetchRecordForRetailerPlannedAnnualSales(String subArea, SclUserModel sclUser, String filter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(RETAILER_PLANNED_ANNUAL_SALES);
        params.put("sclUser", sclUser);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("filter", "%" + filter.toUpperCase() + "%");
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(RetailerPlannedAnnualSalesModel.class));
        final SearchResult<RetailerPlannedAnnualSalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public List<List<Object>> fetchProductSaleDetailsForSummary(String subArea, SclUserModel sclUser) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({pp:totalTarget}), {p:name} from {ProductToProductSaleReln as cp JOIN Product as p on {cp:source}={p:pk} JOIN ProductSale as pp on {cp:target}={pp:pk}} where {pp:subAreaMaster}=?subArea and {pp:salesOfficer}=?sclUser and {pp:isAnnualSalesRevisedForDealer}=?isAnnualSalesRevisedForDealer group by {p:name}");
            boolean isAnnualSalesRevisedForDealer = Boolean.TRUE;
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            params.put("sclUser", sclUser);
            params.put("isAnnualSalesRevisedForDealer",isAnnualSalesRevisedForDealer);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> getMonthSplitupForDealerForRetailerSetting(String sclCustomer, SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate, String subArea) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder(MONTHWISE_QUANTITY_FOR_DEALER_OF_RETAILER);
            OrderStatus orderStatus = OrderStatus.DELIVERED;
            params.put("sclUser", sclUser);
            params.put("baseSite", baseSite);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            params.put("orderStatus", orderStatus);
            params.put("sclCustomer",sclCustomer);
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class,String.class,Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public Double getTotalTargetForDealers(SclUserModel sclUser, String subArea, String financialYear)
    {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {ann:totalRevisedTargetForAllDealers} from {DealerRevisedAnnualSales as ds JOIN AnnualSales as ann on {ds:annualSales}={ann.pk}} where {ds:subAreaMaster}=?subArea and {ann:salesOfficer}=?sclUser and {ann:financialYear}=?financialYear");
        params.put("sclUser", sclUser);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("financialYear", financialYear);
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
    public Double getPlannedTargetAfterTargetSetMonthlySP(SclUserModel sclUser, String subArea, String month, String year)
    {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {m:dealerPlannedTotalPlannedTarget} from {MonthlySales as m} where {m:so}=?sclUser and {m:monthName}=?month and {m:monthYear}=?year and {m:subAreaMaster}=?subArea");
        params.put("sclUser", sclUser);
        params.put("subArea", territoryManagementService.getTerritoryById(subArea));
        params.put("month", month);
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
    public Double getRevisedTargetAfterTargetSetMonthlySP(SclUserModel sclUser, String subArea, String month, String year)
    {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {m:dealerPlannedTotalRevisedTarget} from {MonthlySales as m} where {m:so}=?sclUser and {m:monthName}=?month and {m:monthYear}=?year and {m:subAreaMaster}=?subArea");
        params.put("subArea", territoryManagementService.getTerritoryById(subArea));
        params.put("sclUser",sclUser);
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
    public Double getPlannedTargetForReviewMonthlySP(SclUserModel sclUser, String subArea, String month, String year)
    {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {m:dealerPlannedTotalPlannedTarget} from {MonthlySales as m} where {m:so}=?sclUser and {m:monthName}=?month and {m:monthYear}=?year and {m:subAreaMaster}=?subArea");
        params.put("sclUser", sclUser);
        params.put("subArea", territoryManagementService.getTerritoryById(subArea));
        params.put("month", month);
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
    public Double getPlannedTargetForReviewMonthlySPForRH(DistrictMasterModel districtMasterModel, String month, String year) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {m:dealerPlannedTotalPlannedTarget} from {MonthlySales as m} where {m:so}=?sclUser and {m:monthName}=?month and {m:monthYear}=?year and {m:pk}=?districtMasterModel");
        params.put("districtMasterModel", districtMasterModel);
        params.put("month", month);
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
    public Double getRevisedTargetForReviewMonthlySP(SclUserModel sclUser, String subArea, String month, String year)
    {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {m:dealerPlannedTotalRevisedTarget} from {MonthlySales as m} where {m:so}=?sclUser and {m:monthName}=?month and {m:monthYear}=?year and {m:subAreaMaster}=?subArea");
        params.put("subArea", territoryManagementService.getTerritoryById(subArea));
        params.put("sclUser",sclUser);
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
    public Double getRevisedTargetForReviewMonthlySPForRH(DistrictMasterModel districtMasterModel, String month, String year) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {m:dealerPlannedTotalRevisedTarget} from {MonthlySales as m} where {m:so}=?sclUser and {m:monthName}=?month and {m:monthYear}=?year and {m:pk}=?districtMasterModel");
        params.put("districtMasterModel", districtMasterModel);
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
    public List<List<Object>> fetchProductMixDetailsAfterTargetSetMonthlySummary(String subArea, SclUserModel sclUser, String month, String year, BaseSiteModel baseSite) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({pp:revisedTarget}), {pp:productName} from {ProductSale as pp} where {pp:subAreaMaster}=?subArea and {pp:salesOfficer}=?sclUser and {pp:isMonthlySalesForPlannedDealer}=?isMonthlySalesForPlannedDealer and {pp:brand}=?baseSite group by {pp:productName}");
            boolean isMonthlySalesForPlannedDealer = Boolean.TRUE;
            params.put("subArea", territoryManagementService.getTerritoryById(subArea));
            params.put("sclUser", sclUser);
            params.put("isMonthlySalesForPlannedDealer",isMonthlySalesForPlannedDealer);
            params.put("baseSite",baseSite);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> fetchProductMixDetailsAfterTargetSetMonthlySummaryForRH(DistrictMasterModel districtMasterModel, String month, String year) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({pp:revisedTarget}), {pp:productName} from {ProductSale as pp} where {pp:districtMaster}=?districtMasterModel and {pp:isMonthlySalesForPlannedDealer}=?isMonthlySalesForPlannedDealer group by {pp:productName}");
            boolean isMonthlySalesForPlannedDealer = Boolean.TRUE;
            params.put("districtMasterModel", districtMasterModel);
            params.put("isMonthlySalesForPlannedDealer",isMonthlySalesForPlannedDealer);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> fetchProductMixDetailsAfterTargetSetMonthlySummaryForSumRH(List<DistrictMasterModel> districtMasterModel, String month, String year) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({pp:revisedTarget}), {pp:productName} from {ProductSale as pp} where {pp:districtMaster} in (?districtMasterModel) and {pp:isMonthlySalesForPlannedDealer}=?isMonthlySalesForPlannedDealer group by {pp:productName}");
            boolean isMonthlySalesForPlannedDealer = Boolean.TRUE;
            params.put("districtMasterModel", districtMasterModel);
            params.put("isMonthlySalesForPlannedDealer",isMonthlySalesForPlannedDealer);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> fetchProductMixDetailsAfterTargetSetMonthlySummaryForTSM(List<SubAreaMasterModel> subArea, String month, String year, BaseSiteModel baseSite) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({pp:revisedTarget}), {pp:productName} from {ProductSale as pp} where {pp:subAreaMaster} in (?subArea) and {pp:isMonthlySalesForPlannedDealer}=?isMonthlySalesForPlannedDealer and {pp:brand}=?baseSite group by {pp:productName}");
            boolean isMonthlySalesForPlannedDealer = Boolean.TRUE;
            params.put("subArea", subArea);
            params.put("isMonthlySalesForPlannedDealer",isMonthlySalesForPlannedDealer);
            params.put("baseSite",baseSite);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> fetchProductMixDetailsForReviewTargetMonthlySummary(String subArea, SclUserModel sclUser, String month, String year, BaseSiteModel baseSite) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({pp:revisedTarget}), {pp:productName} from {ProductSale as pp} where {pp:subAreaMaster}=?subArea and {pp:salesOfficer}=?sclUser and {pp:isMonthlySalesForReviewedDealer}=?isMonthlySalesForReviewedDealer and {pp:brand}=?baseSite group by {pp:productName}");
            boolean isMonthlySalesForReviewedDealer = Boolean.TRUE;
            params.put("subArea", territoryManagementService.getTerritoryById(subArea));
            params.put("sclUser", sclUser);
            params.put("isMonthlySalesForReviewedDealer",isMonthlySalesForReviewedDealer);
            params.put("baseSite",baseSite);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> fetchProductMixDetailsForReviewTargetMonthlySummaryForRH(DistrictMasterModel districtMasterModel, String month, String year) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({pp:revisedTarget}), {pp:productName} from {ProductSale as pp} where {pp:districtMaster}=?districtMasterModel and {pp:isMonthlySalesForReviewedDealer}=?isMonthlySalesForReviewedDealer group by {pp:productName}");
            boolean isMonthlySalesForReviewedDealer = Boolean.TRUE;
            params.put("districtMasterModel", districtMasterModel);
            params.put("isMonthlySalesForReviewedDealer",isMonthlySalesForReviewedDealer);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> fetchProductMixDetailsForReviewTargetMonthlySummaryForSumRH(List<DistrictMasterModel> districtMasterModel, String month, String year) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({pp:revisedTarget}), {pp:productName} from {ProductSale as pp} where {pp:districtMaster} in (?districtMasterModel) and {pp:isMonthlySalesForReviewedDealer}=?isMonthlySalesForReviewedDealer group by {pp:productName}");
            boolean isMonthlySalesForReviewedDealer = Boolean.TRUE;
            params.put("districtMasterModel", districtMasterModel);
            params.put("isMonthlySalesForReviewedDealer",isMonthlySalesForReviewedDealer);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> fetchProductMixDetailsForReviewTargetMonthlySummaryForTSM(List<SubAreaMasterModel> subArea, String month, String year, BaseSiteModel baseSite) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({pp:revisedTarget}), {pp:productName} from {ProductSale as pp} where {pp:subAreaMaster} in (?subArea) and  {pp:isMonthlySalesForReviewedDealer}=?isMonthlySalesForReviewedDealer and {pp:brand}=?baseSite group by {pp:productName}");
            boolean isMonthlySalesForReviewedDealer = Boolean.TRUE;
            params.put("subArea", subArea);
            params.put("isMonthlySalesForReviewedDealer",isMonthlySalesForReviewedDealer);
            params.put("baseSite",baseSite);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<MonthWiseAnnualTargetModel> getMonthWiseSkuDetailsForReview(String customerCode, String productCode, String subArea, SclUserModel sclUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {MonthWiseAnnualTarget} where {customerCode}=?customerCode and {productCode}=?productCode and {subAreaMaster}=?subArea and {isAnnualSalesReviewedForDealer}=?isAnnualSalesReviewedForDealer and {salesOfficer}=?sclUser");
        boolean isAnnualSalesReviewedForDealer = Boolean.TRUE;
        params.put("customerCode", customerCode);
        params.put("productCode",productCode);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("isAnnualSalesReviewedForDealer", isAnnualSalesReviewedForDealer);
        params.put("sclUser",sclUser);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        List<MonthWiseAnnualTargetModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public List<MonthWiseAnnualTargetModel> getMonthWiseRetailerDetailsForReview(String dealerCode, String retailerCode, String subArea, SclUserModel sclUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {MonthWiseAnnualTarget} where {customerCode}=?dealerCode and {retailerCode}=?retailerCode and {subAreaMaster}=?subArea and {isAnnualSalesReviewedForRetailerDetails}=?isAnnualSalesReviewedForRetailerDetails and {salesOfficer}=?sclUser");
        params.put("dealerCode", dealerCode);
        params.put("retailerCode",retailerCode);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("isAnnualSalesReviewedForRetailerDetails", true);
        params.put("sclUser",sclUser);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        List<MonthWiseAnnualTargetModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public DealerPlannedMonthlySalesModel checkExistingDealerPlannedMonthlySales(SclUserModel sclUser, String customerCode, String subArea, String monthName, String monthYear) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {DealerPlannedMonthlySales as dp JOIN MonthlySales as m on {dp:monthlySales}={m:pk}} where {dp:customerCode}=?customerCode and {dp:subAreaMaster}=?subArea and {dp:monthName}=?monthName and {dp:monthYear}=?monthYear and {m:so}=?sclUser");
        params.put("sclUser", sclUser);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("customerCode",customerCode);
        params.put("monthName",monthName);
        params.put("monthYear",monthYear);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(DealerPlannedMonthlySalesModel.class));
        final SearchResult<DealerPlannedMonthlySalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public ProductSaleModel checkExistingProductSaleForDealerPlannedMonthlySales(String subArea, SclUserModel sclUser, String productCode, String customerCode, String monthName, String monthYear) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {ProductSale} where {customerCode}=?customerCode and {productCode}=?productCode and {subAreaMaster}=?subArea and {salesOfficer}=?sclUser and {monthName}=?monthName and {monthYear}=?monthYear and {isMonthlySalesForPlannedDealer}=?isMonthlySalesForPlannedDealer and {isMonthlySalesForReviewedDealer}=?isMonthlySalesForReviewedDealer");
        boolean isMonthlySalesForPlannedDealer=true;
        params.put("customerCode",customerCode);
        params.put("productCode",productCode);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("sclUser",sclUser);
        params.put("isMonthlySalesForPlannedDealer",isMonthlySalesForPlannedDealer);
        params.put("isMonthlySalesForReviewedDealer",false);
        params.put("monthName",monthName);
        params.put("monthYear",monthYear);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(ProductSaleModel.class));
        final SearchResult<ProductSaleModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public Double getPlannedTargetAfterReviewMonthlySP(SclUserModel sclUser, String subArea, String month, String year)
    {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {m:dealerReviewedTotalPlannedTarget} from {MonthlySales as m} where {m:so}=?sclUser and {m:monthName}=?month and {m:monthYear}=?year and {m:subAreaMaster}=?subArea");
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("sclUser",sclUser);
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
    public Double getRevisedTargetAfterReviewMonthlySP(SclUserModel sclUser, String subArea, String month, String year, BaseSiteModel site)
    {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {m:dealerReviewedTotalRevisedTarget} from {MonthlySales as m} where {m:so}=?sclUser and {m:monthName}=?month and {m:monthYear}=?year and {m:subAreaMaster}=?subArea and {m:brand}=?site");
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("sclUser",sclUser);
        params.put("month",month);
        params.put("year",year);
        params.put("site",site);
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
    public Double getPlannedTargetAfterReviewMonthlySPForRH(DistrictMasterModel districtMasterModel, String formattedMonth, String year) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {m:dealerReviewedTotalPlannedTarget} from {MonthlySales as m} where {m:monthName}=?month and {m:monthYear}=?year and {m:pk}=?districtMasterModel");
        params.put("districtMasterModel",districtMasterModel);
        params.put("month",formattedMonth);
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
    public Double getRevisedTargetAfterReviewMonthlySPForRH(DistrictMasterModel districtMasterModel, String formattedMonth, String year) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {m:dealerReviewedTotalRevisedTarget} from {MonthlySales as m } where {m:monthName}=?month and {m:monthYear}=?year and {m:pk}=?districtMasterModel");
        params.put("districtMasterModel",districtMasterModel);
        params.put("month",formattedMonth);
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
    public MonthWiseAnnualTargetModel fetchDealerRevisedMonthWiseSkuDetailsForOnboardedDealer(String subArea, String customerCode, String productCode, String monthYear, SclUserModel sclUser, Boolean isNewDealerOnboarded) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {MonthWiseAnnualTarget} where {customerCode}=?customerCode and {productCode}=?productCode and {monthYear}=?monthYear and {salesOfficer}=?sclUser and {subAreaMaster}=?subArea and {isAnnualSalesOnboardedForDealer}=?isNewDealerOnboarded");
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("customerCode",customerCode);
        params.put("productCode",productCode);
        params.put("monthYear",monthYear);
        params.put("isNewDealerOnboarded",isNewDealerOnboarded);
        params.put("sclUser",sclUser);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0):null;
    }

    @Override
    public List<DealerPlannedMonthlySalesModel> fetchDealerPlannedMonthlySalesDetails(String subArea, SclUserModel sclUser, String month, String year) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {dp:pk} FROM {MonthlySales as ms JOIN DealerPlannedMonthlySales as dp on {dp:monthlySales}={ms:pk}} WHERE {ms:so}=?sclUser and {dp:subAreaMaster}=?subArea and {dp:monthName}=?month and {dp:monthYear}=?year and {ms:isMonthlySalesPlanned}=?isMonthlySalesPlanned");
        boolean isMonthlySalesPlanned=true;
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("sclUser",sclUser);
        params.put("month",month);
        params.put("year",year);
        params.put("isMonthlySalesPlanned",isMonthlySalesPlanned);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(DealerPlannedMonthlySalesModel.class));
        final SearchResult<DealerPlannedMonthlySalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult()) ? searchResult.getResult():Collections.emptyList();
    }

    @Override
    public List<DealerRevisedMonthlySalesModel> fetchDealerReviewedMonthlySalesDetails(String subArea, SclUserModel sclUser, String month, String year) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {dp:pk} FROM {MonthlySales as ms JOIN DealerRevisedMonthlySales as dp on {dp:monthlySales}={ms:pk}} WHERE {ms:so}=?sclUser and {dp:subAreaMaster}=?subArea and {dp:monthName}=?month and {dp:monthYear}=?year and {ms:isMonthlySalesReviewed}=?isMonthlySalesReviewed");
        boolean isMonthlySalesReviewed=true;
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("sclUser",sclUser);
        params.put("month",month);
        params.put("year",year);
        params.put("isMonthlySalesReviewed",isMonthlySalesReviewed);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(DealerRevisedMonthlySalesModel.class));
        final SearchResult<DealerRevisedMonthlySalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult()) ? searchResult.getResult():Collections.emptyList();
    }

   
    @Override
    public MonthWiseAnnualTargetModel validateMonthwiseDealerDetailsForNoCySale(String customerCode, String subArea, String key, SclUserModel sclUser) {
        final Map<String, Object> params = new HashMap<String, Object>();

        String[] s = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "InvalidNumber"};
        String monthYear="";
        StringBuilder str=new StringBuilder();
        if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 12) {
            if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 3) {
                monthYear=(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear() + 1))));
            }
            if (Integer.parseInt(key) >= 4 && Integer.parseInt(key) <= 12) {
                monthYear=(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear()))));
            }
        }
        final StringBuilder builder = new StringBuilder("select {pk} from {MonthWiseAnnualTarget} where {customerCode}=?customerCode and {monthYear}=?monthYear and {salesOfficer}=?sclUser and {subAreaMaster}=?subArea and {isNoMonthlyCySaleForDealer}=?isNoMonthlyCySaleForDealer and {productCode} is null");
        Boolean isNoMonthlyCySaleForDealer=true;
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("customerCode",customerCode);
        params.put("monthYear",monthYear);
        params.put("isNoMonthlyCySaleForDealer",isNoMonthlyCySaleForDealer);
        params.put("sclUser",sclUser);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0):null;
    }

    @Override
    public MonthWiseAnnualTargetModel validateMonthwiseDealerDetailsForNoCySaleForSku(String customerCode, String productCode, String subArea, String key, SclUserModel sclUser) {
        final Map<String, Object> params = new HashMap<String, Object>();

        String[] s = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "InvalidNumber"};
        String monthYear="";
        StringBuilder str=new StringBuilder();
        if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 12) {
            if (Integer.parseInt(key) >= 1 && Integer.parseInt(key) <= 3) {
                monthYear=(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear() + 1))));
            }
            if (Integer.parseInt(key) >= 4 && Integer.parseInt(key) <= 12) {
                monthYear=(String.valueOf(str.append(s[Integer.parseInt(key) - 1]).append("-").append(String.valueOf(LocalDate.now().getYear()))));
            }
        }
        final StringBuilder builder = new StringBuilder("select {pk} from {MonthWiseAnnualTarget} where {customerCode}=?customerCode and {monthYear}=?monthYear and {salesOfficer}=?sclUser and {subAreaMaster}=?subArea and {isNoMonthlyCySaleForDealer}=?isNoMonthlyCySaleForDealer and {productCode}=?productCode");
        Boolean isNoMonthlyCySaleForDealer=true;
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("customerCode",customerCode);
        params.put("productCode",productCode);
        params.put("monthYear",monthYear);
        params.put("isNoMonthlyCySaleForDealer",isNoMonthlyCySaleForDealer);
        params.put("sclUser",sclUser);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0):null;
    }

    @Override
    public double getPlannedMonthSaleForMonthlySaleSummary(SclUserModel sclUser, String currentMonthName, String subArea, BaseSiteModel baseSite) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select sum({m:monthTarget}) from {AnnualSales as ann JOIN DealerRevisedAnnualSales as ds on {ds:annualSales}={ann:pk} JOIN MonthWiseAnnualTarget as m on {m:dealerRevisedAnnualSales}={ds:pk}} where {ann:salesOfficer}=?sclUser and {m:monthYear}=?currentMonthName and {m:subAreaMaster}=?subArea and {m:isAnnualSalesRevisedForDealer}=?isAnnualSalesRevisedForDealer and {ds:brand}=?baseSite");
        params.put("sclUser", sclUser);
        params.put("currentMonthName",currentMonthName);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("isAnnualSalesRevisedForDealer", true);
        params.put("baseSite",baseSite);
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
    public double getTotalTargetForDealersAfterTargetSetting(SclUserModel sclUser, String subArea, String financialYear, BaseSiteModel baseSite) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {ann:totalRevisedTargetForAllDealers} from {DealerRevisedAnnualSales as ds JOIN AnnualSales as ann on {ds:annualSales}={ann.pk}} where {ds:subAreaMaster}=?subArea and {ann:salesOfficer}=?sclUser and {ann:financialYear}=?financialYear and {ann:isAnnualSalesRevised}=?isAnnualSalesRevised and {ds:brand}=?baseSite");
        params.put("sclUser", sclUser);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("financialYear", financialYear);
        params.put("isAnnualSalesRevised",true);
        params.put("baseSite",baseSite);
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
    public double getTotalTargetForDealersAfterTargetSettingForRH(DistrictMasterModel districtMasterModel, String financialYear, BaseSiteModel baseSite) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {ann:totalRevisedTargetForAllDealers} from {DealerRevisedAnnualSales as ds JOIN AnnualSales as ann on {ds:annualSales}={ann.pk}} where {ds.districtMaster}=?districtMasterModel and  {ann:financialYear}=?financialYear and {ann:isAnnualSalesRevised}=?isAnnualSalesRevised and {ds:brand}=?baseSite");
        params.put("districtMasterModel", districtMasterModel);
        params.put("financialYear", financialYear);
        params.put("isAnnualSalesRevised",true);
        params.put("baseSite",baseSite);
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
    public List<List<Object>> fetchProductSaleDetailsForSummaryAfterTargetSetting(String subArea, SclUserModel sclUser, BaseSiteModel baseSite) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({pp:totalTarget}), {pp:productName} from {ProductSale as pp} where {pp:subAreaMaster}=?subArea and {pp:salesOfficer}=?sclUser and {pp:isAnnualSalesRevisedForDealer}=?isAnnualSalesRevisedForDealer and {pp:brand}=?baseSite group by {pp:productName}");
            boolean isAnnualSalesRevisedForDealer = Boolean.TRUE;
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            params.put("sclUser", sclUser);
            params.put("isAnnualSalesRevisedForDealer",isAnnualSalesRevisedForDealer);
            params.put("baseSite",baseSite);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> fetchProductSaleDetailsForSummaryAfterTargetSettingForRH(DistrictMasterModel districtMasterModel, BaseSiteModel baseSite) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({pp:totalTarget}), {pp:productName} from {ProductSale as pp} where {pp:districtMaster}=?districtMasterModel and  {pp:isAnnualSalesRevisedForDealer}=?isAnnualSalesRevisedForDealer and {pp:brand}=?baseSite group by {pp:productName}");
            boolean isAnnualSalesRevisedForDealer = Boolean.TRUE;
            params.put("districtMasterModel",districtMasterModel);
            params.put("baseSite",baseSite);
            params.put("isAnnualSalesRevisedForDealer",isAnnualSalesRevisedForDealer);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> fetchProductSaleDetailsForSummaryAfterTargetSettingForRH(List<DistrictMasterModel> districtMasterModel, BaseSiteModel baseSite) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({pp:totalTarget}), {pp:productName} from {ProductSale as pp} where {pp:districtMaster} in (?districtMasterModel) and  {pp:isAnnualSalesRevisedForDealer}=?isAnnualSalesRevisedForDealer and {pp:brand}=?baseSite group by {pp:productName}");
            boolean isAnnualSalesRevisedForDealer = Boolean.TRUE;
            params.put("districtMasterModel",districtMasterModel);
            params.put("baseSite", baseSite);
            params.put("isAnnualSalesRevisedForDealer",isAnnualSalesRevisedForDealer);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> fetchProductSaleDetailsForSummaryAfterTargetSettingForTSM(List<SubAreaMasterModel> subArea, BaseSiteModel baseSite) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({pp:totalTarget}), {pp:productName} from {ProductSale as pp} where {pp:subAreaMaster} in (?subArea) and {pp:isAnnualSalesRevisedForDealer}=?isAnnualSalesRevisedForDealer and {pp:brand}=?baseSite group by {pp:productName}");
            boolean isAnnualSalesRevisedForDealer = Boolean.TRUE;
            params.put("subArea",subArea);
            params.put("isAnnualSalesRevisedForDealer",isAnnualSalesRevisedForDealer);
            params.put("baseSite",baseSite);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> fetchProductSaleDetailsForSummaryAfterTargetSettingForTSMRH(String subArea, SclUserModel sclUser, String districtCode, String regionCode) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({pp:totalTarget}), {pp:productName} from {ProductSale as pp} where  {pp:salesOfficer}=?sclUser and {pp:isAnnualSalesRevisedForDealer}=?isAnnualSalesRevisedForDealer ");
            if(subArea!=null){
                builder.append(" and {pp:subAreaMaster}=?subArea ");
                params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            }
            if(districtCode!=null){
                builder.append(" and {pp:districtMaster}=?districtCode ");
                params.put("districtCode",districtCode);
            }
            if(regionCode!=null){
                builder.append(" and {pp:regionMaster}=?regionCode ");
                params.put("regionCode",regionCode);
            }
            builder.append(" group by {pp:productName}");
            boolean isAnnualSalesRevisedForDealer = Boolean.TRUE;
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            params.put("sclUser", sclUser);
            params.put("isAnnualSalesRevisedForDealer",isAnnualSalesRevisedForDealer);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public double getTotalTargetForDealersAfterReview(SclUserModel sclUser, String subArea, String financialYear, BaseSiteModel baseSite) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {ann:totalReviewedTargetForAllDealers} from {DealerRevisedAnnualSales as ds JOIN AnnualSales as ann on {ds:annualSales}={ann.pk}} where {ds:subAreaMaster}=?subArea and {ann:salesOfficer}=?sclUser and {ann:financialYear}=?financialYear and {ann:isAnnualSalesReviewedForDealer}=?isAnnualSalesReviewedForDealer and {ds:brand}=?baseSite");
        params.put("sclUser", sclUser);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("financialYear", financialYear);
        params.put("isAnnualSalesReviewedForDealer",true);
        params.put("baseSite",baseSite);
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
    public double getTotalTargetForDealersAfterReviewForRH(DistrictMasterModel districtMasterModel, String financialYear, BaseSiteModel baseSite) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {ann:totalReviewedTargetForAllDealers} from {DealerRevisedAnnualSales as ds JOIN AnnualSales as ann on {ds:annualSales}={ann.pk}} where {ds:districtMaster}=?districtMasterModel and {ann:financialYear}=?financialYear and {ann:isAnnualSalesReviewedForDealer}=?isAnnualSalesReviewedForDealer and {ds:brand}=?baseSite");
        params.put("districtMasterModel",districtMasterModel);
        params.put("financialYear", financialYear);
        params.put("isAnnualSalesReviewedForDealer",true);
        params.put("baseSite",baseSite);
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
    public double getTotalTargetForDealersAfterReviewForTSMRH(SclUserModel sclUser, String subArea, String financialYear, String districtCode, String regionCode) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {ann:totalReviewedTargetForAllDealers} from {DealerRevisedAnnualSales as ds JOIN AnnualSales as ann on {ds:annualSales}={ann.pk}} where {ds:subAreaMaster}=?subArea and {ann:salesOfficer}=?sclUser and {ann:financialYear}=?financialYear and {ann:isAnnualSalesReviewedForDealer}=?isAnnualSalesReviewedForDealer");
        if(districtCode!=null){
            builder.append(" and {ds:districtMaster}=?districtCode ");
            params.put("districtCode",districtCode);
        }
        if(regionCode!=null){
            builder.append(" and {ds:regionMaster}=?regionCode ");
            params.put("regionCode",regionCode);
        }
        params.put("sclUser", sclUser);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("financialYear", financialYear);
        params.put("isAnnualSalesReviewedForDealer",true);
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
    public List<List<Object>> fetchProductSaleDetailsForSummaryAfterReview(String subArea, SclUserModel sclUser, BaseSiteModel baseSite) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({pp:totalTarget}), {pp:productName} from {ProductSale as pp} where {pp:subAreaMaster}=?subArea and {pp:salesOfficer}=?sclUser and {pp:isAnnualSalesReviewedForDealer}=?isAnnualSalesReviewedForDealer and {pp:brand}=?baseSite group by {pp:productName}");
            boolean isAnnualSalesReviewedForDealer = Boolean.TRUE;
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            params.put("sclUser", sclUser);
            params.put("isAnnualSalesReviewedForDealer",isAnnualSalesReviewedForDealer);
            params.put("baseSite",baseSite);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> fetchProductSaleDetailsForSummaryAfterReviewForRH(DistrictMasterModel districtMasterModel, BaseSiteModel baseSite) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({pp:totalTarget}), {pp:productName} from {ProductSale as pp} where {pp:districtMaster}=?districtMasterModel and {pp:isAnnualSalesReviewedForDealer}=?isAnnualSalesReviewedForDealer and {pp:brand}=?baseSite group by {pp:productName}");
            boolean isAnnualSalesReviewedForDealer = Boolean.TRUE;
            params.put("districtMasterModel",districtMasterModel);
            params.put("isAnnualSalesReviewedForDealer",isAnnualSalesReviewedForDealer);
            params.put("baseSite",baseSite);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> fetchProductSaleDetailsForSummaryAfterReviewForRH(List<DistrictMasterModel> districtMasterModel, BaseSiteModel baseSite) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({pp:totalTarget}), {pp:productName} from {ProductSale as pp} where {pp:districtMaster} in (?districtMasterModel) and {pp:isAnnualSalesReviewedForDealer}=?isAnnualSalesReviewedForDealer and {pp:brand}=?baseSite group by {pp:productName}");
            boolean isAnnualSalesReviewedForDealer = Boolean.TRUE;
            params.put("districtMasterModel",districtMasterModel);
            params.put("isAnnualSalesReviewedForDealer",isAnnualSalesReviewedForDealer);
            params.put("baseSite",baseSite);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> fetchProductSaleDetailsForSummaryAfterReviewForTSM(List<SubAreaMasterModel> subArea, BaseSiteModel baseSite) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({pp:totalTarget}), {pp:productName} from {ProductSale as pp} where {pp:subAreaMaster} in (?subArea) and  {pp:isAnnualSalesReviewedForDealer}=?isAnnualSalesReviewedForDealer and {pp:brand}=?baseSite group by {pp:productName}");
            boolean isAnnualSalesReviewedForDealer = Boolean.TRUE;
            params.put("subArea",subArea);
            params.put("isAnnualSalesReviewedForDealer",isAnnualSalesReviewedForDealer);
            params.put("baseSite",baseSite);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> fetchProductSaleDetailsForSummaryAfterReviewForTSMRH(String subArea, SclUserModel sclUser, String districtCode, String regionCode) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({pp:totalTarget}), {pp:productName} from {ProductSale as pp} where {pp:subAreaMaster}=?subArea and {pp:salesOfficer}=?sclUser and {pp:isAnnualSalesReviewedForDealer}=?isAnnualSalesReviewedForDealer group by {pp:productName}");
            if(districtCode!=null){
                builder.append(" and {pp:districtMaster}=?districtCode ");
                params.put("districtCode",districtCode);
            }
            if(regionCode!=null){
                builder.append(" and {pp:regionMaster}=?regionCode ");
                params.put("regionCode",regionCode);
            }
            boolean isAnnualSalesReviewedForDealer = Boolean.TRUE;
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            params.put("sclUser", sclUser);
            params.put("isAnnualSalesReviewedForDealer",isAnnualSalesReviewedForDealer);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public DealerRevisedMonthlySalesModel checkExistingDealerRevisedMonthlySales(SclUserModel sclUser, String customerCode, String subArea, String monthName, String monthYear) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {DealerRevisedMonthlySales as dp JOIN MonthlySales as m on {dp:monthlySales}={m:pk}} where {dp:customerCode}=?customerCode and {dp:subAreaMaster}=?subArea and {dp:monthName}=?monthName and {dp:monthYear}=?monthYear and {m:so}=?sclUser");
        params.put("sclUser", sclUser);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("customerCode",customerCode);
        params.put("monthName",monthName);
        params.put("monthYear",monthYear);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(DealerRevisedMonthlySalesModel.class));
        final SearchResult<DealerRevisedMonthlySalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public ProductSaleModel checkExistingProductSaleForDealerRevisedMonthlySales(String subArea, SclUserModel sclUser, String productCode, String customerCode, String monthName, String monthYear) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {ProductSale} where {customerCode}=?customerCode and {productCode}=?productCode and {subAreaMaster}=?subArea and {salesOfficer}=?sclUser and {monthName}=?monthName and {monthYear}=?monthYear and {isMonthlySalesForReviewedDealer}=?isMonthlySalesForReviewedDealer and {isMonthlySalesForPlannedDealer}=?isMonthlySalesForPlannedDealer");
        boolean isMonthlySalesForReviewedDealer=true;
        params.put("customerCode",customerCode);
        params.put("productCode",productCode);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("sclUser",sclUser);
        params.put("isMonthlySalesForReviewedDealer",isMonthlySalesForReviewedDealer);
        params.put("isMonthlySalesForPlannedDealer",false);
        params.put("monthName",monthName);
        params.put("monthYear",monthYear);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(ProductSaleModel.class));
        final SearchResult<ProductSaleModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public List<List<Object>> fetchDealerSaleDetailsForSummaryAfterTargetSetting(String subArea, SclUserModel sclUser) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({ds:totalTarget}),{d:code} from {DealerRevisedAnnualSales as ds JOIN SclCustomer as sc on {sc:uid}={ds:customerCode} JOIN DealerCategory as d on {sc:dealerCategory}={d:pk}} where {ds:subAreaMaster}=?subArea group by {d:code}");
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            //params.put("sclUser", sclUser);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> fetchDealerSaleDetailsForSummaryAfterTargetSettingForRH(DistrictMasterModel districtMasterModel) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({ds:totalTarget}),{d:code} from {DealerRevisedAnnualSales as ds JOIN SclCustomer as sc on {sc:uid}={ds:customerCode} JOIN DealerCategory as d on {sc:dealerCategory}={d:pk}} where {ds:districtMaster}=?districtMasterModel group by {d:code}");
            params.put("districtMasterModel",districtMasterModel);
            //params.put("sclUser", sclUser);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> fetchDealerSaleDetailsForSummaryAfterTargetSettingForTSMRH(String subArea, SclUserModel sclUser, String districtCode, String regionCode) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({ds:totalTarget}),{d:code} from {DealerRevisedAnnualSales as ds JOIN SclCustomer as sc on {sc:uid}={ds:customerCode} JOIN DealerCategory as d on {sc:dealerCategory}={d:pk}} where {ds:subAreaMaster}=?subArea group by {d:code}");
            if(districtCode!=null){
                builder.append(" and {ds:districtMaster}=?districtCode ");
                params.put("districtCode",districtCode);
            }
            if(regionCode!=null){
                builder.append(" and {ds:regionMaster}=?regionCode ");
                params.put("regionCode",regionCode);
            }
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            //params.put("sclUser", sclUser);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> fetchDealerSaleDetailsForSummaryForSummaryAfterReview(String subArea, SclUserModel sclUser, BaseSiteModel baseSite) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({ds:totalTarget}),{d:code} from {DealerRevisedAnnualSales as ds JOIN SclCustomer as sc on {sc:uid}={ds:customerCode} JOIN DealerCategory as d on {sc:dealerCategory}={d:pk}} where {ds:subAreaMaster}=?subArea and {ds:brand}=?baseSite group by {d:code}");
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            params.put("baseSite",baseSite);
            //params.put("sclUser", sclUser);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> fetchDealerSaleDetailsForSummaryForSummaryAfterReviewForRH(DistrictMasterModel districtMasterModel) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({ds:totalTarget}),{d:code} from {DealerRevisedAnnualSales as ds JOIN SclCustomer as sc on {sc:uid}={ds:customerCode} JOIN DealerCategory as d on {sc:dealerCategory}={d:pk}} where {ds:districtMaster}=?districtMasterModel group by {d:code}");
            params.put("districtMasterModel",districtMasterModel);
            //params.put("sclUser", sclUser);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> fetchDealerSaleDetailsForSummaryForSummaryAfterReviewForTSMRH(String subArea, SclUserModel sclUser,String districtCode,String regionCode) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({ds:totalTarget}),{d:code} from {DealerRevisedAnnualSales as ds JOIN SclCustomer as sc on {sc:uid}={ds:customerCode} JOIN DealerCategory as d on {sc:dealerCategory}={d:pk}} where {ds:subAreaMaster}=?subArea group by {d:code}");
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            if(districtCode!=null){
                builder.append(" and {o.districtMaster}=?districtCode ");
                params.put("districtCode",districtCode);
            }
            if(regionCode!=null){
                builder.append(" and {o.regionMaster}=?regionCode ");
                params.put("regionCode",regionCode);
            }
            //params.put("sclUser", sclUser);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> fetchDealerMixDetailsAfterTargetSetMonthlySummary(String subArea, SclUserModel sclUser, String month, String year) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({ds:revisedTarget}),{d:code} from {DealerPlannedMonthlySales as ds JOIN SclCustomer as sc on {sc:uid}={ds:customerCode} JOIN DealerCategory as d on {sc:dealerCategory}={d:pk}} where {ds:subAreaMaster}=?subArea group by {d:code}");
            params.put("subArea", territoryManagementService.getTerritoryById(subArea));
            //params.put("sclUser", sclUser);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> fetchDealerMixDetailsAfterTargetSetMonthlySummaryForRH(DistrictMasterModel districtMasterModel, String formattedMonth, String valueOf) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({ds:revisedTarget}),{d:code} from {DealerPlannedMonthlySales as ds JOIN SclCustomer as sc on {sc:uid}={ds:customerCode} JOIN DealerCategory as d on {sc:dealerCategory}={d:pk}} where {ds:districtMaster}=?districtMasterModel group by {d:code}");
            params.put("districtMasterModel", districtMasterModel);
            //params.put("sclUser", sclUser);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> fetchDealerMixDetailsAfterReviewMonthlySummary(String subArea, SclUserModel sclUser, String month, String year) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({ds:revisedTarget}),{d:code} from {DealerRevisedMonthlySales as ds JOIN SclCustomer as sc on {sc:uid}={ds:customerCode} JOIN DealerCategory as d on {sc:dealerCategory}={d:pk}} where {ds:subAreaMaster}=?subArea group by {d:code}");
            params.put("subArea", territoryManagementService.getTerritoryById(subArea));
            //params.put("sclUser", sclUser);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> fetchDealerMixDetailsAfterReviewMonthlySummaryForRH(DistrictMasterModel districtMasterModel, String formattedMonth, String valueOf) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({ds:revisedTarget}),{d:code} from {DealerRevisedMonthlySales as ds JOIN SclCustomer as sc on {sc:uid}={ds:customerCode} JOIN DealerCategory as d on {sc:dealerCategory}={d:pk}} where {ds:districtMaster}=?districtMasterModel group by {d:code}");
            params.put("districtMasterModel", districtMasterModel);
            //params.put("sclUser", sclUser);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class, String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<ProductSaleModel> checkExistingProductSaleForDealerRevisedMonthlySalesList(String subArea, SclUserModel sclUser, String productCode, String customerCode, String monthName, String monthYear) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {ProductSale} where {customerCode}=?customerCode and {productCode}=?productCode and {subAreaMaster}=?subArea and {salesOfficer}=?sclUser and {monthName}=?monthName and {monthYear}=?monthYear and {isMonthlySalesForReviewedDealer}=?isMonthlySalesForReviewedDealer and {isMonthlySalesForPlannedDealer}=?isMonthlySalesForPlannedDealer");
        boolean isMonthlySalesForReviewedDealer=true;
        params.put("customerCode",customerCode);
        params.put("productCode",productCode);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("sclUser",sclUser);
        params.put("isMonthlySalesForReviewedDealer",isMonthlySalesForReviewedDealer);
        params.put("isMonthlySalesForPlannedDealer",false);
        params.put("monthName",monthName);
        params.put("monthYear",monthYear);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(ProductSaleModel.class));
        final SearchResult<ProductSaleModel> searchResult = flexibleSearchService.search(query);
        List<ProductSaleModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public ProductSaleModel fetchProductSaleForDealerPlannedMonthlySales(String subArea, SclUserModel sclUser, String productCode, String customerCode, String monthName, String monthYear) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();

        if(sclUser!=null){
            if(sclUser.getUserType().getCode()!=null) {
                if (sclUser.getUserType().getCode().equalsIgnoreCase("SO")) {
                    builder.append("select {pk} from {ProductSale} where {customerCode}=?customerCode and {productCode}=?productCode and {subAreaMaster}=?subArea and {salesOfficer}=?sclUser and {monthName}=?monthName and {monthYear}=?monthYear and {isMonthlySalesForReviewedDealer}=?isMonthlySalesForReviewedDealer and {isMonthlySalesForPlannedDealer}=?isMonthlySalesForPlannedDealer");
                    params.put("sclUser", sclUser);
                } else if (sclUser.getUserType().getCode().equals("RH") || sclUser.getUserType().getCode().equalsIgnoreCase("TSM")) {
                    builder.append("select {pk} from {ProductSale} where {customerCode}=?customerCode and {productCode}=?productCode and {subAreaMaster}=?subArea and {monthName}=?monthName and {monthYear}=?monthYear and {isMonthlySalesForReviewedDealer}=?isMonthlySalesForReviewedDealer and {isMonthlySalesForPlannedDealer}=?isMonthlySalesForPlannedDealer");
                }
            }
        }
        else{
            builder.append("select {pk} from {ProductSale} where {customerCode}=?customerCode and {productCode}=?productCode and {subAreaMaster}=?subArea and {monthName}=?monthName and {monthYear}=?monthYear and {isMonthlySalesForReviewedDealer}=?isMonthlySalesForReviewedDealer and {isMonthlySalesForPlannedDealer}=?isMonthlySalesForPlannedDealer");
        }

        params.put("customerCode",customerCode);
        params.put("productCode",productCode);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("sclUser",sclUser);
        params.put("isMonthlySalesForReviewedDealer",false);
        params.put("isMonthlySalesForPlannedDealer",true);
        params.put("monthName",monthName);
        params.put("monthYear",monthYear);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(ProductSaleModel.class));
        final SearchResult<ProductSaleModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public ProductSaleModel fetchProductSaleForDealerRevisedMonthlySales(String subArea, SclUserModel sclUser, String productCode, String customerCode, String monthName, String monthYear) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        if(sclUser!=null){
            if(sclUser.getUserType().getCode()!=null) {
                if (sclUser.getUserType().getCode().equalsIgnoreCase("SO")) {
                    builder.append("select {pk} from {ProductSale} where {customerCode}=?customerCode and {productCode}=?productCode and {subAreaMaster}=?subArea and {salesOfficer}=?sclUser and {monthName}=?monthName and {monthYear}=?monthYear and {isMonthlySalesForReviewedDealer}=?isMonthlySalesForReviewedDealer and {isMonthlySalesForPlannedDealer}=?isMonthlySalesForPlannedDealer");
                    params.put("sclUser", sclUser);
                } else if (sclUser.getUserType().getCode().equals("RH") || sclUser.getUserType().getCode().equalsIgnoreCase("TSM")) {
                    builder.append("select {pk} from {ProductSale} where {customerCode}=?customerCode and {productCode}=?productCode and {subAreaMaster}=?subArea and {monthName}=?monthName and {monthYear}=?monthYear and {isMonthlySalesForReviewedDealer}=?isMonthlySalesForReviewedDealer and {isMonthlySalesForPlannedDealer}=?isMonthlySalesForPlannedDealer");
                }
            }
        }
        else{
            builder.append("select {pk} from {ProductSale} where {customerCode}=?customerCode and {productCode}=?productCode and {subAreaMaster}=?subArea and {monthName}=?monthName and {monthYear}=?monthYear and {isMonthlySalesForReviewedDealer}=?isMonthlySalesForReviewedDealer and {isMonthlySalesForPlannedDealer}=?isMonthlySalesForPlannedDealer");
        }

        params.put("customerCode",customerCode);
        params.put("productCode",productCode);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("isMonthlySalesForReviewedDealer",true);
        params.put("isMonthlySalesForPlannedDealer",false);
        params.put("monthName",monthName);
        params.put("monthYear",monthYear);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(ProductSaleModel.class));
        final SearchResult<ProductSaleModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public DealerRevisedAnnualSalesModel validateReviewForExistingDealersSale(String customerCode, String subArea, SclUserModel sclUser, String financialYear) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {ds:pk} from {DealerRevisedAnnualSales as ds JOIN AnnualSales as ann on {ds:annualSales}={ann.pk}} where {ds:subAreaMaster}=?subArea and {ann:salesOfficer}=?sclUser and {ann:financialYear}=?financialYear and {ds:customerCode}=?customerCode and {ds:isExistingDealerRevisedForReview}=?isExistingDealerRevisedForReview ");
        params.put("sclUser", sclUser);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("customerCode",customerCode);
        params.put("financialYear",financialYear);
        params.put("IsExistingDealerRevisedForReview",true);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(DealerRevisedAnnualSalesModel.class));
        final SearchResult<DealerRevisedAnnualSalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public MonthWiseAnnualTargetModel validateReviewForOnboardedDealersSaleSkuForMonthWise(String subArea, String customerCode, String productCode, String monthYear, SclUserModel sclUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {MonthWiseAnnualTarget} where {customerCode}=?customerCode and {productCode}=?productCode and {monthYear}=?monthYear and {salesOfficer}=?sclUser and {subAreaMaster}=?subArea and {isAnnualSalesOnboardedForDealer}=?isAnnualSalesOnboardedForDealer");
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("customerCode",customerCode);
        params.put("productCode",productCode);
        params.put("monthYear",monthYear);
        params.put("isAnnualSalesOnboardedForDealer",true);
        params.put("sclUser",sclUser);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0):null;
    }

    @Override
    public MonthWiseAnnualTargetModel validateReviewForOnboardedDealersSaleForMonthWise(String subArea, String customerCode, String monthYear, SclUserModel sclUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {MonthWiseAnnualTarget} where {customerCode}=?customerCode and {monthYear}=?monthYear and {salesOfficer}=?sclUser and {subAreaMaster}=?subArea and {isAnnualSalesOnboardedForDealer}=?isAnnualSalesOnboardedForDealer and {productCode} is null");
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("customerCode",customerCode);
        params.put("monthYear",monthYear);
        params.put("isAnnualSalesOnboardedForDealer",true);
        params.put("sclUser",sclUser);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0):null;
    }

    @Override
    public ProductSaleModel validateReviewForOnboardedDealerSkuSale(String subArea, String customerCode, String productCode, SclUserModel sclUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {ProductSale} where {customerCode}=?customerCode and {productCode}=?productCode and {subAreaMaster}=?subArea and {salesOfficer}=?sclUser and {isAnnualSalesOnboardedForDealer}=?isAnnualSalesOnboardedForDealer");
        params.put("customerCode",customerCode);
        params.put("productCode",productCode);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("sclUser",sclUser);
        params.put("isAnnualSalesOnboardedForDealer",true);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(ProductSaleModel.class));
        final SearchResult<ProductSaleModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public DealerRevisedAnnualSalesModel validateReviewForOnboardedDealersSale(String customerCode, String subArea, SclUserModel sclUser, String financialYear) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {ds:pk} from {DealerRevisedAnnualSales as ds JOIN AnnualSales as ann on {ds:annualSales}={ann.pk}} where {ds:subAreaMaster}=?subArea and {ann:salesOfficer}=?sclUser and {ann:financialYear}=?financialYear and {ds:customerCode}=?customerCode and {ds:isNewDealerOnboarded}=?isNewDealerOnboarded ");
        params.put("sclUser", sclUser);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("customerCode",customerCode);
        params.put("financialYear",financialYear);
        params.put("isNewDealerOnboarded",true);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(DealerRevisedAnnualSalesModel.class));
        final SearchResult<DealerRevisedAnnualSalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public MonthWiseAnnualTargetModel validateReviewForExistingDealersSaleForMonthWise(String subArea, String customerCode, String monthYear, SclUserModel sclUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {MonthWiseAnnualTarget} where {customerCode}=?customerCode and {monthYear}=?monthYear and {salesOfficer}=?sclUser and {subAreaMaster}=?subArea and {isAnnualSalesReviewedForDealer}=?isAnnualSalesReviewedForDealer and {productCode} is null");
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("customerCode",customerCode);
        params.put("monthYear",monthYear);
        params.put("isAnnualSalesReviewedForDealer",true);
        params.put("sclUser",sclUser);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0):null;
    }

    @Override
    public MonthWiseAnnualTargetModel validateReviewForExistingDealerSkuSaleForMonthWise(String subArea, String customerCode, String productCode, String monthYear, SclUserModel sclUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {MonthWiseAnnualTarget} where {customerCode}=?customerCode and {productCode}=?productCode and {monthYear}=?monthYear and {salesOfficer}=?sclUser and {subAreaMaster}=?subArea and {isAnnualSalesReviewedForDealer}=?isAnnualSalesReviewedForDealer");
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("customerCode",customerCode);
        params.put("productCode",productCode);
        params.put("monthYear",monthYear);
        params.put("isAnnualSalesReviewedForDealer",true);
        params.put("sclUser",sclUser);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0):null;
    }

    @Override
    public ProductSaleModel validateReviewForExistingDealerSkuSale(String subArea, String customerCode, String productCode, SclUserModel sclUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {ProductSale} where {customerCode}=?customerCode and {productCode}=?productCode and {subAreaMaster}=?subArea and {salesOfficer}=?sclUser and {isAnnualSalesReviewedForDealer}=?isAnnualSalesReviewedForDealer");
        params.put("customerCode", customerCode);
        params.put("productCode", productCode);
        params.put("subArea", territoryManagementService.getTerritoryById(subArea));
        params.put("sclUser", sclUser);
        params.put("isAnnualSalesReviewedForDealer", true);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(ProductSaleModel.class));
        final SearchResult<ProductSaleModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    public Double getMonthWiseAnnualTargetForDealer(SclUserModel sclUser, String dealerCode, String monthYear, List<SubAreaMasterModel> subAreas) {
        final Map<String, Object> params = new HashMap<>();
        String queryString="select {mt.monthTarget} from {MonthWiseAnnualTarget as mt} where {mt.subAreaMaster} in " +
                "(?subAreas) and {mt.salesOfficer}=?salesOfficer and {mt.customerCode}=?customerCode and {mt.monthYear}=?monthYear";
        params.put("customerCode",dealerCode);
        params.put("subAreas",subAreas);
        params.put("salesOfficer",sclUser);
        params.put("monthYear",monthYear);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        query.setResultClassList(List.of(Double.class));
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if(CollectionUtils.isNotEmpty(searchResult.getResult()) ){
            return searchResult.getResult().get(0);
        }
        return 0.0;
    }

    @Override
    public List<MonthWiseAnnualTargetModel> validateReviewForOnboardedDealersSaleSkuForMonthWise(String customerCode, String productCode, String subArea, SclUserModel sclUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {MonthWiseAnnualTarget} where {customerCode}=?customerCode and {productCode}=?productCode and {subAreaMaster}=?subArea and {isAnnualSalesOnboardedForDealer}=?isAnnualSalesOnboardedForDealer and {salesOfficer}=?sclUser");
        boolean isAnnualSalesOnboardedForDealer = Boolean.TRUE;
        params.put("customerCode", customerCode);
        params.put("productCode",productCode);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("isAnnualSalesOnboardedForDealer", isAnnualSalesOnboardedForDealer);
        params.put("sclUser",sclUser);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        List<MonthWiseAnnualTargetModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }
    @Override
    public List<MonthWiseAnnualTargetModel> validateReviewForOnboardedRetailerSaleForMonthWise(String customerCode, String retailerCode, String subArea, SclUserModel sclUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {MonthWiseAnnualTarget} where {customerCode}=?customerCode and {retailerCode}=?retailerCode and {subAreaMaster}=?subArea and {isAnnualSalesOnboardedForRetailer}=?isAnnualSalesOnboardedForRetailer and {salesOfficer}=?sclUser");
        boolean isAnnualSalesOnboardedForRetailer = Boolean.TRUE;
        params.put("customerCode", customerCode);
        params.put("retailerCode",retailerCode);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("isAnnualSalesOnboardedForRetailer", isAnnualSalesOnboardedForRetailer);
        params.put("sclUser",sclUser);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        List<MonthWiseAnnualTargetModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public SearchPageData<AnnualSalesModel> viewPlannedSalesforDealersRetailersMonthwise(SearchPageData searchPageData, String subArea, SclUserModel sclUser, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(ANNUAL_PLANNED_SALES_VIEW_QUERY);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("sclUser",sclUser);
        params.put("brand",brand);
        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(AnnualSalesModel.class));
        query.getQueryParameters().putAll(params);
        parameter.setFlexibleSearchQuery(query);
        return paginatedFlexibleSearchService.search(parameter);
    }

    @Override
    public SearchPageData<RetailerPlannedAnnualSalesModel> fetchRecordForRetailerPlannedAnnualSales(SearchPageData searchPageData, String subArea, SclUserModel sclUser, String filter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(RETAILER_PLANNED_ANNUAL_SALES);
        params.put("sclUser", sclUser);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("filter", "%" + filter.toUpperCase() + "%");
        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(RetailerPlannedAnnualSalesModel.class));
        query.getQueryParameters().putAll(params);
        parameter.setFlexibleSearchQuery(query);
        return paginatedFlexibleSearchService.search(parameter);
    }

    @Override
    public List<MonthWiseAnnualTargetModel> getMonthWiseSkuDetailsBeforeReview(String customerCode, String productCode, String subArea, SclUserModel sclUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {MonthWiseAnnualTarget} where {customerCode}=?customerCode and {productCode}=?productCode and {subAreaMaster}=?subArea and {isAnnualSalesRevisedForDealer}=?isAnnualSalesRevisedForDealer and {salesOfficer}=?sclUser");
        boolean isAnnualSalesRevisedForDealer = Boolean.TRUE;
        params.put("customerCode", customerCode);
        params.put("productCode",productCode);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("isAnnualSalesRevisedForDealer", isAnnualSalesRevisedForDealer);
        params.put("sclUser",sclUser);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        List<MonthWiseAnnualTargetModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }
    @Override
    public List<MonthWiseAnnualTargetModel> getMonthWiseRetailerDetailsBeforeReview(String dealerCustomerCode, String retailerCustomerCode, String subArea, SclUserModel sclUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {MonthWiseAnnualTarget} where {customerCode}=?dealerCustomerCode and {retailerCode}=?retailerCustomerCode and {subAreaMaster}=?subArea and {isAnnualSalesRevisedForRetailer}=?isAnnualSalesRevisedForRetailer and {salesOfficer}=?sclUser and {productCode} is null and {selfCounterCustomerCode} is null");
        boolean isAnnualSalesRevisedForRetailer = Boolean.TRUE;
        params.put("dealerCustomerCode", dealerCustomerCode);
        params.put("retailerCustomerCode",retailerCustomerCode);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("isAnnualSalesRevisedForRetailer", isAnnualSalesRevisedForRetailer);
        params.put("sclUser",sclUser);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        List<MonthWiseAnnualTargetModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public ProductSaleModel fetchDealerPlannedAnnualSaleSkuDetails(String customerCode, String productCode, String subArea,SclUserModel sclUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {ProductSale} where {customerCode}=?customerCode and {productCode}=?productCode and {subAreaMaster}=?subArea and {salesOfficer}=?sclUser and {isAnnualSalesRevisedForDealer}=?isAnnualSalesRevisedForDealer");
        params.put("customerCode",customerCode);
        params.put("productCode",productCode);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("sclUser",sclUser);
        params.put("isAnnualSalesRevisedForDealer",false);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(ProductSaleModel.class));
        final SearchResult<ProductSaleModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public DealerRevisedAnnualSalesModel fetchRecordForDealerRevisedAnnualSalesByCode(String subArea, SclUserModel sclUser, String filter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {ds:pk} from {DealerRevisedAnnualSales as ds JOIN AnnualSales as ann on {ds:annualSales}={ann.pk}} where {ds:subAreaMaster}=?subArea and {ann:salesOfficer}=?sclUser and {ann:isAnnualSalesRevised}=?isAnnualSalesRevised and UPPER({ds:customerCode}) like ?filter");
        params.put("sclUser", sclUser);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("filter","%" + filter.toUpperCase() + "%");
        params.put("isAnnualSalesRevised",true);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(DealerRevisedAnnualSalesModel.class));
        final SearchResult<DealerRevisedAnnualSalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public List<MonthWiseAnnualTargetModel> getMonthWiseAnnualTargetDetailsForDealerRevised(String customerCode, String productCode, String subArea) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {MonthWiseAnnualTarget} where {customerCode}=?customerCode and {productCode}=?productCode and {subAreaMaster}=?subArea and {isAnnualSalesRevisedForDealer}=?isAnnualSalesRevisedForDealer");
        params.put("customerCode", customerCode);
        params.put("productCode",productCode);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("isAnnualSalesRevisedForDealer", true);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        List<MonthWiseAnnualTargetModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public double getRetailerCySale(String dealerCode, String retailerCode, String subArea) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select sum({sale}) from {RetailerSalesSummary} where {dealerCode}=?dealerCode and {retailerCode}=?retailerCode and {subArea}=?subArea");
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("dealerCode", dealerCode);
        params.put("retailerCode",retailerCode);
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
    public List<List<Object>> getMonthSplitupFormDealerRevisedAnnualSales(String customerCode, SclUserModel sclUser, String subArea) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select {m:monthYear}, {m:monthTarget} from {DealerRevisedAnnualSales as ds JOIN MonthWiseAnnualTarget as m on {m:dealerRevisedAnnualSales}={ds:pk}} where {m:customerCode}=?customerCode and {m:productCode} is null and {m:subAreaMaster}=?subArea and {m:isAnnualSalesRevisedForDealer}=?isAnnualSalesRevisedForDealer and {m:isAnnualSalesOnboardedForDealer}=?isAnnualSalesOnboardedForDealer and {m:isAnnualSalesReviewedForDealer}=?isAnnualSalesReviewedForDealer and {m:retailerCode} is null and {m:selfCounterCustomerCode} is null ");
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            params.put("sclUser", sclUser);
            params.put("customerCode",customerCode);
            params.put("isAnnualSalesRevisedForDealer",true);
            params.put("isAnnualSalesOnboardedForDealer",false);
            params.put("isAnnualSalesReviewedForDealer",false);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class, Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> getRetailerDetailsByDealerCode(String subArea, String dealerCode,Date startDate, Date endDate) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select {retailerCode} , sum({sale}) from {RetailerSalesSummary} where {startDate}>=?startDate and {endDate}<=?endDate and {subArea}=?subArea and {dealerCode}=?dealerCode group by {retailerCode}");
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            params.put("dealerCode",dealerCode);
            params.put("startDate",startDate);
            params.put("endDate",endDate);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class, Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public RetailerRevisedAnnualSalesModel validateReviewForExistingRetailersSale(String dealerCode, String subArea, SclUserModel sclUser, String financialYear) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {rs:pk} from {RetailerRevisedAnnualSales as rs JOIN AnnualSales as ann on {rs:annualSales}={ann.pk}} where {rs:subAreaMaster}=?subArea and {ann:salesOfficer}=?sclUser and {ann:financialYear}=?financialYear and {rs:customerCode}=?dealerCode and {rs:isExistingRetailerRevisedForReview}=?isExistingRetailerRevisedForReview ");
        params.put("sclUser", sclUser);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("dealerCode",dealerCode);
        params.put("financialYear",financialYear);
        params.put("isExistingRetailerRevisedForReview",true);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(RetailerRevisedAnnualSalesModel.class));
        final SearchResult<RetailerRevisedAnnualSalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public MonthWiseAnnualTargetModel validateReviewForExistingRetailersSaleForMonthWise(String subArea, String dealerCode, String monthYear, SclUserModel sclUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {MonthWiseAnnualTarget} where {customerCode}=?dealerCode and {monthYear}=?monthYear and {salesOfficer}=?sclUser and {subAreaMaster}=?subArea and {isAnnualSalesReviewedForDealer}=?isAnnualSalesReviewedForDealer and {productCode} is null and {retailerCode} is null and {selfCounterCustomerCode} is null");
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("dealerCode",dealerCode);
        params.put("monthYear",monthYear);
        params.put("isAnnualSalesReviewedForDealer",true);
        params.put("sclUser",sclUser);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0):null;
    }

    @Override
    public SelfCounterSaleDetailsModel validateReviewForExistingSelfCounterSale(String subArea, String selfCounterCode, SclUserModel sclUser) {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select {pk} from {SelfCounterSaleDetails} where {customerCode}=?selfCounterCode and {salesOfficer}=?sclUser and {subAreaMaster}=?subArea and {isAnnualSalesRevisedForRetailer}=?isAnnualSalesRevisedForRetailer and {isAnnualSalesReviewedForRetailer}=?isAnnualSalesReviewedForRetailer");
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            params.put("selfCounterCode",selfCounterCode);
            params.put("sclUser",sclUser);
            params.put("isAnnualSalesRevisedForRetailer",false);
            params.put("isAnnualSalesReviewedForRetailer",true);

            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(SelfCounterSaleDetailsModel.class));
            final SearchResult<SelfCounterSaleDetailsModel> searchResult = flexibleSearchService.search(query);
            return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public MonthWiseAnnualTargetModel validateReviewForExistingSelfCounterSaleMonthWise(String subArea, String selfCounterCode, String monthYear, SclUserModel sclUser, String dealerCode) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {MonthWiseAnnualTarget} where {selfCounterCustomerCode}=?selfCounterCode and {customerCode}=?dealerCode and {monthYear}=?monthYear and {salesOfficer}=?sclUser and {subAreaMaster}=?subArea and {isAnnualSalesReviewedForSelfCounter}=?isAnnualSalesReviewedForSelfCounter and {productCode} is null and {retailerCode} is null");
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("selfCounterCode",selfCounterCode);
        params.put("dealerCode",dealerCode);
        params.put("monthYear",monthYear);
        params.put("isAnnualSalesReviewedForSelfCounter",true);
        params.put("sclUser",sclUser);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0):null;
    }

    @Override
    public RetailerRevisedAnnualSalesDetailsModel validateReviewForExistingRetailerDetailsSale(String subArea, String retailerCode, SclUserModel sclUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {RetailerRevisedAnnualSalesDetails} where {customerCode}=?retailerCode and {salesOfficer}=?sclUser and {subAreaMaster}=?subArea and {isAnnualSalesReviewedForRetailer}=?isAnnualSalesReviewedForRetailer");
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("retailerCode",retailerCode);
        params.put("sclUser",sclUser);
        params.put("isAnnualSalesReviewedForRetailer",true);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(RetailerRevisedAnnualSalesDetailsModel.class));
        final SearchResult<RetailerRevisedAnnualSalesDetailsModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public MonthWiseAnnualTargetModel validateReviewForExistingRetailerDetailSaleForMonthWise(String subArea, String retailerCode, String monthYear, SclUserModel sclUser, String dealerCode) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {MonthWiseAnnualTarget} where {retailerCode}=?retailerCode and {customerCode}=?dealerCode and {monthYear}=?monthYear and {salesOfficer}=?sclUser and {subAreaMaster}=?subArea and {isAnnualSalesReviewedForRetailerDetails}=?isAnnualSalesReviewedForRetailerDetails and {productCode} is null and {selfCounterCustomerCode} is null");
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("retailerCode",retailerCode);
        params.put("dealerCode",dealerCode);
        params.put("monthYear",monthYear);
        params.put("isAnnualSalesReviewedForRetailerDetails",true);
        params.put("sclUser",sclUser);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0):null;
    }

    @Override
    public MonthWiseAnnualTargetModel validateReviewForOnboardRetailerDetailSaleForMonthWise(String subArea, String retailerCode, String monthYear, SclUserModel sclUser,String dealerCode) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {MonthWiseAnnualTarget} where {retailerCode}=?retailerCode and {customerCode}=?dealerCode and {monthYear}=?monthYear and {salesOfficer}=?sclUser and {subAreaMaster}=?subArea and {isAnnualSalesOnboardedForRetailer}=?isAnnualSalesOnboardedForRetailer and {productCode} is null and {selfCounterCustomerCode} is null");
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("retailerCode",retailerCode);
        params.put("dealerCode",dealerCode);
        params.put("monthYear",monthYear);
        params.put("isAnnualSalesOnboardedForRetailer",true);
        params.put("sclUser",sclUser);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0):null;
    }

    @Override
    public RetailerRevisedAnnualSalesDetailsModel validateReviewForOnboardRetailerDetailsSale(String subArea, String retailerCode, SclUserModel sclUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {RetailerRevisedAnnualSalesDetails} where {customerCode}=?retailerCode and {salesOfficer}=?sclUser and {subAreaMaster}=?subArea and {isNewRetailerOnboarded}=?isNewRetailerOnboarded");
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("retailerCode",retailerCode);
        params.put("sclUser",sclUser);
        params.put("isNewRetailerOnboarded",true);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(RetailerRevisedAnnualSalesDetailsModel.class));
        final SearchResult<RetailerRevisedAnnualSalesDetailsModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public MonthWiseAnnualTargetModel validateOnboardedRetailerSelfCounterSaleMonthWise(String subArea, String selfCounterCode, String monthYear, SclUserModel sclUser, String dealerCode) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {MonthWiseAnnualTarget} where {selfCounterCustomerCode}=?selfCounterCode and {customerCode}=?dealerCode and {monthYear}=?monthYear and {salesOfficer}=?sclUser and {subAreaMaster}=?subArea and {isAnnualSalesOnboardedForRetailer}=?isAnnualSalesOnboardedForRetailer and {productCode} is null and {retailerCode} is null");
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("dealerCode",dealerCode);
        params.put("selfCounterCode",selfCounterCode);
        params.put("monthYear",monthYear);
        params.put("isAnnualSalesOnboardedForRetailer",true);
        params.put("sclUser",sclUser);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0):null;
    }

    @Override
    public SelfCounterSaleDetailsModel validateOnboardedRetailerSelfCounterSale(String subArea, String selfCounterCode, SclUserModel sclUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {SelfCounterSaleDetails} where {customerCode}=?selfCounterCode and {salesOfficer}=?sclUser and {subAreaMaster}=?subArea and {isAnnualSalesOnboardedForRetailer}=?isAnnualSalesOnboardedForRetailer");
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("selfCounterCode",selfCounterCode);
        params.put("sclUser",sclUser);
        params.put("isAnnualSalesOnboardedForRetailer",true);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(SelfCounterSaleDetailsModel.class));
        final SearchResult<SelfCounterSaleDetailsModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public MonthWiseAnnualTargetModel validateReviewForOnboardedRetailersSaleForMonthWise(String subArea, String dealerCode, String monthYear, SclUserModel sclUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {MonthWiseAnnualTarget} where {customerCode}=?dealerCode and {monthYear}=?monthYear and {salesOfficer}=?sclUser and {subAreaMaster}=?subArea and {isAnnualSalesOnboardedForRetailer}=?isAnnualSalesOnboardedForRetailer and {productCode} is null and {retailerCode} is null and {selfCounterCustomerCode} is null");
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("dealerCode",dealerCode);
        params.put("monthYear",monthYear);
        params.put("isAnnualSalesOnboardedForRetailer",true);
        params.put("sclUser",sclUser);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(MonthWiseAnnualTargetModel.class));
        final SearchResult<MonthWiseAnnualTargetModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0):null;
    }

    @Override
    public RetailerRevisedAnnualSalesModel validateReviewForOnboardedRetailersSale(String dealerCode, String subArea, SclUserModel sclUser, String financialYear) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {rs:pk} from {RetailerRevisedAnnualSales as rs JOIN AnnualSales as ann on {rs:annualSales}={ann.pk}} where {rs:subAreaMaster}=?subArea and {ann:salesOfficer}=?sclUser and {ann:financialYear}=?financialYear and {rs:customerCode}=?dealerCode and {rs:isNewDealerOnboarded}=?isNewDealerOnboarded ");
        params.put("sclUser", sclUser);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("dealerCode",dealerCode);
        params.put("financialYear",financialYear);
        params.put("isNewDealerOnboarded",true);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(RetailerRevisedAnnualSalesModel.class));
        final SearchResult<RetailerRevisedAnnualSalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public List<String> getRetailerListByDealerCode(String dealerCode, String subArea) {
        try {
                final Map<String, Object> params = new HashMap<String, Object>();
                final StringBuilder builder = new StringBuilder("select distinct{retailerCode} from {RetailerSalesSummary} where {dealerCode}=?dealerCode and {subArea}=?subArea");
                params.put("dealerCode", dealerCode);
                params.put("subArea",territoryManagementService.getTerritoryById(subArea));
                final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
                query.addQueryParameters(params);
                query.setResultClassList(Arrays.asList(String.class));
                final SearchResult<String> searchResult = flexibleSearchService.search(query);
                List<String> result = searchResult.getResult();
                return result!=null && !result.isEmpty() ? result : Collections.emptyList();
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

    public PaginatedFlexibleSearchService getPaginatedFlexibleSearchService() {
        return paginatedFlexibleSearchService;
    }

    public void setPaginatedFlexibleSearchService(PaginatedFlexibleSearchService paginatedFlexibleSearchService) {
        this.paginatedFlexibleSearchService = paginatedFlexibleSearchService;
    }

	@Override
	public Double getDealerSalesMonthlyTarget(String dealerUid, String monthYear, String monthName) {
		final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {revisedTarget} FROM {DealerRevisedMonthlySales} WHERE {customerCode}=?dealerUid AND {monthYear}=?monthYear AND {monthName}=?monthName");
        params.put("dealerUid", dealerUid);
        params.put("monthYear",monthYear);
        params.put("monthName",monthName);
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
	public Double getDealerSalesAnnualTarget(String dealerUid, String monthYear) {
		final Map<String, Object> params = new HashMap<>();
	    String queryString="select {mt.monthTarget} from {MonthWiseAnnualTarget as mt} where {mt.customerCode}=?customerCode and {mt.monthYear}=?monthYear and {mt.isAnnualSalesReviewedForDealer}=?truee and {mt.isAnnualSalesRevisedForDealer}=?falsee and {mt.retailerCode} is null and {mt.productCode} is null and {selfCounterCustomerCode} is null";
	    boolean truee = Boolean.TRUE;
	    boolean falsee = Boolean.FALSE;
	    params.put("customerCode",dealerUid);
	    params.put("monthYear",monthYear);
	    params.put("truee",truee);
	    params.put("falsee",falsee);
	    final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
	    query.addQueryParameters(params);
	    query.setResultClassList(List.of(Double.class));
	    final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
	}
    @Override
    public AnnualSalesModel getAnnualSalesModelDetailsForTSM(String financialYear, DistrictMasterModel districtMaster, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {ann:pk} FROM {AnnualSales as ann} where {ann:financialYear}=?financialYear and {ann:brand}=?brand and {ann:districtMaster}=?districtMaster");
        params.put("financialYear",financialYear);
        params.put("districtMaster",districtMaster);
        params.put("brand",brand);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(AnnualSalesModel.class));
        final SearchResult<AnnualSalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }
    @Override
    public AnnualSalesModel getAnnualSalesModelDetailsForRH( String financialYear, RegionMasterModel regionMaster, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {ann:pk} FROM {AnnualSales as ann} where {ann:financialYear}=?financialYear and {ann:brand}=?brand and {ann:regionMaster}=?regionMaster");
        params.put("financialYear",financialYear);
        params.put("regionMaster",regionMaster);
        params.put("brand",brand);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(AnnualSalesModel.class));
        final SearchResult<AnnualSalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public MonthlySalesModel getMonthlySalesModelDetailsForTSM(String month, String year, DistrictMasterModel districtMaster, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {m:pk} FROM {MonthlySales as m} where {m:monthName}=?month and {m:monthYear}=?year and {m:brand}=?brand and {m:districtMaster}=?districtMaster");
        params.put("month",month);
        params.put("year",year);
        params.put("districtMaster",districtMaster);
        params.put("brand",brand);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(MonthlySalesModel.class));
        final SearchResult<MonthlySalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }
    @Override
    public MonthlySalesModel getMonthlySalesModelDetailsForRH(String month, String year, RegionMasterModel regionMaster, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {m:pk} FROM {MonthlySales as m} where {m:monthName}=?month and {m:monthYear}=?year and {m:brand}=?brand and {m:regionMaster}=?regionMaster");
        params.put("month",month);
        params.put("year",year);
        params.put("regionMaster",regionMaster);
        params.put("brand",brand);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(MonthlySalesModel.class));
        final SearchResult<MonthlySalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    public List<List<Object>> getMonthwiseTargetsForSubarea(String subArea) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({m:monthTarget}), {m:monthYear} from {MonthWiseAnnualTarget as m} where {m:subAreaMaster}=?subArea and {m:isAnnualSalesRevisedForDealer}=?isAnnualSalesRevisedForDealer and {m:customerCode} is not null and {m:retailerCode} is null and {m:selfCounterCustomerCode} is null and {m:productCode} is null and {m:brand}=?brand group by {m:monthYear}  ORDER BY CONVERT(datetime, '01-' + {m:monthYear})");
//            ORDER BY CONVERT(datetime, '01-' + {m:monthYear})

            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            params.put("isAnnualSalesRevisedForDealer", true);
            params.put("brand", baseSiteService.getCurrentBaseSite());
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class,String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> getMonthwiseSkuTargetsForSubarea(String productCode, String subArea) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({m:monthTarget}), {m:monthYear} from {MonthWiseAnnualTarget as m} where {m:subAreaMaster}=?subArea and {m:isAnnualSalesRevisedForDealer}=?isAnnualSalesRevisedForDealer and {m:productCode} =?productCode and {m:customerCode} is not null and {m:retailerCode} is null and {m:selfCounterCustomerCode} is null and {m:brand}=?brand group by {m:monthYear} ORDER BY CONVERT(datetime, '01-' + {m:monthYear})");
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            params.put("isAnnualSalesRevisedForDealer", true);
            params.put("brand", baseSiteService.getCurrentBaseSite());
            params.put("productCode", productCode);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class,String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<String> getSkuListForFinalizedTargets(String subArea) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select distinct{m:productCode} from {MonthWiseAnnualTarget as m} where {m:subAreaMaster}=?subArea and {m:isAnnualSalesRevisedForDealer}=1 and {m:productCode} is not null and {m:customerCode} is not null and {m:retailerCode} is null and {m:selfCounterCustomerCode} is null and {m:brand}=?brand");
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            params.put("isAnnualSalesRevisedForDealer", true);
            params.put("brand", baseSiteService.getCurrentBaseSite());
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class));
            final SearchResult<String> searchResult = flexibleSearchService.search(query);
            List<String> result = searchResult.getResult();
            return result!=null && !result.isEmpty() ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<AnnualSalesModel> getAnnualSalesModelDetailsForDistrict(String financialYear, DistrictMasterModel districtMaster, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {ann:pk} FROM {AnnualSales as ann} where {ann:financialYear}=?financialYear and {ann:brand}=?brand and {ann:districtMaster}=?districtMaster");
        params.put("financialYear",financialYear);
        params.put("districtMaster",districtMaster);
        params.put("brand",brand);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(AnnualSalesModel.class));
        final SearchResult<AnnualSalesModel> searchResult = flexibleSearchService.search(query);
        List<AnnualSalesModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public List<List<Object>> getMonthwiseTargetsForTerritory(SubAreaMasterModel subArea, DistrictMasterModel district) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({m:monthTarget}), {m:monthYear} from {MonthWiseAnnualTarget as m} where {m:subAreaMaster}=?subArea and {m:districtMaster}=?districtMaster and {m:isAnnualSalesRevisedForDealer}=?isAnnualSalesRevisedForDealer and {m:customerCode} is not null and {m:retailerCode} is null and {m:selfCounterCustomerCode} is null and {m:productCode} is null and {m:brand}=?brand group by {m:monthYear}");
            params.put("subArea",subArea);
            params.put("isAnnualSalesRevisedForDealer", true);
            params.put("brand", baseSiteService.getCurrentBaseSite());
            params.put("districtMaster",district);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class,String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> getMonthwiseSkuTargetsForTerritory(String productCode, SubAreaMasterModel subArea, DistrictMasterModel district) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({m:monthTarget}), {m:monthYear} from {MonthWiseAnnualTarget as m} where {m:subAreaMaster}=?subArea and {m:districtMaster}=?districtMaster and {m:isAnnualSalesRevisedForDealer}=?isAnnualSalesRevisedForDealer and {m:productCode} =?productCode and {m:customerCode} is not null and {m:retailerCode} is null and {m:selfCounterCustomerCode} is null and {m:brand}=?brand group by {m:monthYear}");
            params.put("subArea",subArea);
            params.put("isAnnualSalesRevisedForDealer", true);
            params.put("brand", baseSiteService.getCurrentBaseSite());
            params.put("productCode", productCode);
            params.put("districtMaster",district);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class,String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<String> getSkuListForTerritory(SubAreaMasterModel subArea, DistrictMasterModel district) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select distinct{m:productCode} from {MonthWiseAnnualTarget as m} where {m:subAreaMaster}=?subArea and {m:districtMaster}=?districtMaster and {m:isAnnualSalesRevisedForDealer}=1 and {m:productCode} is not null and {m:customerCode} is not null and {m:retailerCode} is null and {m:selfCounterCustomerCode} is null and {m:brand}=?brand");
            params.put("subArea",subArea);
            params.put("isAnnualSalesRevisedForDealer", true);
            params.put("brand", baseSiteService.getCurrentBaseSite());
            params.put("districtMaster",district);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class));
            final SearchResult<String> searchResult = flexibleSearchService.search(query);
            List<String> result = searchResult.getResult();
            return result!=null && !result.isEmpty() ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> getMonthwiseTargetsForDistrict(DistrictMasterModel districtMaster) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({m:monthTarget}), {m:monthYear} from {MonthWiseAnnualTarget as m} where {m:districtMaster}=?districtMaster and {m:isAnnualSalesRevisedForDealer}=?isAnnualSalesRevisedForDealer and {m:customerCode} is not null and {m:retailerCode} is null and {m:selfCounterCustomerCode} is null and {m:productCode} is null and {m:brand}=?brand group by {m:monthYear} ORDER BY CONVERT(datetime, '01-' + {m:monthYear})");
            params.put("districtMaster",districtMaster);
            params.put("isAnnualSalesRevisedForDealer", true);
            params.put("brand", baseSiteService.getCurrentBaseSite());
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class,String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> getMonthwiseSkuTargetsForDistrict(String productCode, DistrictMasterModel districtMaster) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({m:monthTarget}), {m:monthYear} from {MonthWiseAnnualTarget as m} where {m:districtMaster}=?districtMaster and {m:isAnnualSalesRevisedForDealer}=?isAnnualSalesRevisedForDealer and {m:productCode} =?productCode and {m:customerCode} is not null and {m:retailerCode} is null and {m:selfCounterCustomerCode} is null and {m:brand}=?brand group by {m:monthYear} ORDER BY CONVERT(datetime, '01-' + {m:monthYear})");
            params.put("districtMaster",districtMaster);
            params.put("isAnnualSalesRevisedForDealer", true);
            params.put("brand", baseSiteService.getCurrentBaseSite());
            params.put("productCode", productCode);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class,String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<String> getSkuListForFinalizedTargets(DistrictMasterModel districtMaster) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select distinct{m:productCode} from {MonthWiseAnnualTarget as m} where {m:districtMaster}=?districtMaster and {m:isAnnualSalesRevisedForDealer}=1 and {m:productCode} is not null and {m:customerCode} is not null and {m:retailerCode} is null and {m:selfCounterCustomerCode} is null and {m:brand}=?brand");
            params.put("districtMaster",districtMaster);
            params.put("isAnnualSalesRevisedForDealer", true);
            params.put("brand", baseSiteService.getCurrentBaseSite());
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class));
            final SearchResult<String> searchResult = flexibleSearchService.search(query);
            List<String> result = searchResult.getResult();
            return result!=null && !result.isEmpty() ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<List<Object>> getMonthwiseSkuTargetsForSubareaTsm(String productCode, String subArea) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select sum({m:monthTarget}), {m:monthYear} from {MonthWiseAnnualTarget as m} where {m:subAreaMaster}=?subArea and {m:isAnnualSalesRevisedForDealer}=?isAnnualSalesRevisedForDealer and {m:productCode} =?productCode and {m:customerCode} is not null and {m:retailerCode} is null and {m:selfCounterCustomerCode} is null and {m:brand}=?brand group by {m:monthYear}");
            params.put("subArea",territoryManagementService.getTerritoryById(subArea));
            params.put("isAnnualSalesRevisedForDealer", true);
            params.put("brand", baseSiteService.getCurrentBaseSite());
            params.put("productCode", productCode);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Double.class,String.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<ProductSaleModel> getSalesForNewSku(String customerCode, String subArea, SclUserModel sclUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {ProductSale} where {customerCode}=?customerCode and {subAreaMaster}=?subArea and {salesOfficer}=?sclUser and {isAnnualSalesRevisedForDealer}=?isAnnualSalesRevisedForDealer and {isNewSku}=?isNewSku");
        params.put("customerCode",customerCode);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("sclUser",sclUser);
        params.put("isAnnualSalesRevisedForDealer", false);
        params.put("isNewSku",true);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(ProductSaleModel.class));
        final SearchResult<ProductSaleModel> searchResult = flexibleSearchService.search(query);
        List<ProductSaleModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public List<ProductSaleModel> getSalesForSkus(String customerCode, String subArea, SclUserModel sclUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {ProductSale} where {customerCode}=?customerCode and {subAreaMaster}=?subArea and {salesOfficer}=?sclUser and {isAnnualSalesRevisedForDealer}=?isAnnualSalesRevisedForDealer");
        params.put("customerCode",customerCode);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("sclUser",sclUser);
        params.put("isAnnualSalesRevisedForDealer", false);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(ProductSaleModel.class));
        final SearchResult<ProductSaleModel> searchResult = flexibleSearchService.search(query);
        List<ProductSaleModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public List<MonthlySalesModel> getMonthlySalesModelDetailsListForDO(String month, String year, List<SubAreaMasterModel> subAreaList, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {ms:pk} FROM {MonthlySales as ms} WHERE {ms:monthName}=?month and {ms:monthYear}=?year and {ms:subAreaMaster} in (?subArea) and {ms:brand}=?brand");
        params.put("month",month);
        params.put("year",year);
        params.put("subArea",subAreaList);
        params.put("brand",brand);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(MonthlySalesModel.class));
        final SearchResult<MonthlySalesModel> searchResult = flexibleSearchService.search(query);
        List<MonthlySalesModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public AnnualSalesModel getAnnualSalesModelDetails1(SclUserModel sclUser, String financialYear, String subArea, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {ann:pk} FROM {AnnualSales as ann} where {ann:salesOfficer}=?sclUser and {ann:subAreaMaster}=?subArea and {ann:financialYear}=?financialYear and {ann:brand}=?brand");
        params.put("sclUser",sclUser);
        params.put("financialYear",financialYear);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("brand",brand);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(AnnualSalesModel.class));
        final SearchResult<AnnualSalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public AnnualSalesModel viewPlannedSalesforDealersRetailersMonthwise1(String subArea, SclUserModel sclUser, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {ann:pk} FROM {AnnualSales as ann} where {ann:salesOfficer}=?sclUser and {ann:subAreaMaster}=?subArea and {ann:brand}=?brand");
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("sclUser",sclUser);
        params.put("brand",brand);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(AnnualSalesModel.class));
        final SearchResult<AnnualSalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public MonthlySalesModel getMonthlySalesModelDetail1(SclUserModel sclUser, String month, String year, String subArea, BaseSiteModel brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        if(sclUser!=null){
            if(sclUser.getUserType().getCode()!=null) {
                if (sclUser.getUserType().getCode().equalsIgnoreCase("SO")) {
                    builder.append("SELECT {ms:pk} FROM {MonthlySales as ms} WHERE {ms:so}=?sclUser and {ms:monthName}=?month and {ms:monthYear}=?year and {ms:subAreaMaster}=?subArea and {ms:brand}=?brand");
                    params.put("sclUser", sclUser);
                } else if (sclUser.getUserType().getCode().equals("RH") || sclUser.getUserType().getCode().equalsIgnoreCase("TSM")) {
                    builder.append("SELECT {ms:pk} FROM {MonthlySales as ms} WHERE {ms:monthName}=?month and {ms:monthYear}=?year and {ms:subAreaMaster}=?subArea and {ms:brand}=?brand");
                }
            }
        }
        else{
            builder.append("SELECT {ms:pk} FROM {MonthlySales as ms} WHERE {ms:monthName}=?month and {ms:monthYear}=?year and {ms:subAreaMaster}=?subArea and {ms:brand}=?brand");
        }

        params.put("month",month);
        params.put("year",year);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        params.put("brand",brand);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(MonthlySalesModel.class));
        final SearchResult<MonthlySalesModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public AnnualSalesModel viewAnnualSalesModelForDistrict(String district, SclUserModel sclUser, BaseSiteModel brand) {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("SELECT {ann:pk} FROM {AnnualSales as ann} where {ann:salesOfficer}=?sclUser and {ann:districtMaster}=?district and {ann:brand}=?brand");
            params.put("district",getDistrictByCode(district));
            params.put("sclUser",sclUser);
            params.put("brand",brand);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Collections.singletonList(AnnualSalesModel.class));
            final SearchResult<AnnualSalesModel> searchResult = flexibleSearchService.search(query);
            return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    public DistrictMasterModel getDistrictByCode(String district) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final String queryString = "SELECT {pk} FROM {DistrictMaster} WHERE {code} = ?district";
        params.put("district", district);

        FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<DistrictMasterModel> searchResult = flexibleSearchService.search(query);
        if (CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult().get(0);
        }
        return null;
    }
}
