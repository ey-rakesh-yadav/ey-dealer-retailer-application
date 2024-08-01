package com.scl.core.strategies.impl;

import de.hybris.platform.b2b.enums.WorkflowTemplateType;
import de.hybris.platform.b2b.services.B2BWorkflowIntegrationService;
import de.hybris.platform.b2b.strategies.impl.AbstractWorkflowTemplateStrategy;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.workflow.enums.WorkflowActionType;
import de.hybris.platform.workflow.model.AutomatedWorkflowActionTemplateModel;
import de.hybris.platform.workflow.model.WorkflowActionTemplateModel;
import de.hybris.platform.workflow.model.WorkflowTemplateModel;
import org.apache.log4j.Logger;

import java.util.List;

public class ProsDealerSOApprovalWorkflowTemplateStrategy extends AbstractWorkflowTemplateStrategy {

    private static final Logger LOG = Logger.getLogger(ProsDealerSOApprovalWorkflowTemplateStrategy.class);

    @Override
    public WorkflowTemplateModel createWorkflowTemplate(List<? extends UserModel> users, String code, String description) {


        return (WorkflowTemplateModel) getSessionService().executeInLocalView(new SessionExecutionBody() {
            @Override
            public Object execute() {
                final UserModel admin = getUserService().getAdminUser();
                getUserService().setCurrentUser(admin);

                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Creating SO Approval WorkflowTemplate for code %s with description %s", code, description));
                }

                final WorkflowTemplateModel workflowTemplateModel = createBlankWorkflowTemplate(code, description, admin);
                final AutomatedWorkflowActionTemplateModel wakeUpProcessEngineAutomatedAction = createAutomatedWorkflowActionTemplate(
                        code, B2BWorkflowIntegrationService.ACTIONCODES.BACK_TO_PROCESSENGINE.name(), WorkflowActionType.NORMAL, admin,
                        workflowTemplateModel, null, "afterSOApprovalWorkflowAction");

                final AutomatedWorkflowActionTemplateModel afterApproveOnboardingWorkflowDecisionAction = createAutomatedWorkflowActionTemplate(
                        code, B2BWorkflowIntegrationService.ACTIONCODES.APPROVED.name() + "_END", WorkflowActionType.NORMAL, admin,
                        workflowTemplateModel, null, "afterSOApproveOnboardingWorkflowDecisionAction");

                final AutomatedWorkflowActionTemplateModel afterRejectOnboardingWorkflowDecisionAction = createAutomatedWorkflowActionTemplate(
                        code, B2BWorkflowIntegrationService.ACTIONCODES.REJECTED.name() + "_END", WorkflowActionType.NORMAL, admin,
                        workflowTemplateModel, null, "afterSORejectOnboardingWorkflowDecisionAction");

                for (final UserModel approver : users) {
                    final AutomatedWorkflowActionTemplateModel approveOnboardingDecisionAutomatedAction = createAutomatedWorkflowActionTemplate(
                            code, B2BWorkflowIntegrationService.ACTIONCODES.APPROVED.name(), WorkflowActionType.NORMAL, approver,
                            workflowTemplateModel, null, "soApproveOnboardingDecisionAutomatedAction");

                    final AutomatedWorkflowActionTemplateModel rejectOnboardingDecisionAutomatedAction = createAutomatedWorkflowActionTemplate(
                            code, B2BWorkflowIntegrationService.ACTIONCODES.REJECTED.name(), WorkflowActionType.NORMAL, approver,
                            workflowTemplateModel, null, "soRejectOnboardingDecisionAutomatedAction");

                    final WorkflowActionTemplateModel action = createWorkflowActionTemplateModel(code,
                            B2BWorkflowIntegrationService.ACTIONCODES.APPROVAL.name(), WorkflowActionType.START, approver,
                            workflowTemplateModel);
                    // the approve decision links
                    createLink(action, approveOnboardingDecisionAutomatedAction, B2BWorkflowIntegrationService.DECISIONCODES.APPROVE.name(),
                            Boolean.TRUE);
                    createLink(approveOnboardingDecisionAutomatedAction, afterApproveOnboardingWorkflowDecisionAction,
                            B2BWorkflowIntegrationService.DECISIONCODES.APPROVE.name() + "_END", Boolean.TRUE);
                    createLink(afterApproveOnboardingWorkflowDecisionAction, wakeUpProcessEngineAutomatedAction,
                            B2BWorkflowIntegrationService.ACTIONCODES.BACK_TO_PROCESSENGINE.name(), Boolean.FALSE);
                    // reject decision links
                    createLink(action, rejectOnboardingDecisionAutomatedAction, B2BWorkflowIntegrationService.DECISIONCODES.REJECT.name(),
                            Boolean.FALSE);
                    createLink(rejectOnboardingDecisionAutomatedAction, afterRejectOnboardingWorkflowDecisionAction,
                            B2BWorkflowIntegrationService.DECISIONCODES.REJECT.name() + "_END", Boolean.FALSE);
                    createLink(afterRejectOnboardingWorkflowDecisionAction, wakeUpProcessEngineAutomatedAction,
                            B2BWorkflowIntegrationService.ACTIONCODES.BACK_TO_PROCESSENGINE.name(), Boolean.FALSE);
                }

                //end finishAction to end workflow
                final WorkflowActionTemplateModel finishAction = createWorkflowActionTemplateModel(code,
                        B2BWorkflowIntegrationService.ACTIONCODES.END.name(), WorkflowActionType.END, admin, workflowTemplateModel);
                createLink(wakeUpProcessEngineAutomatedAction, finishAction, "WORKFLOW_FINISHED", Boolean.FALSE);

                return workflowTemplateModel;

            }
        });


    }

    @Override
    public String getWorkflowTemplateType() {
        return WorkflowTemplateType.PROSPECTIVE_DEALER_APPROVAL_SO.getCode();
    }
}
