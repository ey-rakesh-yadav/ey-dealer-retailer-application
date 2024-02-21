package com.eydms.integration.cpi.order;

import com.eydms.facades.data.EyDmsOutboundOrderData;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.sap.sapcpiadapter.data.SapCpiOrder;

public interface EyDmsSapCpiOmmOrderConversionService {

    EyDmsOutboundOrderData convertOrderToSapCpiOrder(OrderModel orderModel);
}
