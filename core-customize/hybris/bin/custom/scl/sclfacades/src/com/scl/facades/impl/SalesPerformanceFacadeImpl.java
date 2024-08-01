package com.scl.facades.impl;

import com.scl.core.brand.dao.BrandDao;
import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.*;
import com.scl.core.dao.impl.SalesPerformanceDaoImpl;
import com.scl.core.enums.CounterType;
import com.scl.core.jalo.SclCustomer;
import com.scl.core.model.*;
import com.scl.core.region.dao.DistrictMasterDao;
import com.scl.core.services.*;
import com.scl.core.utility.SclDateUtility;
import com.scl.facades.SalesPerformanceFacade;
import com.scl.facades.data.*;
import com.scl.facades.network.SCLNetworkFacade;
import com.scl.facades.region.GeographicalRegionFacade;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.GenericSearchConstants;
import de.hybris.platform.core.model.c2l.BrandModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SalesPerformanceFacadeImpl implements SalesPerformanceFacade {

    private static final int LOWPERCENTAGE = 20;
    @Autowired
    Converter<SclCustomerModel, SalesPerformNetworkDetailsData> salesPerformnceCustomerDetailsConverter;

    private static final Logger LOG = Logger.getLogger(SalesPerformanceFacadeImpl.class);

    private static final String ZERO = " 0";
    private static final String PERCENTAGE = "%";

    private static final String DEALER = "DEALER";
    private static final String RETAILER = "RETAILER";
    private static final String INFLUENCER = "INFLUENCER";
    private static final String FILTER_MTD = "MTD";
    private static final String FILTER_YTD = "YTD";
    private static final String FILTER_LM = "LastMonth";
    private static final String FILTER_LY = "LastYear";
    DecimalFormat df = new DecimalFormat("#0.00");
    private String START_MONTH="startMonth";
    private String START_YEAR="startYear";
    private String END_MONTH="endMonth";
    private String END_YEAR="endYear";


    private static final Logger LOGGER = Logger.getLogger(SalesPerformanceFacadeImpl.class);

    @Resource
    private UserService userService;
    @Autowired
    DJPVisitDao djpVisitDao;
    @Autowired
    SclSalesSummaryDao sclSalesSummaryDao;
    @Autowired
    EnumerationService enumerationService;
    @Resource
    private BaseSiteService baseSiteService;
    @Autowired
    SCLNetworkFacade networkFacade;
    @Autowired
    TerritoryManagementDao territoryManagementDao;
    @Resource
    TerritoryMasterService territoryMasterService;
    @Autowired
    DistrictMasterDao districtMasterDao;
    @Resource
    private SalesPerformanceService salesPerformanceService;
    @Resource
    private Converter<SclCustomerModel, InfluencerSummaryData> influencerSummaryConverter;
    @Resource
    private SalesPlanningService salesPlanningService;
    @Autowired
    BrandDao brandDao;

    @Resource
    private TerritoryManagementService territoryManagementService;
    @Autowired
    NetworkService networkService;
    @Resource
    GeographicalRegionFacade geographicalRegionFacade;
    @Resource
    private SalesPerformanceDao salesPerformanceDao;
    @Autowired
    OrderRequisitionDao orderRequistionDao;
    @Autowired
    SclUserDao sclUserDao;
    @Autowired
    CollectionDao collectionDao;
    @Autowired
    PointRequisitionService pointRequisitionService;
    @Resource
    DealerDao dealerDao;
    @Autowired
    PointRequisitionDao pointRequisitionDao;
    @Autowired
    private SclSalesSummaryService sclSalesSummaryService;
    @Autowired
    SCLProductService sclProductService;

    @Override
    public SalesAndAchievementData getTotalAndActualTargetForSales(String filter, Integer year, Integer month,List<String> territoryList) {
        SclUserModel sclUser=(SclUserModel) getUserService().getCurrentUser();
        BaseSiteModel currentBaseSite=baseSiteService.getCurrentBaseSite();
        SalesAndAchievementData data=new SalesAndAchievementData();
        double actualTarget = 0.0, totalTarget=0.0, behindTarget=0.0, aheadTarget=0.0;
        double achievementPercentage=0.0;
        if(StringUtils.isBlank(filter))
        {
            if(year!=0 && month!=0)
            {
              //  actualTarget=getSalesPerformanceService().getActualTargetForSalesForMonth(sclUser,currentBaseSite,year,month,doList,subAreaList);
                actualTarget=sclSalesSummaryService.getSalesByMonth(sclUser,month,year, territoryList);
                data.setActualTarget(actualTarget!=0.0?actualTarget:0.0);

                totalTarget=getSalesPerformanceService().getSalesTargetForMonth(sclUser, currentBaseSite, year, month,territoryList);
                data.setTotalTarget(totalTarget!=0.0?totalTarget:0.0);

                if(actualTarget!=0.0 && totalTarget!=0.0)
                    achievementPercentage=(actualTarget/totalTarget)*100;
                data.setAchievementPercentage(achievementPercentage!=0.0 ?achievementPercentage :0.0);

                if(achievementPercentage < 100)
                {
                    behindTarget=totalTarget-actualTarget;
                    data.setBehindTotalTarget(behindTarget!=0.0 ? behindTarget :0.0);
                }
                else if(achievementPercentage > 100)
                {
                    aheadTarget=actualTarget-totalTarget;
                    data.setAheadTotalTarget(aheadTarget!=0.0?aheadTarget:0.0);
                }
            }
            else
            {
                //actualTarget=getSalesPerformanceService().getActualTargetForSalesMTD(subArea,sclUser,currentBaseSite);
                //actualTarget=getSalesPerformanceService().getActualTargetForSalesMTD(sclUser,currentBaseSite,doList,subAreaList);
                actualTarget=sclSalesSummaryService.getCurrentMonthSales(sclUser, territoryList);
                data.setActualTarget(actualTarget!=0.0?actualTarget:0.0);

                totalTarget=getSalesPerformanceService().getMonthlySalesTarget(sclUser,currentBaseSite,territoryList);
                data.setTotalTarget(totalTarget!=0.0?totalTarget:0.0);

                if(actualTarget!=0.0 && totalTarget!=0.0)
                    achievementPercentage=(actualTarget/totalTarget)*100;
                data.setAchievementPercentage(achievementPercentage!=0.0 ?achievementPercentage :0.0);

                if(achievementPercentage < 100)
                {
                    behindTarget=totalTarget-actualTarget;
                    data.setBehindTotalTarget(behindTarget!=0.0 ? behindTarget :0.0);
                }
                else if(achievementPercentage > 100)
                {
                    aheadTarget=actualTarget-totalTarget;
                    data.setAheadTotalTarget(aheadTarget!=0.0?aheadTarget:0.0);
                }
            }

        }
        else
        {
            if(filter.contains(FILTER_MTD))
            {
                //actualTarget=getSalesPerformanceService().getActualTargetForSalesMTD(subArea,sclUser,currentBaseSite);
               // actualTarget=getSalesPerformanceService().getActualTargetForSalesMTD(sclUser,currentBaseSite,doList,subAreaList);
                actualTarget=sclSalesSummaryService.getCurrentMonthSales(sclUser, territoryList);
                data.setActualTarget(actualTarget!=0.0?actualTarget:0.0);

                totalTarget=getSalesPerformanceService().getMonthlySalesTarget(sclUser,currentBaseSite,territoryList);
                data.setTotalTarget(totalTarget!=0.0?totalTarget:0.0);

                if(actualTarget!=0.0 && totalTarget!=0.0)
                    achievementPercentage=(actualTarget/totalTarget)*100;
                data.setAchievementPercentage(achievementPercentage!=0.0 ?achievementPercentage :0.0);

                if(achievementPercentage < 100)
                {
                    behindTarget=totalTarget-actualTarget;
                    data.setBehindTotalTarget(behindTarget!=0.0 ? behindTarget :0.0);
                }
                else if(achievementPercentage > 100)
                {
                    aheadTarget=actualTarget-totalTarget;
                    data.setAheadTotalTarget(aheadTarget!=0.0?aheadTarget:0.0);
                }
            }
            else if(filter.contains(FILTER_YTD))
            {
                //actualTarget=getSalesPerformanceService().getActualTargetForSalesYTD(subArea,sclUser,currentBaseSite);
                //actualTarget=getSalesPerformanceService().getActualTargetForSalesYTD(sclUser,currentBaseSite,doList,subAreaList);
                actualTarget=sclSalesSummaryService.getCurrentFySales(sclUser, territoryList);
                data.setActualTarget(actualTarget!=0.0?actualTarget:0.0);

                totalTarget=getSalesPerformanceService().getAnnualSalesTarget(sclUser,territoryList);
                data.setTotalTarget(totalTarget!=0.0?totalTarget:0.0);

                if(actualTarget!=0.0 && totalTarget!=0.0)
                    achievementPercentage=(actualTarget/totalTarget)*100;
                data.setAchievementPercentage(achievementPercentage!=0.0 ?achievementPercentage :0.0);

                if(achievementPercentage < 100)
                {
                    behindTarget=totalTarget-actualTarget;
                    data.setBehindTotalTarget(behindTarget!=0.0 ? behindTarget :0.0);
                }
                else if(achievementPercentage > 100)
                {
                    aheadTarget=actualTarget-totalTarget;
                    data.setAheadTotalTarget(aheadTarget!=0.0?aheadTarget:0.0);
                }
            }
        }
        return data;
    }

    @Override
    public SalesAndAchievementData getProratedActualAndActualTargetForSales(String filter, Integer yearFilter, Integer monthFilter,List<String> territoryList) {
        SclUserModel sclUser=(SclUserModel) getUserService().getCurrentUser();
        BaseSiteModel currentBaseSite=baseSiteService.getCurrentBaseSite();
        SalesAndAchievementData data=new SalesAndAchievementData();

        double actualTarget=0.0, totalTarget=0.0, proratedTarget=0.0 ,achievementPercentage =0.0, behindProratedTarget=0.0, aheadProratedTarget=0.0;

        /*Calendar cal = Calendar.getInstance();
        int noOfDaysInTheMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int noOfDaysGoneByInTheMonth = cal.get(Calendar.DAY_OF_MONTH) - 1;

        int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        int year = cal.get(Calendar.YEAR);
        Calendar calTwo = new GregorianCalendar(year, 11, 31);
        int totalNoOfDaysInYear = calTwo.get(Calendar.DAY_OF_YEAR);
        int remainingDaysInTheYear = totalNoOfDaysInYear - dayOfYear;
        int noOfDaysGoneBy=totalNoOfDaysInYear-remainingDaysInTheYear;*/


        LocalDate cal=LocalDate.now();
        int noOfDaysInTheMonth = cal.lengthOfMonth();
        int noOfDaysGoneByInTheMonth = cal.getDayOfMonth() - 1;
        int dayOfYear = cal.getDayOfYear();
        int year = cal.getYear();
        LocalDate calTwo=LocalDate.of(year,12,31);
        int totalNoOfDaysInYear = calTwo.getDayOfYear();
        int remainingDaysInTheYear = totalNoOfDaysInYear - dayOfYear;
        int noOfDaysGoneBy=totalNoOfDaysInYear-remainingDaysInTheYear;

        if(StringUtils.isBlank(filter))
        {
            if(yearFilter!=0 && monthFilter!=0) {
                if(yearFilter==LocalDate.now().getYear() && monthFilter==LocalDate.now().getMonthValue())
                {

        		/* cal = Calendar.getInstance();
        		 cal.set(Calendar.MONTH, monthFilter-1);
        		 cal.set(Calendar.YEAR, yearFilter);
        	     noOfDaysInTheMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        	     if((Calendar.getInstance().get(Calendar.MONTH)==(monthFilter-1)) && (Calendar.getInstance().get(Calendar.MONTH)==yearFilter))
        	     {
        	    	 noOfDaysGoneByInTheMonth = cal.get(Calendar.DAY_OF_MONTH) - 1;
        	     }
        	     else
        	     {
        	    	 noOfDaysGoneByInTheMonth = noOfDaysInTheMonth;
        	     }*/
                cal = LocalDate.of(yearFilter, monthFilter, cal.getDayOfMonth());
                noOfDaysGoneByInTheMonth = cal.getDayOfMonth() - 1;

                //actualTarget=getSalesPerformanceService().getActualTargetForSalesForMonth(subArea, sclUser, currentBaseSite, yearFilter, monthFilter);
                // actualTarget=getSalesPerformanceService().getActualTargetForSalesForMonth(sclUser, currentBaseSite, yearFilter, monthFilter,doList,subAreaList);
                actualTarget = sclSalesSummaryService.getSalesByMonth(sclUser, monthFilter, yearFilter, territoryList);
                data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);

                //add territory list in Target

                totalTarget = getSalesPerformanceService().getSalesTargetForMonth(sclUser, currentBaseSite, yearFilter, monthFilter, territoryList);


                if (totalTarget != 0.0)
                    proratedTarget = (totalTarget / noOfDaysInTheMonth) * noOfDaysGoneByInTheMonth;
                data.setProratedTarget(proratedTarget != 0.0 ? proratedTarget : 0.0);

                if (actualTarget != 0.0 && proratedTarget != 0.0)
                    achievementPercentage = (actualTarget / proratedTarget) * 100;
                //achievementPercentage=(actualTarget/totalTarget)*100;
                data.setAchievementPercentage(achievementPercentage != 0.0 ? achievementPercentage : 0.0);

                if (achievementPercentage < 100) {
                    //if(actualTarget!=0.0 && proratedTarget!=0.0)
                    behindProratedTarget = proratedTarget - actualTarget;
                    data.setBehindProratedTarget(behindProratedTarget != 0.0 ? behindProratedTarget : 0.0);
                } else if (achievementPercentage > 100) {
                    //if(actualTarget!=0.0 && proratedTarget!=0.0)
                    aheadProratedTarget = actualTarget - proratedTarget;
                    data.setAheadProratedTarget(aheadProratedTarget != 0.0 ? aheadProratedTarget : 0.0);
                }
            }else{
                    YearMonth yearMonth = YearMonth.of(yearFilter, monthFilter);
                    int daysInMonth = yearMonth.lengthOfMonth();
                    LocalDate cal1=LocalDate.of(yearFilter,monthFilter,daysInMonth);
                    int noOfDaysInTheMonth1 = cal1.lengthOfMonth();


                    actualTarget = sclSalesSummaryService.getSalesByMonth(sclUser, monthFilter, yearFilter, territoryList);
                    data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);
                    totalTarget = getSalesPerformanceService().getSalesTargetForMonth(sclUser, currentBaseSite, yearFilter, monthFilter, territoryList);

                    if (totalTarget != 0.0) {
                        proratedTarget = totalTarget;
                    }
                    data.setProratedTarget(proratedTarget != 0.0 ? proratedTarget : 0.0);

                    if (actualTarget != 0.0 && proratedTarget != 0.0)
                        achievementPercentage = (actualTarget / proratedTarget) * 100;
                    data.setAchievementPercentage(achievementPercentage != 0.0 ? achievementPercentage : 0.0);

                    if (achievementPercentage < 100) {
                        behindProratedTarget = proratedTarget - actualTarget;
                        data.setBehindProratedTarget(behindProratedTarget != 0.0 ? behindProratedTarget : 0.0);
                    } else if (achievementPercentage > 100) {
                        aheadProratedTarget = actualTarget - proratedTarget;
                        data.setAheadProratedTarget(aheadProratedTarget != 0.0 ? aheadProratedTarget : 0.0);
                    }
                }
            }
            else
            {
                //actualTarget=getSalesPerformanceService().getActualTargetForSalesMTD(subArea,sclUser,currentBaseSite);
                //actualTarget=getSalesPerformanceService().getActualTargetForSalesMTD(sclUser,currentBaseSite,doList,subAreaList);
                actualTarget=sclSalesSummaryService.getCurrentMonthSales(sclUser, territoryList);
                data.setActualTarget(actualTarget!=0.0?actualTarget:0.0);

                totalTarget=getSalesPerformanceService().getMonthlySalesTarget(sclUser,currentBaseSite,territoryList);
                if(totalTarget!=0.0)
                    proratedTarget=(totalTarget/noOfDaysInTheMonth)*noOfDaysGoneByInTheMonth;
                data.setProratedTarget(proratedTarget!=0.0?proratedTarget:0.0);

                if(actualTarget!=0.0 && proratedTarget!=0.0)
                    achievementPercentage=(actualTarget/proratedTarget)*100;
                data.setAchievementPercentage(achievementPercentage!=0.0 ?achievementPercentage :0.0);

                if(achievementPercentage < 100)
                {
                    //if(actualTarget!=0.0 && proratedTarget!=0.0)
                        behindProratedTarget=proratedTarget-actualTarget;
                    data.setBehindProratedTarget(behindProratedTarget!=0.0 ? behindProratedTarget :0.0);
                }
                else if(achievementPercentage > 100)
                {
                    //if(actualTarget!=0.0 && proratedTarget!=0.0)
                        aheadProratedTarget=actualTarget-proratedTarget;
                    data.setAheadProratedTarget(aheadProratedTarget!=0.0?aheadProratedTarget:0.0);
                }
            }
       }
        else
        {
            if(filter.contains(FILTER_MTD))
            {
                //actualTarget=getSalesPerformanceService().getActualTargetForSalesMTD(subArea,sclUser,currentBaseSite);
                //actualTarget=getSalesPerformanceService().getActualTargetForSalesMTD(sclUser,currentBaseSite,doList,subAreaList);
                actualTarget=sclSalesSummaryService.getCurrentMonthSales(sclUser, territoryList);
                data.setActualTarget(actualTarget!=0.0?actualTarget:0.0);

                totalTarget=getSalesPerformanceService().getMonthlySalesTarget(sclUser,currentBaseSite,territoryList);
                if(totalTarget!=0.0)
                    proratedTarget=(totalTarget/noOfDaysInTheMonth)*noOfDaysGoneByInTheMonth;
                data.setProratedTarget(proratedTarget!=0.0?proratedTarget:0.0);

                if(actualTarget!=0.0 && proratedTarget!=0.0)
                    achievementPercentage=(actualTarget/proratedTarget)*100;
                data.setAchievementPercentage(achievementPercentage!=0.0 ?achievementPercentage :0.0);

                if(achievementPercentage < 100)
                {
                   // if(actualTarget!=0.0 && proratedTarget!=0.0)
                        behindProratedTarget=proratedTarget-actualTarget;
                    data.setBehindProratedTarget(behindProratedTarget!=0.0 ? behindProratedTarget :0.0);
                }
                else if(achievementPercentage > 100)
                {
                    //if(actualTarget!=0.0 && proratedTarget!=0.0)
                        aheadProratedTarget=actualTarget-proratedTarget;
                    data.setAheadProratedTarget(aheadProratedTarget!=0.0?aheadProratedTarget:0.0);
                }
            }
            else if(filter.contains(FILTER_YTD))
            {

                LocalDate current=LocalDate.now();
                LocalDate startDate=null;
                if(current.getMonth().getValue()>=4){
                    startDate= LocalDate.of(LocalDate.now().getYear(), 4,1);//2024-04-01
                }else{
                    startDate= LocalDate.of(LocalDate.now().getYear()-1, 4,1);
                }
                dayOfYear = (int) daysBetweenInclusive(startDate,current);
                year = startDate.getYear()+1;//2024
                LocalDate endDate= LocalDate.of(year,3,31);
                totalNoOfDaysInYear = endDate.lengthOfYear();
               long remainingDaysInTheYear1 = totalNoOfDaysInYear - dayOfYear;
                long noOfDaysGoneBy1=totalNoOfDaysInYear-remainingDaysInTheYear1;

                /*
                List<Date> currentFinancialYear = salesPerformanceService.getCurrentFinancialYear();
                Date startDate=currentFinancialYear.get(0);
                LocalDate calYtd=startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                LocalDate curDate=LocalDate.now();
                long dayOfYear1 =  (Math.abs(ChronoUnit.DAYS.between(curDate, calYtd)));
                year = calYtd.getYear();//2023
                LocalDate calTwoYtd= LocalDate.of(year,12,31);
                totalNoOfDaysInYear = calTwoYtd.getDayOfYear();//365
                long remainingDaysInTheYear1 = totalNoOfDaysInYear - dayOfYear1;//283
                long noOfDaysGoneBy1=Math.abs(totalNoOfDaysInYear-remainingDaysInTheYear1);//82
                //actualTarget=getSalesPerformanceService().getActualTargetForSalesYTD(subArea,sclUser,currentBaseSite);
                //actualTarget=getSalesPerformanceService().getActualTargetForSalesYTD(sclUser,currentBaseSite,doList,subAreaList);*/

                actualTarget=sclSalesSummaryService.getCurrentFySales(sclUser, territoryList);
                data.setActualTarget(actualTarget!=0.0?actualTarget:0.0);

                totalTarget=getSalesPerformanceService().getAnnualSalesTarget(sclUser,territoryList);
                if(totalTarget!=0.0)
                    proratedTarget=(totalTarget/totalNoOfDaysInYear)*noOfDaysGoneBy1;
                data.setProratedTarget(proratedTarget!=0.0?proratedTarget:0.0);

                if(actualTarget!=0.0 && proratedTarget!=0.0)
                    achievementPercentage=(actualTarget/proratedTarget)*100;
                data.setAchievementPercentage(achievementPercentage!=0.0 ?achievementPercentage :0.0);

                if(achievementPercentage < 100)
                {
                    //if(actualTarget!=0.0 && proratedTarget!=0.0)
                        behindProratedTarget=proratedTarget-actualTarget;
                    data.setBehindProratedTarget(behindProratedTarget!=0.0 ? behindProratedTarget :0.0);
                }
                else if(achievementPercentage > 100)
                {
                   // if(actualTarget!=0.0 && proratedTarget!=0.0)
                    aheadProratedTarget=actualTarget-proratedTarget;
                    data.setAheadProratedTarget(aheadProratedTarget!=0.0?aheadProratedTarget:0.0);
                }
            }
        }
        return data;
    }

    @Override
    public CurrentAskingPredicatedData getPredicatedAchievementCurrentAndAskingRate(String filter,List<String> territoryList,int month,int year) {
        SclUserModel sclUser = (SclUserModel) getUserService().getCurrentUser();
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        CurrentAskingPredicatedData data = new CurrentAskingPredicatedData();

        //Double salesInCurrentMonth = getSalesPerformanceService().getActualTargetForSalesMTD(subArea, sclUser, currentBaseSite);//MTD-Order
        //Double salesInCurrentMonth = getSalesPerformanceService().getActualTargetForSalesMTD(sclUser, currentBaseSite,doList,subAreaList);//MTD-Order
        Double salesInCurrentMonth = sclSalesSummaryService.getCurrentMonthSales(sclUser, territoryList);//MTD-Order

        Double salesInselectedMonth=0.0,monthlySalesTargetMonth=0.0;
        if(month!=0 && year !=0 ) {
            salesInselectedMonth = sclSalesSummaryService.getSalesByMonth((B2BCustomerModel) sclUser, month, year, territoryList);//MTD-Order
            monthlySalesTargetMonth=getSalesPerformanceService().getMonthlySalesTarget(sclUser,currentBaseSite,month,year,territoryList);
        }
        //Double salesTillDate = getSalesPerformanceService().getActualTargetForSalesYTD(subArea, sclUser, currentBaseSite);//YTD-Order
        //Double salesTillDate = getSalesPerformanceService().getActualTargetForSalesYTD(sclUser, currentBaseSite,doList,subAreaList);//YTD-Order
        Double salesTillDate = sclSalesSummaryService.getCurrentFySales(sclUser, territoryList);//YTD-Order



        Double monthlySalesTarget=getSalesPerformanceService().getMonthlySalesTarget(sclUser,currentBaseSite,territoryList);//MTD-salesPlanning
        Double annualSalesTarget=getSalesPerformanceService().getAnnualSalesTarget(sclUser,territoryList);//YTD-salesPlanning

       /* Calendar cal = Calendar.getInstance();
        int noOfDaysInTheMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int noOfDaysGoneByInTheMonth = cal.get(Calendar.DAY_OF_MONTH) - 1;
        int remainingDaysInTheMonth = noOfDaysInTheMonth - noOfDaysGoneByInTheMonth;

        int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        int year = cal.get(Calendar.YEAR);
        Calendar calTwo = new GregorianCalendar(year, 11, 31);
        int totalNoOfDaysInYear = calTwo.get(Calendar.DAY_OF_YEAR);
        int remainingDaysInTheYear = totalNoOfDaysInYear - dayOfYear;
        int noOfDaysTillDate=totalNoOfDaysInYear-remainingDaysInTheYear;*/

        LocalDate cal=LocalDate.now();

        int noOfDaysInTheMonth = cal.lengthOfMonth();
        int noOfDaysGoneByInTheMonth = cal.getDayOfMonth()-1;
        int remainingDaysInTheMonth = noOfDaysInTheMonth - noOfDaysGoneByInTheMonth;

        int dayOfYear = cal.getDayOfYear();
        int year1 = cal.getYear();
        LocalDate calTwo= LocalDate.of(year1,12,31);
        int totalNoOfDaysInYear = calTwo.getDayOfYear();
        int remainingDaysInTheYear = totalNoOfDaysInYear - dayOfYear;
        int noOfDaysTillDate=totalNoOfDaysInYear-remainingDaysInTheYear;

        if(StringUtils.isBlank(filter)) {

            if (month != 0 && year != 0) {

                int currentMonth=LocalDate.now().getMonthValue();
                int currentYear = LocalDate.now().getYear();
                if(month==currentMonth && year==currentYear){

                        if (salesInCurrentMonth != 0.0) {
                            double perDaySale = 0.0;
                            perDaySale = salesInCurrentMonth / noOfDaysGoneByInTheMonth;
                            if (perDaySale != 0.0) {
                                data.setPredicatedAchievement(perDaySale * noOfDaysInTheMonth);
                                data.setCurrentRate(perDaySale);
                            } else {
                                data.setPredicatedAchievement(0.0);
                                data.setCurrentRate(0.0);
                            }

                   /* double askingRate = 0.0;
                    if(monthlySalesTarget!=0.0)
                        askingRate = (monthlySalesTarget - salesInCurrentMonth) / remainingDaysInTheMonth;
                    if (askingRate != 0.0)
                        data.setAskingRate(askingRate);
                    else
                        data.setAskingRate(0.0);*/

                            double askingRate=0.0;

                            if(monthlySalesTarget>0.0)
                                askingRate = (monthlySalesTarget - salesInCurrentMonth) / remainingDaysInTheMonth;//old code
                            else {
                                if(noOfDaysGoneByInTheMonth!=0)
                                    askingRate = salesInCurrentMonth / noOfDaysGoneByInTheMonth;
                                else
                                    askingRate=0.0;
                            }
                            if(askingRate>=0.0)
                            data.setAskingRate(askingRate);
                            else data.setAskingRate(data.getCurrentRate());
                        } else {
                            data.setPredicatedAchievement(0.0);
                            data.setCurrentRate(0.0);
                            data.setAskingRate(0.0);
                        }
                }else{
                    YearMonth yearMonth = YearMonth.of(year, month);
                    int daysInMonth = yearMonth.lengthOfMonth();
                 //   LocalDate date=LocalDate.now();
                    LocalDate cal1=LocalDate.of(year,month,daysInMonth);
                    int noOfDaysInTheMonth1 = cal1.lengthOfMonth();
                    data.setPredicatedAchievement(salesInselectedMonth);
                    data.setCurrentRate(salesInselectedMonth/noOfDaysInTheMonth1);
                    data.setAskingRate(0.0);
                }
            } else {
                if (salesInCurrentMonth != 0.0) {
                    double perDaySale = 0.0;
                    perDaySale = salesInCurrentMonth / noOfDaysGoneByInTheMonth;
                    if (perDaySale != 0.0) {
                        data.setPredicatedAchievement(perDaySale * noOfDaysInTheMonth);
                        data.setCurrentRate(perDaySale);
                    } else {
                        data.setPredicatedAchievement(0.0);
                        data.setCurrentRate(0.0);
                    }

            /*    double askingRate=0.0;
                if(monthlySalesTarget!=0.0)
                    askingRate=(monthlySalesTarget-salesInCurrentMonth)/remainingDaysInTheMonth;
                if(askingRate!=0.0)
                    data.setAskingRate(ask.ingRate);
                else
                    data.setAskingRate(0.0);*/
                    double askingRate = 0.0;

                    if (monthlySalesTarget > 0.0)
                        askingRate = (monthlySalesTarget - salesInCurrentMonth) / remainingDaysInTheMonth;//old code
                    else {
                        if (noOfDaysGoneByInTheMonth != 0)
                            askingRate = salesInCurrentMonth / noOfDaysGoneByInTheMonth;
                        else
                            askingRate = 0.0;
                    }
                    if(askingRate>=0.0)
                    data.setAskingRate(askingRate);
                    else data.setAskingRate(data.getCurrentRate());

                } else {
                    data.setPredicatedAchievement(0.0);
                    data.setCurrentRate(0.0);
                    data.setAskingRate(0.0);
                }
            }
        }
        else {
            if (filter.contains(FILTER_MTD)) {

                if (salesInCurrentMonth != 0.0) {
                    double perDaySale = 0.0;
                    perDaySale = salesInCurrentMonth / noOfDaysGoneByInTheMonth;
                    if (perDaySale != 0.0) {
                        data.setPredicatedAchievement(perDaySale * noOfDaysInTheMonth);
                        data.setCurrentRate(perDaySale);
                    } else {
                        data.setPredicatedAchievement(0.0);
                        data.setCurrentRate(0.0);
                    }

                   /* double askingRate = 0.0;
                    if(monthlySalesTarget!=0.0)
                        askingRate = (monthlySalesTarget - salesInCurrentMonth) / remainingDaysInTheMonth;
                    if (askingRate != 0.0)
                        data.setAskingRate(askingRate);
                    else
                        data.setAskingRate(0.0);*/

                    double askingRate=0.0;

                    if(monthlySalesTarget>0.0)
                        askingRate = (monthlySalesTarget - salesInCurrentMonth) / remainingDaysInTheMonth;//old code
                    else {
                        if(noOfDaysGoneByInTheMonth!=0)
                            askingRate = salesInCurrentMonth / noOfDaysGoneByInTheMonth;
                        else
                            askingRate=0.0;
                    }
                    if(askingRate>=0.0)
                    data.setAskingRate(askingRate);
                    else data.setAskingRate(data.getCurrentRate());
                } else {
                    data.setPredicatedAchievement(0.0);
                    data.setCurrentRate(0.0);
                    data.setAskingRate(0.0);
                }
            }
            else if (filter.contains(FILTER_YTD)) {

                LocalDate current=LocalDate.now();
                LocalDate startDate=null;
                if(current.getMonth().getValue()>=4){
                    startDate= LocalDate.of(LocalDate.now().getYear(), 4,1);//2024-04-01
                }else{
                    startDate= LocalDate.of(LocalDate.now().getYear()-1, 4,1);
                }
               // LocalDate startDate= LocalDate.of(LocalDate.now().getYear()-1, 4,1);
                 dayOfYear = (int) daysBetweenInclusive(startDate,current);
                 year1 = startDate.getYear()+1;//2024
                LocalDate endDate= LocalDate.of(year1,3,31);
                 totalNoOfDaysInYear = endDate.lengthOfYear();
                 remainingDaysInTheYear = totalNoOfDaysInYear - dayOfYear;
                 noOfDaysTillDate=totalNoOfDaysInYear-remainingDaysInTheYear;

                if (salesTillDate != 0.0) {
                    double perDaySale = 0.0;
                    perDaySale = salesTillDate / noOfDaysTillDate;
                    if (perDaySale != 0.0) {
                        data.setPredicatedAchievement(perDaySale * totalNoOfDaysInYear);
                        data.setCurrentRate(perDaySale);
                    } else {
                        data.setPredicatedAchievement(0.0);
                        data.setCurrentRate(0.0);
                    }

                   /* double askingRate = 0.0;
                    if(annualSalesTarget!=0.0)
                        askingRate = (annualSalesTarget - salesTillDate) / remainingDaysInTheYear;
                    if (askingRate != 0.0)
                        data.setAskingRate(askingRate);
                    else
                        data.setAskingRate(0.0);*/
                    double askingRate = 0.0;

                    if(annualSalesTarget>0.0)
                        askingRate = (annualSalesTarget - salesTillDate) / remainingDaysInTheYear;//old code
                    else {
                        if(noOfDaysTillDate!=0)
                            askingRate = salesTillDate / noOfDaysTillDate;
                        else
                            askingRate=0.0;
                    }
                    if(askingRate>=0.0)
                    data.setAskingRate(askingRate);
                    else data.setAskingRate(data.getCurrentRate());
                } else {
                    data.setPredicatedAchievement(0.0);
                    data.setCurrentRate(0.0);
                    data.setAskingRate(0.0);
                }
            }
        }
        return data;
    }
    public static long daysBetweenInclusive(LocalDate ld1, LocalDate ld2) {
        return Math.abs(ChronoUnit.DAYS.between(ld1, ld2)) ;
    }

    @Override
    public CurrentAskingPredicatedData getPredicatedAchievementCurrentAndAskingRateDealerRetailer(String filter,String bgpFilter,int month,int year) {
           /* B2BCustomerModel  currentUser = (B2BCustomerModel) getUserService().getCurrentUser();
            SclCustomerModel sclCustomer = (SclCustomerModel) getUserService().getUserForUID(currentUser.getUid());
*/
        SclCustomerModel sclCustomer = (SclCustomerModel) getUserService().getCurrentUser();

        Double salesForSelectedMonth=0.0,salesInCurrentMonth=0.0,salesTillDate=0.0,monthlySalesTarget=0.0,annualSalesTarget=0.0,monthlySalesTargetForMonth=0.0;
        int noOfDaysInTheMonth=0,noOfDaysGoneByInTheMonth=0,remainingDaysInTheMonth=0,dayOfYear=0,year1=0,totalNoOfDaysInYear=0,remainingDaysInTheYear=0,noOfDaysTillDate=0;

        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        CurrentAskingPredicatedData data = new CurrentAskingPredicatedData();

        LocalDate cal=LocalDate.now();
        noOfDaysInTheMonth = cal.lengthOfMonth();
        noOfDaysGoneByInTheMonth = cal.getDayOfMonth()-1;
        remainingDaysInTheMonth = noOfDaysInTheMonth - noOfDaysGoneByInTheMonth;

        dayOfYear = cal.getDayOfYear();
        year1 = cal.getYear();
        LocalDate calTwo= LocalDate.of(year1,12,31);
        totalNoOfDaysInYear = calTwo.getDayOfYear();
        remainingDaysInTheYear = totalNoOfDaysInYear - dayOfYear;
        noOfDaysTillDate=totalNoOfDaysInYear-remainingDaysInTheYear;

       if(sclCustomer.getCounterType().equals(CounterType.SP)){
            RequestCustomerData requestData = new RequestCustomerData();
            List<String> counterType = List.of(DEALER);
            requestData.setCounterType(counterType);
            List<SclCustomerModel> customerforUser = territoryManagementService.getCustomerforUser(requestData);
            if(customerforUser!=null && !customerforUser.isEmpty()) {
                for (SclCustomerModel customerModel : customerforUser) {
                    LOGGER.info(String.format("Scl customer Model PK:%s", customerModel));
                }
            }
            else{
                LOGGER.info("SP List is Empty");
            }

            if(customerforUser!=null && !customerforUser.isEmpty()) {
                salesForSelectedMonth =  sclSalesSummaryService.getSalesByMonth(sclCustomer,month,year,bgpFilter);//need to change
                salesInCurrentMonth =  getSalesPerformanceService().getActualTargetForSalesSPMTD(customerforUser);
                salesTillDate =  getSalesPerformanceService().getActualTargetForSalesYTDSP(customerforUser,currentBaseSite);

                monthlySalesTarget=getSalesPerformanceService().getMonthlySalesTargetForSP(customerforUser);
                annualSalesTarget=getSalesPerformanceService().getAnnualSalesTargetForSP(customerforUser);
            }
        }
        else if(sclCustomer.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
           // salesInCurrentMonth =  getSalesPerformanceService().getActualTargetForSalesDealerMTD(sclCustomer, currentBaseSite,bgpFilter);//MTD-Order//6
            // salesTillDate =  getSalesPerformanceService().getActualTargetForSalesDealerYTD(sclCustomer, currentBaseSite,bgpFilter);//YTD-Order//48
           if(month!=0 && year!=0) {
               salesForSelectedMonth = sclSalesSummaryService.getSalesByMonth(sclCustomer, month, year, bgpFilter);
               monthlySalesTargetForMonth=getSalesPerformanceService().getMonthlySalesTargetForDealer(sclCustomer,currentBaseSite,month,year,bgpFilter);
           }
                salesInCurrentMonth =  sclSalesSummaryService.getCurrentMonthSales(sclCustomer,bgpFilter);
                salesTillDate =  sclSalesSummaryService.getCurrentFySales(sclCustomer,bgpFilter);

            monthlySalesTarget=getSalesPerformanceService().getMonthlySalesTargetForDealer(sclCustomer,currentBaseSite,bgpFilter);//MTD-salesPlanning//100
            annualSalesTarget=getSalesPerformanceService().getAnnualSalesTargetForDealer(sclCustomer,bgpFilter);//YTD-salesPlanning //200
        }
        else if(sclCustomer.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
            Map<String,Double> salesQty=new HashMap<>();
           /* salesForSelectedMonth= getSalesPerformanceService().getActualTargetForSalesRetailerMTD(sclCustomer, currentBaseSite,bgpFilter);//need to change
            salesInCurrentMonth = getSalesPerformanceService().getActualTargetForSalesRetailerMTD(sclCustomer, currentBaseSite,bgpFilter);//MTD-Order
            salesTillDate = getSalesPerformanceService().getActualTargetForSalesRetailerYTD(sclCustomer, currentBaseSite,bgpFilter);//YTD-Order*/
           if(month!=0 && year!=0) {
               salesQty = networkService.getSalesQuantityForRetailerByMonthYear(sclCustomer, month, year, null, bgpFilter);
               salesForSelectedMonth = salesQty.get("quantityInBags");
           }
           salesQty =  networkService.getSalesQuantityForRetailerByMTD(sclCustomer,null,bgpFilter);
           salesInCurrentMonth=salesQty.get("quantityInBags");
           salesQty =  networkService.getSalesQuantityForRetailerByYTD(sclCustomer,null,bgpFilter);
           salesTillDate=salesQty.get("quantityInBags");

           monthlySalesTarget=getSalesPerformanceService().getMonthlySalesTargetForRetailer(sclCustomer.getUid(),currentBaseSite,bgpFilter);//MTD-salesPlanning
           annualSalesTarget=getSalesPerformanceService().getAnnualSalesTargetForRetailer(sclCustomer,bgpFilter);//YTD-salesPlanning
        }
        LOGGER.info(String.format("salesInSelectedMonth:%s", String.valueOf(salesForSelectedMonth)));
        LOGGER.info(String.format("monthlySalesTargetForMonth:%s", String.valueOf(monthlySalesTargetForMonth)));
        LOGGER.info(String.format("salesInCurrentMonth:%s", String.valueOf(salesInCurrentMonth)));
        LOGGER.info(String.format("salesTillDate:%s",  String.valueOf(salesTillDate)));
        LOGGER.info(String.format("monthlySalesTarget:%s",  String.valueOf(monthlySalesTarget)));
        LOGGER.info(String.format("annualSalesTarget:%s",  String.valueOf(annualSalesTarget)));

        if(StringUtils.isBlank(filter))
        {
            if(month!=0 && year!=0){
                int monthValue = LocalDate.now().getMonthValue();
                int yearValue = LocalDate.now().getYear();
                if(month==monthValue && yearValue==year){
                    if(salesInCurrentMonth!=0.0) {
                        double perDaySale = 0.0;
                        perDaySale = salesInCurrentMonth / noOfDaysGoneByInTheMonth;
                        if (perDaySale != 0.0) {
                            data.setPredicatedAchievement(perDaySale * noOfDaysInTheMonth);
                            data.setCurrentRate(perDaySale);
                        }
                        else
                        {
                            data.setPredicatedAchievement(0.0);
                            data.setCurrentRate(0.0);
                        }

                        double askingRate=0.0;

                        if(monthlySalesTarget>0.0)
                            askingRate = (monthlySalesTarget - salesInCurrentMonth) / remainingDaysInTheMonth;//old code
                        else {
                            if(noOfDaysGoneByInTheMonth!=0)
                                askingRate = salesInCurrentMonth / noOfDaysGoneByInTheMonth;
                            else
                                askingRate=0.0;
                        }
                        if(askingRate>=0.0)
                            data.setAskingRate(askingRate);
                         else
                           data.setAskingRate(data.getCurrentRate());

                    }
                    else
                    {
                        data.setPredicatedAchievement(0.0);
                        data.setCurrentRate(0.0);
                        data.setAskingRate(0.0);
                    }
                }else{
                    YearMonth yearMonth = YearMonth.of(year, month);
                    int daysInMonth = yearMonth.lengthOfMonth();
                    //   LocalDate date=LocalDate.now();
                    LocalDate cal1=LocalDate.of(year,month,daysInMonth);

                    noOfDaysInTheMonth = cal1.lengthOfMonth();
                    data.setPredicatedAchievement(salesForSelectedMonth);
                    data.setCurrentRate(salesForSelectedMonth/noOfDaysInTheMonth);
                    data.setAskingRate(0.0);
                }
            }else{
                    if(salesInCurrentMonth!=0.0) {
                        double perDaySale = 0.0;
                        perDaySale = salesInCurrentMonth / noOfDaysGoneByInTheMonth;
                        if (perDaySale != 0.0) {
                            data.setPredicatedAchievement(perDaySale * noOfDaysInTheMonth);
                            data.setCurrentRate(perDaySale);
                        }
                        else
                        {
                            data.setPredicatedAchievement(0.0);
                            data.setCurrentRate(0.0);
                        }

                        double askingRate=0.0;

                        if(monthlySalesTarget>0.0)
                            askingRate = (monthlySalesTarget - salesInCurrentMonth) / remainingDaysInTheMonth;//old code
                        else {
                            if(noOfDaysGoneByInTheMonth!=0)
                                askingRate = salesInCurrentMonth / noOfDaysGoneByInTheMonth;
                            else
                                askingRate=0.0;
                        }
                        if(askingRate>=0.0)
                            data.setAskingRate(askingRate);
                        else
                            data.setAskingRate(data.getCurrentRate());

                    }
                    else
                    {
                        data.setPredicatedAchievement(0.0);
                        data.setCurrentRate(0.0);
                        data.setAskingRate(0.0);
                    }
            }
        }
        else {
            if (filter.contains(FILTER_MTD)) {

                if (salesInCurrentMonth != 0.0) {
                    double perDaySale = 0.0;
                    perDaySale = salesInCurrentMonth / noOfDaysGoneByInTheMonth;
                    if (perDaySale != 0.0) {
                        data.setPredicatedAchievement(perDaySale * noOfDaysInTheMonth);
                        data.setCurrentRate(perDaySale);
                    } else {
                        data.setPredicatedAchievement(0.0);
                        data.setCurrentRate(0.0);
                    }

                    double askingRate = 0.0;

                    if(monthlySalesTarget>0.0)
                        askingRate = (monthlySalesTarget - salesInCurrentMonth) / remainingDaysInTheMonth;//old code
                    else {
                        if(noOfDaysGoneByInTheMonth!=0)
                            askingRate = salesInCurrentMonth / noOfDaysGoneByInTheMonth;
                        else
                            askingRate=0.0;
                    }
                    if(askingRate>=0.0)
                        data.setAskingRate(askingRate);
                    else
                        data.setAskingRate(data.getCurrentRate());
                } else {
                    data.setPredicatedAchievement(0.0);
                    data.setCurrentRate(0.0);
                    data.setAskingRate(0.0);
                }
            }
            else if (filter.contains(FILTER_YTD)) {

              /*  List<Date> currentFinancialYear = salesPerformanceService.getCurrentFinancialYear();
                Date startDate=currentFinancialYear.get(0);
                LocalDate calYtd=startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                LocalDate curDate=LocalDate.now();
                long dayOfYear1 =  (Math.abs(ChronoUnit.DAYS.between(curDate, calYtd)));
                // dayOfYear = calYtd.getDayOfYear();
                year = calYtd.getYear();//2023
                LocalDate calTwoYtd= LocalDate.of(year,12,31);
                totalNoOfDaysInYear = calTwoYtd.getDayOfYear();//365
                long remainingDaysInTheYear1 = totalNoOfDaysInYear - dayOfYear1;//283
                long noOfDaysTillDate1=Math.abs(totalNoOfDaysInYear-remainingDaysInTheYear1);//82*/

                LocalDate current=LocalDate.now();//2024-03-29
                LocalDate startDate=null;
                if(current.getMonth().getValue()>=4){
                    startDate= LocalDate.of(LocalDate.now().getYear(), 4,1);//2024-04-01
                }else{
                    startDate= LocalDate.of(LocalDate.now().getYear()-1, 4,1);
                }
                //LocalDate startDate= LocalDate.of(LocalDate.now().getYear()-1, 4,1);
                dayOfYear = (int) daysBetweenInclusive(startDate,current);
                year1 = startDate.getYear()+1;
                LocalDate endDate= LocalDate.of(year1,3,31);
                totalNoOfDaysInYear = endDate.lengthOfYear();
                remainingDaysInTheYear = totalNoOfDaysInYear - dayOfYear;
                noOfDaysTillDate=totalNoOfDaysInYear-remainingDaysInTheYear;

                LOGGER.info("year:"+year);
                LOGGER.info("dayOfYear:"+dayOfYear);
                LOGGER.info("totalNoOfDaysInYear:"+totalNoOfDaysInYear);
                LOGGER.info("remainingDaysInTheYear:"+remainingDaysInTheYear);
                LOGGER.info("noOfDaysTillDate:"+noOfDaysTillDate);

                if (salesTillDate != 0.0) {
                    double perDaySale = 0.0;
                    perDaySale = salesTillDate / noOfDaysTillDate;  // 6.25 / 363
                    if (perDaySale != 0.0) {
                        data.setPredicatedAchievement(perDaySale * totalNoOfDaysInYear); //6.30
                        data.setCurrentRate(perDaySale); //0.017
                    } else {
                        data.setPredicatedAchievement(0.0);
                        data.setCurrentRate(0.0);
                    }

                    double askingRate = 0.0;

                    if(annualSalesTarget>0.0)
                        askingRate = (annualSalesTarget - salesTillDate) / remainingDaysInTheYear;//old code// 35 - 6.25 /3 = 9.58
                    else {
                        if(noOfDaysTillDate!=0)
                            askingRate = salesTillDate / noOfDaysTillDate;
                        else
                            askingRate=0.0;
                    }
                    if(askingRate>=0.0)
                        data.setAskingRate(askingRate);
                    else
                        data.setAskingRate(data.getCurrentRate());
                } else {
                    data.setPredicatedAchievement(0.0);
                    data.setCurrentRate(0.0);
                    data.setAskingRate(0.0);
                }
            }
        }
        return data;
    }

    @Override
    public Double getSecondaryLeadDistanceCount(String filter, Integer month1, Integer year1,List<String> territoryList) {
        double distance = 0.0;
        SclUserModel sclUser = (SclUserModel) getUserService().getCurrentUser();
        BaseSiteModel baseSite = baseSiteService.getCurrentBaseSite();

        if(StringUtils.isBlank(filter)) {

            if(month1 !=0 && year1!=0)
            {
                distance=getSalesPerformanceService().getSecondaryLeadDistanceForMonth(sclUser,baseSite,year1,month1,territoryList);
                if(distance!=0.0) {
                    LOG.info(String.format("Distance : %s", distance));
                    return distance;
                }
                else
                    distance=0.0;
            }
            else
            {
                distance=getSalesPerformanceService().getSecondaryLeadDistance(sclUser,baseSite,territoryList);
                if(distance!=0.0) {
                    LOG.info(String.format("Distance : %s", distance));
                    return distance;
                }
                else
                    distance=0.0;
            }
        }
        else if(filter.contains(FILTER_MTD))
        {
            distance=getSalesPerformanceService().getSecondaryLeadDistanceMTD(sclUser,baseSite,territoryList);
            if(distance!=0.0) {
                LOG.info(String.format("Distance : %s", distance));
                return distance;
            }
            else
                distance=0.0;

        }
        else if(filter.contains(FILTER_YTD))
        {
            distance=getSalesPerformanceService().getSecondaryLeadDistanceYTD(sclUser,baseSite,territoryList);
            if(distance!=0.0) {
                LOG.info(String.format("Distance : %s", distance));

                return distance;
            }
            else
                distance=0.0;
        }

        return distance;
    }




    @Override
    public SalesLeaderboardListData getTop5LeaderboardEmpList(String state, String filter, String soFilter,List<String> doList,List<String> subAreaList) {
        SclUserModel sclUser=(SclUserModel) getUserService().getCurrentUser();
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        SalesLeaderboardListData list=new SalesLeaderboardListData();
        Date startDate;
        Date endDate;
        Date currentDate = Calendar.getInstance().getTime();
        int monthInNumber = currentDate.getMonth();
        List<SalesLeaderboardData> leaderboardDataList=new ArrayList<>();
        list.setEmpCode(sclUser.getUid());
        list.setEmpName(sclUser.getName());
        List<SclUserModel> allSalesOfficersByState = territoryManagementService.getAllSalesOfficersByState(StringUtils.isNotBlank(state) ? state : "");
        Map<String, Double> unsortedLeaderboard = new HashMap<>();
        if(StringUtils.isBlank(filter)) {
            startDate = getFirstDateOfMonth(monthInNumber);
            endDate = getLastDateOfMonth(monthInNumber);
            // list.setEmpSale(salesPerformanceService.getSalesByDeliveryDate(subArea, sclUser, currentBaseSite, startDate, endDate));
            list.setEmpSale(salesPerformanceService.getSalesByDeliveryDate(sclUser, currentBaseSite, startDate, endDate,doList,subAreaList));
            populateUnsortedLeaderboard(currentBaseSite, startDate, endDate, allSalesOfficersByState, unsortedLeaderboard,doList,subAreaList);
        } else if (filter.equals("daily")) {
            startDate=new Date();
            endDate=new Date();
            //list.setEmpSale(salesPerformanceService.getSalesByDeliveryDate(subArea, sclUser, currentBaseSite, startDate, endDate));
            list.setEmpSale(salesPerformanceService.getSalesByDeliveryDate(sclUser, currentBaseSite, startDate, endDate,doList,subAreaList));
            populateUnsortedLeaderboard(currentBaseSite, startDate, endDate, allSalesOfficersByState, unsortedLeaderboard,doList,subAreaList);
        } else if (filter.equals("monthly")) {
            startDate = getFirstDateOfMonth(monthInNumber);
            endDate = getLastDateOfMonth(monthInNumber);
            //list.setEmpSale(salesPerformanceService.getSalesByDeliveryDate(subArea, sclUser, currentBaseSite, startDate, endDate));
            list.setEmpSale(salesPerformanceService.getSalesByDeliveryDate(sclUser, currentBaseSite, startDate, endDate,doList,subAreaList));
            populateUnsortedLeaderboard(currentBaseSite, startDate, endDate, allSalesOfficersByState, unsortedLeaderboard,doList,subAreaList);
        } else if (filter.equals("yearly")) {
            List<Date> dates = salesPerformanceService.getCurrentFinancialYear();
            startDate=dates.get(0);
            endDate=dates.get(1);
            //list.setEmpSale(salesPerformanceService.getSalesByDeliveryDate(subArea, sclUser, currentBaseSite, startDate, endDate));
            list.setEmpSale(salesPerformanceService.getSalesByDeliveryDate(sclUser, currentBaseSite, startDate, endDate,doList,subAreaList));
            populateUnsortedLeaderboard(currentBaseSite, startDate, endDate, allSalesOfficersByState, unsortedLeaderboard,doList,subAreaList);
        } else {
            list.setEmpSale(0.0);
        }



        Map<String, Double> sortedLeaderboard = new HashMap<>();
        AtomicInteger flag = new AtomicInteger();

        unsortedLeaderboard.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> sortedLeaderboard.put(x.getKey(), x.getValue()));

        sortedLeaderboard.forEach((salesOfficer, sales) -> {
            SalesLeaderboardData data = new SalesLeaderboardData();
            SclUserModel sclUserModel = (SclUserModel) userService.getUserForUID(salesOfficer);
            flag.addAndGet(1);
            data.setRank(flag.get());
            data.setCode(sclUserModel.getUid());
            data.setName(sclUserModel.getName());
            data.setTotalNetwork(1);
            data.setSale(sales);
            data.setContactNo(sclUserModel.getMobileNumber());
            leaderboardDataList.add(data);
            if (StringUtils.isNotBlank(soFilter)) {
                SclUserModel sclUserFromSoFilter = (SclUserModel) userService.getUserForUID(soFilter);
                if(sclUserFromSoFilter.getName().equalsIgnoreCase(sclUserModel.getName())) {
                    list.setSoFilterData(data);
                }
            }
            else {
                data = new SalesLeaderboardData();
                list.setSoFilterData(data);
            }
        });
        list.setMostSalesEmployeeList(leaderboardDataList);
        return list;
    }

    private void populateUnsortedLeaderboard(BaseSiteModel currentBaseSite, Date startDate, Date endDate, List<SclUserModel> allSalesOfficersByState, Map<String, Double> unsortedLeaderboard,List<String> doList,List<String> subAreaList) {
        if(!allSalesOfficersByState.isEmpty() && allSalesOfficersByState != null) {
            allSalesOfficersByState.stream().forEach(so -> {
                if(so!=null){
                    //  unsortedLeaderboard.put(so.getUid(), salesPerformanceService.getSalesByDeliveryDate(subArea, so, currentBaseSite, startDate, endDate));
                    unsortedLeaderboard.put(so.getUid(), salesPerformanceService.getSalesByDeliveryDate(so, currentBaseSite, startDate, endDate,doList,subAreaList));
                }
            });
        }
    }

    private void populateUnsortedSalesLeaderboard(BaseSiteModel currentBaseSite, Date startDate, Date endDate, List<SclCustomerModel> customerList, Map<String, Double> unsortedLeaderboard) {
        if(!customerList.isEmpty() && customerList != null) {
            customerList.stream().forEach(so -> {
                if(so!=null){
                    Double salesLeaderByDeliveryDate = salesPerformanceService.getSalesLeaderByDeliveryDate(so, currentBaseSite, startDate, endDate);
                    LOGGER.info(String.format("getting UnsortedSalesLeaderboard for customerUID :: %s sale :: %s", so.getUid(),String.valueOf(salesLeaderByDeliveryDate)));


                    //  unsortedLeaderboard.put(so.getUid(), salesPerformanceService.getSalesByDeliveryDate(subArea, so, currentBaseSite, startDate, endDate));
                    unsortedLeaderboard.put(so.getUid(), salesPerformanceService.getSalesLeaderByDeliveryDate(so, currentBaseSite, startDate, endDate));
                }
            });
        }
    }

    private void populatePremiumUnsortedSalesLeaderboard(BaseSiteModel currentBaseSite, Date startDate, Date endDate, List<SclCustomerModel> customerList, Map<String, Double> unsortedPremiumSalesLeaderboard) {
        if(!customerList.isEmpty() && customerList != null) {
            customerList.stream().forEach(so -> {
                if(so!=null){
                    Double salesLeaderByDeliveryDate = salesPerformanceService.getPremiumSalesLeaderByDeliveryDate(so, currentBaseSite, startDate, endDate);
                    LOGGER.info(String.format("getting PremiumUnsortedSalesLeaderboard for customerUID :: %s sale :: %s", so.getUid(),String.valueOf(salesLeaderByDeliveryDate)));


                    //  unsortedLeaderboard.put(so.getUid(), salesPerformanceService.getSalesByDeliveryDate(subArea, so, currentBaseSite, startDate, endDate));
                    unsortedPremiumSalesLeaderboard.put(so.getUid(), salesPerformanceService.getPremiumSalesLeaderByDeliveryDate(so, currentBaseSite, startDate, endDate));
                }
            });
        }
    }

    public static void computeSalesRanks(List<List<Object>> salesData) {
        // Sort the sales data based on sales values in descending order
        Collections.sort(salesData, (o1, o2) -> {
            double sales1 = (double) o1.get(1);
            double sales2 = (double) o2.get(1);
            return Double.compare(sales2, sales1);
        });

        // Compute and assign ranks to the sales data
        for (int i = 0; i < salesData.size(); i++) {
            List<Object> sale = salesData.get(i);
            sale.add(i + 1); // Add the rank as the last element of the sale list
        }
    }

    public List<List<Object>> assignScores(List<List<Object>> salesData) {
        int rankRange = salesData.size() / 10;
        int currentBucket = 10;
        double totalScore = 0.0;

        for (List<Object> sale : salesData) {
            int rank = (int) sale.get(2);
            double score = (currentBucket - 1) * 100.0; // Compute the score based on bucket

            // Decrease bucket after each rank range
            if (rank % rankRange == 0 && currentBucket > 1) {
                currentBucket--;
            }

            sale.set(3, score);// Add the score as the last element of the sale list

        }

        return salesData;
    }

    public static void calculateFinalScores(List<List<Object>> salesData, double totalScore) {
        for (List<Object> sale : salesData) {
            double score = (double) sale.get(3); // Get the assigned score
            double finalScore = score + (totalScore * 0.5); // Calculate the final score
            sale.add(finalScore); // Add the final score as the last element of the sale list
        }
    }

    public static List<List<Object>> getUIDRankScoreList(List<List<Object>> salesData) {
        List<List<Object>> uidRankScoreList = new ArrayList<>();
        for (List<Object> sale : salesData) {
            String uid = (String) sale.get(0);
            int rank = (int) sale.get(2);
            double finalScore = (double) sale.get(3);
            List<Object> uidRankScore = new ArrayList<>();
            uidRankScore.add(uid);
            uidRankScore.add(rank);
            uidRankScore.add(finalScore);
            uidRankScoreList.add(uidRankScore);
        }
        return uidRankScoreList;
    }




    private void populateUnsortedSalesLeaderboardRetailer(BaseSiteModel currentBaseSite, Date startDate, Date endDate, List<SclCustomerModel> customerList, Map<String, Double> unsortedLeaderboard) {
        if(!customerList.isEmpty() && customerList != null) {
            customerList.stream().forEach(so -> {
                if(so!=null){
                    Double salesLeaderByDeliveryDate = salesPerformanceService.getSalesLeaderByDeliveryDateRetailer(so, currentBaseSite, startDate, endDate);
                    LOGGER.info(String.format("getting Sale Quantity for customerUID :: %s sale :: %s", so.getUid(),salesLeaderByDeliveryDate));

                    //  unsortedLeaderboard.put(so.getUid(), salesPerformanceService.getSalesByDeliveryDate(subArea, so, currentBaseSite, startDate, endDate));
                    unsortedLeaderboard.put(so.getUid(), salesPerformanceService.getSalesLeaderByDeliveryDateRetailer(so, currentBaseSite, startDate, endDate));
                }
            });
        }
    }

    public static Map<String, Double> calculatePercentRank(Map<String, Double> data) {
        List<Double> sortedValues = new ArrayList<>();
        Map<String, Double> percentiles = new HashMap<>();

        int nullCount = 0;
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            Double value = entry.getValue();
            if (value != null) {
                sortedValues.add(value);
            } else {
                nullCount++;
            }
        }

        Collections.sort(sortedValues);

        int size = sortedValues.size();
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            double value = entry.getValue() != null ? entry.getValue() : 0.0;
            if (value == 0.0) {
                percentiles.put(entry.getKey(), 0.0);
            } else if (Objects.isNull(entry.getValue())) {
                percentiles.put(entry.getKey(), 1.0);
            } else {
                int rank = getRank(sortedValues, value, nullCount);
                double percentile = ((rank + 1.0) / (size + 1));
                LOGGER.info(String.format("Customer :: %s Percentile :: %s " , entry.getKey(), percentile));
                percentiles.put(entry.getKey(), percentile);
            }
        }

        return percentiles;



    }

    public static int getRank(List<Double> sortedValues, double value, int nullCount) {
        int rank = Collections.binarySearch(sortedValues, value);
        if (rank >= 0) {
            return rank + 1;
        } else {
            return Math.abs(rank) - nullCount;
        }
    }

    public static int getRanks(List<Integer> sortedValues, int value, int nullCount) {
        int index = Collections.binarySearch(sortedValues, value);
        return index >= 0 ? index + 1 : -(index + 1) - nullCount;
    }


    public static int calculateScoreFromPercentile(int percentile) {
        if (percentile >= 91) {
            return 900;
        } else if (percentile >= 81 && percentile <= 90) {
            return 800;
        } else if (percentile >= 71 && percentile <= 80) {
            return 700;
        } else if (percentile >= 61 && percentile <= 70) {
            return 600;
        } else if (percentile >= 51 && percentile <= 60) {
            return 500;
        } else if (percentile >= 41 && percentile <= 50) {
            return 400;
        } else if (percentile >= 31 && percentile <= 40) {
            return 300;
        } else if (percentile >= 21 && percentile <= 30) {
            return 200;
        } else if (percentile >= 11 && percentile <= 20) {
            return 100;
        } else {
            return 0;
        }
    }

    @Override
    public MarketCounterShareData getDealerCounterShareForMarket(String filter,Integer year, Integer month,List<String> doList,List<String> territoryList) {

        B2BCustomerModel currentUser=(B2BCustomerModel) getUserService().getCurrentUser();
        BaseSiteModel currentBaseSite=baseSiteService.getCurrentBaseSite();
        CounterShareResponseData responseData=null;
        double actualTarget = 0.0, totalTarget=0.0,sumOfNumeratorSales = 0.0,sumOfPotential=0.0;
        Date startDate;
        Date endDate ;
        Date currentDate = Calendar.getInstance().getTime();
        int monthInNumber = currentDate.getMonth();
        LocalDate currentMonth = LocalDate.now();

        MarketCounterShareData data = new MarketCounterShareData();
        List<SclCustomerModel> customerFilteredList=new ArrayList<>();
        double counterPotential=0.0;

        if(currentUser instanceof SclUserModel)
        {
           customerFilteredList = salesPerformanceService.getCustomersByLeadType("DEALER", null, null, null);
            //if subAreaList(territory list) is not empty filter them customerFilteredList
           customerFilteredList=salesPerformanceService.getCustomersByTerritoryCode(customerFilteredList,territoryList);
            if(customerFilteredList!=null && !customerFilteredList.isEmpty())
                for (SclCustomerModel sclCustomerModel : customerFilteredList) {
                    counterPotential += sclCustomerModel.getCounterPotential()!=null?sclCustomerModel.getCounterPotential():0.0;
                }
            LOG.info(String.format("counterPotential :: %s",counterPotential));
        }
        else if (currentUser instanceof SclCustomerModel)
        {
            SclCustomerModel sclCustomer= (SclCustomerModel) getUserService().getCurrentUser();
            if (sclCustomer.getCounterType().equals(CounterType.SP)) {
                RequestCustomerData requestData = new RequestCustomerData();
                List<String> counterTypes = List.of(DEALER);
                requestData.setCounterType(counterTypes);
                customerFilteredList = territoryManagementService.getCustomerforUser(requestData);
                if (customerFilteredList != null && !customerFilteredList.isEmpty()) {
                    for (SclCustomerModel customer : customerFilteredList) {
                        LOGGER.info(String.format("Scl customer Model PK:%s", customer));
                        counterPotential += customer.getCounterPotential() != null ? customer.getCounterPotential() : 0.0;
                    }
                }
            }
        }
        if(StringUtils.isBlank(filter))
        {
            if(year!=0 && month!=0)
            {
                if(currentUser instanceof SclUserModel){
                    SclUserModel sclUser = (SclUserModel) userService.getCurrentUser();
                    //actualTarget= sclSalesSummaryService.getSalesByMonth(sclUser, month, year, territoryList);
                    if (customerFilteredList != null && !customerFilteredList.isEmpty()) {
                        CounterShareData counterShareData=new CounterShareData();
                        counterShareData.setMonth(month);//4
                        counterShareData.setYear(year);//2024
                        for (SclCustomerModel customer : customerFilteredList) {
                            counterShareData.setDealerCode(customer.getUid());//11008926
                                responseData = getCounterShareData(counterShareData);
                                if(Objects.nonNull(responseData) && responseData.getPotential()>0){
                                    sumOfNumeratorSales+=responseData.getNumeratorSales();
                                    sumOfPotential+=responseData.getPotential();
                                }
                                LOGGER.info(" Counter Share"+responseData.getCounterShare() + " Potential:"+responseData.getPotential()+ " Numerator:"+responseData.getNumeratorSales()+ " SelfBrandSale"+responseData.getSelfBrandSale()+ " TotalSales "+responseData.getTotalSales());
                        }
                    }
                    //actualTarget=getSalesPerformanceService().getActualTargetForSalesForMonth(sclUser,currentBaseSite,year,month,doList,subAreaList);
                    totalTarget=salesPerformanceService.getSalesTargetForMonth(sclUser, currentBaseSite, year, month,territoryList);

                }
                else if (currentUser instanceof SclCustomerModel)
                {
                    SclCustomerModel sclCustomer = (SclCustomerModel) userService.getCurrentUser();
                    if(sclCustomer.getCounterType().equals(CounterType.SP)) {
                        CounterShareData counterShareData=new CounterShareData();
                        counterShareData.setMonth(month);
                        counterShareData.setYear(year);
                        if (customerFilteredList != null && !customerFilteredList.isEmpty()) {
                            for (SclCustomerModel customer : customerFilteredList) {

                                counterShareData.setDealerCode(customer.getUid());
                                responseData = getCounterShareData(counterShareData);
                                if(Objects.nonNull(responseData) && responseData.getPotential()>0){
                                    sumOfNumeratorSales+=responseData.getNumeratorSales();
                                    sumOfPotential+=responseData.getPotential();
                                }
                                LOGGER.info(" Counter Share"+responseData.getCounterShare() + " Potential:"+responseData.getPotential()+ " Numerator:"+responseData.getNumeratorSales()+ " SelfBrandSale"+responseData.getSelfBrandSale()+ " TotalSales "+responseData.getTotalSales());
                            }
                            // actualTarget = getSalesPerformanceService().getActualTargetForSalesForMonthSP(customerFilteredList, year, month);
                            totalTarget = salesPerformanceService.getSalesTargetForMonthSP(customerFilteredList, year, month);
                        }
                    }else if(sclCustomer.getGroups().contains(getUserService().getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))){
                         actualTarget = salesPerformanceService.getCounterShareForDealer(sclCustomer, month, year);
                         totalTarget = salesPerformanceService.getMonthlySalesTargetForDealer(sclCustomer,currentBaseSite,month,year,null);
                    }

                }

                /*if(actualTarget!=0.0 && counterPotential!=0.0) {
                    data.setActual((actualTarget / counterPotential) * 100);
                }
                else
                    data.setActual(0.0);*/
                if(sumOfPotential!=0.0 && sumOfNumeratorSales!=0.0) {
                    data.setActual((sumOfNumeratorSales / sumOfPotential) * 100);
                }else{
                    data.setActual(0.0);
                }
                if( totalTarget!=0.0 && counterPotential!=0.0){
                    data.setTarget((totalTarget / counterPotential)*100);
                }
                else
                    data.setTarget(0.0);
                LOGGER.info(String.format("Actual Target:%s", actualTarget));
                LOGGER.info(String.format("Total Target:%s", totalTarget));
            }
            else
            {
                if(currentUser instanceof SclUserModel) {
                    SclUserModel sclUser = (SclUserModel) getUserService().getCurrentUser();
                    //actualTarget = sclSalesSummaryService.getCurrentMonthSales(sclUser,territoryList);
                    // actualTarget = getSalesPerformanceService().getActualTargetForSalesMTD(sclUser, currentBaseSite, doList, subAreaList);
                    if (customerFilteredList != null && !customerFilteredList.isEmpty()) {
                        CounterShareData counterShareData=new CounterShareData();

                        counterShareData.setMonth(LocalDate.now().getMonthValue());
                        counterShareData.setYear(LocalDate.now().getYear());
                        for (SclCustomerModel customer : customerFilteredList) {
                            counterShareData.setDealerCode(customer.getUid());
                            responseData = getCounterShareData(counterShareData);
                            if(Objects.nonNull(responseData) && responseData.getPotential()>0){
                                sumOfNumeratorSales+=responseData.getNumeratorSales();
                                sumOfPotential+=responseData.getPotential();
                            }
                            LOGGER.info(" Counter Share"+responseData.getCounterShare() + " Potential:"+responseData.getPotential()+ " Numerator:"+responseData.getNumeratorSales()+ " SelfBrandSale"+responseData.getSelfBrandSale()+ " TotalSales "+responseData.getTotalSales());
                        }
                    }
                    totalTarget = getSalesPerformanceService().getMonthlySalesTarget(sclUser, currentBaseSite,territoryList);
                }
                else if (currentUser instanceof SclCustomerModel) {
                    SclCustomerModel sclCustomer = (SclCustomerModel) getUserService().getCurrentUser();
                    if (sclCustomer.getCounterType().equals(CounterType.SP)) {
                        if (customerFilteredList != null && !customerFilteredList.isEmpty()) {
                            CounterShareData counterShareData=new CounterShareData();

                            counterShareData.setMonth(LocalDate.now().getMonthValue());
                            counterShareData.setYear(LocalDate.now().getYear());
                            for(SclCustomerModel customer: customerFilteredList){
                                counterShareData.setDealerCode(customer.getUid());
                                responseData = getCounterShareData(counterShareData);
                                if(Objects.nonNull(responseData) && responseData.getPotential()>0){
                                    sumOfNumeratorSales+=responseData.getNumeratorSales();
                                    sumOfPotential+=responseData.getPotential();
                                }
                                LOGGER.info(" Counter Share"+responseData.getCounterShare() + " Potential:"+responseData.getPotential()+ " Numerator:"+responseData.getNumeratorSales()+ " SelfBrandSale"+responseData.getSelfBrandSale()+ " TotalSales "+responseData.getTotalSales());
                            }
                          //  actualTarget = getSalesPerformanceService().getActualTargetForSalesSPMTD(customerFilteredList);
                            totalTarget = getSalesPerformanceService().getMonthlySalesTargetForSP(customerFilteredList);
                        }
                    }else if(sclCustomer.getGroups().contains(getUserService().getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))){
                        actualTarget =salesPerformanceService.getCounterShareForDealer(sclCustomer, LocalDate.now().getMonthValue(), LocalDate.now().getYear());
                        totalTarget = salesPerformanceService.getMonthlySalesTargetForDealer(sclCustomer,currentBaseSite,LocalDate.now().getMonthValue(), LocalDate.now().getYear(),null);
                    }

                }

               /* if(actualTarget!=0.0 && counterPotential!=0.0) {
                    data.setActual(actualTarget);
                }
                else
                    data.setActual(0.0);
*/
                if(sumOfPotential!=0.0 && sumOfNumeratorSales!=0.0) {
                    data.setActual((sumOfNumeratorSales / sumOfPotential) * 100);
                }else
                    data.setActual(0.0);
                if( totalTarget!=0.0 && counterPotential!=0.0){
                    data.setTarget((totalTarget / counterPotential) *100);
                }
                else
                    data.setTarget(0.0);
            }
            LOGGER.info(String.format("Actual Target:%s", actualTarget));
            LOGGER.info(String.format("Total Target:%s", totalTarget));

        }
        else if(filter.equalsIgnoreCase(FILTER_LM))
        {

            LocalDate lastMonth = currentMonth.minusMonths(1);

            if(currentUser instanceof SclUserModel) {
                SclUserModel sclUser = (SclUserModel) getUserService().getCurrentUser();
              //  actualTarget = sclSalesSummaryService.getLastMonthSales(sclUser, territoryList);
                        // actualTarget = getSalesPerformanceService().getActualTargetForSalesLastMonth(sclUser, currentBaseSite, lastMonth.getYear(), lastMonth.getMonthValue(), doList, subAreaList);
                if (customerFilteredList != null && !customerFilteredList.isEmpty()) {
                    CounterShareData counterShareData=new CounterShareData();
                    counterShareData.setMonth(LocalDate.now().minusMonths(1).getMonthValue());
                    counterShareData.setYear(LocalDate.now().minusMonths(1).getYear());
                    for (SclCustomerModel customer : customerFilteredList) {
                        counterShareData.setDealerCode(customer.getUid());
                        responseData = getCounterShareData(counterShareData);
                        if(Objects.nonNull(responseData) && responseData.getPotential()>0){
                            sumOfNumeratorSales+=responseData.getNumeratorSales();
                            sumOfPotential+=responseData.getPotential();
                        }
                        LOGGER.info(" Counter Share"+responseData.getCounterShare() + " Potential:"+responseData.getPotential()+ " Numerator:"+responseData.getNumeratorSales()+ " SelfBrandSale"+responseData.getSelfBrandSale()+ " TotalSales "+responseData.getTotalSales());
                    }
                }
                totalTarget = getSalesPerformanceService().getLastMonthSalesTarget(sclUser, currentBaseSite,territoryList);
                LOGGER.info(String.format("Actual Target:%s", actualTarget));
                LOGGER.info(String.format("Total Target:%s", totalTarget));
            }

          /*  if(actualTarget!=0.0 && counterPotential!=0.0) {
                data.setActual((actualTarget / counterPotential)*100);
            }
            else
                data.setActual(0.0);
*/
            if(sumOfPotential!=0.0 && sumOfNumeratorSales!=0.0) {
                data.setActual((sumOfNumeratorSales / sumOfPotential) * 100);
            }else
                data.setActual(0.0);
            if( totalTarget!=0.0 && counterPotential!=0.0){
                data.setTarget((totalTarget / counterPotential)*100);
            }
            else
                data.setTarget(0.0);

        }

        else if(filter.equalsIgnoreCase(FILTER_LY))
        {
            if(currentUser instanceof SclUserModel) {
                SclUserModel sclUser = (SclUserModel) getUserService().getCurrentUser();
                //actualTarget= sclSalesSummaryService.getLastFYSales(sclUser, territoryList);
                // actualTarget=getSalesPerformanceService().getActualTargetForSalesLastYear(sclUser,currentBaseSite,doList,subAreaList);
                if (customerFilteredList != null && !customerFilteredList.isEmpty()) {
                    CounterShareData counterShareData=new CounterShareData();
                    counterShareData.setMonth(LocalDate.now().getMonthValue());
                    counterShareData.setYear(LocalDate.now().getYear()-1);
                    for (SclCustomerModel customer : customerFilteredList) {
                        counterShareData.setDealerCode(customer.getUid());
                        responseData = getCounterShareData(counterShareData);
                        if(Objects.nonNull(responseData) && responseData.getPotential()>0){
                            sumOfNumeratorSales+=responseData.getNumeratorSales();
                            sumOfPotential+=responseData.getPotential();
                        }
                        LOGGER.info(" Counter Share"+responseData.getCounterShare() + " Potential:"+responseData.getPotential()+ " Numerator:"+responseData.getNumeratorSales()+ " SelfBrandSale"+responseData.getSelfBrandSale()+ " TotalSales "+responseData.getTotalSales());
                    }
                }
                totalTarget=getSalesPerformanceService().getLastYearSalesTarget(sclUser,currentBaseSite,territoryList);

                LOGGER.info(String.format("Actual Target:%s", actualTarget));
                LOGGER.info(String.format("Total Target:%s", totalTarget));
            }

           /* if(actualTarget!=0.0 && counterPotential!=0.0) {
                data.setActual((actualTarget / counterPotential)*100);
                double act=(actualTarget / counterPotential)*100;
                LOGGER.info(String.format("Actual Target:%s", act));
            }
            else
                data.setActual(0.0);*/

            if(sumOfPotential!=0.0 && sumOfNumeratorSales!=0.0) {
                data.setActual((sumOfNumeratorSales / sumOfPotential) * 100);
            }else
                data.setActual(0.0);
            if( totalTarget!=0.0 && counterPotential!=0.0){
                data.setTarget((totalTarget / counterPotential)*100);
                double tot=(totalTarget / counterPotential)*100;
                LOGGER.info(String.format("Actual Target:%s", tot));
            }
            else
                data.setTarget(0.0);
        }
        return data;
    }

    @Override
    public ProductMixVolumeAndRatioListData getProductwiseSalesPercentRatioAndVolumeRatio(String filter, Integer month1, Integer year1,List<String> territoryList) {
        double sumOfQuantity = 0.0;
        SclUserModel sclUser = (SclUserModel) getUserService().getCurrentUser();
        BaseSiteModel baseSite = baseSiteService.getCurrentBaseSite();
        ProductMixVolumeAndRatioListData productMixVolumeAndRatioListData = new ProductMixVolumeAndRatioListData();

        if(StringUtils.isBlank(filter)) {
            if(month1 !=0 && year1!=0)
            {
                productMixVolumeAndRatioListData = getProductMixVolumeAndRatioListDataForMonthPicker(sclUser, baseSite, productMixVolumeAndRatioListData, month1, year1,territoryList);
                if (productMixVolumeAndRatioListData != null) return productMixVolumeAndRatioListData;
            }
            else
            {
                productMixVolumeAndRatioListData = getProductMixVolumeAndRatioListDataForMTD(sclUser, baseSite, productMixVolumeAndRatioListData,null,territoryList);
                if (productMixVolumeAndRatioListData != null) return productMixVolumeAndRatioListData;
            }
        }
        else if(filter.contains(FILTER_MTD))
        {

            productMixVolumeAndRatioListData = getProductMixVolumeAndRatioListDataForMTD(sclUser, baseSite, productMixVolumeAndRatioListData,null,territoryList);
            if (productMixVolumeAndRatioListData != null) return productMixVolumeAndRatioListData;
        }
        else if(filter.contains(FILTER_YTD))
        {
            productMixVolumeAndRatioListData = getProductMixVolumeAndRatioListDataForYTD(sclUser, baseSite, productMixVolumeAndRatioListData,null,territoryList,null,null);
            if (productMixVolumeAndRatioListData != null) return productMixVolumeAndRatioListData;
        }

        return productMixVolumeAndRatioListData;
    }

    private ProductMixVolumeAndRatioListData getProductMixVolumeAndRatioListDataForMonthPicker(SclUserModel sclUser, BaseSiteModel baseSite, ProductMixVolumeAndRatioListData productMixVolumeAndRatioListData, Integer month, Integer year,List<String> territoryList) {
        double sumOfQuantity=0.0;
        //List<List<Object>> productMixRatio = getSalesPerformanceService().getProductwiseSalesPercentRatioAndVolumeRatioForMonthPicker(sclUser, baseSite, month,year,doList,subAreaList);
        List<List<Object>> productMixRatio = sclSalesSummaryService.getProductMixSalesDetailsByMonth(sclUser,month,year,territoryList);
        List<ProductMixVolumeAndRatioData> productMixVolumeAndRatioDataList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(productMixRatio)) {
            sumOfQuantity = productMixRatio.stream().filter(o -> Objects.nonNull(o) && Objects.nonNull(o.get(1))).mapToDouble(o -> Objects.nonNull(o.get(1)) ? (double) o.get(1) : 0.0).sum();
        }
        //sumOfQuantity = productMixRatio.stream().collect(Collectors.summingDouble(o -> (double) o.get(2)));

        if (productMixRatio != null && !productMixRatio.isEmpty()) {
            double finalSumOfQuantity = sumOfQuantity;
            productMixRatio.forEach(objects -> {
                ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                if (objects != null && !objects.isEmpty()) {
                    String productCode = (String) objects.get(0);
                    String productName = (String) objects.get(0);
                    double productSalesPercentRatio=0.0,pquantityInMT=0.0;
                    productMixVolumeAndRatioData.setProductCode(productCode);
                    productMixVolumeAndRatioData.setProductName(productName);
                    //set product sales volume
                    if(objects.get(1)!=null) {
                        pquantityInMT = objects.size() > 1 ? (Double) objects.get(1) : 0.0;
                    }
                    productMixVolumeAndRatioData.setProductVolume(pquantityInMT);
                    //set product sales % ratio
                    try {
                        productSalesPercentRatio = (pquantityInMT / finalSumOfQuantity) * 100;
                    } catch (ArithmeticException e) {
                        productSalesPercentRatio = 0.0;
                    }
                    productMixVolumeAndRatioData.setProductRatio(productSalesPercentRatio);
                    productMixVolumeAndRatioDataList.add(productMixVolumeAndRatioData);

                }
            });
            productMixVolumeAndRatioListData.setProductVolumeAndRatio(productMixVolumeAndRatioDataList);
            return productMixVolumeAndRatioListData;
        } else {

            List<String> productsAliasForSalesPerformance = sclProductService.getProductsAliasForSalesPerformance();
            //List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(sclUser.getState());
            if (productsAliasForSalesPerformance != null && !productsAliasForSalesPerformance.isEmpty()) {
                for (String product : productsAliasForSalesPerformance) {
                    ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                    productMixVolumeAndRatioData.setProductRatio(0.0);
                    productMixVolumeAndRatioData.setProductVolume(0.0);
                    productMixVolumeAndRatioData.setProductName(product);
                    productMixVolumeAndRatioDataList.add(productMixVolumeAndRatioData);
                }
                productMixVolumeAndRatioListData.setProductVolumeAndRatio(productMixVolumeAndRatioDataList);
            }
        }

        return productMixVolumeAndRatioListData;
    }

    private ProductMixVolumeAndRatioListData getProductMixVolumeAndRatioListDataForCustomerMonthPicker(SclCustomerModel sclCustomerModel, BaseSiteModel baseSite, ProductMixVolumeAndRatioListData productMixVolumeAndRatioListData, Integer month, Integer year,List<String> territoryList,List<String> subAreaList,List<String> districtList) {
        double sumOfQuantity = 0.0,sumOfQuantity2=0.0;
        List<List<Object>> productMixRatio=new ArrayList<>();
        if ((sclCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))
         || ((sclCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SP_GROUP_ID)))))
        {
           productMixRatio = getSalesPerformanceService().getProductwiseSalesPercentRatioAndVolumeRatioForCustomerMonthPicker(sclCustomerModel, baseSite, month, year,subAreaList,districtList);
            if(CollectionUtils.isNotEmpty(productMixRatio)) {
                sumOfQuantity = productMixRatio.stream().filter(o -> Objects.nonNull(o) && Objects.nonNull(o.get(2))).mapToDouble(o -> Objects.nonNull(o.get(2)) ? (double) o.get(2) : 0.0).sum();
            }
          //  sumOfQuantity2 = productMixRatio.stream().filter(o->Objects.nonNull(o) && Objects.nonNull(o.get(3))).mapToDouble(o -> Objects.nonNull(o.get(3)) ? (double) o.get(3) : 0.0).sum();

            List<ProductMixVolumeAndRatioData> productMixVolumeAndRatioDataList = new ArrayList<>();

            //sumOfQuantity = productMixRatio.stream().collect(Collectors.summingDouble(o -> (double) o.get(2)));

            if (productMixRatio != null && !productMixRatio.isEmpty()) {
                double finalSumOfQuantity = sumOfQuantity;
                productMixRatio.forEach(objects -> {
                    ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                    if (objects != null && !objects.isEmpty()) {
                        String productCode=null;
                        if(Objects.nonNull(objects.get(3)))
                            productCode = (String) objects.get(3);
                        else
                            productCode = (String) objects.get(0);
                        String productName = (String) objects.get(1);
                        double productSalesPercentRatio=0.0,pquantityInMT=0.0,pquantityInBag1=0.0,pquantityInBag2=0.0;
                        productMixVolumeAndRatioData.setProductCode(productCode);
                        productMixVolumeAndRatioData.setProductName(productName);
                        //set product sales volume
                        if(objects.get(1)!=null) {
                            pquantityInBag1 = objects.size() > 2 ? (Double) objects.get(2) : 0.0;
                        }

                       /* if(objects.get(1)!=null) {
                            pquantityInBag2 = objects.size() > 3 ? (Double) objects.get(3) : 0.0;
                        }*/
                        pquantityInMT=pquantityInBag1;
                        productMixVolumeAndRatioData.setProductVolume(pquantityInMT);
                        //set product sales % ratio
                        try {
                            productSalesPercentRatio = (pquantityInMT / finalSumOfQuantity) * 100;
                        } catch (ArithmeticException e) {
                            productSalesPercentRatio = 0.0;
                        }
                        productMixVolumeAndRatioData.setProductRatio(productSalesPercentRatio);
                        productMixVolumeAndRatioDataList.add(productMixVolumeAndRatioData);

                    }
                });
                productMixVolumeAndRatioListData.setProductVolumeAndRatio(productMixVolumeAndRatioDataList);
                return productMixVolumeAndRatioListData;
            } else {
                if (sclCustomerModel!= null) {
                List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(sclCustomerModel.getState());
                    if (productList != null && !productList.isEmpty()) {
                        for (String product : productList) {
                            ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                            productMixVolumeAndRatioData.setProductRatio(0.0);
                            productMixVolumeAndRatioData.setProductVolume(0.0);
                            productMixVolumeAndRatioData.setProductName(product);
                            productMixVolumeAndRatioDataList.add(productMixVolumeAndRatioData);
                        }
                        productMixVolumeAndRatioListData.setProductVolumeAndRatio(productMixVolumeAndRatioDataList);
                    }
                }
            }

        }else  if ((sclCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))){
           productMixRatio = sclSalesSummaryService.getProductMixSalesDetailsByMonth(sclCustomerModel, month, year, territoryList);
            if(CollectionUtils.isNotEmpty(productMixRatio)) {
                sumOfQuantity = productMixRatio.stream().filter(o -> Objects.nonNull(o) && Objects.nonNull(o.get(1))).mapToDouble(o -> Objects.nonNull(o.get(1)) ? (double) o.get(1) : 0.0).sum();
            }
            List<ProductMixVolumeAndRatioData> productMixVolumeAndRatioDataList = new ArrayList<>();

            //sumOfQuantity = productMixRatio.stream().collect(Collectors.summingDouble(o -> (double) o.get(2)));

            if (productMixRatio != null && !productMixRatio.isEmpty()) {
                double finalSumOfQuantity = sumOfQuantity;
                productMixRatio.forEach(objects -> {
                    ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                    if (objects != null && !objects.isEmpty()) {
                        String productCode = (String) objects.get(0);
                        String productName = (String) objects.get(0);
                        double productSalesPercentRatio=0.0,pquantityInMT=0.0;
                        productMixVolumeAndRatioData.setProductCode(productCode);
                        productMixVolumeAndRatioData.setProductName(productName);
                        //set product sales volume
                        if(objects.get(1)!=null) {
                            pquantityInMT = objects.size() > 1 ? (Double) objects.get(1) : 0.0;
                        }
                        productMixVolumeAndRatioData.setProductVolume(pquantityInMT);
                        //set product sales % ratio
                        try {
                            productSalesPercentRatio = (pquantityInMT / finalSumOfQuantity) * 100;
                        } catch (ArithmeticException e) {
                            productSalesPercentRatio = 0.0;
                        }
                        productMixVolumeAndRatioData.setProductRatio(productSalesPercentRatio);
                        productMixVolumeAndRatioDataList.add(productMixVolumeAndRatioData);

                    }
                });
                productMixVolumeAndRatioListData.setProductVolumeAndRatio(productMixVolumeAndRatioDataList);
                return productMixVolumeAndRatioListData;
            } else {
                if (sclCustomerModel!= null) {
                    //List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(sclUser.getState());
                    List<String> productList = sclProductService.getProductsAliasForSalesPerformance();
                    if (productList != null && !productList.isEmpty()) {
                        for (String product : productList) {
                            ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                            productMixVolumeAndRatioData.setProductRatio(0.0);
                            productMixVolumeAndRatioData.setProductVolume(0.0);
                            productMixVolumeAndRatioData.setProductName(product);
                            productMixVolumeAndRatioDataList.add(productMixVolumeAndRatioData);
                        }
                        productMixVolumeAndRatioListData.setProductVolumeAndRatio(productMixVolumeAndRatioDataList);
                    }
                }
            }
        }

        return productMixVolumeAndRatioListData;
    }
    @Override
    public ProductMixVolumeAndRatioListData getProductMixVolumeAndRatioListDataForMTD(SclUserModel sclUser, BaseSiteModel baseSite, ProductMixVolumeAndRatioListData productMixVolumeAndRatioListData,SclCustomerModel customer,List<String> territoryList) {
        double sumOfQuantity=0.0;
      /*  List<List<Object>> productMixRatio =
                getSalesPerformanceService().getProductwiseSalesPercentRatioAndVolumeRatio(sclUser, baseSite, customer, doList, subAreaList);*/
        List<List<Object>> productMixRatio = sclSalesSummaryService.getProductMixSalesDetailsMTD(sclUser,territoryList);
        List<ProductMixVolumeAndRatioData> productMixVolumeAndRatioDataList = new ArrayList<>();
        //   sumOfQuantity =productMixRatio.stream().filter(o->Objects.nonNull(o) && Objects.nonNull(o.get(2))).mapToDouble(o -> (double) o.get(2)).sum();
        if(CollectionUtils.isNotEmpty(productMixRatio)) {
            sumOfQuantity = productMixRatio.stream().filter(o -> Objects.nonNull(o) && Objects.nonNull(o.get(1))).mapToDouble(o -> Objects.nonNull(o.get(1)) ? (double) o.get(1) : 0.0).sum();
        }
        if (productMixRatio != null && !productMixRatio.isEmpty()) {
            double finalSumOfQuantity = sumOfQuantity;
            productMixRatio.forEach(objects -> {
                ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                if (objects != null && !objects.isEmpty()) {
                    String productCode = (String) objects.get(0);
                    String productName = (String) objects.get(0);
                    double productSalesPercentRatio = 0.0, pquantityInMT = 0.0;
                    productMixVolumeAndRatioData.setProductCode(productCode);
                    productMixVolumeAndRatioData.setProductName(productName);
                    //set product sales volume
                    if (objects.get(1) != null) {
                        pquantityInMT = objects.size() > 1 ? (Double) objects.get(1) : 0.0;
                    }
                    productMixVolumeAndRatioData.setProductVolume(pquantityInMT);
                    //set product sales % ratio
                    try {
                        productSalesPercentRatio = (pquantityInMT / finalSumOfQuantity) * 100;
                    } catch (ArithmeticException e) {
                        productSalesPercentRatio = 0.0;
                    }
                    productMixVolumeAndRatioData.setProductRatio(productSalesPercentRatio);
                    productMixVolumeAndRatioDataList.add(productMixVolumeAndRatioData);

                }
            });
            productMixVolumeAndRatioListData.setProductVolumeAndRatio(productMixVolumeAndRatioDataList);
            return productMixVolumeAndRatioListData;
        } else {
            if(sclUser!=null){
                //List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(sclUser.getState());
                List<String> productList = sclProductService.getProductsAliasForSalesPerformance();
                if (productList != null && !productList.isEmpty()) {
                    for (String product : productList) {
                        ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                        productMixVolumeAndRatioData.setProductRatio(0.0);
                        productMixVolumeAndRatioData.setProductVolume(0.0);
                        productMixVolumeAndRatioData.setProductName(product);
                        productMixVolumeAndRatioDataList.add(productMixVolumeAndRatioData);
                    }
                    productMixVolumeAndRatioListData.setProductVolumeAndRatio(productMixVolumeAndRatioDataList);
                }
            }
            else{
                //List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(customer.getState());
                List<String> productList = sclProductService.getProductsAliasForSalesPerformance();
                if (productList != null && !productList.isEmpty()) {
                    for (String product : productList) {
                        ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                        productMixVolumeAndRatioData.setProductRatio(0.0);
                        productMixVolumeAndRatioData.setProductVolume(0.0);
                        productMixVolumeAndRatioData.setProductName(product);
                        productMixVolumeAndRatioDataList.add(productMixVolumeAndRatioData);
                    }
                    productMixVolumeAndRatioListData.setProductVolumeAndRatio(productMixVolumeAndRatioDataList);
                }
            }
        }
        return productMixVolumeAndRatioListData;
    }
    @Override
    public ProductMixVolumeAndRatioListData getProductMixVolumeAndRatioListDataForYTD(SclUserModel sclUser, BaseSiteModel baseSite, ProductMixVolumeAndRatioListData productMixVolumeAndRatioListData,SclCustomerModel customerModel,List<String> territoryList,List<String> subAreaList,List<String> districtList) {
        double sumOfQuantity=0.0,sumOfQuantity2=0.0;
        //List<List<Object>> productMixRatio = getSalesPerformanceService().getProductwiseSalesPercentRatioAndVolumeRatioForYTD(null, baseSite,customerModel,doList,subAreaList);
        List<List<Object>> productMixRatio=new ArrayList<>();
        if(sclUser!=null)
            {
                productMixRatio = sclSalesSummaryService.getProductMixSalesDetailsYTD(sclUser, territoryList);
                List<ProductMixVolumeAndRatioData> productMixVolumeAndRatioDataList = new ArrayList<>();
                if(CollectionUtils.isNotEmpty(productMixRatio)) {
                    sumOfQuantity = productMixRatio.stream().filter(o -> Objects.nonNull(o) && Objects.nonNull(o.get(1))).mapToDouble(o -> Objects.nonNull(o.get(1)) ? (double) o.get(1) : 0.0).sum();
                }
                if (productMixRatio != null && !productMixRatio.isEmpty()) {
                    double finalSumOfQuantity = sumOfQuantity;
                    productMixRatio.forEach(objects -> {
                        ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                        if (objects != null && !objects.isEmpty()) {
                            String productCode = (String) objects.get(0);
                            String productName = (String) objects.get(0);
                            double productSalesPercentRatio=0.0,pquantityInMT=0.0;
                            productMixVolumeAndRatioData.setProductCode(productCode);
                            productMixVolumeAndRatioData.setProductName(productName);
                            //set product sales volume
                            if(objects.get(1)!=null) {
                                pquantityInMT = objects.size() > 1 ? (Double) objects.get(1) : 0.0;
                                productMixVolumeAndRatioData.setProductVolume(pquantityInMT);
                            }
                            productMixVolumeAndRatioData.setProductVolume(pquantityInMT);
                            //set product sales % ratio
                            try {
                                productSalesPercentRatio = (pquantityInMT / finalSumOfQuantity) * 100;
                            } catch (ArithmeticException e) {
                                productSalesPercentRatio = 0.0;
                            }
                            productMixVolumeAndRatioData.setProductRatio(productSalesPercentRatio);
                            productMixVolumeAndRatioDataList.add(productMixVolumeAndRatioData);

                        }
                    });
                    productMixVolumeAndRatioListData.setProductVolumeAndRatio(productMixVolumeAndRatioDataList);
                    return productMixVolumeAndRatioListData;
                } else {
                    if (sclUser != null) {
                        //List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(sclUser.getState());
                        List<String> productList = sclProductService.getProductsAliasForSalesPerformance();
                        if (productList != null && !productList.isEmpty()) {
                            for (String product : productList) {
                                ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                                productMixVolumeAndRatioData.setProductRatio(0.0);
                                productMixVolumeAndRatioData.setProductVolume(0.0);
                                productMixVolumeAndRatioData.setProductName(product);
                                productMixVolumeAndRatioDataList.add(productMixVolumeAndRatioData);
                            }
                            productMixVolumeAndRatioListData.setProductVolumeAndRatio(productMixVolumeAndRatioDataList);
                        }
                    }
                    else{
                        //List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(customerModel.getState());
                        List<String> productList = sclProductService.getProductsAliasForSalesPerformance();
                        if (productList != null && !productList.isEmpty()) {
                            for (String product : productList) {
                                ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                                productMixVolumeAndRatioData.setProductRatio(0.0);
                                productMixVolumeAndRatioData.setProductVolume(0.0);
                                productMixVolumeAndRatioData.setProductName(product);
                                productMixVolumeAndRatioDataList.add(productMixVolumeAndRatioData);
                            }
                            productMixVolumeAndRatioListData.setProductVolumeAndRatio(productMixVolumeAndRatioDataList);
                        }
                    }
                }
            }
        if(customerModel!=null) {
            if((customerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
                productMixRatio = sclSalesSummaryService.getProductMixSalesDetailsYTD(customerModel, territoryList);
                    List<ProductMixVolumeAndRatioData> productMixVolumeAndRatioDataList = new ArrayList<>();
                if(CollectionUtils.isNotEmpty(productMixRatio)) {
                    sumOfQuantity = productMixRatio.stream().filter(o -> Objects.nonNull(o) && Objects.nonNull(o.get(1))).mapToDouble(o -> Objects.nonNull(o.get(1)) ? (double) o.get(1) : 0.0).sum();
                }
                    if (productMixRatio != null && !productMixRatio.isEmpty()) {
                        double finalSumOfQuantity = sumOfQuantity;
                        productMixRatio.forEach(objects -> {
                            ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                            if (objects != null && !objects.isEmpty()) {
                                String productCode = (String) objects.get(0);
                                String productName = (String) objects.get(0);
                                double productSalesPercentRatio=0.0,pquantityInMT=0.0;
                                productMixVolumeAndRatioData.setProductCode(productCode);
                                productMixVolumeAndRatioData.setProductName(productName);
                                //set product sales volume
                                if(objects.get(1)!=null) {
                                    pquantityInMT = objects.size() > 1 ? (Double) objects.get(1) : 0.0;
                                    productMixVolumeAndRatioData.setProductVolume(pquantityInMT);
                                }
                                productMixVolumeAndRatioData.setProductVolume(pquantityInMT);
                                //set product sales % ratio
                                try {
                                    productSalesPercentRatio = (pquantityInMT / finalSumOfQuantity) * 100;
                                } catch (ArithmeticException e) {
                                    productSalesPercentRatio = 0.0;
                                }
                                productMixVolumeAndRatioData.setProductRatio(productSalesPercentRatio);
                                productMixVolumeAndRatioDataList.add(productMixVolumeAndRatioData);

                            }
                        });
                        productMixVolumeAndRatioListData.setProductVolumeAndRatio(productMixVolumeAndRatioDataList);
                        return productMixVolumeAndRatioListData;
                    } else {
                        if (sclUser != null) {
                            //List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(sclUser.getState());
                            List<String> productList = sclProductService.getProductsAliasForSalesPerformance();
                            if (productList != null && !productList.isEmpty()) {
                                for (String product : productList) {
                                    ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                                    productMixVolumeAndRatioData.setProductRatio(0.0);
                                    productMixVolumeAndRatioData.setProductVolume(0.0);
                                    productMixVolumeAndRatioData.setProductName(product);
                                    productMixVolumeAndRatioDataList.add(productMixVolumeAndRatioData);
                                }
                                productMixVolumeAndRatioListData.setProductVolumeAndRatio(productMixVolumeAndRatioDataList);
                            }
                        }
                        else{
                            //List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(customerModel.getState());
                            List<String> productList = sclProductService.getProductsAliasForSalesPerformance();
                            if (productList != null && !productList.isEmpty()) {
                                for (String product : productList) {
                                    ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                                    productMixVolumeAndRatioData.setProductRatio(0.0);
                                    productMixVolumeAndRatioData.setProductVolume(0.0);
                                    productMixVolumeAndRatioData.setProductName(product);
                                    productMixVolumeAndRatioDataList.add(productMixVolumeAndRatioData);
                                }
                                productMixVolumeAndRatioListData.setProductVolumeAndRatio(productMixVolumeAndRatioDataList);
                            }
                        }
                    }
            }
            else if((customerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))
            || (customerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SP_GROUP_ID))))){
                productMixRatio = getSalesPerformanceService().getProductwiseSalesPercentRatioAndVolumeRatioForYTD(null, baseSite,customerModel,districtList,subAreaList);
                List<ProductMixVolumeAndRatioData> productMixVolumeAndRatioDataList = new ArrayList<>();
                if(CollectionUtils.isNotEmpty(productMixRatio)) {
                    sumOfQuantity = productMixRatio.stream().filter(o -> Objects.nonNull(o) && Objects.nonNull(o.get(2))).mapToDouble(o -> Objects.nonNull(o.get(2)) ? (double) o.get(2) : 0.0).sum();
                }
              //  sumOfQuantity2 = productMixRatio.stream().filter(o->Objects.nonNull(o) && Objects.nonNull(o.get(3))).mapToDouble(o -> Objects.nonNull(o.get(3)) ? (double) o.get(3) : 0.0).sum();
                if (productMixRatio != null && !productMixRatio.isEmpty()) {
                    double finalSumOfQuantity = sumOfQuantity;
                    productMixRatio.forEach(objects -> {
                        ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                        if (objects != null && !objects.isEmpty()) {
                            String productCode=null;
                            if(Objects.nonNull(objects.get(3)))
                                productCode = (String) objects.get(3);
                            else
                                productCode = (String) objects.get(0);
                            String productName = (String) objects.get(1);
                            double productSalesPercentRatio=0.0,pquantityInMT=0.0,pquantityInBag2=0.0,pquantityInBag1=0.0;
                            productMixVolumeAndRatioData.setProductCode(productCode);
                            productMixVolumeAndRatioData.setProductName(productName);
                            //set product sales volume
                            if(objects.get(2)!=null) {
                                pquantityInBag1 = objects.size() > 2 ? (Double) objects.get(2) : 0.0;
                             //   pquantityInBag2= objects.size() > 3 ? (Double) objects.get(3) : 0.0;
                                pquantityInMT=pquantityInBag1;
                                productMixVolumeAndRatioData.setProductVolume(pquantityInMT);
                            }
                            productMixVolumeAndRatioData.setProductVolume(pquantityInMT);
                            //set product sales % ratio
                            try {
                                productSalesPercentRatio = (pquantityInMT / finalSumOfQuantity) * 100;
                            } catch (ArithmeticException e) {
                                productSalesPercentRatio = 0.0;
                            }
                            productMixVolumeAndRatioData.setProductRatio(productSalesPercentRatio);
                            productMixVolumeAndRatioDataList.add(productMixVolumeAndRatioData);

                        }
                    });
                    productMixVolumeAndRatioListData.setProductVolumeAndRatio(productMixVolumeAndRatioDataList);
                    return productMixVolumeAndRatioListData;
                } else {
                    if (customerModel.getState() != null) {
                        List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(customerModel.getState());
                        //List<String> productList = sclProductService.getProductsAliasForSalesPerformance();
                        if (productList != null && !productList.isEmpty()) {
                            for (String product : productList) {
                                ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                                productMixVolumeAndRatioData.setProductRatio(0.0);
                                productMixVolumeAndRatioData.setProductVolume(0.0);
                                productMixVolumeAndRatioData.setProductName(product);
                                productMixVolumeAndRatioDataList.add(productMixVolumeAndRatioData);
                            }
                            productMixVolumeAndRatioListData.setProductVolumeAndRatio(productMixVolumeAndRatioDataList);
                        }
                    }
                }
            }
        }
        return productMixVolumeAndRatioListData;
    }

    @Override
    public PartnerDetailsDataForSales getPartnerDetailsForSales(String searchKeyWord, String filter, Integer year, Integer month,List<String> doList,List<String> territoryList) {
        List<List<Object>> partnerDetailsForSales = salesPerformanceService.getPartnerDetailsForSales(searchKeyWord);
        PartnerDetailsDataForSales data=new PartnerDetailsDataForSales();
        for (List<Object> partnerDetailsForSale : partnerDetailsForSales) {

            String code = (String) partnerDetailsForSale.get(0);
            SclCustomerModel sclCustomerModel = (SclCustomerModel) userService.getUserForUID(code);
            String name = (String) partnerDetailsForSale.get(1);
            String mobileNumber = (String) partnerDetailsForSale.get(2);
            String customerNumber = StringUtils.isNotEmpty((String) partnerDetailsForSale.get(3)) ? (String) partnerDetailsForSale.get(3) :"";
            data.setCode(code);
            data.setName(name);
            data.setMobileNumber(mobileNumber);
            data.setCustomerNumber(customerNumber);

            SclUserModel sclUser=(SclUserModel) getUserService().getCurrentUser();
            BaseSiteModel currentBaseSite=baseSiteService.getCurrentBaseSite();
            double actualTarget = 0.0, totalTarget=0.0;
            double achievementPercentage=0.0;
            if(StringUtils.isBlank(filter))
            {
                if(year!=0 && month!=0)
                {
                    //actualTarget=getSalesPerformanceService().getActualTargetForPartnerSalesForMonth(code,sclUser,currentBaseSite,month,year,doList,subAreaList);
                    actualTarget = sclSalesSummaryService.getSalesForPartner(customerNumber,null,month,year,territoryList);
                    data.setActualSales(actualTarget);
                    totalTarget=getSalesPerformanceService().getMonthlySalesTargetForDealer(sclCustomerModel, currentBaseSite, month, year,null);
                    //totalTarget=getSalesPerformanceService().getSalesTargetForPartnerMonth(code, sclUser, currentBaseSite, month, year);
                    data.setTarget(totalTarget);

                    if(actualTarget!=0.0 && totalTarget!=0.0)
                        achievementPercentage=(actualTarget/totalTarget)*100;
                    data.setAchievementPercentage(achievementPercentage);

                }
                else
                {
                   // actualTarget = sclSalesSummaryService.getSalesForPartner(customerNumber,FILTER_MTD,0,0);
                    actualTarget=sclSalesSummaryService.getCurrentMonthSales(sclCustomerModel,territoryList);
                    //actualTarget=getSalesPerformanceService().getActualTargetForPartnerSalesMTD(code,sclUser,currentBaseSite,doList,subAreaList);
                    data.setActualSales(actualTarget);

                    totalTarget=getSalesPerformanceService().getMonthlySalesTargetForDealer(sclCustomerModel, currentBaseSite, month, year,null);
                    data.setTarget(totalTarget);

                    if(actualTarget!=0.0 && totalTarget!=0.0)
                        achievementPercentage=(actualTarget/totalTarget)*100;
                    data.setAchievementPercentage(achievementPercentage);
                }

            }
            else
            {
                if(filter.contains(FILTER_MTD))
                {
                    actualTarget=sclSalesSummaryService.getCurrentMonthSales(sclCustomerModel,territoryList);
                   // actualTarget=getSalesPerformanceService().getActualTargetForPartnerSalesMTD(code,sclUser,currentBaseSite,doList,subAreaList);
                    data.setActualSales(actualTarget);

                    totalTarget=getSalesPerformanceService().getMonthlySalesTargetForDealer(sclCustomerModel, currentBaseSite,null);
                    data.setTarget(totalTarget);

                    if(actualTarget!=0.0 && totalTarget!=0.0)
                        achievementPercentage=(actualTarget/totalTarget)*100;
                    data.setAchievementPercentage(achievementPercentage);

                }
                else if(filter.contains(FILTER_YTD))
                {
                    actualTarget = sclSalesSummaryService.getCurrentFySales(sclCustomerModel,territoryList);
                    //actualTarget=getSalesPerformanceService().getActualTargetForPartnerSalesYTD(code,sclUser,currentBaseSite,doList,subAreaList);
                    data.setActualSales(actualTarget);

                    totalTarget=getSalesPerformanceService().getAnnualSalesTargetForDealer(sclCustomerModel, null);
                    data.setTarget(totalTarget);

                    if(actualTarget!=0.0 && totalTarget!=0.0)
                        achievementPercentage=(actualTarget/totalTarget)*100;
                    data.setAchievementPercentage(achievementPercentage);
                }
            }
        }
        return data;
    }

    @Override
    public ProductMixVolumeAndRatioListData getBrandWiseSalesPercentRatioAndVolumeRatio(String customerCode) {
        double sumOfQuantity = 0.0;
        SclCustomerModel currentUser = (SclCustomerModel) getUserService().getUserForUID(customerCode);
        ProductMixVolumeAndRatioListData productMixVolumeAndRatioListData = new ProductMixVolumeAndRatioListData();
        List<List<Object>> productMixRatio = salesPerformanceService.getBrandwiseSalesPercentRatioAndVolumeRatioForCustomer(currentUser);
        List<ProductMixVolumeAndRatioData> productMixVolumeAndRatioDataList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(productMixRatio)) {
            sumOfQuantity = productMixRatio.stream().collect(Collectors.summingDouble(o -> (double) o.get(2)));
        }

        if (productMixRatio != null && !productMixRatio.isEmpty()) {
            double finalSumOfQuantity = sumOfQuantity;
            productMixRatio.forEach(objects -> {
                ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                if (objects != null && !objects.isEmpty()) {
                    String productCode=null;
                    if(Objects.nonNull(objects.get(3)))
                        productCode = (String) objects.get(3);
                    else
                        productCode = (String) objects.get(0);
                    String productName = (String) objects.get(1);
                    double pquantityInMT = objects.size() > 2 ? (Double) objects.get(2) : 0.0;
                    double productSalesPercentRatio;
                    productMixVolumeAndRatioData.setProductCode(productCode);
                    productMixVolumeAndRatioData.setProductName(productName);
                    //set product sales volume
                    productMixVolumeAndRatioData.setProductVolume(pquantityInMT);
                    //set product sales % ratio
                    try {
                        productSalesPercentRatio = (pquantityInMT / finalSumOfQuantity) * 100;
                    } catch (ArithmeticException e) {
                        productSalesPercentRatio = 0.0;
                    }
                    productMixVolumeAndRatioData.setProductRatio(productSalesPercentRatio);
                    productMixVolumeAndRatioDataList.add(productMixVolumeAndRatioData);

                }
            });
            productMixVolumeAndRatioListData.setProductVolumeAndRatio(productMixVolumeAndRatioDataList);
        }
        return productMixVolumeAndRatioListData;
    }

    @Override
    public ProductMixVolumeAndRatioListData getProductMixPercentRatioAndVolumeRatioWithPoints(String filter) {
        double sumOfQuantity = 0.0;
        //SclCustomerModel currentUser1 = (SclCustomerModel) getUserService().getUserForUID(customerCode);
        SclCustomerModel currentUser = (SclCustomerModel) userService.getCurrentUser();
        ProductMixVolumeAndRatioListData productMixVolumeAndRatioListData = new ProductMixVolumeAndRatioListData();
        List<List<Object>> productMixRatio = salesPerformanceService.getProductMixPercentRatioAndVolumeRatioWithPoints(currentUser,filter);
        List<ProductMixVolumeAndRatioData> productMixVolumeAndRatioDataList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(productMixRatio)) {
            sumOfQuantity = productMixRatio.stream().collect(Collectors.summingDouble(o -> (double) o.get(2)));
        }

        if (productMixRatio != null && !productMixRatio.isEmpty()) {
            double finalSumOfQuantity = sumOfQuantity;
            productMixRatio.forEach(objects -> {
                ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                if (objects != null && !objects.isEmpty()) {
                    String productCode = (String) objects.get(0);
                    String productName = (String) objects.get(1);
                    double pquantityInMT = objects.size() > 2 ? (Double) objects.get(2) : 0.0;
                    double ppoints=objects.size()>3?(Double) objects.get(3):0.0;
                    double productSalesPercentRatio;
                    productMixVolumeAndRatioData.setProductCode(productCode);
                    productMixVolumeAndRatioData.setProductName(productName);
                    //set product sales volume
                    productMixVolumeAndRatioData.setProductVolume(pquantityInMT);
                    //set product sales % ratio
                    try {
                        productSalesPercentRatio = (pquantityInMT / finalSumOfQuantity) * 100;
                    } catch (ArithmeticException e) {
                        productSalesPercentRatio = 0.0;
                    }
                    productMixVolumeAndRatioData.setProductRatio(productSalesPercentRatio);
                    productMixVolumeAndRatioData.setPointsRewarded(ppoints);
                    
                    if(productCode!=null) {
                    	Double currentPoints = pointRequisitionService.getPointsForRequisition(productCode, currentUser.getUid());
                    	productMixVolumeAndRatioData.setCurrentPoints(currentPoints);
                    }
                    productMixVolumeAndRatioDataList.add(productMixVolumeAndRatioData);

                }
            });
            productMixVolumeAndRatioListData.setProductVolumeAndRatio(productMixVolumeAndRatioDataList);
        }
        return productMixVolumeAndRatioListData;
    }

    @Override
    public SalesAndAchievementData getProratedActualAndTargetForSalesDealerRetailer(String filter, Integer yearFilter, Integer monthFilter,String bgpFilter) {
        SclCustomerModel currentUser = (SclCustomerModel) getUserService().getCurrentUser();
        BaseSiteModel currentBaseSite=baseSiteService.getCurrentBaseSite();
        SalesAndAchievementData data=new SalesAndAchievementData();
        double actualTarget=0.0, totalTarget=0.0, proratedTarget=0.0 ,achievementPercentage =0.0, behindProratedTarget=0.0, aheadProratedTarget=0.0;
        LocalDate cal=LocalDate.now();
        int noOfDaysInTheMonth = cal.lengthOfMonth();
        int noOfDaysGoneByInTheMonth = cal.getDayOfMonth() - 1;
        int dayOfYear = cal.getDayOfYear();
        int year = cal.getYear();
        LocalDate calTwo=LocalDate.of(year,12,31);
        int totalNoOfDaysInYear = calTwo.getDayOfYear();
        int remainingDaysInTheYear = totalNoOfDaysInYear - dayOfYear;
        int noOfDaysGoneBy=totalNoOfDaysInYear-remainingDaysInTheYear;
       if(StringUtils.isBlank(filter))
        {
            if(yearFilter!=0 && monthFilter!=0) {

                cal= LocalDate.of(yearFilter,monthFilter,cal.getDayOfMonth());
                noOfDaysGoneByInTheMonth = cal.getDayOfMonth() -1;

                if(currentUser.getCounterType().equals(CounterType.SP)){
                    RequestCustomerData requestData = new RequestCustomerData();
                    List<String> counterType = List.of(DEALER);
                    requestData.setCounterType(counterType);
                    List<SclCustomerModel> customerforUser = territoryManagementService.getCustomerforUser(requestData);
                    if(customerforUser!=null && !customerforUser.isEmpty()) {
                        for (SclCustomerModel customer : customerforUser) {
                            LOGGER.info(String.format("Scl customer Model PK:%s", customer));
                        }
                    }
                    else{
                        LOGGER.info("SP List is Empty");
                    }
                    if(customerforUser!=null && !customerforUser.isEmpty()) {
                        for(SclCustomerModel customer:customerforUser){
                            actualTarget = sclSalesSummaryService.getSalesByMonth(customer, monthFilter, yearFilter, Collections.emptyList());
                        }
                       // actualTarget = getSalesPerformanceService().getActualTargetForSalesForMonthSP(customerforUser, yearFilter, monthFilter);
                        data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);
                        totalTarget = getSalesPerformanceService().getSalesTargetForMonthSP(customerforUser, yearFilter, monthFilter);
                        LOGGER.info(String.format("Actual Target:%s", actualTarget));
                        LOGGER.info(String.format("Total Target:%s", totalTarget));
                    }
                    else{
                        data.setActualTarget(0.0);
                        data.setTotalTarget(0.0);
                    }
                }

                else if(currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                    //actualTarget = getSalesPerformanceService().getActualTargetForSalesForMonthDealer(currentUser, currentBaseSite, yearFilter, monthFilter,bgpFilter);
                    actualTarget = sclSalesSummaryService.getSalesByMonth(currentUser,monthFilter,yearFilter,bgpFilter);
                    data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);
                    totalTarget = getSalesPerformanceService().getSalesTargetForMonthDealer(currentUser, currentBaseSite, yearFilter, monthFilter,bgpFilter);
                }
                else if(currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                    //actualTarget = getSalesPerformanceService().getActualTargetForSalesForMonthRetailer(currentUser, currentBaseSite, yearFilter, monthFilter,bgpFilter);
                    Map<String,Double> salesQty=new HashMap<>();
                    salesQty = networkService.getSalesQuantityForRetailerByMonthYear(currentUser,monthFilter,yearFilter,null,bgpFilter);
                    actualTarget=salesQty.get("quantityInBags");
                    data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);
                    totalTarget = getSalesPerformanceService().getSalesTargetForMonthRetailer(currentUser.getUid(), currentBaseSite, yearFilter, monthFilter,bgpFilter);
                }
                if(yearFilter==LocalDate.now().getYear() && monthFilter==LocalDate.now().getMonthValue()){
                    if(totalTarget!=0.0)
                        proratedTarget=(totalTarget/noOfDaysInTheMonth)*noOfDaysGoneByInTheMonth;
                    data.setProratedTarget(proratedTarget!=0.0?proratedTarget:0.0);

                    if(actualTarget!=0.0 && proratedTarget!=0.0)
                        achievementPercentage=(actualTarget/proratedTarget)*100;
                    data.setAchievementPercentage(achievementPercentage!=0.0 ?achievementPercentage :0.0);

                    if(achievementPercentage < 100)
                    {
                        //if(proratedTarget!=0.0 && actualTarget !=0.0)
                        behindProratedTarget=proratedTarget-actualTarget;
                        data.setBehindProratedTarget(behindProratedTarget!=0.0 ? behindProratedTarget :0.0);
                    }
                    else if(achievementPercentage > 100)
                    {
                        //if(proratedTarget!=0.0 && actualTarget !=0.0)
                        aheadProratedTarget=actualTarget-proratedTarget;
                        data.setAheadProratedTarget(aheadProratedTarget!=0.0?aheadProratedTarget:0.0);
                    }
                }else{
                    if(totalTarget!=0.0)
                        proratedTarget=totalTarget;
                    data.setProratedTarget(proratedTarget!=0.0?proratedTarget:0.0);

                    if(actualTarget!=0.0 && proratedTarget!=0.0)
                        achievementPercentage=(actualTarget/proratedTarget)*100;
                    data.setAchievementPercentage(achievementPercentage!=0.0 ?achievementPercentage :0.0);

                    if(achievementPercentage < 100)
                    {
                        //if(proratedTarget!=0.0 && actualTarget !=0.0)
                        behindProratedTarget=proratedTarget-actualTarget;
                        data.setBehindProratedTarget(behindProratedTarget!=0.0 ? behindProratedTarget :0.0);
                    }
                    else if(achievementPercentage > 100)
                    {
                        //if(proratedTarget!=0.0 && actualTarget !=0.0)
                        aheadProratedTarget=actualTarget-proratedTarget;
                        data.setAheadProratedTarget(aheadProratedTarget!=0.0?aheadProratedTarget:0.0);
                    }
                }
            }
            else
            {
                if(currentUser.getCounterType().equals(CounterType.SP)){
                    RequestCustomerData requestData = new RequestCustomerData();
                    List<String> counterType = List.of(DEALER);
                    requestData.setCounterType(counterType);
                    List<SclCustomerModel> customerforUser = territoryManagementService.getCustomerforUser(requestData);
                    if(customerforUser!=null && !customerforUser.isEmpty()) {
                        for (SclCustomerModel customer : customerforUser) {
                            LOGGER.info(String.format("Scl customer Model PK:%s", customer));
                        }
                    }
                    else{
                        LOGGER.info("SP List is Empty");
                    }
                    if(customerforUser!=null && !customerforUser.isEmpty()) {
                        for(SclCustomerModel customer: customerforUser ){
                            actualTarget = sclSalesSummaryService.getCurrentMonthSales(customer, Collections.emptyList());
                        }
                      //  actualTarget = getSalesPerformanceService().getActualTargetForSalesSPMTD(customerforUser);
                        data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);
                        totalTarget = getSalesPerformanceService().getMonthlySalesTargetForSP(customerforUser);
                        LOGGER.info(String.format("Actual Target:%s", actualTarget));
                        LOGGER.info(String.format("Total Target:%s", totalTarget));
                    }
                    else{
                        data.setActualTarget(0.0);
                        data.setTotalTarget(0.0);
                    }
                }
                else if(currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                    //actualTarget = getSalesPerformanceService().getActualTargetForSalesDealerMTD(currentUser, currentBaseSite,bgpFilter);
                    actualTarget = sclSalesSummaryService.getCurrentMonthSales(currentUser,bgpFilter);
                    data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);
                    totalTarget = getSalesPerformanceService().getMonthlySalesTargetForDealer(currentUser, currentBaseSite,bgpFilter);
                }
                else if(currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                  //  actualTarget = getSalesPerformanceService().getActualTargetForSalesRetailerMTD(currentUser, currentBaseSite,bgpFilter);
                    Map<String,Double> salesQty=new HashMap<>();
                    salesQty = networkService.getSalesQuantityForRetailerByMTD(currentUser,null,bgpFilter);
                    actualTarget=salesQty.get("quantityInBags");
                    data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);
                    totalTarget = getSalesPerformanceService().getMonthlySalesTargetForRetailer(currentUser.getUid(), currentBaseSite,bgpFilter);
                }

                if(totalTarget!=0.0)
                    proratedTarget=(totalTarget/noOfDaysInTheMonth)*noOfDaysGoneByInTheMonth;
                data.setProratedTarget(proratedTarget!=0.0?proratedTarget:0.0);

                if(actualTarget!=0.0 && proratedTarget!=0.0)
                    achievementPercentage=(actualTarget/proratedTarget)*100;
                data.setAchievementPercentage(achievementPercentage!=0.0 ?achievementPercentage :0.0);

                if(achievementPercentage < 100)
                {
                  //  if(proratedTarget!=0.0 && actualTarget !=0.0)
                        behindProratedTarget=proratedTarget-actualTarget;
                    data.setBehindProratedTarget(behindProratedTarget!=0.0 ? behindProratedTarget :0.0);
                }
                else if(achievementPercentage > 100)
                {
                   // if(proratedTarget!=0.0 && actualTarget !=0.0)
                    aheadProratedTarget=actualTarget-proratedTarget;
                    data.setAheadProratedTarget(aheadProratedTarget!=0.0?aheadProratedTarget:0.0);
                }
            }
        }
        else
        {
            if(filter.contains(FILTER_MTD))
            {
                if(currentUser.getCounterType().equals(CounterType.SP)){
                    RequestCustomerData requestData = new RequestCustomerData();
                    List<String> counterType = List.of(DEALER);
                    requestData.setCounterType(counterType);
                    List<SclCustomerModel> customerforUser = territoryManagementService.getCustomerforUser(requestData);
                    if(customerforUser!=null && !customerforUser.isEmpty()) {
                        for (SclCustomerModel customer : customerforUser) {
                            LOGGER.info(String.format("Scl customer Model PK:%s", customer));
                        }
                    }
                    else{
                        LOGGER.info("SP List is Empty");
                    }
                    if(customerforUser!=null && !customerforUser.isEmpty()) {
                        for(SclCustomerModel customer: customerforUser){
                            actualTarget = sclSalesSummaryService.getCurrentMonthSales(customer, Collections.emptyList());
                        }
                      //  actualTarget = getSalesPerformanceService().getActualTargetForSalesSPMTD(customerforUser);
                        data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);
                        totalTarget = getSalesPerformanceService().getMonthlySalesTargetForSP(customerforUser);
                        LOGGER.info(String.format("Actual Target:%s", actualTarget));
                        LOGGER.info(String.format("Total Target:%s", totalTarget));
                    }
                    else{
                        data.setActualTarget(0.0);
                        data.setTotalTarget(0.0);
                    }
                }
                else if(currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                    //actualTarget = getSalesPerformanceService().getActualTargetForSalesDealerMTD(currentUser, currentBaseSite,bgpFilter);
                    actualTarget = sclSalesSummaryService.getCurrentMonthSales(currentUser,bgpFilter);
                    data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);

                    totalTarget = getSalesPerformanceService().getMonthlySalesTargetForDealer(currentUser, currentBaseSite,bgpFilter);
                }
                else if(currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                    //actualTarget = getSalesPerformanceService().getActualTargetForSalesRetailerMTD(currentUser, currentBaseSite,bgpFilter);
                    Map<String,Double> salesQty=new HashMap<>();
                    salesQty = networkService.getSalesQuantityForRetailerByMTD(currentUser,null,bgpFilter);
                    actualTarget=salesQty.get("quantityInBags");
                    data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);

                    totalTarget = getSalesPerformanceService().getMonthlySalesTargetForRetailer(currentUser.getUid(), currentBaseSite,bgpFilter);
                }
                if(totalTarget!=0.0)
                    proratedTarget=(totalTarget/noOfDaysInTheMonth)*noOfDaysGoneByInTheMonth;
                data.setProratedTarget(proratedTarget!=0.0?proratedTarget:0.0);

                if(actualTarget!=0.0 && proratedTarget!=0.0)
                    achievementPercentage=(actualTarget/proratedTarget)*100;
                data.setAchievementPercentage(achievementPercentage!=0.0 ?achievementPercentage :0.0);

                if(achievementPercentage < 100)
                {
                   // if(actualTarget!=0.0 && proratedTarget!=0.0)
                        behindProratedTarget=proratedTarget-actualTarget;
                    data.setBehindProratedTarget(behindProratedTarget!=0.0 ? behindProratedTarget :0.0);
                }
                else if(achievementPercentage > 100)
                {
                   // if(actualTarget!=0.0 && proratedTarget!=0.0)
                    aheadProratedTarget=actualTarget-proratedTarget;
                    data.setAheadProratedTarget(aheadProratedTarget!=0.0?aheadProratedTarget:0.0);
                }
            }
            else if(filter.contains(FILTER_YTD))
            {
                List<Date> currentFinancialYear = salesPerformanceService.getCurrentFinancialYear();
                Date startDate=currentFinancialYear.get(0);
                LocalDate calYtd=startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                LocalDate curDate=LocalDate.now();
                long dayOfYear1 =  (Math.abs(ChronoUnit.DAYS.between(curDate, calYtd)));
                year = calYtd.getYear();//2023
                LocalDate calTwoYtd= LocalDate.of(year,12,31);
                totalNoOfDaysInYear = calTwoYtd.getDayOfYear();//365
                long remainingDaysInTheYear1 = totalNoOfDaysInYear - dayOfYear1;//283
                long noOfDaysGoneBy1=Math.abs(totalNoOfDaysInYear-remainingDaysInTheYear1);//82

                if(currentUser.getCounterType().equals(CounterType.SP)){
                    RequestCustomerData requestData = new RequestCustomerData();
                    List<String> counterType = List.of(DEALER);
                    requestData.setCounterType(counterType);
                    List<SclCustomerModel> customerforUser = territoryManagementService.getCustomerforUser(requestData);
                    if(customerforUser!=null && !customerforUser.isEmpty()) {
                        for (SclCustomerModel customer : customerforUser) {
                            LOGGER.info(String.format("Scl customer Model PK:%s", customer));
                        }
                    }
                    else{
                        LOGGER.info("SP List is Empty");
                    }
                    if(customerforUser!=null && !customerforUser.isEmpty()) {
                        for(SclCustomerModel customer: customerforUser){
                            actualTarget = sclSalesSummaryService.getCurrentFySales(customer,Collections.emptyList());
                        }
                        //actualTarget = getSalesPerformanceService().getActualTargetForSalesYTDSP(customerforUser, currentBaseSite);
                        data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);
                        totalTarget = getSalesPerformanceService().getAnnualSalesTargetForSP(customerforUser);
                        LOGGER.info(String.format("Actual Target:%s", actualTarget));
                        LOGGER.info(String.format("Total Target:%s", totalTarget));
                    }
                    else{
                        data.setActualTarget(0.0);
                        data.setTotalTarget(0.0);
                    }
                }
                else if(currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                   // actualTarget = getSalesPerformanceService().getActualTargetForSalesYTDDealer(currentUser, currentBaseSite,bgpFilter);
                    actualTarget = sclSalesSummaryService.getCurrentFySales(currentUser,bgpFilter);
                    data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);

                   totalTarget = getSalesPerformanceService().getAnnualSalesTargetForDealer(currentUser,bgpFilter);
                }
                else if(currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                    Map<String,Double> salesQty=new HashMap<>();
                    salesQty = networkService.getSalesQuantityForRetailerByYTD(currentUser,null,bgpFilter);
                    actualTarget=salesQty.get("quantityInBags");
                    //actualTarget = getSalesPerformanceService().getActualTargetForSalesYTDRetailer(currentUser, currentBaseSite,bgpFilter);
                    data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);

                    totalTarget = getSalesPerformanceService().getAnnualSalesTargetForRetailer(currentUser,bgpFilter);
                }
                if(totalTarget!=0.0)
                    proratedTarget=(totalTarget/totalNoOfDaysInYear)*noOfDaysGoneBy1;
                data.setProratedTarget(proratedTarget!=0.0?proratedTarget:0.0);

                if(actualTarget!=0.0 && proratedTarget!=0.0)
                    achievementPercentage=(actualTarget/proratedTarget)*100;
                data.setAchievementPercentage(achievementPercentage!=0.0 ?achievementPercentage :0.0);

                if(achievementPercentage < 100)
                {
                   // if(actualTarget!=0.0 && proratedTarget!=0.0)
                        behindProratedTarget=proratedTarget-actualTarget;
                    data.setBehindProratedTarget(behindProratedTarget!=0.0 ? behindProratedTarget :0.0);
                }
                else if(achievementPercentage > 100)
                {
                    //if(actualTarget!=0.0 && proratedTarget!=0.0)
                    aheadProratedTarget=actualTarget-proratedTarget;
                    data.setAheadProratedTarget(aheadProratedTarget!=0.0?aheadProratedTarget:0.0);
                }
            }
        }
        return data;
    }

    @Override
    public LeaderboardListData getSalesLeaderboardEmpList(String filter, String leadType) {
        SclCustomerModel sclUser=(SclCustomerModel) getUserService().getCurrentUser();
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        LeaderboardListData list=new LeaderboardListData();
        Date startDate;
        Date endDate;
        Date currentDate = Calendar.getInstance().getTime();
        int monthInNumber = currentDate.getMonth();
        List<LeaderboardData> leaderboardDataList=new ArrayList<>();

        if(leadType.equalsIgnoreCase(DEALER))
        {
            list.setEmpCode(sclUser.getUid());
            list.setEmpName(sclUser.getName());
            List<SclCustomerModel> allSalesOfficersByState =new ArrayList<>();
            List<SclCustomerModel> customerList = new ArrayList<>();
            DistrictMasterModel district = null;
            List<SubAreaMasterModel> subAreaMasterModelList = territoryManagementService.getTerritoriesForCustomer(sclUser);

            if (CollectionUtils.isNotEmpty(subAreaMasterModelList) && subAreaMasterModelList.get(0) != null) {
                if (subAreaMasterModelList.get(0).getDistrict() != null) {
                    LOG.info("get district:" + subAreaMasterModelList.get(0).getDistrict());
                    String district1 = subAreaMasterModelList.get(0).getDistrict();
                    list.setDistrict(district1);
                }

                district = subAreaMasterModelList.get(0).getDistrictMaster();

            }

            Map<String, Double> unsortedLeaderboard = new HashMap<>();
            Map<String, Double> unsortedPremiumSalesLeaderboard = new HashMap<>();
            Map<String, Double> unsortedCashDiscountAvailedPercentage = new HashMap<>();

            if (filter.equals(FILTER_YTD)) {
                List<Date> dates = salesPerformanceService.getCurrentFinancialYearSales();
                startDate=dates.get(0);
                endDate=dates.get(1);
                //list.setEmpSale(salesPerformanceService.getSalesByDeliveryDate(subArea, sclUser, currentBaseSite, startDate, endDate));
                LOGGER.info("Entering YTD sales");
                List<List<Object>> salesLeaderByDate = salesPerformanceService.getSalesLeaderByDate(district, currentBaseSite, startDate, endDate);

                for (List<Object> objects : salesLeaderByDate) {
                    LOGGER.info(String.format("sales uid %s :: sum %s ::",String.valueOf(objects.get(0)),String.valueOf(objects.get(1))));
                }
                for (List<Object> entry : salesLeaderByDate) {
                    if (entry.size() >= 2) {
                        String key = String.valueOf(entry.get(0));
                        Double value = (Double) entry.get(1);
                        unsortedLeaderboard.put(key, value);
                    }
                }
                LOGGER.info("Entering YTD Premium sales");
                List<List<Object>> premiumSalesLeaderByDate = salesPerformanceService.getPremiumSalesLeaderByDate(district, currentBaseSite, startDate, endDate);
                for (List<Object> objects : premiumSalesLeaderByDate) {
                    LOGGER.info(String.format("premium sales uid %s :: sum %s ::",String.valueOf(objects.get(0)),String.valueOf(objects.get(1))));
                }

                for (List<Object> entry : premiumSalesLeaderByDate) {
                    if (entry.size() >= 2) {
                        String key = String.valueOf(entry.get(0));
                        Double value = (Double) entry.get(1);
                        unsortedPremiumSalesLeaderboard.put(key, value);
                    }
                }

            }
            else if (filter.equals(FILTER_MTD)) {
                startDate = getFirstDateOfMonth(monthInNumber);
                endDate = getLastDateOfMonth(monthInNumber);
                List<List<Object>> salesLeaderByDate = salesPerformanceService.getSalesLeaderByDate(district, currentBaseSite, startDate, endDate);
                for (List<Object> entry : salesLeaderByDate) {
                    if (entry.size() >= 2) {
                        String key = String.valueOf(entry.get(0));
                        Double value = (Double) entry.get(1);
                        LOGGER.info(String.format("SaleValue :: %s",String.valueOf(value)));
                        unsortedLeaderboard.put(key, value);
                    }
                }
                List<List<Object>> premiumSalesLeaderByDate = salesPerformanceService.getPremiumSalesLeaderByDate(district, currentBaseSite, startDate, endDate);
                for (List<Object> entry : premiumSalesLeaderByDate) {
                    if (entry.size() >= 2) {
                        String key = String.valueOf(entry.get(0));
                        Double value = (Double) entry.get(1);
                        LOGGER.info(String.format("SaleValue :: %s",String.valueOf(value)));
                        unsortedPremiumSalesLeaderboard.put(key, value);
                    }
                }
            }
            else if (filter.equals("QTD")) {
                Calendar calendar = Calendar.getInstance();
                int currentYear = calendar.get(Calendar.YEAR);
                int currentMonth = calendar.get(Calendar.MONTH);

                int currentQuarter = (currentMonth / 3) + 1;
                int startMonth = (currentQuarter - 1) * 3;
                int endMonth = startMonth + 2;

                calendar.set(currentYear, startMonth, 1);
                startDate = calendar.getTime();

                calendar.set(currentYear, endMonth, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                endDate = calendar.getTime();
                List<List<Object>> salesLeaderByDate = salesPerformanceService.getSalesLeaderByDate(district, currentBaseSite, startDate, endDate);
                for (List<Object> entry : salesLeaderByDate) {
                    if (entry.size() >= 2) {
                        String key = String.valueOf(entry.get(0));
                        Double value = (Double) entry.get(1);
                        unsortedLeaderboard.put(key, value);
                    }
                }
                List<List<Object>> premiumSalesLeaderByDate = salesPerformanceService.getPremiumSalesLeaderByDate(district, currentBaseSite, startDate, endDate);
                for (List<Object> entry : premiumSalesLeaderByDate) {
                    if (entry.size() >= 2) {
                        String key = String.valueOf(entry.get(0));
                        Double value = (Double) entry.get(1);
                        unsortedPremiumSalesLeaderboard.put(key, value);
                    }
                }
            }

            Map<String, Double> stringDoubleMap = calculatePercentRank(unsortedLeaderboard);
            Map<String, Double> stringDoubleMap1 = calculatePercentRank(unsortedPremiumSalesLeaderboard);

            List<Map.Entry<String, Double>> entries = new ArrayList<>(stringDoubleMap.entrySet());

            Collections.sort(entries, Map.Entry.comparingByValue(Comparator.reverseOrder()));

            Map<String, Double> sortedLeaderboard = new LinkedHashMap<>();

            for (Map.Entry<String, Double> entry : entries) {
                sortedLeaderboard.put(entry.getKey(), entry.getValue());

            }

            List<Map.Entry<String, Double>> entrie = new ArrayList<>(stringDoubleMap1.entrySet());

            Collections.sort(entrie, Map.Entry.comparingByValue(Comparator.reverseOrder()));

            Map<String, Double> sortedPremiumLeaderboard = new LinkedHashMap<>();

            // Iterate over the sorted entries and put them into the sortedLeaderboard
            for (Map.Entry<String, Double> entry : entrie) {
                sortedPremiumLeaderboard.put(entry.getKey(), entry.getValue());
            }


            List<LeaderboardData> sale = new ArrayList<>();
            AtomicInteger flag = new AtomicInteger();
            Integer size = sortedLeaderboard.size();
            LOGGER.info(String.format("sortedLeaderboard size is %s",String.valueOf(size)));
            int bucket = size / 10;
            sortedLeaderboard.forEach((salesOfficer, sales) -> {
                LOGGER.info(String.format("getting Sale sortedLeaderboard for customerUID :: %s sale :: %s",salesOfficer ,sales));

                LeaderboardData data = new LeaderboardData();
                SclCustomerModel sclUserModel = (SclCustomerModel) userService.getUserForUID(salesOfficer);
                int s =(int)(sales*100);
                LOGGER.info(String.format("sale is %s",String.valueOf(s)));
                int score=calculateScoreFromPercentile(s);
                LOGGER.info(String.format("score is %s",String.valueOf(score)));
                data.setScore(score);
                data.setCode(sclUserModel.getUid());
                data.setName(sclUserModel.getName());
                data.setSale(sales);
                sale.add(data);
            });

            List<LeaderboardData> premiumSale = new ArrayList<>();
            AtomicInteger flagPre = new AtomicInteger();
            Integer premiumSize = sortedPremiumLeaderboard.size();
            LOGGER.info(String.format("sortedPremiumLeaderboard size is %s",String.valueOf(premiumSize)));
            int premiumBucket = premiumSize / 10;
            sortedPremiumLeaderboard.forEach((salesOfficer, sales) -> {
                LOGGER.info(String.format("getting Sale sortedPremiumLeaderboard for customerUID :: %s sale :: %s",salesOfficer ,String.valueOf(sales)));
                LeaderboardData data = new LeaderboardData();
                SclCustomerModel sclUserModel = (SclCustomerModel) userService.getUserForUID(salesOfficer);
                int s1 =(int)(sales*100);
                LOGGER.info(String.format("sale is %s",String.valueOf(s1)));
                int score=calculateScoreFromPercentile(s1);
                LOGGER.info(String.format("score is %s",String.valueOf(score)));
                data.setScore(score);
                data.setCode(sclUserModel.getUid());
                data.setName(sclUserModel.getName());
                data.setSale(sales);
                premiumSale.add(data);
            });

            int count = sale.size();
            double sumsScore = 0.0;
            Map<String, Double> finalScore = new HashMap<>();

            for (LeaderboardData leader : sale) {
                for (LeaderboardData data : premiumSale) {

                    if (leader.getCode().equalsIgnoreCase(data.getCode())) {
                        sumsScore = leader.getScore() * 0.6 + data.getScore() * 0.4;
                        finalScore.put(leader.getCode(), sumsScore);
                    }


                }
            }

            List<Map.Entry<String, Double>> entri = new ArrayList<>(finalScore.entrySet());

            Collections.sort(entri, Map.Entry.comparingByValue(Comparator.reverseOrder()));

            Map<String, Double> finalScoreboard = new LinkedHashMap<>();

            // Iterate over the sorted entries and put them into the sortedLeaderboard
            for (Map.Entry<String, Double> entry : entri) {
                finalScoreboard.put(entry.getKey(), entry.getValue());
            }

            Map<Double, Integer> scoreToRankMap = new HashMap<>();
            int currentRank = 1; // Initialize currentRank

            for (Map.Entry<String, Double> entry : finalScoreboard.entrySet()) {
                Double sales = entry.getValue();

                if (!scoreToRankMap.containsKey(sales)) {
                    scoreToRankMap.put(sales, currentRank);
                }

                currentRank++;
            }


            List<LeaderboardData> finalLeaderBoard = new ArrayList<>();
            AtomicInteger flagFinal = new AtomicInteger();
            Double prevScore = null;
            Integer finalSize = finalScoreboard.size();
            LOGGER.info(String.format("finalScoreboard size is %s",String.valueOf(finalSize)));
            int finalBucket = finalSize / 10;
            for (Map.Entry<String, Double> entry : finalScoreboard.entrySet()) {
                String salesOfficer = entry.getKey();
                Double score = entry.getValue();
                LOGGER.info(String.format("getting Sale finalscoreboardQTD for customerUID :: %s sale :: %s",salesOfficer ,String.valueOf(score)));
                LeaderboardData data = new LeaderboardData();
                SclCustomerModel sclUserModel = (SclCustomerModel) userService.getUserForUID(salesOfficer);
                int rank = scoreToRankMap.get(score);
                data.setRank(rank);
                data.setScore(score.intValue());
                data.setCode(sclUserModel.getUid());
                data.setName(sclUserModel.getName());
                finalLeaderBoard.add(data);

            }
            list.setSalesList(finalLeaderBoard);
        }

        if(leadType.equalsIgnoreCase(RETAILER)) {

            /*list.setEmpCode(sclUser.getUid());
            list.setEmpName(sclUser.getName());
            List<SclCustomerModel> allSalesOfficersByTaluka = new ArrayList<>();
            List<SclCustomerModel> customerList = new ArrayList<>();

                List<SubAreaMasterModel> subAreaMasterModelList = territoryManagementService.getTerritoriesForCustomer(sclUser);
                if (CollectionUtils.isNotEmpty(subAreaMasterModelList) && subAreaMasterModelList.get(0) != null) {
                    if (subAreaMasterModelList.get(0).getTaluka() != null) {
                        LOG.info("get district:" + subAreaMasterModelList.get(0).getTaluka());
                        String district1 = subAreaMasterModelList.get(0).getTaluka();
                        allSalesOfficersByTaluka = territoryManagementService.getAllSalesOfficersByTaluka(district1);
                        customerList = allSalesOfficersByTaluka.stream().filter(sclCustomerModel -> sclCustomerModel.getGroups().contains(getUserService().getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))).collect(Collectors.toList());
                        LOGGER.info("List of customers:" + allSalesOfficersByTaluka.size());
                        list.setDistrict(district1);
                    }
                }

            Map<String, Double> unsortedLeaderboard = new HashMap<>();
            Map<String, Double> unsortedPremiumSalesLeaderboard = new HashMap<>();
            if (filter.equals(FILTER_MTD)) {
                startDate = getFirstDateOfMonth(monthInNumber);
                endDate = getLastDateOfMonth(monthInNumber);
                populateUnsortedSalesLeaderboardRetailer(currentBaseSite, startDate, endDate, customerList, unsortedLeaderboard);
                populatePremiumUnsortedSalesLeaderboardRetailer(currentBaseSite, startDate, endDate, customerList, unsortedPremiumSalesLeaderboard);
            } else if (filter.equals(FILTER_YTD)) {
                List<Date> dates = salesPerformanceService.getCurrentFinancialYear();
                startDate = dates.get(0);
                endDate = dates.get(1);
                //list.setEmpSale(salesPerformanceService.getSalesByDeliveryDate(subArea, sclUser, currentBaseSite, startDate, endDate));
                List<Date> dates = salesPerformanceService.getCurrentFinancialYear();
                startDate=dates.get(0);
                endDate=dates.get(1);
                //list.setEmpSale(salesPerformanceService.getSalesByDeliveryDate(subArea, sclUser, currentBaseSite, startDate, endDate));
                List<List<Object>> salesLeaderByDate = salesPerformanceService.getSalesLeaderByDate(district, currentBaseSite, startDate, endDate);
                for (List<Object> entry : salesLeaderByDate) {
                    if (entry.size() >= 2) {
                        String key = String.valueOf(entry.get(0));
                        Double value = (Double) entry.get(1);
                        unsortedLeaderboard.put(key, value);
                    }
                }
                List<List<Object>> premiumSalesLeaderByDate = salesPerformanceService.getPremiumSalesLeaderByDate(district, currentBaseSite, startDate, endDate);
                for (List<Object> entry : premiumSalesLeaderByDate) {
                    if (entry.size() >= 2) {
                        String key = String.valueOf(entry.get(0));
                        Double value = (Double) entry.get(1);
                        unsortedPremiumSalesLeaderboard.put(key, value);
                    }
                }
            } else if (filter.equals("QTD")) {
                Calendar calendar = Calendar.getInstance();
                int currentYear = calendar.get(Calendar.YEAR);
                int currentMonth = calendar.get(Calendar.MONTH);

                int currentQuarter = (currentMonth / 3) + 1;
                int startMonth = (currentQuarter - 1) * 3;
                int endMonth = startMonth + 2;

                calendar.set(currentYear, startMonth, 1);
                startDate = calendar.getTime();

                calendar.set(currentYear, endMonth, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                endDate = calendar.getTime();
                populateUnsortedSalesLeaderboardRetailer(currentBaseSite, startDate, endDate, customerList, unsortedLeaderboard);
                populatePremiumUnsortedSalesLeaderboardRetailer(currentBaseSite, startDate, endDate, customerList, unsortedPremiumSalesLeaderboard);
            }
                Map<String, Double> stringDoubleMap = calculatePercentRank(unsortedLeaderboard);
                Map<String, Double> stringDoubleMap1 = calculatePercentRank(unsortedPremiumSalesLeaderboard);

                List<Map.Entry<String, Double>> entries = new ArrayList<>(stringDoubleMap.entrySet());

                Collections.sort(entries, Map.Entry.comparingByValue(Comparator.reverseOrder()));

                Map<String, Double> sortedLeaderboard = new LinkedHashMap<>();

                for (Map.Entry<String, Double> entry : entries) {
                    sortedLeaderboard.put(entry.getKey(), entry.getValue());

                }

                List<Map.Entry<String, Double>> entrie = new ArrayList<>(stringDoubleMap1.entrySet());

                Collections.sort(entrie, Map.Entry.comparingByValue(Comparator.reverseOrder()));

                Map<String, Double> sortedPremiumLeaderboard = new LinkedHashMap<>();

                // Iterate over the sorted entries and put them into the sortedLeaderboard
                for (Map.Entry<String, Double> entry : entrie) {
                    sortedPremiumLeaderboard.put(entry.getKey(), entry.getValue());
                }


                List<LeaderboardData> sale = new ArrayList<>();
                AtomicInteger flag = new AtomicInteger();
                Integer size = sortedLeaderboard.size();
                LOGGER.info(String.format("sortedLeaderboard size is %s",String.valueOf(size)));
                int bucket = size / 10;
                sortedLeaderboard.forEach((salesOfficer, sales) -> {
                    LOGGER.info(String.format("getting Sale sortedLeaderboard for customerUID :: %s sale :: %s",salesOfficer ,sales));

                    LeaderboardData data = new LeaderboardData();
                    SclCustomerModel sclUserModel = (SclCustomerModel) userService.getUserForUID(salesOfficer);
                    double s =Double.valueOf(sales*100);
                    LOGGER.info(String.format("sale is %s",String.valueOf(s)));
                    int score=calculateScoreFromPercentile(s);
                    LOGGER.info(String.format("score is %s",String.valueOf(score)));
                    data.setScore(score);
                    data.setCode(sclUserModel.getUid());
                    data.setName(sclUserModel.getName());
                    data.setSale(sales);
                    sale.add(data);
                });

                List<LeaderboardData> premiumSale = new ArrayList<>();
                AtomicInteger flagPre = new AtomicInteger();
                Integer premiumSize = sortedPremiumLeaderboard.size();
                LOGGER.info(String.format("sortedPremiumLeaderboard size is %s",String.valueOf(premiumSize)));
                int premiumBucket = premiumSize / 10;
                sortedPremiumLeaderboard.forEach((salesOfficer, sales) -> {
                    LOGGER.info(String.format("getting Sale sortedPremiumLeaderboard for customerUID :: %s sale :: %s",salesOfficer ,String.valueOf(sales)));
                    LeaderboardData data = new LeaderboardData();
                    SclCustomerModel sclUserModel = (SclCustomerModel) userService.getUserForUID(salesOfficer);
                *//*flagPre.addAndGet(1);
                data.setRank(flagPre.get());
                int currentBucket = (data.getRank() - 1) / premiumBucket + 1; // Calculate the current bucket based on rank

                // Assign score based on the bucket
                int score = (10 - currentBucket) * 100;
                if (score < 0) {
                    score = 0;
                }*//*

                    double s1 =Double.valueOf(sales*100);
                    LOGGER.info(String.format("sale is %s",String.valueOf(s1)));
                    int score=calculateScoreFromPercentile(s1);
                    LOGGER.info(String.format("score is %s",String.valueOf(score)));
                    data.setScore(score);
                    data.setCode(sclUserModel.getUid());
                    data.setName(sclUserModel.getName());
                    data.setSale(sales);
                    premiumSale.add(data);
                });

                int count = sale.size();
                double sumsScore = 0.0;
                Map<String, Double> finalScore = new HashMap<>();

                for (LeaderboardData leader : sale) {
                    for (LeaderboardData data : premiumSale) {

                        if (leader.getCode().equalsIgnoreCase(data.getCode())) {
                            sumsScore = leader.getScore() * 0.6 + data.getScore() * 0.4;
                            finalScore.put(leader.getCode(), sumsScore);
                        }


                    }
                }

                List<Map.Entry<String, Double>> entri = new ArrayList<>(finalScore.entrySet());

                Collections.sort(entri, Map.Entry.comparingByValue(Comparator.reverseOrder()));

                Map<String, Double> finalScoreboard = new LinkedHashMap<>();

                // Iterate over the sorted entries and put them into the sortedLeaderboard
                for (Map.Entry<String, Double> entry : entri) {
                    finalScoreboard.put(entry.getKey(), entry.getValue());
                }
                List<LeaderboardData> finalLeaderBoard = new ArrayList<>();
                AtomicInteger flagFinal = new AtomicInteger();
                Integer finalSize = finalScoreboard.size();
                LOGGER.info(String.format("finalScoreboard size is %s",String.valueOf(finalSize)));
                int finalBucket = finalSize / 10;
                finalScoreboard.forEach((salesOfficer, score) -> {
                    LOGGER.info(String.format("getting Sale finalscoreboardQTD for customerUID :: %s sale :: %s",salesOfficer ,String.valueOf(score)));
                    LeaderboardData data = new LeaderboardData();
                    SclCustomerModel sclUserModel = (SclCustomerModel) userService.getUserForUID(salesOfficer);
                    flagFinal.addAndGet(1);
                    data.setRank(flagFinal.get());
                    data.setScore(score.intValue());
                    data.setCode(sclUserModel.getUid());
                    data.setName(sclUserModel.getName());
                    finalLeaderBoard.add(data);
                });
                list.setSalesList(finalLeaderBoard);*/
            list.setEmpCode(sclUser.getUid());
            list.setEmpName(sclUser.getName());
            List<SclCustomerModel> allSalesOfficersByState =new ArrayList<>();
            List<SclCustomerModel> customerList = new ArrayList<>();
            DistrictMasterModel district = null;
            SubAreaMasterModel subAreaMasterModel = null;
            List<SubAreaMasterModel> subAreaMasterModelList = territoryManagementService.getTerritoriesForCustomer(sclUser);

            if (CollectionUtils.isNotEmpty(subAreaMasterModelList) && subAreaMasterModelList.get(0) != null) {
                if (subAreaMasterModelList.get(0).getTaluka() != null) {
                    LOG.info("get district:" + subAreaMasterModelList.get(0).getTaluka());
                    String district1 = subAreaMasterModelList.get(0).getTaluka();
                    list.setDistrict(district1);
                }

                subAreaMasterModel = subAreaMasterModelList.get(0);
                LOGGER.info(String.format("subAreaMasterModel %s ::",String.valueOf(subAreaMasterModel)));

            }

            Map<String, Double> unsortedLeaderboard = new HashMap<>();
            Map<String, Double> unsortedPremiumSalesLeaderboard = new HashMap<>();
            Map<String, Double> unsortedCashDiscountAvailedPercentage = new HashMap<>();

            if (filter.equals(FILTER_YTD)) {
                List<Date> dates = salesPerformanceService.getCurrentFinancialYearSales();
                startDate=dates.get(0);
                endDate=dates.get(1);
                //list.setEmpSale(salesPerformanceService.getSalesByDeliveryDate(subArea, sclUser, currentBaseSite, startDate, endDate));
                List<List<Object>> salesLeaderByDate = salesPerformanceService.getSalesLeaderByDateRetailer(subAreaMasterModel, currentBaseSite, startDate, endDate);
                for (List<Object> entry : salesLeaderByDate) {
                    if (entry.size() >= 2) {
                        String key = String.valueOf(entry.get(0));
                        Double value = (Double) entry.get(1);
                        unsortedLeaderboard.put(key, value);
                    }
                }
                List<List<Object>> premiumSalesLeaderByDate = salesPerformanceService.getPremiumSalesLeaderByDateRetailer(subAreaMasterModel, currentBaseSite, startDate, endDate);
                for (List<Object> entry : premiumSalesLeaderByDate) {
                    if (entry.size() >= 2) {
                        String key = String.valueOf(entry.get(0));
                        Double value = (Double) entry.get(1);
                        unsortedPremiumSalesLeaderboard.put(key, value);
                    }
                }

            }
            else if (filter.equals(FILTER_MTD)) {
                startDate = getFirstDateOfMonth(monthInNumber);
                endDate = getLastDateOfMonth(monthInNumber);
                List<List<Object>> salesLeaderByDate = salesPerformanceService.getSalesLeaderByDateRetailer(subAreaMasterModel, currentBaseSite, startDate, endDate);
                for (List<Object> entry : salesLeaderByDate) {
                    if (entry.size() >= 2) {
                        String key = String.valueOf(entry.get(0));
                        Double value = (Double) entry.get(1);
                        unsortedLeaderboard.put(key, value);
                    }
                }
                List<List<Object>> premiumSalesLeaderByDate = salesPerformanceService.getPremiumSalesLeaderByDateRetailer(subAreaMasterModel, currentBaseSite, startDate, endDate);
                for (List<Object> entry : premiumSalesLeaderByDate) {
                    if (entry.size() >= 2) {
                        String key = String.valueOf(entry.get(0));
                        Double value = (Double) entry.get(1);
                        unsortedPremiumSalesLeaderboard.put(key, value);
                    }
                }
            }
            else if (filter.equals("QTD")) {
                Calendar calendar = Calendar.getInstance();
                int currentYear = calendar.get(Calendar.YEAR);
                int currentMonth = calendar.get(Calendar.MONTH);

                int currentQuarter = (currentMonth / 3) + 1;
                int startMonth = (currentQuarter - 1) * 3;
                int endMonth = startMonth + 2;

                calendar.set(currentYear, startMonth, 1);
                startDate = calendar.getTime();

                calendar.set(currentYear, endMonth, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                endDate = calendar.getTime();
                //List<List<Object>> salesLeaderByDate = salesPerformanceService.getSalesLeaderByDate(district, currentBaseSite, startDate, endDate);
                List<List<Object>> salesLeaderByDate = salesPerformanceService.getSalesLeaderByDateRetailer(subAreaMasterModel, currentBaseSite, startDate, endDate);
                for (List<Object> entry : salesLeaderByDate) {
                    if (entry.size() >= 2) {
                        String key = String.valueOf(entry.get(0));
                        Double value = (Double) entry.get(1);
                        unsortedLeaderboard.put(key, value);
                    }
                }
                List<List<Object>> premiumSalesLeaderByDate = salesPerformanceService.getPremiumSalesLeaderByDateRetailer(subAreaMasterModel, currentBaseSite, startDate, endDate);
                for (List<Object> entry : premiumSalesLeaderByDate) {
                    if (entry.size() >= 2) {
                        String key = String.valueOf(entry.get(0));
                        Double value = (Double) entry.get(1);
                        unsortedPremiumSalesLeaderboard.put(key, value);
                    }
                }
            }

            Map<String, Double> stringDoubleMap = calculatePercentRank(unsortedLeaderboard);
            Map<String, Double> stringDoubleMap1 = calculatePercentRank(unsortedPremiumSalesLeaderboard);

            List<Map.Entry<String, Double>> entries = new ArrayList<>(stringDoubleMap.entrySet());

            Collections.sort(entries, Map.Entry.comparingByValue(Comparator.reverseOrder()));

            Map<String, Double> sortedLeaderboard = new LinkedHashMap<>();

            for (Map.Entry<String, Double> entry : entries) {
                sortedLeaderboard.put(entry.getKey(), entry.getValue());

            }

            List<Map.Entry<String, Double>> entrie = new ArrayList<>(stringDoubleMap1.entrySet());

            Collections.sort(entrie, Map.Entry.comparingByValue(Comparator.reverseOrder()));

            Map<String, Double> sortedPremiumLeaderboard = new LinkedHashMap<>();

            // Iterate over the sorted entries and put them into the sortedLeaderboard
            for (Map.Entry<String, Double> entry : entrie) {
                sortedPremiumLeaderboard.put(entry.getKey(), entry.getValue());
            }


            List<LeaderboardData> sale = new ArrayList<>();
            AtomicInteger flag = new AtomicInteger();
            Integer size = sortedLeaderboard.size();
            LOGGER.info(String.format("sortedLeaderboard size is %s",String.valueOf(size)));
            int bucket = size / 10;
            sortedLeaderboard.forEach((salesOfficer, sales) -> {
                LOGGER.info(String.format("getting Sale sortedLeaderboard for customerUID :: %s sale :: %s",salesOfficer ,sales));

                LeaderboardData data = new LeaderboardData();
                SclCustomerModel sclUserModel = (SclCustomerModel) userService.getUserForUID(salesOfficer);
                int s =(int)(sales*100);
                LOGGER.info(String.format("sale is %s",String.valueOf(s)));
                int score=calculateScoreFromPercentile(s);
                LOGGER.info(String.format("score is %s",String.valueOf(score)));
                data.setScore(score);
                data.setCode(sclUserModel.getUid());
                data.setName(sclUserModel.getName());
                data.setSale(sales);
                sale.add(data);
            });

            List<LeaderboardData> premiumSale = new ArrayList<>();
            AtomicInteger flagPre = new AtomicInteger();
            Integer premiumSize = sortedPremiumLeaderboard.size();
            LOGGER.info(String.format("sortedPremiumLeaderboard size is %s",String.valueOf(premiumSize)));
            int premiumBucket = premiumSize / 10;
            sortedPremiumLeaderboard.forEach((salesOfficer, sales) -> {
                LOGGER.info(String.format("getting Sale sortedPremiumLeaderboard for customerUID :: %s sale :: %s",salesOfficer ,String.valueOf(sales)));
                LeaderboardData data = new LeaderboardData();
                SclCustomerModel sclUserModel = (SclCustomerModel) userService.getUserForUID(salesOfficer);
                /*flagPre.addAndGet(1);
                data.setRank(flagPre.get());
                int currentBucket = (data.getRank() - 1) / premiumBucket + 1; // Calculate the current bucket based on rank

                // Assign score based on the bucket
                int score = (10 - currentBucket) * 100;
                if (score < 0) {
                    score = 0;
                }*/

                int s1 =(int)(sales*100);
                LOGGER.info(String.format("sale is %s",String.valueOf(s1)));
                int score=calculateScoreFromPercentile(s1);
                LOGGER.info(String.format("score is %s",String.valueOf(score)));
                data.setScore(score);
                data.setCode(sclUserModel.getUid());
                data.setName(sclUserModel.getName());
                data.setSale(sales);
                premiumSale.add(data);
            });

            int count = sale.size();
            double sumsScore = 0.0;
            Map<String, Double> finalScore = new HashMap<>();

            for (LeaderboardData leader : sale) {
                for (LeaderboardData data : premiumSale) {

                    if (leader.getCode().equalsIgnoreCase(data.getCode())) {
                        sumsScore = leader.getScore() * 0.6 + data.getScore() * 0.4;
                        finalScore.put(leader.getCode(), sumsScore);
                    }


                }
            }

            List<Map.Entry<String, Double>> entri = new ArrayList<>(finalScore.entrySet());

            Collections.sort(entri, Map.Entry.comparingByValue(Comparator.reverseOrder()));

            Map<String, Double> finalScoreboard = new LinkedHashMap<>();

            // Iterate over the sorted entries and put them into the sortedLeaderboard
            for (Map.Entry<String, Double> entry : entri) {
                finalScoreboard.put(entry.getKey(), entry.getValue());
            }

            Map<Double, Integer> scoreToRankMap = new HashMap<>();
            int currentRank = 1; // Initialize currentRank

            for (Map.Entry<String, Double> entry : finalScoreboard.entrySet()) {
                Double sales = entry.getValue();

                if (!scoreToRankMap.containsKey(sales)) {
                    scoreToRankMap.put(sales, currentRank);
                }

                currentRank++;
            }
            List<LeaderboardData> finalLeaderBoard = new ArrayList<>();
            AtomicInteger flagFinal = new AtomicInteger();
            Double prevScore = null;
            Integer finalSize = finalScoreboard.size();
            LOGGER.info(String.format("finalScoreboard size is %s",String.valueOf(finalSize)));
            int finalBucket = finalSize / 10;
            for (Map.Entry<String, Double> entry : finalScoreboard.entrySet()) {
                String salesOfficer = entry.getKey();
                Double score = entry.getValue();
                LOGGER.info(String.format("getting Sale finalscoreboardQTD for customerUID :: %s sale :: %s",salesOfficer ,String.valueOf(score)));
                LeaderboardData data = new LeaderboardData();
                SclCustomerModel sclUserModel = (SclCustomerModel) userService.getUserForUID(salesOfficer);
                int rank = scoreToRankMap.get(score);
                data.setRank(rank);
                data.setScore(score.intValue());
                data.setCode(sclUserModel.getUid());
                data.setName(sclUserModel.getName());
                finalLeaderBoard.add(data);

            }
            list.setSalesList(finalLeaderBoard);

        }
        return list;
    }

  /*  private void populateCashDiscountAvailedPercentage(BaseSiteModel currentBaseSite, Date startDate, Date endDate, List<SclCustomerModel> customerList, Map<String, Double> unsortedCashDiscountAvailedPercentage) {
        if(!customerList.isEmpty() && customerList != null) {
            customerList.stream().forEach(so -> {
                if(so!=null){
                    Double salesLeaderByDeliveryDate = salesPerformanceService.getCashDiscountAvailedPercentage(so, currentBaseSite, startDate, endDate);
                    LOGGER.info(String.format("getting Sale Quantity for customerUID for premium sales Reatiler:: %s sale :: %s", so.getUid(),salesLeaderByDeliveryDate));

                    //  unsortedLeaderboard.put(so.getUid(), salesPerformanceService.getSalesByDeliveryDate(subArea, so, currentBaseSite, startDate, endDate));
                    unsortedCashDiscountAvailedPercentage.put(so.getUid(), salesPerformanceService.getCashDiscountAvailedPercentage(so, currentBaseSite, startDate, endDate));
                }
            });
        }
    }*/

    private void populatePremiumUnsortedSalesLeaderboardRetailer(BaseSiteModel currentBaseSite, Date startDate, Date endDate, List<SclCustomerModel> customerList, Map<String, Double> unsortedPremiumSalesLeaderboard) {
        if(!customerList.isEmpty() && customerList != null) {
            customerList.stream().forEach(so -> {
                if(so!=null){
                    Double salesLeaderByDeliveryDate = salesPerformanceService.getPremiumSalesLeaderByDeliveryDateRetailer(so, currentBaseSite, startDate, endDate);
                    LOGGER.info(String.format("getting Sale Quantity for customerUID for premium sales Reatiler:: %s sale :: %s", so.getUid(),salesLeaderByDeliveryDate));

                    //  unsortedLeaderboard.put(so.getUid(), salesPerformanceService.getSalesByDeliveryDate(subArea, so, currentBaseSite, startDate, endDate));
                    unsortedPremiumSalesLeaderboard.put(so.getUid(), salesPerformanceService.getPremiumSalesLeaderByDeliveryDateRetailer(so, currentBaseSite, startDate, endDate));
                }
            });
        }
    }

    @Override
    public SalesAndAchievementData getTotalActualAndTargetSaleForCustomer(String filter, int year, int month, String bgpFilter, String counterType,String retailerId) {
     /*   B2BCustomerModel customerModel=(B2BCustomerModel) getUserService().getCurrentUser();
        SclCustomerModel currentUser = (SclCustomerModel) getUserService().getUserForUID(customerModel.getUid());*/
        SclCustomerModel currentUser = (SclCustomerModel) getUserService().getCurrentUser();
        if(StringUtils.isNotBlank(retailerId)){
            currentUser=(SclCustomerModel) userService.getUserForUID(retailerId);
        }
        BaseSiteModel currentBaseSite=baseSiteService.getCurrentBaseSite();

        SalesAndAchievementData data=new SalesAndAchievementData();
        double actualTarget = 0.0, totalTarget=0.0, behindTarget=0.0, aheadTarget=0.0;
        double achievementPercentage=0.0;

        if(StringUtils.isBlank(filter))
        {
            if(year!=0 && month!=0)
            {
                if(currentUser.getCounterType().equals(CounterType.SP)){
                    RequestCustomerData requestData = new RequestCustomerData();
                    List<String> counterTypes = List.of(DEALER);
                    requestData.setCounterType(counterTypes);
                    List<SclCustomerModel> customerforUser = territoryManagementService.getCustomerforUser(requestData);
                    if(customerforUser!=null && !customerforUser.isEmpty()) {
                        for (SclCustomerModel customer : customerforUser) {
                            LOGGER.info(String.format("Scl customer Model PK:%s", customer));
                        }
                    }
                    if(customerforUser!=null && !customerforUser.isEmpty()) {
                        for(SclCustomerModel customer: customerforUser){
                            actualTarget = sclSalesSummaryService.getSalesByMonth(customer, month, year, Collections.emptyList());
                        }
                      //  actualTarget = getSalesPerformanceService().getActualTargetForSalesForMonthSP(customerforUser, year, month);
                        data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);
                        totalTarget = getSalesPerformanceService().getSalesTargetForMonthSP(customerforUser, year, month);
                        data.setTotalTarget(totalTarget != 0.0 ? totalTarget : 0.0);
                    }
                    else{
                        data.setActualTarget(0.0);
                        data.setTotalTarget(0.0);
                    }
                }
                else if(currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                    //actualTarget = getSalesPerformanceService().getActualTargetForSalesForMonthDealer(currentUser, currentBaseSite, year, month, bgpFilter);
                    actualTarget = sclSalesSummaryService.getSalesByMonth(currentUser,month,year,bgpFilter);
                    data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);

                    totalTarget = getSalesPerformanceService().getSalesTargetForMonthDealer(currentUser, currentBaseSite, year, month,bgpFilter);
                    data.setTotalTarget(totalTarget != 0.0 ? totalTarget : 0.0);
                }
                else if(currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                    Map<String,Double> salesQty=new HashMap<>();
                    // actualTarget = getSalesPerformanceService().getActualTargetForSalesRetailerMTD(currentUser, currentBaseSite,bgpFilter);
                    salesQty=networkService.getSalesQuantityForRetailerByMonthYear(currentUser,month,year,null,bgpFilter);
                    //actualTarget=salesQty.get("quantityInBags");
                    /*if(retailerId!=null){
                        if((userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
                            List<SclCustomerModel> sclDealer=new ArrayList<>();
                            sclDealer.add(currentUser);
                            salesQty=networkService.getSalesQuantityForRetailerByMonthYear(currentUser,month,year,sclDealer,bgpFilter);
                            actualTarget = salesQty.get("quantityInMT");
                        }else if(userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                            actualTarget=salesQty.get("quantityInBags");
                        }else{
                            actualTarget=salesQty.get("quantityInMT");
                        }
                    }*/
                    if(retailerId!=null && retailerId.equalsIgnoreCase(userService.getCurrentUser().getUid())) {
                        actualTarget = salesQty.get("quantityInBags");
                    }else if(retailerId!=null) {
                        if ((userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
                            List<SclCustomerModel> sclDealer = new ArrayList<>();
                            sclDealer.add((SclCustomerModel) userService.getCurrentUser());
                            salesQty = networkService.getSalesQuantityForRetailerByMonthYear(currentUser,month,year,sclDealer,bgpFilter);
                            actualTarget = salesQty.get("quantityInMT");
                        }
                    }else {
                        actualTarget = salesQty.get("quantityInMT");
                    }
                    //actualTarget = getSalesPerformanceService().getActualTargetForSalesForMonthRetailer(currentUser, currentBaseSite, year, month,bgpFilter);
                    data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);

                    totalTarget = getSalesPerformanceService().getSalesTargetForMonthRetailer(currentUser.getUid(), currentBaseSite, year, month,bgpFilter);
                    data.setTotalTarget(totalTarget != 0.0 ? totalTarget : 0.0);
                }

                if(actualTarget!=0.0 && totalTarget!=0.0)
                    achievementPercentage=(actualTarget/totalTarget)*100;
                data.setAchievementPercentage(achievementPercentage!=0.0 ?achievementPercentage :0.0);

                if(achievementPercentage < 100)
                {
                    behindTarget=totalTarget-actualTarget;
                    data.setBehindTotalTarget(behindTarget!=0.0 ? behindTarget :0.0);
                }
                else if(achievementPercentage > 100)
                {
                    aheadTarget=actualTarget-totalTarget;
                    data.setAheadTotalTarget(aheadTarget!=0.0?aheadTarget:0.0);
                }
            }
            else
            {
                if(currentUser.getCounterType().equals(CounterType.SP)){
                    RequestCustomerData requestData = new RequestCustomerData();
                    List<String> counterTypes = List.of(DEALER);
                    requestData.setCounterType(counterTypes);
                    List<SclCustomerModel> customerforUser = territoryManagementService.getCustomerforUser(requestData);
                    if(customerforUser!=null && !customerforUser.isEmpty()) {
                        for (SclCustomerModel customer : customerforUser) {
                            LOGGER.info(String.format("Scl customer Model PK:%s", customer));
                        }
                    }
                    if(customerforUser!=null && !customerforUser.isEmpty()) {
                        for(SclCustomerModel customer: customerforUser){
                            actualTarget = sclSalesSummaryService.getCurrentMonthSales(customer, Collections.emptyList());
                        }
                      //  actualTarget = getSalesPerformanceService().getActualTargetForSalesSPMTD(customerforUser);
                        data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);
                        totalTarget = getSalesPerformanceService().getMonthlySalesTargetForSP(customerforUser);
                        data.setTotalTarget(totalTarget != 0.0 ? totalTarget : 0.0);
                    }
                    else{
                        data.setActualTarget(0.0);
                        data.setTotalTarget(0.0);
                    }
                }
                else if(currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                   // actualTarget = getSalesPerformanceService().getActualTargetForSalesDealerMTD(currentUser, currentBaseSite,bgpFilter);
                    actualTarget = sclSalesSummaryService.getCurrentMonthSales(currentUser,bgpFilter);
                    data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);

                    totalTarget = getSalesPerformanceService().getMonthlySalesTargetForDealer(currentUser, currentBaseSite,bgpFilter);
                    data.setTotalTarget(totalTarget != 0.0 ? totalTarget : 0.0);
                }
                if(currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                    Map<String,Double> salesQty=new HashMap<>();
                    salesQty=networkService.getSalesQuantityForRetailerByMTD(currentUser,null,bgpFilter);
                    // actualTarget = getSalesPerformanceService().getActualTargetForSalesRetailerMTD(currentUser, currentBaseSite,bgpFilter);
                   /* if(retailerId!=null){
                         if((userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
                            List<SclCustomerModel> sclDealer=new ArrayList<>();
                            sclDealer.add(currentUser);
                            salesQty=networkService.getSalesQuantityForRetailerByMTD(currentUser,sclDealer,bgpFilter);
                            actualTarget = salesQty.get("quantityInMT");
                        }else if(userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                            actualTarget=salesQty.get("quantityInBags");
                        }else{
                             actualTarget=salesQty.get("quantityInMT");
                         }
                    }*/
                    if(retailerId!=null && retailerId.equalsIgnoreCase(userService.getCurrentUser().getUid())) {
                        actualTarget = salesQty.get("quantityInBags");
                    }else if(retailerId!=null) {
                        if ((userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
                            List<SclCustomerModel> sclDealer = new ArrayList<>();
                            sclDealer.add((SclCustomerModel) userService.getCurrentUser());
                            salesQty = networkService.getSalesQuantityForRetailerByMTD(currentUser, sclDealer, bgpFilter);
                            actualTarget = salesQty.get("quantityInMT");
                        }
                    }else {
                        actualTarget = salesQty.get("quantityInMT");
                    }
                   // actualTarget=salesQty.get("quantityInBags");
                    data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);

                    totalTarget = getSalesPerformanceService().getMonthlySalesTargetForRetailer(currentUser.getUid(), currentBaseSite,bgpFilter);
                    data.setTotalTarget(totalTarget != 0.0 ? totalTarget : 0.0);
                }


                if(actualTarget!=0.0 && totalTarget!=0.0)
                    achievementPercentage=(actualTarget/totalTarget)*100;
                data.setAchievementPercentage(achievementPercentage!=0.0 ?achievementPercentage :0.0);

                if(achievementPercentage < 100)
                {
                    behindTarget=totalTarget-actualTarget;
                    data.setBehindTotalTarget(behindTarget!=0.0 ? behindTarget :0.0);
                }
                else if(achievementPercentage > 100)
                {
                    aheadTarget=actualTarget-totalTarget;
                    data.setAheadTotalTarget(aheadTarget!=0.0?aheadTarget:0.0);
                }
            }

        }
        else {
            if (filter.contains(FILTER_MTD)) {
                if (currentUser.getCounterType().equals(CounterType.SP)) {
                    RequestCustomerData requestData = new RequestCustomerData();
                    List<String> counterTypes = List.of(DEALER);
                    requestData.setCounterType(counterTypes);
                    List<SclCustomerModel> customerforUser = territoryManagementService.getCustomerforUser(requestData);
                    if (customerforUser != null && !customerforUser.isEmpty()) {
                        for (SclCustomerModel customer : customerforUser) {
                            LOGGER.info(String.format("Scl customer Model PK:%s", customer));
                        }
                    }
                    if (customerforUser != null && !customerforUser.isEmpty()) {
                        for(SclCustomerModel customer: customerforUser){
                            actualTarget =sclSalesSummaryService.getCurrentMonthSales(customer, Collections.emptyList());
                        }
                        //actualTarget = getSalesPerformanceService().getActualTargetForSalesSPMTD(customerforUser);
                        data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);
                        totalTarget = getSalesPerformanceService().getMonthlySalesTargetForSP(customerforUser);
                        data.setTotalTarget(totalTarget != 0.0 ? totalTarget : 0.0);
                    } else {
                        data.setActualTarget(0.0);
                        data.setTotalTarget(0.0);
                    }
                } else if (currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                   // actualTarget = getSalesPerformanceService().getActualTargetForSalesDealerMTD(currentUser, currentBaseSite, bgpFilter);
                    actualTarget = sclSalesSummaryService.getCurrentMonthSales(currentUser,bgpFilter);
                    data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);

                    totalTarget = getSalesPerformanceService().getMonthlySalesTargetForDealer(currentUser, currentBaseSite, bgpFilter);
                    data.setTotalTarget(totalTarget != 0.0 ? totalTarget : 0.0);
                }
                if (currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                  /*  if(retailerId!=null)
                    {
                        actualTarget = getSalesPerformanceService().getActualTargetForSalesMTDRetailerList(currentUser, currentBaseSite);
                        data.setActualTarget(actualTarget != 0.0 ? (actualTarget / 20) : 0.0);

                        totalTarget = getSalesPerformanceService().getMonthlySalesTargetForRetailer(currentUser.getUid(), currentBaseSite, bgpFilter);
                        data.setTotalTarget(totalTarget != 0.0 ? (totalTarget/20) : 0.0);
                    }
                    else
                    {*/
                       // actualTarget = getSalesPerformanceService().getActualTargetForSalesRetailerMTD(currentUser, currentBaseSite, bgpFilter);
                    Map<String,Double> salesQty=new HashMap<>();
                    salesQty=networkService.getSalesQuantityForRetailerByMTD(currentUser,null,bgpFilter);
                   /* if(retailerId!=null){
                        if((userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
                            List<SclCustomerModel> sclDealer=new ArrayList<>();
                            sclDealer.add(currentUser);
                            salesQty=networkService.getSalesQuantityForRetailerByMTD(currentUser,sclDealer,bgpFilter);
                            actualTarget = salesQty.get("quantityInMT");
                        }else if(userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                            actualTarget=salesQty.get("quantityInBags");
                        }else{
                            actualTarget=salesQty.get("quantityInMT");
                        }
                    }else{
                        if(userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                            actualTarget = salesQty.get("quantityInBags");
                        }else{
                            actualTarget = salesQty.get("quantityInMT");
                        }
                    }*/
                    //actualTarget=salesQty.get("quantityInBags");
                    if(retailerId!=null && retailerId.equalsIgnoreCase(userService.getCurrentUser().getUid())) {
                        actualTarget = salesQty.get("quantityInBags");
                    }else if(retailerId!=null) {
                        if ((userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
                            List<SclCustomerModel> sclDealer = new ArrayList<>();
                            sclDealer.add((SclCustomerModel) userService.getCurrentUser());
                            salesQty = networkService.getSalesQuantityForRetailerByMTD(currentUser, sclDealer, bgpFilter);
                            actualTarget = salesQty.get("quantityInMT");
                        }
                    }else {
                        actualTarget = salesQty.get("quantityInMT");
                    }
                    data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);

                        totalTarget = getSalesPerformanceService().getMonthlySalesTargetForRetailer(currentUser.getUid(), currentBaseSite, bgpFilter);
                        data.setTotalTarget(totalTarget != 0.0 ? totalTarget : 0.0);
                    //}
                }

                if (actualTarget != 0.0 && totalTarget != 0.0)
                    achievementPercentage = (actualTarget / totalTarget) * 100;
                data.setAchievementPercentage(achievementPercentage != 0.0 ? achievementPercentage : 0.0);

                if (achievementPercentage < 100) {
                    behindTarget = totalTarget - actualTarget;
                    data.setBehindTotalTarget(behindTarget != 0.0 ? behindTarget : 0.0);
                } else if (achievementPercentage > 100) {
                    aheadTarget=actualTarget-totalTarget;
                    data.setAheadTotalTarget(aheadTarget != 0.0 ? aheadTarget : 0.0);
                }
            } else if (filter.contains(FILTER_YTD)) {

                if (currentUser.getCounterType().equals(CounterType.SP)) {
                    RequestCustomerData requestData = new RequestCustomerData();
                    List<String> counterTypes = List.of(DEALER);
                    requestData.setCounterType(counterTypes);
                    List<SclCustomerModel> customerforUser = territoryManagementService.getCustomerforUser(requestData);
                    if (customerforUser != null && !customerforUser.isEmpty()) {
                        for (SclCustomerModel customer : customerforUser) {
                            LOGGER.info(String.format("Scl customer Model PK:%s", customer));
                        }
                    }
                    if (customerforUser != null && !customerforUser.isEmpty()) {
                        for(SclCustomerModel customer: customerforUser){
                            actualTarget = sclSalesSummaryService.getCurrentFySales(customer,Collections.emptyList());
                        }
                      //  actualTarget = getSalesPerformanceService().getActualTargetForSalesYTDSP(customerforUser, currentBaseSite);
                        data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);
                        totalTarget = getSalesPerformanceService().getAnnualSalesTargetForSP(customerforUser);
                        data.setTotalTarget(totalTarget != 0.0 ? totalTarget : 0.0);
                    } else {
                        data.setActualTarget(0.0);
                        data.setTotalTarget(0.0);
                    }
                } else if (currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                    //actualTarget = getSalesPerformanceService().getActualTargetForSalesYTDDealer(currentUser, currentBaseSite, bgpFilter);
                    actualTarget = sclSalesSummaryService.getCurrentFySales(currentUser,bgpFilter);
                    data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);

                    totalTarget = getSalesPerformanceService().getAnnualSalesTargetForDealer(currentUser, bgpFilter);
                    data.setTotalTarget(totalTarget != 0.0 ? totalTarget : 0.0);
                }
                if (currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                   /* if(retailerId!=null)
                    {
                        actualTarget = getSalesPerformanceService().getActualTargetForSalesYTDRetailerList(currentUser, currentBaseSite);
                        data.setActualTarget(actualTarget != 0.0 ? (actualTarget / 20) : 0.0);

                        totalTarget = getSalesPerformanceService().getAnnualSalesTargetForRetailer(currentUser, bgpFilter);
                        data.setTotalTarget(totalTarget != 0.0 ? (totalTarget / 20) : 0.0);
                    }
                    else {*/

                    Map<String,Double> salesQty=new HashMap<>();
                    salesQty=networkService.getSalesQuantityForRetailerByYTD(currentUser,null,bgpFilter);
                    /*if(retailerId!=null){
                        if((userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
                            List<SclCustomerModel> sclDealer=new ArrayList<>();
                            sclDealer.add(currentUser);
                            salesQty=networkService.getSalesQuantityForRetailerByYTD(currentUser,sclDealer,bgpFilter);
                            actualTarget = salesQty.get("quantityInMT");
                        }else if(userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                            actualTarget=salesQty.get("quantityInBags");
                        }else{
                            actualTarget=salesQty.get("quantityInMT");
                        }
                    }else{
                        if(userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                            actualTarget = salesQty.get("quantityInBags");
                        }else{
                            actualTarget = salesQty.get("quantityInMT");
                        }
                    }*/
                    if(retailerId!=null && retailerId.equalsIgnoreCase(userService.getCurrentUser().getUid())) {
                        actualTarget = salesQty.get("quantityInBags");
                    }else if(retailerId!=null) {
                        if ((userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
                            List<SclCustomerModel> sclDealer = new ArrayList<>();
                            sclDealer.add((SclCustomerModel) userService.getCurrentUser());
                            salesQty = networkService.getSalesQuantityForRetailerByYTD(currentUser, sclDealer, bgpFilter);
                            actualTarget = salesQty.get("quantityInMT");
                        }
                    }else {
                        actualTarget = salesQty.get("quantityInMT");
                    }
                    //actualTarget=salesQty.get("quantityInBags");
                        //actualTarget = getSalesPerformanceService().getActualTargetForSalesYTRetailer(currentUser, currentBaseSite, bgpFilter);
                        data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);

                        totalTarget = getSalesPerformanceService().getAnnualSalesTargetForRetailer(currentUser, bgpFilter);
                        data.setTotalTarget(totalTarget != 0.0 ? totalTarget : 0.0);
                    //}
                }

                if (actualTarget != 0.0 && totalTarget != 0.0)
                    achievementPercentage = (actualTarget / totalTarget) * 100;
                data.setAchievementPercentage(achievementPercentage != 0.0 ? achievementPercentage : 0.0);

                if (achievementPercentage < 100) {
                    behindTarget = totalTarget - actualTarget;
                    data.setBehindTotalTarget(behindTarget != 0.0 ? behindTarget : 0.0);
                } else if (achievementPercentage > 100) {
                    aheadTarget=actualTarget-totalTarget;
                    data.setAheadTotalTarget(aheadTarget != 0.0 ? aheadTarget : 0.0);
                }
            }
        }

        return data;
    }

    @Override
    public MonthlySalesListData getActualVsTargetSalesGraph(String filter, String counterType, String bgpFilter) {
        //B2BCustomerModel  currentUser = (B2BCustomerModel) getUserService().getCurrentUser();
        SclCustomerModel sclCustomer = (SclCustomerModel) getUserService().getCurrentUser();
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        MonthlySalesListData monthlySalesListData = new MonthlySalesListData();
        List<MonthlySalesData> dataList = new ArrayList<>();
        double targetSale =0.0;
        double currentMonthSale=0.0;
        double lastMonthSale = 0.0;
        double currentMonthTarget=0.0;
        double currentSales = 0.0;
        double lastYearSales = 0.0;
        double growth=0.0;
        double lastMonthTarget=0.0;

        LocalDate currentMonth = LocalDate.now();
        LocalDate currentMonthForTarget = LocalDate.now();
        double actualSales =0.0,targetSales=0.0,growth1=0.0;
        String monthYear ="", monthName="", yearName="";

        if(sclCustomer.getCounterType().equals(CounterType.SP)) {
            List<DealerRevisedMonthlySalesModel> dealerRevisedMonthlySalesModel = new ArrayList<>();
            RequestCustomerData requestData = new RequestCustomerData();
            List<String> counterTypes = List.of(DEALER);
            requestData.setCounterType(counterTypes);
            List<SclCustomerModel> customerforUser = territoryManagementService.getCustomerforUser(requestData);
            LOGGER.info("Size of customer user :%s" + customerforUser.size());
            if (filter.equalsIgnoreCase("10DayBucket")) {
                //actual sales
                List<Double> actualBucketSaleList = new ArrayList<>();
                double actualSale = 0.0;
                LocalDate startDate = null, endDate = null, startDateTarget = null;
                startDate = LocalDate.of(currentMonth.getYear(), currentMonth.getMonthValue() - 1, 1);
                endDate = startDate.plusDays(9);
                for (int i = 1; i <= 6; i++) {
                    //1-10, 11- 20 and 21-31
                    LOG.info("10DayBucket :" + startDate + " " + endDate);
                    Date startDate1 = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    Date endDate1 = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    actualSale = salesPerformanceService.getActualTargetFor10DayBucketForSP(customerforUser, startDate1, endDate1);
                    startDate = startDate1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    endDate = endDate1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    startDate = endDate.plusDays(1);
                    if(i%3==0) {
                        if(startDate.getMonth().name().equalsIgnoreCase("FEBRUARY"))
                        {
                            if (((startDate.getYear() % 4 == 0) && (startDate.getYear() % 100!= 0)) || (startDate.getYear()%400 == 0)){
                                endDate = startDate.plusDays(9);//leap year
                            }
                            else{
                                endDate = startDate.plusDays(8);//not a leap year
                            }
                        }
                        else if(startDate.getMonth().name().equalsIgnoreCase("JANUARY") || startDate.getMonth().name().equalsIgnoreCase("MARCH") ||
                                startDate.getMonth().name().equalsIgnoreCase("MAY") || startDate.getMonth().name().equalsIgnoreCase("JULY") ||
                                startDate.getMonth().name().equalsIgnoreCase("AUGUST") || startDate.getMonth().name().equalsIgnoreCase("OCTOBER") ||
                                startDate.getMonth().name().equalsIgnoreCase("DECEMBER")) {
                            endDate = startDate.plusDays(10);
                        }
                        else if(startDate.getMonth().name().equalsIgnoreCase("APRIL") || startDate.getMonth().name().equalsIgnoreCase("JUNE") ||
                                startDate.getMonth().name().equalsIgnoreCase("SEPTEMBER") || startDate.getMonth().name().equalsIgnoreCase("NOVEMBER")) {
                            endDate = startDate.plusDays(9);
                        }
                    }
                    else {
                        endDate = startDate.plusDays(9);//20
                    }
                    LOGGER.info(String.format("Actual Sales:%s",actualSale));
                    actualBucketSaleList.add(actualSale != 0.0 ? actualSale : 0.0);
                }
                LOGGER.info((String.format("Actual Sales list:%s",String.valueOf(actualBucketSaleList.size()))));

                //target sales
                startDateTarget = LocalDate.of(currentMonth.getYear(), currentMonth.getMonthValue() - 1, 1);
                Date startDate1Target = Date.from(startDateTarget.atStartOfDay(ZoneId.systemDefault()).toInstant());
                monthName = getMonth(startDate1Target);
                yearName = getYear(startDate1Target);
                List<Double> targetBucketSaleList = new ArrayList<>();

                double revisedTarget=0.0,sumRevisedTarget=0.0;
                for (int i = 0; i <= 1; i++) {

                    dealerRevisedMonthlySalesModel = salesPerformanceService.getMonthlySaleTargetGraphForSP(customerforUser, monthName, yearName);

                        /*if (dealerRevisedMonthlySalesModel != null) {
                            targetBucketSaleList.add(dealerRevisedMonthlySalesModel.getBucket1() != 0.0 ? dealerRevisedMonthlySalesModel.getBucket1() : 0.0);
                            targetBucketSaleList.add(dealerRevisedMonthlySalesModel.getBucket2() != 0.0 ? dealerRevisedMonthlySalesModel.getBucket2() : 0.0);
                            targetBucketSaleList.add(dealerRevisedMonthlySalesModel.getBucket3() != 0.0 ? dealerRevisedMonthlySalesModel.getBucket3() : 0.0);
                        }*/
                    Double bucket1=0.0;
                    Double bucket2=0.0;
                    Double bucket3=0.0;

                    if (dealerRevisedMonthlySalesModel != null) {
                        for (DealerRevisedMonthlySalesModel revisedMonthlySalesModel : dealerRevisedMonthlySalesModel) {
                            if (revisedMonthlySalesModel.getBucket1() != null) {
                                bucket1 +=revisedMonthlySalesModel.getBucket1();
                                targetBucketSaleList.add(bucket1 != 0.0 ? bucket1 : 0.0);

                            }
                            else {
                                targetBucketSaleList.add(0.0);
                            }
                            if (revisedMonthlySalesModel.getBucket2() != null) {
                                bucket2 +=revisedMonthlySalesModel.getBucket2();
                                targetBucketSaleList.add(bucket2 != 0.0 ? bucket2 : 0.0);
                            }
                            else {
                                targetBucketSaleList.add(0.0);
                            }
                            if (revisedMonthlySalesModel.getBucket3() != null) {
                                bucket2 +=revisedMonthlySalesModel.getBucket3();
                                targetBucketSaleList.add(bucket3 != 0.0 ? bucket3 : 0.0);
                            }
                            else {
                                targetBucketSaleList.add(0.0);
                            }
                        }
                    }
                    else{
                        targetBucketSaleList.add(0.0);
                        targetBucketSaleList.add(0.0);
                        targetBucketSaleList.add(0.0);
                    }

                    LOGGER.info((String.format("Target Bucket list Size:%s",String.valueOf(targetBucketSaleList.size()))));
                    startDateTarget = LocalDate.of(startDateTarget.getYear(), startDateTarget.getMonthValue() + 1, 1);
                    startDate1Target = Date.from(startDateTarget.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    monthName = getMonth(startDate1Target);
                    yearName = getYear(startDate1Target);
                }

                List<String> monthYearList = new ArrayList<>();
                int j = 1,k=1;
                for (int i = 1; i <= 6; i++) {
                    //1-10, 11- 20 and 21-31
                    MonthlySalesData data = new MonthlySalesData();
                    String monthYearr = currentMonth.getMonth().minus(1).getDisplayName(TextStyle.SHORT, Locale.ENGLISH).concat("b").concat(String.valueOf(k++));
                    if (i > 3) {
                        monthYearr = currentMonth.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).concat("b").concat(String.valueOf(j++));
                    }

                    monthYearList.add(monthYearr);
                }

                for (int i = 0; i <= 5; i++) {
                    MonthlySalesData data = new MonthlySalesData();

                    if(actualBucketSaleList!=null && !actualBucketSaleList.isEmpty())
                        actualSales = actualBucketSaleList.get(i);
                    if(targetBucketSaleList!=null && !targetBucketSaleList.isEmpty())
                        targetSales = targetBucketSaleList.get(i);
                    if(monthYearList!=null && !monthYearList.isEmpty())
                        monthYear= monthYearList.get(i);

                    data.setActualSales(actualSales);
                    data.setTargetSales(targetSales);
                    data.setMonthYear(monthYear);
                    dataList.add(data);
                }
            }
            else if (filter.equalsIgnoreCase(FILTER_MTD)) {
                for (int i = 1; i <= 6; i++) {
                    MonthlySalesData data = new MonthlySalesData();
                    currentMonthSale = salesPerformanceService.getActualTargetForSalesForMtdSp(customerforUser, currentBaseSite, currentMonth.getYear(), currentMonth.getMonthValue());
                    LOGGER.info(String.format("currentMonthSale :%s",currentMonthSale));

                    LocalDate lastMonth = currentMonth.minusMonths(1);
                    lastMonthSale = salesPerformanceService.getActualTargetForSalesForMtdSp(customerforUser, currentBaseSite, lastMonth.getYear(), lastMonth.getMonthValue());
                    data.setActualSales(currentMonthSale);
                    //double growth = currentMonthSale - lastMonthSale;
                    //data.setGrowth(growth);

                    Date date = Date.from(currentMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    var monthYear1 = SclDateUtility.getFormattedDate(date, "MMM-YYYY");
                    currentMonthTarget = salesPerformanceService.getSalesTargetForMtdSP(customerforUser, currentBaseSite, currentMonth.getYear(), currentMonth.getMonthValue());
                    lastMonthTarget = salesPerformanceService.getSalesTargetForMtdSP(customerforUser, currentBaseSite, lastMonth.getYear(), lastMonth.getMonthValue());
                    LOGGER.info(String.format("currentMonthTarget :%s",currentMonthTarget));
                    data.setTargetSales(currentMonthTarget);

                    if (Objects.nonNull(monthYear1)) {
                        data.setMonthYear(monthYear1.replace("-", " "));
                    }
                    dataList.add(data);
                    currentMonth = lastMonth;
                }
            } else if (filter.equalsIgnoreCase(FILTER_YTD)) {
                List<MonthlySalesData> ytdDataListDealer = new ArrayList<>();
                Date startDate1 = null, endDate1 = null, startDate2 = null, endDate2 = null;

                LocalDate currentDate = LocalDate.now();
                LocalDate startDate, endDate;
                LocalDate startDateMonthYear = LocalDate.now();
                List<Double> yearWiseSales = new ArrayList<>();

                startDate = LocalDate.of(currentDate.getYear(), Month.APRIL, 1);
                endDate = LocalDate.of(currentDate.getYear()+1, Month.APRIL, 1);
                for (int i = 0; i <= 2; i++) {
                    startDate1 = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    endDate1 = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

                    currentSales = salesPerformanceService.getActualSaleForDealerGraphYTDSP(customerforUser, startDate1, endDate1, currentBaseSite);

                    LOGGER.info(String.format("currentSales YTD  :%s",currentSales));
                    startDate = startDate1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    endDate = endDate1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                    startDate = LocalDate.of(startDate.getYear() - 1, Month.APRIL, 1);
                    endDate = LocalDate.of(endDate.getYear() - 1, Month.APRIL, 1);

                    yearWiseSales.add(currentSales);
                }



                for (int i = 0; i <= 2; i++) {
                    MonthlySalesData data = new MonthlySalesData();

                    data.setMonthYear(String.valueOf(startDateMonthYear.getYear() - i));
                    double currentYrSales = yearWiseSales.get(i);
                    data.setActualSales(currentYrSales);
                    //target sales to be done

                    data.setTargetSales(0.0);
                    dataList.add(data);
                }
            }
        }


        else if(sclCustomer.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
            if (filter.equalsIgnoreCase("10DayBucket")) {
                //actual sales
                List<Double> actualBucketSaleList = new ArrayList<>();
                List<Double> GrowthBucketSaleList = new ArrayList<>();
                double actualSale = 0.0;
                double lastBucketSale = 0.0;
                LocalDate startDate = null, endDate = null, startDateTarget = null;
                LocalDate lastMonthDate=currentMonth.minusMonths(1);
                startDate = LocalDate.of(currentMonth.getYear(), lastMonthDate.getMonthValue(), 1);
                endDate = startDate.plusDays(9);
                Date startDate1Target = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

                monthName = getMonth(startDate1Target);
                yearName = getYear(startDate1Target);
                int month; int year;
                month= currentMonth.getMonthValue();
                year = currentMonth.getYear();

                double bucket1 =0.0, bucket2=0.0, bucket3=0.0;
                for (int i =1 ; i<=1 ;i++){
                    List<SalesSummaryModel> dayBucketSales = sclSalesSummaryService.get10DayBucketSales(sclCustomer, month, year);
                    if(CollectionUtils.isNotEmpty(dayBucketSales)){

                        for (SalesSummaryModel dayBucketSale : dayBucketSales) {
                                bucket1 += dayBucketSale.getBucket1()!=null? dayBucketSale.getBucket1():0.0;
                                actualBucketSaleList.add(bucket1);
                                bucket2 += dayBucketSale.getBucket2()!=null? dayBucketSale.getBucket2():0.0;
                                actualBucketSaleList.add(bucket2);
                                bucket3 += dayBucketSale.getBucket3()!=null? dayBucketSale.getBucket3():0.0;
                                actualBucketSaleList.add(bucket3);
                        }
                    }
                    else {

                         actualBucketSaleList.add(0.0);
                    }
                    month = currentMonth.minusMonths(1).getMonthValue();
                    year = currentMonth.getYear();
                }
                LOGGER.info((String.format("Actual Sales list:%s",String.valueOf(actualBucketSaleList.size()))));
                //target sales
                startDateTarget = LocalDate.of(currentMonth.getYear(), currentMonth.getMonthValue() - 1, 1);
                startDate1Target = Date.from(startDateTarget.atStartOfDay(ZoneId.systemDefault()).toInstant());
                monthName = getMonth(startDate1Target);
                yearName = getYear(startDate1Target);
                List<Double> targetBucketSaleList = new ArrayList<>();
                double revisedTarget=0.0,sumRevisedTarget=0.0;

               /* for (int i = 0; i <= 1; i++) {

                    DealerRevisedMonthlySalesModel dealerRevisedMonthlySalesModel = salesPerformanceService.getMonthlySaleTargetGraphForDealer(sclCustomer.getUid(), monthName, yearName);
                    if(StringUtils.isBlank(bgpFilter) || bgpFilter.equalsIgnoreCase("ALL")) {
                        if (dealerRevisedMonthlySalesModel != null) {
                            if (dealerRevisedMonthlySalesModel.getBucket1() != null) {
                                targetBucketSaleList.add(dealerRevisedMonthlySalesModel.getBucket1() != 0.0 ? dealerRevisedMonthlySalesModel.getBucket1() : 0.0);
                            }
                            else {
                                targetBucketSaleList.add(0.0);
                            }
                            if (dealerRevisedMonthlySalesModel.getBucket2() != null) {
                                targetBucketSaleList.add(dealerRevisedMonthlySalesModel.getBucket2() != 0.0 ? dealerRevisedMonthlySalesModel.getBucket2() : 0.0);
                            }
                            else {
                                targetBucketSaleList.add(0.0);
                            }
                            if (dealerRevisedMonthlySalesModel.getBucket3() != null) {
                                targetBucketSaleList.add(dealerRevisedMonthlySalesModel.getBucket3() != 0.0 ? dealerRevisedMonthlySalesModel.getBucket3() : 0.0);
                            }
                            else {
                                targetBucketSaleList.add(0.0);
                            }
                        }
                        else{
                            targetBucketSaleList.add(0.0);
                            targetBucketSaleList.add(0.0);
                            targetBucketSaleList.add(0.0);
                        }
                    }
                    else
                    {
                        if (dealerRevisedMonthlySalesModel != null) {
                            if(dealerRevisedMonthlySalesModel.getListOfSkus()!=null && !dealerRevisedMonthlySalesModel.getListOfSkus().isEmpty())
                            {
                                for (ProductModel product : dealerRevisedMonthlySalesModel.getListOfSkus()) {
                                    if(product.getCode().equalsIgnoreCase(bgpFilter)){
                                        ProductSaleModel productSaleModel = salesPerformanceDao.getTotalTargetForProductBGPFilterMTD(dealerRevisedMonthlySalesModel.getCustomerCode(),product.getCode(),monthName, yearName);
                                        if(productSaleModel!=null) {
                                            targetBucketSaleList.add(productSaleModel.getBucket1());
                                            targetBucketSaleList.add(productSaleModel.getBucket2());
                                            targetBucketSaleList.add(productSaleModel.getBucket3());
                                        }
                                        else{
                                            targetBucketSaleList.add(0.0);
                                            targetBucketSaleList.add(0.0);
                                            targetBucketSaleList.add(0.0);
                                        }
                                    }
                                    else{
                                        targetBucketSaleList.add(0.0);
                                        targetBucketSaleList.add(0.0);
                                        targetBucketSaleList.add(0.0);
                                    }
                                }
                            }
                            else{
                                targetBucketSaleList.add(0.0);
                                targetBucketSaleList.add(0.0);
                                targetBucketSaleList.add(0.0);
                            }
                        }
                        else {
                            targetBucketSaleList.add(0.0);
                            targetBucketSaleList.add(0.0);
                            targetBucketSaleList.add(0.0);
                        }
                    }
                    LOGGER.info((String.format("Target Bucket list Size:%s",String.valueOf(targetBucketSaleList.size()))));
                    startDateTarget = LocalDate.of(startDateTarget.getYear(), startDateTarget.getMonthValue() + 1, 1);
                    startDate1Target = Date.from(startDateTarget.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    monthName = getMonth(startDate1Target);
                    yearName = getYear(startDate1Target);
                } */

                List<String> monthYearList = new ArrayList<>();
                int j = 1,k=1;
                for (int i = 1; i <= 6; i++) {
                    //1-10, 11- 20 and 21-31
                    MonthlySalesData data = new MonthlySalesData();
                    String monthYearr = currentMonth.getMonth().minus(1).getDisplayName(TextStyle.SHORT, Locale.ENGLISH).concat("b").concat(String.valueOf(k++));
                    if (i > 3) {
                        monthYearr = currentMonth.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).concat("b").concat(String.valueOf(j++));
                    }

                    monthYearList.add(monthYearr);
                }

                for (int i = 0; i <= 5; i++) {
                    MonthlySalesData data = new MonthlySalesData();

                    if(actualBucketSaleList!=null && !actualBucketSaleList.isEmpty())
                        actualSales = actualBucketSaleList.get(i);
                    //temp till product source confirms
                   /* if(targetBucketSaleList!=null && !targetBucketSaleList.isEmpty())
                        targetSales = targetBucketSaleList.get(i);*/
                    if(monthYearList!=null && !monthYearList.isEmpty())
                        monthYear= monthYearList.get(i);
                    if(GrowthBucketSaleList!=null && !GrowthBucketSaleList.isEmpty())
                        growth1 = GrowthBucketSaleList.get(i);


                    data.setActualSales(actualSales);
                    data.setTargetSales(targetSales);
                    data.setGrowth(growth1);
                    data.setMonthYear(monthYear);
                    dataList.add(data);
                }
            } else if (filter.equalsIgnoreCase(FILTER_MTD)) {
                for (int i = 1; i <= 6; i++) {
                    MonthlySalesData data = new MonthlySalesData();
                    currentMonthSale = sclSalesSummaryService.getSalesByMonth(sclCustomer,currentMonth.getMonthValue(), currentMonth.getYear(),bgpFilter);
                    LocalDate lastMonth = currentMonth.minusMonths(1);
                    lastMonthSale = sclSalesSummaryService.getSalesByMonth(sclCustomer,lastMonth.getMonthValue(), lastMonth.getYear(),bgpFilter);
                    data.setActualSales(currentMonthSale);

                    Date date = Date.from(currentMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    var monthYear1 = SclDateUtility.getFormattedDate(date, "MMM-YYYY");
                    currentMonthTarget = salesPerformanceService.getSalesTargetForMonthDealer(sclCustomer, currentBaseSite, currentMonth.getYear(), currentMonth.getMonthValue(), bgpFilter);
                    lastMonthTarget = salesPerformanceService.getSalesTargetForMonthDealer(sclCustomer, currentBaseSite, lastMonth.getYear(), lastMonth.getMonthValue(), bgpFilter);

                    data.setTargetSales(currentMonthTarget);
                    data.setGrowth(lastMonthSale-currentMonthSale);

                    if (Objects.nonNull(monthYear1)) {
                        data.setMonthYear(monthYear1.replace("-", " "));
                    }
                    dataList.add(data);
                    currentMonth = lastMonth;
                }
            } else if (filter.equalsIgnoreCase(FILTER_YTD)) {
                double ytdDealerTarget =0.0;
                List<Double> yearWiseTargetSales = new ArrayList<>();
                List<MonthlySalesData> ytdDataListDealer = new ArrayList<>();

                LocalDate startDate, endDate,startDateForGrowth,endDateForGrowth;
                LocalDate startDateForGrowth11 = null,endDateForGrowth11=null;
                LocalDate startDateMonthYear = LocalDate.now();
                List<Double> yearWiseSales = new ArrayList<>();
                List<Double> yearWiseGrowthSales = new ArrayList<>();

                LocalDate date = LocalDate.now();
                int currentYear = date.getYear();
                if (date.getMonthValue() < Month.APRIL.getValue()) {
                    startDate = LocalDate.of(currentYear - 1, Month.APRIL, 1);
                } else {
                    startDate = LocalDate.of(currentYear, Month.APRIL, 1);
                }
                endDate  = startDate.minusMonths(1).plusYears(1);

                for (int i = 0; i <= 2; i++) {
                    StringBuilder f = new StringBuilder();
                    String financialYear= String.valueOf(f.append(String.valueOf(currentYear)).append("-").append(String.valueOf(currentYear+1)));

                    currentSales =sclSalesSummaryService.getSalesByMonthAndYear(sclCustomer, startDate.getMonthValue(), startDate.getYear(),endDate.getMonthValue(), endDate.getYear(), bgpFilter);
                    ytdDealerTarget = salesPerformanceService.getAnnualSalesTargetForDealerFY(sclCustomer,bgpFilter,financialYear);

                    startDate = LocalDate.of(startDate.getYear() - 1, Month.APRIL, 1);
                    endDate = LocalDate.of(endDate.getYear() - 1, Month.MARCH, 31);

                    yearWiseSales.add(currentSales);
                    yearWiseTargetSales.add(ytdDealerTarget);
                }


                if (date.getMonthValue() < Month.APRIL.getValue()) {
                    startDateForGrowth = LocalDate.of(currentYear - 1, Month.APRIL, 1);
                } else {
                    startDateForGrowth = LocalDate.of(currentYear, Month.APRIL, 1);
                }

                endDateForGrowth  = startDateForGrowth.minusMonths(1).plusYears(1);
                for (int i = 0; i <= 2; i++) {

                    currentSales = sclSalesSummaryService.getSalesByMonthAndYear(sclCustomer, startDateForGrowth.getMonthValue(),startDateForGrowth.getYear(),endDateForGrowth.getMonthValue(),endDateForGrowth.getYear(),bgpFilter);
                    startDateForGrowth11 = startDateForGrowth.minusYears(1);
                    endDateForGrowth11 = endDateForGrowth.minusYears(1);
                    lastYearSales = sclSalesSummaryService.getSalesByMonthAndYear(sclCustomer, startDateForGrowth11.getMonthValue(),startDateForGrowth11.getYear(),endDateForGrowth11.getMonthValue(),endDateForGrowth11.getYear(),bgpFilter);
                    growth=lastYearSales-currentSales;
                    startDateForGrowth = startDateForGrowth11;
                    endDateForGrowth = endDateForGrowth11;
                    yearWiseGrowthSales.add(growth);
                }

                for (int i = 0; i <= 2; i++) {
                    MonthlySalesData data = new MonthlySalesData();

                    data.setMonthYear(String.valueOf(startDateMonthYear.getYear() - i));
                    double currentYrSales = yearWiseSales.get(i);
                    data.setActualSales(currentYrSales!=0.0?currentYrSales:0.0);
                    //target sales to be done
                    double currentYearTargetSale = yearWiseTargetSales.get(i);
                    data.setTargetSales(currentYearTargetSale!=0.0?currentYearTargetSale:0.0);
                    double growths = yearWiseGrowthSales.get(i);
                    data.setGrowth(growths!=0.0?growths:0.0);
                    dataList.add(data);
                }
            }
        }
        else if(sclCustomer.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
            //10 day buckets not applicable for retailer
            List<MonthlySalesData> mtdDataListRetailer= new ArrayList<>();
            if (filter.equalsIgnoreCase(FILTER_MTD)) {
                for (int i = 1; i <= 6; i++) {
                    MonthlySalesData data = new MonthlySalesData();
                    //currentMonthSale = salesPerformanceService.getActualTargetForSalesForMonthRetailer(sclCustomer, currentBaseSite, currentMonth.getYear(), currentMonth.getMonthValue(), bgpFilter);
                    Map<String,Double> salesQty=new HashMap<>();
                    salesQty=networkService.getSalesQuantityForRetailerByMonthYear(sclCustomer,currentMonth.getMonthValue(),currentMonth.getYear(),null,bgpFilter);
                    currentMonthSale=salesQty.get("quantityInBags");
                    LocalDate lastMonth = currentMonth.minusMonths(1);
                    //lastMonthSale = salesPerformanceService.getActualTargetForSalesForMonthRetailer(sclCustomer, currentBaseSite, lastMonth.getYear(), lastMonth.getMonthValue(), bgpFilter);
                    salesQty=networkService.getSalesQuantityForRetailerByMonthYear(sclCustomer,lastMonth.getMonthValue(),lastMonth.getYear(),null,bgpFilter);
                    lastMonthSale=salesQty.get("quantityInBags");
                    data.setActualSales(currentMonthSale);

                    Date date = Date.from(currentMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    var monthYear2 = SclDateUtility.getFormattedDate(date, "MMM-YYYY");

                    targetSale = getSalesPerformanceService().getMonthlySalesTargetForRetailer(sclCustomer.getUid(), currentBaseSite,bgpFilter, monthYear2);
                    data.setTargetSales(targetSale);

                    if (Objects.nonNull(monthYear2)) {
                        data.setMonthYear(monthYear2.replace("-", " "));
                    }
                    dataList.add(data);
                    currentMonth = lastMonth;
                }
            }
            else if (filter.equalsIgnoreCase(FILTER_YTD)) {
                List<MonthlySalesData> ytdDataListRetailer= new ArrayList<>();
                double ytdDealerTarget =0.0;
                Date startDate1 = null,endDate1 = null,startDate2 = null, endDate2=null;

                LocalDate currentDate = LocalDate.now();
                LocalDate startDate, endDate,prevStartDate,prevEndDate;
                LocalDate startDateMonthYear=LocalDate.now();

                List<Double> yearWiseSales = new ArrayList<>();
                List<Double> yearWiseTargetSales = new ArrayList<>();

                startDate= LocalDate.of(currentDate.getYear(), Month.APRIL, 1);
                endDate = LocalDate.of(currentDate.getYear()+1,Month.APRIL, 1);


                LocalDate date = LocalDate.now();
                LOG.info("Retailer YTD Start Date and End Date:"+startDate + " " +endDate);
                for(int i=0;i<=2;i++)
                {
                    startDate1 = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    endDate1 = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());



                    int currentYear = date.getYear();
                    StringBuilder f = new StringBuilder();
                    String financialYear= String.valueOf(f.append(String.valueOf(currentYear)).append("-").append(String.valueOf(currentYear+1)));
                    date = date.minusYears(1);

                    LOG.info("Retailer YTD Start Date:"+startDate1 + " " +endDate1);
                    currentSales =  salesPerformanceDao.getMonthWiseForRetailerYTD(sclCustomer, startDate1, endDate1);

                    ytdDealerTarget = salesPerformanceService.getAnnualSalesTargetForRetailerFY(sclCustomer,bgpFilter,financialYear);
                    LOG.info("Retailer YTD Graph Sale:" + currentSales);
                    startDate = startDate1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    endDate = endDate1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                    startDate= LocalDate.of(startDate.getYear()-1, Month.APRIL, 1);
                    endDate = LocalDate.of(endDate.getYear()-1,Month.APRIL, 1);

                    yearWiseSales.add(currentSales);
                    yearWiseTargetSales.add(ytdDealerTarget);
                }

                for(int i=0;i<=2;i++)
                {
                    MonthlySalesData data = new MonthlySalesData();
                    data.setMonthYear(String.valueOf(startDateMonthYear.getYear()-i));
                    double currentYrSales = yearWiseSales.get(i);
                    double currentYrTargetSales = yearWiseTargetSales.get(i);
                    LOG.info("Retailer YTD Graph Actual Sale:" + currentYrSales);
                    data.setActualSales(currentYrSales!=0.0?currentYrSales:0.0);
                    //target sales to be done
                    data.setTargetSales(currentYrTargetSales!=0.0?currentYrTargetSales:0.0);
                    dataList.add(data);
                }
            }
        }
        monthlySalesListData.setSales(dataList);
        return monthlySalesListData;
    }

    //not needed
    @Override
    public MonthlySalesListData getActualVsTargetSalesGraphForTSMRH(String filter,List<String> doList,List<String> subAreaList) {
        return  null;
    }

    @Override
    public ProratedBreachData getProratedBreach(List<String> doList,List<String> territoryList) {
        SclUserModel sclUser=(SclUserModel) getUserService().getCurrentUser();
        BaseSiteModel currentBaseSite=baseSiteService.getCurrentBaseSite();
        ProratedBreachData data=new ProratedBreachData();

        double actualTarget=0.0, totalTarget=0.0, proratedTarget=0.0 ,achievementPercentage =0.0, behindProratedTarget=0.0, aheadProratedTarget=0.0;

        Calendar cal = Calendar.getInstance();
        int noOfDaysInTheMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int noOfDaysGoneByInTheMonth = cal.get(Calendar.DAY_OF_MONTH) - 1;

        int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        int year = cal.get(Calendar.YEAR);
        Calendar calTwo = new GregorianCalendar(year, 11, 31);
        int totalNoOfDaysInYear = calTwo.get(Calendar.DAY_OF_YEAR);
        int remainingDaysInTheYear = totalNoOfDaysInYear - dayOfYear;
        int noOfDaysGoneBy=totalNoOfDaysInYear-remainingDaysInTheYear;

        actualTarget= sclSalesSummaryService.getCurrentMonthSales(sclUser, territoryList);
       // actualTarget=getSalesPerformanceService().getActualTargetForSalesMTD(sclUser,currentBaseSite,doList,subAreaList);
        data.setActual(actualTarget);

        totalTarget=getSalesPerformanceService().getMonthlySalesTarget(sclUser,currentBaseSite,territoryList);
        data.setTarget(totalTarget);
        if(totalTarget!=0.0)
            proratedTarget=(totalTarget/noOfDaysInTheMonth)*noOfDaysGoneByInTheMonth;


        if(actualTarget!=0.0 && totalTarget!=0.0 && proratedTarget!=0.0 )
            achievementPercentage=(actualTarget - totalTarget)/proratedTarget;


        if(achievementPercentage >= 20)
        {
            data.setIsBreached(true);
        }
        else
        {
            data.setIsBreached(false);
        }
        return data;
    }

    @Override
    public Map<String, Object> getDirectDispatchOrdersMTDPercentage(int month, int year,List<String> doList, List<String> territoryList) {
        return salesPerformanceService.getDirectDispatchOrdersMTDPercentage(month, year,doList,territoryList);
    }

    @Override
    public SearchPageData<SalesPerformNetworkDetailsData> getZeroLiftingViewDetailsWithPagination(String fields, String customerType, String searchKey, List<String> doList, List<String> subAreaList, SearchPageData searchPageData) {
        BaseSiteModel currentBaseSite=baseSiteService.getCurrentBaseSite();
        return salesPerformanceService.getZeroLiftingViewDetailsWithPagination(fields,currentBaseSite,customerType,searchKey,doList,subAreaList,searchPageData);
    }

    public LowPerformingNetworkData getLowPerformingNetworkDataForDealerRetailerInfluencers( String leadType, int month, int year,String filter, List<String> territoryList,List<String> subAreaList,List<String> districtList) {
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        List<SalesPerformNetworkDetailsData> currentNetworkWsDataList = new ArrayList<>();
        List<SalesPerformNetworkDetailsData> convertedList=new ArrayList<>();
        LowPerformingNetworkData data=new LowPerformingNetworkData();

        List<TerritoryMasterModel> territoryMasterModels= (List<TerritoryMasterModel>) territoryMasterService.getCurrentTerritory();
        LOGGER.info(String.format("territoryMasterModels:: %s",territoryMasterModels));

        List<SclCustomerModel> customerFilteredList = salesPerformanceDao.getCurrentNetworkCustomers(leadType, null, territoryMasterModels, filter, true,false,true);
        if(CollectionUtils.isNotEmpty(customerFilteredList)) {
            LOGGER.info(String.format("Dealer-customer List size for Low lifting:%s", customerFilteredList.size()));
        }else{
            LOGGER.info(String.format("Dealer-customer List size  for Low lifting is empty"));
        }
        if (customerFilteredList != null && !customerFilteredList.isEmpty()) {
            if(CollectionUtils.isNotEmpty(customerFilteredList)){
              convertedList = salesPerformnceCustomerDetailsConverter.convertAll(customerFilteredList);
                currentNetworkWsDataList.addAll(convertedList);
            }
        }

        if(leadType.equalsIgnoreCase(RETAILER)) {
            if (CollectionUtils.isNotEmpty(currentNetworkWsDataList)) {
                LOGGER.info((String.format("Size for Dealer:%s", currentNetworkWsDataList.size())));
                currentNetworkWsDataList = currentNetworkWsDataList.stream().filter(nw -> Objects.nonNull(nw.getSalesQuantity()) && Objects.nonNull(nw.getSalesQuantity().getRetailerSaleQuantity())).filter(nw -> nw.getSalesQuantity().getRetailerSaleQuantity() != 0.0 && nw.getSalesQuantity().getRetailerSaleQuantity()!=0 ).sorted(Comparator.comparing(nw -> nw.getSalesQuantity().getRetailerSaleQuantity())).collect(Collectors.toList());
                currentNetworkWsDataList= currentNetworkWsDataList.stream().limit((long) currentNetworkWsDataList.size() * LOWPERCENTAGE / 100).collect(Collectors.toList());
               data.setCount(currentNetworkWsDataList.size());

                Double avgOfCounterShare = 0.0;
                CounterShareResponseData responseData = null;
                double sumOfNumeratorSales = 0.0, sumOfPotential = 0.0;

                for (SalesPerformNetworkDetailsData detailsData : currentNetworkWsDataList) {
                    CounterShareData counterShareData = new CounterShareData();
                    counterShareData.setDealerCode(detailsData.getCode());
                    counterShareData.setMonth(LocalDate.now().getMonthValue());
                    counterShareData.setYear(LocalDate.now().getYear());
                    responseData = getCounterShareData(counterShareData);
                    if (Objects.nonNull(responseData) && responseData.getPotential() > 0) {
                        sumOfNumeratorSales += responseData.getNumeratorSales();
                        sumOfPotential += responseData.getPotential();
                    }
                    LOGGER.info(" Counter Share" + responseData.getCounterShare() + " Potential:" + responseData.getPotential() + " Numerator:" + responseData.getNumeratorSales() + " SelfBrandSale" + responseData.getSelfBrandSale() + " TotalSales " + responseData.getTotalSales());
                }
                if (sumOfPotential != 0.0 && sumOfNumeratorSales != 0.0) {
                    avgOfCounterShare = (sumOfNumeratorSales / sumOfPotential) * 100;
                } else {
                    avgOfCounterShare = 0.0;
                }
                data.setCounterSharePercentage(avgOfCounterShare);

                Double totalMonthlyPotential = currentNetworkWsDataList.stream().filter(obj -> Objects.nonNull(obj.getPotential())).mapToDouble(objects -> Double.parseDouble(objects.getPotential())).sum();
                data.setTotalMonthlyPotential(totalMonthlyPotential);

                Double avgMonthlyOrders = 0.0;
                if (currentNetworkWsDataList.size() != 0) {
                    avgMonthlyOrders = currentNetworkWsDataList.stream().filter(nw -> nw.getSalesQuantity().getCurrent() != 0).mapToDouble(objects -> objects.getSalesQuantity().getCurrent()).sum() / currentNetworkWsDataList.size();
                    data.setCountOfAvgMonthlyOrders(avgMonthlyOrders);
                } else {
                    avgMonthlyOrders = 0.0;
                    data.setCountOfAvgMonthlyOrders(avgMonthlyOrders);
                }
            }else{
                data.setCount(0);;
                data.setCounterSharePercentage(0.0);
                data.setCountOfAvgMonthlyOrders(0.0);
                data.setTotalMonthlyPotential(0.0);
            }
        }

         else  if (leadType.equalsIgnoreCase(DEALER)) {
            if (CollectionUtils.isNotEmpty(currentNetworkWsDataList)) {
                LOGGER.info((String.format("Size for Dealer:%s", currentNetworkWsDataList.size())));
                currentNetworkWsDataList = currentNetworkWsDataList.stream().filter(nw -> Objects.nonNull(nw.getSalesQuantity()) && Objects.nonNull(nw.getSalesQuantity().getActual())).filter(nw -> nw.getSalesQuantity().getActual() != 0.0).sorted(Comparator.comparing(nw -> nw.getSalesQuantity().getActual())).collect(Collectors.toList());
                currentNetworkWsDataList= currentNetworkWsDataList.stream().limit((long) currentNetworkWsDataList.size() * LOWPERCENTAGE / 100).collect(Collectors.toList());
                data.setCount(currentNetworkWsDataList.size());

                Double totalMonthlyPotential = currentNetworkWsDataList.stream().filter(obj -> Objects.nonNull(obj.getPotential())).mapToDouble(objects -> Double.parseDouble(objects.getPotential())).sum();
                data.setTotalMonthlyPotential(totalMonthlyPotential);

                Double avgMonthlyOrders = 0.0;
                if (currentNetworkWsDataList.size() != 0) {
                    avgMonthlyOrders = currentNetworkWsDataList.stream().filter(nw -> Objects.nonNull(nw.getSalesQuantity().getActual())).filter(nw -> nw.getSalesQuantity().getActual() != 0).mapToDouble(objects -> objects.getSalesQuantity().getActual()).sum() / currentNetworkWsDataList.size();
                    data.setCountOfAvgMonthlyOrders(avgMonthlyOrders);
                } else {
                    avgMonthlyOrders = 0.0;
                    data.setCountOfAvgMonthlyOrders(avgMonthlyOrders);
                }

                Double avgOfCounterShare = 0.0;
                CounterShareResponseData responseData = null;
                double sumOfNumeratorSales = 0.0, sumOfPotential = 0.0;
                if (CollectionUtils.isNotEmpty(currentNetworkWsDataList)) {
                    {
                        for (SalesPerformNetworkDetailsData detailsData : currentNetworkWsDataList) {
                            CounterShareData counterShareData = new CounterShareData();
                            counterShareData.setDealerCode(detailsData.getCode());
                            counterShareData.setMonth(LocalDate.now().getMonthValue());
                            counterShareData.setYear(LocalDate.now().getYear());
                            responseData = getCounterShareData(counterShareData);
                            if (Objects.nonNull(responseData) && responseData.getPotential() > 0) {
                                sumOfNumeratorSales += responseData.getNumeratorSales();
                                sumOfPotential += responseData.getPotential();
                            }
                            LOGGER.info(" Counter Share" + responseData.getCounterShare() + " Potential:" + responseData.getPotential() + " Numerator:" + responseData.getNumeratorSales() + " SelfBrandSale" + responseData.getSelfBrandSale() + " TotalSales " + responseData.getTotalSales());
                        }
                    }
                    if (sumOfPotential != 0.0 && sumOfNumeratorSales != 0.0) {
                        avgOfCounterShare = (sumOfNumeratorSales / sumOfPotential) * 100;
                    } else {
                        avgOfCounterShare = 0.0;
                    }
                  /*  double avgMonthlySales = collect.stream().filter(nw->Objects.nonNull(nw.getSalesQuantity().getActual())).filter(nw -> nw.getSalesQuantity().getActual() != 0).mapToDouble(objects -> objects.getSalesQuantity().getActual()).sum();
                    totalMonthlyPotential = collect.stream().filter(obj -> Objects.nonNull(obj.getPotential())).mapToDouble(objects -> Double.parseDouble(objects.getPotential())).sum();
                    avgOfCounterShare = (avgMonthlySales/totalMonthlyPotential) * 100;*/
                    data.setCounterSharePercentage(avgOfCounterShare);
                } else {
                    avgOfCounterShare = 0.0;
                    data.setCounterSharePercentage(avgOfCounterShare);
                }
            }else{
                data.setCount(0);;
                data.setCounterSharePercentage(0.0);
                data.setCountOfAvgMonthlyOrders(0.0);
                data.setTotalMonthlyPotential(0.0);
            }
        }

        else if(leadType.equalsIgnoreCase(INFLUENCER)) {
            if (CollectionUtils.isNotEmpty(currentNetworkWsDataList)) {
                    currentNetworkWsDataList = currentNetworkWsDataList.stream().filter(nw -> Objects.nonNull(nw.getBagLifted())).filter(nw -> nw.getBagLifted() != 0 || nw.getBagLifted() != 0.0).limit((long) currentNetworkWsDataList.size() * LOWPERCENTAGE / 100).collect(Collectors.toList());
                    LOGGER.info((String.format("Size for INFLUENCER after low perform:%s", currentNetworkWsDataList.size())));
                    data.setCount(currentNetworkWsDataList.size());
                    data.setTotalMonthlyPotential(0.0);
                    data.setCounterSharePercentage(0.0);

                    double avgMonthlyOrders = 0.0;
                    if (currentNetworkWsDataList.size() != 0) {
                        avgMonthlyOrders = currentNetworkWsDataList.stream().mapToDouble(SalesPerformNetworkDetailsData::getBagLifted).sum() / currentNetworkWsDataList.size();
                        data.setCountOfAvgMonthlyOrders(avgMonthlyOrders);
                    } else {
                        avgMonthlyOrders = 0.0;
                        data.setCountOfAvgMonthlyOrders(avgMonthlyOrders);
                    }
            }else{
                data.setCount(0);;
                data.setCounterSharePercentage(0.0);
                data.setCountOfAvgMonthlyOrders(0.0);
                data.setTotalMonthlyPotential(0.0);
            }
        }
        return data;
    }

    @Override
    public LowPerformingNetworkData getLowPerformingCountDetailsDealer(String leadType) {
        return salesPerformanceService.getLowPerformingSummaryDataDealer(leadType);
    }

    @Override
    public SalesPerformNetworkDetailsListData getListOfAllLowPerformingDealerRetailerInfluencers( String fields,String leadType, String searchKey,List<String> territoryList,List<String> subAreaList,List<String> districtList) {

        List<TerritoryMasterModel> territoryMasterModels = (List<TerritoryMasterModel>) territoryMasterService.getCurrentTerritory();
        LOGGER.info(String.format("territoryMasterModels:: %s", territoryMasterModels));

        List<SclCustomerModel> customerFilteredList = salesPerformanceDao.getCurrentNetworkCustomers(leadType, null, territoryMasterModels, searchKey, true, false, true);
        if (CollectionUtils.isNotEmpty(customerFilteredList)) {
            LOGGER.info(String.format("Low Perform customer List size:%s", customerFilteredList.size()));
        } else {
            LOGGER.info(String.format("Low Perform customer List size is empty"));
        }

        SalesPerformNetworkDetailsListData dealerCurrentNetworkListData = new SalesPerformNetworkDetailsListData();
        List<SalesPerformNetworkDetailsData> currentNetworkWsDataList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(customerFilteredList)) {
           // List<SalesPerformNetworkDetailsData> convertedList = salesPerformnceCustomerDetailsConverter.convertAll(customerFilteredList);
            List<SalesPerformNetworkDetailsData> convertedList= new ArrayList<>();
            if(leadType.equalsIgnoreCase(DEALER)){
                convertedList = getDealerDetailedSummaryListDataForSP(customerFilteredList);

            }else if(leadType.equalsIgnoreCase(RETAILER)) {
                convertedList = getRetailerDetailedSummaryListDataForSP(customerFilteredList);

            }else if(leadType.equalsIgnoreCase(INFLUENCER)) {
                convertedList = getInfluencerDetailedSummaryListData(customerFilteredList,null,null);

            }

            if(CollectionUtils.isNotEmpty(convertedList)) {
                LOGGER.info((String.format("Size for Customer Before Low Perform:%s", convertedList.size())));
                for (SalesPerformNetworkDetailsData detailsData : convertedList) {
                    LOGGER.info((String.format("code for Customer:%s.", detailsData.getCode())));
                }
                if (leadType.equalsIgnoreCase(DEALER)) {
                    convertedList = convertedList.stream().filter(nw -> Objects.nonNull(nw.getSalesQuantity())).filter(nw -> Objects.nonNull(nw.getSalesQuantity().getActual())).filter(nw -> nw.getSalesQuantity().getActual() != 0 && nw.getSalesQuantity().getActual()!=0.0 ).sorted(Comparator.comparing(nw -> nw.getSalesQuantity().getActual())).collect(Collectors.toList());
                    convertedList=convertedList.stream().limit(((long) convertedList.size() * LOWPERCENTAGE) / 100).collect(Collectors.toList());
                    LOGGER.info((String.format("Size for DEALER After Low Perform:%s", convertedList.size())));
                }else if (leadType.equalsIgnoreCase(RETAILER)) {
                    convertedList = convertedList.stream().filter(nw -> Objects.nonNull(nw.getSalesQuantity())).filter(nw -> Objects.nonNull(nw.getSalesQuantity().getRetailerSaleQuantity())).filter(nw -> nw.getSalesQuantity().getRetailerSaleQuantity() != 0 &&  nw.getSalesQuantity().getRetailerSaleQuantity() != 0.0).sorted(Comparator.comparing(nw -> nw.getSalesQuantity().getRetailerSaleQuantity())).collect(Collectors.toList());
                    convertedList=convertedList.stream().limit(((long) convertedList.size() * LOWPERCENTAGE) / 100).collect(Collectors.toList());
                    LOGGER.info((String.format("Size for Retailer After Low Perform:%s", convertedList.size())));
                } else if (leadType.equalsIgnoreCase(INFLUENCER)) {
                    convertedList = convertedList.stream().filter(nw -> Objects.nonNull(nw.getBagLifted())).filter(nw -> nw.getBagLifted() != 0 && nw.getBagLifted() != 0.0).sorted(Comparator.comparing(SalesPerformNetworkDetailsData::getBagLifted)).collect(Collectors.toList());
                    convertedList=convertedList.stream().limit(((long) convertedList.size() * LOWPERCENTAGE) / 100).collect(Collectors.toList());
                    LOGGER.info((String.format("Size for Influencer After Low Perform:%s", convertedList.size())));
                }
            }
            currentNetworkWsDataList.addAll(convertedList);
        }
        currentNetworkWsDataList.sort(Comparator.comparing(SalesPerformNetworkDetailsData::getName));
        dealerCurrentNetworkListData.setNetworkDetails(currentNetworkWsDataList);
        return dealerCurrentNetworkListData;
    }

    @Override
    public SalesPerformNetworkDetailsListData getListOfAllLowPerformingRetailerInfluencersForDealer(String fields, String leadType, String searchKey) {
        return salesPerformanceService.getListOfAllLowPerformingRetailerInfForDealers(fields,leadType,searchKey);
    }

    @Override
    public SearchPageData<SalesPerformNetworkDetailsData> getLowPerformingViewDetailsWithPagination(String fields, String customerType, String searchKey, List<String> doList, List<String> subAreaList, SearchPageData searchPageData) {
        BaseSiteModel currentBaseSite=baseSiteService.getCurrentBaseSite();
        return salesPerformanceService.getLowPerformingViewDetailsWithPagination(fields,currentBaseSite,customerType,searchKey,doList,subAreaList,searchPageData);
    }

    @Override
    public ZeroLiftingNetworkData getCountAndPotentialForZeroLifting(String leadType,int month,int year,String filter,List<String> territoryList,List<String> subAreaList,List<String> districtList) {
        BaseSiteModel site=baseSiteService.getCurrentBaseSite();
        return salesPerformanceService.getZeroLiftingSummaryData(month,year,filter,site,leadType,territoryList,subAreaList,districtList);
    }

    @Override
    public ZeroLiftingNetworkData getCountAndPotentialForZeroLiftingDealer(String leadType) {
        return salesPerformanceService.getZeroLiftingSummaryDataDealer(leadType);
    }

    @Override
    public SalesPerformNetworkDetailsListData getListOfAllZeroLiftingDealerRetailerInfluencers(String fields, String leadType, String filter,List<String> territoryList,List<String> subAreaList,List<String> districtList) {

        List<TerritoryMasterModel> territoryMasterModels = (List<TerritoryMasterModel>) territoryMasterService.getCurrentTerritory();
        LOGGER.info(String.format("territoryMasterModels:: %s", territoryMasterModels));

        List<SclCustomerModel> customerFilteredList = salesPerformanceDao.getCurrentNetworkCustomers(leadType, null, territoryMasterModels, filter, true, true, false);
        if (CollectionUtils.isNotEmpty(customerFilteredList)) {
            LOGGER.info(String.format("Zero Lift customer List size:%s", customerFilteredList.size()));
        } else {
            LOGGER.info(String.format("Zero Lift customer List size is empty"));
        }

        SalesPerformNetworkDetailsListData dealerCurrentNetworkListData = new SalesPerformNetworkDetailsListData();
        List<SalesPerformNetworkDetailsData> currentNetworkWsDataList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(customerFilteredList)) {
            //List<SalesPerformNetworkDetailsData> convertedList = salesPerformnceCustomerDetailsConverter.convertAll(customerFilteredList);
            List<SalesPerformNetworkDetailsData> convertedList= new ArrayList<>();
            if(leadType.equalsIgnoreCase(DEALER)){
                convertedList = getDealerDetailedSummaryListDataForSP(customerFilteredList);

            }else if(leadType.equalsIgnoreCase(RETAILER)) {
                convertedList = getRetailerDetailedSummaryListDataForSP(customerFilteredList);

            }else if(leadType.equalsIgnoreCase(INFLUENCER)) {
                convertedList = getInfluencerDetailedSummaryListData(customerFilteredList,null,null);

            }

            if(CollectionUtils.isNotEmpty(convertedList)) {
                LOGGER.info((String.format("Size for Customer Before Zero Lifting Perform:%s", convertedList.size())));
                for (SalesPerformNetworkDetailsData detailsData : convertedList) {
                    LOGGER.info((String.format("code for Customer:%s.", detailsData.getCode())));
                }
                if (leadType.equalsIgnoreCase(DEALER)) {
                    convertedList = convertedList.stream().filter(nw -> Objects.nonNull(nw.getSalesQuantity())).filter(nw -> Objects.nonNull(nw.getSalesQuantity().getActual())).filter(nw -> nw.getSalesQuantity().getActual() == 0 || nw.getSalesQuantity().getActual()==0.0 ).collect(Collectors.toList());
                    LOGGER.info((String.format("Size for DEALER After Zero Lifting:%s", convertedList.size())));
                }else if (leadType.equalsIgnoreCase(RETAILER)) {
                    convertedList = convertedList.stream().filter(nw -> Objects.nonNull(nw.getSalesQuantity())).filter(nw -> Objects.nonNull(nw.getSalesQuantity().getRetailerSaleQuantity())).filter(nw -> nw.getSalesQuantity().getRetailerSaleQuantity() == 0 ||  nw.getSalesQuantity().getRetailerSaleQuantity() == 0.0).collect(Collectors.toList());
                    LOGGER.info((String.format("Size for Retailer After Zero Lifting:%s", convertedList.size())));
                } else if (leadType.equalsIgnoreCase(INFLUENCER)) {
                    convertedList = convertedList.stream().filter(nw -> Objects.nonNull(nw.getBagLifted())).filter(nw -> nw.getBagLifted() == 0 || nw.getBagLifted() == 0.0).collect(Collectors.toList());
                    LOGGER.info((String.format("Size for Influencer After Zero Lifting:%s", convertedList.size())));
                }
            }
            currentNetworkWsDataList.addAll(convertedList);
        }
        currentNetworkWsDataList.sort(Comparator.comparing(SalesPerformNetworkDetailsData::getName));
        dealerCurrentNetworkListData.setNetworkDetails(currentNetworkWsDataList);
        return dealerCurrentNetworkListData;
    }

    @Override
    public SalesPerformNetworkDetailsListData getListOfAllZeroLiftingRetailersInfluencerForDealers(String fields, String leadType, String searchKey) {
        return salesPerformanceService.getListOfAllZeroLiftingRetailerInfForDealer(fields,leadType,searchKey);
    }

    @Override
    public Integer getCountForAllDealerRetailerInfluencers(String leadType,List<String> territoryList,List<String> subAreaList,List<String> districtList) {
        return salesPerformanceService.getCountForAllDealerRetailerInfluencers(leadType, null,null,territoryList,subAreaList,districtList);
    }

    @Override
    public Integer getCountOfAllRetailersInfluencers(String leadType) {
        return salesPerformanceService.getCountOfAllRetailersInfluencers(leadType);
    }

    @Override
    public SalesPerformNetworkDetailsListData getBottomLaggingCounters( List<String> territoryList) {
        //  SclUserModel sclUser=(SclUserModel) getUserService().getCurrentUser();
        //BaseSiteModel currentBaseSite=baseSiteService.getCurrentBaseSite();
        return salesPerformanceService.getBottomLaggingCounters(territoryList);
    }
    public static Date getFirstDateOfMonth(int month,int year){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.DAY_OF_MONTH,
                calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }
    public static Date getLastDateOfMonth(int month,int year){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }
    NCRTrendData getActualNcrForCurrentMonth(String state,String yearMonth,BaseSiteModel site){
        NCRTrendData ncrTrendData=new NCRTrendData();

        int month=0,year=0;
        String[] split = yearMonth.split("-");
        month=Integer.parseInt(split[1]);
        year=Integer.parseInt(split[0]);


        LocalDate firstDayOfMonth = LocalDate.of(year, month,Month.of(month).minLength());
        LocalDate lastDayOfMonth = LocalDate.of(year, month, Month.of(month).maxLength());
        Date startDate = Date.from(firstDayOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(lastDayOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());

       /* Date startDate = getFirstDateOfMonth(month-1,year);
        Date endDate = getLastDateOfMonth(month-1,year);*/

        Double ncrThresholdForCurrentMonth= salesPerformanceService.getNCRThreshold(state,site,yearMonth);


        double actualNcr = 0.0;
        double currentNCRGap=0.0;
        List<String> districts = new ArrayList<String>(geographicalRegionFacade.findAllDistrict(state));
        for (String district : districts) {
            List<String> allTaluka = geographicalRegionFacade.findAllTaluka(state, district);
            if(allTaluka!=null && !allTaluka.isEmpty()) {
                for (String s : allTaluka) {
                    double actualNcrPerTaluka = 0.0;
                    double ncrSales = 0.0;
                    LOGGER.info("taluka:" + s);
                    List<List<Object>> salesHistoryModelList = salesPerformanceService.getSalesHistoryModelList(s, startDate, endDate, site);
                    //List<List<Object>> salesHistoryModelList = salesPerformanceService.getSalesHistoryModelList(state, startDate, endDate, site);
                    double sumOfSalesQty = salesHistoryModelList.stream().filter(o -> o.get(1) != null).mapToDouble(o -> (double) o.get(1)).sum();
                    LOGGER.info("sumOfSalesQty:" + sumOfSalesQty);
                    for (List<Object> objects : salesHistoryModelList) {
                        if (objects.get(0) != null && objects.get(1) != null) {
                            LOGGER.info("1st Col:"+objects.get(0));
                            LOGGER.info("2nd Col:"+objects.get(1));
                            double ncr = (double) objects.get(0);
                            double salesQty = (double) objects.get(1);
                            ncrSales += ncr * salesQty;
                        }
                    }
                    if (ncrSales != 0.0 && sumOfSalesQty != 0.0) {
                        actualNcrPerTaluka = ncrSales / sumOfSalesQty;
                        actualNcr += actualNcrPerTaluka;
                    }
                }
            }
        }
        LOGGER.info("Actual NCR"+actualNcr);
        LOGGER.info("ncr threshold for CurrentMonth:"+ncrThresholdForCurrentMonth);
        if (actualNcr >= 0.0 && ncrThresholdForCurrentMonth >= 0.0) {
            if (actualNcr >= (1.1 * ncrThresholdForCurrentMonth)) {
                ncrTrendData.setGreenLevel(true);
                ncrTrendData.setAmberLevel(false);
                ncrTrendData.setRedLevel(false);
                LOGGER.info("current ncr gap 1st:"+currentNCRGap);
                currentNCRGap=actualNcr-ncrThresholdForCurrentMonth;
            } else if (actualNcr >= (1.0 * ncrThresholdForCurrentMonth)) {
                ncrTrendData.setGreenLevel(false);
                ncrTrendData.setAmberLevel(true);
                ncrTrendData.setRedLevel(false);
                LOGGER.info("current ncr gap 2nd:"+currentNCRGap);
                currentNCRGap=actualNcr-ncrThresholdForCurrentMonth;
            } else {
                ncrTrendData.setGreenLevel(false);
                ncrTrendData.setAmberLevel(false);
                ncrTrendData.setRedLevel(true);
                LOGGER.info("current ncr gap 3rd:"+currentNCRGap);
                currentNCRGap=ncrThresholdForCurrentMonth-actualNcr;
            }
        }
        ncrTrendData.setMonthYear(String.valueOf(Month.of(month)).concat("-").concat(String.valueOf(year)));
        ncrTrendData.setCurrentNcrGap(currentNCRGap);
        return ncrTrendData;
    }
    @Override
    public NCRTrendListData getNCRTrendList() {
        NCRTrendListData ncrTrendListData=new NCRTrendListData();
        List<NCRTrendData> ncrTrendDataList=new ArrayList<>();
        NCRTrendData ncrTrendData=new NCRTrendData();

        double currentNCRGap=0.0;
        SclUserModel sclUser=(SclUserModel) getUserService().getCurrentUser();
        BaseSiteModel currentBaseSite=baseSiteService.getCurrentBaseSite();

        String state = sclUser.getState();
        LocalDate now = LocalDate.now();
        int year = now.getYear();//2023
        int month = now.getMonthValue();//3
        String yearMonthCM=null,yearMonthPM=null,yearMonthPBM=null;
        if (month <= 9) {
            if(month==2){
                yearMonthCM = String.valueOf(year).concat("-").concat("0").concat(String.valueOf(month));//2023-02
                yearMonthPM = String.valueOf(year-1).concat("-").concat("0").concat(String.valueOf(month-1));//2022-01
                yearMonthPBM = String.valueOf(year-1).concat("-").concat(String.valueOf("12"));//2022-12
            }else if(month==1){
                yearMonthCM = String.valueOf(year).concat("-").concat("0").concat(String.valueOf(month));//2023-01
                yearMonthPM = String.valueOf(year-1).concat("-").concat(String.valueOf("12"));//2022-12
                yearMonthPBM = String.valueOf(year-1).concat("-").concat(String.valueOf("11"));//2022-11
            }
            else {
                yearMonthCM = String.valueOf(year).concat("-").concat("0").concat(String.valueOf(month));//2023-03
                yearMonthPM = String.valueOf(year).concat("-").concat("0").concat(String.valueOf(month - 1));//2023-02
                yearMonthPBM = String.valueOf(year).concat("-").concat("0").concat(String.valueOf(month - 2));//2023-01
            }
        } else if(month==11) {
            yearMonthCM = String.valueOf(year).concat("-").concat(String.valueOf(month));//2023-11
            yearMonthPM = String.valueOf(year).concat("-").concat(String.valueOf(month-1));//2023-10
            yearMonthPBM = String.valueOf(year).concat("-").concat("0").concat(String.valueOf(month-2));//2023-09
        }
        else if(month==10) {
            yearMonthCM = String.valueOf(year).concat("-").concat(String.valueOf(month));//2023-10
            yearMonthPM = String.valueOf(year).concat("-").concat("0").concat(String.valueOf(month-1));//2023-09
            yearMonthPBM = String.valueOf(year).concat("-").concat("0").concat(String.valueOf(month-2));//2023-08
        }
        LOGGER.info(yearMonthCM+"-"+yearMonthPM+"-"+yearMonthPBM);
        NCRTrendData actualNcrForCurrentMonth=new NCRTrendData();
        NCRTrendData actualNcrForPrevMonth=new NCRTrendData();
        NCRTrendData actualNcrForPrevBackMonth=new NCRTrendData();

        if(yearMonthCM!=null)
            actualNcrForCurrentMonth = getActualNcrForCurrentMonth(state, yearMonthCM, currentBaseSite);

        if(yearMonthPM!=null)
            actualNcrForPrevMonth = getActualNcrForCurrentMonth(state, yearMonthPM, currentBaseSite);

        if(yearMonthPBM!=null)
            actualNcrForPrevBackMonth = getActualNcrForCurrentMonth(state, yearMonthPBM, currentBaseSite);

        ncrTrendDataList.add(actualNcrForCurrentMonth);
        ncrTrendDataList.add(actualNcrForPrevMonth);
        ncrTrendDataList.add(actualNcrForPrevBackMonth);

        ncrTrendListData.setNcrTrendLevel(ncrTrendDataList);
        ncrTrendListData.setCurrentNcrGap(actualNcrForCurrentMonth.getCurrentNcrGap());
        return ncrTrendListData;
    }

    @Override
    public NCRTrendListData getNCRTrendListForOneMonth() {
        NCRTrendListData ncrTrendListData=new NCRTrendListData();
        List<NCRTrendData> ncrTrendDataList=new ArrayList<>();
        NCRTrendData ncrTrendData=new NCRTrendData();

        SclUserModel sclUser=(SclUserModel) getUserService().getCurrentUser();
        BaseSiteModel currentBaseSite=baseSiteService.getCurrentBaseSite();
        String state = sclUser.getState();
        LocalDate now = LocalDate.now();
        int year = now.getYear();//2023
        int month = now.getMonthValue();//3
        String monthInNumber=null;
        String yearMonthCM=null,yearMonthPM=null,yearMonthPBM=null;
        if (month <= 9) {
            if(month==2){
                yearMonthCM = String.valueOf(year).concat("-").concat("0").concat(String.valueOf(month));//2023-02
                yearMonthPM = String.valueOf(year-1).concat("-").concat("0").concat(String.valueOf(month-1));//2022-01
                yearMonthPBM = String.valueOf(year-1).concat("-").concat(String.valueOf("12"));//2022-12
            }else if(month==1){
                yearMonthCM = String.valueOf(year).concat("-").concat("0").concat(String.valueOf(month));//2023-01
                yearMonthPM = String.valueOf(year-1).concat("-").concat(String.valueOf("12"));//2022-12
                yearMonthPBM = String.valueOf(year-1).concat("-").concat(String.valueOf("11"));//2022-11
            }
            else {
                yearMonthCM = String.valueOf(year).concat("-").concat("0").concat(String.valueOf(month));//2023-03
                yearMonthPM = String.valueOf(year).concat("-").concat("0").concat(String.valueOf(month - 1));//2023-02
                yearMonthPBM = String.valueOf(year).concat("-").concat("0").concat(String.valueOf(month - 2));//2023-01
            }
        } else if(month==11) {
            yearMonthCM = String.valueOf(year).concat("-").concat(String.valueOf(month));//2023-11
            yearMonthPM = String.valueOf(year).concat("-").concat(String.valueOf(month-1));//2023-10
            yearMonthPBM = String.valueOf(year).concat("-").concat("0").concat(String.valueOf(month-2));//2023-09
        }
        else if(month==10) {
            yearMonthCM = String.valueOf(year).concat("-").concat(String.valueOf(month));//2023-10
            yearMonthPM = String.valueOf(year).concat("-").concat("0").concat(String.valueOf(month-1));//2023-09
            yearMonthPBM = String.valueOf(year).concat("-").concat("0").concat(String.valueOf(month-2));//2023-08
        }
        LOGGER.info(yearMonthCM+"-"+yearMonthPM+"-"+yearMonthPBM);

        Double ncrThresholdForCurrentMonth=0.0;
        if(yearMonthCM!=null)
            ncrThresholdForCurrentMonth= salesPerformanceService.getNCRThreshold(state,currentBaseSite,yearMonthCM);

        double currentNCRGap=0.0;
        double actualNcr=0.0;
        List<String> districts = new ArrayList<String>(geographicalRegionFacade.findAllDistrict(state));
        for (String district : districts) {
            List<String> allTaluka = geographicalRegionFacade.findAllTaluka(state, district);

            LocalDate today = LocalDate.now();
            LocalDate firstDayOfMonth = today.withDayOfMonth(1);
            LocalDate lastDayOfMonth = LocalDate.of(today.getYear(), today.getMonth(), today.getMonth().maxLength());
            Date startDate = Date.from(firstDayOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(lastDayOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
/*
            Date startDate = getFirstDateOfMonth(month1-1);
            Date endDate = getLastDateOfMonth(month1-1);
*/

            for (String s : allTaluka) {
                double actualNcrPerTaluka=0.0;
                double ncrSales=0.0;
                List<List<Object>> salesHistoryModelList = salesPerformanceService.getSalesHistoryModelList(s, startDate, endDate,currentBaseSite);
                //     List<List<Object>> salesHistoryModelList = salesPerformanceService.getSalesHistoryModelList(state, startDate, endDate,currentBaseSite);
                if(salesHistoryModelList!=null) {
                    double sumOfSalesQty = salesHistoryModelList.stream().filter(o -> o.get(1) != null).mapToDouble(o -> (double) o.get(1)).sum();
                    for (List<Object> objects : salesHistoryModelList) {
                        if (objects.get(0) != null && objects.get(1) != null) {
                            double ncr = (double) objects.get(0);
                            double salesQty = (double) objects.get(1);
                            ncrSales += ncr * salesQty;
                        }
                    }
                    if(ncrSales!=0.0 && sumOfSalesQty!=0.0) {
                        actualNcrPerTaluka = ncrSales / sumOfSalesQty;
                        actualNcr += actualNcrPerTaluka;
                    }
                }
            }
        }
        if(actualNcr>=0.0 && ncrThresholdForCurrentMonth>=0.0){
            if(actualNcr >= (1.1*ncrThresholdForCurrentMonth)){
                ncrTrendData.setGreenLevel(true);
                ncrTrendData.setAmberLevel(false);
                ncrTrendData.setRedLevel(false);
                currentNCRGap=actualNcr-ncrThresholdForCurrentMonth;
            } else if (actualNcr>=(1.0*ncrThresholdForCurrentMonth)) {
                ncrTrendData.setGreenLevel(false);
                ncrTrendData.setAmberLevel(true);
                ncrTrendData.setRedLevel(false);
                currentNCRGap=actualNcr-ncrThresholdForCurrentMonth;
            }else {
                ncrTrendData.setGreenLevel(false);
                ncrTrendData.setAmberLevel(false);
                ncrTrendData.setRedLevel(true);
                currentNCRGap=ncrThresholdForCurrentMonth-actualNcr;
            }
        }
        if(ncrTrendData.getAmberLevel().equals(true)){
            String ncrGap="Current NCR Gap to Red Zone ".concat(String.valueOf(currentNCRGap)).concat("/MT");
            ncrTrendListData.setShowCurrentNcrGap(ncrGap);
            ncrTrendListData.setCurrentNcrGap(currentNCRGap);
        }else if(ncrTrendData.getGreenLevel().equals(true)){
            String ncrGap="Current NCR Gap to Red Zone ".concat(String.valueOf(currentNCRGap)).concat("/MT");
            ncrTrendListData.setShowCurrentNcrGap(ncrGap);
            ncrTrendListData.setCurrentNcrGap(currentNCRGap);
        }else if(ncrTrendData.getRedLevel().equals(true)){
            String ncrGap="Current NCR Gap to Amber Zone ".concat(String.valueOf(currentNCRGap)).concat("/MT");
            ncrTrendListData.setShowCurrentNcrGap(ncrGap);
            ncrTrendListData.setCurrentNcrGap(currentNCRGap);
        }
        ncrTrendData.setMonthYear(Month.of(month).name().concat("-").concat(String.valueOf(year)));
        ncrTrendDataList.add(ncrTrendData);
        ncrTrendListData.setNcrTrendLevel(ncrTrendDataList);
        ncrTrendListData.setCurrentNcrGap(currentNCRGap);
        return ncrTrendListData;
    }

    public static Date getFirstDateOfMonth(int month){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH,
                calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }

    public static Date getLastDateOfMonth(int month){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }

    public static String getYear(Date date){
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        return yearFormat.format(date);
    }
    public static String getMonth(Date date){
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        String year=yearFormat.format(date);
        String month=monthFormat.format(date);
        String formattedMonth=month.concat("-").concat(year);
        return formattedMonth;
    }

    public static String getYearPlus(Date date){
        LocalDate localDate=LocalDate.now();
        int nextYear=localDate.plusYears(1).getYear();
        return String.valueOf(nextYear);
    }

    public static String getMonthPlus(Date date){
        LocalDate localDate=LocalDate.now();
        LocalDate nextMonth=localDate.plusMonths(1);
        //int nextYear=localDate.plusYears(1).getYear();
        int currentYear=localDate.getYear();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = nextMonth.atStartOfDay(zone);
        String month = monthFormat.format(Date.from(dateTime.toInstant()));
        String formattedMonth=month.concat("-").concat(String.valueOf(currentYear));
        return formattedMonth;
    }

    @Override
    public SalesPerformamceCockpitSaleData getSalesHistoryForDealer(String subArea) {
        SclUserModel sclUser=(SclUserModel) getUserService().getCurrentUser();
        BaseSiteModel currentBaseSite=baseSiteService.getCurrentBaseSite();
        return salesPerformanceService.getSalesHistoryForDealers(subArea, sclUser, currentBaseSite);
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public BaseSiteService getBaseSiteService() {
        return baseSiteService;
    }

    public void setBaseSiteService(BaseSiteService baseSiteService) {
        this.baseSiteService = baseSiteService;
    }

    public SalesPerformanceService getSalesPerformanceService() {
        return salesPerformanceService;
    }

    public void setSalesPerformanceService(SalesPerformanceService salesPerformanceService) {
        this.salesPerformanceService = salesPerformanceService;
    }

    public TerritoryManagementService getTerritoryManagementService() {
        return territoryManagementService;
    }

    public void setTerritoryManagementService(TerritoryManagementService territoryManagementService) {
        this.territoryManagementService = territoryManagementService;
    }

    @Override
    public ProductMixVolumeAndRatioListData getProductwiseSalesPercentRatioAndVolumeRatioForCustomer(
            SclCustomerModel customer, String customerType,List<String> territoryList,List<String> subAreaList,List<String> districtList) {
        double sumOfQuantity = 0.0,sumOfQuantity2=0.0;
        BaseSiteModel baseSite = baseSiteService.getCurrentBaseSite();
        List<List<Object>> productMixRatio =new ArrayList<>();
        ProductMixVolumeAndRatioListData productMixVolumeAndRatioListData = new ProductMixVolumeAndRatioListData();
        List<ProductMixVolumeAndRatioData> productMixVolumeAndRatioDataList = new ArrayList<>();
        if ((customer.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SP_GROUP_ID))) ||
                ((customer.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))))) {

            productMixRatio = getSalesPerformanceService().getProductwiseSalesPercentRatioAndVolumeRatioForCustomer(customer, baseSite, customerType,districtList,subAreaList);
            if(CollectionUtils.isNotEmpty(productMixRatio)) {
                sumOfQuantity = productMixRatio.stream().collect(Collectors.summingDouble(o -> (double) o.get(2)));
            }
           /* if((customer.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))))
            {
                sumOfQuantity2 = productMixRatio.stream().collect(Collectors.summingDouble(o -> (double) o.get(2)));
            }*/

            if (productMixRatio != null && !productMixRatio.isEmpty()) {
                double finalSumOfQuantity = sumOfQuantity;
                productMixRatio.forEach(objects -> {
                    ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                    if (objects != null && !objects.isEmpty()) {
                        String productCode=null;
                        if(Objects.nonNull(objects.get(3))) {
                            productCode = (String) objects.get(3);
                            LOGGER.info("Equivalence Product code:"+productCode);
                        }
                        else {
                            productCode = (String) objects.get(0);
                            LOGGER.info("Product mix non Equivalence Product code:"+productCode);
                        }
                        String productName = (String) objects.get(1);
                        double pquantityInBag1 = objects.size() > 2 ? (Double) objects.get(2) : 0.0;
                        double pquantityInBag2=0.0;
                       /* if((customer.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
                            pquantityInBag2 = objects.size() > 3 ? (Double) objects.get(3) : 0.0;
                        }*/
                        double pquantityInMT=pquantityInBag1;
                        double productSalesPercentRatio;
                        productMixVolumeAndRatioData.setProductCode(productCode);
                        productMixVolumeAndRatioData.setProductName(productName);
                        //set product sales volume
                        productMixVolumeAndRatioData.setProductVolume(pquantityInMT);
                        //set product sales % ratio
                        try {
                            productSalesPercentRatio = (pquantityInMT / finalSumOfQuantity) * 100;
                        } catch (ArithmeticException e) {
                            productSalesPercentRatio = 0.0;
                        }
                        productMixVolumeAndRatioData.setProductRatio(productSalesPercentRatio);
                        productMixVolumeAndRatioDataList.add(productMixVolumeAndRatioData);

                    }
                });
                productMixVolumeAndRatioListData.setProductVolumeAndRatio(productMixVolumeAndRatioDataList);

            }
            else
            {
                if (null != customer.getState() && !(customer.getState().isEmpty())) {
                   List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(customer.getState());
                   // List<String> productList = sclProductService.getProductsAliasForSalesPerformance();
                    if (productList != null && !productList.isEmpty()) {
                        for (String product : productList) {
                            ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                            productMixVolumeAndRatioData.setProductRatio(0.0);
                            productMixVolumeAndRatioData.setProductVolume(0.0);
                            productMixVolumeAndRatioData.setProductName(product);
                            productMixVolumeAndRatioDataList.add(productMixVolumeAndRatioData);
                        }
                        productMixVolumeAndRatioListData.setProductVolumeAndRatio(productMixVolumeAndRatioDataList);
                    }
                }
            }
        }else if((customer.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))){
            productMixRatio = sclSalesSummaryService.getProductMixSalesDetailsMTD(customer, territoryList);
            if(CollectionUtils.isNotEmpty(productMixRatio)) {
                sumOfQuantity = productMixRatio.stream().collect(Collectors.summingDouble(o -> (double) o.get(1)));
            }

            if (productMixRatio != null && !productMixRatio.isEmpty()) {
                double finalSumOfQuantity = sumOfQuantity;
                productMixRatio.forEach(objects -> {
                    ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                    if (objects != null && !objects.isEmpty()) {
                        String productCode = (String) objects.get(0);
                        String productName = (String) objects.get(0);
                        double pquantityInMT = objects.size() > 1 ? (Double) objects.get(1) : 0.0;
                        double productSalesPercentRatio;
                        productMixVolumeAndRatioData.setProductCode(productCode);
                        productMixVolumeAndRatioData.setProductName(productName);
                        //set product sales volume
                        productMixVolumeAndRatioData.setProductVolume(pquantityInMT);
                        //set product sales % ratio
                        try {
                            productSalesPercentRatio = (pquantityInMT / finalSumOfQuantity) * 100;
                        } catch (ArithmeticException e) {
                            productSalesPercentRatio = 0.0;
                        }
                        productMixVolumeAndRatioData.setProductRatio(productSalesPercentRatio);
                        productMixVolumeAndRatioDataList.add(productMixVolumeAndRatioData);

                    }
                });
                productMixVolumeAndRatioListData.setProductVolumeAndRatio(productMixVolumeAndRatioDataList);

            }
            else
            {
                if (null != customer.getState() && !(customer.getState().isEmpty())) {
                    //List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(customer.getState());
                    List<String> productList = sclProductService.getProductsAliasForSalesPerformance();
                    if (productList != null && !productList.isEmpty()) {
                        for (String product : productList) {
                            ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                            productMixVolumeAndRatioData.setProductRatio(0.0);
                            productMixVolumeAndRatioData.setProductVolume(0.0);
                            productMixVolumeAndRatioData.setProductName(product);
                            productMixVolumeAndRatioDataList.add(productMixVolumeAndRatioData);
                        }
                        productMixVolumeAndRatioListData.setProductVolumeAndRatio(productMixVolumeAndRatioDataList);
                    }
                }
            }
        }
        return productMixVolumeAndRatioListData;
    }

    @Override
    public ProductMixVolumeAndRatioListData getProductwiseSalesPercentRatioAndVolumeRatioForCust(String filter, int month1, int year1, List<String> territoryList,List<String> subAreaList,List<String> districtList) {
        SclCustomerModel currentUser = (SclCustomerModel) userService.getCurrentUser();
        ProductMixVolumeAndRatioListData data=new ProductMixVolumeAndRatioListData();
        String customerType=null;

        if(StringUtils.isBlank(filter)) {
            //productMixVolumeAndRatioListData = getProductMixVolumeAndRatioListDataForMTD(subArea, sclUser, baseSite, productMixVolumeAndRatioListData,null);

            if (month1 != 0 && year1 != 0) {
                data = getProductMixVolumeAndRatioListDataForCustomerMonthPicker(currentUser, baseSiteService.getCurrentBaseSite(), data, month1, year1,territoryList,subAreaList,districtList);
                if (data != null) return data;
            }
            else {
                data= getProductwiseSalesPercentRatioAndVolumeRatioForCustomer(currentUser, customerType, territoryList,subAreaList,districtList);
                if (data != null) return data;
            }
        }
        else if(filter.equalsIgnoreCase(FILTER_MTD)) {
            data= getProductwiseSalesPercentRatioAndVolumeRatioForCustomer(currentUser, customerType, territoryList,subAreaList,districtList);
            if (data != null) return data;
        }
        else if(filter.equalsIgnoreCase(FILTER_YTD)) {
            data = getProductMixVolumeAndRatioListDataForYTD(null, baseSiteService.getCurrentBaseSite(), data, currentUser,territoryList,subAreaList,districtList);
            if (data != null) return data;
        }
        return data;
    }

    @Override
    public NetworkCounterShareData getDealerCounterShareForNetwork(String filter, int month1, int year1,List<String> subAreaList,List<String> doList,List<String> territoryList) {
        SclUserModel sclUser=(SclUserModel) getUserService().getCurrentUser();
        CounterShareResponseData responseData=null;
        double sumOfNumeratorSales=0.0,sumOfPotential=0.0;
       /* BaseSiteModel baseSite = getBaseSiteService().getCurrentBaseSite();
        double actualSaleMTD =0.0, actualSaleYTD=0.0, selectedMonthYearSale=0.0;
        actualSaleMTD = sclSalesSummaryService.getCurrentMonthSales(sclUser, territoryList);
               // actualSaleMTD = getSalesPerformanceService().getActualTargetForSalesMTD(sclUser, baseSite,doList,subAreaList);
        actualSaleYTD = sclSalesSummaryService.getCurrentFySales(sclUser, territoryList);
                // actualSaleYTD = getSalesPerformanceService().getActualTargetForSalesYTD(sclUser, baseSite,doList,subAreaList);
        selectedMonthYearSale= sclSalesSummaryService.getSalesByMonth(sclUser, month1, year1, territoryList);
       // selectedMonthYearSale=getSalesPerformanceService().getActualTargetSalesForSelectedMonthAndYear(sclUser, baseSite, month1, year1,doList,subAreaList);*/
        List<SclCustomerModel> customerFilteredList=new ArrayList<>();
        double counterPotential=0.0;
        double counterShare=0.0;
        Double counterSharePercent = null;
        //customerFilteredList = territoryManagementService.getDealersForSubArea();
        customerFilteredList=salesPerformanceService.getCustomersByLeadType(DEALER,territoryList,null,null);
        //if territroyList not empty then filter customerFilteredList based upon territory
        customerFilteredList=salesPerformanceService.getCustomersByTerritoryCode(customerFilteredList,territoryList);
        /*if(customerFilteredList!=null && !customerFilteredList.isEmpty())
            for (SclCustomerModel sclCustomerModel : customerFilteredList) {
                counterPotential += sclCustomerModel.getCounterPotential()!=null?sclCustomerModel.getCounterPotential():0.0;
                if(sclCustomerModel.getLastCounterVisit()!=null)
                    counterShare += sclCustomerModel.getLastCounterVisit().getCounterShare()!=null ? sclCustomerModel.getLastCounterVisit().getCounterShare() :0.0;
            }*/



        NetworkCounterShareData data = new NetworkCounterShareData();
        if(StringUtils.isBlank(filter)) {
            if (month1 != 0 && year1 != 0) {
                if (customerFilteredList != null && !customerFilteredList.isEmpty()) {
                    for (SclCustomerModel customer : customerFilteredList) {
                        CounterShareData counterShareData=new CounterShareData();
                        counterShareData.setDealerCode(customer.getUid());
                        counterShareData.setMonth(month1);
                        counterShareData.setYear(year1);
                        responseData = getCounterShareData(counterShareData);
                        if(Objects.nonNull(responseData) && responseData.getPotential()>0){
                            sumOfNumeratorSales+=responseData.getNumeratorSales();
                            sumOfPotential+=responseData.getPotential();
                        }
                        LOGGER.info(" Counter Share"+responseData.getCounterShare() + " Potential:"+responseData.getPotential()+ " Numerator:"+responseData.getNumeratorSales()+ " SelfBrandSale"+responseData.getSelfBrandSale()+ " TotalSales "+responseData.getTotalSales());
                    }
                }

                data.setPotential(sumOfPotential);
                data.setActual(sumOfNumeratorSales);

                if (sumOfNumeratorSales != 0.0 && sumOfPotential != 0.0) {
                    counterSharePercent = (sumOfNumeratorSales / sumOfPotential) * 100;
                    data.setCounterSharePercentage(counterSharePercent);
                } else {
                    data.setCounterSharePercentage(0.0);
                }
            }else{

                    if (customerFilteredList != null && !customerFilteredList.isEmpty()) {
                        for (SclCustomerModel customer : customerFilteredList) {
                            CounterShareData counterShareData=new CounterShareData();
                            counterShareData.setDealerCode(customer.getUid());
                            counterShareData.setMonth(LocalDate.now().getMonthValue());
                            counterShareData.setYear(LocalDate.now().getYear());
                            responseData = getCounterShareData(counterShareData);
                            if(Objects.nonNull(responseData) && responseData.getPotential()>0){
                                sumOfNumeratorSales+=responseData.getNumeratorSales();
                                sumOfPotential+=responseData.getPotential();
                            }
                            LOGGER.info(" Counter Share"+responseData.getCounterShare() + " Potential:"+responseData.getPotential()+ " Numerator:"+responseData.getNumeratorSales()+ " SelfBrandSale"+responseData.getSelfBrandSale()+ " TotalSales "+responseData.getTotalSales());
                        }
                    }


                    data.setPotential(sumOfPotential);
                    data.setActual(sumOfNumeratorSales);

                    if (sumOfNumeratorSales != 0.0 && sumOfPotential != 0.0) {
                        counterSharePercent = (sumOfNumeratorSales / sumOfPotential) * 100;
                        data.setCounterSharePercentage(counterSharePercent);
                    } else {
                        data.setCounterSharePercentage(0.0);
                    }
            }
        }

        else if (filter.contains(FILTER_MTD)) {

            if (customerFilteredList != null && !customerFilteredList.isEmpty()) {
                for (SclCustomerModel customer : customerFilteredList) {
                    CounterShareData counterShareData=new CounterShareData();
                    counterShareData.setDealerCode(customer.getUid());
                    counterShareData.setMonth(LocalDate.now().getMonthValue());
                    counterShareData.setYear(LocalDate.now().getYear());
                    responseData = getCounterShareData(counterShareData);
                    if(Objects.nonNull(responseData) && responseData.getPotential()>0){
                        sumOfNumeratorSales+=responseData.getNumeratorSales();
                        sumOfPotential+=responseData.getPotential();
                    }
                    LOGGER.info(" Counter Share"+responseData.getCounterShare() + " Potential:"+responseData.getPotential()+ " Numerator:"+responseData.getNumeratorSales()+ " SelfBrandSale"+responseData.getSelfBrandSale()+ " TotalSales "+responseData.getTotalSales());
                }
            }


            data.setPotential(sumOfPotential);
            data.setActual(sumOfNumeratorSales);

            if (sumOfNumeratorSales != 0.0 && sumOfPotential != 0.0) {
                counterSharePercent = (sumOfNumeratorSales / sumOfPotential) * 100;
                data.setCounterSharePercentage(counterSharePercent);
            } else {
                data.setCounterSharePercentage(0.0);
            }
        }
        else if (filter.contains(FILTER_YTD))
        {

            if (customerFilteredList != null && !customerFilteredList.isEmpty()) {
                for (SclCustomerModel customer : customerFilteredList) {
                    CounterShareData counterShareData=new CounterShareData();
                    counterShareData.setDealerCode(customer.getUid());
                    counterShareData.setMonth(LocalDate.now().getMonthValue());
                    counterShareData.setYear(LocalDate.now().getYear());
                    responseData = getCounterShareData(counterShareData);
                    if(Objects.nonNull(responseData) && responseData.getPotential()>0){
                        sumOfNumeratorSales+=responseData.getNumeratorSales();
                        sumOfPotential+=responseData.getPotential();
                    }
                    LOGGER.info(" Counter Share"+responseData.getCounterShare() + " Potential:"+responseData.getPotential()+ " Numerator:"+responseData.getNumeratorSales()+ " SelfBrandSale"+responseData.getSelfBrandSale()+ " TotalSales "+responseData.getTotalSales());
                }
            }


            data.setPotential(sumOfPotential);
            data.setActual(sumOfNumeratorSales);

            if (sumOfNumeratorSales != 0.0 && sumOfPotential != 0.0) {
                counterSharePercent = (sumOfNumeratorSales / sumOfPotential) * 100;
                data.setCounterSharePercentage(counterSharePercent);
            } else {
                data.setCounterSharePercentage(0.0);
            }
        }
        /*{
            data.setPotential(counterPotential);
            data.setActual(actualSaleYTD);
            if (actualSaleYTD != 0.0 && counterPotential != 0.0) {
                counterSharePercent = (actualSaleYTD / counterPotential) * 100;
                data.setCounterSharePercentage(counterSharePercent);
            }
            else {
                data.setCounterSharePercentage(0.0);
            }
        }*/
        return data;
    }

    @Override
    public List<SalesPerformNetworkDetailsData> getDealerDetailedSummaryListDataForSP(List<SclCustomerModel> dealerList) {
        {
            List<List<Object>> outstandingAmount = djpVisitDao.getDealerOutstandingAmount(dealerList);
            Map<String, Double>  mapOutstanding = outstandingAmount.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                    .collect(Collectors.toMap(each->((String)each.get(0)), each->(Double)each.get(1)));

            String monthName = Month.of(LocalDate.now().getMonthValue()).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            String formattedMonth = monthName.concat("-").concat(String.valueOf(LocalDate.now().getYear()));
            List<List<Object>> targetList = salesPerformanceDao.getMonthlySalesTargetForDealerList(dealerList, formattedMonth,String.valueOf(LocalDate.now().getYear()));
            Map<String, Double>  mapTarget = targetList.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                    .collect(Collectors.toMap(each->((String)each.get(0)), each->(Double)each.get(1)));

            List<List<Object>> list = sclSalesSummaryDao.getSalesMTDforDealer(dealerList, LocalDate.now().getMonthValue(), LocalDate.now().getYear());
            Map<String, Double>  map = list.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                    .collect(Collectors.toMap(each->((String)each.get(0)), each->(Double)each.get(1)));

            List<List<Object>> listLastMonth = sclSalesSummaryDao.getSalesMTDforDealer(dealerList, LocalDate.now().minusMonths(1).getMonthValue(), LocalDate.now().getYear());
            Map<String, Double>  mapLastMonth = listLastMonth.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                    .collect(Collectors.toMap(each->(String)each.get(0), each->(Double)each.get(1)));

            LocalDate currentYearCurrentDate= LocalDate.now();
            LocalDate currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear(), Month.APRIL, 1);
            if(currentYearCurrentDate.getMonth().compareTo(Month.APRIL)<0) {
                currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear()-1, Month.APRIL, 1);
            }
            LocalDate lastYearCurrentDate= currentYearCurrentDate.minusYears(1);//2022-04-02

            LocalDate lastFinancialYearDate = currentFinancialYearDate.minusYears(1);//2022-04-01

            Map<String, Integer> mapYear = sclSalesSummaryService.currentFySalesDates();
            List<List<Object>> currentYTD = sclSalesSummaryDao.getSalesYTDforDealer(dealerList, mapYear.get(START_MONTH), mapYear.get(START_YEAR),mapYear.get(END_MONTH),mapYear.get(END_YEAR));
            Map<String, Double>  mapCurrentYTD = currentYTD.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                    .collect(Collectors.toMap(each->((String)each.get(0)),each->(Double)each.get(1)));

            Map<String, Integer> mapLastYear = sclSalesSummaryService.currentFySalesDates(lastYearCurrentDate.getYear(),lastFinancialYearDate.getYear());
            List<List<Object>> lastYTD = sclSalesSummaryDao.getSalesYTDforDealer(dealerList,  mapLastYear.get(START_MONTH), mapLastYear.get(START_YEAR),mapLastYear.get(END_MONTH),mapLastYear.get(END_YEAR));

            Map<String, Double>  mapLastYTD = lastYTD.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                    .collect(Collectors.toMap(each->((String)each.get(0)), each->(Double)each.get(1)));

            List<SalesPerformNetworkDetailsData> summaryDataList = new ArrayList<>();
            dealerList.forEach(dealer -> {
                SalesPerformNetworkDetailsData dealerCurrentNetworkData = new SalesPerformNetworkDetailsData();
                var subAraMappinglist = territoryManagementService.getTerritoriesForCustomer(dealer);
                dealerCurrentNetworkData.setCode(dealer.getUid());
                if(dealer.getContactNumber()!=null){
                    dealerCurrentNetworkData.setContactNumber(dealer.getMobileNumber());
                }
                dealerCurrentNetworkData.setName(dealer.getName());
                dealerCurrentNetworkData.setCustomerNo(dealer.getCustomerNo()!=null?dealer.getCustomerNo():"-");
                B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
                double salesMtd=0.0,salesQuantity=0.0,salesQuantityLastMonth=0.0,salesMtdLastMonth=0.0,salesLastYearQty=0.0,salesLastYear=0.0,salesCurrentYearQty=0.0,salesCurrentYear=0.0,totalOutstanding=0.0,targetCurrentMonth=0.0;

                dealerCurrentNetworkData.setPotential(String.valueOf(dealer.getCounterPotential()!=null?dealer.getCounterPotential():"0.0"));
                if(map.containsKey(dealer.getUid())) {
                    salesMtd = map.get(dealer.getUid());
                }

                if(mapLastMonth.containsKey(dealer.getUid())) {
                    if(mapLastMonth.get(dealer.getUid())!=null){
                        salesMtdLastMonth = mapLastMonth.get(dealer.getUid());
                    }
                }
                if(mapTarget.containsKey(dealer.getUid())) {
                    if(mapTarget.get(dealer.getUid())!=null){
                        targetCurrentMonth = mapTarget.get(dealer.getUid());
                    }
                }

                if(mapOutstanding.containsKey(dealer.getUid())) {
                    if(mapOutstanding.get(dealer.getUid())!=null){
                        totalOutstanding = mapOutstanding.get(dealer.getUid());
                    }
                }

                salesQuantity = salesMtd;
                salesQuantityLastMonth = salesMtdLastMonth ;

                SalesQuantityData sales = new SalesQuantityData();
                sales.setActual(salesQuantity);
                sales.setCurrent(salesQuantity);
                sales.setLastMonth(salesQuantityLastMonth);
                dealerCurrentNetworkData.setSalesQuantity(sales);

                if (dealer.getLastLiftingDate() != null) {
                    dealerCurrentNetworkData.setDaySinceLastLifting(salesPerformanceService.getDaysFromLastOrder(dealer));
                    dealerCurrentNetworkData.setDaysSinceLastOrder(salesPerformanceService.getDaysFromLastOrder(dealer));
                } else {
                    dealerCurrentNetworkData.setDaysSinceLastOrder(String.valueOf("0"));
                    dealerCurrentNetworkData.setDaySinceLastLifting(String.valueOf("0"));
                }

                if(mapCurrentYTD.containsKey(dealer.getUid())) {
                    salesCurrentYear = mapCurrentYTD.get(dealer.getUid());
                }

                if(mapLastYTD.containsKey(dealer.getUid())) {
                    salesLastYear = mapLastYTD.get(dealer.getUid());
                }
                salesCurrentYearQty = (salesCurrentYear);
                salesLastYearQty = (salesLastYear);

                dealerCurrentNetworkData.setSalesYtd(df.format(salesCurrentYearQty));
                dealerCurrentNetworkData.setGrowthRateYoYPercentage(df.format(getYearToYearGrowth(salesCurrentYearQty,salesLastYearQty)));
                if(CollectionUtils.isNotEmpty(subAraMappinglist)) {
                    var subareaMaster=subAraMappinglist.get(0);
                    dealerCurrentNetworkData.setDistrict(subareaMaster.getDistrict());
                    dealerCurrentNetworkData.setTaluka(subareaMaster.getTaluka());
                }

                dealerCurrentNetworkData.setTarget(df.format(targetCurrentMonth));

                dealerCurrentNetworkData.setOutstandingAmount(df.format(totalOutstanding));

                dealerCurrentNetworkData.setCounterSharePercentage(salesPerformanceService.getCounterShareForDealer(dealer,LocalDate.now().getMonthValue(), LocalDate.now().getYear()));

                summaryDataList.add(dealerCurrentNetworkData);
            });
            //AtomicInteger rank=new AtomicInteger(1);
            if(CollectionUtils.isNotEmpty(summaryDataList)) {
                return summaryDataList.stream().filter(obj->Objects.nonNull(obj.getName())).sorted(Comparator.comparing(SalesPerformNetworkDetailsData::getName)).collect(Collectors.toList());
            }else{
                return  summaryDataList;
            }
        }
    }

    private Double getYearToYearGrowth(double salesCurrentYearQty, double salesLastYearQty){
        if(salesLastYearQty>0) {
            return   (((salesCurrentYearQty - salesLastYearQty) / salesLastYearQty) * 100);
        }
        return 0.0;
    }
    @Override
    public List<SalesPerformNetworkDetailsData> getRetailerDetailedSummaryListDataForSP(List<SclCustomerModel> retailerList) {
        return salesPerformanceService.getRetailerDetailedSummaryListDataForSP(retailerList);
    }
    @Override
    public SalesPerformNetworkDetailsListData getListOfAllDealerRetailerInfluencers(String fields, String leadType, String filter,List<String> territoryList,List<String> subAreaList,List<String> districtList) {
        List<TerritoryMasterModel> territoryMasterModels = (List<TerritoryMasterModel>) territoryMasterService.getCurrentTerritory();
        LOGGER.info(String.format("territoryMasterModels:: %s", territoryMasterModels));

        List<SclCustomerModel> customerFilteredList = salesPerformanceDao.getCurrentNetworkCustomers(leadType, null, territoryMasterModels, filter, true, false, false);
        if (CollectionUtils.isNotEmpty(customerFilteredList)) {
            LOGGER.info(String.format("Dealer-customer List size:%s", customerFilteredList.size()));
        } else {
            LOGGER.info(String.format("Dealer-customer List size is empty"));
        }

            SalesPerformNetworkDetailsListData dealerCurrentNetworkListData = new SalesPerformNetworkDetailsListData();
            if (CollectionUtils.isNotEmpty(customerFilteredList)) {
               // List<SalesPerformNetworkDetailsData> convertedList = salesPerformnceCustomerDetailsConverter.convertAll(customerFilteredList);
                //currentNetworkWsDataList.addAll(convertedList);
                    if(leadType.equalsIgnoreCase(DEALER)){
                        List<SalesPerformNetworkDetailsData> dealerDetailedSummaryListData = getDealerDetailedSummaryListDataForSP(customerFilteredList);
                        dealerDetailedSummaryListData.sort(Comparator.comparing(SalesPerformNetworkDetailsData::getName));
                        dealerCurrentNetworkListData.setNetworkDetails(dealerDetailedSummaryListData);
                    }else if(leadType.equalsIgnoreCase(RETAILER)) {
                        List<SalesPerformNetworkDetailsData> retailerDetailedSummaryListData = getRetailerDetailedSummaryListDataForSP(customerFilteredList);
                        retailerDetailedSummaryListData.sort(Comparator.comparing(SalesPerformNetworkDetailsData::getName));
                        dealerCurrentNetworkListData.setNetworkDetails(retailerDetailedSummaryListData);
                    }else if(leadType.equalsIgnoreCase(INFLUENCER)) {
                        List<SalesPerformNetworkDetailsData> influencerDetailedSummaryListData = getInfluencerDetailedSummaryListData(customerFilteredList,null,null);
                        influencerDetailedSummaryListData.sort(Comparator.comparing(SalesPerformNetworkDetailsData::getName));
                        dealerCurrentNetworkListData.setNetworkDetails(influencerDetailedSummaryListData);
                    }
            }
        return dealerCurrentNetworkListData;
    }



    @Override
    public List<SalesPerformNetworkDetailsData> getInfluencerDetailedSummaryListData(List<SclCustomerModel> influencerList,List<String> doList,List<String> territoryList) {
        return  salesPerformanceService.getInfluencerDetailedSummaryListData(influencerList,doList,territoryList);
    }
    private void computeRankForRetailer(List<SalesPerformNetworkDetailsData> currentNetworkWsDataList) {
        AtomicInteger rank=new AtomicInteger(1);
        currentNetworkWsDataList.stream().sorted(Collections.reverseOrder(Comparator.comparing(nw -> nw.getSalesQuantity().getRetailerSaleQuantity()))).forEach(data->data.setRank(String.valueOf(rank.getAndIncrement())));
    }
    @Override
    public SalesPerformNetworkDetailsListData getListOfAllRetailerInfluencersForDealer(String fields, String leadType, String filter, List<String> doList, List<String> subAreaList) {
        return salesPerformanceService.getListOfAllRetailerInfluencersForDealer(fields,null,leadType,filter,null,null);
    }

    @Override
    public ProductMixVolumeAndRatioListData getProductMixVolumeAndRatioListDataMTDForCustomer(SclUserModel sclUser, BaseSiteModel baseSite, ProductMixVolumeAndRatioListData productMixVolumeAndRatioListData, List<String> doList,List<String> subAreaList, SclCustomerModel customer) {
        double sumOfQuantity = 0.0;

        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) +1 ;
        int year = cal.get(Calendar.YEAR);
        List<List<Object>> productMixRatio = salesPerformanceDao.getProductwiseSalesPercentRatioAndVolumeRatioForMonthPickerForDealer(sclUser, baseSite, month, year, doList, subAreaList, customer);
        List<ProductMixVolumeAndRatioData> productMixVolumeAndRatioDataList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(productMixRatio)) {
            sumOfQuantity = productMixRatio.stream().filter(o -> Objects.nonNull(o) && Objects.nonNull(o.get(2))).mapToDouble(o -> Objects.nonNull(o.get(2)) ? (double) o.get(2) : 0.0).sum();
        }
        //sumOfQuantity = productMixRatio.stream().collect(Collectors.summingDouble(o -> (double) o.get(2)));

        if (productMixRatio != null && !productMixRatio.isEmpty()) {
            double finalSumOfQuantity = sumOfQuantity;
            productMixRatio.forEach(objects -> {
                ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                if (objects != null && !objects.isEmpty()) {
                    String productCode=null;
                    if(Objects.nonNull(objects.get(3)))
                         productCode = (String) objects.get(3);
                    else
                         productCode = (String) objects.get(0);
                    String productName = (String) objects.get(1);
                    double productSalesPercentRatio=0.0,pquantityInMT=0.0;
                    productMixVolumeAndRatioData.setProductCode(productCode);
                    productMixVolumeAndRatioData.setProductName(productName);

                    //set product sales volume
                    if(objects.get(2)!=null) {
                        pquantityInMT = objects.size() > 2 ? (Double) objects.get(2) : 0.0;
                    }
                    productMixVolumeAndRatioData.setProductVolume(pquantityInMT);
                    //set product sales % ratio
                    try {
                        productSalesPercentRatio = (pquantityInMT / finalSumOfQuantity) * 100;
                    } catch (ArithmeticException e) {
                        productSalesPercentRatio = 0.0;
                    }
                    productMixVolumeAndRatioData.setProductRatio(productSalesPercentRatio);
                    productMixVolumeAndRatioDataList.add(productMixVolumeAndRatioData);

                }
            });
            productMixVolumeAndRatioListData.setProductVolumeAndRatio(productMixVolumeAndRatioDataList);
            return productMixVolumeAndRatioListData;
        } else {
            if (sclUser != null) {
                List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(sclUser.getState());
                if (productList != null && !productList.isEmpty()) {
                    for (String product : productList) {
                        ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                        productMixVolumeAndRatioData.setProductRatio(0.0);
                        productMixVolumeAndRatioData.setProductVolume(0.0);
                        productMixVolumeAndRatioData.setProductName(product);
                        productMixVolumeAndRatioDataList.add(productMixVolumeAndRatioData);
                    }
                    productMixVolumeAndRatioListData.setProductVolumeAndRatio(productMixVolumeAndRatioDataList);
                }
            }
            else {
                if(customer.getState()!=null) {
                    List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(customer.getState());
                    if (productList != null && !productList.isEmpty()) {
                        for (String product : productList) {
                            ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                            productMixVolumeAndRatioData.setProductRatio(0.0);
                            productMixVolumeAndRatioData.setProductVolume(0.0);
                            productMixVolumeAndRatioData.setProductName(product);
                            productMixVolumeAndRatioDataList.add(productMixVolumeAndRatioData);
                        }
                        productMixVolumeAndRatioListData.setProductVolumeAndRatio(productMixVolumeAndRatioDataList);
                    }
                }
            }
        }
        return productMixVolumeAndRatioListData;
    }

    @Override
    public NetworkCounterShareData getDealerCounterShareForSP(String filter, int month1, int year1, List<String> doList, List<String> subAreaList) {
        SclCustomerModel currentUser = (SclCustomerModel) getUserService().getCurrentUser();
        BaseSiteModel baseSite = getBaseSiteService().getCurrentBaseSite();
        double actualSaleMTD = 0.0, actualSaleYTD = 0.0, selectedMonthYearSale = 0.0;
        double counterPotential = 0.0;
        double counterShare = 0.0;
        Double counterSharePercent = null;
        if (currentUser.getCounterType().equals(CounterType.SP)) {
            RequestCustomerData requestData = new RequestCustomerData();
            List<String> counterTypes = List.of(DEALER);
            requestData.setCounterType(counterTypes);
            List<SclCustomerModel> customerforUser = territoryManagementService.getCustomerforUser(requestData);
            if (customerforUser != null && !customerforUser.isEmpty()) {
                for (SclCustomerModel customer : customerforUser) {
                    LOGGER.info(String.format("Scl customer Model PK:%s", customer));
                    counterPotential += customer.getCounterPotential() != null ? customer.getCounterPotential() : 0.0;
                    if (customer.getLastCounterVisit() != null)
                        counterShare += customer.getLastCounterVisit().getCounterShare() != null ? customer.getLastCounterVisit().getCounterShare() : 0.0;
                }
            }
            if (customerforUser != null && !customerforUser.isEmpty()) {
                for(SclCustomerModel customer: customerforUser){
                    actualSaleMTD = sclSalesSummaryService.getCurrentMonthSales(customer, subAreaList);
                    actualSaleYTD = sclSalesSummaryService.getCurrentFySales(customer, subAreaList);
                    selectedMonthYearSale = sclSalesSummaryService.getSalesByMonth(customer, month1, year1, subAreaList);
                }
              //  actualSaleMTD = getSalesPerformanceService().getActualTargetForSalesSPMTD(customerforUser);
              //  actualSaleYTD = getSalesPerformanceService().getActualTargetForSalesYTDSP(customerforUser, baseSite);
              //  selectedMonthYearSale = getSalesPerformanceService().getActualTargetForSalesForMonthSP(customerforUser, year1, month1);
                LOGGER.info(String.format("ActualSalesMTD:%s and ActualSaleYTD:%s and SelectedMonthYearSale:%s",actualSaleMTD,actualSaleYTD,selectedMonthYearSale));

            }
        }
        NetworkCounterShareData data = new NetworkCounterShareData();
        if (StringUtils.isBlank(filter)) {
            if (month1 != 0 && year1 != 0) {
                data.setPotential(counterPotential);
                data.setActual(selectedMonthYearSale);

                if (selectedMonthYearSale != 0.0 && counterPotential != 0.0) {
                    counterSharePercent = (selectedMonthYearSale / counterPotential) * 100;
                    data.setCounterSharePercentage(counterSharePercent);
                } else {
                    data.setCounterSharePercentage(0.0);
                }
            }else{
                data.setPotential(counterPotential);
                data.setActual(actualSaleMTD);
                counterSharePercent = (actualSaleMTD / counterPotential) * 100;
                data.setCounterSharePercentage(counterSharePercent);
            }
        } else if (filter.contains(FILTER_MTD)) {
            data.setPotential(counterPotential);
            data.setActual(actualSaleMTD);
            if (actualSaleMTD != 0.0 && counterPotential != 0.0) {
                counterSharePercent = (actualSaleMTD / counterPotential) * 100;
                data.setCounterSharePercentage(counterSharePercent);
            } else {
                data.setCounterSharePercentage(0.0);
            }

        } else if (filter.contains(FILTER_YTD)) {
            data.setPotential(counterPotential);
            data.setActual(actualSaleYTD);
            if (actualSaleYTD != 0.0 && counterPotential != 0.0) {
                counterSharePercent = (actualSaleYTD / counterPotential) * 100;
                data.setCounterSharePercentage(counterSharePercent);
            } else {
                data.setCounterSharePercentage(0.0);
            }
        }
        return data;
    }

    @Override
    public LeaderboardListData getSpSalesLeaderboardEmpList(String filter,String district) {
        SclCustomerModel sclUser = (SclCustomerModel) getUserService().getCurrentUser();
        List<SclCustomerModel> customerforUser = new ArrayList<>();
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        LeaderboardListData list = new LeaderboardListData();
        Date startDate;
        Date endDate;
        Date currentDate = Calendar.getInstance().getTime();
        int monthInNumber = currentDate.getMonth();
        List<LeaderboardData> leaderboardDataList = new ArrayList<>();
        Map<String, Double> unsortedLeaderboard = new HashMap<>();
        Map<String, Double> unsortedPremiumSalesLeaderboard = new HashMap<>();
        Map<String, Integer> unsortedCashDiscountAvailedPercentage = new HashMap<>();
        double actualSaleMTD = 0.0, actualSaleYTD = 0.0, achievementPercentage = 0.0;
        double target = 0.0;
        double counterPotential = 0.0;
        double counterShare = 0.0;
        int outstandingDays =0;
        list.setEmpCode(sclUser.getUid());
        list.setEmpName(sclUser.getName());
        DistrictMasterModel district1 = null;
        if(StringUtils.isNotBlank(district))
        {
            FilterDistrictData filterDistrictData=new FilterDistrictData();
            filterDistrictData.setDistrictCode(district);
            DistrictMasterModel districtMasterModel=salesPerformanceService.getDistrictForSP(filterDistrictData);
            List<SclCustomerModel> spForDistrict = salesPerformanceService.getSPForDistrict(districtMasterModel);
            for (SclCustomerModel sclCustomerModel : spForDistrict) {
                if (sclCustomerModel.getCounterType().equals(CounterType.SP)) {
                    RequestCustomerData requestData = new RequestCustomerData();
                    List<String> counterTypes = List.of(DEALER);
                    requestData.setCounterType(counterTypes);
                    customerforUser = salesPerformanceService.getCustomerForSp(sclCustomerModel);
                    LOGGER.info(String.format("Size of customerForuser %s",customerforUser.size()));
                    List<String> customerNo = new ArrayList<>();
                    for(SclCustomerModel customer : customerforUser)
                    {
                        if(customer.getCustomerNo()!=null)
                            customerNo.add(customer.getCustomerNo());
                    }
                    if (customerforUser != null && !customerforUser.isEmpty()) {
                        if (filter.equals(FILTER_MTD)) {
                            for(SclCustomerModel customer: customerforUser){
                                actualSaleMTD = sclSalesSummaryService.getCurrentMonthSales(customer, Collections.emptyList());
                            }
                          //  actualSaleMTD = getSalesPerformanceService().getActualTargetForSalesSPMTDSearch(customerforUser);
                            LOGGER.info(String.format("SclCustomer :: %s sales :: %s",sclCustomerModel.getUid(), actualSaleMTD));
                            unsortedLeaderboard.put(sclCustomerModel.getUid(), actualSaleMTD);
                            target = getSalesPerformanceService().getMonthlySalesTargetForSP(customerforUser);
                            LOGGER.info(String.format("SclCustomer :: %s target :: %s",sclCustomerModel.getUid(), target));
                            if (actualSaleMTD != 0.0 && target != 0.0) {
                                achievementPercentage = (actualSaleMTD / target) * 100;
                            }
                            LOGGER.info(String.format("SclCustomer :: %s Achievement :: %s",sclCustomerModel.getUid(), achievementPercentage));
                            unsortedPremiumSalesLeaderboard.put(sclCustomerModel.getUid(), achievementPercentage);
                            double totalOutstanding =0.0;
                            if(!customerNo.isEmpty())
                            {
                                totalOutstanding =sclUserDao.getOutstandingAmountForSO(customerNo);
                            }

                            double dailyAverageSales = collectionDao.getDailyAverageSalesForListOfDealers(customerNo);
                            if(dailyAverageSales!=0.0)
                            {
                                outstandingDays = (int) (totalOutstanding/dailyAverageSales);
                            }
                            unsortedCashDiscountAvailedPercentage.put(sclCustomerModel.getUid(), outstandingDays);
                            LOGGER.info(String.format("SclCustomer :: %s OutstandingDays :: %s",sclCustomerModel.getUid(), outstandingDays));
                        } else if (filter.equals(FILTER_YTD)) {
                            for(SclCustomerModel customer: customerforUser){
                            actualSaleYTD = sclSalesSummaryService.getCurrentFySales(customer, Collections.emptyList());
                            }
                            //actualSaleYTD = getSalesPerformanceService().getActualTargetForSalesYTDSP(customerforUser, currentBaseSite);
                            LOGGER.info(String.format("SclCustomer :: %s sales :: %s",sclCustomerModel.getUid(), actualSaleMTD));
                            unsortedLeaderboard.put(sclCustomerModel.getUid(), actualSaleYTD);
                            target = getSalesPerformanceService().getAnnualSalesTargetForSP(customerforUser);
                            LOGGER.info(String.format("SclCustomer :: %s sales :: %s",sclCustomerModel.getUid(), target));
                            if (actualSaleYTD != 0.0 && target != 0.0) {
                                achievementPercentage = (actualSaleYTD / target) * 100;
                            }
                            LOGGER.info(String.format("SclCustomer :: %s Acheivement :: %s",sclCustomerModel.getUid(), achievementPercentage));
                            unsortedPremiumSalesLeaderboard.put(sclCustomerModel.getUid(), achievementPercentage);
                            double totalOutstanding =0.0;
                            if(!customerNo.isEmpty())
                            {
                                totalOutstanding =sclUserDao.getOutstandingAmountForSO(customerNo);
                            }

                            double dailyAverageSales = collectionDao.getDailyAverageSalesForListOfDealers(customerNo);
                            if(dailyAverageSales!=0.0)
                            {
                                outstandingDays = (int) (totalOutstanding/dailyAverageSales);
                            }
                            //outstandingDays= Integer.parseInt(networkService.getOutstandingDaysForPromoter(sclUser));
                            unsortedCashDiscountAvailedPercentage.put(sclCustomerModel.getUid(), outstandingDays);
                            LOGGER.info(String.format("SclCustomer :: %s OutstandingDays :: %s",sclCustomerModel.getUid(), outstandingDays));
                        } else if (filter.equals("QTD")) {
                            for(SclCustomerModel customer: customerforUser){
                                actualSaleYTD = sclSalesSummaryService.getCurrentFySales(customer, Collections.emptyList());
                            }
                          //  actualSaleYTD = getSalesPerformanceService().getActualTargetForSalesYTDSP(customerforUser, currentBaseSite);
                            unsortedLeaderboard.put(sclCustomerModel.getUid(), actualSaleYTD);
                            target = getSalesPerformanceService().getAnnualSalesTargetForSP(customerforUser);
                            if (actualSaleYTD != 0.0 && target != 0.0){
                                achievementPercentage = (actualSaleYTD / target) * 100;
                            }
                            LOGGER.info(String.format("SclCustomer :: %s Acheivement :: %s",sclCustomerModel.getUid(), achievementPercentage));
                            unsortedPremiumSalesLeaderboard.put(sclCustomerModel.getUid(), achievementPercentage);
                            double totalOutstanding =0.0;
                            if(!customerNo.isEmpty())
                            {
                                totalOutstanding =sclUserDao.getOutstandingAmountForSO(customerNo);
                            }

                            double dailyAverageSales = collectionDao.getDailyAverageSalesForListOfDealers(customerNo);
                            if(dailyAverageSales!=0.0)
                            {
                                outstandingDays = (int) (totalOutstanding/dailyAverageSales);
                            }
                            //outstandingDays= Integer.parseInt(networkService.getOutstandingDaysForPromoter(sclUser));
                            unsortedCashDiscountAvailedPercentage.put(sclCustomerModel.getUid(), outstandingDays);
                            LOGGER.info(String.format("SclCustomer :: %s OutstandingDays :: %s",sclCustomerModel.getUid(), outstandingDays));
                        }


                    }
                }
            }
            Map<String, Double> stringDoubleMap = calculatePercentRank(unsortedLeaderboard);
            Map<String, Double> stringDoubleMap1 = calculatePercentRank(unsortedPremiumSalesLeaderboard);
            Map<String, Double> stringDoubleMap2 = calculatePercentileRank(unsortedCashDiscountAvailedPercentage);

            List<Map.Entry<String, Double>> entries = new ArrayList<>(stringDoubleMap.entrySet());

            Collections.sort(entries, Map.Entry.comparingByValue(Comparator.reverseOrder()));

            Map<String, Double> sortedLeaderboard = new LinkedHashMap<>();

            for (Map.Entry<String, Double> entry : entries) {
                sortedLeaderboard.put(entry.getKey(), entry.getValue());

            }

            List<Map.Entry<String, Double>> entrie = new ArrayList<>(stringDoubleMap1.entrySet());

            Collections.sort(entrie, Map.Entry.comparingByValue(Comparator.reverseOrder()));

            Map<String, Double> sortedPremiumLeaderboard = new LinkedHashMap<>();

            // Iterate over the sorted entries and put them into the sortedLeaderboard
            for (Map.Entry<String, Double> entry : entrie) {
                sortedPremiumLeaderboard.put(entry.getKey(), entry.getValue());
            }

            List<Map.Entry<String, Double>> entriees = new ArrayList<>(stringDoubleMap2.entrySet());

            Collections.sort(entriees, Map.Entry.comparingByValue(Comparator.reverseOrder()));

            Map<String, Double> sortedOutstandingLeaderboard = new LinkedHashMap<>();

            for (Map.Entry<String, Double> entry : entriees) {
                sortedOutstandingLeaderboard.put(entry.getKey(), entry.getValue());

            }


            List<LeaderboardData> sale = new ArrayList<>();
            AtomicInteger flag = new AtomicInteger();
            Integer size = sortedLeaderboard.size();
            LOGGER.info(String.format("sortedLeaderboard size is %s", String.valueOf(size)));
            int bucket = size / 10;
            sortedLeaderboard.forEach((salesOfficer, sales) -> {
                LOGGER.info(String.format("getting Sale sortedLeaderboard for customerUID :: %s sale :: %s", salesOfficer, sales));

                LeaderboardData data = new LeaderboardData();
                SclCustomerModel sclUserModel = (SclCustomerModel) userService.getUserForUID(salesOfficer);
                int s = (int) (sales * 100);
                LOGGER.info(String.format("sale is %s", String.valueOf(s)));
                int score = calculateScoreFromPercentile(s);
                LOGGER.info(String.format("score is %s", String.valueOf(score)));
                data.setScore(score);
                data.setCode(sclUserModel.getUid());
                data.setName(sclUserModel.getName());
                data.setSale(sales);
                sale.add(data);
            });

            List<LeaderboardData> premiumSale = new ArrayList<>();
            AtomicInteger flagPre = new AtomicInteger();
            Integer premiumSize = sortedPremiumLeaderboard.size();
            LOGGER.info(String.format("sortedPremiumLeaderboard size is %s", String.valueOf(premiumSize)));
            int premiumBucket = premiumSize / 10;
            sortedPremiumLeaderboard.forEach((salesOfficer, sales) -> {
                LOGGER.info(String.format("getting Sale sortedPremiumLeaderboard for customerUID :: %s sale :: %s", salesOfficer, String.valueOf(sales)));
                LeaderboardData data = new LeaderboardData();
                SclCustomerModel sclUserModel = (SclCustomerModel) userService.getUserForUID(salesOfficer);
                int s1 = (int) (sales * 100);
                LOGGER.info(String.format("sale is %s", String.valueOf(s1)));
                int score = calculateScoreFromPercentile(s1);
                LOGGER.info(String.format("score is %s", String.valueOf(score)));
                data.setScore(score);
                data.setCode(sclUserModel.getUid());
                data.setName(sclUserModel.getName());
                data.setSale(sales);
                premiumSale.add(data);
            });

            List<LeaderboardData> outstandingSale = new ArrayList<>();
            AtomicInteger flagOut = new AtomicInteger();
            Integer outstandingSize = sortedOutstandingLeaderboard.size();
            LOGGER.info(String.format("sortedOutstandingLeaderboard size is %s", String.valueOf(outstandingSize)));

            sortedOutstandingLeaderboard.forEach((salesOfficer, sales) -> {
                LOGGER.info(String.format("getting Sale sortedOutstandingLeaderboard for customerUID :: %s sale :: %s", salesOfficer, String.valueOf(sales)));
                LeaderboardData data = new LeaderboardData();
                SclCustomerModel sclUserModel = (SclCustomerModel) userService.getUserForUID(salesOfficer);
                int s1 = (int) (sales * 100);
                LOGGER.info(String.format("outstanding is %s", String.valueOf(s1)));
                int score = calculateScoreFromPercentile(s1);
                LOGGER.info(String.format("outstandingscore is %s", String.valueOf(score)));
                data.setScore(score);
                data.setCode(sclUserModel.getUid());
                data.setName(sclUserModel.getName());
                data.setSale(sales);
                outstandingSale.add(data);
            });

            int count = sale.size();
            double sumsScore = 0.0;
            Map<String, Double> finalScore = new HashMap<>();

          /*  for (LeaderboardData leader : sale) {
                for (LeaderboardData data : premiumSale) {

                    if (leader.getCode().equalsIgnoreCase(data.getCode())) {
                        sumsScore = leader.getScore() * 0.4 + data.getScore() * 0.4;
                        finalScore.put(leader.getCode(), sumsScore);
                    }

                }
            }*/
            for (LeaderboardData leader : sale) {
                for (LeaderboardData data : premiumSale) {
                    for (LeaderboardData outStanding : outstandingSale) {
                        if (leader.getCode().equalsIgnoreCase(data.getCode()) &&
                                leader.getCode().equalsIgnoreCase(outStanding.getCode())) {
                            sumsScore = leader.getScore() * 0.4 + data.getScore() * 0.4 + outStanding.getScore() * 0.2;
                            finalScore.put(leader.getCode(), sumsScore);
                        }
                    }
                }
            }

            List<Map.Entry<String, Double>> entri = new ArrayList<>(finalScore.entrySet());

            Collections.sort(entri, Map.Entry.comparingByValue(Comparator.reverseOrder()));

            Map<String, Double> finalScoreboard = new LinkedHashMap<>();

            // Iterate over the sorted entries and put them into the sortedLeaderboard
            for (Map.Entry<String, Double> entry : entri) {
                finalScoreboard.put(entry.getKey(), entry.getValue());
            }
            List<LeaderboardData> finalLeaderBoard = new ArrayList<>();
            AtomicInteger flagFinal = new AtomicInteger();
            Integer finalSize = finalScoreboard.size();
            LOGGER.info(String.format("finalScoreboard size is %s", String.valueOf(finalSize)));
            int finalBucket = finalSize / 10;
            finalScoreboard.forEach((salesOfficer, score) -> {
                LOGGER.info(String.format("getting Sale finalscoreboardQTD for customerUID :: %s sale :: %s", salesOfficer, String.valueOf(score)));
                LeaderboardData data = new LeaderboardData();
                SclCustomerModel sclUserModel = (SclCustomerModel) userService.getUserForUID(salesOfficer);
                flagFinal.addAndGet(1);
                data.setRank(flagFinal.get());
                data.setScore(score.intValue());
                data.setCode(sclUserModel.getUid());
                data.setName(sclUserModel.getName());
                finalLeaderBoard.add(data);
            });

            list.setSalesList(finalLeaderBoard);

        }

     /*   if(StringUtils.isBlank(district)) {
            List<DistrictMasterModel> districtForSP = salesPerformanceService.getDistrictForSP(sclUser);
            if (districtForSP != null && !districtForSP.isEmpty()) {
                for (DistrictMasterModel districtMasterModel : districtForSP) {
                    list.setDistrict(districtMasterModel.getName());
                    List<SclCustomerModel> spForDistrict = salesPerformanceService.getSPForDistrict(districtMasterModel);
                    for (SclCustomerModel sclCustomerModel : spForDistrict) {
                        if (sclCustomerModel.getCounterType().equals(CounterType.SP)) {
                            RequestCustomerData requestData = new RequestCustomerData();
                            List<String> counterTypes = List.of(DEALER);
                            requestData.setCounterType(counterTypes);
                            customerforUser = salesPerformanceService.getCustomerForSp(sclCustomerModel);
                            if (customerforUser != null && !customerforUser.isEmpty()) {
                                if (filter.equals(FILTER_MTD)) {
                                    actualSaleMTD = getSalesPerformanceService().getActualTargetForSalesSPMTD(customerforUser);
                                    unsortedLeaderboard.put(sclCustomerModel.getUid(), actualSaleMTD);
                                    target = getSalesPerformanceService().getMonthlySalesTargetForSP(customerforUser);
                                    if (actualSaleMTD != 0.0 && target != 0.0) {
                                        achievementPercentage = (actualSaleMTD / target) * 100;
                                    }
                                    unsortedPremiumSalesLeaderboard.put(sclCustomerModel.getUid(), achievementPercentage);
                                } else if (filter.equals(FILTER_YTD)) {
                                    actualSaleYTD = getSalesPerformanceService().getActualTargetForSalesYTDSP(customerforUser, currentBaseSite);
                                    unsortedLeaderboard.put(sclCustomerModel.getUid(), actualSaleYTD);
                                    target = getSalesPerformanceService().getAnnualSalesTargetForSP(customerforUser);
                                    if (actualSaleYTD != 0.0 && target != 0.0) {
                                        achievementPercentage = (actualSaleYTD / target) * 100;
                                    }
                                    unsortedPremiumSalesLeaderboard.put(sclCustomerModel.getUid(), achievementPercentage);
                                } else if (filter.equals("QTD")) {
                                    actualSaleYTD = getSalesPerformanceService().getActualTargetForSalesYTDSP(customerforUser, currentBaseSite);
                                    unsortedLeaderboard.put(sclCustomerModel.getUid(), actualSaleYTD);
                                    target = getSalesPerformanceService().getAnnualSalesTargetForSP(customerforUser);
                                    if (actualSaleYTD != 0.0 && target != 0.0)
                                        achievementPercentage = (actualSaleYTD / target) * 100;
                                    unsortedPremiumSalesLeaderboard.put(sclCustomerModel.getUid(), achievementPercentage);
                                }


                            }
                        }
                    }


                    Map<String, Double> stringDoubleMap = calculatePercentRank(unsortedLeaderboard);
                    Map<String, Double> stringDoubleMap1 = calculatePercentRank(unsortedPremiumSalesLeaderboard);

                    List<Map.Entry<String, Double>> entries = new ArrayList<>(stringDoubleMap.entrySet());

                    Collections.sort(entries, Map.Entry.comparingByValue(Comparator.reverseOrder()));

                    Map<String, Double> sortedLeaderboard = new LinkedHashMap<>();

                    for (Map.Entry<String, Double> entry : entries) {
                        sortedLeaderboard.put(entry.getKey(), entry.getValue());

                    }

                    List<Map.Entry<String, Double>> entrie = new ArrayList<>(stringDoubleMap1.entrySet());

                    Collections.sort(entrie, Map.Entry.comparingByValue(Comparator.reverseOrder()));

                    Map<String, Double> sortedPremiumLeaderboard = new LinkedHashMap<>();

                    // Iterate over the sorted entries and put them into the sortedLeaderboard
                    for (Map.Entry<String, Double> entry : entrie) {
                        sortedPremiumLeaderboard.put(entry.getKey(), entry.getValue());
                    }


                    List<LeaderboardData> sale = new ArrayList<>();
                    AtomicInteger flag = new AtomicInteger();
                    Integer size = sortedLeaderboard.size();
                    LOGGER.info(String.format("sortedLeaderboard size is %s", String.valueOf(size)));
                    int bucket = size / 10;
                    sortedLeaderboard.forEach((salesOfficer, sales) -> {
                        LOGGER.info(String.format("getting Sale sortedLeaderboard for customerUID :: %s sale :: %s", salesOfficer, sales));

                        LeaderboardData data = new LeaderboardData();
                        SclCustomerModel sclUserModel = (SclCustomerModel) userService.getUserForUID(salesOfficer);
                        int s = (int) (sales * 100);
                        LOGGER.info(String.format("sale is %s", String.valueOf(s)));
                        int score = calculateScoreFromPercentile(s);
                        LOGGER.info(String.format("score is %s", String.valueOf(score)));
                        data.setScore(score);
                        data.setCode(sclUserModel.getUid());
                        data.setName(sclUserModel.getName());
                        data.setSale(sales);
                        sale.add(data);
                    });

                    List<LeaderboardData> premiumSale = new ArrayList<>();
                    AtomicInteger flagPre = new AtomicInteger();
                    Integer premiumSize = sortedPremiumLeaderboard.size();
                    LOGGER.info(String.format("sortedPremiumLeaderboard size is %s", String.valueOf(premiumSize)));
                    int premiumBucket = premiumSize / 10;
                    sortedPremiumLeaderboard.forEach((salesOfficer, sales) -> {
                        LOGGER.info(String.format("getting Sale sortedPremiumLeaderboard for customerUID :: %s sale :: %s", salesOfficer, String.valueOf(sales)));
                        LeaderboardData data = new LeaderboardData();
                        SclCustomerModel sclUserModel = (SclCustomerModel) userService.getUserForUID(salesOfficer);
                        int s1 = (int) (sales * 100);
                        LOGGER.info(String.format("sale is %s", String.valueOf(s1)));
                        int score = calculateScoreFromPercentile(s1);
                        LOGGER.info(String.format("score is %s", String.valueOf(score)));
                        data.setScore(score);
                        data.setCode(sclUserModel.getUid());
                        data.setName(sclUserModel.getName());
                        data.setSale(sales);
                        premiumSale.add(data);
                    });

                    int count = sale.size();
                    double sumsScore = 0.0;
                    Map<String, Double> finalScore = new HashMap<>();

                    for (LeaderboardData leader : sale) {
                        for (LeaderboardData data : premiumSale) {

                            if (leader.getCode().equalsIgnoreCase(data.getCode())) {
                                sumsScore = leader.getScore() * 0.4 + data.getScore() * 0.4;
                                finalScore.put(leader.getCode(), sumsScore);
                            }


                        }
                    }

                    List<Map.Entry<String, Double>> entri = new ArrayList<>(finalScore.entrySet());

                    Collections.sort(entri, Map.Entry.comparingByValue(Comparator.reverseOrder()));

                    Map<String, Double> finalScoreboard = new LinkedHashMap<>();

                    // Iterate over the sorted entries and put them into the sortedLeaderboard
                    for (Map.Entry<String, Double> entry : entri) {
                        finalScoreboard.put(entry.getKey(), entry.getValue());
                    }
                    List<LeaderboardData> finalLeaderBoard = new ArrayList<>();
                    AtomicInteger flagFinal = new AtomicInteger();
                    Integer finalSize = finalScoreboard.size();
                    LOGGER.info(String.format("finalScoreboard size is %s", String.valueOf(finalSize)));
                    int finalBucket = finalSize / 10;
                    finalScoreboard.forEach((salesOfficer, score) -> {
                        LOGGER.info(String.format("getting Sale finalscoreboardQTD for customerUID :: %s sale :: %s", salesOfficer, String.valueOf(score)));
                        LeaderboardData data = new LeaderboardData();
                        SclCustomerModel sclUserModel = (SclCustomerModel) userService.getUserForUID(salesOfficer);
                        flagFinal.addAndGet(1);
                        data.setRank(flagFinal.get());
                        data.setScore(score.intValue());
                        data.setCode(sclUserModel.getUid());
                        data.setName(sclUserModel.getName());
                        finalLeaderBoard.add(data);
                    });

                    list.setSalesList(finalLeaderBoard);
                }
            }
        }*/

        return list;
    }

    private Map<String, Double> calculatePercentileRank(Map<String, Integer> data) {

        List<Integer> sortedValues = new ArrayList<>();
        int nullCount = 0;

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            Integer value = entry.getValue();
            if (value != null) {
                sortedValues.add(value);
            } else {
                nullCount++;
            }
        }

        Collections.sort(sortedValues);

        int size = sortedValues.size();
        Map<String, Double> percentiles = new HashMap<>();

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            int value = entry.getValue() != null ? entry.getValue() : 0;
            if (value == 0) {
                percentiles.put(entry.getKey(), 0.0);
            } else {
                int rank = getRanks(sortedValues, value, nullCount);
                double percentile = (size - rank) / (double) (size - 1);

                percentiles.put(entry.getKey(), percentile);
                LOGGER.info(String.format("Customer :: %s Percentileoutstanding :: %s " , entry.getKey(), percentile));
            }
        }

        return percentiles;
    }

    @Override
    public TsmLeaderboardListData getTsmSalesLeaderboardEmpList(String filter) {
        SclUserModel sclUser = (SclUserModel) getUserService().getCurrentUser();
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        TsmLeaderboardListData list = new TsmLeaderboardListData();
        Date startDate;
        Date endDate;
        Date currentDate = Calendar.getInstance().getTime();
        int monthInNumber = currentDate.getMonth();
        if (sclUser.getUserType().getCode().equals("TSM")) {
            List<TsmLeaderboardData> leaderboardDataList = new ArrayList<>();
            list.setEmpCode(sclUser.getUid());
            list.setEmpName(sclUser.getName());
            String district = null;
            FilterRegionData filterRegion = new FilterRegionData();
            List<RegionMasterModel> regionList = territoryManagementService.getRegionsForUser(filterRegion);
            LOGGER.info(String.format("regionList ::", String.valueOf(regionList)));
            if (regionList != null && !regionList.isEmpty()) {
                if (regionList.get(0).getPk().toString() != null) {
                    LOG.info("get district:" + regionList.get(0).getName());
                    String region1 = regionList.get(0).getName();
                    list.setRegion(region1);

                }
                district = regionList.get(0).getPk().toString();
            }

            List<SclUserModel> allSalesOfficersByRegion = salesPerformanceService.getTsmByRegion(district);
            Map<String, Double> unsortedLeaderboard = new HashMap<>();


            if (filter.equals(FILTER_MTD)) {
                for (SclUserModel sclUserModel : allSalesOfficersByRegion) {


                    startDate = getFirstDateOfMonth(monthInNumber);
                    endDate = getLastDateOfMonth(monthInNumber);
                    List<List<Object>> salesDealerByDate = salesPerformanceService.getSalesDealerByDate(district, sclUserModel, startDate, endDate);
                    for (List<Object> entry : salesDealerByDate) {
                        if (entry.size() >= 2) {
                            String key = String.valueOf(entry.get(0));
                            Double value = (Double) entry.get(1);
                            unsortedLeaderboard.put(key, value);
                        }
                    }
                }

            } else if (filter.equals(FILTER_YTD)) {
                for (SclUserModel sclUserModel : allSalesOfficersByRegion) {
                    List<Date> dates = salesPerformanceService.getCurrentFinancialYear();
                    startDate = dates.get(0);
                    endDate = dates.get(1);

                    List<List<Object>> salesDealerByDate = salesPerformanceService.getSalesDealerByDate(district, sclUserModel, startDate, endDate);
                    for (List<Object> entry : salesDealerByDate) {
                        if (entry.size() >= 2) {
                            String key = String.valueOf(entry.get(0));
                            Double value = (Double) entry.get(1);
                            unsortedLeaderboard.put(key, value);
                        }
                    }
                }

            } else if (filter.equals("QTD")) {
                for (SclUserModel sclUserModel : allSalesOfficersByRegion) {
                    Calendar calendar = Calendar.getInstance();
                    int currentYear = calendar.get(Calendar.YEAR);
                    int currentMonth = calendar.get(Calendar.MONTH);

                    int currentQuarter = (currentMonth / 3) + 1;
                    int startMonth = (currentQuarter - 1) * 3;
                    int endMonth = startMonth + 2;

                    calendar.set(currentYear, startMonth, 1);
                    startDate = calendar.getTime();

                    calendar.set(currentYear, endMonth, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    endDate = calendar.getTime();

                    List<List<Object>> salesDealerByDate = salesPerformanceService.getSalesDealerByDate(district, sclUserModel, startDate, endDate);
                    for (List<Object> entry : salesDealerByDate) {
                        if (entry.size() >= 2) {
                            String key = String.valueOf(entry.get(0));
                            Double value = (Double) entry.get(1);
                            unsortedLeaderboard.put(key, value);
                        }
                    }
                }
            }

            List<Map.Entry<String, Double>> entri = new ArrayList<>(unsortedLeaderboard.entrySet());

            Collections.sort(entri, Map.Entry.comparingByValue(Comparator.reverseOrder()));

            Map<String, Double> finalScoreboard = new LinkedHashMap<>();

            // Iterate over the sorted entries and put them into the sortedLeaderboard
            for (Map.Entry<String, Double> entry : entri) {
                finalScoreboard.put(entry.getKey(), entry.getValue());
            }
            List<TsmLeaderboardData> finalLeaderBoard = new ArrayList<>();
            AtomicInteger flagFinal = new AtomicInteger();
            String finalDistrict = district;
            finalScoreboard.forEach((salesOfficer, score) -> {
                LOGGER.info(String.format("getting Sale finalscoreboardQTD for customerUID :: %s sale :: %s", salesOfficer, String.valueOf(score)));
                TsmLeaderboardData data = new TsmLeaderboardData();
                SclUserModel sclUserModel = (SclUserModel) userService.getUserForUID(salesOfficer);
                flagFinal.addAndGet(1);
                data.setRank(flagFinal.get());
                data.setCode(sclUserModel.getUid());
                data.setName(sclUserModel.getName());
                data.setSale(score != null ? score : 0);
                List<SclCustomerModel> customerforUser = salesPerformanceService.getCustomerForTsm(finalDistrict, sclUserModel);
                data.setTotalNetwork(customerforUser.size());
                data.setContactNo(sclUserModel.getMobileNumber());
                finalLeaderBoard.add(data);
            });
            list.setSalesList(finalLeaderBoard);
        }

        else if (sclUser.getUserType().getCode().equals("RH"))
        {
            List<TsmLeaderboardData> leaderboardDataList = new ArrayList<>();
            list.setEmpCode(sclUser.getUid());
            list.setEmpName(sclUser.getName());
            String district = null;
            List<String> state = territoryManagementService.getAllStatesForSO();
            list.setRegion(state.get(0));
            district=state.get(0);
            LOG.info(String.format("District :",district));
            List<SclUserModel> allSalesOfficersByRegion = salesPerformanceService.getRHByState(district);
            LOG.info(String.format("sclUserModel size of %s:",allSalesOfficersByRegion.size()));
            Map<String, Double> unsortedLeaderboard = new HashMap<>();
            if (filter.equals(FILTER_MTD)) {
                for (SclUserModel sclUserModel : allSalesOfficersByRegion) {
                    LOG.info(String.format("sclUserModel %s:",sclUserModel));
                    startDate = getFirstDateOfMonth(monthInNumber);
                    endDate = getLastDateOfMonth(monthInNumber);
                    List<List<Object>> salesDealerByDate = salesPerformanceService.getRHSalesDealerByDate(sclUserModel, startDate, endDate);
                    for (List<Object> entry : salesDealerByDate) {
                        if (entry.size() >= 2) {
                            String key = String.valueOf(entry.get(0));
                            Double value = (Double) entry.get(1);
                            unsortedLeaderboard.put(key, value);
                            LOG.info((String.format("Key  :: %s Value ::%s ",key,value)));
                        }
                    }
                }

            } else if (filter.equals(FILTER_YTD)) {
                for (SclUserModel sclUserModel : allSalesOfficersByRegion) {
                    List<Date> dates = salesPerformanceService.getCurrentFinancialYear();
                    startDate = dates.get(0);
                    endDate = dates.get(1);

                    List<List<Object>> salesDealerByDate = salesPerformanceService.getRHSalesDealerByDate(sclUserModel, startDate, endDate);
                    for (List<Object> entry : salesDealerByDate) {
                        if (entry.size() >= 2) {
                            String key = String.valueOf(entry.get(0));
                            Double value = (Double) entry.get(1);
                            unsortedLeaderboard.put(key, value);
                            LOG.info((String.format("Key  :: %s Value ::%s ",key,value)));
                        }
                    }
                }

            } else if (filter.equals("QTD")) {
                for (SclUserModel sclUserModel : allSalesOfficersByRegion) {
                    Calendar calendar = Calendar.getInstance();
                    int currentYear = calendar.get(Calendar.YEAR);
                    int currentMonth = calendar.get(Calendar.MONTH);

                    int currentQuarter = (currentMonth / 3) + 1;
                    int startMonth = (currentQuarter - 1) * 3;
                    int endMonth = startMonth + 2;

                    calendar.set(currentYear, startMonth, 1);
                    startDate = calendar.getTime();

                    calendar.set(currentYear, endMonth, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    endDate = calendar.getTime();

                    List<List<Object>> salesDealerByDate = salesPerformanceService.getRHSalesDealerByDate(sclUserModel, startDate, endDate);
                    for (List<Object> entry : salesDealerByDate) {
                        if (entry.size() >= 2) {
                            String key = String.valueOf(entry.get(0));
                            Double value = (Double) entry.get(1);
                            unsortedLeaderboard.put(key, value);
                            LOG.info((String.format("Key  :: %s Value ::%s ",key,value)));
                        }
                    }
                }
            }

      /*  List<Map.Entry<String, Double>> entri = new ArrayList<>(unsortedLeaderboard.entrySet());

        Collections.sort(entri, Map.Entry.comparingByValue(Comparator.reverseOrder()));*/

            List<Map.Entry<String, Double>> entri = unsortedLeaderboard.entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .collect(Collectors.toList());

            Collections.sort(entri, Map.Entry.comparingByValue(Comparator.reverseOrder()));


            Map<String, Double> finalScoreboard = new LinkedHashMap<>();

            // Iterate over the sorted entries and put them into the sortedLeaderboard
            for (Map.Entry<String, Double> entry : entri) {
                finalScoreboard.put(entry.getKey(), entry.getValue());
            }

            List<TsmLeaderboardData> finalLeaderBoard = new ArrayList<>();
            AtomicInteger flagFinal = new AtomicInteger();
            String finalDistrict = district;
            finalScoreboard.forEach((salesOfficer, score) -> {
                LOGGER.info(String.format("getting Sale finalscoreboardQTD for customerUID :: %s sale :: %s", salesOfficer, String.valueOf(score)));
                TsmLeaderboardData data = new TsmLeaderboardData();
                SclUserModel sclUserModel = (SclUserModel) userService.getUserForUID(salesOfficer);
                flagFinal.addAndGet(1);
                data.setRank(flagFinal.get());
                data.setCode(sclUserModel.getUid());
                data.setName(sclUserModel.getName());
                data.setSale(score);
                List<SclCustomerModel> customerforUser = salesPerformanceService.getCustomerForRH(finalDistrict, sclUserModel);
                data.setTotalNetwork(customerforUser.size());
                data.setContactNo(sclUserModel.getMobileNumber());
                finalLeaderBoard.add(data);
            });
            list.setSalesList(finalLeaderBoard);
        }

        return list;
    }

    @Override
    public OverallPerformanceListData getInfluencerOverallPerformance(String filter, String bgpFilter) {
        //growth and bgpfilter to do
        OverallPerformanceListData listData = new OverallPerformanceListData();
        List<OverallPerformanceData> list = new ArrayList<>();
        int week=0, month=0, year=0;
        double quantity=0.0, growth=0.0, currentWeekQuantity=0.0, lastWeekQuantity=0.0, currentMonthQuantity=0.0, lastMonthQuantity=0.0;
        List<List<Object>> weeklyList = getSalesPerformanceService().getWeeklyOverallPerformance(bgpFilter);
        List<List<Object>> monthlyList = getSalesPerformanceService().getMonthlyOverallPerformance(bgpFilter);
        if(filter.equalsIgnoreCase("Weekly"))
        {
            if(weeklyList!=null && !weeklyList.isEmpty())
            {
                for (List<Object> objects : weeklyList) {
                    if (objects != null && !objects.isEmpty()) {
                        week = (int) objects.get(0) != 0 ? (int) objects.get(0) : 0;
                        year = (int) objects.get(1) != 0 ? (int) objects.get(1) : 0;
                        quantity = (double) objects.get(2) != 0 ? (double) objects.get(2) : 0;
                        currentWeekQuantity = quantity;
                        List<Date> dates = getStartAndEndDateOfWeek(week, year);
                        OverallPerformanceData overallPerformanceData = new OverallPerformanceData();
                        if (dates != null && !dates.isEmpty()) {
                            overallPerformanceData.setWeekStartDate(getParsedDate(dates.get(0)));
                            overallPerformanceData.setWeekEndDate(getParsedDate(dates.get(1)));
                        }
                        overallPerformanceData.setQuantity(quantity);
                        growth = currentWeekQuantity-lastWeekQuantity;
                        overallPerformanceData.setGrowth(growth!=0.0 ? growth:0.0);
                        lastWeekQuantity = currentWeekQuantity;
                        list.add(overallPerformanceData);
                    }
                }
            }
        }
        else if(filter.equalsIgnoreCase("Monthly"))
        {
            if(monthlyList!=null && !monthlyList.isEmpty())
            {
                for (List<Object> objects : monthlyList) {
                    if (objects != null && !objects.isEmpty()) {
                        month = (int) objects.get(0) != 0 ? (int) objects.get(0) : 0;
                        year = (int) objects.get(1) != 0 ? (int) objects.get(1) : 0;
                        quantity = (double) objects.get(2) != 0 ? (double) objects.get(2) : 0;
                        currentMonthQuantity = quantity;
                        String monthName = getMonthName(month,year);
                        OverallPerformanceData overallPerformanceData = new OverallPerformanceData();
                        overallPerformanceData.setMonth(monthName);
                        overallPerformanceData.setQuantity(quantity);
                        growth = currentMonthQuantity-lastMonthQuantity;
                        overallPerformanceData.setGrowth(growth!=0.0 ? growth:0.0);
                        lastMonthQuantity = currentMonthQuantity;
                        list.add(overallPerformanceData);
                    }
                }
            }
        }
        listData.setOverallPerformanceDataList(list);
        return listData;
    }

    @Override
    public CounterShareResponseData getCounterShareData(CounterShareData counterShareData) {
        return salesPerformanceService.getCounterShareData(counterShareData);
    }

    @Override
    public DealerCurrentNetworkListData getCurrentNetworkCustomers(String leadType, String networkType, String searchKeyFilter, boolean sclExclusiveCustomer) {
       return salesPerformanceService.getCurrentNetworkCustomers(leadType, networkType, searchKeyFilter, sclExclusiveCustomer);
    }

    @Override
    public List<Map<String, Object>> getLastLiftingDateAndQtyForCustomers(String sclCustomerCode,String leadType) {
       return  salesPerformanceService.getLastLiftingDateAndQtyForCustomers(sclCustomerCode,leadType);
    }

    private String getMonthName(int month, int year) {
        String monthName="";
        LocalDate date = LocalDate.of(year, month, 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM-yyyy");
        monthName = date.format(formatter);
        return monthName;
    }

    private List<Date> getStartAndEndDateOfWeek(int week, int year) {
        List<Date> dateList = new ArrayList<>();
        LocalDate firstDayOfYear = LocalDate.of(year, 1, 1);
        LocalDate startOfWeek1 = firstDayOfYear.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);
        LocalDate startDateOfWeek = startOfWeek1.plusWeeks(week - 1);
        LocalDate endDateOfWeek = startDateOfWeek.plusDays(6);
        Date startDate = Date.from(startDateOfWeek.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(endDateOfWeek.atStartOfDay(ZoneId.systemDefault()).toInstant());
        dateList.add(startDate);
        dateList.add(endDate);
        return dateList;
    }

    private String getParsedDate(Date date) {
        Instant instant = date.toInstant();
        LocalDate localDate = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = localDate.format(formatter);
        return formattedDate;
    }

    public DealerDao getDealerDao() {
        return dealerDao;
    }

    public void setDealerDao(DealerDao dealerDao) {
        this.dealerDao = dealerDao;
    }

    public GeographicalRegionFacade getGeographicalRegionFacade() {
        return geographicalRegionFacade;
    }

    public void setGeographicalRegionFacade(GeographicalRegionFacade geographicalRegionFacade) {
        this.geographicalRegionFacade = geographicalRegionFacade;
    }

    public TerritoryMasterService getTerritoryMasterService() {
        return territoryMasterService;
    }

    public void setTerritoryMasterService(TerritoryMasterService territoryMasterService) {
        this.territoryMasterService = territoryMasterService;
    }
}