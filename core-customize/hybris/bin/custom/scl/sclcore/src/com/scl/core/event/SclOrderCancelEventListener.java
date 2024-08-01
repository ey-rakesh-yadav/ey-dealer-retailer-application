package com.scl.core.event;

import com.scl.core.model.SclAddressProcessModel;
import com.scl.core.model.SclOrderCancelProcessModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.model.ModelService;

import javax.annotation.Resource;

public class SclOrderCancelEventListener extends AbstractEventListener<SclOrderCancelEvent> {

    @Resource
    private BusinessProcessService businessProcessService;

    @Resource
    private ModelService modelService;

    @Override
    protected void onEvent(SclOrderCancelEvent sclOrderCancelEvent) {
        final String processDefName = "sclOrderCancel-process";

        SclOrderCancelProcessModel processInfo= sclOrderCancelEvent.getProcess();

        final SclOrderCancelProcessModel processModel = (SclOrderCancelProcessModel) businessProcessService
                .createProcess(processDefName + "-" + System.currentTimeMillis(), processDefName);

        processModel.setOrder(processInfo.getOrder());
        modelService.save(processModel);

        businessProcessService.startProcess(processModel);
    }
}
