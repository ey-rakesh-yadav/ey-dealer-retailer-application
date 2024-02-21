package com.eydms.facades.impl;


import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.dao.*;
import com.eydms.core.dao.impl.SalesPerformanceDaoImpl;
import com.eydms.core.enums.CounterType;
import com.eydms.core.enums.LeadType;
import com.eydms.core.model.*;
import com.eydms.core.region.dao.DistrictMasterDao;
import com.eydms.core.services.NetworkService;
import com.eydms.core.services.PointRequisitionService;
import com.eydms.core.services.SalesPerformanceService;
import com.eydms.core.services.SalesPlanningService;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.core.utility.EyDmsDateUtility;
import com.eydms.facades.SalesPerformanceFacade;
import com.eydms.facades.data.*;
import com.eydms.facades.network.EYDMSNetworkFacade;
import com.eydms.facades.region.GeographicalRegionFacade;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import javax.print.DocFlavor;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.ParseException;
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
    private static final Logger LOG = Logger.getLogger(SalesPerformanceFacadeImpl.class);

    private static final String ZERO = " 0";
    private static final String PERCENTAGE = "%";

    private static final String DEALER = "DEALER";
    private static final String RETAILER = "RETAILER";
    DecimalFormat df = new DecimalFormat("#.#");

    private static final Logger LOGGER = Logger.getLogger(SalesPerformanceDaoImpl.class);

    @Resource
    private UserService userService;
    @Autowired
    EnumerationService enumerationService;
    @Resource
    private BaseSiteService baseSiteService;
    @Autowired
    EYDMSNetworkFacade networkFacade;
    @Autowired
    TerritoryManagementDao territoryManagementDao;
    @Autowired
    DistrictMasterDao districtMasterDao;
    @Resource
    private SalesPerformanceService salesPerformanceService;
    @Resource
    private Converter<EyDmsCustomerModel, InfluencerSummaryData> influencerSummaryConverter;

    @Resource
    private SalesPlanningService salesPlanningService;

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
    EyDmsUserDao eydmsUserDao;

    @Autowired
    CollectionDao collectionDao;
    
    @Autowired
    PointRequisitionService pointRequisitionService;

    public DealerDao getDealerDao() {
        return dealerDao;
    }

    public void setDealerDao(DealerDao dealerDao) {
        this.dealerDao = dealerDao;
    }

    @Resource
    DealerDao dealerDao;
    @Autowired
    PointRequisitionDao pointRequisitionDao;

    public GeographicalRegionFacade getGeographicalRegionFacade() {
        return geographicalRegionFacade;
    }

    public void setGeographicalRegionFacade(GeographicalRegionFacade geographicalRegionFacade) {
        this.geographicalRegionFacade = geographicalRegionFacade;
    }

    @Override
    public SalesAndAchievementData getTotalAndActualTargetForSales(String filter, Integer year, Integer month,List<String> doList,List<String> subAreaList) {
        EyDmsUserModel eydmsUser=(EyDmsUserModel) getUserService().getCurrentUser();
        BaseSiteModel currentBaseSite=baseSiteService.getCurrentBaseSite();
        SalesAndAchievementData data=new SalesAndAchievementData();
        double actualTarget = 0.0, totalTarget=0.0, behindTarget=0.0, aheadTarget=0.0;
        double achievementPercentage=0.0;
        if(StringUtils.isBlank(filter))
        {
            if(year!=0 && month!=0)
            {
                //actualTarget=getSalesPerformanceService().getActualTargetForSalesForMonth(subArea, eydmsUser, currentBaseSite, year, month);
                actualTarget=getSalesPerformanceService().getActualTargetForSalesForMonth(eydmsUser,currentBaseSite,year,month,doList,subAreaList);
                data.setActualTarget(actualTarget!=0.0?actualTarget:0.0);

                totalTarget=getSalesPerformanceService().getSalesTargetForMonth(eydmsUser, currentBaseSite, year, month);
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
                //actualTarget=getSalesPerformanceService().getActualTargetForSalesMTD(subArea,eydmsUser,currentBaseSite);
                actualTarget=getSalesPerformanceService().getActualTargetForSalesMTD(eydmsUser,currentBaseSite,doList,subAreaList);
                data.setActualTarget(actualTarget!=0.0?actualTarget:0.0);

                totalTarget=getSalesPerformanceService().getMonthlySalesTarget(eydmsUser,currentBaseSite);
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
            if(filter.contains("MTD"))
            {
                //actualTarget=getSalesPerformanceService().getActualTargetForSalesMTD(subArea,eydmsUser,currentBaseSite);
                actualTarget=getSalesPerformanceService().getActualTargetForSalesMTD(eydmsUser,currentBaseSite,doList,subAreaList);
                data.setActualTarget(actualTarget!=0.0?actualTarget:0.0);

                totalTarget=getSalesPerformanceService().getMonthlySalesTarget(eydmsUser,currentBaseSite);
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
            else if(filter.contains("YTD"))
            {
                //actualTarget=getSalesPerformanceService().getActualTargetForSalesYTD(subArea,eydmsUser,currentBaseSite);
                actualTarget=getSalesPerformanceService().getActualTargetForSalesYTD(eydmsUser,currentBaseSite,doList,subAreaList);
                data.setActualTarget(actualTarget!=0.0?actualTarget:0.0);

                totalTarget=getSalesPerformanceService().getAnnualSalesTarget(eydmsUser);
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
    public SalesAndAchievementData getProratedActualAndActualTargetForSales(String filter, Integer yearFilter, Integer monthFilter,List<String> doList,List<String> subAreaList) {
        EyDmsUserModel eydmsUser=(EyDmsUserModel) getUserService().getCurrentUser();
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
                cal= LocalDate.of(yearFilter,monthFilter,cal.getDayOfMonth());
                noOfDaysGoneByInTheMonth = cal.getDayOfMonth() -1;

                //actualTarget=getSalesPerformanceService().getActualTargetForSalesForMonth(subArea, eydmsUser, currentBaseSite, yearFilter, monthFilter);
                actualTarget=getSalesPerformanceService().getActualTargetForSalesForMonth(eydmsUser, currentBaseSite, yearFilter, monthFilter,doList,subAreaList);
                data.setActualTarget(actualTarget!=0.0?actualTarget:0.0);

                totalTarget=getSalesPerformanceService().getSalesTargetForMonth(eydmsUser, currentBaseSite, yearFilter, monthFilter);

                if(totalTarget!=0.0)
                    proratedTarget=(totalTarget/noOfDaysInTheMonth)*noOfDaysGoneByInTheMonth;
                data.setProratedTarget(proratedTarget!=0.0?proratedTarget:0.0);

                if(actualTarget!=0.0 && totalTarget!=0.0)
                    achievementPercentage=(actualTarget/totalTarget)*100;
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
            else
            {
                //actualTarget=getSalesPerformanceService().getActualTargetForSalesMTD(subArea,eydmsUser,currentBaseSite);
                actualTarget=getSalesPerformanceService().getActualTargetForSalesMTD(eydmsUser,currentBaseSite,doList,subAreaList);
                data.setActualTarget(actualTarget!=0.0?actualTarget:0.0);

                totalTarget=getSalesPerformanceService().getMonthlySalesTarget(eydmsUser,currentBaseSite);
                if(totalTarget!=0.0)
                    proratedTarget=(totalTarget/noOfDaysInTheMonth)*noOfDaysGoneByInTheMonth;
                data.setProratedTarget(proratedTarget!=0.0?proratedTarget:0.0);

                if(actualTarget!=0.0 && totalTarget!=0.0)
                    achievementPercentage=(actualTarget/totalTarget)*100;
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
            if(filter.contains("MTD"))
            {
                //actualTarget=getSalesPerformanceService().getActualTargetForSalesMTD(subArea,eydmsUser,currentBaseSite);
                actualTarget=getSalesPerformanceService().getActualTargetForSalesMTD(eydmsUser,currentBaseSite,doList,subAreaList);
                data.setActualTarget(actualTarget!=0.0?actualTarget:0.0);

                totalTarget=getSalesPerformanceService().getMonthlySalesTarget(eydmsUser,currentBaseSite);
                if(totalTarget!=0.0)
                    proratedTarget=(totalTarget/noOfDaysInTheMonth)*noOfDaysGoneByInTheMonth;
                data.setProratedTarget(proratedTarget!=0.0?proratedTarget:0.0);

                if(actualTarget!=0.0 && totalTarget!=0.0)
                    achievementPercentage=(actualTarget/totalTarget)*100;
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
            else if(filter.contains("YTD"))
            {
                //actualTarget=getSalesPerformanceService().getActualTargetForSalesYTD(subArea,eydmsUser,currentBaseSite);
                actualTarget=getSalesPerformanceService().getActualTargetForSalesYTD(eydmsUser,currentBaseSite,doList,subAreaList);
                data.setActualTarget(actualTarget!=0.0?actualTarget:0.0);

                totalTarget=getSalesPerformanceService().getAnnualSalesTarget(eydmsUser);
                if(totalTarget!=0.0)
                    proratedTarget=(totalTarget/totalNoOfDaysInYear)*noOfDaysGoneBy;
                data.setProratedTarget(proratedTarget!=0.0?proratedTarget:0.0);

                if(actualTarget!=0.0 && totalTarget!=0.0)
                    achievementPercentage=(actualTarget/totalTarget)*100;
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
    public CurrentAskingPredicatedData getPredicatedAchievementCurrentAndAskingRate(String filter,List<String> doList,List<String> subAreaList) {
        EyDmsUserModel eydmsUser = (EyDmsUserModel) getUserService().getCurrentUser();
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        CurrentAskingPredicatedData data = new CurrentAskingPredicatedData();

        //Double salesInCurrentMonth = getSalesPerformanceService().getActualTargetForSalesMTD(subArea, eydmsUser, currentBaseSite);//MTD-Order
        Double salesInCurrentMonth = getSalesPerformanceService().getActualTargetForSalesMTD(eydmsUser, currentBaseSite,doList,subAreaList);//MTD-Order

        //Double salesTillDate = getSalesPerformanceService().getActualTargetForSalesYTD(subArea, eydmsUser, currentBaseSite);//YTD-Order
        Double salesTillDate = getSalesPerformanceService().getActualTargetForSalesYTD(eydmsUser, currentBaseSite,doList,subAreaList);//YTD-Order

        Double monthlySalesTarget=getSalesPerformanceService().getMonthlySalesTarget(eydmsUser,currentBaseSite);//MTD-salesPlanning
        Double annualSalesTarget=getSalesPerformanceService().getAnnualSalesTarget(eydmsUser);//YTD-salesPlanning

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
        int year = cal.getYear();
        LocalDate calTwo= LocalDate.of(year,12,31);
        int totalNoOfDaysInYear = calTwo.getDayOfYear();
        int remainingDaysInTheYear = totalNoOfDaysInYear - dayOfYear;
        int noOfDaysTillDate=totalNoOfDaysInYear-remainingDaysInTheYear;

        if(StringUtils.isBlank(filter))
        {
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
                if(monthlySalesTarget!=0.0)
                    askingRate=(monthlySalesTarget-salesInCurrentMonth)/remainingDaysInTheMonth;
                if(askingRate!=0.0)
                    data.setAskingRate(askingRate);
                else
                    data.setAskingRate(0.0);
            }
            else
            {
                data.setPredicatedAchievement(0.0);
                data.setCurrentRate(0.0);
                data.setAskingRate(0.0);
            }
        }
        else {
            if (filter.contains("MTD")) {

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
                    if(monthlySalesTarget!=0.0)
                        askingRate = (monthlySalesTarget - salesInCurrentMonth) / remainingDaysInTheMonth;
                    if (askingRate != 0.0)
                        data.setAskingRate(askingRate);
                    else
                        data.setAskingRate(0.0);
                } else {
                    data.setPredicatedAchievement(0.0);
                    data.setCurrentRate(0.0);
                    data.setAskingRate(0.0);
                }
            }
            else if (filter.contains("YTD")) {
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

                    double askingRate = 0.0;
                    if(annualSalesTarget!=0.0)
                        askingRate = (annualSalesTarget - salesTillDate) / remainingDaysInTheYear;
                    if (askingRate != 0.0)
                        data.setAskingRate(askingRate);
                    else
                        data.setAskingRate(0.0);
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
    public CurrentAskingPredicatedData getPredicatedAchievementCurrentAndAskingRateDealerRetailer(String filter,String bgpFilter) {
           /* B2BCustomerModel  currentUser = (B2BCustomerModel) getUserService().getCurrentUser();
            EyDmsCustomerModel eydmsCustomer = (EyDmsCustomerModel) getUserService().getUserForUID(currentUser.getUid());
*/
        EyDmsCustomerModel eydmsCustomer = (EyDmsCustomerModel) getUserService().getCurrentUser();

        Double salesInCurrentMonth=0.0,salesTillDate=0.0,monthlySalesTarget=0.0,annualSalesTarget=0.0;
        int noOfDaysInTheMonth=0,noOfDaysGoneByInTheMonth=0,remainingDaysInTheMonth=0,dayOfYear=0,year=0,totalNoOfDaysInYear=0,remainingDaysInTheYear=0,noOfDaysTillDate=0;

        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        CurrentAskingPredicatedData data = new CurrentAskingPredicatedData();
        // Calendar cal = Calendar.getInstance();

               /* noOfDaysInTheMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
                 noOfDaysGoneByInTheMonth = cal.get(Calendar.DAY_OF_MONTH) - 1;
                 remainingDaysInTheMonth = noOfDaysInTheMonth - noOfDaysGoneByInTheMonth;

                 dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
                 year = cal.get(Calendar.YEAR);
                 Calendar calTwo = new GregorianCalendar(year, 11, 31);
                 totalNoOfDaysInYear = calTwo.get(Calendar.DAY_OF_YEAR);
                 remainingDaysInTheYear = totalNoOfDaysInYear - dayOfYear;
                 noOfDaysTillDate=totalNoOfDaysInYear-remainingDaysInTheYear;*/

        LocalDate cal=LocalDate.now();

        noOfDaysInTheMonth = cal.lengthOfMonth();
        noOfDaysGoneByInTheMonth = cal.getDayOfMonth()-1;
        remainingDaysInTheMonth = noOfDaysInTheMonth - noOfDaysGoneByInTheMonth;

        dayOfYear = cal.getDayOfYear();
        year = cal.getYear();
        LocalDate calTwo= LocalDate.of(year,12,31);
        totalNoOfDaysInYear = calTwo.getDayOfYear();
        remainingDaysInTheYear = totalNoOfDaysInYear - dayOfYear;
        noOfDaysTillDate=totalNoOfDaysInYear-remainingDaysInTheYear;

        if(eydmsCustomer.getCounterType().equals(CounterType.SP)){
            RequestCustomerData requestData = new RequestCustomerData();
            List<String> counterType = List.of(DEALER);
            requestData.setCounterType(counterType);
            List<EyDmsCustomerModel> customerforUser = territoryManagementService.getCustomerforUser(requestData);
            if(customerforUser!=null && !customerforUser.isEmpty()) {
                for (EyDmsCustomerModel customerModel : customerforUser) {
                    LOGGER.info(String.format("EyDms customer Model PK:%s", customerModel));
                }
            }
            else{
                LOGGER.info("SP List is Empty");
            }

            if(customerforUser!=null && !customerforUser.isEmpty()) {
                salesInCurrentMonth =  getSalesPerformanceService().getActualTargetForSalesSPMTD(customerforUser);
                salesTillDate =  getSalesPerformanceService().getActualTargetForSalesYTDSP(customerforUser,currentBaseSite);

                monthlySalesTarget=getSalesPerformanceService().getMonthlySalesTargetForSP(customerforUser);
                annualSalesTarget=getSalesPerformanceService().getAnnualSalesTargetForSP(customerforUser);

                LOGGER.info(String.format("salesInCurrentMonth:%s", String.valueOf(salesInCurrentMonth)));
                LOGGER.info(String.format("salesTillDate:%s",  String.valueOf(salesTillDate)));
                LOGGER.info(String.format("monthlySalesTarget:%s",  String.valueOf(monthlySalesTarget)));
                LOGGER.info(String.format("annualSalesTarget:%s",  String.valueOf(annualSalesTarget)));
            }
        }
        else if(eydmsCustomer.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
            salesInCurrentMonth =  getSalesPerformanceService().getActualTargetForSalesDealerMTD(eydmsCustomer, currentBaseSite,bgpFilter);//MTD-Order//6
            salesTillDate =  getSalesPerformanceService().getActualTargetForSalesDealerYTD(eydmsCustomer, currentBaseSite,bgpFilter);//YTD-Order//48

            monthlySalesTarget=getSalesPerformanceService().getMonthlySalesTargetForDealer(eydmsCustomer,currentBaseSite,bgpFilter);//MTD-salesPlanning//100
            annualSalesTarget=getSalesPerformanceService().getAnnualSalesTargetForDealer(eydmsCustomer,bgpFilter);//YTD-salesPlanning //200
            LOGGER.info(String.format("salesInCurrentMonth:%s", String.valueOf(salesInCurrentMonth)));
            LOGGER.info(String.format("salesTillDate:%s",  String.valueOf(salesTillDate)));
            LOGGER.info(String.format("monthlySalesTarget:%s",  String.valueOf(monthlySalesTarget)));
            LOGGER.info(String.format("annualSalesTarget:%s",  String.valueOf(annualSalesTarget)));
        }
        else if(eydmsCustomer.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
            salesInCurrentMonth = getSalesPerformanceService().getActualTargetForSalesRetailerMTD(eydmsCustomer, currentBaseSite,bgpFilter);//MTD-Order
            salesTillDate = getSalesPerformanceService().getActualTargetForSalesRetailerYTD(eydmsCustomer, currentBaseSite,bgpFilter);//YTD-Order

            monthlySalesTarget=getSalesPerformanceService().getMonthlySalesTargetForRetailer(eydmsCustomer.getUid(),currentBaseSite,bgpFilter);//MTD-salesPlanning
            annualSalesTarget=getSalesPerformanceService().getAnnualSalesTargetForRetailer(eydmsCustomer,bgpFilter);//YTD-salesPlanning
        }

        if(StringUtils.isBlank(filter))
        {
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
               // if(monthlySalesTarget!=0.0)
                    askingRate=(monthlySalesTarget-salesInCurrentMonth)/remainingDaysInTheMonth;
               // if(askingRate!=0.0)
                    data.setAskingRate(askingRate);
               // else
                    //data.setAskingRate(0.0);
            }
            else
            {
                data.setPredicatedAchievement(0.0);
                data.setCurrentRate(0.0);
                data.setAskingRate(0.0);
            }
        }
        else {
            if (filter.contains("MTD")) {

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
                  //  if(monthlySalesTarget!=0.0)
                        askingRate = (monthlySalesTarget - salesInCurrentMonth) / remainingDaysInTheMonth;
                  //  if (askingRate != 0.0)
                        data.setAskingRate(askingRate);
                   // else
                       // data.setAskingRate(0.0);
                } else {
                    data.setPredicatedAchievement(0.0);
                    data.setCurrentRate(0.0);
                    data.setAskingRate(0.0);
                }
            }
            else if (filter.contains("YTD")) {
                List<Date> currentFinancialYear = salesPerformanceService.getCurrentFinancialYear();
                Date startDate=currentFinancialYear.get(0);
                LocalDate calYtd=startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                LocalDate curDate=LocalDate.now();
                long dayOfYear1 =  (Math.abs(ChronoUnit.DAYS.between(curDate, calYtd)));
                // dayOfYear = calYtd.getDayOfYear();
                year = calYtd.getYear();//2023
                LocalDate calTwoYtd= LocalDate.of(year,12,31);
                totalNoOfDaysInYear = calTwoYtd.getDayOfYear();//365
                long remainingDaysInTheYear1 = totalNoOfDaysInYear - dayOfYear1;//283
                long noOfDaysTillDate1=Math.abs(totalNoOfDaysInYear-remainingDaysInTheYear1);//82

                LOGGER.info("year:"+year);
                LOGGER.info("dayOfYear:"+dayOfYear1);
                LOGGER.info("totalNoOfDaysInYear:"+totalNoOfDaysInYear);
                LOGGER.info("remainingDaysInTheYear:"+remainingDaysInTheYear1);
                LOGGER.info("noOfDaysTillDate:"+noOfDaysTillDate1);

                if (salesTillDate != 0.0) {
                    double perDaySale = 0.0;
                    perDaySale = salesTillDate / noOfDaysTillDate1;
                    if (perDaySale != 0.0) {
                        data.setPredicatedAchievement(perDaySale * totalNoOfDaysInYear);
                        data.setCurrentRate(perDaySale);
                    } else {
                        data.setPredicatedAchievement(0.0);
                        data.setCurrentRate(0.0);
                    }

                    double askingRate = 0.0;
                    //if(annualSalesTarget!=0.0)
                        askingRate = (annualSalesTarget - salesTillDate) / remainingDaysInTheYear1;
                  //  if (askingRate != 0.0)
                        data.setAskingRate(askingRate);
                  //  else
                      //  data.setAskingRate(0.0);
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
    public Double getSecondaryLeadDistanceCount(String filter, Integer month1, Integer year1,List<String> doList,List<String> subAreaList) {
        double distance = 0.0;
        EyDmsUserModel eydmsUser = (EyDmsUserModel) getUserService().getCurrentUser();
        BaseSiteModel baseSite = baseSiteService.getCurrentBaseSite();

        if(StringUtils.isBlank(filter)) {

            if(month1 !=0 && year1!=0)
            {
                distance=getSalesPerformanceService().getSecondaryLeadDistanceForMonth(eydmsUser,baseSite,year1,month1,doList,subAreaList);
                if(distance!=0.0) {
                    LOG.info(String.format("Distance : %s", distance));
                    return distance;
                }
                else
                    distance=0.0;
            }
            else
            {
                distance=getSalesPerformanceService().getSecondaryLeadDistance(eydmsUser,baseSite,doList,subAreaList);
                if(distance!=0.0) {
                    LOG.info(String.format("Distance : %s", distance));
                    return distance;
                }
                else
                    distance=0.0;
            }
        }
        else if(filter.contains("MTD"))
        {
            distance=getSalesPerformanceService().getSecondaryLeadDistanceMTD(eydmsUser,baseSite,doList,subAreaList);
            if(distance!=0.0) {
                LOG.info(String.format("Distance : %s", distance));
                return distance;
            }
            else
                distance=0.0;

        }
        else if(filter.contains("YTD"))
        {
            distance=getSalesPerformanceService().getSecondaryLeadDistanceYTD(eydmsUser,baseSite,doList,subAreaList);
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
        EyDmsUserModel eydmsUser=(EyDmsUserModel) getUserService().getCurrentUser();
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        SalesLeaderboardListData list=new SalesLeaderboardListData();
        Date startDate;
        Date endDate;
        Date currentDate = Calendar.getInstance().getTime();
        int monthInNumber = currentDate.getMonth();
        List<SalesLeaderboardData> leaderboardDataList=new ArrayList<>();
        list.setEmpCode(eydmsUser.getUid());
        list.setEmpName(eydmsUser.getName());
        List<EyDmsUserModel> allSalesOfficersByState = territoryManagementService.getAllSalesOfficersByState(StringUtils.isNotBlank(state) ? state : "");
        Map<String, Double> unsortedLeaderboard = new HashMap<>();
        if(StringUtils.isBlank(filter)) {
            startDate = getFirstDateOfMonth(monthInNumber);
            endDate = getLastDateOfMonth(monthInNumber);
            // list.setEmpSale(salesPerformanceService.getSalesByDeliveryDate(subArea, eydmsUser, currentBaseSite, startDate, endDate));
            list.setEmpSale(salesPerformanceService.getSalesByDeliveryDate(eydmsUser, currentBaseSite, startDate, endDate,doList,subAreaList));
            populateUnsortedLeaderboard(currentBaseSite, startDate, endDate, allSalesOfficersByState, unsortedLeaderboard,doList,subAreaList);
        } else if (filter.equals("daily")) {
            startDate=new Date();
            endDate=new Date();
            //list.setEmpSale(salesPerformanceService.getSalesByDeliveryDate(subArea, eydmsUser, currentBaseSite, startDate, endDate));
            list.setEmpSale(salesPerformanceService.getSalesByDeliveryDate(eydmsUser, currentBaseSite, startDate, endDate,doList,subAreaList));
            populateUnsortedLeaderboard(currentBaseSite, startDate, endDate, allSalesOfficersByState, unsortedLeaderboard,doList,subAreaList);
        } else if (filter.equals("monthly")) {
            startDate = getFirstDateOfMonth(monthInNumber);
            endDate = getLastDateOfMonth(monthInNumber);
            //list.setEmpSale(salesPerformanceService.getSalesByDeliveryDate(subArea, eydmsUser, currentBaseSite, startDate, endDate));
            list.setEmpSale(salesPerformanceService.getSalesByDeliveryDate(eydmsUser, currentBaseSite, startDate, endDate,doList,subAreaList));
            populateUnsortedLeaderboard(currentBaseSite, startDate, endDate, allSalesOfficersByState, unsortedLeaderboard,doList,subAreaList);
        } else if (filter.equals("yearly")) {
            List<Date> dates = salesPerformanceService.getCurrentFinancialYear();
            startDate=dates.get(0);
            endDate=dates.get(1);
            //list.setEmpSale(salesPerformanceService.getSalesByDeliveryDate(subArea, eydmsUser, currentBaseSite, startDate, endDate));
            list.setEmpSale(salesPerformanceService.getSalesByDeliveryDate(eydmsUser, currentBaseSite, startDate, endDate,doList,subAreaList));
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
            EyDmsUserModel eydmsUserModel = (EyDmsUserModel) userService.getUserForUID(salesOfficer);
            flag.addAndGet(1);
            data.setRank(flag.get());
            data.setCode(eydmsUserModel.getUid());
            data.setName(eydmsUserModel.getName());
            data.setTotalNetwork(1);
            data.setSale(sales);
            data.setContactNo(eydmsUserModel.getMobileNumber());
            leaderboardDataList.add(data);
            if (StringUtils.isNotBlank(soFilter)) {
                EyDmsUserModel eydmsUserFromSoFilter = (EyDmsUserModel) userService.getUserForUID(soFilter);
                if(eydmsUserFromSoFilter.getName().equalsIgnoreCase(eydmsUserModel.getName())) {
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

    private void populateUnsortedLeaderboard(BaseSiteModel currentBaseSite, Date startDate, Date endDate, List<EyDmsUserModel> allSalesOfficersByState, Map<String, Double> unsortedLeaderboard,List<String> doList,List<String> subAreaList) {
        if(!allSalesOfficersByState.isEmpty() && allSalesOfficersByState != null) {
            allSalesOfficersByState.stream().forEach(so -> {
                if(so!=null){
                    //  unsortedLeaderboard.put(so.getUid(), salesPerformanceService.getSalesByDeliveryDate(subArea, so, currentBaseSite, startDate, endDate));
                    unsortedLeaderboard.put(so.getUid(), salesPerformanceService.getSalesByDeliveryDate(so, currentBaseSite, startDate, endDate,doList,subAreaList));
                }
            });
        }
    }

    private void populateUnsortedSalesLeaderboard(BaseSiteModel currentBaseSite, Date startDate, Date endDate, List<EyDmsCustomerModel> customerList, Map<String, Double> unsortedLeaderboard) {
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

    private void populatePremiumUnsortedSalesLeaderboard(BaseSiteModel currentBaseSite, Date startDate, Date endDate, List<EyDmsCustomerModel> customerList, Map<String, Double> unsortedPremiumSalesLeaderboard) {
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




    private void populateUnsortedSalesLeaderboardRetailer(BaseSiteModel currentBaseSite, Date startDate, Date endDate, List<EyDmsCustomerModel> customerList, Map<String, Double> unsortedLeaderboard) {
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
    public MarketCounterShareData getDealerCounterShareForMarket(String filter,Integer year, Integer month,List<String> doList,List<String> subAreaList) {

        B2BCustomerModel currentUser=(B2BCustomerModel) getUserService().getCurrentUser();
        BaseSiteModel currentBaseSite=baseSiteService.getCurrentBaseSite();
        double actualTarget = 0.0, totalTarget=0.0;
        Date startDate;
        Date endDate ;
        Date currentDate = Calendar.getInstance().getTime();
        int monthInNumber = currentDate.getMonth();
        LocalDate currentMonth = LocalDate.now();

        MarketCounterShareData data = new MarketCounterShareData();
        List<EyDmsCustomerModel> customerFilteredList=new ArrayList<>();
        double counterPotential=0.0;

        if(currentUser instanceof EyDmsUserModel)
        {
            RequestCustomerData requestCustomerData = new RequestCustomerData();
            requestCustomerData.setCounterType(List.of("Dealer"));
            customerFilteredList = territoryManagementService.getCustomerforUser(requestCustomerData);
            if(customerFilteredList!=null && !customerFilteredList.isEmpty())
                for (EyDmsCustomerModel eydmsCustomerModel : customerFilteredList) {
                    counterPotential += eydmsCustomerModel.getCounterPotential()!=null?eydmsCustomerModel.getCounterPotential():0.0;
                }
            LOG.info(String.format("counterPotential :: %s",counterPotential));
        }
        else if (currentUser instanceof EyDmsCustomerModel)
        {
            EyDmsCustomerModel eydmsCustomer= (EyDmsCustomerModel) getUserService().getCurrentUser();
            if (eydmsCustomer.getCounterType().equals(CounterType.SP)) {
                RequestCustomerData requestData = new RequestCustomerData();
                List<String> counterTypes = List.of(DEALER);
                requestData.setCounterType(counterTypes);
                customerFilteredList = territoryManagementService.getCustomerforUser(requestData);
                if (customerFilteredList != null && !customerFilteredList.isEmpty()) {
                    for (EyDmsCustomerModel customer : customerFilteredList) {
                        LOGGER.info(String.format("EyDms customer Model PK:%s", customer));
                        counterPotential += customer.getCounterPotential() != null ? customer.getCounterPotential() : 0.0;
                    }
                }
            }
        }
        if(StringUtils.isBlank(filter))
        {
            if(year!=0 && month!=0)
            {
                if(currentUser instanceof EyDmsUserModel){
                    EyDmsUserModel eydmsUser = (EyDmsUserModel) getUserService().getCurrentUser();
                    actualTarget=getSalesPerformanceService().getActualTargetForSalesForMonth(eydmsUser,currentBaseSite,year,month,doList,subAreaList);
                    totalTarget=getSalesPerformanceService().getSalesTargetForMonth(eydmsUser, currentBaseSite, year, month);
                    LOGGER.info(String.format("Actual Target:%s", actualTarget));
                    LOGGER.info(String.format("Total Target:%s", totalTarget));
                }
                else if (currentUser instanceof EyDmsCustomerModel)
                {
                    EyDmsCustomerModel eydmsCustomer = (EyDmsCustomerModel) getUserService().getCurrentUser();
                    if(eydmsCustomer.getCounterType().equals(CounterType.SP)) {
                        if (customerFilteredList != null && !customerFilteredList.isEmpty()) {
                            actualTarget = getSalesPerformanceService().getActualTargetForSalesForMonthSP(customerFilteredList, year, month);
                            totalTarget = getSalesPerformanceService().getSalesTargetForMonthSP(customerFilteredList, year, month);
                        }
                    }
                    LOGGER.info(String.format("Actual Target:%s", actualTarget));
                    LOGGER.info(String.format("Total Target:%s", totalTarget));
                }

                if(actualTarget!=0.0 && counterPotential!=0.0) {
                    data.setActual((actualTarget / counterPotential) * 100);
                }
                else
                    data.setActual(0.0);

                if( totalTarget!=0.0 && counterPotential!=0.0){
                    data.setTarget((totalTarget / counterPotential)*100);
                }
                else
                    data.setTarget(0.0);
            }
            else
            {
                if(currentUser instanceof EyDmsUserModel) {
                    EyDmsUserModel eydmsUser = (EyDmsUserModel) getUserService().getCurrentUser();
                    actualTarget = getSalesPerformanceService().getActualTargetForSalesMTD(eydmsUser, currentBaseSite, doList, subAreaList);
                    totalTarget = getSalesPerformanceService().getMonthlySalesTarget(eydmsUser, currentBaseSite);

                    LOGGER.info(String.format("Actual Target:%s", actualTarget));
                    LOGGER.info(String.format("Total Target:%s", totalTarget));
                }
                else if (currentUser instanceof EyDmsCustomerModel) {
                    EyDmsCustomerModel eydmsCustomer = (EyDmsCustomerModel) getUserService().getCurrentUser();
                    if (eydmsCustomer.getCounterType().equals(CounterType.SP)) {
                        if (customerFilteredList != null && !customerFilteredList.isEmpty()) {
                            actualTarget = getSalesPerformanceService().getActualTargetForSalesSPMTD(customerFilteredList);
                            totalTarget = getSalesPerformanceService().getMonthlySalesTargetForSP(customerFilteredList);
                        }
                    }
                }

                if(actualTarget!=0.0 && counterPotential!=0.0) {
                    data.setActual((actualTarget / counterPotential) * 100);
                }
                else
                    data.setActual(0.0);

                if( totalTarget!=0.0 && counterPotential!=0.0){
                    data.setTarget((totalTarget / counterPotential) *100);
                }
                else
                    data.setTarget(0.0);
            }

        }
        else if(filter.equalsIgnoreCase("LastMonth"))
        {

            LocalDate lastMonth = currentMonth.minusMonths(1);

            if(currentUser instanceof EyDmsUserModel) {
                EyDmsUserModel eydmsUser = (EyDmsUserModel) getUserService().getCurrentUser();
                actualTarget = getSalesPerformanceService().getActualTargetForSalesLastMonth(eydmsUser, currentBaseSite, lastMonth.getYear(), lastMonth.getMonthValue(), doList, subAreaList);
                totalTarget = getSalesPerformanceService().getLastMonthSalesTarget(eydmsUser, currentBaseSite);
                LOGGER.info(String.format("Actual Target:%s", actualTarget));
                LOGGER.info(String.format("Total Target:%s", totalTarget));
            }

            if(actualTarget!=0.0 && counterPotential!=0.0) {
                data.setActual((actualTarget / counterPotential)*100);
            }
            else
                data.setActual(0.0);

            if( totalTarget!=0.0 && counterPotential!=0.0){
                data.setTarget((totalTarget / counterPotential)*100);
            }
            else
                data.setTarget(0.0);

        }

        else if(filter.equalsIgnoreCase("LastYear"))
        {
            if(currentUser instanceof EyDmsUserModel) {
                EyDmsUserModel eydmsUser = (EyDmsUserModel) getUserService().getCurrentUser();
                actualTarget=getSalesPerformanceService().getActualTargetForSalesLastYear(eydmsUser,currentBaseSite,doList,subAreaList);
                totalTarget=getSalesPerformanceService().getLastYearSalesTarget(eydmsUser,currentBaseSite);

                LOGGER.info(String.format("Actual Target:%s", actualTarget));
                LOGGER.info(String.format("Total Target:%s", totalTarget));
            }

            if(actualTarget!=0.0 && counterPotential!=0.0) {
                data.setActual((actualTarget / counterPotential)*100);
                double act=(actualTarget / counterPotential)*100;
                LOGGER.info(String.format("Actual Target:%s", act));
            }
            else
                data.setActual(0.0);

            if( totalTarget!=0.0 && counterPotential!=0.0){
                data.setTarget((totalTarget / counterPotential)*100);
                double tot=(actualTarget / counterPotential)*100;
                LOGGER.info(String.format("Actual Target:%s", tot));
            }
            else
                data.setTarget(0.0);
        }
        return data;
    }

    @Override
    public ProductMixVolumeAndRatioListData getProductwiseSalesPercentRatioAndVolumeRatio(String filter, Integer month1, Integer year1,List<String> doList,List<String> subAreaList) {
        double sumOfQuantity = 0.0;
        EyDmsUserModel eydmsUser = (EyDmsUserModel) getUserService().getCurrentUser();
        BaseSiteModel baseSite = baseSiteService.getCurrentBaseSite();
        ProductMixVolumeAndRatioListData productMixVolumeAndRatioListData = new ProductMixVolumeAndRatioListData();

        if(StringUtils.isBlank(filter)) {
            //productMixVolumeAndRatioListData = getProductMixVolumeAndRatioListDataForMTD(subArea, eydmsUser, baseSite, productMixVolumeAndRatioListData,null);

            if(month1 !=0 && year1!=0)
            {
                productMixVolumeAndRatioListData = getProductMixVolumeAndRatioListDataForMonthPicker(eydmsUser, baseSite, productMixVolumeAndRatioListData, month1, year1,doList,subAreaList);
                if (productMixVolumeAndRatioListData != null) return productMixVolumeAndRatioListData;
            }
            else
            {
                productMixVolumeAndRatioListData = getProductMixVolumeAndRatioListDataForMTD(eydmsUser, baseSite, productMixVolumeAndRatioListData,null,doList,subAreaList);
                if (productMixVolumeAndRatioListData != null) return productMixVolumeAndRatioListData;
            }
        }
        else if(filter.contains("MTD"))
        {

            productMixVolumeAndRatioListData = getProductMixVolumeAndRatioListDataForMTD(eydmsUser, baseSite, productMixVolumeAndRatioListData,null,doList,subAreaList);
            if (productMixVolumeAndRatioListData != null) return productMixVolumeAndRatioListData;
        }
        else if(filter.contains("YTD"))
        {
            productMixVolumeAndRatioListData = getProductMixVolumeAndRatioListDataForYTD(eydmsUser, baseSite, productMixVolumeAndRatioListData,null,doList,subAreaList);
            if (productMixVolumeAndRatioListData != null) return productMixVolumeAndRatioListData;
        }

        return productMixVolumeAndRatioListData;
    }

    private ProductMixVolumeAndRatioListData getProductMixVolumeAndRatioListDataForMonthPicker(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, ProductMixVolumeAndRatioListData productMixVolumeAndRatioListData, Integer month, Integer year,List<String> doList,List<String> subAreaList) {
        double sumOfQuantity;
        List<List<Object>> productMixRatio = getSalesPerformanceService().getProductwiseSalesPercentRatioAndVolumeRatioForMonthPicker(eydmsUser, baseSite, month,year,doList,subAreaList);
        List<ProductMixVolumeAndRatioData> productMixVolumeAndRatioDataList = new ArrayList<>();
        sumOfQuantity = productMixRatio.stream().filter(o->Objects.nonNull(o) && Objects.nonNull(o.get(2))).mapToDouble(o -> Objects.nonNull(o.get(2)) ? (double) o.get(2) : 0.0).sum();
        //sumOfQuantity = productMixRatio.stream().collect(Collectors.summingDouble(o -> (double) o.get(2)));

        if (productMixRatio != null && !productMixRatio.isEmpty()) {
            double finalSumOfQuantity = sumOfQuantity;
            productMixRatio.forEach(objects -> {
                ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                if (objects != null && !objects.isEmpty()) {
                    String productCode = (String) objects.get(0);
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

            List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(eydmsUser.getState());
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

        return productMixVolumeAndRatioListData;
    }

    private ProductMixVolumeAndRatioListData getProductMixVolumeAndRatioListDataForCustomerMonthPicker(EyDmsCustomerModel eydmsUser, BaseSiteModel baseSite, ProductMixVolumeAndRatioListData productMixVolumeAndRatioListData, Integer month, Integer year) {
        double sumOfQuantity;
        List<List<Object>> productMixRatio = getSalesPerformanceService().getProductwiseSalesPercentRatioAndVolumeRatioForCustomerMonthPicker(eydmsUser, baseSite, month,year);
        List<ProductMixVolumeAndRatioData> productMixVolumeAndRatioDataList = new ArrayList<>();
        sumOfQuantity = productMixRatio.stream().filter(o->Objects.nonNull(o) && Objects.nonNull(o.get(2))).mapToDouble(o -> Objects.nonNull(o.get(2)) ? (double) o.get(2) : 0.0).sum();
        //sumOfQuantity = productMixRatio.stream().collect(Collectors.summingDouble(o -> (double) o.get(2)));

        if (productMixRatio != null && !productMixRatio.isEmpty()) {
            double finalSumOfQuantity = sumOfQuantity;
            productMixRatio.forEach(objects -> {
                ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                if (objects != null && !objects.isEmpty()) {
                    String productCode = (String) objects.get(0);
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
            if (eydmsUser!= null) {
                List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(eydmsUser.getState());
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
    public ProductMixVolumeAndRatioListData getProductMixVolumeAndRatioListDataForMTD(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, ProductMixVolumeAndRatioListData productMixVolumeAndRatioListData,EyDmsCustomerModel customer,List<String> doList,List<String> subAreaList) {
        double sumOfQuantity;
        List<List<Object>> productMixRatio =
                getSalesPerformanceService().getProductwiseSalesPercentRatioAndVolumeRatio(eydmsUser, baseSite, customer, doList, subAreaList);
        List<ProductMixVolumeAndRatioData> productMixVolumeAndRatioDataList = new ArrayList<>();
        //   sumOfQuantity =productMixRatio.stream().filter(o->Objects.nonNull(o) && Objects.nonNull(o.get(2))).mapToDouble(o -> (double) o.get(2)).sum();
        sumOfQuantity = productMixRatio.stream().filter(o -> Objects.nonNull(o) && Objects.nonNull(o.get(2))).mapToDouble(o -> Objects.nonNull(o.get(2)) ? (double) o.get(2) : 0.0).sum();
        if (productMixRatio != null && !productMixRatio.isEmpty()) {
            double finalSumOfQuantity = sumOfQuantity;
            productMixRatio.forEach(objects -> {
                ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                if (objects != null && !objects.isEmpty()) {
                    String productCode = (String) objects.get(0);
                    String productName = (String) objects.get(1);
                    double productSalesPercentRatio = 0.0, pquantityInMT = 0.0;
                    productMixVolumeAndRatioData.setProductCode(productCode);
                    productMixVolumeAndRatioData.setProductName(productName);
                    //set product sales volume
                    if (objects.get(2) != null) {
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
            if(eydmsUser!=null){
                List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(eydmsUser.getState());
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
        return productMixVolumeAndRatioListData;
    }
    @Override
    public ProductMixVolumeAndRatioListData getProductMixVolumeAndRatioListDataForYTD(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, ProductMixVolumeAndRatioListData productMixVolumeAndRatioListData,EyDmsCustomerModel customerModel,List<String> doList,List<String> subAreaList) {
        double sumOfQuantity;
        List<List<Object>> productMixRatio = getSalesPerformanceService().getProductwiseSalesPercentRatioAndVolumeRatioForYTD(null, baseSite,customerModel,doList,subAreaList);
        List<ProductMixVolumeAndRatioData> productMixVolumeAndRatioDataList = new ArrayList<>();
        sumOfQuantity = productMixRatio.stream().filter(o->Objects.nonNull(o) && Objects.nonNull(o.get(2))).mapToDouble(o -> Objects.nonNull(o.get(2)) ? (double) o.get(2) : 0.0).sum();
        if (productMixRatio != null && !productMixRatio.isEmpty()) {
            double finalSumOfQuantity = sumOfQuantity;
            productMixRatio.forEach(objects -> {
                ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                if (objects != null && !objects.isEmpty()) {
                    String productCode = (String) objects.get(0);
                    String productName = (String) objects.get(1);
                    double productSalesPercentRatio=0.0,pquantityInMT=0.0;
                    productMixVolumeAndRatioData.setProductCode(productCode);
                    productMixVolumeAndRatioData.setProductName(productName);
                    //set product sales volume
                    if(objects.get(2)!=null) {
                        pquantityInMT = objects.size() > 2 ? (Double) objects.get(2) : 0.0;
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
            if (eydmsUser != null) {
                List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(eydmsUser.getState());
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
                List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(customerModel.getState());
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
    public PartnerDetailsDataForSales getPartnerDetailsForSales(String searchKeyWord, String filter, Integer year, Integer month,List<String> doList,List<String> subAreaList) {
        List<List<Object>> partnerDetailsForSales = salesPerformanceService.getPartnerDetailsForSales(searchKeyWord);
        PartnerDetailsDataForSales data=new PartnerDetailsDataForSales();
        for (List<Object> partnerDetailsForSale : partnerDetailsForSales) {

            String code = (String) partnerDetailsForSale.get(0);
            String name = (String) partnerDetailsForSale.get(1);
            String mobileNumber = (String) partnerDetailsForSale.get(2);
            String customerNumber = (String) partnerDetailsForSale.get(3);
            data.setCode(code);
            data.setName(name);
            data.setMobileNumber(mobileNumber);
            data.setCustomerNumber(customerNumber);

            EyDmsUserModel eydmsUser=(EyDmsUserModel) getUserService().getCurrentUser();
            BaseSiteModel currentBaseSite=baseSiteService.getCurrentBaseSite();
            double actualTarget = 0.0, totalTarget=0.0;
            double achievementPercentage=0.0;
            if(StringUtils.isBlank(filter))
            {
                if(year!=0 && month!=0)
                {
                    actualTarget=getSalesPerformanceService().getActualTargetForPartnerSalesForMonth(code,eydmsUser,currentBaseSite,month,year,doList,subAreaList);
                    data.setActualSales(actualTarget);

                    totalTarget=getSalesPerformanceService().getSalesTargetForPartnerMonth(code, eydmsUser, currentBaseSite, month, year);
                    //data.setTarget(totalTarget!=0.0?totalTarget:0.0);
                    data.setTarget(totalTarget);

                    if(actualTarget!=0.0 && totalTarget!=0.0)
                        achievementPercentage=(actualTarget/totalTarget)*100;
                    data.setAchievementPercentage(achievementPercentage);

                }
                else
                {
                    actualTarget=getSalesPerformanceService().getActualTargetForPartnerSalesMTD(code,eydmsUser,currentBaseSite,doList,subAreaList);
                    data.setActualSales(actualTarget);

                    totalTarget=getSalesPerformanceService().getMonthlySalesForPartnerTarget(code,eydmsUser,currentBaseSite);
                    data.setTarget(totalTarget);

                    if(actualTarget!=0.0 && totalTarget!=0.0)
                        achievementPercentage=(actualTarget/totalTarget)*100;
                    data.setAchievementPercentage(achievementPercentage);
                }

            }
            else
            {
                if(filter.contains("MTD"))
                {
                    actualTarget=getSalesPerformanceService().getActualTargetForPartnerSalesMTD(code,eydmsUser,currentBaseSite,doList,subAreaList);
                    data.setActualSales(actualTarget);

                    totalTarget=getSalesPerformanceService().getMonthlySalesForPartnerTarget(code,eydmsUser,currentBaseSite);
                    data.setTarget(totalTarget);

                    if(actualTarget!=0.0 && totalTarget!=0.0)
                        achievementPercentage=(actualTarget/totalTarget)*100;
                    data.setAchievementPercentage(achievementPercentage);

                }
                else if(filter.contains("YTD"))
                {
                    actualTarget=getSalesPerformanceService().getActualTargetForPartnerSalesYTD(code,eydmsUser,currentBaseSite,doList,subAreaList);
                    data.setActualSales(actualTarget);

                    totalTarget=getSalesPerformanceService().getAnnualSalesForPartnerTarget(code,eydmsUser,currentBaseSite);
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
        EyDmsCustomerModel currentUser = (EyDmsCustomerModel) getUserService().getUserForUID(customerCode);
        ProductMixVolumeAndRatioListData productMixVolumeAndRatioListData = new ProductMixVolumeAndRatioListData();
        List<List<Object>> productMixRatio = salesPerformanceService.getBrandwiseSalesPercentRatioAndVolumeRatioForCustomer(currentUser);
        List<ProductMixVolumeAndRatioData> productMixVolumeAndRatioDataList = new ArrayList<>();
        sumOfQuantity = productMixRatio.stream().collect(Collectors.summingDouble(o -> (double) o.get(2)));

        if (productMixRatio != null && !productMixRatio.isEmpty()) {
            double finalSumOfQuantity = sumOfQuantity;
            productMixRatio.forEach(objects -> {
                ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                if (objects != null && !objects.isEmpty()) {
                    String productCode = (String) objects.get(0);
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
        //EyDmsCustomerModel currentUser1 = (EyDmsCustomerModel) getUserService().getUserForUID(customerCode);
        EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
        ProductMixVolumeAndRatioListData productMixVolumeAndRatioListData = new ProductMixVolumeAndRatioListData();
        List<List<Object>> productMixRatio = salesPerformanceService.getProductMixPercentRatioAndVolumeRatioWithPoints(currentUser,filter);
        List<ProductMixVolumeAndRatioData> productMixVolumeAndRatioDataList = new ArrayList<>();
        sumOfQuantity = productMixRatio.stream().collect(Collectors.summingDouble(o -> (double) o.get(2)));

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
        // B2BCustomerModel customerModel=(B2BCustomerModel) getUserService().getCurrentUser();
        // EyDmsCustomerModel currentUser = (EyDmsCustomerModel) getUserService().getUserForUID(customerModel.getUid());
        EyDmsCustomerModel currentUser = (EyDmsCustomerModel) getUserService().getCurrentUser();
        BaseSiteModel currentBaseSite=baseSiteService.getCurrentBaseSite();
        SalesAndAchievementData data=new SalesAndAchievementData();
        double actualTarget=0.0, totalTarget=0.0, proratedTarget=0.0 ,achievementPercentage =0.0, behindProratedTarget=0.0, aheadProratedTarget=0.0;
       /* Calendar cal = Calendar.getInstance();
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

       /*
        //Local date=2023/05/22
        LOGGER.info(noOfDaysInTheMonth);//31
        LOGGER.info(dayOfYear);//142
        LOGGER.info(year);//2023
        LOGGER.info(totalNoOfDaysInYear);//365
        LOGGER.info(remainingDaysInTheYear);//223
        LOGGER.info(noOfDaysGoneBy);//142
        LOGGER.info(noOfDaysGoneByInTheMonth);//21*/
        if(StringUtils.isBlank(filter))
        {
            if(yearFilter!=0 && monthFilter!=0) {

                cal= LocalDate.of(yearFilter,monthFilter,cal.getDayOfMonth());
                noOfDaysGoneByInTheMonth = cal.getDayOfMonth() -1;

                if(currentUser.getCounterType().equals(CounterType.SP)){
                    RequestCustomerData requestData = new RequestCustomerData();
                    List<String> counterType = List.of(DEALER);
                    requestData.setCounterType(counterType);
                    List<EyDmsCustomerModel> customerforUser = territoryManagementService.getCustomerforUser(requestData);
                    if(customerforUser!=null && !customerforUser.isEmpty()) {
                        for (EyDmsCustomerModel customer : customerforUser) {
                            LOGGER.info(String.format("EyDms customer Model PK:%s", customer));
                        }
                    }
                    else{
                        LOGGER.info("SP List is Empty");
                    }
                    if(customerforUser!=null && !customerforUser.isEmpty()) {
                        actualTarget = getSalesPerformanceService().getActualTargetForSalesForMonthSP(customerforUser, yearFilter, monthFilter);
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

                else if(currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                    actualTarget = getSalesPerformanceService().getActualTargetForSalesForMonthDealer(currentUser, currentBaseSite, yearFilter, monthFilter,bgpFilter);
                    data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);
                    totalTarget = getSalesPerformanceService().getSalesTargetForMonthDealer(currentUser, currentBaseSite, yearFilter, monthFilter,bgpFilter);
                }
                else if(currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                    actualTarget = getSalesPerformanceService().getActualTargetForSalesForMonthRetailer(currentUser, currentBaseSite, yearFilter, monthFilter,bgpFilter);
                    data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);
                    totalTarget = getSalesPerformanceService().getSalesTargetForMonthRetailer(currentUser.getUid(), currentBaseSite, yearFilter, monthFilter,bgpFilter);
                }
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
            }
            else
            {
                if(currentUser.getCounterType().equals(CounterType.SP)){
                    RequestCustomerData requestData = new RequestCustomerData();
                    List<String> counterType = List.of(DEALER);
                    requestData.setCounterType(counterType);
                    List<EyDmsCustomerModel> customerforUser = territoryManagementService.getCustomerforUser(requestData);
                    if(customerforUser!=null && !customerforUser.isEmpty()) {
                        for (EyDmsCustomerModel customer : customerforUser) {
                            LOGGER.info(String.format("EyDms customer Model PK:%s", customer));
                        }
                    }
                    else{
                        LOGGER.info("SP List is Empty");
                    }
                    if(customerforUser!=null && !customerforUser.isEmpty()) {
                        actualTarget = getSalesPerformanceService().getActualTargetForSalesSPMTD(customerforUser);
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
                else if(currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                    actualTarget = getSalesPerformanceService().getActualTargetForSalesDealerMTD(currentUser, currentBaseSite,bgpFilter);
                    data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);
                    totalTarget = getSalesPerformanceService().getMonthlySalesTargetForDealer(currentUser, currentBaseSite,bgpFilter);
                }
                else if(currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                    actualTarget = getSalesPerformanceService().getActualTargetForSalesRetailerMTD(currentUser, currentBaseSite,bgpFilter);
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
            if(filter.contains("MTD"))
            {
                if(currentUser.getCounterType().equals(CounterType.SP)){
                    RequestCustomerData requestData = new RequestCustomerData();
                    List<String> counterType = List.of(DEALER);
                    requestData.setCounterType(counterType);
                    List<EyDmsCustomerModel> customerforUser = territoryManagementService.getCustomerforUser(requestData);
                    if(customerforUser!=null && !customerforUser.isEmpty()) {
                        for (EyDmsCustomerModel customer : customerforUser) {
                            LOGGER.info(String.format("EyDms customer Model PK:%s", customer));
                        }
                    }
                    else{
                        LOGGER.info("SP List is Empty");
                    }
                    if(customerforUser!=null && !customerforUser.isEmpty()) {
                        actualTarget = getSalesPerformanceService().getActualTargetForSalesSPMTD(customerforUser);
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
                else if(currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                    actualTarget = getSalesPerformanceService().getActualTargetForSalesDealerMTD(currentUser, currentBaseSite,bgpFilter);
                    data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);

                    totalTarget = getSalesPerformanceService().getMonthlySalesTargetForDealer(currentUser, currentBaseSite,bgpFilter);
                }
                else if(currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                    actualTarget = getSalesPerformanceService().getActualTargetForSalesRetailerMTD(currentUser, currentBaseSite,bgpFilter);
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
            else if(filter.contains("YTD"))
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
                    List<EyDmsCustomerModel> customerforUser = territoryManagementService.getCustomerforUser(requestData);
                    if(customerforUser!=null && !customerforUser.isEmpty()) {
                        for (EyDmsCustomerModel customer : customerforUser) {
                            LOGGER.info(String.format("EyDms customer Model PK:%s", customer));
                        }
                    }
                    else{
                        LOGGER.info("SP List is Empty");
                    }
                    if(customerforUser!=null && !customerforUser.isEmpty()) {
                        actualTarget = getSalesPerformanceService().getActualTargetForSalesYTDSP(customerforUser, currentBaseSite);
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
                else if(currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                    actualTarget = getSalesPerformanceService().getActualTargetForSalesYTDDealer(currentUser, currentBaseSite,bgpFilter);
                    data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);

                    totalTarget = getSalesPerformanceService().getAnnualSalesTargetForDealer(currentUser,bgpFilter);
                }
                else if(currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                    actualTarget = getSalesPerformanceService().getActualTargetForSalesYTDRetailer(currentUser, currentBaseSite,bgpFilter);
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
        EyDmsCustomerModel eydmsUser=(EyDmsCustomerModel) getUserService().getCurrentUser();
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        LeaderboardListData list=new LeaderboardListData();
        Date startDate;
        Date endDate;
        Date currentDate = Calendar.getInstance().getTime();
        int monthInNumber = currentDate.getMonth();
        List<LeaderboardData> leaderboardDataList=new ArrayList<>();

        if(leadType.equalsIgnoreCase(DEALER))
        {
            list.setEmpCode(eydmsUser.getUid());
            list.setEmpName(eydmsUser.getName());
            List<EyDmsCustomerModel> allSalesOfficersByState =new ArrayList<>();
            List<EyDmsCustomerModel> customerList = new ArrayList<>();
            DistrictMasterModel district = null;
            List<SubAreaMasterModel> subAreaMasterModelList = territoryManagementService.getTerritoriesForCustomer(eydmsUser);

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

            if (filter.equals("YTD")) {
                List<Date> dates = salesPerformanceService.getCurrentFinancialYearSales();
                startDate=dates.get(0);
                endDate=dates.get(1);
                //list.setEmpSale(salesPerformanceService.getSalesByDeliveryDate(subArea, eydmsUser, currentBaseSite, startDate, endDate));
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
            else if (filter.equals("MTD")) {
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
                EyDmsCustomerModel eydmsUserModel = (EyDmsCustomerModel) userService.getUserForUID(salesOfficer);
                int s =(int)(sales*100);
                LOGGER.info(String.format("sale is %s",String.valueOf(s)));
                int score=calculateScoreFromPercentile(s);
                LOGGER.info(String.format("score is %s",String.valueOf(score)));
                data.setScore(score);
                data.setCode(eydmsUserModel.getUid());
                data.setName(eydmsUserModel.getName());
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
                EyDmsCustomerModel eydmsUserModel = (EyDmsCustomerModel) userService.getUserForUID(salesOfficer);
                int s1 =(int)(sales*100);
                LOGGER.info(String.format("sale is %s",String.valueOf(s1)));
                int score=calculateScoreFromPercentile(s1);
                LOGGER.info(String.format("score is %s",String.valueOf(score)));
                data.setScore(score);
                data.setCode(eydmsUserModel.getUid());
                data.setName(eydmsUserModel.getName());
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
                EyDmsCustomerModel eydmsUserModel = (EyDmsCustomerModel) userService.getUserForUID(salesOfficer);
                int rank = scoreToRankMap.get(score);
                data.setRank(rank);
                data.setScore(score.intValue());
                data.setCode(eydmsUserModel.getUid());
                data.setName(eydmsUserModel.getName());
                finalLeaderBoard.add(data);

            }
            list.setSalesList(finalLeaderBoard);
        }

        if(leadType.equalsIgnoreCase(RETAILER)) {

            /*list.setEmpCode(eydmsUser.getUid());
            list.setEmpName(eydmsUser.getName());
            List<EyDmsCustomerModel> allSalesOfficersByTaluka = new ArrayList<>();
            List<EyDmsCustomerModel> customerList = new ArrayList<>();

                List<SubAreaMasterModel> subAreaMasterModelList = territoryManagementService.getTerritoriesForCustomer(eydmsUser);
                if (CollectionUtils.isNotEmpty(subAreaMasterModelList) && subAreaMasterModelList.get(0) != null) {
                    if (subAreaMasterModelList.get(0).getTaluka() != null) {
                        LOG.info("get district:" + subAreaMasterModelList.get(0).getTaluka());
                        String district1 = subAreaMasterModelList.get(0).getTaluka();
                        allSalesOfficersByTaluka = territoryManagementService.getAllSalesOfficersByTaluka(district1);
                        customerList = allSalesOfficersByTaluka.stream().filter(eydmsCustomerModel -> eydmsCustomerModel.getGroups().contains(getUserService().getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))).collect(Collectors.toList());
                        LOGGER.info("List of customers:" + allSalesOfficersByTaluka.size());
                        list.setDistrict(district1);
                    }
                }

            Map<String, Double> unsortedLeaderboard = new HashMap<>();
            Map<String, Double> unsortedPremiumSalesLeaderboard = new HashMap<>();
            if (filter.equals("MTD")) {
                startDate = getFirstDateOfMonth(monthInNumber);
                endDate = getLastDateOfMonth(monthInNumber);
                populateUnsortedSalesLeaderboardRetailer(currentBaseSite, startDate, endDate, customerList, unsortedLeaderboard);
                populatePremiumUnsortedSalesLeaderboardRetailer(currentBaseSite, startDate, endDate, customerList, unsortedPremiumSalesLeaderboard);
            } else if (filter.equals("YTD")) {
                List<Date> dates = salesPerformanceService.getCurrentFinancialYear();
                startDate = dates.get(0);
                endDate = dates.get(1);
                //list.setEmpSale(salesPerformanceService.getSalesByDeliveryDate(subArea, eydmsUser, currentBaseSite, startDate, endDate));
                List<Date> dates = salesPerformanceService.getCurrentFinancialYear();
                startDate=dates.get(0);
                endDate=dates.get(1);
                //list.setEmpSale(salesPerformanceService.getSalesByDeliveryDate(subArea, eydmsUser, currentBaseSite, startDate, endDate));
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
                    EyDmsCustomerModel eydmsUserModel = (EyDmsCustomerModel) userService.getUserForUID(salesOfficer);
                    double s =Double.valueOf(sales*100);
                    LOGGER.info(String.format("sale is %s",String.valueOf(s)));
                    int score=calculateScoreFromPercentile(s);
                    LOGGER.info(String.format("score is %s",String.valueOf(score)));
                    data.setScore(score);
                    data.setCode(eydmsUserModel.getUid());
                    data.setName(eydmsUserModel.getName());
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
                    EyDmsCustomerModel eydmsUserModel = (EyDmsCustomerModel) userService.getUserForUID(salesOfficer);
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
                    data.setCode(eydmsUserModel.getUid());
                    data.setName(eydmsUserModel.getName());
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
                    EyDmsCustomerModel eydmsUserModel = (EyDmsCustomerModel) userService.getUserForUID(salesOfficer);
                    flagFinal.addAndGet(1);
                    data.setRank(flagFinal.get());
                    data.setScore(score.intValue());
                    data.setCode(eydmsUserModel.getUid());
                    data.setName(eydmsUserModel.getName());
                    finalLeaderBoard.add(data);
                });
                list.setSalesList(finalLeaderBoard);*/
            list.setEmpCode(eydmsUser.getUid());
            list.setEmpName(eydmsUser.getName());
            List<EyDmsCustomerModel> allSalesOfficersByState =new ArrayList<>();
            List<EyDmsCustomerModel> customerList = new ArrayList<>();
            DistrictMasterModel district = null;
            SubAreaMasterModel subAreaMasterModel = null;
            List<SubAreaMasterModel> subAreaMasterModelList = territoryManagementService.getTerritoriesForCustomer(eydmsUser);

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

            if (filter.equals("YTD")) {
                List<Date> dates = salesPerformanceService.getCurrentFinancialYearSales();
                startDate=dates.get(0);
                endDate=dates.get(1);
                //list.setEmpSale(salesPerformanceService.getSalesByDeliveryDate(subArea, eydmsUser, currentBaseSite, startDate, endDate));
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
            else if (filter.equals("MTD")) {
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
                EyDmsCustomerModel eydmsUserModel = (EyDmsCustomerModel) userService.getUserForUID(salesOfficer);
                int s =(int)(sales*100);
                LOGGER.info(String.format("sale is %s",String.valueOf(s)));
                int score=calculateScoreFromPercentile(s);
                LOGGER.info(String.format("score is %s",String.valueOf(score)));
                data.setScore(score);
                data.setCode(eydmsUserModel.getUid());
                data.setName(eydmsUserModel.getName());
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
                EyDmsCustomerModel eydmsUserModel = (EyDmsCustomerModel) userService.getUserForUID(salesOfficer);
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
                data.setCode(eydmsUserModel.getUid());
                data.setName(eydmsUserModel.getName());
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
                EyDmsCustomerModel eydmsUserModel = (EyDmsCustomerModel) userService.getUserForUID(salesOfficer);
                int rank = scoreToRankMap.get(score);
                data.setRank(rank);
                data.setScore(score.intValue());
                data.setCode(eydmsUserModel.getUid());
                data.setName(eydmsUserModel.getName());
                finalLeaderBoard.add(data);

            }
            list.setSalesList(finalLeaderBoard);

        }
        return list;
    }

  /*  private void populateCashDiscountAvailedPercentage(BaseSiteModel currentBaseSite, Date startDate, Date endDate, List<EyDmsCustomerModel> customerList, Map<String, Double> unsortedCashDiscountAvailedPercentage) {
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

    private void populatePremiumUnsortedSalesLeaderboardRetailer(BaseSiteModel currentBaseSite, Date startDate, Date endDate, List<EyDmsCustomerModel> customerList, Map<String, Double> unsortedPremiumSalesLeaderboard) {
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
        EyDmsCustomerModel currentUser = (EyDmsCustomerModel) getUserService().getUserForUID(customerModel.getUid());*/
        EyDmsCustomerModel currentUser = (EyDmsCustomerModel) getUserService().getCurrentUser();
        if(StringUtils.isNotBlank(retailerId)){
            currentUser=(EyDmsCustomerModel) userService.getUserForUID(retailerId);
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
                    List<EyDmsCustomerModel> customerforUser = territoryManagementService.getCustomerforUser(requestData);
                    if(customerforUser!=null && !customerforUser.isEmpty()) {
                        for (EyDmsCustomerModel customer : customerforUser) {
                            LOGGER.info(String.format("EyDms customer Model PK:%s", customer));
                        }
                    }
                    if(customerforUser!=null && !customerforUser.isEmpty()) {
                        actualTarget = getSalesPerformanceService().getActualTargetForSalesForMonthSP(customerforUser, year, month);
                        data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);
                        totalTarget = getSalesPerformanceService().getSalesTargetForMonthSP(customerforUser, year, month);
                        data.setTotalTarget(totalTarget != 0.0 ? totalTarget : 0.0);
                    }
                    else{
                        data.setActualTarget(0.0);
                        data.setTotalTarget(0.0);
                    }
                }
                else if(currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                    actualTarget = getSalesPerformanceService().getActualTargetForSalesForMonthDealer(currentUser, currentBaseSite, year, month, bgpFilter);
                    data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);

                    totalTarget = getSalesPerformanceService().getSalesTargetForMonthDealer(currentUser, currentBaseSite, year, month,bgpFilter);
                    data.setTotalTarget(totalTarget != 0.0 ? totalTarget : 0.0);
                }
                else if(currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                    actualTarget = getSalesPerformanceService().getActualTargetForSalesForMonthRetailer(currentUser, currentBaseSite, year, month,bgpFilter);
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
                    List<EyDmsCustomerModel> customerforUser = territoryManagementService.getCustomerforUser(requestData);
                    if(customerforUser!=null && !customerforUser.isEmpty()) {
                        for (EyDmsCustomerModel customer : customerforUser) {
                            LOGGER.info(String.format("EyDms customer Model PK:%s", customer));
                        }
                    }
                    if(customerforUser!=null && !customerforUser.isEmpty()) {
                        actualTarget = getSalesPerformanceService().getActualTargetForSalesSPMTD(customerforUser);
                        data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);
                        totalTarget = getSalesPerformanceService().getMonthlySalesTargetForSP(customerforUser);
                        data.setTotalTarget(totalTarget != 0.0 ? totalTarget : 0.0);
                    }
                    else{
                        data.setActualTarget(0.0);
                        data.setTotalTarget(0.0);
                    }
                }
                else if(currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                    actualTarget = getSalesPerformanceService().getActualTargetForSalesDealerMTD(currentUser, currentBaseSite,bgpFilter);
                    data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);

                    totalTarget = getSalesPerformanceService().getMonthlySalesTargetForDealer(currentUser, currentBaseSite,bgpFilter);
                    data.setTotalTarget(totalTarget != 0.0 ? totalTarget : 0.0);
                }
                if(currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                    actualTarget = getSalesPerformanceService().getActualTargetForSalesRetailerMTD(currentUser, currentBaseSite,bgpFilter);
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
            if (filter.contains("MTD")) {
                if (currentUser.getCounterType().equals(CounterType.SP)) {
                    RequestCustomerData requestData = new RequestCustomerData();
                    List<String> counterTypes = List.of(DEALER);
                    requestData.setCounterType(counterTypes);
                    List<EyDmsCustomerModel> customerforUser = territoryManagementService.getCustomerforUser(requestData);
                    if (customerforUser != null && !customerforUser.isEmpty()) {
                        for (EyDmsCustomerModel customer : customerforUser) {
                            LOGGER.info(String.format("EyDms customer Model PK:%s", customer));
                        }
                    }
                    if (customerforUser != null && !customerforUser.isEmpty()) {
                        actualTarget = getSalesPerformanceService().getActualTargetForSalesSPMTD(customerforUser);
                        data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);
                        totalTarget = getSalesPerformanceService().getMonthlySalesTargetForSP(customerforUser);
                        data.setTotalTarget(totalTarget != 0.0 ? totalTarget : 0.0);
                    } else {
                        data.setActualTarget(0.0);
                        data.setTotalTarget(0.0);
                    }
                } else if (currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                    actualTarget = getSalesPerformanceService().getActualTargetForSalesDealerMTD(currentUser, currentBaseSite, bgpFilter);
                    data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);

                    totalTarget = getSalesPerformanceService().getMonthlySalesTargetForDealer(currentUser, currentBaseSite, bgpFilter);
                    data.setTotalTarget(totalTarget != 0.0 ? totalTarget : 0.0);
                }
                if (currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                    if(retailerId!=null)
                    {
                        actualTarget = getSalesPerformanceService().getActualTargetForSalesMTDRetailerList(currentUser, currentBaseSite);
                        data.setActualTarget(actualTarget != 0.0 ? (actualTarget / 20) : 0.0);

                        totalTarget = getSalesPerformanceService().getMonthlySalesTargetForRetailer(currentUser.getUid(), currentBaseSite, bgpFilter);
                        data.setTotalTarget(totalTarget != 0.0 ? (totalTarget/20) : 0.0);
                    }
                    else
                    {
                        actualTarget = getSalesPerformanceService().getActualTargetForSalesRetailerMTD(currentUser, currentBaseSite, bgpFilter);
                        data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);

                        totalTarget = getSalesPerformanceService().getMonthlySalesTargetForRetailer(currentUser.getUid(), currentBaseSite, bgpFilter);
                        data.setTotalTarget(totalTarget != 0.0 ? totalTarget : 0.0);
                    }
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
            } else if (filter.contains("YTD")) {

                if (currentUser.getCounterType().equals(CounterType.SP)) {
                    RequestCustomerData requestData = new RequestCustomerData();
                    List<String> counterTypes = List.of(DEALER);
                    requestData.setCounterType(counterTypes);
                    List<EyDmsCustomerModel> customerforUser = territoryManagementService.getCustomerforUser(requestData);
                    if (customerforUser != null && !customerforUser.isEmpty()) {
                        for (EyDmsCustomerModel customer : customerforUser) {
                            LOGGER.info(String.format("EyDms customer Model PK:%s", customer));
                        }
                    }
                    if (customerforUser != null && !customerforUser.isEmpty()) {
                        actualTarget = getSalesPerformanceService().getActualTargetForSalesYTDSP(customerforUser, currentBaseSite);
                        data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);
                        totalTarget = getSalesPerformanceService().getAnnualSalesTargetForSP(customerforUser);
                        data.setTotalTarget(totalTarget != 0.0 ? totalTarget : 0.0);
                    } else {
                        data.setActualTarget(0.0);
                        data.setTotalTarget(0.0);
                    }
                } else if (currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                    actualTarget = getSalesPerformanceService().getActualTargetForSalesYTDDealer(currentUser, currentBaseSite, bgpFilter);
                    data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);

                    totalTarget = getSalesPerformanceService().getAnnualSalesTargetForDealer(currentUser, bgpFilter);
                    data.setTotalTarget(totalTarget != 0.0 ? totalTarget : 0.0);
                }
                if (currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                    if(retailerId!=null)
                    {
                        actualTarget = getSalesPerformanceService().getActualTargetForSalesYTDRetailerList(currentUser, currentBaseSite);
                        data.setActualTarget(actualTarget != 0.0 ? (actualTarget / 20) : 0.0);

                        totalTarget = getSalesPerformanceService().getAnnualSalesTargetForRetailer(currentUser, bgpFilter);
                        data.setTotalTarget(totalTarget != 0.0 ? (totalTarget / 20) : 0.0);
                    }
                    else {
                        //actualTarget = getSalesPerformanceService().getActualTargetForSalesYTDRetailer(currentUser, currentBaseSite, bgpFilter);
                        actualTarget = getSalesPerformanceService().getActualTargetForSalesYTRetailer(currentUser, currentBaseSite, bgpFilter);
                        data.setActualTarget(actualTarget != 0.0 ? actualTarget : 0.0);

                        totalTarget = getSalesPerformanceService().getAnnualSalesTargetForRetailer(currentUser, bgpFilter);
                        data.setTotalTarget(totalTarget != 0.0 ? totalTarget : 0.0);
                    }
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
        EyDmsCustomerModel eydmsCustomer = (EyDmsCustomerModel) getUserService().getCurrentUser();
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
        String monthYear ="";

        if(eydmsCustomer.getCounterType().equals(CounterType.SP)) {
            List<DealerRevisedMonthlySalesModel> dealerRevisedMonthlySalesModel = new ArrayList<>();
            RequestCustomerData requestData = new RequestCustomerData();
            List<String> counterTypes = List.of(DEALER);
            requestData.setCounterType(counterTypes);
            List<EyDmsCustomerModel> customerforUser = territoryManagementService.getCustomerforUser(requestData);
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
                String monthName = getMonth(startDate1Target);
                String yearName = getYear(startDate1Target);
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
            else if (filter.equalsIgnoreCase("MTD")) {
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
                    var monthYear1 = EyDmsDateUtility.getFormattedDate(date, "MMM-YYYY");
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
            } else if (filter.equalsIgnoreCase("YTD")) {
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


        else if(eydmsCustomer.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
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
                for (int i = 1; i <= 6; i++) {
                    //1-10, 11- 20 and 21-31
                    LOG.info("10DayBucket :" + startDate + " " + endDate);
                    Date startDate1 = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    Date endDate1 = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    actualSale = salesPerformanceService.getActualTargetFor10DayBucketForDealer(eydmsCustomer, bgpFilter, startDate1, endDate1);
                    growth = actualSale - lastBucketSale;
                    lastBucketSale = actualSale;
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
                    LOGGER.info(String.format("Growth:%s",growth));
                    actualBucketSaleList.add(actualSale != 0.0 ? actualSale : 0.0);
                    GrowthBucketSaleList.add(growth != 0.0 ? growth : 0.0);

                }
                LOGGER.info((String.format("Actual Sales list:%s",String.valueOf(actualBucketSaleList.size()))));

                //target sales
                startDateTarget = LocalDate.of(currentMonth.getYear(), currentMonth.getMonthValue() - 1, 1);
                Date startDate1Target = Date.from(startDateTarget.atStartOfDay(ZoneId.systemDefault()).toInstant());
                String monthName = getMonth(startDate1Target);
                String yearName = getYear(startDate1Target);
                List<Double> targetBucketSaleList = new ArrayList<>();
                double revisedTarget=0.0,sumRevisedTarget=0.0;
                for (int i = 0; i <= 1; i++) {

                    DealerRevisedMonthlySalesModel dealerRevisedMonthlySalesModel = salesPerformanceService.getMonthlySaleTargetGraphForDealer(eydmsCustomer.getUid(), monthName, yearName);
                    if(StringUtils.isBlank(bgpFilter) || bgpFilter.equalsIgnoreCase("ALL")) {
                        /*if (dealerRevisedMonthlySalesModel != null) {
                            targetBucketSaleList.add(dealerRevisedMonthlySalesModel.getBucket1() != 0.0 ? dealerRevisedMonthlySalesModel.getBucket1() : 0.0);
                            targetBucketSaleList.add(dealerRevisedMonthlySalesModel.getBucket2() != 0.0 ? dealerRevisedMonthlySalesModel.getBucket2() : 0.0);
                            targetBucketSaleList.add(dealerRevisedMonthlySalesModel.getBucket3() != 0.0 ? dealerRevisedMonthlySalesModel.getBucket3() : 0.0);
                        }*/
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
                    if(GrowthBucketSaleList!=null && !GrowthBucketSaleList.isEmpty())
                        growth1 = GrowthBucketSaleList.get(i);


                    data.setActualSales(actualSales);
                    data.setTargetSales(targetSales);
                    data.setGrowth(growth1);
                    data.setMonthYear(monthYear);
                    dataList.add(data);
                }
            } else if (filter.equalsIgnoreCase("MTD")) {
                for (int i = 1; i <= 6; i++) {
                    MonthlySalesData data = new MonthlySalesData();
                    currentMonthSale = salesPerformanceService.getActualTargetForSalesForMonthDealer(eydmsCustomer, currentBaseSite, currentMonth.getYear(), currentMonth.getMonthValue(), bgpFilter);
                    LocalDate lastMonth = currentMonth.minusMonths(1);
                    lastMonthSale = salesPerformanceService.getActualTargetForSalesForMonthDealer(eydmsCustomer, currentBaseSite, lastMonth.getYear(), lastMonth.getMonthValue(), bgpFilter);
                    data.setActualSales(currentMonthSale);
                    //double growth = currentMonthSale - lastMonthSale;
                    //data.setGrowth(growth);

                    Date date = Date.from(currentMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    var monthYear1 = EyDmsDateUtility.getFormattedDate(date, "MMM-YYYY");
                    currentMonthTarget = salesPerformanceService.getSalesTargetForMonthDealer(eydmsCustomer, currentBaseSite, currentMonth.getYear(), currentMonth.getMonthValue(), bgpFilter);
                    lastMonthTarget = salesPerformanceService.getSalesTargetForMonthDealer(eydmsCustomer, currentBaseSite, lastMonth.getYear(), lastMonth.getMonthValue(), bgpFilter);

                    data.setTargetSales(currentMonthTarget);
                    data.setGrowth(lastMonthSale-currentMonthSale);

                    if (Objects.nonNull(monthYear1)) {
                        data.setMonthYear(monthYear1.replace("-", " "));
                    }
                    dataList.add(data);
                    currentMonth = lastMonth;
                }
            } else if (filter.equalsIgnoreCase("YTD")) {
                double ytdDealerTarget =0.0;
                List<Double> yearWiseTargetSales = new ArrayList<>();
                List<MonthlySalesData> ytdDataListDealer = new ArrayList<>();
                Date startDate1 = null, endDate1 = null, startDate2 = null, endDate2 = null,startDateForGrowth1 = null, endDateForGrowth1 =null,startDateForGrowth11=null,endDateForGrowth11=null;

                LocalDate currentDate = LocalDate.now();
                LocalDate startDate, endDate,startDateForGrowth,endDateForGrowth,startDateForGrowthT,endDateForGrowthT;
                LocalDate startDateMonthYear = LocalDate.now();
                List<Double> yearWiseSales = new ArrayList<>();
                List<Double> yearWiseGrowthSales = new ArrayList<>();
                LocalDate date = LocalDate.now();

                startDate = LocalDate.of(currentDate.getYear(), Month.APRIL, 1);
                endDate = LocalDate.of(currentDate.getYear() + 1, Month.APRIL, 1);
                for (int i = 0; i <= 2; i++) {
                    startDate1 = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    endDate1 = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

                    int currentYear = date.getYear();
                    StringBuilder f = new StringBuilder();
                    String financialYear= String.valueOf(f.append(String.valueOf(currentYear)).append("-").append(String.valueOf(currentYear+1)));
                    date = date.minusYears(1);

                    currentSales = salesPerformanceService.getActualSaleForDealerGraphYTD(eydmsCustomer, startDate1, endDate1, currentBaseSite, bgpFilter);
                    /*ytdDealerTarget = salesPerformanceService.getAnnualSalesTargetForDealer(eydmsCustomer,bgpFilter);*/
                    ytdDealerTarget = salesPerformanceService.getAnnualSalesTargetForDealerFY(eydmsCustomer,bgpFilter,financialYear);
                    startDate = startDate1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    endDate = endDate1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                    startDate = LocalDate.of(startDate.getYear() - 1, Month.APRIL, 1);//2021
                    endDate = LocalDate.of(endDate.getYear() - 1, Month.APRIL, 1);//2022

                    yearWiseSales.add(currentSales);
                    yearWiseTargetSales.add(ytdDealerTarget);
                }
                startDateForGrowth = LocalDate.of(currentDate.getYear(), Month.APRIL, 1);
                endDateForGrowth = LocalDate.of(currentDate.getYear() + 1, Month.APRIL, 1);
                for (int i = 0; i <= 2; i++) {
                    startDateForGrowth1 = Date.from(startDateForGrowth.atStartOfDay(ZoneId.systemDefault()).toInstant());//2023
                    endDateForGrowth1 = Date.from(endDateForGrowth.atStartOfDay(ZoneId.systemDefault()).toInstant());//2024


                    startDateForGrowthT = LocalDate.of(startDateForGrowth.getYear() - 1, Month.APRIL, 1);//2021
                    endDateForGrowthT = LocalDate.of(endDateForGrowth.getYear() - 1, Month.APRIL, 1);//2022
                    startDateForGrowth11 = Date.from(startDateForGrowthT.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    endDateForGrowth11 = Date.from(endDateForGrowthT.atStartOfDay(ZoneId.systemDefault()).toInstant());

                    currentSales = salesPerformanceService.getActualSaleForDealerGraphYTD(eydmsCustomer, startDateForGrowth1, endDateForGrowth1, currentBaseSite, bgpFilter);
                    lastYearSales= salesPerformanceService.getActualSaleForDealerGraphYTD(eydmsCustomer, startDateForGrowth11, endDateForGrowth11, currentBaseSite, bgpFilter);
                    growth=lastYearSales-currentSales;
                    startDateForGrowth = startDateForGrowth1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    endDateForGrowth = endDateForGrowth1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                    startDateForGrowth = LocalDate.of(startDateForGrowth.getYear() - 1, Month.APRIL, 1);//2021
                    endDateForGrowth = LocalDate.of(endDateForGrowth.getYear() - 1, Month.APRIL, 1);//2022

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
        else if(eydmsCustomer.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
            //10 day buckets not applicable for retailer
            List<MonthlySalesData> mtdDataListRetailer= new ArrayList<>();
            if (filter.equalsIgnoreCase("MTD")) {
                for (int i = 1; i <= 6; i++) {
                    MonthlySalesData data = new MonthlySalesData();
                    currentMonthSale = salesPerformanceService.getActualTargetForSalesForMonthRetailer(eydmsCustomer, currentBaseSite, currentMonth.getYear(), currentMonth.getMonthValue(), bgpFilter);
                    LocalDate lastMonth = currentMonth.minusMonths(1);
                    lastMonthSale = salesPerformanceService.getActualTargetForSalesForMonthRetailer(eydmsCustomer, currentBaseSite, lastMonth.getYear(), lastMonth.getMonthValue(), bgpFilter);
                    data.setActualSales(currentMonthSale);

                    Date date = Date.from(currentMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    var monthYear2 = EyDmsDateUtility.getFormattedDate(date, "MMM-YYYY");

                    targetSale = getSalesPerformanceService().getMonthlySalesTargetForRetailer(eydmsCustomer.getUid(), currentBaseSite,bgpFilter, monthYear2);
                    data.setTargetSales(targetSale);

                    if (Objects.nonNull(monthYear2)) {
                        data.setMonthYear(monthYear2.replace("-", " "));
                    }
                    dataList.add(data);
                    currentMonth = lastMonth;
                }
            }
            else if (filter.equalsIgnoreCase("YTD")) {
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
                    currentSales =  salesPerformanceDao.getMonthWiseForRetailerYTD(eydmsCustomer, startDate1, endDate1);

                    /*ytdDealerTarget = salesPerformanceService.getAnnualSalesTargetForRetailer(eydmsCustomer,bgpFilter);*/
                    ytdDealerTarget = salesPerformanceService.getAnnualSalesTargetForRetailerFY(eydmsCustomer,bgpFilter,financialYear);
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

    @Override
    public MonthlySalesListData getActualVsTargetSalesGraphForTSMRH(String filter,List<String> doList,List<String> subAreaList) {
        {
            EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
            BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
            MonthlySalesListData monthlySalesListData = new MonthlySalesListData();
            List<MonthlySalesData> dataList = new ArrayList<>();
            double targetSale =0.0;
            double currentMonthSale=0.0;
            double lastMonthSale = 0.0;
            double currentMonthTarget=0.0;
            double currentSales = 0.0;
            double lastMonthTarget=0.0;

            LocalDate currentMonth = LocalDate.now();
            LocalDate currentMonthForTarget = LocalDate.now();
            double actualSales =0.0,targetSales=0.0;
            String monthYear ="";

            if (filter.equalsIgnoreCase("10DayBucket")) {
                //actual sales
                List<Double> actualBucketSaleList = new ArrayList<>();
                double actualSale = 0.0;
                LocalDate startDate = null, endDate = null, startDateTarget = null;
                startDate = LocalDate.of(currentMonth.getYear(), currentMonth.getMonthValue() - 1, 1);
                endDate = startDate.plusDays(10);
                for (int i = 1; i <= 6; i++) {
                    //1-10, 11- 20 and 21-31
                    Date startDate1 = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    Date endDate1 = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    actualSale = salesPerformanceService.getActualTargetFor10DayBucket(currentUser, startDate1, endDate1,doList,subAreaList);
                    startDate = startDate1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    endDate = endDate1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    startDate = endDate.plusDays(1);
                    endDate = startDate.plusDays(10);
                    LOGGER.info(String.format("Actual Sales:%s",actualSale));
                    actualBucketSaleList.add(actualSale != 0.0 ? actualSale : 0.0);
                }
                LOGGER.info((String.format("Actual Sales list:%s",String.valueOf(actualBucketSaleList.size()))));

                //target sales
                startDateTarget = LocalDate.of(currentMonth.getYear(), currentMonth.getMonthValue() - 1, 1);
                Date startDate1Target = Date.from(startDateTarget.atStartOfDay(ZoneId.systemDefault()).toInstant());
                String monthName = getMonth(startDate1Target);
                String yearName = getYear(startDate1Target);
                List<Double> targetBucketSaleList = new ArrayList<>();
                double revisedTarget=0.0,sumRevisedTarget=0.0;
                for (int i = 0; i <= 1; i++) {

                    DealerRevisedMonthlySalesModel dealerRevisedMonthlySalesModel = salesPerformanceService.getMonthlySaleTargetGraph(currentUser, monthName, yearName,doList,subAreaList);
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
            } else if (filter.equalsIgnoreCase("MTD")) {
                for (int i = 1; i <= 6; i++) {
                    MonthlySalesData data = new MonthlySalesData();
                    currentMonthSale = salesPerformanceService.getActualTargetForSalesForMonth(currentUser, currentBaseSite, currentMonth.getYear(), currentMonth.getMonthValue(), doList,subAreaList);
                    LocalDate lastMonth = currentMonth.minusMonths(1);
                    lastMonthSale = salesPerformanceService.getActualTargetForSalesForMonth(currentUser, currentBaseSite, lastMonth.getYear(), lastMonth.getMonthValue(), doList,subAreaList);
                    data.setActualSales(currentMonthSale);
                    //double growth = currentMonthSale - lastMonthSale;
                    //data.setGrowth(growth);

                    Date date = Date.from(currentMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    var monthYear1 = EyDmsDateUtility.getFormattedDate(date, "MMM-YYYY");
                    currentMonthTarget = salesPerformanceService.getSalesTargetForMonth(currentUser, currentBaseSite, currentMonth.getYear(), currentMonth.getMonthValue(), doList,subAreaList);
                    lastMonthTarget = salesPerformanceService.getSalesTargetForMonth(currentUser, currentBaseSite, lastMonth.getYear(), lastMonth.getMonthValue(), doList,subAreaList);

                    data.setTargetSales(currentMonthTarget);

                    if (Objects.nonNull(monthYear1)) {
                        data.setMonthYear(monthYear1.replace("-", " "));
                    }
                    dataList.add(data);
                    currentMonth = lastMonth;
                }
            } else if (filter.equalsIgnoreCase("YTD")) {
                List<MonthlySalesData> ytdDataListDealer = new ArrayList<>();
                Date startDate1 = null, endDate1 = null, startDate2 = null, endDate2 = null;

                LocalDate currentDate = LocalDate.now();
                LocalDate startDate, endDate;
                LocalDate startDateMonthYear = LocalDate.now();
                List<Double> yearWiseSales = new ArrayList<>();

                startDate = LocalDate.of(currentDate.getYear() - 1, Month.APRIL, 1);
                endDate = LocalDate.of(currentDate.getYear(), Month.APRIL, 1);
                for (int i = 0; i <= 2; i++) {
                    startDate1 = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    endDate1 = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());


                    currentSales = salesPerformanceService.getActualSaleForGraphYTD(currentUser, startDate1, endDate1, currentBaseSite, doList,subAreaList);

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
            monthlySalesListData.setSales(dataList);
            return monthlySalesListData;
        }
    }

    @Override
    public ProratedBreachData getProratedBreach(List<String> doList,List<String> subAreaList) {
        EyDmsUserModel eydmsUser=(EyDmsUserModel) getUserService().getCurrentUser();
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


        //actualTarget=getSalesPerformanceService().getActualTargetForSalesMTD(subArea,eydmsUser,currentBaseSite);
        actualTarget=getSalesPerformanceService().getActualTargetForSalesMTD(eydmsUser,currentBaseSite,doList,subAreaList);
        data.setActual(actualTarget);

        totalTarget=getSalesPerformanceService().getMonthlySalesTarget(eydmsUser,currentBaseSite);
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
    public Map<String, Object> getDirectDispatchOrdersMTDPercentage(int month, int year,List<String> doList, List<String> subAreaList) {
        return salesPerformanceService.getDirectDispatchOrdersMTDPercentage(month, year,doList,subAreaList);
    }

    @Override
    public SearchPageData<SalesPerformNetworkDetailsData> getZeroLiftingViewDetailsWithPagination(String fields, String customerType, String searchKey, List<String> doList, List<String> subAreaList, SearchPageData searchPageData) {
        BaseSiteModel currentBaseSite=baseSiteService.getCurrentBaseSite();
        return salesPerformanceService.getZeroLiftingViewDetailsWithPagination(fields,currentBaseSite,customerType,searchKey,doList,subAreaList,searchPageData);
    }

    public LowPerformingNetworkData getLowPerformingNetworkDataForDealerRetailerInfluencers( String leadType, int month, int year,String filter,List<String> doList, List<String> subAreaList) {
        BaseSiteModel currentBaseSite=baseSiteService.getCurrentBaseSite();
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();

        List<SalesPerformNetworkDetailsData> collect1=new ArrayList<>();
        List<SalesPerformNetworkDetailsData> collect=new ArrayList<>();
        List<SalesPerformNetworkDetailsData> data1=new ArrayList<>();
        LowPerformingNetworkData data=new LowPerformingNetworkData();

        RequestCustomerData requestCustomerData = new RequestCustomerData();
        List<SubAreaMasterModel> soList=new ArrayList<>();
        List<DistrictMasterModel> districtList=new ArrayList<>();
        if(subAreaList!=null && !subAreaList.isEmpty()){
            for (String id : subAreaList) {
                soList.add(territoryManagementDao.getTerritoryById(id));
            }
            if(soList.get(0)!=null)
                requestCustomerData.setSubAreaMasterPk(soList.get(0).getPk().toString());
        }
        if (doList != null && !doList.isEmpty()) {
            for (String code : doList) {
                districtList.add(districtMasterDao.findByCode(code));
            }
            if(districtList.get(0)!=null)
                requestCustomerData.setDistrict(districtList.get(0).getName());
        }

        if(leadType.equalsIgnoreCase(RETAILER)){
            List<EyDmsCustomerModel> retailer=new ArrayList<>();
            List<SalesPerformNetworkDetailsData> retailerDetailedSummaryListData=new ArrayList<>();
            if((currentUser instanceof EyDmsUserModel) || (((EyDmsCustomerModel)currentUser).getCounterType()!=null && ((EyDmsCustomerModel)currentUser).getCounterType().equals(CounterType.SP))){
                requestCustomerData.setCounterType(List.of(RETAILER));
                retailer = territoryManagementService.getCustomerforUser(requestCustomerData);
            }
            if(retailer!=null && !retailer.isEmpty()) {
                retailer = salesPerformanceDao.getEyDmsCustomerLastLiftingList(retailer);
            }
            if(retailer!=null && !retailer.isEmpty()) {
                retailerDetailedSummaryListData = getRetailerDetailedSummaryListData(retailer, doList, subAreaList);
                LOGGER.info((String.format("Size for RETAILER:%s", retailerDetailedSummaryListData.size())));
                collect1 = retailerDetailedSummaryListData.stream().filter(nw -> Objects.nonNull(nw.getSalesQuantity())).filter(nw -> Objects.nonNull(nw.getSalesQuantity().getCurrent())).filter(nw -> nw.getSalesQuantity().getCurrent() != 0).collect(Collectors.toList());
                LOGGER.info((String.format("Size for Retailer:%s", collect1.size())));
                List<SalesPerformNetworkDetailsData> salesPerformNetworkDetailsData1 = collect1.stream().sorted(Comparator.comparing(nw -> nw.getSalesQuantity().getCurrent())).collect(Collectors.toList());
                collect = salesPerformNetworkDetailsData1.stream().limit((long) collect1.size() * LOWPERCENTAGE / 100).collect(Collectors.toList());
                data.setCount(collect.size());

                Double avgOfCounterShare = 0.0;
                if (collect.size() != 0) {
                    avgOfCounterShare = collect.stream().filter(obj -> Objects.nonNull(obj.getCounterSharePercentage())).mapToDouble(SalesPerformNetworkDetailsData::getCounterSharePercentage).sum();
                    data.setCounterSharePercentage(avgOfCounterShare);
                } else {
                    avgOfCounterShare = 0.0;
                    data.setCounterSharePercentage(avgOfCounterShare);
                }

                Double totalMonthlyPotential = collect.stream().filter(obj -> Objects.nonNull(obj.getPotential())).mapToDouble(objects -> Double.parseDouble(objects.getPotential())).sum();
                data.setTotalMonthlyPotential(totalMonthlyPotential);

                Double avgMonthlyOrders = 0.0;
                if (collect.size() != 0) {
                    avgMonthlyOrders = collect.stream().filter(nw -> nw.getSalesQuantity().getCurrent() != 0).mapToDouble(objects -> objects.getSalesQuantity().getCurrent()).sum() / collect.size();
                    data.setCountOfAvgMonthlyOrders(avgMonthlyOrders);
                } else {
                    avgMonthlyOrders = 0.0;
                    data.setCountOfAvgMonthlyOrders(avgMonthlyOrders);
                }
            }
        }
        else if(leadType.equalsIgnoreCase(DEALER)) {
            return salesPerformanceService.getLowPerformingSummaryData(month, year, filter, currentBaseSite, leadType, doList, subAreaList);
        }
        else if(leadType.equalsIgnoreCase("INFLUENCER")) {
            List<SalesPerformNetworkDetailsData> influencerDetailedSummaryListData=new ArrayList<>();
            List<EyDmsCustomerModel> influencer=new ArrayList<>();
            if((currentUser instanceof EyDmsUserModel) || (((EyDmsCustomerModel)currentUser).getCounterType()!=null && ((EyDmsCustomerModel)currentUser).getCounterType().equals(CounterType.SP))){
                requestCustomerData.setCounterType(List.of("Influencer"));
                influencer = territoryManagementService.getCustomerforUser(requestCustomerData);
            }
            if(influencer!=null && !influencer.isEmpty()) {
                influencer = salesPerformanceDao.getEyDmsCustomerLastLiftingList(influencer);
            }
            if(influencer!=null && !influencer.isEmpty()) {
                influencerDetailedSummaryListData = getInfluencerDetailedSummaryListData(influencer, doList, subAreaList);
                LOGGER.info((String.format("Size for INFLUENCER:%s", influencerDetailedSummaryListData.size())));
                collect1 = influencerDetailedSummaryListData.stream().filter(nw -> Objects.nonNull(nw.getBagLifted())).filter(nw -> nw.getBagLifted() != 0).collect(Collectors.toList());
                collect = collect1.stream().filter(nw -> nw.getBagLifted() != 0.0).collect(Collectors.toList());
                if (collect.size() > 0) {
                    data1 = collect.stream().sorted(Comparator.comparing(SalesPerformNetworkDetailsData::getBagLifted)).collect(Collectors.toList());
                }
                LOGGER.info((String.format("Size for INFLUENCER:%s", data1.size())));
                List<SalesPerformNetworkDetailsData> finalCollect = data1.stream().limit((long) collect1.size() * LOWPERCENTAGE / 100).collect(Collectors.toList());
                data.setCount(finalCollect.size());
                data.setTotalMonthlyPotential(0.0);
                data.setCounterSharePercentage(0.0);

                double avgMonthlyOrders = 0.0;
                if (collect.size() != 0) {
                    avgMonthlyOrders = finalCollect.stream().mapToDouble(SalesPerformNetworkDetailsData::getBagLifted).sum() / collect.size();
                    data.setCountOfAvgMonthlyOrders(avgMonthlyOrders);
                } else {
                    avgMonthlyOrders = 0.0;
                    data.setCountOfAvgMonthlyOrders(avgMonthlyOrders);
                }
            }
        }
        return data;
    }

    @Override
    public LowPerformingNetworkData getLowPerformingCountDetailsDealer(String leadType) {
        return salesPerformanceService.getLowPerformingSummaryDataDealer(leadType);
    }

    @Override
    public SalesPerformNetworkDetailsListData getListOfAllLowPerformingDealerRetailerInfluencers( String fields,String leadType, String searchKey,List<String> doList, List<String> subAreaList) {
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        SalesPerformNetworkDetailsListData listData=new SalesPerformNetworkDetailsListData();
        List<SalesPerformNetworkDetailsData> collect1=new ArrayList<>();
        List<SalesPerformNetworkDetailsData> collect=new ArrayList<>();
        List<SalesPerformNetworkDetailsData> data=new ArrayList<>();

        RequestCustomerData requestCustomerData = new RequestCustomerData();
        List<SubAreaMasterModel> soList=new ArrayList<>();
        List<DistrictMasterModel> districtList=new ArrayList<>();
        if(subAreaList!=null && !subAreaList.isEmpty()){
            for (String id : subAreaList) {
                soList.add(territoryManagementDao.getTerritoryById(id));
            }
            if(soList.get(0)!=null)
                requestCustomerData.setSubAreaMasterPk(soList.get(0).getPk().toString());
        }
        if (doList != null && !doList.isEmpty()) {
            for (String code : doList) {
                districtList.add(districtMasterDao.findByCode(code));
            }
            if(districtList.get(0)!=null)
                requestCustomerData.setDistrict(districtList.get(0).getName());
        }


        if(leadType.equalsIgnoreCase(RETAILER)){
            List<SalesPerformNetworkDetailsData> retailerDetailedSummaryListData=new ArrayList<>();
            List<EyDmsCustomerModel> retailer=new ArrayList<>();
            if((currentUser instanceof EyDmsUserModel) || (((EyDmsCustomerModel)currentUser).getCounterType()!=null && ((EyDmsCustomerModel)currentUser).getCounterType().equals(CounterType.SP))){
                requestCustomerData.setCounterType(List.of(RETAILER));
                retailer = territoryManagementService.getCustomerforUser(requestCustomerData);
            }

            if(retailer!=null && !retailer.isEmpty()) {
                retailer = salesPerformanceDao.getEyDmsCustomerLastLiftingList(retailer);
            }

            if (StringUtils.isNotBlank(searchKey)) {
                retailer=salesPerformanceService.filterEyDmsCustomersWithSearchTerm(retailer,searchKey);
            }

            if(retailer!=null && !retailer.isEmpty()) {
                retailerDetailedSummaryListData = getRetailerDetailedSummaryListData(retailer, doList, subAreaList);
                LOGGER.info((String.format("Size for RETAILER:%s", retailerDetailedSummaryListData.size())));
                collect = retailerDetailedSummaryListData.stream().filter(nw -> Objects.nonNull(nw.getSalesQuantity())).filter(nw -> Objects.nonNull(nw.getSalesQuantity().getCurrent())).filter(nw -> nw.getSalesQuantity().getCurrent() != 0).collect(Collectors.toList());
                if (collect.size() > 0)
                    data = collect.stream().sorted(Comparator.comparing(nw -> nw.getSalesQuantity().getCurrent())).collect(Collectors.toList());
                if (data.size() > 0) {
                    listData.setNetworkDetails(data.stream().limit(((long) data.size() * LOWPERCENTAGE) / 100).collect(Collectors.toList()));
                } else {
                    List<SalesPerformNetworkDetailsData> salesPerformNetworkDetailsData = new ArrayList<>();
                    listData.setNetworkDetails(salesPerformNetworkDetailsData);
                }
            }
        }
        else if(leadType.equalsIgnoreCase(DEALER)) {
            return salesPerformanceService.getListOfAllLowPerforming(fields,leadType,searchKey,doList,subAreaList);
        }
        else if(leadType.equalsIgnoreCase("INFLUENCER")){
            List<EyDmsCustomerModel> influencer=new ArrayList<>();
            if((currentUser instanceof EyDmsUserModel) || (((EyDmsCustomerModel)currentUser).getCounterType()!=null && ((EyDmsCustomerModel)currentUser).getCounterType().equals(CounterType.SP))){
                requestCustomerData.setCounterType(List.of("Influencer"));
                influencer = territoryManagementService.getCustomerforUser(requestCustomerData);
            }
            if(influencer!=null && !influencer.isEmpty()) {
                influencer = salesPerformanceDao.getEyDmsCustomerLastLiftingList(influencer);
            }
            if (StringUtils.isNotBlank(searchKey)) {
                influencer=salesPerformanceService.filterEyDmsCustomersWithSearchTerm(influencer,searchKey);
            }
            if(influencer!=null && !influencer.isEmpty()) {
                List<SalesPerformNetworkDetailsData> influencerDetailedSummaryListData = getInfluencerDetailedSummaryListData(influencer, doList, subAreaList);
                LOGGER.info((String.format("Size for INFLUENCER:%s", influencerDetailedSummaryListData.size())));
                collect1 = influencerDetailedSummaryListData.stream().filter(nw -> Objects.nonNull(nw.getBagLifted())).filter(nw -> nw.getBagLifted() != 0).collect(Collectors.toList());
                collect = collect1.stream().filter(nw -> nw.getBagLifted() != 0.0).collect(Collectors.toList());
                if (collect.size() > 0) {
                    data = collect.stream().sorted(Comparator.comparing(SalesPerformNetworkDetailsData::getBagLifted)).collect(Collectors.toList());
                }
                if (data.size() > 0) {
                    listData.setNetworkDetails(data.stream().limit(((long) data.size() * LOWPERCENTAGE) / 100).collect(Collectors.toList()));
                } else {
                    List<SalesPerformNetworkDetailsData> salesPerformNetworkDetailsData = new ArrayList<>();
                    listData.setNetworkDetails(salesPerformNetworkDetailsData);
                }
            }
        }
        return listData;
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
    public ZeroLiftingNetworkData getCountAndPotentialForZeroLifting(String leadType,int month,int year,String filter,List<String> doList, List<String> subAreaList) {
        BaseSiteModel site=baseSiteService.getCurrentBaseSite();
        return salesPerformanceService.getZeroLiftingSummaryData(month,year,filter,site,leadType,doList,subAreaList);
    }

    @Override
    public ZeroLiftingNetworkData getCountAndPotentialForZeroLiftingDealer(String leadType) {
        return salesPerformanceService.getZeroLiftingSummaryDataDealer(leadType);
    }

    @Override
    public SalesPerformNetworkDetailsListData getListOfAllZeroLiftingDealerRetailerInfluencers( String fields, String leadType, String filter,List<String> doList, List<String> subAreaList) {
        BaseSiteModel currentBaseSite=baseSiteService.getCurrentBaseSite();
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        SalesPerformNetworkDetailsListData listData=new SalesPerformNetworkDetailsListData();
        List<SalesPerformNetworkDetailsData> collect1=new ArrayList<>();
        List<SalesPerformNetworkDetailsData> collect=new ArrayList<>();
        List<SalesPerformNetworkDetailsData> data=new ArrayList<>();

        RequestCustomerData requestCustomerData = new RequestCustomerData();
        List<SubAreaMasterModel> soList=new ArrayList<>();
        List<DistrictMasterModel> districtList=new ArrayList<>();
        if(subAreaList!=null && !subAreaList.isEmpty()){
            for (String id : subAreaList) {
                soList.add(territoryManagementDao.getTerritoryById(id));
            }
            if(soList.get(0)!=null)
                requestCustomerData.setSubAreaMasterPk(soList.get(0).getPk().toString());
        }
        if (doList != null && !doList.isEmpty()) {
            for (String code : doList) {
                districtList.add(districtMasterDao.findByCode(code));
            }
            if(districtList.get(0)!=null)
                requestCustomerData.setDistrict(districtList.get(0).getName());
        }
        if(leadType.equalsIgnoreCase(RETAILER)){
            List<SalesPerformNetworkDetailsData> retailerDetailedSummaryListData=new ArrayList<>();
            List<EyDmsCustomerModel> retailer=new ArrayList<>();
            if((currentUser instanceof EyDmsUserModel) || (((EyDmsCustomerModel)currentUser).getCounterType()!=null && ((EyDmsCustomerModel)currentUser).getCounterType().equals(CounterType.SP))){
                requestCustomerData.setCounterType(List.of(RETAILER));
                retailer = territoryManagementService.getCustomerforUser(requestCustomerData);
            }
            LOGGER.info(String.format("size of Cust before:%s",retailer.size()));
            if(retailer!=null && !retailer.isEmpty()) {
                retailer = salesPerformanceDao.getEyDmsCustomerZeroLiftingList(retailer);
            }
            if (StringUtils.isNotBlank(filter)) {
                retailer=salesPerformanceService.filterEyDmsCustomersWithSearchTerm(retailer,filter);
            }
            LOGGER.info(String.format("size of Cust after:%s",retailer.size()));
            if(retailer!=null && !retailer.isEmpty()) {
                retailerDetailedSummaryListData = getRetailerDetailedSummaryListData(retailer, doList, subAreaList);
                LOGGER.info((String.format("Size for RETAILER:%s", retailerDetailedSummaryListData.size())));
                collect = retailerDetailedSummaryListData.stream().filter(nw -> Objects.nonNull(nw.getSalesQuantity())).filter(nw -> Objects.nonNull(nw.getSalesQuantity().getCurrent())).filter(nw -> nw.getSalesQuantity().getCurrent() == 0).collect(Collectors.toList());
                data = collect.stream().sorted(Comparator.comparing(nw -> nw.getSalesQuantity().getCurrent())).collect(Collectors.toList());
                if (data.size() > 0) {
                    listData.setNetworkDetails(data);
                } else {
                    List<SalesPerformNetworkDetailsData> salesPerformNetworkDetailsData = new ArrayList<>();
                    listData.setNetworkDetails(salesPerformNetworkDetailsData);
                }
            }
        }
        else if(leadType.equalsIgnoreCase(DEALER)) {
            return salesPerformanceService.getListOfAllZeroLifting(fields,currentBaseSite,leadType,filter,doList,subAreaList);
        }
        else if(leadType.equalsIgnoreCase("INFLUENCER")) {
            List<SalesPerformNetworkDetailsData> influencerDetailedSummaryListData = new ArrayList<>();
            List<EyDmsCustomerModel> influencer = new ArrayList<>();
            if ((currentUser instanceof EyDmsUserModel) || (((EyDmsCustomerModel) currentUser).getCounterType() != null && ((EyDmsCustomerModel) currentUser).getCounterType().equals(CounterType.SP))) {
                requestCustomerData.setCounterType(List.of("Influencer"));
                influencer = territoryManagementService.getCustomerforUser(requestCustomerData);
            }
            LOGGER.info(String.format("size of Cust after:%s", influencer.size()));
            if (influencer != null && !influencer.isEmpty()) {
                influencer = salesPerformanceDao.getEyDmsCustomerZeroLiftingList(influencer);
            }
            if (StringUtils.isNotBlank(filter)) {
                influencer=salesPerformanceService.filterEyDmsCustomersWithSearchTerm(influencer,filter);
            }
            LOGGER.info(String.format("size of Cust after:%s", influencer.size()));
            if (influencer != null && !influencer.isEmpty()) {
                influencerDetailedSummaryListData = getInfluencerDetailedSummaryListData(influencer, doList, subAreaList);
                LOGGER.info((String.format("Size for INFLUENCER:%s", influencerDetailedSummaryListData.size())));
                collect = influencerDetailedSummaryListData.stream().filter(nw -> Objects.nonNull(nw.getBagLifted())).filter(nw -> nw.getBagLifted() == 0 || nw.getBagLifted() == 0.0).collect(Collectors.toList());
                data = collect.stream().sorted(Comparator.comparing(SalesPerformNetworkDetailsData::getBagLifted)).collect(Collectors.toList());
                if (data.size() > 0) {
                    listData.setNetworkDetails(data);
                } else {
                    List<SalesPerformNetworkDetailsData> salesPerformNetworkDetailsData = new ArrayList<>();
                    listData.setNetworkDetails(salesPerformNetworkDetailsData);
                }
            }
        }
        return listData;
    }

    @Override
    public SalesPerformNetworkDetailsListData getListOfAllZeroLiftingRetailersInfluencerForDealers(String fields, String leadType, String searchKey) {
        return salesPerformanceService.getListOfAllZeroLiftingRetailerInfForDealer(fields,leadType,searchKey);
    }

    @Override
    public Integer getCountForAllDealerRetailerInfluencers(String leadType,List<String> subAreaList,List<String> doList) {
        return salesPerformanceService.getCountForAllDealerRetailerInfluencers(leadType, null,null,subAreaList, doList);
    }

    @Override
    public Integer getCountOfAllRetailersInfluencers(String leadType) {
        return salesPerformanceService.getCountOfAllRetailersInfluencers(leadType);
    }

    @Override
    public SalesPerformNetworkDetailsListData getBottomLaggingCounters(List<String> doList, List<String> subAreaList) {
        //  EyDmsUserModel eydmsUser=(EyDmsUserModel) getUserService().getCurrentUser();
        //BaseSiteModel currentBaseSite=baseSiteService.getCurrentBaseSite();
        return salesPerformanceService.getBottomLaggingCounters(doList,subAreaList);
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
        EyDmsUserModel eydmsUser=(EyDmsUserModel) getUserService().getCurrentUser();
        BaseSiteModel currentBaseSite=baseSiteService.getCurrentBaseSite();

        String state = eydmsUser.getState();
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

        EyDmsUserModel eydmsUser=(EyDmsUserModel) getUserService().getCurrentUser();
        BaseSiteModel currentBaseSite=baseSiteService.getCurrentBaseSite();
        String state = eydmsUser.getState();
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
        EyDmsUserModel eydmsUser=(EyDmsUserModel) getUserService().getCurrentUser();
        BaseSiteModel currentBaseSite=baseSiteService.getCurrentBaseSite();
        return salesPerformanceService.getSalesHistoryForDealers(subArea, eydmsUser, currentBaseSite);
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
            EyDmsCustomerModel customer, String customerType,List<String> doList,List<String> subAreaList) {
        double sumOfQuantity = 0.0;
        //EyDmsUserModel eydmsUser = (EyDmsUserModel) getUserService().getCurrentUser();
        BaseSiteModel baseSite = baseSiteService.getCurrentBaseSite();
        List<List<Object>> productMixRatio = getSalesPerformanceService().getProductwiseSalesPercentRatioAndVolumeRatioForCustomer(customer, baseSite, customerType,doList,subAreaList);
        ProductMixVolumeAndRatioListData productMixVolumeAndRatioListData = new ProductMixVolumeAndRatioListData();
        List<ProductMixVolumeAndRatioData> productMixVolumeAndRatioDataList = new ArrayList<>();
        sumOfQuantity = productMixRatio.stream().collect(Collectors.summingDouble(o-> (double) o.get(2)));

        if (productMixRatio != null && !productMixRatio.isEmpty()) {
            double finalSumOfQuantity = sumOfQuantity;
            productMixRatio.forEach(objects -> {
                ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                if (objects != null && !objects.isEmpty()) {
                    String productCode = (String) objects.get(0);
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
        else
        {
            if (null != customer.getState() && !(customer.getState().isEmpty())) {
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

        return productMixVolumeAndRatioListData;
    }

    @Override
    public ProductMixVolumeAndRatioListData getProductwiseSalesPercentRatioAndVolumeRatioForCust(String filter, int month1, int year1, List<String> doList, List<String> subAreaList) {
        EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
        ProductMixVolumeAndRatioListData data=new ProductMixVolumeAndRatioListData();
        String customerType=null;

        if(StringUtils.isBlank(filter)) {
            //productMixVolumeAndRatioListData = getProductMixVolumeAndRatioListDataForMTD(subArea, eydmsUser, baseSite, productMixVolumeAndRatioListData,null);

            if (month1 != 0 && year1 != 0) {
                data = getProductMixVolumeAndRatioListDataForCustomerMonthPicker(currentUser, baseSiteService.getCurrentBaseSite(), data, month1, year1);
                if (data != null) return data;
            }
            else {
                data= getProductwiseSalesPercentRatioAndVolumeRatioForCustomer(currentUser, customerType, null, null);
                if (data != null) return data;
            }
        }
        else if(filter.equalsIgnoreCase("MTD")) {
            data= getProductwiseSalesPercentRatioAndVolumeRatioForCustomer(currentUser, customerType, null, null);
            if (data != null) return data;
        }
        else if(filter.equalsIgnoreCase("YTD")) {
            data = getProductMixVolumeAndRatioListDataForYTD(null, baseSiteService.getCurrentBaseSite(), data, currentUser,null,null);
            if (data != null) return data;
        }
        return data;
    }

    @Override
    public NetworkCounterShareData getDealerCounterShareForNetwork(String filter, int month1, int year1,List<String> doList,List<String> subAreaList) {
        EyDmsUserModel eydmsUser=(EyDmsUserModel) getUserService().getCurrentUser();
        BaseSiteModel baseSite = getBaseSiteService().getCurrentBaseSite();
        double actualSaleMTD =0.0, actualSaleYTD=0.0, selectedMonthYearSale=0.0;
        //actualSaleMTD = getSalesPerformanceService().getActualTargetForSalesMTD(subArea, eydmsUser, baseSite);
        //actualSaleYTD = getSalesPerformanceService().getActualTargetForSalesYTD(subArea, eydmsUser, baseSite);
        actualSaleMTD = getSalesPerformanceService().getActualTargetForSalesMTD(eydmsUser, baseSite,doList,subAreaList);
        actualSaleYTD = getSalesPerformanceService().getActualTargetForSalesYTD(eydmsUser, baseSite,doList,subAreaList);
        //selectedMonthYearSale=getSalesPerformanceService().getActualTargetSalesForSelectedMonthAndYear(subArea, eydmsUser, baseSite, month1, year1);
        selectedMonthYearSale=getSalesPerformanceService().getActualTargetSalesForSelectedMonthAndYear(eydmsUser, baseSite, month1, year1,doList,subAreaList);
        List<EyDmsCustomerModel> customerFilteredList=new ArrayList<>();
        double counterPotential=0.0;
        double counterShare=0.0;
        Double counterSharePercent = null;
        customerFilteredList = territoryManagementService.getDealersForSubArea();
        if(customerFilteredList!=null && !customerFilteredList.isEmpty())
            for (EyDmsCustomerModel eydmsCustomerModel : customerFilteredList) {
                counterPotential += eydmsCustomerModel.getCounterPotential()!=null?eydmsCustomerModel.getCounterPotential():0.0;
                if(eydmsCustomerModel.getLastCounterVisit()!=null)
                    counterShare += eydmsCustomerModel.getLastCounterVisit().getCounterShare()!=null ? eydmsCustomerModel.getLastCounterVisit().getCounterShare() :0.0;
            }

        NetworkCounterShareData data = new NetworkCounterShareData();
        if(StringUtils.isBlank(filter)) {
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
                if (actualSaleMTD != 0.0 && counterPotential != 0.0) {
                    counterSharePercent = (actualSaleMTD / counterPotential) * 100;
                    data.setCounterSharePercentage(counterSharePercent);
                }
                else {
                    data.setCounterSharePercentage(0.0);
                }
            }
        }

        else if (filter.contains("MTD")) {
            data.setPotential(counterPotential);
            data.setActual(actualSaleMTD);
            if (actualSaleMTD != 0.0 && counterPotential != 0.0) {
                counterSharePercent = (actualSaleMTD / counterPotential) * 100;
                data.setCounterSharePercentage(counterSharePercent);
            }
            else {
                data.setCounterSharePercentage(0.0);
            }

        }
        else if (filter.contains("YTD"))
        {
            data.setPotential(counterPotential);
            data.setActual(actualSaleYTD);
            if (actualSaleYTD != 0.0 && counterPotential != 0.0) {
                counterSharePercent = (actualSaleYTD / counterPotential) * 100;
                data.setCounterSharePercentage(counterSharePercent);
            }
            else {
                data.setCounterSharePercentage(0.0);
            }
        }
        return data;
    }
    @Override
    public List<SalesPerformNetworkDetailsData> getRetailerDetailedSummaryListData(List<EyDmsCustomerModel> retailerList,List<String> doList,List<String> subAreaList) {
        String startDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).toString();
        String endDate = LocalDate.now().toString();

        String startDateLastMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).minusMonths(1).toString();
        String endDateLastMonth = LocalDate.now().minusMonths(1).toString();

        List<List<Object>> list = orderRequistionDao.getSalsdMTDforRetailer(retailerList, startDate, endDate,doList,subAreaList);
        List<List<Object>> listLastMonth = orderRequistionDao.getSalsdMTDforRetailer(retailerList, startDateLastMonth, endDateLastMonth,doList,subAreaList);

        Map<String, Double>  map = list.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                .collect(Collectors.toMap(each->((EyDmsCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));
        Map<String, Double>  mapLastMonth = listLastMonth.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                .collect(Collectors.toMap(each->((EyDmsCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

        LocalDate currentYearCurrentDate= LocalDate.now();
        LocalDate currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear(), Month.APRIL, 1);
        if(currentYearCurrentDate.getMonth().compareTo(Month.APRIL)<0) {
            currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear()-1, Month.APRIL, 1);
        }
        LocalDate lastYearCurrentDate= currentYearCurrentDate.minusYears(1);//2022-04-02

        LocalDate lastFinancialYearDate = currentFinancialYearDate.minusYears(1);//2022-04-01

        List<List<Object>> currentYTD = orderRequistionDao.getSalsdMTDforRetailer(retailerList, currentFinancialYearDate.toString(), currentYearCurrentDate.toString(),doList,subAreaList);
        Map<String, Double>  mapCurrentYTD = currentYTD.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                .collect(Collectors.toMap(each->((EyDmsCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

        List<List<Object>> lastYTD = orderRequistionDao.getSalsdMTDforRetailer(retailerList, lastFinancialYearDate.toString(), lastYearCurrentDate.toString(),doList,subAreaList);

        Map<String, Double>  mapLastYTD = lastYTD.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                .collect(Collectors.toMap(each->((EyDmsCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

        List<SalesPerformNetworkDetailsData> summaryDataList = new ArrayList<>();
        retailerList.forEach(retailer -> {
            SalesPerformNetworkDetailsData dealerCurrentNetworkData = new SalesPerformNetworkDetailsData();
            var subAraMappinglist = territoryManagementService.getTerritoriesForCustomer(retailer);
            dealerCurrentNetworkData.setCode(retailer.getUid());
            if(retailer.getContactNumber()!=null){
                dealerCurrentNetworkData.setContactNumber(retailer.getContactNumber());
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
                    dealerCurrentNetworkData.setDaySinceLastLifting(String.valueOf(Math.toIntExact(ChronoUnit.DAYS.between(transactionDate, today))));
                } else {
                    dealerCurrentNetworkData.setDaySinceLastLifting(String.valueOf("-"));
                }
            }
            else {
                if (retailer.getLastLiftingDate() != null) {
                    EyDmsCustomerModel retailerSalesForDealer = salesPerformanceDao.getRetailerSalesForDealer(retailer, baseSiteService.getCurrentBaseSite());
                    if (retailerSalesForDealer != null) {
                        LocalDate today = LocalDate.now();
                        LocalDate transactionDate = retailer.getLastLiftingDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        dealerCurrentNetworkData.setDaySinceLastLifting(String.valueOf(Math.toIntExact(ChronoUnit.DAYS.between(transactionDate, today))));
                    } else {
                        dealerCurrentNetworkData.setDaySinceLastLifting(String.valueOf("-"));
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
            dealerCurrentNetworkData.setGrowthRateYoYPercentage(df.format(getYearToYearGrowth(salesCurrentYearQty,salesLastYearQty)));
            if(CollectionUtils.isNotEmpty(subAraMappinglist)) {
                var subareaMaster=subAraMappinglist.get(0);
                dealerCurrentNetworkData.setDistrict(subareaMaster.getDistrict());
                dealerCurrentNetworkData.setTaluka(subareaMaster.getTaluka());
            }
            summaryDataList.add(dealerCurrentNetworkData);
        });
       /* AtomicInteger rank=new AtomicInteger(1);
        summaryDataList.stream().sorted(Comparator.comparing(nw -> nw.getSalesQuantity().getRetailerSaleQuantity())).forEach(data->data.setRank(String.valueOf(rank.getAndIncrement())));*/
        return summaryDataList;
    }
    private Double getYearToYearGrowth(double salesCurrentYearQty, double salesLastYearQty){
        if(salesLastYearQty>0) {
            return   (((salesLastYearQty - salesCurrentYearQty) / salesLastYearQty) * 100);
        }
        return 0.0;
    }
    @Override
    public SalesPerformNetworkDetailsListData getListOfAllDealerRetailerInfluencers(String fields, String leadType, String filter,List<String> doList, List<String> subAreaList) {
        BaseSiteModel site = baseSiteService.getCurrentBaseSite();
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        SalesPerformNetworkDetailsListData listData = new SalesPerformNetworkDetailsListData();
        RequestCustomerData requestCustomerData = new RequestCustomerData();
        List<SubAreaMasterModel> soList = new ArrayList<>();
        List<DistrictMasterModel> districtList = new ArrayList<>();
        if (subAreaList != null && !subAreaList.isEmpty()) {
            for (String id : subAreaList) {
                soList.add(territoryManagementDao.getTerritoryById(id));
            }
            if (soList.get(0) != null)
                requestCustomerData.setSubAreaMasterPk(soList.get(0).getPk().toString());

        }
        if (doList != null && !doList.isEmpty()) {
            for (String code : doList) {
                districtList.add(districtMasterDao.findByCode(code));
            }
            if (doList.get(0) != null)
                requestCustomerData.setDistrict(districtList.get(0).getName());
        }

        if (leadType.equalsIgnoreCase(RETAILER)) {
            List<SalesPerformNetworkDetailsData> retailerDetailedSummaryListData = new ArrayList<>();
            List<EyDmsCustomerModel> retailer = new ArrayList<>();
            if ((currentUser instanceof EyDmsUserModel) || (((EyDmsCustomerModel) currentUser).getCounterType() != null && ((EyDmsCustomerModel) currentUser).getCounterType().equals(CounterType.SP))) {
                requestCustomerData.setCounterType(List.of(RETAILER));
                retailer = territoryManagementService.getCustomerforUser(requestCustomerData);
            }
            if (StringUtils.isNotBlank(filter)) {
                retailer=salesPerformanceService.filterEyDmsCustomersWithSearchTerm(retailer,filter);
            }
            if (retailer != null && !retailer.isEmpty()) {
                retailerDetailedSummaryListData = getRetailerDetailedSummaryListData(retailer, doList, subAreaList);
                retailerDetailedSummaryListData = retailerDetailedSummaryListData.stream().sorted(Comparator.comparing(SalesPerformNetworkDetailsData::getName)).collect(Collectors.toList());
            }
            listData.setNetworkDetails(retailerDetailedSummaryListData);

        } else if (leadType.equalsIgnoreCase(DEALER)) {
            return salesPerformanceService.getListOfAllDealerRetailerInfluencers(fields, site, leadType, filter, doList, subAreaList);
        } else if (leadType.equalsIgnoreCase("INFLUENCER")) {
            List<SalesPerformNetworkDetailsData> influencerDetailedSummaryListData=new ArrayList<>();
            List<EyDmsCustomerModel> influencer = new ArrayList<>();
            if ((currentUser instanceof EyDmsUserModel) || (((EyDmsCustomerModel) currentUser).getCounterType() != null && ((EyDmsCustomerModel) currentUser).getCounterType().equals(CounterType.SP))) {
                requestCustomerData.setCounterType(List.of("Influencer"));
                influencer = territoryManagementService.getCustomerforUser(requestCustomerData);
            }
            if (StringUtils.isNotBlank(filter)) {
                influencer=salesPerformanceService.filterEyDmsCustomersWithSearchTerm(influencer,filter);
            }
            if (influencer != null && !influencer.isEmpty()){
                     influencerDetailedSummaryListData = getInfluencerDetailedSummaryListData(influencer, doList, subAreaList);
                     influencerDetailedSummaryListData=influencerDetailedSummaryListData.stream().sorted(Comparator.comparing(SalesPerformNetworkDetailsData::getName)).collect(Collectors.toList());
            }
            listData.setNetworkDetails(influencerDetailedSummaryListData);
        }
        return  listData;
    }
    @Override
    public List<SalesPerformNetworkDetailsData> getInfluencerDetailedSummaryListData(List<EyDmsCustomerModel> influencerList,List<String> doList,List<String> subAreaList) {

        List<SalesPerformNetworkDetailsData> summaryDataList = new ArrayList<>();
        String startDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).toString();
        String endDate = LocalDate.now().plusDays(1).toString();
        List<List<Object>> list = pointRequisitionDao.getBagLiftedMTDforInfluencers(influencerList, startDate, endDate,doList,subAreaList);
        Map<String, Double>  map = list.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                .collect(Collectors.toMap(each->((EyDmsCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

        LocalDate currentYearCurrentDate= LocalDate.now();
        LocalDate currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear(), Month.APRIL, 1);
        if(currentYearCurrentDate.getMonth().compareTo(Month.APRIL)<0) {
            currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear()-1, Month.APRIL, 1);
        }
        LocalDate lastYearCurrentDate= currentYearCurrentDate.minusYears(1);

        LocalDate lastFinancialYearDate = currentFinancialYearDate.minusYears(1);

        List<List<Object>> currentYTD = pointRequisitionDao.getBagLiftedMTDforInfluencers(influencerList, currentFinancialYearDate.toString(), currentYearCurrentDate.toString(),doList,subAreaList);
        Map<String, Double>  mapCurrentYTD = currentYTD.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                .collect(Collectors.toMap(each->((EyDmsCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

        List<List<Object>> lastYTD = pointRequisitionDao.getBagLiftedMTDforInfluencers(influencerList, lastFinancialYearDate.toString(), lastYearCurrentDate.toString(),doList,subAreaList);

        Map<String, Double>  mapLastYTD = lastYTD.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                .collect(Collectors.toMap(each->((EyDmsCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

        influencerList.forEach(influencer -> {
            SalesPerformNetworkDetailsData dealerCurrentNetworkData=new SalesPerformNetworkDetailsData();
            dealerCurrentNetworkData.setCode(influencer.getUid());
            dealerCurrentNetworkData.setName(influencer.getName());
            dealerCurrentNetworkData.setCategory(influencer.getInfluencerType() != null ?
                    enumerationService.getEnumerationName(influencer.getInfluencerType()) : "-");
            dealerCurrentNetworkData.setContactNumber(influencer.getContactNumber());
            dealerCurrentNetworkData.setTimesContacted(influencer.getTimesContacted());
            dealerCurrentNetworkData.setType(influencer.getNetworkType());
            dealerCurrentNetworkData.setPoints(Objects.nonNull(influencer.getAvailablePoints()) ? influencer.getAvailablePoints() : 0.0);

            var subAraMappinglist = territoryManagementService.getTerritoriesForCustomer(influencer);
            var bagLifted = 0.0;
            if(map.containsKey(influencer.getUid())) {
                bagLifted = map.get(influencer.getUid());
            }
            var salesQuantity = (bagLifted / 20);
            dealerCurrentNetworkData.setBagLifted(bagLifted);
            dealerCurrentNetworkData.setBagLiftedNo(bagLifted);
            dealerCurrentNetworkData.setBagLiftedQty(String.valueOf(salesQuantity));
            if(influencer.getLastLiftingDate()!=null) {
                LocalDate today = LocalDate.now();
                LocalDate transactionDate = influencer.getLastLiftingDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                dealerCurrentNetworkData.setDaySinceLastLifting(String.valueOf(Math.toIntExact(ChronoUnit.DAYS.between(transactionDate,today))));
            }
            if(CollectionUtils.isNotEmpty(subAraMappinglist)) {
                var subareaMaster=subAraMappinglist.get(0);
                dealerCurrentNetworkData.setDistrict(subareaMaster.getDistrict());
                dealerCurrentNetworkData.setTaluka(subareaMaster.getTaluka());
            }
            dealerCurrentNetworkData.setPotential(Objects.nonNull(influencer.getCounterPotential()) ? String.valueOf(influencer.getCounterPotential()) : "0");

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
            dealerCurrentNetworkData.setSalesYtd(df.format(salesCurrentYearQty));
            dealerCurrentNetworkData.setGrowthRateYoYPercentage(df.format(getYearToYearGrowth(salesCurrentYearQty,salesLastYearQty)));

            summaryDataList.add(dealerCurrentNetworkData);
        });
        /*AtomicInteger rank=new AtomicInteger(1);
        summaryDataList.stream().sorted(Comparator.comparing(SalesPerformNetworkDetailsData::getBagLifted).reversed()).forEach(infdata-> infdata.setRank(String.valueOf(rank.getAndIncrement())));*/
        return summaryDataList;
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
    public ProductMixVolumeAndRatioListData getProductMixVolumeAndRatioListDataMTDForCustomer(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, ProductMixVolumeAndRatioListData productMixVolumeAndRatioListData, List<String> doList,List<String> subAreaList, EyDmsCustomerModel customer) {
        double sumOfQuantity;

        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) +1 ;
        int year = cal.get(Calendar.YEAR);
        List<List<Object>> productMixRatio = salesPerformanceDao.getProductwiseSalesPercentRatioAndVolumeRatioForMonthPickerForDealer(eydmsUser, baseSite, month, year, doList, subAreaList, customer);
        List<ProductMixVolumeAndRatioData> productMixVolumeAndRatioDataList = new ArrayList<>();
        sumOfQuantity = productMixRatio.stream().filter(o->Objects.nonNull(o) && Objects.nonNull(o.get(2))).mapToDouble(o -> Objects.nonNull(o.get(2)) ? (double) o.get(2) : 0.0).sum();
        //sumOfQuantity = productMixRatio.stream().collect(Collectors.summingDouble(o -> (double) o.get(2)));

        if (productMixRatio != null && !productMixRatio.isEmpty()) {
            double finalSumOfQuantity = sumOfQuantity;
            productMixRatio.forEach(objects -> {
                ProductMixVolumeAndRatioData productMixVolumeAndRatioData = new ProductMixVolumeAndRatioData();
                if (objects != null && !objects.isEmpty()) {
                    String productCode = (String) objects.get(0);
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
            if (eydmsUser != null) {
                List<String> productList = salesPlanningService.getStateWiseProductForSummaryPage(eydmsUser.getState());
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
        return productMixVolumeAndRatioListData;
    }

    @Override
    public NetworkCounterShareData getDealerCounterShareForSP(String filter, int month1, int year1, List<String> doList, List<String> subAreaList) {
        EyDmsCustomerModel currentUser = (EyDmsCustomerModel) getUserService().getCurrentUser();
        BaseSiteModel baseSite = getBaseSiteService().getCurrentBaseSite();
        double actualSaleMTD = 0.0, actualSaleYTD = 0.0, selectedMonthYearSale = 0.0;
        double counterPotential = 0.0;
        double counterShare = 0.0;
        Double counterSharePercent = null;
        if (currentUser.getCounterType().equals(CounterType.SP)) {
            RequestCustomerData requestData = new RequestCustomerData();
            List<String> counterTypes = List.of(DEALER);
            requestData.setCounterType(counterTypes);
            List<EyDmsCustomerModel> customerforUser = territoryManagementService.getCustomerforUser(requestData);
            if (customerforUser != null && !customerforUser.isEmpty()) {
                for (EyDmsCustomerModel customer : customerforUser) {
                    LOGGER.info(String.format("EyDms customer Model PK:%s", customer));
                    counterPotential += customer.getCounterPotential() != null ? customer.getCounterPotential() : 0.0;
                    if (customer.getLastCounterVisit() != null)
                        counterShare += customer.getLastCounterVisit().getCounterShare() != null ? customer.getLastCounterVisit().getCounterShare() : 0.0;
                }
            }
            if (customerforUser != null && !customerforUser.isEmpty()) {
                actualSaleMTD = getSalesPerformanceService().getActualTargetForSalesSPMTD(customerforUser);
                actualSaleYTD = getSalesPerformanceService().getActualTargetForSalesYTDSP(customerforUser, baseSite);
                selectedMonthYearSale = getSalesPerformanceService().getActualTargetForSalesForMonthSP(customerforUser, year1, month1);
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
        } else if (filter.contains("MTD")) {
            data.setPotential(counterPotential);
            data.setActual(actualSaleMTD);
            if (actualSaleMTD != 0.0 && counterPotential != 0.0) {
                counterSharePercent = (actualSaleMTD / counterPotential) * 100;
                data.setCounterSharePercentage(counterSharePercent);
            } else {
                data.setCounterSharePercentage(0.0);
            }

        } else if (filter.contains("YTD")) {
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
        EyDmsCustomerModel eydmsUser = (EyDmsCustomerModel) getUserService().getCurrentUser();
        List<EyDmsCustomerModel> customerforUser = new ArrayList<>();
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
        list.setEmpCode(eydmsUser.getUid());
        list.setEmpName(eydmsUser.getName());
        DistrictMasterModel district1 = null;
        if(StringUtils.isNotBlank(district))
        {
            FilterDistrictData filterDistrictData=new FilterDistrictData();
            filterDistrictData.setDistrictCode(district);
            DistrictMasterModel districtMasterModel=salesPerformanceService.getDistrictForSP(filterDistrictData);
            List<EyDmsCustomerModel> spForDistrict = salesPerformanceService.getSPForDistrict(districtMasterModel);
            for (EyDmsCustomerModel eydmsCustomerModel : spForDistrict) {
                if (eydmsCustomerModel.getCounterType().equals(CounterType.SP)) {
                    RequestCustomerData requestData = new RequestCustomerData();
                    List<String> counterTypes = List.of(DEALER);
                    requestData.setCounterType(counterTypes);
                    customerforUser = salesPerformanceService.getCustomerForSp(eydmsCustomerModel);
                    LOGGER.info(String.format("Size of customerForuser %s",customerforUser.size()));
                    List<String> customerNo = new ArrayList<>();
                    for(EyDmsCustomerModel customer : customerforUser)
                    {
                        if(customer.getCustomerNo()!=null)
                            customerNo.add(customer.getCustomerNo());
                    }
                    if (customerforUser != null && !customerforUser.isEmpty()) {
                        if (filter.equals("MTD")) {
                            actualSaleMTD = getSalesPerformanceService().getActualTargetForSalesSPMTDSearch(customerforUser);
                            LOGGER.info(String.format("EyDmsCustomer :: %s sales :: %s",eydmsCustomerModel.getUid(), actualSaleMTD));
                            unsortedLeaderboard.put(eydmsCustomerModel.getUid(), actualSaleMTD);
                            target = getSalesPerformanceService().getMonthlySalesTargetForSP(customerforUser);
                            LOGGER.info(String.format("EyDmsCustomer :: %s target :: %s",eydmsCustomerModel.getUid(), target));
                            if (actualSaleMTD != 0.0 && target != 0.0) {
                                achievementPercentage = (actualSaleMTD / target) * 100;
                            }
                            LOGGER.info(String.format("EyDmsCustomer :: %s Achievement :: %s",eydmsCustomerModel.getUid(), achievementPercentage));
                            unsortedPremiumSalesLeaderboard.put(eydmsCustomerModel.getUid(), achievementPercentage);
                            double totalOutstanding =0.0;
                            if(!customerNo.isEmpty())
                            {
                                totalOutstanding =eydmsUserDao.getOutstandingAmountForSO(customerNo);
                            }

                            double dailyAverageSales = collectionDao.getDailyAverageSalesForListOfDealers(customerNo);
                            if(dailyAverageSales!=0.0)
                            {
                                outstandingDays = (int) (totalOutstanding/dailyAverageSales);
                            }
                            unsortedCashDiscountAvailedPercentage.put(eydmsCustomerModel.getUid(), outstandingDays);
                            LOGGER.info(String.format("EyDmsCustomer :: %s OutstandingDays :: %s",eydmsCustomerModel.getUid(), outstandingDays));
                        } else if (filter.equals("YTD")) {
                            actualSaleYTD = getSalesPerformanceService().getActualTargetForSalesYTDSP(customerforUser, currentBaseSite);
                            LOGGER.info(String.format("EyDmsCustomer :: %s sales :: %s",eydmsCustomerModel.getUid(), actualSaleMTD));
                            unsortedLeaderboard.put(eydmsCustomerModel.getUid(), actualSaleYTD);
                            target = getSalesPerformanceService().getAnnualSalesTargetForSP(customerforUser);
                            LOGGER.info(String.format("EyDmsCustomer :: %s sales :: %s",eydmsCustomerModel.getUid(), target));
                            if (actualSaleYTD != 0.0 && target != 0.0) {
                                achievementPercentage = (actualSaleYTD / target) * 100;
                            }
                            LOGGER.info(String.format("EyDmsCustomer :: %s Acheivement :: %s",eydmsCustomerModel.getUid(), achievementPercentage));
                            unsortedPremiumSalesLeaderboard.put(eydmsCustomerModel.getUid(), achievementPercentage);
                            double totalOutstanding =0.0;
                            if(!customerNo.isEmpty())
                            {
                                totalOutstanding =eydmsUserDao.getOutstandingAmountForSO(customerNo);
                            }

                            double dailyAverageSales = collectionDao.getDailyAverageSalesForListOfDealers(customerNo);
                            if(dailyAverageSales!=0.0)
                            {
                                outstandingDays = (int) (totalOutstanding/dailyAverageSales);
                            }
                            //outstandingDays= Integer.parseInt(networkService.getOutstandingDaysForPromoter(eydmsUser));
                            unsortedCashDiscountAvailedPercentage.put(eydmsCustomerModel.getUid(), outstandingDays);
                            LOGGER.info(String.format("EyDmsCustomer :: %s OutstandingDays :: %s",eydmsCustomerModel.getUid(), outstandingDays));
                        } else if (filter.equals("QTD")) {
                            actualSaleYTD = getSalesPerformanceService().getActualTargetForSalesYTDSP(customerforUser, currentBaseSite);
                            unsortedLeaderboard.put(eydmsCustomerModel.getUid(), actualSaleYTD);
                            target = getSalesPerformanceService().getAnnualSalesTargetForSP(customerforUser);
                            if (actualSaleYTD != 0.0 && target != 0.0){
                                achievementPercentage = (actualSaleYTD / target) * 100;
                            }
                            LOGGER.info(String.format("EyDmsCustomer :: %s Acheivement :: %s",eydmsCustomerModel.getUid(), achievementPercentage));
                            unsortedPremiumSalesLeaderboard.put(eydmsCustomerModel.getUid(), achievementPercentage);
                            double totalOutstanding =0.0;
                            if(!customerNo.isEmpty())
                            {
                                totalOutstanding =eydmsUserDao.getOutstandingAmountForSO(customerNo);
                            }

                            double dailyAverageSales = collectionDao.getDailyAverageSalesForListOfDealers(customerNo);
                            if(dailyAverageSales!=0.0)
                            {
                                outstandingDays = (int) (totalOutstanding/dailyAverageSales);
                            }
                            //outstandingDays= Integer.parseInt(networkService.getOutstandingDaysForPromoter(eydmsUser));
                            unsortedCashDiscountAvailedPercentage.put(eydmsCustomerModel.getUid(), outstandingDays);
                            LOGGER.info(String.format("EyDmsCustomer :: %s OutstandingDays :: %s",eydmsCustomerModel.getUid(), outstandingDays));
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
                EyDmsCustomerModel eydmsUserModel = (EyDmsCustomerModel) userService.getUserForUID(salesOfficer);
                int s = (int) (sales * 100);
                LOGGER.info(String.format("sale is %s", String.valueOf(s)));
                int score = calculateScoreFromPercentile(s);
                LOGGER.info(String.format("score is %s", String.valueOf(score)));
                data.setScore(score);
                data.setCode(eydmsUserModel.getUid());
                data.setName(eydmsUserModel.getName());
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
                EyDmsCustomerModel eydmsUserModel = (EyDmsCustomerModel) userService.getUserForUID(salesOfficer);
                int s1 = (int) (sales * 100);
                LOGGER.info(String.format("sale is %s", String.valueOf(s1)));
                int score = calculateScoreFromPercentile(s1);
                LOGGER.info(String.format("score is %s", String.valueOf(score)));
                data.setScore(score);
                data.setCode(eydmsUserModel.getUid());
                data.setName(eydmsUserModel.getName());
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
                EyDmsCustomerModel eydmsUserModel = (EyDmsCustomerModel) userService.getUserForUID(salesOfficer);
                int s1 = (int) (sales * 100);
                LOGGER.info(String.format("outstanding is %s", String.valueOf(s1)));
                int score = calculateScoreFromPercentile(s1);
                LOGGER.info(String.format("outstandingscore is %s", String.valueOf(score)));
                data.setScore(score);
                data.setCode(eydmsUserModel.getUid());
                data.setName(eydmsUserModel.getName());
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
                EyDmsCustomerModel eydmsUserModel = (EyDmsCustomerModel) userService.getUserForUID(salesOfficer);
                flagFinal.addAndGet(1);
                data.setRank(flagFinal.get());
                data.setScore(score.intValue());
                data.setCode(eydmsUserModel.getUid());
                data.setName(eydmsUserModel.getName());
                finalLeaderBoard.add(data);
            });

            list.setSalesList(finalLeaderBoard);

        }

     /*   if(StringUtils.isBlank(district)) {
            List<DistrictMasterModel> districtForSP = salesPerformanceService.getDistrictForSP(eydmsUser);
            if (districtForSP != null && !districtForSP.isEmpty()) {
                for (DistrictMasterModel districtMasterModel : districtForSP) {
                    list.setDistrict(districtMasterModel.getName());
                    List<EyDmsCustomerModel> spForDistrict = salesPerformanceService.getSPForDistrict(districtMasterModel);
                    for (EyDmsCustomerModel eydmsCustomerModel : spForDistrict) {
                        if (eydmsCustomerModel.getCounterType().equals(CounterType.SP)) {
                            RequestCustomerData requestData = new RequestCustomerData();
                            List<String> counterTypes = List.of(DEALER);
                            requestData.setCounterType(counterTypes);
                            customerforUser = salesPerformanceService.getCustomerForSp(eydmsCustomerModel);
                            if (customerforUser != null && !customerforUser.isEmpty()) {
                                if (filter.equals("MTD")) {
                                    actualSaleMTD = getSalesPerformanceService().getActualTargetForSalesSPMTD(customerforUser);
                                    unsortedLeaderboard.put(eydmsCustomerModel.getUid(), actualSaleMTD);
                                    target = getSalesPerformanceService().getMonthlySalesTargetForSP(customerforUser);
                                    if (actualSaleMTD != 0.0 && target != 0.0) {
                                        achievementPercentage = (actualSaleMTD / target) * 100;
                                    }
                                    unsortedPremiumSalesLeaderboard.put(eydmsCustomerModel.getUid(), achievementPercentage);
                                } else if (filter.equals("YTD")) {
                                    actualSaleYTD = getSalesPerformanceService().getActualTargetForSalesYTDSP(customerforUser, currentBaseSite);
                                    unsortedLeaderboard.put(eydmsCustomerModel.getUid(), actualSaleYTD);
                                    target = getSalesPerformanceService().getAnnualSalesTargetForSP(customerforUser);
                                    if (actualSaleYTD != 0.0 && target != 0.0) {
                                        achievementPercentage = (actualSaleYTD / target) * 100;
                                    }
                                    unsortedPremiumSalesLeaderboard.put(eydmsCustomerModel.getUid(), achievementPercentage);
                                } else if (filter.equals("QTD")) {
                                    actualSaleYTD = getSalesPerformanceService().getActualTargetForSalesYTDSP(customerforUser, currentBaseSite);
                                    unsortedLeaderboard.put(eydmsCustomerModel.getUid(), actualSaleYTD);
                                    target = getSalesPerformanceService().getAnnualSalesTargetForSP(customerforUser);
                                    if (actualSaleYTD != 0.0 && target != 0.0)
                                        achievementPercentage = (actualSaleYTD / target) * 100;
                                    unsortedPremiumSalesLeaderboard.put(eydmsCustomerModel.getUid(), achievementPercentage);
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
                        EyDmsCustomerModel eydmsUserModel = (EyDmsCustomerModel) userService.getUserForUID(salesOfficer);
                        int s = (int) (sales * 100);
                        LOGGER.info(String.format("sale is %s", String.valueOf(s)));
                        int score = calculateScoreFromPercentile(s);
                        LOGGER.info(String.format("score is %s", String.valueOf(score)));
                        data.setScore(score);
                        data.setCode(eydmsUserModel.getUid());
                        data.setName(eydmsUserModel.getName());
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
                        EyDmsCustomerModel eydmsUserModel = (EyDmsCustomerModel) userService.getUserForUID(salesOfficer);
                        int s1 = (int) (sales * 100);
                        LOGGER.info(String.format("sale is %s", String.valueOf(s1)));
                        int score = calculateScoreFromPercentile(s1);
                        LOGGER.info(String.format("score is %s", String.valueOf(score)));
                        data.setScore(score);
                        data.setCode(eydmsUserModel.getUid());
                        data.setName(eydmsUserModel.getName());
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
                        EyDmsCustomerModel eydmsUserModel = (EyDmsCustomerModel) userService.getUserForUID(salesOfficer);
                        flagFinal.addAndGet(1);
                        data.setRank(flagFinal.get());
                        data.setScore(score.intValue());
                        data.setCode(eydmsUserModel.getUid());
                        data.setName(eydmsUserModel.getName());
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
        EyDmsUserModel eydmsUser = (EyDmsUserModel) getUserService().getCurrentUser();
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        TsmLeaderboardListData list = new TsmLeaderboardListData();
        Date startDate;
        Date endDate;
        Date currentDate = Calendar.getInstance().getTime();
        int monthInNumber = currentDate.getMonth();
        if (eydmsUser.getUserType().getCode().equals("TSM")) {
            List<TsmLeaderboardData> leaderboardDataList = new ArrayList<>();
            list.setEmpCode(eydmsUser.getUid());
            list.setEmpName(eydmsUser.getName());
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

            List<EyDmsUserModel> allSalesOfficersByRegion = salesPerformanceService.getTsmByRegion(district);
            Map<String, Double> unsortedLeaderboard = new HashMap<>();


            if (filter.equals("MTD")) {
                for (EyDmsUserModel eydmsUserModel : allSalesOfficersByRegion) {


                    startDate = getFirstDateOfMonth(monthInNumber);
                    endDate = getLastDateOfMonth(monthInNumber);
                    List<List<Object>> salesDealerByDate = salesPerformanceService.getSalesDealerByDate(district, eydmsUserModel, startDate, endDate);
                    for (List<Object> entry : salesDealerByDate) {
                        if (entry.size() >= 2) {
                            String key = String.valueOf(entry.get(0));
                            Double value = (Double) entry.get(1);
                            unsortedLeaderboard.put(key, value);
                        }
                    }
                }

            } else if (filter.equals("YTD")) {
                for (EyDmsUserModel eydmsUserModel : allSalesOfficersByRegion) {
                    List<Date> dates = salesPerformanceService.getCurrentFinancialYear();
                    startDate = dates.get(0);
                    endDate = dates.get(1);

                    List<List<Object>> salesDealerByDate = salesPerformanceService.getSalesDealerByDate(district, eydmsUserModel, startDate, endDate);
                    for (List<Object> entry : salesDealerByDate) {
                        if (entry.size() >= 2) {
                            String key = String.valueOf(entry.get(0));
                            Double value = (Double) entry.get(1);
                            unsortedLeaderboard.put(key, value);
                        }
                    }
                }

            } else if (filter.equals("QTD")) {
                for (EyDmsUserModel eydmsUserModel : allSalesOfficersByRegion) {
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

                    List<List<Object>> salesDealerByDate = salesPerformanceService.getSalesDealerByDate(district, eydmsUserModel, startDate, endDate);
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
                EyDmsUserModel eydmsUserModel = (EyDmsUserModel) userService.getUserForUID(salesOfficer);
                flagFinal.addAndGet(1);
                data.setRank(flagFinal.get());
                data.setCode(eydmsUserModel.getUid());
                data.setName(eydmsUserModel.getName());
                data.setSale(score != null ? score : 0);
                List<EyDmsCustomerModel> customerforUser = salesPerformanceService.getCustomerForTsm(finalDistrict, eydmsUserModel);
                data.setTotalNetwork(customerforUser.size());
                data.setContactNo(eydmsUserModel.getMobileNumber());
                finalLeaderBoard.add(data);
            });
            list.setSalesList(finalLeaderBoard);
        }

        else if (eydmsUser.getUserType().getCode().equals("RH"))
        {
            List<TsmLeaderboardData> leaderboardDataList = new ArrayList<>();
            list.setEmpCode(eydmsUser.getUid());
            list.setEmpName(eydmsUser.getName());
            String district = null;
            List<String> state = territoryManagementService.getAllStatesForSO();
            list.setRegion(state.get(0));
            district=state.get(0);
            LOG.info(String.format("District :",district));
            List<EyDmsUserModel> allSalesOfficersByRegion = salesPerformanceService.getRHByState(district);
            LOG.info(String.format("eydmsUserModel size of %s:",allSalesOfficersByRegion.size()));
            Map<String, Double> unsortedLeaderboard = new HashMap<>();
            if (filter.equals("MTD")) {
                for (EyDmsUserModel eydmsUserModel : allSalesOfficersByRegion) {
                    LOG.info(String.format("eydmsUserModel %s:",eydmsUserModel));
                    startDate = getFirstDateOfMonth(monthInNumber);
                    endDate = getLastDateOfMonth(monthInNumber);
                    List<List<Object>> salesDealerByDate = salesPerformanceService.getRHSalesDealerByDate(eydmsUserModel, startDate, endDate);
                    for (List<Object> entry : salesDealerByDate) {
                        if (entry.size() >= 2) {
                            String key = String.valueOf(entry.get(0));
                            Double value = (Double) entry.get(1);
                            unsortedLeaderboard.put(key, value);
                            LOG.info((String.format("Key  :: %s Value ::%s ",key,value)));
                        }
                    }
                }

            } else if (filter.equals("YTD")) {
                for (EyDmsUserModel eydmsUserModel : allSalesOfficersByRegion) {
                    List<Date> dates = salesPerformanceService.getCurrentFinancialYear();
                    startDate = dates.get(0);
                    endDate = dates.get(1);

                    List<List<Object>> salesDealerByDate = salesPerformanceService.getRHSalesDealerByDate(eydmsUserModel, startDate, endDate);
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
                for (EyDmsUserModel eydmsUserModel : allSalesOfficersByRegion) {
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

                    List<List<Object>> salesDealerByDate = salesPerformanceService.getRHSalesDealerByDate(eydmsUserModel, startDate, endDate);
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
                EyDmsUserModel eydmsUserModel = (EyDmsUserModel) userService.getUserForUID(salesOfficer);
                flagFinal.addAndGet(1);
                data.setRank(flagFinal.get());
                data.setCode(eydmsUserModel.getUid());
                data.setName(eydmsUserModel.getName());
                data.setSale(score);
                List<EyDmsCustomerModel> customerforUser = salesPerformanceService.getCustomerForRH(finalDistrict, eydmsUserModel);
                data.setTotalNetwork(customerforUser.size());
                data.setContactNo(eydmsUserModel.getMobileNumber());
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
}