package com.scl.core.customer.services;

import java.util.List;

import com.scl.facades.data.SclEndCustomerData;
import com.scl.core.model.SclCustomerModel;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.b2b.services.B2BCustomerService;
import de.hybris.platform.commercefacades.user.data.CustomerData;

public interface SclEndCustomerService extends B2BCustomerService<B2BCustomerModel, B2BUnitModel> {
	String saveEndCustomerData(SclEndCustomerData sclEndCustomerData);

	SclEndCustomerData getRegisteredEndCustomer();
	
	SearchPageData<SclCustomerModel> getDealersList(SearchPageData searchPageData, String brand, String state,String district,String city,String pincode, String influencerType, String counterType);
	
	SearchPageData<SclCustomerModel> getInfluencersList(SearchPageData searchPageData, String InfluencerType, String brand, String state,String district,String city,String pincode);
	List<ProductModel> getProductsForBrand(String brand);
	
	CustomerData getCustomerByContactNo(String mobileNumber, String smsLoginOtp);
}
