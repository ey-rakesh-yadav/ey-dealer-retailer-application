package com.eydms.facades.populators.marketreport;


import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.model.MarketIntelligenceReportModel;
import com.eydms.facades.data.MarketIntelligenceData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class MarketIntelligenceReportPopulator implements Populator<MarketIntelligenceReportModel, MarketIntelligenceData> {

    @Override
    public void populate(MarketIntelligenceReportModel source, MarketIntelligenceData target) throws ConversionException {

        target.setBrandCode(source.getBrandCode());
        target.setBrandName(source.getBrandName());
        target.setProductCode(source.getProductCode());
        target.setProductName(source.getProductName());
        target.setWsp(source.getWsp());
        target.setRsp(source.getRsp());
        target.setDiscount(source.getDiscount());
        target.setBillingPrice(source.getBillingPrice());
        if(source.getVisitDate()!=null) {
        	DateFormat dateFormat = new SimpleDateFormat(EyDmsCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
        	String visitDate = dateFormat.format(source.getVisitDate());
            target.setVisitDate(visitDate);
        }

    }
}