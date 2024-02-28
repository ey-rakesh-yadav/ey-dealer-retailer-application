package com.eydms.core.event;

import com.eydms.core.actions.prosdealer.StartSOApprovalWorkflow;
import com.eydms.core.enums.OnboardingStatus;
import com.eydms.core.model.EyDmsUserModel;
import de.hybris.platform.b2b.enums.WorkflowTemplateType;
import de.hybris.platform.b2b.services.B2BWorkflowIntegrationService;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.user.PhoneContactInfoModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.workflow.WorkflowProcessingService;
import de.hybris.platform.workflow.WorkflowService;
import de.hybris.platform.workflow.model.WorkflowModel;
import de.hybris.platform.workflow.model.WorkflowTemplateModel;
import org.apache.log4j.Logger;

import java.util.*;

public class UpdateContactNumberEventListener extends AbstractEventListener<UpdateContactNumberEvent> {

    private B2BWorkflowIntegrationService b2bWorkflowIntegrationService;
    private WorkflowProcessingService workflowProcessingService;
    private WorkflowService workflowService;

    private ModelService modelService;

    private static final Logger LOG = Logger.getLogger(UpdateContactNumberEventListener.class);
    @Override
    protected void onEvent(UpdateContactNumberEvent updateContactNumberEvent) {

        try{
            PhoneContactInfoModel phoneContactInfoToUpdate = updateContactNumberEvent.getPhoneContactInfoModel();

            final EyDmsUserModel user = (EyDmsUserModel) phoneContactInfoToUpdate.getUser();

            //TODO:://Change it to get logic of supervisior
            final Optional<EyDmsUserModel> reporter= user.getReporter().stream().findFirst();
            EyDmsUserModel supervisor = reporter.orElse(null);
            if(null != supervisor){
                final String workflowTemplateCode = getB2bWorkflowIntegrationService().generateWorkflowTemplateCode("CONTACT_NUMBER_APPROVAL_WORKFLOW",
                        List.of(supervisor));

                //TODO://Change Workflow template type to CONTACT_NUMBER_APPROVAL
                final WorkflowTemplateModel workflowTemplate = getB2bWorkflowIntegrationService().createWorkflowTemplate(List.of(supervisor),
                        workflowTemplateCode, "Generated Contact Number Approval Workflow", WorkflowTemplateType.CONTACT_NUMBER_APPROVAL);

                final WorkflowModel workflow = workflowService.createWorkflow(workflowTemplate.getName(), workflowTemplate,
                        Collections.<ItemModel> singletonList(phoneContactInfoToUpdate), workflowTemplate.getOwner());
                workflowProcessingService.startWorkflow(workflow);
                getModelService().saveAll(); // workaround for PLA-10938

                phoneContactInfoToUpdate.setWorkflow(workflow);

                getModelService().save(phoneContactInfoToUpdate);

            }
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

    public ModelService getModelService() {
        return modelService;
    }

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

}
