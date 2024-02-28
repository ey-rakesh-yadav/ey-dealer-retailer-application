package com.eydms.core.dao.impl;

import com.eydms.core.dao.RetailerSalesSummaryDao;
import com.eydms.core.model.RetailerSalesSummaryModel;
import com.eydms.core.services.TerritoryManagementService;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.*;

public class RetailerSalesSummaryDaoImpl implements RetailerSalesSummaryDao {
    @Resource
    FlexibleSearchService flexibleSearchService;

    @Autowired
    private PaginatedFlexibleSearchService paginatedFlexibleSearchService;

    @Autowired
    TerritoryManagementService territoryManagementService;

    @Override
    public SearchPageData<RetailerSalesSummaryModel> findRetailerSalesBySubareaAndMonthAndYear(String subArea, Date startDate, Date endDate, SearchPageData searchPageData, String filter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {RetailerSalesSummary} where {startDate}>=?startDate and {endDate}<=?endDate and {subArea}=?subArea ");
        if (Objects.nonNull(filter)) {
            builder.append(" and UPPER({dealerCode}) like ?filter");
        }
        builder.append(" ORDER BY {id}");
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        if(Objects.nonNull(filter))
        {
            params.put("filter","%"+filter.toUpperCase()+"%");
        }
        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(RetailerSalesSummaryModel.class));
        query.getQueryParameters().putAll(params);
        parameter.setFlexibleSearchQuery(query);
        return paginatedFlexibleSearchService.search(parameter);
        /*final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select distinct{dealerCode} from {RetailerSalesSummary} where {startDate}>=?startDate and {endDate}<=?endDate and {subArea}=?subArea");
        if (Objects.nonNull(filter)) {
            builder.append(" and UPPER({dealerCode}) like ?filter");
        }
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("subArea",territoryManagementService.getTerritoryById(subArea));
        if(Objects.nonNull(filter))
        {
            params.put("filter","%"+filter.toUpperCase()+"%");
        }
        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(String.class));
        query.getQueryParameters().putAll(params);
        parameter.setFlexibleSearchQuery(query);
        return paginatedFlexibleSearchService.search(parameter);*/
    }

    public FlexibleSearchService getFlexibleSearchService() {
        return flexibleSearchService;
    }

    public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }
}
