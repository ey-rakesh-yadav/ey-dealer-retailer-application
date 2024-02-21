package com.eydms.core.services;

import com.eydms.core.enums.TerritoryLevels;
import com.eydms.core.enums.WorkflowActions;
import com.eydms.core.enums.WorkflowStatus;
import com.eydms.core.enums.WorkflowType;
import com.eydms.core.model.EyDmsWorkflowActionModel;
import com.eydms.core.model.EyDmsWorkflowModel;
import com.eydms.core.model.SubAreaMasterModel;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;

public interface EyDmsWorkflowService {

    EyDmsWorkflowModel saveWorkflow(String name, WorkflowStatus status, WorkflowType type);

    EyDmsWorkflowActionModel saveWorkflowAction(EyDmsWorkflowModel eydmsWorkflowModel, String name, BaseSiteModel baseSite, SubAreaMasterModel subArea, TerritoryLevels territoryLevels);

    boolean updateWorkflowAction(EyDmsWorkflowActionModel eydmsWorkflowActionModel,B2BCustomerModel actionPerformedBy, WorkflowActions actionPerformed, String comment);
}
