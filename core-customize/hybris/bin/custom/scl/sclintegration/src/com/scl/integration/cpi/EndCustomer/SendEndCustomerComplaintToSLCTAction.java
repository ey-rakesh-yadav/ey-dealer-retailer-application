package com.scl.integration.cpi.EndCustomer;

import com.scl.core.enums.BrandingSiteType;
import com.scl.core.model.*;
import com.scl.integration.cpi.branding.SendBrandingRequisitionToSLCTAction;
import com.scl.integration.cpi.order.SclSapCpiOutboundService;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.task.RetryLaterException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static de.hybris.platform.sap.sapcpiadapter.service.SapCpiOutboundService.*;

public class SendEndCustomerComplaintToSLCTAction extends AbstractSimpleDecisionAction<SendEndCustomerComplaintProcessModel>{


    private static final Logger LOG = Logger.getLogger(SendEndCustomerComplaintToSLCTAction.class);
    private static final String MEDIA_BASE_URL = "media.host.base.url";

    private SclSapCpiOutboundService sclSapCpiDefaultOutboundService;
    private EnumerationService enumerationService;

    @Autowired
    ConfigurationService configurationService;

    @Override
    public AbstractSimpleDecisionAction.Transition executeAction(SendEndCustomerComplaintProcessModel sendEndCustomerComplaintProcessModel) throws RetryLaterException, Exception {
   /*     EndCustomerComplaintModel endCustomerComplaint = sendEndCustomerComplaintProcessModel.getEndCustomerComplaint();

        OutboundEndCustomerComplaintModel outboundEndCustomerComplaintDetails = getOutboundEndCustomerComplaintDetails(endCustomerComplaint);
        getSclSapCpiDefaultOutboundService().sendEndCustomerComplaintDetailsToSlct(outboundEndCustomerComplaintDetails).subscribe(

                // onNext
                responseEntityMap -> {
                    if (isSentSuccessfully(responseEntityMap)) {
                        LOG.info(String.format("The End Customer complaint [%s] Request has been successfully sent to the SLCT through SCPI! %n%s",
                                endCustomerComplaint.getRequestId(), getPropertyValue(responseEntityMap, RESPONSE_MESSAGE)));

                    } else {
                        LOG.info(String.format("The End Customer complaint [%s] Request has not been successfully sent to the SLCT through SCPI! %n%s",
                                endCustomerComplaint.getRequestId(), getPropertyValue(responseEntityMap, RESPONSE_MESSAGE)));
                    }
                }
                // onError
                , error -> {

                    LOG.error(String.format("The End Customer Complaint [%s] Requisition has not been successfully sent to the SLCT through SCPI! %n%s", endCustomerComplaint.getRequestId(), error.getMessage()), error);
                }
        );
    */
        return Transition.OK;

    }

    private OutboundEndCustomerComplaintModel getOutboundEndCustomerComplaintDetails(EndCustomerComplaintModel endCustomerComplaintModel) {

        OutboundEndCustomerComplaintModel outboundEndCustomerComplaintModel = new OutboundEndCustomerComplaintModel();
        /*
        if (endCustomerComplaintModel != null) {
            outboundEndCustomerComplaintModel.setRequestId(endCustomerComplaintModel.getRequestId());
            String activityCategory = getEnumerationService().getEnumerationName(endCustomerComplaintModel.getBrandSiteType());
            String activitySubCategory = getEnumerationService().getEnumerationName(endCustomerComplaintModel.getBrandingType());
            outboundBrandingRequisitionModel.setActivityCategory(activityCategory);
            outboundBrandingRequisitionModel.setActivitySubCategory(activitySubCategory);
            outboundBrandingRequisitionModel.setActivityName(endCustomerComplaintModel.getDetails());

            B2BCustomerModel requestRaisedBy = endCustomerComplaintModel.getRequestRaisedBy();
            if (requestRaisedBy != null) {
                if (requestRaisedBy instanceof SclUserModel) {
                    outboundBrandingRequisitionModel.setActivityFor(requestRaisedBy.getUserType() != null ? requestRaisedBy.getUserType().getCode() : "");
                } else if (requestRaisedBy instanceof SclCustomerModel) {
                    outboundBrandingRequisitionModel.setActivityFor(((SclCustomerModel) requestRaisedBy).getCounterType() != null ? ((SclCustomerModel) requestRaisedBy).getCounterType().getCode() : "");
                }
            }
            List<MediaModel> beforeBrandingPhotoList=new ArrayList<>();
            if(brandingRequisitionDetails.getBeforeBrandingPhotos()!=null && !brandingRequisitionDetails.getBeforeBrandingPhotos().isEmpty()) {
                for (MediaModel beforeBrandingPhoto : brandingRequisitionDetails.getBeforeBrandingPhotos()) {
                    beforeBrandingPhotoList.add(beforeBrandingPhoto);
                }
            }
            outboundBrandingRequisitionModel.setUploadPhotos(beforeBrandingPhotoList);
            outboundBrandingRequisitionModel.setCounterCode(brandingRequisitionDetails.getCounterErpCustNo());
            outboundBrandingRequisitionModel.setCounterName(brandingRequisitionDetails.getCounterName());
            outboundBrandingRequisitionModel.setPrimaryContactNumber(brandingRequisitionDetails.getPrimaryContactNumber());
            outboundBrandingRequisitionModel.setSecondaryContactNumber(brandingRequisitionDetails.getSecondaryContactNumber());
            if(brandingRequisitionDetails.getStartDate()!=null)
                outboundBrandingRequisitionModel.setStartDates(getParsedDate(brandingRequisitionDetails.getStartDate()));
            if(brandingRequisitionDetails.getPlanningDateOfCompletion()!=null)
                outboundBrandingRequisitionModel.setPlanningDateOfCompletions(getParsedDate(brandingRequisitionDetails.getPlanningDateOfCompletion()));
            outboundBrandingRequisitionModel.setObjectiveOfActivity(brandingRequisitionDetails.getObjectiveOfActivity());
            outboundBrandingRequisitionModel.setObjectiveOfTargetPercentage(brandingRequisitionDetails.getObjectiveOfTargetPercentage());
            outboundBrandingRequisitionModel.setBudgetPlanned(brandingRequisitionDetails.getBudgetPlanned());
            outboundBrandingRequisitionModel.setHeight(brandingRequisitionDetails.getHeight());
            outboundBrandingRequisitionModel.setLength(brandingRequisitionDetails.getLength());
            outboundBrandingRequisitionModel.setSiteName(brandingRequisitionDetails.getSiteName());
            outboundBrandingRequisitionModel.setSiteAddressLine1(brandingRequisitionDetails.getSiteAddressLine1());
            outboundBrandingRequisitionModel.setSiteAddressLine2(brandingRequisitionDetails.getSiteAddressLine2());
            outboundBrandingRequisitionModel.setCity(brandingRequisitionDetails.getCity());
            outboundBrandingRequisitionModel.setState(brandingRequisitionDetails.getState());
            outboundBrandingRequisitionModel.setDistrict(brandingRequisitionDetails.getDistrict());
            outboundBrandingRequisitionModel.setTaluka(brandingRequisitionDetails.getTaluka());
            outboundBrandingRequisitionModel.setLatitude(brandingRequisitionDetails.getSiteLatitude());
            outboundBrandingRequisitionModel.setLongitude(brandingRequisitionDetails.getSiteLongitude());
            outboundBrandingRequisitionModel.setQuantity(brandingRequisitionDetails.getQuantity());
            outboundBrandingRequisitionModel.setBrand(brandingRequisitionDetails.getBrand().getUid());
            if(brandingRequisitionDetails.getSynced() == null || brandingRequisitionDetails.getSynced().equals(false))
                outboundBrandingRequisitionModel.setIsModify(false);
            else
                outboundEndCustomerComplaintModel.setIsModify(true);
            if(!(brandingRequisitionDetails.getBrandSiteType().equals(BrandingSiteType.DEALER_COSTSHARING_BRANDING))) {
                outboundEndCustomerComplaintModel.setBaseUrl(configurationService.getConfiguration().getString(MEDIA_BASE_URL));
            }
        }
        */
        return outboundEndCustomerComplaintModel;
    }

    private String getParsedDate(Date date) {
        Instant instant = date.toInstant();
        LocalDate localDate = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = localDate.format(formatter);
        return formattedDate;
    }

    public EnumerationService getEnumerationService() {
        return enumerationService;
    }

    public void setEnumerationService(EnumerationService enumerationService) {
        this.enumerationService = enumerationService;
    }

    public SclSapCpiOutboundService getSclSapCpiDefaultOutboundService() {
        return sclSapCpiDefaultOutboundService;
    }

    public void setSclSapCpiDefaultOutboundService(SclSapCpiOutboundService sclSapCpiDefaultOutboundService) {
        this.sclSapCpiDefaultOutboundService = sclSapCpiDefaultOutboundService;
    }

}
