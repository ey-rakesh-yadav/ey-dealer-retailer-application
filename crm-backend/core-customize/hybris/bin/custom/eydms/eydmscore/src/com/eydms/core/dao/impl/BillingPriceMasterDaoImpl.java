package com.eydms.core.dao.impl;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import com.eydms.core.dao.BillingPriceMasterDao;
import com.eydms.core.enums.CustomerCategory;
import com.eydms.core.model.BillingPriceMasterModel;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

public class BillingPriceMasterDaoImpl implements BillingPriceMasterDao {

	@Resource
	private FlexibleSearchService flexibleSearchService;
	
	public BillingPriceMasterModel getBillingPriceMasterForProduct(BaseSiteModel brand, String inventoryItemId, String erpCity, CustomerCategory customerCategory, String packagingCondition, String state, Date currentDate)
	{

		final Map<String, Object> params = new HashMap<String, Object>();
		//final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {BillingPriceMaster} WHERE {effStartDate}<=?currentDate AND {effEndDate}>=?currentDate AND {brand}=?brand AND {inventoryItemId}=?inventoryItemId AND {erpCity}=?erpCity AND {customerCategory}=?customerCategory AND {packagingCondition}=?packagingCondition AND {state}=?state");
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {BillingPriceMaster} WHERE {effStartDate}<=?currentDate AND {effEndDate}>=?currentDate AND {brand}=?brand AND {inventoryItemId}=?inventoryItemId AND {erpCity}=?erpCity AND {customerCategory}=?customerCategory AND {state}=?state");
		params.put("currentDate", currentDate);
		params.put("brand", brand);
		params.put("inventoryItemId", inventoryItemId);
		params.put("erpCity", erpCity);
		params.put("customerCategory", customerCategory);
		//params.put("packagingCondition", packagingCondition);
		params.put("state", state);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(BillingPriceMasterModel.class));
		query.addQueryParameters(params);
		final SearchResult<BillingPriceMasterModel> searchResult = flexibleSearchService.search(query);
		if(searchResult!=null)
			return searchResult.getResult()!=null && !searchResult.getResult().isEmpty() ? searchResult.getResult().get(0) : null;
		
		return null;
	}
}
