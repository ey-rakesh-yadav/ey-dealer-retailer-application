package com.scl.facades.populators;

import com.scl.core.model.SclCustomerModel;
import com.scl.facades.data.DealerDetailsFormData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import javax.annotation.Resource;
import java.util.Objects;

public class DealerDetailsFormPopulator implements Populator<SclCustomerModel, DealerDetailsFormData> {
    @Resource
    private EnumerationService enumerationService;

    @Override
    public void populate(SclCustomerModel source, DealerDetailsFormData target) throws ConversionException {
        target.setPotential(source.getCounterPotential()+" MT");
        target.setContactNo(source.getContactNumber());
        target.setEmail(source.getEmail());
        if(Objects.nonNull(source.getDealerStageStatus())) {
            target.setStage(enumerationService.getEnumerationName(source.getDealerStageStatus()));
        }
       // target.setCity(source.get);
        target.setRejectionReason(source.getRejectionReason());
    }
}
