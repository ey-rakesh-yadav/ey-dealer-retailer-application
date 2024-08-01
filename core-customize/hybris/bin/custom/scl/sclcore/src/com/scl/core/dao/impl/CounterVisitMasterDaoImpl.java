package com.scl.core.dao.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.dao.CounterVisitMasterDao;
import com.scl.core.model.CounterVisitMasterModel;

import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

public class CounterVisitMasterDaoImpl extends DefaultGenericDao<CounterVisitMasterModel> implements CounterVisitMasterDao  {

	public CounterVisitMasterDaoImpl() {
		super(CounterVisitMasterModel._TYPECODE);
	}

    @Autowired
    FlexibleSearchService flexibleSearchService;

	@Override
	public CounterVisitMasterModel findCounterVisitById(String counterVisitId) {
        validateParameterNotNullStandardMessage("counterVisitId", counterVisitId);
        final List<CounterVisitMasterModel> counterVisitList = this.find(Collections.singletonMap(CounterVisitMasterModel.PK, counterVisitId));
        if (counterVisitList.size() > 1)
        {
            throw new AmbiguousIdentifierException(
                    String.format("Found %d counterVisit with the counterVisitId value: '%s', which should be unique", counterVisitList.size(),
                    		counterVisitId));
        }
        else
        {
            return counterVisitList.isEmpty() ? null : counterVisitList.get(0);
        }
    }
	
	@Override
	public CounterVisitMasterModel findCounterVisitByLastVisitDate(Date lastVisitDate) {
        validateParameterNotNullStandardMessage("lastVisitdate", lastVisitDate);
        final List<CounterVisitMasterModel> counterVisitList = this.find(Collections.singletonMap(CounterVisitMasterModel.LASTVISITDATE, lastVisitDate));
        if (counterVisitList.size() > 1)
        {
            throw new AmbiguousIdentifierException(
                    String.format("Found %d counterVisit with the counterVisitId value: '%s', which should be unique", counterVisitList.size(),
                    		lastVisitDate));
        }
        else
        {
            return counterVisitList.isEmpty() ? null : counterVisitList.get(0);
        }
    }

    @Override
    public List<List<Object>> fetchBrandWiseAggregatedData(String counterVisitId) {
        String queryResult= "SELECT sum({m:wholeSales}), sum({m:retailSales}), {b:isocode}, {site:uid} from {MarketMappingDetails as m JOIN CounterVisitMaster as c ON {m:counterVisit}={c.pk} JOIN Brand as b ON {m:brand}={b.pk} JOIN CMSSite as site ON {b:sclBrand}={site.pk}} WHERE {c:pk} = ?counterVisitId GROUP BY {m:brand} , {b:isocode}, {site:uid}";
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
        query.addQueryParameter("counterVisitId",counterVisitId);
        query.setResultClassList(Arrays.asList(Double.class,Double.class,String.class, String.class));
        SearchResult<List<Object>> result= flexibleSearchService.search(query);
        if(!result.getResult().isEmpty() && result.getResult() != null) {
            return result.getResult();
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }
}
