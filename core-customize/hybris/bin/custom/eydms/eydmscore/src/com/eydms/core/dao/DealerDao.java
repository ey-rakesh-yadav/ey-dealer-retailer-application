package com.eydms.core.dao;

import java.util.Date;
import java.util.List;

import com.eydms.core.model.EyDmsCustomerModel;
import de.hybris.platform.core.model.product.ProductModel;
import com.eydms.core.model.ReceiptAllocaltionModel;
import com.eydms.core.model.RetailerRecAllocateModel;

public interface DealerDao {

	List<List<Object>> getMonthWiseForRetailerMTD(List<EyDmsCustomerModel> cusUids, Date startDate, Date endDate);
	
	List<List<Object>> getMonthWiseForInfluencerMTD(List<EyDmsCustomerModel> cusUids, Date startDate, Date endDate);
	
	Double getMonthWiseForRetailerYTD(List<EyDmsCustomerModel> cusUids, Date startDate, Date endDate);
	
	Double getMonthWiseForInfluencerYTD(List<EyDmsCustomerModel> cusUids, Date startDate, Date endDate);
	
	ReceiptAllocaltionModel getDealerAllocation(ProductModel productCode, EyDmsCustomerModel dealerCode);
	
	List<List<Integer>>  getDealerTotalAllocation(EyDmsCustomerModel dealerCode);
	
	RetailerRecAllocateModel getRetailerAllocation(ProductModel productCode, EyDmsCustomerModel dealerCode);
	
	List<List<Integer>>  getRetailerTotalAllocation(EyDmsCustomerModel dealerModel);
}
