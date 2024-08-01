/*
 *  * Copyright (c) SCL. All rights reserved.
 */

package com.scl.integration.cpi.hook;

import com.scl.core.dao.SlctCrmIntegrationDao;
import com.scl.core.enums.CustomerCategory;
import de.hybris.platform.b2b.company.B2BCommerceUnitService;
import de.hybris.platform.basecommerce.enums.InStockStatus;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.inboundservices.persistence.PersistenceContext;
import de.hybris.platform.inboundservices.persistence.hook.PrePersistHook;
import de.hybris.platform.ordersplitting.WarehouseService;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.product.UnitService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

public class SclProductPrePersistHook implements PrePersistHook {
    Logger log = LoggerFactory.getLogger(SclProductPrePersistHook.class);


    private ModelService modelService;
    private ConfigurationService configurationService;

    private WarehouseService warehouseService;

    private SlctCrmIntegrationDao slctCrmIntegrationDao;
    private UnitService unitService;






    @Override
    public Optional<ItemModel> execute(ItemModel item, PersistenceContext context) {
    if (item != null  && item instanceof ProductModel) {
        log.info("SclProductPrePersistHook called....");

        ProductModel product= (ProductModel) item;

        log.debug("SclProductPrePersistHook executed for product code:" + product.getCode() );

        try {
           /* if (Objects.nonNull(product.getSapSalesOrganization())
                    && product.getSapSalesOrganization().getSalesOrganization().equals("1000")
                    && product.getSapSalesOrganization().getDivision().equals("10")
                    && product.getSapSalesOrganization().getDistributionChannel().equals("10")) {
                product.setCustCategory(CustomerCategory.TR);
            }*/
            product.setCustCategory(CustomerCategory.TR);

            if (getModelService().isNew(product)) {
                product.setEquivalenceProductCode(product.getCode());

                WarehouseModel warehouseModel = getWarehouseService().getWarehouseForCode("sclWarehouse");

                StockLevelModel stockLevelModel = modelService.create(StockLevelModel.class);
                stockLevelModel.setWarehouse(warehouseModel);
                stockLevelModel.setProductCode(product.getCode());
                stockLevelModel.setAvailable(1);
                stockLevelModel.setInStockStatus(InStockStatus.FORCEINSTOCK);
                stockLevelModel.setPreOrder(0);
                stockLevelModel.setMaxStockLevelHistoryCount(-1);
                stockLevelModel.setOverSelling(0);
                stockLevelModel.setMaxPreOrder(1);
                stockLevelModel.setReserved(0);
                modelService.save(stockLevelModel);

                PriceRowModel priceRowModel = modelService.create(PriceRowModel.class);
                priceRowModel.setProductId(product.getCode());
                ;
                priceRowModel.setUnit(getUnitService().getUnitForCode("pieces"));
                priceRowModel.setCurrency(getSlctCrmIntegrationDao().getCurrencyModelByISOCode("INR"));
                priceRowModel.setPrice(1D);
                priceRowModel.setMinqtd(1L);
                priceRowModel.setUnitFactor(1);
                priceRowModel.setNet(false);
                modelService.save(priceRowModel);
            }
        }catch (RuntimeException e){
            log.info("SclProductPrePersistHook for product:" + product.getCode() + " Exception: " + e.getMessage());
            e.printStackTrace();
        }

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

    public WarehouseService getWarehouseService() {
        return warehouseService;
    }

    public void setWarehouseService(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    public SlctCrmIntegrationDao getSlctCrmIntegrationDao() {
        return slctCrmIntegrationDao;
    }

    public void setSlctCrmIntegrationDao(SlctCrmIntegrationDao slctCrmIntegrationDao) {
        this.slctCrmIntegrationDao = slctCrmIntegrationDao;
    }

    public UnitService getUnitService() {
        return unitService;
    }

    public void setUnitService(UnitService unitService) {
        this.unitService = unitService;
    }
}
