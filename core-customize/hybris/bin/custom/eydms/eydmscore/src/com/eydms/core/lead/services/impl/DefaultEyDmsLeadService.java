package com.eydms.core.lead.services.impl;


import com.eydms.core.lead.dao.EyDmsLeadDao;
import com.eydms.core.lead.services.EyDmsLeadService;
import com.eydms.core.model.LeadModel;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

public class DefaultEyDmsLeadService implements EyDmsLeadService {

    private ModelService modelService;
    private EyDmsLeadDao eydmsLeadDao;

    private static final Logger LOG = Logger.getLogger(DefaultEyDmsLeadService.class);

    /**
     * Update lead model with some data
     * @param b2BCustomer
     * @param leadModel
     * @return
     */

    @Override
    public Boolean updateLeadEntry(final B2BCustomerModel b2BCustomer,  final LeadModel leadModel){

        leadModel.setModifiedBy(b2BCustomer);

        try{
            getModelService().save(leadModel);
        }
        catch (ModelSavingException mse){
            LOG.error("Error occured while updating Lead "+leadModel.getLeadId()+"\n");
            LOG.error("Exception is: "+mse.getMessage());
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * Service method to get lead by lead ID
     * @param leadID
     * @return
     */
    @Override
    public LeadModel findLeadByLeadId(final String leadID){
        return getEyDmsLeadDao().getLeadByLeadID(leadID);
    }

    /**
     * get all the leads
     * @return
     */
    @Override
    public List<LeadModel> getAllLeads(){
        return getEyDmsLeadDao().getAllLeadsData();
    }


    public ModelService getModelService() {
        return modelService;
    }

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    public EyDmsLeadDao getEyDmsLeadDao() {
        return eydmsLeadDao;
    }

    public void setEyDmsLeadDao(EyDmsLeadDao eydmsLeadDao) {
        this.eydmsLeadDao = eydmsLeadDao;
    }
}
