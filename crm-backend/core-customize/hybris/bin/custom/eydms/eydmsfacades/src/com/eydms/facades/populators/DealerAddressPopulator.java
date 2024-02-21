/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.eydms.facades.populators;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.eydms.core.model.CityModel;
import com.eydms.core.model.DistrictModel;
import com.eydms.core.model.ERPCityModel;
import com.eydms.core.model.StateModel;
import com.eydms.core.model.TalukaModel;
import com.eydms.facades.data.CityData;
import com.eydms.facades.data.DistrictData;
import com.eydms.facades.data.ERPCityData;
import com.eydms.facades.data.StateData;
import com.eydms.facades.data.TalukaData;


import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CountryData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.user.AddressModel;
import org.apache.commons.lang.StringUtils;

public class DealerAddressPopulator implements Populator<AddressModel, AddressData>
{

	public CountryData populateCountry(CountryModel source)
	{
		CountryData countryData = new CountryData();
		if(source != null) {
			countryData.setIsocode(source.getIsocode());
			countryData.setName(source.getName());
		}
		return countryData;
	}

	@Override
	public void populate(AddressModel source, AddressData target) {
		target.setState(source.getState());
		target.setTaluka(source.getTaluka());
		target.setCity(source.getCity());
		target.setDistrict(source.getDistrict());
		target.setLongitude(source.getLongitude());
		target.setLatitude(source.getLatitude());
		target.setErpCity(source.getErpCity());
		target.setCountry(populateCountry(source.getCountry()));      
		if(source.getCreationtime()!=null) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");  
			String strDate = dateFormat.format(source.getCreationtime());  
			target.setCreatedDate(strDate);
		}
		if(source.getModifiedtime()!=null) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");  
			String strDate = dateFormat.format(source.getModifiedtime());  
			target.setModifiedDate(strDate);
		}
		if(StringUtils.isNotBlank(source.getRetailerUid())){
			target.setRetailerUid(source.getRetailerUid());
		}

		if(StringUtils.isNotBlank(source.getRetailerName())){
			target.setRetailerName(source.getRetailerName());
		}
		if(null!= source.getIsPrimaryAddress()){
			target.setIsPrimaryAddress(source.getIsPrimaryAddress());
		}
		target.setCellphone(source.getCellphone());
		target.setEmail(source.getEmail());
		target.setAccountName(source.getAccountName());
		target.setErpId(source.getErpAddressId());
		target.setRetailerAddressPk(source.getRetailerAddressPk());
	}
}
