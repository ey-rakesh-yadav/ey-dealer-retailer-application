package com.scl.core.event;

import javax.annotation.Resource;

import com.scl.core.model.SMSProcessModel;

import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.model.ModelService;

public class SendSMSEventListener extends AbstractEventListener<SendSMSEvent> {

	
	@Resource
	private BusinessProcessService businessProcessService;

	@Resource
	private ModelService modelService;
	

		
	@Override
	protected void onEvent(SendSMSEvent event) {
		
		final String processDefName = "sms-process";
		
		final SMSProcessModel smsProcessInfo = event.getProcess();
		
		/*final SMSProcessModel smsProcessModel = (SMSProcessModel) businessProcessService
				.createProcess(processDefName + "-" + System.currentTimeMillis(), processDefName);
		smsProcessModel.setNumber(smsProcessInfo.getNumber());
		smsProcessModel.setOtp(smsProcessInfo.getOtp());
		smsProcessModel.setUser(smsProcessInfo.getUser());
		*/
		//smsProcessModel.setNumber(smsProcessInfo.getNumber());
		//smsProcessModel.setMessageContent(smsProcessInfo.getMessageContent());
		//smsProcessModel.setTemplateId(smsProcessInfo.getTemplateId());

		//modelService.save(smsProcessModel);

		businessProcessService.startProcess(smsProcessInfo);
		
	}

}
