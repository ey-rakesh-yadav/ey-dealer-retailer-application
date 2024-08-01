package com.scl.core.job;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.scl.core.enums.CustomerGrouping;
import com.scl.core.jalo.GeographicalMaster;
import com.scl.core.model.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.CounterRouteMappingDao;
import com.scl.core.dao.DataConstraintDao;
import com.scl.core.dao.DealerDao;
import com.scl.core.dao.DjpRouteDao;
import com.scl.core.dao.SclDealerRetailerDao;
import com.scl.core.dao.TerritoryManagementDao;
import com.scl.core.dao.TerritoryMasterDao;
import com.scl.core.enums.CounterType;
import com.scl.core.region.dao.GeographicalRegionDao;
import com.scl.core.services.SclDealerRetailerService;

import de.hybris.platform.b2b.company.B2BCommerceUnitService;
import de.hybris.platform.cms2.servicelayer.services.admin.CMSAdminSiteService;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.exceptions.ClassMismatchException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;

public class SclCustomerPostPersistJob extends AbstractJobPerformable<CronJobModel> {


    public static final String FIND_SCLDEALER_PASSword = "FIND_SCLDEALER_PASSword";
    public static final String FIND_SCLRETAILER_PASSword = "FIND_SCLRETAILER_PASSword";

    public static final String FIND_SCLCUST_POST_JOBDAYS = "FIND_SCLCUST_POST_JOBDAYS";

    public static final java.lang.String SC_LJ_OB_POSTLOGIC_DRMENABLE = "SCLjOB_POSTLOGIC_DRMENABLE";
    public static final java.lang.String SC_LJ_OB_POSTLOGIC_USERSUBAREAENABLE = "SCLjOB_POSTLOGIC_USERSUBAREAENABLE";
    public static final java.lang.String SC_LJ_OB_POSTLOGIC_CUSTOMERSUBAREAENABLE = "SCLjOB_POSTLOGIC_CUSTOMERSUBAREAENABLE";

    public static final java.lang.String SC_LJ_OB_POSTLOGIC_COUTERROUTEENABLE = "SCLjOB_POSTLOGIC_COUTERROUTEENABLE";

    @Resource(name = "modelService")
    private ModelService modelService;


    @Resource(name = "b2bCommerceUnitService")
    private B2BCommerceUnitService b2bCommerceUnitService;

    public static final String DEFAULTUNIT="001000";

    @Resource
    private UserService userService;

    @Resource(name = "sclDealerRetailerService")
    private SclDealerRetailerService sclDealerRetailerService;

    @Resource
    private DjpRouteDao djpRouteDao;

    @Resource(name="sclCounterRouteMappingDao")
    CounterRouteMappingDao counterRouteMappingDao;

    @Resource
    TerritoryManagementDao territoryManagementDao;

    @Resource
    DataConstraintDao dataConstraintDao;
    

    @Resource
    private GeographicalRegionDao geographicalRegionDao;

    @Resource
    TerritoryMasterDao territoryMasterDao;

    @Resource
    private CMSAdminSiteService cmsAdminSiteService;

    @Resource
    private DealerDao dealerDao;



    @Resource
    SclDealerRetailerDao sclDealerRetailerDao;
    private static final Logger LOG = Logger.getLogger(SclCustomerPostPersistJob.class);

    @Override
    public PerformResult perform(CronJobModel arg0) {


         Date xOldDate=null;
        Integer lastXDays = dataConstraintDao.findDaysByConstraintName(FIND_SCLCUST_POST_JOBDAYS);


        if(lastXDays>0) {
            LocalDate currentDate = LocalDate.now();
            LocalDate last6MonthsDate = currentDate.minusDays(lastXDays);

            xOldDate = Date.from(last6MonthsDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        }

        List<SclCustomerModel> sclCustomerModelList =sclDealerRetailerService.getDealerRetailerList( xOldDate);
        if(CollectionUtils.isEmpty(sclCustomerModelList) ){
            LOG.info("There are no  SclCustomer  models");
            return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
        }
        else {
            for(SclCustomerModel sclCust: sclCustomerModelList) {

                String routeID;
                RouteMasterModel routemaster;
                CounterRouteMappingModel counterRouteMappingModel;
                String stateCode=null;


                try {


                    if (org.apache.commons.collections.CollectionUtils.isNotEmpty(sclCust.getAddresses())) {
                        for (AddressModel add : sclCust.getAddresses()) {
                            if(StringUtils.isNotEmpty(add.getTransportationZone())){
                                add.setGeographicalMaster(geographicalRegionDao.getGeographyMasterForTransZone(add.getTransportationZone()));
                            }
                            if (StringUtils.isNotEmpty(add.getSapAddressUsage()) && add.getSapAddressUsage().equalsIgnoreCase("DE")) {
                                sclCust.setContactNumber(StringUtils.isNotEmpty(add.getCellphone()) ? add.getCellphone() :null);
                                sclCust.setMobileNumber(StringUtils.isNotEmpty(add.getCellphone()) ? add.getCellphone() :null);
                                if(Objects.nonNull(add.getGeographicalMaster()) && StringUtils.isNotEmpty(add.getGeographicalMaster().getState()) && StringUtils.isNotEmpty(add.getGeographicalMaster().getDistrict())) {
                                    sclCust.setState(add.getGeographicalMaster().getState());
                                    sclCust.setDistrict(add.getGeographicalMaster().getDistrict());
                                    sclCust.setTaluka(StringUtils.isNotEmpty(add.getGeographicalMaster().getTaluka()) ? add.getGeographicalMaster().getTaluka() : StringUtils.EMPTY);
                                    sclCust.setRegionMaster(territoryMasterDao.getRegionMaster(add.getGeographicalMaster().getRegion()));
                                    sclCust.setDistrictMaster(territoryMasterDao.getDistrictMaster(add.getGeographicalMaster().getDistrictCode()));
                                }
                            }
                            if (Objects.nonNull(add.getGeographicalMaster()) && StringUtils.isNotEmpty(add.getGeographicalMaster().getState()) && StringUtils.isNotEmpty(add.getGeographicalMaster().getDistrict())) {
                                add.setState(add.getGeographicalMaster().getState());
                                add.setDistrict(add.getGeographicalMaster().getDistrict());
                                add.setTaluka(StringUtils.isNotEmpty(add.getGeographicalMaster().getTaluka())? add.getGeographicalMaster().getTaluka():StringUtils.EMPTY);
                                add.setPostalcode(StringUtils.isNotEmpty(add.getGeographicalMaster().getPincode())? add.getGeographicalMaster().getPincode(): StringUtils.EMPTY);
                            }
                            modelService.save(add);
                            modelService.refresh(add);
                        }
                    }

                    SubAreaMasterModel subAreaMasterModel=  territoryManagementDao.getTerritoryByDistrictAndTaluka(sclCust.getDistrict(), sclCust.getTaluka());

                    if(Objects.nonNull(subAreaMasterModel)){
                        sclCust.setSubAreaMaster(subAreaMasterModel);
                    }

                    modelService.save(sclCust);
                    modelService.refresh(sclCust);



                    //DealerRetailerMapping population
                    if (Objects.nonNull(sclCust.getCustomerGrouping()) && sclCust.getCustomerGrouping().getCode().equalsIgnoreCase("ZDOM") && org.apache.commons.collections.CollectionUtils.isNotEmpty(sclCust.getAddresses()) && dataConstraintDao.findVersionByConstraintName(SC_LJ_OB_POSTLOGIC_DRMENABLE).equalsIgnoreCase("true")) {


                        for (AddressModel add : sclCust.getAddresses()) {

                            if (StringUtils.isNotEmpty(add.getSapAddressUsage()) && add.getSapAddressUsage().equalsIgnoreCase(SclCoreConstants.SHIPTOTYPE)) {
                                DealerRetailerMappingModel drm = new DealerRetailerMappingModel();
                                drm.setDealer(sclCust);
                                drm.setShipTo(add);
                                drm.setLastUsed(new Date());
                                GeographicalMasterModel validTZone=geographicalRegionDao.getGeographyMasterForTransZone(add.getTransportationZone());
                                if(Objects.nonNull(validTZone))  {
                                    drm.setDistrict((StringUtils.isNotEmpty(validTZone.getDistrict())) ? validTZone.getDistrict() : StringUtils.EMPTY);
                                    drm.setState((StringUtils.isNotEmpty(validTZone.getState())) ? validTZone.getState() : StringUtils.EMPTY);
                               
                                    drm.setPinCode((StringUtils.isNotEmpty(validTZone.getPincode())) ? validTZone.getPincode() : StringUtils.EMPTY);
                                    drm.setErpCity((StringUtils.isNotEmpty(validTZone.getErpCity())) ? validTZone.getErpCity() : StringUtils.EMPTY);
                                    drm.setTaluka((StringUtils.isNotEmpty(validTZone.getTaluka())) ? validTZone.getTaluka() : StringUtils.EMPTY);
                                }
                                else {
                                    drm.setDistrict(StringUtils.EMPTY);
                                    drm.setState(StringUtils.EMPTY);
                                    drm.setPinCode(StringUtils.EMPTY);
                                    drm.setErpCity( StringUtils.EMPTY);
                                    drm.setTaluka(StringUtils.EMPTY);

                                }
                                drm.setPartnerFunctionId((StringUtils.isNotEmpty(add.getPartnerFunctionId())) ? add.getPartnerFunctionId() : StringUtils.EMPTY);

                                DealerRetailerMappingModel drmModel = sclDealerRetailerDao.getDealerRetailerMappingModel(sclCust, add.getPk().toString(), null);

                                if (Objects.isNull(drmModel)) {
                                    modelService.save(drm);
                                } else {
                                    if(Objects.nonNull(validTZone)) {
                                        drmModel.setDistrict((StringUtils.isNotEmpty(validTZone.getDistrict())) ? validTZone.getDistrict() : StringUtils.EMPTY);
                                        drmModel.setState((StringUtils.isNotEmpty(validTZone.getState())) ? validTZone.getState() : StringUtils.EMPTY);
                                        drmModel.setPinCode((StringUtils.isNotEmpty(validTZone.getPincode())) ? validTZone.getPincode() : StringUtils.EMPTY);
                                        drmModel.setErpCity((StringUtils.isNotEmpty(validTZone.getErpCity())) ? validTZone.getErpCity() : StringUtils.EMPTY);
                                        drmModel.setTaluka((StringUtils.isNotEmpty(validTZone.getTaluka())) ? validTZone.getTaluka() : StringUtils.EMPTY);
                                    }
                                    else {
                                        drmModel.setDistrict(StringUtils.EMPTY);
                                        drmModel.setState(StringUtils.EMPTY);
                                        drmModel.setPinCode(StringUtils.EMPTY);
                                        drmModel.setErpCity( StringUtils.EMPTY);
                                        drmModel.setTaluka(StringUtils.EMPTY);
                                    }
                                    drmModel.setPartnerFunctionId((StringUtils.isNotEmpty(add.getPartnerFunctionId())) ? add.getPartnerFunctionId() : StringUtils.EMPTY);

                                    modelService.save(drmModel);
                                }

                            }
                        }

                        List<AddressModel> retailAddress = sclCust.getAddresses().stream().filter(add -> (StringUtils.isNotEmpty(add.getSapAddressUsage()) && add.getSapAddressUsage().equalsIgnoreCase("RT"))).collect(Collectors.toList());
                        for (AddressModel ret : retailAddress) {
                            SclCustomerModel retailer = sclCustSearchOrCreate(ret.getPartnerFunctionId(), (null != ret.getAccountName()) ? ret.getAccountName() : null, (null != ret.getEmail()) ? ret.getEmail() : sclCust.getEmail());
                            if (Objects.nonNull(retailer) && Objects.nonNull(retailer.getCustomerGrouping()) && retailer.getCustomerGrouping().equals(CustomerGrouping.ZRET)) {
                                List<AddressModel> retailShipTo = retailer.getAddresses().stream().filter(retailrAdd -> (StringUtils.isNotEmpty(retailrAdd.getSapAddressUsage()) && retailrAdd.getSapAddressUsage().equalsIgnoreCase("WE"))).collect(Collectors.toList());
                                List<String> retailShipToID = new ArrayList<>();
                                for (AddressModel r : retailShipTo) {
                                    retailShipToID.add(r.getPartnerFunctionId());
                                }
                                List<DealerRetailerMappingModel> dealerRetailerMappingModels = sclDealerRetailerService.getDealerRetailerMappingListForDealer(sclCust);// for current dealer for retail null
                                for (DealerRetailerMappingModel drm : dealerRetailerMappingModels) {
                                    if (retailShipToID.contains(drm.getShipTo().getPartnerFunctionId()) || drm.getShipTo().getPartnerFunctionId().equalsIgnoreCase(retailer.getUid())) {
                                        drm.setRetailer(retailer);
                                        modelService.save(drm);
                                    }
                                }

                            }
                        }



                    }

                    if(dataConstraintDao.findVersionByConstraintName(SC_LJ_OB_POSTLOGIC_USERSUBAREAENABLE).equalsIgnoreCase("true") && Objects.nonNull(sclCust.getTerritoryCode())) {
                        //Populating userSubAreaMapping table After SubAreaMaster
                        createUserSubAreaMapping(sclCust);
                    }

                    //Route Master
                    stateCode=sclCust.getAddresses().stream().filter(add -> (StringUtils.isNotEmpty(add.getSapAddressUsage()) && add.getSapAddressUsage().equalsIgnoreCase("DE"))).collect(Collectors.toList()).get(0).getGeographicalMaster().getStateCode();

                    if(dataConstraintDao.findVersionByConstraintName(SC_LJ_OB_POSTLOGIC_COUTERROUTEENABLE).equalsIgnoreCase("true") && StringUtils.isNotEmpty(sclCust.getTaluka()) && StringUtils.isNotEmpty(stateCode) && StringUtils.isNotEmpty(sclCust.getDistrict()) && (sclCust.getCounterType().equals(CounterType.DEALER) || sclCust.getCounterType().equals(CounterType.RETAILER))){

                        routeID=sclCust.getTaluka().concat(SclCoreConstants.ALL_SCL_).concat(stateCode).concat("_").concat(sclCust.getDistrict());

                        routemaster= djpRouteDao.findRouteById(routeID);
                        if(Objects.nonNull(routemaster)){

                            //Counter Route Master
                            counterRouteMappingModel= counterRouteMappingDao.findCounterByCustomerId(sclCust.getUid());

                            if(Objects.isNull(counterRouteMappingModel)){
                                counterRouteMappingModel=new CounterRouteMappingModel();
                                counterRouteMappingModel.setCounterCode(sclCust.getUid());
                            }
                            counterRouteMappingModel.setRouteName(routemaster.getRouteName());
                            counterRouteMappingModel.setTaluka(sclCust.getTaluka());
                            counterRouteMappingModel.setDistrict(sclCust.getDistrict());
                            counterRouteMappingModel.setState(sclCust.getState());
                            counterRouteMappingModel.setBrand(getCmsAdminSiteService().getSiteForId(SclCoreConstants.SCL_SITE));
                            counterRouteMappingModel.setRoute(routemaster.getRouteId());

                            modelService.save(counterRouteMappingModel);
                        }

                    }

                    // SubareaMaster & CustomerSubAreaMapping population
                    if(dataConstraintDao.findVersionByConstraintName(SC_LJ_OB_POSTLOGIC_CUSTOMERSUBAREAENABLE).equalsIgnoreCase("true") && Objects.nonNull(sclCust.getSubAreaMaster()) && (sclCust.getCounterType().equals(CounterType.DEALER) || sclCust.getCounterType().equals(CounterType.RETAILER)) ){



                        CustomerSubAreaMappingModel customerSubAreaMappingModel= sclDealerRetailerDao.getCustomerSubAreaMapping(sclCust.getUid(),sclCust.getSubAreaMaster(), sclCust.getState(), sclCust.getTaluka(), sclCust.getDistrict());

                        if(Objects.isNull(customerSubAreaMappingModel)){
                            customerSubAreaMappingModel= new CustomerSubAreaMappingModel();
                            customerSubAreaMappingModel.setSclCustomer(sclCust);
                        }

                        customerSubAreaMappingModel.setSubAreaMaster(sclCust.getSubAreaMaster());
                        customerSubAreaMappingModel.setDistrict(sclCust.getSubAreaMaster().getDistrict());
                        customerSubAreaMappingModel.setIsActive(true);
                        customerSubAreaMappingModel.setState(sclCust.getState());
                        customerSubAreaMappingModel.setSubArea(sclCust.getTaluka());
                        customerSubAreaMappingModel.setBrand(getCmsAdminSiteService().getSiteForId(SclCoreConstants.SCL_SITE));
                        customerSubAreaMappingModel.setCounterType(sclCust.getCounterType().getCode());
                        modelService.save(customerSubAreaMappingModel);

                    }



                }catch (RuntimeException e){
                    LOG.info("SclCustomerPostPersistJob customer:"+  sclCust.getUid() + " ExceptionL: "+ e.getMessage());
                    e.printStackTrace();
                }

            }
        }

        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }
    
    @Override
	public boolean isAbortable()
	{
		return true;
	}


    private void createUserSubAreaMapping(SclCustomerModel dealer)
    {

        String pattern = "MM/dd/yyyy";
        DateFormat df = new SimpleDateFormat(pattern);
        Date today = Calendar.getInstance().getTime();
        String todayAsString = df.format(today);

       List<SclUserModel> sclUsers= dealerDao.getSclUserUsingTerritoryusermap(dealer.getTerritoryCode());

        // userSubAreaMappingRetailer for retailers under dealer
        List<SclCustomerModel> retailerList=sclDealerRetailerDao.getRetailerMappingListForDealer(dealer);

        if(CollectionUtils.isNotEmpty(sclUsers)){
           for(SclUserModel sclUser: sclUsers){


               if(Objects.nonNull(dealer.getTerritoryCode()) && Objects.nonNull(sclUser)
                       && Objects.nonNull(dealer.getSubAreaMaster() )&& Objects.nonNull(dealer.getDistrict()) && Objects.nonNull(dealer.getState())) {
                   UserSubAreaMappingModel userSubAreaMappingModel= sclDealerRetailerDao.getUserSubAreaMappingModelModel(sclUser,dealer.getSubAreaMaster(),dealer.getDistrict(),dealer.getState(), dealer.getTaluka());
                   if(Objects.isNull(userSubAreaMappingModel)){
                       userSubAreaMappingModel = new UserSubAreaMappingModel();
                       userSubAreaMappingModel.setSclUser(sclUser);
                       userSubAreaMappingModel.setUpdatedByJob(todayAsString);
                       userSubAreaMappingModel.setSubAreaMaster(dealer.getSubAreaMaster());
                       userSubAreaMappingModel.setDistrict(dealer.getDistrict());
                       userSubAreaMappingModel.setBrand(getCmsAdminSiteService().getSiteForId(SclCoreConstants.SCL_SITE));
                       userSubAreaMappingModel.setState(dealer.getState());
                       userSubAreaMappingModel.setSubArea(dealer.getTaluka());
                       userSubAreaMappingModel.setRegionMaster(Objects.nonNull(dealer.getRegionMaster())? dealer.getRegionMaster() : null);
                       modelService.save(userSubAreaMappingModel);

               /* SclUserModel user=dealer.getTerritoryCode().getSclUser();
                user.setUserSubAreaMapping(userSubAreaMappingModel);
                modelService.save(user);*/


                   }else{

                       userSubAreaMappingModel.setUpdatedByJob(todayAsString);
                       modelService.save(userSubAreaMappingModel);
                       LOG.info(String.format("UserSubAreaMappingModel is already present for User  %s and  district :- %s",dealer.getTerritoryCode().getSclUser().getUid(), dealer.getDistrict()));

                   }
               }else{
                   LOG.info(String.format("UserSubAreaMappingModel will not be create due to  values not present for District:- %s or State:- %s or territoryCode:- %s or subAreMatser:- %s in dealer:-%s",dealer.getDistrict(),dealer.getState(), dealer.getTerritoryCode(),dealer.getSubAreaMaster(),dealer.getUid()));

               }


               if(org.apache.commons.collections.CollectionUtils.isNotEmpty(retailerList)){
                   for(SclCustomerModel retailer: retailerList){

                       UserSubAreaMappingModel usmRetailer =sclDealerRetailerDao.getUserSubAreaMappingModelModel(sclUser,retailer.getSubAreaMaster(),retailer.getDistrict(),retailer.getState(), retailer.getTaluka());
                       if(Objects.nonNull(retailer.getSubAreaMaster()) && StringUtils.isNotEmpty(retailer.getDistrict()) && StringUtils.isNotEmpty(retailer.getState()) && Objects.isNull(usmRetailer)) {
                           UserSubAreaMappingModel userSubAreaMappingRetailer = new UserSubAreaMappingModel();
                           userSubAreaMappingRetailer.setSclUser(sclUser);
                           userSubAreaMappingRetailer.setUpdatedByJob(todayAsString);
                           userSubAreaMappingRetailer.setSubAreaMaster(retailer.getSubAreaMaster());
                           userSubAreaMappingRetailer.setDistrict(retailer.getDistrict());
                           userSubAreaMappingRetailer.setBrand(getCmsAdminSiteService().getSiteForId(SclCoreConstants.SCL_SITE));
                           userSubAreaMappingRetailer.setState(retailer.getState());
                           userSubAreaMappingRetailer.setSubArea(retailer.getTaluka());
                           userSubAreaMappingRetailer.setRegionMaster(Objects.nonNull(retailer.getRegionMaster()) ? retailer.getRegionMaster() : null);
                           modelService.save(userSubAreaMappingRetailer);
                       }else
                        {
                           usmRetailer.setUpdatedByJob(todayAsString);
                           modelService.save(usmRetailer);
                           LOG.info(String.format("UserSubAreaMappingModel is already present for User  %s and  district :- %s",sclUser.getUid(), dealer.getDistrict()));

                       }
                   }
               }



           }
       }




    }


    public SclCustomerModel sclCustSearchOrCreate(String uid, String name, String email){
        SclCustomerModel sclCustomerModel=null;
        try
        {
            sclCustomerModel= (SclCustomerModel) userService.getUserForUID(uid.toLowerCase());
        }
        catch (final UnknownIdentifierException | ClassMismatchException e) {
            LOG.error("SclCustomerPostPersistHook :Failed to get user with code : "+ uid);
          //  sclCustomerModel= new SclCustomerModel();
           // sclCustomerModel.setUid(uid);
         //   sclCustomerModel.setName(name);
         //   sclCustomerModel.setEmail(email);
//sclCustomerModel.setDefaultB2BUnit(b2bCommerceUnitService.getUnitForUid(DEFAULTUNIT));
        //    modelService.save(sclCustomerModel);
        }
        return sclCustomerModel;

    }





    public CMSAdminSiteService getCmsAdminSiteService() {
        return cmsAdminSiteService;
    }

    public void setCmsAdminSiteService(CMSAdminSiteService cmsAdminSiteService) {
        this.cmsAdminSiteService = cmsAdminSiteService;
    }

}
