package com.scl.core.dao;

import com.scl.core.enums.InfluencerType;
import com.scl.core.model.*;
import com.scl.facades.data.InfluencerPointRequisitionRequestData;
import com.scl.facades.data.RequestCustomerData;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;

import java.util.List;

public interface PointRequisitionDao {

    public PointRequisitionModel findByRequisitionId(String requisitionId);

    ProductPointMasterModel getPointsForRequisition(ProductModel product, String schemeId);

    Integer getAllocationRequestCount();
    List<PointRequisitionModel> getAllocationRequestList();


    SearchPageData<PointRequisitionModel> getListOfAllPointRequisition(boolean isDraft, String filter, List<String> statuses, SearchPageData searchPageData, String requisitionId, String influencerCode);

	List<List<Object>> getBagLiftedMTDforInfluencers(List<SclCustomerModel> requestRaisedForList, String startDate,
			String endDate,List<String> doList,List<String> territoryList);

    List<List<Object>> requisitionRaisedDetails(SclCustomerModel currentUser);

    Integer pendingRequisitionDetails(SclCustomerModel currentUser);

    Double pointsFromPreviousYear(SclCustomerModel currentUser);

    Double pointsEarnedCurrentYear(SclCustomerModel currentUser);

    Double pointsRedeemed(SclCustomerModel currentUser);

    Double totalRedeemablePoints(SclCustomerModel currentUser);

    List<GiftShopModel> giftShopSummary();
    CustomersInfluencerMapModel getInfluencersListForCustomers(SclCustomerModel fromCustomer, SclCustomerModel influencer, BaseSiteModel brand);

    Double bagOffTake(SclCustomerModel currentUser);

    PointRequisitionModel getRequistionDetails(String requisitionId);

    SearchPageData<SclCustomerModel> getList(SearchPageData searchPageData, SclCustomerModel sclCustomerModel, BaseSiteModel site);

    SearchPageData<SclCustomerModel> getAllCustomerForTerritories(String filter,SearchPageData searchPageData,List<SubAreaMasterModel> subArea);

    SearchPageData<SclCustomerModel> getSavedDealerRetailer(SearchPageData searchPageData, SclCustomerModel sclCustomerModel, BaseSiteModel site);

    List<List<Object>> getInfluencerDetailedPaginatedSummaryList(RequestCustomerData requestCustomerData);


}
