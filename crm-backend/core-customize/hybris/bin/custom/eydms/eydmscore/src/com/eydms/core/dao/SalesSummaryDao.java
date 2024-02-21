package com.eydms.core.dao;

import com.eydms.core.model.NirmanMitraSalesHistoryModel;
import com.eydms.core.model.OrderRequisitionModel;
import com.eydms.core.model.RetailerSalesSummaryModel;
import com.eydms.core.model.EyDmsCustomerModel;

import java.util.Date;
import java.util.List;

public interface SalesSummaryDao {
    RetailerSalesSummaryModel validateRecordFromSalesSummary(String month, String year, String dealerNo, String retailerNo, Date startDate1, Date endDate1);
    //replaced method for nirman mitra
    List<List<Object>> getCustomerFromOrderRequisitionForUpload(String salesSummaryJobStatus, EyDmsCustomerModel eydmsCustomer, Date startDate,Date endDate);
    OrderRequisitionModel updateJobStatusForOrderRequisition(EyDmsCustomerModel eydmsCustomerModel, String salesSummaryJobStatus);
    List<List<Object>> getCustomerFromOrderRequisitionForUploadTest(String salesSummaryJobStatus, EyDmsCustomerModel eydmsCustomer);
}
