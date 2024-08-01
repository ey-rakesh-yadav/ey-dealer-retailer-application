/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.scl.facades.populators;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.fasterxml.jackson.databind.deser.std.StringArrayDeserializer;
import com.scl.core.model.CityModel;
import com.scl.core.model.DistrictModel;
import com.scl.core.model.ERPCityModel;
import com.scl.core.model.StateModel;
import com.scl.core.model.TalukaModel;


import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CountryData;
import de.hybris.platform.commercefacades.user.data.RegionData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.c2l.RegionModel;
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

	public RegionData populateRegion(RegionModel source)
	{
		RegionData regionData = new RegionData();
		if(source != null) {
			regionData.setIsocode(source.getIsocode());
			regionData.setName(source.getName());
		}
		return regionData;
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
		target.setRegion(populateRegion(source.getRegion()));
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
		if(StringUtils.isNotBlank(source.getPartnerFunctionId())){
			target.setRetailerUid(source.getPartnerFunctionId());
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
		target.setLine3(source.getLine3());
		target.setLine4(source.getLine4());
		target.setLine5(source.getLine5());
		if(source.getGeographicalMaster()!=null) {
			target.setTransportationZone(source.getGeographicalMaster().getTransportationZone());
			target.setState(source.getGeographicalMaster().getState());
			target.setTaluka(source.getGeographicalMaster().getTaluka());
			target.setDistrict(source.getGeographicalMaster().getDistrict());
			target.setErpCity(source.getGeographicalMaster().getErpCity());
		}
	}
}
