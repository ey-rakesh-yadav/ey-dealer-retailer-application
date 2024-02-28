package com.eydms.facades.impl;


import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.dao.NetworkDao;
import com.eydms.core.enums.CounterType;
import com.eydms.core.jalo.EyDmsUser;
import com.eydms.core.model.*;
import com.eydms.core.services.NetworkService;
import com.eydms.core.services.PointRequisitionService;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.core.services.impl.NetworkServiceImpl;
import com.eydms.core.utility.EyDmsDateUtility;
import com.eydms.facades.PointRequisitionFacade;
import com.eydms.facades.data.*;
import com.eydms.facades.order.data.TrackingData;

import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.commercefacades.product.data.ImageData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.user.UserService;

import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.*;

import java.text.SimpleDateFormat;

import static java.util.stream.Collectors.groupingBy;

import java.text.DateFormat;
import java.util.stream.Collectors;


public class PointRequisitionFacadeImpl implements PointRequisitionFacade {

    @Resource
    PointRequisitionService pointRequisitionService;

    @Resource
    ModelService modelService;

    @Resource
    private TerritoryManagementService territoryManagementService;

    @Resource
    private NetworkDao networkDao;



    @Resource
    private NetworkService networkService;

    @Autowired
    UserService userService;

    @Autowired
    Converter<EyDmsCustomerModel,CustomerData> dealerBasicConverter;
    
	private Converter<MediaModel, ImageData> imageConverter;

    private static final Logger LOGGER = Logger.getLogger(NetworkServiceImpl.class);

	
    public Converter<MediaModel, ImageData> getImageConverter() {
		return imageConverter;
	}

	public void setImageConverter(Converter<MediaModel, ImageData> imageConverter) {
		this.imageConverter = imageConverter;
	}

	@Autowired
    Populator<AddressModel, AddressData> addressPopulator;
	
    @Autowired
    EnumerationService enumerationService;
    
    @Override
    public String saveInfluencerPointRequisitionDetails(PointRequisitionData pointRequisitionData) {
        return pointRequisitionService.saveInfluencerPointRequisitionDetails(pointRequisitionData);
    }

    @Override
    public Double getPointsForRequisition(String productCode, String influencer) {
        return pointRequisitionService.getPointsForRequisition(productCode,influencer);
    }

    @Override
    public Integer getAllocationRequestCount() {
        return pointRequisitionService.getAllocationRequestCount();
    }

    @Override
    public List<InfluencerPointRequisitionRequestData> getAllocationRequestList() {
        List<InfluencerPointRequisitionRequestData> pointRequisitionRequestDataList = new ArrayList<>();
        List<PendingPointRequisitionData> pointRequisitionDataList = new ArrayList<>();
        List<PointRequisitionModel> allocationRequestList = pointRequisitionService.getAllocationRequestList();
        if(allocationRequestList!=null && !allocationRequestList.isEmpty()) {
            Map<EyDmsCustomerModel, List<PointRequisitionModel>> collect = allocationRequestList.stream().collect(groupingBy(PointRequisitionModel::getRequestRaisedFor));
            if(collect!=null && !collect.isEmpty()) {
                for (Map.Entry<EyDmsCustomerModel, List<PointRequisitionModel>> eydmsCustomerModelListEntry : collect.entrySet()) {
                    EyDmsCustomerModel key = eydmsCustomerModelListEntry.getKey();
                    InfluencerPointRequisitionRequestData requestData = new InfluencerPointRequisitionRequestData();
                    requestData.setName(key.getName());
                    requestData.setCrmCode(key.getUid());
                    requestData.setCustomerNo(key.getCustomerNo());
                    if(key.getInfluencerType()!=null) {
                        if (key.getInfluencerType().getCode() != null) {
                            requestData.setInfluencerType(key.getInfluencerType().getCode());
                        }
                    }
                    if(eydmsCustomerModelListEntry.getValue()!=null && !eydmsCustomerModelListEntry.getValue().isEmpty()) {
                        for (PointRequisitionModel pointRequisitionModel : eydmsCustomerModelListEntry.getValue()) {
                            PendingPointRequisitionData pointRequisitionData = new PendingPointRequisitionData();
                            Date deliveryDate=null,requisitionCreationDate=null;
                            SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
                            if(pointRequisitionModel.getDeliveryDate()!=null) {
                                deliveryDate = pointRequisitionModel.getDeliveryDate();
                                String delDate = formatter.format(deliveryDate);
                                pointRequisitionData.setOrderLiftedOn(delDate);
                            }
                            if(pointRequisitionModel.getRequisitionCreationDate()!=null) {
                                 requisitionCreationDate = pointRequisitionModel.getRequisitionCreationDate();
                                String reqCreDate = formatter.format(requisitionCreationDate);
                                pointRequisitionData.setAllocationRequestDate(reqCreDate);
                            }
                            pointRequisitionData.setQuantityAllocated(String.valueOf(pointRequisitionModel.getQuantity()));
                            if(pointRequisitionModel.getProduct()!=null) {
                                pointRequisitionData.setProductName(pointRequisitionModel.getProduct().getName());
                                pointRequisitionData.setProductCode(pointRequisitionModel.getProduct().getCode());
                            }
                            pointRequisitionData.setRequistionId(pointRequisitionModel.getRequisitionId());
                            pointRequisitionDataList.add(pointRequisitionData);
                        }
                    }
                    requestData.setPendingPointRequisitionData(pointRequisitionDataList);
                    pointRequisitionRequestDataList.add(requestData);
                }
                }
                }
        return pointRequisitionRequestDataList;

    }

    @Override
    public SearchPageData<PointRequisitionData> getListOfAllPointRequisition(boolean isDraft, String filter, List<String> statuses, SearchPageData searchPageData, String requisitionId, String fields, String influencerCode) {
        List<PointRequisitionData> pointRequisitionDataList = new ArrayList<>();
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm a");

        EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
        String counterType = "Other";
        if(currentUser.getGroups()
    			.contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID))) {
        	counterType = "Influencer";
        }
        SearchPageData<PointRequisitionModel> searchResult = pointRequisitionService.getListOfAllPointRequisition(isDraft, filter,statuses,searchPageData, requisitionId, influencerCode);
        if(searchResult!=null && searchResult.getResults()!=null) {
            List<PointRequisitionModel> pointRequisitionModelsList = searchResult.getResults();
            for (PointRequisitionModel pointRequisitionModel : pointRequisitionModelsList) {
                PointRequisitionData pointRequisitionData = new PointRequisitionData();

                pointRequisitionData.setRequisitionId(pointRequisitionModel.getRequisitionId());
                if (pointRequisitionModel.getRequisitionCreationDate() != null) {
                    pointRequisitionData.setRequisitionCreationDate(dateFormat.format(pointRequisitionModel.getRequisitionCreationDate()));
                }
                pointRequisitionData.setClientName(pointRequisitionModel.getClientName());
                
                AddressData addressData = new AddressData();
                addressData.setState(pointRequisitionModel.getState());
                addressData.setDistrict(pointRequisitionModel.getDistrict());
                addressData.setTaluka(pointRequisitionModel.getTaluka());
                addressData.setErpCity(pointRequisitionModel.getCity());
                addressData.setCellphone(pointRequisitionModel.getPhoneNumber());
                addressData.setLine1(pointRequisitionModel.getAddressLine1());
                addressData.setLine2(pointRequisitionModel.getAddressLine2());
                addressData.setPostalCode(pointRequisitionModel.getPincode());
                
                StringBuilder builder = new StringBuilder("");
	            if(pointRequisitionModel.getAddressLine1()!=null)
	            	builder.append(pointRequisitionModel.getAddressLine1()+",");
	            if(pointRequisitionModel.getAddressLine2()!=null)
	            	builder.append(pointRequisitionModel.getAddressLine2()+",");
	            if(pointRequisitionModel.getCity()!=null)
	            	builder.append(pointRequisitionModel.getCity()+",");
	            if(pointRequisitionModel.getTaluka()!=null)
	            	builder.append(pointRequisitionModel.getTaluka()+",");
	            if(pointRequisitionModel.getDistrict()!=null)
	            	builder.append(pointRequisitionModel.getDistrict()+",");
	            if(pointRequisitionModel.getState()!=null)
	            	builder.append(pointRequisitionModel.getState()+",");
	            if(pointRequisitionModel.getPincode()!=null)
	            	builder.append(pointRequisitionModel.getPincode());
                addressData.setFormattedAddress(builder.toString());

                pointRequisitionData.setDeliveryAddress(addressData);
                
                if(pointRequisitionModel.getRequestRaisedTo()!=null) {
                	pointRequisitionData.setRequestRaisedTo(pointRequisitionModel.getRequestRaisedTo().getUid());
                	pointRequisitionData.setRequestRaisedToName(pointRequisitionModel.getRequestRaisedTo().getName());
                	if(pointRequisitionModel.getRequestRaisedTo().getCounterType()!=null)
                		pointRequisitionData.setRequestRaisedToType(pointRequisitionModel.getRequestRaisedTo().getCounterType().getCode());
                	else
                		pointRequisitionData.setRequestRaisedToType("");               	
                }
                if(pointRequisitionModel.getDeliveryDate()!=null) {
                    DateFormat dateFormat1 = new SimpleDateFormat("dd-MM-yyyy");
                	pointRequisitionData.setDeliveryDate(dateFormat1.format(pointRequisitionModel.getDeliveryDate()));
                }
                if(pointRequisitionModel.getDeliverySlot()!=null) {
                    pointRequisitionData.setDeliverySlotName(enumerationService.getEnumerationName(pointRequisitionModel.getDeliverySlot()));
                	pointRequisitionData.setDeliverySlot(pointRequisitionModel.getDeliverySlot().getCode());
                }
                if(pointRequisitionModel.getRequestRaisedFor()!=null) {
                	pointRequisitionData.setRequestRaisedFor(pointRequisitionModel.getRequestRaisedFor().getUid());
                	pointRequisitionData.setRequestRaisedForName(pointRequisitionModel.getRequestRaisedFor().getName());
                }
                if(pointRequisitionModel.getRequestRaisedBy()!=null) {
                	pointRequisitionData.setRequestRaisedBy(pointRequisitionModel.getRequestRaisedBy().getUid());
                	pointRequisitionData.setRequestRaisedByName(pointRequisitionModel.getRequestRaisedBy().getName());
                }
                if(pointRequisitionModel.getProduct()!=null) {
                	pointRequisitionData.setProductCode(pointRequisitionModel.getProduct().getCode());
                	pointRequisitionData.setProductName(pointRequisitionModel.getProduct().getName());
                }
                if(pointRequisitionModel.getQuantity()!=null) {
                	pointRequisitionData.setQuantity(pointRequisitionModel.getQuantity().intValue());
                }
                if(pointRequisitionModel.getStatus()!=null) {
                    pointRequisitionData.setRequestStatus(pointRequisitionModel.getStatus().getCode());
                }
                pointRequisitionData.setPointsEarned(pointRequisitionModel.getPoints());
                pointRequisitionData.setPointsPerBag(pointRequisitionModel.getPointsPerBag());
                pointRequisitionData.setModificationComment(pointRequisitionModel.getModificationComment());
                pointRequisitionData.setRejectionReason(pointRequisitionModel.getRejectionReason());

                MediaModel profilePicture = null;
                if(counterType.equals("Influencer")) {
                	pointRequisitionModel.getRequestRaisedTo().getProfilePicture();
                }
                else {
                	pointRequisitionModel.getRequestRaisedFor().getProfilePicture();
                }
                if(profilePicture!=null) {
                	final ImageData profileImageData = getImageConverter().convert(profilePicture);
                	pointRequisitionData.setProfilePicture(profileImageData);
                }
        		
                List<TrackingData> trackingDataList = new ArrayList<>();

                TrackingData data = new TrackingData();
                data.setName("Delivery Taken");
                data.setActualTime(pointRequisitionModel.getDeliveryDate());
                if(pointRequisitionModel.getDeliverySlot()!=null) {
                    data.setDeliverySlot(pointRequisitionModel.getDeliverySlot().getCode());
                }

                data.setCode(data.getName());
                trackingDataList.add(data);
                String toCustomerCounterType = "Partner";//pointRequisitionModel.getRequestRaisedTo()!=null && pointRequisitionModel.getRequestRaisedTo().getCounterType()!=null
                			//?pointRequisitionModel.getRequestRaisedTo().getCounterType().getCode():"Dealer/Retailer";
                if(pointRequisitionModel.getRequestRaisedFor().equals(pointRequisitionModel.getRequestRaisedBy())){
                    TrackingData data1 = new TrackingData();
                    data1.setName("Requisition Request Placed By Influencer");
                    data1.setActualTime(pointRequisitionModel.getRequisitionCreationDate());
                    data1.setCode(data1.getName());
                    trackingDataList.add(data1);

                    if(pointRequisitionModel.getReqRejectedDate()!=null){
                        TrackingData data2 = new TrackingData();
                        data2.setName("Request Rejected By " + toCustomerCounterType);
                        data2.setActualTime(pointRequisitionModel.getReqRejectedDate());
                        data2.setCode(data2.getName());
                        
                        if(pointRequisitionModel.getRejectionReason()!=null) {
                    		List<String> comments = new ArrayList<String>();
                    		comments.add("Rejection Reason : " + pointRequisitionModel.getRejectionReason());
                    		data2.setComment(comments);
                    	}
                        
                        trackingDataList.add(data2);
                    }
                    else{
                        TrackingData data3 = new TrackingData();
                        data3.setName("Request Confirmed By " + toCustomerCounterType);
                        data3.setActualTime(pointRequisitionModel.getReqApprovedDate());
                        data3.setCode(data3.getName());
                        trackingDataList.add(data3);
                    }
                }
                else{
                    TrackingData data4 = new TrackingData();
                    data4.setName("Requisition Request Placed By " + toCustomerCounterType);
                    data4.setActualTime(pointRequisitionModel.getRequisitionCreationDate());
                    data4.setCode(data4.getName());
                    trackingDataList.add(data4);
                }

                if(pointRequisitionModel.getReqRejectedDate()==null){
                    TrackingData data5 = new TrackingData();
                    data5.setName("Points Awarded");
                    data5.setActualTime(pointRequisitionModel.getReqApprovedDate());
                    data5.setCode(data5.getName());
                    trackingDataList.add(data5);
                }
                pointRequisitionData.setTrackingDetails(trackingDataList);
                
                pointRequisitionDataList.add(pointRequisitionData);

            }

        }
        /* if(searchResult!=null && searchResult.getResults()!=null) {
            List<PointRequisitionData> orderRequisitionModelsList = searchResult.getResults();
            for(PointRequisitionData orderRequisitionModel : orderRequisitionModelsList) {
                OrderRequisitionData orderRequisitionData = new OrderRequisitionData();

                orderRequisitionData.setRequisitionId(orderRequisitionModel.getRequisitionId());
                if(orderRequisitionModel.getRequisitionDate()!=null) {
                    orderRequisitionData.setRequisitionDate(dateFormat.format(orderRequisitionModel.getRequisitionDate()));
                }

                if(currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                    orderRequisitionData.setFromCustomerUid(orderRequisitionModel.getFromCustomer().getUid());
                    orderRequisitionData.setFromCustomerName(orderRequisitionModel.getFromCustomer().getName());
                    orderRequisitionData.setFromCustomerNo(orderRequisitionModel.getFromCustomer().getCustomerNo());
                    orderRequisitionData.setToCustomerUid(currentUser.getUid());
                    orderRequisitionData.setToCustomerName(currentUser.getName());
                    orderRequisitionData.setToCustomerNo(currentUser.getCustomerNo());
                }
                else if(currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                    orderRequisitionData.setFromCustomerUid(currentUser.getUid());
                    orderRequisitionData.setFromCustomerName(currentUser.getName());
                    orderRequisitionData.setFromCustomerNo(currentUser.getCustomerNo());
                    orderRequisitionData.setToCustomerUid(orderRequisitionModel.getToCustomer().getUid());
                    orderRequisitionData.setToCustomerName(orderRequisitionModel.getToCustomer().getName());
                    orderRequisitionData.setToCustomerNo(orderRequisitionModel.getToCustomer().getCustomerNo());
                }
                if(orderRequisitionModel.getDeliveryAddress()!=null) {
                    AddressData address = new AddressData();
                    addressPopulator.populate(orderRequisitionModel.getDeliveryAddress(), address);
                    orderRequisitionData.setDeliveryAddress(address);
                }
                if(orderRequisitionModel.getProduct()!=null) {
                    orderRequisitionData.setProductCode(orderRequisitionModel.getProduct().getCode());
                    orderRequisitionData.setProductName(orderRequisitionModel.getProduct().getName());
                }
                orderRequisitionData.setQuantity(orderRequisitionModel.getQuantity());
                if(orderRequisitionModel.getExpectedDeliveryDate()!=null) {
                    orderRequisitionData.setExpectedDeliveryDate(dateFormat.format(orderRequisitionModel.getExpectedDeliveryDate()));
                }
                if(orderRequisitionModel.getExpectedDeliverySlot()!=null) {
                    orderRequisitionData.setExpectedDeliverySlot(orderRequisitionModel.getExpectedDeliverySlot().getCode());
                }
                if(orderRequisitionModel.getStatus()!=null) {
                    orderRequisitionData.setStatus(orderRequisitionModel.getStatus().getCode());
                }


                orderRequisitionDataList.add(orderRequisitionData);
            }
        }*/
        final SearchPageData<PointRequisitionData> result = new SearchPageData<>();
        result.setPagination(searchResult.getPagination());
        result.setSorts(searchResult.getSorts());
        result.setResults(pointRequisitionDataList);
        return result;

    }

    public RequisitionCockpitData influencerCockpitSummary() {
        final RequisitionCockpitData requisitionCockpitData = new RequisitionCockpitData();
        List<List<Object>> requisitionRaisedList = pointRequisitionService.requisitionRaisedDetails();
        int requistionRaisedCountMTD = 0;
        Double requistionRaisedBagsMTD = 0.0;

        if(requisitionRaisedList!=null && !requisitionRaisedList.isEmpty()){
            if(requisitionRaisedList.get(0).size()>0 && requisitionRaisedList.get(0).get(0)!=null)
            {
                requistionRaisedCountMTD += (int) requisitionRaisedList.get(0).get(0);
            }
            if(requisitionRaisedList.get(0).size()>1 && requisitionRaisedList.get(0).get(1)!=null) {
                requistionRaisedBagsMTD += (double) requisitionRaisedList.get(0).get(1);
            }
        }

        Integer pendingRequisitionCount =  pointRequisitionService.pendingRequistionsDetails();
        Double pointsFromPreviousYears =  pointRequisitionService.pointsFromPreviousYear();
        Double pointsRedeemed = pointRequisitionService.pointsRedeemed();
        Double pointsEarnedCurrentYear = pointRequisitionService.pointsEarnedCurrentYear();
        Double totalRedeemablePoints = pointRequisitionService.totalRedeemablePoints();
        Double bagOffTake = pointRequisitionService.bagOffTake();


        requisitionCockpitData.setRequisitionPendingCount(String.valueOf(pendingRequisitionCount));
        requisitionCockpitData.setRequistionRaisedCountMTD(String.valueOf(requistionRaisedCountMTD));
        requisitionCockpitData.setRequisitionRaisedBagsMTD(String.valueOf(requistionRaisedBagsMTD));
        requisitionCockpitData.setPointsFromPreviousYears(String.valueOf(pointsFromPreviousYears));
        requisitionCockpitData.setPointsEarnedCurrentYear(String.valueOf(pointsEarnedCurrentYear));
        requisitionCockpitData.setPointsRedeemed(String.valueOf(pointsRedeemed));
        requisitionCockpitData.setTotalReedemablePoints(String.valueOf(totalRedeemablePoints));
        requisitionCockpitData.setBagOfftake(String.valueOf(bagOffTake));

        return requisitionCockpitData;
    }

    @Override
    public GiftShopMessageListData giftShopSummary() {
        List<GiftShopMessageData> giftShopMessageDataList = new ArrayList<>();
        List<GiftShopModel> giftShopList= pointRequisitionService.giftShopSummary();
        GiftShopMessageData data = new GiftShopMessageData();

        for(GiftShopModel list: giftShopList ) {
            data.setSchemeCode(list.getCode());
            data.setSchemeName(list.getName());
            data.setRedeemStartDate(list.getRedeemStartDate());
            data.setRedeemEndDate(list.getRedeemEndDate());

            giftShopMessageDataList.add(data);
            break;
        }

        GiftShopMessageListData listData = new GiftShopMessageListData();
        listData.setGiftList(giftShopMessageDataList);

        return listData;
    }

    @Override
    public PointRequisitionData getRequistionDetails(String requisitionId) {
        return pointRequisitionService.getRequistionDetails(requisitionId);
    }

    @Override
    public ErrorListWsDTO updateAllocationRequestCards(String requisitionId, String status, String rejectionReason) {
        return pointRequisitionService.updateAllocationRequestCards(requisitionId, status, rejectionReason);
    }

    @Override
    public SearchPageData<CustomerData> getList(String filter, SearchPageData searchPageData) {
        final SearchPageData<CustomerData> result = new SearchPageData<>();
        SearchPageData<EyDmsCustomerModel> retailerListForDealer = pointRequisitionService.getList(filter,searchPageData);
        result.setPagination(retailerListForDealer.getPagination());
        result.setSorts(retailerListForDealer.getSorts());
        /*List<CustomerData> eydmsCustomerData = dealerBasicConverter.convertAll(retailerListForDealer.getResults());*/
        List<EyDmsCustomerModel> results = retailerListForDealer.getResults();
        List<CustomerData> dealerData=Optional.of(results.stream()
                .map(b2BCustomer -> dealerBasicConverter
                        .convert(b2BCustomer)).collect(Collectors.toList())).get();


        result.setResults(dealerData);
        return result;
    }

    @Override
    public SearchPageData<CustomerData> getSavedDealerRetailer(SearchPageData searchPageData) {
        final SearchPageData<CustomerData> result = new SearchPageData<>();
        SearchPageData<EyDmsCustomerModel> retailerListForDealer = pointRequisitionService.getSavedDealerRetailer(searchPageData);
        result.setPagination(retailerListForDealer.getPagination());
        result.setSorts(retailerListForDealer.getSorts());
        List<CustomerData> eydmsCustomerData = dealerBasicConverter.convertAll(retailerListForDealer.getResults());
        result.setResults(eydmsCustomerData);
        return result;
    }

    @Override
    public List<NetworkAdditionData> getCockpitNetworkAdditionListDetails() {
        EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
        List<SubAreaMasterModel> subAreaMasterModelList = territoryManagementService.getTerritoriesForCustomer(currentUser);
        String leadTypeDealer="DEALER";
        String leadTypeRetailer="RETAILER";
        String leadTypeInfluencer="INFLUENCER";
        List<NetworkAdditionData> dataList = new ArrayList<>();
        NetworkAdditionData retailer = new NetworkAdditionData();
        NetworkAdditionData influencer = new NetworkAdditionData();
          if((currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
              if (CollectionUtils.isNotEmpty(subAreaMasterModelList) && subAreaMasterModelList.get(0) != null) {
                  if (subAreaMasterModelList.get(0).getTaluka() != null) {
                       retailer = getNetworkAdditionDetails(leadTypeRetailer, subAreaMasterModelList.get(0).getPk().toString());
                       influencer = getNetworkAdditionDetails(leadTypeInfluencer, subAreaMasterModelList.get(0).getPk().toString());
                       dataList.add(retailer);
                      dataList.add(influencer);
                  }
              }
          }
            else if ((currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))){
              if (CollectionUtils.isNotEmpty(subAreaMasterModelList) && subAreaMasterModelList.get(0) != null) {
                  if (subAreaMasterModelList.get(0).getTaluka() != null) {
                      influencer = getNetworkAdditionDetails(leadTypeInfluencer, subAreaMasterModelList.get(0).getPk().toString());
                      dataList.add(influencer);

                  }
              }
            }
        return dataList;
    }

    public NetworkAdditionData getNetworkAdditionDetails(String leadType, String taluka){
        NetworkAdditionPlanModel planModel = getPlanModel(taluka, leadType);
        NetworkAdditionData data = new NetworkAdditionData();
        if(Objects.isNull(planModel)){
            LOGGER.info("Plan Model is null");
            /*return new NetworkAdditionData();*/
            data.setTarget(0);
            var achivement=networkService.getOnboarderCustomer(leadType,taluka);
            data.setActual(achivement);
            LOGGER.info(String.format("LeadType : %s",String.valueOf(leadType)));
            data.setLeadType(leadType);
            return data;

        }
        LOGGER.info(String.format("Plan Model: %s ", String.valueOf(planModel.getRevisedPlan())));

        var target=planModel.getRevisedPlan();
        if(Objects.isNull(target) || target==0) {
            target=planModel.getSystemProposed();
        }
        data.setTarget(target);
        var achivement=networkService.getOnboarderCustomer(leadType,taluka);
        data.setActual(achivement);
        LOGGER.info(String.format("LeadType : %s",String.valueOf(leadType)));
        data.setLeadType(leadType);
        return data;
    }

    private NetworkAdditionPlanModel getPlanModel(String taluka, String leadType) {
        Date timestamp= EyDmsDateUtility.getFirstDayOfFinancialYear();
        List<SubAreaMasterModel> talukas = new ArrayList<>();
        
        if(taluka.equalsIgnoreCase("ALL")) {
        	
        	if(userService.getCurrentUser() instanceof EyDmsUserModel)
        	{
        		talukas = territoryManagementService.getTerritoriesForSO();
        	}
        	else
        	{
        		talukas = territoryManagementService.getTerritoriesForCustomer((EyDmsCustomerModel)userService.getCurrentUser());
        	}
           
        }
        else
        {
        	talukas.add(territoryManagementService.getTerritoryById(taluka));
        }
    
        if (!talukas.isEmpty()) {
            return networkDao.findNeworkPlanByTalukaAndLeadType(talukas, leadType, timestamp);
        }

        return null;
    }
}
