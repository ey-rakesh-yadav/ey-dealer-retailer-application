package com.eydms.facades.populators.prosdealer;

import com.eydms.core.model.ProspectiveDealerModel;
import com.eydms.facades.prosdealer.data.BasicProsDealerData;
import com.eydms.facades.prosdealer.data.ProsDealerData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.AddressModel;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class ProspectiveDealerBasicPopulator implements Populator<ProspectiveDealerModel, ProsDealerData> {

    @Override
    public void populate(ProspectiveDealerModel source, ProsDealerData target) {
        BasicProsDealerData basicProsDealerData = new BasicProsDealerData();

        basicProsDealerData.setName(source.getName());
        basicProsDealerData.setEmail(source.getEmail());


        AddressModel address = source.getAddresses() != null && !source.getAddresses().isEmpty() ? ((List<AddressModel> )source.getAddresses()).get(0) : null;

        if (null != address) {
            basicProsDealerData.setLine1(address.getLine1());
            basicProsDealerData.setLine2(address.getLine2());
            //basicProsDealerData.setMobileNo(address.getMobileNo());
            /*basicProsDealerData.setPinCode(address.getPincode());
            basicProsDealerData.setStateCode(null != address.getState() ? address.getState().getIsocode() : StringUtils.EMPTY);
            basicProsDealerData.setTalukaCode(null != address.getTaluka() ? address.getTaluka().getIsocode() : StringUtils.EMPTY);
            basicProsDealerData.setCityCode(null != address.getCity() ? address.getCity().getIsocode() : StringUtils.EMPTY);
            basicProsDealerData.setDistrictCode(null != address.getDistricts() ? address.getDistricts().getIsocode() : StringUtils.EMPTY);*/
        }
        target.setBasicDetails(basicProsDealerData);
    }
}
