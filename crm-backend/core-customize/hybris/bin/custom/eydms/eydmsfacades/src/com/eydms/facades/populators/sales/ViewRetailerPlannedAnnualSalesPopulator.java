package com.eydms.facades.populators.sales;

import com.eydms.core.model.*;
import com.eydms.facades.data.AnnualSalesMonthWiseTargetData;
import com.eydms.facades.data.MonthWiseTargetData;
import com.eydms.facades.data.RetailerDetailsData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ViewRetailerPlannedAnnualSalesPopulator implements Populator<AnnualSalesModel, AnnualSalesMonthWiseTargetData> {
    @Override
    public void populate(AnnualSalesModel annualSalesMonthwiseModel, AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData) throws ConversionException {

        if(annualSalesMonthwiseModel.getRetailerPlannedAnnualSales()!=null && !annualSalesMonthwiseModel.getRetailerPlannedAnnualSales().isEmpty()) {
            for (RetailerPlannedAnnualSalesModel retailerPlannedAnnualSalesModel : annualSalesMonthwiseModel.getRetailerPlannedAnnualSales()) {
                annualSalesMonthWiseTargetData.setCustomerCode(retailerPlannedAnnualSalesModel.getCustomerCode());
                annualSalesMonthWiseTargetData.setCustomerName(retailerPlannedAnnualSalesModel.getCustomerName());
                annualSalesMonthWiseTargetData.setCustomerPotential(retailerPlannedAnnualSalesModel.getCustomerPotential());
                annualSalesMonthWiseTargetData.setTotalTarget(retailerPlannedAnnualSalesModel.getTotalTarget());
                List<MonthWiseTargetData> monthWiseTargetDataList = new ArrayList<>();

                if (retailerPlannedAnnualSalesModel.getMonthWiseAnnualTarget() != null &&
                        !retailerPlannedAnnualSalesModel.getMonthWiseAnnualTarget().isEmpty()) {
                    for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : retailerPlannedAnnualSalesModel.getMonthWiseAnnualTarget()) {
                        if (retailerPlannedAnnualSalesModel.getCustomerCode().equals(monthWiseAnnualTargetModel.getCustomerCode())) {
                            MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                            monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                            monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                            monthWiseTargetDataList.add(monthWiseTargetData);
                        }
                    }
                }
                annualSalesMonthWiseTargetData.setMonthWiseTarget(monthWiseTargetDataList);

                List<RetailerDetailsData> retailerDetailsDataList=new ArrayList<>();
                if(retailerPlannedAnnualSalesModel.getListOfRetailersPlanned()!=null && !retailerPlannedAnnualSalesModel.getListOfRetailersPlanned().isEmpty()) {
                    for (RetailerPlannedAnnualSalesDetailsModel retailerPlannedAnnualSalesDetailsModel : retailerPlannedAnnualSalesModel.getListOfRetailersPlanned()) {
                        RetailerDetailsData retailerDetailsData=new RetailerDetailsData();
                        retailerDetailsData.setCustomerCode(retailerPlannedAnnualSalesDetailsModel.getCustomerCode());
                        retailerDetailsData.setCustomerName(retailerPlannedAnnualSalesDetailsModel.getCustomerName());
                        retailerDetailsData.setCustomerPotential(retailerPlannedAnnualSalesDetailsModel.getCustomerPotential());
                        List<MonthWiseTargetData> monthWiseTargetDataListforRetailer = new ArrayList<>();
                        for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : retailerPlannedAnnualSalesDetailsModel.getMonthWiseAnnualTarget()) {
                            MonthWiseTargetData monthWiseTargetData=new MonthWiseTargetData();
                            monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                            monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                            monthWiseTargetDataListforRetailer.add(monthWiseTargetData);
                        }
                        retailerDetailsData.setMonthWiseSkuTarget(monthWiseTargetDataListforRetailer);
                        retailerDetailsDataList.add(retailerDetailsData);
                    }
                }
                annualSalesMonthWiseTargetData.setRetailerData(retailerDetailsDataList);
            }
        }
    }
}
