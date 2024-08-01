package com.scl.fulfilmentprocess.actions.order;


import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.scl.core.enums.CRMOrderType;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.task.RetryLaterException;

public class IdentifyOrderNextStep<T extends OrderProcessModel> extends AbstractOrderAction<T> {

	private static final Logger LOG = Logger.getLogger(IdentifyOrderNextStep.class);

	public Transition executeAction(OrderProcessModel process) throws RetryLaterException, Exception {
		OrderModel order = process.getOrder();
		if(order==null)
			return Transition.NOK;
		if (CRMOrderType.GIFT.equals(order.getCrmOrderType())) {
			return Transition.GIFT;
		} else if (CRMOrderType.PURCHASE.equals(order.getCrmOrderType())) {
			return Transition.PURCHASE;
		}
		else {
			return Transition.TRADE;
		}
	}

	@Override
	public String execute(OrderProcessModel orderProcessModel) throws RetryLaterException, Exception {
		return executeAction(orderProcessModel).toString();
	}

	@Override
	public Set<String> getTransitions() {
		return IdentifyOrderNextStep.Transition.getStringValues();
	}

	public enum Transition
	{
		GIFT, PURCHASE, TRADE, NOK;

		public static Set<String> getStringValues()
		{
			final Set<String> res = new HashSet<String>();
			for (final IdentifyOrderNextStep.Transition transitions : IdentifyOrderNextStep.Transition.values())
			{
				res.add(transitions.toString());
			}
			return res;
		}
	}
}
