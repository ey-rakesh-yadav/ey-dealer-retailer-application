package com.eydms.integration.cpi.cancelorder;

import com.eydms.core.enums.OrderType;
import com.eydms.core.model.EyDmsOrderCancelProcessModel;
import com.eydms.integration.cpi.order.EyDmsSapCpiOmmOrderOutboundAction;
import com.eydms.integration.cpi.order.EyDmsSapCpiOutboundOrderConversionService;
import com.eydms.integration.cpi.order.EyDmsSapCpiOutboundService;
import de.hybris.platform.core.enums.ExportStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.action.AbstractProceduralAction;
import de.hybris.platform.sap.orderexchange.constants.SapOrderExchangeActionConstants;
import de.hybris.platform.task.RetryLaterException;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Optional;

import static de.hybris.platform.sap.sapcpiadapter.service.SapCpiOutboundService.*;

public class EyDmsSapCpiOutboundCancelOrderAction extends AbstractProceduralAction<EyDmsOrderCancelProcessModel> {

    private static final Logger LOG = Logger.getLogger(EyDmsSapCpiOutboundCancelOrderAction.class);

    private EyDmsSapCpiOutboundService eydmsSapCpiDefaultOutboundService;
    private EyDmsSapCpiOutboundCancelOrderConversionService eydmsSapCpiOutboundCancelOrderConversionService;

    @Override
    public void executeAction(EyDmsOrderCancelProcessModel eydmsOrderCancelProcessModel) throws RetryLaterException, Exception {

        OrderModel order = eydmsOrderCancelProcessModel.getOrder();
        if(order.getOrderType().equals(OrderType.SO)) {
            getEyDmsSapCpiDefaultOutboundService().sendCancelOrder(getEyDmsSapCpiOutboundCancelOrderConversionService().convertOrderToSapCpiCancelOrder(order)).subscribe(
                    // onNext
                    responseEntityMap -> {

                        if (isSentSuccessfully(responseEntityMap)) {

                            setOrderStatus(order, ExportStatus.EXPORTED);
                            LOG.info(String.format("The OMM order [%s] has been successfully sent to the SAP backend through SCPI! %n%s",
                                    order.getCode(), getPropertyValue(responseEntityMap, RESPONSE_MESSAGE)));

                        } else {

                            setOrderStatus(order, ExportStatus.NOTEXPORTED);
                            LOG.error(String.format("The OMM order [%s] has not been sent to the SAP backend! %n%s",
                                    order.getCode(), getPropertyValue(responseEntityMap, RESPONSE_MESSAGE)));

                        }
                    }

                    // onError
                    , error -> {

                        setOrderStatus(order, ExportStatus.NOTEXPORTED);
                        LOG.error(String.format("The OMM order [%s] has not been sent to the SAP backend through SCPI! %n%s", order.getCode(), error.getMessage()), error);
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
