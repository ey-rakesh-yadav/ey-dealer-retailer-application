package com.scl.core.dao;

import com.scl.core.model.DJPRouteScoreMasterModel;

public interface DjpRouteScoreDao {

	DJPRouteScoreMasterModel findByRouteScoreId(String routeScoreId);
}
