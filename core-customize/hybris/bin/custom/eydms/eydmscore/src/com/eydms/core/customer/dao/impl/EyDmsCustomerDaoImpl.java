package com.eydms.core.customer.dao.impl;

import com.eydms.core.customer.dao.EyDmsCustomerDao;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.model.EyDmsUserModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import javax.annotation.Resource;
import java.util.*;

public class EyDmsCustomerDaoImpl implements EyDmsCustomerDao {

	@Resource
	private FlexibleSearchService flexibleSearchService;
	
	@Override
	public List<List<Object>> getDealerListWithLastInvoiceDate(Date startDate, Date endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {c.pk}, MAX({sh.invoiceDate}) FROM {EyDmsCustomer AS c LEFT JOIN SalesHistory AS sh ON (({c.customerNo}={sh.customerNo}) AND {sh.invoiceDate}>=?startDate AND {sh.invoiceDate}<?endDate) } GROUP BY {c.pk}");
		params.put("startDate", startDate);	
		params.put("endDate", endDate);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Arrays.asList(String.class,Date.class));
		final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public Date getNetworkTypeUpdateDate() {
	
		final StringBuilder builder = new StringBuilder("SELECT MAX({networkTypeUpdateDate}) FROM {CronjobsDateLog}");
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Date.class));
		final SearchResult<Date> searchResult = flexibleSearchService.search(query);
		if (!searchResult.getResult().isEmpty() && searchResult.getResult()!=null) 
			return searchResult.getResult().get(0);
		return null;
	}

	@Override
	public List<List<Object>> getRetailerAndInfluencerListWithLastInvoiceDate(Date startDate, Date endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {c.pk}, MAX({nsh.transactionDate}) FROM {EyDmsCustomer AS c LEFT JOIN NirmanMitraSalesHistory AS nsh ON ({nsh.transactiontype}='DR' AND {nsh.toCustAccNumber}={c.customerNo} AND ({nsh.transactionDate}) >= ?startDate AND ({nsh.transactionDate}) <= ?endDate ) or ({nsh.transactiontype} in ('DM','RM') and {nsh.toCustAccNumber}={c.nirmanMitraCode} AND ({nsh.transactionDate}) >= ?startDate AND ({nsh.transactionDate}) <= ?endDate ) }  GROUP BY {c.pk}");
		params.put("startDate", startDate);	
		params.put("endDate", endDate);	
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Arrays.asList(String.class,Date.class));
		final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public Date getLiftingDateForRetailerAndInfluencer(EyDmsCustomerModel customer) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT MAX({nsh.transactionDate}) FROM {EyDmsCustomer AS c LEFT JOIN NirmanMitraSalesHistory AS nsh ON ({nsh.transactiontype}='DR' AND {nsh.toCustAccNumber}={c.customerNo}) or ({nsh.transactiontype} in ('DM','RM') and {nsh.toCustAccNumber}={c.nirmanMitraCode}) } WHERE {c.pk}=?customer GROUP BY {c.pk}");
		params.put("customer", customer);	
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Arrays.asList(Date.class));
		final SearchResult<Date> searchResult = flexibleSearchService.search(query);
		if (!searchResult.getResult().isEmpty() && searchResult.getResult()!=null) 
			return searchResult.getResult().get(0);
		return null;
	}

	@Override
	public Date getLiftingDateForDealer(EyDmsCustomerModel customer) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT MAX({sh.invoiceDate}) FROM {EyDmsCustomer AS c LEFT JOIN SalesHistory AS sh ON {c.customerNo}={sh.customerNo}} WHERE {c.pk}=?customer GROUP BY {c.pk}");
		params.put("customer", customer);	
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Arrays.asList(Date.class));
		final SearchResult<Date> searchResult = flexibleSearchService.search(query);
		if (!searchResult.getResult().isEmpty() && searchResult.getResult()!=null) 
			return searchResult.getResult().get(0);
		return null;
	}

    @Override
    public List<EyDmsCustomerModel> getRetailerInfluencerList() {
		final Map<String, Object> params = new HashMap<>();
		final StringBuilder builder = new StringBuilder("SELECT {c:pk} FROM {EyDmsCustomer as c JOIN EnumerationValue as e on {e:pk}={c:counterType}} where {e:code} in ('Retailer','Influencer')");
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
		final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
		List<EyDmsCustomerModel> result = searchResult.getResult();
		return result!=null && !result.isEmpty() ? result : Collections.emptyList();
    }

	@Override
	public List<EyDmsCustomerModel> getCustomerList() {
		final Map<String, Object> params = new HashMap<>();
		final StringBuilder builder = new StringBuilder("SELECT {c:pk} FROM {EyDmsCustomer as c JOIN EnumerationValue as e on {e:pk}={c:counterType}} where {e:code} in ('Retailer','Influencer','Dealer')");
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
		final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
		List<EyDmsCustomerModel> result = searchResult.getResult();
		return result!=null && !result.isEmpty() ? result : Collections.emptyList();
	}

}
