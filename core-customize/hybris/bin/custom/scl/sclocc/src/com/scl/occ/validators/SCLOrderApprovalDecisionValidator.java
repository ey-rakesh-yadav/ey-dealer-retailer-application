/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.scl.occ.validators;

import de.hybris.platform.b2b.services.B2BWorkflowIntegrationService;
import de.hybris.platform.b2bwebservicescommons.dto.order.OrderApprovalDecisionWsDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


/**
 * Implementation of {@link Validator} that validate instances of
 * {@link OrderApprovalDecisionWsDTO} for approval decision.
 */
public class SCLOrderApprovalDecisionValidator implements Validator
{
	private static final String FIELD_REQUIRED_ERROR_CODE = "field.required";
	private static final String APPROVER_COMMENTS_REQUIRED_ERROR_CODE = "account.orderApproval.addApproverComments.invalid";
	private static final String APPROVER_TEXT_ERROR_CODE = "account.orderApproval.approval.text.invalid";
	private static final String APPROVAL_DECISION = "APPROVE";

	@Override
	public boolean supports(final Class<?> clazz)
	{
		return OrderApprovalDecisionWsDTO.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(final Object target, final Errors errors)
	{
		final OrderApprovalDecisionWsDTO approval = (OrderApprovalDecisionWsDTO) target;
		Assert.notNull(errors, "Errors object must not be null");

		if (approval.getDecision() == null)
		{
			errors.rejectValue("decision", FIELD_REQUIRED_ERROR_CODE);
		}
		else if (!APPROVAL_DECISION.equals(approval.getDecision()))
		{
			errors.rejectValue("decision", APPROVER_TEXT_ERROR_CODE);
		}
		else if ( StringUtils.isBlank(approval.getComment()))
		{
			errors.rejectValue("comment", APPROVER_COMMENTS_REQUIRED_ERROR_CODE);
		}
	}

}
