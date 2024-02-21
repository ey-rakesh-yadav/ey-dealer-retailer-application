package com.eydms.integration.cpi.address.impl;

import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.model.EyDmsOutboundShipToPartyModel;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.integration.constants.EyDmsintegrationConstants;
import com.eydms.integration.cpi.address.EyDmsSapCpiOutboundAddressConversionService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class EyDmsSapCpiOutboundAddressConversionServiceImpl implements EyDmsSapCpiOutboundAddressConversionService {

    private static final Logger LOG = Logger.getLogger(EyDmsSapCpiOutboundAddressConversionServiceImpl.class);

    @Autowired
    BaseSiteService baseSiteService;

    @Autowired
    UserService userService;

    @Override
    public EyDmsOutboundShipToPartyModel convertShipToAddrToSapCpiAddress(AddressModel addressModel, CustomerModel customerModel, BaseSiteModel baseSiteModel, EyDmsUserModel eydmsUserModel) {
        LOG.info("convertShipToAddrToSapCpiAddress getting called");

        EyDmsOutboundShipToPartyModel eydmsOutboundShipToParty = new EyDmsOutboundShipToPartyModel();
        eydmsOutboundShipToParty.setOrgId(baseSiteModel.getUid());
        eydmsOutboundShipToParty.setOwnerCode(customerModel.getUid());
        eydmsOutboundShipToParty.setAccountName(addressModel.getAccountName());
        eydmsOutboundShipToParty.setCustomerAddress1(addressModel.getLine1());
        eydmsOutboundShipToParty.setCustomerAddress2(addressModel.getLine2());
        eydmsOutboundShipToParty.setCustomerAddress3(EyDmsintegrationConstants.DEFAULT_VALUE_X);
        eydmsOutboundShipToParty.setCity(addressModel.getErpCity());
        eydmsOutboundShipToParty.setTaluka(addressModel.getTaluka());
        eydmsOutboundShipToParty.setDistrict(addressModel.getDistrict());
        eydmsOutboundShipToParty.setState(addressModel.getState());
        eydmsOutboundShipToParty.setSp_ha_mobile("0");
        eydmsOutboundShipToParty.setOfficerMobile("0");
        eydmsOutboundShipToParty.setDuplicate(addressModel.getDuplicate());
        eydmsOutboundShipToParty.setPostalCode(addressModel.getPostalcode());
        eydmsOutboundShipToParty.setCustomerNo(((EyDmsCustomerModel) customerModel).getCustomerNo());
        eydmsOutboundShipToParty.setCustomerEmail(((EyDmsCustomerModel) customerModel).getEmail());
        eydmsOutboundShipToParty.setCustomerMobileNo(addressModel.getCellphone());
        eydmsOutboundShipToParty.setUsername(eydmsUserModel.getEmployeeCode());
        return eydmsOutboundShipToParty;
    }
}