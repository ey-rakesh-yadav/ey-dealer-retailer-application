package com.eydms.core.prosdealer.service.impl;

import com.eydms.core.model.ProspectiveDealerModel;
import com.eydms.core.prosdealer.service.EyDmsEscalationService;
import de.hybris.platform.b2b.services.impl.DefaultB2BEscalationService;
import org.apache.log4j.Logger;

public class DefaultEyDmsEscalationService extends DefaultB2BEscalationService implements EyDmsEscalationService {

    private static final Logger LOG = Logger.getLogger(DefaultEyDmsEscalationService.class);

    @Override
    public boolean escalateOnboarding(ProspectiveDealerModel prospectiveDealerModel) {

        return false;
    }

    @Override
    public void scheduleOnboardingEscalationTask(ProspectiveDealerModel prospectiveDealerModel) {

    }

    @Override
    public boolean canEscalateOnboarding(ProspectiveDealerModel prospectiveDealerModel) {
        return false;
    }
}
