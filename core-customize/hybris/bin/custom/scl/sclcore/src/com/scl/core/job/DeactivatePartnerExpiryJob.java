package com.scl.core.job;

import com.scl.core.model.PartnerCustomerModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class DeactivatePartnerExpiryJob extends AbstractJobPerformable<CronJobModel> {

    private static final Logger LOG = Logger.getLogger(DeactivatePartnerExpiryJob.class);

    @Autowired
    ModelService modelService;

    @Autowired
    FlexibleSearchService flexibleSearchService;

    @Override
    public PerformResult perform(CronJobModel cronJobModel) {
        List<String> partnerCustomerIdList = new ArrayList<>();
        List<PartnerCustomerModel> partnerCustomerModelList = getExpiredPartnerCustomers();
        LOG.info(String.format("Size of active partner customers with expired validity dates less than current time : %d", partnerCustomerModelList.size()));
        if(partnerCustomerModelList.isEmpty()) {
            LOG.error(String.format("There are no active PartnerCustomer models with expired validity dates less than current time"));
            return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
        }

        for(PartnerCustomerModel partnerCustomerModel : partnerCustomerModelList) {
            partnerCustomerModel.setActive(Boolean.FALSE);
            partnerCustomerModel.setInactiveDate(new Date());
            partnerCustomerModel.setIsDeactivatedByDealer(Boolean.FALSE);
            modelService.save(partnerCustomerModel);
            modelService.refresh(partnerCustomerModel);
            partnerCustomerIdList.add(partnerCustomerModel.getId());
        }
        LOG.info(String.format("The Partner Customer IDs processed in the DeactivatePartnerExpiryJob :%s",partnerCustomerIdList.toString()));
        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }

    private List<PartnerCustomerModel> getExpiredPartnerCustomers() {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {PartnerCustomer} where {active} = ?active and {validityExpired} < ?currentDate");

        params.put("active", Boolean.TRUE);
        params.put("currentDate", new Date());

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(PartnerCustomerModel.class));
        query.addQueryParameters(params);
        LOG.info(String.format("DeactivatePartnerExpiryJob query: %s",query));
        final SearchResult<PartnerCustomerModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult();
        else
            return Collections.emptyList();
    }
}
