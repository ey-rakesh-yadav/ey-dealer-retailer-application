package com.eydms.integration.cpi.hook;

import com.eydms.core.enums.BrandingRequestStatus;
import com.eydms.core.model.BrandingRequestDetailsModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.odata2services.odata.persistence.hook.PostPersistHook;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.log4j.Logger;

import javax.annotation.Resource;

public class EyDmsBrandingPostPersistHook implements PostPersistHook {
    private static final Logger LOG = Logger.getLogger(EyDmsBrandingPostPersistHook.class);
    @Resource
    ModelService modelService;

    public static final String WAITING_FOR_BRANDING_APPROVAL="WAIT_FOR_BRANDING_APPROVAL";
    @Override
    public void execute(ItemModel item) {
        if (item instanceof BrandingRequestDetailsModel) {
            LOG.info("The persistence hook EyDmsBrandingPostPersistHook is called!");
            final BrandingRequestDetailsModel brandingRequestDetailsModel = (BrandingRequestDetailsModel) item;
            if(brandingRequestDetailsModel.getNshApprovedDate()!=null)
            {
                brandingRequestDetailsModel.setRequestStatus(BrandingRequestStatus.NSH_APPROVED);
            }
            else if(brandingRequestDetailsModel.getNshRejectedDate()!=null)
            {
                brandingRequestDetailsModel.setRequestStatus(BrandingRequestStatus.NSH_REJECTED);
            }
            else if(brandingRequestDetailsModel.getCbtApprovedDate()!=null)
            {
                brandingRequestDetailsModel.setRequestStatus(BrandingRequestStatus.CBT_APPROVED);
            }
            else if(brandingRequestDetailsModel.getCbtRejectedDate()!=null)
            {
                brandingRequestDetailsModel.setRequestStatus(BrandingRequestStatus.CBT_REJECTED);
            }
            else if(brandingRequestDetailsModel.getLbtApprovedDate()!=null)
            {
                brandingRequestDetailsModel.setRequestStatus(BrandingRequestStatus.LBT_APPROVED);
            }
            else if(brandingRequestDetailsModel.getLbtRejectedDate()!=null)
            {
                brandingRequestDetailsModel.setRequestStatus(BrandingRequestStatus.LBT_REJECTED);
            }
            modelService.save(brandingRequestDetailsModel);
        }
    }
}
