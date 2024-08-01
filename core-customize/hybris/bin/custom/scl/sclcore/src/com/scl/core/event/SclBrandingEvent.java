package com.scl.core.event;

import com.scl.core.model.SclAddressProcessModel;
import com.scl.core.model.SclBrandingProcessModel;
import de.hybris.platform.servicelayer.event.events.AbstractEvent;

public class SclBrandingEvent extends AbstractEvent {

    private final SclBrandingProcessModel process;

    public SclBrandingEvent(SclBrandingProcessModel process) {
        this.process = process;
    }

    public SclBrandingProcessModel getProcess()
    {
        return this.process;
    }
}
