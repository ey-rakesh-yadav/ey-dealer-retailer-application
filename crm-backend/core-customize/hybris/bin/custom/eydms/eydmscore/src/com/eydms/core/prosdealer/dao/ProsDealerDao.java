package com.eydms.core.prosdealer.dao;

import com.eydms.core.enums.OnboardingStatus;
import com.eydms.core.model.DistrictModel;
import com.eydms.core.model.ProspectiveDealerModel;
import de.hybris.platform.servicelayer.internal.dao.Dao;

import java.util.List;

public interface ProsDealerDao extends Dao {

    ProspectiveDealerModel findProsDealerByCode(final String dealerCode);

    List<ProspectiveDealerModel> getProsDealerForSOCustomerQueryAlert();

    List<ProspectiveDealerModel> getProsDealerForSHCustomerQueryAlert();

    List<ProspectiveDealerModel> findProsDealerByDistrictAndOnboardingStatus(final OnboardingStatus onboardingStatus, final DistrictModel district);

}
