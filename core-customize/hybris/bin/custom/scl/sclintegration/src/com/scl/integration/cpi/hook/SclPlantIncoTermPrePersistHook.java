/*
 *  * Copyright (c) SCL. All rights reserved.
 */

package com.scl.integration.cpi.hook;

import com.scl.core.enums.IncoTerms;
import com.scl.core.model.PlantIncoTermModel;
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

public class SclPlantIncoTermPrePersistHook implements PrePersistHook {
    Logger log = LoggerFactory.getLogger(SclPlantIncoTermPrePersistHook.class);


    private ModelService modelService;
    private ConfigurationService configurationService;

    public final String FTB="FTB";
    public final String FTP="FTB";
    public final String EXP="EXP";
    public final String EXB="EXB";


    @Override
    public Optional<ItemModel> execute(ItemModel item, PersistenceContext context) {
    if (item != null  && item instanceof PlantIncoTermModel) {
        log.info("SclPlantIncoTermPrePersistHook called....");

        PlantIncoTermModel planeInco= (PlantIncoTermModel) item;
         
        log.debug("SclPlantIncoTermPrePersistHook executed for plant code:" + planeInco.getSource().getCode()+ " and inco Term " + planeInco.getIncoterms());



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
