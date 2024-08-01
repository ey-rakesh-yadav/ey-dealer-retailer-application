package com.scl.core.event;

import com.scl.core.model.ProsDealerOnboardingProcessModel;
import com.scl.core.model.ProspectiveDealerModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.model.ModelService;

public class ProsDealerOnboardingEventListener extends AbstractEventListener<ProsDealerOnboardingEvent> {

    private ModelService modelService;
    private BusinessProcessService businessProcessService;

    @Override
    protected void onEvent(ProsDealerOnboardingEvent event) {
        ProspectiveDealerModel prospectiveDealer = event.getProspectiveDealer();

        final ProsDealerOnboardingProcessModel prosDealerApprovalProcess= (ProsDealerOnboardingProcessModel) getBusinessProcessService()
                .createProcess("prosDealerApproval-" + prospectiveDealer.getDealerCode() + "-" + System.currentTimeMillis(),
                        "prosDealerApproval");
        prosDealerApprovalProcess.setProsDealer(prospectiveDealer);
        getModelService().save(prosDealerApprovalProcess);
        getBusinessProcessService().startProcess(prosDealerApprovalProcess);

    }

    public ModelService getModelService() {
        return modelService;
    }

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    public BusinessProcessService getBusinessProcessService() {
        return businessProcessService;
    }

    public void setBusinessProcessService(BusinessProcessService businessProcessService) {
        this.businessProcessService = businessProcessService;
    }
}
