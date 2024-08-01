package com.scl.core.source.dao.impl;

import com.scl.core.enums.ProductType;
import com.scl.core.enums.WarehouseType;
import com.scl.core.model.SclPlantLotSizeModel;
import com.scl.core.source.dao.SclPlantLotSizeDao;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.*;

public class SclPlantLotSizeDaoImpl  implements SclPlantLotSizeDao {

    private static final Logger LOG = Logger.getLogger(SclPlantLotSizeDaoImpl.class);

    @Resource
    FlexibleSearchService flexibleSearchService;

    @Autowired
    EnumerationService enumerationService;

    /**
     * @param state
     * @param district
     * @param plant
     * @param isPremiumProduct
     * @return
     */
    @Override
    public List<SclPlantLotSizeModel> findAllSourcePlantLotSize(String state, String district, WarehouseModel plant, boolean isPremiumProduct) {
        final Map<String, Object> params = new HashMap<String, Object>();
        HashSet<ProductType> productTypeList=new HashSet<>();
        final StringBuilder builder = new StringBuilder("SELECT DISTINCT {pk} FROM {SclPlantLotSize} WHERE {destinationState}=?destinationState AND {destinationDistrict}=?destinationDistrict AND ({plant}=?plant OR {plant} IS NULL) AND {productType} IN (?productTypes) ");
        params.put("destinationState",state);
        params.put("destinationDistrict",district);
        params.put("plant",plant);

        if(BooleanUtils.isTrue(isPremiumProduct)){
            productTypeList.add(enumerationService.getEnumerationValue(ProductType.class,"PREMIUM"));
            productTypeList.add(enumerationService.getEnumerationValue(ProductType.class,"BOTH"));
          params.put("productTypes",productTypeList);
        }else if(BooleanUtils.isFalse(isPremiumProduct)){
            productTypeList.add(enumerationService.getEnumerationValue(ProductType.class,"NONPREMIUM"));
            productTypeList.add(enumerationService.getEnumerationValue(ProductType.class,"BOTH"));
            params.put("productTypes",productTypeList);
        }
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(SclPlantLotSizeModel.class));
        query.addQueryParameters(params);
        LOG.info(String.format("find all source plant query ::%s",query));
        final SearchResult<SclPlantLotSizeModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult())?searchResult.getResult(): Collections.EMPTY_LIST;
    }
}
