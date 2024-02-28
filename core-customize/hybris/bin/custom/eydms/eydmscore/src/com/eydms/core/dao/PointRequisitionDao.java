package com.eydms.core.dao;

import com.eydms.core.enums.InfluencerType;
import com.eydms.core.model.*;
import com.eydms.facades.data.InfluencerPointRequisitionRequestData;
import com.eydms.facades.data.RequestCustomerData;
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

	List<List<Object>> getBagLiftedMTDforInfluencers(List<EyDmsCustomerModel> requestRaisedForList, String startDate,
			String endDate,List<String> doList,List<String> subAreaList);

    List<List<Object>> requisitionRaisedDetails(EyDmsCustomerModel currentUser);

    Integer pendingRequisitionDetails(EyDmsCustomerModel currentUser);

    Double pointsFromPreviousYear(EyDmsCustomerModel currentUser);

    Double pointsEarnedCurrentYear(EyDmsCustomerModel currentUser);

    Double pointsRedeemed(EyDmsCustomerModel currentUser);

    Double totalRedeemablePoints(EyDmsCustomerModel currentUser);

    List<GiftShopModel> giftShopSummary();
    CustomersInfluencerMapModel getInfluencersListForCustomers(EyDmsCustomerModel fromCustomer, EyDmsCustomerModel influencer, BaseSiteModel brand);

    Double bagOffTake(EyDmsCustomerModel currentUser);

    PointRequisitionModel getRequistionDetails(String requisitionId);

    SearchPageData<EyDmsCustomerModel> getList(SearchPageData searchPageData, EyDmsCustomerModel eydmsCustomerModel, BaseSiteModel site);

    SearchPageData<EyDmsCustomerModel> getAllCustomerForTerritories(String filter,SearchPageData searchPageData,List<SubAreaMasterModel> subArea);

    SearchPageData<EyDmsCustomerModel> getSavedDealerRetailer(SearchPageData searchPageData, EyDmsCustomerModel eydmsCustomerModel, BaseSiteModel site);

    List<List<Object>> getInfluencerDetailedPaginatedSummaryList(RequestCustomerData requestCustomerData);


}
