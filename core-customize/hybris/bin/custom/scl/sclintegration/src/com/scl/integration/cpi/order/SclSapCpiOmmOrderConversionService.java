package com.scl.integration.cpi.order;

import com.scl.facades.data.SclOutboundOrderData;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.sap.sapcpiadapter.data.SapCpiOrder;

public interface SclSapCpiOmmOrderConversionService {

    SclOutboundOrderData convertOrderToSapCpiOrder(OrderModel orderModel);
}
