package com.eydms.core.region.dao;

import com.eydms.core.model.StateModel;

import java.util.List;


public interface StateDao {

	public List<StateModel> findAllState();
	
	public List<StateModel> findStateByCode(String state);
}
