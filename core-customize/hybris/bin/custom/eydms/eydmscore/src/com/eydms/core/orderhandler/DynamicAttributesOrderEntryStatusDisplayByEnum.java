package com.eydms.core.orderhandler;


import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.model.attribute.DynamicAttributeHandler;

public class DynamicAttributesOrderEntryStatusDisplayByEnum implements DynamicAttributeHandler<String, OrderEntryModel> {

    private EnumerationService enumerationService;

    @Override
    public String get(OrderEntryModel orderEntry) {
        if (orderEntry == null) {
            throw new IllegalArgumentException("Item model is required");
        } else {
            return orderEntry.getStatus() == null ? "" : this.enumerationService.getEnumerationName(orderEntry.getStatus());
        }
    }

    @Override
    public void set(OrderEntryModel model, String s) {
        throw new UnsupportedOperationException();

    }

    public void setEnumerationService(EnumerationService enumerationService) {
        this.enumerationService = enumerationService;
    }
}
