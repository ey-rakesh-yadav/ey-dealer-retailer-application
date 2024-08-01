package com.scl.integration.cpi.hook;

import com.scl.core.enums.DeliveryItemStatus;
import com.scl.core.model.DeliveryItemModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.inboundservices.persistence.PersistenceContext;
import de.hybris.platform.inboundservices.persistence.hook.PostPersistHook;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class SclOrderStageGatePostPersistHook implements PostPersistHook {
    private static final Logger log = Logger.getLogger(SclOrderStageGatePostPersistHook.class);

    private ModelService modelService;

    /**
     * @param item    an item to execute this hook with.
     * @param context to provide information about the item to be persisted.
     */
    @Override
    public void execute(ItemModel item, PersistenceContext context) {
        if (item instanceof OrderEntryModel orderEntry) {
            log.info("SclOrderStageGatePostPersistHook called....");

            log.debug("SclOrderStageGatePostPersistHook executed for order Entry number:" + orderEntry.getEntryNumber() + " and order no: " + orderEntry.getOrder().getCode());


            try {
              if(CollectionUtils.isNotEmpty(orderEntry.getDeliveriesItem())) {
                  for (DeliveryItemModel deliveryItemModel : orderEntry.getDeliveriesItem()) {

                      if(deliveryItemModel.getStatus().equals(DeliveryItemStatus.TRUCK_ALLOCATED_CANCELLED)){
                          deliveryItemModel.setStatus(DeliveryItemStatus.DI_CREATED);
                          deliveryItemModel.setTransporterName(StringUtils.EMPTY);
                          deliveryItemModel.setTransporterPhoneNumber(StringUtils.EMPTY);
                          deliveryItemModel.setTruckAllocatedDate(null);
                          deliveryItemModel.setTruckAllocatedQty(null);
                          deliveryItemModel.setTruckNo(StringUtils.EMPTY);
                          deliveryItemModel.setTruckReachedDate(null);

                      }else if(deliveryItemModel.getStatus().equals(DeliveryItemStatus.INVOICED_CANCELLED)){

                          deliveryItemModel.setStatus(DeliveryItemStatus.TRUCK_ALLOCATED);
                          deliveryItemModel.setTruckDispatchedDateAndTime(null);
                          deliveryItemModel.setDeliveredDate(null);
                          deliveryItemModel.setInvoiceCreationDateAndTime(null);

                      }
                      modelService.save(deliveryItemModel);

                  }
              }

          }catch (Exception e){

          }
        }

    }

    public ModelService getModelService() {
        return modelService;
    }

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }
}
