package com.scl.integration.cpi.cancelorder;

import com.scl.core.dao.DataConstraintDao;
import com.scl.core.enums.CreatedFromCRMorERP;
import com.scl.core.enums.RequisitionStatus;
import com.scl.core.model.OrderRequisitionModel;
import com.scl.core.model.SclOrderLineCancelProcessModel;
import com.scl.core.utility.SclDateUtility;
import de.hybris.platform.constants.GeneratedCoreConstants;
import de.hybris.platform.core.enums.ExportStatus;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class SclOrderLineErpCancelRetriesJob extends AbstractJobPerformable<CronJobModel> {

    @Autowired
    FlexibleSearchService flexibleSearchService;

    @Autowired
    SclSapCpiOutboundCancelOrderLineAction sapCpiOutboundCancelOrderLineAction;

    @Autowired
    DataConstraintDao dataConstraintDao;

    private static final Logger LOG = Logger.getLogger(SclOrderLineErpCancelRetriesJob.class);

    @Override
    public PerformResult perform(CronJobModel arg0) {
        List<OrderEntryModel> crmCancelledOrderEntries = getCancelledOrderEntries();
        if(crmCancelledOrderEntries.isEmpty()) {
            LOG.info("There are no Order Entries satisfying the conditions: Status = Cancelled and cancelOrderLineApiStatus = Not Exported and ErpLineItemId = null");
            return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
        }
        else {
            LOG.info(String.format("No. of cancelled orders processed : %d", crmCancelledOrderEntries.size()));
        }
        for(OrderEntryModel orderEntryModel : crmCancelledOrderEntries) {
            SclOrderLineCancelProcessModel processModel = new SclOrderLineCancelProcessModel();
            OrderModel orderModel = orderEntryModel.getOrder();
            processModel.setOrder(orderModel);
            processModel.setCrmEntryNumber(orderEntryModel.getEntryNumber());
            processModel.setEntryNumber(Integer.valueOf(orderEntryModel.getErpLineItemId()));
            try {
                sapCpiOutboundCancelOrderLineAction.executeAction(processModel);
                LOG.info(String.format("Inside Order Line ERP Cancel Retries Job. Order : %s, EntryNumber: %d and erpLineItemId : %s", orderModel.getCode(), orderEntryModel.getEntryNumber(), orderEntryModel.getErpLineItemId()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }

    public List<OrderEntryModel> getCancelledOrderEntries() {
        final Map<String, Object> params = new HashMap<String, Object>();

        params.put("status", OrderStatus.CANCELLED);
        params.put("cancelOrderLineApiStatus", ExportStatus.NOTEXPORTED);


        List<CreatedFromCRMorERP> createdFromCRMorERP = new ArrayList<CreatedFromCRMorERP>();
        createdFromCRMorERP.add(CreatedFromCRMorERP.CRM);
        createdFromCRMorERP.add(CreatedFromCRMorERP.S4HANA);
        params.put("createdFromCRMorERP", createdFromCRMorERP);

        Integer lastXDays = dataConstraintDao.findDaysByConstraintName("CANCELLED_ORDER_ERP_RETRIES_TIME");

        final StringBuilder builder = new StringBuilder("select {oe:pk} from {OrderEntry as oe join Order as o on {oe:order}={o:pk}}  where ").append(SclDateUtility.getLastXDayQuery("oe:cancelledDate",params,lastXDays)).append(" and {oe:status} = ?status and {cancelOrderLineApiStatus} = ?cancelOrderLineApiStatus and {erpLineItemId} is not null and {o.createdFromCRMorERP} in (?createdFromCRMorERP)");

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(OrderEntryModel.class));
        query.addQueryParameters(params);
        
        LOG.info("SCL order cancel retry job query::"+query);
        
        final SearchResult<OrderEntryModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult();
        else
            return Collections.emptyList();
    }

}
