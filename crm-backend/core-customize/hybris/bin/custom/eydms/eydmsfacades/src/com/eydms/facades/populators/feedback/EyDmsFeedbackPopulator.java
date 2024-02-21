package com.eydms.facades.populators.feedback;

import de.hybris.platform.commercefacades.product.data.ReviewData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.customerreview.model.CustomerReviewModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;


public class EyDmsFeedbackPopulator implements Populator<CustomerReviewModel, ReviewData> {

    /**
     * Method to populate custom review data
     * @param customerReviewModel
     * @param reviewData
     * @throws ConversionException
     */
    @Override
    public void populate(CustomerReviewModel customerReviewModel, ReviewData reviewData) throws ConversionException {

        //Populating Order Code to review Data
        if(null != customerReviewModel && null != customerReviewModel.getOrder()){
            reviewData.setOrderCode(customerReviewModel.getOrder().getCode());
        }


    }
}
