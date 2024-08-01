package com.scl.core.dao;

import java.util.List;

import com.scl.core.model.CounterRouteMappingModel;
import com.scl.core.model.SclCustomerModel;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;

public interface CounterRouteMappingDao {

	List<SclCustomerModel> findCounterByEmployeeAndTalukaAndRoute(String employee, List<String> subAreas, String route,
			BaseSiteModel brand);
	
	List<List<Object>> findRouteByEmployeeAndTaluka(String employee, List<String> subArea, BaseSiteModel brand);

	List<SclCustomerModel> findCounterBySubAreaIdAndRoute(String subAreaMasterId, String route, BaseSiteModel brand);

	CounterRouteMappingModel findCounterByCustomerId(String id);
}
