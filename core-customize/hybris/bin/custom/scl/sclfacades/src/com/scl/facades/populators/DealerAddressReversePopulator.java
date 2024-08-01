package com.scl.facades.populators;

import javax.annotation.Resource;


import com.scl.core.model.GeographicalMasterModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.region.service.GeographicalRegionService;

import de.hybris.platform.commercefacades.user.converters.populator.AddressReversePopulator;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CountryData;
import de.hybris.platform.core.model.user.AddressModel;

import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.util.Objects;


public class DealerAddressReversePopulator extends AddressReversePopulator  {


	@Resource
	GeographicalRegionService geographicalRegionService;
	@Resource
	ModelService modelService;

	@Resource
	UserService userService;

	@Override
	public void populate(final AddressData addressData, final AddressModel addressModel) throws ConversionException
	{

		final CountryData countrydata = new CountryData();
		countrydata.setIsocode("IN");
		countrydata.setName("India");
		addressData.setCountry(countrydata);

		super.populate(addressData, addressModel);

		setGeoGraphicalMaster(addressModel,addressData);
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
			SclCustomerModel sclCustomerModel=(SclCustomerModel)userService.getUserForUID(addressData.getRetailerUid());
			addressModel.setRetailerUid(Objects.nonNull(sclCustomerModel)?sclCustomerModel.getCustomerNo(): Strings.EMPTY);
		}

		if(StringUtils.isNotBlank(addressData.getRetailerName())){
			addressModel.setRetailerName(addressData.getRetailerName());
		}
		addressModel.setDuplicate(Boolean.FALSE);
		addressModel.setCellphone(addressData.getCellphone());
		addressModel.setEmail(addressData.getEmail());
		addressModel.setAccountName(addressData.getAccountName());
		addressModel.setRetailerAddressPk(addressData.getRetailerAddressPk());
	}

	private void setGeoGraphicalMaster(AddressModel addressModel, AddressData addressData) {
		GeographicalMasterModel geographicalMaster = null;
		if(StringUtils.isNotBlank(addressData.getTransportationZone())) {
			geographicalMaster = geographicalRegionService.getGeographicalMaster(addressData.getTransportationZone());

		} else if (StringUtils.isNotBlank(addressData.getState()) && StringUtils.isNotBlank(addressData.getDistrict()) && StringUtils.isNotBlank(addressData.getTaluka()) && StringUtils.isNotBlank(addressData.getErpCity())) {
			geographicalMaster = geographicalRegionService.getGeographicalMaster(addressData.getState(), addressData.getDistrict(), addressData.getTaluka(), addressData.getErpCity());
		}
		if (Objects.nonNull(geographicalMaster)) {
			addressModel.setGeographicalMaster(geographicalMaster);
			addressModel.setTransportationZone(geographicalMaster.getTransportationZone());
		}

	}

}
