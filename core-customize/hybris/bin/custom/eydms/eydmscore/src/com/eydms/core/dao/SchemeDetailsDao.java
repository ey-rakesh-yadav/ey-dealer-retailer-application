package com.eydms.core.dao;

import com.eydms.core.enums.PartnerLevel;
import com.eydms.core.model.SchemeDetailsModel;

import java.util.Date;
import java.util.List;

public interface SchemeDetailsDao {

    SchemeDetailsModel findSchemeDetailsByPK(final String pk);
    List<SchemeDetailsModel> findOnGoingSchemes(Date endDate, PartnerLevel dealer);
}
