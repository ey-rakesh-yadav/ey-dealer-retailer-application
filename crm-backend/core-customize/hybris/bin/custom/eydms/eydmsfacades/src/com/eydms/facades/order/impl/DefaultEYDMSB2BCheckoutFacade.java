package com.eydms.facades.order.impl;

import com.eydms.core.depot.operations.dao.DepotOperationsDao;
import com.eydms.core.enums.CustomerCategory;
import de.hybris.platform.b2bacceleratorfacades.checkout.data.PlaceOrderData;
import de.hybris.platform.b2bacceleratorfacades.exception.EntityValidationException;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BCommentData;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BReplenishmentRecurrenceEnum;
import de.hybris.platform.b2bacceleratorfacades.order.data.TriggerData;
import de.hybris.platform.b2bacceleratorfacades.order.impl.DefaultB2BAcceleratorCheckoutFacade;
import de.hybris.platform.b2b.enums.CheckoutPaymentType;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.payment.dto.TransactionStatus;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static de.hybris.platform.util.localization.Localization.getLocalizedString;

public class DefaultEYDMSB2BCheckoutFacade extends DefaultB2BAcceleratorCheckoutFacade {

    private static final String CART_CHECKOUT_TRANSACTION_NOT_AUTHORIZED = "cart.transation.notAuthorized";
    private static final String CART_CHECKOUT_NO_QUOTE_DESCRIPTION = "cart.no.quote.description";
    private static final String CART_CHECKOUT_REPLENISHMENT_NO_STARTDATE = "cart.replenishment.no.startdate";
    private static final String CART_CHECKOUT_REPLENISHMENT_NO_FREQUENCY = "cart.replenishment.no.frequency";
    
    @Override
    public <T extends AbstractOrderData> T placeOrder(final PlaceOrderData placeOrderData) throws InvalidCartException
    {
        // for CARD type, transaction must be authorized before placing order
        final boolean isCardtPaymentType = CheckoutPaymentType.CARD.equals(getCart().getPaymentType());
        if (isCardtPaymentType)
        {
            final List<PaymentTransactionModel> transactions = getCart().getPaymentTransactions();
            boolean authorized = false;
            for (final PaymentTransactionModel transaction : transactions)
            {
                for (final PaymentTransactionEntryModel entry : transaction.getEntries())
                {
                    if (entry.getType().equals(PaymentTransactionType.AUTHORIZATION)
                            && TransactionStatus.ACCEPTED.name().equals(entry.getTransactionStatus()))
                    {
                        authorized = true;
                        break;
                    }
                }
            }
            if (!authorized)
            {
                // FIXME - change error message
                throw new EntityValidationException(getLocalizedString(CART_CHECKOUT_TRANSACTION_NOT_AUTHORIZED));
            }
        }

        if (isValidCheckoutCart(placeOrderData))
        {
          final CartData cartData = new CartData();
            if(null!=placeOrderData.getIsNTOrder()){
                cartData.setCustomerCategory(placeOrderData.getIsNTOrder() ? CustomerCategory.NT.getCode(): CustomerCategory.T.getCode());
            }
            cartData.setNcrGapAcceptanceReason(placeOrderData.getNcrGapAcceptanceReason());
            updateCheckoutCartWithExtAttribute(cartData);
            // validate quote negotiation
            if (placeOrderData.getNegotiateQuote() != null && placeOrderData.getNegotiateQuote().equals(Boolean.TRUE))
            {
                if (StringUtils.isBlank(placeOrderData.getQuoteRequestDescription()))
                {
                    throw new EntityValidationException(getLocalizedString(CART_CHECKOUT_NO_QUOTE_DESCRIPTION));
                }
                else
                {
                    final B2BCommentData b2BComment = new B2BCommentData();
                    b2BComment.setComment(placeOrderData.getQuoteRequestDescription());

                    cartData.setB2BComment(b2BComment);

                    updateCheckoutCart(cartData);
                }
            }

            // validate replenishment
            if (placeOrderData.getReplenishmentOrder() != null && placeOrderData.getReplenishmentOrder().equals(Boolean.TRUE))
            {
                if (placeOrderData.getReplenishmentStartDate() == null)
                {
                    throw new EntityValidationException(getLocalizedString(CART_CHECKOUT_REPLENISHMENT_NO_STARTDATE));
                }

                if (placeOrderData.getReplenishmentRecurrence().equals(B2BReplenishmentRecurrenceEnum.WEEKLY)
                        && CollectionUtils.isEmpty(placeOrderData.getNDaysOfWeek()))
                {
                    throw new EntityValidationException(getLocalizedString(CART_CHECKOUT_REPLENISHMENT_NO_FREQUENCY));
                }

                final TriggerData triggerData = new TriggerData();
                populateTriggerDataFromPlaceOrderData(placeOrderData, triggerData);

                return (T) scheduleOrder(triggerData);
            }

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
        return getCheckoutCart();
    }
}
