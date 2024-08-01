package com.scl.core.region.dao;

import com.scl.core.model.DistrictMasterModel;

import java.util.List;

public interface DistrictMasterDao {

    DistrictMasterModel findByCode(String districtCode);

    DistrictMasterModel getDistrictByCodeInLocalView(String districtCode);

    List<DistrictMasterModel> getDistrictsForTsmInLocalView();
}
