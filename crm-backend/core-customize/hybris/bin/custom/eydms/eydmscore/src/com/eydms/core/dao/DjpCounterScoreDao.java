package com.eydms.core.dao;

import java.util.List;

import com.eydms.core.model.DJPCounterScoreMasterModel;

public interface DjpCounterScoreDao {

	List<DJPCounterScoreMasterModel> findCounterByRouteAndObjective(String routeScoreId, String objectiveId);

	List<DJPCounterScoreMasterModel> findCounterByRoute(String routeScoreId);

}
