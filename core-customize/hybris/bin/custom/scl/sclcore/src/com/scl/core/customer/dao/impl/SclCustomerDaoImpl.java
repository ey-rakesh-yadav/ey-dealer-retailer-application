package com.scl.core.customer.dao.impl;

import com.scl.core.customer.dao.SclCustomerDao;
import com.scl.core.dao.SalesPerformanceDao;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.SclUserModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.session.SessionPerformanceTest;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.*;

public class SclCustomerDaoImpl implements SclCustomerDao {
	private static final Logger LOGGER = Logger.getLogger(SclCustomerDaoImpl.class);

	@Resource
	private FlexibleSearchService flexibleSearchService;
	@Autowired
	SalesPerformanceDao salesPerformanceDao;
	
	@Override
	public List<List<Object>> getDealerListWithLastInvoiceDate(Date startDate, Date endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {c.pk}, MAX({sh.invoiceDate}) FROM {SclCustomer AS c LEFT JOIN SalesHistory AS sh ON (({c.customerNo}={sh.customerNo}) AND {sh.invoiceDate}>=?startDate AND {sh.invoiceDate}<?endDate) } GROUP BY {c.pk}");
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
		final StringBuilder builder = new StringBuilder("SELECT {c.pk}, MAX({nsh.transactionDate}) FROM {SclCustomer AS c LEFT JOIN NirmanMitraSalesHistory AS nsh ON ({nsh.transactiontype}='DR' AND {nsh.toCustAccNumber}={c.customerNo} AND ({nsh.transactionDate}) >= ?startDate AND ({nsh.transactionDate}) <= ?endDate ) or ({nsh.transactiontype} in ('DM','RM') and {nsh.toCustAccNumber}={c.nirmanMitraCode} AND ({nsh.transactionDate}) >= ?startDate AND ({nsh.transactionDate}) <= ?endDate ) }  GROUP BY {c.pk}");
		params.put("startDate", startDate);	
		params.put("endDate", endDate);	
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Arrays.asList(String.class,Date.class));
		final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public Date getLiftingDateForRetailerAndInfluencer(SclCustomerModel customer) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT MAX({nsh.transactionDate}) FROM {SclCustomer AS c LEFT JOIN NirmanMitraSalesHistory AS nsh ON ({nsh.transactiontype}='DR' AND {nsh.toCustAccNumber}={c.customerNo}) or ({nsh.transactiontype} in ('DM','RM') and {nsh.toCustAccNumber}={c.nirmanMitraCode}) } WHERE {c.pk}=?customer GROUP BY {c.pk}");
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
	public Date getLiftingDateForDealer(SclCustomerModel customer) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT MAX({sh.invoiceDate}) FROM {SclCustomer AS c LEFT JOIN SalesHistory AS sh ON {c.customerNo}={sh.customerNo}} WHERE {c.pk}=?customer GROUP BY {c.pk}");
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
    public List<SclCustomerModel> getRetailerInfluencerList() {
		final Map<String, Object> params = new HashMap<>();
		final StringBuilder builder = new StringBuilder("SELECT {c:pk} FROM {SclCustomer as c JOIN EnumerationValue as e on {e:pk}={c:counterType}} where {e:code} in ('Retailer','Influencer')");
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Collections.singletonList(SclCustomerModel.class));
		final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
		List<SclCustomerModel> result = searchResult.getResult();
		return result!=null && !result.isEmpty() ? result : Collections.emptyList();
    }

	@Override
	public List<SclCustomerModel> getCustomerList() {
		final Map<String, Object> params = new HashMap<>();
		final StringBuilder builder = new StringBuilder("SELECT {c:pk} FROM {SclCustomer as c JOIN EnumerationValue as e on {e:pk}={c:counterType}} where {e:code} in ('Retailer','Influencer','Dealer') ");
		builder.append(" and {c:defaultB2BUnit}=?defaultB2BUnit ");
		B2BUnitModel sclShreeUnit = salesPerformanceDao.getB2BUnitPk("SclShreeUnit");
		params.put("defaultB2BUnit",sclShreeUnit);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Collections.singletonList(SclCustomerModel.class));
		final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
		List<SclCustomerModel> result = searchResult.getResult();
		return result!=null && !result.isEmpty() ? result : Collections.emptyList();
	}

	@Override
	public List<SclCustomerModel> getDealersList() {
		final Map<String, Object> params = new HashMap<>();
		final StringBuilder builder = new StringBuilder("SELECT {c:pk} FROM {SclCustomer as c JOIN EnumerationValue as e on {e:pk}={c:counterType} JOIN EnumerationValue as e2 on {e2:pk}={c:customerGrouping}} where {e:code} in ('Dealer') and {e2:code} in ('ZDOM') and {active}=?active ");
		builder.append(" and {c:defaultB2BUnit}=?defaultB2BUnit ");
		B2BUnitModel sclShreeUnit = salesPerformanceDao.getB2BUnitPk("SclShreeUnit");
		params.put("defaultB2BUnit",sclShreeUnit);
		params.put("active",Boolean.TRUE);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		LOGGER.info("Query for Dealer List from Cron job:"+query);
		query.setResultClassList(Collections.singletonList(SclCustomerModel.class));
		final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
		List<SclCustomerModel> result = searchResult.getResult();
		return result!=null && !result.isEmpty() ? result : Collections.emptyList();
	}

	@Override
	public List<SclCustomerModel> getRetailerList() {
		final Map<String, Object> params = new HashMap<>();
		final StringBuilder builder = new StringBuilder("SELECT {c:pk} FROM {SclCustomer as c JOIN EnumerationValue as e on {e:pk}={c:counterType} JOIN EnumerationValue as e2 on {e2:pk}={c:customerGrouping}} where {e:code} in ('Retailer') and {e2:code} in ('ZRET') and {active}=?active  ");
		builder.append(" and {c:defaultB2BUnit}=?defaultB2BUnit ");
		B2BUnitModel sclShreeUnit = salesPerformanceDao.getB2BUnitPk("SclShreeUnit");
		params.put("defaultB2BUnit",sclShreeUnit);
		params.put("active",Boolean.TRUE);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		LOGGER.info("Query for Retailer List from Cron job:"+query);
		query.setResultClassList(Collections.singletonList(SclCustomerModel.class));
		final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
		List<SclCustomerModel> result = searchResult.getResult();
		return result!=null && !result.isEmpty() ? result : Collections.emptyList();
	}

	@Override
	public List<SclCustomerModel> getInfluencersList() {
		final Map<String, Object> params = new HashMap<>();
		final StringBuilder builder = new StringBuilder("SELECT {c:pk} FROM {SclCustomer as c JOIN EnumerationValue as e on {e:pk}={c:counterType}} where {e:code} in ('Influencer')  and {active}=?active  ");
		builder.append(" and {c:defaultB2BUnit} in (?defaultB2BUnit) ");
		B2BUnitModel sclShreeUnit = salesPerformanceDao.getB2BUnitPk("SclShreeUnit");
		B2BUnitModel sclOtherUnit = salesPerformanceDao.getB2BUnitPk("SclOtherUnit");
		List<B2BUnitModel> unitModelList=new ArrayList<>();
		unitModelList.add(sclOtherUnit);
		unitModelList.add(sclShreeUnit);
		params.put("defaultB2BUnit",unitModelList);
		params.put("active",Boolean.TRUE);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		LOGGER.info("Query for Influencer List from Cron job:"+query);
		query.setResultClassList(Collections.singletonList(SclCustomerModel.class));
		final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
		List<SclCustomerModel> result = searchResult.getResult();
		return result!=null && !result.isEmpty() ? result : Collections.emptyList();
	}

}
