package com.eydms.core.services.impl;

import com.eydms.core.dao.RetailerSalesSummaryDao;
import com.eydms.core.model.RetailerSalesSummaryModel;
import com.eydms.core.services.RetailerSalesSummaryService;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RetailerSalesSummaryServiceImpl implements RetailerSalesSummaryService {

    @Resource
    RetailerSalesSummaryDao retailerSalesSummaryDao;

    @Override
    public SearchPageData<RetailerSalesSummaryModel> getCurrentProratedFyDetails(String subArea, SearchPageData searchPageData, String filter) {
        Date startDate = null, endDate =null;
        List<Date> date = getCurrentFY();
        startDate = date.get(0);
        endDate = date.get(1);
        SearchPageData<RetailerSalesSummaryModel> retailerSalesBySubareaAndMonthAndYear = getRetailerSalesSummaryDao().findRetailerSalesBySubareaAndMonthAndYear(subArea, startDate, endDate,searchPageData, filter);
        return retailerSalesBySubareaAndMonthAndYear;
    }

    @Override
    public List<RetailerSalesSummaryModel> getCurrentFYDetails(String subArea) {
        return null;
    }

    @Override
    public List<RetailerSalesSummaryModel> getLastYearFYDetails(String subArea) {
        return null;
    }

    @Override
    public List<RetailerSalesSummaryModel> getLastYearSameMonthDetails(String subArea) {
        return null;
    }

    private List<Date> getCurrentFY() {
        List<Date> dates=new ArrayList<>();
        Date startDate,endDate;
        Year starYear,endYear;
        LocalDate currentDate = LocalDate.now();
        Month currentMonth = currentDate.getMonth();
        Year currentYear = Year.of(currentDate.getYear());
        Calendar c1 = Calendar.getInstance();
        if(currentMonth.getValue() <= 3  ){
            starYear= Year.of(currentYear.getValue()-1);
            c1.set(starYear.getValue(),02,01);
            startDate = c1.getTime();
            endYear= Year.of(currentYear.getValue());
            c1.set(endYear.getValue(),01,28);
            endDate = c1.getTime();
            dates.add(startDate);
            dates.add(endDate);
        }
        else {
            //starYear=currentYear;
            starYear= Year.of(currentYear.getValue()-1);
            c1.set(starYear.getValue(),02,01);
            startDate = c1.getTime();
            //endYear= Year.of(currentYear.getValue()+1);
            endYear= Year.of(currentYear.getValue());
            c1.set(endYear.getValue(),01,28);
            endDate = c1.getTime();
            dates.add(startDate);
            dates.add(endDate);
        }
        return dates;
    }

    public RetailerSalesSummaryDao getRetailerSalesSummaryDao() {
        return retailerSalesSummaryDao;
    }

    public void setRetailerSalesSummaryDao(RetailerSalesSummaryDao retailerSalesSummaryDao) {
        this.retailerSalesSummaryDao = retailerSalesSummaryDao;
    }
}
