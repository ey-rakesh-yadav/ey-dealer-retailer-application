package com.scl.core.event;

import com.scl.core.model.SclBrandingProcessModel;
import com.scl.core.model.SclOrderLineCancelProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.model.ModelService;

import javax.annotation.Resource;

public class SclBrandingEventListener extends AbstractEventListener<SclBrandingEvent> {

    @Resource
    private BusinessProcessService businessProcessService;

    @Resource
    private ModelService modelService;

    @Override
    protected void onEvent(SclBrandingEvent event) {
        final String processDefName = "sclBranding-Process";

        SclBrandingProcessModel processInfo = event.getProcess();

        final SclBrandingProcessModel processModel = (SclBrandingProcessModel) businessProcessService
                .createProcess(processDefName + "-" + System.currentTimeMillis(), processDefName);

        processModel.setBrandingRequestDetails(processInfo.getBrandingRequestDetails());
        processModel.setAction(processInfo.getAction());
        modelService.save(processModel);

        businessProcessService.startProcess(processModel);
    }
}
