package com.eydms.facades.populators;

import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.core.model.SubAreaMasterModel;
import com.eydms.core.services.NetworkService;
import com.eydms.facades.data.SalesPromoterDetailsData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.collections.CollectionUtils;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.stream.Collectors;

public class SPDetails360Populator implements Populator<SalesPromoterDetailsData, EyDmsCustomerModel> {
    @Resource
    private NetworkService networkService;
    @Override
    public void populate(SalesPromoterDetailsData target, EyDmsCustomerModel source) throws ConversionException {
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
       target.setSalesOfficers(salesOfficers.stream().map(EyDmsUserModel::getName).collect(Collectors.toList()));
       target.setTotalOutstanding(networkService.getTotalOutstandingForPromoter(source));
       target.setOutstandingDays(networkService.getOutstandingDaysForPromoter(source));
    }
}
