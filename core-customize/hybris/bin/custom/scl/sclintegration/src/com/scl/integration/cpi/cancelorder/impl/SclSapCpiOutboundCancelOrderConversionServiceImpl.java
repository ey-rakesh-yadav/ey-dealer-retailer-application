package com.scl.integration.cpi.cancelorder.impl;

import com.scl.core.model.OrderEntryNumberModel;
import com.scl.core.model.SclOutboundCancelOrderModel;
import com.scl.integration.cpi.cancelorder.SclSapCpiOutboundCancelOrderConversionService;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.model.order.OrderModel;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class SclSapCpiOutboundCancelOrderConversionServiceImpl implements SclSapCpiOutboundCancelOrderConversionService {

    private static final Logger LOG = Logger.getLogger(SclSapCpiOutboundCancelOrderConversionServiceImpl.class);

    @Override
    public SclOutboundCancelOrderModel convertOrderToSapCpiCancelOrder(OrderModel orderModel) {
        LOG.info("convertOrderToSapCpiCancelOrder getting called");
        SclOutboundCancelOrderModel sclOutboundCancelOrder = new SclOutboundCancelOrderModel();
        if(orderModel != null) {
            sclOutboundCancelOrder.setCrmOrderNo(orderModel.getCode());
            sclOutboundCancelOrder.setOrderNo(orderModel.getErpOrderNumber());
            sclOutboundCancelOrder.setRejectReason(orderModel.getRejectedReason().getCode());
        }
        return sclOutboundCancelOrder;
    }

    @Override
    public SclOutboundCancelOrderModel convertOrderToSapCpiCancelOrderLine(OrderModel orderModel, Integer entryNumber, Integer crmEntryNumber) {
        LOG.info("convertOrderToSapCpiCancelOrderLine getting called");
        SclOutboundCancelOrderModel sclOutboundCancelOrder = new SclOutboundCancelOrderModel();
        if(orderModel != null && crmEntryNumber != null) {
            sclOutboundCancelOrder.setCrmOrderNo(orderModel.getCode());
            sclOutboundCancelOrder.setOrderNo(orderModel.getErpOrderNumber());

            List<OrderEntryNumberModel> entriesList = new ArrayList<>();
            if(orderModel.getEntries() != null && !orderModel.getEntries().isEmpty()) {
                orderModel.getEntries().stream().forEach(e -> {
                    if (crmEntryNumber == e.getEntryNumber()) {
                        OrderEntryNumberModel orderEntryNumberModel = new OrderEntryNumberModel();
                        orderEntryNumberModel.setCrmEntryNumber(crmEntryNumber);
                        orderEntryNumberModel.setEntryNumber(e.getErpLineItemId());
                        orderEntryNumberModel.setRejectReason(e.getRejectedReason().getCode());
                        entriesList.add(orderEntryNumberModel);
                    }
                });
            }
            sclOutboundCancelOrder.setOrderEntriesList(entriesList);

        }
        return sclOutboundCancelOrder;
    }

    @Override
    public SclOutboundCancelOrderModel convertISOOrderToSapCpiCancelOrderLine(OrderModel orderModel, Integer entryNumber, Integer crmEntryNumber) {
        LOG.info("convertISOOrderToSapCpiCancelOrderLine getting called");
        SclOutboundCancelOrderModel sclOutboundCancelOrder = new SclOutboundCancelOrderModel();
        if(orderModel != null && entryNumber != null) {
            sclOutboundCancelOrder.setCrmOrderNo(orderModel.getCode());
            sclOutboundCancelOrder.setCrmEntryNumber(crmEntryNumber);
            sclOutboundCancelOrder.setOrderNo(orderModel.getErpOrderNumber());
            sclOutboundCancelOrder.setOrderLineId(String.valueOf(entryNumber));
            sclOutboundCancelOrder.setQuantity((double) 0);
            sclOutboundCancelOrder.setUserCode(((B2BCustomerModel)orderModel.getPlacedBy()).getEmployeeCode());
            sclOutboundCancelOrder.setRemarks(orderModel.getCancelReason());
            List<OrderEntryNumberModel> entriesList = new ArrayList<>();
            if(orderModel.getEntries() != null && !orderModel.getEntries().isEmpty()) {
                orderModel.getEntries().stream().forEach(e -> {
                    if (crmEntryNumber != e.getEntryNumber()) {
                        OrderEntryNumberModel orderEntryNumberModel = new OrderEntryNumberModel();
                        orderEntryNumberModel.setEntryNumber(String.valueOf(e.getEntryNumber()));
                        entriesList.add(orderEntryNumberModel);
                    }
                });
            }
            sclOutboundCancelOrder.setOrderEntriesList(entriesList);
            //sclOutboundCancelOrder.setVersionID(orderModel.getVersionID()!= null ? orderModel.getVersionID(): null);
        }
        return sclOutboundCancelOrder;
    }
}
