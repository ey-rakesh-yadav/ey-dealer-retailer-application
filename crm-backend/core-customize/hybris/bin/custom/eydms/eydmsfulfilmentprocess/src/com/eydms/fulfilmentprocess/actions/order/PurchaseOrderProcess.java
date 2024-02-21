package com.eydms.fulfilmentprocess.actions.order;


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.order.services.OrderValidationProcessService;

import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.task.RetryLaterException;

public class PurchaseOrderProcess extends AbstractSimpleDecisionAction<OrderProcessModel> {

	private static final Logger LOG = Logger.getLogger(PurchaseOrderProcess.class);

	@Autowired
	OrderValidationProcessService orderValidationProcessService;

	@Override
	public Transition executeAction(OrderProcessModel process) throws RetryLaterException, Exception {
		return Transition.OK;
	}

}
