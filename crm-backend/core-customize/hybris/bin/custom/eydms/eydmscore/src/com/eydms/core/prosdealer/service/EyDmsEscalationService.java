package com.eydms.core.prosdealer.service;

import com.eydms.core.model.ProspectiveDealerModel;
import de.hybris.platform.b2b.services.B2BEscalationService;
import de.hybris.platform.core.model.order.OrderModel;

public interface EyDmsEscalationService extends B2BEscalationService {

     boolean escalateOnboarding(final ProspectiveDealerModel prospectiveDealerModel);

     void scheduleOnboardingEscalationTask(final ProspectiveDealerModel prospectiveDealerModel);

     boolean canEscalateOnboarding(ProspectiveDealerModel prospectiveDealerModel);
}
