package com.scl.integration.cpi.order;

import com.scl.core.model.SclOutboundOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.sap.sapcpiadapter.model.SAPCpiOutboundOrderModel;

public interface SclSapCpiOmmOrderMapperService<SOURCE extends OrderModel, TARGET extends SclOutboundOrderModel> {

    void map(SOURCE source, TARGET target);
}
