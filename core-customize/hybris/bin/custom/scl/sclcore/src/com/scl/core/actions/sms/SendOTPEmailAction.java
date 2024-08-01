package com.scl.core.actions.sms;

import com.scl.core.model.OTPEmailProcessModel;
import de.hybris.platform.acceleratorservices.email.EmailService;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.servicelayer.model.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendOTPEmailAction extends AbstractSimpleDecisionAction<OTPEmailProcessModel> {

    private static final Logger LOG = LoggerFactory.getLogger(SendOTPEmailAction.class);

    /*private EmailService emailService;
    private ModelService modelService;
    public final void setEmailService(final EmailService emailService) {
        this.emailService = emailService;
    }
    public final void setModelService(final ModelService modelService) {
        this.modelService = modelService;
    }*/

    @Override
    public Transition executeAction(final OTPEmailProcessModel process) {
        // Your logic to send an email goes here
        // For example, retrieve the email template, customer data, and send the email

        // If there is an error
        // return Transition.NOK;
        if (process.getOtpCode() == null)
        {
            LOG.error("The language cannot be empty");
            return Transition.NOK;
        }
        // If email is sent successfully
        return Transition.OK;
    }
}
