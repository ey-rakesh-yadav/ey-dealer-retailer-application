package com.scl.integration.cpi.address;

import com.scl.core.model.SclOutboundShipToPartyModel;
import com.scl.core.model.SclUserModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;

public interface SclSapCpiOutboundAddressConversionService {

    SclOutboundShipToPartyModel convertShipToAddrToSapCpiAddress(AddressModel addressModel, CustomerModel customerModel, BaseSiteModel baseSiteModel, SclUserModel sclUser);
}
