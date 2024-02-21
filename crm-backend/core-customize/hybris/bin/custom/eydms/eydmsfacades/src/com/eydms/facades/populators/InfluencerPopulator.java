package com.eydms.facades.populators;

import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.facades.data.InfluencerData;
import com.eydms.facades.data.EYDMSAddressData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.apache.commons.lang.BooleanUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

public class InfluencerPopulator implements Populator<EyDmsCustomerModel, InfluencerData> {
    @Resource
    private Converter<AddressModel, EYDMSAddressData> eydmsAddressConverter;
    @Resource
    private EnumerationService enumerationService;
    @Override
    public void populate(EyDmsCustomerModel source, InfluencerData target) throws ConversionException {
        target.setInfluencerType(enumerationService.getEnumerationName(source.getInfluencerType()));
        target.setName(source.getName());
        List<EYDMSAddressData> adrses=new ArrayList<>();
                source.getAddresses().forEach(addressModel -> {
                        if(BooleanUtils.isTrue(addressModel.getBillingAddress())){
                            var adrdata=eydmsAddressConverter.convert(addressModel);
                            adrdata.setEmail(source.getEmail());
                            adrses.add(adrdata);
                        }
                });
        target.setAddress(adrses.get(0));
    }
}
