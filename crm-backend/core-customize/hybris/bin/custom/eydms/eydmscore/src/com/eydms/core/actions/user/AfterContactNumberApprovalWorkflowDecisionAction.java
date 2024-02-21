package com.eydms.core.actions.user;


import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.process.approval.actions.B2BAbstractWorkflowAutomatedAction;
import de.hybris.platform.core.enums.PhoneContactInfoType;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.user.AbstractContactInfoModel;
import de.hybris.platform.core.model.user.PhoneContactInfoModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.workflow.model.WorkflowActionModel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class AfterContactNumberApprovalWorkflowDecisionAction extends B2BAbstractWorkflowAutomatedAction {

    private ModelService modelService;

    private static final Logger LOGGER = Logger.getLogger(AfterContactNumberApprovalWorkflowDecisionAction.class);

    @Override
    public void performAction(WorkflowActionModel action) {

        LOGGER.info("IN AfterContactNumberApprovalWorkflowDecisionAction");

        if(CollectionUtils.isNotEmpty(action.getAttachmentItems())){
            for (ItemModel it : action.getAttachmentItems()) {
                if (it instanceof PhoneContactInfoModel) {
                    PhoneContactInfoModel phoneContactInfoModel = (PhoneContactInfoModel) it;

                    //Remove Existing contact number before adding new
                    List<PhoneContactInfoModel> existingContactInfos = Optional.ofNullable(phoneContactInfoModel.getUser().getContactInfos())
                            .orElseGet(Collections::emptyList)
                            .stream().filter(PhoneContactInfoModel.class ::isInstance)
                            .map(PhoneContactInfoModel.class ::cast).collect(Collectors.toList());

                    if(CollectionUtils.isNotEmpty(existingContactInfos)){
                        for(PhoneContactInfoModel contactInfoModel :existingContactInfos){
                            if(contactInfoModel.getApproved() && PhoneContactInfoType.WORK.equals(contactInfoModel.getType())){
                                getModelService().remove(contactInfoModel);
                                getModelService().refresh(phoneContactInfoModel.getUser());
                            }
                        }
                    }
                    //saving new contact info

                    phoneContactInfoModel.setApproved(Boolean.TRUE);
                    getModelService().save(phoneContactInfoModel);

                    B2BCustomerModel b2BCustomerModel = (B2BCustomerModel)phoneContactInfoModel.getUser();
                    b2BCustomerModel.setMobileNumber(phoneContactInfoModel.getPhoneNumber());
                    getModelService().save(b2BCustomerModel);

                }
            }
        }

    }

    @Override
    public ModelService getModelService() {
        return modelService;
    }

    @Override
    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }
}
