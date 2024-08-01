/*
 *  * Copyright (c) SCL. All rights reserved.
 */

package com.scl.integration.cpi.hook;

import com.scl.core.model.GeographicalMasterModel;
import com.scl.core.model.ProductIncoTermModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.inboundservices.persistence.PersistenceContext;
import de.hybris.platform.inboundservices.persistence.hook.PrePersistHook;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class SclInboundProductIncoTermPrePersistHook implements PrePersistHook {
    Logger log = LoggerFactory.getLogger(SclInboundProductIncoTermPrePersistHook.class);


    private ModelService modelService;
    private ConfigurationService configurationService;



    @Override
    public Optional<ItemModel> execute(ItemModel item, PersistenceContext context) {
    if (item != null  && item instanceof ProductIncoTermModel) {
        log.info("SclInboundProductIncoTermPrePersistHook called....");

        ProductIncoTermModel prodInco= (ProductIncoTermModel) item;

        log.debug("SclInboundProductIncoTermPrePersistHook executed for material code:" + prodInco.getMaterial() + " and incoTerm: " + prodInco.getIncoterms());


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
