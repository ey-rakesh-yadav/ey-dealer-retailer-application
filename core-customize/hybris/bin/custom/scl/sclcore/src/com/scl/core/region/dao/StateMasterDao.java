package com.scl.core.region.dao;

import com.scl.core.model.StateMasterModel;

public interface StateMasterDao {

    StateMasterModel findByCode(String stateCode);
}
