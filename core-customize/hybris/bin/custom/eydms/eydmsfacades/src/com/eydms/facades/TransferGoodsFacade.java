package com.eydms.facades;

import com.eydms.facades.data.InventoryStockListData;
import com.eydms.facades.data.EyDmsUserListData;

public interface TransferGoodsFacade {

	public EyDmsUserListData getSalesOfficers(String userId, String key);
	
	public InventoryStockListData getInventoryStock(String userId, String soUid);
}
