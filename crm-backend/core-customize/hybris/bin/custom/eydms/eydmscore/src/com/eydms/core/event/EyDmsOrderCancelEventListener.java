package com.eydms.core.event;

import com.eydms.core.model.EyDmsAddressProcessModel;
import com.eydms.core.model.EyDmsOrderCancelProcessModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.model.ModelService;

import javax.annotation.Resource;

public class EyDmsOrderCancelEventListener extends AbstractEventListener<EyDmsOrderCancelEvent> {

    @Resource
    private BusinessProcessService businessProcessService;

    @Resource
    private ModelService modelService;

    @Override
    protected void onEvent(EyDmsOrderCancelEvent eydmsOrderCancelEvent) {
        final String processDefName = "eydmsOrderCancel-process";

        EyDmsOrderCancelProcessModel processInfo= eydmsOrderCancelEvent.getProcess();

        final EyDmsOrderCancelProcessModel processModel = (EyDmsOrderCancelProcessModel) businessProcessService
                .createProcess(processDefName + "-" + System.currentTimeMillis(), processDefName);

        processModel.setOrder(processInfo.getOrder());
        modelService.save(processModel);

        businessProcessService.startProcess(processModel);
    }
}
