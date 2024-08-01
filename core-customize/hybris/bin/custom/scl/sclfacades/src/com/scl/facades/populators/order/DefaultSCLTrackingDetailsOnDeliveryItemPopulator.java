package com.scl.facades.populators.order;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.scl.core.dao.DataConstraintDao;
import com.scl.core.enums.CreatedFromCRMorERP;
import com.scl.core.model.PartnerCustomerModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.SclUserModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.scl.core.enums.DeliveryItemStatus;
import com.scl.core.enums.IncoTerms;
import com.scl.core.model.DeliveryItemModel;
import com.scl.facades.order.data.TrackingCommentData;
import com.scl.facades.order.data.TrackingData;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class DefaultSCLTrackingDetailsOnDeliveryItemPopulator implements Populator<DeliveryItemModel, List<TrackingData>> {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultSCLTrackingDetailsOnDeliveryItemPopulator.class);
	private EnumerationService enumerationService;

	@Autowired
	DataConstraintDao dataConstraintDao;

	@Override
	public void populate(DeliveryItemModel source, List<TrackingData> target) throws ConversionException {

		Assert.notNull(source, "Parameter source cannot be null.");
		Assert.notNull(target, "Parameter target cannot be null.");

		OrderModel order = (OrderModel) source.getEntry().getOrder();
		OrderEntryModel entry = (OrderEntryModel) source.getEntry();

		//    	TrackingData orderRequisitionTrackingData = new TrackingData();
		//    	orderRequisitionTrackingData.setCode("Retailer Requested");
		//    	orderRequisitionTrackingData.setName("Retailer Requested");
		//    	orderRequisitionTrackingData.setActualTime(order.getDate());
		//    	orderRequisitionTrackingData.setIndex(1);
		//    	if(entry.get) {
		//    		List<TrackingCommentData> commentList = new ArrayList<>();
		//    		commentList.add(createTrackingComment(null, order.getCode()+"/"+entry.getEntryNumber(), 1, false, false));
		//
		//    		orderRequisitionTrackingData.setTrackingComments(commentList);
		//    	}
		//
		//    	target.add(orderRequisitionTrackingData);


		TrackingData orderReceivedTrackingData = new TrackingData();
		orderReceivedTrackingData.setCode(OrderStatus.ORDER_RECEIVED.getCode());
		orderReceivedTrackingData.setName("App Order Generated");
		orderReceivedTrackingData.setActualTime(order.getDate());
		orderReceivedTrackingData.setIndex(2);
		if(order.getCode()!=null && entry.getEntryNumber()!=null) {
			List<TrackingCommentData> commentList = new ArrayList<>();
			commentList.add(createTrackingComment(null, order.getCode()+"/"+entry.getEntryNumber(), 1, false, false));

			if(Objects.nonNull(order.getPlacedByCustomer())){
				commentList.add(createTrackingComment("Placed by : ", order.getPlacedByCustomer(), 2, false, false));
			}
			else if(Objects.nonNull(order.getPlacedBy())){
				if(order.getPlacedBy() instanceof SclUserModel){
					commentList.add(createTrackingComment("Placed by : ", order.getPlacedBy().getUid(), 2, false, false));
				}
				else {
					if(order.getPlacedBy() instanceof SclCustomerModel)
						commentList.add(createTrackingComment("Placed by : ", order.getPlacedBy().getName(), 2, false, false));
				}
			}
			else{
				if (Objects.nonNull(order.getCreatedFromCRMorERP())){
					String placedByErpComment = dataConstraintDao.findVersionByConstraintName("ORDER_PLACED_BY_ERP_COMMENT");
					commentList.add(createTrackingComment("Placed by : ", placedByErpComment, 2, false, false));
				}
			}
			orderReceivedTrackingData.setTrackingComments(commentList);
		}

		target.add(orderReceivedTrackingData);


		if(order.getBlockedDate()!=null) {
			TrackingData orderBlockedTrackingData = new TrackingData();
			orderBlockedTrackingData.setCode("Order Blocked");
			orderBlockedTrackingData.setName("Order Blocked");
			orderBlockedTrackingData.setActualTime(order.getBlockedDate());
			orderBlockedTrackingData.setIndex(3);
			target.add(orderBlockedTrackingData);
		}
		
		TrackingData orderAcceptedTrackingData = new TrackingData();
		orderAcceptedTrackingData.setCode(OrderStatus.ORDER_ACCEPTED.getCode());
		orderAcceptedTrackingData.setName("Order Accepted");
		orderAcceptedTrackingData.setActualTime(order.getOrderAcceptedDate());
		orderAcceptedTrackingData.setIndex(4);
		List<TrackingCommentData> commentList1 = new ArrayList<>();
		if(order.getOrderAcceptedDate()!=null && order.getErpOrderNumber()!=null && entry.getErpLineItemId()!=null && entry.getSource()!=null) {
			commentList1.add(createTrackingComment(null, order.getErpOrderNumber()+"/"+entry.getErpLineItemId(), 1, false, false));
			commentList1.add(createTrackingComment(null, entry.getSource().getCode()+" : "+entry.getSource().getName(), 2, false, false));
		}
		if(order.getOrderAcceptedDate()==null && order.getErpStatusDesc()!=null) {
			String err = order.getErpStatusDesc();
			if(entry.getErpStatusDesc()!=null) {
				err +=", " + entry.getErpStatusDesc();
			}
			commentList1.add(createTrackingComment(null, err, 2, false, false));
		}

		if(commentList1.size()>0) {
			orderAcceptedTrackingData.setTrackingComments(commentList1);
		}
		target.add(orderAcceptedTrackingData);


		/**
		 * Placed By Tracking Data

		TrackingData placedByTrackingData = new TrackingData();
		String placedBy = "Placed by : ";
		if(Objects.nonNull(order.getCreatedFromCRMorERP())){
			if(order.getCreatedFromCRMorERP().equals(CreatedFromCRMorERP.S4HANA)){
				String placeByCustomer = placedBy.concat(order.getUser().getUid());
				placedByTrackingData.setPlacedByCustomer(placeByCustomer);
			}
		}
		else if(Objects.nonNull(order.getPlacedByCustomer())){
			String placeByCustomer = placedBy.concat(order.getPlacedByCustomer());
			placedByTrackingData.setPlacedByCustomer(placeByCustomer);
		}
		if(Objects.nonNull(order.getIsPartnerCustomer())){
			placedByTrackingData.setIsPartnerCustomer(order.getIsPartnerCustomer());
		}
		target.add(placedByTrackingData);
		 */
		TrackingData diCreatedTrackingData = new TrackingData();
		diCreatedTrackingData.setCode(DeliveryItemStatus.DI_CREATED.getCode());
		diCreatedTrackingData.setName("DI Created");
		diCreatedTrackingData.setActualTime(source.getDiCreationDateAndTime());
		diCreatedTrackingData.setIndex(5);
		if(source.getDiNumber()!=null && source.getDeliveryLineNumber()!=null) {
			List<TrackingCommentData> commentList = new ArrayList<>();
			commentList.add(createTrackingComment(null, source.getDiNumber()+"/"+source.getDeliveryLineNumber(), 1, false, false));
			diCreatedTrackingData.setTrackingComments(commentList);
		}

		target.add(diCreatedTrackingData);

		TrackingData vehicaleAllocatedTrackingData = new TrackingData();
		vehicaleAllocatedTrackingData.setCode(DeliveryItemStatus.TRUCK_ALLOCATED.getCode());
		vehicaleAllocatedTrackingData.setName("Vehicle Allocated");
		vehicaleAllocatedTrackingData.setActualTime(source.getTruckAllocatedDate());
		vehicaleAllocatedTrackingData.setIndex(6);
		List<TrackingCommentData> trackingCommentList = new ArrayList<>();
//		if(source.getTruckAllocatedQty()!=null && source.getTruckAllocatedQty()>0) {
//			trackingCommentList.add(createTrackingComment("Allocated Qty", source.getTruckAllocatedQty() + " MT", 1, false, false));
//		}
//		if(source.getDiQuantity()!=null && source.getDiQuantity()>0) {
//			trackingCommentList.add(createTrackingComment("Delivery Qty", source.getDiQuantity() + " MT", 2, false, false));
//		}
		if(StringUtils.isNotBlank(source.getTruckNo())) {
			boolean isGpsEnabled = false;
			if(source.getTokenNumber()!=null) {
				isGpsEnabled = true;
            }
			trackingCommentList.add(createTrackingComment("Vehicle Number", source.getTruckNo(), 3, isGpsEnabled, true));
		}
		if(StringUtils.isNotBlank(source.getErpDriverNumber())) {
			trackingCommentList.add(createTrackingComment("Driver Number", source.getErpDriverNumber(), 4, false, true));
		}
		if(StringUtils.isNotBlank(source.getTransporterPhoneNumber())) {
			trackingCommentList.add(createTrackingComment("Transporter Number", source.getTransporterPhoneNumber(), 5, false, true));
		}
		if(StringUtils.isNotBlank(source.getTransporterName())) {
			trackingCommentList.add(createTrackingComment("Transporter Name", source.getTransporterName(), 6, false, false));
		}
		if(CollectionUtils.isNotEmpty(trackingCommentList)) {
			vehicaleAllocatedTrackingData.setTrackingComments(trackingCommentList);
		}
		target.add(vehicaleAllocatedTrackingData);


		TrackingData invoicedTrackingData = new TrackingData();
		invoicedTrackingData.setCode(DeliveryItemStatus.INVOICED.getCode());
		invoicedTrackingData.setName("Invoiced");
		invoicedTrackingData.setActualTime(source.getInvoiceCreationDateAndTime());
		invoicedTrackingData.setIndex(7);
		if(source.getInvoiceNumber()!=null) {
			List<TrackingCommentData> commentList = new ArrayList<>();
			commentList.add(createTrackingComment(null, source.getInvoiceNumber()+"/"+source.getInvoiceLineNumber(), 1, false, false));
			if(StringUtils.isNotBlank(source.getTaxInvoiceNumber()))
			commentList.add(createTrackingComment(null, source.getTaxInvoiceNumber(), 2, false, false));
			invoicedTrackingData.setTrackingComments(commentList);
		}
		target.add(invoicedTrackingData);

		TrackingData truckDispactchedTrackingData = new TrackingData();
		truckDispactchedTrackingData.setCode(DeliveryItemStatus.TRUCK_DISPATCHED.getCode());
		truckDispactchedTrackingData.setName("Vehicle Dispatched");
		truckDispactchedTrackingData.setActualTime(source.getTruckDispatchedDateAndTime());
		truckDispactchedTrackingData.setIndex(8);
		if(source.getEtaDate()!=null) {
			List<TrackingCommentData> commentList = new ArrayList<>();
			
			DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");  
			Date etaDate   = DateUtils.addHours(source.getEtaDate(), 5);
			etaDate   = DateUtils.addMinutes(etaDate, 30);
			String etaDateString = dateFormat.format(etaDate); 
			commentList.add(createTrackingComment("ETA Date", etaDateString, 1, false, false));
			if(source.getTaxInvoiceNumber()!=null) {
				commentList.add(createTrackingComment(null, source.getTaxInvoiceNumber(), 2, false, false));
			}
			truckDispactchedTrackingData.setTrackingComments(commentList);
		}
		target.add(truckDispactchedTrackingData); 

		TrackingData truckReachedTrackingData = new TrackingData();
		truckReachedTrackingData.setCode(DeliveryItemStatus.TRUCK_REACHED_DESTINATION.getCode());
		truckReachedTrackingData.setName("Vehicle Reached Destination");
		truckReachedTrackingData.setActualTime(source.getTruckReachedDate());
		truckReachedTrackingData.setIndex(9);
		List<TrackingCommentData> commentList = new ArrayList<>();
		commentList.add(createTrackingComment(null, "(Transporter Confirmation)", 1, false, false));
		truckReachedTrackingData.setTrackingComments(commentList);
		target.add(truckReachedTrackingData);

		TrackingData epodTrackingData = new TrackingData();
		epodTrackingData.setCode(DeliveryItemStatus.DELIVERED.getCode());
		epodTrackingData.setName("EPOD");
		epodTrackingData.setActualTime(source.getDeliveredDate());
		epodTrackingData.setIndex(10);
		List<TrackingCommentData> commentListforEpod = new ArrayList<>();
		if(source.getTaxInvoiceNumber()!=null) {
			commentListforEpod.add(createTrackingComment(null, source.getTaxInvoiceNumber(), 1, false, false));
		}
		epodTrackingData.setTrackingComments(commentListforEpod);

		target.add(epodTrackingData);

		if(source.getCancelledDate()!=null) {
			TrackingData cancelledTrackingData = new TrackingData();
			cancelledTrackingData.setCode(DeliveryItemStatus.CANCELLED.getCode());
			cancelledTrackingData.setName("Cancelled");
			cancelledTrackingData.setActualTime(source.getCancelledDate());
			cancelledTrackingData.setIndex(11);
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
