package com.scl.core.dao.impl;

import com.scl.core.dao.SchemeDetailsDao;
import com.scl.core.enums.PartnerLevel;
import com.scl.core.model.SchemeDetailsModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.site.BaseSiteService;

import javax.annotation.Resource;
import java.util.*;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class DefaultSchemeDetailsDao extends DefaultGenericDao<SchemeDetailsModel> implements SchemeDetailsDao {
    @Resource
    private BaseSiteService baseSiteService;
    public DefaultSchemeDetailsDao() {
        super(SchemeDetailsModel._TYPECODE);
    }

    @Override
    public SchemeDetailsModel findSchemeDetailsByPK(String pk) {
        validateParameterNotNullStandardMessage("pk", pk);
        final List<SchemeDetailsModel> schemeDetailsModels = this.find(Collections.singletonMap(SchemeDetailsModel.PK, pk));
        if (schemeDetailsModels.size() > 1)
        {
            throw new AmbiguousIdentifierException(
                    String.format("Found %d scheme Details with the PK value: '%s', which should be unique", schemeDetailsModels.size(),
                            pk));
        }
        else
        {
            return schemeDetailsModels.isEmpty() ? null : schemeDetailsModels.get(0);
        }
    }

    @Override
    public List<SchemeDetailsModel> findOnGoingSchemes(Date endDate, PartnerLevel partnerLevel) {

        final Map<String, Object> queryParams = new HashMap<String, Object>();
        String queryString = "SELECT {pk} FROM {SchemeDetails }";
               String whereClause= " where {endDate}>=?dateParam and {partnerLevel}=?partner and {brand} = ?brand";
        queryParams.put("dateParam",endDate);
        queryParams.put("partner",partnerLevel);
        queryParams.put("brand", baseSiteService.getCurrentBaseSite());
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString+whereClause);
        query.addQueryParameters(queryParams);
        final SearchResult<SchemeDetailsModel> searchResult = getFlexibleSearchService().search(query);
        List<SchemeDetailsModel> result = searchResult.getResult();
        return result!=null ? result : Collections.emptyList();
    }
}
