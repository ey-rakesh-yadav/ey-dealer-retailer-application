package com.scl.core.event;

import com.scl.core.model.OTPEmailProcessModel;
import de.hybris.platform.acceleratorservices.site.AbstractAcceleratorSiteEventListener;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.enums.SiteChannel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.util.ServicesUtil;

public class SendOTPEmailEventListener extends AbstractAcceleratorSiteEventListener<SendOTPEmailEvent> {


    private BusinessProcessService businessProcessService;
    private ModelService modelService;

    @Override
    protected void onSiteEvent(SendOTPEmailEvent event) {
        final String otp = event.getProcess().getOtpCode();

        final OTPEmailProcessModel emailProcess=event.getProcess();

       /* final OTPEmailProcessModel emailProcess = getBusinessProcessService().createProcess(
                "sendOTPEmailProcess-" + event.getCustomer().getUid() + "-" + System.currentTimeMillis(),
                "sendOTPEmailProcess");*/
        emailProcess.setSite(event.getSite());
        emailProcess.setCustomer(event.getCustomer());
        emailProcess.setLanguage(event.getLanguage());
        emailProcess.setCurrency(event.getCurrency());
        emailProcess.setStore(event.getBaseStore());
        emailProcess.setOtpCode(otp);
        getModelService().save(emailProcess);
        getBusinessProcessService().startProcess(emailProcess);
    }

    @Override
    protected SiteChannel getSiteChannelForEvent(SendOTPEmailEvent event) {
        final BaseSiteModel site = event.getSite();
        ServicesUtil.validateParameterNotNullStandardMessage("event.site", site);
        return site.getChannel();
    }

    protected BusinessProcessService getBusinessProcessService()
    {
        return businessProcessService;
    }

    public void setBusinessProcessService(final BusinessProcessService businessProcessService)
    {
        this.businessProcessService = businessProcessService;
    }

    protected ModelService getModelService()
    {
        return modelService;
    }

    public void setModelService(final ModelService modelService)
    {
        this.modelService = modelService;
    }
}