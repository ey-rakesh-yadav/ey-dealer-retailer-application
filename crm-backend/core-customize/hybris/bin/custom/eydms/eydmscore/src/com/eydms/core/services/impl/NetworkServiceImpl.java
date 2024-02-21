package com.eydms.core.services.impl;

import com.google.common.util.concurrent.AtomicDouble;
import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.customer.services.EyDmsCustomerService;
import com.eydms.core.dao.*;
import com.eydms.core.enums.*;
import com.eydms.core.model.*;
import com.eydms.core.order.dao.OrderValidationProcessDao;
import com.eydms.core.services.*;
import com.eydms.core.util.EYDMSDataFormatUtil;
import com.eydms.core.utility.EyDmsDateUtility;
import com.eydms.facades.data.*;
import com.eydms.facades.visit.data.SiteSummaryData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class NetworkServiceImpl implements NetworkService {

    private static final Logger LOGGER = Logger.getLogger(NetworkServiceImpl.class);
    private static final Logger LOG = Logger.getLogger(NetworkServiceImpl.class);

    private static final String DATE_FORMAT="yyyy-MM-dd";
    private static final String ZERO = " 0";
    private static final String DEALER = "DEALER";
    private static final String RETAILER = "RETAILER";

    private static final String INFLUENCER = "INFLUENCER";
    private static final String SITE = "SITE";
    private static final String TPC = "TPC";
    private static final String RETAILER_USER_GROUP_UID = "EyDmsRetailerGroup";
    DecimalFormat df = new DecimalFormat("#.#");
    @Resource
    private EyDmsUserDao eydmsUserDao;
    @Resource
    private NetworkDao networkDao;

    public SalesPerformanceDao getSalesPerformanceDao() {
        return salesPerformanceDao;
    }

    public void setSalesPerformanceDao(SalesPerformanceDao salesPerformanceDao) {
        this.salesPerformanceDao = salesPerformanceDao;
    }

    @Resource
    private SalesPerformanceDao salesPerformanceDao;
    @Autowired
    private UserService userService;
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
    EyDmsCustomerService eydmsCustomerService;
    
    @Autowired
    DJPVisitDao djpVisitDao;

    @Autowired
    DealerDao dealerDao;

    @Resource
    CounterVisitMasterDao counterVisitDao;

    @Resource
    private SalesPlanningService salesPlanningService;

    @Resource
    OrderValidationProcessDao orderValidationProcessDao;

    @Autowired
    SalesPerformanceService salesPerformanceService;

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
  
    /**
     * Return Dealer Current Network List
     * @param site
     * @return
     */
    @Override
    public DealerCurrentNetworkListData getDealerCurrentNetworkWsData(String dealerCategory, String fields, String networkType, BaseSiteModel site, String leadType, boolean eydmsExclusiveCustomer, String searchKey){

    	DealerCurrentNetworkListData dealerCurrentNetworkListData = new DealerCurrentNetworkListData();
    	List<DealerCurrentNetworkData> currentNetworkWsDataList = new ArrayList<>();
    	List<EyDmsCustomerModel> customerFilteredList=new ArrayList<>();
    	B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
    	if((currentUser instanceof EyDmsUserModel) || (((EyDmsCustomerModel)currentUser).getCounterType()!=null && ((EyDmsCustomerModel)currentUser).getCounterType().equals(CounterType.SP))){
    		RequestCustomerData requestCustomerData = new RequestCustomerData();
    		if(leadType.equalsIgnoreCase(DEALER)) {
    			requestCustomerData.setCounterType(List.of("Dealer"));
    		}
    		if(leadType.equalsIgnoreCase(RETAILER)){
    			requestCustomerData.setCounterType(List.of("Retailer"));
    		}
    		customerFilteredList = territoryManagementService.getCustomerforUser(requestCustomerData);
    		if (eydmsExclusiveCustomer) {
    			customerFilteredList = customerFilteredList.stream().filter(eydmsCustomerModel -> BooleanUtils.isTrue(eydmsCustomerModel.getIsShreeSite()))
    					.collect(Collectors.toList());

    		}           
    	}
    	else if(currentUser instanceof EyDmsCustomerModel){
    		if(leadType.equalsIgnoreCase(RETAILER)){
    			customerFilteredList =
    					territoryManagementService.getRetailerListForDealer();
    		}
    	}

    	if(StringUtils.isNotBlank(dealerCategory)){
    		customerFilteredList=filterEyDmsCustomersWithDealerCategory(customerFilteredList,dealerCategory);
    	}
    	if (StringUtils.isNotBlank(searchKey)) {
    		customerFilteredList = filterEyDmsCustomersWithSearchTerm(customerFilteredList,searchKey);
    	}
    	if (Objects.nonNull(networkType)) {
    		customerFilteredList = customerFilteredList.stream().filter(cust -> networkType.equalsIgnoreCase(cust.getNetworkType())).collect(Collectors.toList());
    	}
    	for (EyDmsCustomerModel customerModel : customerFilteredList) {
    		if(Objects.nonNull(customerModel.getCustomerNo())) {
    			DealerCurrentNetworkData dealerCurrentNetworkData = new DealerCurrentNetworkData();
    			dealerCurrentNetworkData.setCode(customerModel.getUid());
    			dealerCurrentNetworkData.setName(customerModel.getName());
    			dealerCurrentNetworkData.setCustomerNo(customerModel.getCustomerNo());
    			dealerCurrentNetworkData.setPotential(Objects.nonNull(customerModel.getCounterPotential()) ? String.valueOf(customerModel.getCounterPotential()) : ZERO);
    			//countershare and target for dealer
    			if (leadType.equalsIgnoreCase(DEALER)) {
    				dealerCurrentNetworkData.setCounterShare(String.valueOf(getCounterShareForDealer(customerModel, site)));
    				//target
    				double target = eydmsUserDao.getCustomerTarget(customerModel.getCustomerNo(), EyDmsDateUtility.getMonth(new Date()), EyDmsDateUtility.getYear(new Date()));
    				dealerCurrentNetworkData.setTarget(df.format(target));

    				dealerCurrentNetworkData.setSalesQuantity(setSalesQuantityForCustomer(customerModel, leadType, site));
    				double totalOutstanding = djpVisitDao.getDealerOutstandingAmount(customerModel.getCustomerNo());
    				dealerCurrentNetworkData.setOutstandingAmount(df.format(totalOutstanding));

    				dealerCurrentNetworkData.setGrowthRate(df.format(getYearToYearGrowthForDealer(customerModel.getCustomerNo(), site)));
    				dealerCurrentNetworkData.setDaySinceLastOrder(String.valueOf(getDaysFromLastOrder(customerModel.getOrders())));

    			} /*else if (leadType.equalsIgnoreCase(RETAILER)) {
    				var salesHistry = getMitraSalesDataForCustomer(customerModel.getCustomerNo(), baseSiteService.getCurrentBaseSite(), EyDmsCoreConstants.DJP.RETAILER_TRANSACTION_TYPE);
    				dealerCurrentNetworkData.setCounterShare(df.format(getCounterShareForRetailer(customerModel, salesHistry)));
    				SalesQuantityData sales = new SalesQuantityData();
    				sales.setRetailerSaleQuantity(getSalesQuantityMTD(salesHistry));
    				dealerCurrentNetworkData.setSalesQuantity(sales);
    				dealerCurrentNetworkData.setDaySinceLastOrder(getDaysSinceLastLifting(salesHistry));
    				dealerCurrentNetworkData.setGrowthRate(df.format(getYearToYearGrowthForRetailer(salesHistry)));
    			}*/
    			List<SubAreaMasterModel> subAreaMasterModelList = territoryManagementService.getTerritoriesForCustomer(customerModel);
    			if (CollectionUtils.isNotEmpty(subAreaMasterModelList) && Objects.nonNull(subAreaMasterModelList.get(0))) {
    				dealerCurrentNetworkData.setDistrict(subAreaMasterModelList.get(0).getDistrict());
    				dealerCurrentNetworkData.setTaluka(subAreaMasterModelList.get(0).getTaluka());
    			}
    			currentNetworkWsDataList.add(dealerCurrentNetworkData);
    		}
    	}
    	/*if (leadType.equalsIgnoreCase(RETAILER)) {
    		computeRankForRetailer(currentNetworkWsDataList);*/
    	 if(leadType.equalsIgnoreCase(DEALER)){
    		computeRankForDealer(currentNetworkWsDataList);
    	}
    	List<DealerCurrentNetworkData> collect = currentNetworkWsDataList.stream().sorted(Comparator.comparing(nw -> nw.getRank())).collect(Collectors.toList());
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

    private double getCounterShareForRetailer(EyDmsCustomerModel customerModel, List<NirmanMitraSalesHistoryModel> salesHistry) {
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
                    if (CollectionUtils.isNotEmpty(filterEyDmsCustomersWithSearchTerm(new ArrayList<>(meet.getCustomers()), searchTerm))) {
                        filteredList.add(meet);
                    }
                }

        );
        return filteredList.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public Integer getOnboarderCustomer(String leadType, String taluka) {
        Date timestamp=EyDmsDateUtility.getFirstDayOfFinancialYear();
        var customers=networkDao.getOnboardedCustomerTillDate(getGroupForLead(leadType),taluka,timestamp,baseSiteService.getCurrentBaseSite());
        LOGGER.info(String.format("Custoemrs : %s", String.valueOf(customers.size())));
        return customers.size();
    }

    @Override
    public NetworkDealerRetailerCounterShareData getDealerRetailerCounterShareForNetwork(String SOFilter, BaseSiteModel site,String taluka) {
        NetworkDealerRetailerCounterShareData data=new NetworkDealerRetailerCounterShareData();
        double counterPotential=0.0,actualTarget=0.0;
        double finalCounterPotential=0.0;
        int achievementPercentage=0;
        double behindTarget=0.0,aheadTarget=0.0;
        //List<EyDmsCustomerModel> customerFilteredList=getCustomerListFromSubArea(subArea,site).stream().filter(cust->Objects.nonNull(cust.getCustomerNo())).collect(Collectors.toList());
        List<EyDmsCustomerModel> customerFilteredList=new ArrayList<>();

        if(SOFilter.equalsIgnoreCase(RETAILER)){
            if(taluka.equalsIgnoreCase("ALL")) {
                customerFilteredList= territoryManagementService.getRetailersForSubArea();

            }
            else
            {
                customerFilteredList = territoryManagementService.getRetailersForSubArea(taluka);
            }
        }

        else if(SOFilter.equalsIgnoreCase(DEALER)) {
            if(taluka.equalsIgnoreCase("ALL")) {

                customerFilteredList = territoryManagementService.getDealersForSubArea();
            }
            else {
                customerFilteredList = territoryManagementService.getDealersForSubArea(taluka);
            }
        }

        for (EyDmsCustomerModel customerModel : customerFilteredList) {

            if (SOFilter.equalsIgnoreCase(DEALER)) {

                //Actual Sales
                SalesQuantityData salesQuantityData = setSalesQuantityForCustomer(customerModel, SOFilter, site);
                Double actual = salesQuantityData.getActual();
                actualTarget+=actual;
               // counterPotential=customerFilteredList.stream().filter(cust->Objects.nonNull(cust.getCounterPotential())).mapToDouble(EyDmsCustomerModel::getCounterPotential).sum();
                if(Objects.nonNull(customerModel.getCounterPotential())){
                    counterPotential+=customerModel.getCounterPotential();
                }
            } else if (SOFilter.equalsIgnoreCase(RETAILER)) {

                //Actual Sales
                /*var salesHistry = getMitraSalesDataForCustomer(customerModel.getCustomerNo(), baseSiteService.getCurrentBaseSite(), EyDmsCoreConstants.DJP.RETAILER_TRANSACTION_TYPE);
                Double salesQuantityMTD = getSalesQuantityMTD(salesHistry);*/
                Double sales = networkDao.getSalesQuantityForRetailerMTD(customerModel, baseSiteService.getCurrentBaseSite());
                Double salesQuantityMTD= sales/20;
                actualTarget+=salesQuantityMTD;
               // counterPotential=customerFilteredList.stream().filter(cust->Objects.nonNull(cust.getCounterPotential())).mapToDouble(EyDmsCustomerModel::getCounterPotential).sum();
                if(Objects.nonNull(customerModel.getCounterPotential())){
                    counterPotential+=customerModel.getCounterPotential();
                }
            }
            finalCounterPotential=counterPotential;
        }

        data.setPotential(String.valueOf(finalCounterPotential).concat("MT"));
        data.setActual(String.valueOf(actualTarget).concat("MT"));

        if(actualTarget!=0.0 && counterPotential!=0.0)
            achievementPercentage= (int) ((actualTarget/finalCounterPotential)*100);
        if(achievementPercentage!=0) {
          data.setAchievementPercentage(achievementPercentage);
        }
        else {
          data.setAchievementPercentage(0);
        }
        if(achievementPercentage < 100)
        {
            behindTarget=finalCounterPotential-actualTarget;
            if(behindTarget!=0.0) {
                data.setBehindTotalTarget(String.valueOf(behindTarget).concat("MT Behind Target"));
            }
            else {
                data.setBehindTotalTarget(String.valueOf(0).concat("MT Equal Target"));
            }
        }
        else if(achievementPercentage > 100)
        {
            aheadTarget=finalCounterPotential-actualTarget;
            if(aheadTarget!=0.0)
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
    public List<EyDmsCustomerModel> getFilteredCustomerForNetworkType(List<EyDmsCustomerModel> customerModels, String networkType) {
        return customerModels.stream().filter(cust->networkType.equalsIgnoreCase(cust.getNetworkType())).collect(Collectors.toList());
    }

    @Override
    public List<EyDmsCustomerModel> getFilteredInfluencerForCategory(List<EyDmsCustomerModel> customerModels, String category) {
        return customerModels.stream().filter(cust->Objects.nonNull(cust.getInfluencerType()) && category.equalsIgnoreCase(cust.getInfluencerType().getCode())).collect(Collectors.toList());
    }

    @Override

    public List<EyDmsCustomerModel> filterEyDmsCustomersWithDealerCategory(List<EyDmsCustomerModel> customerModels, String dealerCategory) {
        return customerModels.stream().filter(cust -> Objects.nonNull(cust.getDealerCategory()) && dealerCategory.equals(cust.getDealerCategory().getCode())).collect(Collectors.toList());
    }
    public List<List<Object>> getCounterLocationDetails(String dealerCode) {
        List<List<Object>> counterLocationDetails = networkDao.getCounterLocationDetails(dealerCode);
        return counterLocationDetails;

    }

    @Override
    public boolean submitUpdatedLocationDetails(CounterLocationDetailsData counterLocationDetailsData) {
        if(counterLocationDetailsData!=null) {
            EyDmsCustomerModel eydmsCustomer = (EyDmsCustomerModel) userService.getUserForUID(counterLocationDetailsData.getId());
            eydmsCustomer.setLatitude(counterLocationDetailsData.getLatitude());
            eydmsCustomer.setLongitude(counterLocationDetailsData.getLongitude());
            modelService.save(eydmsCustomer);
        }
        return true;
    }

    @Override
    public String getExclusiveDealerPercentage() {

        List<EyDmsCustomerModel> customerFilteredList=new ArrayList<>();
        List<EyDmsCustomerModel> exclusiveCustomerFilteredList=new ArrayList<>();

           customerFilteredList = territoryManagementService.getDealersForSubArea();
            double count= customerFilteredList.size();
        double exclusivecount = 0.0;
           // if (eydmsExclusiveCustomer) {
                exclusiveCustomerFilteredList = customerFilteredList.stream().filter(eydmsCustomerModel -> BooleanUtils.isTrue(eydmsCustomerModel.getIsShreeSite()))
                        .collect(Collectors.toList());
                exclusivecount=exclusiveCustomerFilteredList.size();

            //}
            return String.valueOf((exclusivecount/count)*100)+"%";

    }

    @Override
    public List<InviteesData> getInviteesForMeeting(MeetingScheduleModel meet, List<EyDmsCustomerModel> customers) {
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
                        var salesHistry = getMitraSalesDataForCustomer(cust.getNirmanMitraCode(), baseSiteService.getCurrentBaseSite(), EyDmsCoreConstants.DJP.INFLEUNCER_TRANSACTION_TYPE);
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
    public String isMultiBrand(EyDmsCustomerModel promoter) {
        var dealers=getDealersForSalesPromoter(promoter);
       var brandList=
               dealers.stream().filter(dealer->Objects.nonNull(dealer.getDefaultB2BUnit())).map(dealer->dealer.getDefaultB2BUnit().getId()).distinct().collect(Collectors.toList());

        return brandList.size()>1?"Yes":"No";
    }

    @Override
    public List<MonthlySalesData> getLastSixMonthSalesForDealer(String taluka,BaseSiteModel site, String Filter) {
        final EyDmsUserModel eydmsUser = (EyDmsUserModel) userService.getCurrentUser();
        List<MonthlySalesData> dataList = new ArrayList<>();
        List<EyDmsCustomerModel> customerFilteredList = new ArrayList<>();
        LocalDate currentMonth = LocalDate.now();
        LocalDate lastMonth = currentMonth.minusMonths(1);
        Date currentDate = Date.from(currentMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
        var monthYear = EyDmsDateUtility.getFormattedDate(currentDate, "MMM-YYYY");
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

        Date financialYrEndDate = cal.getTime();

        if (Filter.equalsIgnoreCase("MTD")) {

            if (taluka.equalsIgnoreCase("ALL")) {

                List<Double> monthlySale = new ArrayList<>();
                List<Double> lastMonthWiseList = new ArrayList<>();
                List<Double> listOfCurrentMonthSalesTarget = new ArrayList<>();
                List<Double> listOfLastMonthSalesTarget = new ArrayList<>();

                LocalDate date = LocalDate.now();
                for (int i = 0; i <= 6; i++) {
                    LocalDate lastMonth1 = date.minusMonths(i);
                    int month = lastMonth1.getMonthValue();
                    int year = lastMonth1.getYear();

                    Double monthSale = salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYear(eydmsUser, currentBaseSite, month, year, null, null);
                    Double lastMonthWiseSale = salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYear(eydmsUser, currentBaseSite,lastMonth1.getMonthValue(), lastMonth1.getYear(),null,null);

                    Double currentMonthSalesTarget = networkDao.getSalesTarget(CounterType.DEALER.getCode(), date.getMonthValue(), date.getYear());
                    Double lastMonthSalesTarget = networkDao.getSalesTarget(CounterType.DEALER.getCode(), lastMonth1.getMonthValue(), lastMonth1.getYear());
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

            }
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

                    Double monthSale = salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYear(eydmsUser, currentBaseSite, month, year, null, null);
                    Double lastMonthWiseSale = salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYear(eydmsUser, currentBaseSite,lastMonth1.getMonthValue(), lastMonth1.getYear(),null,null);


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

            }
        }
        else if (Filter.equalsIgnoreCase("YTD")) {
            var salesOfficer = (EyDmsUserModel) userService.getCurrentUser();
            if (taluka.equalsIgnoreCase("ALL")) {
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
            }
            else {
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

            }
        }
        return dataList;
        }




    @Override
    public List<MonthlySalesData> getLastSixMonthSalesForRetailer(String taluka, BaseSiteModel site, String Filter) {
        List<MonthlySalesData> dataList = new ArrayList<>();
        final EyDmsUserModel eydmsUser = (EyDmsUserModel) userService.getCurrentUser();
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
        List<EyDmsCustomerModel> customers = new ArrayList<>();

        if(!Objects.isNull(eydmsUser))
        {
                    if (taluka.equalsIgnoreCase("ALL")) {
                        customers = territoryManagementService.getRetailersForSubArea();
                    }
                    else
                    {
                        customers = territoryManagementService.getRetailersForSubArea(taluka);
                    }

        }

        if (Filter.equalsIgnoreCase("MTD")) {

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
                    sales =  dealerDao.getMonthWiseForRetailerMTD(customers, startDate, endDate);
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
                        currentSales =  dealerDao.getMonthWiseForRetailerYTD(customers, startDate, currentDate);
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

                        data.setMonthYear(Filter);
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
    public List<MonthlySalesData> getLastSixMonthSalesForInfluencer(String taluka, BaseSiteModel site, String filter) {

        List<MonthlySalesData> dataList = new ArrayList<>();
        final EyDmsUserModel eydmsUser = (EyDmsUserModel) userService.getCurrentUser();
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
        List<EyDmsCustomerModel> customers = new ArrayList<>();

        if(!Objects.isNull(eydmsUser))
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
        var subareaMaster=territoryManagementService.getTerritoriesForSO();
        var site=baseSiteService.getCurrentBaseSite();
        return networkDao.getNetworkRemovalForSubArea(subareaMaster,site);
    }

    @Override
    public List<SubAreaMasterModel> getSubAreaForSalesPromoter(EyDmsCustomerModel source) {
        var dealers=getDealersForSalesPromoter(source);
        return Collections.emptyList();//territoryManagementService.getSubAreaForDealers(dealers);
    }

    @Override
    public List<EyDmsUserModel> getSalesOfficersForSubArea(List<SubAreaMasterModel> subAreaMasters) {
        return territoryManagementService.getUsersForSubAreas(subAreaMasters);
    }

    @Override
    public String getTotalOutstandingForPromoter(EyDmsCustomerModel promoter) {
        var dealers=getDealersForSalesPromoter(promoter);
        return df.format(dealers.stream().mapToDouble(dealer->djpVisitDao.getDealerOutstandingAmount(dealer.getCustomerNo())).sum());
    }

    @Override
    public String getOutstandingDaysForPromoter(EyDmsCustomerModel promoter) {
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
    public Integer getNewInfluencerCountMTD(EyDmsCustomerModel eydmsCustomer,BaseSiteModel baseSite, Date startDate, Date endDate,Date doj,String fromCustomerType) {
        return networkDao.getNewInfluencerCountMTD(eydmsCustomer,baseSite,startDate,endDate,doj,fromCustomerType);
    }

    @Override
    public Integer getNewRetailerCountMTD(EyDmsCustomerModel eydmsCustomer,BaseSiteModel baseSite, Date startDate, Date endDate,Date doj) {
        return networkDao.getNewRetailerCountMTD(eydmsCustomer,baseSite,startDate,endDate,doj);
    }

    @Override
    public Integer getRetailerInfluencerCardCountMTD(String customerType,EyDmsCustomerModel eydmsCustomer, BaseSiteModel baseSite, Date startDate, Date endDate, String networkType) {
        List<EyDmsCustomerModel> customerList=new ArrayList<>();
        String fromCustomerType=null;
        if(customerType.equalsIgnoreCase(RETAILER)) {
             customerList = networkDao.getRetailerCardCountMTD(eydmsCustomer, baseSite, startDate, endDate);
        }
        if(customerType.equalsIgnoreCase("INFLUENCER")) {
            if (eydmsCustomer.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                fromCustomerType = "Dealer";
            }
            else if (eydmsCustomer.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                fromCustomerType = "Retailer";
            }
            customerList = networkDao.getInfluencerCardCountMTD(eydmsCustomer, baseSite, startDate, endDate,fromCustomerType);
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
    public Integer getNetworkDormantCountCard(String customerType,EyDmsUserModel currentUser, BaseSiteModel currentBaseSite, Date startDateForCM, Date endDateForCM, String networkType) {
        List<EyDmsCustomerModel> customerFilteredList = new ArrayList<>();
        int countOfInfluencer =0;
        if (customerType.equalsIgnoreCase(RETAILER)) {
            customerFilteredList = territoryManagementService.getRetailersForSubArea();
            return customerFilteredList.isEmpty() ? 0 : customerFilteredList.size();
        } else if (customerType.equalsIgnoreCase(DEALER)) {
            customerFilteredList = territoryManagementService.getDealersForSubArea();
            return customerFilteredList.isEmpty() ? 0 : customerFilteredList.size();
        } else if (customerType.equalsIgnoreCase(INFLUENCER)) {
            customerFilteredList = territoryManagementService.getInfluencersForSubArea().stream().filter(cust -> Objects.nonNull(cust.getInfluencerType()) && Objects.nonNull(cust.getNirmanMitraCode())).collect(Collectors.toList());
            var dormantList = customerFilteredList.stream().filter(cust -> cust.getNetworkType().equals("Dormant")).collect(Collectors.toList());
        }
        var dormantList = customerFilteredList.stream().filter(cust -> cust.getNetworkType().equals("Dormant")).collect(Collectors.toList());
        return dormantList.size();


    }

    public MonthlySalesData getMonthlySalesForDealer(EyDmsCustomerModel customer,LocalDate currentMonth) {
        String customerNo = customer.getCustomerNo() != null ? customer.getCustomerNo() : " ";
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        String customerUid = customer.getUid();
        CustomerCategory category = CustomerCategory.TR;
        var salesOfficer=(EyDmsUserModel) userService.getCurrentUser();
        var subAreas = territoryManagementService.getTerritoriesForSO();
         MonthlySalesData data = new MonthlySalesData();
            Double currentMonthSale = djpVisitDao.getSalesHistoryData(customerNo, currentMonth.getMonthValue(), currentMonth.getYear(), category, currentBaseSite);

            LocalDate lastMonth = currentMonth.minusMonths(1);
            Double lastMonthSale = djpVisitDao.getSalesHistoryData(customerNo, lastMonth.getMonthValue(), lastMonth.getYear(), category, currentBaseSite);

            data.setActualSales(currentMonthSale);
            double growth = currentMonthSale - lastMonthSale;
            data.setGrowth(growth);

            Date date = Date.from(currentMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
            var monthYear = EyDmsDateUtility.getFormattedDate(date, "MMM-YYYY");
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


    public MonthlySalesData getYearlySalesForDealer(EyDmsCustomerModel customer,LocalDate currentMonth) {
        String customerNo = customer.getCustomerNo() != null ? customer.getCustomerNo() : " ";
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        String customerUid = customer.getUid();
        CustomerCategory category = CustomerCategory.TR;
        var salesOfficer=(EyDmsUserModel) userService.getCurrentUser();
        var subAreas = territoryManagementService.getTerritoriesForSO();
        MonthlySalesData data = new MonthlySalesData();
        Double currentMonthSale = djpVisitDao.getSalesHistoryData(customerNo, currentMonth.getMonthValue(), currentMonth.getYear(), category, currentBaseSite);

        LocalDate lastMonth = currentMonth.minusMonths(1);
        Double lastMonthSale = djpVisitDao.getSalesHistoryData(customerNo, lastMonth.getMonthValue(), lastMonth.getYear(), category, currentBaseSite);

        data.setActualSales(currentMonthSale);
        double growth = currentMonthSale - lastMonthSale;
        data.setGrowth(growth);

        Date date = Date.from(currentMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
        var monthYear = EyDmsDateUtility.getFormattedDate(date, "MMM-YYYY");
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
        DecimalFormat df = new DecimalFormat("#.#");
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
    public List<EyDmsCustomerModel> getCustomerListFromSubArea(String subArea,BaseSiteModel site){
        return eydmsUserDao.getAllCustomerForSubArea(subArea, site);
    }

    @Override
    public List<MeetingScheduleModel> getInfluencerMeetCards() {
        return eydmsUserDao.getInfluencerMeetCards(userService.getCurrentUser());
    }

  //To be Checked
    @Override
    public Map<String, Integer> getCounterInfoForTaluka(String leadType, String taluka) {
        Map<String, Integer> counterInfoMap = new HashMap<>();
        if (leadType.equalsIgnoreCase(DEALER)) {
            RequestCustomerData requestCustomerData=new RequestCustomerData();
            requestCustomerData.setCounterType(List.of("Dealer"));
            //List<EyDmsCustomerModel> shreeCounters = territoryManagementService.getCustomerforUser(requestCustomerData).stream().filter(eydmsCustomerModel -> eydmsCustomerModel.getIsShreeSite().equals(Boolean.TRUE)).collect(Collectors.toList());
            List<EyDmsCustomerModel> shreeCounters = territoryManagementService.getDealersForSubArea(taluka);
            LOG.info(String.format("Shree Counter :: %s",shreeCounters.size()));
            List<EyDmsCustomerModel> totalCounters = territoryManagementService.getCustomerforUser(requestCustomerData);
           // List<EyDmsCustomerModel> totalCounters= territoryManagementService.getEYDMSAndNonEYDMSAllForSO().stream().filter(eydmsCustomerModel -> eydmsCustomerModel.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());
            //List<EyDmsCustomerModel> totalCounters= territoryManagementService.getAllEyDmsAndNonEyDmsCustomerForTerritories(taluka);

            LOG.info(String.format("totalCounters :%s",totalCounters.size()));
            counterInfoMap.put("totalCounter", totalCounters.size());
            counterInfoMap.put("shreeCounter", shreeCounters.size());
        } else if (leadType.equalsIgnoreCase(RETAILER)) {
            RequestCustomerData requestCustomerData=new RequestCustomerData();
            requestCustomerData.setCounterType(List.of("Retailer"));
            List<EyDmsCustomerModel> shreeCounters = territoryManagementService.getRetailersForSubArea(taluka);
            LOG.info(String.format("Shree Counter :: %s",shreeCounters.size()));
            //List<EyDmsCustomerModel> totalCounters = territoryManagementService.getEYDMSAndNonEYDMSAllForSO().stream().filter(eydmsCustomerModel -> eydmsCustomerModel.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))).collect(Collectors.toList());
           // List<EyDmsCustomerModel> totalCounters= territoryManagementService.getAllEyDmsAndNonEyDmsCustomerForTerritories(taluka);
            List<EyDmsCustomerModel> totalCounters= territoryManagementService.getCustomerforUser(requestCustomerData);
            LOG.info(String.format("totalCounters :: %s",totalCounters.size()));
            //List<EyDmsCustomerModel> shreeCounters = territoryManagementService.getCustomerforUser(requestCustomerData).stream().filter(eydmsCustomerModel -> eydmsCustomerModel.getIsShreeSite().equals(Boolean.TRUE)).collect(Collectors.toList());
            //List<EyDmsCustomerModel> totalCounters = territoryManagementService.getCustomerforUser(requestCustomerData);
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
    public Map<String, Integer> getNetworkTypeCount(String leadType) {
        Map<String, Integer> networkTypeCountMap = new HashMap<>();
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        List<EyDmsCustomerModel> customerList=getCustomersForLead(leadType);
        if(!customerList.isEmpty()) {
        	List<EyDmsCustomerModel> applicableCustomers=customerList.stream().filter(cust->Objects.nonNull(cust.getNetworkType())).collect(Collectors.toList());
            int total=applicableCustomers.size();
            if(total>0) {
            	List<EyDmsCustomerModel> active = applicableCustomers.stream().filter(cus -> "Active".equalsIgnoreCase(cus.getNetworkType())).collect(Collectors.toList());
            	List<EyDmsCustomerModel> inActive = applicableCustomers.stream().filter(cus -> "Inactive".equalsIgnoreCase(cus.getNetworkType())).collect(Collectors.toList());
            	List<EyDmsCustomerModel> dormant = applicableCustomers.stream().filter(cus -> "Dormant".equalsIgnoreCase(cus.getNetworkType())).collect(Collectors.toList());
                networkTypeCountMap.put("active", active.size());
                networkTypeCountMap.put("activeShare", EYDMSDataFormatUtil.calculatePercentage(active.size(), total));
                networkTypeCountMap.put("inActive", inActive.size());
                networkTypeCountMap.put("inActiveShare", EYDMSDataFormatUtil.calculatePercentage(inActive.size(), total));
                networkTypeCountMap.put("dormant", dormant.size());
                networkTypeCountMap.put("dormantShare", EYDMSDataFormatUtil.calculatePercentage(dormant.size(), total));
            }
        }
        if(userService.getCurrentUser() instanceof EyDmsCustomerModel){
            //if(((EyDmsCustomerModel) userService.getCurrentUser()).getUserType().getCode().equalsIgnoreCase("SP"))
            if(((EyDmsCustomerModel) userService.getCurrentUser()).getCounterType().getCode().equalsIgnoreCase("SP")){
                RequestCustomerData requestCustomerData = new RequestCustomerData();
                requestCustomerData.setCounterType(List.of(leadType));
                List<EyDmsCustomerModel> customerForUser = territoryManagementService.getCustomerforUser(requestCustomerData);
                if(!customerForUser.isEmpty()){
                    List<EyDmsCustomerModel> applicableCustomersForUser = customerForUser.stream().filter(customer->Objects.nonNull(customer.getNetworkType())).collect(Collectors.toList());
                    int sum = applicableCustomersForUser.size();
                    if(sum>0){
                        List<EyDmsCustomerModel> active = applicableCustomersForUser.stream().filter(custom-> "Active".equalsIgnoreCase(custom.getNetworkType())).collect(Collectors.toList());
                        List<EyDmsCustomerModel> inActive = applicableCustomersForUser.stream().filter(custom-> "InActive".equalsIgnoreCase(custom.getNetworkType())).collect(Collectors.toList());
                        List<EyDmsCustomerModel> dormant = applicableCustomersForUser.stream().filter(custom-> "Dormant".equalsIgnoreCase(custom.getNetworkType())).collect(Collectors.toList());
                        networkTypeCountMap.put("active", active.size());
                        networkTypeCountMap.put("activeShare", EYDMSDataFormatUtil.calculatePercentage(active.size(), sum));
                        networkTypeCountMap.put("inActive", inActive.size());
                        networkTypeCountMap.put("inActiveShare", EYDMSDataFormatUtil.calculatePercentage(inActive.size(), sum));
                        networkTypeCountMap.put("dormant", dormant.size());
                        networkTypeCountMap.put("dormantShare", EYDMSDataFormatUtil.calculatePercentage(dormant.size(), sum));
                    }
                }

            }
        }
        else if (currentUser.getUserType().getCode().equals("TSM")) {
            RequestCustomerData requestCustomerData = new RequestCustomerData();
            requestCustomerData.setCounterType(List.of(leadType));
            List<EyDmsCustomerModel> customerForUserTSM = territoryManagementService.getCustomerforUser(requestCustomerData);
            if (!customerForUserTSM.isEmpty()) {
                List<EyDmsCustomerModel> applicableCustomersForUserTSM = customerForUserTSM.stream().filter(customer -> Objects.nonNull(customer.getNetworkType())).collect(Collectors.toList());
                int sum = applicableCustomersForUserTSM.size();
                if (sum > 0) {
                    List<EyDmsCustomerModel> active = applicableCustomersForUserTSM.stream().filter(custom -> "Active".equalsIgnoreCase(custom.getNetworkType())).collect(Collectors.toList());
                    List<EyDmsCustomerModel> inActive = applicableCustomersForUserTSM.stream().filter(custom -> "InActive".equalsIgnoreCase(custom.getNetworkType())).collect(Collectors.toList());
                    List<EyDmsCustomerModel> dormant = applicableCustomersForUserTSM.stream().filter(custom -> "Dormant".equalsIgnoreCase(custom.getNetworkType())).collect(Collectors.toList());
                    networkTypeCountMap.put("active", active.size());
                    networkTypeCountMap.put("activeShare", EYDMSDataFormatUtil.calculatePercentage(active.size(), sum));
                    networkTypeCountMap.put("inActive", inActive.size());
                    networkTypeCountMap.put("inActiveShare", EYDMSDataFormatUtil.calculatePercentage(inActive.size(), sum));
                    networkTypeCountMap.put("dormant", dormant.size());
                    networkTypeCountMap.put("dormantShare", EYDMSDataFormatUtil.calculatePercentage(dormant.size(), sum));

                }
            }
        }
        else if (currentUser.getUserType().getCode().equals("RH")) {
            RequestCustomerData requestCustomerData = new RequestCustomerData();
            requestCustomerData.setCounterType(List.of(leadType));
            List<EyDmsCustomerModel> customerForUserRH = territoryManagementService.getCustomerforUser(requestCustomerData);
            if (!customerForUserRH.isEmpty()) {
                List<EyDmsCustomerModel> applicableCustomersForUserRH = customerForUserRH.stream().filter(customer -> Objects.nonNull(customer.getNetworkType())).collect(Collectors.toList());
                int sum = applicableCustomersForUserRH.size();
                if (sum > 0) {
                    List<EyDmsCustomerModel> active = applicableCustomersForUserRH.stream().filter(custom -> "Active".equalsIgnoreCase(custom.getNetworkType())).collect(Collectors.toList());
                    List<EyDmsCustomerModel> inActive = applicableCustomersForUserRH.stream().filter(custom -> "InActive".equalsIgnoreCase(custom.getNetworkType())).collect(Collectors.toList());
                    List<EyDmsCustomerModel> dormant = applicableCustomersForUserRH.stream().filter(custom -> "Dormant".equalsIgnoreCase(custom.getNetworkType())).collect(Collectors.toList());
                    networkTypeCountMap.put("active", active.size());
                    networkTypeCountMap.put("activeShare", EYDMSDataFormatUtil.calculatePercentage(active.size(), sum));
                    networkTypeCountMap.put("inActive", inActive.size());
                    networkTypeCountMap.put("inActiveShare", EYDMSDataFormatUtil.calculatePercentage(inActive.size(), sum));
                    networkTypeCountMap.put("dormant", dormant.size());
                    networkTypeCountMap.put("dormantShare", EYDMSDataFormatUtil.calculatePercentage(dormant.size(), sum));
                }
            }
        }
        return networkTypeCountMap;
    }

    private String getGroupForLead(String leadType) {
        String userGroup="";
        switch (leadType) {
            case DEALER:
                userGroup = EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID;
                break;
            case RETAILER:
                userGroup = EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID;
                break;
            case "INFLUENCER":
                userGroup = EyDmsCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID;
                break;
        }
        return userGroup;
    }

    private List<EyDmsCustomerModel> getCustomersForLead(String leadType) {
        List<EyDmsCustomerModel> customerList=new ArrayList<>();
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        if(currentUser instanceof EyDmsUserModel){
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
        else if(currentUser instanceof EyDmsCustomerModel){
            if((currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))){
                switch (leadType) {
                    case RETAILER:
                        customerList = territoryManagementService.getRetailerListForDealer();
                        break;
                    case "INFLUENCER":
                        customerList = territoryManagementService.getInfluencerListForDealer();
                        break;
                }
            }
            else if((currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
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
    public List<EyDmsCustomerModel> getEyDmsCustomerForGroupAndSO(String userGroupUid,String uid) {
        var customerList = territoryManagementService.getAllCustomerForSO(uid);
        return customerList.stream().filter(cust -> cust.getGroups().contains(userService.getUserGroupForUID(userGroupUid))).collect(Collectors.toList());
    }

    @Override
    public double getLostSaleForCustomer(EyDmsCustomerModel user) {
       
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
    public List<EyDmsCustomerModel> getDealersForSalesPromoter(EyDmsCustomerModel promoter) {
        return networkDao.getDealersForSalesPromoter(promoter);
    }

    @Override
    public String getSPNetworkPotentialMTD(EyDmsCustomerModel promoter) {
        var dealers=networkDao.getNetworkDealersForSalesPromoter(promoter,territoryManagementService.getTerritoriesForSO());
        return getZeroIfEmpty(dealers.stream().filter(cust->Objects.nonNull(cust.getCounterPotential())).mapToDouble(EyDmsCustomerModel::getCounterPotential).sum());
    }

    @Override
    public String getSPNetwokSalesMTD(EyDmsCustomerModel promoter) {
        var customers=networkDao.getNetworkDealersForSalesPromoter(promoter,territoryManagementService.getTerritoriesForSO());
        return getZeroIfEmpty(customers.stream().mapToDouble(dealer->getCurrentMonthSaleQty(dealer.getUid(),baseSiteService.getCurrentBaseSite())).sum());
    }

    private List<EyDmsCustomerModel> getEyDmsCustomerForPromotor(EyDmsCustomerModel promoter) {
        var subAreas=territoryManagementService.getTerritoriesForPromoter(promoter);
        var eydmscustomerList=territoryManagementService.getAllCustomerForSubArea(subAreas);
        return eydmscustomerList.stream().filter(cust -> !cust.getGroups().contains(userService.getUserGroupForUID("salespromotergroup"))).filter(cust->cust.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).filter(cust->Objects.nonNull(cust.getCustomerNo())).collect(Collectors.toList());
    }

    @Override
    public Double getSPNetworkShare(EyDmsCustomerModel promoter) {
        var customers=networkDao.getNetworkDealersForSalesPromoter(promoter,territoryManagementService.getTerritoriesForSO());;
        var site=baseSiteService.getCurrentBaseSite();
       var totalSales=customers.stream().mapToDouble(cust->getCurrentMonthSaleQty(cust.getUid(),site)).sum();
       var totalPotential=customers.stream().filter(cust->Objects.nonNull(cust.getCounterPotential())).mapToDouble(EyDmsCustomerModel::getCounterPotential).sum();
      if(totalPotential==0.0){
          return 0.0;
      }
       return (totalSales*100)/totalPotential;
    }

    public Integer getCounterShareForDealer(EyDmsCustomerModel dealer,BaseSiteModel site) {
        var totalSales=getCurrentMonthSaleQty(dealer.getUid(),site);
       if(Objects.nonNull(dealer.getCounterPotential()) && dealer.getCounterPotential()>0.0){
           return (int)(totalSales*100/dealer.getCounterPotential());
       }
        return 0;
    }

    @Override
    public List<EyDmsCustomerModel> getEyDmsCustomerForGroup(String userGroupUid) {
        var customerList = territoryManagementService.getAllCustomerForSO();
        return customerList.stream().filter(cust -> cust.getGroups().contains(userService.getUserGroupForUID(userGroupUid))).collect(Collectors.toList());
    }



    @Override
    public EYDMSPotentialCustomerListData getTopPotentialCustomerListData(String leadType){
        LOGGER.debug(String.format("Getting Top 10 Potential Customer for LeadType :: %s ",leadType));
        List<EyDmsCustomerModel> customerList=new ArrayList<>();
        List<EYDMSPotentialCustomerData> eydmsPotentialCustomerDataList = new ArrayList<>();
        EYDMSPotentialCustomerListData eydmsPotentialCustomerListData = new EYDMSPotentialCustomerListData();

        if(leadType.equalsIgnoreCase(RETAILER)){
            LOGGER.debug(String.format("Filtering Customer for LeadType :: %s ",leadType));
            customerList=territoryManagementService.getRetailersForSubArea();
        }
        else if(leadType.equalsIgnoreCase(DEALER)){
            LOGGER.debug(String.format("Filtering Customer for LeadType :: %s ",leadType));
            customerList=territoryManagementService.getDealersForSubArea();
        }
        customerList=
                customerList.stream().filter(cust->Objects.nonNull(cust.getCounterPotential())).sorted(Comparator.comparing(EyDmsCustomerModel::getCounterPotential).reversed()).limit(10).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(customerList)) {
            customerList.forEach(customer -> {
                var networkModel=findNetworkByCode(customer.getUid());
                if(Objects.nonNull(networkModel)) {
                    EYDMSPotentialCustomerData data = new EYDMSPotentialCustomerData();
                    data.setDealer(customer.getName());
                        data.setStage("Potential");
                        data.setPotential(String.valueOf(customer.getCounterPotential()));
                        eydmsPotentialCustomerDataList.add(data);
                }
            });
        }
        LOGGER.debug(String.format("Setting Customer Data for LeadType :: %s ",leadType));
        eydmsPotentialCustomerListData.setEyDmsPotentialCustomer(eydmsPotentialCustomerDataList);
        return eydmsPotentialCustomerListData;
    }

    @Override
    public RetailerOnboardListData getOnboarderRetailerData(LeadType leadType, String searchKey){
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        double totalSalesCount = 0;
        RetailerOnboardListData retailerOnboardListData = new RetailerOnboardListData();
        List<RetailerOnboardData> retailerOnboardDataList = new ArrayList<>();
        List<EyDmsCustomerModel> retailersForSubArea=new ArrayList<>();
        if(leadType.getCode().equalsIgnoreCase(RETAILER)){
        if(currentUser instanceof EyDmsUserModel) {
            retailersForSubArea  = territoryManagementService.getRetailersForSubArea();
        }
        else if(currentUser instanceof EyDmsCustomerModel){
            if(currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))){
                retailersForSubArea  = territoryManagementService.getRetailerListForDealer();
            }
        }

            var threeMonthBackDate=EyDmsDateUtility.getThreeMonthBackDate();
            var newRetailerList=retailersForSubArea.stream().filter(cust->Objects.nonNull(cust.getCustomerNo()) && Objects.nonNull(cust.getDateOfJoining()) && cust.getDateOfJoining().after(threeMonthBackDate)).collect(Collectors.toList());
            if(StringUtils.isNotBlank(searchKey) )
            {
                newRetailerList = filterEyDmsCustomersWithSearchTerm(newRetailerList,searchKey);
            }

            for(EyDmsCustomerModel customer : newRetailerList){
                    RetailerOnboardData retailerOnboardData = new RetailerOnboardData();
                    retailerOnboardData.setCode(customer.getUid());
                    retailerOnboardData.setName(customer.getName());
                    retailerOnboardData.setPotential(Objects.nonNull(customer.getCounterPotential()) ? String.valueOf(customer.getCounterPotential()) : ZERO);
                   /*var salesHistry = getMitraSalesDataForCustomer(customer.getCustomerNo(), baseSiteService.getCurrentBaseSite(), EyDmsCoreConstants.DJP.RETAILER_TRANSACTION_TYPE);
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
        var monthStart=EyDmsDateUtility.getMonthStartDate();
        var bagLifted=salesHistry.stream().filter(sale->sale.getTransactionDate().after(monthStart)).mapToDouble(NirmanMitraSalesHistoryModel::getTransactionQuantity).sum();
        return bagLifted/20;
    }
    @Override
    public ChannelStrength getChannelKPIGraphDealerRetailer(String leadType) {

        double numericReach = 0.0;
        double eydmsCountersSales = 0.0;
        double allCountersSales = 0.0;
        Double eydmsSalesAtEyDmsCounters = 0.0;
        Double allSalesAtEyDmsCounters = 0.0;
        ChannelStrength channelStrength = new ChannelStrength();
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();

        CustomerCategory category = CustomerCategory.TR;
        List<EyDmsCustomerModel> dealersForSubArea=new ArrayList<>();
        List<EyDmsCustomerModel> retailersForSubArea=new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.get(Calendar.MONTH);
        if (leadType.equalsIgnoreCase(DEALER)) {

                dealersForSubArea = territoryManagementService.getDealersForSubArea();

            List<EyDmsCustomerModel> collect = territoryManagementService.getEYDMSAndNonEYDMSAllForSO().stream().filter(eydmsCustomerModel -> eydmsCustomerModel.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(dealersForSubArea) && CollectionUtils.isNotEmpty(collect)) {
                numericReach = (((double) dealersForSubArea.size() / collect.size()) * 100);
            }
            eydmsCountersSales = getSummationSalesOfCounters(dealersForSubArea);
            allCountersSales = getSummationSalesOfCounters(collect);


            var customerNos = dealersForSubArea.stream().map(EyDmsCustomerModel::getCustomerNo).collect(Collectors.toList());

            LocalDate date=LocalDate.now();
            LocalDate startDate = LocalDate.of(date.getYear(),date.getMonth(),1);
            if(customerNos!=null) {
                LOGGER.info(String.format("Getting CurrentMonthSaleQuantity for customerCode :: %s startDate ::%s endDate :: %s ", customerNos, startDate, date));
            }
            //eydmsSalesAtEyDmsCounters = djpVisitDao.getSalesHistoryDataForDealerList(customerNos, 0, 0, category, currentBaseSite);
            if(customerNos!=null) {
                eydmsSalesAtEyDmsCounters = eydmsUserDao.getSalesQuantityForCustomerList(customerNos, getStringDate(startDate), getStringDate(date), currentBaseSite);
            }
                allSalesAtEyDmsCounters = sumOfAllSalesAtEyDmsCounters(collect);

                channelStrength.setShreeNumeric((double) dealersForSubArea.size());
                channelStrength.setTotalNumeric((double) collect.size());

        } else if (leadType.equalsIgnoreCase(RETAILER)) {

                retailersForSubArea = territoryManagementService.getRetailersForSubArea();


            List<EyDmsCustomerModel> collect = territoryManagementService.getEYDMSAndNonEYDMSAllForSO().stream().filter(eydmsCustomerModel -> eydmsCustomerModel.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(retailersForSubArea) && CollectionUtils.isNotEmpty(collect)) {
                numericReach = (((double) retailersForSubArea.size() / collect.size()) * 100);
            }
            eydmsCountersSales = getSummationSalesOfCounters(retailersForSubArea);
            allCountersSales = getSummationSalesOfCounters(collect);

            eydmsSalesAtEyDmsCounters = retailersForSubArea.stream().filter(cust -> Objects.nonNull(cust.getLastCounterVisit()) && Objects.nonNull(cust.getLastCounterVisit().getMarketMapping())).flatMap(cust -> cust.getLastCounterVisit().getMarketMapping().stream()).filter(m -> Objects.nonNull(m.getBrand()) && currentBaseSite.equals(m.getBrand().getEyDmsBrand())).filter(map -> Objects.nonNull(map.getWholeSales()) && Objects.nonNull(map.getRetailSales())).mapToDouble(m -> m.getWholeSales() + m.getRetailSales()).sum();
            allSalesAtEyDmsCounters = sumOfAllSalesAtEyDmsCounters(collect);

            channelStrength.setShreeNumeric((double) retailersForSubArea.size());
            channelStrength.setTotalNumeric((double) collect.size());
        }

        channelStrength.setNumericReach(numericReach);
        if (allCountersSales != 0.0) {
            channelStrength.setAcv((eydmsCountersSales / allCountersSales) * 100);
            channelStrength.setShreeAcv(eydmsCountersSales);
            channelStrength.setTotalAcv(allCountersSales);
        } else {
            channelStrength.setAcv(0.0);
            channelStrength.setShreeAcv(eydmsCountersSales);
            channelStrength.setTotalAcv(allCountersSales);
        }

        if (allSalesAtEyDmsCounters != 0.0) {
            channelStrength.setDepth((eydmsSalesAtEyDmsCounters / allSalesAtEyDmsCounters) * 100);
            channelStrength.setShreeDepth(eydmsSalesAtEyDmsCounters);
            channelStrength.setTotalDepth(allSalesAtEyDmsCounters);
        } else {
            channelStrength.setDepth(0.0);
            channelStrength.setShreeDepth(eydmsSalesAtEyDmsCounters);
            channelStrength.setTotalDepth(allSalesAtEyDmsCounters);
        }


        return channelStrength;

    }

    private Double sumOfAllSalesAtEyDmsCounters(List<EyDmsCustomerModel> customerModels) {
        return customerModels.stream().filter(cust->Objects.nonNull(cust.getLastCounterVisit()) && Objects.nonNull(cust.getLastCounterVisit().getMarketMapping())).flatMap(cust->cust.getLastCounterVisit().getMarketMapping().stream()).filter(map->Objects.nonNull(map.getWholeSales()) && Objects.nonNull(map.getRetailSales())).mapToDouble(m->m.getWholeSales()+m.getRetailSales()).sum();
    }

    private double getSummationSalesOfCounters(List<EyDmsCustomerModel> customerModels) {
        return customerModels.stream().filter(c->c.getCounterPotential()!=null).mapToDouble(EyDmsCustomerModel::getCounterPotential).sum();
    }

    @Override
    public List<EyDmsCustomerModel> getInActiveCustomers(String dealerUserGroupUid) {
        var dealers=territoryManagementService.getDealersForSubArea();
        return dealers.stream().filter(dealer->NetworkType.DORMANT.getCode().equals(dealer.getNetworkType())).collect(Collectors.toList());
    }



    @Override
    public SiteSummaryData getSiteSummaryforNetwork(String customerCode) {
        CounterVisitMasterModel counterVisitId = networkDao.getVisitIdByEyDmsCustomer(customerCode);
        SiteSummaryData summaryData = new SiteSummaryData();
        if(counterVisitId!=null) {
            CounterVisitMasterModel counterVisit = counterVisitDao.findCounterVisitById(counterVisitId.getId());

            try {
                EyDmsCustomerModel eydmsCustomer = counterVisit.getEyDmsCustomer();
                summaryData.setPocName(eydmsCustomer.getContactPersonName());
                summaryData.setPocContact(eydmsCustomer.getCustomerNo());
                summaryData.setLastVisitDate(eydmsCustomer.getLastVisitTime());
                summaryData.setNextFollowUpDate(eydmsCustomer.getNextFollowUp());
                summaryData.setConstructionArea(eydmsCustomer.getAreaOfConstruction());
                try {
                    summaryData.setConstructionStatus(eydmsCustomer.getCurrentStageOfConstruction().getCode());
                } catch (NullPointerException n) {
                    LOG.debug(n);
                }
                summaryData.setMonthlyConsumption(eydmsCustomer.getMonthlyConsumption());
                summaryData.setBalancePotential(eydmsCustomer.getBalancePotential());
            } catch (NullPointerException e) {
                LOG.debug(e);
            }

        }

        return summaryData;
    }

    @Override
    public List<EyDmsCustomerModel> filterEyDmsCustomersWithSearchTerm(List<EyDmsCustomerModel> customers,String searchTerm) {
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
    public List<LeadMasterModel> getAllLeads(LeadType leadType,String monthYear,String searchTerm) {
        List<LeadMasterModel> leadmasterList=new ArrayList<>();
        if(Objects.isNull(monthYear)) {
        	if(userService.getCurrentUser().getClass().equals(EyDmsUserModel.class))
        		{
        			leadmasterList.addAll(networkDao.getAllLeadsToDate(null, null,territoryManagementService.getTerritoriesForSO() ));
        		}
        	else
        		{
        		leadmasterList.addAll(networkDao.getAllLeadsToDate(null, null,territoryManagementService.getTerritoriesForCustomer(((EyDmsCustomerModel)userService.getCurrentUser()))));
        		}
            
        }else{
            leadmasterList.addAll(getAllLeadsForMonth(monthYear));
        }
        if(Objects.nonNull(leadType)){
            if(leadType.getCode().equalsIgnoreCase(DEALER) || leadType.getCode().equalsIgnoreCase(RETAILER) || leadType.getCode().equalsIgnoreCase(SITE) || leadType.getCode().equalsIgnoreCase(TPC)){
                leadmasterList=filterLeadsForType(leadmasterList,leadType);
            }else{
                leadmasterList= filterLeadsForInfluencer(leadmasterList);
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


    private List<LeadMasterModel> getAllLeadsForMonth(String monthYear) {
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
        cal.add(Calendar.DAY_OF_MONTH,-1);
        Date end=cal.getTime();
        return networkDao.getAllLeadsToDate(start,end,territoryManagementService.getTerritoriesForSO() );
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

    private Double getYearToYearGrowthForDealer(String customerCode, BaseSiteModel site){
        LocalDate currentYearCurrentDate= LocalDate.now();
        LocalDate currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear(), Month.APRIL, 1);
        if(currentYearCurrentDate.getMonth().compareTo(Month.APRIL)<0) {
            currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear()-1, Month.APRIL, 1);
        }
        LocalDate lastYearCurrentDate= LocalDate.now().minusYears(1);

        LocalDate lastFinancialYearDate = currentFinancialYearDate.minusYears(1);

        LOGGER.info(String.format("Getting yoy growth for customerNo :: %s currentYearCurrentDate :: %s currentFinancialYearDate :: %s lastFinancialYearDate :: %s",customerCode,currentYearCurrentDate,currentFinancialYearDate,lastFinancialYearDate));
        double lastSale = eydmsUserDao.getSalesQuantity(customerCode, getStringDate(lastFinancialYearDate), getStringDate(lastYearCurrentDate));
        double currentSale = eydmsUserDao.getSalesQuantity(customerCode, getStringDate(currentFinancialYearDate), getStringDate(currentYearCurrentDate));
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
    private SalesQuantityData setSalesQuantityForCustomer(EyDmsCustomerModel customerModel,String leadType, BaseSiteModel site){
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
        return eydmsUserDao.getSalesQuantity(customerCode, getStringDate(startDate), getStringDate(endDate));
    }

    private double getCurrentMonthSaleQty(String customerCode, BaseSiteModel site){
        LocalDate date=LocalDate.now().plusDays(1);
        LocalDate startDate = LocalDate.of(date.getYear(),date.getMonth(),1);
        LOGGER.info(String.format("Getting CurrentMonthSaleQuantity for customerCode :: %s startDate ::%s endDate :: %s ",customerCode,startDate,date));
        return eydmsUserDao.getSalesQuantity(customerCode, getStringDate(startDate), getStringDate(date));
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
    	
    	EyDmsCustomerModel user = eydmsCustomerService.getEyDmsCustomerForUid(code);
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
	    if(user.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
	    	data.setNetworkCategory(NetworkCategory.DEALER.getCode());
	    }
	    if(user.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
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

        double pendingOrderAmount =orderValidationProcessDao.getPendingOrderAmount(user.getPk().toString());
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
    	        
    	        if(user.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
    		    	model.setNetworkCategory(NetworkCategory.DEALER);
    		    }
    		    if(user.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
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
    public List<EyDmsUserModel> getOtherNetworkSO() {
        var currentSO=userService.getCurrentUser();
        //var districtList=territoryManagementService.getAllDistrictForSO(currentSO);
        //var users=territoryManagementService.getAllUserForDistrict(districtList);
        return Collections.emptyList();//users.stream().filter(user->!user.getUid().equals(currentSO.getUid())).collect(Collectors.toList());
    }


	@Override
	public List<EYDMSImageData> getOnboardingFormsSS(String uid) {
		EyDmsCustomerModel customer = (EyDmsCustomerModel) userService.getUserForUID(uid);
		List<MediaModel> documents = customer.getOnboardingFormsImages();
		
		List<EYDMSImageData> formSS = new ArrayList<>();
		
		for(MediaModel ss : documents)
		{
			EYDMSImageData form = new EYDMSImageData();
			form.setUrl(ss.getURL());
			
			formSS.add(form);
		}
		
		return formSS;
	}

    @Override
    public SalesHistoryData getSalesHistoryDataForNetworkInfluencer360(EyDmsCustomerModel eydmsCustomer){

        UserGroupModel influencerGroup = userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID);
        UserGroupModel retailerGroup = userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);

        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();

        String transactionType = "";
        String customerCode="";
        LocalDate date=LocalDate.now();
        LocalDate lastMonth=date.minusMonths(1);
        LocalDate lastTolastMonth = date.minusMonths(2);
        LocalDate lastYearSameMonth = date.minusYears(1);

        SalesHistoryData data= new SalesHistoryData();
        double lastMonthSales=0.0,secondLastMonthSales=0.0,lastYearSameMonthSales=0.0,actualMonthSale=0.0;

        if(eydmsCustomer.getGroups() !=null && eydmsCustomer.getGroups().contains(influencerGroup))
        {
           /* transactionType = EyDmsCoreConstants.DJP.INFLEUNCER_TRANSACTION_TYPE;
            customerCode=eydmsCustomer.getNirmanMitraCode();*/
            lastMonthSales = networkDao.getSalesQuantityForInfluencerMonthYear(eydmsCustomer,currentBaseSite,lastMonth.getMonthValue(), lastMonth.getYear());
            secondLastMonthSales =networkDao.getSalesQuantityForInfluencerMonthYear(eydmsCustomer,currentBaseSite, lastTolastMonth.getMonthValue(), lastTolastMonth.getYear());
            lastYearSameMonthSales =networkDao.getSalesQuantityForInfluencerMonthYear(eydmsCustomer,currentBaseSite, lastYearSameMonth.getMonthValue(), lastYearSameMonth.getYear());
            actualMonthSale = networkDao.getSalesQuantityForInfluencerMonthYear(eydmsCustomer, currentBaseSite, date.getMonthValue(), date.getYear());
        }
        else if(eydmsCustomer.getGroups()!=null && eydmsCustomer.getGroups().contains(retailerGroup))
        {
          /*  transactionType=EyDmsCoreConstants.DJP.RETAILER_TRANSACTION_TYPE;
            customerCode=eydmsCustomer.getCustomerNo();*/
            lastMonthSales = networkDao.getSalesQuantityForRetailerMonthYear(eydmsCustomer,currentBaseSite,lastMonth.getMonthValue(), lastMonth.getYear());
            secondLastMonthSales =networkDao.getSalesQuantityForRetailerMonthYear(eydmsCustomer,currentBaseSite, lastTolastMonth.getMonthValue(), lastTolastMonth.getYear());
            lastYearSameMonthSales =networkDao.getSalesQuantityForRetailerMonthYear(eydmsCustomer,currentBaseSite, lastYearSameMonth.getMonthValue(), lastYearSameMonth.getYear());
            actualMonthSale = networkDao.getSalesQuantityForRetailerMonthYear(eydmsCustomer, currentBaseSite, date.getMonthValue(), date.getYear());
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

        if(eydmsCustomer.getGroups()!=null && eydmsCustomer.getGroups().contains(retailerGroup)) {
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
    public Integer getLeadsGeneratedCountedForInfluencer(String filter, EyDmsCustomerModel customerModel, BaseSiteModel brand) {
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

}

