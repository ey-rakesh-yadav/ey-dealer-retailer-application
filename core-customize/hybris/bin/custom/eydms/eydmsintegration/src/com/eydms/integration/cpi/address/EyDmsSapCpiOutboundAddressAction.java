package com.eydms.integration.cpi.address;

import com.eydms.core.model.ProsDealerOnboardingProcessModel;
import com.eydms.core.model.EyDmsAddressProcessModel;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.integration.cpi.order.EyDmsSapCpiOmmOrderOutboundAction;
import com.eydms.integration.cpi.order.EyDmsSapCpiOutboundService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.processengine.action.AbstractProceduralAction;
import de.hybris.platform.task.RetryLaterException;
import org.apache.log4j.Logger;

import static de.hybris.platform.sap.sapcpiadapter.service.SapCpiOutboundService.*;

public class EyDmsSapCpiOutboundAddressAction extends AbstractProceduralAction<EyDmsAddressProcessModel> {

    private static final Logger LOG = Logger.getLogger(EyDmsSapCpiOutboundAddressAction.class);

    private EyDmsSapCpiOutboundService eydmsSapCpiDefaultOutboundService;

    private EyDmsSapCpiOutboundAddressConversionService eydmsSapCpiOutboundAddressConversionService;

    @Override
    public void executeAction(EyDmsAddressProcessModel eydmsAddressProcessModel) throws RetryLaterException, Exception {

        AddressModel addressModel = eydmsAddressProcessModel.getAddress();
        CustomerModel customerModel = eydmsAddressProcessModel.getCustomer();
        BaseSiteModel baseSite = eydmsAddressProcessModel.getBaseSite();
        EyDmsUserModel eydmsUser = eydmsAddressProcessModel.getEyDmsUser();
        getEyDmsSapCpiDefaultOutboundService().sendShipToPartyAddress(getEyDmsSapCpiOutboundAddressConversionService().convertShipToAddrToSapCpiAddress(addressModel,customerModel, baseSite,eydmsUser)).subscribe(

                        // onNext
                        responseEntityMap -> {
                            if (isSentSuccessfully(responseEntityMap)) {
                                LOG.info(String.format("Ship To Party Address [%s] has been sent to the SAP backend through SCPI!", addressModel.getOwner(),
                                        getPropertyValue(responseEntityMap, RESPONSE_MESSAGE)));
                            } else {
                                LOG.error(String.format("Ship To Party Address [%s] has been sent to the SAP backend through SCPI!",
                                        addressModel.getOwner(), getPropertyValue(responseEntityMap, RESPONSE_MESSAGE)));
                            }
                        }
                        // onError
                        , error -> {
                    LOG.error(String.format("Ship To Party Address [%s] has been not sent to the SAP backend through SCPI! %n%s",
                            addressModel.getOwner(), error.getMessage(), error));
                });
    }

    public EyDmsSapCpiOutboundService getEyDmsSapCpiDefaultOutboundService() {
        return eydmsSapCpiDefaultOutboundService;
    }

    public void setEyDmsSapCpiDefaultOutboundService(EyDmsSapCpiOutboundService eydmsSapCpiDefaultOutboundService) {
        this.eydmsSapCpiDefaultOutboundService = eydmsSapCpiDefaultOutboundService;
    }

    public EyDmsSapCpiOutboundAddressConversionService getEyDmsSapCpiOutboundAddressConversionService() {
        return eydmsSapCpiOutboundAddressConversionService;
    }

    public void setEyDmsSapCpiOutboundAddressConversionService(EyDmsSapCpiOutboundAddressConversionService eydmsSapCpiOutboundAddressConversionService) {
        this.eydmsSapCpiOutboundAddressConversionService = eydmsSapCpiOutboundAddressConversionService;
    }

}
