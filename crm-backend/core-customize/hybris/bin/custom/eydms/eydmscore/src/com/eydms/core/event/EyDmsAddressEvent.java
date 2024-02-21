package com.eydms.core.event;

import com.eydms.core.model.EyDmsAddressProcessModel;

import de.hybris.platform.servicelayer.event.events.AbstractEvent;

@SuppressWarnings("serial")
public class EyDmsAddressEvent extends AbstractEvent {
	
	private final EyDmsAddressProcessModel process;
	
	public EyDmsAddressEvent(final EyDmsAddressProcessModel process)
	{
		this.process = process;
	}
	
	public EyDmsAddressProcessModel getProcess()
	{
		return this.process;
	}

}
