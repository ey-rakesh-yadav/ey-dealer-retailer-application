package com.scl.core.brand.service.impl;

import com.scl.core.brand.dao.BrandDao;
import com.scl.core.brand.service.BrandService;
import com.scl.core.dao.TerritoryManagementDao;
import com.scl.core.enums.SclUserType;
import com.scl.core.model.SclUserModel;
import com.scl.core.model.TsoTalukaMappingModel;
import com.scl.core.model.UserSubAreaMappingModel;
import com.scl.core.region.dao.GeographicalRegionDao;
import de.hybris.platform.core.model.c2l.BrandModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BrandServiceImpl implements BrandService {

	@Autowired
	BrandDao brandDao;
	
	@Autowired
	BaseSiteService baseSiteService;

	@Autowired
	UserService userService;

	@Autowired
	TerritoryManagementDao territoryManagementDao;

	@Autowired
	GeographicalRegionDao geographicalRegionDao;
	@Override
	public List<BrandModel> findAllBrand() {

		SclUserModel sclUser =(SclUserModel) userService.getCurrentUser();
		List<String> states = new ArrayList<>();
		if(sclUser!=null && sclUser.getUserType()!=null && sclUser.getUserType().equals(SclUserType.TSO)){
			List<TsoTalukaMappingModel> tsoTalukaMappingForTso = geographicalRegionDao.getTsoTalukaMappingForTso(sclUser);
			if (CollectionUtils.isNotEmpty(tsoTalukaMappingForTso)) {
				states.addAll(tsoTalukaMappingForTso.stream().map(TsoTalukaMappingModel::getState).distinct().toList());
			}
		}else {
			List<UserSubAreaMappingModel> userSubAreaList = geographicalRegionDao.getUserSubAreaMappingForUser(sclUser);
			if (CollectionUtils.isNotEmpty(userSubAreaList)) {
				states.addAll(userSubAreaList.stream().map(data -> data.getState()).distinct().collect(Collectors.toList()));
			}
		}
		List<BrandModel> brandList = brandDao.findBrandByState(states);
		return Objects.nonNull(brandList) ? brandList : Collections.emptyList();
	}

	@Override
	public List<BrandModel> getCompetitorsBrands() {
		SclUserModel sclUser = (SclUserModel) userService.getCurrentUser();
		List<String> states = new ArrayList<>();

		List<BrandModel> brandList = null;

		if(sclUser!=null && sclUser.getUserType()!=null && sclUser.getUserType().equals(SclUserType.TSO)){
			List<TsoTalukaMappingModel> tsoTalukaMappingForTso = geographicalRegionDao.getTsoTalukaMappingForTso(sclUser);
			if (CollectionUtils.isNotEmpty(tsoTalukaMappingForTso)) {
				states.addAll(tsoTalukaMappingForTso.stream().map(TsoTalukaMappingModel::getState).distinct().toList());
			}
		}else {
			List<UserSubAreaMappingModel> userSubAreaList = geographicalRegionDao.getUserSubAreaMappingForUser(sclUser);
			if (CollectionUtils.isNotEmpty(userSubAreaList)) {
				states.addAll(userSubAreaList.stream().map(data -> data.getState()).distinct().collect(Collectors.toList()));
			}
		}
		brandList = brandDao.getCompetitorsBrands(baseSiteService.getCurrentBaseSite(), states);
		return Objects.nonNull(brandList) ? brandList : Collections.emptyList();
	}
	@Override
	public List<BrandModel> getAllBrand() {
		return brandDao.findAllBrand();
	}
}
