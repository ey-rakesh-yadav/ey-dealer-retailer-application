package com.eydms.core.region.dao.impl;

import java.util.Collections;
import java.util.List;

import com.eydms.core.model.DistrictModel;
import com.eydms.core.model.TalukaModel;
import com.eydms.core.region.dao.TalukaDao;

import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;

public class TalukaDaoImpl extends DefaultGenericDao<TalukaModel> implements  TalukaDao{

	public TalukaDaoImpl() {
		super(TalukaModel._TYPECODE);
	}
	
	public List<TalukaModel> findTalukaByDistrict(DistrictModel district){
		return this.find(Collections.singletonMap(TalukaModel.DISTRICT, district));
	}
	
	public List<TalukaModel> findTalukaByCode(String taluka){
		return this.find(Collections.singletonMap(TalukaModel.ISOCODE, taluka));
	}
}
