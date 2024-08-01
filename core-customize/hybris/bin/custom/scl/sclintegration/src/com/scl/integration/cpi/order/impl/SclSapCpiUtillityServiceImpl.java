package com.scl.integration.cpi.order.impl;

import com.scl.core.model.FreightAndIncoTermsMasterModel;
import com.scl.integration.cpi.order.SclSapCpiUtillityService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SclSapCpiUtillityServiceImpl implements SclSapCpiUtillityService {

    @Autowired
    FlexibleSearchService flexibleSearchService;

    @Override
    public List<FreightAndIncoTermsMasterModel> findFreightAndIncoTerms(String state, String district, BaseSiteModel brand, String orgType) {
        final Map<String, Object> attr = new HashMap<String, Object>();
        attr.put(FreightAndIncoTermsMasterModel.STATE, state.toUpperCase());
        attr.put(FreightAndIncoTermsMasterModel.DISTRICT, district.toUpperCase());
        attr.put(FreightAndIncoTermsMasterModel.BRAND, brand.getUid());
        attr.put(FreightAndIncoTermsMasterModel.ORGTYPE, orgType);
        String queryResult="SELECT {f:pk} from {FreightAndIncoTermsMaster as f} where UPPER({f:state})=?state and UPPER({f:district})=?district and {f:brand}=?brand and {f:orgType}=?orgType";

        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
        query.getQueryParameters().putAll(attr);
        final SearchResult<FreightAndIncoTermsMasterModel> result = flexibleSearchService.search(query);
        if(result.getResult() != null && result.getResult().isEmpty())
        {
            return result.getResult();
        }
        else {
            return Collections.EMPTY_LIST;
        }
    }
}
