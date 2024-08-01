/*
 *
 *  *  * Copyright (c) SCL. All rights reserved.
 *
 */

package com.scl.core.job;

import com.scl.core.event.SclDealerRetailerMappingEvent;
import com.scl.core.model.DealerRetailerMappingModel;
import com.scl.core.services.SclDealerRetailerService;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.event.EventService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;

import java.util.List;


public class SclSendDealerRetailerMappingToCpiJob extends AbstractJobPerformable<CronJobModel>
{

	private static final Logger LOG = Logger.getLogger(SclSendDealerRetailerMappingToCpiJob.class);

	private EventService eventService;

	private SclDealerRetailerService sclDealerRetailerService;



	@Override
	public PerformResult perform(final CronJobModel cronJobModel)
	{
		if (clearAbortRequestedIfNeeded(cronJobModel))
		{
			LOG.info("Job aborted manually !");
			return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
		}

		final List<DealerRetailerMappingModel> dealerRetailerMappingList= getSclDealerRetailerService().getDealerRetailerMappingList();
		if(CollectionUtils.isNotEmpty(dealerRetailerMappingList)) {
			for (DealerRetailerMappingModel mappingModel :dealerRetailerMappingList) {
				getEventService().publishEvent(new SclDealerRetailerMappingEvent(mappingModel));
			}

		}

		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}


	@Override
	public boolean isAbortable()
	{
		return true;
	}


	public SclDealerRetailerService getSclDealerRetailerService() {
		return sclDealerRetailerService;
	}

	public void setSclDealerRetailerService(SclDealerRetailerService sclDealerRetailerService) {
		this.sclDealerRetailerService = sclDealerRetailerService;
	}

	public EventService getEventService() {
		return eventService;
	}

	public void setEventService(EventService eventService) {
		this.eventService = eventService;
	}
}
