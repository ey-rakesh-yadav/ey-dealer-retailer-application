package com.eydms.integration.cpi.order;

import com.eydms.core.model.EyDmsOutboundOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.sap.sapcpiadapter.model.SAPCpiOutboundOrderModel;

public interface EyDmsSapCpiOmmOrderMapperService<SOURCE extends OrderModel, TARGET extends EyDmsOutboundOrderModel> {

    void map(SOURCE source, TARGET target);
}
