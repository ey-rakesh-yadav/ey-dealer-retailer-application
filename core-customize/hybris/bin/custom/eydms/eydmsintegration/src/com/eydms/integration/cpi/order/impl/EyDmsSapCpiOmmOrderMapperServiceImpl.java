package com.eydms.integration.cpi.order.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.eydms.core.model.EyDmsOrderLineItemModel;
import com.eydms.core.model.EyDmsOrderSplitLineItemModel;
import com.eydms.core.model.EyDmsOutboundOrderModel;
import com.eydms.facades.data.EyDmsOrderLineItemData;
import com.eydms.facades.data.EyDmsOrderSplitLineItemData;
import com.eydms.facades.data.EyDmsOutboundOrderData;
import com.eydms.integration.cpi.order.EyDmsSapCpiOmmOrderConversionService;
import com.eydms.integration.cpi.order.EyDmsSapCpiOmmOrderMapperService;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.sap.sapcpiadapter.data.SapCpiOrder;
import de.hybris.platform.sap.sapcpiadapter.data.SapCpiOrderItem;
import de.hybris.platform.sap.sapcpiadapter.model.SAPCpiOutboundOrderItemModel;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EyDmsSapCpiOmmOrderMapperServiceImpl implements EyDmsSapCpiOmmOrderMapperService {

    private static final Logger LOG = Logger.getLogger(EyDmsSapCpiOmmOrderMapperServiceImpl.class);

    private EyDmsSapCpiOmmOrderConversionService eydmsSapCpiOmmOrderConversionService;

    @Override
    public void map(OrderModel orderModel, EyDmsOutboundOrderModel eydmsOutboundOrderModel) {
        LOG.info("mapSapCpiOrderToSAPCpiOrderOutbound getting called");
        mapSapCpiOrderToSAPCpiOrderOutbound(getEyDmsSapCpiOmmOrderConversionService().convertOrderToSapCpiOrder(orderModel), eydmsOutboundOrderModel);

    }

    protected void mapSapCpiOrderToSAPCpiOrderOutbound(EyDmsOutboundOrderData sapCpiOrder, EyDmsOutboundOrderModel eydmsOutboundOrder) {
        LOG.info("inside mapSapCpiOrderToSAPCpiOrderOutbound");
        if(null != sapCpiOrder) {
            eydmsOutboundOrder.setCrmOrderNo(sapCpiOrder.getCrmOrderNo());
            eydmsOutboundOrder.setOrderDate(sapCpiOrder.getOrderDate());
            eydmsOutboundOrder.setOrderType(sapCpiOrder.getOrderType());
            eydmsOutboundOrder.setOrderTakenBy(sapCpiOrder.getOrderTakenBy());
            eydmsOutboundOrder.setCustomerID(sapCpiOrder.getCustomerID());
            eydmsOutboundOrder.setBillingLocation(sapCpiOrder.getBillingLocation());
            eydmsOutboundOrder.setShippingLocation(sapCpiOrder.getShippingLocation());
            eydmsOutboundOrder.setPaymentTerms(sapCpiOrder.getPaymentTerms());
            eydmsOutboundOrder.setWarehouse(sapCpiOrder.getWarehouse());
            eydmsOutboundOrder.setUnloadingBy(sapCpiOrder.getUnloadingBy());
            eydmsOutboundOrder.setMmName(sapCpiOrder.getMmName());
            eydmsOutboundOrder.setMmCommission(sapCpiOrder.getMmCommission());
            eydmsOutboundOrder.setContractEndDate(sapCpiOrder.getContractEndDate());
            eydmsOutboundOrder.setCustomerPoNo(sapCpiOrder.getCustomerPoNo());
            eydmsOutboundOrder.setRemarks(sapCpiOrder.getRemarks());
            eydmsOutboundOrder.setOrderLineItems(mapOrderItems(sapCpiOrder.getOrderLineItems()));
            eydmsOutboundOrder.setOrderSplitLineItems(mapOrderSplitItems(sapCpiOrder.getOrderSplitLineItems()));
            eydmsOutboundOrder.setCancelledLineItems(mapCancelledItems(sapCpiOrder.getCancelledLineItems()));
            //eydmsOutboundOrder.setRequestedDeliveryDate(sapCpiOrder.getRequestedDeliveryDate());
            //eydmsOutboundOrder.setRequestedDeliveryslot(sapCpiOrder.getRequestedDeliverySlot());
            //eydmsOutboundOrder.setVersionID(sapCpiOrder.getVersionID());
            try {
                ObjectMapper o = new ObjectMapper();
                String jsonForOutboundOrder = o.writeValueAsString(eydmsOutboundOrder);
            }
            catch (JsonProcessingException e)
            {
                LOG.info("Error in jsonForOutboundOrder" + e.getMessage());
            }
        }
    }

    protected EyDmsOrderLineItemModel mapOrderItems(EyDmsOrderLineItemData eydmsOrderLineItemData) {

        EyDmsOrderLineItemModel eydmsOrderLineItemModel = new EyDmsOrderLineItemModel();
        eydmsOrderLineItemModel.setEntryNumber(eydmsOrderLineItemData.getEntryNumber());
        eydmsOrderLineItemModel.setFob(eydmsOrderLineItemData.getFob());
        eydmsOrderLineItemModel.setFreightTerms(eydmsOrderLineItemData.getFreightTerms());
        eydmsOrderLineItemModel.setProductId(eydmsOrderLineItemData.getProductId());
        eydmsOrderLineItemModel.setProductType(eydmsOrderLineItemData.getProductType());
        eydmsOrderLineItemModel.setUnitOfMeasure(eydmsOrderLineItemData.getUnitOfMeasure());
        //eydmsOrderLineItemModel.setQuantity(eydmsOrderLineItemData.getQuantity().longValue());
        eydmsOrderLineItemModel.setQuantityMT(eydmsOrderLineItemData.getQuantity());
        eydmsOrderLineItemModel.setBrand(eydmsOrderLineItemData.getBrand());
        eydmsOrderLineItemModel.setPrice(eydmsOrderLineItemData.getPrice());
        eydmsOrderLineItemModel.setDispatchType(eydmsOrderLineItemData.getDispatchType());
        eydmsOrderLineItemModel.setPackagingType(eydmsOrderLineItemData.getPackagingType());
        eydmsOrderLineItemModel.setModeOfTransport(eydmsOrderLineItemData.getModeOfTransport());
        eydmsOrderLineItemModel.setRoute(eydmsOrderLineItemData.getRouteId());
        eydmsOrderLineItemModel.setRemarkOfDiversion(eydmsOrderLineItemData.getRemarkOfDiversion());
        eydmsOrderLineItemModel.setOperation(eydmsOrderLineItemData.getOperation());
        eydmsOrderLineItemModel.setBagType(eydmsOrderLineItemData.getBagType());
        eydmsOrderLineItemModel.setRemarks(eydmsOrderLineItemData.getRemarks());
        return eydmsOrderLineItemModel;
    }

    protected List<EyDmsOrderSplitLineItemModel> mapOrderSplitItems(List<EyDmsOrderSplitLineItemData> eydmsOrderSplitLineItemData) {
    	List<EyDmsOrderSplitLineItemModel> listOfSplitLineItems = new ArrayList<>();
    	if(eydmsOrderSplitLineItemData!=null) {
    		for (EyDmsOrderSplitLineItemData eydmsOrderSplitLineItem : eydmsOrderSplitLineItemData) {
    			EyDmsOrderSplitLineItemModel eydmsOrderSplitLineItemModel = new EyDmsOrderSplitLineItemModel();
    			eydmsOrderSplitLineItemModel.setEntryNumber(eydmsOrderSplitLineItem.getEntryNumber());
    			//eydmsOrderSplitLineItemModel.setOrderQty(eydmsOrderSplitLineItem.getOrderQty().longValue());
    			eydmsOrderSplitLineItemModel.setOrderQtyMT(eydmsOrderSplitLineItem.getOrderQty());
                eydmsOrderSplitLineItemModel.setRemarks(eydmsOrderSplitLineItem.getRemarks());
    			listOfSplitLineItems.add(eydmsOrderSplitLineItemModel);
    		}
    	}
    	return listOfSplitLineItems;
    }

    protected List<EyDmsOrderSplitLineItemModel> mapCancelledItems(List<EyDmsOrderSplitLineItemData> eydmsCancelledLineItemData) {
    	List<EyDmsOrderSplitLineItemModel> listOfSplitLineItems = new ArrayList<>();
    	if(eydmsCancelledLineItemData!=null) {
    		for (EyDmsOrderSplitLineItemData eydmsOrderSplitLineItem : eydmsCancelledLineItemData) {
    			EyDmsOrderSplitLineItemModel eydmsOrderSplitLineItemModel = new EyDmsOrderSplitLineItemModel();
    			eydmsOrderSplitLineItemModel.setEntryNumber(eydmsOrderSplitLineItem.getEntryNumber());
    			listOfSplitLineItems.add(eydmsOrderSplitLineItemModel);
    		}
    	}
    	return listOfSplitLineItems;
    }

    public EyDmsSapCpiOmmOrderConversionService getEyDmsSapCpiOmmOrderConversionService() {
        return eydmsSapCpiOmmOrderConversionService;
    }

    public void setEyDmsSapCpiOmmOrderConversionService(EyDmsSapCpiOmmOrderConversionService eydmsSapCpiOmmOrderConversionService) {
        this.eydmsSapCpiOmmOrderConversionService = eydmsSapCpiOmmOrderConversionService;
    }


}
