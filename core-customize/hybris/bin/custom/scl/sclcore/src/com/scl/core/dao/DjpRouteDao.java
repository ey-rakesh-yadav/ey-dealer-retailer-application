package com.scl.core.dao;

import java.util.List;

import com.scl.core.model.RouteMasterModel;

public interface DjpRouteDao {

    public RouteMasterModel findRouteById(final String routeId);
    public List<String> getListOfRoutes();
}
