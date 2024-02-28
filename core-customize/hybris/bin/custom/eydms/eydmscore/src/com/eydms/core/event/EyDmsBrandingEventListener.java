package com.eydms.core.event;

import com.eydms.core.model.EyDmsBrandingProcessModel;
import com.eydms.core.model.EyDmsOrderLineCancelProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.model.ModelService;

import javax.annotation.Resource;

public class EyDmsBrandingEventListener extends AbstractEventListener<EyDmsBrandingEvent> {

    @Resource
    private BusinessProcessService businessProcessService;

    @Resource
    private ModelService modelService;

    @Override
    protected void onEvent(EyDmsBrandingEvent event) {
        final String processDefName = "eydmsBranding-Process";

        EyDmsBrandingProcessModel processInfo = event.getProcess();

        final EyDmsBrandingProcessModel processModel = (EyDmsBrandingProcessModel) businessProcessService
                .createProcess(processDefName + "-" + System.currentTimeMillis(), processDefName);

        processModel.setBrandingRequestDetails(processInfo.getBrandingRequestDetails());
        processModel.setAction(processInfo.getAction());
        modelService.save(processModel);

        businessProcessService.startProcess(processModel);
    }
}
