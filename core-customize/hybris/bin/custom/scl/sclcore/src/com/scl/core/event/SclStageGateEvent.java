package com.scl.core.event;

import com.scl.core.model.SclOutboundStageGateProcessModel;
import de.hybris.platform.servicelayer.event.events.AbstractEvent;

public class SclStageGateEvent extends AbstractEvent {

    private final SclOutboundStageGateProcessModel process;

    public SclStageGateEvent(SclOutboundStageGateProcessModel process) {
        this.process = process;
    }

    public SclOutboundStageGateProcessModel getProcess()
    {
        return this.process;
    }
}
