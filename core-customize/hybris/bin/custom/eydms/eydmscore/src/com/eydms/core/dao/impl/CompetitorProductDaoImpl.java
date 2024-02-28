package com.eydms.core.dao.impl;

import com.eydms.core.dao.CompetitorProductDao;
import com.eydms.core.model.CompetitorProductModel;
import de.hybris.platform.core.model.c2l.BrandModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.*;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class CompetitorProductDaoImpl extends DefaultGenericDao<CompetitorProductModel> implements CompetitorProductDao {

    public CompetitorProductDaoImpl() {
        super(CompetitorProductModel._TYPECODE);
    }

    @Override
    public CompetitorProductModel findCompetitorProductById(final String otherProductId, BrandModel brandId, List<String> states) {
        final Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("brandId",brandId);
        attr.put("states",states);
        attr.put("otherProductId",otherProductId);
        final StringBuilder sql = new StringBuilder();
        sql.append("Select {pk} from {CompetitorProduct} where {state} IN (?states) and {brand}=?brandId and {code}=?otherProductId");
        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(CompetitorProductModel.class));
        query.getQueryParameters().putAll(attr);
        final SearchResult<CompetitorProductModel> result = getFlexibleSearchService().search(query);
        return result.getResult() !=null && !result.getResult().isEmpty()? result.getResult().get(0) : null;
    }

    @Override
    public List<CompetitorProductModel> getCompetitorProductsByState(BrandModel brandId, List<String> states) {
        final Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("brandId",brandId);
        attr.put("states",states);
        final StringBuilder sql = new StringBuilder();
        sql.append("Select {pk} from {CompetitorProduct} where {state} IN (?states) and {brand}=?brandId");
        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(CompetitorProductModel.class));
        query.getQueryParameters().putAll(attr);
        final SearchResult<CompetitorProductModel> result = getFlexibleSearchService().search(query);
        return result.getResult();
    }
}
