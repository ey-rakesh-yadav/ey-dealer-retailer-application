package com.scl.facades.feedback.impl;

import com.scl.core.feedback.services.SclFeedbackService;
import com.scl.facades.data.FeedbackAndComplaintsData;
import com.scl.facades.feedback.SclFeedbackFacade;
import de.hybris.platform.b2b.services.B2BOrderService;
import de.hybris.platform.commercefacades.product.data.ReviewData;
import de.hybris.platform.commercewebservices.core.product.data.ReviewDataList;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.customerreview.enums.CustomerReviewApprovalType;
import de.hybris.platform.customerreview.model.CustomerReviewModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultSclFeedbackFacade implements SclFeedbackFacade {

    private SclFeedbackService sclFeedbackService;
    private B2BOrderService b2bOrderService;
    private UserService userService;
    private ConfigurationService configurationService;
    private ProductService productService;
    private ModelService modelService;
    private CommonI18NService commonI18NService;
    private Converter<CustomerReviewModel, ReviewData> customerReviewConverter;

    private final static String DUMMY_PRODUCT_CODE = "code.product.dummy";

    /**
     * Method to post the feedback
     * @param orderCode
     * @param reviewData
     * @return
     */
    @Override
    public ReviewData postFeedback(final String orderCode, final ReviewData reviewData) {
              CustomerReviewModel customerReview;
        //Adding a dummy product to Customer Review , as its a mandatory field
        final String dummyProdcutCode = getConfigurationService().getConfiguration().getString(DUMMY_PRODUCT_CODE);
        final UserModel user = getUserService().getCurrentUser();
        final ProductModel product = getProductService().getProductForCode(dummyProdcutCode);
        OrderModel order = null;
        if (StringUtils.isNotBlank(orderCode)) {
            order = getB2bOrderService().getOrderForCode(orderCode);
            if(null == order){
                throw new ModelNotFoundException("No Order with code "+orderCode+ " found");
            }
        }

        customerReview = getSclFeedbackService().createFeedback(reviewData.getRating(), reviewData.getHeadline(), reviewData.getComment(), user,product, order);
        customerReview.setLanguage(getCommonI18NService().getCurrentLanguage());
        customerReview.setApprovalStatus(CustomerReviewApprovalType.APPROVED);
        //customerReview.setAlias(reviewData.getAlias());
        getModelService().save(customerReview);
        return getCustomerReviewConverter().convert(customerReview);
    }

    /**
     * Get the customer feedback
     * @param orderCode
     * @return
     */
    @Override
    public ReviewDataList getCustomerFeedback(final String orderCode){

        ReviewDataList reviewDataList = new ReviewDataList();
        final CustomerModel customer = (CustomerModel) getUserService().getCurrentUser();
        OrderModel order = null;
        if (StringUtils.isNotBlank(orderCode)) {
            order = getB2bOrderService().getOrderForCode(orderCode);
            if(null== order){
                throw new ModelNotFoundException("No Order with code "+orderCode+ " found");
            }
        }
        List<CustomerReviewModel> feedbacks = getSclFeedbackService().getFeedbackForCustomerAndOrder(customer,order);

        List<ReviewData> feedbackDatas = feedbacks.stream().map( feedback -> getCustomerReviewConverter().convert(feedback)).collect(Collectors.toList());
        reviewDataList.setReviews(feedbackDatas);
        return reviewDataList;

    }

    @Override
    public boolean submitInfoDeskFeedback(FeedbackAndComplaintsData feedbackAndComplaintsData) {
        return sclFeedbackService.submitInfoDeskFeedback(feedbackAndComplaintsData);
    }


    public SclFeedbackService getSclFeedbackService() {
        return sclFeedbackService;
    }

    public void setSclFeedbackService(SclFeedbackService sclFeedbackService) {
        this.sclFeedbackService = sclFeedbackService;
    }
    public B2BOrderService getB2bOrderService() {
        return b2bOrderService;
    }

    public void setB2bOrderService(B2BOrderService b2bOrderService) {
        this.b2bOrderService = b2bOrderService;
    }
    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    public ProductService getProductService() {
        return productService;
    }

    public void setProductService(ProductService productService) {
        this.productService = productService;
    }
    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
    public ModelService getModelService() {
        return modelService;
    }

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    public CommonI18NService getCommonI18NService() {
        return commonI18NService;
    }

    public void setCommonI18NService(CommonI18NService commonI18NService) {
        this.commonI18NService = commonI18NService;
    }

    public Converter<CustomerReviewModel, ReviewData> getCustomerReviewConverter() {
        return customerReviewConverter;
    }

    public void setCustomerReviewConverter(Converter<CustomerReviewModel, ReviewData> customerReviewConverter) {
        this.customerReviewConverter = customerReviewConverter;
    }
}


