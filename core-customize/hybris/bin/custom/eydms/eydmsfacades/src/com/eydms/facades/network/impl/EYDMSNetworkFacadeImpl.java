package com.eydms.facades.network.impl;

import com.eydms.core.constants.GeneratedEyDmsCoreConstants;
import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.customer.services.EyDmsCustomerService;
import com.eydms.core.dao.*;
import com.eydms.core.enums.*;
import com.eydms.core.model.*;
import com.eydms.core.notifications.service.EyDmsNotificationService;
import com.eydms.core.services.DJPVisitService;
import com.eydms.core.services.NetworkService;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.core.services.impl.EyDmsWorkflowServiceImpl;
import com.eydms.core.util.EYDMSDataFormatUtil;
import com.eydms.core.utility.EyDmsDateUtility;
import com.eydms.facades.SalesPerformanceFacade;
import com.eydms.facades.data.*;
import com.eydms.facades.data.order.vehicle.DealerVehicleDetailsData;
import com.eydms.facades.data.order.vehicle.DealerVehicleDetailsListData;
import com.eydms.facades.network.EYDMSNetworkFacade;
import com.eydms.facades.prosdealer.data.DealerListData;
import com.eydms.facades.prosdealer.data.NominationData;
import com.eydms.facades.visit.data.SiteSummaryData;
import com.eydms.occ.dto.InfluencerNomineeData;
import com.eydms.occ.dto.RetailerOnboardDto;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.notificationservices.enums.NotificationType;
import de.hybris.platform.notificationservices.enums.SiteMessageType;
import de.hybris.platform.notificationservices.model.SiteMessageForCustomerModel;
import de.hybris.platform.notificationservices.model.SiteMessageModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.util.SloppyMath;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class EYDMSNetworkFacadeImpl implements EYDMSNetworkFacade {

    private static final Logger LOGGER = Logger.getLogger(EYDMSNetworkFacadeImpl.class);
    public static final String VERIFY = "verify";
    private static final String RETAILER = "RETAILER";
    private static final String INFLUENCER = "INFLUENCER";
    private static final String DEALER = "DEALER";
    DecimalFormat df = new DecimalFormat("#.#");

    private static final Logger LOG = Logger.getLogger(EYDMSNetworkFacadeImpl.class);


    @Resource
    private Converter<EYDMSNetworkAdditionPlanData, NetworkAdditionPlanModel> networkAdditionPlanReverseConverter;
    @Resource
    private Converter<NetworkAdditionPlanModel, EYDMSNetworkAdditionPlanData> networkAdditionPlanConverter;
    @Resource
    private Converter<InfluencerData, EyDmsCustomerModel> influencerReverseConverter;
    @Resource
    private Converter<EyDmsCustomerModel,InfluencerData> influncerConverter;
    @Resource
    private Populator<InfluencerFinanceData, EyDmsCustomerModel> influencerFinancialsReversePopulator;
    @Resource
    private Populator<EyDmsCustomerModel,InfluencerFinanceData> influencerFinancialsPopulator;
    @Resource
    private Converter<NominationData, NominationModel> nominationConverter;
    @Resource
    private Converter<EyDmsCustomerModel, InfluencerSummaryData> influencerSummaryConverter;
    @Resource
    private EYDMSGenericDao eydmsGenericDao;
    @Resource
    private ModelService modelService;
    @Resource
    private NetworkDao networkDao;
    @Resource
    private SalesPerformanceDao salesPerformanceDao;

    public SalesPerformanceDao getSalesPerformanceDao() {
        return salesPerformanceDao;
    }

    public void setSalesPerformanceDao(SalesPerformanceDao salesPerformanceDao) {
        this.salesPerformanceDao = salesPerformanceDao;
    }

    @Resource
    private NetworkService networkService;
    @Resource
    protected BaseSiteService baseSiteService;
    @Resource
    private TerritoryManagementService territoryManagementService;
    @Resource
    private EnumerationService enumerationService;
    @Resource
    EyDmsCustomerService eydmsCustomerService;

    @Autowired
    EyDmsNotificationService eydmsNotificationService;

    @Resource
    private UserService userService;
    @Resource
    private Converter<MeetingScheduleData, MeetingScheduleModel> meetScheduleReverseConverter;
    @Resource
    private Converter<EyDmsCustomerModel, ProspectiveNetworkData> prospectiveNetworkConverter;
    @Resource
    private Converter<DealerVehicleDetailsModel, DealerVehicleDetailsData> dealerVehicleDetailsConverter;
    @Resource
    private Converter<EyDmsCustomerModel, SiteDetailData> siteDataConverter;
    @Resource
    private Converter<AddressModel, EYDMSAddressData> eydmsAddressConverter;
    @Resource
    private Converter<EyDmsCustomerModel, CustomerCardData> customerCardDataConverter;

    @Resource
    private Converter<EyDmsCustomerModel, DealerDetails360Data> dealerDetails360Converter;
    @Resource
    private Converter<EyDmsCustomerModel, InfluencersDetails360WsData> influencerDetails360Converter;
    @Resource
    private Converter<SchemeDetailsModel, OngoingSchemeDetailsData> ongoingSchemeConverter;
    @Resource
    private Converter<EyDmsCustomerModel, DealerDetailsFormData> dealerDetailsFormConverter;
    @Resource
    private Converter<LeadMasterModel, LeadSummaryData> leadSummaryConverter;
    @Resource
    private Converter<MeetingScheduleModel, ScheduledMeetData> scheduledMeetConverter;

    @Autowired
    Converter<EyDmsCustomerModel,CustomerData> dealerBasicConverter;


    @Resource
    SalesPerformanceFacade salesPerformanceFacade;
    @Resource
    private Converter<EyDmsCustomerModel, SalesPromoterDetailsData> spDetails360Converter;
    
    @Resource
    DJPVisitService djpVisitService;

    @Autowired
    EyDmsWorkflowServiceImpl eydmsWorkflowService;

    @Autowired
    KeyGenerator siteMessageUidGenerator;

    @Override
    public String addNetworkPlan(EYDMSNetworkAdditionPlanData planData) {
        var planModel = getPlanModel(planData.getTaluka(), planData.getApplicableLead());
        if (null != planModel) {
            planModel = networkAdditionPlanReverseConverter.convert(planData, planModel);
        } else {
            planModel = networkAdditionPlanReverseConverter.convert(planData);

        }
        if (Objects.nonNull(planModel)) {
            planModel.setApprovalLevel(TerritoryLevels.DISTRICT);
            EyDmsWorkflowModel approvalWorkflowModel= eydmsWorkflowService.saveWorkflow("NETWORK_ADDITION_WORKFLOW", WorkflowStatus.START, WorkflowType.NETWORK_ADDITION_PLAN);
            planModel.setApprovalWorkflow(approvalWorkflowModel);
            //modelService.save(planModel);
            EyDmsWorkflowActionModel eydmsWorkflowActionModel = eydmsWorkflowService.saveWorkflowAction(approvalWorkflowModel, "", planModel.getBrand(), planModel.getSubAreaMaster(), TerritoryLevels.DISTRICT);
            modelService.save(eydmsWorkflowActionModel);
            planModel.setStatus(NetworkAdditionStatus.valueOf(GeneratedEyDmsCoreConstants.Enumerations.NetworkAdditionStatus.PENDING_FOR_APPROVAL_BY_TSM));
            modelService.save(planModel);

            return planModel.getId();
        }
        return null;

    }

    @Override
    public NetworkAdditionData getNetworkAdditionDetails(String leadType, String taluka){
        NetworkAdditionPlanModel planModel = getPlanModel(taluka, leadType);
        NetworkAdditionData data = new NetworkAdditionData();

        if(Objects.isNull(planModel)){
            LOGGER.info("Plan Model is null");
            data.setLeadType(leadType);
            data.setActual(0);
            data.setTarget(0);
            return data;
        }
        LOGGER.info(String.format("Plan Model: %s ", String.valueOf(planModel.getRevisedPlan())));

        var target=planModel.getRevisedPlan();
        if(Objects.isNull(target) || target==0) {
            target=planModel.getSystemProposed();
        }
        //data.setTarget(target);
        data.setTarget(Objects.isNull(target) ? 0 : target);
        var achivement=networkService.getOnboarderCustomer(leadType,taluka);
        data.setActual(Objects.isNull(achivement) ? 0 : achivement);
        //data.setActual(achivement);
        LOGGER.info(String.format("LEADTYPE :: " +leadType));
        if(leadType.equalsIgnoreCase("INFLUENCER"))
        {
            data.setLeadType("INFLUENCER");
        }
        else {
            data.setLeadType(leadType);
        }
        return data;
    }

    private NetworkAdditionPlanModel getPlanModel(String taluka, String leadType) {
        Date timestamp=EyDmsDateUtility.getFirstDayOfFinancialYear();
        
        List<SubAreaMasterModel> talukas = new ArrayList<>();
        
            if(taluka.equalsIgnoreCase("ALL")) {
            	
            	if(userService.getCurrentUser() instanceof EyDmsUserModel)
            	{
            		talukas = territoryManagementService.getTerritoriesForSO();
            	}
            	else
            	{
            		talukas = territoryManagementService.getTerritoriesForCustomer((EyDmsCustomerModel)userService.getCurrentUser());
            	}
               
            }
            else
            {
            	talukas.add(territoryManagementService.getTerritoryById(taluka));
            }
        
            if (!talukas.isEmpty()) {
                return networkDao.findNeworkPlanByTalukaAndLeadType(talukas, leadType, timestamp);
            }

        return null;
    }



    @Override
    public EYDMSNetworkAdditionPlanData getNetworkPlan(String leadType, String taluka) {
        var planModel = getPlanModel(taluka, leadType);
        if(Objects.isNull(planModel)) {
            planModel = modelService.create(NetworkAdditionPlanModel.class);
        }
        var planinfo = networkService.getCounterInfoForTaluka(leadType, taluka);
        planModel.setShreeCounter(planinfo.get("shreeCounter"));
        planModel.setTotalCounter(planinfo.get("totalCounter"));
        return networkAdditionPlanConverter.convert(planModel);

    }

    @Override
    public String addInfluencerBasics(InfluencerData data) {
    	EyDmsCustomerModel influencer = influencerReverseConverter.convert(data);
        if (Objects.nonNull(influencer)) {
            return influencer.getUid();
        }
        return null;
    }

    @Override
    public InfluencerData getInfluencerBasics(String id) {
        EyDmsCustomerModel influencer= (EyDmsCustomerModel) eydmsGenericDao.findItemByTypeCodeAndUidParam(EyDmsCustomerModel._TYPECODE, EyDmsCustomerModel.UID, id);
        return influncerConverter.convert(influencer);
    }

    @Override
    public String addInfluencerFinancials(InfluencerFinanceData data) {
        EyDmsCustomerModel influencer= (EyDmsCustomerModel) eydmsGenericDao.findItemByTypeCodeAndUidParam(EyDmsCustomerModel._TYPECODE, EyDmsCustomerModel.UID, data.getInfluencerId());
        influencerFinancialsReversePopulator.populate(data,influencer);
        modelService.save(influencer);
        return influencer.getUid();
    }

    @Override
    public InfluencerFinanceData getInfluencerFinancials(String id) {
        EyDmsCustomerModel influencer= (EyDmsCustomerModel) eydmsGenericDao.findItemByTypeCodeAndUidParam(EyDmsCustomerModel._TYPECODE, EyDmsCustomerModel.UID, id);
        InfluencerFinanceData data=new InfluencerFinanceData();
        influencerFinancialsPopulator.populate(influencer,data);
        return data;
    }

    @Override
    public String addInfluencerNominee(InfluencerNomineeData data) {
        EyDmsCustomerModel influencer= (EyDmsCustomerModel) eydmsGenericDao.findItemByTypeCodeAndUidParam(EyDmsCustomerModel._TYPECODE, EyDmsCustomerModel.UID, data.getInfluencerId());
        influencer.setNominee(nominationConverter.convert(data.getNominee()));

        B2BCustomerModel b2BCustomerModel = (B2BCustomerModel) userService.getCurrentUser();
        if(b2BCustomerModel.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.TSO_GROUP_ID)))
        {
        	influencer.setCustomerOnboardingStatus(CustomerOnboardingStatus.APPROVED);


            //modifiableSet.remove(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.INFLUENCER_ONBOARDING_USER_GROUP_UID));
        	
        	Set<PrincipalGroupModel> modifiableSet=influencer.getGroups();
            Set<PrincipalGroupModel> ugSet = new HashSet<>(modifiableSet);
        	ugSet.remove(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.INFLUENCER_ONBOARDING_USER_GROUP_UID));
            ugSet.add(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID));
            influencer.setGroups(ugSet);
            influencer.setCounterType(CounterType.INFLUENCER);
            influencer.setOnboardingApprovedBy(b2BCustomerModel);
            influencer.setPendingWithSoDate(new Date());
            influencer.setApprovedBySoDate(new Date());
            influencer.setDateOfJoining(new Date());
            influencer.setNetworkType(NetworkType.ACTIVE.getCode());
            if(data.getLeadId()!=null)
            {
                LeadMasterModel leadMaster=networkService.findItemByUidParam(data.getLeadId());
                if(influencer.getLeadMaster() == null || !(influencer.getLeadMaster().getLeadId().equalsIgnoreCase(data.getLeadId())))
                {
                    influencer.setLeadMaster(leadMaster);
                }

                if(leadMaster.getOnboardedCustomer() == null && !(leadMaster.getOnboardedCustomer().getUid().equalsIgnoreCase(influencer.getUid()))) {
                    leadMaster.setOnboardedCustomer(influencer);
                }
                    leadMaster.setOnboardedDate(new Date());
                    EyDmsUserModel eydmsUser = (EyDmsUserModel) userService.getCurrentUser();
                    if(eydmsUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.TSO_GROUP_ID))) {
                        leadMaster.setOnboardedBy(eydmsUser);
                    modelService.save(leadMaster);
                }
            }
        }
        else
        {
        	influencer.setCustomerOnboardingStatus(CustomerOnboardingStatus.PENDING_FOR_APPROVAL);
        	influencer.setPendingWithSoDate(new Date());
        }

        modelService.save(influencer);
        try {
            StringBuilder builder = new StringBuilder();
            Map<String,String> suggestion = new HashMap<>();
            builder.append("You have an onboarding request " +influencer.getApplicationNo());
            builder.append(" raised by " +influencer.getOnboardingPlacedBy().getName() + " " +influencer.getOnboardingPlacedBy().getUid());
            if(influencer.getTaluka()!=null) {
                builder.append(",Taluka - " + influencer.getTaluka());
            }
            String body = builder.toString();
            String sub ="New Onboarding Request has been raised";
            FilterTalukaData filterTalukaData = new FilterTalukaData();
            suggestion.put("OnboardingStatus",influencer.getCustomerOnboardingStatus().getCode());
            suggestion.put("InfluencerId",influencer.getUid());
            suggestion.put("InfluencerType",influencer.getInfluencerType().getCode());
            List<SubAreaMasterModel> subAreaList = territoryManagementService.getTaulkaForUser(filterTalukaData);
            List<EyDmsUserModel> tsoList =  territoryManagementService.getTSOforSubAreas(subAreaList);
            for (EyDmsUserModel tsoUser : tsoList) {
                eydmsNotificationService.submitDealerNotification((B2BCustomerModel) tsoUser, body, sub, NotificationCategory.ONBOARD_REQUEST,suggestion);
            }

        }
        catch(Exception e) {
            LOG.error("Error while sending  Onboarding Request  notification");
        }
        return influencer.getUid();
    }

    @Override
    public InfluencerSummaryListData getInfluencerSummaryList(String searchKey, boolean isNew, String networkType,
                                                              String category,String dealerCategory) {
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        InfluencerSummaryListData data=new InfluencerSummaryListData();
        List<EyDmsCustomerModel> influencerList=new ArrayList<>();
        if((currentUser instanceof EyDmsUserModel) || (((EyDmsCustomerModel)currentUser).getCounterType()!=null && ((EyDmsCustomerModel)currentUser).getCounterType().equals(CounterType.SP))){
            RequestCustomerData requestCustomerData = new RequestCustomerData();
            requestCustomerData.setCounterType(List.of(INFLUENCER));
            influencerList = territoryManagementService.getCustomerforUser(requestCustomerData);
            //influencerList = territoryManagementService.getInfluencersForSubArea();
        }
        else {
            if((currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))){
                influencerList = territoryManagementService.getInfluencerListForDealer();
            }
            else if((currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))){
                influencerList = territoryManagementService.getInfluencerListForRetailer();
            }
        }
        if(influencerList!=null && !influencerList.isEmpty()) {
            if (StringUtils.isNotBlank(networkType)) {
                influencerList = influencerList.stream().filter(cust -> Objects.nonNull(cust.getNetworkType()) && networkType.equalsIgnoreCase(cust.getNetworkType())).collect(Collectors.toList());
            }
            if (StringUtils.isNotBlank(searchKey)) {
                influencerList = networkService.filterEyDmsCustomersWithSearchTerm(influencerList, searchKey);
            }
            if (StringUtils.isNotBlank(category)) {
                influencerList = networkService.getFilteredInfluencerForCategory(influencerList, category);
            }
            if (Objects.nonNull(dealerCategory)) {
                influencerList = networkService.filterEyDmsCustomersWithDealerCategory(influencerList, dealerCategory);
            }
            if (isNew) {
                Date monthBackDate = EyDmsDateUtility.getThreeMonthBackDate();
                influencerList = influencerList.stream().filter(inf -> Objects.nonNull(inf.getDateOfJoining()) && inf.getDateOfJoining().after(monthBackDate)).collect(Collectors.toList());
            }
            data.setInfluncerSummary(getInfluencerDetailedSummaryListData(null, influencerList,true,false));
        }
        return data;
    }



    @Override
    public InfluencerSummaryListData getInfluencerSummaryListForSO(String socode, String category, String networkType,String dealerCategory){
        var influencerModelList=networkService.getEyDmsCustomerForGroupAndSO(EyDmsCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID, socode);
        if(StringUtils.isNotBlank(category)){
            influencerModelList=networkService.getFilteredInfluencerForCategory(influencerModelList,category);
        }
        if(StringUtils.isNotBlank(networkType)){
            influencerModelList=networkService.getFilteredCustomerForNetworkType(influencerModelList,networkType);
        }
        if(Objects.nonNull(dealerCategory)){
            influencerModelList=networkService.filterEyDmsCustomersWithDealerCategory(influencerModelList,dealerCategory);
        }
        return getInfluencerSummaryListData(influencerModelList);
    }

    @Override
    public List<InviteesData> getInviteesListForMeeting(String meetCode, String influencerType, String influencerCategory) {
        List<InviteesData> inviteesList=new ArrayList<>();
        if (Objects.nonNull(meetCode)) {
            var meetSchedule = (MeetingScheduleModel) modelService.get(PK.parse(meetCode));
            var attendees= meetSchedule.getAttendees();
            if(Objects.nonNull(attendees)) {
                if(StringUtils.isNotBlank(influencerType)){
                 attendees=networkService.getFilteredInfluencerForCategory(attendees,influencerType);
                }

                if(Objects.nonNull(influencerCategory)){
                  attendees=networkService.filterEyDmsCustomersWithDealerCategory(attendees,
                           influencerCategory);
                }
                inviteesList.addAll(networkService.getInviteesForMeeting(meetSchedule,attendees));

            }
        }
        return inviteesList;
    }

    private InfluencerSummaryListData getInfluencerSummaryListData(List<EyDmsCustomerModel> influencerList) {
        InfluencerSummaryListData listData = new InfluencerSummaryListData();
        List<InfluencerSummaryData> summaryDataList = new ArrayList<>();
        influencerList.forEach(influencer -> {
            if(Objects.nonNull(influencer.getNirmanMitraCode())) {
                var influencerData = influencerSummaryConverter.convert(influencer);
                var subAraMappinglist = territoryManagementService.getTerritoriesForCustomer(influencer);
                var salesHistry = networkService.getMitraSalesDataForCustomer(influencer.getNirmanMitraCode(), baseSiteService.getCurrentBaseSite(), EyDmsCoreConstants.DJP.INFLEUNCER_TRANSACTION_TYPE);
                var bagLifted = getSalesQuantity(salesHistry);
                var salesQuantity = (bagLifted / 20);
                influencerData.setBagLifted(bagLifted);
                influencerData.setBagLiftedNo(bagLifted);
                influencerData.setBagLiftedQty(String.valueOf(salesQuantity));
                influencerData.setDaySinceLastLifting(networkService.getDaysSinceLastLifting(salesHistry));
                if(CollectionUtils.isNotEmpty(subAraMappinglist)) {
                    var subareaMaster=subAraMappinglist.get(0);
                    influencerData.setDistrict(subareaMaster.getDistrict());
                    influencerData.setTaluka(subareaMaster.getTaluka());
                }
                summaryDataList.add(influencerData);
            }
        });
        AtomicInteger rank=new AtomicInteger(1);
        summaryDataList.stream().sorted(Comparator.comparing(InfluencerSummaryData::getBagLifted).reversed()).forEach(infdata-> infdata.setRank(rank.getAndIncrement()));
        listData.setInfluncerSummary(summaryDataList);
        return listData;
    }

    @Autowired
    PointRequisitionDao pointRequisitionDao;
    
    private List<InfluencerSummaryData> getPaginatedInfluencerSummaryListData(List<EyDmsCustomerModel> influencerList) {
       List<InfluencerSummaryData> summaryDataList = new ArrayList<>();
        influencerList.forEach(influencer -> {
            if(Objects.nonNull(influencer.getNirmanMitraCode())) {
                var influencerData = influencerSummaryConverter.convert(influencer);
                var subAraMappinglist = territoryManagementService.getTerritoriesForCustomer(influencer);
                var salesHistry = networkService.getMitraSalesDataForCustomer(influencer.getNirmanMitraCode(), baseSiteService.getCurrentBaseSite(), EyDmsCoreConstants.DJP.INFLEUNCER_TRANSACTION_TYPE);
                var bagLifted = getSalesQuantity(salesHistry);
                var salesQuantity = (bagLifted / 20);
                influencerData.setBagLifted(bagLifted);
                influencerData.setBagLiftedNo(bagLifted);
                influencerData.setBagLiftedQty(String.valueOf(salesQuantity));
                influencerData.setDaySinceLastLifting(networkService.getDaysSinceLastLifting(salesHistry));
                if(CollectionUtils.isNotEmpty(subAraMappinglist)) {
                    var subareaMaster=subAraMappinglist.get(0);
                    influencerData.setDistrict(subareaMaster.getDistrict());
                    influencerData.setTaluka(subareaMaster.getTaluka());
                }
                summaryDataList.add(influencerData);
            }
        });
        AtomicInteger rank=new AtomicInteger(1);
        summaryDataList.stream().sorted(Comparator.comparing(InfluencerSummaryData::getBagLifted).reversed()).forEach(infdata-> infdata.setRank(rank.getAndIncrement()));
        return summaryDataList;
    }

 
    private Double getSalesQuantity(List<NirmanMitraSalesHistoryModel> salesHistry) {
        Date todaty=new Date();
        Calendar monthStart=Calendar.getInstance();
        monthStart.set(Calendar.DAY_OF_MONTH,1);
        return salesHistry.stream().filter(sale->sale.getTransactionDate().after(monthStart.getTime()) && sale.getTransactionDate().before(todaty)).mapToDouble(NirmanMitraSalesHistoryModel::getTransactionQuantity).sum();
    }

    @Override
    public Boolean addMeetingSchedule(MeetingScheduleData data) {
        meetScheduleReverseConverter.convert(data);
        return true;
    }

    @Override
    public Boolean saveMeetingAttendance(MeetingScheduleData data) {
        if (Objects.nonNull(data.getCode())){
            var meetSchedule = (MeetingScheduleModel) modelService.get(PK.parse(data.getCode()));
            if(CollectionUtils.isNotEmpty(data.getAttendees())) {
                meetSchedule.setAttendance(data.getAttendees().size());
                meetSchedule.setAttendees(getAttendees(data.getAttendees()));
            }
            if(Objects.nonNull(data.getDate()) && Objects.nonNull(data.getEndTime())){
                LOGGER.info(String.format("updating meeting time for %s and time:%s",data.getCode(),data.getDate()));
                var newStartDateTime=EyDmsDateUtility.parseMeetingDate(data.getDate());
                var newEndDateTime=EyDmsDateUtility.parseMeetingDate(data.getEndTime());
                if(Objects.nonNull(newStartDateTime) && newStartDateTime.after(meetSchedule.getEventDate())) {
                    meetSchedule.setStatus(MeetStatus.POSTPONED);
                    meetSchedule.setEventDate(newStartDateTime);
                    meetSchedule.setEndTime(newEndDateTime);
                }else{
                    meetSchedule.setStatus(MeetStatus.COMPLETED);
                }
            }
            modelService.save(meetSchedule);
            return true;
        }
        return false;
    }
    private List<EyDmsCustomerModel> getAttendees(List<String> attendedCustomers) {
        List<EyDmsCustomerModel> attendees=new ArrayList<>();
        attendedCustomers.forEach(code->{
            var usermodel= userService.getUserForUID(code);
            if(Objects.nonNull(usermodel)){
                attendees.add((EyDmsCustomerModel) usermodel);
            }
        });
        return attendees;
    }
    @Override
    public ProspectiveNetworkListData getPerspectiveNetworkList(String leadType,String searchTerm,
                                                                String dealerCategory,String taluka1, String stage) {
        List<EyDmsCustomerModel> networkCustomerList=new ArrayList<>();
        List<EyDmsCustomerModel> networkCustomers;
            if (DEALER.equals(leadType)) {
                networkCustomers =territoryManagementService.getDealersForSubArea();
                networkCustomerList.addAll(networkCustomers);
            } else if (RETAILER.equals(leadType)) {
                networkCustomers = territoryManagementService.getRetailersForSubArea();
                networkCustomerList.addAll(networkCustomers);
            }

            if(Objects.nonNull(dealerCategory)){
                networkCustomerList=networkService.filterEyDmsCustomersWithDealerCategory(networkCustomerList,dealerCategory);
            }
            if(Objects.nonNull(searchTerm)){
                networkCustomerList=networkService.filterEyDmsCustomersWithSearchTerm(networkCustomerList,searchTerm);
            }
            var finalCustomerList=networkCustomerList;
        ProspectiveNetworkListData listData=new ProspectiveNetworkListData();
        if(CollectionUtils.isNotEmpty(networkCustomerList)) {
            List<ProspectiveNetworkData> networkDataList = new ArrayList<>();

            networkCustomerList.forEach(customer -> {
                var networkModel=networkService.findNetworkByCode(customer.getUid());
                if(Objects.nonNull(networkModel)) {
                    if(Objects.isNull(stage) || stage.equalsIgnoreCase(networkModel.getCounterShareAction().getCode())) {
                        var data = prospectiveNetworkConverter.convert(customer);
                        data.setCounterShareAction(enumerationService.getEnumerationName(networkModel.getCounterShareAction()));
                        data.setNearbyNetwork(getNearbyNetwork(customer, finalCustomerList));
                        networkDataList.add(data);
                    }
                }
            });
            listData.setNetworkDetails(networkDataList);
        }
        return listData;
    }

    @Override
    public DealerVehicleDetailsListData getDealerVehicleDetails() {
        var vehicles= networkService.getAllVehicleDetails();
        DealerVehicleDetailsListData listData=new DealerVehicleDetailsListData();
        listData.setVehicleDetails(dealerVehicleDetailsConverter.convertAll(vehicles));
        return listData;
    }

    @Override
    public SiteStageSummaryListData getSiteStageSummary() {
        SiteStageSummaryListData siteStageSummaryListData = new SiteStageSummaryListData();
        var siteList =
                territoryManagementService.getSitesForSubArea();
        List<SiteStageSummaryData> summaryDataList = new ArrayList<>();
        enumerationService.getEnumerationValues(CurrentStageOfSiteConstruction.class).forEach(siteStage -> {
            SiteStageSummaryData summaryData = new SiteStageSummaryData();
            summaryData.setName(enumerationService.getEnumerationName(siteStage));
            summaryData.setCode(siteStage.getCode());
            summaryData.setCount(getSiteStageCount(siteStage,siteList));
            summaryDataList.add(summaryData);
        });
        siteStageSummaryListData.setSiteStages(summaryDataList);
        return siteStageSummaryListData;
    }

    private Integer getSiteStageCount(CurrentStageOfSiteConstruction siteStage, List<EyDmsCustomerModel> siteList) {
        return (int) siteList.stream().filter(site -> Objects.nonNull(site.getCurrentStageOfConstruction()) && siteStage.equals(site.getCurrentStageOfConstruction())).count();
    }


    @Override
    public SiteDetailListData getSiteDataList(String siteStage, String searchKey) {
        var siteList = territoryManagementService.getSitesForSubArea();
        var sitesForStage=siteList.stream().filter(site->Objects.nonNull(site.getCurrentStageOfConstruction()) && siteStage.equals(site.getCurrentStageOfConstruction().getCode())).collect(Collectors.toList());

        if(StringUtils.isNotBlank(searchKey))
        {
            sitesForStage = networkService.filterEyDmsCustomersWithSearchTerm(siteList,searchKey);
        }

        SiteDetailListData siteListData=new SiteDetailListData();
        siteListData.setSites(siteDataConverter.convertAll(sitesForStage));
        return siteListData;
    }

    @Override
    public SiteDetailListData getSiteDataList(String searchKey) {
        var siteList = territoryManagementService.getSitesForSubAreaSO();
        if(StringUtils.isNotBlank(searchKey))
        {
            siteList = networkService.filterEyDmsCustomersWithSearchTerm(siteList,searchKey);
        }
        SiteDetailListData siteListData=new SiteDetailListData();
        siteListData.setSites(siteDataConverter.convertAll(siteList));
        return siteListData;
    }

    @Override
    public SiteDetailListData getSiteDataMTDList(String searchKey)  {
        var siteList = territoryManagementService.getSitesForSubAreaSO();
        if(StringUtils.isNotBlank(searchKey))
        {
            siteList = networkService.filterEyDmsCustomersWithSearchTerm(siteList,searchKey);
        }
        LOGGER.debug(String.format("Getting New Site Data(MTD) for %s sites",siteList.size()));
        SiteDetailListData siteListData=new SiteDetailListData();
        Date monthBackDate=EyDmsDateUtility.getThreeMonthBackDate();
        var newSiteList=siteList.stream().filter(site->site.getCreationtime().after(monthBackDate)).collect(Collectors.toList());

        siteListData.setSites(siteDataConverter.convertAll(newSiteList));
        siteListData.setSalesCountMTD(getMTDSale(newSiteList));

        return siteListData;
    }

    private double getMTDSale(List<EyDmsCustomerModel> newSiteList) {
        Date monthBackDate=EyDmsDateUtility.getMonthStartDate();
        return newSiteList.stream().filter(site->site.getCreationtime().after(monthBackDate)).mapToDouble(this::getSiteSale).sum();
    }

    private double getSiteSale(EyDmsCustomerModel site) {
        //TODO replace with actual site sale
        if(Objects.nonNull(site.getWholeSale())){
            return site.getWholeSale();
        }
        return 0;
    }


    @Override
    public SiteDetailListData getSiteDataListByCategory(String category, String searchKey){
        LOGGER.info(String.format("Getting Site Data for Category :: %s ",category));
        var siteList = territoryManagementService.getSitesForSubAreaSO();
        var sitesForCategory=siteList.stream().filter(site->Objects.nonNull(site.getSiteCategory()) && site.getSiteCategory().getCode().equalsIgnoreCase(category)).collect(Collectors.toList());

        if(StringUtils.isNotBlank(searchKey) && CollectionUtils.isNotEmpty(sitesForCategory))
        {
            sitesForCategory = networkService.filterEyDmsCustomersWithSearchTerm(siteList,searchKey);
        }

        SiteDetailListData siteListData=new SiteDetailListData();
        siteListData.setSites(siteDataConverter.convertAll(sitesForCategory));
        return siteListData;
    }

    @Override
    public NwUserListData getRetailerDealerSO() {
        List<EyDmsCustomerModel> customerModels=new ArrayList<>();
        var dealers=territoryManagementService.getDealersForSubArea();
        if(Objects.nonNull(dealers)){
            customerModels.addAll(dealers);
        }
        var retailers=territoryManagementService.getRetailersForSubArea();
        if(Objects.nonNull(retailers)){
            customerModels.addAll(retailers);
        }
        NwUserListData userList=new NwUserListData();
        var soUsers=getOtherNetworkSO();
        List<NwUserData> userDataList=new ArrayList<>(soUsers.getUsers());
        customerModels.forEach(user->{
                NwUserData userData = new NwUserData();
                userData.setCode(user.getUid());
                userData.setName(user.getName());
                userData.setIsSOUser(false);
                userDataList.add(userData);
        });
        userList.setUsers(userDataList);
        return userList;
    }

    private List<NwUserData> populateNetworkUserList(List<EyDmsUserModel> users) {
        List<NwUserData> userDataList =new ArrayList<>();
        users.forEach(user->{
            NwUserData userData=new NwUserData();
            userData.setCode(user.getUid());
            userData.setName(user.getName());
            userData.setIsSOUser(true);
            userDataList.add(userData);
        });
        return userDataList;
    }

    @Override
    public NwUserListData getOtherNetworkSO() {
        var eydmsUsers= networkService.getOtherNetworkSO();
        NwUserListData listData=new NwUserListData();
        listData.setUsers(populateNetworkUserList(eydmsUsers));
        return listData;
    }

    @Override
    public EYDMSAddressData getAddressForUserId(String userId) {
        var user= userService.getUserForUID(userId);

        if(Objects.nonNull(user) && CollectionUtils.isNotEmpty(user.getAddresses())) {
            var addressOptional= user.getAddresses().stream().findFirst();
            if (addressOptional.isPresent()) {
                var address=eydmsAddressConverter.convert(addressOptional.get());
                if(Objects.nonNull(address) && Objects.isNull(address.getContactNumber())){
                    address.setContactNumber(((EyDmsCustomerModel)user).getMobileNumber());
                }
                return address;
            }

        }
        return null;
    }

    @Override
    public InactiveNetworkListData getInactiveNetworkList(String category, String searchKey, String taluka) {
        InactiveNetworkListData listData=new InactiveNetworkListData();
        List<EyDmsCustomerModel> users = new ArrayList<>();
        if(EYDMSNetworkFacadeImpl.DEALER.equalsIgnoreCase(category)) {
            users = networkService.getInActiveCustomers(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID);
        }

            if(StringUtils.isNotBlank(searchKey))
            {
                users = networkService.filterEyDmsCustomersWithSearchTerm(users,searchKey);
            }

            List<InactiveNetworkData> networkList=new ArrayList<>();
            users.forEach(user->{
                InactiveNetworkData data=new InactiveNetworkData();
                data.setCode(user.getUid());
                data.setName(user.getName());
                data.setPotential(df.format(user.getCounterPotential()));
                if(Objects.nonNull(user.getReason())) {
                    data.setReason(enumerationService.getEnumerationName(user.getReason()));
                }
                data.setLostSale(df.format(networkService.getLostSaleForCustomer(user)));
                networkList.add(data);
            });
            listData.setUsers(networkList);
        return listData;
    }

    @Override
    public InactiveNetworkListData getDormantList(String networkType,String customerType, String searchKey, String taluka) {
        InactiveNetworkListData listData = new InactiveNetworkListData();
        if (taluka.equalsIgnoreCase("ALL")) {

            List<EyDmsCustomerModel> customerFilteredList = new ArrayList<>();
            B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
            if (currentUser instanceof EyDmsUserModel) {
                if (EYDMSNetworkFacadeImpl.DEALER.equals(customerType)) {
                    customerFilteredList = territoryManagementService.getDealersForSubArea();
                    if (customerFilteredList != null && !customerFilteredList.isEmpty()) {

                       /* customerFilteredList = customerFilteredList.stream().filter(o->o.getNetworkType()!=null).filter(eydmsCustomerModel -> BooleanUtils.isTrue(eydmsCustomerModel.getNetworkType().equalsIgnoreCase("Dormant")))
                                .collect(Collectors.toList());*/

                        customerFilteredList = customerFilteredList.stream().filter(cust -> cust.getNetworkType().equals("Dormant")).collect(Collectors.toList());
                    }

                }

            }
            if (StringUtils.isNotBlank(searchKey)) {
                customerFilteredList = networkService.filterEyDmsCustomersWithSearchTerm(customerFilteredList, searchKey);
            }

            List<InactiveNetworkData> networkList = new ArrayList<>();
            customerFilteredList.forEach(user -> {
                InactiveNetworkData data = new InactiveNetworkData();
                data.setCode(user.getUid());
                data.setName(user.getName());
                data.setPotential(String.valueOf(user.getCounterPotential()));
                if (Objects.nonNull(user.getReason())) {
                    data.setReason(enumerationService.getEnumerationName(user.getReason()));
                }
                data.setLostSale(String.valueOf(networkService.getLostSaleForCustomer(user)));
                networkList.add(data);
            });
            listData.setUsers(networkList);

        }
        else {


            List<EyDmsCustomerModel> customerFilteredList = new ArrayList<>();
            B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
            if (currentUser instanceof EyDmsUserModel) {
                if (EYDMSNetworkFacadeImpl.DEALER.equals(customerType)) {
                    customerFilteredList = territoryManagementService.getDealersForSubArea(taluka);
                    if (customerFilteredList != null && !customerFilteredList.isEmpty()) {

                        customerFilteredList = customerFilteredList.stream().filter(cust -> cust.getNetworkType().equals("Dormant")).collect(Collectors.toList());

                    }

                }

            }
            if (StringUtils.isNotBlank(searchKey)) {
                customerFilteredList = networkService.filterEyDmsCustomersWithSearchTerm(customerFilteredList, searchKey);
            }

            List<InactiveNetworkData> networkList = new ArrayList<>();
            customerFilteredList.forEach(user -> {
                InactiveNetworkData data = new InactiveNetworkData();
                data.setCode(user.getUid());
                data.setName(user.getName());
                data.setPotential(String.valueOf(user.getCounterPotential()));
                if (Objects.nonNull(user.getReason())) {
                    data.setReason(enumerationService.getEnumerationName(user.getReason()));
                }
                data.setLostSale(String.valueOf(networkService.getLostSaleForCustomer(user)));
                networkList.add(data);
            });
            listData.setUsers(networkList);
        }
        return listData;
    }

    @Override
    public InfluencerSummaryListData getInfluencerListForCategory(String category, String searchKey) {
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        List<EyDmsCustomerModel> influencerList=new ArrayList<>();
        if(currentUser instanceof EyDmsCustomerModel){
             influencerList = territoryManagementService.getInfluencerListForDealer();
        }
        else if(currentUser instanceof EyDmsUserModel){
             influencerList = territoryManagementService.getInfluencersForSubArea();
        }

        var influencerForCategory=influencerList.stream().filter(inf-> InfluencerType.valueOf(category).equals(inf.getInfluencerType())).collect(Collectors.toList());

        if(StringUtils.isNotBlank(searchKey))
        {
            influencerForCategory = networkService.filterEyDmsCustomersWithSearchTerm(influencerForCategory,searchKey);
        }

        return getInfluencerSummaryListData(influencerForCategory);
    }

    @Override
    public SPSalesPerformanceListData getSPSalesPerformanceData(String searchKey) {
        var spList = territoryManagementService.getSalesPromotersForSubArea();
        SPSalesPerformanceListData listData = new SPSalesPerformanceListData();
        List<SPSalesPerformanceData> dataList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(spList)) {
            LOGGER.info("populating SP data for splist:" + spList.size());
            if (StringUtils.isNotBlank(searchKey)) {
                spList = networkService.filterEyDmsCustomersWithSearchTerm(spList,searchKey);
            }
            spList.forEach(promoter -> {
                SPSalesPerformanceData data = new SPSalesPerformanceData();
                data.setName(promoter.getName());
                data.setMultiBrand(networkService.isMultiBrand(promoter));
                data.setPotential(networkService.getSPNetworkPotentialMTD(promoter));
                data.setSales(networkService.getSPNetwokSalesMTD(promoter));
                data.setCounterShare(networkService.getSPNetworkShare(promoter));
                dataList.add(data);
            });
        }
        listData.setPerformanceList(dataList);
        return listData;
    }

    @Override
    public DealerDetails360Data getDealerDetails360(String dealerCode,String subArea) {
        LOG.info("Dealer code : "+dealerCode);
        var dealer= (EyDmsCustomerModel)userService.getUserForUID(dealerCode);
        LOG.info("Dealer: "+dealer);
        var details360Data=dealerDetails360Converter.convert(dealer);
        if(Objects.nonNull(details360Data)) {
            details360Data.setOngoingSchemes(getOngoingSchemesData(PartnerLevel.DEALER));
            ProductMixVolumeAndRatioListData productMixVolumeAndRatioListDataMTD = new ProductMixVolumeAndRatioListData();
            ProductMixVolumeAndRatioListData productMixVolumeAndRatioListDataYTD = new ProductMixVolumeAndRatioListData();
            B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
            if (currentUser instanceof EyDmsUserModel) {
                details360Data.setProductMixMTD(salesPerformanceFacade.getProductMixVolumeAndRatioListDataMTDForCustomer((EyDmsUserModel) userService.getCurrentUser(), baseSiteService.getCurrentBaseSite(), productMixVolumeAndRatioListDataMTD, null, null, dealer));
                details360Data.setProductMixYTD(salesPerformanceFacade.getProductMixVolumeAndRatioListDataForYTD((EyDmsUserModel) userService.getCurrentUser(), baseSiteService.getCurrentBaseSite(), productMixVolumeAndRatioListDataYTD, dealer, null, null));
            }
            else if (currentUser instanceof EyDmsCustomerModel) {
                if ((((EyDmsCustomerModel) currentUser).getCounterType() != null) &&
                        (((EyDmsCustomerModel) currentUser).getCounterType().equals(CounterType.SP))) {
                    details360Data.setProductMixMTD(salesPerformanceFacade.getProductwiseSalesPercentRatioAndVolumeRatioForCustomer(dealer,null, null, null));
                    details360Data.setProductMixYTD(salesPerformanceFacade.getProductMixVolumeAndRatioListDataForYTD(null, baseSiteService.getCurrentBaseSite(), productMixVolumeAndRatioListDataYTD, dealer, null, null));
                }
            }
        }


        return details360Data;
    }
    @Override
    public CounterLocationDetailsData getLocationDetails(String dealerCode) {
    	CounterLocationDetailsData data=new CounterLocationDetailsData();
    	EyDmsCustomerModel customer =(EyDmsCustomerModel) userService.getUserForUID(dealerCode);
    	if(customer!=null) {
    		data.setLatitude(customer.getLatitude());
    		data.setLongitude(customer.getLongitude());

            if(customer.getGeoUpdateTime()!=null){
                data.setGeoUpdateTime(customer.getGeoUpdateTime());
            }
    	}
    	return data;
    }
    @Override
    public CounterLocationDetailsData getRequestUpdate(String dealerCode) {
        CounterLocationDetailsData data=new CounterLocationDetailsData();
        List<List<Object>> counterLocationDetails = networkService.getCounterLocationDetails(dealerCode);
        if(!counterLocationDetails.isEmpty()) {
            for (List<Object> objects : counterLocationDetails) {
                data.setName(String.valueOf(objects.get(0)));
                data.setSiteAddressLine1(String.valueOf(objects.get(3)));
                data.setSiteAddressLine2(String.valueOf(objects.get(4)));
                data.setCity(String.valueOf(objects.get(5)));
                data.setTaluka(String.valueOf(objects.get(6)));
                data.setDistrict(String.valueOf(objects.get(7)));
                data.setState(String.valueOf(objects.get(8)));
                data.setPinCode(String.valueOf(objects.get(9)));
            }
        }
        return data;
    }

    @Override
    public boolean submitUpdatedLocationDetails(CounterLocationDetailsData counterLocationDetailsData) {
        return networkService.submitUpdatedLocationDetails(counterLocationDetailsData);
    }

    @Override
    public InfluencersDetails360WsData getInfluencerDetails360(String influencerCode) {
        var influencer= userService.getUserForUID(influencerCode);
        var influencerDetailsData = influencerDetails360Converter.convert((EyDmsCustomerModel) influencer);
        return influencerDetailsData;
    }

    @Override
    public DealerDetailsFormData getDealerDetailsForm(String dealerCode) {
        return dealerDetailsFormConverter.convert((EyDmsCustomerModel) userService.getUserForUID(dealerCode));
    }

    @Override
    public LeadSummaryListData getLeadSummaryList(LeadType leadType, String monthYear, String searchTerm, String leadId) {
        LeadSummaryListData summaryListData = new LeadSummaryListData();
        List<LeadMasterModel> leadList = new ArrayList<>();
        if (Objects.nonNull(leadId)) {
            var lead = eydmsGenericDao.findItemByTypeCodeAndUidParam(LeadMasterModel._TYPECODE, LeadMasterModel.LEADID, leadId);
            if (Objects.nonNull(lead)) {
                leadList.add((LeadMasterModel) lead);
            }
        } else {
            leadList.addAll(networkService.getAllLeads(leadType, monthYear, searchTerm));
        }
        summaryListData.setSummaryList(leadSummaryConverter.convertAll(leadList));
        return summaryListData;
    }

    @Override
    public boolean removeLeadForId(String leadId) {
        var lead=eydmsGenericDao.findItemByTypeCodeAndUidParam(LeadMasterModel._TYPECODE,LeadMasterModel.LEADID,leadId);
        if(Objects.nonNull(lead)){
            modelService.remove(lead);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateLead(String leadId, String leadStage) {
        var lead=(LeadMasterModel)eydmsGenericDao.findItemByTypeCodeAndUidParam(LeadMasterModel._TYPECODE,LeadMasterModel.LEADID,leadId);
        if(Objects.nonNull(lead)){
            lead.setLeadStage(LeadStage.valueOf(leadStage));
            modelService.save(lead);
            return true;
        }
        return false;
    }


    @Override
    public boolean verifyDenyPartner(String uid, String status, String reason) {
        var customerModel= (EyDmsCustomerModel)userService.getUserForUID(uid);
        Set<PrincipalGroupModel> ugSet= new HashSet<>(customerModel.getGroups());
        
        if(ugSet.contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_ONBOARDING_USER_GROUP_UID))){
            if(status.equals(VERIFY)){
                customerModel.setCustomerOnboardingStatus(CustomerOnboardingStatus.APPROVED);
                customerModel.setDealerStageSubStatus(DealerStageSubStatus.APPROVED_BY_SO);
                
            	ugSet.remove(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_ONBOARDING_USER_GROUP_UID));
                ugSet.add(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID));
                customerModel.setOnboardingApprovedBy((B2BCustomerModel)userService.getCurrentUser());
                customerModel.setApprovedBySoDate(new Date());
                customerModel.setGroups(ugSet);
            }else{
                customerModel.setCustomerOnboardingStatus(CustomerOnboardingStatus.REJECTED);
                customerModel.setDealerStageSubStatus(DealerStageSubStatus.REJECTED_BY_SO);
                customerModel.setRejectionReason(reason);
                customerModel.setRejectedBySoDate(new Date());
            }
            modelService.save(customerModel);
            return true;
        }else if(ugSet.contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_ONBOARDING_USER_GROUP_UID))){
            if(status.equals(VERIFY)){
                customerModel.setCustomerOnboardingStatus(CustomerOnboardingStatus.APPROVED);
                customerModel.setRetailerStageSubStatus(RetailerStageSubStatus.APPROVED_BY_SO);
                
                ugSet.remove(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_ONBOARDING_USER_GROUP_UID));
                ugSet.add(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID));
                customerModel.setGroups(ugSet);
                customerModel.setOnboardingApprovedBy((B2BCustomerModel)userService.getCurrentUser());
            }else{
                customerModel.setCustomerOnboardingStatus(CustomerOnboardingStatus.REJECTED);
                customerModel.setRetailerStageSubStatus(RetailerStageSubStatus.REJECTED_BY_SO);
                customerModel.setRejectionReason(reason);
                customerModel.setRejectedBySoDate(new Date());
            }
            modelService.save(customerModel);
            return true;
        } else if (ugSet.contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.INFLUENCER_ONBOARDING_USER_GROUP_UID))) {
            if(status.equals(VERIFY)){
                customerModel.setCustomerOnboardingStatus(CustomerOnboardingStatus.APPROVED);
                
                ugSet.remove(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.INFLUENCER_ONBOARDING_USER_GROUP_UID));
                ugSet.add(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID));
                customerModel.setGroups(ugSet);
                customerModel.setOnboardingApprovedBy((B2BCustomerModel)userService.getCurrentUser());
                try{
                    notifyDealerRegardingApproval(customerModel);
                    notifySORegardingApproval(customerModel);

                }
                catch(Exception e) {
                    LOG.error("Error while sending approval notification");
                }
                customerModel.setCounterType(CounterType.INFLUENCER);
                customerModel.setDateOfJoining(new Date());
                customerModel.setNetworkType(NetworkType.ACTIVE.getCode());
                customerModel.setApprovedBySoDate(new Date());
            }else{
                customerModel.setCustomerOnboardingStatus(CustomerOnboardingStatus.REJECTED);
                customerModel.setRejectionReason(reason);
                customerModel.setRejectedBySoDate(new Date());
                customerModel.setOnboardingRejectedBy((B2BCustomerModel)userService.getCurrentUser());
                try{
                    notifyDealer(customerModel);
                    notifySO(customerModel);

                }
                	catch(Exception e) {
                    LOG.error("Error while sending rejected notification");
                }

            }
            modelService.save(customerModel);
            return true;
        }
        return false;
    }
    public void notifySO(EyDmsCustomerModel eydmsCustomer){
        B2BCustomerModel so = (B2BCustomerModel)userService.getCurrentUser();
        final StringBuilder builder = new StringBuilder();
        builder.append("Influencer name : " + eydmsCustomer.getName()+ "/" +eydmsCustomer.getUid()+ "Influencer type :" +eydmsCustomer.getInfluencerType());
        builder.append("Taluka : " +eydmsCustomer.getTaluka()+ "Rejection reason : " +eydmsCustomer.getRejectionReason());

        //builder.append("Retailer order request no. " + orderRequisition.getRequisitionId() + " by "+ orderRequisition.getToCustomer().getUid() +" with product " +orderRequisition.getProduct().getName() +" and quantity " + orderRequisition.getQuantity() + " bags is pending for " +orderRequisition.getFromCustomer().getUid());
        String body = builder.toString();
        SiteMessageModel notification = modelService.create(SiteMessageModel.class);
        notification.setNotificationType(NotificationType.NOTIFICATION);
        notification.setCategory(NotificationCategory.INFLUENCER_REJECTED);
        notification.setSubject("Influencer onboarding request has been rejected");
        notification.setBody(body);



        SiteMessageForCustomerModel customer = modelService.create(SiteMessageForCustomerModel.class);
        notification.setOwner(so);
        customer.setCustomer(so);
        notification.setType(SiteMessageType.SYSTEM);
        notification.setUid(String.valueOf(siteMessageUidGenerator.generate()));
        notification.setExpiryDate(getExpiryDate());
        customer.setMessage(notification);
        customer.setSentDate(new Date());
        modelService.save(notification);
        modelService.save(customer);

    }

    public void notifySORegardingApproval(EyDmsCustomerModel eydmsCustomer){
        B2BCustomerModel so = (B2BCustomerModel)userService.getCurrentUser();
        final StringBuilder builder = new StringBuilder();
        builder.append("Influencer name : " + eydmsCustomer.getName()+ "/" +eydmsCustomer.getUid()+ "Influencer type :" +eydmsCustomer.getInfluencerType());
        builder.append("Taluka : " +eydmsCustomer.getTaluka());

        //builder.append("Retailer order request no. " + orderRequisition.getRequisitionId() + " by "+ orderRequisition.getToCustomer().getUid() +" with product " +orderRequisition.getProduct().getName() +" and quantity " + orderRequisition.getQuantity() + " bags is pending for " +orderRequisition.getFromCustomer().getUid());
        String body = builder.toString();
        SiteMessageModel notification = modelService.create(SiteMessageModel.class);
        notification.setNotificationType(NotificationType.NOTIFICATION);
        notification.setCategory(NotificationCategory.INFLUENCER_REJECTED);
        notification.setSubject("New influencer has been registered");
        notification.setBody(body);



        SiteMessageForCustomerModel customer = modelService.create(SiteMessageForCustomerModel.class);
        notification.setOwner(so);
        customer.setCustomer(so);
        notification.setType(SiteMessageType.SYSTEM);
        notification.setUid(String.valueOf(siteMessageUidGenerator.generate()));
        notification.setExpiryDate(getExpiryDate());
        customer.setMessage(notification);
        customer.setSentDate(new Date());
        modelService.save(notification);
        modelService.save(customer);

    }
    public void notifyDealer(EyDmsCustomerModel eydmsCustomer){
        B2BCustomerModel dealer = (B2BCustomerModel)eydmsCustomer;
        final StringBuilder builder = new StringBuilder();
        builder.append("Influencer name : " + eydmsCustomer.getName()+ "/" +eydmsCustomer.getUid()+ "Influencer type :" +eydmsCustomer.getInfluencerType());
        builder.append("Taluka : " +eydmsCustomer.getTaluka()+ "Rejection reason : " +eydmsCustomer.getRejectionReason());

        //builder.append("Retailer order request no. " + orderRequisition.getRequisitionId() + " by "+ orderRequisition.getToCustomer().getUid() +" with product " +orderRequisition.getProduct().getName() +" and quantity " + orderRequisition.getQuantity() + " bags is pending for " +orderRequisition.getFromCustomer().getUid());
        String body = builder.toString();
        SiteMessageModel notification = modelService.create(SiteMessageModel.class);
        notification.setNotificationType(NotificationType.NOTIFICATION);
        notification.setCategory(NotificationCategory.INFLUENCER_REJECTED);
        notification.setSubject("Influencer onboarding request has been rejected");
        notification.setBody(body);

        SiteMessageForCustomerModel customer = modelService.create(SiteMessageForCustomerModel.class);
        notification.setOwner(dealer);
        customer.setCustomer(dealer);
        notification.setType(SiteMessageType.SYSTEM);
        notification.setUid(String.valueOf(siteMessageUidGenerator.generate()));
        notification.setExpiryDate(getExpiryDate());
        customer.setMessage(notification);
        customer.setSentDate(new Date());
        modelService.save(notification);
        modelService.save(customer);
    }
    public void notifyDealerRegardingApproval(EyDmsCustomerModel eydmsCustomer){
        B2BCustomerModel dealer = (B2BCustomerModel)eydmsCustomer;
        final StringBuilder builder = new StringBuilder();
        builder.append("Influencer name : " + eydmsCustomer.getName()+ "/" +eydmsCustomer.getUid()+ "Influencer type :" +eydmsCustomer.getInfluencerType());
        builder.append("Taluka : " +eydmsCustomer.getTaluka());

        //builder.append("Retailer order request no. " + orderRequisition.getRequisitionId() + " by "+ orderRequisition.getToCustomer().getUid() +" with product " +orderRequisition.getProduct().getName() +" and quantity " + orderRequisition.getQuantity() + " bags is pending for " +orderRequisition.getFromCustomer().getUid());
        String body = builder.toString();
        SiteMessageModel notification = modelService.create(SiteMessageModel.class);
        notification.setNotificationType(NotificationType.NOTIFICATION);
        notification.setCategory(NotificationCategory.INFLUENCER_REJECTED);
        notification.setSubject("New influencer has been registered");
        notification.setBody(body);

        SiteMessageForCustomerModel customer = modelService.create(SiteMessageForCustomerModel.class);
        notification.setOwner(dealer);
        customer.setCustomer(dealer);
        notification.setType(SiteMessageType.SYSTEM);
        notification.setUid(String.valueOf(siteMessageUidGenerator.generate()));
        notification.setExpiryDate(getExpiryDate());
        customer.setMessage(notification);
        customer.setSentDate(new Date());
        modelService.save(notification);
        modelService.save(customer);
    }

    private Date getExpiryDate(){
        LocalDate date = LocalDate.now().plusDays(30);
        Date expiryDate = null;
        try {
            expiryDate = new SimpleDateFormat("yyyy-MM-dd").parse(String.valueOf(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return expiryDate;
    }

    @Override
    public Map<String, Integer> getCounterInfoForTaluka(String taluka,String leadType) {
        return networkService.getCounterInfoForTaluka(taluka,leadType);
    }

    @Override
    public Map<String, Integer> getNetworkTypeCount(String leadType) {
        return networkService.getNetworkTypeCount(leadType);
    }

    private List<OngoingSchemeDetailsData> getOngoingSchemesData(PartnerLevel dealer) {
        var schemes = networkService.getOnGoingSchemes(dealer);
        if (CollectionUtils.isNotEmpty(schemes)) {
            return ongoingSchemeConverter.convertAll(schemes);
        }
        return Collections.emptyList();
    }

    private String getNearbyNetwork(EyDmsCustomerModel currentCustomer, List<EyDmsCustomerModel> allCustomers) {
        if(Objects.nonNull(currentCustomer.getLatitude()) && Objects.nonNull(currentCustomer.getLongitude())) {
            List<EyDmsCustomerModel> nearbyNetworkList=new ArrayList<>(allCustomers);
            var latitude = currentCustomer.getLatitude();
            var longitude = currentCustomer.getLongitude();
            nearbyNetworkList.remove(currentCustomer);
            Map<String, Double> leadDistanceMap = new HashMap<>();
            nearbyNetworkList.forEach(networkCustomer -> {
                if(Objects.nonNull(networkCustomer.getLatitude()) && Objects.nonNull(networkCustomer.getLongitude())) {
                    var distance = SloppyMath.haversinMeters(latitude, longitude, networkCustomer.getLatitude(), networkCustomer.getLongitude());
                    leadDistanceMap.put(networkCustomer.getName(), distance);
                }
            });
            return leadDistanceMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue()).limit(1).map(Map.Entry::getKey).findFirst().orElse("");
        }
        return "";
    }

    @Override
    public DealerCurrentNetworkListData getDealerCurrentNetworkData(String dealerCategory, String fields, String networkType, String leadType, boolean eydmsExclusiveCustomer, String searchKey){
        BaseSiteModel site =  baseSiteService.getCurrentBaseSite();
        DealerCurrentNetworkListData dealerCurrentNetworkListData=new DealerCurrentNetworkListData();
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        if(leadType.equalsIgnoreCase(DEALER)){
            return networkService.getDealerCurrentNetworkWsData(dealerCategory, fields, networkType, site, leadType, eydmsExclusiveCustomer, searchKey);
        }
        else if(leadType.equalsIgnoreCase(RETAILER)){
            List<EyDmsCustomerModel> retailer=new ArrayList<>();
            /*if(currentUser instanceof EyDmsUserModel) {
                retailer = territoryManagementService.getCustomerByTerritoriesAndCounterTypeWithoutPagination("Retailer", networkType, searchKey, null, null,null);
            }*/
            if((currentUser instanceof EyDmsUserModel) || (((EyDmsCustomerModel)currentUser).getCounterType()!=null && ((EyDmsCustomerModel)currentUser).getCounterType().equals(CounterType.SP))){
                RequestCustomerData requestCustomerData = new RequestCustomerData();
                    requestCustomerData.setCounterType(List.of("Retailer"));
                retailer = territoryManagementService.getCustomerforUser(requestCustomerData);
                if (eydmsExclusiveCustomer) {
                    retailer = retailer.stream().filter(eydmsCustomerModel -> BooleanUtils.isTrue(eydmsCustomerModel.getIsShreeSite()))
                            .collect(Collectors.toList());
                }
                if (Objects.nonNull(networkType)) {
                    retailer = retailer.stream().filter(cust -> networkType.equalsIgnoreCase(cust.getNetworkType())).collect(Collectors.toList());
                }
            }
            dealerCurrentNetworkListData.setDealerCurrentNetworkList(getRetailerDetailedSummaryListData(retailer));
            return dealerCurrentNetworkListData;
        }
        return null;
    }
    private void computeRankForRetailer(List<DealerCurrentNetworkData> currentNetworkWsDataList) {
        AtomicInteger rank=new AtomicInteger(1);
        currentNetworkWsDataList.stream().sorted(Collections.reverseOrder(Comparator.comparing(nw -> nw.getSalesQuantity().getRetailerSaleQuantity()))).forEach(data->data.setRank(String.valueOf(rank.getAndIncrement())));
    }

    @Override
    public String updateTimesContacted(String customerNo, Boolean phoneContacted){
        if(phoneContacted.equals(true)){
            EyDmsCustomerModel eydmsCustomer = eydmsCustomerService.getEyDmsCustomerForUid(customerNo);
            if(Objects.nonNull(eydmsCustomer)) {
                if (Objects.isNull(eydmsCustomer.getTimesContacted())) {
                    eydmsCustomer.setTimesContacted(1);
                } else {
                    var count=eydmsCustomer.getTimesContacted()+1;
                    eydmsCustomer.setTimesContacted(count);
                }
                modelService.save(eydmsCustomer);
                return "SUCCESS";
            }else{
                return "No Customer Found";
            }
        }else{
            return "Phone Contacted Can Not Be FALSE";
        }
    }

    @Override
    public CustomerCardListData getCustomerCards(String dealerCategory, String leadType, String onboardingStatus, String searchKey){
        BaseSiteModel site =  baseSiteService.getCurrentBaseSite();
        var customerList= eydmsCustomerService.getCustomerCards(dealerCategory, leadType, onboardingStatus, site, searchKey);
        CustomerCardListData customerCardListData = new CustomerCardListData();
        customerCardListData.setCustomerCards(customerCardDataConverter.convertAll(customerList));
        return customerCardListData;
    }

    @Override
    public EYDMSPotentialCustomerListData getTopPotentialCustomer(String leadType){
        return networkService.getTopPotentialCustomerListData(leadType);
    }

    @Override
    public Map<String,Integer> getOnboardingCardCount(String subArea, LeadType leadType,String duration){
        Map<String,Integer> customerCardCountMap = new HashMap<>();
        BaseSiteModel site =  baseSiteService.getCurrentBaseSite();
        Date date=getStartDateFromDuration(duration);
       var pendingCustomerList = eydmsCustomerService.getCustomerCards(subArea, leadType.getCode(), CustomerOnboardingStatus.PENDING_FOR_APPROVAL.getCode(), site, null);
       var approvedCustomerList = eydmsCustomerService.getCustomerCards(subArea, leadType.getCode(), CustomerOnboardingStatus.APPROVED.getCode(), site, null);
       var  rejectedCustomerList = eydmsCustomerService.getCustomerCards(subArea, leadType.getCode(), CustomerOnboardingStatus.REJECTED.getCode(), site, null);

       String monthYear=getMonthYearFromDate(date);
        var leadList = networkService.getAllLeads(leadType,monthYear,null);
        var lostLeadsList=leadList.stream().filter(lead->Objects.nonNull(lead.getLeadStage()) && lead.getLeadStage().equals(LeadStage.NOT_INTERESTED)).collect(Collectors.toList());

        Date d=getStartDateFromDurationForPrevMonth(duration);
       //Date from = Date.from(d.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        String previousMonthYear=getMonthYearFromDate(d);
        var leadListPrevMonth = networkService.getAllLeads(leadType,previousMonthYear,null);

        customerCardCountMap.put("notInterestedCount",lostLeadsList.size());
        customerCardCountMap.put("pendingCount",getCardCount(pendingCustomerList,date));
        customerCardCountMap.put("approvedCount",getCardCount(approvedCustomerList,date));
        customerCardCountMap.put("rejectedCount",getCardCount(rejectedCustomerList,date));
        customerCardCountMap.put("leadsGeneratedCount",leadList.size());
        customerCardCountMap.put("leadsGeneratedCountForPreviousMonth",leadListPrevMonth.size());
        return customerCardCountMap;
    }

    private int getCardCount(List<EyDmsCustomerModel> customerModels,Date date) {
        if(Objects.isNull(date)){
            return customerModels.size();
        }
        return (int) customerModels.stream().filter(cust -> cust.getCreationtime().after(date)).count();
    }

    private String getMonthYearFromDate(Date date) {
        return EyDmsDateUtility.getFormattedDate(date,"MMM YYYY");
    }

    private Date getStartDateFromDuration(String duration) {
        LocalDate currentDate = LocalDate.now();
        if("MONTHLY".equalsIgnoreCase(duration)){
            return Date.from(currentDate.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        } else if ("QUARTERLY".equalsIgnoreCase(duration)) {
            int quarterNumber = currentDate.get(IsoFields.QUARTER_OF_YEAR);
            LocalDate firstDayOfQuarter = LocalDate.of(currentDate.getYear(),
                    Month.of((quarterNumber - 1) * 3 + 1), 1);
            return Date.from(firstDayOfQuarter.atStartOfDay(ZoneId.systemDefault()).toInstant());

        } else if ("YEARLY".equalsIgnoreCase(duration)) {
            return Date.from(currentDate.withDayOfYear(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }

    private Date getStartDateFromDurationForPrevMonth(String duration) {
        LocalDate date = LocalDate.now();
        LocalDate currentDate = date.minusMonths(1);
        if("MONTHLY".equalsIgnoreCase(duration)){
            return Date.from(currentDate.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        } else if ("QUARTERLY".equalsIgnoreCase(duration)) {
            int quarterNumber = currentDate.get(IsoFields.QUARTER_OF_YEAR);
            LocalDate firstDayOfQuarter = LocalDate.of(currentDate.getYear(),
                    Month.of((quarterNumber - 1) * 3 + 1), 1);
            return Date.from(firstDayOfQuarter.atStartOfDay(ZoneId.systemDefault()).toInstant());

        } else if ("YEARLY".equalsIgnoreCase(duration)) {
            return Date.from(currentDate.withDayOfYear(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }

    @Override
    public DealerListData getAllDealersForSubArea(String subArea, boolean eydmsExclusiveCustomer) {
        BaseSiteModel site =  baseSiteService.getCurrentBaseSite();
        List<EyDmsCustomerModel> dealerList = networkService.getCustomerListFromSubArea(subArea,site);
        List<EyDmsCustomerModel> filterDealerList=new ArrayList<>();
        if(!eydmsExclusiveCustomer) {
            filterDealerList = dealerList.stream().filter(eydmsCustomerModel -> eydmsCustomerModel.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());
        }else{
            filterDealerList = dealerList.stream().filter(eydmsCustomerModel -> eydmsCustomerModel.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))
                    .filter(eydmsCustomerModel -> BooleanUtils.isTrue(eydmsCustomerModel.getIsexclusive_shree())).collect(Collectors.toList());
        }
        List<CustomerData> dealerData=Optional.of(filterDealerList.stream()
                .map(b2BCustomer -> dealerBasicConverter
                        .convert(b2BCustomer)).collect(Collectors.toList())).get();

        DealerListData dataList = new DealerListData();
        dataList.setDealers(dealerData);
        return dataList;
    }

    @Override
    public DealerListData getAllRetailersForSubArea(String subArea) {
        BaseSiteModel site =  baseSiteService.getCurrentBaseSite();
        List<EyDmsCustomerModel> retailerList = networkService.getCustomerListFromSubArea(subArea,site);
        List<EyDmsCustomerModel>  filterRetailerList = retailerList.stream().filter(eydmsCustomerModel -> eydmsCustomerModel.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))).collect(Collectors.toList());
        List<CustomerData> retailerData=Optional.of(filterRetailerList.stream()
                .map(b2BCustomer -> dealerBasicConverter
                        .convert(b2BCustomer)).collect(Collectors.toList())).get();
        DealerListData dataList = new DealerListData();
        dataList.setDealers(retailerData);
        return dataList;
    }

    @Override
    public DealerListData getProspectiveCustomer(String leadType){
        List<EyDmsCustomerModel> networkCustomerList=new ArrayList<>();
        if(EYDMSNetworkFacadeImpl.DEALER.equals(leadType)){
            networkCustomerList=territoryManagementService.getDealersForSubArea();
        } else if ("RETAILER".equals(leadType)) {
            networkCustomerList=
                    territoryManagementService.getRetailersForSubArea();
        }
        List<CustomerData> retailerData=Optional.of(networkCustomerList.stream()
                .map(b2BCustomer -> dealerBasicConverter
                        .convert(b2BCustomer)).collect(Collectors.toList())).get();
        DealerListData dataList = new DealerListData();
        dataList.setDealers(retailerData);
        return dataList;
    }

    @Override
    public DealerListData getInfluencerCustomers() {
        List<EyDmsCustomerModel> influencerList =
                territoryManagementService.getInfluencersForSubArea();
        List<CustomerData> retailerData=Optional.of(influencerList.stream()
                .map(b2BCustomer -> dealerBasicConverter
                        .convert(b2BCustomer)).collect(Collectors.toList())).get();
        DealerListData dataList = new DealerListData();
        dataList.setDealers(retailerData);
        return dataList;
    }

    @Override
    public ScheduledMeetListData getInfluencerMeetCards(String code,String dateFilter,String searchTerm,String status,String category,String fromDate,String toDate) {
        ScheduledMeetListData meetCardsData=new ScheduledMeetListData();
        List<MeetingScheduleModel> meetingScheduleModels=new ArrayList<>();
        if(Objects.nonNull(code)){
            var meetSchedule=(MeetingScheduleModel)modelService.get(PK.parse(code));
            meetingScheduleModels.add(meetSchedule);
        }else{
            meetingScheduleModels.addAll(networkService.getInfluencerMeetCards());
        }
        List<MeetingScheduleModel> meetSchedules=meetingScheduleModels;
        if(Objects.nonNull(dateFilter)){
            meetSchedules=meetingScheduleModels.stream().filter(meet->compareDates(meet.getEventDate(),dateFilter)).collect(Collectors.toList());
        }
        if(StringUtils.isNotBlank(searchTerm)) {
            meetSchedules = networkService.filterMeetings(meetingScheduleModels, searchTerm);
        }
        if(StringUtils.isNotBlank(status)) {
            meetSchedules = networkService.filterMeetForStatus(meetingScheduleModels, status);
        }
        if(StringUtils.isNotBlank(category)) {
            meetSchedules = networkService.filterMeetForCategory(meetingScheduleModels, category);
        }
        if(Objects.nonNull(fromDate) && Objects.nonNull(toDate)){
            Date startDate=getStartDateFromDate(fromDate);
            Date endDate=getEndDateFromDate(toDate);
            if(Objects.nonNull(startDate) && Objects.nonNull(endDate)) {
                meetSchedules =
                        meetingScheduleModels.stream().filter(meet -> meet.getEventDate().after(startDate) && meet.getEventDate().before(endDate)).collect(Collectors.toList());
            }
            }
        if(CollectionUtils.isNotEmpty(meetSchedules)) {
            meetCardsData.setMeetCards(scheduledMeetConverter.convertAll(meetSchedules));
        }
        return meetCardsData;
    }

    private Date getEndDateFromDate(String toDate) {
        SimpleDateFormat format=new SimpleDateFormat("MMM yyyy");
        Date end;
        try {
            end=format.parse(toDate);
        }catch (Exception e){
            LOGGER.error("erroy while parsing monthYear Passed");
            return null;
        }
        Calendar cal=Calendar.getInstance();
        cal.setTime(end);
        cal.add(Calendar.MONTH,1);
        cal.add(Calendar.DAY_OF_MONTH,-1);
       return cal.getTime();
    }

    private Date getStartDateFromDate(String fromDate) {
        SimpleDateFormat format=new SimpleDateFormat("MMM yyyy");
        Date start;
        try {
            start=format.parse(fromDate);
        }catch (Exception e){
            LOGGER.error("erroy while parsing monthYear Passed");
            return null;
        }
       return start;
    }

    private boolean compareDates(Date eventDate, String dateFilter) {
        SimpleDateFormat format=new SimpleDateFormat("dd MMM yyyy");
        return format.format(eventDate).equals(dateFilter);
    }




    @Override
    public InactiveNetworkData getInactiveNetworkRemovalDetailsForCode(String code) {

        return networkService.getInactiveNetworkRemovalDetailsForCode(code);
    }

    @Override
    public Map<String,Object> getSiteCategoryCount() {
        Map<String,Object> categoryCountMap =new HashMap<>();
        var siteList = territoryManagementService.getSitesForSubAreaSO().stream().filter(site->Objects.nonNull(site.getSiteCategory())).collect(Collectors.toList());
        categoryCountMap.put("ihbName",enumerationService.getEnumerationName(SiteCategory.IHB));
        var ihbSites=siteList.stream().filter(site-> site.getSiteCategory().equals(SiteCategory.IHB)).collect(Collectors.toList());
        categoryCountMap.put("ihbCount",ihbSites.size());
        categoryCountMap.put("ihbShare",EYDMSDataFormatUtil.calculatePercentage(ihbSites.size(),siteList.size()));
        categoryCountMap.put("precastName",enumerationService.getEnumerationName(SiteCategory.PRECAST));
        var precastSites=siteList.stream().filter(site->site.getSiteCategory().equals(SiteCategory.PRECAST)).collect(Collectors.toList());
        categoryCountMap.put("precastCount",precastSites.size());
        categoryCountMap.put("precastShare",EYDMSDataFormatUtil.calculatePercentage(precastSites.size(),siteList.size()));
        categoryCountMap.put("institutionalName",enumerationService.getEnumerationName(SiteCategory.INSTITUTIONAL));
        var institutionalSites=siteList.stream().filter(site->site.getSiteCategory().equals(SiteCategory.INSTITUTIONAL)).collect(Collectors.toList());
        categoryCountMap.put("institutionalCount",institutionalSites.size());
        categoryCountMap.put("institutionalShare",EYDMSDataFormatUtil.calculatePercentage(institutionalSites.size(),siteList.size()));
        return categoryCountMap;
    }

    @Override
    public String getPanFromGST(String gstNumber) {
        if(StringUtils.isNotBlank(gstNumber)) {
             return gstNumber.substring(2, 12);
        }
       return "";
    }

    @Override
    public Map<String,Object> getInfluencerCategoryCount() {
    	Map<String,Object> categoryCountMap =new HashMap<>();
    	B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
    	List<EyDmsCustomerModel> eydmsCustomerModels=new ArrayList<>();
    	if(currentUser instanceof EyDmsUserModel){
    		RequestCustomerData requestCustomerData = new RequestCustomerData();
    		requestCustomerData.setCounterType(List.of("Influencer"));
    		eydmsCustomerModels = territoryManagementService.getCustomerforUser(requestCustomerData);
    	}
    	else if(currentUser instanceof EyDmsCustomerModel){
    		if ((currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
    			eydmsCustomerModels = territoryManagementService.getInfluencerListForDealer();
    		}
    		else if((currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))){
    			eydmsCustomerModels = territoryManagementService.getInfluencerListForRetailer();
    		}
    	}
    	var custList =
    			eydmsCustomerModels.stream().filter(cust->Objects.nonNull(cust.getInfluencerType())).collect(Collectors.toList());
    	categoryCountMap.put("architectName",enumerationService.getEnumerationName(InfluencerType.ARCHITECT));
    	var architectList=custList.stream().filter(cust-> cust.getInfluencerType().equals(InfluencerType.ARCHITECT)).collect(Collectors.toList());
    	categoryCountMap.put("architectCount",architectList.size());
    	categoryCountMap.put("architectShare",EYDMSDataFormatUtil.calculatePercentage(architectList.size(),custList.size()));
    	categoryCountMap.put("engineerName",enumerationService.getEnumerationName(InfluencerType.ENGINEER));
    	var engineerList=custList.stream().filter(cust-> cust.getInfluencerType().equals(InfluencerType.ENGINEER)).collect(Collectors.toList());
    	categoryCountMap.put("engineerCount",engineerList.size());
    	categoryCountMap.put("engineerShare",EYDMSDataFormatUtil.calculatePercentage(engineerList.size(),custList.size()));
    	categoryCountMap.put("masonName",enumerationService.getEnumerationName(InfluencerType.MASON));
    	var masonList=custList.stream().filter(cust-> cust.getInfluencerType().equals(InfluencerType.MASON)).collect(Collectors.toList());
    	categoryCountMap.put("masonCount",masonList.size());
    	categoryCountMap.put("masonShare",EYDMSDataFormatUtil.calculatePercentage(masonList.size(),custList.size()));
    	categoryCountMap.put("contractorName",enumerationService.getEnumerationName(InfluencerType.CONTRACTOR));
    	var contractorList=custList.stream().filter(cust-> cust.getInfluencerType().equals(InfluencerType.CONTRACTOR)).collect(Collectors.toList());
    	categoryCountMap.put("contractorCount",contractorList.size());
    	categoryCountMap.put("contractorShare",EYDMSDataFormatUtil.calculatePercentage(contractorList.size(),custList.size()));
    	return categoryCountMap;
    }

    @Override
    public Map<String, Object> getChrunReasonCount() {
        Map<String,Object> churnReasons=new HashMap<>();
        var netwrokChrunList=networkService.getNetworkRemoval();
        enumerationService.getEnumerationValues(InActivityReason.class).forEach(reason->{
            churnReasons.put(enumerationService.getEnumerationName(reason),getNetworkCountForreason(reason,netwrokChrunList));
                }
        );
        return churnReasons;
    }

    @Override
    public SalesPromoterDetailsData getSpDetails360(String spCode) {
        var dealer= (EyDmsCustomerModel)userService.getUserForUID(spCode);
        return spDetails360Converter.convert(dealer);
    }

    private Object getNetworkCountForreason(InActivityReason reason, List<NetworkRemovalModel> netwrokChrunList) {
return netwrokChrunList.stream().filter(nw->reason.equals(nw.getReason())).count();
    }

    @Override
    public Map<String, Object> getSiteStages() {
        Map<String,Object> siteStageMap =new HashMap<>();
        siteStageMap.put(SiteStage.SLAB.getCode(),enumerationService.getEnumerationName(SiteStage.SLAB));
        siteStageMap.put(SiteStage.LAY_OUT.getCode(),enumerationService.getEnumerationName(SiteStage.LAY_OUT));
        siteStageMap.put(SiteStage.EARTH_WORK.getCode(),enumerationService.getEnumerationName(SiteStage.EARTH_WORK));
        siteStageMap.put(SiteStage.FOUNDATION.getCode(),enumerationService.getEnumerationName(SiteStage.FOUNDATION));
        siteStageMap.put(SiteStage.PLINTH_STAIR_BEAM.getCode(),enumerationService.getEnumerationName(SiteStage.PLINTH_STAIR_BEAM));
        siteStageMap.put(SiteStage.FLOOR.getCode(),enumerationService.getEnumerationName(SiteStage.FLOOR));
        siteStageMap.put(SiteStage.COLUMN.getCode(),enumerationService.getEnumerationName(SiteStage.COLUMN));
        siteStageMap.put(SiteStage.BRICK_WORK.getCode(),enumerationService.getEnumerationName(SiteStage.BRICK_WORK));
        siteStageMap.put(SiteStage.PLASTERING_BATHROOM_TILES.getCode(),enumerationService.getEnumerationName(SiteStage.PLASTERING_BATHROOM_TILES));
        return siteStageMap;
    }

    @Override
    public ChannelStrength getChannelKPIGraphDealerRetailer(String leadType) {
       return networkService.getChannelKPIGraphDealerRetailer(leadType);
    }

    @Override
    public MapProspectiveNetworkDataList getLatitudeLongitudeOfProspectiveNetworkList(String leadType) {
        List<EyDmsCustomerModel> networkCustomerList = new ArrayList<>();
        List<EyDmsCustomerModel> networkCustomers;

            if (EYDMSNetworkFacadeImpl.DEALER.equals(leadType)) {
                networkCustomers =
                        territoryManagementService.getDealersForSubArea();
                networkCustomerList.addAll(networkCustomers);
            } else if ("RETAILER".equals(leadType)) {
                networkCustomers =
                        territoryManagementService.getRetailersForSubArea();
                networkCustomerList.addAll(networkCustomers);
            }

        MapProspectiveNetworkDataList list = new MapProspectiveNetworkDataList();
        MapProspectiveNetworkData listData = new MapProspectiveNetworkData();
        if (CollectionUtils.isNotEmpty(networkCustomerList)) {
            List<MapProspectiveNetworkData> networkDataList = new ArrayList<>();
            for(EyDmsCustomerModel customer: networkCustomerList) {

                List<List<Object>> latitudeLongitudeOfProspectiveNetworkList = networkService.getLatitudeLongitudeOfProspectiveNetworkList(customer.getUid());
                if (!latitudeLongitudeOfProspectiveNetworkList.isEmpty()) {
                    try {

                        listData.setId((latitudeLongitudeOfProspectiveNetworkList.get(0).toString()));
                        listData.setLatitude(Double.valueOf(latitudeLongitudeOfProspectiveNetworkList.get(1).toString()));
                        listData.setLongitude(Double.valueOf(latitudeLongitudeOfProspectiveNetworkList.get(2).toString()));
                        networkDataList.add(listData);
                    } catch (Exception e) {
                        LOG.error(e);
                    }
                }
            }
            list.setMapProspectiveNetworkDataList(networkDataList);
            }
        return list;
    }

    @Override
    public SiteSummaryData getSiteSummaryforNetwork(String customerCode) {

        return networkService.getSiteSummaryforNetwork(customerCode);
    }

    @Override
    public NetworkDealerRetailerCounterShareData getDealerRetailerCounterShareForNetwork( String SOFilter,String taluka) {
        BaseSiteModel site =  baseSiteService.getCurrentBaseSite();
        NetworkDealerRetailerCounterShareData dealerRetailerCounterShareForNetwork = networkService.getDealerRetailerCounterShareForNetwork( SOFilter, site, taluka);
        return dealerRetailerCounterShareForNetwork;
    }

    @Override
    public String getExclusiveDealerPercentage() {
        return networkService.getExclusiveDealerPercentage();
    }

    @Override
    public List<MonthlySalesData> getLastSixMonthSalesForInfluencer(String taluka, String Filter) {
        BaseSiteModel site =  baseSiteService.getCurrentBaseSite();
        return networkService.getLastSixMonthSalesForInfluencer(taluka,site,Filter);

    }

    @Override
    public List<NetworkAdditionData> getNetworkAdditionListDetails(String taluka) {

        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        List<NetworkAdditionData> dataList = new ArrayList<>();
        String leadTypeDealer = "DEALER";
        String leadTypeRetailer = "RETAILER";
        String leadTypeInfluencer = "INFLUENCER";
        NetworkAdditionData retailer = new NetworkAdditionData();
        NetworkAdditionData influencer = new NetworkAdditionData();
        NetworkAdditionData dealer = new NetworkAdditionData();

            if (currentUser instanceof EyDmsUserModel) {
                if(taluka!=null) {
                    dealer = getNetworkAdditionDetails(leadTypeDealer, taluka);
                    retailer = getNetworkAdditionDetails(leadTypeRetailer, taluka);
                    influencer = getNetworkAdditionDetails(leadTypeInfluencer, taluka);
                    dataList.add(dealer);
                    dataList.add(retailer);
                    dataList.add(influencer);
                }
            }

        else if(currentUser instanceof  EyDmsCustomerModel) {
            EyDmsCustomerModel user = (EyDmsCustomerModel) userService.getCurrentUser();
            List<SubAreaMasterModel> subAreaMasterModelList = territoryManagementService.getTerritoriesForCustomer(user);
            if ((currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
                if (CollectionUtils.isNotEmpty(subAreaMasterModelList) && subAreaMasterModelList.get(0) != null) {
                    if (subAreaMasterModelList.get(0).getTaluka() != null) {
                        retailer = getNetworkAdditionDetails(leadTypeRetailer, subAreaMasterModelList.get(0).getPk().toString());
                        influencer = getNetworkAdditionDetails(leadTypeInfluencer, subAreaMasterModelList.get(0).getPk().toString());
                        dataList.add(retailer);
                        dataList.add(influencer);
                    }
                }
            } else if ((currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
                if (CollectionUtils.isNotEmpty(subAreaMasterModelList) && subAreaMasterModelList.get(0) != null) {
                    if (subAreaMasterModelList.get(0).getTaluka() != null) {
                        influencer = getNetworkAdditionDetails(leadTypeInfluencer, subAreaMasterModelList.get(0).getPk().toString());
                        dataList.add(influencer);

                    }
                }
            }
        }

        return dataList;
    }

    @Override
    public SearchPageData<InfluencerSummaryData> getPagniatedInfluencerSummaryList(String searchKey, boolean isNew, String networkType, String influencerType,String dealerCategory, SearchPageData searchPageData) {
        SearchPageData<EyDmsCustomerModel> searchResult = null ;
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();

        if(currentUser instanceof EyDmsUserModel) {
        	RequestCustomerData requestCustomerData = new RequestCustomerData();
        	requestCustomerData.setCounterType(List.of(CounterType.INFLUENCER.getCode()));
        	requestCustomerData.setNetworkType(networkType);
        	requestCustomerData.setIsNew(isNew);
        	requestCustomerData.setInfluencerType(influencerType);
        	requestCustomerData.setDealerCategory(dealerCategory);
        	requestCustomerData.setSearchKey(searchKey);
        	searchResult = territoryManagementService.getCustomerForUserPagination(searchPageData, requestCustomerData);
//        	searchResult = territoryManagementService.getCustomerByTerritoriesAndCounterType(searchPageData, "Influencer", networkType, isNew, searchKey, influencerType, dealerCategory);
         }
         if(currentUser instanceof  EyDmsCustomerModel){
             if((currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))){
            	 searchResult = territoryManagementService.getInfluencerListForDealerPagination(searchPageData, networkType, isNew, searchKey, influencerType, dealerCategory);
             }
             else if((currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))){
            	 searchResult = territoryManagementService.getRetailerListForDealerPagination(searchPageData, networkType, isNew, searchKey, false);
             }
         }
         
        List<InfluencerSummaryData> dataList = new ArrayList<>();
        if (searchResult != null && searchResult.getResults() != null) {
            List<EyDmsCustomerModel> itemWonList = searchResult.getResults();
            dataList = getPaginatedInfluencerSummaryListData(itemWonList);
        }
        final SearchPageData<InfluencerSummaryData> result = new SearchPageData<>();
        result.setPagination(searchResult.getPagination());
        result.setSorts(searchResult.getSorts());
        result.setResults(dataList);
        LOGGER.info(result.getResults());
        return result;
    }

    @Override
    public NewInfluencerRetailerCountData getNewRetailerInfluencerCountMTD(String customerType) {
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
        //EyDmsCustomerModel eydmsCustomerModel = (EyDmsCustomerModel) currentUser;
        NewInfluencerRetailerCountData data = new NewInfluencerRetailerCountData();

        String fromCustomerType = null;
        LocalDate currentDate = LocalDate.now();
        Date startDateForCM = Date.from(currentDate.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDateForCM = Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        Date startDateForPM = Date.from(currentDate.minusMonths(1).withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDateForPM = Date.from(currentDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).atStartOfDay(ZoneId.systemDefault()).toInstant());

        Date doj = Date.from(currentDate.minusDays(90).atStartOfDay(ZoneId.systemDefault()).toInstant());
        System.out.println("DOJ:" + doj);

        System.out.println(startDateForCM + " " + endDateForCM);
        System.out.println(startDateForPM + " " + endDateForPM);
        if (customerType.equalsIgnoreCase(RETAILER)) {
            Integer retailerCountCurrentMonth = networkService.getNewRetailerCountMTD(currentUser, currentBaseSite, startDateForCM, endDateForCM, doj);
            Integer retailerCountPrevMonth = networkService.getNewRetailerCountMTD(currentUser, currentBaseSite, startDateForPM, endDateForPM, doj);
            data.setCountForCurrentMonth(retailerCountCurrentMonth);
            data.setCountForPreviousMonth(retailerCountPrevMonth);
        }
        else if (customerType.equalsIgnoreCase(INFLUENCER)) {
            if (currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                fromCustomerType = "Dealer";
            }
            else if (currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                    fromCustomerType = "Retailer";
                }
                Integer influencerCountCurrentMonth = networkService.getNewInfluencerCountMTD(currentUser, currentBaseSite, startDateForCM, endDateForCM, doj, fromCustomerType);
                Integer influencerCountPrevMonth = networkService.getNewInfluencerCountMTD(currentUser, currentBaseSite, startDateForPM, endDateForPM, doj, fromCustomerType);
                data.setCountForCurrentMonth(influencerCountCurrentMonth);
                data.setCountForPreviousMonth(influencerCountPrevMonth);
            }
        return data;
    }

    @Override
    public NewInfluencerRetailerCountData getRetailerInfluencerCardCountMTD(String customerType,String networkType) {
        int diff=0;
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
        NewInfluencerRetailerCountData data = new NewInfluencerRetailerCountData();

        LocalDate currentDate = LocalDate.now();
        Date startDateForCM = Date.from(currentDate.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDateForCM = Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        /*Date startDateForPM = Date.from(currentDate.minusMonths(1).withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDateForPM = Date.from(currentDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).atStartOfDay(ZoneId.systemDefault()).toInstant());*/

            Integer retailerCountCurrentMonth = networkService.getRetailerInfluencerCardCountMTD(customerType,currentUser, currentBaseSite, startDateForCM, endDateForCM, networkType);
           // Integer retailerCountPrevMonth = networkService.getRetailerInfluencerCardCountMTD(customerType,currentUser, currentBaseSite, startDateForPM, endDateForPM, networkType);
            data.setCountForCurrentMonth(retailerCountCurrentMonth);
            data.setCountForPreviousMonth(0);
            data.setDifference("0");
            /*diff=retailerCountCurrentMonth-retailerCountPrevMonth;
            if(diff>=0){
                data.setDifference("+".concat(String.valueOf(diff)));
            }
            else{
                data.setDifference(String.valueOf(diff));
            }*/

        return data;
    }

    @Override
    public List<MonthlySalesData> getLastSixMonthSalesForDealer(String taluka,String Filter) {
        BaseSiteModel site =  baseSiteService.getCurrentBaseSite();
        return networkService.getLastSixMonthSalesForDealer(taluka,site,Filter);
    }

    @Override
    public List<MonthlySalesData> getLastSixMonthSalesForRetailer(String taluka, String Filter) {
        BaseSiteModel site =  baseSiteService.getCurrentBaseSite();
        return networkService.getLastSixMonthSalesForRetailer(taluka,site,Filter);
    }


    @Override
    public Boolean saveInactiveNetworkRemovalDetails(InactiveNetworkData data) {
        return networkService.saveInactiveNetworkRemovalDetails(data);
    }

    @Override
    public RetailerOnboardListData getOnboardRetailerList(LeadType leadType, String searchKey){
        return networkService.getOnboarderRetailerData(leadType,searchKey);
    }

    @Override
    public SearchPageData<RetailerOnboardDto> getOnboardRetailerListPagination(LeadType leadType, String searchKey, SearchPageData searchPageData) {
        //return networkService.getOnboarderRetailerData(leadType,searchKey);
        return  null;
    }

    @Override
    public MarketMappingSiteDetailSummary getSumOfBalancePotentialMonthConsumption() {
        LOGGER.info(String.format("Getting Site - Market Mapping Data"));
        double balancePotential=0.0;
        double monthlyconsumption=0.0;
        for (EyDmsCustomerModel eydmsCustomerModel : territoryManagementService.getSitesForSubArea()) {
            if(eydmsCustomerModel!=null) {
                if(eydmsCustomerModel.getMonthlyConsumption()!=null && eydmsCustomerModel.getBalancePotential()!=null) {
                    LOGGER.info(String.format("EYDMS Customer Model Sites:%s,%s", String.valueOf(eydmsCustomerModel.getCounterPotential()), String.valueOf(eydmsCustomerModel.getMonthlyConsumption())));
                    balancePotential += eydmsCustomerModel.getCounterPotential();
                    monthlyconsumption += eydmsCustomerModel.getMonthlyConsumption();
                }
            }
        }
        MarketMappingSiteDetailSummary siteListData=new MarketMappingSiteDetailSummary();
        siteListData.setBalancePotential(balancePotential);
        siteListData.setMonthlyConsumption(monthlyconsumption);
        return siteListData;
    }

////////////////////////////////New Dealer Retailer APIS
@Override
public SearchPageData<InfluencerSummaryData> getInfluencerDetailedSummaryList(String searchKey, Boolean isNew,
                                                                              String networkType, String influencerType, String influencerCategory, SearchPageData searchPageData, Boolean includeSales, Boolean includeScheduleMeet, Boolean includeNonEyDmsCustomer) {
    SearchPageData<EyDmsCustomerModel> searchResult = null ;
    B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();

    if(currentUser instanceof EyDmsUserModel) {
        RequestCustomerData requestCustomerData = new RequestCustomerData();
        requestCustomerData.setCounterType(List.of(CounterType.INFLUENCER.getCode()));
        requestCustomerData.setNetworkType(networkType);
        requestCustomerData.setIsNew(isNew);
        requestCustomerData.setInfluencerType(influencerType);
        requestCustomerData.setDealerCategory(influencerCategory);
        requestCustomerData.setIncludeNonEyDmsCustomer(includeNonEyDmsCustomer);
        requestCustomerData.setSearchKey(searchKey);
        searchResult = territoryManagementService.getCustomerForUserPagination(searchPageData, requestCustomerData);
//        	searchResult = territoryManagementService.getCustomerByTerritoriesAndCounterType(searchPageData, "Influencer", networkType, isNew, searchKey, influencerType, influencerCategory);
    }
    if(currentUser instanceof  EyDmsCustomerModel){
        if((currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))){
            searchResult = territoryManagementService.getInfluencerListForDealerPagination(searchPageData, networkType, isNew, searchKey, influencerType, influencerCategory);
        }
        else if((currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))){
            searchResult = territoryManagementService.getInfluencerListForRetailerPagination(searchPageData, networkType, isNew, searchKey, influencerType, influencerCategory);
        }
    }

    List<InfluencerSummaryData> dataList = new ArrayList<>();
    if (searchResult != null && searchResult.getResults() != null) {
        List<EyDmsCustomerModel> itemWonList = searchResult.getResults();
        dataList = getInfluencerDetailedSummaryListData(null,itemWonList, includeSales, includeScheduleMeet);
    }
    final SearchPageData<InfluencerSummaryData> result = new SearchPageData<>();
    result.setPagination(searchResult.getPagination());
    result.setSorts(searchResult.getSorts());
    result.setResults(dataList);
    LOGGER.info(result.getResults());
    return result;
}

	@Override
	public SearchPageData<DealerCurrentNetworkData> getRetailerDetailedSummaryList(String searchKey, Boolean isNew,
			String networkType, SearchPageData searchPageData) {
        SearchPageData<EyDmsCustomerModel> searchResult = null ;
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();

        if(currentUser instanceof EyDmsUserModel) {
        	RequestCustomerData requestCustomerData = new RequestCustomerData();
        	requestCustomerData.setCounterType(List.of(CounterType.RETAILER.getCode()));
        	requestCustomerData.setNetworkType(networkType);
        	requestCustomerData.setIsNew(isNew);
        	requestCustomerData.setSearchKey(searchKey);
        	searchResult = territoryManagementService.getCustomerForUserPagination(searchPageData, requestCustomerData);
        	//searchResult = territoryManagementService.getCustomerByTerritoriesAndCounterType(searchPageData, "Retailer", networkType, isNew, searchKey, null, null);
         }
         if(currentUser instanceof  EyDmsCustomerModel){
             if((currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))){
            	 searchResult = territoryManagementService.getRetailerListForDealerPagination(searchPageData, networkType, isNew, searchKey, false);
             }
         }
         
        List<DealerCurrentNetworkData> dataList = new ArrayList<>();
        if (searchResult != null && searchResult.getResults() != null) {
            List<EyDmsCustomerModel> itemWonList = searchResult.getResults();
            dataList = getRetailerDetailedSummaryListData(itemWonList);
        }
        final SearchPageData<DealerCurrentNetworkData> result = new SearchPageData<>();
        result.setPagination(searchResult.getPagination());
        result.setSorts(searchResult.getSorts());
        result.setResults(dataList);
        LOGGER.info(result.getResults());
        return result;
    }

    @Override
    public NewInfluencerRetailerCountData getNetworkDormantCountCard(String networkType,String customerType) {
        int diff=0;
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
        NewInfluencerRetailerCountData data = new NewInfluencerRetailerCountData();

        LocalDate currentDate = LocalDate.now();
        Date startDateForCM = Date.from(currentDate.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDateForCM = Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        Date startDateForPM = Date.from(currentDate.minusMonths(1).withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDateForPM = Date.from(currentDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).atStartOfDay(ZoneId.systemDefault()).toInstant());

        Integer retailerCountCurrentMonth = networkService.getNetworkDormantCountCard(customerType,currentUser, currentBaseSite, startDateForCM, endDateForCM, networkType);
        Integer retailerCountPrevMonth = networkService.getNetworkDormantCountCard(customerType,currentUser, currentBaseSite, startDateForPM, endDateForPM, networkType);
        data.setCountForCurrentMonth(retailerCountCurrentMonth);
        data.setCountForPreviousMonth(retailerCountPrevMonth);
        diff=retailerCountCurrentMonth-retailerCountPrevMonth;
        if(diff>=0){
            data.setDifference("+".concat(String.valueOf(diff)));
        }
        else{
            data.setDifference(String.valueOf(diff));
        }

        return data;
    }

    @Override
    public List<InfluencerSummaryData> getInfluencerDetailedSummaryListData(MeetingScheduleModel model,List<EyDmsCustomerModel> influencerList, Boolean includeSales, Boolean includeScheduleMeet) {
        Map<String, Double>  mapCurrentYTD = null;
        Map<String, Double>  map = null;
        Map<String, Double>  mapLastYTD = null;
        if(includeSales) {
            String startDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).toString();
            String endDate = LocalDate.now().plusDays(1).toString();
            List<List<Object>> list = pointRequisitionDao.getBagLiftedMTDforInfluencers(influencerList, startDate, endDate,null,null);
            map = list.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                    .collect(Collectors.toMap(each->((EyDmsCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

            LocalDate currentYearCurrentDate= LocalDate.now();
            LocalDate currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear(), Month.APRIL, 1);
            if(currentYearCurrentDate.getMonth().compareTo(Month.APRIL)<0) {
                currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear()-1, Month.APRIL, 1);
            }
            LocalDate lastYearCurrentDate= currentYearCurrentDate.minusYears(1);

            LocalDate lastFinancialYearDate = currentFinancialYearDate.minusYears(1);

            List<List<Object>> currentYTD = pointRequisitionDao.getBagLiftedMTDforInfluencers(influencerList, currentFinancialYearDate.toString(), currentYearCurrentDate.toString(),null,null);

            mapCurrentYTD = currentYTD.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                    .collect(Collectors.toMap(each->((EyDmsCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

            List<List<Object>> lastYTD = pointRequisitionDao.getBagLiftedMTDforInfluencers(influencerList, lastFinancialYearDate.toString(), lastYearCurrentDate.toString(),null,null);

            mapLastYTD = lastYTD.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                    .collect(Collectors.toMap(each->((EyDmsCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));
        }
        Map<String, Double> bagLiftedBefore=new HashMap<>();
        Map<String, Double> bagLiftedAfter=new HashMap<>();
        //influencer meeting data start
        if(includeScheduleMeet) {
            LocalDate evntDate=null,threeMonthBackDate=null,nextMonthDate=null;
            List<List<Object>> bagLiftedBeforedate=new ArrayList<>();
            List<List<Object>> bagLiftedAfterdate=new ArrayList<>();
            if(model!=null && model.getEventDate()!=null) {
                evntDate = model.getEventDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                threeMonthBackDate = evntDate.minusMonths(3);
                nextMonthDate = evntDate.plusMonths(1);

                bagLiftedBeforedate = pointRequisitionDao.getBagLiftedMTDforInfluencers(influencerList, threeMonthBackDate.toString(), evntDate.toString(), null, null);
                bagLiftedAfterdate = pointRequisitionDao.getBagLiftedMTDforInfluencers(influencerList, evntDate.toString(), nextMonthDate.toString(), null, null);
                bagLiftedBefore = bagLiftedBeforedate.stream().filter(each -> each != null && each.size() > 1 && each.get(0) != null && each.get(1) != null)
                        .collect(Collectors.toMap(each -> ((EyDmsCustomerModel) each.get(0)).getUid(), each -> (Double) each.get(1)));

                bagLiftedAfter = bagLiftedAfterdate.stream().filter(each -> each != null && each.size() > 1 && each.get(0) != null && each.get(1) != null)
                        .collect(Collectors.toMap(each -> ((EyDmsCustomerModel) each.get(0)).getUid(), each -> (Double) each.get(1)));
            }
        }
        //influencer meeting data end

        List<InfluencerSummaryData> summaryDataList = new ArrayList<>();
        Map<String, Double> finalBagLiftedBefore = bagLiftedBefore;
        Map<String, Double> finalBagLiftedAfter = bagLiftedAfter;
        if(influencerList!=null && !influencerList.isEmpty())
        {
            for (EyDmsCustomerModel influencer : influencerList) {
                var influencerData = influencerSummaryConverter.convert(influencer);
                var subAraMappinglist = territoryManagementService.getTerritoriesForCustomer(influencer);
                var bagLifted = 0.0;
                if(includeSales) {
                    if(map.containsKey(influencer.getUid())) {
                        bagLifted = map.get(influencer.getUid());
                    }
                    var salesQuantity = (bagLifted / 20);
                    influencerData.setBagLiftedNo(bagLifted);
                    influencerData.setBagLiftedQty(String.valueOf(salesQuantity));
                }
                influencerData.setBagLifted(bagLifted);
                if(influencer.getLastLiftingDate()!=null) {
                    LocalDate today = LocalDate.now();
                    LocalDate transactionDate = influencer.getLastLiftingDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    influencerData.setDaySinceLastLifting(String.valueOf(Math.toIntExact(ChronoUnit.DAYS.between(transactionDate,today))));
                }
                if(CollectionUtils.isNotEmpty(subAraMappinglist)) {
                    var subareaMaster=subAraMappinglist.get(0);
                    influencerData.setDistrict(subareaMaster.getDistrict());
                    influencerData.setTaluka(subareaMaster.getTaluka());
                }
                influencerData.setPotential(Objects.nonNull(influencer.getCounterPotential()) ? String.valueOf(influencer.getCounterPotential()) : "0");

                if(includeSales) {
                    double salesCurrentYear = 0.0;
                    if(mapCurrentYTD.containsKey(influencer.getUid())) {
                        salesCurrentYear = mapCurrentYTD.get(influencer.getUid());
                    }
                    double salesCurrentYearQty = (salesCurrentYear / 20);

                    double salesLastYear = 0.0;
                    if(mapLastYTD.containsKey(influencer.getUid())) {
                        salesLastYear = mapLastYTD.get(influencer.getUid());
                    }
                    double salesLastYearQty = (salesLastYear / 20);
                    influencerData.setSalesYtd(df.format(salesCurrentYearQty));
                    influencerData.setGrowthRate(df.format(getYearToYearGrowth(salesCurrentYearQty,salesLastYearQty)));
                }
                //influencer meeting data start
                if(includeScheduleMeet) {
                    double bagLiftedAfterData=0.0,bagLiftedBeforeData=0.0;
                    if(finalBagLiftedBefore.containsKey(influencer.getUid())) {
                        bagLiftedBeforeData = finalBagLiftedBefore.get(influencer.getUid());
                    }
                    if(finalBagLiftedAfter.containsKey(influencer.getUid())) {
                        bagLiftedAfterData = finalBagLiftedAfter.get(influencer.getUid());
                    }
                    influencerData.setBagLiftedBefore(bagLiftedBeforeData);
                    influencerData.setBagLiftedAfter(bagLiftedAfterData);
                    influencerData.setGrowth(getGrowth(bagLiftedBeforeData, bagLiftedAfterData));
                    if (model !=null && MeetStatus.COMPLETED.equals(model.getStatus())) {
                        if (model.getAttendees().contains(influencer)) {
                            influencerData.setAttended(true);
                        }
                    }else{
                        influencerData.setAttended(false);
                    }
                }
                //influencer meeting data end

                summaryDataList.add(influencerData);
            }
        }
        AtomicInteger rank=new AtomicInteger(1);
        summaryDataList.stream().sorted(Comparator.comparing(InfluencerSummaryData::getBagLifted).reversed()).forEach(infdata-> infdata.setRank(rank.getAndIncrement()));
        return summaryDataList;
    }

	    @Autowired
	    OrderRequisitionDao orderRequistionDao;
    private String getGrowth(double bagLiftedBefore, double bagLiftedAfter) {
        DecimalFormat df = new DecimalFormat("#.#");
        return df.format(((bagLiftedAfter-bagLiftedBefore)/bagLiftedBefore)* 100L);
    }
	    @Override
	    public List<DealerCurrentNetworkData> getRetailerDetailedSummaryListData(List<EyDmsCustomerModel> retailerList) {
	    	String startDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).toString();
	    	String endDate = LocalDate.now().toString();
            String startDateLastMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).minusMonths(1).toString();
            String endDateLastMonth = LocalDate.now().minusMonths(1).toString();

            List<List<Object>> list = orderRequistionDao.getSalsdMTDforRetailer(retailerList, startDate, endDate,null,null);
	    	Map<String, Double>  map = list.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
	    	.collect(Collectors.toMap(each->((EyDmsCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

            List<List<Object>> listLastMonth = orderRequistionDao.getSalsdMTDforRetailer(retailerList, startDateLastMonth, endDateLastMonth,null,null);
            Map<String, Double>  mapLastMonth = listLastMonth.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                    .collect(Collectors.toMap(each->((EyDmsCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

	        LocalDate currentYearCurrentDate= LocalDate.now();
	        LocalDate currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear(), Month.APRIL, 1);
	        if(currentYearCurrentDate.getMonth().compareTo(Month.APRIL)<0) {
	            currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear()-1, Month.APRIL, 1);
	        }
	        LocalDate lastYearCurrentDate= currentYearCurrentDate.minusYears(1);//2022-04-02

	        LocalDate lastFinancialYearDate = currentFinancialYearDate.minusYears(1);//2022-04-01
	        
	    	List<List<Object>> currentYTD = orderRequistionDao.getSalsdMTDforRetailer(retailerList, currentFinancialYearDate.toString(), currentYearCurrentDate.toString(),null,null);
	    	Map<String, Double>  mapCurrentYTD = currentYTD.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
	    	.collect(Collectors.toMap(each->((EyDmsCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));
	    		    	
	    	List<List<Object>> lastYTD = orderRequistionDao.getSalsdMTDforRetailer(retailerList, lastFinancialYearDate.toString(), lastYearCurrentDate.toString(),null,null);

	    	Map<String, Double>  mapLastYTD = lastYTD.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
	    	.collect(Collectors.toMap(each->((EyDmsCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));
	    		    	
	        List<DealerCurrentNetworkData> summaryDataList = new ArrayList<>();
	        retailerList.forEach(retailer -> {
	        	DealerCurrentNetworkData dealerCurrentNetworkData = new DealerCurrentNetworkData();
	        	var subAraMappinglist = territoryManagementService.getTerritoriesForCustomer(retailer);
	        	dealerCurrentNetworkData.setCode(retailer.getUid());
                if(retailer.getContactNumber()!=null){
                    dealerCurrentNetworkData.setContactNumber(retailer.getMobileNumber());
                }
	        	dealerCurrentNetworkData.setName(retailer.getName());
                B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
                double salesMtd=0.0,salesQuantity=0.0,salesQuantityLastMonth=0.0,salesMtdLastMonth=0.0,salesLastYearQty=0.0,salesLastYear=0.0,salesCurrentYearQty=0.0,salesCurrentYear=0.0;
                if(currentUser instanceof EyDmsUserModel) {
                    if (retailer.getCounterPotential() != null) {
                        dealerCurrentNetworkData.setPotential(String.valueOf(retailer.getCounterPotential()));
                    } else {
                        dealerCurrentNetworkData.setPotential("0");
                    }
                }else{
                    if (retailer.getCounterPotential() != null) {
                        dealerCurrentNetworkData.setPotential(String.valueOf(retailer.getCounterPotential() / 20));
                    } else {
                        dealerCurrentNetworkData.setPotential("0");
                    }
                }

	            if(map.containsKey(retailer.getUid())) {
	            	salesMtd = map.get(retailer.getUid());
	            }

                if(mapLastMonth.containsKey(retailer.getUid())) {
                      if(mapLastMonth.get(retailer.getUid())!=null){
                        salesMtdLastMonth = mapLastMonth.get(retailer.getUid());
                     }
                }
                if(currentUser instanceof EyDmsUserModel) {
                    salesQuantity = salesMtd;
                    salesQuantityLastMonth = salesMtdLastMonth ;
                }else{
                    salesQuantity = (salesMtd / 20);
                    salesQuantityLastMonth = (salesMtdLastMonth / 20);
                }
	            SalesQuantityData sales = new SalesQuantityData();
	            sales.setRetailerSaleQuantity(salesQuantity);
                sales.setCurrent(salesQuantity);
                sales.setLastMonth(salesQuantityLastMonth);
	            dealerCurrentNetworkData.setSalesQuantity(sales);

                if(currentUser instanceof EyDmsUserModel) {
                    if (retailer.getLastLiftingDate() != null) {
                        LocalDate today = LocalDate.now();
                        LocalDate transactionDate = retailer.getLastLiftingDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        dealerCurrentNetworkData.setDaySinceLastOrder(String.valueOf(Math.toIntExact(ChronoUnit.DAYS.between(transactionDate, today))));
                    } else {
                        dealerCurrentNetworkData.setDaySinceLastOrder(String.valueOf("-"));
                    }
                }
                else {
                    if (retailer.getLastLiftingDate() != null) {
                        EyDmsCustomerModel retailerSalesForDealer = salesPerformanceDao.getRetailerSalesForDealer(retailer, baseSiteService.getCurrentBaseSite());
                        if (retailerSalesForDealer != null) {
                            LocalDate today = LocalDate.now();
                            LocalDate transactionDate = retailer.getLastLiftingDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                            dealerCurrentNetworkData.setDaySinceLastOrder(String.valueOf(Math.toIntExact(ChronoUnit.DAYS.between(transactionDate, today))));
                        } else {
                            dealerCurrentNetworkData.setDaySinceLastOrder(String.valueOf("-"));
                        }
                    }
                }

	            if(mapCurrentYTD.containsKey(retailer.getUid())) {
	            	salesCurrentYear = mapCurrentYTD.get(retailer.getUid());
	            }

	            if(mapLastYTD.containsKey(retailer.getUid())) {
	            	salesLastYear = mapLastYTD.get(retailer.getUid());
	            }
                if(currentUser instanceof EyDmsUserModel) {
                    salesCurrentYearQty = (salesCurrentYear);
                    salesLastYearQty = (salesLastYear);
                }else{
                    salesCurrentYearQty = (salesCurrentYear / 20);
                    salesLastYearQty = (salesLastYear / 20);
                }

	            dealerCurrentNetworkData.setSalesYtd(df.format(salesCurrentYearQty));
	            dealerCurrentNetworkData.setGrowthRate(df.format(getYearToYearGrowth(salesCurrentYearQty,salesLastYearQty)));
	            if(CollectionUtils.isNotEmpty(subAraMappinglist)) {
                    var subareaMaster=subAraMappinglist.get(0);
                    dealerCurrentNetworkData.setDistrict(subareaMaster.getDistrict());
                    dealerCurrentNetworkData.setTaluka(subareaMaster.getTaluka());
                }
	            summaryDataList.add(dealerCurrentNetworkData);           
	        });
	        AtomicInteger rank=new AtomicInteger(1);
	        summaryDataList.stream().sorted(Comparator.comparing(nw -> nw.getSalesQuantity().getRetailerSaleQuantity())).forEach(data->data.setRank(String.valueOf(rank.getAndIncrement())));
	        return summaryDataList;
	    }
	    
	    private Double getYearToYearGrowth(double salesCurrentYearQty, double salesLastYearQty){
	       if(salesLastYearQty>0) {
	            return   (((salesCurrentYearQty- salesLastYearQty) / salesLastYearQty) * 100);
	        }
	        return 0.0;
	    }

		@Override
		public List<EYDMSImageData> getOnboardingFormsSS(String uid) {
			return networkService.getOnboardingFormsSS(uid);
		}


        @Override
        public SalesHistoryData getSalesHistoryDataForNetworkInfluencer360(String influencerCode){
            var influencer= userService.getUserForUID(influencerCode);
            return networkService.getSalesHistoryDataForNetworkInfluencer360((EyDmsCustomerModel) influencer);
        }

        @Override
        public Integer getLeadsGeneratedCountedForInfluencer(String filter) {
            EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
            BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
            return networkService.getLeadsGeneratedCountedForInfluencer(filter,currentUser,brand);
        }


}
