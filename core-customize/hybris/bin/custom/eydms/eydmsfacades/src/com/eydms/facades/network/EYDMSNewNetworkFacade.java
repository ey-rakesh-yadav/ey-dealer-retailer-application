package com.eydms.facades.network;

import com.eydms.core.enums.LeadType;
import com.eydms.facades.data.*;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;

public interface EYDMSNewNetworkFacade {

    List<InfluencerSummaryData> getInfluencerDetailedPaginatedSummaryList(RequestCustomerData requestCustomerData);
    List<DealerCurrentNetworkData> getRetailedDetailedPaginatedSummaryList(RequestCustomerData requestCustomerData);

    List<DealerCurrentNetworkData> getDealerDetailedSummaryList(RequestCustomerData requestCustomerData);

    Integer getProposalCount(String leadType);

    ProposePlanListData getProposedPlanSummaryList(LeadType leadType);

    ProposePlanListData getProposedPlansBySO(LeadType leadType, String filter);

    String getProposedPlanViewDetails(EYDMSNetworkAdditionPlanData planData);

    boolean updateTargetStatusForApprovalNwAddition(EYDMSNetworkAdditionPlanData salesApprovalData);

    boolean targetSendForRevisionNwAddition(ProposePlanData salesRevisedTargetData);

    EYDMSNetworkAdditionPlanData proposedPlanViewForTSMRH(String status, String id);

    ProposePlanListData getProposedPlanSummaryListForTSMRH(SearchPageData searchPageData, LeadType leadType,List<String> statuses,boolean isPendingForApproval);

    boolean targetSendToRhShNwAddition(LeadType leadType);

}
