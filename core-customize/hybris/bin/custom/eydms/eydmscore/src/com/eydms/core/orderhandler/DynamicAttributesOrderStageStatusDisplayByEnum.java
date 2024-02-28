package com.eydms.core.orderhandler;

import com.eydms.core.model.OrderStageModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.model.attribute.DynamicAttributeHandler;

public class DynamicAttributesOrderStageStatusDisplayByEnum implements DynamicAttributeHandler<String, OrderStageModel> {

    private EnumerationService enumerationService;

    @Override
    public String get(OrderStageModel orderStage) {
        if (orderStage == null) {
            throw new IllegalArgumentException("Item model is required");
        } else {
            return orderStage.getName() == null ? "" : this.enumerationService.getEnumerationName(orderStage.getName());
        }
    }

    @Override
    public void set(OrderStageModel model, String s) {
        throw new UnsupportedOperationException();

    }

    public void setEnumerationService(EnumerationService enumerationService) {
        this.enumerationService = enumerationService;
    }
}
