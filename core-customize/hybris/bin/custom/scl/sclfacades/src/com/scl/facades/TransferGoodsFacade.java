package com.scl.facades;

import com.scl.facades.data.InventoryStockListData;
import com.scl.facades.data.SclUserListData;

public interface TransferGoodsFacade {

	public SclUserListData getSalesOfficers(String userId, String key);
	
	public InventoryStockListData getInventoryStock(String userId, String soUid);
}
