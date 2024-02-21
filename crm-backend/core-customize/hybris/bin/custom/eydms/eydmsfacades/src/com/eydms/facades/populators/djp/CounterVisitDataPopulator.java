package com.eydms.facades.populators.djp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.enums.CounterType;
import com.eydms.core.model.CounterVisitMasterModel;
import com.eydms.facades.data.CounterVisitData;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class CounterVisitDataPopulator implements Populator<CounterVisitMasterModel,CounterVisitData>{

	@Override
	public void populate(CounterVisitMasterModel source, CounterVisitData target) throws ConversionException {
		target.setCounterName(source.getEyDmsCustomer().getName());
		String visitDate = "";
		if(source.getEyDmsCustomer().getLastVisitTime()!=null){
			DateFormat dateFormat = new SimpleDateFormat(EyDmsCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
			visitDate = dateFormat.format(source.getEyDmsCustomer().getLastVisitTime());
		}
		target.setLastVisitDate(visitDate);

		target.setId(source.getPk().toString());
		target.setCounterPotential(source.getEyDmsCustomer().getCounterPotential());
		double latitude=0,longitude=0;
		if(source.getEyDmsCustomer().getLatitude()!=null)
			latitude = source.getEyDmsCustomer().getLatitude();
		if(source.getEyDmsCustomer().getLongitude()!=null)
			longitude = source.getEyDmsCustomer().getLongitude();
		target.setLatitude(latitude);
		target.setLongitude(longitude);
		target.setCounterType(source.getCounterType()!=null?source.getCounterType().getCode():"");
		target.setCounterCode(source.getEyDmsCustomer().getUid());
		target.setIsAdoc(source.getIsAdHoc());
		target.setSequence(source.getSequence());
		target.setCustomerNo(source.getEyDmsCustomer().getCustomerNo());
	}
}
