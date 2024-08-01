/*
 *  * Copyright (c) SCL. All rights reserved.
 */

package com.scl.integration.cpi.hook;

import com.scl.core.enums.DeliveryItemStatus;
import com.scl.core.enums.StageGateType;
import com.scl.core.enums.WarehouseType;
import com.scl.core.jalo.SclCustomer;
import com.scl.core.model.*;
import com.scl.core.services.SalesPerformanceService;
import com.scl.integration.service.SclintegrationService;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.inboundservices.persistence.PersistenceContext;
import de.hybris.platform.inboundservices.persistence.hook.PrePersistHook;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

public class SclOrderStageGatePrePersistHook implements PrePersistHook {
    Logger log = LoggerFactory.getLogger(SclOrderStageGatePrePersistHook.class);


    private ModelService modelService;
    private ConfigurationService configurationService;

    @Resource
    SalesPerformanceService salesPerformanceService;

    @Resource(name = "sclintegrationService")
    private SclintegrationService sclintegrationService;

    @Resource
    BusinessProcessService businessProcessService;
    Boolean isOutboundStageGateRequired=false;
    Set<String> stringSet;

    @Override
    public Optional<ItemModel> execute(ItemModel item, PersistenceContext context) {
        if (item != null && item instanceof OrderEntryModel) {
            log.info("SclOrderStageGatePrePersistHook called....");

            OrderEntryModel orderEntry = (OrderEntryModel) item;
            Double remQty=orderEntry.getQuantityInMT();
            log.debug("SclOrderStageGatePrePersistHook executed for order Entry number:" + orderEntry.getEntryNumber() + " and order no: " + orderEntry.getOrder().getCode());

            try {
                if(CollectionUtils.isNotEmpty(orderEntry.getDeliveriesItem())) {
                    for (DeliveryItemModel deliveryItemModel : orderEntry.getDeliveriesItem()) {


                        if (deliveryItemModel.getStatus().equals(DeliveryItemStatus.DI_CHANGED) || deliveryItemModel.getStatus().equals(DeliveryItemStatus.PGI_CREATED)) {
                            deliveryItemModel.setStatus(DeliveryItemStatus.DI_CREATED);
                        }

                        if(deliveryItemModel.getStatus().equals(DeliveryItemStatus.DI_CREATED)){
                            deliveryItemModel.setLatestStatusUpdate(deliveryItemModel.getDiCreationDateAndTime());
                        }
                        else if(deliveryItemModel.getStatus().equals(DeliveryItemStatus.TRUCK_ALLOCATED)){
                            deliveryItemModel.setLatestStatusUpdate(deliveryItemModel.getTruckAllocatedDate());
                        }
                        else if (deliveryItemModel.getStatus().equals(DeliveryItemStatus.DI_CANCELLED)){
                            deliveryItemModel.setLatestStatusUpdate(deliveryItemModel.getCancelledDate());
                        }



                        //remaining quantity
                        if (!deliveryItemModel.getStatus().equals(DeliveryItemStatus.DI_CANCELLED) && null!= deliveryItemModel.getDiQuantity()) {
                            remQty -= deliveryItemModel.getDiQuantity();
                        }


                        if (null != deliveryItemModel.getInvoiceCreationDateAndTime() && deliveryItemModel.getStatus().equals(DeliveryItemStatus.INVOICED)) {

                            deliveryItemModel.setLatestStatusUpdate(deliveryItemModel.getInvoiceCreationDateAndTime());

                            if (orderEntry.getSourceType().equals(WarehouseType.DEPOT)) {
                                if (Objects.nonNull(orderEntry.getIncoTerm()) && BooleanUtils.isTrue(orderEntry.getIncoTerm().getMarkAsDelivered())) {
                                    deliveryItemModel.setTruckDispatchedDateAndTime(deliveryItemModel.getInvoiceCreationDateAndTime());
                                    deliveryItemModel.setDeliveredDate(deliveryItemModel.getInvoiceCreationDateAndTime());
                                    deliveryItemModel.setStatus(DeliveryItemStatus.DELIVERED);
                                } else {
                                    deliveryItemModel.setTruckDispatchedDateAndTime(deliveryItemModel.getInvoiceCreationDateAndTime());
                                    deliveryItemModel.setStatus(DeliveryItemStatus.TRUCK_DISPATCHED);
                                }
                            } else {
                                deliveryItemModel.setStatus(DeliveryItemStatus.INVOICED);
                            }


                            // dealer & retailer lifting qty & date
                            SclCustomerModel dealer = (SclCustomerModel) orderEntry.getOrder().getUser();
                            SclCustomerModel retailer=null;
                            if(Objects.nonNull(orderEntry.getRetailer())){
                             retailer = orderEntry.getRetailer();
                            }
                             if( Objects.isNull(dealer.getLastLiftingDate()) || dealer.getLastLiftingDate().before(deliveryItemModel.getInvoiceCreationDateAndTime())){
                                dealer.setLastLiftingDate(deliveryItemModel.getInvoiceCreationDateAndTime());
                                dealer.setLastLiftingQuantity(deliveryItemModel.getInvoiceQuantity());

                            }

                            if( Objects.nonNull(retailer) && (Objects.isNull(retailer.getLastLiftingDate()) || retailer.getLastLiftingDate().before(deliveryItemModel.getInvoiceCreationDateAndTime()))){
                                retailer.setLastLiftingDate(deliveryItemModel.getInvoiceCreationDateAndTime());
                                retailer.setLastLiftingQuantity(deliveryItemModel.getInvoiceQuantity());
                                modelService.save(retailer);
                                modelService.refresh(orderEntry.getRetailer());
                            }

                             modelService.save(dealer);
                            modelService.refresh(orderEntry.getOrder().getUser());
                            modelService.refresh(dealer);
                            salesPerformanceService.updateReceipts(orderEntry.getProduct(), dealer, deliveryItemModel.getInvoiceQuantity());

                        }


                        if (null != deliveryItemModel.getTruckDispatchedDateAndTime() && deliveryItemModel.getStatus().equals(DeliveryItemStatus.TRUCK_DISPATCHED)) {

                            deliveryItemModel.setLatestStatusUpdate(deliveryItemModel.getTruckDispatchedDateAndTime());

                            if (orderEntry.getSourceType().equals(WarehouseType.PLANT)) {
                                if ((Objects.nonNull(orderEntry.getIncoTerm()) && BooleanUtils.isTrue(orderEntry.getIncoTerm().getMarkAsDelivered()))) {
                                    deliveryItemModel.setDeliveredDate(deliveryItemModel.getTruckDispatchedDateAndTime());
                                    deliveryItemModel.setStatus(DeliveryItemStatus.DELIVERED);
                                } else {
                                    if (null != deliveryItemModel.getTruckReachedDate()) {
                                        deliveryItemModel.setStatus(DeliveryItemStatus.TRUCK_REACHED_DESTINATION);
                                        deliveryItemModel.setLatestStatusUpdate(deliveryItemModel.getTruckReachedDate());
                                    } else {
                                        deliveryItemModel.setStatus(DeliveryItemStatus.TRUCK_DISPATCHED);
                                    }
                                }
                            } else {
                                if (null != deliveryItemModel.getTruckReachedDate()) {
                                    deliveryItemModel.setStatus(DeliveryItemStatus.TRUCK_REACHED_DESTINATION);
                                    deliveryItemModel.setLatestStatusUpdate(deliveryItemModel.getTruckReachedDate());
                                } else {
                                    deliveryItemModel.setStatus(DeliveryItemStatus.TRUCK_DISPATCHED);
                                }
                            }



                        }

                        if (CollectionUtils.isNotEmpty(deliveryItemModel.getDiStatusHistory())) {
                            stringSet = new HashSet<>(deliveryItemModel.getDiStatusHistory());
                        } else {
                            stringSet = new HashSet<>();
                        }

                        List<StageGateSequenceMapperModel> sequenceMapperModelList=sclintegrationService.getStageGateSequenceMapperListForSource(orderEntry.getSourceType());
                        Map<String, Integer> sequenceMap=new HashMap<String, Integer>();
                        sequenceMapperModelList.forEach(sm->{
                            sequenceMap.put(sm.getStageGateType().getCode(),sm.getSequenceNumber());
                        });
                        int newDiSequenceValue= sequenceMap.get(deliveryItemModel.getStageGateType().getCode());
                        isOutboundStageGateRequired=false;
                        for(StageGateSequenceMapperModel mapperModel: sequenceMapperModelList){
                            if(mapperModel.getStageGateType().equals(deliveryItemModel.getStageGateType()) && mapperModel.getSequenceNumber()>1) {
                                if (CollectionUtils.isNotEmpty(stringSet)) {
                                    ArrayList<Integer> diHistorySequence=new ArrayList<>();
                                    for(String str:stringSet){
                                        diHistorySequence.add(sequenceMap.get(str));
                                    }
                                    if((newDiSequenceValue-Collections.max(diHistorySequence))>1) {
                                        isOutboundStageGateRequired = true;
                                    }
                                    else if((newDiSequenceValue-Collections.max(diHistorySequence))==0) {
                                    	 isOutboundStageGateRequired = true;
                                    }
                                    else{
                                        stringSet.add(deliveryItemModel.getStageGateType().getCode());
                                    }

                                }else {
                                    isOutboundStageGateRequired = true;
                                }
                            }else if(mapperModel.getStageGateType().equals(deliveryItemModel.getStageGateType()) && mapperModel.getSequenceNumber()==1){
                                stringSet.add(deliveryItemModel.getStageGateType().getCode());
                            }
                        }

                        deliveryItemModel.setDiStatusHistory(stringSet);
                        if(isOutboundStageGateRequired){
                            final SclOutboundStageGateProcessModel stageGateProcessModel = (SclOutboundStageGateProcessModel) businessProcessService.createProcess(
                                    "sclOutboundStageGate-process-" + deliveryItemModel.getDiNumber() + "-" + System.currentTimeMillis(),
                                    "sclOutboundStageGate-process");

                            log.info("sclOutboundStageGate-proces is created for dinumber: "+ deliveryItemModel.getDiNumber() + " and order: "+ orderEntry.getOrder().getCode());
                            stageGateProcessModel.setDeliveryItem(deliveryItemModel);
                            stageGateProcessModel.setOrderEntry(orderEntry);
                            modelService.save(stageGateProcessModel);
                            businessProcessService.startProcess(stageGateProcessModel);
                        }

                    }
                }
                orderEntry.setRemainingQuantity(remQty);
            }catch (RuntimeException e){
                log.info("SclOrderStageGatePrePersistHook for ordercode: "+ orderEntry.getOrder().getCode()+ " exception: "+ e.getMessage());
                e.printStackTrace();
            }
            return Optional.of(item);

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
