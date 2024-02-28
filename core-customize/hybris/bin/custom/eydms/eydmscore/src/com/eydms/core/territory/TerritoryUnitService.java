package com.eydms.core.territory;

import com.eydms.core.model.TerritoryUnitModel;

public interface TerritoryUnitService {
    TerritoryUnitModel getUnitForUid(String uid);

	TerritoryUnitModel getTerritotyUnitforUid(String uid);
}
