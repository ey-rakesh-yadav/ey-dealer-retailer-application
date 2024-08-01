package com.scl.core.dao;

import com.scl.core.model.NirmanMitraSalesHistoryModel;
import com.scl.core.model.OrderRequisitionModel;
import com.scl.core.model.RetailerSalesSummaryModel;
import com.scl.core.model.SclCustomerModel;

import java.util.Date;
import java.util.List;

public interface SalesSummaryDao {
    RetailerSalesSummaryModel validateRecordFromSalesSummary(String month, String year, String dealerNo, String retailerNo, Date startDate1, Date endDate1);
    //replaced method for nirman mitra
    List<List<Object>> getCustomerFromOrderRequisitionForUpload(String salesSummaryJobStatus, SclCustomerModel sclCustomer, Date startDate,Date endDate);
    OrderRequisitionModel updateJobStatusForOrderRequisition(SclCustomerModel sclCustomerModel, String salesSummaryJobStatus);
    List<List<Object>> getCustomerFromOrderRequisitionForUploadTest(String salesSummaryJobStatus, SclCustomerModel sclCustomer);
}
