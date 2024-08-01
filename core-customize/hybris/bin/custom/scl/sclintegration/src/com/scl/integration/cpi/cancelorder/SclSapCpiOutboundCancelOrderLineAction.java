package com.scl.integration.cpi.cancelorder;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.enums.OrderType;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.SclOrderLineCancelProcessModel;
import com.scl.integration.cpi.order.SclSapCpiOutboundService;
import de.hybris.platform.core.enums.ExportStatus;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.processengine.action.AbstractProceduralAction;
import de.hybris.platform.task.RetryLaterException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static de.hybris.platform.sap.sapcpiadapter.service.SapCpiOutboundService.*;

public class SclSapCpiOutboundCancelOrderLineAction extends AbstractProceduralAction<SclOrderLineCancelProcessModel> {

    public static final String SUCCESSFULLY = "successfully";
    private static final Logger LOG = Logger.getLogger(SclSapCpiOutboundCancelOrderAction.class);

    private SclSapCpiOutboundService sclSapCpiDefaultOutboundService;
    private SclSapCpiOutboundCancelOrderConversionService sclSapCpiOutboundCancelOrderConversionService;

    @Override
    public void executeAction(SclOrderLineCancelProcessModel sclOrderLineCancelProcessModel) throws RetryLaterException, Exception {

        OrderModel order = sclOrderLineCancelProcessModel.getOrder();
        Integer entryNumber = sclOrderLineCancelProcessModel.getEntryNumber();
        Integer crmEntryNumber = sclOrderLineCancelProcessModel.getCrmEntryNumber();
        OrderEntryModel oe = (OrderEntryModel) order.getEntries().stream().filter(e -> (e.getEntryNumber().equals(crmEntryNumber) & (Integer.valueOf(e.getErpLineItemId()) == entryNumber))).collect(Collectors.toList()).get(0);
        if (order.getOrderType().equals(OrderType.SO)) {
            getSclSapCpiDefaultOutboundService().sendCancelOrder(getSclSapCpiOutboundCancelOrderConversionService().convertOrderToSapCpiCancelOrderLine(order, entryNumber, crmEntryNumber)).subscribe(
                    // onNext
                    responseEntityMap -> {

                        if (isOrderCancelledSuccessfully(responseEntityMap,oe)) {

                            setOrderEntryStatus(oe, ExportStatus.EXPORTED);

                            LOG.info(String.format("The Cancel order Line [%s] has been successfully sent to the SAP backend through SCPI!",
                                    order.getCode()));

                        } else {

                            setOrderEntryStatus(oe, ExportStatus.NOTEXPORTED);
                            LOG.error(String.format("The Cancel order Line [%s] has not been sent to the SAP backend!",
                                    order.getCode()));

                        }
                    }

                    // onError
                    , error -> {

                        setOrderEntryStatus(oe, ExportStatus.NOTEXPORTED);
                        LOG.error(String.format("The Cancel order Line [%s] has not been sent to the SAP backend through SCPI! %n%s", order.getCode(), error.getMessage()), error);
                    }
            );
        } else if (order.getOrderType().equals(OrderType.ISO)) {
            getSclSapCpiDefaultOutboundService().sendIsoCancelOrder(getSclSapCpiOutboundCancelOrderConversionService().convertISOOrderToSapCpiCancelOrderLine(order, entryNumber, crmEntryNumber)).subscribe(
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

    private boolean isOrderCancelledSuccessfully(ResponseEntity<Map> responseEntityMap, OrderEntryModel oe)
    {
        if (Objects.nonNull(responseEntityMap.getStatusCode()) && responseEntityMap.getStatusCode().toString().equalsIgnoreCase(SclCoreConstants.CREATED_SUCCESSFULLY) && null!=responseEntityMap.getBody()) {
            Map<String, HashMap<String,String>> resBody = (HashMap<String, HashMap<String,String>>) responseEntityMap.getBody();
            if(resBody.get(SclCoreConstants.ISSOREJECT)!=null) {
                 Map<String, String> iSSOREJECTBody =resBody.get(SclCoreConstants.ISSOREJECT);
                 if(StringUtils.isNotEmpty(iSSOREJECTBody.get(SclCoreConstants.SHIPTOIDMSG))  && iSSOREJECTBody.get(SclCoreConstants.SHIPTOIDMSG).toLowerCase().contains(SUCCESSFULLY) ){
                     oe.setCancelOrderLineApiStatusDesc(iSSOREJECTBody.get(SclCoreConstants.SHIPTOIDMSG));
                     return true;
                }
                 else  if(StringUtils.isNotEmpty(iSSOREJECTBody.get(SclCoreConstants.SHIPTOIDMSG))){
                     oe.setCancelOrderLineApiStatusDesc(iSSOREJECTBody.get(SclCoreConstants.SHIPTOIDMSG));
                 }

            }
        }


        return  false;
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

    protected void setOrderStatus(OrderModel order, final ExportStatus exportStatus) {
        order.setExportStatus(exportStatus);

        save(order);
    }

    protected void setOrderEntryStatus(OrderEntryModel oe, final ExportStatus exportStatus) {
        oe.setCancelOrderLineApiStatus(exportStatus);
        save(oe);

    }
}
