package com.scl.core.job;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.*;
import com.scl.core.enums.CounterType;
import com.scl.core.model.*;
import com.scl.core.region.dao.GeographicalRegionDao;
import com.scl.core.services.SclDealerRetailerService;
import de.hybris.platform.b2b.company.B2BCommerceUnitService;
import de.hybris.platform.cms2.servicelayer.services.admin.CMSAdminSiteService;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.exceptions.ClassMismatchException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SclGeographicalMasterUpdateJob extends AbstractJobPerformable<CronJobModel> {


    public static final String FIND_SCLGEO_UPDATE_JOBDAYS = "FIND_SCLGEO_UPDATE_JOBDAYS";

    @Resource(name = "modelService")
    private ModelService modelService;


    @Resource(name = "b2bCommerceUnitService")
    private B2BCommerceUnitService b2bCommerceUnitService;


    @Resource(name = "sclDealerRetailerService")
    private SclDealerRetailerService sclDealerRetailerService;

    @Resource
    DataConstraintDao dataConstraintDao;

    @Resource
    private GeographicalRegionDao geographicalRegionDao;


    @Resource
    TerritoryMasterDao territoryMasterDao;


    private static final Logger LOG = Logger.getLogger(SclGeographicalMasterUpdateJob.class);

    @Override
    public PerformResult perform(CronJobModel arg0) {


        Date xOldDate=null;
        Integer lastXDays = dataConstraintDao.findDaysByConstraintName(FIND_SCLGEO_UPDATE_JOBDAYS);


        if(lastXDays>0) {
            LocalDate currentDate = LocalDate.now();
            LocalDate last6MonthsDate = currentDate.minusDays(lastXDays);

            xOldDate = Date.from(last6MonthsDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        }

        List<GeographicalMasterModel> geoMasterList =sclDealerRetailerService.getSclGeoMasterList( xOldDate);
        if(CollectionUtils.isEmpty(geoMasterList) ){
            LOG.info("There are no  geoMaster  models");
            return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
        }
        else {
            for(GeographicalMasterModel geoMaster: geoMasterList) {

                try {

                  List<AddressModel> addresses=sclDealerRetailerService.getSclAddressByGeoMaster(geoMaster.getTransportationZone());

                  if (CollectionUtils.isNotEmpty(addresses)){
                      for (AddressModel add:addresses) {

                          if (add.getOwner() instanceof SclCustomerModel) {

                              add.setGeographicalMaster(geoMaster);

                              if (StringUtils.isNotEmpty(add.getSapAddressUsage()) && add.getSapAddressUsage().equalsIgnoreCase("DE")) {
                                  add.setState(geoMaster.getState());
                                  add.setDistrict(geoMaster.getDistrict());
                                  add.setTaluka(StringUtils.isNotEmpty(geoMaster.getTaluka()) ? geoMaster.getTaluka() : StringUtils.EMPTY);
                                  add.setPostalcode(StringUtils.isNotEmpty(geoMaster.getPincode()) ? geoMaster.getPincode() : StringUtils.EMPTY);
                                  add.setErpCity(add.getGeographicalMaster().getErpCity());

                                  SclCustomerModel sclCust = (SclCustomerModel) add.getOwner();
                                  sclCust.setState(geoMaster.getState());
                                  sclCust.setDistrict(geoMaster.getDistrict());
                                  sclCust.setTaluka(StringUtils.isNotEmpty(geoMaster.getTaluka()) ? geoMaster.getTaluka() : StringUtils.EMPTY);
                                  sclCust.setRegionMaster(territoryMasterDao.getRegionMaster(geoMaster.getRegion()));
                                  sclCust.setDistrictMaster(territoryMasterDao.getDistrictMaster(geoMaster.getDistrictCode()));
                                  sclCust.setModifiedtime(new Date());
                                     modelService.save(sclCust);
                                     modelService.refresh(sclCust);

                              } else if (StringUtils.isNotEmpty(add.getSapAddressUsage()) && add.getSapAddressUsage().equalsIgnoreCase("WE")) {
                                  add.setState(geoMaster.getState());
                                  add.setDistrict(geoMaster.getDistrict());
                                  add.setErpCity(add.getGeographicalMaster().getErpCity());
                                  add.setTaluka(StringUtils.isNotEmpty(geoMaster.getTaluka()) ? geoMaster.getTaluka() : StringUtils.EMPTY);
                                  add.setPostalcode(StringUtils.isNotEmpty(geoMaster.getPincode()) ? geoMaster.getPincode() : StringUtils.EMPTY);
                                  
                                  SclCustomerModel sclCust = (SclCustomerModel) add.getOwner();
                                  sclCust.setModifiedtime(new Date());
                                  modelService.save(sclCust);
                                  modelService.refresh(sclCust);
                              }


                              modelService.save(add);
                              modelService.refresh(add);


                          }
                      }
                  }



                }catch (RuntimeException e){
                    LOG.info("SclGeographicalMasterUpdateJob transportationzone:"+  geoMaster.getTransportationZone() + " ExceptionL: "+ e.getMessage());
                    e.printStackTrace();
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




}
