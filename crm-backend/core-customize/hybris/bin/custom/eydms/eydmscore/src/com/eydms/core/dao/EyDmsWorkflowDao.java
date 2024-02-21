package com.eydms.core.dao;

import com.eydms.core.enums.WorkflowType;
import com.eydms.core.model.EyDmsWorkflowModel;

public interface EyDmsWorkflowDao {

    EyDmsWorkflowModel getEyDmsWorkflowByType(WorkflowType type);
}
