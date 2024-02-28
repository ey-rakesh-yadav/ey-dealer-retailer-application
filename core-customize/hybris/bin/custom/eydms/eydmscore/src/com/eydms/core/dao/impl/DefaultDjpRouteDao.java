package com.eydms.core.dao.impl;

import com.eydms.core.dao.DjpRouteDao;
import com.eydms.core.model.RouteMasterModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class DefaultDjpRouteDao extends DefaultGenericDao<RouteMasterModel> implements DjpRouteDao {

    public DefaultDjpRouteDao() {
        super(RouteMasterModel._TYPECODE);
    }

    @Override
    public RouteMasterModel findRouteById(final String routeId){
        validateParameterNotNullStandardMessage("routeId", routeId);
        final List<RouteMasterModel> routeList = this.find(Collections.singletonMap(RouteMasterModel.ROUTEID, routeId));
        if (routeList.size() > 1)
        {
            throw new AmbiguousIdentifierException(
                    String.format("Found %d route with the routeId value: '%s', which should be unique", routeList.size(),
                            routeId));
        }
        else
        {
            return routeList.isEmpty() ? null : routeList.get(0);
        }

    }

	@Override
	public List<String> getListOfRoutes() {
		final StringBuilder builder = new StringBuilder("SELECT {routeId} FROM {RouteMaster}");
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(String.class));
		final SearchResult<String> searchResult = getFlexibleSearchService().search(query);
		List<String> result = searchResult.getResult();
		return (result!=null && !result.isEmpty()) ? result : Collections.emptyList();
	}
}
