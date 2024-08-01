package com.scl.integration.cpi.branding;

import com.scl.core.model.*;
import com.scl.integration.cpi.order.SclSapCpiOutboundService;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.task.RetryLaterException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import static de.hybris.platform.sap.sapcpiadapter.service.SapCpiOutboundService.*;

public class SendBrandingReqCancelToSLCTAction extends AbstractSimpleDecisionAction<SclBrandingProcessModel> {

    private static final Logger LOG = Logger.getLogger(SendBrandingReqCancelToSLCTAction.class);

    private SclSapCpiOutboundService sclSapCpiDefaultOutboundService;

    @Autowired
    BusinessProcessService businessProcessService;

    @Override
    public Transition executeAction(SclBrandingProcessModel sclBrandingProcessModel) throws RetryLaterException, Exception {
        BrandingRequestDetailsModel brandingRequisitionDetails = sclBrandingProcessModel.getBrandingRequestDetails();
        SclOutboundBrandingReqCancelModel brandingReqCancelModel = new SclOutboundBrandingReqCancelModel();
        if(brandingRequisitionDetails!=null)
        {
            brandingReqCancelModel.setRequisitionNumber(brandingRequisitionDetails.getRequisitionNumber());
            brandingReqCancelModel.setRequestCancelledBy(brandingRequisitionDetails.getRequestCancelledBy());
            brandingReqCancelModel.setRequestCancelledDate(brandingRequisitionDetails.getRequestCancelledDate());
            brandingReqCancelModel.setCancelComment(brandingRequisitionDetails.getCancelComment());


            //LOG.info("outbound branding requisition cancellation:: " + brandingReqCancelModel);
            getSclSapCpiDefaultOutboundService().sendBrandingRequisitionCancelToSlct(brandingReqCancelModel).subscribe(

                    // onNext
                    responseEntityMap -> {

                        if (isSentSuccessfully(responseEntityMap)) {

                            LOG.info(String.format("The Branding [%s] Requisition Cancellation has been successfully sent to the SLCT through SCPI! %n%s",
                                    brandingRequisitionDetails.getRequisitionNumber(), getPropertyValue(responseEntityMap, RESPONSE_MESSAGE)));

                        } else {
                            LOG.info(String.format("The Branding [%s] Requisition Cancellation has not been successfully sent to the SLCT through SCPI! %n%s",
                                    brandingRequisitionDetails.getRequisitionNumber(), getPropertyValue(responseEntityMap, RESPONSE_MESSAGE)));
                        }

                       businessProcessService.triggerEvent(sclBrandingProcessModel.getCode()+"_WAIT_FOR_BRANDING_APPROVAL");
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

    public SclSapCpiOutboundService getSclSapCpiDefaultOutboundService() {
        return sclSapCpiDefaultOutboundService;
    }

    public void setSclSapCpiDefaultOutboundService(SclSapCpiOutboundService sclSapCpiDefaultOutboundService) {
        this.sclSapCpiDefaultOutboundService = sclSapCpiDefaultOutboundService;
    }
}
