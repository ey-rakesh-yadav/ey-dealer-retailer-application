package com.eydms.facades.populators;

import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.facades.data.DealerDetailsFormData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import javax.annotation.Resource;
import java.util.Objects;

public class DealerDetailsFormPopulator implements Populator<EyDmsCustomerModel, DealerDetailsFormData> {
    @Resource
    private EnumerationService enumerationService;

    @Override
    public void populate(EyDmsCustomerModel source, DealerDetailsFormData target) throws ConversionException {
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
