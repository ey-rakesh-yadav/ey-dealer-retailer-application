package com.eydms.integration.cpi.order.impl;

import com.eydms.core.enums.OrderType;
import com.eydms.core.model.FreightAndIncoTermsMasterModel;
import com.eydms.core.model.EyDmsOrderLineItemModel;
import com.eydms.facades.data.EyDmsOrderLineItemData;
import com.eydms.facades.data.EyDmsOrderSplitLineItemData;
import com.eydms.facades.data.EyDmsOutboundOrderData;
import com.eydms.integration.constants.EyDmsintegrationConstants;
import com.eydms.integration.cpi.order.EyDmsSapCpiOmmOrderConversionService;
import com.eydms.integration.cpi.order.EyDmsSapCpiUtillityService;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.sap.orderexchange.constants.OrderCsvColumns;
import de.hybris.platform.sap.orderexchange.constants.OrderEntryCsvColumns;
import de.hybris.platform.sap.orderexchange.outbound.RawItemContributor;
import de.hybris.platform.sap.sapcpiadapter.data.SapCpiOrder;
import de.hybris.platform.sap.sapcpiadapter.data.SapCpiOrderItem;
import de.hybris.platform.sap.sapcpiorderexchange.service.impl.SapCpiOmmOrderConversionService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EyDmsSapCpiOmmOrderConversionServiceImpl implements EyDmsSapCpiOmmOrderConversionService {

    private static final Logger LOG = Logger.getLogger(EyDmsSapCpiOmmOrderConversionServiceImpl.class);

    @Autowired
    EnumerationService enumerationService;
    @Autowired
    private RawItemContributor<OrderModel> sapOrderContributor;
    @Autowired
    private RawItemContributor<OrderModel> sapOrderEntryContributor;
    @Autowired
    EyDmsSapCpiUtillityService eydmsSapCpiUtillityService;

    @Override
    public EyDmsOutboundOrderData convertOrderToSapCpiOrder(OrderModel orderModel) {
        LOG.info("convertOrderToSapCpiOrder getting called");
        EyDmsOutboundOrderData sapCpiOrder = new EyDmsOutboundOrderData();
        CustomerModel customer= (CustomerModel)orderModel.getUser();
        sapOrderContributor.createRows(orderModel).stream().findFirst().ifPresent(row -> {

            sapCpiOrder.setCrmOrderNo(orderModel.getCode());
            sapCpiOrder.setOrderDate(mapDateAttribute(OrderCsvColumns.DATE, row));
            sapCpiOrder.setOrderType(EyDmsintegrationConstants.DEFAULT_VALUE_X);
            sapCpiOrder.setOrderTakenBy(null != orderModel.getPlacedBy() ? ((B2BCustomerModel)orderModel.getPlacedBy()).getEmployeeCode() : StringUtils.EMPTY);
            sapCpiOrder.setCustomerID(null != customer.getCustomerID() ? customer.getCustomerID() : StringUtils.EMPTY);
            Collection<AddressModel> list = orderModel.getUser().getAddresses();
            if(CollectionUtils.isNotEmpty(list)) {
                List<AddressModel> billingAddressList = list.stream().filter(address -> address.getBillingAddress()).collect(Collectors.toList());
                if(billingAddressList != null && !billingAddressList.isEmpty()) {
                    AddressModel billingAddress = billingAddressList.get(0);
                    if(null != billingAddress)
                    {
                        sapCpiOrder.setBillingLocation(billingAddress.getErpAddressId());
                    }
                }
            }
            sapCpiOrder.setShippingLocation(orderModel.getDeliveryAddress().getErpAddressId());
            sapCpiOrder.setPaymentTerms("IMMEDIATE");
            sapCpiOrder.setWarehouse(null != orderModel.getWarehouse() ? orderModel.getWarehouse().getOrganisationId() : StringUtils.EMPTY);
            sapCpiOrder.setUnloadingBy("BY PARTY");
            sapCpiOrder.setMmName(EyDmsintegrationConstants.DEFAULT_VALUE_X);
            sapCpiOrder.setMmCommission(0);
            sapCpiOrder.setContractEndDate(String.valueOf(0));
            sapCpiOrder.setCustomerPoNo(EyDmsintegrationConstants.DEFAULT_VALUE_X);
            sapCpiOrder.setRemarks(EyDmsintegrationConstants.DEFAULT_VALUE_X);
            List<AbstractOrderEntryModel> lineItemsToBepunhced = orderModel.getEntries().stream()
                    .filter(entry->((OrderEntryModel)entry).getStatus()==null || !((OrderEntryModel)entry).getStatus().equals(OrderStatus.CANCELLED))
                    .sorted(Comparator.comparing(AbstractOrderEntryModel::getEntryNumber))
                    .collect(Collectors.toList());
            double totalQuantity = 0;
            if(lineItemsToBepunhced!=null && !lineItemsToBepunhced.isEmpty()) {
                totalQuantity = lineItemsToBepunhced.stream().collect(Collectors.summingDouble(AbstractOrderEntryModel::getQuantityInMT));
            }
        	sapCpiOrder.setOrderLineItems(mapOrderLineItems(orderModel, lineItemsToBepunhced, totalQuantity));

    		sapCpiOrder.setOrderSplitLineItems(mapOrderSplitLineItems(lineItemsToBepunhced, totalQuantity));

            List<AbstractOrderEntryModel> cancelledlineItems = orderModel.getEntries().stream()
                    .filter(entry->((OrderEntryModel)entry).getStatus()!=null && ((OrderEntryModel)entry).getStatus().equals(OrderStatus.CANCELLED))
                    .sorted(Comparator.comparing(AbstractOrderEntryModel::getEntryNumber))
                    .collect(Collectors.toList());
            sapCpiOrder.setCancelledLineItems(mapCancelledLineItems(cancelledlineItems));
            /*if(orderModel.getRequestedDeliveryDate()!=null)
            sapCpiOrder.setRequestedDeliveryDate(String.valueOf(orderModel.getRequestedDeliveryDate()));
            if(orderModel.getRequestedDeliveryslot()!=null)
            sapCpiOrder.setRequestedDeliverySlot(orderModel.getRequestedDeliveryslot().getCode());*/
            //sapCpiOrder.setVersionID(orderModel.getVersionID()!= null ? orderModel.getVersionID():null);
        });
        return sapCpiOrder;
    }

    protected EyDmsOrderLineItemData mapOrderLineItems(OrderModel orderModel, List<AbstractOrderEntryModel> entryModelList, double totalQuantity) {

    	final EyDmsOrderLineItemData sapCpiOrderItem = new EyDmsOrderLineItemData();
    	if(entryModelList!=null && !entryModelList.isEmpty()) {
    		OrderEntryModel entryModel = (OrderEntryModel) entryModelList.get(0);
    		CustomerModel customer= (CustomerModel)orderModel.getUser();
    		sapCpiOrderItem.setEntryNumber(entryModel.getEntryNumber());
    		sapCpiOrderItem.setFob(entryModel.getFob()!=null?entryModel.getFob().getCode():null);
    		sapCpiOrderItem.setFreightTerms(entryModel.getFreightTerms()!=null?entryModel.getFreightTerms().getCode():null);
    		sapCpiOrderItem.setProductId(entryModel.getProduct().getInventoryId());
    		sapCpiOrderItem.setProductType(entryModel.getProduct().getName());
    		sapCpiOrderItem.setUnitOfMeasure(EyDmsintegrationConstants.UNIT_OF_MEASURE);
    		sapCpiOrderItem.setQuantity(totalQuantity);
    		sapCpiOrderItem.setBrand(orderModel.getSite().getUid()); //to check
    		sapCpiOrderItem.setPrice((double) 0);
    		sapCpiOrderItem.setDispatchType(orderModel.getIsDealerProvideOwnTransport() ? "DEALER TRUCK": "OPEN MARKET");
    		sapCpiOrderItem.setPackagingType(EyDmsintegrationConstants.PACKING_TYPE);
    		//                String productName = orderModel.getEntries().get(0).getProduct().getName();
    		//                String packagingCond = productName.split("-")[2];
    		sapCpiOrderItem.setBagType(entryModel.getProduct().getPackagingCondition()); //to check
    		if(orderModel.getOrderType().getCode().equals(OrderType.SO.getCode()))
    			sapCpiOrderItem.setModeOfTransport(orderModel.getDeliveryMode().getCode());
    		sapCpiOrderItem.setRouteId(orderModel.getRouteId());
    		sapCpiOrderItem.setRemarkOfDiversion(EyDmsintegrationConstants.DEFAULT_VALUE_X);
    		sapCpiOrderItem.setOperation(EyDmsintegrationConstants.LINE_ITEM_OPERATION);
            if(StringUtils.isNotBlank(entryModel.getDriverContactNo()) && StringUtils.isNotBlank(entryModel.getTruckNo()))
            {
                sapCpiOrderItem.setRemarks(entryModel.getTruckNo().concat(" ").concat("|").concat(" ").concat(entryModel.getDriverContactNo()).concat(" ").concat("|").concat(" ").concat(entryModel.getRemarks()));
            }
            else
                sapCpiOrderItem.setRemarks(entryModel.getRemarks());
    	}
    	return sapCpiOrderItem;

    }

    protected List<EyDmsOrderSplitLineItemData> mapOrderSplitLineItems(List<AbstractOrderEntryModel> entryList, double totalQuantity) {

        List<EyDmsOrderSplitLineItemData> listItemsLine = new ArrayList<>();

        Double remainingQty = totalQuantity;
        if(entryList!=null && entryList.size()>1) {
        	for(int i=1;i<entryList.size();i++) {
        		AbstractOrderEntryModel entry = entryList.get(i);
        		EyDmsOrderSplitLineItemData eydmsOrderSplitLineItemData = new EyDmsOrderSplitLineItemData();
        		remainingQty -= entry.getQuantityInMT();
        		eydmsOrderSplitLineItemData.setEntryNumber(entry.getEntryNumber());
        		eydmsOrderSplitLineItemData.setOrderQty(remainingQty);
                if(StringUtils.isNotBlank(entry.getDriverContactNo()) && StringUtils.isNotBlank(entry.getTruckNo()))
                {
                    eydmsOrderSplitLineItemData.setRemarks(entry.getTruckNo().concat(" ").concat("|").concat(" ").concat(entry.getDriverContactNo()).concat(" ").concat("|").concat(" ").concat(entry.getRemarks()));
                }
                else
                    eydmsOrderSplitLineItemData.setRemarks(entry.getRemarks());
        		listItemsLine.add(eydmsOrderSplitLineItemData);
        	}
        }
        return listItemsLine;

    }

    protected List<EyDmsOrderSplitLineItemData> mapCancelledLineItems(List<AbstractOrderEntryModel> entryList) {
    	List<EyDmsOrderSplitLineItemData> listItemsLine = new ArrayList<>();
    	if(entryList!=null) {
    		for(int i=0;i<entryList.size();i++) {
    			AbstractOrderEntryModel entry = entryList.get(i);
    			EyDmsOrderSplitLineItemData eydmsOrderSplitLineItemData = new EyDmsOrderSplitLineItemData();
    			eydmsOrderSplitLineItemData.setEntryNumber(entry.getEntryNumber());
    			listItemsLine.add(eydmsOrderSplitLineItemData);
    		}
    	}
    	return listItemsLine;
    }
    
    protected String mapAttribute(String attribute, Map<String, Object> row) {
        return row.get(attribute) != null ? row.get(attribute).toString() : null;
    }

    protected String mapDateAttribute(String attribute, Map<String, Object> row) {

        if (row.get(attribute) != null && row.get(attribute) instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.format((Date) row.get(attribute));
        }

        return null;
    }
}
