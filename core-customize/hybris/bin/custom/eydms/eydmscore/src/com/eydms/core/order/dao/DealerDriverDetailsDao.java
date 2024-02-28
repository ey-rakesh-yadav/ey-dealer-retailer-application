package com.eydms.core.order.dao;

import com.eydms.core.model.DealerDriverDetailsModel;
import com.eydms.core.model.EyDmsCustomerModel;

import java.util.List;

public interface DealerDriverDetailsDao {

    DealerDriverDetailsModel findDriverDetailsByContactNumber(final String contactNumber);

    List<DealerDriverDetailsModel> findDriverDetailsForDealer(final EyDmsCustomerModel dealer);
}
