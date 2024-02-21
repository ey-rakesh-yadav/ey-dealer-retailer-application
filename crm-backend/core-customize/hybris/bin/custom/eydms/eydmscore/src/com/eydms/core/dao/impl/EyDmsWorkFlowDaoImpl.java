package com.eydms.core.dao.impl;

import com.eydms.core.dao.EyDmsWorkflowDao;
import com.eydms.core.enums.WorkflowType;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.core.model.EyDmsWorkflowModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EyDmsWorkFlowDaoImpl implements EyDmsWorkflowDao {
    @Autowired
    FlexibleSearchService flexibleSearchService;
    @Override
    public EyDmsWorkflowModel getEyDmsWorkflowByType(WorkflowType type) {
        final Map<String, Object> params = new HashMap<>();

        final StringBuilder builder = new StringBuilder("SELECT {s:pk} from {EyDmsWorkflow AS s " +
                "where {s.type}=?type ");
        params.put("type",type);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsWorkflowModel.class));
        query.addQueryParameters(params);
        final SearchResult<EyDmsWorkflowModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
        else
            return null;
    }
}
