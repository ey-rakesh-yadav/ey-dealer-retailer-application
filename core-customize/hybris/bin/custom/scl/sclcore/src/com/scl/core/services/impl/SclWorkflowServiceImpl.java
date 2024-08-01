package com.scl.core.services.impl;

import com.scl.core.dao.SclWorkflowDao;
import com.scl.core.enums.TerritoryLevels;
import com.scl.core.enums.WorkflowActions;
import com.scl.core.enums.WorkflowStatus;
import com.scl.core.enums.WorkflowType;
import com.scl.core.model.*;
import com.scl.core.services.SclWorkflowService;
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

public class SclWorkflowServiceImpl implements SclWorkflowService {

    private static final Logger LOG = Logger.getLogger(SclWorkflowServiceImpl.class);

    @Autowired
    SclWorkflowDao sclWorkflowDao;
    @Autowired
    ModelService modelService;

    @Resource
    private KeyGenerator sclWorkflowCodeGenerator;

    @Resource
    private KeyGenerator workflowActionIndexGenerator;

    @Override
    public SclWorkflowModel saveWorkflow(String name, WorkflowStatus status, WorkflowType type) {
            SclWorkflowModel sclWorkflowModel = modelService.create(SclWorkflowModel.class);

            sclWorkflowModel.setCode(sclWorkflowCodeGenerator.generate().toString());
            sclWorkflowModel.setName(name);
            sclWorkflowModel.setStartTime(new Date());
            sclWorkflowModel.setStatus(status);
            sclWorkflowModel.setType(type);
            modelService.save(sclWorkflowModel);
        return sclWorkflowModel;
    }

    @Override
    public SclWorkflowActionModel saveWorkflowAction(SclWorkflowModel sclWorkflowModel, String name, BaseSiteModel baseSite, SubAreaMasterModel subArea, TerritoryLevels territoryLevels) {
        LOG.info("inside saveWorkflow action");
        LOG.info("sclworkflow action required params:" + sclWorkflowModel + " " +name + " " + baseSite + " " + subArea + " " + territoryLevels);
        SclWorkflowActionModel sclWorkflowActionModel = modelService.create(SclWorkflowActionModel.class);

        DistrictMasterModel districtMaster = null;
        RegionMasterModel regionMaster = null;
        StateMasterModel stateMaster = null;
        sclWorkflowActionModel.setIndex(Integer.valueOf(workflowActionIndexGenerator.generate().toString()));
        sclWorkflowActionModel.setName(name);
        sclWorkflowActionModel.setBrand(baseSite);
        sclWorkflowActionModel.setTerritoryLevel(territoryLevels);
        sclWorkflowActionModel.setSubArea(subArea);
        districtMaster = subArea.getDistrictMaster();
        if(districtMaster!=null) {
            sclWorkflowActionModel.setDistrict(districtMaster);
            regionMaster = districtMaster.getRegion();
            if(regionMaster!=null) {
                sclWorkflowActionModel.setRegion(regionMaster);
                stateMaster = regionMaster.getState();
                if(stateMaster!=null){
                    sclWorkflowActionModel.setState(stateMaster);
                }
            }
        }
        sclWorkflowActionModel.setActive(false);
        sclWorkflowActionModel.setStartTime(new Date());
        sclWorkflowActionModel.setWorkflow(sclWorkflowModel);
        LOG.info("sclWorkflowActionModel" + sclWorkflowActionModel);
        modelService.save(sclWorkflowActionModel);
        if(sclWorkflowActionModel!=null) {

            sclWorkflowModel.setCurrent(sclWorkflowActionModel);
        }

        modelService.save(sclWorkflowModel);
        return sclWorkflowActionModel;
    }



    @Override
    public boolean updateWorkflowAction(SclWorkflowActionModel sclWorkflowActionModel, B2BCustomerModel actionPerformedBy, WorkflowActions actionPerformed, String comment) {
        try {
            if (sclWorkflowActionModel != null) {
                sclWorkflowActionModel.setComment(comment);
                sclWorkflowActionModel.setActionPerformed(actionPerformed);
                sclWorkflowActionModel.setActionPerformedBy(actionPerformedBy);
                sclWorkflowActionModel.setActionPerformedDate(new Date());
                sclWorkflowActionModel.setEndTime(new Date());
                modelService.save(sclWorkflowActionModel);
                LOG.info("sclWorkflowAction" + Objects.nonNull(sclWorkflowActionModel.getIndex()));
            }
        }
        catch (ModelSavingException e){
            LOG.error("Error occurred while updating sclworkflow action "+e.getMessage()+"\n");
            return false;
        }
        return true;
    }
}
