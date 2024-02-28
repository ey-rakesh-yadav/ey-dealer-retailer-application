package com.eydms.core.region.dao.impl;

import java.util.Collections;
import java.util.List;

import com.eydms.core.model.StateModel;
import com.eydms.core.region.dao.StateDao;


import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;

public class StateDaoImpl extends DefaultGenericDao<StateModel> implements StateDao {

	public StateDaoImpl() {
		super(StateModel._TYPECODE);
	}

	public List<StateModel> findAllState(){
		return this.find();
	}
	
	public List<StateModel> findStateByCode(String state){
		return this.find(Collections.singletonMap(StateModel.ISOCODE, state));
	}
}
