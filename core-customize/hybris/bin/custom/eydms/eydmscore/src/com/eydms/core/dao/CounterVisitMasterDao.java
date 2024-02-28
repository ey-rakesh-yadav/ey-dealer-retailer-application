package com.eydms.core.dao;

import com.eydms.core.model.CounterVisitMasterModel;

import java.util.Date;
import java.util.List;

public interface CounterVisitMasterDao {

	CounterVisitMasterModel findCounterVisitById(String counterVisitId);
	List<List<Object>> fetchBrandWiseAggregatedData(String counterVisitId);
	CounterVisitMasterModel findCounterVisitByLastVisitDate(Date lastVisitDate);
}
