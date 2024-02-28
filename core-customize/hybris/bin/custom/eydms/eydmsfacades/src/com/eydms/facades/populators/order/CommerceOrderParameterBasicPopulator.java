package com.eydms.facades.populators.order;

import de.hybris.platform.commercefacades.order.data.AddToCartParams;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.storelocator.model.PointOfServiceModel;
import de.hybris.platform.storelocator.pos.PointOfServiceService;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Resource;

public class CommerceOrderParameterBasicPopulator implements Populator<AddToCartParams, CommerceCartParameter> {

    @Resource
    private ProductService productService;
    @Resource
    private PointOfServiceService pointOfServiceService;

    @Override
    public void populate(AddToCartParams addToCartParams, CommerceCartParameter parameter) throws ConversionException {

        parameter.setEnableHooks(false);
        if (StringUtils.isNotEmpty(addToCartParams.getStoreId()))
        {
            final PointOfServiceModel pointOfServiceModel = pointOfServiceService.getPointOfServiceForName(
                    addToCartParams.getStoreId());
            parameter.setPointOfService(pointOfServiceModel);
        }
        if (addToCartParams.getProductCode() != null)
        {
            final ProductModel product = productService.getProductForCode(addToCartParams.getProductCode());
            parameter.setProduct(product);
            parameter.setUnit(product.getUnit());
        }
        parameter.setQuantity(addToCartParams.getQuantity());
        parameter.setCreateNewEntry(false);
        parameter.setEntryGroupNumbers(addToCartParams.getEntryGroupNumbers());

        parameter.setSelectedDeliveryDate(addToCartParams.getSelectedDeliveryDate());
        parameter.setSelectedDeliverySlot(addToCartParams.getSelectedDeliverySlot());
        parameter.setTruckNo(addToCartParams.getTruckNo());
        parameter.setDriverContactNo(addToCartParams.getDriverContactNo());
        parameter.setCalculatedDeliveryDate(addToCartParams.getCalculatedDeliveryDate());
        parameter.setCalculatedDeliverySlot(addToCartParams.getCalculatedDeliverySlot());
        parameter.setSequence(addToCartParams.getSequence());
        parameter.setQuantityMT(addToCartParams.getQuantityMT());
        parameter.setWarehouseCode(addToCartParams.getWarehouseCode());
        parameter.setRouteId(addToCartParams.getRouteId());
        parameter.setRemarks(addToCartParams.getRemarks());
    }
}
