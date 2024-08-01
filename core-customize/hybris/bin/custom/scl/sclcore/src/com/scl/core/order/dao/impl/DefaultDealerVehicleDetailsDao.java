package com.scl.core.order.dao.impl;

import com.scl.core.model.DealerVehicleDetailsModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.order.dao.DealerVehicleDetailsDao;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;

import java.util.Collections;
import java.util.List;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class DefaultDealerVehicleDetailsDao extends DefaultGenericDao<DealerVehicleDetailsModel> implements DealerVehicleDetailsDao {

    public DefaultDealerVehicleDetailsDao() {
        super(DealerVehicleDetailsModel._TYPECODE);
    }

    @Override
    public DealerVehicleDetailsModel findVehicleDetailsByVehicleNumber(String vehicleNumber) {
        validateParameterNotNullStandardMessage("vehicleNumber", vehicleNumber);
        final List<DealerVehicleDetailsModel> dealerVehicleDetailsModels = this.find(Collections.singletonMap(DealerVehicleDetailsModel.VEHICLENUMBER, vehicleNumber));
        if (dealerVehicleDetailsModels.size() > 1)
        {
            throw new AmbiguousIdentifierException(
                    String.format("Found %d Vehicle Details with the contact number value: '%s', which should be unique", dealerVehicleDetailsModels.size(),
                            vehicleNumber));
        }
        else
        {
            return dealerVehicleDetailsModels.isEmpty() ? null : dealerVehicleDetailsModels.get(0);
        }
    }

    @Override
    public List<DealerVehicleDetailsModel> findVehicleDetailsForDealer(final SclCustomerModel dealer){
        validateParameterNotNull(dealer, "dealer  must not be null");
        return this.find(Collections.singletonMap(DealerVehicleDetailsModel.DEALER, dealer));
    }

}

