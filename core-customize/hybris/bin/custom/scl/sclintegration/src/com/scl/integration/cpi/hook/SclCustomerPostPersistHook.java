/*
 *  * Copyright (c) SCL. All rights reserved.
 */

package com.scl.integration.cpi.hook;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.*;
import com.scl.core.enums.*;
import com.scl.core.model.*;
import com.scl.core.region.dao.GeographicalRegionDao;
import com.scl.core.services.SclDealerRetailerService;
import com.scl.integration.service.SclintegrationService;
import de.hybris.platform.b2b.company.B2BCommerceUnitService;
import de.hybris.platform.cms2.servicelayer.services.admin.CMSAdminSiteService;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.inboundservices.persistence.PersistenceContext;
import de.hybris.platform.inboundservices.persistence.hook.PostPersistHook;
import de.hybris.platform.integrationservices.item.DefaultIntegrationItem;
import de.hybris.platform.servicelayer.exceptions.ClassMismatchException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class SclCustomerPostPersistHook  implements PostPersistHook {
    private static final org.apache.log4j.Logger LOG = Logger.getLogger(SclCustomerPostPersistHook.class);

    public static final String FIND_SCLDEALER_PASSword = "FIND_SCLDEALER_PASSword";
    public static final String FIND_SCLRETAILER_PASSword = "FIND_SCLRETAILER_PASSword";



    @Resource(name = "modelService")
    private ModelService modelService;

    @Resource(name = "sclintegrationService")
    private SclintegrationService sclintegrationService;

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


    private CMSAdminSiteService cmsAdminSiteService;

    @Resource
    SclDealerRetailerDao sclDealerRetailerDao;

    @Resource
    private DealerDao dealerDao;

    @Resource
    private GeographicalRegionDao geographicalRegionDao;


    @Resource
    DataConstraintDao dataConstraintDao;





    @Override
    public void execute(ItemModel item, PersistenceContext context) {
        if (item != null  && item instanceof SclCustomerModel) {
           
            SclCustomerModel sclCust= (SclCustomerModel) item;
            LOG.info("SclCustomerPostPersistHook called for Customer: " + sclCust.getUid());
            String contextPartnerFunctionId=null;
            List<String> contextPartnerFunctionIds= new ArrayList<>();
            List<String> contextPartnerFunctionRTIds= new ArrayList<>();
            if(Objects.nonNull(context.getIntegrationItem()) && Objects.nonNull(context.getIntegrationItem().getAttribute("addresses")) ){
                ArrayList<DefaultIntegrationItem> contextItems= (ArrayList<DefaultIntegrationItem>) context.getIntegrationItem().getAttribute("addresses");
                for(DefaultIntegrationItem contextItem: contextItems){
                    if(Objects.nonNull(contextItem.getAttribute("partnerFunctionId"))  && Objects.nonNull(contextItem.getAttribute("sapAddressUsage")) && contextItem.getAttribute("sapAddressUsage").toString().equalsIgnoreCase("WE")){
                        contextPartnerFunctionIds.add(contextItem.getAttribute("partnerFunctionId").toString());
                    }
                    else if(Objects.nonNull(contextItem.getAttribute("partnerFunctionId"))  && Objects.nonNull(contextItem.getAttribute("sapAddressUsage")) && contextItem.getAttribute("sapAddressUsage").toString().equalsIgnoreCase("RT")){
                        contextPartnerFunctionRTIds.add(contextItem.getAttribute("partnerFunctionId").toString());
                    }
                }
            }
            String routeID;
            RouteMasterModel routemaster;
            CounterRouteMappingModel counterRouteMappingModel;
            String stateCode=null;

            Set<PrincipalGroupModel> ugSet=new HashSet<>(sclCust.getGroups());

            try {
                //DealerRetailerMapping population






             /*   if (Objects.nonNull(sclCust.getCustomerGrouping())) {
                    if (sclCust.getCustomerGrouping().getCode().equalsIgnoreCase("ZRET")) {
                        ugSet.add(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID));
                        sclCust.setGroups(ugSet);
                        sclCust.setCounterType(CounterType.RETAILER);
                        if(modelService.isNew(sclCust)){
                            sclCust.setPassword(dataConstraintDao.findPasswordByConstraintName(FIND_SCLRETAILER_PASSword));
                            sclCust.setLoginDisabled(false);
                        }

                    } else if (sclCust.getCustomerGrouping().getCode().equalsIgnoreCase("ZDOM")) {
                        ugSet.add(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID));
                        sclCust.setGroups(ugSet);
                        sclCust.setCounterType(CounterType.DEALER);
                        if(modelService.isNew(sclCust)) {
                            sclCust.setPassword(dataConstraintDao.findPasswordByConstraintName(FIND_SCLDEALER_PASSword));
                            sclCust.setLoginDisabled(false);
                        }
                    } else if (sclCust.getCustomerGrouping().getCode().equalsIgnoreCase("YDOM")) {
                        ugSet.add(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SHIPTO_USER_GROUP_UID));
                        sclCust.setGroups(ugSet);
                        sclCust.setCounterType(CounterType.SHIPTOPARTY);
                    }

                }*/


                if (Objects.nonNull(sclCust.getCustomerGrouping()) && sclCust.getCustomerGrouping().getCode().equalsIgnoreCase("ZDOM") && CollectionUtils.isNotEmpty(sclCust.getAddresses())) {


                    for (AddressModel add : sclCust.getAddresses()) {

                        if (StringUtils.isNotEmpty(add.getSapAddressUsage()) && add.getSapAddressUsage().equalsIgnoreCase(SclCoreConstants.SHIPTOTYPE) && CollectionUtils.isNotEmpty(contextPartnerFunctionIds) && contextPartnerFunctionIds.contains(add.getPartnerFunctionId())) {
                            DealerRetailerMappingModel drm = new DealerRetailerMappingModel();
                            
                            add.setAddressCreatedStatus(AddressCreatedStatus.UPDATEDFROMS4HANA);
                            modelService.save(add);
                            modelService.refresh(add);
                            
                            drm.setDealer(sclCust);
                            drm.setShipTo(add);
                            drm.setLastUsed(new Date());
                            if(Objects.nonNull(add.getGeographicalMaster())) {
                                drm.setDistrict((StringUtils.isNotEmpty(add.getGeographicalMaster().getDistrict())) ? add.getGeographicalMaster().getDistrict() : StringUtils.EMPTY);
                                drm.setState((StringUtils.isNotEmpty(add.getGeographicalMaster().getState())) ? add.getGeographicalMaster().getState() : StringUtils.EMPTY);

                                drm.setPinCode((StringUtils.isNotEmpty(add.getGeographicalMaster().getPincode())) ? add.getGeographicalMaster().getPincode() : StringUtils.EMPTY);
                                drm.setErpCity((StringUtils.isNotEmpty(add.getGeographicalMaster().getErpCity())) ? add.getGeographicalMaster().getErpCity() : StringUtils.EMPTY);
                                drm.setTaluka((StringUtils.isNotEmpty(add.getGeographicalMaster().getTaluka())) ? add.getGeographicalMaster().getTaluka() : StringUtils.EMPTY);
                            }
                            drm.setPartnerFunctionId((StringUtils.isNotEmpty(add.getPartnerFunctionId())) ? add.getPartnerFunctionId() : StringUtils.EMPTY);

                            DealerRetailerMappingModel drmModel = sclintegrationService.getDealerRetailerMappingModel(sclCust.getPk().toString(), add.getPk().toString(), null);

                            if (Objects.isNull(drmModel)) {
                                modelService.save(drm);
                            } else {
                                if(Objects.nonNull(add.getGeographicalMaster())) {
                                    drmModel.setDistrict((StringUtils.isNotEmpty(add.getGeographicalMaster().getDistrict())) ? add.getGeographicalMaster().getDistrict() : StringUtils.EMPTY);
                                    drmModel.setState((StringUtils.isNotEmpty(add.getGeographicalMaster().getState())) ? add.getGeographicalMaster().getState() : StringUtils.EMPTY);
                                     drmModel.setPinCode((StringUtils.isNotEmpty(add.getGeographicalMaster().getPincode())) ? add.getGeographicalMaster().getPincode() : StringUtils.EMPTY);
                                    drmModel.setErpCity((StringUtils.isNotEmpty(add.getGeographicalMaster().getErpCity())) ? add.getGeographicalMaster().getErpCity() : StringUtils.EMPTY);
                                    drmModel.setTaluka((StringUtils.isNotEmpty(add.getGeographicalMaster().getTaluka())) ? add.getGeographicalMaster().getTaluka() : StringUtils.EMPTY);
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

                deleteDealerRetailerMappingAndPF(sclCust,contextPartnerFunctionIds,contextPartnerFunctionRTIds);

                modelService.refresh(sclCust);
                //Populating userSubAreaMapping table After SubAreaMaster
                createUserSubAreaMapping(sclCust);

                //Route Master
                if(Objects.nonNull(sclCust.getAddresses().stream().filter(add -> (StringUtils.isNotEmpty(add.getSapAddressUsage()) && add.getSapAddressUsage().equalsIgnoreCase("DE"))).collect(Collectors.toList()).get(0).getGeographicalMaster()) && StringUtils.isNotEmpty(sclCust.getAddresses().stream().filter(add -> (StringUtils.isNotEmpty(add.getSapAddressUsage()) && add.getSapAddressUsage().equalsIgnoreCase("DE"))).collect(Collectors.toList()).get(0).getGeographicalMaster().getStateCode())){
                    stateCode=sclCust.getAddresses().stream().filter(add -> (StringUtils.isNotEmpty(add.getSapAddressUsage()) && add.getSapAddressUsage().equalsIgnoreCase("DE"))).collect(Collectors.toList()).get(0).getGeographicalMaster().getStateCode();
                }

                if(StringUtils.isNotEmpty(sclCust.getTaluka()) && StringUtils.isNotEmpty(stateCode) && StringUtils.isNotEmpty(sclCust.getDistrict()) && (sclCust.getCounterType().equals(CounterType.DEALER) || sclCust.getCounterType().equals(CounterType.RETAILER))){

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
                    counterRouteMappingModel.setBrand(sclintegrationService.getCMSSiteForID("scl"));
                    counterRouteMappingModel.setRoute(routemaster.getRouteId());

                    modelService.save(counterRouteMappingModel);
                    }

                }

                // SubareaMaster & CustomerSubAreaMapping population
                if(Objects.nonNull(sclCust.getSubAreaMaster()) && (sclCust.getCounterType().equals(CounterType.DEALER) || sclCust.getCounterType().equals(CounterType.RETAILER)) ){



                    CustomerSubAreaMappingModel customerSubAreaMappingModel= sclintegrationService.getCustomerSubAreaMapping(sclCust.getUid(),sclCust.getSubAreaMaster(), sclCust.getState(), sclCust.getTaluka(), sclCust.getDistrict());

                    if(Objects.isNull(customerSubAreaMappingModel)){
                        customerSubAreaMappingModel= new CustomerSubAreaMappingModel();
                        customerSubAreaMappingModel.setSclCustomer(sclCust);
                    }
                        customerSubAreaMappingModel.setSubAreaMaster(sclCust.getSubAreaMaster());
                        customerSubAreaMappingModel.setDistrict(sclCust.getSubAreaMaster().getDistrict());
                        customerSubAreaMappingModel.setIsActive(true);
                        customerSubAreaMappingModel.setState(sclCust.getState());
                        customerSubAreaMappingModel.setSubArea(sclCust.getTaluka());
                        customerSubAreaMappingModel.setBrand(sclintegrationService.getCMSSiteForID("scl"));
                        customerSubAreaMappingModel.setCounterType(sclCust.getCounterType().getCode());
                        modelService.save(customerSubAreaMappingModel);



                }


            }
            catch (RuntimeException e){
                LOG.info("SclCustomerPostPersistHook exception : " + e.getMessage());
                e.printStackTrace();
            }

            LOG.info("SclCustomerPostPersistHook executed....");

        }

    }

    private void createUserSubAreaMapping(SclCustomerModel dealer)
    {
        String pattern = "MM/dd/yyyy";
        List<SclUserModel> sclUsers=null;
        DateFormat df = new SimpleDateFormat(pattern);
        Date today = Calendar.getInstance().getTime();
        String todayAsString = df.format(today);
        if (Objects.nonNull(dealer.getTerritoryCode())){
            sclUsers=  dealerDao.getSclUserUsingTerritoryusermap(dealer.getTerritoryCode());
        }

        if(CollectionUtils.isNotEmpty(sclUsers)){
            for(SclUserModel sclUser: sclUsers) {

                if (Objects.nonNull(dealer.getTerritoryCode()) && Objects.nonNull(sclUser)
                        && Objects.nonNull(dealer.getSubAreaMaster()) && Objects.nonNull(dealer.getDistrict()) && Objects.nonNull(dealer.getState())) {
                    UserSubAreaMappingModel userSubAreaMappingModel = sclDealerRetailerDao.getUserSubAreaMappingModelModel(sclUser, dealer.getSubAreaMaster(), dealer.getDistrict(), dealer.getState(), dealer.getTaluka());
                    if (Objects.isNull(userSubAreaMappingModel)) {
                        userSubAreaMappingModel = new UserSubAreaMappingModel();
                        userSubAreaMappingModel.setSclUser(sclUser);
                        userSubAreaMappingModel.setUpdatedByJob(todayAsString);
                        userSubAreaMappingModel.setCreatedFromCRMorERP(CreatedFromCRMorERP.S4HANA);
                        userSubAreaMappingModel.setSubAreaMaster(dealer.getSubAreaMaster());
                        userSubAreaMappingModel.setDistrict(dealer.getDistrict());
                        userSubAreaMappingModel.setBrand(getCmsAdminSiteService().getSiteForId(SclCoreConstants.SCL_SITE));
                        userSubAreaMappingModel.setState(dealer.getState());
                        userSubAreaMappingModel.setSubArea(dealer.getTaluka());
                        userSubAreaMappingModel.setRegionMaster(Objects.nonNull(dealer.getRegionMaster()) ? dealer.getRegionMaster() : null);
                        modelService.save(userSubAreaMappingModel);

               /* SclUserModel user=dealer.getTerritoryCode().getSclUser();
                user.setUserSubAreaMapping(userSubAreaMappingModel);
                modelService.save(user);*/


                    } else {
                         userSubAreaMappingModel.setUpdatedByJob(todayAsString);
                         userSubAreaMappingModel.setCreatedFromCRMorERP(CreatedFromCRMorERP.S4HANA);
                        modelService.save(userSubAreaMappingModel);
                        LOG.info(String.format("UserSubAreaMappingModel is already present for User  %s and  district :- %s", dealer.getTerritoryCode().getSclUser().getUid(), dealer.getDistrict()));

                    }
                } else {
                    LOG.info(String.format("UserSubAreaMappingModel will not be create due to  values not present for District:- %s or State:- %s or territoryCode:- %s or subAreMatser:- %s in dealer:-%s", dealer.getDistrict(), dealer.getState(), dealer.getTerritoryCode(), dealer.getSubAreaMaster(), dealer.getUid()));

                }


                // userSubAreaMappingRetailer for retailers under dealer
                List<SclCustomerModel> retailerList = sclDealerRetailerDao.getRetailerMappingListForDealer(dealer);
                if (CollectionUtils.isNotEmpty(retailerList)) {
                    for (SclCustomerModel retailer : retailerList) {

                        UserSubAreaMappingModel usmRetailer=sclDealerRetailerDao.getUserSubAreaMappingModelModel(sclUser, retailer.getSubAreaMaster(), retailer.getDistrict(), retailer.getState(), retailer.getTaluka());
                        if (Objects.nonNull(retailer.getSubAreaMaster()) && StringUtils.isNotEmpty(retailer.getDistrict()) && StringUtils.isNotEmpty(retailer.getState()) && Objects.isNull(usmRetailer)) {
                            UserSubAreaMappingModel userSubAreaMappingRetailer = new UserSubAreaMappingModel();
                            userSubAreaMappingRetailer.setSclUser(sclUser);
                            userSubAreaMappingRetailer.setUpdatedByJob(todayAsString);
                            userSubAreaMappingRetailer.setCreatedFromCRMorERP(CreatedFromCRMorERP.S4HANA);
                            userSubAreaMappingRetailer.setSubAreaMaster(retailer.getSubAreaMaster());
                            userSubAreaMappingRetailer.setDistrict(retailer.getDistrict());
                            userSubAreaMappingRetailer.setBrand(getCmsAdminSiteService().getSiteForId(SclCoreConstants.SCL_SITE));
                            userSubAreaMappingRetailer.setState(retailer.getState());
                            userSubAreaMappingRetailer.setSubArea(retailer.getTaluka());
                            userSubAreaMappingRetailer.setRegionMaster(Objects.nonNull(retailer.getRegionMaster()) ? retailer.getRegionMaster() : null);
                            modelService.save(userSubAreaMappingRetailer);
                        }
                        else {
                            usmRetailer.setUpdatedByJob(todayAsString);
                            usmRetailer.setCreatedFromCRMorERP(CreatedFromCRMorERP.S4HANA);
                            modelService.save(usmRetailer);
                            LOG.info(String.format("UserSubAreaMappingModel is already present for User  %s and  district :- %s", sclUser.getUid(), dealer.getDistrict()));

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
            return sclCustomerModel;
//            sclCustomerModel= new SclCustomerModel();
//            sclCustomerModel.setUid(uid);
//            sclCustomerModel.setName(name);
//            sclCustomerModel.setEmail(email);
//            sclCustomerModel.setDefaultB2BUnit(b2bCommerceUnitService.getUnitForUid(DEFAULTUNIT));
//            modelService.save(sclCustomerModel);
        }
        return sclCustomerModel;

    }

    private  void deleteDealerRetailerMappingAndPF(SclCustomerModel customerModel,List<String> contextPartnerFunctionWEIds, List<String> contextPartnerFunctionRTIds) {

     try{
         ArrayList<AddressModel> notMatchingAddresses = new ArrayList<>();
        if (Objects.nonNull(customerModel.getCustomerGrouping()) && customerModel.getCustomerGrouping().equals(CustomerGrouping.ZDOM)) {
            for (AddressModel add : customerModel.getAddresses()) {
                if (Objects.nonNull(add.getSapAddressUsage()) && add.getSapAddressUsage().equalsIgnoreCase("WE") && !contextPartnerFunctionWEIds.contains(add.getPartnerFunctionId())) {
                    notMatchingAddresses.add(add);
                } else if (Objects.nonNull(add.getSapAddressUsage()) && add.getSapAddressUsage().equalsIgnoreCase("RT") && !contextPartnerFunctionRTIds.contains(add.getPartnerFunctionId())) {
                    notMatchingAddresses.add(add);
                }
            }

            if (CollectionUtils.isNotEmpty(notMatchingAddresses)) {
                for (AddressModel notFoundAdd : notMatchingAddresses) {

                    if (Objects.nonNull(notFoundAdd.getSapAddressUsage()) && notFoundAdd.getSapAddressUsage().equalsIgnoreCase("WE")) {
                        List<DealerRetailerMappingModel> drmList = sclDealerRetailerDao.getDealerRetailerMappingListModel(customerModel, notFoundAdd.getPk().toString(), null);
                        if (CollectionUtils.isNotEmpty(drmList)) {
                            for (DealerRetailerMappingModel drm : drmList) {
                                LOG.info("deleteDealerRetailerMapping Deleting the drm data DealerId " + drm.getDealer().getUid() + " shipTo id id" + notFoundAdd.getPartnerFunctionId());
                                modelService.remove(notFoundAdd);
                                modelService.remove(drm);
                            }
                        }

                    } else if (Objects.nonNull(notFoundAdd.getSapAddressUsage()) && notFoundAdd.getSapAddressUsage().equalsIgnoreCase("RT")) {
                        SclCustomerModel retailer = (SclCustomerModel) userService.getUserForUID(notFoundAdd.getPartnerFunctionId());
                        List<DealerRetailerMappingModel> drmList = sclDealerRetailerDao.getDealerRetailerMappingListModel(customerModel, null, retailer.getPk().toString());
                        if (CollectionUtils.isNotEmpty(drmList)) {
                            for (DealerRetailerMappingModel drm : drmList) {
                                LOG.info("deleteDealerRetailerMapping removing retailer from drm data DealerId " + drm.getDealer().getUid() + " shipTo id id" + notFoundAdd.getPartnerFunctionId() + "retailer id: " + retailer.getUid());
                                drm.setRetailer(null);
                                modelService.save(drm);
                                modelService.remove(notFoundAdd);
                            }
                        }

                    }


                }
            }

        } else if (Objects.nonNull(customerModel.getCustomerGrouping()) && (customerModel.getCustomerGrouping().equals(CustomerGrouping.ZRET) || customerModel.getCustomerGrouping().equals(CustomerGrouping.YDOM))) {
            for (AddressModel add : customerModel.getAddresses()) {
                if (Objects.nonNull(add.getSapAddressUsage()) && add.getSapAddressUsage().equalsIgnoreCase("WE") && !contextPartnerFunctionWEIds.contains(add.getPartnerFunctionId())) {
                    notMatchingAddresses.add(add);
                } else if (Objects.nonNull(add.getSapAddressUsage()) && add.getSapAddressUsage().equalsIgnoreCase("RT") && !contextPartnerFunctionRTIds.contains(add.getPartnerFunctionId())) {
                    notMatchingAddresses.add(add);
                }
            }

            if (CollectionUtils.isNotEmpty(notMatchingAddresses)) {
                modelService.removeAll(notMatchingAddresses);
            }


        }

        }catch(RuntimeException e){
         LOG.error("deleteDealerRetailerMappingAndPF for dealer" + customerModel.getUid() + " exception "+ e.getMessage());
        }


   }


    public CMSAdminSiteService getCmsAdminSiteService() {
        return cmsAdminSiteService;
    }

    public void setCmsAdminSiteService(CMSAdminSiteService cmsAdminSiteService) {
        this.cmsAdminSiteService = cmsAdminSiteService;
    }


}
