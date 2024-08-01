package com.scl.facades.populators;

import com.scl.core.dao.DataConstraintDao;
import com.scl.core.enums.ProductType;
import com.scl.core.enums.WarehouseType;
import com.scl.core.model.DestinationSourceMasterModel;
import com.scl.core.model.SclPlantLotSizeModel;
import com.scl.core.services.SCLProductService;
import com.scl.core.source.dao.SclPlantLotSizeDao;
import com.scl.facades.cart.impl.DefaultSclCartFacade;
import com.scl.facades.data.DestinationSourceMasterData;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.scl.facades.cart.impl.DefaultSclCartFacade.ENABLE_LOT_SIZE_VALIDATION;

public class DestinationSourcePopulator implements Populator<DestinationSourceMasterModel, DestinationSourceMasterData> {

    private static final Logger LOG = LoggerFactory.getLogger(DestinationSourcePopulator.class);
    @Autowired
    private SclPlantLotSizeDao sclPlantLotSizeDao;

    @Autowired
    private SCLProductService sclProductService;

    @Autowired
    private BaseSiteService baseSiteService;

    @Autowired
    private CatalogVersionService catalogVersionService;

    @Autowired
    private ProductService productService;

    @Autowired
    private DataConstraintDao dataConstraintDao;

    

    @Override
    public void populate(DestinationSourceMasterModel source, DestinationSourceMasterData target) throws ConversionException {
        if(null != source) {
            target.setCity(source.getDestinationCity());
            target.setSourcePriority(source.getSourcePriority());
            target.setSourceType(source.getType().getCode());
            target.setSourceName(source.getSource().getName());
            target.setSourceCode(source.getSource().getCode());
            target.setNcrCost(source.getNcrCost());
            target.setDeliveryMode(source.getDeliveryMode().getCode());
            target.setRouteId(source.getRoute());
            if(target.getSourcePriority()!=null && target.getSourcePriority().length()>1) {
            	target.setPriority(Integer.valueOf(target.getSourcePriority().substring(1)));
            }
            String enableLotSizeValidation = dataConstraintDao.findVersionByConstraintName(ENABLE_LOT_SIZE_VALIDATION);
            if(BooleanUtils.isTrue(Boolean.valueOf(enableLotSizeValidation))) {
                   ProductModel productModel = getProductModel(source.getProductCode());
                   boolean isPremiumProduct = sclProductService.getProductType(productModel, source.getDestinationState(), source.getDestinationDistrict());
                   LOG.info(String.format("isPremiumProduct flag ::%s for product::%s and source::%s",isPremiumProduct,productModel.getCode(),source.getSource().getCode()));
                   Integer lotSize = getSourcePlantLotSize(source.getSource(), source.getDestinationState(), source.getDestinationDistrict(), isPremiumProduct);
                   target.setLotSize(lotSize != 0 ? String.valueOf(lotSize) : Strings.EMPTY);
                   LOG.info(String.format("LotSize ::%s updated for source::%s ",lotSize,target.getSourceCode()));

           }
        }
    }

    /**
     * @param source
     * @param state
     * @param district
     * @param isPremiumProduct
     * @return
     */
    private Integer getSourcePlantLotSize(WarehouseModel source, String state, String district, boolean isPremiumProduct) {

         List<SclPlantLotSizeModel> plantLotSizeModels = sclPlantLotSizeDao.findAllSourcePlantLotSize(state, district, source, isPremiumProduct);
        if (CollectionUtils.isNotEmpty(plantLotSizeModels)) {
            LOG.info(String.format("plantLotSize model size::%s with pk::%s",plantLotSizeModels.size(),plantLotSizeModels));
            List<SclPlantLotSizeModel> withPlantList = plantLotSizeModels.stream().filter(plantLot -> Objects.nonNull(plantLot.getPlant())).collect(Collectors.toList());
            List<SclPlantLotSizeModel> withoutPlantList = plantLotSizeModels.stream().filter(plantLot -> Objects.isNull(plantLot.getPlant())).collect(Collectors.toList());

            //withPlant
            if (CollectionUtils.isNotEmpty(withPlantList)) {
                LOG.info(String.format("withPlantList  size::%s with pk::%s",withPlantList.size(),withPlantList));
                for (SclPlantLotSizeModel withPlant : withPlantList) {
                    if (ProductType.PREMIUM.equals(withPlant.getProductType()) || ProductType.NONPREMIUM.equals(withPlant.getProductType())) {
                        LOG.info(String.format(""));
                        return Objects.nonNull(withPlant.getLotSize()) ? withPlant.getLotSize() : 0;
                    } else if (ProductType.BOTH.equals(withPlant.getProductType())) {
                        return Objects.nonNull(withPlant.getLotSize()) ? withPlant.getLotSize() : 0;
                    }
                }
            }
            //withoutPlant
            if (CollectionUtils.isNotEmpty(withoutPlantList)) {
                LOG.info(String.format("withoutPlantList  size::%s with pk::%s",withoutPlantList.size(),withoutPlantList));
                for (SclPlantLotSizeModel withoutPlant : withoutPlantList) {
                    if (ProductType.PREMIUM.equals(withoutPlant.getProductType()) || ProductType.NONPREMIUM.equals(withoutPlant.getProductType())) {
                        return Objects.nonNull(withoutPlant.getLotSize()) ? withoutPlant.getLotSize() : 0;
                    } else if (ProductType.BOTH.equals(withoutPlant.getProductType())) {
                        return Objects.nonNull(withoutPlant.getLotSize()) ? withoutPlant.getLotSize() : 0;
                    }
                }
            }
        }
        return 0;
    }

    /**
     *
     * @param productCode
     * @return
     */
    private ProductModel getProductModel(String productCode) {
        BaseSiteModel baseSite = baseSiteService.getCurrentBaseSite();
        CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSite.getUid() + "ProductCatalog", "Online");
        ProductModel product = productService.getProductForCode(catalogVersion, productCode);
        return product;
    }

}
