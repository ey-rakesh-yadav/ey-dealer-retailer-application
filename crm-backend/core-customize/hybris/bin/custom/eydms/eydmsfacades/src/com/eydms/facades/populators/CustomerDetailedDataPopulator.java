package com.eydms.facades.populators;

import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.facades.customer.EyDmsCustomerFacade;
import com.eydms.facades.data.CustomerDetailedData;
import com.eydms.facades.data.EYDMSFinancialInfoData;
import com.eydms.facades.data.EYDMSRetailerData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import javax.annotation.Resource;

public class CustomerDetailedDataPopulator implements Populator<EyDmsCustomerModel, CustomerDetailedData> {
    @Resource
    private Converter<EyDmsCustomerModel, EYDMSRetailerData> eydmsRetailerConverter;

    @Resource
    private EyDmsCustomerFacade eydmsCustomerFacade;

    @Resource
    private Converter<EyDmsCustomerModel, EYDMSFinancialInfoData>  financialInfoConverter;

    @Override
    public void populate(EyDmsCustomerModel source, CustomerDetailedData target) {
        target.setPartnerDetails(eydmsRetailerConverter.convert(source));
        target.setCompanyDetails(eydmsCustomerFacade.getCompanyDetails(source.getUid()));
        target.setBusinessDetails(eydmsCustomerFacade.getBusinessInfo(source.getUid()));
        target.setFinancialDetails(financialInfoConverter.convert(source));
    }

}
