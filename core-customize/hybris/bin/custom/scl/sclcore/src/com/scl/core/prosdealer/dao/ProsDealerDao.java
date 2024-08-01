package com.scl.core.prosdealer.dao;

import com.scl.core.enums.OnboardingStatus;
import com.scl.core.model.DistrictModel;
import com.scl.core.model.ProspectiveDealerModel;
import de.hybris.platform.servicelayer.internal.dao.Dao;

import java.util.List;

public interface ProsDealerDao extends Dao {

    ProspectiveDealerModel findProsDealerByCode(final String dealerCode);

    List<ProspectiveDealerModel> getProsDealerForSOCustomerQueryAlert();

    List<ProspectiveDealerModel> getProsDealerForSHCustomerQueryAlert();

    List<ProspectiveDealerModel> findProsDealerByDistrictAndOnboardingStatus(final OnboardingStatus onboardingStatus, final DistrictModel district);

}
