package com.scl.occ.validators;

import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class DefaultSCLB2BPlaceOrderCartValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return CartData.class.equals(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {

        final CartData cart = (CartData) target;

        if (!cart.isCalculated())
        {
            errors.reject("cart.notCalculated");
        }
        if(cart.getCrmOrderType()!=null && cart.getCrmOrderType().equalsIgnoreCase("GIFT")) {
            if (cart.getDeliveryAddress() == null)
            {
                errors.reject("cart.deliveryAddressNotSet");
            }

            if (cart.getDeliveryMode() == null)
            {
                errors.reject("cart.deliveryModeNotSet");
            }
        }
        else {
            for(OrderEntryData cartEntry: cart.getEntries()) {
                if (cartEntry.getDeliveryAddress() == null)
                {
                    errors.reject("cart.entry.deliveryAddressNotSet", String.valueOf(cartEntry.getEntryNumber()));
                }
                if (cartEntry.getDeliveryMode() == null)
                {
                    errors.reject("cart.entry.deliveryModeNotSet", String.valueOf(cartEntry.getEntryNumber()));
                }
                if (cartEntry.getWarehouseCode() == null)
                {
                    errors.reject("cart.entry.sourceNotSet", String.valueOf(cartEntry.getEntryNumber()));
                }
            }
        }


//        if (cart.getDeliveryAddress() == null)
//        {
//            errors.reject("cart.deliveryAddressNotSet");
//        }
//
//        if (cart.getDeliveryMode() == null)
//        {
//            errors.reject("cart.deliveryModeNotSet");
//        }

        /*if (CheckoutPaymentType.CARD.getCode().equals(cart.getPaymentType().getCode()))
        {
            if (cart.getPaymentInfo() == null)
            {
                errors.reject("cart.paymentInfoNotSet");
            }
        }
        else
        {
            if (cart.getCostCenter() == null)
            {
                errors.reject("cart.costCenterNotSet");
            }
        }*/

    }
}
