package com.scl.core.order.services;

import com.scl.core.model.DealerDriverDetailsModel;
import com.scl.core.model.DealerVehicleDetailsModel;
import com.scl.core.model.SclCustomerModel;
import de.hybris.platform.webservicescommons.dto.error.ErrorWsDTO;

import java.util.List;

public interface DealerTransitService {

    boolean isDriverExisting(final String contactNumber);

    boolean isVehicleExisting(final String vehicleNumber);

    List<DealerVehicleDetailsModel> fetchVehicleDetailsForDealer(final SclCustomerModel dealer);

    List<DealerDriverDetailsModel> fetchDriverDetailsForDealer(final SclCustomerModel dealer);

    void saveVehicleDetailsForDealer(final List<DealerVehicleDetailsModel> dealerVehicleDetailsModelList , final String dealerUid);

    void saveDriverDetailsForDealer(final List<DealerDriverDetailsModel> dealerDriverDetailsModelList , final String dealerUid);

    ErrorWsDTO removeVehicle(final String vehicleNumber);

    ErrorWsDTO removeDriver(final String contactNumber);
}

