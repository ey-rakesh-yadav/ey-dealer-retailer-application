package com.scl.core.event;

import javax.annotation.Resource;

import com.scl.core.model.SMSProcessModel;
import com.scl.core.model.SclAddressProcessModel;

import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.model.ModelService;

public class SclAddressEventListener extends AbstractEventListener<SclAddressEvent> {

	@Resource
	private BusinessProcessService businessProcessService;

	@Resource
	private ModelService modelService;
	
	@Override
	protected void onEvent(SclAddressEvent event) {
		
		final String processDefName = "sclAddress-process";
		
		final SclAddressProcessModel processInfo = event.getProcess();
		
		final SclAddressProcessModel processModel = (SclAddressProcessModel) businessProcessService
				.createProcess(processDefName + "-" + System.currentTimeMillis(), processDefName);
		
		processModel.setAddress(processInfo.getAddress());
		processModel.setCustomer(processInfo.getCustomer());
		processModel.setBaseSite(processInfo.getBaseSite());
		processModel.setSclUser(processInfo.getSclUser());
		modelService.save(processModel);

		businessProcessService.startProcess(processModel);
	}

}
