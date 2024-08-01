package com.scl.facades.lead;

import com.scl.facades.lead.data.LeadData;

public interface SclLeadFacade {

    /**
     * create or update lead model with lead data
     * @param leadData
     * @return
     */
    String updateLead(final LeadData leadData);

    /**
     * fetches lead with given leadID
     * @param leadId
     * @return
     */
    LeadData getLeadForLeadId(final String leadId);
}
