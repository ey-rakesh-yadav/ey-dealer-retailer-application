package com.scl.integration.cpi.address;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.enums.AddressCreatedStatus;
import com.scl.core.model.*;
import com.scl.integration.cpi.order.SclSapCpiOmmOrderOutboundAction;
import com.scl.integration.cpi.order.SclSapCpiOutboundService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.processengine.action.AbstractProceduralAction;
import de.hybris.platform.servicelayer.exceptions.ClassMismatchException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.task.RetryLaterException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static de.hybris.platform.sap.sapcpiadapter.service.SapCpiOutboundService.*;

public class SclSapCpiOutboundAddressAction extends AbstractProceduralAction<SclAddressProcessModel> {

    public static final String WE = "WE";
    private static final Logger LOG = Logger.getLogger(SclSapCpiOutboundAddressAction.class);

    private SclSapCpiOutboundService sclSapCpiDefaultOutboundService;

    private UserService userService;

    private SclSapCpiOutboundAddressConversionService sclSapCpiOutboundAddressConversionService;

    @Override
    public void executeAction(SclAddressProcessModel sclAddressProcessModel) throws RetryLaterException, Exception {

        AddressModel addressModel = sclAddressProcessModel.getAddress();
        if(StringUtils.isNotEmpty(addressModel.getPartnerFunctionId())) {
            addressModel.setRetailerUid(addressModel.getPartnerFunctionId());
        }
        CustomerModel customerModel = sclAddressProcessModel.getCustomer();
        BaseSiteModel baseSite = sclAddressProcessModel.getBaseSite();
        SclUserModel sclUser = sclAddressProcessModel.getSclUser();


        getSclSapCpiDefaultOutboundService().sendShipToPartyAddress(getSclSapCpiOutboundAddressConversionService().convertShipToAddrToSapCpiAddress(addressModel,customerModel, baseSite,sclUser)).subscribe(

                        // onNext
                        responseEntityMap -> {
                            if (isShipToCreatedSuccessfully(responseEntityMap,addressModel)) {
                                LOG.info(String.format("Ship To Party Address [%s] has been sent to the SAP backend through SCPI and ShipTo has been Created!", addressModel.getOwner()));
                            } else {
                                LOG.error(String.format("Ship To Party Address [%s] has been sent to the SAP backend through SCPI but ShipTo Not created!",
                                        addressModel.getOwner()));
                            }
                        }
                        // onError
                        , error -> {
                    LOG.error(String.format("Ship To Party Address [%s] has been not sent to the SAP backend through SCPI! %n%s",
                            addressModel.getOwner(), error.getMessage(), error));
                });
    }

    private boolean isShipToCreatedSuccessfully(ResponseEntity<Map> responseEntityMap, AddressModel addressModel)
    {
        if (Objects.nonNull(responseEntityMap.getStatusCode()) && responseEntityMap.getStatusCode().toString().equalsIgnoreCase(SclCoreConstants.CREATED_SUCCESSFULLY) && null!=responseEntityMap.getBody()) {
            Map<String, String> resBody = (HashMap<String, String>) responseEntityMap.getBody();
            if(StringUtils.isNotEmpty(resBody.get(SclCoreConstants.SHIPTOID))){
                addressModel.setPartnerFunctionId(resBody.get(SclCoreConstants.SHIPTOID));
                addressModel.setSapAddressUsage(WE);
                addressModel.setAddressCreatedStatus(AddressCreatedStatus.CREATEDFROMCRM);
                addressModel.setSapCustomerID(((SclCustomerModel)addressModel.getOwner()).getUid());
                addressModel.setErpAddressStatusDesc(resBody.get(SclCoreConstants.SHIPTOIDMSG));
                addressModel.setErpAddressStatus(SclCoreConstants.SUCCESSFULLY);
                modelService.save(addressModel);

//                DealerRetailerMappingModel drm = new DealerRetailerMappingModel();
//                drm.setDealer((SclCustomerModel) addressModel.getOwner());
//                drm.setShipTo(addressModel);
//                drm.setLastUsed(new Date());
//                if(Objects.nonNull(addressModel.getGeographicalMaster())) {
//                    drm.setDistrict((org.apache.commons.lang3.StringUtils.isNotEmpty(addressModel.getGeographicalMaster().getDistrict())) ? addressModel.getGeographicalMaster().getDistrict() : org.apache.commons.lang3.StringUtils.EMPTY);
//                    drm.setState((org.apache.commons.lang3.StringUtils.isNotEmpty(addressModel.getGeographicalMaster().getState())) ? addressModel.getGeographicalMaster().getState() : org.apache.commons.lang3.StringUtils.EMPTY);
//                    drm.setPartnerFunctionId((org.apache.commons.lang3.StringUtils.isNotEmpty(addressModel.getPartnerFunctionId())) ? addressModel.getPartnerFunctionId() : org.apache.commons.lang3.StringUtils.EMPTY);
//                    drm.setPinCode((org.apache.commons.lang3.StringUtils.isNotEmpty(addressModel.getGeographicalMaster().getPincode())) ? addressModel.getGeographicalMaster().getPincode() : org.apache.commons.lang3.StringUtils.EMPTY);
//                    drm.setErpCity((org.apache.commons.lang3.StringUtils.isNotEmpty(addressModel.getGeographicalMaster().getErpCity())) ? addressModel.getGeographicalMaster().getErpCity() : org.apache.commons.lang3.StringUtils.EMPTY);
//                    drm.setTaluka((org.apache.commons.lang3.StringUtils.isNotEmpty(addressModel.getGeographicalMaster().getTaluka())) ? addressModel.getGeographicalMaster().getTaluka() : org.apache.commons.lang3.StringUtils.EMPTY);
//                }
//                if(StringUtils.isNotEmpty(addressModel.getRetailerUid())){
//                    drm.setRetailer((SclCustomerModel) userService.getUserForUID(addressModel.getRetailerUid()));
//                }
//
//                modelService.save(drm);


                return true;
            }
            else{

                addressModel.setErpAddressStatus(SclCoreConstants.ERROR);
                addressModel.setErpAddressStatusDesc(resBody.get(SclCoreConstants.SHIPTOIDMSG));
                modelService.save(addressModel);
            }
        }


       return  false;
    }

    public SclSapCpiOutboundService getSclSapCpiDefaultOutboundService() {
        return sclSapCpiDefaultOutboundService;
    }

    public void setSclSapCpiDefaultOutboundService(SclSapCpiOutboundService sclSapCpiDefaultOutboundService) {
        this.sclSapCpiDefaultOutboundService = sclSapCpiDefaultOutboundService;
    }

    public SclSapCpiOutboundAddressConversionService getSclSapCpiOutboundAddressConversionService() {
        return sclSapCpiOutboundAddressConversionService;
    }

    public void setSclSapCpiOutboundAddressConversionService(SclSapCpiOutboundAddressConversionService sclSapCpiOutboundAddressConversionService) {
        this.sclSapCpiOutboundAddressConversionService = sclSapCpiOutboundAddressConversionService;
    }

}
