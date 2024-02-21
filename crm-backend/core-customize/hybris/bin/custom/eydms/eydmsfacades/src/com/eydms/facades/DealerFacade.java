package com.eydms.facades;

import java.util.List;

import com.eydms.facades.data.MonthlySalesData;
import com.eydms.facades.data.EYDMSDealerSalesAllocationData;
import com.eydms.facades.data.EyDmsCustomerData;

public interface DealerFacade {

	EyDmsCustomerData getCustomerProfile(String uid);
	
	List<MonthlySalesData> getLastSixMonthSalesForDealer(String customerId, String Filter, String customerType,String retailerId);

	CreditLimitData getHighPriorityActions(String uid);
	
	EYDMSDealerSalesAllocationData getStockAllocationForDealer(String productCode);
	
	EYDMSDealerSalesAllocationData getStockAllocationForRetailer(String productCode);
	
	List<CreditLimitData> getHighPriorityActionsForDealer(String uid);
}
