package com.eydms.core.services;

import com.eydms.core.enums.LeadType;
import com.eydms.core.model.NetworkAdditionPlanModel;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.facades.data.*;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;

public interface EYDMSNewNetworkService {
    List<List<Object>> getInfluencerDetailedPaginatedSummaryList(RequestCustomerData requestCustomerData);
    List<List<Object>> getRetailedDetailedPaginatedSummaryList(RequestCustomerData requestCustomerData);

    List<List<Object>> getDealerDetailedSummaryList(RequestCustomerData requestCustomerData);

    List<NetworkAdditionPlanModel> getProposedPlanSummaryList(LeadType leadType, List<EyDmsUserModel> soForUser);

    List<NetworkAdditionPlanModel> getProposedPlansBySO(LeadType leadType, List<EyDmsUserModel> soForUser,String filter);

    boolean updateTargetStatusForApprovalNwAddition(EYDMSNetworkAdditionPlanData salesApprovalData);

    boolean targetSendForRevisionNwAddition(ProposePlanData salesRevisedTargetData);


    NetworkAdditionPlanModel getProposedPlanViewForTSMRH(String status, String id);

    SearchPageData<NetworkAdditionPlanModel> getProposedPlanSummaryListForTSMRH(SearchPageData searchPageData,LeadType leadType,List<String> statuses,boolean isPendingForApproval,List<EyDmsUserModel> soForUser, List<EyDmsUserModel> tsmForUser, EyDmsUserModel currentUser);


    List<NetworkAdditionPlanModel> getCountOfProposedPlanSummaryListForRH(LeadType leadType, List<EyDmsUserModel> tsmForUser);

    Integer getApprovedAdditionSumForTSMRH(LeadType leadType,List<EyDmsUserModel> soForUser, List<EyDmsUserModel> tsmForUser, EyDmsUserModel currentUser);

}
