package com.scl.core.process.approval.action;

import com.scl.core.model.ProsDealerOnboardingProcessModel;
import com.scl.core.model.ProspectiveDealerModel;
import de.hybris.platform.b2b.process.approval.actions.B2BAbstractWorkflowAutomatedAction;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.workflow.model.WorkflowActionModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

public class RejectOnboardingDecisionAction  extends B2BAbstractWorkflowAutomatedAction {

    private static final Logger LOG = Logger.getLogger(RejectOnboardingDecisionAction.class);
    private EventService eventService;

    @Override
    public void performAction(final WorkflowActionModel action)
    {
        ProspectiveDealerModel prospectiveDealerModel = null;
        try
        {
            final PrincipalModel principalAssigned = action.getPrincipalAssigned();
            final ProsDealerOnboardingProcessModel process = (ProsDealerOnboardingProcessModel) CollectionUtils.find(action.getAttachmentItems(),
                    PredicateUtils.instanceofPredicate(ProsDealerOnboardingProcessModel.class));
            Assert.notNull(process, String.format("Process attachment missing for action %s", action.getCode()));
            prospectiveDealerModel = process.getProsDealer();
            if (LOG.isDebugEnabled())
            {
                LOG.debug(String.format("Executing action %s for process %s on order %s assigned to %s", action.getCode(),
                        process.getCode(), prospectiveDealerModel.getDealerCode(), principalAssigned.getUid()));
            }
            /*updatePermissionResultsStatus(order, principalAssigned, PermissionStatus.REJECTED);

            getEventService().publishEvent(new OrderRejectedEvent(order, principalAssigned));*/
        }
        catch (final Exception e)
        {
            LOG.error(e.getMessage(), e);
            if (prospectiveDealerModel != null) // NOSONAR
            {
                //order.setStatus(OrderStatus.B2B_PROCESSING_ERROR);
                prospectiveDealerModel.setStateOfRegistration("ERROR");
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
