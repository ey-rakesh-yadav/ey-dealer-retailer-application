package com.eydms.core.dao;

import com.eydms.core.model.DJPRouteScoreMasterModel;

public interface DjpRouteScoreDao {

	DJPRouteScoreMasterModel findByRouteScoreId(String routeScoreId);
}
