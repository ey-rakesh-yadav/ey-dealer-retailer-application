package com.scl.core.dao;

import com.scl.core.model.PointRequisitionModel;

import java.util.Date;
import java.util.List;

public interface AutoCancellingRequestRaisedByInfluencerDao {
    List<PointRequisitionModel> getListOfRequestRaisedBeforeThreeDays(Date requestRaisedDate);
}
