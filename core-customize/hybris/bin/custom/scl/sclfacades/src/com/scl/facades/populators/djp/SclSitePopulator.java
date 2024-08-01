package com.scl.facades.populators.djp;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.facades.data.SclSiteData;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SclSitePopulator implements Populator<B2BCustomerModel,SclSiteData>{

	@Autowired
	Converter<AddressModel, AddressData> addressConverter;
	
	@Override
	public void populate(B2BCustomerModel source, SclSiteData target) throws ConversionException {
		target.setName(source.getName());
		target.setCode(source.getUid());
		Collection<AddressModel> list = source.getAddresses();
		if(source.getDefaultShipmentAddress()!=null)
		{
			target.setAddress(addressConverter.convert(source.getDefaultShipmentAddress()));
		}
		else {
			if (CollectionUtils.isNotEmpty(list)) {
				List<AddressModel> billingAddressList = list.stream().filter(address -> address.getBillingAddress()).collect(Collectors.toList());
				if (billingAddressList != null && !billingAddressList.isEmpty()) {
					AddressModel billingAddress = billingAddressList.get(0);
					if (null != billingAddress) {
						target.setAddress(addressConverter.convert(billingAddress));
					}
				}
			}
		}
	}

}
