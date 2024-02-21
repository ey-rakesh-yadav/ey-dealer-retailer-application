package com.eydms.integration.cpi.order;
import com.eydms.core.jalo.EyDmsOutboundErpLineItemId;
import com.eydms.core.model.SalesHistoryModel;
import com.eydms.core.model.EyDmsOutboundErpItemModel;
import com.eydms.core.model.EyDmsOutboundErpLineItemIdModel;
import com.eydms.core.model.EyDmsOutboundLineItemIdModel;
import de.hybris.platform.processengine.BusinessProcessService;
import org.apache.log4j.Logger;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static de.hybris.platform.sap.sapcpiadapter.service.SapCpiOutboundService.*;

public class EyDmsMissingOrderEntriesToSLCT {
    private static final Logger LOG = Logger.getLogger(EyDmsMissingOrderEntriesToSLCT.class);

    @Resource
    EyDmsSapCpiOutboundService eydmsSapCpiDefaultOutboundService;

    @Resource
    BusinessProcessService businessProcessService;

    public boolean sendErpLineItemIdToSLCT(List<SalesHistoryModel> lineItemIdList) {
        LOG.info("sending ERP line item id to slct");
        List<EyDmsOutboundErpLineItemIdModel> list = new ArrayList<>();
        if(lineItemIdList!=null && !lineItemIdList.isEmpty()) {
            for (SalesHistoryModel salesHistoryModel : lineItemIdList) {
                EyDmsOutboundErpLineItemIdModel eydmsOutboundErpLineItemId = new EyDmsOutboundErpLineItemIdModel();
                eydmsOutboundErpLineItemId.setErpLineItemId(salesHistoryModel.getLineId());
                eydmsOutboundErpLineItemId.setCustomerTransactionId(salesHistoryModel.getCustomerTransactionId());
                eydmsOutboundErpLineItemId.setCustomerTransactionLineId(salesHistoryModel.getCustomerTransactionLineId());
                eydmsOutboundErpLineItemId.setBrand(salesHistoryModel.getBrand());
                LOG.info("eydmsOutboundErpLineItemId data" + " "+ " erp line item id "+ eydmsOutboundErpLineItemId.getErpLineItemId() + " " + " customer transaction id "+ eydmsOutboundErpLineItemId.getCustomerTransactionId() + " " + "invoice no " + eydmsOutboundErpLineItemId.getInvoiceNo() + " " + "brand:" +eydmsOutboundErpLineItemId.getBrand());
                list.add(eydmsOutboundErpLineItemId);
            }
        }

        EyDmsOutboundErpItemModel eydmsOutboundErpItemModel = new EyDmsOutboundErpItemModel();
        Long currentTimeInMillis = System.currentTimeMillis();
        eydmsOutboundErpItemModel.setId("OrderSalesReplication".concat(String.valueOf(currentTimeInMillis)));
        LOG.info("Erp line item id list for outbound " + list.size());
        eydmsOutboundErpItemModel.setErpLineItemIds(list);

        eydmsSapCpiDefaultOutboundService.sendErpLineItemIdToSLCT(eydmsOutboundErpItemModel).subscribe(

                // onNext
                responseEntityMap -> {

                    if (isSentSuccessfully(responseEntityMap)) {
                        LOG.info("Order Line Item ID has been successfully sent to the SLCT through SCPI!");
                    } else {
                        LOG.info("Order Line Item ID has not been successfully sent to the SLCT through SCPI!");
                    }
                }
                // onError
                , error -> {
                    LOG.error("Order Line Item ID has not been successfully sent to the SLCT through SCPI!");
                }
        );

        return true;

    }
}