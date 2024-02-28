package com.eydms.core.feedback.services.impl;

import com.eydms.core.enums.FeedbackCategory;
import com.eydms.core.feedback.dao.EyDmsFeedbackDao;
import com.eydms.core.feedback.services.EyDmsFeedbackService;
import com.eydms.core.model.FeedbackAndComplaintsModel;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.facades.data.FeedbackAndComplaintsData;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.customerreview.model.CustomerReviewModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Class for eydms feedback service
 */
public class DefaultEyDmsFeedbackService implements EyDmsFeedbackService {

    private ModelService modelService;
    private EyDmsFeedbackDao eydmsFeedbackDao;

    @Autowired
    UserService userService;

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
    @Override
    public CustomerReviewModel createFeedback(Double rating, String headline, String comment, UserModel user, ProductModel product ,  OrderModel order){

        CustomerReviewModel review = (CustomerReviewModel)this.getModelService().create(CustomerReviewModel.class);
        review.setUser(user);
        review.setProduct(product);
        if(null!= order){
            review.setOrder(order);
        }
        review.setRating(rating);
        review.setHeadline(headline);
        review.setComment(comment);
        this.getModelService().save(review);
        return review;
    }

    /**
     * getting customer feedback with Order code(Order code is optional, if not present all feedback
     * of that customer will be fetched)
     * @param customer
     * @param order
     * @return
     */
    @Override
    public List<CustomerReviewModel> getFeedbackForCustomerAndOrder(final CustomerModel customer, final OrderModel order){
        return getEyDmsFeedbackDao().findFeedbacksForCustomerAndOrder(customer,order);
    }

    @Override
    public boolean submitInfoDeskFeedback(FeedbackAndComplaintsData feedbackAndComplaintsData) {
        EyDmsUserModel eydmsUser = (EyDmsUserModel) userService.getCurrentUser();

        FeedbackAndComplaintsModel feedbackAndComplaintsModel = modelService.create(FeedbackAndComplaintsModel.class);
        if(feedbackAndComplaintsData != null)
        {
            feedbackAndComplaintsModel.setFeedbackArea(feedbackAndComplaintsData.getFeedbackArea());
            feedbackAndComplaintsModel.setComplaintsComment(feedbackAndComplaintsData.getCounterComplaints());
            feedbackAndComplaintsModel.setFeedbackCategory(FeedbackCategory.valueOf(FeedbackCategory.INFO_DESK.getCode()));
            if(eydmsUser != null)
                feedbackAndComplaintsModel.setFeedbackRaisedBy(eydmsUser);
            modelService.save(feedbackAndComplaintsModel);
        }
        return true;
    }

    public ModelService getModelService() {
        return modelService;
    }

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    public EyDmsFeedbackDao getEyDmsFeedbackDao() {
        return eydmsFeedbackDao;
    }

    public void setEyDmsFeedbackDao(EyDmsFeedbackDao eydmsFeedbackDao) {
        this.eydmsFeedbackDao = eydmsFeedbackDao;
    }

}
