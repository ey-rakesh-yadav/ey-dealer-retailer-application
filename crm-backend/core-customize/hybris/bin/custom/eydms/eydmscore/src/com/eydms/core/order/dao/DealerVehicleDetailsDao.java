package com.eydms.core.order.dao;

import com.eydms.core.model.DealerVehicleDetailsModel;
import com.eydms.core.model.EyDmsCustomerModel;

import java.util.List;

public interface DealerVehicleDetailsDao {

    DealerVehicleDetailsModel findVehicleDetailsByVehicleNumber(final String vehicleNumber);

    List<DealerVehicleDetailsModel> findVehicleDetailsForDealer(final EyDmsCustomerModel dealer);
}
