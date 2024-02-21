package com.eydms.core.order.dao;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eydms.core.enums.OrderType;
import com.eydms.core.enums.WarehouseType;
import com.eydms.core.jalo.SalesHistory;
import com.eydms.core.model.SalesHistoryModel;
import com.eydms.core.model.EyDmsCustomerModel;

import com.eydms.facades.data.RequestCustomerData;
import de.hybris.platform.b2b.dao.B2BOrderDao;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.store.BaseStoreModel;

public interface EyDmsOrderCountDao extends B2BOrderDao{

	public Integer findDirectDispatchOrdersMTDCount(UserModel currentUser, WarehouseType warehouseType,  final int month, final int year);
	Integer findOrderByExpectedDeliveryDate(UserModel user, Date estimatedDeliveryDate, String routeId);
	Integer findOrdersByStatusForSO(UserModel user, OrderStatus[] status, Boolean approvalPending);
	Integer findOrderEntriesByStatusForSO(UserModel user, OrderStatus[] status);
	public Double findMaxOrderQuantityForSO(UserModel currentUser, Date startDate, Date endDate);
	public Integer checkOrderCountBeforeThreeMonths(UserModel currentUser, Date startDate, Date endDate);
	public List<Double> findOrderQuantityListForSO(UserModel currentUser, Date startDate, Date endDate,
												   Set<String> isoCodes);

	SearchPageData<OrderModel> findOrdersListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, Boolean isCreditLimitBreached, String spApprovalFilter, Boolean approvalPending);

	SearchPageData<OrderEntryModel> findOrderEntriesListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, String spApprovalFilter);

	SearchPageData<OrderModel> findOrdersListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, String filter ,String productName , OrderType orderType, Boolean isCreditLimitBreached, String spApprovalFilter, Boolean approvalPending);

	SearchPageData<OrderEntryModel> findOrderEntriesListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, String filter ,String productName , OrderType orderType, String spApprovalFilter);

	public Map<String, Long> findOrdersInAnyStatusByDateRange(final UserModel user, final OrderStatus status);

	Integer findCreditBreachCountMTD(EyDmsCustomerModel dealer);
	Integer findISOOrderByExpectedDeliveryDate(WarehouseModel depotCode, Date estimatedDeliveryDate, String routeId);

	public Integer findCancelOrdersByStatusForSO(UserModel currentUser, OrderStatus[] status);

	public Integer findCancelOrderEntriesByStatusForSO(UserModel currentUser, OrderStatus[] status);

	SearchPageData<OrderModel> findCancelOrdersListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, String spApprovalFilter, String monthYear);

	SearchPageData<OrderEntryModel> findCancelOrderEntriesListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, String spApprovalFilter, String monthYear);

	SearchPageData<OrderModel> findCancelOrdersListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, String filter ,String productName , OrderType orderType, String spApprovalFilter, String monthYear);

	SearchPageData<OrderEntryModel> findCancelOrderEntriesListByStatusForSO(UserModel user, BaseStoreModel store, OrderStatus[] status, SearchPageData searchPageData, String filter ,String productName , OrderType orderType, String spApprovalFilter, String monthYear);

	public Integer checkOrderCountBeforeThreeMonths(EyDmsCustomerModel eydmsCustomer);

	public Double findMaxOrderQuantityForSO(EyDmsCustomerModel eydmsCustomer);

	List<Double> findOrderQuantityListForSO(String subArea);

	SearchPageData<OrderEntryModel> findOrderEntriesListByStatusForEPOD(UserModel user, BaseStoreModel store, List<String> Status, SearchPageData searchPageData, String filter);

	List<List<Object>> getDealerDetailedSummaryList(RequestCustomerData requestCustomerData);
	Map<String, Object> findMaxInvoicedDateAndQunatity(UserModel user);

	void getApprovalLevelByUser(UserModel user, Map<String, Object> attr);

	List<SalesHistoryModel> getNCREntriesExistingInOrderEntry();

	List<String> getNCREntriesNotExistingInOrderEntry();
}