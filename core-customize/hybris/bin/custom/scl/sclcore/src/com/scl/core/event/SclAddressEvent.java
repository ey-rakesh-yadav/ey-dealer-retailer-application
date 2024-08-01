package com.scl.core.event;

import com.scl.core.model.SclAddressProcessModel;

import de.hybris.platform.servicelayer.event.events.AbstractEvent;

@SuppressWarnings("serial")
public class SclAddressEvent extends AbstractEvent {
	
	private final SclAddressProcessModel process;
	
	public SclAddressEvent(final SclAddressProcessModel process)
	{
		this.process = process;
	}
	
	public SclAddressProcessModel getProcess()
	{
		return this.process;
	}

}
