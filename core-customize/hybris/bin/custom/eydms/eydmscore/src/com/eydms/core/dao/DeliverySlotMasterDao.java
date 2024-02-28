package com.eydms.core.dao;

import java.util.List;

import com.eydms.core.model.DeliverySlotMasterModel;

public interface DeliverySlotMasterDao {

	DeliverySlotMasterModel findByDisplayName(String displayName);

	DeliverySlotMasterModel findByEnum(String enumCode);

	List<DeliverySlotMasterModel> findAll();

}
