package com.scl.core.checkout;

import de.hybris.platform.commerceservices.order.impl.DefaultCommerceCheckoutService;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;

public interface SclCommerceCheckoutService {

    public boolean setCartDetails(CommerceCheckoutParameter parameter);
}
