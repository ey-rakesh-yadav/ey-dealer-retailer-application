package com.scl.core.event;

import com.scl.core.jalo.SclOrderLineCancelProcess;
import com.scl.core.model.SclOrderLineCancelProcessModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.model.ModelService;

import javax.annotation.Resource;

public class SclOrderLineCancelEventListener extends AbstractEventListener<SclOrderLineCancelEvent> {

    @Resource
    private BusinessProcessService businessProcessService;

    @Resource
    private ModelService modelService;

    @Override
    protected void onEvent(SclOrderLineCancelEvent event) {
        final String processDefName = "sclOrderLineCancel-process";

        SclOrderLineCancelProcessModel processInfo = event.getProcess();

        final SclOrderLineCancelProcessModel processModel = (SclOrderLineCancelProcessModel) businessProcessService
                .createProcess(processDefName + "-" +processInfo.getOrder().getCode()+"_"+ System.currentTimeMillis(), processDefName);

        processModel.setOrder(processInfo.getOrder());
        processModel.setEntryNumber(processInfo.getEntryNumber());
        processModel.setCrmEntryNumber(processInfo.getCrmEntryNumber());
        modelService.save(processModel);

        businessProcessService.startProcess(processModel);
    }
}
