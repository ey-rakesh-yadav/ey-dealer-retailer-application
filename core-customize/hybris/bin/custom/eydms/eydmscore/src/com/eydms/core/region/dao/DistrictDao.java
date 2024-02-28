package com.eydms.core.region.dao;

import com.eydms.core.model.DistrictModel;
import com.eydms.core.model.StateModel;

import java.util.List;

public interface DistrictDao {

	public List<DistrictModel> findDistrictByState(StateModel state);
	
	public List<DistrictModel> findDistrictByCode(String district);
}
