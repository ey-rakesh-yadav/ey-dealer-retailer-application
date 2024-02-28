package com.eydms.facades.populators;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.enums.BrandingRequestStatus;
import com.eydms.core.model.BrandingRequestDetailsModel;
import com.eydms.facades.order.data.BrandingTrackingData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.user.UserService;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class EyDmsBrandingRequisitionTrackerPopulator implements Populator<BrandingRequestDetailsModel, List<BrandingTrackingData>> {

    private EnumerationService enumerationService;
    private UserService userService;

    @Override
    public void populate(BrandingRequestDetailsModel source, List<BrandingTrackingData> target) throws ConversionException {
        Assert.notNull(source, "Parameter source cannot be null.");
        Assert.notNull(target, "Parameter target cannot be null.");

        List<String> commentsForRequisition = new ArrayList<String>();
        List<String> commentsForRequest = new ArrayList<String>();

        List<String> comments = new ArrayList<String>();
        //dealer/retailer - getrequestby
        //so - requisitionraised

        if (source.getRequestRaisedBy().getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.SALES_OFFICER_GROUP_ID))) {
//            BrandingTrackingData requestRaisedData = new BrandingTrackingData();
//            requestRaisedData.setCode(BrandingRequestStatus.REQUEST_RAISED.getCode());
//            requestRaisedData.setName(getEnumerationService().getEnumerationName(BrandingRequestStatus.REQUEST_RAISED));
//            if (source.getRequestRaisedDate() != null)
//                requestRaisedData.setActualTime(source.getRequestRaisedDate());
//            requestRaisedData.setIndex(1);
//            /*if (source.getRequestRaisedBy() != null && source.getCbtComment()==null && source.getLbtComment()==null && source.getNshComment()==null)
//                comments.add(String.format("Request raised by %s", source.getRequestRaisedBy().getUid()));
//            requestRaisedData.setComment(comments);*/
//            target.add(requestRaisedData);

            BrandingTrackingData requisitionRaisedData = new BrandingTrackingData();
            requisitionRaisedData.setCode(BrandingRequestStatus.REQUISITION_RAISED.getCode());
            requisitionRaisedData.setName(getEnumerationService().getEnumerationName(BrandingRequestStatus.REQUISITION_RAISED));
            if (source.getRequisitionRaisedDate() != null)
                requisitionRaisedData.setActualTime(source.getRequisitionRaisedDate());
            requisitionRaisedData.setIndex(2);
            if (source.getRequestRaisedBy() != null) {
                    commentsForRequisition.add(String.format("Requisition raised by %s", source.getRequestRaisedBy().getUid()));
                    if(source.getSlctReqNo()!=null) {
                        commentsForRequisition.add(String.format("Slct No %s", source.getSlctReqNo()));
                    }
                    requisitionRaisedData.setComment(commentsForRequisition);
            }
            target.add(requisitionRaisedData);
        }
        if(source.getRequestRaisedBy().getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)) ||
                source.getRequestRaisedBy().getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)) )
        {
            BrandingTrackingData requestRaisedData = new BrandingTrackingData();
            requestRaisedData.setCode(BrandingRequestStatus.REQUEST_RAISED.getCode());
            requestRaisedData.setName(getEnumerationService().getEnumerationName(BrandingRequestStatus.REQUEST_RAISED));
            if (source.getRequestRaisedDate() != null)
                requestRaisedData.setActualTime(source.getRequestRaisedDate());
            requestRaisedData.setIndex(1);
            if (source.getRequestRaisedBy() != null)
            	commentsForRequest.add(String.format("Request raised by %s", source.getRequestRaisedBy().getUid()));
            requestRaisedData.setComment(commentsForRequest);
            target.add(requestRaisedData);

            BrandingTrackingData requisitionRaisedData = new BrandingTrackingData();
            requisitionRaisedData.setCode(BrandingRequestStatus.REQUISITION_RAISED.getCode());
            requisitionRaisedData.setName(getEnumerationService().getEnumerationName(BrandingRequestStatus.REQUISITION_RAISED));
            if (source.getRequisitionRaisedDate() != null)
                requisitionRaisedData.setActualTime(source.getRequisitionRaisedDate());
            requisitionRaisedData.setIndex(2);
            if (source.getRequestApprovedBy() != null) {
                    commentsForRequisition.add(String.format("Requisition raised by %s", source.getRequestApprovedBy().getUid()));
                    if(source.getSlctReqNo()!=null) {
                        commentsForRequisition.add(String.format("Slct No %s", source.getSlctReqNo()));
                     }
                    requisitionRaisedData.setComment(commentsForRequisition);
            }
            target.add(requisitionRaisedData);
        }

        BrandingTrackingData lbtApprovedData = new BrandingTrackingData();
        lbtApprovedData.setCode(BrandingRequestStatus.LBT_APPROVED.getCode());
        lbtApprovedData.setName(getEnumerationService().getEnumerationName(BrandingRequestStatus.LBT_APPROVED));
        if (source.getLbtApprovedDate() != null)
            lbtApprovedData.setActualTime(source.getLbtApprovedDate());
        lbtApprovedData.setIndex(3);
        if (source.getLbtApprovedDate()!=null && source.getLbtComment() != null) {
            comments.add(String.format(source.getLbtComment()));
            lbtApprovedData.setComment(comments);
        }
        target.add(lbtApprovedData);

        BrandingTrackingData cbtApprovedData = new BrandingTrackingData();
        cbtApprovedData.setCode(BrandingRequestStatus.CBT_APPROVED.getCode());
        cbtApprovedData.setName(getEnumerationService().getEnumerationName(BrandingRequestStatus.CBT_APPROVED));
        if (source.getCbtApprovedDate() != null)
            cbtApprovedData.setActualTime(source.getCbtApprovedDate());
        cbtApprovedData.setIndex(4);
        if (source.getCbtApprovedDate() != null && source.getCbtComment() != null) {
            comments.add(String.format(source.getCbtComment()));
            cbtApprovedData.setComment(comments);
        }
        target.add(cbtApprovedData);

        BrandingTrackingData nshApprovedData = new BrandingTrackingData();
        nshApprovedData.setCode(BrandingRequestStatus.NSH_APPROVED.getCode());
        nshApprovedData.setName(getEnumerationService().getEnumerationName(BrandingRequestStatus.NSH_APPROVED));
        if (source.getNshApprovedDate() != null)
            nshApprovedData.setActualTime(source.getNshApprovedDate());
        nshApprovedData.setIndex(5);
        if (source.getNshApprovedDate() != null && source.getNshComment() != null) {
            comments.add(String.format(source.getNshComment()));
            nshApprovedData.setComment(comments);
        }
        target.add(nshApprovedData);

        //if dealer/retailer raised a request
        if (source.getRequestRaisedBy().getGroups().contains(userService.getUserGroupForUID((EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) || source.getRequestRaisedBy().getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
        BrandingTrackingData activitiyVerifiedData = new BrandingTrackingData();
            activitiyVerifiedData.setCode(BrandingRequestStatus.ACTIVITY_VERIFIED.getCode());
            activitiyVerifiedData.setName(getEnumerationService().getEnumerationName(BrandingRequestStatus.ACTIVITY_VERIFIED));
        if (source.getActivityVerificationDate() != null)
            activitiyVerifiedData.setActualTime(source.getActivityVerificationDate());
            activitiyVerifiedData.setIndex(6);

        if (source.getRequestRaisedBy().getGroups().contains(EyDmsCoreConstants.CUSTOMER.SALES_OFFICER_GROUP_ID)) {
            comments.add(String.format("Verification initiated by %s", source.getRequestRaisedBy().getUid()));
            activitiyVerifiedData.setComment(comments);
        }
        target.add(activitiyVerifiedData);

     /*   BrandingTrackingData invoiceUploadData = new BrandingTrackingData();
            invoiceUploadData.setCode(BrandingRequestStatus.INVOICE_UPLOAD.getCode());
            invoiceUploadData.setName(getEnumerationService().getEnumerationName(BrandingRequestStatus.INVOICE_UPLOAD));
        if (source.getInvoiceUploadedDate() != null)
            invoiceUploadData.setActualTime(source.getInvoiceUploadedDate());
            invoiceUploadData.setIndex(7);

        if (source.getRequestRaisedBy().getGroups().contains(EyDmsCoreConstants.CUSTOMER.SALES_OFFICER_GROUP_ID)) {
            comments.add(String.format("Invoice Uploaded by %s", source.getRequestRaisedBy().getUid()));
            invoiceUploadData.setComment(comments);
        }
        target.add(invoiceUploadData);*/

        /*BrandingTrackingData lbtRejectedData = new BrandingTrackingData();
            lbtRejectedData.setCode(BrandingRequestStatus.LBT_REJECTED.getCode());
            lbtRejectedData.setName(getEnumerationService().getEnumerationName(BrandingRequestStatus.LBT_REJECTED));
        if (source.getLbtRejectedDate() != null)
            lbtRejectedData.setActualTime(source.getLbtRejectedDate());
            lbtRejectedData.setIndex(7);
        if (source.getLbtRejectedDate() != null && source.getLbtComment() != null) {
            comments.add(String.format(source.getLbtComment()));
            lbtRejectedData.setComment(comments);
        }
        target.add(lbtRejectedData);

        BrandingTrackingData cbtRejectedData = new BrandingTrackingData();
            cbtRejectedData.setCode(BrandingRequestStatus.CBT_REJECTED.getCode());
            cbtRejectedData.setName(getEnumerationService().getEnumerationName(BrandingRequestStatus.CBT_REJECTED));
        if (source.getCbtRejectedDate() != null)
            cbtRejectedData.setActualTime(source.getCbtRejectedDate());
            cbtRejectedData.setIndex(8);
        if (source.getCbtRejectedDate() != null && source.getCbtComment() != null) {
            comments.add(String.format(source.getCbtComment()));
            cbtRejectedData.setComment(comments);
        }
        target.add(cbtRejectedData);

            BrandingTrackingData nshRejectedData = new BrandingTrackingData();
            nshRejectedData.setCode(BrandingRequestStatus.NSH_REJECTED.getCode());
            nshRejectedData.setName(getEnumerationService().getEnumerationName(BrandingRequestStatus.NSH_REJECTED));
            if (source.getNshRejectedDate() != null)
                nshRejectedData.setActualTime(source.getNshRejectedDate());
            nshRejectedData.setIndex(9);
            if (source.getNshRejectedDate() != null && source.getNshComment() != null) {
                comments.add(String.format(source.getNshComment()));
                nshRejectedData.setComment(comments);
            }
            target.add(nshRejectedData);*/
    }

        //if DO raised a request
        if (source.getRequestRaisedBy().getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.SALES_OFFICER_GROUP_ID))) {
            BrandingTrackingData activitiyVerifiedData = new BrandingTrackingData();
            activitiyVerifiedData.setCode(BrandingRequestStatus.ACTIVITY_VERIFIED.getCode());
            activitiyVerifiedData.setName(getEnumerationService().getEnumerationName(BrandingRequestStatus.ACTIVITY_VERIFIED));
            if (source.getActivityVerificationDate() != null)
                activitiyVerifiedData.setActualTime(source.getActivityVerificationDate());
            activitiyVerifiedData.setIndex(6);

            if (source.getRequestRaisedBy().getGroups().contains(EyDmsCoreConstants.CUSTOMER.SALES_OFFICER_GROUP_ID)) {
                comments.add(String.format("Verification initiated by %s", source.getRequestRaisedBy().getUid()));
                activitiyVerifiedData.setComment(comments);
            }
            target.add(activitiyVerifiedData);

            /*BrandingTrackingData lbtRejectedData = new BrandingTrackingData();
            lbtRejectedData.setCode(BrandingRequestStatus.LBT_REJECTED.getCode());
            lbtRejectedData.setName(getEnumerationService().getEnumerationName(BrandingRequestStatus.LBT_REJECTED));
            if (source.getLbtRejectedDate() != null)
                lbtRejectedData.setActualTime(source.getLbtRejectedDate());
            lbtRejectedData.setIndex(7);
            if (source.getLbtRejectedDate() != null && source.getLbtComment() != null) {
                comments.add(String.format(source.getLbtComment()));
                lbtRejectedData.setComment(comments);
            }
            target.add(lbtRejectedData);

            BrandingTrackingData cbtRejectedData = new BrandingTrackingData();
            cbtRejectedData.setCode(BrandingRequestStatus.CBT_REJECTED.getCode());
            cbtRejectedData.setName(getEnumerationService().getEnumerationName(BrandingRequestStatus.CBT_REJECTED));
            if (source.getCbtRejectedDate() != null)
                cbtRejectedData.setActualTime(source.getCbtRejectedDate());
            cbtRejectedData.setIndex(8);
            if (source.getCbtRejectedDate() != null && source.getCbtComment() != null) {
                comments.add(String.format(source.getCbtComment()));
                cbtRejectedData.setComment(comments);
            }
            target.add(cbtRejectedData);

            BrandingTrackingData nshRejectedData = new BrandingTrackingData();
            nshRejectedData.setCode(BrandingRequestStatus.NSH_REJECTED.getCode());
            nshRejectedData.setName(getEnumerationService().getEnumerationName(BrandingRequestStatus.NSH_REJECTED));
            if (source.getNshRejectedDate() != null)
                nshRejectedData.setActualTime(source.getNshRejectedDate());
            nshRejectedData.setIndex(9);
            if (source.getNshRejectedDate() != null && source.getNshComment() != null) {
                comments.add(String.format(source.getNshComment()));
                nshRejectedData.setComment(comments);
            }
            target.add(nshRejectedData);*/
        }

        if(source.getNshRejectedDate()!=null || source.getLbtRejectedDate()!=null || source.getCbtRejectedDate()!=null || source.getRequestRejectedDate()!=null || source.getRequestCancelledDate()!=null)
        {
            BrandingTrackingData rejectedData = new BrandingTrackingData();
            rejectedData.setCode("REJECTED");
            rejectedData.setName("Rejected");
            List<String> rejectedComments = new ArrayList<>();
            if(source.getRequestRejectedDate()!=null) {
                rejectedData.setActualTime(source.getRequestRejectedDate());
                if(source.getRequestRejectedBy()!=null) {
                    rejectedComments.add("Rejected by: " + source.getRequestRejectedBy().getUid());
                }
            }
            else if(source.getRequestCancelledDate()!=null) {
                rejectedData.setActualTime(source.getRequestCancelledDate());
                if(source.getRequestCancelledBy()!=null) {
                    rejectedComments.add("Cancelled by: " + source.getRequestCancelledBy().getUid());
                }
            }
            else if(source.getNshRejectedDate()!=null) {
                rejectedData.setActualTime(source.getNshRejectedDate());
                rejectedComments.add("Rejected by NSH");
                if (source.getNshComment() != null) {
                    rejectedComments.add(source.getNshComment());
                }
            }
            else if(source.getCbtRejectedDate()!=null) {
                rejectedData.setActualTime(source.getCbtRejectedDate());
                rejectedComments.add("Rejected by CBT");
                if (source.getCbtComment() != null) {
                    rejectedComments.add(source.getCbtComment());
                }
            }
            else if(source.getLbtRejectedDate()!=null) {
                rejectedData.setActualTime(source.getLbtRejectedDate());
                rejectedComments.add("Rejected by LBT");
                if (source.getLbtComment() != null) {
                    rejectedComments.add(source.getLbtComment());
                }
            }
            rejectedData.setComment(rejectedComments);
            rejectedData.setIndex(7);
            target.add(rejectedData);
        }
    }

    public EnumerationService getEnumerationService() {
        return enumerationService;
    }

    public void setEnumerationService(EnumerationService enumerationService) {
        this.enumerationService = enumerationService;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
