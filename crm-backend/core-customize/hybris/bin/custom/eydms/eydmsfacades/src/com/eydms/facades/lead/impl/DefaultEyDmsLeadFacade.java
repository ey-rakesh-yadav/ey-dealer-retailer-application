package com.eydms.facades.lead.impl;

import com.eydms.core.lead.services.EyDmsLeadService;
import com.eydms.core.model.LeadModel;
import com.eydms.facades.lead.data.LeadData;
import com.eydms.facades.lead.EyDmsLeadFacade;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Resource;

import java.util.UUID;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class DefaultEyDmsLeadFacade implements EyDmsLeadFacade {

    private ModelService modelService;
    private EyDmsLeadService eydmsLeadService;
    private Converter<LeadData, LeadModel> eydmsLeadReverseConverter;
    private Converter<LeadModel , LeadData> eydmsLeadConverter;

    /**
     * create or update lead model with lead data
     * @param leadData
     * @return
     */
    @Override
    public String updateLead(LeadData leadData) {
        validateParameterNotNullStandardMessage("leadData", leadData);
        LeadModel leadModel;
        String leadId = UUID.randomUUID().toString();
        if(StringUtils.isBlank(leadData.getLeadId())){
            leadModel = modelService.create(LeadModel.class);
            leadModel.setLeadId(leadId);
        }
        else {
            leadModel = eydmsLeadService.findLeadByLeadId(leadData.getLeadId());
            leadId = leadData.getLeadId();
        }
        modelService.save(eydmsLeadReverseConverter.convert(leadData,leadModel));
        return leadId;
    }

    /**
     * fetches lead with given leadID
     * @param leadId
     * @return
     */
    @Override
    public LeadData getLeadForLeadId(String leadId) {

         LeadData leadData = new LeadData();
         final LeadModel leadModel = eydmsLeadService.findLeadByLeadId(leadId);
         return eydmsLeadConverter.convert(leadModel,leadData);
    }

    public ModelService getModelService() {
        return modelService;
    }

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    public EyDmsLeadService getEyDmsLeadService() {
        return eydmsLeadService;
    }

    public void setEyDmsLeadService(EyDmsLeadService eydmsLeadService) {
        this.eydmsLeadService = eydmsLeadService;
    }

    public Converter<LeadData, LeadModel> getEyDmsLeadReverseConverter() {
        return eydmsLeadReverseConverter;
    }

    public void setEyDmsLeadReverseConverter(Converter<LeadData, LeadModel> eydmsLeadReverseConverter) {
        this.eydmsLeadReverseConverter = eydmsLeadReverseConverter;
    }

    public Converter<LeadModel, LeadData> getEyDmsLeadConverter() {
        return eydmsLeadConverter;
    }

    public void setEyDmsLeadConverter(Converter<LeadModel, LeadData> eydmsLeadConverter) {
        this.eydmsLeadConverter = eydmsLeadConverter;
    }

}
