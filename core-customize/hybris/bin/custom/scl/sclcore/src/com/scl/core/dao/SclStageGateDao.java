package com.scl.core.dao;

import com.scl.core.enums.WarehouseType;
import com.scl.core.model.SclBrandModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.StageGateSequenceMapperModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;

public interface SclStageGateDao {

    List<StageGateSequenceMapperModel> getStageGateSequenceMapperListForSource(WarehouseType warehouseType);

    List<OrderModel> getS4OrdersFoGetCall();

}
