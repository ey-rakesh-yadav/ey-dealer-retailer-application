package com.scl.integration.cpi.order.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scl.core.model.SclOrderLineItemModel;
import com.scl.core.model.SclOrderSplitLineItemModel;
import com.scl.core.model.SclOutboundOrderModel;
import com.scl.facades.data.SclOrderLineItemData;
import com.scl.facades.data.SclOrderSplitLineItemData;
import com.scl.facades.data.SclOutboundOrderData;
import com.scl.integration.cpi.order.SclSapCpiOmmOrderConversionService;
import com.scl.integration.cpi.order.SclSapCpiOmmOrderMapperService;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.sap.sapcpiadapter.data.SapCpiOrder;
import de.hybris.platform.sap.sapcpiadapter.data.SapCpiOrderItem;
import de.hybris.platform.sap.sapcpiadapter.model.SAPCpiOutboundOrderItemModel;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SclSapCpiOmmOrderMapperServiceImpl implements SclSapCpiOmmOrderMapperService {

    private static final Logger LOG = Logger.getLogger(SclSapCpiOmmOrderMapperServiceImpl.class);

    private SclSapCpiOmmOrderConversionService sclSapCpiOmmOrderConversionService;

    @Override
    public void map(OrderModel orderModel, SclOutboundOrderModel sclOutboundOrderModel) {
        LOG.info("mapSapCpiOrderToSAPCpiOrderOutbound getting called");
        mapSapCpiOrderToSAPCpiOrderOutbound(getSclSapCpiOmmOrderConversionService().convertOrderToSapCpiOrder(orderModel), sclOutboundOrderModel);

    }

    protected void mapSapCpiOrderToSAPCpiOrderOutbound(SclOutboundOrderData sapCpiOrder, SclOutboundOrderModel sclOutboundOrder) {
        LOG.info("inside mapSapCpiOrderToSAPCpiOrderOutbound");
        if(null != sapCpiOrder) {
            sclOutboundOrder.setCrmOrderNo(sapCpiOrder.getCrmOrderNo());
            sclOutboundOrder.setOrderDate(sapCpiOrder.getOrderDate());
            sclOutboundOrder.setOrderType(sapCpiOrder.getOrderType());
            sclOutboundOrder.setOrderTakenBy(sapCpiOrder.getOrderTakenBy());
            sclOutboundOrder.setCustomerID(sapCpiOrder.getCustomerID());
            sclOutboundOrder.setBillingLocation(sapCpiOrder.getBillingLocation());
           // sclOutboundOrder.setShippingLocation(sapCpiOrder.getShippingLocation());
            sclOutboundOrder.setPaymentTerms(sapCpiOrder.getPaymentTerms());
            sclOutboundOrder.setWarehouse(sapCpiOrder.getWarehouse());
            sclOutboundOrder.setUnloadingBy(sapCpiOrder.getUnloadingBy());
            sclOutboundOrder.setMmName(sapCpiOrder.getMmName());
            sclOutboundOrder.setMmCommission(sapCpiOrder.getMmCommission());
            sclOutboundOrder.setContractEndDate(sapCpiOrder.getContractEndDate());
            sclOutboundOrder.setCustomerPoNo(sapCpiOrder.getCustomerPoNo());
            sclOutboundOrder.setRemarks(sapCpiOrder.getRemarks());
            sclOutboundOrder.setOrderLineItems(mapOrderItems(sapCpiOrder.getOrderLineItems()));
           // sclOutboundOrder.setOrderSplitLineItems(mapOrderSplitItems(sapCpiOrder.getOrderSplitLineItems()));
            sclOutboundOrder.setCancelledLineItems(mapCancelledItems(sapCpiOrder.getCancelledLineItems()));
            sclOutboundOrder.setRequestedDeliveryDate(sapCpiOrder.getRequestedDeliveryDate());
            sclOutboundOrder.setCreatedOrderFrom(sapCpiOrder.getCreateOrderFrom());
            //sclOutboundOrder.setRequestedDeliveryslot(sapCpiOrder.getRequestedDeliverySlot());
            //sclOutboundOrder.setVersionID(sapCpiOrder.getVersionID());
           /* try {
                ObjectMapper o = new ObjectMapper();
                String jsonForOutboundOrder = o.writeValueAsString(sclOutboundOrder);
            }
            catch (JsonProcessingException e)
            {
                LOG.info("Error in jsonForOutboundOrder" + e.getMessage());
            }*/
        }
    }

    protected List<SclOrderLineItemModel> mapOrderItems(List<SclOrderLineItemData> sclOrderLineItemsData) {

        List<SclOrderLineItemModel> sclOrderLineItems = new ArrayList<>();
        sclOrderLineItemsData.forEach(sclOrderLineItemData-> {
            SclOrderLineItemModel sclOrderLineItemModel = new SclOrderLineItemModel();
            sclOrderLineItemModel.setEntryNumber(sclOrderLineItemData.getEntryNumber());
            sclOrderLineItemModel.setFob(sclOrderLineItemData.getFob());
            sclOrderLineItemModel.setFreightTerms(sclOrderLineItemData.getFreightTerms());
            sclOrderLineItemModel.setProductId(sclOrderLineItemData.getProductId());
            sclOrderLineItemModel.setProductType(sclOrderLineItemData.getProductType());
            sclOrderLineItemModel.setUnitOfMeasure(sclOrderLineItemData.getUnitOfMeasure());
            //sclOrderLineItemModel.setQuantity(sclOrderLineItemData.getQuantity().longValue());
            sclOrderLineItemModel.setQuantityMT(sclOrderLineItemData.getQuantity());
            sclOrderLineItemModel.setBrand(sclOrderLineItemData.getBrand());
            sclOrderLineItemModel.setPrice(sclOrderLineItemData.getPrice());
            sclOrderLineItemModel.setDispatchType(sclOrderLineItemData.getDispatchType());
            sclOrderLineItemModel.setPackagingType(sclOrderLineItemData.getPackagingType());
            sclOrderLineItemModel.setModeOfTransport(sclOrderLineItemData.getModeOfTransport());
            sclOrderLineItemModel.setRoute(sclOrderLineItemData.getRouteId());
            sclOrderLineItemModel.setRemarkOfDiversion(sclOrderLineItemData.getRemarkOfDiversion());
            sclOrderLineItemModel.setOperation(sclOrderLineItemData.getOperation());
            sclOrderLineItemModel.setBagType(sclOrderLineItemData.getBagType());
            sclOrderLineItemModel.setRemarks(sclOrderLineItemData.getRemarks());

            sclOrderLineItemModel.setRetailerCode(sclOrderLineItemData.getRetailerCode());
            sclOrderLineItemModel.setShiptoAddressId(sclOrderLineItemData.getShiptoAddressId());
            sclOrderLineItemModel.setSource(sclOrderLineItemData.getSource());
            sclOrderLineItemModel.setDeliverySlotDate(sclOrderLineItemData.getDeliverySlotDate());
            sclOrderLineItemModel.setDeliverySlotTime(sclOrderLineItemData.getDeliverySlotTime());
           // sclOrderLineItemModel.setTruckNumber(sclOrderLineItemData.getTruckNumber());
           // sclOrderLineItemModel.setDriverPhoneNumber(sclOrderLineItemData.getDriverPhoneNumber());
            sclOrderLineItemModel.setDealerProvidingOwnTransport(sclOrderLineItemData.getDealerProvidingOwnTransport());
            sclOrderLineItemModel.setIncoTerm(sclOrderLineItemData.getIncoTerm());
            sclOrderLineItemModel.setSpecialProcessIndicator(sclOrderLineItemData.getSpecialProcessIndicator());

            sclOrderLineItems.add(sclOrderLineItemModel);

        });
        return sclOrderLineItems;
    }

    protected List<SclOrderSplitLineItemModel> mapOrderSplitItems(List<SclOrderSplitLineItemData> sclOrderSplitLineItemData) {
    	List<SclOrderSplitLineItemModel> listOfSplitLineItems = new ArrayList<>();
    	if(sclOrderSplitLineItemData!=null) {
    		for (SclOrderSplitLineItemData sclOrderSplitLineItem : sclOrderSplitLineItemData) {
    			SclOrderSplitLineItemModel sclOrderSplitLineItemModel = new SclOrderSplitLineItemModel();
    			sclOrderSplitLineItemModel.setEntryNumber(sclOrderSplitLineItem.getEntryNumber());
    			//sclOrderSplitLineItemModel.setOrderQty(sclOrderSplitLineItem.getOrderQty().longValue());
    			sclOrderSplitLineItemModel.setOrderQtyMT(sclOrderSplitLineItem.getOrderQty());
                sclOrderSplitLineItemModel.setRemarks(sclOrderSplitLineItem.getRemarks());
    			listOfSplitLineItems.add(sclOrderSplitLineItemModel);
    		}
    	}
    	return listOfSplitLineItems;
    }

    protected List<SclOrderSplitLineItemModel> mapCancelledItems(List<SclOrderSplitLineItemData> sclCancelledLineItemData) {
    	List<SclOrderSplitLineItemModel> listOfSplitLineItems = new ArrayList<>();
    	if(sclCancelledLineItemData!=null) {
    		for (SclOrderSplitLineItemData sclOrderSplitLineItem : sclCancelledLineItemData) {
    			SclOrderSplitLineItemModel sclOrderSplitLineItemModel = new SclOrderSplitLineItemModel();
    			sclOrderSplitLineItemModel.setEntryNumber(sclOrderSplitLineItem.getEntryNumber());
    			listOfSplitLineItems.add(sclOrderSplitLineItemModel);
    		}
    	}
    	return listOfSplitLineItems;
    }

    public SclSapCpiOmmOrderConversionService getSclSapCpiOmmOrderConversionService() {
        return sclSapCpiOmmOrderConversionService;
    }

    public void setSclSapCpiOmmOrderConversionService(SclSapCpiOmmOrderConversionService sclSapCpiOmmOrderConversionService) {
        this.sclSapCpiOmmOrderConversionService = sclSapCpiOmmOrderConversionService;
    }


}
