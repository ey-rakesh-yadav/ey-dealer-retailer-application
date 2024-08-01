package com.scl.core.actions.prosdealer;

import com.scl.core.model.ProsDealerOnboardingProcessModel;
import com.scl.core.model.ProspectiveDealerModel;
import com.scl.core.model.SclUserModel;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.task.RetryLaterException;
import org.apache.log4j.Logger;

import java.util.*;

public class FindProsDealerApproversAction extends AbstractSimpleDecisionAction<ProsDealerOnboardingProcessModel> {

    private ModelService modelService;

    private FlexibleSearchService flexibleSearchService;

    private static final Logger LOG = Logger.getLogger(FindProsDealerApproversAction.class);
    @Override
    public Transition executeAction(ProsDealerOnboardingProcessModel businessProcessModel) throws RetryLaterException, Exception {

        /*Optional<ProspectiveDealerModel> prospectiveDealer = Optional.of(businessProcessModel)
                .filter(businessProcess -> businessProcess instanceof ProsDealerOnboardingProcessModel)
                .map(businessProcess -> ((ProsDealerOnboardingProcessModel) businessProcess).getProsDealer());
*/
        ProspectiveDealerModel prospectiveDealer  = businessProcessModel.getProsDealer();
        if (null!=prospectiveDealer){
            //TODO:Refactor code to remove static assignment
            SclUserModel sclUserModel = new SclUserModel();
            sclUserModel.setUid("scluser1@mailinator.com");
            sclUserModel = getFlexibleSearchService().getModelByExample(sclUserModel);
            Set<SclUserModel> sclUsers = new HashSet<>();
            sclUsers.add(sclUserModel);
            prospectiveDealer.setReporter(sclUsers);

            Set<B2BCustomerModel> approvers = new HashSet<>();
            approvers.add(sclUserModel);
            prospectiveDealer.setApprovers(approvers);

            getModelService().save(prospectiveDealer);
            return Transition.OK;
        }
        else{
            LOG.error("No attached prospective dealer found!");
            return Transition.NOK;
        }
    }

    @Override
    public ModelService getModelService() {
        return modelService;
    }

    @Override
    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    public FlexibleSearchService getFlexibleSearchService() {
        return flexibleSearchService;
    }

    public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }
}
