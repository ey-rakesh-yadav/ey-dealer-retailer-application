package com.eydms.facades.populators;

import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.facades.data.SiteDetailData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.lang.text.StrBuilder;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.Optional;

public class SiteDataPopulator implements Populator<EyDmsCustomerModel, SiteDetailData> {
    public static final String COMMA = ",";
    @Resource
    private EnumerationService enumerationService;
    @Override
    public void populate(EyDmsCustomerModel source, SiteDetailData target) throws ConversionException {
        target.setName(source.getName());
        target.setCode(source.getUid());
        target.setBalance(String.format("%s",source.getBalancePotential()));
        target.setAddress(getAddressAsString(source.getAddresses().stream().filter(AddressModel::getBillingAddress).findFirst()));
        if(Objects.nonNull(source.getCurrentStageOfConstruction())) {
            target.setStatus(enumerationService.getEnumerationName(source.getCurrentStageOfConstruction()));
        }
        target.setConsumption(String.format("%s",source.getMonthlyConsumption()));
       // target.setSaleMTD(source.getsaleMTD);
    }

    private String getAddressAsString(Optional<AddressModel> addressModelOptional) {
        if (addressModelOptional.isPresent()) {
            StrBuilder formattedAddress = new StrBuilder();
            var adress = addressModelOptional.get();
            formattedAddress.append(adress.getLine1()).append(COMMA).append(adress.getLine2()).append(COMMA).append(adress.getErpCity()).append(COMMA).append(adress.getTaluka()).append(COMMA).append(adress.getDistrict());
            return formattedAddress.toString();
        }
        return "";
    }
}
