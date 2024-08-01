package com.scl.facades.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import com.scl.core.model.PurchaseOrderEntryModel;
import com.scl.core.model.PurchaseOrderModel;
import com.scl.core.services.BulkGoodsService;
import com.scl.facades.BulkGoodsFacade;
import com.scl.facades.data.InventoryStockData;
import com.scl.facades.data.InventoryStockListData;
import com.scl.facades.data.PurchaseOrderBatchData;
import com.scl.facades.data.PurchaseOrderData;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;


public class BulkGoodsFacadeImpl implements BulkGoodsFacade {

	@Autowired
	BulkGoodsService bulkGoodsService;
	
	@Autowired
	Populator<PurchaseOrderModel,PurchaseOrderData> purchaseOrderPopulator;
	
	@Override
	public List<String> getListOfPurchaseOrderList(String userId) {
		return bulkGoodsService.getListOfPurchaseOrderList(userId);
	}

	@Override
	public PurchaseOrderData getPurchaseOrderDetails(String orderNo) {
		
		PurchaseOrderModel model = bulkGoodsService.getPurchaseOrderForOrderNo(orderNo);
		
		PurchaseOrderData data = new PurchaseOrderData();
		
		purchaseOrderPopulator.populate(model, data);
		
		return data;
	}

	@Override
	public String saveBatch(String userId, PurchaseOrderData orderData, String batchCode) {
		return bulkGoodsService.saveBatch(userId, orderData, batchCode);
	}

	@Override
	public List<PurchaseOrderBatchData> getListOfTicketsWithoutGRN(String userId) {
		return bulkGoodsService.getListOfTicketsWithoutGRN(userId);
	}

	@Override
	public List<PurchaseOrderBatchData> getListOfTicketsWithGRN(String userId) {
		return bulkGoodsService.getListOfTicketsWithGRN(userId);
	}
	
	@Override
	public String uploadInvoiceImage(String userId, String purchaseOrderNo,  String batchNo, MultipartFile file) {
		return bulkGoodsService.uploadInvoiceImage(userId, purchaseOrderNo, batchNo, file);
	}

	@Override
	public PurchaseOrderBatchData getBatchDetails(String batchCode) {
		return bulkGoodsService.getBatchDetails(batchCode);
	}

	@Override
	public String generateGRN(String userId, PurchaseOrderBatchData batchData, String batchCode) {
		return bulkGoodsService.generateGRN(userId, batchData, batchCode);
	}

	@Override
	public Boolean uploadImagesForGRNGeneration(String batchCode, int batchEntryNo, MultipartFile[] files) {
		return bulkGoodsService.uploadImagesForGRNGeneration(batchCode, batchEntryNo, files);
	}

	@Override
	public InventoryStockListData getInventoryStock(String userId) {
		return bulkGoodsService.getInventoryStock(userId);
	}

	@Override
	public PurchaseOrderBatchData getGRNDetails(String batchCode) {
		return bulkGoodsService.getGRNDetails(batchCode);
	}

	@Override
	public Boolean uploadImageForGRNGeneration(String batchCode, int batchEntryNo, MultipartFile file) {
		return bulkGoodsService.uploadImageForGRNGeneration(batchCode, batchEntryNo, file);
	}

}
