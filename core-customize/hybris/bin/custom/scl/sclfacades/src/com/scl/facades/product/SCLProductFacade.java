package com.scl.facades.product;

import com.scl.core.model.SclCustomerModel;
import com.scl.facades.data.ProductAliasListData;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

public interface SCLProductFacade {

    SearchPageData<ProductData> getProductList(SearchPageData searchPageData, SclCustomerModel sclCustomer, String transportationZone, String baseSiteId);

    ProductAliasListData getProductsAlias();
}
