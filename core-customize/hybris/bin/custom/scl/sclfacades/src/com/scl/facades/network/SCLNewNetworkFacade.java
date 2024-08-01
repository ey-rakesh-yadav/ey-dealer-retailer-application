package com.scl.facades.network;

import com.scl.core.enums.LeadType;
import com.scl.facades.data.*;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;

public interface SCLNewNetworkFacade {

    List<InfluencerSummaryData> getInfluencerDetailedPaginatedSummaryList(RequestCustomerData requestCustomerData);
    List<DealerCurrentNetworkData> getRetailedDetailedPaginatedSummaryList(RequestCustomerData requestCustomerData);

    List<DealerCurrentNetworkData> getDealerDetailedSummaryList(RequestCustomerData requestCustomerData);

    Integer getProposalCount(String leadType);

    ProposePlanListData getProposedPlanSummaryList(LeadType leadType);

    ProposePlanListData getProposedPlansBySO(LeadType leadType, String filter);

    String getProposedPlanViewDetails(SCLNetworkAdditionPlanData planData);

    boolean updateTargetStatusForApprovalNwAddition(SCLNetworkAdditionPlanData salesApprovalData);

    boolean targetSendForRevisionNwAddition(ProposePlanData salesRevisedTargetData);

    SCLNetworkAdditionPlanData proposedPlanViewForTSMRH(String status, String id);

    ProposePlanListData getProposedPlanSummaryListForTSMRH(SearchPageData searchPageData, LeadType leadType,List<String> statuses,boolean isPendingForApproval);

    boolean targetSendToRhShNwAddition(LeadType leadType);

}
