package com.scl.core.services;

import java.util.List;

import com.scl.core.model.SclUserModel;
import com.scl.facades.data.InventoryStockListData;


public interface TransferGoodsService {

	public List<SclUserModel> getSalesOfficers(String userId, String key);
	
	public InventoryStockListData getInventoryStock(String userId, String soUid);
}
