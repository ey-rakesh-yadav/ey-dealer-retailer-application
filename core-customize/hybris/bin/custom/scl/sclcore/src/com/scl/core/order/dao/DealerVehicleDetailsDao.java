package com.scl.core.order.dao;

import com.scl.core.model.DealerVehicleDetailsModel;
import com.scl.core.model.SclCustomerModel;

import java.util.List;

public interface DealerVehicleDetailsDao {

    DealerVehicleDetailsModel findVehicleDetailsByVehicleNumber(final String vehicleNumber);

    List<DealerVehicleDetailsModel> findVehicleDetailsForDealer(final SclCustomerModel dealer);
}
