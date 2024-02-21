package com.eydms.core.event;

import com.eydms.core.model.EyDmsAddressProcessModel;
import com.eydms.core.model.EyDmsBrandingProcessModel;
import de.hybris.platform.servicelayer.event.events.AbstractEvent;

public class EyDmsBrandingEvent extends AbstractEvent {

    private final EyDmsBrandingProcessModel process;

    public EyDmsBrandingEvent(EyDmsBrandingProcessModel process) {
        this.process = process;
    }

    public EyDmsBrandingProcessModel getProcess()
    {
        return this.process;
    }
}
