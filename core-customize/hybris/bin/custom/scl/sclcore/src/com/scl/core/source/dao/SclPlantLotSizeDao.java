package com.scl.core.source.dao;

import com.scl.core.enums.WarehouseType;
import com.scl.core.model.SclPlantLotSizeModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;

import java.util.List;

public interface SclPlantLotSizeDao {

    /**
     * @param state
     * @param district
     * @param plant
     * @param isPremiumProduct
     * @return
     */
    List<SclPlantLotSizeModel> findAllSourcePlantLotSize(String state, String district, WarehouseModel plant, boolean isPremiumProduct);
}
