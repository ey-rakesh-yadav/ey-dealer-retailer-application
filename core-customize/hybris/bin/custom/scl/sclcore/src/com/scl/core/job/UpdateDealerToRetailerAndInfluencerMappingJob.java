package com.scl.core.job;

import com.scl.core.dao.DJPVisitDao;
import com.scl.core.dao.TerritoryManagementDao;
import com.scl.core.model.*;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UpdateDealerToRetailerAndInfluencerMappingJob extends AbstractJobPerformable<CronJobModel> {
    private static final Logger LOG = Logger.getLogger(UpdateDealerToRetailerAndInfluencerMappingJob.class);

    @Autowired
    DJPVisitDao djpVisitDao;

    @Autowired
    UserService userService;

    @Autowired
    TerritoryManagementDao territoryManagementDao;

    @Autowired
    BaseSiteService baseSiteService;
    @Override
    public PerformResult perform(CronJobModel cronJobModel) {
        try {
            List<List<String>> listOfDealerToRetailerAndInflu=djpVisitDao.getMappingRecordsByTransType();
            for (List<String> listAsPerTransType : listOfDealerToRetailerAndInflu) {
                getValue(listAsPerTransType);
            }
         return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
        }
        catch (Exception e){
            LOG.error("Unknown exception occured:", e);
            return new PerformResult(CronJobResult.ERROR, CronJobStatus.FINISHED);
        }

    }

    private void getValue (List<String> listAsPerTransType) {
             List<DealerRetailerMapModel> listOfDealerAndRetailers=new ArrayList<>();
             DealerRetailerMapModel dealerRetailerMapModel = new DealerRetailerMapModel();

             if(listAsPerTransType.get(2).equals("Retailer"))
                {
                    dealerRetailerMapModel.setDealerCustNo(listAsPerTransType.get(0));
                    dealerRetailerMapModel.setRetailerCustNo(listAsPerTransType.get(1));
                    listOfDealerAndRetailers.add(dealerRetailerMapModel);
                }
             else{
                 dealerRetailerMapModel.setDealerCustNo(listAsPerTransType.get(0));
                 dealerRetailerMapModel.setRetailerCustNo(listAsPerTransType.get(1));
                 listOfDealerAndRetailers.add(dealerRetailerMapModel);
             }
        modelService.saveAll(listOfDealerAndRetailers);
    }

}
