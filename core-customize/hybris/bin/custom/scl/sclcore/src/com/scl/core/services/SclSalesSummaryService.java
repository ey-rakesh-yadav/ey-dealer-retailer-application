package com.scl.core.services;

import com.scl.core.model.SalesSummaryModel;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.model.product.ProductModel;

import java.util.List;
import java.util.Map;

public interface SclSalesSummaryService {

    double getCurrentMonthSales(B2BCustomerModel b2bCustomer, List<String> territoryList);

     double getCurrentMonthSales(List<B2BCustomerModel> b2bCustomer,int month,int year, List<String> territoryList);
    double getCurrentMonthSales(B2BCustomerModel b2bCustomer,String productAlias);
    double getLastMonthSales(B2BCustomerModel b2bCustomer, List<String> territoryList);
    double getLastToLastMonthSales(B2BCustomerModel b2bCustomer, List<String> territoryList);
    double getSecondLastMonthSales(B2BCustomerModel b2bCustomer);

    double getSalesByMonth(B2BCustomerModel b2bCustomer, int month, int year, List<String> territoryList);
    double getSalesByMonth(B2BCustomerModel b2bCustomer, int month, int year,String productAlias);

    double getLastYearCurrentMonthSales(B2BCustomerModel b2bCustomer);

    double getLastYearCurrentMonthSales(B2BCustomerModel b2bCustomer,List<String> territoryList);

    double getCurrentFySales(B2BCustomerModel b2BCustomer, List<String> territoryList);
    double getCurrentFySales(List<B2BCustomerModel> b2BCustomer, List<String> territoryList);
    public double getCurrentFySalesForSelectedYear(List<B2BCustomerModel> b2BCustomer,int startYear,int endYear);
    double getCurrentFySales(B2BCustomerModel b2BCustomer,String productAlias);
    double getLastFYSales(B2BCustomerModel b2BCustomerModel, List<String> territoryList);
    List<List<Object>> getProductMixSalesDetailsMTD(B2BCustomerModel b2bCustomer,List<String> territoryList);
    List<List<Object>> getProductMixSalesDetailsByMonth(B2BCustomerModel b2bCustomer, int month, int year,List<String> territoryList);
    List<List<Object>> getProductMixSalesDetailsYTD(B2BCustomerModel b2BCustomer,List<String> territoryList);
    List<SalesSummaryModel> get10DayBucketSales(B2BCustomerModel b2bcustomer, int month, int year);
    double getSalesByMonthAndYear(B2BCustomerModel b2BCustomer,int startMonth, int startYear, int endMonth, int endYear, String productAlias);

    double getSalesForPartner(String customerNo, String filter,int month, int year,List<String> territoryList);
    ProductModel getProductAliasNameByCode(String bgpFilter);
    Map<String,Integer> currentFySalesDates(int startingYear, int endingYear);
    Map<String,Integer> currentFySalesDates();
}
