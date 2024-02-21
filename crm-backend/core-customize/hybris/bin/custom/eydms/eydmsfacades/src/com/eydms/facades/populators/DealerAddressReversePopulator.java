package com.eydms.facades.populators;

import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;


import com.eydms.core.model.CityModel;
import com.eydms.core.model.DistrictModel;
import com.eydms.core.model.ERPCityModel;
import com.eydms.core.model.StateModel;
import com.eydms.core.model.TalukaModel;
import com.eydms.core.region.service.RegionService;
import com.eydms.facades.data.CityData;
import com.eydms.facades.data.DistrictData;
import com.eydms.facades.data.ERPCityData;
import com.eydms.facades.data.StateData;
import com.eydms.facades.data.TalukaData;

import de.hybris.platform.commercefacades.user.converters.populator.AddressReversePopulator;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CountryData;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.user.AddressModel;

import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.lang.StringUtils;


public class DealerAddressReversePopulator extends AddressReversePopulator  {


	@Resource
	ModelService modelService;

	@Override
	public void populate(final AddressData addressData, final AddressModel addressModel) throws ConversionException
	{
		
		final CountryData countrydata = new CountryData();
		countrydata.setIsocode("IN");
		countrydata.setName("India");
		addressData.setCountry(countrydata);

		super.populate(addressData, addressModel);
		
		addressModel.setState(addressData.getState());
		addressModel.setTaluka(addressData.getTaluka());
		addressModel.setCity(addressData.getCity());
		addressModel.setDistrict(addressData.getDistrict());
		if(addressData.getErpId()!=null) {
			addressModel.setErpAddressId(addressData.getErpId());
		}
		addressModel.setLongitude(addressData.getLongitude());
		addressModel.setLatitude(addressData.getLatitude());
		
		addressModel.setErpCity(addressData.getErpCity());
		if(StringUtils.isNotBlank(addressData.getRetailerUid())){
			addressModel.setRetailerUid(addressData.getRetailerUid());
		}
		if(StringUtils.isNotBlank(addressData.getRetailerName())){
			addressModel.setRetailerName(addressData.getRetailerName());
		}
		addressModel.setCellphone(addressData.getCellphone());
		addressModel.setEmail(addressData.getEmail());
		addressModel.setAccountName(addressData.getAccountName());
		addressModel.setRetailerAddressPk(addressData.getRetailerAddressPk());
	}



}
