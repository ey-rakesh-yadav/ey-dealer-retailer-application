package com.eydms.facades.populators.order;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.util.Assert;

import com.eydms.core.enums.IncoTerms;
import com.eydms.facades.order.data.TrackingData;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class DefaultEYDMSTrackingDetailsOnEntryPopulator implements Populator<OrderEntryModel, List<TrackingData>> {


    private EnumerationService enumerationService;

    @Override
    public void populate(OrderEntryModel source, List<TrackingData> target) throws ConversionException {

    	Assert.notNull(source, "Parameter source cannot be null.");
    	Assert.notNull(target, "Parameter target cannot be null.");

    	TrackingData truckAllocatedTrackingData = new TrackingData();
    	truckAllocatedTrackingData.setCode(OrderStatus.TRUCK_ALLOCATED.getCode());
    	truckAllocatedTrackingData.setName(getEnumerationService().getEnumerationName(OrderStatus.TRUCK_ALLOCATED));
    	truckAllocatedTrackingData.setActualTime(source.getTruckAllocatedDate());
    	truckAllocatedTrackingData.setIndex(3);

    	List<String> comments = new ArrayList<String>();
    	if(source.getTruckAllocatedQty()!=null && source.getTruckAllocatedQty()>0)
    		comments.add("Truck Allocated Quantity : " + source.getTruckAllocatedQty());
    	if(source.getDeliveryQty()!=null && source.getDeliveryQty()>0)
    		comments.add("Delivery Quantity : " + source.getDeliveryQty());
    	if(StringUtils.isNotBlank(source.getErpTruckNumber()))
    		comments.add("Vehicle number : " + source.getErpTruckNumber());
    	if(StringUtils.isNotBlank(source.getErpDriverNumber()))
    		comments.add("Driver contact Number : " + source.getErpDriverNumber());
    	if(StringUtils.isNotBlank(source.getTransporterName()))
    		comments.add("Transporter Name : " + source.getTransporterName());
    	if(StringUtils.isNotBlank(source.getTransporterPhoneNumber()))
    		comments.add("Transporter Number : " + source.getTransporterPhoneNumber());
    	truckAllocatedTrackingData.setComment(comments);

    	target.add(truckAllocatedTrackingData);

    	TrackingData truckDispatchedTrackingData = new TrackingData();
    	truckDispatchedTrackingData.setCode(OrderStatus.TRUCK_DISPATCHED.getCode());
    	truckDispatchedTrackingData.setName(getEnumerationService().getEnumerationName(OrderStatus.TRUCK_DISPATCHED));
    	truckDispatchedTrackingData.setActualTime(source.getTruckDispatcheddate());
    	truckDispatchedTrackingData.setIndex(4);
    	target.add(truckDispatchedTrackingData);

    	if(source.getFob()!=null && source.getFob().equals(IncoTerms.FOR)) {
    		TrackingData truckReachedTrackingData = new TrackingData();
    		truckReachedTrackingData.setCode(OrderStatus.TRUCK_REACHED_DESTINATION.getCode());
    		truckReachedTrackingData.setName(getEnumerationService().getEnumerationName(OrderStatus.TRUCK_REACHED_DESTINATION));
    		truckReachedTrackingData.setActualTime(source.getTruckReachedDate());
    		truckReachedTrackingData.setIndex(5);
    		List<String> epodComments = new ArrayList<String>();
    		if(source.getTruckReachedDate()!=null) {
    			DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");  
    			Date truckRechedDate   = DateUtils.addHours(source.getTruckReachedDate(), 5);
    			truckRechedDate   = DateUtils.addMinutes(truckRechedDate, 30);
    			String truckReached = dateFormat.format(truckRechedDate); 
    			epodComments.add("Truck arrival confirmed time : " + truckReached);
    		}
    		if(source.getEpodInitiateDate()!=null) {
    			DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");  
    			Date epodDate   = DateUtils.addHours(source.getEpodInitiateDate(), 5);
    			epodDate   = DateUtils.addMinutes(epodDate, 30);
    			String epod = dateFormat.format(epodDate); 
    			epodComments.add("EPOD Initiated time : " + epod);
    		}
    		if(epodComments.size()>0) {
    			truckReachedTrackingData.setComment(epodComments);
    		}

      		target.add(truckReachedTrackingData);
    	}

    	TrackingData deliveredTrackingData = new TrackingData();
    	deliveredTrackingData.setCode(OrderStatus.DELIVERED.getCode());
    	deliveredTrackingData.setName(getEnumerationService().getEnumerationName(OrderStatus.DELIVERED));
    	deliveredTrackingData.setActualTime(source.getDeliveredDate());
    	deliveredTrackingData.setIndex(6);
    	target.add(deliveredTrackingData);

    	TrackingData cancelledTrackingData = new TrackingData();
    	cancelledTrackingData.setCode(OrderStatus.CANCELLED.getCode());
    	cancelledTrackingData.setName(getEnumerationService().getEnumerationName(OrderStatus.CANCELLED));
    	cancelledTrackingData.setActualTime(source.getCancelledDate());
    	cancelledTrackingData.setIndex(7);
    	target.add(cancelledTrackingData);

    }
    public EnumerationService getEnumerationService() {
        return enumerationService;
    }

    public void setEnumerationService(EnumerationService enumerationService) {
        this.enumerationService = enumerationService;
    }
}
