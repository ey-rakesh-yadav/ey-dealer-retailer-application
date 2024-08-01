package com.scl.core.services.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import com.scl.core.constants.GeneratedSclCoreConstants;
import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.*;
import com.scl.core.enums.*;

import com.scl.core.model.*;
import com.scl.core.notifications.service.SclNotificationService;
import com.scl.core.order.impl.DefaultSCLB2BOrderService;
import com.scl.core.services.OrderRequisitionService;
import com.scl.core.services.TerritoryManagementService;
import com.scl.facades.data.OrderRequisitionData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;

public class OrderRequisitionServiceImpl implements OrderRequisitionService {

    @Autowired
    OrderRequisitionDao orderRequisitionDao;
    @Autowired
    SclSalesSummaryDao sclSalesSummaryDao;
    @Autowired
    SalesPerformanceDao salesPerformanceDao;
    @Autowired
    ConfigurationService configurationService;
    @Autowired
    BaseStoreService baseStoreService;
    @Autowired
    EnumerationService enumerationService;

    @Autowired
    UserService userService;

    @Autowired
    DataConstraintDao dataConstraintDao;

    @Autowired
    BaseSiteService baseSiteService;

    @Autowired
    TerritoryManagementService territoryManagementService;

    @Autowired
    ModelService modelService;

    @Resource
    KeyGenerator orderRequisitionIdGenerator;

    @Autowired
    CatalogVersionService catalogVersionService;

    @Autowired
    ProductService productService;

    @Autowired
    SclNotificationService sclNotificationService;

    @Autowired
    SclUserDao sclUserDao;

    @Resource
	DealerDao dealerDao;
    @Autowired
    private DeliverySlotMasterDao deliverySlotMasterDao;
    
    private static final Logger LOGGER = Logger.getLogger(OrderRequisitionServiceImpl.class);
    
    @Override
    public boolean saveOrderRequisitionDetails(OrderRequisitionData orderRequisitionData) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            cal.add(Calendar.MONTH, -1);
            Date oneMonthDate = cal.getTime();
            cal.add(Calendar.MONTH, -2);
            Date threeMonthDate = cal.getTime();

            OrderRequisitionModel orderRequisitionModel;
            BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
          DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
          //  DateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd"); // Customize the format as needed
            //dateFormat.setTimeZone(TimeZone.getTimeZone("IST"));

            if (Objects.nonNull(orderRequisitionData.getRequisitionId())) {
                orderRequisitionModel = orderRequisitionDao.findByRequisitionId(orderRequisitionData.getRequisitionId());
            } else {
                orderRequisitionModel = modelService.create(OrderRequisitionModel.class);
                orderRequisitionModel.setRequisitionId(orderRequisitionIdGenerator.generate().toString());
                orderRequisitionModel.setRequisitionDate(new Date());
            }

            //new changes in save api
                Date liftingDate = null;
                SclCustomerModel fromCustomerModel=null,toCustomerModel=null;
                Date date = new Date();
                RequisitionAction requisitionAction=null;
                try {
                    orderRequisitionModel.setBrand(brand);
                    if (orderRequisitionData.getLiftingDate() != null) {
                        //liftingDate = timeFormat.parse(orderRequisitionData.getLiftingDate());
                        liftingDate = new SimpleDateFormat("yyyy-MM-dd").parse(orderRequisitionData.getLiftingDate());
                        orderRequisitionModel.setLiftingDate(liftingDate);
                    }

                    if (orderRequisitionData.getFromCustomerUid() != null) {
                        fromCustomerModel = (SclCustomerModel) userService.getUserForUID(orderRequisitionData.getFromCustomerUid());
                    }
                    if (orderRequisitionData.getToCustomerUid() != null) {
                        toCustomerModel = (SclCustomerModel) userService.getUserForUID(orderRequisitionData.getToCustomerUid());
                    }
                    if(Objects.nonNull(orderRequisitionData.getRequisitionAction())){
                       requisitionAction = enumerationService.getEnumerationValue(RequisitionAction.class, orderRequisitionData.getRequisitionAction());
                    }
                //shipToParty,crmOrderNumber,orderEntryNo,approvedDate,expiredDate,requisitionFor
                    if (Objects.nonNull(fromCustomerModel) && Objects.nonNull(toCustomerModel)) {
                        if (fromCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                            if (requisitionAction.equals(RequisitionAction.CREATE) && Objects.isNull(orderRequisitionData.getRequisitionId())) {
                                orderRequisitionModel.setRequestRaisedBy(RequestRaisedBy.DEALER);
                                orderRequisitionModel.setStatus(RequisitionStatus.SERVICED_BY_DEALER);
                                if (orderRequisitionData.getProductCode() != null && !(orderRequisitionData.getProductCode().isEmpty())) {
                                    if (baseSiteService.getCurrentBaseSite() != null) {
                                        CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
                                        ProductModel product = productService.getProductForCode(catalogVersion, orderRequisitionData.getProductCode());
                                        orderRequisitionModel.setProduct(product);
                                    }
                                }

                                orderRequisitionModel.setAliasCode(orderRequisitionData.getAliasCode());
                                orderRequisitionModel.setEquivalenceProductCode(orderRequisitionData.getEquivalenceProductCode());
                                if (orderRequisitionData.getQuantityInBags() != 0.0) {
                                    orderRequisitionModel.setQuantity(orderRequisitionData.getQuantityInBags() / 20);
                                    orderRequisitionModel.setQuantityInBags(orderRequisitionData.getQuantityInBags());
                                } else {
                                    orderRequisitionModel.setQuantity(0.0);
                                    orderRequisitionModel.setQuantityInBags(0.0);
                                }
                                orderRequisitionModel.setRequisitionDate(new Date());
                                orderRequisitionModel.setFromCustomer(fromCustomerModel);

                                    orderRequisitionModel.setToCustomer(toCustomerModel);
                                    if (toCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                                        orderRequisitionModel.setRequisitionFor(RequisitionFor.RETAILER);
                                    }
                                    else if (toCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID))) {
                                        orderRequisitionModel.setRequisitionFor(RequisitionFor.INFLUENCER);
                                    }
                                /*if (orderRequisitionData.getPlacedById() != null) {
                                    SclCustomerModel currentUser = (SclCustomerModel) userService.getUserForUID(fromCustomerModel.getUid());
                                    List<PartnerCustomerModel> partnerCustomerModel = currentUser.getPartnerCustomer().stream().filter(a -> a.getId().equalsIgnoreCase(orderRequisitionData.getPlacedById())).collect(Collectors.toList());
                                    String partnerName = partnerCustomerModel.get(0).getName();
                                    orderRequisitionModel.setPlacedByName(partnerName);
                                }
                                else{
                                    UserModel userModel = userService.getUserForUID(fromCustomerModel.getUid());
                                    orderRequisitionModel.setPlacedByName(userModel.getName());
                                }*/
                                orderRequisitionModel.setInvoiceNumber(orderRequisitionData.getInvoiceNumber());
                                orderRequisitionModel.setAssignedDate(new Date());
                                if(Objects.nonNull(liftingDate)){
                                if (liftingDate.before(date)) {
                                    orderRequisitionModel.setRequisitionType(RequisitionType.LIFTING);
                                    orderRequisitionModel.setLiftingDate(liftingDate);

                                }else{
                                    //need to check
                                    orderRequisitionModel.setRequestRaisedBy(RequestRaisedBy.RETAILER);
                                    orderRequisitionModel.setRequisitionType(RequisitionType.ORDER);
                                    orderRequisitionModel.setStatus(RequisitionStatus.PENDING_CONFIRMATION);
                                    if (toCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                                        orderRequisitionModel.setRequisitionFor(RequisitionFor.RETAILER);
                                        orderRequisitionModel.setToCustomer(toCustomerModel);
                                    }
                                    if (orderRequisitionData.getExpectedDeliverySlot() != null && !(orderRequisitionData.getExpectedDeliverySlot().isEmpty())) {
                                        if (!Objects.isNull(DeliverySlots.valueOf(orderRequisitionData.getExpectedDeliverySlot()))) {
                                            orderRequisitionModel.setExpectedDeliverySlot(deliverySlotMasterDao.findByCentreTime(orderRequisitionData.getExpectedDeliverySlot()));
                                        }
                                    }

                                    if (orderRequisitionData.getExpectedDeliveryDate() != null && !(orderRequisitionData.getExpectedDeliveryDate().isEmpty())) {
                                        try {
                                            orderRequisitionModel.setExpectedDeliveryDate(dateFormat.parse(orderRequisitionData.getExpectedDeliveryDate()));
                                        } catch (ParseException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                    if (!Objects.isNull(orderRequisitionData.getDeliveryAddress())) {
                                        if (orderRequisitionData.getDeliveryAddress().getId() != null && !orderRequisitionData.getDeliveryAddress().getId().isEmpty() && !Objects.isNull(sclUserDao.getAddressByPk(orderRequisitionData.getDeliveryAddress().getId()))) {
                                            orderRequisitionModel.setDeliveryAddress(sclUserDao.getAddressByPk(orderRequisitionData.getDeliveryAddress().getId()));
                                        }
                                    }
                                    orderRequisitionModel.setShipToParty(orderRequisitionData.getShipToParty());
                                }
                              }
                            }
                            if (requisitionAction.equals(RequisitionAction.APPROVE)) {
                                orderRequisitionModel.setRequisitionAction(RequisitionAction.APPROVE);
                                orderRequisitionModel.setApprovedDate(new Date());
                                orderRequisitionModel.setInvoiceNumber(orderRequisitionData.getInvoiceNumber());

                                if(Objects.nonNull(liftingDate)) {
                                    if (liftingDate.before(date)) {
                                        orderRequisitionModel.setRequisitionType(RequisitionType.LIFTING);
                                        orderRequisitionModel.setLiftingDate(liftingDate);
                                        orderRequisitionModel.setStatus(RequisitionStatus.SERVICED_BY_DEALER);
                                            orderRequisitionModel.setToCustomer(toCustomerModel);
                                            if (toCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                                                orderRequisitionModel.setRequisitionFor(RequisitionFor.RETAILER);
                                            }
                                            else if (toCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID))) {
                                                orderRequisitionModel.setRequisitionFor(RequisitionFor.INFLUENCER);
                                            }
                                    } else {
                                        orderRequisitionModel.setRequisitionType(RequisitionType.ORDER);
                                            if (toCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                                                orderRequisitionModel.setToCustomer(toCustomerModel);
                                                orderRequisitionModel.setRequisitionFor(RequisitionFor.RETAILER);
                                            }

                                        if (orderRequisitionData.getOrderCode() != null && !orderRequisitionData.getOrderCode().isEmpty()) {
                                            orderRequisitionModel.setCrmOrderNumber(orderRequisitionData.getOrderCode());
                                            if (orderRequisitionDao.findOrderByCode(orderRequisitionData.getOrderCode()) != null) {
                                                OrderModel orderModel = orderRequisitionDao.findOrderByCode(orderRequisitionData.getOrderCode());
                                                if (orderModel.getEntries() != null) {
                                                    orderRequisitionModel.setOrderEntry(orderModel.getEntries().get(0));
                                                }
                                                orderRequisitionModel.setStatus(RequisitionStatus.ORDER_PLACED);

                                                if (orderModel.getCreationtime() != null) {
                                                    orderRequisitionModel.setApprovedDate(orderModel.getCreationtime());
                                                }
                                           }
                                        }
                                    }
                                }
                            }
                            if (requisitionAction.equals(RequisitionAction.REJECT)) {
                                orderRequisitionModel.setRequisitionAction(RequisitionAction.REJECT);
                                orderRequisitionModel.setStatus(RequisitionStatus.REJECTED);
                                orderRequisitionModel.setRejectedDate(new Date());

                                if (liftingDate.before(date)) {
                                    orderRequisitionModel.setRequisitionType(RequisitionType.LIFTING);
                                    orderRequisitionModel.setLiftingDate(liftingDate);
                                    if (Objects.nonNull(orderRequisitionData.getToCustomerUid())) {
                                        toCustomerModel = (SclCustomerModel) userService.getUserForUID(orderRequisitionData.getToCustomerUid());
                                        orderRequisitionModel.setToCustomer(toCustomerModel);
                                        if (toCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                                            orderRequisitionModel.setRequisitionFor(RequisitionFor.RETAILER);
                                        }
                                       else if (toCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID))) {
                                            orderRequisitionModel.setRequisitionFor(RequisitionFor.INFLUENCER);
                                        }
                                    }
                                } else {
                                    orderRequisitionModel.setRequisitionType(RequisitionType.ORDER);
                                    if (Objects.nonNull(orderRequisitionData.getToCustomerUid())) {
                                        toCustomerModel = (SclCustomerModel) userService.getUserForUID(orderRequisitionData.getToCustomerUid());
                                        if (toCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                                            orderRequisitionModel.setToCustomer(toCustomerModel);
                                            orderRequisitionModel.setRequisitionFor(RequisitionFor.RETAILER);
                                        }
                                    }
                                }
                            }
                        }
                        else if (fromCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                            if (requisitionAction.equals(RequisitionAction.CREATE) && Objects.isNull(orderRequisitionData.getRequisitionId())) {
                                orderRequisitionModel.setStatus(RequisitionStatus.PENDING_CONFIRMATION);
                                orderRequisitionModel.setFromCustomer(fromCustomerModel);

                                if (toCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                                    orderRequisitionModel.setToCustomer(toCustomerModel);
                                    orderRequisitionModel.setRequisitionFor(RequisitionFor.RETAILER);
                                }
                                else if (toCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID))) {
                                    orderRequisitionModel.setToCustomer(toCustomerModel);
                                    orderRequisitionModel.setRequisitionFor(RequisitionFor.INFLUENCER);
                                }
                                orderRequisitionModel.setRequisitionDate(new Date());
                                if (orderRequisitionData.getProductCode() != null && !(orderRequisitionData.getProductCode().isEmpty())) {
                                    if (baseSiteService.getCurrentBaseSite() != null) {
                                        CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
                                        ProductModel product = productService.getProductForCode(catalogVersion, orderRequisitionData.getProductCode());
                                        orderRequisitionModel.setProduct(product);
                                    }
                                }
                                orderRequisitionModel.setAliasCode(orderRequisitionData.getAliasCode());
                                orderRequisitionModel.setEquivalenceProductCode(orderRequisitionData.getEquivalenceProductCode());
                                /*if(orderRequisitionData.getPlacedById()!=null){
                                    SclCustomerModel sclCustomerModel = (SclCustomerModel) userService.getUserForUID(fromCustomerModel.getUid());
                                    orderRequisitionModel.setPlacedByName(sclCustomerModel.getName());
                                }*/
                                if (orderRequisitionData.getQuantityInBags() != 0.0) {
                                    orderRequisitionModel.setQuantity(orderRequisitionData.getQuantityInBags() / 20);
                                    orderRequisitionModel.setQuantityInBags(orderRequisitionData.getQuantityInBags());
                                } else {
                                    orderRequisitionModel.setQuantity(0.0);
                                    orderRequisitionModel.setQuantityInBags(0.0);
                                }
                                if(Objects.nonNull(liftingDate)){
                                if (liftingDate.before(date)) {
                                    orderRequisitionModel.setRequisitionType(RequisitionType.LIFTING);
                                    orderRequisitionModel.setLiftingDate(liftingDate);
                                } else {
                                    orderRequisitionModel.setRequisitionType(RequisitionType.ORDER);
                                    if (orderRequisitionData.getExpectedDeliverySlot() != null && !(orderRequisitionData.getExpectedDeliverySlot().isEmpty())) {
                                        if (!Objects.isNull(DeliverySlots.valueOf(orderRequisitionData.getExpectedDeliverySlot()))) {
                                            orderRequisitionModel.setExpectedDeliverySlot(deliverySlotMasterDao.findByCentreTime(orderRequisitionData.getExpectedDeliverySlot()));
                                        }
                                    }

                                    if (orderRequisitionData.getExpectedDeliveryDate() != null && !(orderRequisitionData.getExpectedDeliveryDate().isEmpty())) {
                                        try {
                                            orderRequisitionModel.setExpectedDeliveryDate(dateFormat.parse(orderRequisitionData.getExpectedDeliveryDate()));
                                        } catch (ParseException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                    if (!Objects.isNull(orderRequisitionData.getDeliveryAddress())) {
                                        if (orderRequisitionData.getDeliveryAddress().getId() != null && !orderRequisitionData.getDeliveryAddress().getId().isEmpty() && !Objects.isNull(sclUserDao.getAddressByPk(orderRequisitionData.getDeliveryAddress().getId()))) {
                                            //                orderRequisitionModel.setDeliveryAddress(sclUserDao.getAddressByErpId(orderRequisitionData.getDeliveryAddress().getErpId(),retailer));
                                            orderRequisitionModel.setDeliveryAddress(sclUserDao.getAddressByPk(orderRequisitionData.getDeliveryAddress().getId()));
                                        }
                                    }
                                    orderRequisitionModel.setShipToParty(orderRequisitionData.getShipToParty());
                                    orderRequisitionModel.setRequisitionType(RequisitionType.ORDER);
                                    if (toCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                                            orderRequisitionModel.setToCustomer(toCustomerModel);
                                            orderRequisitionModel.setRequisitionFor(RequisitionFor.RETAILER);
                                        }

                                    if (orderRequisitionData.getOrderCode() != null && !orderRequisitionData.getOrderCode().isEmpty()) {
                                        orderRequisitionModel.setCrmOrderNumber(orderRequisitionData.getOrderCode());
                                        if (orderRequisitionDao.findOrderByCode(orderRequisitionData.getOrderCode()) != null) {
                                            OrderModel orderModel = orderRequisitionDao.findOrderByCode(orderRequisitionData.getOrderCode());
                                            if (orderModel.getEntries() != null) {
                                                orderRequisitionModel.setOrderEntry(orderModel.getEntries().get(0));
                                            }
                                            orderRequisitionModel.setStatus(RequisitionStatus.ORDER_PLACED);

                                            if (orderModel.getCreationtime() != null) {
                                                orderRequisitionModel.setApprovedDate(orderModel.getCreationtime());
                                            }
                                        }
                                      }
                                    }
                                 }
                            }
                            else if(requisitionAction.equals(RequisitionAction.REJECT)) {
                                orderRequisitionModel.setRequisitionAction(RequisitionAction.REJECT);
                                orderRequisitionModel.setStatus(RequisitionStatus.REJECTED);
                                orderRequisitionModel.setRejectedDate(new Date());
                                if (Objects.nonNull(liftingDate)) {
                                    if (liftingDate.before(date)) {
                                        orderRequisitionModel.setRequisitionType(RequisitionType.LIFTING);
                                        orderRequisitionModel.setLiftingDate(liftingDate);
                                            if (toCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID))) {
                                                orderRequisitionModel.setRequisitionFor(RequisitionFor.INFLUENCER);
                                                orderRequisitionModel.setToCustomer(toCustomerModel);
                                            }
                                       }
                                }
                            }
                            //need to check
                            else if(requisitionAction.equals(RequisitionAction.APPROVE)){

                            }
                        }
                        //need to check
                        else if (fromCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID))) {
                            if(Objects.nonNull(liftingDate)) {
                                if (liftingDate.before(date)) {
                                    orderRequisitionModel.setStatus(RequisitionStatus.PENDING_CONFIRMATION);
                                    orderRequisitionModel.setRequisitionFor(RequisitionFor.RETAILER);
                                }
                            }
                            /*if(orderRequisitionData.getPlacedById()!=null){
                                SclCustomerModel sclCustomerModel = (SclCustomerModel) userService.getUserForUID(fromCustomerModel.getUid());
                                orderRequisitionModel.setPlacedByName(sclCustomerModel.getName());
                            }*/
                    }
                }
                orderRequisitionModel.setActive(true);
                } catch (ParseException e) {
                    LOGGER.error("not able to parse the date :" + liftingDate + " " + e.getMessage() + " " + e.getCause());
                }
            if (Objects.nonNull(orderRequisitionData.getToCustomerUid()) && territoryManagementService.getTerritoriesForCustomer(orderRequisitionData.getToCustomerUid()) != null && !(territoryManagementService.getTerritoriesForCustomer(orderRequisitionData.getToCustomerUid()).isEmpty())) {

                SubAreaMasterModel subAreaMaster = territoryManagementService.getTerritoriesForCustomer(orderRequisitionData.getToCustomerUid()).get(0);
                if (subAreaMaster != null) {
                    orderRequisitionModel.setSubAreaMaster(subAreaMaster);
                    DistrictMasterModel district = subAreaMaster.getDistrictMaster();
                    if (district != null) {
                        orderRequisitionModel.setDistrictMaster(district);
                        RegionMasterModel region = district.getRegion();
                        if (region != null) {
                            orderRequisitionModel.setRegionMaster(region);
                        }
                    }

                }
            }
            //need to be checked
            if(Objects.nonNull(orderRequisitionModel.getFromCustomer())) {
                TerritoryMasterModel territoryCode = orderRequisitionModel.getFromCustomer().getTerritoryCode();
                orderRequisitionModel.setTerritoryCode(territoryCode);
            }
               //saveDealerRetailerMapping((SclCustomerModel) userService.getUserForUID(orderRequisitionData.getFromCustomerUid()), (SclCustomerModel) userService.getUserForUID(orderRequisitionData.getToCustomerUid()), brand);




            orderRequisitionModel.setSaleSummaryJobStatus("N");
            if (orderRequisitionData.getInvoiceNumber() != null && orderRequisitionData.getLiftingDate()!=null ) {
                synchronized (this) {
                    if (updateMasterStockAllocation(orderRequisitionData, orderRequisitionModel)) {
                        LOGGER.info("Saving Order Req Model");
                        modelService.save(orderRequisitionModel);
                        sendNotification(orderRequisitionData, orderRequisitionModel);

                    } else {
                        LOGGER.info("Removing Order Req Model");
                        modelService.remove(orderRequisitionModel);
                        modelService.refresh(orderRequisitionModel);
                    }
                }
            }else{

                modelService.save(orderRequisitionModel);
                modelService.refresh(orderRequisitionModel);
            }
            if(orderRequisitionModel!=null){
                Map<String, Object> resultMaxRetailer = salesPerformanceDao.findMaxInvoicedDateAndQuantityForRetailer(orderRequisitionModel.getToCustomer(),null);
                SclCustomerModel sclCustomer = orderRequisitionModel.getToCustomer();
                if (resultMaxRetailer.get("liftingDate") != null) {
                    String dateString = (String) resultMaxRetailer.get("liftingDate");
                    String pattern = "yyyy-MM-dd HH:mm";
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                    try {
                        LocalDate date1 = LocalDate.parse(dateString, formatter);
                        Date lastLiftingDate = Date.from(date1.atStartOfDay(ZoneId.systemDefault()).toInstant());
                        sclCustomer.setLastLiftingDate(lastLiftingDate);
                        LOGGER.info(String.format("Last Lifting Date for Retailer:%s:::Date:%s", sclCustomer.getUid(), lastLiftingDate));
                    } catch (DateTimeParseException e) {
                        LOGGER.info(String.format("Parse Exception handled in cron job last lifting date Cause:%s :: Message ::%s", e.getCause(), e.getMessage()));
                    }
                    double qty = (double) resultMaxRetailer.get("liftingQty");
                    sclCustomer.setLastLiftingQuantity(qty);
                    if (sclCustomer.getLastLiftingDate() != null) {
                        if (oneMonthDate.compareTo(sclCustomer.getLastLiftingDate()) < 0)
                            sclCustomer.setNetworkType(NetworkType.ACTIVE.getCode());
                        else if (threeMonthDate.compareTo(sclCustomer.getLastLiftingDate()) < 0)
                            sclCustomer.setNetworkType(NetworkType.INACTIVE.getCode());
                        else
                            sclCustomer.setNetworkType(NetworkType.DORMANT.getCode());
                    } else {
                        sclCustomer.setNetworkType(NetworkType.DORMANT.getCode());
                    }
                    modelService.save(sclCustomer);
                    modelService.refresh(orderRequisitionModel);
                }
            }
            return true;
        }catch(Exception e){
            LOGGER.error("not able to parse the date :" + e.getMessage() + " " + e.getCause());
            return false;
        }
    }

    //Note:- this has to be updated for approve,reject,expired cases in next release after confirmation from BA/client
    private void sendNotification(OrderRequisitionData orderRequisitionData, OrderRequisitionModel orderRequisitionModel) {
        try {
            SclCustomerModel dealer = (SclCustomerModel) userService.getUserForUID(orderRequisitionData.getFromCustomerUid());
            SclCustomerModel retailer = (SclCustomerModel) userService.getUserForUID(orderRequisitionData.getToCustomerUid());
            SclUserModel so = territoryManagementService.getSOforCustomer((SclCustomerModel) orderRequisitionModel.getFromCustomer());
            String liftingAllocationMsg=dataConstraintDao.findVersionByConstraintName("LIFTING_ALLOCATION_MSG");
            String liftingAllocationSubject=dataConstraintDao.findVersionByConstraintName("LIFTING_ALLOCATION_SUB");
            StringBuilder builder = new StringBuilder();
            builder.append( liftingAllocationMsg +" "+ retailer.getName() + " , " + retailer.getUid());
            builder.append(" with requisition number  " + orderRequisitionModel.getRequisitionId());
            builder.append(" of " + orderRequisitionModel.getProduct().getName() + ", " + orderRequisitionModel.getQuantity() + " MT to " + orderRequisitionModel.getFromCustomer().getName() + "," + orderRequisitionModel.getFromCustomer().getUid());
            builder.append(" on " + orderRequisitionModel.getRequisitionDate());
            String body = builder.toString();
            String sub = liftingAllocationSubject;
            sclNotificationService.submitOrderRequisitionNotification(orderRequisitionModel, orderRequisitionModel.getFromCustomer(), body, sub, NotificationCategory.ORDER_REQUISITION_CONFIRMATION);
            if (orderRequisitionModel.getQuantity() > 1000) {
                sclNotificationService.submitOrderRequisitionNotification(orderRequisitionModel, so, body, sub, NotificationCategory.ORDER_REQUISITION_CONFIRMATION);
            }
        } catch (Exception e) {
            LOGGER.error("Error while sending Requisition placed by retailer");
        }
    }

    //To get the stock available for Retailer
	private int getStockAvailForRetailer(int receipt, int saleToRetailer, int saleToInfluencer) {
		int stockRetailer = 0;
		stockRetailer = Math.abs(receipt - saleToRetailer - saleToInfluencer);
		return stockRetailer;
	}
    
	//To calculate the stock for Influencer when requisition placed
	private int getStockAvailForInfluencer(int receipt, int saleToRetailer, int saleToInfluencer) {
		int stockInfluencer = 0;
		stockInfluencer = (int) ((0.7 * (receipt - saleToRetailer)) - saleToInfluencer);
		return stockInfluencer;
	}
		private boolean updateMasterStockAllocation(OrderRequisitionData orderRequisitionData,OrderRequisitionModel orderRequisitionModel) {
        boolean deductedFlag=false;
        try {
            double balanceQtyInBags = 0.0;
            MasterStockAllocationModel masterModel = orderRequisitionDao.getMasterAllocationEntry(orderRequisitionData);

            if (Objects.nonNull(orderRequisitionModel) && orderRequisitionModel.getRequisitionId() != null) {
                if (masterModel != null) {
                    if (masterModel.getBalanceQtyInBags() != null && orderRequisitionModel.getQuantityInBags() != null &&
                            masterModel.getBalanceQtyInBags() >= orderRequisitionModel.getQuantityInBags()) {
                        LOGGER.info(String.format("Allocating %s Quantity From Invoice ID:- %s", orderRequisitionModel.getQuantityInBags(), orderRequisitionData.getInvoiceNumber()));
                        balanceQtyInBags = masterModel.getBalanceQtyInBags() - orderRequisitionModel.getQuantityInBags();
                        masterModel.setBalanceQtyInMt(balanceQtyInBags / 20);
                        masterModel.setBalanceQtyInBags(balanceQtyInBags);

                        orderRequisitionModel.setInvoiceNumber(masterModel.getTaxInvoiceNumber());
                        modelService.save(masterModel);
                        deductedFlag=true;
                    }else{
                        LOGGER.info("Quantity requested more than the balance quantity. So Declined the request");
                        LOGGER.info(" Balance Qty in Master(Bags):"+masterModel.getBalanceQtyInBags()+": Request Qty:"+ orderRequisitionModel.getQuantityInBags());
                        deductedFlag=false;
                    }
                }else{
                    LOGGER.info("No Master Allocation Entry found for invoice id: "+orderRequisitionData.getInvoiceNumber());
                    deductedFlag=false;
                }
            }
        }catch(Exception e){
                LOGGER.error("Exception when update Master Stock Balance Qty:"+e.getMessage());
                deductedFlag=false;
        }
        return deductedFlag;
        }
    @Override
    public SearchPageData<OrderRequisitionModel> getOrderRequisitionDetails(String statuses, String submitType, Integer fromMonth, Integer fromYear, Integer toMonth, Integer toYear, SclCustomerModel currentUser, String productCode, SearchPageData searchPageData, String requisitionId, String searchKey) {
        String fromDate = null;
        String toDate = null;
        String statues = validateAndMapOrderStatuses(statuses);
        LOGGER.info(String.format("getOrderHistoryForOrderRequisition Statuses::%s for user ::%s",statues,currentUser));
        final BaseStoreModel currentBaseStore = baseStoreService.getCurrentBaseStore();
        final Set<RequisitionStatus> statusSet = extractOrderStatuses(statues);

        if(fromMonth!=null && fromMonth!=0) {
        	int fYear = Integer.parseInt(fromYear.toString());
        	int fMonth = Integer.parseInt(fromMonth.toString());

//        	if(submitType.equals("Draft") || submitType.equals("draft")) {
//        		if(fMonth >= 1 && fMonth <=12) {
//        			if(fMonth == 12) {
//        				tMonth = 1;
//        				tYear = fYear + 1;
//        			}
//        			else if(fMonth<12) {
//        				tMonth = fMonth + 1;
//        			}
//        		}
//        	}

        	String singleDigitFromMonth = Integer.toString(fYear)+"-0"+Integer.toString(fMonth)+"-"+"%";
        	String doubleDigitFromMonth = Integer.toString(fYear)+"-"+Integer.toString(fMonth)+"-"+"%";

        	fromDate = (fMonth>=1 && fMonth<=9) ? singleDigitFromMonth : doubleDigitFromMonth;
        }
        return orderRequisitionDao.getOrderRequisitionDetails(statusSet.toArray(new RequisitionStatus[statusSet.size()]), submitType, fromDate, currentUser, productCode, searchPageData, requisitionId, searchKey);
    }

    @Override
    public void saveDealerRetailerMapping(SclCustomerModel dealer, SclCustomerModel retailer, BaseSiteModel brand) {
        DealerRetailerMappingModel dealerRetailerMapModel = orderRequisitionDao.getDealerforRetailerDetails(dealer, retailer, brand);
        if(Objects.isNull(dealerRetailerMapModel)) {
            dealerRetailerMapModel = modelService.create(DealerRetailerMappingModel.class);
            dealerRetailerMapModel.setDealer(dealer);
            dealerRetailerMapModel.setRetailer(retailer);
            dealerRetailerMapModel.setBrand(brand);
           dealerRetailerMapModel.setActive(true);
            modelService.save(dealerRetailerMapModel);
        }
        else if(!dealerRetailerMapModel.getActive()) {
            dealerRetailerMapModel.setActive(true);
            modelService.save(dealerRetailerMapModel);
        }
    }

    @Override
    public void orderCountIncrementForDealerRetailerMap(Date deliveredDate,SclCustomerModel dealer, SclCustomerModel retailer, BaseSiteModel brand) {
        DealerRetailerMappingModel dealerRetailerMapModel = orderRequisitionDao.getDealerforRetailerDetails(dealer, retailer, brand);
        if(!Objects.isNull(dealerRetailerMapModel)) {
            dealerRetailerMapModel.setOrderCount(dealerRetailerMapModel.getOrderCount()+1);
            SclCustomerModel retailerModel = dealerRetailerMapModel.getRetailer();
            retailerModel.setLastLiftingDate(deliveredDate);
            retailerModel.setNetworkType(NetworkType.ACTIVE.getCode());
            modelService.save(retailerModel);
            dealerRetailerMapModel.setLastLiftingDate(deliveredDate);
            modelService.save(dealerRetailerMapModel);
        }
    }

    @Override
    public Boolean updateOrderRequistionStatus(String requisitionId, String status, Double receivedQty, String cancelReason) {
        OrderRequisitionModel model = orderRequisitionDao.findByRequisitionId(requisitionId);
        if(status!=null && status.equals("DELIVERED")){
            if(model.getStatus()!=null && model.getStatus().equals(RequisitionStatus.PENDING_DELIVERY)){
                Date deliveryDate = new Date();

                model.setStatus(RequisitionStatus.DELIVERED);
                model.setDeliveredDate(deliveryDate);
                model.setReceivedQty(receivedQty);

                modelService.save(model);
                
                SclCustomerModel currentUser = (SclCustomerModel) userService.getCurrentUser();
                LOGGER.info("1. Retailer RECEIPT:::Record found--- Requisition Status... " + model.getStatus()
    					+ " Current customer No -->" + currentUser.getCustomerNo());
				if (null != model.getFromCustomer() && null != currentUser) {
					updateRetailerReceipts(model.getProduct(), model.getToCustomer(), receivedQty);
				}
            }
            else{
                throw new UnsupportedOperationException();
            }
        }
        else if(status!=null && status.equals("CANCELLED")){
            if(model.getStatus()!=null && model.getStatus().equals(RequisitionStatus.PENDING_CONFIRMATION)){
                Date cancelledDate = new Date();
                SclCustomerModel currentUser = (SclCustomerModel) userService.getCurrentUser();

                model.setStatus(RequisitionStatus.CANCELLED);
                model.setCancelledDate(cancelledDate);
                model.setCancelReason(cancelReason);
                model.setCancelledBy(currentUser);

                modelService.save(model);
            }
            else{
                throw new UnsupportedOperationException();
            }
        }
        else if(status!=null && status.equals("DELETE")){
                model.setActive(false);
                modelService.save(model);
        }
        else{
            throw new UnsupportedOperationException();
        }
        return true;
    }
    protected Set<RequisitionStatus> extractOrderStatuses(final String statuses)
    {
        final String[] statusesStrings = statuses.split(SclCoreConstants.ORDER.ENUM_VALUES_SEPARATOR);

        final Set<RequisitionStatus> statusesEnum = new HashSet<>();
        for (final String status : statusesStrings)
        {
            statusesEnum.add(RequisitionStatus.valueOf(status));
        }
        return statusesEnum;
    }

    /**
     *
     * @param searchPageData
     * @param orderStatus
     * @param filter
     * @param productName
     * @param requestType
     * @param submitType
     * @param fromMonth
     * @param fromYear
     * @param toMonth
     * @param toYear
     * @param requisitionId
     * @return
     */
    @Override
    public SearchPageData<OrderRequisitionModel> getOrderHistoryForOrderRequisition(SearchPageData searchPageData,String orderStatus, String filter,String productName,String requestType,String submitType,
                                                                                   Integer fromMonth, Integer fromYear, Integer toMonth, Integer toYear, String requisitionId) {
        final SclCustomerModel currentUser = (SclCustomerModel) userService.getCurrentUser();
        String statues = validateAndMapOrderStatuses(orderStatus);
        LOGGER.info(String.format("getOrderHistoryForOrderRequisition Statuses::%s for user ::%s", statues, currentUser));
        final BaseStoreModel currentBaseStore = baseStoreService.getCurrentBaseStore();
        final Set<RequisitionStatus> statusSet = extractOrderStatuses(statues);

        String fromDate = StringUtils.EMPTY;
        String toDate = StringUtils.EMPTY;;
        if (fromMonth != null && fromMonth != 0) {
            int fYear = Integer.parseInt(fromYear.toString());
            int fMonth = Integer.parseInt(fromMonth.toString());

            String singleDigitFromMonth = Integer.toString(fYear) + "-0" + Integer.toString(fMonth) + "-" + "%";
            String doubleDigitFromMonth = Integer.toString(fYear) + "-" + Integer.toString(fMonth) + "-" + "%";

            fromDate = (fMonth >= 1 && fMonth <= 9) ? singleDigitFromMonth : doubleDigitFromMonth;
        }

        SearchPageData<OrderRequisitionModel> searchResult = null;
        try {
            searchResult = orderRequisitionDao.getOrderRequisitionDetails(statusSet.toArray(new RequisitionStatus[statusSet.size()]), submitType, fromDate, currentUser, productName, searchPageData, requisitionId, filter);

        } catch (Exception ex) {

            LOGGER.error(String.format("Exception occur in order entries list for so::%s and get cause::%s", ex.getMessage(), ex.getCause()));
        }
        return searchResult;
    }

    @Override
    public String validateAndMapOrderStatuses(final String inputStatus){
        String statuses;
        switch(inputStatus){
            case SclCoreConstants.ORDER.PENDING_ORDER_REQUISITION:
                statuses = configurationService.getConfiguration().getString(SclCoreConstants.ORDER.PENDING_FOR_OR_STATUS_MAPPING);
                break;

            case SclCoreConstants.ORDER.APPROVED_ORDER_REQUISITION:
                statuses = configurationService.getConfiguration().getString(SclCoreConstants.ORDER.APPROVED_FOR_OR_STATUS_MAPPING);
                break;

            case SclCoreConstants.ORDER.EXPIRED_ORDER_REQUISITION:
                statuses = configurationService.getConfiguration().getString(SclCoreConstants.ORDER.EXPIRED_FOR_OR_STATUS_MAPPING);
                break;

            case SclCoreConstants.ORDER.REJECTED_ORDER_REQUISITION:
                statuses = configurationService.getConfiguration().getString(SclCoreConstants.ORDER.REJECTED_FOR_OR_STATUS_MAPPING);
                break;

            default :
                statuses = inputStatus;
        }
        return statuses;
    }


    //To update the quantity as receipts for allocation calculation
  	private void updateRetailerReceipts(ProductModel productCode, SclCustomerModel dealerCode, Double receivedQuantity) {
  		RetailerRecAllocateModel receiptRetailerAllocate = dealerDao.getRetailerAllocation(productCode, dealerCode);
  		if (null != receiptRetailerAllocate) {
            int receiptQty = receiptRetailerAllocate.getReceipt()!=null ? receiptRetailerAllocate.getReceipt() : 0;
  			LOGGER.info("1. Retailer RECEIPT:::Record found--- Receipts for Dealer " + receiptRetailerAllocate.getReceipt()
  			+ " Dealer No -->" + receiptRetailerAllocate.getDealerCode()
  			+ " Available stock for influencer -->" + receiptRetailerAllocate.getStockAvlForInfluencer()
  			+ " Available allocated or sales to influencer -->" + receiptRetailerAllocate.getSalesToInfluencer());
//  			Double updatedQty = receiptRetailerAllocate.getReceipt() - (receivedQuantity * SclCoreConstants.QUANTITY_INMT_TO_BAGS);
  			//receiptRetailerAllocate.setReceipt((null != updatedQty)?updatedQty.intValue():0);
  			receiptRetailerAllocate.setSalesToInfluencer((new Double(receivedQuantity)).intValue());
  			int stockRetailerToInfluencer = (int) ((1.0 * (receiptQty	- receiptRetailerAllocate.getSalesToInfluencer())));
  			receiptRetailerAllocate.setStockAvlForInfluencer(stockRetailerToInfluencer);
              receiptRetailerAllocate.setMonth(LocalDate.now().getYear());
              receiptRetailerAllocate.setYear(LocalDate.now().getMonthValue());
  			LOGGER.info("2. Retailer RECEIPT:::Updated " + receiptQty
  			+ " Available stock for influencer -->" + receiptRetailerAllocate.getStockAvlForInfluencer()
  			+ " Allocated or sales to influencer -->" + receiptRetailerAllocate.getSalesToInfluencer());
  			modelService.save(receiptRetailerAllocate);
  		} else {
  			//If product and dealer is not found in the RetailerRecAllocate 
  			//then it means new entry has to be made as orderrequisition is placed with this combination
            Double updatedQty = receivedQuantity;

  			RetailerRecAllocateModel receiptRetailerAllocateNew = modelService.create(RetailerRecAllocateModel.class);
            int retailerReceiptQty  = (null != updatedQty)?updatedQty.intValue():0;
  			receiptRetailerAllocateNew.setProduct(productCode.getPk().toString());
  			receiptRetailerAllocateNew.setDealerCode(dealerCode.getPk().toString());

  			receiptRetailerAllocateNew.setReceipt(retailerReceiptQty);
  			receiptRetailerAllocateNew.setSalesToInfluencer(0);
  			int stockRetailerInfluencer = (int) ((1.0 * (retailerReceiptQty - receiptRetailerAllocateNew.getSalesToInfluencer())));
  			receiptRetailerAllocateNew.setStockAvlForInfluencer(stockRetailerInfluencer);
              receiptRetailerAllocateNew.setYear(LocalDate.now().getYear());
              receiptRetailerAllocateNew.setMonth(LocalDate.now().getMonthValue());
  			modelService.save(receiptRetailerAllocateNew);
  			modelService.refresh(receiptRetailerAllocateNew);
  		}
  	}

    @Override
    public List<OrderRequisitionModel> getSalesVisibilityForDealersAndRetailersFromOrmService(SclCustomerModel raisedByCustomer,SclCustomerModel raisedToCustomer, String fromDate, String toDate, String filter){
        List<OrderRequisitionModel> searchResult= orderRequisitionDao.getSalesDetailsForDealerOfRetailersFromOrmDao(raisedByCustomer, raisedToCustomer, fromDate, toDate,filter);
        return searchResult;
    }

    @Override
    public List<MasterStockAllocationModel> getSalesVisibilityForDealersAndRetailersFromMsaService(SclCustomerModel raisedByCustomer,SclCustomerModel raisedToCustomer, String fromDate, String toDate, String filter){
        List<MasterStockAllocationModel> searchResult= orderRequisitionDao.getSalesDetailsForDealerOfRetailersFromMsaDao(raisedByCustomer, raisedToCustomer, fromDate, toDate,filter);
        return searchResult;
    }

}
