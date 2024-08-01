package com.scl.integration.cpi.order.impl;

import com.scl.core.model.SclOrderLineItemModel;
import com.scl.core.model.SclOutboundOrderModel;
import com.scl.integration.constants.SclintegrationConstants;
import com.scl.integration.cpi.order.SclSapCpiOmmOrderMapperService;
import com.scl.integration.cpi.order.SclSapCpiOutboundOrderConversionService;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.commercefacades.order.data.DeliveryModesData;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SclSapCpiOutboundOrderConversionServiceImpl implements SclSapCpiOutboundOrderConversionService {

    private static final Logger LOG = Logger.getLogger(SclSapCpiOutboundOrderConversionServiceImpl.class);

    private List<SclSapCpiOmmOrderMapperService<OrderModel, SclOutboundOrderModel>> sclCpiOrderMappers;

    @Override
    public SclOutboundOrderModel convertOrderToSapCpiOrder(OrderModel orderModel) {
        LOG.info("convertOrderToSapCpiOrder getting called");
        return convertOrderToSapCpiOrder(orderModel, new SclOutboundOrderModel());
    }

    @Override
    public <T extends SclOutboundOrderModel> T convertOrderToSapCpiOrder(OrderModel orderModel, T sclOutboundOrderModel) {
        LOG.info("inside convertOrderToSapCpiOrder ---> mapper ");
        getSclCpiOrderMappers().forEach(mapper -> mapper.map(orderModel, sclOutboundOrderModel));
        return sclOutboundOrderModel;
    }

    @Override
    public SclOutboundOrderModel convertISOOrderToSapCpiOrder(OrderModel orderModel) {
        SclOutboundOrderModel sclOutboundOrderModel = new SclOutboundOrderModel();
        if(null!=orderModel) {
            sclOutboundOrderModel.setCrmOrderNo(orderModel.getCode());
            sclOutboundOrderModel.setOrderTakenBy(((B2BCustomerModel) orderModel.getPlacedBy()).getEmployeeCode());
            //sclOutboundOrderModel.setWarehouse(orderModel.getWarehouse().getOrganisationId());
            //sclOutboundOrderModel.setVersionID(orderModel.getVersionID()!=null ? orderModel.getVersionID() : null);
            List<SclOrderLineItemModel> entriesList = new ArrayList<>();
            if (orderModel.getEntries() != null && !orderModel.getEntries().isEmpty()) {
                for (AbstractOrderEntryModel entry : orderModel.getEntries()) {
                    SclOrderLineItemModel sclOrderLineItemModel = new SclOrderLineItemModel();
                    sclOrderLineItemModel.setEntryNumber(entry.getEntryNumber());
                    sclOrderLineItemModel.setBrand(orderModel.getSite().getUid()); //hardcoded
                    sclOrderLineItemModel.setPreparerId(((B2BCustomerModel) orderModel.getPlacedBy()).getEmployeeCode()); //Common User Employee ID
                    sclOrderLineItemModel.setDeliverToRequesterId(((B2BCustomerModel) orderModel.getPlacedBy()).getEmployeeCode()); //Common User Employee ID
                    sclOrderLineItemModel.setUnitOfMeasure(SclintegrationConstants.UNIT_OF_MEASURE);
                    sclOrderLineItemModel.setDepotOrganizationId(orderModel.getDestination().getOrganisationId());
                    sclOrderLineItemModel.setDeliverToLocationId(orderModel.getDestination().getLocationCode());
                    sclOrderLineItemModel.setRefNum(orderModel.getCode());
                    sclOrderLineItemModel.setSourceOrgId(orderModel.getWarehouse().getOrganisationId());
                    sclOrderLineItemModel.setQuantityMT(entry.getQuantityInMT());
                    sclOrderLineItemModel.setModeOfTransport(orderModel.getDeliveryMode().getCode());
                    sclOrderLineItemModel.setProductId(entry.getProduct().getInventoryId());
                    sclOrderLineItemModel.setPackagingType(SclintegrationConstants.PACKING_TYPE);
                    sclOrderLineItemModel.setBagType(orderModel.getEntries().get(0).getProduct().getPackagingCondition());
                    sclOrderLineItemModel.setRoute(orderModel.getRouteId());
                    DateFormat date = new SimpleDateFormat("yyyy-MM-dd");
                    String formattedDate = date.format(entry.getExpectedDeliveryDate());
                    sclOrderLineItemModel.setNeededByDate(formattedDate);
                    sclOrderLineItemModel.setInterfaceSourceCode("IMPORT_REQ");
                    sclOrderLineItemModel.setDestinationTypeCode("INVENTORY");
                    sclOrderLineItemModel.setHeaderDescription("X");
                    sclOrderLineItemModel.setAuthorizationStatus("APPROVED");
                    sclOrderLineItemModel.setSourceTypeCode("INVENTORY");
                    sclOrderLineItemModel.setCategoryId(0);
                    sclOrderLineItemModel.setItemDescription("X");
                    sclOrderLineItemModel.setLineType("Goods");
                    sclOrderLineItemModel.setNoteToBuyer("X");
                    sclOrderLineItemModel.setLineAttribute6("NON-PROJECT");
                    sclOrderLineItemModel.setLineAttribute7("RUNNING");
                    sclOrderLineItemModel.setHeaderAttribute1("X");
                    sclOrderLineItemModel.setPrice(Double.valueOf(0));
                    if (orderModel.getDeliveryMode().getCode().equals("ROAD")) {
                        sclOrderLineItemModel.setSecRouteId("0");
                        sclOrderLineItemModel.setRailSide(0);
                        sclOrderLineItemModel.setSecFrtTerms("X");
                    }
                    //todo
                    else if (orderModel.getDeliveryMode().getCode().equals("RAIL")) {
                        sclOrderLineItemModel.setSecRouteId("0");
                        sclOrderLineItemModel.setRailSide(0);
                        sclOrderLineItemModel.setSecFrtTerms("TBB");
                    }
                        entriesList.add(sclOrderLineItemModel);
                }
            }
            sclOutboundOrderModel.setOrderLineItemsList(entriesList);
        }
        return sclOutboundOrderModel;
    }

    public List<SclSapCpiOmmOrderMapperService<OrderModel, SclOutboundOrderModel>> getSclCpiOrderMappers() {
        return sclCpiOrderMappers;
    }

    public void setSclCpiOrderMappers(List<SclSapCpiOmmOrderMapperService<OrderModel, SclOutboundOrderModel>> sclCpiOrderMappers) {
        this.sclCpiOrderMappers = sclCpiOrderMappers;
    }
}
