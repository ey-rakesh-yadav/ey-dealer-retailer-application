package com.scl.integration.cpi.order.impl;

import com.scl.core.enums.CreatedFromCRMorERP;
import com.scl.core.enums.OrderType;
import com.scl.core.model.FreightAndIncoTermsMasterModel;
import com.scl.core.model.SclOrderLineItemModel;
import com.scl.facades.data.SclOrderLineItemData;
import com.scl.facades.data.SclOrderSplitLineItemData;
import com.scl.facades.data.SclOutboundOrderData;
import com.scl.integration.constants.SclintegrationConstants;
import com.scl.integration.cpi.order.SclSapCpiOmmOrderConversionService;
import com.scl.integration.cpi.order.SclSapCpiUtillityService;
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
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SclSapCpiOmmOrderConversionServiceImpl implements SclSapCpiOmmOrderConversionService {

    private static final Logger LOG = Logger.getLogger(SclSapCpiOmmOrderConversionServiceImpl.class);

    @Autowired
    EnumerationService enumerationService;
    @Autowired
    private RawItemContributor<OrderModel> sapOrderContributor;
    @Autowired
    private RawItemContributor<OrderModel> sapOrderEntryContributor;
    @Autowired
    SclSapCpiUtillityService sclSapCpiUtillityService;

    private ConfigurationService configurationService;



    @Override
    public SclOutboundOrderData convertOrderToSapCpiOrder(OrderModel orderModel) {
        LOG.info("convertOrderToSapCpiOrder getting called");
        SclOutboundOrderData sapCpiOrder = new SclOutboundOrderData();
        CustomerModel customer= (CustomerModel)orderModel.getUser();
        sapOrderContributor.createRows(orderModel).stream().findFirst().ifPresent(row -> {

            sapCpiOrder.setCrmOrderNo(orderModel.getCode());
            //sapCpiOrder.setOrderDate(mapDateAttribute(OrderCsvColumns.DATE, row));
            sapCpiOrder.setOrderDate(getFormatDate(orderModel.getDate()));
            sapCpiOrder.setOrderType(getConfigurationService().getConfiguration().getString(SclintegrationConstants.SCL_SALESORDER_ORDERTYPE,"ZTRD"));
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
            //sapCpiOrder.setShippingLocation(orderModel.getDeliveryAddress().getErpAddressId());
            sapCpiOrder.setPaymentTerms("IMMEDIATE");
            sapCpiOrder.setWarehouse(null != orderModel.getWarehouse() ? orderModel.getWarehouse().getOrganisationId() : StringUtils.EMPTY);
            sapCpiOrder.setUnloadingBy("BY PARTY");
            sapCpiOrder.setMmName(SclintegrationConstants.DEFAULT_VALUE_X);
            sapCpiOrder.setMmCommission(0);
            sapCpiOrder.setContractEndDate(String.valueOf(0));
            sapCpiOrder.setCustomerPoNo(SclintegrationConstants.DEFAULT_VALUE_X);
            sapCpiOrder.setRemarks(SclintegrationConstants.DEFAULT_VALUE_X);
            List<AbstractOrderEntryModel> lineItemsToBepunhced = orderModel.getEntries().stream()
                    .filter(entry->((OrderEntryModel)entry).getStatus()==null || !((OrderEntryModel)entry).getStatus().equals(OrderStatus.CANCELLED))
                    .sorted(Comparator.comparing(AbstractOrderEntryModel::getEntryNumber))
                    .collect(Collectors.toList());
            /*double totalQuantity = 0;
            if(lineItemsToBepunhced!=null && !lineItemsToBepunhced.isEmpty()) {
                totalQuantity = lineItemsToBepunhced.stream().collect(Collectors.summingDouble(AbstractOrderEntryModel::getQuantityInMT));
            }*/
            sapCpiOrder.setOrderLineItems(mapOrderLineItems(orderModel, lineItemsToBepunhced));

    		//sapCpiOrder.setOrderSplitLineItems(mapOrderSplitLineItems(lineItemsToBepunhced, totalQuantity));

            List<AbstractOrderEntryModel> cancelledlineItems = orderModel.getEntries().stream()
                    .filter(entry->((OrderEntryModel)entry).getStatus()!=null && ((OrderEntryModel)entry).getStatus().equals(OrderStatus.CANCELLED))
                    .sorted(Comparator.comparing(AbstractOrderEntryModel::getEntryNumber))
                    .collect(Collectors.toList());
            sapCpiOrder.setCancelledLineItems(mapCancelledLineItems(cancelledlineItems));
           if(orderModel.getRequestedDeliveryDate()!=null) {
               sapCpiOrder.setRequestedDeliveryDate(getFormatDate(orderModel.getRequestedDeliveryDate()));
           }
           sapCpiOrder.setCreateOrderFrom(String.valueOf(CreatedFromCRMorERP.CRM));
           /* if(orderModel.getRequestedDeliveryslot()!=null)
            sapCpiOrder.setRequestedDeliverySlot(orderModel.getRequestedDeliveryslot().getCode());*/
            //sapCpiOrder.setVersionID(orderModel.getVersionID()!= null ? orderModel.getVersionID():null);
        });
        return sapCpiOrder;
    }

    protected List<SclOrderLineItemData> mapOrderLineItems(OrderModel orderModel, List<AbstractOrderEntryModel> entryModelList) {

        final  List<SclOrderLineItemData> sapCpiOrderItems = new ArrayList<>();
    	if(entryModelList!=null && !entryModelList.isEmpty()) {
            entryModelList.forEach(entryModel -> {
                final SclOrderLineItemData sapCpiOrderItem = new SclOrderLineItemData();
                CustomerModel customer= (CustomerModel)orderModel.getUser();
                sapCpiOrderItem.setEntryNumber(entryModel.getEntryNumber());
                sapCpiOrderItem.setFob(entryModel.getFob()!=null?entryModel.getFob().getCode():null);
                sapCpiOrderItem.setFreightTerms(entryModel.getFreightTerms()!=null?entryModel.getFreightTerms().getCode():null);
                sapCpiOrderItem.setProductId(entryModel.getEquivalenceProductCode());
                sapCpiOrderItem.setProductType(entryModel.getProduct().getName());
                sapCpiOrderItem.setUnitOfMeasure(SclintegrationConstants.UNIT_OF_MEASURE);
                sapCpiOrderItem.setQuantity(Double.valueOf(entryModel.getQuantityInMT()));
                sapCpiOrderItem.setBrand(orderModel.getSite().getUid()); //to check
                sapCpiOrderItem.setPrice((double) 0);
                sapCpiOrderItem.setDispatchType(orderModel.getIsDealerProvideOwnTransport() ? "DEALER TRUCK": "OPEN MARKET");
                sapCpiOrderItem.setPackagingType(SclintegrationConstants.PACKING_TYPE);sapCpiOrderItem.setBagType(entryModel.getProduct().getPackagingCondition()); //to check
                if(orderModel.getOrderType().getCode().equals(OrderType.SO.getCode())) {
                    sapCpiOrderItem.setModeOfTransport(entryModel.getDeliveryMode().getCode());
                }
                sapCpiOrderItem.setRouteId(entryModel.getRouteId());
                sapCpiOrderItem.setRemarkOfDiversion(SclintegrationConstants.DEFAULT_VALUE_X);
                sapCpiOrderItem.setOperation(SclintegrationConstants.LINE_ITEM_OPERATION);


                if(StringUtils.isNotBlank(entryModel.getDriverContactNo()) && StringUtils.isNotBlank(entryModel.getTruckNo()))
                {

                  //   sapCpiOrderItem.setDriverPhoneNumber(entryModel.getDriverContactNo());
                  //  sapCpiOrderItem.setTruckNumber(entryModel.getTruckNo());
                    sapCpiOrderItem.setRemarks(entryModel.getTruckNo().concat(" ").concat("|").concat(" ").concat(entryModel.getDriverContactNo()).concat(" ").concat("|").concat(" ").concat(entryModel.getRemarks()));
                    sapCpiOrderItem.setDealerProvidingOwnTransport("true");
                }
                else{
                    sapCpiOrderItem.setDealerProvidingOwnTransport("false");
                    sapCpiOrderItem.setRemarks(entryModel.getRemarks());
                }

                //sapCpiOrderItem.setRemarks(entryModel.getRemarks());
                if(Objects.nonNull(entryModel.getDeliveryAddress())){
                    if (StringUtils.isNotBlank(entryModel.getDeliveryAddress().getPartnerFunctionId()) && entryModel.getDeliveryAddress().getSapAddressUsage().equalsIgnoreCase("RT")) {
                        sapCpiOrderItem.setRetailerCode(entryModel.getDeliveryAddress().getPartnerFunctionId());
                    }
                    sapCpiOrderItem.setShiptoAddressId(entryModel.getDeliveryAddress().getPartnerFunctionId());
                }
                sapCpiOrderItem.setSource(entryModel.getSource().getCode());
               // sapCpiOrderItem.setDeliverySlotDate(entryModel.getExpectedDeliveryDate());
                sapCpiOrderItem.setDeliverySlotTime(entryModel.getExpectedSlot().getCentreTime());
                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                String strDate = dateFormat.format(entryModel.getExpectedDeliveryDate());
               strDate=  strDate.concat(" "+entryModel.getExpectedSlot().getCentreTime());
                try {
                    sapCpiOrderItem.setDeliverySlotDate(new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(strDate));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                sapCpiOrderItem.setIncoTerm(Objects.nonNull(entryModel.getIncoTerm())? entryModel.getIncoTerm().getIncoTerm(): null);
                sapCpiOrderItem.setSpecialProcessIndicator(Objects.nonNull(entryModel.getSpecialProcessIndicator())? entryModel.getSpecialProcessIndicator().getCode(): null);

                sapCpiOrderItems.add(sapCpiOrderItem);
            });

    	}
    	return sapCpiOrderItems;

    }

    protected List<SclOrderSplitLineItemData> mapOrderSplitLineItems(List<AbstractOrderEntryModel> entryList, double totalQuantity) {

        List<SclOrderSplitLineItemData> listItemsLine = new ArrayList<>();

        Double remainingQty = totalQuantity;
        if(entryList!=null && entryList.size()>1) {
        	for(int i=1;i<entryList.size();i++) {
        		AbstractOrderEntryModel entry = entryList.get(i);
        		SclOrderSplitLineItemData sclOrderSplitLineItemData = new SclOrderSplitLineItemData();
        		remainingQty -= entry.getQuantityInMT();
        		sclOrderSplitLineItemData.setEntryNumber(entry.getEntryNumber());
        		sclOrderSplitLineItemData.setOrderQty(remainingQty);
                if(StringUtils.isNotBlank(entry.getDriverContactNo()) && StringUtils.isNotBlank(entry.getTruckNo()))
                {
                    sclOrderSplitLineItemData.setRemarks(entry.getTruckNo().concat(" ").concat("|").concat(" ").concat(entry.getDriverContactNo()).concat(" ").concat("|").concat(" ").concat(entry.getRemarks()));
                }
                else
                    sclOrderSplitLineItemData.setRemarks(entry.getRemarks());
        		listItemsLine.add(sclOrderSplitLineItemData);
        	}
        }
        return listItemsLine;

    }

    protected List<SclOrderSplitLineItemData> mapCancelledLineItems(List<AbstractOrderEntryModel> entryList) {
    	List<SclOrderSplitLineItemData> listItemsLine = new ArrayList<>();
    	if(entryList!=null) {
    		for(int i=0;i<entryList.size();i++) {
    			AbstractOrderEntryModel entry = entryList.get(i);
    			SclOrderSplitLineItemData sclOrderSplitLineItemData = new SclOrderSplitLineItemData();
    			sclOrderSplitLineItemData.setEntryNumber(entry.getEntryNumber());
    			listItemsLine.add(sclOrderSplitLineItemData);
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

    protected String getFormatDate(Date date) {
        if(date !=null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("IST"));
            return sdf.format(date);
        }
        return null;
    }


    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
}
