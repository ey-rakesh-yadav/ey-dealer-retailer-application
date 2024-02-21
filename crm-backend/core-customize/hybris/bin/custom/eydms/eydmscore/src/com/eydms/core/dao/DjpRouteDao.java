package com.eydms.core.dao;

import java.util.List;

import com.eydms.core.model.RouteMasterModel;

public interface DjpRouteDao {

    public RouteMasterModel findRouteById(final String routeId);
    public List<String> getListOfRoutes();
}
