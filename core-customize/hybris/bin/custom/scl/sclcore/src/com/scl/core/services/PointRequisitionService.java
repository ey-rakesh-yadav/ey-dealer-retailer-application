package com.scl.core.services;


import com.scl.core.model.GiftShopModel;
import com.scl.core.model.PointRequisitionModel;
import com.scl.core.model.SclCustomerModel;

import com.scl.core.model.SubAreaMasterModel;
import com.scl.facades.data.PointRequisitionData;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;

import java.util.Date;
import java.util.List;

public interface PointRequisitionService {
    String saveInfluencerPointRequisitionDetails(PointRequisitionData pointRequisitionData);

    Double getPointsForRequisition(String productCode, String influencer);

    Integer getAllocationRequestCount();
   List<PointRequisitionModel> getAllocationRequestList();


    SearchPageData<PointRequisitionModel> getListOfAllPointRequisition(boolean isDraft, String filter, List<String> statuses, SearchPageData searchPageData, String requisitionId, String influencerCode);

    List<List<Object>> requisitionRaisedDetails();

    Integer pendingRequistionsDetails();

    Double pointsFromPreviousYear();

    Double pointsEarnedCurrentYear();

    Double pointsRedeemed();

    Double totalRedeemablePoints();

    List<GiftShopModel> giftShopSummary();

    void saveCustomerInfluencerMapping(SclCustomerModel fromCustomer, SclCustomerModel influencer, BaseSiteModel brand);

    void orderCountIncrementForCustomerRetailerMap(Date deliveryDate, SclCustomerModel fromCustomer, SclCustomerModel influencer, BaseSiteModel brand,Double qty);

    Double bagOffTake();

    PointRequisitionData getRequistionDetails(String requisitionId);

    ErrorListWsDTO updateAllocationRequestCards(String requisitionId, String status, String rejectionReason);

    SearchPageData<SclCustomerModel> getSavedDealerRetailer(SearchPageData searchPageData);

    SearchPageData<SclCustomerModel> getList(String filter, SearchPageData searchPageData);

}
