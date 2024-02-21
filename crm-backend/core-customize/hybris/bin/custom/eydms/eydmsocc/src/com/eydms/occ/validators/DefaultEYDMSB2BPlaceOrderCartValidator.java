package com.eydms.occ.validators;

import de.hybris.platform.commercefacades.order.data.CartData;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class DefaultEYDMSB2BPlaceOrderCartValidator implements Validator {

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

        if (cart.getDeliveryAddress() == null)
        {
            errors.reject("cart.deliveryAddressNotSet");
        }

        if (cart.getDeliveryMode() == null)
        {
            errors.reject("cart.deliveryModeNotSet");
        }

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
