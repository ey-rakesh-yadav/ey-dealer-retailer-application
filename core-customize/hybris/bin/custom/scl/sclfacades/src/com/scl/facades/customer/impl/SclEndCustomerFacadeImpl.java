package com.scl.facades.customer.impl;

import com.scl.facades.data.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.facades.customer.SclCustomerFacade;
import com.scl.facades.customer.SclEndCustomerFacade;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.AddressModel;
import com.scl.core.model.*;
import com.scl.core.services.TerritoryManagementService;
import com.scl.core.constants.SclCoreConstants;
import com.scl.core.customer.services.SclEndCustomerService;

import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.b2bacceleratorfacades.customer.impl.DefaultB2BCustomerFacade;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commercefacades.user.data.AddressData;

import javax.annotation.Resource;

public class SclEndCustomerFacadeImpl extends DefaultB2BCustomerFacade implements SclEndCustomerFacade {
	
	@Autowired
	SclEndCustomerService sclEndCustomerService;
	
	@Resource
    private UserService userService;
	
	@Resource
	TerritoryManagementService territoryManagementService;
	
	@Resource
    Converter<AddressModel, SCLAddressData> sclAddressConverter;
	
	Converter<AddressModel, AddressData> addressConverter;
		
	private static final Logger LOGGER = Logger.getLogger(SclEndCustomerFacadeImpl.class);
	
	@Override
	public String saveEndCustomerData(SclEndCustomerData sclEndCustomerData) {
		return sclEndCustomerService.saveEndCustomerData(sclEndCustomerData);
	}

	@Override
	public SclEndCustomerData getRegisteredEndCustomer() {
		return sclEndCustomerService.getRegisteredEndCustomer();
	}
	
	@Override
	public SearchPageData<SclEndCustomerDealerData> getDealersList(SearchPageData searchPageData, String brand, String state,String district,String city,String pincode, String influencerType, String counterType){
		List<SclEndCustomerDealerData> dataList = new ArrayList<>();
		SearchPageData<SclCustomerModel> customerModelList = sclEndCustomerService.getDealersList(searchPageData, brand, state,district,city,pincode, influencerType, counterType);
        if (customerModelList != null && customerModelList.getResults() != null) {
            List<SclCustomerModel> searchResultList = customerModelList.getResults();
            dataList = getEndCustomerListData(searchResultList);
        }
        final SearchPageData<SclEndCustomerDealerData> result = new SearchPageData<>();
        result.setPagination(customerModelList.getPagination());
        result.setSorts(customerModelList.getSorts());
        result.setResults(dataList);
        LOGGER.info(result.getResults());
		return result;
	}
	
	
	@Override
	public SearchPageData<SclEndCustomerDealerData> getPagniatedInfluencersList(SearchPageData searchPageData, String InfluencerType, String brand, String state,String district,String city,String pincode) {
		SearchPageData<SclCustomerModel> searchResult = null;
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();

		/*
		 * if(currentUser instanceof SclUserModel) { searchResult =
		 * territoryManagementService.getCustomerByTerritoriesAndCounterType(
		 * searchPageData, "EndCustomer", brandType, true, district, city, pincode); }
		 * if(currentUser instanceof SclCustomerModel){
		 * if((currentUser.getGroups().contains(userService.getUserGroupForUID(
		 * SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))){ searchResult =
		 * territoryManagementService.getInfluencerListForDealerPagination(
		 * searchPageData, brandType, true, district, city, pincode); } }
		 */
        SearchPageData<SclCustomerModel> customerModelList = sclEndCustomerService.getInfluencersList(searchPageData, InfluencerType, brand, state,district,city,pincode);
        List<SclEndCustomerDealerData> dataList = new ArrayList<>();
        if (customerModelList != null && customerModelList.getResults() != null) {
            List<SclCustomerModel> searchResultList = customerModelList.getResults();
            dataList = getEndCustomerListData(searchResultList);
        }
        final SearchPageData<SclEndCustomerDealerData> result = new SearchPageData<>();
        result.setPagination(customerModelList.getPagination());
        result.setSorts(customerModelList.getSorts());
        result.setResults(dataList);
        LOGGER.info(result.getResults());
        return result;
	}
	
	/*
	 * @Override public SearchPageData<SclEndCustomerDealerData>
	 * getPagniatedInfluencersList(String brandType, String influencerType, String
	 * state, String district, String city, String pincode, SearchPageData
	 * searchPageData) { SearchPageData<SclCustomerModel> searchResult = null;
	 * B2BCustomerModel currentUser = (B2BCustomerModel)
	 * userService.getCurrentUser();
	 * 
	 * if(currentUser instanceof SclUserModel) { searchResult =
	 * territoryManagementService.getCustomerByTerritoriesAndCounterType(
	 * searchPageData, "EndCustomer", brandType, true, district, city, pincode); }
	 * if(currentUser instanceof SclCustomerModel){
	 * if((currentUser.getGroups().contains(userService.getUserGroupForUID(
	 * SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))){ searchResult =
	 * territoryManagementService.getInfluencerListForDealerPagination(
	 * searchPageData, brandType, true, district, city, pincode); } }
	 * 
	 * List<SclEndCustomerDealerData> dataList = new ArrayList<>(); if (searchResult
	 * != null && searchResult.getResults() != null) { List<SclCustomerModel>
	 * searchResultList = searchResult.getResults(); dataList =
	 * getEndCustomerListData(searchResultList); } final
	 * SearchPageData<SclEndCustomerDealerData> result = new SearchPageData<>();
	 * result.setPagination(searchResult.getPagination());
	 * result.setSorts(searchResult.getSorts()); result.setResults(dataList);
	 * LOGGER.info(result.getResults()); return result; }
	 */
	
	private List<SclEndCustomerDealerData> getEndCustomerListData(List<SclCustomerModel> customerList) {
		List<SclEndCustomerDealerData> endCustomerDataList = new ArrayList<SclEndCustomerDealerData>();
		for (SclCustomerModel sclCustomer : customerList) {
			SclEndCustomerDealerData sclEndCustomerDealer = new SclEndCustomerDealerData();
			sclEndCustomerDealer.setDealerName(sclCustomer.getName());
			Collection<AddressModel> list = sclCustomer.getAddresses();
			if(CollectionUtils.isNotEmpty(list)) {
				List<AddressModel> billingAddressList = list.stream().filter(a -> a.getBillingAddress()).collect(Collectors.toList());
				if(billingAddressList != null && !billingAddressList.isEmpty()) {
					AddressModel billingAddress = billingAddressList.get(0);
					if(null != billingAddress)
					{
						sclEndCustomerDealer.setDealerAddress((sclAddressConverter.convert(billingAddress)));
					}
				}
			}
			
			String brand = sclCustomer.getDefaultB2BUnit().getUid();
			if (brand != null && (brand.toLowerCase().contains("shree") || brand.toLowerCase().contains("102") || sclCustomer.getIsShreeSite())) {
				sclEndCustomerDealer.setDealerBrand("102");
			}
			if (brand != null && (brand.toLowerCase().contains("bangur") || brand.toLowerCase().contains("103") || sclCustomer.getIsBangurSite())) {
				sclEndCustomerDealer.setDealerBrand("103");
			}
			if (brand != null && (brand.toLowerCase().contains("rockstrong") || brand.toLowerCase().contains("104") || sclCustomer.getIsRockstrongSite())) {
				sclEndCustomerDealer.setDealerBrand("104");
			}
			sclEndCustomerDealer.setDealerContactNumber(sclCustomer.getMobileNumber());
			sclEndCustomerDealer.setLatitude(sclCustomer.getLatitude());
			sclEndCustomerDealer.setLongitude(sclCustomer.getLongitude());
			endCustomerDataList.add(sclEndCustomerDealer);
		}
		return endCustomerDataList;
	}
	
	@Override
	public ProductsListData getProductsForBrand(String brand) {
		ProductsListData products = new ProductsListData();
		List<ProductModel> productModels = sclEndCustomerService.getProductsForBrand(brand);
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
        return sclEndCustomerService.getCustomerByContactNo(mobileNumber, smsLoginOtp);
	}

	public Converter<AddressModel, AddressData> getAddressConverter() {
		return addressConverter;
	}

	public void setAddressConverter(Converter<AddressModel, AddressData> addressConverter) {
		this.addressConverter = addressConverter;
	}
}
