package com.scl.fulfilmentprocess.actions.order;


import com.scl.core.order.services.OrderValidationProcessService;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.action.AbstractAction;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.task.RetryLaterException;
import org.apache.log4j.Logger;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;

public class CheckOrderReviewStatus<T extends OrderProcessModel> extends AbstractOrderAction<T> {

	private static final Logger LOG = Logger.getLogger(CheckOrderReviewStatus.class);

	public Transition executeAction(OrderProcessModel process) throws RetryLaterException, Exception {

		if (OrderStatus.APPROVED.equals(process.getOrder().getStatus())) {
			return Transition.OK;
		} else if (OrderStatus.ORDER_MODIFIED.equals(process.getOrder().getStatus())) {
			return Transition.MODIFY;
		}
		else if (OrderStatus.ORDER_FAILED_VALIDATION.equals(process.getOrder().getStatus())) {
				return Transition.MODIFY;
		}
		else if(process.getOrder().getTotalPrice()!=null && process.getOrder().getTotalPrice()>0) {
			return Transition.MODIFY;
		}
		else {
			return Transition.NOK;
		}
	}

	@Override
	public String execute(OrderProcessModel orderProcessModel) throws RetryLaterException, Exception {
		return executeAction(orderProcessModel).toString();
	}

	@Override
	public Set<String> getTransitions() {
		return CheckOrderReviewStatus.Transition.getStringValues();
	}

	public enum Transition
	{
		OK, NOK, MODIFY;

		public static Set<String> getStringValues()
		{
			final Set<String> res = new HashSet<String>();
			for (final CheckOrderReviewStatus.Transition transitions : CheckOrderReviewStatus.Transition.values())
			{
				res.add(transitions.toString());
			}
			return res;
		}
	}
}
