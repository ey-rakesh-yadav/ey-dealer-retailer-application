package com.eydms.core.cart.dao;

import java.util.List;

import com.eydms.core.model.TruckModelMasterModel;

public interface EyDmsTruckDao {

	public List<TruckModelMasterModel> findAllTrucks();

	public TruckModelMasterModel findTruckModelByModelNo(String truckModelNo);
}
