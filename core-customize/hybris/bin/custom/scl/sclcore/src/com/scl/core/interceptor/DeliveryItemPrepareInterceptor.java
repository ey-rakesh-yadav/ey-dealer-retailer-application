package com.scl.core.interceptor;

import com.scl.core.enums.DeliveryItemStatus;
import com.scl.core.enums.EpodStatus;
import com.scl.core.enums.StageGateType;
import com.scl.core.enums.WarehouseType;
import com.scl.core.model.DeliveryItemModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashSet;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

public class DeliveryItemPrepareInterceptor implements PrepareInterceptor<DeliveryItemModel> {
    Logger log = LoggerFactory.getLogger(DeliveryItemPrepareInterceptor.class);

    Set<String> stringSet;


    /**
     * @param deliveryItemModel
     * @param interceptorContext
     * @throws InterceptorException
     */
    @Override
    public void onPrepare(DeliveryItemModel deliveryItemModel, InterceptorContext interceptorContext) throws InterceptorException {

        log.info("deliveryItemPrepareInterceptor called....");

        OrderEntryModel orderEntry = (OrderEntryModel) deliveryItemModel.getEntry();
        if (Objects.nonNull(orderEntry)) {

            if (CollectionUtils.isNotEmpty(deliveryItemModel.getDiStatusHistory())) {
                stringSet = new HashSet<>(deliveryItemModel.getDiStatusHistory());
            } else {
                stringSet = new HashSet<>();
            }



            if (null != deliveryItemModel.getDiCreationDateAndTime()) {
                deliveryItemModel.setStatus(DeliveryItemStatus.DI_CREATED);
                deliveryItemModel.setStageGateType(StageGateType.DELIVERY_DOCUMENT);
                stringSet.add(StageGateType.DELIVERY_DOCUMENT.getCode());

            }
            if (null != deliveryItemModel.getTruckAllocatedDate()) {
                deliveryItemModel.setStatus(DeliveryItemStatus.TRUCK_ALLOCATED);
                deliveryItemModel.setStageGateType(StageGateType.SHIPMENT_DOCUMENT);
                stringSet.add(StageGateType.SHIPMENT_DOCUMENT.getCode());
            }


            if (null != deliveryItemModel.getInvoiceCreationDateAndTime()) {

                if (orderEntry.getSourceType().equals(WarehouseType.DEPOT)) {
                    if (Objects.nonNull(orderEntry.getIncoTerm()) && BooleanUtils.isTrue(orderEntry.getIncoTerm().getMarkAsDelivered())) {
                        deliveryItemModel.setStatus(DeliveryItemStatus.DELIVERED);
                        deliveryItemModel.setDeliveredDate(deliveryItemModel.getInvoiceCreationDateAndTime());
                        deliveryItemModel.setTruckDispatchedDateAndTime(deliveryItemModel.getInvoiceCreationDateAndTime());
                    } else {
                        deliveryItemModel.setStatus(DeliveryItemStatus.TRUCK_DISPATCHED);
                        deliveryItemModel.setTruckDispatchedDateAndTime(deliveryItemModel.getInvoiceCreationDateAndTime());
                    }
                } else {
                    deliveryItemModel.setStatus(DeliveryItemStatus.INVOICED);
                }
                deliveryItemModel.setStageGateType(StageGateType.INVOICE_DOCUMENT);
                stringSet.add(StageGateType.INVOICE_DOCUMENT.getCode());
            }


            if (null != deliveryItemModel.getTruckDispatchedDateAndTime()) {

                if (orderEntry.getSourceType().equals(WarehouseType.PLANT)) {
                    if ((Objects.nonNull(orderEntry.getIncoTerm()) && BooleanUtils.isTrue(orderEntry.getIncoTerm().getMarkAsDelivered()))) {
                        deliveryItemModel.setStatus(DeliveryItemStatus.DELIVERED);
                        deliveryItemModel.setDeliveredDate(deliveryItemModel.getTruckDispatchedDateAndTime());
                    } else {
                        if (null != deliveryItemModel.getTruckReachedDate()) {
                            deliveryItemModel.setStatus(DeliveryItemStatus.TRUCK_REACHED_DESTINATION);
                        } else {
                            deliveryItemModel.setStatus(DeliveryItemStatus.TRUCK_DISPATCHED);
                        }
                    }
                    deliveryItemModel.setStageGateType(StageGateType.TRUCKDETAILS_DOCUMENT);
                    stringSet.add(StageGateType.TRUCKDETAILS_DOCUMENT.getCode());
                }
            }

            deliveryItemModel.setDiStatusHistory(stringSet);
        }


        if(Objects.nonNull(deliveryItemModel.getEpodStatus()) && (deliveryItemModel.getEpodStatus().equals(EpodStatus.DISPUTED) || deliveryItemModel.getEpodStatus().equals(EpodStatus.APPROVED))){
            deliveryItemModel.setStatus(DeliveryItemStatus.DELIVERED);
            deliveryItemModel.setDeliveredDate(new Date());
        }

    }
}
