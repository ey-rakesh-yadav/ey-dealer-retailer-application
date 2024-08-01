package com.scl.core.actions.user;

import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.workflow.jobs.AutomatedWorkflowTemplateJob;
import de.hybris.platform.workflow.model.WorkflowActionModel;
import de.hybris.platform.workflow.model.WorkflowDecisionModel;

import java.util.Iterator;

public class AfterContactNumberApprovalWorkflowAction implements AutomatedWorkflowTemplateJob  {

    private ModelService modelService;

    @Override
    public WorkflowDecisionModel perform(WorkflowActionModel action) {

        Iterator<WorkflowDecisionModel> var6 = action.getDecisions().iterator();
        if (var6.hasNext()) {
            return (WorkflowDecisionModel)var6.next();
        } else {
            return null;
        }
    }
    public ModelService getModelService() {
        return modelService;
    }

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }
}
