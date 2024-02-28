package com.eydms.core.dao.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.dao.CounterRouteMappingDao;
import com.eydms.core.model.CounterRouteMappingModel;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.model.SubAreaMasterModel;
import com.eydms.core.services.TerritoryManagementService;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.model.SubAreaMasterModel;
import com.eydms.core.services.TerritoryManagementService;

public class CounterRouteMappingDaoImpl extends DefaultGenericDao<CounterRouteMappingModel> implements CounterRouteMappingDao {

	public CounterRouteMappingDaoImpl() {
		super(CounterRouteMappingModel._TYPECODE);
	}
	
	@Autowired
	TerritoryManagementService territoryManagementService;

	//To be Checked
	@Override
	public List<EyDmsCustomerModel> findCounterByEmployeeAndTalukaAndRoute(String employee, List<String> subAreas, String route, BaseSiteModel brand) {
		final StringBuilder builder = new StringBuilder("SELECT distinct {c.pk} FROM {CounterRouteMapping as cr join EyDmsCustomer as c on {c.uid}={cr.counterCode} } where {cr.employeeCode}=?employeeCode and {cr.route}=?route and {cr.brand}=?brand ")  ;

		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("employeeCode", employee);
		params.put("route", route);
		params.put("brand", brand);
		if(subAreas!=null && !subAreas.isEmpty()) {
			builder.append(" and {cr.taluka} in (?subAreas)");
			params.put("subAreas", subAreas);
		}
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(EyDmsCustomerModel.class));
		query.addQueryParameters(params);
		final SearchResult<EyDmsCustomerModel> searchResult = getFlexibleSearchService().search(query);
		List<EyDmsCustomerModel> result = searchResult.getResult();
		return (result!=null && !result.isEmpty()) ? result : Collections.emptyList();
	}
	
	@Override
	public List<EyDmsCustomerModel> findCounterBySubAreaIdAndRoute(String subAreaMasterId, String route, BaseSiteModel brand) {
		SubAreaMasterModel subArea = territoryManagementService.getTerritoryById(subAreaMasterId);
		if(subArea!=null) {
			final StringBuilder builder = new StringBuilder("SELECT distinct {c.pk} FROM {CounterRouteMapping as cr join EyDmsCustomer as c on {c.uid}={cr.counterCode} join CustomerSubAreaMapping as m on {m.eydmsCustomer}={c.pk}} where {cr.district}=?district and {cr.taluka}=?taluka and {cr.route}=?route and {cr.brand}=?brand ")  ;
			String district = subArea.getDistrict();
			String taluka = subArea.getTaluka();
			if(subArea.getDistrictMaster()!=null) {
				district = subArea.getDistrictMaster().getName();
			}
			final Map<String, Object> params = new HashMap<String, Object>();
			params.put("district", district);
			params.put("taluka", taluka);
			params.put("route", route);
			params.put("brand", brand);
			//builder.append(" order by {c.creationTime} DESC ");
			FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
			query.setResultClassList(Arrays.asList(EyDmsCustomerModel.class));
			query.addQueryParameters(params);
			final SearchResult<EyDmsCustomerModel> searchResult = getFlexibleSearchService().search(query);
			List<EyDmsCustomerModel> result = searchResult.getResult();
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


}
