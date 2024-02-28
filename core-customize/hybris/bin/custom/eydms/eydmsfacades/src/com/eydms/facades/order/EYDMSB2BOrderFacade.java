package com.eydms.facades.order;

import com.eydms.facades.data.DeliveryDateAndSlotListData;
import com.eydms.facades.data.DeliverySlotMasterData;
import com.eydms.facades.data.EpodFeedbackData;
import com.eydms.facades.order.data.EYDMSOrderData;

import com.eydms.facades.order.data.EyDmsOrderHistoryData;
import de.hybris.platform.b2bacceleratorfacades.order.B2BOrderFacade;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BOrderApprovalData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commercewebservicescommons.dto.order.CartWsDTO;
import de.hybris.platform.core.servicelayer.data.PaginationData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.order.exceptions.CalculationException;

import java.util.List;

public interface EYDMSB2BOrderFacade extends B2BOrderFacade {

    B2BOrderApprovalData updateOrderWithApprovalDecision(final String orderCode , final B2BOrderApprovalData b2BOrderApprovalData);


    EYDMSOrderData getOrderDetails(final String orderCode , final String entryNumber);

    /**
     * Method to map order statuses with input status
     * @param inputStatus
     * @return
     */
    String validateAndMapOrderStatuses(final String inputStatus);

    PaginationData getPaginationDataForSOOrdersEntriesByCustomerAndStore(final SearchPageData searchPageData,final String statuses);
    
    boolean cancelOrderEntry(String orderCode, Integer orderEntryNo, String reason);
    
    DeliveryDateAndSlotListData fetchOptimalDeliveryDateAndSlot(final int orderQtyfinal, final String routeId , final String userId, final String sourceCode);
    boolean cancelOrderByCode(String orderCode, String reason);

    void modifyOrder(OrderData orderData) throws CommerceCartModificationException, CalculationException;

    SearchPageData<EyDmsOrderHistoryData> getOrderHistoryForOrder(SearchPageData searchPageData, String orderStatus, String filter,String productName,String orderType, Boolean isCreditLimitBreached, String spApprovalFilter, Boolean approvalPending);

    SearchPageData<EyDmsOrderHistoryData> getOrderHistoryForOrderEntry(SearchPageData searchPageData, String orderStatus, String filter,String productCode,String orderType, String spApprovalFilter);


	CartData getOrderForCode(String code);
	
    DeliveryDateAndSlotListData fetchOptimalDeliveryWindow(final double orderQtyfinal, final String routeId , final String userId, final String sourceCode, String isDealerProvidingTruck);


	List<DeliverySlotMasterData> getDeliverySlotList();


	void updateTotalQuantity(long quantity);

    SearchPageData<EyDmsOrderHistoryData> getCancelOrderHistoryForOrder(SearchPageData searchPageData, String orderStatus, String filter,String productName,String orderType, String spApprovalFilter, Integer month, Integer year);

    SearchPageData<EyDmsOrderHistoryData> getCancelOrderHistoryForOrderEntry(SearchPageData searchPageData, String orderStatus, String filter,String productName,String orderType, String spApprovalFilter, Integer month, Integer year);


	DeliveryDateAndSlotListData fetchOptimalISODeliveryWindow(Double orderQty, String routeId, String userId,
			String sourceCode, String depotCode);
    Boolean getVehicleArrivalConfirmationForOrder(boolean vehicleArrived, String orderCode, String entryNumber);

    Boolean updateEpodStatusForOrder(double shortageQuantity, String orderCode, int entryNumber);

    Boolean getOrderFromRetailersRequest(String requisitionId, String status);

    SearchPageData<EyDmsOrderHistoryData> getEpodListBasedOnOrderStatus(SearchPageData searchPageData, List<String> Status, String filter);

    Boolean getEpodFeedback(EpodFeedbackData epodFeedbackData);

    boolean updateSpApprovalStatus(String orderCode, String status, String spRejectionReason);
    
    B2BOrderApprovalData approveOrder(String orderCode, B2BOrderApprovalData b2bOrderApprovalData);
}
