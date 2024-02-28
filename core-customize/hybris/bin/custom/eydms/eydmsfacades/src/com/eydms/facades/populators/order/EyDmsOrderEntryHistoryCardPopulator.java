package com.eydms.facades.populators.order;

import com.eydms.core.model.OrderRequisitionModel;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.facades.order.data.EYDMSOrderData;
import com.eydms.facades.order.data.EyDmsOrderHistoryData;

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
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class EyDmsOrderEntryHistoryCardPopulator implements Populator<OrderEntryModel, EyDmsOrderHistoryData> {

    @Autowired
    Converter<AddressModel, AddressData> addressConverter;

    @Autowired
    private PriceDataFactory priceDataFactory;

    @Autowired
    Converter<MediaModel, ImageData> imageConverter;
    @Autowired
    TerritoryManagementService territoryService;


    @Override
    public void populate(OrderEntryModel source, EyDmsOrderHistoryData target) throws ConversionException {
        Assert.notNull(source, "Parameter source cannot be null.");
        Assert.notNull(target, "Parameter target cannot be null.");

        if(null != source) {
            final OrderModel order = source.getOrder();

            target.setEntryNumber(source.getEntryNumber());
            target.setOrderNo(order.getCode());
            target.setDealerCode(order.getUser().getUid());
            target.setDealerName(order.getUser().getName());
            target.setDealerContactNumber(((B2BCustomerModel)order.getUser()).getMobileNumber());
            if(source.getProduct()!=null) {
            	target.setProductCode(source.getProduct().getCode());
            	target.setProductName(source.getProduct().getName());
            }
            target.setQuantity(source.getQuantityInMT());
            target.setStatus(source.getStatusDisplay());
            if (order.getDeliveryAddress() != null) {
            	target.setDeliveryAddress(addressConverter.convert(order.getDeliveryAddress()));
            }
            if(order.getSubAreaMaster()!=null) {
            	target.setSubAreaMasterId(order.getSubAreaMaster().getPk().toString());
            }
            target.setVehicleArrivalTime(source.getVehicleArrivalTime());
            target.setTransporterName(source.getTransporterName());
            target.setDriverName(source.getDriverName());
            target.setTruckNumber(source.getErpTruckNumber());
            target.setTransporterPhone(source.getTransporterPhoneNumber());
            target.setDriverPhone(source.getErpDriverNumber());

            if (source.getTotalPrice() != null) {
                BigDecimal totalPrice = BigDecimal.valueOf(source.getTotalPrice().doubleValue());
                if (Boolean.TRUE.equals(order.getNet())) {
                    totalPrice = totalPrice.add(BigDecimal.valueOf(order.getTotalTax().doubleValue()));
                }
                target.setTotalPrice(priceDataFactory.create(PriceDataType.BUY, totalPrice, order.getCurrency()));
            }
            if(source.getStatus().equals(OrderStatus.DELIVERED))
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
            target.setErpLineItemId(source.getErpLineItemId());
        /*try {
            Date orderDate = new SimpleDateFormat("dd MMMM yyyy HH:mm").parse(String.valueOf(order.getDate()));
            target.setOrderDate(orderDate);
        }
        catch (ParseException e)
        {
            throw new IllegalArgumentException("Date is not in the correct format: " + e.getMessage());
        }*/
            target.setOrderDate(order.getDate());
            target.setShortageQuantity(source.getShortageQuantity());
            target.setEpodInitiateDate(source.getEpodInitiateDate());
            target.setErpVehicleNumber(source.getErpTruckNumber());
            if(source.getTokenNumber()!=null) {
            	target.setIsGpsEnabled(true);
            }
            else {
            	target.setIsGpsEnabled(false);
            }
            if (null != order.getUser().getProfilePicture()) {
                populateProfilePicture(order.getUser().getProfilePicture(), target);
            }
    		if(order.getDestination()!=null) {
    			target.setDestinationCode(order.getDestination().getCode());
    			target.setDestinationName(order.getDestination().getName());
    		}
    		target.setSpApprovalStatus(order.getSpApprovalStatus()!=null?order.getSpApprovalStatus().getCode():"");
            target.setExpectedDeliveryDate(source.getExpectedDeliveryDate());
            if(order.getPlacedBy()!=null && order.getPlacedBy() instanceof EyDmsUserModel){
                target.setSoContactNumber(((EyDmsUserModel) order.getPlacedBy()).getMobileNumber());
            }

            else{
                EyDmsUserModel salesOfficer = territoryService.getSOforCustomer((EyDmsCustomerModel) order.getUser());
                if(!Objects.isNull(salesOfficer)){
                    target.setSoContactNumber(salesOfficer.getMobileNumber());
                }
            }

            if(order.getRequisitions()!=null && !order.getRequisitions().isEmpty()) {
                String requisitionId = "";
                for(OrderRequisitionModel orderRequisitionModel : order.getRequisitions()) {
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
        //Estimated arrival time
    }


    private void populateProfilePicture(final MediaModel profilePicture, final EyDmsOrderHistoryData eydmsOrderHistoryData) {
        final ImageData profileImageData = imageConverter.convert(profilePicture);
        eydmsOrderHistoryData.setDealerProfilePicture(profileImageData);
    }
}
