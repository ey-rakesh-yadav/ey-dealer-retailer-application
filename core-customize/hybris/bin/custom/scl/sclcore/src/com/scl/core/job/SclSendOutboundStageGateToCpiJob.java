/*
 *
 *  *  * Copyright (c) SCL. All rights reserved.
 *
 */

package com.scl.core.job;

import com.scl.core.dao.SclStageGateDao;
import com.scl.core.event.SclDealerRetailerMappingEvent;
import com.scl.core.event.SclStageGateEvent;
import com.scl.core.model.DealerRetailerMappingModel;
import com.scl.core.model.DeliveryItemModel;
import com.scl.core.model.SclOutboundStageGateProcessModel;
import com.scl.core.services.SclDealerRetailerService;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.event.EventService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;


public class SclSendOutboundStageGateToCpiJob extends AbstractJobPerformable<CronJobModel>
{

	private static final Logger LOG = Logger.getLogger(SclSendOutboundStageGateToCpiJob.class);

	private EventService eventService;


	private SclStageGateDao sclStageGateDao;

	@Override
	public PerformResult perform(final CronJobModel cronJobModel)
	{
		if (clearAbortRequestedIfNeeded(cronJobModel))
		{
			LOG.info("Job aborted manually !");
			return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
		}

		List<OrderModel> orderModelList= getSclStageGateDao().getS4OrdersFoGetCall();//need to find the order
		//not null erp no
		//Accepted status
		//createdFromCRMorERP=s4

		for(OrderModel orderModel:orderModelList){
			for(AbstractOrderEntryModel entryModel:orderModel.getEntries()){
				for(DeliveryItemModel deliveryItemModel:entryModel.getDeliveriesItem()){

					if(null==deliveryItemModel.getTruckDispatchedDateAndTime() && null==deliveryItemModel.getInvoiceCreationDateAndTime()&& null==deliveryItemModel.getTruckAllocatedDate() && null!=deliveryItemModel.getDiCreationDateAndTime()) {
						SclOutboundStageGateProcessModel processModel = new SclOutboundStageGateProcessModel();
						processModel.setDeliveryItem(deliveryItemModel);
						processModel.setOrderEntry((OrderEntryModel) entryModel);
						getEventService().publishEvent(new SclStageGateEvent(processModel));
					}

				}
			}
		}

		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}


	@Override
	public boolean isAbortable()
	{
		return true;
	}

	public SclStageGateDao getSclStageGateDao() {
		return sclStageGateDao;
	}

	public void setSclStageGateDao(SclStageGateDao sclStageGateDao) {
		this.sclStageGateDao = sclStageGateDao;
	}

	public EventService getEventService() {
		return eventService;
	}

	public void setEventService(EventService eventService) {
		this.eventService = eventService;
	}
}
