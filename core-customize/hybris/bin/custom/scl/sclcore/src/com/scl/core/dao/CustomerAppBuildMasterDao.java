package com.scl.core.dao;

import com.scl.core.model.CustomerAppBuildMasterModel;

import java.util.List;

public interface CustomerAppBuildMasterDao {

    CustomerAppBuildMasterModel findStatusByBuildAndVersionNo(String buildNumber, String versionNumber);

    List<CustomerAppBuildMasterModel> findApplicationVersionByNo(int buildNumber);
}
