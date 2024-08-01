package com.scl.core.services;

import com.scl.core.model.SclCustomerModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;

public interface SCLProductService {

    SearchPageData<ProductModel> fetchProducts(SearchPageData searchPageData, SclCustomerModel sclCustomer, String transportationZone, String baseSiteId);

    List<String> getProductsAliasForSalesPerformance();

    String getProductAlias(ProductModel productModel, String state, String district);

    boolean getProductType(ProductModel productModel, String state, String district);


}
