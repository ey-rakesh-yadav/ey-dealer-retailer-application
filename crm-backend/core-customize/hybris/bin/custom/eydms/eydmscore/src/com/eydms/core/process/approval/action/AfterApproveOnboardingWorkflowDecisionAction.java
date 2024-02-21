package com.eydms.core.process.approval.action;


import com.eydms.core.model.ProsDealerOnboardingProcessModel;
import com.eydms.core.model.ProspectiveDealerModel;
import de.hybris.platform.b2b.process.approval.actions.B2BAbstractWorkflowAutomatedAction;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.workflow.model.WorkflowActionModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

/**
 * Action which handles business logic after an approve decision has been chosen by an approver dealer onboarding
 * workflow
 */
public class AfterApproveOnboardingWorkflowDecisionAction extends B2BAbstractWorkflowAutomatedAction {

    private static final Logger LOG = Logger.getLogger(AfterApproveOnboardingWorkflowDecisionAction.class);

    private EventService eventService;

    @Override
    public void performAction(WorkflowActionModel action) {

        ProspectiveDealerModel prospectiveDealerModel = null;

        try
        {
            final ProsDealerOnboardingProcessModel process = (ProsDealerOnboardingProcessModel) CollectionUtils.find(action.getAttachmentItems(),
                    PredicateUtils.instanceofPredicate(ProsDealerOnboardingProcessModel.class));
            Assert.notNull(process, String.format("Process attachment missing for action %s", action.getCode()));
            prospectiveDealerModel = process.getProsDealer();
            //prospectiveDealerModel.setStatus(OrderStatus.APPROVED);
            prospectiveDealerModel.setStateOfRegistration("APPROVED");
            getModelService().save(prospectiveDealerModel);
        }
        catch (final Exception e)
        {
            LOG.error(e.getMessage(), e);
            if (prospectiveDealerModel != null)
            {
                //prospectiveDealerModel.setStatus(OrderStatus.B2B_PROCESSING_ERROR);
                prospectiveDealerModel.setStateOfRegistration("PROCESSING_ERROR");
                getModelService().save(prospectiveDealerModel);
            }
        }
    }

    public EventService getEventService() {
        return eventService;
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }
}
