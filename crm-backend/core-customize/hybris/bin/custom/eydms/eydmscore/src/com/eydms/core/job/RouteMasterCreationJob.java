package com.eydms.core.job;

import com.eydms.core.dao.DataConstraintDao;
import com.eydms.core.dao.SlctCrmIntegrationDao;
import com.eydms.core.model.RouteMasterModel;
import com.eydms.core.model.UserSubAreaMappingModel;
import com.eydms.core.model.VisitMasterModel;
import com.eydms.core.utility.EyDmsDateUtility;
import com.eydms.facades.data.SlctCrmOrderEntryData;
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
import org.hsqldb.rights.User;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class RouteMasterCreationJob extends AbstractJobPerformable<CronJobModel> {

    @Autowired
    ModelService modelService;

    @Autowired
    FlexibleSearchService flexibleSearchService;

    @Autowired
    SlctCrmIntegrationDao slctCrmIntegrationDao;

    @Autowired
    DataConstraintDao dataConstraintDao;

    private static final Logger LOG = Logger.getLogger(RouteMasterCreationJob.class);

    @Override
    public PerformResult perform(CronJobModel arg0) {
        List<UserSubAreaMappingModel> userSubAreaMappingModelList = getModifiedUserSubAreaMappingModels();
        if(userSubAreaMappingModelList.isEmpty()) {
            LOG.info("There are no modified UserSubAreaMapping models");
            return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
        }
        else {
            for(UserSubAreaMappingModel userSubAreaMappingModel: userSubAreaMappingModelList) {
                if(userSubAreaMappingModel.getBrand()!=null && userSubAreaMappingModel.getEyDmsUser()!=null && userSubAreaMappingModel.getSubAreaMaster()!=null && userSubAreaMappingModel.getState()!=null && !userSubAreaMappingModel.getState().isEmpty()) {
                    RouteMasterModel routeMasterModel = slctCrmIntegrationDao.getRouteMaster(userSubAreaMappingModel.getSubArea(),userSubAreaMappingModel.getDistrict(),userSubAreaMappingModel.getState(),userSubAreaMappingModel.getBrand().getUid());
                    String stateCode = slctCrmIntegrationDao.getStateCode(userSubAreaMappingModel.getState());
                    if(Objects.isNull(routeMasterModel)) {
                        routeMasterModel = modelService.create(RouteMasterModel.class);
                        String routeId = userSubAreaMappingModel.getSubAreaMaster().getTaluka() + "_ALL_" + userSubAreaMappingModel.getBrand().getUid() + "_" + stateCode + "_" + userSubAreaMappingModel.getSubAreaMaster().getDistrict();
                        routeMasterModel.setRouteId(routeId);
                        routeMasterModel.setRouteName(routeId);
                        routeMasterModel.setSubArea(userSubAreaMappingModel.getSubAreaMaster().getTaluka());
                        routeMasterModel.setDistrict(userSubAreaMappingModel.getSubAreaMaster().getDistrict());
                        routeMasterModel.setState(userSubAreaMappingModel.getState());
                        routeMasterModel.setBrand(userSubAreaMappingModel.getBrand().getUid());
                        routeMasterModel.setSubAreaMaster(userSubAreaMappingModel.getSubAreaMaster());
                        routeMasterModel.setIsDefaultRoute(true);
                        modelService.save(routeMasterModel);
                    }
                }

            }
        }

        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }

    public List<UserSubAreaMappingModel> getModifiedUserSubAreaMappingModels() {
        final Map<String, Object> params = new HashMap<String, Object>();

        Integer lastXDays = dataConstraintDao.findDaysByConstraintName("LAST_UPDATED_DO_SUBAREA_MAPPING_TIME");

        final StringBuilder builder = new StringBuilder("select {pk} from {UserSubAreaMapping} where ").append(EyDmsDateUtility.getLastXDayQuery("modifiedTime",params,lastXDays));

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(UserSubAreaMappingModel.class));
        query.addQueryParameters(params);
        final SearchResult<UserSubAreaMappingModel> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult()!=null && !searchResult.getResult().isEmpty()) {
            return searchResult.getResult();
        }
        else {
            return Collections.emptyList();
        }

    }


}
