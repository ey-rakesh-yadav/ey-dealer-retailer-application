package com.scl.facades.impl;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.enums.BrandingRequestStatus;
import com.scl.core.enums.BrandingSiteType;
import com.scl.core.enums.BrandingType;
import com.scl.core.enums.SclUserType;
import com.scl.core.model.*;
import com.scl.core.services.BrandingService;
import com.scl.core.services.TerritoryManagementService;
import com.scl.facades.BrandingFacade;
import com.scl.facades.data.*;
import com.scl.facades.order.data.BrandingTrackingData;
import com.scl.facades.order.data.BrandingTrackingListData;
import com.scl.facades.prosdealer.data.DealerListData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.commercefacades.product.data.ImageData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.session.SessionPerformanceTest;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class BrandingFacadeImpl implements BrandingFacade {
    @Resource
    BrandingService brandingService;
    @Autowired
    UserService userService;
    @Autowired
    private Converter<AddressModel, SCLAddressData> sclAddressConverter;
    @Resource
    private Converter<MediaModel, ImageData> imageConverter;

    @Resource
    Converter<SclCustomerModel,CustomerData> dealerBasicConverter;
    @Resource
    private EnumerationService enumerationService;

    public Converter<SclCustomerModel, CustomerData> getDealerBasicConverter() {
        return dealerBasicConverter;
    }

    public void setDealerBasicConverter(Converter<SclCustomerModel, CustomerData> dealerBasicConverter) {
        this.dealerBasicConverter = dealerBasicConverter;
    }
    private static final Logger LOGGER = LoggerFactory.getLogger(BrandingFacadeImpl.class);

    public BrandingService getBrandingService() {
        return brandingService;
    }

    public void setBrandingService(BrandingService brandingService) {
        this.brandingService = brandingService;
    }

    public EnumerationService getEnumerationService() {
        return enumerationService;
    }

    public void setEnumerationService(EnumerationService enumerationService) {
        this.enumerationService = enumerationService;
    }

    public Converter<MediaModel, ImageData> getImageConverter() {
        return imageConverter;
    }

    public void setImageConverter(Converter<MediaModel, ImageData> imageConverter) {
        this.imageConverter = imageConverter;
    }

    @Resource
    TerritoryManagementService territoryManagementService;

    @Resource
    private Converter<BrandingRequestDetailsModel, List<BrandingTrackingData>> sclBrandingReqTrackerConverter;

    @Override
    public BrandingRequestDetailsData getCounterDetailsForPointOfSale(String searchKeyWord) {
        BrandingRequestDetailsData brandingRequestDetailsData=new BrandingRequestDetailsData();
        SclCustomerModel model = brandingService.getCounterDetailsForPointOfSaleNew(searchKeyWord);
       if(model!=null) {
            brandingRequestDetailsData.setCounterCode(model.getUid());
            brandingRequestDetailsData.setCounterName(model.getName());
            brandingRequestDetailsData.setPrimaryContactNumber(model.getMobileNumber());
            Collection<AddressModel> list = model.getAddresses();
            if (CollectionUtils.isNotEmpty(list)) {
                List<AddressModel> billingAddressList = list.stream().filter(a -> a.getBillingAddress()).collect(Collectors.toList());
                if (billingAddressList != null && !billingAddressList.isEmpty()) {
                    AddressModel billingAddress = billingAddressList.get(0);
                    if (null != billingAddress) {
                        brandingRequestDetailsData.setLocation((sclAddressConverter.convert(billingAddress)));
                    }
                }
            }
        }


     /*  List<List<Object>> counterDetailsForPointOfSale = brandingService.getCounterDetailsForPointOfSale(searchKeyWord);
        if(!counterDetailsForPointOfSale.isEmpty()) {
            for (List<Object> objects : counterDetailsForPointOfSale) {
                brandingRequestDetailsData.setCounterCode((String) objects.get(0));
                brandingRequestDetailsData.setCounterName((String) objects.get(1));
                brandingRequestDetailsData.setPrimaryContactNumber((String) objects.get(2));

                String address=new String();
                if(objects.get(6)!=null) {
                    address = objects.get(3).toString().concat(objects.get(4).toString().concat(objects.get(5).toString()).concat(objects.get(6).toString()).concat(",").concat(objects.get(7).toString()));
                }
                else {
                    address = objects.get(3).toString().concat(objects.get(4).toString().concat(",").concat(objects.get(5).toString()).concat(",").concat(objects.get(7).toString()));
                }
                brandingRequestDetailsData.setLocation(address);
            }
        }*/
            return brandingRequestDetailsData;
    }

    @Override
    public boolean submitBrandingRequisition(BrandingRequestDetailsData brandingRequestDetailsData) {
        return brandingService.submitBrandingRequisition(brandingRequestDetailsData);
    }
    @Override
    public SearchPageData<BrandingRequestDetailsData> getBrandingRequestDetails(String filter, String startDate, String endDate, List<String> requestStatus, List<String> brandingSiteType, SearchPageData searchPageData) throws ParseException {
        final SearchPageData<BrandingRequestDetailsData> results = new SearchPageData<>();
        List<BrandingRequestDetailsData> detailsDataList = new ArrayList<>();
        SearchPageData<BrandingRequestDetailsModel> brandingRequestDetails = brandingService.getBrandingRequestDetails(filter, startDate,endDate, requestStatus,brandingSiteType,searchPageData);
        if(brandingRequestDetails!=null && !brandingRequestDetails.getResults().isEmpty()) {
            for (BrandingRequestDetailsModel brandRequestDetailsModel : brandingRequestDetails.getResults()) {
                if (brandRequestDetailsModel != null) {
                    BrandingRequestDetailsData data = new BrandingRequestDetailsData();
                    data.setRequestTitle(brandRequestDetailsModel.getRequestTitle());
                    data.setRequisitionNumber(brandRequestDetailsModel.getRequisitionNumber());

                    String reqStatus = null, brandType = null, brandSiteType = null,dealerReqStatus=null;

                     if (brandRequestDetailsModel.getBrandingType() != null) {
                        brandType = getEnumerationService().getEnumerationName(brandRequestDetailsModel.getBrandingType());
                    }
                    if (brandRequestDetailsModel.getBrandSiteType() != null) {
                        brandSiteType = getEnumerationService().getEnumerationName(brandRequestDetailsModel.getBrandSiteType());
                    }
                    if (brandRequestDetailsModel.getRequestStatus() != null) {
                        reqStatus = getEnumerationService().getEnumerationName(brandRequestDetailsModel.getRequestStatus());
                    }
                    B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
                    if (((currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) ||
                            ((currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))))){

                        if (brandRequestDetailsModel.getDealerRequestStatus() != null) {
                            dealerReqStatus = getEnumerationService().getEnumerationName(brandRequestDetailsModel.getDealerRequestStatus());
                            data.setRequestStatus(dealerReqStatus);
                            LOGGER.info("Dealer Request Status:"+dealerReqStatus);
                            data.setRequestStatusCode(brandRequestDetailsModel.getDealerRequestStatus().getCode());
                        }
                        if(reqStatus!=null && reqStatus.toUpperCase().contains("REJECTED")) {
                            reqStatus = "Rejected";
                            data.setRequestStatus(reqStatus);
                        }
                        if(reqStatus!=null && reqStatus.toUpperCase().contains("CANCELLED")) {
                            reqStatus = "Cancelled";
                            data.setRequestStatus(reqStatus);
                        }
                        else if (!reqStatus.toUpperCase().contains("CANCELLED") && !reqStatus.toUpperCase().contains("REJECTED")) {
                            if (brandRequestDetailsModel.getNshApprovedDate()==null)
                            reqStatus = "Pending";
                    	else if (brandRequestDetailsModel.getNshApprovedDate()!=null && brandRequestDetailsModel.getInvoiceUploadedDate()==null)
                            reqStatus = "Approved";
                    	else if (brandRequestDetailsModel.getInvoiceUploadedDate()!=null)
                            reqStatus = "Completed";
                            data.setRequestStatus(reqStatus);
                        }
                    }
                    else{
                        data.setRequestStatus(reqStatus);
                        LOGGER.info("Request Status:"+requestStatus);
                        data.setRequestStatusCode(brandRequestDetailsModel.getRequestStatus().getCode());
                    }
                    data.setBrandingSiteType(brandSiteType);
                    data.setBrandingType(brandType);
                    data.setQuantity(brandRequestDetailsModel.getQuantity());
                    data.setCounterCode(brandRequestDetailsModel.getCounterCode());
                    data.setCounterName(brandRequestDetailsModel.getCounterName());
                    data.setPrimaryContactNumber(brandRequestDetailsModel.getPrimaryContactNumber());
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    if(brandRequestDetailsModel.getRequestRaisedDate()!=null) {
                        String requestRaisedDate = formatter.format(brandRequestDetailsModel.getRequestRaisedDate());
                        data.setRequestRaisedDate(requestRaisedDate);
                    }
                    data.setVendorName(brandRequestDetailsModel.getVendorName());
                    data.setVendorDetails(brandRequestDetailsModel.getVendorDetails());
                    if(brandRequestDetailsModel.getDateOfCompletion()!=null) {
                        String completedOn = formatter.format(brandRequestDetailsModel.getDateOfCompletion());
                        data.setCompletedOn(completedOn);
                        data.setDateOfCompletion(completedOn);
                    }
                    data.setRequestRaisedBy(brandRequestDetailsModel.getRequestRaisedBy().getName());

                    //When to show modify button
                    if(brandRequestDetailsModel.getLbtApprovedDate()==null){
                        if(((brandRequestDetailsModel.getRequestRaisedBy().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) ||
                                ((brandRequestDetailsModel.getRequestRaisedBy().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))))) {
                            data.setEnableModify(true);
                        }
                        data.setEnableModify(false);
                    }
                    else{
                        data.setEnableModify(false);
                    }

                    //When to show give feedback button
                    if (brandRequestDetailsModel.getActivityVerificationDate()==null && brandRequestDetailsModel.getBrandComments() == null &&
                       brandRequestDetailsModel.getReqRaisedRole()!=null) {
                        data.setEnableFeedback(true);
                    }
                    else if (brandRequestDetailsModel.getActivityVerificationDate() == null && brandRequestDetailsModel.getNshApprovedDate() != null
                            && brandRequestDetailsModel.getRequestStatus().equals(BrandingRequestStatus.NSH_APPROVED) &&
                            brandRequestDetailsModel.getRequestRaisedBy().equals(currentUser) && brandRequestDetailsModel.getBrandComments() == null) {
                        data.setEnableFeedback(true);
                    } else if (brandRequestDetailsModel.getActivityVerificationDate() == null && brandRequestDetailsModel.getBrandComments() != null
                            && currentUser instanceof SclUserModel && currentUser.getUserType() != null && currentUser.getUserType().equals(SclUserType.SO)) {
                        data.setEnableFeedback(true);
                    } else {
                        data.setEnableFeedback(false);
                    }

           /* for reference:
            DO -
            Activity Verification - if enableFeedback is true
            Activity Details - if viewFeedback is true
            Dealer
            Give feedback - if enableFeedback is true
            View feedback  - if viewFeedback is true*/

                    //this boolean to be used in Dealer persona
                    if(brandRequestDetailsModel.getActivityVerificationDate()!=null)//SO
                        data.setIsViewFeedback(true);
                    else if(brandRequestDetailsModel.getBrandComments()!=null &&
                            ((currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) ||
                                    ((currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))))))//Dealer
                        data.setIsViewFeedback(true);
                    else
                        data.setIsViewFeedback(false);

                    //when to show invoice upload(dealer raised)
                    if(brandRequestDetailsModel.getRequestRaisedBy().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)) ||
                            ((currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))))) {
                        if(brandRequestDetailsModel.getActivityVerificationDate()!=null && brandRequestDetailsModel.getInvoiceUploadedDate()==null) {
                        data.setEnabledInvoiceUpload(true);
                        }
                        data.setIsCustomerRaised(true);
                    }else{
                        data.setIsCustomerRaised(false);
                        data.setEnabledInvoiceUpload(false);
                    }
                    if(brandRequestDetailsModel.getReqRaisedRole()!=null) {
                        data.setReqRaisedRole(brandRequestDetailsModel.getReqRaisedRole());
                    }
                    data.setOtherBrandingType(brandRequestDetailsModel.getOtherBrandingType()!=null?brandRequestDetailsModel.getOtherBrandingType():"");
                    data.setSlctReqNo(brandRequestDetailsModel.getSlctReqNo()!=null?brandRequestDetailsModel.getSlctReqNo():"");
                    detailsDataList.add(data);
                }
            }
            results.setSorts(brandingRequestDetails.getSorts());
            results.setResults(detailsDataList);
            results.setPagination(brandingRequestDetails.getPagination());
        }
        return results;
    }

    @Override
    public DropdownListData getBrandingSiteTypeDropDownList(String brandingSiteType) {
        return brandingService.getBrandingSiteTypeDropDownList(brandingSiteType);
    }

    @Override
    public DropdownListData getEnumTypes(String type) {
        return brandingService.getEnumTypes(type);
    }

    @Override
    public boolean submitActivityVerificationDetailsForRequest(BrandingRequestDetailsData brandingRequestDetailsData) {
        return brandingService.submitActivityVerificationDetailsForRequest(brandingRequestDetailsData);
    }

    @Override
    public BrandingRequestDetailsData getBrandReqDetailsForActivityVerification(String requisitionNumber) {
        BrandingRequestDetailsModel brandingRequestDetails = brandingService.getBrandingRequestDetailsByReqNumber(requisitionNumber);
        BrandingRequestDetailsData data=new BrandingRequestDetailsData();
        if(brandingRequestDetails!=null)
        {
            data.setRequisitionNumber(brandingRequestDetails.getRequisitionNumber());
            String reqStatus = getEnumerationService().getEnumerationName(brandingRequestDetails.getRequestStatus());
            String brandType=getEnumerationService().getEnumerationName(brandingRequestDetails.getBrandingType());
            String brandSiteType=getEnumerationService().getEnumerationName(brandingRequestDetails.getBrandSiteType());
            data.setRequestStatus(reqStatus);
            data.setBrandingSiteType(brandSiteType);
            data.setBrandingType(brandType);
            data.setPrimaryContactNumber(brandingRequestDetails.getPrimaryContactNumber());
            data.setSecondaryContactNumber(brandingRequestDetails.getSecondaryContactNumber());
            data.setQuantity(brandingRequestDetails.getQuantity());
            data.setDetails(brandingRequestDetails.getDetails());
            data.setDimensions(brandingRequestDetails.getDimensions());
            data.setLength(brandingRequestDetails.getLength());
            data.setHeight(brandingRequestDetails.getHeight());
            if(brandingRequestDetails.getBrandSiteType().getCode().equals(BrandingSiteType.POINT_OF_SALE.getCode()) ||
            (brandingRequestDetails.getBrandSiteType().getCode().equals(BrandingSiteType.DEALER_COSTSHARING_BRANDING.getCode()))) {
                data.setCounterName(brandingRequestDetails.getCounterName());
                data.setCounterCode(brandingRequestDetails.getCounterCode());
                if(brandingRequestDetails.getCounterErpCustNo()!=null) {
                    data.setErpCustomerNo(brandingRequestDetails.getCounterErpCustNo());
                }
                if(brandingRequestDetails.getLocation()==null){
                    if(brandingRequestDetails.getCounterCode()!=null) {
                        BrandingRequestDetailsData loc = getCounterDetailsForPointOfSale(brandingRequestDetails.getCounterCode());
                        if(loc!=null && loc.getLocation()!=null) {
                            data.setLocation(loc.getLocation());
                            if(loc.getLocation().getLine1()!=null && loc.getLocation().getLine2()!=null)
                            data.setLocationNew(loc.getLocation().getLine1()+" "+loc.getLocation().getLine2());
                            else if(loc.getLocation().getLine1()!=null && loc.getLocation().getLine2()==null)
                                data.setLocationNew(loc.getLocation().getLine1());
                            else if(loc.getLocation().getLine1()==null && loc.getLocation().getLine2()!=null)
                                data.setLocationNew(loc.getLocation().getLine2());
                        }
                    }
                }

                List<MediaModel> list =  brandingRequestDetails.getBeforeBrandingPhotos();
                List<MediaDetailsData> imageList = new ArrayList<>();
                if(list!=null && !list.isEmpty()) {
                    for (MediaModel mediaModel : list) {
                        //  final ImageData imageData = getImageConverter().convert(mediaModel);
                        if (mediaModel != null) {
                            MediaDetailsData mediaDetailsData = new MediaDetailsData();
                            mediaDetailsData.setCode(mediaModel.getCode());
                            mediaDetailsData.setAltText(mediaModel.getAltText());
                            mediaDetailsData.setUrl(mediaModel.getURL());
                            mediaDetailsData.setName(mediaModel.getRealFileName());
                            imageList.add(mediaDetailsData);
                        }
                    }
                }
                data.setDownloadBeforeBrandingPhotos(imageList);
            }
            if(brandingRequestDetails.getBrandSiteType().getCode().equals(BrandingSiteType.OUTDOORS.getCode())) {
                if(brandingRequestDetails.getCounterCode()!=null) {
                    data.setCounterCode(brandingRequestDetails.getCounterCode());
                }
                if(brandingRequestDetails.getCounterErpCustNo()!=null) {
                    data.setErpCustomerNo(brandingRequestDetails.getCounterErpCustNo());
                }
                data.setSiteName(brandingRequestDetails.getCounterName());
                data.setSiteAddressLine1(brandingRequestDetails.getSiteAddressLine1());
                data.setSiteAddressLine2(brandingRequestDetails.getSiteAddressLine2());
                data.setCity(brandingRequestDetails.getCity());
                data.setState(brandingRequestDetails.getState());
                data.setDistrict(brandingRequestDetails.getDistrict());
                data.setTaluka(brandingRequestDetails.getTaluka());
                List<MediaModel> list =  brandingRequestDetails.getBeforeBrandingPhotos();
                List<MediaDetailsData> imageList = new ArrayList<>();
                if(list!=null && !list.isEmpty()) {
                    for (MediaModel mediaModel : list) {
                        //  final ImageData imageData = getImageConverter().convert(mediaModel);
                        if (mediaModel != null) {
                            MediaDetailsData mediaDetailsData = new MediaDetailsData();
                            mediaDetailsData.setCode(mediaModel.getCode());
                            mediaDetailsData.setAltText(mediaModel.getAltText());
                            mediaDetailsData.setUrl(mediaModel.getURL());
                            mediaDetailsData.setName(mediaModel.getRealFileName());
                            imageList.add(mediaDetailsData);
                        }
                    }
                }
                data.setDownloadBeforeBrandingPhotos(imageList);
            }

            B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
            if(((currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) ||
                    ((currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))))
            {
                data.setRequestStatus(brandingRequestDetails.getDealerRequestStatus().getCode());

              /*  if(reqStatus!=null && reqStatus.toUpperCase().contains("REJECTED")) {
                    reqStatus = "Rejected";
                    data.setRequestStatus(reqStatus);
                }
                if(reqStatus!=null && reqStatus.toUpperCase().contains("CANCELLED")) {
                    reqStatus = "Cancelled";
                    data.setRequestStatus(reqStatus);
                }
               // else if (!brandingRequestDetails.getRequestStatus().getCode().equalsIgnoreCase("CANCELLED") && !brandingRequestDetails.getRequestStatus().getCode().equalsIgnoreCase("REJECTED")) {
                else if (!reqStatus.toUpperCase().contains("CANCELLED") && !reqStatus.toUpperCase().contains("REJECTED")) {
                    if (brandingRequestDetails.getNshApprovedDate()==null)
                        reqStatus = "Pending";
                    else if (brandingRequestDetails.getNshApprovedDate()!=null && brandingRequestDetails.getInvoiceUploadedDate()==null)
                        reqStatus = "Approved";
                    else if (brandingRequestDetails.getInvoiceUploadedDate()!=null)
                        reqStatus = "Completed";
                    data.setRequestStatus(reqStatus);
                }*/
            }else {
                data.setRequestStatus(brandingRequestDetails.getRequestStatus().getCode());
            }
            //When to show modify button
            if(brandingRequestDetails.getLbtApprovedDate()==null){
                if(((brandingRequestDetails.getRequestRaisedBy().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) ||
                        ((brandingRequestDetails.getRequestRaisedBy().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))))) {
                    data.setEnableModify(true);
                }
                data.setEnableModify(false);
            }
            else{
                data.setEnableModify(false);
            }
            //When to show give feedback button
            if(brandingRequestDetails.getActivityVerificationDate()==null && brandingRequestDetails.getNshApprovedDate()!=null
                    && brandingRequestDetails.getRequestStatus().equals(BrandingRequestStatus.NSH_APPROVED) &&
                        brandingRequestDetails.getRequestRaisedBy().equals(currentUser) && brandingRequestDetails.getBrandComments()==null){
                data.setEnableFeedback(true);
            }else if(brandingRequestDetails.getActivityVerificationDate()==null && brandingRequestDetails.getBrandComments()!=null
                    && currentUser instanceof SclUserModel  && currentUser.getUserType()!=null && currentUser.getUserType().equals(SclUserType.SO)){
                     data.setEnableFeedback(true);
            }
            else{
                data.setEnableFeedback(false);
            }

           /* for reference:
            DO -
            Activity Verification - if enableFeedback is true
            Activity Details - if viewFeedback is true
            Dealer
            Give feedback - if enableFeedback is true
            View feedback  - if viewFeedback is true*/

            //this boolean to be used in Dealer persona
            if(brandingRequestDetails.getActivityVerificationDate()!=null)//SO
                    data.setIsViewFeedback(true);
            else if(brandingRequestDetails.getBrandComments()!=null &&
                    ((currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) ||
                    ((currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))))))//Dealer
                 data.setIsViewFeedback(true);
            else
                data.setIsViewFeedback(false);


            //when to show invoice upload(dealer raised)
            if(brandingRequestDetails.getRequestRaisedBy().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)) ||
                    (brandingRequestDetails.getRequestRaisedBy().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
                if(brandingRequestDetails.getActivityVerificationDate()!=null && brandingRequestDetails.getInvoiceUploadedDate()==null) {
                    data.setEnabledInvoiceUpload(true);
                }
                data.setIsCustomerRaised(true);
            }else{
                data.setIsCustomerRaised(false);
                data.setEnabledInvoiceUpload(false);
            }
            if(brandingRequestDetails.getRequestRaisedBy()!=null && brandingRequestDetails.getRequestRaisedBy().getUserType()!=null) {
                if (brandingRequestDetails.getRequestRaisedBy().getUserType().getCode().equalsIgnoreCase("SO")) {
                    data.setRequestRaisedBy("SO");
                }else if(brandingRequestDetails.getRequestRaisedBy().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))){
                    data.setRequestRaisedBy("Retailer");
                }else if(brandingRequestDetails.getRequestRaisedBy().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))){
                    data.setRequestRaisedBy("Dealer");
                }
            }
        }
        return data;
    }

    @Override
    public BrandingRequestDetailsData getActivityDetailsForRequest(String requisitionNumber) {
        BrandingRequestDetailsModel activityDetailsForRequest = brandingService.getBrandingRequestDetailsByReqNumber(requisitionNumber);
        //BrandingActivityVerficationModel activityDetailsForRequest = brandingService.getActivityDetailsForRequest(requisitionNumber);
        //ActivityVerificationDetailsData data=new ActivityVerificationDetailsData();
        BrandingRequestDetailsData data=new BrandingRequestDetailsData();
        if(activityDetailsForRequest!=null){
            String brandType=getEnumerationService().getEnumerationName(activityDetailsForRequest.getBrandingType());
            String brandSiteType=getEnumerationService().getEnumerationName(activityDetailsForRequest.getBrandSiteType());
            data.setBrandingSiteType(brandSiteType);
            data.setBrandingType(brandType);
            data.setBrandingSiteTypeCode(activityDetailsForRequest.getBrandSiteType().getCode());
            data.setBrandingTypeCode(activityDetailsForRequest.getBrandingType().getCode());
            data.setPrimaryContactNumber(activityDetailsForRequest.getPrimaryContactNumber());
            data.setSecondaryContactNumber(activityDetailsForRequest.getSecondaryContactNumber());
            data.setQuantity(activityDetailsForRequest.getQuantity());
            data.setDimensions(activityDetailsForRequest.getDimensions());
            data.setDetails(activityDetailsForRequest.getDetails());
            if(activityDetailsForRequest.getBrandSiteType().getCode().equals(BrandingSiteType.POINT_OF_SALE.getCode())){

                data.setCounterCode(activityDetailsForRequest.getCounterCode());
                data.setCounterName(activityDetailsForRequest.getCounterName());
               if(activityDetailsForRequest.getLocation()==null){
                   if(activityDetailsForRequest.getCounterCode()!=null) {
                       BrandingRequestDetailsData loc = getCounterDetailsForPointOfSale(activityDetailsForRequest.getCounterCode());
                       if(loc!=null && loc.getLocation()!=null) {
                           data.setLocation(loc.getLocation());
                           if(loc.getLocation().getLine1()!=null && loc.getLocation().getLine2()!=null)
                               data.setLocationNew(loc.getLocation().getLine1()+" "+loc.getLocation().getLine2());
                           else if(loc.getLocation().getLine1()!=null && loc.getLocation().getLine2()==null)
                               data.setLocationNew(loc.getLocation().getLine1());
                           else if(loc.getLocation().getLine1()==null && loc.getLocation().getLine2()!=null)
                               data.setLocationNew(loc.getLocation().getLine2());
                       }
                   }
                }
                /*data.setLocation(activityDetailsForRequest.getLocation());*/
                data.setLength(activityDetailsForRequest.getLength());
                data.setHeight(activityDetailsForRequest.getHeight());
                List<MediaModel> list =  activityDetailsForRequest.getBeforeBrandingPhotos();
                List<MediaDetailsData> imageList = new ArrayList<>();
                if(list!=null && !list.isEmpty()) {
                    for (MediaModel mediaModel : list) {
                        //  final ImageData imageData = getImageConverter().convert(mediaModel);
                        if (mediaModel != null) {
                            MediaDetailsData mediaDetailsData = new MediaDetailsData();
                            mediaDetailsData.setCode(mediaModel.getCode());
                            mediaDetailsData.setAltText(mediaModel.getAltText());
                            mediaDetailsData.setUrl(mediaModel.getURL());
                            mediaDetailsData.setName(mediaModel.getRealFileName());
                            imageList.add(mediaDetailsData);
                        }
                    }
                }
                data.setDownloadBeforeBrandingPhotos(imageList);
            }
            if(activityDetailsForRequest.getBrandSiteType().getCode().equals(BrandingSiteType.OUTDOORS.getCode())) {
                data.setSiteName(activityDetailsForRequest.getSiteName());
                data.setSiteAddressLine1(activityDetailsForRequest.getSiteAddressLine1());
                data.setSiteAddressLine2(activityDetailsForRequest.getSiteAddressLine2());
                data.setCity(activityDetailsForRequest.getCity());
                data.setState(activityDetailsForRequest.getState());
                data.setDistrict(activityDetailsForRequest.getDistrict());
                data.setTaluka(activityDetailsForRequest.getTaluka());
                data.setDimensions(activityDetailsForRequest.getDimensions());
                data.setLatitude(activityDetailsForRequest.getLatitude());
                data.setLongitude(activityDetailsForRequest.getLongitude());
                data.setLength(activityDetailsForRequest.getLength());
                data.setHeight(activityDetailsForRequest.getHeight());
                List<MediaModel> list =  activityDetailsForRequest.getBeforeBrandingPhotos();
                List<MediaDetailsData> imageList = new ArrayList<>();
                if(list!=null && !list.isEmpty()) {
                    for (MediaModel mediaModel : list) {
                        //  final ImageData imageData = getImageConverter().convert(mediaModel);
                        if (mediaModel != null) {
                            MediaDetailsData mediaDetailsData = new MediaDetailsData();
                            mediaDetailsData.setCode(mediaModel.getCode());
                            mediaDetailsData.setAltText(mediaModel.getAltText());
                            mediaDetailsData.setUrl(mediaModel.getURL());
                            mediaDetailsData.setName(mediaModel.getRealFileName());
                            imageList.add(mediaDetailsData);
                        }
                    }
                }
                data.setDownloadBeforeBrandingPhotos(imageList);
            }
            if(activityDetailsForRequest.getBrandSiteType().getCode().equals(BrandingSiteType.DEALER_COSTSHARING_BRANDING.getCode())) {
                data.setCounterCode(activityDetailsForRequest.getCounterCode());
                data.setCounterName(activityDetailsForRequest.getCounterName());
            }
            List<String> photoAfterBrand=new ArrayList<>();
            List<String> uploadInvoice=new ArrayList<>();
            if(activityDetailsForRequest.getAfterBrandingPhotos()!=null && !activityDetailsForRequest.getAfterBrandingPhotos().isEmpty()) {
                List<MediaModel> list =  activityDetailsForRequest.getAfterBrandingPhotos();
                List<MediaDetailsData> imageList = new ArrayList<>();
                if(list!=null && !list.isEmpty()) {
                    for (MediaModel mediaModel : list) {
                        //  final ImageData imageData = getImageConverter().convert(mediaModel);
                        if (mediaModel != null) {
                            MediaDetailsData mediaDetailsData = new MediaDetailsData();
                            mediaDetailsData.setCode(mediaModel.getCode());
                            mediaDetailsData.setAltText(mediaModel.getAltText());
                            mediaDetailsData.setUrl(mediaModel.getURL());
                            mediaDetailsData.setName(mediaModel.getRealFileName());
                            imageList.add(mediaDetailsData);
                        }
                    }
                }
                data.setDownloadAfterBrandingPhotos(imageList);
            }
            if(activityDetailsForRequest.getInvoiceAmount()!=null){
                data.setInvoiceAmount(activityDetailsForRequest.getInvoiceAmount());
            }
            if(activityDetailsForRequest.getUploadInvoice()!=null && !activityDetailsForRequest.getUploadInvoice().isEmpty()) {
                List<MediaModel> list =  activityDetailsForRequest.getUploadInvoice();
                List<MediaDetailsData> imageList = new ArrayList<>();
                if(list!=null && !list.isEmpty()) {
                    for (MediaModel mediaModel : list) {
                        //  final ImageData imageData = getImageConverter().convert(mediaModel);
                        if (mediaModel != null) {
                            MediaDetailsData mediaDetailsData = new MediaDetailsData();
                            mediaDetailsData.setCode(mediaModel.getCode());
                            mediaDetailsData.setAltText(mediaModel.getAltText());
                            mediaDetailsData.setUrl(mediaModel.getURL());
                            mediaDetailsData.setName(mediaModel.getRealFileName());
                            imageList.add(mediaDetailsData);
                        }
                    }
                }
                data.setDownloadInvoice(imageList);
            }
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            if(activityDetailsForRequest.getDateOfCompletion()!=null) {
                String dateOfCompletion = formatter.format(activityDetailsForRequest.getDateOfCompletion());
                data.setDateOfCompletion(dateOfCompletion);
            }
            data.setResponsiveness(activityDetailsForRequest.getResponsiveness());
            data.setCompletionTime(activityDetailsForRequest.getCompletionTime());
            data.setQualityOfWork(activityDetailsForRequest.getQualityOfWork());
            data.setExperience(activityDetailsForRequest.getExperience());
            data.setComments(activityDetailsForRequest.getBrandComments());
            data.setFeedBack(activityDetailsForRequest.getFeedback());
            //new fields
            if(activityDetailsForRequest.getStartDate()!=null){
                String startDate = formatter.format(activityDetailsForRequest.getStartDate());
                data.setStartDate(startDate);
            }
            if(activityDetailsForRequest.getPlanningDateOfCompletion()!=null) {
                String planDateOfCompletion = formatter.format(activityDetailsForRequest.getPlanningDateOfCompletion());
                data.setPlanningDateOfCompletion(planDateOfCompletion);
            }
            data.setObjectiveOfActivity(activityDetailsForRequest.getObjectiveOfActivity());
            data.setObjectiveTargetPercentage(activityDetailsForRequest.getObjectiveOfTargetPercentage());
            data.setBudgetPlanned(activityDetailsForRequest.getBudgetPlanned());
            //When to show modify button
            if(activityDetailsForRequest.getLbtApprovedDate()==null){
                if(((activityDetailsForRequest.getRequestRaisedBy().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) ||
                        ((activityDetailsForRequest.getRequestRaisedBy().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))))) {
                    data.setEnableModify(true);
                }
                data.setEnableModify(false);
            }
            else{
                data.setEnableModify(false);
            }
            String reqStatus = getEnumerationService().getEnumerationName(activityDetailsForRequest.getRequestStatus());
            B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
            if(((currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) ||
                    ((currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))))){
                data.setRequestStatus(activityDetailsForRequest.getDealerRequestStatus().getCode());
            /*    if(reqStatus!=null && reqStatus.toUpperCase().contains("REJECTED")) {
                    reqStatus = "Rejected";
                    data.setRequestStatus(reqStatus);
                }
                else if(reqStatus!=null && reqStatus.toUpperCase().contains("CANCELLED")) {
                    reqStatus = "Cancelled";
                    data.setRequestStatus(reqStatus);
                }
                else if (reqStatus!=null && !reqStatus.toUpperCase().contains("CANCELLED") && !reqStatus.toUpperCase().contains("REJECTED")) {
                    if (activityDetailsForRequest.getNshApprovedDate() == null)
                        reqStatus = "Pending";
                    else if (activityDetailsForRequest.getNshApprovedDate() != null && activityDetailsForRequest.getInvoiceUploadedDate()==null)
                        reqStatus = "Approved";
                    else if (activityDetailsForRequest.getInvoiceUploadedDate() != null)
                        reqStatus = "Completed";
                    data.setRequestStatus(reqStatus);
                }*/
            }else{
                data.setRequestStatus(activityDetailsForRequest.getRequestStatus().getCode());
            }

            //when to show invoice upload(dealer raised)
            if(activityDetailsForRequest.getRequestRaisedBy().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)) ||
                    (activityDetailsForRequest.getRequestRaisedBy().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))))
            {
                if(activityDetailsForRequest.getActivityVerificationDate()!=null && activityDetailsForRequest.getInvoiceUploadedDate()==null) {
                    data.setEnabledInvoiceUpload(true);
                }else{
                    data.setEnabledInvoiceUpload(false);
                }
                data.setIsCustomerRaised(true);
            }else{
                data.setIsCustomerRaised(false);
                data.setEnabledInvoiceUpload(false);
            }
            if(activityDetailsForRequest.getRequestRaisedBy()!=null && activityDetailsForRequest.getRequestRaisedBy().getUserType()!=null) {
                if (activityDetailsForRequest.getRequestRaisedBy().getUserType().getCode().equalsIgnoreCase("SO")) {
                    data.setRequestRaisedBy("SO");
                }else if(activityDetailsForRequest.getRequestRaisedBy().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))){
                    data.setRequestRaisedBy("Retailer");
                }else if(activityDetailsForRequest.getRequestRaisedBy().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))){
                    data.setRequestRaisedBy("Dealer");
                }
            }
        }
        return data;
    }

    @Override
    public DealerListData getAllDealerRetailerList(boolean isDealer,boolean isRetailer) {
        //List<SclCustomerModel> customerList =  territoryManagementService.getSCLAndNonSCLDealersRetailersForSubArea();
        RequestCustomerData customerDataDealer = new RequestCustomerData();
        RequestCustomerData customerDataRetailer = new RequestCustomerData();
        List<SclCustomerModel> dealerList=new ArrayList<>();
        List<SclCustomerModel> retailerList=new ArrayList<>();
        if(isDealer) {
            customerDataDealer.setCounterType(List.of("DEALER"));
            dealerList = territoryManagementService.getCustomerforUser(customerDataDealer);
        }
        else if(isRetailer) {
            customerDataRetailer.setCounterType(List.of("RETAILER"));
            retailerList = territoryManagementService.getCustomerforUser(customerDataRetailer);
        }
        else{
            customerDataDealer.setCounterType(List.of("DEALER"));
            customerDataRetailer.setCounterType(List.of("RETAILER"));
           dealerList = territoryManagementService.getCustomerforUser(customerDataDealer);
           retailerList = territoryManagementService.getCustomerforUser(customerDataRetailer);
        }
        List<SclCustomerModel> customerList=new ArrayList<>();
        customerList.addAll(dealerList);
        customerList.addAll(retailerList);
        List<CustomerData> allData= Optional.of(customerList.stream()
                .map(b2BCustomer -> dealerBasicConverter
                        .convert(b2BCustomer)).collect(Collectors.toList())).get();
        DealerListData dataList = new DealerListData();
        dataList.setDealers(allData.stream().filter(cus->Objects.nonNull(cus.getName())).sorted(Comparator.comparing(CustomerData::getName)).collect(Collectors.toList()));
        return dataList;
    }
    @Override
    public DealerListData getAllDealersForSubArea() {
        RequestCustomerData customerData=new RequestCustomerData();
        customerData.setCounterType(List.of("DEALER"));
        List<SclCustomerModel> dealerList = territoryManagementService.getCustomerforUser(customerData);
        //List<SclCustomerModel> dealerList = territoryManagementService.getDealersForSubArea();
        List<CustomerData> dealerData=Optional.of(dealerList.stream()
                .map(b2BCustomer -> dealerBasicConverter
                        .convert(b2BCustomer)).collect(Collectors.toList())).get();
        DealerListData dataList = new DealerListData();
        dataList.setDealers(dealerData.stream().filter(cus->Objects.nonNull(cus.getName())).sorted(Comparator.comparing(CustomerData::getName)).collect(Collectors.toList()));
        return dataList;
    }

    @Override
    public DropdownListData findAllState() {
        return brandingService.findAllState();
    }

    @Override
    public DropdownListData findAllDistrict(String state) {
        return brandingService.findAllDistrict(state);
    }

    @Override
    public DropdownListData findAllTaluka(String state, String district) {
        return brandingService.findAllTaluka(state,district);
    }

    @Override
    public DropdownListData findAllErpCity(String state, String district, String taluka) {
        return brandingService.findAllErpCity(state,district,taluka);
    }

    @Override
    public TrackingStatusDetailsData getTrackingStatusDetailsForRequest(String requisitionNumber, String requestStatus) {
       /* BrandingTrackingStatusModel trackingStatusDetailsForRequest = brandingService.getTrackingStatusDetailsForRequest(requisitionNumber, requestStatus);
        List<BrandingTrackStatusStageGateModel> brandingTrackHistoryDetails = brandingService.getBrandingTrackHistoryDetails(requisitionNumber);
        BrandingTrackingData trackingData=new BrandingTrackingData();
        List<BrandingTrackingData> trackingDataList=new ArrayList<>();
        for (BrandingTrackStatusStageGateModel brandingTrackHistoryDetail : brandingTrackHistoryDetails) {
            trackingData.setDate(brandingTrackHistoryDetail.getApprovedDate());
            trackingData.setTime(brandingTrackHistoryDetail.getApprovedTime());
            trackingData.setTitle(brandingTrackHistoryDetail.getRequestName());
            trackingData.setIsPending(brandingTrackHistoryDetail.getIsPending());
            trackingData.setStatus(brandingTrackHistoryDetail.getStatusDetails());
            trackingDataList.add(trackingData);
        }*/
        TrackingStatusDetailsData data = new TrackingStatusDetailsData();
       /* if(trackingStatusDetailsForRequest!=null) {
            BrandingRequestDetailsModel brandingRequestDetailsModel = trackingStatusDetailsForRequest.getRequisitionNumber();
            String brandingSiteType = getEnumerationService().getEnumerationName(brandingRequestDetailsModel.getBrandSiteType());
            String brandingType = getEnumerationService().getEnumerationName(brandingRequestDetailsModel.getBrandingType());
            data.setBrandingSiteType(brandingSiteType);
            data.setBrandingType(brandingType);
            data.setQuantity(brandingRequestDetailsModel.getQuantity());
            String trackStatus = getEnumerationService().getEnumerationName(trackingStatusDetailsForRequest.getTrackStatus());
            data.setTrackStatus(trackStatus);
            data.setTrackStatusDetails(trackingStatusDetailsForRequest.getTrackStatusDetails());
            data.setStatusDetails(trackingStatusDetailsForRequest.getStatusDetails());
            data.setVendorDetails(brandingRequestDetailsModel.getVendorDetails());
            data.setVendorName(brandingRequestDetailsModel.getVendorName());
            data.setEstimatedDeliverDate(trackingStatusDetailsForRequest.getEstimatedDeliveryDate().toString());
            data.setRequestNumber(brandingRequestDetailsModel.getRequisitionNumber());
            data.setTrackingDetails(trackingDataList);
        }*/
        return data;
    }

    @Override
    public TrackingStatusDetailsData getTrackingStatusDetailsForCompleteForm(String requisitionNumber, String requestStatus) {
        TrackingStatusDetailsData data=new TrackingStatusDetailsData();
        /*BrandingTrackingStatusModel trackingStatusDetailsForRequest = brandingService.getTrackingStatusDetailsForRequest(requisitionNumber, requestStatus);
        List<BrandingTrackStatusStageGateModel> brandingTrackHistoryDetails = brandingService.getBrandingTrackHistoryDetails(requisitionNumber);
        BrandingTrackingData trackingData=new BrandingTrackingData();
        List<BrandingTrackingData> trackingDataList=new ArrayList<>();
        for (BrandingTrackStatusStageGateModel brandingTrackHistoryDetail : brandingTrackHistoryDetails) {
            trackingData.setDate(brandingTrackHistoryDetail.getApprovedDate());
            trackingData.setTime(brandingTrackHistoryDetail.getApprovedTime());
            trackingData.setTitle(brandingTrackHistoryDetail.getRequestName());
            trackingData.setIsPending(brandingTrackHistoryDetail.getIsPending());
            trackingData.setStatus(brandingTrackHistoryDetail.getStatusDetails());
            trackingDataList.add(trackingData);
        }

        if(requestStatus.equals(BrandingRequestStatus.PENDING.getCode()))
        {
            data.setStatusDetails("Pending Action By SO");
        }

        if(trackingStatusDetailsForRequest!=null) {
            BrandingRequestDetailsModel brandingRequestDetailsModel = trackingStatusDetailsForRequest.getRequisitionNumber();
            String brandingSiteType = getEnumerationService().getEnumerationName(brandingRequestDetailsModel.getBrandSiteType());
            String brandingType = getEnumerationService().getEnumerationName(brandingRequestDetailsModel.getBrandingType());
            data.setBrandingSiteType(brandingSiteType);
            data.setBrandingType(brandingType);
            data.setQuantity(brandingRequestDetailsModel.getQuantity());
            String trackStatus = getEnumerationService().getEnumerationName(trackingStatusDetailsForRequest.getTrackStatus());
            data.setTrackStatus(trackStatus);
            data.setTrackStatusDetails(trackingStatusDetailsForRequest.getTrackStatusDetails());
            data.setVendorDetails(brandingRequestDetailsModel.getVendorDetails());
            data.setVendorName(brandingRequestDetailsModel.getVendorName());
            data.setEstimatedDeliverDate(trackingStatusDetailsForRequest.getEstimatedDeliveryDate().toString());
            data.setRequestNumber(brandingRequestDetailsModel.getRequisitionNumber());
            data.setTrackingDetails(trackingDataList);
        }*/
        return data;
    }

    @Override
    public boolean updateBrandingRequestStatus(String requisitionNo, String status, String comments) {
        return brandingService.updateBrandingRequestStatus(requisitionNo,status,comments);
    }

    @Override
    public BrandingTrackingListData getBrandingRequisitionTrackerDetails(String requisitionNumber) {
        BrandingTrackingListData listData = new BrandingTrackingListData();
        List<BrandingTrackingData> trackingDataList = new ArrayList<>();
        BrandingRequestDetailsModel brandingRequestDetails = brandingService.getBrandingRequestDetailsByReqNumber(requisitionNumber);
        if(brandingRequestDetails!=null)
        {
            List<BrandingTrackingData> trackingData = sclBrandingReqTrackerConverter.convert(brandingRequestDetails, trackingDataList);
            listData.setTrackingDetails(trackingData);
        }
        return listData;
    }

    @Override
    public BrandingRequestData submitBrandingRequisitionSlct(BrandingRequestDetailsData brandingRequestDetailsData) {
        return brandingService.submitBrandingRequisitionSlct(brandingRequestDetailsData);
    }

    @Override
    public TrackingStatusDetailsData viewRequestDetailsFromTrackingPage(String requisitionNumber, String requestStatus) {/*
        TrackingStatusDetailsData data=new TrackingStatusDetailsData();
        BrandingTrackingStatusModel trackingStatusDetailsForRequest = brandingService.getTrackingStatusDetailsForRequest(requisitionNumber, requestStatus);

        if(requestStatus.equals(BrandingRequestStatus.PENDING.getCode()))
        {
            data.setStatusDetails("Pending Action By SO");
        }
        else if(requestStatus.equals(BrandingRequestStatus.SH_APPROVAL.getCode()) || requestStatus.equals(BrandingRequestStatus.SH_APPROVAL.getCode()) || requestStatus.equals(BrandingRequestStatus.REQUEST_RAISED.getCode())
           || requestStatus.equals(BrandingRequestStatus.PO_COMPLETED.getCode())){
            BrandingRequestDetailsModel brandingRequestDetails = brandingService.getBrandingRequestDetailsByReqNumber(requisitionNumber);
            if(brandingRequestDetails!=null)
            {
               data.setRequestNumber(brandingRequestDetails.getRequisitionNumber());
                if(brandingRequestDetails.getBrandSiteType().getCode().equals(BrandingSiteType.POINT_OF_SALE.getCode())) {
                    String reqStatus = getEnumerationService().getEnumerationName(brandingRequestDetails.getRequestStatus());
                    String brandType=getEnumerationService().getEnumerationName(brandingRequestDetails.getBrandingType());
                    String brandSiteType=getEnumerationService().getEnumerationName(brandingRequestDetails.getBrandSiteType());
                    data.setRequestStatus(reqStatus);
                    data.setBrandingSiteType(brandSiteType);
                    data.setBrandingType(brandType);
                    data.setPrimaryContactNumber(brandingRequestDetails.getPrimaryContactNumber());
                    data.setSecondaryContactNumber(brandingRequestDetails.getSecondaryContactNumber());
                    data.setQuantity(brandingRequestDetails.getQuantity());
                    data.setDimensions(brandingRequestDetails.getDimensions());
                    data.setDetails(brandingRequestDetails.getDetails());
                    data.setUploadPhoto(brandingRequestDetails.getUploadPhoto().getRealFileName());
                    data.setCounterName(brandingRequestDetails.getCounterName());
                    data.setCounterCode(brandingRequestDetails.getCounterCode());
                    *//*if(brandingRequestDetails.getLocation()!=null){
                        data.setLocation((sclAddressConverter.convert(brandingRequestDetails.getLocation())));
                    }*//*
                    data.setLocation(brandingRequestDetails.getLocation());
                    data.setUploadPhoto(brandingRequestDetails.getUploadPhoto().getRealFileName());
                }
                if(brandingRequestDetails.getBrandSiteType().getCode().equals(BrandingSiteType.OUTDOORS.getCode())) {
                    String reqStatus = getEnumerationService().getEnumerationName(brandingRequestDetails.getRequestStatus());
                    String brandType=getEnumerationService().getEnumerationName(brandingRequestDetails.getBrandingType());
                    String brandSiteType=getEnumerationService().getEnumerationName(brandingRequestDetails.getBrandSiteType());
                    data.setRequestStatus(reqStatus);
                    data.setBrandingSiteType(brandSiteType);
                    data.setBrandingType(brandType);
                    data.setSiteName(brandingRequestDetails.getSiteName());
                    data.setSiteAddressLine1(brandingRequestDetails.getSiteAddressLine1());
                    data.setSiteAddressLine2(brandingRequestDetails.getSiteAddressLine2());
                    data.setCity(brandingRequestDetails.getCity());
                    data.setState(brandingRequestDetails.getState());
                    data.setDistrict(brandingRequestDetails.getDistrict());
                    data.setTaluka(brandingRequestDetails.getTaluka());
                    data.setPrimaryContactNumber(brandingRequestDetails.getPrimaryContactNumber());
                    data.setSecondaryContactNumber(brandingRequestDetails.getSecondaryContactNumber());
                    data.setQuantity(brandingRequestDetails.getQuantity());
                    data.setDimensions(brandingRequestDetails.getDimensions());
                    data.setDetails(brandingRequestDetails.getDetails());
                    data.setUploadPhoto(brandingRequestDetails.getUploadPhoto().getRealFileName());
                }
                if(brandingRequestDetails.getBrandSiteType().getCode().equals(BrandingSiteType.DEALER_COSTSHARING_BRANDING.getCode())) {
                    data.setRequestNumber(brandingRequestDetails.getRequisitionNumber());
                    String reqStatus = getEnumerationService().getEnumerationName(brandingRequestDetails.getRequestStatus());
                    String brandType=getEnumerationService().getEnumerationName(brandingRequestDetails.getBrandingType());
                    String brandSiteType=getEnumerationService().getEnumerationName(brandingRequestDetails.getBrandSiteType());
                    data.setRequestStatus(reqStatus);
                    data.setBrandingSiteType(brandSiteType);
                    data.setBrandingType(brandType);
                    data.setPrimaryContactNumber(brandingRequestDetails.getPrimaryContactNumber());
                    data.setSecondaryContactNumber(brandingRequestDetails.getSecondaryContactNumber());
                    data.setQuantity(brandingRequestDetails.getQuantity());
                    data.setDetails(brandingRequestDetails.getDetails());
                    data.setCounterName(brandingRequestDetails.getCounterName());
                    data.setCounterCode(brandingRequestDetails.getCounterCode());
                }
            }
        }
        else if(requestStatus.equals(BrandingRequestStatus.ACTIVITY_VERIFIED.getCode()) || requestStatus.equals(BrandingRequestStatus.INVOICE_UPLOAD.getCode())
        ||requestStatus.equals(BrandingRequestStatus.VENDOR_PAYMENT.getCode()) ){
            BrandingActivityVerficationModel activityDetailsForRequest = brandingService.getActivityDetailsForRequest(requisitionNumber);
           if(activityDetailsForRequest!=null){
                if(activityDetailsForRequest.getBrandingSiteType().getCode().equals(BrandingSiteType.POINT_OF_SALE.getCode())){
                    String brandType=getEnumerationService().getEnumerationName(activityDetailsForRequest.getBrandingType());
                    String brandSiteType=getEnumerationService().getEnumerationName(activityDetailsForRequest.getBrandingSiteType());
                    data.setBrandingSiteType(brandSiteType);
                    data.setBrandingType(brandType);
                    data.setCounterCode(activityDetailsForRequest.getCounterCode());
                    data.setCounterName(activityDetailsForRequest.getCounterName());
                   *//* if(activityDetailsForRequest.getLocation()!=null) {
                        data.setLocation(sclAddressConverter.convert(activityDetailsForRequest.getLocation()));
                    }*//*
                    data.setLocation(activityDetailsForRequest.getLocation());
                    data.setPrimaryContactNumber(activityDetailsForRequest.getPrimaryContactNumber());
                    data.setSecondaryContactNumber(activityDetailsForRequest.getSecondaryContactNumber());
                    data.setQuantity(activityDetailsForRequest.getQuantity());
                    data.setDimensions(activityDetailsForRequest.getDimensions());
                    data.setDetails(activityDetailsForRequest.getDetails());
                    data.setUploadPhoto(activityDetailsForRequest.getPhotoAfterBranding().getRealFileName());
                }
                if(activityDetailsForRequest.getBrandingSiteType().getCode().equals(BrandingSiteType.OUTDOORS.getCode())) {
                    String brandType=getEnumerationService().getEnumerationName(activityDetailsForRequest.getBrandingType());
                    String brandSiteType=getEnumerationService().getEnumerationName(activityDetailsForRequest.getBrandingSiteType());
                    data.setBrandingSiteType(brandSiteType);
                    data.setBrandingType(brandType);
                    data.setPrimaryContactNumber(activityDetailsForRequest.getPrimaryContactNumber());
                    data.setSecondaryContactNumber(activityDetailsForRequest.getSecondaryContactNumber());
                    data.setQuantity(activityDetailsForRequest.getQuantity());
                    data.setDimensions(activityDetailsForRequest.getDimensions());
                    data.setDetails(activityDetailsForRequest.getDetails());
                    data.setLatitude(activityDetailsForRequest.getLatitude());
                    data.setLongitude(activityDetailsForRequest.getLongitude());
                    data.setUploadPhoto(activityDetailsForRequest.getPhotoAfterBranding().getRealFileName());
                }
                if(activityDetailsForRequest.getBrandingSiteType().getCode().equals(BrandingSiteType.DEALER_COSTSHARING_BRANDING.getCode())) {
                    String brandType=getEnumerationService().getEnumerationName(activityDetailsForRequest.getBrandingType());
                    String brandSiteType=getEnumerationService().getEnumerationName(activityDetailsForRequest.getBrandingSiteType());
                    data.setBrandingSiteType(brandSiteType);
                    data.setBrandingType(brandType);
                    data.setCounterCode(activityDetailsForRequest.getCounterCode());
                    data.setCounterName(activityDetailsForRequest.getCounterName());
                    data.setPrimaryContactNumber(activityDetailsForRequest.getPrimaryContactNumber());
                    data.setSecondaryContactNumber(activityDetailsForRequest.getSecondaryContactNumber());
                    data.setQuantity(activityDetailsForRequest.getQuantity());
                    data.setDetails(activityDetailsForRequest.getDetails());
                }
                data.setDateOfCompletion(activityDetailsForRequest.getDateOfCompletion().toString());
                data.setResponsiveness(activityDetailsForRequest.getResponsiveness());
                data.setCompletionTime(activityDetailsForRequest.getCompletionTime());
                data.setQualityOfWork(activityDetailsForRequest.getQualityOfWork());
                data.setExperience(activityDetailsForRequest.getExperience());
                data.setPhotoAfterBranding(activityDetailsForRequest.getPhotoAfterBranding().getRealFileName());
                data.setUploadInvoice(activityDetailsForRequest.getUploadInvoice().getRealFileName());
                data.setComments(activityDetailsForRequest.getBrandComments());
                data.setFeedBack(activityDetailsForRequest.getFeedback());
            }
        }
        return data;*/
        return null;
    }

    public TerritoryManagementService getTerritoryManagementService() {
        return territoryManagementService;
    }

    public void setTerritoryManagementService(TerritoryManagementService territoryManagementService) {
        this.territoryManagementService = territoryManagementService;
    }

    public Converter<BrandingRequestDetailsModel, List<BrandingTrackingData>> getSclBrandingReqTrackerConverter() {
        return sclBrandingReqTrackerConverter;
    }

    public void setSclBrandingReqTrackerConverter(Converter<BrandingRequestDetailsModel, List<BrandingTrackingData>> sclBrandingReqTrackerConverter) {
        this.sclBrandingReqTrackerConverter = sclBrandingReqTrackerConverter;
    }
}

