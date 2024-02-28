package com.eydms.integration.cpi.order.impl;

import com.eydms.core.model.*;
import com.eydms.integration.cpi.order.EyDmsSapCpiOutboundService;
import de.hybris.platform.sap.sapcpiadapter.model.SAPCpiOutboundOrderModel;
import de.hybris.platform.sap.sapcpiadapter.service.SapCpiOutboundService;
import de.hybris.platform.sap.sapcpiadapter.service.impl.SapCpiOutboundServiceImpl;
import org.springframework.http.ResponseEntity;
import rx.Observable;

import java.util.Map;

public class EyDmsSapCpiOutboundServiceImpl extends SapCpiOutboundServiceImpl implements EyDmsSapCpiOutboundService {


    //EyDms Order Outbound
    private static final String OUTBOUND_ORDER_OBJECT = "EyDmsOutboundOrder";
    private static final String OUTBOUND_ORDER_DESTINATION = "eydmsScpiOrderDestination";

    //EyDms Cancel order/order line item Outbound
    private static final String OUTBOUND_CANCEL_ORDER_OBJECT = "EyDmsOutboundCancelOrder";
    private static final String OUTBOUND_CANCEL_ORDER_DESTINATION = "eydmsScpiOrderCancelDestination";

    //EyDms Ship To Party Address Outbound
    private static final String OUTBOUND_SHIP_TO_PARTY_ADDRESS_OBJECT = "EyDmsOutboundShipToPartyAddress";
    private static final String OUTBOUND_SHIP_TO_PARTY_ADDRESS_DESTINATION = "eydmsShipToPartyAddressDestination";

    //EyDms ISO Order Outbound
    private static final String OUTBOUND_ISO_ORDER_OBJECT = "EyDmsOutboundISOOrder";
    private static final String OUTBOUND_ISO_ORDER_DESTINATION = "eydmsScpiISOOrderDestination";

    //EyDms Cancel ISO order/order line item Outbound
    private static final String OUTBOUND_CANCEL_ISO_ORDER_OBJECT = "EyDmsOutboundCancelISOOrder";
    private static final String OUTBOUND_CANCEL_ISO_ORDER_DESTINATION = "eydmsScpiOrderCancelISODestination";

    private static final String OUTBOUND_PRICE_OBJECT = "EyDmsOutboundPrice";
    private static final String OUTBOUND_PRICE_DESTINATION = "eydmsScpiPriceDestination";

    // LP Source Master Outbound
    private static final String OUTBOUND_LP_SOURCE_OBJECT = "";
    private static final String OUTBOUND_LP_SOURCE_DESTINATION = "";

    private static final String OUTBOUND_BRANDING_REQUISITION_OBJECT = "EyDmsOutboundBrandingRequisition";
    private static final String OUTBOUND_BRANDING_REQUISITION_DESTINATION = "eydmsScpiBrandingRequisitionDestination";
    private static final String OUTBOUND_BRANDING_REQUISITION_FEEDBACK_OBJECT = "EyDmsOutboundBrandingRequisitionFeedback";
    private static final String OUTBOUND_BRANDING_REQUISITION_FEEDBACK_DESTINATION = "eydmsScpiBrandingRequisitionDestinationFeedback";

    private static final String OUTBOUND_BRANDING_REQUISITION_CANCEL_OBJECT = "EyDmsOutboundBrandingRequisitionCancel";
    private static final String OUTBOUND_BRANDING_REQUISITION_CANCEL_DESTINATION = "eydmsScpiBrandingRequisitionCancelDestination";

    private static final String OUTBOUND_LINE_ITEM_ID_OBJECT = "EyDmsOutboundLineItemId";
    private static final String OUTBOUND_LINE_ITEM_ID_DESTINATION = "EyDmsOutboundLineItemIdDestination";

    private static final String OUTBOUND_ENDCUSTOMER_COMPLAINT_OBJECT = "EyDmsOutboundEndCustomerComplaint";
    private static final String OUTBOUND_ENDCUSTOMER_COMPLAINT_DESTINATION = "eydmsScpiEndCustomerComplaintDestination";


    @Override
    public Observable<ResponseEntity<Map>> sendOrder(EyDmsOutboundOrderModel eydmsOutboundOrderModel) {
        return getOutboundServiceFacade().send(eydmsOutboundOrderModel, OUTBOUND_ORDER_OBJECT, OUTBOUND_ORDER_DESTINATION);
    }

    @Override
    public Observable<ResponseEntity<Map>> sendCancelOrder(EyDmsOutboundCancelOrderModel eydmsOutboundCancelOrderModel) {
        return getOutboundServiceFacade().send(eydmsOutboundCancelOrderModel, OUTBOUND_CANCEL_ORDER_OBJECT, OUTBOUND_CANCEL_ORDER_DESTINATION);
    }

    @Override
    public Observable<ResponseEntity<Map>> sendShipToPartyAddress(EyDmsOutboundShipToPartyModel eydmsOutboundShipToPartyModel) {
        return getOutboundServiceFacade().send(eydmsOutboundShipToPartyModel, OUTBOUND_SHIP_TO_PARTY_ADDRESS_OBJECT, OUTBOUND_SHIP_TO_PARTY_ADDRESS_DESTINATION);
    }

    @Override
    public Observable<ResponseEntity<Map>> sendIsoOrder(EyDmsOutboundOrderModel eydmsOutboundOrderModel) {
        return getOutboundServiceFacade().send(eydmsOutboundOrderModel, OUTBOUND_ISO_ORDER_OBJECT, OUTBOUND_ISO_ORDER_DESTINATION);
    }

    @Override
    public Observable<ResponseEntity<Map>> sendIsoCancelOrder(EyDmsOutboundCancelOrderModel eydmsOutboundCancelOrderModel) {
        return getOutboundServiceFacade().send(eydmsOutboundCancelOrderModel, OUTBOUND_CANCEL_ISO_ORDER_OBJECT, OUTBOUND_CANCEL_ISO_ORDER_DESTINATION);
    }

    public Observable<ResponseEntity<Map>>  getPrice(EyDmsOutboundPriceModel outboundPrice){
        return getOutboundServiceFacade().send(outboundPrice, OUTBOUND_PRICE_OBJECT, OUTBOUND_PRICE_DESTINATION);
    }

    @Override
    public Observable<ResponseEntity<Map>> sendBrandingRequisitionDetailsToSlct(EyDmsOutboundBrandingRequisitionModel brandingRequistion) {
        return getOutboundServiceFacade().send(brandingRequistion, OUTBOUND_BRANDING_REQUISITION_OBJECT, OUTBOUND_BRANDING_REQUISITION_DESTINATION);
    }

    @Override
    public Observable<ResponseEntity<Map>> sendBrandingRequisitionDetailsFeedbackToSlct(EyDmsOutboundBrandingRequisitionFeedbackModel brandingRequistionFeedback) {
        return getOutboundServiceFacade().send(brandingRequistionFeedback, OUTBOUND_BRANDING_REQUISITION_FEEDBACK_OBJECT, OUTBOUND_BRANDING_REQUISITION_FEEDBACK_DESTINATION);
    }

    @Override
    public Observable<ResponseEntity<Map>> sendBrandingRequisitionCancelToSlct(EyDmsOutboundBrandingReqCancelModel brandingRequisition) {
        return getOutboundServiceFacade().send(brandingRequisition, OUTBOUND_BRANDING_REQUISITION_CANCEL_OBJECT, OUTBOUND_BRANDING_REQUISITION_CANCEL_DESTINATION);
    }

    @Override
    public Observable<ResponseEntity<Map>> sendErpLineItemIdToSLCT(EyDmsOutboundErpItemModel eydmsOutboundErpItemModel) {
        return getOutboundServiceFacade().send(eydmsOutboundErpItemModel, OUTBOUND_LINE_ITEM_ID_OBJECT, OUTBOUND_LINE_ITEM_ID_DESTINATION);
    }

    @Override
    public Observable<ResponseEntity<Map>> sendEndCustomerComplaintDetailsToSlct(OutboundEndCustomerComplaintModel endCustomerComplaintModel) {
        return getOutboundServiceFacade().send(endCustomerComplaintModel, OUTBOUND_ENDCUSTOMER_COMPLAINT_OBJECT, OUTBOUND_ENDCUSTOMER_COMPLAINT_DESTINATION);
    }

}