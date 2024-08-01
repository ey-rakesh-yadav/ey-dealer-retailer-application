package com.scl.facades.populators.order;

import com.scl.core.model.DeliveryItemModel;
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

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class SclDeliveryItemHistoryCardPopulator implements Populator<DeliveryItemModel, SclOrderHistoryData> {

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
    public void populate(DeliveryItemModel source, SclOrderHistoryData target) throws ConversionException {
        Assert.notNull(source, "Parameter source cannot be null.");
        Assert.notNull(target, "Parameter target cannot be null.");

        if(null != source) {
            final OrderModel order = (OrderModel) source.getEntry().getOrder();
            final OrderEntryModel entry = (OrderEntryModel) source.getEntry();

            target.setEntryNumber(entry.getEntryNumber());
            target.setOrderNo(order.getCode());
            target.setDeliveryLineNumber(source.getDeliveryLineNumber());
            target.setDiNumber(source.getDiNumber());
            target.setInvoiceNumber(source.getInvoiceNumber());
            target.setInvoiceLineNumber(source.getInvoiceLineNumber());
            target.setDealerCode(order.getUser().getUid());
            target.setDealerName(order.getUser().getName());
            target.setDealerContactNumber(((B2BCustomerModel)order.getUser()).getMobileNumber());
            if(entry.getProduct()!=null) {
            	target.setProductCode(entry.getProduct().getCode());
            	target.setProductName(entry.getProduct().getName());
            	if (entry.getDeliveryAddress() != null && entry.getDeliveryAddress().getGeographicalMaster()!=null) {
            		String aliasName = sclProductService.getProductAlias(entry.getProduct(), 
            				entry.getDeliveryAddress().getGeographicalMaster().getState(), 
            				entry.getDeliveryAddress().getGeographicalMaster().getDistrict());
            		if(StringUtils.isNotBlank(aliasName)) {
            			target.setProductName(aliasName);
            		}
            	}
            }
            target.setQuantity(source.getDiQuantity());
            if(source.getStatus()!=null) {
            	target.setStatus(enumerationService.getEnumerationName(source.getStatus())!=null
            			?enumerationService.getEnumerationName(source.getStatus())
            					:source.getStatus().getCode());
            }
            if (entry.getDeliveryAddress() != null) {
            	target.setDeliveryAddress(addressConverter.convert(entry.getDeliveryAddress()));
            }
            if(order.getSubAreaMaster()!=null) {
            	target.setSubAreaMasterId(order.getSubAreaMaster().getPk().toString());
            }
//            target.setVehicleArrivalTime(entry.getVehicleArrivalTime());
            target.setTransporterName(source.getTransporterName());
//            target.setDriverName(entry.getDriverName());
            //target.setTruckNumber(source.getTruckNo());
            target.setTransporterPhone(source.getTransporterPhoneNumber());
            target.setDriverPhone(source.getErpDriverNumber());
           target.setErpVehicleNumber(StringUtils.isNotBlank(source.getTruckNo())?source.getTruckNo(): Strings.EMPTY);

            if(source.getDeliveredDate()!=null)
            {
            	target.setDeliveredDate(source.getDeliveredDate());
            }
            if(source.getEtaDate() != null) {
                target.setErpEtaDate(source.getEtaDate());
            }
            if(source.getCancelledDate()!=null) {
                target.setCancelledDate(source.getCancelledDate());
            }

            target.setErpOrderNo(String.valueOf(order.getErpOrderNumber()));
            target.setErpLineItemId(entry.getErpLineItemId());
            target.setOrderDate(source.getLatestStatusUpdate());
            
            target.setShortageQuantity(source.getShortageQuantity());
            target.setEpodInitiateDate(source.getEpodInitiateDate());
            if(entry.getSource()!=null) {
            	target.setSourceCode(entry.getSource().getCode());
            	target.setSourceName(entry.getSource().getName());
            }

            if(source.getTokenNumber()!=null) {
            	target.setIsGpsEnabled(source.getIsGpsEnabled());
                target.setTokenNumber(source.getTokenNumber());
            }
            else {
            	target.setIsGpsEnabled(source.getIsGpsEnabled());
            }
            
            if (null != order.getUser().getProfilePicture()) {
                populateProfilePicture(order.getUser().getProfilePicture(), target);
            }
    		target.setSpApprovalStatus(order.getSpApprovalStatus()!=null?order.getSpApprovalStatus().getCode():"");
            target.setExpectedDeliveryDate(entry.getExpectedDeliveryDate());
            if(order.getPlacedBy()!=null && order.getPlacedBy() instanceof SclUserModel){
                target.setSoContactNumber(((SclUserModel) order.getPlacedBy()).getMobileNumber());
            }
            //For SAP order need to check is placedBY setting or not
           /* else{
            	try {
            		SclUserModel salesOfficer = territoryMasterService.getUserByTerritory(((SclCustomerModel)order.getUser()).getTerritoryCode());
            		if(!Objects.isNull(salesOfficer)){
            			target.setSoContactNumber(salesOfficer.getMobileNumber());
            		}
            	}
            	catch(Exception e) {

            	}
            }*/
            if((source.getEntry()!=null && source.getEntry().getRequisitions()!=null) && !source.getEntry().getRequisitions().isEmpty()) {
                String requisitionId = "";
                for(OrderRequisitionModel orderRequisitionModel : source.getEntry().getRequisitions()) {
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
    }


    private void populateProfilePicture(final MediaModel profilePicture, final SclOrderHistoryData sclOrderHistoryData) {
        final ImageData profileImageData = imageConverter.convert(profilePicture);
        sclOrderHistoryData.setDealerProfilePicture(profileImageData);
    }
}
