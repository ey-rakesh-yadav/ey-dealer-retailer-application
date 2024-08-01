package com.scl.core.dao;

import java.util.List;

import com.scl.core.model.ObjectiveModel;

public interface ObjectiveDao {

	ObjectiveModel findByObjectiveId(String objectiveId);

	List<ObjectiveModel> findAllObjective();

}
