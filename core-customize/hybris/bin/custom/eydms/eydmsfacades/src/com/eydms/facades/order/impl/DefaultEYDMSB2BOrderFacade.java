package com.eydms.facades.order.impl;

import com.eydms.core.customer.services.EyDmsCustomerAccountService;
import com.eydms.core.model.DeliverySlotMasterModel;
import com.eydms.core.order.EYDMSB2BOrderService;
import com.eydms.facades.data.DeliveryDateAndSlotListData;
import com.eydms.facades.data.DeliverySlotMasterData;
import com.eydms.facades.data.EpodFeedbackData;
import com.eydms.facades.order.EYDMSB2BOrderFacade;
import com.eydms.facades.order.data.EYDMSOrderData;

import de.hybris.platform.core.enums.OrderStatus;
import com.eydms.facades.order.data.EyDmsOrderHistoryData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2bacceleratorfacades.exception.EntityValidationException;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BOrderApprovalData;
import de.hybris.platform.b2bacceleratorfacades.order.impl.DefaultB2BOrderFacade;
import de.hybris.platform.commercefacades.order.data.AddToCartParams;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.servicelayer.data.PaginationData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.ordersplitting.WarehouseService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.store.BaseStoreModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;

import static de.hybris.platform.util.localization.Localization.getLocalizedString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DefaultEYDMSB2BOrderFacade extends DefaultB2BOrderFacade implements EYDMSB2BOrderFacade  {

    private EYDMSB2BOrderService eydmsB2BOrderService;
    private Converter<OrderEntryModel, EYDMSOrderData> eydmsOrderEntryCardConverter;
    private Converter<OrderModel, EYDMSOrderData> eydmsOrderCardConverter;
    private EyDmsCustomerAccountService eydmsCustomerAccountService;
    @Autowired
    private BaseSiteService baseSiteService;
    @Autowired
    private WarehouseService warehouseService;

    @Resource
    private Converter<AddToCartParams, CommerceCartParameter> commerceOrderParameterConverter;
    @Autowired
	private Converter<OrderModel, CartData> eydmsOrderConverter;

    private static final Logger LOGGER = Logger.getLogger(DefaultEYDMSB2BOrderFacade.class);

    private static final Integer MINIMUM_SINGLE_SKU_ADD_CART = 1;
    private static final String BASKET_QUANTITY_ERROR_KEY = "basket.error.quantity.invalid";

    @Override
    public B2BOrderApprovalData updateOrderWithApprovalDecision(final String orderCode , final B2BOrderApprovalData b2BOrderApprovalData){

            OrderModel order = getEyDmsB2BOrderService().getOrderForCode(orderCode);
            getEyDmsB2BOrderService().updateOrderWithPermissionResult(order,b2BOrderApprovalData);

            final OrderData orderData = getOrderConverter().convert(order);
            b2BOrderApprovalData.setB2bOrderData(orderData);

            return b2BOrderApprovalData;
    }

    @Override
    public EYDMSOrderData getOrderDetails(final String orderCode , final String entryNumber){
        OrderModel orderModel = getEyDmsB2BOrderService().getOrderForCode(orderCode);
        if(StringUtils.isNotBlank(entryNumber)){
            int entryNumberInt = 0;
            try {
                entryNumberInt = Integer.parseInt(entryNumber);
            }
            catch (Exception ex){
                LOGGER.debug("Error While Parsing : "+entryNumber);
                throw new ClassCastException("Could not parse entry number : "+entryNumber);
            }

           final OrderEntryModel orderEntry = getEyDmsB2BOrderService().getEntryForNumber(orderModel,entryNumberInt);
            return getEyDmsOrderEntryCardConverter().convert(orderEntry);
        }
        else{
            return getEyDmsOrderCardConverter().convert(orderModel);
        }

    }
    /**
     * Method to map order statuses with input status
     * @param inputStatus
     * @return
     */
    @Override
    public String validateAndMapOrderStatuses(final String inputStatus){
        return getEyDmsB2BOrderService().validateAndMapOrderStatuses(inputStatus);
    }

    @Override
    public PaginationData getPaginationDataForSOOrdersEntriesByCustomerAndStore(final SearchPageData searchPageData , final String statuses){

        final CustomerModel currentCustomer = (CustomerModel) getUserService().getCurrentUser();
        final BaseStoreModel currentBaseStore = getBaseStoreService().getCurrentBaseStore();

        SearchPageData<OrderEntryModel> orderEntrySearchPageData = getEyDmsCustomerAccountService().fetchSOOrdersEntriesByCustomerAndStore(currentCustomer,currentBaseStore,statuses,searchPageData);
        return orderEntrySearchPageData.getPagination();
    }
    
    @Override
    public boolean cancelOrderEntry(String orderCode, Integer orderEntryNo, String reason)
    {
    	return getEyDmsB2BOrderService().cancelOrderEntry(orderCode, orderEntryNo, reason);
    }

    @Override
    public boolean cancelOrderByCode(String orderCode, String reason)
    {
    	return getEyDmsB2BOrderService().cancelOrderByCode(orderCode, reason);
    }
    
    @Override
    public DeliveryDateAndSlotListData fetchOptimalDeliveryDateAndSlot(final int orderQtyfinal, final String routeId , final String userId, final String sourceCode){
        B2BCustomerModel customerModel = (B2BCustomerModel) getUserService().getUserForUID(userId);
        DeliveryDateAndSlotListData deliverySlots = getEyDmsB2BOrderService().getOptimalDeliveryDateAndSlot(orderQtyfinal,routeId,customerModel,LocalDateTime.now(), sourceCode);
        return deliverySlots;
    }

    @Override
    public void modifyOrder(OrderData orderData) throws CommerceCartModificationException,CalculationException {
    	OrderModel order = getEyDmsB2BOrderService().getOrderForCode(orderData.getCode());
        Double basePrice = 0.0;
        LOGGER.info("Before if clause : The base price is " + basePrice);
        
        if(order.getEntries().get(0).getBasePrice()!=null) {
            basePrice = order.getEntries().get(0).getBasePrice();
            LOGGER.info("In the if clause : The base price is " + basePrice);
        }

    	if(order.getStatus()!=null && order.getStatus().equals(OrderStatus.ORDER_FAILED_VALIDATION)) {
    		final OrderModel processedOrder = eydmsB2BOrderService.createOrderSnapshot(orderData.getCode());
    		if(CollectionUtils.isNotEmpty(processedOrder.getEntries())){
    			for(AbstractOrderEntryModel orderEntryModel : processedOrder.getEntries()){
    				getModelService().remove(orderEntryModel);
    			}
    		}
    		getModelService().save(processedOrder);
    		getModelService().refresh(processedOrder);
    		for(OrderEntryData orderEntry : orderData.getEntries()){
    			if (isValidEntry(orderEntry)){
    				final AddToCartParams addToCartParams = populateAddToCartParams(orderEntry);
    				CommerceCartParameter parameter = commerceOrderParameterConverter.convert(addToCartParams);
    				eydmsB2BOrderService.modifyOrderEntry(processedOrder,parameter);
    			}
    			else{
    				throw new EntityValidationException(getLocalizedString(BASKET_QUANTITY_ERROR_KEY));
    			}
    		}

    		eydmsB2BOrderService.modifyOrderDetails(processedOrder,orderData,basePrice);
    	}
    	else {
			throw new UnsupportedOperationException(String.format("Order %s cannot be modified at this stage", order.getCode()));
		}
    }

    private AddToCartParams populateAddToCartParams(OrderEntryData orderEntry) {
        final AddToCartParams addToCartParams = new AddToCartParams();
        addToCartParams.setProductCode(orderEntry.getProduct().getCode());
        addToCartParams.setQuantity(orderEntry.getQuantity());
        addToCartParams.setTruckNo(orderEntry.getTruckNo());
        addToCartParams.setDriverContactNo(orderEntry.getDriverContactNo());
        addToCartParams.setSelectedDeliveryDate(orderEntry.getSelectedDeliveryDate());
        addToCartParams.setSelectedDeliverySlot(orderEntry.getSelectedDeliverySlot());
        addToCartParams.setCalculatedDeliveryDate(orderEntry.getCalculatedDeliveryDate());
        addToCartParams.setCalculatedDeliverySlot(orderEntry.getCalculatedDeliverySlot());
        addToCartParams.setSequence(orderEntry.getSequence());
        addToCartParams.setQuantityMT(orderEntry.getQuantityMT());
        addToCartParams.setWarehouseCode(orderEntry.getWarehouseCode());
        addToCartParams.setRouteId(orderEntry.getRouteId());
        addToCartParams.setRemarks(orderEntry.getRemarks());
        return addToCartParams;
    }

    private boolean isValidEntry(OrderEntryData orderEntry) {
        return (orderEntry.getProduct() != null && orderEntry.getProduct().getCode() != null) && orderEntry.getQuantity() != null
                && orderEntry.getQuantity() >= MINIMUM_SINGLE_SKU_ADD_CART;
    }

    @Override
    public SearchPageData<EyDmsOrderHistoryData> getOrderHistoryForOrder(SearchPageData searchPageData, String orderStatus, String filter,String productName,String orderType, Boolean isCreditLimitBreached, String spApprovalFilter, Boolean approvalPending) {
        return eydmsB2BOrderService.getOrderHistoryForOrder(searchPageData,orderStatus,filter,productName,orderType,isCreditLimitBreached,spApprovalFilter,approvalPending);
    }

    @Override
    public SearchPageData<EyDmsOrderHistoryData> getOrderHistoryForOrderEntry(SearchPageData searchPageData, String orderStatus, String filter,String productName,String orderType, String spApprovalFilter) {
        return eydmsB2BOrderService.getOrderHistoryForOrderEntry(searchPageData,orderStatus,filter,productName,orderType,spApprovalFilter);
    }

    public EYDMSB2BOrderService getEyDmsB2BOrderService() {
        return eydmsB2BOrderService;
    }

    public void setEyDmsB2BOrderService(EYDMSB2BOrderService eydmsB2BOrderService) {
        this.eydmsB2BOrderService = eydmsB2BOrderService;
    }
    public Converter<OrderEntryModel, EYDMSOrderData> getEyDmsOrderEntryCardConverter() {
        return eydmsOrderEntryCardConverter;
    }

    public void setEyDmsOrderEntryCardConverter(Converter<OrderEntryModel, EYDMSOrderData> eydmsOrderEntryCardConverter) {
        this.eydmsOrderEntryCardConverter = eydmsOrderEntryCardConverter;
    }

    public Converter<OrderModel, EYDMSOrderData> getEyDmsOrderCardConverter() {
        return eydmsOrderCardConverter;
    }

    public void setEyDmsOrderCardConverter(Converter<OrderModel, EYDMSOrderData> eydmsOrderCardConverter) {
        this.eydmsOrderCardConverter = eydmsOrderCardConverter;
    }
    public EyDmsCustomerAccountService getEyDmsCustomerAccountService() {
        return eydmsCustomerAccountService;
    }

    public void setEyDmsCustomerAccountService(EyDmsCustomerAccountService eydmsCustomerAccountService) {
        this.eydmsCustomerAccountService = eydmsCustomerAccountService;
    }
    
    public BaseSiteService getBaseSiteService() {
        return baseSiteService;
    }

    public void setBaseSiteService(BaseSiteService baseSiteService) {
        this.baseSiteService = baseSiteService;
    }
    
    public WarehouseService getWarehouseService() {
        return warehouseService;
    }

    public void setWarehouseService(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

	@Override
	public CartData getOrderForCode(String code) {
        OrderModel orderModel = getEyDmsB2BOrderService().getOrderForCode(code);
        CartData cartData = eydmsOrderConverter.convert(orderModel);
		return cartData;
	}

	@Override
	public DeliveryDateAndSlotListData fetchOptimalDeliveryWindow(double orderQtyfinal, String routeId, String userId,
			String sourceCode, String isDealerProvidingTruck) {
        B2BCustomerModel customerModel = (B2BCustomerModel) getUserService().getUserForUID(userId);
        DeliveryDateAndSlotListData deliverySlots = getEyDmsB2BOrderService().getOptimalDeliveryWindow(orderQtyfinal,routeId,customerModel,LocalDateTime.now(), sourceCode, isDealerProvidingTruck);
        return deliverySlots;
    }

	@Override
	public List<DeliverySlotMasterData> getDeliverySlotList() {
		List<DeliverySlotMasterData>  dataList = new ArrayList<DeliverySlotMasterData>();
		List<DeliverySlotMasterModel> modelList = getEyDmsB2BOrderService().getDeliverySlotList();
		for(DeliverySlotMasterModel model: modelList) {
			DeliverySlotMasterData data = new DeliverySlotMasterData();
			data.setDisplayName(model.getDisplayName());
			data.setEnd(model.getEnd());
			data.setStart(model.getStart());
			data.setSequence(model.getSequence());
			data.setEnumName(model.getSlot().getCode());
			dataList.add(data);
		}
		return dataList;
	}

	@Override
	public void updateTotalQuantity(long quantity) {	
		getEyDmsB2BOrderService().updateTotalQuantity(quantity);
	}

    @Override
    public SearchPageData<EyDmsOrderHistoryData> getCancelOrderHistoryForOrder(SearchPageData searchPageData, String orderStatus, String filter, String productName, String orderType, String spApprovalFilter, Integer month, Integer year) {
        return eydmsB2BOrderService.getCancelOrderHistoryForOrder(searchPageData,orderStatus,filter,productName,orderType,spApprovalFilter,month,year);
    }

    @Override
    public SearchPageData<EyDmsOrderHistoryData> getCancelOrderHistoryForOrderEntry(SearchPageData searchPageData, String orderStatus, String filter, String productName, String orderType, String spApprovalFilter, Integer month, Integer year) {
        return eydmsB2BOrderService.getCancelOrderHistoryForOrderEntry(searchPageData,orderStatus,filter,productName,orderType,spApprovalFilter, month, year);
    }

	@Override
	public DeliveryDateAndSlotListData fetchOptimalISODeliveryWindow(Double orderQty, String routeId, String userId,
			String sourceCode, String depotCode) {
        B2BCustomerModel customerModel = (B2BCustomerModel) getUserService().getUserForUID(userId);
        DeliveryDateAndSlotListData deliverySlots = getEyDmsB2BOrderService().getOptimalISODeliveryWindow(orderQty, routeId, customerModel, LocalDateTime.now(), sourceCode, depotCode);
        return deliverySlots;
    }
    public Boolean getVehicleArrivalConfirmationForOrder(boolean vehicleArrived, String orderCode, String entryNumber){
        return eydmsB2BOrderService.getVehicleArrivalConfirmationForOrder(vehicleArrived,orderCode,entryNumber);

    }
    public Boolean updateEpodStatusForOrder(double shortageQuantity, String orderCode, int entryNumber){
        return eydmsB2BOrderService.updateEpodStatusForOrder(shortageQuantity, orderCode, entryNumber);
    }

    public Boolean getOrderFromRetailersRequest(String requisitionId, String status){
        return eydmsB2BOrderService.getOrderFromRetailersRequest(requisitionId, status);
    }

    public SearchPageData<EyDmsOrderHistoryData> getEpodListBasedOnOrderStatus(SearchPageData searchPageData, List<String> Status, String filter) {
        return eydmsB2BOrderService.getEpodListBasedOnOrderStatus(searchPageData,Status,filter);
    }

    @Override
    public Boolean getEpodFeedback(EpodFeedbackData epodFeedbackData) {
        return eydmsB2BOrderService.getEpodFeedback(epodFeedbackData);
    }

    @Override
    public boolean updateSpApprovalStatus(String orderCode, String status, String spRejectionReason) {
        return eydmsB2BOrderService.updateSpApprovalStatus(orderCode, status, spRejectionReason);
    }
    
	@Override
	public B2BOrderApprovalData approveOrder(String orderCode, B2BOrderApprovalData b2bOrderApprovalData) {
        OrderModel order = getEyDmsB2BOrderService().getOrderForCode(orderCode);
        getEyDmsB2BOrderService().updateOrderWithPermissionResult(order,b2bOrderApprovalData);
        //getEyDmsB2BOrderService().approveOrder(order,b2bOrderApprovalData);
        final OrderData orderData = getOrderConverter().convert(order);
        //b2bOrderApprovalData.setB2bOrderData(orderData);
        return b2bOrderApprovalData;
	}
}
