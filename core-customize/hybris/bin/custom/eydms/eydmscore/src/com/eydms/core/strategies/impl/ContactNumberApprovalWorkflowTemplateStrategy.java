package com.eydms.core.strategies.impl;

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

public class ContactNumberApprovalWorkflowTemplateStrategy extends AbstractWorkflowTemplateStrategy {


    private static final Logger LOG = Logger.getLogger(ContactNumberApprovalWorkflowTemplateStrategy.class);

    @Override
    public WorkflowTemplateModel createWorkflowTemplate(List<? extends UserModel> users, String code, String description) {

        return (WorkflowTemplateModel) getSessionService().executeInLocalView(new SessionExecutionBody()
        {
            @Override
            public Object execute()
            {
                final UserModel admin = getUserService().getAdminUser();
                getUserService().setCurrentUser(admin);

                if (LOG.isDebugEnabled())
                {
                    LOG.debug(String.format("Createing WorkflowTemplate for code %s with description %s", code, description));
                }
                final WorkflowTemplateModel workflowTemplateModel = createBlankWorkflowTemplate(code, description, admin);

                final AutomatedWorkflowActionTemplateModel autoActionTemplate = createAutomatedWorkflowActionTemplate(code,
                        B2BWorkflowIntegrationService.ACTIONCODES.BACK_TO_PROCESSENGINE.name(), WorkflowActionType.NORMAL, admin,
                        workflowTemplateModel, null, "afterContactNumberApprovalWorkflowAction");


                for (final UserModel approver : users)
                {

                    final AutomatedWorkflowActionTemplateModel approveDecisionTemplate = createAutomatedWorkflowActionTemplate(code,
                            B2BWorkflowIntegrationService.ACTIONCODES.APPROVED.name(), WorkflowActionType.NORMAL, approver,
                            workflowTemplateModel,null, "afterContactNumberApprovalWorkflowDecisionAction");
                    final AutomatedWorkflowActionTemplateModel rejectDecisionTemplate = createAutomatedWorkflowActionTemplate(code,
                            B2BWorkflowIntegrationService.ACTIONCODES.REJECTED.name(), WorkflowActionType.NORMAL, approver,
                            workflowTemplateModel, null, "afterContactNumberRejectWorkflowDecisionAction");

                    final WorkflowActionTemplateModel action = createWorkflowActionTemplateModel(code,
                            B2BWorkflowIntegrationService.ACTIONCODES.APPROVAL.name(), WorkflowActionType.START, approver,
                            workflowTemplateModel);

                    createLink(action, approveDecisionTemplate, B2BWorkflowIntegrationService.DECISIONCODES.APPROVE.name(),
                            Boolean.FALSE);

                    createLink(approveDecisionTemplate, autoActionTemplate,
                            B2BWorkflowIntegrationService.ACTIONCODES.BACK_TO_PROCESSENGINE.name(), Boolean.TRUE);


                    createLink(action, rejectDecisionTemplate, B2BWorkflowIntegrationService.DECISIONCODES.REJECT.name(),
                            Boolean.FALSE);
                    createLink(rejectDecisionTemplate, autoActionTemplate,
                            B2BWorkflowIntegrationService.ACTIONCODES.BACK_TO_PROCESSENGINE.name(), Boolean.FALSE);
                }
                //end finishAction to end workflow
                final WorkflowActionTemplateModel finishAction = createWorkflowActionTemplateModel(code,
                        B2BWorkflowIntegrationService.ACTIONCODES.END.name(), WorkflowActionType.END, admin, workflowTemplateModel);
                createLink(autoActionTemplate, finishAction, "WORKFLOW_FINISHED", Boolean.FALSE);
                return workflowTemplateModel;
            }
        });
    }

    //TODO::// Chnage template type
    @Override
    public String getWorkflowTemplateType() {
        return WorkflowTemplateType.CONTACT_NUMBER_APPROVAL.getCode();
    }
}
