package com.scl.core.dao;

import java.util.List;

import com.scl.core.model.RouteMasterModel;
import com.scl.core.model.SubAreaMasterModel;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;

public interface RouteMasterDao {

	RouteMasterModel findByRouteId(String routeIds);

	List<RouteMasterModel> findBySubAreaAndBrand(SubAreaMasterModel subAreaMaster, BaseSiteModel brand);

}
