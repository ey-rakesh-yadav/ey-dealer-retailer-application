package com.eydms.core.order.services.impl;

import com.eydms.core.customer.services.EyDmsCustomerService;
import com.eydms.core.model.DealerDriverDetailsModel;
import com.eydms.core.model.DealerVehicleDetailsModel;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.order.dao.DealerDriverDetailsDao;
import com.eydms.core.order.dao.DealerVehicleDetailsDao;
import com.eydms.core.order.services.DealerTransitService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.webservicescommons.dto.error.ErrorWsDTO;
import org.apache.commons.collections.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

public class DefaultDealerTransitService implements DealerTransitService {

    @Resource
    private DealerVehicleDetailsDao dealerVehicleDetailsDao;

    @Resource
    private DealerDriverDetailsDao dealerDriverDetailsDao;

    @Resource
    private EyDmsCustomerService eydmsCustomerService;

    @Resource
    private ModelService modelService;

    private static final String VEHICLE_NOT_PRESENT_ERROR_MESSAGE = "Vehicle Not Present with Vehicle Number : %s";
    private static final String DRIVER_NOT_PRESENT_ERROR_MESSAGE = "Driver Not Present with Contact Number : %s";

    @Override
    public boolean isVehicleExisting(String vehicleNumber) {
        DealerVehicleDetailsModel dealerVehicleDetailsModel = dealerVehicleDetailsDao.findVehicleDetailsByVehicleNumber(vehicleNumber);
        if(null!= dealerVehicleDetailsModel){
            return Boolean.TRUE;
        }
        else{
            return Boolean.FALSE;
        }
    }

    @Override
    public boolean isDriverExisting(String contactNumber) {
        DealerDriverDetailsModel dealerDriverDetailsModel = dealerDriverDetailsDao.findDriverDetailsByContactNumber(contactNumber);
        if(null != dealerDriverDetailsModel){
            return Boolean.TRUE;
        }
        else{
            return Boolean.FALSE;
        }
    }

    @Override
    public List<DealerVehicleDetailsModel> fetchVehicleDetailsForDealer(final EyDmsCustomerModel dealer){
            return dealerVehicleDetailsDao.findVehicleDetailsForDealer(dealer);
    }

    @Override
    public List<DealerDriverDetailsModel> fetchDriverDetailsForDealer(final EyDmsCustomerModel dealer){
            return dealerDriverDetailsDao.findDriverDetailsForDealer(dealer);
    }
    @Override
    public void saveVehicleDetailsForDealer(final List<DealerVehicleDetailsModel> dealerVehicleDetailsModelList , final String dealerUid){
        if(CollectionUtils.isNotEmpty(dealerVehicleDetailsModelList)){
            dealerVehicleDetailsModelList.forEach(dealerVehicleDetailsMode -> dealerVehicleDetailsMode.setDealer(eydmsCustomerService.getEyDmsCustomerForUid(dealerUid)));
            modelService.saveAll(dealerVehicleDetailsModelList);
        }
    }

    @Override
    public void saveDriverDetailsForDealer(final List<DealerDriverDetailsModel> dealerDriverDetailsModelList , final String dealerUid){

        if(CollectionUtils.isNotEmpty(dealerDriverDetailsModelList)){
            dealerDriverDetailsModelList.forEach(dealerDriverDetailsModel -> dealerDriverDetailsModel.setDealer(eydmsCustomerService.getEyDmsCustomerForUid(dealerUid)));
            modelService.saveAll(dealerDriverDetailsModelList);
        }
    }

    @Override
    public ErrorWsDTO removeVehicle(final String vehicleNumber){
        DealerVehicleDetailsModel dealerVehicleDetailsModel = dealerVehicleDetailsDao.findVehicleDetailsByVehicleNumber(vehicleNumber);
        if(null == dealerVehicleDetailsModel){
            ErrorWsDTO errorWsDTO = new ErrorWsDTO();
            errorWsDTO.setErrorCode(vehicleNumber);
            errorWsDTO.setReason(String.format(VEHICLE_NOT_PRESENT_ERROR_MESSAGE,vehicleNumber));
            return errorWsDTO;
        }
        else{
            modelService.remove(dealerVehicleDetailsModel);
            return new ErrorWsDTO();
        }
    }

    @Override
    public ErrorWsDTO removeDriver(final String contactNumber){
        DealerDriverDetailsModel dealerDriverDetailsModel = dealerDriverDetailsDao.findDriverDetailsByContactNumber(contactNumber);
        if(null == dealerDriverDetailsModel){
            ErrorWsDTO errorWsDTO = new ErrorWsDTO();
            errorWsDTO.setErrorCode(contactNumber);
            errorWsDTO.setReason(String.format(DRIVER_NOT_PRESENT_ERROR_MESSAGE,contactNumber));
            return errorWsDTO;
        }
        else{
            modelService.remove(dealerDriverDetailsModel);
            return new ErrorWsDTO();
        }
    }

}
