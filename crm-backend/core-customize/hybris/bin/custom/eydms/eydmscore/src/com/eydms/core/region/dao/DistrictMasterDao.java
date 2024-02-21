package com.eydms.core.region.dao;

import com.eydms.core.model.DistrictMasterModel;

import java.util.List;

public interface DistrictMasterDao {

    DistrictMasterModel findByCode(String districtCode);

    DistrictMasterModel getDistrictByCodeInLocalView(String districtCode);

    List<DistrictMasterModel> getDistrictsForTsmInLocalView();
}
