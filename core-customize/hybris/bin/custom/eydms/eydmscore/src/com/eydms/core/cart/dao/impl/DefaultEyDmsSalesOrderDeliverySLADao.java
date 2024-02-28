package com.eydms.core.cart.dao.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.eydms.core.cart.dao.EyDmsSalesOrderDeliverySLADao;
import com.eydms.core.model.SalesOrderDeliverySLAModel;

import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

public class DefaultEyDmsSalesOrderDeliverySLADao extends AbstractItemDao implements EyDmsSalesOrderDeliverySLADao{

    private FlexibleSearchService flexibleSearchService;

//    private static final String PRODUCT = "product";
//    private static final String DESTINATION = "destination";
//    private static final String SOURCE = "source";
//    private static final String BRAND = "brand";
//
//    private static final String FIND_SALES_ORDER_DELIVERY_SLA = "SELECT {"+SalesOrderDeliverySLAModel.PK+"} FROM {"+SalesOrderDeliverySLAModel._TYPECODE+
//            "} WHERE {"+SalesOrderDeliverySLAModel.PRODUCT+"} = ?product AND {"+SalesOrderDeliverySLAModel.DESTINATION+"} = ?destination AND {"+
//            SalesOrderDeliverySLAModel.BRAND+"} = ?brand AND {"+SalesOrderDeliverySLAModel.SOURCE+"} = ?source";

    private static final String FIND_SALES_ORDER_DELIVERY_SLA_BY_ROUTE = "SELECT {"+SalesOrderDeliverySLAModel.PK+"} FROM {"+SalesOrderDeliverySLAModel._TYPECODE+
            "} WHERE {"+SalesOrderDeliverySLAModel.ROUTE+"} = ?route ";
//    @Override
//    public SalesOrderDeliverySLAModel findSalesOrderDeliverySLA(BaseSiteModel brand, WarehouseModel source, ERPCityModel destination, ProductModel product) {
//        validateParameterNotNull(source, "source cannot be null");
//        validateParameterNotNull(destination, "destination cannot be null");
//        validateParameterNotNull(product, "product cannot be null");
//        final Map<String, Object> params = new HashMap<String, Object>(4);
//        params.put(BRAND, brand);
//        params.put(SOURCE, source);
//        params.put(DESTINATION, destination);
//        params.put(PRODUCT, product);
//
//
//        final SearchResult<SalesOrderDeliverySLAModel> result = getFlexibleSearchService().search(FIND_SALES_ORDER_DELIVERY_SLA,params);
//        if(CollectionUtils.isNotEmpty(result.getResult()) && result.getResult().size()>0){
//            return result.getResult().get(0);
//        }
//        else {
//            return null;
//        }
//
//    }
//    
    @Override
    public SalesOrderDeliverySLAModel findByRoute(String routeId) {
        validateParameterNotNull(routeId, "routeId cannot be null");
        final Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("route", routeId);
        final SearchResult<SalesOrderDeliverySLAModel> result = getFlexibleSearchService().search(FIND_SALES_ORDER_DELIVERY_SLA_BY_ROUTE,params);
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