package com.eydms.integration.cpi.cancelorder;

import com.eydms.core.model.EyDmsOutboundCancelOrderModel;
import com.eydms.core.model.EyDmsOutboundShipToPartyModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;

public interface EyDmsSapCpiOutboundCancelOrderConversionService {

    EyDmsOutboundCancelOrderModel convertOrderToSapCpiCancelOrder(OrderModel orderModel);

    EyDmsOutboundCancelOrderModel convertOrderToSapCpiCancelOrderLine(OrderModel orderModel, Integer entryNumber, Integer crmEntryNumber);

    EyDmsOutboundCancelOrderModel convertISOOrderToSapCpiCancelOrderLine(OrderModel orderModel, Integer entryNumber, Integer crmEntryNumber);
}
