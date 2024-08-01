package com.scl.facades.populators;


import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.lang.StringUtils;


public class ProductMinAndMaxQuantityPopulator implements Populator<ProductModel,ProductData> 
{

	@Override
	public void populate(ProductModel source, ProductData target) throws ConversionException {
		
		target.setMinOrderQuantity(source.getMinOrderQuantity());
		target.setMaxOrderQuantity(source.getMaxOrderQuantity());
		
	}

}
