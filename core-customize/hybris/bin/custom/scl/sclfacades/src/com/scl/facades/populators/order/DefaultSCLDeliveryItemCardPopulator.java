package com.scl.facades.populators.order;

import com.scl.core.model.DeliveryItemModel;
import com.scl.core.model.OrderRequisitionModel;
import com.scl.core.services.SCLProductService;
import com.scl.facades.order.data.SCLOrderData;
import com.scl.facades.order.data.SclOrderHistoryData;
import com.scl.facades.order.data.TrackingData;

import de.hybris.platform.commercefacades.product.data.ImageData;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.PrincipalData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DefaultSCLDeliveryItemCardPopulator implements Populator<DeliveryItemModel, SCLOrderData> {

    private Converter<ProductModel, ProductData> productConverter;
    private Converter<AddressModel, AddressData> addressConverter;
    private Populator<DeliveryItemModel, List<TrackingData>> trackingDetailsOnDeliveryItemPopulator;
    
    @Autowired
    EnumerationService enumerationService;
    
    @Autowired
    Converter<MediaModel, ImageData> imageConverter;
    
    
    @Autowired
    SCLProductService sclProductService;
    

    public Populator<DeliveryItemModel, List<TrackingData>> getTrackingDetailsOnDeliveryItemPopulator() {
		return trackingDetailsOnDeliveryItemPopulator;
	}

	public void setTrackingDetailsOnDeliveryItemPopulator(
			Populator<DeliveryItemModel, List<TrackingData>> trackingDetailsOnDeliveryItemPopulator) {
		this.trackingDetailsOnDeliveryItemPopulator = trackingDetailsOnDeliveryItemPopulator;
	}

	@Override
    public void populate(DeliveryItemModel source, SCLOrderData target) throws ConversionException {

        Assert.notNull(source, "Parameter source cannot be null.");
        Assert.notNull(target, "Parameter target cannot be null.");

        final OrderModel orderForEntry = (OrderModel) source.getEntry().getOrder();
        final OrderEntryModel entry = (OrderEntryModel) source.getEntry();

        if( orderForEntry.getStatus()!=null && orderForEntry.getStatus().equals(OrderStatus.ERROR_IN_ERP)){
            target.setIsErrorInERP(Boolean.TRUE);
        }
        target.setEntryNumber(String.valueOf(entry.getEntryNumber()));
        target.setCode(orderForEntry.getCode());
        target.setQuantity(source.getDiQuantity());
        if(source.getStatus()!=null) {
        	target.setStatus(enumerationService.getEnumerationName(source.getStatus())!=null
        			?enumerationService.getEnumerationName(source.getStatus())
        					:source.getStatus().getCode());
        }
        if(null != entry.getProduct()){
        	ProductData productData = getProductInfo(entry.getProduct());
        	if (entry.getDeliveryAddress() != null && entry.getDeliveryAddress().getGeographicalMaster()!=null) {
        		String aliasName = sclProductService.getProductAlias(entry.getProduct(), 
        				entry.getDeliveryAddress().getGeographicalMaster().getState(), 
        				entry.getDeliveryAddress().getGeographicalMaster().getDistrict());
        		if(StringUtils.isNotBlank(aliasName)) {
        			productData.setName(aliasName);
        		}
        	}
            target.setProducts(List.of(productData));
        }
        if(null != entry.getDeliveryAddress()){
            populateDeliveryAddress(entry.getDeliveryAddress(),target);
        }
        if(orderForEntry.getSubAreaMaster()!=null) {
        	target.setSubAreaMasterId(orderForEntry.getSubAreaMaster().getPk().toString());
        }
        target.setEstimatedDeliveryDate(orderForEntry.getEstimatedDeliveryDate());

        target.setDiNumber(source.getDiNumber());
        target.setDeliveryLineNumber(source.getDeliveryLineNumber());
        target.setInvoiceNumber(source.getInvoiceNumber());
        target.setInvoiceLineNumber(source.getInvoiceLineNumber());
        
        target.setTransporterName(source.getTransporterName());
        if(source.getIsGpsEnabled()!=null && source.getIsGpsEnabled()) {
        	target.setTruckNumber(source.getTruckNo());
        	target.setTokenNumber(source.getTokenNumber());
        }
        target.setTransporterPhone(source.getTransporterPhoneNumber());
        target.setDriverPhone(source.getErpDriverNumber());
        target.setDispatchQty(source.getInvoiceQuantity());
        target.setShortageQty(source.getShortageQuantity());
        
        List<TrackingData> trackingDataList = new ArrayList<>();
        getTrackingDetailsOnDeliveryItemPopulator().populate(source,trackingDataList);
        

        target.setTrackingDetails(trackingDataList);
        target.setExpectedDeliveryDate(entry.getExpectedDeliveryDate());

        target.setErpLineItemId(entry.getErpLineItemId());
        if(StringUtils.isNotBlank(orderForEntry.getErpOrderNumber())){
            target.setErpOrderNo(String.valueOf(orderForEntry.getErpOrderNumber()));
        }
        if (null != orderForEntry.getUser().getProfilePicture()) {
            populateProfilePicture(orderForEntry.getUser().getProfilePicture(), target);
        }

        if(source.getEtaDate()!=null) {
            target.setErpEtaDate(source.getEtaDate());
        }
        if(null!= orderForEntry.getPlacedByCustomer()){
            target.setPlacedByCustomer(orderForEntry.getPlacedByCustomer());
        }
        if(Objects.nonNull(orderForEntry.getIsPartnerCustomer()))
        {
            target.setIsPartnerCustomer(orderForEntry.getIsPartnerCustomer());
        }
        PrincipalData user = new PrincipalData();
        user.setUid(orderForEntry.getUser().getUid());
        user.setName(orderForEntry.getUser().getName());
        target.setUser(user);        

        if((orderForEntry.getEntries()!=null && orderForEntry.getEntries().get(0).getRequisitions()!=null) && !orderForEntry.getEntries().get(0).getRequisitions().isEmpty()) {
            String requisitionId = "";
            for(OrderRequisitionModel orderRequisitionModel : orderForEntry.getEntries().get(0).getRequisitions()) {
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

    private void populateDeliveryAddress(final AddressModel deliveryAddress , final SCLOrderData target){
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

    
    private void populateProfilePicture ( final MediaModel profilePicture,
    final SCLOrderData sclOrderData){
        final ImageData profileImageData = imageConverter.convert(profilePicture);
        sclOrderData.setDealerProfilePicture(profileImageData);
    }
}
