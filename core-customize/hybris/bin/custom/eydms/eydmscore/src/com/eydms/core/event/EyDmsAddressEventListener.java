package com.eydms.core.event;

import javax.annotation.Resource;

import com.eydms.core.model.SMSProcessModel;
import com.eydms.core.model.EyDmsAddressProcessModel;

import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.model.ModelService;

public class EyDmsAddressEventListener extends AbstractEventListener<EyDmsAddressEvent> {

	@Resource
	private BusinessProcessService businessProcessService;

	@Resource
	private ModelService modelService;
	
	@Override
	protected void onEvent(EyDmsAddressEvent event) {
		
		final String processDefName = "eydmsAddress-process";
		
		final EyDmsAddressProcessModel processInfo = event.getProcess();
		
		final EyDmsAddressProcessModel processModel = (EyDmsAddressProcessModel) businessProcessService
				.createProcess(processDefName + "-" + System.currentTimeMillis(), processDefName);
		
		processModel.setAddress(processInfo.getAddress());
		processModel.setCustomer(processInfo.getCustomer());
		processModel.setBaseSite(processInfo.getBaseSite());
		processModel.setEyDmsUser(processInfo.getEyDmsUser());
		modelService.save(processModel);

		businessProcessService.startProcess(processModel);
	}

}
