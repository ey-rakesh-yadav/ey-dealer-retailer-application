package com.eydms.integration.cpi.order;

import com.eydms.core.enums.OrderType;
import de.hybris.platform.core.enums.ExportStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.sap.orderexchange.constants.SapOrderExchangeActionConstants;
import de.hybris.platform.sap.sapcpiadapter.service.SapCpiOutboundService;
import de.hybris.platform.sap.sapcpiorderexchange.actions.SapCpiOmmOrderOutboundAction;
import de.hybris.platform.sap.sapcpiorderexchange.service.SapCpiOrderOutboundConversionService;
import de.hybris.platform.task.RetryLaterException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import static de.hybris.platform.sap.sapcpiadapter.service.SapCpiOutboundService.*;

import java.util.Date;

public class EyDmsSapCpiOmmOrderOutboundAction extends SapCpiOmmOrderOutboundAction {

    private static final Logger LOG = Logger.getLogger(EyDmsSapCpiOmmOrderOutboundAction.class);

    private EyDmsSapCpiOutboundService eydmsSapCpiDefaultOutboundService;

    private EyDmsSapCpiOutboundOrderConversionService eydmsSapCpiOutboundOrderConversionService;
    @Override
    public void executeAction(OrderProcessModel process) throws RetryLaterException {

        final OrderModel order = process.getOrder();

        if (order.getOrderType().equals(OrderType.SO)) {
            getEyDmsSapCpiDefaultOutboundService().sendOrder(getEyDmsSapCpiOutboundOrderConversionService().convertOrderToSapCpiOrder(order)).subscribe(

                    // onNext
                    responseEntityMap -> {

                        if (isSentSuccessfully(responseEntityMap)) {

                            setOrderStatus(order, ExportStatus.EXPORTED);
                            resetEndMessage(process);
                            LOG.info(String.format("The OMM order [%s] has been successfully sent to the SAP backend through SCPI! %n%s",
                                    order.getCode(), getPropertyValue(responseEntityMap, RESPONSE_MESSAGE)));

                        } else {

                            setOrderStatus(order, ExportStatus.NOTEXPORTED);
                            LOG.error(String.format("The OMM order [%s] has not been sent to the SAP backend! %n%s",
                                    order.getCode(), getPropertyValue(responseEntityMap, RESPONSE_MESSAGE)));

                        }

                        final String eventName = new StringBuilder().append(SapOrderExchangeActionConstants.ERP_ORDER_SEND_COMPLETION_EVENT).append(order.getCode()).toString();
                        getBusinessProcessService().triggerEvent(eventName);

                    }

                    // onError
                    , error -> {

                        setOrderStatus(order, ExportStatus.NOTEXPORTED);
                        LOG.error(String.format("The OMM order [%s] has not been sent to the SAP backend through SCPI! %n%s", order.getCode(), error.getMessage()), error);

                        final String eventName = new StringBuilder().append(SapOrderExchangeActionConstants.ERP_ORDER_SEND_COMPLETION_EVENT).append(order.getCode()).toString();
                        getBusinessProcessService().triggerEvent(eventName);

                    }

            );
            order.setIsOrderSendtoErpOnce(true);
            order.setOrderSendtoErpDate(new Date());
            modelService.save(order);
            modelService.refresh(order);
        }
        else if(order.getOrderType().equals(OrderType.ISO))
        {
            getEyDmsSapCpiDefaultOutboundService().sendIsoOrder(getEyDmsSapCpiOutboundOrderConversionService().convertISOOrderToSapCpiOrder(order)).subscribe(

                    // onNext
                    responseEntityMap -> {

                        if (isSentSuccessfully(responseEntityMap)) {

                            setOrderStatus(order, ExportStatus.EXPORTED);
                            resetEndMessage(process);
                            LOG.info(String.format("The OMM order [%s] has been successfully sent to the SAP backend through SCPI! %n%s",
                                    order.getCode(), getPropertyValue(responseEntityMap, RESPONSE_MESSAGE)));

                        } else {

                            setOrderStatus(order, ExportStatus.NOTEXPORTED);
                            LOG.error(String.format("The OMM order [%s] has not been sent to the SAP backend! %n%s",
                                    order.getCode(), getPropertyValue(responseEntityMap, RESPONSE_MESSAGE)));

                        }

                        final String eventName = new StringBuilder().append(SapOrderExchangeActionConstants.ERP_ORDER_SEND_COMPLETION_EVENT).append(order.getCode()).toString();
                        getBusinessProcessService().triggerEvent(eventName);

                    }

                    // onError
                    , error -> {

                        setOrderStatus(order, ExportStatus.NOTEXPORTED);
                        LOG.error(String.format("The OMM order [%s] has not been sent to the SAP backend through SCPI! %n%s", order.getCode(), error.getMessage()), error);

                        final String eventName = new StringBuilder().append(SapOrderExchangeActionConstants.ERP_ORDER_SEND_COMPLETION_EVENT).append(order.getCode()).toString();
                        getBusinessProcessService().triggerEvent(eventName);

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

    public EyDmsSapCpiOutboundOrderConversionService getEyDmsSapCpiOutboundOrderConversionService() {
        return eydmsSapCpiOutboundOrderConversionService;
    }

    public void setEyDmsSapCpiOutboundOrderConversionService(EyDmsSapCpiOutboundOrderConversionService eydmsSapCpiOutboundOrderConversionService) {
        this.eydmsSapCpiOutboundOrderConversionService = eydmsSapCpiOutboundOrderConversionService;
    }
}
