package com.eydms.integration.cpi.order;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.enums.IncoTerms;
import com.eydms.core.enums.NetworkType;
import com.eydms.core.enums.WarehouseType;
import com.eydms.core.model.ReceiptAllocaltionModel;
import com.eydms.core.model.SalesHistoryModel;
import com.eydms.core.model.EyDmsCustomerModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

public class OrderSalesReplicationJob extends AbstractJobPerformable<CronJobModel> {

    private static final Logger LOG = Logger.getLogger(OrderSalesReplicationJob.class);

    @Resource
    EyDmsMissingOrderEntriesToSLCT eydmsMissingOrderEntriesToSLCT;

    @Resource
    FlexibleSearchService flexibleSearchService;

    @Resource
    ModelService modelService;

    @Override
    public PerformResult perform(CronJobModel cronJobModel) {
        try {

            boolean synced = true;
            boolean isDelivered = false;

            //check if order entry is present for custTransId, custTransLineId and brand combination for NCR
            List<SalesHistoryModel> ncrEntriesByNCRCombination = getNCREntriesByNCRCombination();
            if (ncrEntriesByNCRCombination != null && !ncrEntriesByNCRCombination.isEmpty()) {
                for (SalesHistoryModel salesHistoryModel : ncrEntriesByNCRCombination) {
                    OrderEntryModel orderEntry = findOrderEntryByNcrCombination(salesHistoryModel.getCustomerTransactionId(), salesHistoryModel.getCustomerTransactionLineId(), salesHistoryModel.getBrand().getUid());
                    if (orderEntry != null) {
                        orderEntry.setQuantityInMT(salesHistoryModel.getQuantity());
                        orderEntry.setInvoiceCreationDateAndTime(salesHistoryModel.getInvoiceDate());
                        orderEntry.setQuantity((long) (salesHistoryModel.getQuantity() * 1000));
                        orderEntry.setTotalPrice(salesHistoryModel.getQuantity() * orderEntry.getBasePrice());
                        modelService.save(orderEntry);

                        salesHistoryModel.setSynced(synced);
                        modelService.save(salesHistoryModel);
                    }
                }
            }

            //Case 1: get all NCR having entry in Order Entry - positive qty
            List<SalesHistoryModel> ncrEntriesExistingInOrderEntry = getNCREntriesExistingInOrderEntry();
            List<SalesHistoryModel> ncrNegativeQtyEntriesExistingInOrderEntry = getNCRNegativeQtyEntriesExistingInOrderEntry();
            OrderModel exisitngOrder=null;
            if (ncrEntriesExistingInOrderEntry != null && !ncrEntriesExistingInOrderEntry.isEmpty()) {
                for (SalesHistoryModel salesHistory : ncrEntriesExistingInOrderEntry) {
                    if (salesHistory.getSalesOrderNo() != null && salesHistory.getOrderType() != null) {
                        exisitngOrder = findOrderbyErpOrderNoAndOrderType(salesHistory.getSalesOrderNo(), salesHistory.getOrderType());
                    }
                    if (exisitngOrder != null) {
                        OrderEntryModel orderEntry = findOrderEntryByErpLineItemId(salesHistory.getLineId());
                        if (orderEntry != null) {
                            orderEntry.setQuantityInMT(salesHistory.getQuantity());
                            orderEntry.setInvoiceCreationDateAndTime(salesHistory.getInvoiceDate());
                            orderEntry.setQuantity((long) (salesHistory.getQuantity() * 1000));
                            orderEntry.setTotalPrice(salesHistory.getQuantity() * orderEntry.getBasePrice());

                            orderEntry.setBrand(salesHistory.getBrand() != null ? salesHistory.getBrand().getUid() : "");
                            orderEntry.setCustomerTransactionId(salesHistory.getCustomerTransactionId());
                            orderEntry.setCustomerTransactionLineId(salesHistory.getCustomerTransactionLineId());

                            if (orderEntry.getTruckDispatcheddate() == null && orderEntry.getInvoiceCancelDate() == null && orderEntry.getCancelledDate() == null) {
                                if (orderEntry.getSource() != null && orderEntry.getSource().getType().equals(WarehouseType.DEPOT)) {
                                    orderEntry.setTruckAllocatedDate(orderEntry.getInvoiceCreationDateAndTime());
                                    orderEntry.setTruckDispatcheddate(orderEntry.getInvoiceCreationDateAndTime());
                                    orderEntry.setStatus(OrderStatus.TRUCK_DISPATCHED);
                                    if (orderEntry.getFob() != null && orderEntry.getFob().equals(IncoTerms.EX)) {
                                        orderEntry.setDeliveredDate(orderEntry.getInvoiceCreationDateAndTime());
                                        orderEntry.setStatus(OrderStatus.DELIVERED);
                                        isDelivered = true;
                                    }
                                } else {
                                    orderEntry.setStatus(OrderStatus.INVOICED);
                                }
                            }


                            //last lifting date and network type
                            EyDmsCustomerModel cus = (EyDmsCustomerModel) orderEntry.getOrder().getUser();
                            Date existingLastLiftingDate = cus.getLastLiftingDate();
                            updateLastLiftingAndNetworkType(orderEntry, cus, existingLastLiftingDate);

                            //stock quantity
                            Double receiptQty = 0.0;
                            if (null != orderEntry.getInvoiceQuantity()) {
                                receiptQty = orderEntry.getInvoiceQuantity() * EyDmsCoreConstants.QUANTITY_INMT_TO_BAGS;
                            }
                            OrderModel orderRequistionCheck = orderEntry.getOrder();
                            if (null != orderRequistionCheck && (null == orderRequistionCheck.getRequisitions()
                                    || orderRequistionCheck.getRequisitions().size() != 1)) {
                                updateReceipts(orderEntry.getProduct(), cus, receiptQty);
                            }

                            modelService.save(orderEntry);
                            salesHistory.setSynced(synced);
                            modelService.save(salesHistory);
                        }
                    }
                }
            }

            //Case 2: get all NCR with negative qty - create new order entry
            if (ncrNegativeQtyEntriesExistingInOrderEntry != null && !ncrNegativeQtyEntriesExistingInOrderEntry.isEmpty()) {
                OrderEntryModel entryModel = null;
                OrderModel existingOrder = null;
                for (SalesHistoryModel salesHistoryModel : ncrNegativeQtyEntriesExistingInOrderEntry) {
                    //create new order entry
                    if(salesHistoryModel.getLineId()!=null) {
                        //find order based on erp line item id
                        existingOrder= findOrderByErpLineId(salesHistoryModel.getLineId());
                    }
                    if(existingOrder!=null) {
                        if (existingOrder.getEntries() != null && !existingOrder.getEntries().isEmpty()) {
                            AbstractOrderEntryModel baseEntryModel = existingOrder.getEntries().get(0);
                            entryModel = modelService.create(OrderEntryModel.class);
                            entryModel.setOrder(existingOrder);
                            entryModel.setQuantity((long) (salesHistoryModel.getQuantity() * 1000));
                            entryModel.setQuantityInMT(salesHistoryModel.getQuantity());
                            entryModel.setInvoiceCreationDateAndTime(salesHistoryModel.getInvoiceDate());
                            entryModel.setProduct(baseEntryModel.getProduct());
                            entryModel.setUnit(baseEntryModel.getUnit());
                            entryModel.setEntryNumber(addEntryNumber(existingOrder));
                            entryModel.setSequence(entryModel.getEntryNumber() + 1);
                            double basePrice = baseEntryModel.getBasePrice() != null ? baseEntryModel.getBasePrice() : 0.0;
                            entryModel.setBasePrice(basePrice);
                            entryModel.setTotalPrice(entryModel.getQuantityInMT() * entryModel.getBasePrice());
                            entryModel.setBrand(salesHistoryModel.getBrand()!=null ? salesHistoryModel.getBrand().getUid():"");
                            entryModel.setCustomerTransactionId(salesHistoryModel.getCustomerTransactionId());
                            entryModel.setCustomerTransactionLineId(salesHistoryModel.getCustomerTransactionLineId());
                            modelService.save(entryModel);

                            salesHistoryModel.setSynced(synced);
                            modelService.save(salesHistoryModel);
                        }
                    }
                }
            }
            //Case 3: get all NCR not having entry in Order Entry
            List<SalesHistoryModel> list = getNCREntriesNotExistingInOrderEntry();
            List<SalesHistoryModel> lineItemIdList = list.stream().distinct().collect(Collectors.toList());

            if (lineItemIdList != null && !lineItemIdList.isEmpty()) {
                LOG.info("sending missing erp order entries to slct");
                LOG.info("line item id list size" + lineItemIdList.size());
                eydmsMissingOrderEntriesToSLCT.sendErpLineItemIdToSLCT(lineItemIdList);
            }
            return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
        }
        catch (final Exception e)
        {
            LOG.error("Exception occurred during Order sales replication job", e);
            return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
        }
    }

    private void updateLastLiftingAndNetworkType(OrderEntryModel orderEntry, EyDmsCustomerModel cus, Date existingLastLiftingDate) {
        if(existingLastLiftingDate !=null && orderEntry.getInvoiceCreationDateAndTime()!=null)
        {
            if(existingLastLiftingDate.compareTo(orderEntry.getInvoiceCreationDateAndTime())<0)
            {
                cus.setLastLiftingDate(orderEntry.getInvoiceCreationDateAndTime());
                cus.setNetworkType(NetworkType.ACTIVE.getCode());
                modelService.save(cus);
                modelService.refresh(cus);
            }
        }
        else
        {
            cus.setLastLiftingDate(orderEntry.getInvoiceCreationDateAndTime());
            cus.setNetworkType(NetworkType.ACTIVE.getCode());
            modelService.save(cus);
            modelService.refresh(cus);
        }
    }

    public EyDmsMissingOrderEntriesToSLCT getEyDmsMissingOrderEntriesToSLCT() {
        return eydmsMissingOrderEntriesToSLCT;
    }

    public void setEyDmsMissingOrderEntriesToSLCT(EyDmsMissingOrderEntriesToSLCT eydmsMissingOrderEntriesToSLCT) {
        this.eydmsMissingOrderEntriesToSLCT = eydmsMissingOrderEntriesToSLCT;
    }

    public List<SalesHistoryModel> getNCREntriesNotExistingInOrderEntry() {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {s:pk} FROM {SalesHistory as s LEFT JOIN OrderEntry as oe on {oe:erpLineItemId}={s:lineId}} WHERE {s.synced}=?synced and {oe:pk} is null and {s:quantity} > 0");
        params.put("synced", false);

        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(SalesHistoryModel.class));
        query.addQueryParameters(params);
        final SearchResult<SalesHistoryModel> searchResult = getFlexibleSearchService().search(query);
        return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
    }

    public List<SalesHistoryModel> getNCREntriesExistingInOrderEntry() {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {s:pk} FROM {SalesHistory as s JOIN OrderEntry as oe on {oe:erpLineItemId}={s:lineId}} WHERE {s.synced}=?synced and {s:quantity} > 0");
        params.put("synced", false);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(SalesHistoryModel.class));
        query.addQueryParameters(params);
        final SearchResult<SalesHistoryModel> searchResult = getFlexibleSearchService().search(query);
        return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
    }

    public OrderEntryModel findOrderEntryByErpLineItemId(String erpLineItemId) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {oe.pk} from {OrderEntry as oe join Order as o on {oe.order}={o.pk}} where {oe.erpLineItemId} = ?erpLineItemId");

        params.put("erpLineItemId", erpLineItemId);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(OrderEntryModel.class));
        query.addQueryParameters(params);
        final SearchResult<OrderEntryModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
        else
            return null;
    }

    public List<SalesHistoryModel> getNCRNegativeQtyEntriesExistingInOrderEntry() {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {s:pk} FROM {SalesHistory as s JOIN OrderEntry as oe on {oe:erpLineItemId}={s:lineId}} WHERE {s.synced}=?synced and {s:quantity} < 0");
        params.put("synced", false);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(SalesHistoryModel.class));
        query.addQueryParameters(params);
        final SearchResult<SalesHistoryModel> searchResult = getFlexibleSearchService().search(query);
        return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
    }

    private void updateReceipts(ProductModel productCode, EyDmsCustomerModel dealerCode, Double invoicedQuantity) {
        try {
            ReceiptAllocaltionModel receiptAllocate = getDealerAllocation(productCode, dealerCode);
            if (null != receiptAllocate) {
                Double updatedQty = receiptAllocate.getReceipt() + invoicedQuantity;
                receiptAllocate.setReceipt((null != updatedQty)?updatedQty.intValue():0);
                Integer salesToRetailer = receiptAllocate.getSalesToRetailer()!=null ? receiptAllocate.getSalesToRetailer() : 0;
                Integer salesToInfluencer = receiptAllocate.getSalesToInfluencer()!=null ? receiptAllocate.getSalesToInfluencer() :0;
                int stockRetailer = Math.abs(receiptAllocate.getReceipt() - salesToRetailer
                        - salesToInfluencer);
                receiptAllocate.setStockAvlForRetailer(stockRetailer);
                int stockInfluencer = Math.abs((int) ((0.7 * (receiptAllocate.getReceipt() - salesToRetailer))
                        - salesToInfluencer));
                receiptAllocate.setStockAvlForInfluencer(stockInfluencer);
                modelService.save(receiptAllocate);
            } else {
                //If product and dealer is not found in the ReceiptAllocation
                //then it means new entry has to be made as order is placed with this combination
                ReceiptAllocaltionModel receiptAllocateNew = modelService.create(ReceiptAllocaltionModel.class);
                receiptAllocateNew.setProduct(productCode.getPk().toString());
                receiptAllocateNew.setDealerCode(dealerCode.getPk().toString());
                Double updatedQty = invoicedQuantity;
                receiptAllocateNew.setReceipt((null != updatedQty)?updatedQty.intValue():0);
                receiptAllocateNew.setSalesToRetailer(0);
                receiptAllocateNew.setSalesToInfluencer(0);
                int stockRetailer = Math.abs(receiptAllocateNew.getReceipt() - receiptAllocateNew.getSalesToRetailer()
                        - receiptAllocateNew.getSalesToInfluencer());
                receiptAllocateNew.setStockAvlForRetailer(stockRetailer);
                int stockInfluencer = Math.abs((int) ((0.7 * (receiptAllocateNew.getReceipt() - receiptAllocateNew.getSalesToRetailer()))
                        - receiptAllocateNew.getSalesToInfluencer()));
                receiptAllocateNew.setStockAvlForInfluencer(stockInfluencer);
                modelService.save(receiptAllocateNew);
                modelService.refresh(receiptAllocateNew);
            }
        } catch (Exception e) {
            String errorMsg = e.getMessage()!=null?e.getMessage():e.getClass().getName() + " Order Replication Job updateReceipts Occurred";
            throw new UnknownIdentifierException(errorMsg);
        }

    }

    public ReceiptAllocaltionModel getDealerAllocation(ProductModel productCode, EyDmsCustomerModel dealerCode) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {ReceiptAllocaltion} WHERE {dealerCode}=?dealerCode AND {product}=?product");

        params.put("dealerCode", dealerCode);
        params.put("product", productCode);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(ReceiptAllocaltionModel.class));
        query.addQueryParameters(params);
        final SearchResult<ReceiptAllocaltionModel> searchResult = flexibleSearchService.search(query);
        LOG.info("In DealerDao:getDealerAllocation method--> query:::" + builder.toString() + ":::Product Code:::" + productCode.getPk().toString() + ":::Dealer Code:::" + dealerCode.getPk().toString());
        LOG.info("In DealerDao:getDealerAllocation method--> Show the result of the query:::" + searchResult.getResult());
        if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : null;
        else
            return null;
    }

    private OrderModel findOrderbyErpOrderNoAndOrderType(String erpOrderNumber, String erpOrderType) {

        final Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("erpOrderNumber", erpOrderNumber);
        attr.put("erpOrderType", erpOrderType);

        String queryResult="SELECT {o:pk} from {Order as o} where {o.erpOrderNumber}=?erpOrderNumber and {o.erpOrderType}=?erpOrderType ";

        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
        query.getQueryParameters().putAll(attr);
        final SearchResult<OrderModel> result = flexibleSearchService.search(query);
        if(result.getResult() != null && !result.getResult().isEmpty())
        {
            return result.getResult().get(0);
        }
        else {
            return null;
        }
    }

    private OrderModel findOrderByErpLineId(String erpLineItemId) {
        final Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("erpLineItemId", erpLineItemId);
        String queryResult="select {o:pk} from {Order as o join OrderEntry as oe on {oe:order}={o:pk}} where {oe:erpLineItemId} =?erpLineItemId";
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
        query.getQueryParameters().putAll(attr);
        final SearchResult<OrderModel> result = flexibleSearchService.search(query);
        if(result.getResult() != null && !result.getResult().isEmpty())
        {
            return result.getResult().get(0);
        }
        else {
            return null;
        }
    }

    private int addEntryNumber(AbstractOrderModel order)
    {
        LOG.info("adding entry number");
        int largestEntryNumber = findLargestEntryNumber(order);
        int currentEntryNumber = largestEntryNumber + 1;
        return currentEntryNumber;
    }
    private int findLargestEntryNumber(AbstractOrderModel order)
    {
        LOG.info("finding largest entry number");
        int largestEntryNumber = 2999;
        List<AbstractOrderEntryModel> entries = order.getEntries();
        if(entries!=null && !entries.isEmpty())
        {
            for (AbstractOrderEntryModel entry : entries) {
                if(entry.getEntryNumber() >= 3000 && entry.getEntryNumber() > largestEntryNumber)
                {
                    largestEntryNumber = entry.getEntryNumber();
                }
            }

        }
        LOG.info("largest entry number ::"+largestEntryNumber);
        return largestEntryNumber;
    }

    public OrderEntryModel findOrderEntryByNcrCombination(String customerTransactionId, String customerTransactionLineId, String brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        //final StringBuilder builder = new StringBuilder("select {oe.pk} from {SalesHistory as s JOIN OrderEntry as oe on {oe:customerTransactionId}={s:customerTransactionId} and {oe:customerTransactionLineId}={s:customerTransactionLineId} and {oe:brand}={s:brand}} where {s.customerTransactionId} =?customerTransactionId and {s:customerTransactionLineId} =?customerTransactionLineId and {s:brand}=?brand");
        final StringBuilder builder = new StringBuilder("select {oe.pk} from {OrderEntry as oe} where {oe.customerTransactionId} =?customerTransactionId and {oe:customerTransactionLineId} =?customerTransactionLineId and {oe:brand}=?brand");
        params.put("customerTransactionId", customerTransactionId);
        params.put("customerTransactionLineId", customerTransactionLineId);
        params.put("brand", brand);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(OrderEntryModel.class));
        query.addQueryParameters(params);
        final SearchResult<OrderEntryModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
        else
            return null;
    }

    public List<SalesHistoryModel> getNCREntriesByNCRCombination() {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {s.pk} from {SalesHistory as s JOIN OrderEntry as oe on {oe:customerTransactionId}={s:customerTransactionId} and {oe:customerTransactionLineId}={s:customerTransactionLineId} and {oe:brand}={s:brand}} where {s:synced} =?synced");
        params.put("synced", false);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(SalesHistoryModel.class));
        query.addQueryParameters(params);
        final SearchResult<SalesHistoryModel> searchResult = getFlexibleSearchService().search(query);
        return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
    }

    public FlexibleSearchService getFlexibleSearchService() {
        return flexibleSearchService;
    }

    @Override
    public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }
}