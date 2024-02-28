package com.eydms.facades.populators.order;

import com.eydms.core.model.OrderRequisitionModel;
import com.eydms.facades.order.data.EYDMSOrderData;
import com.eydms.facades.order.data.EyDmsOrderHistoryData;
import com.eydms.facades.order.data.TrackingData;

import de.hybris.platform.commercefacades.product.data.ImageData;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.PrincipalData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DefaultEYDMSOrderEntryCardPopulator implements Populator<OrderEntryModel, EYDMSOrderData> {

    private Converter<ProductModel, ProductData> productConverter;
    private Converter<AddressModel, AddressData> addressConverter;
    private Populator<OrderEntryModel, List<TrackingData>> trackingDetailsOnEntryPopulator;
    private Populator<OrderModel, List<TrackingData>> trackingDetailsOnOrderPopulator;
    
    @Autowired
    Converter<MediaModel, ImageData> imageConverter;

    @Override
    public void populate(OrderEntryModel source, EYDMSOrderData target) throws ConversionException {

        Assert.notNull(source, "Parameter source cannot be null.");
        Assert.notNull(target, "Parameter target cannot be null.");

        final OrderModel orderForEntry = source.getOrder();

        
        target.setEntryNumber(String.valueOf(source.getEntryNumber()));
        target.setCode(orderForEntry.getCode());
        target.setQuantity(source.getQuantityInMT());
        target.setStatus(source.getStatusDisplay());
        if(null != source.getProduct()){
            target.setProducts(List.of(getProductInfo(source.getProduct())));
        }
        if(null != orderForEntry.getDeliveryAddress()){
            populateDeliveryAddress(orderForEntry.getDeliveryAddress(),target);
        }
        if(orderForEntry.getSubAreaMaster()!=null) {
        	target.setSubAreaMasterId(orderForEntry.getSubAreaMaster().getPk().toString());
        }
        target.setEstimatedDeliveryDate(orderForEntry.getEstimatedDeliveryDate());

        target.setTransporterName(source.getTransporterName());
        target.setTruckNumber(source.getErpTruckNumber());
        target.setTransporterPhone(source.getTransporterPhoneNumber());
        target.setDriverPhone(source.getErpDriverNumber());
        target.setDispatchQty(source.getInvoiceQuantity());
        target.setShortageQty(source.getShortageQuantity());
        
        List<TrackingData> trackingDataList = new ArrayList<>();
        getTrackingDetailsOnOrderPopulator().populate(orderForEntry,trackingDataList);
        getTrackingDetailsOnEntryPopulator().populate(source,trackingDataList);
        
        if(StringUtils.isBlank(source.getErpLineItemId()) && StringUtils.isNotBlank(source.getErpStatusDesc())) {
        	Optional<TrackingData> orderAcceptedTrackingData = trackingDataList.stream().filter(data->data.getIndex()!=null && data.getIndex()==2).findFirst();
        	if(orderAcceptedTrackingData.isPresent()) {
        		TrackingData orderAcceptedData = orderAcceptedTrackingData.get();
        		if(orderAcceptedData.getComment()!=null)
        			orderAcceptedData.getComment().add(source.getErpStatusDesc());
        		else {
        			List<String>  comments = new ArrayList<>();
        			comments.add(source.getErpStatusDesc());
        			orderAcceptedData.setComment(comments);
        		}
        	}
        }
        target.setTrackingDetails(trackingDataList);
        target.setExpectedDeliveryDate(source.getExpectedDeliveryDate());
        target.setErpVehicleNumber(source.getErpTruckNumber());
        if(source.getTokenNumber()!=null) {
        	target.setIsGpsEnabled(true);
        }
        else {
        	target.setIsGpsEnabled(false);
        }
        target.setErpLineItemId(source.getErpLineItemId());
        if(StringUtils.isNotBlank(orderForEntry.getErpOrderNumber())){
            target.setErpOrderNo(String.valueOf(orderForEntry.getErpOrderNumber()));
        }
        if (null != orderForEntry.getUser().getProfilePicture()) {
            populateProfilePicture(orderForEntry.getUser().getProfilePicture(), target);
        }

        if(source.getEtaDate()!=null) {
            target.setErpEtaDate(source.getEtaDate());
        }

        PrincipalData user = new PrincipalData();
        user.setUid(orderForEntry.getUser().getUid());
        user.setName(orderForEntry.getUser().getName());
        target.setUser(user);
        
		if(orderForEntry.getDestination()!=null) {
			target.setDestinationCode(orderForEntry.getDestination().getCode());
			target.setDestinationName(orderForEntry.getDestination().getName());
		}

        if(orderForEntry.getRequisitions()!=null && !orderForEntry.getRequisitions().isEmpty()) {
            String requisitionId = "";
            for(OrderRequisitionModel orderRequisitionModel : orderForEntry.getRequisitions()) {
                if(requisitionId.isEmpty()) {
                    requisitionId = orderRequisitionModel.getRequisitionId();
                }
                else {
                    requisitionId = requisitionId + "," + orderRequisitionModel.getRequisitionId();
                }
            }
            target.setOrderRequisitionId(requisitionId);
        }

    }

    private void populateDeliveryAddress(final AddressModel deliveryAddress , final EYDMSOrderData target){
        target.setDeliveryAddress(getAddressConverter().convert(deliveryAddress));
    }
    private ProductData getProductInfo(final ProductModel productModel){

        return getProductConverter().convert(productModel);
    }

    public Converter<ProductModel, ProductData> getProductConverter() {
        return productConverter;
    }

    public void setProductConverter(Converter<ProductModel, ProductData> productConverter) {
        this.productConverter = productConverter;
    }

    public Converter<AddressModel, AddressData> getAddressConverter() {
        return addressConverter;
    }

    public void setAddressConverter(Converter<AddressModel, AddressData> addressConverter) {
        this.addressConverter = addressConverter;
    }
    public Populator<OrderEntryModel, List<TrackingData>> getTrackingDetailsOnEntryPopulator() {
        return trackingDetailsOnEntryPopulator;
    }

    public void setTrackingDetailsOnEntryPopulator(Populator<OrderEntryModel, List<TrackingData>> trackingDetailsOnEntryPopulator) {
        this.trackingDetailsOnEntryPopulator = trackingDetailsOnEntryPopulator;
    }

    public Populator<OrderModel, List<TrackingData>> getTrackingDetailsOnOrderPopulator() {
        return trackingDetailsOnOrderPopulator;
    }

    public void setTrackingDetailsOnOrderPopulator(Populator<OrderModel, List<TrackingData>> trackingDetailsOnOrderPopulator) {
        this.trackingDetailsOnOrderPopulator = trackingDetailsOnOrderPopulator;
    }
    
    private void populateProfilePicture ( final MediaModel profilePicture,
    final EYDMSOrderData eydmsOrderData){
        final ImageData profileImageData = imageConverter.convert(profilePicture);
        eydmsOrderData.setDealerProfilePicture(profileImageData);
    }
}
