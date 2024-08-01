package com.scl.facades.populators.order;

import com.scl.core.model.OrderRequisitionModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.SclUserModel;
import com.scl.core.services.SCLProductService;
import com.scl.core.services.TerritoryManagementService;
import com.scl.core.services.TerritoryMasterService;
import com.scl.facades.order.data.SCLOrderData;
import com.scl.facades.order.data.SclOrderHistoryData;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.commercefacades.product.PriceDataFactory;
import de.hybris.platform.commercefacades.product.data.ImageData;
import de.hybris.platform.commercefacades.product.data.PriceDataType;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class SclOrderEntryHistoryCardPopulator implements Populator<OrderEntryModel, SclOrderHistoryData> {

    @Autowired
    Converter<AddressModel, AddressData> addressConverter;

    @Autowired
    private PriceDataFactory priceDataFactory;

    @Autowired
    Converter<MediaModel, ImageData> imageConverter;
    @Autowired
    TerritoryManagementService territoryService;

    @Autowired
    EnumerationService enumerationService;
    
    @Autowired
    SCLProductService sclProductService;
    
    @Autowired
    TerritoryMasterService territoryMasterService;
    
    @Override
    public void populate(OrderEntryModel source, SclOrderHistoryData target) throws ConversionException {
        Assert.notNull(source, "Parameter source cannot be null.");
        Assert.notNull(target, "Parameter target cannot be null.");

        if(null != source) {
            final OrderModel order = source.getOrder();

            if( order.getStatus()!=null && order.getStatus().equals(OrderStatus.ERROR_IN_ERP)){
                target.setIsErrorInERP(Boolean.TRUE);
            }
            target.setEntryNumber(source.getEntryNumber());
            target.setOrderNo(order.getCode());
            target.setDealerCode(order.getUser().getUid());
            target.setDealerName(order.getUser().getName());
            target.setDealerContactNumber(((B2BCustomerModel)order.getUser()).getMobileNumber());
            if(source.getProduct()!=null) {
            	target.setProductCode(source.getProduct().getCode());
            	target.setProductName(source.getProduct().getName());
            	if (source.getDeliveryAddress() != null && source.getDeliveryAddress().getGeographicalMaster()!=null) {
            		String aliasName = sclProductService.getProductAlias(source.getProduct(), 
            				source.getDeliveryAddress().getGeographicalMaster().getState(), 
            				source.getDeliveryAddress().getGeographicalMaster().getDistrict());
            		if(StringUtils.isNotBlank(aliasName)) {
            			target.setProductName(aliasName);
            		}
            	}
            }
            target.setCancelReason(source.getCancelReason());
            target.setCancelledQTY(source.getRemainingQuantity());
            
            if(Objects.nonNull(source.getRejectedReason())){
                target.setRejectionReason(source.getRejectedReason().getCode());
            }
        
            if(source.getRemainingQuantity()!=null) {
            	target.setQuantity(source.getRemainingQuantity());
            }
            if(source.getStatus()!=null) {
            	target.setStatus(enumerationService.getEnumerationName(source.getStatus())!=null
            			?enumerationService.getEnumerationName(source.getStatus())
            					:source.getStatus().getCode());
            }
            if (source.getDeliveryAddress() != null) {
            	target.setDeliveryAddress(addressConverter.convert(source.getDeliveryAddress()));
            }
            if(order.getSubAreaMaster()!=null) {
            	target.setSubAreaMasterId(order.getSubAreaMaster().getPk().toString());
            }

            if(source.getCancelledDate()!=null) {
                target.setCancelledDate(source.getCancelledDate());
            }

            target.setErpOrderNo(String.valueOf(order.getErpOrderNumber()));
            target.setErpLineItemId(source.getErpLineItemId());
            if(source.getSource()!=null) {
            	target.setSourceCode(source.getSource().getCode());
            	target.setSourceName(source.getSource().getName());
            }

            target.setOrderDate(source.getLatestStatusUpdate());

            if (null != order.getUser().getProfilePicture()) {
                populateProfilePicture(order.getUser().getProfilePicture(), target);
            }
    		target.setSpApprovalStatus(order.getSpApprovalStatus()!=null?order.getSpApprovalStatus().getCode():"");
            target.setExpectedDeliveryDate(source.getExpectedDeliveryDate());
            if(order.getPlacedBy()!=null && order.getPlacedBy() instanceof SclUserModel){
                target.setSoContactNumber(((SclUserModel) order.getPlacedBy()).getMobileNumber());
            }
            //For SAP order need to check is placedBY setting or not
            /*else{
            	try {
            		SclUserModel salesOfficer = territoryMasterService.getUserByTerritory(((SclCustomerModel)order.getUser()).getTerritoryCode());
            		if(!Objects.isNull(salesOfficer)){
            			target.setSoContactNumber(salesOfficer.getMobileNumber());
            		}
            	}
            	catch(Exception e) {

            	}
            }*/

            if(CollectionUtils.isNotEmpty(source.getRequisitions())) {
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
            /*if(source.getTruckNo()!=null)
            {
                target.setTruckNumber(source.getTruckNo());
            }
            target.setErpVehicleNumber(StringUtils.isNotBlank(source.getErpTruckNumber())?source.getErpTruckNumber(): Strings.EMPTY);*/
        }
        //Estimated arrival time
    }


    private void populateProfilePicture(final MediaModel profilePicture, final SclOrderHistoryData sclOrderHistoryData) {
        final ImageData profileImageData = imageConverter.convert(profilePicture);
        sclOrderHistoryData.setDealerProfilePicture(profileImageData);
    }
}
