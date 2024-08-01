package com.scl.core.services.impl;

import com.scl.core.dao.SclSalesSummaryDao;
import com.scl.core.model.SalesSummaryModel;
import com.scl.core.services.SclSalesSummaryService;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.easymock.cglib.core.Local;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SclSalesSummaryServiceImpl implements SclSalesSummaryService {
    private static final Logger LOG = Logger.getLogger(SclSalesSummaryServiceImpl.class);
    private String START_MONTH="startMonth";
    private String START_YEAR="startYear";
    private String END_MONTH="endMonth";
    private String END_YEAR="endYear";
    private String FILTER_MTD="MTD";
    private String FILTER_YTD="YTD";

    private SclSalesSummaryDao sclSalesSummaryDao;
    @Autowired
    UserService userService;

    @Override
    public double getCurrentMonthSales(B2BCustomerModel b2bCustomer, List<String> territoryList) {
        LocalDate currentMonth= LocalDate.now();
        int month = currentMonth.getMonthValue();
        int year= currentMonth.getYear();
        return getSclSalesSummaryDao().getSalesDetails(b2bCustomer, month, year,territoryList);
    }

    public double getCurrentMonthSales(List<B2BCustomerModel> b2bCustomer, int month,int year,List<String> territoryList) {
        LocalDate currentMonth= LocalDate.now();
        return getSclSalesSummaryDao().getSalesDetails(b2bCustomer, month, year);
    }

    @Override
    public double getCurrentMonthSales(B2BCustomerModel b2bCustomer, String productAlias) {
        LocalDate currentMonth= LocalDate.now();
        int month = currentMonth.getMonthValue();
        int year= currentMonth.getYear();
        return getSclSalesSummaryDao().getSalesDetails(b2bCustomer, month, year,productAlias);
    }

    @Override
    public double getLastMonthSales(B2BCustomerModel b2bCustomer, List<String> territoryList) {
        LocalDate lastMonth= LocalDate.now().minusMonths(1);
        int month = lastMonth.getMonthValue();
        int year= lastMonth.getYear();
        return getSclSalesSummaryDao().getSalesDetails(b2bCustomer, month, year,territoryList);
    }

    public double getLastToLastMonthSales(B2BCustomerModel b2bCustomer, List<String> territoryList) {
        LocalDate lastMonth= LocalDate.now().minusMonths(2);
        int month = lastMonth.getMonthValue();
        int year= lastMonth.getYear();
        return getSclSalesSummaryDao().getSalesDetails(b2bCustomer, month, year,territoryList);
    }

    @Override
    public double getSecondLastMonthSales(B2BCustomerModel b2bCustomer) {
        LocalDate secondLastMonth= LocalDate.now().minusMonths(2);
        int month = secondLastMonth.getMonthValue();
        int year= secondLastMonth.getYear();
        return getSclSalesSummaryDao().getSalesDetails(b2bCustomer, month, year, Collections.EMPTY_LIST);
    }

    @Override
    public double getSalesByMonth(B2BCustomerModel b2bCustomer, int month, int year, List<String> territoryList) {
        return getSclSalesSummaryDao().getSalesDetails(b2bCustomer, month, year,territoryList);
    }

    @Override
    public double getSalesByMonth(B2BCustomerModel b2bCustomer, int month, int year, String productAlias) {
        return getSclSalesSummaryDao().getSalesDetails(b2bCustomer, month, year,productAlias);
    }

    @Override
    public double getLastYearCurrentMonthSales(B2BCustomerModel b2bCustomer) {
        LocalDate currentMonth= LocalDate.now();
        int month = currentMonth.getMonthValue();
        int year= currentMonth.minusYears(1).getYear();
        return getSclSalesSummaryDao().getSalesDetails(b2bCustomer, month, year,Collections.EMPTY_LIST);
    }

    @Override
    public double getLastYearCurrentMonthSales(B2BCustomerModel b2bCustomer,List<String> territoryList) {
        LocalDate currentMonth= LocalDate.now();
        int month = currentMonth.getMonthValue();
        int year= currentMonth.minusYears(1).getYear();
        return getSclSalesSummaryDao().getSalesDetails(b2bCustomer, month, year,territoryList);
    }

    @Override
    public double getCurrentFySales(B2BCustomerModel b2BCustomer, List<String> territoryList) {
        Map<String, Integer> map = currentFySalesDates();
        return getSclSalesSummaryDao().getSalesDetails(b2BCustomer, map.get(START_MONTH), map.get(START_YEAR),map.get(END_MONTH),map.get(END_YEAR), territoryList);
    }

    @Override
    public double getCurrentFySales(List<B2BCustomerModel> b2BCustomer, List<String> territoryList) {
        Map<String, Integer> map = currentFySalesDates();
        return getSclSalesSummaryDao().getSalesDetails(b2BCustomer, map.get(START_MONTH), map.get(START_YEAR),map.get(END_MONTH),map.get(END_YEAR), territoryList);
    }

    @Override
    public double getCurrentFySalesForSelectedYear(List<B2BCustomerModel> b2BCustomer,int startYear,int endYear) {
        Map<String, Integer> map = currentFySalesDates(startYear,endYear);
        return getSclSalesSummaryDao().getSalesDetails(b2BCustomer, map.get(START_MONTH), map.get(START_YEAR),map.get(END_MONTH),map.get(END_YEAR),null);
    }

    @Override
    public double getCurrentFySales(B2BCustomerModel b2BCustomer, String productAlias) {
        Map<String, Integer> map = currentFySalesDates();
        return getSclSalesSummaryDao().getSalesDetails(b2BCustomer,map.get(START_MONTH), map.get(START_YEAR),map.get(END_MONTH),map.get(END_YEAR),productAlias);
    }

    @Override
    public double getSalesByMonthAndYear(B2BCustomerModel b2BCustomer, int startMonth, int startYear, int endMonth, int endYear, String productAlias) {
        return getSclSalesSummaryDao().getSalesDetails(b2BCustomer,startMonth,startYear,endMonth,endYear,productAlias);
    }

    @Override
    public double getSalesForPartner(String customerNo, String filter, int month, int year,List<String> territoryList) {
        LocalDate currentMonth=LocalDate.now();
        B2BCustomerModel sclCustomer=null;
        if(customerNo!=null){
             sclCustomer = (B2BCustomerModel) userService.getUserForUID(customerNo);
        }
        if(StringUtils.isBlank(filter))
        {
            if(year!=0 && month!=0) {
                return sclSalesSummaryDao.getSalesDetails(sclCustomer,month,year,territoryList);
            }
            else
            {
                return sclSalesSummaryDao.getSalesDetails(sclCustomer,currentMonth.getMonthValue(),currentMonth.getYear(),territoryList);
            }
        }
        else if(filter.equalsIgnoreCase(FILTER_MTD))
        {
            return sclSalesSummaryDao.getSalesDetails(sclCustomer,currentMonth.getMonthValue(),currentMonth.getYear(),territoryList);
        }
        else if(filter.equalsIgnoreCase(FILTER_YTD))
        {
            Map<String, Integer> map = currentFySalesDates();
            return sclSalesSummaryDao.getSalesDetails(customerNo,0,0,map.get(START_MONTH), map.get(START_YEAR),map.get(END_MONTH),map.get(END_YEAR));
        }
        return 0;
    }

    @Override
    public ProductModel getProductAliasNameByCode(String bgpFilter) {
        return  sclSalesSummaryDao.getProductAliasNameByCode(bgpFilter);
    }

    @Override
    public double getLastFYSales(B2BCustomerModel b2BCustomer, List<String> territoryList) {
        LocalDate financialYearStart=null;
        LocalDate currentDate = LocalDate.now();
        int lastYear = currentDate.getYear();
        if (currentDate.getMonthValue() < Month.APRIL.getValue()) {
            financialYearStart= LocalDate.of(lastYear - 2, Month.APRIL, 1);
        } else {
            financialYearStart = LocalDate.of(lastYear -1, Month.APRIL, 1);
        }
        LocalDate financialYearEnd = financialYearStart.plusYears(1);

        int startMonth = financialYearStart.getMonthValue();
        int startYear = financialYearStart.getYear();
        int endMonth = financialYearEnd.minusMonths(1).getMonthValue();
        int endYear = financialYearEnd.getYear();
        return getSclSalesSummaryDao().getSalesDetails(b2BCustomer, startMonth, startYear,endMonth, endYear,territoryList);
    }

    @Override
    public List<List<Object>> getProductMixSalesDetailsMTD(B2BCustomerModel b2bCustomer,List<String> territoryList) {
        LocalDate currentMonth= LocalDate.now();
        int month = currentMonth.getMonthValue();
        int year= currentMonth.getYear();
        return getSclSalesSummaryDao().getProductMixSalesDetailsMTD(b2bCustomer, month, year,territoryList);
    }

    @Override
    public List<List<Object>> getProductMixSalesDetailsByMonth(B2BCustomerModel b2bCustomer, int month, int year,List<String> territoryList) {
        return getSclSalesSummaryDao().getProductMixSalesDetailsMTD(b2bCustomer, month, year,territoryList);
    }

    @Override
    public List<List<Object>> getProductMixSalesDetailsYTD(B2BCustomerModel b2BCustomer,List<String> territoryList) {
        Map<String, Integer> map = currentFySalesDates();
        return getSclSalesSummaryDao().getProductMixSalesDetailsYTD(b2BCustomer, map.get(START_MONTH), map.get(START_YEAR),map.get(END_MONTH),map.get(END_YEAR),territoryList);
    }

    @Override
    public List<SalesSummaryModel> get10DayBucketSales(B2BCustomerModel b2bcustomer, int month, int year){
        return sclSalesSummaryDao.getSalesDetailsFor10DayBucket(b2bcustomer, month, year);
    }

    public SclSalesSummaryDao getSclSalesSummaryDao() {
        return sclSalesSummaryDao;
    }

    public void setSclSalesSummaryDao(SclSalesSummaryDao sclSalesSummaryDao) {
        this.sclSalesSummaryDao = sclSalesSummaryDao;
    }

    @Override
    public Map<String,Integer> currentFySalesDates() {
        Map<String,Integer> listOfDates=new HashMap<>();
        LocalDate financialYearStart = null;
        LocalDate currentDate = LocalDate.now();
        int currentYear = currentDate.getYear();
        if (currentDate.getMonthValue() < Month.APRIL.getValue()) {
            financialYearStart = LocalDate.of(currentYear - 1, Month.APRIL, 1);
        } else {
            financialYearStart = LocalDate.of(currentYear, Month.APRIL, 1);
        }
        LocalDate financialYearEnd = financialYearStart.plusYears(1);

        int startMonth = financialYearStart.getMonthValue();
        int startYear = financialYearStart.getYear();
        int endMonth = financialYearEnd.getMonthValue();
        int endYear = financialYearEnd.getYear();
        listOfDates.put("startMonth",startMonth);
        listOfDates.put("startYear",startYear);
        listOfDates.put("endMonth",endMonth);
        listOfDates.put("endYear",endYear);
        return  listOfDates;
    }
    @Override
    public Map<String,Integer> currentFySalesDates(int startingYear,int endingYear) {
        Map<String,Integer> listOfDates=new HashMap<>();

            LocalDate financialYearStart = LocalDate.of(startingYear, Month.APRIL, 1);
            LocalDate financialYearEnd = financialYearStart.plusYears(1);

        int startMonth = financialYearStart.getMonthValue();//4
        int startYear = financialYearStart.getYear();//2024
        int endMonth = financialYearEnd.getMonthValue();//4
        int endYear = financialYearEnd.getYear();//2025
        listOfDates.put("startMonth",startMonth);
        listOfDates.put("startYear",startYear);
        listOfDates.put("endMonth",endMonth);
        listOfDates.put("endYear",endYear);
        return  listOfDates;
    }
}
