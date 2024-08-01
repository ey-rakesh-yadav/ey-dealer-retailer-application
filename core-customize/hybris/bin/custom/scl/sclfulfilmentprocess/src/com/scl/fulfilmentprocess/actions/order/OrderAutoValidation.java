package com.scl.fulfilmentprocess.actions.order;


import com.scl.core.order.services.OrderValidationProcessService;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.task.RetryLaterException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class OrderAutoValidation extends AbstractSimpleDecisionAction<OrderProcessModel> {

	private static final Logger LOG = Logger.getLogger(OrderAutoValidation.class);

	@Autowired
	OrderValidationProcessService orderValidationProcessService;

	@Override
	public Transition executeAction(OrderProcessModel process) throws RetryLaterException, Exception {

		if (process.getOrder() == null)
		{
			LOG.error("Missing the order, exiting the process");
			return Transition.NOK;
		}
		else {
			boolean result = orderValidationProcessService.validateOrder(process.getOrder());
			if(result)
				return Transition.OK;
			else 
				return Transition.NOK;
		}
	}

}
