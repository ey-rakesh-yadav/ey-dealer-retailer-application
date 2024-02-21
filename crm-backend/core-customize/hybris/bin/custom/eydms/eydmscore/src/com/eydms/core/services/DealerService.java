package com.eydms.core.services;

import java.util.List;

import com.eydms.facades.CreditLimitData;
import com.eydms.facades.data.MonthlySalesData;
import com.eydms.facades.data.EYDMSDealerSalesAllocationData;


public interface DealerService {

	List<MonthlySalesData> getLastSixMonthSalesForDealer(String customerId, String Filter, String customerType,String retailerId);

	CreditLimitData getHighPriorityActions(String dealerCode);
	int getStockAvailForRetailer(int receipt,int saleToRetailer,int saleToInfluencer);
	int getStockAvailForInfluencer(int receipt,int saleToRetailer,int saleToInfluencer);
	EYDMSDealerSalesAllocationData getStockAllocationForDealer(String productCode);
	EYDMSDealerSalesAllocationData getStockAllocationForRetailer(String productCode);
	List<CreditLimitData> getHighPriorityActionsForDealer(String uid);
}
