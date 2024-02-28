package com.eydms.core.dao;

import java.util.Date;

import com.eydms.core.model.EyDmsUserModel;
import com.eydms.core.model.VisitMasterModel;

public interface VisitMasterDao {

	VisitMasterModel findById(String visitId);

	VisitMasterModel findByRouteAndDate(final EyDmsUserModel eydmsUserModel, Date date);

	VisitMasterModel findVisitMasterByIdInLocalView(String visitMasterId);
}
