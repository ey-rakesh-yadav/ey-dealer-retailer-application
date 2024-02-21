/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.eydms.occ.controllers;

import static com.eydms.occ.constants.EyDmsoccConstants.OCC_REWRITE_OVERLAPPING_BASE_SITE_USER_PATH;
import static de.hybris.platform.util.localization.Localization.getLocalizedString;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.eydms.facades.data.DeliveryDateAndSlotListData;
import com.eydms.facades.data.EpodFeedbackData;
import com.eydms.facades.exception.EyDmsException;
import com.eydms.facades.order.EYDMSB2BOrderFacade;
import com.eydms.facades.order.data.EYDMSOrderData;
import com.eydms.facades.order.data.EyDmsOrderHistoryData;
import com.eydms.facades.order.data.EyDmsOrderHistoryListData;
import com.eydms.facades.order.impl.DefaultEYDMSB2BCheckoutFacade;
import com.eydms.occ.annotation.ApiBaseSiteIdAndTerritoryParam;
import com.eydms.occ.annotation.ApiBaseSiteIdAndUserIdAndTerritoryParam;
import com.eydms.occ.dto.order.EYDMSOrderWSDTO;
import com.eydms.occ.dto.order.EyDmsOrderHistoryListWsDTO;
import com.eydms.occ.security.EyDmsSecuredAccessConstants;

import de.hybris.platform.b2b.enums.CheckoutPaymentType;
import de.hybris.platform.b2bacceleratorfacades.api.cart.CartFacade;
import de.hybris.platform.b2bacceleratorfacades.checkout.data.PlaceOrderData;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BOrderApprovalData;
import de.hybris.platform.b2bwebservicescommons.dto.order.OrderApprovalDecisionWsDTO;
import de.hybris.platform.b2bwebservicescommons.dto.order.ReplenishmentOrderWsDTO;
import de.hybris.platform.b2bwebservicescommons.dto.order.ScheduleReplenishmentFormWsDTO;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.CartModificationData;
import de.hybris.platform.commercefacades.order.data.CartModificationDataList;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.commerceservices.request.mapping.annotation.RequestMappingOverride;
import de.hybris.platform.commercewebservices.core.order.data.OrderEntryDataList;
import de.hybris.platform.commercewebservicescommons.annotation.SiteChannelRestriction;
import de.hybris.platform.commercewebservicescommons.dto.order.CartWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.OrderEntryListWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.OrderEntryWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.OrderWsDTO;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.PaymentAuthorizationException;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.RequestParameterException;
import de.hybris.platform.commercewebservicescommons.strategies.CartLoaderStrategy;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.ordermanagementfacades.order.OmsOrderFacade;
import de.hybris.platform.servicelayer.search.paginated.util.PaginatedSearchUtils;
import de.hybris.platform.tx.Transaction;
import de.hybris.platform.webservicescommons.errors.exceptions.WebserviceValidationException;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper;
import de.hybris.platform.webservicescommons.pagination.WebPaginationUtils;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@RequestMapping(value = OCC_REWRITE_OVERLAPPING_BASE_SITE_USER_PATH)
@ApiVersion("v2")
@Tag(name = "EYDMS B2B Orders")
public class EYDMSB2BOrdersController extends EyDmsBaseController
{
	protected static final String API_COMPATIBILITY_B2B_CHANNELS = "api.compatibility.b2b.channels";
	private static final String CART_CHECKOUT_TERM_UNCHECKED = "cart.term.unchecked";
	private static final String OBJECT_NAME_SCHEDULE_REPLENISHMENT_FORM = "ScheduleReplenishmentForm";

	private static final Logger LOGGER = Logger.getLogger(EYDMSB2BOrdersController.class);

	@Resource(name = "userFacade")
	protected UserFacade userFacade;

	@Resource(name = "defaultEYDMSB2BCheckoutFacade")
	private DefaultEYDMSB2BCheckoutFacade b2bCheckoutFacade;

	@Resource(name = "b2bCartFacade")
	private CartFacade cartFacade;

	@Resource(name = "cartLoaderStrategy")
	private CartLoaderStrategy cartLoaderStrategy;

	@Resource(name = "dataMapper")
	private DataMapper dataMapper;

	@Resource(name = "b2BPlaceOrderCartValidator")
	private Validator placeOrderCartValidator;

	@Resource(name = "scheduleReplenishmentFormWsDTOValidator")
	private Validator scheduleReplenishmentFormWsDTOValidator;

	@Resource(name = "orderFacade")
	private EYDMSB2BOrderFacade orderFacade;

	@Resource
	private OmsOrderFacade omsOrderFacade;

	//@Resource(name = "ordersHelper")
	//private OrdersHelper ordersHelper;

	@Resource(name = "webPaginationUtils")
	private WebPaginationUtils webPaginationUtils;
	
	@Resource(name = "eydmsOrderApprovalDecisionValidator")
    private Validator eydmsOrderApprovalDecisionValidator;

	@Resource(name = "eydmsOrderModificationValidator")
	private Validator eydmsOrderModificationValidator;

	@Secured(
	{ EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_GUEST,
			EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT })
	@RequestMapping(value = "/orders", method = RequestMethod.POST)
	@RequestMappingOverride(priorityProperty = "eydmsocc.EYDMSB2BOrdersController.placeOrder.priority")
	@SiteChannelRestriction(allowedSiteChannelsProperty = API_COMPATIBILITY_B2B_CHANNELS)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	@Operation(operationId = "placeOrgOrder", summary = "Places a B2B Order.", description = "Places a B2B Order. By default the payment type is ACCOUNT. Please set payment type to CARD if placing an order using credit card.")
	public OrderWsDTO placeOrgOrder(
			@Parameter(description = "Cart identifier: cart code for logged in user, cart guid for anonymous user, 'current' for the last modified cart", required = true) @RequestParam(required = true) final String cartId,
			@Parameter(description = "Whether terms were accepted or not.", required = true) @RequestParam(required = true) final boolean termsChecked, @RequestParam(required = false) final String ncrGapAcceptanceReason,
			@ApiFieldsParam @RequestParam(required = false, defaultValue = FieldSetLevelHelper.DEFAULT_LEVEL) final String fields)
			throws InvalidCartException, PaymentAuthorizationException
	{
		try {
		validateTerms(termsChecked);

		validateUser();

		cartLoaderStrategy.loadCart(cartId);
		final CartData cartData = cartFacade.getCurrentCart();

		validateCart(cartData);
		validateAndAuthorizePayment(cartData);
		boolean isNTOrder = false;
		PlaceOrderData placeOrderData = new PlaceOrderData();
		placeOrderData.setIsNTOrder(isNTOrder);
		placeOrderData.setNcrGapAcceptanceReason(ncrGapAcceptanceReason);

		return dataMapper.map(b2bCheckoutFacade.placeOrder(placeOrderData), OrderWsDTO.class, fields);
	}
	catch(NullPointerException n) {
		OrderWsDTO nn = new OrderWsDTO();
		nn.setCode(ExceptionUtils.getStackTrace(n));
		return nn;
	}
	}

	@Secured(
	{ EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,
			EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP, "ROLE_CLIENT" })
	@RequestMapping(value = "/cartFromOrder", method = RequestMethod.POST)
	@SiteChannelRestriction(allowedSiteChannelsProperty = API_COMPATIBILITY_B2B_CHANNELS)
	@RequestMappingOverride
	@ResponseBody
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(operationId = "createCartFromOrder", summary = "Create a cart based on a previous order", description = "Returns a list of modification applied to the new cart compared to original. e.g lower quantity was added")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public String createCartFromOrder(
			@Parameter(description = "The order code", required = true) @RequestParam final String orderCode,
			@Parameter(description = "Response configuration. This is the list of fields that should be returned in the response body.", schema = @Schema(allowableValues = {"BASIC", "DEFAULT", "FULL"})) @RequestParam(defaultValue = "DEFAULT") final String fields,
			@Parameter(hidden = true) final HttpServletResponse response)
	{
		b2bCheckoutFacade.createCartFromOrder(orderCode);
		CartData cartData = getCommerceCartFacade().getSessionCart();
		return cartData.getCode();
	}

	@Secured(
	{ EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_GUEST,
			EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT })
	@RequestMapping(value = "/replenishmentOrders", method = RequestMethod.POST, consumes =
	{ MediaType.APPLICATION_JSON_VALUE })
	@RequestMappingOverride
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	@Operation(operationId = "createReplenishmentOrder", summary = "Creates an Order and schedules Replenishment.", description = "Creates an Order and schedules Replenishment. By default the payment type is ACCOUNT. Please set payment type to CARD if placing an order using credit card.")
	public ReplenishmentOrderWsDTO createReplenishmentOrder(
			@Parameter(description = "Cart identifier: cart code for logged in user, cart guid for anonymous user, 'current' for the last modified cart", required = true) @RequestParam(required = true) final String cartId,
			@Parameter(description = "Whether terms were accepted or not.", required = true) @RequestParam(required = true) final boolean termsChecked,
			@Parameter(description = "Schedule replenishment form object.", required = true) @RequestBody(required = true) final ScheduleReplenishmentFormWsDTO scheduleReplenishmentForm,
			@ApiFieldsParam @RequestParam(required = false, defaultValue = FieldSetLevelHelper.DEFAULT_LEVEL) final String fields)
			throws InvalidCartException, PaymentAuthorizationException
	{

		validateTerms(termsChecked);

		validateUser();

		cartLoaderStrategy.loadCart(cartId);
		final CartData cartData = cartFacade.getCurrentCart();

		validateCart(cartData);
		validateAndAuthorizePayment(cartData);

		validateScheduleReplenishmentForm(scheduleReplenishmentForm);
		final PlaceOrderData placeOrderData = createPlaceOrderData(scheduleReplenishmentForm);

		return dataMapper.map(b2bCheckoutFacade.placeOrder(placeOrderData), ReplenishmentOrderWsDTO.class, fields);
	}

	protected void validateUser()
	{
		if (userFacade.isAnonymousUser())
		{
			throw new AccessDeniedException("Access is denied");
		}
	}

	protected void validateTerms(final boolean termsChecked)
	{
		if (!termsChecked)
		{
			throw new RequestParameterException(getLocalizedString(CART_CHECKOUT_TERM_UNCHECKED));
		}
	}

	protected void validateScheduleReplenishmentForm(ScheduleReplenishmentFormWsDTO scheduleReplenishmentForm)
	{
		validate(scheduleReplenishmentForm, OBJECT_NAME_SCHEDULE_REPLENISHMENT_FORM, scheduleReplenishmentFormWsDTOValidator);
	}

	protected void validateAndAuthorizePayment(final CartData cartData)
			throws PaymentAuthorizationException
	{
		if (CheckoutPaymentType.CARD.getCode().equals(cartData.getPaymentType().getCode()) && !b2bCheckoutFacade.authorizePayment(null))
		{
				throw new PaymentAuthorizationException();
		}
	}

	protected void validateCart(final CartData cartData) throws InvalidCartException
	{
		final Errors errors = new BeanPropertyBindingResult(cartData, "sessionCart");
		placeOrderCartValidator.validate(cartData, errors);
		if (errors.hasErrors())
		{
			throw new WebserviceValidationException(errors);
		}

		try
		{
			final List<CartModificationData> modificationList = cartFacade.validateCurrentCartData();
			if(CollectionUtils.isNotEmpty(modificationList))
			{
				final CartModificationDataList cartModificationDataList = new CartModificationDataList();
				cartModificationDataList.setCartModificationList(modificationList);
				throw new WebserviceValidationException(cartModificationDataList);
			}
		}
		catch (final CommerceCartModificationException e)
		{
			throw new InvalidCartException(e);
		}
	}

	protected PlaceOrderData createPlaceOrderData(final ScheduleReplenishmentFormWsDTO scheduleReplenishmentForm)
	{
		final PlaceOrderData placeOrderData = new PlaceOrderData();
		dataMapper.map(scheduleReplenishmentForm, placeOrderData, false);
		if (scheduleReplenishmentForm != null)
		{
			placeOrderData.setReplenishmentOrder(Boolean.TRUE);
		}
		placeOrderData.setTermsCheck(Boolean.TRUE);
		return placeOrderData;
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/orders", method = RequestMethod.GET)
	@ResponseBody
	@Operation(operationId = "getEYDMSUserOrders", summary = "Get a eydms  order.", description = "Returns specific order/Entry details based on a specific order code. The response contains detailed order information.")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public ResponseEntity<EYDMSOrderWSDTO> getUserOrders(
			@Parameter(description = "Order GUID (Globally Unique Identifier) or order CODE", required = true) @RequestParam final String code,
			@Parameter(description = "Order entry number") @RequestParam(required = false) final String entryNumber,
			@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) throws EyDmsException
	{
		try {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug(String.format("Getting details for Order Code [%s] and Entry Number [%s]",code,entryNumber));
			}
			final EYDMSOrderData orderData = orderFacade.getOrderDetails(code,entryNumber);
			return ResponseEntity.status(HttpStatus.OK).body(getDataMapper().map(orderData, EYDMSOrderWSDTO.class, fields));
		}
		catch(Exception e) {
			throw new EyDmsException(e.getMessage(), e);
		}
	}
	
	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/orders/{code}", method = RequestMethod.GET)
	@ResponseBody
	@RequestMappingOverride
	@Operation(operationId = "getOrderForCode", summary = "Get a eydms  order detail.", description = "Returns specific order/Entry details based on a specific order code. The response contains detailed order information.")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public CartWsDTO getOrderForCode(
			@PathVariable final String code,
			@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) throws EyDmsException
	{
		try {
			CartData cartData = orderFacade.getOrderForCode(code);
			return getDataMapper().map(cartData, CartWsDTO.class, fields);
		}
		catch(Exception e) {
			throw new EyDmsException(e.getMessage(), e);
		}
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/orders/{code}/entries", method = RequestMethod.GET)
	@ResponseBody
	@RequestMappingOverride
	@Operation(operationId = "getOrderEntryForCode", summary = "Get a eydms  order entry detail.", description = "Returns specific order/Entry details based on a specific order code. The response contains detailed order information.")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public OrderEntryListWsDTO getOrderEntryForCode(
			@PathVariable final String code,
			@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		final OrderEntryDataList dataList = new OrderEntryDataList();
		CartData cartData = orderFacade.getOrderForCode(code);
		dataList.setOrderEntries(cartData.getEntries());
		return getDataMapper().map(dataList, OrderEntryListWsDTO.class, fields);
	}
	
	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/cancelorder", method = RequestMethod.POST)
	@ResponseBody
	@Operation(operationId = "cancelOrder", summary = "Cancels order or orderentry")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public ResponseEntity<Boolean> cancelOrder(
			@Parameter @RequestParam(required = true) final String orderCode,
			@Parameter @RequestParam(required = true) final String reason)

	{
		return ResponseEntity.status(HttpStatus.OK).body(orderFacade.cancelOrderByCode(orderCode,reason));
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/cancelorderEntry", method = RequestMethod.POST)
	@ResponseBody
	@Operation(operationId = "cancelorderEntry", summary = "Cancels order line item")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public ResponseEntity<Boolean> cancelOrderEntry(
			@Parameter @RequestParam(required = true) final String orderCode,
			@Parameter @RequestParam(required = true) final Integer orderEntryNo,
			@Parameter @RequestParam(required = true) final String reason)

	{
		return ResponseEntity.status(HttpStatus.OK).body(orderFacade.cancelOrderEntry(orderCode, orderEntryNo,reason));
	}
	
	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/deliverydatesandslots", method = RequestMethod.GET)
	@ResponseBody
	@Operation(operationId = "getOptimalDeliveryDatesAndSlots", summary = "Get Optimal Delivery dates And Slots.", description = "Get Optimal Delivery dates And Slots.")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public ResponseEntity<DeliveryDateAndSlotListData> getDeliveryDateAndSlot( @RequestParam final String routeId
			, @PathVariable final String userId, @RequestParam final Integer orderQty, @RequestParam String sourceCode)

	{
		DeliveryDateAndSlotListData deliveryDateAndSlots = orderFacade.fetchOptimalDeliveryDateAndSlot(orderQty, routeId, userId, sourceCode);
		return ResponseEntity.status(HttpStatus.OK).body(deliveryDateAndSlots);
	}


	@Secured(
	{ EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,
			EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP, "ROLE_CLIENT" })
	@RequestMapping(value = "/cartFromOrderEntry", method = RequestMethod.POST)
	@SiteChannelRestriction(allowedSiteChannelsProperty = API_COMPATIBILITY_B2B_CHANNELS)
	@ResponseBody
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(operationId = "createCartFromOrder", summary = "Create a cart based on a previous order Entry", description = "Returns a list of modification applied to the new cart compared to original. e.g lower quantity was added")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public String createCartFromOrder(
			@Parameter(description = "The order code", required = true) @RequestParam final String orderCode,
			@Parameter(description = "The order Entry Number", required = true) @RequestParam final String orderEntryNumber,
			@Parameter(description = "Response configuration. This is the list of fields that should be returned in the response body.", schema = @Schema( allowableValues ={ "BASIC", "DEFAULT", "FULL"})) @RequestParam(defaultValue = "DEFAULT") final String fields,
			@Parameter(hidden = true) final HttpServletResponse response)
	{
		if(StringUtils.isNotBlank(orderEntryNumber)) {
			b2bCheckoutFacade.createCartFromOrder(orderCode);

			CartData cartData = getCommerceCartFacade().getSessionCart();
			int inputEntryEnumber = Integer.valueOf(orderEntryNumber);
			for(int i=cartData.getEntries().size()-1;i>=0;i--) {
				int entryNumber = cartData.getEntries().get(i).getEntryNumber();
				if(inputEntryEnumber != entryNumber) {
					try {
						getCommerceCartFacade().updateCartEntry(entryNumber, 0);
					} catch (CommerceCartModificationException e) {
						cartFacade.removeSessionCart();
						throw new IllegalArgumentException("Unable to create cart from the given order. Cart cannot be modified", e);
					}
				}
			}
				orderFacade.updateTotalQuantity(cartData.getEntries().get(0).getQuantity());
			return cartData.getCode();
		}
		else {
			throw new IllegalArgumentException("Unable to create cart from the given order. Entry Number cannot be empty");
		}
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/modifyorder", method = RequestMethod.POST)
	@ResponseBody
	@Operation(operationId = "modifyOrder", summary = "Modify the Order", description = "order review - modify the order")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public ResponseEntity<Object> modifyOrder(@Parameter(description = "Orders with list of entries to add")@RequestBody(required = true) final OrderWsDTO orderDetails,
												 @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		if(orderDetails.getEntries() != null) {
			for (OrderEntryWsDTO entry : orderDetails.getEntries()) {
				if (entry.getQuantityMT() != null) {
					entry.setQuantity((long) (entry.getQuantityMT() * 1000));
				}
			}
		}

		validate(orderDetails, "orderModificationDetails", eydmsOrderModificationValidator);
		OrderData orderData = convertToOrderData(orderDetails);
		 final Transaction tx = Transaction.current();
		 tx.begin();
		try{
			 orderFacade.modifyOrder(orderData);
			 CartData cartData = orderFacade.getOrderForCode(orderData.getCode());
			 tx.commit();
			return ResponseEntity.status(HttpStatus.OK).body(getDataMapper().map(cartData, CartWsDTO.class, fields));
		}
		catch (Exception e){
			LOGGER.error("Exception occured "+e.getMessage());
			e.printStackTrace();
			tx.rollback();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	private OrderData convertToOrderData(OrderWsDTO orderDetails) {
		 OrderData orderData = new OrderData();
		 List<OrderEntryData> orderEntryDataList = new ArrayList<>();
		 orderData.setCode(orderDetails.getCode());
		 orderData.setTotalQuantity(orderDetails.getTotalQuantity());
		 orderData.setOrderSource(orderDetails.getOrderSource());
		 orderData.setErpCityCode(orderDetails.getErpCityCode());
		 orderData.setModificationReason(orderDetails.getModificationReason());

		 for(OrderEntryWsDTO entryWsDTO : orderDetails.getEntries()){
			 ProductData productData = new ProductData();
			 productData.setCode(entryWsDTO.getProduct().getCode());

			 OrderEntryData orderEntryData = new OrderEntryData();
			 orderEntryData.setEntryNumber(entryWsDTO.getEntryNumber());
			 orderEntryData.setProduct(productData);
			 orderEntryData.setQuantity(entryWsDTO.getQuantity());
			 orderEntryData.setTruckNo(entryWsDTO.getTruckNo());
			 orderEntryData.setDriverContactNo(entryWsDTO.getDriverContactNo());
			 orderEntryData.setSelectedDeliveryDate(entryWsDTO.getSelectedDeliveryDate());
			 orderEntryData.setSelectedDeliverySlot(entryWsDTO.getSelectedDeliverySlot());
			 orderEntryData.setCalculatedDeliveryDate(entryWsDTO.getCalculatedDeliveryDate());
			 orderEntryData.setCalculatedDeliverySlot(entryWsDTO.getCalculatedDeliverySlot());
			 orderEntryData.setSequence(entryWsDTO.getSequence());
			 orderEntryData.setQuantityMT(entryWsDTO.getQuantityMT());
			 orderEntryData.setWarehouseCode(entryWsDTO.getWarehouseCode());
			 orderEntryData.setRouteId(entryWsDTO.getRouteId());
			 orderEntryData.setRemarks(entryWsDTO.getRemarks());
			 orderEntryDataList.add(orderEntryData);
		 }
		 orderData.setEntries(orderEntryDataList);
		 orderData.setRequestedDeliveryDate(orderDetails.getRequestedDeliveryDate());
		 orderData.setRequestedDeliverySlot(orderDetails.getRequestedDeliverySlot());

		 return orderData;
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_B2BADMINGROUP", "ROLE_B2BAPPROVERGROUP" })
	@RequestMapping(value = "/{orderCode}/approve", method = RequestMethod.POST)
	@Operation(operationId = "doMakeOrderApprovalDecision", summary = "Makes an approval decision for an order.", description = "Makes a decision on the order approval that will trigger the next step of the order")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public OrderApprovalDecisionWsDTO orderApprovalDecision(
			@Parameter(description = "Code that identifies the order approval.", required = true) @PathVariable final String orderCode,
			@Parameter(description = "The order approval decision. The approval decision and comment field is mandatory.", required = true) @RequestBody final OrderApprovalDecisionWsDTO orderApprovalDecision,
			@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) throws EyDmsException {
		try {
			validate(orderApprovalDecision, "orderApproval", eydmsOrderApprovalDecisionValidator);

			final B2BOrderApprovalData b2bOrderApprovalData = getDataMapper().map(orderApprovalDecision,
					B2BOrderApprovalData.class);
			// b2bOrderApprovalData.setWorkflowActionModelCode(orderApprovalCode);
			orderFacade.updateOrderWithApprovalDecision(orderCode, b2bOrderApprovalData);
			return getDataMapper().map(b2bOrderApprovalData, OrderApprovalDecisionWsDTO.class, fields);
		}
		catch(Exception e) {
			throw new EyDmsException(e.getMessage(), e);
		}
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/getOrderHistoryForOrder", method = RequestMethod.GET)
	@ResponseBody
	@Operation(operationId = "getOrderHistoryForOrder", summary = "Get a eydms  order history as per status", description = "Returns order listing.")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public EyDmsOrderHistoryListWsDTO getOrderHistoryForOrder(@RequestParam(required = false) String orderStatus, @RequestParam(required = false) String filter, @Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
															@Parameter(description = "Optional {@link PaginationData} parameter in case of savedCartsOnly == true. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,
															@Parameter(description = "Product Name field") @RequestParam(required = false) final String productName,
															@Parameter(description = "Order Type  field") @RequestParam(required = false) final String orderType,@RequestParam(required = false, defaultValue = "all") final String spApprovalFilter,
															@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,final HttpServletResponse response, @RequestParam(required = false, defaultValue = "false") final Boolean isCreditLimitBreached, @RequestParam(required = false, defaultValue = "false") final Boolean approvalPending)
	{
		EyDmsOrderHistoryListData eydmsOrderHistoryListData = new EyDmsOrderHistoryListData();

		final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);

		SearchPageData<EyDmsOrderHistoryData> orderHistoryForOrder = orderFacade.getOrderHistoryForOrder(searchPageData, orderStatus, filter,productName,orderType,isCreditLimitBreached,spApprovalFilter,approvalPending);
		eydmsOrderHistoryListData.setOrdersList(orderHistoryForOrder.getResults());

		if (orderHistoryForOrder.getPagination() != null)
		{
			response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(orderHistoryForOrder.getPagination().getTotalNumberOfResults()));
		}
		return getDataMapper().map(eydmsOrderHistoryListData, EyDmsOrderHistoryListWsDTO.class, fields);

	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/getOrderHistoryForOrderEntry", method = RequestMethod.GET)
	@ResponseBody
	@Operation(operationId = "getOrderHistoryForOrderEntry", summary = "Get a eydms  order entry history as per status", description = "Returns order entry listing.")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public EyDmsOrderHistoryListWsDTO getOrderHistoryForOrderEntry(@RequestParam(required = false) String orderStatus,@RequestParam(required = false) String filter, @Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
																 @Parameter(description = "Optional {@link PaginationData} parameter in case of savedCartsOnly == true. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,
																 @Parameter(description = "Product Name field") @RequestParam(required = false) final String productName,
																 @Parameter(description = "Order Type  field") @RequestParam(required = false) final String orderType,@RequestParam(required = false, defaultValue = "all") final String spApprovalFilter,
																 @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,final HttpServletResponse response)
	{
		EyDmsOrderHistoryListData eydmsOrderHistoryListData = new EyDmsOrderHistoryListData();

		final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);

		SearchPageData<EyDmsOrderHistoryData> orderHistoryForOrderEntry = orderFacade.getOrderHistoryForOrderEntry(searchPageData, orderStatus, filter,productName,orderType,spApprovalFilter);
		eydmsOrderHistoryListData.setOrdersList(orderHistoryForOrderEntry.getResults());

		if (orderHistoryForOrderEntry.getPagination() != null)
		{
			response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(orderHistoryForOrderEntry.getPagination().getTotalNumberOfResults()));
		}
		
		return getDataMapper().map(eydmsOrderHistoryListData, EyDmsOrderHistoryListWsDTO.class, fields);
	}
	
	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/optimalDeliveryWindow", method = RequestMethod.GET)
	@ResponseBody
	@Operation(operationId = "getOptimalDeliveryWindow", summary = "Get Optimal Delivery dates And Slots.", description = "Get Optimal Delivery dates And Slots.")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public ResponseEntity<DeliveryDateAndSlotListData> getOptimalDeliveryWindow( @RequestParam final String routeId
			, @PathVariable final String userId, @RequestParam final Double orderQty, @RequestParam String sourceCode, @RequestParam(defaultValue = "false") String isDealerProvidingTruck)
	{
		DeliveryDateAndSlotListData deliveryDateAndSlots = orderFacade.fetchOptimalDeliveryWindow(orderQty, routeId, userId, sourceCode, isDealerProvidingTruck);
		return ResponseEntity.status(HttpStatus.OK).body(deliveryDateAndSlots);
	}
	
	@RequestMapping(value = "/optimalISODeliveryWindow", method = RequestMethod.GET)
	@ResponseBody
	@Operation(operationId = "optimalISODeliveryWindow", summary = "Get Optimal ISO Delivery dates And Slots.", description = "Get Optimal Delivery dates And Slots.")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public ResponseEntity<DeliveryDateAndSlotListData> getOptimalISODeliveryWindow( @RequestParam final String routeId
			, @PathVariable final String userId, @RequestParam final Double orderQty, @RequestParam String sourceCode, @RequestParam String depotCode)
	{
		DeliveryDateAndSlotListData deliveryDateAndSlots = orderFacade.fetchOptimalISODeliveryWindow(orderQty, routeId, userId, sourceCode, depotCode);
		return ResponseEntity.status(HttpStatus.OK).body(deliveryDateAndSlots);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/getCancelOrderHistoryForOrder", method = RequestMethod.GET)
	@ResponseBody
	@Operation(operationId = "getCancelOrderHistoryForOrder", summary = "Get a eydms cancel order history as per status", description = "Returns order listing.")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public EyDmsOrderHistoryListWsDTO getCancelOrderHistoryForOrder(@RequestParam(required = false) String orderStatus, @RequestParam(required = false) String filter, @Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
			@Parameter(description = "Optional {@link PaginationData} parameter in case of savedCartsOnly == true. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,
			@Parameter(description = "Product Name field") @RequestParam(required = false) final String productName,
																  @Parameter(description = "Order Type  field") @RequestParam(required = false) final String orderType,@RequestParam(required = false, defaultValue = "all") final String spApprovalFilter,
																  @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,final HttpServletResponse response, @Parameter(description = "Filters by Month") @RequestParam(required = false, defaultValue = "0") Integer month,
																  @Parameter(description = "Filters by Year") @RequestParam(required = false, defaultValue = "0") Integer year)
	{
		EyDmsOrderHistoryListData eydmsOrderHistoryListData = new EyDmsOrderHistoryListData();

		final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);

		SearchPageData<EyDmsOrderHistoryData> orderHistoryForOrder = orderFacade.getCancelOrderHistoryForOrder(searchPageData, orderStatus, filter,productName,orderType,spApprovalFilter,month,year);
		eydmsOrderHistoryListData.setOrdersList(orderHistoryForOrder.getResults());

		if (orderHistoryForOrder.getPagination() != null)
		{
			response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(orderHistoryForOrder.getPagination().getTotalNumberOfResults()));
		}
		return getDataMapper().map(eydmsOrderHistoryListData, EyDmsOrderHistoryListWsDTO.class, fields);

	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/getCancelOrderHistoryForOrderEntry", method = RequestMethod.GET)
	@ResponseBody
	@Operation(operationId = "getCancelOrderHistoryForOrderEntry", summary = "Get a eydms cancel order entry history as per status", description = "Returns order entry listing.")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public EyDmsOrderHistoryListWsDTO getCancelOrderHistoryForOrderEntry(@RequestParam(required = false) String orderStatus,@RequestParam(required = false) String filter, @Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
			@Parameter(description = "Optional {@link PaginationData} parameter in case of savedCartsOnly == true. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,
			@Parameter(description = "Product Name field") @RequestParam(required = false) final String productName,
			@Parameter(description = "Order Type  field") @RequestParam(required = false) final String orderType,@RequestParam(required = false, defaultValue = "all") final String spApprovalFilter,
																	   @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,final HttpServletResponse response, @Parameter(description = "Filters by Month") @RequestParam(required = false, defaultValue = "0") Integer month,
																	   @Parameter(description = "Filters by Year") @RequestParam(required = false, defaultValue = "0") Integer year)
	{
		EyDmsOrderHistoryListData eydmsOrderHistoryListData = new EyDmsOrderHistoryListData();

		final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);

		SearchPageData<EyDmsOrderHistoryData> orderHistoryForOrderEntry = orderFacade.getCancelOrderHistoryForOrderEntry(searchPageData, orderStatus, filter,productName,orderType,spApprovalFilter,month,year);
		eydmsOrderHistoryListData.setOrdersList(orderHistoryForOrderEntry.getResults());

		if (orderHistoryForOrderEntry.getPagination() != null)
		{
			response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(orderHistoryForOrderEntry.getPagination().getTotalNumberOfResults()));
		}

		return getDataMapper().map(eydmsOrderHistoryListData, EyDmsOrderHistoryListWsDTO.class, fields);
	}
	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/getVehicleArrivalConfirmationForOrder", method = RequestMethod.GET)
	@ResponseBody
	@Operation(operationId = "getVehicleArrivalConfirmationForOrder", summary = "Get vehicle arrival confirmation as per status", description = "Vehicle Arrival Confirmation.")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public Boolean getVehicleArrivalConfirmationForOrder(@RequestParam boolean vehicleArrived, @RequestParam String orderCode, @RequestParam String entryNumber)
	{
		return orderFacade.getVehicleArrivalConfirmationForOrder(vehicleArrived, orderCode, entryNumber);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/updateEpodStatusForOrder", method = RequestMethod.GET)
	@ResponseBody
	@Operation(operationId = "updateEpodStatusForOrder", summary = "Get EPOD status", description = "Get EPOD Status.")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public Boolean updateEpodStatusForOrder(@RequestParam double shortageQuantity, @RequestParam String orderCode, @RequestParam int entryNumber)
	{
		return orderFacade.updateEpodStatusForOrder(shortageQuantity,orderCode, entryNumber);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/getOrderFromRetailersRequest", method = RequestMethod.GET)
	@ResponseBody
	@Operation(operationId = "getOrderFromRetailersRequest", summary = "\n" +
			"Orders from retailers- self stock or reject request", description = "\n" +
			"Orders from retailers- self stock or reject request.")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public Boolean getOrderFromRetailersRequest(@RequestParam String requisitionId, @RequestParam String status) {
		return orderFacade.getOrderFromRetailersRequest(requisitionId, status);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/getEpodListBasedOnOrderStatus", method = RequestMethod.GET)
	@ResponseBody
	@Operation(operationId = "getEpodListBasedOnOrderStatus", summary = "Get EPOD List as per status", description = "Returns EPOD List.")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public EyDmsOrderHistoryListWsDTO getEpodListBasedOnOrderStatus(@RequestParam List<String> Status, @RequestParam(required = false) String filter, @Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
																 @Parameter(description = "Optional {@link PaginationData} parameter in case of savedCartsOnly == true. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,
																 @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,final HttpServletResponse response)
	{
		EyDmsOrderHistoryListData eydmsOrderHistoryListData = new EyDmsOrderHistoryListData();

		final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);

		SearchPageData<EyDmsOrderHistoryData> orderHistoryForOrderEntry = orderFacade.getEpodListBasedOnOrderStatus(searchPageData, Status, filter);
		eydmsOrderHistoryListData.setOrdersList(orderHistoryForOrderEntry.getResults());

		if (orderHistoryForOrderEntry.getPagination() != null)
		{
			response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(orderHistoryForOrderEntry.getPagination().getTotalNumberOfResults()));
		}

		return getDataMapper().map(eydmsOrderHistoryListData, EyDmsOrderHistoryListWsDTO.class, fields);

	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/getEpodFeedback", method = RequestMethod.POST)
	@ResponseBody
	@Operation(operationId = "getEpodFeedback", summary = "Get EPOD Feedback", description = "Returns Feedback.")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public Boolean getEpodFeedback(@Parameter(description = "Data object that contains information necessary for order feedback details", required = true) @RequestBody final EpodFeedbackData epodFeedbackData)
	{
		return orderFacade.getEpodFeedback(epodFeedbackData);

	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/updateSpApprovalStatus", method = RequestMethod.POST)
	@ResponseBody
	@Operation(operationId = "updateSpApprovalStatus", summary = "Update SP Approval Status", description = "Returns Feedback.")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public Boolean updateSpApprovalStatus(@RequestParam final String orderCode, @RequestParam final String status, @RequestParam(required = false) String spRejectionReason)
	{
		return orderFacade.updateSpApprovalStatus(orderCode,status, spRejectionReason);

	}

	@Secured({ "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_B2BADMINGROUP", "ROLE_B2BAPPROVERGROUP" })
	@RequestMapping(value = "/{orderCode}/approveOrder", method = RequestMethod.POST)
	@Operation(operationId = "doMakeOrderApprovalDecision", summary = "Makes an approval decision for an order.", description = "Makes a decision on the order approval that will trigger the next step of the order")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@ApiBaseSiteIdAndTerritoryParam
	public OrderApprovalDecisionWsDTO approveOrder(
			@PathVariable final String orderCode,
			@RequestBody final OrderApprovalDecisionWsDTO orderApprovalDecision,
			@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) throws EyDmsException {
		B2BOrderApprovalData b2bOrderApprovalData = getDataMapper().map(orderApprovalDecision,
				B2BOrderApprovalData.class);
		b2bOrderApprovalData = orderFacade.approveOrder(orderCode, b2bOrderApprovalData);
		return getDataMapper().map(b2bOrderApprovalData, OrderApprovalDecisionWsDTO.class, fields);
	}

}
