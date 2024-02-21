package com.eydms.core.brand.dao;

import java.util.List;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.c2l.BrandModel;


public interface BrandDao 
{
	public List<BrandModel> findAllBrand();

	BrandModel findBrandById(String brandId);

	public List<BrandModel> getCompetitorsBrands(BaseSiteModel currentBaseSite,List<String> states);

	List<BrandModel> findBrandByState(List<String> states);
}
