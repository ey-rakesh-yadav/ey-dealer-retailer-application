package com.scl.core.territory;

import com.scl.core.model.TerritoryUnitModel;

public interface TerritoryUnitService {
    TerritoryUnitModel getUnitForUid(String uid);

	TerritoryUnitModel getTerritotyUnitforUid(String uid);
}
