/*
 *  * Copyright (c) SCL. All rights reserved.
 */

package com.scl.integration.cpi.hook;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.DataConstraintDao;
import com.scl.core.dao.TerritoryManagementDao;
import com.scl.core.enums.CounterType;
import com.scl.core.enums.CustomerCategory;
import com.scl.core.model.DealerRetailerMappingModel;
import com.scl.core.model.GeographicalMasterModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.SclUserModel;

import com.scl.core.model.SubAreaMasterModel;
import com.scl.core.region.dao.GeographicalRegionDao;
import com.scl.integration.cpi.hook.exception.ValidTransportationZoneNotFoundException;
import com.scl.integration.service.SclintegrationService;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.inboundservices.persistence.PersistenceContext;
import de.hybris.platform.inboundservices.persistence.hook.PrePersistHook;
import de.hybris.platform.inboundservices.persistence.hook.impl.PersistenceHookException;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.user.User;
import de.hybris.platform.jalo.user.UserManager;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.exceptions.ClassMismatchException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.Session;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class SclCustomerPrePersistHook implements PrePersistHook {
    Logger log = LoggerFactory.getLogger(SclCustomerPrePersistHook.class);

    public static final String FIND_SCLDEALER_PASSword = "FIND_SCLDEALER_PASSword";
    public static final String FIND_SCLRETAILER_PASSword = "FIND_SCLRETAILER_PASSword";

    public static final String FIND_SCLDEALER_PASSword_QAENV = "FIND_SCLDEALER_PASSword_QAENV";
    public static final String FIND_SCLRETAILER_PASSword_QAENV = "FIND_SCLRETAILER_PASSword_QAENV";


    private ModelService modelService;
    private ConfigurationService configurationService;

    @Resource
    private UserService userService;

    @Resource
    DataConstraintDao dataConstraintDao;

    @Resource(name = "sclintegrationService")
    private SclintegrationService sclintegrationService;

    @Resource
    TerritoryManagementDao territoryManagementDao;

    @Resource
    private GeographicalRegionDao geographicalRegionDao;

  

    @Override
    public Optional<ItemModel> execute(ItemModel item, PersistenceContext context) {
    if (item != null  && item instanceof SclCustomerModel) {
        log.info("SclCustomerPrePersistHook called....");
        SclCustomerModel sclCust= (SclCustomerModel) item;
        Set<PrincipalGroupModel> ugSet=new HashSet<>(sclCust.getGroups());

        try {

        	if (Objects.nonNull(sclCust.getCustomerGrouping())) {
                if (sclCust.getCustomerGrouping().getCode().equalsIgnoreCase("ZRET")) {
                    ugSet.add(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID));
                    sclCust.setGroups(ugSet);
                    sclCust.setCounterType(CounterType.RETAILER);
                    if(modelService.isNew(sclCust)){
                        modelService.save(sclCust);
                        modelService.refresh(sclCust);
                        String prodEnv = configurationService.getConfiguration().getString("prod.environment");
                        if(org.apache.commons.lang.StringUtils.isNotEmpty(prodEnv) && prodEnv.equals("true")) {
                            sclCust.setPassword(dataConstraintDao.findPasswordByConstraintName(FIND_SCLRETAILER_PASSword));
                        }
                        else {
                            sclCust.setPassword(dataConstraintDao.findPasswordByConstraintName(FIND_SCLRETAILER_PASSword_QAENV));
                        }
                        sclCust.setLoginDisabled(false);
                    }
                } else if (sclCust.getCustomerGrouping().getCode().equalsIgnoreCase("ZDOM")) {
                    ugSet.add(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID));
                    sclCust.setGroups(ugSet);
                    sclCust.setCounterType(CounterType.DEALER);
                    if(modelService.isNew(sclCust)) {
                        modelService.save(sclCust);
                        modelService.refresh(sclCust);

                        String prodEnv = configurationService.getConfiguration().getString("prod.environment");
                        if(org.apache.commons.lang.StringUtils.isNotEmpty(prodEnv) && prodEnv.equals("true")) {
                            sclCust.setPassword(dataConstraintDao.findPasswordByConstraintName(FIND_SCLDEALER_PASSword));
                        }
                        else {
                            sclCust.setPassword(dataConstraintDao.findPasswordByConstraintName(FIND_SCLDEALER_PASSword_QAENV));
                        }
                        sclCust.setLoginDisabled(false);
                    }

                } else if (sclCust.getCustomerGrouping().getCode().equalsIgnoreCase("YDOM")) {
                    ugSet.add(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SHIPTO_USER_GROUP_UID));
                    sclCust.setGroups(ugSet);
                    sclCust.setCounterType(CounterType.SHIPTOPARTY);
                }

            }
        	
        	sclCust.setCustomerCategory(CustomerCategory.TR);

            if (CollectionUtils.isNotEmpty(sclCust.getAddresses())) {
                for (AddressModel add : sclCust.getAddresses()) {
                    if(StringUtils.isNotEmpty(add.getTransportationZone())){
                        GeographicalMasterModel gm = geographicalRegionDao.getGeographyMasterForTransZone(add.getTransportationZone());
                        if(Objects.nonNull(gm)) {
                            add.setGeographicalMaster(gm);
                        }
                        else {
                         log.error("ValidTransportationZoneNotFoundException : Valid transportation zone not found for: %s",add.getTransportationZone());
                        }

                    }
                    if (StringUtils.isNotEmpty(add.getSapAddressUsage()) && add.getSapAddressUsage().equalsIgnoreCase("DE")) {
                        sclCust.setContactNumber(StringUtils.isNotEmpty(add.getCellphone()) ? add.getCellphone() :null);
                        sclCust.setMobileNumber(StringUtils.isNotEmpty(add.getCellphone()) ? add.getCellphone() :null);
                        if(Objects.nonNull(add.getGeographicalMaster()) && StringUtils.isNotEmpty(add.getGeographicalMaster().getState()) && StringUtils.isNotEmpty(add.getGeographicalMaster().getDistrict())) {
                            sclCust.setState(add.getGeographicalMaster().getState());
                            sclCust.setDistrict(add.getGeographicalMaster().getDistrict());
                            sclCust.setTaluka(StringUtils.isNotEmpty(add.getGeographicalMaster().getTaluka()) ? add.getGeographicalMaster().getTaluka() : StringUtils.EMPTY);
                            sclCust.setRegionMaster(sclintegrationService.getRegionMaster(add.getGeographicalMaster().getRegion()));
                            sclCust.setDistrictMaster(sclintegrationService.getDistrictMaster(add.getGeographicalMaster().getDistrictCode()));
                        }
                    }
                    if (Objects.nonNull(add.getGeographicalMaster()) && StringUtils.isNotEmpty(add.getGeographicalMaster().getState()) && StringUtils.isNotEmpty(add.getGeographicalMaster().getDistrict())) {
                        add.setState(add.getGeographicalMaster().getState());
                        add.setDistrict(add.getGeographicalMaster().getDistrict());
                        add.setErpCity(add.getGeographicalMaster().getErpCity());
                        add.setTaluka(StringUtils.isNotEmpty(add.getGeographicalMaster().getTaluka())? add.getGeographicalMaster().getTaluka():StringUtils.EMPTY);
                        add.setPostalcode(StringUtils.isNotEmpty(add.getGeographicalMaster().getPincode())? add.getGeographicalMaster().getPincode(): StringUtils.EMPTY);
                    }
                }
            }

            if(StringUtils.isNotEmpty(sclCust.getDistrict()) && StringUtils.isNotEmpty(sclCust.getTaluka())){
                SubAreaMasterModel subAreaMasterModel=  territoryManagementDao.getTerritoryByDistrictAndTaluka(sclCust.getDistrict(), sclCust.getTaluka());
                if(Objects.nonNull(subAreaMasterModel)){
                    sclCust.setSubAreaMaster(subAreaMasterModel);
                }

            }


        }catch (RuntimeException e){
            log.info("SclCustomerPrePersistHook Exception for sclCust:" + sclCust.getUid() + " exception: "+  e.getMessage() + " stack: "+e.getStackTrace());
           /* if(e instanceof ValidTransportationZoneNotFoundException) {
            	throw new ValidTransportationZoneNotFoundException(e.getMessage());
            }*/
        }
        log.debug("SclCustomerPrePersistHook executed for sclCustomer uid:" + sclCust.getUid());
        }
        return Optional.of(item);
    }



    public ModelService getModelService() {
        return modelService;
    }

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

   }