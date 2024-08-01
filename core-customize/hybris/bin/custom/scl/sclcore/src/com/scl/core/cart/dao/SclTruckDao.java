package com.scl.core.cart.dao;

import java.util.List;

import com.scl.core.model.TruckModelMasterModel;

public interface SclTruckDao {

	public List<TruckModelMasterModel> findAllTrucks();

	public TruckModelMasterModel findTruckModelByModelNo(String truckModelNo);
}
