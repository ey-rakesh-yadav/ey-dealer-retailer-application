package com.eydms.core.dao;

import com.eydms.core.model.DealerRetailerMapModel;
import com.eydms.core.model.OrderRequisitionModel;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.facades.data.RequestCustomerData;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;

public interface OrderRequisitionDao {

    public OrderRequisitionModel findByRequisitionId(String requisitionId);

    SearchPageData<OrderRequisitionModel> getOrderRequisitionDetails(List<String> status, String submitType, String fromDate, EyDmsCustomerModel currentUser, String productCode, SearchPageData searchPageData, String requisitionId, String searchKey);

	List<List<Object>> getSalsdMTDforRetailer(List<EyDmsCustomerModel> toCustomerList, String startDate, String endDate,List<String> doList,List<String> subAreaList);

    public DealerRetailerMapModel getDealerforRetailerDetails(EyDmsCustomerModel dealer, EyDmsCustomerModel retailer, BaseSiteModel brand);

    List<List<Object>> getRetailedDetailedPaginatedSummaryList(RequestCustomerData requestCustomerData);

    OrderModel findOrderByCode(String code);

}
