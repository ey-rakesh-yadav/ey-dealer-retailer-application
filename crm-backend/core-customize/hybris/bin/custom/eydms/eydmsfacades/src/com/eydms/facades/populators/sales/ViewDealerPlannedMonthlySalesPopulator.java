package com.eydms.facades.populators.sales;

import com.eydms.core.model.*;
import com.eydms.facades.data.MonthlySKUData;
import com.eydms.facades.data.MonthlySalesTargetSettingData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import java.util.*;

public class ViewDealerPlannedMonthlySalesPopulator implements Populator<AnnualSalesModel, MonthlySalesTargetSettingData> {

    public static String theMonth(int month){
        String[] monthNames = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        return monthNames[month-1];
    }
    @Override
    public void populate(AnnualSalesModel annualSalesModel, MonthlySalesTargetSettingData monthlySalesTargetSettingData) throws ConversionException {
        double bucketDivide = 0.0;
        Date currentDate=Calendar.getInstance().getTime();
        int month=currentDate.getMonth();
        String currentMonthName=theMonth(month);
        if (annualSalesModel.getDealerRevisedAnnualSales() != null && !annualSalesModel.getDealerRevisedAnnualSales().isEmpty()) {
            for (DealerRevisedAnnualSalesModel dealerRevisedAnnualSalesModel : annualSalesModel.getDealerRevisedAnnualSales()) {
                monthlySalesTargetSettingData.setCustomerCode(dealerRevisedAnnualSalesModel.getCustomerCode());
                monthlySalesTargetSettingData.setCustomerName(dealerRevisedAnnualSalesModel.getCustomerName());
                monthlySalesTargetSettingData.setCustomerPotential(dealerRevisedAnnualSalesModel.getCustomerPotential());
                List<MonthlySKUData> monthlySKUDataList=new ArrayList<>();
                for (ProductModel listOfSkus : dealerRevisedAnnualSalesModel.getListOfSkus()) {
                    MonthlySKUData monthlySKUData=new MonthlySKUData();
                    monthlySKUData.setProductCode(listOfSkus.getCode());
                    monthlySKUData.setProductName(listOfSkus.getName());
                    Collection<MonthWiseAnnualTargetModel> monthWiseAnnualTarget = listOfSkus.getMonthWiseAnnualTarget();
                    for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : monthWiseAnnualTarget) {
                        if(monthWiseAnnualTargetModel.getProductCode().equals(monthlySKUData.getProductCode())) {
                            if (monthWiseAnnualTargetModel.getMonthYear().equals(currentMonthName)) {
                                monthlySKUData.setPlannedTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                monthlySKUData.setRevisedTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                if (monthWiseAnnualTargetModel.getMonthTarget() != 0.0) {
                                    bucketDivide = monthWiseAnnualTargetModel.getMonthTarget() / 3;
                                    monthlySKUData.setBucket1(bucketDivide);
                                    monthlySKUData.setBucket2(bucketDivide);
                                    monthlySKUData.setBucket3(bucketDivide);
                                } else {
                                    monthlySKUData.setBucket1(0.0);
                                    monthlySKUData.setBucket2(0.0);
                                    monthlySKUData.setBucket3(0.0);
                                }
                                monthlySKUDataList.add(monthlySKUData);
                            }
                        }
                    }
                    monthlySalesTargetSettingData.setMonthlySkuDataList(monthlySKUDataList);
                }

                for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : dealerRevisedAnnualSalesModel.getMonthWiseAnnualTarget()) {
                    if(monthWiseAnnualTargetModel!=null){
                        if (monthWiseAnnualTargetModel.getCustomerCode().equals(monthlySalesTargetSettingData.getCustomerCode())) {
                            if (monthWiseAnnualTargetModel.getMonthYear().equals(currentMonthName)) {
                                monthlySalesTargetSettingData.setPlannedTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                monthlySalesTargetSettingData.setRevisedTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                monthlySalesTargetSettingData.setMonthName(monthWiseAnnualTargetModel.getMonthYear());
                                monthlySalesTargetSettingData.setMonthYear(String.valueOf(currentDate.getYear()));
                                if(monthWiseAnnualTargetModel.getMonthTarget()!=0.0) {
                                    bucketDivide = monthWiseAnnualTargetModel.getMonthTarget() / 3;
                                    monthlySalesTargetSettingData.setBucket1(bucketDivide);
                                    monthlySalesTargetSettingData.setBucket2(bucketDivide);
                                    monthlySalesTargetSettingData.setBucket3(bucketDivide);
                                }
                                else {
                                    bucketDivide = 0.0;
                                    monthlySalesTargetSettingData.setBucket1(bucketDivide);
                                    monthlySalesTargetSettingData.setBucket2(bucketDivide);
                                    monthlySalesTargetSettingData.setBucket3(bucketDivide);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}