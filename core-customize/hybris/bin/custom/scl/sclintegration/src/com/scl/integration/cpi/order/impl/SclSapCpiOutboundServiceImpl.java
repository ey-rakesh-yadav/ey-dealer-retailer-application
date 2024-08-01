package com.scl.integration.cpi.order.impl;

import com.scl.core.model.*;
import com.scl.integration.cpi.order.SclSapCpiOutboundService;
import com.scl.integration.model.SclOutboundDealerRetailerMappingModel;
import de.hybris.platform.sap.sapcpiadapter.model.SAPCpiOutboundOrderModel;
import de.hybris.platform.sap.sapcpiadapter.service.SapCpiOutboundService;
import de.hybris.platform.sap.sapcpiadapter.service.impl.SapCpiOutboundServiceImpl;
import org.springframework.http.ResponseEntity;
import rx.Observable;

import java.util.Map;

public class SclSapCpiOutboundServiceImpl extends SapCpiOutboundServiceImpl implements SclSapCpiOutboundService {


    //Scl Order Outbound
    private static final String OUTBOUND_ORDER_OBJECT = "SclOutboundOrder";
    private static final String OUTBOUND_ORDER_DESTINATION = "sclScpiOrderDestination";

    //Scl Cancel order/order line item Outbound
    private static final String OUTBOUND_CANCEL_ORDER_OBJECT = "SclOutboundCancelOrder";
    private static final String OUTBOUND_CANCEL_ORDER_DESTINATION = "sclScpiOrderCancelDestination";

    //Scl Ship To Party Address Outbound
    private static final String OUTBOUND_SHIP_TO_PARTY_ADDRESS_OBJECT = "SclOutboundShipToPartyAddress";
    private static final String OUTBOUND_SHIP_TO_PARTY_ADDRESS_DESTINATION = "sclShipToPartyAddressDestination";

    private static final String OUTBOUND_STAGE_GATE_OBJECT = "SclOutboundStageGate";
    private static final String OUTBOUND_STAGE_GATE_DESTINATION = "sclStageGateDestination";

    //Scl ISO Order Outbound
    private static final String OUTBOUND_ISO_ORDER_OBJECT = "SclOutboundISOOrder";
    private static final String OUTBOUND_ISO_ORDER_DESTINATION = "sclScpiISOOrderDestination";

    //Scl Cancel ISO order/order line item Outbound
    private static final String OUTBOUND_CANCEL_ISO_ORDER_OBJECT = "SclOutboundCancelISOOrder";
    private static final String OUTBOUND_CANCEL_ISO_ORDER_DESTINATION = "sclScpiOrderCancelISODestination";

    private static final String OUTBOUND_PRICE_OBJECT = "SclOutboundPrice";
    private static final String OUTBOUND_PRICE_DESTINATION = "sclScpiPriceDestination";

    // LP Source Master Outbound
    private static final String OUTBOUND_LP_SOURCE_OBJECT = "";
    private static final String OUTBOUND_LP_SOURCE_DESTINATION = "";

    private static final String OUTBOUND_BRANDING_REQUISITION_OBJECT = "SclOutboundBrandingRequisition";
    private static final String OUTBOUND_BRANDING_REQUISITION_DESTINATION = "sclScpiBrandingRequisitionDestination";
    private static final String OUTBOUND_BRANDING_REQUISITION_FEEDBACK_OBJECT = "SclOutboundBrandingRequisitionFeedback";
    private static final String OUTBOUND_BRANDING_REQUISITION_FEEDBACK_DESTINATION = "sclScpiBrandingRequisitionDestinationFeedback";

    private static final String OUTBOUND_BRANDING_REQUISITION_CANCEL_OBJECT = "SclOutboundBrandingRequisitionCancel";
    private static final String OUTBOUND_BRANDING_REQUISITION_CANCEL_DESTINATION = "sclScpiBrandingRequisitionCancelDestination";

    private static final String OUTBOUND_LINE_ITEM_ID_OBJECT = "SclOutboundLineItemId";
    private static final String OUTBOUND_LINE_ITEM_ID_DESTINATION = "SclOutboundLineItemIdDestination";
    private static final String OUTBOUND_DEALER_RETAILER_MAPPING_OBJECT = "SclOutboundDealerRetailerMapping";
    private static final String OUTBOUND_DEALER_RETAILER_MAPPING_DESTINATION = "SclOutboundDealerRetailerMappingDestination";;

    private static final String OUTBOUND_ENDCUSTOMER_COMPLAINT_OBJECT = "SclOutboundEndCustomerComplaint";
    private static final String OUTBOUND_ENDCUSTOMER_COMPLAINT_DESTINATION = "sclScpiEndCustomerComplaintDestination";


    @Override
    public Observable<ResponseEntity<Map>> sendOrder(SclOutboundOrderModel sclOutboundOrderModel) {
        return getOutboundServiceFacade().send(sclOutboundOrderModel, OUTBOUND_ORDER_OBJECT, OUTBOUND_ORDER_DESTINATION);
    }

    @Override
    public Observable<ResponseEntity<Map>> sendCancelOrder(SclOutboundCancelOrderModel sclOutboundCancelOrderModel) {
        return getOutboundServiceFacade().send(sclOutboundCancelOrderModel, OUTBOUND_CANCEL_ORDER_OBJECT, OUTBOUND_CANCEL_ORDER_DESTINATION);
    }

    @Override
    public Observable<ResponseEntity<Map>> sendShipToPartyAddress(SclOutboundShipToPartyModel sclOutboundShipToPartyModel) {
        return getOutboundServiceFacade().send(sclOutboundShipToPartyModel, OUTBOUND_SHIP_TO_PARTY_ADDRESS_OBJECT, OUTBOUND_SHIP_TO_PARTY_ADDRESS_DESTINATION);
    }

    @Override
    public Observable<ResponseEntity<Map>> sendStageGate(SclOutboundStageGateModel sclOutboundStageGateModel) {
        return getOutboundServiceFacade().send(sclOutboundStageGateModel, OUTBOUND_STAGE_GATE_OBJECT, OUTBOUND_STAGE_GATE_DESTINATION);
    }

    @Override
    public Observable<ResponseEntity<Map>> sendIsoOrder(SclOutboundOrderModel sclOutboundOrderModel) {
        return getOutboundServiceFacade().send(sclOutboundOrderModel, OUTBOUND_ISO_ORDER_OBJECT, OUTBOUND_ISO_ORDER_DESTINATION);
    }

    @Override
    public Observable<ResponseEntity<Map>> sendIsoCancelOrder(SclOutboundCancelOrderModel sclOutboundCancelOrderModel) {
        return getOutboundServiceFacade().send(sclOutboundCancelOrderModel, OUTBOUND_CANCEL_ISO_ORDER_OBJECT, OUTBOUND_CANCEL_ISO_ORDER_DESTINATION);
    }

    public Observable<ResponseEntity<Map>>  getPrice(SclOutboundPriceModel outboundPrice){
        return getOutboundServiceFacade().send(outboundPrice, OUTBOUND_PRICE_OBJECT, OUTBOUND_PRICE_DESTINATION);
    }

    @Override
    public Observable<ResponseEntity<Map>> sendBrandingRequisitionDetailsToSlct(SclOutboundBrandingRequisitionModel brandingRequistion) {
        return getOutboundServiceFacade().send(brandingRequistion, OUTBOUND_BRANDING_REQUISITION_OBJECT, OUTBOUND_BRANDING_REQUISITION_DESTINATION);
    }

    @Override
    public Observable<ResponseEntity<Map>> sendBrandingRequisitionDetailsFeedbackToSlct(SclOutboundBrandingRequisitionFeedbackModel brandingRequistionFeedback) {
        return getOutboundServiceFacade().send(brandingRequistionFeedback, OUTBOUND_BRANDING_REQUISITION_FEEDBACK_OBJECT, OUTBOUND_BRANDING_REQUISITION_FEEDBACK_DESTINATION);
    }

    @Override
    public Observable<ResponseEntity<Map>> sendBrandingRequisitionCancelToSlct(SclOutboundBrandingReqCancelModel brandingRequisition) {
        return getOutboundServiceFacade().send(brandingRequisition, OUTBOUND_BRANDING_REQUISITION_CANCEL_OBJECT, OUTBOUND_BRANDING_REQUISITION_CANCEL_DESTINATION);
    }

    @Override
    public Observable<ResponseEntity<Map>> sendErpLineItemIdToSLCT(SclOutboundErpItemModel sclOutboundErpItemModel) {
        return getOutboundServiceFacade().send(sclOutboundErpItemModel, OUTBOUND_LINE_ITEM_ID_OBJECT, OUTBOUND_LINE_ITEM_ID_DESTINATION);
    }

    @Override
    public Observable<ResponseEntity<Map>> sendDealerRetailerMappingToSLCT(SclOutboundDealerRetailerMappingModel sclOutboundDealerRetailerMappingModel) {
        return getOutboundServiceFacade().send(sclOutboundDealerRetailerMappingModel, OUTBOUND_DEALER_RETAILER_MAPPING_OBJECT, OUTBOUND_DEALER_RETAILER_MAPPING_DESTINATION);
    }
    @Override    
    public Observable<ResponseEntity<Map>> sendEndCustomerComplaintDetailsToSlct(OutboundEndCustomerComplaintModel endCustomerComplaintModel) {
        return getOutboundServiceFacade().send(endCustomerComplaintModel, OUTBOUND_ENDCUSTOMER_COMPLAINT_OBJECT, OUTBOUND_ENDCUSTOMER_COMPLAINT_DESTINATION);
    }

}