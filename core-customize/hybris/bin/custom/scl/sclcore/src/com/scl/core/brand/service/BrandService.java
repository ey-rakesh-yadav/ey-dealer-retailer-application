package com.scl.core.brand.service;

import de.hybris.platform.core.model.c2l.BrandModel;

import java.util.List;


public interface BrandService
{
	List<BrandModel> findAllBrand();

	List<BrandModel> getCompetitorsBrands();
	List<BrandModel> getAllBrand();
}
