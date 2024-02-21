package com.eydms.fulfilmentprocess.actions.order;


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.enums.GiftType;
import com.eydms.core.enums.TransactionType;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.services.SchemesAndDiscountService;

import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.task.RetryLaterException;

public class GiftOrderProcess extends AbstractSimpleDecisionAction<OrderProcessModel> {

	private static final Logger LOG = Logger.getLogger(GiftOrderProcess.class);

	@Autowired
	SchemesAndDiscountService schemesAndDiscountService;

	@Override
	public Transition executeAction(OrderProcessModel process) throws RetryLaterException, Exception {

		if (process.getOrder() == null)
		{
			LOG.error("Missing the order, exiting the process");
			return Transition.NOK;
		}
		else {
//			double points = process.getOrder().getTotalPrice();
//			if(process.getOrder().getGiftType()!=null && process.getOrder().getGiftType().equals(GiftType.CASH)) {
//				points = process.getOrder().getEntries().get(0).getQuantity();
//			}
//			schemesAndDiscountService.updateInfluencerPoint((EyDmsCustomerModel)process.getOrder().getUser(), points, TransactionType.DEBIT, process.getOrder().getGiftShop());
			boolean result = true;
			if(result)
				return Transition.OK;
			else 
				return Transition.NOK;
		}
	}

}
