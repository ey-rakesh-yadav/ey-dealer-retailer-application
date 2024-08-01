package com.scl.core.dao.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.scl.core.dao.BulkGoodsDao;
import com.scl.core.enums.QuarterEndOverdueStatus;
import com.scl.core.model.PurchaseOrderBatchModel;
import com.scl.core.model.PurchaseOrderModel;
import com.scl.core.model.SclUserModel;
import com.scl.core.model.SubAreaMasterModel;

import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;



public class BulkGoodsDaoImpl implements BulkGoodsDao{

	@Resource
	FlexibleSearchService flexibleSearchService;
	
	//New Territory Change
	@Override
	public List<String> getListOfPurchaseOrderList(List<SubAreaMasterModel> subAreas) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {code} FROM {PurchaseOrder} WHERE {subAreaMaster} IN (?subAreas) AND ({status}!=?status OR {status} IS NULL) AND {orderCompletionDate} IS NULL");
		OrderStatus status = OrderStatus.COMPLETED;
		params.put("subAreas", subAreas);	
		params.put("status", status);	
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		query.addQueryParameters(params);
		final SearchResult<String> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
	}
	

	
	
	@Override
	public PurchaseOrderModel getPurchaseOrderDetails(String orderNo) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {PurchaseOrder} WHERE {code}=?orderNo");
		params.put("orderNo", orderNo);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(PurchaseOrderModel.class));
		query.addQueryParameters(params);
		final SearchResult<PurchaseOrderModel> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : null;	
		else
			return null;
	}




	@Override
	public WarehouseModel getWarehouseByCode(String code) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {Warehouse} WHERE {code}=?code");
		params.put("code", code);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(WarehouseModel.class));
		query.addQueryParameters(params);
		final SearchResult<WarehouseModel> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : null;	
		else
			return null;
	}



	//New Territory Change
	@Override
	public List<PurchaseOrderBatchModel> getPurchaseOrderBatchWithoutGRNNoListForUser(List<SubAreaMasterModel> subAreas) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pob.pk} FROM { PurchaseOrderBatch AS pob JOIN PurchaseOrder AS po ON {pob.purchaseOrder}={po.pk} } WHERE {po.subAreaMaster} IN (?subAreas) AND {pob.grnNo} IS NULL  ");
		params.put("subAreas", subAreas);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(PurchaseOrderBatchModel.class));
		query.addQueryParameters(params);
		final SearchResult<PurchaseOrderBatchModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
	}



	//New Territory Change
	@Override
	public List<PurchaseOrderBatchModel> getPurchaseOrderBatchWithGRNNoListForUser(List<SubAreaMasterModel> subAreas) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pob.pk} FROM { PurchaseOrderBatch AS pob JOIN PurchaseOrder AS po ON {pob.purchaseOrder}={po.pk} } WHERE {po.subAreaMaster} IN (?subAreas) AND {pob.grnNo} IS NOT NULL  ");
		params.put("subAreas", subAreas);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(PurchaseOrderBatchModel.class));
		query.addQueryParameters(params);
		final SearchResult<PurchaseOrderBatchModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
	}




	@Override
	public PurchaseOrderBatchModel getPurchaseOrderBatchForCode(String code) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {PurchaseOrderBatch} WHERE {code}=?code");
		params.put("code", code);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(PurchaseOrderBatchModel.class));
		query.addQueryParameters(params);
		final SearchResult<PurchaseOrderBatchModel> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : null;	
		else
			return null;
	}

	@Override
	public Integer getStockForType(String type, String pk, Date startDate, Date endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT SUM({entry." + type + "}) from {StockLevelHistoryEntry AS entry JOIN StockLevel AS s ON {entry.stockLevel}={s.pk} } WHERE {s.pk}=?pk AND {" + type + "} IS NOT NULL AND {updateDate} BETWEEN ?startDate AND ?endDate");
		params.put("type", type);	
		params.put("pk", pk);
		params.put("startDate", startDate);	
		params.put("endDate", endDate);	
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Integer.class));
		query.addQueryParameters(params);
		final SearchResult<Integer> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0;	
		else
			return 0;
	}
}


