package com.scl.facades;

public interface SclSalesSummaryFacade {

    double getCurrentMonthSales();
    double getLastMonthSales();
    double getSecondLastMonthSales();
    double getSalesByMonth(int month, int year);
    double getLastYearCurrentMonthSales();
    double getCurrentFySales();
    double getLastFYSales();

}
