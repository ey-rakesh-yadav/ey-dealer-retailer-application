package com.scl.core.dao;

import java.util.List;

import com.scl.core.model.DJPCounterScoreMasterModel;

public interface DjpCounterScoreDao {

	List<DJPCounterScoreMasterModel> findCounterByRouteAndObjective(String routeScoreId, String objectiveId);

	List<DJPCounterScoreMasterModel> findCounterByRoute(String routeScoreId);

}
