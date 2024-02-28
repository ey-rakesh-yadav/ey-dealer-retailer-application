package com.eydms.core.job;

import com.eydms.core.enums.RequisitionStatus;
import com.eydms.core.model.MonthlySalesModel;
import com.eydms.core.model.OrderRequisitionModel;
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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class AutoCancellingOrderRequisitionJob extends AbstractJobPerformable<CronJobModel> {

    @Autowired
    ModelService modelService;

    @Autowired
    FlexibleSearchService flexibleSearchService;

    private static final Logger LOG = Logger.getLogger(AutoCancellingOrderRequisitionJob.class);

    @Override
    public PerformResult perform(CronJobModel cronJobModel) {
        LocalDate currentDate = LocalDate.now();
        Date beforeThreeDays = Date.from(currentDate.minusDays(3).atStartOfDay(ZoneId.systemDefault()).toInstant());
        LOG.info(String.format("Before Three Days:%s",beforeThreeDays));

        List<OrderRequisitionModel> orderRequisitionModelList = getOrderRequisitionsBeforeThreeDays(beforeThreeDays);
        if(orderRequisitionModelList.isEmpty()) {
            LOG.error("There are no Order requisitions with status as pending confirmation before three days or synced would be false");
            return new PerformResult(CronJobResult.FAILURE, CronJobStatus.ABORTED);
        }

        LOG.info(String.format("No. of order requisitions to be cancelled:%d",orderRequisitionModelList.size()));
        for(OrderRequisitionModel orderRequisitionModel : orderRequisitionModelList) {
//            orderRequisitionModel.setStatus(RequisitionStatus.CANCELLED);
//            orderRequisitionModel.setCancelledDate(new Date());
//            orderRequisitionModel.setSynced(true);
//            orderRequisitionModel.setCancelReason("Auto Cancel by system after 3 days");
            orderRequisitionModel.setStatus(RequisitionStatus.REJECTED);
            orderRequisitionModel.setRejectedDate(new Date());
            orderRequisitionModel.setRejectReason("Auto rejected by system after 3 days");
            orderRequisitionModel.setSynced(true);
            modelService.save(orderRequisitionModel);
            LOG.info(String.format("Order requisition with id %s that was created on %s is being cancelled",orderRequisitionModel.getRequisitionId(),orderRequisitionModel.getRequisitionDate()));
        }

        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }

    public List<OrderRequisitionModel> getOrderRequisitionsBeforeThreeDays(Date requisitionDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {OrderRequisition} where {requisitionDate} < ?requisitionDate and {status} = ?status and {synced} = ?synced");

        params.put("requisitionDate", requisitionDate);
        params.put("status", RequisitionStatus.PENDING_CONFIRMATION);
        params.put("synced", Boolean.FALSE);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(OrderRequisitionModel.class));
        query.addQueryParameters(params);
        final SearchResult<OrderRequisitionModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult();
        else
            return Collections.emptyList();
    }
}
