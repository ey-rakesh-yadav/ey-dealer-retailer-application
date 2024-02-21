package com.eydms.facades.populators;

import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.facades.data.ProspectiveNetworkData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.util.Objects;

public class ProspectiveNetworkPopulator implements Populator<EyDmsCustomerModel, ProspectiveNetworkData> {
    @Resource
    private EnumerationService enumerationService;
    @Override
    public void populate(EyDmsCustomerModel source, ProspectiveNetworkData target) throws ConversionException {
        DecimalFormat df = new DecimalFormat("#.#");
        target.setName(source.getName());
        target.setCode(source.getUid());
        if(Objects.nonNull(source.getCounterPotential())) {
            target.setPotential( df.format(source.getCounterPotential()));
        }
        if(Objects.nonNull(source.getRetailerStageStatus())) {
            target.setStage(enumerationService.getEnumerationName(source.getRetailerStageStatus()));
        }
       var dealerGroup= source.getGroups().stream().filter(group->group.getUid().equals("EyDmsDealerGroup")).findAny();
       var retailerGroup= source.getGroups().stream().filter(group->group.getUid().equals("EyDmsRetailerGroup")).findAny();
       if(dealerGroup.isPresent()) {
           target.setCategory("Dealer");
       } else if (retailerGroup.isPresent()) {
           target.setCategory("Retailer");
       }
       
       target.setLatitude(source.getLatitude());
       target.setLongitude(source.getLongitude());
    }


}
