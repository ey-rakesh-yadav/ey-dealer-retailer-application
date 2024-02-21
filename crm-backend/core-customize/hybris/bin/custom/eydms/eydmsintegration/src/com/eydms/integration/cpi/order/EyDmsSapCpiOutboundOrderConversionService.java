package com.eydms.integration.cpi.order;

import com.eydms.core.model.EyDmsOutboundOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.sap.sapcpiadapter.model.SAPCpiOutboundOrderModel;

public interface EyDmsSapCpiOutboundOrderConversionService {

    EyDmsOutboundOrderModel convertOrderToSapCpiOrder(OrderModel orderModel);

    default <T extends EyDmsOutboundOrderModel> T convertOrderToSapCpiOrder(OrderModel orderModel, T eydmsOutboundOrderModel) {
        return eydmsOutboundOrderModel;
    }

    EyDmsOutboundOrderModel convertISOOrderToSapCpiOrder(OrderModel orderModel);
}
