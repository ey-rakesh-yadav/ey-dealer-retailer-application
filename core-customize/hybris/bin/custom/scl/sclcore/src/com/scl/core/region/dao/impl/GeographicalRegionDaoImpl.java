package com.scl.core.region.dao.impl;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.DataConstraintDao;
import com.scl.core.dao.SCLProductDao;
import com.scl.core.model.*;
import com.scl.core.region.dao.GeographicalRegionDao;

import com.scl.core.utility.SclDateUtility;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.odata2services.odata.RuntimeIOException;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

public class GeographicalRegionDaoImpl implements GeographicalRegionDao{

	public static final String LAST_USED_MONTHS = "LAST_USED_MONTHS";
	Logger LOG=Logger.getLogger(GeographicalRegionDaoImpl.class);
	public static final String DEALER_RETAILER_CHECK_PINCODE = "DEALER_RETAILER_CHECK_PINCODE";
	public static final String DEALER_RETAILER_CHECK_TALUKA = "DEALER_RETAILER_CHECK_TALUKA";
	public static final String DEALER_RETAILER_CHECK_CITY = "DEALER_RETAILER_CHECK_CITY";
	public static final String DEALER_RETAILER_CHECK = "DEALER_RETAILER_CHECK";
	@Resource
	FlexibleSearchService flexibleSearchService;
	
	@Autowired
	BaseSiteService baseSiteService;
	
	@Autowired
	UserService userService;
	@Autowired
	private DataConstraintDao dataConstraintDao;

	@Autowired
	private SCLProductDao sclProductDao;

	@Override
	public List<String> findAllState() {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT {geographicalState} FROM {GeographicalMaster} WHERE {fromDate}<=?currentDate AND {toDate}>=?currentDate");
		Date currentDate=new Date();
		params.put("currentDate",currentDate);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		query.addQueryParameters(params);
		final SearchResult<String> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<String> findAllDistrict(String state) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT {district} FROM {GeographicalMaster} WHERE {geographicalState}=?state AND {fromDate}<=?currentDate AND {toDate}>=?currentDate");
		Date currentDate=new Date();
		params.put("state", state);
		params.put("currentDate",currentDate);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		query.addQueryParameters(params);
		final SearchResult<String> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<String> findAllTaluka(String state, String district) {
		final Map<String, Object> params = new HashMap<String, Object>();
		Date currentDate=new Date();
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT {taluka} FROM {GeographicalMaster} WHERE {geographicalState}=?state AND {district}=?district AND {fromDate}<=?currentDate AND {toDate}>=?currentDate");
		params.put("state", state);
		params.put("district", district);
		params.put("currentDate",currentDate);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		query.addQueryParameters(params);
		final SearchResult<String> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<String> findAllErpCity(String state, String district, String taluka) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT {erpCity} FROM {GeographicalMaster} WHERE {geographicalState}=?state AND {district}=?district AND {taluka}=?taluka AND {fromDate}<=?currentDate AND {toDate}>=?currentDate");
		Date currentDate=new Date();
		params.put("state", state);
		params.put("district", district);
		params.put("taluka", taluka);
		params.put("currentDate",currentDate);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		query.addQueryParameters(params);
		final SearchResult<String> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<String> findAllErpCity(String state, String district) {
		final Map<String, Object> params = new HashMap<String, Object>();
		Date currentDate=new Date();
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT {erpCity} FROM {GeographicalMaster} WHERE {geographicalState}=?state AND {district}=?district AND {fromDate}<=?currentDate AND {toDate}>=?currentDate");
		params.put("state", state);
		params.put("district", district);
		params.put("currentDate",currentDate);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		query.addQueryParameters(params);
		final SearchResult<String> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}
	
	@Override
	public List<String> findAllErpCityByDistrictCode(String district) {
		final Map<String, Object> params = new HashMap<String, Object>();
		Date currentDate=new Date();
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT {erpCity} FROM {GeographicalMaster} WHERE  {district}=?district AND {fromDate}<=?currentDate AND {toDate}>=?currentDate");
		params.put("district", district);
		params.put("currentDate",currentDate);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		query.addQueryParameters(params);
		final SearchResult<String> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<GeographicalMasterModel> getGeographyByPincode(String pincode) {
		final Map<String, Object> params = new HashMap<String, Object>();
		Date currentDate=new Date();
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT {pk} FROM {GeographicalMaster} WHERE  {pincode}=?pincode AND {fromDate}<=?currentDate AND {toDate}>=?currentDate");
		params.put("pincode", pincode);
		params.put("currentDate",currentDate);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(GeographicalMasterModel.class));
		query.addQueryParameters(params);
		final SearchResult<GeographicalMasterModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public  List<GeographicalMasterModel> getAllGeographyMasters(){
		final Map<String, Object> params = new HashMap<String, Object>();
		Date currentDate=new Date();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {GeographicalMaster} where {transportationZone} is not null AND {fromDate}<=?currentDate AND {toDate}>=?currentDate");
		params.put("currentDate",currentDate);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(List.of(GeographicalMasterModel.class));
		query.addQueryParameters(params);
		final SearchResult<GeographicalMasterModel> searchResult = flexibleSearchService.search(query);
		return CollectionUtils.isNotEmpty(searchResult.getResult())? searchResult.getResult().stream().filter(g-> (null!=g.getFromDate() && null!= g.getToDate() && g.getToDate().after(new Date()) && g.getFromDate().before(new Date()))).collect(Collectors.toList()):null;

	}

	@Override
	public  GeographicalMasterModel getGeographyMasterForTransZone(String transportationZone){
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {GeographicalMaster} where {transportationZone}=?transportationZone AND {fromDate}<=?currentDate AND {toDate}>=?currentDate");
		Date currentDate=new Date();
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("transportationZone", transportationZone);
		params.put("currentDate",currentDate);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(List.of(GeographicalMasterModel.class));
		query.addQueryParameters(params);
		final SearchResult<GeographicalMasterModel> searchResult = flexibleSearchService.search(query);
		List<GeographicalMasterModel> validTransportationZones = searchResult.getResult().stream().filter(g-> (null!=g.getFromDate() && null!= g.getToDate() && g.getToDate().after(new Date()) && g.getFromDate().before(new Date()))).collect(Collectors.toList());
		return CollectionUtils.isNotEmpty(validTransportationZones) && validTransportationZones.size()==1 ? validTransportationZones.get(0):null;
	}

	@Override
	public List<String> getBusinessState(String geographicalState, String district, String taluka, String erpCity) {
		final Map<String, Object> params = new HashMap<String, Object>();
		Date currentDate=new Date();
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT {state} FROM {GeographicalMaster} WHERE {geographicalState}=?geographicalState AND {district}=?district AND {taluka}=?taluka AND {erpCity}=?erpCity AND {fromDate}<=?currentDate AND {toDate}>=?currentDate ");
		params.put("geographicalState", geographicalState);
		params.put("district", district);
		params.put("taluka", taluka);
		params.put("erpCity", erpCity);
		params.put("currentDate",currentDate);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		query.addQueryParameters(params);
		final SearchResult<String> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<String> getGeographicalStateByGoogleMapState(String googleMapState) {
		final Map<String, Object> params = new HashMap<String, Object>();
		Date currentDate=new Date();
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT {geographicalState} FROM {GeographicalMaster} WHERE {googleMapState}=?googleMapState AND {fromDate}<=?currentDate AND {toDate}>=?currentDate ");
		params.put("googleMapState", googleMapState);
		params.put("currentDate",currentDate);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		query.addQueryParameters(params);
		final SearchResult<String> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}
	@Override
	public List<String> getStateByGSTState(String gstState) {
		final Map<String, Object> params = new HashMap<String, Object>();
		Date currentDate=new Date();
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT {state} FROM {GeographicalMaster} WHERE {gstState}=?gstState AND {fromDate}<=?currentDate AND {toDate}>=?currentDate");
		params.put("gstState", gstState);
		params.put("currentDate",currentDate);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		query.addQueryParameters(params);
		final SearchResult<String> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	@Override
	public List<String> findAllLpSourceErpCity(String dealerId,String retailerUid,String state, String district, String taluka) {
		final Map<String, Object> params = new HashMap<String, Object>();
		SclCustomerModel sclCustomer=(SclCustomerModel)userService.getUserForUID(dealerId);
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT {d.destinationCity} FROM {DestinationSourceMaster as d " + "join Product as p on {d.productCode}={p.code} and {d.customerCategory}={p.custCategory}");
		Integer dealerRetailerCheckCity = dataConstraintDao.findDaysByConstraintName(DEALER_RETAILER_CHECK_CITY);
		if(Objects.nonNull(dealerRetailerCheckCity) && dealerRetailerCheckCity ==1) {
			builder.append("join DealerRetailerMapping as dr on UPPER({dr.taluka})=UPPER({d.destinationTaluka}) and UPPER({dr.state})=UPPER({d.destinationState}) and UPPER({dr.district})=UPPER({d.destinationDistrict}) and UPPER({dr.erpCity})=UPPER({d.destinationCity}) AND {dr.pinCode}={d.pincode}}");
		}
		else{
			builder.append("}");
		}
		builder.append("WHERE" + " " + "UPPER({d.destinationState})=?state"+ " " + "AND UPPER({d.destinationDistrict})=?district" + " " + "AND UPPER({d.destinationTaluka})=?taluka" + " ");
		params.put("state", state.toUpperCase());
		params.put("district", district.toUpperCase());
		params.put("taluka", taluka.toUpperCase());

		if(Objects.nonNull(sclCustomer)) {
			if(dealerRetailerCheckCity ==1) {
				builder.append("AND {dr.dealer}=?dealer");
				params.put("dealer", sclCustomer);
			}
			List<SclBrandModel> sclBrandList = sclProductDao.getNeilsonBrandMapping(sclCustomer);
			if (CollectionUtils.isNotEmpty(sclBrandList)) {
				builder.append(" AND {p.brandCode} IN (?sclBrandList)");
				params.put("sclBrandList", sclBrandList);
			}
		}
		if(StringUtils.isNotBlank(retailerUid)) {
			SclCustomerModel retailer=(SclCustomerModel)userService.getUserForUID(retailerUid);
			if (Objects.nonNull(retailer)) {
				builder.append(" AND {dr.retailer}=?retailer");
				params.put("retailer", retailer);
			}
		}else{
			builder.append(" AND {dr.retailer} IS NULL");
		}
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		query.addQueryParameters(params);
		LOG.info(String.format("find all source erpCity::%s",query));
		final SearchResult<String> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}
	
	@Override
	public List<String> findAllLpSourceTaluka(String state, String district,String dealerId,String retailerUid) {
		final Map<String, Object> params = new HashMap<String, Object>();
		SclCustomerModel sclCustomer=(SclCustomerModel)userService.getUserForUID(dealerId);
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT {d.destinationTaluka} FROM {DestinationSourceMaster as d " + "join Product as p on {d.productCode}={p.code} and {d.customerCategory}={p.custCategory}");
		Integer dealerRetailerCheckTaluka = dataConstraintDao.findDaysByConstraintName(DEALER_RETAILER_CHECK_TALUKA);
		if(Objects.nonNull(dealerRetailerCheckTaluka) && dealerRetailerCheckTaluka ==1) {
			builder.append("join DealerRetailerMapping as dr on UPPER({dr.taluka})=UPPER({d.destinationTaluka}) and UPPER({dr.state})=UPPER({d.destinationState}) and UPPER({dr.district})=UPPER({d.destinationDistrict}) AND UPPER({dr.erpCity})=UPPER({d.destinationCity}) AND {dr.pinCode}={d.pincode}}");
		}
		else{
			builder.append("}");
		}
		builder.append("WHERE" + " " +"UPPER({d.destinationState})=?state" + " " + "AND UPPER({d.destinationDistrict})=?district" + " ");
		params.put("state", state.toUpperCase());
		params.put("district", district.toUpperCase());
		if(Objects.nonNull(sclCustomer)) {
			if(dealerRetailerCheckTaluka ==1) {
				builder.append("AND {dr.dealer}=?dealer ");
				params.put("dealer", sclCustomer);
			}
			List<SclBrandModel> sclBrandList = sclProductDao.getNeilsonBrandMapping(sclCustomer);
			if (CollectionUtils.isNotEmpty(sclBrandList)) {
				builder.append(" AND {p.brandCode} IN (?sclBrandList)");
				params.put("sclBrandList", sclBrandList);
			}
		}
		if(StringUtils.isNotBlank(retailerUid)) {
			SclCustomerModel retailer=(SclCustomerModel)userService.getUserForUID(retailerUid);
			if (Objects.nonNull(retailer)) {
				builder.append(" AND {dr.retailer}=?retailer");
				params.put("retailer", retailer);
			}
		}else{
			builder.append(" AND {dr.retailer} IS NULL");
		}

		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		query.addQueryParameters(params);
		LOG.info(String.format("find all source taluka::%s",query));
		final SearchResult<String> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}
	
	@Override
	public List<String> findAllLpSourceDistrict(String dealerId,String retailerUid, String state) {
		SclCustomerModel dealer=(SclCustomerModel)userService.getUserForUID(dealerId);
		final Map<String, Object> params = new HashMap<String, Object>();
		Integer lastXMonth = dataConstraintDao.findDaysByConstraintName(LAST_USED_MONTHS);

		final StringBuilder builder = 
				new StringBuilder("select DISTINCT ({ds.destinationDistrict}) from {DealerRetailerMapping as dr join DestinationSourceMaster as ds"
						+ " on UPPER({dr.state})=UPPER({ds.destinationState}) and UPPER({dr.district})=UPPER({ds.destinationDistrict}) AND UPPER({dr.taluka})=UPPER({ds.destinationTaluka}) AND UPPER({dr.erpCity})=UPPER({ds.destinationCity}) AND {dr.pinCode}={ds.pincode} join Product as p on {p.code}={ds.productCode} and {ds.customerCategory}={p.custCategory}}"
						+ " where {dr.dealer}=?dealer and UPPER({dr.state})=?state  AND {ds.customerCategory}=?customerCategory");
		//params.put("brand", baseSiteService.getCurrentBaseSite());

		params.put("state", state.toUpperCase());
		if(Objects.nonNull(dealer)) {
			params.put("dealer", dealer);
			params.put("customerCategory", dealer.getCustomerCategory());
			List<SclBrandModel> sclBrandList = sclProductDao.getNeilsonBrandMapping(dealer);
			if (CollectionUtils.isNotEmpty(sclBrandList)) {
				builder.append(" AND {p.brandCode} IN (?sclBrandList)");
				params.put("sclBrandList", sclBrandList);
			}
		}
		if(StringUtils.isNotBlank(retailerUid)) {
			SclCustomerModel retailer=(SclCustomerModel)userService.getUserForUID(retailerUid);
			if (Objects.nonNull(retailer)) {
				builder.append(" AND {dr.retailer}=?retailer");
				params.put("retailer", retailer);
			}
		}else{
			builder.append(" AND {dr.retailer} IS NULL");
		}
		if(null!= lastXMonth){
			builder.append(" AND ")
					.append(SclDateUtility.getLastXMonthQuery("dr.lastUsed", params, lastXMonth));
		}
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		query.addQueryParameters(params);
		LOG.info(String.format("find all source district::%s",query));
		final SearchResult<String> searchResult = flexibleSearchService.search(query);
		return Objects.nonNull(searchResult) && CollectionUtils.isNotEmpty(searchResult.getResult()) ? searchResult.getResult() : Collections.emptyList();
	}	
	
	@Override
	public List<String> findAllLpSourceState() {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT {d.destinationState} FROM {DestinationSourceMaster as d} WHERE {d.brand}=?brand  ");
		params.put("brand", baseSiteService.getCurrentBaseSite());
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		query.addQueryParameters(params);
		LOG.info(String.format("find all source state::%s",query));
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

	/**
	 * @param state
	 * @param district
	 * @param taluka
	 * @param erpCity
	 * @return
	 */
	@Override
	public GeographicalMasterModel fetchGeographicalMaster(String state, String district, String taluka, String erpCity) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {GeographicalMaster} WHERE UPPER({state})=?state AND UPPER({district})=?district AND UPPER({taluka})=?taluka AND UPPER({erpCity})=?erpCity AND {fromDate}<=?currentDate AND {toDate}>=?currentDate");
		Date currentDate = new Date();
		params.put("state", state.toUpperCase());
		params.put("district", district.toUpperCase());
		params.put("taluka", taluka.toUpperCase());
		params.put("erpCity",erpCity.toUpperCase());
		params.put("currentDate",currentDate);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(GeographicalMasterModel.class));
		query.addQueryParameters(params);
		final SearchResult<GeographicalMasterModel> searchResult = flexibleSearchService.search(query);
		return (Objects.nonNull(searchResult.getResult()) && searchResult.getResult().size()>0)?searchResult.getResult().get(0):null;
	}

	/**
	 * @param state
	 * @param district
	 * @param taluka
	 * @param erpCity
	 * @param productCode
	 * @return
	 */
	@Override
	public List<DestinationSourceMasterModel> validateAddressFields(String state, String district, String taluka, String erpCity, String pincode, String productCode, SclCustomerModel dealer) {

		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT({pk}) FROM {DestinationSourceMaster as d" +" "+"join Product as p on {d.productCode}={p.code} and {d.customerCategory}={p.custCategory}");

		Integer dealerRetailerCheck = dataConstraintDao.findDaysByConstraintName(DEALER_RETAILER_CHECK);
		if(Objects.nonNull(dealerRetailerCheck) && dealerRetailerCheck ==1) {
			builder.append("join DealerRetailerMapping as dr on {dr.taluka}= {d.destinationTaluka} and {dr.state}={d.destinationState} and {dr.district}={d.destinationDistrict} and {dr.erpCity}={d.destinationCity} and {dr.pinCode}={d.pincode} }");
		}
		else{
			builder.append("}");
		}
		builder.append(" WHERE" + " " +"{d.destinationState}=?state" + " " + "AND {d.destinationDistrict}=?district" + " "+"AND {d.destinationTaluka}=?taluka" + " " + "AND {d.destinationCity}=?city" + " " + "AND {d.pincode}=?pincode " + " " + "AND {d.productCode}=?productCode" + " ");

		params.put("state", state);
		params.put("district", district);
		params.put("taluka", taluka);
		params.put("city", erpCity);
		params.put("pincode",pincode );
		params.put("productCode",productCode);
		if(Objects.nonNull(dealer)) {
			if(dealerRetailerCheck ==1) {
			builder.append("AND {dr.dealer}=?dealer" + " ");
			params.put("dealer", dealer);
			}
			List<SclBrandModel> sclBrandList = sclProductDao.getNeilsonBrandMapping(dealer);
			if (CollectionUtils.isNotEmpty(sclBrandList)) {
				builder.append("AND {p.brandCode} IN (?sclBrandList)");
				params.put("sclBrandList", sclBrandList);
			}
		}
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(DestinationSourceMasterModel.class));
		query.addQueryParameters(params);
		LOG.info(String.format("validate address details query :: %s",query));
		final SearchResult<DestinationSourceMasterModel> searchResult = flexibleSearchService.search(query);
		return (Objects.nonNull(searchResult.getResult()) && searchResult.getResult().size()>0)?searchResult.getResult():Collections.emptyList();

	}

	/**
	 * @param transportationZone
	 * @return
	 */
	@Override
	public GeographicalMasterModel fetchGeographicalMaster(String transportationZone) {
		final Map<String, Object> params = new HashMap<String, Object>();
		Date currentDate = new Date();
		StringBuilder builder = new StringBuilder("SELECT {pk} FROM {GeographicalMaster} WHERE UPPER({transportationZone})=?transportationZone and {fromDate}<=?currentDate and {toDate}>=?currentDate");
		params.put("transportationZone", transportationZone.toUpperCase());
		params.put("currentDate",currentDate);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(GeographicalMasterModel.class));
		query.addQueryParameters(params);
		LOG.info(String.format("fetchGeographicalMaster for transportationZone query::%s",query));
		final SearchResult<GeographicalMasterModel> searchResult = flexibleSearchService.search(query);
		return (Objects.nonNull(searchResult.getResult()) && searchResult.getResult().size()>0)?searchResult.getResult().get(0):null;
	}

	@Override
	public List<List<Object>> findAllLpSourcePincode(String dealerId,String retailerUid,String state, String district, String taluka, String city) {
		final Map<String, Object> params = new HashMap<String, Object>();
		Date currentDate=new Date();
		SclCustomerModel sclCustomer=(SclCustomerModel)userService.getUserForUID(dealerId);
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT {d.pincode},{d.transportationZone} FROM {DestinationSourceMaster as d " + "join Product as p on {d.productCode}={p.code} and {d.customerCategory}={p.custCategory} ");

		builder.append("join DealerRetailerMapping as dr on {dr.taluka}={d.destinationTaluka} and {dr.state}={d.destinationState} and {dr.district}={d.destinationDistrict} and {dr.erpCity}={d.destinationCity} and {dr.pinCode}={d.pincode} } ");

		builder.append("WHERE" + " " +"{d.destinationState}=?state" + " " + "AND {d.destinationDistrict}=?district" + " " + "AND {d.destinationTaluka}=?taluka" + " " + "AND {d.destinationCity}=?city" + " ");
		params.put("state", state);
		params.put("district", district);
		params.put("taluka", taluka);
		params.put("city", city);
		params.put("currentDate",currentDate);
		if(Objects.nonNull(sclCustomer)) {
				builder.append("AND {dr.dealer}=?dealer");
				params.put("dealer", sclCustomer);

			List<SclBrandModel> sclBrandList = sclProductDao.getNeilsonBrandMapping(sclCustomer);
			if (CollectionUtils.isNotEmpty(sclBrandList)) {
				builder.append(" AND {p.brandCode} IN (?sclBrandList)");
				params.put("sclBrandList", sclBrandList);
			}
		}
		if(StringUtils.isNotBlank(retailerUid)) {
			SclCustomerModel retailer=(SclCustomerModel)userService.getUserForUID(retailerUid);
			if (Objects.nonNull(retailer)) {
				builder.append(" AND {dr.retailer}=?retailer");
				params.put("retailer", retailer);
			}
		}else{
			builder.append(" AND {dr.retailer} IS NULL");
		}
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class, String.class));
		query.addQueryParameters(params);
		LOG.info(String.format("find all lpsourcePincode query :: %s",query));
		SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);;
		return searchResult.getResult();
	}

	/**
	 * @param retailerId
	 * @param dealerId
	 * @return
	 */
	@Override
	public List<String> findAllLpSourceState(String dealerId,String retailerId) {
		LOG.info(String.format("Inside finaAllLpSourceState for dealer::%s and retailer::%s",dealerId,retailerId));

		Integer lastXMonth = dataConstraintDao.findDaysByConstraintName(LAST_USED_MONTHS);
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT({ds.destinationState}) FROM {DestinationSourceMaster as ds join DealerRetailerMapping as dm on UPPER({dm.state})=UPPER({ds.destinationState}) and UPPER({dm.district})=UPPER({ds.destinationDistrict}) AND UPPER({dm.taluka})=UPPER({ds.destinationTaluka}) AND UPPER({dm.erpCity})=UPPER({ds.destinationCity}) AND {dm.pinCode}={ds.pincode} join Product as p on {p.code}={ds.productCode}} WHERE {ds.customerCategory}=?customerCategory AND {dm.dealer}=?dealer ");

		//order for self
		if(Objects.nonNull(dealerId)){
			SclCustomerModel dealer=(SclCustomerModel) userService.getUserForUID(dealerId);
			List<SclBrandModel> sclBrandList=sclProductDao.getNeilsonBrandMapping(dealer);
			if(CollectionUtils.isNotEmpty(sclBrandList)) {
			   builder.append(" AND {p.brandCode} IN (?sclBrandList)");
				params.put("sclBrandList", sclBrandList);
			}
			if(Objects.nonNull(dealer)) {
				params.put("customerCategory", dealer.getCustomerCategory());
				params.put("dealer", dealer);
			}
		}

		if(StringUtils.isNotBlank(retailerId)) {
			SclCustomerModel retailer=(SclCustomerModel)userService.getUserForUID(retailerId);
			if (Objects.nonNull(retailer)) {
				builder.append(" AND {dm.retailer}=?retailer");
				params.put("retailer", retailer);
			}
		}else{
			builder.append(" AND {dm.retailer} IS NULL");
		}
		if(null!= lastXMonth){
			builder.append(" AND ")
					.append(SclDateUtility.getLastXMonthQuery("dm.lastUsed", params, lastXMonth));
		}

		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		query.addQueryParameters(params);
		LOG.info(String.format("find all state query ::%s",query));
		final SearchResult<String> searchResult = flexibleSearchService.search(query);
		return Objects.nonNull(searchResult) && CollectionUtils.isNotEmpty(searchResult.getResult()) ? searchResult.getResult() : Collections.EMPTY_LIST;
	}

	/**
	 * @param state
	 * @param district
	 * @param taluka
	 * @param erpCity
	 * @return
	 */
	@Override
	public List<List<Object>> fetchPincode(String state, String district, String taluka, String erpCity) {

		final Map<String, Object> params = new HashMap<String, Object>();
		Date currentDate=new Date();
		final StringBuilder builder = new StringBuilder("SELECT DISTINCT {pincode},{transportationZone} FROM {GeographicalMaster} WHERE {geographicalState}=?geographicalState AND {district}=?district AND {taluka}=?taluka AND {erpCity}=?erpCity AND {fromDate}<=?currentDate AND {toDate}>=?currentDate ");
		params.put("geographicalState", state);
		params.put("district", district);
		params.put("taluka", taluka);
		params.put("erpCity", erpCity);
		params.put("currentDate",currentDate);
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class, String.class));
		query.addQueryParameters(params);
		LOG.info(String.format("fetch PinCode query :: %s",query));
		final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult();
	}

	/**
	 * @param sclUser
	 * @return
	 */
	@Override
	public List<UserSubAreaMappingModel> getUserSubAreaMappingForUser(SclUserModel sclUser) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {UserSubAreaMapping} WHERE {sclUser} = ?sclUser AND {isActive} = ?active AND {brand}=?brand AND {state} IS NOT NULL ");
		params.put("sclUser", sclUser);
		params.put("active", Boolean.TRUE);
		params.put("brand",baseSiteService.getBaseSiteForUID(SclCoreConstants.SCL_SITE));
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(UserSubAreaMappingModel.class));
		query.addQueryParameters(params);
		final SearchResult<UserSubAreaMappingModel> searchResult = flexibleSearchService.search(query);
		List<UserSubAreaMappingModel> result = searchResult.getResult();
		return result != null && !result.isEmpty() ? result : Collections.emptyList();
	}


	@Override
	public List<TsoTalukaMappingModel> getTsoTalukaMappingForTso(SclUserModel tsoUser) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {TsoTalukaMapping} WHERE {tsoUser} = ?tsoUser AND {isActive} = ?active AND {brand}=?brand AND {state} IS NOT NULL  ");
		params.put("tsoUser", tsoUser);
		params.put("active", Boolean.TRUE);
		params.put("brand",baseSiteService.getCurrentBaseSite());
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(TsoTalukaMappingModel.class));
		query.addQueryParameters(params);
		final SearchResult<TsoTalukaMappingModel> searchResult = flexibleSearchService.search(query);
		List<TsoTalukaMappingModel> result = searchResult.getResult();
		return result != null && !result.isEmpty() ? result : Collections.emptyList();
	}

}
