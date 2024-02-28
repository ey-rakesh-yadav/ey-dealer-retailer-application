package com.eydms.core.process.approval.action.saleshead;

import com.eydms.core.enums.OnboardingStatus;
import com.eydms.core.model.ProsDealerOnboardingProcessModel;
import com.eydms.core.model.ProspectiveDealerModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.task.RetryLaterException;

public class CheckSHApprovalWorkflowResult extends AbstractSimpleDecisionAction {


    @Override
    public Transition executeAction(BusinessProcessModel businessProcessModel) throws RetryLaterException, Exception {
        ProspectiveDealerModel prospectiveDealerModel = null;
        try
        {
            ProsDealerOnboardingProcessModel prosDealerOnboardingProcessModel = (ProsDealerOnboardingProcessModel)businessProcessModel;
            prospectiveDealerModel = prosDealerOnboardingProcessModel.getProsDealer();
            if (OnboardingStatus.APPROVED_BY_SH.equals(prospectiveDealerModel.getOnboardingStatus()))
            {
                // create order history and exit process.
                return Transition.OK;

            }
            else
            {
                //prospectiveDealerModel.setOnboardingStatus(OnboardingStatus.RE);
                //this.modelService.save(prospectiveDealerModel);
                return Transition.NOK;
            }
        }
        catch (final Exception e)
        {
           // this.handleError(order, e);
            return Transition.NOK;
        }
    }
}
