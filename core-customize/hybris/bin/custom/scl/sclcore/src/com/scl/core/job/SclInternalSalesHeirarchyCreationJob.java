package com.scl.core.job;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.*;
import com.scl.core.enums.CreatedFromCRMorERP;
import com.scl.core.enums.SclUserType;
import com.scl.core.model.*;
import com.scl.core.utility.SclDateUtility;
import de.hybris.platform.b2b.company.B2BCommerceUnitService;
import de.hybris.platform.cms2.servicelayer.services.admin.CMSAdminSiteService;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.exceptions.ClassMismatchException;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class SclInternalSalesHeirarchyCreationJob extends AbstractJobPerformable<CronJobModel> {

    public static final String FIND_SCLUSES_PASSword = "FIND_SCLUSES_PASSword";

    public static final String FIND_SCLUSES_PASSword_QAENV = "FIND_SCLUSES_PASSword_QAENV";
    
    public static final String ENABLE_SALES_HIERARCHY_HIGHER_ROLE = "ENABLE_SALES_HIERARCHY_HIGHER_ROLE";

    @Resource
    TerritoryMasterDao territoryMasterDao;

    @Resource(name = "modelService")
    private ModelService modelService;

    @Resource
    FlexibleSearchService flexibleSearchService;

    @Resource(name = "userService")
    private UserService userService;

    @Resource(name = "b2bCommerceUnitService")
    private B2BCommerceUnitService b2bCommerceUnitService;

    @Autowired
    ConfigurationService configurationService;

    @Resource
    DataConstraintDao dataConstraintDao;

    @Resource
    SclDealerRetailerDao sclDealerRetailerDao;

    @Resource
    OrderRequisitionDao orderRequisitionDao;

    @Resource
    private DealerDao dealerDao;


    @Resource
    CMSAdminSiteService cmsAdminSiteService;

    public static final String DEFAULTUNIT="SclShreeUnit";

    private static final Logger LOG = Logger.getLogger(SclInternalSalesHeirarchyCreationJob.class);

    @Override
    public PerformResult perform(CronJobModel arg0) {
        List<SCLIntSalesHierarchyModel> sCLIntSalesHierarchyModelList = territoryMasterDao.getAllIntSalesHierarchy();
        if(CollectionUtils.isEmpty(sCLIntSalesHierarchyModelList) ){
            LOG.info("There are no  sCLIntSalesHierarchyModelList models");
            return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
        }
        else {
            for(SCLIntSalesHierarchyModel sCLIntSalesHierarchy: sCLIntSalesHierarchyModelList) {



                SclUserModel sclUser;
                SclUserModel tsm;
                SclUserModel regionHead;
                SclUserModel stateHead;
                SclUserModel clusterHead;
                SclUserModel zonalHead;
                SclUserModel nationalHead;

                try {

                    Set<PrincipalGroupModel> tsmugSet = new HashSet<>();
                    Set<PrincipalGroupModel> rhugSet = new HashSet<>();
                    Set<PrincipalGroupModel> stateugSet = new HashSet<>();
                    Set<PrincipalGroupModel> clusterugSet = new HashSet<>();
                    Set<PrincipalGroupModel> zonalugSet = new HashSet<>();
                    Set<PrincipalGroupModel> nationalugSet = new HashSet<>();
                    Set<PrincipalGroupModel> sclUserGPSet = new HashSet<>();
                    UserGroupModel defaultB2bcustgrp = userService.getUserGroupForUID("b2bcustomergroup");

                    TerritoryMasterModel territoryMaster = orderRequisitionDao.getTerritoryMasterByTrriId(sCLIntSalesHierarchy.getTerritoryCode());

                    if (Objects.isNull(territoryMaster)) {
                        territoryMaster = new TerritoryMasterModel();
                        territoryMaster.setTerritoryCode(sCLIntSalesHierarchy.getTerritoryCode());
                    }
                    modelService.save(territoryMaster);


/*
                    if(StringUtils.isNotEmpty(sCLIntSalesHierarchy.getNationalHeadEmail()) && StringUtils.isNotEmpty(sCLIntSalesHierarchy.getNationalHeadCode()) && StringUtils.isNotEmpty(sCLIntSalesHierarchy.getNationalHeadName())) {

                        nationalHead = sclUserSearchOrCreate(sCLIntSalesHierarchy.getNationalHeadEmail(), sCLIntSalesHierarchy.getNationalHeadName(), sCLIntSalesHierarchy.getNationalHeadEmail());
                        nationalHead.setDefaultB2BUnit(b2bCommerceUnitService.getUnitForUid(DEFAULTUNIT));
                        nationalugSet.add(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.NATINALHEAD_GROUP_ID));
                        nationalugSet.add(defaultB2bcustgrp);
                        nationalHead.setGroups(nationalugSet);
                        nationalHead.setUserType(SclUserType.NATIONALHEAD);
                        nationalHead.setEmployeeCode(sCLIntSalesHierarchy.getNationalHeadCode());

                        Set<TerritoryMasterModel> nhTerriMasters;
                        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(nationalHead.getTerritoryMaster())) {
                            nhTerriMasters = new HashSet<>(nationalHead.getTerritoryMaster());
                        } else {
                            nhTerriMasters = new HashSet<>();
                        }
                        nhTerriMasters.add(territoryMaster);
                        nationalHead.setTerritoryMaster(nhTerriMasters);

                        modelService.save(nationalHead);

                        createTUMForSclUser(sCLIntSalesHierarchy,territoryMaster,nationalHead);
                    }

                    if(StringUtils.isNotEmpty(sCLIntSalesHierarchy.getZonalHeadEmail()) && StringUtils.isNotEmpty(sCLIntSalesHierarchy.getZonalHeadCode()) && StringUtils.isNotEmpty(sCLIntSalesHierarchy.getZonalHeadName())) {


                        zonalHead = sclUserSearchOrCreate(sCLIntSalesHierarchy.getZonalHeadEmail(), sCLIntSalesHierarchy.getZonalHeadName(), sCLIntSalesHierarchy.getZonalHeadEmail());
                        zonalHead.setDefaultB2BUnit(b2bCommerceUnitService.getUnitForUid(DEFAULTUNIT));

                        zonalugSet.add(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.ZONALHEAD_GROUP_ID));
                        zonalugSet.add(defaultB2bcustgrp);
                        zonalHead.setGroups(zonalugSet);
                        zonalHead.setUserType(SclUserType.ZONALHEAD);
                        zonalHead.setEmployeeCode(sCLIntSalesHierarchy.getZonalHeadCode());

                        Set<TerritoryMasterModel> zhTerriMasters;
                        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(zonalHead.getTerritoryMaster())) {
                            zhTerriMasters = new HashSet<>(zonalHead.getTerritoryMaster());
                        } else {
                            zhTerriMasters = new HashSet<>();
                        }
                        zhTerriMasters.add(territoryMaster);
                        zonalHead.setTerritoryMaster(zhTerriMasters);

                        modelService.save(zonalHead);

                        createTUMForSclUser(sCLIntSalesHierarchy,territoryMaster,zonalHead);
                    }

                    if(StringUtils.isNotEmpty(sCLIntSalesHierarchy.getClusterHeadEmail()) && StringUtils.isNotEmpty(sCLIntSalesHierarchy.getClusterHeadCode()) && StringUtils.isNotEmpty(sCLIntSalesHierarchy.getClusterHeadName())) {


                        clusterHead = sclUserSearchOrCreate(sCLIntSalesHierarchy.getClusterHeadEmail(), sCLIntSalesHierarchy.getClusterHeadName(), sCLIntSalesHierarchy.getClusterHeadEmail());
                        clusterHead.setDefaultB2BUnit(b2bCommerceUnitService.getUnitForUid(DEFAULTUNIT));
                        clusterugSet.add(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.CLUSTERHEAD_GROUP_ID));
                        clusterugSet.add(defaultB2bcustgrp);
                        clusterHead.setGroups(clusterugSet);
                        clusterHead.setUserType(SclUserType.CLUSTERHEAD);
                        clusterHead.setEmployeeCode(sCLIntSalesHierarchy.getClusterHeadCode());

                        Set<TerritoryMasterModel> chTerriMasters;
                        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(clusterHead.getTerritoryMaster())) {
                            chTerriMasters = new HashSet<>(clusterHead.getTerritoryMaster());
                        } else {
                            chTerriMasters = new HashSet<>();
                        }
                        chTerriMasters.add(territoryMaster);
                        clusterHead.setTerritoryMaster(chTerriMasters);

                        modelService.save(clusterHead);

                        createTUMForSclUser(sCLIntSalesHierarchy,territoryMaster,clusterHead);
                    }

                    if(StringUtils.isNotEmpty(sCLIntSalesHierarchy.getStateHeadEmail()) && StringUtils.isNotEmpty(sCLIntSalesHierarchy.getStateHeadCode()) && StringUtils.isNotEmpty(sCLIntSalesHierarchy.getStateHeadName())) {


                        stateHead = sclUserSearchOrCreate(sCLIntSalesHierarchy.getStateHeadEmail(), sCLIntSalesHierarchy.getStateHeadName(), sCLIntSalesHierarchy.getStateHeadEmail());
                        stateHead.setDefaultB2BUnit(b2bCommerceUnitService.getUnitForUid(DEFAULTUNIT));
                        stateugSet.add(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.STATEHEAD_GROUP_ID));
                        stateugSet.add(defaultB2bcustgrp);
                        stateHead.setGroups(stateugSet);
                        stateHead.setUserType(SclUserType.STATEHEAD);
                        stateHead.setEmployeeCode(sCLIntSalesHierarchy.getStateHeadCode());

                        Set<TerritoryMasterModel> shTerriMasters;
                        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(stateHead.getTerritoryMaster())) {
                            shTerriMasters = new HashSet<>(stateHead.getTerritoryMaster());
                        } else {
                            shTerriMasters = new HashSet<>();
                        }
                        shTerriMasters.add(territoryMaster);
                        stateHead.setTerritoryMaster(shTerriMasters);

                        modelService.save(stateHead);

                        createTUMForSclUser(sCLIntSalesHierarchy,territoryMaster,stateHead);
                    }
*/





                    if (dataConstraintDao.findVersionByConstraintName(ENABLE_SALES_HIERARCHY_HIGHER_ROLE).equalsIgnoreCase("true")) {
                    	
                   try {
                       if (StringUtils.isNotEmpty(sCLIntSalesHierarchy.getEmployeeEmail()) && StringUtils.isNotEmpty(sCLIntSalesHierarchy.getEmployeeCode()) && StringUtils.isNotEmpty(sCLIntSalesHierarchy.getEmployeeName())) {
                           sclUser = sclUserSearchOrCreate(sCLIntSalesHierarchy.getEmployeeEmail(), sCLIntSalesHierarchy.getEmployeeName(), sCLIntSalesHierarchy.getEmployeeEmail());

                           if(Objects.nonNull(sclUser.getUserType())){
                               if(sCLIntSalesHierarchy.getDateTo().before(new Date())){
                                   if(sclUser.getUserType().equals(SclUserType.TSO) && BooleanUtils.isTrue(territoryMasterDao.checkValidTSOMapping(sclUser))){

                                   }
                                   else{
                                       sclUser.setUserType(SclUserType.SO);
                                       sclUser.setDefaultB2BUnit(b2bCommerceUnitService.getUnitForUid(DEFAULTUNIT));
                                       sclUserGPSet.add(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SALES_OFFICER_GROUP_ID));
                                       sclUserGPSet.add(defaultB2bcustgrp);
                                       sclUser.setGroups(sclUserGPSet);
                                       sclUser.setEmployeeCode(sCLIntSalesHierarchy.getEmployeeCode());
                                       sclUser.setName(sCLIntSalesHierarchy.getEmployeeName());
                                   }
                               }
                               else {
                                   if(sclUser.getUserType().equals(SclUserType.TSO) && BooleanUtils.isTrue(territoryMasterDao.checkValidTSOMapping(sclUser))){

                                   }
                                   else {

                                   }
                               }
                           }
                           else {
                               sclUser.setUserType(SclUserType.SO);
                               sclUser.setDefaultB2BUnit(b2bCommerceUnitService.getUnitForUid(DEFAULTUNIT));
                               sclUserGPSet.add(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SALES_OFFICER_GROUP_ID));
                               sclUserGPSet.add(defaultB2bcustgrp);
                               sclUser.setGroups(sclUserGPSet);
                               sclUser.setEmployeeCode(sCLIntSalesHierarchy.getEmployeeCode());
                               sclUser.setName(sCLIntSalesHierarchy.getEmployeeName());
                           }
                           /*if(sCLIntSalesHierarchy.getDateTo().after(new Date()) && sCLIntSalesHierarchy.getDateFrom().before(new Date())){
                               if (Objects.isNull(sclUser.getUserType())) {
                                   sclUser.setUserType(SclUserType.SO);
                                   sclUser.setDefaultB2BUnit(b2bCommerceUnitService.getUnitForUid(DEFAULTUNIT));
                                   sclUserGPSet.add(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SALES_OFFICER_GROUP_ID));
                                   sclUserGPSet.add(defaultB2bcustgrp);
                                   sclUser.setGroups(sclUserGPSet);
                                   sclUser.setEmployeeCode(sCLIntSalesHierarchy.getEmployeeCode());

                               }
                           }
                           else{
                               if (Objects.isNull(sclUser.getUserType()) || (Objects.nonNull(sclUser.getUserType())
                                       && (!(sclUser.getUserType().equals(SclUserType.TSO)) || (sclUser.getUserType().equals(SclUserType.TSO) && BooleanUtils.isTrue(territoryMasterDao.checkValidTSOMapping(sclUser))) ) )){
                                   sclUser.setUserType(SclUserType.SO);
                                   sclUser.setDefaultB2BUnit(b2bCommerceUnitService.getUnitForUid(DEFAULTUNIT));
                                   sclUserGPSet.add(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SALES_OFFICER_GROUP_ID));
                                   sclUserGPSet.add(defaultB2bcustgrp);
                                   sclUser.setGroups(sclUserGPSet);
                                   sclUser.setEmployeeCode(sCLIntSalesHierarchy.getEmployeeCode());

                               }
                           }

*/

                           Set<TerritoryMasterModel> empterriMasters;
                           if (org.apache.commons.collections.CollectionUtils.isNotEmpty(sclUser.getTerritoryMaster())) {
                               empterriMasters = new HashSet<>(sclUser.getTerritoryMaster());
                           } else {
                               empterriMasters = new HashSet<>();
                           }

                           empterriMasters.add(territoryMaster);
                           sclUser.setTerritoryMaster(empterriMasters);

                           modelService.save(sclUser);
                           modelService.refresh(sclUser);

                           //  TerritoryUserMapping Creation
                           //  if(null!=sCLIntSalesHierarchy.getDateFrom() && null!=sCLIntSalesHierarchy.getDateTo() && sCLIntSalesHierarchy.getDateFrom().before(new Date()) && sCLIntSalesHierarchy.getDateTo().after(new Date())) {
                           createTUMForSclUser(sCLIntSalesHierarchy, territoryMaster, sclUser);
                           //  }

                       }

                   }
                   catch(RuntimeException e){
                       LOG.info("SO block TerritoryCode:"+  sCLIntSalesHierarchy.getTerritoryCode() + " ExceptionL: "+ e.getMessage());
                   }


                        //  TSM user Creation
                  try {
                      if (StringUtils.isNotEmpty(sCLIntSalesHierarchy.getTsmEmail()) && StringUtils.isNotEmpty(sCLIntSalesHierarchy.getTsmCode()) && StringUtils.isNotEmpty(sCLIntSalesHierarchy.getTsmName())) {

                          tsm = sclUserSearchOrCreate(sCLIntSalesHierarchy.getTsmEmail(), sCLIntSalesHierarchy.getTsmName(), sCLIntSalesHierarchy.getTsmEmail());

                          if(Objects.nonNull(tsm.getUserType())){
                             if(sCLIntSalesHierarchy.getDateTo().before(new Date()) ){
                                 if(tsm.getUserType().equals(SclUserType.TSO) && BooleanUtils.isTrue(territoryMasterDao.checkValidTSOMapping(tsm)) ){

                                 }
                                 else {
                                     tsm.setDefaultB2BUnit(b2bCommerceUnitService.getUnitForUid(DEFAULTUNIT));
                                     tsmugSet.add(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SALES_OFFICER_GROUP_ID));
                                     tsmugSet.add(defaultB2bcustgrp);
                                     tsm.setUserType(SclUserType.SO);
                                     tsm.setGroups(tsmugSet);
                                     tsm.setEmployeeCode(sCLIntSalesHierarchy.getEmployeeCode());
                                     tsm.setName(sCLIntSalesHierarchy.getEmployeeName());
                                 }
                             }
                             else {
                                 if(tsm.getUserType().equals(SclUserType.TSO) && BooleanUtils.isTrue(territoryMasterDao.checkValidTSOMapping(tsm)) ){

                                 }
                                 else {
                                     if(tsm.getUserType().equals(SclUserType.RH)){

                                     }
                                     else {
                                         tsm.setDefaultB2BUnit(b2bCommerceUnitService.getUnitForUid(DEFAULTUNIT));
                                         tsmugSet.add(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.TSM_GROUP_ID));
                                         tsmugSet.add(defaultB2bcustgrp);
                                         tsm.setUserType(SclUserType.TSM);
                                         tsm.setGroups(tsmugSet);
                                         tsm.setEmployeeCode(sCLIntSalesHierarchy.getTsmCode());
                                         tsm.setName(sCLIntSalesHierarchy.getTsmName());
                                     }
                                 }
                             }
                          }
                          else {
                              if(sCLIntSalesHierarchy.getDateTo().before(new Date()) ){
                                  tsm.setDefaultB2BUnit(b2bCommerceUnitService.getUnitForUid(DEFAULTUNIT));
                                  tsmugSet.add(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SALES_OFFICER_GROUP_ID));
                                  tsmugSet.add(defaultB2bcustgrp);
                                  tsm.setUserType(SclUserType.SO);
                                  tsm.setGroups(tsmugSet);
                                  tsm.setEmployeeCode(sCLIntSalesHierarchy.getEmployeeCode());
                                  tsm.setName(sCLIntSalesHierarchy.getEmployeeName());
                              }
                              else {
                                  tsm.setDefaultB2BUnit(b2bCommerceUnitService.getUnitForUid(DEFAULTUNIT));
                                  tsmugSet.add(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.TSM_GROUP_ID));
                                  tsmugSet.add(defaultB2bcustgrp);
                                  tsm.setUserType(SclUserType.TSM);
                                  tsm.setGroups(tsmugSet);
                                  tsm.setEmployeeCode(sCLIntSalesHierarchy.getTsmCode());
                                  tsm.setName(sCLIntSalesHierarchy.getTsmName());

                              }
                          }

                /*          if(sCLIntSalesHierarchy.getDateTo().after(new Date()) && sCLIntSalesHierarchy.getDateFrom().before(new Date())){

                              if (Objects.isNull(tsm.getUserType()) || (Objects.nonNull(tsm.getUserType()) && !tsm.getUserType().equals(SclUserType.RH))) {
                                  tsm.setDefaultB2BUnit(b2bCommerceUnitService.getUnitForUid(DEFAULTUNIT));
                                  tsmugSet.add(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.TSM_GROUP_ID));
                                  tsmugSet.add(defaultB2bcustgrp);
                                  tsm.setUserType(SclUserType.TSM);
                                  tsm.setGroups(tsmugSet);
                                  tsm.setEmployeeCode(sCLIntSalesHierarchy.getTsmCode());

                              }
                          }
                          else{
                              if (Objects.isNull(tsm.getUserType()) || (Objects.nonNull(tsm.getUserType())
                                      && (tsm.getUserType().equals(SclUserType.TSO) && BooleanUtils.isTrue(territoryMasterDao.checkValidTSOMapping(tsm)) ) )) {
                                  tsm.setDefaultB2BUnit(b2bCommerceUnitService.getUnitForUid(DEFAULTUNIT));
                                  tsmugSet.add(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SALES_OFFICER_GROUP_ID));
                                  tsmugSet.add(defaultB2bcustgrp);
                                  tsm.setUserType(SclUserType.SO);
                                  tsm.setGroups(tsmugSet);
                                  tsm.setEmployeeCode(sCLIntSalesHierarchy.getTsmCode());

                              }
                          }*/


                          Set<TerritoryMasterModel> tsmTerriMasters;
                          if (org.apache.commons.collections.CollectionUtils.isNotEmpty(tsm.getTerritoryMaster())) {
                              tsmTerriMasters = new HashSet<>(tsm.getTerritoryMaster());
                          } else {
                              tsmTerriMasters = new HashSet<>();
                          }
                          tsmTerriMasters.add(territoryMaster);
                          tsm.setTerritoryMaster(tsmTerriMasters);

                          modelService.save(tsm);
                          //  if(null!=sCLIntSalesHierarchy.getDateFrom() && null!=sCLIntSalesHierarchy.getDateTo() && sCLIntSalesHierarchy.getDateFrom().before(new Date()) && sCLIntSalesHierarchy.getDateTo().after(new Date())) {
                          createTUMForSclUser(sCLIntSalesHierarchy, territoryMaster, tsm);
                          //  }

                      }

                  }
                   catch(RuntimeException e){
                        LOG.info("TSM block TerritoryCode:"+  sCLIntSalesHierarchy.getTerritoryCode() + " ExceptionL: "+ e.getMessage());
                    }

                   try {
                       if (StringUtils.isNotEmpty(sCLIntSalesHierarchy.getRegionHeadEmail()) && StringUtils.isNotEmpty(sCLIntSalesHierarchy.getRegionHeadCode()) && StringUtils.isNotEmpty(sCLIntSalesHierarchy.getRegionHeadName())) {

                           regionHead = sclUserSearchOrCreate(sCLIntSalesHierarchy.getRegionHeadEmail(), sCLIntSalesHierarchy.getRegionHeadName(), sCLIntSalesHierarchy.getRegionHeadEmail());

                           if(Objects.nonNull(regionHead.getUserType())){
                               if(sCLIntSalesHierarchy.getDateTo().before(new Date())){
                                   if(regionHead.getUserType().equals(SclUserType.TSO) && BooleanUtils.isTrue(territoryMasterDao.checkValidTSOMapping(regionHead))){

                                   }
                                   else {
                                       regionHead.setUserType(SclUserType.SO);
                                       regionHead.setDefaultB2BUnit(b2bCommerceUnitService.getUnitForUid(DEFAULTUNIT));
                                       rhugSet.add(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SALES_OFFICER_GROUP_ID));
                                       rhugSet.add(defaultB2bcustgrp);
                                       regionHead.setGroups(rhugSet);
                                       regionHead.setEmployeeCode(sCLIntSalesHierarchy.getEmployeeCode());
                                       regionHead.setName(sCLIntSalesHierarchy.getEmployeeName());
                                   }
                               }
                               else{
                                   if(regionHead.getUserType().equals(SclUserType.TSO) && BooleanUtils.isTrue(territoryMasterDao.checkValidTSOMapping(regionHead))){

                                   }
                                   else {
                                       regionHead.setUserType(SclUserType.RH);
                                       regionHead.setDefaultB2BUnit(b2bCommerceUnitService.getUnitForUid(DEFAULTUNIT));
                                       rhugSet.add(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RH_GROUP_ID));
                                       rhugSet.add(defaultB2bcustgrp);
                                       regionHead.setGroups(rhugSet);
                                       regionHead.setEmployeeCode(sCLIntSalesHierarchy.getRegionHeadCode());
                                       regionHead.setName(sCLIntSalesHierarchy.getRegionHeadName());
                                   }
                               }
                           }
                           else {
                               if(sCLIntSalesHierarchy.getDateTo().before(new Date())){
                                   regionHead.setUserType(SclUserType.SO);
                                   regionHead.setDefaultB2BUnit(b2bCommerceUnitService.getUnitForUid(DEFAULTUNIT));
                                   rhugSet.add(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SALES_OFFICER_GROUP_ID));
                                   rhugSet.add(defaultB2bcustgrp);
                                   regionHead.setGroups(rhugSet);
                                   regionHead.setEmployeeCode(sCLIntSalesHierarchy.getEmployeeCode());
                                   regionHead.setName(sCLIntSalesHierarchy.getEmployeeName());
                               }
                               else{
                                   regionHead.setUserType(SclUserType.RH);
                                   regionHead.setDefaultB2BUnit(b2bCommerceUnitService.getUnitForUid(DEFAULTUNIT));
                                   rhugSet.add(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RH_GROUP_ID));
                                   rhugSet.add(defaultB2bcustgrp);
                                   regionHead.setGroups(rhugSet);
                                   regionHead.setEmployeeCode(sCLIntSalesHierarchy.getRegionHeadCode());
                                   regionHead.setName(sCLIntSalesHierarchy.getRegionHeadName());
                               }
                           }
               /*            if(sCLIntSalesHierarchy.getDateTo().after(new Date()) && sCLIntSalesHierarchy.getDateFrom().before(new Date())){

                               if (Objects.isNull(regionHead.getUserType()) || (Objects.nonNull(regionHead.getUserType()) && regionHead.getUserType().equals(SclUserType.TSO) && BooleanUtils.isTrue(territoryMasterDao.checkValidTSOMapping(regionHead)))) {
                                   regionHead.setUserType(SclUserType.RH);
                                   regionHead.setDefaultB2BUnit(b2bCommerceUnitService.getUnitForUid(DEFAULTUNIT));
                                   rhugSet.add(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RH_GROUP_ID));
                                   rhugSet.add(defaultB2bcustgrp);
                                   regionHead.setGroups(rhugSet);
                               }
                           }
                           else{
                               if (Objects.isNull(regionHead.getUserType()) || (Objects.nonNull(regionHead.getUserType()) && regionHead.getUserType().equals(SclUserType.TSO) && BooleanUtils.isTrue(territoryMasterDao.checkValidTSOMapping(regionHead))) ) {
                                   regionHead.setUserType(SclUserType.SO);
                                   regionHead.setDefaultB2BUnit(b2bCommerceUnitService.getUnitForUid(DEFAULTUNIT));
                                   rhugSet.add(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SALES_OFFICER_GROUP_ID));
                                   rhugSet.add(defaultB2bcustgrp);
                                   regionHead.setGroups(rhugSet);
                                   regionHead.setEmployeeCode(sCLIntSalesHierarchy.getRegionHeadCode());

                               }
                           }

*/
                           Set<TerritoryMasterModel> rhTerriMasters;
                           if (org.apache.commons.collections.CollectionUtils.isNotEmpty(regionHead.getTerritoryMaster())) {
                               rhTerriMasters = new HashSet<>(regionHead.getTerritoryMaster());
                           } else {
                               rhTerriMasters = new HashSet<>();
                           }
                           rhTerriMasters.add(territoryMaster);
                           regionHead.setTerritoryMaster(rhTerriMasters);

                           modelService.save(regionHead);
                           //  if(null!=sCLIntSalesHierarchy.getDateFrom() && null!=sCLIntSalesHierarchy.getDateTo() && sCLIntSalesHierarchy.getDateFrom().before(new Date()) && sCLIntSalesHierarchy.getDateTo().after(new Date())) {
                           createTUMForSclUser(sCLIntSalesHierarchy, territoryMaster, regionHead);
                           //  }
                       }
                   }
                   catch(RuntimeException e){
                        LOG.info("RH block TerritoryCode:"+  sCLIntSalesHierarchy.getTerritoryCode() + " ExceptionL: "+ e.getMessage());
                    }
                   	
                   }
                   else {
                       try {
                           if (StringUtils.isNotEmpty(sCLIntSalesHierarchy.getRegionHeadEmail()) && StringUtils.isNotEmpty(sCLIntSalesHierarchy.getRegionHeadCode()) && StringUtils.isNotEmpty(sCLIntSalesHierarchy.getRegionHeadName())) {

                               regionHead = sclUserSearchOrCreate(sCLIntSalesHierarchy.getRegionHeadEmail(), sCLIntSalesHierarchy.getRegionHeadName(), sCLIntSalesHierarchy.getRegionHeadEmail());

                               if ((Objects.isNull(regionHead.getUserType()) || (Objects.nonNull(regionHead.getUserType())
                                       && !(regionHead.getUserType().equals(SclUserType.SO) || regionHead.getUserType().equals(SclUserType.TSM)))) && sCLIntSalesHierarchy.getDateTo().after(new Date()) && sCLIntSalesHierarchy.getDateFrom().before(new Date())) {
                                   regionHead.setUserType(SclUserType.RH);
                                   regionHead.setDefaultB2BUnit(b2bCommerceUnitService.getUnitForUid(DEFAULTUNIT));
                                   rhugSet.add(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RH_GROUP_ID));
                                   rhugSet.add(defaultB2bcustgrp);
                                   regionHead.setGroups(rhugSet);
                                   regionHead.setEmployeeCode(sCLIntSalesHierarchy.getRegionHeadCode());
                               }

                               Set<TerritoryMasterModel> rhTerriMasters;
                               if (org.apache.commons.collections.CollectionUtils.isNotEmpty(regionHead.getTerritoryMaster())) {
                                   rhTerriMasters = new HashSet<>(regionHead.getTerritoryMaster());
                               } else {
                                   rhTerriMasters = new HashSet<>();
                               }
                               rhTerriMasters.add(territoryMaster);
                               regionHead.setTerritoryMaster(rhTerriMasters);

                               modelService.save(regionHead);

                               //    createTUMForSclUser(sCLIntSalesHierarchy,territoryMaster,regionHead);
                           }
                       }
                       catch(RuntimeException e){
                           LOG.info("RH block TerritoryCode:"+  sCLIntSalesHierarchy.getTerritoryCode() + " ExceptionL: "+ e.getMessage());
                       }

                    try {
                        //  TSM user Creation
                        if (StringUtils.isNotEmpty(sCLIntSalesHierarchy.getTsmEmail()) && StringUtils.isNotEmpty(sCLIntSalesHierarchy.getTsmCode()) && StringUtils.isNotEmpty(sCLIntSalesHierarchy.getTsmName())) {

                            tsm = sclUserSearchOrCreate(sCLIntSalesHierarchy.getTsmEmail(), sCLIntSalesHierarchy.getTsmName(), sCLIntSalesHierarchy.getTsmEmail());

                            if ((Objects.isNull(tsm.getUserType()) || (Objects.nonNull(tsm.getUserType()) && !tsm.getUserType().equals(SclUserType.SO))) && sCLIntSalesHierarchy.getDateTo().after(new Date()) && sCLIntSalesHierarchy.getDateFrom().before(new Date())) {
                                tsm.setDefaultB2BUnit(b2bCommerceUnitService.getUnitForUid(DEFAULTUNIT));
                                tsmugSet.add(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.TSM_GROUP_ID));
                                tsmugSet.add(defaultB2bcustgrp);
                                tsm.setUserType(SclUserType.TSM);
                                tsm.setGroups(tsmugSet);
                                tsm.setEmployeeCode(sCLIntSalesHierarchy.getTsmCode());
                            }

                            Set<TerritoryMasterModel> tsmTerriMasters;
                            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(tsm.getTerritoryMaster())) {
                                tsmTerriMasters = new HashSet<>(tsm.getTerritoryMaster());
                            } else {
                                tsmTerriMasters = new HashSet<>();
                            }
                            tsmTerriMasters.add(territoryMaster);
                            tsm.setTerritoryMaster(tsmTerriMasters);

                            modelService.save(tsm);

                            //     createTUMForSclUser(sCLIntSalesHierarchy,territoryMaster,tsm);
                        }
                    }
                    catch(RuntimeException e){
                        LOG.info("TSM block TerritoryCode:"+  sCLIntSalesHierarchy.getTerritoryCode() + " ExceptionL: "+ e.getMessage());
                    }

                       try {
                      if (StringUtils.isNotEmpty(sCLIntSalesHierarchy.getEmployeeEmail()) && StringUtils.isNotEmpty(sCLIntSalesHierarchy.getEmployeeCode()) && StringUtils.isNotEmpty(sCLIntSalesHierarchy.getEmployeeName())) {
                          sclUser = sclUserSearchOrCreate(sCLIntSalesHierarchy.getEmployeeEmail(), sCLIntSalesHierarchy.getEmployeeName(), sCLIntSalesHierarchy.getEmployeeEmail());

                          if ((Objects.isNull(sclUser.getUserType()) || (Objects.nonNull(sclUser.getUserType())
                                  && (sclUser.getUserType().equals(SclUserType.SO) || sclUser.getUserType().equals(SclUserType.RH) || sclUser.getUserType().equals(SclUserType.TSM)))) && sCLIntSalesHierarchy.getDateTo().after(new Date()) && sCLIntSalesHierarchy.getDateFrom().before(new Date())) {

                              sclUserGPSet.add(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SALES_OFFICER_GROUP_ID));
                              sclUserGPSet.add(defaultB2bcustgrp);
                              sclUser.setUserType(SclUserType.SO);
                              sclUser.setEmployeeCode(sCLIntSalesHierarchy.getEmployeeCode());
                              sclUser.setGroups(sclUserGPSet);
                              sclUser.setDefaultB2BUnit(b2bCommerceUnitService.getUnitForUid(DEFAULTUNIT));
                          }

                          Set<TerritoryMasterModel> empterriMasters;
                          if (org.apache.commons.collections.CollectionUtils.isNotEmpty(sclUser.getTerritoryMaster())) {
                              empterriMasters = new HashSet<>(sclUser.getTerritoryMaster());
                          } else {
                              empterriMasters = new HashSet<>();
                          }

                          empterriMasters.add(territoryMaster);
                          sclUser.setTerritoryMaster(empterriMasters);

                          modelService.save(sclUser);
                          modelService.refresh(sclUser);

                          createTUMForSclUser(sCLIntSalesHierarchy, territoryMaster, sclUser);

                      }
                     }
                       catch(RuntimeException e){
                      LOG.info("SO block TerritoryCode:"+  sCLIntSalesHierarchy.getTerritoryCode() + " ExceptionL: "+ e.getMessage());
                  }

                    }

                }catch (RuntimeException e){
                    LOG.info("SclInternalSalesHeirarchyCreationJob TerritoryCode:"+  sCLIntSalesHierarchy.getTerritoryCode() + " ExceptionL: "+ e.getMessage());
                    e.printStackTrace();
                }

                LOG.debug("The persistence hook SclInternalSalesHeirarchyCreationJob is executed for territory id "+ sCLIntSalesHierarchy.getTerritoryCode());


            }
        }

        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }

    @Override
	public boolean isAbortable()
	{
		return true;
	}

    public SclUserModel sclUserSearchOrCreate(String uid, String name, String email){
        SclUserModel sclUser=null;
        try
        {
            sclUser= (SclUserModel) userService.getUserForUID(uid.toLowerCase());
        }
        catch (final UnknownIdentifierException | ClassMismatchException e) {
            LOG.error("SclInternalSalesHeirarchyCreationJob :Failed to get user with code : "+ uid);
            sclUser= modelService.create(SclUserModel.class);
            sclUser.setUid(uid);
            sclUser.setName(name);
            sclUser.setEmail(email);
            sclUser.setDefaultB2BUnit(b2bCommerceUnitService.getUnitForUid(DEFAULTUNIT));

            modelService.save(sclUser);
            modelService.refresh(sclUser);
            String prodEnv = configurationService.getConfiguration().getString("prod.environment");
            if(StringUtils.isNotEmpty(prodEnv) && prodEnv.equals("true")) {
                sclUser.setPassword(dataConstraintDao.findPasswordByConstraintName(FIND_SCLUSES_PASSword));
                sclUser.setLoginDisabled(false);
                sclUser.setActive(true);
            }
            else {
                sclUser.setPassword(dataConstraintDao.findPasswordByConstraintName(FIND_SCLUSES_PASSword_QAENV));
                sclUser.setLoginDisabled(false);
                sclUser.setActive(true);
            }
        }
        sclUser.setLoginDisabled(false);
        sclUser.setActive(true);

        return sclUser;

    }

    public void createTUMForSclUser(SCLIntSalesHierarchyModel sCLIntSalesHierarchy, TerritoryMasterModel territoryMaster  ,SclUserModel sclUser){
        String pattern = "MM/dd/yyyy";
        DateFormat df = new SimpleDateFormat(pattern);
        Date today = Calendar.getInstance().getTime();
        String todayAsString = df.format(today);

        LOG.info("SclIntSalesHierarchyPostCreationJob createTUMForSclUser method started for "+ sclUser.getUid() + " & territoryCode" + territoryMaster.getTerritoryCode());

        TerritoryUserMappingModel territoryUserMapping = orderRequisitionDao.getTerritoryUserMapping(sCLIntSalesHierarchy.getTerritoryCode(), sclUser.getUid());
try {
    if (Objects.isNull(territoryUserMapping)) {
        territoryUserMapping = new TerritoryUserMappingModel();
        territoryUserMapping.setSclUser(sclUser);
        territoryUserMapping.setTerritoryMaster(territoryMaster);
        territoryUserMapping.setValidFrom(sCLIntSalesHierarchy.getDateFrom());
        territoryUserMapping.setValidTo(sCLIntSalesHierarchy.getDateTo());
        territoryUserMapping.setBrand(cmsAdminSiteService.getSiteForId(SclCoreConstants.SCL_SITE));
    } else {
        territoryUserMapping.setValidFrom(sCLIntSalesHierarchy.getDateFrom());
        territoryUserMapping.setValidTo(sCLIntSalesHierarchy.getDateTo());
        territoryUserMapping.setBrand(cmsAdminSiteService.getSiteForId(SclCoreConstants.SCL_SITE));
    }

    modelService.save(territoryUserMapping);

}
catch (RuntimeException e){
    LOG.info("SclIntSalesHierarchyPostCreationJob TUM already found for "+ sclUser.getUid() + " & territoryCode" + territoryMaster.getTerritoryCode());

}


         // populating userSubArea mapping

        List<SclCustomerModel> dealerList = dealerDao.getDealerFromTerritoryCode(territoryMaster);
        if(org.apache.commons.collections.CollectionUtils.isNotEmpty(dealerList)) {
            for (SclCustomerModel dealer : dealerList) {
                UserSubAreaMappingModel userSubAreaMappingModel=null;
                try {
                    userSubAreaMappingModel= sclDealerRetailerDao.getUserSubAreaMappingModelModel(sclUser,dealer.getSubAreaMaster(),dealer.getDistrict(),dealer.getState(), dealer.getTaluka());

                    if (Objects.nonNull(dealer.getTerritoryCode()) && Objects.nonNull(sclUser)
                            && Objects.isNull(userSubAreaMappingModel) && Objects.nonNull(dealer.getSubAreaMaster()) && Objects.nonNull(dealer.getDistrict()) && Objects.nonNull(dealer.getState())) {
                        userSubAreaMappingModel = new UserSubAreaMappingModel();
                        userSubAreaMappingModel.setSclUser(sclUser);
                        userSubAreaMappingModel.setSubAreaMaster(dealer.getSubAreaMaster());
                        userSubAreaMappingModel.setDistrict(dealer.getDistrict());
                        userSubAreaMappingModel.setBrand(cmsAdminSiteService.getSiteForId(SclCoreConstants.SCL_SITE));
                        userSubAreaMappingModel.setState(dealer.getState());
                        userSubAreaMappingModel.setSubArea(dealer.getTaluka());
                        userSubAreaMappingModel.setUpdatedByJob(todayAsString);
                        userSubAreaMappingModel.setCreatedFromCRMorERP(CreatedFromCRMorERP.S4HANA);
                        userSubAreaMappingModel.setRegionMaster(Objects.nonNull(dealer.getRegionMaster()) ? dealer.getRegionMaster() : null);
                        modelService.save(userSubAreaMappingModel);

                        sclUser.setUserSubAreaMapping(userSubAreaMappingModel);
                        modelService.save(sclUser);
                        modelService.refresh(sclUser);
                    }
                    else {
                        userSubAreaMappingModel.setUpdatedByJob(todayAsString);
                        userSubAreaMappingModel.setCreatedFromCRMorERP(CreatedFromCRMorERP.S4HANA);
                        modelService.save(userSubAreaMappingModel);

                    }

                    // userSubAreaMappingRetailer for retailers under dealer
                    List<SclCustomerModel> retailerList = sclDealerRetailerDao.getRetailerMappingListForDealer(dealer);
                    if (org.apache.commons.collections.CollectionUtils.isNotEmpty(retailerList)) {
                        for (SclCustomerModel retailer : retailerList) {
                            UserSubAreaMappingModel userSubAreaMappingRetailer =sclDealerRetailerDao.getUserSubAreaMappingModelModel(sclUser, retailer.getSubAreaMaster(), retailer.getDistrict(), retailer.getState(), retailer.getTaluka());
                            try {
                                if (Objects.nonNull(retailer.getSubAreaMaster()) && org.apache.commons.lang3.StringUtils.isNotEmpty(retailer.getDistrict()) && org.apache.commons.lang3.StringUtils.isNotEmpty(retailer.getState()) && Objects.isNull(userSubAreaMappingRetailer)) {
                                   userSubAreaMappingRetailer=new UserSubAreaMappingModel();
                                    userSubAreaMappingRetailer.setSclUser(sclUser);
                                    userSubAreaMappingRetailer.setSubAreaMaster(retailer.getSubAreaMaster());
                                    userSubAreaMappingRetailer.setDistrict(retailer.getDistrict());
                                    userSubAreaMappingRetailer.setBrand(cmsAdminSiteService.getSiteForId(SclCoreConstants.SCL_SITE));
                                    userSubAreaMappingRetailer.setState(retailer.getState());
                                    userSubAreaMappingRetailer.setSubArea(retailer.getTaluka());
                                    userSubAreaMappingRetailer.setUpdatedByJob(todayAsString);
                                    userSubAreaMappingRetailer.setCreatedFromCRMorERP(CreatedFromCRMorERP.S4HANA);
                                    userSubAreaMappingRetailer.setRegionMaster(Objects.nonNull(retailer.getRegionMaster()) ? retailer.getRegionMaster() : null);
                                    modelService.save(userSubAreaMappingRetailer);
                                }
                                else {
                                    userSubAreaMappingRetailer.setUpdatedByJob(todayAsString);
                                    userSubAreaMappingRetailer.setCreatedFromCRMorERP(CreatedFromCRMorERP.S4HANA);
                                    modelService.save(userSubAreaMappingRetailer);
                                }
                            }
                            catch (ModelSavingException e) {
                                userSubAreaMappingRetailer= flexibleSearchService.getModelByExample(userSubAreaMappingRetailer);
                                userSubAreaMappingRetailer.setUpdatedByJob(todayAsString);
                            modelService.save(userSubAreaMappingRetailer);
                            LOG.info(String.format("createTUMForSclUser UserSubAreaMappingModel is already present for User  %s and  district :- %s", sclUser.getUid(), dealer.getDistrict()));
                        }
                            catch (RuntimeException e){
                                LOG.info(String.format("retailer Runtime UserSubAreaMappingModel is already present for User  %s and  district :- %s", sclUser.getUid(), dealer.getDistrict()));

                            }
                        }
                    }

                } catch (ModelSavingException e) {
                    userSubAreaMappingModel=  flexibleSearchService.getModelByExample(userSubAreaMappingModel);
                    userSubAreaMappingModel.setUpdatedByJob(todayAsString);
                    modelService.save(userSubAreaMappingModel);
                    LOG.info(String.format("createTUMForSclUser UserSubAreaMappingModel is already present for User  %s and  district :- %s", sclUser.getUid(), dealer.getDistrict()));
                }
                catch (RuntimeException e){
                    LOG.info(String.format("Runtime UserSubAreaMappingModel is already present for User  %s and  district :- %s", sclUser.getUid(), dealer.getDistrict()));

                }
            }
        }
    }

}
