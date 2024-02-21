package com.eydms.facades.populators.efficacyreport;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.model.EfficacyNewProductReportModel;
import com.eydms.facades.data.NewProductReportData;

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
		DateFormat dateFormat = new SimpleDateFormat(EyDmsCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
		String visitDate = dateFormat.format(source.getDateOfVisit());
		target.setVisitDate(visitDate);
		}
	}

}
