package com.scl.core.order.dao;

import com.scl.core.model.RejectionReasonModel;

import java.util.List;

public interface RejectionReasonsDao {
    /**
     *
     * @return
     */
     List<RejectionReasonModel> getRejectionReasons();
}
