package com.scl.core.dao;

import com.scl.core.model.SalesSummaryModel;
import com.scl.core.model.SclCustomerModel;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.model.product.ProductModel;

import java.util.List;

public interface SclSalesSummaryDao {
    double getSalesDetails(B2BCustomerModel b2BCustomer, int month, int year, List<String> territoryList);
    double getSalesDetails(B2BCustomerModel b2BCustomer, int startMonth, int startYear, int endMonth, int endYear,List<String> territoryList);
    double getSalesDetails(List<B2BCustomerModel> b2BCustomer, int startMonth, int startYear, int endMonth, int endYear,List<String> territoryList);
    double getSalesDetails(List<B2BCustomerModel> b2BCustomer, int month, int year);

    List<List<Object>> getSalesYTDforDealer(List<SclCustomerModel> b2BCustomerModels,int startMonth, int startYear, int endMonth, int endYear);
    List<List<Object>>  getSalesMTDforDealer(List<SclCustomerModel> b2BCustomerModels, int month, int year);
    double getSalesDetails(B2BCustomerModel b2BCustomer, int startMonth, int startYear, int endMonth, int endYear,String productAlias);
    double getSalesDetails(int month, int year);
    double getSalesDetails(B2BCustomerModel b2BCustomer,int month, int year, String productAlias);
    List<List<Object>> getProductMixSalesDetailsMTD(B2BCustomerModel b2bCustomer, int month, int year,List<String> territoryList);
    List<List<Object>> getProductMixSalesDetailsYTD(B2BCustomerModel b2BCustomer, int startMonth, int startYear, int endMonth, int endYear,List<String> territoryList);
    List<SalesSummaryModel> getSalesDetailsFor10DayBucket(B2BCustomerModel b2bCustomer, int month, int year);
    double getSalesDetails(String customerNo, int month, int year, int startMonth, int startYear, int endMonth, int endYear);
    ProductModel getProductAliasNameByCode(String bgpFilter);
}
