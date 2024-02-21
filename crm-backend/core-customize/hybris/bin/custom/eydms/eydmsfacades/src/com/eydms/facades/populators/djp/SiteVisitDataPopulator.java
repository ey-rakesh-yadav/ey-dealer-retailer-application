package com.eydms.facades.populators.djp;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.eydms.core.model.CounterVisitMasterModel;
import com.eydms.facades.data.SitePreferredBrandData;
import com.eydms.facades.data.SiteVisitFormData;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class SiteVisitDataPopulator implements Populator<CounterVisitMasterModel,SiteVisitFormData>{

	@Override
	public void populate(CounterVisitMasterModel source, SiteVisitFormData target) throws ConversionException {
		Assert.notNull(source, "Parameter source cannot be null.");
		Assert.notNull(target, "Parameter target cannot be null.");
		target.setCustomerType(source.getEyDmsCustomer().getCustomerType() != null ? source.getEyDmsCustomer().getCustomerType().getCode() : "");
		target.setContactNumber(StringUtils.isNotBlank(source.getEyDmsCustomer().getContactNumber())?source.getEyDmsCustomer().getContactNumber():"");
		target.setContactPersonName(StringUtils.isNotBlank(source.getEyDmsCustomer().getContactPersonName())?source.getEyDmsCustomer().getContactPersonName():"");
		target.setIsShreeCounter(source.getEyDmsCustomer().getIsShreeSite() != null ? source.getEyDmsCustomer().getIsShreeSite() : false);
		target.setIsBangurCounter(source.getEyDmsCustomer().getIsBangurSite() != null ? source.getEyDmsCustomer().getIsBangurSite() : false);
		target.setIsRockstrongCounter(source.getEyDmsCustomer().getIsRockstrongSite() != null ? source.getEyDmsCustomer().getIsRockstrongSite() : false);
		target.setBrandUnderUse(source.getEyDmsCustomer().getBrandUnderUse() != null ? source.getEyDmsCustomer().getBrandUnderUse().getIsocode() : "");
		target.setForPrice(source.getEyDmsCustomer().getForPrice() != null ? source.getEyDmsCustomer().getForPrice() : 0.0);
		
		List<SitePreferredBrandData> preferredBrandList = new ArrayList<SitePreferredBrandData>();
		if(CollectionUtils.isNotEmpty(source.getEyDmsCustomer().getPreferredBrandsList())){
			source.getEyDmsCustomer().getPreferredBrandsList().forEach(preferredBrand -> {
				SitePreferredBrandData brandData = new SitePreferredBrandData();
				brandData.setPreferredBrand(preferredBrand.getPreferredBrand() != null ? preferredBrand.getPreferredBrand().getIsocode() : "");
				brandData.setReasonOfPreference(preferredBrand.getReasonOfPreference() != null ? preferredBrand.getReasonOfPreference() : "");
				preferredBrandList.add(brandData);
			});
		}
		
		target.setPreferredBrandAndReason(preferredBrandList);
		target.setAreaOfConstruction(source.getEyDmsCustomer().getAreaOfConstruction() != null ? source.getEyDmsCustomer().getAreaOfConstruction() : 0.0);
		target.setCurrentStageOfConstruction(source.getEyDmsCustomer().getCurrentStageOfConstruction() != null ? source.getEyDmsCustomer().getCurrentStageOfConstruction().getCode() : "");
		target.setBalancePotential(source.getEyDmsCustomer().getBalancePotential() != null ? source.getEyDmsCustomer().getBalancePotential() : 0.0);
		target.setMonthlyConsumption(source.getEyDmsCustomer().getMonthlyConsumption() != null ? source.getEyDmsCustomer().getMonthlyConsumption() : 0.0);
		target.setCompletionPeriod(source.getEyDmsCustomer().getCompletionPeriod() != null ? source.getEyDmsCustomer().getCompletionPeriod() : "");
		target.setTargetOfConversion(source.getEyDmsCustomer().getTargetOfConversion() != null ? source.getEyDmsCustomer().getTargetOfConversion().getCode() : "");
		target.setActivitiesPerformedAtSite(source.getEyDmsCustomer().getActivitiesPerformedAtSite() != null ? source.getEyDmsCustomer().getActivitiesPerformedAtSite().getCode() : "");
	}

}
