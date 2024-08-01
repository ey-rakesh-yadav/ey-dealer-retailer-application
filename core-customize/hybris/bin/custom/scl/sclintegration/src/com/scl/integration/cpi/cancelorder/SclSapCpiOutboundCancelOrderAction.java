package com.scl.integration.cpi.cancelorder;

import com.scl.core.enums.OrderType;
import com.scl.core.model.SclOrderCancelProcessModel;
import com.scl.integration.cpi.order.SclSapCpiOmmOrderOutboundAction;
import com.scl.integration.cpi.order.SclSapCpiOutboundOrderConversionService;
import com.scl.integration.cpi.order.SclSapCpiOutboundService;
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

public class SclSapCpiOutboundCancelOrderAction extends AbstractProceduralAction<SclOrderCancelProcessModel> {

    private static final Logger LOG = Logger.getLogger(SclSapCpiOutboundCancelOrderAction.class);

    private SclSapCpiOutboundService sclSapCpiDefaultOutboundService;
    private SclSapCpiOutboundCancelOrderConversionService sclSapCpiOutboundCancelOrderConversionService;

    @Override
    public void executeAction(SclOrderCancelProcessModel sclOrderCancelProcessModel) throws RetryLaterException, Exception {

        OrderModel order = sclOrderCancelProcessModel.getOrder();
        if(order.getOrderType().equals(OrderType.SO)) {
            getSclSapCpiDefaultOutboundService().sendCancelOrder(getSclSapCpiOutboundCancelOrderConversionService().convertOrderToSapCpiCancelOrder(order)).subscribe(
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

    public SclSapCpiOutboundService getSclSapCpiDefaultOutboundService() {
        return sclSapCpiDefaultOutboundService;
    }

    public void setSclSapCpiDefaultOutboundService(SclSapCpiOutboundService sclSapCpiDefaultOutboundService) {
        this.sclSapCpiDefaultOutboundService = sclSapCpiDefaultOutboundService;
    }

    public SclSapCpiOutboundCancelOrderConversionService getSclSapCpiOutboundCancelOrderConversionService() {
        return sclSapCpiOutboundCancelOrderConversionService;
    }

    public void setSclSapCpiOutboundCancelOrderConversionService(SclSapCpiOutboundCancelOrderConversionService sclSapCpiOutboundCancelOrderConversionService) {
        this.sclSapCpiOutboundCancelOrderConversionService = sclSapCpiOutboundCancelOrderConversionService;
    }

    protected void setOrderStatus(final OrderModel order, final ExportStatus exportStatus) {
        order.setExportStatus(exportStatus);
        save(order);

    }
}
