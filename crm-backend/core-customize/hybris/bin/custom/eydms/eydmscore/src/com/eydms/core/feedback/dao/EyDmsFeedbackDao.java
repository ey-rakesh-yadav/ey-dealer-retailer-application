package com.eydms.core.feedback.dao;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.customerreview.model.CustomerReviewModel;
import de.hybris.platform.servicelayer.internal.dao.Dao;

import java.util.List;

/**
 * Interface for eydms feedabcks
 */
public interface EyDmsFeedbackDao extends Dao {

    /**
     * Dao class to fetch feedback for customer ,and order(if order is provided)
     * @param customer
     * @param order
     * @return
     */
    List<CustomerReviewModel> findFeedbacksForCustomerAndOrder(final CustomerModel customer, final OrderModel order);
}
