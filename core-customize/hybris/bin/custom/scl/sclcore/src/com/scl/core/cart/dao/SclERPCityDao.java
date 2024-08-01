package com.scl.core.cart.dao;

import java.util.List;

import com.scl.core.model.ERPCityModel;

public interface SclERPCityDao {

    List<ERPCityModel> findERPCityByISOCode(String isocode);

}