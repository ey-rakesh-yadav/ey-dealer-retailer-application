package com.scl.core.dao;

import com.scl.core.model.CounterVisitMasterModel;

import java.util.Date;
import java.util.List;

public interface CounterVisitMasterDao {

	CounterVisitMasterModel findCounterVisitById(String counterVisitId);
	List<List<Object>> fetchBrandWiseAggregatedData(String counterVisitId);
	CounterVisitMasterModel findCounterVisitByLastVisitDate(Date lastVisitDate);
}
