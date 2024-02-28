package com.eydms.core.dao.impl;

import com.eydms.core.dao.EYDMSMastersDao;
import com.eydms.core.enums.OrderType;
import com.eydms.core.model.*;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

public class DefaultEYDMSMastersDao extends AbstractItemDao implements EYDMSMastersDao {


    private static final String FIND_DESTINATION_SOURCE_MASTER_BY_SOURCE_AND_BASESITE = "SELECT {"+DestinationSourceMasterModel.PK+"} FROM {"+DestinationSourceMasterModel._TYPECODE+
            "} WHERE {"+DestinationSourceMasterModel.SOURCE+"} = ?source AND {"+DestinationSourceMasterModel.BRAND+"} = ?brand and {city}=?city and {deliveryMode}=?deliveryMode and {orderType}=?orderType ";

    private static final String FIND_VEHICLE_DETAILS_FOR_DEALER = "SELECT {"+ DealerVehicleDetailsModel.PK+"} FROM {"+DealerVehicleDetailsModel._TYPECODE+
            "} WHERE {"+DealerVehicleDetailsModel.DEALER+"} = ?dealer";

    private static final String FIND_DRIVERS_DETAILS_FOR_DEALER = "SELECT {"+ DealerDriverDetailsModel.PK+"} FROM {"+DealerDriverDetailsModel._TYPECODE+
            "} WHERE {"+DealerDriverDetailsModel.DEALER+"} = ?dealer";


    @Override
    public DestinationSourceMasterModel findDestinationSourceMasterForSourceAndBrand(final WarehouseModel source , final BaseSiteModel brand, ERPCityModel erpCity, OrderType orderType, DeliveryModeModel deliveryMode){

//        validateParameterNotNull(source, "source  must not be null");
//        validateParameterNotNull(brand, "brand  must not be null");
//
//        final Map<String, Object> queryParams = new HashMap<String, Object>();
//        queryParams.put("source", source);
//        queryParams.put("brand", brand);
//        queryParams.put("city", erpCity);
//        queryParams.put("deliveryMode", deliveryMode);
//        queryParams.put("orderType", orderType);
//
//        final SearchResult<DestinationSourceMasterModel> result = getFlexibleSearchService().search(FIND_DESTINATION_SOURCE_MASTER_BY_SOURCE_AND_BASESITE, queryParams);
//        if(CollectionUtils.isNotEmpty(result.getResult()) && result.getResult().size()>0){
//            return result.getResult().get(0);
//        }
        return null;
        
    }


}