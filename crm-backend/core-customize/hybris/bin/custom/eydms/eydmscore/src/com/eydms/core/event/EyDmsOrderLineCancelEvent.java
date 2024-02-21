package com.eydms.core.event;

import com.eydms.core.jalo.EyDmsOrderCancelProcess;
import com.eydms.core.jalo.EyDmsOrderLineCancelProcess;
import com.eydms.core.model.EyDmsOrderLineCancelProcessModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.servicelayer.event.events.AbstractEvent;

public class EyDmsOrderLineCancelEvent extends AbstractEvent {

    private final EyDmsOrderLineCancelProcessModel orderLineCancelProcess;

    public EyDmsOrderLineCancelEvent(final EyDmsOrderLineCancelProcessModel process)
    {
        this.orderLineCancelProcess = process;
    }

    public EyDmsOrderLineCancelProcessModel getProcess()
    {
        return this.orderLineCancelProcess;
    }
}
