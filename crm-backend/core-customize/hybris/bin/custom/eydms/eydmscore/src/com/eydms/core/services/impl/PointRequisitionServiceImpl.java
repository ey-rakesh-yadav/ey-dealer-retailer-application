package com.eydms.core.services.impl;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.dao.DealerDao;
import com.eydms.core.dao.PointRequisitionDao;
import com.eydms.core.dao.TerritoryManagementDao;
import com.eydms.core.enums.*;
import com.eydms.core.model.*;
import com.eydms.core.notifications.service.EyDmsNotificationService;
import com.eydms.core.services.PointRequisitionService;
import com.eydms.core.services.SchemesAndDiscountService;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.facades.data.PointRequisitionData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;
import de.hybris.platform.webservicescommons.dto.error.ErrorWsDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class PointRequisitionServiceImpl implements PointRequisitionService {

    private static final Logger LOG = Logger.getLogger(PointRequisitionServiceImpl.class);

    @Resource
    KeyGenerator pointRequisitionIdGenerator;

    @Resource
    ModelService modelService;

    @Autowired
    TerritoryManagementDao territoryManagementDao;

    @Autowired
    EyDmsNotificationService eydmsNotificationService;

    @Resource
    PointRequisitionDao pointRequisitionDao;

    @Resource
	DealerDao dealerDao;
    
    @Resource
    CatalogVersionService catalogVersionService;

    @Resource
    ProductService productService;

    @Resource
    BaseSiteService baseSiteService;

    @Resource
    UserService userService;

    @Resource
    TerritoryManagementService territoryManagementService;

    @Resource
    SchemesAndDiscountService schemesAndDiscountService;

    @Autowired
    SessionService sessionService;


    @Autowired
    private SearchRestrictionService searchRestrictionService;

    @Override
    public String saveInfluencerPointRequisitionDetails(PointRequisitionData pointRequisitionData) {
       // final ErrorListWsDTO errorListWsDTO = new ErrorListWsDTO();
      //  final List<ErrorWsDTO> errorWsDTOList = new ArrayList<>();
    	boolean isDealerRetailer = false;
        PointRequisitionModel pointRequisitionModel = null;
        if(Objects.nonNull(pointRequisitionData)) {
            if (pointRequisitionData.getRequisitionId() != null) {
                pointRequisitionModel = pointRequisitionDao.findByRequisitionId(pointRequisitionData.getRequisitionId());
            } else {
                pointRequisitionModel = modelService.create(PointRequisitionModel.class);
                pointRequisitionModel.setRequisitionId(pointRequisitionIdGenerator.generate().toString());
            }

            pointRequisitionModel.setClientName(pointRequisitionData.getClientName());

            CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
            ProductModel product = productService.getProductForCode(catalogVersion, pointRequisitionData.getProductCode());
            pointRequisitionModel.setProduct(product);
            if (pointRequisitionData.getQuantity() != null) {
                pointRequisitionModel.setQuantity(pointRequisitionData.getQuantity().doubleValue());
            }
            pointRequisitionModel.setPoints(pointRequisitionData.getPointsEarned());
            pointRequisitionModel.setPointsPerBag(pointRequisitionData.getPointsPerBag());

            double totalPoints = 0.0;
            if (pointRequisitionData.getPointsEarned() != 0.0) {
                totalPoints += pointRequisitionData.getPointsEarned();
                pointRequisitionModel.setTotalPoints(totalPoints);
            }

            //dealer or retailer or influencer
            EyDmsCustomerModel eydmsCustomer = (EyDmsCustomerModel) userService.getCurrentUser();
            if (Objects.nonNull(eydmsCustomer)) {
                pointRequisitionModel.setRequestRaisedBy(eydmsCustomer);
                if(eydmsCustomer.getCounterType()!=null && (eydmsCustomer.getCounterType().equals(CounterType.DEALER) || eydmsCustomer.getCounterType().equals(CounterType.RETAILER))) {
                	isDealerRetailer = true;
                }
            }
            //dealer or retailer
            if (pointRequisitionData.getRequestRaisedTo() != null) {
                EyDmsCustomerModel dealerOrRetailer = (EyDmsCustomerModel) userService.getUserForUID(pointRequisitionData.getRequestRaisedTo());
                if (!Objects.isNull(dealerOrRetailer)) {
                    pointRequisitionModel.setRequestRaisedTo(dealerOrRetailer);
                }
            }
            //influencer
            if (pointRequisitionData.getRequestRaisedFor() != null) {
                EyDmsCustomerModel influencer = (EyDmsCustomerModel) userService.getUserForUID(pointRequisitionData.getRequestRaisedFor());
                if (!Objects.isNull(influencer)) {
                    pointRequisitionModel.setRequestRaisedFor(influencer);

                    if(product!=null && influencer.getInfluencerType()!=null && influencer.getState()!=null && influencer.getCounterType()!=null) {
                        GiftSchemeModel scheme = schemesAndDiscountService.getCurrentSchemesByGeography(influencer.getState(), influencer.getInfluencerType().getCode());
                        if(!Objects.isNull(scheme)) {
                            ProductPointMasterModel productPointMasterModel  = pointRequisitionDao.getPointsForRequisition(product, scheme.getSchemeId());
                            if(productPointMasterModel!=null && productPointMasterModel.getSchemeId()!=null) {
                                pointRequisitionModel.setSchemeId(productPointMasterModel.getSchemeId());
                            }
                        }
                    }
                }
            }

                AddressData address = pointRequisitionData.getDeliveryAddress();
                pointRequisitionModel.setAddressLine1(address.getLine1());
                pointRequisitionModel.setAddressLine2(address.getLine2());
                pointRequisitionModel.setState(address.getState());
                pointRequisitionModel.setDistrict(address.getDistrict());
                pointRequisitionModel.setTaluka(address.getTaluka());
                pointRequisitionModel.setCity(address.getErpCity());
                pointRequisitionModel.setPincode(address.getPostalCode());
                pointRequisitionModel.setPhoneNumber(address.getCellphone());

            pointRequisitionModel.setRequisitionType(PointRequisitionType.POINT);

            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            if (pointRequisitionData.getDeliveryDate() != null || !pointRequisitionData.getDeliveryDate().contains("")) {
                try {
                    pointRequisitionModel.setDeliveryDate(dateFormat.parse(pointRequisitionData.getDeliveryDate()));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
            if (pointRequisitionData.getDeliverySlot() != null || !pointRequisitionData.getDeliverySlot().contains(""))
                pointRequisitionModel.setDeliverySlot(DeliverySlots.valueOf(pointRequisitionData.getDeliverySlot()));

            if (pointRequisitionData.getIsModify().equals(true) && pointRequisitionData.getIsDraft().equals(true)) {
                pointRequisitionModel.setDraftModificationDate(new Date());
            } else if (pointRequisitionData.getIsDraft() != null && pointRequisitionData.getIsDraft().equals(true)) {
                pointRequisitionModel.setDraftCreationDate(new Date());
                pointRequisitionModel.setDraftModificationDate(new Date());
                pointRequisitionModel.setIsRequisitionPlaced(false);
                pointRequisitionModel.setStatus(PointRequisitionStatus.DRAFT);
            } else if (pointRequisitionData.getIsModify() != null && pointRequisitionData.getIsModify().equals(true)) {
                pointRequisitionModel.setReqModificationDate(new Date());
                pointRequisitionModel.setModificationComment(pointRequisitionData.getModificationComment());
            } else {
                pointRequisitionModel.setStatus(PointRequisitionStatus.PENDING);
                pointRequisitionModel.setIsRequisitionPlaced(true);
                pointRequisitionModel.setRequisitionCreationDate(new Date());
                pointRequisitionModel.setReqModificationDate(new Date());
            }

            if(Objects.nonNull(pointRequisitionData.getRequestRaisedFor())) {

                SubAreaMasterModel subAreaMaster = territoryManagementService.getTerritoriesForCustomer(pointRequisitionData.getRequestRaisedFor()).get(0);
                if(subAreaMaster != null) {
                    pointRequisitionModel.setSubAreaMaster(subAreaMaster);
                    DistrictMasterModel district = subAreaMaster.getDistrictMaster();
                    if(district!=null) {
                        pointRequisitionModel.setDistrictMaster(district);
                        RegionMasterModel region = district.getRegion();
                        if(region!=null) {
                            pointRequisitionModel.setRegionMaster(region);
                        }
                    }
                    pointRequisitionModel.setSubAreaMaster(subAreaMaster);
                }
            }
        }
        modelService.save(pointRequisitionModel);
        modelService.refresh(pointRequisitionModel);
        if(isDealerRetailer && pointRequisitionModel.getIsRequisitionPlaced()) {
        	updateAllocationRequestCards(pointRequisitionModel.getRequisitionId(), PointRequisitionStatus.APPROVED.getCode(), null);
        }
        
        if(!pointRequisitionData.getIsDraft() && !pointRequisitionData.getIsCancel() && !pointRequisitionData.getIsDelete()) {
            saveCustomerInfluencerMapping(pointRequisitionModel.getRequestRaisedTo(), pointRequisitionModel.getRequestRaisedFor(), baseSiteService.getCurrentBaseSite());
        }
        
        if (Objects.nonNull(pointRequisitionModel)) {
            return pointRequisitionModel.getRequisitionId();
        }
        //errorListWsDTO.setErrors(errorWsDTOList);
       //return errorListWsDTO;
        return null;
    }
    
    //To calculate the stock for Influencer when requisition placed
	private int getStockAvailForInfluencer(int receipt, int saleToRetailer, int saleToInfluencer) {
		int stockInfluencer = 0;
		stockInfluencer = Math.abs((int) ((0.7 * (receipt - saleToRetailer)) - saleToInfluencer));
		return stockInfluencer;
	}
	
	//To get the stock available for Retailer
	private int getStockAvailForRetailer(int receipt, int saleToRetailer, int saleToInfluencer) {
		int stockRetailer = 0;
		stockRetailer = Math.abs(receipt - saleToRetailer - saleToInfluencer);
		return stockRetailer;
	}

    @Override
    public Double getPointsForRequisition(String productCode, String influencerCode) {
    	double points =0.0;
        String schemeId = "";
    	EyDmsCustomerModel influencer = (EyDmsCustomerModel) userService.getUserForUID(influencerCode);
    	CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
    	ProductModel product = productService.getProductForCode(catalogVersion, productCode);
    	List<SubAreaMasterModel> subAreaMasterModelList = territoryManagementService.getTerritoriesForCustomer(influencer);

    	if (CollectionUtils.isNotEmpty(subAreaMasterModelList) && subAreaMasterModelList.get(0) != null) {
    		if (subAreaMasterModelList.get(0).getDistrict() != null) {
    			LOG.info("get points for requisition district:" + subAreaMasterModelList.get(0).getDistrict());
    			if(product!=null && influencer.getInfluencerType()!=null && influencer.getState()!=null && influencer.getCounterType()!=null) {
                    GiftSchemeModel scheme = schemesAndDiscountService.getCurrentSchemesByGeography(influencer.getState(),influencer.getInfluencerType().getCode());
                    if(!Objects.isNull(scheme)) {
                        schemeId = scheme.getSchemeId();
                    }
    				ProductPointMasterModel productPointMasterModel  = pointRequisitionDao.getPointsForRequisition(product, schemeId);
                    if(productPointMasterModel!=null && productPointMasterModel.getPoints()!=null) {
                        points = productPointMasterModel.getPoints();
                    }
    			}
    		}
    	}
    	return points;
    }
    
    @Override

    public Integer getAllocationRequestCount() {
        return pointRequisitionDao.getAllocationRequestCount();
    }

    @Override
    public List<PointRequisitionModel> getAllocationRequestList() {
        return pointRequisitionDao.getAllocationRequestList();
    }

    public SearchPageData<PointRequisitionModel> getListOfAllPointRequisition(boolean isDraft, String filter, List<String> statuses, SearchPageData searchPageData, String requisitionId, String influencerCode) {

        return pointRequisitionDao.getListOfAllPointRequisition(isDraft, filter, statuses,searchPageData, requisitionId, influencerCode);
    }

    public List<List<Object>> requisitionRaisedDetails(){

        EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();

        return pointRequisitionDao.requisitionRaisedDetails(currentUser);


    }

    @Override
    public Integer pendingRequistionsDetails() {
        EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();

        return pointRequisitionDao.pendingRequisitionDetails(currentUser);

    }

    @Override
    public Double pointsFromPreviousYear() {
        EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
        return pointRequisitionDao.pointsFromPreviousYear(currentUser);
    }

    @Override
    public Double pointsEarnedCurrentYear() {
        EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
        return pointRequisitionDao.pointsEarnedCurrentYear(currentUser);
    }

    @Override
    public Double pointsRedeemed() {
        EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
        return pointRequisitionDao.pointsRedeemed(currentUser);
    }

    @Override
    public Double totalRedeemablePoints() {
        EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
        return pointRequisitionDao.totalRedeemablePoints(currentUser);
    }

    @Override
    public List<GiftShopModel> giftShopSummary() {

        return pointRequisitionDao.giftShopSummary();

    }

    @Override
    public void saveCustomerInfluencerMapping(EyDmsCustomerModel fromCustomer, EyDmsCustomerModel influencer, BaseSiteModel brand) {
        CustomersInfluencerMapModel customersInfluencerMapModel = pointRequisitionDao.getInfluencersListForCustomers(fromCustomer, influencer, brand);

        if(Objects.isNull(customersInfluencerMapModel)) {
            customersInfluencerMapModel = modelService.create(CustomersInfluencerMapModel.class);

            if(fromCustomer.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                DealerInfluencerMapModel dealerInfluencerMapModel = modelService.create(DealerInfluencerMapModel.class);
                dealerInfluencerMapModel.setFromCustomer(fromCustomer);
                dealerInfluencerMapModel.setInfluencer(influencer);
                dealerInfluencerMapModel.setFromCustomerType("Dealer");
                dealerInfluencerMapModel.setActive(true);
                dealerInfluencerMapModel.setBrand(brand);
                modelService.save(dealerInfluencerMapModel);
            }

            else if (fromCustomer.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                RetailerInfluencerMapModel retailerInfluencerMapModel = modelService.create(RetailerInfluencerMapModel.class);
                retailerInfluencerMapModel.setFromCustomer(fromCustomer);
                retailerInfluencerMapModel.setInfluencer(influencer);
                retailerInfluencerMapModel.setFromCustomerType("Retailer");
                retailerInfluencerMapModel.setBrand(brand);
                retailerInfluencerMapModel.setActive(true);
                modelService.save(retailerInfluencerMapModel);
            }

//            customersInfluencerMapModel.setFromCustomer(fromCustomer);
//            customersInfluencerMapModel.setInfluencer(influencer);
//            customersInfluencerMapModel.setBrand(brand);
//            customersInfluencerMapModel.setActive(true);
//            modelService.save(customersInfluencerMapModel);
        }
        else if(!customersInfluencerMapModel.getActive()) {
            customersInfluencerMapModel.setActive(true);
            modelService.save(customersInfluencerMapModel);
        }

    }

    @Override
    public void orderCountIncrementForCustomerRetailerMap(Date deliveryDate,EyDmsCustomerModel fromCustomer, EyDmsCustomerModel influencer, BaseSiteModel brand,Double qty) {
        CustomersInfluencerMapModel customersInfluencerMapModel = pointRequisitionDao.getInfluencersListForCustomers(fromCustomer, influencer, brand);

        if(!Objects.isNull(customersInfluencerMapModel)) {
            if(customersInfluencerMapModel.getOrderCount()!=null) {
                customersInfluencerMapModel.setOrderCount(customersInfluencerMapModel.getOrderCount()+1);
            }
            customersInfluencerMapModel.setInfluencer(influencer);
            EyDmsCustomerModel influencer1 = customersInfluencerMapModel.getInfluencer();
            influencer1.setLastLiftingDate(deliveryDate);
            influencer1.setLastLiftingQuantity(qty);
            modelService.save(influencer1);
            customersInfluencerMapModel.setLastBagLiftedDate(deliveryDate);
            modelService.save(customersInfluencerMapModel);
        }
    }

    @Override
    public Double bagOffTake() {
        EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
        return pointRequisitionDao.bagOffTake(currentUser);
    }
    @Override
    public PointRequisitionData getRequistionDetails(String requisitionId) {
        PointRequisitionModel requistionDetails = pointRequisitionDao.getRequistionDetails(requisitionId);
        PointRequisitionData summaryData = new PointRequisitionData();
        if (requistionDetails != null) {

            summaryData.setClientName(String.valueOf(requistionDetails.getClientName()));
            summaryData.setRequisitionId(requistionDetails.getRequisitionId());
            summaryData.setProductCode(requistionDetails.getProduct().getCode());
            summaryData.setProductName(requistionDetails.getProduct().getName());
            summaryData.setDeliveryDate(String.valueOf(requistionDetails.getDeliveryDate()));
            summaryData.setDeliverySlot(String.valueOf(requistionDetails.getDeliverySlot()));
            summaryData.setRequestRaisedBy(String.valueOf(requistionDetails.getRequestRaisedBy().getName()));
        }
        return summaryData;

    }

    @Override
    public ErrorListWsDTO updateAllocationRequestCards(String requisitionId, String status, String rejectionReason) {
        final ErrorListWsDTO errorListWsDTO = new ErrorListWsDTO();
        final List<ErrorWsDTO> errorWsDTOList = new ArrayList<>();
        GiftSchemeModel giftShop = null;
        //influencer
        EyDmsCustomerModel currentUser=(EyDmsCustomerModel) userService.getCurrentUser();
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();

        if(requisitionId !=null) {
            PointRequisitionModel pointRequisitionModel = pointRequisitionDao.findByRequisitionId(requisitionId);

            if(pointRequisitionModel == null){
                ErrorWsDTO error = getError(requisitionId ,"given requisition id does not exist", IllegalArgumentException.class.getName());
                errorWsDTOList.add(error);
            }
            else
            {
                if(status.equalsIgnoreCase(String.valueOf(PointRequisitionStatus.APPROVED)))
                {
                    pointRequisitionModel.setReqApprovedDate(new Date());
                    pointRequisitionModel.setStatus(PointRequisitionStatus.APPROVED);
                    if(pointRequisitionModel.getPoints()!=null) {
                        CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
                        if(pointRequisitionModel.getRequestRaisedFor().getCounterType()!=null) {
                            giftShop = schemesAndDiscountService.getCurrentSchemesByGeography(pointRequisitionModel.getRequestRaisedFor().getState() != null ? pointRequisitionModel.getRequestRaisedFor().getState() : "",
                                    pointRequisitionModel.getRequestRaisedFor().getInfluencerType().getCode() != null ? pointRequisitionModel.getRequestRaisedFor().getInfluencerType().getCode() : "");
                        }
                        schemesAndDiscountService.updateInfluencerPoint(pointRequisitionModel.getRequestRaisedFor(), pointRequisitionModel.getPoints(), TransactionType.CREDIT, giftShop, null);
                    }
                    pointRequisitionModel.setApprovedBy(currentUser);
                    orderCountIncrementForCustomerRetailerMap(pointRequisitionModel.getDeliveryDate(),pointRequisitionModel.getRequestRaisedTo(), pointRequisitionModel.getRequestRaisedFor(),currentBaseSite,pointRequisitionModel.getQuantity());
                    //comment the code dealer allocation
                   LOG.info("1: PointRequisition:: Stock allocation to influencer:::" );
                    //To update the quantity as sales to influencer for allocation calculation
					if (null != pointRequisitionModel
							&& PointRequisitionStatus.APPROVED.equals(pointRequisitionModel.getStatus())
							&& null != pointRequisitionModel.getProduct()
							&& null != pointRequisitionModel.getRequestRaisedTo()) {
						LOG.info("2: PointRequisition:: Before getting ReceiptAllocation object::: Dealer Code:::" + pointRequisitionModel.getRequestRaisedTo() + ":::Product code:::" + pointRequisitionModel.getProduct());
            			ReceiptAllocaltionModel receiptAllocate = dealerDao.getDealerAllocation(pointRequisitionModel.getProduct(),
            					pointRequisitionModel.getRequestRaisedTo());
            			Double updatedQty = 0.0;
            			int stockAvailableForInfluencer = 0;
            			int stockAvailableForRetailer = 0;
            			if (null != receiptAllocate && null != receiptAllocate.getReceipt() && null != receiptAllocate.getSalesToRetailer()) {
            				LOG.info("3: PointRequisition:: Receipt allocation Record found to update:::" );
                            if(receiptAllocate.getSalesToInfluencer()!=null) {
                                updatedQty = receiptAllocate.getSalesToInfluencer() + pointRequisitionModel.getQuantity();
                                LOG.info("4: PointRequisition:: SalesToInfluencer allocation update:::" + updatedQty);
                                receiptAllocate.setSalesToInfluencer(updatedQty.intValue());
	            			    stockAvailableForInfluencer = getStockAvailForInfluencer(receiptAllocate.getReceipt(),
	            					receiptAllocate.getSalesToRetailer(), updatedQty.intValue());
                                stockAvailableForRetailer = getStockAvailForRetailer(receiptAllocate.getReceipt(),
                                        receiptAllocate.getSalesToRetailer(), receiptAllocate.getSalesToInfluencer());
                                LOG.info("5: PointRequisition:: stockAvailableForInfluencer:::"
                                        + stockAvailableForInfluencer + ":::stockAvailableForRetailer:::" + stockAvailableForRetailer);
                                receiptAllocate.setStockAvlForRetailer(stockAvailableForRetailer);
                                receiptAllocate.setStockAvlForInfluencer(stockAvailableForInfluencer);
                            }
	            			modelService.save(receiptAllocate);
            			}
            		}
					
					if (null != pointRequisitionModel
							&& PointRequisitionStatus.APPROVED.equals(pointRequisitionModel.getStatus()) && null != currentUser
							&& null != pointRequisitionModel.getProduct()
							&& null != pointRequisitionModel.getRequestRaisedFor() && null != pointRequisitionModel.getRequestRaisedTo()) {
						//LOG.info("6: PointRequisition:: stock allocation update receipts for retailer:::" );
						updateRetailerReceipts(pointRequisitionModel.getProduct(), pointRequisitionModel.getRequestRaisedTo(), pointRequisitionModel.getQuantity());
                        try {
                            StringBuilder builder = new StringBuilder();
                            builder.append("Accepted by : " + pointRequisitionModel.getRequestRaisedTo().getName()+ " and "+pointRequisitionModel.getRequestRaisedTo().getUid());
                            builder.append(" Raised by: "+pointRequisitionModel.getRequestRaisedFor().getName()+ " and " +pointRequisitionModel.getRequestRaisedFor().getName());
                            builder.append(" Product : " +pointRequisitionModel.getProduct().getName());
                            builder.append(" Quantity : " +pointRequisitionModel.getQuantity());
                            builder.append(" Points earned : "+pointRequisitionModel.getPoints());
                            builder.append(" Lifted on : "+pointRequisitionModel.getReqApprovedDate());
                            builder.append(" Allocation request date : "+pointRequisitionModel.getRequisitionCreationDate());

                            String body = builder.toString();
                            String sub ="Point Requisition has been accepted";
                            eydmsNotificationService.submitDealerNotification((B2BCustomerModel) pointRequisitionModel.getRequestRaisedTo(),body,sub, NotificationCategory.DELIVERY_WINDOW_NOT_MET,null);
                            eydmsNotificationService.submitDealerNotification((B2BCustomerModel) pointRequisitionModel.getRequestRaisedFor(),body,sub,NotificationCategory.DELIVERY_WINDOW_NOT_MET,null);
                        }
                        catch(Exception e) {
                            LOG.error("Error while sending PointRequisition approved notification");
                        }
					}
                }
                else if(status.equalsIgnoreCase(String.valueOf(PointRequisitionStatus.REJECTED)))
                {
                    pointRequisitionModel.setReqRejectedDate(new Date());
                    pointRequisitionModel.setRejectionReason(rejectionReason!=null ? rejectionReason : "");
                    pointRequisitionModel.setStatus(PointRequisitionStatus.REJECTED);
                    pointRequisitionModel.setRejectedBy(currentUser);
                }
                if(status.equalsIgnoreCase(String.valueOf(PointRequisitionStatus.CANCELLED)))
                {
                    pointRequisitionModel.setReqCancellationDate(new Date());
                    pointRequisitionModel.setCancelledBy(currentUser);
                    pointRequisitionModel.setCancelReason("");
                    pointRequisitionModel.setStatus(PointRequisitionStatus.CANCELLED);
                }
                if(status.equalsIgnoreCase(String.valueOf(PointRequisitionStatus.DELETE)))
                {
                    pointRequisitionModel.setReqDeletedDate(new Date());
                    pointRequisitionModel.setIsActive(false);
                    pointRequisitionModel.setStatus(PointRequisitionStatus.DELETE);
                    pointRequisitionModel.setDeletedBy(currentUser);
                }
                modelService.save(pointRequisitionModel);
            }
        }
        errorListWsDTO.setErrors(errorWsDTOList);
        return errorListWsDTO;
    }

    @Override
    public SearchPageData<EyDmsCustomerModel> getSavedDealerRetailer(SearchPageData searchPageData) {
        EyDmsCustomerModel eydmsCustomerModel=(EyDmsCustomerModel) userService.getCurrentUser();
        BaseSiteModel site = baseSiteService.getCurrentBaseSite();
        return pointRequisitionDao.getList(searchPageData,eydmsCustomerModel,site);

    }


    @Override
    public SearchPageData<EyDmsCustomerModel> getList(String filter, SearchPageData searchPageData) {
        EyDmsCustomerModel eydmsCustomerModel=(EyDmsCustomerModel) userService.getCurrentUser();
        BaseSiteModel site = baseSiteService.getCurrentBaseSite();
        List<SubAreaMasterModel> list = new ArrayList<SubAreaMasterModel>();
        UserModel user = userService.getCurrentUser();
        list = territoryManagementDao.getTerritoriesForCustomer(eydmsCustomerModel);
        SearchPageData<EyDmsCustomerModel> allCustomerForTerritories = pointRequisitionDao.getAllCustomerForTerritories(filter,searchPageData, list);
        List<EyDmsCustomerModel> collect = allCustomerForTerritories.getResults().stream().filter(d -> (d.getGroups()).contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)) || (d.getGroups()).contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))).collect(Collectors.toList());
        allCustomerForTerritories.setResults(collect);
        return allCustomerForTerritories;

    }


    private ErrorWsDTO getError(final String code, final String reason, final String type) {
        ErrorWsDTO errorWsDTO = new ErrorWsDTO();
        errorWsDTO.setReason(reason);
        errorWsDTO.setType(type);
        errorWsDTO.setErrorCode(code);
        return errorWsDTO;
    }
    
    //To update the quantity as receipts for allocation calculation
  	private void updateRetailerReceipts(ProductModel productCode, EyDmsCustomerModel dealerCode, Double receivedQuantity) {
  		LOG.info("8: PointRequisition:: Retailer updating stocks for Influencer:::" );
  		RetailerRecAllocateModel receiptRetailerAllocate = dealerDao.getRetailerAllocation(productCode, dealerCode);
  		LOG.info("9: PointRequisition:: receiptRetailerAllocate:::" + receiptRetailerAllocate);
        int stockRetailerToInfluencer=0;
  		if (null != receiptRetailerAllocate) {
  			LOG.info("10: PointRequisition:: receiptRetailerAllocate found to update:::" + receiptRetailerAllocate);
  			int updatedQty = (int)(receiptRetailerAllocate.getSalesToInfluencer() + receivedQuantity);
  			LOG.info("11: PointRequisition:: quantoty to update:::" + updatedQty);
  			receiptRetailerAllocate.setSalesToInfluencer(updatedQty);
              if(receiptRetailerAllocate.getReceipt()!=null && receiptRetailerAllocate.getSalesToInfluencer()!=null) {
                   stockRetailerToInfluencer = Math.abs((int) ((1.0 * (receiptRetailerAllocate.getReceipt() - receiptRetailerAllocate.getSalesToInfluencer()))));
                  receiptRetailerAllocate.setStockAvlForInfluencer(stockRetailerToInfluencer);
                  LOG.info("12: PointRequisition:: stockRetailerToInfluencer to update:::" + stockRetailerToInfluencer);
              }
  			modelService.save(receiptRetailerAllocate);
  		} else {
  			//If product and dealer is not found in the RetailerRecAllocate 
  			//then it means new entry has to be made as orderrequisition is placed with this combination
  			LOG.info("13: PointRequisition:: Record not FOUND -->> New Entry:::");
  			RetailerRecAllocateModel receiptRetailerAllocateNew = modelService.create(RetailerRecAllocateModel.class);
  			receiptRetailerAllocateNew.setProduct(productCode.getPk().toString());
  			receiptRetailerAllocateNew.setDealerCode(dealerCode.getPk().toString());
  			Double updatedQty = receivedQuantity;
  			receiptRetailerAllocateNew.setReceipt((null != updatedQty)?updatedQty.intValue():0);
  			receiptRetailerAllocateNew.setSalesToInfluencer(0);
  			int stockRetailerInfluencer = Math.abs((int) ((1.0 * (receiptRetailerAllocateNew.getReceipt() - receiptRetailerAllocateNew.getSalesToInfluencer()))));
  			receiptRetailerAllocateNew.setStockAvlForInfluencer(stockRetailerInfluencer);
  			LOG.info("14: PointRequisition:: Record not FOUND -->> New Entry:::" + stockRetailerInfluencer + ":::updatedQty:::" +updatedQty);
  			modelService.save(receiptRetailerAllocateNew);
  			modelService.refresh(receiptRetailerAllocateNew);
  		}
  	}
}
