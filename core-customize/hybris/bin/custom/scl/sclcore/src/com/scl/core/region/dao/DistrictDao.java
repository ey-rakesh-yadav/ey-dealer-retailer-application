package com.scl.core.region.dao;

import com.scl.core.model.DistrictModel;
import com.scl.core.model.StateModel;

import java.util.List;

public interface DistrictDao {

	public List<DistrictModel> findDistrictByState(StateModel state);
	
	public List<DistrictModel> findDistrictByCode(String district);
}
