package com.scl.integration.cpi.order;
import com.scl.core.jalo.SclOutboundErpLineItemId;
import com.scl.core.model.SalesHistoryModel;
import com.scl.core.model.SclOutboundErpItemModel;
import com.scl.core.model.SclOutboundErpLineItemIdModel;
import com.scl.core.model.SclOutboundLineItemIdModel;
import de.hybris.platform.processengine.BusinessProcessService;
import org.apache.log4j.Logger;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static de.hybris.platform.sap.sapcpiadapter.service.SapCpiOutboundService.*;

public class SclMissingOrderEntriesToSLCT {
    private static final Logger LOG = Logger.getLogger(SclMissingOrderEntriesToSLCT.class);

    @Resource
    SclSapCpiOutboundService sclSapCpiDefaultOutboundService;

    @Resource
    BusinessProcessService businessProcessService;

    public boolean sendErpLineItemIdToSLCT(List<SalesHistoryModel> lineItemIdList) {
        LOG.info("sending ERP line item id to slct");
        List<SclOutboundErpLineItemIdModel> list = new ArrayList<>();
        if(lineItemIdList!=null && !lineItemIdList.isEmpty()) {
            for (SalesHistoryModel salesHistoryModel : lineItemIdList) {
                SclOutboundErpLineItemIdModel sclOutboundErpLineItemId = new SclOutboundErpLineItemIdModel();
                sclOutboundErpLineItemId.setErpLineItemId(salesHistoryModel.getLineId());
                sclOutboundErpLineItemId.setCustomerTransactionId(salesHistoryModel.getCustomerTransactionId());
                sclOutboundErpLineItemId.setCustomerTransactionLineId(salesHistoryModel.getCustomerTransactionLineId());
                sclOutboundErpLineItemId.setBrand(salesHistoryModel.getBrand());
                LOG.info("sclOutboundErpLineItemId data" + " "+ " erp line item id "+ sclOutboundErpLineItemId.getErpLineItemId() + " " + " customer transaction id "+ sclOutboundErpLineItemId.getCustomerTransactionId() + " " + "invoice no " + sclOutboundErpLineItemId.getInvoiceNo() + " " + "brand:" +sclOutboundErpLineItemId.getBrand());
                list.add(sclOutboundErpLineItemId);
            }
        }

        SclOutboundErpItemModel sclOutboundErpItemModel = new SclOutboundErpItemModel();
        Long currentTimeInMillis = System.currentTimeMillis();
        sclOutboundErpItemModel.setId("OrderSalesReplication".concat(String.valueOf(currentTimeInMillis)));
        LOG.info("Erp line item id list for outbound " + list.size());
        sclOutboundErpItemModel.setErpLineItemIds(list);

        sclSapCpiDefaultOutboundService.sendErpLineItemIdToSLCT(sclOutboundErpItemModel).subscribe(

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