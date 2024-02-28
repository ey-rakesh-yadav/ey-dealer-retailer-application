package com.eydms.core.services;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.eydms.core.model.PurchaseOrderModel;
import com.eydms.facades.data.InventoryStockData;
import com.eydms.facades.data.InventoryStockListData;
import com.eydms.facades.data.PurchaseOrderBatchData;
import com.eydms.facades.data.PurchaseOrderData;

public interface BulkGoodsService {

	public List<String> getListOfPurchaseOrderList(String userId);
	
	public PurchaseOrderModel getPurchaseOrderForOrderNo(String orderNo);

	public String uploadInvoiceImage(String userId, String purchaseOrderNo, String batchNo, MultipartFile file);
	
	public String saveBatch(String userId, PurchaseOrderData orderData, String batchCode);
	
	public List<PurchaseOrderBatchData> getListOfTicketsWithoutGRN(String userID);
	
	public List<PurchaseOrderBatchData> getListOfTicketsWithGRN(String userID);
	
	public PurchaseOrderBatchData getBatchDetails(String batchCode);
	
	public String generateGRN(String userId, PurchaseOrderBatchData batchData, String batchCode);
	
	public Boolean uploadImagesForGRNGeneration(String batchCode, int batchEntryNo, MultipartFile[] files);
	
	public PurchaseOrderBatchData getGRNDetails(String batchCode);
	
	public InventoryStockListData getInventoryStock(String userId);
	
	public Boolean uploadImageForGRNGeneration(String batchCode, int batchEntryNo, MultipartFile file);
}
