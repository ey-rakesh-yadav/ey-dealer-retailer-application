package com.scl.facades.populators.order;

import com.scl.core.enums.CreatedFromCRMorERP;
import com.scl.facades.order.data.TrackingData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DefaultSCLTrackingDetailsOnOrderPopulator implements Populator<OrderModel, List<TrackingData>> {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultSCLTrackingDetailsOnOrderPopulator.class);
    private EnumerationService enumerationService;

    @Override
    public void populate(OrderModel source, List<TrackingData> target) throws ConversionException {

        Assert.notNull(source, "Parameter source cannot be null.");
        Assert.notNull(target, "Parameter target cannot be null.");


        TrackingData orderReceivedTrackingData = new TrackingData();
        orderReceivedTrackingData.setCode(OrderStatus.ORDER_RECEIVED.getCode());
        orderReceivedTrackingData.setName(getEnumerationService().getEnumerationName(OrderStatus.ORDER_RECEIVED));
        orderReceivedTrackingData.setActualTime(source.getDate());
        orderReceivedTrackingData.setIndex(1);
        target.add(orderReceivedTrackingData);

        TrackingData orderAcceptedTrackingData = new TrackingData();
        orderAcceptedTrackingData.setCode(OrderStatus.ORDER_ACCEPTED.getCode());
        orderAcceptedTrackingData.setName(getEnumerationService().getEnumerationName(OrderStatus.ORDER_ACCEPTED));
        orderAcceptedTrackingData.setActualTime(source.getOrderAcceptedDate());
        orderAcceptedTrackingData.setIndex(2);
        populateComments(source,orderAcceptedTrackingData);
        target.add(orderAcceptedTrackingData);


        //Populating Sub Statuses
        TrackingData orderFailedValidationTrackingData = new TrackingData();
        orderFailedValidationTrackingData.setCode(OrderStatus.ORDER_FAILED_VALIDATION.getCode());
        orderFailedValidationTrackingData.setName(getEnumerationService().getEnumerationName(OrderStatus.ORDER_FAILED_VALIDATION));
        orderFailedValidationTrackingData.setActualTime(source.getOrderFailedValidationDate());
        orderFailedValidationTrackingData.setParent(OrderStatus.ORDER_ACCEPTED.getCode());
        orderFailedValidationTrackingData.setIndex(1);
        populateComments(source,orderFailedValidationTrackingData);
        target.add(orderFailedValidationTrackingData);

        TrackingData orderSentForApprovalTrackingData = new TrackingData();
        orderSentForApprovalTrackingData.setCode(OrderStatus.ORDER_SENT_TO_SO.getCode());
        orderSentForApprovalTrackingData.setName(getEnumerationService().getEnumerationName(OrderStatus.ORDER_SENT_TO_SO));
        orderSentForApprovalTrackingData.setActualTime(source.getOrderSentForApprovalDate());
        orderSentForApprovalTrackingData.setParent(OrderStatus.ORDER_ACCEPTED.getCode());
        orderSentForApprovalTrackingData.setIndex(2);
        target.add(orderSentForApprovalTrackingData);

        TrackingData orderModifiedTrackingData = new TrackingData();
        orderModifiedTrackingData.setCode(OrderStatus.ORDER_MODIFIED.getCode());
        orderModifiedTrackingData.setName(getEnumerationService().getEnumerationName(OrderStatus.ORDER_MODIFIED));
        orderModifiedTrackingData.setActualTime(source.getOrderModifiedDate());
        orderModifiedTrackingData.setParent(OrderStatus.ORDER_ACCEPTED.getCode());
        orderModifiedTrackingData.setIndex(3);
        target.add(orderModifiedTrackingData);


        /**
         * Placed By Tracking Data

        TrackingData placedByTrackingData = new TrackingData();
        String placedBy = "Placed by : ";
        if(Objects.nonNull(source.getCreatedFromCRMorERP())){
            if(source.getCreatedFromCRMorERP().equals(CreatedFromCRMorERP.S4HANA)){
                String placeByCustomer = placedBy.concat(source.getUser().getUid());
                placedByTrackingData.setPlacedByCustomer(placeByCustomer);
            }
        }
        else if(Objects.nonNull(source.getPlacedByCustomer())){
            String placeByCustomer = placedBy.concat(source.getPlacedByCustomer());
            placedByTrackingData.setPlacedByCustomer(placeByCustomer);
        }
        if(Objects.nonNull(source.getIsPartnerCustomer())){
            placedByTrackingData.setIsPartnerCustomer(source.getIsPartnerCustomer());
        }
        target.add(placedByTrackingData);

         */
        TrackingData orderValidatedTrackingData = new TrackingData();
        orderModifiedTrackingData.setCode(OrderStatus.ORDER_VALIDATED.getCode());
        orderValidatedTrackingData.setName(getEnumerationService().getEnumerationName(OrderStatus.ORDER_VALIDATED));
        orderValidatedTrackingData.setActualTime(source.getOrderValidatedDate());
        orderValidatedTrackingData.setParent(OrderStatus.ORDER_ACCEPTED.getCode());
        orderValidatedTrackingData.setIndex(4);
        target.add(orderValidatedTrackingData);


    }
    private void populateComments(OrderModel source , TrackingData trackingData){
        List<String>  comments = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(source.getRejectionReasons())){
            for(String reason : source.getRejectionReasons()){
                comments.add(reason);
            }
        }
        if(StringUtils.isBlank(source.getErpOrderNumber()) && StringUtils.isNotBlank(source.getErpStatusDesc())) {
        	comments.add(source.getErpStatusDesc());
        }
        if(StringUtils.isNotBlank(source.getCancelOrderApiStatusDesc()) && source.getCancelledDate()==null) {
        	comments.add("Cancellation Failure Reason : " + source.getCancelOrderApiStatusDesc());
        }
        if(!comments.isEmpty())
        	trackingData.setComment(comments);
        
    }
    public EnumerationService getEnumerationService() {
        return enumerationService;
    }

    public void setEnumerationService(EnumerationService enumerationService) {
        this.enumerationService = enumerationService;
    }
}
