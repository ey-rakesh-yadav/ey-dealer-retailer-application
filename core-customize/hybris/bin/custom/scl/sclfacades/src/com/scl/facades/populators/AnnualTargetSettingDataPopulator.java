package com.scl.facades.populators;

import com.scl.core.model.RetailerSalesSummaryModel;
import com.scl.facades.data.AnnualSalesTargetSettingData;
import com.scl.facades.data.RetailerDetailsData;
import com.scl.facades.data.SelfCounterSaleData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AnnualTargetSettingDataPopulator implements Populator<RetailerSalesSummaryModel, AnnualSalesTargetSettingData> {

    @Autowired
    Populator<RetailerSalesSummaryModel, RetailerDetailsData>  retailerDetailsDataPopulator;

    @Autowired
    Populator<RetailerSalesSummaryModel, SelfCounterSaleData>  selfCounterSaleDataPopulator;
    @Override
    public void populate(RetailerSalesSummaryModel source, AnnualSalesTargetSettingData target) throws ConversionException {
        Assert.notNull(source, "Parameter source cannot be null.");
        Assert.notNull(target, "Parameter target cannot be null.");
        List<RetailerDetailsData> updateRetailerDetailsDataList = new ArrayList<>();
        if (target.getRetailerData() != null && !target.getRetailerData().isEmpty()) {

            updateRetailerDetailsDataList = new ArrayList<>(target.getRetailerData());
            RetailerDetailsData retailerDetailsData = new RetailerDetailsData();
            retailerDetailsDataPopulator.populate(source, retailerDetailsData);
            updateRetailerDetailsDataList.add(retailerDetailsData);

        }
        else {
            RetailerDetailsData retailerDetailsData = new RetailerDetailsData();
            retailerDetailsDataPopulator.populate(source, retailerDetailsData);
            updateRetailerDetailsDataList.add(retailerDetailsData);
        }
        target.setCustomerCode(source.getDealerCode());
        target.setCustomerName(source.getDealerName());
        target.setCustomerPotential(source.getDealerPotential());
        target.setErpCustomerNo(source.getDealerErpCustomerNo());

        target.setRetailerData(updateRetailerDetailsDataList);

        double dealerCySale=0.0;
        for (RetailerDetailsData retailerDatum : target.getRetailerData()) {
            dealerCySale += retailerDatum.getCySales();
        }

        SelfCounterSaleData selfCounterSaleData = new SelfCounterSaleData();
        selfCounterSaleDataPopulator.populate(source,selfCounterSaleData);
        target.setSelfCounterSale(selfCounterSaleData);
    }



}
