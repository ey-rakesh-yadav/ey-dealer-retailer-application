package com.scl.core.dao.impl;

import com.scl.core.dao.SclWorkflowDao;
import com.scl.core.enums.WorkflowType;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.SclUserModel;
import com.scl.core.model.SclWorkflowModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SclWorkFlowDaoImpl implements SclWorkflowDao {
    @Autowired
    FlexibleSearchService flexibleSearchService;
    @Override
    public SclWorkflowModel getSclWorkflowByType(WorkflowType type) {
        final Map<String, Object> params = new HashMap<>();

        final StringBuilder builder = new StringBuilder("SELECT {s:pk} from {SclWorkflow AS s " +
                "where {s.type}=?type ");
        params.put("type",type);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(SclWorkflowModel.class));
        query.addQueryParameters(params);
        final SearchResult<SclWorkflowModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
        else
            return null;
    }
}
