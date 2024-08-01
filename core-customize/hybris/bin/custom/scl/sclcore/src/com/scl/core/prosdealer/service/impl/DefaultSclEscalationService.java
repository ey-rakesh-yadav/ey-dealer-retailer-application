package com.scl.core.prosdealer.service.impl;

import com.scl.core.model.ProspectiveDealerModel;
import com.scl.core.prosdealer.service.SclEscalationService;
import de.hybris.platform.b2b.services.impl.DefaultB2BEscalationService;
import org.apache.log4j.Logger;

public class DefaultSclEscalationService extends DefaultB2BEscalationService implements SclEscalationService {

    private static final Logger LOG = Logger.getLogger(DefaultSclEscalationService.class);

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
