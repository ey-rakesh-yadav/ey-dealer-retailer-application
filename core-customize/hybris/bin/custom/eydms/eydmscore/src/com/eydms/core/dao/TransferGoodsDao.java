package com.eydms.core.dao;

import java.util.List;

import com.eydms.core.model.EyDmsUserModel;
import com.eydms.core.model.SubAreaMasterModel;

public interface TransferGoodsDao {

	public List<EyDmsUserModel> searchForSalesOfficers(String key, List<String> officerCodes);
	
	public Double getPendingGiftStockForSO(String itemName, List<SubAreaMasterModel> districtSubAreas);
}
