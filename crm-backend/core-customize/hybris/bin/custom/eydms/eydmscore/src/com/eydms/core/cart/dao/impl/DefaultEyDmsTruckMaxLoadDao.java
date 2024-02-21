package com.eydms.core.cart.dao.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.eydms.core.cart.dao.EyDmsTruckMaxLoadDao;
import com.eydms.core.model.TerritoryUnitModel;
import com.eydms.core.model.TruckMaXLoadModel;

import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;

public class DefaultEyDmsTruckMaxLoadDao extends AbstractItemDao  implements EyDmsTruckMaxLoadDao{

	@Override
	public Integer findTruckMaxLoadSize(WarehouseModel source, TerritoryUnitModel city) {
		validateParameterNotNull(source, "source cannot be null");
		validateParameterNotNull(city, "city cannot be null");
		final Map<String, Object> params = new HashMap<String, Object>(2);
		params.put("source", source);
		params.put("city", city);
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT MAX({o:").append(TruckMaXLoadModel.TRUCKMAXLOAD).append("} from { ").append(TruckMaXLoadModel._TYPECODE).append(" as o} WHERE {o:source} = ?source ")
				.append(" and {o:city} = ?city");
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Arrays.asList(Integer.class));
		query.getQueryParameters().putAll(params);
		final SearchResult<Integer> result = getFlexibleSearchService().search(query);
		return result.getResult().get(0);
		
	}

}
