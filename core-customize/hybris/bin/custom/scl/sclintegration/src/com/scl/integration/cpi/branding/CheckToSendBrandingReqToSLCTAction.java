package com.scl.integration.cpi.branding;

import com.scl.core.model.BrandingRequestDetailsModel;
import com.scl.core.model.SclBrandingProcessModel;
import de.hybris.platform.processengine.action.AbstractAction;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.task.RetryLaterException;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class CheckToSendBrandingReqToSLCTAction<T extends SclBrandingProcessModel> extends AbstractAction<T> {

    private static final Logger LOG = Logger.getLogger(CheckToSendBrandingReqToSLCTAction.class);

    public Transition executeAction(SclBrandingProcessModel sclBrandingProcessModel) throws RetryLaterException, Exception {
        BrandingRequestDetailsModel brandingRequisitionDetails = sclBrandingProcessModel.getBrandingRequestDetails();
            if(brandingRequisitionDetails!=null && sclBrandingProcessModel.getAction()!=null)
            {
                if(sclBrandingProcessModel.getAction().equals("MODIFY"))
                {
                    return Transition.MODIFY;
                }
                else if (sclBrandingProcessModel.getAction().equals("CANCEL"))
                {
                    return Transition.CANCEL;
                }
                else if (sclBrandingProcessModel.getAction().equals("FEEDBACK"))
                {
                    return Transition.FEEDBACK;
                }
                return Transition.OK;
            }
            else
            {
                return Transition.OK;
            }
    }

    @Override
    public String execute(T t) throws RetryLaterException, Exception {
        return executeAction(t).toString();
    }

    @Override
    public Set<String> getTransitions() {
        return CheckToSendBrandingReqToSLCTAction.Transition.getStringValues();
    }

    public enum Transition
    {
        MODIFY, CANCEL, OK, NOK, FEEDBACK;

        public static Set<String> getStringValues()
        {
            final Set<String> res = new HashSet<String>();
            for (final CheckToSendBrandingReqToSLCTAction.Transition transitions : CheckToSendBrandingReqToSLCTAction.Transition.values())
            {
                res.add(transitions.toString());
            }
            return res;
        }
    }
}
