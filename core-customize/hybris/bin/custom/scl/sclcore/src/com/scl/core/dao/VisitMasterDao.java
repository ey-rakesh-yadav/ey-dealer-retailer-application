package com.scl.core.dao;

import java.util.Date;

import com.scl.core.model.SclUserModel;
import com.scl.core.model.VisitMasterModel;

public interface VisitMasterDao {

	VisitMasterModel findById(String visitId);

	VisitMasterModel findByRouteAndDate(final SclUserModel sclUserModel, Date date);

	VisitMasterModel findVisitMasterByIdInLocalView(String visitMasterId);
}
