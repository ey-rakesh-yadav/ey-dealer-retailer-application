package com.scl.core.dao;

import com.scl.core.model.MasterStockAllocationModel;
import com.scl.core.model.SclCustomerModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;

public interface DealerStockAllocationDao {

    /**
     * @param dealer
     * @param customer
     * @param startDate
     * @param endDate
     * @return
     */
    List<MasterStockAllocationModel> getMasterStockAllocationForDateRange(SclCustomerModel dealer,SclCustomerModel customer,String startDate,String endDate);

    /**
     * @param searchPageData
     * @param dealer
     * @param customer
     * @param startDate
     * @param endDate
     * @param quantity
     * @param productCode
     * @param filter
     * @return
     */
    List<MasterStockAllocationModel> getMasterStockAllocationForProductInvoice(SearchPageData searchPageData, SclCustomerModel dealer, SclCustomerModel customer, String startDate, String endDate, Double quantity, String productCode,String productAlias, String filter);
}
