package com.eydms.core.region.dao;

import com.eydms.core.model.RegionMasterModel;

import java.util.List;

public interface RegionMasterDao {

    RegionMasterModel findByCode(String regionCode);

    RegionMasterModel getRegionByCodeInLocalView(String regionCode);

    List<RegionMasterModel> getRegionsForRhInLocalView();
}

