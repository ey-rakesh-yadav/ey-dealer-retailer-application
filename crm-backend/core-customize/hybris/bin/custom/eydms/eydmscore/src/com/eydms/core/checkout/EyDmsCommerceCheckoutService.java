package com.eydms.core.checkout;

import de.hybris.platform.commerceservices.order.impl.DefaultCommerceCheckoutService;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;

public interface EyDmsCommerceCheckoutService {

    public boolean setCartDetails(CommerceCheckoutParameter parameter);
}
