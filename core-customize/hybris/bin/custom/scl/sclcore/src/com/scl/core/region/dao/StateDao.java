package com.scl.core.region.dao;

import com.scl.core.model.StateModel;

import java.util.List;


public interface StateDao {

	public List<StateModel> findAllState();
	
	public List<StateModel> findStateByCode(String state);
}
