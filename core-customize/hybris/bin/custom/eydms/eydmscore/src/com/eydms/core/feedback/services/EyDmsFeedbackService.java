package com.eydms.core.feedback.services;

import com.eydms.facades.data.FeedbackAndComplaintsData;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.customerreview.model.CustomerReviewModel;

import java.util.List;

/**
 * Interface for eydms feedback service
 */
public interface EyDmsFeedbackService {

    /**
     * Creating  feeback with provided fields
     * @param rating
     * @param headline
     * @param comment
     * @param user
     * @param product
     * @param order
     * @return
     */
    CustomerReviewModel createFeedback(Double rating, String headline, String comment, UserModel user, ProductModel product , OrderModel order);

    /**
     * getting customer feedback with Order code(Order code is optional, if not present all feedback
     * of that customer will be fetched)
     * @param customer
     * @param order
     * @return
     */
    List<CustomerReviewModel> getFeedbackForCustomerAndOrder(final CustomerModel customer, final OrderModel order);

    boolean submitInfoDeskFeedback(FeedbackAndComplaintsData feedbackAndComplaintsData);
}
