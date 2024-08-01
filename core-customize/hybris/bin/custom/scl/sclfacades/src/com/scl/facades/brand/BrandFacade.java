package com.scl.facades.brand;

import com.scl.facades.data.BrandData;
import com.scl.facades.data.BrandListData;

import java.util.List;

public interface BrandFacade 
{
	List<BrandData> findAllBrand();
	BrandListData getAllBrand();

	List<BrandData> getCompetitorsBrands();
}
