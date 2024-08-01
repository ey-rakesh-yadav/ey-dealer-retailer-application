package com.scl.core.job;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.DjpRouteDao;
import com.scl.core.dao.OrderRequisitionDao;
import com.scl.core.dao.TerritoryManagementDao;
import com.scl.core.dao.TerritoryMasterDao;
import com.scl.core.enums.SclUserType;
import com.scl.core.model.*;
import com.scl.core.region.dao.GeographicalRegionDao;
import de.hybris.platform.b2b.company.B2BCommerceUnitService;
import de.hybris.platform.cms2.servicelayer.services.admin.CMSAdminSiteService;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.exceptions.ClassMismatchException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import java.util.*;

public class SclGeoPostLogicMasterCreationJob extends AbstractJobPerformable<CronJobModel> {


    @Resource(name = "modelService")
    private ModelService modelService;

    @Resource
    private DjpRouteDao djpRouteDao;

    @Resource
    CMSAdminSiteService cmsAdminSiteService;

    @Resource
    private GeographicalRegionDao geographicalRegionDao;


    @Resource
    TerritoryMasterDao territoryMasterDao;

    @Resource
    TerritoryManagementDao territoryManagementDao;

    public static final String DEFAULTUNIT="SclShreeUnit";

    private static final Logger LOG = Logger.getLogger(SclGeoPostLogicMasterCreationJob.class);

    @Override
    public PerformResult perform(CronJobModel arg0) {
        List<GeographicalMasterModel> geographicalMasterModelList = geographicalRegionDao.getAllGeographyMasters();
        if(CollectionUtils.isEmpty(geographicalMasterModelList) ){
            LOG.info("There are no  SclGeoPostLogicMasterCreationJob models");
            return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
        }
        else {
            for(GeographicalMasterModel sclGreo: geographicalMasterModelList) {

                String routeID;
                RouteMasterModel routemaster;

                try {

                    TalukaMasterModel tm = null;
                    DistrictMasterModel dm = null;
                    RegionMasterModel rm = null;
                    StateMasterModel sm = null;
                    Set<TalukaMasterModel> talukaMasters;
                    Set<DistrictMasterModel> districtMasters;
                    Set<RegionMasterModel> regionMasters;
                    SubAreaMasterModel subAreaMasterModel;

                    if (StringUtils.isNotEmpty(sclGreo.getTalukaCode()) && StringUtils.isNotEmpty(sclGreo.getTaluka())) {
                        tm = territoryMasterDao.getTalukaMaster(sclGreo.getTalukaCode());
                        if (Objects.isNull(tm)) {
                            tm = new TalukaMasterModel();
                            tm.setCode(sclGreo.getTalukaCode());
                            tm.setName(sclGreo.getTaluka(), Locale.ENGLISH);
                            modelService.save(tm);
                        }
                    }


                    if (StringUtils.isNotEmpty(sclGreo.getDistrict()) && StringUtils.isNotEmpty(sclGreo.getDistrictCode())) {
                        dm = territoryMasterDao.getDistrictMaster(sclGreo.getDistrictCode());

                        if (Objects.isNull(dm)) {
                            dm = new DistrictMasterModel();
                            dm.setCode(sclGreo.getDistrictCode());
                            dm.setName(sclGreo.getDistrict(),Locale.ENGLISH);
                        }

                        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(dm.getTalukas())) {
                            talukaMasters = new HashSet<>(dm.getTalukas());
                        } else {
                            talukaMasters = new HashSet<>();
                        }
                        talukaMasters.add(tm);
                        dm.setTalukas(talukaMasters);

                        modelService.save(dm);
                    }


                    if (StringUtils.isNotEmpty(sclGreo.getRegion())) {
                        rm = territoryMasterDao.getRegionMaster(sclGreo.getRegion());

                        if (Objects.isNull(rm)) {
                            rm = new RegionMasterModel();
                            rm.setCode(sclGreo.getRegion());
                           if(StringUtils.isNotEmpty(sclGreo.getRegionNameManMade())) {
                               rm.setName(sclGreo.getRegionNameManMade(), Locale.ENGLISH);
                           }
                        }

                        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(rm.getDistricts())) {
                            districtMasters = new HashSet<>(rm.getDistricts());
                        } else {
                            districtMasters = new HashSet<>();
                        }
                        districtMasters.add(dm);
                        rm.setDistricts(districtMasters);

                        modelService.save(rm);
                    }


                    if (StringUtils.isNotEmpty(sclGreo.getState()) && StringUtils.isNotEmpty(sclGreo.getStateCode())) {
                        sm = territoryMasterDao.getStateMaster(sclGreo.getStateCode());

                        if (Objects.isNull(sm)) {
                            sm = new StateMasterModel();
                            sm.setCode(sclGreo.getStateCode());
                            sm.setName(sclGreo.getState(),Locale.ENGLISH);

                        }

                        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(sm.getRegions())) {
                            regionMasters = new HashSet<>(sm.getRegions());
                        } else {
                            regionMasters = new HashSet<>();
                        }
                        regionMasters.add(rm);
                        sm.setRegions(regionMasters);

                        modelService.save(sm);
                    }

                    //SubArea Master creation
                    if(StringUtils.isNotEmpty(sclGreo.getTaluka()) && StringUtils.isNotEmpty(sclGreo.getDistrict()) ){
                        subAreaMasterModel=  territoryManagementDao.getTerritoryByDistrictAndTaluka(sclGreo.getDistrict(), sclGreo.getTaluka());

                        if(Objects.isNull(subAreaMasterModel)){
                            subAreaMasterModel=new SubAreaMasterModel();
                            subAreaMasterModel.setDistrict(sclGreo.getDistrict());
                            subAreaMasterModel.setTaluka(sclGreo.getTaluka());
                            subAreaMasterModel.setDistrictMaster(dm);
                            modelService.save(subAreaMasterModel);
                        }


                        //Routmaster
                        if(StringUtils.isNotEmpty(sclGreo.getTaluka()) && StringUtils.isNotEmpty(sclGreo.getStateCode()) && StringUtils.isNotEmpty(sclGreo.getDistrict())){

                            routeID=sclGreo.getTaluka().concat(SclCoreConstants.ALL_SCL_).concat(sclGreo.getStateCode()).concat("_").concat(sclGreo.getDistrict());

                            routemaster= djpRouteDao.findRouteById(routeID);

                            if(Objects.isNull(routemaster)){
                                routemaster=new RouteMasterModel();
                                routemaster.setRouteId(routeID);
                                routemaster.setRouteName(routeID);
                                routemaster.setBrand(SclCoreConstants.SCL_SITE);
                                routemaster.setDistrict(sclGreo.getDistrict());
                                routemaster.setSubArea(sclGreo.getTaluka());
                                routemaster.setState(sclGreo.getState());
                                routemaster.setSubAreaMaster(subAreaMasterModel);
                            }
                            else {
                                routemaster.setRouteName(routeID);
                                routemaster.setDistrict(sclGreo.getDistrict());
                                routemaster.setSubArea(sclGreo.getTaluka());
                                routemaster.setState(sclGreo.getState());
                                routemaster.setBrand(SclCoreConstants.SCL_SITE);
                                routemaster.setSubAreaMaster(subAreaMasterModel);
                            }
                            modelService.save(routemaster);

                        }
                    }





                }catch (RuntimeException e){
                    LOG.info("SclGeoPostLogicMasterCreationJob for transZone:"+ sclGreo.getTransportationZone() +" exception: "+ e.getMessage());
                    e.printStackTrace();;
                }


            }
        }

        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }



}
