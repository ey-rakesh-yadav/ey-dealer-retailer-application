package com.eydms.core.cart.dao.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.eydms.core.cart.dao.EyDmsISODeliverySLADao;
import com.eydms.core.model.ISODeliverySLAModel;

import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

public class DefaultEyDmsISODeliverySLADao extends AbstractItemDao implements EyDmsISODeliverySLADao{

    private FlexibleSearchService flexibleSearchService;

    private static final String FIND_ISO_DELIVERY_SLA_BY_ROUTE = "SELECT {"+ISODeliverySLAModel.PK+"} FROM {"+ISODeliverySLAModel._TYPECODE+
            "} WHERE {"+ISODeliverySLAModel.ROUTE+"} = ?route ";

    @Override
    public ISODeliverySLAModel findByRoute(String routeId) {
        validateParameterNotNull(routeId, "routeId cannot be null");
        final Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("route", routeId);
        final SearchResult<ISODeliverySLAModel> result = getFlexibleSearchService().search(FIND_ISO_DELIVERY_SLA_BY_ROUTE,params);
        if(CollectionUtils.isNotEmpty(result.getResult()) && result.getResult().size()>0){
            return result.getResult().get(0);
        }
        else {
            return null;
        }
    }
    
    @Override
    public FlexibleSearchService getFlexibleSearchService() {
        return flexibleSearchService;
    }

    @Override
    public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }
}