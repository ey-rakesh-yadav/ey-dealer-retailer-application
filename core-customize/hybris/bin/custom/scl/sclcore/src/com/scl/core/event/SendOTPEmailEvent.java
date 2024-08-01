package com.scl.core.event;

import com.scl.core.model.OTPEmailProcessModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.event.AbstractCommerceUserEvent;

public class SendOTPEmailEvent extends AbstractCommerceUserEvent<BaseSiteModel> {

    private OTPEmailProcessModel process;

    public SendOTPEmailEvent() {
        super();
    }

    public SendOTPEmailEvent(final OTPEmailProcessModel process)
    {
        super();
        this.process = process;
    }

    public OTPEmailProcessModel getProcess()
    {
        return this.process;
    }

}