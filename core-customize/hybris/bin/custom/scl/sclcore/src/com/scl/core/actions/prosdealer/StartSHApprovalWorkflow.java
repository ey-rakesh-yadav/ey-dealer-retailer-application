package com.scl.core.actions.prosdealer;

import com.scl.core.enums.OnboardingStatus;
import com.scl.core.model.ProsDealerOnboardingProcessModel;
import com.scl.core.model.ProspectiveDealerModel;
import com.scl.core.model.SclUserModel;
import de.hybris.platform.b2b.enums.WorkflowTemplateType;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.services.B2BWorkflowIntegrationService;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.processengine.action.AbstractProceduralAction;
import de.hybris.platform.task.RetryLaterException;
import de.hybris.platform.workflow.WorkflowProcessingService;
import de.hybris.platform.workflow.WorkflowService;
import de.hybris.platform.workflow.model.WorkflowModel;
import de.hybris.platform.workflow.model.WorkflowTemplateModel;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StartSHApprovalWorkflow extends AbstractProceduralAction<ProsDealerOnboardingProcessModel> {

private B2BWorkflowIntegrationService b2bWorkflowIntegrationService;
private WorkflowProcessingService workflowProcessingService;
private WorkflowService workflowService;

private static final Logger LOG = Logger.getLogger(StartSHApprovalWorkflow.class);

@Override
public void executeAction(ProsDealerOnboardingProcessModel prosDealerApprovalProcessModel) throws RetryLaterException, Exception {

   try{
       ProspectiveDealerModel prospectiveDealerModel = prosDealerApprovalProcessModel.getProsDealer();

       Set<B2BCustomerModel> approversSet = prospectiveDealerModel.getApprovers();
       List<SclUserModel> approvers  = approversSet.stream()
               .filter(SclUserModel.class::isInstance)
               .map(SclUserModel.class::cast)
               .collect(Collectors.toList());

       if (LOG.isDebugEnabled())
       {
           final List<String> approverUids = new ArrayList<String>();
           for (final SclUserModel approver : approvers)
           {
               approverUids.add(approver.getUid());
           }
           LOG.debug(String.format("Creating a SH approval worflow for prosDealer %s and approvers %s", prospectiveDealerModel.getDealerCode(), approverUids));
       }
       final String workflowTemplateCode = b2bWorkflowIntegrationService.generateWorkflowTemplateCode("SH_APPROVAL_WORKFLOW",
               approvers);
       final WorkflowTemplateModel workflowTemplate = b2bWorkflowIntegrationService.createWorkflowTemplate(approvers,
               workflowTemplateCode, "Generated SH  Approval Workflow", WorkflowTemplateType.PROSPECTIVE_DEALER_APPROVAL_SH);

       final WorkflowModel workflow = workflowService.createWorkflow(workflowTemplate.getName(), workflowTemplate,
               Collections.<ItemModel> singletonList(prosDealerApprovalProcessModel), workflowTemplate.getOwner());
       workflowProcessingService.startWorkflow(workflow);
       this.modelService.saveAll(); // workaround for PLA-10938
       prospectiveDealerModel.setWorkflow(workflow);
       prospectiveDealerModel.setOnboardingStatus(OnboardingStatus.PENDING_APPROVAL_SH);
       //order.setExhaustedApprovers(new HashSet<B2BCustomerModel>(approvers));
       this.modelService.save(prospectiveDealerModel);

       /*if (escalate)
       {
           b2bEscalationService.scheduleEscalationTask(prospectiveDealerModel);
       }*/

   }
   catch (Exception e){
       LOG.error(e.getMessage(), e);
       throw new IllegalStateException(e.getMessage(), e);
   }
}

public B2BWorkflowIntegrationService getB2bWorkflowIntegrationService() {
    return b2bWorkflowIntegrationService;
}

public void setB2bWorkflowIntegrationService(B2BWorkflowIntegrationService b2bWorkflowIntegrationService) {
    this.b2bWorkflowIntegrationService = b2bWorkflowIntegrationService;
}

public WorkflowProcessingService getWorkflowProcessingService() {
    return workflowProcessingService;
}

public void setWorkflowProcessingService(WorkflowProcessingService workflowProcessingService) {
    this.workflowProcessingService = workflowProcessingService;
}

public WorkflowService getWorkflowService() {
    return workflowService;
}

public void setWorkflowService(WorkflowService workflowService) {
    this.workflowService = workflowService;
}

/*public B2BEscalationService getB2bEscalationService() {
    return b2bEscalationService;
}

public void setB2bEscalationService(B2BEscalationService b2bEscalationService) {
    this.b2bEscalationService = b2bEscalationService;
}*/


}
