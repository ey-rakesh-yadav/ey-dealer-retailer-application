package com.scl.integration.cpi.stagegate;

import com.scl.core.model.DeliveryItemModel;
import com.scl.core.model.SclOutboundShipToPartyModel;
import com.scl.core.model.SclOutboundStageGateModel;
import com.scl.core.model.SclUserModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;

public interface SclSapCpiOutboundStageGateConversionService {

    SclOutboundStageGateModel convertDIToStageGateOutBound(DeliveryItemModel di, OrderEntryModel oe);
}
