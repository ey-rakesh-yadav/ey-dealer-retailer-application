package com.eydms.integration.cpi.order;

import com.eydms.core.model.*;
import de.hybris.platform.sap.sapcpiadapter.model.SAPCpiOutboundOrderModel;
import org.springframework.http.ResponseEntity;
import rx.Observable;

import java.util.Map;

public interface EyDmsSapCpiOutboundService {

    Observable<ResponseEntity<Map>> sendOrder(EyDmsOutboundOrderModel eydmsOutboundOrderModel);

    Observable<ResponseEntity<Map>> sendCancelOrder(EyDmsOutboundCancelOrderModel eydmsOutboundCancelOrderModel);

    Observable<ResponseEntity<Map>> sendShipToPartyAddress(EyDmsOutboundShipToPartyModel eydmsOutboundShipToPartyModel);

    Observable<ResponseEntity<Map>> sendIsoOrder(EyDmsOutboundOrderModel eydmsOutboundOrderModel);

    Observable<ResponseEntity<Map>> sendIsoCancelOrder(EyDmsOutboundCancelOrderModel eydmsOutboundCancelOrderModel);

    Observable<ResponseEntity<Map>>  getPrice(EyDmsOutboundPriceModel outboundPrice);

    Observable<ResponseEntity<Map>>  sendBrandingRequisitionDetailsToSlct(EyDmsOutboundBrandingRequisitionModel brandingRequisition);
    Observable<ResponseEntity<Map>>  sendBrandingRequisitionDetailsFeedbackToSlct(EyDmsOutboundBrandingRequisitionFeedbackModel brandingRequistionFeedback);
    Observable<ResponseEntity<Map>>  sendBrandingRequisitionCancelToSlct(EyDmsOutboundBrandingReqCancelModel brandingRequisition);
    Observable<ResponseEntity<Map>>  sendErpLineItemIdToSLCT(EyDmsOutboundErpItemModel eydmsOutboundErpItemModel);
    Observable<ResponseEntity<Map>>  sendEndCustomerComplaintDetailsToSlct(OutboundEndCustomerComplaintModel endCustomerComplaintModel);

}