package com.eydms.core.dao;

import java.util.List;

import com.eydms.core.model.EyDmsCustomerModel;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;

public interface CounterRouteMappingDao {

	List<EyDmsCustomerModel> findCounterByEmployeeAndTalukaAndRoute(String employee, List<String> subAreas, String route,
			BaseSiteModel brand);
	
	List<List<Object>> findRouteByEmployeeAndTaluka(String employee, List<String> subArea, BaseSiteModel brand);

	List<EyDmsCustomerModel> findCounterBySubAreaIdAndRoute(String subAreaMasterId, String route, BaseSiteModel brand);
}
