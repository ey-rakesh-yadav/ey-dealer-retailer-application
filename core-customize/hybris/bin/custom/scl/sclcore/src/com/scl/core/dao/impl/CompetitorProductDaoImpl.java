package com.scl.core.dao.impl;

import com.scl.core.dao.CompetitorProductDao;
import com.scl.core.model.CompetitorProductModel;
import com.scl.core.model.CounterVisitMasterModel;
import de.hybris.platform.core.model.c2l.BrandModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.apache.log4j.Logger;

import java.util.*;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class CompetitorProductDaoImpl extends DefaultGenericDao<CompetitorProductModel> implements CompetitorProductDao {
    private static final Logger LOG = Logger.getLogger(CompetitorProductDaoImpl.class);
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

    /**
     * Get Selected Competitor Product with Latest Counter Visit
     * @param latestCounterVisit
     * @return
     */
    @Override
    public List<CompetitorProductModel> getCompetitorProductByVisitId(CounterVisitMasterModel latestCounterVisit){
        final Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("latestCounterVisit",latestCounterVisit);
        final StringBuilder sql = new StringBuilder();
        sql.append("select distinct {cp:pk} from {CounterVisitMaster as cvm join MarketMappingDetails as mmd on {mmd.counterVisit}={cvm.pk} join CompetitorProduct as cp on {mmd:product}={cp.pk}} where {mmd:counterVisit}=?latestCounterVisit");
        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(CompetitorProductModel.class));
        query.getQueryParameters().putAll(attr);
        LOG.info(String.format("Get Selected Competitor Product query :%s with Latest Counter Visit :%s",query,latestCounterVisit.getId()));
        final SearchResult<CompetitorProductModel> result = getFlexibleSearchService().search(query);
        return result.getResult() != null && !result.getResult().isEmpty() ? result.getResult() : Collections.emptyList();

    }

    /**
     * Get List of All CompetitorProducts By State
     * @param brands
     * @param states
     * @return
     */
    @Override
    public List<CompetitorProductModel> getListOfCompetitorProductsByState(List<BrandModel> brands, List<String> states){
        final Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("brands",brands);
        attr.put("states",states);
        final StringBuilder sql = new StringBuilder();
        sql.append("Select {pk} from {CompetitorProduct} where {state} IN (?states) and {brand} IN (?brands)");
        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(CompetitorProductModel.class));
        query.getQueryParameters().putAll(attr);
        LOG.info(String.format("Get List of all Competitor Products query :%s by state :%s",query,states));
        final SearchResult<CompetitorProductModel> result = getFlexibleSearchService().search(query);
        return result.getResult();
    }
}
