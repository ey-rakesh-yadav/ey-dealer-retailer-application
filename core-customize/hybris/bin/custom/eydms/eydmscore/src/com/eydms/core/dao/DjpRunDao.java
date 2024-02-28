package com.eydms.core.dao;

import com.eydms.core.model.DJPRunMasterModel;

public interface DjpRunDao {

	DJPRunMasterModel findByPlannedDateAndUser(String plannedDate, String district, String taluka, String brand);

}
	