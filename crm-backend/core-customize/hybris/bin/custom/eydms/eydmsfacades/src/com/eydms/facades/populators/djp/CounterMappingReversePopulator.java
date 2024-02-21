package com.eydms.facades.populators.djp;

import javax.annotation.Resource;

import com.eydms.core.enums.InfluencerType;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.facades.djp.data.CounterMappingData;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.lang.StringUtils;

import java.util.Date;

public class CounterMappingReversePopulator implements Populator<CounterMappingData,EyDmsCustomerModel> {

	@Resource
	private UserService userService;

	@Resource
	private KeyGenerator customCodeGenerator;

	@Resource
	private KeyGenerator applicationNumberGenerator;
	
	@Override
	public void populate(CounterMappingData source, EyDmsCustomerModel target) throws ConversionException {
		target.setUid(String.valueOf(customCodeGenerator.generate()));
		target.setMobileNumber(source.getContactNumber());
		target.setName(source.getCustomerName());
		target.setCounterPotential(source.getTotalSale());
		target.setWholeSale(source.getWholeSale());
		target.setRetailSale(source.getCounterSale());
		target.setEmail(source.getEmail());
		if(StringUtils.isNotBlank(source.getInfluencerType())) {
			target.setInfluencerType(InfluencerType.valueOf(source.getInfluencerType()));
		}
		target.setApplicationDate(new Date());
		target.setApplicationNo(applicationNumberGenerator.generate().toString());
	}

}
