package com.eydms.core.services;

import com.eydms.core.enums.BrandingRequestStatus;
import com.eydms.core.enums.BrandingSiteType;
import com.eydms.core.enums.OrderType;
import com.eydms.core.model.*;
import com.eydms.facades.data.*;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public interface BrandingService {
    List<List<Object>> getCounterDetailsForPointOfSale(String searchKeyWord);
    EyDmsCustomerModel getCounterDetailsForPointOfSaleNew(String searchKeyWord);
    boolean submitBrandingRequisition(BrandingRequestDetailsData brandingRequestDetailsData);
    List<BrandingRequestDetailsModel> viewBrandingRequestDetails();
    SearchPageData<BrandingRequestDetailsModel> getBrandingRequestDetails(String filter, String startDate, String endDate, List<String> requestStatus, List<String> brandingSiteType,SearchPageData searchPageData);
    DropdownListData getBrandingSiteTypeDropDownList(String brandingSiteType);
    DropdownListData getEnumTypes(String type);
    boolean submitActivityVerificationDetailsForRequest(BrandingRequestDetailsData activityVerificationDetailsData);
    BrandingRequestDetailsModel getBrandingRequestDetailsByReqNumber(String reqNumber);
    BrandingActivityVerficationModel getActivityDetailsForRequest(String requisitionNumber);
    public DropdownListData findAllState();
    public DropdownListData findAllDistrict(String state);
    public DropdownListData findAllTaluka(String state, String district);
    public DropdownListData findAllErpCity(String state, String district, String taluka);
    public BrandingTrackingStatusModel getTrackingStatusDetailsForRequest(String requisitionNumber, String requestStatus);
    public List<BrandingTrackStatusStageGateModel> getBrandingTrackHistoryDetails(String requestNumber);
    boolean updateBrandingRequestStatus(String requisitionNo, String status, String comments);

    BrandingRequestData submitBrandingRequisitionSlct(BrandingRequestDetailsData brandingRequestDetailsData);
}
