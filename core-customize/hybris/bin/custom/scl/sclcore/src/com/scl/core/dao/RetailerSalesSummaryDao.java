package com.scl.core.dao;

import com.scl.core.model.RetailerSalesSummaryModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.Date;
import java.util.List;

public interface RetailerSalesSummaryDao {

    SearchPageData<RetailerSalesSummaryModel> findRetailerSalesBySubareaAndMonthAndYear(String subArea, Date startDate, Date endDate, SearchPageData searchPageData, String filter);
}
