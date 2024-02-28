package com.eydms.core.region.dao;

import com.eydms.core.model.StateMasterModel;

public interface StateMasterDao {

    StateMasterModel findByCode(String stateCode);
}
