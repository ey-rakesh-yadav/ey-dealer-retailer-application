package com.eydms.core.dao;

import java.util.List;

import com.eydms.core.model.ObjectiveModel;

public interface ObjectiveDao {

	ObjectiveModel findByObjectiveId(String objectiveId);

	List<ObjectiveModel> findAllObjective();

}
