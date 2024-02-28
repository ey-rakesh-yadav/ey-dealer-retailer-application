package com.eydms.core.dao;

import com.eydms.core.model.PointRequisitionModel;

import java.util.Date;
import java.util.List;

public interface AutoCancellingRequestRaisedByInfluencerDao {
    List<PointRequisitionModel> getListOfRequestRaisedBeforeThreeDays(Date requestRaisedDate);
}
