package com.scl.facades.populators.order;

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
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DefaultSCLOrderEntryCardPopulator implements Populator<OrderEntryModel, SCLOrderData> {
    private static final Logger LOG = Logger.getLogger(DefaultSCLOrderEntryCardPopulator.class);
    private Converter<ProductModel, ProductData> productConverter;
    private Converter<AddressModel, AddressData> addressConverter;
    private Populator<OrderEntryModel, List<TrackingData>> trackingDetailsOnEntryPopulator;
    private Populator<OrderModel, List<TrackingData>> trackingDetailsOnOrderPopulator;
    
    @Autowired
    Converter<MediaModel, ImageData> imageConverter;

    @Autowired
    EnumerationService enumerationService;
    
    @Autowired
    SCLProductService sclProductService;
    
    @Override
    public void populate(OrderEntryModel source, SCLOrderData target) throws ConversionException {

        Assert.notNull(source, "Parameter source cannot be null.");
        Assert.notNull(target, "Parameter target cannot be null.");

        final OrderModel orderForEntry = source.getOrder();

        if( orderForEntry.getStatus()!=null && orderForEntry.getStatus().equals(OrderStatus.ERROR_IN_ERP)){
            target.setIsErrorInERP(Boolean.TRUE);
        }
        target.setEntryNumber(String.valueOf(source.getEntryNumber()));
        target.setCode(orderForEntry.getCode());
        target.setQuantity(source.getRemainingQuantity());
        if(source.getStatus()!=null) {
        	target.setStatus(enumerationService.getEnumerationName(source.getStatus())!=null
        			?enumerationService.getEnumerationName(source.getStatus())
        					:source.getStatus().getCode());
        }
        if(null != source.getProduct()){
        	ProductData productData = getProductInfo(source.getProduct());
        	if (source.getDeliveryAddress() != null && source.getDeliveryAddress().getGeographicalMaster()!=null) {
        		String aliasName = sclProductService.getProductAlias(source.getProduct(), 
        				source.getDeliveryAddress().getGeographicalMaster().getState(), 
        				source.getDeliveryAddress().getGeographicalMaster().getDistrict());
        		if(StringUtils.isNotBlank(aliasName)) {
        			productData.setName(aliasName);
        		}
        	}
            target.setProducts(List.of(productData));
        }
        if(null != source.getDeliveryAddress()){
            populateDeliveryAddress(source.getDeliveryAddress(),target);
        }
        if(orderForEntry.getSubAreaMaster()!=null) {
        	target.setSubAreaMasterId(orderForEntry.getSubAreaMaster().getPk().toString());
        }
        target.setEstimatedDeliveryDate(orderForEntry.getEstimatedDeliveryDate());
        
        List<TrackingData> trackingDataList = new ArrayList<>();
        //getTrackingDetailsOnOrderPopulator().populate(orderForEntry,trackingDataList);
        getTrackingDetailsOnEntryPopulator().populate(source,trackingDataList);
        
        target.setTrackingDetails(trackingDataList);
        target.setExpectedDeliveryDate(source.getExpectedDeliveryDate());
        
        target.setErpLineItemId(source.getErpLineItemId());
        if(StringUtils.isNotBlank(orderForEntry.getErpOrderNumber())){
            target.setErpOrderNo(String.valueOf(orderForEntry.getErpOrderNumber()));
        }
        if (null != orderForEntry.getUser().getProfilePicture()) {
            populateProfilePicture(orderForEntry.getUser().getProfilePicture(), target);
        }
        if(null!= orderForEntry.getPlacedByCustomer()){
            target.setPlacedByCustomer(orderForEntry.getPlacedByCustomer());
            LOG.info(String.format("****Placed By Customer**** %s",target.getPlacedByCustomer()));

        }
        if(Objects.nonNull(orderForEntry.getIsPartnerCustomer()))
        {
            target.setIsPartnerCustomer(orderForEntry.getIsPartnerCustomer());
        }

        PrincipalData user = new PrincipalData();
        user.setUid(orderForEntry.getUser().getUid());
        user.setName(orderForEntry.getUser().getName());
        target.setUser(user);
        
        if((source.getRequisitions()!=null) && !source.getRequisitions().isEmpty()) {
            String requisitionId = "";
            for(OrderRequisitionModel orderRequisitionModel : source.getRequisitions()) {
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
    final SCLOrderData sclOrderData){
        final ImageData profileImageData = imageConverter.convert(profilePicture);
        sclOrderData.setDealerProfilePicture(profileImageData);
    }
}
