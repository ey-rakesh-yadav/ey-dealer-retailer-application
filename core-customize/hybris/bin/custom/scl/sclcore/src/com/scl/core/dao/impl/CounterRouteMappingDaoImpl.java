package com.scl.core.dao.impl;

import java.util.*;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.DataConstraintDao;
import com.scl.core.enums.CounterType;
import de.hybris.platform.enumeration.EnumerationService;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.dao.CounterRouteMappingDao;
import com.scl.core.model.CounterRouteMappingModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.SubAreaMasterModel;
import com.scl.core.services.TerritoryManagementService;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.model.SubAreaMasterModel;
import com.scl.core.services.TerritoryManagementService;




public class CounterRouteMappingDaoImpl extends DefaultGenericDao<CounterRouteMappingModel> implements CounterRouteMappingDao {

	public CounterRouteMappingDaoImpl() {
		super(CounterRouteMappingModel._TYPECODE);
	}
	
	@Autowired
	TerritoryManagementService territoryManagementService;
	@Autowired
	DataConstraintDao dataConstraintDao;

	@Autowired
	EnumerationService enumerationService;

	//To be Checked
	@Override
	public List<SclCustomerModel> findCounterByEmployeeAndTalukaAndRoute(String employee, List<String> subAreas, String route, BaseSiteModel brand) {
		final StringBuilder builder = new StringBuilder("SELECT distinct {c.pk} FROM {CounterRouteMapping as cr join SclCustomer as c on {c.uid}={cr.counterCode} } where {cr.employeeCode}=?employeeCode and {cr.route}=?route and {cr.brand}=?brand ")  ;

		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("employeeCode", employee);
		params.put("route", route);
		params.put("brand", brand);
		if(subAreas!=null && !subAreas.isEmpty()) {
			builder.append(" and {cr.taluka} in (?subAreas)");
			params.put("subAreas", subAreas);
		}
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(SclCustomerModel.class));
		query.addQueryParameters(params);
		final SearchResult<SclCustomerModel> searchResult = getFlexibleSearchService().search(query);
		List<SclCustomerModel> result = searchResult.getResult();
		return (result!=null && !result.isEmpty()) ? result : Collections.emptyList();
	}
	
	@Override
	public List<SclCustomerModel> findCounterBySubAreaIdAndRoute(String subAreaMasterId, String route, BaseSiteModel brand) {
		SubAreaMasterModel subArea = territoryManagementService.getTerritoryById(subAreaMasterId);
		if(subArea!=null) {
			final Map<String, Object> params = new HashMap<String, Object>();
			final StringBuilder builder = new StringBuilder("SELECT distinct {c.pk},{c.creationTime} FROM {CounterRouteMapping as cr join SclCustomer as c on {c.uid}={cr.counterCode} join CustomerSubAreaMapping as m on {m.sclCustomer}={c.pk} } where {cr.district}=?district and {cr.taluka}=?taluka and {cr.route}=?route and {cr.brand}=?brand  ")  ;
			String enbleInfuencerSite = dataConstraintDao.findVersionByConstraintName(SclCoreConstants.CUSTOMER.EXCLUDE_INFLUENCER_SITE);
			if(BooleanUtils.isTrue(Boolean.valueOf(enbleInfuencerSite))) {
				builder.append(" and {c.counterType} not in (?counterList) ");
				List<CounterType>  counterTypes=new ArrayList<>();
				counterTypes.add(enumerationService.getEnumerationValue(CounterType.class,SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_TYPE));
				counterTypes.add( enumerationService.getEnumerationValue(CounterType.class,SclCoreConstants.CUSTOMER.SITE_USER_GROUP_TYPE));
				params.put("counterList",counterTypes);
			}
			String district = subArea.getDistrict();
			String taluka = subArea.getTaluka();
			if(subArea.getDistrictMaster()!=null) {
				district = subArea.getDistrictMaster().getName();
			}

			params.put("district", district);
			params.put("taluka", taluka);
			params.put("route", route);
			params.put("brand", brand);
			builder.append(" order by {c.creationTime} DESC ");
			FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
			query.setResultClassList(Arrays.asList(SclCustomerModel.class));
			query.addQueryParameters(params);
			final SearchResult<SclCustomerModel> searchResult = getFlexibleSearchService().search(query);
			List<SclCustomerModel> result = searchResult.getResult();
			return (result!=null && !result.isEmpty()) ? result : Collections.emptyList();
		}
		return Collections.emptyList();
	}

	//To be Checked
	@Override
	public List<List<Object>> findRouteByEmployeeAndTaluka(String employee, List<String> subAreas, BaseSiteModel brand) {
		final StringBuilder builder = new StringBuilder("SELECT distinct {cr.route}, {cr.routeName} FROM {CounterRouteMapping as cr} where {cr.employeeCode}=?employeeCode and {cr.brand}=?brand "); 
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("employeeCode", employee);
		params.put("brand", brand);
		if(subAreas!=null && !subAreas.isEmpty()) {
			builder.append(" and {cr.taluka} in (?subAreas)");
			params.put("subAreas", subAreas);
		}
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class, String.class));
		query.addQueryParameters(params);
		final SearchResult<List<Object>> searchResult = getFlexibleSearchService().search(query);
		List<List<Object>> result = searchResult.getResult();
		return (result!=null && !result.isEmpty()) ? result : Collections.emptyList();
	}

	@Override
	public CounterRouteMappingModel findCounterByCustomerId(String id){

		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {CounterRouteMapping} where {counterCode}=?id");
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", id);

		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(CounterRouteMappingModel.class));
		query.addQueryParameters(params);
		final SearchResult<CounterRouteMappingModel> searchResult = getFlexibleSearchService().search(query);
		List<CounterRouteMappingModel> result = searchResult.getResult();
		return (result!=null && !result.isEmpty()) ? result.get(0) : null;
	}


}
