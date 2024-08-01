package com.scl.core.dao;

import com.scl.core.enums.WorkflowType;
import com.scl.core.model.SclWorkflowModel;

public interface SclWorkflowDao {

    SclWorkflowModel getSclWorkflowByType(WorkflowType type);
}
