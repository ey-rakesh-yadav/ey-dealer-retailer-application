package com.eydms.core.brand.dao.impl;


import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eydms.core.brand.dao.BrandDao;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.c2l.BrandModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;

public class BrandDaoImpl extends DefaultGenericDao<BrandModel> implements BrandDao {
	
	private static final String SEARCH_QUERY ="SELECT distinct{b:pk} from {Brand as b JOIN CompetitorProduct as c on {c:brand}={b:pk}} where {c:state} IN (?states) and {b.eydmsBrand}!=?brand or {b.eydmsBrand} is null ";
	
	public BrandDaoImpl() {
		super(BrandModel._TYPECODE);
	}

	@Override
	public List<BrandModel> findAllBrand() {

		return this.find();
	}
	
	@Override
    public BrandModel findBrandById(final String brandId) {
        validateParameterNotNullStandardMessage("brandId", brandId);
        final List<BrandModel> brandList = this.find(Collections.singletonMap(BrandModel.ISOCODE, brandId));
        if (brandList.size() > 1)
        {
            throw new AmbiguousIdentifierException(
                    String.format("Found %d leads with the leadId value: '%s', which should be unique", brandList.size(),
                            brandId));
        }
        else
        {
            return brandList.isEmpty() ? null : brandList.get(0);
        }
    }

	@Override
	public List<BrandModel> getCompetitorsBrands(BaseSiteModel currentBaseSite, List<String> states) {
		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put("brand", currentBaseSite.getPk());
		attr.put("states", states);
		final StringBuilder sql = new StringBuilder();
		sql.append(SEARCH_QUERY);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Arrays.asList(BrandModel.class));
		query.getQueryParameters().putAll(attr);
		final SearchResult<BrandModel> result = getFlexibleSearchService().search(query);
		return result.getResult() != null && !result.getResult().isEmpty() ? result.getResult() : Collections.emptyList();
	}

	@Override
	public List<BrandModel> findBrandByState(List<String> states) {
		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put("states", states);
		final StringBuilder sql = new StringBuilder();
		sql.append("select distinct{b:pk} from {Brand as b JOIN CompetitorProduct as c on {c:brand}={b:pk}} where {c:state} IN (?states)");
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Arrays.asList(BrandModel.class));
		query.getQueryParameters().putAll(attr);
		final SearchResult<BrandModel> result = getFlexibleSearchService().search(query);
		return result.getResult() != null && !result.getResult().isEmpty() ? result.getResult() : Collections.emptyList();
	}
}
