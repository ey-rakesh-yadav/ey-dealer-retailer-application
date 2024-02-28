package com.eydms.facades.populators;

import com.eydms.core.model.ComplaintDispatchDetailsModel;
import com.eydms.facades.data.ComplaintDispatchDetailsData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class EyDmsComplaintDispatchDetailsPopulator implements Populator<ComplaintDispatchDetailsModel, ComplaintDispatchDetailsData> {
    @Override
    public void populate(ComplaintDispatchDetailsModel source, ComplaintDispatchDetailsData target) throws ConversionException {
        target.setId(source.getId());
        if(source.getDateOfDispatch()!=null){
            target.setDateOfDispatch(String.valueOf(source.getDateOfDispatch()));
        }
        target.setDispatchFrom(source.getDispatchedFrom());
        target.setQuantity(source.getQuantity());
        target.setChallanNo(source.getCahllanNo());
        target.setTypeOfPacking(source.getTypeOfPacking());
        target.setTransporters(source.getTransporters());
        if(source.getInvoiceDate()!=null){
            target.setInvoiceDate(String.valueOf(source.getInvoiceDate()));
        }
        target.setInvoiceNo(source.getInvoiceNo());
        if(source.getTypeOfCement()!=null){
            target.setTypeOfCement(source.getTypeOfCement().getCode());
        }
        else if(source.getSiteCementType()!=null){
            target.setTypeOfCement(source.getSiteCementType().getName());
        }
        if(source.getManufacturingDate()!=null){
            target.setManufacturingDate(String.valueOf(source.getManufacturingDate()));
        }
        target.setWeekNumber(source.getWeekNumber());

    }
}
