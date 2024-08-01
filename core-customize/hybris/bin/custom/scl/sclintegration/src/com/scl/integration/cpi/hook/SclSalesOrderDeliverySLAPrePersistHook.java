/*
 *  * Copyright (c) SCL. All rights reserved.
 */

package com.scl.integration.cpi.hook;

import com.scl.core.model.SalesOrderDeliverySLAModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.inboundservices.persistence.PersistenceContext;
import de.hybris.platform.inboundservices.persistence.hook.PrePersistHook;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class SclSalesOrderDeliverySLAPrePersistHook implements PrePersistHook {
    Logger log = LoggerFactory.getLogger(SclSalesOrderDeliverySLAPrePersistHook.class);


    private ModelService modelService;
    private ConfigurationService configurationService;



    @Override
    public Optional<ItemModel> execute(ItemModel item, PersistenceContext context) {
    if (item != null  && item instanceof SalesOrderDeliverySLAModel) {
        log.info("SclSalesOrderDeliverySLAPrePersistHook called....");

        SalesOrderDeliverySLAModel salesOrderDSLA= (SalesOrderDeliverySLAModel) item;

        log.debug("SclSalesOrderDeliverySLAPrePersistHook executed for route code:" + salesOrderDSLA.getRoute() );

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
