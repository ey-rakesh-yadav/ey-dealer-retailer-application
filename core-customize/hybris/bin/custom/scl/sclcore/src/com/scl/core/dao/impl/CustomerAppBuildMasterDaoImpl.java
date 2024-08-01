package com.scl.core.dao.impl;

import com.scl.core.dao.CustomerAppBuildMasterDao;
import com.scl.core.model.CustomerAppBuildMasterModel;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class CustomerAppBuildMasterDaoImpl extends DefaultGenericDao<CustomerAppBuildMasterModel> implements CustomerAppBuildMasterDao  {

    @Autowired
    FlexibleSearchService flexibleSearchService;

    public CustomerAppBuildMasterDaoImpl(){super(CustomerAppBuildMasterModel._TYPECODE); }

    @Override
    public CustomerAppBuildMasterModel findStatusByBuildAndVersionNo(String buildNumber, String versionNumber) {
        validateParameterNotNullStandardMessage("buildNumber", buildNumber);
        validateParameterNotNullStandardMessage("versionNumber", versionNumber);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(CustomerAppBuildMasterModel.BUILDNUMBER, buildNumber);
        map.put(CustomerAppBuildMasterModel.VERSIONNUMBER, versionNumber);

        final List<CustomerAppBuildMasterModel> appBuildList = this.find(map);
        if(appBuildList!=null && !appBuildList.isEmpty())
            return appBuildList.get(0);
        else {
            throw new ModelNotFoundException(String.format("App not found with build number %s and version Number %s", buildNumber, versionNumber));
        }
    }

    @Override
    public List<CustomerAppBuildMasterModel> findApplicationVersionByNo(int buildNumber) {
        Map<String, Object> params = new HashMap<String, Object>();

        final StringBuilder builder = new StringBuilder("select * from {CustomerAppBuildMaster}  where cast({buildNumber} as int) >?buildNumber and {status} =1 and {forceUpdate} = 1 ");
        params.put("buildNumber", buildNumber);

        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(CustomerAppBuildMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<CustomerAppBuildMasterModel> searchResult = flexibleSearchService.search(query);
        List<CustomerAppBuildMasterModel> result = searchResult.getResult();

        return result!=null && !result.isEmpty() ? result : Collections.emptyList();
    }
}
