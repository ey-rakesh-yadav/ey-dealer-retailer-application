/*
 *
 *  *  * Copyright (c) SCL. All rights reserved.
 *
 */

package com.scl.core.job;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.DealerDao;
import com.scl.core.dao.SclDealerRetailerDao;
import com.scl.core.dao.SclUserDao;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.SclUserModel;
import com.scl.core.model.TerritoryMasterModel;
import com.scl.core.model.UserSubAreaMappingModel;
import de.hybris.platform.cms2.servicelayer.services.admin.CMSAdminSiteService;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;


public class PopulateUserSubareaMappingJob extends AbstractJobPerformable<CronJobModel>
{

	private static final Logger LOG = Logger.getLogger(PopulateUserSubareaMappingJob.class);

	private EventService eventService;

	private SclUserDao sclUserDao;

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

		List<SclUserModel> userList = getSclUserDao().getUserListForEmptyUserSubArea();
		if(Objects.nonNull(userList)){
			for(SclUserModel user:userList){
				if(Objects.nonNull(user.getTerritoryMaster())) {
					for(TerritoryMasterModel territoryMasterModel:user.getTerritoryMaster()) {
						List<SclCustomerModel> dealerList = dealerDao.getDealerFromTerritoryCode(territoryMasterModel);
						for (SclCustomerModel dealer : dealerList) {
							try {
								if(Objects.nonNull(dealer.getTerritoryCode()) && Objects.nonNull(dealer.getTerritoryCode().getSclUser())
										&& Objects.nonNull(dealer.getSubAreaMaster() )&& Objects.nonNull(dealer.getDistrict()) && Objects.nonNull(dealer.getState())) {
									UserSubAreaMappingModel userSubAreaMappingModel = new UserSubAreaMappingModel();
									userSubAreaMappingModel.setSclUser(user);
									userSubAreaMappingModel.setSubAreaMaster(dealer.getSubAreaMaster());
									userSubAreaMappingModel.setDistrict(dealer.getDistrict());
									userSubAreaMappingModel.setBrand(getCmsAdminSiteService().getSiteForId(SclCoreConstants.SCL_SITE));
									userSubAreaMappingModel.setState(dealer.getState());
									userSubAreaMappingModel.setSubArea(dealer.getTaluka());
									userSubAreaMappingModel.setRegionMaster(Objects.nonNull(dealer.getRegionMaster()) ? dealer.getRegionMaster() : null);
									modelService.save(userSubAreaMappingModel);

									user.setUserSubAreaMapping(userSubAreaMappingModel);
									modelService.save(user);
								}

								// userSubAreaMappingRetailer for retailers under dealer
								List<SclCustomerModel> retailerList = sclDealerRetailerDao.getRetailerMappingListForDealer(dealer);
								if (CollectionUtils.isNotEmpty(retailerList)) {
									for (SclCustomerModel retailer : retailerList) {

										if (Objects.nonNull(retailer.getSubAreaMaster()) && StringUtils.isNotEmpty(retailer.getDistrict()) && StringUtils.isNotEmpty(retailer.getState()) && Objects.isNull(sclDealerRetailerDao.getUserSubAreaMappingModelModel(dealer.getTerritoryCode().getSclUser(), retailer.getSubAreaMaster(), retailer.getDistrict(), retailer.getState(), retailer.getTaluka()))) {
											UserSubAreaMappingModel userSubAreaMappingRetailer = new UserSubAreaMappingModel();
											userSubAreaMappingRetailer.setSclUser(user);
											userSubAreaMappingRetailer.setSubAreaMaster(retailer.getSubAreaMaster());
											userSubAreaMappingRetailer.setDistrict(retailer.getDistrict());
											userSubAreaMappingRetailer.setBrand(getCmsAdminSiteService().getSiteForId(SclCoreConstants.SCL_SITE));
											userSubAreaMappingRetailer.setState(retailer.getState());
											userSubAreaMappingRetailer.setSubArea(retailer.getTaluka());
											userSubAreaMappingRetailer.setRegionMaster(Objects.nonNull(retailer.getRegionMaster()) ? retailer.getRegionMaster() : null);
											modelService.save(userSubAreaMappingRetailer);
										}
									}
								}

							} catch (ModelSavingException e) {
								LOG.info(String.format("UserSubAreaMappingModel is already present for User  %s and  district :- %s",user.getUid(), dealer.getDistrict()));
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
