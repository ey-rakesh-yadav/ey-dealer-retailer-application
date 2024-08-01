package com.scl.core.customer.dao;

import java.util.List;

import com.scl.core.model.CustomerGeneratedOtpModel;
import com.scl.core.model.PartnerCustomerModel;
import com.scl.core.model.SclCustomerModel;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

public interface SclEndCustomerDao {
	
	SclCustomerModel getRegisteredEndCustomer(String userUid);
	
	List<ProductModel> getProductsForBrand(String brand);
	
	B2BCustomerModel getEndCustomerDetails(String mobileNo);
	
	SearchPageData<SclCustomerModel> getAllInfluncersForStateDistrict(SearchPageData searchPageData, String InfluencerType, String site, String state, String district, String city, String pincode);

	List<CustomerGeneratedOtpModel> fetchGeneratedOtpForCustomer(B2BCustomerModel customerModel, PartnerCustomerModel selectedPartner);

}
