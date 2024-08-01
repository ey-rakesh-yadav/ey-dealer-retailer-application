package com.scl.integration.cpi.address.impl;

import com.scl.core.enums.CustomerGrouping;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.SclOutboundShipToPartyModel;
import com.scl.core.model.SclUserModel;
import com.scl.integration.constants.SclintegrationConstants;
import com.scl.integration.cpi.address.SclSapCpiOutboundAddressConversionService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

public class SclSapCpiOutboundAddressConversionServiceImpl implements SclSapCpiOutboundAddressConversionService {

    private static final Logger LOG = Logger.getLogger(SclSapCpiOutboundAddressConversionServiceImpl.class);

    @Autowired
    BaseSiteService baseSiteService;

    @Autowired
    UserService userService;

    @Override
    public SclOutboundShipToPartyModel convertShipToAddrToSapCpiAddress(AddressModel addressModel, CustomerModel customerModel, BaseSiteModel baseSiteModel, SclUserModel sclUserModel) {
        LOG.info("convertShipToAddrToSapCpiAddress getting called");

        SclOutboundShipToPartyModel sclOutboundShipToParty = new SclOutboundShipToPartyModel();
        sclOutboundShipToParty.setCustomerNo(((SclCustomerModel) customerModel).getCustomerNo());
        sclOutboundShipToParty.setRetailerUid(addressModel.getRetailerUid());
        sclOutboundShipToParty.setTitle((Objects.nonNull(addressModel.getTitle()))? addressModel.getTitle().getCode() : StringUtils.EMPTY);
        sclOutboundShipToParty.setAccountName(addressModel.getAccountName());
        sclOutboundShipToParty.setCustomerAddress1(addressModel.getLine1());
        sclOutboundShipToParty.setCustomerAddress2(addressModel.getLine2());
        sclOutboundShipToParty.setDistrict(addressModel.getDistrict());
       // sclOutboundShipToParty.setTaluka(addressModel.getTaluka());
        sclOutboundShipToParty.setCity(addressModel.getErpCity());
        sclOutboundShipToParty.setPostalCode(addressModel.getPostalcode());
        sclOutboundShipToParty.setCountry(addressModel.getCountry().getIsocode());
       // sclOutboundShipToParty.setRegion((Objects.nonNull(addressModel.getRegion()))? addressModel.getRegion().getIsocode():StringUtils.EMPTY);
        sclOutboundShipToParty.setLatitude(addressModel.getLatitude());
        sclOutboundShipToParty.setLongitude(addressModel.getLongitude());
        sclOutboundShipToParty.setCustomerGrouping(CustomerGrouping.YDOM);
        sclOutboundShipToParty.setCustomerEmail(addressModel.getEmail());
        sclOutboundShipToParty.setCustomerMobileNo(addressModel.getCellphone());
        sclOutboundShipToParty.setCustomerAddress3(addressModel.getLine3());
        sclOutboundShipToParty.setCustomerAddress4(addressModel.getLine4());
        sclOutboundShipToParty.setCustomerAddress5(addressModel.getLine5());
        sclOutboundShipToParty.setTransportationZone(Objects.nonNull(addressModel.getGeographicalMaster()) ? addressModel.getGeographicalMaster().getTransportationZone():null);

        return sclOutboundShipToParty;
    }
}