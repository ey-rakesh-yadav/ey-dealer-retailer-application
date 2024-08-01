package com.scl.facades.feedback;

import com.scl.facades.data.FeedbackAndComplaintsData;
import de.hybris.platform.commercefacades.product.data.ReviewData;
import de.hybris.platform.commercewebservices.core.product.data.ReviewDataList;

import java.util.List;

public interface SclFeedbackFacade {

     /**
      * Method to post the feedback
      * @param orderCode
      * @param reviewData
      * @return
      */
     ReviewData postFeedback(final String orderCode, final ReviewData reviewData);

     /**
      * Get the customer feedback
      * @param orderCode
      * @return
      */
     ReviewDataList getCustomerFeedback(final String orderCode);

    boolean submitInfoDeskFeedback(FeedbackAndComplaintsData feedbackAndComplaintsData);
}
