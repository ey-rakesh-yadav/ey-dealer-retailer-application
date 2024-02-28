package com.eydms.core.job;

import com.eydms.core.jalo.EyDmsUser;
import com.eydms.core.model.CustomerSubAreaMappingModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.UserModel;
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

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class CustomerActiveStatusUpdateJob extends AbstractJobPerformable<CronJobModel> {

    private static final Logger LOG = Logger.getLogger(CustomerActiveStatusUpdateJob.class);

    @Resource
    ModelService modelService;

    @Resource
    FlexibleSearchService flexibleSearchService;

    @Override
    public PerformResult perform(CronJobModel cronJobModel) {
        try {
            LOG.info("inside CustomerActiveStatusUpdateJob");
            List<CustomerSubAreaMappingModel> allDealersFromErpOrder = getAllDealersFromErpOrder();
            LOG.info("allDealersFromErpOrder size::"+allDealersFromErpOrder.size());
            if(allDealersFromErpOrder!=null && !allDealersFromErpOrder.isEmpty())
            {
                for (CustomerSubAreaMappingModel customerSubAreaMappingModel : allDealersFromErpOrder) {
                    customerSubAreaMappingModel.setIsActive(true);
                    modelService.save(customerSubAreaMappingModel);
                }
            }
            return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
        }
        catch (Exception e)
        {
            LOG.error("issue while performing CustomerActiveStatusUpdate ::"+e.getMessage());
            return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
        }
    }

    public List<CustomerSubAreaMappingModel> getAllDealersFromErpOrder() {

        LocalDate date = LocalDate.now();
        LocalDate previousDate = date.minusDays(1);
        //Date endDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date startDate = Date.from(previousDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        LOG.info("getAllDealersFromErpOrder start Date::" + startDate);
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select distinct {m.pk} from {Order as o join CustomerSubAreaMapping as m on {m:eydmsCustomer}={o:user} and {m:brand}={o:site}} where  {o:creationTime} >= ?startDate and {m:isActive}=?isActive");
        params.put("isActive", false);
        params.put("startDate", startDate);
        //params.put("endDate", endDate);
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(CustomerSubAreaMappingModel.class));
        query.addQueryParameters(params);
        final SearchResult<CustomerSubAreaMappingModel> searchResult = flexibleSearchService.search(query);
        List<CustomerSubAreaMappingModel> result = searchResult.getResult();
        LOG.info("result for getAllDealersFromErpOrder::"+result);
        return result!=null ? result : Collections.emptyList();
    }

    public ModelService getModelService() {
        return modelService;
    }

    @Override
    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    public FlexibleSearchService getFlexibleSearchService() {
        return flexibleSearchService;
    }

    @Override
    public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }
}