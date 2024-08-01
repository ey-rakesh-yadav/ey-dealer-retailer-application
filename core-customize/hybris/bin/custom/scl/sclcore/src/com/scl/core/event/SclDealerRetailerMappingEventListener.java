package com.scl.core.event;

import com.scl.core.model.DealerRetailerMappingModel;
import com.scl.core.model.SclDealerRetailerMappingProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.model.ModelService;


public class SclDealerRetailerMappingEventListener extends AbstractEventListener<SclDealerRetailerMappingEvent> {

    private BusinessProcessService businessProcessService;

    private ModelService modelService;


    @Override
    protected void onEvent(SclDealerRetailerMappingEvent event) {
        final String processDefName = "dealerRetailerMapping-process";
        DealerRetailerMappingModel dealerRetailer = event.getDealerRetailer();
        SclDealerRetailerMappingProcessModel processModel= (SclDealerRetailerMappingProcessModel) getBusinessProcessService().createProcess(processDefName + "-" + System.currentTimeMillis(), processDefName);
        processModel.setDealerRetailer(dealerRetailer);
        getModelService().save(processModel);
        getBusinessProcessService().startProcess(processModel);
    }

    public BusinessProcessService getBusinessProcessService() {
        return businessProcessService;
    }

    public void setBusinessProcessService(BusinessProcessService businessProcessService) {
        this.businessProcessService = businessProcessService;
    }

    public ModelService getModelService() {
        return modelService;
    }

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }
}
