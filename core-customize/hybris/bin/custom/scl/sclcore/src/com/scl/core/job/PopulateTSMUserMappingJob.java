/*
 *
 *  *  * Copyright (c) SCL. All rights reserved.
 *
 */

package com.scl.core.job;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.DataConstraintDao;
import com.scl.core.dao.DealerDao;
import com.scl.core.dao.SclUserDao;
import com.scl.core.model.*;
import de.hybris.platform.cms2.servicelayer.services.admin.CMSAdminSiteService;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;


public class PopulateTSMUserMappingJob extends AbstractJobPerformable<CronJobModel>
{

	public static final String TSM = "TSM";
	public static final String FIND_SCLUSES_XOLD_DAYS = "FIND_SCLUSES_XOLD_DAYS";
	private static final Logger LOG = Logger.getLogger(PopulateTSMUserMappingJob.class);


	private SclUserDao sclUserDao;

	private DealerDao dealerDao;

	private CMSAdminSiteService cmsAdminSiteService;

	private ConfigurationService configurationService;

	private DataConstraintDao dataConstraintDao;



	@Override
	public PerformResult perform(final CronJobModel cronJobModel)
	{
		if (clearAbortRequestedIfNeeded(cronJobModel))
		{
			LOG.info("tsmDistrictMappingModel Job aborted manually !");
			return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
		}

		Date xOldDate=null;


		Integer lastXDays = dataConstraintDao.findDaysByConstraintName(FIND_SCLUSES_XOLD_DAYS);


		  if(lastXDays>0) {
			LocalDate currentDate = LocalDate.now();
			LocalDate last6MonthsDate = currentDate.minusDays(lastXDays);

			xOldDate = Date.from(last6MonthsDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

		}

		List<SclUserModel> userList = getSclUserDao().getSCLUserListBasedOnType(TSM, xOldDate);
		if(Objects.nonNull(userList)){
			for(SclUserModel user:userList){
				if(Objects.nonNull(user.getTerritoryMaster())) {
					for(TerritoryMasterModel territoryMasterModel:user.getTerritoryMaster()) {
						List<SclCustomerModel> dealerList = dealerDao.getDealerFromTerritoryCode(territoryMasterModel);
						for (SclCustomerModel dealer : dealerList) {
							try {
								TsmDistrictMappingModel tsmDistrictMappingModel = new TsmDistrictMappingModel();

								tsmDistrictMappingModel.setTsmUser(user);
								tsmDistrictMappingModel.setIsActive(true);
								tsmDistrictMappingModel.setDistrict(dealer.getDistrictMaster());
								tsmDistrictMappingModel.setBrand(getCmsAdminSiteService().getSiteForId(SclCoreConstants.SCL_SITE));
								modelService.save(tsmDistrictMappingModel);


							} catch (ModelSavingException e) {
								LOG.info(String.format("tsmDistrictMappingModel is exception for User  %s and  district :- %s   e.message:- %s",user.getUid(), dealer.getDistrict(), e.getMessage()));
								e.printStackTrace();
							}
						}
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

	public ConfigurationService getConfigurationService() {
		return configurationService;
	}

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	public DataConstraintDao getDataConstraintDao() {
		return dataConstraintDao;
	}

	public void setDataConstraintDao(DataConstraintDao dataConstraintDao) {
		this.dataConstraintDao = dataConstraintDao;
	}
}
