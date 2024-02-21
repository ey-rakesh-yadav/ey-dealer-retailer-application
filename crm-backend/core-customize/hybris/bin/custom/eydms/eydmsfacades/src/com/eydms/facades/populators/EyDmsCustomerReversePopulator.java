package com.eydms.facades.populators;

import org.springframework.util.Assert;

import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commerceservices.strategies.CustomerNameStrategy;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class EyDmsCustomerReversePopulator implements Populator<CustomerData, CustomerModel>
{
	private CustomerNameStrategy customerNameStrategy;
	
	@Override
	public void populate(CustomerData source, CustomerModel target) throws ConversionException {
		Assert.notNull(source, "Parameter source cannot be null.");
		Assert.notNull(target, "Parameter target cannot be null.");
		
		target.setName(getCustomerNameStrategy().getName(source.getFirstName(), source.getLastName()));
	}

	public CustomerNameStrategy getCustomerNameStrategy() {
		return customerNameStrategy;
	}

	public void setCustomerNameStrategy(CustomerNameStrategy customerNameStrategy) {
		this.customerNameStrategy = customerNameStrategy;
	}
	
}