package com.scl.core.order.dao;

import com.scl.core.model.DealerDriverDetailsModel;
import com.scl.core.model.SclCustomerModel;

import java.util.List;

public interface DealerDriverDetailsDao {

    DealerDriverDetailsModel findDriverDetailsByContactNumber(final String contactNumber);

    List<DealerDriverDetailsModel> findDriverDetailsForDealer(final SclCustomerModel dealer);
}
