package com.eydms.core.dao;

import com.eydms.core.model.AppBuildMasterModel;
import com.eydms.core.model.CustomerAppBuildMasterModel;

import java.util.List;

public interface AppBuildMasterDao {

	AppBuildMasterModel findStatusByBuildAndVersionNumber(String buildNumber, String versionNumber);

	List<AppBuildMasterModel> findApplicationVersionByNumber(int buildNumber);



}
