package com.eydms.core.source.dao.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.enums.CustomerCategory;
import com.eydms.core.enums.OrderType;
import com.eydms.core.model.DestinationSourceMasterModel;
import com.eydms.core.source.dao.DestinationSourceMasterDao;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.site.BaseSiteService;

public class DefaultDestinationSourceMasterDao extends DefaultGenericDao<DestinationSourceMasterModel> implements DestinationSourceMasterDao {


	public DefaultDestinationSourceMasterDao() {
		super(DestinationSourceMasterModel._TYPECODE);
	}

	@Autowired
	BaseSiteService baseSiteService;

	@Override
	public List<DestinationSourceMasterModel> findDestinationSourceByCode(String city, DeliveryModeModel deliveryMode, OrderType orderType, CustomerCategory customerCategory, String grade, String packaging, String district, String state, BaseSiteModel brand, String taluka) {

		if(city!= null && district!= null && state!=null) {
			Map<String, Object> map = new HashMap<>();
			map.put(DestinationSourceMasterModel.DELIVERYMODE, deliveryMode);
			map.put(DestinationSourceMasterModel.BRAND, brand);
			map.put(DestinationSourceMasterModel.ORDERTYPE, orderType);
			map.put(DestinationSourceMasterModel.CUSTOMERCATEGORY, customerCategory);
			map.put(DestinationSourceMasterModel.DESTINATIONCITY, city.toUpperCase());
			if(StringUtils.isNotBlank(taluka)) {
				map.put(DestinationSourceMasterModel.DESTINATIONTALUKA, taluka.toUpperCase());
			}
			map.put(DestinationSourceMasterModel.GRADE, grade);
			map.put(DestinationSourceMasterModel.PACKAGING, packaging);
			map.put(DestinationSourceMasterModel.DESTINATIONDISTRICT, district.toUpperCase());
			map.put(DestinationSourceMasterModel.DESTINATIONSTATE, state.toUpperCase());
			String queryResult = "SELECT {ds:pk} from {DestinationSourceMaster as ds} where {ds:brand}=?brand and {ds:customerCategory}=?customerCategory and {ds:deliveryMode}=?deliveryMode and {ds:orderType}=?orderType and UPPER({ds:destinationState})=?destinationState and UPPER({ds:destinationDistrict})=?destinationDistrict " ;
			if(StringUtils.isNotBlank(taluka)) {
				queryResult = queryResult + " and UPPER({ds:destinationTaluka})=?destinationTaluka ";
			}
			queryResult = queryResult + " and UPPER({ds:destinationCity})=?destinationCity and {ds:grade}=?grade and {ds:packaging}=?packaging ";

			final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
			query.getQueryParameters().putAll(map);
			final SearchResult<DestinationSourceMasterModel> result = this.getFlexibleSearchService().search(query);
			return result.getResult();
		}
		return Collections.emptyList();
	}

	@Override
	public List<DestinationSourceMasterModel> findL1Source(String city, DeliveryModeModel deliveryMode, OrderType orderType, CustomerCategory customerCategory, String grade, String packaging, String district, String state, BaseSiteModel brand, String taluka) {
		if(city!= null && district!= null && state!=null) {
			Map<String, Object> map = new HashMap<>();
			map.put(DestinationSourceMasterModel.DELIVERYMODE, deliveryMode);
			map.put(DestinationSourceMasterModel.BRAND, brand);
			map.put(DestinationSourceMasterModel.ORDERTYPE, orderType);
			map.put(DestinationSourceMasterModel.CUSTOMERCATEGORY, customerCategory);
			map.put(DestinationSourceMasterModel.DESTINATIONCITY, city.toUpperCase());
			map.put(DestinationSourceMasterModel.DESTINATIONTALUKA, taluka.toUpperCase());
			map.put(DestinationSourceMasterModel.GRADE, grade);
			map.put(DestinationSourceMasterModel.PACKAGING, packaging);
			map.put(DestinationSourceMasterModel.DESTINATIONDISTRICT, district.toUpperCase());
			map.put(DestinationSourceMasterModel.DESTINATIONSTATE, state.toUpperCase());
			String queryResult = "SELECT {ds:pk} from {DestinationSourceMaster as ds} where {ds:brand}=?brand and {ds:customerCategory}=?customerCategory and {ds:deliveryMode}=?deliveryMode and {ds:orderType}=?orderType and UPPER({ds:destinationState})=?destinationState and UPPER({ds:destinationDistrict})=?destinationDistrict and UPPER({ds:destinationTaluka})=?destinationTaluka " +
					" and UPPER({ds:destinationCity})=?destinationCity and {ds:grade}=?grade and {ds:packaging}=?packaging and {ds.sourcePriority} like '%1' ";

			final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
			query.getQueryParameters().putAll(map);
			final SearchResult<DestinationSourceMasterModel> result = this.getFlexibleSearchService().search(query);
			return result.getResult();
		}
		return Collections.emptyList();

	}

	@Override
	public DestinationSourceMasterModel getDestinationSourceBySource(OrderType orderType,
			CustomerCategory customerCategory, WarehouseModel source, DeliveryModeModel deliveryMode, String city,
			String district, String state, BaseSiteModel brand, String grade, String packaging, String taluka) {
		
		if(city!= null && district!= null && state!=null) {
			Map<String, Object> map = new HashMap<>();
			map.put(DestinationSourceMasterModel.DELIVERYMODE, deliveryMode);
			map.put(DestinationSourceMasterModel.BRAND, brand);
			map.put(DestinationSourceMasterModel.ORDERTYPE, orderType);
			map.put(DestinationSourceMasterModel.CUSTOMERCATEGORY, customerCategory);
			map.put(DestinationSourceMasterModel.DESTINATIONCITY, city.toUpperCase());
			map.put(DestinationSourceMasterModel.GRADE, grade);
			map.put(DestinationSourceMasterModel.PACKAGING, packaging);
			map.put(DestinationSourceMasterModel.DESTINATIONDISTRICT, district.toUpperCase());
			map.put(DestinationSourceMasterModel.DESTINATIONSTATE, state.toUpperCase());
			map.put(DestinationSourceMasterModel.DESTINATIONTALUKA, taluka.toUpperCase());
			map.put(DestinationSourceMasterModel.SOURCE, source);
			
			String queryResult = "SELECT {ds:pk} from {DestinationSourceMaster as ds} where {ds:brand}=?brand and {ds:customerCategory}=?customerCategory and {ds:deliveryMode}=?deliveryMode and {ds:orderType}=?orderType and UPPER({ds:destinationState})=UPPER(?destinationState) and UPPER({ds:destinationDistrict})=UPPER(?destinationDistrict) and UPPER({ds:destinationTaluka})=UPPER(?destinationTaluka) " +
					" and UPPER({ds:destinationCity})=UPPER(?destinationCity) and {ds:grade}=?grade and {ds:packaging}=?packaging and {ds.source}=?source";
			
			final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
			query.getQueryParameters().putAll(map);
			final SearchResult<DestinationSourceMasterModel> result = this.getFlexibleSearchService().search(query);
			return result.getResult()!=null && !result.getResult().isEmpty()? result.getResult().get(0) : new DestinationSourceMasterModel();
		}
		return new DestinationSourceMasterModel();
	}    
}
