package com.eydms.facades.order.impl;

import com.eydms.core.customer.services.EyDmsCustomerService;
import com.eydms.core.model.DealerDriverDetailsModel;
import com.eydms.core.model.DealerVehicleDetailsModel;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.order.dao.DealerDriverDetailsDao;
import com.eydms.core.order.dao.DealerVehicleDetailsDao;
import com.eydms.core.order.services.DealerTransitService;
import com.eydms.facades.data.order.vehicle.DealerDriverDetailsData;
import com.eydms.facades.data.order.vehicle.DealerDriverDetailsListData;
import com.eydms.facades.data.order.vehicle.DealerVehicleDetailsData;
import com.eydms.facades.data.order.vehicle.DealerVehicleDetailsListData;
import com.eydms.facades.order.DealerTransitFacade;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;
import de.hybris.platform.webservicescommons.dto.error.ErrorWsDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DefaultDealerTransitFacade implements DealerTransitFacade {

    @Resource
    private DealerTransitService dealerTransitService;

    @Resource
    private ModelService modelService;

    @Resource
    private EyDmsCustomerService eydmsCustomerService;

    @Resource
    private Converter<DealerVehicleDetailsModel, DealerVehicleDetailsData> dealerVehicleDetailsConverter;

    @Resource
    private Converter<DealerDriverDetailsModel, DealerDriverDetailsData> dealerDriverDetailsConverter;

    @Resource
    private Populator<DealerVehicleDetailsData,DealerVehicleDetailsModel> dealerVehicleDetailsReversePopulator;

    @Resource
    private Populator<DealerDriverDetailsData, DealerDriverDetailsModel> dealerDriverDetailsReversePopulator;

    @Resource
    private Converter<UserModel, CustomerData> customerConverter;

    @Resource
    private DealerVehicleDetailsDao dealerVehicleDetailsDao;

    @Resource
    private DealerDriverDetailsDao dealerDriverDetailsDao;

    private static final String VEHICLE_EXISTING_ERROR_MESSAGE = "Vehicle already present";

    private static final String DRIVER_EXISTING_ERROR_MESSAGE = "Driver already present";

    @Override
    public DealerVehicleDetailsListData getDealerVehicleDetails(final String dealerUid){
        final EyDmsCustomerModel eydmsCustomer = eydmsCustomerService.getEyDmsCustomerForUid(dealerUid);
        BigDecimal totalCapacity = BigDecimal.ZERO;
        List<DealerVehicleDetailsModel> dealerVehicleDetailsModelList = dealerTransitService.fetchVehicleDetailsForDealer(eydmsCustomer);
        List<DealerVehicleDetailsData> dealerVehicleDetailsDataList = new ArrayList<>();
        DealerVehicleDetailsListData dealerVehicleDetailsList = new DealerVehicleDetailsListData();
        if(CollectionUtils.isNotEmpty(dealerVehicleDetailsModelList)){
            for(DealerVehicleDetailsModel dealerVehicleDetailsModel : dealerVehicleDetailsModelList){
                totalCapacity = totalCapacity.add(BigDecimal.valueOf(dealerVehicleDetailsModel.getCapacity()));
                final DealerVehicleDetailsData dealerVehicleDetailsData = new DealerVehicleDetailsData();
                dealerVehicleDetailsConverter.convert(dealerVehicleDetailsModel,dealerVehicleDetailsData);
                dealerVehicleDetailsDataList.add(dealerVehicleDetailsData);
            }
        }
        final CustomerData customerData = new CustomerData();
        customerConverter.convert(eydmsCustomer,customerData);

        dealerVehicleDetailsList.setVehicleDetails(dealerVehicleDetailsDataList);
        dealerVehicleDetailsList.setDealer(customerData);
        dealerVehicleDetailsList.setFleetCount(String.valueOf(dealerVehicleDetailsDataList.size()));
        dealerVehicleDetailsList.setTotalCapacity(totalCapacity.stripTrailingZeros().toPlainString());
        return dealerVehicleDetailsList;

    }

    @Override
    public DealerDriverDetailsListData getDealerDriverDetails(final String dealerUid){
        final EyDmsCustomerModel eydmsCustomer = eydmsCustomerService.getEyDmsCustomerForUid(dealerUid);
        List<DealerDriverDetailsModel> dealerDriverDetailsModelList = dealerTransitService.fetchDriverDetailsForDealer(eydmsCustomer);
        List<DealerDriverDetailsData> dealerDriverDetailsDataList = new ArrayList<>();
        DealerDriverDetailsListData dealerDriverDetailsList = new DealerDriverDetailsListData();
        if(CollectionUtils.isNotEmpty(dealerDriverDetailsModelList)){
            for(DealerDriverDetailsModel dealerDriverDetailsModel : dealerDriverDetailsModelList){
                final DealerDriverDetailsData dealerDriverDetailsData = new DealerDriverDetailsData();
                dealerDriverDetailsConverter.convert(dealerDriverDetailsModel,dealerDriverDetailsData);
                dealerDriverDetailsDataList.add(dealerDriverDetailsData);
            }
        }
        final CustomerData customerData = new CustomerData();
        customerConverter.convert(eydmsCustomer,customerData);

        dealerDriverDetailsList.setDriverDetails(dealerDriverDetailsDataList);
        dealerDriverDetailsList.setDealer(customerData);
        dealerDriverDetailsList.setNoOfDrivers(String.valueOf(dealerDriverDetailsDataList.size()));
        return dealerDriverDetailsList;
    }

    @Override
    public ErrorListWsDTO createDealerVehicleDetails(final DealerVehicleDetailsListData dealerVehicleDetailsListData, final String dealerUid){
        List<DealerVehicleDetailsModel> dealerVehicleDetailsModelList =  new ArrayList<>();
        List<DealerVehicleDetailsData> dealerVehicleDetailsDataList = dealerVehicleDetailsListData.getVehicleDetails();
        final ErrorListWsDTO errorListWsDTO = new ErrorListWsDTO();
        final List<ErrorWsDTO> errorWsDTOList = new ArrayList<>();

        for(DealerVehicleDetailsData dealerVehicleDetailsData : dealerVehicleDetailsDataList){
            /*if(StringUtils.isBlank(dealerVehicleDetailsData.getVehicleNumber()) || dealerTransitService.isVehicleExisting(dealerVehicleDetailsData.getVehicleNumber())){
                ErrorWsDTO error = getError(dealerVehicleDetailsData.getVehicleNumber(),VEHICLE_EXISTING_ERROR_MESSAGE, AmbiguousIdentifierException.class.getName());
                errorWsDTOList.add(error);
                continue;
            }*/
            DealerVehicleDetailsModel dealerVehicleDetailsModel = dealerVehicleDetailsDao.findVehicleDetailsByVehicleNumber(dealerVehicleDetailsData.getVehicleNumber());
            if(dealerVehicleDetailsModel!=null)
            {
                dealerVehicleDetailsReversePopulator.populate(dealerVehicleDetailsData,dealerVehicleDetailsModel);
                dealerVehicleDetailsModelList.add(dealerVehicleDetailsModel);
            }
            else
            {
                dealerVehicleDetailsModel=modelService.create(DealerVehicleDetailsModel.class);
                dealerVehicleDetailsReversePopulator.populate(dealerVehicleDetailsData,dealerVehicleDetailsModel);
                dealerVehicleDetailsModelList.add(dealerVehicleDetailsModel);
            }
        }
        dealerTransitService.saveVehicleDetailsForDealer(dealerVehicleDetailsModelList,dealerUid);
        errorListWsDTO.setErrors(errorWsDTOList);
        return errorListWsDTO;
    }

    @Override
    public ErrorListWsDTO createDealerDriverDetails(final DealerDriverDetailsListData dealerDriverDetailsListData, final String dealerUid){

        List<DealerDriverDetailsModel> dealerDriverDetailsModelList =  new ArrayList<>();
        List<DealerDriverDetailsData> dealerDriverDetailsDataList = dealerDriverDetailsListData.getDriverDetails();

        final ErrorListWsDTO errorListWsDTO = new ErrorListWsDTO();
        final List<ErrorWsDTO> errorWsDTOList = new ArrayList<>();

        for(DealerDriverDetailsData dealerDriverDetailsData : dealerDriverDetailsDataList){
            /*if(StringUtils.isBlank(dealerDriverDetailsData.getContactNumber()) || dealerTransitService.isDriverExisting(dealerDriverDetailsData.getContactNumber())){
                ErrorWsDTO error = getError(dealerDriverDetailsData.getContactNumber(),DRIVER_EXISTING_ERROR_MESSAGE, AmbiguousIdentifierException.class.getName());
                errorWsDTOList.add(error);
                continue;
            }*/
            DealerDriverDetailsModel dealerDriverDetailsModel = dealerDriverDetailsDao.findDriverDetailsByContactNumber(dealerDriverDetailsData.getContactNumber());
            if(dealerDriverDetailsModel!=null)
            {
                dealerDriverDetailsReversePopulator.populate(dealerDriverDetailsData,dealerDriverDetailsModel);
                dealerDriverDetailsModelList.add(dealerDriverDetailsModel);
            }
            else
            {
                dealerDriverDetailsModel = modelService.create(DealerDriverDetailsModel.class);
                dealerDriverDetailsReversePopulator.populate(dealerDriverDetailsData,dealerDriverDetailsModel);
                dealerDriverDetailsModelList.add(dealerDriverDetailsModel);
            }
        }
        dealerTransitService.saveDriverDetailsForDealer(dealerDriverDetailsModelList,dealerUid);

        errorListWsDTO.setErrors(errorWsDTOList);
        return errorListWsDTO;
    }

    @Override
    public ErrorWsDTO removeVehicle(final String vehicleNumber){
        return dealerTransitService.removeVehicle(vehicleNumber);
    }

    @Override
    public ErrorWsDTO removeDriver(final String contactNumber){
        return dealerTransitService.removeDriver(contactNumber);
    }

    private ErrorWsDTO getError(final String code, final String reason, final String type) {
        ErrorWsDTO errorWsDTO = new ErrorWsDTO();
        errorWsDTO.setReason(reason);
        errorWsDTO.setType(type);
        errorWsDTO.setErrorCode(code);
        return errorWsDTO;
    }

}
