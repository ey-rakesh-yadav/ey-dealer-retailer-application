package com.eydms.core.dao.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.dao.EfficacyReportDao;
import com.eydms.core.enums.CounterType;
import com.eydms.core.enums.NetworkType;
import com.eydms.core.enums.VisitStatus;
import com.eydms.core.model.CounterVisitMasterModel;
import com.eydms.core.model.CronjobsDateLogModel;
import com.eydms.core.model.EfficacyReportMasterModel;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.core.model.SubAreaMasterModel;
import com.eydms.core.model.VisitMasterModel;
import com.eydms.core.utility.EyDmsDateUtility;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;
import de.hybris.platform.site.BaseSiteService;

public class EfficacyReportDaoImpl implements EfficacyReportDao{

	@Resource
	private PaginatedFlexibleSearchService paginatedFlexibleSearchService;

	@Resource
	private FlexibleSearchService flexibleSearchService;

	@Autowired
	BaseSiteService baseSiteService;

	@Override
	public List<CounterVisitMasterModel> findCounterVisitForMonthYear(int month, int year, SubAreaMasterModel subArea) {
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("subarea", subArea);
		List<CounterType> counterType = new ArrayList<>();
		counterType.add(CounterType.DEALER);
		counterType.add(CounterType.RETAILER);
		params.put("counterType", counterType);

		String searchQuery = "select {cv.pk} from {counterVisitMaster as cv join VisitMaster as v on {v.pk}={cv.visit}} where {v.subAreaMaster}=?subarea and {cv.endVisittime} is not null and {cv.counterType} in (?counterType) and "
				+ EyDmsDateUtility.getDateClauseQueryByMonthYear("v.visitPlannedDate", month, year, params);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(searchQuery);
		query.addQueryParameters(params);
		query.setResultClassList(Collections.singletonList(CounterVisitMasterModel.class));
		final SearchResult<CounterVisitMasterModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
	}

	@Override
	public Double findAfterSaleForDealer(Date visitDate, EyDmsCustomerModel customer) {
		final Map<String, Object> params = new HashMap<String, Object>();
		LocalDate visitLocalDate = visitDate.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDate();
		LocalDate endDate = visitLocalDate.plusDays(3);
		String dateQuery = EyDmsDateUtility.getDateRangeClauseQuery("oe.deliveredDate", visitLocalDate.toString(), endDate.toString(), params);

		params.put("dealer", customer);
		params.put("site", baseSiteService.getCurrentBaseSite());

		String searchQuery = "Select SUM({oe.quantityInMT}) from {Order as o join OrderEntry as oe on {oe.order}={o.pk}} where {o.user}=?dealer AND {o:versionID} IS NULL AND {o:site} = ?site AND "
				+ dateQuery;

		final FlexibleSearchQuery query = new FlexibleSearchQuery(searchQuery);
		query.addQueryParameters(params);
		query.setResultClassList(Collections.singletonList(Double.class));
		final SearchResult<Double> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult()!=null && !searchResult.getResult().isEmpty()
				&& searchResult.getResult().get(0)!=null
				?searchResult.getResult().get(0):0;
	}

	@Override
	public Double findBeforeSaleForDealer(Date visitDate, EyDmsCustomerModel customer) {
		final Map<String, Object> params = new HashMap<String, Object>();
        LocalDate visitLocalDate = visitDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate startDate = visitLocalDate.plusDays(3);
        LocalDate endDate = visitLocalDate.minusDays(1);
		String dateQuery = EyDmsDateUtility.getDateRangeClauseQuery("oe.deliveredDate", startDate.toString(), endDate.toString(), params);

		params.put("dealer", customer);
		params.put("site", baseSiteService.getCurrentBaseSite());

		String searchQuery = "Select SUM({oe.quantityInMT}) from {Order as o join OrderEntry as oe on {oe.order}={o.pk}} where {o.user}=?dealer AND {o:versionID} IS NULL AND {o:site} = ?site AND "
				+ dateQuery;

		final FlexibleSearchQuery query = new FlexibleSearchQuery(searchQuery);
		query.addQueryParameters(params);
		query.setResultClassList(Collections.singletonList(Double.class));
		final SearchResult<Double> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult()!=null && !searchResult.getResult().isEmpty()
				&& searchResult.getResult().get(0)!=null
				?searchResult.getResult().get(0):0;
	}

	@Override
	public Double findAfterSaleForRetailer(Date visitDate, EyDmsCustomerModel customer) {
		final Map<String, Object> params = new HashMap<String, Object>();
		LocalDate visitLocalDate = visitDate.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDate();
		LocalDate endDate = visitLocalDate.plusDays(3);
		String dateQuery = EyDmsDateUtility.getDateRangeClauseQuery("oe.deliveredDate", visitLocalDate.toString(), endDate.toString(), params);

		params.put("retailer", customer);
		params.put("site", baseSiteService.getCurrentBaseSite());

		String searchQuery = "Select SUM({oe.quantityInMT}) from {Order as o join OrderEntry as oe on {oe.order}={o.pk}} where {o.retailer}=?retailer AND {o:versionID} IS NULL AND {o:site} = ?site AND "
				+ dateQuery;

		final FlexibleSearchQuery query = new FlexibleSearchQuery(searchQuery);
		query.addQueryParameters(params);
		query.setResultClassList(Collections.singletonList(Double.class));
		final SearchResult<Double> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult()!=null && !searchResult.getResult().isEmpty()
				&& searchResult.getResult().get(0)!=null
				?searchResult.getResult().get(0):0;
	}

	@Override
	public Double findBeforeSaleForRetailer(Date visitDate, EyDmsCustomerModel customer) {
		final Map<String, Object> params = new HashMap<String, Object>();
        LocalDate visitLocalDate = visitDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate startDate = visitLocalDate.plusDays(3);
        LocalDate endDate = visitLocalDate.minusDays(1);
		String dateQuery = EyDmsDateUtility.getDateRangeClauseQuery("oe.deliveredDate", startDate.toString(), endDate.toString(), params);

		params.put("retailer", customer);
		params.put("site", baseSiteService.getCurrentBaseSite());

		String searchQuery = "Select SUM({oe.quantityInMT}) from {Order as o join OrderEntry as oe on {oe.order}={o.pk}} where {o.retailer}=?retailer AND {o:versionID} IS NULL AND {o:site} = ?site AND "
				+ dateQuery;

		final FlexibleSearchQuery query = new FlexibleSearchQuery(searchQuery);
		query.addQueryParameters(params);
		query.setResultClassList(Collections.singletonList(Double.class));
		final SearchResult<Double> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult()!=null && !searchResult.getResult().isEmpty()
				&& searchResult.getResult().get(0)!=null
				?searchResult.getResult().get(0):0;
	}

	//New Territory Change
	@Override
	public EfficacyReportMasterModel getEfficacyReportForMonth(Integer month, Integer year, SubAreaMasterModel subArea, UserModel eydmsUser) {

		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("month", month);
		params.put("year", year);
		params.put("subArea", subArea);
		params.put("eydmsUser", eydmsUser);

		String searchQuery = "select {e.pk} from {EfficacyReportMaster as e} where {e.month}=?month and {e.year}=?year and {e.subAreaMaster}=?subArea and {e.eydmsUser}=?eydmsUser";
		final FlexibleSearchQuery query = new FlexibleSearchQuery(searchQuery);
		query.addQueryParameters(params);
		final SearchResult<EfficacyReportMasterModel> searchResult = flexibleSearchService.search(query);
		return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;

	}

	@Override
	public List<List<Double>> getOutstandingAmountAndDailyAverageSalesWithinDate(String customerCode, Date date) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT TOP 1 {outstandingAmount}, {dailyAverageSales} FROM {OutstandingHistory} WHERE {customerCode}=?customerCode AND {updatedDate} < ?date ORDER BY {updatedDate} DESC ");
		params.put("customerCode", customerCode);
		params.put("date", date);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Double.class,Double.class));
		query.addQueryParameters(params);
		final SearchResult<List<Double>> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
	}

	public List<ProductModel> getAllNewProducts(Date startDate, Date endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {Product} WHERE {launchDate} BETWEEN ?startDate AND ?endDate");
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(ProductModel.class));
		query.addQueryParameters(params);
		final SearchResult<ProductModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult()!=null ? searchResult.getResult():Collections.emptyList();
	}

	@Override
	public List<List<Object>> getSalesForNewProducts(List<ProductModel> productList, EyDmsCustomerModel eydmsCustomer, Date startDate, Date endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {p.code},{p.name}, SUM({oe.quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe.order}={o.pk} JOIN Product AS p ON {oe.product}={p.pk}} WHERE {oe.product} IN (?productList) AND {o.user}=?eydmsCustomer AND {o.orderAcceptedDate} BETWEEN ?counterVisitStartDate AND ?counterVisitEndDate AND {oe.invoiceCreationDateAndTime} BETWEEN ?startDate AND ?endDate GROUP BY {p.code},{p.name}");
		params.put("productList", productList);
		params.put("eydmsCustomer", eydmsCustomer);
		params.put("counterVisitStartDate", startDate);
		params.put("counterVisitEndDate", endDate);
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class, String.class, Double.class));
		query.addQueryParameters(params);
		final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
		if(!Objects.isNull(searchResult))
			return searchResult.getResult();
		return null;
	}

	//New Territory Change
	@Override
	public List<VisitMasterModel> getAllVisitMasterForSubAreaAndSO(Date startDate, Date endDate, SubAreaMasterModel subarea, EyDmsUserModel so) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {VisitMaster} WHERE {subAreaMaster}=?subarea AND {user}=?so AND {endVisitTime} BETWEEN ?startDate AND ?endDate ");
		params.put("subarea", subarea);
		params.put("so", so);
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(VisitMasterModel.class));
		query.addQueryParameters(params);
		final SearchResult<VisitMasterModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public Double getMonthlySalesForNewProduct(ProductModel product, List<EyDmsCustomerModel> eydmsCustomerList, Date startDate, Date endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT SUM({oe.quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe.order}={o.pk} JOIN Product AS p ON {oe.product}={p.pk} } WHERE {oe.product}=?product AND {o.user} IN (?eydmsCustomerList) AND {oe.invoiceCreationDateAndTime} BETWEEN ?startDate AND ?endDate GROUP BY {p.code},{p.name}");
		params.put("product", product);
		params.put("eydmsCustomerList", eydmsCustomerList);
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(Double.class));
		query.addQueryParameters(params);
		final SearchResult<Double> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
		else
			return 0.0;
	}
	
	@Override
	public Double getMonthlySalesForDealer(EyDmsCustomerModel dealer, Date startDate, Date endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT SUM({oe.quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe.order}={o.pk} } WHERE {o.user}=?dealer AND {oe.outForDeliveryDate} BETWEEN ?startDate AND ?endDate");
		params.put("dealer", dealer);
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class, Double.class));
		query.addQueryParameters(params);
		final SearchResult<Double> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
		else
			return 0.0;
	}

	@Override
	public Double getMonthlySalesForRetailer(EyDmsCustomerModel retailer, Date startDate, Date endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT SUM({oe.quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe.order}={o.pk} } WHERE {o.retailer}=?retailer AND {oe.outForDeliveryDate} BETWEEN ?startDate AND ?endDate");
		params.put("retailer", retailer);
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class, Double.class));
		query.addQueryParameters(params);
		final SearchResult<Double> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
		else
			return 0.0;
	}

	@Override
	public List<EyDmsCustomerModel> getObsoleteCountersList(EyDmsUserModel eydmsUser, Date startDate, Date endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT {cvm.eydmsCustomer} FROM {CounterVisitMaster AS cvm JOIN VisitMaster AS v ON {cvm.visit}={v.pk} } WHERE {v.user}=?eydmsUser AND {v.visitPlannedDate} >= ?startDate AND {v.visitPlannedDate} < ?endDate AND {cvm.networkType}=?dormant and {cvm.endVisitTime} IS NOT NULL and {v.status}=?completed ");
		String dormant = NetworkType.DORMANT.getCode();
		VisitStatus completed = VisitStatus.COMPLETED;
		params.put("eydmsUser", eydmsUser);
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		params.put("dormant", dormant);
		params.put("completed", completed);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
		query.addQueryParameters(params);
		final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
	}

	@Override
	public List<EyDmsCustomerModel> getRevivedCountersList(List<EyDmsCustomerModel> obsoleteCounters) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT {c.pk} FROM {EyDmsCustomer AS c} WHERE {c.pk} IN (?obsoleteCounters) AND {c.networkType}=?active");
		String active = NetworkType.ACTIVE.getCode();
		params.put("obsoleteCounters", obsoleteCounters);
		params.put("active", active);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(VisitMasterModel.class));
		query.addQueryParameters(params);
		final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
	}

	@Override
	public EfficacyReportMasterModel getEfficacyReportsMaster(String efficacyId) {
		Map<String, Object> params = new HashMap<String, Object>();
		String searchQuery = "select {e.pk} from {EfficacyReportMaster as e} where {e.pk}=?pk";
		params.put("pk", efficacyId);
		FlexibleSearchQuery query = new FlexibleSearchQuery(searchQuery);
		query.addQueryParameters(params);
		SearchResult<EfficacyReportMasterModel> searchResult = flexibleSearchService.search(query);
		return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
	}

	@Override
	public CronjobsDateLogModel getCronjobsDateLog() {
		String searchQuery = "SELECT {pk} FROM {CronjobsDateLog}";
		FlexibleSearchQuery query = new FlexibleSearchQuery(searchQuery);
		query.setResultClassList(Collections.singletonList(CronjobsDateLogModel.class));
		SearchResult<CronjobsDateLogModel> searchResult = flexibleSearchService.search(query);
		return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
	}

	@Override
	public Double getSalesForCustomerList(List<EyDmsCustomerModel> customerList, Date startDate, Date endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT SUM({oe.quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe.order}={o.pk} } WHERE {o.user} IN (?customerList) AND {oe.outForDeliveryDate} BETWEEN ?startDate AND ?endDate");
		params.put("customerList", customerList);
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class, Double.class));
		query.addQueryParameters(params);
		final SearchResult<Double> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
		else
			return 0.0;
	}

	@Override
	public Double getActualSalesForDealer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel baseSite, Date startDate, Date endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder();

		builder.append("SELECT SUM({oe:quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe:order}={o:pk} JOIN EyDmsCustomer as sc on {o:user}={sc:pk}} WHERE {o:user} = ?eydmsCustomer and {o:versionID} IS NULL and {o:site} =?site and {oe.cancelledDate} is null and {oe:invoiceCreationDateAndTime} >= ?startDate and {oe:invoiceCreationDateAndTime} < ?endDate");

		params.put("eydmsCustomer", eydmsCustomer);
		params.put("site", baseSite);
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
