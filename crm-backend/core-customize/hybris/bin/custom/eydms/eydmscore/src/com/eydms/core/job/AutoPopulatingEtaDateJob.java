package com.eydms.core.job;

import com.eydms.core.model.EtaTrackerModel;
import com.eydms.core.model.ISOMasterModel;
import com.eydms.core.model.MonthlySalesModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
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

public class AutoPopulatingEtaDateJob extends AbstractJobPerformable<CronJobModel> {

    @Autowired
    ModelService modelService;

    @Autowired
    FlexibleSearchService flexibleSearchService;

    private static final Logger LOG = Logger.getLogger(AutoPopulatingEtaDateJob.class);

    @Override
    public PerformResult perform(CronJobModel arg0) {

        List<EtaTrackerModel> etaTrackerList = getEtaTrackerList();

        if(etaTrackerList.isEmpty()) {
            LOG.error("There are no ETA tracker models with synced being false");
            return new PerformResult(CronJobResult.FAILURE, CronJobStatus.ABORTED);
        }

        //Iterating through the list of ETA Tracker models
        for(EtaTrackerModel etaTrackerModel : etaTrackerList) {
            if(etaTrackerModel.getOrderType()!=null && !etaTrackerModel.getOrderType().isEmpty()) {
                if(etaTrackerModel.getOrderType().equals("DI")) {
                    OrderEntryModel orderEntryModel = getOrderEntriesByDiNumber(etaTrackerModel.getDeliveryId());
                    if(!Objects.isNull(orderEntryModel)) {
                        orderEntryModel.setEtaDate(etaTrackerModel.getEtaDate());
                        etaTrackerModel.setSynced(true);
                        modelService.save(orderEntryModel);
                        modelService.save(etaTrackerModel);
                        if(orderEntryModel.getOrder()!=null) {
                            LOG.info(String.format("Order Entry with order code %s, entry number %d and DI Number %s is populated with ETA date", orderEntryModel.getOrder().getCode(), orderEntryModel.getEntryNumber(), orderEntryModel.getDiNumber()));
                        }

                    }
                }
//                else if(etaTrackerModel.getOrderType().equals("ISO")) {
//                        List<ISOMasterModel> isoMasterModelList = getIsoByDeliveryId(etaTrackerModel.getDeliveryId());
//                        if(!isoMasterModelList.isEmpty()) {
//                            for(ISOMasterModel isoMasterModel : isoMasterModelList) {
//                                isoMasterModel.setEtaDate(etaTrackerModel.getEtaDate());
//                                etaTrackerModel.setSynced(true);
//                                modelService.save(isoMasterModel);
//                                LOG.info(String.format("ISO Master with delivery detail ID = %s, deliveryId = %s is populated with ETA date", isoMasterModel.getDeliveryDetailId(), isoMasterModel.getDeliveryId()));
//                            }
//                            modelService.save(etaTrackerModel);
//                        }
//                }
            }
        }

        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }

    public List<EtaTrackerModel> getEtaTrackerList() {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {EtaTracker} where {synced} = ?status");

        params.put("status", Boolean.FALSE);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EtaTrackerModel.class));
        query.addQueryParameters(params);
        final SearchResult<EtaTrackerModel> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult()!=null && !searchResult.getResult().isEmpty()) {
            return searchResult.getResult();
        }
        else {
            return Collections.emptyList();
        }
    }

    public OrderEntryModel getOrderEntriesByDiNumber(String diNumber) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {OrderEntry} where {diNumber} = ?diNumber");

        params.put("diNumber", diNumber);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(OrderEntryModel.class));
        query.addQueryParameters(params);
        final SearchResult<OrderEntryModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
        else
            return null;
    }

    public List<ISOMasterModel> getIsoByDeliveryId(String deliveryId) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {ISOMaster} where {deliveryId} = ?deliveryId");

        params.put("deliveryId", deliveryId);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(ISOMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<ISOMasterModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult();
        else
            return Collections.emptyList();
    }

}
