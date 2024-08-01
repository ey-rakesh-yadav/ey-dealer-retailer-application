package com.scl.core.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.dao.DjpCounterScoreDao;
import com.scl.core.dao.DjpRouteScoreDao;
import com.scl.core.dao.ObjectiveDao;
import com.scl.core.model.DJPCounterScoreMasterModel;
import com.scl.core.model.DJPRouteScoreMasterModel;
import com.scl.core.model.ObjectiveModel;

import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;

public class DjpCounterScoreDaoImpl extends DefaultGenericDao<DJPCounterScoreMasterModel> implements DjpCounterScoreDao {

	@Autowired
	DjpRouteScoreDao djpRouteScoreDao;
	
	@Autowired
	ObjectiveDao objectiveDao;
	
	public DjpCounterScoreDaoImpl() {
		super(DJPCounterScoreMasterModel._TYPECODE);
	}

	@Override
	public List<DJPCounterScoreMasterModel> findCounterByRouteAndObjective(String routeScoreId, String objectiveId) {
		DJPRouteScoreMasterModel djpRoute = djpRouteScoreDao.findByRouteScoreId(routeScoreId);
		if(djpRoute==null)
		{
			throw new ModelNotFoundException(String.format("DJPRouteScoreMasterModel not found for id %s", routeScoreId));
			
		}
		ObjectiveModel objective = objectiveDao.findByObjectiveId(objectiveId);
		if(objective==null) {
			throw new ModelNotFoundException(String.format("ObjectiveModel not found for id %s", objectiveId));
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(DJPCounterScoreMasterModel.ROUTESCORE, djpRoute);
		map.put(DJPCounterScoreMasterModel.OBJECTIVE, objective);
		final List<DJPCounterScoreMasterModel> djpCounterList = this.find(map);
		return djpCounterList;
	}

	@Override
	public List<DJPCounterScoreMasterModel> findCounterByRoute(String routeScoreId) {
		DJPRouteScoreMasterModel djpRoute = djpRouteScoreDao.findByRouteScoreId(routeScoreId);
		if(djpRoute==null)
		{
			throw new ModelNotFoundException(String.format("DJPRouteScoreMasterModel not found for id %s", routeScoreId));
			
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(DJPCounterScoreMasterModel.ROUTESCORE, djpRoute);
		final List<DJPCounterScoreMasterModel> djpCounterList = this.find(map);
		return djpCounterList;
	}
}
