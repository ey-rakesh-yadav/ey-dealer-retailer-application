package com.scl.integration.cpi.cancelorder;

import com.scl.core.model.SclOutboundCancelOrderModel;
import com.scl.core.model.SclOutboundShipToPartyModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;

public interface SclSapCpiOutboundCancelOrderConversionService {

    SclOutboundCancelOrderModel convertOrderToSapCpiCancelOrder(OrderModel orderModel);

    SclOutboundCancelOrderModel convertOrderToSapCpiCancelOrderLine(OrderModel orderModel, Integer entryNumber, Integer crmEntryNumber);

    SclOutboundCancelOrderModel convertISOOrderToSapCpiCancelOrderLine(OrderModel orderModel, Integer entryNumber, Integer crmEntryNumber);
}
