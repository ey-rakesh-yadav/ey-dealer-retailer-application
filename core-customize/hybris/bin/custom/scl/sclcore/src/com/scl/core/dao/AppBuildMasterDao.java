package com.scl.core.dao;

import com.scl.core.model.AppBuildMasterModel;
import com.scl.core.model.CustomerAppBuildMasterModel;

import java.util.List;

public interface AppBuildMasterDao {

	AppBuildMasterModel findStatusByBuildAndVersionNumber(String buildNumber, String versionNumber);

	List<AppBuildMasterModel> findApplicationVersionByNumber(int buildNumber);



}
