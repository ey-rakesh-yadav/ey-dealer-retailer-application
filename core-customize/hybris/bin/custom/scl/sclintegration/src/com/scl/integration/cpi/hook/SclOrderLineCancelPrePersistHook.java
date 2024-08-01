package com.scl.integration.cpi.hook;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.inboundservices.persistence.PersistenceContext;
import de.hybris.platform.inboundservices.persistence.hook.PrePersistHook;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Optional;

public class SclOrderLineCancelPrePersistHook implements PrePersistHook {


    private static final Logger LOG = Logger.getLogger(SclOrderPostPersistHook.class);

    /**
     * @param item    an item to execute this hook with.
     * @param context to provide information about the item to be persisted
     * @return
     */
    @Override
    public Optional<ItemModel> execute(ItemModel item, PersistenceContext context) {
        if (item instanceof OrderModel) {
            LOG.info("SclOrderLineCancelPrePersistHook called....");

            OrderModel order = (OrderModel) item;
            try {
                for (AbstractOrderEntryModel entryModel : order.getEntries()) {
                    entryModel.setLatestStatusUpdate((null!=entryModel.getCancelledDate()) ?  entryModel.getCancelledDate():new Date());
                }
            }catch (RuntimeException e){
                LOG.info("SclOrderLineCancelPrePersistHook for order:"+  order.getCode()   + " exception : " + e.getMessage());
                e.printStackTrace();
            }
        }

        return Optional.empty();
    }
}
