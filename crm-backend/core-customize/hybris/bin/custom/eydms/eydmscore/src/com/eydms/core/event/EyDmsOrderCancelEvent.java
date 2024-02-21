package com.eydms.core.event;

import com.eydms.core.jalo.EyDmsOrderCancelProcess;
import com.eydms.core.model.EyDmsAddressProcessModel;
import com.eydms.core.model.EyDmsOrderCancelProcessModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.servicelayer.event.events.AbstractEvent;

public class EyDmsOrderCancelEvent extends AbstractEvent {

    private final EyDmsOrderCancelProcessModel orderProcess;

    public EyDmsOrderCancelEvent(final EyDmsOrderCancelProcessModel process)
    {
        this.orderProcess = process;
    }

    public EyDmsOrderCancelProcessModel getProcess()
    {
        return this.orderProcess;
    }
}
