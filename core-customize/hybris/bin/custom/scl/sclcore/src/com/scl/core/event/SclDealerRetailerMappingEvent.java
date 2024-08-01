package com.scl.core.event;

import com.scl.core.model.DealerRetailerMappingModel;
import de.hybris.platform.servicelayer.event.events.AbstractEvent;

public class SclDealerRetailerMappingEvent extends AbstractEvent {

    private final DealerRetailerMappingModel dealerRetailer;

    public SclDealerRetailerMappingEvent(DealerRetailerMappingModel dealerRetailerMappingModel) {
        this.dealerRetailer = dealerRetailerMappingModel;
    }

    public DealerRetailerMappingModel getDealerRetailer()
    {
        return this.dealerRetailer;
    }
}
