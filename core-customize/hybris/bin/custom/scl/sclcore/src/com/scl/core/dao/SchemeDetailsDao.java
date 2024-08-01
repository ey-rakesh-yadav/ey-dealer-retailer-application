package com.scl.core.dao;

import com.scl.core.enums.PartnerLevel;
import com.scl.core.model.SchemeDetailsModel;

import java.util.Date;
import java.util.List;

public interface SchemeDetailsDao {

    SchemeDetailsModel findSchemeDetailsByPK(final String pk);
    List<SchemeDetailsModel> findOnGoingSchemes(Date endDate, PartnerLevel dealer);
}
