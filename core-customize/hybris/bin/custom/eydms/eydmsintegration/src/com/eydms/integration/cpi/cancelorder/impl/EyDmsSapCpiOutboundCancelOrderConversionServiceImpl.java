package com.eydms.integration.cpi.cancelorder.impl;

import com.eydms.core.model.OrderEntryNumberModel;
import com.eydms.core.model.EyDmsOutboundCancelOrderModel;
import com.eydms.integration.cpi.cancelorder.EyDmsSapCpiOutboundCancelOrderConversionService;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.model.order.OrderModel;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class EyDmsSapCpiOutboundCancelOrderConversionServiceImpl implements EyDmsSapCpiOutboundCancelOrderConversionService {

    private static final Logger LOG = Logger.getLogger(EyDmsSapCpiOutboundCancelOrderConversionServiceImpl.class);

    @Override
    public EyDmsOutboundCancelOrderModel convertOrderToSapCpiCancelOrder(OrderModel orderModel) {
        LOG.info("convertOrderToSapCpiCancelOrder getting called");
        EyDmsOutboundCancelOrderModel eydmsOutboundCancelOrder = new EyDmsOutboundCancelOrderModel();
        if(orderModel != null) {
            eydmsOutboundCancelOrder.setCrmOrderNo(orderModel.getCode());
            eydmsOutboundCancelOrder.setOrderNo(orderModel.getErpOrderNumber());
            eydmsOutboundCancelOrder.setOrderLineId("0");
            eydmsOutboundCancelOrder.setOrderType(orderModel.getErpOrderType());
            eydmsOutboundCancelOrder.setUserCode(((B2BCustomerModel)orderModel.getPlacedBy()).getEmployeeCode());
            //eydmsOutboundCancelOrder.setVersionID(orderModel.getVersionID()!= null ? orderModel.getVersionID(): null);
        }
        return eydmsOutboundCancelOrder;
    }

    @Override
    public EyDmsOutboundCancelOrderModel convertOrderToSapCpiCancelOrderLine(OrderModel orderModel, Integer entryNumber, Integer crmEntryNumber) {
        LOG.info("convertOrderToSapCpiCancelOrderLine getting called");
        EyDmsOutboundCancelOrderModel eydmsOutboundCancelOrder = new EyDmsOutboundCancelOrderModel();
        if(orderModel != null && entryNumber != null) {
            eydmsOutboundCancelOrder.setCrmOrderNo(orderModel.getCode());
            eydmsOutboundCancelOrder.setCrmEntryNumber(crmEntryNumber);
            eydmsOutboundCancelOrder.setOrderNo(orderModel.getErpOrderNumber());
            eydmsOutboundCancelOrder.setOrderLineId(String.valueOf(entryNumber));
            eydmsOutboundCancelOrder.setOrderType(orderModel.getErpOrderType());
            eydmsOutboundCancelOrder.setUserCode(((B2BCustomerModel)orderModel.getPlacedBy()).getEmployeeCode());
           //eydmsOutboundCancelOrder.setVersionID(orderModel.getVersionID()!= null ? orderModel.getVersionID(): null);
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
            eydmsOutboundCancelOrder.setOrderEntriesList(entriesList);
        }
        return eydmsOutboundCancelOrder;
    }

    @Override
    public EyDmsOutboundCancelOrderModel convertISOOrderToSapCpiCancelOrderLine(OrderModel orderModel, Integer entryNumber, Integer crmEntryNumber) {
        LOG.info("convertISOOrderToSapCpiCancelOrderLine getting called");
        EyDmsOutboundCancelOrderModel eydmsOutboundCancelOrder = new EyDmsOutboundCancelOrderModel();
        if(orderModel != null && entryNumber != null) {
            eydmsOutboundCancelOrder.setCrmOrderNo(orderModel.getCode());
            eydmsOutboundCancelOrder.setCrmEntryNumber(crmEntryNumber);
            eydmsOutboundCancelOrder.setOrderNo(orderModel.getErpOrderNumber());
            eydmsOutboundCancelOrder.setOrderLineId(String.valueOf(entryNumber));
            eydmsOutboundCancelOrder.setQuantity((double) 0);
            eydmsOutboundCancelOrder.setUserCode(((B2BCustomerModel)orderModel.getPlacedBy()).getEmployeeCode());
            eydmsOutboundCancelOrder.setRemarks(orderModel.getCancelReason());
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
            eydmsOutboundCancelOrder.setOrderEntriesList(entriesList);
            //eydmsOutboundCancelOrder.setVersionID(orderModel.getVersionID()!= null ? orderModel.getVersionID(): null);
        }
        return eydmsOutboundCancelOrder;
    }
}
