package com.eydms.core.services;

import com.eydms.core.model.OrderRequisitionModel;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.facades.data.OrderRequisitionData;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.Date;
import java.util.List;
//import com.eydms.facades.data.OrderRequisitionListData;

public interface OrderRequisitionService {
    public boolean saveOrderRequisitionDetails(OrderRequisitionData orderRequisitionData);

    SearchPageData<OrderRequisitionModel> getOrderRequisitionDetails(List<String> statuses, String submitType, Integer fromMonth, Integer fromYear, Integer toMonth, Integer toYear, EyDmsCustomerModel currentUser, String productCode, SearchPageData searchPageData, String requisitionId, String searchKey);

    void saveDealerRetailerMapping(EyDmsCustomerModel dealer, EyDmsCustomerModel retailer, BaseSiteModel brand);

    void orderCountIncrementForDealerRetailerMap(Date deliveredDate, EyDmsCustomerModel dealer, EyDmsCustomerModel retailer, BaseSiteModel brand);

    Boolean updateOrderRequistionStatus(String requisitionId, String status, Double receivedQty, String cancelReason);
}
