package com.scl.core.orderhandler;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.servicelayer.model.attribute.DynamicAttributeHandler;

public class DynamicAttributesOrderEntryQtyMT implements DynamicAttributeHandler<Double, AbstractOrderEntryModel> {
    @Override
    public Double get(AbstractOrderEntryModel orderEntry) {
        if(orderEntry == null)
            throw new IllegalArgumentException("Item model is required!!!");
        else
        return orderEntry.getQuantity() == null ? 0.0 : (double) orderEntry.getQuantity()/1000;
    }

    @Override
    public void set(AbstractOrderEntryModel orderEntry, Double aDouble) {
        throw new UnsupportedOperationException();
    }
}
