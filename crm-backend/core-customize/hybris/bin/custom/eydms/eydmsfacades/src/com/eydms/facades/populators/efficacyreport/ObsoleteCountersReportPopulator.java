package com.eydms.facades.populators.efficacyreport;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.model.ReinclusionObsoleteCounterReportModel;
import com.eydms.facades.data.ObsoleteCounterReportData;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class ObsoleteCountersReportPopulator implements Populator<ReinclusionObsoleteCounterReportModel,ObsoleteCounterReportData>{

	@Override
	public void populate(ReinclusionObsoleteCounterReportModel source, ObsoleteCounterReportData target)
			throws ConversionException {
		
		target.setCounterName(source.getCounterName());
		target.setCounterCode(source.getCounterCode());
		target.setCustomerNo(source.getCustomerNo());
		target.setDaysSinceLastLifting(source.getDaysSinceLastLifting());
		target.setOrderCaptured(source.getOrderCaptured());
		target.setOrderBooked(source.getOrderBooked());
		
		if(source.getVisitDate()!=null)
		{
		DateFormat dateFormat = new SimpleDateFormat(EyDmsCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
		String visitDate = dateFormat.format(source.getVisitDate());
		target.setVisitDate(visitDate);
		}
		
	}

}
