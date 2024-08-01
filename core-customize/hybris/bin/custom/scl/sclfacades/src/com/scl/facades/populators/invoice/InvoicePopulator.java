package com.scl.facades.populators.invoice;

import com.scl.core.model.InvoiceMasterModel;
import com.scl.facades.data.InvoiceMasterData;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.converters.Populator;

import java.util.Objects;

public class InvoicePopulator implements Populator<InvoiceMasterModel,InvoiceMasterData> {

    @Override
    public void populate(InvoiceMasterModel source,  InvoiceMasterData target) throws ConversionException {
        target.setCustomerName(source.getCustomerName());
        target.setCustomerNo(source.getCustomerNo());
        target.setInvoiceAmount(source.getInvoiceAmount());
        target.setInvoiceNo(source.getInvoiceNo());
        target.setInvoiceDate(source.getInvoiceDate());
        target.setFinalReconciledInvoiceAmount(source.getFinalReconciledInvoiceAmount());
        target.setInvoiceAmountWithDiscount(source.getInvoiceAmountWithDiscount());
        target.setReconcilationDate(source.getReconcilationDate());

        AbstractOrderEntryModel entry = source.getOrderEntry();
        if(Objects.nonNull(entry)){
            target.setTransporter(entry.getTransporterName());
            target.setQuantity(Double.valueOf(entry.getQuantity()));
            target.setProduct(entry.getProduct() != null? entry.getProduct().getName() :null);
        }

    }

}
