package com.eydms.facades.populators.prosdealer;

import com.eydms.core.model.*;
import com.eydms.facades.prosdealer.data.*;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.ArrayList;
import java.util.List;

public class ProspectiveDealerExtPopulator implements Populator<ProspectiveDealerModel, ProsDealerData> {


    private Converter<AddressModel, AddressData> addressConverter;

    @Override
    public void populate(ProspectiveDealerModel source, ProsDealerData target) throws ConversionException {

        target.setDated(source.getDated());
        target.setDdNo(source.getDdNo());
        target.setIsDealerCapableForFormFilling(source.getIsDealerCapableForFormFilling());
        target.setIsDealerContacted(source.getIsDealerContacted());
        target.setIsSmsTriggeredToUploadDoc(source.getIsSmsTriggeredToUploadDoc());
        target.setPaymentType(null!= source.getPaymentType() ? source.getPaymentType().getCode():null);
        target.setSecurityDepositAmt(source.getSecurityDepositAmt());
        target.setSite(null!= source.getSite()? source.getSite().getUid():null);
        target.setTransactionDate(source.getTransactionDate());
        target.setTransactionID(source.getTransactionID());
    }

    public Converter<AddressModel, AddressData> getAddressConverter() {
        return addressConverter;
    }

    public void setAddressConverter(Converter<AddressModel, AddressData> addressConverter) {
        this.addressConverter = addressConverter;
    }
}
