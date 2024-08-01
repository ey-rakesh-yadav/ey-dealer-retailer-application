package com.scl.facades.impl;

import com.scl.core.services.SclSalesSummaryService;
import com.scl.facades.SclSalesSummaryFacade;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.servicelayer.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Collections;

public class SclSalesSummaryFacadeImpl implements SclSalesSummaryFacade {

    @Autowired
    SclSalesSummaryService sclSalesSummaryService;

    UserService userService;

    @Override
    public double getCurrentMonthSales() {
        B2BCustomerModel b2bCustomer = (B2BCustomerModel) getUserService().getCurrentUser();
        return sclSalesSummaryService.getCurrentMonthSales(b2bCustomer, Collections.emptyList());
    }

    @Override
    public double getLastMonthSales() {
        B2BCustomerModel b2bCustomer = (B2BCustomerModel) getUserService().getCurrentUser();
        return sclSalesSummaryService.getLastMonthSales(b2bCustomer, null);
    }

    @Override
    public double getSecondLastMonthSales() {
        B2BCustomerModel b2bCustomer = (B2BCustomerModel) getUserService().getCurrentUser();
        return sclSalesSummaryService.getSecondLastMonthSales(b2bCustomer);
    }

    @Override
    public double getSalesByMonth(int month, int year) {
        B2BCustomerModel b2bCustomer = (B2BCustomerModel) getUserService().getCurrentUser();
        return sclSalesSummaryService.getSalesByMonth(b2bCustomer, month, year, Collections.emptyList());
    }

    @Override
    public double getLastYearCurrentMonthSales() {
        B2BCustomerModel b2bCustomer = (B2BCustomerModel) getUserService().getCurrentUser();
        return sclSalesSummaryService.getLastYearCurrentMonthSales(b2bCustomer);
    }

    @Override
    public double getCurrentFySales() {
        B2BCustomerModel b2bCustomer = (B2BCustomerModel) getUserService().getCurrentUser();
        return sclSalesSummaryService.getCurrentFySales(b2bCustomer, Collections.emptyList());
    }

    @Override
    public double getLastFYSales() {
        B2BCustomerModel b2bCustomer = (B2BCustomerModel) getUserService().getCurrentUser();
        return sclSalesSummaryService.getLastFYSales(b2bCustomer, null);
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

}
