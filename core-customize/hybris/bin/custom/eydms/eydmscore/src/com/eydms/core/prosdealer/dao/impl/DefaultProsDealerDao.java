package com.eydms.core.prosdealer.dao.impl;

import com.eydms.core.enums.OnboardingStatus;
import com.eydms.core.model.DistrictModel;
import com.eydms.core.model.ProspectiveDealerModel;
import com.eydms.core.prosdealer.dao.ProsDealerDao;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class DefaultProsDealerDao extends DefaultGenericDao<ProspectiveDealerModel>  implements ProsDealerDao {


    private static final String FIND_PROS_DEALER_BY_DISTRICT_QUERY = "SELECT {" + ProspectiveDealerModel.PK + "} " +
            " FROM {" + ProspectiveDealerModel._TYPECODE + "} WHERE {" + ProspectiveDealerModel.ADDRESSES
            + "} IN ({{SELECT {"+ AddressModel.PK +"} FROM {"+AddressModel._TYPECODE+"} WHERE {"+AddressModel.DISTRICT+ "} = ?district}})";

    public DefaultProsDealerDao() {
        super(ProspectiveDealerModel._TYPECODE);
    }

    @Override
    public ProspectiveDealerModel findProsDealerByCode(final String prosDealerCode){
        validateParameterNotNullStandardMessage("dealerCode", prosDealerCode);
        final List<ProspectiveDealerModel> prosDealerList = this.find(Collections.singletonMap(ProspectiveDealerModel.DEALERCODE, prosDealerCode));
        if (prosDealerList.size() > 1)
        {
            throw new AmbiguousIdentifierException(
                    String.format("Found %d prospective dealers with the dealerCode value: '%s', which should be unique", prosDealerList.size(),
                            prosDealerCode));
        }
        else
        {
            return prosDealerList.isEmpty() ? null : prosDealerList.get(0);
        }
    }

    public List<ProspectiveDealerModel> getProsDealerForSOCustomerQueryAlert() {
        Map<String, Object> soMap = new HashMap<String, Object>();
        soMap.put(ProspectiveDealerModel.ISDEALERCONTACTED, false);
        soMap.put(ProspectiveDealerModel.ISSOALERTTRIGGERED, false);
        return this.find(soMap);
    }

    @Override
    public List<ProspectiveDealerModel> getProsDealerForSHCustomerQueryAlert() {

        Map<String, Object> shMap = new HashMap<String, Object>();
        shMap.put(ProspectiveDealerModel.ISDEALERCONTACTED, false);
        shMap.put(ProspectiveDealerModel.ISSOALERTTRIGGERED, true);
        shMap.put(ProspectiveDealerModel.ISSHALERTTRIGGERED, false);
        return this.find(shMap);
    }

    @Override
    public List<ProspectiveDealerModel> findProsDealerByDistrictAndOnboardingStatus(final OnboardingStatus onboardingStatus, final DistrictModel district){

        Map<String,Object> prosDealerLookupMap = new HashMap<>();
        prosDealerLookupMap.put(ProspectiveDealerModel.ONBOARDINGSTATUS,onboardingStatus);

        final List<ProspectiveDealerModel> prosDealerList = this.find(Collections.singletonMap(ProspectiveDealerModel.ONBOARDINGSTATUS, onboardingStatus));
        //TODO:// Put District also as query parameter
        //prosDealerLookupMap.put(ProspectiveDealerModel. ,onboardingStatus.getCode());
        return  this.find(prosDealerLookupMap);
    }

}
