package com.eydms.core.job;

import com.eydms.core.dao.DJPVisitDao;
import com.eydms.core.enums.ApprovalStatus;
import com.eydms.core.enums.VisitStatus;
import com.eydms.core.model.*;
import com.eydms.core.enums.VisitStatus;
import com.eydms.core.model.VisitMasterModel;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoCompletingDjpVisitsJob extends AbstractJobPerformable<CronJobModel> {

    @Autowired
    ModelService modelService;

    @Autowired
    FlexibleSearchService flexibleSearchService;

    @Autowired
    DJPVisitDao djpVisitDao;

    private static final Logger LOG = Logger.getLogger(AutoCompletingDjpVisitsJob.class);

    @Override
    public PerformResult perform(CronJobModel arg0) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        List<VisitMasterModel> visitList = getAllVisitsWithNoEndDate("STARTED");
        if(visitList.isEmpty()) {
            LOG.error("There are visits with end visit time being null and visit status as started");
            return new PerformResult(CronJobResult.FAILURE, CronJobStatus.ABORTED);
        }

        //Iterating through the list of visits with no end date
        for(VisitMasterModel visit : visitList) {
            String endVisitTime = dateFormat.format(visit.getStartVisitTime()).split(" ")[0] + " 18:29:59";
//            Date endVisitTime1 = dateFormat.parse(endVisitTime);
//            visit.setEndVisitTime(endVisitTime);
            LOG.info("The end visit time is"+endVisitTime);
            try {
                visit.setEndVisitTime(dateFormat.parse(endVisitTime));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            visit.setStatus(VisitStatus.COMPLETED);
            visit.setApprovalStatus(ApprovalStatus.SYSTEM_APPROVED);
            visit.setSynced(false);

//            if(visit.getUser().getUserType().getCode().equals("SO")) {
//                DJPRouteScoreMasterModel selectedRouteScore = visit.getRouteScore();
//                if(selectedRouteScore!=null) {
//                    DJPRunMasterModel djpRunMasterModel = selectedRouteScore.getRun();
//                    if (djpRunMasterModel != null) {
//                        RouteMasterModel recommendedRoute1 = djpRunMasterModel.getRecommendedRoute1();
//                        RouteMasterModel recommendedRoute2 = djpRunMasterModel.getRecommendedRoute2();
//
//                        RouteMasterModel selectedRouteMaster = selectedRouteScore.getRoute();
//
//                        if (recommendedRoute1 != selectedRouteMaster && recommendedRoute2 != selectedRouteMaster) {
//                            visit.setApprovalStatus(ApprovalStatus.PENDING_APPROVAL);
//                        } else {
//                            final ObjectiveModel recommendedObj1 = djpVisitDao.findOjectiveById(selectedRouteScore.getRecommendedObj1());
//                            final ObjectiveModel recommendedObj2 = djpVisitDao.findOjectiveById(selectedRouteScore.getRecommendedObj2());
//                            final ObjectiveModel selectedObjective = visit.getObjective();
//                            if (recommendedObj1 != selectedObjective && recommendedObj2 != selectedObjective) {
//                                visit.setApprovalStatus(ApprovalStatus.PENDING_APPROVAL);
//                            } else {
//                                long adHocCountersNotVisitedList = visit.getCounterVisits().stream().filter(counter -> (null != counter.getIsAdHoc() && !counter.getIsAdHoc() && null == counter.getEndVisitTime())).count();
//                                if (adHocCountersNotVisitedList > 0) {
//                                    visit.setApprovalStatus(ApprovalStatus.PENDING_APPROVAL);
//                                } else {
//                                    long adHocCountersVisitedList = visit.getCounterVisits().stream().filter(counter -> (null != counter.getIsAdHoc() && counter.getIsAdHoc()) && null != counter.getEndVisitTime()).count();
//                                    if (adHocCountersVisitedList > 0) {
//                                        visit.setApprovalStatus(ApprovalStatus.PENDING_APPROVAL);
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }

            modelService.save(visit);

            LOG.info(String.format("The visit status of the DJP Visit is marked as completed and the end visit time is populated with %s for the Visit with visitId = %s", visit.getEndVisitTime(),visit.getId()));

        }

        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }

    public List<VisitMasterModel> getAllVisitsWithNoEndDate(String status) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {vm:pk} from {VisitMaster as vm join VisitStatus as vs on {vm:status}={vs:pk}} where {vm:startVisitTime} is not null and {vm:endVisitTime} is null and {vs:code} = ?status");

        params.put("status", status);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(VisitMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<VisitMasterModel> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult()!=null && !searchResult.getResult().isEmpty()) {
            return searchResult.getResult();
        }
        else {
            return Collections.emptyList();
        }

    }
}

