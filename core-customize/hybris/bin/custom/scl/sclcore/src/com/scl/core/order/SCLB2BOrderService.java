package com.scl.core.order;

import java.time.LocalDateTime;
import java.util.List;

import com.scl.core.jalo.SalesHistory;
import com.scl.core.model.*;
import com.scl.facades.data.DeliveryDateAndSlotListData;

import com.scl.facades.data.EpodFeedbackData;
import com.scl.facades.order.data.SclOrderHistoryData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.services.B2BOrderService;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BOrderApprovalData;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.commercewebservicescommons.dto.order.CartWsDTO;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.product.UnitModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import javolution.io.Struct;

/**
 *  Interface for SCL B2BOrder Services
 */
public interface SCLB2BOrderService extends B2BOrderService {

    /**
     * Method to update the permissionResult oncce SO Takes action
     * @param order
     * @param b2BOrderApprovalData
     * @return
     */
    OrderModel updateOrderWithPermissionResult(final OrderModel order, B2BOrderApprovalData b2BOrderApprovalData);

    /**
     * Method to map order statuses with input status
     * @param inputStatus
     * @return
     */
    String validateAndMapOrderStatuses(final String inputStatus);
    
    boolean cancelOrderEntry(String orderCode, Integer orderEntryNo, String reason);

    DeliveryDateAndSlotListData getOptimalDeliveryDateAndSlot(final int orderQuantity,final String routeId, B2BCustomerModel user, final LocalDateTime  orderdate,final String sourceCode);

	boolean cancelOrderByCode(String orderCode, String reason);

    void modifyOrderEntry(OrderModel order , CommerceCartParameter parameter) throws CommerceCartModificationException;

    void  modifyOrderDetails(OrderModel order , OrderData orderData, Double baseprice) throws CalculationException;

    SearchPageData<SclOrderHistoryData> getOrderHistoryForOrder(SearchPageData searchPageData, String orderStatus, String filter,String productName , String orderType, Boolean isCreditLimitBreached, String spApprovalFilter, Boolean approvalPending);


    AbstractOrderEntryModel addNewOrderEntry(final OrderModel order, final ProductModel product,
                                                    final long qty, final UnitModel unit, final int number);

    OrderModel createOrderSnapshot(final String code);
    SearchPageData<SclOrderHistoryData> getOrderHistoryForOrderEntry(SearchPageData searchPageData, String orderStatus, String filter,String productName , String orderType, String spApprovalFilter);

	DeliveryDateAndSlotListData getOptimalDeliveryWindow(double orderQuantity, String routeId, B2BCustomerModel user,
			LocalDateTime orderPunchedDate, String sourceCode,String isDealerProvidingTruck);

	List<DeliverySlotMasterModel> getDeliverySlotList();

    void submitOrderForCancellation(SclOrderCancelProcessModel orderCancelProcessModel);

    void submitOrderLineForCancellation(SclOrderLineCancelProcessModel sclOrderLineCancelProcessModel);

	void updateTotalQuantity(long quantity);

    SearchPageData<SclOrderHistoryData> getCancelOrderHistoryForOrder(SearchPageData searchPageData, String orderStatus, String filter,String productName , String orderType, String spApprovalFilter, Integer month, Integer year);

    SearchPageData<SclOrderHistoryData> getCancelOrderHistoryForOrderEntry(SearchPageData searchPageData, String orderStatus, String filter,String productName , String orderType, String spApprovalFilter, Integer month, Integer year);

	DeliveryDateAndSlotListData getOptimalISODeliveryWindow(double orderQuantity, String routeId, B2BCustomerModel user,
			LocalDateTime orderPunchedDate, String sourceCode, String depotCode);

	boolean cancelOrderFromCrm(OrderModel order, String reason, B2BCustomerModel b2BCustomer, Boolean fromCancelJob);

	boolean cancelOrderEntryFromCRM(OrderEntryModel orderEntry, String reason, B2BCustomerModel b2BCustomer,
			Boolean fromCancelJob);
    Boolean getVehicleArrivalConfirmationForOrder(boolean vehicleArrived, String orderCode, String entryNumber, String deliveryItemCode, String deliveryLineNumber);

    Boolean updateEpodStatusForOrder(double shortageQuantity, String orderCode, int entryNumber, String deliveryItemCode, String deliveryLineNumber);

    Boolean getOrderFromRetailersRequest(String requisitionId, String status);

    SearchPageData<SclOrderHistoryData> getEpodListBasedOnOrderStatus(SearchPageData searchPageData, List<String> Status, String filter);

    Boolean getEpodFeedback(EpodFeedbackData epodFeedbackData);

    void saveOrderRequisitionEntryDetails(OrderEntryModel orderEntry, DeliveryItemModel deliveryItemModel, String status);

    boolean updateSpApprovalStatus(String orderCode, String status, String spRejectionReason);

    void getRequisitionStatusByOrderLines(OrderEntryModel orderEntry);
    
    void approveOrder(OrderModel order, B2BOrderApprovalData b2bOrderApprovalData);

    List<SalesHistoryModel> getNCREntriesExistingInOrderEntry();

    List<String> getNCREntriesNotExistingInOrderEntry();
    List<RejectionReasonModel> getCancelReasons();

	SearchPageData<SclOrderHistoryData> getOrderHistoryForDeliveryItem(SearchPageData searchPageData,
			String deliveryStatus, String filter, String spApprovalFilter);

	Boolean updateTripEndedForDeliveryItem(String orderCode, String entryNumber, String deliveryItemCode,
			String deliveryLineNumber);
}
