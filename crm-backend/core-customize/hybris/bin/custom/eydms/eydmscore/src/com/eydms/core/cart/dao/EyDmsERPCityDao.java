package com.eydms.core.cart.dao;

import java.util.List;

import com.eydms.core.model.ERPCityModel;

public interface EyDmsERPCityDao {

    List<ERPCityModel> findERPCityByISOCode(String isocode);

}