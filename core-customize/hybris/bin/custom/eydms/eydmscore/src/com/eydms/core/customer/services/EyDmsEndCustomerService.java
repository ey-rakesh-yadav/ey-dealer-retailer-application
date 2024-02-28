package com.eydms.core.customer.services;

import java.util.List;

import com.eydms.facades.data.EyDmsEndCustomerData;
import com.eydms.core.model.EyDmsCustomerModel;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.b2b.services.B2BCustomerService;
import de.hybris.platform.commercefacades.user.data.CustomerData;

public interface EyDmsEndCustomerService extends B2BCustomerService<B2BCustomerModel, B2BUnitModel> {
	String saveEndCustomerData(EyDmsEndCustomerData eydmsEndCustomerData);

	EyDmsEndCustomerData getRegisteredEndCustomer();
	
	SearchPageData<EyDmsCustomerModel> getDealersList(SearchPageData searchPageData, String brand, String state,String district,String city,String pincode, String influencerType, String counterType);
	
	SearchPageData<EyDmsCustomerModel> getInfluencersList(SearchPageData searchPageData, String InfluencerType, String brand, String state,String district,String city,String pincode);
	List<ProductModel> getProductsForBrand(String brand);
	
	CustomerData getCustomerByContactNo(String mobileNumber, String smsLoginOtp);
}
