package com.scl.core.actions.user;


import de.hybris.platform.b2b.process.approval.actions.B2BAbstractWorkflowAutomatedAction;
import de.hybris.platform.workflow.model.WorkflowActionModel;
import org.apache.log4j.Logger;

public class AfterContactNumberRejectWorkflowDecisionAction extends B2BAbstractWorkflowAutomatedAction {
   private static final Logger LOGGER = Logger.getLogger(AfterContactNumberRejectWorkflowDecisionAction.class);
    @Override
    public void performAction(WorkflowActionModel action) {
        LOGGER.info("IN AfterContactNumberRejectWorkflowDecisionAction");
    }
}
