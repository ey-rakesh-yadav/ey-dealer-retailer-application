package com.eydms.core.brand.service.impl;

import com.eydms.core.brand.dao.BrandDao;
import com.eydms.core.brand.service.BrandService;
import com.eydms.core.dao.TerritoryManagementDao;
import com.eydms.core.model.EyDmsUserModel;
import de.hybris.platform.core.model.c2l.BrandModel;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BrandServiceImpl implements BrandService {

	@Autowired
	BrandDao brandDao;
	
	@Autowired
	BaseSiteService baseSiteService;

	@Autowired
	UserService userService;

	@Autowired
	TerritoryManagementDao territoryManagementDao;

	@Override
	public List<BrandModel> findAllBrand() {

		EyDmsUserModel eydmsUser =(EyDmsUserModel) userService.getCurrentUser();
		List<String> states = new ArrayList<>();
		states.add(eydmsUser.getState());
		List<BrandModel> brandList = brandDao.findBrandByState(states);
		return Objects.nonNull(brandList) ? brandList : Collections.emptyList();
	}

	@Override
	public List<BrandModel> getCompetitorsBrands() {
		EyDmsUserModel eydmsUser =(EyDmsUserModel) userService.getCurrentUser();
		List<String> states = new ArrayList<>();
		states.add(eydmsUser.getState());
		List<BrandModel> brandList = brandDao.getCompetitorsBrands(baseSiteService.getCurrentBaseSite(),states);
		return Objects.nonNull(brandList) ? brandList : Collections.emptyList();
	}

	@Override
	public List<BrandModel> getAllBrand() {
		return brandDao.findAllBrand();
	}
}
