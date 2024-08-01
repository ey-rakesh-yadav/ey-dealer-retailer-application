package com.scl.integration.cpi.order;

import com.scl.core.model.SclOutboundOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.sap.sapcpiadapter.model.SAPCpiOutboundOrderModel;

public interface SclSapCpiOutboundOrderConversionService {

    SclOutboundOrderModel convertOrderToSapCpiOrder(OrderModel orderModel);

    default <T extends SclOutboundOrderModel> T convertOrderToSapCpiOrder(OrderModel orderModel, T sclOutboundOrderModel) {
        return sclOutboundOrderModel;
    }

    SclOutboundOrderModel convertISOOrderToSapCpiOrder(OrderModel orderModel);
}
