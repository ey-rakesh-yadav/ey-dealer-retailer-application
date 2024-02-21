package com.eydms.core.process.approval.action;

import com.eydms.core.enums.OnboardingStatus;
import com.eydms.core.model.ProsDealerApprovalResultModel;
import com.eydms.core.model.ProsDealerOnboardingProcessModel;
import com.eydms.core.model.ProspectiveDealerModel;
import de.hybris.platform.b2b.enums.PermissionStatus;
import de.hybris.platform.b2b.event.OrderApprovedEvent;
import de.hybris.platform.b2b.model.B2BPermissionResultModel;
import de.hybris.platform.b2b.process.approval.actions.ApproveDecisionAutomatedAction;
import de.hybris.platform.b2b.process.approval.actions.B2BAbstractWorkflowAutomatedAction;
import de.hybris.platform.b2b.process.approval.model.B2BApprovalProcessModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.workflow.model.WorkflowActionModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;

public class ApproveOnboardingDecisionAutomatedAction extends B2BAbstractWorkflowAutomatedAction {

    private static final Logger LOG = Logger.getLogger(ApproveOnboardingDecisionAutomatedAction.class);
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
            //updateOnboardingPermissionResultsStatus(prospectiveDealerModel, principalAssigned, PermissionStatus.APPROVED);
            //getEventService().publishEvent(new OrderApprovedEvent(order, principalAssigned));
        }
        catch (final Exception e)
        {
            LOG.error(e.getMessage(), e);
            if (prospectiveDealerModel != null) // NOSONAR
            {
                //prospectiveDealerModel.setStatus(OrderStatus.B2B_PROCESSING_ERROR);
                prospectiveDealerModel.setStateOfRegistration("PROC_ERROR");
                getModelService().save(prospectiveDealerModel);
            }
        }
    }

    private void updateOnboardingPermissionResultsStatus(ProspectiveDealerModel prospectiveDealerModel, PrincipalModel principalAssigned, PermissionStatus status) {

        final Collection<ProsDealerApprovalResultModel> permissionResults = prospectiveDealerModel.getPermissionResults() != null ? prospectiveDealerModel
                .getPermissionResults() : Collections.<ProsDealerApprovalResultModel> emptyList();
        for (final ProsDealerApprovalResultModel prosDealerApprovalResultModel : permissionResults)
        {
            if (principalAssigned.equals(prosDealerApprovalResultModel.getApprover()))
            {
                prosDealerApprovalResultModel.setStatus(status);
                if (LOG.isDebugEnabled())
                {
                    LOG.debug(String.format("%s|%s|%s ", prosDealerApprovalResultModel.getPermissionTypeCode(),
                            prosDealerApprovalResultModel.getStatus(), prosDealerApprovalResultModel.getApprover().getUid()));
                }
            }
        }
        getModelService().saveAll(permissionResults);

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
