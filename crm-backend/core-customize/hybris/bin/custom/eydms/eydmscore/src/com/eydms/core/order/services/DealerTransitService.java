package com.eydms.core.order.services;

import com.eydms.core.model.DealerDriverDetailsModel;
import com.eydms.core.model.DealerVehicleDetailsModel;
import com.eydms.core.model.EyDmsCustomerModel;
import de.hybris.platform.webservicescommons.dto.error.ErrorWsDTO;

import java.util.List;

public interface DealerTransitService {

    boolean isDriverExisting(final String contactNumber);

    boolean isVehicleExisting(final String vehicleNumber);

    List<DealerVehicleDetailsModel> fetchVehicleDetailsForDealer(final EyDmsCustomerModel dealer);

    List<DealerDriverDetailsModel> fetchDriverDetailsForDealer(final EyDmsCustomerModel dealer);

    void saveVehicleDetailsForDealer(final List<DealerVehicleDetailsModel> dealerVehicleDetailsModelList , final String dealerUid);

    void saveDriverDetailsForDealer(final List<DealerDriverDetailsModel> dealerDriverDetailsModelList , final String dealerUid);

    ErrorWsDTO removeVehicle(final String vehicleNumber);

    ErrorWsDTO removeDriver(final String contactNumber);
}

