package com.eydms.facades.customer;

import java.util.List;

import com.eydms.facades.data.EndCustomerComplaintData;
import com.eydms.facades.data.ProductsListData;
import com.eydms.facades.data.EyDmsEndCustomerData;
import com.eydms.facades.data.EyDmsEndCustomerDealerData;

import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

public interface EyDmsEndCustomerFacade extends CustomerFacade {

	String saveEndCustomerData(EyDmsEndCustomerData eydmsEndCustomerData);
	
	EyDmsEndCustomerData getRegisteredEndCustomer();
	
	SearchPageData<EyDmsEndCustomerDealerData> getDealersList(SearchPageData searchPageData, String brand, String state,String district,String city,String pincode, String influencerType, String counterType);
	
	SearchPageData<EyDmsEndCustomerDealerData> getPagniatedInfluencersList(SearchPageData searchPageData, String InfluencerType, String brand, String state,String district,String city,String pincode);
	
	ProductsListData getProductsForBrand(String brand);
	
	CustomerData getCustomerByContactNo(String mobileNumber, String smsLoginOtp);
}
