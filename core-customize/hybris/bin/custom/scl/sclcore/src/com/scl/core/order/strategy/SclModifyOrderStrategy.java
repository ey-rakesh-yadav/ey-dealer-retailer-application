package com.scl.core.order.strategy;

import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;

public interface SclModifyOrderStrategy {

    void modiyOrderEntry(final OrderModel orderModel , final CommerceCartParameter parameter ) throws CommerceCartModificationException;
}
