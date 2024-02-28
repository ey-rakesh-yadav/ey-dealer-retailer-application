package com.eydms.integration.cpi.address;

import com.eydms.core.model.EyDmsOutboundShipToPartyModel;
import com.eydms.core.model.EyDmsUserModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;

public interface EyDmsSapCpiOutboundAddressConversionService {

    EyDmsOutboundShipToPartyModel convertShipToAddrToSapCpiAddress(AddressModel addressModel, CustomerModel customerModel, BaseSiteModel baseSiteModel, EyDmsUserModel eydmsUser);
}
