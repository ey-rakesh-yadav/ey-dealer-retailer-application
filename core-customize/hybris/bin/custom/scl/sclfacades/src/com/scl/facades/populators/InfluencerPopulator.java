package com.scl.facades.populators;

import com.scl.core.model.SclCustomerModel;
import com.scl.facades.data.InfluencerData;
import com.scl.facades.data.SCLAddressData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.apache.commons.lang.BooleanUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

public class InfluencerPopulator implements Populator<SclCustomerModel, InfluencerData> {
    @Resource
    private Converter<AddressModel, SCLAddressData> sclAddressConverter;
    @Resource
    private EnumerationService enumerationService;
    @Override
    public void populate(SclCustomerModel source, InfluencerData target) throws ConversionException {
        target.setInfluencerType(enumerationService.getEnumerationName(source.getInfluencerType()));
        target.setName(source.getName());
        List<SCLAddressData> adrses=new ArrayList<>();
                source.getAddresses().forEach(addressModel -> {
                        if(BooleanUtils.isTrue(addressModel.getBillingAddress())){
                            var adrdata=sclAddressConverter.convert(addressModel);
                            adrdata.setEmail(source.getEmail());
                            adrses.add(adrdata);
                        }
                });
        target.setAddress(adrses.get(0));
    }
}
