package com.scl.core.process.approval.action.saleshead;

import com.scl.core.enums.OnboardingStatus;
import com.scl.core.model.ProsDealerOnboardingProcessModel;
import com.scl.core.model.ProspectiveDealerModel;
import de.hybris.platform.b2b.process.approval.actions.B2BAbstractWorkflowAutomatedAction;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.workflow.model.WorkflowActionModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

public class AfterSHRejectOnboardingWorkflowDecisionAction extends B2BAbstractWorkflowAutomatedAction {

    private static final Logger LOG = Logger.getLogger(AfterSHRejectOnboardingWorkflowDecisionAction.class);
    private EventService eventService;

    @Override
    public void performAction(final WorkflowActionModel action)
    {
        ProspectiveDealerModel prospectiveDealerModel = null;
        try
        {
            final ProsDealerOnboardingProcessModel process = (ProsDealerOnboardingProcessModel) CollectionUtils.find(action.getAttachmentItems(),
                    PredicateUtils.instanceofPredicate(ProsDealerOnboardingProcessModel.class));
            Assert.notNull(process, String.format("Process attachment missing for action %s", action.getCode()));
            prospectiveDealerModel = process.getProsDealer();
            //prospectiveDealerModel.setStatus(OrderStatus.REJECTED);
            //prospectiveDealerModel.setStateOfRegistration("REJECTED");
            prospectiveDealerModel.setOnboardingStatus(OnboardingStatus.REJECTED_BY_SH);
            getModelService().save(prospectiveDealerModel);
        }
        catch (final Exception e)
        {
            LOG.error(e.getMessage(), e);
            if (prospectiveDealerModel != null)
            {
                //prospectiveDealerModel.setStatus(OrderStatus.B2B_PROCESSING_ERROR);
               // prospectiveDealerModel.setStateOfRegistration("PROCESSING_ERROR");
                prospectiveDealerModel.setOnboardingStatus(OnboardingStatus.PROCESSING_ERROR);
                getModelService().save(prospectiveDealerModel);
            }
        }
    }

    protected EventService getEventService()
    {
        return eventService;
    }

    @Required
    public void setEventService(final EventService eventService)
    {
        this.eventService = eventService;
    }
}
