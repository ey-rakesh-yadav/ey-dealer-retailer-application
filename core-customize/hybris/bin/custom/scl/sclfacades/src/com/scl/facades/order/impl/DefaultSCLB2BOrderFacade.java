package com.scl.facades.order.impl;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.customer.services.SclCustomerAccountService;
import com.scl.core.dao.DataConstraintDao;
import com.scl.core.dao.SclUserDao;
import com.scl.core.dao.TerritoryMasterDao;
import com.scl.core.model.*;
import com.scl.core.order.SCLB2BOrderService;
import com.scl.core.region.dao.GeographicalRegionDao;
import com.scl.core.services.DJPVisitService;
import com.scl.core.services.TerritoryMasterService;
import com.scl.facades.data.*;

import com.scl.facades.order.SCLB2BOrderFacade;
import com.scl.facades.order.data.SCLOrderData;

import de.hybris.platform.core.enums.OrderStatus;
import com.scl.facades.order.data.SclOrderHistoryData;
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
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.servicelayer.data.PaginationData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.ordersplitting.WarehouseService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.store.BaseStoreModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;

import static de.hybris.platform.util.localization.Localization.getLocalizedString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class DefaultSCLB2BOrderFacade extends DefaultB2BOrderFacade implements SCLB2BOrderFacade {

    private static final Logger LOG = Logger.getLogger(DefaultSCLB2BOrderFacade.class);
    public static final String WE = "WE";
    public static final String ID = "%id";
    private SCLB2BOrderService sclB2BOrderService;
    private Converter<OrderEntryModel, SCLOrderData> sclOrderEntryCardConverter;
    private Converter<OrderModel, SCLOrderData> sclOrderCardConverter;
    private Converter<DeliveryItemModel, SCLOrderData> sclDeliveryItemCardConverter;
    private SclCustomerAccountService sclCustomerAccountService;
    @Autowired
    private BaseSiteService baseSiteService;
    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private DJPVisitService djpVisitService;
    @Autowired
    private TerritoryMasterDao territoryMasterDao;

    @Autowired
    private GeographicalRegionDao geographicalRegionDao;

    @Resource
    private Converter<AddToCartParams, CommerceCartParameter> commerceOrderParameterConverter;
    @Autowired
    private Converter<OrderModel, CartData> sclOrderConverter;
    @Autowired
    private Converter<RejectionReasonModel, RejectionReasonData> rejectionReasonsConverter;

    @Autowired
    private SclUserDao sclUserDao;

    @Autowired
    private DataConstraintDao dataConstraintDao;
    private static final Logger LOGGER = Logger.getLogger(DefaultSCLB2BOrderFacade.class);

    private static final Integer MINIMUM_SINGLE_SKU_ADD_CART = 1;
    private static final String BASKET_QUANTITY_ERROR_KEY = "basket.error.quantity.invalid";
    @Autowired
    TerritoryMasterService territoryMasterService;


    public Converter<DeliveryItemModel, SCLOrderData> getSclDeliveryItemCardConverter() {
        return sclDeliveryItemCardConverter;
    }

    public void setSclDeliveryItemCardConverter(Converter<DeliveryItemModel, SCLOrderData> sclDeliveryItemCardConverter) {
        this.sclDeliveryItemCardConverter = sclDeliveryItemCardConverter;
    }

    @Override
    public B2BOrderApprovalData updateOrderWithApprovalDecision(final String orderCode, final B2BOrderApprovalData b2BOrderApprovalData) {

        OrderModel order = getSclB2BOrderService().getOrderForCode(orderCode);
        getSclB2BOrderService().updateOrderWithPermissionResult(order, b2BOrderApprovalData);

        final OrderData orderData = getOrderConverter().convert(order);
        b2BOrderApprovalData.setB2bOrderData(orderData);

        return b2BOrderApprovalData;
    }

    @Override
    public SCLOrderData getOrderDetails(final String orderCode, final String entryNumber, String diNumber, String deliveryLineNumber) {
        OrderModel orderModel = getSclB2BOrderService().getOrderForCode(orderCode);
        if (StringUtils.isNotBlank(diNumber) && StringUtils.isNotBlank(deliveryLineNumber) && StringUtils.isNotBlank(entryNumber)) {
            int entryNumberInt = 0;
            try {
                entryNumberInt = Integer.parseInt(entryNumber);
            } catch (Exception ex) {
                LOGGER.debug("Error While Parsing : " + entryNumber);
                throw new ClassCastException("Could not parse entry number : " + entryNumber);
            }

            final OrderEntryModel orderEntry = getSclB2BOrderService().getEntryForNumber(orderModel, entryNumberInt);
            Optional<DeliveryItemModel> optItem = orderEntry.getDeliveriesItem().stream().filter(item -> item != null
                            && item.getDiNumber() != null && item.getDiNumber().equals(diNumber)
                            && item.getDeliveryLineNumber() != null && item.getDeliveryLineNumber().equals(deliveryLineNumber))
                    .findAny();
            if (optItem.isPresent()) {
                return getSclDeliveryItemCardConverter().convert(optItem.get());
            } else {
                return null;
            }
        } else if (StringUtils.isNotBlank(entryNumber)) {
            int entryNumberInt = 0;
            try {
                entryNumberInt = Integer.parseInt(entryNumber);
            } catch (Exception ex) {
                LOGGER.debug("Error While Parsing : " + entryNumber);
                throw new ClassCastException("Could not parse entry number : " + entryNumber);
            }

            final OrderEntryModel orderEntry = getSclB2BOrderService().getEntryForNumber(orderModel, entryNumberInt);
            return getSclOrderEntryCardConverter().convert(orderEntry);
        } else {
            return getSclOrderCardConverter().convert(orderModel);
        }

    }

    /**
     * Method to map order statuses with input status
     *
     * @param inputStatus
     * @return
     */
    @Override
    public String validateAndMapOrderStatuses(final String inputStatus) {
        return getSclB2BOrderService().validateAndMapOrderStatuses(inputStatus);
    }

    @Override
    public PaginationData getPaginationDataForSOOrdersEntriesByCustomerAndStore(final SearchPageData searchPageData, final String statuses) {

        final CustomerModel currentCustomer = (CustomerModel) getUserService().getCurrentUser();
        final BaseStoreModel currentBaseStore = getBaseStoreService().getCurrentBaseStore();

        SearchPageData<OrderEntryModel> orderEntrySearchPageData = getSclCustomerAccountService().fetchSOOrdersEntriesByCustomerAndStore(currentCustomer, currentBaseStore, statuses, searchPageData);
        return orderEntrySearchPageData.getPagination();
    }

    @Override
    public boolean cancelOrderEntry(String orderCode, Integer orderEntryNo, String reason) {
        return getSclB2BOrderService().cancelOrderEntry(orderCode, orderEntryNo, reason);
    }

    @Override
    public boolean cancelOrderByCode(String orderCode, String reason) {
        return getSclB2BOrderService().cancelOrderByCode(orderCode, reason);
    }

    @Override
    public DeliveryDateAndSlotListData fetchOptimalDeliveryDateAndSlot(final int orderQtyfinal, final String routeId, final String userId, final String sourceCode) {
        B2BCustomerModel customerModel = (B2BCustomerModel) getUserService().getUserForUID(userId);
        DeliveryDateAndSlotListData deliverySlots = getSclB2BOrderService().getOptimalDeliveryDateAndSlot(orderQtyfinal, routeId, customerModel, LocalDateTime.now(), sourceCode);
        return deliverySlots;
    }

    @Override
    public void modifyOrder(OrderData orderData) throws CommerceCartModificationException, CalculationException {
        OrderModel order = getSclB2BOrderService().getOrderForCode(orderData.getCode());
        Double basePrice = 0.0;
        LOGGER.info("Before if clause : The base price is " + basePrice);

        if (order.getEntries().get(0).getBasePrice() != null) {
            basePrice = order.getEntries().get(0).getBasePrice();
            LOGGER.info("In the if clause : The base price is " + basePrice);
        }

        if (order.getStatus() != null && order.getStatus().equals(OrderStatus.ORDER_FAILED_VALIDATION)) {
            final OrderModel processedOrder = sclB2BOrderService.createOrderSnapshot(orderData.getCode());
            if (CollectionUtils.isNotEmpty(processedOrder.getEntries())) {
                for (AbstractOrderEntryModel orderEntryModel : processedOrder.getEntries()) {
                    getModelService().remove(orderEntryModel);
                }
            }
            getModelService().save(processedOrder);
            getModelService().refresh(processedOrder);
            for (OrderEntryData orderEntry : orderData.getEntries()) {
                if (isValidEntry(orderEntry)) {
                    final AddToCartParams addToCartParams = populateAddToCartParams(orderEntry);
                    CommerceCartParameter parameter = commerceOrderParameterConverter.convert(addToCartParams);
                    sclB2BOrderService.modifyOrderEntry(processedOrder, parameter);
                } else {
                    throw new EntityValidationException(getLocalizedString(BASKET_QUANTITY_ERROR_KEY));
                }
            }

            sclB2BOrderService.modifyOrderDetails(processedOrder, orderData, basePrice);
        } else {
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
        addToCartParams.setAddressPk(orderEntry.getAddressPk());
        addToCartParams.setRetailerUid(orderEntry.getRetailerUid());
        addToCartParams.setIncoTerm(orderEntry.getIncoTerm());
        addToCartParams.setOrderFor(orderEntry.getOrderFor());
        addToCartParams.setIsDealerProvidingOwnTransport(orderEntry.getIsDealerProvidingOwnTransport());
        addToCartParams.setDeliveryMode(orderEntry.getDeliveryMode());
        return addToCartParams;
    }

    private boolean isValidEntry(OrderEntryData orderEntry) {
        return (orderEntry.getProduct() != null && orderEntry.getProduct().getCode() != null) && orderEntry.getQuantity() != null
                && orderEntry.getQuantity() >= MINIMUM_SINGLE_SKU_ADD_CART;
    }

    @Override
    public SearchPageData<SclOrderHistoryData> getOrderHistoryForOrder(SearchPageData searchPageData, String orderStatus, String filter, String productName, String orderType, Boolean isCreditLimitBreached, String spApprovalFilter, Boolean approvalPending) {
        return sclB2BOrderService.getOrderHistoryForOrder(searchPageData, orderStatus, filter, productName, orderType, isCreditLimitBreached, spApprovalFilter, approvalPending);
    }

    @Override
    public SearchPageData<SclOrderHistoryData> getOrderHistoryForOrderEntry(SearchPageData searchPageData, String orderStatus, String filter, String productName, String orderType, String spApprovalFilter) {
        return sclB2BOrderService.getOrderHistoryForOrderEntry(searchPageData, orderStatus, filter, productName, orderType, spApprovalFilter);
    }

    @Override
    public SearchPageData<SclOrderHistoryData> getOrderHistoryForDeliveryItem(SearchPageData searchPageData, String orderStatus, String filter, String spApprovalFilter) {
        return sclB2BOrderService.getOrderHistoryForDeliveryItem(searchPageData, orderStatus, filter, spApprovalFilter);
    }

    public SCLB2BOrderService getSclB2BOrderService() {
        return sclB2BOrderService;
    }

    public void setSclB2BOrderService(SCLB2BOrderService sclB2BOrderService) {
        this.sclB2BOrderService = sclB2BOrderService;
    }

    public Converter<OrderEntryModel, SCLOrderData> getSclOrderEntryCardConverter() {
        return sclOrderEntryCardConverter;
    }

    public void setSclOrderEntryCardConverter(Converter<OrderEntryModel, SCLOrderData> sclOrderEntryCardConverter) {
        this.sclOrderEntryCardConverter = sclOrderEntryCardConverter;
    }

    public Converter<OrderModel, SCLOrderData> getSclOrderCardConverter() {
        return sclOrderCardConverter;
    }

    public void setSclOrderCardConverter(Converter<OrderModel, SCLOrderData> sclOrderCardConverter) {
        this.sclOrderCardConverter = sclOrderCardConverter;
    }

    public SclCustomerAccountService getSclCustomerAccountService() {
        return sclCustomerAccountService;
    }

    public void setSclCustomerAccountService(SclCustomerAccountService sclCustomerAccountService) {
        this.sclCustomerAccountService = sclCustomerAccountService;
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
        OrderModel orderModel = getSclB2BOrderService().getOrderForCode(code);
        CartData cartData = sclOrderConverter.convert(orderModel);
        return cartData;
    }

    @Override
    public DeliveryDateAndSlotListData fetchOptimalDeliveryWindow(double orderQtyfinal, String routeId, String userId,
                                                                  String sourceCode, String isDealerProvidingTruck) {
        B2BCustomerModel customerModel = (B2BCustomerModel) getUserService().getUserForUID(userId);
        DeliveryDateAndSlotListData deliverySlots = getSclB2BOrderService().getOptimalDeliveryWindow(orderQtyfinal, routeId, customerModel, LocalDateTime.now(), sourceCode, isDealerProvidingTruck);
        return deliverySlots;
    }

    @Override
    public List<DeliverySlotMasterData> getDeliverySlotList() {
        List<DeliverySlotMasterData> dataList = new ArrayList<DeliverySlotMasterData>();
        List<DeliverySlotMasterModel> modelList = getSclB2BOrderService().getDeliverySlotList();
        for (DeliverySlotMasterModel model : modelList) {
            DeliverySlotMasterData data = new DeliverySlotMasterData();
            data.setCentreTime(model.getCentreTime());
            data.setDisplayName(model.getDisplayName());
            data.setEnd(model.getEnd());
            data.setStart(model.getStart());
//			data.setSequence(model.getSequence());
//			data.setEnumName(model.getSlot().getCode());
            dataList.add(data);
        }
        return dataList;
    }

    @Override
    public void updateTotalQuantity(long quantity) {
        getSclB2BOrderService().updateTotalQuantity(quantity);
    }

    @Override
    public SearchPageData<SclOrderHistoryData> getCancelOrderHistoryForOrder(SearchPageData searchPageData, String orderStatus, String filter, String productName, String orderType, String spApprovalFilter, Integer month, Integer year) {
        return sclB2BOrderService.getCancelOrderHistoryForOrder(searchPageData, orderStatus, filter, productName, orderType, spApprovalFilter, month, year);
    }

    @Override
    public SearchPageData<SclOrderHistoryData> getCancelOrderHistoryForOrderEntry(SearchPageData searchPageData, String orderStatus, String filter, String productName, String orderType, String spApprovalFilter, Integer month, Integer year) {
        return sclB2BOrderService.getCancelOrderHistoryForOrderEntry(searchPageData, orderStatus, filter, productName, orderType, spApprovalFilter, month, year);
    }

    @Override
    public DeliveryDateAndSlotListData fetchOptimalISODeliveryWindow(Double orderQty, String routeId, String userId,
                                                                     String sourceCode, String depotCode) {
        B2BCustomerModel customerModel = (B2BCustomerModel) getUserService().getUserForUID(userId);
        DeliveryDateAndSlotListData deliverySlots = getSclB2BOrderService().getOptimalISODeliveryWindow(orderQty, routeId, customerModel, LocalDateTime.now(), sourceCode, depotCode);
        return deliverySlots;
    }

    @Override
    public Boolean getVehicleArrivalConfirmationForOrder(boolean vehicleArrived, String orderCode, String entryNumber, String deliveryItemCode, String deliveryLineNumber) {
        return sclB2BOrderService.getVehicleArrivalConfirmationForOrder(vehicleArrived, orderCode, entryNumber, deliveryItemCode, deliveryLineNumber);

    }

    public Boolean updateEpodStatusForOrder(double shortageQuantity, String orderCode, int entryNumber, String deliveryItemCode, String deliveryLineNumber) {
        return sclB2BOrderService.updateEpodStatusForOrder(shortageQuantity, orderCode, entryNumber, deliveryItemCode, deliveryLineNumber);
    }

    public Boolean getOrderFromRetailersRequest(String requisitionId, String status) {
        return sclB2BOrderService.getOrderFromRetailersRequest(requisitionId, status);
    }

    public SearchPageData<SclOrderHistoryData> getEpodListBasedOnOrderStatus(SearchPageData searchPageData, List<String> Status, String filter) {
        return sclB2BOrderService.getEpodListBasedOnOrderStatus(searchPageData, Status, filter);
    }

    @Override
    public Boolean getEpodFeedback(EpodFeedbackData epodFeedbackData) {
        return sclB2BOrderService.getEpodFeedback(epodFeedbackData);
    }

    @Override
    public boolean updateSpApprovalStatus(String orderCode, String status, String spRejectionReason) {
        return sclB2BOrderService.updateSpApprovalStatus(orderCode, status, spRejectionReason);
    }

    @Override
    public B2BOrderApprovalData approveOrder(String orderCode, B2BOrderApprovalData b2bOrderApprovalData) {
        OrderModel order = getSclB2BOrderService().getOrderForCode(orderCode);
        getSclB2BOrderService().updateOrderWithPermissionResult(order, b2bOrderApprovalData);
        //getSclB2BOrderService().approveOrder(order,b2bOrderApprovalData);
        final OrderData orderData = getOrderConverter().convert(order);
        //b2bOrderApprovalData.setB2bOrderData(orderData);
        return b2bOrderApprovalData;
    }

    /**
     * @return
     */
    @Override
    public List<RejectionReasonData> getRejectionReasons() {
        List<RejectionReasonModel> rejectionReasonModels = sclB2BOrderService.getCancelReasons();
        List<RejectionReasonData> rejectionReasonDataList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(rejectionReasonModels)) {
            rejectionReasonDataList = rejectionReasonsConverter.convertAll(rejectionReasonModels);
        }
        return rejectionReasonDataList;
    }

    @Override
    public Boolean updateTripEndedForDeliveryItem(String orderCode, String entryNumber, String deliveryItemCode, String deliveryLineNumber) {
        return sclB2BOrderService.updateTripEndedForDeliveryItem(orderCode, entryNumber, deliveryItemCode, deliveryLineNumber);

    }

    /**
     * @param orderNumber
     * @param entryNumber
     * @return
     */
    @Override
    public ValidateOrderData validateOrder(String orderNumber, Integer entryNumber, String userId) {
        String placeOrderEnabled;
        //check app level block
        if (getUserService().getCurrentUser().getGroups().contains(getUserService().getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)) ||
                getUserService().getCurrentUser().getGroups().contains(getUserService().getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
            placeOrderEnabled = dataConstraintDao.findVersionByConstraintName(SclCoreConstants.ORDER.UDAAN_CONNECT_PLACE_ORDER_ENABLED);
        }
        else{
            placeOrderEnabled = dataConstraintDao.findVersionByConstraintName(SclCoreConstants.ORDER.UDAAN_PRO_PLACE_ORDER_ENABLED);
        }


        LOG.info(String.format("check placeOrderEnabled at app level ::%s",placeOrderEnabled));
        ValidateOrderData validateOrderData = new ValidateOrderData();
        if (BooleanUtils.isTrue(Boolean.valueOf(placeOrderEnabled))) {
            OrderModel order = sclB2BOrderService.getOrderForCode(orderNumber);
            B2BCustomerModel b2BCustomerModel = (B2BCustomerModel) getUserService().getUserForUID(userId);
            SclUserModel sclUser = null;
            SclCustomerModel sclCustomer = null;
            if (b2BCustomerModel != null && b2BCustomerModel instanceof SclUserModel) {
                sclUser = (SclUserModel) b2BCustomerModel;
            } else if (b2BCustomerModel != null && b2BCustomerModel instanceof SclCustomerModel) {
                sclCustomer = (SclCustomerModel) b2BCustomerModel;
            }

            SclCustomerModel retailerchk = null;
            String errorMessage = null;
            String addressId = Strings.EMPTY;
            List<TerritoryMasterModel> territoryMasterModels = new ArrayList<>();
            if (Objects.nonNull(order)) {
                validateOrderData = new ValidateOrderData();
                SclCustomerModel dealer = (SclCustomerModel) order.getUser();
                if (Objects.nonNull(sclUser) || Objects.nonNull(sclCustomer)) {
                    if (b2BCustomerModel instanceof SclUserModel) {
                        territoryMasterModels = territoryMasterDao.getTerritoriesForSO(sclUser);
                    }
                    Optional<AbstractOrderEntryModel> orderEntryOptional = order.getEntries().stream().filter(entry -> entryNumber == entry.getEntryNumber()).findFirst();
                    if (orderEntryOptional.isPresent()) {
                        OrderEntryModel orderEntryModel = (OrderEntryModel) orderEntryOptional.get();
                        retailerchk = Objects.nonNull(orderEntryModel.getRetailer()) ? orderEntryModel.getRetailer() : null;
                    }


                    if (validateDealerRetailerBlock(validateOrderData, dealer, retailerchk, orderNumber)) {
                        return validateOrderData;
                    } else {
                        if (!checkDealer(territoryMasterModels, dealer)) {
                            LOGGER.info(String.format("No valid dealer found for DO territory ::%s for reorder::%s", dealer.getUid(), orderNumber));
                            errorMessage = dataConstraintDao.findVersionByConstraintName("DEALER_VALIDATION_MESSAGE");
                            validateOrderData.setErrorMessage(errorMessage);
                            validateOrderData.setIsOrderValid(Boolean.FALSE);
                            validateOrderData.setAddressId(Strings.EMPTY);
                        }
                        //validate retailer and Address
                        orderEntryOptional = order.getEntries().stream().filter(entry -> Objects.equals(entryNumber, entry.getEntryNumber())).findFirst();
                        if (orderEntryOptional.isPresent()) {
                            OrderEntryModel orderEntryModel = (OrderEntryModel) orderEntryOptional.get();
                            //SclCustomerModel retailer = Objects.nonNull(orderEntryModel.getRetailer()) ? orderEntryModel.getRetailer() : null;
                            if (!checkRetailer(orderEntryModel, dealer)) {

                                LOGGER.info(String.format("No valid retailer found for dealer::%s for reorder::%s", dealer.getUid(), orderNumber));
                                errorMessage = dataConstraintDao.findVersionByConstraintName("RETAILER_VALIDATION_MESSAGE");
                                validateOrderData.setErrorMessage(errorMessage);
                                validateOrderData.setIsOrderValid(Boolean.FALSE);
                                validateOrderData.setAddressId(Strings.EMPTY);
                            }
                            if (!checkAddress(orderEntryModel, dealer)) {
                                LOGGER.info(String.format("No valid address  ::%s combination found for dealer ::%s for reorder::%s", orderEntryModel.getDeliveryAddress(), dealer.getUid(), orderNumber));
                                errorMessage = dataConstraintDao.findVersionByConstraintName("ADDRESS_VALIDATION_MESSAGE");
                                validateOrderData.setErrorMessage(errorMessage);
                                validateOrderData.setIsOrderValid(Boolean.FALSE);
                                validateOrderData.setAddressId(Strings.EMPTY);
                            } else {
                                List<AddressModel> dealerAddressList = List.copyOf(dealer.getAddresses());
                                Optional<AddressModel> matchedAddress = dealerAddressList.stream().filter(
                                        address -> (address.getPartnerFunctionId() != null && orderEntryModel.getDeliveryAddress().getPartnerFunctionId() != null) && (address.getPartnerFunctionId().equals(orderEntryModel.getDeliveryAddress().getPartnerFunctionId())) && StringUtils.isNotEmpty(address.getSapAddressUsage()) && address.getSapAddressUsage().equalsIgnoreCase(WE)).findFirst();
                                if (matchedAddress.isPresent()) {
                                    addressId = String.valueOf(matchedAddress.get().getPk());
                                    LOGGER.info(String.format("valid address found::%s and setting address Id::%s", orderEntryModel.getDeliveryAddress(), matchedAddress.get()));
                                } else {
                                    errorMessage = dataConstraintDao.findVersionByConstraintName("ADDRESS_VALIDATION_MESSAGE");
                                    validateOrderData.setErrorMessage(errorMessage);
                                    validateOrderData.setIsOrderValid(Boolean.FALSE);
                                    validateOrderData.setAddressId(Strings.EMPTY);
                                }
                            }

                        } else {
                            LOGGER.info(String.format("No entries found for order:: %s for entryNumber::%s", orderNumber, entryNumber));
                            validateOrderData.setErrorMessage("No entries found for order");
                            validateOrderData.setIsOrderValid(Boolean.FALSE);
                            validateOrderData.setAddressId(Strings.EMPTY);
                        }

                        if (StringUtils.isBlank(validateOrderData.getErrorMessage()) && StringUtils.isNotBlank(addressId)) {
                            LOGGER.info(String.format("Order is valid for reorder with orderNo::%s and entryNumber::%s", orderNumber, entryNumber));
                            validateOrderData.setErrorMessage(Strings.EMPTY);
                            validateOrderData.setIsOrderValid(Boolean.TRUE);
                            validateOrderData.setAddressId(addressId);
                        }
                    }
                }
            } else {
                validateOrderData.setErrorMessage(String.format("No valid order found with code::%s", orderNumber));
                validateOrderData.setIsOrderValid(Boolean.FALSE);
                validateOrderData.setAddressId(Strings.EMPTY);
            }
        }else{
            LOG.info(String.format("placeOrderDisabled at app level ::%s",placeOrderEnabled));
            String errorMessage = dataConstraintDao.findVersionByConstraintName(SclCoreConstants.ORDER.SAP_ORDER_PLACEMENT_BLOCKED);
            validateOrderData.setErrorMessage(errorMessage);
            validateOrderData.setIsOrderValid(Boolean.FALSE);
            validateOrderData.setAddressId(Strings.EMPTY);
        }
        return validateOrderData;
    }


    private boolean validateDealerRetailerBlock(ValidateOrderData validateOrderData, SclCustomerModel dealer, SclCustomerModel retailer, String orderNumber) {
        boolean blockFlag = Boolean.FALSE;
        String customer = Strings.EMPTY;
        String errorMessage = Strings.EMPTY;

        if (Objects.nonNull(dealer) && ((Objects.nonNull(dealer.getIsDeliveryBlock()) && BooleanUtils.isTrue(dealer.getIsDeliveryBlock())) || (Objects.nonNull(dealer.getIsBillingBlock()) && BooleanUtils.isTrue(dealer.getIsBillingBlock())) || (Objects.nonNull(dealer.getIsOrderBlock()) && BooleanUtils.isTrue(dealer.getIsOrderBlock())))) {
            blockFlag = true;
            customer = dealer.getUid();
        } else if (Objects.nonNull(retailer) && ((Objects.nonNull(retailer.getIsDeliveryBlock()) && BooleanUtils.isTrue(retailer.getIsDeliveryBlock())) || (Objects.nonNull(retailer.getIsBillingBlock()) && BooleanUtils.isTrue(retailer.getIsBillingBlock())) || (Objects.nonNull(retailer.getIsOrderBlock()) && BooleanUtils.isTrue(retailer.getIsOrderBlock())))) {
            blockFlag = true;
            customer = retailer.getUid();
        }
        if (BooleanUtils.isTrue(blockFlag)) {
            LOGGER.info(String.format("The SAP code %s is Blocked for reorder::%s", customer, orderNumber));
            if (getUserService().getCurrentUser().getGroups().contains(getUserService().getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)) ||
                    getUserService().getCurrentUser().getGroups().contains(getUserService().getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                errorMessage = dataConstraintDao.findVersionByConstraintName(SclCoreConstants.ORDER.SAP_CODE_BLOCKED_CUSTOMER);
            } else {
                errorMessage = dataConstraintDao.findVersionByConstraintName(SclCoreConstants.ORDER.SAP_CODE_BLOCKED);
            }
            if (StringUtils.isNotBlank(errorMessage)) {
                if (errorMessage.contains(ID)) {
                    String errorMsg = errorMessage.replace(ID, customer);
                    validateOrderData.setErrorMessage(errorMsg);
                }
            }
            validateOrderData.setIsOrderValid(Boolean.FALSE);
            validateOrderData.setAddressId(Strings.EMPTY);
            return blockFlag;
        } else {
            return blockFlag;
        }

        /*if (validateOrderData!=null && StringUtils.isNotBlank(validateOrderData.getErrorMessage())) {
                blockFlag = true;
            } else {
            blockFlag = false;
        }*/

    }

    /**
     * @param entryModel
     * @param dealer
     * @return
     */
    private boolean checkRetailer(OrderEntryModel entryModel, SclCustomerModel dealer) {

        if (Objects.nonNull(entryModel) && Objects.nonNull(dealer)) {
            SclCustomerModel retailer = Objects.nonNull(entryModel.getRetailer()) ? entryModel.getRetailer() : null;
            if (retailer != null) {
                String partnerFunctionId = Objects.nonNull(entryModel.getDeliveryAddress()) ? entryModel.getDeliveryAddress().getPartnerFunctionId() : Strings.EMPTY;
                DealerRetailerMappingModel dealerRetailerMapping = sclUserDao.getDealerRetailerMapping(dealer, retailer, partnerFunctionId);
                LOGGER.info(String.format("DealerRetailerMapping for dealer ::%s and retailer::%s with partnerFunctionId::%s", dealer, retailer, partnerFunctionId));
                if (Objects.nonNull(dealerRetailerMapping)) {
                    LOGGER.info(String.format("Valid retailer found"));
                    return Boolean.TRUE;
                }
            } else {
                LOGGER.info(String.format("Retailer is not assigned for order::%s and entry::%s", entryModel.getOrder().getCode(), entryModel.getEntryNumber()));
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    /**
     * @param entryModel
     * @param dealer
     * @return
     */
    private boolean checkAddress(OrderEntryModel entryModel, SclCustomerModel dealer) {
        AddressModel address = entryModel.getDeliveryAddress();
        if ((Objects.nonNull(address) && Objects.nonNull(address.getGeographicalMaster())) && StringUtils.isNotBlank(address.getGeographicalMaster().getTransportationZone())) {
            // GeographicalMasterModel geographicalMaster=address.getGeographicalMaster();
            String transportationZone = address.getGeographicalMaster().getTransportationZone();
            GeographicalMasterModel geographicalMaster = geographicalRegionDao.fetchGeographicalMaster(transportationZone);
            if (Objects.nonNull(geographicalMaster)) {
                String state = StringUtils.isNotBlank(geographicalMaster.getState()) ? geographicalMaster.getState() : Strings.EMPTY;
                String district = StringUtils.isNotBlank(geographicalMaster.getDistrict()) ? geographicalMaster.getDistrict() : Strings.EMPTY;
                String taluka = StringUtils.isNotBlank(geographicalMaster.getTaluka()) ? geographicalMaster.getTaluka() : Strings.EMPTY;
                String erpCity = StringUtils.isNotBlank(geographicalMaster.getErpCity()) ? geographicalMaster.getErpCity() : Strings.EMPTY;
                String pincode = StringUtils.isNotBlank(geographicalMaster.getPincode()) ? geographicalMaster.getPincode() : Strings.EMPTY;
                List<DestinationSourceMasterModel> destinationSourceMasterModelList = geographicalRegionDao.validateAddressFields(state, district, taluka, erpCity, pincode, entryModel.getProduct().getCode(), dealer);
                if (CollectionUtils.isNotEmpty(destinationSourceMasterModelList) && destinationSourceMasterModelList.size() > 0) {
                    LOGGER.info(String.format("Valid address found"));
                    return Boolean.TRUE;
                }
            } else {
                LOGGER.info(String.format("No valid geographical master entry found for transportationZone::%s", transportationZone));
            }
        }
        return Boolean.FALSE;
    }

    /**
     * @param territoryMasterModels
     * @param dealer
     * @return
     */
    private boolean checkDealer(List<TerritoryMasterModel> territoryMasterModels, SclCustomerModel dealer) {
        //dealer territoryCode
        if (CollectionUtils.isNotEmpty(territoryMasterModels) && Objects.nonNull(dealer.getTerritoryCode())) {
            for (TerritoryMasterModel territoryMaster : territoryMasterModels) {
                if (territoryMaster.equals(dealer.getTerritoryCode())) {
                    LOGGER.info(String.format("valid dealer for territorycode::%s", dealer.getTerritoryCode()));
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }

}
