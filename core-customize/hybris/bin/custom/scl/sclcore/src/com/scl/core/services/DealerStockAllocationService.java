package com.scl.core.services;

import com.scl.core.model.SclCustomerModel;
import com.scl.facades.data.InvoiceListData;
import com.scl.facades.data.LiftingDateRangeData;
import com.scl.facades.data.ProductStockAllocationListData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;


public interface DealerStockAllocationService {

	LiftingDateRangeData getLiftingDateRange(SclCustomerModel customer) throws Exception;

    ProductStockAllocationListData getProductListForStockAllocation(SclCustomerModel dealer, SclCustomerModel customer, String selectedLiftingDate) throws Exception;

    InvoiceListData getInvoiceListForProduct(SearchPageData searchPageData, SclCustomerModel dealer, SclCustomerModel customer, String selectedLiftingDate, String productCode, String productAlias, Double quantity, String filter) throws Exception;
}
