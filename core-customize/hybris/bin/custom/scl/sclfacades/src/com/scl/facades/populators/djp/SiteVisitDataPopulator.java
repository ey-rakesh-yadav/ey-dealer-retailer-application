package com.scl.facades.populators.djp;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.scl.core.model.CounterVisitMasterModel;
import com.scl.facades.data.SitePreferredBrandData;
import com.scl.facades.data.SiteVisitFormData;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class SiteVisitDataPopulator implements Populator<CounterVisitMasterModel,SiteVisitFormData>{

	@Override
	public void populate(CounterVisitMasterModel source, SiteVisitFormData target) throws ConversionException {
		Assert.notNull(source, "Parameter source cannot be null.");
		Assert.notNull(target, "Parameter target cannot be null.");
		target.setCustomerType(source.getSclCustomer().getCustomerType() != null ? source.getSclCustomer().getCustomerType().getCode() : "");
		target.setContactNumber(StringUtils.isNotBlank(source.getSclCustomer().getContactNumber())?source.getSclCustomer().getContactNumber():"");
		target.setContactPersonName(StringUtils.isNotBlank(source.getSclCustomer().getContactPersonName())?source.getSclCustomer().getContactPersonName():"");
		target.setIsShreeCounter(source.getSclCustomer().getIsShreeSite() != null ? source.getSclCustomer().getIsShreeSite() : false);
		target.setIsBangurCounter(source.getSclCustomer().getIsBangurSite() != null ? source.getSclCustomer().getIsBangurSite() : false);
		target.setIsRockstrongCounter(source.getSclCustomer().getIsRockstrongSite() != null ? source.getSclCustomer().getIsRockstrongSite() : false);
		target.setBrandUnderUse(source.getSclCustomer().getBrandUnderUse() != null ? source.getSclCustomer().getBrandUnderUse().getIsocode() : "");
		target.setForPrice(source.getSclCustomer().getForPrice() != null ? source.getSclCustomer().getForPrice() : 0.0);
		
		List<SitePreferredBrandData> preferredBrandList = new ArrayList<SitePreferredBrandData>();
		if(CollectionUtils.isNotEmpty(source.getSclCustomer().getPreferredBrandsList())){
			source.getSclCustomer().getPreferredBrandsList().forEach(preferredBrand -> {
				SitePreferredBrandData brandData = new SitePreferredBrandData();
				brandData.setPreferredBrand(preferredBrand.getPreferredBrand() != null ? preferredBrand.getPreferredBrand().getIsocode() : "");
				brandData.setReasonOfPreference(preferredBrand.getReasonOfPreference() != null ? preferredBrand.getReasonOfPreference() : "");
				preferredBrandList.add(brandData);
			});
		}
		
		target.setPreferredBrandAndReason(preferredBrandList);
		target.setAreaOfConstruction(source.getSclCustomer().getAreaOfConstruction() != null ? source.getSclCustomer().getAreaOfConstruction() : 0.0);
		target.setCurrentStageOfConstruction(source.getSclCustomer().getCurrentStageOfConstruction() != null ? source.getSclCustomer().getCurrentStageOfConstruction().getCode() : "");
		target.setBalancePotential(source.getSclCustomer().getBalancePotential() != null ? source.getSclCustomer().getBalancePotential() : 0.0);
		target.setMonthlyConsumption(source.getSclCustomer().getMonthlyConsumption() != null ? source.getSclCustomer().getMonthlyConsumption() : 0.0);
		target.setCompletionPeriod(source.getSclCustomer().getCompletionPeriod() != null ? source.getSclCustomer().getCompletionPeriod() : "");
		target.setTargetOfConversion(source.getSclCustomer().getTargetOfConversion() != null ? source.getSclCustomer().getTargetOfConversion().getCode() : "");
		target.setActivitiesPerformedAtSite(source.getSclCustomer().getActivitiesPerformedAtSite() != null ? source.getSclCustomer().getActivitiesPerformedAtSite().getCode() : "");
	}

}
