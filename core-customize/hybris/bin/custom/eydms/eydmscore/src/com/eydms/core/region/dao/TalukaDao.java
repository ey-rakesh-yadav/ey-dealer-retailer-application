package com.eydms.core.region.dao;

import com.eydms.core.model.DistrictModel;
import com.eydms.core.model.TalukaModel;

import java.util.List;

public interface TalukaDao {

	public List<TalukaModel> findTalukaByDistrict(DistrictModel district);
	
	public List<TalukaModel> findTalukaByCode(String taluka);
}
