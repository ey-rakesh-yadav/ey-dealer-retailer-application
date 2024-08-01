package com.scl.core.dao;

import com.scl.core.model.SclBrandModel;
import com.scl.core.model.SclCustomerModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;

public interface SCLProductDao {

    SearchPageData<ProductModel>getProductList(SearchPageData searchPageData, SclCustomerModel sclCustomer, String transportationZone, String baseSiteId);
    List<SclBrandModel> getNeilsonBrandMapping(SclCustomerModel sclCustomer);
    List<String> getProductsAliasForSalesPerformance();
    List<String> getProductsAliasForRetailer();
}
