package com.scl.integration.cpi.hook;

import com.scl.core.enums.BrandingRequestStatus;
import com.scl.core.enums.DealerRequestStatus;
import com.scl.core.model.BrandingRequestDetailsModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.odata2services.odata.persistence.hook.PostPersistHook;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;

public class SclBrandingPostPersistHook implements PostPersistHook {
    private static final Logger LOG = Logger.getLogger(SclBrandingPostPersistHook.class);
    @Resource
    ModelService modelService;
    @Autowired
    EnumerationService enumerationService;

    public static final String WAITING_FOR_BRANDING_APPROVAL="WAIT_FOR_BRANDING_APPROVAL";
    @Override
    public void execute(ItemModel item) {
        if (item instanceof BrandingRequestDetailsModel) {
            LOG.info("The persistence hook SclBrandingPostPersistHook is called!");
            final BrandingRequestDetailsModel brandingRequestDetailsModel = (BrandingRequestDetailsModel) item;
            if (brandingRequestDetailsModel.getNshApprovedDate() != null) {
                brandingRequestDetailsModel.setRequestStatus(BrandingRequestStatus.NSH_APPROVED);
            } else if (brandingRequestDetailsModel.getNshRejectedDate() != null) {
                brandingRequestDetailsModel.setRequestStatus(BrandingRequestStatus.NSH_REJECTED);
            } else if (brandingRequestDetailsModel.getCbtApprovedDate() != null) {
                brandingRequestDetailsModel.setRequestStatus(BrandingRequestStatus.CBT_APPROVED);
            } else if (brandingRequestDetailsModel.getCbtRejectedDate() != null) {
                brandingRequestDetailsModel.setRequestStatus(BrandingRequestStatus.CBT_REJECTED);
            } else if (brandingRequestDetailsModel.getLbtApprovedDate() != null) {
                brandingRequestDetailsModel.setRequestStatus(BrandingRequestStatus.LBT_APPROVED);
            } else if (brandingRequestDetailsModel.getLbtRejectedDate() != null) {
                brandingRequestDetailsModel.setRequestStatus(BrandingRequestStatus.LBT_REJECTED);
            }
            String reqStatus = null;

            if (brandingRequestDetailsModel.getRequestStatus() != null) {
                reqStatus = enumerationService.getEnumerationName(brandingRequestDetailsModel.getRequestStatus());
            }
            if (reqStatus != null && reqStatus.toUpperCase().contains("REJECTED")) {
                brandingRequestDetailsModel.setDealerRequestStatus(DealerRequestStatus.REJECTED);
            }
            if (reqStatus != null && reqStatus.toUpperCase().contains("CANCELLED")) {
                brandingRequestDetailsModel.setDealerRequestStatus(DealerRequestStatus.CANCELLED);
            }
            if (reqStatus != null)
                if (!reqStatus.toUpperCase().contains("CANCELLED") && !reqStatus.toUpperCase().contains("REJECTED")) {
                    if (brandingRequestDetailsModel.getNshApprovedDate() == null)
                        brandingRequestDetailsModel.setDealerRequestStatus(DealerRequestStatus.PENDING);
                    else if (brandingRequestDetailsModel.getNshApprovedDate() != null && brandingRequestDetailsModel.getInvoiceUploadedDate() == null)
                        brandingRequestDetailsModel.setDealerRequestStatus(DealerRequestStatus.APPROVED);
                    else if (brandingRequestDetailsModel.getInvoiceUploadedDate() != null)
                        brandingRequestDetailsModel.setDealerRequestStatus(DealerRequestStatus.COMPLETED);
                }
            modelService.save(brandingRequestDetailsModel);
        }
    }
}
