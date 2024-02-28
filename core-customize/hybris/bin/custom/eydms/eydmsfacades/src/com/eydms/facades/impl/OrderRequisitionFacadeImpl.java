package com.eydms.facades.impl;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.dao.EyDmsUserDao;
import com.eydms.core.enums.RequisitionStatus;
import com.eydms.core.jalo.OrderRequisitionEntry;
import com.eydms.core.model.OrderRequisitionEntryModel;
import com.eydms.core.model.OrderRequisitionModel;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.services.OrderRequisitionService;
import com.eydms.facades.OrderRequisitionFacade;
import com.eydms.facades.data.OrderRequisitionData;
import com.eydms.facades.data.OrderRequisitionEntryData;
import com.eydms.facades.order.data.TrackingData;

import de.hybris.platform.commercefacades.product.data.ImageData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class OrderRequisitionFacadeImpl implements OrderRequisitionFacade {

    @Autowired
    OrderRequisitionService orderRequisitionService;

    @Autowired
    UserService userService;

    @Autowired
    Populator<AddressModel, AddressData> addressPopulator;
    
    @Autowired
    Populator<AddressModel, AddressData> dealerAddressPopulator;

    @Autowired
    Converter<AddressModel, AddressData> addressConverter;
    
	private Converter<MediaModel, ImageData> imageConverter;

    @Autowired
    EnumerationService enumerationService;
    
    @Autowired
    EyDmsUserDao eydmsUserDao;

    private static final Logger LOG = Logger.getLogger(OrderRequisitionFacadeImpl.class);
	
    public Converter<MediaModel, ImageData> getImageConverter() {
		return imageConverter;
	}

	public void setImageConverter(Converter<MediaModel, ImageData> imageConverter) {
		this.imageConverter = imageConverter;
	}

    @Override
    public boolean saveOrderRequisitionDetails(OrderRequisitionData orderRequisitionData) {
        return orderRequisitionService.saveOrderRequisitionDetails(orderRequisitionData);
    }

    @Override
    public SearchPageData<OrderRequisitionData> getOrderRequisitionDetails(List<String> statuses, String submitType, Integer fromMonth, Integer fromYear, Integer toMonth, Integer toYear, String productCode, String fields, SearchPageData searchPageData, String requisitionId, String searchKey) {
        List<OrderRequisitionData> orderRequisitionDataList = new ArrayList<>();
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");
        EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
        
        String counterType = "Other";
        if(currentUser.getGroups()
    			.contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
        	counterType = "Retailer";
        }

        SearchPageData<OrderRequisitionModel> searchResult = orderRequisitionService.getOrderRequisitionDetails(statuses, submitType, fromMonth, fromYear, toMonth, toYear, currentUser, productCode, searchPageData, requisitionId, searchKey);

        if(searchResult!=null && searchResult.getResults()!=null) {
            List<OrderRequisitionModel> orderRequisitionModelsList = searchResult.getResults();
            for(OrderRequisitionModel orderRequisitionModel : orderRequisitionModelsList) {
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
//                    AddressData address = new AddressData();
//                    addressPopulator.populate(orderRequisitionModel.getDeliveryAddress(), address);
                    orderRequisitionData.setDeliveryAddress(addressConverter.convert(orderRequisitionModel.getDeliveryAddress()));
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
                	orderRequisitionData.setDeliverySlotName(enumerationService.getEnumerationName(orderRequisitionModel.getExpectedDeliverySlot()));
                    orderRequisitionData.setExpectedDeliverySlot(orderRequisitionModel.getExpectedDeliverySlot().getCode());
                }
                if(orderRequisitionModel.getStatus()!=null) {
                    orderRequisitionData.setStatus(orderRequisitionModel.getStatus().getCode());
                }
                if(submitType.equals("draft") || submitType.equals("Draft")) {
                    orderRequisitionData.setIsDraft(true);
                }
                else {
                    orderRequisitionData.setIsDraft(false);
                }

//                if(fields!=null && fields.equals("FULL")) {
                if(orderRequisitionModel.getOrder()!=null) {
                    orderRequisitionData.setOrderCode(((OrderModel)orderRequisitionModel.getOrder()).getCode());
                }
                if(orderRequisitionModel.getOrder()!=null) {
                    orderRequisitionData.setErpOrderNo(((OrderModel)orderRequisitionModel.getOrder()).getErpOrderNumber());
                }


                if(orderRequisitionModel.getReceivedQty()!=null) {
                    orderRequisitionData.setReceivedQuantity(orderRequisitionModel.getReceivedQty());
                }

                MediaModel profilePicture = null;
                if(counterType.equals("Retailer")) {
                    orderRequisitionModel.getFromCustomer().getProfilePicture();
                }
                else {
                    orderRequisitionModel.getToCustomer().getProfilePicture();
                }
                if(profilePicture!=null) {
                    final ImageData profileImageData = getImageConverter().convert(profilePicture);
                    orderRequisitionData.setProfilePicture(profileImageData);
                }

//                if(orderRequisitionModel.getServiceType()!=null) {
//                    orderRequisitionData.setServiceType(orderRequisitionModel.getServiceType().getCode());
//                }

//                List<String> orderReqEntriesComments = new ArrayList<>();
//                if(orderRequisitionModel.getOrderRequisitionEntries()!=null && !orderRequisitionModel.getOrderRequisitionEntries().isEmpty()) {
//                    for(OrderRequisitionEntryModel orderReqEntry : orderRequisitionModel.getOrderRequisitionEntries()) {
//                        if(orderReqEntry.getEntry().getDeliveredDate()!=null)
//                            orderReqEntriesComments.add(String.format("Quantity Fulfilled %s : %s at %s", orderReqEntry.getEntryNumber()+1, orderReqEntry.getQuantity(), orderReqEntry.getEntry().getDeliveredDate()));
//                    }
//                }

                List<String> serviceTypeList = new ArrayList<>();
                if(orderRequisitionModel.getServiceType()!=null) {
                    orderRequisitionData.setServiceType(orderRequisitionModel.getServiceType().getCode());
                    if(orderRequisitionModel.getServiceType().getCode().equals("PLACED")) {
                        serviceTypeList.add("Order Placed");
                    }
                    if(orderRequisitionModel.getServiceType().getCode().equals("CLUBBED_PLACED")) {
                        serviceTypeList.add("Clubbed Orders and placed");
                    }
                    if(orderRequisitionModel.getServiceType().getCode().equals("SELF_STOCK")) {
                        serviceTypeList.add("Served from self-stock");
                    }
                }

                if(orderRequisitionModel.getOrderRequisitionEntries()!=null && !(orderRequisitionModel.getOrderRequisitionEntries().isEmpty()) && (orderRequisitionModel.getOrderRequisitionEntries().size() >= 1)) {
                    List<OrderRequisitionEntryData> orderRequisitionEntryDataList = new ArrayList<>();

                    for(OrderRequisitionEntryModel orderRequisitionEntryModel : orderRequisitionModel.getOrderRequisitionEntries()) {
                        OrderRequisitionEntryData orderRequisitionEntryData = new OrderRequisitionEntryData();
                        orderRequisitionEntryData.setQuantity(orderRequisitionEntryModel.getQuantity());
                        orderRequisitionEntryData.setEntryNumber(orderRequisitionEntryModel.getEntryNumber()+1);
                        orderRequisitionEntryData.setDeliveredDate(orderRequisitionEntryModel.getEntry().getDeliveredDate());
                        orderRequisitionEntryData.setCancelledDate(orderRequisitionEntryModel.getEntry().getCancelledDate());
                        orderRequisitionEntryDataList.add(orderRequisitionEntryData);
                    }
                    orderRequisitionData.setRequisitionEntryDetails(orderRequisitionEntryDataList);
                }

                List<TrackingData> trackingDataList = new ArrayList<>();

                TrackingData pendingConfirmationData = new TrackingData();
                pendingConfirmationData.setCode(RequisitionStatus.PENDING_CONFIRMATION.getCode());
                pendingConfirmationData.setName("Requisition Received");
                pendingConfirmationData.setActualTime(orderRequisitionModel.getRequisitionDate());
                pendingConfirmationData.setIndex(1);
                trackingDataList.add(pendingConfirmationData);

                TrackingData pendingFulfillmentData = new TrackingData();
                pendingFulfillmentData.setCode(RequisitionStatus.PENDING_FULFILLMENT.getCode());
                pendingFulfillmentData.setName("Requisition Accepted");
                pendingFulfillmentData.setActualTime(orderRequisitionModel.getAcceptedDate());
                pendingFulfillmentData.setIndex(2);
                if(serviceTypeList!=null && !serviceTypeList.isEmpty()) {
                    pendingFulfillmentData.setComment(serviceTypeList);
                }
                trackingDataList.add(pendingFulfillmentData);

                TrackingData pendingDeliveryData = new TrackingData();
                pendingDeliveryData.setCode(RequisitionStatus.PENDING_DELIVERY.getCode());
                pendingDeliveryData.setName("Requisition Fulfilled");
                pendingDeliveryData.setActualTime(orderRequisitionModel.getFulfilledDate());
                pendingDeliveryData.setIndex(3);
                if(serviceTypeList!=null && !serviceTypeList.isEmpty()) {
                    pendingDeliveryData.setComment(serviceTypeList);
                }
//                if(orderReqEntriesComments!=null && !orderReqEntriesComments.isEmpty()) {
//                    if(orderReqEntriesComments.size()>1) {
//                        pendingDeliveryData.setComment(orderReqEntriesComments);
//                    }
//                }
                trackingDataList.add(pendingDeliveryData);

                TrackingData deliveredData = new TrackingData();
                if(orderRequisitionModel.getDeliveredDate() == null) {
                    deliveredData.setCode(RequisitionStatus.DELIVERED.getCode());
                }
                else {
                    deliveredData.setCode(orderRequisitionModel.getStatus().getCode());
                    if(orderRequisitionModel.getStatus().getCode().equals("PARTIAL_DELIVERED")) {
                        deliveredData.setName("Request is Partially Delivered");
                        deliveredData.setActualTime(orderRequisitionModel.getPartialDeliveredDate());
                    }
                    else {
                        deliveredData.setName("Request is Delivered");
                        deliveredData.setActualTime(orderRequisitionModel.getDeliveredDate());
                    }

                }
                deliveredData.setIndex(4);
//                if(orderReqEntriesComments!=null && !orderReqEntriesComments.isEmpty()) {
//                    deliveredData.setComment(orderReqEntriesComments);
//                }
                trackingDataList.add(deliveredData);

                TrackingData cancelledData = new TrackingData();
                cancelledData.setCode(RequisitionStatus.CANCELLED.getCode());
                cancelledData.setName("Request Cancelled");
                cancelledData.setActualTime(orderRequisitionModel.getCancelledDate());
                cancelledData.setIndex(5);
                trackingDataList.add(cancelledData);

                TrackingData rejectedData = new TrackingData();
                rejectedData.setCode(RequisitionStatus.REJECTED.getCode());
                rejectedData.setName("Request Rejected");
                rejectedData.setActualTime(orderRequisitionModel.getRejectedDate());
                rejectedData.setIndex(6);
                trackingDataList.add(rejectedData);

                orderRequisitionData.setTrackingDetails(trackingDataList);
//               }

                orderRequisitionDataList.add(orderRequisitionData);
            }
        }
        final SearchPageData<OrderRequisitionData> result = new SearchPageData<>();
        result.setPagination(searchResult.getPagination());
        result.setSorts(searchResult.getSorts());
        result.setResults(orderRequisitionDataList);
        return result;
    }

    @Override
    public Boolean updateOrderRequistionStatus(String requisitionId, String status, Double receivedQty, String cancelReason) {
        return orderRequisitionService.updateOrderRequistionStatus(requisitionId,status,receivedQty,cancelReason);
    }

	@Override
	public AddressData getAddressDataFromAddressModel(String id, String dealerUid) {
		EyDmsCustomerModel dealer = (EyDmsCustomerModel)userService.getUserForUID(dealerUid);
		AddressModel dealerAddress = eydmsUserDao.getDealerAddressByRetailerPk(id, dealer);
		if(dealerAddress==null) {
	        EyDmsCustomerModel retailer = (EyDmsCustomerModel) userService.getCurrentUser();

			AddressModel addressModel = eydmsUserDao.getAddressByPk(id);
			AddressData address = new AddressData();
			addressPopulator.populate(addressModel, address);
			address.setState(addressModel.getState());
			address.setErpCity(addressModel.getErpCity());
			address.setTaluka(addressModel.getTaluka());
			address.setDistrict(addressModel.getDistrict());
			address.setAccountName(addressModel.getAccountName());
            address.setIsPrimaryAddress(addressModel.getIsPrimaryAddress());
            address.setErpId(null);
            address.setRetailerAddressPk(id);
            address.setRetailerUid(retailer.getUid());
            address.setRetailerName(retailer.getName());
            address.setLastUsedDate(new Date());
            userService.setCurrentUser(dealer);
           
            LOG.error("ADDRESS_DUPLICATE_ISSUE" + " Current User is " + userService.getCurrentUser().getUid() + "-" + userService.getCurrentUser().getName() + " and the address pk is " + addressModel.getPk().toString());

            return address;
		}
		return null;
	}

    @Override
    public List<AddressData> getAddressListForRetailer() {
        EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
        List<AddressData> addressDataList = new ArrayList<>();
        List<AddressModel> addressModelList = currentUser.getAddresses().stream().collect(Collectors.toList());
        if(addressModelList!=null && !addressModelList.isEmpty()) {
            for(AddressModel addressModel : addressModelList) {
                AddressData addressData = new AddressData();
                addressPopulator.populate(addressModel, addressData);
                addressData.setState(addressModel.getState());
                addressData.setDistrict(addressModel.getDistrict());
                addressData.setErpCity(addressModel.getErpCity());
                addressData.setErpId(addressModel.getErpAddressId());
                addressData.setTaluka(addressModel.getTaluka());
                addressData.setAccountName(addressModel.getAccountName());
                addressDataList.add(addressData);
            }

            return addressDataList;
        }
        return Collections.emptyList();
    }
}
