package com.scl.core.dao;

import java.util.List;

import com.scl.core.model.DeliverySlotMasterModel;

public interface DeliverySlotMasterDao {

	DeliverySlotMasterModel findByDisplayName(String displayName);

	DeliverySlotMasterModel findByEnum(String enumCode);

	List<DeliverySlotMasterModel> findAll();

	DeliverySlotMasterModel findByCentreTime(String centreTime);

}
