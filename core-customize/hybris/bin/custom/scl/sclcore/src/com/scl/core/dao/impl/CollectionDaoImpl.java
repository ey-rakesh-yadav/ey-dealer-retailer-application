package com.scl.core.dao.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.scl.core.utility.SclDateUtility;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.dao.CollectionDao;
import com.scl.core.enums.QuarterEndOverdueStatus;
import com.scl.core.model.CashDiscountAvailedModel;
import com.scl.core.model.CashDiscountSlabsModel;
import com.scl.core.model.InvoiceMasterModel;
import com.scl.core.model.LedgerDetailsModel;
import com.scl.core.model.SPInvoiceModel;
import com.scl.core.model.SclCustomerModel;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;

public class CollectionDaoImpl implements CollectionDao{

	@Resource
	private FlexibleSearchService flexibleSearchService;

	@Autowired
	PaginatedFlexibleSearchService paginatedFlexibleSearchService;

	@Override
	public Double getSalesForDealer(List<SclCustomerModel> dealerList, Date startDate, Date endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT SUM({oe.quantityInMT}) FROM {OrderEntry AS oe JOIN Order AS o ON {oe.order}={o.pk} } WHERE {o.user} IN (?dealerList) AND {oe.outForDeliveryDate} BETWEEN ?startDate AND ?endDate");
		params.put("dealerList", dealerList);
		params.put("startDate", startDate);
		params.put("endDate", endDate);
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
	public Double getSecurityDepositForDealer(String dealer) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {securityDeposit} FROM {CreditAndOutstanding} WHERE {customerCode}=?dealer");
		params.put("dealer", dealer);
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
	public List<OrderModel> getCreditBreachedOrders(SclCustomerModel dealer) {
		final Map<String, Object> params = new HashMap<String, Object>();
		//final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {Order} WHERE {user}=?dealer AND {creditLimitBreached}=1 and ( {status}=?modified OR {status}=?approved) ");
		final StringBuilder builder = new StringBuilder();
		builder.append("SELECT {o:pk} from { ").append(OrderModel._TYPECODE).append(" as o} WHERE {o:creditLimitBreached} = ?boolean  ")
				.append(" AND {o:user} = ?user  and ").append(SclDateUtility.getMtdClauseQuery("o:date", params)).append(" AND {o:" + OrderModel.VERSIONID + "} IS NULL");

		/*OrderStatus modified = OrderStatus.ORDER_MODIFIED;
		OrderStatus approved = OrderStatus.APPROVED;*/
		//params.put("dealer", dealer);
		/*params.put("modified", modified);
		params.put("approved", approved);*/

		params.put(OrderModel.USER, dealer);
		params.put("boolean",true);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(OrderModel.class));
		query.addQueryParameters(params);
		final SearchResult<OrderModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
	}

	@Override
	public List<LedgerDetailsModel> getLedgerForDealer(String customerNo, Boolean isDebit, Boolean isCredit, Date startDate, Date endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {LedgerDetails} WHERE {customerNo}=?customerNo AND {date} BETWEEN ?startDate AND ?endDate AND {active} = ?active");

		if(isDebit==Boolean.TRUE && isCredit==Boolean.FALSE)
		{
			builder.append(" AND {debitAmount} IS NOT NULL");
		}
		else if(isCredit==Boolean.TRUE && isDebit==Boolean.FALSE)
		{
			builder.append(" AND {creditAmount} IS NOT NULL");
		}
		builder.append(" ORDER BY {date} DESC");
		params.put("customerNo", customerNo);
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		params.put("active", Boolean.TRUE);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(LedgerDetailsModel.class));
		query.addQueryParameters(params);
		final SearchResult<LedgerDetailsModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
	}

	@Override
	public Double getTotalCDAvailedForDealer(String customerNo, Date startDate, Date endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT SUM({availedDiscount}) FROM {CashDiscountAvailed} WHERE {customerNo} = ?customerNo");
		if(startDate!=null && endDate!=null)
		{
			builder.append(" AND {discountAvailedDate} BETWEEN ?startDate AND ?endDate");
			params.put("startDate", startDate);
			params.put("endDate", endDate);

		}
		params.put("customerNo", customerNo);
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
	public Double getTotalCDLostForDealer(String customerNo, Date startDate, Date endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT SUM({lostDiscount}) FROM {CashDiscountLost} WHERE {customerNo} = ?customerNo");
		if(startDate!=null && endDate!=null)
		{
			builder.append(" AND {discountLostDate} BETWEEN ?startDate AND ?endDate");
			params.put("startDate", startDate);
			params.put("endDate", endDate);

		}
		params.put("customerNo", customerNo);
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
	public List<List<Double>> getTotalEligibleCDForDealer(String customerNo) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT SUM({invoiceAmount}), SUM({invoiceAmountWithDiscount}) FROM {InvoiceMaster} WHERE {customerNo} = ?customerNo AND {reconcilationDate} IS NULL AND {finalReconciledInvoiceAmount} IS NULL");
		params.put("customerNo", customerNo);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Double.class,Double.class));
		query.addQueryParameters(params);
		final SearchResult<List<Double>> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
	}

	@Override
	public Double getDealerNetOutstandingAmount(String customer) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {netOutstanding} FROM {CreditAndOutstanding} WHERE {customerCode}=?customer");
		params.put("customer", customer);
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
	public QuarterEndOverdueStatus getDealerQuarterEndOverdueStatus(String customerNo) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {quarterEndOverdueStatus} FROM {SclCustomer} WHERE {customerNo}=?customerNo");
		params.put("customerNo", customerNo);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(QuarterEndOverdueStatus.class));
		query.addQueryParameters(params);
		final SearchResult<QuarterEndOverdueStatus> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public List<CashDiscountSlabsModel> getAllCashDiscountSlabs() {
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {CashDiscountSlabs} ");
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(CashDiscountSlabsModel.class));
		final SearchResult<CashDiscountSlabsModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
	}

	@Override
	public List<InvoiceMasterModel> getNonReconciledInvoices(String customerNo, Date startDate, Date endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {InvoiceMaster} WHERE {customerNo}=?customerNo AND {reconcilationDate} IS NULL AND {finalReconciledInvoiceAmount} IS NULL AND {invoiceDate} BETWEEN ?startDate AND ?endDate");
		params.put("customerNo", customerNo);
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(InvoiceMasterModel.class));
		query.addQueryParameters(params);
		final SearchResult<InvoiceMasterModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
	}

	@Override
	public Double getNextSlabDiscount(String customerNo, double currentDiscount) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT MAX({discount}) FROM {CashDiscountSlabs} WHERE {customerNo}=?customerNo AND {discount} < ?currentDiscount");
		params.put("customerNo", customerNo);
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
	public List<InvoiceMasterModel> getReconciledInvoices() {
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {InvoiceMaster} WHERE {reconcilationDate} IS NOT NULL AND {finalReconciledInvoiceAmount} IS NOT NULL");

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(InvoiceMasterModel.class));

		final SearchResult<InvoiceMasterModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
	}

	@Override
	public CashDiscountAvailedModel getCashDiscountAvailedModel(String customerNo, String invoiceNo) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {CashDiscountAvailed} WHERE {customerNo}=?customerNo AND {invoiceNo}=?invoiceNo");
		params.put("customerNo", customerNo);
		params.put("invoiceNo", invoiceNo);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(CashDiscountAvailedModel.class));
		query.addQueryParameters(params);
		final SearchResult<CashDiscountAvailedModel> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}

	@Override
	public Double getDailyAverageSalesForDealer(String customerNo) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {dailyAverageSales} FROM {CreditAndOutstanding} WHERE {customerCode}=?customerNo");
		params.put("customerNo", customerNo);
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
	public Double getDailyAverageSalesForListOfDealers(List<String> customerNos) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT SUM({dailyAverageSales}) FROM {CreditAndOutstanding} WHERE {customerCode} IN (?customerNos)");
		params.put("customerNos", customerNos);
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
	public SearchPageData<SPInvoiceModel> getSPInvoiceList(SclCustomerModel sp, SearchPageData searchPageData,
														   Date startDate, Date endDate, String sortKey, String sort) {
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT {pk} FROM {SPInvoice} WHERE {salesPromoter} = ?sp");
		final Map<String, Object> params = new HashMap<String, Object>(20);
		params.put("sp",sp);

		if(startDate!=null && endDate!=null)
		{
			sql.append(" AND {invoiceRaisedDate} >= ?startDate AND {invoiceRaisedDate} <= ?endDate");
			params.put("startDate",startDate);
			params.put("endDate",endDate);
		}

		if(sortKey.equalsIgnoreCase("DATE"))
		{
			sql.append(" ORDER BY {invoiceRaisedDate}");

			if(sort.equalsIgnoreCase("HIGH"))
			{
				sql.append(" DESC");
			}
		}

		else if(sortKey.equalsIgnoreCase("AMOUNT"))
		{
			sql.append(" ORDER BY {amount}");

			if(sort.equalsIgnoreCase("HIGH"))
			{
				sql.append(" DESC");
			}
		}

		final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
		parameter.setSearchPageData(searchPageData);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Collections.singletonList(SPInvoiceModel.class));
		query.getQueryParameters().putAll(params);
		parameter.setFlexibleSearchQuery(query);
		return paginatedFlexibleSearchService.search(parameter);
	}

	@Override
	public Date getLastUpdateDateForOutstanding(List<String> customerNo) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT MAX({lastUpdatedDate}) FROM {CreditAndOutstanding} WHERE {customerCode} in (?customerNo)");
		params.put("customerNo", customerNo);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Date.class));
		query.addQueryParameters(params);
		final SearchResult<Date> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty())) {
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : null;
		}
		else {
			return null;
		}
	}



}