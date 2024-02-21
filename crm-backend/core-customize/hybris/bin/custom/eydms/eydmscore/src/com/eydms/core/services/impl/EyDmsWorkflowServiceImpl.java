package com.eydms.core.services.impl;

import com.eydms.core.dao.EyDmsWorkflowDao;
import com.eydms.core.enums.TerritoryLevels;
import com.eydms.core.enums.WorkflowActions;
import com.eydms.core.enums.WorkflowStatus;
import com.eydms.core.enums.WorkflowType;
import com.eydms.core.model.*;
import com.eydms.core.services.EyDmsWorkflowService;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;

public class EyDmsWorkflowServiceImpl implements EyDmsWorkflowService {

    private static final Logger LOG = Logger.getLogger(EyDmsWorkflowServiceImpl.class);

    @Autowired
    EyDmsWorkflowDao eydmsWorkflowDao;
    @Autowired
    ModelService modelService;

    @Resource
    private KeyGenerator eydmsWorkflowCodeGenerator;

    @Resource
    private KeyGenerator workflowActionIndexGenerator;

    @Override
    public EyDmsWorkflowModel saveWorkflow(String name, WorkflowStatus status, WorkflowType type) {
            EyDmsWorkflowModel eydmsWorkflowModel = modelService.create(EyDmsWorkflowModel.class);

            eydmsWorkflowModel.setCode(eydmsWorkflowCodeGenerator.generate().toString());
            eydmsWorkflowModel.setName(name);
            eydmsWorkflowModel.setStartTime(new Date());
            eydmsWorkflowModel.setStatus(status);
            eydmsWorkflowModel.setType(type);
            modelService.save(eydmsWorkflowModel);
        return eydmsWorkflowModel;
    }

    @Override
    public EyDmsWorkflowActionModel saveWorkflowAction(EyDmsWorkflowModel eydmsWorkflowModel, String name, BaseSiteModel baseSite, SubAreaMasterModel subArea, TerritoryLevels territoryLevels) {
        LOG.info("inside saveWorkflow action");
        LOG.info("eydmsworkflow action required params:" + eydmsWorkflowModel + " " +name + " " + baseSite + " " + subArea + " " + territoryLevels);
        EyDmsWorkflowActionModel eydmsWorkflowActionModel = modelService.create(EyDmsWorkflowActionModel.class);

        DistrictMasterModel districtMaster = null;
        RegionMasterModel regionMaster = null;
        StateMasterModel stateMaster = null;
        eydmsWorkflowActionModel.setIndex(Integer.valueOf(workflowActionIndexGenerator.generate().toString()));
        eydmsWorkflowActionModel.setName(name);
        eydmsWorkflowActionModel.setBrand(baseSite);
        eydmsWorkflowActionModel.setTerritoryLevel(territoryLevels);
        eydmsWorkflowActionModel.setSubArea(subArea);
        districtMaster = subArea.getDistrictMaster();
        if(districtMaster!=null) {
            eydmsWorkflowActionModel.setDistrict(districtMaster);
            regionMaster = districtMaster.getRegion();
            if(regionMaster!=null) {
                eydmsWorkflowActionModel.setRegion(regionMaster);
                stateMaster = regionMaster.getState();
                if(stateMaster!=null){
                    eydmsWorkflowActionModel.setState(stateMaster);
                }
            }
        }
        eydmsWorkflowActionModel.setActive(false);
        eydmsWorkflowActionModel.setStartTime(new Date());
        eydmsWorkflowActionModel.setWorkflow(eydmsWorkflowModel);
        LOG.info("eydmsWorkflowActionModel" + eydmsWorkflowActionModel);
        modelService.save(eydmsWorkflowActionModel);
        if(eydmsWorkflowActionModel!=null) {

            eydmsWorkflowModel.setCurrent(eydmsWorkflowActionModel);
        }

        modelService.save(eydmsWorkflowModel);
        return eydmsWorkflowActionModel;
    }



    @Override
    public boolean updateWorkflowAction(EyDmsWorkflowActionModel eydmsWorkflowActionModel, B2BCustomerModel actionPerformedBy, WorkflowActions actionPerformed, String comment) {
        try {
            if (eydmsWorkflowActionModel != null) {
                eydmsWorkflowActionModel.setComment(comment);
                eydmsWorkflowActionModel.setActionPerformed(actionPerformed);
                eydmsWorkflowActionModel.setActionPerformedBy(actionPerformedBy);
                eydmsWorkflowActionModel.setActionPerformedDate(new Date());
                eydmsWorkflowActionModel.setEndTime(new Date());
                modelService.save(eydmsWorkflowActionModel);
                LOG.info("eydmsWorkflowAction" + Objects.nonNull(eydmsWorkflowActionModel.getIndex()));
            }
        }
        catch (ModelSavingException e){
            LOG.error("Error occurred while updating eydmsworkflow action "+e.getMessage()+"\n");
            return false;
        }
        return true;
    }
}
