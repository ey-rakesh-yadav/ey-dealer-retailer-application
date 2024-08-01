package com.scl.facades.impl;

import static de.hybris.platform.util.localization.Localization.getLocalizedString;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.cart.service.SclB2BCartService;
import com.scl.facades.cart.SclB2BCartFacade;
import com.scl.facades.order.data.SclOrderHistoryData;


import de.hybris.platform.b2bacceleratorfacades.exception.DomainException;
import de.hybris.platform.b2bacceleratorfacades.exception.EntityValidationException;
import de.hybris.platform.b2bacceleratorfacades.order.impl.DefaultB2BCartFacade;
import de.hybris.platform.commercefacades.order.data.AddToCartParams;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.CartModificationData;
import de.hybris.platform.commercefacades.order.data.CommerceSaveCartParameterData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commerceservices.order.CommerceCartService;
import de.hybris.platform.commerceservices.order.CommerceSaveCartException;
import de.hybris.platform.commerceservices.order.CommerceSaveCartService;
import de.hybris.platform.converters.Converters;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.time.TimeService;
import de.hybris.platform.servicelayer.user.UserService;

public class SclB2BCartFacadeImpl extends DefaultB2BCartFacade implements SclB2BCartFacade {

	private static final String BASKET_QUANTITY_ERROR_KEY = "basket.error.quantity.invalid";
	private static final String CART_MODIFICATION_ERROR = "basket.error.occurred";
	private static int DEFAULT_SAVE_CART_EXPIRY_DAYS = 30;
	
	private SclB2BCartService sclB2BCartService;
	private UserService userService;
	
	@Autowired
	private TimeService timeService;

	@Autowired
	private CommerceCartService commerceCartService;

	@Autowired
	Converter<AbstractOrderModel, SclOrderHistoryData> sclOrderHistoryCardConverter;
	
	@Autowired
	ModelService modelService;
	
	@Autowired
	ConfigurationService configurationService;
	
	@Override
	public CartModificationData addOrderEntry(final OrderEntryData cartEntry)
	{
		if (!isValidEntry(cartEntry))
		{
			throw new EntityValidationException(getLocalizedString(BASKET_QUANTITY_ERROR_KEY));
		}
		CartModificationData cartModification = null;
		
		final AddToCartParams addToCartParams = new AddToCartParams();
		addToCartParams.setProductCode(cartEntry.getProduct().getCode());
		addToCartParams.setQuantity(cartEntry.getQuantity());
		addToCartParams.setTruckNo(cartEntry.getTruckNo());
		addToCartParams.setDriverContactNo(cartEntry.getDriverContactNo());
		addToCartParams.setSelectedDeliveryDate(cartEntry.getSelectedDeliveryDate());
		addToCartParams.setSelectedDeliverySlot(cartEntry.getSelectedDeliverySlot());
		addToCartParams.setSequence(cartEntry.getSequence());
		addToCartParams.setCalculatedDeliveryDate(cartEntry.getCalculatedDeliveryDate());		
		addToCartParams.setCalculatedDeliverySlot(cartEntry.getCalculatedDeliverySlot());
		addToCartParams.setQuantityMT(cartEntry.getQuantityMT());
		addToCartParams.setWarehouseCode(cartEntry.getWarehouseCode());
		addToCartParams.setRouteId(cartEntry.getRouteId());
		addToCartParams.setRemarks(cartEntry.getRemarks());
		addToCartParams.setAddressPk(cartEntry.getAddressPk());
		addToCartParams.setRetailerUid(cartEntry.getRetailerUid());
		addToCartParams.setIncoTerm(cartEntry.getIncoTerm());
		addToCartParams.setOrderFor(cartEntry.getOrderFor());
		addToCartParams.setIsDealerProvidingOwnTransport(cartEntry.getIsDealerProvidingOwnTransport());
		addToCartParams.setDeliveryMode(cartEntry.getDeliveryMode());
		addToCartParams.setOrderRequisitionId(cartEntry.getOrderRequisitionId());
		addToCartParams.setPlacedByCustomer(cartEntry.getPlacedByCustomer());
		addToCartParams.setIsPartnerCustomer(cartEntry.getIsPartnerCustomer());
		addToCartParams.setProductAliasName(cartEntry.getProduct().getName());
		try{
			cartModification = getCartFacade().addToCart(addToCartParams);
		}
		catch (final CommerceCartModificationException e)
		{
			throw new DomainException(getLocalizedString(CART_MODIFICATION_ERROR), e);
		}
		setAddStatusMessage(cartEntry, cartModification);
		return cartModification;
	}

	@Override
	public SearchPageData<SclOrderHistoryData> getSavedCartsBySavedBy(SearchPageData searchPageData, String filter, int month, int year,String productName,String orderType) {
		
		final SearchPageData<SclOrderHistoryData> result = new SearchPageData<>();
		final SearchPageData<CartModel> savedCartModels = getSclB2BCartService().getSavedCartsBySavedBy(getUserService().getCurrentUser(), searchPageData, filter, month, year,productName,orderType);

		result.setPagination(savedCartModels.getPagination());
		result.setSorts(savedCartModels.getSorts());

		List<SclOrderHistoryData> sclOrderHistoryData = sclOrderHistoryCardConverter.convertAll(savedCartModels.getResults());

		//final List<CartData> savedCartDatas = Converters.convertAll(savedCartModels.getResults(), getCartConverter());

		result.setResults(sclOrderHistoryData);
		return result;
	}

	public SclB2BCartService getSclB2BCartService() {
		return sclB2BCartService;
	}

	public void setSclB2BCartService(SclB2BCartService sclB2BCartService) {
		this.sclB2BCartService = sclB2BCartService;
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@Override
	public boolean saveCart(CommerceSaveCartParameterData inputParameters, String employeeCode) throws CommerceSaveCartException {
		CartModel cartToBeSaved = null;

		if (StringUtils.isEmpty(inputParameters.getCartId()))
		{
			cartToBeSaved = getCartService().getSessionCart();
		}
		else
		{
			cartToBeSaved = commerceCartService.getCartForCodeAndUser(inputParameters.getCartId(),
					getUserService().getCurrentUser());

			if (cartToBeSaved == null)
			{
				throw new CommerceSaveCartException("Cannot find a cart for code [" + inputParameters.getCartId() + "]");
			}
		}

		final Date currentDate = timeService.getCurrentTime();

		cartToBeSaved.setExpirationTime(calculateExpirationTime(currentDate));
		cartToBeSaved.setSaveTime(currentDate);
		cartToBeSaved.setSavedBy(userService.getUserForUID(employeeCode));
		cartToBeSaved.setName(inputParameters.getName());
		cartToBeSaved.setDescription(inputParameters.getDescription());

		//saveCartResult.setSavedCart(cartToBeSaved);
		modelService.save(cartToBeSaved);
		modelService.refresh(cartToBeSaved);
		
		return true;
	}
	
	protected Date calculateExpirationTime(final Date currentDate)
	{
		final Integer expirationDays = configurationService.getConfiguration().getInteger(
				"commerceservices.saveCart.expiryTime.days", Integer.valueOf(DEFAULT_SAVE_CART_EXPIRY_DAYS));
		return new DateTime(currentDate).plusDays(expirationDays.intValue()).toDate();
	}
	
}
