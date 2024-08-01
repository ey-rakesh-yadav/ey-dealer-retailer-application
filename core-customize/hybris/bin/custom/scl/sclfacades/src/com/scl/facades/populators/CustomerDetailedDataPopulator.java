package com.scl.facades.populators;

import com.scl.core.model.SclCustomerModel;
import com.scl.facades.customer.SclCustomerFacade;
import com.scl.facades.data.CustomerDetailedData;
import com.scl.facades.data.SCLFinancialInfoData;
import com.scl.facades.data.SCLRetailerData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import javax.annotation.Resource;

public class CustomerDetailedDataPopulator implements Populator<SclCustomerModel, CustomerDetailedData> {
    @Resource
    private Converter<SclCustomerModel, SCLRetailerData> sclRetailerConverter;

    @Resource
    private SclCustomerFacade sclCustomerFacade;

    @Resource
    private Converter<SclCustomerModel, SCLFinancialInfoData>  financialInfoConverter;

    @Override
    public void populate(SclCustomerModel source, CustomerDetailedData target) {
        target.setPartnerDetails(sclRetailerConverter.convert(source));
        target.setCompanyDetails(sclCustomerFacade.getCompanyDetails(source.getUid()));
        target.setBusinessDetails(sclCustomerFacade.getBusinessInfo(source.getUid()));
        target.setFinancialDetails(financialInfoConverter.convert(source));
    }

}
