package com.scl.facades.brand.impl;

import com.scl.core.brand.service.BrandService;
import com.scl.facades.brand.BrandFacade;
import com.scl.facades.data.BrandData;
import com.scl.facades.data.BrandListData;
import de.hybris.platform.core.model.c2l.BrandModel;
import de.hybris.platform.servicelayer.i18n.I18NService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

		for (BrandModel brand : modelList) {
			BrandData data = new BrandData();
			data.setIsocode(brand.getIsocode());
			data.setName(brand.getName(i18NService.getCurrentLocale()));
			dataList.add(data);
		}
		dataList = dataList.stream().sorted(Comparator.comparing(BrandData::getIsocode)).collect(Collectors.toList());
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
