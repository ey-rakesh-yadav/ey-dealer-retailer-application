package com.scl.core.dao.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;
import java.util.Collections;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.scl.core.jalo.CustomerAppBuildMaster;
import com.scl.core.model.CustomerAppBuildMasterModel;
import com.scl.core.model.SclCustomerModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.dao.AppBuildMasterDao;
import com.scl.core.dao.DjpRunDao;
import com.scl.core.model.AppBuildMasterModel;
import com.scl.core.model.DJPRunMasterModel;

import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.servicelayer.user.UserService;

public class AppBuildMasterDaoImpl extends DefaultGenericDao<AppBuildMasterModel> implements AppBuildMasterDao {

	@Autowired
	FlexibleSearchService flexibleSearchService;

	public AppBuildMasterDaoImpl() {
		super(AppBuildMasterModel._TYPECODE);
	}

	@Override
	public AppBuildMasterModel findStatusByBuildAndVersionNumber(String buildNumber, String versionNumber) {
		validateParameterNotNullStandardMessage("buildNumber", buildNumber);
		validateParameterNotNullStandardMessage("versionNumber", versionNumber);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(AppBuildMasterModel.BUILDNUMBER, buildNumber);
		map.put(AppBuildMasterModel.VERSIONNUMBER, versionNumber);

		final List<AppBuildMasterModel> appBuildList = this.find(map);
		if(appBuildList!=null && !appBuildList.isEmpty())
			return appBuildList.get(0);
		else {
			throw new ModelNotFoundException(String.format("App not found with build number %s and version Number %s", buildNumber, versionNumber));
		}
	}

	@Override
	public List<AppBuildMasterModel> findApplicationVersionByNumber(int buildNumber) {
		Map<String, Object> params = new HashMap<String, Object>();

		final StringBuilder builder = new StringBuilder("select * from {AppBuildMaster}  where cast({buildNumber} as int) >?buildNumber and {status} =1 and {forceUpdate} = 1 ");
		params.put("buildNumber", buildNumber);

		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(AppBuildMasterModel.class));
		query.addQueryParameters(params);
		final SearchResult<AppBuildMasterModel> searchResult = flexibleSearchService.search(query);
		List<AppBuildMasterModel> result = searchResult.getResult();

		return result!=null && !result.isEmpty() ? result : Collections.emptyList();
	}


}
