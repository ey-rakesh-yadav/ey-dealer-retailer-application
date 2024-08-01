package com.scl.facades.impl;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.customer.services.SclCustomerService;
import com.scl.core.dao.SclUserDao;
import com.scl.core.enums.RequestRaisedBy;
import com.scl.core.enums.RequisitionStatus;
import com.scl.core.enums.RequisitionType;
import com.scl.core.enums.SalesVisibilityTypes;
import com.scl.core.jalo.OrderRequisitionEntry;
import com.scl.core.model.*;
import com.scl.core.services.OrderRequisitionService;
import com.scl.facades.OrderRequisitionFacade;
import com.scl.facades.data.*;
import com.scl.facades.order.data.TrackingData;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.commercefacades.product.data.ImageData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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
    SclUserDao sclUserDao;

    @Resource
    SclCustomerService sclCustomerService;

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
    public SearchPageData<OrderRequisitionData> getOrderRequisitionDetails(String statuses, String submitType, Integer fromMonth, Integer fromYear, Integer toMonth, Integer toYear, String productCode, String fields, SearchPageData searchPageData, String requisitionId, String searchKey,String requestType) {
        final SearchPageData<OrderRequisitionData> result = new SearchPageData<>();
        List<OrderRequisitionData> orderRequisitionDataList = new ArrayList<>();
        //DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
        dateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
        SclCustomerModel currentUser = (SclCustomerModel) userService.getCurrentUser();
        
        String counterType = "Other";
        if(currentUser.getGroups()
    			.contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
        	counterType = "Retailer";
        }

      //  SearchPageData<OrderRequisitionModel> searchResult = orderRequisitionService.getOrderRequisitionDetails(statuses, submitType, fromMonth, fromYear, toMonth, toYear, currentUser, productCode, searchPageData, requisitionId, searchKey);
        SearchPageData<OrderRequisitionModel> searchResult=orderRequisitionService.getOrderHistoryForOrderRequisition(searchPageData, statuses, searchKey, productCode, requestType, submitType, fromMonth, fromYear, toMonth, toYear, requisitionId);

        if(searchResult!=null && searchResult.getResults()!=null) {
            List<OrderRequisitionModel> orderRequisitionModelsList = searchResult.getResults();
            for(OrderRequisitionModel orderRequisitionModel : orderRequisitionModelsList) {
                OrderRequisitionData orderRequisitionData = new OrderRequisitionData();

                orderRequisitionData.setRequisitionId(orderRequisitionModel.getRequisitionId());
                if (orderRequisitionModel.getRequisitionDate() != null) {
                    orderRequisitionData.setRequisitionDate(dateFormat.format(orderRequisitionModel.getRequisitionDate()));
                }

                if (currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                    orderRequisitionData.setFromCustomerUid(orderRequisitionModel.getFromCustomer().getUid());
                    orderRequisitionData.setFromCustomerName(orderRequisitionModel.getFromCustomer().getName());
                    orderRequisitionData.setFromCustomerNo(orderRequisitionModel.getFromCustomer().getCustomerNo());
                    orderRequisitionData.setToCustomerUid(currentUser.getUid());
                    orderRequisitionData.setToCustomerName(currentUser.getName());
                    orderRequisitionData.setToCustomerNo(currentUser.getCustomerNo());
                } else if (currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                    orderRequisitionData.setFromCustomerUid(currentUser.getUid());
                    orderRequisitionData.setFromCustomerName(currentUser.getName());
                    orderRequisitionData.setFromCustomerNo(currentUser.getCustomerNo());
                    orderRequisitionData.setToCustomerUid(orderRequisitionModel.getToCustomer().getUid());
                    orderRequisitionData.setToCustomerName(orderRequisitionModel.getToCustomer().getName());
                    orderRequisitionData.setToCustomerNo(orderRequisitionModel.getToCustomer().getCustomerNo());
                }
                if (orderRequisitionModel.getDeliveryAddress() != null) {
                    AddressData address = new AddressData();
                    addressPopulator.populate(orderRequisitionModel.getDeliveryAddress(), address);
                    AddressModel addressModel=orderRequisitionModel.getDeliveryAddress();
                    address.setAccountName(addressModel.getAccountName());
                    address.setTransportationZone(addressModel.getTransportationZone());
                    address.setState(addressModel.getState());
                    address.setTaluka(addressModel.getTaluka());
                    address.setErpCity(addressModel.getErpCity());
                    orderRequisitionData.setDeliveryAddress(address);
                }
                if (orderRequisitionModel.getProduct() != null) {
                    orderRequisitionData.setProductCode(orderRequisitionModel.getProduct().getCode());
                    orderRequisitionData.setProductName(orderRequisitionModel.getProduct().getName());
                }
                orderRequisitionData.setQuantityInMT(orderRequisitionModel.getQuantity());
                orderRequisitionData.setQuantityInBags(orderRequisitionModel.getQuantityInBags());
                if (orderRequisitionModel.getExpectedDeliveryDate() != null) {
                    orderRequisitionData.setExpectedDeliveryDate(dateFormat.format(orderRequisitionModel.getExpectedDeliveryDate()));
                }
                if (orderRequisitionModel.getExpectedDeliverySlot() != null) {
                    orderRequisitionData.setDeliverySlotName(orderRequisitionModel.getExpectedDeliverySlot().getDisplayName());
                    orderRequisitionData.setExpectedDeliverySlot(orderRequisitionModel.getExpectedDeliverySlot().getCentreTime());
                }  //center time
                if (orderRequisitionModel.getStatus() != null) {
                    orderRequisitionData.setStatus(orderRequisitionModel.getStatus().getCode());
                    if(orderRequisitionModel.getStatus().equals(RequisitionStatus.SERVICED_BY_DEALER)){
                        orderRequisitionData.setRequisitionType(enumerationService.getEnumerationName(orderRequisitionModel.getRequisitionType()));
                        if(orderRequisitionModel.getToCustomer()!=null) {
                            orderRequisitionData.setRequisitionFor(orderRequisitionModel.getToCustomer().getUid());
                        }
                    }
                    if(orderRequisitionModel.getStatus().equals(RequisitionStatus.REJECTED)){
                        orderRequisitionData.setRequisitionType(enumerationService.getEnumerationName(orderRequisitionModel.getRequisitionType()));
                        if(orderRequisitionModel.getToCustomer()!=null) {
                            orderRequisitionData.setRequisitionFor(orderRequisitionModel.getToCustomer().getUid());
                        }
                    }
                    if(orderRequisitionModel.getStatus().equals(RequisitionStatus.PENDING_CONFIRMATION) ||
                            orderRequisitionModel.getStatus().equals(RequisitionStatus.PENDING_DELIVERY) ||
                            orderRequisitionModel.getStatus().equals(RequisitionStatus.PENDING_FULFILLMENT)){
                        orderRequisitionData.setRequisitionType(enumerationService.getEnumerationName(orderRequisitionModel.getRequisitionType()));
                        if(orderRequisitionModel.getToCustomer()!=null) {
                            orderRequisitionData.setRequisitionFor(orderRequisitionModel.getToCustomer().getUid());
                        }
                    }
                }
               /* if (StringUtils.isNotBlank(submitType) && submitType.equals("draft") || submitType.equals("Draft")) {
                    orderRequisitionData.setIsDraft(true);
                } else {
                    orderRequisitionData.setIsDraft(false);
                }*/

//                if(fields!=null && fields.equals("FULL")) {
                //orderRequisitionModel.getOrder()  --> orderRequisitionModel.getOrderEntry().getOrder()
                if (orderRequisitionModel.getOrderEntry() != null && orderRequisitionModel.getOrderEntry().getOrder() != null) {
                    orderRequisitionData.setOrderCode(((OrderModel) orderRequisitionModel.getOrderEntry().getOrder()).getCode());

                    if (StringUtils.isNotBlank(((OrderModel) orderRequisitionModel.getOrderEntry().getOrder()).getErpOrderNumber())) {
                        orderRequisitionData.setErpOrderNo(((OrderModel) orderRequisitionModel.getOrderEntry().getOrder()).getErpOrderNumber());
                    }
                }

                if(orderRequisitionModel.getReceivedQty()!=null) {
                    orderRequisitionData.setReceivedQuantity(orderRequisitionModel.getReceivedQty());
                }

                MediaModel profilePicture = null;
                if(counterType.equals("Retailer")) {
                    profilePicture=orderRequisitionModel.getFromCustomer().getProfilePicture();
                }
                else {
                    profilePicture=orderRequisitionModel.getToCustomer().getProfilePicture();
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
                        orderRequisitionEntryData.setDiNumber(orderRequisitionEntryModel.getDiNumber()+1);
                        orderRequisitionEntryData.setDeliveredDate(orderRequisitionEntryModel.getDeliveryItem().getDeliveredDate());
                        orderRequisitionEntryData.setCancelledDate(orderRequisitionEntryModel.getDeliveryItem().getCancelledDate());
                        orderRequisitionEntryDataList.add(orderRequisitionEntryData);
                    }
                    orderRequisitionData.setRequisitionEntryDetails(orderRequisitionEntryDataList);
                }

             /*
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

                orderRequisitionData.setTrackingDetails(trackingDataList);*/
//               }

                orderRequisitionDataList.add(orderRequisitionData);
            }
            result.setPagination(searchResult.getPagination());
            result.setSorts(searchResult.getSorts());
            result.setResults(orderRequisitionDataList);
            return result;
        }else {
            return result;
        }
    }

    @Override
    public Boolean updateOrderRequistionStatus(String requisitionId, String status, Double receivedQty, String cancelReason) {
        return orderRequisitionService.updateOrderRequistionStatus(requisitionId,status,receivedQty,cancelReason);
    }

	@Override
	public AddressData getAddressDataFromAddressModel(String id, String dealerUid) {
		SclCustomerModel dealer = (SclCustomerModel)userService.getUserForUID(dealerUid);
		AddressModel dealerAddress = sclUserDao.getDealerAddressByRetailerPk(id, dealer);
		if(dealerAddress==null) {
	        SclCustomerModel retailer = (SclCustomerModel) userService.getCurrentUser();

			AddressModel addressModel = sclUserDao.getAddressByPk(id);
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
        SclCustomerModel currentUser = (SclCustomerModel) userService.getCurrentUser();
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

    @Override
    public List<SalesVisibilityData> getSalesVisibilityForDealersAndRetailers(final String fromCustomer, final String toCustomer, String fromDate, String toDate, String filter) {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date1 = null;
        Date date2 = null;
        if(fromDate==null || toDate==null)
        {
            Calendar cal = Calendar.getInstance();
            date2 = cal.getTime();

            cal.add(Calendar.MONTH, -1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            date1 = cal.getTime();
        }
        else
        {
            try {
                date1 = dateFormat.parse(fromDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            try {
                date2 =dateFormat.parse(toDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        String date1_string = dateFormat1.format(date1);
        String date2_string = dateFormat1.format(date2);
        final SclCustomerModel raisedByCustomer = sclCustomerService.getSclCustomerForUid(fromCustomer);
        final SclCustomerModel raisedToCustomer = sclCustomerService.getSclCustomerForUid(toCustomer);

        List<SalesVisibilityData> salesVisibilityDataList = new ArrayList<>();

        List<OrderRequisitionModel> orderRequisitionModels = orderRequisitionService.getSalesVisibilityForDealersAndRetailersFromOrmService(raisedByCustomer, raisedToCustomer, date1_string, date2_string, filter);
        List<MasterStockAllocationModel> masterStockAllocationModels = orderRequisitionService.getSalesVisibilityForDealersAndRetailersFromMsaService(raisedByCustomer, raisedToCustomer, date1_string, date2_string, filter);

        if (StringUtils.isBlank(filter)) {
            if(Objects.nonNull(orderRequisitionModels)){
                salesVisibilityDataList.addAll(getOrderRequisitionEntry(orderRequisitionModels,fromCustomer,toCustomer,filter));
            }
            if(Objects.nonNull(masterStockAllocationModels)){
                getCombinedList(salesVisibilityDataList,masterStockAllocationModels,fromCustomer,toCustomer);
            }

            return salesVisibilityDataList;
        }

        if (filter.equalsIgnoreCase("LIFTING_ASSIGNED") || filter.equalsIgnoreCase("LIFTING_APPROVED")) {

            if(Objects.nonNull(orderRequisitionModels)){
                salesVisibilityDataList.addAll(getOrderRequisitionEntry(orderRequisitionModels,fromCustomer,toCustomer,filter));
            }
        }
        else if (filter.equalsIgnoreCase("INVOICED")){

            if(Objects.nonNull(masterStockAllocationModels)){
                salesVisibilityDataList.addAll(getMasterStockAllocationEntry(masterStockAllocationModels,fromCustomer,toCustomer));
            }
        }
        return salesVisibilityDataList;
    }

    @Override
    public List<SalesVisibilityData> getSalesVisibilityForUser(String toCustomer, String fromDate, String toDate, String filter) {
        {
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            Date date1 = null;
            Date date2 = null;
            if(fromDate==null || toDate==null)
            {
                Calendar cal = Calendar.getInstance();
                date2 = cal.getTime();

                cal.add(Calendar.MONTH, -1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);

                date1 = cal.getTime();
            }
            else
            {
                try {
                    date1 =dateFormat.parse(fromDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                try {
                    date2 = dateFormat.parse(toDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
            String date1_string = dateFormat1.format(date1);
            String date2_string = dateFormat1.format(date2);
            final SclCustomerModel raisedToCustomer = sclCustomerService.getSclCustomerForUid(toCustomer);

            List<SalesVisibilityData> salesVisibilityDataList = new ArrayList<>();


            List<OrderRequisitionModel> orderRequisitionModels = orderRequisitionService.getSalesVisibilityForDealersAndRetailersFromOrmService(null, raisedToCustomer, date1_string, date2_string, filter);
            List<MasterStockAllocationModel> masterStockAllocationModels = orderRequisitionService.getSalesVisibilityForDealersAndRetailersFromMsaService(null, raisedToCustomer, date1_string, date2_string, filter);

            if (StringUtils.isBlank(filter)) {
                if(Objects.nonNull(orderRequisitionModels)){
                    salesVisibilityDataList.addAll(getOrderRequisitionEntry(orderRequisitionModels,null,toCustomer,filter));
                }
                if(Objects.nonNull(masterStockAllocationModels)){
                    getCombinedList(salesVisibilityDataList,masterStockAllocationModels,null,null);
                }

                return salesVisibilityDataList;
            }

            if (filter.equalsIgnoreCase("LIFTING_ASSIGNED") || filter.equalsIgnoreCase("LIFTING_APPROVED")) {

                if(Objects.nonNull(orderRequisitionModels)){
                    salesVisibilityDataList.addAll(getOrderRequisitionEntry(orderRequisitionModels,null,toCustomer,filter));
                }
            }
            else if (filter.equalsIgnoreCase("INVOICED")){

                if(Objects.nonNull(masterStockAllocationModels)){
                    salesVisibilityDataList.addAll(getMasterStockAllocationEntry(masterStockAllocationModels,null,toCustomer));
                }
            }
            return salesVisibilityDataList;
        }
    }

    List<SalesVisibilityData> getOrderRequisitionEntry(List<OrderRequisitionModel> orderRequisitionModels, String raisedByCustomer, String raisedToCustomer,String filter) {
         B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        List<SalesVisibilityData> salesVisibilityDataList = new ArrayList<>();
        SalesVisibilityData salesVisibilityData = new SalesVisibilityData();
        if(Objects.nonNull(raisedByCustomer)) {
            salesVisibilityData.setDealerUid(raisedByCustomer);
        }else{
            if(currentUser instanceof SclUserModel){
                salesVisibilityData.setDealerUid(currentUser.getUid());
            }
        }
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        List<SalesVisibilityProductData> salesVisibilityProductDataList = new ArrayList<>();

        Map<SclCustomerModel, List<OrderRequisitionModel>> collect = orderRequisitionModels.stream().collect(Collectors.groupingBy(OrderRequisitionModel::getToCustomer));

        for (Map.Entry<SclCustomerModel, List<OrderRequisitionModel>> retailerRequisitionsList : collect.entrySet()) {

            SalesVisibilityRetailerData salesVisibilityRetailerData = new SalesVisibilityRetailerData();
            salesVisibilityRetailerData.setRetailerUid(retailerRequisitionsList.getKey().getUid());

            if (retailerRequisitionsList.getValue() != null && !retailerRequisitionsList.getValue().isEmpty()) {

                for (OrderRequisitionModel orderRequisitionModel : retailerRequisitionsList.getValue()) {

                    if (orderRequisitionModel.getProduct() != null) {
                        SalesVisibilityProductData salesVisibilityProductData = new SalesVisibilityProductData();

                        if(orderRequisitionModel.getEquivalenceProductCode()!=null){
                            salesVisibilityProductData.setProductCode(orderRequisitionModel.getEquivalenceProductCode());
                        }
                        else if(orderRequisitionModel.getProduct().getCode()!=null){
                                salesVisibilityProductData.setProductCode(orderRequisitionModel.getProduct().getCode());}

                        if(orderRequisitionModel.getAliasCode()!=null){
                            salesVisibilityProductData.setProductAliasName(orderRequisitionModel.getAliasCode());
                        }
                        else if(orderRequisitionModel.getProduct().getName()!=null){
                            salesVisibilityProductData.setProductAliasName(orderRequisitionModel.getProduct().getName());}

                        if(orderRequisitionModel.getQuantityInBags()!=null){
                            salesVisibilityProductData.setQuantityInBag(orderRequisitionModel.getQuantityInBags());}
                        if(orderRequisitionModel.getQuantity()!=null){
                            salesVisibilityProductData.setQuantityInMt(orderRequisitionModel.getQuantity());}
                        if(orderRequisitionModel.getRequisitionId()!=null){
                            salesVisibilityProductData.setLiftingId(orderRequisitionModel.getRequisitionId());}
                        if(orderRequisitionModel.getInvoiceNumber()!=null){
                            salesVisibilityProductData.setReferenceInvoice(orderRequisitionModel.getInvoiceNumber());}
                        if(orderRequisitionModel.getLiftingDate()!=null){
                            salesVisibilityProductData.setDate(formatter.format(orderRequisitionModel.getLiftingDate()));}
                        if (orderRequisitionModel.getRequestRaisedBy().equals(RequestRaisedBy.DEALER)) {
                            salesVisibilityProductData.setType(SalesVisibilityTypes.LIFTING_ASSIGNED.getCode());
                        } else {
                            salesVisibilityProductData.setType(SalesVisibilityTypes.LIFTING_APPROVED.getCode());
                        }
                        if(currentUser instanceof SclUserModel) {
                            if (orderRequisitionModel.getFromCustomer() != null) {
                                if(orderRequisitionModel.getFromCustomer().getName()!=null)
                                    salesVisibilityProductData.setDealerName(orderRequisitionModel.getFromCustomer().getName());
                                if(orderRequisitionModel.getFromCustomer().getUid()!=null)
                                    salesVisibilityProductData.setDealerCode(orderRequisitionModel.getFromCustomer().getUid());
                            }
                        }
                        salesVisibilityProductDataList.add(salesVisibilityProductData);
                    }
                    if(StringUtils.isNotEmpty(filter) && StringUtils.isNotBlank(filter)){
                    if(filter.equalsIgnoreCase(SalesVisibilityTypes.LIFTING_ASSIGNED.getCode())){
                        salesVisibilityRetailerData.setProduct(salesVisibilityProductDataList.stream().filter(item-> item.getType().equalsIgnoreCase(SalesVisibilityTypes.LIFTING_ASSIGNED.getCode())).collect(Collectors.toList()));
                    }
                    else if(filter.equalsIgnoreCase(SalesVisibilityTypes.LIFTING_APPROVED.getCode())){
                        salesVisibilityRetailerData.setProduct(salesVisibilityProductDataList.stream().filter(item-> item.getType().equalsIgnoreCase(SalesVisibilityTypes.LIFTING_APPROVED.getCode())).collect(Collectors.toList()));
                     }
                    }
                    else{
                        salesVisibilityRetailerData.setProduct(salesVisibilityProductDataList);
                    }
                }
                salesVisibilityData.setRetailer(salesVisibilityRetailerData);
            }
            salesVisibilityDataList.add(salesVisibilityData);
        }
        return salesVisibilityDataList;
    }

    List<SalesVisibilityData> getMasterStockAllocationEntry(List<MasterStockAllocationModel> masterStockAllocationModelList, String raisedByCustomer, String raisedToCustomer) {
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        List<SalesVisibilityData> salesVisibilityDataList = new ArrayList<>();
        SalesVisibilityData salesVisibilityData = new SalesVisibilityData();
        if(Objects.nonNull(raisedByCustomer)) {
            salesVisibilityData.setDealerUid(raisedByCustomer);
        }else{
            if(currentUser instanceof SclUserModel){
                salesVisibilityData.setDealerUid(currentUser.getUid());
            }
        }
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        List<SalesVisibilityProductData> salesVisibilityProductDataList = new ArrayList<>();

        Map<SclCustomerModel, List<MasterStockAllocationModel>> collect = masterStockAllocationModelList.stream().collect(Collectors.groupingBy(MasterStockAllocationModel::getRetailer));

        for (Map.Entry<SclCustomerModel, List<MasterStockAllocationModel>> retailerRequisitionsList : collect.entrySet()) {

            SalesVisibilityRetailerData salesVisibilityRetailerData = new SalesVisibilityRetailerData();
            salesVisibilityRetailerData.setRetailerUid(retailerRequisitionsList.getKey().getUid());

            if (retailerRequisitionsList.getValue() != null && !retailerRequisitionsList.getValue().isEmpty()) {
                for (MasterStockAllocationModel masterStockAllocationModel : retailerRequisitionsList.getValue()) {

                    SalesVisibilityProductData salesVisibilityProductData = new SalesVisibilityProductData();
                    if (masterStockAllocationModel.getProduct() != null) {
                        if (masterStockAllocationModel.getProduct().getEquivalenceProductCode() != null) {
                            salesVisibilityProductData.setProductCode(masterStockAllocationModel.getProduct().getEquivalenceProductCode());
                        } else if(masterStockAllocationModel.getProduct().getCode()!=null) {
                            salesVisibilityProductData.setProductCode(masterStockAllocationModel.getProduct().getCode());}

                    if (masterStockAllocationModel.getAliasCode() != null) {
                        salesVisibilityProductData.setProductAliasName(masterStockAllocationModel.getAliasCode());
                    } else if (masterStockAllocationModel.getProduct().getName()!=null){
                        salesVisibilityProductData.setProductAliasName(masterStockAllocationModel.getProduct().getName());}

                    if (masterStockAllocationModel.getQuantityInBags() != null) {
                            salesVisibilityProductData.setQuantityInBag(masterStockAllocationModel.getQuantityInBags());
                        }
                    if (masterStockAllocationModel.getQuantityInMt() != null) {
                        salesVisibilityProductData.setQuantityInMt(masterStockAllocationModel.getQuantityInMt());
                    }
                    if (masterStockAllocationModel.getIsInvoiceCancelled().equals(Boolean.FALSE)) {
                        salesVisibilityProductData.setType(SalesVisibilityTypes.INVOICED.getCode());
                    }
                    if (masterStockAllocationModel.getTaxInvoiceNumber() != null) {
                        salesVisibilityProductData.setReferenceInvoice(masterStockAllocationModel.getTaxInvoiceNumber());
                    }
                    if (masterStockAllocationModel.getInvoicedDate() != null) {
                        salesVisibilityProductData.setDate(formatter.format(masterStockAllocationModel.getInvoicedDate()));
                    }
                    if(masterStockAllocationModel.getAliasCode()!=null){
                        salesVisibilityProductData.setProductAliasName(masterStockAllocationModel.getAliasCode());}
                    if(masterStockAllocationModel.getQuantityInBags()!=null){
                        salesVisibilityProductData.setQuantityInBag(masterStockAllocationModel.getQuantityInBags());}
                    if(masterStockAllocationModel.getQuantityInMt()!=null){
                        salesVisibilityProductData.setQuantityInMt(masterStockAllocationModel.getQuantityInMt());}
                    if(masterStockAllocationModel.getIsInvoiceCancelled().equals(Boolean.FALSE)){
                        salesVisibilityProductData.setType(SalesVisibilityTypes.INVOICED.getCode());}
                    if(masterStockAllocationModel.getTaxInvoiceNumber()!=null){
                        salesVisibilityProductData.setReferenceInvoice(masterStockAllocationModel.getTaxInvoiceNumber());}
                    if(masterStockAllocationModel.getInvoicedDate()!=null){
                        salesVisibilityProductData.setDate(formatter.format(masterStockAllocationModel.getInvoicedDate()));}
                    if(currentUser instanceof SclUserModel) {
                        if (masterStockAllocationModel.getDealer() != null) {
                            if(masterStockAllocationModel.getDealer().getName()!=null)
                                salesVisibilityProductData.setDealerName(masterStockAllocationModel.getDealer().getName());
                            if(masterStockAllocationModel.getDealer().getUid()!=null)
                                salesVisibilityProductData.setDealerCode(masterStockAllocationModel.getDealer().getUid());
                        }
                    }
                    salesVisibilityProductDataList.add(salesVisibilityProductData);
                }
                    salesVisibilityRetailerData.setProduct(salesVisibilityProductDataList);
                }
                salesVisibilityData.setRetailer(salesVisibilityRetailerData);
            }
            salesVisibilityDataList.add(salesVisibilityData);
        }
        return salesVisibilityDataList;

    }

    void getCombinedList(List<SalesVisibilityData> salesVisibilityDataList, List<MasterStockAllocationModel> masterStockAllocationModel,String raisedByCustomer, String raisedToCustomer){

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        List<SalesVisibilityProductData> salesVisibilityProductDataList= new ArrayList<>();

        if(CollectionUtils.isNotEmpty(masterStockAllocationModel)) {
            masterStockAllocationModel.forEach(item -> {
                SalesVisibilityProductData productData = new SalesVisibilityProductData();
                if (item.getProductCode() != null) {
                    productData.setProductCode(item.getProductCode());
                }
                if (item.getAliasCode() != null) {
                    productData.setProductAliasName(item.getAliasCode());
                }
                if (item.getQuantityInBags() != null) {
                    productData.setQuantityInBag(item.getQuantityInBags());
                }
                if (item.getQuantityInMt() != null) {
                    productData.setQuantityInMt(item.getQuantityInMt());
                }
                if (item.getIsInvoiceCancelled().equals(Boolean.FALSE)) {
                    productData.setType(SalesVisibilityTypes.INVOICED.getCode());
                }
                if (item.getTaxInvoiceNumber() != null) {
                    productData.setReferenceInvoice(item.getTaxInvoiceNumber());
                }
                if (item.getInvoicedDate() != null) {
                    productData.setDate(formatter.format(item.getInvoicedDate()));
                }
                salesVisibilityProductDataList.add(productData);
            });
        }
        if(CollectionUtils.isNotEmpty(salesVisibilityDataList)) {
            List<SalesVisibilityProductData> existingProductDataList = salesVisibilityDataList.get(0).getRetailer().getProduct();
            existingProductDataList.addAll(salesVisibilityProductDataList);
            salesVisibilityDataList.get(0).getRetailer().setProduct(existingProductDataList);
        }
        else{
            salesVisibilityDataList.addAll(getMasterStockAllocationEntry(masterStockAllocationModel,raisedByCustomer,raisedToCustomer));
        }
    }

}
