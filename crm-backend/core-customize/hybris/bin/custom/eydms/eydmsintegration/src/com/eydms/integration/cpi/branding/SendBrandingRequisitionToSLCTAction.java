package com.eydms.integration.cpi.branding;

import com.eydms.core.enums.BrandingSiteType;
import com.eydms.core.model.*;
import com.eydms.integration.cpi.order.EyDmsSapCpiOutboundService;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.task.RetryLaterException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static de.hybris.platform.sap.sapcpiadapter.service.SapCpiOutboundService.*;

public class SendBrandingRequisitionToSLCTAction extends AbstractSimpleDecisionAction<EyDmsBrandingProcessModel> {

    private static final Logger LOG = Logger.getLogger(SendBrandingRequisitionToSLCTAction.class);
    private static final String MEDIA_BASE_URL = "media.host.base.url";

    private EyDmsSapCpiOutboundService eydmsSapCpiDefaultOutboundService;
    private EnumerationService enumerationService;

    @Autowired
    ConfigurationService configurationService;

    @Override
    public Transition executeAction(EyDmsBrandingProcessModel eydmsBrandingProcessModel) throws RetryLaterException, Exception {
        BrandingRequestDetailsModel brandingRequisitionDetails = eydmsBrandingProcessModel.getBrandingRequestDetails();

        if(brandingRequisitionDetails!=null && brandingRequisitionDetails.getSlctReqNo()!=null && brandingRequisitionDetails.getReqRaisedRole()!=null)
        {
            return Transition.OK;
        }
        else {
            EyDmsOutboundBrandingRequisitionModel outboundBrandingRequisitionModel = getOutboundBrandingRequisitionDetails(brandingRequisitionDetails);
            getEyDmsSapCpiDefaultOutboundService().sendBrandingRequisitionDetailsToSlct(outboundBrandingRequisitionModel).subscribe(

                    // onNext
                    responseEntityMap -> {
                        if (isSentSuccessfully(responseEntityMap)) {
                            LOG.info(String.format("The Branding [%s] Requisition has been successfully sent to the SLCT through SCPI! %n%s",
                                    brandingRequisitionDetails.getRequisitionNumber(), getPropertyValue(responseEntityMap, RESPONSE_MESSAGE)));

                        } else {
                            LOG.info(String.format("The Branding [%s] Requisition has not been successfully sent to the SLCT through SCPI! %n%s",
                                    brandingRequisitionDetails.getRequisitionNumber(), getPropertyValue(responseEntityMap, RESPONSE_MESSAGE)));
                        }
                    }
                    // onError
                    , error -> {

                        LOG.error(String.format("The Branding [%s] Requisition has not been successfully sent to the SLCT through SCPI! %n%s", brandingRequisitionDetails.getRequisitionNumber(), error.getMessage()), error);
                    }
            );
            return Transition.OK;
        }
    }

    private EyDmsOutboundBrandingRequisitionModel getOutboundBrandingRequisitionDetails(BrandingRequestDetailsModel brandingRequisitionDetails) {
        EyDmsOutboundBrandingRequisitionModel outboundBrandingRequisitionModel = new EyDmsOutboundBrandingRequisitionModel();
        if (brandingRequisitionDetails != null) {
            outboundBrandingRequisitionModel.setRequisitionNumber(brandingRequisitionDetails.getRequisitionNumber());
          /*  outboundBrandingRequisitionModel.setActivityCategory(brandingRequisitionDetails.getBrandSiteType().getCode());
            outboundBrandingRequisitionModel.setActivitySubCategory(brandingRequisitionDetails.getBrandingType().getCode());*/
            String activityCategory = getEnumerationService().getEnumerationName(brandingRequisitionDetails.getBrandSiteType());
            String activitySubCategory = getEnumerationService().getEnumerationName(brandingRequisitionDetails.getBrandingType());
            outboundBrandingRequisitionModel.setActivityCategory(activityCategory);
            outboundBrandingRequisitionModel.setActivitySubCategory(activitySubCategory);
            outboundBrandingRequisitionModel.setActivityName(brandingRequisitionDetails.getDetails());

            B2BCustomerModel requestRaisedBy = brandingRequisitionDetails.getRequestRaisedBy();
            if (requestRaisedBy != null) {
                if (requestRaisedBy instanceof EyDmsUserModel) {
                    outboundBrandingRequisitionModel.setActivityFor(requestRaisedBy.getUserType() != null ? requestRaisedBy.getUserType().getCode() : "");
                } else if (requestRaisedBy instanceof EyDmsCustomerModel) {
                    outboundBrandingRequisitionModel.setActivityFor(((EyDmsCustomerModel) requestRaisedBy).getCounterType() != null ? ((EyDmsCustomerModel) requestRaisedBy).getCounterType().getCode() : "");
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
                outboundBrandingRequisitionModel.setIsModify(true);
            if(!(brandingRequisitionDetails.getBrandSiteType().equals(BrandingSiteType.DEALER_COSTSHARING_BRANDING))) {
                outboundBrandingRequisitionModel.setBaseUrl(configurationService.getConfiguration().getString(MEDIA_BASE_URL));
            }
        }
        return outboundBrandingRequisitionModel;
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

    public EyDmsSapCpiOutboundService getEyDmsSapCpiDefaultOutboundService() {
        return eydmsSapCpiDefaultOutboundService;
    }

    public void setEyDmsSapCpiDefaultOutboundService(EyDmsSapCpiOutboundService eydmsSapCpiDefaultOutboundService) {
        this.eydmsSapCpiDefaultOutboundService = eydmsSapCpiDefaultOutboundService;
    }
}
