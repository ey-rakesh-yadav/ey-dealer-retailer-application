package com.eydms.core.services;

import java.util.List;

import com.eydms.core.model.EyDmsUserModel;
import com.eydms.facades.data.InventoryStockListData;


public interface TransferGoodsService {

	public List<EyDmsUserModel> getSalesOfficers(String userId, String key);
	
	public InventoryStockListData getInventoryStock(String userId, String soUid);
}
