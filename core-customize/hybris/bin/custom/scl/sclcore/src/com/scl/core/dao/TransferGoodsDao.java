package com.scl.core.dao;

import java.util.List;

import com.scl.core.model.SclUserModel;
import com.scl.core.model.SubAreaMasterModel;

public interface TransferGoodsDao {

	public List<SclUserModel> searchForSalesOfficers(String key, List<String> officerCodes);
	
	public Double getPendingGiftStockForSO(String itemName, List<SubAreaMasterModel> districtSubAreas);
}
