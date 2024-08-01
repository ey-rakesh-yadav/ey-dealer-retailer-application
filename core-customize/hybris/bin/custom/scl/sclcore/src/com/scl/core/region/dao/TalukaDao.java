package com.scl.core.region.dao;

import com.scl.core.model.DistrictModel;
import com.scl.core.model.TalukaModel;

import java.util.List;

public interface TalukaDao {

	public List<TalukaModel> findTalukaByDistrict(DistrictModel district);
	
	public List<TalukaModel> findTalukaByCode(String taluka);
}
