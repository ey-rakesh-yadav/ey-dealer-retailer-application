package com.scl.facades.populators;

import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.SclUserModel;
import com.scl.core.model.SubAreaMasterModel;
import com.scl.core.services.NetworkService;
import com.scl.facades.data.SalesPromoterDetailsData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.collections.CollectionUtils;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.stream.Collectors;

public class SPDetails360Populator implements Populator<SalesPromoterDetailsData, SclCustomerModel> {
    @Resource
    private NetworkService networkService;
    @Override
    public void populate(SalesPromoterDetailsData target, SclCustomerModel source) throws ConversionException {
        target.setCity(source.getName());
        target.setGroup("Sales Promoter");
        target.setPotential(networkService.getSPNetworkPotentialMTD(source));
        target.setSelfSale(networkService.getSPNetwokSalesMTD(source));
        if(CollectionUtils.isNotEmpty(source.getAddresses())) {
            var address = source.getAddresses().iterator().next();
            if(Objects.nonNull(address)){
                target.setCity(address.getCity());
                target.setDistrict(address.getDistrict());
                target.setTaluka(source.getTaluka());
            }
        }
        var subAreaMasters=networkService.getSubAreaForSalesPromoter(source);
       target.setSubAreas(subAreaMasters.stream().map(SubAreaMasterModel::getTaluka).collect(Collectors.toList()));
       var salesOfficers=networkService.getSalesOfficersForSubArea(subAreaMasters);
       target.setSalesOfficers(salesOfficers.stream().map(SclUserModel::getName).collect(Collectors.toList()));
       target.setTotalOutstanding(networkService.getTotalOutstandingForPromoter(source));
       target.setOutstandingDays(networkService.getOutstandingDaysForPromoter(source));
    }
}
