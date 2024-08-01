/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.scl.core.actions.prosdealer;

import com.scl.core.constants.SclCoreConstants;
import de.hybris.platform.b2b.process.approval.actions.B2BAbstractWorkflowAutomatedAction;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.processengine.constants.ProcessengineConstants;
import de.hybris.platform.processengine.helpers.ProcessParameterHelper;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.workflow.model.WorkflowActionModel;
import org.springframework.beans.factory.annotation.Required;


public class AfterSOApprovalWorkflowAction extends B2BAbstractWorkflowAutomatedAction
{
	private ProcessParameterHelper processParameterHelper;
	private BusinessProcessService businessProcessService;

	@Override
	public void performAction(final WorkflowActionModel action)
	{

		for (final ItemModel attachment : action.getAttachmentItems())
		{
			if (attachment instanceof BusinessProcessModel)
			{
				final BusinessProcessModel process = (BusinessProcessModel) attachment;
				final String eventName = (String) this.getProcessParameterHelper()
						.getProcessParameterByName(process, SclCoreConstants.PROCESSING_CONSTANT.EVENT_AFTER_SO_APPROVAL_PARAM_NAME).getValue();
				if (eventName != null)
				{
					this.getBusinessProcessService().triggerEvent(process.getCode() + "_" + eventName);
				}
			}
		}
	}

	protected ProcessParameterHelper getProcessParameterHelper()
	{
		return processParameterHelper;
	}

	@Required
	public void setProcessParameterHelper(final ProcessParameterHelper processParameterHelper)
	{
		this.processParameterHelper = processParameterHelper;
	}

	protected BusinessProcessService getBusinessProcessService()
	{
		return businessProcessService;
	}

	@Required
	public void setBusinessProcessService(final BusinessProcessService businessProcessService)
	{
		this.businessProcessService = businessProcessService;
	}
}
