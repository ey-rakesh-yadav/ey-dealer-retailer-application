package com.eydms.facades.order;

import com.eydms.facades.data.order.vehicle.DealerDriverDetailsListData;
import com.eydms.facades.data.order.vehicle.DealerVehicleDetailsListData;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;
import de.hybris.platform.webservicescommons.dto.error.ErrorWsDTO;

public interface DealerTransitFacade {

    DealerVehicleDetailsListData getDealerVehicleDetails(final String dealerUid);

    DealerDriverDetailsListData getDealerDriverDetails(final String dealerUid);

    ErrorListWsDTO createDealerVehicleDetails(final DealerVehicleDetailsListData dealerVehicleDetailsListData, final String dealerUid);

    ErrorListWsDTO createDealerDriverDetails(final DealerDriverDetailsListData dealerDriverDetailsListData, final String dealerUid);

    ErrorWsDTO removeVehicle(final String vehicleNumber);

    ErrorWsDTO removeDriver(final String contactNumber);
}
