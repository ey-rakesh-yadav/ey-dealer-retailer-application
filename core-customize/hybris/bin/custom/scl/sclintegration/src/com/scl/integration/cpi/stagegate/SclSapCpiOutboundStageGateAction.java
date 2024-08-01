package com.scl.integration.cpi.stagegate;

import com.oracle.truffle.js.builtins.helper.JSONData;
import com.scl.core.constants.SclCoreConstants;
import com.scl.core.model.*;
import com.scl.integration.cpi.order.SclSapCpiOutboundService;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.processengine.action.AbstractProceduralAction;
import de.hybris.platform.task.RetryLaterException;
import netscape.javascript.JSObject;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;

import javax.annotation.Resource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static de.hybris.platform.sap.sapcpiadapter.service.SapCpiOutboundService.isSentSuccessfully;

public class SclSapCpiOutboundStageGateAction extends AbstractProceduralAction<SclOutboundStageGateProcessModel> {

    public static final String WE = "WE";
    private static final Logger LOG = Logger.getLogger(SclSapCpiOutboundStageGateAction.class);

    @Resource
    private SclSapCpiOutboundService sclSapCpiDefaultOutboundService;
    @Resource
    private SclSapCpiOutboundStageGateConversionService sclSapCpiOutboundStageGateConversionService;

    @Override
    public void executeAction(SclOutboundStageGateProcessModel sclOutboundStageGateProcessModel) throws RetryLaterException, Exception {

        DeliveryItemModel deliveryItem=sclOutboundStageGateProcessModel.getDeliveryItem();

        sclSapCpiDefaultOutboundService.sendStageGate(sclSapCpiOutboundStageGateConversionService.convertDIToStageGateOutBound(sclOutboundStageGateProcessModel.getDeliveryItem(), sclOutboundStageGateProcessModel.getOrderEntry())).subscribe(

                        // onNext
                        responseEntityMap -> {
                            if (isRequestSentSuccessfully(responseEntityMap,deliveryItem)) {
                                LOG.info(String.format("Outbound Stage gate for di [%s] has been sent to the SAP backend through SCPI !", sclOutboundStageGateProcessModel.getDeliveryItem().getDiNumber()));
                            } else {
                                LOG.error(String.format("Outbound Stage gate for di  [%s] has been sent to the SAP backend through SCPI But failed !",
                                        sclOutboundStageGateProcessModel.getDeliveryItem().getDiNumber()));
                            }
                        }
                        // onError
                        , error -> {
                    LOG.error(String.format("Outbound Stage gate for di  [%s] has been not sent to the SAP backend through SCPI! %n%s",
                            sclOutboundStageGateProcessModel.getDeliveryItem().getDiNumber(), error.getMessage(), error));
                });
    }

    private boolean isRequestSentSuccessfully(ResponseEntity<Map> responseEntityMap, DeliveryItemModel deliveryItemModel)
    {
        if (Objects.nonNull(responseEntityMap.getStatusCode()) && responseEntityMap.getStatusCode().toString().equalsIgnoreCase(SclCoreConstants.CREATED_SUCCESSFULLY) && null!=responseEntityMap.getBody()) {
            Map<String, HashMap<String,String>> resBody = (HashMap<String, HashMap<String,String>>) responseEntityMap.getBody();
            if(resBody.get("deliveriesItem")!=null) {
                List<Map> diMapList= (List<Map>) resBody.get("deliveriesItem");

                LOG.info("StageGate action isRequestSentSuccessfully for dino: "+ deliveryItemModel.getDiNumber() +" response body: "+ resBody.toString());

                for (Map<String, String> di : diMapList) {
                    if(deliveryItemModel.getDiNumber().equalsIgnoreCase(di.get("diNumber"))) {
                        deliveryItemModel.setCarrierId(di.get("carrierId"));
                        deliveryItemModel.setConsigneeId(di.get("consigneeId"));
                        deliveryItemModel.setDeliveryLineNumber(di.get("deliveryLineNumber"));
                        deliveryItemModel.setInvoiceNumber(di.get("invoiceNumber"));
                        deliveryItemModel.setInvoiceLineNumber(di.get("invoiceLineNumber"));
                        deliveryItemModel.setTruckNo(di.get("truckNo"));
                        deliveryItemModel.setErpDriverNumber(di.get("erpDriverNumber"));
                        deliveryItemModel.setErpOrderNumber(di.get("erpOrderNumber"));
                        deliveryItemModel.setErpOrderType(di.get("erpOrderType"));
                        deliveryItemModel.setTokenNumber(di.get("tokenNumber"));
                        deliveryItemModel.setTransporterName(di.get("transporterName"));
                        deliveryItemModel.setTransporterPhoneNumber(di.get("transporterPhoneNumber"));
                        deliveryItemModel.setParentId(di.get("parentId"));

                        if(StringUtils.isNotEmpty(di.get("diCreationDateAndTime")))
                            deliveryItemModel.setDiCreationDateAndTime(convertStringToDate(di.get("diCreationDateAndTime")));
                        if(StringUtils.isNotEmpty(di.get("truckAllocatedDate")))
                            deliveryItemModel.setTruckAllocatedDate(convertStringToDate(di.get("truckAllocatedDate")));
                        if(StringUtils.isNotEmpty(di.get("truckDispatchedDateAndTime")))
                            deliveryItemModel.setTruckDispatchedDateAndTime(convertStringToDate(di.get("truckDispatchedDateAndTime")));
                        if(StringUtils.isNotEmpty(di.get("invoiceCancelDate")))
                            deliveryItemModel.setInvoiceCancelDate(convertStringToDate(di.get("invoiceCancelDate")));
                        if(StringUtils.isNotEmpty(di.get("invoiceCreationDateAndTime")))
                            deliveryItemModel.setInvoiceCreationDateAndTime(convertStringToDate(di.get("invoiceCreationDateAndTime")));
                        if(StringUtils.isNotEmpty(di.get("truckAllocatedQty")))
                            deliveryItemModel.setTruckAllocatedQty(Double.valueOf(di.get("truckAllocatedQty")));
                        if(StringUtils.isNotEmpty(di.get("invoiceQuantity")))
                            deliveryItemModel.setInvoiceQuantity(Double.valueOf(di.get("invoiceQuantity")));
                        if(StringUtils.isNotEmpty(di.get("invoiceCancelQuantity")))
                            deliveryItemModel.setInvoiceCancelQuantity(Double.valueOf(di.get("invoiceCancelQuantity")));
                        if(StringUtils.isNotEmpty(di.get("diQuantity")))
                            deliveryItemModel.setDiQuantity(Double.valueOf(di.get("diQuantity")));

                    }
                }
            }
            LOG.info("Response Stage Gate Get Call=" + resBody.toString());
            modelService.save(deliveryItemModel);
            return true;
        }

        return  false;
    }

    public Date convertStringToDate(String strDate){
        Date date;
        try {
            date =new  SimpleDateFormat("yyyyMMddHHmmss").parse(strDate);
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse date: ", e);
        }
        return date;
    }



}
