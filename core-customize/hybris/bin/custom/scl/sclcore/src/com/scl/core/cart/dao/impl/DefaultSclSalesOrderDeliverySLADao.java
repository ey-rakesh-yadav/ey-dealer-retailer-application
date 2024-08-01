package com.scl.core.cart.dao.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.scl.core.model.DestinationSourceMasterModel;
import com.scl.core.order.impl.DefaultSCLB2BOrderService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import org.apache.commons.collections.CollectionUtils;

import com.scl.core.cart.dao.SclSalesOrderDeliverySLADao;
import com.scl.core.model.SalesOrderDeliverySLAModel;

import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.apache.log4j.Logger;

public class DefaultSclSalesOrderDeliverySLADao extends AbstractItemDao implements SclSalesOrderDeliverySLADao{

    private static final Logger LOG = Logger.getLogger(DefaultSclSalesOrderDeliverySLADao.class);
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

    /**
     * @param routeId
     * @return
     */
    @Override
    public DestinationSourceMasterModel getDeliverySlaHour(String routeId) {
        validateParameterNotNull(routeId, "routeId cannot be null");
        final Map<String, String> params = new HashMap<>();
        final StringBuilder builder = new StringBuilder("Select DISTINCT({pk}) from {DestinationSourceMaster} where {route}=?routeId and {distance} IS NOT NULL and {plantDispatchSlaHour} IS NOT NULL");
        params.put("routeId", routeId);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(DestinationSourceMasterModel.class));
        query.addQueryParameters(params);
        LOG.info(String.format("getDeliverySlaHour from rank table Query :%s ",query));
        final SearchResult<DestinationSourceMasterModel> searchResult = flexibleSearchService.search(query);
        if (CollectionUtils.isNotEmpty(searchResult.getResult()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) :null;
        else
            return null;
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