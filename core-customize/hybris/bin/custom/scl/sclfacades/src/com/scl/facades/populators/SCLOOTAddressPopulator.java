/*
 * Copyright (c) 2023 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.scl.facades.populators;

import de.hybris.platform.commercefacades.user.converters.populator.AddressPopulator;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.user.AddressModel;

import org.apache.commons.lang3.StringUtils;


/**
 * Converter implementation for {@link de.hybris.platform.core.model.user.AddressModel} as source and
 * {@link de.hybris.platform.commercefacades.user.data.AddressData} as target type.
 */
public class SCLOOTAddressPopulator extends AddressPopulator
{


	@Override
	public void populate(final AddressModel source, final AddressData target)
	{
	super.populate(source,target);

	if(StringUtils.isNotEmpty(source.getCRMaddressStatus())){
		target.setCRMaddressStatus(source.getCRMaddressStatus());
	}
	}


}
