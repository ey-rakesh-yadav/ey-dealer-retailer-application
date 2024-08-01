package com.scl.facades.populators.sales;

import com.scl.core.model.AnnualSalesModel;
import com.scl.core.model.DealerPlannedAnnualSalesModel;
import com.scl.core.model.DealerRevisedAnnualSalesModel;
import com.scl.core.model.MonthWiseAnnualTargetModel;
import com.scl.facades.data.AnnualSalesMonthWiseTargetData;
import com.scl.facades.data.AnnualSalesMonthWiseTargetListData;
import com.scl.facades.data.MonthWiseTargetData;
import com.scl.facades.data.SKUData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ViewDealerPlannedAnnualSalesPopulator implements Populator<AnnualSalesModel, AnnualSalesMonthWiseTargetListData> {

    @Override
    public void populate(AnnualSalesModel annualSalesMonthwiseModel, AnnualSalesMonthWiseTargetListData annualSalesMonthWiseTargetListData) throws ConversionException {
        List<AnnualSalesMonthWiseTargetData> annualSalesMonthWiseTargetDataList=new ArrayList<>();
        if(annualSalesMonthwiseModel.getDealerPlannedAnnualSales()!=null  &&
                !annualSalesMonthwiseModel.getDealerPlannedAnnualSales().isEmpty()) {
            for (DealerPlannedAnnualSalesModel dealerPlannedAnnualSalesModel : annualSalesMonthwiseModel.getDealerPlannedAnnualSales()) {
                AnnualSalesMonthWiseTargetData annualSalesMonthWiseTargetData=new AnnualSalesMonthWiseTargetData();
                    annualSalesMonthWiseTargetData.setCustomerCode(dealerPlannedAnnualSalesModel.getCustomerCode());
                    annualSalesMonthWiseTargetData.setCustomerName(dealerPlannedAnnualSalesModel.getCustomerName());
                    annualSalesMonthWiseTargetData.setCustomerPotential(dealerPlannedAnnualSalesModel.getCustomerPotential());
                    annualSalesMonthWiseTargetData.setTotalTarget(dealerPlannedAnnualSalesModel.getTotalTarget());
                    List<MonthWiseTargetData> monthWiseTargetDataList = new ArrayList<>();

                    if (dealerPlannedAnnualSalesModel.getMonthWiseAnnualTarget() != null &&
                            !dealerPlannedAnnualSalesModel.getMonthWiseAnnualTarget().isEmpty()) {
                        for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : dealerPlannedAnnualSalesModel.getMonthWiseAnnualTarget()) {
                            if (dealerPlannedAnnualSalesModel.getCustomerCode().equals(monthWiseAnnualTargetModel.getCustomerCode())) {
                                MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                monthWiseTargetDataList.add(monthWiseTargetData);
                            }
                        }
                    }
                    annualSalesMonthWiseTargetData.setMonthWiseTarget(monthWiseTargetDataList);

                    List<SKUData> skuDataList=new ArrayList<>();
                    if(dealerPlannedAnnualSalesModel.getListOfSkus()!=null && !dealerPlannedAnnualSalesModel.getListOfSkus().isEmpty()) {
                        for (ProductModel sku : dealerPlannedAnnualSalesModel.getListOfSkus()) {
                            SKUData skuData=new SKUData();
                            skuData.setProductCode(sku.getCode());
                            skuData.setProductName(sku.getName());
                            skuData.setCySales(sku.getCySales());
                            skuData.setPlanSales(sku.getPlanSales());
                            List<MonthWiseTargetData> monthWiseTargetDataListforSKU = new ArrayList<>();
                            for (MonthWiseAnnualTargetModel monthWiseAnnualTargetModel : sku.getMonthWiseAnnualTarget()) {
                                if (monthWiseAnnualTargetModel.getProductCode() != null) {
                                    MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
                                    monthWiseTargetData.setMonthTarget(monthWiseAnnualTargetModel.getMonthTarget());
                                    monthWiseTargetData.setMonthYear(monthWiseAnnualTargetModel.getMonthYear());
                                    monthWiseTargetDataListforSKU.add(monthWiseTargetData);
                                }
                            }
                            skuData.setMonthWiseSkuTarget(monthWiseTargetDataListforSKU);
                            skuDataList.add(skuData);
                        }
                    }
                    annualSalesMonthWiseTargetData.setSkuDataList(skuDataList);
                }
            annualSalesMonthWiseTargetListData.setAnnualSalesMonthWiseTargetData(annualSalesMonthWiseTargetDataList);
            }
        }
}
