package com.eydms.core.customer.dao;

import java.util.List;

import com.eydms.core.model.EyDmsCustomerModel;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

public interface EyDmsEndCustomerDao {
	
	EyDmsCustomerModel getRegisteredEndCustomer(String userUid);
	
	List<ProductModel> getProductsForBrand(String brand);
	
	B2BCustomerModel getEndCustomerDetails(String mobileNo);
	
	SearchPageData<EyDmsCustomerModel> getAllInfluncersForStateDistrict(SearchPageData searchPageData, String InfluencerType, String site, String state, String district, String city, String pincode);
}
