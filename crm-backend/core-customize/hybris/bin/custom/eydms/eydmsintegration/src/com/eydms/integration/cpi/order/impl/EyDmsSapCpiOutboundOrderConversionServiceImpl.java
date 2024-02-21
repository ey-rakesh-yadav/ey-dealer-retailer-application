package com.eydms.integration.cpi.order.impl;

import com.eydms.core.model.EyDmsOrderLineItemModel;
import com.eydms.core.model.EyDmsOutboundOrderModel;
import com.eydms.integration.constants.EyDmsintegrationConstants;
import com.eydms.integration.cpi.order.EyDmsSapCpiOmmOrderMapperService;
import com.eydms.integration.cpi.order.EyDmsSapCpiOutboundOrderConversionService;
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

public class EyDmsSapCpiOutboundOrderConversionServiceImpl implements EyDmsSapCpiOutboundOrderConversionService {

    private static final Logger LOG = Logger.getLogger(EyDmsSapCpiOutboundOrderConversionServiceImpl.class);

    private List<EyDmsSapCpiOmmOrderMapperService<OrderModel, EyDmsOutboundOrderModel>> eydmsCpiOrderMappers;

    @Override
    public EyDmsOutboundOrderModel convertOrderToSapCpiOrder(OrderModel orderModel) {
        LOG.info("convertOrderToSapCpiOrder getting called");
        return convertOrderToSapCpiOrder(orderModel, new EyDmsOutboundOrderModel());
    }

    @Override
    public <T extends EyDmsOutboundOrderModel> T convertOrderToSapCpiOrder(OrderModel orderModel, T eydmsOutboundOrderModel) {
        LOG.info("inside convertOrderToSapCpiOrder ---> mapper ");
        getEyDmsCpiOrderMappers().forEach(mapper -> mapper.map(orderModel, eydmsOutboundOrderModel));
        return eydmsOutboundOrderModel;
    }

    @Override
    public EyDmsOutboundOrderModel convertISOOrderToSapCpiOrder(OrderModel orderModel) {
        EyDmsOutboundOrderModel eydmsOutboundOrderModel = new EyDmsOutboundOrderModel();
        if(null!=orderModel) {
            eydmsOutboundOrderModel.setCrmOrderNo(orderModel.getCode());
            eydmsOutboundOrderModel.setOrderTakenBy(((B2BCustomerModel) orderModel.getPlacedBy()).getEmployeeCode());
            //eydmsOutboundOrderModel.setWarehouse(orderModel.getWarehouse().getOrganisationId());
            //eydmsOutboundOrderModel.setVersionID(orderModel.getVersionID()!=null ? orderModel.getVersionID() : null);
            List<EyDmsOrderLineItemModel> entriesList = new ArrayList<>();
            if (orderModel.getEntries() != null && !orderModel.getEntries().isEmpty()) {
                for (AbstractOrderEntryModel entry : orderModel.getEntries()) {
                    EyDmsOrderLineItemModel eydmsOrderLineItemModel = new EyDmsOrderLineItemModel();
                    eydmsOrderLineItemModel.setEntryNumber(entry.getEntryNumber());
                    eydmsOrderLineItemModel.setBrand(orderModel.getSite().getUid()); //hardcoded
                    eydmsOrderLineItemModel.setPreparerId(((B2BCustomerModel) orderModel.getPlacedBy()).getEmployeeCode()); //Common User Employee ID
                    eydmsOrderLineItemModel.setDeliverToRequesterId(((B2BCustomerModel) orderModel.getPlacedBy()).getEmployeeCode()); //Common User Employee ID
                    eydmsOrderLineItemModel.setUnitOfMeasure(EyDmsintegrationConstants.UNIT_OF_MEASURE);
                    eydmsOrderLineItemModel.setDepotOrganizationId(orderModel.getDestination().getOrganisationId());
                    eydmsOrderLineItemModel.setDeliverToLocationId(orderModel.getDestination().getLocationCode());
                    eydmsOrderLineItemModel.setRefNum(orderModel.getCode());
                    eydmsOrderLineItemModel.setSourceOrgId(orderModel.getWarehouse().getOrganisationId());
                    eydmsOrderLineItemModel.setQuantityMT(entry.getQuantityInMT());
                    eydmsOrderLineItemModel.setModeOfTransport(orderModel.getDeliveryMode().getCode());
                    eydmsOrderLineItemModel.setProductId(entry.getProduct().getInventoryId());
                    eydmsOrderLineItemModel.setPackagingType(EyDmsintegrationConstants.PACKING_TYPE);
                    eydmsOrderLineItemModel.setBagType(orderModel.getEntries().get(0).getProduct().getPackagingCondition());
                    eydmsOrderLineItemModel.setRoute(orderModel.getRouteId());
                    DateFormat date = new SimpleDateFormat("yyyy-MM-dd");
                    String formattedDate = date.format(entry.getExpectedDeliveryDate());
                    eydmsOrderLineItemModel.setNeededByDate(formattedDate);
                    eydmsOrderLineItemModel.setInterfaceSourceCode("IMPORT_REQ");
                    eydmsOrderLineItemModel.setDestinationTypeCode("INVENTORY");
                    eydmsOrderLineItemModel.setHeaderDescription("X");
                    eydmsOrderLineItemModel.setAuthorizationStatus("APPROVED");
                    eydmsOrderLineItemModel.setSourceTypeCode("INVENTORY");
                    eydmsOrderLineItemModel.setCategoryId(0);
                    eydmsOrderLineItemModel.setItemDescription("X");
                    eydmsOrderLineItemModel.setLineType("Goods");
                    eydmsOrderLineItemModel.setNoteToBuyer("X");
                    eydmsOrderLineItemModel.setLineAttribute6("NON-PROJECT");
                    eydmsOrderLineItemModel.setLineAttribute7("RUNNING");
                    eydmsOrderLineItemModel.setHeaderAttribute1("X");
                    eydmsOrderLineItemModel.setPrice(Double.valueOf(0));
                    if (orderModel.getDeliveryMode().getCode().equals("ROAD")) {
                        eydmsOrderLineItemModel.setSecRouteId("0");
                        eydmsOrderLineItemModel.setRailSide(0);
                        eydmsOrderLineItemModel.setSecFrtTerms("X");
                    }
                    //todo
                    else if (orderModel.getDeliveryMode().getCode().equals("RAIL")) {
                        eydmsOrderLineItemModel.setSecRouteId("0");
                        eydmsOrderLineItemModel.setRailSide(0);
                        eydmsOrderLineItemModel.setSecFrtTerms("TBB");
                    }
                        entriesList.add(eydmsOrderLineItemModel);
                }
            }
            eydmsOutboundOrderModel.setOrderLineItemsList(entriesList);
        }
        return eydmsOutboundOrderModel;
    }

    public List<EyDmsSapCpiOmmOrderMapperService<OrderModel, EyDmsOutboundOrderModel>> getEyDmsCpiOrderMappers() {
        return eydmsCpiOrderMappers;
    }

    public void setEyDmsCpiOrderMappers(List<EyDmsSapCpiOmmOrderMapperService<OrderModel, EyDmsOutboundOrderModel>> eydmsCpiOrderMappers) {
        this.eydmsCpiOrderMappers = eydmsCpiOrderMappers;
    }
}
