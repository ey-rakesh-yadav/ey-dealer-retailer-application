package com.scl.facades.populators;

import com.scl.core.enums.IsDealerProvidingTransport;
import com.scl.core.model.DestinationSourceMasterModel;
import com.scl.core.model.SclIncoTermMasterModel;
import com.scl.facades.data.DestinationSourceMasterData;
import com.scl.facades.data.IsDealerProvidingTransportData;
import com.scl.facades.data.SclIncoTermMasterData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class SCLIncoTermPopulator implements Populator<SclIncoTermMasterModel, SclIncoTermMasterData> {


    Converter<IsDealerProvidingTransport, IsDealerProvidingTransportData> dealerProvidingTransportConverter;
    /**
     * Populate the target instance with values from the source instance.
     *
     * @param source object
     * @param target to fill
     * @throws ConversionException if an error occurs
     */
    @Override
    public void populate(SclIncoTermMasterModel source, SclIncoTermMasterData target) throws ConversionException {
          if(StringUtils.isNotBlank(source.getIncoTerm())){
              target.setIncoTerm(source.getIncoTerm());
          }
        if(StringUtils.isNotBlank(source.getName())){
            target.setName(source.getName());
        }
        if(StringUtils.isNotBlank(source.getFreight())){
            target.setFreight(source.getFreight());
        }
         if(source.getIncoTermSequence()!=null){
             target.setSequence(source.getIncoTermSequence());
         }

        if(CollectionUtils.isNotEmpty(source.getIsDealerProvidingTransport())){
            target.setIsDealerProvidingTransport(getDealerProvidingTransportConverter().convertAll(source.getIsDealerProvidingTransport()));
        }

    }

    public Converter<IsDealerProvidingTransport, IsDealerProvidingTransportData> getDealerProvidingTransportConverter() {
        return dealerProvidingTransportConverter;
    }

    public void setDealerProvidingTransportConverter(Converter<IsDealerProvidingTransport, IsDealerProvidingTransportData> dealerProvidingTransportConverter) {
        this.dealerProvidingTransportConverter = dealerProvidingTransportConverter;
    }
}
