package com.scl.facades.order.impl;

import com.scl.core.dao.SclUserDao;
import com.scl.core.depot.operations.dao.DepotOperationsDao;
import com.scl.core.enums.CRMOrderType;
import com.scl.core.enums.CustomerCategory;
import com.scl.core.model.DealerRetailerMappingModel;
import com.scl.core.model.SclCustomerModel;

import de.hybris.platform.b2bacceleratorfacades.checkout.data.PlaceOrderData;
import de.hybris.platform.b2bacceleratorfacades.exception.EntityValidationException;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BCommentData;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BPaymentTypeData;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BReplenishmentRecurrenceEnum;
import de.hybris.platform.b2bacceleratorfacades.order.data.TriggerData;
import de.hybris.platform.b2bacceleratorfacades.order.impl.DefaultB2BAcceleratorCheckoutFacade;
import de.hybris.platform.b2b.enums.CheckoutPaymentType;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.payment.dto.TransactionStatus;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static de.hybris.platform.util.localization.Localization.getLocalizedString;

public class DefaultSCLB2BCheckoutFacade extends DefaultB2BAcceleratorCheckoutFacade {

    private static final String CART_CHECKOUT_TRANSACTION_NOT_AUTHORIZED = "cart.transation.notAuthorized";
    private static final String CART_CHECKOUT_NO_QUOTE_DESCRIPTION = "cart.no.quote.description";
    private static final String CART_CHECKOUT_REPLENISHMENT_NO_STARTDATE = "cart.replenishment.no.startdate";
    private static final String CART_CHECKOUT_REPLENISHMENT_NO_FREQUENCY = "cart.replenishment.no.frequency";
    
    private static final String CART_CHECKOUT_DELIVERYADDRESS_INVALID = "cart.deliveryAddress.invalid";
	private static final String CART_CHECKOUT_NOT_CALCULATED = "cart.not.calculated";
    
    @Autowired
    SclUserDao sclUserDao;
    
    protected boolean isValidCheckoutCart(final PlaceOrderData placeOrderData)
    {
    	final CartData cartData = getCheckoutCart();
    	final boolean valid = true;

    	if (!cartData.isCalculated())
    	{
    		throw new EntityValidationException(getLocalizedString(CART_CHECKOUT_NOT_CALCULATED));
    	}

		if(cartData.getCrmOrderType()!=null && cartData.getCrmOrderType().equalsIgnoreCase("GIFT")) {
			// TODO
			if(cartData.getDeliveryAddress()==null) {
				throw new EntityValidationException(getLocalizedString(CART_CHECKOUT_DELIVERYADDRESS_INVALID));
			}
		}
		else if (CollectionUtils.isNotEmpty(cartData.getEntries()))
    	{
    		cartData.getEntries().forEach(obj-> {
    			if(obj.getDeliveryAddress()==null)
    				throw new EntityValidationException(getLocalizedString(CART_CHECKOUT_DELIVERYADDRESS_INVALID));
    		});
    	}
    	return valid;
    }

    
    @Override
    public <T extends AbstractOrderData> T placeOrder(final PlaceOrderData placeOrderData) throws InvalidCartException
    {
    	if (isValidCheckoutCart(placeOrderData))
    	{
    		final CartData cartData = new CartData();
    		if(null!=placeOrderData.getIsNTOrder()){
    			cartData.setCustomerCategory(placeOrderData.getIsNTOrder() ? CustomerCategory.NT.getCode(): CustomerCategory.TR.getCode());
    		}
    		cartData.setNcrGapAcceptanceReason(placeOrderData.getNcrGapAcceptanceReason());
    		updateCheckoutCartWithExtAttribute(cartData);
    		return (T) super.placeOrder();
    	}

    	return null;
    }


    
    private CartData updateCheckoutCartWithExtAttribute(final CartData cartData) {

        final CartModel cartModel = getCart();
        if (cartModel == null)
        {
            return null;
        }
        if(null != cartData.getCustomerCategory()) {
            cartModel.setCustomerCategory(CustomerCategory.valueOf(cartData.getCustomerCategory()));
        }
        cartModel.setNcrGapAcceptanceReason(cartData.getNcrGapAcceptanceReason());
        getModelService().save(cartModel);
		if(!(cartModel.getCrmOrderType()!=null && cartModel.getCrmOrderType().equals(CRMOrderType.GIFT))) {
			updateDealerRetailerMapping(cartModel);
		}
        return getCheckoutCart();
    }
    
    private void updateDealerRetailerMapping(CartModel cart) {
    	try {
    		if(CollectionUtils.isNotEmpty(cart.getEntries())) {
    			for(AbstractOrderEntryModel cartEntry : cart.getEntries()) {
					SclCustomerModel retailer=Objects.nonNull(cartEntry.getRetailer())?cartEntry.getRetailer():null;
					String partnerFunctionId=Objects.nonNull(cartEntry.getDeliveryAddress())?cartEntry.getDeliveryAddress().getPartnerFunctionId(): Strings.EMPTY;
					DealerRetailerMappingModel dealerRetailerMapping = sclUserDao.getDealerRetailerMapping((SclCustomerModel) cart.getUser(), retailer,partnerFunctionId);
					dealerRetailerMapping.setLastUsed(new Date());
					getModelService().save(dealerRetailerMapping);

    			}
    		}
    	}
    	catch(Exception e) {
    		
    	}
    }
}
