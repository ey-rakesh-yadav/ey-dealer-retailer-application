package com.eydms.integration.cpi.EndCustomer;

import com.eydms.core.model.EndCustomerComplaintModel;
import com.eydms.core.model.SendEndCustomerComplaintProcessModel;
import com.eydms.integration.cpi.branding.CheckToSendBrandingReqToSLCTAction;
import de.hybris.platform.processengine.action.AbstractAction;
import de.hybris.platform.task.RetryLaterException;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class CheckToEndCustomerCompToSLCTAction <T extends SendEndCustomerComplaintProcessModel> extends AbstractAction<T> {

    private static final Logger LOG = Logger.getLogger(CheckToSendBrandingReqToSLCTAction.class);

    public CheckToSendBrandingReqToSLCTAction.Transition executeAction(SendEndCustomerComplaintProcessModel sendEndCustomerComplaintProcessModel) throws RetryLaterException, Exception {
        EndCustomerComplaintModel endCustomerComplaint = sendEndCustomerComplaintProcessModel.getEndCustomerComplaint();
        if(endCustomerComplaint!=null && sendEndCustomerComplaintProcessModel.getAction()!=null)
        {
            if(sendEndCustomerComplaintProcessModel.getAction().equals("MODIFY"))
            {
                return CheckToSendBrandingReqToSLCTAction.Transition.MODIFY;
            }
            else if (sendEndCustomerComplaintProcessModel.getAction().equals("CANCEL"))
            {
                return CheckToSendBrandingReqToSLCTAction.Transition.CANCEL;
            }
            else if (sendEndCustomerComplaintProcessModel.getAction().equals("FEEDBACK"))
            {
                return CheckToSendBrandingReqToSLCTAction.Transition.FEEDBACK;
            }
            return CheckToSendBrandingReqToSLCTAction.Transition.OK;
        }
        else
        {
            return CheckToSendBrandingReqToSLCTAction.Transition.OK;
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
