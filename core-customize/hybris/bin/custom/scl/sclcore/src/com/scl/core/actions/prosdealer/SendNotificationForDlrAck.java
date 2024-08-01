package com.scl.core.actions.prosdealer;

import com.scl.core.model.ProsDealerOnboardingProcessModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.task.RetryLaterException;

public class SendNotificationForDlrAck extends AbstractSimpleDecisionAction<ProsDealerOnboardingProcessModel> {


    @Override
    public Transition executeAction(ProsDealerOnboardingProcessModel prosDealerOnboardingProcessModel) throws RetryLaterException, Exception {
        //Write Logic to Send Notification to SO for acknowledging prospective dealer
        return Transition.OK;
    }

}
