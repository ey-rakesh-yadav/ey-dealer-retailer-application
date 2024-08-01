package com.scl.facades.populators.order;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.scl.core.dao.DataConstraintDao;
import com.scl.core.enums.CreatedFromCRMorERP;
import com.scl.core.model.PartnerCustomerModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.SclUserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.scl.core.enums.IncoTerms;
import com.scl.facades.order.data.TrackingCommentData;
import com.scl.facades.order.data.TrackingData;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class DefaultSCLTrackingDetailsOnEntryPopulator implements Populator<OrderEntryModel, List<TrackingData>> {
	private static final Logger LOG = LoggerFactory.getLogger(DefaultSCLTrackingDetailsOnEntryPopulator.class);

    private EnumerationService enumerationService;

	@Autowired
	DataConstraintDao dataConstraintDao;

    @Override
    public void populate(OrderEntryModel source, List<TrackingData> target) throws ConversionException {

    	Assert.notNull(source, "Parameter source cannot be null.");
    	Assert.notNull(target, "Parameter target cannot be null.");

		//    	TrackingData orderRequisitionTrackingData = new TrackingData();
		//    	orderRequisitionTrackingData.setCode("Retailer Requested");
		//    	orderRequisitionTrackingData.setName("Retailer Requested");
		//    	orderRequisitionTrackingData.setActualTime(order.getDate());
		//    	orderRequisitionTrackingData.setIndex(1);
		//    	if(entry.get) {
		//    		List<TrackingCommentData> commentList = new ArrayList<>();
		//    		commentList.add(createTrackingComment(null, order.getCode()+"/"+entry.getEntryNumber(), 1, false, false));
		//
		//    		orderRequisitionTrackingData.setTrackingDetails(commentList);
		//    	}
		//
		//    	target.add(orderRequisitionTrackingData);


		TrackingData orderReceivedTrackingData = new TrackingData();
		orderReceivedTrackingData.setCode(OrderStatus.ORDER_RECEIVED.getCode());
		orderReceivedTrackingData.setName("App Order Generated");
		orderReceivedTrackingData.setActualTime(source.getOrder().getDate());
		orderReceivedTrackingData.setIndex(2);
		if(source.getOrder().getCode()!=null && source.getEntryNumber()!=null) {
			List<TrackingCommentData> commentList = new ArrayList<>();
			commentList.add(createTrackingComment(null, source.getOrder().getCode()+"/"+source.getEntryNumber(), 1, false, false));
			if(Objects.nonNull(source.getPlacedByCustomer())){
					commentList.add(createTrackingComment("Placed by : ", source.getPlacedByCustomer(), 2, false, false));
			}
			else if(Objects.nonNull(source.getOrder().getPlacedBy())){
				if(source.getOrder().getPlacedBy() instanceof SclUserModel){
					commentList.add(createTrackingComment("Placed by : ", source.getOrder().getPlacedBy().getUid(), 2, false, false));
				}
				else {
					if(source.getOrder().getPlacedBy() instanceof SclCustomerModel)
					commentList.add(createTrackingComment("Placed by : ", source.getOrder().getPlacedBy().getName(), 2, false, false));
				}
			}
			else{
				if (Objects.nonNull(source.getOrder().getCreatedFromCRMorERP())){
					String placedByErpComment = dataConstraintDao.findVersionByConstraintName("ORDER_PLACED_BY_ERP_COMMENT");
					commentList.add(createTrackingComment("Placed by : ", placedByErpComment, 2, false, false));
				}
			  }
			orderReceivedTrackingData.setTrackingComments(commentList);
		}

		target.add(orderReceivedTrackingData);


		if(source.getOrder().getBlockedDate()!=null) {
			TrackingData orderBlockedTrackingData = new TrackingData();
			orderBlockedTrackingData.setCode("Order Blocked");
			orderBlockedTrackingData.setName("Order Blocked");
			orderBlockedTrackingData.setActualTime(source.getOrder().getBlockedDate());
			orderBlockedTrackingData.setIndex(3);
			if(source.getOrder().getStatus()!=null && (source.getOrder().getStatus().equals(OrderStatus.CREDIT_BLOCK) || source.getOrder().getStatus().equals(OrderStatus.TOTAL_BLOCK))  
					&& source.getOrder().getErpOrderNumber()!=null && source.getErpLineItemId()!=null) {
				List<TrackingCommentData> commentList = new ArrayList<>();
				commentList.add(createTrackingComment(null, source.getOrder().getErpOrderNumber()+"/"+source.getErpLineItemId(), 1, false, false));

				orderBlockedTrackingData.setTrackingComments(commentList);
			}
			target.add(orderBlockedTrackingData);
		}

		/**
		 * Placed By Tracking Data

		TrackingData placedByTrackingData = new TrackingData();
		String placedBy = "Placed by : ";
		if(Objects.nonNull(source.getOrder().getCreatedFromCRMorERP())){
			if(source.getOrder().getCreatedFromCRMorERP().equals(CreatedFromCRMorERP.S4HANA)){
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


		
		TrackingData orderAcceptedTrackingData = new TrackingData();
		orderAcceptedTrackingData.setCode(OrderStatus.ORDER_ACCEPTED.getCode());
		orderAcceptedTrackingData.setName("Order Accepted");
		orderAcceptedTrackingData.setActualTime(source.getOrder().getOrderAcceptedDate());
		orderAcceptedTrackingData.setIndex(4);
		List<TrackingCommentData> commentList1 = new ArrayList<>();
		if(source.getOrder().getOrderAcceptedDate()!=null && source.getOrder().getErpOrderNumber()!=null && source.getErpLineItemId()!=null && source.getSource()!=null) {
			commentList1.add(createTrackingComment(null, source.getOrder().getErpOrderNumber()+"/"+source.getErpLineItemId(), 1, false, false));
			commentList1.add(createTrackingComment(null, source.getSource().getCode()+" : "+source.getSource().getName(), 2, false, false));
		}
		if(source.getOrder().getOrderAcceptedDate()==null && source.getOrder().getErpStatusDesc()!=null) {
			String err = source.getOrder().getErpStatusDesc();
			if(source.getErpStatusDesc()!=null) {
				err +=", " + source.getErpStatusDesc();
			}
			commentList1.add(createTrackingComment(null, err, 3, false, false));
		}
		if(commentList1.size()>0) {
			orderAcceptedTrackingData.setTrackingComments(commentList1);
		}
		target.add(orderAcceptedTrackingData);

		if(source.getCancelledDate()!=null) {
			TrackingData cancelledTrackingData = new TrackingData();
			cancelledTrackingData.setCode(OrderStatus.CANCELLED.getCode());
			cancelledTrackingData.setName(getEnumerationService().getEnumerationName(OrderStatus.CANCELLED));
			cancelledTrackingData.setActualTime(source.getCancelledDate());
			cancelledTrackingData.setIndex(5);
			if(source.getCancelReason()!=null ) {
				List<TrackingCommentData> commentList = new ArrayList<>();
				commentList.add(createTrackingComment("Cancel Reason: ", source.getCancelReason(), 1, false, false));

				cancelledTrackingData.setTrackingComments(commentList);
			}
			target.add(cancelledTrackingData);
		}

    }
    
    private TrackingCommentData createTrackingComment(String label, String value, Integer sequence, Boolean isGpsEnabled, Boolean isContactNumber) {
		TrackingCommentData commentData = new TrackingCommentData();
		commentData.setLabel(label);
		commentData.setValue(value);
		commentData.setSequence(sequence);
		commentData.setIsGpsEnabled(isGpsEnabled);
		commentData.setIsContactNumber(isContactNumber);
		return commentData;
	}
    
    public EnumerationService getEnumerationService() {
        return enumerationService;
    }

    public void setEnumerationService(EnumerationService enumerationService) {
        this.enumerationService = enumerationService;
    }
}