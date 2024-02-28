package com.eydms.core.region.dao.impl;

import com.eydms.core.model.GeographicalMasterModel;
import com.eydms.core.region.dao.GeographicalRegionDao;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeographicalRegionDaoImpl implements GeographicalRegionDao{

	@Resource
	FlexibleSearchService flexibleSearchService;
	
	@Autowired
	BaseSiteService baseSiteService;
	
	@Autowired
	UserService userService;
	
	@Override
	public List<String> findAllState() {
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT {geographicalState} FROM {GeographicalMaster}");
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		final SearchResult<String> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<String> findAllDistrict(String state) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT {district} FROM {GeographicalMaster} WHERE {geographicalState}=?state");
		params.put("state", state);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		query.addQueryParameters(params);
		final SearchResult<String> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<String> findAllTaluka(String state, String district) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT {taluka} FROM {GeographicalMaster} WHERE {geographicalState}=?state AND {district}=?district");
		params.put("state", state);
		params.put("district", district);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		query.addQueryParameters(params);
		final SearchResult<String> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<String> findAllErpCity(String state, String district, String taluka) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT {erpCity} FROM {GeographicalMaster} WHERE {geographicalState}=?state AND {district}=?district AND {taluka}=?taluka");
		params.put("state", state);
		params.put("district", district);
		params.put("taluka", taluka);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		query.addQueryParameters(params);
		final SearchResult<String> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<String> findAllErpCity(String state, String district) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT {erpCity} FROM {GeographicalMaster} WHERE {geographicalState}=?state AND {district}=?district ");
		params.put("state", state);
		params.put("district", district);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		query.addQueryParameters(params);
		final SearchResult<String> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}
	
	@Override
	public List<String> findAllErpCityByDistrictCode(String district) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT {erpCity} FROM {GeographicalMaster} WHERE  {district}=?district");
		params.put("district", district);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		query.addQueryParameters(params);
		final SearchResult<String> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<GeographicalMasterModel> getGeographyByPincode(String pincode) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT {pk} FROM {GeographicalMaster} WHERE  {pincode}=?pincode");
		params.put("pincode", pincode);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(GeographicalMasterModel.class));
		query.addQueryParameters(params);
		final SearchResult<GeographicalMasterModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<String> getBusinessState(String geographicalState, String district, String taluka, String erpCity) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT {state} FROM {GeographicalMaster} WHERE {geographicalState}=?geographicalState AND {district}=?district AND {taluka}=?taluka AND {erpCity}=?erpCity ");
		params.put("geographicalState", geographicalState);
		params.put("district", district);
		params.put("taluka", taluka);
		params.put("erpCity", erpCity);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		query.addQueryParameters(params);
		final SearchResult<String> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<String> getGeographicalStateByGoogleMapState(String googleMapState) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT {geographicalState} FROM {GeographicalMaster} WHERE {googleMapState}=?googleMapState ");
		params.put("googleMapState", googleMapState);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		query.addQueryParameters(params);
		final SearchResult<String> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}
	@Override
	public List<String> getStateByGSTState(String gstState) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT {state} FROM {GeographicalMaster} WHERE {gstState}=?gstState ");
		params.put("gstState", gstState);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		query.addQueryParameters(params);
		final SearchResult<String> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<String> findAllLpSourceErpCity(String state, String district, String taluka) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT {d.destinationCity} FROM {DestinationSourceMaster as d} WHERE {d.brand}=?brand and {d.destinationState}=?state AND {d.destinationDistrict}=?district AND {d.destinationTaluka}=?taluka ");
		params.put("state", state);
		params.put("district", district);
		params.put("taluka", taluka);
		params.put("brand", baseSiteService.getCurrentBaseSite());
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		query.addQueryParameters(params);
		final SearchResult<String> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}
	
	@Override
	public List<String> findAllLpSourceTaluka(String state, String district) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT {d.destinationTaluka} FROM {DestinationSourceMaster as d} WHERE {d.brand}=?brand and {d.destinationState}=?state AND {d.destinationDistrict}=?district ");
		params.put("state", state);
		params.put("district", district);
		params.put("brand", baseSiteService.getCurrentBaseSite());
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		query.addQueryParameters(params);
		final SearchResult<String> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}
	
	@Override
	public List<String> findAllLpSourceDistrict(String state) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT {d.destinationDistrict} FROM {DestinationSourceMaster as d} WHERE {d.brand}=?brand and {d.destinationState}=?state  ");
		params.put("state", state);
		params.put("brand", baseSiteService.getCurrentBaseSite());
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		query.addQueryParameters(params);
		final SearchResult<String> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}	
	
	@Override
	public List<String> findAllLpSourceState() {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT {d.destinationState} FROM {DestinationSourceMaster as d} WHERE {d.brand}=?brand  ");
		params.put("brand", baseSiteService.getCurrentBaseSite());
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		query.addQueryParameters(params);
		final SearchResult<String> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<String> findUserState(String customerCode) {
		List<String> result = new ArrayList<String>();	        
		B2BCustomerModel customer = null;
		if(customerCode!=null) {
			customer = (B2BCustomerModel) userService.getUserForUID(customerCode);
		}
		else {
			customer = (B2BCustomerModel) userService.getCurrentUser();
		}
		result.add(customer.getState());
		return result;
	}
	


}
