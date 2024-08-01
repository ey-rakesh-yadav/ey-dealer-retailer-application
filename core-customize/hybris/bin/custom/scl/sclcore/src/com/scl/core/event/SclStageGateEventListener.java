package com.scl.core.event;

import com.scl.core.model.DeliveryItemModel;
import com.scl.core.model.SclBrandingProcessModel;
import com.scl.core.model.SclOutboundStageGateProcessModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.model.ModelService;

import javax.annotation.Resource;

public class SclStageGateEventListener extends AbstractEventListener<SclStageGateEvent> {

    @Resource
    private BusinessProcessService businessProcessService;

    @Resource
    private ModelService modelService;

    @Override
    protected void onEvent(SclStageGateEvent event) {

        SclOutboundStageGateProcessModel processInfo = event.getProcess();
        DeliveryItemModel deliveryItemModel=processInfo.getDeliveryItem();
        OrderEntryModel orderEntry=processInfo.getOrderEntry();

        final SclOutboundStageGateProcessModel stageGateProcessModel = (SclOutboundStageGateProcessModel) businessProcessService.createProcess(
                "sclOutboundStageGate-process-" + deliveryItemModel.getDiNumber() + "-" + System.currentTimeMillis(),
                "sclOutboundStageGate-process");
        stageGateProcessModel.setDeliveryItem(deliveryItemModel);
        stageGateProcessModel.setOrderEntry(orderEntry);
        modelService.save(stageGateProcessModel);
        businessProcessService.startProcess(stageGateProcessModel);

    }
}
