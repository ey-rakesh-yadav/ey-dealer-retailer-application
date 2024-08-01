package com.scl.facades;

import com.scl.facades.data.OrderRequisitionData;

import com.scl.facades.data.SalesVisibilityData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;

public interface OrderRequisitionFacade {
    public boolean saveOrderRequisitionDetails(OrderRequisitionData orderRequisitionData);
    SearchPageData<OrderRequisitionData> getOrderRequisitionDetails(String statuses, String submitType, Integer fromMonth, Integer fromYear, Integer toMonth, Integer toYear, String productCode, String fields, SearchPageData searchPageData, String requisitionId, String searchKey,String requestType);

    Boolean updateOrderRequistionStatus(String requisitionId, String status, Double receivedQty, String cancelReason);
	AddressData getAddressDataFromAddressModel(String id, String dealerUid);

    List<AddressData> getAddressListForRetailer();

    List<SalesVisibilityData> getSalesVisibilityForDealersAndRetailers(final String fromCustomer, final String toCustomer, String fromDate, String toDate, String filter);
    List<SalesVisibilityData> getSalesVisibilityForUser(final String toCustomer, String fromDate, String toDate, String filter);

}