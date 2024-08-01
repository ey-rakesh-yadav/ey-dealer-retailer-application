package com.scl.core.region.dao;

import com.scl.core.model.RegionMasterModel;

import java.util.List;

public interface RegionMasterDao {

    RegionMasterModel findByCode(String regionCode);

    RegionMasterModel getRegionByCodeInLocalView(String regionCode);

    List<RegionMasterModel> getRegionsForRhInLocalView();
}

