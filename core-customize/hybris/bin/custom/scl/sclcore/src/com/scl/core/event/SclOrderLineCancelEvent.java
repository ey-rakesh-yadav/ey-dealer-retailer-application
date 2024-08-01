package com.scl.core.event;

import com.scl.core.jalo.SclOrderCancelProcess;
import com.scl.core.jalo.SclOrderLineCancelProcess;
import com.scl.core.model.SclOrderLineCancelProcessModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.servicelayer.event.events.AbstractEvent;

public class SclOrderLineCancelEvent extends AbstractEvent {

    private final SclOrderLineCancelProcessModel orderLineCancelProcess;

    public SclOrderLineCancelEvent(final SclOrderLineCancelProcessModel process)
    {
        this.orderLineCancelProcess = process;
    }

    public SclOrderLineCancelProcessModel getProcess()
    {
        return this.orderLineCancelProcess;
    }
}
