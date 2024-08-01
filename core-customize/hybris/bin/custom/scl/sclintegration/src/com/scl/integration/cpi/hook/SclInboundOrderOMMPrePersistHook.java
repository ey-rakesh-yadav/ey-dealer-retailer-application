/*
 *  * Copyright (c) SCL. All rights reserved.
 */

package com.scl.integration.cpi.hook;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.SclDealerRetailerDao;
import com.scl.core.enums.CreatedFromCRMorERP;
import com.scl.core.enums.CustomerCategory;
import com.scl.core.enums.DeliveryItemStatus;
import com.scl.core.enums.OrderFor;
import com.scl.core.enums.OrderType;
import com.scl.core.model.DealerRetailerMappingModel;
import com.scl.core.model.DeliveryItemModel;

import com.scl.core.model.DestinationSourceMasterModel;
import com.scl.core.model.FreightSPIMappingModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.services.SlctCrmIntegrationService;
import com.scl.core.source.dao.DestinationSourceMasterDao;
import com.scl.integration.cpi.hook.exception.DuplicateOrderRuntimeException;
import com.scl.integration.service.SclintegrationService;

import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.inboundservices.persistence.PersistenceContext;
import de.hybris.platform.inboundservices.persistence.hook.PrePersistHook;
import de.hybris.platform.order.daos.DeliveryModeDao;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.AddressService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.store.services.BaseStoreService;

public class SclInboundOrderOMMPrePersistHook implements PrePersistHook {
    Logger log = LoggerFactory.getLogger(SclInboundOrderOMMPrePersistHook.class);


    private ModelService modelService;
    private ConfigurationService configurationService;

    @Resource
    private KeyGenerator orderCodeGenerator;

    @Resource
    private AddressService addressService;

    @Autowired
    DeliveryModeDao deliveryModeDao;

    @Resource(name = "sclintegrationService")
    private SclintegrationService sclintegrationService;

    @Resource(name = "baseStoreService")
    private BaseStoreService baseStoreService;

    @Resource
    private BaseSiteService baseSiteService;

    @Autowired
    SlctCrmIntegrationService slctCrmIntegrationService;

    @Resource
    SclDealerRetailerDao sclDealerRetailerDao;
    
    @Autowired
    DestinationSourceMasterDao destinationSourceMasterDao;



    @Override
    public Optional<ItemModel> execute(ItemModel item, PersistenceContext context) {
    if (item != null  && item instanceof OrderModel) {
        log.info("SclIbnboundOrderOMMPrePersistHook called....");
        OrderModel order= (OrderModel) item;

        Boolean OrderAlreadyExist=false;

        try {
        	
            if (StringUtils.isNotEmpty(order.getErpOrderNumber()) && order.getCode().equalsIgnoreCase(order.getErpOrderNumber())) {
                OrderModel existingOrder = sclintegrationService.getOrderFromERPOrderNumber(order.getErpOrderNumber());
                if (Objects.nonNull(existingOrder)) {
                    order.setCode(existingOrder.getCode());
                    OrderAlreadyExist=true;
                    throw new RuntimeException("DuplicateCRMOrderError:"+existingOrder.getCode());
                } else {
                    order.setCode(orderCodeGenerator.generate().toString());
                    order.setCreatedFromCRMorERP(CreatedFromCRMorERP.S4HANA);
                    
                    order.setStore(baseStoreService.getBaseStoreForUid(SclCoreConstants.SCL_SITE));
                    order.setSite(baseSiteService.getBaseSiteForUID(SclCoreConstants.SCL_SITE));


                    for(AbstractOrderEntryModel entry:order.getEntries()){
                        //ProductFromEquiCode only for order created from s4Hana
                        entry.setEquivalenceProductCode(entry.getProduct().getCode());
                        entry.setProduct(sclintegrationService.getProductFromEquiCode(entry.getProduct().getEquivalenceProductCode(), entry.getProduct().getCatalogVersion()));
                  
                        if(Objects.nonNull(entry.getSource()) && StringUtils.isNotEmpty(entry.getSource().getCode())) {
                        	entry.setSourceAtOrderBooking(entry.getSource().getCode());
                        }
                        
                        if(Objects.nonNull(entry.getSpecialProcessIndicator())){
                        	FreightSPIMappingModel spi = destinationSourceMasterDao.getFreightTypeFromSPI(entry.getSpecialProcessIndicator());
            				if (Objects.nonNull(spi) && Objects.nonNull(spi.getSpi()) && Objects.nonNull(spi.getDeliveryMode())){
            					entry.setFreightType(spi.getFreightType());
            					entry.setDeliveryMode(spi.getDeliveryMode());
            				}
                        }
                        
                        if(StringUtils.isNotEmpty(entry.getShipToParty()) && Objects.isNull(entry.getDeliveryAddress())){
                            DealerRetailerMappingModel drm= sclDealerRetailerDao.getDealerRetailerMappingForDealerAndShipto((SclCustomerModel) order.getUser(),entry.getShipToParty());
                            if(Objects.nonNull(drm) && Objects.nonNull(drm.getShipTo())) {
                                AddressModel addNew=addressService.cloneAddress(drm.getShipTo());
                                addNew.setOwner(entry);
                                entry.setDeliveryAddress(addNew);
                                drm.setLastUsed(new Date());
                                modelService.save(drm);
                                modelService.refresh(drm);
                            }
                            if(Objects.nonNull(drm) && Objects.nonNull(drm.getRetailer()) && Objects.isNull(entry.getRetailer())){
                            	entry.setRetailer(drm.getRetailer());
                            	entry.setOrderFor(OrderFor.RETAILER);
                            }

                        }
                        
                        if(entry.getProduct()!=null && entry.getDeliveryMode()!=null && entry.getDeliveryAddress()!=null 
                				&& entry.getOrder().getSite()!=null && entry.getSource()!=null
                				&& entry.getDeliveryAddress().getTransportationZone()!=null && entry.getIncoTerm()!=null) {
                			DestinationSourceMasterModel destinationSource =  
                					destinationSourceMasterDao.getDestinationSourceBySourceAndSapProductCode(OrderType.SO, CustomerCategory.TR, 
                							entry.getSource(), entry.getDeliveryMode(),
                							entry.getDeliveryAddress().getTransportationZone(),
                							entry.getProduct().getCode(), entry.getOrder().getSite(), entry.getIncoTerm());
                			if(destinationSource!=null) {
                				entry.setSourceRank(StringUtils.isNotBlank(destinationSource.getSourcePriority())?destinationSource.getSourcePriority(): Strings.EMPTY);
                			}
                        }
                    }
                }
            }


            if (Objects.nonNull(((SclCustomerModel) order.getUser()).getTerritoryCode())) {
                order.setTerritoryMaster(((SclCustomerModel) order.getUser()).getTerritoryCode());
            }
            if (order.getStatus().equals(OrderStatus.CREDIT_BLOCK)) {
                order.setBlockedDate((null!=order.getLatestStatusUpdate())? order.getLatestStatusUpdate(): order.getDate());
                order.setCreditLimitBreached(Boolean.TRUE);
            } else if (order.getStatus().equals(OrderStatus.TOTAL_BLOCK)) {
                order.setBlockedDate((null!=order.getLatestStatusUpdate())? order.getLatestStatusUpdate(): order.getDate());
            }else if(order.getStatus().equals(OrderStatus.ERROR_IN_ERP)) {
                order.setLatestStatusUpdate(new Date());
                for(AbstractOrderEntryModel entryModel:order.getEntries()){
                    entryModel.setLatestStatusUpdate(order.getLatestStatusUpdate());
                }
            }
           // List<DeliveryModeModel> deliveryModeList = deliveryModeDao.findDeliveryModesByCode("ROAD");

            for(AbstractOrderEntryModel entryModel:order.getEntries()){

                if(Objects.nonNull(entryModel.getSource())) {
                    entryModel.setSourceType(entryModel.getSource().getType());
                }

                entryModel.setSequence(entryModel.getEntryNumber()+1);
                Double remQty= entryModel.getQuantityInMT();
                //entryModel.setDeliveryMode(deliveryModeList.get(0));

                

                if(Objects.nonNull(entryModel.getSpecialProcessIndicator())){
                	FreightSPIMappingModel spi = destinationSourceMasterDao.getFreightTypeFromSPI(entryModel.getSpecialProcessIndicator());
    				if (Objects.nonNull(spi) && Objects.nonNull(spi.getSpi()) && Objects.nonNull(spi.getDeliveryMode())){
    					entryModel.setFreightType(spi.getFreightType());
    					entryModel.setDeliveryMode(spi.getDeliveryMode());
    				}
                }


                //remaining quantity logic
                if(CollectionUtils.isNotEmpty(entryModel.getDeliveriesItem())) {
                    for (DeliveryItemModel deliveryItemModel : entryModel.getDeliveriesItem()) {


                        if(Objects.nonNull(deliveryItemModel.getStatus()) && !deliveryItemModel.getStatus().equals(DeliveryItemStatus.DI_CANCELLED)){
                            remQty-=deliveryItemModel.getDiQuantity();
                        }
                    }
                }

                entryModel.setRemainingQuantity(remQty);


                if(((OrderEntryModel) entryModel).getStatus().equals(OrderStatus.ORDER_ACCEPTED)){
                    entryModel.setLatestStatusUpdate(order.getOrderAcceptedDate());
                }
                if (((OrderEntryModel) entryModel).getStatus().equals(OrderStatus.CREDIT_BLOCK)){
                	entryModel.setLatestStatusUpdate((null!=order.getLatestStatusUpdate())? order.getLatestStatusUpdate(): order.getDate());
                }
                
                if(null != entryModel.getRoute()){
                    entryModel.setRouteId(entryModel.getRoute());
                }
                if(order.getCreatedFromCRMorERP().equals(CreatedFromCRMorERP.S4HANA)) {
                    slctCrmIntegrationService.populateDeliverySlotAndDate(entryModel.getRouteId(), order, (OrderEntryModel) entryModel);
                }

                if(StringUtils.isNotEmpty(entryModel.getShipToParty()) && Objects.isNull(entryModel.getDeliveryAddress())){
                    DealerRetailerMappingModel drm= sclDealerRetailerDao.getDealerRetailerMappingForDealerAndShipto((SclCustomerModel) order.getUser(),entryModel.getShipToParty());
                    if(Objects.nonNull(drm) && Objects.nonNull(drm.getShipTo())) {
                        AddressModel addNew=addressService.cloneAddress(drm.getShipTo());
                        addNew.setOwner(entryModel);
                        entryModel.setDeliveryAddress(addNew);
                        drm.setLastUsed(new Date());
                        modelService.save(drm);
                        modelService.refresh(drm);
                    }
                    if(Objects.nonNull(drm) && Objects.nonNull(drm.getRetailer()) && Objects.isNull(entryModel.getRetailer())){
                        entryModel.setRetailer(drm.getRetailer());
                        entryModel.setOrderFor(OrderFor.RETAILER);
                    }

                }
            }

            if(StringUtils.isNotEmpty(order.getErpOrderType()) && order.getErpOrderType().equalsIgnoreCase("ZTRD")){
                order.setOrderType(OrderType.SO);
            }


        }catch (RuntimeException e){

            log.info("SclIbnboundOrderOMMPrePersistHook exception for Order code:" + order.getCode() + " exception msg:"+ e.getMessage());
            e.printStackTrace();
            if(OrderAlreadyExist)  {
                throw new DuplicateOrderRuntimeException("DuplicateCRMOrderError:"+order.getCode());
            }



        }

        log.debug("SclIbnboundOrderOMMPrePersistHook executed for Order code:" + order.getCode());
        }
        return Optional.of(item);
    }

    public ModelService getModelService() {
        return modelService;
    }

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

   }
