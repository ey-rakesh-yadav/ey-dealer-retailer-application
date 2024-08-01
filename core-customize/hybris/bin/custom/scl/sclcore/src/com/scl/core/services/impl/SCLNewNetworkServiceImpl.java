package com.scl.core.services.impl;

import com.scl.core.dao.NetworkDao;
import com.scl.core.dao.OrderRequisitionDao;
import com.scl.core.dao.PointRequisitionDao;
import com.scl.core.enums.*;
import com.scl.core.model.*;
import com.scl.core.order.dao.SclOrderCountDao;
import com.scl.core.services.SCLNewNetworkService;
import com.scl.core.services.TerritoryManagementService;
import com.scl.facades.data.*;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SCLNewNetworkServiceImpl implements SCLNewNetworkService {

    private static final Logger LOG = Logger.getLogger(SalesPlanningServiceImpl.class);
    @Resource
    UserService userService;
    @Resource
    ModelService modelService;
    @Resource
    BaseSiteService baseSiteService;
    @Autowired
    TerritoryManagementService territoryManagementService;
    @Autowired
    PointRequisitionDao pointRequisitionDao;
    @Autowired
    OrderRequisitionDao orderRequisitionDao;
    @Autowired
    SclOrderCountDao sclOrderCountDao;
    @Autowired
    SclWorkflowServiceImpl sclWorkflowService;

   @Autowired
   NetworkDao networkDao;

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public ModelService getModelService() {
        return modelService;
    }

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    public BaseSiteService getBaseSiteService() {
        return baseSiteService;
    }

    public void setBaseSiteService(BaseSiteService baseSiteService) {
        this.baseSiteService = baseSiteService;
    }

    @Override
    public List<List<Object>> getInfluencerDetailedPaginatedSummaryList(RequestCustomerData requestCustomerData){
        return pointRequisitionDao.getInfluencerDetailedPaginatedSummaryList(requestCustomerData);
    }

    @Override
    public List<List<Object>> getRetailedDetailedPaginatedSummaryList(RequestCustomerData requestCustomerData) {
        return orderRequisitionDao.getRetailedDetailedPaginatedSummaryList(requestCustomerData);
    }

    @Override
    public List<List<Object>> getDealerDetailedSummaryList(RequestCustomerData requestCustomerData) {
        return sclOrderCountDao.getDealerDetailedSummaryList(requestCustomerData);
    }

    @Override
    public List<NetworkAdditionPlanModel> getProposedPlanSummaryList(LeadType leadType,List<SclUserModel> soForUser) {
        return networkDao.getNetworkAdditionPlanSummary(leadType,soForUser);
    }

    @Override
    public List<NetworkAdditionPlanModel> getProposedPlansBySO(LeadType leadType, List<SclUserModel> soForUser,String filter) {
        return networkDao.getProposedPlansBySO(leadType,soForUser,filter);

    }

    @Override
    public boolean updateTargetStatusForApprovalNwAddition(SCLNetworkAdditionPlanData salesApprovalData) {

        SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
        BaseSiteModel baseSite = (BaseSiteModel) baseSiteService.getCurrentBaseSite();
        NetworkAdditionPlanModel networkAdditionPlanModel=null;

        SclWorkflowModel sclWorkflowModel=null;
        List<SubAreaMasterModel> subAreaMasterModelList = new ArrayList<>();
       /* sclUserModel=salesApprovalData.getRaisedBy();
        SclUserModel sclUserModel= (SclUserModel) userService.getUserForUID(salesApprovalData.getRaisedByUid());
        String subArea=salesApprovalData.getSubAreaMasterId();
        SubAreaMasterModel subAreaMaster= networkDao.getSubareaForSOString(subArea);
                networkAdditionPlanModel = networkDao.getNetworkAdditionPlan(sclUserModel);*/
        networkAdditionPlanModel = networkDao.getNetworkAdditionPlan(salesApprovalData.getId());

                if(currentUser.getUserType()!=null && currentUser.getUserType().equals(SclUserType.TSM)) {
                    try {


                        if (networkAdditionPlanModel.getApprovalWorkflow() != null) {
                            SclWorkflowActionModel sclWorkflowActionModel = networkAdditionPlanModel.getApprovalWorkflow().getCurrent();


                            sclWorkflowService.updateWorkflowAction(sclWorkflowActionModel, currentUser, WorkflowActions.APPROVED, "NetworkAddition is approved");

                            networkAdditionPlanModel.setApprovalLevel(null);
                            networkAdditionPlanModel.setStatus(NetworkAdditionStatus.APPROVED_BY_TSM);
                            networkAdditionPlanModel.setEnableApproveFormCompletion(Boolean.FALSE);
                            networkAdditionPlanModel.setIsTargetApproved(Boolean.TRUE);
                            networkAdditionPlanModel.setApprovedBy(currentUser);
                            modelService.save(networkAdditionPlanModel);

                             return true;
                        }
                    }
                    catch (ModelSavingException e)
                    {
                        LOG.error("Error occurred while approving target "+e.getMessage()+"\n");
                        return false;
                    }
                }
                else if(currentUser.getUserType()!=null && currentUser.getUserType().equals(SclUserType.RH))
                {
                    try {

                        if (networkAdditionPlanModel.getApprovalWorkflow() != null) {
                            SclWorkflowActionModel sclWorkflowActionModel = networkAdditionPlanModel.getApprovalWorkflow().getCurrent();


                            sclWorkflowService.updateWorkflowAction(sclWorkflowActionModel, currentUser, WorkflowActions.APPROVED, "NetworkAddition is approved");

                            networkAdditionPlanModel.setApprovalLevel(null);
                            networkAdditionPlanModel.setStatus(NetworkAdditionStatus.APPROVED_BY_RH);
                            networkAdditionPlanModel.setIsTargetApproved(true);
                            networkAdditionPlanModel.setEnableApproveFormCompletion(Boolean.FALSE);
                            networkAdditionPlanModel.setApprovedBy(currentUser);
                            modelService.save(networkAdditionPlanModel);

                            return true;
                        }
                    }
                    catch (ModelSavingException e)
                    {
                        LOG.error("Error occurred while approving target "+e.getMessage()+"\n");
                        return false;
                    }
                }
                else
                {
                    return false;
                }
                return true;

    }

   @Override
    public boolean targetSendForRevisionNwAddition(ProposePlanData salesRevisedTargetData) {
        SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
        BaseSiteModel baseSite = (BaseSiteModel) baseSiteService.getCurrentBaseSite();
        NetworkAdditionPlanModel networkAdditionPlanModel=null;
        //String sclUserModel=null;
        SclWorkflowModel sclWorkflow =null;

       networkAdditionPlanModel = networkDao.getNetworkAdditionPlan(salesRevisedTargetData.getId());
                // annualSalesModel = viewPlannedSalesforDealersRetailersMonthWise(salesRevisedTargetData.getSubAreaId(), sclUserModel, baseSite);
                if(networkAdditionPlanModel!=null) {
                    sclWorkflow = networkAdditionPlanModel.getApprovalWorkflow();
                }
                //send revision of target by tsm to so
                if(currentUser.getUserType()!=null && currentUser.getUserType().equals(SclUserType.TSM))
                {
                    try {
                               networkAdditionPlanModel.setRevisedTarget(Double.valueOf(salesRevisedTargetData.getProposedAdditionByDI()));
                               networkAdditionPlanModel.setSystemProposed(salesRevisedTargetData.getProposedAdditionByDI());
                                networkAdditionPlanModel.setRevisedBy(currentUser);
                                networkAdditionPlanModel.setCommentsForRevision(salesRevisedTargetData.getComments());

                          if (networkAdditionPlanModel.getApprovalWorkflow() != null) {
                            SclWorkflowActionModel sclWorkflowActionModel = networkAdditionPlanModel.getApprovalWorkflow().getCurrent();


                            sclWorkflowService.updateWorkflowAction(sclWorkflowActionModel, currentUser, WorkflowActions.REVISED, "NetworkAddition is revised");

                            networkAdditionPlanModel.setApprovalLevel(null);
                            networkAdditionPlanModel.setStatus(NetworkAdditionStatus.REVISED_AND_SENT_TO_SO);
                              networkAdditionPlanModel.setEnableRevisedFormCompletion(Boolean.FALSE);
                              modelService.save(networkAdditionPlanModel);

                            return true;
                        }
                    }


                    catch (ModelSavingException e)
                    {
                        LOG.error("Error occurred while sending for revision of target "+e.getMessage()+"\n");
                        return false;
                    }
                }
                else if(currentUser.getUserType()!=null && currentUser.getUserType().equals(SclUserType.RH))
                {
                    try {


                                networkAdditionPlanModel.setRevisedTarget(Double.valueOf(salesRevisedTargetData.getProposedAdditionByDI()));
                                networkAdditionPlanModel.setSystemProposed(salesRevisedTargetData.getProposedAdditionByDI());
                                networkAdditionPlanModel.setRevisedBy(currentUser);
                                networkAdditionPlanModel.setCommentsForRevision(salesRevisedTargetData.getComments());

                        if (networkAdditionPlanModel.getApprovalWorkflow() != null) {
                            SclWorkflowActionModel sclWorkflowActionModel = networkAdditionPlanModel.getApprovalWorkflow().getCurrent();

                            sclWorkflowService.updateWorkflowAction(sclWorkflowActionModel, currentUser, WorkflowActions.REVISED, "NetworkAddition is revised");

                            networkAdditionPlanModel.setApprovalLevel(null);
                            networkAdditionPlanModel.setStatus(NetworkAdditionStatus.REVISED_AND_SENT_TO_SO);
                            networkAdditionPlanModel.setEnableRevisedFormCompletion(Boolean.FALSE);
                            modelService.save(networkAdditionPlanModel);

                            return true;
                        }
                    }
                    catch (ModelSavingException e)
                    {
                        LOG.error("Error occurred while sending for revision of target "+e.getMessage()+"\n");
                        return false;
                    }
                }
                else {
                    return false;
                }
        return true;
    }

    @Override
    public NetworkAdditionPlanModel getProposedPlanViewForTSMRH(String status,String id) {
        return networkDao.getProposedPlanViewForTSMRH(status,id);
    }

    @Override
    public SearchPageData<NetworkAdditionPlanModel> getProposedPlanSummaryListForTSMRH(SearchPageData searchPageData,LeadType leadType,List<String> statuses,boolean isPendingForApproval,List<SclUserModel> soForUser, List<SclUserModel> tsmForUser,SclUserModel currentUser) {
        return networkDao.getNetworkAdditionPlanSummaryForTSMRH(searchPageData,leadType,statuses,isPendingForApproval,soForUser,tsmForUser,currentUser);
    }

    @Override
    public List<NetworkAdditionPlanModel> getCountOfProposedPlanSummaryListForRH(LeadType leadType, List<SclUserModel> tsmForUser) {
        return networkDao.getCountOfProposedPlanSummaryListForRH(leadType,tsmForUser);
    }

    @Override
    public Integer getApprovedAdditionSumForTSMRH(LeadType leadType,List<SclUserModel> soForUser, List<SclUserModel> tsmForUser, SclUserModel currentUser) {
        return networkDao.getApprovedAdditionSumForTSMRH(leadType,soForUser,tsmForUser,currentUser);
    }


}
