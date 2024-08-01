package com.scl.core.event;

import com.scl.core.jalo.SclOrderCancelProcess;
import com.scl.core.model.SclAddressProcessModel;
import com.scl.core.model.SclOrderCancelProcessModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.servicelayer.event.events.AbstractEvent;

public class SclOrderCancelEvent extends AbstractEvent {

    private final SclOrderCancelProcessModel orderProcess;

    public SclOrderCancelEvent(final SclOrderCancelProcessModel process)
    {
        this.orderProcess = process;
    }

    public SclOrderCancelProcessModel getProcess()
    {
        return this.orderProcess;
    }
}
