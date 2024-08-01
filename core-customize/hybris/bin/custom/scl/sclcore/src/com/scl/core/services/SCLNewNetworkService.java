package com.scl.core.services;

import com.scl.core.enums.LeadType;
import com.scl.core.model.NetworkAdditionPlanModel;
import com.scl.core.model.SclUserModel;
import com.scl.facades.data.*;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;

public interface SCLNewNetworkService {
    List<List<Object>> getInfluencerDetailedPaginatedSummaryList(RequestCustomerData requestCustomerData);
    List<List<Object>> getRetailedDetailedPaginatedSummaryList(RequestCustomerData requestCustomerData);

    List<List<Object>> getDealerDetailedSummaryList(RequestCustomerData requestCustomerData);

    List<NetworkAdditionPlanModel> getProposedPlanSummaryList(LeadType leadType, List<SclUserModel> soForUser);

    List<NetworkAdditionPlanModel> getProposedPlansBySO(LeadType leadType, List<SclUserModel> soForUser,String filter);

    boolean updateTargetStatusForApprovalNwAddition(SCLNetworkAdditionPlanData salesApprovalData);

    boolean targetSendForRevisionNwAddition(ProposePlanData salesRevisedTargetData);


    NetworkAdditionPlanModel getProposedPlanViewForTSMRH(String status, String id);

    SearchPageData<NetworkAdditionPlanModel> getProposedPlanSummaryListForTSMRH(SearchPageData searchPageData,LeadType leadType,List<String> statuses,boolean isPendingForApproval,List<SclUserModel> soForUser, List<SclUserModel> tsmForUser, SclUserModel currentUser);


    List<NetworkAdditionPlanModel> getCountOfProposedPlanSummaryListForRH(LeadType leadType, List<SclUserModel> tsmForUser);

    Integer getApprovedAdditionSumForTSMRH(LeadType leadType,List<SclUserModel> soForUser, List<SclUserModel> tsmForUser, SclUserModel currentUser);

}
