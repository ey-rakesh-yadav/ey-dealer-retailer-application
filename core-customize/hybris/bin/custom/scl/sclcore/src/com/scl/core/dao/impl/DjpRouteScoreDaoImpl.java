package com.scl.core.dao.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import java.util.Collections;
import java.util.List;

import com.scl.core.dao.DjpRouteScoreDao;
import com.scl.core.model.DJPRouteScoreMasterModel;

import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;

public class DjpRouteScoreDaoImpl extends DefaultGenericDao<DJPRouteScoreMasterModel> implements DjpRouteScoreDao {

	public DjpRouteScoreDaoImpl() {
		super(DJPRouteScoreMasterModel._TYPECODE);
	}

	@Override
	public DJPRouteScoreMasterModel findByRouteScoreId(String routeScoreId) {
        validateParameterNotNullStandardMessage("routeScoreId", routeScoreId);
        final List<DJPRouteScoreMasterModel> routeScoreList = this.find(Collections.singletonMap(DJPRouteScoreMasterModel.ID, routeScoreId));
        if (routeScoreList.size() > 1)
        {
            throw new AmbiguousIdentifierException(
                    String.format("Found %d routeScores with the routeScoreId value: '%s', which should be unique", routeScoreList.size(),
                    		routeScoreId));
        }
        else
        {
            return routeScoreList.isEmpty() ? null : routeScoreList.get(0);
        }
    }
}
