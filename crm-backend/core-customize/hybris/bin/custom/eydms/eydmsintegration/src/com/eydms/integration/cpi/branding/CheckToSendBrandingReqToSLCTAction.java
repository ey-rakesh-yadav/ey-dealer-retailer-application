package com.eydms.integration.cpi.branding;

import com.eydms.core.model.BrandingRequestDetailsModel;
import com.eydms.core.model.EyDmsBrandingProcessModel;
import de.hybris.platform.processengine.action.AbstractAction;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.task.RetryLaterException;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class CheckToSendBrandingReqToSLCTAction<T extends EyDmsBrandingProcessModel> extends AbstractAction<T> {

    private static final Logger LOG = Logger.getLogger(CheckToSendBrandingReqToSLCTAction.class);

    public Transition executeAction(EyDmsBrandingProcessModel eydmsBrandingProcessModel) throws RetryLaterException, Exception {
        BrandingRequestDetailsModel brandingRequisitionDetails = eydmsBrandingProcessModel.getBrandingRequestDetails();
            if(brandingRequisitionDetails!=null && eydmsBrandingProcessModel.getAction()!=null)
            {
                if(eydmsBrandingProcessModel.getAction().equals("MODIFY"))
                {
                    return Transition.MODIFY;
                }
                else if (eydmsBrandingProcessModel.getAction().equals("CANCEL"))
                {
                    return Transition.CANCEL;
                }
                else if (eydmsBrandingProcessModel.getAction().equals("FEEDBACK"))
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
