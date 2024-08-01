/*
 *
 *  *  * Copyright (c) SCL. All rights reserved.
 *
 */

package com.scl.core.job;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.scl.core.dao.DealerDao;
import com.scl.core.dao.SclDealerRetailerDao;
import com.scl.core.dao.SclUserDao;
import com.scl.core.model.UserSubAreaMappingModel;
import com.scl.core.region.dao.GeographicalRegionDao;

import de.hybris.platform.cms2.servicelayer.services.admin.CMSAdminSiteService;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.event.EventService;


public class CleanUpUserSubareaMappingJob extends AbstractJobPerformable<CronJobModel>
{

	private static final Logger LOG = Logger.getLogger(CleanUpUserSubareaMappingJob.class);

	private EventService eventService;

	private SclUserDao sclUserDao;

	@Resource
	private GeographicalRegionDao geographicalRegionDao;

	private DealerDao dealerDao;

	private CMSAdminSiteService cmsAdminSiteService;

	@Resource
	SclDealerRetailerDao sclDealerRetailerDao;


	@Override
	public PerformResult perform(final CronJobModel cronJobModel)
	{
		if (clearAbortRequestedIfNeeded(cronJobModel))
		{
			LOG.info("Job aborted manually !");
			return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
		}

		List<UserSubAreaMappingModel> sclUsers = getSclUserDao().getUserSubareaListForUpdatedByJob();

		if(CollectionUtils.isNotEmpty(sclUsers) ){
			for(UserSubAreaMappingModel user:sclUsers) {
				user.setIsActive(false);
				modelService.save(user);
				LOG.info(String.format("SclUser :: %, for district :: %s, taluka :: %s, to be deactivated!", user.getSclUser().getUid(),user.getDistrict(),user.getSubArea()));
			}
		}
		else {
			LOG.info("No Scluser found for deactivation!! ");
		}


		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}


	@Override
	public boolean isAbortable()
	{
		return true;
	}

	public EventService getEventService() {
		return eventService;
	}

	public void setEventService(EventService eventService) {
		this.eventService = eventService;
	}

	public SclUserDao getSclUserDao() {
		return sclUserDao;
	}

	public void setSclUserDao(SclUserDao sclUserDao) {
		this.sclUserDao = sclUserDao;
	}

	public DealerDao getDealerDao() {
		return dealerDao;
	}

	public void setDealerDao(DealerDao dealerDao) {
		this.dealerDao = dealerDao;
	}

	public CMSAdminSiteService getCmsAdminSiteService() {
		return cmsAdminSiteService;
	}

	public void setCmsAdminSiteService(CMSAdminSiteService cmsAdminSiteService) {
		this.cmsAdminSiteService = cmsAdminSiteService;
	}


}
