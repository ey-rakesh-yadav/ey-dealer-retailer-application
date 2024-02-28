package com.eydms.facades;


import com.eydms.facades.data.*;

import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;

import java.util.List;

public interface PointRequisitionFacade {
    String saveInfluencerPointRequisitionDetails(PointRequisitionData pointRequisitionData);
    Double getPointsForRequisition(String productCode, String influencer);

    Integer getAllocationRequestCount();
    List<InfluencerPointRequisitionRequestData> getAllocationRequestList();

    SearchPageData<PointRequisitionData> getListOfAllPointRequisition(boolean isDraft, String filter, List<String> statuses, SearchPageData searchPageData, String requisitionId, String fields, String influencerCode);

    RequisitionCockpitData influencerCockpitSummary();

    GiftShopMessageListData giftShopSummary();

    PointRequisitionData getRequistionDetails(String requisitionId);

    ErrorListWsDTO updateAllocationRequestCards(String requisitionId, String status, String rejectionReason);

    SearchPageData<CustomerData> getList(String filter, SearchPageData searchPageData);

    SearchPageData<CustomerData> getSavedDealerRetailer(SearchPageData searchPageData);

    List<NetworkAdditionData> getCockpitNetworkAdditionListDetails();
}
