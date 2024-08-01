package com.scl.core.services.impl;

import com.google.common.util.concurrent.AtomicDouble;
import com.scl.core.constants.SclCoreConstants;
import com.scl.core.customer.services.SclCustomerService;
import com.scl.core.dao.*;
import com.scl.core.enums.*;
import com.scl.core.model.*;
import com.scl.core.order.dao.OrderValidationProcessDao;
import com.scl.core.region.dao.DistrictMasterDao;
import com.scl.core.services.*;
import com.scl.core.util.SCLDataFormatUtil;
import com.scl.core.utility.SclDateUtility;
import com.scl.facades.data.*;
import com.scl.facades.visit.data.SiteSummaryData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.notificationservices.model.SiteMessageModel;
import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class NetworkServiceImpl implements NetworkService {

    private static final Logger LOGGER = Logger.getLogger(NetworkServiceImpl.class);
    private static final Logger LOG = Logger.getLogger(NetworkServiceImpl.class);

    DecimalFormat df = new DecimalFormat("#0.00");
    private static final String DATE_FORMAT="yyyy-MM-dd";
    private static final String ZERO = " 0";
    private static final String DEALER = "DEALER";
    private static final String RETAILER = "RETAILER";

    private static final String INFLUENCER = "INFLUENCER";
    private static final String SITE = "SITE";
    private static final String TPC = "TPC";
    private static final String RETAILER_USER_GROUP_UID = "SclRetailerGroup";

    @Resource
    private SclUserDao sclUserDao;
    @Resource
    private NetworkDao networkDao;
    @Autowired
    DJPVisitService djpVisitService;
    @Autowired
    UserService userService;
    @Resource
    private OrderRequisitionDao orderRequisitionDao;
    @Autowired
    private NetworkService networkService;
    @Autowired
    private FlexibleSearchService flexibleSearchService;
    @Autowired
    Converter<SclCustomerModel, DealerCurrentNetworkData> currentNetworkDetailsConverter;

    public SalesPerformanceDao getSalesPerformanceDao() {
        return salesPerformanceDao;
    }

    public void setSalesPerformanceDao(SalesPerformanceDao salesPerformanceDao) {
        this.salesPerformanceDao = salesPerformanceDao;
    }

    @Resource
    private SalesPerformanceDao salesPerformanceDao;

    @Resource
    private TerritoryManagementService territoryManagementService;

    @Resource
    private BaseSiteService baseSiteService;
    @Resource
    private SchemeDetailsDao schemeDetailsDao;

    @Resource
    ModelService modelService;

    @Resource
    EnumerationService enumerationService;

    @Resource
    SclCustomerService sclCustomerService;
    @Autowired
    SalesPerformanceService salesPerformanceService;

    @Autowired
    DJPVisitDao djpVisitDao;

    @Autowired
    DealerDao dealerDao;

    @Autowired
    TerritoryMasterService territoryMasterService;

    @Resource
    CounterVisitMasterDao counterVisitDao;

    @Autowired
    SessionService sessionService;

    @Autowired
    private SearchRestrictionService searchRestrictionService;

    @Resource
    SclSalesSummaryService sclSalesSummaryService;

    @Resource
    private SalesPlanningService salesPlanningService;

    @Resource
    OrderValidationProcessDao orderValidationProcessDao;

    public CounterVisitMasterDao getCounterVisitDao() {
        return counterVisitDao;
    }

    public void setCounterVisitDao(CounterVisitMasterDao counterVisitDao) {
        this.counterVisitDao = counterVisitDao;
    }

    @Resource
    CollectionDao collectionDao;

    @Resource
    AmountFormatService amountFormatService;

    @Resource
    TechnicalAssistanceService technicalAssistanceService;

    @Autowired
    DistrictMasterDao districtMasterDao;

    @Autowired
    TerritoryManagementDao territoryManagementDao;

    /**
     * Return Dealer Current Network List
     * @param site
     * @return
     */
    @Override
    public DealerCurrentNetworkListData getDealerCurrentNetworkWsData(String dealerCategory, String fields, String networkType, BaseSiteModel site, String leadType, boolean sclExclusiveCustomer, String searchKey, List<String> doList,
                                                                      List<String> subAreaList, List<String> territoryList){

        DealerCurrentNetworkListData dealerCurrentNetworkListData = new DealerCurrentNetworkListData();
        List<DealerCurrentNetworkData> currentNetworkWsDataList = new ArrayList<>();
        List<SclCustomerModel> customerFilteredList=new ArrayList<>();
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        if((currentUser instanceof SclUserModel) || (((SclCustomerModel)currentUser).getCounterType()!=null && ((SclCustomerModel)currentUser).getCounterType().equals(CounterType.SP))){
            customerFilteredList = salesPerformanceService.getCustomersByLeadType(leadType, territoryList, subAreaList, doList);

            if (sclExclusiveCustomer) {
                //if(Objects.nonNull(sclCustomer.getDefaultB2BUnit()) && sclCustomer.getDefaultB2BUnit().getUid().equalsIgnoreCase(SclCoreConstants.B2B_UNIT.SCL_SHREE_UNIT_UID))
                customerFilteredList = customerFilteredList.stream().filter(sclCustomer->Objects.nonNull(sclCustomer.getDefaultB2BUnit()) && sclCustomer.getDefaultB2BUnit().getUid().equalsIgnoreCase(SclCoreConstants.B2B_UNIT.SCL_SHREE_UNIT_UID)).collect(Collectors.toList());
            }
        }
        else if(currentUser instanceof SclCustomerModel){
            if(leadType.equalsIgnoreCase(RETAILER)){
                customerFilteredList =
                        territoryManagementService.getRetailerListForDealer();
            }
        }

   	if(StringUtils.isNotBlank(dealerCategory)){
    		customerFilteredList=filterSclCustomersWithDealerCategory(customerFilteredList,dealerCategory);
    	}
    	if (StringUtils.isNotBlank(searchKey)) {
    		customerFilteredList = filterSclCustomersWithSearchTerm(customerFilteredList,searchKey);
    	}
    	if (Objects.nonNull(networkType)) {
    		customerFilteredList = customerFilteredList.stream().filter(cust -> networkType.equalsIgnoreCase(cust.getNetworkType())).collect(Collectors.toList());
    	}
    	/*for (SclCustomerModel customerModel : customerFilteredList) {
    		if(Objects.nonNull(customerModel.getCustomerNo())) {
    			DealerCurrentNetworkData dealerCurrentNetworkData = new DealerCurrentNetworkData();
    			dealerCurrentNetworkData.setCode(customerModel.getUid());
    			dealerCurrentNetworkData.setName(customerModel.getName());
    			dealerCurrentNetworkData.setCustomerNo(customerModel.getCustomerNo());
    			dealerCurrentNetworkData.setPotential(Objects.nonNull(customerModel.getCounterPotential()) ? String.valueOf(customerModel.getCounterPotential()) : ZERO);

    				dealerCurrentNetworkData.setCounterShare(String.valueOf(getCounterShareForDealer(customerModel, site)));
    				//target
                    double target = salesPerformanceService.getMonthlySalesTargetForDealer(customerModel,baseSiteService.getCurrentBaseSite(),null);
    				//double target = sclUserDao.getCustomerTarget(customerModel.getUid(), SclDateUtility.getMonth(new Date()), SclDateUtility.getYear(new Date()));
    				dealerCurrentNetworkData.setTarget(df.format(target));
                    dealerCurrentNetworkData.setSalesQuantity(salesPerformanceService.setSalesQuantityForCustomer(customerModel, leadType,  territoryList));
    				double totalOutstanding = djpVisitDao.getDealerOutstandingAmount(customerModel.getCustomerNo());
    				//dealerCurrentNetworkData.setOutstandingAmount(df.format(totalOutstanding));
                    // dealerCurrentNetworkData.setOutstandingAmount(String.valueOf(totalOutstanding));
                    dealerCurrentNetworkData.setOutstandingAmount(df.format(totalOutstanding));

    				dealerCurrentNetworkData.setGrowthRate(df.format(salesPerformanceService.getYearToYearGrowthForDealer(customerModel)));
    				dealerCurrentNetworkData.setDaySinceLastOrder(String.valueOf(getDaysFromLastOrder(customerModel.getOrders())));



                List<List<Object>> districtAndTaluka = territoryMasterService.getDistrictAndTalukaForCustomer(customerModel);
                if (districtAndTaluka!=null && CollectionUtils.isNotEmpty(districtAndTaluka)) {
                    for (List<Object> list : districtAndTaluka) {
                        if(list.get(0)!=null){
                            dealerCurrentNetworkData.setTaluka(StringUtils.isNotBlank(String.valueOf(list.get(0))) ? String.valueOf(list.get(0)) : "");
                        }
                        if(list.get(1)!=null) {
                            dealerCurrentNetworkData.setDistrict(StringUtils.isNotBlank(String.valueOf(list.get(1))) ? String.valueOf(list.get(1)) : "");
                        }
                    }
                }
    			*//*List<SubAreaMasterModel> subAreaMasterModelList = territoryManagementService.getTerritoriesForCustomer(customerModel);
    			if (CollectionUtils.isNotEmpty(subAreaMasterModelList) && Objects.nonNull(subAreaMasterModelList.get(0))) {
    				dealerCurrentNetworkData.setDistrict(subAreaMasterModelList.get(0).getDistrict());
    				dealerCurrentNetworkData.setTaluka(subAreaMasterModelList.get(0).getTaluka());

    			}*//*
    			currentNetworkWsDataList.add(dealerCurrentNetworkData);
    		}
    	}*/
        if(CollectionUtils.isNotEmpty(customerFilteredList)){
            List<DealerCurrentNetworkData> convertedList = currentNetworkDetailsConverter.convertAll(customerFilteredList);
            currentNetworkWsDataList.addAll(convertedList);
        }

    	 if(leadType.equalsIgnoreCase(DEALER)){
    		computeRankForDealer(currentNetworkWsDataList);
    	}
    	List<DealerCurrentNetworkData> collect = currentNetworkWsDataList.stream().sorted(Comparator.comparing(DealerCurrentNetworkData::getName)).collect(Collectors.toList());
    	dealerCurrentNetworkListData.setDealerCurrentNetworkList(collect);
    	return dealerCurrentNetworkListData;
    }

    private void computeRankForDealer(List<DealerCurrentNetworkData> currentNetworkWsDataList) {
        AtomicInteger rank=new AtomicInteger(1);
        currentNetworkWsDataList.stream().sorted(Collections.reverseOrder(Comparator.comparing(nw -> nw.getSalesQuantity().getActual()))).forEach(data->data.setRank(String.valueOf(rank.getAndIncrement())));
    }

    private void computeRankForRetailer(List<DealerCurrentNetworkData> currentNetworkWsDataList) {
        AtomicInteger rank=new AtomicInteger(1);
        currentNetworkWsDataList.stream().sorted(Collections.reverseOrder(Comparator.comparing(nw -> nw.getSalesQuantity().getRetailerSaleQuantity()))).forEach(data->data.setRank(String.valueOf(rank.getAndIncrement())));
    }

    private double getCounterShareForRetailer(SclCustomerModel customerModel, List<NirmanMitraSalesHistoryModel> salesHistry) {
        Calendar cal=Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.DAY_OF_MONTH,1);
        Date startTime=cal.getTime();
        cal.add(Calendar.MONTH,1);
        cal.add(Calendar.DAY_OF_MONTH,-1);
        Date end=new Date();
        var baglifted=salesHistry.stream().filter(sale->sale.getTransactionDate().after(startTime) && sale.getTransactionDate().before(end)).mapToDouble(NirmanMitraSalesHistoryModel::getTransactionQuantity).sum();
        var counterPotential=customerModel.getCounterPotential();
        var saleQuantity=baglifted/20;
        if(Objects.nonNull(counterPotential)) {
            return (saleQuantity * 100) / counterPotential;
        }
        return 0.0;
    }

    @Override
    public String getDaysSinceLastLifting(List<NirmanMitraSalesHistoryModel> salesHistry) {
        Optional<NirmanMitraSalesHistoryModel> salesModel = salesHistry.stream().max(Comparator.comparing(NirmanMitraSalesHistoryModel::getTransactionDate));
        if(salesModel.isPresent()) {
            LocalDate today = LocalDate.now();
            LocalDate transactionDate = salesModel.get().getTransactionDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return String.valueOf(Math.toIntExact(ChronoUnit.DAYS.between(transactionDate,today)));
        }
        return "-";
    }

    @Override
    public List<MeetingScheduleModel> filterMeetings(List<MeetingScheduleModel> meetingScheduleModels, String searchTerm) {
        List<MeetingScheduleModel> filteredList = new ArrayList<>();
        meetingScheduleModels.forEach(meet -> {
                    if (CollectionUtils.isNotEmpty(filterSclCustomersWithSearchTerm(new ArrayList<>(meet.getCustomers()), searchTerm))) {
                        filteredList.add(meet);
                    }
                }

        );
        return filteredList.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public Integer getOnboarderCustomer(String leadType, String taluka) {
        Date timestamp=SclDateUtility.getFirstDayOfFinancialYear();
        var customers=networkDao.getOnboardedCustomerTillDate(getGroupForLead(leadType),taluka,timestamp,baseSiteService.getCurrentBaseSite());
        LOGGER.info(String.format("Custoemrs : %s", String.valueOf(customers.size())));
        return customers.size();
    }

    @Override
    public NetworkDealerRetailerCounterShareData getDealerRetailerCounterShareForNetwork(String SOFilter, BaseSiteModel site,String taluka, List<String> doList,  List<String> subAreaList,  List<String> territoryList) {
        NetworkDealerRetailerCounterShareData data = new NetworkDealerRetailerCounterShareData();
        double counterPotential = 0.0, actualTarget = 0.0;
        double finalCounterPotential = 0.0;
        int achievementPercentage = 0;
        double behindTarget = 0.0, aheadTarget = 0.0;
        List<SclCustomerModel> customerFilteredList = new ArrayList<>();

        CounterShareResponseData responseData=null;
        double sumOfNumeratorSales=0.0,sumOfPotential=0.0;

        Double counterSharePercent = null;
        customerFilteredList=salesPerformanceService.getCustomersByLeadType(DEALER,territoryList,null,null);
        customerFilteredList=salesPerformanceService.getCustomersByTerritoryCode(customerFilteredList,territoryList);
        CounterShareData counterShareData=new CounterShareData();

        counterShareData.setMonth(LocalDate.now().getMonthValue());
        counterShareData.setYear(LocalDate.now().getYear());
            if (customerFilteredList != null && !customerFilteredList.isEmpty()) {
                for (SclCustomerModel customer : customerFilteredList) {
                    counterShareData.setDealerCode(customer.getUid());
                    responseData = salesPerformanceService.getCounterShareData(counterShareData);
                    if(Objects.nonNull(responseData) && responseData.getPotential()>0){
                        sumOfNumeratorSales+=responseData.getNumeratorSales();
                        sumOfPotential+=responseData.getPotential();
                    }
                    LOGGER.info(" Counter Share"+responseData.getCounterShare() + " Potential:"+responseData.getPotential()+ " Numerator:"+responseData.getNumeratorSales()+ " SelfBrandSale"+responseData.getSelfBrandSale()+ " TotalSales "+responseData.getTotalSales());
                }
            }


            data.setPotential(String.valueOf(sumOfPotential).concat("MT"));
            data.setActual(String.valueOf(sumOfNumeratorSales).concat("MT"));



       /* if (SOFilter.equalsIgnoreCase(RETAILER)) {
            customerFilteredList = salesPerformanceService.getCustomersByLeadType(SOFilter,territoryList,subAreaList,doList);
            if (CollectionUtils.isNotEmpty(customerFilteredList)) {
                for (SclCustomerModel customerModel : customerFilteredList) {
                    double sales = 0.0, salesQuantityMTD = 0.0;
                    List<SclCustomerModel> customerModelList = new ArrayList<>();
                    customerModelList.add(customerModel);
                    LocalDate firstDayOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
                    LocalDate lastDayOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);
                    if(CollectionUtils.isNotEmpty(customerFilteredList)){
                        for (SclCustomerModel sclCustomerModel : customerFilteredList) {
                            Map<String, Double> salesQuantityForRetailerByMTD = networkService.getSalesQuantityForRetailerByMTD(sclCustomerModel, null, null);
                            if(salesQuantityForRetailerByMTD!=null){
                                Double quantityInMT = salesQuantityForRetailerByMTD.get("quantityInMT");
                                salesQuantityMTD+=quantityInMT;
                            }
                        }
                        actualTarget=salesQuantityMTD;
                    }else {
                        actualTarget = 0.0;
                    }

                    List<List<Object>> salesList = orderRequisitionDao.getSalsdMTDforRetailer(customerModelList, firstDayOfMonth.toString(), lastDayOfMonth.toString(), doList, subAreaList);
                    if (salesList != null && !salesList.isEmpty()) {
                        for (List<Object> objects : salesList) {
                            if (objects.get(1) != null) {
                                sales = Double.parseDouble(objects.get(1).toString());
                                salesQuantityMTD = sales / 20;
                                actualTarget += salesQuantityMTD;
                            }
                        }
                    } else {
                        actualTarget = 0.0;
                    }
                    if (Objects.nonNull(customerModel.getCounterPotential())) {
                        counterPotential += customerModel.getCounterPotential();
                    }
                }
            }
        } else if (SOFilter.equalsIgnoreCase(DEALER)) {
            customerFilteredList = salesPerformanceService.getCustomersByLeadType(SOFilter,territoryList,subAreaList,doList);
            if (CollectionUtils.isNotEmpty(customerFilteredList)) {
                for (SclCustomerModel customerModel : customerFilteredList) {
                    SalesQuantityData salesQuantityData = salesPerformanceService.setSalesQuantityForCustomer(customerModel, SOFilter, territoryList);
                    Double actual = salesQuantityData.getActual();
                    actualTarget += actual;
                    if (Objects.nonNull(customerModel.getCounterPotential())) {
                        counterPotential += customerModel.getCounterPotential();
                    }
                }
            }
        }
        data.setPotential(String.valueOf(counterPotential).concat("MT"));
        data.setActual(String.valueOf(actualTarget).concat("MT"));


        if (actualTarget != 0.0 && counterPotential != 0.0)
            achievementPercentage = (int) ((actualTarget / counterPotential) * 100);
        data.setAchievementPercentage(achievementPercentage);
        */

        if (sumOfNumeratorSales != 0.0 && sumOfPotential != 0.0) {
            achievementPercentage = (int) ((sumOfNumeratorSales / sumOfPotential) * 100);
            data.setAchievementPercentage(achievementPercentage);
        } else {
            data.setAchievementPercentage(0);
        }
        if (achievementPercentage < 100) {
            behindTarget = sumOfPotential - sumOfNumeratorSales;
            if (behindTarget != 0.0) {
                data.setBehindTotalTarget(String.valueOf(behindTarget).concat("MT Behind Target"));
            } else {
                data.setBehindTotalTarget(String.valueOf(0).concat("MT Equal Target"));
            }
        } else if (achievementPercentage > 100) {
            aheadTarget = sumOfNumeratorSales - sumOfPotential;
            if (aheadTarget != 0.0)
                data.setAheadTotalTarget(String.valueOf(aheadTarget).concat("MT Ahead Target"));
            else
                data.setAheadTotalTarget(String.valueOf(0).concat("MT Equal Target"));
        }
        return data;
    }

    @Override
    public List<MeetingScheduleModel> filterMeetForStatus(List<MeetingScheduleModel> meetingScheduleModels, String statusFilter) {
        return meetingScheduleModels.stream().filter(meet->Objects.nonNull(meet.getStatus()) && meet.getStatus().getCode().equals(statusFilter)).collect(Collectors.toList());
    }

    @Override
    public List<MeetingScheduleModel> filterMeetForCategory(List<MeetingScheduleModel> meetingScheduleModels, String categoryFilter) {
        return meetingScheduleModels.stream().filter(meet->Objects.nonNull(meet.getCategory()) && meet.getCategory().getCode().equals(categoryFilter)).collect(Collectors.toList());
    }

    @Override
    public List<SclCustomerModel> getFilteredCustomerForNetworkType(List<SclCustomerModel> customerModels, String networkType) {
        return customerModels.stream().filter(cust->networkType.equalsIgnoreCase(cust.getNetworkType())).collect(Collectors.toList());
    }

    @Override
    public List<SclCustomerModel> getFilteredInfluencerForCategory(List<SclCustomerModel> customerModels, String category) {
        return customerModels.stream().filter(cust->Objects.nonNull(cust.getInfluencerType()) && category.equalsIgnoreCase(cust.getInfluencerType().getCode())).collect(Collectors.toList());
    }

    @Override

    public List<SclCustomerModel> filterSclCustomersWithDealerCategory(List<SclCustomerModel> customerModels, String dealerCategory) {
        return customerModels.stream().filter(cust -> Objects.nonNull(cust.getDealerCategory()) && dealerCategory.equals(cust.getDealerCategory().getCode())).collect(Collectors.toList());
    }
    public List<List<Object>> getCounterLocationDetails(String dealerCode) {
        List<List<Object>> counterLocationDetails = networkDao.getCounterLocationDetails(dealerCode);
        return counterLocationDetails;

    }

    @Override
    public boolean submitUpdatedLocationDetails(CounterLocationDetailsData counterLocationDetailsData) {
        if(counterLocationDetailsData!=null) {
            SclCustomerModel sclCustomer = (SclCustomerModel) userService.getUserForUID(counterLocationDetailsData.getId());
            sclCustomer.setLatitude(counterLocationDetailsData.getLatitude());
            sclCustomer.setLongitude(counterLocationDetailsData.getLongitude());
            modelService.save(sclCustomer);
        }
        return true;
    }

    @Override
    public String getExclusiveDealerPercentage(List<String> territoryList) {

        List<SclCustomerModel> customerFilteredList=new ArrayList<>();
        List<SclCustomerModel> exclusiveCustomerFilteredList=new ArrayList<>();
        int count=0;
        int exclusivecount = 0;
        List<TerritoryMasterModel> territoryMasterModels= (List<TerritoryMasterModel>) territoryMasterService.getCurrentTerritory();
        LOGGER.info(String.format("territoryMasterModels:: %s",territoryMasterModels));
        customerFilteredList=salesPerformanceDao.getCurrentNetworkCustomers("DEALER",null,territoryMasterModels,null,false,false,false);
        LOGGER.info("Exclusive Customer all count:"+customerFilteredList.size());
        if(CollectionUtils.isNotEmpty(customerFilteredList)) {
            count = customerFilteredList.size();
            exclusiveCustomerFilteredList= salesPerformanceDao.getCurrentNetworkCustomers("DEALER",null,territoryMasterModels,null,true,false,false);
            exclusivecount =exclusiveCustomerFilteredList.size();
            LOGGER.info("Exclusive Customer shree count:"+exclusivecount);
            return String.valueOf((exclusivecount / count) * 100) + "%";
        }else{
            return null;
        }
    }

    @Override
    public List<InviteesData> getInviteesForMeeting(MeetingScheduleModel meet, List<SclCustomerModel> customers) {
        List<InviteesData> inviteesDataList=new ArrayList<>();
        if(Objects.nonNull(customers)) {
            customers.forEach(cust -> {
                InviteesData invitee = new InviteesData();
                invitee.setCode(cust.getUid());
                invitee.setName(cust.getName());
                //TODO Set reason
                if (MeetStatus.COMPLETED.equals(meet.getStatus())) {
                    if (meet.getAttendees().contains(cust)) {
                        invitee.setAttended(true);
                    }
                    if (Objects.nonNull(cust.getNirmanMitraCode())) {
                        var salesHistry = getMitraSalesDataForCustomer(cust.getNirmanMitraCode(), baseSiteService.getCurrentBaseSite(), SclCoreConstants.DJP.INFLEUNCER_TRANSACTION_TYPE);
                        var bagLiftedBefore = getBagLiftedBeforeMeet(salesHistry, meet.getEventDate());
                        var bagLiftedAfter = getBagLiftedAfterMeet(salesHistry, meet.getEventDate());
                        invitee.setBagLiftedBefore(bagLiftedBefore);
                        invitee.setBagLiftedAfter(bagLiftedAfter);
                        invitee.setGrowth(getGrowth(bagLiftedBefore, bagLiftedAfter));
                    }
                }
                inviteesDataList.add(invitee);
            });
        }
        return inviteesDataList;
    }

    @Override
    public String isMultiBrand(SclCustomerModel promoter) {
        var dealers=getDealersForSalesPromoter(promoter);
        var brandList=
                dealers.stream().filter(dealer->Objects.nonNull(dealer.getDefaultB2BUnit())).map(dealer->dealer.getDefaultB2BUnit().getId()).distinct().collect(Collectors.toList());

        return brandList.size()>1?"Yes":"No";
    }
    private double getTwoDeimalPlaceFormat(double value){
        return  new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
    @Override
    public List<MonthlySalesData> getLastSixMonthSalesForDealer(String taluka,BaseSiteModel site, String Filter,List<String> territoryList) {
        final SclUserModel sclUser = (SclUserModel) userService.getCurrentUser();
        List<MonthlySalesData> dataList = new ArrayList<>();
        List<SclCustomerModel> customerFilteredList = new ArrayList<>();
        LocalDate currentMonth = LocalDate.now();
        LocalDate lastMonth = currentMonth.minusMonths(1);
        Date currentDate = Date.from(currentMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
        var monthYear = SclDateUtility.getFormattedDate(currentDate, "MMM-YYYY");
        BaseSiteModel currentBaseSite=baseSiteService.getCurrentBaseSite();
        CustomerCategory category = CustomerCategory.TR;
        Calendar cal = Calendar.getInstance();
        Date cDate = cal.getTime();

        Date startDate = null;
        Date endDate = null;
        cal.set(Calendar.MONTH, 2);
        cal.set(Calendar.DATE, 31);
        cal.set(Calendar.HOUR, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        List<SclCustomerModel> dealers=new ArrayList<>();
        if(sclUser!=null){
            dealers=salesPerformanceService.getCustomersByLeadType(DEALER,null,null,null);
        }
        List<B2BCustomerModel> dealersList = new ArrayList<>(dealers);

        LOG.info(String.format("Dealer size:%s",dealers.size()));
        //dealersList= Collections.singletonList((B2BCustomerModel) dealers);

        Date financialYrEndDate = cal.getTime();

        if (Filter.equalsIgnoreCase("MTD")) {

            //    if (taluka.equalsIgnoreCase("ALL")) {

            List<Double> monthlySale = new ArrayList<>();
            List<Double> lastMonthWiseList = new ArrayList<>();
            List<Double> listOfCurrentMonthSalesTarget = new ArrayList<>();
            List<Double> listOfLastMonthSalesTarget = new ArrayList<>();
            List<Double> growthList=new ArrayList<>();

            LocalDate date = LocalDate.now();
            for (int i = 0; i <= 6; i++) {
                LocalDate lastMonth1 = date.minusMonths(i);
                int month = lastMonth1.getMonthValue();
                int year = lastMonth1.getYear();

                  /*  Double monthSale = salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYear(sclUser, currentBaseSite, month, year, null, null);
                    Double lastMonthWiseSale = salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYear(sclUser, currentBaseSite,lastMonth1.getMonthValue(), lastMonth1.getYear(),null,null);*/

                Double monthSale = sclSalesSummaryService.getCurrentMonthSales(dealersList,month,year,territoryList);
                LOG.info(String.format("Dealers monthSale:%s",monthSale));
                Double lastMonthWiseSale =  sclSalesSummaryService.getCurrentMonthSales(dealersList,lastMonth1.getMonthValue(), lastMonth1.getYear(),territoryList);
                LOG.info(String.format("Dealers lastMonthWiseSale:%s",lastMonthWiseSale));

                Double currentMonthSalesTarget = salesPerformanceService.getMonthlySalesTargetForDealerList(dealersList,baseSiteService.getCurrentBaseSite(),month,year);
                LOG.info(String.format("Dealers currentMonthSalesTarget:%s",currentMonthSalesTarget));
                Double lastMonthSalesTarget = salesPerformanceService.getMonthlySalesTargetForDealerLastMonth(dealersList,baseSiteService.getCurrentBaseSite(),lastMonth1.getMonthValue(),lastMonth1.getYear());


                // Double currentMonthSalesTarget = networkDao.getSalesTarget(CounterType.DEALER.getCode(), date.getMonthValue(), date.getYear());
                //Double lastMonthSalesTarget = networkDao.getSalesTarget(CounterType.DEALER.getCode(), lastMonth1.getMonthValue(), lastMonth1.getYear());
                LOG.info(String.format("Dealers lastMonthSalesTarget:%s",lastMonthSalesTarget));

                monthlySale.add(monthSale);
                lastMonthWiseList.add(lastMonthWiseSale);
                listOfCurrentMonthSalesTarget.add(currentMonthSalesTarget);
                listOfLastMonthSalesTarget.add(lastMonthSalesTarget);
            }

            for (int i = 0; i <= 5; i++) {
                MonthlySalesData data = new MonthlySalesData();
                LocalDate lastMonth2 = date.minusMonths(i);
                int month = lastMonth2.getMonthValue();
                int year = lastMonth2.getYear();

                data.setMonthYear(getMonthName(month - 1).concat(" ").concat(String.valueOf(year)));

                double currentMonthSales = lastMonthWiseList.get(i);
                double lastMonthSales = lastMonthWiseList.get(i + 1);
                double monthWiseAvgSale = monthlySale.get(i);
                double lastMonthAvgSale = lastMonthWiseList.get(i);

                  //* to do - optimize *//*
                    if (date.getMonthValue() != 0 && monthWiseAvgSale != 0)
                        data.setActualSales(getTwoDeimalPlaceFormat(monthWiseAvgSale));
                    else
                        data.setActualSales(0.0);

                    if (lastMonth2.getMonthValue() != 0 && lastMonthAvgSale != 0)
                        data.setActualSales(getTwoDeimalPlaceFormat(lastMonthAvgSale));
                    else
                        data.setActualSales(0.0);
                    LOG.info(String.format("Dealers Actual:%s",data.getActualSales()));


                data.setGrowth(currentMonthSales - lastMonthSales);
                LOG.info(String.format("Dealers Growth:%s",data.getGrowth()));

                double monthlySalesTarget = listOfCurrentMonthSalesTarget.get(i);
                double lastMonthSalesTarget = listOfLastMonthSalesTarget.get(i);

                 if (date.getMonthValue() != 0 && monthlySalesTarget != 0.0)
                        data.setTargetSales(getTwoDeimalPlaceFormat(monthlySalesTarget));
                    else
                        data.setTargetSales(0.0);

                    if (lastMonth.getMonthValue() != 0 && lastMonthSalesTarget != 0.0)
                        data.setTargetSales(getTwoDeimalPlaceFormat(lastMonthSalesTarget));
                    else
                        data.setTargetSales(0.0);

                    if (monthlySalesTarget > 0) {
                        data.setPercentage(getTwoDeimalPlaceFormat(currentMonthSales / monthlySalesTarget) * 100);
                    }else {
                        data.setPercentage(0.0);
                    }
                    LOG.info(String.format("Dealers Percentage:%s",data.getPercentage()));
                    dataList.add(data);


                if (month != 0)
                    --month;
                else {
                    month = 11;
                    --year;
                }
            }

            /*}
            else
            {
                List<Double> monthlySale = new ArrayList<>();
                List<Double> lastMonthWiseList = new ArrayList<>();
                List<Double> listOfCurrentMonthSalesTarget = new ArrayList<>();
                List<Double> listOfLastMonthSalesTarget = new ArrayList<>();
                LocalDate date = LocalDate.now();
                for (int i = 0; i <= 6; i++) {
                    LocalDate lastMonth1 = date.minusMonths(i);
                    int month = lastMonth1.getMonthValue();
                    int year = lastMonth1.getYear();

                    Double monthSale = salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYear(sclUser, currentBaseSite, month, year, null, null);
                    Double lastMonthWiseSale = salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYear(sclUser, currentBaseSite,lastMonth1.getMonthValue(), lastMonth1.getYear(),null,null);


                    Double currentMonthSalesTarget = networkDao.getSalesTargetForTaluka(taluka,CounterType.DEALER.getCode(), date.getMonthValue(), date.getYear());
                    Double lastMonthSalesTarget = networkDao.getSalesTargetForTaluka(taluka,CounterType.DEALER.getCode(), lastMonth1.getMonthValue(), lastMonth1.getYear());
                    monthlySale.add(monthSale);
                    lastMonthWiseList.add(lastMonthWiseSale);
                    listOfCurrentMonthSalesTarget.add(currentMonthSalesTarget);
                    listOfLastMonthSalesTarget.add(lastMonthSalesTarget);
                }

                for (int i = 0; i <= 5; i++) {
                    MonthlySalesData data = new MonthlySalesData();
                    LocalDate lastMonth2 = date.minusMonths(i);
                    int month = lastMonth2.getMonthValue();
                    int year = lastMonth2.getYear();

                    data.setMonthYear(getMonthName(month - 1).concat(" ").concat(String.valueOf(year)));

                    double currentMonthSales = lastMonthWiseList.get(i);
                    double lastMonthSales = lastMonthWiseList.get(i + 1);
                    double monthWiseAvgSale = monthlySale.get(i);
                    double lastMonthAvgSale = lastMonthWiseList.get(i);

                    //* to do - optimize *//*
                    if (date.getMonthValue() != 0 && monthWiseAvgSale != 0)
                        data.setActualSales(monthWiseAvgSale);
                    else
                        data.setActualSales(0.0);

                    if (lastMonth2.getMonthValue() != 0 && lastMonthAvgSale != 0)
                        data.setActualSales(lastMonthAvgSale);
                    else
                        data.setActualSales(0.0);

                    double growth = currentMonthSales - lastMonthSales;
                    if (growth >= 0.0)
                        data.setGrowth(growth);
                    else
                        data.setGrowth(0.0);

                    double monthlySalesTarget = listOfCurrentMonthSalesTarget.get(i);
                    double lastMonthSalesTarget = listOfLastMonthSalesTarget.get(i);

                    if (date.getMonthValue() != 0 && monthlySalesTarget != 0.0)
                        data.setTargetSales(monthlySalesTarget);
                    else
                        data.setTargetSales(0.0);

                    if (lastMonth.getMonthValue() != 0 && lastMonthSalesTarget != 0.0)
                        data.setTargetSales(lastMonthSalesTarget);
                    else
                        data.setTargetSales(0.0);
                    data.setPercentage(0.0);
                    dataList.add(data);

                    if (month != 0)
                        --month;
                    else {
                        month = 11;
                        --year;
                    }
                }

            }*/
        }
        else if (Filter.equalsIgnoreCase("YTD")) {
            var salesOfficer = (SclUserModel) userService.getCurrentUser();
            // if (taluka.equalsIgnoreCase("ALL")) {
            int currentYr = 0;
            int previousYr = 0;

            if (currentDate.before(financialYrEndDate)) {
                endDate = financialYrEndDate;
                cal.setTime(endDate);

                currentYr = cal.get(Calendar.YEAR);

                cal = Calendar.getInstance();
                cal.add(Calendar.YEAR, -1);
                cal.set(Calendar.MONTH, 3);
                cal.set(Calendar.DATE, 1);
                cal.set(Calendar.HOUR, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);

                startDate = cal.getTime();

                previousYr = cal.get(Calendar.YEAR);

            } else {
                cal.setTime(financialYrEndDate);
                cal.add(Calendar.YEAR, 1);
                endDate = cal.getTime();
                currentYr = cal.get(Calendar.YEAR);

                cal.add(Calendar.YEAR, -1);
                cal.set(Calendar.MONTH, 3);
                cal.set(Calendar.DATE, 1);
                cal.set(Calendar.HOUR, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);

                startDate = cal.getTime();

                previousYr = cal.get(Calendar.YEAR);

            }


            List<Double> yearWiseSales = new ArrayList<>();
            List<Double> yearWiseTarget = new ArrayList<>();

            int startYear = 0, endYear = 0;
            if (LocalDate.now().getMonth().getValue() >= 4) {
                endYear = LocalDate.now().getYear() + 1;//24
                startYear = LocalDate.now().getYear();
            } else {
                startYear = LocalDate.now().getYear() - 1;
                endYear = LocalDate.now().getYear();
            }
               /* endYear=LocalDate.now().getYear();//24
                startYear=endYear-1;//23*/
            for (int i = 0; i <= 3; i++) {
                startYear = startYear - i;
                endYear = endYear - i;
                double currentSales = 0.0, targetSalesAnnual = 0.0;
                //currentSales = salesPerformanceDao.getActualTargetForSalesYTD(salesOfficer,site,startDate,endDate,null,null);
                currentSales = sclSalesSummaryService.getCurrentFySalesForSelectedYear(dealersList, startYear, endYear);
                StringBuilder f = new StringBuilder();
                String financialYear = String.valueOf(f.append(startYear).append("-").append(endYear));
                targetSalesAnnual = salesPerformanceDao.getAnnualSalesTarget(sclUser, financialYear, territoryList);


                    /*cal.setTime(startDate);
                    cal.add(Calendar.YEAR, -1);
                    startDate = cal.getTime();

                    cal.setTime(endDate);
                    cal.add(Calendar.YEAR, -1);
                    endDate = cal.getTime();*/
                yearWiseSales.add(currentSales);
                yearWiseTarget.add(targetSalesAnnual);
            }


            if (LocalDate.now().getMonth().getValue() >= 4) {
                endYear = LocalDate.now().getYear() + 1;//24
                startYear = LocalDate.now().getYear();
            } else {
                startYear = LocalDate.now().getYear() - 1;
                endYear = LocalDate.now().getYear();
            }
            for (int i = 0; i <= 2; i++) {
                MonthlySalesData data = new MonthlySalesData();
                double currentYrSales = yearWiseSales.get(i);
                double previousYrSales = yearWiseSales.get(i + 1);
                double currentYearTarget = yearWiseTarget.get(i);
                data.setActualSales(getTwoDeimalPlaceFormat(currentYrSales));
                //if (previousYrSales != 0.0) {
                data.setGrowth(getTwoDeimalPlaceFormat(currentYrSales) - getTwoDeimalPlaceFormat(previousYrSales));
                //}
                LOG.info("Growth YTD" + data.getGrowth());
                StringBuilder f = new StringBuilder();
                String financialYear = String.valueOf(f.append(startYear - i).append("-").append(endYear - i));
                data.setMonthYear(financialYear);
                data.setTargetSales(currentYearTarget);
                if (currentYearTarget > 0) {
                    data.setPercentage(getTwoDeimalPlaceFormat((currentYrSales / currentYearTarget) * 100));
                } else {
                    data.setPercentage(0.0);
                }
                LOG.info("Percentage YTD" + data.getPercentage());
                dataList.add(data);
            }
        }
            //}
            /*else {
                int currentYr = 0;
                int previousYr = 0;

                if(currentDate.before(financialYrEndDate))
                {
                    endDate = financialYrEndDate;
                    cal.setTime(endDate);

                    currentYr = cal.get(Calendar.YEAR);

                    cal = Calendar.getInstance();
                    cal.add(Calendar.YEAR, -1);
                    cal.set(Calendar.MONTH, 3);
                    cal.set(Calendar.DATE, 1);
                    cal.set(Calendar.HOUR, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);

                    startDate = cal.getTime();

                    previousYr = cal.get(Calendar.YEAR);

                }
                else
                {
                    cal.setTime(financialYrEndDate);
                    cal.add(Calendar.YEAR, 1);
                    endDate = cal.getTime();
                    currentYr = cal.get(Calendar.YEAR);

                    cal.add(Calendar.YEAR, -1);
                    cal.set(Calendar.MONTH, 3);
                    cal.set(Calendar.DATE, 1);
                    cal.set(Calendar.HOUR, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);

                    startDate = cal.getTime();

                    previousYr = cal.get(Calendar.YEAR);

                }

                List<Double> yearWiseSales = new ArrayList<>();


                for (int i = 0; i <= 3; i++) {
                    double currentSales = 0.0;
                    currentSales = salesPerformanceDao.getActualTargetForSalesYTD(salesOfficer,site,startDate,endDate,null,null);


                    cal.setTime(startDate);
                    cal.add(Calendar.YEAR, -1);
                    startDate = cal.getTime();

                    cal.setTime(endDate);
                    cal.add(Calendar.YEAR, -1);
                    endDate = cal.getTime();

                    yearWiseSales.add(currentSales);
                }

                for (int i = 0; i <= 2; i++) {
                    MonthlySalesData data = new MonthlySalesData();

                    data.setMonthYear(Filter);
                    double currentYrSales = yearWiseSales.get(i);
                    double previousYrSales = yearWiseSales.get(i + 1);

                    data.setActualSales(currentYrSales);
                    if (previousYrSales != 0.0) {
                        data.setGrowth(((currentYrSales / previousYrSales) * 100));
                    }

                    String year = Integer.toString((previousYr - i)).concat("-").concat(Integer.toString((currentYr - i)));
                    data.setMonthYear(year);

                    dataList.add(data);
                }

            }*/

        return dataList;
    }




    @Override
    public List<MonthlySalesData> getLastSixMonthSalesForRetailer(String taluka, BaseSiteModel site, String Filter, List<String> territoryList, List<String> districtList, List<String> subAreaList) {
        List<MonthlySalesData> dataList = new ArrayList<>();
        final B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        List<SclCustomerModel> customers = new ArrayList<>();
        List<SclCustomerModel> dealersList = new ArrayList<>();
        if (!Objects.isNull(currentUser)) {
            if(currentUser instanceof SclUserModel) {
                customers = salesPerformanceService.getCustomersByLeadType(RETAILER, territoryList, subAreaList, districtList);
             //   dealersList = salesPerformanceService.getCustomersByLeadType(DEALER, territoryList, subAreaList, districtList);
            }

            else if(currentUser instanceof SclCustomerModel)
            {
                if((currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
                    customers = territoryManagementService.getRetailerListForDealer();
                    dealersList.add((SclCustomerModel) currentUser);
                }
                else if((currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
                    customers.add((SclCustomerModel) currentUser);
                }
            }
        }


        Map<String,Double> salesQtyCM=new HashMap<>();
        Map<String,Double> salesQtyLM=new HashMap<>();
        if (Filter.equalsIgnoreCase("MTD")) {

            //    if (taluka.equalsIgnoreCase("ALL")) {

            List<Double> monthlySale = new ArrayList<>();
            List<Double> lastMonthWiseList = new ArrayList<>();
            List<Double> listOfCurrentMonthSalesTarget = new ArrayList<>();
            List<Double> listOfLastMonthSalesTarget = new ArrayList<>();
            List<Double> growthList=new ArrayList<>();

            LocalDate date = LocalDate.now();
            // LocalDate currentMonth = LocalDate.now();
            for (int i = 0; i <= 6; i++) {
                LocalDate lastMonth1 = date.minusMonths(i);
                int month = lastMonth1.getMonthValue();
                int year = lastMonth1.getYear();
                String monthName = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                String formattedMonth = monthName.concat("-").concat(String.valueOf(year));

                salesQtyCM = networkService.getSalesQuantityForRetailerByMonthYear(customers, date.getMonthValue(), date.getYear(), dealersList, null);
                salesQtyLM = networkService.getSalesQuantityForRetailerByMonthYear(customers, lastMonth1.getMonthValue(), lastMonth1.getYear(), dealersList, null);


                Double currentMonthSalesTarget = salesPerformanceService.getMonthlySalesTargetForRetailer(customers,baseSiteService.getCurrentBaseSite(),null,formattedMonth);
                LOG.info(String.format("Retailer currentMonthSalesTarget:%s",currentMonthSalesTarget));
                Double lastMonthSalesTarget = salesPerformanceService.getMonthlySalesTargetForRetailer(customers,baseSiteService.getCurrentBaseSite(),null,formattedMonth);
                LOG.info(String.format("Dealers lastMonthSalesTarget:%s",lastMonthSalesTarget));

                /*monthlySale.add(monthSale);
                lastMonthWiseList.add(lastMonthWiseSale);*/
                if (currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                    monthlySale.add(salesQtyCM.get("quantityInBags"));
                    lastMonthWiseList.add(salesQtyLM.get("quantityInBags"));
                    double growth=salesQtyCM.get("quantityInBags")-salesQtyLM.get("quantityInBags");
                    growthList.add(growth);
                }
               else {
                    monthlySale.add(salesQtyCM.get("quantityInMT"));
                    lastMonthWiseList.add(salesQtyLM.get("quantityInMT"));
                    double growth=salesQtyCM.get("quantityInMT")-salesQtyLM.get("quantityInMT");
                    growthList.add(growth);
                }
                listOfCurrentMonthSalesTarget.add(currentMonthSalesTarget);
                listOfLastMonthSalesTarget.add(lastMonthSalesTarget);
            }

            for (int i = 0; i <= 5; i++) {
                MonthlySalesData data = new MonthlySalesData();
                LocalDate lastMonth2 = date.minusMonths(i);
                int month = lastMonth2.getMonthValue();
                int year = lastMonth2.getYear();

                data.setMonthYear(getMonthName(month - 1).concat(" ").concat(String.valueOf(year)));

                double currentMonthSales = lastMonthWiseList.get(i);
                double lastMonthSales = lastMonthWiseList.get(i + 1);
                double monthWiseAvgSale = monthlySale.get(i);
                double lastMonthAvgSale = lastMonthWiseList.get(i);

                //* to do - optimize *//*
                if (date.getMonthValue() != 0 && monthWiseAvgSale != 0)
                    data.setActualSales(monthWiseAvgSale);
                else
                    data.setActualSales(0.0);

                if (lastMonth2.getMonthValue() != 0 && lastMonthAvgSale != 0)
                    data.setActualSales(lastMonthAvgSale);
                else
                    data.setActualSales(0.0);
                LOG.info(String.format("Dealers Actual:%s",data.getActualSales()));

                data.setGrowth(currentMonthSales - lastMonthSales);
                LOG.info(String.format("Dealers Growth:%s",data.getGrowth()));

                double monthlySalesTarget = listOfCurrentMonthSalesTarget.get(i);
                double lastMonthSalesTarget = listOfLastMonthSalesTarget.get(i);

                if (date.getMonthValue() != 0 && monthlySalesTarget != 0.0)
                    data.setTargetSales(monthlySalesTarget);
                else
                    data.setTargetSales(0.0);

                if (lastMonth2.getMonthValue() != 0 && lastMonthSalesTarget != 0.0)
                    data.setTargetSales(lastMonthSalesTarget);
                else
                    data.setTargetSales(0.0);

                if (monthlySalesTarget > 0) {
                    data.setPercentage((currentMonthSales / monthlySalesTarget) * 100);
                }else {
                    data.setPercentage(0.0);
                }
                data.setGrowth(growthList.get(i));
                LOG.info(String.format("Retailer Percentage:%s",data.getPercentage()));
                dataList.add(data);

                if (month != 0)
                    --month;
                else {
                    month = 11;
                    --year;
                }
            }
        }

        if (Filter.equalsIgnoreCase("YTD")){
            List<Double> yearWiseSales = new ArrayList<>();
            List<Double> yearWiseTarget = new ArrayList<>();

            int startYear=0,endYear=0;
            if(LocalDate.now().getMonth().getValue()>=4){
                endYear=LocalDate.now().getYear()+1;//24
                startYear=LocalDate.now().getYear();
            }else{
                startYear=LocalDate.now().getYear()-1;
                endYear=LocalDate.now().getYear();
            }

            for (int i = 0; i <= 3; i++) {
                startYear = startYear - i;
                endYear = endYear - i;
                double targetSalesAnnual=0.0;
                salesQtyCM=networkService.getSalesQuantityForRetailerByYTD(customers, dealersList, null, startYear, endYear);
                StringBuilder f = new StringBuilder();
                String financialYear = String.valueOf(f.append(startYear-i).append("-").append(endYear-i));
                targetSalesAnnual=salesPerformanceDao.getAnnualSalesTargetForRetailer(customers,financialYear,null);
                if ((currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
                    yearWiseSales.add(salesQtyCM.get("quantityInBags"));
                }
                else {
                    yearWiseSales.add(salesQtyCM.get("quantityInMT"));
                }
                yearWiseTarget.add(targetSalesAnnual);
            }


            if(LocalDate.now().getMonth().getValue()>=4){
                endYear=LocalDate.now().getYear()+1;//24
                startYear=LocalDate.now().getYear();
            }else{
                startYear=LocalDate.now().getYear()-1;
                endYear=LocalDate.now().getYear();
            }
            for (int i = 0; i <= 2; i++) {
                MonthlySalesData data = new MonthlySalesData();
                double currentYrSales = yearWiseSales.get(i);
                double previousYrSales = yearWiseSales.get(i + 1);
                double currentYearTarget= yearWiseTarget.get(i);

                data.setActualSales(currentYrSales);
                //if (previousYrSales != 0.0) {
                data.setGrowth(currentYrSales - previousYrSales);
                //}
                LOG.info("Growth YTD"+data.getGrowth());
                StringBuilder f = new StringBuilder();
                String financialYear = String.valueOf(f.append(startYear-i).append("-").append(endYear-i));
                data.setMonthYear(financialYear);
                data.setTargetSales(currentYearTarget);
                if (currentYearTarget > 0) {
                    data.setPercentage((currentYrSales / currentYearTarget) * 100);
                }else {
                    data.setPercentage(0.0);
                }
                LOG.info("Percentage YTD"+data.getPercentage());
                dataList.add(data);
            }
        }
        return dataList;
    }


    @Override
    public List<MonthlySalesData> getLastSixMonthSalesForInfluencer(String taluka, BaseSiteModel site, String filter) {

        List<MonthlySalesData> dataList = new ArrayList<>();
        final SclUserModel sclUser = (SclUserModel) userService.getCurrentUser();
        Calendar cal = Calendar.getInstance();
        Date currentDate = cal.getTime();// jul-2023

        Date startDate = null;
        Date endDate = null;

        cal.set(Calendar.MONTH, 2);
        cal.set(Calendar.DATE, 31);
        cal.set(Calendar.HOUR, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);

        Date financialYrEndDate = cal.getTime();
        List<SclCustomerModel> customers = new ArrayList<>();

        if(!Objects.isNull(sclUser))
        {
            if (taluka.equalsIgnoreCase("ALL")) {
                customers = territoryManagementService.getInfluencersForSubArea();
            }
            else
            {
                customers = territoryManagementService.getInfluencersForSubArea(taluka);
            }

        }

        if (filter.equalsIgnoreCase("MTD")) {

            List<List<Object>> sales = new ArrayList<>();

            cal = Calendar.getInstance();

            // Set the end date to the current date
            endDate = cal.getTime();
            int endYear = cal.get(Calendar.YEAR);
            int endMonth = cal.get(Calendar.MONTH)+1;

            // Set the start date to 8 months before the current date
            cal.add(Calendar.MONTH, -6);
            cal.set(Calendar.HOUR, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            startDate = cal.getTime();

            // Check if the start date is before the 30th of the month
            int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
            if (dayOfMonth < 30) {
                // Set the start date to the 30th of that month
                cal.set(Calendar.DAY_OF_MONTH, 30);
                startDate = cal.getTime();
            }
            int startYear = cal.get(Calendar.YEAR);
            int startMonth = cal.get(Calendar.MONTH)+1;

            LOG.info("Getting sales List");
            sales =  dealerDao.getMonthWiseForInfluencerMTD(customers, startDate, endDate);
            for (List<Object> sale : sales) {
                LOG.info(String.format("Sales %s ::",sale));
            }

            try {

                int i=0;
                for(int j=0;j<6;j++)
                {
                    // int currentMonth = endMonth - j < 1 ? 12 - (j - 1) : endMonth - j;
                    MonthlySalesData data = new MonthlySalesData();
                    double previousSales=0.0;
                    double currentSales=0.0;
                    String monthYr;
                    if(i>=sales.size())
                    {
                        monthYr = getMonthName(endMonth-1).concat(" ");
                        if(startMonth-endMonth>0)
                        {
                            monthYr = monthYr + Integer.toString(endYear);
                            if(endMonth==1){
                                endMonth=12;
                            }
                            else {
                                endMonth--;
                            }
                        }
                        else {
                            monthYr = monthYr +Integer.toString(startYear);
                            endMonth--;
                        }
                        data.setGrowth(0.0);
                    }
                    else if(endMonth!=(int)sales.get(i).get(1)){
                        monthYr = getMonthName(endMonth-1).concat(" ");
                        if(startMonth-endMonth>0)
                        {
                            monthYr = monthYr + Integer.toString(endYear);
                            if(endMonth==1){
                                endMonth=12;
                            }
                            else {
                                endMonth--;
                            }
                        }
                        else {
                            monthYr = monthYr +Integer.toString(startYear);
                            endMonth--;
                        }
                        data.setGrowth(0.0);
                    }
                    else
                    {
                        monthYr = getMonthName((int)sales.get(i).get(1)-1).concat(" ").concat(Integer.toString((int)sales.get(i).get(0)));
                        currentSales = (double) sales.get(i).get(2);
                        if((i+1)<sales.size())
                        {
                            previousSales = (double) sales.get(i+1).get(2);
                            if((int)sales.get(i).get(1)==1)
                            {
                                if(((int)sales.get(i).get(1) - (int)sales.get(i+1).get(1)) == (-11))
                                {
                                    data.setGrowth(((currentSales-previousSales)/previousSales)*100);
                                }
                                else
                                {
                                    data.setGrowth(0.0);
                                }
                            }
                            else
                            {
                                if(((int)sales.get(i).get(1) - (int)sales.get(i+1).get(1)) == 1)
                                {
                                    data.setGrowth(((currentSales-previousSales)/previousSales)*100);
                                }
                                else
                                {
                                    data.setGrowth(0.0);
                                }
                            }
                        }
                        else
                        {
                            data.setGrowth(0.0);
                        }
                        i++;
                        if(endMonth==1){
                            endMonth=12;
                        }
                        else {
                            endMonth--;
                        }
                    }
                    data.setMonthYear(monthYr);
                    data.setActualSales(currentSales);
                    dataList.add(data);
                }
            }catch(Exception e)
            {
                LOG.error(e);
            }
        }
        else
        {

            int currentYr = 0;
            int previousYr = 0;

            if (currentDate.before(financialYrEndDate)) {
                endDate = financialYrEndDate;
                cal.setTime(endDate);

                currentYr = cal.get(Calendar.YEAR);

                cal = Calendar.getInstance();
                cal.add(Calendar.YEAR, -1);
                cal.set(Calendar.MONTH, 3);
                cal.set(Calendar.DATE, 1);
                cal.set(Calendar.HOUR, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);

                startDate = cal.getTime();

                previousYr = cal.get(Calendar.YEAR);

            } else {
                cal.setTime(financialYrEndDate);
                cal.add(Calendar.YEAR, 1);
                endDate = cal.getTime();
                currentYr = cal.get(Calendar.YEAR);

                cal.add(Calendar.YEAR, -1);
                cal.set(Calendar.MONTH, 3);
                cal.set(Calendar.DATE, 1);
                cal.set(Calendar.HOUR, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);

                startDate = cal.getTime();

                previousYr = cal.get(Calendar.YEAR);
            }

            List<Double> yearWiseSales = new ArrayList<>();
            double currentSales = 0.0;

            for(int i=0;i<=3;i++)
            {
                LOG.info(String.format("StartDate ::%s",startDate));
                if (i == 0) {
                    LOG.info(String.format("EndDate ::%s",currentDate));
                    currentSales =  dealerDao.getMonthWiseForInfluencerYTD(customers, startDate, currentDate);
                } else {
                    cal.setTime(endDate);
                    cal.add(Calendar.YEAR, -1);
                    cal.set(Calendar.MONTH, 2); // Set the month to March
                    cal.set(Calendar.DATE, 31); // Set the date to the last day of March
                    cal.set(Calendar.HOUR, 23);
                    cal.set(Calendar.MINUTE, 59);
                    cal.set(Calendar.SECOND, 59);
                    endDate = cal.getTime();
                    LOG.info(String.format("EndDates ::%s",endDate));

                    currentSales =  dealerDao.getMonthWiseForInfluencerYTD(customers, startDate, endDate);

                }

                cal.setTime(startDate);
                cal.add(Calendar.YEAR, -1);
                startDate = cal.getTime();

                yearWiseSales.add(currentSales);
            }

            try {
                for(int i=0;i<=2;i++)
                {
                    MonthlySalesData data = new MonthlySalesData();

                    data.setMonthYear(filter);
                    double currentYrSales = yearWiseSales.get(i);
                    double previousYrSales = yearWiseSales.get(i+1);

                    data.setActualSales(currentYrSales);
                    if(previousYrSales!=0.0)
                    {
                        data.setGrowth(((currentYrSales/previousYrSales)*100));
                    }

                    String year = Integer.toString((previousYr-i)).concat("-").concat(Integer.toString((currentYr-i)));
                    data.setMonthYear(year);

                    dataList.add(data);
                }
            }catch(Exception e) {
                LOG.error(e);
            }
        }

        return dataList;
    }

    @Override
    public List<NetworkRemovalModel> getNetworkRemoval() {
      /*  var site = baseSiteService.getCurrentBaseSite();
        if (userService.getCurrentUser().getClass().equals(SclUserModel.class)) {
            SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
            if (currentUser.getUserType().equals(SclUserType.TSM) || currentUser.getUserType().equals(SclUserType.RH)) {
                FilterTalukaData filterTalukaData = new FilterTalukaData();
                List<DistrictMasterModel> districtList = new ArrayList<>();
                List<SubAreaMasterModel> soList = new ArrayList<>();
                if (doList != null && !doList.isEmpty()) {
                    for (String code : doList) {
                        districtList.add(districtMasterDao.findByCode(code));
                    }
                    if (districtList.get(0) != null)
                        filterTalukaData.setDistrictCode(districtList.get(0).getCode());
                }
                if (subAreaList != null && !subAreaList.isEmpty()) {
                    for (String id : subAreaList) {
                        soList.add(territoryManagementDao.getTerritoryById(id));
                    }
                    if (soList.get(0) != null)
                        filterTalukaData.setTalukaName(soList.get(0).getTaluka());
                }
                if (CollectionUtils.isEmpty(soList)) {
                    List<SubAreaMasterModel> sublist = territoryManagementService.getTaulkaForUser(filterTalukaData);
                    soList.addAll(sublist);
                }
                return networkDao.getNetworkRemovalForSubArea(soList, site);
            }
            else {
                var subareaMaster = territoryManagementService.getTerritoriesForSO();
                return networkDao.getNetworkRemovalForSubArea(subareaMaster, site);
            }
        }
        else {
            var subareaMaster = territoryManagementService.getTerritoriesForSO();
            return networkDao.getNetworkRemovalForSubArea(subareaMaster, site);
        }*/

        var subareaMaster=territoryManagementService.getTerritoriesForSO();
        var site=baseSiteService.getCurrentBaseSite();
        return networkDao.getNetworkRemovalForSubArea(subareaMaster,site);

    }

    @Override
    public List<SubAreaMasterModel> getSubAreaForSalesPromoter(SclCustomerModel source) {
        var dealers=getDealersForSalesPromoter(source);
        return Collections.emptyList();//territoryManagementService.getSubAreaForDealers(dealers);
    }

    @Override
    public List<SclUserModel> getSalesOfficersForSubArea(List<SubAreaMasterModel> subAreaMasters) {
        return territoryManagementService.getUsersForSubAreas(subAreaMasters);
    }

    @Override
    public String getTotalOutstandingForPromoter(SclCustomerModel promoter) {
        var dealers=getDealersForSalesPromoter(promoter);
        return df.format(dealers.stream().mapToDouble(dealer->djpVisitDao.getDealerOutstandingAmount(dealer.getCustomerNo())).sum());
    }

    @Override
    public String getOutstandingDaysForPromoter(SclCustomerModel promoter) {
        var dealers=getDealersForSalesPromoter(promoter);
        AtomicDouble maxOutstandingDays =new AtomicDouble(0.0);
        dealers.forEach(dealer->{
                    double totalOutstandingAmount = djpVisitDao.getDealerOutstandingAmount(dealer.getCustomerNo());
                    double dailyAverageSales = collectionDao.getDailyAverageSalesForDealer(dealer.getCustomerNo());

                    if(dailyAverageSales!=0.0)
                    {
                        var outstandingDays = totalOutstandingAmount/dailyAverageSales;
                        if(outstandingDays>maxOutstandingDays.get()){
                            maxOutstandingDays.set(outstandingDays);
                        }
                    }
                }
        );

        return df.format(maxOutstandingDays.get());
    }

    @Override
    public Integer getNewInfluencerCountMTD(SclCustomerModel sclCustomer,BaseSiteModel baseSite, Date startDate, Date endDate,Date doj,String fromCustomerType) {
        return networkDao.getNewInfluencerCountMTD(sclCustomer,baseSite,startDate,endDate,doj,fromCustomerType);
    }

    @Override
    public Integer getNewRetailerCountMTD(SclCustomerModel sclCustomer,BaseSiteModel baseSite, Date startDate, Date endDate,Date doj) {
        return networkDao.getNewRetailerCountMTD(sclCustomer,baseSite,startDate,endDate,doj);
    }

    @Override
    public Integer getRetailerInfluencerCardCountMTD(String customerType,SclCustomerModel sclCustomer, BaseSiteModel baseSite, Date startDate, Date endDate, String networkType) {
        List<SclCustomerModel> customerList=new ArrayList<>();
        String fromCustomerType=null;
        if(customerType.equalsIgnoreCase(RETAILER)) {
            customerList = networkDao.getRetailerCardCountMTD(sclCustomer, baseSite, startDate, endDate);
        }
        if(customerType.equalsIgnoreCase("INFLUENCER")) {
            if (sclCustomer.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                fromCustomerType = "Dealer";
            }
            else if (sclCustomer.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                fromCustomerType = "Retailer";
            }
            customerList = networkDao.getInfluencerCardCountMTD(sclCustomer, baseSite, startDate, endDate,fromCustomerType);
        }
        var applicableCustomers=customerList.stream().filter(cust->Objects.nonNull(cust.getNetworkType())).collect(Collectors.toList());
        var total=applicableCustomers.size();
        int count=0;
        if(total>0) {
            if(networkType.equalsIgnoreCase("Active")){
                count= (int) applicableCustomers.stream().filter(cus -> "Active".equalsIgnoreCase(cus.getNetworkType())).count();
            }
            else if(networkType.equalsIgnoreCase("Inactive")){
                count = (int) applicableCustomers.stream().filter(cus -> "Inactive".equalsIgnoreCase(cus.getNetworkType())).count();
            }
            else if(networkType.equalsIgnoreCase("Dormant")){
                count= (int) applicableCustomers.stream().filter(cus -> "Dormant".equalsIgnoreCase(cus.getNetworkType())).count();
            }
        }
        return count;
    }

    @Override
    public Integer getNetworkDormantCountCard(String customerType,SclUserModel currentUser, BaseSiteModel currentBaseSite, Date startDateForCM, Date endDateForCM, String networkType,List<String> doList,
                                              List<String> subAreaList,List<String> territoryList) {
        List<SclCustomerModel> customerFilteredList = new ArrayList<>();
        int countOfInfluencer =0;
        if (customerType.equalsIgnoreCase(RETAILER)) {
            customerFilteredList = salesPerformanceService.getCustomersByLeadType(customerType,territoryList,subAreaList,doList);
            return customerFilteredList.isEmpty() ? 0 : customerFilteredList.size();
        } else if (customerType.equalsIgnoreCase(DEALER)) {
            customerFilteredList =  salesPerformanceService.getCustomersByLeadType(customerType,territoryList,subAreaList,doList);
            return customerFilteredList.isEmpty() ? 0 : customerFilteredList.size();
        } else if (customerType.equalsIgnoreCase(INFLUENCER)) {
            customerFilteredList = territoryManagementService.getInfluencersForSubArea().stream().filter(cust -> Objects.nonNull(cust.getInfluencerType()) && Objects.nonNull(cust.getNirmanMitraCode())).collect(Collectors.toList());
            var dormantList = customerFilteredList.stream().filter(cust -> cust.getNetworkType().equals("Dormant")).collect(Collectors.toList());
        }
        var dormantList = customerFilteredList.stream().filter(cust -> cust.getNetworkType().equals("Dormant")).collect(Collectors.toList());
        return dormantList.size();


    }

    public MonthlySalesData getMonthlySalesForDealer(SclCustomerModel customer,LocalDate currentMonth) {
        String customerNo = customer.getCustomerNo() != null ? customer.getCustomerNo() : " ";
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        String customerUid = customer.getUid();
        CustomerCategory category = CustomerCategory.TR;
        var salesOfficer=(SclUserModel) userService.getCurrentUser();
        var subAreas = territoryManagementService.getTerritoriesForSO();
        MonthlySalesData data = new MonthlySalesData();
        Double currentMonthSale = djpVisitDao.getSalesHistoryData(customerNo, currentMonth.getMonthValue(), currentMonth.getYear(), category, currentBaseSite);

        LocalDate lastMonth = currentMonth.minusMonths(1);
        Double lastMonthSale = djpVisitDao.getSalesHistoryData(customerNo, lastMonth.getMonthValue(), lastMonth.getYear(), category, currentBaseSite);

        data.setActualSales(currentMonthSale);
        double growth = currentMonthSale - lastMonthSale;
        data.setGrowth(growth);

        Date date = Date.from(currentMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
        var monthYear = SclDateUtility.getFormattedDate(date, "MMM-YYYY");
        Double targetSale = salesPlanningService.getMonthWiseAnnualTargetForDealer(salesOfficer, customerUid, monthYear, subAreas);
        data.setTargetSales(targetSale);
        if (targetSale > 0) {
            data.setPercentage((currentMonthSale / targetSale) * 100);
        }else {
            data.setPercentage(0.0);
        }

        if(Objects.nonNull(monthYear)) {
            data.setMonthYear(monthYear.replace("-", " "));
        }
        return data;

    }


    public MonthlySalesData getYearlySalesForDealer(SclCustomerModel customer,LocalDate currentMonth) {
        String customerNo = customer.getCustomerNo() != null ? customer.getCustomerNo() : " ";
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        String customerUid = customer.getUid();
        CustomerCategory category = CustomerCategory.TR;
        var salesOfficer=(SclUserModel) userService.getCurrentUser();
        var subAreas = territoryManagementService.getTerritoriesForSO();
        MonthlySalesData data = new MonthlySalesData();
        Double currentMonthSale = djpVisitDao.getSalesHistoryData(customerNo, currentMonth.getMonthValue(), currentMonth.getYear(), category, currentBaseSite);

        LocalDate lastMonth = currentMonth.minusMonths(1);
        Double lastMonthSale = djpVisitDao.getSalesHistoryData(customerNo, lastMonth.getMonthValue(), lastMonth.getYear(), category, currentBaseSite);

        data.setActualSales(currentMonthSale);
        double growth = currentMonthSale - lastMonthSale;
        data.setGrowth(growth);

        Date date = Date.from(currentMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
        var monthYear = SclDateUtility.getFormattedDate(date, "MMM-YYYY");
        Double targetSale = salesPlanningService.getMonthWiseAnnualTargetForDealer(salesOfficer, customerUid, monthYear, subAreas);
        data.setTargetSales(targetSale);
        if (targetSale > 0) {
            data.setPercentage((currentMonthSale / targetSale) * 100);
        }else {
            data.setPercentage(0.0);
        }

        if(Objects.nonNull(monthYear)) {
            data.setMonthYear(monthYear.replace("-", " "));
        }
        return data;
    }

    private String getGrowth(Integer bagLiftedBefore, Integer bagLiftedAfter) {
        DecimalFormat df = new DecimalFormat("#0.00");
        return df.format(((bagLiftedAfter-bagLiftedBefore)/bagLiftedBefore)* 100L);
    }

    private Integer getBagLiftedAfterMeet(List<NirmanMitraSalesHistoryModel> salesHistry,Date eventDate) {
        Calendar cal=Calendar.getInstance();
        cal.setTime(eventDate);
        cal.add(Calendar.MONTH,1);
        Date nextMonthDate= cal.getTime();
        var baglifted= salesHistry.stream().filter(sale->sale.getTransactionDate().after(eventDate) && sale.getTransactionDate().before(nextMonthDate)).mapToDouble(NirmanMitraSalesHistoryModel::getTransactionQuantity).sum();
        long diff = nextMonthDate.getTime() - eventDate.getTime();
        var days= TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        return (int) (baglifted/days);
    }

    private Integer getBagLiftedBeforeMeet(List<NirmanMitraSalesHistoryModel> salesHistry,Date eventDate) {
        Calendar cal=Calendar.getInstance();
        cal.setTime(eventDate);
        cal.add(Calendar.MONTH,-3);
        Date threeMonthBackDate= cal.getTime();
        var baglifted= salesHistry.stream().filter(sale->sale.getTransactionDate().after(threeMonthBackDate) && sale.getTransactionDate().before(eventDate)).mapToDouble(NirmanMitraSalesHistoryModel::getTransactionQuantity).sum();
        long diff = eventDate.getTime()-threeMonthBackDate.getTime();
        var days= TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        return (int) (baglifted/days);
    }

    private Double getSalesQuantity(List<NirmanMitraSalesHistoryModel> salesHistry) {
        Date todaty=new Date();
        Calendar monthStart=Calendar.getInstance();
        monthStart.set(Calendar.DAY_OF_MONTH,1);
        return salesHistry.stream().filter(sale->sale.getTransactionDate().after(monthStart.getTime()) && sale.getTransactionDate().before(todaty)).mapToDouble(NirmanMitraSalesHistoryModel::getTransactionQuantity).sum();
    }
    @Override
    public List<SclCustomerModel> getCustomerListFromSubArea(String subArea,BaseSiteModel site){
        return sclUserDao.getAllCustomerForSubArea(subArea, site);
    }

    @Override
    public List<MeetingScheduleModel> getInfluencerMeetCards() {
        return sclUserDao.getInfluencerMeetCards(userService.getCurrentUser());
    }

    //To be Checked
    @Override
    public Map<String, Integer> getCounterInfoForTaluka(String leadType, String taluka) {
        Map<String, Integer> counterInfoMap = new HashMap<>();
        if (leadType.equalsIgnoreCase(DEALER)) {
            RequestCustomerData requestCustomerData=new RequestCustomerData();
            requestCustomerData.setCounterType(List.of("Dealer"));
            //List<SclCustomerModel> shreeCounters = territoryManagementService.getCustomerforUser(requestCustomerData).stream().filter(sclCustomerModel -> sclCustomerModel.getIsShreeSite().equals(Boolean.TRUE)).collect(Collectors.toList());
            List<SclCustomerModel> shreeCounters = territoryManagementService.getDealersForSubArea(taluka);
            LOG.info(String.format("Shree Counter :: %s",shreeCounters.size()));
            List<SclCustomerModel> totalCounters = territoryManagementService.getCustomerforUser(requestCustomerData);
            // List<SclCustomerModel> totalCounters= territoryManagementService.getSCLAndNonSCLAllForSO().stream().filter(sclCustomerModel -> sclCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());
            //List<SclCustomerModel> totalCounters= territoryManagementService.getAllSclAndNonSclCustomerForTerritories(taluka);

            LOG.info(String.format("totalCounters :%s",totalCounters.size()));
            counterInfoMap.put("totalCounter", totalCounters.size());
            counterInfoMap.put("shreeCounter", shreeCounters.size());
        } else if (leadType.equalsIgnoreCase(RETAILER)) {
            RequestCustomerData requestCustomerData=new RequestCustomerData();
            requestCustomerData.setCounterType(List.of("Retailer"));
            List<SclCustomerModel> shreeCounters = territoryManagementService.getRetailersForSubArea(taluka);
            LOG.info(String.format("Shree Counter :: %s",shreeCounters.size()));
            //List<SclCustomerModel> totalCounters = territoryManagementService.getSCLAndNonSCLAllForSO().stream().filter(sclCustomerModel -> sclCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))).collect(Collectors.toList());
            // List<SclCustomerModel> totalCounters= territoryManagementService.getAllSclAndNonSclCustomerForTerritories(taluka);
            List<SclCustomerModel> totalCounters= territoryManagementService.getCustomerforUser(requestCustomerData);
            LOG.info(String.format("totalCounters :: %s",totalCounters.size()));
            //List<SclCustomerModel> shreeCounters = territoryManagementService.getCustomerforUser(requestCustomerData).stream().filter(sclCustomerModel -> sclCustomerModel.getIsShreeSite().equals(Boolean.TRUE)).collect(Collectors.toList());
            //List<SclCustomerModel> totalCounters = territoryManagementService.getCustomerforUser(requestCustomerData);
            counterInfoMap.put("totalCounter", totalCounters.size());
            counterInfoMap.put("shreeCounter", shreeCounters.size());
        }

        counterInfoMap.put("systemProposed", 0);
        return counterInfoMap;
    }

    private String getMonthName(int month){
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sept", "Oct", "Nov", "Dec"};
        return monthNames[month];
    }

    @Override
    public Map<String, Integer> getNetworkTypeCount(String leadType, List<String> doList,
                                                    List<String> subAreaList, List<String> territoryList) {
        Map<String, Integer> networkTypeCountMap = new HashMap<>();
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        List<SclCustomerModel> customerList=new ArrayList<>();
        List<TerritoryMasterModel> territoryMasterModels= (List<TerritoryMasterModel>) territoryMasterService.getCurrentTerritory();
        LOGGER.info(String.format("territoryMasterModels:: %s",territoryMasterModels));
        if(currentUser instanceof SclUserModel){
            customerList = salesPerformanceDao.getCurrentNetworkCustomers(leadType, null, territoryMasterModels, null,true,false,false);
        }
       /* if(currentUser instanceof  SclUserModel) {
            //customerList=salesPerformanceService.getCustomersByLeadType(leadType,territoryList,subAreaList,doList);
            ..
        }*/else if(currentUser instanceof SclCustomerModel){
            if((currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))){
                switch (leadType) {
                    case RETAILER:
                        customerList = territoryManagementService.getRetailerListForDealer();
                        break;
                    case "INFLUENCER":
                        customerList = territoryManagementService.getInfluencerListForDealer();
                        break;
                }
            }
            else if((currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
                switch (leadType) {
                    case "INFLUENCER":
                        customerList = territoryManagementService.getInfluencerListForRetailer();
                        break;
                }
            }
        }

        if(!customerList.isEmpty()) {
            List<SclCustomerModel> applicableCustomers=customerList.stream().filter(cust->Objects.nonNull(cust.getNetworkType())).collect(Collectors.toList());
            int total=applicableCustomers.size();
            if(total>0) {
                List<SclCustomerModel> active = applicableCustomers.stream().filter(cus -> "Active".equalsIgnoreCase(cus.getNetworkType())).collect(Collectors.toList());
                List<SclCustomerModel> inActive = applicableCustomers.stream().filter(cus -> "Inactive".equalsIgnoreCase(cus.getNetworkType())).collect(Collectors.toList());
                List<SclCustomerModel> dormant = applicableCustomers.stream().filter(cus -> "Dormant".equalsIgnoreCase(cus.getNetworkType())).collect(Collectors.toList());
                networkTypeCountMap.put("active", active.size());
                networkTypeCountMap.put("activeShare", SCLDataFormatUtil.calculatePercentage(active.size(), total));
                networkTypeCountMap.put("inActive", inActive.size());
                networkTypeCountMap.put("inActiveShare", SCLDataFormatUtil.calculatePercentage(inActive.size(), total));
                networkTypeCountMap.put("dormant", dormant.size());
                networkTypeCountMap.put("dormantShare", SCLDataFormatUtil.calculatePercentage(dormant.size(), total));
            }
        }
        if(userService.getCurrentUser() instanceof SclCustomerModel){
            //if(((SclCustomerModel) userService.getCurrentUser()).getUserType().getCode().equalsIgnoreCase("SP"))
            if(((SclCustomerModel) userService.getCurrentUser()).getCounterType().getCode().equalsIgnoreCase("SP")){
                RequestCustomerData requestCustomerData = new RequestCustomerData();
                requestCustomerData.setCounterType(List.of(leadType));
                List<SclCustomerModel> customerForUser = territoryManagementService.getCustomerforUser(requestCustomerData);
                if(!customerForUser.isEmpty()){
                    List<SclCustomerModel> applicableCustomersForUser = customerForUser.stream().filter(customer->Objects.nonNull(customer.getNetworkType())).collect(Collectors.toList());
                    int sum = applicableCustomersForUser.size();
                    if(sum>0){
                        List<SclCustomerModel> active = applicableCustomersForUser.stream().filter(custom-> "Active".equalsIgnoreCase(custom.getNetworkType())).collect(Collectors.toList());
                        List<SclCustomerModel> inActive = applicableCustomersForUser.stream().filter(custom-> "InActive".equalsIgnoreCase(custom.getNetworkType())).collect(Collectors.toList());
                        List<SclCustomerModel> dormant = applicableCustomersForUser.stream().filter(custom-> "Dormant".equalsIgnoreCase(custom.getNetworkType())).collect(Collectors.toList());
                        networkTypeCountMap.put("active", active.size());
                        networkTypeCountMap.put("activeShare", SCLDataFormatUtil.calculatePercentage(active.size(), sum));
                        networkTypeCountMap.put("inActive", inActive.size());
                        networkTypeCountMap.put("inActiveShare", SCLDataFormatUtil.calculatePercentage(inActive.size(), sum));
                        networkTypeCountMap.put("dormant", dormant.size());
                        networkTypeCountMap.put("dormantShare", SCLDataFormatUtil.calculatePercentage(dormant.size(), sum));
                    }
                }

            }
        }
        /*else if (currentUser.getUserType().getCode().equals("TSM")) {
         *//*RequestCustomerData requestCustomerData = new RequestCustomerData();
            requestCustomerData.setCounterType(List.of(leadType));
            List<SclCustomerModel> customerForUserTSM = territoryManagementService.getCustomerforUser(requestCustomerData);*//*
            List<SclCustomerModel> customerForUserTSM = salesPerformanceService.getCustomersByLeadType(leadType, territoryList);
            if (!customerForUserTSM.isEmpty()) {
                List<SclCustomerModel> applicableCustomersForUserTSM = customerForUserTSM.stream().filter(customer -> Objects.nonNull(customer.getNetworkType())).collect(Collectors.toList());
                int sum = applicableCustomersForUserTSM.size();
                if (sum > 0) {
                    List<SclCustomerModel> active = applicableCustomersForUserTSM.stream().filter(custom -> "Active".equalsIgnoreCase(custom.getNetworkType())).collect(Collectors.toList());
                    List<SclCustomerModel> inActive = applicableCustomersForUserTSM.stream().filter(custom -> "InActive".equalsIgnoreCase(custom.getNetworkType())).collect(Collectors.toList());
                    List<SclCustomerModel> dormant = applicableCustomersForUserTSM.stream().filter(custom -> "Dormant".equalsIgnoreCase(custom.getNetworkType())).collect(Collectors.toList());
                    networkTypeCountMap.put("active", active.size());
                    networkTypeCountMap.put("activeShare", SCLDataFormatUtil.calculatePercentage(active.size(), sum));
                    networkTypeCountMap.put("inActive", inActive.size());
                    networkTypeCountMap.put("inActiveShare", SCLDataFormatUtil.calculatePercentage(inActive.size(), sum));
                    networkTypeCountMap.put("dormant", dormant.size());
                    networkTypeCountMap.put("dormantShare", SCLDataFormatUtil.calculatePercentage(dormant.size(), sum));

                }
            }
        }
        else if (currentUser.getUserType().getCode().equals("RH")) {
            *//*RequestCustomerData requestCustomerData = new RequestCustomerData();
            requestCustomerData.setCounterType(List.of(leadType));
            List<SclCustomerModel> customerForUserRH = territoryManagementService.getCustomerforUser(requestCustomerData);*//*
            List<SclCustomerModel> customerForUserRH = salesPerformanceService.getCustomersByLeadType(leadType, territoryList);
            if (!customerForUserRH.isEmpty()) {
                List<SclCustomerModel> applicableCustomersForUserRH = customerForUserRH.stream().filter(customer -> Objects.nonNull(customer.getNetworkType())).collect(Collectors.toList());
                int sum = applicableCustomersForUserRH.size();
                if (sum > 0) {
                    List<SclCustomerModel> active = applicableCustomersForUserRH.stream().filter(custom -> "Active".equalsIgnoreCase(custom.getNetworkType())).collect(Collectors.toList());
                    List<SclCustomerModel> inActive = applicableCustomersForUserRH.stream().filter(custom -> "InActive".equalsIgnoreCase(custom.getNetworkType())).collect(Collectors.toList());
                    List<SclCustomerModel> dormant = applicableCustomersForUserRH.stream().filter(custom -> "Dormant".equalsIgnoreCase(custom.getNetworkType())).collect(Collectors.toList());
                    networkTypeCountMap.put("active", active.size());
                    networkTypeCountMap.put("activeShare", SCLDataFormatUtil.calculatePercentage(active.size(), sum));
                    networkTypeCountMap.put("inActive", inActive.size());
                    networkTypeCountMap.put("inActiveShare", SCLDataFormatUtil.calculatePercentage(inActive.size(), sum));
                    networkTypeCountMap.put("dormant", dormant.size());
                    networkTypeCountMap.put("dormantShare", SCLDataFormatUtil.calculatePercentage(dormant.size(), sum));
                }
            }
        }*/
        return networkTypeCountMap;
    }

    private String getGroupForLead(String leadType) {
        String userGroup="";
        switch (leadType) {
            case DEALER:
                userGroup = SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID;
                break;
            case RETAILER:
                userGroup = SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID;
                break;
            case "INFLUENCER":
                userGroup = SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID;
                break;
        }
        return userGroup;
    }

    private List<SclCustomerModel> getCustomersForLead(String leadType) {
        List<SclCustomerModel> customerList=new ArrayList<>();
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        if(currentUser instanceof SclUserModel){
            RequestCustomerData requestCustomerData = new RequestCustomerData();
            switch (leadType) {
                case DEALER:
                    requestCustomerData.setCounterType(List.of("Dealer"));
                    //customerList = territoryManagementService.getDealersForSubArea();
                    break;
                case RETAILER:
                    requestCustomerData.setCounterType(List.of("Retailer"));
                    //customerList = territoryManagementService.getRetailersForSubArea();
                    break;
                case "INFLUENCER":
                    requestCustomerData.setCounterType(List.of("Influencer"));
                    //customerList = territoryManagementService.getInfluencersForSubArea();
                    break;
            }
            customerList = territoryManagementService.getCustomerforUser(requestCustomerData);
        }
        else if(currentUser instanceof SclCustomerModel){
            if((currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))){
                switch (leadType) {
                    case RETAILER:
                        customerList = territoryManagementService.getRetailerListForDealer();
                        break;
                    case "INFLUENCER":
                        customerList = territoryManagementService.getInfluencerListForDealer();
                        break;
                }
            }
            else if((currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
                switch (leadType) {
                    case "INFLUENCER":
                        customerList = territoryManagementService.getInfluencerListForRetailer();
                        break;
                }
            }
        }
        return customerList;
    }

    @Override
    public ProspectiveNetworkModel findNetworkByCode(String code) {
        return networkDao.findPerspectiveNetworkByCode(code);
    }

    @Override
    public List<List<Object>> getLatitudeLongitudeOfProspectiveNetworkList(String customerCode) {
        List<List<Object>> latitudeLongitudeOfProspectiveNetworkList = networkDao.getLatitudeLongitudeOfProspectiveNetworkList(customerCode);
        return latitudeLongitudeOfProspectiveNetworkList;
    }


    @Override
    public List<NirmanMitraSalesHistoryModel> getMitraSalesDataForCustomer(String customerNo, BaseSiteModel brand, String transactionType) {
        return networkDao.getMitraSalesDataForCustomer(customerNo, brand, transactionType);
    }


    @Override
    public List<DealerVehicleDetailsModel> getAllVehicleDetails() {
        return networkDao.getAllVehicleDetails();
    }
    @Override
    public List<SclCustomerModel> getSclCustomerForGroupAndSO(String userGroupUid,String uid) {
        var customerList = territoryManagementService.getAllCustomerForSO(uid);
        return customerList.stream().filter(cust -> cust.getGroups().contains(userService.getUserGroupForUID(userGroupUid))).collect(Collectors.toList());
    }

    @Override
    public double getLostSaleForCustomer(SclCustomerModel user) {

        double lost3MonthSale = 0.0;
        try {
            List<OrderModel> orderList= user.getOrders().stream().sorted(Comparator.comparing(OrderModel::getCreationtime).reversed()).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(orderList)){
                return 0.0;
            }
            OrderModel latestOrder=orderList.get(0);
            Calendar cal=Calendar.getInstance();
            cal.setTime(latestOrder.getCreationtime());
            cal.add(Calendar.MONTH,-3);
            lost3MonthSale=orderList.stream().filter(order->order.getCreationtime().after(cal.getTime())).flatMap(order->order.getEntries().stream()).mapToDouble(AbstractOrderEntryModel::getQuantityInMT).sum();
        }catch(Exception e)
        {
            LOGGER.error(e);
        }

        return lost3MonthSale/3;
    }

    @Override
    public List<SclCustomerModel> getDealersForSalesPromoter(SclCustomerModel promoter) {
        return networkDao.getDealersForSalesPromoter(promoter);
    }

    @Override
    public String getSPNetworkPotentialMTD(SclCustomerModel promoter) {
        var dealers=networkDao.getNetworkDealersForSalesPromoter(promoter,territoryManagementService.getTerritoriesForSO());
        return getZeroIfEmpty(dealers.stream().filter(cust->Objects.nonNull(cust.getCounterPotential())).mapToDouble(SclCustomerModel::getCounterPotential).sum());
    }

    @Override
    public String getSPNetwokSalesMTD(SclCustomerModel promoter) {
        var customers=networkDao.getNetworkDealersForSalesPromoter(promoter,territoryManagementService.getTerritoriesForSO());
        return getZeroIfEmpty(customers.stream().mapToDouble(dealer->getCurrentMonthSaleQty(dealer.getUid(),baseSiteService.getCurrentBaseSite())).sum());
    }

    private List<SclCustomerModel> getSclCustomerForPromotor(SclCustomerModel promoter) {
        var subAreas=territoryManagementService.getTerritoriesForPromoter(promoter);
        var sclcustomerList=territoryManagementService.getAllCustomerForSubArea(subAreas);
        return sclcustomerList.stream().filter(cust -> !cust.getGroups().contains(userService.getUserGroupForUID("salespromotergroup"))).filter(cust->cust.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).filter(cust->Objects.nonNull(cust.getCustomerNo())).collect(Collectors.toList());
    }

    @Override
    public Double getSPNetworkShare(SclCustomerModel promoter) {
        var customers=networkDao.getNetworkDealersForSalesPromoter(promoter,territoryManagementService.getTerritoriesForSO());;
        var site=baseSiteService.getCurrentBaseSite();
        var totalSales=customers.stream().mapToDouble(cust->getCurrentMonthSaleQty(cust.getUid(),site)).sum();
        var totalPotential=customers.stream().filter(cust->Objects.nonNull(cust.getCounterPotential())).mapToDouble(SclCustomerModel::getCounterPotential).sum();
        if(totalPotential==0.0){
            return 0.0;
        }
        return (totalSales*100)/totalPotential;
    }

    public Integer getCounterShareForDealer(SclCustomerModel dealer,BaseSiteModel site) {
        var totalSales=getCurrentMonthSaleQty(dealer.getUid(),site);
        if(Objects.nonNull(dealer.getCounterPotential()) && dealer.getCounterPotential()>0.0){
            return (int)(totalSales*100/dealer.getCounterPotential());
        }
        return 0;
    }

    @Override
    public List<SclCustomerModel> getSclCustomerForGroup(String userGroupUid) {
        var customerList = territoryManagementService.getAllCustomerForSO();
        return customerList.stream().filter(cust -> cust.getGroups().contains(userService.getUserGroupForUID(userGroupUid))).collect(Collectors.toList());
    }



    @Override
    public SCLPotentialCustomerListData getTopPotentialCustomerListData(String leadType){
        LOGGER.debug(String.format("Getting Top 10 Potential Customer for LeadType :: %s ",leadType));
        List<SclCustomerModel> customerList=new ArrayList<>();
        List<SCLPotentialCustomerData> sclPotentialCustomerDataList = new ArrayList<>();
        SCLPotentialCustomerListData sclPotentialCustomerListData = new SCLPotentialCustomerListData();

        if(leadType.equalsIgnoreCase(RETAILER)){
            LOGGER.debug(String.format("Filtering Customer for LeadType :: %s ",leadType));
            customerList=territoryManagementService.getRetailersForSubArea();
        }
        else if(leadType.equalsIgnoreCase(DEALER)){
            LOGGER.debug(String.format("Filtering Customer for LeadType :: %s ",leadType));
            customerList=territoryManagementService.getDealersForSubArea();
        }
        customerList=
                customerList.stream().filter(cust->Objects.nonNull(cust.getCounterPotential())).sorted(Comparator.comparing(SclCustomerModel::getCounterPotential).reversed()).limit(10).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(customerList)) {
            customerList.forEach(customer -> {
                var networkModel=findNetworkByCode(customer.getUid());
                if(Objects.nonNull(networkModel)) {
                    SCLPotentialCustomerData data = new SCLPotentialCustomerData();
                    data.setDealer(customer.getName());
                    data.setStage("Potential");
                    data.setPotential(String.valueOf(customer.getCounterPotential()));
                    sclPotentialCustomerDataList.add(data);
                }
            });
        }
        LOGGER.debug(String.format("Setting Customer Data for LeadType :: %s ",leadType));
        sclPotentialCustomerListData.setSclPotentialCustomer(sclPotentialCustomerDataList);
        return sclPotentialCustomerListData;
    }

    @Override
    public RetailerOnboardListData getOnboarderRetailerData(LeadType leadType, String searchKey){
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        double totalSalesCount = 0;
        RetailerOnboardListData retailerOnboardListData = new RetailerOnboardListData();
        List<RetailerOnboardData> retailerOnboardDataList = new ArrayList<>();
        List<SclCustomerModel> retailersForSubArea=new ArrayList<>();
        if(leadType.getCode().equalsIgnoreCase(RETAILER)){
            if(currentUser instanceof SclUserModel) {
                retailersForSubArea  = territoryManagementService.getRetailersForSubArea();
            }
            else if(currentUser instanceof SclCustomerModel){
                if(currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))){
                    retailersForSubArea  = territoryManagementService.getRetailerListForDealer();
                }
            }

            var threeMonthBackDate=SclDateUtility.getThreeMonthBackDate();
            var newRetailerList=retailersForSubArea.stream().filter(cust->Objects.nonNull(cust.getCustomerNo()) && Objects.nonNull(cust.getDateOfJoining()) && cust.getDateOfJoining().after(threeMonthBackDate)).collect(Collectors.toList());
            if(StringUtils.isNotBlank(searchKey) )
            {
                newRetailerList = filterSclCustomersWithSearchTerm(newRetailerList,searchKey);
            }

            for(SclCustomerModel customer : newRetailerList){
                RetailerOnboardData retailerOnboardData = new RetailerOnboardData();
                retailerOnboardData.setCode(customer.getUid());
                retailerOnboardData.setName(customer.getName());
                retailerOnboardData.setPotential(Objects.nonNull(customer.getCounterPotential()) ? String.valueOf(customer.getCounterPotential()) : ZERO);
                   /*var salesHistry = getMitraSalesDataForCustomer(customer.getCustomerNo(), baseSiteService.getCurrentBaseSite(), SclCoreConstants.DJP.RETAILER_TRANSACTION_TYPE);
                    double customerSales =getSalesQuantityMTD(salesHistry);*/
                double customerSales= networkDao.getSalesQuantityForRetailerMTD(customer, baseSiteService.getCurrentBaseSite());
                double custSales=customerSales/20;
                totalSalesCount += custSales;
                retailerOnboardData.setSalesQuantity(String.valueOf(customerSales));
                retailerOnboardDataList.add(retailerOnboardData);

            }
            retailerOnboardListData.setSalesCountMTD(totalSalesCount);
            retailerOnboardListData.setRetailerOnboardList(retailerOnboardDataList);
        }else {
            LOGGER.debug(String.format("unable to find any customer with %s",leadType.getCode()));
        }
        return retailerOnboardListData;
    }
    private Double getSalesQuantityMTD(List<NirmanMitraSalesHistoryModel> salesHistry) {
        var monthStart=SclDateUtility.getMonthStartDate();
        var bagLifted=salesHistry.stream().filter(sale->sale.getTransactionDate().after(monthStart)).mapToDouble(NirmanMitraSalesHistoryModel::getTransactionQuantity).sum();
        return bagLifted/20;
    }
    @Override
    public ChannelStrength getChannelKPIGraphDealerRetailer(String leadType,List<String> doList,  List<String> subAreaList,  List<String> territoryList) {

        double numericReach = 0.0;
        double sclCountersSales = 0.0;
        double allCountersSales = 0.0;
        Double sclSalesAtSclCounters = 0.0;
        Double allSalesAtSclCounters = 0.0;
        ChannelStrength channelStrength = new ChannelStrength();

        List<SclCustomerModel> dealersForSubArea=new ArrayList<>();
        List<B2BCustomerModel> customersForSubArea=new ArrayList<>();
        List<SclCustomerModel> retailersForSubArea=new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.get(Calendar.MONTH);
        List<TerritoryMasterModel> territoryMasterModels= (List<TerritoryMasterModel>) territoryMasterService.getCurrentTerritory();
        LOG.info(String.format("territoryMasterModels:: %s",territoryMasterModels));
        List<SclCustomerModel> nonSclcustomers=salesPerformanceDao.getCurrentNetworkCustomers(null,null,territoryMasterModels,null,false,false,false);
        LOG.info(String.format("Non Scl Customers:: %s",nonSclcustomers.size()));
        if (leadType.equalsIgnoreCase(DEALER)) {
            List<SclCustomerModel> collect=new ArrayList<>();
            dealersForSubArea=salesPerformanceDao.getCurrentNetworkCustomers(leadType,null,territoryMasterModels,null,true,false,false);
            List<SclCustomerModel> nonSclDealers=nonSclcustomers.stream().filter(Objects::nonNull).filter(sclCustomerModel -> sclCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());
            collect.addAll(dealersForSubArea);
            collect.addAll(nonSclDealers);

           /* dealersForSubArea = salesPerformanceService.getCustomersByLeadType(leadType,territoryList,subAreaList,doList);
            dealersForSubArea = dealersForSubArea.stream().filter(sclCustomer->Objects.nonNull(sclCustomer.getDefaultB2BUnit()) && sclCustomer.getDefaultB2BUnit().getUid().equalsIgnoreCase(SclCoreConstants.B2B_UNIT.SCL_SHREE_UNIT_UID)).collect(Collectors.toList());
            customersForSubArea.addAll(dealersForSubArea);//15
//territory code apply
            List<SclCustomerModel> filterdList = territoryManagementService.getSCLAndNonSCLAllForSO().stream().filter(Objects::nonNull).filter(sclCustomerModel -> sclCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());

            Collection<TerritoryMasterModel> territoryMasterModels=territoryMasterService.getCurrentTerritory();
            LOG.info(String.format("territoryMasterModels:: %s",territoryMasterModels));
            List<SclCustomerModel> collect=new ArrayList<>();
            if (CollectionUtils.isNotEmpty(territoryMasterModels)) {
                List<TerritoryMasterModel> territoryMasterModelList = territoryMasterModels.stream().distinct().collect(Collectors.toList());
                filterdList.forEach(sclCustomer -> {
                    //dealer-sclshree
                    if(Objects.nonNull(sclCustomer.getDefaultB2BUnit()) && sclCustomer.getDefaultB2BUnit().getUid().equalsIgnoreCase(SclCoreConstants.B2B_UNIT.SCL_SHREE_UNIT_UID)) {
                        if (sclCustomer.getCounterType().equals(CounterType.DEALER) && Objects.nonNull(sclCustomer.getTerritoryCode())) {
                            LOG.info(String.format("Inside dealer check ::%s and territoryCode::%s", sclCustomer, sclCustomer.getTerritoryCode()));
                            if (territoryMasterModelList.contains(sclCustomer.getTerritoryCode())) {
                                LOG.info(String.format("territoryModels dealer check ::%s ", territoryMasterModelList.contains(sclCustomer.getTerritoryCode())));
                                collect.add(sclCustomer);
                            }
                        }
                    }else{
                        //non scl
                        collect.add(sclCustomer);
                    }
                });
            }*/
            if (CollectionUtils.isNotEmpty(dealersForSubArea) && CollectionUtils.isNotEmpty(collect)) {
                numericReach = (((double) dealersForSubArea.size() / collect.size()) * 100);
            }
            sclCountersSales = getSummationSalesOfCounters(dealersForSubArea);
            allCountersSales = getSummationSalesOfCounters(collect);


            LocalDate date=LocalDate.now();
            if(dealersForSubArea!=null && !dealersForSubArea.isEmpty()) {
                sclSalesAtSclCounters = sclSalesSummaryService.getCurrentMonthSales(customersForSubArea,date.getMonth().getValue(),date.getYear(),territoryList);
            }
            allSalesAtSclCounters = sumOfAllSalesAtSclCounters(collect);

            channelStrength.setShreeNumeric((double) dealersForSubArea.size());
            channelStrength.setTotalNumeric((double) collect.size());

        } else if (leadType.equalsIgnoreCase(RETAILER)) {
            List<SclCustomerModel> collect=new ArrayList<>();
            retailersForSubArea=salesPerformanceDao.getCurrentNetworkCustomers(leadType,null,territoryMasterModels,null,true,false,false);
            List<SclCustomerModel> nonSclRetailer=nonSclcustomers.stream().filter(Objects::nonNull).filter(sclCustomerModel -> sclCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))).collect(Collectors.toList());
            collect.addAll(retailersForSubArea);
            collect.addAll(nonSclRetailer);

            /*//count of scl retailer
            retailersForSubArea =salesPerformanceService.getCustomersByLeadType(leadType,territoryList,subAreaList,doList);
            retailersForSubArea = retailersForSubArea.stream().filter(sclCustomer->Objects.nonNull(sclCustomer.getDefaultB2BUnit()) && sclCustomer.getDefaultB2BUnit().getUid().equalsIgnoreCase(SclCoreConstants.B2B_UNIT.SCL_SHREE_UNIT_UID)).collect(Collectors.toList());

            //count of scl and non scl retailer
            //need to apply territory code logic
            List<SclCustomerModel> filterdList = territoryManagementService.getSCLAndNonSCLAllForSO().stream().filter(Objects::nonNull).filter(sclCustomerModel -> sclCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(filterdList))
                LOGGER.info(String.format("filterdList size:%s",filterdList.size()));

            Collection<TerritoryMasterModel> territoryMasterModels=territoryMasterService.getCurrentTerritory();
            LOG.info(String.format("territoryMasterModels:: %s",territoryMasterModels));
            List<SclCustomerModel> collect=new ArrayList<>();
            if (CollectionUtils.isNotEmpty(territoryMasterModels)) {
                List<TerritoryMasterModel> territoryMasterModelList = territoryMasterModels.stream().distinct().collect(Collectors.toList());
                filterdList.forEach(sclCustomer -> {
                    if(Objects.nonNull(sclCustomer.getDefaultB2BUnit()) && sclCustomer.getDefaultB2BUnit().getUid().equalsIgnoreCase(SclCoreConstants.B2B_UNIT.SCL_SHREE_UNIT_UID)) {
                        if ( Objects.nonNull(sclCustomer.getCounterType()) && sclCustomer.getCounterType().equals(CounterType.RETAILER)) {
                            Integer retailerCount = djpVisitDao.getRetailerCountByTerritory(sclCustomer, territoryMasterModelList);
                            LOG.info(String.format("Inside retailer check ::%s and territoryCode::%s and retailerCount::%s", sclCustomer, territoryMasterModels, retailerCount));
                            if (retailerCount > 0) {
                                collect.add(sclCustomer);
                            }
                            LOGGER.info(String.format("scl retailer count:%s",collect.size()));
                        }
                    }else{
                        LOGGER.info(String.format("non scl retailer count retailer:%s",collect.size()));
                        collect.add(sclCustomer);
                    }
                });
            }
*/
            if (CollectionUtils.isNotEmpty(retailersForSubArea) && CollectionUtils.isNotEmpty(collect.stream().distinct().collect(Collectors.toList()))) {
                numericReach = (((double) retailersForSubArea.size() / collect.size()) * 100);
            }

            //Counter potential summation
            sclCountersSales = getSummationSalesOfCounters(retailersForSubArea);
            allCountersSales = getSummationSalesOfCounters(collect.stream().distinct().collect(Collectors.toList()));

            //Actual Sales of Scl retailers
            if(CollectionUtils.isNotEmpty(retailersForSubArea)) {
                // sclSalesAtSclCounters = retailersForSubArea.stream().filter(cust -> Objects.nonNull(cust.getLastCounterVisit()) && Objects.nonNull(cust.getLastCounterVisit().getMarketMapping())).flatMap(cust -> cust.getLastCounterVisit().getMarketMapping().stream()).filter(m -> Objects.nonNull(m.getBrand()) && currentBaseSite.equals(m.getBrand().getSclBrand())).filter(map -> Objects.nonNull(map.getWholeSales()) && Objects.nonNull(map.getRetailSales())).mapToDouble(m -> m.getWholeSales() + m.getRetailSales()).sum();
                Map<String, Double> salesQuantityForRetailerByMTD = networkService.getSalesQuantityForRetailerByMTD(retailersForSubArea, null, null);
                if(salesQuantityForRetailerByMTD!=null){
                    sclSalesAtSclCounters= salesQuantityForRetailerByMTD.get("quantityInMT");
                }
            }

            //Actual Sales of Scl and non scl retailers
            allSalesAtSclCounters = sumOfAllSalesAtSclCounters(collect);

            channelStrength.setShreeNumeric((double) retailersForSubArea.size());
            channelStrength.setTotalNumeric((double) (int) collect.stream().distinct().count());
        }

        channelStrength.setNumericReach(numericReach);
        if (allCountersSales != 0.0) {
            channelStrength.setAcv((sclCountersSales / allCountersSales) * 100);
            channelStrength.setShreeAcv(sclCountersSales);
            channelStrength.setTotalAcv(allCountersSales);
        } else {
            channelStrength.setAcv(0.0);
            channelStrength.setShreeAcv(sclCountersSales);
            channelStrength.setTotalAcv(allCountersSales);
        }

        if (allSalesAtSclCounters != 0.0) {
            channelStrength.setDepth((sclSalesAtSclCounters / allSalesAtSclCounters) * 100);
            channelStrength.setShreeDepth(sclSalesAtSclCounters);
            channelStrength.setTotalDepth(allSalesAtSclCounters);
        } else {
            channelStrength.setDepth(0.0);
            channelStrength.setShreeDepth(sclSalesAtSclCounters);
            channelStrength.setTotalDepth(allSalesAtSclCounters);
        }
        return channelStrength;
    }

    private Double sumOfAllSalesAtSclCounters(List<SclCustomerModel> customerModels) {
        return customerModels.stream().filter(cust->Objects.nonNull(cust.getLastCounterVisit()) && Objects.nonNull(cust.getLastCounterVisit().getMarketMapping())).flatMap(cust->cust.getLastCounterVisit().getMarketMapping().stream()).filter(map->Objects.nonNull(map.getWholeSales()) && Objects.nonNull(map.getRetailSales())).mapToDouble(m->m.getWholeSales()+m.getRetailSales()).sum();
    }

    private double getSummationSalesOfCounters(List<SclCustomerModel> customerModels) {
        return customerModels.stream().filter(c->c.getCounterPotential()!=null).mapToDouble(SclCustomerModel::getCounterPotential).sum();
    }

    @Override
    public List<SclCustomerModel> getInActiveCustomers(String dealerUserGroupUid) {
        var dealers=salesPerformanceService.getCustomersByLeadType(DEALER,null,null,null);
        return dealers.stream().filter(dealer->NetworkType.INACTIVE.getCode().equals(dealer.getNetworkType())).collect(Collectors.toList());
    }

    @Override
    public SiteSummaryData getSiteSummaryforNetwork(String customerCode) {
        CounterVisitMasterModel counterVisitId = networkDao.getVisitIdBySclCustomer(customerCode);
        SiteSummaryData summaryData = new SiteSummaryData();
        if(counterVisitId!=null) {
            CounterVisitMasterModel counterVisit = counterVisitDao.findCounterVisitById(counterVisitId.getId());

            try {
                SclCustomerModel sclCustomer = counterVisit.getSclCustomer();
                summaryData.setPocName(sclCustomer.getContactPersonName());
                summaryData.setPocContact(sclCustomer.getCustomerNo());
                summaryData.setLastVisitDate(sclCustomer.getLastVisitTime());
                summaryData.setNextFollowUpDate(sclCustomer.getNextFollowUp());
                summaryData.setConstructionArea(sclCustomer.getAreaOfConstruction());
                try {
                    summaryData.setConstructionStatus(sclCustomer.getCurrentStageOfConstruction().getCode());
                } catch (NullPointerException n) {
                    LOG.debug(n);
                }
                summaryData.setMonthlyConsumption(sclCustomer.getMonthlyConsumption());
                summaryData.setBalancePotential(sclCustomer.getBalancePotential());
            } catch (NullPointerException e) {
                LOG.debug(e);
            }

        }

        return summaryData;
    }

    @Override
    public List<SclCustomerModel> filterSclCustomersWithSearchTerm(List<SclCustomerModel> customers,String searchTerm) {
        return  customers.stream().filter(user -> containsIgnoreCase(user.getName(),searchTerm) || containsIgnoreCase(user.getUid(),searchTerm)).collect(Collectors.toList());
    }
    private boolean containsIgnoreCase(String name, String searchKey) {
        return name.toLowerCase().contains(searchKey.toLowerCase());
    }
    @Override
    public List<SchemeDetailsModel> getOnGoingSchemes(PartnerLevel dealer) {
        return schemeDetailsDao.findOnGoingSchemes(new Date(),dealer);
    }

    @Override
    public List<LeadMasterModel> getAllLeads(LeadType leadType, String monthYear, String searchTerm,  List<String> doList,  List<String> subAreaList,List<String> territoryList) {
        List<LeadMasterModel> leadmasterList=new ArrayList<>();
        if(Objects.isNull(monthYear)) {
            if(userService.getCurrentUser().getClass().equals(SclUserModel.class))
            {
                SclUserModel currentUser=(SclUserModel) userService.getCurrentUser();
                if(currentUser.getUserType().equals(SclUserType.TSM) || currentUser.getUserType().equals(SclUserType.RH))
                {
                    List<SubAreaMasterModel> soList=new ArrayList<>();
                    soList=getSubAreaList(doList,subAreaList);
                    leadmasterList.addAll(networkDao.getAllLeadsToDates(null, null, soList));
                }
                else
                    leadmasterList.addAll(networkDao.getAllLeadsToDate(null, null, territoryManagementService.getTerritoriesForSO()));
            }
            else
            {
                SclCustomerModel currentUser=(SclCustomerModel)  userService.getCurrentUser();
                leadmasterList.addAll(networkDao.getAllLeadsToDate(null, null,territoryManagementService.getTerritoriesForCustomer(currentUser)));
            }

        }  else{
            if(userService.getCurrentUser().getClass().equals(SclUserModel.class))
            {
                SclUserModel currentUser=(SclUserModel) userService.getCurrentUser();
                if(currentUser.getUserType().equals(SclUserType.TSM) || currentUser.getUserType().equals(SclUserType.RH))
                {
                    List<SubAreaMasterModel> soList=new ArrayList<>();
                    soList=getSubAreaList(doList,subAreaList);
                    leadmasterList.addAll(getAllLeadsForMonth(monthYear,doList,subAreaList));
                }
                else
                    leadmasterList.addAll(getAllLeadsForMonth(monthYear,null,null));
            }
            else
            {
                SclCustomerModel currentUser=(SclCustomerModel)  userService.getCurrentUser();
                leadmasterList.addAll(getAllLeadsForMonth(monthYear,null,null));
            }
            //leadmasterList.addAll(getAllLeadsForMonth(monthYear,doList,subAreaList));
        }
        if(Objects.nonNull(leadType)){
            if(Objects.nonNull(leadType.getCode())){
                if(leadType.getCode().equalsIgnoreCase("INFLUENCER")) {
                    leadmasterList=filterLeadsForInfluencer(leadmasterList);
                }
                else {
                    leadmasterList=filterLeadsForType(leadmasterList,leadType);
                }
            }
        }
        if(Objects.nonNull(searchTerm)){
            return leadmasterList.stream().filter(lead->lead.getName().toLowerCase().contains(searchTerm.toLowerCase())||lead.getLeadId().contains(searchTerm)).collect(Collectors.toList());
        }
        //return excludeTPCLeads(leadmasterList);
        return  leadmasterList;
    }

    private List<LeadMasterModel> excludeTPCLeads(List<LeadMasterModel> leadmasterList) {
        return leadmasterList.stream().filter(lead->Objects.nonNull(lead.getLeadType()) && !lead.getLeadType().equals(LeadType.TPC)).collect(Collectors.toList());
    }

    @Override
    public List<LeadMasterModel> getAllLostLeads(LeadType leadType){
        List<LeadMasterModel> leadmasterList=new ArrayList<>();
        leadmasterList.addAll(networkDao.getAllLeadsToDate(null, null,territoryManagementService.getTerritoriesForSO() ));
        if(leadType.getCode().equalsIgnoreCase(DEALER)){
            return leadmasterList.stream().filter(lead->lead.getLeadStage().equals(LeadStage.NOT_INTERESTED)).collect(Collectors.toList());
        }
        return leadmasterList;
    }

    private List<LeadMasterModel> filterLeadsForType(List<LeadMasterModel> allLeads,LeadType leadType) {
        return allLeads.stream().filter(lead->Objects.nonNull(lead.getLeadType()) && lead.getLeadType().equals(leadType)).collect(Collectors.toList());
    }

    private List<LeadMasterModel> filterLeadsForInfluencer(List<LeadMasterModel> allLeads){
        var influencerLeadType=List.of(LeadType.ARCHITECT,LeadType.ENGINEER,LeadType.CONTRACTOR,LeadType.MASON,LeadType.INFLUENCER);
        return allLeads.stream().filter(lead->Objects.nonNull(lead.getLeadType()) && influencerLeadType.contains(lead.getLeadType())).collect(Collectors.toList());
    }

    private List<LeadMasterModel> getAllLeadsForMonth(String monthYear,List<String> doList,List<String> subAreaList) {
        SimpleDateFormat format=new SimpleDateFormat("MMM yyyy");
        Date start;
        try {
            start=format.parse(monthYear);
        }catch (Exception e){
            LOGGER.error("error while parsing monthYear Passed");
            return Collections.emptyList();
        }
        Calendar cal=Calendar.getInstance();
        cal.setTime(start);
        cal.add(Calendar.MONTH,1);
        cal.add(Calendar.DAY_OF_MONTH,0);
        Date end=cal.getTime();

        if(userService.getCurrentUser().getClass().equals(SclUserModel.class))
        {
            SclUserModel currentUser=(SclUserModel) userService.getCurrentUser();
            if(currentUser.getUserType().equals(SclUserType.TSM) || currentUser.getUserType().equals(SclUserType.RH))
            {
                List<SubAreaMasterModel> soList=new ArrayList<>();
                soList=getSubAreaList(doList,subAreaList);
                return networkDao.getAllLeadsToDates(start, end, soList);
            }
            else
                return networkDao.getAllLeadsToDate(start, end, territoryManagementService.getTerritoriesForSO());
        }
        else
        {
            SclCustomerModel currentUser=(SclCustomerModel)  userService.getCurrentUser();
            return networkDao.getAllLeadsToDate(start, end,territoryManagementService.getTerritoriesForCustomer(currentUser));
        }

    }

    @Override
    public List<SubAreaMasterModel> getSubAreaList(List<String> doList, List<String> subAreaList) {
        FilterTalukaData filterTalukaData = new FilterTalukaData();
        List<DistrictMasterModel> districtList=new ArrayList<>();
        List<SubAreaMasterModel> soList=new ArrayList<>();
        if (doList != null && !doList.isEmpty()) {
            for (String code : doList) {
                districtList.add(districtMasterDao.findByCode(code));
            }
            if(districtList.get(0)!=null)
                filterTalukaData.setDistrictCode(districtList.get(0).getCode());
        }
        if(subAreaList!=null && !subAreaList.isEmpty()){
            for (String id : subAreaList) {
                soList.add(territoryManagementDao.getTerritoryById(id));
            }
            if(soList.get(0)!=null)
                filterTalukaData.setTalukaName(soList.get(0).getTaluka());
        }
        if(CollectionUtils.isEmpty(soList)) {
            List<SubAreaMasterModel> sublist = territoryManagementService.getTaulkaForUser(filterTalukaData);
            soList.addAll(sublist);
        }

        return soList;
    }

    private int getDaysFromLastOrder(Collection<OrderModel> orderList) {
        if(CollectionUtils.isEmpty(orderList)){
            return 0;
        }
        Date lastOrderDate =orderList.stream().sorted(Comparator.comparing(OrderModel::getCreationtime).reversed()).collect(Collectors.toList()).get(0).getCreationtime();
        int numberOfDays;
        Date currentDate = new Date(System.currentTimeMillis());
        LocalDateTime current = LocalDateTime.ofInstant(currentDate.toInstant(), ZoneId.systemDefault());
        LocalDateTime last = LocalDateTime.ofInstant(lastOrderDate.toInstant(), ZoneId.systemDefault());
        LOGGER.info(String.format("Getting Number Of Days last :: %s current :: %s ",last,current));
        numberOfDays = (int) ChronoUnit.DAYS.between(last, current);
        return numberOfDays;

    }

    private Double getYearToYearGrowthForDealer1(String customerCode, BaseSiteModel site){
        LocalDate currentYearCurrentDate= LocalDate.now();
        LocalDate currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear(), Month.APRIL, 1);
        if(currentYearCurrentDate.getMonth().compareTo(Month.APRIL)<0) {
            currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear()-1, Month.APRIL, 1);
        }
        LocalDate lastYearCurrentDate= LocalDate.now().minusYears(1);

        LocalDate lastFinancialYearDate = currentFinancialYearDate.minusYears(1);

        LOGGER.info(String.format("Getting yoy growth for customerNo :: %s currentYearCurrentDate :: %s currentFinancialYearDate :: %s lastFinancialYearDate :: %s",customerCode,currentYearCurrentDate,currentFinancialYearDate,lastFinancialYearDate));
        double lastSale = sclUserDao.getSalesQuantity(customerCode, getStringDate(lastFinancialYearDate), getStringDate(lastYearCurrentDate));
        double currentSale = sclUserDao.getSalesQuantity(customerCode, getStringDate(currentFinancialYearDate), getStringDate(currentYearCurrentDate));
        LOGGER.info(String.format("For CustomerNo :: %s lastSale :: %s currentSale :: %s",customerCode,lastSale,currentSale));
        if(lastSale>0) {
            return   (((lastSale - currentSale) / lastSale) * 100);
        }
        return 0.0;
    }
    private Double getYearToYearGrowthForRetailer(List<NirmanMitraSalesHistoryModel> sales){
        LocalDate currentYearCurrentDate= LocalDate.now();
        LocalDate currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear(), Month.APRIL, 1);
        if(currentYearCurrentDate.getMonth().compareTo(Month.APRIL)<0) {
            currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear()-1, Month.APRIL, 1);
        }
        LocalDate lastYearCurrentDate= LocalDate.now().minusYears(1);

        LocalDate lastFinancialYearDate = currentFinancialYearDate.minusYears(1);
        double lastSale=sales.stream().filter(sale->sale.getTransactionDate().after(getDate(lastFinancialYearDate)) && sale.getTransactionDate().before(getDate(lastYearCurrentDate))).mapToDouble(NirmanMitraSalesHistoryModel::getTransactionQuantity).sum();
        Date currentFYDate=getDate(currentFinancialYearDate);
        double currentSale = sales.stream().filter(sale->sale.getTransactionDate().after(currentFYDate) && sale.getTransactionDate().before(getDate(currentYearCurrentDate))).mapToDouble(NirmanMitraSalesHistoryModel::getTransactionQuantity).sum();
        LOGGER.info(String.format("lastSale :: %s currentSale :: %s",lastSale,currentSale));
        if(lastSale>0) {
            return   (((lastSale - currentSale) / lastSale) * 100);
        }
        return 0.0;
    }
    private SalesQuantityData setSalesQuantityForCustomer(SclCustomerModel customerModel,String leadType, BaseSiteModel site){
        SalesQuantityData salesQuantityData = new SalesQuantityData();
        LOGGER.info(String.format("getting Sale Quantity for leadType :: %s customerUID :: %s",leadType,customerModel.getUid()));
        if(leadType.equalsIgnoreCase(DEALER)) {
            double lastMonth = getlastMonthSaleQty(customerModel.getUid(), site);
            double actual = getCurrentMonthSaleQty(customerModel.getUid(), site);
            salesQuantityData.setLastMonth(Objects.nonNull(lastMonth)?lastMonth:0.0);
            salesQuantityData.setActual(Objects.nonNull(actual)?actual:0.0);
            LOGGER.info(String.format("Sale Quantity for leadType :: %s customerUID :: %s lastMonth :: %s actual :: %s",leadType,customerModel.getUid(),lastMonth,actual));
        }
        return salesQuantityData;
    }

    private double getlastMonthSaleQty(String customerCode, BaseSiteModel site){
        LocalDate lastMonthdate=LocalDate.now().minusMonths(1);
        LocalDate startDate = LocalDate.of(lastMonthdate.getYear(),lastMonthdate.getMonth(),1);
        LocalDate endDate = startDate.plusMonths(1);
        LOGGER.info(String.format("Getting LastMonthSaleQuantity for customerCode :: %s startDate ::%s endDate :: %s ",customerCode,startDate,endDate));
        return sclUserDao.getSalesQuantity(customerCode, getStringDate(startDate), getStringDate(endDate));
    }

    private double getCurrentMonthSaleQty(String customerCode, BaseSiteModel site){
        LocalDate date=LocalDate.now().plusDays(1);
        LocalDate startDate = LocalDate.of(date.getYear(),date.getMonth(),1);
        LOGGER.info(String.format("Getting CurrentMonthSaleQuantity for customerCode :: %s startDate ::%s endDate :: %s ",customerCode,startDate,date));
        return sclUserDao.getSalesQuantity(customerCode, getStringDate(startDate), getStringDate(date));
    }

    private String getStringDate(LocalDate localDate) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = localDate.atStartOfDay(zone);
        return formatter.format(Date.from(dateTime.toInstant()));
    }
    private Date getDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

    }


    @Override
    public InactiveNetworkData getInactiveNetworkRemovalDetailsForCode(String code) {

        SclCustomerModel user = sclCustomerService.getSclCustomerForUid(code);
        String customerNo = user.getCustomerNo();
        InactiveNetworkData data = new InactiveNetworkData();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        var model = networkDao.getNetworkRemovalModelForCustomerCode(code);

        double lostSale = getLostSaleForCustomer(user);
        double netOutstanding = collectionDao.getDealerNetOutstandingAmount(customerNo);
        double securityDeposit = collectionDao.getSecurityDepositForDealer(customerNo);
        double totalOutstanding = djpVisitDao.getDealerOutstandingAmount(customerNo);
        double totalCreditLimit = djpVisitDao.getDealerCreditLimit(customerNo);
        double utilizedCred = 0.0;
        SecurityDepositStatus status = djpVisitDao.getSecurityDepositStatusForDealer(customerNo);

        data.setCode(user.getUid());
        data.setName(user.getName());
        data.setPotential(getZeroIfEmpty(user.getCounterPotential()));
        data.setLostSale(df.format(lostSale));

        if(user.getLastLiftingDate()!=null)
        {
            data.setLastLiftingDate(dateFormat.format(user.getLastLiftingDate()));
        }
        if(user.getLastVisitTime()!=null)
        {
            data.setLastVisitDate(dateFormat.format(user.getLastVisitTime()));
        }
        if(user.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
            data.setNetworkCategory(NetworkCategory.DEALER.getCode());
        }
        if(user.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
            data.setNetworkCategory(NetworkCategory.RETAILER.getCode());
        }

        Calendar cal = Calendar.getInstance();

        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

        data.setVisitMTD(djpVisitDao.getVisitCountMTD(user, month, year).toString());

        Date endDate = cal.getTime();
        cal.add(Calendar.YEAR, -1);

        Date startDate = cal.getTime();

        data.setVisitYTD(djpVisitDao.getVisitCountBetweenDates(user, startDate, endDate).toString());

        double pendingOrderAmount= getPendingOrderAmount(user.getPk().toString());

        //double pendingOrderAmount =orderValidationProcessDao.getPendingOrderAmount(user.getPk().toString());

        if(totalOutstanding!=0.0) {
            utilizedCred = totalOutstanding + pendingOrderAmount;
            data.setUtilizedCred(amountFormatService.getFormattedValue(utilizedCred));
        }
        else
        {
            data.setUtilizedCred(String.valueOf(0.0));
        }
        data.setNetOutstanding(amountFormatService.getFormattedValue(netOutstanding));
        data.setSecurityDeposit(amountFormatService.getFormattedValue(securityDeposit));

        data.setTotalCreditLimit(amountFormatService.getFormattedValue(totalCreditLimit));
        data.setSecurityDepositStatus(status!=null ? status.getCode() : null);

        if(Objects.isNull(model))
        {
            model = modelService.create(NetworkRemovalModel.class);

            model.setCode(user.getUid());
            model.setName(user.getName());
            model.setPotential(user.getCounterPotential());
            model.setReason(user.getReason());
            model.setLostSale(lostSale);
            model.setLastLiftingDate(user.getLastLiftingDate());
            model.setLastVisitDate(user.getLastVisitTime());

            if(user.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                model.setNetworkCategory(NetworkCategory.DEALER);
            }
            if(user.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                model.setNetworkCategory(NetworkCategory.RETAILER);
            }

            model.setVisitMTD(Integer.valueOf(data.getVisitMTD()!=null ? data.getVisitMTD() : "0"));
            model.setVisitYTD(Integer.valueOf(data.getVisitYTD()!=null ? data.getVisitYTD() : "0"));

            model.setNetOutstanding(netOutstanding);
            model.setSecurityDeposit(securityDeposit);
            model.setUtilizedCredit(utilizedCred);
            model.setTotalCreditLimit(totalCreditLimit);
            model.setSecurityDepositStatus(status);

            modelService.save(model);
        }
        else
        {
            data.setComment(model.getComment());
            if(model.getReason()!=null)
            {
                data.setReason(enumerationService.getEnumerationName(model.getReason()));
            }

        }

        return data;
    }



    @SuppressWarnings("unchecked")
    private Double getPendingOrderAmount(String user) {

        return (Double) sessionService.executeInLocalView(new SessionExecutionBody()
        {
            @Override
            public Double execute()
            {

                try {
                    searchRestrictionService.disableSearchRestrictions();

                    return  orderValidationProcessDao.getPendingOrderAmount(user);
                }
                finally
                {
                    searchRestrictionService.enableSearchRestrictions();
                }
            }

        });


    }

    private String getZeroIfEmpty(Object value) {
        if(Objects.isNull(value)){
            return String.valueOf(0);
        }
        else return String.valueOf(value);
    }

    @Override
    public Boolean saveInactiveNetworkRemovalDetails(InactiveNetworkData data) {

        NetworkRemovalModel model = networkDao.getNetworkRemovalModelForCustomerCode(data.getCode());

        model.setReason((InActivityReason) technicalAssistanceService.getEnumerationValueForLocalizedName("InActivityReason", data.getReason()));
        model.setComment(data.getComment());

        modelService.save(model);

        return Boolean.TRUE;
    }

    @Override
    public List<SclUserModel> getOtherNetworkSO() {
        var currentSO=userService.getCurrentUser();
        //var districtList=territoryManagementService.getAllDistrictForSO(currentSO);
        //var users=territoryManagementService.getAllUserForDistrict(districtList);
        return Collections.emptyList();//users.stream().filter(user->!user.getUid().equals(currentSO.getUid())).collect(Collectors.toList());
    }


    @Override
    public List<SCLImageData> getOnboardingFormsSS(String uid) {
        SclCustomerModel customer = (SclCustomerModel) userService.getUserForUID(uid);
        List<MediaModel> documents = customer.getOnboardingFormsImages();

        List<SCLImageData> formSS = new ArrayList<>();

        for(MediaModel ss : documents)
        {
            SCLImageData form = new SCLImageData();
            form.setUrl(ss.getURL());

            formSS.add(form);
        }

        return formSS;
    }

    @Override
    public SalesHistoryData getSalesHistoryDataForNetworkInfluencer360(SclCustomerModel sclCustomer,List<String> subAreaList,List<String> districtList){

        UserGroupModel influencerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID);
        UserGroupModel retailerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);

        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();

        String transactionType = "";
        String customerCode="";
        LocalDate date=LocalDate.now();
        LocalDate lastMonth=date.minusMonths(1);
        LocalDate lastTolastMonth = date.minusMonths(2);
        LocalDate lastYearSameMonth = date.minusYears(1);

        SalesHistoryData data= new SalesHistoryData();
        double lastMonthSales=0.0,secondLastMonthSales=0.0,lastYearSameMonthSales=0.0,actualMonthSale=0.0;

        if(sclCustomer.getGroups() !=null && sclCustomer.getGroups().contains(influencerGroup))
        {
           /* transactionType = SclCoreConstants.DJP.INFLEUNCER_TRANSACTION_TYPE;
            customerCode=sclCustomer.getNirmanMitraCode();*/
            lastMonthSales = networkDao.getSalesQuantityForInfluencerMonthYear(sclCustomer,currentBaseSite,lastMonth.getMonthValue(), lastMonth.getYear(),subAreaList,districtList);
            secondLastMonthSales =networkDao.getSalesQuantityForInfluencerMonthYear(sclCustomer,currentBaseSite, lastTolastMonth.getMonthValue(), lastTolastMonth.getYear(),subAreaList,districtList);
            lastYearSameMonthSales =networkDao.getSalesQuantityForInfluencerMonthYear(sclCustomer,currentBaseSite, lastYearSameMonth.getMonthValue(), lastYearSameMonth.getYear(),subAreaList,districtList);
            actualMonthSale = networkDao.getSalesQuantityForInfluencerMonthYear(sclCustomer, currentBaseSite, date.getMonthValue(), date.getYear(),subAreaList,districtList);
        }
        else if(sclCustomer.getGroups()!=null && sclCustomer.getGroups().contains(retailerGroup))
        {
          /*  transactionType=SclCoreConstants.DJP.RETAILER_TRANSACTION_TYPE;
            customerCode=sclCustomer.getCustomerNo();*/
            lastMonthSales = networkDao.getSalesQuantityForRetailerMonthYear(sclCustomer,currentBaseSite,lastMonth.getMonthValue(), lastMonth.getYear(),subAreaList,districtList);
            secondLastMonthSales =networkDao.getSalesQuantityForRetailerMonthYear(sclCustomer,currentBaseSite, lastTolastMonth.getMonthValue(), lastTolastMonth.getYear(),subAreaList,districtList);
            lastYearSameMonthSales =networkDao.getSalesQuantityForRetailerMonthYear(sclCustomer,currentBaseSite, lastYearSameMonth.getMonthValue(), lastYearSameMonth.getYear(),subAreaList,districtList);
            actualMonthSale = networkDao.getSalesQuantityForRetailerMonthYear(sclCustomer, currentBaseSite, date.getMonthValue(), date.getYear(),subAreaList,districtList);
        }

        data.setLastMonthSales(lastMonthSales);
        data.setLastYearCurrentMonthSales(lastYearSameMonthSales);
        data.setSalesMTD(actualMonthSale);

        if(lastMonthSales !=0.0 && secondLastMonthSales!=0.0) {
            double growth = ((lastMonthSales - secondLastMonthSales) / secondLastMonthSales) * 100;
            if (growth != 0.0)
                data.setGrowth(growth);
            else
                data.setGrowth(0.0);
        }
        else
        {
            data.setGrowth(0.0);
        }

        if(sclCustomer.getGroups()!=null && sclCustomer.getGroups().contains(retailerGroup)) {
            Double monthlySalesTarget=0.0;
            if(customerCode!=null) {
                monthlySalesTarget = djpVisitDao.getSalesTargetFor360(customerCode, CounterType.RETAILER.getCode(), date.getMonthValue(), date.getYear());
            }

            Calendar cal = Calendar.getInstance();
            int noOfDaysOfTheMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            int pastDay = cal.get(Calendar.DAY_OF_MONTH) - 1;
            int remainingDays = noOfDaysOfTheMonth - pastDay;

            double askingRate = (monthlySalesTarget - actualMonthSale) / remainingDays;
            if (askingRate != 0.0) {
                data.setAskingRate(askingRate);
            } else {
                data.setAskingRate(0.0);
            }
        }
        return data;
    }

    @Override
    public LeadMasterModel findItemByUidParam(String leadId) {

        return networkDao.findItemByUidParam(leadId);
    }

    @Override
    public Integer getLeadsGeneratedCountedForInfluencer(String filter, SclCustomerModel customerModel, BaseSiteModel brand) {
        if(filter!=null && filter.equalsIgnoreCase("MTD")) {
            return networkDao.getLeadsGeneratedCountForInfluencerMtd(customerModel,brand);
        }
        else if(filter !=null && filter.equalsIgnoreCase("YTD")) {
            List<Date> dates = salesPerformanceService.getCurrentFinancialYear();
            Date startDate = null;
            Date endDate = null;
            startDate = dates.get(0);
            endDate = dates.get(1);
            LOG.info("StartDate:"+startDate.toString() + "EndDate:"+endDate.toString());
            return networkDao.getLeadsGeneratedCountForInfluencerYtd(customerModel, brand, startDate, endDate);
        }
        return 0;

    }

    @Override
    public List<SclCustomerModel> getSalesPromotersForSubArea(List<String> doList, List<String> subareaList) {
        List<SubAreaMasterModel> soList=new ArrayList<>();
        soList=getSubAreaList(doList,subareaList);
        return	territoryManagementService.getAllCustomerForSubArea(soList).stream().filter(s->(s.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SP_GROUP_ID))).collect(Collectors.toList());

    }

    @Override
    public List<List<Object>> getOrderReqSalesForRetailer(List<SclCustomerModel> sclReailer, SclCustomerModel dealer, String startDate, String endDate) {
        return networkDao.getOrderReqSalesForRetailer(sclReailer,dealer,startDate,endDate);
    }

    @Override
    public List<List<Object>> getMasterSalesForRetailer(List<SclCustomerModel> sclReailer, SclCustomerModel dealer, String startDate, String endDate) {
        return networkDao.getMasterSalesForRetailer(sclReailer,dealer,startDate,endDate);
    }

    @Override
    public List<List<Object>> getSalesForRetailerList(List<SclCustomerModel> sclRetailer, SclCustomerModel dealer, String startDate, String endDate) {
        return networkDao.getSalesForRetailerList(sclRetailer,dealer,startDate,endDate);
    }

    @Override
    public Map<String,Double> getSalesQuantityForRetailerByYTD(SclCustomerModel sclReailer, List<SclCustomerModel> sclDealer, String product) {
        LocalDate currentYearCurrentDate= LocalDate.now().plusDays(1);
        LocalDate currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear(), Month.APRIL, 1);
        if(currentYearCurrentDate.getMonth().compareTo(Month.APRIL)<0) {
            currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear()-1, Month.APRIL, 1);
        }
        LOGGER.info(String.format("CurrentFinancialYearDate :: %s ",currentFinancialYearDate.toString()));
        LOGGER.info(String.format("currentYearCurrentDate :: %s ",currentYearCurrentDate.toString()));
        return networkDao.getSalesQuantityForRetailerByMonth(sclReailer,currentFinancialYearDate.toString(),currentYearCurrentDate.toString(),sclDealer,product);
    }

    @Override
    public Map<String, Double> getSalesQuantityForRetailerByYTD(List<SclCustomerModel> sclReailer, List<SclCustomerModel> sclDealer, String product, int StartYear, int endYear) {
        LocalDate currentFinancialYearDate = LocalDate.of(StartYear, Month.APRIL, 1);
        LocalDate currentYearCurrentDate = LocalDate.of(endYear, Month.APRIL, 1).plusDays(1);

        LOGGER.info(String.format("CurrentFinancialYearDate :: %s ",currentFinancialYearDate.toString()));
        LOGGER.info(String.format("currentYearCurrentDate :: %s ",currentYearCurrentDate.toString()));
        return networkDao.getSalesQuantityForRetailerByMonth(sclReailer,currentFinancialYearDate.toString(),currentYearCurrentDate.toString(),sclDealer,product);
    }

    @Override
    public Map<String, Double> getSalesQuantityForRetailerByDate(SclCustomerModel sclReailer, String startDate, String endDate, List<SclCustomerModel> sclDealer, String product) {
        return networkDao.getSalesQuantityForRetailerByMonth(sclReailer,startDate,endDate,sclDealer,product);
    }

    @Override
    public Map<String, Double> getSalesQuantityForRetailerByDate(List<SclCustomerModel> sclReailer, String startDate, String endDate, List<SclCustomerModel> sclDealer, String product) {
        return networkDao.getSalesQuantityForRetailerByMonth(sclReailer,startDate,endDate,sclDealer,product);
    }

    @Override
    public Map<String,Double>  getSalesQuantityForRetailerByMTD(SclCustomerModel sclReailer, List<SclCustomerModel> sclDealer, String product) {
        LocalDate firstDayOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDayOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);
        return networkDao.getSalesQuantityForRetailerByMonth(sclReailer,firstDayOfMonth.toString(),lastDayOfMonth.toString(),sclDealer,product);
    }

    @Override
    public Map<String, Double> getSalesQuantityForRetailerByMTD(List<SclCustomerModel> sclReailer, List<SclCustomerModel> sclDealer, String product) {
        LocalDate firstDayOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDayOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);
        return networkDao.getSalesQuantityForRetailerByMonth(sclReailer,firstDayOfMonth.toString(),lastDayOfMonth.toString(),sclDealer,product);
    }

    @Override
    public Map<String,Double>  getSalesQuantityForRetailerByMonthYear(SclCustomerModel sclReailer, int month, int year, List<SclCustomerModel> sclDealer, String product) {
        LocalDate startDate=LocalDate.of(year, Month.of(month),1);
        LocalDate endDate=LocalDate.of(year,Month.of(month),startDate.lengthOfMonth()).plusDays(1);
        return  networkDao.getSalesQuantityForRetailerByMonth(sclReailer,startDate.toString(),endDate.toString(),sclDealer,product);
    }

    @Override
    public Map<String, Double> getSalesQuantityForRetailerByMonthYear(List<SclCustomerModel> sclReailer, int month, int year, List<SclCustomerModel> sclDealer, String product) {
        LocalDate startDate=LocalDate.of(year, Month.of(month),1);
        LocalDate endDate=LocalDate.of(year,Month.of(month),startDate.lengthOfMonth()).plusDays(1);
        return  networkDao.getSalesQuantityForRetailerByMonth(sclReailer,startDate.toString(),endDate.toString(),sclDealer,product);
    }

    /**
     *
     * @param body
     * @return
     */
    @Override
    public boolean isBodyPresentInSiteMessage(String body){
        final Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("body", body);
        final StringBuilder sql = new StringBuilder();
        sql.append("select * from {SiteMessage} where {body}=?body");
        final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
        query.setResultClassList(Arrays.asList(SiteMessageModel.class));
        query.getQueryParameters().putAll(attr);
        final SearchResult<SiteMessageModel> result = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(result.getResult());
    }


    public SclSalesSummaryService getSclSalesSummaryService() {
        return sclSalesSummaryService;
    }

    public void setSclSalesSummaryService(SclSalesSummaryService sclSalesSummaryService) {
        this.sclSalesSummaryService = sclSalesSummaryService;
    }
}
