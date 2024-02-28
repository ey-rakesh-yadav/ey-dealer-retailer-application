package com.eydms.integration.cpi.branding;

import com.eydms.core.model.*;
import com.eydms.integration.cpi.order.EyDmsSapCpiOutboundService;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.processengine.BusinessProcessService;
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

public class SendBrandingRequisitionFeedbackToSLCTAction extends AbstractSimpleDecisionAction<EyDmsBrandingProcessModel> {

    private static final Logger LOG = Logger.getLogger(SendBrandingRequisitionFeedbackToSLCTAction.class);
    private static final String MEDIA_BASE_URL = "media.host.base.url";

    private EyDmsSapCpiOutboundService eydmsSapCpiDefaultOutboundService;

    @Autowired
    ConfigurationService configurationService;

    @Override
    public Transition executeAction(EyDmsBrandingProcessModel eydmsBrandingProcessModel) throws RetryLaterException, Exception {
        List<MediaModel> afterBrandingPhotoList=new ArrayList<>();
        List<MediaModel> uploadInvoiceList = new ArrayList<>();

        BrandingRequestDetailsModel brandingRequisitionDetails = eydmsBrandingProcessModel.getBrandingRequestDetails();
        EyDmsOutboundBrandingRequisitionFeedbackModel outboundBrandingRequisitionModel = new EyDmsOutboundBrandingRequisitionFeedbackModel();
        if(brandingRequisitionDetails!=null)
        {
            outboundBrandingRequisitionModel.setRequisitionNumber(brandingRequisitionDetails.getRequisitionNumber());
            outboundBrandingRequisitionModel.setFeedback(brandingRequisitionDetails.getFeedback());
            outboundBrandingRequisitionModel.setLatitude(brandingRequisitionDetails.getLatitude());
            outboundBrandingRequisitionModel.setLongitude(brandingRequisitionDetails.getLongitude());
            outboundBrandingRequisitionModel.setBrandComments(brandingRequisitionDetails.getBrandComments());
            if(brandingRequisitionDetails.getDateOfCompletion()!=null) {
                outboundBrandingRequisitionModel.setDateOfCompletions(getParsedDate(brandingRequisitionDetails.getDateOfCompletion()));
            }
            outboundBrandingRequisitionModel.setExperience(brandingRequisitionDetails.getExperience());
            outboundBrandingRequisitionModel.setActivityVerificationDates(getParsedDate(brandingRequisitionDetails.getActivityVerificationDate()));
            outboundBrandingRequisitionModel.setInvoiceUploadedDates(getParsedDate(brandingRequisitionDetails.getInvoiceUploadedDate()));
            outboundBrandingRequisitionModel.setCompletionTime(brandingRequisitionDetails.getCompletionTime());
            outboundBrandingRequisitionModel.setQualityOfWork(brandingRequisitionDetails.getQualityOfWork());
            outboundBrandingRequisitionModel.setResponsiveness(brandingRequisitionDetails.getResponsiveness());
            if(brandingRequisitionDetails.getAfterBrandingPhotos()!=null && !brandingRequisitionDetails.getAfterBrandingPhotos().isEmpty()) {
                for (MediaModel afterBrandingPhoto : brandingRequisitionDetails.getAfterBrandingPhotos()) {
                    afterBrandingPhotoList.add(afterBrandingPhoto);
                }
            }
            if(brandingRequisitionDetails.getUploadInvoice()!=null && !brandingRequisitionDetails.getUploadInvoice().isEmpty()) {

                for (MediaModel uploadInvoice : brandingRequisitionDetails.getUploadInvoice()) {
                    uploadInvoiceList.add(uploadInvoice);
                }
            }
            outboundBrandingRequisitionModel.setAfterBrandingPhotos(afterBrandingPhotoList);
                if(brandingRequisitionDetails.getInvoiceAmount()!=null) {
                    outboundBrandingRequisitionModel.setInvoiceAmount(brandingRequisitionDetails.getInvoiceAmount());
                }
                if(uploadInvoiceList!=null && !uploadInvoiceList.isEmpty()) {
                    outboundBrandingRequisitionModel.setUploadInvoice(uploadInvoiceList);
                }

            outboundBrandingRequisitionModel.setBaseUrl(configurationService.getConfiguration().getString(MEDIA_BASE_URL));
            getEyDmsSapCpiDefaultOutboundService().sendBrandingRequisitionDetailsFeedbackToSlct(outboundBrandingRequisitionModel).subscribe(

                    // onNext
                    responseEntityMap -> {

                        if (isSentSuccessfully(responseEntityMap)) {

                            LOG.info(String.format("The Branding [%s] Requisition Feedback has been successfully sent to the SLCT through SCPI! %n%s",
                                    brandingRequisitionDetails.getRequisitionNumber(), getPropertyValue(responseEntityMap, RESPONSE_MESSAGE)));

                        } else {
                            LOG.info(String.format("The Branding [%s] Requisition Feedback has not been successfully sent to the SLCT through SCPI! %n%s",
                                    brandingRequisitionDetails.getRequisitionNumber(), getPropertyValue(responseEntityMap, RESPONSE_MESSAGE)));
                        }

                    }

                    // onError
                    , error -> {

                        LOG.error(String.format("The Branding [%s] Requisition Feedback has not been successfully sent to the SLCT through SCPI! %n%s", brandingRequisitionDetails.getRequisitionNumber(), error.getMessage()), error);
                    }

            );

        }
        return Transition.OK;
    }

    public EyDmsSapCpiOutboundService getEyDmsSapCpiDefaultOutboundService() {
        return eydmsSapCpiDefaultOutboundService;
    }

    public void setEyDmsSapCpiDefaultOutboundService(EyDmsSapCpiOutboundService eydmsSapCpiDefaultOutboundService) {
        this.eydmsSapCpiDefaultOutboundService = eydmsSapCpiDefaultOutboundService;
    }

    private String getParsedDate(Date date) {
        Instant instant = date.toInstant();
        LocalDate localDate = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = localDate.format(formatter);
        return formattedDate;
    }
}
