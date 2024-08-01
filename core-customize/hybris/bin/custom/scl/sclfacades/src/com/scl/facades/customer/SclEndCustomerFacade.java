package com.scl.facades.customer;

import java.util.List;

import com.scl.facades.data.EndCustomerComplaintData;
import com.scl.facades.data.ProductsListData;
import com.scl.facades.data.SclEndCustomerData;
import com.scl.facades.data.SclEndCustomerDealerData;

import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

public interface SclEndCustomerFacade extends CustomerFacade {

	String saveEndCustomerData(SclEndCustomerData sclEndCustomerData);
	
	SclEndCustomerData getRegisteredEndCustomer();
	
	SearchPageData<SclEndCustomerDealerData> getDealersList(SearchPageData searchPageData, String brand, String state,String district,String city,String pincode, String influencerType, String counterType);
	
	SearchPageData<SclEndCustomerDealerData> getPagniatedInfluencersList(SearchPageData searchPageData, String InfluencerType, String brand, String state,String district,String city,String pincode);
	
	ProductsListData getProductsForBrand(String brand);
	
	CustomerData getCustomerByContactNo(String mobileNumber, String smsLoginOtp);
}
