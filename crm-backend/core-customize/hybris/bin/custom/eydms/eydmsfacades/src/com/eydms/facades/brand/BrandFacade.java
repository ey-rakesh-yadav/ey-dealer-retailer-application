package com.eydms.facades.brand;

import com.eydms.facades.data.BrandData;
import com.eydms.facades.data.BrandListData;

import java.util.List;

public interface BrandFacade 
{
	List<BrandData> findAllBrand();
	BrandListData getAllBrand();

	List<BrandData> getCompetitorsBrands();
}
