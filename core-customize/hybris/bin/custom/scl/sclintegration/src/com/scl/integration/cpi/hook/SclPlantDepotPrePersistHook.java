/*
 *  * Copyright (c) SCL. All rights reserved.
 */

package com.scl.integration.cpi.hook;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.inboundservices.persistence.PersistenceContext;
import de.hybris.platform.inboundservices.persistence.hook.PrePersistHook;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class SclPlantDepotPrePersistHook implements PrePersistHook {
    Logger log = LoggerFactory.getLogger(SclPlantDepotPrePersistHook.class);


    private ModelService modelService;
    private ConfigurationService configurationService;



    @Override
    public Optional<ItemModel> execute(ItemModel item, PersistenceContext context) {
    if (item != null  && item instanceof WarehouseModel) {
        log.info("SclPlantDepoyPrePersistHook called....");


        WarehouseModel wareHouse= (WarehouseModel) item;

        log.debug("SclPlantDepoyPrePersistHook executed for warehouse code:" + wareHouse.getCode());
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
