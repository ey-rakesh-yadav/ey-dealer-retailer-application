package com.eydms.core.event;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.event.events.AbstractEvent;

public class UpdateDealerRetailerMappingEvent extends AbstractEvent {

    private final OrderModel order;

    public UpdateDealerRetailerMappingEvent(OrderModel order) {
        this.order = order;
    }

    public OrderModel getOrder(){
        return order;
    }
}
