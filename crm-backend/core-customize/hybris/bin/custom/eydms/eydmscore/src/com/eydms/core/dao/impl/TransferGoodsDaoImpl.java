package com.eydms.core.dao.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.eydms.core.dao.TransferGoodsDao;
import com.eydms.core.enums.CRMOrderType;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.core.model.SubAreaMasterModel;

import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

public class TransferGoodsDaoImpl implements TransferGoodsDao{

	@Resource
	FlexibleSearchService flexibleSearchService;
	
	@Override
	public List<EyDmsUserModel> searchForSalesOfficers(String key, List<String> officerCodes) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {EyDmsUser} WHERE {employeeCode} IN (?officerCodes) AND {name} LIKE ?searchKey AND {email} LIKE ?searchKey  AND {mobileNumber} LIKE ?searchKey AND {employeeCode} LIKE ?searchKey");
		params.put("officerCodes", officerCodes);
		
		String searchKey = "%".concat(key).concat("%");
		params.put("searchKey", searchKey);
		
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(EyDmsUserModel.class));
		query.addQueryParameters(params);
		final SearchResult<EyDmsUserModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
	}

	@Override
	public Double getPendingGiftStockForSO(String itemName, List<SubAreaMasterModel> districtSubAreas) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT SUM({oe.quantity}) FROM {Order AS o JOIN OrderEntry AS oe ON {o.pk}={oe.order} JOIN Product AS p ON {oe.product}={p.pk} } WHERE {o.crmOrderType} = ?gift AND {oe.dateOfReceiving} IS NULL AND {p.name} = ?itemName AND {o.subAreaMaster} IN (?subAreas) ");
		CRMOrderType gift = CRMOrderType.GIFT;
		
		params.put("gift", gift);	
		params.put("itemName", itemName);
		params.put("subAreas", districtSubAreas);
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
