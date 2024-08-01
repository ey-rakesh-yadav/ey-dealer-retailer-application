package com.scl.core.dao;

import com.scl.core.model.DJPRunMasterModel;

public interface DjpRunDao {

	DJPRunMasterModel findByPlannedDateAndUser(String plannedDate, String district, String taluka, String brand);

}
	