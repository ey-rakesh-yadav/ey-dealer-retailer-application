package com.eydms.integration.cpi.branding;

import com.eydms.core.model.*;
import com.eydms.integration.cpi.order.EyDmsSapCpiOutboundService;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.task.RetryLaterException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import static de.hybris.platform.sap.sapcpiadapter.service.SapCpiOutboundService.*;

public class SendBrandingReqCancelToSLCTAction extends AbstractSimpleDecisionAction<EyDmsBrandingProcessModel> {

    private static final Logger LOG = Logger.getLogger(SendBrandingReqCancelToSLCTAction.class);

    private EyDmsSapCpiOutboundService eydmsSapCpiDefaultOutboundService;

    @Autowired
    BusinessProcessService businessProcessService;

    @Override
    public Transition executeAction(EyDmsBrandingProcessModel eydmsBrandingProcessModel) throws RetryLaterException, Exception {
        BrandingRequestDetailsModel brandingRequisitionDetails = eydmsBrandingProcessModel.getBrandingRequestDetails();
        EyDmsOutboundBrandingReqCancelModel brandingReqCancelModel = new EyDmsOutboundBrandingReqCancelModel();
        if(brandingRequisitionDetails!=null)
        {
            brandingReqCancelModel.setRequisitionNumber(brandingRequisitionDetails.getRequisitionNumber());
            brandingReqCancelModel.setRequestCancelledBy(brandingRequisitionDetails.getRequestCancelledBy());
            brandingReqCancelModel.setRequestCancelledDate(brandingRequisitionDetails.getRequestCancelledDate());
            brandingReqCancelModel.setCancelComment(brandingRequisitionDetails.getCancelComment());


            //LOG.info("outbound branding requisition cancellation:: " + brandingReqCancelModel);
            getEyDmsSapCpiDefaultOutboundService().sendBrandingRequisitionCancelToSlct(brandingReqCancelModel).subscribe(

                    // onNext
                    responseEntityMap -> {

                        if (isSentSuccessfully(responseEntityMap)) {

                            LOG.info(String.format("The Branding [%s] Requisition Cancellation has been successfully sent to the SLCT through SCPI! %n%s",
                                    brandingRequisitionDetails.getRequisitionNumber(), getPropertyValue(responseEntityMap, RESPONSE_MESSAGE)));

                        } else {
                            LOG.info(String.format("The Branding [%s] Requisition Cancellation has not been successfully sent to the SLCT through SCPI! %n%s",
                                    brandingRequisitionDetails.getRequisitionNumber(), getPropertyValue(responseEntityMap, RESPONSE_MESSAGE)));
                        }

                       businessProcessService.triggerEvent(eydmsBrandingProcessModel.getCode()+"_WAIT_FOR_BRANDING_APPROVAL");
                    }

                    // onError
                    , error -> {

                        LOG.error(String.format("The Branding [%s] Requisition Cancellation has not been successfully sent to the SLCT through SCPI! %n%s", brandingRequisitionDetails.getRequisitionNumber(), error.getMessage()), error);
                        //final String eventName = new StringBuilder().append(SapOrderExchangeActionConstants.ERP_ORDER_SEND_COMPLETION_EVENT).append(order.getCode()).toString();
                        //businessProcessService.triggerEvent(eventName);
                    }

            );

        }
        return Transition.OK;
    }

    public EyDmsSapCpiOutboundService getEyDmsSapCpiDefaultOutboundService() {
        return eydmsSapCpiDefaultOutboundService;
    }

    public void setEyDmsSapCpiDefaultOutboundService(EyDmsSapCpiOutboundService eydmsSapCpiDefaultOutboundService) {
        this.eydmsSapCpiDefaultOutboundService = eydmsSapCpiDefaultOutboundService;
    }
}
