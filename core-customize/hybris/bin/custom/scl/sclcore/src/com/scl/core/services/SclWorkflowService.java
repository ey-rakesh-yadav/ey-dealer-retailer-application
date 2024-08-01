package com.scl.core.services;

import com.scl.core.enums.TerritoryLevels;
import com.scl.core.enums.WorkflowActions;
import com.scl.core.enums.WorkflowStatus;
import com.scl.core.enums.WorkflowType;
import com.scl.core.model.SclWorkflowActionModel;
import com.scl.core.model.SclWorkflowModel;
import com.scl.core.model.SubAreaMasterModel;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;

public interface SclWorkflowService {

    SclWorkflowModel saveWorkflow(String name, WorkflowStatus status, WorkflowType type);

    SclWorkflowActionModel saveWorkflowAction(SclWorkflowModel sclWorkflowModel, String name, BaseSiteModel baseSite, SubAreaMasterModel subArea, TerritoryLevels territoryLevels);

    boolean updateWorkflowAction(SclWorkflowActionModel sclWorkflowActionModel,B2BCustomerModel actionPerformedBy, WorkflowActions actionPerformed, String comment);
}
