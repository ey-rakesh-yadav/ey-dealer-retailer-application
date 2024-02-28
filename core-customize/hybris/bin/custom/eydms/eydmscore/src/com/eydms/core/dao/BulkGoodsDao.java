package com.eydms.core.dao;

import java.util.Date;
import java.util.List;

import com.eydms.core.model.PurchaseOrderBatchModel;
import com.eydms.core.model.PurchaseOrderModel;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.core.model.SubAreaMasterModel;

import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;

public interface BulkGoodsDao {

	public List<String> getListOfPurchaseOrderList(List<SubAreaMasterModel> subAreas);
	
	public PurchaseOrderModel getPurchaseOrderDetails(String orderNo);
	
	public WarehouseModel getWarehouseByCode(String code);
	
	public List<PurchaseOrderBatchModel> getPurchaseOrderBatchWithoutGRNNoListForUser(List<SubAreaMasterModel> subAreas);
	
	public List<PurchaseOrderBatchModel> getPurchaseOrderBatchWithGRNNoListForUser(List<SubAreaMasterModel> subAreas);
	
	public PurchaseOrderBatchModel getPurchaseOrderBatchForCode(String code);
	
	public Integer getStockForType(String type, String pk, Date startDate, Date endDate);
}
