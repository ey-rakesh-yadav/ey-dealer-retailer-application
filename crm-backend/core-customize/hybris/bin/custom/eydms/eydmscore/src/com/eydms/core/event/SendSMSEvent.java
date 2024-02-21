package com.eydms.core.event;

import com.eydms.core.model.SMSProcessModel;

import de.hybris.platform.servicelayer.event.events.AbstractEvent;

@SuppressWarnings("serial")
public class SendSMSEvent extends AbstractEvent {
	
	private final SMSProcessModel process;

	public SendSMSEvent(final SMSProcessModel process)
	{
		this.process = process;
	}

	public SMSProcessModel getProcess()
	{
		return this.process;
	}

}
