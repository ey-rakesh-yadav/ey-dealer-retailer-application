/*
 *  * Copyright (c) SCL. All rights reserved.
 */

package com.scl.integration.cpi.hook;

import com.scl.core.model.GeographicalMasterModel;
import com.scl.core.model.SclCustomerModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.inboundservices.persistence.PersistenceContext;
import de.hybris.platform.inboundservices.persistence.hook.PrePersistHook;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class SclGeographicalMasterPrePersistHook implements PrePersistHook {
    Logger log = LoggerFactory.getLogger(SclGeographicalMasterPrePersistHook.class);


    private ModelService modelService;
    private ConfigurationService configurationService;



    @Override
    public Optional<ItemModel> execute(ItemModel item, PersistenceContext context) {
    if (item != null  && item instanceof GeographicalMasterModel) {
        log.info("SclGeographicalMasterPrePersistHook called....");
        GeographicalMasterModel geoMas= (GeographicalMasterModel) item;

        if(StringUtils.isNotEmpty(geoMas.getState())) {
            geoMas.setGeographicalState(geoMas.getState());
        }

        log.debug("SclGeographicalMasterPrePersistHook executed for territoryCode uid:" + geoMas.getTerritoryCode());

        return Optional.of(item);

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
