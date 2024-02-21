package com.eydms.core.lead.dao.impl;

import com.eydms.core.lead.dao.EyDmsLeadDao;
import com.eydms.core.model.LeadModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;

import java.util.Collections;
import java.util.List;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class DefaultEyDmsLeadDao extends DefaultGenericDao<LeadModel> implements EyDmsLeadDao {

    DefaultEyDmsLeadDao(){
        super(LeadModel._TYPECODE);
    }

    /**
     * Dao method to fetch lead by lead ID
     * @param leadID
     * @return
     */
    @Override
    public LeadModel getLeadByLeadID(final String leadID) {
        validateParameterNotNullStandardMessage("leadID", leadID);
        final List<LeadModel> leadList = this.find(Collections.singletonMap(LeadModel.LEADID, leadID));
        if (leadList.size() > 1)
        {
            throw new AmbiguousIdentifierException(
                    String.format("Found %d leads with the leadId value: '%s', which should be unique", leadList.size(),
                            leadID));
        }
        else
        {
            return leadList.isEmpty() ? null : leadList.get(0);
        }
    }

    /**
     * gets all leads
     * @return
     */
    @Override
    public List<LeadModel> getAllLeadsData(){
        return this.find();
    }
}
