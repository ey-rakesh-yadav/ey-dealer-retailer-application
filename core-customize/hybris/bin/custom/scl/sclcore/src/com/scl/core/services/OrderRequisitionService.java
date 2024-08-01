package com.scl.core.services;

import com.scl.core.model.MasterStockAllocationModel;
import com.scl.core.model.OrderRequisitionModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.facades.data.OrderRequisitionData;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.Date;
import java.util.List;
//import com.scl.facades.data.OrderRequisitionListData;

public interface OrderRequisitionService {
    public boolean saveOrderRequisitionDetails(OrderRequisitionData orderRequisitionData);

    SearchPageData<OrderRequisitionModel> getOrderRequisitionDetails(String statuses, String submitType, Integer fromMonth, Integer fromYear, Integer toMonth, Integer toYear, SclCustomerModel currentUser, String productCode, SearchPageData searchPageData, String requisitionId, String searchKey);

    void saveDealerRetailerMapping(SclCustomerModel dealer, SclCustomerModel retailer, BaseSiteModel brand);

    void orderCountIncrementForDealerRetailerMap(Date deliveredDate, SclCustomerModel dealer, SclCustomerModel retailer, BaseSiteModel brand);

    Boolean updateOrderRequistionStatus(String requisitionId, String status, Double receivedQty, String cancelReason);

    /**
     *
     * @param searchPageData
     * @param orderStatus
     * @param filter
     * @param productName
     * @param requestType
     * @param submitType
     * @param fromMonth
     * @param fromYear
     * @param toMonth
     * @param toYear
     * @param requisitionId
     * @return
     */
    SearchPageData<OrderRequisitionModel> getOrderHistoryForOrderRequisition(SearchPageData searchPageData,String orderStatus, String filter,String productName,String requestType,String submitType,
                                                                            Integer fromMonth, Integer fromYear, Integer toMonth, Integer toYear, String requisitionId);

    String validateAndMapOrderStatuses(final String inputStatus);

    List<OrderRequisitionModel> getSalesVisibilityForDealersAndRetailersFromOrmService(SclCustomerModel raisedByCustomer,SclCustomerModel raisedToCustomer, String fromDate, String toDate, String filter);

    List<MasterStockAllocationModel> getSalesVisibilityForDealersAndRetailersFromMsaService(SclCustomerModel raisedByCustomer, SclCustomerModel raisedToCustomer, String fromDate, String toDate, String filter);
}
