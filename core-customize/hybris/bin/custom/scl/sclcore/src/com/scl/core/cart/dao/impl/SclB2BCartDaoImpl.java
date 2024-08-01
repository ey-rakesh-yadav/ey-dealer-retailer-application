package com.scl.core.cart.dao.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import java.util.*;

import com.scl.core.cart.dao.SclB2BCartDao;

import com.scl.core.enums.OrderType;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;
import org.apache.commons.lang.StringUtils;


public class SclB2BCartDaoImpl implements SclB2BCartDao {
	
	private PaginatedFlexibleSearchService paginatedFlexibleSearchService;
	
	@Override
	public SearchPageData<CartModel> getSavedCartsBySavedBy(UserModel user, SearchPageData searchPageData, String filter, Date startDate, Date endDate, String productName, OrderType orderType) {
		validateParameterNotNull(user, "Customer must not be null");
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {c.pk} from {Cart AS c ");
		if(Objects.nonNull(filter))
		{
			builder.append("JOIN B2BCustomer AS u ON {u.pk} = {c.user} LEFT JOIN WAREHOUSE as w on {w:pk} = {c:destination}");
		}
		builder.append("} WHERE {c.savedBy} = ?currentUser AND {c.date} BETWEEN ?startDate AND ?endDate ");
		params.put("currentUser", user);
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		if(Objects.nonNull(filter))
		{
			builder.append("AND (UPPER({w:code}) like ?filter OR UPPER({u.name}) LIKE ?filter OR UPPER({u.uid}) LIKE ?filter)");
			params.put("filter", "%"+filter.toUpperCase()+"%");
		}
		if(null != productName) {
			List<String> productList = Arrays.asList(productName.split(","));
			if (productList != null && !productList.isEmpty()) {
				builder.append(" and {c:productName} in (?productList) ");
				params.put("productList", productList);
			}
		}
		if(null!= orderType){
			builder.append(" AND {c.orderType} = ?orderType ");
			params.put("orderType",orderType);
		}
		builder.append("Order By {c.saveTime} DESC");
		final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        parameter.setFlexibleSearchQuery(query);
        return getPaginatedFlexibleSearchService().search(parameter);
	}

	public PaginatedFlexibleSearchService getPaginatedFlexibleSearchService() {
		return paginatedFlexibleSearchService;
	}

	public void setPaginatedFlexibleSearchService(PaginatedFlexibleSearchService paginatedFlexibleSearchService) {
		this.paginatedFlexibleSearchService = paginatedFlexibleSearchService;
	}

}
