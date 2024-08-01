package com.scl.core.brand.dao;

import java.util.List;

import com.scl.core.model.CounterVisitMasterModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.c2l.BrandModel;


public interface BrandDao 
{
	public List<BrandModel> findAllBrand();

	BrandModel findBrandById(String brandId);

	public List<BrandModel> getCompetitorsBrands(BaseSiteModel currentBaseSite,List<String> states);

	List<BrandModel> findBrandByState(List<String> states);

	/**
	 * Get List Of Brands
	 * @param brandIds
	 * @return
	 */
	List<BrandModel> findBrandProductById(List<String> brandIds);
	/**
	 * Get Selected Brands with Latest Counter Visit
	 * @param latestCounterVisit
	 * @return
	 */
	List<BrandModel> selectedBrands(CounterVisitMasterModel latestCounterVisit);
}
