package com.eydms.facades.brand.impl;

import com.eydms.core.brand.service.BrandService;
import com.eydms.facades.brand.BrandFacade;
import com.eydms.facades.data.BrandData;
import com.eydms.facades.data.BrandListData;
import de.hybris.platform.core.model.c2l.BrandModel;
import de.hybris.platform.servicelayer.i18n.I18NService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class BrandFacadeImpl implements BrandFacade {

	@Autowired
	BrandService brandService;
	
	@Autowired
	private I18NService i18NService;
	
	@Override
	public List<BrandData> findAllBrand() {

		
		List<BrandModel> modelList =  brandService.findAllBrand();

		return getBrandDataList(modelList);
	}

	private List<BrandData> getBrandDataList(List<BrandModel> modelList) {
		List<BrandData> dataList = new ArrayList<>();

		modelList.forEach(brand->
		{
			BrandData data = new BrandData();
			data.setIsocode(brand.getIsocode());
			data.setName(brand.getName(i18NService.getCurrentLocale()));
			dataList.add(data);
		});
		return dataList;
	}

	@Override
	public BrandListData getAllBrand() {
		BrandListData brandListData=new BrandListData();
		brandListData.setBrands(getBrandDataList(brandService.getAllBrand()));
		return brandListData;
	}

	@Override
	public List<BrandData> getCompetitorsBrands() {

		
		List<BrandModel> modelList =  brandService.getCompetitorsBrands();
		List<BrandData> dataList = getBrandDataList(modelList);

		return dataList;
	}

}
