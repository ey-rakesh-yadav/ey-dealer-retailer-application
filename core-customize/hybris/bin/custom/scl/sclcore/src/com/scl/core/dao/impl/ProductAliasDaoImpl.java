package com.scl.core.dao.impl;

import com.scl.core.dao.ProductAliasDao;
import com.scl.core.model.BrandingRequestDetailsModel;
import com.scl.core.model.ProductAliasModel;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductAliasDaoImpl extends DefaultGenericDao<ProductAliasModel> implements ProductAliasDao {


    public ProductAliasDaoImpl() {
        super(ProductAliasModel._TYPECODE);
    }

    private final String PRODUCT_ALIAS_MODEL_QUERY="SELECT {pa:pk} FROM {ProductAlias as pa} where {pa:validFrom}<=?currentDate and {pa:validTo}>=?currentDate ";

    @Autowired
    private FlexibleSearchService flexibleSearchService;

    /**
     * @return
     */
    @Override
    public List<ProductAliasModel> findAllProductAlias() {
        return this.find();
    }

    /**
     * @return
     */
    @Override
    public List<ProductAliasModel> getProductAlias() {

        final Map<String, Object> params = new HashMap<String, Object>();
        LocalDate currentDate = LocalDate.now();
        final StringBuilder builder = new StringBuilder(PRODUCT_ALIAS_MODEL_QUERY);
        params.put("currentDate", currentDate);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(ProductAliasModel.class));
        final SearchResult<ProductAliasModel> searchResult = flexibleSearchService.search(query);
        List<ProductAliasModel> result = searchResult.getResult();
        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }


}
