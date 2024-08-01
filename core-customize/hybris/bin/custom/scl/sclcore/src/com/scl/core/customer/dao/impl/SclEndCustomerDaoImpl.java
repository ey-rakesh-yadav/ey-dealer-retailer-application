package com.scl.core.customer.dao.impl;

import java.util.*;

import javax.annotation.Resource;

import com.scl.core.enums.OTPStatus;
import com.scl.core.model.CustomerGeneratedOtpModel;
import com.scl.core.model.PartnerCustomerModel;
import de.hybris.platform.enumeration.EnumerationService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.model.SclCustomerModel;
import com.scl.core.customer.dao.SclEndCustomerDao;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;

public class SclEndCustomerDaoImpl implements SclEndCustomerDao {

	private static final Logger LOGGER = Logger.getLogger(SclEndCustomerDaoImpl.class);
	
	@Resource
	private FlexibleSearchService flexibleSearchService;
	
	@Resource
    PaginatedFlexibleSearchService paginatedFlexibleSearchService;

	@Autowired
	EnumerationService enumerationService;
	
	@Override
	public SclCustomerModel getRegisteredEndCustomer(String userUid) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {SclCustomer} where {mobilenumber} =?userUid ");
		params.put("userUid", userUid);
		//params.put("isRegistered", Boolean.TRUE);
		
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Arrays.asList(SclCustomerModel.class));
		final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
		if (!searchResult.getResult().isEmpty() && searchResult.getResult()!=null) 
			return searchResult.getResult().get(0);
		return null;
	}
	
	@Override
	public List<ProductModel> getProductsForBrand(String brand){
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {p.pk} from {Product as p join Catalog as c on {c.pk}={p.catalog} "
				+ " join CatalogVersion as cv on {cv.pk}={p.catalogversion} "
				+ " join ArticleApprovalStatus as aas on  {p.approvalstatus}={aas.pk} "
				+ " and {cv.version}='Online' and {aas.codelowercase}='approved' "
				+ " and {c.id}=?brand}");
		if (brand != null && (brand.toLowerCase().contains("shree") || brand.toLowerCase().contains("102"))) {
			params.put("brand", "102ProductCatalog");
		}
		if (brand != null && (brand.toLowerCase().contains("bangur") || brand.toLowerCase().contains("103"))) {
			params.put("brand", "103ProductCatalog");
		}
		if (brand != null && (brand.toLowerCase().contains("rockstrong") || brand.toLowerCase().contains("104"))) {
			params.put("brand", "104ProductCatalog");
		}
		LOGGER.info(" getProductsForBrand: Query [" + builder.toString() + "] to get products for the brand--" + brand + " params value -->" + params.get("brand"));
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Collections.singletonList(ProductModel.class));
		final SearchResult<ProductModel> searchResult = flexibleSearchService.search(query);
		LOGGER.info(" getProductsForBrand: Before Product list count --->>" + searchResult.getResult().size());
		if (searchResult.getResult()!=null && !(searchResult.getResult().isEmpty())) 
			return searchResult.getResult();
		LOGGER.info(" getProductsForBrand: Product list count --->>" + searchResult.getResult().size());
		return null;
	}
	
	@Override
	public B2BCustomerModel getEndCustomerDetails(String mobileNo) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {B2BCustomer} where {mobileNumber} =?mobileNo ");
		params.put("mobileNo", mobileNo);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Arrays.asList(B2BCustomerModel.class));
		final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
		LOGGER.info("End Customer Details ::" + searchResult.getResult().get(0));
		return (searchResult.getResult() !=null && !searchResult.getResult().isEmpty())? searchResult.getResult().get(0) : null;
	}
	
	@Override
    public SearchPageData<SclCustomerModel> getAllInfluncersForStateDistrict(SearchPageData searchPageData, String InfluencerType, String site, String state, String district, String city, String pincode) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select distinct {sc.pk} from {SCLCustomer as sc JOIN Address as add on {sc:pk}={add:owner}} where {add.billingAddress}=1 and {add.state}=?state and {add.district}=?district and {add.city}=?city and {add.pincode}=?pincode");
        
        boolean active = Boolean.TRUE;
        params.put("brand", site);
        params.put("state", state);
        params.put("district", district);
        params.put("city", city);
        params.put("pincode", pincode);
        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(SclCustomerModel.class));
        query.getQueryParameters().putAll(params);
        parameter.setFlexibleSearchQuery(query);
        return paginatedFlexibleSearchService.search(parameter);
    }

	/**
     * @param customerModel
     * @param selectedPartner
     * @return
     */
	@Override
	public List<CustomerGeneratedOtpModel> fetchGeneratedOtpForCustomer(B2BCustomerModel customerModel, PartnerCustomerModel selectedPartner) {

		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {CustomerGeneratedOtp} where {otpStatus}=?otpStatus");

		if(Objects.nonNull(customerModel)){
			builder.append(" and {customer} =?customer");
			params.put("customer", customerModel);
		} else if (Objects.nonNull(selectedPartner)) {
			builder.append(" and {partnerCustomer} =?selectedPartner");
			params.put("selectedPartner", selectedPartner);
		}

		params.put("otpStatus",OTPStatus.GENERATED);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.addQueryParameters(params);
		query.setResultClassList(Arrays.asList(CustomerGeneratedOtpModel.class));
		final SearchResult<CustomerGeneratedOtpModel> searchResult = flexibleSearchService.search(query);
		LOGGER.info(String.format("fetch Generated Otp For Customer ::%s",query));
		return (searchResult !=null && CollectionUtils.isNotEmpty(searchResult.getResult()))? searchResult.getResult() : Collections.EMPTY_LIST;
	}
}
