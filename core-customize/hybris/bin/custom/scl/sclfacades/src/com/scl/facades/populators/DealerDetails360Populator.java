package com.scl.facades.populators;

import com.scl.core.dao.*;
import com.scl.core.enums.CustomerCategory;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.order.SCLB2BOrderService;
import com.scl.core.order.dao.OrderValidationProcessDao;
import com.scl.core.order.dao.SclOrderCountDao;
import com.scl.core.services.AmountFormatService;
import com.scl.core.services.DJPVisitService;
import com.scl.core.services.SalesPerformanceService;
import com.scl.core.services.SclSalesSummaryService;
import com.scl.core.utility.SclDateUtility;
import com.scl.facades.data.*;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

public class DealerDetails360Populator implements Populator<SclCustomerModel, DealerDetails360Data> {

    private static final Logger LOG = Logger.getLogger(DealerDetails360Populator.class);

    @Resource
    private DJPVisitDao djpVisitDao;
    @Resource
    private SalesPerformanceDao salesPerformanceDao;
    @Autowired
    private SalesPerformanceService salesPerformanceService;
    @Resource
    private DJPVisitService djpVisitService;
    @Resource
    private BaseSiteService baseSiteService;
    @Resource
    private SclOrderCountDao sclOrderCountDao;
    @Resource
    private CollectionDao collectionDao;
    @Resource
    private AmountFormatService amountFormatService;
    @Resource
    private OrderValidationProcessDao orderValidationProcessDao;

    @Resource
    SalesPlanningDao salesPlanningDao;

    @Resource
    TerritoryManagementDao territoryManagementDao;

    @Resource
    SCLB2BOrderService b2bOrderService;

    @Resource
    SclSalesSummaryService sclSalesSummaryService;

    @Override
    public void populate(SclCustomerModel source, DealerDetails360Data target) throws ConversionException {

        DecimalFormat df = new DecimalFormat("#0.00");
        try {

            String customerNumber = source.getCustomerNo();
            target.setName(source.getName());
            if (Objects.nonNull(source.getCustomerNo())) {
                target.setCustomerNumber(source.getCustomerNo());
            }
            if (Objects.nonNull(source.getMobileNumber())) {
                target.setContactNo("+91"+source.getMobileNumber());
            }
            if (Objects.nonNull(source.getDealerCategory())) {
                target.setDealerCategory(source.getDealerCategory().getCode());
            }
            if (Objects.nonNull(source.getIsDealerFlag())) {
                target.setIsDealerFlag(source.getIsDealerFlag());
            }
            if (Objects.nonNull(source.getRemarkForFlag())) {
                target.setRemarkForFlag(source.getRemarkForFlag());
            }
            Map<String, Date> map = new HashMap<>();

           /* Map<String, Object> resultMax  = sclOrderCountDao.findMaxInvoicedDateAndQunatity(source);
            if(resultMax.get(OrderEntryModel.INVOICECREATIONDATEANDTIME)!=null) {
                target.setLastOrderDate(getFormattedDate((Date) resultMax.get(OrderEntryModel.INVOICECREATIONDATEANDTIME)));
            }
            else {
                target.setLastOrderDate("");
            }
            target.setLastOrderSize(resultMax.get(OrderEntryModel.QUANTITYINMT).toString());*/

            if(source.getLastLiftingDate()!=null) {
                SimpleDateFormat formatter= new SimpleDateFormat("dd/MM/yyyy");
                target.setLastOrderDate(formatter.format(source.getLastLiftingDate()));
            }

            if(source.getLastLiftingQuantity()!=null){
                target.setLastOrderSize(String.valueOf(source.getLastLiftingQuantity()));
            }

            if (Objects.nonNull(source.getLastVisitTime())) {
                target.setLastVisitDate(getFormattedDate(source.getLastVisitTime()));
            }
            if (Objects.nonNull(source.getNetworkType())) {
                target.setNetworkType(source.getNetworkType());
            }
            if(Objects.nonNull(source.getCounterPotential())) {
                target.setPotential(df.format(source.getCounterPotential()));
            }
            if(Objects.nonNull(source.getWholeSale())) {
                target.setWholeSale(source.getWholeSale());
            }
            if(Objects.nonNull(source.getRetailSale())) {
                target.setRetailSale(source.getRetailSale());
            }

            Calendar cal = Calendar.getInstance();
            int currentMonth = cal.get(Calendar.MONTH)+1;
            int currentYear = cal.get(Calendar.YEAR);

            cal.add(Calendar.MONTH, -1);
            int lastMonth = cal.get(Calendar.MONTH)+1;

            cal.add(Calendar.MONTH, -1);
            int lastToLastMonth = cal.get(Calendar.MONTH)+1;
            BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
            CustomerCategory category = CustomerCategory.TR;
            Calendar lastYrCal = Calendar.getInstance();
            lastYrCal.add(Calendar.YEAR,-1);
            int lastYear=lastYrCal.get(Calendar.YEAR);

            Integer djpVisits = djpVisitDao.getVisitCountMTD(source, currentMonth, currentYear);
            LOG.info("DJP Visit:"+djpVisits+" Month:"+currentMonth+" Year:"+currentYear);
            if(Objects.nonNull(djpVisits)) {
                target.setVisits(djpVisits);
            }

            String monthYear = SclDateUtility.getFormattedDate(Calendar.getInstance().getTime(), "YYYY");
            String monthName = SclDateUtility.getFormattedDate(Calendar.getInstance().getTime(), "MMM-YYYY");
            LOG.info(monthYear + " " +monthName);
            List<MonthlySalesData> dataList = new ArrayList<>();
            LOG.info("before setting dataList"+ dataList.size());
            dataList=djpVisitService.getMonthlySalesForDealer(dataList,source);
            target.setGrowth(String.valueOf(0));
            LOG.info("After setting dataList"+dataList.size());
            MonthlySalesListData salesListData=new MonthlySalesListData();

            Double monthlySalesTarget=salesPerformanceService.getMonthlySalesTargetForDealer(source,brand,null);
            if(customerNumber!=null)
            {
                double askingRate=0.0;
                double actualMonthSale= sclSalesSummaryService.getCurrentMonthSales(source, target.getTerritoryList());
                // double actualMonthSale = salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYearForDealer(source,brand,currentMonth,currentYear,null);
                //double actualMonthSale = djpVisitDao.getSalesHistoryDataForDealer(customerNumber, currentMonth, currentYear, category, brand);
                LOG.info("actualMonthSale"+actualMonthSale);
                if (monthlySalesTarget>0 && actualMonthSale>0) {
                    var percentage =(actualMonthSale/monthlySalesTarget)*100;
                    salesListData.setPercentage(percentage);
                }else{
                    salesListData.setPercentage(0.0);
                }
                if (actualMonthSale>0) {
                    salesListData.setActual(actualMonthSale);
                } else {
                    salesListData.setActual(0.0);
                }

                if(monthlySalesTarget!=0.0) {
                     askingRate = (monthlySalesTarget - actualMonthSale) / getRemainingDaysInMonth();
                    if (askingRate != 0.0) {
                        target.setAskingRate(df.format(askingRate));
                    }
                }else {
                    if(getNoOfDaysPassedInMonth()!=0) {
                         askingRate = actualMonthSale / getNoOfDaysPassedInMonth();
                        target.setAskingRate(df.format(askingRate));
                    }else{
                        target.setAskingRate(df.format(0.0));
                    }
                }
                if(askingRate>=0.0)
                    target.setAskingRate(df.format(askingRate));
                else target.setAskingRate(df.format(actualMonthSale / getNoOfDaysPassedInMonth()));

                double lastMonthSales= sclSalesSummaryService.getLastMonthSales(source, target.getTerritoryList());
                // double lastMonthSales = salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYearForDealer(source, brand, lastMonth, currentYear, null);
              //  double secondLastMonthSales= sclSalesSummaryService.getSecondLastMonthSales(source);
               // double secondLastMonthSales = salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYearForDealer(source, brand, lastToLastMonth, currentYear,null);
                if (lastMonthSales != 0.0) {
                    target.setLastMonthSale(df.format(lastMonthSales));
                }

                double currentYrLastMonthSale= sclSalesSummaryService.getLastMonthSales(source, target.getTerritoryList());
                // double currentYrLastMonthSale = salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYearForDealer(source, brand, lastMonth, currentYear,null);
                if (currentYrLastMonthSale != 0.0) {
                    target.setCurrentYearLastMonthSale(df.format(currentYrLastMonthSale));
                }

                double lastYearCurrentMonthSale= sclSalesSummaryService.getLastYearCurrentMonthSales(source,target.getTerritoryList());
              //  double lastYearCurrentMonthSale = salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYearForDealer(source, brand, currentMonth, lastYear,null);
                if (lastYearCurrentMonthSale != 0.0) {
                    target.setLastYearCurrentMonthSales(lastYearCurrentMonthSale);
                }
                if(actualMonthSale!= 0.0 && lastYearCurrentMonthSale!=0.0) {
                    double growth = ((actualMonthSale - lastYearCurrentMonthSale) / lastYearCurrentMonthSale) * 100;
                    if (growth != 0.0) {
                        target.setGrowth(df.format(growth));
                    }
                }else {
                    target.setGrowth(df.format(0.0));
                }

                List<List<Double>> bucketResultList = djpVisitDao.getOutstandingBucketsForDealer(customerNumber);
                LOG.info(String.format("Bucket result list ::%s",bucketResultList));
                if (null != bucketResultList && CollectionUtils.isNotEmpty(bucketResultList) && CollectionUtils.isNotEmpty(bucketResultList.get(0))) {
                    var bucketList = bucketResultList.get(0);
                    LOG.info(String.format("Bucket list ::%s",bucketList));
                    if (null != bucketList) {
//                        var filteredBucketList=bucketList.stream().filter(Objects::nonNull).collect(Collectors.toList());
                        var filteredBucketList=bucketList.stream().filter(Objects::nonNull).collect(Collectors.toList());
                        List<OutstandingBucketData> bucketDataList=new ArrayList<>();
                        for(double bucket:filteredBucketList){
                            OutstandingBucketData buckt=new OutstandingBucketData();
                            LOG.info(String.format("Bucket amount before formatting ::%s",bucket));
                            buckt.setAmount(bucket);

                            LOG.info(String.format("Bucket amount after formatting ::%s",buckt.getAmount()));
                            bucketDataList.add(buckt);
                        }
                        target.setBucketList(bucketDataList);
                    }
                }
                if (null!= bucketResultList && CollectionUtils.isNotEmpty(bucketResultList) && CollectionUtils.isNotEmpty(bucketResultList.get(0))) {
                    double bucketsTotal = bucketResultList.get(0).stream().filter(b -> (b != null && b != 0.0)).mapToDouble(b -> b.doubleValue()).sum();
                    if (bucketsTotal > 0.0) {
                        target.setBucketsTotal(df.format(bucketsTotal));
                    }
                }

                List<String> customerNos = new ArrayList<>();
                customerNos.add(customerNumber);

                Date lastUpdatedDate = collectionDao.getLastUpdateDateForOutstanding(customerNos);
                if(lastUpdatedDate!=null) {
                    Calendar cal1 = Calendar.getInstance();
                    cal1.setTime(lastUpdatedDate);
                    cal1.add(Calendar.HOUR,5);
                    cal1.add(Calendar.MINUTE,30);
                    target.setLastUpdateDate(cal1.getTime());
                }

                double totalOutstandingAmount = djpVisitDao.getDealerOutstandingAmount(customerNumber);

                double dailyAverageSales = collectionDao.getDailyAverageSalesForDealer(customerNumber);
                double outstandingDays = 0.0;
                if(totalOutstandingAmount != 0.0 && dailyAverageSales!=0.0)
                {
                    outstandingDays = totalOutstandingAmount/dailyAverageSales;
                    target.setOutstandingDays(outstandingDays);
                } else {
                    target.setOutstandingDays(outstandingDays);
                }
                if (totalOutstandingAmount != 0.0) {
                    //target.setOutstandingAmount(df.format(totalOutstandingAmount));
                    //data.setTotalOutstanding(amountFormatService.getFormattedValue(totalOutstanding));
                    target.setOutstandingAmount(totalOutstandingAmount);
                }
                Double securityDeposit = collectionDao.getSecurityDepositForDealer(customerNumber);
                target.setSecurityDeposit(securityDeposit!=null?securityDeposit:0.0);

                //Double creditLimitMultiplier = 2.0;
                Double totalCreditLimit = djpVisitDao.getDealerCreditLimit(customerNumber);

                double pendingOrderAmount =0, remainingQtyAndPrice=0, diQtyAndPrice=0;
                 remainingQtyAndPrice = orderValidationProcessDao.getRemainingQtyAndPrice(source.getPk().toString());
                 diQtyAndPrice = orderValidationProcessDao.getDiQtyAndPrice(source.getPk().toString());
                //double pendingOrderAmount = orderValidationProcessDao.getPendingOrderAmount(source.getPk().toString());
                pendingOrderAmount = remainingQtyAndPrice + diQtyAndPrice;

                Double utilizeCredit = totalOutstandingAmount + pendingOrderAmount;
                double creditLimit = djpVisitDao.getDealerCreditLimit(source.getPk().toString());
                if(pendingOrderAmount!=0.0 && totalOutstandingAmount!=0.0 && creditLimit !=0.0) {
                    double creditConsumed = ((totalOutstandingAmount + pendingOrderAmount) / creditLimit) * 100;
                    if (creditConsumed != 0.0) {
                        target.setCreditConsumed(creditConsumed);
                    }
                }
                else
                {
                    target.setCreditConsumed(0.0);
                }
                if((totalCreditLimit - utilizeCredit)>0)
                {
                    target.setAvailableCredit(totalCreditLimit - utilizeCredit);
                } else {
                    target.setAvailableCredit(0.0);
                }
                Integer creditBreachCount = sclOrderCountDao.findCreditBreachCountMTD(source);
                if (creditBreachCount != 0) {
                    target.setCreditBreachCount(creditBreachCount);
                } else {
                    target.setCreditBreachCount(0);
                }

                CashDiscountDetailsData cashDisDetailsData = getCashDiscountDetails(source.getUid(),source.getCustomerNo(),totalOutstandingAmount,df);
                if (cashDisDetailsData != null) {
                    target.setCashDiscountDetail(cashDisDetailsData);
                } else {
                    target.setCashDiscountDetail(new CashDiscountDetailsData());
                }
                BaseSiteModel baseSite = baseSiteService.getCurrentBaseSite();
                Integer retailerNetwork = territoryManagementDao.getRetailerCountForDealer(source, baseSite);
                if (retailerNetwork > 0) {
                    target.setRetailerNetwork(retailerNetwork);
                } else {
                    target.setRetailerNetwork(0);
                }
                Integer influencerNetwork = territoryManagementDao.getInfluencerCountForDealer(source, baseSite);
                if (influencerNetwork != 0) {
                    target.setInfluencerNetwork(influencerNetwork);
                } else {
                    target.setInfluencerNetwork(0);
                }

                double achievementPercentage=0.0, behindTarget=0.0, aheadTarget=0.0;
                if(actualMonthSale!=0.0 && monthlySalesTarget!=0.0)
                    achievementPercentage=(actualMonthSale/monthlySalesTarget)*100;
                salesListData.setAchievementPercentage(achievementPercentage!=0.0 ? achievementPercentage :0.0);

                if(achievementPercentage < 100)
                {
                    behindTarget=monthlySalesTarget-actualMonthSale;
                    salesListData.setBehindTotalTarget(behindTarget!=0.0 ? behindTarget :0.0);
                }
                else if(achievementPercentage > 100)
                {
                    aheadTarget=monthlySalesTarget-actualMonthSale;
                    salesListData.setAheadTotalTarget(aheadTarget!=0.0?aheadTarget:0.0);
                }
            }

            if (monthlySalesTarget != 0.0) {
                salesListData.setTarget(monthlySalesTarget);
            } else {
                salesListData.setTarget(0.0);
            }
            if (CollectionUtils.isNotEmpty(dataList)) {
                LOG.info(String.format("datalist is not empty ::%s",dataList));
                salesListData.setSales(dataList);
            }
            target.setSalesData(salesListData);
            LOG.info("Sales List Data:"+salesListData);
           /* if (null != salesListData) {
                target.setSalesData(salesListData);
            }*/


            target.setCurrentMonthLastYear(String.valueOf(Month.of(currentMonth)).concat("-").concat(String.valueOf(lastYear)));//Jun-2023
            target.setCurrentMonthCounterShare(salesPerformanceService.getCounterShareForDealer(source, LocalDate.now().getMonthValue(), LocalDate.now().getYear()));//
            target.setLastMonthCurrentYear(String.valueOf(Month.of(lastMonth)).concat("-").concat(String.valueOf(currentYear)));//May-2024
            LocalDate lastMonthValue = LocalDate.now().minusMonths(1);
            target.setLastMonthCounterShare(salesPerformanceService.getCounterShareForDealer(source,lastMonthValue.getMonthValue(), lastMonthValue.getYear()));//


            cal = Calendar.getInstance();
            Date curDate = cal.getTime();
            cal.add(Calendar.YEAR, -1);
            Date prevYear = cal.getTime();
        }

        catch(Exception e)
        {
            e.printStackTrace();
            LOG.error("DealerDetails360Populator: populated--> Error Message:" + e.getMessage() + " Caused : " + e.getCause() + " Error stack Trace::" +  e.getStackTrace());
        }

    }



    private CashDiscountDetailsData getCashDiscountDetails(String dealerCode, String customerNo, Double totalOutstandingAmount, DecimalFormat df) {
        CashDiscountDetailsData data = new CashDiscountDetailsData();
        Calendar cal = Calendar.getInstance();
        Date endDate = cal.getTime();

        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Date startDate = cal.getTime();

        double availedMtd = collectionDao.getTotalCDAvailedForDealer(dealerCode, startDate, endDate);
        double lostMtd = collectionDao.getTotalCDLostForDealer(dealerCode, startDate, endDate);

        double availedPercent;

        if ((availedMtd + lostMtd) != 0.0) {
            availedPercent = (availedMtd / (availedMtd + lostMtd)) * 100;
            data.setAvailedPercentage(df.format(availedPercent));
        }
        data.setAvailedMTD(df.format(availedMtd));
        data.setLostMTD(df.format(lostMtd));
        List<List<Double>> invoiceAmt = collectionDao.getTotalEligibleCDForDealer(customerNo);
        if (CollectionUtils.isNotEmpty(invoiceAmt.get(0)) &&Objects.nonNull(invoiceAmt.get(0).get(0)) &&Objects.nonNull(invoiceAmt.get(0).get(1))) {
            double totalEligibleDiscount = invoiceAmt.get(0).get(0) - invoiceAmt.get(0).get(1);
            double totalLost = collectionDao.getTotalCDLostForDealer(customerNo, null, null);
            data.setAvailableCD(df.format(totalEligibleDiscount - totalLost));
        }
        data.setPaymentForCD(df.format(totalOutstandingAmount));

        return data;
    }

    private int getRemainingDaysInMonth() {
        Calendar cal=Calendar.getInstance();
        int noOfDaysOfTheMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int pastDay = cal.get(Calendar.DAY_OF_MONTH) - 1;
        return noOfDaysOfTheMonth - pastDay;
    }
    private int getNoOfDaysPassedInMonth() {
        Calendar cal=Calendar.getInstance();
        int noOfDaysOfTheMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int pastDay = cal.get(Calendar.DAY_OF_MONTH) - 1;
        return  pastDay;
    }

   /* private double getCounterShare(String pk, int month, int year) {
        var counterShareResultList = djpVisitDao.getCounterSharesForDealerOrRetailer(pk, month, year);
        if (CollectionUtils.isNotEmpty(counterShareResultList) && CollectionUtils.isNotEmpty(counterShareResultList.get(0))) {
            var counterShareList = counterShareResultList.get(0);
            if (CollectionUtils.isNotEmpty(counterShareList) && Objects.nonNull(counterShareList.get(0)) && Objects.nonNull(counterShareList.get(1))) {
                return Double.parseDouble(String.valueOf(counterShareList.get(0))) / Integer.parseInt(String.valueOf(counterShareList.get(1)));
            }
        }
        return 0;
    }*/

    private String getFormattedDate(Date lastVisitTime) {
        if(Objects.isNull(lastVisitTime)){
            return "-";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd'th' MMMM yyyy");
        return sdf.format(lastVisitTime);
    }
}