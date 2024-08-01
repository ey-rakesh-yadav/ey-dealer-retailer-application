package com.scl.core.dao.impl;

import com.scl.core.dao.SclStageGateDao;
import com.scl.core.enums.CreatedFromCRMorERP;
import com.scl.core.enums.WarehouseType;
import com.scl.core.jalo.SclUser;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.SclUserModel;
import com.scl.core.model.StageGateSequenceMapperModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.*;

public class SclStageGateDaoImpl implements SclStageGateDao {
    @Autowired
    FlexibleSearchService flexibleSearchService;


    /**
     * @param warehouseType
     * @return
     */
    @Override
    public List<StageGateSequenceMapperModel> getStageGateSequenceMapperListForSource(WarehouseType warehouseType) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {StageGateSequenceMapper} WHERE {sourceType}=?sourceType ORDER BY {sequenceNumber}");
        params.put("sourceType", warehouseType);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(SclUser.class));
        query.addQueryParameters(params);
        final SearchResult<StageGateSequenceMapperModel> searchResult = flexibleSearchService.search(query);
        List<StageGateSequenceMapperModel> result = searchResult.getResult();
        return result!=null && !result.isEmpty() ? result : Collections.emptyList();
    }

    /**
     * @return
     */
    @Override
    public List<OrderModel> getS4OrdersFoGetCall() {
        final Map<String, Object> params = new HashMap<String, Object>();

        final StringBuilder builder = new StringBuilder("SELECT {o.pk} FROM {Order as o} WHERE  {o.erpOrderNumber} is NOT null and {o.status} in (?orderStatus) and {o.createdFromCRMorERP}=?orderedFrom ");
        List<OrderStatus> orderStatus = new ArrayList<OrderStatus>();
        orderStatus.add(OrderStatus.ORDER_ACCEPTED);
        params.put("orderedFrom", CreatedFromCRMorERP.S4HANA);

        params.put("orderStatus", orderStatus);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(OrderModel.class));
        query.addQueryParameters(params);
        final SearchResult<OrderModel> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();

    }
}
