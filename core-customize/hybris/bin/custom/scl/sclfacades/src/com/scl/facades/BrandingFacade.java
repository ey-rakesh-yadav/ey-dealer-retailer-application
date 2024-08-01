package com.scl.facades;

import com.scl.core.enums.BrandingRequestStatus;
import com.scl.core.enums.BrandingSiteType;
import com.scl.core.enums.OrderType;
import com.scl.core.model.BrandingRequestDetailsModel;
import com.scl.core.model.BrandingTrackStatusStageGateModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.facades.data.*;
import com.scl.facades.order.data.BrandingTrackingListData;
import com.scl.facades.prosdealer.data.DealerListData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public interface BrandingFacade {
    BrandingRequestDetailsData getCounterDetailsForPointOfSale(String searchKeyWord);
    boolean submitBrandingRequisition(BrandingRequestDetailsData brandingRequestDetailsData);
    SearchPageData<BrandingRequestDetailsData> getBrandingRequestDetails(String filter, String startDate, String endDate, List<String> requestStatus, List<String> brandingSiteType, SearchPageData searchPageData) throws ParseException;
    DropdownListData getBrandingSiteTypeDropDownList(String brandingSiteType);
    DropdownListData getEnumTypes(String type);
    boolean submitActivityVerificationDetailsForRequest(BrandingRequestDetailsData brandingRequestDetailsData);
    BrandingRequestDetailsData getBrandReqDetailsForActivityVerification(String requisitionNumber);
    BrandingRequestDetailsData getActivityDetailsForRequest(String requisitionNumber);
    DealerListData getAllDealerRetailerList(boolean isDealer,boolean isRetailer);
    DealerListData getAllDealersForSubArea();
    public DropdownListData findAllState();
    public DropdownListData findAllDistrict(String state);
    public DropdownListData findAllTaluka(String state, String district);
    public DropdownListData findAllErpCity(String state, String district, String taluka);
    public TrackingStatusDetailsData getTrackingStatusDetailsForRequest(String requisitionNumber, String requestStatus);
    public TrackingStatusDetailsData viewRequestDetailsFromTrackingPage(String requisitionNumber, String requestStatus);
    public TrackingStatusDetailsData getTrackingStatusDetailsForCompleteForm(String requisitionNumber, String requestStatus);
    boolean updateBrandingRequestStatus(String requisitionNo, String status, String comments);

    BrandingTrackingListData getBrandingRequisitionTrackerDetails(String requisitionNumber);

    BrandingRequestData submitBrandingRequisitionSlct(BrandingRequestDetailsData brandingRequestDetailsData);
}
