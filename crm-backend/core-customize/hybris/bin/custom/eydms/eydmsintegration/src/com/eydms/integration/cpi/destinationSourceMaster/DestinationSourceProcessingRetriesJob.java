package com.eydms.integration.cpi.destinationSourceMaster;

import com.eydms.core.enums.CustomerCategory;
import com.eydms.core.enums.OrderType;
import com.eydms.core.enums.WarehouseType;
import com.eydms.core.model.*;
import com.eydms.facades.data.LPSourceMasterData;
import com.eydms.facades.data.LPSourceMasterListData;
import com.eydms.integration.cpi.order.EyDmsInboundPriceAction;
import com.eydms.integration.cpi.order.EyDmsSapCpiOutboundService;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;

public class DestinationSourceProcessingRetriesJob extends AbstractJobPerformable<CronJobModel> {

    @Autowired
    FlexibleSearchService flexibleSearchService;

    @Autowired
    ModelService modelService;

    @Autowired
    BaseSiteService baseSiteService;

    private EyDmsSapCpiOutboundService eydmsSapCpiDefaultOutboundService;

    private static final Logger LOG = Logger.getLogger(DestinationSourceProcessingRetriesJob.class);

    @Override
    public PerformResult perform(CronJobModel arg0) {
        executeEyDmsOutboundLpSource();

        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }

    public boolean removeExistingDestinationSourceMasterData() {
        List<DestinationSourceMasterModel> existingDestinationSourceMasterModels = getAllDestinationSourceRecords();
        if(existingDestinationSourceMasterModels.isEmpty()) {
            return false;
        }
        modelService.removeAll(existingDestinationSourceMasterModels);
        return true;
    }

    public boolean insertDestinationSourceData(LPSourceMasterListData lpSourceMasterListData) {
        List<LPSourceMasterData> lpSourceMasterDataList = lpSourceMasterListData.getLpSourceMaster();
        for(LPSourceMasterData lpSourceMasterData : lpSourceMasterDataList) {
            DestinationSourceMasterModel destinationSourceMasterModel = modelService.create(DestinationSourceMasterModel.class);

            destinationSourceMasterModel.setOrderType(OrderType.valueOf(lpSourceMasterData.getOrderType()));
            if(baseSiteService.getBaseSiteForUID(lpSourceMasterData.getBrand()) != null) {
                destinationSourceMasterModel.setBrand(baseSiteService.getBaseSiteForUID(lpSourceMasterData.getBrand()));
            }
            destinationSourceMasterModel.setCustomerCategory(CustomerCategory.valueOf(lpSourceMasterData.getCustCategory()));
            if(getDeliveryMode(lpSourceMasterData.getDeliveryMode())!=null) {
                destinationSourceMasterModel.setDeliveryMode(getDeliveryMode(lpSourceMasterData.getDeliveryMode()));
            }
            destinationSourceMasterModel.setGrade(lpSourceMasterData.getGrade());
            destinationSourceMasterModel.setPackaging(lpSourceMasterData.getPackaging());
            destinationSourceMasterModel.setDestinationTaluka(lpSourceMasterData.getDestTaluka());
            destinationSourceMasterModel.setDestinationDistrict(lpSourceMasterData.getDestDistrict());
            destinationSourceMasterModel.setDestinationCity(lpSourceMasterData.getDestCity());
            destinationSourceMasterModel.setDestinationState(lpSourceMasterData.getDestState());
            destinationSourceMasterModel.setSourceTaluka(lpSourceMasterData.getSourceTaluka());
            destinationSourceMasterModel.setSourceDistrict(lpSourceMasterData.getSourceDistrict());
            destinationSourceMasterModel.setSourceCity(lpSourceMasterData.getSourceCity());
            destinationSourceMasterModel.setSourceState(lpSourceMasterData.getSourceState());
            destinationSourceMasterModel.setSource(getWarehouseByCode(lpSourceMasterData.getSource()));
            destinationSourceMasterModel.setNcrCost(lpSourceMasterData.getNcrCost());
            destinationSourceMasterModel.setSourcePriority(lpSourceMasterData.getSourcePriority());
            destinationSourceMasterModel.setRoute(lpSourceMasterData.getRoute());
            destinationSourceMasterModel.setTlcPerMT(lpSourceMasterData.getTlcPerMT());
            destinationSourceMasterModel.setContributionPerMT(lpSourceMasterData.getContributionPerMT());
            destinationSourceMasterModel.setType(WarehouseType.valueOf(lpSourceMasterData.getType()));
            destinationSourceMasterModel.setSecondaryRoute(lpSourceMasterData.getSecondaryRoute());

            modelService.save(destinationSourceMasterModel);
        }

        return true;
    }

    public List<DestinationSourceMasterModel> getAllDestinationSourceRecords() {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {DestinationSourceMaster}");

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(DestinationSourceMasterModel.class));
        final SearchResult<DestinationSourceMasterModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult();
        else
            return Collections.emptyList();
    }

    public DeliveryModeModel getDeliveryMode(String deliveryMode) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {DeliveryMode} where {code} = ?deliveryMode");

        params.put("deliveryMode", deliveryMode);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(DeliveryModeModel.class));
        query.addQueryParameters(params);
        final SearchResult<DeliveryModeModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
        else
            return null;
    }

    public WarehouseModel getWarehouseByCode(String code) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {Warehouse} where {code} = ?code");

        params.put("code", code);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(WarehouseModel.class));
        query.addQueryParameters(params);
        final SearchResult<WarehouseModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
        else
            return null;
    }

    public EyDmsSapCpiOutboundService getEyDmsSapCpiDefaultOutboundService() {
        return eydmsSapCpiDefaultOutboundService;
    }

    public boolean executeEyDmsOutboundLpSource() {
//        EyDmsOutboundLpSourceModel outboundLpSourceModel = new EyDmsOutboundLpSourceModel();
//        outboundLpSourceModel.setCurrentDate(new Date());

        final LpSourceWrapper lpSourceWrapper = new LpSourceWrapper();
        lpSourceWrapper.responseRecieved = false;
        lpSourceWrapper.apiCallCount = 0;

//        callEyDmsOutboundLpSource(outboundLpSourceModel,lpSourceWrapper);
        return true;
    }

//    private void callEyDmsOutboundLpSource(EyDmsOutboundLpSourceModel outboundLpSourceMasterModel, LpSourceWrapper lpSourceWrapper) {
//        lpSourceWrapper.apiCallCount++;
//        LOG.info("LP Souce API count :" + lpSourceWrapper.apiCallCount);
//        getEyDmsSapCpiDefaultOutboundService().getLpSourceData(outboundLpSourceMasterModel).subscribe(
//
//                // onNext
//                responseEntityMap -> {
//                    String response = getPropertyValue(responseEntityMap, "IS_LP_SOURCE_AVAILABLE_IN_SLCT");
//                    if(StringUtils.isNotBlank(response)) {
//                        LOG.error("LP Source API call response Test" + " : " + response);
//                    }
//                    else {
//                        LOG.error("LP Sourcce api call error Test");
//                        if(lpSourceWrapper.apiCallCount<5 && lpSourceWrapper.responseRecieved==false) {
//                            callEyDmsOutboundLpSource(outboundLpSourceMasterModel, lpSourceWrapper);
//                        }
//                        else {
//                            //updatePriceNotFoundStatus(order);
//                            LOG.error("LP Source API Call Count exceeded the limit : 5 times");
//
//                        }
//                    }
//
//                }
//
//                //onError
//                , error -> {
//                    LOG.error("LP Sourcce api call error Test");
//                    if(lpSourceWrapper.apiCallCount<5 && lpSourceWrapper.responseRecieved==false) {
//                        callEyDmsOutboundLpSource(outboundLpSourceMasterModel, lpSourceWrapper);
//                    }
//                    else {
//                        //updatePriceNotFoundStatus(order);
//                        LOG.error("LP Source API Call Count exceeded the limit : 5 times");
//
//                    }
//                }
//
//
//
//        );
//    }

    static String getPropertyValue(ResponseEntity<Map> responseEntityMap, String property)
    {
        if (responseEntityMap.getBody() != null)
        {
            Object next = responseEntityMap.getBody().keySet().iterator().next();
            checkArgument(next != null,
                    String.format("SCPI response entity key set cannot be null for property [%s]!", property));

            String responseKey = next.toString();
            checkArgument(responseKey != null && !responseKey.isEmpty(),
                    String.format("SCPI response property can neither be null nor empty for property [%s]!", property));

            Object propertyValue = responseEntityMap.getBody().get(responseKey);
            //checkArgument(propertyValue != null, String.format("SCPI response property [%s] value cannot be null!", property));

            return propertyValue.toString();
        }
        else
        {
            return null;
        }
    }

    public class LpSourceWrapper {
        public int apiCallCount;
        public boolean responseRecieved;
    }
}
