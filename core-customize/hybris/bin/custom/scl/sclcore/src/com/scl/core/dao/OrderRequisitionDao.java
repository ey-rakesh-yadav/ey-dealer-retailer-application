package com.scl.core.dao;

import com.scl.core.enums.RequisitionStatus;
import com.scl.core.model.*;
import com.scl.facades.data.OrderRequisitionData;
import com.scl.facades.data.RequestCustomerData;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;

public interface OrderRequisitionDao {

    public OrderRequisitionModel findByRequisitionId(String requisitionId);

    SearchPageData<OrderRequisitionModel> getOrderRequisitionDetails(RequisitionStatus[] status, String submitType, String fromDate, SclCustomerModel currentUser, String productCode, SearchPageData searchPageData, String requisitionId, String searchKey);

	List<List<Object>> getSalsdMTDforRetailer(List<SclCustomerModel> toCustomerList, String startDate, String endDate,List<String> doList,List<String> subAreaList);

    public DealerRetailerMappingModel getDealerforRetailerDetails(SclCustomerModel dealer, SclCustomerModel retailer, BaseSiteModel brand);

    List<List<Object>> getRetailedDetailedPaginatedSummaryList(RequestCustomerData requestCustomerData);

    OrderModel findOrderByCode(String code);

    OrderModel getOrderFromERPOrderNumber(String erpOrderNo);

    ProductModel getProductFromEquiCode(String equiCode, CatalogVersionModel catalogVer);

    TerritoryMasterModel getTerritoryMasterByTrriId(String trriId);

    TerritoryUserMappingModel getTerritoryUserMapping(String trriId, String Uid);
    MasterStockAllocationModel getMasterAllocationEntry(OrderRequisitionData orderRequisitionData);

    List<OrderRequisitionModel> getSalesDetailsForDealerOfRetailersFromOrmDao(SclCustomerModel raisedByCustomer, SclCustomerModel raisedToCustomer, String fromDate, String toDate, String filter);

    List<MasterStockAllocationModel> getSalesDetailsForDealerOfRetailersFromMsaDao(SclCustomerModel raisedByCustomer, SclCustomerModel raisedToCustomer, String fromDate, String toDate, String filter);

}
