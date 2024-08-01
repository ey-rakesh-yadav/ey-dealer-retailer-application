package com.scl.integration.cpi.order;

import com.scl.core.model.*;
import com.scl.integration.model.SclOutboundDealerRetailerMappingModel;
import de.hybris.platform.sap.sapcpiadapter.model.SAPCpiOutboundOrderModel;
import org.springframework.http.ResponseEntity;
import rx.Observable;

import java.util.Map;

public interface SclSapCpiOutboundService {

    Observable<ResponseEntity<Map>> sendOrder(SclOutboundOrderModel sclOutboundOrderModel);

    Observable<ResponseEntity<Map>> sendCancelOrder(SclOutboundCancelOrderModel sclOutboundCancelOrderModel);

    Observable<ResponseEntity<Map>> sendShipToPartyAddress(SclOutboundShipToPartyModel sclOutboundShipToPartyModel);

    Observable<ResponseEntity<Map>> sendStageGate(SclOutboundStageGateModel sclOutboundStageGateModel);

    Observable<ResponseEntity<Map>> sendIsoOrder(SclOutboundOrderModel sclOutboundOrderModel);

    Observable<ResponseEntity<Map>> sendIsoCancelOrder(SclOutboundCancelOrderModel sclOutboundCancelOrderModel);

    Observable<ResponseEntity<Map>>  getPrice(SclOutboundPriceModel outboundPrice);

    Observable<ResponseEntity<Map>>  sendBrandingRequisitionDetailsToSlct(SclOutboundBrandingRequisitionModel brandingRequisition);
    Observable<ResponseEntity<Map>>  sendBrandingRequisitionDetailsFeedbackToSlct(SclOutboundBrandingRequisitionFeedbackModel brandingRequistionFeedback);
    Observable<ResponseEntity<Map>>  sendBrandingRequisitionCancelToSlct(SclOutboundBrandingReqCancelModel brandingRequisition);
    Observable<ResponseEntity<Map>>  sendErpLineItemIdToSLCT(SclOutboundErpItemModel sclOutboundErpItemModel);
    Observable<ResponseEntity<Map>>  sendEndCustomerComplaintDetailsToSlct(OutboundEndCustomerComplaintModel endCustomerComplaintModel);

    Observable<ResponseEntity<Map>>  sendDealerRetailerMappingToSLCT(SclOutboundDealerRetailerMappingModel sclOutboundDealerRetailerMappingModel);


}