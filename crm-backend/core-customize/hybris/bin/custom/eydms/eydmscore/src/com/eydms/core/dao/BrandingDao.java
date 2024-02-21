package com.eydms.core.dao;

import com.eydms.core.enums.BrandingRequestStatus;
import com.eydms.core.model.*;
import com.eydms.facades.data.ActivityVerificationDetailsData;
import com.eydms.facades.data.BrandingRequestDetailsData;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.Date;
import java.util.List;

public interface BrandingDao {
    List<List<Object>> getCounterDetailsForPointOfSale(String searchKeyWord);
    EyDmsCustomerModel getCounterDetailsForPointOfSaleNew(String searchKeyWord);
    List<BrandingRequestDetailsModel> viewBrandingRequestDetails();
    SearchPageData<BrandingRequestDetailsModel> getBrandingRequestDetails(String filter, String startDate, String endDate, List<String> requestStatus, List<String> brandingSiteType,SearchPageData searchPageData);
    BrandingRequestDetailsModel getBrandingRequestDetailsByReqNumber(String reqNumber);
    BrandingActivityVerficationModel getActivityDetailsForRequest(String requisitionNumber);
    public BrandingTrackingStatusModel getTrackingStatusDetailsForRequest(String requisitionNumber, String requestStatus);
    BrandingTrackingStatusModel getBrandingTrackingDetailsByReqNumber(String reqNumber);
    public List<BrandingTrackStatusStageGateModel> getBrandingTrackHistoryDetails(String requestNumber);
}
