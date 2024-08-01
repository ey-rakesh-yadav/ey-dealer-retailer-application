package com.scl.facades.populators.efficacyreport;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.model.EfficacyNewProductReportModel;
import com.scl.facades.data.NewProductReportData;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class EfficacyNewProductReportPopulator implements Populator<EfficacyNewProductReportModel,NewProductReportData>{

	@Override
	public void populate(EfficacyNewProductReportModel source, NewProductReportData target) throws ConversionException {
		target.setCounterName(source.getCounterName());
		target.setCounterCode(source.getCounterCode());
		target.setCustomerNo(source.getCustomerNo());
		target.setNewProducOrSKU(source.getNewProducOrSKU());
		target.setOrderCaptured(source.getOrderCaptured());
		target.setOrderBooked(source.getOrderBooked());
		
		if(source.getDateOfVisit()!=null)
		{
		DateFormat dateFormat = new SimpleDateFormat(SclCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
		String visitDate = dateFormat.format(source.getDateOfVisit());
		target.setVisitDate(visitDate);
		}
	}

}
