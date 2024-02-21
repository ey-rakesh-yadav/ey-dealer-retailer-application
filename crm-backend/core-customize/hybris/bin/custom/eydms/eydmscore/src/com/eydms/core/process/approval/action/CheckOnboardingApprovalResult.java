package com.eydms.core.process.approval.action;

import com.eydms.core.model.ProsDealerOnboardingProcessModel;
import com.eydms.core.model.ProspectiveDealerModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.task.RetryLaterException;

public class CheckOnboardingApprovalResult extends AbstractSimpleDecisionAction {


    @Override
    public Transition executeAction(BusinessProcessModel businessProcessModel) throws RetryLaterException, Exception {
        ProspectiveDealerModel prospectiveDealerModel = null;
        try
        {
            ProsDealerOnboardingProcessModel prosDealerOnboardingProcessModel = (ProsDealerOnboardingProcessModel)businessProcessModel;
            prospectiveDealerModel = prosDealerOnboardingProcessModel.getProsDealer();
            if (prospectiveDealerModel.getStateOfRegistration().equals("REJECTED"))
            {
                // create order history and exit process.
                return Transition.NOK;

            }
            else
            {
                // if order was approved delegate to PerformMerchantCheck action
               // order.setStatus(OrderStatus.PENDING_APPROVAL_FROM_MERCHANT);
                prospectiveDealerModel.setStateOfRegistration("SUCCESS");
                this.modelService.save(prospectiveDealerModel);
                return Transition.OK;
            }
        }
        catch (final Exception e)
        {
           // this.handleError(order, e);
            return Transition.NOK;
        }
    }
}
