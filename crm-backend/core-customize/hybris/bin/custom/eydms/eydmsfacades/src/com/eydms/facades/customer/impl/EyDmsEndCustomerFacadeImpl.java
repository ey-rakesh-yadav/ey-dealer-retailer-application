package com.eydms.facades.customer.impl;

import com.eydms.facades.data.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.facades.customer.EyDmsCustomerFacade;
import com.eydms.facades.customer.EyDmsEndCustomerFacade;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.AddressModel;
import com.eydms.core.model.*;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.customer.services.EyDmsEndCustomerService;

import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.b2bacceleratorfacades.customer.impl.DefaultB2BCustomerFacade;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commercefacades.user.data.AddressData;

import javax.annotation.Resource;

public class EyDmsEndCustomerFacadeImpl extends DefaultB2BCustomerFacade implements EyDmsEndCustomerFacade {
	
	@Autowired
	EyDmsEndCustomerService eydmsEndCustomerService;
	
	@Resource
    private UserService userService;
	
	@Resource
	TerritoryManagementService territoryManagementService;
	
	@Resource
    Converter<AddressModel, EYDMSAddressData> eydmsAddressConverter;
	
	Converter<AddressModel, AddressData> addressConverter;
		
	private static final Logger LOGGER = Logger.getLogger(EyDmsEndCustomerFacadeImpl.class);
	
	@Override
	public String saveEndCustomerData(EyDmsEndCustomerData eydmsEndCustomerData) {
		return eydmsEndCustomerService.saveEndCustomerData(eydmsEndCustomerData);
	}

	@Override
	public EyDmsEndCustomerData getRegisteredEndCustomer() {
		return eydmsEndCustomerService.getRegisteredEndCustomer();
	}
	
	@Override
	public SearchPageData<EyDmsEndCustomerDealerData> getDealersList(SearchPageData searchPageData, String brand, String state,String district,String city,String pincode, String influencerType, String counterType){
		List<EyDmsEndCustomerDealerData> dataList = new ArrayList<>();
		SearchPageData<EyDmsCustomerModel> customerModelList = eydmsEndCustomerService.getDealersList(searchPageData, brand, state,district,city,pincode, influencerType, counterType);
        if (customerModelList != null && customerModelList.getResults() != null) {
            List<EyDmsCustomerModel> searchResultList = customerModelList.getResults();
            dataList = getEndCustomerListData(searchResultList);
        }
        final SearchPageData<EyDmsEndCustomerDealerData> result = new SearchPageData<>();
        result.setPagination(customerModelList.getPagination());
        result.setSorts(customerModelList.getSorts());
        result.setResults(dataList);
        LOGGER.info(result.getResults());
		return result;
	}
	
	
	@Override
	public SearchPageData<EyDmsEndCustomerDealerData> getPagniatedInfluencersList(SearchPageData searchPageData, String InfluencerType, String brand, String state,String district,String city,String pincode) {
		SearchPageData<EyDmsCustomerModel> searchResult = null;
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();

		/*
		 * if(currentUser instanceof EyDmsUserModel) { searchResult =
		 * territoryManagementService.getCustomerByTerritoriesAndCounterType(
		 * searchPageData, "EndCustomer", brandType, true, district, city, pincode); }
		 * if(currentUser instanceof EyDmsCustomerModel){
		 * if((currentUser.getGroups().contains(userService.getUserGroupForUID(
		 * EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))){ searchResult =
		 * territoryManagementService.getInfluencerListForDealerPagination(
		 * searchPageData, brandType, true, district, city, pincode); } }
		 */
        SearchPageData<EyDmsCustomerModel> customerModelList = eydmsEndCustomerService.getInfluencersList(searchPageData, InfluencerType, brand, state,district,city,pincode);
        List<EyDmsEndCustomerDealerData> dataList = new ArrayList<>();
        if (customerModelList != null && customerModelList.getResults() != null) {
            List<EyDmsCustomerModel> searchResultList = customerModelList.getResults();
            dataList = getEndCustomerListData(searchResultList);
        }
        final SearchPageData<EyDmsEndCustomerDealerData> result = new SearchPageData<>();
        result.setPagination(customerModelList.getPagination());
        result.setSorts(customerModelList.getSorts());
        result.setResults(dataList);
        LOGGER.info(result.getResults());
        return result;
	}
	
	/*
	 * @Override public SearchPageData<EyDmsEndCustomerDealerData>
	 * getPagniatedInfluencersList(String brandType, String influencerType, String
	 * state, String district, String city, String pincode, SearchPageData
	 * searchPageData) { SearchPageData<EyDmsCustomerModel> searchResult = null;
	 * B2BCustomerModel currentUser = (B2BCustomerModel)
	 * userService.getCurrentUser();
	 * 
	 * if(currentUser instanceof EyDmsUserModel) { searchResult =
	 * territoryManagementService.getCustomerByTerritoriesAndCounterType(
	 * searchPageData, "EndCustomer", brandType, true, district, city, pincode); }
	 * if(currentUser instanceof EyDmsCustomerModel){
	 * if((currentUser.getGroups().contains(userService.getUserGroupForUID(
	 * EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))){ searchResult =
	 * territoryManagementService.getInfluencerListForDealerPagination(
	 * searchPageData, brandType, true, district, city, pincode); } }
	 * 
	 * List<EyDmsEndCustomerDealerData> dataList = new ArrayList<>(); if (searchResult
	 * != null && searchResult.getResults() != null) { List<EyDmsCustomerModel>
	 * searchResultList = searchResult.getResults(); dataList =
	 * getEndCustomerListData(searchResultList); } final
	 * SearchPageData<EyDmsEndCustomerDealerData> result = new SearchPageData<>();
	 * result.setPagination(searchResult.getPagination());
	 * result.setSorts(searchResult.getSorts()); result.setResults(dataList);
	 * LOGGER.info(result.getResults()); return result; }
	 */
	
	private List<EyDmsEndCustomerDealerData> getEndCustomerListData(List<EyDmsCustomerModel> customerList) {
		List<EyDmsEndCustomerDealerData> endCustomerDataList = new ArrayList<EyDmsEndCustomerDealerData>();
		for (EyDmsCustomerModel eydmsCustomer : customerList) {
			EyDmsEndCustomerDealerData eydmsEndCustomerDealer = new EyDmsEndCustomerDealerData();
			eydmsEndCustomerDealer.setDealerName(eydmsCustomer.getName());
			Collection<AddressModel> list = eydmsCustomer.getAddresses();
			if(CollectionUtils.isNotEmpty(list)) {
				List<AddressModel> billingAddressList = list.stream().filter(a -> a.getBillingAddress()).collect(Collectors.toList());
				if(billingAddressList != null && !billingAddressList.isEmpty()) {
					AddressModel billingAddress = billingAddressList.get(0);
					if(null != billingAddress)
					{
						eydmsEndCustomerDealer.setDealerAddress((eydmsAddressConverter.convert(billingAddress)));
					}
				}
			}
			
			String brand = eydmsCustomer.getDefaultB2BUnit().getUid();
			if (brand != null && (brand.toLowerCase().contains("shree") || brand.toLowerCase().contains("102") || eydmsCustomer.getIsShreeSite())) {
				eydmsEndCustomerDealer.setDealerBrand("102");
			}
			if (brand != null && (brand.toLowerCase().contains("bangur") || brand.toLowerCase().contains("103") || eydmsCustomer.getIsBangurSite())) {
				eydmsEndCustomerDealer.setDealerBrand("103");
			}
			if (brand != null && (brand.toLowerCase().contains("rockstrong") || brand.toLowerCase().contains("104") || eydmsCustomer.getIsRockstrongSite())) {
				eydmsEndCustomerDealer.setDealerBrand("104");
			}
			eydmsEndCustomerDealer.setDealerContactNumber(eydmsCustomer.getMobileNumber());
			eydmsEndCustomerDealer.setLatitude(eydmsCustomer.getLatitude());
			eydmsEndCustomerDealer.setLongitude(eydmsCustomer.getLongitude());
			endCustomerDataList.add(eydmsEndCustomerDealer);
		}
		return endCustomerDataList;
	}
	
	@Override
	public ProductsListData getProductsForBrand(String brand) {
		ProductsListData products = new ProductsListData();
		List<ProductModel> productModels = eydmsEndCustomerService.getProductsForBrand(brand);
		if (null!= productModels) {
			products = getProductsData(productModels);
		}
		return products;
	}
	
	private ProductsListData getProductsData(List<ProductModel> productModels) {
		ProductsListData productsData = new ProductsListData();
		List<ProductsData> productsDataList = new ArrayList<ProductsData>();
		for (ProductModel productModel: productModels) {
			ProductsData productData = new ProductsData();
			productData.setProductCode(productModel.getCode());
			productData.setProductName(productModel.getName());
			productData.setProductDescription(productModel.getDescription());
			productsDataList.add(productData);
		}
		productsData.setProductsBrand(productsDataList);
		return productsData;
	}
	
	@Override
	public CustomerData getCustomerByContactNo(String mobileNumber, String smsLoginOtp) {        
        return eydmsEndCustomerService.getCustomerByContactNo(mobileNumber, smsLoginOtp);
	}

	public Converter<AddressModel, AddressData> getAddressConverter() {
		return addressConverter;
	}

	public void setAddressConverter(Converter<AddressModel, AddressData> addressConverter) {
		this.addressConverter = addressConverter;
	}
}
