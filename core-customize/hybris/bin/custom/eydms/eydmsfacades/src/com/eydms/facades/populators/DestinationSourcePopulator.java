package com.eydms.facades.populators;

import com.eydms.core.model.DestinationSourceMasterModel;
import com.eydms.facades.data.DestinationSourceMasterData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class DestinationSourcePopulator implements Populator<DestinationSourceMasterModel, DestinationSourceMasterData> {

    @Override
    public void populate(DestinationSourceMasterModel source, DestinationSourceMasterData target) throws ConversionException {
        if(null != source) {
            target.setCity(source.getDestinationCity());
            target.setSourcePriority(source.getSourcePriority());
            target.setSourceType(source.getType().getCode());
            target.setSourceName(source.getSource().getName());
            target.setSourceCode(source.getSource().getCode());
            target.setNcrCost(source.getNcrCost());
            target.setDeliveryMode(source.getDeliveryMode().getCode());
            target.setRouteId(source.getRoute());
            if(target.getSourcePriority()!=null && target.getSourcePriority().length()>1) {
            	target.setPriority(Integer.valueOf(target.getSourcePriority().substring(1)));
            }
        }
    }
}
