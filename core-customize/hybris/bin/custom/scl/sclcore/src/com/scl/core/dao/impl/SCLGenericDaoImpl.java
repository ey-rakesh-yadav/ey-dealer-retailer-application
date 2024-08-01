package com.scl.core.dao.impl;

import com.scl.core.dao.SCLGenericDao;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.apache.commons.collections.CollectionUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SCLGenericDaoImpl implements SCLGenericDao {
    @Resource
    FlexibleSearchService flexibleSearchService;

    @Override
    public ItemModel findItemByTypeCodeAndUidParam(String typeCode, String uidQualifier, String param) {
        var itemList=findListItemByTypeCodeAndUidParam(typeCode, uidQualifier, param);
        if(Objects.nonNull(itemList)) {
            return itemList.get(0);
        }
        return null;
    }

    @Override
    public List<ItemModel> findListItemByTypeCodeAndUidParam(String typeCode, String uidQualifier, String param) {
        final Map<String, Object> params = new HashMap<>();
        String queryString = String.format("SELECT {pk} FROM {%s} WHERE {%s}=?param", typeCode, uidQualifier);
        params.put("param", param);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<ItemModel> searchResult = flexibleSearchService.search(query);
        if (CollectionUtils.isNotEmpty(searchResult.getResult())) {
            return searchResult.getResult();
        }

        return null;
    }
}
