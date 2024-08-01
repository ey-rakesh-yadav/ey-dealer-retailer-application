package com.scl.integration.cpi.dealer;

import com.scl.core.model.DealerRetailerMappingModel;
import com.scl.core.model.SclDealerRetailerMappingProcessModel;
import com.scl.integration.cpi.order.SclSapCpiOutboundService;
import com.scl.integration.model.SclOutboundDealerRetailerMappingModel;
import de.hybris.platform.processengine.action.AbstractAction;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.task.RetryLaterException;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

import static de.hybris.platform.sap.sapcpiadapter.service.SapCpiOutboundService.RESPONSE_MESSAGE;
import static de.hybris.platform.sap.sapcpiadapter.service.SapCpiOutboundService.getPropertyValue;
import static de.hybris.platform.sap.sapcpiadapter.service.SapCpiOutboundService.isSentSuccessfully;

public  class SendDealerRetailerMappingToCpiAction  extends AbstractAction<SclDealerRetailerMappingProcessModel>{

    private static final Logger LOG = Logger.getLogger(SendDealerRetailerMappingToCpiAction.class);

    private SclSapCpiOutboundService sclSapCpiDefaultOutboundService;


    @Override
    public String execute(SclDealerRetailerMappingProcessModel process) throws RetryLaterException, Exception {
        LOG.info("SendDealerRetailerMappingToCpiAction Called ..."+ process.getDealerRetailer().getType());
        getSclSapCpiDefaultOutboundService().sendDealerRetailerMappingToSLCT(getOutboundDealerRetailerMappingDetails(process.getDealerRetailer())).subscribe(
                responseEntityMap  -> {
                    if (isSentSuccessfully(responseEntityMap)) {

                        LOG.info(String.format("DealerRetailerMapping has been successfully sent to the SLCT through SCPI! %n%s",
                               getPropertyValue(responseEntityMap, RESPONSE_MESSAGE)));

                    } else {
                        LOG.info(String.format("DealerRetailerMapping has not been successfully sent to the SLCT through SCPI! %n%s",
                              getPropertyValue(responseEntityMap, RESPONSE_MESSAGE)));
                    }

                }

                , error -> {

                    LOG.error(String.format("DealerRetailerMapping has not been successfully sent to the SLCT through SCPI! %n%s", error.getMessage()), error);

                }
        );
        return Transition.OK.toString();
    }

    private SclOutboundDealerRetailerMappingModel getOutboundDealerRetailerMappingDetails(DealerRetailerMappingModel model){
        SclOutboundDealerRetailerMappingModel outboundMappingModel= new SclOutboundDealerRetailerMappingModel();
        outboundMappingModel.setDealer(model.getDealer().getUid());
        outboundMappingModel.setRetailer(model.getRetailer().getUid());
        outboundMappingModel.setShipTo(model.getShipTo().getErpAddressId());
        outboundMappingModel.setType(model.getType());

        return outboundMappingModel;

    }

    public enum Transition
    {
        OK, NOK;

        public static Set<String> getStringValues()
        {
            final Set<String> res = new HashSet<String>();

            for (final Transition transition : Transition.values())
            {
                res.add(transition.toString());
            }
            return res;
        }
    }

    @Override
    public Set<String> getTransitions() {
        return Transition.getStringValues();
    }

    public SclSapCpiOutboundService getSclSapCpiDefaultOutboundService() {
        return sclSapCpiDefaultOutboundService;
    }

    public void setSclSapCpiDefaultOutboundService(SclSapCpiOutboundService sclSapCpiDefaultOutboundService) {
        this.sclSapCpiDefaultOutboundService = sclSapCpiDefaultOutboundService;
    }
}
