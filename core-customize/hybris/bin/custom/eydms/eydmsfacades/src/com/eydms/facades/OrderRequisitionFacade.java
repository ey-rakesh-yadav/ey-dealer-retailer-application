package com.eydms.facades;

import com.eydms.facades.data.OrderRequisitionData;

import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;

public interface OrderRequisitionFacade {
    public boolean saveOrderRequisitionDetails(OrderRequisitionData orderRequisitionData);
    SearchPageData<OrderRequisitionData> getOrderRequisitionDetails(List<String> statuses, String submitType, Integer fromMonth, Integer fromYear, Integer toMonth, Integer toYear, String productCode, String fields, SearchPageData searchPageData, String requisitionId, String searchKey);

    Boolean updateOrderRequistionStatus(String requisitionId, String status, Double receivedQty, String cancelReason);
	AddressData getAddressDataFromAddressModel(String id, String dealerUid);

    List<AddressData> getAddressListForRetailer();
}