package com.scl.facades.populators;

import com.scl.core.model.SclCustomerModel;
import com.scl.facades.data.ProspectiveNetworkData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.util.Objects;

public class ProspectiveNetworkPopulator implements Populator<SclCustomerModel, ProspectiveNetworkData> {
    @Resource
    private EnumerationService enumerationService;
    @Override
    public void populate(SclCustomerModel source, ProspectiveNetworkData target) throws ConversionException {
        DecimalFormat df = new DecimalFormat("#0.00");
        target.setName(source.getName());
        target.setCode(source.getUid());
        if(Objects.nonNull(source.getCounterPotential())) {
            target.setPotential( df.format(source.getCounterPotential()));
        }
        if(Objects.nonNull(source.getRetailerStageStatus())) {
            target.setStage(enumerationService.getEnumerationName(source.getRetailerStageStatus()));
        }
       var dealerGroup= source.getGroups().stream().filter(group->group.getUid().equals("SclDealerGroup")).findAny();
       var retailerGroup= source.getGroups().stream().filter(group->group.getUid().equals("SclRetailerGroup")).findAny();
       if(dealerGroup.isPresent()) {
           target.setCategory("Dealer");
       } else if (retailerGroup.isPresent()) {
           target.setCategory("Retailer");
       }
       
       target.setLatitude(source.getLatitude());
       target.setLongitude(source.getLongitude());
    }


}
