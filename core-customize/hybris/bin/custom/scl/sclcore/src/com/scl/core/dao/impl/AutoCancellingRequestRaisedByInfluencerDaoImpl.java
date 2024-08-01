package com.scl.core.dao.impl;

import com.scl.core.dao.AutoCancellingRequestRaisedByInfluencerDao;
import com.scl.core.enums.PointRequisitionStatus;
import com.scl.core.model.PointRequisitionModel;
import com.scl.core.model.SclCustomerModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.user.UserService;

import javax.annotation.Resource;
import java.util.*;

public class AutoCancellingRequestRaisedByInfluencerDaoImpl implements AutoCancellingRequestRaisedByInfluencerDao {

    @Resource
    FlexibleSearchService flexibleSearchService;
    @Resource
    UserService userService;

    public FlexibleSearchService getFlexibleSearchService() {
        return flexibleSearchService;
    }

    public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }


    @Override
    public List<PointRequisitionModel> getListOfRequestRaisedBeforeThreeDays(Date requestRaisedDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {PointRequisition as p} where {p:requisitionCreationDate}<?requestRaisedDate and {p:status}=?status");
        PointRequisitionStatus status = PointRequisitionStatus.PENDING;
        params.put("status", status);
        params.put("requestRaisedDate",requestRaisedDate);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(PointRequisitionModel.class));
        final SearchResult<PointRequisitionModel> searchResult = flexibleSearchService.search(query);
        List<PointRequisitionModel> result = searchResult.getResult();
        return result!=null && !result.isEmpty() ? result : Collections.emptyList();
    }
}
