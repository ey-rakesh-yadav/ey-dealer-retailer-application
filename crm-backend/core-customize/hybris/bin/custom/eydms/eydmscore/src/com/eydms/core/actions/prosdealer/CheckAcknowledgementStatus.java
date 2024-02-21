package com.eydms.core.actions.prosdealer;

import com.eydms.core.enums.OnboardingStatus;
import com.eydms.core.model.ProsDealerOnboardingProcessModel;
import com.eydms.core.model.ProspectiveDealerModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.task.RetryLaterException;

public class CheckAcknowledgementStatus extends AbstractSimpleDecisionAction<ProsDealerOnboardingProcessModel> {


    @Override
    public Transition executeAction(ProsDealerOnboardingProcessModel prosDealerOnboardingProcessModel) throws RetryLaterException, Exception {

        ProspectiveDealerModel prospectiveDealer = prosDealerOnboardingProcessModel.getProsDealer();

        if (OnboardingStatus.DEALER_ACKNOWLEDGED.equals(prospectiveDealer.getOnboardingStatus())) {
            return Transition.OK;
        } else {
            return Transition.NOK;
        }

    }
}
