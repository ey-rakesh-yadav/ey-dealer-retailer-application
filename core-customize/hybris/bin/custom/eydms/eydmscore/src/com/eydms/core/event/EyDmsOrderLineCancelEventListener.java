package com.eydms.core.event;

import com.eydms.core.jalo.EyDmsOrderLineCancelProcess;
import com.eydms.core.model.EyDmsOrderLineCancelProcessModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.model.ModelService;

import javax.annotation.Resource;

public class EyDmsOrderLineCancelEventListener extends AbstractEventListener<EyDmsOrderLineCancelEvent> {

    @Resource
    private BusinessProcessService businessProcessService;

    @Resource
    private ModelService modelService;

    @Override
    protected void onEvent(EyDmsOrderLineCancelEvent event) {
        final String processDefName = "eydmsOrderLineCancel-process";

        EyDmsOrderLineCancelProcessModel processInfo = event.getProcess();

        final EyDmsOrderLineCancelProcessModel processModel = (EyDmsOrderLineCancelProcessModel) businessProcessService
                .createProcess(processDefName + "-" + System.currentTimeMillis(), processDefName);

        processModel.setOrder(processInfo.getOrder());
        processModel.setEntryNumber(processInfo.getEntryNumber());
        processModel.setCrmEntryNumber(processInfo.getCrmEntryNumber());
        modelService.save(processModel);

        businessProcessService.startProcess(processModel);
    }
}
