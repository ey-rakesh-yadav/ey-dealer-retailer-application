package com.eydms.core.feedback.dao.impl;

import com.eydms.core.feedback.dao.EyDmsFeedbackDao;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.customerreview.model.CustomerReviewModel;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Class for eydms feedabcks
 */
public class DefaultEyDmsFeedbackDao extends DefaultGenericDao<CustomerReviewModel> implements EyDmsFeedbackDao {

    DefaultEyDmsFeedbackDao(){
        super(CustomerReviewModel._TYPECODE);
    }

    private static final String FIND_FEEDBACK_BY_CUSTOMER_QUERY = "SELECT {"+CustomerReviewModel.PK+"} FROM {"+CustomerReviewModel._TYPECODE+"} " +
            "WHERE {"+CustomerReviewModel.USER+"} = ?customer ";

    private static final String FIND_FEEDBACK_BY_CUSTOMER_AND_ORDER_QUERY = FIND_FEEDBACK_BY_CUSTOMER_QUERY + " AND {"
            + CustomerReviewModel.ORDER+"} = ?order ";

    private FlexibleSearchService flexibleSearchService;

    /**
     * Dao class to fetch feedback for customer ,and order(if order is provided)
     * @param customer
     * @param order
     * @return
     */
    @Override
    public List<CustomerReviewModel> findFeedbacksForCustomerAndOrder(final CustomerModel customer,final OrderModel order){

        validateParameterNotNull(customer, "Customer must not be null");

        String query = FIND_FEEDBACK_BY_CUSTOMER_QUERY;

        final Map<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("customer", customer);

        if(null!=order){
            query = FIND_FEEDBACK_BY_CUSTOMER_AND_ORDER_QUERY;
            queryParams.put("order", order);
        }

        final SearchResult<CustomerReviewModel> result = getFlexibleSearchService().search(query, queryParams);
        return result.getResult();
    }

    @Override
    public FlexibleSearchService getFlexibleSearchService() {
        return flexibleSearchService;
    }

    @Override
    public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }

}
