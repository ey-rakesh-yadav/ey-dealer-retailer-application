package com.eydms.integration.cpi.cancelorder;

import com.eydms.core.enums.OrderType;
import com.eydms.core.model.EyDmsOrderLineCancelProcessModel;
import com.eydms.integration.cpi.order.EyDmsSapCpiOutboundService;
import de.hybris.platform.core.enums.ExportStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.processengine.action.AbstractProceduralAction;
import de.hybris.platform.task.RetryLaterException;
import org.apache.log4j.Logger;

import static de.hybris.platform.sap.sapcpiadapter.service.SapCpiOutboundService.*;

public class EyDmsSapCpiOutboundCancelOrderLineAction extends AbstractProceduralAction<EyDmsOrderLineCancelProcessModel> {

    private static final Logger LOG = Logger.getLogger(EyDmsSapCpiOutboundCancelOrderAction.class);

    private EyDmsSapCpiOutboundService eydmsSapCpiDefaultOutboundService;
    private EyDmsSapCpiOutboundCancelOrderConversionService eydmsSapCpiOutboundCancelOrderConversionService;

    @Override
    public void executeAction(EyDmsOrderLineCancelProcessModel eydmsOrderLineCancelProcessModel) throws RetryLaterException, Exception {
        
        OrderModel order = eydmsOrderLineCancelProcessModel.getOrder();
        Integer entryNumber = eydmsOrderLineCancelProcessModel.getEntryNumber();
        Integer crmEntryNumber = eydmsOrderLineCancelProcessModel.getCrmEntryNumber();
        if(order.getOrderType().equals(OrderType.SO)) {
            getEyDmsSapCpiDefaultOutboundService().sendCancelOrder(getEyDmsSapCpiOutboundCancelOrderConversionService().convertOrderToSapCpiCancelOrderLine(order, entryNumber, crmEntryNumber)).subscribe(
                    // onNext
                    responseEntityMap -> {

                        if (isSentSuccessfully(responseEntityMap)) {

                            setOrderStatus(order, ExportStatus.EXPORTED);
                            LOG.info(String.format("The Cancel order Line [%s] has been successfully sent to the SAP backend through SCPI! %n%s",
                                    order.getCode(), getPropertyValue(responseEntityMap, RESPONSE_MESSAGE)));

                        } else {

                            setOrderStatus(order, ExportStatus.NOTEXPORTED);
                            LOG.error(String.format("The Cancel order Line [%s] has not been sent to the SAP backend! %n%s",
                                    order.getCode(), getPropertyValue(responseEntityMap, RESPONSE_MESSAGE)));

                        }
                    }

                    // onError
                    , error -> {

                        setOrderStatus(order, ExportStatus.NOTEXPORTED);
                        LOG.error(String.format("The Cancel order Line [%s] has not been sent to the SAP backend through SCPI! %n%s", order.getCode(), error.getMessage()), error);
                    }
            );
        }

        else if (order.getOrderType().equals(OrderType.ISO))
        {
            getEyDmsSapCpiDefaultOutboundService().sendIsoCancelOrder(getEyDmsSapCpiOutboundCancelOrderConversionService().convertISOOrderToSapCpiCancelOrderLine(order, entryNumber, crmEntryNumber)).subscribe(
                    // onNext
                    responseEntityMap -> {

                        if (isSentSuccessfully(responseEntityMap)) {

                            setOrderStatus(order, ExportStatus.EXPORTED);
                            LOG.info(String.format("The Cancel order Line [%s] has been successfully sent to the SAP backend through SCPI! %n%s",
                                    order.getCode(), getPropertyValue(responseEntityMap, RESPONSE_MESSAGE)));

                        } else {

                            setOrderStatus(order, ExportStatus.NOTEXPORTED);
                            LOG.error(String.format("The Cancel order Line [%s] has not been sent to the SAP backend! %n%s",
                                    order.getCode(), getPropertyValue(responseEntityMap, RESPONSE_MESSAGE)));

                        }
                    }

                    // onError
                    , error -> {

                        setOrderStatus(order, ExportStatus.NOTEXPORTED);
                        LOG.error(String.format("The Cancel order Line [%s] has not been sent to the SAP backend through SCPI! %n%s", order.getCode(), error.getMessage()), error);
                    }
            );

        }
    }

    public EyDmsSapCpiOutboundService getEyDmsSapCpiDefaultOutboundService() {
        return eydmsSapCpiDefaultOutboundService;
    }

    public void setEyDmsSapCpiDefaultOutboundService(EyDmsSapCpiOutboundService eydmsSapCpiDefaultOutboundService) {
        this.eydmsSapCpiDefaultOutboundService = eydmsSapCpiDefaultOutboundService;
    }

    public EyDmsSapCpiOutboundCancelOrderConversionService getEyDmsSapCpiOutboundCancelOrderConversionService() {
        return eydmsSapCpiOutboundCancelOrderConversionService;
    }

    public void setEyDmsSapCpiOutboundCancelOrderConversionService(EyDmsSapCpiOutboundCancelOrderConversionService eydmsSapCpiOutboundCancelOrderConversionService) {
        this.eydmsSapCpiOutboundCancelOrderConversionService = eydmsSapCpiOutboundCancelOrderConversionService;
    }

    protected void setOrderStatus(final OrderModel order, final ExportStatus exportStatus) {
        order.setExportStatus(exportStatus);
        save(order);

    }
}
